package cn.sunline.ltts.busi.dptran.trans.close;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.namedsql.AccChngbrDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ClsAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcal;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbClac;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseIN;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSATP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSTAT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;



public class cltrfe {

	/**
	 * @Title: dealTransBefore 
	 * @Description:  销户转账（银联，本行、内部户）
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年10月25日 上午10:37:13 
	 * @version V2.3.0
	 */
	public static void dealTransBefore( final cn.sunline.ltts.busi.dptran.trans.close.intf.Cltrfe.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Cltrfe.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Cltrfe.Output output){
		
		BigDecimal tlcgam = chkParam(input);
		
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();
		//检查账户是否是预销户
		IoCaKnaAcdc acdc = ActoacDao.selKnaAcdc(input.getCardno(), false);
		if(CommUtil.isNull(acdc)){
			throw DpModuleError.DpstComm.BNAS0750();
		}
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(acdc.getCustac());
		if(cuacst != E_CUACST.PRECLOS){
			throw DpModuleError.DpstComm.BNAS0851();
		}
		
		IoCaKnbClac cplKnbClac = DpAcctDao.selKnbClacByClossq(input.getPyinsq(), false);
		if(CommUtil.isNull(cplKnbClac)){
			throw DpModuleError.DpstComm.BNAS0214();
		}
		
		//修改销户登记簿状态为成功
		CommTools.getRemoteInstance(DpAcctSvcType.class).updKnbClacStat(input.getPyinsq(), acdc.getCustac(), E_CLSTAT.SUCC);
		
		
		E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(acdc.getCustac());
		
		KnaAcct tblKnaAcct = ActoacDao.selKnaAcct(cplKnbClac.getAcctno(), true);
		
		//新增机构校验限制，业务需求，如果渠道是后管不校验交易机构
		if(!CommUtil.equals(CommTools.getBaseRunEnvs().getChannel_id(), "EB")){
			if(!CommUtil.equals(tblKnaAcct.getBrchno(),brchno)){
				throw DpModuleError.DpstComm.BNAS0887();
			}
		}
		
		
		if(CommUtil.isNotNull(input.getSettbl())){
			if(!CommUtil.equals(input.getSettbl(), tblKnaAcct.getOnlnbl())){
				throw DpModuleError.DpstComm.BNAS0594();
			}
		}
		if(CommUtil.compare(input.getCrcycd(), tblKnaAcct.getCrcycd())!=0){
			throw DpModuleError.DpstComm.E9999("币种["+input.getCrcycd()+"]与账户币种["+tblKnaAcct.getCrcycd()+"]不一致");
		}
		
		if(input.getChaglg() == E_YES___.YES){
			if(CommUtil.compare(tblKnaAcct.getOnlnbl(), tlcgam) < 0){
				throw DpModuleError.DpstComm.BNAS0334();
			}
		}
		
		//add 2016/12/26 songlw 获取客户手机号
		IoCaKnaAcal cplKnaAcal = AccChngbrDao.selKnaacalByCus(acdc.getCustac(), E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);
		if(CommUtil.isNotNull(cplKnaAcal)){
			property.setAcalno(cplKnaAcal.getAcalno());
		}
		
		//IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_accsByCustno(tblKnaAcct.getCustno(), true, E_STATUS.NORMAL);
		property.setCustid(tblKnaAcct.getCustno());
		
		property.setTblKnaAcct(tblKnaAcct);
		property.setAccatp(accatp);
		property.setChgflg(E_CHGFLG.ALL);
		property.setCsextg(E_CSEXTG.CASH);
		property.setToscac(acdc.getCustac());
		property.setDrawwy(cplKnbClac.getDrawwy());
		property.setClosbr(cplKnbClac.getClosbr());
		property.setClosus(cplKnbClac.getClosus());
		property.setClossv(cplKnbClac.getClossv());
	}
	
