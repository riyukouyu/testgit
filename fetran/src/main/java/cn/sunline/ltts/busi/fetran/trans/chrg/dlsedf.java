package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpScevDefnHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpScevDefnHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDefn;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDefnDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDetl;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDetlDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_MODULE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class dlsedf {
	private static final BizLog bizlog = BizLogUtil.getBizLog(dlsedf.class);

	/**
	 * 
	 * @Title: dlsedf
	 * @Description: (删除场景事件)
	 * @param input
	 * @param property
	 * @author leipeng
	 * @date 2016年7月7日 下午7:51:50
	 * @version V2.3.0
	 */
	public static void dlsedf(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlsedf.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlsedf.Property property) {
		bizlog.method("dlsedf begin >>>>>>");
		String evetcd = input.getEvetcd(); // 事件编号
		E_MODULE module = E_MODULE.CG; // 默认费用模块

		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}

		if (CommUtil.isNull(evetcd)) {
			throw FeError.Chrg.BNASF217();
		}

		if (CommUtil.isNull(module)) {
			throw FeError.Chrg.BNASF188();
		}

		// 有绑定关系不允许删除
		List<KcpScevDetl> selkcpDcevDetl = KcpScevDetlDao.selectAll_odb2(
				module, evetcd, false);
		if (CommUtil.isNotNull(selkcpDcevDetl)) {
			throw FeError.Chrg.BNASF177();
		}

		KcpScevDefn entity = SysUtil.getInstance(KcpScevDefn.class);
		entity = KcpScevDefnDao.selectOne_odb1(module, evetcd, false);

		if (CommUtil.isNull(entity)) {
			throw FeError.Chrg.BNASF020();
		}

		KcpScevDefnDao.deleteOne_odb1(module, evetcd);

		// 增加审计
		ApDataAudit.regLogOnDeleteParameter(entity);

		/**********************************************
		 * 维护历史表
		 **********************************************/
		// 实例化场景事件定义历史表定义历史表
		KcpScevDefnHist tblKcpScevDefnHist = SysUtil.getInstance(KcpScevDefnHist.class);
		tblKcpScevDefnHist.setCorpno(entity.getCorpno());
		tblKcpScevDefnHist.setEvetcd(entity.getEvetcd());
		tblKcpScevDefnHist.setEvetna(entity.getEvetna());
		tblKcpScevDefnHist.setModule(entity.getModule());
		tblKcpScevDefnHist.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		tblKcpScevDefnHist.setRemark(entity.getRemark());
		// 新增历史表
		KcpScevDefnHistDao.insert(tblKcpScevDefnHist);

		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblKcpScevDefnHist);

	}
}
