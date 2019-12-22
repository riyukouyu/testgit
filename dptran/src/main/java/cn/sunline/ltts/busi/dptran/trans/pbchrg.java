package cn.sunline.ltts.busi.dptran.trans;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;




public class pbchrg {

	private static final BizLog bizlog = BizLogUtil.getBizLog(pbchrg.class);
	/**
	 * 收费前处理
	 * @author songkailei
	 * @param input
	 * @param property
	 * @param output
	 */
public static void DtransBefore( final cn.sunline.ltts.busi.dptran.trans.intf.Pbchrg.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Pbchrg.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Pbchrg.Output output){
	
	String cardno = input.getCardno();//客户电子账号
	String otacna = input.getOtacna();//转出方户名
	String otbrch = input.getOtbrch();//转出方账户所属机构
	String crcycd = input.getCrcycd();//币种
	E_CSEXTG csextg = input.getCsextg();//钞汇标志
	String smrycd = input.getSmrycd();//摘要码
	BigDecimal tlcgam = input.getTlcgam();//收费总额
	String chckdt = input.getChckdt();//对账日期
	String keepdt = input.getKeepdt();//清算日期
	E_YES___ isckqt = input.getChkqtn().getIsckqt();
	
	if(CommUtil.isNull(cardno)){
		throw DpModuleError.DpstComm.BNAS0541();
		
	}
	
	if(CommUtil.isNull(otacna)){
		throw DpModuleError.DpstComm.BNAS0045();
		
	}
	
	if(!BusinessConstants.SUMMARY_SF.equals(smrycd))
		throw DpModuleError.DpstComm.BNAS1041();
	
	//客户电子账号和户名是否一致
	IoCaSevQryTableInfo qry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
	IoCaKnaAcdc  tblkna_acdc = qry.getKnaAcdcOdb2(cardno, false);
	if(CommUtil.isNull(tblkna_acdc)){
		throw DpModuleError.DpstComm.BNAS1205();
	}
	String custac = tblkna_acdc.getCustac();//根据客户电子账号获取电子账号ID
	
	//获取电子账户分类
	E_ACCATP accttp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac);//账户分类
	
	//设置属性区接口
	property.setCustac(custac);
	property.setAccttp(accttp);
	property.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); //交易渠道 
	property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //渠道来源流水
	property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date()); //渠道来源日期 
	property.setChgflg(E_CHGFLG.ALL);
	
	IoCaKnaCust tblkn_cust = qry.getKnaCustByCardnoOdb1(cardno, false);
	String custna = tblkn_cust.getCustna();//根据电子账户ID获取客户名称
	
	if(!CommUtil.equals(otacna, custna)){
		throw DpModuleError.DpstComm.BNAS0307();
		
	}
	
	CapitalTransCheck.ChkAcctstOT(tblkn_cust.getCustac()); //转出方电子账户状态校验
	
	String brchno = tblkn_cust.getBrchno();//获取电子账户所属机构
	if(!CommUtil.equals(otbrch, brchno)){
		throw DpModuleError.DpstComm.BNAS0043();
		
	}
	
	//币种检查
	if(CommUtil.isNull(crcycd)){
		throw DpModuleError.DpstAcct.BNAS0634();
		
	}
	IoDpSrvQryTableInfo qryt = SysUtil.getInstance(IoDpSrvQryTableInfo.class);
	//获取客户账户币种
	KnaAcct tblKnaAcct = CapitalTransDeal.getSettKnaAcctAc(custac);	
	
	if(CommUtil.compare(crcycd,tblKnaAcct.getCrcycd()) > 0){
		throw DpModuleError.DpstComm.BNAS0632();
		
	}
		
	CapitalTransCheck.ChkAcctFrozOT(custac);//转出方状态字检查
	
	//余额检查  账户存在签约产品则查账户资金池余额  若没有签约产品则查结算户余额
	BigDecimal realam = input.getTlcgam();//收费总额
	
	//初始化账户可用余额
	BigDecimal usebal = BigDecimal.ZERO;
	
