package cn.sunline.ltts.busi.dp.client;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnsTranEror;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnsTranErorDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcRevQuota;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.InknlcnapotDetl;
import cn.sunline.ltts.busi.iobus.type.IoInTable.IoGlKnaAcct;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccountClearInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.CupsTranfe;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.LsamClerIN;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTypeStrikeInfo.ChargStrikeOutput;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTypeStrikeInfo.ProcPbChargStrikeInput;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLERST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOTYPE;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SUBSYS;
import cn.sunline.ltts.busi.sys.type.FnEnumType.E_WARNTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;

/**
 * @ClassName: CapitalTransLsamStrike 
 * @Description: 大小额冲正 挂账 补充清算分录 
 * @author zhangan
 * @date 2017年2月21日 下午8:42:53 
 *
 */
public class CapitalTransLsamStrike {
	
	
	private static BizLog log = BizLogUtil.getBizLog(CapitalTransLsamStrike.class);
	
	/**
	 * @Title: prcLsamStrikeHold 
	 * @Description: 大小额冲正挂账
	 * @param knlCnapot
	 * @author zhangan
	 * @date 2017年2月21日 上午8:59:15 
	 * @version V2.3.0
	 */
	public static void prcLsamStrikeHold(InknlcnapotDetl knlCnapot, String errmes){
		//4.贷系统内清算
		E_CLACTP clactp=null;
		
		if(CommUtil.equals(knlCnapot.getSubsys(),E_SUBSYS.LM.getValue())){//小额
			clactp=E_CLACTP._06;
		}else if(CommUtil.equals(knlCnapot.getSubsys(),E_SUBSYS.BG.getValue())){//大额
			clactp=E_CLACTP._05;
		}else {
			throw DpModuleError.DpstComm.BNAS1589();
		}
		IoInAccount inSrv = CommTools.getRemoteInstance(IoInAccount.class);
		
		//省中心机构
		String acbrch = BusiTools.getBusiRunEnvs().getCentbr(); //获取省中心机构号
		//获取系统内清算往来业务编号
		KnpParameter para1 = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", clactp.getValue(), "%", true);
		//挂账业务编号
		KnpParameter para3 = KnpParameterDao.selectOne_odb1("InParm.cupsconfrim","in", "02", "%", true);
		
		IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户记账输入
		IaAcdrInfo acdrIn4 = SysUtil.getInstance(IaAcdrInfo.class);
		IaTransOutPro acdrOt = SysUtil.getInstance(IaTransOutPro.class);
		
		IoCaKnaAcdc acdc = SysUtil.getInstance(IoCaKnaAcdc.class);
		KnaAcct acct = SysUtil.getInstance(KnaAcct.class);
		
		
		String cardno = "";
		String acctna = "";
		String opcard = "";
		String opacna = "";
		if(knlCnapot.getIotype() == E_IOTYPE.IN){ //来账
			
			
			throw DpModuleError.DpstComm.BNAS1590();
			
			/*cardno = knlCnapot.getPyeeac();
			acctna = knlCnapot.getPyeena();
			opcard = knlCnapot.getPyerac();
			opacna = knlCnapot.getPyerna();
			
			acdc = ActoacDao.selKnaAcdc(cardno, true);
			acct = CapitalTransDeal.getSettKnaAcctAc(acdc.getCustac());
			
			//2.电子账户支取记账服务
			
			DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class); //存款记账服务
			
			SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class); //电子账户存入记账复合类型
			
			saveIn.setAcctno(acct.getAcctno()); //结算账户、钱包账户
			saveIn.setBankcd(knlCnapot.getPyercd());
			saveIn.setBankna("");
			saveIn.setCardno(cardno); //电子账户卡号
			saveIn.setCrcycd(acct.getCrcycd());
			saveIn.setCustac(acct.getCustac());
			saveIn.setOpacna(opacna);
			saveIn.setOpbrch(knlCnapot.getBrchno());
			saveIn.setSmrycd(E_SMRYCD.CZ);
			saveIn.setToacct(knlCnapot.getPyerac());
			saveIn.setTranam(knlCnapot.getTranam().negate()); //红字存入
			saveIn.setIschck(E_YES___.NO);
			saveIn.setStrktg(E_YES___.NO);
			saveIn.setNegafg(E_YES___.YES); //是否允许负金额
			
			dpSrv.addPostAcctDp(saveIn);
			
			//电子汇划记账
			CupsTranfe cplCnapot = SysUtil.getInstance(CupsTranfe.class);
			cplCnapot.setCrcycd(knlCnapot.getCrcycd());
			cplCnapot.setSmrycd(E_SMRYCD.CZ);
			cplCnapot.setInbrch(knlCnapot.getBrchno());
			cplCnapot.setTranam(knlCnapot.getTranam().negate());
			cplCnapot.setInacct(knlCnapot.getPyerac());
			cplCnapot.setInacna(knlCnapot.getPyerna());
			cplCnapot.setOtacct(knlCnapot.getPyeeac());
			cplCnapot.setOtacna(knlCnapot.getPyeena());
			CapitalTransDeal.dealCnapotVN(cplCnapot, knlCnapot.getIotype());
			*/
		}else if(knlCnapot.getIotype() == E_IOTYPE.OUT){ //往账
			cardno = knlCnapot.getPyerac();
			acctna = knlCnapot.getPyerna();
			opcard = knlCnapot.getPyeeac();
			opacna = knlCnapot.getPyeena();
			
			acdc = ActoacDao.selKnaAcdc(cardno, true);
			E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(acdc.getCustac());
			if(eAccatp == E_ACCATP.WALLET){
				acct = ActoacDao.selknaAcctclose(E_ACSETP.MA, acdc.getCustac(), true).get(0);
			}else{
				acct = ActoacDao.selknaAcctclose(E_ACSETP.SA, acdc.getCustac(), true).get(0);
			}
			
			
			acdrIn1.setAcbrch(acbrch);
			acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
			acdrIn1.setCrcycd(knlCnapot.getCrcycd());
			acdrIn1.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd()); //冲正
			acdrIn1.setToacct(opcard);
			acdrIn1.setToacna(opacna);
			acdrIn1.setTranam(knlCnapot.getTranam().negate());
			acdrIn1.setBusino(para1.getParm_value1()); //业务编码
			acdrIn1.setSubsac(para1.getParm_value2());//子户号
			
			inSrv.ioInAccr(acdrIn1);
			
			//电子汇划记账
			CupsTranfe cplCnapot = SysUtil.getInstance(CupsTranfe.class);
			cplCnapot.setCrcycd(knlCnapot.getCrcycd());
			cplCnapot.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
			cplCnapot.setInbrch(knlCnapot.getBrchno());
			cplCnapot.setTranam(knlCnapot.getTranam().negate());
			cplCnapot.setInacct(knlCnapot.getPyeeac());
			cplCnapot.setInacna(knlCnapot.getPyeena());
			cplCnapot.setOtacct(knlCnapot.getPyerac());
			cplCnapot.setOtacna(knlCnapot.getPyerna());
			CapitalTransDeal.dealCnapotVN(cplCnapot, knlCnapot.getIotype());
		}
		
//		String trcorp = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(knlCnapot.getBrchno()).getCorpno();
//		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//		CommTools.getBaseRunEnvs().setBusi_org_id(trcorp);
		
