package cn.sunline.ltts.busi.dp.froz;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.logging.LogConfigManager.SystemType;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpFrozDao;
import cn.sunline.ltts.busi.dp.namedsql.DpSaveDrawDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbDfir;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbDfirDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDedu;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDeduDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetl;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetlDao;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaAcctJoint;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaAcctJointDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdr;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdrDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdrPlan;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdrPlanDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.DeductOut;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInOpenClose;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaSignDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDeductIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpUnStopPayIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IofrozInfo;
import cn.sunline.ltts.busi.iobus.type.in.IoInOpenCloseComplex.IoInacOpen_IN;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DRAWCT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DWBKDL;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DWBKLI;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVPLFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AMNTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CTRLWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DEDUTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_MONYTO;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SPECTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TEARTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TIMEWY;


public class DpDeduct {
	private static BizLog log = BizLogUtil.getBizLog(DpDeduct.class);

	/**
	 * 扣划检查
	 * 
	 * @param cpliodpfrozin
	 */
	

	public static void deductCheck(IoDeductIn deductin) {
		if (CommUtil.isNull(deductin.getDedutp())) {
			throw DpModuleError.DpstComm.BNAS0505();
		}
		if (CommUtil.isNull(deductin.getCardno())) {
			throw DpModuleError.DpstProd.BNAS0926();
		}
		
		if(CommUtil.isNull(deductin.getCustna())){
			throw DpModuleError.DpstComm.BNAS0533();
		}
		
		if (deductin.getDedutp() != E_DEDUTP.DEDUIN && deductin.getDedutp() != E_DEDUTP.DEDUOT) {
			throw DpModuleError.DpstComm.BNAS0504();
		}
		if (CommUtil.isNull(deductin.getTrantp())) {
			throw DpModuleError.DpstComm.BNAS0618();
		}
		
		if(CommUtil.isNull(deductin.getCrcycd())){
			throw DpModuleError.DpstComm.BNAS1101();
		}
		
		if(CommUtil.equals(BusiTools.getDefineCurrency() , deductin.getCrcycd()) && CommUtil.isNotNull(deductin.getCsextg())){
			throw DpModuleError.DpstComm.BNAS1098();
		}
		
		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil
                .getInstance(IoCaSevQryTableInfo.class);
        IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(
        		deductin.getCardno(), false);
        if(CommUtil.isNull(caKnaAcdc)){
        	throw DpModuleError.DpstComm.BNAS0711();
        }
        IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(caKnaAcdc.getCustac());
        if (!CommUtil.equals(deductin.getCrcycd(), cplKnaAcct.getCrcycd())) {
            throw DpModuleError.DpstComm.E9999("输入币种与账户币种不一致");
        }
		
		if (!CommUtil.equals(deductin.getTrantp().getValue(), "TR")) {
			throw DpModuleError.DpstComm.BNAS0616();
		}
		
		
		if (deductin.getDedutp() == E_DEDUTP.DEDUIN
				&& !CommUtil.isNull(deductin.getFrozno())) {
			throw DpModuleError.DpstComm.BNAS0503();
		}
		if (CommUtil.isNull(deductin.getDectno())) {
			throw DpModuleError.DpstComm.BNAS0498();
		}

		if(CommUtil.compare(deductin.getDeduam(), BigDecimal.ZERO) == 0){
			throw DpModuleError.DpstComm.BNAS0506();
		}
		
		if (CommUtil.compare(deductin.getDeduam(), BigDecimal.ZERO) < 0) {
			throw DpModuleError.DpstComm.BNAS0508();

		}
		
		if (CommUtil.isNull(deductin.getMonyto())) {
		    deductin.setMonyto(E_MONYTO.TOSETTEM);
		}
		
		if(deductin.getDedutp() == E_DEDUTP.DEDUIN){
			if (CommUtil.isNotNull(deductin.getDeogtp())) {
				throw DpModuleError.DpstComm.BNAS0449();
			}
			
			if (CommUtil.isNull(deductin.getDeogna())) {
				throw DpModuleError.DpstComm.BNAS0448();
			}
			
			if (CommUtil.isNotNull(deductin.getFrozdt())) {
				throw DpModuleError.DpstComm.BNAS0452();
			}
			
			if (CommUtil.isNotNull(deductin.getFrozno())) {
				throw DpModuleError.DpstComm.BNAS0451();
			}
			
			if (CommUtil.isNotNull(deductin.getFrozno())) {
				throw DpModuleError.DpstComm.BNAS0450();
			}
			
			//内部扣划时-只能选择结算暂收
			if(E_MONYTO.TOSETTEM != deductin.getMonyto() &&  E_MONYTO.TOINNACC != deductin.getMonyto() ){
				throw DpModuleError.DpstComm.BNAS0453();
			}
		}
		if(deductin.getDedutp() == E_DEDUTP.DEDUOT){
			if (CommUtil.isNull(deductin.getDeogtp())) {
				throw DpModuleError.DpstComm.E9999("外部扣划时，有权机构类型不允许为空");
			}
			if (CommUtil.isNull(deductin.getDeogna())) {
				throw DpModuleError.DpstComm.BNAS1233();
			}
			
			//冻结序号不为空，确定为扣划冻结金额
			if(CommUtil.isNotNull(deductin.getFrozno())){
//				if(CommUtil.isNull(deductin.getUfctno())){
//					throw DpModuleError.DpstComm.E9999("解冻通知书编号不允许为空");
//				}
				
				if(CommUtil.isNull(deductin.getFrozdt())){
					throw DpModuleError.DpstComm.BNAS0828();
				}
			}
		}
		if (CommUtil.isNull(deductin.getOpna01())) {
			throw DpModuleError.DpstComm.BNAS0234();
		}	
		if (CommUtil.isNull(deductin.getOptp01())) {
			throw DpModuleError.DpstComm.BNAS0233();
		}
		if (CommUtil.isNull(deductin.getOpno01())) {
			throw DpModuleError.DpstComm.BNAS1596();
		}
		//校验证件类型、证件号码
		if(deductin.getOptp01() == E_IDTFTP.SFZ){
			cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp = cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP.SFZ;
			BusiTools.chkCertnoInfo(idtftp, deductin.getOpno01());
		}
		if (deductin.getDedutp() == E_DEDUTP.DEDUOT){
			if (CommUtil.isNull(deductin.getOpna02())) {
				throw DpModuleError.DpstComm.BNAS0502();
			}
			if (CommUtil.isNull(deductin.getOptp02())) {
				throw DpModuleError.DpstComm.BNAS0500();
			}
			if (CommUtil.isNull(deductin.getOpno02())) {
				throw DpModuleError.DpstComm.BNAS0501();
			}
			//校验证件类型、证件号码
			if(deductin.getOptp02() == E_IDTFTP.SFZ){
				cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp = cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP.SFZ;
				BusiTools.chkCertnoInfo(idtftp, deductin.getOpno02());
			}
		}
		// 新增配置账户KnpParameter
		// 查询对应账户
		// 赋值
		
		
		if (CommUtil.isNull(deductin.getOtheac())) {
			/*throw DpModuleError.DpstComm.BNAS0811();*/
		    KnpParameter KnpParameter= KnpParameterDao.selectOne_odb1("DPTRAN", "DEDUCT", "TOACCT", "%", true);
		    IoInacOpen_IN inopac= SysUtil.getInstance(IoInacOpen_IN.class);
	        inopac.setBusino(KnpParameter.getParm_value1());
	        inopac.setSubsac(KnpParameter.getParm_value2());
	        inopac.setCrcycd(KnpParameter.getParm_value3());
	        String repacc = SysUtil.getInstance(IoInOpenClose.class).inacOpen(inopac);
		    deductin.setOtheac(repacc);
		    deductin.setOthena(KnpParameter.getParm_value4());
		}else{
		    if (CommUtil.isNull(deductin.getOthena())) {
		        throw DpModuleError.DpstComm.BNAS0812();
		        }
		    }
		if (CommUtil.isNull(deductin.getReason())) {
			throw DpModuleError.DpstComm.BNAS0499();
		}
	}

	/**
	 * 直接扣划检查并获取扣划限制金额
	 * 
	 * @param deductin totalam
	 */
	public static void frozDireDeduCheck(IoDeductIn deductin, String trandt) {
		BigDecimal totalam = BigDecimal.ZERO;
		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(deductin.getCardno(), false);
		if(CommUtil.isNull(caKnaAcdc)){
			throw DpModuleError.DpstComm.BNAS0692();
		}
		String custac = caKnaAcdc.getCustac();
		
		// 根据客户账号查询冻结，续冻，外部止付信息
		List<KnbFroz> lstTblKnbFroz = KnbFrozDao.selectAll_odb3(custac, E_FROZST.VALID, false);
		if (CommUtil.isNotNull(lstTblKnbFroz)) {
			for (KnbFroz knbFroz : lstTblKnbFroz) {
	  	        if ((knbFroz.getFroztp() == E_FROZTP.JUDICIAL || knbFroz.getFroztp() == E_FROZTP.ADD)
	  	        		&& knbFroz.getFrlmtp() != E_FRLMTP.AMOUNT) {
	  	        	throw DpModuleError.DpstComm.BNAS0999();
	  	        }
	  	        
	  	        // 记录优先权高的冻结金额总和
	  	        // 为冻结业务类型的相加
	  	        if ((knbFroz.getFroztp() == E_FROZTP.JUDICIAL)
	  	        		&& knbFroz.getFrlmtp() == E_FRLMTP.AMOUNT ) {
	  	        	KnbFrozDetl knbFrozDetl=KnbFrozDetlDao.selectOne_odb2(knbFroz.getFrozno(), knbFroz.getFrozsq(), false);
	  	        	totalam = totalam.add(knbFrozDetl.getFrozbl());
	  	        }
	  	        // 为续冻业务类型的还需要判断日期范围是否满足，满足才相加
	  	        if (knbFroz.getFroztp() == E_FROZTP.ADD 
	  	        		&& knbFroz.getFrlmtp() == E_FRLMTP.AMOUNT
	  	        		&& CommUtil.Between(trandt, knbFroz.getFrbgdt(), knbFroz.getFreddt())) {
	  	        	KnbFrozDetl knbFrozDetl=KnbFrozDetlDao.selectOne_odb2(knbFroz.getFrozno(), knbFroz.getFrozsq(), false);
	  	        	totalam = totalam.add(knbFrozDetl.getFrozbl());
	  	        }
			}
		}

	}	
	
