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
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIobl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.ChkPwdIN;
import cn.sunline.ltts.busi.dp.type.DpAcctType.ChkQtIN;
import cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Suprin.Input;
import cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Suprin.Output;
import cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Suprin.Property;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbSmsSvcType;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccountClearInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkOT;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubSqrd;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOTYPE;



public class suprin {

    public static void dealSuprin( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Suprin.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Suprin.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Suprin.Output output){
            String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();//主交易流水
            String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//交易日期
            ChkQtIN chkqtn = input.getChkqtn();//额度中心参数
            BigDecimal tranam = input.getTranam();//交易金额
            
            String smrycd = BusinessConstants.SUMMARY_ZR;//摘要代码-转入
            E_SBACTP sbactp = E_SBACTP._11;//账户类别-默认为活期结算账户
            E_CAPITP capitp =  E_CAPITP.IN109;//资金交易类型-超网来账
            E_CLACTP clactp = E_CLACTP._36;//清算账户类型-超网支付系统
            String tranev =  ApUtil.TRANS_EVENT_IOBILL;//明细冲正事件
            String tranev2 =  ApUtil.TRANS_EVENT_CLER;//清算明细冲正事件
            String servtp=input.getServtp();
            /**
    		 * add by liuz
    		 * 2018/08/16
    		 */
    		E_ACCTROUTTYPE routeType1 = ApAcctRoutTools.getRouteType(input.getIncard());
            E_YES___ isflag=E_YES___.NO;
            if(routeType1 == E_ACCTROUTTYPE.INSIDE){
               isflag = E_YES___.YES;//转入方为内部户
            }else{
               isflag = E_YES___.NO;//转入方为电子账户
            }
            /**
             * 1.输入校验
             */
            chkParam(input);
            
            /**
             * 2.重复检查
             */
            KnlIobl knlIobl = KnlIoblDao.selectOneWithLock_odb1(transq, trandt, false);
            if(CommUtil.isNotNull(knlIobl)){
                output.setMntrdt(knlIobl.getTrandt());
                output.setMntrsq(knlIobl.getTransq());
                output.setMntrtm(knlIobl.getTrantm());
                return;
            }
            
            if(isflag==E_YES___.NO){//转入方为电子账户
	            /**
	             * 3,账户信息校验
	             */
	            KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(input.getIncard(), false);//获取卡和电子账号映射
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
	            
	            if(!CommUtil.equals(input.getCrcycd(), tblKnaAcct.getCrcycd())){//币种校验
	                throw DpModuleError.DpstComm.BNAS0632();
	            }
	            /**
	             * 4,资金交易前检查
	             */
	            AcTranfeChkIN chkIN = SysUtil.getInstance(AcTranfeChkIN.class);
	            chkIN.setAccatp(accatp);//电子账户分类 
	            chkIN.setCardno(tblKnaAcdc.getCardno());//电子账户卡号 
	            chkIN.setCustac(tblKnaAcdc.getCustac());//电子账号ID   
	            chkIN.setCustna(tblKnaAcct.getAcctna());//电子账户户名 
	            chkIN.setOpcard(input.getOtcard());//对方账户卡号 -转出方账号
	            chkIN.setOppona(input.getOtacna());//对方户名-转出方户名
	            chkIN.setCapitp(capitp);//转账交易类型 
	            chkIN.setServtp(input.getServtp());//交易渠道          
	            AcTranfeChkOT chkOT = CapitalTransCheck.chkTranfe(chkIN);
	            
	            /**
	             * 5,扣减账户额度
	             */     
	            IoCaSevAccountLimit qt = CommTools.getRemoteInstance(IoCaSevAccountLimit.class); 
	            cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter qtIn = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter.class);
	            cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output   qtOt = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output.class);      
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
	            qtIn.setServtp(input.getServtp());
	            qtIn.setTranam(tranam);
	            qtIn.setCustie(chkOT.getIsbind());//是否绑定卡标识
	            qtIn.setFacesg(chkOT.getFacesg());//是否面签标识
	            qtIn.setRecpay(null);//收付方标识，电子账户转电子账户需要输入
	            qt.SubAcctQuota(qtIn, qtOt);//额度检查扣减
	            /**
	             * 6,记账
	             */
	            //1,内部户借方记账
	            KnpParameter para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "12", "%", true);
	            if(CommUtil.isNull(para)){
	                throw DpModuleError.DpstComm.E9999("业务代码未配置！");
	            }
	            String busino=para.getParm_value1();
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
	            saveIn.setBankcd(input.getPyercd());
	            saveIn.setCardno(tblKnaAcdc.getCardno());
	            saveIn.setCrcycd(tblKnaAcct.getCrcycd());
	            saveIn.setCustac(tblKnaAcdc.getCustac());
	            saveIn.setOpacna(input.getOtacna());
	//            saveIn.setOpbrch(input.getPyercd());
	            saveIn.setSmrycd(smrycd);
	            saveIn.setToacct(input.getOtcard());
	            saveIn.setTranam(tranam);
	            dpSrv.addPostAcctDp(saveIn);
	            
	            //3,平衡性检查
	            SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
	             
	            /**
	             * 7,账户状态处理(休眠转正常结息处理 + 修改电子账户状态)
	             */
	            CapitalTransDeal.dealAcctStatAndSett(chkOT.getCuacst(), tblKnaAcct);
	            
	            /**
	             * 8,登记明细登记簿
	             */     
	            KnlIobl iobl = SysUtil.getInstance(KnlIobl.class);
	              iobl.setCardno(input.getIncard());//转入方账号
	              iobl.setCrcycd(input.getCrcycd());
	//            iobl.setFromtp(input.getFromtp());
	              iobl.setPrcscd("suprin");
	              iobl.setServdt(input.getFrondt());//支付前置流水-渠道流水
	              iobl.setServsq(input.getFronsq());//支付前置日期-渠道日期
	              iobl.setStatus(E_TRANST.NORMAL);//交易状态
	              iobl.setTranam(input.getTranam());
	//            iobl.setRemark(input.getRemark());
	              iobl.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
	              iobl.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());
	              iobl.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
	              iobl.setCuacno(tblKnaAcdc.getCustac());//转入方电子账号
	              iobl.setIoflag(E_IOFLAG.IN);//出入金标志
	              iobl.setKeepdt(input.getKeepdt());//清算时间
	              iobl.setCapitp(capitp);
	           
	//            iobl.setToacno(input.getToacno());
	//            iobl.setToscac(input.getOtcard());
	              iobl.setBusino(busino);
	              iobl.setAcctno(tblKnaAcct.getAcctno()); //子账号
	              iobl.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq()); //业务跟踪编号
	              iobl.setChckdt(input.getNpcpdt()); //对账日期
	              iobl.setBrchno(input.getBrchno()); //电子账户所属机构
	//            iobl.setTobrch(input.getTobrch()); //对方账号所属机构
	              iobl.setTlcgam(input.getAfeeam()); //收费总金额
	              iobl.setToacct(input.getOtcard()); //对方账号
	//            iobl.setIncorp(CommTools.getBaseRunEnvs().getBusi_org_id()); //转入方法人
	//            iobl.setTocorp(input.getOtcorp()); //转出方法人
	              iobl.setIscler(E_YES___.NO); //是否已清算
	//            iobl.setIsspan(input.getIsspan()); //是否跨法人
	              iobl.setServtp(servtp); //渠道
	            KnlIoblDao.insert(iobl);
	            
	            //交易明细冲正事件
	            IoApRegBook cplinput = SysUtil.getInstance(IoApRegBook.class);
	            cplinput.setCustac(tblKnaAcdc.getCustac());
	            cplinput.setEvent1(input.getFronsq());
	            cplinput.setEvent2(input.getFrondt());
	            cplinput.setTranev(tranev);
	           // ApStrike.regBook(cplinput);
	    		IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);    		
	    		apinput.setReversal_event_id(cplinput.getTranev());
	    		apinput.setInformation_value(SysUtil.serialize(cplinput));
	    		MsEvent.register(apinput, true);
	            /**
	             * 9,短信通知
	             */
	            sendMessage(input,property, output);
	            
	            /**
	             * 10,清算登记
	             */
	            IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);                                    
	            clerinfo.setAcctno(outPro.getAcctno());//账号                                 
	            clerinfo.setAcctna(outPro.getAcctna());//账户名称                             
	            clerinfo.setProdcd(para.getParm_value1());//产品编号                               
	            clerinfo.setClactp(clactp);//系统内账号类型                                                                        
	            clerinfo.setAcctbr(tblKnaAcct.getBrchno());//账务机构                         
	            clerinfo.setCrcycd(input.getCrcycd());//币种                                             
	            clerinfo.setAmntcd(E_AMNTCD.CR);//借贷标志                                         
	            clerinfo.setTranam(tranam);//交易金额                                         
	            clerinfo.setServtp(input.getServtp());//交易渠道         
	            clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
	            clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次                                       
	            SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
	            
	            //清算明细冲正注册
	            IoApRegBook cplinput2 = SysUtil.getInstance(IoApRegBook.class);
	            cplinput2.setCustac(tblKnaAcdc.getCustac());
	            cplinput2.setEvent1(CommTools.getBaseRunEnvs().getMain_trxn_seq());
	            cplinput2.setEvent2(CommTools.getBaseRunEnvs().getTrxn_date());
	            cplinput2.setTranev(tranev2);
		           // ApStrike.regBook(cplinput);
		    		IoMsRegEvent apinput2 = SysUtil.getInstance(IoMsRegEvent.class);    		
		    		apinput2.setReversal_event_id(cplinput2.getTranev());
		    		apinput2.setInformation_value(SysUtil.serialize(cplinput2));
		    		MsEvent.register(apinput2, true);
	            
            }else if(isflag == E_YES___.YES){//转入方为内部户
            	IoInQuery inQry = CommTools.getRemoteInstance(IoInQuery.class);
                IoInacInfo glKnaAcct = inQry.InacInfoQuery(input.getIncard());

                if(CommUtil.isNull(glKnaAcct)){
                    throw DpModuleError.DpstComm.E9999("转入方账号不存在！");
                }
                KnpParameter para =KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "12", "%", true);
                String busino = para.getParm_value1();
    			if(CommUtil.isNull(para)){
    				throw DpModuleError.DpstComm.E9999("业务代码未配置！");
    			}
