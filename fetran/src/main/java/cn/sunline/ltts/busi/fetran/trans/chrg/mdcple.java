package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;
import java.util.Map;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeCodeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrg;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPldf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPldfDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPlex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPlexDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_FASTTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mdcple {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mdcple.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：优惠计划解析表修改
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void mdcple(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdcple.Input input) {

		bizlog.method("mdcple begin>>>>>>");
		String chrgcd = input.getChrgcd();// 费种代码
		String diplcd = input.getDiplcd();// 优惠计划代码
		E_FASTTP fasttp = input.getFasttp();// 优惠起点类型
		BigDecimal fastam = input.getFastam();// 优惠起点
		BigDecimal favoir = input.getFavoir();// 优惠比例
		String efctdt = input.getEfctdt(); // 生效日期
		String inefdt = input.getInefdt(); // 失效日期
		String explan = input.getExplan(); // 说明
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();// 交易机构
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date();// 当前日期

		if (CommUtil.isNull(fasttp)) {
			throw FeError.Chrg.BNASF313();
		}
		if (CommUtil.isNull(fastam)) {
			throw FeError.Chrg.BNASF312();
		}

		if (CommUtil.compare(fastam, BigDecimal.ZERO) < 0) {
			throw FeError.Chrg.BNASF314();
		}

		// 若优惠类型为按笔数，判断输入是否为整数
		if (fasttp == E_FASTTP.NUM) {
			String str = fastam.toString();
			for (int i = str.length(); --i >= 0;) {
				if (!Character.isDigit(str.charAt(i))) {
					if (!String.valueOf(str.charAt(i + 1)).equals("0"))
						throw FeError.Chrg.BNASF315();
				}
			}
		}
		if (CommUtil.isNull(favoir)) {
			throw FeError.Chrg.BNASF297();
		}
		if (CommUtil.compare(favoir, BigDecimal.ZERO) <= 0
				|| CommUtil.compare(favoir, BigDecimal.valueOf(100)) > 0) {
			throw FeError.Chrg.BNASF296();
		}
		if (CommUtil.isNull(efctdt)) {
			throw FeError.Chrg.BNASF207();
		}
		if (CommUtil.isNull(inefdt)) {
			throw FeError.Chrg.BNASF212();
		}
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}
		if (DateUtil.compareDate(inefdt, sTime) <= 0) {
			throw FeError.Chrg.BNASF209();
		}

		KcpFavoPlex tblFavoplex = KcpFavoPlexDao.selectOne_odb1(chrgcd, diplcd,
				false);

		if (CommUtil.isNull(tblFavoplex)) {
			throw FeError.Chrg.BNASF152();
		}

		// 已生效
		if (CommUtil.compare(sTime, tblFavoplex.getEfctdt()) >= 0
				&& CommUtil.compare(sTime, tblFavoplex.getInefdt()) < 0) {
			if (CommUtil.compare(fasttp, tblFavoplex.getFasttp()) != 0
					|| CommUtil.compare(fastam, tblFavoplex.getFastam()) != 0
					|| CommUtil.compare(favoir, tblFavoplex.getFavoir()) != 0
					|| CommUtil.compare(efctdt, tblFavoplex.getEfctdt()) != 0
					|| CommUtil.compare(explan, tblFavoplex.getExplan()) != 0) {
				throw FeError.Chrg.BNASF101();
			}
		} else if (CommUtil.compare(tblFavoplex.getEfctdt(), sTime) > 0) { // 未生效
			if (DateUtil.compareDate(efctdt, sTime) <= 0) {
				throw FeError.Chrg.BNASF204();
			}
		}

		if (DateUtil.compareDate(CommTools.getBaseRunEnvs().getTrxn_date(),
				tblFavoplex.getInefdt()) > 0) { // 已失效
			throw FeError.Chrg.BNASF302();
		}

		// 与原记录相同，不允许更新操作
		if (CommUtil.compare(fasttp, tblFavoplex.getFasttp()) == 0
				&& CommUtil.compare(fastam, tblFavoplex.getFastam()) == 0
				&& CommUtil.compare(favoir, tblFavoplex.getFavoir()) == 0
				&& CommUtil.compare(efctdt, tblFavoplex.getEfctdt()) == 0
				&& CommUtil.compare(inefdt, tblFavoplex.getInefdt()) == 0
				&& CommUtil.compare(explan, tblFavoplex.getExplan()) == 0) {
			throw FeError.Chrg.BNASF317();
		}

		// 20170406 mod songlw 查询去掉法人代码
		KcpChrg kcpChrg = FeCodeDao.selone_kcp_chrg(chrgcd, false);
		if (CommUtil.isNull(kcpChrg)) {
			throw FeError.Chrg.BNASF076();
		}
		KcpFavoPldf tblkcp_favo_pldf = KcpFavoPldfDao.selectOne_odb2(diplcd,
				false);
		if (CommUtil.isNull(tblkcp_favo_pldf)) {
			throw FeError.Chrg.BNASF306();
		}

		// 判断与优惠计划失效日期比较
		KcpFavoPldf kcpFavoPldf = KcpFavoPldfDao.selectOne_odb2(diplcd, false);
		// 20161108 mod 生效日期与定义的失效日期进行比较
		if (DateUtil.compareDate(efctdt, kcpFavoPldf.getInefdt()) > 0) {
			throw FeError.Chrg.BNASF206();
		}

		if (CommUtil.isNotNull(tblFavoplex)) {

			// 已生效的优惠计划只能修改失效日期
			if (DateUtil.compareDate(tblFavoplex.getEfctdt(), sTime) <= 0) {
				if (CommUtil.compare(fasttp.toString(), tblFavoplex.getFasttp()
						.toString()) != 0
						|| CommUtil.compare(fastam, tblFavoplex.getFastam()) != 0
						|| CommUtil.compare(favoir, tblFavoplex.getFavoir()) != 0
						|| CommUtil.compare(efctdt, tblFavoplex.getEfctdt()) != 0
						|| CommUtil.compare(explan, tblFavoplex.getExplan()) != 0) {
					throw FeError.Chrg.BNASF107();
				}
			}

			// 判断是否为省级柜员
			Map<String, Object> map = FeDiscountDao.selKubBrchLevel(tranbr,
					false);
			String brchlv = map.get("brchlv").toString();
			if (!brchlv.equals("1")) {
				if (!CommUtil.equals(tranbr, tblFavoplex.getBrchno())) {
					throw FeError.Chrg.BNASF058();
				}
			}

			KcpFavoPlex oldEntity = CommTools.clone(KcpFavoPlex.class,
					tblFavoplex);

			// 明细登记簿维护
			Long num = (long) 0;
			if (CommUtil.compare(fasttp.toString(), tblFavoplex.getFasttp()
					.toString()) != 0) {
				num++;
				tblFavoplex.setFasttp(fasttp);
			}
			if (CommUtil.compare(input.getFastam(), tblFavoplex.getFastam()) != 0) {
				num++;
				tblFavoplex.setFastam(fastam);
			}
			if (CommUtil.compare(favoir.toString(), tblFavoplex.getFavoir()
					.toString()) != 0) {
				num++;
				tblFavoplex.setFavoir(favoir);
			}
			if (CommUtil.compare(explan, tblFavoplex.getExplan()) != 0) {
				num++;
				tblFavoplex.setExplan(explan);
			}
			if (CommUtil.compare(efctdt, tblFavoplex.getEfctdt()) != 0) {
				num++;
				tblFavoplex.setEfctdt(efctdt);
			}
			if (CommUtil.compare(inefdt, tblFavoplex.getInefdt()) != 0) {
				num++;
				tblFavoplex.setInefdt(inefdt);
			}

			// 修改优惠计划解析表
			KcpFavoPlexDao.updateOne_odb1(tblFavoplex);
			ApDataAudit.regLogOnUpdateParameter(oldEntity, tblFavoplex);
		}
	}
}
