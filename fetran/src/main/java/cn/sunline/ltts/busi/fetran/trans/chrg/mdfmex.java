package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.text.DecimalFormat;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdfDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdt;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdtDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmexDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_FELYTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mdfmex {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mdfmex.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：标准费率定义修改
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void mdfmex(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdfmex.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdfmex.Property property) {
		bizlog.method("mdfmex begin >>>>>>");
		// 判断当前机构是否为省中心机构
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}

		String chrgcd = input.getChrgcd(); // 费种代码
		String chrgfm = input.getChrgfm(); // 计费公式代码
		String efctdt = input.getEfctdt(); // 生效日期
		String inefdt = input.getInefdt(); // 失效日期
		String crcycd = BusiTools.getDefineCurrency(); // 默认人民币
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date();

		// 不为空判断
		if (CommUtil.isNull(chrgcd)) {
			throw FeError.Chrg.BNASF076();
		}

		if (CommUtil.isNull(chrgfm)) {
			throw FeError.Chrg.BNASF142();
		}

		if (CommUtil.isNull(inefdt)) {
			throw FeError.Chrg.BNASF212();
		}

		if (CommUtil.isNull(efctdt)) {
			throw FeError.Chrg.BNASF207();
		}
		if (DateUtil.compareDate(inefdt, efctdt) <= 0){
			throw FeError.Chrg.BNASF210();
		}
		if (DateUtil.compareDate(inefdt, sTime) <= 0) {
			throw FeError.Chrg.BNASF209();
		}

		KcpChrgFmex tblChrgfmex = KcpChrgFmexDao.selectOne_odb4(chrgcd,
				input.getSeqnum(), false);
		if (CommUtil.isNotNull(tblChrgfmex)) {
			if (CommUtil.compare(chrgcd, tblChrgfmex.getChrgcd()) == 0
					&& CommUtil.compare(chrgfm, tblChrgfmex.getChrgfm()) == 0
					&& CommUtil.compare(efctdt, tblChrgfmex.getEfctdt()) == 0
					&& CommUtil.compare(inefdt, tblChrgfmex.getInefdt()) == 0) {
				throw FeError.Chrg.BNASF317();
			}

			// 判断该标准费率是否已失效
			if (DateUtil.compareDate(tblChrgfmex.getInefdt(), sTime) <= 0) {
				throw FeError.Chrg.BNASF092();
			}

			// 未生效的记录才能修改生效日期
			if (DateUtil.compareDate(tblChrgfmex.getEfctdt(), sTime) >= 0) {
				if (DateUtil.compareDate(efctdt, sTime) <= 0) {
					throw FeError.Chrg.BNASF204();
				}
			}else{
				if (DateUtil.compareDate(inefdt, sTime) > 0){
					if( CommUtil.compare(chrgfm, tblChrgfmex.getChrgfm()) != 0
							|| CommUtil.compare(efctdt, tblChrgfmex.getEfctdt()) != 0){
						throw FeError.Chrg.BNASF292();
					}
				}
				
			}

			KcpChrgFmdf tblKcp_chrg_fmdf = KcpChrgFmdfDao.selectOne_odb1(
					chrgfm, false);

			if (CommUtil.isNull(tblKcp_chrg_fmdf)) {
				throw FeError.Chrg.BNASF141();
			}

			// add 20170320 增加时间的判断
			if (CommUtil.compare(efctdt, tblKcp_chrg_fmdf.getEfctdt()) < 0
					|| CommUtil.compare(inefdt, tblKcp_chrg_fmdf.getInefdt()) > 0) {
				throw FeError.Chrg.BNASF114();
			}

			// add 20170204 songlw 费种代码取值类型与计费公式档次下限是否匹配
			if (E_FELYTP.NUM == KcpChrgDao
					.selectOne_odb1(chrgcd, crcycd, false).getFelytp()) { // 若为按笔数
				List<KcpChrgFmdt> lstKcp_chrg_fmdt = KcpChrgFmdtDao
						.selectAll_odb3(chrgfm, false);
				DecimalFormat df = new DecimalFormat("###.####");
				for (KcpChrgFmdt tblKcp_chrg_fmdt : lstKcp_chrg_fmdt) {
					String limiam = df.format(tblKcp_chrg_fmdt.getLimiam()); // 金额区间下限
					for (int i = limiam.length(); --i >= 0;) {
						if (!Character.isDigit(limiam.charAt(i))) {
							throw FeError.Chrg.BNASF083();
						}
					}
				}
			}
			KcpChrgFmex oldChrgfmex = CommTools.clone(KcpChrgFmex.class,
					tblChrgfmex);
			Long num = (long) 0;

			if (CommUtil.compare(chrgfm, tblChrgfmex.getChrgfm()) != 0) {// 计费公式代码
				if(CommUtil.isNotNull(KcpChrgFmexDao.selectOne_odb1(chrgcd,
						chrgfm, false))){
					throw FeError.Chrg.BNASF398();
				}
				num++;
				tblChrgfmex.setChrgfm(chrgfm);
			}
			if (CommUtil.compare(efctdt, tblChrgfmex.getEfctdt()) != 0) {// 生效日期
				num++;
				tblChrgfmex.setEfctdt(efctdt);
			}
			if (CommUtil.compare(inefdt, tblChrgfmex.getInefdt()) != 0) {// 失效日期
				num++;
				tblChrgfmex.setInefdt(inefdt);
			}
			// 判断当前标准费率是否已生效
			if (DateUtil.compareDate(tblChrgfmex.getEfctdt(), sTime) <= 0) {
				KcpChrgFmex tblkcp_chrg = KcpChrgFmexDao.selectOne_odb1(chrgcd,
						chrgfm, true);
				KcpChrgFmex oldEntity = CommTools.clone(KcpChrgFmex.class,
						tblkcp_chrg);

				tblkcp_chrg.setInefdt(inefdt);
				// 更新费种代码解析表
				KcpChrgFmexDao.updateOne_odb1(tblkcp_chrg);
				ApDataAudit.regLogOnUpdateParameter(oldEntity, tblkcp_chrg);
				return;
			}

			// 更新费种代码解析表
			KcpChrgFmexDao.updateOne_odb4(tblChrgfmex);
			ApDataAudit.regLogOnUpdateParameter(oldChrgfmex, tblChrgfmex);

		} else {
			throw FeError.Chrg.BNASF152();
		}
	}
}