	/**
	 * @Title: prcAccount 
	 * @Description: 转出记账并注销电子账户 
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年10月25日 下午4:22:50 
	 * @version V2.3.0
	 */
	public static void prcAccount( final cn.sunline.ltts.busi.dptran.trans.close.intf.Cltrfe.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Cltrfe.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Cltrfe.Output output){
		
		KnaAcct tblKnaAcct = property.getTblKnaAcct();
		BigDecimal onlbal = BigDecimal.ZERO;
		E_CLACTP clactp = E_CLACTP._01;
		if(input.getTractp() == E_CLSATP.LOCAL 
				|| input.getTractp() == E_CLSATP.INACCT){
			clactp = E_CLACTP._01;
		}else if( input.getTractp() == E_CLSATP.CRECA){
			clactp = E_CLACTP._09;
		}else if(input.getTractp() == E_CLSATP.OTHER){
			clactp = E_CLACTP._03;
		}
		KnpParameter para2 = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", clactp.getValue(), "%", true);
		
//		if(CommUtil.compare(tranam, BigDecimal.ZERO) > 0){
//			DrawDpAcctIn drawIn = SysUtil.getInstance(DrawDpAcctIn.class); //电子账户支取记账复合类型
//			drawIn.setAcctno(tblKnaAcct.getAcctno());
//			drawIn.setAcseno(null);
//			drawIn.setAuacfg(E_YES___.NO);
//			drawIn.setBankcd(null);
//			drawIn.setBankna(null);
//			drawIn.setCardno(input.getCardno());
//			drawIn.setCrcycd(tblKnaAcct.getCrcycd());
//			drawIn.setCustac(tblKnaAcct.getCustac());
//			drawIn.setIschck(E_YES___.NO); //不检查支取控制，无条件支取
//			drawIn.setOpacna(input.getTracna());
//			drawIn.setOpbrch(input.getTrbkno());
//			drawIn.setRemark("销户转电子账户记账");
//			drawIn.setSmrycd(input.getSmrycd());
//			drawIn.setSmryds("");
//			drawIn.setStrktg(E_YES___.NO); //本交易不提供冲正
//			drawIn.setToacct(input.getTracno());
//			drawIn.setTranam(tranam);
//			SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawIn);
		IoDpCloseIN clsin = SysUtil.getInstance(IoDpCloseIN.class);
		
		clsin.setCardno(input.getCardno());
		clsin.setToacct(input.getTracno());
		clsin.setToname(input.getTracna());
		clsin.setTobrch(input.getTrbkno());
		clsin.setSmrycd(BusinessConstants.SUMMARY_XH);
		if (CommUtil.isNotNull(property.getClossv())) {
			//clsin.setRemark(property.getClossv().getLongName()+"销户");
			clsin.setRemark("销户");
		}
		
		//onlbal = DpCloseAcctno.prcCurrOnbal(tblKnaAcct, clsin);
		
		onlbal = CommTools.getRemoteInstance(DpAcctSvcType.class).closeAcctno(tblKnaAcct.getAcctno(), clsin);
		
		if(CommUtil.compare(onlbal, BigDecimal.ZERO) > 0){
			//内部户贷方记账服务
			String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();
			String acbrch = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(brchno).getBrchno();
			
			IaAcdrInfo acdrIn2 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
			
			acdrIn2.setAcbrch(acbrch); //挂账所属机构挂电子账户机构
			acdrIn2.setTrantp(E_TRANTP.TR); //交易类型 
			acdrIn2.setCrcycd(tblKnaAcct.getCrcycd());
			acdrIn2.setSmrycd(BusinessConstants.SUMMARY_XH);
			acdrIn2.setToacct(tblKnaAcct.getAcctno());
			acdrIn2.setToacna(tblKnaAcct.getAcctna());
			acdrIn2.setTranam(onlbal);
			acdrIn2.setBusino(para2.getParm_value1()); //业务编码
			acdrIn2.setSubsac(para2.getParm_value2());//子户号
			
			IaTransOutPro acdrOt = CommTools.getRemoteInstance(IoInAccount.class).ioInAccr(acdrIn2); //贷记服务
			
			//平衡性检查
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
		}
		//}
		
		//add by chenlk 20161119  增加柜员操作额度校验
/*		if(CommUtil.compare(input.getSettbl(), BigDecimal.ZERO)>0 && BusiTools.isCounterChannel()){
			//机构、柜员额度验证
			IoBrchUserQt ioBrchUserQt = SysUtil.getInstance(IoBrchUserQt.class);
			ioBrchUserQt.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
			ioBrchUserQt.setBusitp(E_BUSITP.TR);
			ioBrchUserQt.setCrcycd(input.getCrcycd());
			ioBrchUserQt.setTranam(input.getSettbl());
			ioBrchUserQt.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());
			SysUtil.getInstance(IoSrvPbBranch.class).selBrchUserQt(ioBrchUserQt);			
			
		}*/
		
		ClsAcctIn cplClsAcctIn = SysUtil.getInstance(ClsAcctIn.class);
		
		cplClsAcctIn.setCardno(input.getCardno());
		cplClsAcctIn.setCustac(tblKnaAcct.getCustac());
		cplClsAcctIn.setCustna(tblKnaAcct.getAcctna());
		cplClsAcctIn.setCustno(tblKnaAcct.getCustno());
		cplClsAcctIn.setClossq(input.getPyinsq());
		//注销电子账户
		CommTools.getRemoteInstance(DpAcctSvcType.class).acctStatusUpd(cplClsAcctIn);
		
		IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
		cplDimeInfo.setCustac(tblKnaAcct.getCustac());
		cplDimeInfo.setDime01(input.getTractp().getValue()); //收款人账户类型
		CommTools.getRemoteInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);
		
