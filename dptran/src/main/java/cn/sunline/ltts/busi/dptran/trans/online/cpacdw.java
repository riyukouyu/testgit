package cn.sunline.ltts.busi.dptran.trans.online;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.acct.DpSaveProc;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSrvDpPasswd;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkEacctStatusOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkSaveDepositIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;

public class cpacdw {

	public static void CheckTransAfter(
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Cpacdw.Input Input,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Cpacdw.Property Property,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Cpacdw.Output Output) {
		// 平衡性检查 开关开时检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(
				CommTools.getBaseRunEnvs().getTrxn_date(),
				CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);

		// 返回参数
		Output.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		// bizlog.debug("交易时间：========="+BusiTools.getBusiRunEnvs().getTrantm());
		Output.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());
		Output.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
	}

	public static void CheckTransBefore(
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Cpacdw.Input Input,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Cpacdw.Property Property,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Cpacdw.Output Output) {
		// 1、参数不能为空
		if (CommUtil.isNull(Input.getPasswd())) {
			throw DpModuleError.DpstComm.BNAS0609();
		}
		if (CommUtil.isNull(Input.getCrcycd())) {
			throw DpModuleError.DpstComm.BNAS1101();
		}
		if (CommUtil.isNull(Input.getTranam())) {
			throw DpModuleError.DpstProd.BNAS0620();
		}
		if (CommUtil.compare(Input.getTranam(), BigDecimal.ZERO) < 0) {
			throw DpModuleError.DpstComm.BNAS0621();
		}
		if (CommUtil.isNull(Input.getAcctno())) {
			throw DpModuleError.DpstComm.BNAS1941();
		}
		if (CommUtil.isNull(Input.getAcctno())) {
			throw DpModuleError.DpstComm.BNAS1388();
		}
		if (CommUtil.isNull(Input.getFronsq())) {
			throw DpModuleError.DpstComm.BNAS1389();
		}
		if (CommUtil.isNull(Input.getFrondt())) {
			throw DpModuleError.DpstComm.BNAS1390();
		}
		if (CommUtil.isNull(Input.getAcctnm())) {
			throw DpModuleError.DpstComm.BNAS0534();
		}
		Property.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
		//kna_acdc acdc = null;
		//acdc = Kna_acdcDao.selectOne_odb1(Input.getAcctno(), E_DPACST.NORMAL,false);
		IoCaKnaAcdc acdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb1(Input.getAcctno(), E_DPACST.NORMAL, false);
		if (CommUtil.isNull(acdc)) {
			throw DpModuleError.DpstComm.BNAS1071();
		}
		String cardno = acdc.getCardno();
		// 2、密码检查
		IoSrvDpPasswd.ValidatePasswdComm.InputSetter passwdInput = SysUtil.getInstance(IoSrvDpPasswd.ValidatePasswdComm.InputSetter.class);
		passwdInput.setAcctno(cardno);
		passwdInput.setPasswd(Input.getPasswd());
		SysUtil.getInstance(IoSrvDpPasswd.class).validatePasswdComm(passwdInput);
		// 3、查询活期负债帐号
		DpProdSvc.QryDpAcctOut acctOut = SysUtil.getInstance(
				DpProdSvcType.class).QryDpByAcctSub(Input.getAcctno(),Input.getSubsac());

		// 4、检查账户状态
		ChkSaveDepositIn saveDpIn = SysUtil.getInstance(ChkSaveDepositIn.class);
		saveDpIn.setCrcycd(Input.getCrcycd());
		saveDpIn.setEcctno(Input.getAcctno());
		saveDpIn.setTranac(Input.getCardno());
		saveDpIn.setCrcycd(Input.getCrcycd());
		saveDpIn.setTranam(Input.getTranam());
		saveDpIn.setAcctna(Input.getAcctnm());
		saveDpIn.setTocdno(Input.getCardno());
		ChkEacctStatusOut out = DpSaveProc.chkEacctSt(saveDpIn);
		Property.setAcesno(acctOut.getSubsac());
		Property.setCustna(out.getCustna());
		Property.setDeptno(acctOut.getAcctno());
	}
}
