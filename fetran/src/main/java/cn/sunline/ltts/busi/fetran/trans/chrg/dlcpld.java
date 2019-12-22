package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.util.Map;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoPldfHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpFavoPldfHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPldf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPldfDao;
import cn.sunline.ltts.busi.sys.errors.FeError;

public class dlcpld {

	/**
	 * 
	 * @Title: dlcpld
	 * @Description: 删除优惠计划定义表
	 * @param diplcd
	 *            优惠计划代码
	 * @author zhangjunlei
	 * @date 2016年7月7日 下午8:38:43
	 * @version V2.3.0
	 */

	public static void dlcpld(String diplcd) {

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
		if (DateUtil.compareDate(tblKcp_favo_pldf.getEfctdt(), sTime) <= 0) {
			throw FeError.Chrg.BNASF111();
		}

		// 绑定优惠计划解析不能删除 add by chenjk
		if (FeDiscountDao.selSpexCntByDip(diplcd, false) != 0l) {

			throw FeError.Chrg.BNASF108();
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

	}
}
