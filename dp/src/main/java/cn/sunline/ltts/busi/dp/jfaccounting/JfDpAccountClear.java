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
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_JFCLTP;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountClearInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountClearParam;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountingInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpBusiInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpCommAcctNormInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpInsertSummonsInput;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

/**
 * 即富清算记账处理
 * @author sunline37
 *
 */
public class JfDpAccountClear {

	private final static BizLog bizlog = BizLogUtil.getBizLog(JfDpAccountClear.class);
	/**
	 * 清算记账处理
	 * @param dpAccountClearInput
	 */
	public static void process(DpAccountClearInput dpAccountClearInput) {
		
		JfDpAccountCommCheck.checkAccountClearInput(dpAccountClearInput);
		
		//1.记账参数组装
		DpAccountingInput output = SysUtil.getInstance(DpAccountingInput.class);
		
		getAccountClearParam(dpAccountClearInput, output);
		
		
		//2.记账处理
		JfDpAccountingPublic.commAccounting(output);
		
		//3.登记对账登记簿
		regeditKnsAcsqRgst(dpAccountClearInput);
	}
	
	/**
	 * 组装清算记账参数
	 * @param dpAccountClearInput
	 */
	private static void getAccountClearParam(DpAccountClearInput dpAccountClearInput, DpAccountingInput output) {
		
		JfDpAccountCommCheck.checkAccountClearInput(dpAccountClearInput);
		String  smrycd = "QS";
		List<DpAccountClearParam> lstDpAccountBackParam = dpAccountClearInput.getLstLedger();
		//DpAccountingInput output = SysUtil.getInstance(DpAccountingInput.class);
		Options<DpBusiInput> busiInputList = output.getBusiList(); //内部户记账
		//Options<DpAccountInput> acctInputList = output.getAccountList(); //存款账户记账
		Options<DpInsertSummonsInput> summonsInputList = output.getSummonsInut(); //传票记账
		
		for (DpAccountClearParam dpAccountClearParam : lstDpAccountBackParam) {
			
			bizlog.debug(String.format("清算类型：[%s]", dpAccountClearInput.getClertp().getLongName()));
			bizlog.debug(String.format("记账金额：[%s]", CommUtil.nvl(dpAccountClearParam.getYstram(), BigDecimal.ZERO)));
			bizlog.debug(String.format("记账差额：[%s]", CommUtil.nvl(dpAccountClearParam.getCetram(), BigDecimal.ZERO)));
			
			
			if (dpAccountClearInput.getClertp() == E_JFCLTP._001) {
				//收单清算，
				/**
				 *  Dr	101205 其他货币资金-信用保证金存款	996
					Cr	112202 应收账款-待清算款	996
					Dr	112202 应收账款-待清算款	1
					Cr	60010318-原即付宝/开店宝收入/60010326-自营大POS收入/......	1
					Cr	112202 应收账款-待清算款	1
					Cr	60010318-原即付宝/开店宝收入/60010326-自营大POS收入/......	-1
					如果轧差记账金额为正，则差额计入收入，如为负，则冲抵收入
				 */
				//获取保证金存款业务编码
				String margin = JfDpAccountCommHelper.getMarginSubject(); 
				//获取应收账款-待清算业务编码
				String ysbusi = JfDpAccountCommHelper.getRecvableExpose(dpAccountClearParam.getSvcode()); 
				if (CommUtil.compare(dpAccountClearParam.getYstram(), BigDecimal.ZERO) != 0) {
					DpBusiInput busiDrysInput = SysUtil.getInstance(DpBusiInput.class);
					busiDrysInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
					busiDrysInput.setBusino(margin); //获取待清算业务编码信息
					busiDrysInput.setJfactp(E_JFACTP.ACDR); //内部户借方
					busiDrysInput.setCrcycd(dpAccountClearInput.getCrcycd());//币种
					busiDrysInput.setTranam(dpAccountClearParam.getYstram()); //交易金额取字段应收银联待清算交易金额
					busiInputList.add(busiDrysInput);
					
					DpBusiInput busiCrysInput = SysUtil.getInstance(DpBusiInput.class);
					busiCrysInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
					busiCrysInput.setBusino(ysbusi); //获取待清算业务编码信息
					busiCrysInput.setJfactp(E_JFACTP.ACCR); //内部户借方
					busiCrysInput.setCrcycd(dpAccountClearInput.getCrcycd());//币种
					busiCrysInput.setTranam(dpAccountClearParam.getYstram()); //交易金额取字段应收银联待清算交易金额
					busiCrysInput.setSmrycd(smrycd);
					busiInputList.add(busiCrysInput); 
				}
				
				if (CommUtil.compare(dpAccountClearParam.getCetram(), BigDecimal.ZERO) != 0) {
					if (CommUtil.compare(dpAccountClearParam.getCetram(), BigDecimal.ZERO) > 0) {
						//差额为正金额
						DpBusiInput busiDrceInput = SysUtil.getInstance(DpBusiInput.class);
						busiDrceInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
						busiDrceInput.setBusino(ysbusi); //获取待清算业务编码信息
						busiDrceInput.setJfactp(E_JFACTP.ACDR); //内部户借方
						busiDrceInput.setCrcycd(dpAccountClearInput.getCrcycd());//币种
						busiDrceInput.setTranam(dpAccountClearParam.getCetram()); //交易金额取字段应收银联待清算交易金额
						busiDrceInput.setSmrycd(smrycd);
						busiInputList.add(busiDrceInput);
					} else if (CommUtil.compare(dpAccountClearParam.getCetram(), BigDecimal.ZERO) < 0) {
						//差额为正金额
						DpBusiInput busiCrceInput = SysUtil.getInstance(DpBusiInput.class);
						busiCrceInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
						busiCrceInput.setBusino(ysbusi); //获取待清算业务编码信息
						busiCrceInput.setJfactp(E_JFACTP.ACCR); //内部户借方
						busiCrceInput.setCrcycd(dpAccountClearInput.getCrcycd());//币种
						busiCrceInput.setTranam(dpAccountClearParam.getCetram().abs()); //交易金额取字段应收银联待清算交易金额
						busiCrceInput.setSmrycd(smrycd);
						busiInputList.add(busiCrceInput);
					}
					
					//费用收入登记传票，金额按正负
					KnpParameter febusi = JfDpAccountCommHelper.getExpenseIncomeSubject(dpAccountClearParam.getSbrand());
					DpInsertSummonsInput summonsInput = SysUtil.getInstance(DpInsertSummonsInput.class);
					E_BLNCDN busidn = E_BLNCDN.C;
					
					summonsInput.setBusidn(busidn);
					summonsInput.getAccountingIntf().setAcctno(febusi.getParm_value1());
					summonsInput.getAccountingIntf().setProdcd(febusi.getParm_value3());
					summonsInput.getAccountingIntf().setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());
					summonsInput.getAccountingIntf().setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
					summonsInput.getAccountingIntf().setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
					summonsInput.getAccountingIntf().setAmntcd(E_AMNTCD.CR);
					summonsInput.getAccountingIntf().setDtitcd(febusi.getParm_value3());
					summonsInput.getAccountingIntf().setCrcycd(dpAccountClearInput.getCrcycd());
					summonsInput.getAccountingIntf().setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch());
					summonsInput.getAccountingIntf().setTranam(dpAccountClearParam.getCetram()); //轧差差额
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
				
			} else if (dpAccountClearInput.getClertp() == E_JFCLTP._002){
				/*
				 * 	Dr	64010304（01-17） 品牌名称+资金成本	0.5
					Cr	220206商户应付账款D0	0.5
					Dr	220206商户应付账款D0	990.5
					Cr	101205 其他货币资金-信用保证金存款	990.5
					如有差额资金，则记资金成本
				 */
				
				//获取资金成本垫费业务编码
				String dfbusi = JfDpAccountCommHelper.getCapitalCostSubject(dpAccountClearParam.getSbrand());
				//获取商户应付账款业务编码
				String yfbusi = JfDpAccountCommHelper.getHandleExpose(dpAccountClearParam.getFinsty());
				//获取保证金存款业务编码
				String margin = JfDpAccountCommHelper.getMarginSubject(); //保证金存款业务编码
				
				if (CommUtil.compare(dpAccountClearParam.getCetram(), BigDecimal.ZERO) != 0) {
					DpInsertSummonsInput summonsInput = SysUtil.getInstance(DpInsertSummonsInput.class);
					E_BLNCDN busidn = E_BLNCDN.D;
					
					summonsInput.setBusidn(busidn);
					summonsInput.getAccountingIntf().setAcctno(dfbusi);
					summonsInput.getAccountingIntf().setProdcd(dfbusi);
					summonsInput.getAccountingIntf().setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());
					summonsInput.getAccountingIntf().setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
					summonsInput.getAccountingIntf().setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
					summonsInput.getAccountingIntf().setAmntcd(E_AMNTCD.DR);
					summonsInput.getAccountingIntf().setDtitcd(dfbusi);
					summonsInput.getAccountingIntf().setCrcycd(dpAccountClearInput.getCrcycd());
					summonsInput.getAccountingIntf().setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch());
					summonsInput.getAccountingIntf().setTranam(dpAccountClearParam.getCetram()); //轧差差额
					summonsInput.getAccountingIntf().setToacct(null);
					summonsInput.getAccountingIntf().setToacna(null);
					summonsInput.getAccountingIntf().setTobrch(CommTools.getBaseRunEnvs().getTrxn_branch());
					summonsInput.getAccountingIntf().setAtowtp(E_ATOWTP.IN);
					summonsInput.getAccountingIntf().setTrsqtp(E_ATSQTP.ACCOUNT);
					summonsInput.getAccountingIntf().setBltype(E_BLTYPE.BALANCE);
					summonsInput.getAccountingIntf().setServtp(CommTools.getBaseRunEnvs().getChannel_id());
					summonsInput.getAccountingIntf().setCorpno(CommTools.getBusiOrgId());
				    summonsInputList.add(summonsInput);
				    
				    DpBusiInput busiCrysInput = SysUtil.getInstance(DpBusiInput.class);
					busiCrysInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
					busiCrysInput.setBusino(yfbusi); //获取待清算业务编码信息
					busiCrysInput.setJfactp(E_JFACTP.ACCR); //内部户借方
					busiCrysInput.setCrcycd(dpAccountClearInput.getCrcycd());//币种
					busiCrysInput.setTranam(dpAccountClearParam.getCetram()); //交易金额取字段应收银联待清算交易金额
					busiCrysInput.setSmrycd(smrycd);
					busiInputList.add(busiCrysInput); 
				}
				
