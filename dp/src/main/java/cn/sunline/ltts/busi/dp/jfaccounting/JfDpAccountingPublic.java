package cn.sunline.ltts.busi.dp.jfaccounting;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_EVENTLEVEL;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_YESORNO;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CHANTP;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_FINSTY;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_JFACTP;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_JFTRTP;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountingInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpBusiInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpCommAcctNormInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpInsertSummonsInput;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqRgst;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqRgstDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.AcctSbad;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

public class JfDpAccountingPublic {
	private final static BizLog bizlog = BizLogUtil.getBizLog(JfDpAccountingPublic.class);
	/**
	 * 存款改造，通用记账处理
	 * @author liunan
	 * @date 2019-12-09 13:44
	 * @param custac 电子账号
	 * @return E_FROZST 冻结状态
	 */
	public static void jfCommAcctDeal(DpCommAcctNormInput input){
		
		//调用方法，将输入信息组装为通用记账标准输入格式
		DpAccountingInput  dpAccountingInput = SysUtil.getInstance(DpAccountingInput.class);
		String jftrtp = input.getJftrtp().getValue();
		switch (jftrtp) {
		case "001":
			//收单
			if(input.getFinsty() == E_FINSTY.D0) {
				input.setSmrycd("D0");
			}else {
				input.setSmrycd("S1");
			}
			dpAccountingInput = getJfacqrAcctInput(input);
			break;
		case "002":
			//代发
			input.setSmrycd("F1");
			dpAccountingInput = getJfoblfAcctInput(input);
			break;
		case "003":
			//挂账
			input.setSmrycd("GZ");
			dpAccountingInput = getJfbookAcctInput(input);
			break;
		case "009":
			input.setSmrycd("TP");
			dpAccountingInput = getJfacqrAcctInput(input);
			break;
		default:
			//记账交易类型错误
			throw DpModuleError.DpTrans.TS020078();
		}
		
		//调用通用记账方法
		commAccounting(dpAccountingInput);
		
		//登记登记簿
		insertAcsqRgst(input);
		
	}
	
	/**
	 * 获取收单或退货记账通用记账list
	 * @param input
	 * @return
	 */
	public static DpAccountingInput getJfacqrAcctInput(DpCommAcctNormInput input){
		/*
		 *   收单记账会计分录（退货与收单相反）
		 * 	 Dr	112202应收账款-待清算款-银联	1000
		 *   Cr	60010318-原即付宝/开店宝收入/60010326-自营大POS收入/......	-2
	     *   Cr	220209应付账款-商户存款	992
	     *   Cr	60010318-原即付宝/开店宝收入/60010326-自营大POS收入/......	5
	     *   Cr	112202应收账款-待清算款	5
	     *   
	     *   代发：
	     *   Dr	220209应付账款-商户存款		990
	     *   Cr	220206商户应付账款D0	990
		 */
		//输入参数检查
		JfDpAccountCommCheck.checkAccountAcquireInput(input);
		
		DpAccountingInput output = SysUtil.getInstance(DpAccountingInput.class);
		//获取商户信息
		AcctSbad acctSbad = JfDpAccountCommHelper.getMerchantAcct(input.getInmeid()); 
		//获取应收账款业务编号
		String recvableBusino = JfDpAccountCommHelper.getRecvableExpose(input.getSvcode()); 
	    //根据品牌编号获取费用收入业务编码信息
		KnpParameter  incomeBusi = JfDpAccountCommHelper.getExpenseIncomeSubject(acctSbad.getSbrand());
		// 会计分录一：Dr 应收账款-待清算款-银联 记账信息赋值
		DpBusiInput busiInput = SysUtil.getInstance(DpBusiInput.class);
		busiInput.setBusino(recvableBusino); //应收账款业务编号
		if(input.getJftrtp() == E_JFTRTP.jfacqr) {
			busiInput.setJfactp(E_JFACTP.ACDR); //内部户借方
		}else if(input.getJftrtp() == E_JFTRTP.jfacth) {
			busiInput.setJfactp(E_JFACTP.ACCR); //内部户借方
		}else {
			throw DpModuleError.DpTrans.TS010068(); //记账交易类型不匹配
		}
		busiInput.setCrcycd(input.getCrcycd());//币种
		busiInput.setTranam(input.getYsunam()); //交易金额取字段【应收银联待清算交易金额】
		busiInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch()); //交易机构
		busiInput.setToacct(input.getPabkno()); //支付银行卡账号
		busiInput.setToacna(input.getPabkna()); //支付银行卡户名
		busiInput.setSmrycd(input.getSmrycd()); //摘要码
		output.getBusiList().add(busiInput);
		
