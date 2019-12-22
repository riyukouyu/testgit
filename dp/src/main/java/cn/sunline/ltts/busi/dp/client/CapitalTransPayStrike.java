package cn.sunline.ltts.busi.dp.client;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.namedsql.DpStrikeSqlDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnsTranEror;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnsTranErorDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlIobl;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.ioKnlSpnd;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.StrkpyAcnt;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTypeStrikeInfo.ChargStrikeOutput;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTypeStrikeInfo.ProcPbChargStrikeInput;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.FnEnumType.E_WARNTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;

/**
 * @ClassName: CapitalTransPayStrike 
 * @Description: 支付平台交易需要冲正自动挂账的处理类 
 * @author zhangan
 * @date 2017年2月28日 下午7:18:56 
 *
 */
public class CapitalTransPayStrike {

	private static BizLog log = BizLogUtil.getBizLog(CapitalTransPayStrike.class);
	
	
	/**
	 * @Title: procStrkpyIobl 
	 * @Description: 登记出入金登记簿的交易冲正挂账处理  
	 * @param iobl
	 * @param ioflag
	 * @author zhangan
	 * @date 2017年2月28日 下午7:22:21 
	 * @version V2.3.0
	 */
	public static void procStrkpyIobl(IoKnlIobl iobl, E_IOFLAG ioflag, String errmsg){
		
		
		StrkpyAcnt acnt = SysUtil.getInstance(StrkpyAcnt.class);
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String tmstmp = DateTools2.getCurrentTimestamp();
		
		//IoKnlIobl iobl = DpAcctQryDao.selKnlIoblbyTransq(retrsq, true);
		//KnlIobl iobl = ActoacDao.selKnlIoblDetl(retrsq, retrdt, true);
		
		acnt.setCapitp(iobl.getCapitp());
		acnt.setTranam(iobl.getTranam());
		acnt.setCrcycd(iobl.getCrcycd());
		acnt.setErrmsg(errmsg);
		acnt.setRetrdt(iobl.getTrandt());
		acnt.setRetrsq(iobl.getTransq());
		
		
		if(ioflag == E_IOFLAG.IN){ //入金
			
			acnt.setCardno(iobl.getCardno());
			acnt.setCustac(iobl.getCuacno());
			acnt.setAcctno(iobl.getAcctno());
			acnt.setBrchno(iobl.getBrchno());
			acnt.setCustna("");
			acnt.setOpcard(iobl.getToacct());
			acnt.setOpbrch(iobl.getTobrch());
			acnt.setOpcsna("");
		}else if(ioflag == E_IOFLAG.OUT){
			
			acnt.setCardno(iobl.getToacct());
			acnt.setCustac(iobl.getToscac());
			acnt.setAcctno(iobl.getToacno());
			acnt.setBrchno(iobl.getTobrch());
			acnt.setCustna("");
			acnt.setOpcard(iobl.getCardno());
			acnt.setOpbrch(iobl.getBrchno());
			acnt.setOpcsna("");
		}
		
		//挂账处理
		procStrkpy(acnt, ioflag);
		String revrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		//修改出入金登记簿为已冲正
		DpStrikeSqlDao.strikeKnlIoblStatus(iobl.getServsq(), iobl.getServdt(), E_TRANST.STRIKED, revrsq, trandt, tmstmp);
	
	}
	
	/**
	 * @Title: procStrkpySpnd 
	 * @Description: 登记消费登记簿的交易冲正挂账处理  
	 * @param spnd
	 * @param ioflag
	 * @author zhangan
	 * @date 2017年2月28日 下午7:22:55 
	 * @version V2.3.0
	 */
	public static void procStrkpySpnd(ioKnlSpnd spnd, E_IOFLAG ioflag, String errmsg){
		
		StrkpyAcnt acnt = SysUtil.getInstance(StrkpyAcnt.class);
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String tmstmp = DateTools2.getCurrentTimestamp();
		
		//ioKnlSpnd spnd = DpAcctDao.selKnlSpndByTransq(retrsq, true);
		//KnlSpnd spnd = DpAcctDao.selKnlSpndInfo(retrsq, retrdt, true);
		acnt.setCapitp(spnd.getCapitp());
		acnt.setTranam(spnd.getTranam());
		acnt.setCrcycd(spnd.getCrcycd());
		acnt.setErrmsg(errmsg);
		acnt.setRetrdt(spnd.getTrandt());
		acnt.setRetrsq(spnd.getTransq());
		
		if(ioflag == E_IOFLAG.IN){
			
		}else if(ioflag == E_IOFLAG.OUT){
			
			acnt.setCardno(spnd.getToacct());
			acnt.setCustac(spnd.getToscac());
			acnt.setAcctno(spnd.getToacno());
			acnt.setBrchno(spnd.getTobrch());
			acnt.setCustna(spnd.getToname());
			acnt.setOpcard(spnd.getIncard());
			acnt.setOpbrch(spnd.getInbrch());
			if(spnd.getCapitp() == E_CAPITP.OU401){
				acnt.setOpcsna(spnd.getInbsno());
			}else if(spnd.getCapitp() == E_CAPITP.OU402){
				acnt.setOpcsna(spnd.getInname());
			}
			
		}
		//挂账处理
		procStrkpy(acnt, ioflag);
		String revrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		
		DpStrikeSqlDao.strikeKnlSpndStatus(spnd.getServsq(), spnd.getServdt(), E_TRANST.STRIKED, revrsq, trandt,tmstmp);
	}
	
