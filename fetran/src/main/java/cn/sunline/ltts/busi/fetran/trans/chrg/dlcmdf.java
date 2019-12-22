package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeFormulaDao;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpChrgFmdfHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpChrgFmdfHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdfDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class dlcmdf {
	private static final BizLog bizlog = BizLogUtil.getBizLog(dlcmdf.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：计费公式定义表删除
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void dlcmdf(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlcmdf.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlcmdf.Property property) {

		bizlog.method("dlcmdf begin >>>>>>");

		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}

		String chrgfm = input.getChrgfm(); // 计费公式代码
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); // 交易日期

		if (CommUtil.isNull(chrgfm)) {
			throw FeError.Chrg.BNASF142();
		}

		// 根据计费公式查找计费公式明细
		Long fmdtCount = FeFormulaDao.selcnt_kcp_chrg_fmdt(chrgfm, null, null,
				null, null, false);

		if (fmdtCount > 0) {
			throw FeError.Chrg.BNASF140();
		}

		KcpChrgFmdf entity = SysUtil.getInstance(KcpChrgFmdf.class);
		entity = KcpChrgFmdfDao.selectOne_odb1(chrgfm, false);
		
		if(CommUtil.isNull(entity)){
			throw FeError.Chrg.BNASF055();
		}
		// 已生效计费公式允许删除
		if (CommUtil.compare(trandt, entity.getEfctdt()) >= 0
				&& CommUtil.compare(trandt, entity.getInefdt()) <= 0) {
			throw FeError.Chrg.BNASF139();
		}

		// 未生效存在绑定关系不允许删除
		Long fmexCount = FeFormulaDao.selcnt_kcp_chrg_fmex(null, null, null,
				chrgfm, false);
		if (CommUtil.compare(trandt, entity.getEfctdt()) < 0 && fmexCount > 0) {
			throw FeError.Chrg.BNASF176();
		}

		if (CommUtil.isNull(entity)) {
			throw FeError.Chrg.BNASF143();
		}

		if (CommUtil.isNotNull(entity)) {
			KcpChrgFmdfDao.deleteOne_odb1(chrgfm);
			
			// 增加审计
			ApDataAudit.regLogOnDeleteParameter(entity);
		}

		// 插入维度类别管理历史表
		KcpChrgFmdfHist tblKcp_chrg_fmdf = SysUtil.getInstance(KcpChrgFmdfHist.class);
		CommUtil.copyProperties(tblKcp_chrg_fmdf, entity);
		tblKcp_chrg_fmdf.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		KcpChrgFmdfHistDao.insert(tblKcp_chrg_fmdf);

		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblKcp_chrg_fmdf);

	}
}
