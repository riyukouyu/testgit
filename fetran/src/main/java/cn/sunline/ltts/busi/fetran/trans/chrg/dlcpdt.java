package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoSmdlHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoSmdlHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdl;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdlDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class dlcpdt {
	private static final BizLog bizlog = BizLogUtil.getBizLog(dlcpdt.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：删除超额优惠明细
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */

	public static void dlcpdt(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlcpdt.Input input) {

		String smfacd = input.getSmfacd();// 超额优惠代码
		BigDecimal smstrt = input.getSmstrt();// 超额起点

		if (CommUtil.isNull(smfacd)) {
			throw FeError.Chrg.BNASF028();
		}

		if (CommUtil.isNull(smstrt)) {
			throw FeError.Chrg.BNASF023();
		}

		KcpFavoSmdl entity = SysUtil.getInstance(KcpFavoSmdl.class);
		entity = KcpFavoSmdlDao.selectOne_odb1(smfacd, smstrt, false);

		if (CommUtil.isNull(entity)) {
			throw FeError.Chrg.BNASF032();
		}

		if (DateUtil.compareDate(CommTools.getBaseRunEnvs().getTrxn_date(),
				entity.getEfctdt()) >= 0
				&& DateUtil.compareDate(CommTools.getBaseRunEnvs().getTrxn_date(),
						entity.getInefdt()) < 0) {
			throw FeError.Chrg.BNASF285();
		}

		// 删除
		KcpFavoSmdlDao.deleteOne_odb1(smfacd, smstrt);

		// 增加审计
		ApDataAudit.regLogOnDeleteParameter(entity);

		/**********************************************
		 * 维护历史表
		 **********************************************/
		// 实例化超额优惠解析历史表
		KcpFavoSmdlHist tblkcpsmdlhist = SysUtil.getInstance(KcpFavoSmdlHist.class);

		tblkcpsmdlhist.setCorpno(entity.getCorpno());
		tblkcpsmdlhist.setEfctdt(entity.getEfctdt());
		tblkcpsmdlhist.setExplan(entity.getExplan());
		tblkcpsmdlhist.setInefdt(entity.getInefdt());
		tblkcpsmdlhist.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		tblkcpsmdlhist.setSmblup(entity.getSmblup());
		tblkcpsmdlhist.setSmfacd(entity.getSmfacd());
		tblkcpsmdlhist.setSmfapc(entity.getSmfapc());
		tblkcpsmdlhist.setSmstrt(entity.getSmstrt());

		// 新增历史表
		KcpFavoSmdlHistDao.insert(tblkcpsmdlhist);

		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblkcpsmdlhist);

	}
}
