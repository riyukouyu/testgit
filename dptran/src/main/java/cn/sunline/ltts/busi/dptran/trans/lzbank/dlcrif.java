package cn.sunline.ltts.busi.dptran.trans.lzbank;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapotError;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapotErrorDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsError;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsErrorDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblError;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblErrorDao;
import cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Dlcrif.Input;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcctDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccountClearInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CPRSST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DJTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUPSST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOTYPE;



public class dlcrif {
	/**
	 * 差错处理
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void dealDlcrif( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Dlcrif.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Dlcrif.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Dlcrif.Output output){
		String rdtrsq = input.getRdtrsq();//差错读取流水
		String rdtrdt = input.getRdtrdt();//差错读取日期
		String cprsty = input.getCprsty();//对账渠道类型-二代支付，银联，支付前置，超级网银
		E_CPRSST cprsst = input.getCprsst();//对账结果
		BigDecimal tranam = input.getTranam();//交易金额
		
		//非空校验
		if(CommUtil.isNull(rdtrsq)){
			throw DpModuleError.DpstComm.E9999("读取流水不允许为空");
		}
		if(CommUtil.isNull(rdtrdt)){
			throw DpModuleError.DpstComm.E9999("读取日期不允许为空");
		}
		if(CommUtil.isNull(cprsty)){
			throw DpModuleError.DpstComm.E9999("对账渠道类型不允许为空");
		}
		if(CommUtil.isNull(cprsst)){
			throw DpModuleError.DpstComm.E9999("对账结果不允许为空");
		}
		if(CommUtil.isNull(tranam)){
			throw DpModuleError.DpstComm.E9999("交易金额不允许为空");
		}
		
		
		//差错处理
		if(CommUtil.compare("01", cprsty)==0){
			
			dealLsamrs(input);//二代支付差错处理
		}else if(CommUtil.compare("02", cprsty)==0){
			
			dealCupsrs(input);//银联差错处理
		}else if(CommUtil.compare("03", cprsty)==0){
			
			dealSfcdrs(input);//支付前置差错处理
		}else if(CommUtil.compare("04", cprsty)==0){
            
            dealSuprrs(input);//超级网银差错处理
        }else {
			throw DpModuleError.DpstComm.E9999("对账渠道类型不支持");
		}
		
	}
	
	/**
	/**
     * 超级网银差错处理
     * @param input
     */
	private static void dealSuprrs(Input input) {
	    String rdtrsq = input.getRdtrsq();//差错读取流水
        String rdtrdt = input.getRdtrdt();//差错读取日期
        E_CPRSST cprsst = input.getCprsst();//对账结果
        BigDecimal tranam = input.getTranam();//交易金额
        
        String smrycd = BusinessConstants.SUMMARY_AJ;//摘要代码-调整
        /**
         * 1，获取差错信息
         */
        KnlIoblError tblRcInfo = KnlIoblErrorDao.selectOne_odb1(rdtrsq, rdtrdt, false);
        if (CommUtil.isNull(tblRcInfo)){
            throw DpModuleError.DpstComm.E9999("支付前置差错信息不存在");
        }
        if(E_TRANST.SUCCESS == tblRcInfo.getStatus()){
            throw DpModuleError.DpstComm.E9999("该差错已处理完成过");
        }
        
        /**
         * 2,校验交易金额
         */
        if(CommUtil.compare(tranam, tblRcInfo.getTranam())!=0){
            throw DpModuleError.DpstComm.E9999("传入金额与差错信息中金额不符");
        }
        
        /**
         * 3,对账结果校验
         */
        if(cprsst != tblRcInfo.getErroty()){
            throw DpModuleError.DpstComm.E9999("传入对账结果与记录不符");
        }
        
        //判断转入方是内部户还是电子账户
		  E_ACCTROUTTYPE routeType =ApAcctRoutTools.getRouteType(tblRcInfo.getCardno());
	        E_YES___ isflag=E_YES___.NO;
	        if(routeType == E_ACCTROUTTYPE.INSIDE){
	           isflag = E_YES___.YES;//转入方为内部户
	        }else{
	           isflag = E_YES___.NO;//转入方为电子账户
	        }
	        
	    //判断转出方是内部户还是电子账户
		  E_ACCTROUTTYPE routeType1 =ApAcctRoutTools.getRouteType(tblRcInfo.getToacct());
		     E_YES___ isflag1=E_YES___.NO;
		     if(routeType1 == E_ACCTROUTTYPE.INSIDE){
		        isflag1 = E_YES___.YES;//转出方为内部户
		     }else{
		         isflag1 = E_YES___.NO;//转出方为电子账户
		      }  
        
        /**
         * 4,差错处理
         */
        //获取业务代码
        KnpParameter para = SysUtil.getInstance(KnpParameter.class);
        para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "12", "%", true);
        //内部账户类型
        BaseEnumType.E_CLACTP clactp = BaseEnumType.E_CLACTP._36;
   
