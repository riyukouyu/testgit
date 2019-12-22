package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsq;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIobl;
import cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Output.Acvoch;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcctDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkOT;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ISPAYA;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;



public class tranin {
	

	/**
	 * @Title: DealTransBefore 
	 * @Description:交易前检查  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年12月9日 上午10:38:43 
	 * @version V2.3.0
	 */
	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Output output){
		
		
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
		
		IoCaKnaAcdc inacdc = ActoacDao.selKnaAcdc(input.getIncard(), false);
		if(CommUtil.isNull(inacdc)){
			throw DpModuleError.DpstComm.BNAS0750();
		}
		
		if(inacdc.getStatus() == E_DPACST.CLOSE){
			throw DpModuleError.DpstComm.BNAS0441();
		}
		
		//String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//		CommTools.getBaseRunEnvs().setBusi_org_id(inacdc.getCorpno());
        /**
         * 2017.12.18
         * 检查客户化账户状态
         * */
		E_CUACST checkcuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(inacdc.getCustac());
		
		if(checkcuacst == E_CUACST.OUTAGE){
		    throw DpModuleError.DpstComm.BNAS0850();
		}
		
		//状态、类型、状态字检查
		//获取客户账户类型
		E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(inacdc.getCustac());
		AcTranfeChkIN chkIN = SysUtil.getInstance(AcTranfeChkIN.class);
		chkIN.setAccatp(accatp);
		chkIN.setCardno(input.getIncard()); //电子账号卡号
		chkIN.setCustac(inacdc.getCustac()); //电子账号ID
		chkIN.setCustna(input.getInacna());
		chkIN.setCapitp(capitp);
		chkIN.setOpcard(input.getOtcard());
		//chkIN.setOppoac(input.getOtcard());
		chkIN.setOppona(input.getOtacna());
		chkIN.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		
		AcTranfeChkOT chkOT = CapitalTransCheck.chkTranfe(chkIN);
		
		E_CUACST cuacst = chkOT.getCuacst();
		
		KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
		//获取转入子账号
		if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //结算户
			tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(inacdc.getCustac(), E_ACSETP.SA);
			input.getChkqtn().setSbactp(E_SBACTP._11);
			if(CommUtil.isNotNull(input.getIncstp()) && input.getIncstp() != E_ACSETP.SA){
				throw DpModuleError.DpstComm.BNAS0029();
			}
		}else{ // 钱包户
			tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(inacdc.getCustac(), E_ACSETP.MA);
			input.getChkqtn().setSbactp(E_SBACTP._12);
			if(CommUtil.isNotNull(input.getIncstp()) && input.getIncstp() != E_ACSETP.MA){
				throw DpModuleError.DpstComm.BNAS0029();
			}
		}
		
		if(CommUtil.isNotNull(input.getInacna()) && !CommUtil.equals(input.getInacna(), tblKnaAcct.getAcctna())){
			throw DpModuleError.DpstComm.BNAS0892();
		}
		
		//设置额度中心参数
		input.getChkqtn().setAccttp(accatp);
		input.getChkqtn().setCustac(inacdc.getCustac());
		input.getChkqtn().setBrchno(tblKnaAcct.getBrchno());// 交易机构号
		input.getChkqtn().setCustie(chkOT.getIsbind()); //是否绑定卡标识
		input.getChkqtn().setFacesg(chkOT.getFacesg());	//是否面签标识
		input.getChkqtn().setRecpay(null); //收付方标识，电子账户转电子账户需要输入
		
		//币种校验
		if(!CommUtil.equals(input.getCrcycd(), tblKnaAcct.getCrcycd())){
			throw DpModuleError.DpstComm.BNAS0632();
		}
		//机构检查
		if(CommUtil.isNotNull(input.getInbrch())){
			if(!CommUtil.equals(input.getInbrch(), tblKnaAcct.getBrchno())){
				throw DpModuleError.DpstComm.BNAS0031();
			}
		}
		
		
//		CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);
		
		//费用参数初始化设置
		if(CommUtil.compare(input.getTlcgam(), BigDecimal.ZERO) > 0){
			property.setIschrg(E_YES___.YES); //收费金额大于0才使用公共收费
			property.setChgflg(E_CHGFLG.ALL); //设置记账标志
		}
		
		property.setIncsac(tblKnaAcct.getCustac()); //电子账号ID
		property.setInchld(tblKnaAcct.getAcctno()); //子账号
		property.setTblKnaAcct(tblKnaAcct);
		property.setCuacst(cuacst);
		property.setInactp(accatp);
		//4.设置属性值
		property.setIncorp("");
		property.setOtcorp("");
