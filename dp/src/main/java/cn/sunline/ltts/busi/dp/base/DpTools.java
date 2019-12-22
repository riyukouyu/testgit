package cn.sunline.ltts.busi.dp.base;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;

public class DpTools {

	public final static String OPENPAY_O_URL = "/openpay/merchantFile/pos_sales_slip";

	public final static String OPENPAY_N_URL = "/home/app/share/NGPFILE/STORAGE/merchantFile/Bpos";

	public final static String MERCHANT_O_URL = "/KaiFangZhiFu_v2/img/MERCHANT";

	public final static String MERCHANT_N_URL = "/home/app/share/NGPFILE/STORAGE/merchantFile/Mpos";
	
	public final static String OPENPAY_O_U_URL = "/home/app/cache/pos_sales_slip"; 

	public final static String MERCHANT_O_U_URL = "/home/fs/openpay-file/KaiFangZhiFu_v2/img/MERCHANT";
	
	/**
	 * 通过key和长度生成序列
	 * 
	 * @param key
	 * @param len
	 * @return 生成规则当天日期加上Key生成序列
	 */
	public static String genSequenceWithTrandt(String key, int len) {
		StringBuffer sequNo = new StringBuffer();

		return sequNo.append(CommTools.getBaseRunEnvs().getComputer_date()).append(BusiTools.getSequence(key, len))
				.toString();
	}

	/**
	 * 地址替换
	 * 
	 * @param signUrl
	 * @return
	 */
	public static String repUrl(String signUrl) {
		if (CommUtil.isNull(signUrl)) {
			return null;
		}
		if (signUrl.indexOf(OPENPAY_O_URL) >= 0) {
			signUrl = signUrl.replace(OPENPAY_O_URL, OPENPAY_N_URL);
		} else if (signUrl.indexOf(MERCHANT_O_URL) == 0) {
			signUrl = signUrl.replace(MERCHANT_O_URL, MERCHANT_N_URL);
		}else if(signUrl.indexOf(OPENPAY_O_U_URL) >= 0) {
			signUrl = signUrl.replace(OPENPAY_O_U_URL, OPENPAY_N_URL);
		}else if (signUrl.indexOf(MERCHANT_O_U_URL)>= 0) {
			signUrl = signUrl.replace(MERCHANT_O_U_URL, MERCHANT_N_URL);
		}
		return signUrl;
	}
	
	public static void main(String[] args) {
		System.out.println(repUrl("/KaiFangZhiFu_v2/img/MERCHANT/asdfsafd"));
	}
}
