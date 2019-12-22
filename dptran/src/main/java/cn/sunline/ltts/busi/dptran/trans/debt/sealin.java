package cn.sunline.ltts.busi.dptran.trans.debt;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkEacctStatusOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkSaveDepositIn;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;


public class sealin {

	public static void CheckTransAfter( final cn.sunline.ltts.busi.dptran.trans.debt.intf.Sealin.Input Input,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Sealin.Property Property,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Sealin.Output Output){
		//平衡性检查 开关开时检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._03);
		
		//返回参数
		Output.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		//bizlog.debug("交易时间：========="+BusiTools.getBusiRunEnvs().getTrantm());
		Output.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());
		Output.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
	}

	public static void CheckTransBefore( final cn.sunline.ltts.busi.dptran.trans.debt.intf.Sealin.Input Input,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Sealin.Property Property,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Sealin.Output Output){
		//1、参数不能为空
		if(CommUtil.isNull(Input.getCrcycd())){
			throw DpModuleError.DpstComm.BNAS1101();
		}
		if(CommUtil.isNull(Input.getTranam())){
			throw DpModuleError.DpstAcct.BNAS0623();
		}
		if(CommUtil.compare(Input.getTranam(), BigDecimal.ZERO)<0){
			throw DpModuleError.DpstComm.BNAS0621();
		}
		if(CommUtil.isNull(Input.getAcctno())){
			throw DpModuleError.DpstComm.BNAS1388();
		}
		if(CommUtil.isNull(Input.getFronsq())){
			throw DpModuleError.DpstComm.BNAS1389();
		}
		if(CommUtil.isNull(Input.getFrondt())){
			throw DpModuleError.DpstComm.BNAS1390();
		}
		if(CommUtil.isNull(Input.getKeepdt())){
			throw DpModuleError.DpstComm.BNAS0399();
		}
		if(CommUtil.isNull(Input.getCardno())){
			throw DpModuleError.DpstComm.BNAS1391();
		}
		if(CommUtil.isNull(Input.getAcctnm())){
			throw DpModuleError.DpstComm.BNAS0532();
		}
		Property.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
		//2、检查账户状态
		ChkSaveDepositIn saveDpIn = SysUtil.getInstance(ChkSaveDepositIn.class);
		saveDpIn.setCrcycd(Input.getCrcycd());
		saveDpIn.setEcctno(Input.getAcctno());
		saveDpIn.setTranac(Input.getCardno());
		saveDpIn.setCrcycd(Input.getCrcycd());
		saveDpIn.setTranam(Input.getTranam());
		saveDpIn.setAcctna(Input.getAcctnm());
		saveDpIn.setTocdno(Input.getCardno()); 
		ChkEacctStatusOut out = SysUtil.getInstance(DpProdSvcType.class).chkEacct(saveDpIn);
		//3、查询活期负债帐号
		DpProdSvc.QryDpAcctOut acctOut = SysUtil.getInstance(DpProdSvcType.class).qryDpAcct(Input.getAcctno(), Input.getCrcycd());
		Property.setAcesno(acctOut.getSubsac());
		Property.setCustna(out.getCustna());
		Property.setDeptno(acctOut.getAcctno());
	}
}
