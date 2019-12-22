package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoSpexHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoSpexHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSpex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSpexDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class dlspex {
	private static final BizLog bizlog = BizLogUtil.getBizLog(dlspex.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：单一优惠删除
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */

	public static void dlspex(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlspex.Input input) {
		String chrgcd = input.getChrgcd(); // 费种代码
		String brchno = input.getBrchno(); // 机构号
		//String crcycd = BusiTools.getBusiRunEnvs().getCrcycd(); // 默认人民币
		String crcycd = BusiTools.getBusiRunEnvs().getCrcycd(); // 默认人民币

		if (CommUtil.isNull(chrgcd)) {
			throw FeError.Chrg.BNASF076();
		}

		if (CommUtil.isNull(brchno)) {
			throw FeError.Chrg.BNASF131();
		}

		if (CommUtil.isNull(input.getFasttp())) {
			throw FeError.Chrg.BNASF042();
		}

		if (CommUtil.isNull(input.getFastam())) {
			throw FeError.Chrg.BNASF040();
		}

		if (CommUtil.isNull(input.getFavalu())) {
			throw FeError.Chrg.BNASF259();
		}

		if (CommUtil.isNull(input.getFatype())) {
			throw FeError.Chrg.BNASF255();
		}

		KcpFavoSpex entity = SysUtil.getInstance(KcpFavoSpex.class);
		entity = KcpFavoSpexDao.selectOne_odb1(chrgcd, brchno,
				input.getFasttp(), input.getFastam(), input.getFatype(),
				input.getFavalu(), false);

		if (CommUtil.isNull(entity)) {
			throw FeError.Chrg.BNASF038();
		}

		if (DateUtil.compareDate(CommTools.getBaseRunEnvs().getTrxn_date(),
				entity.getEfctdt()) >= 0
				&& DateUtil.compareDate(CommTools.getBaseRunEnvs().getTrxn_date(),
						entity.getInefdt()) < 0) {
			throw FeError.Chrg.BNASF286();
		}

		KcpFavoSpexDao.deleteOne_odb1(chrgcd, brchno, entity.getFasttp(),
				entity.getFastam(), entity.getFatype(), entity.getFavalu());

		// 增加审计
		ApDataAudit.regLogOnDeleteParameter(entity);

		KcpFavoSpexHist tblKcpFavoSpexHist = SysUtil.getInstance(KcpFavoSpexHist.class);
		tblKcpFavoSpexHist.setBrchno(entity.getBrchno());
		tblKcpFavoSpexHist.setChrgcd(entity.getChrgcd());
		tblKcpFavoSpexHist.setCorpno(entity.getCorpno());
		tblKcpFavoSpexHist.setCrcycd(entity.getCrcycd());
		tblKcpFavoSpexHist.setEfctdt(entity.getEfctdt());
		tblKcpFavoSpexHist.setFastam(entity.getFastam());
		tblKcpFavoSpexHist.setFasttp(entity.getFasttp());
		tblKcpFavoSpexHist.setFatype(entity.getFatype());
		tblKcpFavoSpexHist.setFavalu(entity.getFavalu());
		tblKcpFavoSpexHist.setFavoir(entity.getFavoir());
		tblKcpFavoSpexHist.setInefdt(entity.getInefdt());
		tblKcpFavoSpexHist.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		tblKcpFavoSpexHist.setSbbkcd(entity.getSbbkcd());
		// 新增历史表
		KcpFavoSpexHistDao.insert(tblKcpFavoSpexHist);

		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblKcpFavoSpexHist);

	}
}