		ProcPbChargStrikeInput input = SysUtil.getInstance(ProcPbChargStrikeInput.class);
		input.setOrtrdt(knlCnapot.getTrandt());
		input.setOrtrsq(knlCnapot.getTransq());
		
		Options<ChargStrikeOutput> lstOutput = SysUtil.getInstance(IoPbStrikeSvcType.class).procPbChargStrike(input);
		
//		CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
		
		BigDecimal chrgam = BigDecimal.ZERO;
		for(ChargStrikeOutput output : lstOutput){
			chrgam = chrgam.add(CommUtil.nvl(output.getTranam(), BigDecimal.ZERO));
		}
		
		//结算暂收挂账
		acdrIn4.setAcbrch(acct.getBrchno()); //挂账所属机构挂电子账户机构
		acdrIn4.setTrantp(E_TRANTP.TR); //交易类型 
		acdrIn4.setCrcycd(knlCnapot.getCrcycd());
		acdrIn4.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
		acdrIn4.setToacct(cardno);
		acdrIn4.setToacna(acctna);
		if(knlCnapot.getIotype() == E_IOTYPE.OUT){
			acdrIn4.setTranam(knlCnapot.getTranam().add(chrgam));
		}else{
			acdrIn4.setTranam(knlCnapot.getTranam().subtract(chrgam));
		}
		acdrIn4.setBusino(para3.getParm_value1()); //业务编码
		acdrIn4.setSubsac(para3.getParm_value2());//子户号
		
		String remark = cardno + knlCnapot.getTrandt() + knlCnapot.getTransq() + errmes;
		acdrIn4.setDscrtx(remark);
		
		acdrOt = inSrv.ioInAccr(acdrIn4); //贷记服务
		
		
		//补充清算分录记录
		LsamClerIN clerIN = SysUtil.getInstance(LsamClerIN.class);
		clerIN.setBrchno(acct.getBrchno());
		clerIN.setCorpno(acct.getCorpno());
		clerIN.setCrcycd(acct.getCrcycd());
		clerIN.setIotype(knlCnapot.getIotype());
		clerIN.setIsdieb(E_YES___.NO);
		clerIN.setIshold(E_YES___.YES);
		clerIN.setSubsys(CommUtil.toEnum(E_SUBSYS.class, knlCnapot.getSubsys()));
		clerIN.setTranam(knlCnapot.getTranam());
		clerIN.setChrgam(chrgam);
		clerIN.setTrbrch(knlCnapot.getBrchno()); //交易机构
		
		CapitalTransLsamStrike.prcLsamClear(clerIN);
		
