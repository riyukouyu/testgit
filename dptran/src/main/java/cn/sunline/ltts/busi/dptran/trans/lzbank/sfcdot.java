package cn.sunline.ltts.busi.dptran.trans.lzbank;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIobl;
import cn.sunline.ltts.busi.dp.type.DpAcctType.ChkPwdIN;
import cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Sfcdot.Input;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcctDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbSmsSvcType;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccountClearInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkOT;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubSqrd;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;



public class sfcdot {

	/**
	 * 交易前处理
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void dealBefore( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Sfcdot.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Sfcdot.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Sfcdot.Output output){
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();//主交易流水
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//交易日期
		E_CAPITP capitp = input.getCapitp();//资金交易类型
		String otcard =  input.getOtcard();//转出方卡号/账号
		String otacna = input.getOtacna();//转出方户名
		String otbrch = input.getOtbrch();//转出方账号所属机构
		BigDecimal tlcgam = input.getTlcgam();//收费总金额
		String crcycd = input.getCrcycd();//币种
		String fronsq = input.getFronsq();//支付前置流水
		String frondt = input.getFrondt();//支付前置日期
		String incard = input.getIncard();//转入方卡号
		String inacna = input.getInacna();//转入方户名
		BigDecimal tranam = input.getTranam();//交易金额
		//20180521 yanghao
		BaseEnumType.E_CLACTP clactp = BaseEnumType.E_CLACTP._33;//清算账户类型
		E_SBACTP sbactp = null;//额度中心参数-子账户类型
		E_YES___ issucc = E_YES___.NO;//是否重复流水
		
		E_ACCTROUTTYPE routeType1 = ApAcctRoutTools.getRouteType(input.getOtcard());
        if(routeType1 == E_ACCTROUTTYPE.INSIDE){
            property.setToisin(E_YES___.YES);
        }else{
            property.setToisin(E_YES___.NO);
        }
        String servtp=CommTools.getBaseRunEnvs().getChannel_id();
        if(!CommUtil.equals(servtp, "IM")){
            servtp="IM";
        }
		
		/**
		 * 1，重复提交检查
		 */
		KnlIobl iobl = ActoacDao.selKnlIoblDetl(transq, trandt, false);
		if(CommUtil.isNotNull(iobl)){
			property.setIssucc(E_YES___.YES);
			output.setMntrdt(iobl.getTrandt());	//
			output.setMntrsq(iobl.getTransq()); // 主流水
			output.setMntrtm(iobl.getTrantm());
			return;
		}
		
		
		/**
		 * 2,非空校验 和 收费检查
		 */
		checkNotNull(input);
		
		
		/**
		 * 3,交易类型校验
		 */
		/*if(E_CAPITP.OT201 != capitp){//资金交易类型校验（201 电子账户转本行借记卡）
			throw DpModuleError.DpstComm.BNAS1188(capitp.getLongName());
		}*/
		
		
		/**
		 * 4,获取账户信息，校验账户状态
		 */
		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(otcard, false);//获取卡和电子账号映射
		if(CommUtil.isNotNull(tblKnaAcdc)){
		    if(E_DPACST.CLOSE == tblKnaAcdc.getStatus()){
	            throw DpModuleError.DpstComm.BNAS0441();
	        }
	        //获取负债账户信息
	        KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
	        E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
	        if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
	            tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
	            sbactp = E_SBACTP._11;
	        }else{ // III类户
	            tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
	            sbactp = E_SBACTP._12;
	            if(CommUtil.isNotNull(input.getIncstp()) && input.getIncstp() != E_ACSETP.MA){
	                throw DpModuleError.DpstComm.BNAS0029();
	            }
	        }  
	       
