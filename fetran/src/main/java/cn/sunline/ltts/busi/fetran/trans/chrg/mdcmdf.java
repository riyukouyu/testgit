package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeFormulaDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdfDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mdcmdf {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mdcmdf.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：计费公式定义表修改
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void mdcmdf(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdcmdf.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdcmdf.Property property) {
		bizlog.method("mdcmdf begin >>>>>>");

		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();//最低授权级别：省清算中心主办
		}

		String chrgfm = input.getChrgfm(); // 计费公式
		String fmunam = input.getFmunam(); // 计费公式名称
		String efctdt = input.getEfctdt(); // 生效日期
		String inefdt = input.getInefdt(); // 失效日期
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date(); // 交易日期

		if (CommUtil.isNull(fmunam)) {
			throw FeError.Chrg.BNASF145();
		}
		if (CommUtil.isNull(efctdt)) {
			throw FeError.Chrg.BNASF207();
		}
		if (CommUtil.isNull(inefdt)) {
			throw FeError.Chrg.BNASF212();
		}
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
		    throw FeError.Chrg.BNASF210();
		}
		// if (DateUtil.compareDate(efctdt, sTime) <= 0) {
		// throw FeError.Chrg.E9999("生效日期必须大于当前系统日期");
		// }

		if (DateUtil.compareDate(inefdt, sTime) <= 0) {
			throw FeError.Chrg.BNASF209();
		}

		KcpChrgFmdf tblkcp_chrg_fmdf = KcpChrgFmdfDao.selectOne_odb1(chrgfm,
				false); // 查询计费公式定义
		if (CommUtil.isNotNull(tblkcp_chrg_fmdf)) {
			if (CommUtil.compare(chrgfm, tblkcp_chrg_fmdf.getChrgfm()) == 0
					&& CommUtil.compare(fmunam, tblkcp_chrg_fmdf.getFmunam()) == 0
					&& CommUtil.compare(inefdt, tblkcp_chrg_fmdf.getInefdt()) == 0
					&& CommUtil.compare(efctdt, tblkcp_chrg_fmdf.getEfctdt()) == 0
					&& CommUtil.compare(input.getFilebs(),
							tblkcp_chrg_fmdf.getFilebs()) == 0) {
				throw FeError.Chrg.BNASF317();
			}
			Long num = (long) 0;

			// 未生效的记录才能修改生效日期
			if (DateUtil.compareDate(tblkcp_chrg_fmdf.getEfctdt(), sTime) > 0) {
				if (DateUtil.compareDate(efctdt, sTime) <= 0) {
					throw FeError.Chrg.BNASF204();
				}
			}else{
				// 已经生效
				if (DateUtil.compareDate(tblkcp_chrg_fmdf.getInefdt(), sTime) < 0) {
					throw FeError.Chrg.BNASF295();
				} else {
					if (CommUtil.compare(tblkcp_chrg_fmdf.getChrgfm(),
							chrgfm) != 0
							|| CommUtil.compare(tblkcp_chrg_fmdf.getFmunam(),
									input.getFmunam()) != 0
							|| CommUtil.compare(tblkcp_chrg_fmdf.getFilebs(),
									input.getFilebs()) != 0
							|| CommUtil.compare(tblkcp_chrg_fmdf.getEfctdt(),
									input.getEfctdt()) != 0) {
						throw FeError.Chrg.BNASF292();
					} else {
						if (CommUtil.compare(input.getInefdt(),
								tblkcp_chrg_fmdf.getInefdt()) == 0) {
							throw FeError.Chrg.BNASF317();
						}
					}
			    }
			}
			KcpChrgFmdf oldEntity = CommTools.clone(KcpChrgFmdf.class,
					tblkcp_chrg_fmdf);

			if (CommUtil.compare(fmunam, tblkcp_chrg_fmdf.getFmunam()) != 0) { // 计费公式名称
				KcpChrgFmdf tblkcpchrgfmdf = FeFormulaDao.selfum_kcp_chrg_fmdf(fmunam, false);
				
				if (CommUtil.isNotNull(tblkcpchrgfmdf)) {
					throw FeError.Chrg.BNASF116();
				}
				num++;
				tblkcp_chrg_fmdf.setFmunam(fmunam);
			}

			if (CommUtil.compare(efctdt, tblkcp_chrg_fmdf.getEfctdt()) != 0) { // 生效日期
				num++;
				tblkcp_chrg_fmdf.setEfctdt(efctdt);
			}

			if (CommUtil.compare(inefdt, tblkcp_chrg_fmdf.getInefdt()) != 0) { // 失效日期
				num++;
				tblkcp_chrg_fmdf.setInefdt(inefdt);
			}

			if (CommUtil.compare(input.getFilebs(),
					tblkcp_chrg_fmdf.getFilebs()) != 0) { // 文件依据
				num++;
				tblkcp_chrg_fmdf.setFilebs(input.getFilebs());
			}
			KcpChrgFmdfDao.updateOne_odb1(tblkcp_chrg_fmdf);

			ApDataAudit.regLogOnUpdateParameter(oldEntity, tblkcp_chrg_fmdf);
		} else {
			throw FeError.Chrg.BNASF152();
		}
	}
}
