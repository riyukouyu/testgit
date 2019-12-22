package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIobl;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkOT;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;



public class atmdin {

	/**
	 * @Title: DealTransBefore 
	 * @Description: 交易前检查  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年12月15日 上午10:25:37 
	 * @version V2.3.0
	 */
	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdin.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdin.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdin.Output output){
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
		
		IoCaKnaAcdc inacdc = ActoacDao.selKnaAcdc(input.getIncard(), false);
		if(CommUtil.isNull(inacdc)){
			throw DpModuleError.DpstComm.BNAS0902();
		}
		
		if(inacdc.getStatus() == E_DPACST.CLOSE){
			throw DpModuleError.DpstComm.BNAS0441();
		}
		
		String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//		CommTools.getBaseRunEnvs().setBusi_org_id(inacdc.getCorpno());
		
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
		if(!CommUtil.equals(input.getCrcycd(),tblKnaAcct.getCrcycd())){
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
		property.setLinkno(null); //连笔号
		property.setPrcscd("atmdin"); //交易码
		property.setDscrtx(capitp.getLongName()); //描述
		property.setTrantp(E_TRANTP.TR); //交易类型 
		property.setIoflag(E_IOFLAG.IN);
		property.setBrchno(tblKnaAcct.getBrchno());
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();
		String acbrch = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(brchno).getBrchno();
		property.setAcbrch(acbrch); //省中心
		
		property.setClactp(E_CLACTP._01);
		
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", property.getClactp().getValue(), "%", true);
		
		property.setBusino(para.getParm_value1()); //业务编码
		property.setSubsac(para.getParm_value2());//子户号
	}
	/**
	 * @Title: DealAcctStatAndSett 
	 * @Description: 休眠转入修改状态  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年12月15日 下午2:28:13 
	 * @version V2.3.0
	 */
	public static void DealAcctStatAndSett( final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdin.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdin.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdin.Output output){
		//修改账户状态，休眠转正常结息
		//转入账户的电子账户信息，转入账户的结算户信息或钱包户信息
		CapitalTransDeal.dealAcctStatAndSett(property.getCuacst(), property.getTblKnaAcct());
	}
	
	/**
	 * @Title: DealTransAfter 
	 * @Description: 交易后处理  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年12月15日 下午2:28:28 
	 * @version V2.3.0
	 */
	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdin.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdin.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdin.Output output){
		if(property.getIssucc() == E_YES___.NO){
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),property.getClactp());
			
			output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
			output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
		}	
	}
	
	/**
	 * @Title: chkParam 
	 * @Description: 参数关系检查  
	 * @param input
	 * @return
	 * @author zhangan
	 * @date 2016年12月15日 下午2:28:39 
	 * @version V2.3.0
	 */
	public static BigDecimal chkParam( final cn.sunline.ltts.busi.dptran.trans.client.intf.Atmdin.Input input){
		
		BigDecimal tlcgam = input.getTlcgam();
		
		// 输入项非空检查
		if (CommUtil.isNull(input.getCapitp())) {
			throw DpModuleError.DpstComm.BNAS0023();
		}
		
		E_CAPITP capitp = input.getCapitp();
		//交易范围检查
		//借记卡转入、贷记卡转入、银联在线转入、内部户转入
		if(capitp != E_CAPITP.IN105 && capitp != E_CAPITP.IN106){
			throw DpModuleError.DpstComm.BNAS1188(capitp.getLongName());
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
			throw DpModuleError.DpstComm.BNAS0331();
		}
		if (CommUtil.isNull(input.getChckdt())) {
			throw DpModuleError.DpstComm.BNAS0808();
		}
		if (CommUtil.isNull(input.getKeepdt())) {
			throw DpModuleError.DpstComm.BNAS0399();
		}
//		if (CommUtil.isNull(input.getChkqtn().getIsckqt())) {
//			throw DpModuleError.DpstComm.E9027("额度验证扣减标志");
//		}
		
		if(CommUtil.isNull(input.getIncard())){
			throw DpModuleError.DpstComm.BNAS0030();
		}
		if(CommUtil.isNull(input.getInacna())){
		//	throw DpModuleError.DpstComm.BNAS0032();
		}
		if(CommUtil.isNull(input.getOtcard())){
		//	throw DpModuleError.DpstComm.E9027("转出方卡号/账号!");
		}
		if(CommUtil.isNull(input.getOtacna())){
		//	throw DpModuleError.DpstComm.E9027("转出方户名!");
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
				tlcgam = tlcgam.add(paidam);
			}
			
			if(!CommUtil.equals(totPaidam, input.getTlcgam())){
				throw DpModuleError.DpstComm.BNAS0243();
			}
		}
		
		
		return tlcgam;
	}
}
