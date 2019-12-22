package cn.sunline.ltts.busi.dptran.trans.debt;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
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


public class secpou {

	public static void CheckTransBefore( final cn.sunline.ltts.busi.dptran.trans.debt.intf.Secpou.Input input,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Secpou.Property property,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Secpou.Output output){
		//1、参数不能为空
		if(CommUtil.isNull(input.getPasswd())){
			throw DpModuleError.DpstComm.BNAS0609();
		}
		if(CommUtil.isNull(input.getCrcycd())){
			throw DpModuleError.DpstComm.BNAS1101();
		}
		if(CommUtil.isNull(input.getTranam())){
			throw DpModuleError.DpstProd.BNAS0620();
		}
		if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO)<0){
			throw DpModuleError.DpstComm.BNAS0621();
		}
		if(CommUtil.isNull(input.getAcctno())){
			throw DpModuleError.DpstComm.BNAS1941();
		}
		if(CommUtil.isNull(input.getFronsq())){
			throw DpModuleError.DpstComm.BNAS1389();
		}
		if(CommUtil.isNull(input.getFrondt())){
			throw DpModuleError.DpstComm.BNAS1390();
		}
		if(CommUtil.isNull(input.getKeepdt())){
			throw DpModuleError.DpstComm.BNAS0399();
		}
		if(CommUtil.isNull(input.getAcctnm())){
			throw DpModuleError.DpstComm.BNAS0534();
		}
		property.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
		//查询卡客户对照信息
		IoCaSevQryTableInfo qryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaAcdc cplKnaAcdc = qryTableInfo.getKnaAcdcOdb1(input.getAcctno(), E_DPACST.NORMAL, false);
		if(CommUtil.isNull(cplKnaAcdc)){
			throw DpModuleError.DpstComm.BNAS1071();
		}
		String cardno = cplKnaAcdc.getCardno();
		//2、密码检查
//		if(!CaTools.checkCardPasswd(cardno, input.getPasswd(), "1", true)){
//			throw DpModuleError.DpstComm.E0003("密码错误");
//		}
		IoSrvDpPasswd.ValidatePasswdComm.InputSetter passwdInput = SysUtil.getInstance(IoSrvDpPasswd.ValidatePasswdComm.InputSetter.class);
		passwdInput.setAcctno(cardno);
		passwdInput.setPasswd(input.getPasswd());
		SysUtil.getInstance(IoSrvDpPasswd.class).validatePasswdComm(passwdInput);
		//3、检查账户状态
		ChkSaveDepositIn saveDpIn = SysUtil.getInstance(ChkSaveDepositIn.class);
		saveDpIn.setCrcycd(input.getCrcycd());
		saveDpIn.setEcctno(input.getAcctno());
		saveDpIn.setTranac(input.getCardno());
		saveDpIn.setCrcycd(input.getCrcycd());
		saveDpIn.setTranam(input.getTranam());
		saveDpIn.setAcctna(input.getAcctnm());
		saveDpIn.setTocdno(input.getCardno());
		ChkEacctStatusOut out = SysUtil.getInstance(DpProdSvcType.class).chkEacct(saveDpIn);
		//4、查询活期负债帐号
		DpProdSvc.QryDpAcctOut acctOut = SysUtil.getInstance(DpProdSvcType.class).qryDpAcct(input.getAcctno(), input.getCrcycd());
		property.setAcesno(acctOut.getSubsac());
		property.setCustna(out.getCustna());
		property.setDeptno(acctOut.getAcctno());
	}

	public static void CheckTransAfter( final cn.sunline.ltts.busi.dptran.trans.debt.intf.Secpou.Input input,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Secpou.Property property,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Secpou.Output output){
		//平衡性检查 开关开时检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
		//返回参数
		output.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		//bizlog.debug("交易时间：========="+BusiTools.getBusiRunEnvs().getTrantm());
		output.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());
		output.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
	}
}
