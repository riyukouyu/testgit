package cn.sunline.ltts.busi.dptran.trans.close;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTran;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTranDao;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.acct.DpCloseAcctno;
import cn.sunline.ltts.busi.dp.acct.DpCloseCustac;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.AccChngbrDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ClsAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcal;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaSign;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbClac;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseDetailOT;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseOT;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CLOSST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SLEPST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SPPRST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSATP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSTAT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.PbEnumType;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;



public class clacbt {

	private static BizLog log = BizLogUtil.getBizLog(clacbt.class);
	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Output output){
		//poc增加审计日志
		ApDataAudit.regLogOnInsertBusiPoc(null);
		//jym add 交易金额
		BigDecimal tranam = input.getClosam();
		RunEnvsComm runEnvs = CommTools.getBaseRunEnvs();
		KnsTran knsTran = KnsTranDao.selectOne_odb1(runEnvs.getTrxn_seq(), runEnvs.getTrxn_date(), true);
		knsTran.setTranam(tranam);
		KnsTranDao.updateOne_odb1(knsTran);
		
		log.debug("<<=====统一后管销户，账号:[%s]处理开始", input.getCardno());
		
		String cardno = input.getCardno();
		String timetm =DateTools2.getCurrentTimestamp();
		
		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		//检查电子账户是否存在
		IoCaKnaAcdc acdc = caqry.getKnaAcdcOdb2(cardno, false);
		if(CommUtil.isNull(acdc)){
			throw DpModuleError.DpstComm.BNAS0750();
		}
		if(acdc.getStatus() == E_DPACST.CLOSE){
			throw DpModuleError.DpstComm.BNAS0740();
		}
		//jym add 有定期的不让销户
		List<KnaFxac> knaFxac = KnaFxacDao.selectAll_odb4(acdc.getCustac(), E_DPACST.NORMAL, false);
		if(CommUtil.isNotNull(knaFxac)) {
			throw DpModuleError.DpstComm.E9999("存在定期产品，不允许销户");
		}
		E_CUACST cuacst = cagen.selCaStInfo(acdc.getCustac());
		if(cuacst == E_CUACST.CLOSED){
			throw DpModuleError.DpstComm.BNAS0740();
		}
		
		//检查销户登记簿是否有未处理完成的记录
		IoCaKnbClac tblKnbClac = DpAcctDao.selKnbClacByDoubleStat(acdc.getCustac(), E_CLSTAT.DEAL,E_CLSTAT.TRSC, false);
		if(CommUtil.isNotNull(tblKnbClac)){
			if(tblKnbClac.getStatus() == E_CLSTAT.DEAL){ //如果是预销户状态，则作废该条记录，并继续向下做销户处理
				DpAcctDao.updKnbClacStatBySeq(E_CLSTAT.FAIL, tblKnbClac.getClossq(),timetm);
			}else{ //如果是转账成功，则需要等待转账结果，不允许继续做销户交易
				throw DpModuleError.DpstComm.BNAS0998();	
			}
		}
		
		//输入参数检查
		BigDecimal totPaidam = chkParam(input);
		
		property.setPaidam(totPaidam); //收费金额
		
		//获取电子账户类型
		E_ACCATP accatp = cagen.qryAccatpByCustac(acdc.getCustac());
		
		IoCaKnaCust cust = caqry.getKnaCustByCustacOdb1(acdc.getCustac(), true);
		/**
		if(!CommUtil.equals(cust.getBrchno(),CommTools.getBaseRunEnvs().getTrxn_branch())){
			throw DpModuleError.DpstComm.E9999("电子账户所属机构与交易机构不一致");
		}
		**/
		// 查询出用户ID
		//IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_accsByCustno(cust.getCustno(), true, E_STATUS.NORMAL);
//		IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(IoSrvCfPerson.class).getCifCustAccsByCustno(cust.getCustno(), E_STATUS.NORMAL, true);
		// 查询出绑定手机号
		IoCaKnaAcal cplKnaAcal = AccChngbrDao.selKnaacalByCus(acdc.getCustac(), E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);			
		
		//若存在绑定手机号则登记不存在不做登记
		if(CommUtil.isNotNull(cplKnaAcal)){
			property.setAcalno(cplKnaAcal.getAcalno());// 电子账户绑定手机号
		}
		
		// 查询证件类型证件号码
		//IoCucifCust cplCifCust = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_selectOne_odb1(cust.getCustno(), true);
//		IoSrvCfPerson.IoGetCifCust.InputSetter queryCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//		queryCust.setCustno(cust.getCustno());
//		IoSrvCfPerson.IoGetCifCust.Output cplCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//		SysUtil.getInstance(IoSrvCfPerson.class).getCifCust(queryCust, cplCifCust);
		
		// 检查输入户名和电子账户户名是否一致
//		if (!CommUtil.equals(cplCifCust.getCustna(), input.getCustna())) {
//			throw DpModuleError.DpstComm.BNAS0531();
//		}
//		
//		// 输入存款人证件号码与电子账户所属客户证件号码必须一致
//		if (!CommUtil.equals(cplCifCust.getIdtfno(), input.getIdtfno())) {
//			throw DpModuleError.DpstComm.BNAS0312();
//		}
		
		property.setClstat(E_CLSTAT.DEAL); //初始化执行状态标志
		
		property.setBrchno(cust.getBrchno());
		property.setAccatp(accatp);
		property.setCustac(acdc.getCustac()); //交易后处理需要用到参数
//		property.setCustid(cplCifCustAccs.getCustid()); // 用户ID
		property.setClacfg(E_YES___.YES);//收费销户标志
		//property.setAcalno(cplKnaAcal.getAcalno());// 电子账户绑定手机号
		
		//获取电子账户客户号状态,并设置后续步骤的执行标志,如果客户化状态是预销户，则可直接进入交易后处理，在交易后处理中获取原销户流水和日期进行返回
		
		//查询签约表获取签约号
		IoCaKnaSign sign = caqry.getKnaSignOdb1(acdc.getCustac(), E_SIGNTP.ZNCXL, E_SIGNST.QY, false);
		if(CommUtil.isNull(sign)){
			property.setSignno(null);
			property.setIssign(E_YES___.NO);
		}else{
			property.setSignno(sign.getSignno());
			property.setIssign(E_YES___.YES);
		}
		
		//初始化收费参数
		if(input.getChaglg() == E_YES___.YES){
			property.setChgflg(E_CHGFLG.ALL);
		}
		
		property.setCuacst(cuacst);
		
		if(cuacst == E_CUACST.PRECLOS){ //预销户
			property.setClstat(E_CLSTAT.SUCC); //设置为空，表示后续步骤不执行
		}
		
		if(CommUtil.isNull(input.getClosam())){
			throw DpModuleError.DpstAcct.BNAS0289();
		}
		
		//检查对方账号的状态和状态字
		if(input.getTractp() == E_CLSATP.CUSTAC){
			
			if (CommUtil.equals(input.getTracno(), input.getCardno())) {
				throw DpModuleError.DpstComm.BNAS0025();
			}
			
			IoCaKnaAcdc toAcdc = ActoacDao.selKnaAcdc(input.getTracno(), false);
			if(CommUtil.isNull(toAcdc) || toAcdc.getStatus() == E_DPACST.CLOSE){
				throw DpModuleError.DpstComm.BNAS0327();
			}
			
			E_ACCATP toActp = cagen.qryAccatpByCustac(toAcdc.getCustac());
			
			AcTranfeChkIN chkIN = SysUtil.getInstance(AcTranfeChkIN.class);
			chkIN.setAccatp(toActp);
			chkIN.setCapitp(E_CAPITP.CL701);
			chkIN.setCardno(toAcdc.getCardno());
			chkIN.setCustac(toAcdc.getCustac());
			chkIN.setCustna(input.getTracna());
			chkIN.setOpcard(input.getCardno()); //销户账号
			chkIN.setOppona(input.getCustna());
			chkIN.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		
			KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
			if(toActp == E_ACCATP.WALLET){
				tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(toAcdc.getCustac(), E_ACSETP.MA); //钱包账户
				//钱包账户需要检查限额
				//BigDecimal hdmxmy = KnaAcctAddtDao.selectOne_odb1(tblKnaAcct.getAcctno(), true).getHigham();
				BigDecimal hdmxmy = ConvertUtil.toBigDecimal(KnpParameterDao.selectOne_odb1("DpParm.maxbln", "3", "%", "%", true).getParm_value1());
				if(!CommUtil.equals(hdmxmy,BigDecimal.ZERO)){
					if(CommUtil.compare(input.getClosam().subtract(property.getPaidam()), hdmxmy.subtract(tblKnaAcct.getOnlnbl())) > 0){
						throw CaError.Eacct.BNAS0956();
					}
				}
			}else{
				tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(toAcdc.getCustac(), E_ACSETP.SA); //钱包账户
			}
			
			property.setTblKnaAcct(tblKnaAcct);
			property.setTobrch(tblKnaAcct.getBrchno());
			
		}
		
		
		
		
		
		property.setCustno(cust.getCustno());
		
		property.setAcseno(null); //子账号序号
		property.setCsextg(E_CSEXTG.CASH);
		
		
		
		
		property.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());
		property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());
		property.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		property.setBusisq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		property.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
	}

	/**
	 * @Title: clsCustac 
	 * @Description:  存款子账户结息销户
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年10月24日 上午9:54:25 
	 * @version V2.3.0
	 */
	public static void clsCustac( final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Output output){
		/**
		 * 1.判断账户类型，一、二类户调用电子账户销户服务后再调用结算户结息服务
		 * 2.三类户直接调用结算户结息服务
		 * 3.校验销户金额与交易金额是否一致
		 * 4.调用修改账户状态服务
		 */
		BigDecimal interest = BigDecimal.ZERO; 
		E_ACCATP accatp = property.getAccatp(); //账户类型
		KnaAcct acct = SysUtil.getInstance(KnaAcct.class);
		
		InterestAndIntertax cplint = SysUtil.getInstance(InterestAndIntertax.class);
		
//		String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		
		if(property.getClstat() == E_CLSTAT.DEAL){
			if(accatp == E_ACCATP.WALLET){  //三类户
				acct = CapitalTransDeal.getSettKnaAcctSub(property.getCustac(), E_ACSETP.MA); //钱包账户
				
//				CommTools.getBaseRunEnvs().setBusi_org_id(acct.getCorpno());
				
				IoDpCloseIN clsin = SysUtil.getInstance(IoDpCloseIN.class);
				
				cplint = DpCloseAcctno.prcCurrInterest(acct, clsin);
				interest = cplint.getDiffam();
				
				IoDpCloseDetailOT detl_MA = SysUtil.getInstance(IoDpCloseDetailOT.class);
				detl_MA.setClinst(interest);
				detl_MA.setClprcp(acct.getOnlnbl());
				detl_MA.setDpactp(E_ACSETP.MA);
				detl_MA.setClatax(cplint.getIntxam());//利息税
				detl_MA.setCltxin(cplint.getInstam());//应税利息
				
				output.getClprirInfoList().add(detl_MA); //销户利息清单
				
				property.setSettbl(acct.getOnlnbl().add(interest)); //设置销户目标金额
			}else{
				acct = CapitalTransDeal.getSettKnaAcctSub(property.getCustac(), E_ACSETP.SA);
				
//				CommTools.getBaseRunEnvs().setBusi_org_id(acct.getCorpno());
				
				IoDpCloseIN clsin = SysUtil.getInstance(IoDpCloseIN.class);
				
				clsin.setAccatp(accatp);
				clsin.setCardno(input.getCardno());
				clsin.setCrcycd(acct.getCrcycd());
				clsin.setCustac(property.getCustac());
				clsin.setToacct(input.getTracno());
				clsin.setTobrch(property.getTobrch());
				clsin.setToname(input.getTracna());
				clsin.setSmrycd(input.getSmrycd());
				
				IoDpCloseOT clsot = DpCloseCustac.CloseCustac(clsin); //调用电子账户销户服务
				
			    cplint = DpCloseAcctno.prcCurrInterest(acct, clsin); //活期结算户结息
				interest = cplint.getDiffam();
				
				IoDpCloseDetailOT detl_SA = SysUtil.getInstance(IoDpCloseDetailOT.class);
				detl_SA.setClinst(interest);
				detl_SA.setClprcp(acct.getOnlnbl());
				detl_SA.setDpactp(E_ACSETP.SA);
				detl_SA.setClatax(cplint.getIntxam());//利息税
				detl_SA.setCltxin(cplint.getInstam());//应税利息
				
				output.getClprirInfoList().add(detl_SA); //销户利息清单
				output.getClprirInfoList().addAll(clsot.getDetail());
				
				property.setSettbl(acct.getOnlnbl().add(clsot.getSettbl()).add(interest)); //设置销户目标金额
			}
		}else{ //已经预销户的账户需要在此做结息
			acct = CapitalTransDeal.getSettKnaAcctAc(property.getCustac());
			
//			CommTools.getBaseRunEnvs().setBusi_org_id(acct.getCorpno());
			
			IoDpCloseIN clsin = SysUtil.getInstance(IoDpCloseIN.class);
			
			cplint = DpCloseAcctno.prcCurrInterest(acct, clsin); //活期结算户结息
			interest = cplint.getDiffam();
			
			IoDpCloseDetailOT detl = SysUtil.getInstance(IoDpCloseDetailOT.class);
			detl.setClinst(interest);
			detl.setClprcp(acct.getOnlnbl());
			detl.setDpactp(acct.getAcsetp());
			detl.setClatax(cplint.getIntxam());//利息税
			detl.setCltxin(cplint.getInstam());//应税利息
			
			output.getClprirInfoList().add(detl); //销户利息清单
			
			property.setSettbl(acct.getOnlnbl().add(interest)); //设置销户目标金额
		}
		
		
//		CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);
		
		property.setCrcycd(acct.getCrcycd());
		property.setLinkno(null);
		property.setAcctno(acct.getAcctno());
		
		
		DpAcctSvcType dpSrv = SysUtil.getInstance(DpAcctSvcType.class); //存款记账服务
		
		SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class); //电子账户存入记账复合类型
		DrawDpAcctIn drawin = SysUtil.getInstance(DrawDpAcctIn.class); //支取服务输入参数
		
		if(CommUtil.compare(cplint.getInstam(), BigDecimal.ZERO) > 0){ //利息存入结算户
			//调用存入服务，存入利息\
			
			saveIn.setAcctno(acct.getAcctno()); //结算账户、钱包账户
			saveIn.setBankcd("");
			saveIn.setBankna("");
			saveIn.setCardno(input.getCardno());
			saveIn.setCrcycd(acct.getCrcycd());
			saveIn.setCustac(acct.getCustac());
			saveIn.setOpacna(acct.getAcctna());
			saveIn.setOpbrch(acct.getBrchno());
			saveIn.setRemark("子账户销户结息转入");
			saveIn.setSmrycd(BusinessConstants.SUMMARY_SX);
			saveIn.setToacct(acct.getAcctno());
			saveIn.setTranam(cplint.getInstam());
			saveIn.setIschck(E_YES___.NO);
			saveIn.setStrktg(E_YES___.NO);
			
			dpSrv.addPostAcctDp(saveIn);
		}
		
		//利息税入账
		if(CommUtil.compare(cplint.getIntxam(), BigDecimal.ZERO) > 0){
			//结算户支取记账处理				
			drawin.setAcctno(acct.getAcctno()); //做支取的负债账号
			drawin.setAuacfg(E_YES___.NO);
			drawin.setCardno(input.getCardno());
			drawin.setCrcycd(acct.getCrcycd());
			drawin.setCustac(acct.getCustac());
			drawin.setLinkno(null);
			drawin.setOpacna(acct.getAcctna());
			drawin.setToacct(acct.getAcctno()); //结算账号
			drawin.setTranam(cplint.getIntxam());
			drawin.setSmrycd(BusinessConstants.SUMMARY_JS);// 缴税
			saveIn.setStrktg(E_YES___.NO);
			dpSrv.addDrawAcctDp(drawin);
			
			
		}
		
		log.debug("销户金额:[%s]", input.getClosam());
		log.debug("目标金额:[%s]", property.getSettbl());
		
		if(CommUtil.compare(input.getClosam(), property.getSettbl()) != 0){
			throw DpModuleError.DpstComm.BNAS0288();
		}
		
		//如果原状态是休眠,则修改休眠登记簿状态是休眠转销户
		if(property.getCuacst() == E_CUACST.DORMANT){
			String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
			String trantm = BusiTools.getBusiRunEnvs().getTrantm();
			String timetm = DateTools2.getCurrentTimestamp();
			ActoacDao.updKnbSlepStat(E_SPPRST.CNCL, trandt, trantm, E_SLEPST.CANCEL, acct.getCustac(), E_SLEPST.SLEP,timetm);
		}
		if(CommUtil.compare(property.getSettbl(), BigDecimal.ZERO) == 0){
			property.setCuacst(E_CUACST.CLOSED); //零余额销户，设置客户化状态为销户 
		}
	}
	
	/**
	 * @Title: dealClbt 
	 * @Description:销户金额转电子账户不需要再调用销户转账，直接在销户中进行记账登记  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年10月24日 上午11:32:38 
	 * @version V2.3.0
	 */
	public static void dealClbt( final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Output output){
		
		//property.setAuacfg(E_YES___.NO);
		
		//收款人类型是电子账户，不需要经过支付2.0和支付系统,直接在此处进行记账登记
		if(input.getTractp() == E_CLSATP.CUSTAC){
			DpAcctSvcType dpSrv = SysUtil.getInstance(DpAcctSvcType.class); //存款记账服务
			BigDecimal tranam = property.getSettbl().subtract(property.getPaidam()); //获取转出金额
			//设置记账金额
			property.setTranam(tranam);
			
			if(CommUtil.compare(tranam, BigDecimal.ZERO) > 0){
				DrawDpAcctIn drawIn = SysUtil.getInstance(DrawDpAcctIn.class); //电子账户支取记账复合类型
				drawIn.setAcctno(property.getAcctno());
				drawIn.setAcseno(property.getAcseno());
				//drawIn.setAuacfg(property.getAuacfg());
				drawIn.setBankcd(null);
				drawIn.setBankna(null);
				drawIn.setCardno(input.getCardno());
				drawIn.setCrcycd(property.getCrcycd());
				drawIn.setCustac(property.getCustac());
				drawIn.setIschck(E_YES___.NO); //不检查支取控制，无条件支取
				drawIn.setOpacna(input.getTracna());
				drawIn.setOpbrch(property.getTobrch());
				drawIn.setRemark("销户转电子账户记账");
				drawIn.setSmrycd(BusinessConstants.SUMMARY_XH);
				drawIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_XH));
				drawIn.setStrktg(E_YES___.YES); //分布式需要提供冲正
				drawIn.setToacct(input.getTracno());
				drawIn.setTranam(tranam);
				dpSrv.addDrawAcctDp(drawIn);
				
				KnaAcct tblKnaAcct = property.getTblKnaAcct();
				SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class); //电子账户存入记账复合类型
				saveIn.setAcctno(tblKnaAcct.getAcctno());
				saveIn.setAcseno(null);
				saveIn.setBankcd(null);
				saveIn.setBankna(null);
				saveIn.setCardno(input.getTracno());
				saveIn.setCrcycd(tblKnaAcct.getCrcycd());
				saveIn.setCustac(tblKnaAcct.getCustac());
				saveIn.setIschck(E_YES___.YES); //检查存入控制
				saveIn.setLinkno(null);
				saveIn.setOpacna(input.getCustna());
				saveIn.setOpbrch(property.getBrchno());
				saveIn.setRemark("柜面销户转电子账户对方账户记账");
				saveIn.setSmrycd(BusinessConstants.SUMMARY_ZR);
				saveIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_ZR));
				saveIn.setStrktg(E_YES___.YES); //本交易不提供冲正
				saveIn.setToacct(input.getCardno());
				saveIn.setTranam(tranam);
				dpSrv.addPostAcctDp(saveIn);
				
				//销户转入电子账户，需要添加休眠转正常的结息处理
				E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(tblKnaAcct.getCustac());
				CapitalTransDeal.dealAcctStatAndSett(cuacst, tblKnaAcct);
				
				//add by chenlk 20161119  增加柜员操作额度校验