//		property.setBusisq(CommTools.getBaseRunEnvs().getBstrsq()); //业务跟踪编号 
		property.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); //交易渠道 
		property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //渠道来源流水
		property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date()); //渠道来源日期 
		property.setFrondt(CommTools.getBaseRunEnvs().getInitiator_date());//渠道来源日期 
		property.setFronsq(CommTools.getBaseRunEnvs().getInitiator_seq()); //渠道来源日期
		property.setLinkno(null); //连笔号
		property.setPrcscd("tranin"); //交易码
		property.setDscrtx(capitp.getLongName()); //描述
		property.setTrantp(E_TRANTP.TR); //交易类型 
		property.setIoflag(E_IOFLAG.IN);
		property.setBrchno(tblKnaAcct.getBrchno());
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();
		//add by sh 20170929 金谷项目，内部户记账机构为交易机构
		//String acbrch = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(brchno).getBrchno();
		//property.setAcbrch(acbrch); //省中心
		property.setAcbrch(brchno);
		
		if(capitp == E_CAPITP.IN120){//银联全渠道转电子账户
			property.setClactp(E_CLACTP._18);
		}else if(capitp == E_CAPITP.IN122){//通联代扣转电子账户
			property.setClactp(E_CLACTP._16);
		}else if(capitp == E_CAPITP.IN127){//通联金融代扣转电子账户
			property.setClactp(E_CLACTP._25);
		}else if(capitp == E_CAPITP.IN128){//兴业银行代扣转电子账户
			property.setClactp(E_CLACTP._27);
		}else{
			throw DpModuleError.DpstAcct.E9999("暂不支持的记账类型!");
		}
		
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", property.getClactp().getValue(), "%", true);
		
		property.setBusino(para.getParm_value1()); //业务编码
		property.setSubsac(para.getParm_value2());//子户号		
	}

	/**
	 * @Title: DealAcctStatAndSett 
	 * @Description:休眠户转正常结息并修改账户状态  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年12月9日 上午10:39:35 
	 * @version V2.3.0
	 */
	public static void DealAcctStatAndSett( final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Output output){
		//修改账户状态，休眠转正常结息
		//转入账户的电子账户信息，转入账户的结算户信息或钱包户信息
		CapitalTransDeal.dealAcctStatAndSett(property.getCuacst(), property.getTblKnaAcct());
	}

	/**
	 * @Title: DealTransAfter 
	 * @Description:  交易后处理
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年12月9日 上午10:40:11 
	 * @version V2.3.0
	 */
	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Output output){
		
		if(property.getIssucc() == E_YES___.NO){
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),property.getClactp());
			List<KnsAcsq> listAcsq = InacSqlsDao.seltransq(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getTrxn_seq(),true);
			for(KnsAcsq tbKnsAcsq:listAcsq){//会计凭证供打印使用
				Acvoch acvoch = SysUtil.getInstance(Acvoch.class);
				acvoch.setTranam(tbKnsAcsq.getTranam());
				acvoch.setAmntcd(tbKnsAcsq.getAmntcd());
				acvoch.setDtitcd(tbKnsAcsq.getDtitcd());
				acvoch.setCrcycd(tbKnsAcsq.getCrcycd());
				if(CommUtil.compare(ApAcctRoutTools.getRouteType(tbKnsAcsq.getAcctno()),E_ACCTROUTTYPE.INSIDE)==0){//内部户账号
					GlKnaAcct tbGlKnaAcct = GlKnaAcctDao.selectOne_odb1(tbKnsAcsq.getAcctno(), true);
					acvoch.setAcctno(tbKnsAcsq.getAcctno());
					acvoch.setAcctna(tbGlKnaAcct.getAcctna());
					acvoch.setIspaya(tbGlKnaAcct.getIspaya());
				}else{//客户账
					acvoch.setAcctno(input.getIncard());
					acvoch.setAcctna(input.getInacna());
					acvoch.setIspaya(E_ISPAYA._0);
				}
				output.getAcvoch().add(acvoch);
			}
			output.setSmrycd(input.getSmrycd());
			output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
			output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
		}
		
	}
	
	/**
	 * @Title: chkParam 
	 * @Description: 交易前参数检查  
	 * @param input
	 * @return
	 * @author zhangan
	 * @date 2016年12月9日 上午10:06:15 
	 * @version V2.3.0
	 */
	private static BigDecimal chkParam(final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Input input){
		
		// 输入项非空检查
		if (CommUtil.isNull(input.getCapitp())) {
			throw DpModuleError.DpstComm.BNAS0023();
		}
		
		E_CAPITP capitp = input.getCapitp();
		//交易范围检查
		//借记卡转入、贷记卡转入、银联在线转入、内部户转入
		//add by xionglz 增加通联金融代扣转电子账户，兴业银行代扣转电子账户
		if(capitp != E_CAPITP.IN120 && capitp != E_CAPITP.IN122 && capitp != E_CAPITP.IN108
				&& capitp != E_CAPITP.IN127&& capitp != E_CAPITP.IN128){
			throw DpModuleError.DpstComm.BNAS1188(capitp.getLongName() );
		}
		
		if (CommUtil.isNull(input.getCrcycd())) {
			throw DpModuleError.DpstComm.BNAS1101();
		}
		if (CommUtil.isNull(input.getCsextg())) {
			throw DpModuleError.DpstComm.BNAS1047();
		}
		if (CommUtil.isNull(input.getTranam())) {
			throw DpModuleError.DpstProd.BNAS0620();
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
		
		
		return BigDecimal.ZERO;
	}

	/**
	 * 短信通知
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void sendAppTraninInfoMsg(
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Input input,  
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Property property,  
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Output output) {
		
//			MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//			mri.setMtopic("Q0101005");
//			ToAppSendMsg AppSendMsgInput = SysUtil.getInstance(ToAppSendMsg.class);
//			//根据卡号查询电子账号
//			IoCaKnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCard(input.getIncard(), false);
//			 String custac = tblKnaAcdc.getCustac();
//			 //查询电子账户信息
//			 CaCustInfo.accoutinfos accoutinfos = SysUtil.getInstance(CaCustInfo.accoutinfos.class);
//			 accoutinfos = EacctMainDao.selCustInfobyCustac(custac,
//						E_ACALST.NORMAL, E_ACALTP.CELLPHONE, false);
//			 
//			AppSendMsgInput.setUserId(accoutinfos.getCustid()); // 用户ID
//			AppSendMsgInput.setOutNoticeId("Q0101005");//外部消息ID
//			AppSendMsgInput.setNoticeTitle("资金变动");//公告标题
//			String date  = CommTools.getBaseRunEnvs().getTrxn_date().substring(0,4)+"年"
//			+CommTools.getBaseRunEnvs().getTrxn_date().substring(4,6)+"月"+ 
//					CommTools.getBaseRunEnvs().getTrxn_date().substring(6,8)+"日"+
//					BusiTools.getBusiRunEnvs().getTrantm().substring(0,2)+":"+
//					BusiTools.getBusiRunEnvs().getTrantm().substring(2,4)+":"+
//					BusiTools.getBusiRunEnvs().getTrantm().substring(4,6);
//		/*	
//			AppSendMsgInput.setContent("您的ThreeBank电子账户于"+date+"收入金额:"+
//			   input.getTranam()+"元,"+"对方户名:"+input.getInacna()+",请点击查看详情。"); //内容
//*/			
//			 StringBuffer sb=new StringBuffer();
//		        //获取可用余额
//		            IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);
//		            BigDecimal acctbl = BigDecimal.ZERO;
//		            acctbl = SysUtil.getInstance(DpAcctSvcType.class).
//		                    getAcctaAvaBal(cplKnaAcct.getCustac(), cplKnaAcct.getAcctno(),
//		                            input.getCrcycd(), E_YES___.YES, E_YES___.NO);
//		            sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易时间：").append(date).
//		                append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易类型：").append("充值").
//		                append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易金额：").append(input.getTranam()+"元").
//		                append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;可用余额：").append(acctbl+"元").
//		                append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;对方账户：").append(input.getInacna()).
//		                append("请点击查看详情。");
//		            AppSendMsgInput.setContent(sb.toString());
//			
//		
//			AppSendMsgInput.setSendtime(CommTools.getBaseRunEnvs().getTrxn_date());//消息生成时间
//			AppSendMsgInput.setClickType(cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_CLIKTP.YES);
//			AppSendMsgInput.setClickValue("LOGINURL||/page/electronicAcct/bill/electAcctBill.html");//点击动作值
//			AppSendMsgInput.setTirggerSys(CommTools.getBaseRunEnvs().getSystcd());//触发系统
//			AppSendMsgInput.setTransType(cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_APPTTP.RECHARGE);
//			
//			
//			mri.setMsgtyp("ApSmsType.ToAppSendMsg");
//			mri.setMsgobj(AppSendMsgInput); 
//			AsyncMessageUtil.add(mri);  

	}

	/**
	 * 生成清算记录
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void dealCler( final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranin.Output output){
		//TODO
	}
}