	/**
	 * 冻结扣划检查并获取扣划限制金额
	 * 
	 * @param deductin totalam
	 */
	public static BigDecimal frozDeduCheck(IoDeductIn deductin,BigDecimal totalam,String trandt,E_FRLMTP frlmtp) {
		
		// 查询冻结信息(取出的可能是冻结信息，也可能是续冻信息)
		List<KnbFroz> lstTblKnbFroz = KnbFrozDao.selectAll_odb5(
				deductin.getFrozno(), E_FROZST.VALID, false);
		if (CommUtil.isNull(lstTblKnbFroz)) {
			// 仅根据冻结编号查询冻结信息，判断冻结编号是否存在
			KnbFroz tblKnbFrozFl = KnbFrozDao.selectOne_odb2(deductin.getFrozno(), false);
			if (CommUtil.isNull(tblKnbFrozFl)) {
				throw DpModuleError.DpstComm.BNAS0739();
			}
			
			if(!CommUtil.equals(deductin.getFrozdt(), tblKnbFrozFl.getFrozdt())){
				throw DpModuleError.DpstComm.BNAS0827();
			}
			
			if (tblKnbFrozFl.getFroztp() != E_FROZTP.ADD 
					&& tblKnbFrozFl.getFroztp() != E_FROZTP.JUDICIAL) {
				throw DpModuleError.DpstComm.BNAS0763();
			}
	    	throw DpModuleError.DpstComm.BNAS0736();
	    }
		
		for(KnbFroz tblKnbFroz : lstTblKnbFroz){
			if(!CommUtil.equals(deductin.getFrozdt(), tblKnbFroz.getFrozdt()) && (E_FROZTP.ADD != tblKnbFroz.getFroztp())){
				throw DpModuleError.DpstComm.BNAS0827();
			}
		}
		
		//frlmtp = lstTblKnbFroz.get(0).getFrlmtp();
		
		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(deductin.getCardno(), false);
		if(CommUtil.isNull(caKnaAcdc)){
			throw DpModuleError.DpstComm.BNAS0692();
		}
		
		String custac = caKnaAcdc.getCustac();
		
	    if (!CommUtil.equals(lstTblKnbFroz.get(0).getCustac(), custac)) {
	    	throw DpModuleError.DpstComm.BNAS0514();
	    }
		if (lstTblKnbFroz.get(0).getFroztp() != E_FROZTP.ADD 
				&& lstTblKnbFroz.get(0).getFroztp() != E_FROZTP.JUDICIAL) {
			throw DpModuleError.DpstComm.BNAS0763();
		}

		//查询存在续冻情况下获取生效的冻结信息
		IofrozInfo frozInfo = DpAcctQryDao.selFrozInfoByFrozno(deductin.getFrozno(), true);
		
		KnbFrozDetl lstKnbFrozDetl=KnbFrozDetlDao.selectOne_odb2(lstTblKnbFroz.get(0).getFrozno(), frozInfo.getFrozsq(), false);
		
		// 如果是金额冻结，需要判断扣划金额是否不大于原冻结（或续冻）金额
		if (lstTblKnbFroz.get(0).getFrlmtp() == E_FRLMTP.AMOUNT) {
		//	for (KnbFroz knbFroz : lstTblKnbFroz) {
			    lstKnbFrozDetl=KnbFrozDetlDao.selectOne_odb2(lstTblKnbFroz.get(0).getFrozno(), frozInfo.getFrozsq(), false);
				if (lstTblKnbFroz.get(0).getFroztp() == E_FROZTP.JUDICIAL
						&& CommUtil.compare(lstKnbFrozDetl.getFrozbl(),deductin.getDeduam()) < 0) {
					throw DpModuleError.DpstComm.BNAS0507();
				}
				if (lstTblKnbFroz.get(0).getFroztp() == E_FROZTP.ADD
						&& CommUtil.compare(lstKnbFrozDetl.getFrozbl(),deductin.getDeduam()) < 0
						&& CommUtil.Between(trandt, lstTblKnbFroz.get(0).getFrbgdt(), lstTblKnbFroz.get(0).getFreddt())) {
					throw DpModuleError.DpstComm.BNAS0507();
				}
			}
		//}
		
	    // 判断其他司法冻结对该扣划的影响
		// 如果该冻结权限为1，则为最大权限，不需要检查其他冻结的影响
		if (lstKnbFrozDetl.getFrsbsq()>1){
			// 根据电子账号查询所有冻结信息
		    // 获取该电子账户冻结权限比该冻结编号大的记录sql改造(包括当前有续冻生效的记录）
	  	    List<IofrozInfo> lstKnbFrozDetl1= DpFrozDao.selFrozDetlByFrsbsq(E_FROZST.VALID, lstKnbFrozDetl.getFrsbsq(), custac, deductin.getFrozno(), false);
	  	   
	  	    if ( !CommUtil.isNull(lstKnbFrozDetl1) ) { //不为空则需要判断类型是否为冻结和续冻
	  	    	
	  	    	for (IofrozInfo knbfrozdetl : lstKnbFrozDetl1) {
	  	    		
	  	    		//获取当前冻结记录
	  	    		KnbFroz knbFroz = KnbFrozDao.selectOne_odb6(knbfrozdetl.getFrozno(), knbfrozdetl.getFrozsq(), E_FROZST.VALID, true);//DpFrozDao.selCurFrozInfoByFrozno(knbfrozdetl.getFrozno(), E_FROZST.VALID, true);
		  	       
	  	    		if ((knbFroz.getFroztp() == E_FROZTP.JUDICIAL || knbFroz.getFroztp() == E_FROZTP.ADD)
		  	        		&& knbFroz.getFrlmtp() != E_FRLMTP.AMOUNT) {
		  	        	throw DpModuleError.DpstComm.BNAS0999();
		  	        }
		  	        
	  	    		//获取当前冻结明细记录
	  	    		KnbFrozDetl KnbFrozDetl = KnbFrozDetlDao.selectOne_odb2(knbFroz.getFrozno(), knbFroz.getFrozsq(), true);
	  	    		// 记录优先权高的冻结金额总和
		  	        // 为冻结业务类型的相加
		  	        if ( (knbFroz.getFroztp() == E_FROZTP.JUDICIAL 
		  	        		|| knbFroz.getFroztp() == E_FROZTP.ADD)
		  	        		&& knbFroz.getFrlmtp() == E_FRLMTP.AMOUNT ) {
		  	        	totalam = totalam.add( KnbFrozDetl.getFrozbl() );
		  	        }
		  	        
	  	    	}
	  	    }
		}
		return totalam;//累加当前冻结编号之前的在先冻结信息(部冻金额之和)
	}
	
	/**
	 * 获取所有账户的账户可用余额
	 * @param custac 电子账户
	 * @param crcycd 币种
	 */
	public static BigDecimal getAllAcctBal(String custac, String crcycd){
		BigDecimal balance = BigDecimal.ZERO;//账户余额
		
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		//List<kna_accs> accss = Kna_accsDao.selectAll_odb8(custac, crcycd, true);
		List<IoCaKnaAccs> accss = caqry.listKnaAccsOdb8(custac, crcycd, false);
		
		for (IoCaKnaAccs accs : accss){
			// 仅有活期的和定期的子账户
			if(E_PRODTP.DEPO == accs.getProdtp()){
				//存款
				if(E_FCFLAG.CURRENT == accs.getFcflag()){
					//查询活期账户
					balance = balance.add(DpAcctProc.getBalance(accs.getAcctno()));
				}else if(E_FCFLAG.FIX == accs.getFcflag()){
					//查询定期账户
					KnaFxac fxac = KnaFxacDao.selectOne_odb1(accs.getAcctno(), true);
					balance = balance.add(fxac.getOnlnbl());
				}else{
					throw DpModuleError.DpstAcct.BNAS1744(accs.getFcflag());
				}
			}
		}
		
		//减掉智能储蓄冻结余额
		balance = balance.subtract(DpFrozTools.getFrozBala(E_FROZOW.AUACCT, custac));
		
		if(CommUtil.compare(balance, BigDecimal.ZERO) <=0){
			balance = BigDecimal.ZERO;
		}
		
		return balance;
	}
	
	/**
	 * 获取账户余额
	 * @param custac 电子账户
	 * @param crcycd 币种
	 */
	public static BigDecimal getAcctBal(String custac, String crcycd){
        BigDecimal balance = BigDecimal.ZERO;//账户余额
		
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		//List<kna_accs> accss = Kna_accsDao.selectAll_odb8(custac, crcycd, true);
		List<IoCaKnaAccs> accss = caqry.listKnaAccsOdb8(custac, crcycd, false);
		
		for (IoCaKnaAccs accs : accss){
			// 仅有活期的和定期的子账户
			if(E_PRODTP.DEPO == accs.getProdtp()){
				//存款
				if(E_FCFLAG.CURRENT == accs.getFcflag()){
					//查询活期账户
					KnaAcctJoint joinacc = KnaAcctJointDao.selectOne_odb1(accs.getAcctno(), false);
					if(CommUtil.isNotNull(joinacc)) {
						continue;
					}
					
					balance = balance.add(DpAcctProc.getBalance(accs.getAcctno()));

				}else if(E_FCFLAG.FIX == accs.getFcflag()){
					//查询定期账户
					KnaFxac fxac = KnaFxacDao.selectOne_odb1(accs.getAcctno(), true);
					balance = balance.add(fxac.getOnlnbl());
				}else{
					throw DpModuleError.DpstAcct.BNAS1744(accs.getFcflag());
				}
			}
		}
		
		return balance;
	}
	
	/**
	 * 获取外部无冻结扣划金额
	 */
	public static BigDecimal getFrozAmt(String custac, E_YES___ isdedu){
		
		BigDecimal totalam = BigDecimal.ZERO;
		
		//获取电子账号所有司法冻结的记录(包括未生效的续冻)
		List<IofrozInfo> lstKnbFrozDetl = DpFrozDao.selAcctFrozInfo(custac, false);
		
		if(CommUtil.isNotNull(lstKnbFrozDetl)){
			
			for (IofrozInfo knbfrozdetl : lstKnbFrozDetl) {
				
				//查询存在续冻情况下获取生效的冻结信息
				//IofrozInfo frozInfo = DpAcctQryDao.selFrozInfoByFrozno(knbfrozdetl.getFrozno(), true);
				//获取当前生效冻结信息
  	    		KnbFroz knbFroz = KnbFrozDao.selectOne_odb6(knbfrozdetl.getFrozno(), knbfrozdetl.getFrozsq(), E_FROZST.VALID, true);
	  	        
  	    		if ((knbFroz.getFroztp() == E_FROZTP.JUDICIAL 
	  	        		|| knbFroz.getFroztp() == E_FROZTP.ADD)
	  	        		&& knbFroz.getFrlmtp() != E_FRLMTP.AMOUNT 
	  	        		&& E_YES___.YES == isdedu) {
	  	        	throw DpModuleError.DpstComm.BNAS0999();
	  	        }
	  	        
  	    		//获取当前生效冻结明细信息
  	    		KnbFrozDetl tblKnbFrozDetl = KnbFrozDetlDao.selectOne_odb2(knbFroz.getFrozno(), knbFroz.getFrozsq(), true);
	  	        //累加当前生效的冻结金额(包括生效的续冻)
	  	        if ((knbFroz.getFroztp() == E_FROZTP.JUDICIAL || 
	  	        		knbFroz.getFroztp() == E_FROZTP.ADD)
	  	        		&& knbFroz.getFrlmtp() == E_FRLMTP.AMOUNT ) {
	  	        	totalam = totalam.add(tblKnbFrozDetl.getFrozbl());
	  	        }
  	    	}
		}
		
		return totalam;
	}
	
	/**
	 * 资金扣划
	 * 
	 * @param deductin
	 */
	public static void moneyDeduct(IoDeductIn deductin) {
		BigDecimal acctbal = deductin.getDeduam();//扣划变化金额,初始值为扣划金额
		BigDecimal bal = null;//结算户可用余额
		String remark ;
		
		if(CommUtil.isNull(deductin.getRemark())){
			remark = "";
			
		}else{
			remark = deductin.getRemark();
		}
		
		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(deductin.getCardno(), false);
		if(CommUtil.isNull(caKnaAcdc)){
			throw DpModuleError.DpstComm.BNAS0692();
		}
		String custac = caKnaAcdc.getCustac();
		
		// 查询电子账户个人活期结算户金额（根据客户账号，子账户区分类型）
		E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac);
		
		//如果是一，二内户从个人结算户扣划出去
		if(E_ACCATP.FINANCE == eAccatp || E_ACCATP.GLOBAL == eAccatp){
			//获取非销户状态的个人结算户
			//KnaAcct acct = KnaAcctDao.selectFirst_odb9(E_ACSETP.SA, custac, true);
			KnaAcct acct = DpFrozDao.selKnaAcctInfoByCustac(custac, E_ACSETP.SA, true);
			
/*			// 获取转存签约明细信息
			IoCaKnaSignDetl cplkna_sign_detl = SysUtil.getInstance(IoCaSevQryTableInfo.class)
					.kna_sign_detl_selectFirst_odb2(acct.getAcctno(), E_SIGNST.QY, false);
			
			// 存在转存签约明细信息则取资金池可用余额
			if (CommUtil.isNotNull(cplkna_sign_detl)) {
				bal = DpAcctProc.getProductBal(custac, acct.getCrcycd(), E_YES___.NO, false);
			}else{
				// 其他取账户余额
				bal = DpAcctProc.getAcctOnlnbl(acct.getAcctno(), false);
			}*/
			
			// 可用余额 add by xiongzhao 2016/12/22
			bal = SysUtil.getInstance(DpAcctSvcType.class)
					.getAcctaAvaBal(custac, acct.getAcctno(),
							acct.getCrcycd(), E_YES___.NO, E_YES___.NO);
			
			// 将其他子账户的金额按活期账户，亲情钱包，定期账户顺序取出到个人活期结算户，当同一类型有多个时按由近及远的规则转出
			if (CommUtil.compare(bal, acctbal) < 0) {//结算户可用余额小于扣划金额
				acctbal = acctbal.subtract(bal);

				// 查询KnaAcct表的非个人活期结算户种类的账户信息,根据开户日期排序，开户日期最大的排前面
				//钱包账户
				List<KnaAcct> lstKnaAcct = DpAcctQryDao.selKnaAcctZNhq(custac, E_ACSETP.MA, false);
				
				if(CommUtil.isNotNull(lstKnaAcct)){
					// 活期账户转出
					for (KnaAcct KnaAcct : lstKnaAcct) {
						//判断当前扣划金额是否为0
						if (CommUtil.compare(acctbal, BigDecimal.ZERO) == 0) break;
						
						if (CommUtil.compare(KnaAcct.getOnlnbl(), BigDecimal.ZERO) > 0 && CommUtil.compare(KnaAcct.getOnlnbl(), acctbal) <= 0) { // 该账户需要全额转入个人活期结算户
							// 调用支取记账处理,支取金额 KnaAcct.getOnlnbl()
							DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
							cplDrawAcctIn.setCustac(KnaAcct.getCustac());
							cplDrawAcctIn.setTranam(KnaAcct.getOnlnbl());
							cplDrawAcctIn.setCrcycd(KnaAcct.getCrcycd());
							cplDrawAcctIn.setAcctno(KnaAcct.getAcctno());
							cplDrawAcctIn.setOpacna(acct.getAcctna());
							cplDrawAcctIn.setToacct(acct.getAcctno());
							cplDrawAcctIn.setIschck(E_YES___.NO);
							cplDrawAcctIn.setIsdedu(E_YES___.YES);
							cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
							cplDrawAcctIn.setRemark("扣划" + remark);
							DrawDpAcctOut dpAcctOut = SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
							
							// 调用存入记账处理 ,存入个人活期结算户
							SaveDpAcctIn cplSaveAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
					        cplSaveAcctIn.setCustac(acct.getCustac());
					        cplSaveAcctIn.setTranam(KnaAcct.getOnlnbl());
					        cplSaveAcctIn.setCrcycd(acct.getCrcycd());
					        cplSaveAcctIn.setAcctno(acct.getAcctno());
					        cplSaveAcctIn.setOpacna(KnaAcct.getAcctna());
					        cplSaveAcctIn.setToacct(KnaAcct.getAcctno());
					        cplSaveAcctIn.setIschck(E_YES___.NO);
					        cplSaveAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
					        cplSaveAcctIn.setRemark("扣划" + remark);
							SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn);
							
							acctbal = acctbal.subtract(KnaAcct.getOnlnbl());
							
						
						}else if (CommUtil.compare(KnaAcct.getOnlnbl(), acctbal) > 0){//该账户部分转入个人活期结算户
							// 调用支取记账处理,支取金额 acctbal
							DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
							cplDrawAcctIn.setCustac(KnaAcct.getCustac());
							cplDrawAcctIn.setTranam(acctbal);
							cplDrawAcctIn.setCrcycd(KnaAcct.getCrcycd());
							cplDrawAcctIn.setAcctno(KnaAcct.getAcctno());
							cplDrawAcctIn.setOpacna(acct.getAcctna());
							cplDrawAcctIn.setToacct(acct.getAcctno());
							cplDrawAcctIn.setIschck(E_YES___.NO);
							cplDrawAcctIn.setIsdedu(E_YES___.YES);
							cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
							cplDrawAcctIn.setRemark("扣划" + remark);
							DrawDpAcctOut dpAcctOut = SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
							
							// 调用存入记账处理 ,存入个人活期结算户
							SaveDpAcctIn cplSaveAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
					        cplSaveAcctIn.setCustac(acct.getCustac());
					        cplSaveAcctIn.setTranam(acctbal);
					        cplSaveAcctIn.setCrcycd(acct.getCrcycd());
					        cplSaveAcctIn.setAcctno(acct.getAcctno());
					        cplSaveAcctIn.setOpacna(KnaAcct.getAcctna());
					        cplSaveAcctIn.setToacct(KnaAcct.getAcctno());
					        cplSaveAcctIn.setIschck(E_YES___.NO);
					        cplSaveAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
					        cplSaveAcctIn.setRemark("扣划" + remark);
							SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn);
							
							acctbal = acctbal.subtract(acctbal);
						}
					}
				}