/*				if(CommUtil.compare(tranam, BigDecimal.ZERO)>0 && BusiTools.isCounterChannel()){
					//机构、柜员额度验证
					IoBrchUserQt ioBrchUserQt = SysUtil.getInstance(IoBrchUserQt.class);
					ioBrchUserQt.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
					ioBrchUserQt.setBusitp(E_BUSITP.TR);
					ioBrchUserQt.setCrcycd(tblKnaAcct.getCrcycd());
					ioBrchUserQt.setTranam(tranam);
					ioBrchUserQt.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());
					SysUtil.getInstance(IoSrvPbBranch.class).selBrchUserQt(ioBrchUserQt);			
					
				}*/
			}
			
			//收款人账号类型是电子账号，设置客户化状态为销户
			property.setCuacst(E_CUACST.CLOSED);
		}else if (input.getTractp() == E_CLSATP.INACCT) {
			DpAcctSvcType dpSrv = SysUtil.getInstance(DpAcctSvcType.class); //存款记账服务
			BigDecimal tranam = property.getSettbl().subtract(property.getPaidam()); //获取转出金额
			//设置记账金额
			property.setTranam(tranam);
			
			if(CommUtil.compare(tranam, BigDecimal.ZERO) > 0){
				//销户支取记账
				DrawDpAcctIn drawIn = SysUtil.getInstance(DrawDpAcctIn.class); //电子账户支取记账复合类型
				drawIn.setAcctno(property.getAcctno());
				drawIn.setAcseno(property.getAcseno());
				//drawIn.setAuacfg(property.getAuacfg());
				drawIn.setBankcd(null);
				drawIn.setBankna(null);
				drawIn.setCardno(input.getCardno());
				drawIn.setCrcycd(property.getCrcycd());
				drawIn.setCustac(property.getCustac());
				drawIn.setIschck(E_YES___.NO); //不检查支取控制，无条件支取
				drawIn.setOpacna(input.getTracna());
				drawIn.setOpbrch(property.getTobrch());
				drawIn.setRemark("销户转电子账户记账");
				drawIn.setSmrycd(BusinessConstants.SUMMARY_XH);
				drawIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_XH));
				drawIn.setStrktg(E_YES___.YES); //本交易不提供冲正
				drawIn.setToacct(input.getTracno());
				drawIn.setTranam(tranam);
				dpSrv.addDrawAcctDp(drawIn);
				
				//内部户存入记账
				KnpParameter para = SysUtil.getInstance(KnpParameter.class);
				para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", "%", "%", true);
				IaAcdrInfo info = SysUtil.getInstance(IaAcdrInfo.class);
				info.setAcctno(input.getTracno());
				info.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
				info.setBusino(para.getParm_value1());//业务编码
				info.setSubsac(para.getParm_value2());//子户号
				info.setCrcycd(property.getCrcycd());
				info.setSmrycd(BusinessConstants.SUMMARY_XH);
				info.setToacct(input.getCardno());
				info.setToacna(input.getCustna());
				info.setTranam(tranam);
				info.setTrantp(E_TRANTP.TR);
				SysUtil.getRemoteInstance(IoInAccount.class).IoInAccrAdm(info);
				property.setBusino(para.getParm_value1());
				property.setChckdt(CommTools.getBaseRunEnvs().getTrxn_date());
				property.setCapitp(E_CAPITP.AL999);
				property.setIoflag(E_IOFLAG.OUT);
			}
			//收款人账号类型是电子账号，设置客户化状态为销户
			property.setCuacst(E_CUACST.CLOSED);
			
		}
		
		
	}
	
	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Output output){

		IoCaKnbClac cplKnbClac = SysUtil.getInstance(IoCaKnbClac.class);
		
		cplKnbClac.setAccttp(property.getAccatp());
		cplKnbClac.setAgidno(input.getDlidno()); //经办人证件号码
		cplKnbClac.setAgidtp(input.getDlidtp()); //经办人证件类型
		cplKnbClac.setAgntna(input.getDealna()); //经办人姓名
		cplKnbClac.setClosam(property.getSettbl());
		cplKnbClac.setDrawwy(input.getDrawwy());
		cplKnbClac.setClosbr(CommTools.getBaseRunEnvs().getTrxn_branch());
		cplKnbClac.setClosdt(CommTools.getBaseRunEnvs().getTrxn_date());
		cplKnbClac.setClosrs(input.getClreas()); //销户原因
		cplKnbClac.setClossq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		cplKnbClac.setClossv(CommTools.getBaseRunEnvs().getChannel_id());
		cplKnbClac.setClosus(CommTools.getBaseRunEnvs().getTrxn_teller());
		cplKnbClac.setCloswy(input.getClostp()); //销户方式
		//cplKnbClac.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		cplKnbClac.setCrcycd(property.getCrcycd());
		cplKnbClac.setCsextg(E_CSEXTG.CASH);
		cplKnbClac.setCustac(property.getCustac());
		cplKnbClac.setCustna(input.getCustna());
		cplKnbClac.setDocuno(input.getDocmno()); //证明文件编号
		cplKnbClac.setDocutp(input.getDocmna()); //证明文件
		cplKnbClac.setTnacna(input.getTracna());
		cplKnbClac.setTnacno(input.getTracno());
		cplKnbClac.setTnactp(input.getTractp());
		cplKnbClac.setAcalno(property.getAcalno());
		cplKnbClac.setCustid(property.getCustid());
		if(property.getCuacst() == E_CUACST.CLOSED){
			cplKnbClac.setStatus(E_CLSTAT.SUCC);
		}else{
			cplKnbClac.setStatus(E_CLSTAT.DEAL);
		}
		cplKnbClac.setAcctno(property.getAcctno());
		
		String timetm=DateTools2.getCurrentTimestamp();
		
		SysUtil.getInstance(IoCaSevQryTableInfo.class).saveKnbClac(cplKnbClac);
		//平衡性检查
		if(input.getTractp() == E_CLSATP.CUSTAC && !property.getBrchno().equals(property.getTobrch())){ //转电子账户，且不同机构
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._10);
		}else{
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), null);
		}
		
		
		
		if (property.getClstat() == E_CLSTAT.DEAL
				|| (property.getClstat() == E_CLSTAT.SUCC && input.getTractp() == E_CLSATP.CUSTAC)
				|| (property.getClstat() == E_CLSTAT.SUCC && input.getTractp() == E_CLSATP.INACCT)) {
			if(property.getCuacst() == E_CUACST.CLOSED){
				ClsAcctIn cplClsAcctIn = SysUtil.getInstance(ClsAcctIn.class);
				
				cplClsAcctIn.setCardno(input.getCardno());
				cplClsAcctIn.setCustac(property.getCustac());
				cplClsAcctIn.setCustna(input.getCustna());
				cplClsAcctIn.setCustno(property.getCustno());
				cplClsAcctIn.setClossq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
				//注销电子账户
				//DpAcctProc.prcAcctst(cplClsAcctIn);
				SysUtil.getInstance(DpAcctSvcType.class).acctStatusUpd(cplClsAcctIn);
			}else{
				property.setCuacst(E_CUACST.PRECLOS);
				//修改客户信息表记录为关户
				ActoacDao.updKnaCustStat(E_ACCTST.SETTLE,E_CUACST.CLOSED, CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), property.getCustac(),timetm,CommTools.getBaseRunEnvs().getChannel_id());
				
				//预销户需要计提，此处不修改结算户状态
				
				//ActoacDao.updKnaAcctStat(E_DPACST.CLOSE, CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), property.getAcctno());
			}
			
			//修改客户化状态
			IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
			cplDimeInfo.setCustac(property.getCustac());
			if(CommUtil.compare(property.getSettbl(), BigDecimal.ZERO) == 0){
				cplDimeInfo.setDime01(E_YES___.YES.getValue()); //零余额销户，设置客户化状态为销户 
			}else{
				cplDimeInfo.setDime01(E_YES___.NO.getValue()); //非零余额销户，传入收款任账户类型更新客户状态
				cplDimeInfo.setDime02(input.getTractp().getValue()); //收款人账户类型
			}
			
			SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);
		}
		
		
		output.setPyinsq(cplKnbClac.getClossq());
		output.setPyindt(cplKnbClac.getClosdt());
		output.setSettbl(cplKnbClac.getClosam());
		if(property.getCuacst() == E_CUACST.PRECLOS){
			output.setClosst(E_CLOSST.FORECLAC);
		}else{
			output.setClosst(E_CLOSST.CLOSEAC);
		}
		
		
		log.debug("<<=====统一后管销户，账号:[%s]处理结束", input.getCardno());
	}
	/**
	 * @Title: chkRepack 
	 * @Description:检查是否有未领取红包  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年11月25日 上午10:27:34 
	 * @version V2.3.0
	 */
	public static void chkRepack( final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Output output){
		
		CapitalTransDeal.chkRedpack(input.getCardno(), property.getCustid());
		
		
	}
	

	/**
	 * 
	 * @Title: sendCloseInfoMsg
	 * @Description: (销户信息通知)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年7月26日 下午10:38:21
	 * @version V2.3.0
	 */
	public static void sendCloseInfoMsg(
			final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Input input,
			final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Property property,
			final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Output output) {

		// 查询销户登记簿处理状态为成功的记录
		//IoCaKnbClac cplKnbClac = DpAcctDao.selKnbClac(property.getCustac(),E_CLSTAT.SUCC, false);

		if (property.getCuacst() == E_CUACST.CLOSED) {
			
//			E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介
			
/*
			KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("CLOSAC", "CUSTSM",
					"%", "%", true);
			
			String bdid = tblKnaPara.getParm_value1();// 服务绑定ID
			
			IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind(
					IoCaOtherService.class, bdid);*/
			
			// 1.销户成功发送销户结果到客户信息
			/*String mssdid = CommTools.getMySysId();// 消息ID
			String mesdna = tblKnaPara.getParm_value2();// 媒介名称

			IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter closeSendMsgInput = SysUtil.getInstance(IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter.class);*/
			
			//closeSendMsgInput.setMsgid(mssdid); // 发送消息ID
//			closeSendMsgInput.setMedium(mssdtp); // 消息媒介
			//closeSendMsgInput.setMdname(mesdna); // 媒介名称

			//修改销户cmq通知  modify lull
//			MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//			mri.setMtopic("Q0101004");
//			IoCaCloseAcctSendMsg closeSendMsgInput = SysUtil.getInstance(IoCaCloseAcctSendMsg.class);
//			closeSendMsgInput.setCustid(property.getCustid()); // 用户ID
//			closeSendMsgInput.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());// 操作机构
//			closeSendMsgInput.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());// 操作柜员
//			closeSendMsgInput.setClossv(CommTools.getBaseRunEnvs().getChannel_id());// 销户渠道
//			if (input.getDrawwy() == E_CLSDTP.MGD) {
//				closeSendMsgInput.setClosfg(E_YES___.YES);// 是否挂失销户标志
//			} else {
//				closeSendMsgInput.setClosfg(E_YES___.NO);// 是否挂失销户标志
//			}
//
//			mri.setMsgtyp("ApSmsType.IoCaCloseAcctSendMsg");
//			mri.setMsgobj(closeSendMsgInput); 
//			AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
			//caOtherService.closeAcctSendMsg(closeSendMsgInput);
			
			/*// 2.销户成功发送销户结果到合约库
			KnpParameter tblKnaPara1 = KnpParameterDao.selectOne_odb1("CLOSAC", "AGRTSM",
					"%", "%", true);

			String mssdid1 = CommTools.getMySysId();// 消息ID

			String mesdna1 = tblKnaPara1.getParm_value2();// 媒介名称

			IoCaOtherService.IoCaClAcSendContractMsg.InputSetter closeSendAgrtInput = CommTools
					.getInstance(IoCaOtherService.IoCaClAcSendContractMsg.InputSetter.class);
	
			closeSendAgrtInput.setMsgid(mssdid1); // 发送消息ID
			closeSendAgrtInput.setMdname(mesdna1); // 媒介名称
			closeSendAgrtInput.setUserId(property.getCustid()); // 用户ID
			closeSendAgrtInput.setAcctType(property.getAccatp());// 账户分类
			closeSendAgrtInput.setOrgId(property.getBrchno());// 归属机构
			closeSendAgrtInput.setAcctNo(input.getCardno());// 电子账号
			closeSendAgrtInput.setAcctStat(E_CUACST.CLOSED);// 客户化状态
			closeSendAgrtInput.setAcctName(input.getCustna());// 户名
			closeSendAgrtInput.setCertNo(input.getIdtfno());// 证件号码
			closeSendAgrtInput.setCertType(input.getIdtftp());// 证件类型
			closeSendAgrtInput.setTransTime(DateTools2.getCurrentTimestamp());//操作时间

			caOtherService.clAcSendContractMsg(closeSendAgrtInput);*/
			
		}
	  }

	/**
	 * 涉案账号交易信息登记 
	 * @Title: prcyInacRegister 
	 * @Description: 涉案账号交易信息登记 
	 * @param input
	 * @param property
	 * @param output
	 * @author liaojincai
	 * @date 2016年8月2日 上午11:02:33 
	 * @version V2.3.0
	 */
	public static void prcyInacRegister( final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Output output){
		
		E_INSPFG invofg = property.getInvofg();// 转出账号是否涉案
		E_INSPFG invofg1 = property.getInvofg1();// 转入账号是否涉案

		// 涉案账户交易信息登记
		if (E_INSPFG.INVO == invofg || E_INSPFG.INVO == invofg1) {

			// 独立事务
			DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
				@Override
				public Void execute() {

					// 获取涉案账户交易信息登记输入信息
					IoCaKnbTrinInput cplKnbTrin = SysUtil.getInstance(IoCaKnbTrinInput.class);
					cplKnbTrin.setTrantp(cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRANTP.TRANSFER);// 交易类型
					cplKnbTrin.setOtcard(input.getCardno());// 转出账号
					cplKnbTrin.setOtacna(input.getCustna());// 转出账号名称
					cplKnbTrin.setIncard(input.getTracno());// 转入账号
					cplKnbTrin.setInacna(input.getTracna());// 转入账户名称
					cplKnbTrin.setTranam(input.getClosam());// 交易金额
					cplKnbTrin.setIssucc(E_YES___.NO);// 交易是否成功

					// 涉案账户交易信息登记
					SysUtil.getInstance(ServEacctSvcType.class)
							.ioCaKnbTrinRegister(cplKnbTrin);

					return null;
				}
			});

			// 转出账号涉案
			if (E_INSPFG.INVO == invofg) {
				throw DpModuleError.DpstAcct.BNAS0770();
			}

			// 转入账号涉案
			if (E_INSPFG.INVO == invofg1) {
				throw DpModuleError.DpstAcct.BNAS0321();
			}

		}

	}
	
	
	private static BigDecimal chkParam(final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Input input){
		
		//非空检查
		
		
		if(CommUtil.isNull(input.getClostp())){
			throw DpModuleError.DpstAcct.BNAS0291();
		}
		
		if(CommUtil.isNull(input.getDrawwy())){
			throw DpModuleError.DpstAcct.BNAS1936();
		}
		
		if(input.getClostp() == E_ACCLTP.INHERIT && input.getDrawwy() != E_CLSDTP.NO){
			throw DpModuleError.DpstAcct.BNAS0283();
		}
		
		if(input.getClostp() == E_ACCLTP.AGENT && input.getDrawwy() != E_CLSDTP.MGD){
			throw DpModuleError.DpstAcct.BNAS3003();
		}
		
		if(CommUtil.isNull(input.getCardno())){
			throw DpModuleError.DpstComm.BNAS0955();
		}
		
		if(CommUtil.isNull(input.getCustna())){
			throw DpModuleError.DpstComm.BNAS0534();
		}
		
		if(CommUtil.isNull(input.getClosam())){
			throw DpModuleError.DpstAcct.BNAS0289();
		}
		
		if(CommUtil.compare(input.getClosam(), BigDecimal.ZERO) > 0){
			throw DpModuleError.DpstComm.E9999("暂不支持有金额销户");
		}
		
		if(CommUtil.isNull(input.getClreas())){
			throw DpModuleError.DpstAcct.BNAS0284();
		}
		
		if(CommUtil.isNull(input.getDocmna())){
			throw DpModuleError.DpstAcct.BNAS0139();
		}
		
		if(CommUtil.isNull(input.getDocmno())){
			throw DpModuleError.DpstAcct.BNAS0140();
		}
		
		if(CommUtil.isNull(input.getIdtfno())){
			throw DpModuleError.DpstAcct.BNAS1037();
		}
		
		if(CommUtil.isNull(input.getIdtftp())){
			throw DpModuleError.DpstAcct.BNAS1036();
		}
		
		if(CommUtil.isNull(input.getDealna())){
			throw DpModuleError.DpstAcct.BNAS1266();
		}
		
		if(CommUtil.isNull(input.getDlidtp())){
			throw DpModuleError.DpstAcct.BNAS1937();
		}
		
		if(CommUtil.isNull(input.getDlidno())){
			throw DpModuleError.DpstAcct.BNAS1938();
		}
		
		if(CommUtil.isNull(input.getIdtftp())){ //存款人证件类型
			throw DpModuleError.DpstAcct.BNAS3007();
		}
		
		if(CommUtil.isNull(input.getIdtfno())){ //存款人证件号码
			throw DpModuleError.DpstAcct.BNAS3008();
		}
			
		if(CommUtil.isNull(input.getChaglg())){
			throw DpModuleError.DpstAcct.BNAS0341();
		}
		
		if(CommUtil.isNull(input.getSmrycd())){
			throw DpModuleError.DpstComm.BNAS0195();
		}
		
		
		//柜面销户 收款人账户类型不能为“他行卡” 
		if(input.getTractp() == E_CLSATP.OTHER){
			throw DpModuleError.DpstComm.BNAS0685();
		}
		
		// 当转入客户账时，转入户名必须和存款人姓名或经办人姓名一致
		if (input.getTractp() != E_CLSATP.INACCT) {
			if(CommUtil.isNotNull(input.getTracna())){
				if (!CommUtil.equals(input.getTracna(), input.getCustna()) && !CommUtil.equals(input.getTracna(), input.getDealna())) {
					throw DpModuleError.DpstComm.BNAS0026();
				}
			}
		}
	
		
		BigDecimal totPaidam = BigDecimal.ZERO;
		if(input.getChaglg() == E_YES___.YES){
			if(CommUtil.isNull(input.getChagwy())){
				throw DpModuleError.DpstAcct.BNAS0339();
			}
			
//			if(CommUtil.isNull(input.getTlcgam())){
//				throw DpModuleError.DpstAcct.E9999("收费总金额不能为空");
//			}
//			
//			if(CommUtil.compare(input.getTlcgam(), BigDecimal.ZERO) <= 0){
//				throw DpModuleError.DpstAcct.E9999("收费总金额不能小于或等于0");
//			}
			
			//检查销户金额是否小于收费总额
			if(CommUtil.isNotNull(input.getTlcgam()) && CommUtil.compare(input.getClosam(), input.getTlcgam()) < 0){
				throw DpModuleError.DpstComm.BNAS0334();
			}
			
			// 收费交易金额检查
			if(input.getChrgpm().size() <= 0){
				throw DpModuleError.DpstComm.BNAS0395();
			}
			
			for (IoCgCalCenterReturn IoCgCalCenterReturn : input.getChrgpm()) {
				BigDecimal tranam = IoCgCalCenterReturn.getTranam();// 交易金额
				BigDecimal clcham = IoCgCalCenterReturn.getClcham();// 应收费用金额（未优惠）
				BigDecimal dircam = IoCgCalCenterReturn.getDircam();// 优惠后应收金额
				BigDecimal paidam = IoCgCalCenterReturn.getPaidam();// 实收金额
				
				if (CommUtil.isNotNull(tranam)) {
					if(CommUtil.compare(tranam, BigDecimal.ZERO) < 0){ //交易金额金额
						throw DpModuleError.DpstComm.BNAS0621();
					}
					if(CommUtil.compare(tranam, input.getClosam()) != 0){ //交易金额金额
						throw DpModuleError.DpstComm.BNAS0336();
					}
				}
				if (CommUtil.isNotNull(clcham)) {
					if(CommUtil.compare(clcham, BigDecimal.ZERO) < 0){ //交易金额金额
						throw DpModuleError.DpstComm.BNAS0244();
					}
				}
				if (CommUtil.isNotNull(dircam)) {
					if(CommUtil.compare(dircam, BigDecimal.ZERO) < 0){ //交易金额金额
						throw DpModuleError.DpstComm.BNAS0237();
					}
				}
				if (CommUtil.isNotNull(paidam)) {
					if(CommUtil.compare(paidam, BigDecimal.ZERO) < 0){ //交易金额金额
						throw DpModuleError.DpstComm.BNAS0355();
					}
				}
				
				
				totPaidam = totPaidam.add(paidam);
			}
			
			if(CommUtil.isNotNull(input.getTlcgam()) && !CommUtil.equals(totPaidam, input.getTlcgam())){
				throw DpModuleError.DpstComm.BNAS0243();
			}
		}
		
		return totPaidam;
		
	}
	/**
	 * @Title: chkNotChrgFee 
	 * @Description: 是否有未收讫费用检查  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年12月22日 下午2:02:04 
	 * @version V2.3.0
	 */
	public static void chkNotChrgFee( final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clacbt.Output output){
		PbEnumType.E_YES___ isfee = SysUtil.getInstance(IoCgChrgSvcType.class).CgChageRgstNotChargeByCustac(property.getCustac());
		if(isfee == PbEnumType.E_YES___.YES){
			throw DpModuleError.DpstAcct.BNAS0854();
		}
	}

	

	

	
}
