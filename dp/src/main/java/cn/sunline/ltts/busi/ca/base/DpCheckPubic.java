package cn.sunline.ltts.busi.ca.base;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaAcctJoint;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaAcctJointDao;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaAcctJointLimit;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaAcctJointLimitDao;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnbPlatInfo;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnbPlatInfoDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_JOTRTP;

public class DpCheckPubic {

	private static BizLog bizLog = BizLogUtil.getBizLog(DpCheckPubic.class);
	
	public static String getBusinoByPlatno (String platno){
		// 校验平台信息
		KnbPlatInfo knbPlatInfo = KnbPlatInfoDao.selectOne_odb1(platno, false);
		if (CommUtil.isNull(knbPlatInfo)) {
			throw DpModuleError.DpstProd.E0010("平台信息不存在！");
		}
		return knbPlatInfo.getPllqbu();
	}
	
	public static String chkJointInfo(String custac, String platno,
			BigDecimal tranam, E_JOTRTP jotrtp) {
		// 校验联名账户
		KnaAcctJoint knaAcctJoint = KnaAcctJointDao.selectOne_odb2(platno,
				custac, false);
		if (CommUtil.isNull(knaAcctJoint)) {
			throw DpModuleError.DpstProd.E0010("联名账户信息不存在！");
		}
		if (jotrtp == E_JOTRTP.TX || jotrtp == E_JOTRTP.XF) {
			BigDecimal onlnbl = knaAcctJoint.getOnlnbl();
			if (CommUtil.compare(onlnbl, tranam) < 0) {
				throw DpModuleError.DpstProd.E0010("可用金额不足!");
			}
		}
		String joinac = knaAcctJoint.getJoinac();
		// 校验联名账户额度
		KnaAcctJointLimit knaAcctJointLimit = KnaAcctJointLimitDao
				.selectOne_odb1(joinac, false);
		if (CommUtil.isNull(knaAcctJointLimit)) {
			throw DpModuleError.DpstProd.E0010("联名账户额度信息不存在！");
		}

		if (jotrtp == E_JOTRTP.CZ) {
			if (CommUtil.compare(tranam, knaAcctJointLimit.getSginam()) > 0) {
				throw DpModuleError.DpstProd.E0010("交易金额不能大于转入单笔限额！");
			}
		} else if (jotrtp == E_JOTRTP.TX) {
			if (CommUtil.compare(tranam, knaAcctJointLimit.getSgouam()) > 0) {
				throw DpModuleError.DpstProd.E0010("交易金额不能大于转出单笔限额！");
			}
		} else if (jotrtp == E_JOTRTP.XF) {
			if (CommUtil.compare(tranam, knaAcctJointLimit.getSgspam()) > 0) {
				throw DpModuleError.DpstProd.E0010("交易金额不能大于消费单笔限额！");
			}
		}
		return joinac;
	}

	public static String chkUsebal(String custac, BigDecimal tranam) {
		E_ACCATP accatp = CommTools.getRemoteInstance(
				IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac);
		// 获取结算户或钱包户
		KnaAcct knaAcct = SysUtil.getInstance(KnaAcct.class); // 转出方子账号
		
		if (accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE) {
			knaAcct = CapitalTransDeal.getSettKnaAcctSub(custac, E_ACSETP.SA);
		} else {
			knaAcct = CapitalTransDeal.getSettKnaAcctSub(custac, E_ACSETP.MA);
		}
		// 3.转出方可用余额校验
		BigDecimal realam = tranam; // 该金额用于校验可用余额是否满足交易金额和费用的总额
		if (accatp == E_ACCATP.WALLET) { // 还信用卡允许使用三类户，需要校验三类户金额
			if (CommUtil.compare(knaAcct.getOnlnbl(), realam) < 0) {
				throw DpModuleError.DpstComm.BNAS0442();
			}
		} else {
			BigDecimal usebal = CommTools
					.getRemoteInstance(DpAcctSvcType.class).getAcctaAvaBal(
							custac, knaAcct.getAcctno(), knaAcct.getCrcycd(),
							E_YES___.YES, E_YES___.NO);

			if (CommUtil.compare(usebal, realam) < 0) {
				throw DpModuleError.DpstComm.BNAS0442();
			}
		}

		bizLog.debug("<<==电子账户：[%s],电子账户类型：[%s],子账号：[%s]==>>", custac,
				accatp.getLongName(), knaAcct.getAcctno());
		return knaAcct.getAcctno();
	}
	
	public static void chkAcctStatus(String custac) {
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
	}
	
	/**
	 * 卡号及子账户状态校验
	 * @param cardno
	 */
	public static void chkCardno(String cardno) {
		IoCaKnaAcdc ioCaKnaAcdc = ActoacDao
				.selKnaAcdc(cardno, false);

		if (CommUtil.isNull(ioCaKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0750();
		}

		if (ioCaKnaAcdc.getStatus() == E_DPACST.CLOSE) {
			throw DpModuleError.DpstComm.BNAS0441();
		}
	}
}
