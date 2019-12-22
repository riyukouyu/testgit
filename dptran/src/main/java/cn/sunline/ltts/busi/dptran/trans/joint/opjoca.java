package cn.sunline.ltts.busi.dptran.trans.joint;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaAcctJoint;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaAcctJointDao;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnbPlatInfo;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnbPlatInfoDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

public class opjoca {

	public static void beforeMethod(
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Opjoca.Input input,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Opjoca.Property property,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Opjoca.Output output) {

		String cardno = input.getCardno();
		String platno = input.getPlatno();

		if (CommUtil.isNull(cardno)) {
			throw DpModuleError.DpstComm.E9027("交易卡号");
		}
		if (CommUtil.isNull(input.getJoinna())) {
			throw DpModuleError.DpstComm.E9027("联名账户名称");
		}
		if (CommUtil.isNull(input.getPlatno())) {
			throw DpModuleError.DpstComm.E9027("平台编号");
		}
		if (CommUtil.isNull(input.getSginam())) {
			throw DpModuleError.DpstComm.E9027("转入单笔限额");
		}
		if (CommUtil.isNull(input.getSgouam())) {
			throw DpModuleError.DpstComm.E9027("转出单笔限额");
		}
		if (CommUtil.isNull(input.getSgspam())) {
			throw DpModuleError.DpstComm.E9027("消费单笔限额");
		}
		String custac = CaTools.getCustacByCardnoCheckStatu(cardno);
		if (CommUtil.isNull(custac)) {
			throw DpModuleError.DpstProd.BNAS0935();
		}
		
		//判断是否已开通联名账户
		KnaAcctJoint knaAcctJoint = KnaAcctJointDao.selectOne_odb2(platno, custac, false);
		if (CommUtil.isNotNull(knaAcctJoint)) {
			throw DpModuleError.DpstProd.E0010("用户已开通联名账户！");
		}
		// 获取创建人电子账户信息
		KnaCust knaCust = KnaCustDao.selectOne_odb1(custac, false);

		if (CommUtil.isNull(knaCust)) {
			throw DpModuleError.DpstProd.E0010("创建人电子账号ID不存在！");
		}
		if (E_ACCTST.NORMAL != knaCust.getAcctst()) {
			throw DpModuleError.DpstProd.E0010("您的电子账户状态异常，无法操作！");
		}

		// 判断电子账户状态
		IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(
				IoDpFrozSvcType.class).getAcStatusWord(custac);
		if (CommUtil.isNotNull(cplAcStatus)) {
			if (E_YES___.YES == cplAcStatus.getBrfroz()
					|| E_YES___.YES == cplAcStatus.getDbfroz()
					|| E_YES___.YES == cplAcStatus.getAlstop()) {

				throw DpModuleError.DpstProd.E0010("您的电子账户状态字异常！");
			}
		}

		// 校验平台信息
		KnbPlatInfo knbPlatInfo = KnbPlatInfoDao.selectOne_odb1(platno, false);
		if (CommUtil.isNull(knbPlatInfo)) {
			throw DpModuleError.DpstProd.E0010("平台未注册，请核对平台编号！");
		}
		property.setCustac(custac);
		property.setCustno(knaCust.getCustno());
		
		//poc增加审计日志
		ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
		apAudit.regLogOnInsertBusiPoc(cardno);
		
	}

	public static void afterMethod(
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Opjoca.Input input,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Opjoca.Property property,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Opjoca.Output output) {
		output.setJoinac(property.getJoinac());
	}
}
