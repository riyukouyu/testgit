package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import java.util.Map;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
//import com.alibaba.fastjson.JSON;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.dp.dayend.DpDayEndInt;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcinDao;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbBein;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbBeinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbBeinInfo;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbBeinInfoDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProd;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.InBeinTranData;
import cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Instpy.Input;
import cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Instpy.Property;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaBatchWarnInfo;
//import cn.sunline.ltts.busi.ltts.zjrc.infrastructure.dap.plugin.type.ECustPlugin.E_MEDIUM;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BEINST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;

	 /**
	  * 结息测试
	  *
	  */

public class instpyDataProcessor extends
AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Instpy.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Instpy.Property, cn.sunline.ltts.busi.dp.type.DpDayEndType.InBeinTranData> {
	
		
	private final static BizLog bizlog = BizLogUtil.getBizLog(BizLog.class);
	/**
		 * 批次数据项处理逻辑。
		 * 
		 * @param job 批次作业ID
		 * @param index  批次作业第几笔数据(从1开始)
		 * @param dataItem 批次数据项
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 */
		@Override
	public void process(String jobId, int index, cn.sunline.ltts.busi.dp.type.DpDayEndType.InBeinTranData dataItem, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Instpy.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Instpy.Property property) {
		
//		String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		String oldTranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
//		CommTools.getBaseRunEnvs().setBusi_org_id(dataItem.getCorpno()); //交易前设置法人
		CommTools.getBaseRunEnvs().setTrxn_branch(dataItem.getCorpno()+"000");//设置交易机构
		
		//更新结息账号状态为处理中
		KnbBein tbknbbein = SysUtil.getInstance(KnbBein.class);
		tbknbbein = KnbBeinDao.selectOne_odb2(dataItem.getTrandt(), dataItem.getAcctno(), false);
		tbknbbein.setBeinst(E_BEINST.DOING);
		KnbBeinDao.updateOne_odb2(tbknbbein);
		
		KnbAcin tblKnbAcin = KnbAcinDao.selectOneWithLock_odb1(dataItem.getAcctno(), true);
		E_INTRTP intrtp = E_INTRTP.ZHENGGLX;			//利息类型默认为"正利息"
		//kna_accs accs = Kna_accsDao.selectOne_odb2(dataItem.getAcctno(), true);
		IoCaKnaAccs accs = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAccsOdb2(dataItem.getAcctno(), true);
		
		if(accs.getAcctst() == E_DPACST.NORMAL){
			
			//获取报文头中的备注字段
			String remarks = BusiTools.getBusiRunEnvs().getRemark();
			
			//很据负债账号获取可售产品名称
			KnaAcctProd tblKnaAcctProd = DpAcctDao.selKnaAcctProdByAcctno(dataItem.getAcctno(), false);
//			KnaFxacProd tblKnaFxacProd = DpAcctDao.selKnaFxacProdByAcctno(dataItem.getAcctno(), false);
			
			
			//判断可售产品名称是否为空，若为空则去产品名称
			if(CommUtil.isNotNull(tblKnaAcctProd)){
				if(CommUtil.isNotNull(tblKnaAcctProd.getObgaon())){
					BusiTools.getBusiRunEnvs().setRemark(tblKnaAcctProd.getObgaon());
				}else {
					//根据产品号获取产品名称
					KupDppb kupdppb = DpProductDao.selkupdppbbyprodcds(accs.getProdcd(),CommTools.getBaseRunEnvs().getBusi_org_id() , false);
					BusiTools.getBusiRunEnvs().setRemark(kupdppb.getProdtx());
				}
			}/*else if(CommUtil.isNotNull(tblKnaFxacProd)){
				if(CommUtil.isNotNull(tblKnaFxacProd.getObgaon())){
					BusiTools.getBusiRunEnvs().setRemark(tblKnaFxacProd.getObgaon());
				}else {
					//根据产品号获取产品名称
					KupDppb kupdppb = DpProductDao.selkupdppbbyprodcds(accs.getProdcd(), false);
					BusiTools.getBusiRunEnvs().setRemark(kupdppb.getProdtx());
				}
			}else {
				//根据产品号获取产品名称
				KupDppb kupdppb = DpProductDao.selkupdppbbyprodcds(accs.getProdcd(), false);
				if(CommUtil.isNotNull(kupdppb)){
					//将产品名称附到报文头中
					BusiTools.getBusiRunEnvs().setRemark(kupdppb.getProdtx());
				}
			}*/
//			//很据产品好获取产品名称
//			KupDppb kupdppb = DpProductDao.selkupdppbbyprodcds(accs.getProdcd(), false);
//			
//			if(CommUtil.isNotNull(kupdppb)){
//				//将产品名称附到报文头中
//				BusiTools.getBusiRunEnvs().setRemark(kupdppb.getProdtx());
//			}
			
			
			/* 双冻也结息，这里取消双冻控制  update by renjh in 20161116 */
//			boolean flag = DpFrozTools.isFroz(E_FROZOW.AUACCT, accs.getCustac(), E_YES___.YES, E_YES___.NO, E_YES___.YES);
//			// 不能进的冻结，写入登记簿，直接退出
//			if (flag) {
//				//写入登记簿 
//				knb_froz_edct tblKnbfrozedct = SysUtil
//						.getInstance(knb_froz_edct.class);
//				tblKnbfrozedct.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
//				tblKnbfrozedct.setCustac(accs.getCustac());
//				tblKnbfrozedct.setAcctno(dataItem.getAcctno());
//				tblKnbfrozedct.setFailds("已被冻结，结息失败。");
//				Knb_froz_edctDao.insert(tblKnbfrozedct);
//				
//				// 冻结不结息，但是要更新下次结息日
//				String nextpaydt = tblKnbAcin.getNcindt();
//
//				// 计算下次结息日期
//				if (CommUtil.isNotNull(tblKnbAcin.getTxbefr())) {
//
//					nextpaydt = DpPublic.getNextPeriod(nextpaydt, CommTools
//							.prcRunEnvs().getNxtrdt(), tblKnbAcin.getTxbefr());
//				}
//				tblKnbAcin.setNcindt(nextpaydt); // 下次结息日
//				KnbAcinDao.updateOne_odb1(tblKnbAcin);
//				//更新结息账号状态为处理失败
//				tbknbbein.setBeinst(E_BEINST.FAIL);
//				tbknbbein.setRemark("已被冻结，结息失败。");
//				KnbBeinDao.updateOne_odb2(tbknbbein);
//				return;
//			}
			
			//活期结息处理,活期结息的时候扫描支取账户
			if (intrtp == tblKnbAcin.getIntrtp()){
				tblKnbAcin.setPlanin(dataItem.getIntest()); //设置结息金额为knb_cbdl获取的上一日结息金额 + 调整金额
				//活期结息
				DpDayEndInt.prcPay(tblKnbAcin, accs.getCustac());
				
				//更新利息调整记录 jym add
				DpAcinDao.updKnbCbdlFixStatus(dataItem.getCorpno(), CommTools.getBaseRunEnvs().getTrxn_date(), 
						dataItem.getAcctno(), tblKnbAcin.getCrcycd(),tblKnbAcin.getProdcd());
			}
		
			//将原备注插入报文头
			BusiTools.getBusiRunEnvs().setRemark(remarks);
		}
		//更新结息账号状态为处理成功
		tbknbbein.setBeinst(E_BEINST.SUCC);
		KnbBeinDao.updateOne_odb2(tbknbbein);
		bizlog.debug("结息账号："+dataItem.getAcctno());
		
		
//		CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno); //交易后设置法人为原法人
		CommTools.getBaseRunEnvs().setTrxn_branch(oldTranbr); //交易后设置交易机构为原交易机构
		
//			throw CaError.Eacct.E0001("错误回滚==========acctno:"+dataItem.getAcctno());
	}
		
	@Override
	public BatchDataWalker<cn.sunline.ltts.busi.dp.type.DpDayEndType.InBeinTranData> getBatchDataWalker(
			Input input, Property property) {		
	
		String trandt = DateTools2.getDateInfo().getSystdt();
		//String trandt = "20160322";

		Params params = new Params();
		params.add("trandt", trandt);
//		return new CursorBatchDataWalker<InBeinTranData>(
//				DpDayEndDao.namedsql_selInstpyAcctData, params);
		return new CursorBatchDataWalker<InBeinTranData>(
				DpDayEndDao.namedsql_selbeinData, params);
		

		
		
	}
	
	
	@Override
	public void beforeTranProcess(String taskId, Input input,
				Property property) {
		bizlog.debug("------进入结息批量前处理方法------");
		
		ApSysDateStru cplDateStru = DateTools2.getDateInfo();
		String trandt = cplDateStru.getSystdt();
		String lastdt = cplDateStru.getLastdt();
		
		String timetm = DateTools2.getCurrentTimestamp();
		
		String head = "6000"; //子账户前四位
		int headLen = 4; //子账户前几位
		int sum = 0; //该批次总笔数
		
		BusiTools.getBusiRunEnvs().setSmrycd(BusinessConstants.SUMMARY_SX);//摘要默认付息
		//String trandt = "20160322";
		int seq = 0; //批次记录序号
		String minacct = ""; //批次最小账号
		String minNumber = ""; //批次最小序号
		String maxacct = ""; //批次最大账号
		String lastMaxacct = ""; //上批次最大账号
		//获取批次开始时间
        String time= DateTools2.getCurrentLocalTime();
        
        Map<String,Object> lastbatch = DpDayEndDao.selbeinmaxacct(trandt, false);
		
		
		KnpParameter tbKnpParameter = KnpParameterDao.selectOne_odb1("BeinBatchSize", "%", "%", "%", false);
		String size = tbKnpParameter.getParm_value1();//结息批次账号分段数量
		if(CommUtil.isNull(size)){
			throw DpModuleError.DpstAcct.BNAS1740();
		}
		
		Long number = Long.parseLong(size); //参数配置的没批次数量
		
		
		//子账号规则为6000+19位的序号,其他规则需要改造,每次取参数配置批次量进行结息
		if(CommUtil.isNull(lastbatch)){ //当日首次结息
			
			minNumber = "0000000000000000000";
			minacct = head.concat(minNumber);
			lastMaxacct = minacct;
			
		}else{
			seq = (int)lastbatch.get("seqno");
			lastMaxacct = (String)lastbatch.get("acctno");
			Long currminno = (ConvertUtil.toLong(lastMaxacct.substring(headLen)) + 1);
			minacct = head.concat(CommUtil.lpad(currminno.toString(),19,"0"));
			minNumber = lastMaxacct.substring(headLen);
		}
		
		Long lMaxNumber = ConvertUtil.toLong(minNumber) + number;
		maxacct = head.concat(CommUtil.lpad(lMaxNumber.toString(), 19, "0"));
		
		//查询当前最大活期账号
		String currMaxAcct = DpDayEndDao.selMaxAcctnoForKnaAcct(false);
		
		bizlog.debug("========当前最大账号为[%s]", currMaxAcct);
		
		if(CommUtil.isNull(currMaxAcct)){
			return;
		}
		
		bizlog.debug("========最小账号为[%s]，最大账号为[%s]", minacct, maxacct);
		
		//根据初始化的最大最小账号查询批次数据，如果未查询到结果则循环下一批次数据
		while(true){
			
			//将待结息批次分段数据插入KnbBein
			int count = DpDayEndDao.insknbbein(trandt, E_FCFLAG.CURRENT, maxacct, minacct,lastdt,timetm);
			
			//批次总数据
			sum = count;
			
			if(sum > 0){
				seq = seq + 1; //批次序号加1
				break;
			}else{
				
				//如果批次最大账号大于等于活期账户当前最大账号，并且未取到结息数据则返回
				if(CommUtil.compare(maxacct, currMaxAcct) >= 0){
					break;
				}
				
				Long lMinno = ConvertUtil.toLong(maxacct.substring(headLen)) + 1;
				minacct = head.concat(CommUtil.lpad(lMinno.toString(), 19, "0"));
				Long lMaxno = ConvertUtil.toLong(maxacct.substring(headLen)) + number;
				maxacct = head.concat(CommUtil.lpad(lMaxno.toString(), 19, "0"));
				
			}
			
			bizlog.debug("========最小账号为[%s]，最大账号为[%s]", minacct, maxacct);
		}
		
		//批次序号大于0，则记录批次最大账号及相关信息
		if(CommUtil.isNotNull(seq) && CommUtil.compare(seq, 0) > 0  
				&& CommUtil.isNotNull(sum) && CommUtil.compare(sum,0) > 0){
			
			KnbBeinInfo tbknbbeininfo = SysUtil.getInstance(KnbBeinInfo.class);
			//记录批次数据相关信息
			tbknbbeininfo.setTrandt(trandt);
			tbknbbeininfo.setSeqno(seq);
			tbknbbeininfo.setBgtime(time);
			tbknbbeininfo.setAcctno(maxacct);
			tbknbbeininfo.setTotal(sum);
			KnbBeinInfoDao.insert(tbknbbeininfo); //插入批次记录信息
		}
		
		//设置属性报文
		property.setTrandt(trandt);
		property.setSum(sum);
		property.setSeq(seq);
		
	}					
		
	@Override
	public void afterJobProcess(String taskId, Input input,
				Property property, String jobId, int totalSuccessCount,
				int totalErrorCount) {

	}
	
	@Override
	public void jobExceptionProcess(String taskId, Input input,
				Property property, String jobId, InBeinTranData dataItem,
				Throwable t) {
		//更新结息账号状态为处理失败
		KnbBein tbknbbein = SysUtil.getInstance(KnbBein.class);
		tbknbbein = KnbBeinDao.selectOne_odb2(dataItem.getTrandt(), dataItem.getAcctno(), false);
		tbknbbein.setBeinst(E_BEINST.FAIL);
		tbknbbein.setRemark(t.toString());
		KnbBeinDao.updateOne_odb2(tbknbbein);
		
		//监控预警平台
		KnpParameter para = KnpParameterDao.selectOne_odb1("DAYENDNOTICE", "%", "%", "%", true);
		
		String bdid = para.getParm_value1();// 服务绑定ID
		
		String mssdid = CommTools.getMySysId();// 随机生成消息ID
		
		String mesdna = para.getParm_value2();// 媒介名称
		
//		E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介
		
		IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
		
		IoCaOtherService.IoCaDayEndFailNotice.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaDayEndFailNotice.InputSetter.class);
		
		String timetm = DateTools2.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS000");
		IoCaBatchWarnInfo content = SysUtil.getInstance(IoCaBatchWarnInfo.class);
		content.setPljioyma("instpy");
		content.setPljyzbsh("2006");
		content.setPljyzwmc("日终结息异常预警");
		content.setErrmsg("日终结息失败");
		content.setTrantm(timetm);
		
		// 发送消息
		mqInput.setMsgid(mssdid); // 消息ID
//		mqInput.setMedium(mssdtp); // 消息媒介
		mqInput.setMdname(mesdna); // 媒介名称
		mqInput.setTypeCode("NAS");
		mqInput.setTypeName("网络金融核心平台-电子账户核心系统");
		mqInput.setItemId("NAS_BATCH_WARN");
		mqInput.setItemName("电子账户核心批量执行错误预警");
		
//		String str =JSON.toJSONString(content);
//		mqInput.setContent(str);
		
		mqInput.setWarnTime(timetm);
		
		caOtherService.dayEndFailNotice(mqInput);
	}
	
	@Override
	public void afterTranProcess(String taskId, Input input,
				Property property) {
		
		bizlog.debug("=======结息批次处理完成======");
		int seq = 0;
		String trandt = property.getTrandt();
		if(CommUtil.isNotNull(property.getSeq())){
			seq = property.getSeq();
		}
		
		
		int sum = CommUtil.nvl(property.getSum(), 0);
		
		//更新批次内结息成功的账号数量
		if(CommUtil.isNotNull(trandt)&&CommUtil.isNotNull(seq)&&CommUtil.compare(seq, 0)>0
				&& CommUtil.isNotNull(sum) && CommUtil.compare(sum,0) > 0){
			int succSum = DpDayEndDao.selbeinSuccCount(trandt, false);
			
			KnbBeinInfo tbknbbeininfo = KnbBeinInfoDao.selectOne_odb2(trandt, seq, true);
			tbknbbeininfo.setSuccsum(succSum);
			KnbBeinInfoDao.updateOne_odb2(tbknbbeininfo);
			
			//更新成功的数据到错误信息表中
			DpDayEndDao.updKnbBeinErorForSucc();
			
			//把结息数据批次表中失败的数据移到错误信息表中
			DpDayEndDao.insKnbBeinErorByFailure(trandt, seq);
			
			//删除结息批次明细表内结息的数据
			DpDayEndDao.truncateKnbBein();
		}
	}

}