        if(E_CPRSST.HWD == cprsst){
            //互联网核心多-蓝字冲账 红字冲清算
            if(DpEnumType.E_IOFLAG.IN == tblRcInfo.getIoflag()){
                if(isflag == E_YES___.YES){//转入方为内部户
                	//借-内部户
                    IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
                    GlKnaAcct glKnaAcct = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getCardno(), false);
                    acdrIn1.setAcbrch(tblRcInfo.getBrchno());//机构
                    acdrIn1.setBusino(glKnaAcct.getBusino()); //业务编码
					acdrIn1.setSubsac(glKnaAcct.getSubsac());//子户号
					acdrIn1.setCrcycd(glKnaAcct.getCrcycd());//币种
                    acdrIn1.setTranam(tblRcInfo.getTranam());//交易金额
                    acdrIn1.setToacct(tblRcInfo.getToacct());//对方账号
                    acdrIn1.setToacna("");//对方户名
                    acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
                    acdrIn1.setSmrycd(smrycd);
                    IoInAccount inSrv1 = CommTools.getRemoteInstance(IoInAccount.class);
                    inSrv1.ioInAcdr(acdrIn1);
                }else if( isflag == E_YES___.NO){//转入方为电子账户
                	  //获取电子账户信息
                    KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getCardno(), false);
                    E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
                    KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
                    if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
                        tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
                    }else{ // III类户
                        tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
                    }
                	//借-客户账
                    DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class);
                    DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
                    drawDpAcctIn.setCardno(tblKnaAcdc.getCardno());//卡号
                    drawDpAcctIn.setCustac(tblKnaAcdc.getCustac());//电子账号
                    drawDpAcctIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
                    drawDpAcctIn.setCrcycd(tblRcInfo.getCrcycd());//币种
                    drawDpAcctIn.setTranam(tblRcInfo.getTranam());//交易金额
                    drawDpAcctIn.setToacct(tblRcInfo.getToacct());//对手账号
                    drawDpAcctIn.setOpacna("");//对手户名
                    drawDpAcctIn.setOpbrch(tblRcInfo.getTobrch()); 
                    drawDpAcctIn.setBankcd(tblRcInfo.getTobrch());
                    drawDpAcctIn.setSmrycd(smrycd);
                    dpSrv.addDrawAcctDp(drawDpAcctIn);
                }
                
                
              //贷-待清算
                IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
                acdrIn.setAcbrch(tblRcInfo.getBrchno());//机构
                acdrIn.setBusino(para.getParm_value1()); //业务编码
                acdrIn.setSubsac(para.getParm_value2());//子户号
                acdrIn.setCrcycd(tblRcInfo.getCrcycd());//币种
                acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
                acdrIn.setToacct(tblRcInfo.getToacct());//对方账号
                acdrIn.setToacna("");//对方户名
                acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
                acdrIn.setSmrycd(smrycd);
                IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
                IaTransOutPro outPro = inAcctSer.ioInAccr(acdrIn);
                //平衡性检查
                SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
                
                //清算明细登记 贷 负金额
                IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
                clerinfo.setAcctno(outPro.getAcctno());//账号                                 
                clerinfo.setAcctna(outPro.getAcctna());//账户名称                             
                clerinfo.setProdcd(para.getParm_value1());//产品编号                               
                clerinfo.setClactp(clactp);//系统内账号类型                                                                       
                clerinfo.setAcctbr(tblRcInfo.getBrchno());//账务机构                         
                clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
                clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.CR);//借贷标志                                        
                clerinfo.setTranam(tblRcInfo.getTranam().negate());//交易金额    冲账记负金额                                     
                clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
                clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
                clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
                SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
                
            }else if(DpEnumType.E_IOFLAG.OUT == tblRcInfo.getIoflag()){
                	 //借-待清算
            		
                    IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
                    acdrIn1.setAcbrch(tblRcInfo.getBrchno());//机构
                    acdrIn1.setBusino(para.getParm_value1()); //业务编码
					acdrIn1.setSubsac(para.getParm_value2());//子户号
					acdrIn1.setCrcycd(tblRcInfo.getCrcycd());//币种
                    acdrIn1.setTranam(tblRcInfo.getTranam());//交易金额
                    acdrIn1.setToacct(tblRcInfo.getToacct());//对方账号
                    acdrIn1.setToacna("");//对方户名
                    acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
                    acdrIn1.setSmrycd(smrycd);
                    IoInAccount inSrv1 = CommTools.getRemoteInstance(IoInAccount.class);
                    IaTransOutPro outPro1 = inSrv1.ioInAcdr(acdrIn1);
                    
                    if(isflag1 ==E_YES___.NO){//转出方为电子账户
                    	//获取电子账户信息
                        KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getToacct(), false);
                        E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
                        KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
                        if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
                            tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
                        }else{ // III类户
                            tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
                        }
                        //贷-客户账
                        DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class); 
                        SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class); 
                        saveIn.setCardno(tblKnaAcdc.getCardno());//卡号
                        saveIn.setCustac(tblKnaAcdc.getCustac());//电子账号
                        saveIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
                        saveIn.setCrcycd(tblKnaAcct.getCrcycd());//币种
                        saveIn.setTranam(tblRcInfo.getTranam());//交易金额
                        saveIn.setToacct(tblRcInfo.getToacct());//对方账号
                        saveIn.setOpacna("");//对方户名
                        saveIn.setSmrycd(smrycd);
                        dpSrv.addPostAcctDp(saveIn);
                    }else if(isflag1 ==E_YES___.YES){//转出方为内部户
                    	//贷-内部户
    					GlKnaAcct glKnaAcct1 = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getToacct(), false);
    					IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
    					acdrIn.setAcbrch(tblRcInfo.getBrchno());//机构
    					acdrIn.setBusino(glKnaAcct1.getBusino()); //业务编码
    					acdrIn.setSubsac(glKnaAcct1.getSubsac());//子户号
    					acdrIn.setCrcycd(glKnaAcct1.getCrcycd());//币种
    					acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
    					acdrIn.setToacct(tblRcInfo.getToacct());//对方账号
    					acdrIn.setToacna("");//对方户名
    					acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
    					acdrIn.setSmrycd(smrycd);
    					IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
    				    inAcctSer.ioInAccr(acdrIn);
                    }
                    
                    //平衡性检查
                    SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
                    
                    //清算明细登记 借 负金额
                    IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
                    clerinfo.setAcctno(outPro1.getAcctno());//账号                                 
                    clerinfo.setAcctna(outPro1.getAcctna());//账户名称                             
                    clerinfo.setProdcd(para.getParm_value1());//产品编号                               
                    clerinfo.setClactp(clactp);//系统内账号类型                                                                       
                    clerinfo.setAcctbr(tblRcInfo.getBrchno());//账务机构                         
                    clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
                    clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.DR);//借贷标志                                        
                    clerinfo.setTranam(tblRcInfo.getTranam().negate());//交易金额    冲账记负金额                                     
                    clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
                    clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
                    clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
                    SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
            }else{
                throw DpModuleError.DpstComm.E9999("出入金标志不支持");
            }
            
        }else if(E_CPRSST.HWS == cprsst){
            //互联网核心少-补账
            if(DpEnumType.E_IOFLAG.IN == tblRcInfo.getIoflag()){
                //借-待清算
                IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
                acdrIn1.setAcbrch(tblRcInfo.getBrchno());//机构
                acdrIn1.setBusino(para.getParm_value1()); //业务编码
                acdrIn1.setSubsac(para.getParm_value2());//子户号
                acdrIn1.setCrcycd(tblRcInfo.getCrcycd());//币种
                acdrIn1.setTranam(tblRcInfo.getTranam());//交易金额
                acdrIn1.setToacct(tblRcInfo.getToacct());//对方账号
                acdrIn1.setToacna("");//对方户名
                acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
                acdrIn1.setSmrycd(smrycd);
                IoInAccount inSrv1 = CommTools.getRemoteInstance(IoInAccount.class);
                IaTransOutPro outPro1 = inSrv1.ioInAcdr(acdrIn1);
                
                if(isflag == E_YES___.NO){
                	 //获取电子账户信息
                    KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getCardno(), false);
                    E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
                    KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
                    if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
                        tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
                    }else{ // III类户
                        tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
                    }
                	//贷-客户账
                    DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class);
                    SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class);
                    saveIn.setCardno(tblKnaAcdc.getCardno());//卡号
                    saveIn.setCustac(tblKnaAcdc.getCustac());//电子账号
                    saveIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
                    saveIn.setCrcycd(tblKnaAcct.getCrcycd());//币种
                    saveIn.setTranam(tblRcInfo.getTranam());//交易金额
                    saveIn.setToacct(tblRcInfo.getToacct());//对方账号
                    saveIn.setOpacna("");//对方户名
                    saveIn.setSmrycd(smrycd);
                    dpSrv.addPostAcctDp(saveIn);
                }else if(isflag == E_YES___.YES){
                	//贷-内部户
					GlKnaAcct glKnaAcct2 = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getCardno(), false);
					IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
					acdrIn.setAcbrch(tblRcInfo.getBrchno());//机构
					acdrIn.setBusino(glKnaAcct2.getBusino()); //业务编码
					acdrIn.setSubsac(glKnaAcct2.getSubsac());//子户号
					acdrIn.setCrcycd(glKnaAcct2.getCrcycd());//币种
					acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
					acdrIn.setToacct(tblRcInfo.getToacct());//对方账号
					acdrIn.setToacna("");//对方户名
					acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
					acdrIn.setSmrycd(smrycd);
					IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
				    inAcctSer.ioInAccr(acdrIn);
                }
                
                //平衡性检查
                SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
                
                //登记清算明细
                IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
                clerinfo.setAcctno(outPro1.getAcctno());//账号                                 
                clerinfo.setAcctna(outPro1.getAcctna());//账户名称                             
                clerinfo.setProdcd(para.getParm_value1());//产品编号                               
                clerinfo.setClactp(clactp);//系统内账号类型                                                                       
                clerinfo.setAcctbr(tblRcInfo.getBrchno());//账务机构                         
                clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
                clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.CR);//借贷标志-贷                                         
                clerinfo.setTranam(tblRcInfo.getTranam());//交易金额                                         
                clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
                clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
                clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
                SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
                
            }else if(DpEnumType.E_IOFLAG.OUT == tblRcInfo.getIoflag()){
            	if(isflag1 ==E_YES___.NO){
            		//获取电子账户信息
                    KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getToacct(), false);
                    E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
                    KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
                    if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
                        tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
                    }else{ // III类户
                        tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
                    }
                    //借-客户账
                    DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class);
                    DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
                    drawDpAcctIn.setCardno(tblKnaAcdc.getCardno());//卡号
                    drawDpAcctIn.setCustac(tblKnaAcdc.getCustac());//电子账号
                    drawDpAcctIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
                    drawDpAcctIn.setCrcycd(tblRcInfo.getCrcycd());//币种
                    drawDpAcctIn.setTranam(tblRcInfo.getTranam());//交易金额
                    drawDpAcctIn.setToacct(tblRcInfo.getCardno());//对手账号
                    drawDpAcctIn.setOpacna("");//对手户名
                    drawDpAcctIn.setOpbrch(tblRcInfo.getBrchno()); 
                    drawDpAcctIn.setBankcd(tblRcInfo.getBrchno());
                    drawDpAcctIn.setSmrycd(smrycd);
                    dpSrv.addDrawAcctDp(drawDpAcctIn);
            	}else if(isflag1 ==E_YES___.YES){
            		//借-内部户
                    IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
                    GlKnaAcct glKnaAcct = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getToacct(), false);
                    acdrIn1.setAcbrch(tblRcInfo.getBrchno());//机构
                    acdrIn1.setBusino(glKnaAcct.getBusino()); //业务编码
					acdrIn1.setSubsac(glKnaAcct.getSubsac());//子户号
					acdrIn1.setCrcycd(glKnaAcct.getCrcycd());//币种
                    acdrIn1.setTranam(tblRcInfo.getTranam());//交易金额
                    acdrIn1.setToacct(tblRcInfo.getToacct());//对方账号
                    acdrIn1.setToacna("");//对方户名
                    acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
                    acdrIn1.setSmrycd(smrycd);
                    IoInAccount inSrv1 = CommTools.getRemoteInstance(IoInAccount.class);
                    inSrv1.ioInAcdr(acdrIn1);
            	}
                
                
                //贷-待清算
                IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
                acdrIn.setAcbrch(tblRcInfo.getBrchno());//机构
                acdrIn.setBusino(para.getParm_value1()); //业务编码
                acdrIn.setSubsac(para.getParm_value2());//子户号
                acdrIn.setCrcycd(tblRcInfo.getCrcycd());//币种
                acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
                acdrIn.setToacct(tblRcInfo.getCardno());//对方账号
                acdrIn.setToacna("");//对方户名
                acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
                acdrIn.setSmrycd(smrycd);
                IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
                IaTransOutPro outPro = inAcctSer.ioInAccr(acdrIn);
                //平衡性检查
                SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
                
                //清算明细登记 借
                IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
                clerinfo.setAcctno(outPro.getAcctno());//账号                                 
                clerinfo.setAcctna(outPro.getAcctna());//账户名称                             
                clerinfo.setProdcd(para.getParm_value1());//产品编号                               
                clerinfo.setClactp(clactp);//系统内账号类型                                                                       
                clerinfo.setAcctbr(tblRcInfo.getBrchno());//账务机构                         
                clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
                clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.DR);//借贷标志                                         
                clerinfo.setTranam(tblRcInfo.getTranam());//交易金额                             
                clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
                clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
                clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
                SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
            }else{
                throw DpModuleError.DpstComm.E9999("出入金标志不支持");
            }
            
        }else{
            throw DpModuleError.DpstComm.E9999("不支持处理类型，请手工处理");
        }
        
        /**
         * 5,差错信息状态更新
         */
        tblRcInfo.setStatus(E_TRANST.SUCCESS);//9-处理完成
        KnlIoblErrorDao.updateOne_odb1(tblRcInfo);
        
    }
	

    /**
	 * 支付前置差错处理
	 * @param input
	 */
	private static void dealSfcdrs(Input input) {
		String rdtrsq = input.getRdtrsq();//差错读取流水
		String rdtrdt = input.getRdtrdt();//差错读取日期
		E_CPRSST cprsst = input.getCprsst();//对账结果
		BigDecimal tranam = input.getTranam();//交易金额
		
		String smrycd = BusinessConstants.SUMMARY_AJ;//摘要代码-调整
		/**
		 * 1，获取差错信息
		 */
		KnlIoblError tblRcInfo = KnlIoblErrorDao.selectOne_odb1(rdtrsq, rdtrdt, false);
		if (CommUtil.isNull(tblRcInfo)){
			throw DpModuleError.DpstComm.E9999("支付前置差错信息不存在");
		}
		if(E_TRANST.SUCCESS == tblRcInfo.getStatus()){
			throw DpModuleError.DpstComm.E9999("该差错已处理完成过");
		}
		
		/**
		 * 2,校验交易金额
		 */
		if(CommUtil.compare(tranam, tblRcInfo.getTranam())!=0){
			throw DpModuleError.DpstComm.E9999("传入金额与差错信息中金额不符");
		}
		
		/**
		 * 3,对账结果校验
		 */
		if(cprsst != tblRcInfo.getErroty()){
			throw DpModuleError.DpstComm.E9999("传入对账结果与记录不符");
		}
		
		//判断转入方是内部户还是电子账户
		  E_ACCTROUTTYPE routeType =ApAcctRoutTools.getRouteType(tblRcInfo.getCardno());
	        E_YES___ isflag=E_YES___.NO;
	        if(routeType == E_ACCTROUTTYPE.INSIDE){
	           isflag = E_YES___.YES;//转入方为内部户
	        }else{
	           isflag = E_YES___.NO;//转入方为电子账户
	        }
	        
	    //判断转出方是内部户还是电子账户
		  E_ACCTROUTTYPE routeType1 =ApAcctRoutTools.getRouteType(tblRcInfo.getToacct());
		     E_YES___ isflag1=E_YES___.NO;
		     if(routeType1 == E_ACCTROUTTYPE.INSIDE){
		        isflag1 = E_YES___.YES;//转出方为内部户
		     }else{
		         isflag1 = E_YES___.NO;//转出方为电子账户
		      }    
		
		/**
		 * 4,差错处理
		 */
		//获取业务代码
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "06", "%", true);
		//内部账户类型
		BaseEnumType.E_CLACTP clactp = BaseEnumType.E_CLACTP._33;
		if(E_CPRSST.HWD == cprsst){
			//互联网核心多-蓝字冲账 红字冲清算
			if(DpEnumType.E_IOFLAG.IN == tblRcInfo.getIoflag()){
				if(isflag ==E_YES___.NO){//转入方为电子账户
					//获取电子账户信息
					KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getCardno(), false);
					E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
					KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
					if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
					}else{ // III类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
					}
					//借-客户账
					DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class);
					DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
					drawDpAcctIn.setCardno(tblKnaAcdc.getCardno());//卡号
					drawDpAcctIn.setCustac(tblKnaAcdc.getCustac());//电子账号
					drawDpAcctIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
					drawDpAcctIn.setCrcycd(tblRcInfo.getCrcycd());//币种
					drawDpAcctIn.setTranam(tblRcInfo.getTranam());//交易金额
					drawDpAcctIn.setToacct(tblRcInfo.getToacct());//对手账号
			        drawDpAcctIn.setOpacna("");//对手户名
			        drawDpAcctIn.setOpbrch(tblRcInfo.getTobrch()); 
			        drawDpAcctIn.setBankcd(tblRcInfo.getTobrch());
			        drawDpAcctIn.setSmrycd(smrycd);
			        dpSrv.addDrawAcctDp(drawDpAcctIn);
				}else if(isflag ==E_YES___.YES){//转入方为内部户
					//借-内部户
					GlKnaAcct glKnaAcct = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getCardno(), false);
					IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
					acdrIn.setAcbrch(tblRcInfo.getBrchno());//机构
					acdrIn.setBusino(glKnaAcct.getBusino()); //业务编码
					acdrIn.setSubsac(glKnaAcct.getSubsac());//子户号
					acdrIn.setCrcycd(glKnaAcct.getCrcycd());//币种
					acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
					acdrIn.setToacct(tblRcInfo.getToacct());//对方账号
					acdrIn.setToacna("");//对方户名
					acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
					acdrIn.setSmrycd(smrycd);
					IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
					inAcctSer.ioInAcdr(acdrIn);
				}
			
		        //贷-待清算
				IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
				acdrIn.setAcbrch(tblRcInfo.getBrchno());//机构
				acdrIn.setBusino(para.getParm_value1()); //业务编码
				acdrIn.setSubsac(para.getParm_value2());//子户号
				acdrIn.setCrcycd(tblRcInfo.getCrcycd());//币种
				acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
				acdrIn.setToacct(tblRcInfo.getToacct());//对方账号
				acdrIn.setToacna("");//对方户名
				acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
				acdrIn.setSmrycd(smrycd);
				IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
				IaTransOutPro outPro = inAcctSer.ioInAccr(acdrIn);
				//平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
				
				//清算明细登记 贷 负金额
				IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
                clerinfo.setAcctno(outPro.getAcctno());//账号                                 
                clerinfo.setAcctna(outPro.getAcctna());//账户名称                             
                clerinfo.setProdcd(para.getParm_value1());//产品编号                               
                clerinfo.setClactp(clactp);//系统内账号类型                                                                       
                clerinfo.setAcctbr(tblRcInfo.getBrchno());//账务机构                         
                clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
                clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.CR);//借贷标志                                        
                clerinfo.setTranam(tblRcInfo.getTranam().negate());//交易金额    冲账记负金额                                     
                clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
                clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
                clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
                SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
                
			}else if(DpEnumType.E_IOFLAG.OUT == tblRcInfo.getIoflag()){
				//借-待清算
				IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
				acdrIn1.setAcbrch(tblRcInfo.getBrchno());//机构
				acdrIn1.setBusino(para.getParm_value1()); //业务编码
				acdrIn1.setSubsac(para.getParm_value2());//子户号
				acdrIn1.setCrcycd(tblRcInfo.getCrcycd());//币种
				acdrIn1.setTranam(tblRcInfo.getTranam());//交易金额
				acdrIn1.setToacct(tblRcInfo.getToacct());//对方账号
				acdrIn1.setToacna("");//对方户名
				acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
				acdrIn1.setSmrycd(smrycd);
				IoInAccount inSrv1 = CommTools.getRemoteInstance(IoInAccount.class);
				IaTransOutPro outPro1 = inSrv1.ioInAcdr(acdrIn1);
				
				if(isflag1 ==E_YES___.NO){
					//获取电子账户信息
					KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getToacct(), false);
					E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
					KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
					if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
					}else{ // III类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
					}
					//贷-客户账
					DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class); 
					SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class); 
					saveIn.setCardno(tblKnaAcdc.getCardno());//卡号
					saveIn.setCustac(tblKnaAcdc.getCustac());//电子账号
					saveIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
					saveIn.setCrcycd(tblKnaAcct.getCrcycd());//币种
					saveIn.setTranam(tblRcInfo.getTranam());//交易金额
					saveIn.setToacct(tblRcInfo.getToacct());//对方账号
					saveIn.setOpacna("");//对方户名
					saveIn.setSmrycd(smrycd);
					dpSrv.addPostAcctDp(saveIn);
				}else if(isflag1 ==E_YES___.YES){
					//贷-内部户
					GlKnaAcct glKnaAcct = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getToacct(), false);
					IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
					acdrIn.setAcbrch(tblRcInfo.getBrchno());//机构
					acdrIn.setBusino(glKnaAcct.getBusino()); //业务编码
					acdrIn.setSubsac(glKnaAcct.getSubsac());//子户号
					acdrIn.setCrcycd(glKnaAcct.getCrcycd());//币种
					acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
					acdrIn.setToacct(tblRcInfo.getToacct());//对方账号
					acdrIn.setToacna("");//对方户名
					acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
					acdrIn.setSmrycd(smrycd);
					IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
				    inAcctSer.ioInAccr(acdrIn);
				}
				
				//平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
				
				//清算明细登记 借 负金额
				IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
                clerinfo.setAcctno(outPro1.getAcctno());//账号                                 
                clerinfo.setAcctna(outPro1.getAcctna());//账户名称                             
                clerinfo.setProdcd(para.getParm_value1());//产品编号                               
                clerinfo.setClactp(clactp);//系统内账号类型                                                                       
                clerinfo.setAcctbr(tblRcInfo.getBrchno());//账务机构                         
                clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
                clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.DR);//借贷标志                                        
                clerinfo.setTranam(tblRcInfo.getTranam().negate());//交易金额    冲账记负金额                                     
                clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
                clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
                clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
                SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
                
			}else{
				throw DpModuleError.DpstComm.E9999("出入金标志不支持");
			}
			
		}else if(E_CPRSST.HWS == cprsst){
			//互联网核心少-补账
			if(DpEnumType.E_IOFLAG.IN == tblRcInfo.getIoflag()){
				GlKnaAcct glKnaAcct = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getCardno(), false);
				//借-待清算
				IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
				acdrIn1.setAcbrch(tblRcInfo.getBrchno());//机构
				acdrIn1.setBusino(glKnaAcct.getBusino()); //业务编码
				acdrIn1.setSubsac(glKnaAcct.getSubsac());//子户号
				acdrIn1.setCrcycd(glKnaAcct.getCrcycd());//币种
				acdrIn1.setTranam(tblRcInfo.getTranam());//交易金额
				acdrIn1.setToacct(tblRcInfo.getToacct());//对方账号
				acdrIn1.setToacna("");//对方户名
				acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
				acdrIn1.setSmrycd(smrycd);
				IoInAccount inSrv1 = CommTools.getRemoteInstance(IoInAccount.class);
				IaTransOutPro outPro1 = inSrv1.ioInAcdr(acdrIn1);
				
				if(isflag ==E_YES___.NO){//转出方为电子账户
					//获取电子账户信息
					KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getCardno(), false);
					E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
					KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
					if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
					}else{ // III类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
					}
					//贷-客户账
					DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class);
					SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class);
					saveIn.setCardno(tblKnaAcdc.getCardno());//卡号
					saveIn.setCustac(tblKnaAcdc.getCustac());//电子账号
					saveIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
					saveIn.setCrcycd(tblKnaAcct.getCrcycd());//币种
					saveIn.setTranam(tblRcInfo.getTranam());//交易金额
					saveIn.setToacct(tblRcInfo.getToacct());//对方账号
					saveIn.setOpacna("");//对方户名
					saveIn.setSmrycd(smrycd);
					dpSrv.addPostAcctDp(saveIn);
				}else if(isflag ==E_YES___.YES){//转入方为内部户
					//贷-内部户
					GlKnaAcct glKnaAcct1 = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getCardno(), false);
					IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
					acdrIn.setAcbrch(tblRcInfo.getBrchno());//机构
					acdrIn.setBusino(glKnaAcct1.getBusino()); //业务编码
					acdrIn.setSubsac(glKnaAcct1.getSubsac());//子户号
					acdrIn.setCrcycd(glKnaAcct1.getCrcycd());//币种
					acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
					acdrIn.setToacct(tblRcInfo.getToacct());//对方账号
					acdrIn.setToacna("");//对方户名
					acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
					acdrIn.setSmrycd(smrycd);
					IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
					inAcctSer.ioInAccr(acdrIn);
					
				}
				
				//平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
				
				//登记清算明细
				IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
                clerinfo.setAcctno(outPro1.getAcctno());//账号                                 
                clerinfo.setAcctna(outPro1.getAcctna());//账户名称                             
                clerinfo.setProdcd(para.getParm_value1());//产品编号                               
                clerinfo.setClactp(clactp);//系统内账号类型                                                                       
                clerinfo.setAcctbr(tblRcInfo.getBrchno());//账务机构                         
                clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
                clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.CR);//借贷标志-贷                                         
                clerinfo.setTranam(tblRcInfo.getTranam());//交易金额                                         
                clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
                clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
                clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
                SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
				
			}else if(DpEnumType.E_IOFLAG.OUT == tblRcInfo.getIoflag()){
				if(isflag1 ==E_YES___.NO){//转出方为电子账户
					//获取电子账户信息
					KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getToacct(), false);
					E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
					KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
					if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
					}else{ // III类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
					}
					//借-客户账
					DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class);
					DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
					drawDpAcctIn.setCardno(tblKnaAcdc.getCardno());//卡号
					drawDpAcctIn.setCustac(tblKnaAcdc.getCustac());//电子账号
					drawDpAcctIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
					drawDpAcctIn.setCrcycd(tblRcInfo.getCrcycd());//币种
					drawDpAcctIn.setTranam(tblRcInfo.getTranam());//交易金额
					drawDpAcctIn.setToacct(tblRcInfo.getCardno());//对手账号
			        drawDpAcctIn.setOpacna("");//对手户名
			        drawDpAcctIn.setOpbrch(tblRcInfo.getBrchno()); 
			        drawDpAcctIn.setBankcd(tblRcInfo.getBrchno());
			        drawDpAcctIn.setSmrycd(smrycd);
			        dpSrv.addDrawAcctDp(drawDpAcctIn);
				}else if(isflag1 ==E_YES___.YES){//转出方为内部户
					//借-内部户
					GlKnaAcct glKnaAcct = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getToacct(), false);
					IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
					acdrIn.setAcbrch(tblRcInfo.getBrchno());//机构
					acdrIn.setBusino(glKnaAcct.getBusino()); //业务编码
					acdrIn.setSubsac(glKnaAcct.getSubsac());//子户号
					acdrIn.setCrcycd(glKnaAcct.getCrcycd());//币种
					acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
					acdrIn.setToacct(tblRcInfo.getToacct());//对方账号
					acdrIn.setToacna("");//对方户名
					acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
					acdrIn.setSmrycd(smrycd);
					IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
					inAcctSer.ioInAcdr(acdrIn);
				}
				
				//贷-待清算
				IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
				acdrIn.setAcbrch(tblRcInfo.getBrchno());//机构
				acdrIn.setBusino(para.getParm_value1()); //业务编码
				acdrIn.setSubsac(para.getParm_value2());//子户号
				acdrIn.setCrcycd(tblRcInfo.getCrcycd());//币种
				acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
				acdrIn.setToacct(tblRcInfo.getCardno());//对方账号
				acdrIn.setToacna("");//对方户名
				acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
				acdrIn.setSmrycd(smrycd);
				IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
				IaTransOutPro outPro = inAcctSer.ioInAccr(acdrIn);
				//平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
				
				//清算明细登记 借
				IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
                clerinfo.setAcctno(outPro.getAcctno());//账号                                 
                clerinfo.setAcctna(outPro.getAcctna());//账户名称                             
                clerinfo.setProdcd(para.getParm_value1());//产品编号                               
                clerinfo.setClactp(clactp);//系统内账号类型                                                                       
                clerinfo.setAcctbr(tblRcInfo.getBrchno());//账务机构                         
                clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
                clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.DR);//借贷标志                                         
                clerinfo.setTranam(tblRcInfo.getTranam());//交易金额                             
                clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
                clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
                clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
                SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
			}else{
				throw DpModuleError.DpstComm.E9999("出入金标志不支持");
			}
			
		}else{
			throw DpModuleError.DpstComm.E9999("不支持处理类型，请手工处理");
		}
		
		/**
		 * 5,差错信息状态更新
		 */
		tblRcInfo.setStatus(E_TRANST.SUCCESS);//9-处理完成
		KnlIoblErrorDao.updateOne_odb1(tblRcInfo);
	}

	/**
	 * 银联差错处理
	 * @param input
	 */
	private static void dealCupsrs(Input input) {
		String rdtrsq = input.getRdtrsq();//差错读取流水
		String rdtrdt = input.getRdtrdt();//差错读取日期
		E_CPRSST cprsst = input.getCprsst();//对账结果
		BigDecimal tranam = input.getTranam();//交易金额
		
		String smrycd = BusinessConstants.SUMMARY_AJ;//摘要代码-调整
		
		/**
		 * 1，获取差错信息
		 */
		KnlIoblCupsError tblRcInfo = KnlIoblCupsErrorDao.selectOne_odb1(rdtrsq, rdtrdt, false);
		if(CommUtil.isNull(tblRcInfo)){
			throw DpModuleError.DpstComm.E9999("银联差错信息不存在");
		}
		if(E_CUPSST.CLWC == tblRcInfo.getTranst()){
			throw DpModuleError.DpstComm.E9999("该差错已处理完成过");
		}
		
		/**
		 * 2,校验交易金额
		 */
		if(CommUtil.compare(tranam, tblRcInfo.getTranam())!=0){
			throw DpModuleError.DpstComm.E9999("传入金额与差错信息中金额不符");
		}
		
		/**
		 * 3,对账结果校验
		 */
		if(cprsst != tblRcInfo.getErroty()){
			throw DpModuleError.DpstComm.E9999("传入对账结果与记录不符");
		}
		
		//判断转入方是内部户还是电子账户
		  E_ACCTROUTTYPE routeType =ApAcctRoutTools.getRouteType(tblRcInfo.getInacct());
	        E_YES___ isflag=E_YES___.NO;
	        if(routeType == E_ACCTROUTTYPE.INSIDE){
	           isflag = E_YES___.YES;//转入方为内部户
	        }else{
	           isflag = E_YES___.NO;//转入方为电子账户
	        }
	        
	    //判断转出方是内部户还是电子账户
		  E_ACCTROUTTYPE routeType1 =ApAcctRoutTools.getRouteType(tblRcInfo.getCardno());
		     E_YES___ isflag1=E_YES___.NO;
		     if(routeType1 == E_ACCTROUTTYPE.INSIDE){
		        isflag1 = E_YES___.YES;//转出方为内部户
		     }else{
		         isflag1 = E_YES___.NO;//转出方为电子账户
		      } 
		
		/**
		 * 3,差错处理
		 */
		//获取业务代码，
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		E_CLACTP clactp=null;//内部账户类型
		if(E_DJTYPE.YLWKZZ == tblRcInfo.getDjtype()){
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "07", "%", true);
			clactp=E_CLACTP._34;
		}else if(E_DJTYPE.CUPSZZ == tblRcInfo.getDjtype()){
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "09", "%", true);
			clactp=E_CLACTP._35;
		}else if(E_DJTYPE.ALCHZZ == tblRcInfo.getDjtype()){
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "14", "%", true);
			clactp=E_CLACTP._37;
		}else{
			throw DpModuleError.DpstComm.E9999("银联业务代码参数未配置");
		}
		
		if(E_CPRSST.HWD == cprsst){
			//互联网核心多-蓝字冲账 红字冲清算
			if(E_AMNTCD.CR == tblRcInfo.getAmntcd()){
				if(isflag == E_YES___.NO){//转入方为电子账户
					//获取电子账户信息
					KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getCardno(), false);
					E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
					KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
					if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
					}else{ // III类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
					}
									
					//来账冲账 借-客户账
					DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class);
					DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
					drawDpAcctIn.setCardno(tblKnaAcdc.getCardno());//卡号
					drawDpAcctIn.setCustac(tblKnaAcdc.getCustac());//电子账号
					drawDpAcctIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
					drawDpAcctIn.setCrcycd(tblRcInfo.getCrcycd());//币种
					drawDpAcctIn.setTranam(tblRcInfo.getTranam());//交易金额
					drawDpAcctIn.setToacct(tblRcInfo.getOtacct());//对手账号
			        drawDpAcctIn.setOpacna(tblRcInfo.getOtacna());//对手户名
			        drawDpAcctIn.setOpbrch(tblRcInfo.getOtbrch()); 
			        drawDpAcctIn.setSmrycd(smrycd);
			        dpSrv.addDrawAcctDp(drawDpAcctIn);
				}else if(isflag == E_YES___.YES){//转入方为内部户
					GlKnaAcct glKnaAcct = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getCardno(), false);
					IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
					acdrIn1.setAcbrch(tblRcInfo.getTrbrch());//机构
					acdrIn1.setBusino(glKnaAcct.getBusino()); //业务编码
					acdrIn1.setSubsac(glKnaAcct.getSubsac());//子户号
					acdrIn1.setCrcycd(glKnaAcct.getCrcycd());//币种
					acdrIn1.setTranam(tblRcInfo.getTranam());//交易金额
					acdrIn1.setToacct(tblRcInfo.getInacct());//对方账号
					acdrIn1.setToacna(tblRcInfo.getInacna());//对方户名
					acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
					acdrIn1.setSmrycd(smrycd);
					IoInAccount inSrv1 = CommTools.getRemoteInstance(IoInAccount.class);
					inSrv1.ioInAcdr(acdrIn1);
				}
				//贷-待清算
				IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
				acdrIn.setAcbrch(tblRcInfo.getTrbrch());//机构
				acdrIn.setBusino(para.getParm_value1()); //业务编码
				acdrIn.setSubsac(para.getParm_value2());//子户号
				acdrIn.setCrcycd(tblRcInfo.getCrcycd());//币种
				acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
				acdrIn.setToacct(tblRcInfo.getOtacct());//对方账号
				acdrIn.setToacna(tblRcInfo.getOtacna());//对方户名
				acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
				acdrIn.setSmrycd(smrycd);
				IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
				IaTransOutPro outPro = inAcctSer.ioInAccr(acdrIn);
				//平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
				
				//清算明细登记 
				IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
                clerinfo.setAcctno(outPro.getAcctno());//账号                                 
                clerinfo.setAcctna(outPro.getAcctna());//账户名称                             
                clerinfo.setProdcd(para.getParm_value1());//产品编号                               
                clerinfo.setClactp(clactp);//系统内账号类型                                                                       
                clerinfo.setAcctbr(tblRcInfo.getTrbrch());//账务机构                         
                clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
                clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.CR);//借贷标志                                         
                clerinfo.setTranam(tblRcInfo.getTranam().negate());//交易金额   冲账用负金额                          
                clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
                clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
                clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
                SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
				
				
			}else if(E_AMNTCD.DR == tblRcInfo.getAmntcd()){
				//往账冲账 借-待清算	
				
				IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
				acdrIn1.setAcbrch(tblRcInfo.getTrbrch());//机构
				acdrIn1.setBusino(para.getParm_value1()); //业务编码
				acdrIn1.setSubsac(para.getParm_value2());//子户号
				acdrIn1.setCrcycd(tblRcInfo.getCrcycd());//币种
				acdrIn1.setTranam(tblRcInfo.getTranam());//交易金额
				acdrIn1.setToacct(tblRcInfo.getInacct());//对方账号
				acdrIn1.setToacna(tblRcInfo.getInacna());//对方户名
				acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
				acdrIn1.setSmrycd(smrycd);
				IoInAccount inSrv1 = CommTools.getRemoteInstance(IoInAccount.class);
				IaTransOutPro outPro1 = inSrv1.ioInAcdr(acdrIn1);
				
				if(isflag1== E_YES___.NO){//转出方为电子账户
					//获取电子账户信息
					KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getCardno(), false);
					E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
					KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
					if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
					}else{ // III类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
					}
					
					//贷-客户账
					DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class);
					SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class);
					saveIn.setCardno(tblKnaAcdc.getCardno());//卡号
					saveIn.setCustac(tblKnaAcdc.getCustac());//电子账号
					saveIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
					saveIn.setCrcycd(tblKnaAcct.getCrcycd());//币种
					saveIn.setTranam(tblRcInfo.getTranam());//交易金额
					saveIn.setToacct(tblRcInfo.getInacct());//对方账号
					saveIn.setOpacna(tblRcInfo.getInacna());//对方户名
					saveIn.setOpbrch(tblRcInfo.getInbrch());//对方机构
					saveIn.setSmrycd(smrycd);
					dpSrv.addPostAcctDp(saveIn);
				}else if(isflag1 == E_YES___.YES){//转出方内部户
					//贷-内部户
					GlKnaAcct glKnaAcct1 = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getCardno(), false);
					IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
					acdrIn.setAcbrch(tblRcInfo.getTrbrch());//机构
					acdrIn.setBusino(glKnaAcct1.getBusino()); //业务编码
					acdrIn.setSubsac(glKnaAcct1.getSubsac());//子户号
					acdrIn.setCrcycd(glKnaAcct1.getCrcycd());//币种
					acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
					acdrIn.setToacct(tblRcInfo.getInacct());//对方账号
					acdrIn.setToacna(tblRcInfo.getInacna());//对方户名
					acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
					acdrIn.setSmrycd(smrycd);
					IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
					inAcctSer.ioInAccr(acdrIn);
				}
				
				//平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
				
				//登记清算明细
				IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
                clerinfo.setAcctno(outPro1.getAcctno());//账号                                 
                clerinfo.setAcctna(outPro1.getAcctna());//账户名称                             
                clerinfo.setProdcd(para.getParm_value1());//产品编号                               
                clerinfo.setClactp(clactp);//系统内账号类型                                                                       
                clerinfo.setAcctbr(tblRcInfo.getTrbrch());//账务机构                         
                clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
                clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.DR);//借贷标志                                         
                clerinfo.setTranam(tblRcInfo.getTranam().negate());//交易金额     冲正用负金额                                    
                clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
                clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
                clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
                SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
				
			}else{
				throw DpModuleError.DpstComm.E9999("借贷标志不支持");
			}
			
		}else if(E_CPRSST.HWS == cprsst){
			//互联网核心少-补账
			if(E_AMNTCD.CR == tblRcInfo.getAmntcd()){
				//来账 借-待清算
				IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
				acdrIn1.setAcbrch(tblRcInfo.getTrbrch());//机构
				acdrIn1.setBusino(para.getParm_value1()); //业务编码
				acdrIn1.setSubsac(para.getParm_value2());//子户号
				acdrIn1.setCrcycd(tblRcInfo.getCrcycd());//币种
				acdrIn1.setTranam(tblRcInfo.getTranam());//交易金额
				acdrIn1.setToacct(tblRcInfo.getOtacct());//对方账号
				acdrIn1.setToacna(tblRcInfo.getOtacna());//对方户名
				acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
				acdrIn1.setSmrycd(smrycd);
				IoInAccount inSrv1 = CommTools.getRemoteInstance(IoInAccount.class);
				IaTransOutPro outPro1 = inSrv1.ioInAcdr(acdrIn1);
				
				if(isflag == E_YES___.NO){//转入方为电子账户
					//获取电子账户信息
					KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getCardno(), false);
					E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
					KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
					if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
					}else{ // III类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
					}
					//贷-客户账
					DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class);
					SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class);
					saveIn.setCardno(tblKnaAcdc.getCardno());//卡号
					saveIn.setCustac(tblKnaAcdc.getCustac());//电子账号
					saveIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
					saveIn.setCrcycd(tblKnaAcct.getCrcycd());//币种
					saveIn.setTranam(tblRcInfo.getTranam());//交易金额
					saveIn.setToacct(tblRcInfo.getOtacct());//对方账号
					saveIn.setOpacna(tblRcInfo.getOtacna());//对方户名
					saveIn.setSmrycd(smrycd);
					dpSrv.addPostAcctDp(saveIn);
					
				}else if(isflag == E_YES___.YES){//转入方为内部户
					GlKnaAcct glKnaAcct1 = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getCardno(), false);
					IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
					acdrIn.setAcbrch(tblRcInfo.getTrbrch());//机构
					acdrIn.setBusino(glKnaAcct1.getBusino()); //业务编码
					acdrIn.setSubsac(glKnaAcct1.getSubsac());//子户号
					acdrIn.setCrcycd(glKnaAcct1.getCrcycd());//币种
					acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
					acdrIn.setToacct(tblRcInfo.getOtacct());//对方账号
					acdrIn.setToacna(tblRcInfo.getOtacna());//对方户名
					acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
					acdrIn.setSmrycd(smrycd);
					IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
					inAcctSer.ioInAccr(acdrIn);
				}
				
				//平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
				
				//登记清算明细
				IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
                clerinfo.setAcctno(outPro1.getAcctno());//账号                                 
                clerinfo.setAcctna(outPro1.getAcctna());//账户名称                             
                clerinfo.setProdcd(para.getParm_value1());//产品编号                               
                clerinfo.setClactp(clactp);//系统内账号类型                                                                       
                clerinfo.setAcctbr(tblRcInfo.getTrbrch());//账务机构                         
                clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
                clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.CR);//借贷标志-贷                                         
                clerinfo.setTranam(tblRcInfo.getTranam());//交易金额                                         
                clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
                clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
                clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
                SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
				
			}else if(E_AMNTCD.DR == tblRcInfo.getAmntcd()){
				if(isflag1 == E_YES___.NO){
					//获取电子账户信息
					KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getCardno(), false);
					E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
					KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
					if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
					}else{ // III类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
					}
					
					//往账 借-客户账
					DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class);
					DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
					drawDpAcctIn.setCardno(tblKnaAcdc.getCardno());//卡号
					drawDpAcctIn.setCustac(tblKnaAcdc.getCustac());//电子账号
					drawDpAcctIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
					drawDpAcctIn.setCrcycd(tblRcInfo.getCrcycd());//币种
					drawDpAcctIn.setTranam(tblRcInfo.getTranam());//交易金额
					drawDpAcctIn.setToacct(tblRcInfo.getInacct());//对手账号
			        drawDpAcctIn.setOpacna(tblRcInfo.getInacna());//对手户名
			        drawDpAcctIn.setOpbrch(tblRcInfo.getInbrch()); 
			        drawDpAcctIn.setSmrycd(smrycd);
			        dpSrv.addDrawAcctDp(drawDpAcctIn);
				}else if(isflag1 == E_YES___.YES){
					GlKnaAcct glKnaAcct = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getCardno(), false);
					IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
					acdrIn1.setAcbrch(tblRcInfo.getTrbrch());//机构
					acdrIn1.setBusino(glKnaAcct.getBusino()); //业务编码
					acdrIn1.setSubsac(glKnaAcct.getSubsac());//子户号
					acdrIn1.setCrcycd(glKnaAcct.getCrcycd());//币种
					acdrIn1.setTranam(tblRcInfo.getTranam());//交易金额
					acdrIn1.setToacct(tblRcInfo.getInacct());//对方账号
					acdrIn1.setToacna(tblRcInfo.getInacna());//对方户名
					acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
					acdrIn1.setSmrycd(smrycd);
					IoInAccount inSrv1 = CommTools.getRemoteInstance(IoInAccount.class);
					inSrv1.ioInAcdr(acdrIn1);
				}
				
				//贷-待清算
				IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
				acdrIn.setAcbrch(tblRcInfo.getTrbrch());//机构
				acdrIn.setBusino(para.getParm_value1()); //业务编码
				acdrIn.setSubsac(para.getParm_value2());//子户号
				acdrIn.setCrcycd(tblRcInfo.getCrcycd());//币种
				acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
				acdrIn.setToacct(tblRcInfo.getInacct());//对方账号
				acdrIn.setToacna(tblRcInfo.getInacna());//对方户名
				acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
				acdrIn.setSmrycd(smrycd);
				IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
				IaTransOutPro outPro = inAcctSer.ioInAccr(acdrIn);
				//平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
				
				//清算明细登记 借
				IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
                clerinfo.setAcctno(outPro.getAcctno());//账号                                 
                clerinfo.setAcctna(outPro.getAcctna());//账户名称                             
                clerinfo.setProdcd(para.getParm_value1());//产品编号                               
                clerinfo.setClactp(clactp);//系统内账号类型                                                                       
                clerinfo.setAcctbr(tblRcInfo.getTrbrch());//账务机构                         
                clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
                clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.DR);//借贷标志                                         
                clerinfo.setTranam(tblRcInfo.getTranam());//交易金额                             
                clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
                clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
                clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
                SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
				
			}else{
				throw DpModuleError.DpstComm.E9999("借贷标志不支持");
			}
			
		}else{
			throw DpModuleError.DpstComm.E9999("不支持处理类型，请手工处理");
		}
		/**
		 * 4,差错信息状态更新
		 */
		tblRcInfo.setTranst(E_CUPSST.CLWC);;//9-处理完成
		KnlIoblCupsErrorDao.updateOne_odb1(tblRcInfo);
	}

	/**
	 * 二代支付差错处理
	 * @param input
	 */
	private static void dealLsamrs(Input input) {
		String rdtrsq = input.getRdtrsq();//差错读取流水
		String rdtrdt = input.getRdtrdt();//差错读取日期
		E_CPRSST cprsst = input.getCprsst();//对账结果
		BigDecimal tranam = input.getTranam();//交易金额
		
		String smrycd = BusinessConstants.SUMMARY_AJ;//摘要代码-调整
		
		/**
		 * 1，获取差错信息
		 */
		KnlCnapotError tblRcInfo = KnlCnapotErrorDao.selectOne_odb1(rdtrsq, rdtrdt, false);
		if(CommUtil.isNull(tblRcInfo)){
			throw DpModuleError.DpstComm.E9999("大小额差错信息不存在");
		}
		if(E_TRANST.SUCCESS == tblRcInfo.getStatus()){
			throw DpModuleError.DpstComm.E9999("该差错已处理完成过");
		}
		
		/**
		 * 2,校验交易金额
		 */
		if(CommUtil.compare(tranam, tblRcInfo.getTranam())!=0){
			throw DpModuleError.DpstComm.E9999("传入金额与差错信息中金额不符");
		}
		
		/**
		 * 3,对账结果校验
		 */
		if(cprsst != tblRcInfo.getErroty()){
			throw DpModuleError.DpstComm.E9999("传入对账结果与记录不符");
		}
		
		 //判断转入方是内部户还是电子账户
		  E_ACCTROUTTYPE routeType =ApAcctRoutTools.getRouteType(tblRcInfo.getPyeeac());
	        E_YES___ isflag=E_YES___.NO;
	        if(routeType == E_ACCTROUTTYPE.INSIDE){
	           isflag = E_YES___.YES;//转入方为内部户
	        }else{
	           isflag = E_YES___.NO;//转入方为电子账户
	        }
	        
        //判断转出方是内部户还是电子账户
		  E_ACCTROUTTYPE routeType1 =ApAcctRoutTools.getRouteType(tblRcInfo.getPyerac());
		     E_YES___ isflag1=E_YES___.NO;
		     if(routeType1 == E_ACCTROUTTYPE.INSIDE){
		        isflag1 = E_YES___.YES;//转出方为内部户
		     }else{
		         isflag1 = E_YES___.NO;//转出方为电子账户
		      } 
		
		/**
		 * 4,差错处理
		 */
		//获取业务代码，
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		E_CLACTP clactp=null;//内部账户类型
		if(CommUtil.equals("0", tblRcInfo.getSubsys())){
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "05", "%", true);
			clactp=E_CLACTP._05;
		}else if(CommUtil.equals("1", tblRcInfo.getSubsys())){
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "10", "%", true);
			clactp=E_CLACTP._06;
		}else{
			throw DpModuleError.DpstComm.E9999("大小额业务代码参数未配置");
		}
		
		if(E_CPRSST.HWD == cprsst){
			//互联网核心多-蓝字冲账 红字冲清算
			if(E_IOTYPE.IN == tblRcInfo.getIotype()){
				if(isflag == E_YES___.NO){//转入方为电子账号
					//获取电子账户信息
					KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getPyeeac(), false);
					E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
					KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
					if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
					}else{ // III类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
					}
					
					//借-客户账
					DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class);
					DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
					drawDpAcctIn.setCardno(tblKnaAcdc.getCardno());//卡号
					drawDpAcctIn.setCustac(tblKnaAcdc.getCustac());//电子账号
					drawDpAcctIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
					drawDpAcctIn.setCrcycd(tblKnaAcct.getCrcycd());//币种
					drawDpAcctIn.setTranam(tblRcInfo.getTranam());//交易金额
					drawDpAcctIn.setToacct(tblRcInfo.getPyerac());//对方账号
					drawDpAcctIn.setOpacna(tblRcInfo.getPyerna());//对方户名
					drawDpAcctIn.setBankcd(tblRcInfo.getPyercd());//发起行号
					drawDpAcctIn.setSmrycd(smrycd);
					dpSrv.addDrawAcctDp(drawDpAcctIn); 
				}else if(isflag == E_YES___.YES){//转入方为内部户
					GlKnaAcct glKnaAcct = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getPyeeac(), false);
					IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
					acdrIn1.setAcbrch(tblRcInfo.getBrchno());//机构
					acdrIn1.setBusino(glKnaAcct.getBusino()); //业务编码
					acdrIn1.setSubsac(glKnaAcct.getSubsac());//子户号
					acdrIn1.setCrcycd(glKnaAcct.getCrcycd());//币种
					acdrIn1.setTranam(tblRcInfo.getTranam());//交易金额
					acdrIn1.setToacct(tblRcInfo.getPyerac());//对方账号
					acdrIn1.setToacna(tblRcInfo.getPyerna());//对方户名
					acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
					acdrIn1.setSmrycd(smrycd);
					IoInAccount inSrv1 = CommTools.getRemoteInstance(IoInAccount.class);
					inSrv1.ioInAcdr(acdrIn1);
				}
				
				//贷-待清算
				IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
				acdrIn1.setAcbrch(tblRcInfo.getBrchno());//机构
				acdrIn1.setBusino(para.getParm_value1()); //业务编码
				acdrIn1.setSubsac(para.getParm_value2());//子户号
				acdrIn1.setCrcycd(tblRcInfo.getCrcycd());//币种
				acdrIn1.setTranam(tblRcInfo.getTranam());//交易金额
				acdrIn1.setToacct(tblRcInfo.getPyerac());//对方账号
				acdrIn1.setToacna(tblRcInfo.getPyerna());//对方户名
				acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
				acdrIn1.setSmrycd(smrycd);
				IoInAccount inSrv1 = CommTools.getRemoteInstance(IoInAccount.class);
				IaTransOutPro outPro1 = inSrv1.ioInAccr(acdrIn1);
				//平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
				
				//登记清算明细
				IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
                clerinfo.setAcctno(outPro1.getAcctno());//账号                                 
                clerinfo.setAcctna(outPro1.getAcctna());//账户名称                             
                clerinfo.setProdcd(para.getParm_value1());//产品编号                               
                clerinfo.setClactp(clactp);//系统内账号类型                                                                       
                clerinfo.setAcctbr(tblRcInfo.getBrchno());//账务机构                         
                clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
                clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.CR);//借贷标志                                         
                clerinfo.setTranam(tblRcInfo.getTranam().negate());//交易金额  冲账记负金额                                   
                clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
                clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
                clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
                SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
				
				
			}else if(E_IOTYPE.OUT == tblRcInfo.getIotype()){
				//往账冲账 借-待清算	
				
				IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
				acdrIn1.setAcbrch(tblRcInfo.getBrchno());//机构
				acdrIn1.setBusino(para.getParm_value1()); //业务编码
				acdrIn1.setSubsac(para.getParm_value2());//子户号
				acdrIn1.setCrcycd(tblRcInfo.getCrcycd());//币种
				acdrIn1.setTranam(tblRcInfo.getTranam());//交易金额
				acdrIn1.setToacct(tblRcInfo.getPyeeac());//对方账号
				acdrIn1.setToacna(tblRcInfo.getPyeena());//对方户名
				acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
				acdrIn1.setSmrycd(smrycd);
				IoInAccount inSrv1 = CommTools.getRemoteInstance(IoInAccount.class);
				IaTransOutPro outPro1 = inSrv1.ioInAcdr(acdrIn1);
				
				if(isflag1== E_YES___.NO){//转出方为电子账户
					//获取电子账户信息
					KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getPyerac(), false);
					E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
					KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
					if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
					}else{ // III类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
					}
					
					//贷-客户账
					DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class);
					SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class);
					saveIn.setCardno(tblKnaAcdc.getCardno());//卡号
					saveIn.setCustac(tblKnaAcdc.getCustac());//电子账号
					saveIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
					saveIn.setCrcycd(tblKnaAcct.getCrcycd());//币种
					saveIn.setTranam(tblRcInfo.getTranam());//交易金额
					saveIn.setToacct(tblRcInfo.getPyeeac());//对方账号
					saveIn.setOpacna(tblRcInfo.getPyeena());//对方户名
