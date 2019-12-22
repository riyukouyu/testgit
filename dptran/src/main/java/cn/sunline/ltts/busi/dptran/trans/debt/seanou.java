package cn.sunline.ltts.busi.dptran.trans.debt;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkEacctStatusOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkSaveDepositIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;


public class seanou {

	public static void CheckTransBefore( final cn.sunline.ltts.busi.dptran.trans.debt.intf.Seanou.Input input,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Seanou.Property property,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Seanou.Output output){
//		//1、参数不能为空--不验密
//		if(CommUtil.isNull(Input.getPasswd())){
//			throw DpModuleError.DpstComm.E0005("交易密码");
//		}
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
		if(CommUtil.isNull(input.getAcctno())){
			throw DpModuleError.DpstComm.BNAS1388();
		}
		if(CommUtil.isNull(input.getFronsq())){
			throw DpModuleError.DpstComm.BNAS1389();
		}
		if(CommUtil.isNull(input.getFrondt())){
			throw DpModuleError.DpstComm.BNAS1390();
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
//		String cardno = acdc.getCardno();
//		//2、密码检查 不验密
//		if(!CaTools.checkCardPasswd(cardno, Input.getPasswd(), "1", true)){
//			throw DpModuleError.DpstComm.E0003("密码错误");
//		}	
		//3、查询活期负债帐号
		DpProdSvc.QryDpAcctOut acctOut = SysUtil.getInstance(DpProdSvcType.class).qryDpAcct(input.getAcctno(), input.getCrcycd());
		
		//4、检查账户状态
		ChkSaveDepositIn saveDpIn = SysUtil.getInstance(ChkSaveDepositIn.class);
		saveDpIn.setCrcycd(input.getCrcycd());
		saveDpIn.setEcctno(input.getAcctno());
		saveDpIn.setTranac(input.getCardno());
		saveDpIn.setCrcycd(input.getCrcycd());
		saveDpIn.setTranam(input.getTranam());
		saveDpIn.setAcctna(input.getAcctnm());
		saveDpIn.setTocdno(input.getCardno());
		ChkEacctStatusOut out = SysUtil.getInstance(DpProdSvcType.class).chkEacct(saveDpIn);
		property.setAcesno(acctOut.getSubsac());
		property.setCustna(out.getCustna());
		property.setDeptno(acctOut.getAcctno());
	}

	public static void CheckTransAfter( final cn.sunline.ltts.busi.dptran.trans.debt.intf.Seanou.Input input,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Seanou.Property property,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Seanou.Output output){
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._04);
		//返回参数
		output.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		//bizlog.debug("交易时间：========="+BusiTools.getBusiRunEnvs().getTrantm());
		output.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());
		output.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
	}
}
