package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaAcctJoint;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaAcctJointDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;

public class qrjoin {

	public static void qrJointInfo(
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrjoin.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrjoin.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrjoin.Output output) {

		String cardno = input.getCardno();
		String platno = input.getPlatno();
		if (CommUtil.isNull(cardno)) {
			throw DpModuleError.DpstProd.E0010("交易卡号不能为空！");
		}
		if (CommUtil.isNull(input.getPlatno())) {
			throw DpModuleError.DpstProd.E0010("平台编号不能为空！");
		}
		String custac = CaTools.getCustacByCardno(cardno);
		KnaAcctJoint knaAcctJoint = KnaAcctJointDao.selectOne_odb2(platno, custac, false);
		if (CommUtil.isNotNull(knaAcctJoint)) {
			output.setJoinac(knaAcctJoint.getJoinac());
			output.setLmacbl(knaAcctJoint.getOnlnbl());
		}
		
		//poc增加审计日志
		ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
		apAudit.regLogOnInsertBusiPoc(input.getCardno());
	}
}
