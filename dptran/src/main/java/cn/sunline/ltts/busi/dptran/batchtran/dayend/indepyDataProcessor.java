package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.dp.dayend.DpDayEndInt;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProd;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.InBeinTranData;
import cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Instpy.Input;
import cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Instpy.Property;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;
	 /**
	  * 定期结息
	  *
	  */

public class indepyDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Indepy.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Indepy.Property, cn.sunline.ltts.busi.dp.type.DpDayEndType.InBeinTranData> {
	  
	private final static BizLog bizlog = BizLogUtil.getBizLog(indepyDataProcessor.class);
	
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.dp.type.DpDayEndType.InBeinTranData dataItem, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Indepy.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Indepy.Property property) {
				
			String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
			String oldTranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
			CommTools.getBaseRunEnvs().setBusi_org_id(dataItem.getCorpno()); //交易前设置法人
			CommTools.getBaseRunEnvs().setTrxn_branch(dataItem.getCorpno()+"000");//设置交易机构
			String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
			
			
			KnbAcin tblKnb_acin = KnbAcinDao.selectOneWithLock_odb1(dataItem.getAcctno(), true);
			E_INTRTP intrtp = E_INTRTP.ZHENGGLX;			//利息类型默认为"正利息"
			//kna_accs accs = Kna_accsDao.selectOne_odb2(dataItem.getAcctno(), true);
			IoCaKnaAccs accs = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAccsOdb2(dataItem.getAcctno(), true);
			
			if(accs.getAcctst() == E_DPACST.NORMAL){
				
				//获取报文头中的备注字段
				String remarks = BusiTools.getBusiRunEnvs().getRemark();
				
				//若结息日与到期日一致则跳过结息，统一到日终到期结息批量处理
				KnaFxac tblknafxac = KnaFxacDao.selectOne_odb1(accs.getAcctno(), true);
				if(CommUtil.equals(trandt, tblknafxac.getMatudt())){
					return;
				}
				
				//很据负债账号获取可售产品名称
//				kna_acct_prod tblKnaAcctProd = DpAcctDao.selKnaAcctProdByAcctno(dataItem.getAcctno(), false);
				KnaFxacProd tblKnaFxacProd = DpAcctDao.selKnaFxacProdByAcctno(dataItem.getAcctno(), false);
				
				
				//判断可售产品名称是否为空，若为空则去产品名称
				if(CommUtil.isNotNull(tblKnaFxacProd)){
					if(CommUtil.isNotNull(tblKnaFxacProd.getObgaon())){
						BusiTools.getBusiRunEnvs().setRemark(tblKnaFxacProd.getObgaon());
					}else {
						//根据产品号获取产品名称
						KupDppb kupdppb = DpProductDao.selkupdppbbyprodcds(accs.getProdcd(),dataItem.getCorpno(), true);
						BusiTools.getBusiRunEnvs().setRemark(kupdppb.getProdtx());
					}
				}
				
				//定期结息处理,定期结息的时候扫描支取账户
				if (intrtp == tblKnb_acin.getIntrtp()){
					tblKnb_acin.setPlanin(dataItem.getIntest()); //设置结息金额为knb_cbdl获取的上一日结息金额
					//定期结息
					DpDayEndInt.prcPay(tblKnb_acin, accs.getCustac());
				}
			
				//将原备注插入报文头
				BusiTools.getBusiRunEnvs().setRemark(remarks);
			}
			//更新结息账号状态为处理成功
			bizlog.debug("结息账号：", dataItem.getAcctno());
			
			
			CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno); //交易后设置法人为原法人
			CommTools.getBaseRunEnvs().setTrxn_branch(oldTranbr); //交易后设置交易机构为原交易机构
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.dp.type.DpDayEndType.InBeinTranData> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Indepy.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Indepy.Property property) {
			
			//获取日期
			ApSysDateStru cplDateStru = DateTools2.getDateInfo();
			String trandt = cplDateStru.getSystdt();
			String lastdt = cplDateStru.getLastdt();
			
			Params params = new Params();
			params.add("trandt", trandt);
			params.add("lastdt", lastdt);
			params.add("pddpfg", E_FCFLAG.FIX);
			params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
			
			return new CursorBatchDataWalker<InBeinTranData>(
					DpDayEndDao.namedsql_seldepy, params);
		}
		
		public void jobExceptionProcess(String taskId, Input input,Property property, String jobId, InBeinTranData dataItem,Throwable t) {
			/*
			//监控预警平台
			KnpParameter para = KnpParameterDao.selectOne_odb1("DAYENDNOTICE", "%", "%", "%", true);
			
			String bdid = para.getParm_value1();// 服务绑定ID
			
			String mssdid = CommTools.getMySysId();// 随机生成消息ID
			
			String mesdna = para.getParm_value2();// 媒介名称
			
			//E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介
			
			IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
			
			IoCaOtherService.IoCaDayEndFailNotice.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaDayEndFailNotice.InputSetter.class);
			
			String timetm = DateTools2.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS000");
			IoCaBatchWarnInfo content = SysUtil.getInstance(IoCaBatchWarnInfo.class);
			content.setPljioyma("instpy");
			content.setPljyzbsh("2006");
			content.setPljyzwmc("定期结息异常预警");
			content.setErrmsg("定期结息失败");
			content.setTrantm(timetm);
			
			// 发送消息
			mqInput.setMsgid(mssdid); // 消息ID
			//mqInput.setMedium(mssdtp); // 消息媒介
			mqInput.setMdname(mesdna); // 媒介名称
			mqInput.setTypeCode("NAS");
			mqInput.setTypeName("网络金融核心平台-电子账户核心系统");
			mqInput.setItemId("NAS_BATCH_WARN");
			mqInput.setItemName("电子账户核心批量执行错误预警");
			
			String str =JSON.toJSONString(content);
			mqInput.setContent(str);
			
			mqInput.setWarnTime(timetm);
			
			caOtherService.dayEndFailNotice(mqInput);
			*/
		}

}