//                if(!CommUtil.equals(lsamIN.getInacna(), glKnaAcct.getAcctna())){
//                    throw DpModuleError.DpstComm.E9999("转入方账户名称输入错误！");
//                }
    			
    			//1.内部户借方记账
                IaAcdrInfo acdrIn2 = SysUtil.getInstance(IaAcdrInfo.class);
                acdrIn2.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
                acdrIn2.setTrantp(E_TRANTP.TR);//交易类型 
                acdrIn2.setCrcycd(glKnaAcct.getCrcycd());
                acdrIn2.setSmrycd(smrycd);//摘要码-ZR
                acdrIn2.setToacct(glKnaAcct.getAcctno());
                acdrIn2.setToacna(glKnaAcct.getAcctna());
                acdrIn2.setTranam(tranam);
                acdrIn2.setBusino(para.getParm_value1());//业务编码
                acdrIn2.setSubsac(para.getParm_value2());//子户号
                IoInAccount inAcctSer2 = SysUtil.getInstance(IoInAccount.class);
                IaTransOutPro outPro2 = inAcctSer2.ioInAcdr(acdrIn2);
                
                //2.内部户贷方记账
    			IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class);
    			acdrIn.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
    			acdrIn.setTrantp(E_TRANTP.TR);//交易类型 
    			acdrIn.setCrcycd(glKnaAcct.getCrcycd());
    			acdrIn.setSmrycd(smrycd);//摘要码-ZC
    			acdrIn.setToacct(input.getOtcard());
    			acdrIn.setToacna(input.getOtacna());
    			acdrIn.setTranam(tranam);
    			acdrIn.setAcctno(glKnaAcct.getAcctno());