	        /**
	         * 6， 资金交易前检查
	         */
	        AcTranfeChkIN chkIN = SysUtil.getInstance(AcTranfeChkIN.class);
	        chkIN.setAccatp(accatp);//电子账户分类 
	        chkIN.setCardno(tblKnaAcdc.getCardno());//电子账户卡号 
	        chkIN.setCustac(tblKnaAcdc.getCustac());//电子账号ID   
	        chkIN.setCustna(tblKnaAcct.getAcctna());//电子账户户名 
	        chkIN.setOpcard(incard);//对方账户卡号  
	        chkIN.setOppona(inacna);//对方户名     
	        chkIN.setCapitp(capitp);//转账交易类型 
	        chkIN.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道          
	        AcTranfeChkOT chkOT = CapitalTransCheck.chkTranfe(chkIN);
	        /**
	         * 7,可用余额校验
	         */
	        if(accatp == E_ACCATP.WALLET){ 
	            if(CommUtil.compare(tblKnaAcct.getOnlnbl(), tranam.add(tlcgam)) < 0){
	                throw DpModuleError.DpstComm.BNAS0442();
	            }
	        }else{
	            BigDecimal usebal = CommTools.getRemoteInstance(DpAcctSvcType.class).getAcctaAvaBal(tblKnaAcct.getCustac(), tblKnaAcct.getAcctno(),tblKnaAcct.getCrcycd(), E_YES___.YES, E_YES___.NO);
	            if(CommUtil.compare(usebal, tranam.add(tlcgam)) < 0){
	                throw DpModuleError.DpstComm.BNAS0442();
	            }
	        }
	        
	        /**
	         * 8,额度参数设置
	         */
	        input.getChkqtn().setSbactp(sbactp);
	        input.getChkqtn().setAccttp(accatp);
	        input.getChkqtn().setCustac(tblKnaAcct.getCustac());
	        input.getChkqtn().setBrchno(tblKnaAcct.getBrchno());// 交易机构号
	        input.getChkqtn().setCustie(chkOT.getIsbind()); //是否绑定卡标识
	        input.getChkqtn().setFacesg(chkOT.getFacesg()); //是否面签标识
	        input.getChkqtn().setRecpay(null); //收付方标识，电子账户转电子账户需要输入
	        
	        /**
	         * 9,属性参数设置
	         */
	        //获取记账业务代码
	        KnpParameter para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "06", "%", true);
	        
	        //1，交易明细登记属性
	        if(CommUtil.compare(tlcgam, BigDecimal.ZERO) > 0){
	            property.setIschrg(E_YES___.YES); //是否收费 
	            property.setChgflg(E_CHGFLG.ALL); //记账标志 
	        }else{
	            property.setIschrg(E_YES___.NO); //是否收费 
	        }
	        property.setOtcsac(tblKnaAcct.getCustac());//转出方电子账号ID  
	        property.setOtchld(tblKnaAcct.getAcctno());//转出方子账号      
//	      property.setOtcorp();//转出方法人代码    
//	      property.setIncorp();//转入方法人代码    
//	      property.setIncsac();//转入方电子账号ID  
//	      property.setInchld();//转入方子账号  
	        
	        //2，公共属性
//	      property.setBusisq();//业务跟踪编号      
	        property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//交易流水          
	        property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期          
	        property.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());//交易码            
	        property.setIoflag(E_IOFLAG.OUT);//出入金标志 -出金       
	        property.setBrchno(tblKnaAcct.getBrchno());//电子账户归属机构  
	        property.setFrosq1(fronsq);//渠道流水          
	        property.setFrodt1(frondt);//渠道日期  
	        property.setClactp(clactp);//清算账户类型  
	        
	        //3,记账属性
	        property.setAcctno(tblKnaAcct.getAcctno());//负债账号          
	        property.setCardno(tblKnaAcdc.getCardno());//卡号              
	        property.setCustac(tblKnaAcdc.getCustac());//客户账号 
	        
	        //modify by zdj 20190311
	        property.setToacct(incard);//对方账号          
	        property.setOpacna(inacna);//对方户名 
	        //modify by zdj end
