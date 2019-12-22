package cn.sunline.ltts.busi.dptran.trans;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapot;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevGenBindCard;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCacd;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbClac;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.CupsTranfe;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AFEETG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSATP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSTAT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CNBSTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOTYPE;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SUBSYS;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;

public class cnapot {
	
	private static BizLog log = BizLogUtil.getBizLog(cnapot.class);

	/***
	 * 转账前处理
	 * */
	public static void dotransbefore( final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Output output){
		
		log.debug("--------------进入大小额往来账服务-----------");
		
		BigDecimal tlcgam = chkParam(input); //输入参数校验
		String timetm =DateTools2.getCurrentTimestamp();
		
		
		//交易前检查原流水是否存在
		KnlCnapot iobl = DpAcctQryDao.selknlcnapotChk(input.getMsetdt(), input.getMsetsq(), input.getPyercd(), input.getIotype(), input.getCrdbtg(), E_TRANST.NORMAL, false);
		if(CommUtil.isNotNull(iobl)){
			property.setIssucc(E_YES___.YES);
			output.setHostsq(iobl.getTransq());
			output.setHostdt(iobl.getTrandt());
			output.setAcctbr(input.getBrchno());
			output.setClerdt(iobl.getClerdt());
			output.setClerod(iobl.getClenum());
			return;
		}else{
			property.setIssucc(E_YES___.NO);
		}
		
		IoCaSevGenBindCard bind = SysUtil.getInstance(IoCaSevGenBindCard.class);
		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		
		ApSysDateStru tblKappClrdat = BusiTools.getClearDateInfo();
		property.setClerdt(tblKappClrdat.getSystdt());
		property.setClenum(tblKappClrdat.getClenum());
		
		
		if(input.getIotype()==E_IOTYPE.OUT){ //往账
			
			KnaAcct tblKnaAcct  = SysUtil.getInstance(KnaAcct.class);
			
			IoCaKnaAcdc otacdc = ActoacDao.selKnaAcdc(input.getPyerac(), false);
			if(CommUtil.isNull(otacdc)){
				throw CaError.Eacct.BNAS0750();
			}
			if(otacdc.getStatus() == E_DPACST.CLOSE){
				throw CaError.Eacct.BNAS0857();
			}
			E_ACCATP accatp = cagen.qryAccatpByCustac(otacdc.getCustac()); 
			E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(otacdc.getCustac());
			//走销户转账流程
			if(cuacst == E_CUACST.PRECLOS){ //预销户
				property.setIsclos(E_YES___.YES); //设置销户标志
				IoCaKnbClac cplKnbClac = DpAcctDao.selKnbClacBySignleStat(otacdc.getCustac(), E_CLSTAT.DEAL, false);
				if(CommUtil.isNull(cplKnbClac)){
					throw DpModuleError.DpstComm.BNAS0214();
				}
				//修改销户登记簿状态
				DpAcctDao.updKnbClacStatBySeq(E_CLSTAT.TRSC, cplKnbClac.getClossq(),timetm);
				
				
				tblKnaAcct = CapitalTransDeal.getSettKnaAcctAc(otacdc.getCustac());
				if(!CommUtil.equals(input.getTranam(), tblKnaAcct.getOnlnbl())){
					throw DpModuleError.DpstComm.BNAS0594();
				}
				if(CommUtil.compare(tblKnaAcct.getOnlnbl(), tlcgam) < 0){
					throw DpModuleError.DpstComm.BNAS0334();
				}
				
			}else{
				property.setIsclos(E_YES___.NO); //设置销户标志
				String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//				CommTools.getBaseRunEnvs().setBusi_org_id(otacdc.getCorpno());
				
				//1.转出方电子账户状态校验
				CapitalTransCheck.ChkAcctstOT(otacdc.getCustac()); 
				//2.转出方状态字检查
				CapitalTransCheck.ChkAcctFrozOT(otacdc.getCustac());
				//类型检查
				if(accatp == E_ACCATP.FINANCE){ //二类只能绑定卡转出
					IoCaKnaCacd cacd = bind.selBindByCard(otacdc.getCustac(), input.getPyeeac(), E_DPACST.NORMAL, false);
					if(CommUtil.isNull(cacd)){
						throw DpModuleError.DpstComm.BNAS1366();
					}
				}else if(accatp == E_ACCATP.WALLET){
					throw DpModuleError.DpstComm.BNAS1367();
				}
				
				tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(otacdc.getCustac(), E_ACSETP.SA);
				
				//可用余额检查
				//BigDecimal usebal = DpAcctProc.getProductBal(otacdc.getCustac(), input.getCrcycd(), false);
			
				// 可用余额 addby xiongzhao 20161223 
				BigDecimal usebal = SysUtil.getInstance(DpAcctSvcType.class)
						.getAcctaAvaBal(otacdc.getCustac(), tblKnaAcct.getAcctno(),
								input.getCrcycd(), E_YES___.YES, E_YES___.NO);
				
				if(CommUtil.compare(usebal, input.getTranam()) < 0){
					throw DpModuleError.DpstComm.BNAS0442();
				}
				if(CommUtil.compare(usebal, tlcgam) < 0){
					throw DpModuleError.DpstComm.BNAS0334();
				}
				
//				CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
				
			}
			
			property.setOtcsac(otacdc.getCustac()); //电子账号ID
			property.setOtchld(tblKnaAcct.getAcctno()); //子账号
			property.setAcctbr(tblKnaAcct.getBrchno());
			property.setAccatp(accatp);
			property.setCuacst(cuacst);
			property.setTblKnaAcct(tblKnaAcct);
		}
		
		if(input.getIotype()==E_IOTYPE.IN){//来账
			
			
			IoCaKnaAcdc inacdc = ActoacDao.selKnaAcdc(input.getPyeeac(), true);
			
			String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//			CommTools.getBaseRunEnvs().setBusi_org_id(inacdc.getCorpno());
			E_CUACST cuacst = null;
			if(CommUtil.equals(input.getBusitp(), E_CNBSTP.A105.getValue())){ //退汇 不检查预销户
				cuacst = CapitalTransCheck.ChkCnapotAcctstIN(inacdc.getCustac());
			}else{
				cuacst = CapitalTransCheck.ChkAcctstIN(inacdc.getCustac()); //1.转出方电子账户状态校验
			}
			CapitalTransCheck.ChkAcctFrozIN(inacdc.getCustac());
			
			KnaAcct tblKnaAcct = CapitalTransDeal.getSettKnaAcctAc(inacdc.getCustac());
			
			//add 20161206 songlw 增加三类户同名检验
			E_ACCATP accatp = cagen.qryAccatpByCustac(inacdc.getCustac()); 
			if(E_ACCATP.WALLET == accatp && CommUtil.isNotNull(input.getPyerna())){
				if(!CommUtil.equals(tblKnaAcct.getAcctna(), input.getPyerna())){
					throw DpModuleError.DpstComm.BNAS1368();
				}
			}
			
			//来账，且是退汇，则更新销户登记簿处理状态
			if(CommUtil.equals(input.getBusitp(), E_CNBSTP.A105.getValue())){
				IoCaKnbClac cplKnbClac = DpAcctDao.selKnbClacBySignleStat(inacdc.getCustac(), E_CLSTAT.TRSC, false);
				if(CommUtil.isNotNull(cplKnbClac)){
					CommTools.getRemoteInstance(DpAcctSvcType.class).updKnbClacStat(cplKnbClac.getClossq(), inacdc.getCustac(), E_CLSTAT.FAIL);
				}
			}
			
			property.setIncsac(tblKnaAcct.getCustac()); //电子账号ID
			property.setInchld(tblKnaAcct.getAcctno()); //子账号
			property.setAcctbr(tblKnaAcct.getBrchno());
			property.setCuacst(cuacst);
			//add 20161206 songlw 
			property.setTblKnaAcct(tblKnaAcct);
			
//			CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
		} 
	
		//4.设置属性值
		
//		property.setBusisq(CommTools.getBaseRunEnvs().getBstrsq()); // 业务跟踪编号
		property.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
		property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 业务跟踪编号
		property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 业务跟踪编号
		property.setLinkno(""); // 连笔号
		property.setPrcscd("cnapot"); // 交易码
		
		
		if(input.getAfeetg() != E_AFEETG.T0){ //收费标志
			//property.setQuotfs(E_YES___.YES);
			property.setChgflg(E_CHGFLG.ALL); //设置记账标志
		}


		if (input.getIotype()==E_IOTYPE.OUT) { // 转出方是电子账户 往账
//			property.setAuacfg(E_YES___.NO); // 存取标志
			property.setIoflag("0"); // 出入金标志
			property.setTrantp(E_TRANTP.TR); // 交易类型
			property.setDscrtx(E_CAPITP.OT206.getLongName()); // 描述
		} else if (input.getIotype()==E_IOTYPE.IN){ // 转入方是电子账户  来账
//			property.setAuacfg(E_YES___.YES); // 存取标志
			property.setIoflag("1"); // 出入金标志
			property.setTrantp(E_TRANTP.TR); // 交易类型
			property.setDscrtx(E_CAPITP.IN107.getLongName()); // 描述
		}
        String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();
		String acbrch = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(brchno).getBrchno();
		
		E_CLACTP clactp=null;
		
		if(CommUtil.equals(input.getSubsys(),E_SUBSYS.LM.getValue())){//小额
			clactp=E_CLACTP._06;
		}else if(CommUtil.equals(input.getSubsys(),E_SUBSYS.BG.getValue())){//大额
			clactp=E_CLACTP._05;
		}else {
			throw DpModuleError.DpstComm.BNAS1369();
		}
	
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", clactp.getValue(), "%", true);		
		property.setBusino(para.getParm_value1()); // 业务编码IA
		property.setSubsac(para.getParm_value2());//子户号
		property.setAcbrch(acbrch); // 账户机构
	}

