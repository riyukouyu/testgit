package cn.sunline.ltts.busi.dptran.batchtran;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessorWithJobDataItem;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctBach;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctBachDao;
import cn.sunline.ltts.busi.dptran.batchtran.intf.Batran.Input;
import cn.sunline.ltts.busi.dptran.batchtran.intf.Batran.Property;
import cn.sunline.ltts.busi.dptran.batchtran.redpck.rpbackDataProcessor;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcctDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.IoApAccount;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbSmsSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpAcdcOut;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubSqrd;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_ACLMFG;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_AUTHTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_CUSTLV;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_PYTLTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_REBKTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RISKLV;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_ACCTTP;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ACUTTP;


/**
 * 电子账户批量转账
 * 
 */

public class batranDataProcessor
		extends
		AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.dptran.batchtran.intf.Batran.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Batran.Property, String, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctBach> {
	private static BizLog log = BizLogUtil.getBizLog(rpbackDataProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);

	/**
	 * 交易前处理
	 * 
	 * @param taskid
	 *            批次作业ID
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void beforeTranProcess(String taskId,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batran.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batran.Property property) {
		log.info("<<===================批量提现交易前更新处理状态======================>>");
		filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		
		filetab.setBtfest(E_BTFEST.DING);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
		property.setSourdt(filetab.getAcctdt());
		log.info("<<===================filetab======================>>"+filetab.toString());
		super.beforeTranProcess(taskId, input, property);
	}

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param job
	 *            批次作业ID
	 * @param index
	 *            批次作业第几笔数据(从1开始)
	 * @param dataItem
	 *            批次数据项
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(
			String jobId,
			int index,
			cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctBach dataItem,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batran.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batran.Property property) {

		log.info("<<===================电子账户批量转账交易处理开始======================>>");
//		CommTools.genNewSerail(dataItem.getTrandt());
		MsSystemSeq.getTrxnSeq();
		
		dataItem.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		dataItem.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		
		
		
		//获取当前交易法人
		//String cur_coprno = CommTools.getBaseRunEnvs().getBusi_org_id();
		String acctno = dataItem.getAcctno();//转入方账户
		String pyacct = dataItem.getPyacct();//转出方账户
		// 转出方电子账号
		KnaAcct acctOut = SysUtil.getInstance(KnaAcct.class);
		// 转入方电子账号
		KnaAcct acctIn = SysUtil.getInstance(KnaAcct.class);
		IoDpAcdcOut acdcOut = SysUtil.getInstance(IoDpAcdcOut.class);
		IoDpAcdcOut acdcIn = SysUtil.getInstance(IoDpAcdcOut.class);
		KnbAcctBach knbAcctBach = SysUtil.getInstance(KnbAcctBach.class);
		BigDecimal bal = BigDecimal.ZERO;//可用余额
		BigDecimal swpamt = BigDecimal.ZERO;//归集金额
		if (CommUtil.isNotNull(acctno) && CommUtil.isNotNull(pyacct)){
			//查询账户账号类型
			E_ACCTTP accttp = getAcctp(acctno);//归集账户类型
			E_ACCTTP pyactp = getAcctp(pyacct);//被归集账户类型
			
			if(accttp == E_ACCTTP.DP && pyactp == E_ACCTTP.DP){
				//1.转入转出二者均不为空，且账户均为电子账户，执行电子账户批量转账
				log.info("<<===================转出方电子账户======================>>"+acctOut);
				// 根据电子账号查询电子账户ID
				acdcOut = DpAcctDao.selKnaAcdcInfoByCardno(dataItem.getPyacct(), true);
				// 判断当前转出账户状态字是否异常
				CapitalTransCheck.ChkAcctstWord(acdcOut.getCustac());
				// 判断当前转出账户状态是否异常
				CapitalTransCheck.ChkAcctstOT(acdcOut.getCustac());
				E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(acdcOut.getCustac());
				log.info("<<===================电子钱包转账交======================>>"+eAccatp);
				if (eAccatp == E_ACCATP.WALLET) {
					log.info("<<===================电子钱包转账交易处理开始======================>>");
					acctOut = CapitalTransDeal.getSettKnaAcctSubAcLock(acdcOut.getCustac(), E_ACSETP.MA);
					bal = acctOut.getOnlnbl();
					amountDeductedWal(dataItem, bal, acdcOut, eAccatp);//三类转出账户额度扣减
				} else {
					log.info("<<==================其他转账交易处理开始======================>>");
					acctOut = CapitalTransDeal.getSettKnaAcctSubAcLock(acdcOut.getCustac(), E_ACSETP.SA);
					// 可用余额 addby xiongzhao 20161223 
					bal = SysUtil.getInstance(DpAcctSvcType.class)
							.getAcctaAvaBal(acdcOut.getCustac(), acctOut.getAcctno(),
									acctOut.getCrcycd(), E_YES___.YES, E_YES___.NO);
					if(eAccatp == E_ACCATP.FINANCE){
						log.info("<<===================理财方电子账号======================>>");
						amountDeductedFin(dataItem, bal, acdcOut, eAccatp);//额度扣减
					}
				}
				
				log.info("<<===================转入方电子账号======================>>"+acctIn);
				// 根据电子账号查询电子账户ID
				acdcIn = DpAcctDao.selKnaAcdcByAcctno(dataItem.getAcctno(), true);
				// 检查是否涉案
				IoCaSevQryAccout.IoCaQryInacInfo.Output out = SysUtil
						.getInstance(IoCaSevQryAccout.IoCaQryInacInfo.Output.class);
				SysUtil.getInstance(IoCaSevQryAccout.class).qryInac(
						dataItem.getAcctno(), dataItem.getAcctna(), null, null, out);
				if (E_INSPFG.INVO == out.getInspfg()) {
					throw DpModuleError.DpstComm.E9999("归集账户为涉案账户");
				}
				// 判断当前转入账户状态字是否异常
				CapitalTransCheck.ChkAcctFrozIN(acdcIn.getCustac());
				// 判断当前转入账户状态是否异常
				CapitalTransCheck.ChkAcctstRe(acdcIn.getCustac());
				E_ACCATP IAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
						.qryAccatpByCustac(acdcIn.getCustac());
	
				if (IAccatp == E_ACCATP.WALLET) {
					throw DpModuleError.DpstComm.E9999("转入账户类型不能为电子钱包账户");
				}
	
				acctIn = CapitalTransDeal.getSettKnaAcctAcLock(acdcIn.getCustac());
				BusiTools.getBusiRunEnvs().setRemark("资金归集");
				// 固定金额
				if (CommUtil.equals(dataItem.getSweptp(), "1")) {
					if (CommUtil.compare(dataItem.getSwpamt(), bal) > 0) {
						throw DpModuleError.DpstComm.E9999("被归集账户余额不足");
					} else {
						swpamt = dataItem.getSwpamt();
						// 调用支取记账服务
						drawAcctDp(dataItem, acctOut, acdcOut,swpamt);
						// 调用存入记账服务
						postAcctDp(dataItem, acctIn, acdcIn,swpamt);
					}
					// 保留金额
				} else if (CommUtil.equals(dataItem.getSweptp(), "2")) {
					if (CommUtil.compare(dataItem.getSwpamt(), bal) >= 0) {
						throw DpModuleError.DpstComm.E9999("被归集账户余额不足");
					} else {
						// 调用支取记账服务
						swpamt = bal.subtract(dataItem.getSwpamt());
						drawAcctDp(dataItem, acctOut, acdcOut,swpamt);
						// 调用存入记账服务
						postAcctDp(dataItem, acctIn, acdcIn,swpamt);
					}
				} else {
					throw DpModuleError.DpstComm.E9999("归集方式输入错误");
				}
			}
			
			if(accttp == E_ACCTTP.IN && pyactp == E_ACCTTP.DP){
				//2.(归集账户)转入方为内部户，(被归集账户)转出方存款户
				log.info("<<===================转出方电子账户======================>>"+acctOut);
				// 根据电子账号查询电子账户ID
				acdcOut = DpAcctDao.selKnaAcdcInfoByCardno(dataItem.getPyacct(), true);
				// 判断当前转出账户状态字是否异常
				CapitalTransCheck.ChkAcctstWord(acdcOut.getCustac());
				// 判断当前转出账户状态是否异常
				CapitalTransCheck.ChkAcctstOT(acdcOut.getCustac());
				E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(acdcOut.getCustac());
				log.info("<<===================电子钱包转账交======================>>"+eAccatp);
				if (eAccatp == E_ACCATP.WALLET) {
					log.info("<<===================电子钱包转账交易处理开始======================>>");
					acctOut = CapitalTransDeal.getSettKnaAcctSubAcLock(acdcOut.getCustac(), E_ACSETP.MA);
					bal = acctOut.getOnlnbl();
					amountDeductedWal(dataItem, bal, acdcOut, eAccatp);//三类转出账户额度扣减

				} else {
					log.info("<<==================其他转账交易处理开始======================>>");
					acctOut = CapitalTransDeal.getSettKnaAcctSubAcLock(acdcOut.getCustac(), E_ACSETP.SA);
					// 可用余额 addby xiongzhao 20161223 
					bal = SysUtil.getInstance(DpAcctSvcType.class)
							.getAcctaAvaBal(acdcOut.getCustac(), acctOut.getAcctno(),
									acctOut.getCrcycd(), E_YES___.YES, E_YES___.NO);
					if(eAccatp == E_ACCATP.FINANCE){
						log.info("<<===================理财方电子账号======================>>");
						amountDeductedFin(dataItem, bal, acdcOut, eAccatp);//额度扣减
					}
				}
				
				BusiTools.getBusiRunEnvs().setRemark("资金归集");
				
				// 固定金额
				if (CommUtil.equals(dataItem.getSweptp(), "1")) {
					if (CommUtil.compare(dataItem.getSwpamt(), bal) > 0) {
						throw DpModuleError.DpstComm.E9999("被归集账户余额不足");
					} else {
						// 调用支取记账服务
						swpamt = dataItem.getSwpamt();
						drawAcctDp(dataItem, acctOut, acdcOut,swpamt);
					}
					// 保留金额
				} else if (CommUtil.equals(dataItem.getSweptp(), "2")) {
					if (CommUtil.compare(dataItem.getSwpamt(), bal) >= 0) {
						throw DpModuleError.DpstComm.E9999("被归集账户余额不足");
					} else {
						// 调用支取记账服务
						swpamt = bal.subtract(dataItem.getSwpamt());
						drawAcctDp(dataItem, acctOut, acdcOut,swpamt);
					}
				} else {
					throw DpModuleError.DpstComm.E9999("归集方式输入错误");
				}
				//内部户贷方记账服务
				InacTransComplex.IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
//				KnpParameter para = SysUtil.getInstance(KnpParameter.class);
//				para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", E_CLACTP._19.getValue(), "%", true);
				GlKnaAcct glKnaAcct = GlKnaAcctDao.selectOne_odb1(dataItem.getAcctno(), true);
				acdrIn.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
				acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
				acdrIn.setAcuttp(E_ACUTTP._1);
				acdrIn.setCrcycd(acctOut.getCrcycd());
				acdrIn.setSmrycd(BusinessConstants.SUMMARY_ZC);
				acdrIn.setToacct(dataItem.getAcctno());
				acdrIn.setToacna(dataItem.getAcctna());
				acdrIn.setTranam(swpamt);
				acdrIn.setBusino(glKnaAcct.getBusino()); //业务编码
				acdrIn.setSubsac(glKnaAcct.getSubsac());//子户号
				acdrIn.setAcctno(dataItem.getAcctno());
				acdrIn.setAcctna(dataItem.getAcctna());
				IoInAccount ioInAccount = SysUtil.getInstance(IoInAccount.class);
				ioInAccount.ioInAccr(acdrIn);
			}
			
			if(accttp == E_ACCTTP.DP && pyactp == E_ACCTTP.IN){
				//3.(归集账户)转入方为存款户，(被归集账户)转出方内部户
				log.info("<<===================转入方电子账号======================>>"+acctIn);
				// 根据电子账号查询电子账户ID
				acdcIn = DpAcctDao.selKnaAcdcByAcctno(
			              dataItem.getAcctno(), true);
				acctIn = CapitalTransDeal.getSettKnaAcctAcLock(acdcIn.getCustac());
				// 检查是否涉案
				IoCaSevQryAccout.IoCaQryInacInfo.Output out = SysUtil.getInstance(IoCaSevQryAccout.IoCaQryInacInfo.Output.class);
				SysUtil.getInstance(IoCaSevQryAccout.class).qryInac(
						dataItem.getAcctno(), dataItem.getAcctna(), null, null, out);
				if (E_INSPFG.INVO == out.getInspfg()) {
					throw DpModuleError.DpstComm.E9999("归集账户为涉案账户");
				}
				// 判断当前转入账户状态字是否异常
				CapitalTransCheck.ChkAcctFrozIN(acdcIn.getCustac());
				// 判断当前转入账户状态是否异常
				CapitalTransCheck.ChkAcctstRe(acdcIn.getCustac());
				E_ACCATP IAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
						.qryAccatpByCustac(acdcIn.getCustac());

				if (IAccatp == E_ACCATP.WALLET) {
					throw DpModuleError.DpstComm.E9999("转入账户类型不能为电子钱包账户");
				}

				//内部户借方记账服务
				InacTransComplex.IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
//				KnpParameter para = SysUtil.getInstance(KnpParameter.class);
//				para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", E_CLACTP._18.getValue(), "%", true);
				GlKnaAcct glKnaAcct = GlKnaAcctDao.selectOne_odb1(dataItem.getPyacct(), true);
				if (CommUtil.equals(dataItem.getSweptp(), "1")) {
					// 调用存入记账服务
					swpamt = dataItem.getSwpamt();
				}else if(CommUtil.equals(dataItem.getSweptp(), "2")) {
					swpamt = bal.subtract(dataItem.getSwpamt());
				}
				acdrIn.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
				acdrIn.setAcuttp(E_ACUTTP._1);
				acdrIn.setTrantp(E_TRANTP.TR); //交易类型 InParm.cupsconfrim
				acdrIn.setCrcycd(acctIn.getCrcycd());
				acdrIn.setSmrycd(BusinessConstants.SUMMARY_ZR);
				acdrIn.setToacct(dataItem.getPyacct());
				acdrIn.setToacna(dataItem.getPyacna());
				acdrIn.setTranam(swpamt);
				acdrIn.setBusino(glKnaAcct.getBusino()); //业务编码
				acdrIn.setSubsac(glKnaAcct.getSubsac());//子户号
				acdrIn.setAcctno(dataItem.getPyacct());
				acdrIn.setAcctna(dataItem.getPyacna());
				IoInAccount ioInAccount = SysUtil.getInstance(IoInAccount.class);
				ioInAccount.ioInAcdr(acdrIn);
				BusiTools.getBusiRunEnvs().setRemark("资金归集");
				postAcctDp(dataItem, acctIn, acdcIn,swpamt);
			}
			
		}else if(CommUtil.isNull(acctno) && CommUtil.isNotNull(pyacct)){			
			//2.(归集账户)转入方为空，(被归集账户)转出方不为空
			log.info("<<===================转出方电子账户======================>>"+acctOut);
			// 根据电子账号查询电子账户ID
			acdcOut = DpAcctDao.selKnaAcdcInfoByCardno(dataItem.getPyacct(), true);
			// 判断当前转出账户状态字是否异常
			CapitalTransCheck.ChkAcctstWord(acdcOut.getCustac());
			// 判断当前转出账户状态是否异常
			CapitalTransCheck.ChkAcctstOT(acdcOut.getCustac());
			E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(acdcOut.getCustac());
			log.info("<<===================电子钱包转账交======================>>"+eAccatp);
			if (eAccatp == E_ACCATP.WALLET) {
				log.info("<<===================电子钱包转账交易处理开始======================>>");
				acctOut = CapitalTransDeal.getSettKnaAcctSubAcLock(acdcOut.getCustac(), E_ACSETP.MA);
				bal = acctOut.getOnlnbl();
				amountDeductedWal(dataItem, bal, acdcOut, eAccatp);//三类转出账户额度扣减

			} else {
				log.info("<<==================其他转账交易处理开始======================>>");
				acctOut = CapitalTransDeal.getSettKnaAcctSubAcLock(acdcOut.getCustac(), E_ACSETP.SA);
				// 可用余额 addby xiongzhao 20161223 
				bal = SysUtil.getInstance(DpAcctSvcType.class)
						.getAcctaAvaBal(acdcOut.getCustac(), acctOut.getAcctno(),
								acctOut.getCrcycd(), E_YES___.YES, E_YES___.NO);
				if(eAccatp == E_ACCATP.FINANCE){
					log.info("<<===================理财方电子账号======================>>");
					amountDeductedFin(dataItem, bal, acdcOut, eAccatp);//额度扣减
				}
			}
			
			BusiTools.getBusiRunEnvs().setRemark("资金归集");
			
			// 固定金额
			if (CommUtil.equals(dataItem.getSweptp(), "1")) {
				if (CommUtil.compare(dataItem.getSwpamt(), bal) > 0) {
					throw DpModuleError.DpstComm.E9999("被归集账户余额不足");
				} else {
					// 调用支取记账服务
					swpamt = dataItem.getSwpamt();
					drawAcctDp(dataItem, acctOut, acdcOut,swpamt);
				}
				// 保留金额
			} else if (CommUtil.equals(dataItem.getSweptp(), "2")) {
				if (CommUtil.compare(dataItem.getSwpamt(), bal) >= 0) {
					throw DpModuleError.DpstComm.E9999("被归集账户余额不足");
				} else {
					// 调用支取记账服务
					swpamt = bal.subtract(dataItem.getSwpamt());
					drawAcctDp(dataItem, acctOut, acdcOut,swpamt);
				}
			} else {
				throw DpModuleError.DpstComm.E9999("归集方式输入错误");
			}
			//内部户贷方记账服务
			InacTransComplex.IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
			KnpParameter para = SysUtil.getInstance(KnpParameter.class);
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", E_CLACTP._19.getValue(), "%", true);
			acdrIn.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
			acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
			acdrIn.setAcuttp(E_ACUTTP._1);
			acdrIn.setCrcycd(acctOut.getCrcycd());
			acdrIn.setSmrycd(BusinessConstants.SUMMARY_ZC);
			acdrIn.setToacct(dataItem.getAcctno());
			acdrIn.setToacna(dataItem.getAcctna());
			acdrIn.setTranam(swpamt);
			acdrIn.setBusino(para.getParm_value1()); //业务编码
			acdrIn.setSubsac(para.getParm_value2());//子户号
			IoInAccount ioInAccount = SysUtil.getInstance(IoInAccount.class);
			IaTransOutPro iaTransOutPro = ioInAccount.ioInAccr(acdrIn);
			// 1、内部户查询
	        knbAcctBach.setAcctno(iaTransOutPro.getAcctno());
	        knbAcctBach.setAcctna(iaTransOutPro.getAcctna());
	        knbAcctBach.setAccttp("9");//1为存款账户，9为内部户
	        KnbAcctBachDao.updateOne_odb2(knbAcctBach);//更新归集账户为内部户
		}else if(CommUtil.isNotNull(acctno) && CommUtil.isNull(pyacct)){
			//3.(归集账户)转入方不为空，(被归集账户)转出方为空
			log.info("<<===================转入方电子账号======================>>"+acctIn);
			// 根据电子账号查询电子账户ID
			acdcIn = DpAcctDao.selKnaAcdcByAcctno(
		              dataItem.getAcctno(), true);
			acctIn = CapitalTransDeal.getSettKnaAcctAcLock(acdcIn.getCustac());
			// 检查是否涉案
			IoCaSevQryAccout.IoCaQryInacInfo.Output out = SysUtil.getInstance(IoCaSevQryAccout.IoCaQryInacInfo.Output.class);
			SysUtil.getInstance(IoCaSevQryAccout.class).qryInac(
					dataItem.getAcctno(), dataItem.getAcctna(), null, null, out);
			if (E_INSPFG.INVO == out.getInspfg()) {
				throw DpModuleError.DpstComm.E9999("归集账户为涉案账户");
			}
			// 判断当前转入账户状态字是否异常
			CapitalTransCheck.ChkAcctFrozIN(acdcIn.getCustac());
			// 判断当前转入账户状态是否异常
			CapitalTransCheck.ChkAcctstRe(acdcIn.getCustac());
			E_ACCATP IAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
					.qryAccatpByCustac(acdcIn.getCustac());

			if (IAccatp == E_ACCATP.WALLET) {
				throw DpModuleError.DpstComm.E9999("转入账户类型不能为电子钱包账户");
			}

			//内部户借方记账服务
			InacTransComplex.IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
			KnpParameter para = SysUtil.getInstance(KnpParameter.class);
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", E_CLACTP._18.getValue(), "%", true);
			if (CommUtil.equals(dataItem.getSweptp(), "1")) {
				// 调用存入记账服务
				swpamt = dataItem.getSwpamt();
			}else if(CommUtil.equals(dataItem.getSweptp(), "2")) {
				swpamt = bal.subtract(dataItem.getSwpamt());
			}
			acdrIn.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
			acdrIn.setAcuttp(E_ACUTTP._1);
			acdrIn.setTrantp(E_TRANTP.TR); //交易类型 InParm.cupsconfrim
			acdrIn.setCrcycd(acctIn.getCrcycd());
			acdrIn.setSmrycd(BusinessConstants.SUMMARY_ZR);
			acdrIn.setToacct(dataItem.getPyacct());
			acdrIn.setToacna(dataItem.getPyacna());
			acdrIn.setTranam(swpamt);
			acdrIn.setBusino(para.getParm_value1()); //业务编码
			acdrIn.setSubsac(para.getParm_value2());//子户号
			IoInAccount ioInAccount = SysUtil.getInstance(IoInAccount.class);
			IaTransOutPro iaTransOutPro= ioInAccount.ioInAcdr(acdrIn);
			BusiTools.getBusiRunEnvs().setRemark("资金归集");
			postAcctDp(dataItem, acctIn, acdcIn,swpamt);
			// 1、内部户查询
	        knbAcctBach.setPyacct(iaTransOutPro.getAcctno());
	        knbAcctBach.setPyacna(iaTransOutPro.getAcctna());
	        KnbAcctBachDao.updateOne_odb2(knbAcctBach);//更新被归集为内部户
		}
		
		
		if(CommUtil.equals(acdcIn.getCorpno(), acdcOut.getCorpno())){
			//交易平衡性检查
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), 
					CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
		}else{
			//交易平衡性检查
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), 
					CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._10);
		}
		
		dataItem.setTranst(E_TRANST.SUCCESS);
		dataItem.setDescrb("成功完成");

		// 判断当前是否为保留余额
		if (CommUtil.equals(dataItem.getSweptp(), "2")) {
			dataItem.setSwpamt(acctOut.getOnlnbl().subtract(
					dataItem.getSwpamt()));
		}

		KnbAcctBachDao.updateOne_odb2(dataItem);

		
		//设置法人为当前转出账户法人
//		CommTools.getBaseRunEnvs().setBusi_org_id(acdcOut.getCorpno());
		
		// 短信流水登记
		//登记贷方短信
		if(CommUtil.isNotNull(pyacct))
		{
			registerMsgCredit(acctOut);
		}
		//登记借方短信
		if(CommUtil.isNotNull(acctno))
		{
			registerMsgDebit(acctIn);
		}
		log.info("<<===================电子账户批量转账交易处理结束======================>>");
	}
	
	/**
	 * 获取电子账户
	 * @param acctno
	 * @return
	 */
	private E_ACCTTP getAcctp(
			String acctno) {
		IoApAccount.queryAccountType.Output output = SysUtil.getInstance(IoApAccount.queryAccountType.Output.class);
		IoApAccount ioApAccount = SysUtil.getInstance(IoApAccount.class);
		ioApAccount.queryAccountType(acctno, output);
		return output.getAccttp();
	}

	private void postAcctDp(
			cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctBach dataItem,
			KnaAcct acctIn, IoDpAcdcOut acdcIn,BigDecimal swpamt) {
		SaveDpAcctIn save = SysUtil.getInstance(SaveDpAcctIn.class);
		DpAcctSvcType dpsvc = SysUtil.getInstance(DpAcctSvcType.class);

		save.setAcctno(acctIn.getAcctno());
		save.setAcseno("");
		save.setCardno(acdcIn.getCardno());
		save.setCrcycd(BusiTools.getDefineCurrency());
		save.setCustac(acdcIn.getCustac());
		save.setOpacna(dataItem.getPyacna());
		save.setToacct(dataItem.getPyacct());
		save.setTranam(swpamt);
		save.setSmrycd(BusinessConstants.SUMMARY_ZR);
		save.setRemark(BusiTools.getBusiRunEnvs().getRemark());
		dpsvc.addPostAcctDp(save);
	}

	private void drawAcctDp(
			cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctBach dataItem,
			KnaAcct acctOut, IoDpAcdcOut acdcOut,BigDecimal swpamt) {
		DpAcctSvcType dpAcct = SysUtil.getInstance(DpAcctSvcType.class);
		DrawDpAcctIn dpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
		dpAcctIn.setAcctno(acctOut.getAcctno());
		dpAcctIn.setAcseno("");
		dpAcctIn.setCardno(dataItem.getPyacct());
		dpAcctIn.setCustac(acdcOut.getCustac());
		dpAcctIn.setCrcycd(BusiTools.getDefineCurrency());
		dpAcctIn.setOpacna(dataItem.getAcctna());
		dpAcctIn.setToacct(dataItem.getAcctno());
		dpAcctIn.setTranam(swpamt);
		dpAcctIn.setSmrycd(BusinessConstants.SUMMARY_ZC);
		dpAcctIn.setRemark(BusiTools.getBusiRunEnvs().getRemark());
		dpAcct.addDrawAcctDp(dpAcctIn);
		
	}

	private void amountDeductedWal(
			cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctBach dataItem,
			BigDecimal bal, IoDpAcdcOut acdcOut, E_ACCATP eAccatp) {
		//三类转出账户额度扣减
		IoCaSevAccountLimit qt = SysUtil.getInstance(IoCaSevAccountLimit.class); //获取电子账户额度服务
		//获取输入复合类型
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter qtIn = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter.class);
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output	qtOt = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output.class);
		qtIn.setBrchno(dataItem.getBrchno());
		qtIn.setAclmfg(E_ACLMFG._0);
		qtIn.setAccttp(eAccatp);
		qtIn.setAuthtp(E_AUTHTP._01);
		qtIn.setCustac(acdcOut.getCustac());
		//qtIn.setCustid(qtIN.getCustid());
		qtIn.setCustlv(E_CUSTLV._02);
		qtIn.setAcctrt(E_ACCTROUTTYPE.PERSON);
		qtIn.setLimttp(E_LIMTTP.TR);
		qtIn.setPytltp(E_PYTLTP._99);
		qtIn.setRebktp(E_REBKTP._99);
		qtIn.setRisklv(E_RISKLV._02);
		qtIn.setSbactp(E_SBACTP._12);
		qtIn.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());
		qtIn.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		qtIn.setServtp(dataItem.getServtp());
		if(CommUtil.equals(dataItem.getSweptp(), "1")){
			qtIn.setTranam(dataItem.getSwpamt());
		}else if(CommUtil.equals(dataItem.getSweptp(), "2")){
			qtIn.setTranam(bal.subtract(dataItem.getSwpamt()));
		}
		qt.SubAcctQuota(qtIn, qtOt); //额度检查扣减
	}

	private void amountDeductedFin(
			cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctBach dataItem,
			BigDecimal bal, IoDpAcdcOut acdcOut, E_ACCATP eAccatp) {
		IoCaSevAccountLimit qt = SysUtil.getInstance(IoCaSevAccountLimit.class); //获取电子账户额度服务
		
		//获取输入复合类型
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter qtIn = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter.class);
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output	qtOt = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output.class);
		qtIn.setBrchno(dataItem.getBrchno());
		qtIn.setAclmfg(E_ACLMFG._0);
		qtIn.setAccttp(eAccatp);
		qtIn.setAuthtp(E_AUTHTP._01);
		qtIn.setCustac(acdcOut.getCustac());
		//qtIn.setCustid(qtIN.getCustid());
		qtIn.setCustlv(E_CUSTLV._02);
		qtIn.setAcctrt(E_ACCTROUTTYPE.PERSON);
		qtIn.setLimttp(E_LIMTTP.TR);
		qtIn.setPytltp(E_PYTLTP._99);
		qtIn.setRebktp(E_REBKTP._99);
		qtIn.setRisklv(E_RISKLV._02);
		qtIn.setSbactp(E_SBACTP._11);
		qtIn.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());
		qtIn.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		qtIn.setServtp(dataItem.getServtp());
		if(CommUtil.equals(dataItem.getSweptp(), "1")){
			qtIn.setTranam(dataItem.getSwpamt());
		}else if(CommUtil.equals(dataItem.getSweptp(), "2")){
			qtIn.setTranam(bal.subtract(dataItem.getSwpamt()));
		}
		qt.SubAcctQuota(qtIn, qtOt); //额度检查扣减
	}

	private void registerMsgCredit(KnaAcct acctOut) {
		//IoCifCustAccs cifcust = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_accsByCustno(acct1.getCustno(), false, E_STATUS.NORMAL);
		IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
		//cplKubSqrd.setAppsid(cifcust.getAppsid());// app推送ID
		cplKubSqrd.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());// 内部交易码
		cplKubSqrd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
		cplKubSqrd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());// 交易时间
		cplKubSqrd.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
		cplKubSqrd.setTmstmp(DateTools2.getCurrentTimestamp());// 时间戳
		cplKubSqrd.setPmvl01("C");
		// 调用短信流水登记服务
		SysUtil.getInstance(IoPbSmsSvcType.class).pbTransqReg(cplKubSqrd);
	}

	private void registerMsgDebit(KnaAcct acctIn) {
		//设置当前法人为转入账户法人
//		CommTools.getBaseRunEnvs().setBusi_org_id(acdcIn.getCorpno());
		//IoCifCustAccs cifcust2 = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_accsByCustno(acct2.getCustno(), false, E_STATUS.NORMAL);
		IoPbKubSqrd cplKubSqrd2 = SysUtil.getInstance(IoPbKubSqrd.class);
		//cplKubSqrd2.setAppsid(cifcust2.getAppsid());// app推送ID
		cplKubSqrd2.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());// 内部交易码
		cplKubSqrd2.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
		cplKubSqrd2.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());// 交易时间
		cplKubSqrd2.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
		cplKubSqrd2.setTmstmp(DateTools2.getCurrentTimestamp());// 时间戳
		cplKubSqrd2.setPmvl01("D");
		// 调用短信流水登记服务
		SysUtil.getInstance(IoPbSmsSvcType.class).pbTransqReg(cplKubSqrd2);
		
		//设置当前法人为批量交易法人
