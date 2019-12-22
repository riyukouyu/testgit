package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.client.CapitalTransLsam;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapot;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnsTranEror;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnsTranErorDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.ChkQtIN;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbSmsSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.InknlcnapotDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkOT;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.CupsTranfe;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubSqrd;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AFEETG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOTYPE;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SUBSYS;



public class lsamin {

	public static void dealLsamin( final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamin.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamin.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamin.Output output){
		
		// 摘要码
		if (CommUtil.isNull(input.getSmrycd())) {
			BusiTools.getBusiRunEnvs().setSmrycd(BusinessConstants.SUMMARY_ZR);// 转入
		}
		//交易前检查原流水是否存在
		KnlCnapot iobl = DpAcctQryDao.selknlcnapotChk(input.getMsetdt(), input.getMsetsq(), input.getPyercd(), input.getIotype(), input.getCrdbtg(), E_TRANST.NORMAL, false);
		if(CommUtil.isNotNull(iobl)){
			output.setHostsq(iobl.getTransq());
			output.setHostdt(iobl.getTrandt());
			output.setAcctbr(input.getBrchno());
			output.setClerdt(iobl.getClerdt());
			output.setClerod(iobl.getClenum());
			return;
		}
		
		BigDecimal tlcgam = chkParam(input); //输入参数校验
		if(CommUtil.compare(tlcgam, BigDecimal.ZERO) > 0){
			property.setIschrg(E_YES___.YES);
		}
		String errotx ="";
		try {
 			dealTrans(input, property, output);
			
		} catch (Exception e) {
			errotx = e.getLocalizedMessage();
			String  tx = "";
			
			if(CommUtil.isNotNull(errotx)){
				int index = errotx.indexOf("]");		
				if(index >= 0){					
					tx = errotx.substring(index + 1).replace("]", "").replace("[", "");
					
				}
			}
			final String errmsg = tx;

			DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
				@Override
				public Void execute() {
					
					KnsTranEror tblKnsTranEror = SysUtil.getInstance(KnsTranEror.class);
					tblKnsTranEror.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
					tblKnsTranEror.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
					/*if(CommTools.getBaseRunEnvs().getIscose()==E_YES___.YES){
						
						tblKnsTranEror.setErortp(E_WARNTP.CLOSING);	
					}else{
						
						tblKnsTranEror.setErortp(E_WARNTP.ACOUNT);			
					}*/
					tblKnsTranEror.setBrchno(input.getBrchno());
					tblKnsTranEror.setOtacct(input.getPyerac());
					tblKnsTranEror.setOtacna(input.getPyerna());
					tblKnsTranEror.setOtbrch("");
					tblKnsTranEror.setInacct(input.getPyeeac());					
					tblKnsTranEror.setInacna(input.getPyeena());
					tblKnsTranEror.setInbrch(input.getBrchno());
					tblKnsTranEror.setTranam(input.getTranam());
					tblKnsTranEror.setCrcycd(input.getCrcycd());
					tblKnsTranEror.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
					tblKnsTranEror.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());
					tblKnsTranEror.setGlacct("");
					tblKnsTranEror.setGlacna("");
					tblKnsTranEror.setGlseeq("");
					tblKnsTranEror.setDescrb(errmsg);
					KnsTranErorDao.insert(tblKnsTranEror);  
					return null;
				}
				});

			throw DpModuleError.DpstComm.E9999(errmsg);			
		}
		if(CommUtil.isNotNull(errotx)){

		}
		
	}

	
	
	public static void dealTrans(final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamin.Input input,final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamin.Property property, final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamin.Output output){
	    KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
	    E_ACCTROUTTYPE routeType1 = ApAcctRoutTools.getRouteType(input.getPyeeac());
	    //add by liuz begin
	    ChkQtIN chkqtn = input.getChkqtn();//额度中心参数
	    BigDecimal tranam = input.getTranam();//交易金额  
	    E_SBACTP sbactp = E_SBACTP._11;//账户类别-默认为活期结算账户
	    //add by liuz end
        E_YES___ isflag=E_YES___.NO;
        if(routeType1 == E_ACCTROUTTYPE.INSIDE){
           isflag = E_YES___.YES;//转出方为内部户
        }else{
           isflag = E_YES___.NO;//转出方为电子账户
        }
		if(isflag == E_YES___.NO){
		    IoCaKnaAcdc acdc = ActoacDao.selKnaAcdc(input.getPyeeac(), false);
	        if(CommUtil.isNull(acdc)){
	            throw DpModuleError.DpstComm.BNAS0750();
	        }
	        if(acdc.getStatus() == E_DPACST.CLOSE){
	            //CommTools.getBaseRunEnvs().setIscose(E_YES___.YES);
	            throw DpModuleError.DpstComm.BNAS0740();
	        }
	        //获取账户状态
	        E_CUACST checkcuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(acdc.getCustac());
	        if(checkcuacst == E_CUACST.OUTAGE){
	            throw DpModuleError.DpstComm.BNAS0850();
	        }
	        if(checkcuacst == E_CUACST.INACTIVE){
	            throw DpModuleError.DpstComm.E9999("非活跃账户不允许入账");
	        }
	        
//	      String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//	      CommTools.getBaseRunEnvs().setBusi_org_id(acdc.getCorpno());
	        
	        //状态、类型和状态字检查
	        E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(acdc.getCustac()); 
	        AcTranfeChkIN chkIN = SysUtil.getInstance(AcTranfeChkIN.class);
	        chkIN.setAccatp(accatp);
	        chkIN.setCardno(acdc.getCardno()); //电子账号卡号
	        chkIN.setCustac(acdc.getCustac()); //电子账号ID
	        chkIN.setCustna(input.getPyeena());
	        chkIN.setCapitp(E_CAPITP.IN107); //大小额来账
	        chkIN.setOpcard(input.getPyerac());
	        chkIN.setOppona(input.getPyerna());
	        chkIN.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
	        chkIN.setDime01(input.getBusitp()); //大小额的业务类型，鉴别退汇
	        
	        AcTranfeChkOT chkOT = CapitalTransCheck.chkTranfe(chkIN);
	        
	        E_CUACST cuacst = chkOT.getCuacst();
	        
	        if(accatp == E_ACCATP.WALLET){
	            tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(acdc.getCustac(), E_ACSETP.MA);
	            input.getChkqtn().setSbactp(E_SBACTP._12);
	            //add by liuz 20190226 begin
	            sbactp = E_SBACTP._12;
	            //add by liuz 20190226 end
	        }else{
	            input.getChkqtn().setSbactp(E_SBACTP._11);
	            tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(acdc.getCustac(), E_ACSETP.SA);
	            //add by liuz 20190226 begin
	            sbactp = E_SBACTP._11;
	            //add by liuz 20190226 end
	        }
	        
	        if(CommUtil.isNotNull(input.getPyeena()) && !CommUtil.equals(input.getPyeena(), tblKnaAcct.getAcctna())){
	            throw DpModuleError.DpstComm.BNAS0892();
	        }
	        
	        //add by liuz 20190226 begin
	        /**
			 * 5,扣减账户额度
			 */		
			IoCaSevAccountLimit qt = CommTools.getRemoteInstance(IoCaSevAccountLimit.class); 
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter qtIn = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter.class);
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output	qtOt = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output.class);		
			qtIn.setBrchno(tblKnaAcct.getBrchno());
			qtIn.setAclmfg(chkqtn.getAclmfg());
			qtIn.setAccttp(accatp);
			qtIn.setCustac(acdc.getCustac());
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
			//add by liuz 20190226 end
	        
