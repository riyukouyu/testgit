package cn.sunline.ltts.busi.cgtran.trans;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.iobus.servicetype.IoApAccount;
import cn.sunline.ltts.busi.iobus.servicetype.IoApAccount.queryAccountType.Output;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_ACLMFG;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_REBKTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RECPAY;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RISKLV;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_ACCTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ADJTTP;


public class chrgjt {

public static void befTran( final cn.sunline.ltts.busi.cgtran.trans.intf.Chrgjt.Input input,  final cn.sunline.ltts.busi.cgtran.trans.intf.Chrgjt.Property property){
	
	String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期
	
	if(CommUtil.compare(trandt, input.getPrtrdt())<=0){
		
		throw FeError.Chrg.BNASF320();
	}
	
	if(input.getAdjttp()!= E_ADJTTP.ALL){
		if(CommUtil.isNull(input.getAdjtam())||CommUtil.compare(input.getAdjtam(), BigDecimal.ZERO)==0){
			
			throw FeError.Chrg.BNASF244();
		}
		if(CommUtil.isNull(input.getAcctno())){
			
			throw FeError.Chrg.BNASF060();
		}
	}
	
	if(CommUtil.compare(input.getPrtrdt().substring(0, 4), trandt.substring(0, 4))!=0){
		throw FeError.Chrg.BNASF235();
	}
	
	
	if(input.getAdjttp()==E_ADJTTP.ADD||input.getAdjttp()==E_ADJTTP.DEL){//多收退回、多付收回扣减额度
		if(CommUtil.isNull(input.getPrtrdt())){
			throw FeError.Chrg.BNASF321();
			}
		if(CommUtil.isNull(input.getAcctno())){
			throw FeError.Chrg.BNASF248();
		}		
		//调整金额需小于等于原金额
		if(CommUtil.compare(input.getAdjtam(), input.getPrtram()) > 0){
			throw FeError.Chrg.BNASF245();
		}
		
		// 获取调整后账号类型			
		Output output = SysUtil.getInstance(Output.class);
		SysUtil.getInstance(IoApAccount.class).queryAccountType(input.getAcctno(), output);
		E_ACCTTP acctType1 = E_ACCTTP.DP;//调整后账号类型
		if (CommUtil.isNotNull(output)) {
				acctType1 = output.getAccttp();
		} else {
				throw FeError.Chrg.BNASF127();				
		}
		if(CommUtil.compare(acctType1, E_ACCTTP.IN) == 0){//内部户不扣减额度			
			return;
		}
		property.setIsckqt(E_YES___.YES);
		property.setAclmfg(E_ACLMFG._3);
		property.setAuthtp("02");
		property.setAcctrt(E_ACCTROUTTYPE.PERSON);
		property.setRebktp(E_REBKTP._99);
		property.setRisklv(E_RISKLV._01);
		property.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
		property.setSbactp(E_SBACTP._11);
		property.setAccttp(E_ACCATP.GLOBAL);
		property.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
		property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 业务跟踪编号
		property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 交易日期
				
		
		if(CommUtil.compare(input.getAcctno(), input.getToacct())!=0){//调整前后账号不一致
			if(input.getAdjttp()==E_ADJTTP.ADD){//多收退回，收方扣减额度
				
				property.setLimttp(E_LIMTTP.TI);
				property.setDcflag1(E_RECPAY.REC);
			}else{//多付收回，付方扣减额度
				property.setLimttp(E_LIMTTP.TR);
				property.setDcflag1(E_RECPAY.PAY);
			}
			property.setTranam(input.getAdjtam());
		}else{//调整前后账号一致，则存在金额为负，恢复额度的情况
			
            if(input.getAdjttp()==E_ADJTTP.ADD){//多收退回，金额为负，恢复付方额度				
				property.setLimttp(E_LIMTTP.TR);
				property.setDcflag1(E_RECPAY.PAY);
				property.setTranam(input.getAdjtam().negate());
				
			}else{//多付收回，付方扣减额度
				property.setLimttp(E_LIMTTP.TR);
				property.setDcflag1(E_RECPAY.PAY);
				property.setTranam(input.getAdjtam());
			}
		}
		
		IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcByCardno(input.getAcctno(), true);
		property.setCustac(tblKnaAcdc.getCustac());
	}
}
}
