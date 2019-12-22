package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.type.CaCustInfo;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIobl;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkOT;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.ApSmsType.ToAppSendMsg;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;



public class tranot {

	private static BizLog log = BizLogUtil.getBizLog(tranot.class);
	
	/**
	 * @Title: DealTransBefore 
	 * @Description: 交易前业务性检查  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年12月13日 下午2:00:44 
	 * @version V2.3.0
	 */
	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranot.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranot.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranot.Output output){
		//重复提交检查
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		KnlIobl iobl = ActoacDao.selKnlIoblDetl(transq, trandt, false);
		if(CommUtil.isNotNull(iobl)){
			property.setIssucc(E_YES___.YES);
			property.setMntrsq(iobl.getTransq());
			output.setMntrdt(iobl.getTrandt());	//
			output.setMntrsq(iobl.getTransq()); // 主流水
			output.setMntrtm(iobl.getTrantm());
			return;
		}else{
			property.setIssucc(E_YES___.NO);
		}
		
		//入参检查
		chkParam(input);
		
		
		E_CAPITP capitp = input.getCapitp();
		
		IoCaKnaAcdc otacdc = ActoacDao.selKnaAcdc(input.getOtcard(), false);
		if(CommUtil.isNull(otacdc)){
			throw DpModuleError.DpstComm.BNAS0750();
		}
		
		if(otacdc.getStatus() == E_DPACST.CLOSE){
			throw DpModuleError.DpstComm.BNAS0441();
		}
		//转出电子账户信息
		
		//String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//		CommTools.getBaseRunEnvs().setBusi_org_id(otacdc.getCorpno());
		
		//状态、类型、状态字检查
		E_ACCATP accatp = CommTools.getRemoteInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(otacdc.getCustac());
		AcTranfeChkIN chkIN = SysUtil.getInstance(AcTranfeChkIN.class);
		chkIN.setAccatp(accatp);
		chkIN.setCardno(input.getOtcard()); //电子账户卡号
		chkIN.setCustac(otacdc.getCustac()); //电子账号ID 
		chkIN.setCustna(input.getOtacna());
		chkIN.setCapitp(capitp);
		chkIN.setOpcard(input.getIncard());
		//chkIN.setOppoac(input.getIncard());
		chkIN.setOppona(input.getInacna());
		chkIN.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		
		CapitalTransCheck.ChkAcctstOT(otacdc.getCustac());
		AcTranfeChkOT chkOT = CapitalTransCheck.chkTranfe(chkIN);
		
		//E_CUACST cuacst = chkOT.getCuacst();
		
		//获取结算户或钱包户
		KnaAcct otacct = SysUtil.getInstance(KnaAcct.class); //转出方子账号
		if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){
			otacct = CapitalTransDeal.getSettKnaAcctSub(otacdc.getCustac(), E_ACSETP.SA);
			
			input.getChkqtn().setSbactp(E_SBACTP._11);
			if(CommUtil.isNotNull(input.getOtcstp()) && input.getOtcstp() != E_ACSETP.SA){
				throw DpModuleError.DpstComm.BNAS0040();
			}
		}else{
			otacct = CapitalTransDeal.getSettKnaAcctSub(otacdc.getCustac(), E_ACSETP.MA);
			
			input.getChkqtn().setSbactp(E_SBACTP._12);
			if(CommUtil.isNotNull(input.getOtcstp()) && input.getOtcstp() != E_ACSETP.MA){
				throw DpModuleError.DpstComm.BNAS0040();
			}
		}
		
		if(CommUtil.isNotNull(input.getOtacna()) && !CommUtil.equals(input.getOtacna(), otacct.getAcctna())){
			throw DpModuleError.DpstComm.BNAS0892();
		}
		
		//额度参数设置
		input.getChkqtn().setAccttp(accatp);
		input.getChkqtn().setCustac(otacdc.getCustac());
		input.getChkqtn().setBrchno(otacct.getBrchno());// 交易机构号
		input.getChkqtn().setCustie(chkOT.getIsbind()); //是否绑定卡标识
		input.getChkqtn().setFacesg(chkOT.getFacesg());	//是否面签标识
		input.getChkqtn().setRecpay(null); //收付方标识，电子账户转电子账户需要输入
		
		//币种校验
		if(!CommUtil.equals(input.getCrcycd(), otacct.getCrcycd())){
			throw DpModuleError.DpstComm.BNAS0632();
		}
		//检查转出机构与电子账户机构是否一致
		if(CommUtil.isNotNull(input.getOtbrch())){
			if(!CommUtil.equals(input.getOtbrch(), otacct.getBrchno())){
				throw DpModuleError.DpstComm.BNAS0043();
			}
		}
		
		
		
		//3.转出方可用余额校验
		BigDecimal realam = input.getTranam(); //该金额用于校验可用余额是否满足交易金额和费用的总额
		if(accatp == E_ACCATP.WALLET){ //还信用卡允许使用三类户，需要校验三类户金额
			if(CommUtil.compare(otacct.getOnlnbl(), realam) < 0){
				throw DpModuleError.DpstComm.BNAS0442();
			}
		}else{
			//BigDecimal usebal = DpAcctProc.getProductBal(otacdc.getCustac(), input.getCrcycd(), false);
			
			// 可用余额 addby xiongzhao 20161223 
			BigDecimal usebal = CommTools.getRemoteInstance(DpAcctSvcType.class)
					.getAcctaAvaBal(otacdc.getCustac(), otacct.getAcctno(),
							otacct.getCrcycd(), E_YES___.YES, E_YES___.NO);
			
			if(CommUtil.compare(usebal, realam) < 0){
				throw DpModuleError.DpstComm.BNAS0442();
			}
		}
		
		log.debug("<<==电子账户：[%s],电子账户类型：[%s],子账号：[%s],交易类型：[%s]==>>",input.getIncard(),accatp.getLongName(),otacct.getAcctno(),capitp.getLongName());
		
		property.setOtcsac(otacct.getCustac()); //电子账号ID
		property.setOtchld(otacct.getAcctno()); //子账号
