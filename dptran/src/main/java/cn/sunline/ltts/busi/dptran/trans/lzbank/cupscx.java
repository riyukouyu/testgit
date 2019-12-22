package cn.sunline.ltts.busi.dptran.trans.lzbank;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_RETYPE;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.ChkPwdIN;
import cn.sunline.ltts.busi.dp.type.DpAcctType.ChkQtIN;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbSmsSvcType;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccountClearInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkOT;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubSqrd;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PREPTY;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUPSST;



public class cupscx {
	/**
	 * 银联CUPS消费撤销以及退货
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void dealCupscx( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Cupscx.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Cupscx.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Cupscx.Output output){
		String prepsq = input.getPrepsq();//前置流水        
		String cardno = input.getCardno();//交易卡号        
		String devcno = input.getDevcno();//设备号          
		E_AMNTCD amntcd = input.getAmntcd();//借贷标志        
		BigDecimal tranam = input.getTranam();//交易金额        
		String prepdt = input.getPrepdt();//前置日期        
		String crcycd = input.getCrcycd();//币种            
		String cnkpdt = input.getCnkpdt();//核心清算日期    
		String unkpdt = input.getUnkpdt();//银联清算日期    
		String otacct = input.getOtacct();//转出账号        
		String otacna = input.getOtacna();//转出户名   
		String otbrch = input.getOtbrch();//转出机构  
		String inacct = input.getInacct();//转入账号   
		String inacna = input.getInacna();//转入户名
		String inbrch = input.getInbrch();//转入机构        
		String prdate = input.getPrdate();//前置日期        
		String prbrmk = input.getPrbrmk();//代理机构标识号  
		String trbrmk = input.getTrbrmk();//发送机构标识号  
		String trbrch = input.getTrbrch();//交易机构        
		String trcode = input.getTrcode();//交易码          
		String stand1 = input.getStand1();//32域            
		String stand2 = input.getStand2();//33域            
		String uniseq = input.getUniseq();//银联流水        
		String retrdt = input.getRetrdt();//原日期时间      
		String resssq = input.getResssq();//原系统跟踪号    
		String reprsq = input.getReprsq();//原前置流水号    
		String servsq = input.getServsq();//全渠道流水号    
		String chckno = input.getChckno();//对账分类编号    
		BigDecimal chrgam = input.getChrgam();//手续费          
		String busino = input.getBusino();//商户代码        
		String busitp = input.getBusitp();//商户类型        
		String authno = input.getAuthno();//预授权标识码    
		String messtp = input.getMesstp();//报文类型        
		String proccd = input.getProccd();//处理码          
		String spared = input.getSpared();//备用            
		ChkQtIN chkqtn = input.getChkqtn();//额度中心参数 
		String refndt = input.getRefndt();//原支付前置日期
		String refnsq = input.getRefnsq();//原支付前置流水号
		String fronsq = input.getFronsq();//支付前置流失    
		String frondt = input.getFrondt();//支付前置日期    
		E_PREPTY prepty = input.getPrepty();//银联交易类型		
		String smrycd = BusinessConstants.SUMMARY_ZR;//摘要代码-转账
		E_SBACTP sbactp = E_SBACTP._11;//账户类别-默认为活期结算账户
		E_CAPITP capitp =  E_CAPITP.IN130;//资金交易类型-银联传统CUPS消费撤销和退货
		
		E_CLACTP clactp = E_CLACTP._35;//清算账户类型
		String tranev =  ApUtil.TRANS_EVENT_CUPSTR;//明细冲正事件
		String tranev2 =  ApUtil.TRANS_EVENT_CLER;//清算明细冲正事件

		
		/**
		 * 1,输入校验
		 */
		chkParam(input);
		
		/**
		 * 2,重复请求校验
		 */
		KnlIoblCups tblKnlIoblCups = KnlIoblCupsDao.selectOne_odb2(input.getFronsq(), input.getFrondt(), false);
		if(CommUtil.isNotNull(tblKnlIoblCups)){
			output.setMntrdt(tblKnlIoblCups.getTrandt());
			output.setMntrsq(tblKnlIoblCups.getMntrsq());
			output.setMntrtm(tblKnlIoblCups.getTmstmp());
			return;
		}
		
