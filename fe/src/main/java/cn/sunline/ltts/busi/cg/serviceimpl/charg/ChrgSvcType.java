package cn.sunline.ltts.busi.cg.serviceimpl.charg;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.cg.charg.ChargeProc;
import cn.sunline.ltts.busi.cg.namedsql.charg.PBChargeRegisterDao;
import cn.sunline.ltts.busi.cg.serviceimpl.IoCgChrgSvcImpl;
import cn.sunline.ltts.busi.cg.servicetype.ChrgSvcType.chargeAdjust.Input;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbAdjtRgst;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbAdjtRgstDao;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgDetl;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgRgst;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgRgstDao;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgChargeFee_IN;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgDanbJzFee_IN;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgDanbJzFee_OUT;
import cn.sunline.ltts.busi.cg.type.CgComplexType.ChargeInfo;
import cn.sunline.ltts.busi.cg.type.CgComplexType.PublicCharge_IN;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaPaydDetail;
import cn.sunline.ltts.busi.iobus.servicetype.IoApAccount;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.QryDpAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalFee_IN;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_ACCTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSTRFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_KPACFG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ADJTTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BRMPTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CGPYRV;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CGTRTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHARTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_OPRFLG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_RUISMA;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_STATUS;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
 /**
  * 收费内部服务实现
  * 收费内部服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="ChrgSvcType", longname="收费内部服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class ChrgSvcType implements cn.sunline.ltts.busi.cg.servicetype.ChrgSvcType{

	private static final BizLog bizlog = BizLogUtil
			.getBizLog(ChrgSvcType.class);
/**
 * 汇总销记登记簿查询
 */

