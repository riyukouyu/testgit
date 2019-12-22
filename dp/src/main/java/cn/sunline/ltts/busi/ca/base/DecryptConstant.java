package cn.sunline.ltts.busi.ca.base;

import com.jfpal.legends.security.EncryptTool;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.busi.dp.reversal.SaveActoacPeerReversal;
import cn.sunline.ltts.apollo.ApolloConfigBean;

/**
 * 加密公共常量
 * 
 * @author sunzy
 *
 */
public class DecryptConstant {
	private static final BizLog bizlog = BizLogUtil.getBizLog(DecryptConstant.class);
	
	public final static String DECRYPT_KEY = ApolloConfigBean.getInstance().getSecretKey();

	public final static String LEGENDS_IP = ApolloConfigBean.getInstance().getEncryptIp();

	public final static String LEGENDS_KEYX = ApolloConfigBean.getInstance().getEncryptKeyX();

	public final static String LEGENDS_KEYY = ApolloConfigBean.getInstance().getEncryptKeyY();

	/**
	 * 名称脱敏
	 * 
	 * @param encryptedText
	 * @return
	 */
	public static String maskName(String encryptedText) {
		String maskName = null;
		if (CommUtil.isNull(encryptedText)) {
			return maskName;
		}
		try {
			maskName = EncryptTool.maskName(encryptedText, DECRYPT_KEY);
		} catch (Exception e) {
			bizlog.debug("[%s]", e);
//			e.printStackTrace();
		}
		return maskName;
	}

	/**
	 * 证件号码脱敏
	 * 
	 * @param encryptedText
	 * @return
	 */
	public static String maskIdCard(String encryptedText) {
		String maskIdCard = null;
		if (CommUtil.isNull(encryptedText)) {
			return maskIdCard;
		}
		try {
			maskIdCard = EncryptTool.maskIdCard(encryptedText, DECRYPT_KEY);
		} catch (Exception e) {
			bizlog.debug("[%s]", e);
//			e.printStackTrace();
		}
		return maskIdCard;
	}

	/**
	 * 手机号脱敏
	 * 
	 * @param encryptedText
	 * @return
	 */
	public static String maskMobile(String encryptedText) {
		String maskMobile = null;
		if (CommUtil.isNull(encryptedText)) {
			return maskMobile;
		}
		try {
			maskMobile = EncryptTool.maskMobile(encryptedText, DECRYPT_KEY);
		} catch (Exception e) {
			bizlog.debug("[%s]", e);
//			e.printStackTrace();
		}
		return maskMobile;
	}

	/**
	 * 银行卡号脱敏
	 * 
	 * @param encryptedText
	 * @return
	 */
	public static String maskBankCard(String encryptedText) {
		String maskBankCard = null;
		if (CommUtil.isNull(encryptedText)) {
			return maskBankCard;
		}
		try {
			maskBankCard = EncryptTool.maskBankCard(encryptedText, DECRYPT_KEY);
		} catch (Exception e) {
			bizlog.debug("[%s]", e);
//			e.printStackTrace();
		}
		return maskBankCard;
	}

	/**
	 * 加密
	 * 
	 * @param encryptedText
	 * @return
	 */
	public static String encrypt(String encryptedText) {
		String encryptText = null;
		if (CommUtil.isNull(encryptedText)) {
			return encryptText;
		}
		try {
			encryptText = EncryptTool.encrypt(encryptedText, DECRYPT_KEY);
		} catch (Exception e) {
			bizlog.debug("[%s]", e);
//			e.printStackTrace();
		}
		return encryptText;
	}

	/**
	 * 解密
	 * 
	 * @param encryptedText
	 * @return
	 */
	public static String decrypt(String encryptedText) {
		String decryptText = null;
		if (CommUtil.isNull(encryptedText)) {
			return decryptText;
		}
		try {
			decryptText = EncryptTool.decrypt(encryptedText, DECRYPT_KEY);
		} catch (Exception e) {
			bizlog.debug("[%s]", e);
//			e.printStackTrace();
		}
		return decryptText;
	}

	/**
	 * 邮箱脱敏
	 * 
	 * @param encryptedText
	 * @return
	 */
	public static String maskEmail(String encryptedText) {
		String maskEmail = null;
		if (CommUtil.isNull(encryptedText)) {
			return maskEmail;
		}
		try {
			maskEmail = EncryptTool.maskEmail(encryptedText, DECRYPT_KEY);
		} catch (Exception e) {
			bizlog.debug("[%s]", e);
//			e.printStackTrace();
		}
		return maskEmail;
	}

	/**
	 * 转加密公共方法
	 * 
	 * @param convertStr
	 * @return
	 */
	public static String convertSm4(String convertStr) {
		String sm4Str = null;
		if (CommUtil.isNull(convertStr)) {
			return sm4Str;
		}
		try {
			sm4Str = EncryptTool.jfpalConvertSm4(convertStr, DECRYPT_KEY);
		} catch (Exception e) {
			bizlog.debug("[%s]", e);
//			e.printStackTrace();
		}
		return sm4Str;
	}
}