//	      property.setLinkno();//连笔标志          
//	      property.setSmryds();//摘要描述          
	        property.setServtp(servtp);//交易渠道         
	        property.setIntrcd(CommTools.getBaseRunEnvs().getTrxn_code());//内部交易码        
	        property.setIssucc(issucc);//验证流水是否已存在
//	      property.setAcnoin();//内部户帐号        
	        property.setAcbcin(CommTools.getBaseRunEnvs().getTrxn_branch());//账户机构          
	        property.setToacin(tblKnaAcdc.getCardno());//对方账号          
	        property.setTonain(tblKnaAcct.getAcctna());//对方户名          
	        property.setBusino(para.getParm_value1());//业务代码          
	        property.setSubsac(para.getParm_value2());//子户号            
//	      property.setAcctna();//内部户帐号名称    
//	      property.setDscrtx();//描述              
	        
		}else{
		    GlKnaAcct glKnaAcct = GlKnaAcctDao.selectOne_odb1(otcard, false);//转出方账户为内部户   
	        if(CommUtil.isNull(glKnaAcct)){
	            throw DpModuleError.DpstComm.E9027("转出方账号输入有误！");
	        }
	        /**
	         * 5，输入账户信息校验-户名，币种，机构
	         */
	        if(!CommUtil.equals(otacna, glKnaAcct.getAcctna())){
	            throw DpModuleError.DpstComm.BNAS0892();
	        }
	        if(!CommUtil.equals(crcycd, glKnaAcct.getCrcycd())){
	            throw DpModuleError.DpstComm.BNAS0632();
	        }
	        if(CommUtil.isNotNull(otbrch)){
	            if(!CommUtil.equals(otbrch, glKnaAcct.getBrchno())){
	                throw DpModuleError.DpstComm.BNAS0031();
	            }
	        }
	        
	        //获取记账业务代码
            KnpParameter para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "06", "%", true);
            
            //1，交易明细登记属性
            if(CommUtil.compare(tlcgam, BigDecimal.ZERO) > 0){
                property.setIschrg(E_YES___.YES); //是否收费 
                property.setChgflg(E_CHGFLG.ALL); //记账标志 
            }else{
                property.setIschrg(E_YES___.NO); //是否收费 
            }
            property.setOtcsac(glKnaAcct.getAcctno());//转出方电子账号ID  
//            property.setOtchld(glKnaAcct.getAcctno());//转出方子账号      
//        property.setOtcorp();//转出方法人代码    
//        property.setIncorp();//转入方法人代码    
//        property.setIncsac();//转入方电子账号ID  
//        property.setInchld();//转入方子账号  
            
            //2，公共属性
//        property.setBusisq();//业务跟踪编号      
            property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//交易流水          
            property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期          
            property.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());//交易码            
            property.setIoflag(E_IOFLAG.OUT);//出入金标志 -出金       
            property.setBrchno(glKnaAcct.getBrchno());//电子账户归属机构  
            property.setFrosq1(fronsq);//渠道流水          
            property.setFrodt1(frondt);//渠道日期  
            property.setClactp(clactp);//清算账户类型  
            
            //3,记账属性
            property.setAcctno(glKnaAcct.getAcctno());//负债账号          
            property.setCardno(glKnaAcct.getAcctno());//卡号              
//            property.setCustac(tblKnaAcdc.getCustac());//客户账号          
//        property.setToacct();//对方账号          
//        property.setOpacna();//对方户名          
//        property.setLinkno();//连笔标志          
//        property.setSmryds();//摘要描述          
            property.setServtp(servtp);//交易渠道         
            property.setIntrcd(CommTools.getBaseRunEnvs().getTrxn_code());//内部交易码        
            property.setIssucc(issucc);//验证流水是否已存在