@Override
public void selChargRegistGroupByTrifno(
		String bgdate,
		String endate,
		String acctno,
		String cardno,
		cn.sunline.ltts.busi.cg.servicetype.ChrgSvcType.SelChargRegistGroupByTrifno.Output output) {

	if(CommUtil.isNull(acctno)){
		throw FeError.Chrg.BNASF329();
	}	
	if(CommUtil.isNull(cardno)){
		throw FeError.Chrg.BNASF500();
	}	
	E_ACCTROUTTYPE accttp =ApAcctRoutTools.getRouteType(acctno);
	if(accttp!=E_ACCTROUTTYPE.DEPOSIT){
		throw FeError.Chrg.BNASF334();
	}
	if(CommUtil.isNull(bgdate)){
		throw FeError.Chrg.BNASF276();
	}
	if(CommUtil.compare(bgdate,CommTools.getBaseRunEnvs().getTrxn_date())>0){
		
		throw FeError.Chrg.BNASF191();
	}
	if(CommUtil.isNull(endate)){
		throw FeError.Chrg.BNASF275();
	}
	if(CommUtil.compare(endate,CommTools.getBaseRunEnvs().getTrxn_date())>0){
		
		throw FeError.Chrg.BNASF167();
	}
	if(CommUtil.compare(endate, bgdate) < 0){
		throw FeError.Chrg.BNASF168();
	}
	IoCaKnaAccs tblKnaAccs =SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAccsOdb3(acctno, false);
    if(null==tblKnaAccs){
    	throw FeError.Chrg.BNASF328();
    }
	String custac=tblKnaAccs.getCustac();		
	if(!CommUtil.equals(custac, SysUtil.getInstance(IoCaSevQryTableInfo.class).
			getKnaAcdcByCardno(cardno, true).getCustac())){
		throw FeError.Chrg.BNASF501();
	}
	List<ChargeInfo> chargeList = PBChargeRegisterDao.selChargeRegistGroupByTrifno(bgdate, endate, custac, false);
	if(null!=chargeList&&chargeList.size()>0){
		int i=0;
		for (ChargeInfo info : chargeList ){
			i++;
			info.setChrgsq(i);
			info.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
			output.getChargeInfo().add(info);
		}
		CommTools.getBaseRunEnvs().setTotal_count((long) i);
	}	
}
@Override
public void chargeAdjust(Input input) {

	
	String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();// 交易机构
	String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期
	String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();// 交易流水
	String telleno = CommTools.getBaseRunEnvs().getTrxn_teller();// 交易柜员
	String auth = BusiTools.getBusiRunEnvs().getAuthvo().getAuthus();// 授权柜员
	Long evrgsq = input.getEvrgsq();//收费序号
	String product = "";// 调整后产品编码
	product = input.getJtprod();
	E_ACCTTP acctType = E_ACCTTP.DP;//调整前账号类型
	E_ACCTTP acctType1 = E_ACCTTP.DP;//调整后账号类型
	IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
	IoCaKnaAcdc otacdc = SysUtil.getInstance(IoCaKnaAcdc.class); // 卡客户账号对照表
	IoCaKnaCust otcust = SysUtil.getInstance(IoCaKnaCust.class); // 调整前电子账户信息
	IoCaKnaAcdc otacdc1 = SysUtil.getInstance(IoCaKnaAcdc.class); // 卡客户账号对照表
	IoCaKnaCust otcust1 = SysUtil.getInstance(IoCaKnaCust.class); // 调整后电子账户信息
	IoInacInfo ioInacInfo1 =SysUtil.getInstance(IoInacInfo.class); // 调整前内部户信息
	CgDanbJzFee_OUT out = SysUtil.getInstance(CgDanbJzFee_OUT.class); // 手续费单边记账
	IoDpKnaAcct acct = SysUtil.getInstance(IoDpKnaAcct.class); // 调整后电子账户信息
	String custac ="";
	String acctac = "";
	String acctna="";
	
	if(CommUtil.compare(trandt, input.getPrtrdt())<=0){
		
		throw FeError.Chrg.BNASF320();
	}
	if(CommUtil.isNull(input.getToacct())){
		throw FeError.Chrg.BNASF503();
	}
	if(input.getAdjttp()!= E_ADJTTP.ALL){
		if(CommUtil.isNull(input.getAdjtam())||CommUtil.compare(input.getAdjtam(), BigDecimal.ZERO)==0){
			
			throw FeError.Chrg.BNASF244();
		}
		if(CommUtil.isNull(input.getAcctno())){
			
			throw FeError.Chrg.BNASF060();
		}
	}
	if(CommUtil.isNull(input.getRemark())){
		throw FeError.Chrg.BNASF400();	
	}
	
	if(CommUtil.compare(input.getPrtrdt().substring(0, 4), trandt.substring(0, 4))!=0){
		throw FeError.Chrg.BNASF235();
	}

	//检查原交易是否已调整过
	   SysUtil.getInstance(KcbAdjtRgstDao.class);
	  	KcbAdjtRgst oRgst = PBChargeRegisterDao.selKcbAdjtRgstByPrtrsq(input.getPrtrdt(), input.getPrtrsq(),evrgsq, false);
	if(CommUtil.isNotNull(oRgst)){
		throw FeError.Chrg.BNASF100();
	}
	

	//获取原收费交易信息
	KcbChrgRgst tbrgst = KcbChrgRgstDao.selectOne_odb6(input.getPrtrdt(), input.getPrtrsq(),evrgsq, false);
	if(CommUtil.isNull(tbrgst)){
        //费用记帐明细处理
        List<KcbChrgDetl> lstChrgDetl = PBChargeRegisterDao.selKcbChrgDetlByTrdtTrsq(input.getPrtrdt(),  input.getPrtrsq(),evrgsq, false);
        boolean isstrk =false;
        if (CommUtil.isNotNull(lstChrgDetl) && lstChrgDetl.size() > 0){
        	   for (KcbChrgDetl tblChrgDetl : lstChrgDetl){
        		   if(tblChrgDetl.getRuisma()==E_RUISMA.DR){
        			   isstrk =true;
        		   }
        	   }
        }
        if(isstrk){
        	throw FeError.Chrg.BNASF323();
        }else{
        	
        	throw FeError.Chrg.BNASF268();
        }
            
		
	}
	if(input.getAdjttp()== E_ADJTTP.ALL&&CommUtil.compare(tbrgst.getAcclam(), input.getAdjtam())!=0){
		
		throw FeError.Chrg.BNASF008();
	}		
	if(input.getAdjttp()== E_ADJTTP.ALL&&CommUtil.compare(input.getPrtram(), input.getAdjtam())!=0){
		
		throw FeError.Chrg.BNASF009();
	}		
	String prodcd =tbrgst.getProdcd();//产品
	String trinfo =tbrgst.getTrinfo();//交易信息
	
	//机构检查
	String obrchno = tbrgst.getAcctbr();//原交易机构
	if(!obrchno.equals(brchno)){//非原机构，则判断是否为清算中心或清算机构下辖网点
		String centbr = BusiTools.getBusiRunEnvs().getCentbr();
		if(CommUtil.compare(centbr, brchno)!=0){//不为清算中心，则判断上级机构是否为清算机构
		String	upbrchno = SysUtil.getInstance(IoSrvPbBranch.class).getUpprBranch(brchno, E_BRMPTP.M, BusiTools.getDefineCurrency()).getBrchno();
		if(CommUtil.compare(centbr, upbrchno)!=0){
			throw FeError.Chrg.BNASF043();
		}
		}
	}
	
	// 获取调整前账号类型			
	IoApAccount.queryAccountType.Output output1 = SysUtil.getInstance(IoApAccount.queryAccountType.Output.class);
	SysUtil.getInstance(IoApAccount.class).queryAccountType(
			input.getToacct(), output1);
	if (CommUtil.isNotNull(output1)) {
		acctType = output1.getAccttp();
	} else {
		throw FeError.Chrg.BNASF384();				
	}	
	//账号检查
	if (CommUtil.compare(input.getAdjttp(), E_ADJTTP.ALL) != 0) {// 1、多收退回   2、多付收回
		if(CommUtil.isNull(input.getAcctno())){
			throw FeError.Chrg.BNASF248();
		}
		
		//调整金额需小于等于原金额
		if(CommUtil.compare(input.getAdjtam(), input.getPrtram()) > 0){
			throw FeError.Chrg.BNASF245();
		}
		if(CommUtil.isNull(input.getJtchcd())){
			throw FeError.Chrg.BNASF233();
		}
		if(!CommUtil.equals(input.getChrgcd(), input.getJtchcd())){
			throw FeError.Chrg.BNASF399();
		}

		
		if(CommUtil.compare(acctType, E_ACCTTP.DP) == 0){//调整前为电子账户  toacct  为原账号
			//调整前账号信息
			otacdc1 = caqry.getKnaAcdcByCardno(input.getToacct(), true);

			otcust1 = caqry.getKnaCustByCustacOdb1(otacdc1.getCustac(), true);

		//	otcust1 = caqry.getKnaCustByCardnoOdb1(otacdc1.getCustac(), true);
			
			custac= otacdc1.getCustac();//电子账号
			acctna = otcust1.getCustna();
			if(!CommUtil.equals(tbrgst.getCustac(), custac)){
				throw FeError.Chrg.BNASF502();
			}
			
		}else if(CommUtil.compare(acctType, E_ACCTTP.IN) == 0){//调整前为内部户
			ioInacInfo1 = SysUtil.getInstance(IoInSrvQryTableInfo.class).selKnaGlAcctnoByAcctno(input.getToacct(), true);
			acctna= ioInacInfo1.getAcctna();

			
		}
		
		
		
		if(CommUtil.compare(input.getAcctno(), input.getToacct())!=0){//调整后的账号与原账号不同   Acctno 为调整后入账账号
			
			
			// 获取调整后账号类型			
			IoApAccount.queryAccountType.Output output = SysUtil.getInstance(IoApAccount.queryAccountType.Output.class);
			SysUtil.getInstance(IoApAccount.class).queryAccountType(
					input.getAcctno(), output);
			if (CommUtil.isNotNull(output)) {
				acctType1 = output.getAccttp();
			} else {
				throw FeError.Chrg.BNASF127();				
			}
			
			if (CommUtil.compare(acctType1, acctType) != 0) {
				throw FeError.Chrg.BNASF247();	
			}
			
			if (CommUtil.compare(acctType1, E_ACCTTP.DP) == 0) {//电子账号判断是否与原账号同一客户号，与同一机构法人
				
				//调整后账号信息
				otacdc = caqry.getKnaAcdcByCardno(input.getAcctno(), true);
				otcust = caqry.getKnaCustByCustacOdb1(otacdc.getCustac(), true);
				acct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctSub(otacdc.getCustac(), E_ACSETP.SA);
				if(CommUtil.compare(otcust.getCustno(), otcust1.getCustno()) != 0){
					throw FeError.Chrg.BNASF174();
				}
				if(CommUtil.compare(otacdc.getCorpno(), otacdc1.getCorpno()) != 0){
					throw FeError.Chrg.BNASF246();
				}

				
				ChkAcctstOT(otcust.getCuacst(), otcust.getAcctst()); //电子账号状态检查
				
			
				QryDpAcctOut info =  SysUtil.getInstance(DpProdSvcType.class).QryDpByAcctno(acct.getAcctno());
				if(CommUtil.compare(info.getCrcycd(), input.getCrcycd())!=0){
		    	    throw FeError.Chrg.BNASF331();
		        }
				if(CommUtil.isNull(input.getJtprod())){
					product = info.getAcctcd();//调整后产品代码为空 ，默认取账号产品
				}
				custac= otacdc.getCustac();//电子账号
				acctac= acct.getAcctno();//负债账号
				acctna= acct.getAcctna();
				
			}else if (CommUtil.compare(acctType1, E_ACCTTP.IN) == 0) {//内部户检查
				 IoInacInfo ioInacInfo = SysUtil.getInstance(IoInSrvQryTableInfo.class).selKnaGlAcctnoByAcctno(input.getAcctno(), true);
				 acctna= ioInacInfo.getAcctna();
			        if(E_INACST.CLOSED == ioInacInfo.getAcctst()){
			        	throw FeError.Chrg.BNASF045();
			        }
			        
			        if(E_KPACFG._1 == ioInacInfo.getKpacfg()){
			        	throw FeError.Chrg.BNASF044();
			        }
			        if(!CommUtil.equals(ioInacInfo1.getBrchno(), ioInacInfo.getBrchno())){
			        	throw FeError.Chrg.BNASF184();
			        }
			
			        //内部户收付费  差错调整 调整后产品必输
			        if(input.getAdjttp()==E_ADJTTP.ALL){
			        	if(CommUtil.isNull(product)){
			        		
			        		throw FeError.Chrg.BNASF189();
			        	}
			        }
			}
			
		}else{ //调整前后账号相同
			if (CommUtil.compare(acctType, E_ACCTTP.DP) == 0) {
				ChkAcctstOT(otcust.getCuacst(), otcust1.getAcctst()); //电子账号状态检查
				acct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(otacdc1.getCustac());
				
				QryDpAcctOut info =  SysUtil.getInstance(DpProdSvcType.class).QryDpByAcctno(acct.getAcctno());
				if(CommUtil.compare(info.getCrcycd(), input.getCrcycd())!=0){
		    	    throw FeError.Chrg.BNASF331();
		        }
				acctac= acct.getAcctno();//负债账号
				acctna= acct.getAcctna();
				if(CommUtil.isNull(input.getJtprod())){
					product = info.getAcctcd();//调整后产品代码为空 ，默认取账号产品
				}
				
			}else if (CommUtil.compare(acctType, E_ACCTTP.IN) == 0) {//内部户检查
				
				 if(E_INACST.CLOSED == ioInacInfo1.getAcctst()){
			        	throw FeError.Chrg.BNASF045();
			        }				        
			     if(E_KPACFG._1 == ioInacInfo1.getKpacfg()){
			        	throw FeError.Chrg.BNASF044();
			        }
			     if(CommUtil.compare(ioInacInfo1.getCrcycd(), input.getCrcycd())!=0){
			    	    throw FeError.Chrg.BNASF331();
			     }
				//内部户收付费  差错调整 调整后产品必输
			/*	if(input.getAdjttp()==E_ADJTTP.ALL){
					if(CommUtil.isNull(product)){
						
						throw FeError.Chrg.BNASF189();
					}
				}			     */
				acctna= ioInacInfo1.getAcctna();
			}
		}

	}else{//差错调整
		
		if(CommUtil.compare(acctType, E_ACCTTP.IN) == 0&&CommUtil.isNull(product)){
			
			throw FeError.Chrg.BNASF189();
			
		}else if(CommUtil.compare(acctType, E_ACCTTP.DP) == 0&&CommUtil.isNull(product)){
			
			QryDpAcctOut info =  SysUtil.getInstance(DpProdSvcType.class).QryDpByAcctno(tbrgst.getSysacn());
				
			product = info.getAcctcd();//调整后产品代码为空 ，默认取账号产品
							
		}

		if(CommUtil.isNull(input.getJttrif())){
			
			throw FeError.Chrg.BNASF385();
		}
		if(CommUtil.isNull(input.getJtchcd())){
			
			throw FeError.Chrg.BNASF233();
		}
		if(CommUtil.equals(input.getJtchcd(), tbrgst.getChrgcd())
				&&CommUtil.equals(tbrgst.getTrinfo(),input.getJttrif())){
			throw FeError.Chrg.BNASF234();
		}

		if(tbrgst.getChrgcd().substring(2, 3).equals("0")&&input.getJtchcd().substring(2, 3).equals("1")){//收费费种代码
				
			throw FeError.Chrg.BNASF391(tbrgst.getChrgcd(),input.getJtchcd());	
			
		}else if (tbrgst.getChrgcd().substring(2, 3).equals("1")&&input.getJtchcd().substring(2, 3).equals("0")){
 				
			throw FeError.Chrg.BNASF392(tbrgst.getChrgcd(),input.getJtchcd());	
 			
			}

	}
	
	// 手续费收取支出
	CgDanbJzFee_IN cplDbFee_In = SysUtil.getInstance(CgDanbJzFee_IN.class);
	cplDbFee_In.setChrgcd(input.getChrgcd());// 收费代码
//	cplDbFee_In.setProdcd(input.getProdcd());// 产品编号
	cplDbFee_In.setTrancy(input.getCrcycd());// 币种
	cplDbFee_In.setAcctbr(obrchno);// 机构
	cplDbFee_In.setCstrfg(E_CSTRFG.TRNSFER);// 现转标志
	cplDbFee_In.setProdcd(prodcd);
	cplDbFee_In.setTrinfo(trinfo);

	if (CommUtil.compare(input.getAdjttp(), E_ADJTTP.ADD) == 0) {// 多收退回


		// 贷：手续费收入红字
		cplDbFee_In.setCgpyrv(E_CGPYRV.RECIVE);
		cplDbFee_In.setTranam(input.getAdjtam().negate());// 金额 红字

		if (CommUtil.compare(acctType, E_ACCTTP.DP) == 0) {// 电子账户		
						

			//账户状态检查，支取红字，做贷方检查，不检查借冻
			
			SysUtil.getInstance(IoAccountSvcType.class).checkStatusBeforeAccount(custac, E_AMNTCD.CR,BaseEnumType.E_YES___.NO, BaseEnumType.E_YES___.NO);
			// 调用电子账户借方支取服务
			DrawDpAcctIn cplDrawIn = SysUtil.getInstance(DrawDpAcctIn.class);

			cplDrawIn.setAcctno(acctac);
			cplDrawIn.setAcseno(null);//
			cplDrawIn.setCardno(input.getAcctno());
			cplDrawIn.setCrcycd(input.getCrcycd());
			cplDrawIn.setSmrycd(BusinessConstants.SUMMARY_QT);
			cplDrawIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_QT));
			cplDrawIn.setRemark("多收退回,"+input.getRemark());
			cplDrawIn.setCustac(custac);
			cplDrawIn.setLinkno(null);
			cplDrawIn.setAuacfg(cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.NO);
			cplDrawIn.setTranam(input.getAdjtam().negate());// 记账金额 红字
			


//			CommTools.getRemoteInstance(DpAcctSvcType.class).addDrawAcctDp(
//					cplDrawIn);
			SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawIn);

		} else if (CommUtil.compare(acctType, E_ACCTTP.IN) == 0) {// 内部户
			// 调用内部户借方记账服务
			IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);

			iaAcdrInfo.setTrantp(E_TRANTP.TR);
			iaAcdrInfo.setAcctno(input.getAcctno());
			iaAcdrInfo.setTranam(input.getAdjtam().negate());// 记账金额 红字
			iaAcdrInfo.setCrcycd(input.getCrcycd());// 币种
			iaAcdrInfo.setDscrtx(input.getRemark());
			if (CommUtil.isNotNull(input.getPayalst())) {
				iaAcdrInfo.setPayadetail(input.getPayalst());
			}
			if (CommUtil.isNotNull(input.getPaydlst())) {
				iaAcdrInfo.setPayddetail(input.getPaydlst());
			}

		/*	CommTools.getRemoteInstance(IoInAccount.class).ioInAcdr(
					iaAcdrInfo);*/
			SysUtil.getInstance(IoInAccount.class).ioInAcdr(
					iaAcdrInfo);

		}

	} else if (CommUtil.compare(input.getAdjttp(), E_ADJTTP.DEL) == 0) {// 多付收回
		// 借：手续费支出红字
		cplDbFee_In.setCgpyrv(E_CGPYRV.PAY);
		cplDbFee_In.setTranam(input.getAdjtam().negate());// 金额 红字

		if (CommUtil.compare(acctType, E_ACCTTP.DP) == 0) {// 电子账户			

			//存入红字，做借方检查
			SysUtil.getInstance(IoAccountSvcType.class).checkStatusBeforeAccount(custac, E_AMNTCD.DR,BaseEnumType.E_YES___.YES,BaseEnumType.E_YES___.NO);
			
			// 调用电子账户贷方存入服务
			SaveDpAcctIn cplSaveIn = SysUtil.getInstance(SaveDpAcctIn.class);

			cplSaveIn.setAcctno(acctac);
			cplSaveIn.setCardno(input.getAcctno());
			cplSaveIn.setCrcycd(input.getCrcycd());
			cplSaveIn.setCustac(custac);
			cplSaveIn.setLinkno(null);
			cplSaveIn.setRemark("多付收回"+input.getRemark());
			cplSaveIn.setSmrycd(BusinessConstants.SUMMARY_QT);
			cplSaveIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_QT));
			cplSaveIn.setTranam(input.getAdjtam().negate());// 记账金额 红字
			cplSaveIn.setNegafg(cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES);//允许红字
	//		CommTools.getRemoteInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveIn);
			SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveIn);

		} else if (CommUtil.compare(acctType, E_ACCTTP.IN) == 0) {// 内部户

			// 调用内部户贷方记账服务
			IaAcdrInfo iaAcdrInfo2 = SysUtil.getInstance(IaAcdrInfo.class);
			iaAcdrInfo2.setTrantp(E_TRANTP.TR);
			iaAcdrInfo2.setAcctno(input.getAcctno());
			iaAcdrInfo2.setTranam(input.getAdjtam().negate()); // 记账金额 红字
			iaAcdrInfo2.setCrcycd(input.getCrcycd());
			iaAcdrInfo2.setDscrtx(input.getRemark());
			if (CommUtil.isNotNull(input.getPayalst())) {
				iaAcdrInfo2.setPayadetail(input.getPayalst());
			}
			if (CommUtil.isNotNull(input.getPaydlst())) {
				iaAcdrInfo2.setPayddetail((Options<IaPaydDetail>) input
						.getPaydlst());
			}

		/*	CommTools.getRemoteInstance(IoInAccount.class).ioInAccr(
					iaAcdrInfo2);// 内部户贷方服
*/			SysUtil.getInstance(IoInAccount.class).ioInAccr(
					iaAcdrInfo2);// 内部户贷方服


		}

	} else if (CommUtil.compare(input.getAdjttp(), E_ADJTTP.ALL) == 0) {// 差错调整
		//收入调整
		if (CommUtil.compare(input.getCgpyrv(), E_CGPYRV.RECIVE) == 0) {
			// 贷：手续费收入红字
			cplDbFee_In.setCgpyrv(E_CGPYRV.RECIVE);
			cplDbFee_In.setTranam(input.getAdjtam().negate());// 金额 红字
			calDanbJzfee(cplDbFee_In);
			
			//调整后手续费
			CgDanbJzFee_IN cplDbFee_In1 = SysUtil.getInstance(CgDanbJzFee_IN.class);
			cplDbFee_In1.setChrgcd(input.getJtchcd());// 收费代码
			cplDbFee_In1.setProdcd(product);// 调整后产品编号
			cplDbFee_In1.setTrancy(input.getCrcycd());// 币种
			cplDbFee_In1.setAcctbr(brchno);// 机构
			cplDbFee_In1.setTrinfo(input.getJttrif());// 调整后交易信息
			cplDbFee_In1.setCstrfg(E_CSTRFG.TRNSFER);// 现转标志
			cplDbFee_In1.setCgpyrv(E_CGPYRV.RECIVE);
			cplDbFee_In1.setTranam(input.getAdjtam());// 金额 红字
			out = calDanbJzfee(cplDbFee_In1);
			
		}else if (CommUtil.compare(input.getCgpyrv(), E_CGPYRV.PAY) == 0) {//支出调整
			// 贷：手续费支出红字
			cplDbFee_In.setCgpyrv(E_CGPYRV.PAY);
			cplDbFee_In.setTranam(input.getAdjtam().negate());// 金额 红字
			calDanbJzfee(cplDbFee_In);
			
			//调整后手续费
			CgDanbJzFee_IN cplDbFee_In1 = SysUtil.getInstance(CgDanbJzFee_IN.class);
			cplDbFee_In1.setChrgcd(input.getJtchcd());// 收费代码
			cplDbFee_In1.setProdcd(product);// 产品编号 
			cplDbFee_In1.setTrancy(input.getCrcycd());// 币种
			cplDbFee_In1.setAcctbr(brchno);// 机构
			cplDbFee_In1.setCstrfg(E_CSTRFG.TRNSFER);// 现转标志
			cplDbFee_In1.setCgpyrv(E_CGPYRV.PAY);
			cplDbFee_In1.setTranam(input.getAdjtam());// 金额 红字
			cplDbFee_In1.setTrinfo(input.getJttrif());// 调整后交易信息
			out = calDanbJzfee(cplDbFee_In1);
		}

	}

	
	if (CommUtil.compare(input.getAdjttp(), E_ADJTTP.ALL) != 0) {// 非差错调整
		// 单边记费用科目账
		out = calDanbJzfee(cplDbFee_In);
		
	}

	// 登记收费调整登记薄
	if(CommUtil.isNotNull(out)){
		
	KcbAdjtRgst tbRgst = SysUtil.getInstance(KcbAdjtRgst.class);
	tbRgst.setTrandt(trandt);// 交易日期
	tbRgst.setTransq(transq);// 交易流水
	tbRgst.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());// 交易机构登记请求机构
	tbRgst.setAdjtsq(transq);//调整流水
	tbRgst.setAdjttp(input.getAdjttp());// 收费调整方式
	tbRgst.setProdno(tbrgst.getProdcd());// 原产品编码
	tbRgst.setJtprod(product);// 调整后的产品编码
	tbRgst.setChrgno(input.getChrgcd());// 收费代码
	tbRgst.setJtchno(input.getJtchcd());// 调整后的收费代码
	tbRgst.setTrinfo(trinfo);
	tbRgst.setJttrif(input.getJttrif());//调整后的交易信息
	tbRgst.setCgpyrv(input.getCgpyrv());
	tbRgst.setChnotp("");// 费种大类
	tbRgst.setCrcycd(input.getCrcycd());// 币种
	tbRgst.setAdjtam(input.getAdjtam());// 交易金额
	tbRgst.setAcctno(input.getToacct());// 对方账号
	tbRgst.setAcctna(acctna);// 对方户名
	tbRgst.setPrtrdt(input.getPrtrdt());// 原交易日期
	tbRgst.setPrtrsq(input.getPrtrsq());// 原交易流水
	tbRgst.setEvrgsq(evrgsq);
	tbRgst.setRemark(input.getRemark());// 备注信息
	tbRgst.setTranus(telleno);// 交易柜员
	tbRgst.setAuthus(auth);// 受理用户
	tbRgst.setStatus(E_STATUS.ZC);// 正常状态
	KcbAdjtRgstDao.insert(tbRgst);
	}

	
	//冲正注册		
	IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
	cplInput.setTranam(input.getAdjtam());
	cplInput.setTranac(input.getAcctno());
	cplInput.setEvent1(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期
	cplInput.setEvent2(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //交易流水
	cplInput.setCrcycd(input.getCrcycd());
	cplInput.setBgindt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
	cplInput.setTranev(ApUtil.TRANS_EVENT_CHRGJT);

	//ApStrike.regBook(cplInput);
	
	IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);    		
	apinput.setReversal_event_id(ApUtil.TRANS_EVENT_CHRGJT);
	apinput.setInformation_value(SysUtil.serialize(cplInput));
	MsEvent.register(apinput, true);
		
	//平衡性检查
	//SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._99);
	
	//TODO
	//机构、柜员额度验证
	/*BrchUserQt ioBrchUserQt = SysUtil.getInstance(BrchUserQt.class);
	ioBrchUserQt.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
	ioBrchUserQt.setBusitp(E_BUSITP.TR);
	ioBrchUserQt.setCrcycd(input.getCrcycd());
	ioBrchUserQt.setTranam(input.getAdjtam());
	ioBrchUserQt.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());
	SysUtil.getInstance(IoSrvPbBranch.class).selBrchUserQt(ioBrchUserQt);	*/
	
}

