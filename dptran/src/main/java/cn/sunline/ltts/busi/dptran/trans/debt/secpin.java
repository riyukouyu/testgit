package cn.sunline.ltts.busi.dptran.trans.debt;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkEacctStatusOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkSaveDepositIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;


public class secpin {
	private static final BizLog bizlog = BizLogUtil.getBizLog(secpin.class);
	/**
	 * 交易前检查*
	 * @param Input
	 * @param Property
	 * 
	 * @author wanggl
	 */
	public static void CheckTransBefore( final cn.sunline.ltts.busi.dptran.trans.debt.intf.Secpin.Input input,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Secpin.Property property,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Secpin.Output output){
		//1、参数不能为空
		
				if(CommUtil.isNull(input.getCrcycd())){
					throw DpModuleError.DpstComm.BNAS1101();
				}
				if(CommUtil.isNull(input.getTranam())){
					throw DpModuleError.DpstProd.BNAS0620();
				}
				if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO)<=0){
					throw DpModuleError.DpstComm.BNAS0621();
				}
				if(CommUtil.isNull(input.getAcctno())){
					throw DpModuleError.DpstComm.BNAS1388();
				}
				if(CommUtil.isNull(input.getTransq())){
					throw DpModuleError.DpstComm.BNAS1942();
				}
				if(CommUtil.isNull(input.getTrandt())){
					throw DpModuleError.DpstComm.BNAS1943();
				}
//				if(CommUtil.isNull(input.getKeepdt())){
//					throw DpModuleError.DpstComm.E0005("清算日期");
//				}
				
				IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
				IoCaKnaAcdc acdc = SysUtil.getInstance(IoCaKnaAcdc.class);
				try {
					acdc = caqry.getKnaAcdcOdb2(input.getAcctno(), true);
				} catch (Exception e) {
					throw DpModuleError.DpstComm.BNAS0444();
				}
				
				property.setKeepdt(CommTools.getBaseRunEnvs().getTrxn_date());
				if(CommUtil.isNull(input.getAcctnm())){
					throw DpModuleError.DpstComm.BNAS0533();
				}
				property.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
				
				//2、检查账户状态
				ChkSaveDepositIn saveDpIn = SysUtil.getInstance(ChkSaveDepositIn.class);
				saveDpIn.setCrcycd(input.getCrcycd());
				saveDpIn.setEcctno(acdc.getCustac());
				saveDpIn.setTranac(input.getCardno());
				saveDpIn.setCrcycd(input.getCrcycd());
				saveDpIn.setTranam(input.getTranam());
				saveDpIn.setAcctna(input.getAcctnm());
				saveDpIn.setTocdno(input.getCardno());
				ChkEacctStatusOut out = SysUtil.getInstance(DpProdSvcType.class).chkEacct(saveDpIn);
				//3、查询活期负债帐号
				DpProdSvc.QryDpAcctOut acctOut = SysUtil.getInstance(DpProdSvcType.class).qryDpAcct(acdc.getCustac(), input.getCrcycd());
				property.setAcesno(acctOut.getSubsac());
				property.setCustna(out.getCustna());
				property.setDeptno(acctOut.getAcctno());
				property.setCustac(acdc.getCustac());
	}
	public static void CheckTransAfter( final cn.sunline.ltts.busi.dptran.trans.debt.intf.Secpin.Input input,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Secpin.Property property,  final cn.sunline.ltts.busi.dptran.trans.debt.intf.Secpin.Output output){
		//平衡性检查 开关开时检查
//		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq());
		
		//返回参数
		output.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());
		bizlog.debug("交易时间：========="+BusiTools.getBusiRunEnvs().getTrantm());
		output.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());
		output.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
	}
}