//    			acdrIn.setBusino(para.getParm_value1());//业务编码
//    			acdrIn.setSubsac(para.getParm_value2());//子户号
    			IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
    			IaTransOutPro outPro = inAcctSer.ioInAccr(acdrIn);
    			
	            //3,平衡性检查
	            SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
	             
	            /**
	             * 7,账户状态处理(休眠转正常结息处理 + 修改电子账户状态)
	             */
//	            CapitalTransDeal.dealAcctStatAndSett(chkOT.getCuacst(), tblKnaAcct);
	            
	            /**
	             * 8,登记明细登记簿
	             */     
	            KnlIobl iobl = SysUtil.getInstance(KnlIobl.class);
	              iobl.setCardno(input.getIncard());//转入方账号
	              iobl.setCrcycd(input.getCrcycd());
	//            iobl.setFromtp(input.getFromtp());
	              iobl.setPrcscd("suprin");
	              iobl.setServdt(input.getFrondt());//支付前置流水-渠道流水
	              iobl.setServsq(input.getFronsq());//支付前置日期-渠道日期
	              iobl.setStatus(E_TRANST.NORMAL);//交易状态
	              iobl.setTranam(input.getTranam());
	//            iobl.setRemark(input.getRemark());
	              iobl.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
	              iobl.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());
	              iobl.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
	//              iobl.setCuacno(tblKnaAcdc.getCustac());//转入方电子账号
	              iobl.setIoflag(E_IOFLAG.IN);//出入金标志
	              iobl.setKeepdt(input.getKeepdt());//清算时间
	              iobl.setCapitp(capitp);
	           
	//            iobl.setToacno(input.getToacno());
	//            iobl.setToscac(input.getOtcard());
	              iobl.setBusino(busino);
	//              iobl.setAcctno(tblKnaAcct.getAcctno()); //子账号
	              iobl.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq()); //业务跟踪编号
	              iobl.setChckdt(input.getNpcpdt()); //对账日期
	              iobl.setBrchno(input.getBrchno()); //电子账户所属机构
	//            iobl.setTobrch(input.getTobrch()); //对方账号所属机构
	              iobl.setTlcgam(input.getAfeeam()); //收费总金额
	              iobl.setToacct(input.getOtcard()); //对方账号
	//            iobl.setIncorp(CommTools.getBaseRunEnvs().getBusi_org_id()); //转入方法人
	//            iobl.setTocorp(input.getOtcorp()); //转出方法人
	              iobl.setIscler(E_YES___.NO); //是否已清算
	//            iobl.setIsspan(input.getIsspan()); //是否跨法人
	              iobl.setServtp(servtp); //渠道
	            KnlIoblDao.insert(iobl);
	            
	            //交易明细冲正事件
	            IoApRegBook cplinput = SysUtil.getInstance(IoApRegBook.class);
	            cplinput.setCustac(input.getIncard());
	            cplinput.setEvent1(input.getFronsq());
	            cplinput.setEvent2(input.getFrondt());
	            cplinput.setTranev(tranev);
		           // ApStrike.regBook(cplinput);
		    		IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);    		
		    		apinput.setReversal_event_id(cplinput.getTranev());
		    		apinput.setInformation_value(SysUtil.serialize(cplinput));
		    		MsEvent.register(apinput, true);
	            /**
	             * 9,短信通知
	             */
	//            sendMessage(input,property, output);
	            
	            /**
	             * 10,清算登记
	             */
	            IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);                                    
	            clerinfo.setAcctno(outPro.getAcctno());//账号                                 
	            clerinfo.setAcctna(outPro.getAcctna());//账户名称                             
	            clerinfo.setProdcd(para.getParm_value1());//产品编号                               
	            clerinfo.setClactp(clactp);//系统内账号类型                                                                        
	            clerinfo.setAcctbr(glKnaAcct.getBrchno());//账务机构                         
	            clerinfo.setCrcycd(input.getCrcycd());//币种                                             
	            clerinfo.setAmntcd(E_AMNTCD.CR);//借贷标志                                         
	            clerinfo.setTranam(tranam);//交易金额                                         
	            clerinfo.setServtp(input.getServtp());//交易渠道         
	            clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
	            clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次                                       
	            SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
	            
	            //清算明细冲正注册
	            IoApRegBook cplinput2 = SysUtil.getInstance(IoApRegBook.class);
	            cplinput2.setCustac(input.getIncard());
	            cplinput2.setEvent1(CommTools.getBaseRunEnvs().getMain_trxn_seq());
	            cplinput2.setEvent2(CommTools.getBaseRunEnvs().getTrxn_date());
	            cplinput2.setTranev(tranev2);
		           // ApStrike.regBook(cplinput);
		    		IoMsRegEvent apinput2 = SysUtil.getInstance(IoMsRegEvent.class);    		
		    		apinput2.setReversal_event_id(cplinput2.getTranev());
		    		apinput2.setInformation_value(SysUtil.serialize(cplinput2));
		    		MsEvent.register(apinput2, true);
            }
            /**
             * 11，输出赋值
             */
            output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
            output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
            output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
            
    }
    /**
     * 短信通知登记
     */
    private static void sendMessage(Input input, Property property, Output output) {
        IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
//      cplKubSqrd.setAppsid();//APP推送ID 
        cplKubSqrd.setCardno(input.getIncard());//交易卡号  
//      cplKubSqrd.setPmvl01();//参数01    
//      cplKubSqrd.setPmvl02();//参数02    
//      cplKubSqrd.setPmvl03();//参数03    
//      cplKubSqrd.setPmvl04();//参数04    
//      cplKubSqrd.setPmvl05();//参数05    
        cplKubSqrd.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());//内部交易码
        cplKubSqrd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期  
        cplKubSqrd.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());//交易流水  
        cplKubSqrd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());//交易时间  
        IoPbSmsSvcType svcType = SysUtil.getInstance(IoPbSmsSvcType.class);
        svcType.pbTransqReg(cplKubSqrd);
        
    }
    /**
     * 入参检查
     */
    private static void chkParam(Input input) {
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
        if(CommUtil.isNull(input.getOtcard())){//转出账号
    		throw DpModuleError.DpstComm.BNAS1918();
    	}
        if (CommUtil.isNull(input.getCrcycd())) {
            throw DpModuleError.DpstComm.BNAS1101();
        }
//        if (CommUtil.isNull(input.getCstrfg())) {
//            throw DpModuleError.DpstComm.BNAS1377();
//        }
        if (CommUtil.isNull(input.getCsextg())) {
            throw DpModuleError.DpstComm.BNAS1378();
        }
        if (CommUtil.isNull(input.getPyercd())) {
            throw DpModuleError.DpstComm.BNAS1379();
        }
        if (CommUtil.isNull(input.getPyeecd())) {
            throw DpModuleError.DpstComm.BNAS1380();
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
        if (CommUtil.isNull(input.getTranam())) {
            throw DpModuleError.DpstAcct.BNAS0623();
        } 
        if (CommUtil.isNull(input.getServtp())) {
            throw DpModuleError.DpstAcct.E9999("交易渠道不能为空!");
        }
        if(input.getIotype()==E_IOTYPE.IN){
            if(CommUtil.isNull(input.getIncard())){ 
                throw DpModuleError.DpstComm.BNAS0324();
            } 
            if(CommUtil.isNull(input.getInacna())){ 
                throw DpModuleError.DpstComm.BNAS0325();
            } 
            
        }else{
            throw DpModuleError.DpstComm.BNAS1387();
        }
        if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0){
            throw DpModuleError.DpstComm.BNAS0394();
        }
        if(CommUtil.isNotNull(input.getChkpwd())){  //参数不为空才验密
            ChkPwdIN chkpwd = input.getChkpwd();
            if(CommUtil.isNotNull(chkpwd.getIspass())&&(E_YES___.YES==chkpwd.getIspass())){
//                String cryptoPassword=EncryTools.encryPassword(chkpwd.getPasswd(), chkpwd.getAuthif(),input.getIncard());
//                DpPassword.validatePassword(input.getIncard(), cryptoPassword,
//                        MsType.U_CHANNEL.ALL, "%");
                
            }
        }
        
        
    }
    
}
