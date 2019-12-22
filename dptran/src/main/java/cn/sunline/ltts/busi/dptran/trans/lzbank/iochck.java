package cn.sunline.ltts.busi.dptran.trans.lzbank;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;



public class iochck {

	public static void dealIochck( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Iochck.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Iochck.Output output){
		String cardno = input.getCardno();//卡号
		String toacct = input.getToacct();//对方账号
		E_CAPITP capitp = input.getCapitp();//转账交易类型
		BigDecimal tranam = input.getTranam();//交易金额
		
		E_YES___ isTranin = E_YES___.YES;//是否是转入(Yes-转入 NO-转出)
		
		/**
		 * 1,输入校验
		 */
		if(CommUtil.isNull(cardno)){
			throw DpModuleError.DpstAcct.BNAS0311();
		}
//		消费类型交易没有对方账号
//		if(CommUtil.isNull(toacct)){
//			throw DpModuleError.DpstComm.E9999("对方账号不能为空");
//		}
		if(CommUtil.isNull(capitp)){
			throw DpModuleError.DpstComm.E9999("转账交易类型不能为空");
		}
		if(CommUtil.isNull(tranam)){
			throw DpModuleError.DpstComm.E9999("交易金额不能为空");
		}
		
		/**
		 * 2，出账与入账 判断
		 */
		KnaAcdc tblKnaAcdc = SysUtil.getInstance(KnaAcdc.class);
		//本行借记卡转入，（大额来账，银联无卡转入，银联CUPS转入）
		if(E_CAPITP.IN101==capitp){
			isTranin = E_YES___.YES;
		}else//本行借记卡转出，（大额往账，银联无卡转出，银联CUPS转出）
		if(E_CAPITP.OT201==capitp){
			isTranin = E_YES___.NO;
		}else{
			throw DpModuleError.DpstComm.E9999("转账交易类型不支持");
		}
		
		/**
		 * 3,账户存在性校验
		 */
		tblKnaAcdc = KnaAcdcDao.selectOne_odb2(cardno, false);
		if(CommUtil.isNull(tblKnaAcdc)){
			output.setIsable(CaEnumType.E_CKTRIF.NOAC);
			output.setRemark("账户不存在");
			output.setCustnm("");
			return;
		}
		/**
		 * 4,获取账户附属信息
		 */
		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(tblKnaAcdc.getCustac(), true);
		
		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		E_ACCATP accatp = cagen.qryAccatpByCustac(tblKnaAcdc.getCustac()); //账户类型
		
//		/**
//		 * 5，电子账户状态校验
//		 */
//		IoCaKnaAcdc inacdc = ActoacDao.selKnaAcdc(input.getCardno(), false);
//		if(CommUtil.isNull(inacdc)){
//			output.setIsable(CaEnumType.E_CKTRIF.NOAC);
//			output.setRemark("账户不存在");
//			output.setCustnm("");
//			return;
//		}
//		
//		if(inacdc.getStatus() == E_DPACST.CLOSE){
//			output.setIsable(CaEnumType.E_CKTRIF.NOAC);
//			output.setRemark("账户已销户");
//			output.setCustnm(tblKnaCust.getCustna());
//		}
		
		/**
		 * 6,资金交易前检查
		 */
		AcTranfeChkIN chkIN = SysUtil.getInstance(AcTranfeChkIN.class);
		chkIN.setAccatp(accatp);
		chkIN.setCardno(input.getCardno()); //电子账号卡号
		chkIN.setCustac(tblKnaCust.getCustac()); //电子账号ID
		chkIN.setCustna(tblKnaCust.getCustna());
		chkIN.setCapitp(capitp);
		chkIN.setOpcard(input.getToacct());
		chkIN.setOppona(null);
		chkIN.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		CapitalTransCheck.chkTranfe(chkIN);
		
		
		/**
		 * 7，状态校验
		 */
		if(isTranin == E_YES___.YES){
			CapitalTransCheck.ChkAcctstIN(tblKnaCust.getCustac());
			CapitalTransCheck.ChkAcctFrozIN(tblKnaCust.getCustac());
		}else{
			CapitalTransCheck.ChkAcctstOT(tblKnaCust.getCustac());
			CapitalTransCheck.ChkAcctFrozOT(tblKnaCust.getCustac());
		}
		
		/**
		 * 8，转出余额不足校验
		 */
		if(isTranin == E_YES___.NO){
			KnaAcct tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
			BigDecimal usebal = CommTools.getRemoteInstance(DpAcctSvcType.class).getAcctaAvaBal(tblKnaAcct.getCustac(), tblKnaAcct.getAcctno(),tblKnaAcct.getCrcycd(), E_YES___.YES, E_YES___.NO);
			if(CommUtil.compare(usebal, tranam) < 0){
				output.setIsable(CaEnumType.E_CKTRIF.NOAC);
				output.setRemark("电子账户余额不足");
				output.setCustnm(tblKnaCust.getCustna());
				return;
			}
		}
		output.setIsable(CaEnumType.E_CKTRIF.ENOK);
		output.setRemark("");
		output.setCustnm(tblKnaCust.getCustna());
	}
}
