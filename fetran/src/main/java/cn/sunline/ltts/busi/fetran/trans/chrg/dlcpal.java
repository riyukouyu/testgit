package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoSmexHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoSmexHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmexDao;
import cn.sunline.ltts.busi.sys.errors.FeError;

public class dlcpal {
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：删除超额优惠解析
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */

	public static void dlcpal(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlcpal.Input input) {
		String chrgcd = input.getChrgcd();// 费种代码
		String smfacd = input.getSmfacd();// 超额优惠代码

		if (CommUtil.isNull(input.getChrgcd())) {
			throw FeError.Chrg.BNASF076();
		}
		if (CommUtil.isNull(input.getSmfacd())) {
			throw FeError.Chrg.BNASF028();
		}
		if (CommUtil.isNull(input.getFadmtp())) {
			throw FeError.Chrg.BNASF251();
		}
		KcpFavoSmex entity = KcpFavoSmexDao
				.selectOne_odb1(chrgcd, input.getFadmtp(),
						BusiTools.getDefineCurrency(), smfacd, false);

		if (CommUtil.isNull(entity)) {
			throw FeError.Chrg.BNASF031();
		}

		if (DateUtil.compareDate(CommTools.getBaseRunEnvs().getTrxn_date(),entity.getEfctdt()) >= 0
			&& DateUtil.compareDate(CommTools.getBaseRunEnvs().getTrxn_date(),entity.getInefdt()) < 0) {
			throw FeError.Chrg.BNASF285();
		}

		// 删除
		KcpFavoSmexDao.deleteOne_odb1(chrgcd, input.getFadmtp(),
				BusiTools.getDefineCurrency(), smfacd);
		
		// 增加审计
		ApDataAudit.regLogOnDeleteParameter(entity);

		/**********************************************
		 * 维护历史表
		 **********************************************/
		// 实例化超额优惠解析历史表
		KcpFavoSmexHist tblkcpsmexhist = SysUtil.getInstance(KcpFavoSmexHist.class);
		tblkcpsmexhist.setChrgcd(entity.getChrgcd());
		tblkcpsmexhist.setCorpno(entity.getCorpno());
		tblkcpsmexhist.setCrcycd(entity.getCrcycd());
		tblkcpsmexhist.setDimevl(entity.getDimevl());
		tblkcpsmexhist.setEfctdt(entity.getEfctdt());
		tblkcpsmexhist.setFadmtp(entity.getFadmtp());
		tblkcpsmexhist.setInefdt(entity.getInefdt());
		tblkcpsmexhist.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		tblkcpsmexhist.setSmfacd(entity.getSmfacd());
		// 新增历史表
		KcpFavoSmexHistDao.insert(tblkcpsmexhist);

		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblkcpsmexhist);

	}
}
