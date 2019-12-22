package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
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
import cn.sunline.ltts.busi.dp.type.DpAcctType.ChkQtIN;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.InknlcnapotDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkOT;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.CupsTranfe;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AFEETG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSATP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOTYPE;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SUBSYS;
//import cn.sunline.ltts.busi.ltts.zjrc.infrastructure.dap.plugin.type.ECustPlugin.E_MEDIUM;



public class lsamot {



	public static void dealLsamot( final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamot.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamot.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamot.Output output){
		
		// 摘要码
		if (CommUtil.isNull(input.getSmrycd())) {
			BusiTools.getBusiRunEnvs().setSmrycd(BusinessConstants.SUMMARY_ZC);// 转出
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
		
		BigDecimal tlcgam = chkParam(input);
		if(CommUtil.compare(tlcgam, BigDecimal.ZERO) > 0){
			property.setIschrg(E_YES___.YES);
		}
		
		property.setTlcgam(tlcgam);
		
		dealTrans(input, property, output);
		
		//sendCloseInfoMsg(input, property, output);
	}
	
	
	
	public static void dealTrans( final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamot.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamot.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamot.Output output){
		
	    KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
	    //根据账户规则判断账户是内部户还是电子账户
	    E_YES___ isinac = E_YES___.NO;
        E_ACCTROUTTYPE routeType1 = ApAcctRoutTools.getRouteType(input.getPyerac());
        //add by liuz begin
	    ChkQtIN chkqtn = input.getChkqtn();//额度中心参数
	    BigDecimal tranam = input.getTranam();//交易金额  
	    E_SBACTP sbactp = E_SBACTP._11;//账户类别-默认为活期结算账户
	    //add by liuz end
        if(routeType1 == E_ACCTROUTTYPE.INSIDE){
            isinac = E_YES___.YES;
        }

        if (isinac == E_YES___.NO)
        {
            IoCaKnaAcdc acdc = ActoacDao.selKnaAcdc(input.getPyerac(), false);
		
            if(CommUtil.isNull(acdc)){
                throw DpModuleError.DpstComm.BNAS0750();
            }
            if(acdc.getStatus() == E_DPACST.CLOSE){
                throw DpModuleError.DpstComm.BNAS0740();
            }
				
            E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(acdc.getCustac()); 
		
            
            //往账交易检查返回参数
            AcTranfeChkOT chkOT = SysUtil.getInstance(AcTranfeChkOT.class);

			//property.setIsclos(E_YES___.NO); //设置销户标志
			
			//状态、类型、状态字检查
			AcTranfeChkIN chkIN = SysUtil.getInstance(AcTranfeChkIN.class);
			chkIN.setAccatp(accatp);
			chkIN.setCardno(acdc.getCardno());
			chkIN.setCustac(acdc.getCustac());
			chkIN.setCustna(input.getPyerna());
			chkIN.setCapitp(E_CAPITP.OT206);
			chkIN.setOpcard(input.getPyeeac());
			chkIN.setOppona(input.getPyeena());
			chkIN.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
            chkOT = CapitalTransCheck.chkTranfe(chkIN);		
            
            /*
	        //客户化状态检查
			E_CUACST cuacst = CapitalTransCheck.ChkAcctstOT(acdc.getCustac());
            //状态字检查
            CapitalTransCheck.ChkAcctstWord(acdc.getCustac());
			*/
			
			if(accatp == E_ACCATP.WALLET){
				tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(acdc.getCustac(), E_ACSETP.MA);
				input.getChkqtn().setSbactp(E_SBACTP._12);
				//add by liuz 20190226 begin
	            sbactp = E_SBACTP._12;
	            //add by liuz 20190226 end
			}else{
				tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(acdc.getCustac(), E_ACSETP.SA);
				input.getChkqtn().setSbactp(E_SBACTP._11);
				//add by liuz 20190226 begin
	            sbactp = E_SBACTP._11;
	            //add by liuz 20190226 end
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
			
			
			//可用余额检查
			//BigDecimal usebal = DpAcctProc.getProductBal(acdc.getCustac(), input.getCrcycd(), false);
			if(CommUtil.isNotNull(input.getPyerna()) && !CommUtil.equals(input.getPyerna(), tblKnaAcct.getAcctna())){
				throw DpModuleError.DpstComm.BNAS0892();
			}
			// 可用余额 addby xiongzhao 20161223 
			BigDecimal usebal = SysUtil.getInstance(DpAcctSvcType.class)
					.getAcctaAvaBal(acdc.getCustac(), tblKnaAcct.getAcctno(),
							input.getCrcycd(), E_YES___.YES, E_YES___.NO);
			
			if(CommUtil.compare(usebal, input.getTranam()) < 0){
				throw DpModuleError.DpstComm.BNAS0442();
			}
			if(CommUtil.compare(usebal, property.getTlcgam()) < 0){
				throw DpModuleError.DpstComm.BNAS0334();
			}   			
        }
		CupsTranfe lsamOT = SysUtil.getInstance(CupsTranfe.class);
		
		lsamOT.setCrcycd(input.getCrcycd());
		lsamOT.setCsextg(input.getCsextg());
		lsamOT.setInacct(input.getPyeeac());
		lsamOT.setInacna(input.getPyeena());
		lsamOT.setInbrch(input.getPyeecd());
		lsamOT.setIotype(input.getIotype());
		lsamOT.setIschrg(property.getIschrg());
		lsamOT.setOtacct(input.getPyerac());
		lsamOT.setOtacna(input.getPyerna());
		lsamOT.setOtbrch(input.getPyercd());
		lsamOT.setSmrycd(input.getSmrycd());
		lsamOT.setSubsys(input.getSubsys());
		lsamOT.setTranam(input.getTranam());
//		lsamOT.setCustac(acdc.getCustac());
		lsamOT.setBrchno(input.getBrchno()); //电子汇划记账机构
//		lsamOT.setCuacst(cuacst);
//		lsamOT.setAccatp(accatp);
		/*
		CapitalTransLsam.lnprcLsamOT(tblKnaAcct, lsamOT, input.getChkqtn(), input.getChrgpm(), property.getIsclos(), chkOT);
		
		property.setAccatp(accatp);
		property.setTblKnaAcct(tblKnaAcct);
		
		if(property.getIsclos() == E_YES___.YES){ //销户
			prcClosAcct(tblKnaAcct, input, property);
		}
		*/
		
//		CommTools.getBaseRunEnvs().setBusi_org_id(corpno);

	    CapitalTransLsam.lnprcLsamOT(lsamOT, tblKnaAcct, isinac);

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
		detl.setServdt(input.getFrondt());//支付前置日期-渠道日期
		detl.setServsq(input.getFronsq());//支付前置流水-渠道流水
		detl.setIotype(E_IOTYPE.OUT);
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
//		detl.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());
		detl.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		detl.setSubsys(input.getSubsys());
		detl.setTranam(lsamOT.getTranam());
		detl.setUserid(input.getUserid());
		
		CommTools.getRemoteInstance(DpAcctSvcType.class).saveIOknlnapot(detl);
		
		/**
		 * 短信通知
		 */
		/* 贷款放款不发短信，没有开通电子账户。
		IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
//		cplKubSqrd.setAppsid();//APP推送ID 
		cplKubSqrd.setCardno(input.getPyerac());//交易卡号  
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
		*/
		
		
		output.setHostsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setHostdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setAcctbr(input.getBrchno());
		output.setClerdt(tblKappClrdat.getSystdt());
		output.setClerod(tblKappClrdat.getClenum());
	}
	/**
	 * @Title: prcClosAcct 
	 * @Description:   销户处理
	 * @param tblKnaAcct
	 * @param input
	 * @param property
	 * @author zhangan
	 * @date 2016年12月12日 上午10:29:08 
	 * @version V2.3.0
	 */
	public static void prcClosAcct(KnaAcct tblKnaAcct, final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamot.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamot.Property property){
		//6.如果是销户转账则销户
		
		//IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_accsByCustno(tblKnaAcct.getCustno(), true, E_STATUS.NORMAL);
		property.setCustid(tblKnaAcct.getCustno());
		
//		ClsAcctIn cplClsAcctIn = SysUtil.getInstance(ClsAcctIn.class);
//		
//		cplClsAcctIn.setCardno(input.getPyerac());
//		cplClsAcctIn.setCustac(tblKnaAcct.getCustac());
//		cplClsAcctIn.setCustna(tblKnaAcct.getAcctna());
//		cplClsAcctIn.setCustno(tblKnaAcct.getCustno());
//		//注销电子账户
//		SysUtil.getInstance(DpAcctSvcType.class).acctStatusUpd(cplClsAcctIn);
		
		IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
		cplDimeInfo.setCustac(tblKnaAcct.getCustac());
		cplDimeInfo.setDime01(E_CLSATP.OTHER.getValue()); //收款人账户类型
		SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);
		
	}
	
	public static void sendCloseInfoMsg( final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamot.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamot.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamot.Output output){
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
//			IoCaCloseAcctSendMsg closeSendMsgInput = CommTools
//					.getInstance(IoCaCloseAcctSendMsg.class);
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
			
			IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter closeSendMsgInput = CommTools
					.getInstance(IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter.class);
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
			
			IoCaOtherService.IoCaClAcSendContractMsg.InputSetter closeSendAgrtInput = CommTools
					.getInstance(IoCaOtherService.IoCaClAcSendContractMsg.InputSetter.class);
			
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
	/**
	 * @Title: chkParam 
	 * @Description: 参数检查  
	 * @param input
	 * @return
	 * @author zhangan
	 * @date 2016年12月9日 下午3:41:27 
	 * @version V2.3.0
	 */
	public static BigDecimal chkParam(final cn.sunline.ltts.busi.dptran.trans.client.intf.Lsamot.Input input){
		
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
		/*if (CommUtil.isNull(input.getChfcnb())) {
			throw DpModuleError.DpstComm.BNAS1362();
		}*/
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

		
		if(input.getIotype()==E_IOTYPE.OUT){//往账
			
			if(CommUtil.isNull(input.getPyerac())){ 
				throw DpModuleError.DpstComm.BNAS1385();
			} 
			if(CommUtil.isNull(input.getPyerna())){ 
				throw DpModuleError.DpstComm.BNAS1386();
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
