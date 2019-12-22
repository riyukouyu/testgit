package cn.sunline.ltts.busi.dptran.trans.client;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPidl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPidlDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwne;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwneDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbDraw;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbDrawDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbSmsSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaSignDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubSqrd;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CORDST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.ltts.busi.sys.type.yht.E_ISOVER;
import cn.sunline.ltts.busi.sys.type.yht.E_TRANST;


public class smrans {
	/**
	 * 定期智能存款支取交易前处理
	 * **/

	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smrans.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smrans.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smrans.Output output){
		if(CommUtil.isNull(input.getCardno())){
			throw DpModuleError.DpstComm.BNAS0955();
		}
		if(CommUtil.isNull(input.getAcctno())){
			throw DpModuleError.DpstComm.BNAS0839();
		}
		
		if(CommUtil.isNull(input.getTranam())){
			throw DpModuleError.DpstAcct.BNAS0623();
		}
		if(CommUtil.isNull(input.getCrcycd())){
			throw DpModuleError.DpstAcct.BNAS0634();
		}
		
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		//赎回不检查涉案账户和可疑账户
		//根据客户电子账号（卡号）获取电子账号ID
		IoCaKnaAcdc acdc = caqry.getKnaAcdcOdb2(input.getCardno(), false);
		if(CommUtil.isNull(acdc) || acdc.getStatus() == E_DPACST.CLOSE){
			throw DpModuleError.DpstComm.BNAS0750();
		}
		//根据负债子账号查询负债信息
		KnaFxac fxac = KnaFxacDao.selectOne_odb1(input.getAcctno(), false);
		IoCaKnaSignDetl cplKnaSignDetl = DpAcctDao.selKnaSignDetlInfo(input.getAcctno(), false);
		
		if(CommUtil.isNull(fxac) || 
				fxac.getDebttp() == E_DEBTTP.DP2404 || 
				CommUtil.isNotNull(cplKnaSignDetl)){
			throw DpModuleError.DpstComm.BNAS0879();
		}
		
		if(CommUtil.isNotNull(fxac) && fxac.getAcctst() == E_DPACST.CLOSE){
			throw DpModuleError.DpstComm.BNAS0759();
		}
		
		//查询是否子账户冻结   by  zhx
		KnbFrozOwne frozowne  =  KnbFrozOwneDao.selectOne_odb1(E_FROZOW.ACCTNO, input.getAcctno(), false);
		if (CommUtil.isNotNull(frozowne)) {
			throw CaError.Eacct.BNAS0435();
		}

		
		//判断产品编号
		if(CommUtil.isNotNull(input.getProdcd())){
			if(!CommUtil.equals(input.getProdcd(), fxac.getProdcd())){
				throw DpModuleError.DpstComm.BNAS1034();
			}
		}
		//检查输入的币种是否与购买时的币种一致
		if(!CommUtil.equals(input.getCrcycd(), fxac.getCrcycd())){
			throw DpModuleError.DpstComm.BNAS1026();
		}
		
		//判断输入的负债账号是否与电子账号有关联
		if(!CommUtil.equals(acdc.getCustac(), fxac.getCustac())){
			throw DpModuleError.DpstComm.BNAS0310();
		}
		
		if(CommUtil.compare(fxac.getOnlnbl(), input.getTranam()) == 0){ //判断是否全额支取
			property.setIsclos(E_YES___.YES);
		}
		
		//检查账户状态
		CapitalTransCheck.ChkAcctstOT(acdc.getCustac());
									
		//调用查询电子账户状态字服务
		IoDpFrozSvcType ioDpFrozSvcType = SysUtil.getInstance(IoDpFrozSvcType.class);
		IoDpAcStatusWord  cplAcStatusWord = ioDpFrozSvcType.getAcStatusWord(acdc.getCustac());

		if(E_YES___.YES == cplAcStatusWord.getDbfroz()){
			throw DpModuleError.DpstComm.BNAS0430();
		}
						
		if(E_YES___.YES == cplAcStatusWord.getBrfroz()){
			throw DpModuleError.DpstComm.BNAS0432();
		}
						
		if(E_YES___.YES == cplAcStatusWord.getBkalsp()){
			throw DpModuleError.DpstComm.BNAS0439();
		}
			
		if(E_YES___.YES == cplAcStatusWord.getClstop()){
			throw DpModuleError.DpstComm.BNAS0438();
		}
						
		if(E_YES___.YES == cplAcStatusWord.getOtalsp()){
			throw DpModuleError.DpstComm.BNAS1931();
		}
		
		//查询结算户信息
		KnaAcct acct = CapitalTransDeal.getSettKnaAcctSub(acdc.getCustac(), E_ACSETP.SA);
		
		property.setAcseno("");
		property.setAuacfg(E_YES___.NO);
		property.setCustac(acdc.getCustac());
		property.setLinkno("");
		property.setOpacna(acct.getAcctna());
		property.setToacct(acct.getAcctno());
		property.setOpbrch(acct.getBrchno());
		// 摘要码
		BusiTools.getBusiRunEnvs().setSmrycd(BusinessConstants.SUMMARY_TZ);//投资
		
		// 缴税摘要代码
		property.setSmrycd(BusinessConstants.SUMMARY_JS);//缴税
		//备注信息
		KnaFxacProd tblKnaFxacProd = KnaFxacProdDao.selectOne_odb1(fxac.getAcctno(), false);
		if (CommUtil.isNotNull(tblKnaFxacProd)) {
			property.setRemark("支取-"+tblKnaFxacProd.getObgaon());
		}
		
		 //poc
        KnaAcdc kacdc=KnaAcdcDao.selectFirst_odb1(acdc.getCustac(), E_DPACST.NORMAL, false);
        if(CommUtil.isNotNull(kacdc)){
        	ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
    		apAudit.regLogOnInsertBusiPoc(kacdc.getCardno());
        }
		
	}
	
	/**
	 * 定期智能存款支取交易后处理
	 * **/
	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smrans.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smrans.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smrans.Output output){
		
		/**
		 * add xj 20180910 短信通知登记
		 */
		//获取产品信息
		KupDppb tblKupDppb = KupDppbDao.selectOne_odb1(input.getProdcd(), false);
		//登记短信信息
		IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
//		cplKubSqrd.setAppsid();//APP推送ID 
		cplKubSqrd.setCardno(input.getCardno());//交易卡号  
		cplKubSqrd.setPmvl01(CommUtil.isNotNull(tblKupDppb)? tblKupDppb.getProdtx():null);//参数01  产品名称 
//		cplKubSqrd.setPmvl02();//参数02    
//		cplKubSqrd.setPmvl03();//参数03    
//		cplKubSqrd.setPmvl04();//参数04    
//		cplKubSqrd.setPmvl05();//参数05    
		cplKubSqrd.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());//内部交易码
		cplKubSqrd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期  
		cplKubSqrd.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());//交易流水  
		cplKubSqrd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());//交易时间  
		IoPbSmsSvcType svcType = SysUtil.getInstance(IoPbSmsSvcType.class);
		svcType.pbTransqReg(cplKubSqrd);
		/**end*/
		
		
		//根据销户标志修改负债账号客户账号对照表记录状态
		if(property.getIsclos() == E_YES___.YES){
			IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			caqry.updateCaDaoKnaAccsByAcctno(E_ACCTST.CLOSE, input.getAcctno());
			
			//根据销户标志发送异步消息至合约库
		}
		
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
		//add by zdj 20181026
        output.setTranst(E_TRANST.SUCCESS);//订单状态
        output.setIntere(property.getInstam());//支取利息
        KupDppb kup = KupDppbDao.selectOne_odb1(input.getProdcd(), false);
        if(CommUtil.isNotNull(kup)){
            if(E_FCFLAG.CURRENT == kup.getPddpfg()){
                KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(input.getAcctno(), false);
                if(CommUtil.isNotNull(tblKnaAcct)){
                    //到期日期和当前系统日期比较
                    output.setIsover(CommUtil.compare(tblKnaAcct.getMatudt(), CommTools.getBaseRunEnvs().getTrxn_date())>0?E_ISOVER.CONTINUE:E_ISOVER.OVER);//是否到期
                }
            }else if(E_FCFLAG.FIX == kup.getPddpfg()){
                KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(input.getAcctno(), false);
                if(CommUtil.isNotNull(tblKnaFxac)){
                    //到期日期和当前系统日期比较
                    output.setIsover(CommUtil.compare(tblKnaFxac.getMatudt(), CommTools.getBaseRunEnvs().getTrxn_date())>0?E_ISOVER.CONTINUE:E_ISOVER.OVER);//是否到期
                }
            }
        }
        output.setActamt(null);//实际支取金额
        output.setMntrsq(CommTools.getBaseRunEnvs().getTrxn_seq());//交易流水
        output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());//交易时间
        output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());//交易时间
        output.setDrdate(CommTools.getBaseRunEnvs().getTrxn_date());//支取日期
        //add end
		output.setIsclos(property.getIsclos());
	}

	public static void DealInstam( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smrans.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smrans.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smrans.Output output){
		//转入金额等于交易金额+支取利息-追缴利息
	    property.setAcctam(property.getInstam().add(input.getTranam()).subtract(property.getPyafam()));
		property.setIntxam(property.getIntxam());//利息税
		
	}

	public static void drawRegister( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smrans.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smrans.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smrans.Output output){
		KnbDraw tblKnbDraw = SysUtil.getInstance(KnbDraw.class);
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		tblKnbDraw.setOrdrid(input.getOrdrid()); //订单号
		//证件类型、证件号码不能为空
        if (CommUtil.isNull(input.getIdtfno())) {
            CommTools.fieldNotNull(input.getIdtfno(), BaseDict.Comm.idtfno.getId(), BaseDict.Comm.idtfno.getLongName());
        }
        if (CommUtil.isNull(input.getIdtftp())) {
            CommTools.fieldNotNull(input.getIdtftp(), BaseDict.Comm.idtftp.getId(), BaseDict.Comm.idtftp.getLongName());
        }
        KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(input.getCardno(), false);
        String custac = tblKnaAcdc.getCustac();
        KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(custac, false);
        String custno = tblKnaCust.getCustno();
//        CifCust tblCifCust = CifCustDao.selectOne_odb1(custno, false);
//        E_IDTFTP idtftp = tblCifCust.getIdtftp();
//        String idtfno = tblCifCust.getIdtfno(); 
        
//		if (CommUtil.compare(idtftp, input.getIdtftp()) !=0 || CommUtil.compare(idtfno, input.getIdtfno()) !=0){
//			throw DpModuleError.DpstComm.E9999("电子账号与客户不匹配");
//		} else{
//			tblKnbDraw.setIdtftp(idtftp); //证件类型
//			tblKnbDraw.setIdtfno(idtfno); //证件号码
//		}
		tblKnbDraw.setCardno(input.getCardno()); //电子账号
		tblKnbDraw.setRedqam(input.getTranam()); //交易金额
		tblKnbDraw.setAcctno(input.getAcctno()); //负债账号
		tblKnbDraw.setOrdedt(input.getOrdedt()); //交易订单日期
		tblKnbDraw.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //交易流水
		tblKnbDraw.setTrandt(trandt); //交易日期
		tblKnbDraw.setCordst(E_CORDST.SUCCESS); //订单状态
		tblKnbDraw.setChnlid(input.getChnlid()); //交易渠道
		tblKnbDraw.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq()); //业务流水
		KnbPidl tblKnbPidl = KnbPidlDao.selectFirst_odb1(input.getAcctno(), false);
		if (CommUtil.isNotNull(tblKnbPidl)){
			tblKnbDraw.setInrmon(tblKnbPidl.getRlintr());
		}
		KupDppb tblKupDppb = KupDppbDao.selectOne_odb1(input.getProdcd(), false);
		if (CommUtil.isNull(tblKupDppb)){
			throw DpModuleError.DpstComm.E9999("产品代码未配置！");
		} else{
			tblKnbDraw.setBupdcd(input.getProdcd()); //产品代码
			if (CommUtil.isNotNull(tblKupDppb.getInefdt()) && CommUtil.compare(tblKupDppb.getInefdt(), trandt) < 0){
				tblKnbDraw.setIsexpire(E_YES___.YES); //到期
			} else{
				tblKnbDraw.setIsexpire(E_YES___.NO); //未到期
			}
		}
		KnbDrawDao.insert(tblKnbDraw);
		
	}
}
