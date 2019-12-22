package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdt;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdtDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_CUFETP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mdcmdt {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mdcmdt.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：计费公式明细修改
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void mdcmdt(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdcmdt.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdcmdt.Property property) {
		bizlog.method("mdcmdt begin >>>>>>");
		String chrgfm = input.getChrgfm(); // 计费公式代码
		String brchno = input.getBrchno(); // 机构号
		BigDecimal limiam = input.getLimiam(); // 档次区间下限

		if (CommUtil.isNull(chrgfm)) {
			throw FeError.Chrg.BNASF142();
		}
		if (CommUtil.isNull(brchno)) {
			throw FeError.Chrg.BNASF131();
		}
		if (CommUtil.isNull(input.getCrcycd())) {
			throw FeError.Chrg.BNASF156();
		}
		if (CommUtil.isNull(limiam)
				|| CommUtil.compare(limiam, BigDecimal.ZERO) < 0) {
			throw FeError.Chrg.BNASF046();
		}

		if (CommUtil.isNull(input.getCufetp())) {
			throw FeError.Chrg.BNASF149();
		}
		// 计费类型为比例
		if (CommUtil.compare(input.getCufetp(), E_CUFETP.R) == 0) {

			if (CommUtil.isNull(input.getChrgrt())) {
				throw FeError.Chrg.BNASF135();
			}

			if (CommUtil.compare(input.getChrgrt(), BigDecimal.ZERO) < 0
					|| CommUtil.compare(input.getChrgrt(),
							BigDecimal.TEN.multiply(BigDecimal.TEN)) > 0) {
				throw FeError.Chrg.BNASF136();
			}

			if (CommUtil.isNull(input.getCgmxam())) {
				throw FeError.Chrg.BNASF349();
			}

			if (CommUtil.isNull(input.getCgmnam())) {
				throw FeError.Chrg.BNASF343();
			}

			if (CommUtil.compare(input.getCgmnam(), BigDecimal.ZERO) < 0) {
				throw FeError.Chrg.BNASF344();
			}

			if (CommUtil.compare(input.getCgmxam(), input.getCgmnam()) <= 0) {
				throw FeError.Chrg.BNASF350();
			}

		}

		// 计费类型为单价
		if (CommUtil.compare(input.getCufetp(), E_CUFETP.S) == 0
				|| CommUtil.compare(input.getCufetp(), E_CUFETP.N) == 0) {

			if (CommUtil.isNull(input.getPecgam())) {
				throw FeError.Chrg.BNASF137();
			}

			if (CommUtil.compare(input.getPecgam(), BigDecimal.ZERO) < 0) {
				throw FeError.Chrg.BNASF138();
			}
		}

		// 判断传入机构是否存在

		if (!(brchno.equals(BusiTools.getBusiRunEnvs().getCentbr()))) {
			throw FeError.Chrg.BNASF199();
		}

		// 省县两级参数管理员均有操作权限，县级行社参数管理员只允许新增本行社
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), BusiTools.getBusiRunEnvs().getCentbr())
				&& !CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
						input.getBrchno())) {
			throw FeError.Chrg.BNASF158();
		}

		// 获取修改原数据
		KcpChrgFmdt tblkcp_chrg_fmdt = KcpChrgFmdtDao.selectOne_odb1(chrgfm,
				brchno, input.getCrcycd(), limiam, false);

		if (CommUtil.isNull(tblkcp_chrg_fmdt)) {
			throw FeError.Chrg.BNASF151();
		}

		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), BusiTools.getBusiRunEnvs().getCentbr())
				&& !CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
						tblkcp_chrg_fmdt.getBrchno())) {
			throw FeError.Chrg.BNASF128();
		}

		if (CommUtil.compare(input.getCufetp().toString(), tblkcp_chrg_fmdt
				.getCufetp().toString()) == 0
				&& CommUtil.compare(input.getChrgrt(),
						tblkcp_chrg_fmdt.getChrgrt()) == 0
				&& CommUtil.compare(input.getPecgam(),
						tblkcp_chrg_fmdt.getPecgam()) == 0
				&& CommUtil.compare(input.getCgmnam(),
						tblkcp_chrg_fmdt.getCgmnam()) == 0
				&& CommUtil.compare(input.getCgmxam(),
						tblkcp_chrg_fmdt.getCgmxam()) == 0
				&& CommUtil.compare(input.getRemark(),
						tblkcp_chrg_fmdt.getRemark()) == 0) {
			throw FeError.Chrg.BNASF317();
		}
		if (CommUtil.isNotNull(tblkcp_chrg_fmdt)) {

			KcpChrgFmdt oldEntity = CommTools.clone(KcpChrgFmdt.class,
					tblkcp_chrg_fmdt);
			Long num = (long) 0;

			if (CommUtil.compare(input.getCufetp().toString(), tblkcp_chrg_fmdt
					.getCufetp().toString()) != 0) { // 计费类型
				num++;
				tblkcp_chrg_fmdt.setCufetp(input.getCufetp());
			}
			if (CommUtil.compare(input.getChrgrt(),
					tblkcp_chrg_fmdt.getChrgrt()) != 0) {// 计费比例
				num++;
				tblkcp_chrg_fmdt.setChrgrt(input.getChrgrt());
			}
			if (CommUtil.compare(input.getPecgam(),
					tblkcp_chrg_fmdt.getPecgam()) != 0) {// 计费单价
				num++;
				tblkcp_chrg_fmdt.setPecgam(input.getPecgam());
			}
			if (CommUtil.compare(input.getCgmnam(),
					tblkcp_chrg_fmdt.getCgmnam()) != 0) {// 最低金额
				num++;
				tblkcp_chrg_fmdt.setCgmnam(input.getCgmnam());
			}
			if (CommUtil.compare(input.getCgmxam(),
					tblkcp_chrg_fmdt.getCgmxam()) != 0) {// 最高金额
				num++;
				tblkcp_chrg_fmdt.setCgmxam(input.getCgmxam());
			}
			if (CommUtil.compare(input.getRemark(),
					tblkcp_chrg_fmdt.getRemark()) != 0) {// 备注
				num++;
				tblkcp_chrg_fmdt.setRemark(input.getRemark());
			}
			KcpChrgFmdtDao.updateOne_odb1(tblkcp_chrg_fmdt);
			ApDataAudit.regLogOnUpdateParameter(oldEntity, tblkcp_chrg_fmdt);

		} else {
			throw FeError.Chrg.BNASF152();
		}
	}
}
