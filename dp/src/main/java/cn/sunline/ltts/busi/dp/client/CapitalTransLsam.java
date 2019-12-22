package cn.sunline.ltts.busi.dp.client;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.type.DpAcctType.ChkQtIN;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccountClearInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkOT;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.CupsTranfe;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalFee_IN;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSTRFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SUBSYS;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;
import cn.sunline.edsp.base.lang.Options;

public class CapitalTransLsam {

	
	/**
	 * @Title: prcLsamIN 
	 * @Description:大小额来账记账  
	 * @author zhangan
	 * @date 2016年12月9日 下午3:54:06 
	 * @version V2.3.0
	 */
	public static void prcLsamIN(KnaAcct tblKnaAcct, CupsTranfe lsamIN, ChkQtIN qtIN, Options<IoCgCalCenterReturn> chrgpm, AcTranfeChkOT chkOT, String brchno){
		
		//1.额度扣减
//		if(qtIN.getIsckqt() == E_YES___.YES){ 
			IoCaSevAccountLimit qt = CommTools.getRemoteInstance(IoCaSevAccountLimit.class); //获取电子账户额度服务
			
			//获取输入复合类型
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter qtIn = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter.class);
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output	qtOt = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output.class);
			
			qtIn.setBrchno(tblKnaAcct.getBrchno());
			qtIn.setAclmfg(qtIN.getAclmfg());
			qtIn.setAccttp(lsamIN.getAccatp());
			//qtIn.setAuthtp(qtIN.getAuthtp().toString());
			qtIn.setCustac(lsamIN.getCustac());
			qtIn.setCustid(qtIN.getCustid());
			qtIn.setCustlv(qtIN.getCustlv());
			qtIn.setAcctrt(qtIN.getAcctrt());
			qtIn.setLimttp(qtIN.getLimttp());
			qtIn.setPytltp(qtIN.getPytltp());
			qtIn.setRebktp(qtIN.getRebktp());
			qtIn.setRisklv(qtIN.getRisklv());
			qtIn.setSbactp(qtIN.getSbactp());
			qtIn.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());
			qtIn.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			qtIn.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
			qtIn.setTranam(lsamIN.getTranam());
			qtIn.setCustie(chkOT.getIsbind()); //是否绑定卡标识
			qtIn.setFacesg(chkOT.getFacesg());	//是否面签标识
			qtIn.setRecpay(null); //收付方标识，电子账户转电子账户需要输入
			
			qt.SubAcctQuota(qtIn, qtOt); //额度检查扣减
			
//		}
		
		BigDecimal chrgam = BigDecimal.ZERO;
		
		//2.收费
		if(lsamIN.getIschrg() == E_YES___.YES){
//			IoCgChrgSvcType chrg = CommTools.getRemoteInstance(IoCgChrgSvcType.class);
//			IoCgCalFee_IN chrgIN = SysUtil.getInstance(IoCgCalFee_IN.class);
//			
//			chrgIN.setCalcenter(chrgpm);
//			chrgIN.setCstrfg(E_CSTRFG.TRNSFER);
//			chrgIN.setChgflg(E_CHGFLG.ALL);
//			chrgIN.setChrgcy(lsamIN.getCrcycd());
//			chrgIN.setCsexfg(lsamIN.getCsextg());
//			chrgIN.setCustac(lsamIN.getCustac());
//			chrgIN.setTrancy(lsamIN.getCrcycd());
//			chrgIN.setClactp(null); //销户属性，在往账中的销户分支中使用
//			
//			chrg.CalCharge(chrgIN);
//			
//			
//			for (IoCgCalCenterReturn chrgDetl : chrgpm){
//				chrgam = chrgam.add(CommUtil.nvl(chrgDetl.getPaidam(), BigDecimal.ZERO));
//			}
			
		}
		
		//3.系统内往来内部户借记
		E_CLACTP clactp=null;
		
		if(CommUtil.equals(lsamIN.getSubsys(),E_SUBSYS.LM.getValue())){//小额
			clactp=E_CLACTP._06;
		}else if(CommUtil.equals(lsamIN.getSubsys(),E_SUBSYS.BG.getValue())){//大额
			clactp=E_CLACTP._05;
		}else {
			throw DpModuleError.DpstComm.BNAS1589();
		}
		
		IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
		
		String acbrch = BusiTools.getBusiRunEnvs().getCentbr(); //获取省中心机构号
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		
		if(CommUtil.equals("0", lsamIN.getSubsys())){
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "05", "%", true);
		}else if(CommUtil.equals("1", lsamIN.getSubsys())){
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "10", "%", true);
		}else{
			throw DpModuleError.DpstComm.E9999("大小额业务代码参数未配置");
		}
		acdrIn.setAcbrch(acbrch);
		acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
		acdrIn.setCrcycd(tblKnaAcct.getCrcycd());
		acdrIn.setSmrycd(lsamIN.getSmrycd());
		acdrIn.setToacct(lsamIN.getOtacct());
		acdrIn.setToacna(lsamIN.getOtacna());
		acdrIn.setTranam(lsamIN.getTranam());
		acdrIn.setBusino(para.getParm_value1()); //业务编码
		acdrIn.setSubsac(para.getParm_value2());//子户号
	
		IoInAccount inSrv = CommTools.getRemoteInstance(IoInAccount.class);
		IaTransOutPro outPro = inSrv.ioInAcdr(acdrIn);
		
		/**
		 * mod by xj 20180502 柳州项目不需要电子汇划记账
		 */
		//4.电子汇划记账
