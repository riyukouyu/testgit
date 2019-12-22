package cn.sunline.ltts.busi.cgtran.trans;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_ACLMFG;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_REBKTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RECPAY;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RISKLV;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CGTRTP;

public class charge {


public static void befTran( final cn.sunline.ltts.busi.cgtran.trans.intf.Charge.Input input,  final cn.sunline.ltts.busi.cgtran.trans.intf.Charge.Property property,  final cn.sunline.ltts.busi.cgtran.trans.intf.Charge.Output output){

	
	if(CommUtil.isNull(input.getTrantp())){
		throw FeError.Chrg.BNASF160();	
	}
	if(CommUtil.isNull(input.getRemark())){
		throw FeError.Chrg.BNASF325();	
	}
	
	String sendbr = CommTools.getBaseRunEnvs().getTrxn_branch();//请求机构
	
	if(input.getTrantp()==E_CGTRTP.SAVE&&CommUtil.isNull(input.getChrgtp())){
		throw FeError.Chrg.BNASF161();
	}
	if(!CommUtil.equals(sendbr, CommTools.getBaseRunEnvs().getTrxn_branch())&& input.getTrantp() != E_CGTRTP.SAVE){
		
		throw FeError.Chrg.BNASF163();	
	}
	if(CommUtil.compare(input.getTotlam(), BigDecimal.ZERO)<=0){
		
		throw FeError.Chrg.BNASF159();	
	}
	if(CommUtil.isNull(input.getAcctno())){
		throw FeError.Chrg.BNASF329();
	}		
	if(CommUtil.isNull(input.getAcctna())){
		throw FeError.Chrg.BNASF333();
	}		
	E_ACCTROUTTYPE accttp=ApAcctRoutTools.getRouteType(input.getAcctno());
	if((input.getTrantp()==E_CGTRTP.INACCOUNT||input.getTrantp()==E_CGTRTP.OUTACCOUNT)&&accttp!=E_ACCTROUTTYPE.INSIDE){
		
		throw FeError.Chrg.BNASF237();
	}
	if((input.getTrantp()==E_CGTRTP.PAY||input.getTrantp()==E_CGTRTP.SAVE)&&accttp==E_ACCTROUTTYPE.INSIDE){
		
		throw FeError.Chrg.BNASF221();
	}
	if(accttp==E_ACCTROUTTYPE.DEPOSIT){
		if(CommUtil.isNull(input.getCardno())){
			throw FeError.Chrg.BNASF500();
		}
		IoCaKnaAccs tblKnaAccs = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAccsOdb3(input.getAcctno(), true);
		if(!CommUtil.equals(tblKnaAccs.getCustac(), SysUtil.getInstance(IoCaSevQryTableInfo.class).
				getKnaAcdcByCardno(input.getCardno(), true).getCustac())){
			throw FeError.Chrg.BNASF501();
		}
	}
	
	if((input.getTrantp()==E_CGTRTP.PAY||input.getTrantp()==E_CGTRTP.SAVE)){
		property.setIsckqt(E_YES___.YES);//额度控制标志
		property.setAclmfg(E_ACLMFG._3);///累积限额标志
		property.setAuthtp("02");//认证方式
		property.setAcctrt(E_ACCTROUTTYPE.PERSON);//客户类型
		property.setRebktp(E_REBKTP._99);//收款行范围
		property.setRisklv(E_RISKLV._01);//风险承受等级
		property.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());//机构号
		property.setSbactp(E_SBACTP._11);//子账户类型
		property.setAccttp(E_ACCATP.GLOBAL);//账户类型
		property.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
		property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 业务跟踪编号
		property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 交易日期
		
		if(input.getTrantp()==E_CGTRTP.SAVE){//收费
			property.setLimttp(E_LIMTTP.TR);
			property.setDcflag1(E_RECPAY.PAY);
		}else{
			property.setLimttp(E_LIMTTP.TI);
			property.setDcflag1(E_RECPAY.REC);
		}
		IoCaKnaAccs tblKnaAccs = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAccsOdb3(input.getAcctno(), true);
		property.setCustac(tblKnaAccs.getCustac());
	}
	
}
}