/***
 * 检查电子账户是否允许转入
 * 
 * @param cusast
 *            客户状态
 * @param acctst
 *            账户状态
 */
public static void ChkAcctstOT(E_CUACST cuacst, E_ACCTST acctst) {
	if (acctst == E_ACCTST.CLOSE) { // 关闭
		if (cuacst == E_CUACST.PRECLOS) { // 预销户
			throw FeError.Chrg.BNASF053();
		} else if (cuacst == E_CUACST.CLOSED) { // 销户
			throw FeError.Chrg.E9999("电子账户为销户");
		}
	} else if (acctst == E_ACCTST.INVALID) { // 未生效
		if (cuacst == E_CUACST.PREOPEN) { // 预开户
			throw FeError.Chrg.E9999("电子账户为预开户");
		} else if (cuacst == E_CUACST.NOACTIVE) { // 未激活
			throw FeError.Chrg.E9999("电子账户为未激活");
		} else if (cuacst == E_CUACST.NOENABLE) { // 未启用
			throw FeError.Chrg.E9999("电子账户为未启用,请先启用电子账户！");
		}
	} else if (acctst == E_ACCTST.SLEEP) { // 睡眠
		if (cuacst == E_CUACST.DORMANT) { // 休眠
			throw FeError.Chrg.E9999("电子账户为休眠,请向电子账户转入任意金额激活电子账户");
		} else if (cuacst == E_CUACST.OUTAGE) { // 停用
			throw FeError.Chrg.E9999("电子账户为停用");
		}
	}
}