/*		CupsTranfe cplCnapot = SysUtil.getInstance(CupsTranfe.class);
		cplCnapot.setCrcycd(lsamIN.getCrcycd());
		cplCnapot.setSmrycd(lsamIN.getSmrycd());
		cplCnapot.setInbrch(lsamIN.getBrchno());
		cplCnapot.setTranam(lsamIN.getTranam());
		cplCnapot.setInacct(lsamIN.getOtacct());
		cplCnapot.setInacna(lsamIN.getOtacna());
		cplCnapot.setOtacct(lsamIN.getInacct());
		cplCnapot.setOtacna(lsamIN.getInacna());
		CapitalTransDeal.dealCnapotVN(cplCnapot, lsamIN.getIotype());*/
		
		//5.电子账户存入
		DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class); //存款记账服务
		
		SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class); //电子账户存入记账复合类型
		
		saveIn.setAcctno(tblKnaAcct.getAcctno()); //结算账户、钱包账户
		saveIn.setBankcd(lsamIN.getOtbrch());
		saveIn.setBankna("");
		saveIn.setCardno(lsamIN.getInacct()); //电子账户卡号
		saveIn.setCrcycd(tblKnaAcct.getCrcycd());
		saveIn.setCustac(lsamIN.getCustac());
		saveIn.setOpacna(lsamIN.getOtacna());
		saveIn.setOpbrch(acbrch);
		saveIn.setSmrycd(lsamIN.getSmrycd());
		saveIn.setToacct(lsamIN.getOtacct());
		saveIn.setTranam(lsamIN.getTranam());
		
		//add 20170313 songlw 修改公共报文头机构为传入记账机构
		String sBrchno = CommTools.getBaseRunEnvs().getTrxn_branch();
		CommTools.getBaseRunEnvs().setTrxn_branch(brchno);
		
		dpSrv.addPostAcctDp(saveIn);
		
		CommTools.getBaseRunEnvs().setTrxn_branch(sBrchno);
		
		//6.休眠转正常结息处理 + 修改电子账户状态
		CapitalTransDeal.dealAcctStatAndSett(lsamIN.getCuacst(), tblKnaAcct);
		
		//补充清算分录记录
		/*LsamClerIN clerIN = SysUtil.getInstance(LsamClerIN.class);
		clerIN.setBrchno(tblKnaAcct.getBrchno());
		clerIN.setCorpno(tblKnaAcct.getCorpno());
		clerIN.setCrcycd(tblKnaAcct.getCrcycd());
		clerIN.setIotype(E_IOTYPE.IN);
		clerIN.setIsdieb(E_YES___.NO);
		clerIN.setIshold(E_YES___.NO);
		clerIN.setSubsys(CommUtil.toEnum(E_SUBSYS.class, lsamIN.getSubsys()));
		clerIN.setTranam(lsamIN.getTranam());
		clerIN.setChrgam(chrgam);
		clerIN.setTrbrch(lsamIN.getBrchno()); //交易机构
		
		CapitalTransLsamStrike.prcLsamClear(clerIN);*/
		//清算
		IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
        //     clerinfo.setTrandt();//交易日期                                             
       //      clerinfo.setMntrsq();//主交易流水                                           
       //      clerinfo.setRecdno();//记录次序号                                           
               clerinfo.setAcctno(outPro.getAcctno());//账号                                 
               clerinfo.setAcctna(outPro.getAcctna());//账户名称                             
               clerinfo.setProdcd(para.getParm_value1());//产品编号                               
               clerinfo.setClactp(clactp);//系统内账号类型                                   
       //      clerinfo.setToacct();//对方账号                                             
       //      clerinfo.setToacbr();//对方机构号                                           
               clerinfo.setAcctbr(tblKnaAcct.getBrchno());//账务机构                         
               clerinfo.setCrcycd(lsamIN.getCrcycd());//币种                                             
               clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.CR);//借贷标志-贷                                         
               clerinfo.setTranam(lsamIN.getTranam());//交易金额                                         
               clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
               clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
               clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
               //clerinfo.setClerst(clerst);//数据同步标志                                   
               SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);	
		
	}
	
	public static void prcLsamOT(KnaAcct tblKnaAcct, CupsTranfe lsamOT, ChkQtIN qtIN, Options<IoCgCalCenterReturn> chrgpm, E_YES___ isclos, AcTranfeChkOT chkOT){
		//1.额度扣减
		if(isclos == E_YES___.NO){ //不是销户都要扣减额度
			IoCaSevAccountLimit qt = CommTools.getRemoteInstance(IoCaSevAccountLimit.class); //获取电子账户额度服务
			
			//获取输入复合类型
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter qtIn = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter.class);
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output	qtOt = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output.class);
			
			
			qtIn.setBrchno(tblKnaAcct.getBrchno());
			qtIn.setAclmfg(qtIN.getAclmfg());
			qtIn.setAccttp(lsamOT.getAccatp());
			//qtIn.setAuthtp(qtIN.getAuthtp().toString());
			qtIn.setCustac(lsamOT.getCustac());
			qtIn.setCustid(qtIN.getCustid());
			qtIn.setCustlv(qtIN.getCustlv());
			qtIn.setAcctrt(qtIN.getAcctrt());
			qtIn.setLimttp(qtIN.getLimttp());
			qtIn.setPytltp(qtIN.getPytltp());
			qtIn.setRebktp(qtIN.getRebktp());
			qtIn.setRisklv(qtIN.getRisklv());
			qtIn.setSbactp(qtIN.getSbactp());
			qtIn.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());
			qtIn.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			qtIn.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
			qtIn.setTranam(lsamOT.getTranam());
			qtIn.setCustie(chkOT.getIsbind()); //是否绑定卡标识
			qtIn.setFacesg(chkOT.getFacesg());	//是否面签标识
			qtIn.setRecpay(null); //收付方标识，电子账户转电子账户需要输入
			
			qt.SubAcctQuota(qtIn, qtOt); //额度检查扣减
		}
		
		//省中心机构
		String acbrch = BusiTools.getBusiRunEnvs().getCentbr(); //获取省中心机构号
		
		BigDecimal chrgam = BigDecimal.ZERO;
		//2.收费
		if(lsamOT.getIschrg() == E_YES___.YES){
			IoCgChrgSvcType chrg = CommTools.getRemoteInstance(IoCgChrgSvcType.class);
			IoCgCalFee_IN chrgIN = SysUtil.getInstance(IoCgCalFee_IN.class);
			
			//modify by  chenlk  20170308  增加交易金额登记
			for (IoCgCalCenterReturn chrgDetl : chrgpm){
				chrgam = chrgam.add(CommUtil.nvl(chrgDetl.getPaidam(), BigDecimal.ZERO));
				chrgDetl.setTranam(lsamOT.getTranam());

			}
			
			chrgIN.setCalcenter(chrgpm);
			chrgIN.setCstrfg(E_CSTRFG.TRNSFER);
			chrgIN.setChgflg(E_CHGFLG.ALL);
			chrgIN.setChrgcy(lsamOT.getCrcycd());
			chrgIN.setCsexfg(lsamOT.getCsextg());
			chrgIN.setCustac(lsamOT.getCustac());
			chrgIN.setTrancy(lsamOT.getCrcycd());
			chrgIN.setClactp(isclos); //销户属性，在往账中的销户分支中使用
			chrgIN.setIsclos(isclos);
			
			chrg.CalCharge(chrgIN);
			

		}
		
		//3.电子账户支取记账服务
		DrawDpAcctIn drawIN = SysUtil.getInstance(DrawDpAcctIn.class);
		DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class); //存款记账服务
		
		drawIN.setAcctno(tblKnaAcct.getAcctno());
		drawIN.setAcseno(null);
		drawIN.setAuacfg(null);
		drawIN.setBankcd(lsamOT.getInbrch());
		drawIN.setBankna("");
		drawIN.setCardno(lsamOT.getOtacct());
		drawIN.setCrcycd(lsamOT.getCrcycd());
		drawIN.setCustac(lsamOT.getCustac());
		if(isclos == E_YES___.YES){
			drawIN.setIschck(E_YES___.NO);
		}else{
			drawIN.setIschck(E_YES___.YES);
		}
		drawIN.setOpacna(lsamOT.getInacna());
		drawIN.setOpbrch(acbrch);
		drawIN.setSmrycd(lsamOT.getSmrycd());
		drawIN.setSmryds(ApSmryTools.getText(lsamOT.getSmrycd()));
		drawIN.setToacct(lsamOT.getInacct());
		drawIN.setTranam(lsamOT.getTranam());
		
		dpSrv.addDrawAcctDp(drawIN);
		
		
		/**
		 * mod by xj 20180502 柳州项目不需要电子汇划记账
		 */
		//4.电子汇划记账
