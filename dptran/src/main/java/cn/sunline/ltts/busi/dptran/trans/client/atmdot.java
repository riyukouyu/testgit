package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIobl;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpOtherService;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCuad;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkOT;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.CapAuthComb;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;



public class atmdot {

	private static BizLog log = BizLogUtil.getBizLog(atmdot.class);
	
	/**
	 * @Title: DealTransBefore 
	 * @Description: 交易前检查  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年12月15日 上午9:44:18 
	 * @version V2.3.0
	 */
	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdot.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdot.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdot.Output output){
		//重复提交检查
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		KnlIobl iobl = ActoacDao.selKnlIoblDetl(transq, trandt, false);
		if(CommUtil.isNotNull(iobl)){
			property.setIssucc(E_YES___.YES);
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
			throw DpModuleError.DpstComm.BNAS0740();
		}
		//转出电子账户信息
		
		String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//		CommTools.getBaseRunEnvs().setBusi_org_id(otacdc.getCorpno());
		
		//状态、类型、状态字检查
		E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(otacdc.getCustac());
		AcTranfeChkIN chkIN = SysUtil.getInstance(AcTranfeChkIN.class);
		chkIN.setAccatp(accatp);
		chkIN.setCardno(otacdc.getCardno());
		chkIN.setCustac(otacdc.getCustac());
		chkIN.setCustna(input.getOtacna());
		chkIN.setCapitp(capitp);
		chkIN.setOpcard(input.getIncard());
		//chkIN.setOppoac(input.getIncard());
		chkIN.setOppona(input.getInacna());
		chkIN.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		
		AcTranfeChkOT chkOT = CapitalTransCheck.chkTranfe(chkIN);
		
		KnaAcct otacct = CapitalTransDeal.getSettKnaAcctSub(otacdc.getCustac(), E_ACSETP.SA);
		if(CommUtil.isNotNull(input.getOtcstp()) && input.getOtcstp() != E_ACSETP.SA){
			throw DpModuleError.DpstComm.BNAS0029();
		}
		
		if(CommUtil.isNotNull(input.getOtacna()) && !CommUtil.equals(input.getOtacna(), otacct.getAcctna())){
			throw DpModuleError.DpstComm.BNAS0892();
		}
		
		
		//额度参数设置
		input.getChkqtn().setSbactp(E_SBACTP._11);
		input.getChkqtn().setAccttp(accatp);
		input.getChkqtn().setCustac(otacdc.getCustac());
		input.getChkqtn().setBrchno(otacct.getBrchno());// 交易机构号
		input.getChkqtn().setCustie(chkOT.getIsbind()); //是否绑定卡标识
		input.getChkqtn().setFacesg(chkOT.getFacesg());	//是否面签标识
		input.getChkqtn().setRecpay(null); //收付方标识，电子账户转电子账户需要输入
		//币种校验
		if(!CommUtil.equals(input.getCrcycd(),otacct.getCrcycd())){
			throw DpModuleError.DpstComm.BNAS0632();
		}
		//检查转出机构与电子账户机构是否一致
		if(CommUtil.isNotNull(input.getOtbrch())){
			if(!CommUtil.equals(input.getOtbrch(), otacct.getBrchno())){
				throw DpModuleError.DpstComm.BNAS0043();
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
		property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //渠道来源流水
		property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date()); //渠道来源日期 
		property.setLinkno(null); //连笔号
		property.setPrcscd("atmdot"); //交易码
		property.setIoflag(E_IOFLAG.OUT);
		property.setBrchno(otacct.getBrchno());
		//出金
		property.setTrantp(E_TRANTP.TR); //交易类型 
		
		property.setClactp(E_CLACTP._01);
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();
		String acbrch = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(brchno).getBrchno();
		property.setAcbrch(acbrch); //省中心
		
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", property.getClactp().getValue(), "%", true);
		
		property.setBusino(para.getParm_value1()); //业务编码
		property.setSubsac(para.getParm_value2());//子户号
	}
	
	/**
	 * @Title: DealTransAfter 
	 * @Description: 交易后处理  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年12月15日 上午9:44:33 
	 * @version V2.3.0
	 */
	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdot.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdot.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdot.Output output){
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
	 * @date 2016年12月14日 上午9:30:52 
	 * @version V2.3.0
	 */
	public static BigDecimal chkParam( final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdot.Input input ){
		
		BigDecimal tlcgam = input.getTlcgam();
		
		// 输入项非空检查
		if (CommUtil.isNull(input.getCapitp())) {
			throw DpModuleError.DpstComm.BNAS0023();
		}
		
		E_CAPITP capitp = input.getCapitp();
		//交易范围检查
		//转借记卡、转贷记卡(还本行信用卡)、银联在线转出、还贷款
		if(capitp != E_CAPITP.OT204){
			throw DpModuleError.DpstComm.BNAS1188( capitp.getLongName() );
		}
		
		if (CommUtil.isNull(input.getCrcycd())) {
			throw DpModuleError.DpstAcct.BNAS0634();
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
			throw DpModuleError.DpstComm.BNAS0330();
		}
		if (CommUtil.isNull(input.getChckdt())) {
			throw DpModuleError.DpstComm.BNAS0808();
		}
		if (CommUtil.isNull(input.getKeepdt())) {
			throw DpModuleError.DpstComm.BNAS0399();
		}
		if (CommUtil.isNull(input.getChkqtn().getIsckqt())) {
			throw DpModuleError.DpstAcct.BNAS1897();
		}
		
		if(CommUtil.isNull(input.getOtcard())){
			throw DpModuleError.DpstComm.BNAS0042();
		}
		
		if(CommUtil.isNull(input.getChkpwd().getIspass())){
			throw DpModuleError.DpstAcct.BNAS1892();
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

	public static void BusiCheck( final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdot.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdot.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdot.Output output){
		
		
		//实例化
		IoDpOtherService.IoDpBusiCheck.InputSetter bcInput = 
				SysUtil.getInstance(IoDpOtherService.IoDpBusiCheck.InputSetter.class);
		IoCaSevQryTableInfo qryCaTable = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		
		KnpParameter para = KnpParameterDao.selectOne_odb1("BUSICK", "%", "%", "%", true);
		
		String bdid = para.getParm_value1();// 服务绑定ID
		
		IoDpOtherService dpOtherService = 
				SysUtil.getInstanceProxyByBind (IoDpOtherService.class, bdid);
		
		Options<CapAuthComb> lst = new DefaultOptions<CapAuthComb>();
		//认证组合
		CapAuthComb authComb = SysUtil.getInstance(CapAuthComb.class);
		
		authComb.setAuthType("01"); //密码类型 01账户密码
		authComb.setAuthInfo(input.getOtcard());
		authComb.setCert(input.getChkpwd().getPasswd()); //密码
		
		lst.add(authComb);
		
		//查询电子账户附表
		IoCaKnaCuad knaCuad = qryCaTable.getKnaCuadOdb1(property.getOtcsac(), true);
		
		bcInput.setSessionId(input.getChkpwd().getAuthif()); //加密因子
		bcInput.setUserId(knaCuad.getCustid()); //用户ID
		bcInput.setAuthComb(lst); //认证组合
		
//		CommTools.getBaseRunEnvs().setCustid(knaCuad.getCustid()); //用户ID
		
		dpOtherService.dpBusiCheck(bcInput);
	}
}