		//登记转账预警登记簿
		KnsTranEror tblKnsTranEror = SysUtil.getInstance(KnsTranEror.class);
		tblKnsTranEror.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		tblKnsTranEror.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		tblKnsTranEror.setErortp(E_WARNTP.ACOUNT);			
		tblKnsTranEror.setBrchno(knlCnapot.getBrchno());
		tblKnsTranEror.setOtacct(knlCnapot.getPyerac());
		tblKnsTranEror.setOtacna(knlCnapot.getPyerna());
		tblKnsTranEror.setOtbrch("");
		tblKnsTranEror.setInacct(knlCnapot.getPyeeac());					
		tblKnsTranEror.setInacna(knlCnapot.getPyeena());
		tblKnsTranEror.setInbrch("");
		tblKnsTranEror.setTranam(acdrIn4.getTranam());
		tblKnsTranEror.setCrcycd(knlCnapot.getCrcycd());
		tblKnsTranEror.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		tblKnsTranEror.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());
		tblKnsTranEror.setGlacct(acdrOt.getAcctno());
		tblKnsTranEror.setGlacna(acdrOt.getAcctna());
		if(CommUtil.isNotNull(acdrOt.getPayasqlist())&&acdrOt.getPayasqlist().size()>0){
			
			tblKnsTranEror.setGlseeq(acdrOt.getPayasqlist().get(0).getPayasq());
		}
		
		tblKnsTranEror.setDescrb(errmes);
		//插入登记簿
		KnsTranErorDao.insert(tblKnsTranEror);
		//平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalanceLsam(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),clactp);
		
	}
	/**
	 * @Title: prcLsamStrikeDieb 
	 * @Description: 大小额隔日冲正记账  
	 * @param knlCnapot 大小额登记簿信息
	 * @author zhangan
	 * @date 2017年2月21日 下午8:52:38 
	 * @version V2.3.0
	 */
	public static void prcLsamStrikeDieb(InknlcnapotDetl knlCnapot){
		

		E_IOTYPE iotype = knlCnapot.getIotype(); //往来标志
		E_CLACTP clactp=null;
		
		if(CommUtil.equals(knlCnapot.getSubsys(),E_SUBSYS.LM.getValue())){//小额
			clactp=E_CLACTP._06;
		}else if(CommUtil.equals(knlCnapot.getSubsys(),E_SUBSYS.BG.getValue())){//大额
			clactp=E_CLACTP._05;
		}else {
			throw DpModuleError.DpstComm.BNAS1589();
		}
		IoInAccount inSrv = CommTools.getRemoteInstance(IoInAccount.class);
		
		IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaKnaAcdc.class);
		KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
		//省中心机构
		String acbrch = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(knlCnapot.getBrchno()).getBrchno(); //获取省中心机构号
		//获取系统内清算往来业务编号
		KnpParameter para1 = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", clactp.getValue(), "%", true);
		//应扣费用
		BigDecimal chrgam = BigDecimal.ZERO;
		
//		String trcorp = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(knlCnapot.getBrchno()).getCorpno();
		
		if(iotype == E_IOTYPE.IN){
			
			tblKnaAcdc = ActoacDao.selKnaAcdc(knlCnapot.getPyeeac(), true);
			if(tblKnaAcdc.getStatus() == E_DPACST.CLOSE){
				throw DpModuleError.DpstComm.BNAS0740();
			}
			
			CapitalTransCheck.ChkAcctFrozOT(tblKnaAcdc.getCustac());
			
			tblKnaAcct = CapitalTransDeal.getSettKnaAcctAc(tblKnaAcdc.getCustac());
			
		}else if(iotype == E_IOTYPE.OUT){
			
			tblKnaAcdc = ActoacDao.selKnaAcdc(knlCnapot.getPyerac(), true);
			if(tblKnaAcdc.getStatus() == E_DPACST.CLOSE){
				throw DpModuleError.DpstComm.BNAS0740();
			}
			//冲正,红字支取只检查是否双冻
			CapitalTransCheck.ChkAcctFrozIN_2(tblKnaAcdc.getCustac());
			
			tblKnaAcct = CapitalTransDeal.getSettKnaAcctAc(tblKnaAcdc.getCustac());
		}
		
		DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class); //存款记账服务
		
		if(iotype == E_IOTYPE.IN){
			
			//大小额 系统内往来 记账
			IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class); //内部户记账输入
			acdrIn.setAcbrch(acbrch);
			acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
			acdrIn.setCrcycd(tblKnaAcct.getCrcycd());
			acdrIn.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
			acdrIn.setToacct(knlCnapot.getPyerac());
			acdrIn.setToacna(knlCnapot.getPyerna());
			acdrIn.setTranam(knlCnapot.getTranam().negate()); //反向红字记账
			acdrIn.setBusino(para1.getParm_value1()); //业务编码
			acdrIn.setSubsac(para1.getParm_value2());//子户号
			inSrv.ioInAcdr(acdrIn);
			
			//电子汇划冲正记账
			CupsTranfe cplCnapot = SysUtil.getInstance(CupsTranfe.class);
			cplCnapot.setCrcycd(tblKnaAcct.getCrcycd());
			cplCnapot.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
			cplCnapot.setInbrch(knlCnapot.getBrchno());
			cplCnapot.setTranam(knlCnapot.getTranam().negate()); //反向红字记账
			cplCnapot.setInacct(knlCnapot.getPyerac());
			cplCnapot.setInacna(knlCnapot.getPyerna());
			cplCnapot.setOtacct(knlCnapot.getPyeeac());
			cplCnapot.setOtacna(knlCnapot.getPyeena());
			CapitalTransDeal.dealCnapotVN(cplCnapot, iotype);
			
			
			//4.电子账户存入
			
			SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class); //电子账户存入记账复合类型
			
			saveIn.setAcctno(tblKnaAcct.getAcctno()); //结算账户、钱包账户
			saveIn.setBankcd(knlCnapot.getPyercd());
			saveIn.setBankna("");
			saveIn.setCardno(knlCnapot.getPyeeac()); //电子账户卡号
			saveIn.setCrcycd(tblKnaAcct.getCrcycd());
			saveIn.setCustac(tblKnaAcdc.getCustac());
			saveIn.setOpacna(knlCnapot.getPyerna());
			saveIn.setOpbrch(knlCnapot.getBrchno());
			saveIn.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
			saveIn.setToacct(knlCnapot.getPyerac());
			saveIn.setTranam(knlCnapot.getTranam().negate()); //红字存入
			saveIn.setIschck(E_YES___.NO);
			saveIn.setStrktg(E_YES___.NO);
			saveIn.setNegafg(E_YES___.YES); //是否允许负金额
			
			dpSrv.addPostAcctDp(saveIn);
			
			
			//费用冲正
