package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgSubj;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgSubjDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mdcaab {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mdcaab.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：修改费种代码核算属性
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */

	public static void mdcaab(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdcaab.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdcaab.Property property) {

		bizlog.method("<<<<<< mdcaab begin >>>>>>");

		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}

		String chrgcd = input.getChrgcd(); // 费种代码
		String scencd = input.getScencd();// 场景代码

		if (CommUtil.isNull(chrgcd)) {
			throw FeError.Chrg.BNASF076();
		}

		if (CommUtil.isNull(scencd)) {
			throw FeError.Chrg.BNASF016();
		}

		if (CommUtil.isNotNull(input.getPrmark())) {
			if (CommUtil.equals(input.getPrmark(), "1")
					&& CommUtil.isNull(input.getPronum())) { // 当对应产品标志选择1-归属产品时，产品编号必输，若与场景代码无关，填写%
				throw FeError.Chrg.BNASF010();
			}
		}

		if ((CommUtil.isNotNull(input.getPrmark()) || CommUtil.isNotNull(input
				.getTrinfo()))
				&& (CommUtil.isNull(input.getPrmark()) || CommUtil.isNull(input
						.getTrinfo()))) { // 网络核算属性必须同时维护
			throw FeError.Chrg.BNASF249();
		}

		if ((CommUtil.isNotNull(input.getAcclev())
				|| CommUtil.isNotNull(input.getSubnum()) || CommUtil
					.isNotNull(input.getIntacc()))
				&& (CommUtil.isNull(input.getAcclev())
						|| CommUtil.isNull(input.getSubnum()) || CommUtil
							.isNull(input.getIntacc()))) { // 柜面核算属性必须同时维护
			throw FeError.Chrg.BNASF123();
		}

		KcpChrgSubj tblKcpChrgSubj = KcpChrgSubjDao.selectOne_odb1(chrgcd,
				scencd, false);

		if (CommUtil.isNotNull(tblKcpChrgSubj)) {
			if (CommUtil.compare(input.getIntacc(), tblKcpChrgSubj.getIntacc()) == 0
					&& CommUtil.compare(input.getPrmark(),
							tblKcpChrgSubj.getPrmark()) == 0
					&& CommUtil.compare(input.getPronum(),
							tblKcpChrgSubj.getPronum()) == 0
					&& CommUtil.compare(input.getSubnum(),
							tblKcpChrgSubj.getSubnum()) == 0
					&& CommUtil.compare(input.getTrinfo(),
							tblKcpChrgSubj.getTrinfo()) == 0
					&& input.getAcclev() == tblKcpChrgSubj.getAcclev()) {
				throw FeError.Chrg.BNASF317();
			}

			KcpChrgSubj oldEntity = CommTools.clone(KcpChrgSubj.class,
					tblKcpChrgSubj);
			// 明细登记簿维护
			Long num = (long) 0; // 序列
			if (CommUtil.compareIgnoreCase(input.getSubnum(),
					tblKcpChrgSubj.getSubnum()) != 0) {// 科目号
				num++;

				tblKcpChrgSubj.setSubnum(input.getSubnum());
			}

			if (CommUtil.compare(input.getIntacc(), tblKcpChrgSubj.getIntacc()) != 0) {// 内部帐对应顺序号
				num++;

				tblKcpChrgSubj.setIntacc(input.getIntacc());
			}

			if (CommUtil.compare(input.getPronum(), tblKcpChrgSubj.getPronum()) != 0) {// 产品编号
				num++;

				tblKcpChrgSubj.setPronum(input.getPronum());
			}

			if (CommUtil.compare(input.getTrinfo(), tblKcpChrgSubj.getTrinfo()) != 0) {// 交易信息
				num++;

				tblKcpChrgSubj.setTrinfo(input.getTrinfo());
			}

			if (input.getAcclev() != tblKcpChrgSubj.getAcclev()) {
				num++;

				tblKcpChrgSubj.setAcclev(input.getAcclev());
			}
			KcpChrgSubjDao.updateOne_odb1(tblKcpChrgSubj);

			ApDataAudit.regLogOnUpdateParameter(oldEntity, tblKcpChrgSubj);

		} else {
			throw FeError.Chrg.BNASF152();
		}

	}
}