/*		CupsTranfe cplCnapot = SysUtil.getInstance(CupsTranfe.class);
		cplCnapot.setCrcycd(lsamOT.getCrcycd());
		cplCnapot.setSmrycd(lsamOT.getSmrycd());
		cplCnapot.setInbrch(lsamOT.getBrchno());
		cplCnapot.setTranam(lsamOT.getTranam());
		cplCnapot.setInacct(lsamOT.getInacct());
		cplCnapot.setInacna(lsamOT.getInacna());
		cplCnapot.setOtacct(lsamOT.getOtacct());
		cplCnapot.setOtacna(lsamOT.getOtacna());
		CapitalTransDeal.dealCnapotVN(cplCnapot, lsamOT.getIotype());*/
		
		//5.贷系统内清算
		E_CLACTP clactp=null;
		
		if(CommUtil.equals(lsamOT.getSubsys(),E_SUBSYS.LM.getValue())){//小额
			clactp=E_CLACTP._06;
		}else if(CommUtil.equals(lsamOT.getSubsys(),E_SUBSYS.BG.getValue())){//大额
			clactp=E_CLACTP._05;
		}else {
			throw DpModuleError.DpstComm.BNAS1589();
		}
		
		IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class); //内部户记账输入
		
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "05", "%", true);
		
		acdrIn.setAcbrch(acbrch);
		acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
		acdrIn.setCrcycd(tblKnaAcct.getCrcycd());
		acdrIn.setSmrycd(lsamOT.getSmrycd());
		acdrIn.setToacct(lsamOT.getInacct());
		acdrIn.setToacna(lsamOT.getInacna());
		acdrIn.setTranam(lsamOT.getTranam());
		acdrIn.setBusino(para.getParm_value1()); //业务编码
		acdrIn.setSubsac(para.getParm_value2());//子户号
		
		IoInAccount inSrv = CommTools.getRemoteInstance(IoInAccount.class);
		IaTransOutPro outPro = inSrv.ioInAccr(acdrIn);

		/*//补充清算分录记录
		LsamClerIN clerIN = SysUtil.getInstance(LsamClerIN.class);
		clerIN.setBrchno(tblKnaAcct.getBrchno());
		clerIN.setCorpno(tblKnaAcct.getCorpno());
		clerIN.setCrcycd(tblKnaAcct.getCrcycd());
		clerIN.setIotype(E_IOTYPE.OUT);
		clerIN.setIsdieb(E_YES___.NO);
		clerIN.setIshold(E_YES___.NO);
		clerIN.setSubsys(CommUtil.toEnum(E_SUBSYS.class, lsamOT.getSubsys()));
		clerIN.setTranam(lsamOT.getTranam());
		clerIN.setChrgam(chrgam);
		clerIN.setTrbrch(lsamOT.getBrchno()); //交易机构
		
		CapitalTransLsamStrike.prcLsamClear(clerIN);*/
        
        //清算
        IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
        //     clerinfo.setTrandt();//交易日期                                             
       //      clerinfo.setMntrsq();//主交易流水                                           
       //      clerinfo.setRecdno();//记录次序号                                           
               clerinfo.setAcctno(outPro.getAcctno());//账号                                 
               clerinfo.setAcctna(outPro.getAcctna());//账户名称                             
               clerinfo.setProdcd(para.getParm_value1());//产品编号                               
               clerinfo.setClactp(clactp);//系统内账号类型                                   
       //      clerinfo.setToacct();//对方账号                                             
       //      clerinfo.setToacbr();//对方机构号                                           
               clerinfo.setAcctbr(tblKnaAcct.getBrchno());//账务机构                         
               clerinfo.setCrcycd(lsamOT.getCrcycd());//币种                                             
               clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.DR);//借贷标志 -借                                     
               clerinfo.setTranam(lsamOT.getTranam());//交易金额                                         
               clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
               clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
               clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
               //clerinfo.setClerst(clerst);//数据同步标志                                   
               SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
		
	}
	

	public static void lnprcLsamOT(CupsTranfe lsamOT, KnaAcct tblKnaAcct, E_YES___ isinac){
   
	    //省中心机构
	    String acbrch = BusiTools.getBusiRunEnvs().getCentbr(); //获取省中心机构号
 	    	    
        E_CLACTP clactp=null;
        IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class); //内部户记账输入
        IoInAccount inSrv = CommTools.getRemoteInstance(IoInAccount.class);
        
        if(CommUtil.equals(lsamOT.getSubsys(),E_SUBSYS.LM.getValue())){//小额
            clactp=E_CLACTP._06;
        }else if(CommUtil.equals(lsamOT.getSubsys(),E_SUBSYS.BG.getValue())){//大额
            clactp=E_CLACTP._05;
        }else {
            throw DpModuleError.DpstComm.BNAS1589();
        }
        
        if (isinac == E_YES___.NO)
        {
            //借：电子账户支取记账
            DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class); //存款记账服务
            DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
            drawDpAcctIn.setAcctno(tblKnaAcct.getAcctno()); //结算账户、钱包账户
            drawDpAcctIn.setCustac(tblKnaAcct.getCustac());
            drawDpAcctIn.setCardno(lsamOT.getOtacct());
            drawDpAcctIn.setOpacna(lsamOT.getInacna());
            drawDpAcctIn.setToacct(lsamOT.getInacct());
            drawDpAcctIn.setCrcycd(lsamOT.getCrcycd());
            drawDpAcctIn.setBankcd(tblKnaAcct.getBrchno());
            drawDpAcctIn.setTranam(lsamOT.getTranam());
            drawDpAcctIn.setSmrycd(lsamOT.getSmrycd());
