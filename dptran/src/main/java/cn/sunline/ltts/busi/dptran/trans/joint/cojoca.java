package cn.sunline.ltts.busi.dptran.trans.joint;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.ca.base.DpCheckPubic;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_JOTRTP;

public class cojoca {

	public static void beforeMethod(
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Cojoca.Input input,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Cojoca.Property property,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Cojoca.Output output) {
		String cardno = input.getCardno();
		String platno = input.getPlatno();
		BigDecimal tranam = input.getTranam();

		if (CommUtil.isNull(cardno)) {
			throw DpModuleError.DpstComm.E9027("交易卡号");
		}
		if (CommUtil.isNull(platno)) {
			throw DpModuleError.DpstComm.E9027("平台编号");
		}
		if (CommUtil.isNull(tranam)) {
			throw DpModuleError.DpstComm.E9027("交易金额");
		} else if (CommUtil.compare(tranam, BigDecimal.ZERO) < 0) {
			throw DpModuleError.DpstProd.E0010("交易金额必须大于0！");
		} 
		String custac = CaTools.getCustacByCardnoCheckStatu(cardno);
		DpCheckPubic.getBusinoByPlatno(platno);
		String joinac = DpCheckPubic.chkJointInfo(custac, platno, tranam, E_JOTRTP.XF);
		property.setCustac(custac);
		property.setJoinac(joinac);
		
		//poc增加审计日志
		ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
		apAudit.regLogOnInsertBusiPoc(cardno);
				
	}

	public static void afterMetond(
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Cojoca.Input input,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Cojoca.Property property,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Cojoca.Output output) {
		RunEnvsComm runEnvs = CommTools.getBaseRunEnvs();
		output.setTransq(runEnvs.getTrxn_seq());
		output.setTrandt(runEnvs.getTrxn_date());
		output.setTranam(input.getTranam());
	}
}
