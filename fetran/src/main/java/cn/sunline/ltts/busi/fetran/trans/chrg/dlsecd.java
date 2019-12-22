package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpChrgScdfHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpChrgScdfHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgScdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgScdfDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_MODULE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class dlsecd {
	private static final BizLog bizlog = BizLogUtil.getBizLog(dlsecd.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：删除场景计费管理
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */

	public static void dlsecd(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlsecd.Input input) {
		String chrgcd = input.getChrgcd();// 费种代码
		String scencd = input.getScencd();// 场景代码
		E_MODULE module = E_MODULE.CG;// 默认费用

		KcpChrgScdf eneity = KcpChrgScdfDao.selectOne_odb3(scencd, chrgcd,
				false);

		if (CommUtil.isNull(eneity)) {
			throw FeError.Chrg.BNASF198();
		}

		KcpChrgScdfDao.deleteOne_odb3(scencd, chrgcd);

		// 增加审计
		ApDataAudit.regLogOnDeleteParameter(eneity);

		/**********************************************
		 * 维护历史表
		 **********************************************/
		// 实例化场景计费定义历史表
		KcpChrgScdfHist tblkChrgScdfHist = SysUtil.getInstance(KcpChrgScdfHist.class);
		tblkChrgScdfHist.setSeqnum(eneity.getSeqnum());
		tblkChrgScdfHist.setChrgcd(eneity.getChrgcd());
		tblkChrgScdfHist.setModule(eneity.getModule());
		tblkChrgScdfHist.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		tblkChrgScdfHist.setRemark(eneity.getRemark());
		tblkChrgScdfHist.setCorpno(eneity.getCorpno());
		tblkChrgScdfHist.setScencd(eneity.getScencd());
		// 新增历史表
		KcpChrgScdfHistDao.insert(tblkChrgScdfHist);
		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblkChrgScdfHist);

	}
}
