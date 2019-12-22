package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeShareDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgDvid;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgDvidDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mdcrsp {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mdcrsp.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：修改分润管理
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void mdcrsp(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdcrsp.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdcrsp.Property property) {

		bizlog.method("mdcrsp begin >>>>>>");

		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}

		String chrgcd = input.getChrgcd(); // 费种代码
		String crcycd = input.getCrcycd(); // 币种
		BigDecimal indvrt = input.getIndvrt();// 转入行分成比率
		BigDecimal oudvrt = input.getOudvrt();// 转出行分成比率
		BigDecimal trdvrt = input.getTrdvrt();// 交易行分成比率
		BigDecimal spavrt = input.getSpavrt();// 备用行分成比率
		String efctdt = input.getEfctdt(); // 生效日期
		String inefdt = input.getInefdt(); // 失效日期
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 日期

		if (CommUtil.isNull(indvrt)) {
			throw FeError.Chrg.BNASF341();
		}

		if (CommUtil.isNull(oudvrt)) {
			throw FeError.Chrg.BNASF340();
		}

		if (CommUtil.isNull(oudvrt)) {
			throw FeError.Chrg.BNASF157();
		}

		if (CommUtil.isNull(spavrt)) {
			throw FeError.Chrg.BNASF002();
		}

		if (CommUtil.compare(indvrt, BigDecimal.ZERO) < 0) {
			throw FeError.Chrg.BNASF222();
		}

		if (CommUtil.compare(oudvrt, BigDecimal.ZERO) < 0) {
			throw FeError.Chrg.BNASF090();
		}

		if (CommUtil.compare(trdvrt, BigDecimal.ZERO) < 0) {
			throw FeError.Chrg.BNASF354();
		}

		if (CommUtil.compare(spavrt, BigDecimal.ZERO) < 0) {
			throw FeError.Chrg.BNASF355();
		}

		Long num = (long) 0;
		BigDecimal sumvrt = indvrt.add(spavrt).add(oudvrt).add(trdvrt);

		if (!CommUtil.equals(sumvrt, BigDecimal.valueOf(100))) {
			throw FeError.Chrg.BNASF089();
		}

		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}

		if (DateUtil.compareDate(inefdt, trandt) <= 0) {
			throw FeError.Chrg.BNASF209();
		}

		List<KcpChrgDvid> tblKcpchrgdvid = KcpChrgDvidDao.selectAll_odb4(
				chrgcd, crcycd, false);

		if (CommUtil.isNull(tblKcpchrgdvid)) {
			throw FeError.Chrg.BNASF279();
		}

		for (KcpChrgDvid chrgdvid : tblKcpchrgdvid) {

			// 已生效
			if (CommUtil.compare(trandt, chrgdvid.getEfctdt()) >= 0
					&& CommUtil.compare(trandt, chrgdvid.getInefdt()) < 0) {
				if (CommUtil.compare(chrgdvid.getOudvrt(), input.getOudvrt()) != 0
						|| CommUtil.compare(chrgdvid.getIndvrt(),
								input.getIndvrt()) != 0
						|| CommUtil.compare(chrgdvid.getTrdvrt(),
								input.getTrdvrt()) != 0
						|| CommUtil.compare(chrgdvid.getSpavrt(),
								input.getSpavrt()) != 0
						|| CommUtil.compare(chrgdvid.getEfctdt(),
								input.getEfctdt()) != 0) {
					throw FeError.Chrg.BNASF292();
				}
			} else if (CommUtil.compare(chrgdvid.getEfctdt(), trandt) > 0) { // 未生效
				if (DateUtil.compareDate(efctdt, trandt) <= 0) {
					throw FeError.Chrg.BNASF204();
				}
			}

			// 记录失效
			if (CommUtil.compare(chrgdvid.getInefdt(), trandt) < 0) {

				throw FeError.Chrg.BNASF208();
			}

			if (CommUtil.compare(chrgdvid.getOudvrt(), input.getOudvrt()) == 0
					&& CommUtil
							.compare(chrgdvid.getIndvrt(), input.getIndvrt()) == 0
					&& CommUtil
							.compare(chrgdvid.getTrdvrt(), input.getTrdvrt()) == 0
					&& CommUtil
							.compare(chrgdvid.getSpavrt(), input.getSpavrt()) == 0
					&& CommUtil
							.compare(chrgdvid.getEfctdt(), input.getEfctdt()) == 0
					&& CommUtil
							.compare(chrgdvid.getInefdt(), input.getInefdt()) == 0) {
				throw FeError.Chrg.BNASF317();
			}

			KcpChrgDvid oldEntity = CommTools
					.clone(KcpChrgDvid.class, chrgdvid);

			// 如修改的记录未生效
			if (CommUtil.compare(chrgdvid.getEfctdt(), trandt) > 0) { // 记录未生效

				// 登记维护登记簿
				num++;
				// 当记录未生效时币种、费率代码不可修改

				chrgdvid.setIndvrt(input.getIndvrt());
				chrgdvid.setOudvrt(input.getOudvrt());
				chrgdvid.setSpavrt(input.getSpavrt());
				chrgdvid.setTrdvrt(input.getTrdvrt());
				chrgdvid.setEfctdt(input.getEfctdt());
				chrgdvid.setInefdt(input.getInefdt());

				FeShareDao.upall_kcp_chrg_dvid(chrgcd, crcycd, inefdt, indvrt,
						oudvrt, trdvrt, spavrt, efctdt, trandt);
			} else if (CommUtil.compare(chrgdvid.getEfctdt(), trandt) <= 0
					&& CommUtil.compare(chrgdvid.getInefdt(), trandt) >= 0) {// 记录生效

				// 当记录生效时只能修改失效日期
				if (CommUtil.compare(chrgdvid.getIndvrt(), input.getIndvrt()) != 0) {
					throw FeError.Chrg.BNASF200();
				}
				if (CommUtil.compare(chrgdvid.getOudvrt(), input.getOudvrt()) != 0) {
					throw FeError.Chrg.BNASF200();
				}
				if (CommUtil.compare(chrgdvid.getSpavrt(), input.getSpavrt()) != 0) {
					throw FeError.Chrg.BNASF200();
				}
				if (CommUtil.compare(chrgdvid.getTrdvrt(), input.getTrdvrt()) != 0) {
					throw FeError.Chrg.BNASF200();
				}
				if (!CommUtil.equals(chrgdvid.getEfctdt(), input.getEfctdt())) {
					throw FeError.Chrg.BNASF203();
				}
				if (!CommUtil.equals(chrgdvid.getChrgcd(), input.getChrgcd())) {
					throw FeError.Chrg.BNASF201();
				}
				if (!CommUtil.equals(chrgdvid.getCrcycd().toString(), input
						.getCrcycd().toString())) {
					throw FeError.Chrg.BNASF202();
				}

				// 修改到期日
				chrgdvid.setInefdt(input.getInefdt());
				FeShareDao.upall_kcp_chrg_dvid(chrgcd, crcycd, inefdt, indvrt,
						oudvrt, trdvrt, spavrt, efctdt, trandt);

				// 登记维护登记簿
				num++;
			}

			ApDataAudit.regLogOnUpdateParameter(oldEntity, chrgdvid);

		}
	}
}