//          drawDpAcctIn.setOpbrch(inbrch); 
            dpSrv.addDrawAcctDp(drawDpAcctIn);
        }else {           
            //借：上送的付款人账号（内部户）
//          para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "11", "%", true);
        
            acdrIn.setAcbrch(acbrch);
            acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
            acdrIn.setCrcycd(lsamOT.getCrcycd());
            acdrIn.setSmrycd(lsamOT.getSmrycd());
            acdrIn.setToacct(lsamOT.getOtacct());
            acdrIn.setToacna(lsamOT.getOtacna());
            acdrIn.setTranam(lsamOT.getTranam());
            acdrIn.setAcctno(lsamOT.getOtacct());
//          acdrIn.setBusino(para.getParm_value1()); //业务编码
//          acdrIn.setSubsac(para.getParm_value2());//子户号

            IoInQuery inQry = CommTools.getRemoteInstance(IoInQuery.class);
            IoInacInfo outInfo = inQry.InacInfoQuery(acdrIn.getAcctno());
            if (CommUtil.isNull(outInfo.getAcctno())) {
                throw InError.comm.E0003("内部户[" + acdrIn.getAcctno() + "]信息不存在");
            }
        
            IaTransOutPro outPro = inSrv.ioInAcdr(acdrIn);
        }
        
	    //贷：待清算-大小额
	    acdrIn = SysUtil.getInstance(IaAcdrInfo.class); //内部户贷方记账输入
	    KnpParameter para = SysUtil.getInstance(KnpParameter.class);
        if(CommUtil.equals("0", lsamOT.getSubsys())){
            para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "05", "%", true);
        }else if(CommUtil.equals("1", lsamOT.getSubsys())){
            para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "10", "%", true);
        }else{
            throw DpModuleError.DpstComm.E9999("大小额业务代码参数未配置");
        }	    	    
	    acdrIn.setAcbrch(acbrch);
	    acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
	    acdrIn.setCrcycd(lsamOT.getCrcycd());
	    acdrIn.setSmrycd(lsamOT.getSmrycd());
	    acdrIn.setToacct(lsamOT.getInacct());
	    acdrIn.setToacna(lsamOT.getInacna());
	    acdrIn.setTranam(lsamOT.getTranam());
	    acdrIn.setBusino(para.getParm_value1()); //业务编码
	    acdrIn.setSubsac(para.getParm_value2());//子户号
	    
	    inSrv = CommTools.getRemoteInstance(IoInAccount.class);
	    IaTransOutPro outPro = inSrv.ioInAccr(acdrIn);

	    //清算
	    IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
	    //     clerinfo.setTrandt();//交易日期                                             
	   //      clerinfo.setMntrsq();//主交易流水                                           
	   //      clerinfo.setRecdno();//记录次序号                                           
	           clerinfo.setAcctno(outPro.getAcctno());//账号                                 
	           clerinfo.setAcctna(outPro.getAcctna());//账户名称                             
	           clerinfo.setProdcd(para.getParm_value1());//产品编号                               
	           clerinfo.setClactp(clactp);//系统内账号类型                                   
	   //      clerinfo.setToacct();//对方账号                                             
	   //      clerinfo.setToacbr();//对方机构号                                           
	           clerinfo.setAcctbr(acbrch);//账务机构                         
	           clerinfo.setCrcycd(lsamOT.getCrcycd());//币种                                             
	           clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.DR);//借贷标志                                
	           clerinfo.setTranam(lsamOT.getTranam());//交易金额                                         
	           clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
	           clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
	           clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
	           //clerinfo.setClerst(clerst);//数据同步标志                                   
	           SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);
	           
	}
	/**
	 * 大小额来账(支持转内部户)
	 * add by yanghao 20180726
	 */
    public static void lnprcLsamIN(CupsTranfe lsamIN, KnaAcct tblKnaAcct, E_YES___ isflag) {
        //省中心机构
        String acbrch = BusiTools.getBusiRunEnvs().getCentbr(); //获取省中心机构号
                
        E_CLACTP clactp=null;
        IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class); //内部户记账输入
        IoInAccount inSrv = CommTools.getRemoteInstance(IoInAccount.class);
        
        if(CommUtil.equals(lsamIN.getSubsys(),E_SUBSYS.LM.getValue())){//小额
            clactp=E_CLACTP._06;
        }else if(CommUtil.equals(lsamIN.getSubsys(),E_SUBSYS.BG.getValue())){//大额
            clactp=E_CLACTP._05;
        }else {
            throw DpModuleError.DpstComm.BNAS1589();
        }
        //内部户借方记账
        KnpParameter para = SysUtil.getInstance(KnpParameter.class); 
        if(CommUtil.equals("0", lsamIN.getSubsys())){
            para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "05", "%", true);
        }else if(CommUtil.equals("1", lsamIN.getSubsys())){
            para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "10", "%", true);
        }else{
            throw DpModuleError.DpstComm.E9999("大小额业务代码参数未配置");
        }
        acdrIn.setAcbrch(acbrch);
        acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
        acdrIn.setCrcycd(lsamIN.getCrcycd());
        acdrIn.setSmrycd(lsamIN.getSmrycd());
        acdrIn.setToacct(lsamIN.getOtacct());
        acdrIn.setToacna(lsamIN.getOtacna());
        acdrIn.setTranam(lsamIN.getTranam());
        acdrIn.setBusino(para.getParm_value1()); //业务编码
        acdrIn.setSubsac(para.getParm_value2());//子户号
        IaTransOutPro outPro = inSrv.ioInAcdr(acdrIn);
        
        if(isflag==E_YES___.NO){//转入方账户为电子账户
            //电子账户贷方记账
            DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class); //存款记账服务
            SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class); //电子账户存入记账复合类型
            
            saveIn.setAcctno(tblKnaAcct.getAcctno()); //结算账户、钱包账户
            saveIn.setBankcd(lsamIN.getOtbrch());
            saveIn.setBankna("");
            saveIn.setCardno(lsamIN.getInacct());
            saveIn.setCrcycd(lsamIN.getCrcycd());
            saveIn.setCustac(lsamIN.getCustac());
            saveIn.setOpacna(lsamIN.getOtacna());
            saveIn.setOpbrch(acbrch);
            saveIn.setSmrycd(lsamIN.getSmrycd());
            saveIn.setToacct(lsamIN.getOtacct());
            saveIn.setTranam(lsamIN.getTranam());
            dpSrv.addPostAcctDp(saveIn);
            
            //休眠转正常结息处理 + 修改电子账户状态
            CapitalTransDeal.dealAcctStatAndSett(lsamIN.getCuacst(), tblKnaAcct); 
            
        }else if(isflag==E_YES___.YES){//转入方账户为内部户
            IoInQuery inQry = CommTools.getRemoteInstance(IoInQuery.class);
            IoInacInfo glKnaAcct = inQry.InacInfoQuery(lsamIN.getInacct());

            if(CommUtil.isNull(glKnaAcct)){
                throw DpModuleError.DpstComm.E9999("转入方账号不存在！");
            }
            if(!CommUtil.equals(lsamIN.getInacna(), glKnaAcct.getAcctna())){
                throw DpModuleError.DpstComm.E9999("转入方账户名称输入错误！");
            }
            
            //内部户贷方记账
            acdrIn.setAcbrch(acbrch);
            acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
            acdrIn.setCrcycd(lsamIN.getCrcycd());
            acdrIn.setSmrycd(lsamIN.getSmrycd());
            acdrIn.setToacct(lsamIN.getOtacct());
            acdrIn.setToacna(lsamIN.getOtacna());
            acdrIn.setTranam(lsamIN.getTranam());
            acdrIn.setBusino(glKnaAcct.getBusino()); //业务编码
            acdrIn.setSubsac(glKnaAcct.getSubsac());//子户号
            inSrv = CommTools.getRemoteInstance(IoInAccount.class);
            inSrv.ioInAccr(acdrIn);
        }   
      //清算
        IoAccountClearInfo clerinfo = SysUtil.getInstance(IoAccountClearInfo.class);
        //     clerinfo.setTrandt();//交易日期                                             
        //      clerinfo.setMntrsq();//主交易流水                                           
        //      clerinfo.setRecdno();//记录次序号                                           
        clerinfo.setAcctno(outPro.getAcctno());//账号                                 
        clerinfo.setAcctna(outPro.getAcctna());//账户名称                             
        clerinfo.setProdcd(para.getParm_value1());//产品编号                               
        clerinfo.setClactp(clactp);//系统内账号类型                                   
        //      clerinfo.setToacct();//对方账号                                             
        //      clerinfo.setToacbr();//对方机构号                                           
        clerinfo.setAcctbr(acbrch);//账务机构                         
        clerinfo.setCrcycd(lsamIN.getCrcycd());//币种                                             
        clerinfo.setAmntcd(BaseEnumType.E_AMNTCD.CR);//借贷标志-贷                                         
        clerinfo.setTranam(lsamIN.getTranam());//交易金额                                         
        clerinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道         
        clerinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期         
        clerinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次         
        //clerinfo.setClerst(clerst);//数据同步标志                                   
        SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsqCler(clerinfo);  
    }

}
