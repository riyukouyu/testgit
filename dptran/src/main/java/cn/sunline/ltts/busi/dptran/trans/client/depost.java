package cn.sunline.ltts.busi.dptran.trans.client;


import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;



public class depost {
	
	/**
	 * 电子账户钱包账户提现
	 * 交易后处理：调用内部户平衡性检查
	 * */
	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.client.intf.Depost.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Depost.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Depost.Output output){
		
		// 获取输入项
//		E_YES___ ispass = input.getChkpwd().getIspass();// 是否验密
//		String passtp = input.getChkpwd().getAuthtp();// 密码类型
//		String authif = input.getChkpwd().getAuthif();// 加密因子
//		String passwd = input.getChkpwd().getPasswd();// 密码
		
		//acpayt.chkPasswd(ispass, passtp, authif, passwd);// 密码检查
				
		//输入参数非空检查
		
		if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0){
			throw DpModuleError.DpstComm.BNAS0630();
		}
		//电子账户状态检查
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		
		IoCaKnaAcdc kna_acdc = caqry.getKnaAcdcOdb2(input.getCardno(), false); //获得对内的电子账号ID
		if(CommUtil.isNull(kna_acdc)){
			throw DpModuleError.DpstComm.BNAS0754();
		}
		
		if(kna_acdc.getStatus() == E_DPACST.CLOSE){
			throw DpModuleError.DpstComm.BNAS1922();
		}
		
//		IoCaKnaCust kna_cust = caqry.kna_cust_selectOne_odb1(kna_acdc.getCustac(), false);
//		if(CommUtil.isNull(kna_cust)){
//			throw DpModuleError.DpstComm.E9999("该电子账号不存在");
//		}
		
		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		E_ACCATP accatp = cagen.qryAccatpByCustac(kna_acdc.getCustac());
		
		if(accatp == E_ACCATP.WALLET){
			throw DpModuleError.DpstComm.BNAS0743();
		}
		
		CapitalTransCheck.ChkAcctstOT(kna_acdc.getCustac());
		
		KnaAcct tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(kna_acdc.getCustac(), E_ACSETP.SA); //获取结算户信息
		
		KnaAcct tblKnaWact = CapitalTransDeal.getSettKnaAcctSub(kna_acdc.getCustac(), E_ACSETP.MA); //获取钱包户信息
		
		if(CommUtil.isNotNull(input.getToacct())){
			if(!CommUtil.equals(input.getToacct(), tblKnaWact.getAcctno())){
				throw DpModuleError.DpstComm.BNAS1923();
			}
		}
		
		CapitalTransCheck.ChkAcctstWord(kna_acdc.getCustac());
		
		property.setWaacct(tblKnaWact.getAcctno());
		property.setAcctno(tblKnaAcct.getAcctno());
		
		//电子账户钱包账户余额检查
		if(CommUtil.compare(tblKnaWact.getOnlnbl(), input.getTranam()) < 0){
			throw DpModuleError.DpstComm.BNAS1924();
		}
		property.setCustac(kna_acdc.getCustac());
		property.setBusino("");
		property.setLinkno("");
		property.setAuacfg(E_YES___.NO);
		property.setSmrycd1(BusinessConstants.SUMMARY_ZR);//结算户摘要码为转入
		property.setBrchno(tblKnaAcct.getBrchno());
		property.setWaacna(tblKnaAcct.getAcctna());
		
	}

	
		
	/**
	 * 电子账户钱包账户提现
	 * 交易后处理：调用内部户平衡性检查
	 * */
	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.client.intf.Depost.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Depost.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Depost.Output output){
		//平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
		
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
	}

	
}
