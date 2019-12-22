package cn.sunline.ltts.busi.catran.trans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.type.CaCustInfo;
import cn.sunline.ltts.busi.ca.type.CaCustInfo.acctAllInfos;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CHNLID;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class slacct {

public static void prcSelAcctAllInfos( final cn.sunline.ltts.busi.catran.trans.intf.Slacct.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Slacct.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Slacct.Output output){

	// 检查输入项
	String sCustac = "";// 电子账号ID
	String sCardno = input.getCardno();// 电子账号
	String sAcalno = input.getAcalno();// 绑定手机号
	String sCustid = input.getCustid();// 用户ID
	E_IDTFTP eIdtftp = input.getIdtftp();// 证件类型
	String sIdtfno = input.getIdtfno();// 证件号码
	E_CHNLID servtp = input.getServtp2();//渠道类型
	BigDecimal acctbl = BigDecimal.ZERO;// 可支取余额
	BigDecimal onlnbl = BigDecimal.ZERO; // 当前账户余额
	String crcycd = null;// 币种
	E_CSEXTG csextg = null;// 钞汇标识
	
	// 电子账号、证件号码、绑定手机号、用户ID不能同时为空
	if (CommUtil.isNull(sIdtfno) && CommUtil.isNull(sAcalno)
			&& CommUtil.isNull(sCustid) && CommUtil.isNull(sCardno)) {
		throw CaError.Eacct.BNAS0950();
	}
	
	if (CommUtil.isNotNull(sCardno)) {
		if(CommUtil.isNotNull(sAcalno)||CommUtil.isNotNull(sCustid)||CommUtil.isNotNull(sIdtfno)){
			throw CaError.Eacct.BNAS0111();
		}		
	}else if(CommUtil.isNotNull(sAcalno)){
		if(CommUtil.isNotNull(sCustid)||CommUtil.isNotNull(sIdtfno)){
			throw CaError.Eacct.BNAS0111();
		}
	}else if(CommUtil.isNotNull(sCustid)){
		if(CommUtil.isNotNull(sIdtfno)){
			throw CaError.Eacct.BNAS0111();
		}
	}
//	if(CommUtil.isNull(servtp)){
//	    throw DpModuleError.DpstComm.E9999("渠道类型不能为空!");
//	}
	List<String> custacInfo = new ArrayList<>();//集合中存储查询的电子账号ID
	// 如果电子账号不为空，根据电子账号查询出电子账号ID
	if (CommUtil.isNotNull(sCardno)) {
		IoCaKnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCard(sCardno, false);
		if (CommUtil.isNull(tblKnaAcdc)) {
			throw CaError.Eacct.BNAS1279();
		}
//		CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaAcdc.getCorpno());
		sCustac = tblKnaAcdc.getCustac();
		custacInfo.add(sCustac);
	}
	// 如果用户ID不为空，根据用户ID查询出电子账号
	if (CommUtil.isNotNull(sCustid)) {
		List<KnaCust> tblKnaCusts = CaDao.selCustAllInfoByCustid(sCustid, false);
		if (tblKnaCusts.size()<=0) {
			throw CaError.Eacct.BNAS0390();
		}
		for(KnaCust tblKnaCust:tblKnaCusts){
			sCustac=tblKnaCust.getCustac();
			custacInfo.add(sCustac);
		}
//		CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaCust.getCorpno());
//		sCustac = tblKnaCust.getCustac();
	}
	// 如果手机号不为空，根据绑定手机号查询出电子账号ID
	if (CommUtil.isNotNull(sAcalno)) {
		
		//检查手机号长度和是否为全为数字
		if (sAcalno.length() != 11) {
			throw CaError.Eacct.BNAS0397();
		}
		
		if (!BusiTools.isNum(sAcalno)) {
			throw CaError.Eacct.BNAS0319();
		}
		
		List<KnaAcal> tblKnaAcals = CaDao.selKnaAcalInfoByAcalno(sAcalno, E_ACALTP.CELLPHONE, false);
		if (tblKnaAcals.size()<=0) {
			throw CaError.Eacct.BNAS0392();
		}
		for(KnaAcal tblKnaAcal:tblKnaAcals){
			sCustac = tblKnaAcal.getCustac();
			custacInfo.add(sCustac);
		}
//		CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaAcal.getCorpno());
//		sCustac = tblKnaAcal.getCustac();
	}
	// 如果证件类型证件号码不为空，查询出电子账号ID
	if (CommUtil.isNotNull(sIdtfno) && CommUtil.isNotNull(eIdtftp)) {
		
		//校验证件类型、证件号码
//        BusiTools.chkCertnoInfo(eIdtftp, sIdtfno);
        
		List<IoCaKnaCust> cplKnaCusts = EacctMainDao.selCustAllInfosByIdtfno(eIdtftp, sIdtfno, false);
		if (cplKnaCusts.size()<=0) {
			throw CaError.Eacct.BNAS0721();
		}
		for(IoCaKnaCust cplKnaCust:cplKnaCusts ){
			sCustac = cplKnaCust.getCustac();
			custacInfo.add(sCustac);
		}
//		CommTools.getBaseRunEnvs().setBusi_org_id(cplKnaCust.getCorpno());
//		sCustac = cplKnaCust.getCustac();
		
	} else if (!(CommUtil.isNull(sIdtfno) && CommUtil.isNull(eIdtftp))) {
		throw CaError.Eacct.BNAS0148();
	}
	
	
	if(custacInfo.size()<=0){
		return;
	}
//	List<CaCustInfo.acctAllInfos> result = new ArrayList<CaCustInfo.acctAllInfos>();
	Options<CaCustInfo.acctAllInfos> results = new DefaultOptions<CaCustInfo.acctAllInfos>();
	//循环获取的每个电子账户
	for(String custac:custacInfo){
		
		//查询电子账户状态
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);	
		if (cuacst == E_CUACST.DELETE) {
			throw CaError.Eacct.BNAS1284();
		}
		//将法人set到公共报文
//		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(custac, false);
//		KnaCust tblKnaCust = CaDao.selKnaCustByCustac(custac, false);
//		CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaCust.getCorpno());
		
		// 查询电子账户基本信息
		List<acctAllInfos> acctInfos = null;
		if(CommUtil.isNull(servtp)){
		    acctInfos=EacctMainDao.selCustInfosbyCustac(custac, E_ACALTP.CELLPHONE, false);
		}else{
		    acctInfos=EacctMainDao.selCustInfosByCustacAndServtp(custac, E_ACALTP.CELLPHONE, servtp, false);
		}
		
		if (acctInfos.size()<=0) {
//			throw CaError.Eacct.BNAS0762();
		    continue;
		}
		
		//查询电子账户分类
		E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac);
		
		//判断状态字
		if(cuacst == E_CUACST.PREOPEN || cuacst == E_CUACST.CLOSED){
			if(cuacst == E_CUACST.CLOSED){
				// 查询销户信息
				IoDpKnaAcct tblKnaAcct = SysUtil.getInstance(IoDpKnaAcct.class);
				if (eAccatp == E_ACCATP.GLOBAL || eAccatp == E_ACCATP.FINANCE) {
					tblKnaAcct = EacctMainDao.selKnaAcctByacsetp(custac, E_ACSETP.SA, true);
				} else if (eAccatp == E_ACCATP.WALLET) {
					tblKnaAcct = EacctMainDao.selKnaAcctByacsetp(custac, E_ACSETP.MA, true);
				}
				onlnbl = tblKnaAcct.getOnlnbl(); // 当前账户余额
				crcycd = tblKnaAcct.getCrcycd();// 币种
				csextg = tblKnaAcct.getCsextg();// 钞汇标识
			    acctbl = BigDecimal.ZERO;// 可支取余额
			}
		}else{
			// 查询当前余额
//			IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);
			// 检查查询结果是否为空
//			if (CommUtil.isNotNull(cplKnaAcct)) {
//				
//			    onlnbl = cplKnaAcct.getOnlnbl(); // 当前账户余额
//				crcycd = cplKnaAcct.getCrcycd();// 币种
//				csextg = cplKnaAcct.getCsextg();// 钞汇标识
//				
//				// 可用余额
//			    acctbl = SysUtil.getInstance(DpAcctSvcType.class)
//						.getAcctaAvaBal(custac, cplKnaAcct.getAcctno(),
//								crcycd, E_YES___.YES, E_YES___.NO);
//				
//				/*// 查询可支取余额
//				// 获取转存签约明细信息
//				IoCaKnaSignDetl cplkna_sign_detl = SysUtil.getInstance(
//						IoCaSevQryTableInfo.class).kna_sign_detl_selectFirst_odb2(
//						cplKnaAcct.getAcctno(), E_SIGNST.QY, false);
//	
//				// 存在转存签约明细信息则取资金池可用余额
//				if (CommUtil.isNotNull(cplkna_sign_detl)) {
//					acctbl = SysUtil.getInstance(DpAcctSvcType.class)
//							.getProductBal(custac, cplKnaAcct.getCrcycd(), false);
//				} else {
//					// 其他取账户余额,正常的支取交易排除冻结金额
//					acctbl = SysUtil.getInstance(DpAcctSvcType.class)
//							.getOnlnblForFrozbl(cplKnaAcct.getAcctno(), false);
//				}
//	*/
//			}
		}
		
		// 面签标识查询
//		E_YES___ facesg = EacctMainDao.selFacesgByCustac(sCustac, true);
		
		//给每个电子账户输出的字段赋值
		for(acctAllInfos acctInfo:acctInfos){
				
			//若电子账户状态为结清则销户日期返回空
				if(acctInfo.getActtst() == E_ACCTST.SETTLE){
					acctInfo.setClosdt(null);
				}
				
				// 电子账户状态字查询
//				IoDpAcStatusWord cplGetAcStWord =  SysUtil.getInstance(IoDpFrozSvcType.class)
//											.getAcStatusWord(custac);
				
				//初始化电子账户全部信息输出信息
				CaCustInfo.acctAllInfos acctAllInfos = SysUtil.getInstance(CaCustInfo.acctAllInfos.class);
				
				acctAllInfos.setOnlnbl(onlnbl);// 当前账户余额
				acctAllInfos.setAcalno(acctInfo.getAcalno());// 绑定手机号码
				acctAllInfos.setAcalst(acctInfo.getAcalst());//绑定手机状态
				acctAllInfos.setAcctbl(acctbl);// 账户余额
				acctAllInfos.setAcctst(E_CUACST.NORMAL);// 账户状态
				acctAllInfos.setAccttp(E_ACCATP.FINANCE);// 电子账户分类
//				acctAllInfos.setAcctst(cuacst);// 账户状态
//				acctAllInfos.setAccttp(eAccatp);// 电子账户分类
//				acctAllInfos.setAcstsz(cplGetAcStWord.getAcstsz());// 电子账户状态字
				acctAllInfos.setBrchno(acctInfo.getBrchno());// 归属机构
				acctAllInfos.setCardno(acctInfo.getCardno());// 虚拟交易卡号
				acctAllInfos.setClosdt(acctInfo.getClosdt());// 销户日期
				acctAllInfos.setCrcycd("CNY");// 币种
//				acctAllInfos.setCsextg(E_CSEXTG.NONE);// 钞汇标识
				acctAllInfos.setCustac(custac);// 电子账号	
//				acctAllInfos.setCustcd(acctInfo.getCustcd());// 客户内码
//				acctAllInfos.setCustid(acctInfo.getCustid());// 用户ID
				acctAllInfos.setCustna(acctInfo.getCustna());// 客户名称
				acctAllInfos.setIdtfno(acctInfo.getIdtfno());// 证件号码
				acctAllInfos.setIdtftp(acctInfo.getIdtftp());// 证件类型
				acctAllInfos.setOpenbr(acctInfo.getOpenbr());// 开户结构
				acctAllInfos.setOpendt(acctInfo.getOpendt());// 开户日期
//				acctAllInfos.setFacesg(facesg);
				results.add(acctAllInfos);
			}	
		}
	    if(results.size()<=0){
	        throw CaError.Eacct.BNAS0762();
	    }
		CommTools.getBaseRunEnvs().setTotal_count(Long.valueOf(results.size()));// 总笔数
		output.setAcctInfosList(results);
	}
}
