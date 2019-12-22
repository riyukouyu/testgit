package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoPljoHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoPljoHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPljo;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPljoDao;
import cn.sunline.ltts.busi.sys.errors.FeError;

public class dlcplj {

	/*
	 * 删除优惠计划明细
	 */
	public static void dlcplj(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlcplj.Input input) {
		String diplcd = input.getDiplcd(); // 优惠计划代码
		String seqnum = input.getSeqnum(); // 顺序号

		if (CommUtil.isNull(diplcd)) {
			throw FeError.Chrg.BNASF303();
		}

		if (CommUtil.isNull(seqnum)) {
			throw FeError.Chrg.BNASF240();
		}

		KcpFavoPljo tblKcpFavoPljo = KcpFavoPljoDao.selectOne_odb1(diplcd,
				seqnum, false);
		if (CommUtil.isNull(tblKcpFavoPljo)) {
			throw FeError.Chrg.BNASF301();
		}

		// 删除数据
		KcpFavoPljoDao.deleteOne_odb1(diplcd, seqnum);

		// 增加审计
		ApDataAudit.regLogOnDeleteParameter(tblKcpFavoPljo);

		// 插入历史表
		KcpFavoPljoHist tblkcp_favo_pljo_hist = SysUtil.getInstance(KcpFavoPljoHist.class);
		CommUtil.copyProperties(tblkcp_favo_pljo_hist, tblKcpFavoPljo);
		tblkcp_favo_pljo_hist.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		KcpFavoPljoHistDao.insert(tblkcp_favo_pljo_hist);

		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblkcp_favo_pljo_hist);

	}
}