//			String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//			CommTools.getBaseRunEnvs().setBusi_org_id(trcorp);
			ProcPbChargStrikeInput input = SysUtil.getInstance(ProcPbChargStrikeInput.class);
			input.setOrtrdt(knlCnapot.getTrandt());
			input.setOrtrsq(knlCnapot.getTransq());
			Options<ChargStrikeOutput> lstOutput = SysUtil.getInstance(IoPbStrikeSvcType.class).procPbChargStrike(input);
//			CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
			//统计总费用
			for(ChargStrikeOutput output : lstOutput){
				
				BigDecimal tlcgam = CommUtil.nvl(output.getTranam(), BigDecimal.ZERO);
				chrgam = chrgam.add(tlcgam);
				
				if(CommUtil.compare(tlcgam, BigDecimal.ZERO) > 0){
					
					DrawDpAcctIn drawIN2 = SysUtil.getInstance(DrawDpAcctIn.class);
					drawIN2.setAcctno(tblKnaAcct.getAcctno());
					drawIN2.setAcseno(null);
					drawIN2.setAuacfg(null);
					drawIN2.setBankcd(knlCnapot.getPyercd());
					drawIN2.setBankna("");
					drawIN2.setCardno(knlCnapot.getPyeeac());
					drawIN2.setCrcycd(tblKnaAcct.getCrcycd());
					drawIN2.setCustac(tblKnaAcdc.getCustac());
					drawIN2.setIschck(E_YES___.NO);
					drawIN2.setOpacna(output.getChrgcd());
					drawIN2.setOpbrch(output.getBrchno());
					drawIN2.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
					drawIN2.setToacct(output.getChrgcd());
					drawIN2.setTranam(tlcgam.negate()); //费用冲正
					drawIN2.setStrktg(E_YES___.NO);
					
					dpSrv.addDrawAcctDp(drawIN2);
				}
			}
			
			/*//客户账冲正
			for(IoDpKnlBill bill : lstBill){
				
				DpSaveEntity entity = new DpSaveEntity();
				entity.setAcctno(bill.getAcctno());
				entity.setCustac(bill.getCustac());
				entity.setOramnt(bill.getAmntcd());
				entity.setColrfg(CommUtil.toEnum(E_COLOUR.class, eHolzjzbz)); //红字冲正
				entity.setCrcycd(bill.getTrancy());
				entity.setDetlsq(bill.getDetlsq());
				entity.setOrtrdt(bill.getTrandt());
				entity.setStacps(bill.getStacps());
				entity.setTranam(bill.getTranam());
				
				DpPublicServ.strikePostAcctDp(entity);
				
			}*/
			
		} else if (iotype == E_IOTYPE.OUT){
			
//			String cardno = knlCnapot.getPyerac(); //电子账号
//			acdc = ActoacDao.selKnaAcdc(cardno, true);
//			tblKnaAcct = CapitalTransDeal.getSettKnaAcctAc(acdc.getCustac());
//			
			//客户账冲正
/*			for(IoDpKnlBill bill : lstBill){
				
				DpSaveEntity entity = new DpSaveEntity();
				entity.setAcctno(bill.getAcctno());
				entity.setCustac(bill.getCustac());
				entity.setOramnt(bill.getAmntcd());
				entity.setColrfg(CommUtil.toEnum(E_COLOUR.class, eHolzjzbz)); //红字冲正
				entity.setCrcycd(bill.getTrancy());
				entity.setDetlsq(bill.getDetlsq());
				entity.setOrtrdt(bill.getTrandt());
				entity.setStacps(bill.getStacps());
				entity.setTranam(bill.getTranam());
				
				DpPublicServ.strikeDrawAcctDp(entity);
				
			}*/
			
			
			
			//2.电子账户支取记账服务
			DrawDpAcctIn drawIN = SysUtil.getInstance(DrawDpAcctIn.class);
			
			drawIN.setAcctno(tblKnaAcct.getAcctno());
			drawIN.setAcseno(null);
			drawIN.setAuacfg(null);
			drawIN.setBankcd(knlCnapot.getPyeecd());
			drawIN.setBankna("");
			drawIN.setCardno(knlCnapot.getPyerac());
			drawIN.setCrcycd(tblKnaAcct.getCrcycd());
			drawIN.setCustac(tblKnaAcdc.getCustac());
			drawIN.setIschck(E_YES___.NO);
			drawIN.setOpacna(knlCnapot.getPyeena());
			drawIN.setOpbrch(knlCnapot.getBrchno());
			drawIN.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
			drawIN.setToacct(knlCnapot.getPyeeac());
			drawIN.setTranam(knlCnapot.getTranam().negate());
			drawIN.setStrktg(E_YES___.NO);
			
			dpSrv.addDrawAcctDp(drawIN);
			
			
			//费用冲正
//			String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//			CommTools.getBaseRunEnvs().setBusi_org_id(trcorp);
			ProcPbChargStrikeInput input = SysUtil.getInstance(ProcPbChargStrikeInput.class);
			input.setOrtrdt(knlCnapot.getTrandt());
			input.setOrtrsq(knlCnapot.getTransq());
			Options<ChargStrikeOutput> lstOutput = SysUtil.getInstance(IoPbStrikeSvcType.class).procPbChargStrike(input);
//			CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
			//费用汇总
			for(ChargStrikeOutput output : lstOutput){
				
				BigDecimal tlcgam = CommUtil.nvl(output.getTranam(), BigDecimal.ZERO);
				chrgam = chrgam.add(tlcgam);
				
				if(CommUtil.compare(tlcgam, BigDecimal.ZERO) > 0){
					DrawDpAcctIn drawIN2 = SysUtil.getInstance(DrawDpAcctIn.class);
					
					drawIN2.setAcctno(tblKnaAcct.getAcctno());
					drawIN2.setAcseno(null);
					drawIN2.setAuacfg(null);
					drawIN2.setBankcd(knlCnapot.getPyeecd());
					drawIN2.setBankna("");
					drawIN2.setCardno(knlCnapot.getPyerac());
					drawIN2.setCrcycd(tblKnaAcct.getCrcycd());
					drawIN2.setCustac(tblKnaAcdc.getCustac());
					drawIN2.setIschck(E_YES___.NO);
					drawIN2.setOpacna(output.getChrgcd());
					drawIN2.setOpbrch(output.getBrchno());
					drawIN2.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
					drawIN2.setToacct(output.getChrgcd());
					drawIN2.setTranam(tlcgam.negate()); //费用冲正
					drawIN2.setStrktg(E_YES___.NO);
					
					dpSrv.addDrawAcctDp(drawIN2);
					
				}
				
			}
			
			
			//电子汇划记账
			CupsTranfe cplCnapot = SysUtil.getInstance(CupsTranfe.class);
			cplCnapot.setCrcycd(tblKnaAcct.getCrcycd());
			cplCnapot.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
			cplCnapot.setInbrch(knlCnapot.getBrchno());
			cplCnapot.setTranam(knlCnapot.getTranam().negate()); //反向红字记账
			cplCnapot.setInacct(knlCnapot.getPyeeac());
			cplCnapot.setInacna(knlCnapot.getPyeena());
			cplCnapot.setOtacct(knlCnapot.getPyerac());
			cplCnapot.setOtacna(knlCnapot.getPyerna());
			CapitalTransDeal.dealCnapotVN(cplCnapot, iotype);
			
			
			//大小额 系统内往来 记账
			IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class); //内部户记账输入
			acdrIn.setAcbrch(acbrch);
			acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
			acdrIn.setCrcycd(tblKnaAcct.getCrcycd());
			acdrIn.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
			acdrIn.setToacct(knlCnapot.getPyerac());
			acdrIn.setToacna(knlCnapot.getPyerac());
			acdrIn.setTranam(knlCnapot.getTranam().negate());
			acdrIn.setBusino(para1.getParm_value1()); //业务编码
			acdrIn.setSubsac(para1.getParm_value2());//子户号
			
			inSrv.ioInAccr(acdrIn);
			
		}
		
		
		//额度冲正
		IoCaSevAccountLimit caSevAccountLimit = SysUtil.getInstance(IoCaSevAccountLimit.class);	
		IoAcRevQuota.InputSetter inputSetter = SysUtil.getInstance(IoAcRevQuota.InputSetter.class);
		IoAcRevQuota.Output output = SysUtil.getInstance(IoAcRevQuota.Output.class);
		
		inputSetter.setCustac(tblKnaAcdc.getCustac());
		inputSetter.setServsq(knlCnapot.getTransq()); //渠道来源流水
		inputSetter.setServdt(knlCnapot.getTrandt()); //渠道来源日期 
		//inputSetter.setServtp(CommUtil.toEnum(String.class, knlCnapot.getServtp()));
		inputSetter.setServtp(knlCnapot.getServtp());