	/**
	 * @Title: procStrkpy 
	 * @Description:  
	 * @author zhangan
	 * @date 2017年3月1日 上午8:42:26 
	 * @version V2.3.0
	 */
	private static void procStrkpy(StrkpyAcnt acnt, E_IOFLAG ioflag){
		
		
		log.debug("原交易类型:[%s]", acnt.getCapitp().getLongName());
		log.debug("原电子账户:[%s]", acnt.getCardno());
		log.debug("原交易流水:[%s]", acnt.getRetrsq());
		log.debug("原交易日期:[%s]", acnt.getRetrdt());
		log.debug("原交易金额:[%s]", acnt.getTranam());
		//log.debug("原交易费用:[%s]", acnt.getRetrdt());
		
		E_CAPITP capitp = acnt.getCapitp();
		E_CLACTP clactp = null;
		if(capitp == E_CAPITP.IN101){
			clactp = E_CLACTP._01;
		}else if(capitp == E_CAPITP.IN103){//103	银联在线转入电子账户
			clactp = E_CLACTP._18;
		}else if(capitp == E_CAPITP.IN105){//105	ATM转电子账户
			clactp = E_CLACTP._01;
		}else if(capitp == E_CAPITP.IN106){//106	ATM存现
			clactp = E_CLACTP._01;
		}else if(capitp == E_CAPITP.IN108){//108	内部户转电子账户
			clactp = E_CLACTP._01;
		}else if(capitp == E_CAPITP.OT201){//201	电子账户转出本行借记卡
			clactp = E_CLACTP._01;
		}else if(capitp == E_CAPITP.OT202){//202	电子账户转出本行贷记卡
			clactp = E_CLACTP._09;
		}else if(capitp == E_CAPITP.OT203){//203	电子账户银联在线转出
			clactp = E_CLACTP._19;
		}else if(capitp == E_CAPITP.OT204){//204	ATM无卡取现
			clactp = E_CLACTP._01;
		}else if(capitp == E_CAPITP.OT205){//205	电子账户还贷款
			clactp = E_CLACTP._01;
		}else if(capitp == E_CAPITP.OU401){//401	消费
			clactp = E_CLACTP._01;
		}else if(capitp == E_CAPITP.OU402){//402	缴费
			clactp = E_CLACTP._02;
		}else{
			throw DpModuleError.DpstAcct.BNAS0207();
		}
		
		
		String remark = ""; //挂账户的备注
		
		DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class); //存款记账服务
		
		IoInAccount inSrv = CommTools.getRemoteInstance(IoInAccount.class);
		
		//省中心机构
		String acbrch = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(acnt.getBrchno()).getBrchno(); //获取省中心机构号
		//获取系统内清算往来业务编号
		KnpParameter para1 = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", clactp.getValue(), "%", true);
		//挂账业务编号
		KnpParameter para3 = KnpParameterDao.selectOne_odb1("InParm.cupsconfrim","in", "01", "%", true);
		
		BigDecimal glpyam = BigDecimal.ZERO;
		
		//费用冲正
		BigDecimal chrgam = BigDecimal.ZERO;
		
		ProcPbChargStrikeInput input = SysUtil.getInstance(ProcPbChargStrikeInput.class);
		input.setOrtrdt(acnt.getRetrdt());
		input.setOrtrsq(acnt.getRetrsq());
		Options<ChargStrikeOutput> lstOutput = SysUtil.getInstance(IoPbStrikeSvcType.class).procPbChargStrike(input);
		
		for(ChargStrikeOutput output : lstOutput){
			
			BigDecimal tlcgam = CommUtil.nvl(output.getTranam(), BigDecimal.ZERO);
			chrgam = chrgam.add(tlcgam);
			
		}
		
		if(ioflag == E_IOFLAG.IN){ //入金
			
			glpyam = acnt.getTranam().subtract(chrgam);
			//红字存入
			SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class); //电子账户存入记账复合类型
			