	public static void dotransAfter( final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Output output){
		
		if(property.getIssucc() == E_YES___.NO){
			//平衡性检查
			E_CLACTP clactp=null;
			if(CommUtil.equals(input.getSubsys(),E_SUBSYS.LM.getValue())){//小额
				clactp=E_CLACTP._06;
			}else if(CommUtil.equals(input.getSubsys(),E_SUBSYS.BG.getValue())){//大额
				clactp=E_CLACTP._05;
			}else {
				throw DpModuleError.DpstComm.BNAS1369();
			}
			
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),clactp);
			
			output.setHostsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			output.setHostdt(CommTools.getBaseRunEnvs().getTrxn_date());
			output.setAcctbr(input.getBrchno());
			output.setClerdt(property.getClerdt());
			output.setClerod(property.getClenum());
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
	 * @date 2016年8月2日 上午9:16:00 
	 * @version V2.3.0
	 */
	public static void prcyInacRegister( final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Output output){
		
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
					cplKnbTrin.setOtcard(input.getPyerac());// 转出账号
					cplKnbTrin.setOtacna(input.getPyerna());// 转出账号名称
					cplKnbTrin.setOtbank(input.getPyercd());// 转出银行行号
					cplKnbTrin.setIncard(input.getPyeeac());// 转入账号
					cplKnbTrin.setInacna(input.getPyeena());// 转入账户名称
					cplKnbTrin.setInbank(input.getPyeecd());// 转入银行行号
					cplKnbTrin.setTranam(input.getTranam());// 交易金额
					cplKnbTrin.setCrcycd(input.getCrcycd());// 币种
					cplKnbTrin.setIssucc(E_YES___.NO);// 交易是否成功

					// 涉案账户交易信息登记
					SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(cplKnbTrin);

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

	public static void changeAcctStuts(
			final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Output output) {
		// 修改账户状态，休眠转正常结息

		CapitalTransDeal.dealAcctStatAndSett(property.getCuacst(), property.getTblKnaAcct());

	}

	/**
	 * @Title: prcCnapotVN 
	 * @Description:电子汇划往账记账  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年11月3日 下午2:41:45 
	 * @version V2.3.0
	 */
	public static void prcCnapotVN( final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Output output){
		CupsTranfe cplCnapot = SysUtil.getInstance(CupsTranfe.class);
		
		if(input.getIotype() == E_IOTYPE.IN){ //来账
			cplCnapot.setCrcycd(input.getCrcycd());
			cplCnapot.setSmrycd(input.getSmrycd());
			cplCnapot.setInbrch(property.getAcctbr());
			cplCnapot.setTranam(input.getTranam());
			cplCnapot.setInacct(input.getPyerac());
			cplCnapot.setInacna(input.getPyerna());
			cplCnapot.setOtacct(input.getPyeeac());
			cplCnapot.setOtacna(input.getPyeena());
		}else if(input.getIotype() == E_IOTYPE.OUT){
			cplCnapot.setCrcycd(input.getCrcycd());
			cplCnapot.setSmrycd(input.getSmrycd());
			cplCnapot.setInbrch(property.getAcctbr());
			cplCnapot.setTranam(input.getTranam());
			cplCnapot.setInacct(input.getPyeeac());
			cplCnapot.setInacna(input.getPyeena());
			cplCnapot.setOtacct(input.getPyerac());
			cplCnapot.setOtacna(input.getPyerna());
		}
		
		CapitalTransDeal.dealCnapotVN(cplCnapot, input.getIotype());
	}
	/**
	 * @Title: prcCLSAccount 
	 * @Description: 销户处理  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年11月11日 下午3:42:30 
	 * @version V2.3.0
	 */
	public static void prcCLSAccount( final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Output output){
		
		KnaAcct tblKnaAcct = property.getTblKnaAcct();
		//IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_accsByCustno(tblKnaAcct.getCustno(), true, E_STATUS.NORMAL);
		property.setCustid(tblKnaAcct.getCustno());
		
//		ClsAcctIn cplClsAcctIn = SysUtil.getInstance(ClsAcctIn.class);
//		
//		cplClsAcctIn.setCardno(input.getPyerac());
//		cplClsAcctIn.setCustac(tblKnaAcct.getCustac());
//		cplClsAcctIn.setCustna(tblKnaAcct.getAcctna());
//		cplClsAcctIn.setCustno(tblKnaAcct.getCustno());
//		//注销电子账户
//		CommTools.getRemoteInstance(DpAcctSvcType.class).acctStatusUpd(cplClsAcctIn);
		
		IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
		cplDimeInfo.setCustac(tblKnaAcct.getCustac());
		cplDimeInfo.setDime01(E_CLSATP.OTHER.getValue()); //收款人账户类型
		CommTools.getRemoteInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);
		
		
		
	}
	/**
	 * @Title: chkParam 
	 * @Description:参数检查  
	 * @param input
	 * @author zhangan
	 * @date 2016年11月11日 下午2:04:52 
	 * @version V2.3.0
	 */
	public static BigDecimal chkParam(final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Input input){
		
		
		//BigDecimal afeeam = CommUtil.nvl(input.getAfeeam(), BigDecimal.ZERO); //手续费
		//BigDecimal feeam1 = CommUtil.nvl(input.getFeeam1(), BigDecimal.ZERO); //汇划费
		BigDecimal tlcgam = BigDecimal.ZERO;
		
		if(CommUtil.isNull(input.getCrdbtg())){
			throw DpModuleError.DpstComm.BNAS1370();
		}
		if(CommUtil.isNull(input.getPriotp())){
			throw DpModuleError.DpstComm.BNAS1371();
		}
		if(CommUtil.isNull(input.getAfeetg())){
			throw DpModuleError.DpstAcct.BNAS0341();
		}
		
		if(CommUtil.isNull(input.getSmrycd())){
			throw DpModuleError.DpstComm.BNAS0195();
		}
		if (CommUtil.isNull(input.getSubsys())) {
			throw DpModuleError.DpstComm.BNAS1372();
		}
		if (CommUtil.isNull(input.getMsetdt())) {
			throw DpModuleError.DpstComm.BNAS1373();
		}
		if (CommUtil.isNull(input.getMsetsq())) {
			throw DpModuleError.DpstComm.BNAS1374();
		}
		if (CommUtil.isNull(input.getMesgtp())) {
			throw DpModuleError.DpstComm.BNAS1375();
		}
		if (CommUtil.isNull(input.getIotype())) {
			throw DpModuleError.DpstComm.BNAS1376();
		}
		if (CommUtil.isNull(input.getCrcycd())) {
			throw DpModuleError.DpstComm.BNAS1101();
		}
		if (CommUtil.isNull(input.getCstrfg())) {
			throw DpModuleError.DpstComm.BNAS1377();
		}
		if (CommUtil.isNull(input.getCsextg())) {
			throw DpModuleError.DpstComm.BNAS1378();
		}
		if (CommUtil.isNull(input.getPyercd())) {
			throw DpModuleError.DpstComm.BNAS1379();
		}
		if (CommUtil.isNull(input.getPyeecd())) {
			throw DpModuleError.DpstComm.BNAS1380();
		}
		if (CommUtil.isNull(input.getChfcnb())) {
			throw DpModuleError.DpstComm.BNAS1362();
		}
		if (CommUtil.isNull(input.getFrondt())) {
			throw DpModuleError.DpstComm.BNAS1381();
		}
		if (CommUtil.isNull(input.getFronsq())) {
			throw DpModuleError.DpstComm.BNAS1382();
		}
		if (CommUtil.isNull(input.getBrchno())) {
			throw DpModuleError.DpstComm.BNAS1383();
		}
		if (CommUtil.isNull(input.getUserid())) {
			throw DpModuleError.DpstComm.BNAS1384();
		}
		if (CommUtil.isNull(input.getTranam())) {
			throw DpModuleError.DpstAcct.BNAS0623();
		}

		
		if(input.getIotype()==E_IOTYPE.IN){//来账
			if(CommUtil.isNull(input.getPyerac())){ 
				throw DpModuleError.DpstComm.BNAS1385();
			} 
			if(CommUtil.isNull(input.getPyerna())){ 
				throw DpModuleError.DpstComm.BNAS1386();
			} 
			if(CommUtil.isNull(input.getKeepdt())){
				throw DpModuleError.DpstComm.BNAS0399();
			}
		}else if(input.getIotype()==E_IOTYPE.OUT){//往账
			
			if(CommUtil.isNull(input.getPyeeac())){ 
				throw DpModuleError.DpstComm.BNAS0324();
			} 
			if(CommUtil.isNull(input.getPyeena())){ 
				throw DpModuleError.DpstComm.BNAS0325();
			} 
		}else{
			throw DpModuleError.DpstComm.BNAS1387();
		}
		
		if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0){
			throw DpModuleError.DpstComm.BNAS0394();
		}
		
		
		if(input.getAfeetg() != E_AFEETG.T0){ //收费标志
			
			
			//BigDecimal tmpAm = BigDecimal.ZERO;
			// 收费交易金额检查
			if(input.getChrgpm().size() <= 0){
				throw DpModuleError.DpstComm.BNAS0395();
			}
			for (IoCgCalCenterReturn IoCgCalCenterReturn : input.getChrgpm()) {
				BigDecimal clcham = CommUtil.nvl(IoCgCalCenterReturn.getClcham(),BigDecimal.ZERO);// 应收费用金额（未优惠）
				BigDecimal dircam = CommUtil.nvl(IoCgCalCenterReturn.getDircam(),BigDecimal.ZERO);// 优惠后应收金额
				BigDecimal paidam = CommUtil.nvl(IoCgCalCenterReturn.getPaidam(),BigDecimal.ZERO);// 实收金额
				
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
				
				tlcgam = tlcgam.add(paidam);
			} //end for
			
			
//			if(!CommUtil.equals(tmpAm, tlcgam)){
//				throw DpModuleError.DpstComm.E9999("实收费用金额与收费总金额不一致");
//			}
			
		}
		