		/**
		 * 3,账户信息校验
		 */
		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(cardno, false);//获取卡和电子账号映射
		if(CommUtil.isNull(tblKnaAcdc)){
			throw DpModuleError.DpstComm.BNAS0750();
		}
		if(E_DPACST.CLOSE == tblKnaAcdc.getStatus()){
			throw DpModuleError.DpstComm.BNAS0441();
		}
		//获取账户状态
		E_CUACST checkcuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(tblKnaAcdc.getCustac());
		if(checkcuacst == E_CUACST.OUTAGE){
		    throw DpModuleError.DpstComm.BNAS0850();
		}
		if(checkcuacst == E_CUACST.INACTIVE){
		    throw DpModuleError.DpstComm.E9999("非活跃账户不允许入账");
		}
		
		KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
		E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
		if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
			tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
			sbactp = E_SBACTP._11;
		}else{ // III类户
			tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
			sbactp = E_SBACTP._12;
		}
		
		if(!CommUtil.equals(crcycd, tblKnaAcct.getCrcycd())){//币种校验
			throw DpModuleError.DpstComm.BNAS0632();
		}
//		if(!CommUtil.equals(inbrch, tblKnaAcct.getBrchno())){//转入机构校验
//			throw DpModuleError.DpstComm.BNAS0031();
//		}
		
		
		/**
		 * 4,资金交易前检查
		 */
		AcTranfeChkIN chkIN = SysUtil.getInstance(AcTranfeChkIN.class);
		chkIN.setAccatp(accatp);//电子账户分类 
		chkIN.setCardno(tblKnaAcdc.getCardno());//电子账户卡号 
		chkIN.setCustac(tblKnaAcdc.getCustac());//电子账号ID   
		chkIN.setCustna(tblKnaAcct.getAcctna());//电子账户户名 
		chkIN.setOpcard(otacct);//对方账户卡号  
		chkIN.setOppona(otacna);//对方户名     
		chkIN.setCapitp(capitp);//转账交易类型 
		chkIN.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道     		
		AcTranfeChkOT chkOT = CapitalTransCheck.chkTranfe(chkIN);
		
		
		/**
		 * 5,原交易状态，交易日期以及交易金额校验
		 */		
		//根据原支付前置日期和原支付前置流水号查询原交易
		KnlIoblCups tblKnlIoblCups2=KnlIoblCupsDao.selectOne_odb2(input.getFronsq(), input.getFrondt(), false);
		if (CommUtil.isNull(tblKnlIoblCups2)) {
			throw DpModuleError.DpstComm.E9999("未查询到原交易！");
		}