//        property.setAcnoin();//内部户帐号        
            property.setAcbcin(CommTools.getBaseRunEnvs().getTrxn_branch());//账户机构          
            property.setToacin(glKnaAcct.getAcctno());//对方账号          
            property.setTonain(glKnaAcct.getAcctna());//对方户名          
            property.setBusino(para.getParm_value1());//业务代码          
            property.setSubsac(para.getParm_value2());//子户号            
//        property.setAcctna();//内部户帐号名称    
//        property.setDscrtx();//描述      
	        
	        
		}
				
	}
	
	
	//非空校验
	private static void checkNotNull(Input input) {
	
		/*if (CommUtil.isNull(input.getCapitp())) {//资金交易类型
			throw DpModuleError.DpstComm.BNAS0023();
		}*/
		if(CommUtil.isNull(input.getOtcard())){//转出方账号
			throw DpModuleError.DpstComm.BNAS0042();
		}
		if(CommUtil.isNull(input.getOtacna())){//转出方户名
			throw DpModuleError.DpstComm.BNAS0045();
		}
		if (CommUtil.isNull(input.getCrcycd())) {//币种
			throw DpModuleError.DpstComm.BNAS1101();
		}
		if (CommUtil.isNull(input.getCsextg())) {//钞汇标志
			throw DpModuleError.DpstComm.BNAS1047();
		}	
		if (CommUtil.isNull(input.getTranam())) {//交易金额
			throw DpModuleError.DpstAcct.BNAS0623();
		}
		if (CommUtil.isNull(input.getSmrycd())) {//摘要码
			throw DpModuleError.DpstComm.BNAS0195();
		}
		if(CommUtil.isNull(input.getIncard())){//转入方账号
			throw DpModuleError.DpstComm.BNAS0030();
		}
		if(CommUtil.isNull(input.getInacna())){//转入方户名
			throw DpModuleError.DpstComm.BNAS0032();
		}
//		if (CommUtil.isNull(input.getTlcgam())) {//收费总金额
//			throw DpModuleError.DpstComm.BNAS0331();
//		}
	//	if (CommUtil.isNull(input.getChckdt())) {
	//		throw DpModuleError.DpstComm.E9027("对账日期");
	//	}
	//	if (CommUtil.isNull(input.getKeepdt())) {
	//		throw DpModuleError.DpstComm.E9027("清算日期");
	//	}
//		if (CommUtil.isNull(input.getChkqtn().getIsckqt())) {//额度控制标志
//			throw DpModuleError.DpstComm.BNAS0802();
//		}
		
		//金额校验
		if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0){ //校验交易金额
			throw DpModuleError.DpstComm.BNAS0627();
		}
		if(CommUtil.compare(input.getTlcgam(), BigDecimal.ZERO) < 0){ //校验收费总金额
			throw DpModuleError.DpstComm.BNAS0328();
		}
		
		//密码验证  20180515 yanghao
        if(CommUtil.isNotNull(input.getChkpwd())){  //参数不为空才验密
            ChkPwdIN chkpwd = input.getChkpwd();
            if(CommUtil.isNotNull(chkpwd.getIspass())&&(E_YES___.YES==chkpwd.getIspass())){
//                EncryTools encryTools=SysUtil.getInstance(EncryTools.class);
//                String cryptoPassword=EncryTools.encryPassword(chkpwd.getPasswd(),chkpwd.getAuthif(),input.getOtcard());
//                DpPassword.validatePassword(input.getOtcard(), cryptoPassword,
//                        MsType.U_CHANNEL.ALL, "%");
            }
        }
		
		
		
		//收费参数检查
