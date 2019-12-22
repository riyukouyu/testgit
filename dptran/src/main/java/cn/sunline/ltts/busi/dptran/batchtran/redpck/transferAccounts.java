package cn.sunline.ltts.busi.dptran.batchtran.redpck;
        

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.timer.LttsTimerProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.DpTransferAcct.KnbAcctSign;
import cn.sunline.ltts.busi.dp.tables.DpTransferAcct.KnbAcctSignDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.type.DpTransfer.DpKnbAcctSign;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpAcdcOut;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;


//定时调度转账业务
public class transferAccounts extends LttsTimerProcessor {
	private static BizLog log = BizLogUtil.getBizLog(transferAccounts.class);
//TODO  优化控制条数
	@Override
	public void process(String param, DataArea dataArea) {
		log.debug("<<===================电子账户定期转账交易处理开始======================>>");

		String trandt=CommTools.getBaseRunEnvs().getTrxn_date();
		String trdate=trandt.substring(6, 8);	
		List<DpKnbAcctSign> list=DpAcctQryDao.selTransferCount(trdate,trandt,false);
		if(list.size()==0){
			return;
		}
		for(DpKnbAcctSign knbAcctSign:list){
			//CommTools.genNewSerail(dataItem.getTrandt());
			//dataItem.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			//dataItem.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
			
			//获取当前交易法人
			//String cur_coprno = CommTools.getBaseRunEnvs().getBusi_org_id();
			// 转出方电子账号
			KnaAcct acctOut = SysUtil.getInstance(KnaAcct.class);
			// 根据电子账号查询电子账户ID
			IoDpAcdcOut acdcOut = DpAcctDao.selKnaAcdcInfoByCardno(
					knbAcctSign.getCardno(), true);
			
//			CifCust cifCust=CifCustDao.selectOne_odb1(knbAcctSign.getCustno(), false);
			// 判断当前转出账户状态字是否异常
			CapitalTransCheck.ChkAcctstWord(acdcOut.getCustac());
			// 判断当前转出账户状态是否异常
			CapitalTransCheck.ChkAcctstOT(acdcOut.getCustac());

			BigDecimal bal = BigDecimal.ZERO;
			E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
					.qryAccatpByCustac(acdcOut.getCustac());
			
			
			if (eAccatp == E_ACCATP.WALLET) {
				
				acctOut = CapitalTransDeal.getSettKnaAcctSubAcLock(
						acdcOut.getCustac(), E_ACSETP.MA);
				bal = acctOut.getOnlnbl();		
				//三类转出账户额度扣减
				IoCaSevAccountLimit qt = CommTools.getRemoteInstance(IoCaSevAccountLimit.class); //获取电子账户额度服务
				//获取输入复合类型
				cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter qtIn = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter.class);
				cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output	qtOt = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output.class);
//				qtIn.setBrchno(cifCust.getBrchno());
				qtIn.setAccttp(E_ACCATP.WALLET);
				qtIn.setCustac(acdcOut.getCustac());
				qtIn.setAcctrt(E_ACCTROUTTYPE.PERSON);
				qtIn.setSbactp(E_SBACTP._12);
				qtIn.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());
				qtIn.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
				qtIn.setServtp("IE");
				qtIn.setTranam(knbAcctSign.getTranam());	
				qt.SubAcctQuota(qtIn, qtOt); //额度检查扣减
			} else {
				acctOut = CapitalTransDeal.getSettKnaAcctSubAcLock(
						acdcOut.getCustac(), E_ACSETP.SA);		
				// 可用余额 addby xiongzhao 20161223 
				bal = SysUtil.getInstance(DpAcctSvcType.class)
						.getAcctaAvaBal(acdcOut.getCustac(), acctOut.getAcctno(),
								acctOut.getCrcycd(), E_YES___.YES, E_YES___.NO);

				if(eAccatp == E_ACCATP.FINANCE){
					
					IoCaSevAccountLimit qt = CommTools.getRemoteInstance(IoCaSevAccountLimit.class); //获取电子账户额度服务
					
					//获取输入复合类型
					cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter qtIn = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter.class);
					cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output	qtOt = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output.class);
					