		// 会计分录二：Cr 应付账款-商户存款
		DpAccountInput dpAccountInput = SysUtil.getInstance(DpAccountInput.class);
		dpAccountInput.setAcctno(acctSbad.getAcctno());
		dpAccountInput.setCardno(acctSbad.getCardno());
		dpAccountInput.setCrcycd(input.getCrcycd());
		dpAccountInput.setCustac(acctSbad.getCustac());
		dpAccountInput.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));
		if(input.getJftrtp() == E_JFTRTP.jfacqr) {
			dpAccountInput.setJfactp(E_JFACTP.SAVE); //存入
		}else if(input.getJftrtp() == E_JFTRTP.jfacth) {
			dpAccountInput.setJfactp(E_JFACTP.DRAW); //支取
		}else {
			throw DpModuleError.DpTrans.TS010068(); //记账交易类型不匹配
		}
		dpAccountInput.setSmrycd(input.getSmrycd()); //摘要码
		dpAccountInput.setTranam(input.getYfmcam());//交易金额取字段【应付商户存款交易金额】
		dpAccountInput.setRemark(input.getRemark());
		dpAccountInput.setIschck(E_YES___.NO);
		dpAccountInput.setToacct(input.getPabkno());
		dpAccountInput.setOpacna(input.getPabkna());
		output.getAccountList().add(dpAccountInput);
		
		// 会计分录三：Cr 应收账款-待清算款
		busiInput = SysUtil.getInstance(DpBusiInput.class);
		busiInput.setBusino(recvableBusino); //应收账款业务编号
		if(input.getJftrtp() == E_JFTRTP.jfacqr) {
			busiInput.setJfactp(E_JFACTP.ACCR); //内部户贷方
		}else if(input.getJftrtp() == E_JFTRTP.jfacth) {
			busiInput.setJfactp(E_JFACTP.ACDR); //内部户借方
		}else {
			throw DpModuleError.DpTrans.TS010068(); //记账交易类型不匹配
		}
		busiInput.setCrcycd(input.getCrcycd());//币种
		busiInput.setTranam(input.getUnamfe()); //交易金额取字段【冲抵应收账款交易金额(银联手续费)】
		busiInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch()); //交易机构
		busiInput.setToacct(input.getPabkno()); //支付银行卡账号
		busiInput.setToacna(input.getPabkna()); //支付银行卡户名
		busiInput.setSmrycd(input.getSmrycd()); //摘要码
		output.getBusiList().add(busiInput);
		
		//会计分录四：Cr	60010318-原即付宝/开店宝收入/60010326-自营大POS收入/......	(收入)
		DpInsertSummonsInput summonsIncome = SysUtil.getInstance(DpInsertSummonsInput.class);
		summonsIncome.getAccountingIntf().setAcctno(incomeBusi.getParm_value1());
		summonsIncome.getAccountingIntf().setProdcd(incomeBusi.getParm_value3());
		summonsIncome.getAccountingIntf().setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());
		summonsIncome.getAccountingIntf().setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		summonsIncome.getAccountingIntf().setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		if(input.getJftrtp() == E_JFTRTP.jfacqr) {
			summonsIncome.getAccountingIntf().setAmntcd(E_AMNTCD.CR);
		}else if(input.getJftrtp() == E_JFTRTP.jfacth) {
			summonsIncome.getAccountingIntf().setAmntcd(E_AMNTCD.DR);
		}else {
			throw DpModuleError.DpTrans.TS010068(); //记账交易类型不匹配
		}
		summonsIncome.getAccountingIntf().setDtitcd(incomeBusi.getParm_value3());
		summonsIncome.getAccountingIntf().setCrcycd(input.getCrcycd());
		summonsIncome.getAccountingIntf().setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch());
		summonsIncome.getAccountingIntf().setToacct(input.getPabkno());
		summonsIncome.getAccountingIntf().setTranam(input.getDivdam()); //交易金额取字段 【分润费用收入】
		summonsIncome.getAccountingIntf().setToacna(input.getPabkna());
		//summonsIncome.getAccountingIntf().setTobrch(knaAcct.getBrchno());
		summonsIncome.getAccountingIntf().setAtowtp(E_ATOWTP.IN);
		summonsIncome.getAccountingIntf().setTrsqtp(E_ATSQTP.ACCOUNT);
		summonsIncome.getAccountingIntf().setBltype(E_BLTYPE.BALANCE);
		summonsIncome.getAccountingIntf().setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		summonsIncome.getAccountingIntf().setCorpno(CommTools.getBusiOrgId());
		summonsIncome.setBusidn(E_BLNCDN.get(incomeBusi.getParm_value2()));
		output.getSummonsInut().add(summonsIncome);
		
		//如果抵扣金额不为空，抵扣金额记账
		if(CommUtil.isNotNull(input.getDivdfe())) {
			//会计分录四：Cr	60010318-原即付宝/开店宝收入/60010326-自营大POS收入/......	(抵扣券)
			DpInsertSummonsInput summonsCoupon = SysUtil.getInstance(DpInsertSummonsInput.class);
			IoAccounttingIntf intf = SysUtil.getInstance(IoAccounttingIntf .class);
			CommUtil.copyProperties(intf, summonsIncome.getAccountingIntf());
			intf.setTranam(input.getDivdfe().abs().negate()); //交易金额取字段 【冲抵分润费用收入（抵用券）】
			summonsCoupon.setAccountingIntf(intf);
			summonsCoupon.setBusidn(E_BLNCDN.get(incomeBusi.getParm_value2()));
			output.getSummonsInut().add(summonsCoupon);
		}
		
		// 如果结算类型为D0，代发记账
	    if(input.getFinsty() == E_FINSTY.D0) {
			//获取应付账款业务编号
			String handleBusino = JfDpAccountCommHelper.getHandleExpose(input.getFinsty());
	    	//会计分录六： 代发 --存款支取服务
		    dpAccountInput = SysUtil.getInstance(DpAccountInput.class);
			dpAccountInput.setAcctno(acctSbad.getAcctno());
			dpAccountInput.setCardno(acctSbad.getCardno());
			dpAccountInput.setCrcycd(input.getCrcycd());
			dpAccountInput.setCustac(acctSbad.getCustac());
			dpAccountInput.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));
			if(input.getJftrtp() == E_JFTRTP.jfacqr) {
				dpAccountInput.setJfactp(E_JFACTP.DRAW); //支取
			}else if(input.getJftrtp() == E_JFTRTP.jfacth) {
				dpAccountInput.setJfactp(E_JFACTP.SAVE); //存入
			}else {
				throw DpModuleError.DpTrans.TS010068(); //记账交易类型不匹配
			}
			dpAccountInput.setSmrycd(input.getSmrycd()); //摘要码
			dpAccountInput.setTranam(input.getYfmcam());//商户存入金额
			dpAccountInput.setToacct(input.getPabkno());//支付商户号
			dpAccountInput.setOpacna(input.getPamena());//支付商户名称
			output.getAccountList().add(dpAccountInput);

			//会计分录七：代发内部户转入
			busiInput = SysUtil.getInstance(DpBusiInput.class);
			busiInput.setBusino(handleBusino); //应付账款业务编号
			if(input.getJftrtp() == E_JFTRTP.jfacqr) {
				busiInput.setJfactp(E_JFACTP.ACCR); //内部户借方
			}else if(input.getJftrtp() == E_JFTRTP.jfacth) {
				busiInput.setJfactp(E_JFACTP.ACDR); //内部户借方
			}else {
				throw DpModuleError.DpTrans.TS010068(); //记账交易类型不匹配
			}
			busiInput.setCrcycd(input.getCrcycd());//币种
			busiInput.setTranam(input.getYfmcam()); //交易金额取字段【应付商户存款交易金额】
			busiInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch()); //交易机构
			busiInput.setToacct(input.getPameno()); //支付商户号
			busiInput.setToacna(input.getPamena()); //支付商户名称
			busiInput.setSmrycd(input.getSmrycd()); //摘要码
			output.getBusiList().add(busiInput);
	    }
		return output;
	}
	
	/**
	 * 获取代发记账通用记账list
	 * 代发：
	 * Dr	220209应付账款-商户存款		990
	 * Cr	220206商户应付账款D0	990
	 * @param input
	 * @return
	 */
	public static DpAccountingInput getJfoblfAcctInput(DpCommAcctNormInput input){
		
		DpAccountingInput output = SysUtil.getInstance(DpAccountingInput.class);
		if(input.getFinsty() == E_FINSTY.T1) {
			//输入参数检查
			JfDpAccountCommCheck.checkAccountSendInput(input);
			//获取商户信息
			AcctSbad acctSbad = JfDpAccountCommHelper.getMerchantAcct(input.getInmeid()); 
		    //获取应付账款业务编号
			String handleBusino = JfDpAccountCommHelper.getHandleExpose(input.getFinsty());
	    	//会计分录一： 代发 --存款支取服务
			DpAccountInput dpAccountInput = SysUtil.getInstance(DpAccountInput.class);
			dpAccountInput.setAcctno(acctSbad.getAcctno());
			dpAccountInput.setCardno(acctSbad.getCardno());
			dpAccountInput.setCrcycd(input.getCrcycd());
			dpAccountInput.setCustac(acctSbad.getCustac());
			dpAccountInput.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));
			dpAccountInput.setJfactp(E_JFACTP.DRAW);
			dpAccountInput.setSmrycd(input.getSmrycd());
			dpAccountInput.setTranam(input.getTranam());//交易金额
			dpAccountInput.setToacct(input.getPabkno());//支付商户号
			dpAccountInput.setOpacna(input.getPamena());//支付商户名称
			output.getAccountList().add(dpAccountInput);
			
			//会计分录二：代发内部户转入
			DpBusiInput busiInput = SysUtil.getInstance(DpBusiInput.class);
			busiInput.setBusino(handleBusino); //应付账款业务编号
			busiInput.setJfactp(E_JFACTP.ACCR); //内部户贷方
			busiInput.setCrcycd(input.getCrcycd());//币种
			busiInput.setTranam(input.getTranam()); //交易金额
			busiInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch()); //交易机构
			busiInput.setToacct(input.getPameno()); //支付商户号
			busiInput.setToacna(input.getPamena()); //支付商户名称
			busiInput.setSmrycd(input.getSmrycd());
			output.getBusiList().add(busiInput);
	    } else {
	    	throw DpModuleError.DpTrans.TS010076(); //代发记账结算类型必须为T1
	    }
		return output;
	}

	/**
	 * 获取挂账记账通用记账list
	 * @param input
	 * @return
	 */
	public static DpAccountingInput getJfbookAcctInput(DpCommAcctNormInput input){
		//输入参数检查
		JfDpAccountCommCheck.checkAccountPendingInput(input);
		DpAccountingInput output = SysUtil.getInstance(DpAccountingInput.class);
		DpBusiInput busiInput = SysUtil.getInstance(DpBusiInput.class);		
		//获取应收账款业务编号
		String recvableBusino = JfDpAccountCommHelper.getRecvableExpose(input.getSvcode()); 
		//获取长款挂账业务编号
		String longBusino = JfDpAccountCommHelper.getLongPaymentSubject();
		//会计分录一：Cr 长款挂账户
		busiInput.setBusino(longBusino); //长款挂账户
		busiInput.setJfactp(E_JFACTP.ACCR); //内部户贷方
		busiInput.setCrcycd(input.getCrcycd());//币种
		busiInput.setTranam(input.getTranam()); //交易金额取字段【应付商户存款交易金额】
		busiInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch()); //交易机构
		busiInput.setToacct(input.getPabkno()); //交易对手账号
		busiInput.setToacna(input.getPabkna()); //交易对手户名
		busiInput.setSmrycd(input.getSmrycd());
		output.getBusiList().add(busiInput);
    	//会计分录二： Dr 应收账款-待清算款
		busiInput = SysUtil.getInstance(DpBusiInput.class);	
		busiInput.setBusino(recvableBusino); //应收账款业务编号
		busiInput.setJfactp(E_JFACTP.ACDR); //内部户借方
		busiInput.setCrcycd(input.getCrcycd());//币种
		busiInput.setTranam(input.getTranam()); //交易金额
		busiInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch()); //交易机构
		busiInput.setToacct(input.getPabkno()); //交易对手账号
		busiInput.setToacna(input.getPabkna()); //交易对手户名
		busiInput.setSmrycd(input.getSmrycd());
		output.getBusiList().add(busiInput);
		return output;
	}

	/**
	 * 通用记账
	 * @param input
	 * @return
	 */
	public static void commAccounting(DpAccountingInput input){
			
		// 内部户记账list循环记账
		Options<DpBusiInput> busiInputList = input.getBusiList();
		IoInAccount ioInAccount = SysUtil.getInstance(IoInAccount.class); //内部户记账服务初始化
		for (DpBusiInput busiInput : busiInputList) {
			if (CommUtil.compare(busiInput.getTranam(), BigDecimal.ZERO) == 0) {
				continue;
			}
			
			IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class); //内部户记账输入复核类型初始化
			CommUtil.copyProperties(iaAcdrInfo, busiInput);  //为内部户记账输入复核类型赋值
			if (busiInput.getJfactp() == E_JFACTP.ACDR) {
				ioInAccount.ioInAcdr(iaAcdrInfo);//调用内部户借方服务
			}else if(busiInput.getJfactp() == E_JFACTP.ACCR) {
				ioInAccount.ioInAccr(iaAcdrInfo);//调用内部户贷方服务
			}else if(busiInput.getJfactp() == E_JFACTP.ACPV) {
				ioInAccount.ioInAcpv(iaAcdrInfo); //调用内部户付方服务
			}else if(busiInput.getJfactp() == E_JFACTP.ACRV) {
				ioInAccount.ioInAcrv(iaAcdrInfo); //调用内部户收方服务
			}else {
				//交易记账方式错误
				throw DpModuleError.DpTrans.TS020077();
			}
		}
		
		// 账户记账list循环记账
		Options<DpAccountInput> acctInputList = input.getAccountList();
		DpAcctSvcType dpAcctSvcType = SysUtil.getInstance(DpAcctSvcType.class); //账户记账服务初始化
		for (DpAccountInput dpAccountInput : acctInputList) {
			if (CommUtil.compare(dpAccountInput.getTranam(), BigDecimal.ZERO) == 0) {
				continue;
			}
			SaveDpAcctIn saveDpAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
			DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
			if (dpAccountInput.getJfactp() == E_JFACTP.SAVE) {
				CommUtil.copyProperties(saveDpAcctIn, dpAccountInput); //为存入记账输入复核类型赋值
				saveDpAcctIn.setIschck(E_YES___.NO);
				dpAcctSvcType.addPostAcctDp(saveDpAcctIn);//调用账户存入记账服务
			}else if(dpAccountInput.getJfactp() == E_JFACTP.DRAW) {
				CommUtil.copyProperties(drawDpAcctIn, dpAccountInput); //为存入记账输入复核类型赋值
				drawDpAcctIn.setIschck(E_YES___.NO); //支取不检查冻结标志
				dpAcctSvcType.addDrawAcctDp(drawDpAcctIn);//调用账户支取记账服务
			}else {
				//交易记账方式错误
				throw DpModuleError.DpTrans.TS020077();
			}
		}
		
		// 登记传票list循环录入
		Options<DpInsertSummonsInput> summonsInputList = input.getSummonsInut();
		IoSaveIoTransBill ioSaveIoTransBill = SysUtil.getInstance(IoSaveIoTransBill.class);
		for (DpInsertSummonsInput dpInsertSummonsInput : summonsInputList) {
			if (CommUtil.compare(dpInsertSummonsInput.getAccountingIntf().getTranam(), BigDecimal.ZERO) == 0) {
				continue;
			}
			ioSaveIoTransBill.SaveKnlAcsq(dpInsertSummonsInput.getAccountingIntf(), dpInsertSummonsInput.getBusidn());
		}	
		
		// 平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(),
				CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._35);
	}
	
	/**
	 * 登记通用记账登记簿
	 * @param input
	 * @return
	 */
	public static void insertAcsqRgst(DpCommAcctNormInput input){
		insertAcsqRgstStrke(input, E_YES___.YES);	
	}
	
	/**
	 * 增加登记簿冲正事件注册
	 * @param input
	 * @param strktg
	 */
	public static void insertAcsqRgstStrke(DpCommAcctNormInput input, E_YES___ strktg){
		
		KnsAcsqRgst tblKnsAcsqRgst = SysUtil.getInstance(KnsAcsqRgst.class);
		tblKnsAcsqRgst.setChantp(input.getChantp());
		tblKnsAcsqRgst.setCktrdt(input.getCktrdt());
		tblKnsAcsqRgst.setCltrdt(input.getCltrdt());
		tblKnsAcsqRgst.setCltrsq(input.getCltrsq());
		tblKnsAcsqRgst.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		tblKnsAcsqRgst.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		tblKnsAcsqRgst.setMntrtm(CommTools.getBaseRunEnvs().getComputer_time());  
		tblKnsAcsqRgst.setBusisq(CommTools.getBaseRunEnvs().getBusi_seq()); //业务流水
		tblKnsAcsqRgst.setCorpno(CommTools.getBusiOrgId());
		tblKnsAcsqRgst.setCrcycd(input.getCrcycd());
		tblKnsAcsqRgst.setDivdam(input.getDivdam());
		tblKnsAcsqRgst.setDivdfe(input.getDivdfe());
		tblKnsAcsqRgst.setInmeid(input.getInmeid());
		tblKnsAcsqRgst.setIschck(input.getIschck());
		tblKnsAcsqRgst.setJftrtp(input.getJftrtp());
		tblKnsAcsqRgst.setClertp(input.getClertp());
		tblKnsAcsqRgst.setMarkfe(input.getMarkfe());
		tblKnsAcsqRgst.setOtbkna(input.getOtbkna());
		tblKnsAcsqRgst.setPabkno(input.getPabkno());
		tblKnsAcsqRgst.setPamena(input.getPamena());
		tblKnsAcsqRgst.setPameno(input.getPameno());
		tblKnsAcsqRgst.setPosnum(input.getPosnum());
		tblKnsAcsqRgst.setRemark(input.getRemark());
		tblKnsAcsqRgst.setSvcode(input.getSvcode());
		tblKnsAcsqRgst.setTmstmp(CommTools.getBaseRunEnvs().getTimestamp());
		tblKnsAcsqRgst.setTranam(input.getTranam());
		tblKnsAcsqRgst.setUnamfe(input.getUnamfe());
		tblKnsAcsqRgst.setVoucfe(input.getVoucfe());
		tblKnsAcsqRgst.setYfmcam(input.getYfmcam());
		tblKnsAcsqRgst.setYsunam(input.getYsunam());
		tblKnsAcsqRgst.setTranst(E_TRANST.SUCCESS);
		
		KnsAcsqRgstDao.insert(tblKnsAcsqRgst);
		
		if (strktg == E_YES___.YES) {
			
			//冲正注册		
	    	IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
	    	
	    	cplInput.setEvent1(tblKnsAcsqRgst.getMntrsq()); //传票流水
	    	cplInput.setEvent2(tblKnsAcsqRgst.getMntrdt()); //主交易流水
	    	
	    	IoMsRegEvent ioMsReg = SysUtil.getInstance(IoMsRegEvent.class);    		
	    	ioMsReg.setInformation_value(SysUtil.serialize(cplInput));
			ioMsReg.setCall_out_seq(CommTools.getBaseRunEnvs().getCall_out_seq());//外调流水
			ioMsReg.setReversal_event_id("strkeRegistKnsAcsqRgst");//冲正事件ID
			ioMsReg.setService_id("strkeRegistKnsAcsqRgst");//服务ID
			ioMsReg.setSub_system_id(CoreUtil.getSubSystemId());//子系统ID
			ioMsReg.setTxn_event_level(E_EVENTLEVEL.LOCAL);//教义事件级别NORMAL（）INQUIRE（）LOCAL（）CRDIT（）
			ioMsReg.setIs_across_dcn(E_YESORNO.NO);
			
			MsEvent.register(ioMsReg, true);
		}
	}
	
	/**
	 * 检查原流水是否存在
	 * @param ortrsq 原交易流水
	 * @param ortrdt 原交易日期
	 * @return 即富记账登记簿
	 */
	public static KnsAcsqRgst chckOrtrsq(String ortrsq, String ortrdt) {
		
		KnsAcsqRgst tblKnsAcsqRgst = KnsAcsqRgstDao.selectOne_odb1(ortrsq, ortrdt, false);
		
		if (CommUtil.isNull(tblKnsAcsqRgst)) {
			tblKnsAcsqRgst = SysUtil.getInstance(KnsAcsqRgst.class);
			tblKnsAcsqRgst.setChantp(E_CHANTP.POSP);
			tblKnsAcsqRgst.setCrcycd("CNY");
		} else if (tblKnsAcsqRgst.getTranst() == E_TRANST.STRIKED) {
			//原交易已冲正	
			throw DpModuleError.DpTrans.TS020081(); 
		}
		
		return tblKnsAcsqRgst;
	}
	
	/**
	 * 更新原流水状态
	 * @param tblKnsAcsqRgst
	 */
	public static void updOrtrsq(KnsAcsqRgst tblKnsAcsqRgst) {
		KnsAcsqRgstDao.updateOne_odb1(tblKnsAcsqRgst);
	}

}
