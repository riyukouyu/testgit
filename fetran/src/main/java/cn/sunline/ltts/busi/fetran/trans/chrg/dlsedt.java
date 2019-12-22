package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpScevDetlHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpScevDetlHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgScdfDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDetl;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDetlDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_MODULE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class dlsedt {
	private static final BizLog bizlog = BizLogUtil.getBizLog(dlsedt.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：删除场景代码明细管理
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void dlsedt(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlsedt.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlsedt.Property property) {
		String scencd = input.getScencd(); // 场景代码
		E_MODULE module = E_MODULE.CG; // 默认费用
		String evetcd = input.getEvetcd(); // 事件编号
		String dimecg = input.getDimecg(); // 维度类别
		String dimevl = input.getDimevl(); // 维度值

		if (CommUtil.isNull(scencd)) {
			throw FeError.Chrg.BNASF016();
		}

		if (CommUtil.isNull(evetcd)) {
			throw FeError.Chrg.BNASF217();
		}

		if (CommUtil.isNull(dimecg)) {
			throw FeError.Chrg.BNASF251();
		}

		if (CommUtil.isNull(dimevl)) {
			throw FeError.Chrg.BNASF259();
		}

		if (CommUtil.isNull(FeDimeDao.selone_evl_dime(dimevl, dimecg, false))) {
			throw FeError.Chrg.BNASF258();
		}

		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}

		// 场景代码存在绑定关系不允许删除
		if (CommUtil.isNotNull(KcpChrgScdfDao.selectAll_odb2(scencd, false))) {
			throw FeError.Chrg.BNASF172();
		}

		KcpScevDetl entity = SysUtil.getInstance(KcpScevDetl.class);
		entity = KcpScevDetlDao.selectOne_odb1(scencd, module, evetcd, dimecg,
				false);

		if (CommUtil.isNull(entity)) {
			throw FeError.Chrg.BNASF198();
		}

		KcpScevDetlDao.deleteOne_odb1(scencd, module, evetcd, dimecg);

		// 增加审计
		ApDataAudit.regLogOnDeleteParameter(entity);

		/**********************************************
		 * 维护历史表
		 **********************************************/
		// 实例化场景事件明细历史表
		KcpScevDetlHist tblKcpScevDetlHist = SysUtil.getInstance(KcpScevDetlHist.class);
		tblKcpScevDetlHist.setCorpno(entity.getCorpno());
		tblKcpScevDetlHist.setDimecg(entity.getDimecg());
		tblKcpScevDetlHist.setDimevl(entity.getDimevl());
		tblKcpScevDetlHist.setEvetcd(entity.getEvetcd());
		tblKcpScevDetlHist.setEvetus(entity.getEvetus());
		tblKcpScevDetlHist.setModule(entity.getModule());
		tblKcpScevDetlHist.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		tblKcpScevDetlHist.setRemark(entity.getRemark());
		tblKcpScevDetlHist.setScencd(entity.getScencd());
		tblKcpScevDetlHist.setScends(entity.getScends());
		// 新增历史表
		KcpScevDetlHistDao.insert(tblKcpScevDetlHist);
		
		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblKcpScevDetlHist);

	}
}