//		if(CommUtil.compare(input.getTlcgam(),BigDecimal.ZERO) > 0){
//			// 收费交易金额检查
//			if(input.getChrgpm().size() <= 0){
//				throw DpModuleError.DpstComm.BNAS0395();
//			}
//			BigDecimal totPaidam = BigDecimal.ZERO;
//			for (IoCgCalCenterReturn IoCgCalCenterReturn : input.getChrgpm()) {
//				BigDecimal tranam = IoCgCalCenterReturn.getTranam();// 交易金额
//				BigDecimal clcham = IoCgCalCenterReturn.getClcham();// 应收费用金额（未优惠）
//				BigDecimal dircam = IoCgCalCenterReturn.getDircam();// 优惠后应收金额
//				BigDecimal paidam = IoCgCalCenterReturn.getPaidam();// 实收金额
//				
//				if (CommUtil.isNotNull(tranam)) {
//					if(CommUtil.compare(tranam, BigDecimal.ZERO) < 0){ //交易金额金额
//						throw DpModuleError.DpstComm.BNAS0338();
//					}
//					if(CommUtil.compare(tranam, input.getTranam()) != 0){ //交易金额金额
//						throw DpModuleError.DpstComm.BNAS0337();
//					}
//				}
//				if (CommUtil.isNotNull(clcham)) {
//					if(CommUtil.compare(clcham, BigDecimal.ZERO) < 0){ //交易金额金额
//						throw DpModuleError.DpstComm.BNAS0244();
//					}
//				}
//				if (CommUtil.isNotNull(dircam)) {
//					if(CommUtil.compare(dircam, BigDecimal.ZERO) < 0){ //交易金额金额
//						throw DpModuleError.DpstComm.BNAS0237();
//					}
//				}
//				if (CommUtil.isNotNull(paidam)) {
//					if(CommUtil.compare(paidam, BigDecimal.ZERO) < 0){ //交易金额金额
//						throw DpModuleError.DpstComm.BNAS0355();
//					}
//				}
//				
//				
//				totPaidam = totPaidam.add(paidam);
//			}
//			
//			if(!CommUtil.equals(totPaidam, input.getTlcgam())){
//				throw DpModuleError.DpstComm.BNAS0243();
//			}
//		}
		
	}
	
	/**
	 * 短信通知登记
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void sendMessage( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Sfcdot.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Sfcdot.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Sfcdot.Output output){
	    
//			MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//			mri.setMtopic("Q0101005");
//			ToAppSendMsg AppSendMsgInput = SysUtil.getInstance(ToAppSendMsg.class);
//			//根据卡号查询电子账号
//			IoCaKnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCard(input.getOtcard(), false);
//			 String custac = tblKnaAcdc.getCustac();
//			 //查询电子账户信息
//			 CaCustInfo.accoutinfos accoutinfos = SysUtil.getInstance(CaCustInfo.accoutinfos.class);
//			 accoutinfos = EacctMainDao.selCustInfobyCustac(custac,
//						E_ACALST.NORMAL, E_ACALTP.CELLPHONE, false);
//			AppSendMsgInput.setUserId(accoutinfos.getCustid()); // 用户ID
//			AppSendMsgInput.setOutNoticeId("Q0101005");//外部消息ID
//			AppSendMsgInput.setNoticeTitle("资金变动");//公告标题
//			String date  = CommTools.getBaseRunEnvs().getTrxn_date().substring(0,4)+"年"
//			+CommTools.getBaseRunEnvs().getTrxn_date().substring(4,6)+"月"+ 
//					CommTools.getBaseRunEnvs().getTrxn_date().substring(6,8)+"日"+
//					BusiTools.getBusiRunEnvs().getTrantm().substring(0,2)+":"+
//					BusiTools.getBusiRunEnvs().getTrantm().substring(2,4)+":"+
//					BusiTools.getBusiRunEnvs().getTrantm().substring(4,6);
//			
//			/*AppSendMsgInput.setContent("您的电子账户于"+date+"支出金额:"+
//			   input.getTranam()+"元,"+"对方户名:"+input.getInacna()+",请点击查看详情。"); //内容
//			   			   			   
//		*/			StringBuffer sb=new StringBuffer();
//		//获取可用余额
//		    IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);
//		    BigDecimal acctbl = BigDecimal.ZERO;
//		    acctbl = SysUtil.getInstance(DpAcctSvcType.class).
//		            getAcctaAvaBal(cplKnaAcct.getCustac(), cplKnaAcct.getAcctno(),
//		                    input.getCrcycd(), E_YES___.YES, E_YES___.NO);
//		    sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易时间：").append(date).
//		        append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易类型：").append("提现").
//		        append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易金额：").append(input.getTranam()+"元").
//		        append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;可用余额：").append(acctbl+"元").
//		        append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;对方账户：").append(input.getInacna()).
//		        append("请点击查看详情。");
//			AppSendMsgInput.setContent(sb.toString());
//			AppSendMsgInput.setSendtime(CommTools.getBaseRunEnvs().getTrxn_date());//消息生成时间
//			AppSendMsgInput.setClickType(cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_CLIKTP.YES);
//			AppSendMsgInput.setClickValue("LOGINURL||/page/electronicAcct/bill/electAcctBill.html");//点击动作值
//			AppSendMsgInput.setTirggerSys(CommTools.getBaseRunEnvs().getSystcd());//触发系统
//			AppSendMsgInput.setTransType(cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_APPTTP.CAPICH);
//			
//			
//			mri.setMsgtyp("ApSmsType.ToAppSendMsg");
//			mri.setMsgobj(AppSendMsgInput); 
//			AsyncMessageUtil.add(mri); 
					
			/**
			 * 短信通知登记
			 */
	    if(property.getToisin()==E_YES___.NO){
	        IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
//          cplKubSqrd.setAppsid();//APP推送ID 
            cplKubSqrd.setCardno(input.getOtcard());//交易卡号  
//          cplKubSqrd.setPmvl01();//参数01    
//          cplKubSqrd.setPmvl02();//参数02    
//          cplKubSqrd.setPmvl03();//参数03    
//          cplKubSqrd.setPmvl04();//参数04    
//          cplKubSqrd.setPmvl05();//参数05    
            cplKubSqrd.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());//内部交易码
            cplKubSqrd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期  
            cplKubSqrd.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());//交易流水  
            cplKubSqrd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());//交易时间  
            IoPbSmsSvcType svcType = SysUtil.getInstance(IoPbSmsSvcType.class);
            svcType.pbTransqReg(cplKubSqrd);
	    }
			
	}
	/**
	 * 交易后处理
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void dealAfter( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Sfcdot.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Sfcdot.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Sfcdot.Output output){
		if(property.getIssucc() == E_YES___.NO){
	        //清算明细登记
	        IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
	        clerinfo.setAcctno(property.getClacno());//账号                                 
	        clerinfo.setAcctna(property.getClacna());//账户名称                             
	        clerinfo.setProdcd(property.getBusino());//产品编号                               
	        clerinfo.setClactp(property.getClactp());//系统内账号类型                                   
	        clerinfo.setAcctbr(property.getBrchno());//账务机构                         
	        clerinfo.setCrcycd(input.getCrcycd());//币种                                             
	        clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.DR);//借贷标志 -借                                        
	        clerinfo.setTranam(input.getTranam());//交易金额                                         
	        clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
	        clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
	        clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次                                       
	        SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
	        
			//清算明细冲正注册
			IoApRegBook cplInput2 = SysUtil.getInstance(IoApRegBook.class);
			cplInput2.setCustac(property.getCustac());
			cplInput2.setEvent1(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			cplInput2.setEvent2(CommTools.getBaseRunEnvs().getTrxn_date());
			cplInput2.setTranev(ApUtil.TRANS_EVENT_CLER);
			//ApStrike.regBook(cplInput2);
    		IoMsRegEvent apinput2 = SysUtil.getInstance(IoMsRegEvent.class);    		
    		apinput2.setReversal_event_id(cplInput2.getTranev());
    		apinput2.setInformation_value(SysUtil.serialize(cplInput2));
    		MsEvent.register(apinput2, true);
			
			//平衡性检查
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),property.getClactp());
			
			output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
			output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
		}
	}
}
