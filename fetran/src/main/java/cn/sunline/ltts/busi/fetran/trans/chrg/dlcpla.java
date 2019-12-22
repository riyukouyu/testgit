package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.util.ArrayList;
import java.util.Map;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoPldfHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoPldfHistDao;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoPljoHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoPljoHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPldf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPldfDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPljo;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPljoDao;
import cn.sunline.ltts.busi.sys.errors.FeError;

/**
 * 
 * @ClassName: dlcpla
 * @Description: 删除优惠计划
 * @author chengen
 * @date 2016年8月1日 上午11:00:15
 * 
 */
public class dlcpla {

	public static void dlcpla(String diplcd) {
		// 优惠计划代码
		if (CommUtil.isNull(diplcd)) {
			throw FeError.Chrg.BNASF303();
		}

		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();// 交易机构
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date(); // 交易日期

		// 获取机构等级
		Map<String, Object> map = FeDiscountDao.selKubBrchLevel(tranbr, false);
		String brchlv = map.get("brchlv").toString();// 机构等级

		// 查询优惠计划信息
		KcpFavoPldf tblKcp_favo_pldf = KcpFavoPldfDao.selectOne_odb2(diplcd,
				false);

		//
		if (CommUtil.isNull(tblKcp_favo_pldf)) {
			throw FeError.Chrg.BNASF267();
		}

		// 不是省级行社的只能操作本行社优惠计划信息
		if (!brchlv.equals("1")) {
			if (!CommUtil.equals(tranbr, tblKcp_favo_pldf.getBrchno())) {
				throw FeError.Chrg.BNASF057();
			}
		}

		// 已生效的优惠计划不能删除
		if (DateUtil.compareDate(tblKcp_favo_pldf.getEfctdt(), sTime) <= 0
				&& DateUtil.compareDate(CommTools.getBaseRunEnvs().getTrxn_date(),
						tblKcp_favo_pldf.getInefdt()) < 0) {
			throw FeError.Chrg.BNASF111();
		}

		// 绑定关系的判断 add by chenjk
		if (FeDiscountDao.selSpexCntByDip(diplcd, false) > 0) {
			throw FeError.Chrg.BNASF106();

		}

		// 删除
		KcpFavoPldfDao.deleteOne_odb2(diplcd);// 删除优惠计划代码

		// 增加审计
		ApDataAudit.regLogOnDeleteParameter(tblKcp_favo_pldf);

		/**********************************************
		 * 维护历史表
		 **********************************************/
		// 实例化优惠计划定义历史表
		KcpFavoPldfHist tblkcppldfhist = SysUtil.getInstance(KcpFavoPldfHist.class);

		tblkcppldfhist.setBrchno(tblKcp_favo_pldf.getBrchno());
		tblkcppldfhist.setCorpno(tblKcp_favo_pldf.getCorpno());
		tblkcppldfhist.setDimecg(tblKcp_favo_pldf.getDimecg());
		tblkcppldfhist.setDiplcd(tblKcp_favo_pldf.getDiplcd());
		tblkcppldfhist.setEfctdt(tblKcp_favo_pldf.getEfctdt());
		tblkcppldfhist.setExplan(tblKcp_favo_pldf.getExplan());
		tblkcppldfhist.setExpmsg(tblKcp_favo_pldf.getExpmsg());
		tblkcppldfhist.setFadmmp(tblKcp_favo_pldf.getFadmmp());
		tblkcppldfhist.setIldmch(tblKcp_favo_pldf.getIldmch());
		tblkcppldfhist.setInefdt(tblKcp_favo_pldf.getInefdt());
		tblkcppldfhist.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		tblkcppldfhist.setPlanna(tblKcp_favo_pldf.getPlanna());

		// 新增历史表
		KcpFavoPldfHistDao.insert(tblkcppldfhist);

		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblkcppldfhist);

		java.util.List<KcpFavoPljo> tblKcpFavoPljo = new ArrayList<KcpFavoPljo>();
		tblKcpFavoPljo = KcpFavoPljoDao.selectAll_odb4(diplcd, false);

		if (CommUtil.isNotNull(tblKcpFavoPljo)) {
			KcpFavoPljoDao.delete_odb4(diplcd);

			// 实例化优惠计划定义历史表
			// java.util.List<kcp_favo_pljo_hist> kcpFavoPljoHist = new
			// ArrayList<kcp_favo_pljo_hist>();

			// Long nums = (long) 0;

			for (KcpFavoPljo kcpFavoPljo : tblKcpFavoPljo) {
				KcpFavoPljoHist kcpFavoPljoHist = SysUtil.getInstance(KcpFavoPljoHist.class);

				kcpFavoPljoHist.setDimecg(kcpFavoPljo.getDimecg());
				kcpFavoPljoHist.setDiplcd(kcpFavoPljo.getDiplcd());
				kcpFavoPljoHist.setFadmvl(kcpFavoPljo.getFadmvl());
				kcpFavoPljoHist.setIldmdn(kcpFavoPljo.getIldmdn());
				kcpFavoPljoHist.setIldmup(kcpFavoPljo.getIldmup());
				kcpFavoPljoHist.setSeqnum(kcpFavoPljo.getSeqnum());
				kcpFavoPljoHist.setRelvfg(kcpFavoPljo.getRelvfg());
				KcpFavoPljoHistDao.insert(kcpFavoPljoHist);

				// 增加审计
				ApDataAudit.regLogOnInsertParameter(kcpFavoPljoHist);

			}
		}
	}
}
