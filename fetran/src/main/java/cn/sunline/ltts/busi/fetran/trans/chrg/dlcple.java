package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.util.Map;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoPlexHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoPlexHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPlex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPlexDao;
import cn.sunline.ltts.busi.sys.errors.FeError;

public class dlcple {
	public static void dlcple(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlcple.Input input) {

		String chrgcd = input.getChrgcd();// 费种代码
		String diplcd = input.getDiplcd();// 优惠计划代码
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();// 交易机构

		if (CommUtil.isNull(chrgcd)) {
			throw FeError.Chrg.BNASF076();
		}
		if (CommUtil.isNull(diplcd)) {
			throw FeError.Chrg.BNASF303();
		}

		// 获取机构等级
		Map<String, Object> map = FeDiscountDao.selKubBrchLevel(tranbr, false);
		String brchlv = map.get("brchlv").toString();// 机构等级

		KcpFavoPlex tblFavoplex = KcpFavoPlexDao.selectOne_odb1(chrgcd, diplcd,
				false);// 优惠计划解析表

		if (CommUtil.isNull(tblFavoplex)) {
			throw FeError.Chrg.BNASF153();
		}
		// 不是省级行社的只能操作本行社优惠计划信息
		if (!brchlv.equals("1")) {
			if (!CommUtil.equals(tranbr, tblFavoplex.getBrchno())) {
				throw FeError.Chrg.BNASF056();
			}
		}

		if (DateUtil.compareDate(CommTools.getBaseRunEnvs().getTrxn_date(),
				tblFavoplex.getEfctdt()) >= 0
				&& DateUtil.compareDate(CommTools.getBaseRunEnvs().getTrxn_date(),
						tblFavoplex.getInefdt()) < 0) {
			throw FeError.Chrg.BNASF288();
		}

		KcpFavoPlexDao.deleteOne_odb1(chrgcd, diplcd);// 删除优惠计划解析

		// 增加审计
		ApDataAudit.regLogOnDeleteParameter(tblFavoplex);

		// 插入优惠计划解析历史表
		KcpFavoPlexHist tblkcp_favo_plex_hist = SysUtil.getInstance(KcpFavoPlexHist.class);
		CommUtil.copyProperties(tblkcp_favo_plex_hist, tblFavoplex);
		tblkcp_favo_plex_hist.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		KcpFavoPlexHistDao.insert(tblkcp_favo_plex_hist);

		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblkcp_favo_plex_hist);

	}
}