				// 查询KnaAcct表的非个人活期结算户种类的账户信息,根据开户日期排序，开户日期最大的排前面
				//亲情钱包
				List<KnaAcct> lstKnaAcctone = DpAcctQryDao.selKnaAcctZNhq(custac, E_ACSETP.FW, false);
				
				if(CommUtil.isNotNull(lstKnaAcctone)){
					// 活期账户转出
					for (KnaAcct KnaAcct : lstKnaAcctone) {
						//判断当前扣划金额是否为0
						if (CommUtil.compare(acctbal, BigDecimal.ZERO) == 0) break;
						
						if (CommUtil.compare(KnaAcct.getOnlnbl(), BigDecimal.ZERO) > 0 && CommUtil.compare(KnaAcct.getOnlnbl(), acctbal) <= 0) { // 该账户需要全额转入个人活期结算户
							// 调用支取记账处理,支取金额 KnaAcct.getOnlnbl()
							DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
							cplDrawAcctIn.setCustac(KnaAcct.getCustac());
							cplDrawAcctIn.setTranam(KnaAcct.getOnlnbl());
							cplDrawAcctIn.setCrcycd(KnaAcct.getCrcycd());
							cplDrawAcctIn.setAcctno(KnaAcct.getAcctno());
							cplDrawAcctIn.setOpacna(acct.getAcctna());
							cplDrawAcctIn.setToacct(acct.getAcctno());
							cplDrawAcctIn.setIschck(E_YES___.NO);
							cplDrawAcctIn.setIsdedu(E_YES___.YES);
							cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
							cplDrawAcctIn.setRemark("扣划" + remark);
							DrawDpAcctOut dpAcctOut = SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
							
							// 调用存入记账处理 ,存入个人活期结算户
							SaveDpAcctIn cplSaveAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
					        cplSaveAcctIn.setCustac(acct.getCustac());
					        cplSaveAcctIn.setTranam(KnaAcct.getOnlnbl());
					        cplSaveAcctIn.setCrcycd(acct.getCrcycd());
					        cplSaveAcctIn.setAcctno(acct.getAcctno());
					        cplSaveAcctIn.setOpacna(KnaAcct.getAcctna());
					        cplSaveAcctIn.setToacct(KnaAcct.getAcctno());
					        cplSaveAcctIn.setIschck(E_YES___.NO);
					        cplSaveAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
					        cplSaveAcctIn.setRemark("扣划" + remark);
					        
							SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn);
							
							acctbal = acctbal.subtract(KnaAcct.getOnlnbl());
							
						
						}else if (CommUtil.compare(KnaAcct.getOnlnbl(), acctbal) > 0){//该账户部分转入个人活期结算户
							// 调用支取记账处理,支取金额 acctbal
							DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
							cplDrawAcctIn.setCustac(KnaAcct.getCustac());
							cplDrawAcctIn.setTranam(acctbal);
							cplDrawAcctIn.setCrcycd(KnaAcct.getCrcycd());
							cplDrawAcctIn.setAcctno(KnaAcct.getAcctno());
							cplDrawAcctIn.setOpacna(acct.getAcctna());
							cplDrawAcctIn.setIschck(E_YES___.NO);
							cplDrawAcctIn.setIsdedu(E_YES___.YES);
							cplDrawAcctIn.setToacct(acct.getAcctno());
							cplDrawAcctIn.setIsdedu(E_YES___.YES);
							cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
							cplDrawAcctIn.setRemark("扣划" + remark);
							DrawDpAcctOut dpAcctOut = SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
							
							// 调用存入记账处理 ,存入个人活期结算户
							SaveDpAcctIn cplSaveAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
					        cplSaveAcctIn.setCustac(acct.getCustac());
					        cplSaveAcctIn.setTranam(acctbal);
					        cplSaveAcctIn.setCrcycd(acct.getCrcycd());
					        cplSaveAcctIn.setAcctno(acct.getAcctno());
					        cplSaveAcctIn.setOpacna(KnaAcct.getAcctna());
					        cplSaveAcctIn.setToacct(KnaAcct.getAcctno());
					        cplSaveAcctIn.setIschck(E_YES___.NO);
					        cplSaveAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
					        cplSaveAcctIn.setRemark("扣划" + remark);
					        
							SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn);
							
							acctbal = acctbal.subtract(acctbal);
							
						}
					}
				}
				
				// 定期账户转出
				if (CommUtil.compare(acctbal, BigDecimal.ZERO) > 0) {
					// 根据查询KnaFxac表的定期账户信息,根据开户日期排序，开户日期最大的排前面
					// kna_fxsv表  detifg字段，为是的是智能储蓄，为否的是定期，先扣智能储蓄的
					List<KnaFxac> lstKnaFxac = DpAcctQryDao.selKnaFxacByCustacOrderDetlfg(custac,E_DPACST.CLOSE,false);
					
					if(CommUtil.isNotNull(lstKnaFxac)){
					
						for (KnaFxac knafxac : lstKnaFxac) {
							//判断剩余扣划金额是否为0
							if (CommUtil.compare(acctbal, BigDecimal.ZERO) == 0) break;
							
							//签约为个人活期结算户部分，已做扣划，不在定期中重复计算
							// 获取转存签约明细信息
							IoCaKnaSignDetl cplkna_sign_detlone = SysUtil.getInstance(IoCaSevQryTableInfo.class)
									.getKnaSignDetlFirstOdb3(acct.getAcctno(), knafxac.getAcctno(), E_SIGNST.QY, false);//(knafxac.getAcctno(), E_SIGNST.QY, false);
							
							if(CommUtil.isNotNull(cplkna_sign_detlone)){
								continue;
							}
							if (CommUtil.compare(knafxac.getOnlnbl(), BigDecimal.ZERO) > 0 && CommUtil.compare(knafxac.getOnlnbl(), acctbal) <= 0) { // 该账户需要全额转入个人活期结算户
								// 调用支取记账处理,支取金额 knafxac.getOnlnbl()
								DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
								cplDrawAcctIn.setCustac(knafxac.getCustac());
								cplDrawAcctIn.setTranam(knafxac.getOnlnbl());
								cplDrawAcctIn.setCrcycd(knafxac.getCrcycd());
								cplDrawAcctIn.setAcctno(knafxac.getAcctno());
								cplDrawAcctIn.setOpacna(acct.getAcctna());
								cplDrawAcctIn.setToacct(acct.getAcctno());
								cplDrawAcctIn.setAuacfg(E_YES___.NO);
								cplDrawAcctIn.setIschck(E_YES___.NO);
								cplDrawAcctIn.setIsdedu(E_YES___.YES); //扣划标志
								cplDrawAcctIn.setDedutp(deductin.getDedutp()); //扣划类型
								cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
								cplDrawAcctIn.setRemark("扣划" + remark);
								
								DrawDpAcctOut dpAcctOut = SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
								
								log.debug("扣划定期利息为：" + dpAcctOut.getInstam() +"--------------------" );
								
								//若定期支取利息为空，则给初始化为0
						        if(CommUtil.isNull(dpAcctOut.getInstam())){
						        	dpAcctOut.setInstam(BigDecimal.ZERO);
						        }
								
								// 调用存入记账处理 ,存入个人活期结算户
								SaveDpAcctIn cplSaveAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
						        cplSaveAcctIn.setCustac(acct.getCustac());
						        cplSaveAcctIn.setTranam(knafxac.getOnlnbl().add(dpAcctOut.getInstam()));
						        cplSaveAcctIn.setCrcycd(acct.getCrcycd());
						        cplSaveAcctIn.setAcctno(acct.getAcctno());
						        cplSaveAcctIn.setOpacna(knafxac.getAcctna());
						        cplSaveAcctIn.setToacct(knafxac.getAcctno());
						        cplSaveAcctIn.setIschck(E_YES___.NO);
						        cplSaveAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
						        cplSaveAcctIn.setRemark("扣划" + remark);
						        
								SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn);
								
								//支取利息税
								if(CommUtil.isNotNull(dpAcctOut.getIntxam()) &&
										CommUtil.compare(dpAcctOut.getIntxam(), BigDecimal.ZERO) > 0){
									DrawDpAcctIn drawAcctOut = SysUtil.getInstance(DrawDpAcctIn.class);
									drawAcctOut.setCustac(acct.getCustac());//电子账号
									drawAcctOut.setTranam(dpAcctOut.getIntxam());//利息税
									drawAcctOut.setCrcycd(acct.getCrcycd());
									drawAcctOut.setAcctno(acct.getAcctno());
									drawAcctOut.setIschck(E_YES___.NO);
									drawAcctOut.setIsdedu(E_YES___.YES);
									drawAcctOut.setSmrycd(BusinessConstants.SUMMARY_KH);
									drawAcctOut.setRemark("扣划" + remark);
									
									SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawAcctOut);
								}
								
								acctbal = acctbal.subtract(knafxac.getOnlnbl());
								
							}else if (CommUtil.compare(knafxac.getOnlnbl(), acctbal) > 0){//该账户部分转入个人活期结算户
								// 调用支取记账处理,支取金额 acctbal
								DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
								cplDrawAcctIn.setCustac(knafxac.getCustac());
								//判断定期产品是否可以部分提前支取，如果不允许，将定期全部提前支取到结算子账户
								KnbDfir tblKnbDfir = KnbDfirDao.selectOne_odb1(knafxac.getAcctno(), E_TEARTP.TQZQ, false);
								if (CommUtil.isNull(tblKnbDfir)) {
									acctbal = knafxac.getOnlnbl();  //扣划变化金额改为定期全部金额
								}
								cplDrawAcctIn.setTranam(acctbal);
								cplDrawAcctIn.setCrcycd(knafxac.getCrcycd());
								cplDrawAcctIn.setAcctno(knafxac.getAcctno());
								cplDrawAcctIn.setOpacna(acct.getAcctna());
								cplDrawAcctIn.setToacct(acct.getAcctno());
								cplDrawAcctIn.setAuacfg(E_YES___.NO);
								cplDrawAcctIn.setIschck(E_YES___.NO);
								cplDrawAcctIn.setIsdedu(E_YES___.YES); //扣划标志
								cplDrawAcctIn.setDedutp(deductin.getDedutp()); //扣划类型
								cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
								cplDrawAcctIn.setRemark("扣划" + remark);
								
								DrawDpAcctOut dpAcctOut = SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
								
								log.debug("扣划定期利息为：" + dpAcctOut.getInstam() +"+++++++++++++++++++++++++++" );
								
								//若定期支取利息为空，则给初始化为0
						        if(CommUtil.isNull(dpAcctOut.getInstam())){
						        	dpAcctOut.setInstam(BigDecimal.ZERO);
						        }
								
								// 调用存入记账处理 ,存入个人活期结算户
								SaveDpAcctIn cplSaveAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
						        cplSaveAcctIn.setCustac(acct.getCustac());
						        cplSaveAcctIn.setTranam(acctbal.add(dpAcctOut.getInstam()));
						        cplSaveAcctIn.setCrcycd(acct.getCrcycd());
						        cplSaveAcctIn.setAcctno(acct.getAcctno());
						        cplSaveAcctIn.setOpacna(knafxac.getAcctna());
						        cplSaveAcctIn.setToacct(knafxac.getAcctno());
						        cplSaveAcctIn.setIschck(E_YES___.NO);
						        cplSaveAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
						        cplSaveAcctIn.setRemark("扣划" + remark);
						        
								SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn);
								
								
								//支取利息税
								if(CommUtil.isNotNull(dpAcctOut.getIntxam()) &&
										CommUtil.compare(dpAcctOut.getIntxam(), BigDecimal.ZERO) > 0){
									DrawDpAcctIn drawAcctOut = SysUtil.getInstance(DrawDpAcctIn.class);
									drawAcctOut.setCustac(acct.getCustac());//电子账号
									drawAcctOut.setTranam(dpAcctOut.getIntxam());//利息税
									drawAcctOut.setCrcycd(acct.getCrcycd());
									drawAcctOut.setAcctno(acct.getAcctno());
									drawAcctOut.setIschck(E_YES___.NO);
									drawAcctOut.setIsdedu(E_YES___.YES);
									drawAcctOut.setSmrycd(BusinessConstants.SUMMARY_KH);
									drawAcctOut.setRemark("扣划" + remark);
									
									SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawAcctOut);
								}
								
								acctbal = acctbal.subtract(acctbal); //扣划变化金额改为0
								
							}
						}
					}
				}
				
			}
			
			// 电子账户个人活期结算户出金
			DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
			cplDrawAcctIn.setCustac(acct.getCustac());
			cplDrawAcctIn.setTranam(deductin.getDeduam());
			cplDrawAcctIn.setCrcycd(acct.getCrcycd());
			cplDrawAcctIn.setAcctno(acct.getAcctno());
			cplDrawAcctIn.setOpacna(deductin.getOthena());
			cplDrawAcctIn.setToacct(deductin.getOtheac());
			cplDrawAcctIn.setIschck(E_YES___.NO);
			cplDrawAcctIn.setIsdedu(E_YES___.YES);
			cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
			cplDrawAcctIn.setRemark("扣划" + remark);
			
			SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
			
			KnpParameter tbl_KnpParameter = KnpParameterDao.selectOne_odb1("DPTRAN", "DEDUCT", "%","%", true);
			String acctno =null;
			// 电子账户个人结算户入往来内部户

			if(E_MONYTO.TOINNACC != deductin.getMonyto()){
				IaAcdrInfo acdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
				acdrInfo.setBusino(tbl_KnpParameter.getParm_value1());
				acdrInfo.setSubsac(tbl_KnpParameter.getParm_value2());
				acdrInfo.setCrcycd(acct.getCrcycd());
				acdrInfo.setToacct(acct.getAcctno());
				acdrInfo.setToacna(acct.getAcctna());
				acdrInfo.setTranam(deductin.getDeduam());
				acdrInfo.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
				acdrInfo.setSmrycd(BusinessConstants.SUMMARY_KH);
				acdrInfo.setDscrtx("扣划" + remark);
				IaTransOutPro out = SysUtil.getInstance(IoInAccount.class).ioInAccr(acdrInfo);
				acctno = out.getAcctno();
			}
			else {
				IoCaKnaAcdc caKnaAcdc2 = caSevQryTableInfo.getKnaAcdcOdb2(deductin.getOtheac(), false);
				KnaAcct acct2 = DpFrozDao.selKnaAcctInfoByCustac(caKnaAcdc2.getCustac(), E_ACSETP.SA, true);
				SaveDpAcctIn cplSaveAcctIn2 = SysUtil.getInstance(SaveDpAcctIn.class);
		        cplSaveAcctIn2.setCustac(acct2.getCustac());
		        cplSaveAcctIn2.setTranam(deductin.getDeduam());
		        cplSaveAcctIn2.setCrcycd(acct2.getCrcycd());
		        cplSaveAcctIn2.setAcctno(acct2.getAcctno());
		        cplSaveAcctIn2.setOpacna(acct2.getAcctna());
		        cplSaveAcctIn2.setToacct(acct2.getAcctno());
		        cplSaveAcctIn2.setIschck(E_YES___.NO);
		        cplSaveAcctIn2.setSmrycd(BusinessConstants.SUMMARY_KH);
		        cplSaveAcctIn2.setRemark("扣划" + remark);
		        
				SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn2);
				acctno = acct2.getAcctno();
				
			}

			//登记出入金登记簿
			IoSaveIoTransBill.SaveIoBill.InputSetter billInfo =SysUtil.getInstance(IoSaveIoTransBill.SaveIoBill.InputSetter.class);
			
			billInfo.setAcctnm(deductin.getCustna());//客户名称
			billInfo.setBrchno(BusiTools.getBusiRunEnvs().getCentbr());//内部户记账机构
			billInfo.setBusino(tbl_KnpParameter.getParm_value1());//内部户业务代码
			billInfo.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq());//业务跟踪号
			billInfo.setCapitp(E_CAPITP.DP996);//扣划
			billInfo.setCardno(acctno);//内部户账号
			billInfo.setCrcycd(BusiTools.getDefineCurrency());//币种
			billInfo.setFrondt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
			billInfo.setFronsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
			billInfo.setIoflag(E_IOFLAG.OUT);//出入金标识
			billInfo.setKeepdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期
			billInfo.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());//内部交易码
			billInfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id().toString());//交易渠道
			billInfo.setToacct(deductin.getCardno());//电子账号
			billInfo.setToacno(acct.getAcctno());//个人结算主账户
			billInfo.setTobrch(acct.getBrchno());//电子账户开户机构
			billInfo.setTranam(deductin.getDeduam());//交易金额
			billInfo.setToscac(deductin.getCardno());//电子账号
			billInfo.setTranst(E_TRANST.NORMAL);//交易状态
			
			SysUtil.getInstance(IoSaveIoTransBill.class).saveIoBill(billInfo);
		
		//若为三类户，则从钱包账户
		}else if(E_ACCATP.WALLET == eAccatp){
			//钱包账户
			List<KnaAcct> lstKnaAcct = DpAcctQryDao.selKnaAcctZNhq(custac, E_ACSETP.MA, false);
			/**/
			
			IoCaKnaAcdc caKnaAcdc2 = caSevQryTableInfo.getKnaAcdcOdb2(deductin.getOtheac(), false);
			KnaAcct acct2 = DpFrozDao.selKnaAcctInfoByCustac(caKnaAcdc2.getCustac(), E_ACSETP.SA, true);
			KnpParameter tbl_KnpParameter = KnpParameterDao.selectOne_odb1("DPTRAN", "DEDUCT", "%","%", true);
			if(CommUtil.isNotNull(lstKnaAcct)){
				for(KnaAcct KnaAcct : lstKnaAcct){
					// 电子账户钱包账户出金
					DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
					cplDrawAcctIn.setCustac(KnaAcct.getCustac());
					cplDrawAcctIn.setTranam(deductin.getDeduam());
					cplDrawAcctIn.setCrcycd(KnaAcct.getCrcycd());
					cplDrawAcctIn.setAcctno(KnaAcct.getAcctno());
					cplDrawAcctIn.setOpacna(deductin.getOthena());
					cplDrawAcctIn.setToacct(deductin.getOtheac());
					cplDrawAcctIn.setIschck(E_YES___.NO);
					cplDrawAcctIn.setIsdedu(E_YES___.YES);
					cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
					cplDrawAcctIn.setRemark("扣划" + remark);
					
					SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);

					String acctno2 =null;
					// 电子账户个人结算户入往来内部户
					if(E_MONYTO.TOINNACC != deductin.getMonyto()){
						// 电子账户钱包户入往来内部户
						IaAcdrInfo acdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
						acdrInfo.setBusino(tbl_KnpParameter.getParm_value1());
						acdrInfo.setSubsac(tbl_KnpParameter.getParm_value2());
						acdrInfo.setCrcycd(KnaAcct.getCrcycd());
						acdrInfo.setToacct(KnaAcct.getAcctno());
						acdrInfo.setToacna(KnaAcct.getAcctna());
						acdrInfo.setTranam(deductin.getDeduam());
						acdrInfo.setAcbrch(BusiTools.getBusiRunEnvs().getCentbr());
						acdrInfo.setSmrycd(BusinessConstants.SUMMARY_KH);
						acdrInfo.setDscrtx("扣划" + remark);
						IaTransOutPro out = SysUtil.getInstance(IoInAccount.class).ioInAccr(acdrInfo);
						acctno2 =out.getAcctno();
					}
					else {
						SaveDpAcctIn cplSaveAcctIn2 = SysUtil.getInstance(SaveDpAcctIn.class);
				        cplSaveAcctIn2.setCustac(acct2.getCustac());
				        cplSaveAcctIn2.setTranam(deductin.getDeduam());
				        cplSaveAcctIn2.setCrcycd(acct2.getCrcycd());
				        cplSaveAcctIn2.setAcctno(acct2.getAcctno());
				        cplSaveAcctIn2.setOpacna(acct2.getAcctna());
				        cplSaveAcctIn2.setToacct(acct2.getAcctno());
				        cplSaveAcctIn2.setIschck(E_YES___.NO);
				        cplSaveAcctIn2.setSmrycd(BusinessConstants.SUMMARY_KH);
				        cplSaveAcctIn2.setRemark("扣划" + remark);
				        
						SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn2);
						acctno2 = acct2.getAcctno();
					}

					
					//登记出入金登记簿
					IoSaveIoTransBill.SaveIoBill.InputSetter billInfo = SysUtil.getInstance(IoSaveIoTransBill.SaveIoBill.InputSetter.class);
					
					billInfo.setAcctnm(deductin.getCustna());//客户名称
					billInfo.setBrchno(BusiTools.getBusiRunEnvs().getCentbr());//内部户记账机构
					billInfo.setBusino(tbl_KnpParameter.getParm_value1());//内部户业务代码
					billInfo.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq());//业务跟踪号
					billInfo.setCapitp(E_CAPITP.DP996);//扣划
					billInfo.setCardno(acctno2);//内部户账号
					billInfo.setCrcycd(BusiTools.getDefineCurrency());//币种
					billInfo.setFrondt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
					billInfo.setFronsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
					billInfo.setIoflag(E_IOFLAG.OUT);//出入金标识
					billInfo.setKeepdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期
					billInfo.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());//内部交易码
					billInfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id().toString());//交易渠道
					billInfo.setToacct(deductin.getCardno());//电子账号
					billInfo.setToacno(KnaAcct.getAcctno());//个人结算主账户
					billInfo.setTobrch(KnaAcct.getBrchno());//电子账户开户机构
					billInfo.setTranam(deductin.getDeduam());//交易金额
					billInfo.setToscac(deductin.getCardno());//电子账号
					billInfo.setTranst(E_TRANST.NORMAL);//交易状态
					
					SysUtil.getInstance(IoSaveIoTransBill.class).saveIoBill(billInfo);
				}
			}
		}
	}
	
	private static String genDeduno() {
		return BusiTools.getSequence("deduno", 10, "0");
	}
	
	private static Boolean checkDrawFxac(String acctno, BigDecimal tranam) {
		
		Boolean isDrawAll = true; // 全额支取
		Boolean isDrawPart = false; // 全额支取
		
		//金额控制方式
		E_AMNTWY dramwy = null;
		//最小金额
		BigDecimal drmiam = BigDecimal.ZERO;
		//最大金额
		BigDecimal drmxam = BigDecimal.ZERO;
		//次数控制方式
		E_TIMEWY drtmwy = null;
		//实际支取次数
		Long count = 0l;
		//最小支取次数
		Long minitm = 0l;
		//最大支取次数
		Long maxitm = 0l;
		E_DRAWCT posttp = null;//支取控制方式		
		E_CTRLWY postwy = null;//支取控制方法
		E_YES___ setpwy = null;//是否设置支取计划
		E_DWBKLI drdfsd = null;//支取违约标准
		E_DWBKDL drdfwy = null;//违约处理方式
		E_SVPLFG drcrwy = null;//支取计划完成方式
		BigDecimal minibl = BigDecimal.ZERO; //最低留存余额
		E_YES___ ismibl = null; //是否允许小于最低留存余额
		E_YES___ dpbkfg=null;
		
		KnaFxdr tblKnaFxdr = null;//负债定期账户支取控制
		KnaFxdrPlan tblKnaFxdrPlan =null;//债账户支取计划表
		KnaFxac tblKnafxac = null;// 定期负债账户表
		// 获取负债定期账户支取控制信息
		log.debug("负债账号=============" + acctno);
		tblKnaFxdr = KnaFxdrDao.selectOne_odb1(acctno, false);
		if(CommUtil.isNull(tblKnaFxdr)){
			return isDrawAll; // 没有支取控制信息,全额支取
		}
		
		dramwy = tblKnaFxdr.getDramwy();//支取金额控制方式
		drmiam = tblKnaFxdr.getDrmiam();//单次支取最小金额
		drmxam = tblKnaFxdr.getDrmxam();//单次支取最大金额
		drtmwy = tblKnaFxdr.getDrtmwy();//支取次数控制方式
		count  = tblKnaFxdr.getRedwnm()+1;//实际支取次数
		minitm = tblKnaFxdr.getDrmitm();//最小支取次数
		maxitm = tblKnaFxdr.getDrmxtm();//最大支取次数
		posttp = tblKnaFxdr.getDrawtp();//支取控制方式
		postwy = tblKnaFxdr.getCtrlwy();//支取控制方法
		drdfsd = tblKnaFxdr.getDrdfsd();//支取违约标准
		drdfwy = tblKnaFxdr.getDrdfwy();//违约支取处理方式
		drcrwy = tblKnaFxdr.getDrcrwy();//支取计划控制完成方式			
		setpwy = tblKnaFxdr.getSetpwy();//是否设置支取计划
		ismibl = tblKnaFxdr.getIsmamt(); //是否允许小于最低留存余额
		dpbkfg = tblKnaFxdr.getDpbkfg();//是否违约标志 
		
		if(E_YES___.YES==setpwy){//如果设置了支取计划，则按计划控制
			Long minSeqnum = DpSaveDrawDao.selKnaFxdrPlanMinSeqno(CommTools.getBaseRunEnvs().getBusi_org_id(), acctno, true);
			tblKnaFxdrPlan=  KnaFxdrPlanDao.selectOne_odb1(acctno, minSeqnum, true);
			drmiam = tblKnaFxdrPlan.getDrmiam();//单次支取最小金额
			drmxam = tblKnaFxdrPlan.getDrmxam();//单次支取最大金额
		}
		

		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		tblKnafxac = KnaFxacDao.selectOne_odb1(acctno, true); //查询定期账户表
		
		minibl = tblKnafxac.getHdmimy(); //最低留存余额
		
		//判断是否到期后支取或者是否全部支取，如果是的话，则不检查控制信息
		if ((CommUtil.isNull(tblKnafxac.getMatudt())
				|| (CommUtil.compare(tblKnafxac.getMatudt(), trandt) > 0))
				&& CommUtil.compare(tranam, tblKnafxac.getOnlnbl()) != 0) {

			// 判断最低留存余额,支取后账户小于最低留存余额，不允许支取,暂时只判断联机交易
			if(SysUtil.getCurrentSystemType() == SystemType.onl){
				
				if(E_YES___.YES != ismibl){
					//支取后余额
					BigDecimal bigacctbl = tblKnafxac.getOnlnbl().subtract(tranam);
					if(CommUtil.compare(bigacctbl, BigDecimal.ZERO) > 0 
							&& CommUtil.compare(bigacctbl, minibl) < 0){
						return isDrawAll;
					}
				}
			}
		}
		
		
		if(E_DRAWCT.YES == posttp){
			return isDrawPart;
		}else if(E_DRAWCT.COND == posttp){
							
			//判断是否到期后支取或者是否全部支取，如果是的话，则不检查控制信息
			if ((CommUtil.isNull(tblKnafxac.getMatudt())
					|| (CommUtil.compare(tblKnafxac.getMatudt(), trandt) > 0))
					&& CommUtil.compare(tranam, tblKnafxac.getOnlnbl()) != 0) {
	
				//如果违约后拒绝支取，则抛出异常
				if(E_DWBKDL.REFUSE == drdfwy && dpbkfg == E_YES___.YES){
					return isDrawAll;
				}
				
				if(postwy == E_CTRLWY.AMCL){ //金额控制  
					//===================金额控制开始================================
					//支取金额检查
					//控制方式为最小金额
					if(E_AMNTWY.MNAC == dramwy){
						//交易金额小于最小支取金额
						if(CommUtil.compare(tranam,drmiam)<0){ // 低于单次最小支取金额
							return isDrawAll;
						}
					}else if(E_AMNTWY.MXAC == dramwy){
						//交易金额大于最大支取金额
						if(CommUtil.compare(tranam,drmxam)>0){
							return isDrawAll;
						}
					}else if(E_AMNTWY.SCAC == dramwy){
						//交易金额不在最小支取金额和最大支取金额之间
						if(CommUtil.compare(tranam,drmiam) < 0 || CommUtil.compare(tranam,drmxam) > 0){
							return isDrawAll;
						}
					}else{
						return isDrawAll;
					}
					//===================金额控制结束================================
				}else if(postwy == E_CTRLWY.TMCL){ //次数控制
					//===================次数控制开始===========================
					//支取次数检查
					//支取方式为最小支取次数
					if(E_TIMEWY.MNTM == drtmwy){
						//支取次数小于最小支取次数
						if(count < minitm){
							return isDrawAll;
						}
					//支取方式为最大次数
					}else if(E_TIMEWY.MXTM == drtmwy){
						//支取次数大于最大支取次数
						if(count > maxitm){
						    /*
						     * 2018/03/02，ouyt
						     * 根据业务修改提示语为：产品如果不允许提前支取，抛错只允许一次性全额支取，如果累计次数达到最大支取次数，抛错已累计多少次，先只能全额支取
						     * */
							if(maxitm == 0){
								return isDrawAll;
							}
							if(maxitm >0){
								Long redwnm = count -1; //累计支取次数
								return isDrawAll;
							}
						}
					//支取方式为最小与最大次数之间
					}else if(E_TIMEWY.SCTM == drtmwy){
						if(count < minitm || count > maxitm){
							return isDrawAll;
						}
					}else{
						return isDrawAll; 
					}
					//===================次数控制结束===========================
				}else if(postwy == E_CTRLWY.ATML){ //金额和次数控制
					
					//===================金额控制开始================================
					//支取金额检查
					//控制方式为最小金额
					if(E_AMNTWY.MNAC == dramwy){
						//交易金额小于最小支取金额
						if(CommUtil.compare(tranam,drmiam)<0){ // 低于单次最小支取金额
							return isDrawAll;
						}
					}else if(E_AMNTWY.MXAC == dramwy){
						//交易金额大于最大支取金额
						if(CommUtil.compare(tranam,drmxam)>0){
							return isDrawAll;
						}
					}else if(E_AMNTWY.SCAC == dramwy){
						//交易金额不在最小支取金额和最大支取金额之间
						if(CommUtil.compare(tranam,drmiam) < 0 || CommUtil.compare(tranam,drmxam) > 0){
							return isDrawAll;
						}
					}else{
						return isDrawAll;
					}
					//===================金额控制结束================================
					//===================次数控制开始===========================
					//支取次数检查
					//支取方式为最小支取次数
					if(E_TIMEWY.MNTM == drtmwy){
						//支取次数小于最小支取次数
						if(count < minitm){
							return isDrawAll;
						}
					//支取方式为最大次数
					}else if(E_TIMEWY.MXTM == drtmwy){
						//支取次数大于最大支取次数
						if(count > maxitm){
						    /*
						     * 2018/03/02，ouyt
						     * 根据业务修改提示语为：产品如果不允许提前支取，抛错只允许一次性全额支取，如果累计次数达到最大支取次数，抛错已累计多少次，先只能全额支取
						     * */
							if(maxitm == 0){
								return isDrawAll;
							}
							if(maxitm >0){
								Long redwnm = count -1; //累计支取次数
								return isDrawAll;
							}
						}
					//支取方式为最小与最大次数之间
					}else if(E_TIMEWY.SCTM == drtmwy){
						if(count < minitm || count > maxitm){
							return isDrawAll;
						}
					}else{
						return isDrawAll; 
					}
					//===================次数控制结束===========================
					
				}else{
					return isDrawAll;
				}
			}
		}								
		return isDrawAll;
	}
	
	/**
	 * 扣划
	 * 
	 * @param deductin
	 */
	public static cn.sunline.ltts.busi.dp.type.DpAcctType.DeductOut deductPrc(IoDeductIn deductin) {
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		E_FRLMTP frlmtp = null;
		
		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		
		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(deductin.getCardno(), false);
		
		if(CommUtil.isNull(caKnaAcdc)){
			throw DpModuleError.DpstComm.BNAS0692();
		}
		
//		if(caKnaAcdc.getStatus() != E_DPACST.NORMAL){
//			throw DpModuleError.DpstComm.E9999("根据电子账号获取电子账号ID状态不正常");
//		}
		
		String custac = caKnaAcdc.getCustac();
		
		
		//判断账号状态
		//账户状态为预开户、转久悬、预销户、销户的，交易拒绝，报错：“电子账户状态为***状态，无法操作！”。
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		if (cuacst == E_CUACST.CLOSED) {
			throw DpModuleError.DpstComm.BNAS1597();
		
		}else if (cuacst == E_CUACST.PREOPEN) {
			throw DpModuleError.DpstComm.BNAS1598();
			
		}
//		else if (cuacst == E_CUACST.PRECLOS) {
//			throw DpModuleError.DpstComm.E9999("电子账户状态为预销户状态，无法操作！");
//		
//		}
		
		// 调用DP模块服务查询冻结状态，检查电子账户状态字 
		IoDpFrozSvcType froz = SysUtil.getInstance(IoDpFrozSvcType.class);
		IoDpAcStatusWord cplGetAcStWord = froz.getAcStatusWord(custac);
		
		if(E_DEDUTP.DEDUIN == deductin.getDedutp()){
			if (cplGetAcStWord.getBrfroz() == E_YES___.YES){
				throw CaError.Eacct.BNAS0435();
			
			}else if(E_YES___.YES == cplGetAcStWord.getDbfroz() ){
				throw CaError.Eacct.BNAS0433();
			
//			}else if(E_YES___.YES == cplGetAcStWord.getBkalsp()){
//				throw CaError.Eacct.E0001("您的账户已全止(银行止付)");
				
			}else if(E_YES___.YES == cplGetAcStWord.getOtalsp()){
				throw CaError.Eacct.BNAS0434();
			}
			
			//涉恐检查
			SysUtil.getInstance(IoCaSevQryAccout.class).IoCaQryInwadeInfo(deductin.getCardno(), null, null, null);
		}		
		// 获取客户账号信息
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
							
		IoCaKnaCust tblKnaCust = caqry.getKnaCustByCustacOdb1(custac, true);
		//涉案检查
		IoCaSevQryAccout.IoCaQryInacInfo.Output output = SysUtil.getInstance(IoCaSevQryAccout.IoCaQryInacInfo.Output.class);
		SysUtil.getInstance(IoCaSevQryAccout.class).qryInac(deductin.getCardno(), tblKnaCust.getCustna(), null, null, output);
		
		final String otcard_in =deductin.getCardno();//转出账号
	    final String otacna_in =deductin.getCustna();//转出账户名称
	    final String otbrch_in =CommTools.getBaseRunEnvs().getTrxn_branch();//转出机构
	    final String incard_in =deductin.getOtheac();//转入账号
	    final String inacna_in =deductin.getOthena();//转入账号名称
	    final String inbrch_in =CommTools.getBaseRunEnvs().getTrxn_branch();//转入机构
	    final BigDecimal tranam_in = deductin.getDeduam();//交易金额
	    final String crcycd = deductin.getCrcycd();//币种
	    
		
		//内部扣划
		if(E_INSPFG.INVO == output.getInspfg()){
				
			// 独立事务
			DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
				@Override
				public Void execute() {
					// 获取涉案账户交易信息登记输入信息
					IoCaKnbTrinInput cplKnbTrin = SysUtil.getInstance(IoCaKnbTrinInput.class);							
											
					cplKnbTrin.setOtcard(otcard_in);// 转出账号
					cplKnbTrin.setOtacna(otacna_in);// 转出账号名称
					cplKnbTrin.setOtbrch(otbrch_in);// 转出账户机构
					cplKnbTrin.setIncard(incard_in);// 转入账号
					cplKnbTrin.setInacna(inacna_in);// 转入账户名称
					cplKnbTrin.setInbrch(inbrch_in);// 转入账户机构
					cplKnbTrin.setTranam(tranam_in);// 交易金额
					cplKnbTrin.setCrcycd(crcycd);// 币种
					cplKnbTrin.setIssucc(E_YES___.NO);// 交易是否成功
	
					// 涉案账户交易信息登记
					SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(cplKnbTrin);
	
					return null;
				}
			});
			
			if(E_DEDUTP.DEDUIN == deductin.getDedutp()){
				throw DpModuleError.DpstComm.BNAS0497();
			}	
		//可疑账户登记上报交易信息
		}else if (E_INSPFG.SUSP == output.getInspfg()) {
			// 独立事务
			DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
				@Override
				public Void execute() {
					// 获取涉案账户交易信息登记输入信息
					IoCaKnbTrinInput cplKnbTrin = SysUtil.getInstance(IoCaKnbTrinInput.class);							
											
					cplKnbTrin.setOtcard(otcard_in);// 转出账号
					cplKnbTrin.setOtacna(otacna_in);// 转出账号名称
					cplKnbTrin.setOtbrch(otbrch_in);// 转出账户机构
					cplKnbTrin.setIncard(incard_in);// 转入账号
					cplKnbTrin.setInacna(inacna_in);// 转入账户名称
					cplKnbTrin.setInbrch(inbrch_in);// 转入账户机构
					cplKnbTrin.setTranam(tranam_in);// 交易金额
					cplKnbTrin.setCrcycd(crcycd);// 币种
					cplKnbTrin.setIssucc(E_YES___.NO);// 交易是否成功
	
					// 涉案账户交易信息登记
					SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(cplKnbTrin);
	
					return null;
				}
			});
		}
		
		
		// 检查客户账号是否为空
		// 获取客户账号信息
	//	IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		//kna_cust tblKna_cust = Kna_custDao.selectOne_odb1(deductin.getCustac(), true);
		IoCaKnaCust tblKna_cust = caqry.getKnaCustByCustacOdb1(custac, true);
		
		// 客户账户是否存在
		if (CommUtil.isNull(tblKna_cust)) {
			throw DpModuleError.DpstComm.BNAS0520();
		}
		
		// 同一核算主体的经办柜员能扣划该核算主体下的电子账户
		if (!CommUtil.equals(tblKna_cust.getCorpno(), CommTools.getBaseRunEnvs().getBusi_org_id())) {
			throw DpModuleError.DpstComm.BNAS0791();
		}
		
		if ( !CommUtil.equals(tblKna_cust.getCustna(), deductin.getCustna())) {
			throw DpModuleError.DpstComm.BNAS0530();
		}
		
		
		BigDecimal totalam = BigDecimal.ZERO;//由于其他冻结引起的扣划限制金额
		BigDecimal acctBal = BigDecimal.ZERO;//账户总额
		
		if (CommUtil.isNotNull(deductin.getFrozno())) {
			// 冻结扣划检查,并获取扣划限制金额
			totalam = frozDeduCheck(deductin ,totalam ,trandt ,frlmtp);
		}else{
			// 直接扣划检查
			// 检查是否有冻结，有冻结时需要在冻结范围内扣划
			frozDireDeduCheck(deductin ,trandt);
		}
		
		//获取当前冻结扣划的冻结限制类型
		if(CommUtil.isNotNull(deductin.getFrozno())){
					
			// 查询冻结信息(取出的可能是冻结信息，也可能是续冻信息,限制类型对于统一编号是同一种)
			List<KnbFroz> lstTblKnbFroz = KnbFrozDao.selectAll_odb5(
			deductin.getFrozno(), E_FROZST.VALID, false);
			
			if(deductin.getDeogtp() != lstTblKnbFroz.get(0).getFrexog()){
				throw DpModuleError.DpstComm.BNAS0232();
			}
			
			if(!CommUtil.equals(deductin.getDeogna(), lstTblKnbFroz.get(0).getFrogna())){
				throw DpModuleError.DpstComm.BNAS0232();
			}
			
			frlmtp = lstTblKnbFroz.get(0).getFrlmtp();
		}
		
		//内部扣划(扣划账户可用余额)  当前有权机关能扣划金额  = 账户总额 - 当前生效的部冻总金额之和 
		if(E_DEDUTP.DEDUIN == deductin.getDedutp()){
			
			//获取账户总额
			acctBal = getAcctBal(custac,BusiTools.getDefineCurrency());
			
			//获取账户生效的冻结金额
			BigDecimal frozam = getFrozAmt(custac, E_YES___.YES);
			log.debug("--------当前账户生效的冻结金额为：" + frozam +"---------");
			
			BigDecimal deduam = acctBal.subtract(frozam);
			if(CommUtil.compare(deduam, BigDecimal.ZERO) <=0){
				deduam = BigDecimal.ZERO;
			}
			
			log.debug("--------当前内部扣划可扣划金额为：" + deduam +"---------");
			
			// 获取账户总额，为银行扣划时，总额为账户可用余额
			if (CommUtil.compare(deduam, deductin.getDeduam()) < 0) {
				throw DpModuleError.DpstComm.BNAS0454();
			}
		//外部扣划(扣划账户余额)
		}else{
			 //有冻结扣划：当前有权机关能扣划金额 = 账户总额 - 在先冻结金额(所有生效的部冻且在当前冻结记录之前冻结的金额总和)
			 if(CommUtil.isNotNull(deductin.getFrozno())){
				//获取账户总额
				acctBal = getAcctBal(custac,BusiTools.getDefineCurrency());
				log.debug("--------账户总额为：" + acctBal +"---------");
				
				log.debug("--------当前外部有冻结扣划在先冻结总额为：" + totalam +"---------");
				if(CommUtil.compare(acctBal.subtract(totalam), deductin.getDeduam()) < 0){
					throw DpModuleError.DpstComm.BNAS0236();
				}
			
			//无冻结扣划：当前有权机关能扣划金额 = 账户总额 - 当前生效的部冻总金额之和 
			}else{
				//获取账户总额
				acctBal = getAcctBal(custac,BusiTools.getDefineCurrency());
				log.debug("--------账户总额为：" + acctBal +"---------");
				
				//获取账户生效的冻结金额
				BigDecimal frozam = getFrozAmt(custac, E_YES___.YES);
				log.debug("--------当前账户生效的冻结金额为：" + frozam +"---------");
				
				BigDecimal deduam = acctBal.subtract(frozam);
				if(CommUtil.compare(deduam, BigDecimal.ZERO) <=0){
					deduam = BigDecimal.ZERO;
				}
				
				log.debug("--------当前外部无冻结可扣划金额为：" + deduam +"---------");
				
				if(CommUtil.compare(deduam, deductin.getDeduam()) < 0){
					throw DpModuleError.DpstComm.BNAS1198();
				}
			}
		}
		
		if (CommUtil.isNotNull(deductin.getFrozno()) 
				&& E_FRLMTP.AMOUNT.equals(frlmtp)) {   // 指定金额的冻结扣划才有解冻操作
			// 原冻结解冻处理（调用解冻服务）
			IoDpUnStopPayIn cplIoDpUnStopPayIn = SysUtil.getInstance(IoDpUnStopPayIn.class);
			cplIoDpUnStopPayIn.setCardno(deductin.getCardno());
			cplIoDpUnStopPayIn.setFrozdt(deductin.getFrozdt());
			cplIoDpUnStopPayIn.setIdno01(deductin.getOpno01());
			cplIoDpUnStopPayIn.setIdno02(deductin.getOpno02());
			cplIoDpUnStopPayIn.setIdtp01(deductin.getOptp01());
			cplIoDpUnStopPayIn.setIdtp02(deductin.getOptp02());
			cplIoDpUnStopPayIn.setOdfrno(deductin.getFrozno());
			cplIoDpUnStopPayIn.setUfctno(deductin.getUfctno());
			//cplIoDpUnStopPayIn.setUfcttp(deductin.getUfcttp());//没有解冻证明文书类别了
			cplIoDpUnStopPayIn.setUfexog(deductin.getDeogtp());
			cplIoDpUnStopPayIn.setUfna01(deductin.getOpna01());
			cplIoDpUnStopPayIn.setUfna02(deductin.getOpna02());
			cplIoDpUnStopPayIn.setUfreas(deductin.getReason());
			cplIoDpUnStopPayIn.setUnfram(deductin.getDeduam());
			cplIoDpUnStopPayIn.setRemark(deductin.getRemark());
			
			//获取原冻结编号在冻结状态下的最大序号(冻结登记簿)
			if(CommUtil.isNull(DpFrozDao.selFrozFrozstMaxseq(deductin.getFrozno(),E_FROZST.VALID, false))){
				throw DpModuleError.DpstComm.BNAS0738();
			}
			
			long maxFrozsq = DpUnfrProc.getMaxFrozstFrozsq(cplIoDpUnStopPayIn);
			
			// 获取原冻结登记簿信息
			KnbFroz tblKnbFroz8 = KnbFrozDao.selectOne_odb8(deductin.getFrozno(), maxFrozsq, false);
			//调用解冻服务
			DpUnfrProc.unFrozDo(cplIoDpUnStopPayIn, tblKnbFroz8);
			//SysUtil.getInstance(IoDpFrozSvcType.class).IoDpUnfrByLaw(cplIoDpUnStopPayIn);
		}
		
		
		// 生成冻结编号
		KnpParameter tblknp_para = KnpParameterDao.selectOne_odb1("IS_CHECK", "SPECTP", "%", "%", false);
		if(CommUtil.isNull(tblknp_para)){
			throw DpModuleError.DpstComm.BNAS1210();
		}
						
		String deduno = null;
						
		if(CommUtil.equals("Y", tblknp_para.getParm_value1())){
			String specno = "23" + CommTools.getBaseRunEnvs().getTrxn_date().substring(0,4);
							
			deduno = DpFrozProc.getMaxSpecno(E_SPECTP.DEDUCT,specno);
						
		}else if(CommUtil.equals("N", tblknp_para.getParm_value1())){
			// 生成扣划编号
			deduno = "23" + CommTools.getBaseRunEnvs().getTrxn_date().substring(0,4) + genDeduno();
		}
		// 生成扣划编号
