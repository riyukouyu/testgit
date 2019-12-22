package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpChrgDvidHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpChrgDvidHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgDvid;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgDvidDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class dlcrsp {
	private static final BizLog bizlog = BizLogUtil.getBizLog(adcrsp.class);

	public static void dlcrsp(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlcrsp.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlcrsp.Property property) {

		bizlog.method("dlcrsp begin >>>>>>");

		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}

		String chrgcd = input.getChrgcd(); // 费种代码
		String crcycd = input.getCrcycd(); // 币种
		String efctdt = input.getEfctdt(); // 生效日期
		
		if (CommUtil.isNull(efctdt)) {
			throw FeError.Chrg.BNASF207();
		}

		if (CommUtil.isNull(crcycd)) {
			throw FeError.Chrg.BNASF156();
		}

		if (CommUtil.isNull(chrgcd)) {
			throw FeError.Chrg.BNASF076();
		}
		
		KcpChrgDvid dvid = KcpChrgDvidDao.selectOne_odb1(chrgcd, crcycd,
				efctdt, false);
		
		if (CommUtil.isNull(dvid)) {
			throw FeError.Chrg.BNASF280();
		}
		if (DateUtil.compareDate(CommTools.getBaseRunEnvs().getTrxn_date(),dvid.getEfctdt()) >= 0
				&& DateUtil.compareDate(CommTools.getBaseRunEnvs().getTrxn_date(),dvid.getInefdt()) < 0) {
				throw FeError.Chrg.BNASF287();
			}

		// 删除表数据
		KcpChrgDvidDao.deleteOne_odb1(chrgcd, crcycd, efctdt);

		// 增加审计
		ApDataAudit.regLogOnDeleteParameter(dvid);

		// 插入收费分润历史表
		KcpChrgDvidHist tblkcp_chrg_dvid_hist = SysUtil.getInstance(KcpChrgDvidHist.class);
		CommUtil.copyProperties(tblkcp_chrg_dvid_hist, dvid);
		tblkcp_chrg_dvid_hist.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		KcpChrgDvidHistDao.insert(tblkcp_chrg_dvid_hist);

		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblkcp_chrg_dvid_hist);

	}
}
