package cn.sunline.ltts.busi.dp.jfaccounting;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_JFCPTP;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_JFTRTP;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountBackInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountBackParam;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountClearInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpCommAcctNormInput;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

public class JfDpAccountCommCheck {

	/**
	 * 退单记账交易接口输入参数检查
	 * @param dpAccountBackInput
	 */
	public static void checkAccountBackInput(DpAccountBackInput dpAccountBackInput) {
		
		if (CommUtil.isNull(dpAccountBackInput.getCltrsq())) {
			throw DpModuleError.DpTrans.TS010064(); //清结算流水不能为空
		}
		
		if (CommUtil.isNull(dpAccountBackInput.getCltrdt())) {
			throw DpModuleError.DpTrans.TS010065(); //清结算日期不能为空
		}
		
		if (CommUtil.isNull(dpAccountBackInput.getIschck())) {
			dpAccountBackInput.setIschck(E_YES___.YES);
		}
		
		if (CommUtil.isNull(dpAccountBackInput.getCktrdt())) {
			throw DpModuleError.DpTrans.TS010067();  //对账日期不能为空
		}
		
		if (CommUtil.isNull(dpAccountBackInput.getJftrtp()) || dpAccountBackInput.getJftrtp() != E_JFTRTP.jfacbk) {
			throw DpModuleError.DpTrans.TS010068(); //记账交易类型不匹配
		}
		
		if (CommUtil.isNull(dpAccountBackInput.getCrcycd())) {
			dpAccountBackInput.setCrcycd("CNY");
		}
		
		//借贷平衡检查
		if (CommUtil.isNull(dpAccountBackInput.getLstLedger()) || dpAccountBackInput.getLstLedger().size() <= 0) {
			throw DpModuleError.DpTrans.TS010074(); //记账复核类型列表不能为空
		}
		
		BigDecimal debitAmt = BigDecimal.ZERO;
		BigDecimal creditAmt = BigDecimal.ZERO;
		for (DpAccountBackParam dpAccountBackParam : dpAccountBackInput.getLstLedger()) {
			BigDecimal amt = CommUtil.nvl(dpAccountBackParam.getTranam(), BigDecimal.ZERO);
			if (dpAccountBackParam.getAmntcd() == E_AMNTCD.DR) {
				debitAmt = debitAmt.add(amt);
			} else if (dpAccountBackParam.getAmntcd() == E_AMNTCD.CR) {
				creditAmt = creditAmt.add(amt);
			} else {
				throw DpModuleError.DpTrans.TS020079();
			}
		}
		
		if (!CommUtil.equals(debitAmt, creditAmt)) {
			throw DpModuleError.DpTrans.TS020080(); //借方与贷方金额不相等
		}
		
	}
	
	
	/**
	 * 清算记账参数基础检查
	 * @param dpAccountClearInput
	 */
	public static void checkAccountClearInput(DpAccountClearInput dpAccountClearInput) {
		if (CommUtil.isNull(dpAccountClearInput.getCltrsq())) {
			throw DpModuleError.DpTrans.TS010064(); //清结算流水不能为空
		}
		
		if (CommUtil.isNull(dpAccountClearInput.getCltrdt())) {
			throw DpModuleError.DpTrans.TS010065(); //清结算日期不能为空
		}
		
		if (CommUtil.isNull(dpAccountClearInput.getIschck())) {
			dpAccountClearInput.setIschck(E_YES___.YES);
		}
		
		if (CommUtil.isNull(dpAccountClearInput.getCktrdt())) {
			throw DpModuleError.DpTrans.TS010067();  //对账日期不能为空
		}
		
		if (CommUtil.isNull(dpAccountClearInput.getJftrtp()) || dpAccountClearInput.getJftrtp() != E_JFTRTP.jfcler) {
			throw DpModuleError.DpTrans.TS010068(); //记账交易类型不匹配
		}
		
		if (CommUtil.isNull(dpAccountClearInput.getCrcycd())) {
			dpAccountClearInput.setCrcycd("CNY");
		}
	}
	