			saveIn.setAcctno(acnt.getAcctno()); //结算账户、钱包账户
			saveIn.setBankcd(acnt.getOpbrch());
			saveIn.setBankna("");
			saveIn.setCardno(acnt.getCardno()); //电子账户卡号
			saveIn.setCrcycd(acnt.getCrcycd());
			saveIn.setCustac(acnt.getCustac());
			saveIn.setOpacna(acnt.getOpcsna());
			saveIn.setOpbrch(acnt.getOpbrch());
			saveIn.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
			saveIn.setToacct(acnt.getOpcard());
			saveIn.setTranam(acnt.getTranam().negate()); //红字存入
			saveIn.setIschck(E_YES___.NO);
			saveIn.setStrktg(E_YES___.NO);
			saveIn.setNegafg(E_YES___.YES); //是否允许负金额
			
			dpSrv.addPostAcctDp(saveIn);
			remark = acnt.getCardno() + acnt.getRetrdt() + acnt.getRetrsq() + acnt.getErrmsg();
			
		}else if(ioflag == E_IOFLAG.OUT){
			
			glpyam = acnt.getTranam().add(chrgam);
			//系统内清算贷方红字 
			IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户记账输入
			acdrIn1.setAcbrch(acbrch);
			acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
			acdrIn1.setCrcycd(acnt.getCrcycd());
			acdrIn1.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd()); //冲正
			acdrIn1.setToacct(acnt.getOpcard());
			acdrIn1.setToacna(acnt.getOpcsna());
			acdrIn1.setTranam(acnt.getTranam().negate());
			acdrIn1.setBusino(para1.getParm_value1()); //业务编码
			acdrIn1.setSubsac(para1.getParm_value2());//子户号
			
			inSrv.ioInAccr(acdrIn1);
			
			remark = acnt.getOpcard() + acnt.getRetrdt() + acnt.getRetrsq() + acnt.getErrmsg();
		}
			
	
		IaTransOutPro acdrOt = SysUtil.getInstance(IaTransOutPro.class);
		IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户记账输入
		//结算暂收挂账
		acdrIn1.setAcbrch(acnt.getBrchno()); //挂账所属机构挂电子账户机构
		acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
		acdrIn1.setCrcycd(acnt.getCrcycd());
		acdrIn1.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
		acdrIn1.setToacct(acnt.getOpcard());
		acdrIn1.setToacna(acnt.getOpcard());
		acdrIn1.setTranam(glpyam);
		acdrIn1.setBusino(para3.getParm_value1()); //业务编码
		acdrIn1.setSubsac(para3.getParm_value2());//子户号
		
		acdrIn1.setDscrtx(remark);
		
		acdrOt = inSrv.ioInAccr(acdrIn1); //贷记服务

		//登记预警登记簿
		//登记转账预警登记簿
		KnsTranEror tblKnsTranEror = SysUtil.getInstance(KnsTranEror.class);
		tblKnsTranEror.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		tblKnsTranEror.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		tblKnsTranEror.setErortp(E_WARNTP.ACOUNT);	
		tblKnsTranEror.setBrchno(acnt.getBrchno());
		if(ioflag == E_IOFLAG.IN){
			tblKnsTranEror.setOtacct(acnt.getOpcard());
			tblKnsTranEror.setOtacna(acnt.getOpcsna());
			tblKnsTranEror.setOtbrch(acnt.getOpbrch());
			tblKnsTranEror.setInacct(acnt.getCardno());					
			tblKnsTranEror.setInacna(acnt.getCustna());
			tblKnsTranEror.setInbrch(acnt.getBrchno());
		}else if(ioflag == E_IOFLAG.OUT){
			tblKnsTranEror.setOtacct(acnt.getCardno());
			tblKnsTranEror.setOtacna(acnt.getCustna());
			tblKnsTranEror.setOtbrch(acnt.getBrchno());
			tblKnsTranEror.setInacct(acnt.getOpcard());					
			tblKnsTranEror.setInacna(acnt.getOpcsna());
			tblKnsTranEror.setInbrch(acnt.getOpbrch());
		}
		
		tblKnsTranEror.setTranam(glpyam);
		tblKnsTranEror.setCrcycd(acnt.getCrcycd());
		tblKnsTranEror.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		tblKnsTranEror.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());
		tblKnsTranEror.setGlacct(acdrOt.getAcctno());
		tblKnsTranEror.setGlacna(acdrOt.getAcctna());
		if(CommUtil.isNotNull(acdrOt.getPayasqlist())&&acdrOt.getPayasqlist().size()>0){
			
			tblKnsTranEror.setGlseeq(acdrOt.getPayasqlist().get(0).getPayasq());
		}
		
		tblKnsTranEror.setDescrb(acnt.getErrmsg());
		//插入登记簿
		KnsTranErorDao.insert(tblKnsTranEror);
		//平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);
		
	}
	
	
	
	
}
