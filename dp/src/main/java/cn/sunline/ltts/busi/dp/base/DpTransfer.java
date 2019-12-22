package cn.sunline.ltts.busi.dp.base;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SERVCD;
import cn.sunline.edsp.busi.ds.iobus.type.DsEnumType.E_BUSITP;

/**
 * 字段转换相关方法
 * 
 * @author sunzy
 *
 */
public class DpTransfer {
	/**
	 * 服务码转换成业务编码
	 * 
	 * @param servcd
	 * @return E_BUSITP
	 */
	public static E_BUSITP svcodeToBusitp(E_SERVCD servcd) {
		if (CommUtil.isNull(servcd)) {
			return null;
		}

		E_BUSITP busitp = null;
		String svcode = servcd.getValue();
		switch (svcode) {
		case "10001":
			busitp = E_BUSITP._1;
			break;
		case "10004":
			busitp = E_BUSITP._2;
			break;
		case "30002":
			busitp = E_BUSITP._3;
			break;
		case "10002":
			busitp = E_BUSITP._4;
			break;
		case "10005":
			busitp = E_BUSITP._6;
			break;
		case "60001":
			busitp = E_BUSITP._7;
			break;
		case "60002":
			busitp = E_BUSITP._8;
			break;
		case "60003":
			busitp = E_BUSITP._9;
			break;
		case "60004":
			busitp = E_BUSITP._10;
			break;
		case "X0000":
			busitp = E_BUSITP._11;
			break;
		case "50004":
			busitp = E_BUSITP._12;
			break;
		case "40001":
			busitp = E_BUSITP._12;
			break;
		case "10003":
			busitp = E_BUSITP._15;
			break;
		case "50003":
			busitp = E_BUSITP._1;
			break;
		}

		return busitp;

	}
}
