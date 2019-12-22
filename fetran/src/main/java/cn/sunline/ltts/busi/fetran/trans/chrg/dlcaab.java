package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpChrgSubjHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpChrgSubjHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgSubj;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgSubjDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class dlcaab {
	private static final BizLog bizlog = BizLogUtil.getBizLog(dlcaab.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：删除费种代码核算属性
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */

	public static void dlcaab(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlcaab.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlcaab.Property property) {
		bizlog.method("dlcaab begin >>>>>>");

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

		KcpChrgSubj entity = SysUtil.getInstance(KcpChrgSubj.class);
		entity = KcpChrgSubjDao.selectOne_odb1(chrgcd, scencd, false);

		if (CommUtil.isNull(entity)) {
			throw FeError.Chrg.BNASF077();
		}

		if (CommUtil.isNotNull(entity)) {
			KcpChrgSubjDao.deleteOne_odb1(chrgcd, scencd);
			// 增加审计
			ApDataAudit.regLogOnDeleteParameter(entity);
		}

		// 插入维度类别管理历史表
		KcpChrgSubjHist tblKcp_chrg_subj = SysUtil.getInstance(KcpChrgSubjHist.class);
		CommUtil.copyProperties(tblKcp_chrg_subj, entity);
		tblKcp_chrg_subj.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		KcpChrgSubjHistDao.insert(tblKcp_chrg_subj);

		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblKcp_chrg_subj);

	}

}
