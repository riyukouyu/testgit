package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeCodeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrg;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdfDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmexDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mdcpal {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mdcpal.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：超额优惠解析修改
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void mdcpal(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdcpal.Input input) {
		bizlog.method("mdcpal begin >>>>>>");
		String chrgcd = input.getChrgcd();// 费种代码
		String smfacd = input.getSmfacd();// 超额优惠代码
		String crcycd = BusiTools.getDefineCurrency(); // 币种 默认人民币
		String efctdt = input.getEfctdt(); // 失效日期
		String inefdt = input.getInefdt(); // 生效日期
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date(); // 交易日期

		if (CommUtil.isNull(chrgcd)) {
			throw FeError.Chrg.BNASF076();
		}
		if (CommUtil.isNull(smfacd)) {
			throw FeError.Chrg.BNASF028();
		}
		if (CommUtil.isNull(efctdt)) {
			throw FeError.Chrg.BNASF207();
		}
		if (CommUtil.isNull(inefdt)) {
			throw FeError.Chrg.BNASF212();
		}
		if (CommUtil.isNull(input.getDimevl())) {
			throw FeError.Chrg.BNASF259();
		}
		if (CommUtil.isNull(input.getFadmtp())) {
			throw FeError.Chrg.BNASF251();
		}
		if (CommUtil.isNull(FeCodeDao.selone_kcp_chrg(chrgcd, false))) {
			throw FeError.Chrg.BNASF074();
		}
		if (CommUtil.isNull(FeDimeDao.selone_evl_dime(input.getDimevl(),
				input.getFadmtp(), false))) {
			throw FeError.Chrg.BNASF252();
		}
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}
		if (DateUtil.compareDate(inefdt, sTime) <= 0) {
			throw FeError.Chrg.BNASF209();
		}

		KcpFavoSmdf kcpFavoSmdf = KcpFavoSmdfDao.selectOne_odb1(smfacd, false);
		if (CommUtil.isNull(kcpFavoSmdf)) {
			throw FeError.Chrg.BNASF027();
		}
		if (CommUtil.compare(efctdt, kcpFavoSmdf.getEfctdt()) < 0
				|| CommUtil.compare(inefdt, kcpFavoSmdf.getInefdt()) > 0) {
			throw FeError.Chrg.BNASF054();
		}
		// 20170406 mod songlw 查询去掉法人代码
		KcpChrg kcpChrg = FeCodeDao.selone_kcp_chrg(chrgcd, false);
		if (CommUtil.isNull(kcpChrg)) {
			throw FeError.Chrg.BNASF074();
		}
		KcpFavoSmex tblFavosmex = KcpFavoSmexDao.selectOne_odb1(chrgcd,
				input.getFadmtp(), crcycd, smfacd, false);

		if (CommUtil.isNull(tblFavosmex)) {
			throw FeError.Chrg.BNASF031();
		}

		// 已生效
		if (CommUtil.compare(sTime, tblFavosmex.getEfctdt()) >= 0
				&& CommUtil.compare(sTime, tblFavosmex.getInefdt()) < 0) {
			if (CommUtil.compare(efctdt, tblFavosmex.getEfctdt()) != 0
					|| CommUtil.compare(input.getDimevl(),
							tblFavosmex.getDimevl()) != 0
					|| CommUtil.compare(input.getExplan(),
							tblFavosmex.getExplan()) != 0) {
				throw FeError.Chrg.BNASF284();
			}
		} else if (CommUtil.compare(tblFavosmex.getEfctdt(), sTime) > 0) { // 未生效
			if (DateUtil.compareDate(efctdt, sTime) <= 0) {
				throw FeError.Chrg.BNASF204();
			}
		}
		if(CommUtil.compare(tblFavosmex.getInefdt(), sTime)<=0){
			throw FeError.Chrg.BNASF290();
		}
		if (CommUtil.compare(input.getDimevl(), tblFavosmex.getDimevl()) == 0
				&& CommUtil.compare(efctdt, tblFavosmex.getEfctdt()) == 0
				&& CommUtil.compare(inefdt, tblFavosmex.getInefdt()) == 0
				&& CommUtil.compare(input.getExplan(), tblFavosmex.getExplan()) == 0) {
			throw FeError.Chrg.BNASF317();
		}
		if (CommUtil.isNotNull(tblFavosmex)) {

			KcpFavoSmex oldEntity = CommTools.clone(KcpFavoSmex.class,
					tblFavosmex);
			Long num = (long) 0;

			if (CommUtil.compare(input.getDimevl(), tblFavosmex.getDimevl()) != 0) { // 维度值
				num++;
				tblFavosmex.setDimevl(input.getDimevl());
			}
			if (CommUtil.compare(efctdt, tblFavosmex.getEfctdt()) != 0) {// 生效日期
				num++;
				tblFavosmex.setEfctdt(input.getEfctdt());
			}
			if (CommUtil.compare(inefdt, tblFavosmex.getInefdt()) != 0) {// 失效日期
				num++;
				tblFavosmex.setInefdt(input.getInefdt());
			}
			if (CommUtil.compare(input.getExplan(), tblFavosmex.getExplan()) != 0) {// 备注
				num++;
				tblFavosmex.setExplan(input.getExplan());
			}
			// 更新超额优惠解析
			KcpFavoSmexDao.updateOne_odb1(tblFavosmex);
			ApDataAudit.regLogOnUpdateParameter(oldEntity, tblFavosmex);

		} else {
			throw FeError.Chrg.BNASF152();
		}
	}
}
