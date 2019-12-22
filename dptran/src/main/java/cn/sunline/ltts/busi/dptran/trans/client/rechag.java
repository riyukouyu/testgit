package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;


public class rechag {
	/**
	 * 电子账户钱包账户充值
	 * 交易前处理：检查电子账户状态、状态字是否正常
	 * */
	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.client.intf.Rechag.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Rechag.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Rechag.Output output){
		
		// 获取输入项
		E_YES___ ispass = input.getChkpwd().getIspass();// 是否验密
		String passtp = input.getChkpwd().getAuthtp();// 密码类型
		String authif = input.getChkpwd().getAuthif();// 加密因子
		String passwd = input.getChkpwd().getPasswd();// 密码
		
		acpayt.chkPasswd(ispass, passtp, authif, passwd);// 密码检查
		
		//输入参数非空检查
		if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0){
			throw DpModuleError.DpstAcct.BNAS0623();
		}
	
		//电子账户状态检查
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		
		IoCaKnaAcdc kna_acdc = caqry.getKnaAcdcOdb2(input.getCardno(), false); //获得对内的电子账号ID
		if(CommUtil.isNull(kna_acdc)){
			throw DpModuleError.DpstComm.BNAS0750();
		}
		if(kna_acdc.getStatus() == E_DPACST.CLOSE){
			throw DpModuleError.DpstComm.BNAS0740();
		}
		
		CapitalTransCheck.ChkAcctstOT(kna_acdc.getCustac()); //1.转出方电子账户状态校验
		
		CapitalTransCheck.ChkAcctstWord(kna_acdc.getCustac()); //1.转出方电子账户状态字校验
		
		IoCaKnaCust kna_cust = caqry.getKnaCustByCustacOdb1(kna_acdc.getCustac(), true); //根据电子账号ID查询电子账户信息
		
		E_ACCATP accatp = cagen.qryAccatpByCustac(kna_acdc.getCustac());
		
		if(accatp == E_ACCATP.WALLET){
			throw DpModuleError.DpstComm.BNAS0743();
		}
		
		if(CommUtil.isNull(input.getToacct())){
			KnaAcct acct = CapitalTransDeal.getSettKnaAcctSub(kna_acdc.getCustac(), E_ACSETP.MA); //获取钱包户信息
			property.setWaacct(acct.getAcctno());
		}else{
			property.setWaacct(input.getToacct());
		}
		
		CapitalTransCheck.ChkAcctstOT(kna_cust.getCustac());
		CapitalTransCheck.CkhFrozstOT(kna_acdc);
		
		KnaAcct KnaAcct = CapitalTransDeal.getSettKnaAcctSub(kna_acdc.getCustac(), E_ACSETP.SA);//根据电子账号ID获取结算户信息
		//BigDecimal bal = SysUtil.getInstance(DpAcctSvcType.class).getProductBal(kna_acdc.getCustac(), KnaAcct.getCrcycd(), false);
		
		// 可用余额 addby xiongzhao 20161223 
		BigDecimal bal = SysUtil.getInstance(DpAcctSvcType.class)
				.getAcctaAvaBal(kna_acdc.getCustac(), KnaAcct.getAcctno(),
						KnaAcct.getCrcycd(), E_YES___.YES, E_YES___.NO);
		
		//电子账户结算户余额检查
		
		if(CommUtil.compare(bal, input.getTranam()) < 0){
			throw DpModuleError.DpstComm.BNAS1926();
		}
		
		KnaAcct acct2 = KnaAcctDao.selectOne_odb1(property.getWaacct(), false); //获取钱包账户信息
		if(CommUtil.isNull(acct2)){
			throw DpModuleError.DpstComm.BNAS1927();
		}
		property.setCustac(kna_acdc.getCustac());
		property.setWaacna(KnaAcct.getAcctna());
		property.setAcctno(KnaAcct.getAcctno());
		property.setAuacfg(E_YES___.YES);
		property.setSmrycd1(BusinessConstants.SUMMARY_ZC);//结算户摘要码为转出
		
	}
	
	/**
	 * 电子账户钱包账户充值
	 * 交易后处理：调用内部户平衡性检查
	 * */
	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.client.intf.Rechag.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Rechag.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Rechag.Output output){
		
		
		//平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
		
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
		
	}

	public static void prcyInacRegister( final cn.sunline.ltts.busi.dptran.trans.client.intf.Rechag.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Rechag.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Rechag.Output output){
		
		E_INSPFG invofg = property.getInvofg();// 转出账号是否涉案

		// 涉案账户交易信息登记
		if (E_INSPFG.INVO == invofg) {

			// 独立事务
			DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
				@Override
				public Void execute() {

					// 获取涉案账户交易信息登记输入信息
					IoCaKnbTrinInput cplKnbTrin = SysUtil.getInstance(IoCaKnbTrinInput.class);
					cplKnbTrin.setTrantp(cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRANTP.TRANSFER);// 交易类型
					cplKnbTrin.setOtcard(input.getCardno());// 转出账号
					cplKnbTrin.setOtacna(input.getAcctna());// 转出账号名称
					cplKnbTrin.setOtbrch(CommTools.getBaseRunEnvs().getTrxn_branch());// 转出机构
					cplKnbTrin.setTranam(input.getTranam());// 交易金额
					cplKnbTrin.setCrcycd(input.getCrcycd());// 币种
					cplKnbTrin.setIssucc(E_YES___.NO);// 交易是否成功

					// 涉案账户交易信息登记
					SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(cplKnbTrin);

					return null;
				}
			});
			
			// 转出账号涉案
			if (E_INSPFG.INVO == invofg) {
				throw DpModuleError.DpstAcct.BNAS1910();
			}
			
		}

	}
	
}