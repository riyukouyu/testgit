package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdfDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdl;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdlDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mdcpdt {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mdcpdt.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：超额优惠明细修改
	 *         </p>
	 * @param
	 * @return void
	 * @throws
	 */
	public static void mdcpdt(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdcpdt.Input input) {

		bizlog.method("<<<<<< mdcpdt begin >>>>>>");

		String smfacd = input.getSmfacd();// 超额优惠代码
		BigDecimal smstrt = input.getSmstrt();// 超额起点
		String efctdt = input.getEfctdt(); // 生效日期
		String inefdt = input.getInefdt(); // 失效日期
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date(); // 交易日期
		BigDecimal smblup = input.getSmblup(); // 累积交易额限制

		if (CommUtil.isNull(smfacd)) {
			throw FeError.Chrg.BNASF028();
		}

		if (CommUtil.isNull(smstrt)) {
			throw FeError.Chrg.BNASF023();
		}

		if (CommUtil.isNull(efctdt)) {
			throw FeError.Chrg.BNASF207();
		}

		if (CommUtil.isNull(inefdt)) {
			throw FeError.Chrg.BNASF212();
		}

		if (CommUtil.isNull(input.getSmfapc())) {
			throw FeError.Chrg.BNASF025();
		}

		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}
		if (DateUtil.compareDate(inefdt, sTime) <= 0) {
			throw FeError.Chrg.BNASF209();
		}
		if (CommUtil.compare(input.getSmfapc(), BigDecimal.ZERO) < 0
				|| CommUtil.compare(input.getSmfapc(), BigDecimal.valueOf(100)) > 0) {
			throw FeError.Chrg.BNASF024();
		}

		KcpFavoSmdf kcpFavoSmdf = KcpFavoSmdfDao.selectOne_odb1(smfacd, false);
		if (CommUtil.isNull(kcpFavoSmdf)) {
			throw FeError.Chrg.BNASF027();
		}
		if (CommUtil.compare(smstrt, BigDecimal.ZERO) < 0) {
			throw FeError.Chrg.BNASF021();
		}
		if (CommUtil.compare(smblup, BigDecimal.ZERO) < 0) {
			throw FeError.Chrg.BNASF186();
		}

		KcpFavoSmdl tblFavosmdl = KcpFavoSmdlDao.selectOne_odb1(smfacd, smstrt,
				false);

		if (CommUtil.isNull(tblFavosmdl)) {
			throw FeError.Chrg.BNASF270();
		}

		// 已生效
		if (CommUtil.compare(sTime, tblFavosmdl.getEfctdt()) >= 0
				&& CommUtil.compare(sTime, tblFavosmdl.getInefdt()) < 0) {
			if (CommUtil.compare(input.getSmfapc(), tblFavosmdl.getSmfapc()) != 0
					|| CommUtil.compare(input.getSmblup(),
							tblFavosmdl.getSmblup()) != 0
					|| CommUtil.compare(input.getExplan(),
							tblFavosmdl.getExplan()) != 0
					|| CommUtil.compare(efctdt, tblFavosmdl.getEfctdt()) != 0) {
				throw FeError.Chrg.BNASF292();
			}
		} else if (CommUtil.compare(tblFavosmdl.getEfctdt(), sTime) > 0) { // 未生效
			if (DateUtil.compareDate(efctdt, sTime) <= 0) {
				throw FeError.Chrg.BNASF204();
			}
		}
		if (CommUtil.compare(tblFavosmdl.getInefdt(), sTime) < 0) {
			throw FeError.Chrg.BNASF290();
		}

		if (CommUtil.compare(input.getSmfapc(), tblFavosmdl.getSmfapc()) == 0
				&& CommUtil.compare(input.getSmblup(), tblFavosmdl.getSmblup()) == 0
				&& CommUtil.compare(input.getExplan(), tblFavosmdl.getExplan()) == 0
				&& CommUtil.compare(efctdt, tblFavosmdl.getEfctdt()) == 0
				&& CommUtil.compare(inefdt, tblFavosmdl.getInefdt()) == 0) {
			throw FeError.Chrg.BNASF317();
		}

		KcpFavoSmdl oldEntity = CommTools.clone(KcpFavoSmdl.class, tblFavosmdl);

		Long num = (long) 0;

		if (CommUtil.compare(input.getSmfapc(), tblFavosmdl.getSmfapc()) != 0) {
			num++;
			tblFavosmdl.setSmfapc(input.getSmfapc());
		}
		if (CommUtil.compare(input.getSmblup(), tblFavosmdl.getSmblup()) != 0) {
			num++;
			tblFavosmdl.setSmblup(input.getSmblup());
		}
		if (CommUtil.compare(input.getExplan(), tblFavosmdl.getExplan()) != 0) {
			num++;
			tblFavosmdl.setExplan(input.getExplan());
		}
		if (CommUtil.compare(efctdt, tblFavosmdl.getEfctdt()) != 0) {
			num++;
			tblFavosmdl.setEfctdt(efctdt);
		}
		if (CommUtil.compare(inefdt, tblFavosmdl.getInefdt()) != 0) {
			num++;
			tblFavosmdl.setInefdt(inefdt);
		}

		// 更新超额优惠明细
		KcpFavoSmdlDao.updateOne_odb1(tblFavosmdl);
		ApDataAudit.regLogOnUpdateParameter(oldEntity, tblFavosmdl);
	}
}