/*	// 获取转存签约明细信息
	IoCaKnaSignDetl cplkna_sign_detl = SysUtil.getInstance(IoCaSevQryTableInfo.class)
			.kna_sign_detl_selectFirst_odb2(tblKnaAcct.getAcctno(), E_SIGNST.QY, false);
	
	// 存在转存签约明细信息则取资金池可用余额
	if (CommUtil.isNotNull(cplkna_sign_detl)) {
		usebal = DpAcctProc.getProductBal(custac, crcycd, false);
	}else{   //否则取结算户余额
		
		usebal = DpAcctProc.getAcctOnlnbl(tblKnaAcct.getAcctno(), false);
	}*/
	
	// 可用余额 addby xiongzhao 20161223 
	usebal = SysUtil.getInstance(DpAcctSvcType.class)
			.getAcctaAvaBal(custac, tblKnaAcct.getAcctno(),
			        crcycd, E_YES___.YES, E_YES___.NO);
	
	if(CommUtil.compare(realam, usebal) > 0){
		throw DpModuleError.DpstComm.BNAS0442();
	}
	
	if(CommUtil.isNull(csextg)){
		throw DpModuleError.DpstComm.BNAS1047();
		
	}
	
	if(CommUtil.isNull(smrycd)){
		throw DpModuleError.DpstComm.BNAS0195();
		
	}
	
	if(CommUtil.isNull(tlcgam)){
		throw DpModuleError.DpstComm.BNAS0331();
		
	}
	
	if(CommUtil.compare(tlcgam, BigDecimal.ZERO) <= 0){
		throw DpModuleError.DpstComm.BNAS0330();
	}
	
	if(CommUtil.isNull(chckdt)){
		throw DpModuleError.DpstComm.BNAS0808();
		
	}
	
	if(CommUtil.isNull(keepdt)){
		throw DpModuleError.DpstComm.BNAS0595();
		
	}
	
	if(CommUtil.isNull(isckqt)){
		throw DpModuleError.DpstComm.BNAS0807();
	}
	
	//循环检验输入信息
	BigDecimal total = BigDecimal.ZERO;
	List<IoCgCalCenterReturn> list = input.getChrgpm();//获取输入收费信息
	
	for(IoCgCalCenterReturn info : list){
		
		String chrgcd = info.getChrgcd();//费种代码
		BigDecimal clcham = info.getClcham();//应收费用金额（未优惠）
		BigDecimal dircam = info.getDircam();//优惠后应收金额
		BigDecimal paidam = info.getPaidam();//实收金额
		String trinfo = info.getTrinfo();//交易信息
		
		if(CommUtil.isNull(chrgcd)){
			throw DpModuleError.DpstComm.BNAS0777();
			
		}
		
		if(CommUtil.isNull(clcham) || CommUtil.compare(clcham, BigDecimal.ZERO) <= 0){
			throw DpModuleError.DpstComm.BNAS0245();
			
		}
		
		if(CommUtil.isNull(dircam) || CommUtil.compare(dircam, BigDecimal.ZERO) < 0){
			throw DpModuleError.DpstComm.BNAS0238();
			
		}
		
		if(CommUtil.compare(clcham, dircam) < 0){
			throw DpModuleError.DpstComm.BNAS0239();
		}
		
		if(CommUtil.isNull(paidam) || CommUtil.compare(paidam, BigDecimal.ZERO) < 0){
			throw DpModuleError.DpstComm.BNAS0355();
			
		}
		
		if(CommUtil.isNull(trinfo)){
			throw DpModuleError.DpstComm.BNAS0602();
			
		}
		
		total = total.add(paidam);
	}
	
	//收费总额与实收总额是否一致
	if(CommUtil.compare(tlcgam, total) > 0){
		throw DpModuleError.DpstComm.BNAS0329();
		
	}
}

public static void DtransAfter( final cn.sunline.ltts.busi.dptran.trans.intf.Pbchrg.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Pbchrg.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Pbchrg.Output output){
	
	//平衡性检查
	SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
	
	output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
	output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
	output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
}
}
