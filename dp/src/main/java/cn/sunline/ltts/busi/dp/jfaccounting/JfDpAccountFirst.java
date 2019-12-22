package cn.sunline.ltts.busi.dp.jfaccounting;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_JFACTP;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_JFTRTP;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountBackInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountingInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpBusiInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpCommAcctNormInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpInsertSummonsInput;
import cn.sunline.ltts.busi.dp.type.DpAcctType.AcctSbad;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

/**
 * 首次刷单记账交易处理
 * 
 * @author sunline37
 *
 */
public class JfDpAccountFirst {

	private final static BizLog bizlog = BizLogUtil.getBizLog(JfDpAccountFirst.class);

	/**
	 * 
	 * @param dpCommAcctNormInput
	 */
	public static void process(DpCommAcctNormInput dpCommAcctNormInput) {
		// 1.记账参数组装
		DpAccountingInput output = SysUtil.getInstance(DpAccountingInput.class);
		getAccountFirstParam(dpCommAcctNormInput, output);

		// 2.记账处理
		JfDpAccountingPublic.commAccounting(output);

		// 3.登记对账登记簿
		JfDpAccountingPublic.insertAcsqRgst(dpCommAcctNormInput);
	}

	/**
	 * 组装记账参数
	 *  Dr 112202应收账款-待清算款  300 
	 *  Cr 600110服务费收入（押金）  298 
	 *  Cr 112202 应收账款-待清算（银联成本） 2
	 * 
	 * @param dpAccountReturnsInput
	 */
	private static void getAccountFirstParam(DpCommAcctNormInput dpCommAcctNormInput, DpAccountingInput output) {
		// 入参检查及默认值设置
		JfDpAccountCommCheck.checkAccountReturnsInput(dpCommAcctNormInput);

		String smrycd = "FD";
		// 获取应收账款业务编号
		String recvableBusino = JfDpAccountCommHelper.getRecvableExpose(dpCommAcctNormInput.getSvcode());
		// 获取商户信息
		AcctSbad acctSbad = JfDpAccountCommHelper.getMerchantAcct(dpCommAcctNormInput.getInmeid());
		// 根据品牌编号获取服务费收入业务编码信息
		KnpParameter serviceBusi = JfDpAccountCommHelper.getServiceChargesSubject(acctSbad.getSbrand());

		// 会计分录一：Dr 应收账款-待清算款-银联 记账信息赋值
		DpBusiInput busiInput = SysUtil.getInstance(DpBusiInput.class);
		busiInput.setBusino(recvableBusino); // 应收账款业务编号
		busiInput.setJfactp(E_JFACTP.ACDR); // 内部户借方
		busiInput.setCrcycd(dpCommAcctNormInput.getCrcycd());// 币种
		busiInput.setTranam(dpCommAcctNormInput.getYsunam()); // 交易金额取字段【应收银联待清算交易金额】
		busiInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
		busiInput.setToacct(dpCommAcctNormInput.getPabkno());
		busiInput.setToacna(dpCommAcctNormInput.getPabkna());
		busiInput.setSmrycd(smrycd);
		output.getBusiList().add(busiInput);

		// 会计分录二： Cr 服务费收入（押金）
		DpInsertSummonsInput summonsIncome = SysUtil.getInstance(DpInsertSummonsInput.class);
		summonsIncome.getAccountingIntf().setAcctno(serviceBusi.getParm_value1());
		summonsIncome.getAccountingIntf().setProdcd(serviceBusi.getParm_value1());
		summonsIncome.getAccountingIntf().setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());
		summonsIncome.getAccountingIntf().setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		summonsIncome.getAccountingIntf().setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		summonsIncome.getAccountingIntf().setAmntcd(E_AMNTCD.CR);
		summonsIncome.getAccountingIntf().setDtitcd(serviceBusi.getParm_value1());
		summonsIncome.getAccountingIntf().setCrcycd(dpCommAcctNormInput.getCrcycd());
		summonsIncome.getAccountingIntf().setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch());
		summonsIncome.getAccountingIntf().setToacct(dpCommAcctNormInput.getPabkno()); // 交易对手【支付银行卡账号】
		summonsIncome.getAccountingIntf().setTranam(dpCommAcctNormInput.getDivdam()); // 交易金额取字段 【分润费用收入】
		summonsIncome.getAccountingIntf().setToacna(dpCommAcctNormInput.getPabkna()); // 交易对手【支付银行卡名称】
		// summonsIncome.getAccountingIntf().setTobrch(dpCommAcctNormInput.getOtbkna()); // 对方交易机构【支付银行卡名称】
		summonsIncome.getAccountingIntf().setAtowtp(E_ATOWTP.IN);
		summonsIncome.getAccountingIntf().setTrsqtp(E_ATSQTP.ACCOUNT);
		summonsIncome.getAccountingIntf().setBltype(E_BLTYPE.BALANCE);
		summonsIncome.getAccountingIntf().setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		summonsIncome.getAccountingIntf().setCorpno(CommTools.getBusiOrgId());
		summonsIncome.setBusidn(E_BLNCDN.get(serviceBusi.getParm_value2()));
		output.getSummonsInut().add(summonsIncome);

		// 会计分录三：Cr 应收账款-待清算款
		busiInput = SysUtil.getInstance(DpBusiInput.class);
		busiInput.setBusino(recvableBusino); // 应收账款业务编号
		busiInput.setJfactp(E_JFACTP.ACCR); // 内部户贷方
		busiInput.setCrcycd(dpCommAcctNormInput.getCrcycd());// 币种
		busiInput.setTranam(dpCommAcctNormInput.getUnamfe()); // 交易金额取字段【冲抵应收账款交易金额(银联手续费)】
		busiInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
		busiInput.setToacct(dpCommAcctNormInput.getPabkno());
		busiInput.setToacna(dpCommAcctNormInput.getPabkna());
		busiInput.setSmrycd(smrycd);
		output.getBusiList().add(busiInput);

	}
}
