package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;
import java.util.Map;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPldf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPldfDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPljo;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPljoDao;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgCplj;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 
 * @ClassName: mdcpla
 * @Description: 修改优惠计划
 * @author chengen
 * @date 2016年8月1日 上午8:41:06
 * 
 */

public class mdcpla {

	private static final BizLog bizlog = BizLogUtil.getBizLog(mdcpla.class);

	public static void mdcpla(
			final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Mdcpla.Input input) {

		bizlog.method("<<<<<< mdcpla begin >>>>>>");

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
		// 失效日期必须大于生效日期
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}

		// 生效日期必须大于当前系统日期
		if (DateUtil.compareDate(inefdt, sTime) <= 0) {
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

		// 已生效但未失效的优惠计划只能修改失效日期
		if (CommUtil.compare(sTime, tblKcpfavopldf.getEfctdt()) >= 0
				&& CommUtil.compare(sTime, tblKcpfavopldf.getInefdt()) < 0) {
			if (CommUtil.compare(planna, tblKcpfavopldf.getPlanna()) != 0
					|| CommUtil.compare(efctdt, tblKcpfavopldf.getEfctdt()) != 0
					|| CommUtil.compare(explan, tblKcpfavopldf.getExplan()) != 0) {
				throw FeError.Chrg.BNASF293();
			}
		} else if (CommUtil.compare(tblKcpfavopldf.getEfctdt(), sTime) > 0) { // 未生效
			if (DateUtil.compareDate(efctdt, sTime) <= 0) {
				throw FeError.Chrg.BNASF204();
			}
		}

		// 已失效的优惠计划不能修改
		if (DateUtil.compareDate(tblKcpfavopldf.getInefdt(), sTime) <= 0) {
			throw FeError.Chrg.BNASF109();
		}
		KcpFavoPldf oldKcpFavoPld = CommTools.clone(KcpFavoPldf.class,
				tblKcpfavopldf);

		for (IoCgCplj kcpFavoPljo : input.getLjinfo()) {
			String seqnum = kcpFavoPljo.getSeqnum();

			if (CommUtil.isNull(seqnum)) {
				throw FeError.Chrg.BNASF240();
			}

			KcpFavoPljo tblKcpfavopljo = KcpFavoPljoDao.selectOne_odb1(diplcd,
					seqnum, false);

			if (CommUtil.isNull(tblKcpfavopljo)) {
				throw FeError.Chrg.BNASF307();
			}

			if (CommUtil.isNotNull(tblKcpfavopljo)) {
				if (CommUtil.compare(kcpFavoPljo.getDimecg(),
						tblKcpfavopljo.getDimecg()) == 0
						&& CommUtil.compare(kcpFavoPljo.getRelvfg(),
								tblKcpfavopljo.getRelvfg()) == 0
						&& CommUtil.compare(kcpFavoPljo.getFadmvl(),
								tblKcpfavopljo.getFadmvl()) == 0
						&& CommUtil.compare(kcpFavoPljo.getIldmup(),
								tblKcpfavopljo.getIldmup()) == 0
						&& CommUtil.compare(kcpFavoPljo.getIldmdn(),
								tblKcpfavopljo.getIldmdn()) == 0
						&& CommUtil.compare(planna, tblKcpfavopldf.getPlanna()) == 0
						&& CommUtil.compare(efctdt, tblKcpfavopldf.getEfctdt()) == 0
						&& CommUtil.compare(inefdt, tblKcpfavopldf.getInefdt()) == 0
						&& CommUtil.compare(explan, tblKcpfavopldf.getExplan()) == 0) {
					throw FeError.Chrg.BNASF317();
				}

				KcpFavoPljo oldEntity = CommTools.clone(KcpFavoPljo.class,
						tblKcpfavopljo);

				// 明细登记簿维护

				if (CommUtil.isNull(kcpFavoPljo.getDimecg())) {
					throw FeError.Chrg.BNASF120();
				}

				if (CommUtil.compare(kcpFavoPljo.getDimecg(),
						tblKcpfavopljo.getDimecg()) != 0) {// 关联维度
					throw FeError.Chrg.BNASF119();
				}

				if (CommUtil.isNull(kcpFavoPljo.getRelvfg())) {
					throw FeError.Chrg.BNASF117();
				}
				if (CommUtil
						.isNull(FeDimeDao.selone_evl_dime(
								kcpFavoPljo.getFadmvl(),
								kcpFavoPljo.getDimecg(), false))
						&& CommUtil.isNotNull(kcpFavoPljo.getFadmvl())) {
					throw FeError.Chrg.BNASF252();
				}

				if (CommUtil.compare(BigDecimal.ZERO, kcpFavoPljo.getIldmdn()) != 0
						&& CommUtil.compare(BigDecimal.ZERO,
								kcpFavoPljo.getIldmup()) != 0
						&& CommUtil.compare(kcpFavoPljo.getIldmdn(),
								kcpFavoPljo.getIldmup()) >= 0) {
					throw FeError.Chrg.BNASF122();
				}

				if (CommUtil.isNull(kcpFavoPljo.getIldmdn())
						|| CommUtil.isNull(kcpFavoPljo.getIldmup())) {
					throw FeError.Chrg.BNASF121();
				}

				if ((CommUtil.compare(BigDecimal.ZERO, kcpFavoPljo.getIldmdn()) >= 0 || CommUtil
						.compare(BigDecimal.ZERO, kcpFavoPljo.getIldmup()) >= 0)
						&& CommUtil.isNull(kcpFavoPljo.getFadmvl())) {
					throw FeError.Chrg.BNASF311();
				}

				if ((CommUtil.compare(BigDecimal.ZERO, kcpFavoPljo.getIldmdn()) < 0 || CommUtil
						.compare(BigDecimal.ZERO, kcpFavoPljo.getIldmup()) < 0)
						&& CommUtil.isNotNull(kcpFavoPljo.getFadmvl())) {
					throw FeError.Chrg.BNASF310();
				}

				Long nums = (long) 0;

				if (CommUtil.compare(kcpFavoPljo.getRelvfg(),
						tblKcpfavopljo.getRelvfg()) != 0) {// 关联标志
					nums++;
					tblKcpfavopljo.setRelvfg(kcpFavoPljo.getRelvfg());
				}
				if (CommUtil.compare(kcpFavoPljo.getFadmvl(),
						tblKcpfavopljo.getFadmvl()) != 0) {// 优惠计划维度取值
					nums++;
					tblKcpfavopljo.setFadmvl(kcpFavoPljo.getFadmvl());
				}
				if (CommUtil.compare(kcpFavoPljo.getIldmdn(),
						tblKcpfavopljo.getIldmdn()) != 0) {
					nums++;
					tblKcpfavopljo.setIldmdn(kcpFavoPljo.getIldmdn());
				}
				if (CommUtil.compare(kcpFavoPljo.getIldmup(),
						tblKcpfavopljo.getIldmup()) != 0) {
					nums++;
					tblKcpfavopljo.setIldmup(kcpFavoPljo.getIldmup());
				}
				// 修改优惠计划明细表
				KcpFavoPljoDao.updateOne_odb1(tblKcpfavopljo);
				ApDataAudit.regLogOnUpdateParameter(oldEntity, tblKcpfavopljo);
			} else {
				throw FeError.Chrg.BNASF308();
			}
		}

		// 明细登记簿维护
		Long num = (long) 0;
		if (CommUtil.compare(input.getPlanna(), tblKcpfavopldf.getPlanna()) != 0) {
			num++;
			tblKcpfavopldf.setPlanna(planna);
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
		ApDataAudit.regLogOnUpdateParameter(oldKcpFavoPld, tblKcpfavopldf);
	}
}