//					saveIn.setOpbrch(tblRcInfo.getBrchno());//对方机构
					saveIn.setSmrycd(smrycd);
					dpSrv.addPostAcctDp(saveIn);
				}else if(isflag1 == E_YES___.YES){//转出方内部户
					//贷-内部户
					GlKnaAcct glKnaAcct1 = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getPyerac(), false);
					IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
					acdrIn.setAcbrch(tblRcInfo.getBrchno());//机构
					acdrIn.setBusino(glKnaAcct1.getBusino()); //业务编码
					acdrIn.setSubsac(glKnaAcct1.getSubsac());//子户号
					acdrIn.setCrcycd(glKnaAcct1.getCrcycd());//币种
					acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
					acdrIn.setToacct(tblRcInfo.getPyeeac());//对方账号
					acdrIn.setToacna(tblRcInfo.getPyeena());//对方户名
					acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
					acdrIn.setSmrycd(smrycd);
					IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
					inAcctSer.ioInAccr(acdrIn);
				}
		        
		        //平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
			    
				//清算明细登记
			    IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
	            clerinfo.setAcctno(outPro1.getAcctno());//账号                                 
	            clerinfo.setAcctna(outPro1.getAcctna());//账户名称                             
	            clerinfo.setProdcd(para.getParm_value1());//产品编号                               
	            clerinfo.setClactp(clactp);//系统内账号类型                                   
	            clerinfo.setAcctbr(tblRcInfo.getBrchno());//账务机构                         
	            clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
	            clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.DR);//借贷标志                      
	            clerinfo.setTranam(tblRcInfo.getTranam().negate());//交易金额 -冲账用负金额                                     
	            clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
	            clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
	            clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
	            SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
			}else{
				throw DpModuleError.DpstComm.E9999("来往账标志不支持");
			}
			
		}else if(E_CPRSST.HWS == cprsst){
			//互联网核心少-补账
			if(E_IOTYPE.IN == tblRcInfo.getIotype()){
				
				//借-待清算
				IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
				acdrIn1.setAcbrch(tblRcInfo.getBrchno());//机构
				acdrIn1.setBusino(para.getParm_value1()); //业务编码
				acdrIn1.setSubsac(para.getParm_value2());//子户号
				acdrIn1.setCrcycd(tblRcInfo.getCrcycd());//币种
				acdrIn1.setTranam(tblRcInfo.getTranam());//交易金额
				acdrIn1.setToacct(tblRcInfo.getPyerac());//对方账号
				acdrIn1.setToacna(tblRcInfo.getPyerna());//对方户名
				acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
				acdrIn1.setSmrycd(smrycd);
				IoInAccount inSrv1 = CommTools.getRemoteInstance(IoInAccount.class);
				IaTransOutPro outPro1 = inSrv1.ioInAcdr(acdrIn1);
				
				if(isflag == E_YES___.NO){//转入方为电子账户
					//获取电子账户信息
					KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getPyeeac(), false);
					E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
					KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
					if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
					}else{ // III类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
					}
					
					//贷-客户账
					DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class);
					SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class);
					saveIn.setCardno(tblKnaAcdc.getCardno());//卡号
					saveIn.setCustac(tblKnaAcdc.getCustac());//电子账号
					saveIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
					saveIn.setCrcycd(tblKnaAcct.getCrcycd());//币种
					saveIn.setTranam(tblRcInfo.getTranam());//交易金额
					saveIn.setToacct(tblRcInfo.getPyerac());//对方账号
					saveIn.setOpacna(tblRcInfo.getPyerna());//对方户名
					saveIn.setBankcd(tblRcInfo.getPyercd());//发起行号
					saveIn.setSmrycd(smrycd);
					dpSrv.addPostAcctDp(saveIn);
				}else if(isflag == E_YES___.YES){//转入方为内部户
					IaAcdrInfo acdrIn2 = SysUtil.getInstance(IaAcdrInfo.class); 
			        acdrIn2.setAcbrch(tblRcInfo.getBrchno());//机构
			        acdrIn2.setAcctno(tblRcInfo.getPyerac());//内部户账号
			        acdrIn2.setCrcycd(tblRcInfo.getCrcycd());//币种
			        acdrIn2.setTranam(tblRcInfo.getTranam());//交易金额
			        acdrIn2.setToacct(tblRcInfo.getPyeeac());//对方账号
			        acdrIn2.setToacna(tblRcInfo.getPyeena());//对方户名
			        acdrIn2.setTrantp(E_TRANTP.TR); //交易类型 
			        acdrIn2.setSmrycd(smrycd);
			        IoInAccount inSrv2 = CommTools.getRemoteInstance(IoInAccount.class);
			        inSrv2.ioInAccr(acdrIn1);
				}
				
				//平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
				
				//登记清算明细
				IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
                clerinfo.setAcctno(outPro1.getAcctno());//账号                                 
                clerinfo.setAcctna(outPro1.getAcctna());//账户名称                             
                clerinfo.setProdcd(para.getParm_value1());//产品编号                               
                clerinfo.setClactp(clactp);//系统内账号类型                                                                       
                clerinfo.setAcctbr(tblRcInfo.getBrchno());//账务机构                         
                clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
                clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.CR);//借贷标志-贷                                         
                clerinfo.setTranam(tblRcInfo.getTranam());//交易金额                                         
                clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
                clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
                clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
                SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
				
			}else if(E_IOTYPE.OUT == tblRcInfo.getIotype()){
				if(isflag1 == E_YES___.NO){
					//获取电子账户信息
					KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(tblRcInfo.getPyerac(), false);
					E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
					KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
					if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //I/II类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
					}else{ // III类户
						tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
					}
					
					//往账 借-客户账
					DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class);
					DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
					drawDpAcctIn.setCardno(tblKnaAcdc.getCardno());//卡号
					drawDpAcctIn.setCustac(tblKnaAcdc.getCustac());//电子账号
					drawDpAcctIn.setAcctno(tblKnaAcct.getAcctno());//负债账号
					drawDpAcctIn.setCrcycd(tblRcInfo.getCrcycd());//币种
					drawDpAcctIn.setTranam(tblRcInfo.getTranam());//交易金额
					drawDpAcctIn.setToacct(tblRcInfo.getPyeeac());//对方账号
			        drawDpAcctIn.setOpacna(tblRcInfo.getPyeena());//对方户名