//		if (E_CUPSST.SUCC == tblKnlIoblCups2.getTranst() && (E_PREPTY.XFCX == prepty || E_PREPTY.DSCX == prepty)) {
//			String oldtrdt = tblKnlIoblCups2.getFrondt();//原交易支付日期
//			BigDecimal oldtram = tblKnlIoblCups2.getTranam();//原交易金额
//			if (CommUtil.equals(tranam, oldtram)) {
//			}else {
//				throw DpModuleError.DpstComm.E9999("原交易金额与撤销金额必须相等！");
//			}
//			if (CommUtil.equals(oldtrdt, frondt)) {	
//			}else {
//				throw DpModuleError.DpstComm.E9999("原交易日期与撤销日期必须相等！");
//			}
//			tblKnlIoblCups2.setTranst(E_CUPSST.CNCL);
//		}else if (E_PREPTY.XFTH == prepty && (E_CUPSST.SUCC == tblKnlIoblCups2.getTranst() || E_CUPSST.RTGD ==tblKnlIoblCups2.getTranst())) {
//			String oldtrdt = tblKnlIoblCups2.getFrondt();//原交易日期
//			BigDecimal oldtram = tblKnlIoblCups2.getTranam();//原交易金额
//			BigDecimal regoam = tblKnlIoblCups2.getRegoam();//已退货金额
//			if ((tranam.add(regoam)).compareTo(oldtram) > 0) {//比较交易金额加已退货金额与原交易金额
//				throw DpModuleError.DpstComm.E9999("交易金额加已退货金额超过了原交易金额！");
//			}
//			//更改已退货金额为交易金额加已退货金额
//			tblKnlIoblCups2.setRegoam(tranam.add(regoam));
//			//计算退货日期与原交易日期的天数差  
//			int diffDay = DateTools2.calDiffDays(DateTools2.covStringToDate(oldtrdt), DateTools2.covStringToDate(frondt));
//			//判断天数差是否在0到30天
//			if (CommUtil.Between(diffDay, 0, 30)) {
//			}else {
//				throw DpModuleError.DpstComm.E9999("退货交易日期与原交易日期间隔必须小于30天！");
//			}
//			tblKnlIoblCups2.setTranst(E_CUPSST.RTGD);
//		}else{
//			throw DpModuleError.DpstComm.E9999("银联交易类型错误或者原交易交易状态错误！");
//		}
		
		/**
		 * 6,记账
		 */
		//1,内部户借方记账
		KnpParameter para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "09", "%", true);
		if(CommUtil.isNull(para)){
			throw DpModuleError.DpstComm.E9999("业务代码未配置！");
		}
		IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
		acdrIn.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
		acdrIn.setTrantp(E_TRANTP.TR);//交易类型 
		acdrIn.setCrcycd(tblKnaAcct.getCrcycd());
		acdrIn.setSmrycd(smrycd);//摘要码-ZR
		acdrIn.setToacct(tblKnaAcdc.getCardno());
		acdrIn.setToacna(tblKnaAcct.getAcctna());
		acdrIn.setTranam(tranam);
		acdrIn.setBusino(para.getParm_value1());//业务编码
		acdrIn.setSubsac(para.getParm_value2());//子户号
		IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
		IaTransOutPro outPro = inAcctSer.ioInAcdr(acdrIn);
		
		//2,电子账户存入记账
		DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class); //存款记账服务
		SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class); //电子账户存入记账复合类型
		saveIn.setAcctno(tblKnaAcct.getAcctno()); //结算账户、钱包账户
		saveIn.setBankcd(otbrch);
