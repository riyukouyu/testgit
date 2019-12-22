package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.namedsql.FeSceneDao;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpScevDimeHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpScevDimeHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPljo;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSpex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDetl;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDime;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDimeDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class dldiva {
	private static final BizLog bizlog = BizLogUtil.getBizLog(dldiva.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：删除维度值管理
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void dldiva(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dldiva.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dldiva.Property property) {

		bizlog.method("bizlog begin >>>>>>");

		// 判断当前机构是否为省中心机构
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}

		if (CommUtil.isNull(input.getDimecg())) {
			throw FeError.Chrg.BNASF251();
		}

		if (CommUtil.isNull(input.getDimevl())) {
			throw FeError.Chrg.BNASF259();
		}

		// List<kcp_scev_detl> tblKcpScevDetl = new ArrayList<kcp_scev_detl>();
		// List<kcp_favo_smex> tblKcpFavoSmex = new ArrayList<kcp_favo_smex>();

		// tblKcpScevDetl = Kcp_scev_detlDao.selectAll_odb6(input.getDimevl(),
		// false);
		// tblKcpFavoSmex = Kcp_favo_smexDao.selectAll_odb4(input.getDimevl(),
		// false);

		// mod 用namesql方式 不带法人查询
		List<KcpScevDetl> tblKcpScevDetl = FeSceneDao
				.selall_kcp_scev_detl_dimevl(input.getDimecg(),
						input.getDimevl(), false); // 查询场景代码明细
		List<KcpFavoSpex> tblKcpFavoSpex = FeDiscountDao
				.selall_kcp_favo_spex_dimevl(input.getDimecg(),
						input.getDimevl(), false); // 查询单一优惠
		List<KcpFavoSmex> tblKcpFavoSmex = FeDiscountDao
				.selall_kcp_favo_smex_dimevl(input.getDimecg(),
						input.getDimevl(), false); // 查询超额优惠解析
		List<KcpFavoPljo> tblKcpFavoPljo = FeDiscountDao
				.selall_kcp_favo_pljo_dimevl(input.getDimecg(),
						input.getDimevl(), false); // 查询优惠计划明细

		if ((CommUtil.isNotNull(tblKcpScevDetl) && tblKcpScevDetl.size() > 0)
				|| CommUtil.isNotNull(tblKcpFavoSmex)
				&& tblKcpFavoSmex.size() > 0
				|| CommUtil.isNotNull(tblKcpFavoSpex)
				&& tblKcpFavoSpex.size() > 0
				|| CommUtil.isNotNull(tblKcpFavoPljo)
				&& tblKcpFavoPljo.size() > 0) {
			throw FeError.Chrg.BNASF260();
		}

		KcpScevDime entity = SysUtil.getInstance(KcpScevDime.class);
		entity = KcpScevDimeDao.selectOne_odb1(input.getDimecg(),
				input.getDimevl(), false);

		if (CommUtil.isNull(entity)) {
			throw FeError.Chrg.BNASF266();
		}

		KcpScevDimeDao.deleteOne_odb1(entity.getDimecg(), entity.getDimevl());

		// 增加审计
		ApDataAudit.regLogOnDeleteParameter(entity);

		// 插入维度值历史明细表
		KcpScevDimeHist tblkcp_scev_dime_hist = SysUtil.getInstance(KcpScevDimeHist.class);
		CommUtil.copyProperties(tblkcp_scev_dime_hist, entity);
		tblkcp_scev_dime_hist.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		KcpScevDimeHistDao.insert(tblkcp_scev_dime_hist);

		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblkcp_scev_dime_hist);

	}
}
