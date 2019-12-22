package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdfDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mdcpdf {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mdcpdf.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：超额优惠定义修改
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void mdcpdf(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdcpdf.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdcpdf.Property property) {
		bizlog.method("mdcpdf begin >>>>>>");
		String smfacd = input.getSmfacd(); // 超额优惠代码
		String efctdt = input.getEfctdt(); // 失效日期
		String inefdt = input.getInefdt(); // 生效日期
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date(); // 交易日期

		if (CommUtil.isNull(inefdt)) {
			throw FeError.Chrg.BNASF212();
		}
		if (CommUtil.isNull(efctdt)) {
			throw FeError.Chrg.BNASF207();
		}
		if (CommUtil.isNull(input.getPdunit())) {
			throw FeError.Chrg.BNASF339();
		}
		if (CommUtil.isNull(input.getSmbdtp())) {
			throw FeError.Chrg.BNASF187();
		}
		if (CommUtil.isNull(input.getCgsmtp())) {
			throw FeError.Chrg.BNASF232();
		}
		if (CommUtil.isNull(input.getBrchno())) {
			throw FeError.Chrg.BNASF131();
		}
		if (CommUtil.isNull(input.getSmfacd())) {
			throw FeError.Chrg.BNASF028();
		}
		if (CommUtil.isNull(input.getSmfana())) {
			throw FeError.Chrg.BNASF029();
		}
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}
		if (DateUtil.compareDate(inefdt, sTime) <= 0) {
			throw FeError.Chrg.BNASF209();
		}
		if (CommUtil.isNull(KcpFavoSmdfDao.selectOne_odb1(smfacd, false))) {
			throw FeError.Chrg.BNASF027();
		}

		// 省县两级参数管理员均有操作权限，县级行社参数管理员只允许新增本行社
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				BusiTools.getBusiRunEnvs().getCentbr())
				&& !CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
						input.getBrchno())) {
			throw FeError.Chrg.BNASF158();
		}

		KcpFavoSmdf tblFavosmdf = KcpFavoSmdfDao.selectOne_odb1(smfacd, false);

		if (CommUtil.isNull(tblFavosmdf)) {
			throw FeError.Chrg.BNASF005();
		}

		if (CommUtil.compare(input.getBrchno(), tblFavosmdf.getBrchno()) != 0) {
			throw FeError.Chrg.BNASF132();
		}

		// 已生效
		if (CommUtil.compare(sTime, tblFavosmdf.getEfctdt()) >= 0
				&& CommUtil.compare(sTime, tblFavosmdf.getInefdt()) < 0) {
			if (CommUtil.compare(efctdt, tblFavosmdf.getEfctdt()) != 0
					|| CommUtil.compare(input.getSmfana(),
							tblFavosmdf.getSmfana()) != 0
					|| CommUtil.compare(input.getSmbdtp().toString(),
							tblFavosmdf.getSmbdtp().toString()) != 0
					|| CommUtil.compare(input.getCgsmtp().toString(),
							tblFavosmdf.getCgsmtp().toString()) != 0
					|| CommUtil.compare(input.getPdunit().toString(),
							tblFavosmdf.getPdunit().toString()) != 0) {
				throw FeError.Chrg.BNASF292();
			}
		} else if (CommUtil.compare(tblFavosmdf.getEfctdt(), sTime) > 0) { // 未生效
			if (DateUtil.compareDate(efctdt, sTime) <= 0) {
				throw FeError.Chrg.BNASF204();
			}
		}
		if(CommUtil.compare(sTime, tblFavosmdf.getInefdt()) >= 0 ){
			throw FeError.Chrg.BNASF289();
		}

		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				BusiTools.getBusiRunEnvs().getCentbr())
				&& !CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
						tblFavosmdf.getBrchno())) {
			throw FeError.Chrg.BNASF128();
		}

		if (CommUtil.compare(input.getSmfana(), tblFavosmdf.getSmfana()) == 0
				&& CommUtil.compare(input.getSmbdtp().toString(), tblFavosmdf
						.getSmbdtp().toString()) == 0
				&& CommUtil.compare(input.getCgsmtp().toString(), tblFavosmdf
						.getCgsmtp().toString()) == 0
				&& CommUtil.compare(input.getPdunit().toString(), tblFavosmdf
						.getPdunit().toString()) == 0
				&& CommUtil.compare(efctdt, tblFavosmdf.getEfctdt()) == 0
				&& CommUtil.compare(inefdt, tblFavosmdf.getInefdt()) == 0) {
			throw FeError.Chrg.BNASF317();
		}
		KcpFavoSmdf oldEntity = CommTools.clone(KcpFavoSmdf.class, tblFavosmdf);
		Long num = (long) 0;

		if (CommUtil.compare(input.getSmfana(), tblFavosmdf.getSmfana()) != 0) {
			num++;
			tblFavosmdf.setSmfana(input.getSmfana());
		}

		if (CommUtil.compare(input.getSmbdtp().toString(), tblFavosmdf
				.getSmbdtp().toString()) != 0) {
			num++;
			tblFavosmdf.setSmbdtp(input.getSmbdtp());
		}

		if (CommUtil.compare(input.getCgsmtp().toString(), tblFavosmdf
				.getCgsmtp().toString()) != 0) {
			num++;
			tblFavosmdf.setCgsmtp(input.getCgsmtp());
		}

		if (CommUtil.compare(input.getPdunit().toString(), tblFavosmdf
				.getPdunit().toString()) != 0) {
			num++;
			tblFavosmdf.setPdunit(input.getPdunit());
		}

		if (CommUtil.compare(efctdt, tblFavosmdf.getEfctdt()) != 0) {
			num++;
			tblFavosmdf.setEfctdt(input.getEfctdt());
		}

		if (CommUtil.compare(inefdt, tblFavosmdf.getInefdt()) != 0) {
			num++;
			tblFavosmdf.setInefdt(input.getInefdt());
		}

		// 更新超额优惠定义表
		KcpFavoSmdfDao.updateOne_odb1(tblFavosmdf);
		ApDataAudit.regLogOnUpdateParameter(oldEntity, tblFavosmdf);
	}
}