//	      //检查转入方机构与电子账户机构是否一致
//	      if(CommUtil.isNotNull(input.getBrchno())){
//	          if(!CommUtil.equals(input.getBrchno(), tblKnaAcct.getBrchno())){
//	              throw DpModuleError.DpstComm.BNAS0031();
//	          }
//	      }
	        
	    
		}
	    CupsTranfe lsamIN = SysUtil.getInstance(CupsTranfe.class);
        IoSrvPbBranch brchSvrType = SysUtil.getInstance(IoSrvPbBranch.class);
        
        lsamIN.setCrcycd(input.getCrcycd());
        lsamIN.setCsextg(input.getCsextg());
        lsamIN.setInacct(input.getPyeeac());
        lsamIN.setInacna(input.getPyeena());
        lsamIN.setInbrch(input.getPyeecd());
        lsamIN.setIotype(input.getIotype());
        lsamIN.setIschrg(property.getIschrg());
        lsamIN.setOtacct(input.getPyerac());
        lsamIN.setOtacna(input.getPyerna());
        lsamIN.setOtbrch(input.getPyercd());
        lsamIN.setSmrycd(input.getSmrycd());
        lsamIN.setSubsys(input.getSubsys());
        lsamIN.setTranam(input.getTranam());
//        lsamIN.setCustac(acdc.getCustac());
        //mod 20170207 songlw 获取清算中心机构号
        lsamIN.setBrchno(brchSvrType.getCenterBranch(input.getBrchno()).getBrchno()); //电子汇划记账机构
        
//        lsamIN.setCuacst(cuacst);
//        lsamIN.setAccatp(accatp);
        
