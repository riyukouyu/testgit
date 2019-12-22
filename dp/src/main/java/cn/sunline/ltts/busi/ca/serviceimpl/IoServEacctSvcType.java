package cn.sunline.ltts.busi.ca.serviceimpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.edsp.busi.dp.errors.DpCaError;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.ca.eacct.process.CaEcctProc;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccsDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbTrin;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbTrinDao;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType.QryTransferDetail.Input;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType.QryTransferDetail.Output;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpHKnlBill;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbAcin;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnlBill;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.AcctCabrInfoList;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaInfoOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkOT;
import cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctBlHtyDtlOut;
import cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctCustnaCardno;
import cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctInfoBal;
import cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctInfoOut;
import cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctTranHistyOut;
import cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctTransDetailInfoOut;
import cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctTransDtlInfo;
import cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctTransDtlInfoOut;
import cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctTransDtlNameInfo;
import cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctTransHistyDtl;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.ltts.busi.sys.dict.DpDict;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BSCDTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ECCTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_FAIL__;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_FLOWTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CORRTG;
 /**
  * 电子账户信息服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoServEacctSvcType", longname="电子账户信息服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoServEacctSvcType implements cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType{

	private static char sperate = '|';
	private static final BizLog bizlog = BizLogUtil.getBizLog(IoServEacctSvcType.class);
	
/**
  * 电子账户信息查询
  *
  */
	public cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctInfoOut qryEacctInfo( String ecctno,  String crcycd){
		if(CommUtil.isNull(ecctno)){
			throw DpModuleError.DpstAcct.BNAS0311();
		}
		
		if(CommUtil.isNull(crcycd)){
			throw CaError.Eacct.BNAS0663();
		}
		
		// 判断电子账号是否存在
		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(ecctno, false);
		if (CommUtil.isNull(tblKnaCust)) {
			throw CaError.Eacct.BNAS0750();
		}
		if (tblKnaCust.getAcctst() == E_ACCTST.CLOSE) {
			throw CaError.Eacct.BNAS0906();
		}
		
		EacctInfoOut out = SysUtil.getInstance(EacctInfoOut.class);

		BigDecimal holdbl = BigDecimal.ZERO;  //冻结余额.需要改造
		DpAcctSvcType dpAcctSvc = SysUtil.getInstance(DpAcctSvcType.class);
		BigDecimal avalbl = dpAcctSvc.getProductBal(ecctno, crcycd, false);  //可用余额
		
		StringBuffer strCust = new StringBuffer();//电子账户信息
		StringBuffer strFnac = new StringBuffer(); //小马理财
		
		BigDecimal fnacOnlnbl = BigDecimal.ZERO; //小马理财投标金额
		BigDecimal fnacNdrcin = BigDecimal.ZERO; //小马理财代收利息
		boolean isExistFnac = false;//是否购买小马理财标志
		
		
		BigDecimal totalbl = BigDecimal.ZERO;
		
		List<KnaAccs> lstKnaAccs = KnaAccsDao.selectAll_odb5(ecctno, true);
		AcctCabrInfoList totalpf = SysUtil.getInstance(DpAcctSvcType.class).selAcctCabrInfo(ecctno);
		BigDecimal cabrsm = totalpf.getCabrsm();
		
		//收益为空的时候设置为0
		if(CommUtil.isNull(cabrsm)){
			cabrsm = BigDecimal.ZERO;
		}
		avalbl = BusiTools.roundByCurrency(crcycd,avalbl,null);
		holdbl = BusiTools.roundByCurrency(crcycd,holdbl,null);
		cabrsm = BusiTools.roundByCurrency(crcycd,cabrsm,null);
		
		strCust.append(ecctno).append(sperate).append(avalbl).append(sperate).append(holdbl).append(sperate).append(cabrsm);
		Options<EacctInfoBal> lstbal = out.getLstbal();
		EacctInfoBal infoBal = SysUtil.getInstance(EacctInfoBal.class);
		infoBal.setAccttp("1");//电子账户级别余额
		infoBal.setAcctvl(strCust.toString());
		lstbal.add(infoBal);
		totalbl = totalbl.add(avalbl).add(holdbl);
		for(KnaAccs accs:lstKnaAccs){
			if(accs.getAcctst()!=E_DPACST.NORMAL&& !accs.getCrcycd().equals(crcycd)){
				continue;
			}
			
			infoBal = SysUtil.getInstance(EacctInfoBal.class);
			
			//存款,已在上面做处理，现在不做处理
			if(accs.getProdtp()==E_PRODTP.DEPO){
				continue;
			}
			
//			//小马金融
//			if(accs.getProdtp()==E_PRODTP.FNAC){
//				fca_acct tblfacAcct = Fca_acctDao.selectOne_odb1(accs.getAcctno(), false);
//				if(CommUtil.isNotNull(tblfacAcct)){
//					DpProdSvc.FnacInfo tblfca_acct = DpAcctDao.selfnacOnlnblAndNdrcin(accs.getAcctno(), false);
//					if(CommUtil.isNotNull(tblfca_acct)){
//						//获取账户余额
//						fnacOnlnbl = fnacOnlnbl.add(tblfca_acct.getOnlnbl());
//						fnacOnlnbl = CommTools.roundByCrcy(crcycd,fnacOnlnbl);
//						//获取代收利息
//						fnacNdrcin = fnacNdrcin.add(tblfca_acct.getNdrcin());
//						fnacNdrcin = CommTools.roundByCrcy(crcycd,fnacNdrcin);
//						
//						totalbl = totalbl.add(CommTools.roundByCrcy(crcycd,tblfca_acct.getOnlnbl()));
//					}
//						
//					isExistFnac = true;
//				}
//			}
			
			
			//基金
//			if(accs.getProdtp()==E_PRODTP.FUND){
//				
//				//基金账户
//				fda_fund tblFda_fund = Fda_fundDao.selectOne_odb1(accs.getAcctno(), false);
//				
//				//基金产品信息
//				fup_prod tblFup_prod = Fup_prodDao.selectOne_odb1(accs.getProdcd(), false); //产品号
//				
//				BigDecimal lsprft = BigDecimal.ZERO;
//				
//				String pfupdt = FdDao.selMaxPfupdt(false);
//				
//				// 收益更新日期等于交易日期，则表示上日有收益
//				if(CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_date(), tblFda_fund.getPfupdt())
//						||CommUtil.equals(CommTools.getBaseRunEnvs().getLast_date(), pfupdt)){
//					
//					lsprft = tblFda_fund.getLsprft();	//基金上日累计收益
//					lsprft = tblFda_fund.getProfit().subtract(lsprft);//基金上日收益 = 累计收益 - 上日累计收益
//				}
//				
//				
//				BigDecimal frozbl = tblFda_fund.getFrozbl();	//基金冻结份额
//				BigDecimal onlnbl = tblFda_fund.getOnlnbl();	//基金份额
//				totalbl = totalbl.add(onlnbl.add(frozbl));		//加上基金份额
//				
//				//保留2位小数
//				lsprft = CommTools.roundByCrcy(crcycd,lsprft);
//				frozbl = CommTools.roundByCrcy(crcycd,frozbl);
//				onlnbl = CommTools.roundByCrcy(crcycd,onlnbl);
//				totalbl = CommTools.roundByCrcy(crcycd,totalbl);
//				strFund.append(onlnbl.add(frozbl)).append(sperate).append(tblFup_prod.getSeinrt()).append(sperate)
//				.append(lsprft).append(sperate).append(1);
//				infoBal = SysUtil.getInstance(EacctInfoBal.class);
//				infoBal.setAccttp("2");//基金产品余额信息
//				infoBal.setAcctvl(strFund.toString());
//				lstbal.add(infoBal);
//				
//			}
			//保险
//			if(accs.getProdtp()==E_PRODTP.INSU){
//				//保险账户
//				dfa_hold tblDfa_hold = Dfa_holdDao.selectOne_odx1(accs.getAcctno(), true);
//				
//				//产品信息
//				dfp_prod tblDfp_prod = Dfp_prodDao.selectOne_odb2(accs.getProdcd(), true);//险种代码
//				List<dfa_hold_detl> lstDfa_hold_detl = DfAcctDao.selInsuDetailByAcctSt(accs.getAcctno(), false);
//				BigDecimal lastcm = BigDecimal.ZERO;	//上日收益
//				BigDecimal insucm = BigDecimal.ZERO;	//累计收益=保险累计收益+银行补贴累计收益
//				BigDecimal acctvl = tblDfa_hold.getOnlnbl();//账户价值=总金额+累计收益
//				totalbl = totalbl.add(acctvl);		//加上账户价值
//				for(dfa_hold_detl tblDfa_hold_detl:lstDfa_hold_detl){
//					lastcm = lastcm.add(tblDfa_hold_detl.getLastcm());//上日收益
//					insucm = insucm.add(tblDfa_hold_detl.getInsucm().add(tblDfa_hold_detl.getBankcm()));//累计收益=保险累计收益+银行补贴累计收益
//				}
//				
//				acctvl = CommTools.roundByCrcy(crcycd,acctvl);
//				lastcm = CommTools.roundByCrcy(crcycd,lastcm);
//				insucm = CommTools.roundByCrcy(crcycd,insucm);
//				
//				strInsu.append(acctvl).append(sperate).append(tblDfp_prod.getHtyevl()).append(sperate)
//				.append(insucm).append(sperate).append(1).append(sperate).append(tblDfp_prod.getMaxxam()).
//				append(sperate).append(tblDfp_prod.getIncram()).append(sperate).append(tblDfp_prod.getIuskcd()).
//				append(sperate).append(tblDfp_prod.getProdna()).append(sperate).append(tblDfp_prod.getThktem());
//				infoBal = SysUtil.getInstance(EacctInfoBal.class);
//				infoBal.setAcctvl(strInsu.toString());
//				infoBal.setAccttp("3");//保险产品余额信息
//				lstbal.add(infoBal);
//			}
		}
		
		//基金是否已申购
//		if(strFund.length()==0){
//			List<fup_prod> lstFup_prod = Fup_prodDao.selectAll_odb4(E_PRODST.NORMAL, false);
//			for(fup_prod prod:lstFup_prod){
//				strFund = new StringBuffer();
//				
//				strFund.append(zero).append(sperate).append(prod.getSeinrt()).append(sperate)
//				.append(zero).append(sperate).append(0);
//				infoBal = SysUtil.getInstance(EacctInfoBal.class);
//				infoBal.setAccttp("2");//基金产品余额信息
//				infoBal.setAcctvl(strFund.toString());
//				lstbal.add(infoBal);
//			}
//		}
		
		
		//保险是否已申购
//		if(strInsu.length()==0){
//			List<dfp_prod> lstDfp_prod = Dfp_prodDao.selectAll_odb3(E_PRODST.NORMAL, false);
//			for(dfp_prod prod:lstDfp_prod){
//				strInsu.append(zero).append(sperate).append(prod.getHtyevl()).append(sperate)
//				.append(zero).append(sperate).append(0).append(sperate).append(prod.getMaxxam()).
//				append(sperate).append(prod.getIncram()).append(sperate).append(prod.getIuskcd()).
//				append(sperate).append(prod.getProdna()).append(sperate).append(prod.getThktem());
//				infoBal = SysUtil.getInstance(EacctInfoBal.class);
//				infoBal.setAcctvl(strInsu.toString());
//				infoBal.setAccttp("3");//保险产品余额信息
//				lstbal.add(infoBal);
//			}
//			
//		}
		
		//定存368单独处理，因为定存368存在被冲正的情况
		/*kup_dppb  dppb368 = Kup_dppbDao.selectOne_odb1(ApUtil.DEPT_PROD_368, false);
		if(CommUtil.isNotNull(dppb368)  && dppb368.getProdst() == E_PRODST.NORMAL){
			List<kna_fxac> lstkna_fxac368 = DpAcctQryDao.selKnaFxacByCustacAndProdcd(ecctno,ApUtil.DEPT_PROD_368,crcycd, false);
			
			if (CommUtil.isNotNull(lstkna_fxac368)) {
				for (kna_fxac tblkna_fxac368 : lstkna_fxac368) {
					strfxac368.delete(0, strfxac368.length());
					/*strfxac368 = getFxacAcctValue(tblkna_fxac368, crcycd,
							ApUtil.DEPT_PROD_368);*/
		/*			Map<String, Object> result = getFxacAcctValue(tblkna_fxac368, crcycd,
							ApUtil.DEPT_PROD_368);
					strfxac368 = (StringBuffer) result.get("resultStr");
					infoBal = SysUtil.getInstance(EacctInfoBal.class);
					infoBal.setAccttp("4");
					strfxac368 = strfxac368.append(sperate).append(1);
					infoBal.setAcctvl(strfxac368.toString());
					lstbal.add(infoBal);
					totalbl = totalbl.add((BigDecimal) result.get("totalAm"));
					totalbl = CommTools.roundByCrcy(crcycd,totalbl);
				}
			}else{
				strfxac368.delete(0, strfxac368.length());
				Map<String, Object> result = getFxacAcctValue(null, crcycd, ApUtil.DEPT_PROD_368);
				strfxac368 = (StringBuffer) result.get("resultStr");
				infoBal = SysUtil.getInstance(EacctInfoBal.class);
				infoBal.setAccttp("4");
				strfxac368 = strfxac368.append(sperate).append(0);
				infoBal.setAcctvl(strfxac368.toString());
				lstbal.add(infoBal);
			}
		} */
		//定存788
		/*kup_dppb  dppb788 = Kup_dppbDao.selectOne_odb1(ApUtil.DEPT_PROD_788, false);
		if(CommUtil.isNotNull(dppb788)  && dppb788.getProdst() == E_PRODST.NORMAL){
			List<kna_fxac> lstkna_fxac788 = DpAcctQryDao.selKnaFxacByCustacAndProdcd(ecctno,ApUtil.DEPT_PROD_788,crcycd, false);
			if (CommUtil.isNotNull(lstkna_fxac788)) {
				for (kna_fxac tblkna_fxac788 : lstkna_fxac788) {
					strfxac788.delete(0, strfxac788.length());
					Map<String, Object> result = getFxacAcctValue(tblkna_fxac788, crcycd,
							ApUtil.DEPT_PROD_788);
					strfxac788 = (StringBuffer) result.get("resultStr");
					infoBal = SysUtil.getInstance(EacctInfoBal.class);
					infoBal.setAccttp("5");
					strfxac788 = strfxac788.append(sperate).append(1);
					infoBal.setAcctvl(strfxac788.toString());
					lstbal.add(infoBal);
					totalbl = totalbl.add((BigDecimal) result.get("totalAm"));
					totalbl = CommTools.roundByCrcy(crcycd,totalbl);
				}
			}else{
				strfxac788.delete(0, strfxac788.length());
				Map<String, Object> result = getFxacAcctValue(null, crcycd, ApUtil.DEPT_PROD_788);
				strfxac788 = (StringBuffer) result.get("resultStr");
				infoBal = SysUtil.getInstance(EacctInfoBal.class);
				infoBal.setAccttp("5");
				strfxac788 = strfxac788.append(sperate).append(0);
				infoBal.setAcctvl(strfxac788.toString());
				lstbal.add(infoBal);
			}
		}*/
		//小马理财
		if(!isExistFnac){
			strFnac.append(fnacOnlnbl).append(sperate).append(fnacNdrcin).append(sperate).append("090010001").append(sperate).append("0");;
			infoBal = SysUtil.getInstance(EacctInfoBal.class);
			infoBal.setAccttp("6");//小马理财
			infoBal.setAcctvl(strFnac.toString());
			lstbal.add(infoBal);
		}else{
			strFnac.append(fnacOnlnbl).append(sperate).append(fnacNdrcin).append(sperate).append("090010001").append(sperate).append("1");
			infoBal = SysUtil.getInstance(EacctInfoBal.class);
			infoBal.setAccttp("6");//小马理财
			infoBal.setAcctvl(strFnac.toString());
			lstbal.add(infoBal);
		}

		// 大麦查询近一个月待还款
//		LnQryLoanInfoImpl lnQryLoanInfo = SysUtil
//				.getInstance(LnQryLoanInfoImpl.class);
//		Map<String, BigDecimal> result = lnQryLoanInfo.qreLnLengdingAmount(
//				ecctno, CommTools.getBaseRunEnvs().getTrxn_date(), 30);
//		BigDecimal overdueAmount = result.get("pendingRepyFor30Days");
		StringBuffer strDm = SysUtil.getInstance(StringBuffer.class);
//		if (isExistDm) {
//            strDm.append("").append(sperate)
//					.append(overdueAmount).append(sperate).append("1");
//		} else {
//            strDm.append("").append(sperate)
//					.append(overdueAmount).append(sperate).append("0");
//		}
		infoBal = SysUtil.getInstance(EacctInfoBal.class);
        infoBal.setAccttp("7");// 贷款产品
		infoBal.setAcctvl(strDm.toString());
		lstbal.add(infoBal);

		out.setEcctno(ecctno); // 电子账号
		out.setEcctbl(totalbl); // 电子账户余额
		out.setCrcycd(crcycd); // 货币代码
		out.setOpendt(tblKnaCust.getOpendt()); // 开户日期
		out.setEcctst(tblKnaCust.getAcctst()); // 账户状态
		out.setBkactp(E_BSCDTP.DRCARD); // 银行方账户类型
		out.setAcname(tblKnaCust.getCustna()); // 账户名称

		return out;
	}
 /**
  * 电子账户交易明细查询
  *
  */
	public cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctTransDtlInfoOut qryEacctTransDtlInfo( final cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctTransDtlInfoIn transDtlInfoIn){
		E_ECCTTP type = transDtlInfoIn.getEccttp();
		String crcycd = transDtlInfoIn.getCrcycd();
		String ecctno = transDtlInfoIn.getEcctno();
		Long recdct = transDtlInfoIn.getRecdct();
		Long detlsq = transDtlInfoIn.getDetlsq();
		String bgindt= transDtlInfoIn.getBgindt();
		String eenddt = transDtlInfoIn.getEenddt();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String detlsqTrandt = transDtlInfoIn.getTrandt();		//下一页时传入上页最后一个数据的顺序号(detlsq),和日期(trandt)
		
		if(CommUtil.isNull(ecctno)){
			throw DpModuleError.DpstAcct.BNAS0311();
		}
		
		if(CommUtil.isNull(recdct)){
			throw CaError.Eacct.BNAS0461();
		}
		
		//第一页的时候顺序号为0，则查询第一个数据
		if(detlsq==0){
			detlsq = Long.getLong("999999999");
		}
		
		
		//类型为空默认查询所有交易
		if(CommUtil.isNull(type)){
			type = E_ECCTTP.ALL;
		}
		
		if(CommUtil.isNotNull(bgindt)){
			if(DateUtil.compareDate(bgindt, trandt)>0){
				throw CaError.Eacct.BNAS0556();
			}
			if(CommUtil.isNotNull(eenddt)){
				if(DateUtil.compareDate(bgindt, eenddt)>0){
					throw CaError.Eacct.BNAS0555();
				}
			}
		}
		
		//如果结束日期为空或结束日期大于交易日期，则结束日期为交易日期
		if(CommUtil.isNull(eenddt)||(CommUtil.compare(eenddt, trandt)>0)){
			eenddt = trandt;
		}
		
		if(CommUtil.isNull(crcycd)){
			throw CaError.Eacct.BNAS0663();
		}
		
		// 判断电子账号是否存在
		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(ecctno, false);
		if (CommUtil.isNull(tblKnaCust)) {
			throw CaError.Eacct.BNAS0750();
		}
		if (tblKnaCust.getAcctst() == E_ACCTST.CLOSE) {
			throw CaError.Eacct.BNAS0906();
		}

		EacctTransDtlInfoOut out =SysUtil.getInstance(EacctTransDtlInfoOut.class);
		int count=0;
		List<IoDpKnlBill> knl_billList = null;

		
		//结束日期小于当前日期，直接查询历史表h_knl_bill
		if(CommUtil.compare(eenddt, trandt)<0){
			
			knl_billList = EacctMainDao.selEacctTransDtlInfoHist(crcycd, ecctno, bgindt, eenddt, type, detlsq, recdct, false);
			
		}else if(CommUtil.compare(bgindt, trandt)==0){
			
			//开始日期等于交易日期，直接查询明细表knl_bill
			knl_billList = EacctMainDao.selEacctTransDtlInfoTrandt(crcycd, ecctno, bgindt, eenddt, type, detlsq, recdct, false);
			
		}else if(CommUtil.isNotNull(detlsqTrandt)){
			//上页的最后一条数据日期小于交易日期，直接查询历史数据表
			if(CommUtil.compare(detlsqTrandt, trandt)<0){
				knl_billList = EacctMainDao.selEacctTransDtlInfoHist(crcycd, ecctno, bgindt, eenddt, type, detlsq, recdct, false);
			}else{
				//查询历史表和明细表
				knl_billList = EacctMainDao.selEacctTransDtlInfoAll(crcycd, ecctno, bgindt, eenddt, type, detlsq, recdct, false);
			}
		}else{
			//查询历史表和明细表
			knl_billList = EacctMainDao.selEacctTransDtlInfoAll(crcycd, ecctno, bgindt, eenddt, type, detlsq, recdct, false);
		}
		count = EacctMainDao.selCountEacctTransDtlAll(crcycd, ecctno, bgindt, eenddt, type, false);
		
		for(IoDpKnlBill bill:knl_billList){
			EacctTransDtlInfo info = setQryResults(bill);
			info.setCrcycd(transDtlInfoIn.getCrcycd());
			out.getEcctlt().add(info);
		}
		
		out.setEcctno(transDtlInfoIn.getEcctno());		//电子账号
		out.setRecdsm(count);				//记录总数
		
		return out;
	}
	
	private static EacctTransDtlInfo setQryResults(IoDpKnlBill bill){
		EacctTransDtlInfo cplEacctInfo = SysUtil.getInstance(EacctTransDtlInfo.class);
		cplEacctInfo.setAmntcd(bill.getAmntcd());		//借贷标志
		cplEacctInfo.setTrandt(bill.getTrandt());		//交易日期
		String trantm = CommUtil.lpad(bill.getTrantm().toString(), 9, "0");//时间补齐9位
		cplEacctInfo.setTrantm(trantm);		//开始时间
		cplEacctInfo.setTranam(bill.getTranam());		//交易金额
		cplEacctInfo.setChnlno(bill.getServtp());		//交易渠道
		cplEacctInfo.setCrcycd(bill.getTrancy()); 		//币种
		cplEacctInfo.setAvalbl(bill.getAcctbl()); 		//账户余额
		cplEacctInfo.setSmryds(bill.getSmryds()); 		//摘要描述
		cplEacctInfo.setJnlseq(bill.getTransq()); 		//交易流水
		cplEacctInfo.setAmntcd(bill.getAmntcd()); 		//借贷标志
		if(bill.getAmntcd()==E_AMNTCD.CR){
			cplEacctInfo.setFlowtp(E_FLOWTP.I);			//转入
		}else if(bill.getAmntcd()==E_AMNTCD.DR){
			cplEacctInfo.setFlowtp(E_FLOWTP.O);      	//转出
		}
		if(bill.getCorrtg()==E_CORRTG._1){
			cplEacctInfo.setTranst(E_FAIL__.FAIL);
		}else{
			cplEacctInfo.setTranst(E_FAIL__.SUCCESS);
		}
		cplEacctInfo.setPrcsid(bill.getIntrcd());		//处理码
		cplEacctInfo.setDetlsq(bill.getDetlsq());		//顺序号
		
		return cplEacctInfo;
	}
	
 /**
  * 电子账户7日交易历史明细查询
  *
  */
	public cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctTranHistyOut qryEacctTransHstDtl7Day( String trandt,  String ecctno,  String crcycd){
		//有效性检查
		CaEcctProc.checkBeforePrc(ecctno, trandt, crcycd);
		
		EacctTranHistyOut out = SysUtil.getInstance(EacctTranHistyOut.class);
		String bgindt = DateUtil.dateAdd(Calendar.DATE, -7, trandt);
		String eenddt = DateUtil.dateAdd(Calendar.DATE, -1, trandt);
		List<IoDpHKnlBill> lstH_knl_bill = EacctMainDao.selTransHtyDtl(crcycd, ecctno, bgindt, eenddt, false);
		for(IoDpHKnlBill bill:lstH_knl_bill){
			EacctTransHistyDtl cplEacctInfo = SysUtil.getInstance(EacctTransHistyDtl.class);
			cplEacctInfo.setAmntcd(bill.getAmntcd());		//借贷标志
			cplEacctInfo.setTrandt(bill.getTrandt());		//交易日期
			cplEacctInfo.setStatdt(bill.getTrandt());		//开始日期
			cplEacctInfo.setTranam(bill.getTranam());		//交易金额
			cplEacctInfo.setChnlno(bill.getServtp());		//交易渠道
			cplEacctInfo.setAmntcd(bill.getAmntcd()); 		//借贷标志
			cplEacctInfo.setAvalbl(bill.getAcctbl()); 		//账户余额
			cplEacctInfo.setSmrycd(bill.getSmrycd()); 		//摘要代码
			cplEacctInfo.setJnlseq(bill.getTransq()); 		//交易流水
			cplEacctInfo.setJnlldt(bill.getTrandt());		//流水日期
			cplEacctInfo.setToacct(bill.getOpcuac());		//对方账号
			cplEacctInfo.setPartfo(bill.getOpacna());		//对方信息
			cplEacctInfo.setPartna(bill.getOpacna());  		//对方名称
			cplEacctInfo.setTrbrch(bill.getTranbr());		//交易机构
			cplEacctInfo.setPyacno(bill.getAcctno());		//本方账号
			cplEacctInfo.setPyacna(bill.getAcctna());		//账户名称
			cplEacctInfo.setPebkna(bill.getBankna());		//对方行名称
			out.getTrandl().add(cplEacctInfo);
		}
		return out;
	}
 /**
  * 电子账户7日余额历史明细查询
  *
  */
	public cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctBlHtyDtlOut qryEacctBlHstDtl7Day( String ecctno,  String trandt,  String crcycd){
		//有效性检查
		CaEcctProc.checkBeforePrc(ecctno, trandt, crcycd);
		EacctBlHtyDtlOut out = CaEcctProc.eacctBlHtyList(ecctno,trandt,crcycd,30);
		
		return out;
	}
 /**
  * 靠档预期收益查询
  *
  */
	public cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType.QryEacctPlanIn.Output qryEacctPlanIn( String ecctno,  String trandt,  String crcycd){
		//有效性检查
		CaEcctProc.checkBeforePrc(ecctno, trandt, crcycd);
		
		// 电子账户下定期支取金额部分总计提金额
		BigDecimal fxPlanIn =BigDecimal.ZERO;
		fxPlanIn = EacctMainDao.selSumCabrFxdrAm(ecctno, crcycd, trandt, false);
		if(CommUtil.isNull(fxPlanIn)){
			 fxPlanIn =BigDecimal.ZERO;
		}
		
		String acctno = CaTools.getAcctno(ecctno, crcycd);//获取活期账号
		
		//活期部分计提总额
		IoDpSrvQryTableInfo qryDpTable = SysUtil.getInstance(IoDpSrvQryTableInfo.class);
		IoDpKnbAcin cplKnbAcin = qryDpTable.getKnbAcinOdb1(acctno, false);
		
		//总额
		BigDecimal sum = BigDecimal.ZERO;
		if(CommUtil.isNotNull(cplKnbAcin)){
			sum = fxPlanIn.add(cplKnbAcin.getPlanin());//计提金额在本期积数里面
		}
		
		ServEacctSvcType.QryEacctPlanIn.Output out = SysUtil.getInstance(ServEacctSvcType.QryEacctPlanIn.Output.class);
		out.setEcctno(ecctno);
		out.setTrandt(trandt);
		out.setPlanin(sum);
		return out;
	}
	
  /**
  * 电子账户信息修改
  *
  */
	public void uptEacctInfo( String custac,  cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST acctst,  String emails,  String addres){
		//电子账户不能为空
		if(CommUtil.isNull(custac)){
			throw CaError.Eacct.E0013();
		}
		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(custac, true);
		//客户信息相关内容取消，模块拆分
//		IoSrvCfPerson.IoUpdateCifCust.InputSetter queryCifCust = SysUtil.getInstance(IoSrvCfPerson.IoUpdateCifCust.InputSetter.class);
//		IoSrvCfPerson cifCustServ = SysUtil.getInstance(IoSrvCfPerson.class);
//		queryCifCust.setCustno(tblKnaCust.getCustno());
//		queryCifCust.setEmails(emails);
//		queryCifCust.setAddres(addres);
//		cifCustServ.updateCifCust(queryCifCust);
		if(CommUtil.isNotNull(acctst)){
			tblKnaCust.setAcctst(acctst);
		}
		KnaCustDao.updateOne_odb1(tblKnaCust);
	}
 /**
  * 电子账户交易明细查询
  *
  */
	public cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctTransDtlInfoOut qryTransDetail( final cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctTransDtlInfoIn transDtlInfoIn){
		E_ECCTTP type = transDtlInfoIn.getEccttp();
		String crcycd = transDtlInfoIn.getCrcycd();
		String ecctno = transDtlInfoIn.getEcctno();
		Long pageno = transDtlInfoIn.getPageno();
		Long recdct = transDtlInfoIn.getRecdct();
		String bgindt= transDtlInfoIn.getBgindt();
		String eenddt = transDtlInfoIn.getEenddt();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		
		if(CommUtil.isNull(ecctno)){
			throw DpCaError.DpEacct.E0024(DpDict.Acct.acctno.getLongName());
		}
		
		if(CommUtil.isNull(pageno)){
			throw DpCaError.DpEacct.BNAS0249();
		}
		//页码不能为0
		if(pageno==0){
			throw DpCaError.DpEacct.BNAS0248();
		}
		
		if(CommUtil.isNull(recdct)){
			throw DpCaError.DpEacct.BNAS0461();
		}
		//每页容量不能超过20条
		if(recdct>20){
			throw DpCaError.DpEacct.BNAS0462();
		}
		
		//类型为空默认查询所有交易
		if(CommUtil.isNull(type)){
			type = E_ECCTTP.ALL;
		}
		
		if(CommUtil.isNotNull(bgindt)){
			if(DateUtil.compareDate(bgindt, trandt)>0){
				throw DpCaError.DpEacct.BNAS0556();
			}
			if(CommUtil.isNotNull(eenddt)){
				if(DateUtil.compareDate(bgindt, eenddt)>0){
					throw DpCaError.DpEacct.BNAS0555();
				}
			}
		}
		
		//如果结束日期为空或结束日期大于交易日期，则结束日期为交易日期
		if(CommUtil.isNull(eenddt)||(CommUtil.compare(eenddt, trandt)>0)){
			eenddt = trandt;
		}
		
		
		if(CommUtil.isNull(crcycd)){
			throw DpCaError.DpEacct.E0024(DpDict.Acct.crcycd.getLongName());
		}
		
		// 判断电子账号是否存在
		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(ecctno, false);
		if (CommUtil.isNull(tblKnaCust)) {
			throw DpCaError.DpEacct.BNAS0750();
		}
		if (tblKnaCust.getAcctst() == E_ACCTST.CLOSE) {
			throw DpCaError.DpEacct.BNAS0906();
		}
		
//		select a.*,b.jiaoyimc from (select * from knl_bill union select * from h_knl_bill) a, kapp_jioyxx b where (a.trandt <= '20160101') 
//		and a.trancy ='01' and a.custac='6000000001' and a.intrcd not in('auacin','fxtocu','instpy','api111') and a.intrcd = b.jiaoyima
//		order by a.trandt desc ;

		EacctTransDtlInfoOut out =SysUtil.getInstance(EacctTransDtlInfoOut.class);
		long begin=(transDtlInfoIn.getPageno()-1)*transDtlInfoIn.getRecdct(); 			//起始笔数
		int count=0;
		List<EacctTransDtlNameInfo> knl_billList = null;
		//自带分页
		knl_billList = EacctMainDao.selEacctTransDtlInfo(crcycd, ecctno, bgindt, eenddt,type, begin, recdct, false);
		count = EacctMainDao.selCountEacctTransDtl(crcycd, ecctno, bgindt, eenddt,type, false);
		count =count + EacctMainDao.selCountEacctTransDtlHist(crcycd, ecctno, bgindt, eenddt,type, false);
		
		for(EacctTransDtlNameInfo bill:knl_billList){
			EacctTransDtlInfo info = setQryResultForjiaoyimc(bill);
			info.setCrcycd(transDtlInfoIn.getCrcycd());
			out.getEcctlt().add(info);
		}
		
		out.setEcctno(transDtlInfoIn.getEcctno());		//电子账号
		out.setRecdsm(count);				//记录总数
		
		return out;
	}
	
	private static EacctTransDtlInfo setQryResultForjiaoyimc(EacctTransDtlNameInfo bill){
		EacctTransDtlInfo cplEacctInfo = SysUtil.getInstance(EacctTransDtlInfo.class);
		cplEacctInfo.setAmntcd(bill.getAmntcd());		//借贷标志
		cplEacctInfo.setTrandt(bill.getTrandt());		//交易日期
		String trantm = CommUtil.lpad(bill.getTrantm().toString(), 9, "0");//时间补齐9位
		cplEacctInfo.setTrantm(trantm);		//开始时间
		cplEacctInfo.setTranam(bill.getTranam());		//交易金额
		cplEacctInfo.setChnlno(bill.getServtp());		//交易渠道
		cplEacctInfo.setCrcycd(bill.getTrancy()); 		//币种
		cplEacctInfo.setAvalbl(bill.getAcctbl()); 		//账户余额
		cplEacctInfo.setSmryds(bill.getSmryds()); 		//摘要描述
		cplEacctInfo.setJnlseq(bill.getTransq()); 		//交易流水
		cplEacctInfo.setAmntcd(bill.getAmntcd()); 		//借贷标志
		if(bill.getAmntcd()==E_AMNTCD.CR){
			cplEacctInfo.setFlowtp(E_FLOWTP.I);			//转入
		}else if(bill.getAmntcd()==E_AMNTCD.DR){
			cplEacctInfo.setFlowtp(E_FLOWTP.O);      	//转出
		}
		if(bill.getCorrtg()==E_CORRTG._1){
			cplEacctInfo.setTranst(E_FAIL__.FAIL);
		}else{
			cplEacctInfo.setTranst(E_FAIL__.SUCCESS);
		}
		cplEacctInfo.setPrcsid(bill.getJiaoyimc());		//处理码
		cplEacctInfo.setDetlsq(bill.getDetlsq());		//顺序号
		
		return cplEacctInfo;
	}
	
 /**
  * 产品信息列表查询
  *
  */
	public cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType.QryProdInfo.Output qryProdInfo(){
		
		class QueryProdProp {
			@SuppressWarnings("unused")
			String prodcd;// 产品编码
			E_PRODTP prodtp;// 产品类型
			@SuppressWarnings("unused")
			String accttp;  // 账户类型
			@SuppressWarnings("unused")
			String crcycd;// 币种
			@SuppressWarnings("unused")
			E_TERMCD termcd; //存期
			

			public void setProdcd(String prodcd) {
				this.prodcd = prodcd;
			}

			public E_PRODTP getProdtp() {
				return prodtp;
			}

			public void setProdtp(E_PRODTP prodtp) {
				this.prodtp = prodtp;
			}


			public void setAccttp(String accttp) {
				this.accttp = accttp;
			}


			public void setCrcycd(String crcycd) {
				this.crcycd = crcycd;
			}


			public void setTermcd(E_TERMCD termcd) {
				this.termcd = termcd;
			}
		}
		
		List<QueryProdProp> queryProds = new ArrayList<QueryProdProp>();
		QueryProdProp prod1 = new QueryProdProp();
		prod1.setAccttp("1");
		prod1.setProdcd("010010002");
		prod1.setCrcycd(BusiTools.getDefineCurrency());
		prod1.setTermcd(E_TERMCD.T305);
		prod1.setProdtp(E_PRODTP.DEPO);
		queryProds.add(prod1);
		
		QueryProdProp prod2 = new QueryProdProp();
		prod2.setAccttp("2");
		prod2.setProdcd("040010001");
		prod2.setProdtp(E_PRODTP.FUND);
		queryProds.add(prod2);
		
		QueryProdProp prod3 = new QueryProdProp();
		prod3.setAccttp("3");
		prod3.setProdcd("050010001");
		prod3.setProdtp(E_PRODTP.INSU);
		queryProds.add(prod3);
		
		QueryProdProp prod4 = new QueryProdProp();
		prod4.setAccttp("4");
		prod4.setProdcd("010010003");
		prod4.setCrcycd(BusiTools.getDefineCurrency());
		prod4.setProdtp(E_PRODTP.DEPO);
		queryProds.add(prod4);
		
		QueryProdProp prod5 = new QueryProdProp();
		prod5.setAccttp("5");
		prod5.setProdcd("010010004");
		prod5.setCrcycd(BusiTools.getDefineCurrency());
		prod5.setProdtp(E_PRODTP.DEPO);
		queryProds.add(prod5);
		
		QryProdInfo.Output out = SysUtil.getInstance(QryProdInfo.Output.class);
		
		for(QueryProdProp prod : queryProds){
			
			
			// 存款
			if(prod.getProdtp() == E_PRODTP.DEPO){
				//通过产品配置过滤是否返回给前台数据
				/*kup_dppb dppb = Kup_dppbDao.selectOne_odb1(prod.getProdcd(), false);
				if(CommUtil.isNotNull(dppb) && dppb.getProdst() == E_PRODST.NORMAL){
					if(CommUtil.isNull(prod.getTermcd())){
						List<kup_dppb_term> lstKup_dppb_term = Kup_dppb_termDao.selectAll_odb2(prod.getProdcd(), true);
						if(lstKup_dppb_term.size() != 1){
							throw CaError.Eacct.E0001("产品下有多个或没有存期，需指定输入存期！");
						}
						kup_dppb_term tblKup_dppb_term = lstKup_dppb_term.get(0);
						prod.setTermcd(tblKup_dppb_term.getDepttm());
					}
					
					kup_dppb tblKup_dppb = Kup_dppbDao.selectOne_odb1(prod.getProdcd(), true);
					kup_dppb_post tblKup_dppb_post = Kup_dppb_postDao.selectOne_odb1(prod.getProdcd(), prod.getCrcycd(), true);
					kup_dppb_intr tblKup_dppb_intr = Kup_dppb_intrDao.selectOne_odb1(prod.getProdcd(), prod.getCrcycd(), true);
					IntrPublicEntity entity = SysUtil.getInstance(IntrPublicEntity.class);
					entity.setCrcycd(prod.getCrcycd());
					entity.setDepttm(prod.getTermcd());
					entity.setIntrcd(tblKup_dppb_intr.getIntrcd());
					entity.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
					
					if(CommUtil.compare("1", prod.getAccttp()) == 0){
						IntrPublic.genIntrvl(entity, 1800);
						strfx.append(entity.getIntrvl());//最高基准利率
					}else{
						entity.setIncdtp(tblKup_dppb_intr.getIncdtp());
						IntrPublic.getIntr(entity);
						strfx.append(tblKup_dppb.getProdtx()).append(sperate).append(entity.getIntrvl()).append(sperate).
						append(tblKup_dppb.getProdcd()).append(sperate).append(tblKup_dppb_post.getSvrule()).append(sperate).
						append(tblKup_dppb_post.getMiniam()).append(sperate).append(tblKup_dppb_post.getMaxiam());
						
					}
					infoBal.setAcctvl(strfx.toString());
					infoBal.setAccttp(prod.getAccttp());
					out.getProdls().add(infoBal);
				}*/
			// 基金
//			}else if(prod.getProdtp() == E_PRODTP.FUND){
//				//基金
//				fup_prod fundProd = Fup_prodDao.selectOne_odb1(prod.getProdcd(), true);
//				if(fundProd.getProdst() == E_PRODST.NORMAL){
//					strfx.append(fundProd.getSeinrt());
//					infoBal.setAcctvl(strfx.toString());
//					infoBal.setAccttp(prod.getAccttp());
//					out.getProdls().add(infoBal);
//				}
//			// 保险
//			}else if(prod.getProdtp() == E_PRODTP.INSU){
////				dfp_prod insuProd = Dfp_prodDao.selectOne_odb2(prod.getProdcd(), true);
//				if(insuProd.getProdst() == E_PRODST.NORMAL){
//					strfx.append(insuProd.getHtyevl()).append(sperate).append(insuProd.getMinxam()).append(sperate).append(insuProd.getIuterm());
//					infoBal.setAcctvl(strfx.toString());
//					infoBal.setAccttp(prod.getAccttp());
//					out.getProdls().add(infoBal);
//				}
			}else {
				throw CaError.Eacct.BNAS1201();
			}
		
		}
		
		return out;
	}

	/**
	 * 
	 * @Title: qryTransferDetail 
	 * @Description: 电子账号转账交易明细查询 
	 * @param input
	 * @return output
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:03:50 
	 * @version V2.3.0
	 */
	@Override
	public Output qryTransferDetail(Input input) {
		//电子账号
		String ecctno = input.getEcctno();
		//法人代码
		String corpno = input.getCorpno();
		//起始页数
		long pageno = input.getPageno();
		//每页条数
		long recdct = input.getRecdct();
		
		if(CommUtil.isNull(ecctno)){
			throw DpModuleError.DpstAcct.BNAS0311();
		}
		
		// 起始数
		int starno = (int) ((pageno - 1) * recdct); 
		//获取分页数据
		List<EacctTransDetailInfoOut> infos = (List<EacctTransDetailInfoOut>) CaDao.selEacctTransDetail(ecctno, corpno, starno, recdct, true);
	    //获取总记录数
		String totalCount = CaDao.selTotalCount(ecctno, corpno, false);
	    
	    long count = Long.parseLong(totalCount);
		
	    Output output = SysUtil.getInstance(Output.class);
		output.getInfos().addAll(infos);
		output.setCount(count);
		
		return output;
	}
	
	/**
	 * 
	 * @Title: qryTransferResult 
	 * @Description: 转账交易结果查询 
	 * @param input
	 * @param output
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:00:58 
	 * @version V2.3.0
	 */
	public void qryTransferResult(
			cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType.QryTransferResult.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType.QryTransferResult.Output output) {

	    /*
		//原交易流水
		String retraq = input.getRetraq();

		E_CAPITP capitp = input.getCapitp(); //资金交易类型

		if(CommUtil.isNull(retraq)){
			throw CaError.Eacct.E0001("原交易流水号不能为空");
		}
		
		IoCaInfoOut info = null;
		//查询交易状态和交易流水
		if(capitp == E_CAPITP.IN101 || capitp == E_CAPITP.IN102 || capitp == E_CAPITP.IN103
				|| capitp == E_CAPITP.IN105 ||capitp == E_CAPITP.IN106 || capitp == E_CAPITP.OT201
				|| capitp == E_CAPITP.OT202 || capitp == E_CAPITP.OT203 || capitp == E_CAPITP.OT204
				|| capitp == E_CAPITP.OT205){ //出入金登记簿
			//查询电子账号状态和交易流水
			info = CaDao.selTransResultByIobl(retraq, false);
		}else if(capitp == E_CAPITP.IN104){ //银联cups
			info = CaDao.knlIoblCupsVerify(retraq, false);
		}else if(capitp == E_CAPITP.IN107){//大小额
			info = CaDao.knlCnapotVerify(retraq, false);
		}else if(capitp == E_CAPITP.OU401 || capitp == E_CAPITP.OU402 || capitp == E_CAPITP.OU403){ //消费登记簿
			info = CaDao.knlSpndVerify(retraq, false);
		}else if(capitp == E_CAPITP.CG501){ //收费
			info = CaDao.kcbChrgRgstVerify(retraq, false);
		}else if(capitp == E_CAPITP.NT301){ //电子账户转电子账户
			info =CaDao.knlCaryVerify(retraq, false);
		}else if(capitp == E_CAPITP.IT601){	
			 Long selGlkns = CaDao.selGlKnsGlvcByTransq(retraq, false);
			 if(selGlkns <= 0){

				 info = SysUtil.getInstance(IoCaInfoOut.class);
				 info.setStatus(BaseEnumType.E_YES___.NO);
			 }else{
				 info = SysUtil.getInstance(IoCaInfoOut.class);
				 info.setTransq(retraq);
				 info.setStatus(BaseEnumType.E_YES___.YES);
			 }
		}else if(capitp == E_CAPITP.AL999){
			
			info =CaDao.selKappJioybwByMtrasq(retraq, false);
		}else{
			throw CaError.Eacct.E0001("资金交易类型输入有误");
		}
			
	    if(CommUtil.isNotNull(info)){
	        output.setTransq(retraq);
	        output.setIsable(info.getStatus());
	    }else{
	        output.setTransq(retraq);
	        output.setIsable(BaseEnumType.E_YES___.NO);
	    } 
		*/
//================== 20170822 by yuxiaobo 改写成通用的交易查证接口================
	    
		//上送系统交易流水
		String inpusq = input.getInpusq();
		//上送系统交易日期
		String inpudt = input.getInpudt();
		//核心交易流水
		String transq = input.getTransq();
		//核心交易日期
		String trandt = input.getTrandt();
		//输入判断
		if ((CommUtil.isNotNull(inpusq) && CommUtil.isNotNull(inpudt)) 
				|| (CommUtil.isNotNull(transq) && CommUtil.isNotNull(trandt))) {
			
		} else {
			throw CaError.Eacct.E0001("核心交易流水和交易日期或上送系统交易流水和上送系统交易日期必须出现一组进行查询");
		}
		
		IoCaInfoOut info = null;
		IoCaInfoOut info1 = null;
        //查询kns_redu防重表交易流水级联返回上送系统流水和交易状态
        info = CaDao.selKnsreduTranByInpusqdt(inpusq,inpudt,false);
        info1 = CaDao.selKnsTranreduBytransqdt(transq,trandt,false);

		if(CommUtil.isNotNull(info)){
		    output.setTransq(info.getTransq());
		    output.setTrandt(info.getTrandt());
		    output.setTxnsts(info.getTxnsts());
		}else if(CommUtil.isNotNull(info1) && CommUtil.isNull(info)){
			output.setTransq(info1.getTransq());
		    output.setTrandt(info1.getTrandt());
            output.setTxnsts(info1.getTxnsts());
		
		}else{
		    throw CaError.Eacct.BNAS1672(inpusq);
		}
	
		//poc增加审计日志
		ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
		apAudit.regLogOnInsertBusiPoc(null);
		
	}
	
	   
	/**
	 * 
	 * @Title: ioCaKnbTrinRegister 
	 * @Description: 涉案账户交易信息登记
	 * @param knbTrinInput
	 * @author liaojincai
	 * @date 2016年8月1日 下午2:56:56 
	 * @version V2.3.0
	 */
	public void ioCaKnbTrinRegister(IoCaKnbTrinInput knbTrinInput) {
		
		// 获取输入项信息
		String otcard = knbTrinInput.getOtcard();// 转出账号
		String otacna = knbTrinInput.getOtacna();// 转出账号名称
		String otbrch = knbTrinInput.getOtbrch();// 转出账号机构
		String otbank = knbTrinInput.getOtbank();// 转出银行行号
		String incard = knbTrinInput.getIncard();// 转入账号
		String inacna = knbTrinInput.getInacna();// 转入账号名称
		String inbrch = knbTrinInput.getInbrch();// 转入账号机构
		String inbank = knbTrinInput.getInbank();// 转入银行行号
		BigDecimal tranam = knbTrinInput.getTranam();// 交易金额
		String crcycd = knbTrinInput.getCrcycd();// 币种
		E_YES___ issucc = knbTrinInput.getIssucc();// 是否成功
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期
		String mntrsq =  CommTools.getBaseRunEnvs().getMain_trxn_seq();// 主交易流水	
		String trantm = BusiTools.getBusiRunEnvs().getTrantm();// 交易时间
		//String bstrsq = CommTools.getBaseRunEnvs().getBstrsq();//跟踪号
		bizlog.debug("<<<<<<<<<<<交易时间=[" + trantm + "]>>>>>>>>>>>>>>>>");
		
		// 输入项非空检查
//		if (CommUtil.isNull(Trantp)) {
//			throw CaError.Eacct.E0001("交易类型输入不能为空");
//		}
//		if (CommUtil.isNull(tranam)) {
//			throw CaError.Eacct.E0001("交易金额输入不能为空");
//		}
//		if (CommUtil.isNull(crcycd)) {
//			throw CaError.Eacct.E0001("币种输入不能为空");
//		}
//		if (CommUtil.isNull(issucc)) {
//			throw CaError.Eacct.E0001("是否成功输入不能为空");
//		}
		if (CommUtil.compare(tranam, BigDecimal.ZERO) < 0) {
			throw CaError.Eacct.BNAS0628();
		}
		
		// 获取涉案账户交易信息登记薄实例
		KnbTrin tblKnbTrin = SysUtil.getInstance(KnbTrin.class);
		
		tblKnbTrin.setTrandt(trandt);// 交易日期
		tblKnbTrin.setTransq(mntrsq);// 交易流水
		tblKnbTrin.setTrntim(trantm);// 交易时间
		tblKnbTrin.setOtcard(otcard);// 转出账号
		tblKnbTrin.setOtacna(otacna);// 转出账号名称
		tblKnbTrin.setOtbrch(otbrch);// 转出账号机构
		tblKnbTrin.setOtbank(otbank);// 转出银行行号
		tblKnbTrin.setIncard(incard);// 转入账号
		tblKnbTrin.setInacna(inacna);// 转入账号名称
		tblKnbTrin.setInbrch(inbrch);// 转入账号机构
		tblKnbTrin.setInbank(inbank);// 转入银行行号
		tblKnbTrin.setTranam(tranam);// 交易金额
		tblKnbTrin.setCrcycd(crcycd);// 币种
		tblKnbTrin.setIssucc(issucc);// 交易是否成功
		
		// 新增记录
		KnbTrinDao.insert(tblKnbTrin);
	}
	@Override
	public EacctCustnaCardno qryCustnaAndCardno(String custac) {
		EacctCustnaCardno out=SysUtil.getInstance(EacctCustnaCardno.class);
		
		KnaAcdc tblKnaAcdc =CaDao.selKnaAcdcByCustac(custac, false);
		if(CommUtil.isNotNull(tblKnaAcdc)){
			out.setCardno(tblKnaAcdc.getCardno());
		}
		
		KnaCust tblKnaCust =CaDao.selKnaCustByCustac(custac, false);
		if(CommUtil.isNotNull(tblKnaCust)){
			out.setCustna(tblKnaCust.getCustna());
		}
		
		return out;
	}
	
	/**
	 * 
	 * @Title: qryGlcounts 
	 * @Description: 查询已开立一类户数量及信息
	 * @param idtftp
	 * @param idtfno
	 * @param output
	 * @author songkailei
	 * @date 2017年1月5日 上午9:54:08 
	 * @version V2.3.0
	 */
	@Override
	public void qryGlcounts(
		E_IDTFTP idtftp,
		String idtfno,
		String brchno,
		cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType.QryGlcounts.Output output) {
		
		//非空校验
		if(CommUtil.isNull(idtftp)){
			throw CaError.Eacct.BNAS1673();
		}
			
		if(CommUtil.isNull(idtfno)){
			throw CaError.Eacct.BNAS1674();
		}
		
		if(CommUtil.isNull(brchno)){
			throw CaError.Eacct.BNAS1675();
		}
		
		//根据证件类型和证件号码查询客户号
//		IoSrvCfPerson.IoGetCifCust.InputSetter getCifCustInput = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//		IoSrvCfPerson.IoGetCifCust.Output tblcifcust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//		IoSrvCfPerson getCifCustServer = SysUtil.getInstance(IoSrvCfPerson.class);
//		getCifCustInput.setIdtfno(idtfno);
//		getCifCustInput.setIdtftp(idtftp);
//		getCifCustServer.getCifCust(getCifCustInput, tblcifcust);
//		
//		String custno = tblcifcust.getCustno();
		
		//根据客户号查询已开立的全功能账户
		List<KnaCust> tblknacust = CaDao.selKnaCustAllbyCustno("", brchno, false);
		
		/*IoCaOpenGlinfos cainfo = SysUtil.getInstance(IoCaOpenGlinfos.class);
		Options<IoCaOpenGlinfos> cainfos = new DefaultOptions<>();*/
		int counts = 0;
		int goblct = 0;
		int finact = 0;
		int waltct = 0;
		
		if(CommUtil.isNotNull(tblknacust)){
			for(KnaCust cust : tblknacust){
				//获取账户类型
				E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
						.qryAccatpByCustac(cust.getCustac());
				
				//若为全功能账户则输出
				if(eAccatp == E_ACCATP.GLOBAL){
					goblct = goblct + 1;
					
/*					cainfo.setCustna(cust.getCustna());//客户名称
					cainfo.setBrchno(cust.getBrchno());//账户所属机构
					
					KnaAcdc tblacdc = KnaAcdcDao.selectFirst_odb4(cust.getCustac(), false);
					if(CommUtil.isNull(tblacdc)){
						bizlog.debug("**************" + cust.getCustac() + "****账号查询KnaAcdc失败");
						throw CaError.Eacct.E0001("账号异常！");
					}
					
					cainfo.setCardno(tblacdc.getCardno());//电子账号
					cainfos.add(cainfo);
*/				}
				
				//若为理财账户则 加1
				if(eAccatp == E_ACCATP.FINANCE){
					finact = finact + 1;
				}
				
				//若为钱包户，waltct加1
				if(eAccatp == E_ACCATP.WALLET){
					waltct = waltct + 1;
				}
				
				counts = goblct + finact + waltct;//总账户数量
			}
		}
		
		//若未找到开立的电子账户，返回特定错误码
		if(counts == 0){
			throw CaError.Eacct.E0022();
		}
		
		output.setCounts(counts);
		output.setGlobct(goblct);
		output.setFinact(finact);
		output.setWaltct(waltct);
		
	}
	
	public void ioCaTsfInCheck(
			cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType.IoCaTsfInCheck.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType.IoCaTsfInCheck.Output output) {
		
		E_CAPITP capitp = E_CAPITP.NT301; //交易类型
		
		
		IoCaKnaAcdc inacdc = ActoacDao.selKnaAcdc(input.getIncard(), false);
		if(CommUtil.isNull(inacdc)){
			throw DpModuleError.DpstComm.BNAS1904();
		}
		if(inacdc.getStatus() == E_DPACST.CLOSE){
			throw DpModuleError.DpstComm.BNAS1905();
		}
		
		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		E_ACCATP inaccatp = cagen.qryAccatpByCustac(inacdc.getCustac()); //转入方电子账户类型
		

		KnaAcct inacct = SysUtil.getInstance(KnaAcct.class); // 转入方子账号
		
		
		//转入方电子账户校验
		if (inaccatp == E_ACCATP.WALLET) {
			inacct = CapitalTransDeal.getSettKnaAcctSub(inacdc.getCustac(), E_ACSETP.MA);
			output.setSbactp2(E_SBACTP._12);
		}else{
			inacct = CapitalTransDeal.getSettKnaAcctSub(inacdc.getCustac(), E_ACSETP.SA);
			output.setSbactp2(E_SBACTP._11);
		}
		
		if(CommUtil.isNotNull(input.getInacna()) && !CommUtil.equals(input.getInacna(), inacct.getAcctna())){
			throw DpModuleError.DpstComm.BNAS0892();
		}				

		output.setAccttp2(inaccatp);
		output.setCustac2(inacdc.getCustac());
		
		//电子账户状态，状态字检查
		AcTranfeChkIN chkIN = SysUtil.getInstance(AcTranfeChkIN.class);
		chkIN.setAccatp(input.getAccttp());
		chkIN.setCardno(input.getCardno()); //电子账号卡号
		chkIN.setCustac(input.getOtcsac()); //电子账号ID
		chkIN.setCustna(input.getOtacna());
		chkIN.setCapitp(capitp);
		chkIN.setOpactp(inaccatp);
		chkIN.setOpcard(inacdc.getCardno());
		chkIN.setOppoac(inacdc.getCustac());
		chkIN.setOppona(inacct.getAcctna());
		chkIN.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		
		AcTranfeChkOT chkOT = CapitalTransCheck.chkTranfe(chkIN);
				
		output.setCustie(chkOT.getIsbind());
		output.setFacesg(chkOT.getFacesg());
		
		output.setCustie2(chkOT.getIsbind());
		output.setFacesg2(chkOT.getOpface());
		
		E_CUACST opacst = chkOT.getOpacst();		
		
		if(CommUtil.isNotNull(input.getInacno()) && !CommUtil.equals(inacct.getAcctno(), input.getInacno())){
			throw DpModuleError.DpstComm.BNAS1907();
		}		
		
		output.setCuacst(opacst); //转入账户的客户化状态
		output.setInbrch2(inacct.getBrchno());
		output.setIncorp(inacct.getCorpno());
		output.setIncsac(inacct.getCustac()); // 电子账号ID
		output.setInchld(inacct.getAcctno()); // 子账号
		//output.setTblKnaAcct(tblKnaAcct);
		// CommUtil.copyProperties(, inacct); //传入转入账户
		output.getTblKnaAcct().setAcctcd(inacct.getAcctcd());
		output.getTblKnaAcct().setAcctna(inacct.getAcctna());
		output.getTblKnaAcct().setAcctno(inacct.getAcctno());
		output.getTblKnaAcct().setAcctst(inacct.getAcctst());
		output.getTblKnaAcct().setAccttp(inacct.getAccttp());
		output.getTblKnaAcct().setBgindt(inacct.getBgindt());
		output.getTblKnaAcct().setBkmony(inacct.getBkmony());
		output.getTblKnaAcct().setBrchno(inacct.getBrchno());
		output.getTblKnaAcct().setClosdt(inacct.getClosdt());
		output.getTblKnaAcct().setClossq(inacct.getClossq());
		output.getTblKnaAcct().setCorpno(inacct.getCorpno());
		output.getTblKnaAcct().setCrcycd(inacct.getCrcycd());
		output.getTblKnaAcct().setCsextg(inacct.getCsextg());
		output.getTblKnaAcct().setCustac(inacct.getCustac());
		output.getTblKnaAcct().setCustno(inacct.getCustno());
		output.getTblKnaAcct().setDebttp(inacct.getDebttp());
		output.getTblKnaAcct().setDepttm(inacct.getDepttm());
		output.getTblKnaAcct().setHdmimy(inacct.getHdmimy());
		output.getTblKnaAcct().setHdmxmy(inacct.getHdmxmy());
		output.getTblKnaAcct().setIsdrft(inacct.getIsdrft());
		output.getTblKnaAcct().setMatudt(inacct.getMatudt());
		output.getTblKnaAcct().setOnlnbl(inacct.getOnlnbl());
		output.getTblKnaAcct().setOpendt(inacct.getOpendt());
		output.getTblKnaAcct().setOpensq(inacct.getOpensq());
		output.getTblKnaAcct().setOpmony(inacct.getOpmony());
		output.getTblKnaAcct().setPddpfg(inacct.getPddpfg());
		output.getTblKnaAcct().setProdcd(inacct.getProdcd());
		output.getTblKnaAcct().setSleptg(inacct.getSleptg());
		output.getTblKnaAcct().setSpectp(inacct.getSpectp());
		output.getTblKnaAcct().setTmstmp(inacct.getTmstmp());
		output.getTblKnaAcct().setTrsvtp(inacct.getTrsvtp());
		output.getTblKnaAcct().setUpbldt(inacct.getUpbldt());
	}
	public void ioCaKnbTrinRegisterPeer( final cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput knbTrinInput){
		
		
		// 获取输入项信息
		String otcard = knbTrinInput.getOtcard();// 转出账号
		String otacna = knbTrinInput.getOtacna();// 转出账号名称
		String otbrch = knbTrinInput.getOtbrch();// 转出账号机构
		String otbank = knbTrinInput.getOtbank();// 转出银行行号
		String incard = knbTrinInput.getIncard();// 转入账号
		String inacna = knbTrinInput.getInacna();// 转入账号名称
		String inbrch = knbTrinInput.getInbrch();// 转入账号机构
		String inbank = knbTrinInput.getInbank();// 转入银行行号
		BigDecimal tranam = knbTrinInput.getTranam();// 交易金额
		String crcycd = knbTrinInput.getCrcycd();// 币种
		E_YES___ issucc = knbTrinInput.getIssucc();// 是否成功
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期
		String mntrsq =  CommTools.getBaseRunEnvs().getMain_trxn_seq();// 主交易流水	
		String trantm = BusiTools.getBusiRunEnvs().getTrantm();// 交易时间
		//String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();//法人
		//String bstrsq = CommTools.getBaseRunEnvs().getBstrsq();//跟踪号
		bizlog.debug("<<<<<<<<<<<交易时间=[" + trantm + "]>>>>>>>>>>>>>>>>");
		
		//若已登记，直接退出
		KnbTrin trin2 = KnbTrinDao.selectOne_ob1(trandt, mntrsq, false);
		if(CommUtil.isNotNull(trin2)){
			return;
		}
		
		// 输入项非空检查
//		if (CommUtil.isNull(Trantp)) {
//			throw CaError.Eacct.E0001("交易类型输入不能为空");
//		}
//		if (CommUtil.isNull(tranam)) {
//			throw CaError.Eacct.E0001("交易金额输入不能为空");
//		}
//		if (CommUtil.isNull(crcycd)) {
//			throw CaError.Eacct.E0001("币种输入不能为空");
//		}
//		if (CommUtil.isNull(issucc)) {
//			throw CaError.Eacct.E0001("是否成功输入不能为空");
//		}
		if (CommUtil.compare(tranam, BigDecimal.ZERO) < 0) {
			throw CaError.Eacct.BNAS0628();
		}
		
		// 获取涉案账户交易信息登记薄实例
		KnbTrin tblKnbTrin = SysUtil.getInstance(KnbTrin.class);
		
		tblKnbTrin.setTrandt(trandt);// 交易日期
		tblKnbTrin.setTransq(mntrsq);// 交易流水
		tblKnbTrin.setTrntim(trantm);// 交易时间
		tblKnbTrin.setOtcard(otcard);// 转出账号
		tblKnbTrin.setOtacna(otacna);// 转出账号名称
		tblKnbTrin.setOtbrch(otbrch);// 转出账号机构
		tblKnbTrin.setOtbank(otbank);// 转出银行行号
		tblKnbTrin.setIncard(incard);// 转入账号
		tblKnbTrin.setInacna(inacna);// 转入账号名称
		tblKnbTrin.setInbrch(inbrch);// 转入账号机构
		tblKnbTrin.setInbank(inbank);// 转入银行行号
		tblKnbTrin.setTranam(tranam);// 交易金额
		tblKnbTrin.setCrcycd(crcycd);// 币种
		tblKnbTrin.setIssucc(issucc);// 交易是否成功
		
		// 新增记录
		KnbTrinDao.insert(tblKnbTrin);
	}
	

}

