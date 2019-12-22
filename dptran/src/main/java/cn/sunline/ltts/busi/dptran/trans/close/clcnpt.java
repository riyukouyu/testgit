package cn.sunline.ltts.busi.dptran.trans.close;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.AccChngbrDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ClsAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcal;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbClac;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.CupsTranfe;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseIN;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
//import cn.sunline.ltts.busi.ltts.zjrc.infrastructure.dap.plugin.type.ECustPlugin.E_MEDIUM;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AFEETG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSATP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOTYPE;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;


public class clcnpt {

	public static void dealTransBefore(
			final cn.sunline.ltts.busi.dptran.trans.close.intf.Clcnpt.Input input,
			final cn.sunline.ltts.busi.dptran.trans.close.intf.Clcnpt.Property property,
			final cn.sunline.ltts.busi.dptran.trans.close.intf.Clcnpt.Output output) {

		BigDecimal tlcam = chkParam(input); //返回收费总金额
		
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();
		//检查账户是否是预销户
		IoCaKnaAcdc acdc = ActoacDao.selKnaAcdc(input.getPyerac(), false);
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
		KnaAcct tblKnaAcct = ActoacDao.selKnaAcct(cplKnbClac.getAcctno(), true);
		
		E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(acdc.getCustac());
		
		if(!CommUtil.equals(tblKnaAcct.getBrchno(),brchno)){
			throw DpModuleError.DpstComm.BNAS0887();
		}
		
		if(input.getAfeetg() != E_AFEETG.T0){ //收费标志
			if(CommUtil.compare(tblKnaAcct.getOnlnbl(), tlcam) < 0){
				throw DpModuleError.DpstComm.BNAS0334();
			}
			property.setChgflg(E_CHGFLG.ALL); //设置记账标志
			property.setChaglg(E_YES___.YES);
		}
		
		if(CommUtil.isNotNull(input.getTranam())){
			if(!CommUtil.equals(input.getTranam(), tblKnaAcct.getOnlnbl())){
				throw DpModuleError.DpstComm.BNAS0594();
			}
		}
		
		//add 2016/12/26 songlw 获取客户手机号
		IoCaKnaAcal cplKnaAcal = AccChngbrDao.selKnaacalByCus(acdc.getCustac(), E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);
		if(CommUtil.isNotNull(cplKnaAcal)){
			property.setAcalno(cplKnaAcal.getAcalno());
		}
		
		//IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_accsByCustno(tblKnaAcct.getCustno(), true, E_STATUS.NORMAL);
		property.setCustid(tblKnaAcct.getCustno());
		
		property.setAccatp(accatp);
		
		property.setCustno(tblKnaAcct.getCustno());
		property.setTblKnaAcct(tblKnaAcct);
		property.setTlcam(tlcam);
		
		property.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());
		property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());
		property.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
	}
	
	/**
	 * @Title: prcAccount 
	 * @Description:销户转账 并注销电子账户  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年10月25日 下午9:04:33 
	 * @version V2.3.0
	 */
	public static void prcAccount( final cn.sunline.ltts.busi.dptran.trans.close.intf.Clcnpt.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clcnpt.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clcnpt.Output output){
		KnaAcct tblKnaAcct = property.getTblKnaAcct();
		/*BigDecimal tranam = tblKnaAcct.getOnlnbl();
		if(input.getAfeetg() != E_AFEETG.T0){ //收费标志
			tranam = tblKnaAcct.getOnlnbl().subtract(property.getTlcam());
		}*/
		KnpParameter para2 = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", E_CLACTP._01.getValue(), "%", true);
		
		
		IoDpCloseIN clsin = SysUtil.getInstance(IoDpCloseIN.class);
		
		clsin.setCardno(input.getPyerac());
		clsin.setToacct(input.getPyeeac());
		clsin.setToname(input.getPyeena());
		clsin.setTobrch(input.getPyeecd());
		clsin.setSmrycd(BusinessConstants.SUMMARY_XH);
		
		//BigDecimal onlbal = DpCloseAcctno.prcCurrOnbal(tblKnaAcct, clsin);
		BigDecimal onlbal = SysUtil.getInstance(DpAcctSvcType.class).closeAcctno(tblKnaAcct.getAcctno(), clsin);
		/*if(CommUtil.compare(tranam, BigDecimal.ZERO) > 0){
			DrawDpAcctIn drawIn = SysUtil.getInstance(DrawDpAcctIn.class); //电子账户支取记账复合类型
			drawIn.setAcctno(tblKnaAcct.getAcctno());
			drawIn.setAcseno(null);
			drawIn.setAuacfg(E_YES___.NO);
			drawIn.setBankcd(null);
			drawIn.setBankna(null);
			drawIn.setCardno(input.getPyerac());
			drawIn.setCrcycd(tblKnaAcct.getCrcycd());
			drawIn.setCustac(tblKnaAcct.getCustac());
			drawIn.setIschck(E_YES___.NO); //不检查支取控制，无条件支取
			drawIn.setOpacna(input.getPyeena());
			drawIn.setOpbrch(input.getPyeecd());
			drawIn.setRemark("销户转电子账户记账");
			drawIn.setSmrycd(input.getSmrycd());
			drawIn.setSmryds("");
			drawIn.setStrktg(E_YES___.NO); //本交易不提供冲正
			drawIn.setToacct(input.getPyeeac());
			drawIn.setTranam(tranam);
			SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawIn);
			*/
		if(CommUtil.compare(onlbal, BigDecimal.ZERO) > 0){

			CupsTranfe cplCnapot = SysUtil.getInstance(CupsTranfe.class);
			
			cplCnapot.setCrcycd(input.getCrcycd());
			cplCnapot.setSmrycd(BusinessConstants.SUMMARY_XH);
			cplCnapot.setInbrch(tblKnaAcct.getBrchno());
			cplCnapot.setTranam(input.getTranam());
			cplCnapot.setOtacct(input.getPyeeac());
			cplCnapot.setOtacna(input.getPyeena());
			
			CapitalTransDeal.dealCnapotVN(cplCnapot, input.getIotype());
			
			
			//内部户贷方记账服务
			String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();
			String acbrch = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(brchno).getBrchno();
			
			IaAcdrInfo acdrIn2 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
			
			acdrIn2.setAcbrch(acbrch); //挂账所属机构挂电子账户机构
			acdrIn2.setAmntcd(E_AMNTCD.CR);
			acdrIn2.setTrantp(E_TRANTP.TR); //交易类型 
//			acdrIn2.setInptsr(E_INPTSR.GL00); //交易来源类别
			acdrIn2.setBusidn(E_BLNCDN.C); //业务代码方向 
			acdrIn2.setCrcycd(tblKnaAcct.getCrcycd());
			acdrIn2.setSmrycd(BusinessConstants.SUMMARY_XH);
			acdrIn2.setToacct(tblKnaAcct.getAcctno());
			acdrIn2.setToacna(tblKnaAcct.getAcctna());
			acdrIn2.setTranam(onlbal);
			acdrIn2.setBusino(para2.getParm_value1()); //业务编码
			acdrIn2.setSubsac(para2.getParm_value2());//子户号
			
			
			//平衡性检查
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._01);
		}
		
		ClsAcctIn cplClsAcctIn = SysUtil.getInstance(ClsAcctIn.class);
		
		cplClsAcctIn.setCardno(input.getPyerac());
		cplClsAcctIn.setCustac(tblKnaAcct.getCustac());
		cplClsAcctIn.setCustna(tblKnaAcct.getAcctna());
		cplClsAcctIn.setCustno(tblKnaAcct.getCustno());
		//注销电子账户
		SysUtil.getInstance(DpAcctSvcType.class).acctStatusUpd(cplClsAcctIn);
		
		IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
		cplDimeInfo.setCustac(tblKnaAcct.getCustac());
		cplDimeInfo.setDime01(input.getTractp().getValue()); //收款人账户类型
		SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);
		
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
		
	}

	/**
	 * @Title: sendCloseInfoMsg 
	 * @Description:  发短信和消息
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年10月26日 下午7:51:14 
	 * @version V2.3.0
	 */
	public static void sendCloseInfoMsg( final cn.sunline.ltts.busi.dptran.trans.close.intf.Clcnpt.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clcnpt.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Clcnpt.Output output){
		// 查询销户登记簿处理状态为成功的记录
		//IoCaKnbClac cplKnbClac = DpAcctDao.selKnbClac(property.getCustac(),E_CLSTAT.SUCC, false);

	
//		E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介
		//IoCucifCust cplCifCust = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_selectOne_odb1(property.getTblKnaAcct().getCustno(), true);
		
//		IoSrvCfPerson.IoGetCifCust.InputSetter queryCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//		queryCust.setCustno(property.getTblKnaAcct().getCustno());
//		IoSrvCfPerson.IoGetCifCust.Output cplCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//		SysUtil.getInstance(IoSrvCfPerson.class).getCifCust(queryCust, cplCifCust);
		
		
//		//修改销户cmq通知  modify lull
//		MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//		mri.setMtopic("Q0101004");
//		IoCaCloseAcctSendMsg closeSendMsgInput = CommTools
//				.getInstance(IoCaCloseAcctSendMsg.class);
//		closeSendMsgInput.setCustid(property.getCustid()); // 用户ID
//		closeSendMsgInput.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());// 操作机构
//		closeSendMsgInput.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());// 操作柜员
//		closeSendMsgInput.setClossv(CommTools.getBaseRunEnvs().getChannel_id());// 销户渠道
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
		closeSendMsgInput.setCustid(property.getCustid()); // 用户ID
		
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
		closeSendAgrtInput.setOrgId(property.getTblKnaAcct().getBrchno());// 归属机构
		closeSendAgrtInput.setAcctNo(input.getPyerac());// 电子账号
		closeSendAgrtInput.setAcctStat(E_CUACST.CLOSED);// 客户化状态
		closeSendAgrtInput.setAcctName(property.getTblKnaAcct().getAcctna());// 户名
		closeSendAgrtInput.setCertNo(cplCifCust.getIdtfno());// 证件号码
		closeSendAgrtInput.setCertType(cplCifCust.getIdtftp());// 证件类型
		closeSendAgrtInput.setTransTime(DateTools2.getCurrentTimestamp());//操作时间
		
		caOtherService.clAcSendContractMsg(closeSendAgrtInput);*/
	}
	/**
	 * @Title: chkParam
	 * @Description:参数检查
	 * @param input
	 * @author zhangan
	 * @date 2016年10月25日 下午8:26:19
	 * @version V2.3.0
	 */
	private static BigDecimal chkParam(final cn.sunline.ltts.busi.dptran.trans.close.intf.Clcnpt.Input input) {
		if (CommUtil.isNull(input.getSubsys())) {
			throw DpModuleError.DpstComm.BNAS1361();
		}
		if (CommUtil.isNull(input.getMsetdt())) {
			throw DpModuleError.DpstComm.BNAS1373();
		}
		if (CommUtil.isNull(input.getMsetsq())) {
			throw DpModuleError.DpstComm.BNAS1374();
		}
		if (CommUtil.isNull(input.getCrdbtg())) {
			throw DpModuleError.DpstComm.BNAS1370();
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
		if (CommUtil.isNull(input.getPyerac())) {
			throw DpModuleError.DpstComm.BNAS1385();
		}
		if (CommUtil.isNull(input.getPyerna())) {
			throw DpModuleError.DpstComm.BNAS1386();
		}
		if (CommUtil.isNull(input.getPyeeac())) {
			throw DpModuleError.DpstComm.BNAS0324();
		}
		if (CommUtil.isNull(input.getPyeena())) {
			throw DpModuleError.DpstComm.BNAS0325();
		}
		if (CommUtil.isNull(input.getChfcnb())) {
			throw DpModuleError.DpstComm.BNAS1362();
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
		if (CommUtil.isNull(input.getUserid())) {
			throw DpModuleError.DpstComm.BNAS1384();
		}
		if (CommUtil.isNull(input.getPriotp())) {
			throw DpModuleError.DpstComm.BNAS1371();
		}
		if (CommUtil.isNull(input.getAfeetg())) {
			throw DpModuleError.DpstAcct.BNAS0341();
		}
		if (CommUtil.isNull(input.getTranam())) {
			throw DpModuleError.DpstProd.BNAS0620();
		}
		if(CommUtil.isNull(input.getPyindt())){
			throw DpModuleError.DpstComm.BNAS0285();
		}
		
		if(CommUtil.isNull(input.getPyinsq())){
			throw DpModuleError.DpstComm.BNAS0286();
		}
		if(input.getIotype()!=E_IOTYPE.OUT){//往账
			throw DpModuleError.DpstComm.BNAS1387();
		}
		
		BigDecimal tlcam = BigDecimal.ZERO;
		
		if(input.getAfeetg() != E_AFEETG.T0){ //收费标志
			// 收费交易金额检查
			if(input.getChrgpm().size() <= 0){
				throw DpModuleError.DpstComm.BNAS0395();
			}
			for (IoCgCalCenterReturn IoCgCalCenterReturn : input.getChrgpm()) {
				//BigDecimal tranam = IoCgCalCenterReturn.getTranam();// 交易金额
				BigDecimal clcham = IoCgCalCenterReturn.getClcham();// 应收费用金额（未优惠）
				BigDecimal dircam = IoCgCalCenterReturn.getDircam();// 优惠后应收金额
				BigDecimal paidam = IoCgCalCenterReturn.getPaidam();// 实收金额
				
//				if (CommUtil.isNotNull(tranam)) {
//					if(CommUtil.compare(tranam, BigDecimal.ZERO) < 0){ //交易金额金额
//						throw DpModuleError.DpstComm.E9999("收费交易金额不能小于0");
//					}
//					if(CommUtil.compare(tranam, input.getTranam()) != 0){ //交易金额金额
//						throw DpModuleError.DpstComm.E9999("收费交易金额与出入金交易金额不一致");
//					}
//				}
				
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
					tlcam = tlcam.add(paidam);
				}
			}
		}
		
		if(input.getTractp() != E_CLSATP.OTHER){
			throw DpModuleError.DpstComm.BNAS1940();
		}
		
		return tlcam;
	}

	

	
}