//    //退汇时额度扣减标志为是
//    if (CommUtil.isNotNull(input.getBusitp()) && CommUtil.equals(input.getBusitp(),  E_CNBSTP.A105.getValue())) {
//        input.getChkqtn().setIsckqt(E_YES___.YES);
//    }else {
//        input.getChkqtn().setIsckqt(E_YES___.YES);
//    }
//        CapitalTransLsam.prcLsamIN(tblKnaAcct, lsamIN, input.getChkqtn(), input.getChrgpm(), chkOT, input.getBrchno());	
        CapitalTransLsam.lnprcLsamIN(lsamIN, tblKnaAcct, isflag);//记账
        
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
        
        
        ApSysDateStru tblKappClrdat = BusiTools.getClearDateInfo();
		//登记大小额登记簿
		InknlcnapotDetl detl = SysUtil.getInstance(InknlcnapotDetl.class);
		detl.setServsq(input.getFronsq());//支付前置流水-渠道流水
		detl.setServdt(input.getFrondt());//支付前置日期-渠道日期
		detl.setAfeeam(input.getAfeeam());
		detl.setAfeetg(input.getAfeetg().getValue());
		detl.setAuthus(input.getAuthus());
		detl.setBrchno(input.getBrchno());
		detl.setChckdt(input.getMsetdt());
		detl.setChfcnb(input.getChfcnb());
		detl.setCkbkus(input.getCkbkus());
		detl.setCrcycd(input.getCrcycd());
		detl.setCrdbtg(input.getCrdbtg().getValue());
		detl.setCsextg(input.getCsextg());
		detl.setCstrfg(input.getCstrfg());
		detl.setFeeam1(input.getFeeam1());
		detl.setIotype(E_IOTYPE.IN);
		detl.setIscler(null);
		detl.setIsspan(null);
		detl.setKeepdt(input.getKeepdt());
		detl.setClerdt(tblKappClrdat.getSystdt());
		detl.setClenum(tblKappClrdat.getClenum());
		detl.setMesgtp(input.getMesgtp());
		detl.setMsetdt(input.getMsetdt());
		detl.setMsetsq(input.getMsetsq());
		detl.setNpcpbt(input.getNpcpbt());
		detl.setNpcpdt(input.getNpcpdt());
		detl.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());
		detl.setPriotp(input.getPriotp().getValue());
		detl.setPyeeac(input.getPyeeac());
		detl.setPyeecd(input.getPyeecd());
		detl.setPyeena(input.getPyeena());
		detl.setPyerac(input.getPyerac());
		detl.setPyercd(input.getPyercd());
		detl.setPyerna(input.getPyerna());
		detl.setRemark1(input.getRemark1());
		detl.setRemark2(input.getRemark2());
		detl.setRevrdt(input.getMsetdt());
		detl.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		detl.setSubsys(input.getSubsys());
		detl.setTranam(input.getTranam());
		detl.setUserid(input.getUserid());
		detl.setFronsq(input.getFronsq());
		detl.setFrondt(input.getFrondt());
		CommTools.getRemoteInstance(DpAcctSvcType.class).saveIOknlnapot(detl);
		
//		//来账，且是退汇，则更新销户登记簿处理状态
//		if(CommUtil.equals(input.getBusitp(), E_CNBSTP.A105.getValue())){
//			IoCaKnbClac cplKnbClac = DpAcctDao.selKnbClacBySignleStat(acdc.getCustac(), E_CLSTAT.TRSC, false);
//			if(CommUtil.isNotNull(cplKnbClac)){
//				CommTools.getRemoteInstance(DpAcctSvcType.class).updKnbClacStat(cplKnbClac.getClossq(), acdc.getCustac(), E_CLSTAT.FAIL);
//			}
//		}
		
		/**
		 * 短信通知（转出方为内部户时不登记短信处理流水）
		 */
		if(!CommUtil.equals(isflag.toString(), "1")){
		    IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
//	        cplKubSqrd.setAppsid();//APP推送ID 
	        cplKubSqrd.setCardno(input.getPyeeac());//交易卡号  
//	        cplKubSqrd.setPmvl01();//参数01    
//	        cplKubSqrd.setPmvl02();//参数02    
//	        cplKubSqrd.setPmvl03();//参数03    
//	        cplKubSqrd.setPmvl04();//参数04    
//	        cplKubSqrd.setPmvl05();//参数05    
	        cplKubSqrd.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());//内部交易码
	        cplKubSqrd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期  
	        cplKubSqrd.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());//交易流水  
	        cplKubSqrd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());//交易时间  
	        IoPbSmsSvcType svcType = SysUtil.getInstance(IoPbSmsSvcType.class);
	        svcType.pbTransqReg(cplKubSqrd);
		}
		
        output.setHostsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
        output.setHostdt(CommTools.getBaseRunEnvs().getTrxn_date());
        output.setAcctbr(input.getBrchno());
        output.setClerdt(tblKappClrdat.getSystdt());
        output.setClerod(tblKappClrdat.getClenum());
		
	}
	
	
	/**
	 * @Title: chkParam 
	 * @Description: 参数检查  
	 * @param input
	 * @return
	 * @author zhangan
	 * @date 2016年12月9日 下午3:41:27 
	 * @version V2.3.0
	 */
	public static BigDecimal chkParam(final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamin.Input input){
		
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
			throw DpModuleError.DpstComm.BNAS1361();
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
		//20180509 yanghao
//		if (CommUtil.isNull(input.getChfcnb())) {
//			throw DpModuleError.DpstComm.BNAS1362();
//		}
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
			if(CommUtil.isNull(input.getPyeeac())){ 
				throw DpModuleError.DpstComm.BNAS0324();
			} 
			if(CommUtil.isNull(input.getPyeena())){ 
				throw DpModuleError.DpstComm.BNAS0325();
			} 
			if(CommUtil.isNull(input.getKeepdt())){
				//与周品沟通：来帐  我这边可能没有人行的清算日期的   这个 你那边帮忙去掉检查吧
				//throw DpModuleError.DpstComm.E9027("清算日期");
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
}
