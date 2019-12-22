package cn.sunline.ltts.busi.dp.jfaccounting;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_JFACTP;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_JFCTTP;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountBackInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountBackParam;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountingInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpBusiInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpCommAcctNormInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpInsertSummonsInput;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.type.DpAcctType.AcctSbad;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpUnStopPayIn;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZWY;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

/**
 * 退单记账交易处理
 * @author sunline37
 *
 */
public class JfDpAccountBack {

	
	private final static BizLog bizlog = BizLogUtil.getBizLog(JfDpAccountBack.class);
	/**
	 * 
	 * @param dpAccountBackInput
	 */
	public static void process(DpAccountBackInput dpAccountBackInput) {
		//1.记账参数组装
		DpAccountingInput output = SysUtil.getInstance(DpAccountingInput.class);
		getAccountBackParam(dpAccountBackInput, output);
		
		//2.记账处理
		JfDpAccountingPublic.commAccounting(output);
		
		//3.登记对账登记簿
		regeditKnsAcsqRgst(dpAccountBackInput);
	}
	
	/**
	 * 组装记账参数
	 * @param dpAccountBackInput
	 */
	private static void getAccountBackParam(DpAccountBackInput dpAccountBackInput, DpAccountingInput output) {
		//入参检查及默认值设置
		JfDpAccountCommCheck.checkAccountBackInput(dpAccountBackInput);
		String smrycd = "TD";
		List<DpAccountBackParam> lstDpAccountBackParam = dpAccountBackInput.getLstLedger();
		//DpAccountingInput output = SysUtil.getInstance(DpAccountingInput.class);
		Options<DpBusiInput> busiInputList = output.getBusiList(); //内部户记账
		Options<DpAccountInput> acctInputList = output.getAccountList(); //存款账户记账
		Options<DpInsertSummonsInput> summonsInputList = output.getSummonsInut(); //传票记账
		
		for (DpAccountBackParam dpAccountBackParam : lstDpAccountBackParam) {
			
			if (CommUtil.equals(CommUtil.nvl(dpAccountBackParam.getTranam(), BigDecimal.ZERO), BigDecimal.ZERO)) {
				continue;
			}
			
			bizlog.debug(String.format("退单记账账户类型：[%s]", dpAccountBackParam.getJfcttp().getLongName()));
			bizlog.debug(String.format("退单记账交易金额：[%s]", CommUtil.nvl(dpAccountBackParam.getTranam(), BigDecimal.ZERO)));
			bizlog.debug(String.format("退单记账账户类型：[%s]", dpAccountBackParam.getAmntcd().getLongName()));
			if (dpAccountBackParam.getJfcttp() == E_JFCTTP._001) { 
				//银联成本 支出类科目不记账，登记传票流水
				String busino = JfDpAccountCommHelper.getUnionCostSubject();
				
				DpInsertSummonsInput summonsInput = SysUtil.getInstance(DpInsertSummonsInput.class);
				E_BLNCDN busidn = CommUtil.toEnum(E_BLNCDN.class, dpAccountBackParam.getAmntcd().getValue());
				
				summonsInput.setBusidn(busidn);
				summonsInput.getAccountingIntf().setAcctno(busino);
				summonsInput.getAccountingIntf().setProdcd(busino);
				summonsInput.getAccountingIntf().setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());
				summonsInput.getAccountingIntf().setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
				summonsInput.getAccountingIntf().setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
				summonsInput.getAccountingIntf().setAmntcd(dpAccountBackParam.getAmntcd());
				summonsInput.getAccountingIntf().setDtitcd(busino);
				summonsInput.getAccountingIntf().setCrcycd(dpAccountBackInput.getCrcycd());
				summonsInput.getAccountingIntf().setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch());
				summonsInput.getAccountingIntf().setTranam(dpAccountBackParam.getTranam()); //银联成本金额
				summonsInput.getAccountingIntf().setToacct(null);
				summonsInput.getAccountingIntf().setToacna(null);
				summonsInput.getAccountingIntf().setTobrch(CommTools.getBaseRunEnvs().getTrxn_branch());
				summonsInput.getAccountingIntf().setAtowtp(E_ATOWTP.IN);
				summonsInput.getAccountingIntf().setTrsqtp(E_ATSQTP.ACCOUNT);
				summonsInput.getAccountingIntf().setBltype(E_BLTYPE.BALANCE);
				summonsInput.getAccountingIntf().setServtp(CommTools.getBaseRunEnvs().getChannel_id());
				summonsInput.getAccountingIntf().setCorpno(CommTools.getBusiOrgId());
			    summonsInputList.add(summonsInput);
			} else if (dpAccountBackParam.getJfcttp() == E_JFCTTP._002) {
				//应收账款，根据svcode获取对应应收账款业务编码，进行内部户记账处理
				String busino = JfDpAccountCommHelper.getRecvableExpose(dpAccountBackParam.getSvcode());
				
				DpBusiInput busiInput = SysUtil.getInstance(DpBusiInput.class);
				busiInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
				busiInput.setBusino(busino); //获取待清算业务编码信息
				busiInput.setJfactp(dpAccountBackParam.getAmntcd() == E_AMNTCD.DR ? E_JFACTP.ACDR : E_JFACTP.ACCR); //内部户借方
				busiInput.setCrcycd(dpAccountBackInput.getCrcycd());//币种
				busiInput.setTranam(dpAccountBackParam.getTranam()); //交易金额取字段应收银联待清算交易金额
				busiInput.setSmrycd(smrycd);
				busiInputList.add(busiInput);
			} else if (dpAccountBackParam.getJfcttp() == E_JFCTTP._003) {
				//长款挂账，获取挂账业务编码
				String busino = JfDpAccountCommHelper.getLongPaymentSubject();
				
				DpBusiInput busiInput = SysUtil.getInstance(DpBusiInput.class);
				busiInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
				busiInput.setBusino(busino); //获取待清算业务编码信息
				busiInput.setJfactp(dpAccountBackParam.getAmntcd() == E_AMNTCD.DR ? E_JFACTP.ACDR : E_JFACTP.ACCR); //内部户借方
				busiInput.setCrcycd(dpAccountBackInput.getCrcycd());//币种
				busiInput.setTranam(dpAccountBackParam.getTranam()); //交易金额取字段应收银联待清算交易金额
				busiInput.setSmrycd(smrycd);
				busiInputList.add(busiInput);
			} else if (dpAccountBackParam.getJfcttp() == E_JFCTTP._004 || dpAccountBackParam.getJfcttp() == E_JFCTTP._005) {
				//商户账款，根据商户号获取商户账户
				String inmeid = dpAccountBackParam.getInmeid(); //商户ID/服务商ID
				AcctSbad acctSbad = JfDpAccountCommHelper.getMerchantAcct(inmeid);
				
				IoDpUnStopPayIn dpUnStopPayIn = SysUtil.getInstance(IoDpUnStopPayIn.class);
				dpUnStopPayIn.setCardno(acctSbad.getCardno());
				dpUnStopPayIn.setFrozwy(E_FROZWY.TSOLVE);
				dpUnStopPayIn.setCrcycd(dpAccountBackInput.getCrcycd());
				dpUnStopPayIn.setUnfram(dpAccountBackParam.getTranam());
				dpUnStopPayIn.setOdfrno(dpAccountBackParam.getOdfrno());
				SysUtil.getInstance(IoDpFrozSvcType.class).IoDpUnfrByLaw(dpUnStopPayIn);
				
				// 会计分录二：应付账款-商户存款
				DpAccountInput dpAccountInput = SysUtil.getInstance(DpAccountInput.class);
				dpAccountInput.setAcctno(acctSbad.getAcctno());
				dpAccountInput.setCardno(acctSbad.getCardno());
				dpAccountInput.setCrcycd(dpAccountBackInput.getCrcycd());
				dpAccountInput.setCustac(acctSbad.getCustac());
				dpAccountInput.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));
				dpAccountInput.setJfactp(E_JFACTP.SAVE);
				dpAccountInput.setSmrycd(dpAccountBackInput.getSmrycd());
				dpAccountInput.setTranam(dpAccountBackParam.getTranam());//商户存入金额
				dpAccountInput.setRemark(dpAccountBackInput.getRemark());
				dpAccountInput.setIschck(E_YES___.NO);
				dpAccountInput.setJfactp(dpAccountBackParam.getAmntcd() == E_AMNTCD.DR ? E_JFACTP.DRAW : E_JFACTP.SAVE);
				dpAccountInput.setSmrycd(smrycd);
				acctInputList.add(dpAccountInput);
				
			} else if (dpAccountBackParam.getJfcttp() == E_JFCTTP._006) {
				//费用收入科目
				String sbrand = dpAccountBackParam.getSbrand(); //品牌
				
				KnpParameter tblKnpParameter = JfDpAccountCommHelper.getExpenseIncomeSubject(sbrand);
				
				DpInsertSummonsInput summonsInput = SysUtil.getInstance(DpInsertSummonsInput.class);
				E_BLNCDN busidn = CommUtil.toEnum(E_BLNCDN.class, dpAccountBackParam.getAmntcd().getValue());
				
				summonsInput.setBusidn(busidn);
				summonsInput.getAccountingIntf().setAcctno(tblKnpParameter.getParm_value3());
				summonsInput.getAccountingIntf().setProdcd(tblKnpParameter.getParm_value3());
				summonsInput.getAccountingIntf().setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());
				summonsInput.getAccountingIntf().setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
				summonsInput.getAccountingIntf().setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
				summonsInput.getAccountingIntf().setAmntcd(dpAccountBackParam.getAmntcd());
				summonsInput.getAccountingIntf().setDtitcd(tblKnpParameter.getParm_value3());
				summonsInput.getAccountingIntf().setCrcycd(dpAccountBackInput.getCrcycd());
				summonsInput.getAccountingIntf().setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch());
				summonsInput.getAccountingIntf().setTranam(dpAccountBackParam.getTranam()); //银联成本金额
				summonsInput.getAccountingIntf().setToacct(null);
				summonsInput.getAccountingIntf().setToacna(null);
				summonsInput.getAccountingIntf().setTobrch(CommTools.getBaseRunEnvs().getTrxn_branch());
				summonsInput.getAccountingIntf().setAtowtp(E_ATOWTP.IN);
				summonsInput.getAccountingIntf().setTrsqtp(E_ATSQTP.ACCOUNT);
				summonsInput.getAccountingIntf().setBltype(E_BLTYPE.BALANCE);
				summonsInput.getAccountingIntf().setServtp(CommTools.getBaseRunEnvs().getChannel_id());
				summonsInput.getAccountingIntf().setCorpno(CommTools.getBusiOrgId());
			    summonsInputList.add(summonsInput);
			}
		}
		
	}
	
	/**
	 * 注册对账登记簿
	 * @param dpAccountClearInput
	 */
	private static void regeditKnsAcsqRgst(DpAccountBackInput dpAccountBackInput) {
		DpCommAcctNormInput dpCommAcctNormInput = SysUtil.getInstance(DpCommAcctNormInput.class);
		
		
		dpCommAcctNormInput.setIschck(dpAccountBackInput.getIschck());
		dpCommAcctNormInput.setChantp(dpAccountBackInput.getChantp());
		dpCommAcctNormInput.setCktrdt(dpAccountBackInput.getCktrdt());
		dpCommAcctNormInput.setCltrdt(dpAccountBackInput.getCltrdt());
		dpCommAcctNormInput.setCltrsq(dpAccountBackInput.getCltrsq());
		dpCommAcctNormInput.setCrcycd(dpAccountBackInput.getCrcycd());
//		dpCommAcctNormInput.setDivdam(dpAccountClearInput.getDivdam());
//		dpCommAcctNormInput.setDivdfe(dpAccountClearInput.getDivdfe());
//		dpCommAcctNormInput.setInmeid(dpAccountClearInput.getInmeid());
		dpCommAcctNormInput.setJftrtp(dpAccountBackInput.getJftrtp());
//		dpCommAcctNormInput.setClertp(dpAccountBackInput.getClertp());
//		dpCommAcctNormInput.setMarkfe(dpAccountClearInput.getMarkfe());
//		dpCommAcctNormInput.setOtbkna(dpAccountClearInput.getOtbkna());
//		dpCommAcctNormInput.setPabkno(dpAccountClearInput.getPabkno());
//		dpCommAcctNormInput.setPamena(dpAccountClearInput.getPamena());
//		dpCommAcctNormInput.setPameno(dpAccountClearInput.getPameno());
//		dpCommAcctNormInput.setPosnum(dpAccountClearInput.getPosnum());
		dpCommAcctNormInput.setRemark(CommUtil.nvl(dpAccountBackInput.getRemark(), "清算记账"));
//		dpCommAcctNormInput.setSvcode(dpAccountClearInput.getSvcode());
//		dpCommAcctNormInput.setTranam(dpAccountClearInput.getTranam());
//		dpCommAcctNormInput.setUnamfe(dpAccountClearInput.getUnamfe());
//		dpCommAcctNormInput.setVoucfe(dpAccountClearInput.getVoucfe());
//		dpCommAcctNormInput.setYfmcam(dpAccountClearInput.getYfmcam());
//		dpCommAcctNormInput.setYsunam(dpAccountClearInput.getYsunam());
		
		JfDpAccountingPublic.insertAcsqRgst(dpCommAcctNormInput);
	}
}