				if (CommUtil.compare(dpAccountClearParam.getYstram(), BigDecimal.ZERO) != 0) {
					DpBusiInput busiDrysInput = SysUtil.getInstance(DpBusiInput.class);
					busiDrysInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
					busiDrysInput.setBusino(yfbusi); //获取待清算业务编码信息
					busiDrysInput.setJfactp(E_JFACTP.ACDR); //内部户借方
					busiDrysInput.setCrcycd(dpAccountClearInput.getCrcycd());//币种
					busiDrysInput.setTranam(dpAccountClearParam.getYstram()); //交易金额取字段应收银联待清算交易金额
					busiDrysInput.setSmrycd(smrycd);
					busiInputList.add(busiDrysInput);
					
					DpBusiInput busiCrysInput = SysUtil.getInstance(DpBusiInput.class);
					busiCrysInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
					busiCrysInput.setBusino(margin); //获取待清算业务编码信息
					busiCrysInput.setJfactp(E_JFACTP.ACCR); //内部户借方
					busiCrysInput.setCrcycd(dpAccountClearInput.getCrcycd());//币种
					busiCrysInput.setTranam(dpAccountClearParam.getYstram()); //交易金额取字段应收银联待清算交易金额
					busiDrysInput.setSmrycd(smrycd);
					busiInputList.add(busiCrysInput); 
				}
				
			}
		}
		
	}
	/**
	 * 注册对账登记簿
	 * @param dpAccountClearInput
	 */
	private static void regeditKnsAcsqRgst(DpAccountClearInput dpAccountClearInput) {
		DpCommAcctNormInput dpCommAcctNormInput = SysUtil.getInstance(DpCommAcctNormInput.class);
		
		
		dpCommAcctNormInput.setIschck(dpAccountClearInput.getIschck());
		dpCommAcctNormInput.setChantp(dpAccountClearInput.getChantp());
		dpCommAcctNormInput.setCktrdt(dpAccountClearInput.getCktrdt());
		dpCommAcctNormInput.setCltrdt(dpAccountClearInput.getCltrdt());
		dpCommAcctNormInput.setCltrsq(dpAccountClearInput.getCltrsq());
		dpCommAcctNormInput.setCrcycd(dpAccountClearInput.getCrcycd());
//		dpCommAcctNormInput.setDivdam(dpAccountClearInput.getDivdam());
//		dpCommAcctNormInput.setDivdfe(dpAccountClearInput.getDivdfe());
//		dpCommAcctNormInput.setInmeid(dpAccountClearInput.getInmeid());
		dpCommAcctNormInput.setJftrtp(dpAccountClearInput.getJftrtp());
		dpCommAcctNormInput.setClertp(dpAccountClearInput.getClertp());
//		dpCommAcctNormInput.setMarkfe(dpAccountClearInput.getMarkfe());
//		dpCommAcctNormInput.setOtbkna(dpAccountClearInput.getOtbkna());
//		dpCommAcctNormInput.setPabkno(dpAccountClearInput.getPabkno());
//		dpCommAcctNormInput.setPamena(dpAccountClearInput.getPamena());
//		dpCommAcctNormInput.setPameno(dpAccountClearInput.getPameno());
//		dpCommAcctNormInput.setPosnum(dpAccountClearInput.getPosnum());
		dpCommAcctNormInput.setRemark(CommUtil.nvl(dpAccountClearInput.getRemark(), "清算记账"));
//		dpCommAcctNormInput.setSvcode(dpAccountClearInput.getSvcode());
//		dpCommAcctNormInput.setTranam(dpAccountClearInput.getTranam());
//		dpCommAcctNormInput.setUnamfe(dpAccountClearInput.getUnamfe());
//		dpCommAcctNormInput.setVoucfe(dpAccountClearInput.getVoucfe());
//		dpCommAcctNormInput.setYfmcam(dpAccountClearInput.getYfmcam());
//		dpCommAcctNormInput.setYsunam(dpAccountClearInput.getYsunam());
		
		JfDpAccountingPublic.insertAcsqRgst(dpCommAcctNormInput);
	}
}
