package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpChrgFmdtHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpChrgFmdtHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdt;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdtDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class dlcmdt {
	private static final BizLog bizlog = BizLogUtil.getBizLog(dlcmdt.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：计费公式明细表删除
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void dlcmdt(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlcmdt.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlcmdt.Property property) {
		bizlog.method("dlcmdt begin >>>>>>");
		String chrgfm = input.getChrgfm(); // 计费公式代码
		String brchno = input.getBrchno();// 机构号
		String crcycd = input.getCrcycd();// 货币代号
		BigDecimal limiam = input.getLimiam();// 金额区间下限

		if (CommUtil.isNull(chrgfm)) {
			throw FeError.Chrg.BNASF142();
		}
		if (CommUtil.isNull(brchno)) {
			throw FeError.Chrg.BNASF131();
		}
		if (CommUtil.isNull(crcycd)) {
			throw FeError.Chrg.BNASF125();
		}
		if (CommUtil.isNull(limiam)) {
			throw FeError.Chrg.BNASF171();
		}

		// 省县两级参数管理员均有操作权限，县级行社参数管理员只允许新增本行社
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), BusiTools.getBusiRunEnvs().getCentbr())
				&& !CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), brchno)) {
			throw FeError.Chrg.BNASF158();
		}

		KcpChrgFmdt entity = SysUtil.getInstance(KcpChrgFmdt.class);
		entity = KcpChrgFmdtDao.selectOne_odb1(chrgfm, brchno, crcycd, limiam,
				false);

		if (CommUtil.isNull(entity)) {
			throw FeError.Chrg.BNASF146();
		}

		if (CommUtil.isNotNull(entity)) {
			KcpChrgFmdtDao.deleteOne_odb1(chrgfm, brchno, crcycd, limiam);
			
			// 增加审计
			ApDataAudit.regLogOnDeleteParameter(entity);
		}

		// 插入维度类别管理历史表
		KcpChrgFmdtHist tblKcp_chrg_fmdt = SysUtil.getInstance(KcpChrgFmdtHist.class);
		CommUtil.copyProperties(tblKcp_chrg_fmdt, entity);
		tblKcp_chrg_fmdt.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		KcpChrgFmdtHistDao.insert(tblKcp_chrg_fmdt);

		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblKcp_chrg_fmdt);

	}
}
