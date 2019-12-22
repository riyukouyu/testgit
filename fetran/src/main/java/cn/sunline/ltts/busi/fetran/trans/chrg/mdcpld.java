package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.util.Map;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPldf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPldfDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mdcpld {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mdcpld.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：修改优惠计划定义
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */

	public static void mdcpld(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdcpld.Input input) {

		String diplcd = input.getDiplcd();// 优惠计划代码
		String planna = input.getPlanna();// 优惠计划名称
		String brchno = input.getBrchno();// 机构号
		String efctdt = input.getEfctdt(); // 生效日期
		String inefdt = input.getInefdt(); // 失效日期
		String explan = input.getExplan();// 备注
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date(); // 交易日期
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();// 交易机构

		// 优惠计划代码
		if (CommUtil.isNull(diplcd)) {
			throw FeError.Chrg.BNASF303();
		}

		// 优惠计划代码名称
		if (CommUtil.isNull(planna)) {
			throw FeError.Chrg.BNASF304();
		}

		// 机构号
		if (CommUtil.isNull(brchno)) {
			throw FeError.Chrg.BNASF131();
		}

		// 生效日期
		if (CommUtil.isNull(efctdt)) {
			throw FeError.Chrg.BNASF207();
		}

		// 失效日期
		if (CommUtil.isNull(inefdt)) {
			throw FeError.Chrg.BNASF212();
		}

		// 备注
		if (CommUtil.isNull(explan)) {
			throw FeError.Chrg.BNASF003();
		}

		// 失效日期必须大于生效日期
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}

		// 生效日期必须大于当前系统日期
		if (DateUtil.compareDate(efctdt, sTime) <= 0) {
			throw FeError.Chrg.BNASF209();
		}

		// 获取交易机构等级
		Map<String, Object> map = FeDiscountDao.selKubBrchLevel(tranbr, false);
		String brchlv = map.get("brchlv").toString();

		// 不为省级的需判断是否为本行社
		if (!brchlv.equals("1")) {
			if (!CommUtil.equals(tranbr, brchno)) {
				throw FeError.Chrg.BNASF059();
			}
		}

		// 获取优惠定义表信息
		KcpFavoPldf tblKcpfavopldf = KcpFavoPldfDao.selectOne_odb1(diplcd,
				brchno, false);
		if (CommUtil.isNull(tblKcpfavopldf)) {
			throw FeError.Chrg.BNASF299();
		}

		// 已生效的优惠计划只能修改失效日期
		if (DateUtil.compareDate(tblKcpfavopldf.getEfctdt(), sTime) <= 0) {
			if (CommUtil.compare(planna, tblKcpfavopldf.getPlanna()) != 0
					|| CommUtil.compare(efctdt, tblKcpfavopldf.getEfctdt()) != 0
					|| CommUtil.compare(explan, tblKcpfavopldf.getExplan()) != 0) {
				throw FeError.Chrg.BNASF110();
			}
		}

		// 已失效的优惠计划不能修改
		if (DateUtil.compareDate(CommTools.getBaseRunEnvs().getTrxn_date(),
				tblKcpfavopldf.getInefdt()) > 0) {
			throw FeError.Chrg.BNASF112();
		}

		// 判断有没有修改
		if (CommUtil.compare(planna, tblKcpfavopldf.getPlanna()) == 0
				&& CommUtil.compare(efctdt, tblKcpfavopldf.getEfctdt()) == 0
				&& CommUtil.compare(inefdt, tblKcpfavopldf.getInefdt()) == 0
				&& CommUtil.compare(explan, tblKcpfavopldf.getExplan()) == 0) {
			throw FeError.Chrg.BNASF317();
		}
		KcpFavoPldf oldEntity = CommTools.clone(KcpFavoPldf.class,
				tblKcpfavopldf);

		// 明细登记簿维护
		Long num = (long) 0;

		if (CommUtil.compare(input.getPlanna(), tblKcpfavopldf.getPlanna()) != 0) {
			num++;
			tblKcpfavopldf.setPlanna(input.getPlanna());
		}

		if (CommUtil.compare(explan, tblKcpfavopldf.getExplan()) != 0) {
			num++;
			tblKcpfavopldf.setExplan(explan);
		}

		if (CommUtil.compare(efctdt, tblKcpfavopldf.getEfctdt()) != 0) {
			num++;
			tblKcpfavopldf.setEfctdt(efctdt);
		}

		if (CommUtil.compare(inefdt, tblKcpfavopldf.getInefdt()) != 0) {
			num++;
			tblKcpfavopldf.setInefdt(inefdt);
		}

		// 修改优惠计划定义表
		KcpFavoPldfDao.updateOne_odb1(tblKcpfavopldf);
		ApDataAudit.regLogOnUpdateParameter(oldEntity, tblKcpfavopldf);
	}
}