//		if(iotype == E_IOTYPE.OUT){
//			inputSetter.setDcflag(E_RECPAY.PAY);
//		}else{
//			inputSetter.setDcflag(E_RECPAY.REC);
//		}
//		inputSetter.setCustie(E_YES___.NO); //绑定关系  默认为否
//		inputSetter.setTranam(knlCnapot.getTranam().negate());
		
		//账户额度恢复
		//caSevAccountLimit.SubAcctQuota(inputSetter, output);
		caSevAccountLimit.RevAcctQuota(inputSetter, output);
		
		//补充清算分录记录
		LsamClerIN clerIN = SysUtil.getInstance(LsamClerIN.class);
		clerIN.setBrchno(tblKnaAcct.getBrchno());
		clerIN.setCorpno(tblKnaAcct.getCorpno());
		clerIN.setCrcycd(tblKnaAcct.getCrcycd());
		clerIN.setIotype(knlCnapot.getIotype());
		clerIN.setIsdieb(E_YES___.YES);
		clerIN.setIshold(E_YES___.NO);
		clerIN.setSubsys(CommUtil.toEnum(E_SUBSYS.class, knlCnapot.getSubsys()));
		clerIN.setTranam(knlCnapot.getTranam());
		clerIN.setChrgam(chrgam);
		clerIN.setTrbrch(knlCnapot.getBrchno()); //交易机构
		
		CapitalTransLsamStrike.prcLsamClear(clerIN);
		
		//平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalanceLsam(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),clactp);
	}
	/**
	 * @Title: prcLsamClear 
	 * @Description: 大小额交易清算补充分录  
	 * @param LsamClerIN 大小额补充清算复合类型
	 * @author zhangan
	 * @date 2017年2月21日 下午3:21:47 
	 * @version V2.3.0
	 */
	public static void prcLsamClear(LsamClerIN clerIN){
		
		log.debug("<<======补充清算分录======>>");
		log.debug("<<======归属机构：[%s]", clerIN.getBrchno());
		log.debug("<<======归属法人：[%s]", clerIN.getCorpno());
		log.debug("<<======交易机构：[%s]", clerIN.getTrbrch());
		log.debug("<<======交易类型：[%s]", clerIN.getIotype().getLongName());
		log.debug("<<======是否挂账：[%s]", clerIN.getIshold().getLongName());
		log.debug("<<======是否冲正：[%s]", clerIN.getIsdieb().getLongName());
		log.debug("<<======交易金额：[%s]", clerIN.getTranam());
		log.debug("<<======费用金额：[%s]", clerIN.getChrgam());
		
		E_CLACTP clactp=null;
		
		if(clerIN.getSubsys() == E_SUBSYS.LM){//小额
			clactp=E_CLACTP._06;
		}else if(clerIN.getSubsys() == E_SUBSYS.BG){//大额
			clactp=E_CLACTP._05;
		}else {
			throw DpModuleError.DpstComm.BNAS1589();
		}
		
		//省中心机构
		IoBrchInfo centerBrch = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(clerIN.getBrchno());
		IoCheckBalance checkBal = SysUtil.getInstance(IoCheckBalance.class);
		String acbrch = centerBrch.getBrchno(); //获取省中心机构号
		String center = centerBrch.getCorpno(); //获取省中心法人号
		//交易机构法人代码
		String trcorp = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(clerIN.getTrbrch()).getCorpno();
		//获取系统内待清算业务编号和子户号 9930410404
		KnpParameter para1 = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", E_CLACTP._10.getValue(), "%", true);
		//网络核心与柜面核心系统间往来 9930410306
		KnpParameter para2 = KnpParameterDao.selectOne_odb1("InParm.clearbusi","out", "%", "%", true);
		//系统内资金清算往来-大小额系统 9930410402
		KnpParameter para3 = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", clactp.getValue(), "%", true);
		//省中心系统内待清算
		IoGlKnaAcct glAcct1 = checkBal.getGlKnaAcct(para1.getParm_value1(), acbrch, clerIN.getCrcycd(), para1.getParm_value2());
		//归属机构 往来核心与柜面核心系统间往来
		IoGlKnaAcct glAcct2 = checkBal.getGlKnaAcct(para2.getParm_value1(), clerIN.getBrchno(), clerIN.getCrcycd(), para2.getParm_value2());
		//交易机构 往来核心与柜面核心系统间往来
		IoGlKnaAcct glAcct3 = checkBal.getGlKnaAcct(para2.getParm_value1(), clerIN.getTrbrch(), clerIN.getCrcycd(), para2.getParm_value2());
		//省中心 系统内资金清算往来-大小额系统 
		IoGlKnaAcct glAcct4 = checkBal.getGlKnaAcct(para3.getParm_value1(), acbrch, clerIN.getCrcycd(), para3.getParm_value2());
		
		E_AMNTCD clincd = null;
		E_AMNTCD clotcd = null;
		
		BigDecimal cleram = BigDecimal.ZERO;
		BigDecimal tranam = BigDecimal.ZERO;
		
		if(clerIN.getIsdieb() == E_YES___.YES){ //隔日冲正
			if(clerIN.getIotype() == E_IOTYPE.IN){ //来账
				clincd = E_AMNTCD.DR;
				clotcd = E_AMNTCD.CR;
				
				tranam = clerIN.getTranam().negate();
				cleram = clerIN.getTranam().subtract(clerIN.getChrgam()).negate();
			}else if(clerIN.getIotype() == E_IOTYPE.OUT){ //往账
				clincd = E_AMNTCD.CR;
				clotcd = E_AMNTCD.DR;
				
				tranam = clerIN.getTranam().negate();
				cleram = clerIN.getTranam().add(clerIN.getChrgam()).negate();
			}
		}else if(clerIN.getIshold() == E_YES___.YES){ //冲正挂账
			if(clerIN.getIotype() == E_IOTYPE.IN){ //来账冲正挂账
				clincd = E_AMNTCD.CR;
				clotcd = E_AMNTCD.DR;
				
				tranam = clerIN.getTranam();
				cleram = clerIN.getTranam().add(clerIN.getChrgam());
				
			}else if(clerIN.getIotype() == E_IOTYPE.OUT){ //往账冲正挂账
				clincd = E_AMNTCD.DR;
				clotcd = E_AMNTCD.CR;
				
				tranam = clerIN.getTranam();
				cleram = clerIN.getTranam().subtract(clerIN.getChrgam());
			}
		}else{
			if(clerIN.getIotype() == E_IOTYPE.IN){ //来账
				clincd = E_AMNTCD.DR;
				clotcd = E_AMNTCD.CR;
				
				tranam = clerIN.getTranam();
				cleram = clerIN.getTranam().subtract(clerIN.getChrgam());
				
			}else if(clerIN.getIotype() == E_IOTYPE.OUT){ //往账
				clincd = E_AMNTCD.CR;
				clotcd = E_AMNTCD.DR;
				
				tranam = clerIN.getTranam();
				cleram = clerIN.getTranam().add(clerIN.getChrgam());
			}
		}
		
		//补充 归属机构 网络柜面核心间往来  系统内资金清算往来-大小额支付系统
		IoAccountClearInfo clearInfo_C = SysUtil.getInstance(IoAccountClearInfo.class);
		IoAccountClearInfo clearInfo_D = SysUtil.getInstance(IoAccountClearInfo.class);
		//贷 归属机构 30410306 
		clearInfo_C.setCorpno(clerIN.getCorpno());//账号归属法人
		clearInfo_C.setAcctno(glAcct2.getAcctno());//系统内往来账号
		clearInfo_C.setAcctna(glAcct2.getAcctna());//系统内往来账户名称
		clearInfo_C.setProdcd(para2.getParm_value1());//产品编码
		clearInfo_C.setAcctbr(clerIN.getBrchno());//账务机构
		clearInfo_C.setCrcycd(clerIN.getCrcycd());//币种
		clearInfo_C.setAmntcd(clincd);//借贷标志
		if(clerIN.getBrchno().equals(clerIN.getTrbrch())){
			
			clearInfo_C.setTranam(tranam);//交易金额
		}else{
			clearInfo_C.setTranam(cleram);//交易金额
		}
		clearInfo_C.setClerst(E_CLERST.WAIT);//清算状态
		if(clerIN.getBrchno().equals(clerIN.getTrbrch())){
			//如果交易机构和账户机构相同，则不走系统内清算
			clearInfo_C.setClactp(clactp);
			clearInfo_C.setToacct(glAcct4.getAcctno());//9930410402
		}else{
			
			clearInfo_C.setToacct(glAcct1.getAcctno());//9930410404
			clearInfo_C.setClactp(E_CLACTP._10);//
		}
		clearInfo_C.setToacbr(acbrch);//对方机构
		
		//登记会计流水清算信息
		SysUtil.getInstance(IoAccountSvcType.class).registKnsAcsqCler(clearInfo_C);
		//借 省中心 30410402
		clearInfo_D.setCorpno(center);//账号归属法人
		clearInfo_D.setAcctno(glAcct4.getAcctno());//系统内往来账号
		clearInfo_D.setAcctna(glAcct4.getAcctna());//系统内往来账户名称
		clearInfo_D.setProdcd(para3.getParm_value1());//产品编码
		clearInfo_D.setAcctbr(acbrch);//账务机构
		if(clerIN.getBrchno().equals(clerIN.getTrbrch())){
			clearInfo_D.setToacbr(clerIN.getBrchno());//账户归属机构
			clearInfo_D.setToacct(glAcct2.getAcctno());//
			
		}else{
			clearInfo_D.setToacbr(clerIN.getTrbrch());//交易机构
			clearInfo_D.setToacct(glAcct3.getAcctno());//交易机构 9930410306			
		}
			
		clearInfo_D.setCrcycd(clerIN.getCrcycd());//币种
		clearInfo_D.setAmntcd(clotcd);//借贷标志
		clearInfo_D.setTranam(tranam);//交易金额
		clearInfo_D.setClerst(E_CLERST.WAIT);//清算状态
		clearInfo_D.setClactp(clactp);//
		//登记会计流水清算信息
		SysUtil.getInstance(IoAccountSvcType.class).registKnsAcsqCler(clearInfo_D);
		
		//交易机构与账户归属机构一致不需要补充清算记录
				if(CommUtil.equals(clerIN.getBrchno(), clerIN.getTrbrch())){
					return;
				}
		
		clearInfo_C = SysUtil.getInstance(IoAccountClearInfo.class);
		clearInfo_D = SysUtil.getInstance(IoAccountClearInfo.class);
		//贷 省中心 30410404 
		clearInfo_C.setCorpno(center);//账号归属法人
		clearInfo_C.setAcctno(glAcct1.getAcctno());//系统内往来账号
		clearInfo_C.setAcctna(glAcct1.getAcctna());//系统内往来账户名称
		clearInfo_C.setProdcd(para1.getParm_value1());//产品编码
		clearInfo_C.setAcctbr(acbrch);//账务机构
		clearInfo_C.setCrcycd(clerIN.getCrcycd());//币种
		clearInfo_C.setAmntcd(clincd);//借贷标志		
		clearInfo_C.setTranam(cleram);//交易金额
		clearInfo_C.setClerst(E_CLERST.WAIT);//清算状态
		clearInfo_C.setClactp(E_CLACTP._10);//系统内清算

		clearInfo_C.setToacbr(clerIN.getTrbrch());//对方机构
		clearInfo_C.setToacct(glAcct3.getAcctno());//			
		
		SysUtil.getInstance(IoAccountSvcType.class).registKnsAcsqCler(clearInfo_C);
		
		//借 交易机构 30410306
		clearInfo_D.setCorpno(trcorp);//账号归属法人
		clearInfo_D.setAcctno(glAcct3.getAcctno());//系统内往来账号
		clearInfo_D.setAcctna(glAcct3.getAcctna());//系统内往来账户名称
		clearInfo_D.setProdcd(para2.getParm_value1());//产品编码
		clearInfo_D.setAcctbr(clerIN.getTrbrch());//账务机构
		clearInfo_D.setCrcycd(clerIN.getCrcycd());//币种
		clearInfo_D.setAmntcd(clotcd);//借贷标志
		clearInfo_D.setTranam(cleram);//交易金额
		clearInfo_D.setClerst(E_CLERST.WAIT);//清算状态
		if(clerIN.getIotype() == E_IOTYPE.OUT){
			
			clearInfo_D.setClactp(clactp);//
		}else{
			
			clearInfo_D.setClactp(E_CLACTP._10);//
		}
		clearInfo_D.setToacbr(clerIN.getBrchno());//对方机构
		clearInfo_D.setToacct(glAcct1.getAcctno());//
		//登记会计流水清算信息
		SysUtil.getInstance(IoAccountSvcType.class).registKnsAcsqCler(clearInfo_D);
		
		clearInfo_C = SysUtil.getInstance(IoAccountClearInfo.class);
		clearInfo_D = SysUtil.getInstance(IoAccountClearInfo.class);
		//贷 交易机构 30410306
		clearInfo_C.setCorpno(trcorp);//账号归属法人
		clearInfo_C.setAcctno(glAcct3.getAcctno());//系统内往来账号
		clearInfo_C.setAcctna(glAcct3.getAcctna());//系统内往来账户名称
		clearInfo_C.setProdcd(para2.getParm_value1());//产品编码
		clearInfo_C.setAcctbr(clerIN.getTrbrch());//账务机构
		clearInfo_C.setCrcycd(clerIN.getCrcycd());//币种
		clearInfo_C.setAmntcd(clincd);//借贷标志
		clearInfo_C.setTranam(tranam);//交易金额
		clearInfo_C.setClerst(E_CLERST.WAIT);//清算状态
		if(clerIN.getIotype() == E_IOTYPE.IN){
			
			clearInfo_C.setClactp(clactp);//
		}else{
			
			clearInfo_C.setClactp(E_CLACTP._10);//
		}		
		clearInfo_C.setToacbr(clerIN.getBrchno());//对方机构
		clearInfo_C.setToacct(glAcct1.getAcctno());//
		//登记会计流水清算信息
		SysUtil.getInstance(IoAccountSvcType.class).registKnsAcsqCler(clearInfo_C);
		
		//借 省中心 30410404
		clearInfo_D.setCorpno(center);//账号归属法人
		clearInfo_D.setAcctno(glAcct1.getAcctno());//系统内往来账号
		clearInfo_D.setAcctna(glAcct1.getAcctna());//系统内往来账户名称
		clearInfo_D.setProdcd(para1.getParm_value1());//产品编码
		clearInfo_D.setAcctbr(acbrch);//账务机构
		clearInfo_D.setToacbr(clerIN.getBrchno());//对方机构
		clearInfo_D.setCrcycd(clerIN.getCrcycd());//币种
		clearInfo_D.setAmntcd(clotcd);//借贷标志
		clearInfo_D.setTranam(tranam);//交易金额
		if(clerIN.getBrchno().equals(clerIN.getTrbrch())){
			
			clearInfo_D.setTranam(tranam);//交易金额
		}else{
			
			clearInfo_D.setTranam(cleram);//交易金额
		}		
		clearInfo_D.setClerst(E_CLERST.WAIT);//清算状态
		clearInfo_D.setClactp(E_CLACTP._10);//
		clearInfo_D.setToacct(glAcct2.getAcctno());//
		
		SysUtil.getInstance(IoAccountSvcType.class).registKnsAcsqCler(clearInfo_D);
		
		//登记清算补记账信息
		clearInfo_C = SysUtil.getInstance(IoAccountClearInfo.class);
		clearInfo_D = SysUtil.getInstance(IoAccountClearInfo.class);
		
		//30410404
		clearInfo_C.setCorpno(center);//账务机构法人行
		clearInfo_C.setAcctno(glAcct1.getAcctno());//账号
		clearInfo_C.setAcctna(glAcct1.getAcctna());//账户名称
		clearInfo_C.setProdcd(para1.getParm_value1());//产品编码
		clearInfo_C.setAcctbr(acbrch);//账务机构
		clearInfo_C.setToacbr(clerIN.getBrchno());//对方机构
		clearInfo_C.setCrcycd(clerIN.getCrcycd());//币种
		clearInfo_C.setAmntcd(clincd);//借贷标志
		clearInfo_C.setTranam(cleram);//交易金额
		clearInfo_C.setClerst(E_CLERST.WAIT);//清算状态
		clearInfo_C.setClactp(E_CLACTP._10);//系统内往来账户类型
		clearInfo_C.setToacct(null);//
		//登记系统内跨法人会计流水清算信息
		SysUtil.getInstance(IoAccountSvcType.class).registKnsAcsqClin(clearInfo_C);
		
		//30410404
		clearInfo_D.setCorpno(center);//账务机构法人行
		clearInfo_D.setAcctno(glAcct1.getAcctno());//账号
		clearInfo_D.setAcctna(glAcct1.getAcctna());//账户名称
		clearInfo_D.setProdcd(para1.getParm_value1());//产品编码
		clearInfo_D.setAcctbr(acbrch);//账务机构
		clearInfo_D.setToacbr(clerIN.getTrbrch());//对方机构
		clearInfo_D.setCrcycd(clerIN.getCrcycd());//币种
		clearInfo_D.setAmntcd(clotcd);//借贷标志
		clearInfo_D.setTranam(cleram);//交易金额
		clearInfo_D.setClerst(E_CLERST.WAIT);//清算状态
		clearInfo_D.setClactp(E_CLACTP._10);//系统内往来账户类型
		clearInfo_D.setToacct(null);//
		//登记系统内跨法人会计流水清算信息
		SysUtil.getInstance(IoAccountSvcType.class).registKnsAcsqClin(clearInfo_D);
	}

}
