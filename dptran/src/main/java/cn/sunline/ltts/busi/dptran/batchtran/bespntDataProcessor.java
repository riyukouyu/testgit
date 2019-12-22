package cn.sunline.ltts.busi.dptran.batchtran;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
//import com.esotericsoftware.minlog.Log;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccsDao;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.froz.DpAcctStatus;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInterestAndPrincipal;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpClsChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpClsChkOT;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;

	 /**
	  * 休眠前通知
	  *
	  */

public class bespntDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.intf.Bespnt.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Bespnt.Property, String> {
	private final static BizLog bizlog = BizLogUtil.getBizLog(bespntDataProcessor.class);
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
		public void process(String jobId, int index, String dataItem,
				cn.sunline.ltts.busi.dptran.batchtran.intf.Bespnt.Input input,
				cn.sunline.ltts.busi.dptran.batchtran.intf.Bespnt.Property property) {
			
			//设置批量的业务流水，推送消息
			CommTools.getBaseRunEnvs().setBusi_seq(CommTools.getBaseRunEnvs().getTrxn_seq());
			
			String sCustac = dataItem;
			String sUpbldt = "";
			String sCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();// 法人代码
			String sPrcscd = CommTools.getBaseRunEnvs().getTrxn_code();// 交易码
			String sTrandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期
			String sTrantm = BusiTools.getBusiRunEnvs().getTrantm();// 交易时间
			
			KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("sleepday", "%", "%", "%", false);
			if(CommUtil.isNull(tblKnpParameter)){
				throw CaError.Eacct.E0001("未配置休眠控制参数！");
			}
			
			//参数化休眠提醒间隔
			KnpParameter tblknp = KnpParameterDao.selectOne_odb1("bespnt", "%", "%", "%", false);
			if(CommUtil.isNull(tblknp)){
				throw CaError.Eacct.E0001("未配置休眠前提醒间隔参数！");
			}
			
			// 根据电子账户获取电子账户表数据
			IoCaKnaCust cplCaKnaCust = SysUtil.getInstance(
					IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(sCustac,
					false);
			if (CommUtil.isNull(cplCaKnaCust)) {
				//throw DpModuleError.DpstComm.E9999("电子账号不存在! ");
	        	bizlog.error("电子账号不存在! ", sTrandt);
	        	return;
			}
			
			//根据客户号查询客户信息关联表信息
			/*IoCifCustAccs tblcifaccs = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_accsByCustno(cplCaKnaCust.getCustno(), false, E_STATUS.NORMAL);
			
			if(CommUtil.isNull(tblcifaccs)){
				throw DpModuleError.DpstComm.E9999("根据客户号未找到客户信息关联表信息！");
			}*/
			
			//查询电子账户分类
			E_ACCATP accttp = SysUtil.getInstance(
					IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(sCustac);
			

				KnaAcct cplKnaAcct = CapitalTransDeal.getSettKnaAcctAc(sCustac);
				
				if (CommUtil.isNotNull(cplKnaAcct)) {
					sUpbldt = cplKnaAcct.getUpbldt();
				}
			
			
			
//			if (CommUtil.isNotNull(cplKnaAcct)) {
//				sUpbldt = cplKnaAcct.getUpbldt();
//			}

			// 调用获取电子账户状态字方法
			IoDpAcStatusWord cplGetAcStWord = DpAcctStatus.GetAcStatus(sCustac);

			// 部冻、借冻、双冻、部止、全止（银行止付）、全止（外部止付）、质押、存款证明、开单和预授权不允许休眠
			if (cplGetAcStWord.getPtfroz() == E_YES___.YES
					|| cplGetAcStWord.getBrfroz() == E_YES___.YES
					|| cplGetAcStWord.getDbfroz() == E_YES___.YES
					|| cplGetAcStWord.getPtstop() == E_YES___.YES
					|| cplGetAcStWord.getBkalsp() == E_YES___.YES
					|| cplGetAcStWord.getOtalsp() == E_YES___.YES
					|| cplGetAcStWord.getPledge() == E_YES___.YES
					|| cplGetAcStWord.getCertdp() == E_YES___.YES
					|| cplGetAcStWord.getBillin() == E_YES___.YES
					|| cplGetAcStWord.getPreaut() == E_YES___.YES) {
				//throw DpModuleError.DpstComm.E9999("电子账户状态字异常不允许休眠! ");
	        	bizlog.error("电子账户状态字异常不允许休眠!", sTrandt);
	        	return;
			}
			
			// 检查定期余额是否为0
			BigDecimal bigFixddp = DpAcctDao.selFxacAcctBlncByCustac(sCustac, false);
			if (CommUtil.isNotNull(bigFixddp) && CommUtil.compare(bigFixddp, BigDecimal.ZERO) != 0 ) {
				//throw DpModuleError.DpstComm.E9999("定期账户余额不为0不允许休眠！ ");
	        	bizlog.error("定期账户余额不为0不允许休眠！", sTrandt);
	        	return;
			}

			// 检查是否有理财未赎回
//			Options<IoFnFnaAcct> fnlist = SysUtil.getInstance(IoFnSevQryTableInfo.class).fna_acct_selectAll_odb2(sCustac);
//			for(IoFnFnaAcct fnacct : fnlist){
//				if(fnacct.getDpacst() == FnEnumType.E_DPACST.NORMAL){
//					bizlog.error("有未赎回的理财产品！", sTrandt);
//					return;
//				}
//			}
			
			//若活期智能存款账户余额不为0不允许休眠
			List<KnaAcct> tblknaAcct = KnaAcctDao.selectAll_odb9(E_ACSETP.HQ, sCustac, false);
			if(CommUtil.isNotNull(tblknaAcct)){
				//如果活期智能存款账户不为0，不允许休眠
				for(KnaAcct accts : tblknaAcct){
					BigDecimal onlnbl = DpAcctProc.getAcctBalance(accts);
					if(CommUtil.compare(onlnbl, BigDecimal.ZERO) > 0){
						
						bizlog.debug("账户存在生效的活期智能存款[" + sCustac + "]");
						return;
					}
				}
			}
			
			/*
			 * 检查贷款金额是否为0  修复： 电子账号有贷款产品，贷款金额不为0，六个月后仍转休眠
			 * 2017.12.11
			 * */
			
	        BigDecimal intest = BigDecimal.ZERO;//利息
	        BigDecimal principal = BigDecimal.ZERO;//本金
	        List<KnaAccs> listKnaAccs = KnaAccsDao.selectAll_odb5(sCustac, false); 
	        for (KnaAccs tblknaAccs : listKnaAccs) {
	            if(tblknaAccs.getAcctst()==E_DPACST.NORMAL&&E_PRODTP.LOAN == tblknaAccs.getProdtp()){
	                //贷款状态为正常
	                DpInterestAndPrincipal dpInterestAndPrincipal = DpAcctQryDao.selPrincipalAndInterestByAcctno(tblknaAccs.getAcctno(),cn.sunline.ltts.busi.sys.type.LnEnumType.E_ACCTST.NORMAL, false);
	                if(CommUtil.isNotNull(dpInterestAndPrincipal)){ 
	                intest = intest.add(dpInterestAndPrincipal.getIntest());// 利息赋值值
	                principal = principal.add(dpInterestAndPrincipal.getPrincipal());// 本金赋值
	                }
	                if(CommUtil.compare(intest.add(principal), BigDecimal.ZERO) > 0){
	                    bizlog.debug("账户存在生效的余额不为0的贷款[" + sCustac + "]");
	                    return;
	                }
	            }
	        }
	        
			// 检查电子账户存款资金是否满足休眠条件
			IoDpClsChkIN chkin = SysUtil.getInstance(IoDpClsChkIN.class);
			chkin.setCustac(sCustac);
			chkin.setIssett(E_YES___.NO);
			IoDpClsChkOT cplDpClsChkOT = SysUtil.getInstance(DpAcctSvcType.class)
					.TestInterest(chkin);

			// 检查个人结算户、钱包账户和其他活期存款类子账户合计余额是否在50元（含）以下
			BigDecimal bigTotlam = cplDpClsChkOT.getTotlam().subtract(cplDpClsChkOT.getIntrvl());
			if (accttp == E_ACCATP.GLOBAL || accttp == E_ACCATP.FINANCE) {
				if (CommUtil.compare(bigTotlam, ConvertUtil.toBigDecimal(tblKnpParameter.getParm_value3())) > 0) {
					//throw DpModuleError.DpstComm.E9999("账户资金金额不满足休眠条件！ ");
		        	bizlog.error("账户资金金额不满足休眠条件！", sTrandt);
		        	return;
				}
			}
			// 电子钱包账户账户余额为1元以下
			else if (accttp == E_ACCATP.WALLET) {
				if (CommUtil.compare(bigTotlam, ConvertUtil.toBigDecimal(tblKnpParameter.getParm_value4())) > 0) {
					//throw DpModuleError.DpstComm.E9999("电子钱包账户余额不满足休眠条件！ ");
		        	bizlog.error("电子钱包账户余额不满足休眠条件！ ", sTrandt);
		        	return;
				}
			}
			String str = "0";//补位
			String idx = tblKnpParameter.getParm_value2();
			String ntdate = DateTools2.calDateByTerm(sTrandt,tblknp.getParm_value1());//当前日期加一月
			
			bizlog.debug("当前交易日期加一月===========" + ntdate);
			
			// 查询电子账户最后一次存取业务的交易日期
			//计算休眠日期
			String sSlepdt = "";//休眠日期
			bizlog.debug("上次余额更新日期============" + sUpbldt + "************************");
			if (CommUtil.isNotNull(sUpbldt)) {
				String sTrandt1 = DpDayEndDao.selLastTrandt(sCustac, false);// 查询账户余额发生明细当前表
				if (CommUtil.isNotNull(sTrandt1)){
					sSlepdt = DateTools2.calDateByTerm(sTrandt1,
							tblKnpParameter.getParm_value1());
				}else{
					String sTrandt2 = DpDayEndDao.selHlastTrandt(sCustac, false);// 查询账户余额发生明细历史表
					if(CommUtil.isNotNull(sTrandt2)){
						sSlepdt = DateTools2.calDateByTerm(sTrandt1,
								tblKnpParameter.getParm_value1());
					}else{
						sSlepdt = DateTools2.calDateByTerm(
								cplCaKnaCust.getOpendt(), tblKnpParameter.getParm_value1());
					}
				}
				
				
				}else{
					sSlepdt = DateTools2.calDateByTerm(
							cplCaKnaCust.getOpendt(), tblKnpParameter.getParm_value1());
				}
			
			if(CommUtil.isNotNull(sSlepdt)){
				
				//日期加一天
				sSlepdt =  DateTools2.calDateByTerm(sSlepdt, "1D");
				
				//检查休眠日期是否为当月5号若小于5号则改为5号，若大于5号则改为下月5号
				Integer iTrandt =  DateTools2.getDay(DateTools2.covStringToDate(sSlepdt));
				
				
				
				bizlog.debug("预计休眠日期===========" + sSlepdt);
				
				if(CommUtil.compare(iTrandt, ConvertUtil.toInteger(tblKnpParameter.getParm_value2())) < 0){
					sSlepdt = sSlepdt.substring(0, 6) + CommUtil.lpad(idx, 2, str);
				}else if(CommUtil.compare(iTrandt, ConvertUtil.toInteger(tblKnpParameter.getParm_value2())) > 0){
						sSlepdt = DateTools2.calDateByTerm(sSlepdt,tblknp.getParm_value1());
						sSlepdt = sSlepdt.substring(0, 6) + CommUtil.lpad(idx, 2, str);
				}
				
				bizlog.debug("预计休眠日期===========" + sSlepdt);
				
				if(CommUtil.equals(ntdate, sSlepdt)){
					
					//IoCaKnaAcdc  cplKna_acdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb1(sCustac, E_DPACST.NORMAL, true);
					
					 MsSystemSeq.getTrxnSeq(); //每一笔需要发送短信的重置流水
					//String sTransq = CommTools.getBaseRunEnvs().getTrxn_seq();// 交易流水
					
					/*MessageRegistration.InputSetter entity = SysUtil.getInstance(MessageRegistration.InputSetter.class);
					entity.setPrcscd(sPrcscd);
					//entity.setAppsid(tblcifaccs.getAppsid());
					entity.setCardno(cplKna_acdc.getCardno());
					entity.setTrandt(sTrandt);
					entity.setTrantm(sTrantm);
					entity.setTransq(sTransq);
					SysUtil.getInstance(IoPbSmsSvcType.class).messageRegistration(entity, null);*/
					
					IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
					IoCaKnaCust cust = caqry.getKnaCustByCustacOdb1(sCustac, true);
//					IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(IoSrvCfPerson.class).getCifCustAccsByCustno(cust.getCustno(), E_STATUS.NORMAL, true);
					//消息推送至APP客户端
//					MessageRealInfo mri2 = SysUtil.getInstance(MessageRealInfo.class);
//					mri2.setMtopic("Q0101005");
//					//mri.setTdcnno("R00");  //测试指定DCN
//					ToAppSendMsg toAppSendMsg = CommTools
//							.getInstance(ToAppSendMsg.class);
//					// 消息内容
//					toAppSendMsg.setUserId(cplCifCustAccs.getCustid()); //用户ID
//					toAppSendMsg.setOutNoticeId("Q0101005"); //外部消息ID
//					toAppSendMsg.setNoticeTitle("您的电子账户即将休眠"); //公告标题
//					toAppSendMsg.setContent("您的ThreeBank电子账户长期未使用即将休眠。如有疑问可咨询我行客服，电话0471-96616。"); //公告内容
//					toAppSendMsg.setSendtime(CommTools.getBaseRunEnvs().getTrxn_date()+BusiTools.getBusiRunEnvs().getTrantm()); //消息生成时间
//					toAppSendMsg.setTransType(E_APPTTP.CUACCH); //交易类型
//					toAppSendMsg.setTirggerSys(SysUtil.getSystemId()); //触发系统
//					toAppSendMsg.setClickType(E_CLIKTP.YES);   //点击动作类型
//					toAppSendMsg.setClickValue("LOGINURL||/page/electronicAcct/mainElect.html"); //点击动作值
//					
//					mri2.setMsgtyp("ApSmsType.ToAppSendMsg");
//					mri2.setMsgobj(toAppSendMsg); 
//					AsyncMessageUtil.add(mri2); //将待发送消息放入当前交易暂存区，commit后发送
//					SysUtil.getInstance(IoApAsyncMessage.class).afterBatchSendSMS();
				}
			}	
			// 将原法人设置回环境变量
//			CommTools.getBaseRunEnvs().setBusi_org_id(sCorpno);	
			
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<String> getBatchDataWalker(
				cn.sunline.ltts.busi.dptran.batchtran.intf.Bespnt.Input input, 
				cn.sunline.ltts.busi.dptran.batchtran.intf.Bespnt.Property property) {
			
			//获取电子账户状态为1正常和3未激活状态的电子账户	
			Params params = new Params();
			
			return new CursorBatchDataWalker<String>(
					DpDayEndDao.namedsql_selAcSleepInfos, params);
		}

}