//		CommTools.getBaseRunEnvs().setBusi_org_id(cur_coprno);
	}

	@Override
	public void jobExceptionProcess(String taskId, Input input,
			Property property, String jobId, KnbAcctBach dataItem, Throwable t) {

		// DaoUtil.rollbackTransaction(); //主事物回滚
		log.info("<<==================处理异常，更新批量失败开始======================>>");
		dataItem.setTranst(E_TRANST.FAIL);
		dataItem.setDescrb(t.getMessage());
		KnbAcctBachDao.updateOne_odb2(dataItem);

		log.info("<<==================处理异常，更新批量失败结束======================>>");
		// super.jobExceptionProcess(taskId, input, property, jobId, dataItem,
		// t);

	}

	/**
	 * 获取数据遍历器。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<String> getBatchDataWalker(
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batran.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batran.Property property) {
		log.info("<<===================获取作业编号开始======================>>");
		
		Params param = new Params();
		param.put("filesq", input.getFilesq());
		param.put("sourdt", property.getSourdt());
		
		return new CursorBatchDataWalker<String>(
				DpAcctDao.namedsql_selDataidByFilesq,param);
	}

	/**
	 * 获取作业数据遍历器
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @param dataItem
	 *            批次数据项
	 * @return
	 */
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctBach> getJobBatchDataWalker(
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batran.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batran.Property property,
			String dataItem) {

		log.info("<<===================获取作业数据集合开始======================>>");
		Params param = new Params();
		param.put("filesq", input.getFilesq());
		param.put("sourdt", property.getSourdt());
		param.put("dataid",dataItem);
		log.info("<<===================获取作业数据集合结束======================>>");
		return new CursorBatchDataWalker<KnbAcctBach>(
				DpAcctDao.namedsql_selAcctTranferInfo, param);
	}
}