//		saveIn.setBankna("");
		saveIn.setCardno(tblKnaAcdc.getCardno());
		saveIn.setCrcycd(tblKnaAcct.getCrcycd());
		saveIn.setCustac(tblKnaAcdc.getCustac());
		saveIn.setOpacna(otacna);
		saveIn.setOpbrch(otbrch);
		saveIn.setSmrycd(smrycd);
		saveIn.setToacct(otacct);
		saveIn.setTranam(tranam);
		dpSrv.addPostAcctDp(saveIn);
		
		//3,平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
		
		//4,冲正注册
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
		cplInput.setCustac(tblKnaAcdc.getCustac());
		cplInput.setEvent1(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		cplInput.setEvent2(CommTools.getBaseRunEnvs().getTrxn_date());
		cplInput.setEvent3(E_YES___.NO.getValue()); //是否是银联转入确认交易
		cplInput.setTranev(tranev);
//		ApStrike.regBook(cplInput);
		
		IoApRegBook cplInput2 = SysUtil.getInstance(IoApRegBook.class);
		cplInput2.setCustac(tblKnaAcdc.getCustac());
		cplInput2.setEvent1(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		cplInput2.setEvent2(CommTools.getBaseRunEnvs().getTrxn_date());
		cplInput2.setTranev(tranev2);
//		ApStrike.regBook(cplInput2);
		IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);    		
		apinput.setReversal_event_id(cplInput.getTranev());
		apinput.setInformation_value(SysUtil.serialize(cplInput));
		MsEvent.register(apinput, true);

		//ApStrike.regBook(cplInput2);
		IoMsRegEvent apinput2 = SysUtil.getInstance(IoMsRegEvent.class);    		
		apinput2.setReversal_event_id(cplInput2.getTranev());
		apinput2.setInformation_value(SysUtil.serialize(cplInput2));
		MsEvent.register(apinput2, true);


		
		/**
		 * 7,账户状态处理(休眠转正常结息处理 + 修改电子账户状态)
		 */
		CapitalTransDeal.dealAcctStatAndSett(chkOT.getCuacst(), tblKnaAcct);
		
		
		/**
		 * 8,登记明细登记簿
		 */		
	    KnlIoblCups entity = SysUtil.getInstance(KnlIoblCups.class);
		entity.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水       
		entity.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期         
//		entity.setPrepsq(prepsq);//前置流水         
		entity.setUniseq(uniseq);//银联流水         
		entity.setCardno(cardno);//交易卡号         
//		entity.setDevcno(devcno);//设备号           
		entity.setTranam(tranam);//交易金额         
//		entity.setPrepdt(prepdt);//前置日期         
		entity.setCrcycd(crcycd);//币种             
//		entity.setCnkpdt(cnkpdt);//核心清算日期     
		entity.setUnkpdt(unkpdt);//银联清算日期     
//		entity.setOtacct(otacct);//转出账号         
//		entity.setOtacna(otacna);//转出账号户名     
//		entity.setInacct(inacct);//转入账号         
//		entity.setInacna(tblKnaAcct.getAcctna());//转入户名         
//		entity.setOtbrch(otbrch);//转出机构         
//		entity.setInbrch(inbrch);//转入机构         
//		entity.setTrbrch(trbrch);//交易机构         
//		entity.setPrdate(prdate);//前置交易日期     
//		entity.setPrbrmk(prbrmk);//代理机构标识号   
//		entity.setTrbrmk(trbrmk);//发送机构标识号   
//		entity.setTrcode(trcode);//交易码           
//		entity.setStand1(stand1);//32域             
//		entity.setStand2(stand2);//33域             
//		entity.setRetrdt(retrdt);//设备交易日期时间 
//		entity.setResssq(resssq);//原系统跟踪号     
//		entity.setReprsq(reprsq);//原前置流水号     
//		entity.setServsq(servsq);//全渠道流水号     
//		entity.setChckno(chckno);//对账分类编号     
//		entity.setChrgam(chrgam);//手续费           
//		entity.setBusino(busino);//商户代码         
//		entity.setBusitp(busitp);//商户类型         
		entity.setAuthno(authno);//预授权标识码     
//		entity.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//渠道代码         
//		entity.setMesstp(messtp);//报文类型         
//		entity.setTranst(E_CUPSST.SUCC);//交易状态         
		entity.setProccd(proccd);//处理码           
		//entity.setSpared(spared);//备用             
//		entity.setGlacct();//挂账账号         
//		entity.setGlacna();//挂账账号名称     
//		entity.setGlseeq();//挂账序号         
//		entity.setDescrb("银联CUPS消费撤销成功");//交易信息         
//		entity.setDjtype(E_RETYPE.CUPSZZ);//登记方式
//		entity.setPrepty(prepty);//银联交易类型
		KnlIoblCupsDao.insert(entity);
		
		/**
		 * 9,更改原交易交易状态
		 */
		KnlIoblCupsDao.updateOne_odb2(tblKnlIoblCups2);
		
		
		/**
		 * 10,短信通知
		 */
		
		IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
//		cplKubSqrd.setAppsid();//APP推送ID 
		cplKubSqrd.setCardno(cardno);//交易卡号  
//		cplKubSqrd.setPmvl01();//参数01    
//		cplKubSqrd.setPmvl02();//参数02    
//		cplKubSqrd.setPmvl03();//参数03    
//		cplKubSqrd.setPmvl04();//参数04    
//		cplKubSqrd.setPmvl05();//参数05    
		cplKubSqrd.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());//内部交易码
		cplKubSqrd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期  
		cplKubSqrd.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());//交易流水  
		cplKubSqrd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());//交易时间  
		IoPbSmsSvcType svcType = SysUtil.getInstance(IoPbSmsSvcType.class);
		svcType.pbTransqReg(cplKubSqrd);
		
		
		/**
		 * 10,清算登记
		 */
        IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
//      clerinfo.setTrandt();//交易日期                                             
//      clerinfo.setMntrsq();//主交易流水                                           
//      clerinfo.setRecdno();//记录次序号                                           
        clerinfo.setAcctno(outPro.getAcctno());//账号                                 
        clerinfo.setAcctna(outPro.getAcctna());//账户名称                             
        clerinfo.setProdcd(para.getParm_value1());//产品编号                               
        clerinfo.setClactp(clactp);//系统内账号类型                                   
//      clerinfo.setToacct();//对方账号                                             
//      clerinfo.setToacbr();//对方机构号                                           
        clerinfo.setAcctbr(tblKnaAcct.getBrchno());//账务机构                         
        clerinfo.setCrcycd(crcycd);//币种                                             
        clerinfo.setAmntcd(E_AMNTCD.CR);//借贷标志                                         
        clerinfo.setTranam(tranam);//交易金额                                         
        clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
        clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
        clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
