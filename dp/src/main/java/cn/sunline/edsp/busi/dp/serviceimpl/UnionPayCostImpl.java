
package cn.sunline.edsp.busi.dp.serviceimpl;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.iobus.type.dp.DpdebitAcctnos.UnionCastInfo;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CARDTP;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CHARTP;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_MELMTP;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_MERCFG;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SERVCD;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SPCIPY;
import cn.sunline.ltts.busi.ca.tables.TransRoute.KnpUnionCostPara;
import cn.sunline.ltts.busi.ca.tables.TransRoute.KnpUnionCostParaDao;

/**
 * 银联成本计算服务实现
 *
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "UnionPayCostImpl", longname = "银联成本计算服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class UnionPayCostImpl implements cn.sunline.edsp.busi.dp.iobus.servicetype.dp.UnionPayCostSvcType {
	/**
	 * 银联成本计算处理
	 *
	 */
	@Override
	public UnionCastInfo unionPayCostDeal(E_CARDTP cardtp, E_SPCIPY spcipy, E_MERCFG mercfg, BigDecimal tranam,
			E_SERVCD svcode) {
		if (CommUtil.isNull(mercfg)) {
			mercfg = E_MERCFG.STANDARD;
		}

		if (CommUtil.isNotNull(cardtp)) {// 判断是否无卡
			if (CommUtil.equals(cardtp.getValue(), E_CARDTP.NONE.getValue())) {
				return costDeal(E_CHARTP.NOCD, E_CARDTP.NONE, tranam, mercfg);
			} else {// 有卡处理情况

				// 判断是否有特殊优惠场景
				if (CommUtil.isNotNull(spcipy) && !CommUtil.equals(E_SPCIPY.NONE.getValue(), spcipy.getValue())) {
					return costDeal(E_CHARTP.DOUBLEEXE, cardtp, tranam, mercfg);
				}

				// 处理NFC支付和银联二维码场景
				if (CommUtil.isNotNull(svcode)) {
					if (CommUtil.equals(svcode.getValue(), E_SERVCD.PAY10003.getValue())) {
						return costDeal(E_CHARTP.NFC, cardtp, tranam, mercfg);
					}

					if (CommUtil.equals(svcode.getValue(), E_SERVCD.PAY10005.getValue())) {
						return costDeal(E_CHARTP.UNION, cardtp, tranam, mercfg);
					}
				}

				// 判断商户类型非空
				if (CommUtil.isNotNull(mercfg)) {
					// 商户类型为优惠场景
					if (CommUtil.equals(mercfg.getValue(), E_MERCFG.FAVOUR.getValue())) {
						return costDeal(E_CHARTP.FAVOUR, cardtp, tranam, mercfg);
					}

					// 商户类型为标准场景
					if (CommUtil.equals(mercfg.getValue(), E_MERCFG.STANDARD.getValue())) {
						return costDeal(E_CHARTP.STANDARD, cardtp, tranam, mercfg);
					}

					// 商户类型为减免场景
					if (CommUtil.equals(mercfg.getValue(), E_MERCFG.MITIGATE.getValue())) {
						return costDeal(E_CHARTP.MITIGATE, cardtp, tranam, mercfg);
					}

					// 商户类型为公缴场景
					if (CommUtil.equals(mercfg.getValue(), E_MERCFG.PUBLICPAY.getValue())) {
						return costDeal(E_CHARTP.PUBLICPAY, cardtp, tranam, mercfg);
					}
				}
			}
		}
		return null;
	}

	/**
	 * 计算银联费用
	 * 
	 * @param chartp
	 * @param cardtp
	 * @param tranam
	 * @return BigDecimal
	 */
	public static UnionCastInfo costDeal(E_CHARTP chartp, E_CARDTP cardtp, BigDecimal tranam, E_MERCFG mercfg) {
		BigDecimal reanam = BigDecimal.ZERO;// 初始化金额

		BigDecimal divisor = new BigDecimal(100);// 初始化金额

		KnpUnionCostPara knpUnionCostPara = KnpUnionCostParaDao.selectOne_index01(chartp, cardtp, false);

		if (CommUtil.isNull(knpUnionCostPara)) {
			throw DpModuleError.DpTrans.TS020047();
		}

		if (CommUtil.equals(chartp.getValue(), E_CHARTP.NFC.getValue())
				|| CommUtil.equals(chartp.getValue(), E_CHARTP.UNION.getValue())
				|| CommUtil.equals(chartp.getValue(), E_CHARTP.DOUBLEEXE.getValue())) {
			if (CommUtil.equals(E_MELMTP.LESSOREQ.getValue(), knpUnionCostPara.getMelmtp().getValue())) {
				// 判断金额是否大于限制,大于限制后计费方式改为标准
				if (CommUtil.compare(tranam, knpUnionCostPara.getMonelm()) > 0) {
					String merctp = mercfg.getValue();
					switch (merctp) {
					case "1":
						knpUnionCostPara = KnpUnionCostParaDao.selectOne_index01(E_CHARTP.MITIGATE, cardtp, false);
						break;
					case "2":
						knpUnionCostPara = KnpUnionCostParaDao.selectOne_index01(E_CHARTP.PUBLICPAY, cardtp, false);
						break;
					case "3":
						knpUnionCostPara = KnpUnionCostParaDao.selectOne_index01(E_CHARTP.FAVOUR, cardtp, false);
						break;
					case "4":
						knpUnionCostPara = KnpUnionCostParaDao.selectOne_index01(E_CHARTP.STANDARD, cardtp, false);
						break;
					}
				}
			} else {
				// 大于等于情况待处理
			}
		}
		// 计算发卡基础费
		BigDecimal cdtpvl = tranam.multiply(knpUnionCostPara.getBscdrt()).divide(divisor).setScale(2,
				BigDecimal.ROUND_HALF_UP);

		if (!CommUtil.equals(BigDecimal.ZERO, knpUnionCostPara.getCdtpvl())
				&& CommUtil.compare(cdtpvl, knpUnionCostPara.getCdtpvl()) > 0) {
			cdtpvl = knpUnionCostPara.getCdtpvl();
		}

		// 银联网络服务费
		BigDecimal sttpvl = tranam.multiply(knpUnionCostPara.getSvntfe()).divide(divisor).setScale(2,
				BigDecimal.ROUND_HALF_UP);

		if (!CommUtil.equals(BigDecimal.ZERO, knpUnionCostPara.getSttpvl())
				&& CommUtil.compare(sttpvl, knpUnionCostPara.getSttpvl()) > 0) {
			sttpvl = knpUnionCostPara.getSttpvl();
		}

		// 银联品牌服务费
		BigDecimal unbdvl = tranam.multiply(knpUnionCostPara.getUnbdfe()).divide(divisor).setScale(2,
				BigDecimal.ROUND_HALF_UP);

		reanam = cdtpvl.add(sttpvl).add(unbdvl);
		// 初始化返回类型
		UnionCastInfo UnionCastInfo = SysUtil.getInstance(UnionCastInfo.class);
		UnionCastInfo.setBscdam(cdtpvl);
		UnionCastInfo.setSvntam(sttpvl);
		UnionCastInfo.setUnbdam(unbdvl);
		UnionCastInfo.setChrgam(reanam);

		return UnionCastInfo;

	}
}