//					qtIn.setBrchno(cifCust.getBrchno());			
					qtIn.setAccttp(E_ACCATP.FINANCE);			
					qtIn.setCustac(acdcOut.getCustac());			
					qtIn.setAcctrt(E_ACCTROUTTYPE.PERSON);		
					qtIn.setSbactp(E_SBACTP._11);
					qtIn.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());
					qtIn.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
					qtIn.setServtp("IE");
					qtIn.setTranam(knbAcctSign.getTranam());		
					qt.SubAcctQuota(qtIn, qtOt); //额度检查扣减
				}
			}

			// 转入方电子账号
			KnaAcct acctIn = SysUtil.getInstance(KnaAcct.class);
			// 根据电子账号查询电子账户ID	
			
			IoDpAcdcOut acdcIn=DpAcctDao.selKnaAcdcInfoByCardno(
					knbAcctSign.getTgacct(), true);
			
			//IoDpAcdcOut acdcIn = DpAcctDao.selKnaAcdcByAcctno(
			//		knbAcctSign.getTgacct(), true);
			// 判断当前转入账户状态字是否异常
			CapitalTransCheck.ChkAcctFrozIN(acdcIn.getCustac());
			// 判断当前转入账户状态是否异常
			CapitalTransCheck.ChkAcctstRe(acdcIn.getCustac());	
			E_ACCATP IAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
					.qryAccatpByCustac(acdcIn.getCustac());
			//if (IAccatp == E_ACCATP.WALLET) {
				//throw DpModuleError.DpstComm.E9999("转入账户类型不能为电子钱包账户");
				//continue;
			//}
			acctIn = CapitalTransDeal.getSettKnaAcctAcLock(acdcIn.getCustac());
		
			BusiTools.getBusiRunEnvs().setRemark("定额资金定期划转");
			
				if (CommUtil.compare(knbAcctSign.getTranam(), bal) > 0) {
					//throw DpModuleError.DpstComm.E9999("账户余额不足");
					KnbAcctSign knbASign=KnbAcctSignDao.selectOne_odb1(knbAcctSign.getAcctno(), true);			
					knbASign.setTrandt(trandt);			
					KnbAcctSignDao.updateOne_odb1(knbASign);
					continue;
				} else {				
					// 调用支取记账服务
					DpAcctSvcType dpAcct = SysUtil.getInstance(DpAcctSvcType.class);
					DrawDpAcctIn dpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);

					dpAcctIn.setAcctno(acctOut.getAcctno());
					dpAcctIn.setAcseno("");
					dpAcctIn.setCardno(knbAcctSign.getCardno());
					dpAcctIn.setCustac(acdcOut.getCustac());
					dpAcctIn.setCrcycd(BusiTools.getDefineCurrency());
					//dpAcctIn.setOpacna(dataItem.getAcctna());
					dpAcctIn.setToacct(knbAcctSign.getTgacct());
					dpAcctIn.setTranam(knbAcctSign.getTranam());
					dpAcctIn.setSmrycd(BusinessConstants.SUMMARY_ZC);
					dpAcctIn.setRemark(BusiTools.getBusiRunEnvs().getRemark());

					dpAcct.addDrawAcctDp(dpAcctIn);
					
					
					// 调用存入记账服务
					SaveDpAcctIn save = SysUtil.getInstance(SaveDpAcctIn.class);
					DpAcctSvcType dpsvc = SysUtil
							.getInstance(DpAcctSvcType.class);

					save.setAcctno(acctIn.getAcctno());
					save.setAcseno("");
					save.setCardno(acdcIn.getCardno());
					save.setCrcycd(BusiTools.getDefineCurrency());
					save.setCustac(acdcIn.getCustac());
					//save.setOpacna(dataItem.getPyacna());
					save.setToacct(knbAcctSign.getCardno());
					save.setTranam(knbAcctSign.getTranam());
					save.setSmrycd(BusinessConstants.SUMMARY_ZR);
					save.setRemark(BusiTools.getBusiRunEnvs().getRemark());
					
					dpsvc.addPostAcctDp(save);
				}

				KnbAcctSign knbASign=KnbAcctSignDao.selectOne_odb1(knbAcctSign.getAcctno(), true);			
				knbASign.setTrandt(trandt);			
				KnbAcctSignDao.updateOne_odb1(knbASign);
			
			if(CommUtil.equals(acdcIn.getCorpno(), acdcOut.getCorpno())){
				//交易平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), 
						CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
			}else{
				//交易平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), 
						CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._10);
			}
			
			//dataItem.setTranst(E_TRANST.SUCCESS);
			//dataItem.setDescrb("成功完成");
			//KnbAcctBachDao.updateOne_odb2(dataItem);		
			// 短信流水登记
			//登记贷方短信
		/*	KnaAcct acct1 = KnaAcctDao.selectOne_odb1(acctOut.getAcctno(), false);
			IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
			cplKubSqrd.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());// 内部交易码
			cplKubSqrd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
			cplKubSqrd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());// 交易时间
			cplKubSqrd.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
			cplKubSqrd.setTmstmp(DateTools2.getCurrentTimestamp());// 时间戳
			cplKubSqrd.setPmvl01("C");
			// 调用短信流水登记服务
			SysUtil.getInstance(IoPbSmsSvcType.class).pbTransqReg(cplKubSqrd);
			
			//登记借方短信
			//设置当前法人为转入账户法人	
			KnaAcct acct2 = KnaAcctDao.selectOne_odb1(acctIn.getAcctno(), false);
			IoPbKubSqrd cplKubSqrd2 = SysUtil.getInstance(IoPbKubSqrd.class);
			cplKubSqrd2.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());// 内部交易码
			cplKubSqrd2.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
			cplKubSqrd2.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());// 交易时间
			cplKubSqrd2.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
			cplKubSqrd2.setTmstmp(DateTools2.getCurrentTimestamp());// 时间戳
			cplKubSqrd2.setPmvl01("D");
			// 调用短信流水登记服务
			SysUtil.getInstance(IoPbSmsSvcType.class).pbTransqReg(cplKubSqrd2);*/			
			}
			DaoUtil.commitTransaction();
			log.debug("<<===================电子账户定期转账交易处理结束======================>>");		
		}
	   
}