//		String specno = "23" + CommTools.getBaseRunEnvs().getTrxn_date().substring(0,4);
//		
//		String deduno = DpFrozProc.getMaxSpecno(E_SPECTP.DEDUCT, specno);
		
		// 根据扣划金额，将电子账户活期户资金，电子账户亲情钱包资金和电子账户定期户余额转入电子账户个人活期结算户	
		//moneyDeduct(deductin);
		DeductOut deductOut = moneyRemoteDeduct(deductin);
		deductOut.setDeduno(deduno);

		// 登记扣划登记簿
		KnbDedu tblknbdedu = SysUtil.getInstance(KnbDedu.class);
		tblknbdedu.setDeduno(deduno);
		tblknbdedu.setCustac(custac);
		tblknbdedu.setIsflag(E_YES___.NO);
		tblknbdedu.setDedutp(deductin.getDedutp());
		tblknbdedu.setCustna(deductin.getCustna());
		tblknbdedu.setCrcycd(deductin.getCrcycd());
		tblknbdedu.setCsextg(deductin.getCsextg());
		tblknbdedu.setDeduam(deductin.getDeduam());
		tblknbdedu.setTrantp(deductin.getTrantp());
		tblknbdedu.setFrozno(deductin.getFrozno());
		tblknbdedu.setFrozdt(deductin.getFrozdt());
		tblknbdedu.setUfctno(deductin.getUfctno());
		tblknbdedu.setDectno(deductin.getDectno());
		tblknbdedu.setDeogtp(deductin.getDeogtp());
		tblknbdedu.setDeogna(deductin.getDeogna());
		tblknbdedu.setOpna01(deductin.getOpna01());
		tblknbdedu.setOptp01(deductin.getOptp01());
		tblknbdedu.setOpno01(deductin.getOpno01());
		tblknbdedu.setOpna02(deductin.getOpna02());
		tblknbdedu.setOptp02(deductin.getOptp02());
		tblknbdedu.setOpno02(deductin.getOpno02());
		tblknbdedu.setMonyto(deductin.getMonyto());
		tblknbdedu.setOtheac(deductin.getOtheac());
		tblknbdedu.setOthena(deductin.getOthena());
		tblknbdedu.setOthebk(deductin.getOthebk());
		tblknbdedu.setReason(deductin.getReason());
		tblknbdedu.setRemark(deductin.getRemark());
		tblknbdedu.setTrandt(trandt);
		tblknbdedu.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
		tblknbdedu.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq()); // 交易流水
		tblknbdedu.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller()); // 交易柜员
		//tblknbdedu.setCptrcd(CommTools.getBaseRunEnvs().getCptrcd()); // 对账代码
		tblknbdedu.setLttscd(BusiTools.getBusiRunEnvs().getLttscd()); // 内部交易码
		tblknbdedu.setCkuser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 授权柜员
		tblknbdedu.setPruser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 审批柜员
		tblknbdedu.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
		tblknbdedu.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
		KnbDeduDao.insert(tblknbdedu);
		
		//平衡性检查
		//SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._01);
		