//      clerinfo.setClerst(clerst);//数据同步标志                                   
        SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);

		/**
		 * 11，输出赋值
		 */
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
}
	/**
	 * 输入检查
	 * @param input
	 */
	private static void chkParam(final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Cupscx.Input input){
		if(CommUtil.isNull(input.getPrepsq())){//银联前置流水
			throw DpModuleError.DpstComm.BNAS1914();
		}
		if(CommUtil.isNull(input.getCardno())){//交易卡号
			throw DpModuleError.DpstComm.BNAS0572();
		}
//		if(CommUtil.isNull(input.getDevcno())){//设备号
//			throw DpModuleError.DpstComm.BNAS1915();
//		}
//		if(CommUtil.isNull(input.getAmntcd())){//借贷标志
//			throw DpModuleError.DpstComm.BNAS1370();
//		}
		//金额
		if(CommUtil.isNull(input.getPrepdt())){//银联前置日期
			throw DpModuleError.DpstComm.BNAS1916();
		}
		
		if(CommUtil.isNull(input.getCrcycd())){//币种
			throw DpModuleError.DpstComm.BNAS0195();
		}
		
		if(CommUtil.isNull(input.getCnkpdt())){//传统核心清算日期  
			//throw DpModuleError.DpstComm.E9999("核心清算日期不能为空");
		}
		
		if(CommUtil.isNull(input.getUnkpdt())){//银联清算日期
			throw DpModuleError.DpstComm.BNAS1917();
		}
		
//		if(CommUtil.isNull(input.getInacct())){//转入账号
//			throw DpModuleError.DpstAcct.BNAS0028();
//		}
		
//		if(CommUtil.isNull(input.getOtacct())){//转出账号
//			throw DpModuleError.DpstComm.BNAS1918();
//		}
		
		if(CommUtil.isNull(input.getInbrch())){
			//throw DpModuleError.DpstComm.E9999("转入机构不能为空");
		}
		
		if(CommUtil.isNull(input.getOtbrch())){
			//throw DpModuleError.DpstComm.E9999("转出机构不能为空");
		}
		
		if(CommUtil.isNull(input.getTrbrch())){
			//throw DpModuleError.DpstComm.E9999("受理机构不能为空");
		}
		
//		if(CommUtil.isNull(input.getTrcode())){//银联交易码
//			throw DpModuleError.DpstComm.BNAS1919();
//		}
		
		if(CommUtil.isNull(input.getServsq())){//全渠道流水号
			throw DpModuleError.DpstComm.BNAS1920();
		}
		
		if(CommUtil.isNull(input.getChckno())){
			//throw DpModuleError.DpstComm.E9999("对账分类编号不能为空");
		}
		
		if(CommUtil.isNull(input.getOtacna())){
			//throw DpModuleError.DpstComm.E9999("转出户名不能为空");
		}
		
		if(CommUtil.isNull(input.getUniseq())){//银联流水
			throw DpModuleError.DpstComm.BNAS1921();
		}
		
//		if(CommUtil.isNull(input.getChkqtn().getIsckqt())){//额度控制标志
//			throw DpModuleError.DpstAcct.BNAS1897();
//		}
		
		if(CommUtil.isNull(input.getFronsq())){
			throw DpModuleError.DpstComm.E9999("支付前置流水不能为空");
		}
		
		if(CommUtil.isNull(input.getFrondt())){
			throw DpModuleError.DpstComm.E9999("支付前置日期不能为空");
		}
		
		if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0){  //校验交易金额
			throw DpModuleError.DpstComm.BNAS0627();
		}

        if(CommUtil.isNotNull(input.getChkpwd())){  //参数不为空才验密
            ChkPwdIN chkpwd = input.getChkpwd();
            if(CommUtil.isNotNull(chkpwd.getIspass())&&(E_YES___.YES==chkpwd.getIspass())){   
////                EncryTools encryTools=SysUtil.getInstance(EncryTools.class);
//                String cryptoPassword=EncryTools.encryPassword(chkpwd.getPasswd(),chkpwd.getAuthif(),input.getCardno());
//                DpPassword.validatePassword(input.getCardno(), cryptoPassword,
//                        MsType.U_CHANNEL.ALL, "%");
//                
            }
        }
	}
	
}
