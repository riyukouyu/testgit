package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpOtherService;
import cn.sunline.ltts.busi.iobus.type.pb.IoIntrComplexType.IoKupRlir;
import cn.sunline.ltts.busi.iobus.type.pb.IoIntrComplexType.IoMaxir;
import cn.sunline.ltts.busi.pb.namedsql.intr.ProintrSelDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRSRPF;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

	 /**
	  * 利率代码最大利率值发送MQ到移动前端
	  * @author leipeng
	  *
	  */
	  

public class maxnirDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Maxnir.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Maxnir.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	private static final BizLog bizlog = BizLogUtil
			.getBizLog(maxnirDataProcessor.class);
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Maxnir.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Maxnir.Property property) {
		 bizlog.debug("--------------最大利率定时查询开始-------------------");
	     String trantm = CommTools.getBaseRunEnvs().getTrxn_date() + BusiTools.getBusiRunEnvs().getTrantm();
	     
	     String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//		 E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE; // 消息媒介

		 KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("DPUPGD", "INTRSM",
				"%", "%", true);
		
		 String bdid = tblKnaPara.getParm_value1();// 服务绑定ID
		
		 IoDpOtherService dpOtherService = SysUtil.getInstanceProxyByBind(
				IoDpOtherService.class, bdid);

		// 1.开户成功发送开户结果到客户信息
		 String mssdid = CommTools.getMySysId();// 消息ID
		 String mesdna = tblKnaPara.getParm_value2();// 媒介名称

		 IoDpOtherService.IoDpSentMaxIntrvlMsg.InputSetter openSendMsgInput = SysUtil.getInstance(IoDpOtherService.IoDpSentMaxIntrvlMsg.InputSetter.class);

//		 openSendMsgInput.setMedium(mssdtp); // 消息媒介
		 openSendMsgInput.setMsgid(mssdid); // 发送消息ID
		 openSendMsgInput.setMdname(mesdna); // 媒介名称
		 
		 
		 //-------------------------查询利率信息，并计算利率值，循环发送MQ-------------------------------------------
		 Options<IoMaxir> list = new DefaultOptions<>();
		 List<IoMaxir> iomaxir = ProintrSelDao.selBrchnoIntrcdIncdtp(corpno,false);
		 list.addAll(iomaxir);
		 for(IoMaxir info : list){
			 //IoBrchInfo brch = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(info.getBrchno());
			 
			 //基础利率通过机构号，利率代码，存期查询出有效的最大范围值
			 if(E_IRCDTP.Reference == info.getIncdtp()){
				 IoMaxir rfirInfo = ProintrSelDao.selMaxRfirToQD( info.getIntrcd(), trantm, info.getDepttm(), corpno, false);
				 
				 info.setIntrvl(rfirInfo.getIntrvl());
				 
			 }else if(E_IRCDTP.BASE == info.getIncdtp()){
		    	 IoMaxir bkirinfo = ProintrSelDao.selMaxBkirToQD(info.getIntrcd(), trantm, info.getDepttm(), corpno, false);
				 
				 //关联的县级机构基础利率无数据就查询上级机构
				 //if(CommUtil.isNull(bkirinfo) && E_BRCHLV.COUNT == brch.getBrchlv()){
		    	 if(CommUtil.isNull(bkirinfo)){
					 bkirinfo= ProintrSelDao.selMaxBkirToQD( info.getIntrcd(), trantm, info.getDepttm(), corpno, false);
				 }
				 
				 info.setIntrvl(bkirinfo.getIntrvl());
				 
			 }else if(E_IRCDTP.LAYER == info.getIncdtp()){
				 
				 /*
				  * 先查出分档利率代码 所有有效的的 档位，在循环查出 最大档位的利率边界值
				  */
				 List<IoKupRlir> rlirInfos = ProintrSelDao.selRlirInfoByIntrcdAndBrchno(info.getIntrcd(), trantm, corpno, false);
				 BigDecimal intrvl = BigDecimal.ZERO;//分档利率最大利率值
				 
				 if(CommUtil.isNotNull(rlirInfos)){
					 for(IoKupRlir rlirInfo : rlirInfos){
						 if(E_IRSRPF.CK == rlirInfo.getIntrsr()){//参考利率
							 
							 IoMaxir infos = ProintrSelDao.selRlirIntrToRfirIntr( rlirInfo.getIntrcd(), trantm, corpno, false);
							 
							 //if(CommUtil.isNull(infos) && E_BRCHLV.COUNT == brch.getBrchlv()){
							 if(CommUtil.isNull(infos)){
								 infos = ProintrSelDao.selRlirIntrToRfirIntr( rlirInfo.getIntrcd(), trantm, corpno, false);

							 }
							 
							 if(CommUtil.isNotNull(infos)){
								 if(CommUtil.compare(infos.getIntrvl(), intrvl)>0){
									 intrvl = infos.getIntrvl();
								 }
							 }
						 }
					 }
				 }
				 
				 info.setIntrvl(intrvl);//重新对利率赋值
			 }
		 
			 //每条利率代码发一次MQ
			 if(CommUtil.isNotNull(info)){
				 openSendMsgInput.setIntinf(info);// 是否开户成功
				 //dpOtherService.dpSentMaxIntrvlMsg(openSendMsgInput);
			 }
		 }
		 
	}

}


