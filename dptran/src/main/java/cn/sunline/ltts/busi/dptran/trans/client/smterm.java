package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSignDetl;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSignDetlDao;
//import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSign;
//import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSignDao;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.acct.DpCloseAcctno;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProdDao;
//import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbRegi;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpOtherService;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.cf.IoCuTable.IoCifCustAccs;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaSignDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseIN;
//import cn.sunline.ltts.busi.ltts.zjrc.infrastructure.dap.plugin.type.ECustPlugin.E_MEDIUM;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_UNSGTP;
import cn.sunline.ltts.busi.sys.type.yht.E_TRANST;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;



public class smterm {
	
	private static BizLog log = BizLogUtil.getBizLog(smterm.class);
	/**
	 * 解约交易前处理
	 * **/
	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smterm.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smterm.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smterm.Output output){
		log.debug("<<===========解约交易前处理=========>>");
		//查询是否有正常签约信息
		if(CommUtil.isNull(input.getCardno())){
			throw DpModuleError.DpstComm.BNAS0955();
		}
		if(CommUtil.isNull(input.getAcctno())){
			throw DpModuleError.DpstComm.BNAS0064();
		}
		if(CommUtil.isNull(input.getCrcycd())){
			throw DpModuleError.DpstComm.BNAS1101();
		}
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaAcdc acdc = caqry.getKnaAcdcOdb2(input.getCardno(), false);
		if(CommUtil.isNull(acdc) || acdc.getStatus() == E_DPACST.CLOSE){
			throw DpModuleError.DpstComm.BNAS0750();
		}
		//判断输入的负债账号是否与电子账号有关联
		IoCaKnaAccs knaAccs = caqry.getKnaAccsOdb2(input.getAcctno(), true);
		if(!CommUtil.equals(acdc.getCustac(), knaAccs.getCustac())){
			throw DpModuleError.DpstComm.BNAS0768();
		}
		
		//检查产品编号
		if(CommUtil.isNotNull(input.getProdcd())){
			if(!CommUtil.equals(input.getProdcd(), knaAccs.getProdcd())){
				throw DpModuleError.DpstComm.BNAS1034();
			}
		}
		//检查输入的币种是否与签约时的币种一致
		if(CommUtil.compare(input.getCrcycd(), knaAccs.getCrcycd()) !=0 ){
			throw DpModuleError.DpstComm.BNAS1026();
		}
		//检查账户状态
		CapitalTransCheck.ChkAcctstOT(acdc.getCustac());
							
		//调用查询电子账户状态字服务
		IoDpFrozSvcType ioDpFrozSvcType = SysUtil.getInstance(IoDpFrozSvcType.class);
		IoDpAcStatusWord  cplAcStatusWord = ioDpFrozSvcType.getAcStatusWord(acdc.getCustac());

		if(E_YES___.YES == cplAcStatusWord.getDbfroz()){
			throw DpModuleError.DpstComm.BNAS0430();
		}
				
		if(E_YES___.YES == cplAcStatusWord.getBrfroz()){
			throw DpModuleError.DpstComm.BNAS0432();
		}
				
		if(E_YES___.YES == cplAcStatusWord.getBkalsp()){
			throw DpModuleError.DpstComm.BNAS0439();
		}
				
		if(E_YES___.YES == cplAcStatusWord.getClstop()){
			throw DpModuleError.DpstComm.BNAS0438();
		}
				
		if(E_YES___.YES == cplAcStatusWord.getOtalsp()){
			throw DpModuleError.DpstComm.BNAS1931();
		}
		
		//查询结算户信息
		KnaAcct acct = CapitalTransDeal.getSettKnaAcctSub(acdc.getCustac(), E_ACSETP.SA);
		IoCaKnaSignDetl detl = caqry.getKnaSignDetlFirstOdb3(acct.getAcctno(), input.getAcctno(), E_SIGNST.QY, false);
		
		//查询签约信息
		//IoCaKnaSign sign = caqry.kna_sign_selectOne_odb1(acdc.getCustac(), E_SIGNTP.ZNCXL, E_SIGNST.QY, false);
		if(CommUtil.isNull(detl)){
			throw DpModuleError.DpstComm.BNAS0744();
		}
		
		property.setUnsgtp(detl.getUnsgtp());
		//获得智能存款子账户余额
		
		KnaAcct dpacct = KnaAcctDao.selectOne_odb1(input.getAcctno(), false);
		if(CommUtil.isNull(dpacct)){
			KnaFxac dpfxac = KnaFxacDao.selectOne_odb1(input.getAcctno(), false);
			if(CommUtil.isNull(dpfxac)){
				throw DpModuleError.DpstComm.BNAS0701();
			}
			if(dpfxac.getDebttp() != E_DEBTTP.DP2509){
				throw DpModuleError.DpstComm.BNAS0701();
			}
			property.setFcflag(dpfxac.getPddpfg());
			property.setTobrch(dpfxac.getBrchno());
		}else{
			if(dpacct.getDebttp() != E_DEBTTP.DP2404){
				throw DpModuleError.DpstComm.BNAS0701();
			}
			property.setFcflag(dpacct.getPddpfg());
			property.setTobrch(dpacct.getBrchno());
		}
		
		//设置property值
		property.setAcseno(null);
		property.setAuacfg(E_YES___.YES);
		property.setCustac(acdc.getCustac());
		property.setOpacna(acct.getAcctna());
		property.setSignno(detl.getSignno());
		property.setToacct(acct.getAcctno());
		
		// 摘要码
		if (CommUtil.isNull(input.getSmrycd())) {
			BusiTools.getBusiRunEnvs().setSmrycd(BusinessConstants.SUMMARY_TZ);// 投资
		}
		
		property.setSmrycd1(BusinessConstants.SUMMARY_JS);// 摘要码-缴税
		
		/**
         * add by huangwh 20181204: start
         * description: 组装解冻信息！
         */
		
		//根据签约序号查询:转出签约明细表中的签约日期
        KnaSignDetl knaSignDetl = KnaSignDetlDao.selectOne_odb1(detl.getSignno(), true);
        String signdt = knaSignDetl.getSigndt();//签约日期=冻结日期
        
		//根据子账号查:冻结流水
	    String mntrsq = null;//开户流水=主交易流水=冻结流水
		KnaAcctProd knaAcctProd = KnaAcctProdDao.selectOne_odb1(input.getAcctno(),false);
        KnaFxacProd knaFxacProd = KnaFxacProdDao.selectOne_odb1(property.getToacct(), false);
		if(!CommUtil.isNull(knaAcctProd.getObgasi())){//活期预留字段有值
		    mntrsq = knaAcctProd.getObgasi();
		    property.setFrozfl(E_YES___.YES);
            property.setTrandt(signdt);
            property.setMntrsq(mntrsq);
		}else if(!CommUtil.isNull(knaFxacProd.getObgasi())){//定期预留字段有值
		    mntrsq = knaAcctProd.getObgasi();
		    property.setFrozfl(E_YES___.YES);
            property.setTrandt(signdt);
            property.setMntrsq(mntrsq);
		}else{
		    property.setFrozfl(E_YES___.NO);
		}
		
        /**
         * add by huangwh 20181204: end
         */
		
	}
	/**
	 * 解约交易后处理
	 * **/

	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smterm.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smterm.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smterm.Output output){
		//TODO:是否修改客户账号与负债账号对照表中的状态
		log.debug("<<===========智能存款解约修改子账户状态==========>>");
		//平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
		//add by zdj 20181026
	    output.setTranst(E_TRANST.SUCCESS);//订单状态
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());//主交易日期
		output.setMntrsq(CommTools.getBaseRunEnvs().getTrxn_seq());//主交易流水
		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());//交易时间
		//add end
	}
	
	
	/***
	 * @Title: DealCloseAcctno 
	 * @Description: 活期智能存款子账号销户处理 
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年7月13日 下午5:10:46 
	 * @version V2.3.0
	 */
	public static void DealCloseAcctno( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smterm.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smterm.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smterm.Output output){
		
		property.setTranam(BigDecimal.ZERO);
		property.setIntxam(BigDecimal.ZERO);
		
		if(property.getUnsgtp() == E_UNSGTP.T1){
			IoDpCloseIN to_clsin = SysUtil.getInstance(IoDpCloseIN.class);
			to_clsin.setCardno(input.getCardno());
			to_clsin.setAcseno(null);
			to_clsin.setSmrycd(BusinessConstants.SUMMARY_XH);
			to_clsin.setToacct(property.getToacct());
			to_clsin.setToname(property.getOpacna());
			to_clsin.setTobrch(property.getTobrch());
			
			if(property.getFcflag() == E_FCFLAG.CURRENT){
				KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(input.getAcctno(), false);
				InterestAndIntertax cplint =DpCloseAcctno.prcCurrInterest(tblKnaAcct, to_clsin);
				BigDecimal interest = cplint.getInstam();
				tblKnaAcct.setOnlnbl(DpAcctProc.getAcctBalance(tblKnaAcct));
				BigDecimal onlbal = DpCloseAcctno.prcCurrOnbal(tblKnaAcct, to_clsin);
				property.setTranam(onlbal.add(CommUtil.isNotNull(interest) ? interest : BigDecimal.ZERO));//本金+利息（包含利息税）
				property.setIntxam(cplint.getIntxam());//利息税
			
			}else if(property.getFcflag() == E_FCFLAG.FIX){
				KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(input.getAcctno(), false);
				DrawDpAcctOut drawDpAcctOut = DpCloseAcctno.prcClsFxacAcct(tblKnaFxac, property.getToacct(), E_YES___.YES);
				BigDecimal interest = drawDpAcctOut.getInstam();
				
				property.setTranam(tblKnaFxac.getOnlnbl().add(interest));
				property.setIntxam(drawDpAcctOut.getIntxam());//利息税
			}else{
				throw DpModuleError.DpstComm.BNAS1048();
			}
		}
 	}
	public static void termmq( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smterm.Input input,  
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Smterm.Property property,  
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Smterm.Output output){

		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//系统当前日期
		
		KnpParameter para = KnpParameterDao.selectOne_odb1("SIGNMQ", "%", "%", "%", true);
		
		String bdid = para.getParm_value1();// 服务绑定ID
		
		String mssdid = CommTools.getMySysId();// 随机生成消息ID
		
		String mesdna = para.getParm_value2();// 媒介名称
		
//		E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介
		
		IoDpOtherService dpOtherService = SysUtil.getInstanceProxyByBind (IoDpOtherService.class, bdid);
		
		IoDpOtherService.IoDpSendSignMsg.InputSetter mqInput = SysUtil.getInstance(IoDpOtherService.IoDpSendSignMsg.InputSetter.class);
		
		IoCaSevQryTableInfo ioCaSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		//查询签约明细表信息
	//	IoCaKnaSignDetl ioCaKnaSignDetl = ioCaSevQryTableInfo.kna_sign_detl_selectOne_odb1(property.getSignno(), false);
		IoCaKnaSignDetl ioCaKnaSignDetl = DpAcctDao.selKnaSignDetlInfo(input.getAcctno(),false);
		if(CommUtil.isNotNull(ioCaKnaSignDetl)){
			mqInput.setProdcd(ioCaKnaSignDetl.getTrprod());
			mqInput.setTimetm(ioCaKnaSignDetl.getTmstmp());
			mqInput.setSigndt(ioCaKnaSignDetl.getSigndt());
			mqInput.setEffedt(ioCaKnaSignDetl.getCncldt());
		}
//		if(!CommUtil.equals(input.getAcctno(), ioCaKnaSignDetl.getFxacct())){
//			throw DpModuleError.DpstComm.E9999("该账户未签约产品或已解约");
//		}
		//查询账户信息
		IoCaKnaCust ioCaKnaCust = ioCaSevQryTableInfo.getKnaCustByCustacOdb1(property.getCustac(), false);
		if(CommUtil.isNull(ioCaKnaCust)){
			throw DpModuleError.DpstComm.BNAS0754();
		}
		//获取客户关联信息
		IoCifCustAccs IoCifCustAccs =
				DpAcctDao.selCifCustAccsByCustno(ioCaKnaCust.getCustno(), false);
		if(CommUtil.isNull(IoCifCustAccs)){
			throw DpModuleError.DpstComm.BNAS1932();
		}
		
		if(CommUtil.isNotNull(property.getFcflag())){
			if(property.getFcflag()==E_FCFLAG.CURRENT){
				//查询活期附属表信息
				KnaAcctProd tblKnaAcctProd = KnaAcctProdDao.selectOne_odb1(input.getAcctno(), false);
				if(CommUtil.isNotNull(tblKnaAcctProd)){
					mqInput.setProdna(tblKnaAcctProd.getObgaon());
				}
				
			}else if(property.getFcflag()==E_FCFLAG.FIX){
				//查询定期附属表信息
				KnaFxacProd tblKnaFxacProd = KnaFxacProdDao.selectOne_odb1(input.getAcctno(), false);
				if(CommUtil.isNotNull(tblKnaFxacProd)){
					mqInput.setProdna(tblKnaFxacProd.getObgaon());
				}
			}
		}
		
		mqInput.setMsgid(mssdid); //发送消息ID
//		mqInput.setMedium(mssdtp); //消息媒介
		mqInput.setMdname(mesdna); //媒介名称
		mqInput.setSignst(E_SIGNST.JY);
		mqInput.setCustno(IoCifCustAccs.getCustid());
		mqInput.setCustna(ioCaKnaCust.getCustna());
		mqInput.setCardno(input.getCardno());
		mqInput.setBrchno(ioCaKnaCust.getBrchno());
//		mqInput.setBrchna("");
//		mqInput.setKeepam(input.getKeepam());
//		mqInput.setTrmiam(input.getTrmiam());
		mqInput.setTrandt(trandt);
		mqInput.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());
		mqInput.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		mqInput.setFcflag(property.getFcflag());
		dpOtherService.sendSignMsg(mqInput);

	}
	public static void qryinac( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smterm.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smterm.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smterm.Output output){
		E_INSPFG inspfg = property.getInspfg();
		
		if(E_INSPFG.INVO == inspfg){ //账户涉案
			//交易信息登记输入
			IoCaKnbTrinInput entity = SysUtil.getInstance(IoCaKnbTrinInput.class);
			entity.setCrcycd(input.getCrcycd());//币种
			entity.setInacna(property.getOpacna());//转入账户名称
			//entity.setInbank(property.getAcbrch());//转入银行行号
			entity.setIncard(property.getToacct());//转入账号
			entity.setIssucc(E_YES___.NO);//是否成功
			entity.setOtacna(property.getOpacna());//转出账户名称
			entity.setOtbrch(CommTools.getBaseRunEnvs().getTrxn_branch());//转出账户机构
			entity.setOtcard(input.getCardno());//转出账号
			entity.setTranam(property.getTranam());//交易金额
			entity.setTrantp(cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRANTP.TRANSFER);//交易类型
			//调用涉案可疑交易信息登记服务
			SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(entity);
			throw DpModuleError.DpstAcct.BNAS0770();
		}
		
		if(E_INSPFG.SUSP == inspfg){ //账户可疑
			//交易信息登记输入
			IoCaKnbTrinInput entity = SysUtil.getInstance(IoCaKnbTrinInput.class);
			entity.setCrcycd(input.getCrcycd());//币种
			entity.setInacna(property.getOpacna());//转入账户名称
			//entity.setInbank(property.getAcbrch());//转入银行行号
			entity.setIncard(property.getToacct());//转入账号
			entity.setIssucc(E_YES___.NO);//是否成功
			entity.setOtacna(property.getOpacna());//转出账户名称
			entity.setOtbrch(CommTools.getBaseRunEnvs().getTrxn_branch());//转出账户机构
			entity.setOtcard(input.getCardno());//转出账号
			entity.setTranam(property.getTranam());//交易金额
			entity.setTrantp(cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRANTP.TRANSFER);//交易类型
			//调用涉案可疑交易信息登记服务
			SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(entity);
			throw DpModuleError.DpstComm.BNAS0706();
		}
	}

}