public static CgDanbJzFee_OUT calDanbJzfee(CgDanbJzFee_IN cplDbFeeIn) {

    CgDanbJzFee_OUT cplOut = SysUtil.getInstance(CgDanbJzFee_OUT.class);
    String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();

    String sJzjigo = "";
    sJzjigo = cplDbFeeIn.getAcctbr();

    if (CommUtil.isNull(cplDbFeeIn.getCgpyrv())) {
        throw FeError.Chrg.BNASF066();
    }

    if (CommUtil.isNull(cplDbFeeIn.getChrgcd())) {
        throw FeError.Chrg.BNASF226();
    }

    if (CommUtil.isNull(cplDbFeeIn.getTrancy())) {
        throw FeError.Chrg.BNASF155();
    }

    if (CommUtil.isNull(cplDbFeeIn.getCstrfg())) {
        throw FeError.Chrg.BNASF273();
    }

    if (CommUtil.isNull(sJzjigo)) {
        sJzjigo = CommTools.getBaseRunEnvs().getTrxn_branch();
    }

	/*List<IoKnsProdClerInfo> cplProdCler = SysUtil.getInstance(IoAccountSvcType.class).selKnsProdClerInfo(corpno,cplDbFeeIn.getProdcd(), cplDbFeeIn.getTrinfo());
	
	if(CommUtil.isNull(cplProdCler)||cplProdCler.size()==0){
		
		throw FeError.Chrg.BNASF011();
	}        */

    // 登记会计流水开始
    IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
    cplIoAccounttingIntf.setCuacno(cplDbFeeIn.getChrgcd()); //记账账号-登记收费代码
    cplIoAccounttingIntf.setAcseno(cplDbFeeIn.getChrgcd()); //子账户序号-登记收费代码
    cplIoAccounttingIntf.setAcctno(cplDbFeeIn.getChrgcd()); //负债账号-登记收费代码
    cplIoAccounttingIntf.setProdcd(cplDbFeeIn.getProdcd()); //产品编号
    cplIoAccounttingIntf.setDtitcd(cplDbFeeIn.getProdcd()); //核算口径-核算业务编号
    cplIoAccounttingIntf.setCrcycd(cplDbFeeIn.getTrancy()); //币种                 
    cplIoAccounttingIntf.setTranam(cplDbFeeIn.getTranam()); //交易金额 
    cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
    cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //主交易流水
    cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期 
    cplIoAccounttingIntf.setAcctbr(sJzjigo); //账务机构
    cplIoAccounttingIntf.setTranms(cplDbFeeIn.getTrinfo());//交易信息

    if (cplDbFeeIn.getCgpyrv() == E_CGPYRV.PAY) { //付费，记借方
        cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR); //借贷标志-借方
    }
    else if (cplDbFeeIn.getCgpyrv() == E_CGPYRV.RECIVE) { //收费，记贷方
        cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR); //借贷标志-贷方   
    }

    cplIoAccounttingIntf.setAtowtp(E_ATOWTP.FE); //会计主体类型-手续费
    cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型-账务流水
    cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE); //余额属性-本金科目
    //登记会计流水
    SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);

    cplOut.setChrgcd(cplDbFeeIn.getChrgcd());

    return cplOut;
}
/**
 * 公共收费
 */

