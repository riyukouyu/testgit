package cn.sunline.ltts.busi.dptran.trans.online;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.prod.DpProd;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCust;
import cn.sunline.ltts.busi.dp.type.DpPoc.AcctnoInfo;
import cn.sunline.ltts.busi.dp.type.DpPoc.CustacInfo;
import cn.sunline.ltts.busi.dp.type.DpPoc.TranInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkSaveDepositIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT;

public class dpsaop {

	public static void chkOpsbacInfo(
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Dpsaop.Input Input,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Dpsaop.Property Property,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Dpsaop.Output Output) {
		String custac = Input.getCustac();
		String acctna = Input.getAcctna();
		String prodcd = Input.getProdcd();
		String crcycd = Input.getCrcycd();
		E_TERMCD depttm = Input.getDepttm();

		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		
		
		if (CommUtil.isNull(acctna)) {
			throw DpModuleError.DpstComm.BNAS0538();
		}
		if (CommUtil.isNull(custac)) {
			throw DpModuleError.DpstComm.BNAS0955();
		} else {
			//kna_cust tblKna_cust = Kna_custDao.selectOne_odb1(custac, false);
			IoCaKnaCust tblKna_cust = caqry.getKnaCustByCustacOdb1(custac, false);
			if (CommUtil.isNull(tblKna_cust)) {
				throw DpModuleError.DpstComm.BNAS0754();
			}

		}
		// 产品编号
		if (CommUtil.isNull(crcycd)) {
			throw DpModuleError.DpstAcct.BNAS0634();
		}

		// 货币代号
		if (CommUtil.isNull(prodcd)) {
			throw DpModuleError.DpstComm.BNAS0665();
		}

		// 货币代号
		if (CommUtil.isNull(depttm)) {
			throw DpModuleError.DpstProd.BNAS1025();
		}

		// 有金额时密码不能为空
		if ((CommUtil.compare(Input.getTranam(), BigDecimal.ZERO) > 0)
				&& CommUtil.isNull(Input.getPasswd())) {
			throw DpModuleError.DpstComm.BNAS0609();
		}

		// 金额不能小于0
		if (CommUtil.compare(Input.getTranam(), BigDecimal.ZERO) < 0) {
			throw DpModuleError.DpstComm.BNAS0621();
		}

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

		// 查询卡信息
		//kna_acdc acdc = null;
		//acdc = Kna_acdcDao.selectOne_odb1(Input.getCustac(), E_DPACST.NORMAL,false);
		IoCaKnaAcdc acdc = caqry.getKnaAcdcOdb1(Input.getCustac(), E_DPACST.NORMAL,false);
		if (CommUtil.isNull(acdc)) {
			throw DpModuleError.DpstComm.BNAS1071();
		}

		// 获取产品信息
		KupDppb cplKupDppb = DpProd.getKupDppb(prodcd);

		// 获取账户类型控制信息
		E_CUSACT cacttp = null; // 客户账号类型
		//cacttp = KubCorpProc.getCsactp(Input.getCacttp());
		cacttp = Input.getCacttp();
		KupDppbActp cplKupDppbActp = DpProd.getKupDppbActp(prodcd,
				cacttp);

		// 获取开户控制信息
		KupDppbCust cplKupDppbCust = DpProd.getKupDppbCust(prodcd,
				crcycd);

		// 构造电子账户信息
		CustacInfo cplCustacInfo = SysUtil.getInstance(CustacInfo.class);
		cplCustacInfo.setCustac(custac);
		cplCustacInfo.setCustno(acctOut.getCustno());
		cplCustacInfo.setCacttp(cacttp);

		// 构造账户信息
		AcctnoInfo cplAcctnoInfo = SysUtil.getInstance(AcctnoInfo.class);
		cplAcctnoInfo.setCrcycd(crcycd);

		// 构造交易信息
		TranInfo cplTranInfo = SysUtil.getInstance(TranInfo.class);
		cplTranInfo.setTranam(Input.getTranam());
		cplTranInfo.setCrcycd(crcycd);

		// 客户账户类型检查
		DpProd.checkCacttp(cplCustacInfo, cplAcctnoInfo, cplKupDppb,
				cplKupDppbActp);

		// 开户控制检查
		DpProd.checkOpen(cplCustacInfo, cplAcctnoInfo, cplTranInfo,
				cplKupDppb, cplKupDppbCust);

		Property.setAcctno(acctOut.getAcctno());
		Property.setCustno(acctOut.getCustno());
		Property.setCardno(acdc.getCardno());
	}

	public static void prcOpsbacOut(
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Dpsaop.Input Input,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Dpsaop.Property Property,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Dpsaop.Output Output) {
		Output.setToacct(Property.getToacct());
		Output.setPddpfg(Property.getPddpfg());
	}

}