//			        drawDpAcctIn.setOpbrch(tblRcInfo.getBrchno()); 
			        drawDpAcctIn.setSmrycd(smrycd);
			        dpSrv.addDrawAcctDp(drawDpAcctIn);
				}else if(isflag1 == E_YES___.YES){
					GlKnaAcct glKnaAcct = GlKnaAcctDao.selectOne_odb1(tblRcInfo.getPyerac(), false);
					IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
					acdrIn1.setAcbrch(tblRcInfo.getBrchno());//机构
					acdrIn1.setBusino(glKnaAcct.getBusino()); //业务编码
					acdrIn1.setSubsac(glKnaAcct.getSubsac());//子户号
					acdrIn1.setCrcycd(glKnaAcct.getCrcycd());//币种
					acdrIn1.setTranam(tblRcInfo.getTranam());//交易金额
					acdrIn1.setToacct(tblRcInfo.getPyeeac());//对方账号
					acdrIn1.setToacna(tblRcInfo.getPyeena());//对方户名
					acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
					acdrIn1.setSmrycd(smrycd);
					IoInAccount inSrv1 = CommTools.getRemoteInstance(IoInAccount.class);
					inSrv1.ioInAcdr(acdrIn1);
				}
				
				//贷-待清算
				IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
				acdrIn.setAcbrch(tblRcInfo.getBrchno());//机构
				acdrIn.setBusino(para.getParm_value1()); //业务编码
				acdrIn.setSubsac(para.getParm_value2());//子户号
				acdrIn.setCrcycd(tblRcInfo.getCrcycd());//币种
				acdrIn.setTranam(tblRcInfo.getTranam());//交易金额
				acdrIn.setToacct(tblRcInfo.getPyeeac());//对方账号
				acdrIn.setToacna(tblRcInfo.getPyeena());//对方户名
				acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
				acdrIn.setSmrycd(smrycd);
				IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
				IaTransOutPro outPro = inAcctSer.ioInAccr(acdrIn);
			    //平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
			    
				//清算明细登记
			    IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
	            clerinfo.setAcctno(outPro.getAcctno());//账号                                 
	            clerinfo.setAcctna(outPro.getAcctna());//账户名称                             
	            clerinfo.setProdcd(para.getParm_value1());//产品编号                               
	            clerinfo.setClactp(clactp);//系统内账号类型                                   
	            clerinfo.setAcctbr(tblRcInfo.getBrchno());//账务机构                         
	            clerinfo.setCrcycd(tblRcInfo.getCrcycd());//币种                                             
	            clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.DR);//借贷标志                             
	            clerinfo.setTranam(tblRcInfo.getTranam());//交易金额                                         
	            clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
	            clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
	            clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
	            SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
			}else{
				throw DpModuleError.DpstComm.E9999("来往账标志不支持");
			}
			
		}else{
			throw DpModuleError.DpstComm.E9999("不支持处理类型，请手工处理");
		}
		
		/**
		 * 5,差错信息状态更新
		 */
		tblRcInfo.setStatus(E_TRANST.SUCCESS);//9-处理完成
		KnlCnapotErrorDao.updateOne_odb1(tblRcInfo);
	}
	
}