	/**
	 * 收单记账参数基础检查
	 * @param DpCommAcctNormInput
	 */
	public static void checkAccountAcquireInput(DpCommAcctNormInput dpCommAcctNormInput) {
		if (CommUtil.isNull(dpCommAcctNormInput.getCltrsq())) {
			throw DpModuleError.DpTrans.TS010064(); //清结算流水不能为空
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getCltrdt())) {
			throw DpModuleError.DpTrans.TS010065(); //清结算日期不能为空
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getIschck())) {
			dpCommAcctNormInput.setIschck(E_YES___.YES);
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getCktrdt())) {
			throw DpModuleError.DpTrans.TS010067();  //对账日期不能为空
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getCrcycd())) {
			dpCommAcctNormInput.setCrcycd("CNY");
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getInmeid())) {// 内部商户号
			throw DpModuleError.DpTrans.TS010070();
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getFinsty())) {
			throw DpModuleError.DpTrans.TS010069();
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getChantp())) {// 交易渠道
			throw DpModuleError.DpTrans.TS010072();
		}
		if (CommUtil.isNull(dpCommAcctNormInput.getSvcode())) {// 服务编码
			throw DpModuleError.DpTrans.TS010073();
		}
	}
	
	/**
	 * 代发记账参数基础检查
	 * @param DpCommAcctNormInput
	 */
	public static void checkAccountSendInput(DpCommAcctNormInput dpCommAcctNormInput) {
		if (CommUtil.isNull(dpCommAcctNormInput.getCltrsq())) {
			throw DpModuleError.DpTrans.TS010064(); //清结算流水不能为空
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getCltrdt())) {
			throw DpModuleError.DpTrans.TS010065(); //清结算日期不能为空
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getIschck())) {
			dpCommAcctNormInput.setIschck(E_YES___.YES);
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getCktrdt())) {
			throw DpModuleError.DpTrans.TS010067();  //对账日期不能为空
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getJftrtp()) || dpCommAcctNormInput.getJftrtp() != E_JFTRTP.jfoblf) {
			throw DpModuleError.DpTrans.TS010068(); //记账交易类型不匹配
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getCrcycd())) {
			dpCommAcctNormInput.setCrcycd("CNY");
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getInmeid())) {// 内部商户号
			throw DpModuleError.DpTrans.TS010070();
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getFinsty())) {//结算类型
			throw DpModuleError.DpTrans.TS010069();  
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getChantp())) {// 交易渠道
			throw DpModuleError.DpTrans.TS010072();
		}
	}
	
	/**
	 * 挂账记账参数基础检查
	 * @param DpCommAcctNormInput
	 */
	public static void checkAccountPendingInput(DpCommAcctNormInput dpCommAcctNormInput) {
		if (CommUtil.isNull(dpCommAcctNormInput.getCltrsq())) {
			throw DpModuleError.DpTrans.TS010064(); //清结算流水不能为空
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getCltrdt())) {
			throw DpModuleError.DpTrans.TS010065(); //清结算日期不能为空
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getIschck())) {
			dpCommAcctNormInput.setIschck(E_YES___.YES);
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getCktrdt())) {
			throw DpModuleError.DpTrans.TS010067();  //对账日期不能为空
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getJftrtp()) || dpCommAcctNormInput.getJftrtp() != E_JFTRTP.jfoblf) {
			throw DpModuleError.DpTrans.TS010068(); //记账交易类型不匹配
		}
		if (CommUtil.isNull(dpCommAcctNormInput.getBooktp()) || dpCommAcctNormInput.getBooktp() != E_JFCPTP._001) {
			throw DpModuleError.DpTrans.TS010075(); //挂账类型类型不匹配
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getCrcycd())) {
			dpCommAcctNormInput.setCrcycd("CNY");
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getInmeid())) {// 内部商户号
			throw DpModuleError.DpTrans.TS010070();
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getChantp())) {// 交易渠道
			throw DpModuleError.DpTrans.TS010072();
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getSvcode())) {// 服务编码
			throw DpModuleError.DpTrans.TS010073();
		}
	}
	
	/**
	 * 首次刷卡记账参数基础检查
	 * @param DpCommAcctNormInput
	 */
	public static void checkAccountReturnsInput(DpCommAcctNormInput dpCommAcctNormInput) {
		if (CommUtil.isNull(dpCommAcctNormInput.getCltrsq())) {
			throw DpModuleError.DpTrans.TS010064(); //清结算流水不能为空
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getCltrdt())) {
			throw DpModuleError.DpTrans.TS010065(); //清结算日期不能为空
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getIschck())) {
			dpCommAcctNormInput.setIschck(E_YES___.YES);
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getCktrdt())) {
			throw DpModuleError.DpTrans.TS010067();  //对账日期不能为空
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getJftrtp()) || dpCommAcctNormInput.getJftrtp() != E_JFTRTP.jfscsk) {
			throw DpModuleError.DpTrans.TS010068(); //记账交易类型不匹配
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getCrcycd())) {
			dpCommAcctNormInput.setCrcycd("CNY");
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getInmeid())) {// 内部商户号
			throw DpModuleError.DpTrans.TS010070();
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getChantp())) {// 交易渠道
			throw DpModuleError.DpTrans.TS010072();
		}
		
		if (CommUtil.isNull(dpCommAcctNormInput.getSvcode())) {// 服务编码
			throw DpModuleError.DpTrans.TS010073();
		}
	}
	
}