		property.setAcctno(input.getTracno());
		property.setBrchno(tblKnaAcct.getBrchno());
		property.setBusino(para2.getParm_value1());
		
		
		if(input.getTractp() == E_CLSATP.LOCAL){
			property.setCapitp(E_CAPITP.OT201);
		}else if(input.getTractp() == E_CLSATP.INACCT){
			property.setCapitp(E_CAPITP.OT201);
		}else if(input.getTractp() == E_CLSATP.CRECA){
			property.setCapitp(E_CAPITP.OT202);
		}else{
			property.setCapitp(E_CAPITP.OT203);
		}
		property.setCuacno(input.getTracno());
		property.setFromtp(E_FROMTP.A);
		property.setIoflag(E_IOFLAG.OUT);
		property.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());
		property.setRemark("");
		property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());
		property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		property.setToscac(tblKnaAcct.getCustac());
		property.setToacno(tblKnaAcct.getAcctno());
		property.setTranam(onlbal);
		
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
		
	}
	
	/**
	 * @Title: sendCloseInfoMsg 
	 * @Description:发短信和消息  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年10月26日 下午7:51:31 
	 * @version V2.3.0
	 */
	public static void sendCloseInfoMsg( final cn.sunline.ltts.busi.dptran.trans.close.intf.Cltrfe.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Cltrfe.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Cltrfe.Output output){
		// 查询销户登记簿处理状态为成功的记录
				//IoCaKnbClac cplKnbClac = DpAcctDao.selKnbClac(property.getCustac(),E_CLSTAT.SUCC, false);

//		E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介
		//IoCucifCust cplCifCust = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_selectOne_odb1(property.getTblKnaAcct().getCustno(), true);
//		IoSrvCfPerson.IoGetCifCust.InputSetter queryCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//		queryCust.setCustno(property.getTblKnaAcct().getCustno());
//		IoSrvCfPerson.IoGetCifCust.Output cplCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//		SysUtil.getInstance(IoSrvCfPerson.class).getCifCust(queryCust, cplCifCust);
		
		
		//修改销户cmq通知  modify lull
//		MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//		mri.setMtopic("Q0101004");
//		IoCaCloseAcctSendMsg closeSendMsgInput = SysUtil.getInstance(IoCaCloseAcctSendMsg.class);
//		closeSendMsgInput.setCustid(property.getCustid()); // 用户ID
//		closeSendMsgInput.setBrchno(property.getClosbr());// 操作机构
//		closeSendMsgInput.setUserid(property.getClosbr());// 操作柜员
//		closeSendMsgInput.setClossv(CommTools.getBaseRunEnvs().getChannel_id());// 销户渠道
//		if (property.getDrawwy() == E_CLSDTP.MGD) {
//			closeSendMsgInput.setClosfg(E_YES___.YES);// 是否挂失销户标志
//		} else {
//			closeSendMsgInput.setClosfg(E_YES___.NO);// 是否挂失销户标志
//		}
//
//		mri.setMsgtyp("ApSmsType.IoCaCloseAcctSendMsg");
//		mri.setMsgobj(closeSendMsgInput); 
//		AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
		/*KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("CLOSAC", "CUSTSM","%", "%", true);
		
		String bdid = tblKnaPara.getParm_value1();// 服务绑定ID
		
		IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind(
				IoCaOtherService.class, bdid);
		
		// 1.销户成功发送销户结果到客户信息
		String mssdid = CommTools.getMySysId();// 消息ID
		String mesdna = tblKnaPara.getParm_value2();// 媒介名称

		IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter closeSendMsgInput = SysUtil.getInstance(IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter.class);
		closeSendMsgInput.setMsgid(mssdid); // 发送消息ID
//		closeSendMsgInput.setMedium(mssdtp); // 消息媒介
		closeSendMsgInput.setMdname(mesdna); // 媒介名称
		closeSendMsgInput.setCustid(property.getCustid());// 用户ID
		closeSendMsgInput.setClosbr(property.getClosbr());// 操作机构
		closeSendMsgInput.setClosus(property.getClosus());// 操作柜员
		closeSendMsgInput.setClossv(property.getClossv());// 销户渠道
		if (property.getDrawwy() == E_CLSDTP.MGD) {
			closeSendMsgInput.setClosfg(E_YES___.YES);
		} else {
			closeSendMsgInput.setClosfg(E_YES___.NO);
		}
		

		caOtherService.closeAcctSendMsg(closeSendMsgInput);
		
		// 2.销户成功发送销户结果到合约库
		KnpParameter tblKnaPara1 = KnpParameterDao.selectOne_odb1("CLOSAC", "AGRTSM",
				"%", "%", true);

		String mssdid1 = CommTools.getMySysId();// 消息ID

		String mesdna1 = tblKnaPara1.getParm_value2();// 媒介名称

		IoCaOtherService.IoCaClAcSendContractMsg.InputSetter closeSendAgrtInput = SysUtil.getInstance(IoCaOtherService.IoCaClAcSendContractMsg.InputSetter.class);

		closeSendAgrtInput.setMsgid(mssdid1); // 发送消息ID
//		closeSendAgrtInput.setMedium(mssdtp); // 消息媒介
		closeSendAgrtInput.setMdname(mesdna1); // 媒介名称
		closeSendAgrtInput.setUserId(property.getCustid()); // 用户ID
		closeSendAgrtInput.setAcctType(property.getAccatp());// 账户分类
		closeSendAgrtInput.setOrgId(property.getBrchno());// 归属机构
		closeSendAgrtInput.setAcctNo(input.getCardno());// 电子账号
		closeSendAgrtInput.setAcctStat(E_CUACST.CLOSED);// 客户化状态
		closeSendAgrtInput.setAcctName(property.getTblKnaAcct().getAcctna());// 户名
		closeSendAgrtInput.setCertNo(cplCifCust.getIdtfno());// 证件号码
		closeSendAgrtInput.setCertType(cplCifCust.getIdtftp());// 证件类型
		closeSendAgrtInput.setTransTime(DateTools2.getCurrentTimestamp());//操作时间

		caOtherService.clAcSendContractMsg(closeSendAgrtInput);
			*/
	}
	
	private static BigDecimal chkParam(final cn.sunline.ltts.busi.dptran.trans.close.intf.Cltrfe.Input input){
		
		if(CommUtil.isNull(input.getCardno())){
			throw DpModuleError.DpstComm.BNAS0287();
		}
		
		if(CommUtil.isNull(input.getChaglg())){
			throw DpModuleError.DpstAcct.BNAS0341();
		}
		
		if(CommUtil.isNull(input.getCrcycd())){
			throw DpModuleError.DpstComm.BNAS1101();
		}
		
		if(CommUtil.isNull(input.getChckdt())){
			throw DpModuleError.DpstComm.BNAS0808();
		}
		
		if(CommUtil.isNull(input.getKeepdt())){
			throw DpModuleError.DpstComm.BNAS0399();
		}
		
		if(CommUtil.isNull(input.getPyindt())){
			throw DpModuleError.DpstComm.BNAS0285();
		}
		
		if(CommUtil.isNull(input.getPyinsq())){
			throw DpModuleError.DpstComm.BNAS0286();
		}
		
		if(CommUtil.isNull(input.getSettbl())){
			
		}
		
		if(CommUtil.isNull(input.getSmrycd())){
			throw DpModuleError.DpstComm.BNAS0196();
		}
		
		if(CommUtil.isNull(input.getTracna())){
			throw DpModuleError.DpstComm.BNAS0326();
		}
		
		if(CommUtil.isNull(input.getTracno())){
			throw DpModuleError.DpstComm.BNAS0324();
		}
		
		if(CommUtil.isNull(input.getTractp())){
			throw DpModuleError.DpstComm.BNAS0322();
		}
		
		if(input.getTractp() == E_CLSATP.CUSTAC){
			throw DpModuleError.DpstComm.BNAS0323();
		}
		
		if(CommUtil.isNull(input.getTrbkno())){
			
		}
		
		BigDecimal totPaidam = BigDecimal.ZERO;
		
		if(input.getChaglg() == E_YES___.YES){
			
			// 收费交易金额检查
			if(input.getChrgpm().size() <= 0){
				throw DpModuleError.DpstComm.BNAS0395();
			}
			
			for (IoCgCalCenterReturn IoCgCalCenterReturn : input.getChrgpm()) {
				BigDecimal tranam = IoCgCalCenterReturn.getTranam();// 交易金额
				BigDecimal clcham = IoCgCalCenterReturn.getClcham();// 应收费用金额（未优惠）
				BigDecimal dircam = IoCgCalCenterReturn.getDircam();// 优惠后应收金额
				BigDecimal paidam = IoCgCalCenterReturn.getPaidam();// 实收金额
				
				if (CommUtil.isNotNull(tranam)) {
					if(CommUtil.compare(tranam, BigDecimal.ZERO) < 0){ //交易金额金额
						throw DpModuleError.DpstComm.BNAS0338();
					}
					if(CommUtil.compare(tranam, input.getSettbl()) != 0){ //交易金额金额
						throw DpModuleError.DpstComm.BNAS0336();
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
			
			
		}
		
		return totPaidam;
		
	}

	

}