//		CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);
		
		//设置收费参数
		if(CommUtil.compare(input.getTlcgam(), BigDecimal.ZERO) > 0){
			property.setIschrg(E_YES___.YES); //收费金额大于0才使用公共收费
			property.setChgflg(E_CHGFLG.ALL); //设置记账标志
		}else{
			property.setIschrg(E_YES___.NO); //初始化收费的参数
		}
		
		property.setIncorp("");
		property.setOtcorp("");
//		property.setBusisq(CommTools.getBaseRunEnvs().getBstrsq()); //业务跟踪编号 
		property.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); //交易渠道 
		property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //交易流水
		property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期 
		property.setFrondt(CommTools.getBaseRunEnvs().getInitiator_date());//渠道来源日期 
		//渠道来源日期 
		property.setFronsq(CommTools.getBaseRunEnvs().getInitiator_seq()); //渠道来源日期 
		property.setLinkno(null); //连笔号
		property.setPrcscd("tranot"); //交易码
		property.setDscrtx(capitp.getLongName()); //描述
		property.setIoflag(E_IOFLAG.OUT);
		//出金
		property.setTrantp(E_TRANTP.TR); //交易类型 
		property.setBrchno(otacct.getBrchno());
		
		
		if(capitp == E_CAPITP.OT220){//电子账户银联全渠道转出
			property.setClactp(E_CLACTP._19);
		}else if(capitp == E_CAPITP.OT222){//电子账户通联代付转出
			property.setClactp(E_CLACTP._17);
		}else if(capitp == E_CAPITP.OT226){//电子账户通联金融转出
			property.setClactp(E_CLACTP._24);
		}else if(capitp == E_CAPITP.OT227){//电子账户兴业银行转出
			property.setClactp(E_CLACTP._26);
		}else{
			throw DpModuleError.DpstAcct.BNAS0207();
		}
		
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", property.getClactp().getValue(), "%", true);
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();
		//add by sh 20170929 金谷项目，内部户记账机构为交易机构
		//String acbrch = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(brchno).getBrchno();
		//property.setAcbrch(acbrch); //省中心
		property.setAcbrch(brchno);
		property.setBusino(para.getParm_value1()); //业务编码
		property.setSubsac(para.getParm_value2());//子户号
		
		//poc增加审计日志
        KnaAcdc kacdc=KnaAcdcDao.selectFirst_odb1(otacct.getCustac(), E_DPACST.NORMAL, false);
        if(CommUtil.isNotNull(kacdc)){
        	ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
    		apAudit.regLogOnInsertBusiPoc(kacdc.getCardno());
        }
	}

	/**
	 * @Title: DealTransAfter 
	 * @Description: 交易后处理  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年12月13日 下午2:51:56 
	 * @version V2.3.0
	 */
	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranot.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranot.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranot.Output output){
		if(property.getIssucc() == E_YES___.NO){
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),property.getClactp());
			
			output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
			output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
		}
	}
	
	/**
	 * @Title: chkParam 
	 * @Description: 输入参数检查  
	 * @param input
	 * @return
	 * @author zhangan
	 * @date 2016年12月13日 下午1:52:33 
	 * @version V2.3.0
	 */
	public static BigDecimal chkParam(final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranot.Input input){
		
		BigDecimal tlcgam = BigDecimal.ZERO;
		
		// 输入项非空检查
		if (CommUtil.isNull(input.getCapitp())) {
			throw DpModuleError.DpstComm.BNAS0023();
		}
		
		E_CAPITP capitp = input.getCapitp();
		//交易范围检查
		//转借记卡、转贷记卡(还本行信用卡)、银联在线转出、还贷款
		//add by xionglz 增加通联金融与兴业银行转出渠道
		if(capitp != E_CAPITP.OT220 && capitp != E_CAPITP.OT222
				&&capitp!=E_CAPITP.OT226&&capitp!=E_CAPITP.OT227){
			throw DpModuleError.DpstComm.BNAS1188(capitp.getLongName());

		}
		
		if (CommUtil.isNull(input.getCrcycd())) {
			throw DpModuleError.DpstComm.BNAS1101();
		}
		if (CommUtil.isNull(input.getCsextg())) {
			throw DpModuleError.DpstComm.BNAS1047();
		}
		if (CommUtil.isNull(input.getTranam())) {
			throw DpModuleError.DpstAcct.BNAS0623();
		}
		if (CommUtil.isNull(input.getSmrycd())) {
			throw DpModuleError.DpstComm.BNAS0195();
		}
		if (CommUtil.isNull(input.getTlcgam())) {
			throw DpModuleError.DpstComm.BNAS0331();
		}
//		if (CommUtil.isNull(input.getChckdt())) {
//			throw DpModuleError.DpstComm.E9027("对账日期");
//		}
//		if (CommUtil.isNull(input.getKeepdt())) {
//			throw DpModuleError.DpstComm.E9027("清算日期");
//		}
		if (CommUtil.isNull(input.getChkqtn().getIsckqt())) {
			throw DpModuleError.DpstComm.BNAS0802();
		}
		
		if(CommUtil.isNull(input.getIncard())){
			throw DpModuleError.DpstComm.BNAS0030();
		}
		if(CommUtil.isNull(input.getInacna())){
			throw DpModuleError.DpstComm.BNAS0032();
		}
		if(CommUtil.isNull(input.getOtcard())){
			throw DpModuleError.DpstComm.BNAS0042();
		}
		if(CommUtil.isNull(input.getOtacna())){
			throw DpModuleError.DpstComm.BNAS0045();
		}
		
		if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0){ //校验交易金额
			throw DpModuleError.DpstComm.BNAS0627();
		}
		if(CommUtil.compare(input.getTlcgam(), BigDecimal.ZERO) < 0){ //校验收费总金额
			throw DpModuleError.DpstComm.BNAS0328();
		}
		
		//收费参数检查
		if(CommUtil.compare(input.getTlcgam(),BigDecimal.ZERO) > 0){
			// 收费交易金额检查
			if(input.getChrgpm().size() <= 0){
				throw DpModuleError.DpstComm.BNAS0395();
			}
			BigDecimal totPaidam = BigDecimal.ZERO;
			for (IoCgCalCenterReturn IoCgCalCenterReturn : input.getChrgpm()) {
				BigDecimal tranam = IoCgCalCenterReturn.getTranam();// 交易金额
				BigDecimal clcham = IoCgCalCenterReturn.getClcham();// 应收费用金额（未优惠）
				BigDecimal dircam = IoCgCalCenterReturn.getDircam();// 优惠后应收金额
				BigDecimal paidam = IoCgCalCenterReturn.getPaidam();// 实收金额
				
				if (CommUtil.isNotNull(tranam)) {
					if(CommUtil.compare(tranam, BigDecimal.ZERO) < 0){ //交易金额金额
						throw DpModuleError.DpstComm.BNAS0338();
					}
					if(CommUtil.compare(tranam, input.getTranam()) != 0){ //交易金额金额
						throw DpModuleError.DpstComm.BNAS0337();
					}
				}
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
				
				
				totPaidam = totPaidam.add(paidam);
			}
			
			if(!CommUtil.equals(totPaidam, input.getTlcgam())){
				throw DpModuleError.DpstComm.BNAS0243();
			}
		}
		
		
		
		return tlcgam;
		
	}

	public static void sendAppTranotInfoMsg(
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranot.Input input,  
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranot.Property property,  
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranot.Output output) {
	        
	      
//			MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//			mri.setMtopic("Q0101005");
			ToAppSendMsg AppSendMsgInput = SysUtil.getInstance(ToAppSendMsg.class);
			//根据卡号查询电子账号
			IoCaKnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCard(input.getOtcard(), false);
			 String custac = tblKnaAcdc.getCustac();
			 //查询电子账户信息
			 CaCustInfo.accoutinfos accoutinfos = SysUtil.getInstance(CaCustInfo.accoutinfos.class);
			 accoutinfos = EacctMainDao.selCustInfobyCustac(custac,
						E_ACALST.NORMAL, E_ACALTP.CELLPHONE, false);
			AppSendMsgInput.setUserId(accoutinfos.getCustid()); // 用户ID
			AppSendMsgInput.setOutNoticeId("Q0101005");//外部消息ID
			AppSendMsgInput.setNoticeTitle("资金变动");//公告标题
			String date  = CommTools.getBaseRunEnvs().getTrxn_date().substring(0,4)+"年"
			+CommTools.getBaseRunEnvs().getTrxn_date().substring(4,6)+"月"+ 
					CommTools.getBaseRunEnvs().getTrxn_date().substring(6,8)+"日"+
					BusiTools.getBusiRunEnvs().getTrantm().substring(0,2)+":"+
					BusiTools.getBusiRunEnvs().getTrantm().substring(2,4)+":"+
					BusiTools.getBusiRunEnvs().getTrantm().substring(4,6);
			
			/*AppSendMsgInput.setContent("您的电子账户于"+date+"支出金额:"+
			   input.getTranam()+"元,"+"对方户名:"+input.getInacna()+",请点击查看详情。"); //内容
			   			   			   
*/			StringBuffer sb=new StringBuffer();
        //获取可用余额
            IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);
            BigDecimal acctbl = BigDecimal.ZERO;
            acctbl = SysUtil.getInstance(DpAcctSvcType.class).
                    getAcctaAvaBal(cplKnaAcct.getCustac(), cplKnaAcct.getAcctno(),
                            input.getCrcycd(), E_YES___.YES, E_YES___.NO);
            sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易时间：").append(date).
                append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易类型：").append("提现").
                append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易金额：").append(input.getTranam()+"元").
                append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;可用余额：").append(acctbl+"元").
                append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;对方账户：").append(input.getInacna()).
                append("请点击查看详情。");
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
						
		

	  }
}