/*		//机构、柜员额度验证
		IoBrchUserQt ioBrchUserQt = SysUtil.getInstance(IoBrchUserQt.class);
		ioBrchUserQt.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
		ioBrchUserQt.setBusitp(E_BUSITP.TR);
		ioBrchUserQt.setCrcycd(deductin.getCrcycd());
		ioBrchUserQt.setTranam(deductin.getDeduam());
		ioBrchUserQt.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());
		SysUtil.getInstance(IoSrvPbBranch.class).selBrchUserQt(ioBrchUserQt);*/
		
		
		/**
    	 * 冲正登记
    	 */
    	IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
    	cplInput.setCustac(custac); //电子账户号
    	cplInput.setTranev(ApUtil.TRANS_EVENT_DEDUCT);
    	cplInput.setTranam(deductin.getDeduam());// 交易金额
    	cplInput.setCrcycd(deductin.getCrcycd());// 货币代号
    	cplInput.setFrozno(deductin.getFrozno());// 交易序号
    	cplInput.setEvent2(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
    	cplInput.setEvent1(CommTools.getBaseRunEnvs().getTrxn_date());//主交易日期
    	
    	//ApStrike.regBook(cplInput);

		IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);    		
		apinput.setReversal_event_id(cplInput.getTranev());
		apinput.setInformation_value(SysUtil.serialize(cplInput));
		MsEvent.register(apinput, true);
		
		return deductOut;
	}
	/**
	 * 扣划外调记账处理
	 * @param <IoDeductOut>
	 * @param deductin
	 */
	public static cn.sunline.ltts.busi.dp.type.DpAcctType.DeductOut moneyRemoteDeduct(IoDeductIn deductin) {

		BigDecimal acctbal = deductin.getDeduam();//扣划变化金额,初始值为扣划金额
		BigDecimal bal = null;//结算户可用余额
		String remark ;
		DeductOut deductOut = SysUtil.getInstance(DeductOut.class);
		
		if(CommUtil.isNull(deductin.getRemark())){
			remark = "";
			
		}else{
			remark = deductin.getRemark();
		}
		
		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(deductin.getCardno(), false);
		if(CommUtil.isNull(caKnaAcdc)){
			throw DpModuleError.DpstComm.BNAS0692();
		}
		String custac = caKnaAcdc.getCustac();
		
		// 查询电子账户个人活期结算户金额（根据客户账号，子账户区分类型）
		E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac);
		
		//如果是一，二内户从个人结算户扣划出去
		if(E_ACCATP.FINANCE == eAccatp || E_ACCATP.GLOBAL == eAccatp){
			//获取非销户状态的个人结算户
			//KnaAcct acct = KnaAcctDao.selectFirst_odb9(E_ACSETP.SA, custac, true);
			KnaAcct acct = DpFrozDao.selKnaAcctInfoByCustac(custac, E_ACSETP.SA, true);
			
/*			// 获取转存签约明细信息
			IoCaKnaSignDetl cplkna_sign_detl = SysUtil.getInstance(IoCaSevQryTableInfo.class)
					.kna_sign_detl_selectFirst_odb2(acct.getAcctno(), E_SIGNST.QY, false);
			
			// 存在转存签约明细信息则取资金池可用余额
			if (CommUtil.isNotNull(cplkna_sign_detl)) {
				bal = DpAcctProc.getProductBal(custac, acct.getCrcycd(), E_YES___.NO, false);
			}else{
				// 其他取账户余额
				bal = DpAcctProc.getAcctOnlnbl(acct.getAcctno(), false);
			}*/
			
			// 可用余额 add by xiongzhao 2016/12/22
			bal = SysUtil.getInstance(DpAcctSvcType.class)
					.getAcctaAvaBal(custac, acct.getAcctno(),
							acct.getCrcycd(), E_YES___.NO, E_YES___.NO);
			
			// 将其他子账户的金额按活期账户，亲情钱包，定期账户顺序取出到个人活期结算户，当同一类型有多个时按由近及远的规则转出
			if (CommUtil.compare(bal, acctbal) < 0) {//结算户可用余额小于扣划金额
				acctbal = acctbal.subtract(bal);

				// 查询KnaAcct表的非个人活期结算户种类的账户信息,根据开户日期排序，开户日期最大的排前面
				//钱包账户
				List<KnaAcct> lstKnaAcct = DpAcctQryDao.selKnaAcctZNhq(custac, E_ACSETP.MA, false);
				
				if(CommUtil.isNotNull(lstKnaAcct)){
					// 活期账户转出
					for (KnaAcct KnaAcct : lstKnaAcct) {
						//判断当前扣划金额是否为0
						if (CommUtil.compare(acctbal, BigDecimal.ZERO) == 0) break;
						
						if (CommUtil.compare(KnaAcct.getOnlnbl(), BigDecimal.ZERO) > 0 && CommUtil.compare(KnaAcct.getOnlnbl(), acctbal) <= 0) { // 该账户需要全额转入个人活期结算户
							// 调用支取记账处理,支取金额 KnaAcct.getOnlnbl()
							DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
							cplDrawAcctIn.setCustac(KnaAcct.getCustac());
							cplDrawAcctIn.setTranam(KnaAcct.getOnlnbl());
							cplDrawAcctIn.setCrcycd(KnaAcct.getCrcycd());
							cplDrawAcctIn.setAcctno(KnaAcct.getAcctno());
							cplDrawAcctIn.setOpacna(acct.getAcctna());
							cplDrawAcctIn.setToacct(acct.getAcctno());
							cplDrawAcctIn.setIschck(E_YES___.NO);
							cplDrawAcctIn.setIsdedu(E_YES___.YES);
							cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
							cplDrawAcctIn.setRemark("扣划" + remark);
							DrawDpAcctOut dpAcctOut = SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
							
							// 调用存入记账处理 ,存入个人活期结算户
							SaveDpAcctIn cplSaveAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
					        cplSaveAcctIn.setCustac(acct.getCustac());
					        cplSaveAcctIn.setTranam(KnaAcct.getOnlnbl());
					        cplSaveAcctIn.setCrcycd(acct.getCrcycd());
					        cplSaveAcctIn.setAcctno(acct.getAcctno());
					        cplSaveAcctIn.setOpacna(KnaAcct.getAcctna());
					        cplSaveAcctIn.setToacct(KnaAcct.getAcctno());
					        cplSaveAcctIn.setIschck(E_YES___.NO);
					        cplSaveAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
					        cplSaveAcctIn.setRemark("扣划" + remark);
							SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn);
							
							acctbal = acctbal.subtract(KnaAcct.getOnlnbl());
							
						
						}else if (CommUtil.compare(KnaAcct.getOnlnbl(), acctbal) > 0){//该账户部分转入个人活期结算户
							// 调用支取记账处理,支取金额 acctbal
							DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
							cplDrawAcctIn.setCustac(KnaAcct.getCustac());
							cplDrawAcctIn.setTranam(acctbal);
							cplDrawAcctIn.setCrcycd(KnaAcct.getCrcycd());
							cplDrawAcctIn.setAcctno(KnaAcct.getAcctno());
							cplDrawAcctIn.setOpacna(acct.getAcctna());
							cplDrawAcctIn.setToacct(acct.getAcctno());
							cplDrawAcctIn.setIschck(E_YES___.NO);
							cplDrawAcctIn.setIsdedu(E_YES___.YES);
							cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
							cplDrawAcctIn.setRemark("扣划" + remark);
							DrawDpAcctOut dpAcctOut = SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
							
							// 调用存入记账处理 ,存入个人活期结算户
							SaveDpAcctIn cplSaveAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
					        cplSaveAcctIn.setCustac(acct.getCustac());
					        cplSaveAcctIn.setTranam(acctbal);
					        cplSaveAcctIn.setCrcycd(acct.getCrcycd());
					        cplSaveAcctIn.setAcctno(acct.getAcctno());
					        cplSaveAcctIn.setOpacna(KnaAcct.getAcctna());
					        cplSaveAcctIn.setToacct(KnaAcct.getAcctno());
					        cplSaveAcctIn.setIschck(E_YES___.NO);
					        cplSaveAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
					        cplSaveAcctIn.setRemark("扣划" + remark);
							SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn);
							
							acctbal = acctbal.subtract(acctbal);
						}
					}
				}

				// 查询KnaAcct表的非个人活期结算户种类的账户信息,根据开户日期排序，开户日期最大的排前面
				//亲情钱包
				List<KnaAcct> lstKnaAcctone = DpAcctQryDao.selKnaAcctZNhq(custac, E_ACSETP.FW, false);
				
				if(CommUtil.isNotNull(lstKnaAcctone)){
					// 活期账户转出
					for (KnaAcct KnaAcct : lstKnaAcctone) {
						//判断当前扣划金额是否为0
						if (CommUtil.compare(acctbal, BigDecimal.ZERO) == 0) break;
						
						if (CommUtil.compare(KnaAcct.getOnlnbl(), BigDecimal.ZERO) > 0 && CommUtil.compare(KnaAcct.getOnlnbl(), acctbal) <= 0) { // 该账户需要全额转入个人活期结算户
							// 调用支取记账处理,支取金额 KnaAcct.getOnlnbl()
							DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
							cplDrawAcctIn.setCustac(KnaAcct.getCustac());
							cplDrawAcctIn.setTranam(KnaAcct.getOnlnbl());
							cplDrawAcctIn.setCrcycd(KnaAcct.getCrcycd());
							cplDrawAcctIn.setAcctno(KnaAcct.getAcctno());
							cplDrawAcctIn.setOpacna(acct.getAcctna());
							cplDrawAcctIn.setToacct(acct.getAcctno());
							cplDrawAcctIn.setIschck(E_YES___.NO);
							cplDrawAcctIn.setIsdedu(E_YES___.YES);
							cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
							cplDrawAcctIn.setRemark("扣划" + remark);
							DrawDpAcctOut dpAcctOut = SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
							
							// 调用存入记账处理 ,存入个人活期结算户
							SaveDpAcctIn cplSaveAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
					        cplSaveAcctIn.setCustac(acct.getCustac());
					        cplSaveAcctIn.setTranam(KnaAcct.getOnlnbl());
					        cplSaveAcctIn.setCrcycd(acct.getCrcycd());
					        cplSaveAcctIn.setAcctno(acct.getAcctno());
					        cplSaveAcctIn.setOpacna(KnaAcct.getAcctna());
					        cplSaveAcctIn.setToacct(KnaAcct.getAcctno());
					        cplSaveAcctIn.setIschck(E_YES___.NO);
					        cplSaveAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
					        cplSaveAcctIn.setRemark("扣划" + remark);
					        
							SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn);
							
							acctbal = acctbal.subtract(KnaAcct.getOnlnbl());
							
						
						}else if (CommUtil.compare(KnaAcct.getOnlnbl(), acctbal) > 0){//该账户部分转入个人活期结算户
							// 调用支取记账处理,支取金额 acctbal
							DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
							cplDrawAcctIn.setCustac(KnaAcct.getCustac());
							cplDrawAcctIn.setTranam(acctbal);
							cplDrawAcctIn.setCrcycd(KnaAcct.getCrcycd());
							cplDrawAcctIn.setAcctno(KnaAcct.getAcctno());
							cplDrawAcctIn.setOpacna(acct.getAcctna());
							cplDrawAcctIn.setIschck(E_YES___.NO);
							cplDrawAcctIn.setIsdedu(E_YES___.YES);
							cplDrawAcctIn.setToacct(acct.getAcctno());
							cplDrawAcctIn.setIsdedu(E_YES___.YES);
							cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
							cplDrawAcctIn.setRemark("扣划" + remark);
							DrawDpAcctOut dpAcctOut = SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
							
							// 调用存入记账处理 ,存入个人活期结算户
							SaveDpAcctIn cplSaveAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
					        cplSaveAcctIn.setCustac(acct.getCustac());
					        cplSaveAcctIn.setTranam(acctbal);
					        cplSaveAcctIn.setCrcycd(acct.getCrcycd());
					        cplSaveAcctIn.setAcctno(acct.getAcctno());
					        cplSaveAcctIn.setOpacna(KnaAcct.getAcctna());
					        cplSaveAcctIn.setToacct(KnaAcct.getAcctno());
					        cplSaveAcctIn.setIschck(E_YES___.NO);
					        cplSaveAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
					        cplSaveAcctIn.setRemark("扣划" + remark);
					        
							SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn);
							
							acctbal = acctbal.subtract(acctbal);
							
						}
					}
				}
				
				// 定期账户转出
				if (CommUtil.compare(acctbal, BigDecimal.ZERO) > 0) {
					// 根据查询KnaFxac表的定期账户信息,根据开户日期排序，开户日期最大的排前面
					// kna_fxsv表  detifg字段，为是的是智能储蓄，为否的是定期，先扣智能储蓄的
					List<KnaFxac> lstKnaFxac = DpAcctQryDao.selKnaFxacByCustacOrderDetlfg(custac,E_DPACST.CLOSE,false);
					
					if(CommUtil.isNotNull(lstKnaFxac)){
					
						for (KnaFxac knafxac : lstKnaFxac) {
							//判断剩余扣划金额是否为0
							if (CommUtil.compare(acctbal, BigDecimal.ZERO) == 0) break;
							
							//签约为个人活期结算户部分，已做扣划，不在定期中重复计算
							// 获取转存签约明细信息
							IoCaKnaSignDetl cplkna_sign_detlone = SysUtil.getInstance(IoCaSevQryTableInfo.class)
									.getKnaSignDetlFirstOdb3(acct.getAcctno(), knafxac.getAcctno(), E_SIGNST.QY, false);//(knafxac.getAcctno(), E_SIGNST.QY, false);
							
							if(CommUtil.isNotNull(cplkna_sign_detlone)){
								continue;
							}
							if (CommUtil.compare(knafxac.getOnlnbl(), BigDecimal.ZERO) > 0 && CommUtil.compare(knafxac.getOnlnbl(), acctbal) <= 0) { // 该账户需要全额转入个人活期结算户
								// 调用支取记账处理,支取金额 knafxac.getOnlnbl()
								DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
								cplDrawAcctIn.setCustac(knafxac.getCustac());
								cplDrawAcctIn.setTranam(knafxac.getOnlnbl());
								cplDrawAcctIn.setCrcycd(knafxac.getCrcycd());
								cplDrawAcctIn.setAcctno(knafxac.getAcctno());
								cplDrawAcctIn.setOpacna(acct.getAcctna());
								cplDrawAcctIn.setToacct(acct.getAcctno());
								cplDrawAcctIn.setAuacfg(E_YES___.NO);
								cplDrawAcctIn.setIschck(E_YES___.NO);
								cplDrawAcctIn.setIsdedu(E_YES___.YES); //扣划标志
								cplDrawAcctIn.setDedutp(deductin.getDedutp()); //扣划类型
								cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
								cplDrawAcctIn.setRemark("扣划" + remark);
								
								DrawDpAcctOut dpAcctOut = SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
								
								log.debug("扣划定期利息为：" + dpAcctOut.getInstam() +"--------------------" );
								
								//若定期支取利息为空，则给初始化为0
						        if(CommUtil.isNull(dpAcctOut.getInstam())){
						        	dpAcctOut.setInstam(BigDecimal.ZERO);
						        }
								
								// 调用存入记账处理 ,存入个人活期结算户
								SaveDpAcctIn cplSaveAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
						        cplSaveAcctIn.setCustac(acct.getCustac());
						        cplSaveAcctIn.setTranam(knafxac.getOnlnbl().add(dpAcctOut.getInstam()));
						        cplSaveAcctIn.setCrcycd(acct.getCrcycd());
						        cplSaveAcctIn.setAcctno(acct.getAcctno());
						        cplSaveAcctIn.setOpacna(knafxac.getAcctna());
						        cplSaveAcctIn.setToacct(knafxac.getAcctno());
						        cplSaveAcctIn.setIschck(E_YES___.NO);
						        cplSaveAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
						        cplSaveAcctIn.setRemark("扣划" + remark);
						        
								SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn);
								
								//支取利息税
								if(CommUtil.isNotNull(dpAcctOut.getIntxam()) &&
										CommUtil.compare(dpAcctOut.getIntxam(), BigDecimal.ZERO) > 0){
									DrawDpAcctIn drawAcctOut = SysUtil.getInstance(DrawDpAcctIn.class);
									drawAcctOut.setCustac(acct.getCustac());//电子账号
									drawAcctOut.setTranam(dpAcctOut.getIntxam());//利息税
									drawAcctOut.setCrcycd(acct.getCrcycd());
									drawAcctOut.setAcctno(acct.getAcctno());
									drawAcctOut.setIschck(E_YES___.NO);
									drawAcctOut.setIsdedu(E_YES___.YES);
									drawAcctOut.setSmrycd(BusinessConstants.SUMMARY_KH);
									drawAcctOut.setRemark("扣划" + remark);
									
									SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawAcctOut);
								}
								
								acctbal = acctbal.subtract(knafxac.getOnlnbl());
								
							}else if (CommUtil.compare(knafxac.getOnlnbl(), acctbal) > 0){//该账户部分转入个人活期结算户
								// 调用支取记账处理,支取金额 acctbal
								BigDecimal tranam = BigDecimal.ZERO; // 定义定期的支取金额
								DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
								cplDrawAcctIn.setCustac(knafxac.getCustac());
								boolean isDrawAll = checkDrawFxac(knafxac.getAcctno(),acctbal);
								if (isDrawAll) {
									cplDrawAcctIn.setTranam(knafxac.getOnlnbl()); // 全额支取
								} else {
									cplDrawAcctIn.setTranam(acctbal); // 部分支取
								}
								cplDrawAcctIn.setCrcycd(knafxac.getCrcycd());
								cplDrawAcctIn.setAcctno(knafxac.getAcctno());
								cplDrawAcctIn.setOpacna(acct.getAcctna());
								cplDrawAcctIn.setToacct(acct.getAcctno());
								cplDrawAcctIn.setAuacfg(E_YES___.NO);
								cplDrawAcctIn.setIschck(E_YES___.NO);
								cplDrawAcctIn.setIsdedu(E_YES___.YES); //扣划标志
								cplDrawAcctIn.setDedutp(deductin.getDedutp()); //扣划类型
								cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
								cplDrawAcctIn.setRemark("扣划" + remark);
								
								DrawDpAcctOut dpAcctOut = SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
								
								log.debug("扣划定期利息为：" + dpAcctOut.getInstam() +"+++++++++++++++++++++++++++" );
								
								//若定期支取利息为空，则给初始化为0
						        if(CommUtil.isNull(dpAcctOut.getInstam())){
						        	dpAcctOut.setInstam(BigDecimal.ZERO);
						        }
								
								// 调用存入记账处理 ,存入个人活期结算户
								SaveDpAcctIn cplSaveAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
						        cplSaveAcctIn.setCustac(acct.getCustac());
						        cplSaveAcctIn.setTranam(tranam.add(dpAcctOut.getInstam()));
						        cplSaveAcctIn.setCrcycd(acct.getCrcycd());
						        cplSaveAcctIn.setAcctno(acct.getAcctno());
						        cplSaveAcctIn.setOpacna(knafxac.getAcctna());
						        cplSaveAcctIn.setToacct(knafxac.getAcctno());
						        cplSaveAcctIn.setIschck(E_YES___.NO);
						        cplSaveAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
						        cplSaveAcctIn.setRemark("扣划" + remark);
						        
								SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn);
								
								
								//支取利息税
								if(CommUtil.isNotNull(dpAcctOut.getIntxam()) &&
										CommUtil.compare(dpAcctOut.getIntxam(), BigDecimal.ZERO) > 0){
									DrawDpAcctIn drawAcctOut = SysUtil.getInstance(DrawDpAcctIn.class);
									drawAcctOut.setCustac(acct.getCustac());//电子账号
									drawAcctOut.setTranam(dpAcctOut.getIntxam());//利息税
									drawAcctOut.setCrcycd(acct.getCrcycd());
									drawAcctOut.setAcctno(acct.getAcctno());
									drawAcctOut.setIschck(E_YES___.NO);
									drawAcctOut.setIsdedu(E_YES___.YES);
									drawAcctOut.setSmrycd(BusinessConstants.SUMMARY_KH);
									drawAcctOut.setRemark("扣划" + remark);
									
									SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawAcctOut);
								}
								
								acctbal = acctbal.subtract(acctbal);
								
							}
						}
					}
				}
				
			}
			
			// 电子账户个人活期结算户出金
			DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
			cplDrawAcctIn.setCustac(acct.getCustac());
			cplDrawAcctIn.setTranam(deductin.getDeduam());
			cplDrawAcctIn.setCrcycd(acct.getCrcycd());
			cplDrawAcctIn.setAcctno(acct.getAcctno());
			cplDrawAcctIn.setOpacna(deductin.getOthena());
			cplDrawAcctIn.setToacct(deductin.getOtheac());
			cplDrawAcctIn.setIschck(E_YES___.NO);
			cplDrawAcctIn.setIsdedu(E_YES___.YES);
			cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
			cplDrawAcctIn.setRemark("扣划" + remark);
			
			SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
			//返回值赋值
			deductOut.setAcctno(acct.getAcctno());
			deductOut.setAcctna(acct.getAcctna());
			deductOut.setBrchno(acct.getBrchno());
			
			/*KnpParameter tbl_KnpParameter = KnpParameterDao.selectOne_odb1("DPTRAN", "DEDUCT", "%","%", true);
			// 电子账户个人结算户入往来内部户
			IaAcdrInfo acdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
			acdrInfo.setBusino(tbl_KnpParameter.getParm_value1());
			acdrInfo.setSubsac(tbl_KnpParameter.getParm_value2());
			acdrInfo.setCrcycd(acct.getCrcycd());
			acdrInfo.setToacct(acct.getAcctno());
			acdrInfo.setToacna(acct.getAcctna());
			acdrInfo.setTranam(deductin.getDeduam());
			acdrInfo.setAcbrch(BusiTools.getBusiRunEnvs().getCentbr());
			acdrInfo.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
			acdrInfo.setDscrtx("扣划" + remark);
			IaTransOutPro out = SysUtil.getRemoteInstance(IoInAccount.class).IoInAccrAdm(acdrInfo);			
			
			//登记出入金登记簿
			IoSaveIoTransBill.SaveIoBill.InputSetter billInfo = SysUtil.getInstance(IoSaveIoTransBill.SaveIoBill.InputSetter.class);
			
			billInfo.setAcctnm(deductin.getCustna());//客户名称
			billInfo.setBrchno(BusiTools.getBusiRunEnvs().getCentbr());//内部户记账机构
			billInfo.setBusino(tbl_KnpParameter.getParm_value1());//内部户业务代码
			billInfo.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq());//业务跟踪号
			billInfo.setCapitp(E_CAPITP.DP996);//扣划
			billInfo.setCardno(out.getAcctno());//内部户账号
			billInfo.setCrcycd(BusiTools.getDefineCurrency());//币种
			billInfo.setFrondt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
			billInfo.setFronsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
			billInfo.setIoflag(E_IOFLAG.OUT);//出入金标识
			billInfo.setKeepdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期
			billInfo.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());//内部交易码
			billInfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id().toString());//交易渠道
			billInfo.setToacct(deductin.getCardno());//电子账号
			billInfo.setToacno(acct.getAcctno());//个人结算主账户
			billInfo.setTobrch(acct.getBrchno());//电子账户开户机构
			billInfo.setTranam(deductin.getDeduam());//交易金额
			billInfo.setToscac(deductin.getCardno());//电子账号
			billInfo.setTranst(E_TRANST.NORMAL);//交易状态
			
			SysUtil.getInstance(IoSaveIoTransBill.class).saveIoBill(billInfo);*/
		
		//若为三类户，则从钱包账户
		}else if(E_ACCATP.WALLET == eAccatp){
			//钱包账户
			List<KnaAcct> lstKnaAcct = DpAcctQryDao.selKnaAcctZNhq(custac, E_ACSETP.MA, false);
			
			if(CommUtil.isNotNull(lstKnaAcct)){
				KnaAcct tblKnaAcct = lstKnaAcct.get(0);
				if(lstKnaAcct.size() == 1){
					if(CommUtil.compare(tblKnaAcct.getOnlnbl(), deductin.getDeduam())>0){
						DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
						cplDrawAcctIn.setCustac(tblKnaAcct.getCustac());
						cplDrawAcctIn.setTranam(deductin.getDeduam());
						cplDrawAcctIn.setCrcycd(tblKnaAcct.getCrcycd());
						cplDrawAcctIn.setAcctno(tblKnaAcct.getAcctno());
						cplDrawAcctIn.setOpacna(deductin.getOthena());
						cplDrawAcctIn.setToacct(deductin.getOtheac());
						cplDrawAcctIn.setIschck(E_YES___.NO);
						cplDrawAcctIn.setIsdedu(E_YES___.YES);
						cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_KH);
						cplDrawAcctIn.setRemark("扣划" + remark);
						SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
						//返回值赋值
						deductOut.setAcctno(tblKnaAcct.getAcctno());
						deductOut.setAcctna(tblKnaAcct.getAcctna());
						deductOut.setBrchno(tblKnaAcct.getBrchno());
					}
				}else if (lstKnaAcct.size() > 1) {
					//TODO:暂不支持多个钱包账户扣划
					throw DpModuleError.DpstFroz.BNAS3003();
				}
				/*for(KnaAcct KnaAcct : lstKnaAcct){
					// 电子账户钱包账户出金
					
					
					KnpParameter tbl_KnpParameter = KnpParameterDao.selectOne_odb1("DPTRAN", "DEDUCT", "%","%", true);
					// 电子账户钱包户入往来内部户
					IaAcdrInfo acdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
					acdrInfo.setBusino(tbl_KnpParameter.getParm_value1());
					acdrInfo.setSubsac(tbl_KnpParameter.getParm_value2());
					acdrInfo.setCrcycd(KnaAcct.getCrcycd());
					acdrInfo.setToacct(KnaAcct.getAcctno());
					acdrInfo.setToacna(KnaAcct.getAcctna());
					acdrInfo.setTranam(deductin.getDeduam());
					acdrInfo.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
					acdrInfo.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
					acdrInfo.setDscrtx("扣划" + remark);
					IaTransOutPro out = SysUtil.getRemoteInstance(IoInAccount.class).IoInAccrAdm(acdrInfo);					
					
					//登记出入金登记簿
					IoSaveIoTransBill.SaveIoBill.InputSetter billInfo = SysUtil.getInstance(IoSaveIoTransBill.SaveIoBill.InputSetter.class);
					
					billInfo.setAcctnm(deductin.getCustna());//客户名称
					billInfo.setBrchno(BusiTools.getBusiRunEnvs().getCentbr());//内部户记账机构
					billInfo.setBusino(tbl_KnpParameter.getParm_value1());//内部户业务代码
					billInfo.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq());//业务跟踪号
					billInfo.setCapitp(E_CAPITP.DP996);//扣划
					billInfo.setCardno(out.getAcctno());//内部户账号
					billInfo.setCrcycd(BusiTools.getDefineCurrency());//币种
					billInfo.setFrondt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
					billInfo.setFronsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
					billInfo.setIoflag(E_IOFLAG.OUT);//出入金标识
					billInfo.setKeepdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期
					billInfo.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());//内部交易码
					billInfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id().toString());//交易渠道
					billInfo.setToacct(deductin.getCardno());//电子账号
					billInfo.setToacno(KnaAcct.getAcctno());//个人结算主账户
					billInfo.setTobrch(KnaAcct.getBrchno());//电子账户开户机构
					billInfo.setTranam(deductin.getDeduam());//交易金额
					billInfo.setToscac(deductin.getCardno());//电子账号
					billInfo.setTranst(E_TRANST.NORMAL);//交易状态
					
					SysUtil.getInstance(IoSaveIoTransBill.class).saveIoBill(billInfo);
				}*/
			}
		}
		return deductOut;
	}
	
}
