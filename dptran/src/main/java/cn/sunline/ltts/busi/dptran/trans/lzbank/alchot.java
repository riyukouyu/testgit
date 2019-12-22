package cn.sunline.ltts.busi.dptran.trans.lzbank;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_RETYPE;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
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
import cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Alchot.Input;
import cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Alchot.Output;
import cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Alchot.Property;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcctDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbSmsSvcType;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccountClearInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkOT;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubSqrd;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
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



public class alchot {

	public static void dealAlchot( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Alchot.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Alchot.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Alchot.Output output){

		String cardno = input.getCardno();//交易卡号    
		String devcno = input.getDevcno();//设备号          
		E_AMNTCD amntcd = input.getAmntcd();//借贷标志        
		BigDecimal tranam = input.getTranam();//交易金额        
		String crcycd = input.getCrcycd();//币种            
		String otacct = input.getOtacct();//转出账号        
		String otacna = input.getOtacna();//转出户名   
		String inacct = input.getInacct();//转入账号   
		String inacna = input.getInacna();//转入户名
		String retrdt = input.getRetrdt();//原日期时间      
		BigDecimal chrgam = input.getChrgam();//手续费          
		String busino = input.getBusino();//商户代码        
		String busitp = input.getBusitp();//商户类型        
		String authno = input.getAuthno();//预授权标识码    
		String messtp = input.getMesstp();//报文类型        
		String proccd = input.getProccd();//处理码          
		String spared = input.getSpared();//备用            
		ChkQtIN chkqtn = input.getChkqtn();//额度中心参数    
		String fronsq = input.getFronsq();//渠道请求流水    
		String frondt = input.getFrondt();//渠道请求日期 
		E_PREPTY prepty = input.getPrepty();//银联交易类型	  

		String smrycd = BusinessConstants.SUMMARY_ZC;//摘要代码-转出
		E_SBACTP sbactp = E_SBACTP._11;//账户类别-默认为活期结算账户
		E_CAPITP capitp = E_CAPITP.OT229;//资金交易类型-银联全渠道转出
		// 20180521 yanghao
		E_CLACTP clactp = E_CLACTP._37;//清算账户类型
		String tranev =  ApUtil.TRANS_EVENT_CUPSTR;//明细冲正事件
		String tranev2 =  ApUtil.TRANS_EVENT_CLER;//清算明细冲正事件
		/**
		 * add by liuz
		 * 2018/08/16
		 */
		E_ACCTROUTTYPE routeType1 = ApAcctRoutTools.getRouteType(otacct);
        E_YES___ isflag=E_YES___.NO;
        if(routeType1 == E_ACCTROUTTYPE.INSIDE){
           isflag = E_YES___.YES;//转出方为内部户
        }else{
           isflag = E_YES___.NO;//转出方为电子账户
        }
		/**
		 * end
		 */
		if(CommUtil.isNull(chrgam)){
			chrgam = BigDecimal.ZERO;
		}
		
		
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
		if(isflag==E_YES___.NO){//电子账户转内部户
		
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
			    throw DpModuleError.DpstComm.E9999("非活跃账户不允许出账");
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
	//		if(!CommUtil.equals(otbrch, tblKnaAcct.getBrchno())){//转出机构校验
	//			throw DpModuleError.DpstComm.BNAS0031();
	//		}
			
			
			
			
			/**
			 * 4,可用余额校验
			 */
			if(accatp == E_ACCATP.WALLET){ 
				if(CommUtil.compare(tblKnaAcct.getOnlnbl(), tranam.add(chrgam)) < 0){
					throw DpModuleError.DpstComm.BNAS0442();
				}
			}else{
				BigDecimal usebal = CommTools.getRemoteInstance(DpAcctSvcType.class).getAcctaAvaBal(tblKnaAcct.getCustac(), tblKnaAcct.getAcctno(),tblKnaAcct.getCrcycd(), E_YES___.YES, E_YES___.NO);
				if(CommUtil.compare(usebal, tranam.add(chrgam)) < 0){
					throw DpModuleError.DpstComm.BNAS0442();
				}
			}
			
			
			/**
			 * 5,资金交易前检查
			 */
			AcTranfeChkIN chkIN = SysUtil.getInstance(AcTranfeChkIN.class);
			chkIN.setAccatp(accatp);//电子账户分类 
			chkIN.setCardno(tblKnaAcdc.getCardno());//电子账户卡号 
			chkIN.setCustac(tblKnaAcdc.getCustac());//电子账号ID   
			chkIN.setCustna(tblKnaAcct.getAcctna());//电子账户户名 
			chkIN.setOpcard(inacct);//对方账户卡号  
			chkIN.setOppona(inacna);//对方户名     
			chkIN.setCapitp(capitp);//转账交易类型 
			chkIN.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道     		
			AcTranfeChkOT chkOT = CapitalTransCheck.chkTranfe(chkIN);
			
			
			/**
			 * 5,扣减账户额度
			 */		
			IoCaSevAccountLimit qt = CommTools.getRemoteInstance(IoCaSevAccountLimit.class); 
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter qtIn = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter.class);
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output	qtOt = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output.class);		
			qtIn.setBrchno(tblKnaAcct.getBrchno());
			qtIn.setAclmfg(chkqtn.getAclmfg());
			qtIn.setAccttp(accatp);
			qtIn.setCustac(tblKnaAcdc.getCustac());
			qtIn.setCustid(chkqtn.getCustid());
			qtIn.setCustlv(chkqtn.getCustlv());
			qtIn.setAcctrt(chkqtn.getAcctrt());
			qtIn.setLimttp(chkqtn.getLimttp());
			qtIn.setPytltp(chkqtn.getPytltp());
			qtIn.setRebktp(chkqtn.getRebktp());
			qtIn.setRisklv(chkqtn.getRisklv());
			qtIn.setSbactp(sbactp);
			qtIn.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());
			qtIn.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			qtIn.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
			qtIn.setTranam(tranam);
			qtIn.setCustie(chkOT.getIsbind());//是否绑定卡标识
			qtIn.setFacesg(chkOT.getFacesg());//是否面签标识
			qtIn.setRecpay(null);//收付方标识，电子账户转电子账户需要输入
			qt.SubAcctQuota(qtIn, qtOt);//额度检查扣减
			
			/**
			 * 6,记账
			 */
			//1,内部户贷方记账
			KnpParameter para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "13", "%", true);
			if(CommUtil.isNull(para)){
				throw DpModuleError.DpstComm.E9999("业务代码未配置！");
			}
			IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
			acdrIn.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
			acdrIn.setTrantp(E_TRANTP.TR);//交易类型 
			acdrIn.setCrcycd(tblKnaAcct.getCrcycd());
			acdrIn.setSmrycd(smrycd);//摘要码-ZC
			acdrIn.setToacct(tblKnaAcdc.getCardno());
			acdrIn.setToacna(tblKnaAcct.getAcctna());
			acdrIn.setTranam(tranam);
			acdrIn.setBusino(para.getParm_value1());//业务编码
			acdrIn.setSubsac(para.getParm_value2());//子户号
			IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
			IaTransOutPro outPro = inAcctSer.ioInAccr(acdrIn);
			
			//2,电子账户支取记账
			DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class); //存款记账服务
			//SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class); //电子账户存入记账复合类型
			DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
			drawDpAcctIn.setAcctno(tblKnaAcct.getAcctno()); //结算账户、钱包账户
			drawDpAcctIn.setCustac(tblKnaAcdc.getCustac());
			drawDpAcctIn.setCardno(tblKnaAcdc.getCardno());
			drawDpAcctIn.setOpacna(outPro.getAcctna());
			drawDpAcctIn.setToacct(outPro.getAcctno());
			drawDpAcctIn.setCrcycd(tblKnaAcct.getCrcycd());
			drawDpAcctIn.setBankcd(tblKnaAcct.getBrchno());
			drawDpAcctIn.setTranam(tranam);
			drawDpAcctIn.setSmrycd(smrycd);
	//		drawDpAcctIn.setOpbrch(inbrch);	
			dpSrv.addDrawAcctDp(drawDpAcctIn);
			
			//3,平衡性检查
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
			
			//4,冲正注册
			IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
			cplInput.setCustac(tblKnaAcdc.getCustac());
			cplInput.setEvent1(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			cplInput.setEvent2(CommTools.getBaseRunEnvs().getTrxn_date());
			cplInput.setEvent3(E_YES___.NO.getValue()); //是否是银联转入确认交易
			cplInput.setTranev(tranev);
			//ApStrike.regBook(cplInput);
			
			IoApRegBook cplInput2 = SysUtil.getInstance(IoApRegBook.class);
			cplInput2.setCustac(tblKnaAcdc.getCustac());
			cplInput2.setEvent1(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			cplInput2.setEvent2(CommTools.getBaseRunEnvs().getTrxn_date());
			cplInput2.setTranev(tranev2);
			//ApStrike.regBook(cplInput2);
			

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
			entity.setUniseq(fronsq);//银联流水 
			entity.setCardno(cardno);//交易卡号         
			entity.setTranam(tranam);//交易金额         
			entity.setCrcycd(crcycd);//币种             
//			entity.setOtacct(otacct);//转出账号         
//			entity.setOtacna(tblKnaAcct.getAcctna());//转出账号户名     
//			entity.setInacct(inacct);//转入账号         
//			entity.setInacna(inacna);//转入户名         
//			entity.setBusino(busino);//商户代码         
//			entity.setBusitp(busitp);//商户类型         
			entity.setAuthno(authno);//预授权标识码     
//			entity.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//渠道代码         
//			entity.setMesstp(messtp);//报文类型         
//			entity.setTranst(E_CUPSST.);//交易状态         
			entity.setProccd(proccd);//处理码           
			//entity.setSpared(spared);//备用             
	//		entity.setGlacct();//挂账账号         
	//		entity.setGlacna();//挂账账号名称     
	//		entity.setGlseeq();//挂账序号         
//			entity.setDescrb("银联全渠道转出成功");//交易信息     
//			entity.setPrepty(prepty);//银联交易类型
//			entity.setDjtype(E_RETYPE.ALCHZZ);//登记方式
			KnlIoblCupsDao.insert(entity);
			
			/**
			 * 9,短信通知
			 */
			sendMessage(input,property, output);
			
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
	        clerinfo.setAmntcd(E_AMNTCD.DR);//借贷标志                                         
	        clerinfo.setTranam(tranam);//交易金额                                         
	        clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
	        clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
	        clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
	//      clerinfo.setClerst(clerst);//数据同步标志                                   
	        SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
		}else if(isflag == E_YES___.YES){//内部户转内部户
			GlKnaAcct glKnaAcct = GlKnaAcctDao.selectOne_odb1(otacct, false);
            if (CommUtil.isNull(glKnaAcct)) {
                throw DpModuleError.DpstComm.E9999("转出方账号不存在！");
            }
//            if(!CommUtil.equals(input.getOtacna(), glKnaAcct.getAcctna())){
//                throw DpModuleError.DpstComm.E9999("转出方账户名称输入错误！");
//            }
            
          //1,内部户贷方记账
			KnpParameter para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "13", "%", true);
			if(CommUtil.isNull(para)){
				throw DpModuleError.DpstComm.E9999("业务代码未配置！");
			}
			IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
			acdrIn.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
			acdrIn.setTrantp(E_TRANTP.TR);//交易类型 
			acdrIn.setCrcycd(glKnaAcct.getCrcycd());
			acdrIn.setSmrycd(smrycd);//摘要码-ZC
			acdrIn.setToacct(glKnaAcct.getAcctno());
			acdrIn.setToacna(glKnaAcct.getAcctna());
			acdrIn.setTranam(tranam);
			acdrIn.setBusino(para.getParm_value1());//业务编码
			acdrIn.setSubsac(para.getParm_value2());//子户号
			IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
			IaTransOutPro outPro = inAcctSer.ioInAccr(acdrIn);
            
			//2.内部户借方记账
            IaAcdrInfo acdrIn2 = SysUtil.getInstance(IaAcdrInfo.class);
            acdrIn2.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
            acdrIn2.setTrantp(E_TRANTP.TR);//交易类型 
            acdrIn2.setCrcycd(glKnaAcct.getCrcycd());
            acdrIn2.setSmrycd(smrycd);//摘要码-ZR
            acdrIn2.setToacct(input.getInacct());
            acdrIn2.setToacna(input.getInacna());
            acdrIn2.setTranam(tranam);
            acdrIn2.setBusino(glKnaAcct.getBusino());//业务编码
            acdrIn2.setSubsac(glKnaAcct.getSubsac());//子户号
            IoInAccount inAcctSer2 = SysUtil.getInstance(IoInAccount.class);
            IaTransOutPro outPro2 = inAcctSer2.ioInAcdr(acdrIn2);
            
            //3,平衡性检查
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
			
			//4,冲正注册
			IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
			cplInput.setCustac(input.getOtacct());
			cplInput.setEvent1(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			cplInput.setEvent2(CommTools.getBaseRunEnvs().getTrxn_date());
			cplInput.setEvent3(E_YES___.NO.getValue()); //是否是银联转入确认交易
			cplInput.setTranev(tranev);
			//ApStrike.regBook(cplInput);
			
			IoApRegBook cplInput2 = SysUtil.getInstance(IoApRegBook.class);
			cplInput2.setCustac(input.getOtacct());
			cplInput2.setEvent1(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			cplInput2.setEvent2(CommTools.getBaseRunEnvs().getTrxn_date());
			cplInput2.setTranev(tranev2);
			//ApStrike.regBook(cplInput2);
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
			 * 8,登记明细登记簿
			 */		
		    KnlIoblCups entity = SysUtil.getInstance(KnlIoblCups.class);
			entity.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水       
			entity.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期         
			entity.setUniseq(fronsq);//银联流水  
			entity.setCardno(cardno);//交易卡号         
			entity.setTranam(tranam);//交易金额         
			entity.setCrcycd(crcycd);//币种             
//			entity.setOtacct(input.getOtacct());//转出账号         
//			entity.setOtacna(input.getOtacna());//转出账号户名     
//			entity.setInacct(inacct);//转入账号         
//			entity.setInacna(inacna);//转入户名         
//			entity.setBusino(busino);//商户代码         
//			entity.setBusitp(busitp);//商户类型         
			entity.setAuthno(authno);//预授权标识码     
//			entity.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//渠道代码         
//			entity.setMesstp(messtp);//报文类型         
			entity.setProccd(proccd);//处理码           
			//entity.setSpared(spared);//备用             
	//		entity.setGlacct();//挂账账号         
	//		entity.setGlacna();//挂账账号名称     
	//		entity.setGlseeq();//挂账序号         
//			entity.setDescrb("银联全渠道转出成功");//交易信息  
//			entity.setPrepty(prepty);//银联交易类型
//			entity.setDjtype(E_RETYPE.ALCHZZ);//登记方式
			KnlIoblCupsDao.insert(entity);
			
			/**
			 * 9,短信通知
			 */
	//		sendMessage(input,property, output);//转出方为内部户时不需发送短信
			
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
	        clerinfo.setAcctbr(glKnaAcct.getBrchno());//账务机构                         
	        clerinfo.setCrcycd(crcycd);//币种                                             
	        clerinfo.setAmntcd(E_AMNTCD.DR);//借贷标志                                         
	        clerinfo.setTranam(tranam);//交易金额                                         
	        clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
	        clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
	        clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
	//      clerinfo.setClerst(clerst);//数据同步标志                                   
	        SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
		}
		
		/**
		 * 11，输出赋值
		 */
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
	
	}

	private static void chkParam(Input input) {
		if(CommUtil.isNull(input.getCardno())){//交易卡号
			throw DpModuleError.DpstComm.BNAS0572();
		}
		if(CommUtil.isNull(input.getAmntcd())){//借贷标志
			throw DpModuleError.DpstComm.BNAS1370();
		}
		
		if(CommUtil.isNull(input.getCrcycd())){//币种
			throw DpModuleError.DpstComm.BNAS1101();
		}
		
		if(CommUtil.isNull(input.getInacct())){//转入账号
			throw DpModuleError.DpstAcct.BNAS0028();
		}
		
		if(CommUtil.isNull(input.getOtacct())){//转出账号
			throw DpModuleError.DpstComm.BNAS1918();
		}
		
//		if(CommUtil.isNull(input.getChkqtn().getIsckqt())){//额度控制标志
//			throw DpModuleError.DpstAcct.BNAS1897();
//		}
		
		if(CommUtil.isNull(input.getFronsq())){
			throw DpModuleError.DpstComm.E9999("渠道请求流水不能为空");
		}
		
		if(CommUtil.isNull(input.getFrondt())){
			throw DpModuleError.DpstComm.E9999("渠道请求日期不能为空");
		}
		
		if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0){  //校验交易金额
			throw DpModuleError.DpstComm.BNAS0627();
		}
		
		//密码验证  20180515 yanghao
        if(CommUtil.isNotNull(input.getChkpwd())){  //参数不为空才验密
            ChkPwdIN chkpwd = input.getChkpwd();
            if(CommUtil.isNotNull(chkpwd.getIspass())&&(E_YES___.YES==chkpwd.getIspass())){   
//                EncryTools encryTools=SysUtil.getInstance(EncryTools.class);
//                String cryptoPassword=EncryTools.encryPassword(chkpwd.getPasswd(),chkpwd.getAuthif(),input.getCardno());
//                DpPassword.validatePassword(input.getCardno(), cryptoPassword,
//                        MsType.U_CHANNEL.ALL, "%");
                
            }
        }
	}

	private static void sendMessage(Input input, Property property,
			Output output) {
//		MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//		mri.setMtopic("Q0101005");
//		ToAppSendMsg AppSendMsgInput = SysUtil.getInstance(ToAppSendMsg.class);
//		//根据卡号查询电子账号
//		IoCaKnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCard(input.getCardno(), false);
//		 String custac = tblKnaAcdc.getCustac();
//		 //查询电子账户信息
//		 CaCustInfo.accoutinfos accoutinfos = SysUtil.getInstance(CaCustInfo.accoutinfos.class);
//		 accoutinfos = EacctMainDao.selCustInfobyCustac(custac,
//					E_ACALST.NORMAL, E_ACALTP.CELLPHONE, false);
//		AppSendMsgInput.setUserId(accoutinfos.getCustid()); // 用户ID
//		AppSendMsgInput.setOutNoticeId("Q0101005");//外部消息ID
//		AppSendMsgInput.setNoticeTitle("资金变动");//公告标题
//		String date  = CommTools.getBaseRunEnvs().getTrxn_date().substring(0,4)+"年"
//		+CommTools.getBaseRunEnvs().getTrxn_date().substring(4,6)+"月"+ 
//				CommTools.getBaseRunEnvs().getTrxn_date().substring(6,8)+"日"+
//				BusiTools.getBusiRunEnvs().getTrantm().substring(0,2)+":"+
//				BusiTools.getBusiRunEnvs().getTrantm().substring(2,4)+":"+
//				BusiTools.getBusiRunEnvs().getTrantm().substring(4,6);
//		
//		/*AppSendMsgInput.setContent("您的电子账户于"+date+"支出金额:"+
//		   input.getTranam()+"元,"+"对方户名:"+input.getInacna()+",请点击查看详情。"); //内容
//		   			   			   
//	*/			StringBuffer sb=new StringBuffer();
//	//获取可用余额
//	    IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);
//	    BigDecimal acctbl = BigDecimal.ZERO;
//	    acctbl = SysUtil.getInstance(DpAcctSvcType.class).
//	            getAcctaAvaBal(cplKnaAcct.getCustac(), cplKnaAcct.getAcctno(),
//	                    input.getCrcycd(), E_YES___.YES, E_YES___.NO);
//	    sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易时间：").append(date).
//	        append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易类型：").append("提现").
//	        append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易金额：").append(input.getTranam()+"元").
//	        append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;可用余额：").append(acctbl+"元").
//	        append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;对方账户：").append(accoutinfos.getCustna()).
//	        append("请点击查看详情。");
//		AppSendMsgInput.setContent(sb.toString());
//		AppSendMsgInput.setSendtime(CommTools.getBaseRunEnvs().getTrxn_date());//消息生成时间
//		AppSendMsgInput.setClickType(cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_CLIKTP.YES);
//		AppSendMsgInput.setClickValue("LOGINURL||/page/electronicAcct/bill/electAcctBill.html");//点击动作值
//		AppSendMsgInput.setTirggerSys(CommTools.getBaseRunEnvs().getSystcd());//触发系统
//		AppSendMsgInput.setTransType(cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_APPTTP.CAPICH);
//		
//		
//		mri.setMsgtyp("ApSmsType.ToAppSendMsg");
//		mri.setMsgobj(AppSendMsgInput); 
//		AsyncMessageUtil.add(mri);
		/**
		 * 短信通知登记
		 */
		IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
//		cplKubSqrd.setAppsid();//APP推送ID 
		cplKubSqrd.setCardno(input.getCardno());//交易卡号  
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
	}
}
