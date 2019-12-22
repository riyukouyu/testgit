package cn.sunline.ltts.busi.dptran.trans.online;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkSaveDepositIn;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;

public class dpsasv {

	public static void dpsasvCheck(
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Dpsasv.Input Input,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Dpsasv.Property Property,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Dpsasv.Output Output) {

		// 1、参数不能为空
		if (CommUtil.isNull(Input.getCrcycd())) {
			throw DpModuleError.DpstAcct.BNAS0634();
		}
		if (CommUtil.isNull(Input.getTranam())) {
			throw DpModuleError.DpstProd.BNAS0620();
		}
		if (CommUtil.compare(Input.getTranam(), BigDecimal.ZERO) < 0) {
			throw DpModuleError.DpstComm.BNAS0621();
		}
		// if (CommUtil.isNull(Input.getAcctno())) {
		// throw DpModuleError.DpstComm.E0005("客户账户");
		// }
		// if (CommUtil.isNull(Input.getFronsq())) {
		// throw DpModuleError.DpstComm.E0005("前置流水");
		// }
		// if (CommUtil.isNull(Input.getFrondt())) {
		// throw DpModuleError.DpstComm.E0005("前置日期");
		// }
		if (CommUtil.isNull(Input.getCardno())) {
			throw DpModuleError.DpstComm.BNAS0570();
		}
		if (CommUtil.isNull(Input.getAcctna())) {
			throw DpModuleError.DpstComm.BNAS0534();
		}
		// Property.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());

		// 查询活期负债帐号
		DpProdSvc.QryDpAcctOut acctOut = SysUtil.getInstance(
				DpProdSvcType.class).qryDpAcct(Input.getCustac(),
				Input.getCrcycd());

		// 检查账户状态
		ChkSaveDepositIn saveDpIn = SysUtil.getInstance(ChkSaveDepositIn.class);
		saveDpIn.setCrcycd(Input.getCrcycd());
		saveDpIn.setEcctno(Input.getCustac());
		saveDpIn.setCrcycd(Input.getCrcycd());
		saveDpIn.setTranam(Input.getTranam());
		saveDpIn.setAcctna(Input.getAcctna());
		saveDpIn.setTranac(acctOut.getAcctno());
		SysUtil.getInstance(DpProdSvcType.class).chkEacct(saveDpIn);

		// 交易币种与账户币种匹配性检查
		// if(Input.getCrcycd() != acctOut.getCrcycd()){
		// throw
		// DeptComm.E9999("交易币种["+Input.getCrcycd()+"]与账户["+acctOut.getAcctno()+"]币种["+acctOut.getCrcycd()+"]不匹配");
		// }

		Property.setAcctno(acctOut.getAcctno());
	}
}
