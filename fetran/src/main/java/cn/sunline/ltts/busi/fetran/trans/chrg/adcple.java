package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeCodeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrg;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPldf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPlex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPlexDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_FASTTP;

public class adcple {
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：优惠计划解析新增
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void adcple(
			final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcple.Input input) {
		String chrgcd = input.getChrgcd();// 费种代码
		String diplcd = input.getDiplcd();// 优惠计划代码
		String crcycd = BusiTools.getDefineCurrency();// 币种 默认人民币
		E_FASTTP fasttp = input.getFasttp();// 优惠起点类型
		BigDecimal fastam = input.getFastam();// 优惠起点
		BigDecimal favoir = input.getFavoir();// 优惠比例
		String efctdt = input.getEfctdt(); // 生效日期
		String inefdt = input.getInefdt(); // 失效日期
		String explan = input.getExplan(); // 说明
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();// 交易机构
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); // 交易日期

		// 费种代码
		if (CommUtil.isNull(chrgcd)) {
			throw FeError.Chrg.BNASF076();
		}

		// 优惠计划代码
		if (CommUtil.isNull(diplcd)) {
			throw FeError.Chrg.BNASF303();
		}

		// 优惠起点类型
		if (CommUtil.isNull(fasttp)) {
			throw FeError.Chrg.BNASF313();
		}

		// 优惠起点
		if (CommUtil.isNull(fastam)) {
			throw FeError.Chrg.BNASF312();
		}

		// 优惠比例
		if (CommUtil.isNull(favoir)) {
			throw FeError.Chrg.BNASF297();
		}

		if (CommUtil.isNull(efctdt)) {
			throw FeError.Chrg.BNASF207();
		}

		if (CommUtil.isNull(inefdt)) {
			throw FeError.Chrg.BNASF212();
		}

		// 优惠比例应大于0小于等于1
		if (CommUtil.compare(favoir, BigDecimal.ZERO) <= 0
				|| CommUtil.compare(favoir, BigDecimal.valueOf(100)) > 0) {
			throw FeError.Chrg.BNASF298();
		}

		if (CommUtil.compare(fastam, BigDecimal.ZERO) < 0) {
			throw FeError.Chrg.BNASF314();
		}

		// 失效日期必须大于生效日期
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}

		// 生效日期必须大于当前系统日期
		if (DateUtil.compareDate(efctdt, trandt) <= 0) {
			throw FeError.Chrg.BNASF204();
		}

		// 若优惠类型为按笔数，判断输入是否为整数
		if (fasttp == E_FASTTP.NUM) {
			String str = fastam.toString();
			for (int i = str.length(); --i >= 0;) {
				if (!Character.isDigit(str.charAt(i))) {
					if (!String.valueOf(str.charAt(i + 1)).equals("0"))
						throw FeError.Chrg.BNASF315();
				}
			}
		}
		// 查询费种代码信息 不带法人 change by chenjk
		// kcp_chrg tblKcp_chrg = Kcp_chrgDao.selectOne_odb1(chrgcd, crcycd,
		// false);
		KcpChrg tblKcp_chrg = FeCodeDao.selChrgByChrg(chrgcd, crcycd, false);
		if (CommUtil.isNull(tblKcp_chrg)) {
			throw FeError.Chrg.BNASF085();
		}

		// 查询优惠计划代码信息 不带法人 change by chenjk
		// kcp_favo_pldf tblKcp_favo_pldf =
		// Kcp_favo_pldfDao.selectOne_odb2(diplcd, false);
		KcpFavoPldf tblKcp_favo_pldf = FeDiscountDao.selPldfByPlcd(diplcd,
				false);
		if (CommUtil.isNull(tblKcp_favo_pldf)) {
			throw FeError.Chrg.BNASF306();
		}

		// 判断费种代码和优惠计划代码是否存在绑定关系 不带法人 change by chenjk
		// kcp_favo_plex tblFavoplex =
		// Kcp_favo_plexDao.selectOne_odb1(chrgcd,diplcd, false);
		KcpFavoPlex tblFavoplex = FeDiscountDao.selPlexByPlcd(chrgcd, diplcd,
				false);
		if (CommUtil.isNotNull(tblFavoplex)) {
			throw FeError.Chrg.BNASF098();

		} else {
			
			if (DateUtil.compareDate(efctdt, tblKcp_favo_pldf.getEfctdt()) < 0) {
				throw FeError.Chrg.BNASF206();
			}
			if (DateUtil.compareDate(inefdt, tblKcp_favo_pldf.getInefdt()) > 0) {
				throw FeError.Chrg.BNASF211();
			}
			tblFavoplex = SysUtil.getInstance(KcpFavoPlex.class);// 优惠计划解析表

			tblFavoplex.setChrgcd(chrgcd); // 费种代码
			tblFavoplex.setCrcycd(crcycd); // 币种
			tblFavoplex.setDiplcd(diplcd); // 优惠计划代码
			tblFavoplex.setFasttp(fasttp); // 优惠起点类型
			tblFavoplex.setFastam(fastam);// 优惠起点
			tblFavoplex.setFavoir(favoir);// 优惠比例
			tblFavoplex.setBrchno(tranbr);// 机构号
			tblFavoplex.setEfctdt(efctdt);// 生效日期
			tblFavoplex.setInefdt(inefdt);// 失效日期
			tblFavoplex.setExplan(explan);// 说明

			KcpFavoPlexDao.insert(tblFavoplex);// 登记优惠计划解析表

			// 增加审计
			ApDataAudit.regLogOnInsertParameter(tblFavoplex);
		}

	}
}
