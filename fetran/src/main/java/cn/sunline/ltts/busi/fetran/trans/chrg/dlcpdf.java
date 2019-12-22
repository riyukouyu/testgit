package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoSmdfHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoSmdfHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdfDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class dlcpdf {
	private static final BizLog bizlog = BizLogUtil.getBizLog(dlcpdf.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：删除超额优惠定义
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */

	public static void dlcpdf(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlcpdf.Input input) {

		bizlog.method("dlcpdf begin >>>>>>");
		String smfacd = input.getSmfacd();

		if (CommUtil.isNull(input.getSmfacd())) {
			throw FeError.Chrg.BNASF028();
		}

		KcpFavoSmdf entity = KcpFavoSmdfDao.selectOne_odb1(smfacd, false);

		if (CommUtil.isNull(entity)) {
			throw FeError.Chrg.BNASF030();
		}

		// 判断是否存在绑定关系
		Long smexCount = FeDiscountDao.selcnt_kcp_favo_smex(null, null,
				BusiTools.getDefineCurrency(), smfacd, false);

		Long smdlCount = FeDiscountDao
				.selcnt_kcp_favo_smdl(smfacd, null, false);

		if ((smexCount + smdlCount) > 0) {
			throw FeError.Chrg.BNASF173();
		}

		if (DateUtil.compareDate(entity.getEfctdt(), CommTools.getBaseRunEnvs().getTrxn_date()) >= 0) { // 未生效

		} else {// 已生效且未失效
			if (DateUtil.compareDate(CommTools.getBaseRunEnvs().getTrxn_date(),
					entity.getInefdt()) < 0)
				throw FeError.Chrg.BNASF026();
		}

		// 删除
		KcpFavoSmdfDao.deleteOne_odb1(smfacd);
		// 增加审计
		ApDataAudit.regLogOnDeleteParameter(entity);

		/**********************************************
		 * 维护历史表
		 **********************************************/
		// 实例化超额优惠解析历史表
		KcpFavoSmdfHist tblkcpsmdfhist = SysUtil.getInstance(KcpFavoSmdfHist.class);

		tblkcpsmdfhist.setBrchno(entity.getBrchno());
		tblkcpsmdfhist.setCgsmtp(entity.getCgsmtp());
		tblkcpsmdfhist.setCorpno(entity.getCorpno());
		tblkcpsmdfhist.setEfctdt(entity.getEfctdt());
		tblkcpsmdfhist.setExplan(entity.getExplan());
		tblkcpsmdfhist.setInefdt(entity.getInefdt());
		tblkcpsmdfhist.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		tblkcpsmdfhist.setPdunit(entity.getPdunit());
		tblkcpsmdfhist.setSmbdtp(entity.getSmbdtp());
		tblkcpsmdfhist.setSmfacd(entity.getSmfacd());
		tblkcpsmdfhist.setSmfana(entity.getSmfana());

		// 新增历史表
		KcpFavoSmdfHistDao.insert(tblkcpsmdfhist);

		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblkcpsmdfhist);

	}

}