		return tlcgam;
	}

	/**
	 * @Title: sendCloseInfoMsg 
	 * @Description:   发短信和消息
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年11月11日 下午3:50:05 
	 * @version V2.3.0
	 */
	public static void sendCloseInfoMsg( final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Cnapot.Output output){
		// 查询销户登记簿处理状态为成功的记录
		if(property.getIsclos() == E_YES___.YES){
//			E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介
			//IoCucifCust cplCifCust = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_selectOne_odb1(property.getTblKnaAcct().getCustno(), true);
//			IoSrvCfPerson.IoGetCifCust.InputSetter queryCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//			queryCust.setCustno(property.getTblKnaAcct().getCustno());
//			IoSrvCfPerson.IoGetCifCust.Output cplCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//			SysUtil.getInstance(IoSrvCfPerson.class).getCifCust(queryCust, cplCifCust);
			
			//修改销户cmq通知  modify lull
//			MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//			mri.setMtopic("Q0101004");
//			IoCaCloseAcctSendMsg closeSendMsgInput = SysUtil.getInstance(IoCaCloseAcctSendMsg.class);
//			closeSendMsgInput.setCustid(property.getCustid()); // 用户ID
//			closeSendMsgInput.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());// 操作机构
//			closeSendMsgInput.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());// 操作柜员
//			closeSendMsgInput.setClossv(CommTools.getBaseRunEnvs().getChannel_id());// 销户渠道
//			closeSendMsgInput.setClosfg(E_YES___.NO);// 是否挂失销户标志
//
//			mri.setMsgtyp("ApSmsType.IoCaCloseAcctSendMsg");
//			mri.setMsgobj(closeSendMsgInput); 
//			AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
			/*KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("CLOSAC", "CUSTSM","%", "%", true);
			
			String bdid = tblKnaPara.getParm_value1();// 服务绑定ID
			
			IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind(
					IoCaOtherService.class, bdid);
			
			// 1.销户成功发送销户结果到客户信息
			String mssdid = CommTools.getMySysId();// 消息ID
			String mesdna = tblKnaPara.getParm_value2();// 媒介名称
			
			IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter closeSendMsgInput = SysUtil.getInstance(IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter.class);
			closeSendMsgInput.setMsgid(mssdid); // 发送消息ID
//			closeSendMsgInput.setMedium(mssdtp); // 消息媒介
			closeSendMsgInput.setMdname(mesdna); // 媒介名称
			closeSendMsgInput.setCustid(property.getCustid()); // 用户ID
			closeSendMsgInput.setClosfg(E_YES___.NO);// 是否挂失销户标志（大小额转账只会用于移动前端销户，而移动前端销户不会有挂失销户）
			
			caOtherService.closeAcctSendMsg(closeSendMsgInput);
			
			// 2.销户成功发送销户结果到合约库
			KnpParameter tblKnaPara1 = KnpParameterDao.selectOne_odb1("CLOSAC", "AGRTSM",
					"%", "%", true);
			
			String mssdid1 = CommTools.getMySysId();// 消息ID
			
			String mesdna1 = tblKnaPara1.getParm_value2();// 媒介名称
			
			IoCaOtherService.IoCaClAcSendContractMsg.InputSetter closeSendAgrtInput = SysUtil.getInstance(IoCaOtherService.IoCaClAcSendContractMsg.InputSetter.class);
			
			closeSendAgrtInput.setMsgid(mssdid1); // 发送消息ID
//			closeSendAgrtInput.setMedium(mssdtp); // 消息媒介
			closeSendAgrtInput.setMdname(mesdna1); // 媒介名称
			closeSendAgrtInput.setUserId(property.getCustid()); // 用户ID
			closeSendAgrtInput.setAcctType(property.getAccatp());// 账户分类
			closeSendAgrtInput.setOrgId(property.getTblKnaAcct().getBrchno());// 归属机构
			closeSendAgrtInput.setAcctNo(input.getPyerac());// 电子账号
			closeSendAgrtInput.setAcctStat(E_CUACST.CLOSED);// 客户化状态
			closeSendAgrtInput.setAcctName(property.getTblKnaAcct().getAcctna());// 户名
			closeSendAgrtInput.setCertNo(cplCifCust.getIdtfno());// 证件号码
			closeSendAgrtInput.setCertType(cplCifCust.getIdtftp());// 证件类型
			closeSendAgrtInput.setTransTime(DateTools2.getCurrentTimestamp());//操作时间
			
			caOtherService.clAcSendContractMsg(closeSendAgrtInput);*/
		}
	}

}