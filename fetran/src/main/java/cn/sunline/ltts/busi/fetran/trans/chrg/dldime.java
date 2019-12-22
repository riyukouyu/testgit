package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpDimeHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpDimeHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpDime;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpDimeDao;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgDiva;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_WAYTYP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class dldime {
	private static final BizLog bizlog = BizLogUtil.getBizLog(dldime.class);

	/**
	 * 
	 * @Title: dldime
	 * @Description: 维度信息参数删除
	 * @param input
	 * @param property
	 * @author songliangwei
	 * @date 2016年7月8日 下午3:45:31
	 * @version V2.3.0
	 */
	public static void dldime(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dldime.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dldime.Property property) {
		bizlog.method("dldime begin >>>>>>");
		// 判断当前机构是否为省中心机构
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}

		// 维度类型
		E_WAYTYP waytyp = input.getWaytyp();
		// 维度类别
		String dimecg = input.getDimecg();

		if (CommUtil.isNull(waytyp)) {
			throw FeError.Chrg.BNASF255();
		}
		if (CommUtil.isNull(dimecg)) {
			throw FeError.Chrg.BNASF251();
		}

		KcpDime entity = SysUtil.getInstance(KcpDime.class);
		entity = KcpDimeDao.selectOne_odb1(waytyp, dimecg, true);

		if (CommUtil.isNull(entity)) {
			throw FeError.Chrg.BNASF257();
		}

		List<IoCgDiva> cgDiva = FeDimeDao.sel_kcp_scev_dime(dimecg, null, null,
				false);
		if (cgDiva.size() > 0) {
			throw FeError.Chrg.BNASF178();
		}

		// 删除记录
		KcpDimeDao.deleteOne_odb1(waytyp, dimecg);

		// 增加审计
		ApDataAudit.regLogOnDeleteParameter(entity);

		// 插入维度类别管理历史表
		KcpDimeHist tblKcp_dime_hist = SysUtil.getInstance(KcpDimeHist.class);
		CommUtil.copyProperties(tblKcp_dime_hist, entity);
		tblKcp_dime_hist.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		KcpDimeHistDao.insert(tblKcp_dime_hist);

		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblKcp_dime_hist);

	}
}