@Override
public void publicCharge(
		PublicCharge_IN input,
		cn.sunline.ltts.busi.cg.servicetype.ChrgSvcType.publicCharge.Output output) {

	
	String  custac="";
	if(CommUtil.isNull(input.getTrantp())){
		throw FeError.Chrg.BNASF160();	
	}
	if(CommUtil.isNull(input.getRemark())){
		throw FeError.Chrg.BNASF325();	
	}
	String sendbr = CommTools.getBaseRunEnvs().getTrxn_branch();//请求机构
	
	if(input.getTrantp()==E_CGTRTP.SAVE&&CommUtil.isNull(input.getChrgtp())){
		throw FeError.Chrg.BNASF161();
	}
	/*if(SysUtil.getInstance(IoSrvPbBranch.class).getBranch(sendbr).getBrchtp()!=E_BRCHTP.CLEA&& input.getTrantp() != E_CGTRTP.SAVE){
		throw FeError.Chrg.BNASF163();	
	}*/
	if(CommUtil.compare(input.getTotlam(), BigDecimal.ZERO)<=0){
		
		throw FeError.Chrg.BNASF159();	
	}
	if(CommUtil.isNull(input.getAcctno())){
		throw FeError.Chrg.BNASF329();
	}		
	if(CommUtil.isNull(input.getAcctna())){
		throw FeError.Chrg.BNASF333();
	}		
	E_ACCTROUTTYPE accttp= ApAcctRoutTools.getRouteType(input.getAcctno());
	if((input.getTrantp()==E_CGTRTP.INACCOUNT||input.getTrantp()==E_CGTRTP.OUTACCOUNT)&&accttp!=E_ACCTROUTTYPE.INSIDE){
		
		throw FeError.Chrg.BNASF237();
	}
	if((input.getTrantp()==E_CGTRTP.PAY||input.getTrantp()==E_CGTRTP.SAVE)&&accttp==E_ACCTROUTTYPE.INSIDE){
		
		throw FeError.Chrg.BNASF221();
	}
	if(accttp==E_ACCTROUTTYPE.INSIDE){
		IoInacInfo inacInfo =SysUtil.getInstance(IoInQuery.class).InacInfoQuery(input.getAcctno());
		
		//内部户必须再所属机构进行交易
		if(!CommUtil.equals(inacInfo.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch())){
			
			throw FeError.Chrg.BNASF185();
		}
		if(!CommUtil.equals(inacInfo.getAcctna(), input.getAcctna())){
			
			throw FeError.Chrg.BNASF238();
		}
		if(inacInfo.getBusidn()!=E_BLNCDN.C){
			
			throw FeError.Chrg.BNASF190();
		}
		if(inacInfo.getAcctst() == E_INACST.CLOSED){
			
			throw FeError.Chrg.BNASF103();
		}
		if(inacInfo.getKpacfg() == E_KPACFG._1){
			
			throw FeError.Chrg.BNASF102();
		}
		
		//收入入账，可用余额检查
		if(input.getTrantp() == E_CGTRTP.INACCOUNT){
			if(inacInfo.getPmodtg() == BaseEnumType.E_YES___.YES){
//				
				if(CommUtil.compare(inacInfo.getOnlnbl().add(inacInfo.getOvmony()), input.getTotlam()) <0){
					throw FeError.Chrg.BNASF336();
				}
			}else{
				
				if(CommUtil.compare(inacInfo.getOnlnbl(), input.getTotlam()) <0){
					throw FeError.Chrg.BNASF336();
				}
			}
			
		}
		
	}else if(accttp==E_ACCTROUTTYPE.DEPOSIT){

		IoCaKnaAccs tblKnaAccs = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAccsOdb3(input.getAcctno(), true);
		
		//add by wuzx 20161102 收费增加电子账户状态字校验-end
		IoDpKnaAcct tbkKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(tblKnaAccs.getCustac());
		
		custac=tbkKnaAcct.getCustac();
		//非省中心，内部户必须再所属机构进行交易
		if(!CommUtil.equals(tbkKnaAcct.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch())){
			
			throw FeError.Chrg.BNASF185();
		}	
		if(!CommUtil.equals(tbkKnaAcct.getAcctna(), input.getAcctna())){
			
			throw FeError.Chrg.BNASF238();
		}
		
		//收费电子账户状态检查
		if(input.getTrantp()==E_CGTRTP.SAVE){
			//获得账户法人 add by chenjk 账户状态检查
		/*	String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
			CommTools.getBaseRunEnvs().setBusi_org_id(tbkKnaAcct.getCorpno());
			*/
			//客户化状态判断
			E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
			if(cuacst != E_CUACST.NORMAL){
				
				throw FeError.Chrg.BNASF393(cuacst.getLongName());
			}

			// 调用DP模块服务查询冻结状态，检查
			IoDpFrozSvcType froz = SysUtil.getInstance(IoDpFrozSvcType.class);
			IoDpAcStatusWord cplGetAcStWord = froz.getAcStatusWord(custac);
			if(cplGetAcStWord.getAlstop() == BaseEnumType.E_YES___.YES){
				throw FeError.Chrg.BNASF094();
			}
			if(cplGetAcStWord.getBrfroz() == BaseEnumType.E_YES___.YES){
				throw FeError.Chrg.BNASF093();
			}
			if(cplGetAcStWord.getDbfroz() == BaseEnumType.E_YES___.YES){
				throw FeError.Chrg.BNASF095();
			}
			
			//可用余额判断
			BigDecimal usebal = SysUtil.getInstance(DpAcctSvcType.class).getAcctaAvaBal(custac, tbkKnaAcct.getAcctno(), tbkKnaAcct.getCrcycd(), BaseEnumType.E_YES___.YES, BaseEnumType.E_YES___.NO);
			if(CommUtil.compare(usebal, input.getTotlam()) < 0){
				
				throw FeError.Chrg.BNASF337();
			}
			
			/*CommTools.getBaseRunEnvs().setBusi_org_id(corpno);*/
		}
	}


	//统一收费输入
	IoCgCalFee_IN cgFeeIn = SysUtil.getInstance(IoCgCalFee_IN.class);	
	
	//收费或者收入入账
	if(E_CHARTP.XJ!=input.getChrgtp()){
		
		BigDecimal tranam = BigDecimal.ZERO;
		for(ChargeInfo info: input.getChargeInfo()){
			
			tranam =tranam.add(info.getChrgam());
		}
		if(CommUtil.compare(tranam, input.getTotlam())!=0){
			throw FeError.Chrg.BNASF124();
		}
		
		if(input.getChargeInfo().size()<1){
			
			throw FeError.Chrg.BNASF065();
		}			
		
		if(input.getChargeInfo().size()>5){
			
			throw FeError.Chrg.BNASF213();
		}			
		
		//内部户收费付费时，产品编码必输
		if(accttp==E_ACCTROUTTYPE.INSIDE){
			for(ChargeInfo info: input.getChargeInfo()){
				if(CommUtil.isNull(info.getProdcd())){
					throw FeError.Chrg.BNASF069();	
				}
			}
		}

		cgFeeIn.setChgflg(E_CHGFLG.ALL);//费用和客户账同时记账
		cgFeeIn.setTrancy(input.getCrcycd());//交易币种
		cgFeeIn.setChrgcy(input.getCrcycd());//收费币种
		cgFeeIn.setCstrfg(E_CSTRFG.TRNSFER);//现转标志
		cgFeeIn.setCustac(CommUtil.nvl(custac,input.getAcctno()));//客户账号或内部户
		cgFeeIn.getPayaDetail().addAll(input.getPayaListInfo());//挂账明细
		cgFeeIn.getPaydDetail().addAll(input.getPaydListInfo());//销账明细	
		cgFeeIn.setIsclos(cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES);//销记收费记录
		cgFeeIn.setRemark(input.getRemark());//摘要
		cgFeeIn.setSmryds(input.getRemark());//备注

		//计费中心返回，此交易直接前台输入
		for(ChargeInfo info: input.getChargeInfo()){
			//计费中心返回，此交易直接前台输入
			IoCgCalCenterReturn cginfo= SysUtil.getInstance(IoCgCalCenterReturn.class);
			
			//add by songkl 20161107 新增收付费场景校验  -begin
			if(input.getTrantp() == E_CGTRTP.PAY || input.getTrantp() == E_CGTRTP.INACCOUNT ||input.getTrantp() == E_CGTRTP.OUTACCOUNT){
				if(CommUtil.isNull(info.getScencd())){
					throw FeError.Chrg.BNASF165();
				}
			}
			
			if(input.getTrantp() == E_CGTRTP.SAVE && CommUtil.isNotNull(input.getChrgtp())){
				if(input.getChrgtp() == E_CHARTP.SS && CommUtil.isNull(info.getScencd())){
					throw FeError.Chrg.BNASF164();
				}
				if(input.getChrgtp() == E_CHARTP.XJ && CommUtil.isNotNull(info.getScencd())){
					throw FeError.Chrg.BNASF231();
				}
			}
			//add by songkl 20161107 新增收付费场景校验  -end
			
			if(CommUtil.isNull(info.getChrgcd())){
				throw FeError.Chrg.BNASF076();	
			}
			if(info.getChrgcd().substring(2, 3).equals("0")){//收费费种代码
				if(input.getTrantp()==E_CGTRTP.PAY||input.getTrantp()==E_CGTRTP.OUTACCOUNT){
					throw FeError.Chrg.BNASF386();	
				}
			}else if (info.getChrgcd().substring(2, 3).equals("1")){
				if(input.getTrantp()==E_CGTRTP.SAVE||input.getTrantp()==E_CGTRTP.INACCOUNT){
					throw FeError.Chrg.BNASF387();	
				}					
			}else{
				throw FeError.Chrg.BNASF075();	
			}
			cginfo.setChrgcd(info.getChrgcd());//费种代码
			
			if(CommUtil.isNull(info.getTrinfo())){
				throw FeError.Chrg.BNASF166();	
			}				
			cginfo.setTrinfo(info.getTrinfo());//交易信息
			
			if(CommUtil.compare(info.getChrgam(),BigDecimal.ZERO)<=0){
				throw FeError.Chrg.BNASF229();	
			}
			cginfo.setTranam(info.getChrgam());//交易金额
			cginfo.setClcham(info.getChrgam());//计算金额	
			cginfo.setDircam(info.getChrgam());//优惠后应收
			cginfo.setPaidam(info.getChrgam());//收费金额
			cginfo.setPronum(info.getProdcd());//产品代码	
			
			if(CommUtil.isNull(info.getServtp())){
				throw FeError.Chrg.BNASF197();	
			}					
			cginfo.setServtp(info.getServtp());//渠道
			cginfo.setScencd(info.getScencd());
			cginfo.setScends(info.getScends());
			cgFeeIn.getCalcenter().add(cginfo);
		}	
		cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType cg = new IoCgChrgSvcImpl();
		cg.CalCharge(cgFeeIn);
		
	}else if (E_CHARTP.XJ==input.getChrgtp()) {
		
		if(CommUtil.isNull(input.getBgdate())){
			throw FeError.Chrg.BNASF276();
		}
		if(CommUtil.compare(input.getBgdate(),CommTools.getBaseRunEnvs().getTrxn_date())>0){
			
			throw FeError.Chrg.BNASF191();
		}
		if(CommUtil.isNull(input.getEndate())){
			throw FeError.Chrg.BNASF275();
		}
		if(CommUtil.compare(input.getEndate(),CommTools.getBaseRunEnvs().getTrxn_date())>0){
			
			throw FeError.Chrg.BNASF167();
		}	
		if(CommUtil.compare(input.getEndate(), input.getBgdate()) < 0){
			throw FeError.Chrg.BNASF168();
		}
		bizlog.debug(">>>>>>>>>>>>>>>>> 销记收费登记簿开始>>>>>>>>>>>>");
		//销记收费登记簿
		cgFeeIn.setChgflg(E_CHGFLG.ALL);//费用和客户账同时记账
		cgFeeIn.setTrancy(input.getCrcycd());//交易币种
		cgFeeIn.setChrgcy(input.getCrcycd());//收费币种
		cgFeeIn.setCstrfg(E_CSTRFG.TRNSFER);//现转标志
		cgFeeIn.setCustac(custac);//客户账号	
		cgFeeIn.getPayaDetail().addAll(input.getPayaListInfo());//挂账明细
		cgFeeIn.getPaydDetail().addAll(input.getPaydListInfo());//销账明细	
		//销记明细查询
		List<KcbChrgRgst>  tblKcbChrgRgst = PBChargeRegisterDao.selChargeRegistrInfoForCharge(input.getBgdate(), input.getEndate(), custac, false);
		if(tblKcbChrgRgst.size()<=0){
			throw FeError.Chrg.BNASF274();
		}
		int i=0;
		BigDecimal sumTranam = BigDecimal.ZERO;
		for(KcbChrgRgst rgst: tblKcbChrgRgst){
			
			//收费输入
			CgChargeFee_IN cplChFeeIn = SysUtil.getInstance(CgChargeFee_IN.class);
			
			cplChFeeIn.setOprflg(E_OPRFLG.SEQNO); /* 按计费流水+序号收 */
			cplChFeeIn.setTrnseq(rgst.getTrnseq()); //柜员流水
			cplChFeeIn.setEvrgsq(rgst.getEvrgsq()); //事件登记序号
			cplChFeeIn.setIfflag(E_YES___.YES); //是否标志 默认是
			cplChFeeIn.setTrancy(rgst.getChrgcy()); //交易币种
			cplChFeeIn.setTrandt(rgst.getTrandt()); //交易日期
			cplChFeeIn.setCstrfg(E_CSTRFG.TRNSFER); //现转标志
			
			cplChFeeIn.setDecuac(rgst.getCustac()); //客户账号
			cplChFeeIn.setCustno(rgst.getCustno());//客户号
			cplChFeeIn.setAcclam(rgst.getArrgam()); //实收金额
			cplChFeeIn.setIsclos(cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES);//销记收费记录
			cplChFeeIn.setRemark(input.getRemark());
			cplChFeeIn.setSmryds(input.getRemark());
			bizlog.debug(">>>>>>>>>>>>>>>cplChFeeIn[%s]", cplChFeeIn);
			
			ChargeProc.chargeFeeOff(cplChFeeIn); //收费处理
			
			sumTranam=sumTranam.add(rgst.getArrgam());
			
			bizlog.debug(">>>>>>>>>>>>>>>> 销记收费登记簿结束>>>>>>>>>>>>>>");	
			ChargeInfo info =SysUtil.getInstance(ChargeInfo.class);
			CommUtil.copyProperties(info, rgst);
			
			i++;
			info.setChrgsq(i);
			info.setChrgam(rgst.getArrgam());//收费金额=欠费金额
			info.setServtp(rgst.getTrnchl());
			rgst.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
			output.getChargeInfo().add(info);
							
		}
		if(CommUtil.compare(sumTranam, input.getTotlam())!=0){
			
			throw FeError.Chrg.BNASF277();
		}
		
	}
	SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._10);
	//机构、柜员额度验证
	
	//TODO
/*	IoBrchUserQt ioBrchUserQt = SysUtil.getInstance(IoBrchUserQt.class);
	ioBrchUserQt.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
	ioBrchUserQt.setBusitp(E_BUSITP.TR);
	ioBrchUserQt.setCrcycd(input.getCrcycd());
	ioBrchUserQt.setTranam(input.getTotlam());
	ioBrchUserQt.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());
	SysUtil.getInstance(IoSrvPbBranch.class).selBrchUserQt(ioBrchUserQt);
*/
	
}



}

