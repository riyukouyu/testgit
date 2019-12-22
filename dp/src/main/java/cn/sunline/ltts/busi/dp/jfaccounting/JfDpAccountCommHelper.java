package cn.sunline.ltts.busi.dp.jfaccounting;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_FINSTY;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_RETYPE;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SERVCD;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.AcctSbad;

public class JfDpAccountCommHelper {

	/**
	 * 获取应收账款业务编号
	 * @param svcode 服务码
	 * @return
	 */
	public static String getRecvableExpose(E_SERVCD svcode) {
		E_RETYPE servtp = null;
		//新增预授权完成服务码判断
		if ((E_SERVCD.PAY10001 == svcode) || (E_SERVCD.PAY10003 == svcode)
				|| (E_SERVCD.PAY10005 == svcode) || (E_SERVCD.PAY50003 == svcode)) {
			servtp = E_RETYPE.CUPSZZ;
		} else if (E_SERVCD.PAY10004 == svcode) {
			servtp = E_RETYPE.ALIWKZZ;
		} else if (E_SERVCD.PAY10002 == svcode) {
			servtp = E_RETYPE.WXINWKZZ;
		} else {
			throw DpModuleError.DpTrans.TS020060();
		}
		// 业务编码查询赋值
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", servtp.getId(), "%", "%", false);
		if (CommUtil.isNull(knpParameter) || CommUtil.isNull(knpParameter.getParm_value1())) {
			throw DpModuleError.DpTrans.TS020021();
		}
		
		return knpParameter.getParm_value1();
	}
	/**
	 * 获取应付商户款项业务编号
	 * @param finsty 结算类型 D0，T1
	 * @return
	 */
	public static String getHandleExpose(E_FINSTY finsty) {
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", finsty.getValue(), "%", "%", false);
		if (CommUtil.isNull(knpParameter)) {
			throw DpModuleError.DpTrans.TS020021();
		}
		return knpParameter.getParm_value1();
	}
	
	/**
	 * 获取银联成本业务编号
	 * @return
	 */
	public static String getUnionCostSubject() {
		
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", "UnionCost", "%", "%", false);
		if (CommUtil.isNull(knpParameter) || CommUtil.isNull(knpParameter.getParm_value1())) {
			throw DpModuleError.DpTrans.TS020021();
		}
		
		return knpParameter.getParm_value1();
	}
	
	/**
	 * 获取长款挂账业务编码
	 * @return
	 */
	public static String getLongPaymentSubject() {
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", "LongPayment", "%", "%", false);
		if (CommUtil.isNull(knpParameter) || CommUtil.isNull(knpParameter.getParm_value1())) {
			throw DpModuleError.DpTrans.TS020021();
		}
		
		return knpParameter.getParm_value1();
	}
	
	/**
	 * 获取其他货币资金-信用保证金存款业务编码
	 * @return
	 */
	public static String getMarginSubject() {
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", "Margin", "%", "%", false);
		if (CommUtil.isNull(knpParameter) || CommUtil.isNull(knpParameter.getParm_value1())) {
			throw DpModuleError.DpTrans.TS020021();
		}
		
		return knpParameter.getParm_value1();
	}
	
	
	/**
	 * 获取品牌名称+资金成本业务编码
	 * @param sbrand 品牌号
	 * @return
	 */
	public static String getCapitalCostSubject(String sbrand) {
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", "CapitalCost", sbrand, "%", false);
		if (CommUtil.isNull(knpParameter) || CommUtil.isNull(knpParameter.getParm_value1())) {
			throw DpModuleError.DpTrans.TS020021();
		}
		
		return knpParameter.getParm_value1();
	}
	
	/**
	 * 获取品牌服务费收入业务编码
	 * @param sbrand 品牌号
	 * @return
	 */
	public static KnpParameter getServiceChargesSubject(String sbrand) {
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", "ServiceCharges", sbrand, "%", false);
		if (CommUtil.isNull(knpParameter) || CommUtil.isNull(knpParameter.getParm_value1())) {
			throw DpModuleError.DpTrans.TS020021();
		}
		
		return knpParameter;
	}
	
	/**
	 * 根据品牌编号获取费用收入业务编码
	 * @param sbrand 品牌号
	 * @return
	 */
	public static KnpParameter getExpenseIncomeSubject(String sbrand) {
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", sbrand, "%", "%", false);
		if (CommUtil.isNull(knpParameter)) {
			throw DpModuleError.DpTrans.TS020021();
		}
		return knpParameter;
	}
	/**
	 * 获取商户/服务商账户信息
	 * @param inmeid 商户号/服务商ID
	 * @return AcctSbad
	 */
	public static AcctSbad getMerchantAcct(String inmeid) {
		
		AcctSbad acctSbad = SysUtil.getInstance(AcctSbad.class);
		
		KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(inmeid, false);
		KnaAcdc knaAcdc = SysUtil.getInstance(KnaAcdc.class);
		KnaAcct knaAcct = SysUtil.getInstance(KnaAcct.class);
		if (CommUtil.isNotNull(tblKnaSbad)) {
			knaAcdc = KnaAcdcDao.selectFirst_odb3(tblKnaSbad.getCustac(), false);
			knaAcct = KnaAcctDao.selectOne_odb1(tblKnaSbad.getAcctno(), false);
		}else {
			throw DpModuleError.DpTrans.TS020027();
		}
		
		acctSbad.setCustac(knaAcdc.getCustac());
		acctSbad.setCardno(knaAcdc.getCardno());
		acctSbad.setAcctno(knaAcct.getAcctno());
		acctSbad.setSbrand(tblKnaSbad.getSbrand()); //品牌
		
		return acctSbad;
	}
}
