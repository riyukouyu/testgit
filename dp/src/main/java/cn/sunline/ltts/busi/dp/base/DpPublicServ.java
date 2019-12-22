package cn.sunline.ltts.busi.dp.base;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_EVENTLEVEL;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_EVNTST;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_YESORNO;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.acct.DpCloseAcctno;
import cn.sunline.ltts.busi.dp.acct.DpKnaFxac;
import cn.sunline.ltts.busi.dp.acct.DpOpenDefault;
import cn.sunline.ltts.busi.dp.acct.OpenSubAcctDeal;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.domain.DpAcctOnlnblEntity;
import cn.sunline.ltts.busi.dp.domain.DpOpenAcctEntity;
import cn.sunline.ltts.busi.dp.domain.DpSaveEntity;
import cn.sunline.ltts.busi.dp.froz.DpFrozTools;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpSaveDrawDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.DpHisDepo.HKnlBill;
import cn.sunline.ltts.busi.dp.tables.DpHisDepo.HKnlBillDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDpsg;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDpsgDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDraw;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDrawDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDrawPlan;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDrawPlanDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdr;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdrDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdrPlan;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdrPlanDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxsv;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxsvDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxsvPlan;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxsvPlanDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSave;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSaveDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSavePlan;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSavePlanDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBill;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBillDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.AuacinTranData;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaStaPublic;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.AddSubAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkOpenAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpOpenSub;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSTRFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CTRLWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PLANFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PLSTAT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PMCRAC;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_POSTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SIGNTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRDPTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CORRTG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

/**
 * 对外提供方法
 * 
 * @author cuijia
 * 
 */
public class DpPublicServ {

    private static final BizLog bizlog = BizLogUtil
            .getBizLog(DpPublicServ.class);

    /**
     * 存入方法
     * 
     * @param entity
     * 
     *        1.活期账户余额变更 2.存入控制登记 3.利息调整处理 4.登记会计流水
     */
	public static void postAcctDp(DpSaveEntity entity) {
        String acctno = entity.getAcctno();
        String crcycd = entity.getCrcycd();
        BigDecimal tranam = entity.getTranam();

        if (CommUtil.isNull(tranam)) {
        	throw DpModuleError.DpstProd.BNAS0620();
        }
        // 检查交易金额
        if (E_YES___.YES != entity.getNegafg()) {
            if (CommUtil.compare(tranam, BigDecimal.ZERO) <= 0) {
            	throw DpModuleError.DpstComm.BNAS0622();
            }
        }
        if (CommUtil.isNull(crcycd)) {
        	throw DpModuleError.DpstComm.BNAS1101();
        }
        String cardno = entity.getCardno();
        String custac = entity.getCustac();
        String acseno = entity.getAcseno();
        String toacct = entity.getToacct();
        String opacna = entity.getOpacna();

    	String event = ApUtil.TRANS_EVENT_DPSAVE;

        IoCaKnaAccs cplKnaAccs = DpAcctQryDao.selKnaAccsByAcctno(acctno, false);
        if (CommUtil.isNull(cplKnaAccs))
        	throw DpModuleError.DpstAcct.BNAS1125(custac, acctno);

        if (CommUtil.isNull(cplKnaAccs.getFcflag()))
        	throw DpModuleError.DpstAcct.BNAS0844();
        // 定活标志
        E_FCFLAG fcflag = cplKnaAccs.getFcflag();

        // 是否检查规则标志不为否是，则检查，为否是则不检查
        if (E_YES___.NO != entity.getIschck()) {
            /**
             * 存入控制检查
             */
            DpAcctComm.checkDpSave(acctno, tranam, fcflag,
                    cplKnaAccs.getProdcd(), crcycd, entity.getAuacfg(), custac,
                    entity.getNgblfg());
        }

        /**
         * mod by xj 20180502 IoHotCtrlSvcType没有服务实现,注释掉关于热点账户的代码
         */
        /*IoHotCtrlSvcType ioHotCtrlSvcType = SysUtil.getInstance(IoHotCtrlSvcType.class);
        E_YES___ hcflag = ioHotCtrlSvcType.selHcpDefn(acctno);*/
        E_YES___ hcflag = E_YES___.NO;
        
        /**
         * 修改账户余额
         */
        DpAcctOnlnblEntity onlnblEntity = new DpAcctOnlnblEntity();
        onlnblEntity.setAcctno(acctno);
        onlnblEntity.setAmntcd(E_AMNTCD.CR);
        onlnblEntity.setTranam(tranam);
        onlnblEntity.setCrcycd(crcycd);
        onlnblEntity.setCardno(cardno);
        onlnblEntity.setCustac(custac);
        onlnblEntity.setAcseno(acseno);
        onlnblEntity.setToacct(toacct);
        onlnblEntity.setOpacna(opacna);
        onlnblEntity.setLinkno(entity.getLinkno());
        onlnblEntity.setOpbrch(entity.getOpbrch());
        onlnblEntity.setBankcd(entity.getBankcd()); // 对方金融机构代码
        onlnblEntity.setBankna(entity.getBankna()); // 对方金融机构名称
        onlnblEntity.setSmrycd(entity.getSmrycd()); // 摘要代码
        onlnblEntity.setSmryds(entity.getSmryds()); // 摘要描述
        onlnblEntity.setRemark(entity.getRemark()); // 备注
        onlnblEntity.setFxaufg(entity.getFxaufg()); // 自动转存标志
        onlnblEntity.setDetlsq(entity.getDetlsq());
        onlnblEntity.setMacdrs(entity.getMacdrs());
        onlnblEntity.setTeleno(entity.getTeleno());
        onlnblEntity.setImeino(entity.getImeino());
        onlnblEntity.setUdidno(entity.getUdidno());
        onlnblEntity.setTrands(entity.getTrands());
        onlnblEntity.setServtp(entity.getServtp());
        onlnblEntity.setIntrcd(entity.getIntrcd());
        onlnblEntity.setTransq(entity.getTransq());

		if (E_YES___.NO == hcflag) {
			DpAcctProc.updateDpAcctOnlnbl(onlnblEntity, fcflag, E_YES___.NO);
	       /**
	         * 分布式下跨节点要保全止付
	         */
	        if (BusiTools.getDistributedDeal()) {
	        	//电子账户状态字检查 账户状态为借冻、双冻、银行止付全止、外部止付全止、客户止付的不需要再进行保全止付   红字存入记账不需要保全止付
	    		IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
	    		if (E_YES___.YES == cplAcStatus.getBrfroz() || E_YES___.YES == cplAcStatus.getDbfroz() 
	    				|| E_YES___.YES == cplAcStatus.getBkalsp() || E_YES___.YES == cplAcStatus.getOtalsp()
	    				|| E_YES___.YES == cplAcStatus.getClstop() || CommUtil.compare(tranam, BigDecimal.ZERO) < 0) {
	    			
	    		}else {
	    			IoDpFrozComplexType.IoDpStFzIn frozeInput = SysUtil
	                        .getInstance(IoDpFrozComplexType.IoDpStFzIn.class);
	                frozeInput.setCardno(cardno);
	                frozeInput.setCrcycd(crcycd);
	                frozeInput.setFrozam(tranam);
	                frozeInput.setFroztp(E_FROZTP.AM);
	                SysUtil.getInstance(IoDpFrozSvcType.class).IoDpStFz(frozeInput);
				}
	        }

	       /**
	         * 登记会计流水
	         */
	        IoAccounttingIntf cplIoAccounttingIntf = SysUtil
	                .getInstance(IoAccounttingIntf.class);
	        cplIoAccounttingIntf.setCuacno(custac);
	        cplIoAccounttingIntf.setAcctno(acctno);
	        cplIoAccounttingIntf.setAcseno(acseno);
	        cplIoAccounttingIntf.setToacct(toacct);// 对方账号
	        cplIoAccounttingIntf.setToacna(opacna);// 对方户名
	        cplIoAccounttingIntf.setTobrch(entity.getOpbrch());// 对方账户所属机构
	        cplIoAccounttingIntf.setProdcd(onlnblEntity.getProdcd());
	        cplIoAccounttingIntf.setDtitcd(onlnblEntity.getDtitcd());
	        cplIoAccounttingIntf.setCrcycd(crcycd);
	        cplIoAccounttingIntf.setTranam(tranam);
	        cplIoAccounttingIntf.setAcctdt(onlnblEntity.getAcctdt());// 应入账日期
	        cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
	        cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
	        cplIoAccounttingIntf.setAcctbr(onlnblEntity.getAcctbr());
	        cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR);
	        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
	        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
	        cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE);
	        // 登记交易信息，供总账解析
	        if (CommUtil.equals(
	                "1",
	                KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
	                        true).getParm_value1())) {
	            KnpParameter para = SysUtil.getInstance(KnpParameter.class);
	            para = KnpParameterDao.selectOne_odb1("GlAnalysis", "1010000", "%", "%",
	                    true);
	            cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息 20160701
	                                                             // 产品增加
	        }
	        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
	                cplIoAccounttingIntf);
		} else {
			/*//更改交易事件为热点账户
			event = ApUtil.TRANS_EVENT_HC_TRAN;
			IoChkHotCtrlIn hotinp = SysUtil.getInstance(IoChkHotCtrlIn.class);
			hotinp.setAmntcd(cn.sunline.ltts.busi.sys.type.HcEnumType.E_AMNTCD.C);
			hotinp.setHcacct(acctno);
			hotinp.setHctype(E_HCTYPE.DP);
			hotinp.setTranam(tranam);
			ioHotCtrlSvcType.chkHotCtrl(hotinp);*/
		}
        if (E_YES___.NO != entity.getStrktg()) {
            /**
             * 冲正登记
             */
            IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
            IoMsRegEvent ioMsReg = SysUtil.getInstance(IoMsRegEvent.class);
            
            cplInput.setCustac(custac); // 电子账户号
            cplInput.setTranac(acctno); // 负债账号
            cplInput.setTranev(event); //交易事件
            cplInput.setTranam(tranam);// 交易金额
            cplInput.setAmntcd(E_AMNTCD.CR);// 借贷标志
            cplInput.setCrcycd(crcycd);// 货币代号
            cplInput.setTranno(onlnblEntity.getDetlsq());// 交易序号
            cplInput.setEvent1(CommTools.getBaseRunEnvs().getTrxn_date());
        	
//    		ioMsReg.setCall_out_seq(CommTools.getBaseRunEnvs().getCall_out_seq());//外调流水
//    		ioMsReg.setConfirm_event_id("");//二提交事件id
    		ioMsReg.setEvent_status(E_EVNTST.SUCCESS);//事件状态SUCCESS（成功）STRIKED（已冲正）NEED2C（需要二次提交）
    		ioMsReg.setInformation_value(SysUtil.serialize(cplInput));
        	ioMsReg.setReversal_event_id("strkeDpPostAcct");//冲正事件ID
        	ioMsReg.setService_id("strkeDpPostAcct");//服务ID
    		ioMsReg.setSub_system_id(CoreUtil.getSubSystemId());//子系统ID
//    		ioMsReg.setTarget_dcn(CoreUtil.getCurrentShardingId());//目标DCB编号
//    		ioMsReg.setTarget_org_id("025");//目标法人
    		ioMsReg.setTxn_event_level(E_EVENTLEVEL.LOCAL);//教义事件级别NORMAL（）INQUIRE（）LOCAL（）CRDIT（）
    		ioMsReg.setIs_across_dcn(E_YESORNO.NO);
    		
    		MsEvent.register(ioMsReg, true);
        }

    }

    /**
     * 支取方法
     * 
     * @param entity
     * 
     *        1.活期账户余额变更 2.存入控制登记 3.利息调整处理 4.登记会计流水
     */
    public static void drawAcctDp(DpSaveEntity entity) {
        String cardno = entity.getCardno();
        String acctno = entity.getAcctno();
        String crcycd = entity.getCrcycd();
        if (CommUtil.isNull(crcycd))
        	throw DpModuleError.DpstComm.BNAS1101();
        BigDecimal tranam = entity.getTranam();
        // 检查交易金额
        if (E_YES___.YES != entity.getNegafg()) {
            if (CommUtil.compare(tranam, BigDecimal.ZERO) == 0) {
            	throw DpModuleError.DpstAcct.BNAS0624();
            }
        }

        String custac = entity.getCustac();
        String acseno = entity.getAcseno();
        String toacct = entity.getToacct();
        String opacna = entity.getOpacna();
    	String event = ApUtil.TRANS_EVENT_DPDRAW;

        // 检查是定期产品还是活期产品
        // kna_accs accs = Kna_accsDao.selectOne_odb2(acctno, false);
        // IoCaKnaAccs accs = caqry.kna_accs_selectOne_odb2(acctno, false);
        IoCaKnaAccs accs = DpAcctQryDao.selKnaAccsByAcctno(acctno, false);

        if (CommUtil.isNull(accs))
        	throw DpModuleError.DpstAcct.BNAS1125(custac, acctno);

        if (CommUtil.isNull(accs.getFcflag()))
        	throw DpModuleError.DpstAcct.BNAS0844();
        // 定活标志
        E_FCFLAG fcflag = accs.getFcflag();

        // 是否检查规则标志不为否时，则检查，为否时，则不检查
        if (E_YES___.NO != entity.getIschck()) {

            /**
             * 支取控制检查
             */
        	//暂时取消掉支取控制检查
//            DpProductProc.chkDpDraw(acctno, tranam, fcflag, accs.getProdcd(),
//                    crcycd);
        }

        /**
         * 支取可用金额检查 活期支取，获取可用余额按照活期资金池查询 定期支取，按照子账户余额查询
         * 活期资金池中产品为活期资金池可用余额，非活期资金池可用余额为账户余额
         */
        BigDecimal bal = BigDecimal.ZERO;
        if (E_FCFLAG.CURRENT == fcflag) {

            /**
             * 活期支取冻结检查(只收不付，全额冻结额时，不允许取款) 支取方式不是非智能储蓄存入则检查。 智能储蓄存取取得的是资金池的余额
             * 非智能储蓄存取（子户存取）取得的是子户的可用余额。
             */
            if (entity.getAuacfg() != E_YES___.NO) {

                if (E_YES___.NO != entity.getIschck()
                        && !DpFrozTools.getDrawFg(E_FROZOW.AUACCT, custac))
                	throw DpModuleError.DpstComm.BNAS1576();

                E_YES___ isdfam = E_YES___.YES; // 是否扣除冻结金额
                // 扣划交易不扣除冻结金额
                if (E_YES___.YES == entity.getIsdedu()) {
                    isdfam = E_YES___.NO;
                } else {
                    isdfam = E_YES___.YES;
                }

                /*
                 * // 获取转存签约明细信息 IoCaKnaSignDetl cplkna_sign_detl =
                 * SysUtil.getInstance(IoCaSevQryTableInfo.class)
                 * .kna_sign_detl_selectFirst_odb2(acctno, E_SIGNST.QY, false);
                 * 
                 * // 存在转存签约明细信息则取资金池可用余额 if
                 * (CommUtil.isNotNull(cplkna_sign_detl)) { bal =
                 * DpAcctProc.getProductBal(custac, crcycd, isdfam, true); }
                 * else { // 其他取账户余额,正常的支取交易排除冻结金额 bal =
                 * DpAcctProc.getAcctOnlnblForFrozbl(acctno, isdfam, true); }
                 */

                // 可用余额 addby xiongzhao 20161223
                //add by wenbo 账户保护对于贷款还款特殊处理
                if(CommUtil.compare(entity.getSmrycd(), BusinessConstants.SUMMARY_HK)==0){
                	bal = SysUtil.getInstance(DpAcctSvcType.class).getProductBal(
        					custac, crcycd, false);
                }else{
                	bal = SysUtil.getInstance(DpAcctSvcType.class)
                			.getAcctaAvaBal(custac, acctno, crcycd, isdfam,
                					E_YES___.YES);
                }

            } else {
                bal = DpAcctProc.getAcctOnlnbl(acctno, true);
            }
        } else if (E_FCFLAG.FIX == fcflag) {
            bal = DpAcctProc.getAcctBal(acctno, fcflag);

            /* 获取定期是否明细汇总标志 update by renjinghua in 20150906 */
            // 判断是否为明细汇总,只有定期才有此属性
            KnaFxsv kxsv = KnaFxsvDao.selectOne_odb1(acctno, false);
            if (CommUtil.isNull(kxsv)) {
            	throw DpModuleError.DpstComm.BNAS0841();
            }

            entity.setDetlfg(kxsv.getDetlfg());
        } else {
        	throw DpModuleError.DpstAcct.BNAS0843(fcflag.toString());
        }

        /*
         * Ngblfg必须用[!=]进行比较，默认是空。
         */
        if (entity.getNgblfg() != E_YES___.YES
                && CommUtil.compare(tranam, bal) > 0
                && CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {
            bizlog.debug("可用余额=================[" + bal + "]");
            throw DpModuleError.DpstComm.BNAS0177();
        }

        // 存在利息追缴扣除支取本金，所有处理放在更新余额逻辑之前
        if (E_FCFLAG.FIX == fcflag) {
            // 处理传统定期支取结息，明细汇总类不处理 E_YES___.NO == entity.getDetlfg()
            DpAcctProc.prcDrawCalcin(entity, fcflag);
            if (CommUtil.isNotNull(entity.getPyafamount())) {
                tranam = tranam.subtract(entity.getPyafamount());
                DpKnaFxac.dealPayAfter(entity, entity.getPyafamount());
            }
        }

        /**
         * 修改账户余额
         */
        DpAcctOnlnblEntity onlnblEntity = new DpAcctOnlnblEntity();
        onlnblEntity.setAcctno(acctno);
        onlnblEntity.setAmntcd(E_AMNTCD.DR);
        onlnblEntity.setTranam(tranam);
        onlnblEntity.setCrcycd(crcycd);
        onlnblEntity.setCardno(cardno);
        onlnblEntity.setCustac(custac);
        onlnblEntity.setAcseno(acseno);
        onlnblEntity.setToacct(toacct);
        onlnblEntity.setOpacna(opacna);
        onlnblEntity.setLinkno(entity.getLinkno());
        onlnblEntity.setDetlfg(entity.getDetlfg()); // 是否明细汇总标志
        onlnblEntity.setOpbrch(entity.getOpbrch()); // 对方账户所属机构
        onlnblEntity.setBankcd(entity.getBankcd()); // 对方金融机构
        onlnblEntity.setBankna(entity.getBankna()); // 对方金融机构名称
        onlnblEntity.setSmrycd(entity.getSmrycd()); // 摘要代码
        onlnblEntity.setSmryds(entity.getSmryds()); // 摘要描述
        onlnblEntity.setRemark(entity.getRemark()); // 备注
        onlnblEntity.setDetlsq(entity.getDetlsq());
        onlnblEntity.setMacdrs(entity.getMacdrs());
        onlnblEntity.setTeleno(entity.getTeleno());
        onlnblEntity.setImeino(entity.getImeino());
        onlnblEntity.setUdidno(entity.getUdidno());
        onlnblEntity.setTrands(entity.getTrands());
        onlnblEntity.setServtp(entity.getServtp());
        onlnblEntity.setIntrcd(entity.getIntrcd());
        onlnblEntity.setTransq(entity.getTransq());

        /**
         * mod by xj 20180503 IoHotCtrlSvcType没有服务实现,默认无热点账户
         */
/*        IoHotCtrlSvcType ioHotCtrlSvcType = SysUtil.getInstance(IoHotCtrlSvcType.class);
        E_YES___ hcflag = ioHotCtrlSvcType.selHcpDefn(acctno);*/
        E_YES___ hcflag = E_YES___.NO;
        if (E_YES___.NO == hcflag) {
            DpAcctProc.updateDpAcctOnlnbl(onlnblEntity, fcflag, E_YES___.NO);
            // 处理定期明细表及支取结息
            if (E_FCFLAG.FIX == fcflag) {
                // 处理明细汇总类的定期产品E_YES___.YES == entity.getDetlfg()
                DpAcctProc.prcFxacDetl(acctno, onlnblEntity, tranam);
            }

            /** 登记支取利息会计流水，入账指令 **/
            if (CommUtil.isNotNull(entity.getInstam())
                    && CommUtil.compare(entity.getInstam(), BigDecimal.ZERO) != 0) {

                bizlog.debug("登记利息支取会计流水>>>>>>>>>>>>>>>>>" + entity.getInstam());

                IoAccounttingIntf cplIoAccounttingInrt = SysUtil
                        .getInstance(IoAccounttingIntf.class);
                cplIoAccounttingInrt.setCuacno(custac); // 电子账号
                cplIoAccounttingInrt.setAcctno(acctno); // 账号
                cplIoAccounttingInrt.setAcseno(acseno); // 子户号
                cplIoAccounttingInrt.setProdcd(onlnblEntity.getProdcd()); // 产品编号
                cplIoAccounttingInrt.setDtitcd(onlnblEntity.getDtitcd()); // 核算口径
                cplIoAccounttingInrt.setCrcycd(crcycd); // 币种
                cplIoAccounttingInrt.setTranam(entity.getInstam()); // 利息
                cplIoAccounttingInrt.setAcctdt(onlnblEntity.getAcctdt());// 应入账日期
                cplIoAccounttingInrt.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
                cplIoAccounttingInrt.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
                cplIoAccounttingInrt.setAcctbr(onlnblEntity.getAcctbr()); // 登记账户机构
                cplIoAccounttingInrt.setAmntcd(E_AMNTCD.DR); // 借方
                cplIoAccounttingInrt.setAtowtp(E_ATOWTP.DP); // 存款
                cplIoAccounttingInrt.setTrsqtp(E_ATSQTP.ACCOUNT); // 会计流水类型，账务
                cplIoAccounttingInrt.setBltype(E_BLTYPE.PYIN); // 余额属性利息支出
                // 登记交易信息，供总账解析
                if (CommUtil.equals(
                        "1",
                        KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
                                true).getParm_value1())) {
                    KnpParameter para = SysUtil.getInstance(KnpParameter.class);
                    para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%",
                            "%", true);
                    cplIoAccounttingInrt.setTranms(para.getParm_value1());// 登记交易信息
                                                                     // 20160701
                                                                     // 结息
                }

                SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                        cplIoAccounttingInrt);
            }

            /** 登记支取利息税会计流水，入账指令 **/
            IoAccounttingIntf cplIoAccounttingIntx = SysUtil
                    .getInstance(IoAccounttingIntf.class);
            if (CommUtil.isNotNull(entity.getIntxam())
                    && CommUtil.compare(entity.getIntxam(), BigDecimal.ZERO) != 0) {

                cplIoAccounttingIntx.setCuacno(custac);
                cplIoAccounttingIntx.setAcctno(acctno);
                cplIoAccounttingIntx.setAcseno(acseno);
                cplIoAccounttingIntx.setProdcd(onlnblEntity.getProdcd());
                cplIoAccounttingIntx.setDtitcd(onlnblEntity.getDtitcd());
                cplIoAccounttingIntx.setCrcycd(crcycd);
                cplIoAccounttingIntx.setTranam(entity.getIntxam()); // 利息税
                cplIoAccounttingIntx.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 应入账日期
                cplIoAccounttingIntx.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
                cplIoAccounttingIntx.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); // 账务日期
                cplIoAccounttingIntx.setAcctbr(onlnblEntity.getAcctbr());
                cplIoAccounttingIntx.setAmntcd(E_AMNTCD.CR);
                cplIoAccounttingIntx.setAtowtp(E_ATOWTP.DP);
                cplIoAccounttingIntx.setTrsqtp(E_ATSQTP.ACCOUNT);
                cplIoAccounttingIntx.setBltype(E_BLTYPE.INTAX);
                // 登记交易信息，供总账解析
                if (CommUtil.equals(
                        "1",
                        KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
                                true).getParm_value1())) {
                    KnpParameter para = SysUtil.getInstance(KnpParameter.class);
                    para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3020100", "%",
                            "%", true);
                    cplIoAccounttingIntx.setTranms(para.getParm_value1());// 登记交易信息
                }
                SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                        cplIoAccounttingIntx);

            }
            /**
             * 登记会计流水
             */
            IoAccounttingIntf cplIoAccounttingIntf = SysUtil
                    .getInstance(IoAccounttingIntf.class);
            cplIoAccounttingIntf.setCuacno(custac);
            cplIoAccounttingIntf.setAcctno(acctno);
            cplIoAccounttingIntf.setAcseno(acseno);
            cplIoAccounttingIntf.setToacct(toacct);// 对方账户
            cplIoAccounttingIntf.setToacna(opacna);// 对方名称
            cplIoAccounttingIntf.setTobrch(entity.getOpbrch());// 对方账户所属机构
            cplIoAccounttingIntf.setProdcd(onlnblEntity.getProdcd());
            cplIoAccounttingIntf.setDtitcd(onlnblEntity.getDtitcd());
            cplIoAccounttingIntf.setCrcycd(crcycd);
            cplIoAccounttingIntf.setTranam(tranam);
            cplIoAccounttingIntf.setAcctdt(onlnblEntity.getAcctdt());// 应入账日期
            cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
            cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
            cplIoAccounttingIntf.setAcctbr(onlnblEntity.getAcctbr()); // 登记账户机构
            cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR);
            cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
            cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
            cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE);
            // 登记交易信息，供总账解析
            if (CommUtil.equals(
                    "1",
                    KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
                            true).getParm_value1())) {
                KnpParameter para = SysUtil.getInstance(KnpParameter.class);
                para = KnpParameterDao.selectOne_odb1("GlAnalysis", "1020000", "%", "%",
                        true);
                cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息 20160701
                                                                 // 产品减少
            }

            SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                    cplIoAccounttingIntf);

		} else {
			/**
			 * mod by xj 20180503 IoHotCtrlSvcType没有服务实现,默认无热点账户
			 */
			/*event = ApUtil.TRANS_EVENT_HC_TRAN;
			IoChkHotCtrlIn hotinp = SysUtil.getInstance(IoChkHotCtrlIn.class);
			hotinp.setAmntcd(cn.sunline.ltts.busi.sys.type.HcEnumType.E_AMNTCD.D);
			hotinp.setHcacct(acctno);
			hotinp.setHctype(E_HCTYPE.DP);
			hotinp.setTranam(tranam);
			ioHotCtrlSvcType.chkHotCtrl(hotinp);*/
		}

        if (E_YES___.NO != entity.getStrktg()) {
            /**
             * 冲正登记
             */
            IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
            cplInput.setCustac(custac); // 电子账户号
            cplInput.setTranac(acctno); // 负债账号
            //cplInput.setTranev(event);
            cplInput.setTranam(tranam);// 交易金额
            cplInput.setAmntcd(E_AMNTCD.DR);// 借贷标志
            cplInput.setCrcycd(crcycd);// 货币代号
            cplInput.setTranno(onlnblEntity.getDetlsq());// 交易序号
            
            if (CommUtil.isNotNull(entity.getInstam())) {
                cplInput.setEvent1(String.valueOf(entity.getInstam())); // 支取利息
            }
            if (CommUtil.isNotNull(onlnblEntity.getAcctst())) {
                cplInput.setEvent2(onlnblEntity.getAcctst().getValue()); // 原账户状态
            }
            if (CommUtil.isNotNull(entity.getIntxam())) {
                cplInput.setEvent3(String.valueOf(entity.getIntxam())); // 支取利息
            }
            if (CommUtil.isNotNull(entity.getPyafamount())) {
                cplInput.setEvent4(String.valueOf(entity.getIntxam())); // 追缴金额
                cplInput.setEvent5(String.valueOf(entity.getPydetlsq()));//追缴产生的bill流水号
            }
            
            cplInput.setEvent6(CommTools.getBaseRunEnvs().getTrxn_date());
            
            IoMsRegEvent ioMsReg = SysUtil.getInstance(IoMsRegEvent.class);
    		//ioMsReg.setCall_out_seq(CommTools.getBaseRunEnvs().getCall_out_seq());//外调流水
    		//ioMsReg.setConfirm_event_id("");//二提交事件id
    		ioMsReg.setEvent_status(E_EVNTST.SUCCESS);//事件状态SUCCESS（成功）STRIKED（已冲正）NEED2C（需要二次提交）
    		//ioMsReg.setInformation_value(acctno);//冲正信息值
    		ioMsReg.setInformation_value(SysUtil.serialize(cplInput));
        	ioMsReg.setReversal_event_id("strkeDpDrawAcct");//冲正事件ID
        	ioMsReg.setService_id("strkeDpDrawAcct");//服务ID
    		ioMsReg.setSub_system_id(CoreUtil.getSubSystemId());//子系统ID
    		//ioMsReg.setTarget_dcn(CoreUtil.getCurrentShardingId());//目标DCB编号
    		//ioMsReg.setTarget_org_id("025");//目标法人
    		ioMsReg.setTxn_event_level(E_EVENTLEVEL.LOCAL);//教义事件级别NORMAL（）INQUIRE（）LOCAL（）CRDIT（）
    		ioMsReg.setIs_across_dcn(E_YESORNO.NO);
    		
    		MsEvent.register(ioMsReg, true);
            
        }

        // 更新回原交易法人
//         CommTools.getBaseRunEnvs().setBusi_org_id(corpno);

    }

    /**
     * 开立负债账户对外方法
     * 
     * @param entity
     */
    public static void openAcct(DpOpenAcctEntity entity) {
        // 获取产品编号，如果输入的产品编号为空，则获取客户账号类型默认的产品编号

        String prodcd = null;
        if (CommUtil.isNull(entity.getProdcd())) {
            IoCaStaPublic casta = SysUtil.getInstance(IoCaStaPublic.class);
            prodcd = casta.CaEAccountProc_getProdcd(entity.getCacttp());
        } else {
            prodcd = entity.getProdcd();
        }

        if (CommUtil.isNull(entity.getDepttm()))
        	throw DpModuleError.DpstProd.BNAS1025();

        bizlog.debug("存期============" + entity.getDepttm());

        // 设置开户产品检查输入参数。
        ChkOpenAcctIn cplOpenAcctIn = SysUtil.getInstance(ChkOpenAcctIn.class);

        cplOpenAcctIn.setAcctna(entity.getAcctna());
        cplOpenAcctIn.setCustno(entity.getCustno());
        cplOpenAcctIn.setCustac(entity.getCustac());
        cplOpenAcctIn.setCacttp(entity.getCacttp());
        cplOpenAcctIn.setProdcd(prodcd);
        cplOpenAcctIn.setDepttm(entity.getDepttm());
        cplOpenAcctIn.setCacttp(entity.getCacttp());
        cplOpenAcctIn.setCrcycd(entity.getCrcycd());
        cplOpenAcctIn.setTranam(CommUtil.nvl(entity.getTranam(),
                BigDecimal.ZERO)); // 存入金额

        // ChkOpenAcctOut cplOpenAcctOut =
        // SysUtil.getInstance(ChkOpenAcctOut.class);

        // 检查开户产品信息
        IoDpOpenSub openInfo = DpOpenDefault.chkOpenAcct(cplOpenAcctIn);
        openInfo.getBase().setOpacfg(entity.getOpacfg());
        openInfo.getBase().setCusttp(entity.getCusttp());
        openInfo.getBase().setCustno(entity.getCustno());

        // 首开户时检查产品定活标志，首开户时产品必须是活期
        if (entity.getOpacfg() == E_YES___.YES) {
            if (E_FCFLAG.CURRENT != openInfo.getBase().getPddpfg()) {
            	throw DpModuleError.DpstProd.BNAS0314();
            }
        }

        // 获取产品检查传出的负债账号
        // String acctno = cplOpenAcctOut.getAcctno();
        String acctno = openInfo.getBase().getAcctno();
        AddSubAcctOut openOut = SysUtil.getInstance(AddSubAcctOut.class);
        if (CommUtil.isNull(acctno)) {

            openInfo.getBase().setAcsetp(entity.getAcsetp());

            openOut = OpenSubAcctDeal.OpenSub(openInfo);
            acctno = openOut.getAcctno();
            entity.setPddpfg(openOut.getPddpfg());
            entity.setDebttp(openOut.getDebttp());
            // 开户 负债子账户
            // 设置开户输入参数
            // AddSubAcctIn cplAddSubAcctIn = SysUtil
            // .getInstance(AddSubAcctIn.class);
            // cplAddSubAcctIn.setAcctna(entity.getAcctna());
            // cplAddSubAcctIn.setCustno(entity.getCustno());
            // cplAddSubAcctIn.setCustac(entity.getCustac());
            // cplAddSubAcctIn.setCacttp(entity.getCacttp());
            // cplAddSubAcctIn.setProdcd(prodcd);
            // cplAddSubAcctIn.setDepttm(entity.getDepttm());
            // cplAddSubAcctIn.setCusttp(entity.getCusttp());
            // cplAddSubAcctIn.setOpenir(entity.getOpenir());
            // cplAddSubAcctIn.setCrcycd(entity.getCrcycd());
            // cplAddSubAcctIn.setTranam(entity.getTranam());
            // acctno = OpenCurrAcctProc.openCurrAcct(cplAddSubAcctIn,
            // cplOpenAcctOut);
        } else {
            entity.setPddpfg(openInfo.getBase().getPddpfg());
            entity.setDebttp(openInfo.getDppb().getProdlt()); // 存款种类
        }

        entity.setAcctno(acctno);
        entity.setProdcd(prodcd);

    }

    /**
     * 存入冲正方法
     * 
     * @param entity
     */
    public static void strikePostAcctDp(DpSaveEntity entity) {
        String acctno = entity.getAcctno();
        String custac = entity.getCustac();
        /**
         * 输入项检查
         */
        if (CommUtil.isNull(acctno))
        	throw DpModuleError.DpstComm.BNAS1577();
        if (CommUtil.isNull(custac))
        	throw DpModuleError.DpstComm.BNAS1578();

        // add 20161107 slw 通过电子账户找到对应的法人代码设置到运行环境变量中，先保存原交易法人，服务结束时再更新回去
        IoCaSevQryTableInfo ioCaSerQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
        IoCaKnaAcdc caKnaAcdc = ioCaSerQryTableInfo.getKnaAcdcOdb1(custac,
                E_DPACST.NORMAL, false);

        if (CommUtil.isNull(caKnaAcdc)) {
        	throw DpModuleError.DpstComm.BNAS0740();
        }

        /**
         * 冲正检查
         */
        KnlBill bill = SysUtil.getInstance(KnlBill.class);
        HKnlBill KnlBill = SysUtil.getInstance(HKnlBill.class);

        bill = KnlBillDao.selectOne_odb1(acctno, entity.getDetlsq(),
                entity.getOrtrdt(), false);

        if (CommUtil.isNull(bill) || CommUtil.isNull(bill.getAcctno())) {
            KnlBill = HKnlBillDao.selectOne_odb1(acctno, entity.getDetlsq(),
                    entity.getOrtrdt(), true);
            bill.setAcctbl(KnlBill.getAcctbl());
            bill.setAcctna(KnlBill.getAcctna());
            bill.setAcctno(KnlBill.getAcctno());
            bill.setAcseno(KnlBill.getAcseno());
            bill.setAmntcd(KnlBill.getAmntcd());
            bill.setBankcd(KnlBill.getBankcd());
            bill.setBankna(KnlBill.getBankna());
            bill.setBgindt(KnlBill.getBgindt());
            bill.setCardno(KnlBill.getCardno());
            bill.setCstrfg(KnlBill.getCstrfg());
            bill.setCkaccd(KnlBill.getCkaccd());
            bill.setCorrtg(KnlBill.getCorrtg());
            bill.setCsextg(KnlBill.getCsextg());
            bill.setCustac(KnlBill.getCustac());
            bill.setDcbtno(KnlBill.getDcbtno());
            bill.setDcmttp(KnlBill.getDcmttp());
            bill.setDcsrno(KnlBill.getDcsrno());
            bill.setDetlsq(KnlBill.getDetlsq());
            bill.setIntrcd(KnlBill.getIntrcd());
            bill.setMachdt(KnlBill.getMachdt());
            bill.setMsacdt(KnlBill.getMsacdt());
            bill.setMsacsq(KnlBill.getMsacsq());
            bill.setOpacna(KnlBill.getOpacna());
            bill.setOpcuac(KnlBill.getOpcuac());
            bill.setOpenbr(KnlBill.getOpenbr());
            bill.setOppomk(KnlBill.getOppomk());
            bill.setOrigpq(KnlBill.getOrigpq());
            bill.setPmcrac(KnlBill.getPmcrac());
            bill.setProcsq(KnlBill.getProcsq());
            bill.setRemark(KnlBill.getRemark());
            bill.setServtp(KnlBill.getServtp());
            bill.setSmrycd(KnlBill.getSmrycd());
            bill.setSmryds(KnlBill.getSmryds());
            bill.setStacps(KnlBill.getStacps());
            bill.setStrktp(KnlBill.getStrktp());
            bill.setTranam(KnlBill.getTranam());
            bill.setTranbr(KnlBill.getTranbr());
            bill.setTrancy(KnlBill.getTrancy());
            bill.setTrandt(KnlBill.getTrandt());
            bill.setTransq(KnlBill.getTransq());
            bill.setTrantm(KnlBill.getTrantm());
            bill.setUserid(KnlBill.getUserid());
            bill.setUssqno(KnlBill.getUssqno());
        }

        bizlog.debug("冲正金额==========" + entity.getTranam() + "原交易金额："
                + bill.getTranam());
        if (E_PMCRAC.NORMAL != bill.getPmcrac())
        	throw DpModuleError.DpstComm.BNAS1579();
        if (CommUtil.compare(entity.getTranam(), bill.getTranam()) != 0)
        	throw DpModuleError.DpstComm.BNAS1580();
        if (bill.getAmntcd() != entity.getOramnt())
        	throw DpModuleError.DpstComm.BNAS1581();

        // 现金不允许隔日冲账
        bizlog.debug("是否现金交易：" + bill.getCstrfg());
        if (E_CSTRFG.CASH == bill.getCstrfg()) {
            // 是否隔日
            if (CommUtil.compare(CommTools.getBaseRunEnvs().getTrxn_date(),
                    entity.getOrtrdt()) != 0)
            	throw DpModuleError.DpstComm.BNAS1582();
        }

        // 检查是定期产品还是活期产品
        // kna_accs accs = Kna_accsDao.selectOne_odb2(acctno, false);
        IoCaSevQryTableInfo caqry = SysUtil
                .getInstance(IoCaSevQryTableInfo.class);
        IoCaKnaAccs accs = caqry.getKnaAccsOdb2(acctno, false);
        if (CommUtil.isNull(accs))
        	throw DpModuleError.DpstAcct.BNAS1125(entity.getCustac(), acctno);

        if (CommUtil.isNull(accs.getFcflag()))
        	throw DpModuleError.DpstAcct.BNAS0844();
        // 定活标志
        E_FCFLAG fcflag = accs.getFcflag();

        /**
         * 支取可用金额检查 活期支取，获取可用余额按照活期资金池查询 定期支取，按照子账户余额查询
         */
        BigDecimal bal = BigDecimal.ZERO;
        if (E_FCFLAG.CURRENT == fcflag) {

            /*
             * IoCaKnaSignDetl cplkna_sign_detl =
             * SysUtil.getInstance(IoCaSevQryTableInfo.class)
             * .kna_sign_detl_selectFirst_odb2(acctno, E_SIGNST.QY, false);
             * 
             * // 存在转存签约明细信息则取资金池可用余额 if (CommUtil.isNotNull(cplkna_sign_detl))
             * { bal = DpAcctProc.getProductBal(custac, entity.getCrcycd(),
             * E_YES___.NO, true); } else { // 其他取账户余额,正常的支取交易排除冻结金额 bal =
             * DpAcctProc.getAcctOnlnblForFrozbl(acctno, E_YES___.NO, true); }
             */

            // 可用余额 add by xiongzhao 2016/12/22
            // 冲正不扣减冻结金额 modify by chenlk 20161229
            bal = SysUtil.getInstance(DpAcctSvcType.class).getAcctaAvaBal(
                    custac, acctno, entity.getCrcycd(), E_YES___.YES, E_YES___.NO);

        } else if (E_FCFLAG.FIX == fcflag) {
            bal = DpAcctProc.getAcctBal(acctno, fcflag);

            /* 获取定期是否明细汇总标志 update by renjinghua in 20150906 */
            // 判断是否为明细汇总,只有定期才有此属性
            KnaFxsv kxsv = KnaFxsvDao.selectOne_odb1(acctno, false);
            if (CommUtil.isNull(kxsv)) {
            	throw DpModuleError.DpstComm.BNAS0841();
            }

            entity.setDetlfg(kxsv.getDetlfg());
        } else {
        	throw DpModuleError.DpstAcct.BNAS0843(fcflag.toString());
        }
        bizlog.debug("账户余额===========" + bal);
        if (CommUtil.compare(entity.getTranam(), bal) > 0) {
        	throw DpModuleError.DpstComm.BNAS0177();
        }

        /**
         * Modified by wanggl 2015-08-28 13：44 解决问题：隔日冲正未更新历史明细表记录状态
         */
        // mdy by zhanga 将日期对比改为判断KnlBill是否非空
        // if (CommUtil.compare(entity.getOrtrdt(),
        // CommTools.getBaseRunEnvs().getTrxn_date()) < 0) {
        if (CommUtil.isNotNull(KnlBill)
                && CommUtil.isNotNull(KnlBill.getAcctno())) {
            KnlBill.setPmcrac(E_PMCRAC.TOSTRIK);
            KnlBill.setStacps(entity.getStacps());// 冲正冲账分类
            KnlBill.setCorrtg(E_CORRTG._1);// 设置抹账标志
            HKnlBillDao.updateOne_odb1(KnlBill);
        } else {
            // 修改原余额变更账单
            bill.setPmcrac(E_PMCRAC.TOSTRIK);// 冲补账标志
            bill.setStacps(entity.getStacps());// 冲正冲账分类
            bill.setCorrtg(E_CORRTG._1);// 设置抹账标志
            KnlBillDao.updateOne_odb1(bill);
        }

        /**
         * 修改账户余额
         */
        BigDecimal tranam = BigDecimal.ZERO;
        E_AMNTCD amntcd = null;

        DpAcctOnlnblEntity onlnblEntity = new DpAcctOnlnblEntity();
        // 同向红字记账
        if (E_COLOUR.RED == entity.getColrfg()) {
            tranam = entity.getTranam().negate();// 交易金额
            amntcd = entity.getOramnt();// 借贷标志
        } else if (E_COLOUR.BLUE == entity.getColrfg()) {
            // 反向蓝字记
            tranam = entity.getTranam();// 交易金额
            if (entity.getOramnt() == E_AMNTCD.CR) {
                amntcd = E_AMNTCD.DR; // 借贷标志
            } else {
                amntcd = E_AMNTCD.CR;// 借贷标志
            }
        } else {
        	throw DpModuleError.DpstComm.BNAS1583(entity.getColrfg().toString());
        }

        onlnblEntity.setAcctno(acctno);
        onlnblEntity.setCrcycd(entity.getCrcycd());
        onlnblEntity.setTranam(tranam);
        onlnblEntity.setAmntcd(amntcd);
        onlnblEntity.setCardno(bill.getCardno());
        onlnblEntity.setCustac(custac);
        onlnblEntity.setAcseno(bill.getAcseno());
        onlnblEntity.setToacct(bill.getOpcuac());
        onlnblEntity.setOpacna(bill.getOpacna());
        // 原交易日期
        onlnblEntity.setOrtrdt(entity.getOrtrdt());
        // 原主交易流水
        onlnblEntity.setOrtrsq(bill.getTransq());
        // 原业务流水
        onlnblEntity.setOrigpq(bill.getProcsq());
        // 原柜员流水
        onlnblEntity.setOrigaq(bill.getUssqno());
        // 是否明细标志
        onlnblEntity.setDetlfg(entity.getDetlfg());
        // 20170809新增
        onlnblEntity.setSmrycd(bill.getSmrycd());
        onlnblEntity.setSmryds(bill.getSmryds());
        // 流水生成
        onlnblEntity.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));

        onlnblEntity.setRemark("存入冲正");

        DpAcctProc.updateDpAcctOnlnbl(onlnblEntity, fcflag, E_YES___.YES);

        /**
         * 存入控制变更
         */
        /**E_PLANFG planfg = E_PLANFG.NOSET;
        E_POSTWY postwy = null;
        KnaSave tblSave = SysUtil.getInstance(KnaSave.class);
        KnaFxsv tblFxsv = SysUtil.getInstance(KnaFxsv.class);
        KnaSavePlan tblSavepl = SysUtil.getInstance(KnaSavePlan.class);
        KnaFxsvPlan tblFxsvpl = SysUtil.getInstance(KnaFxsvPlan.class);

        if (fcflag == E_FCFLAG.CURRENT) { // 活期
            tblSave = KnaSaveDao.selectOne_odb1(acctno, false);
            planfg = tblSave.getPlanfg();
            postwy = tblSave.getPostwy();
            tblSave.setResvnm(tblSave.getResvnm() - 1); // 实际存入次数减少
            tblSave.setResvam(tblSave.getResvam().subtract(tranam)); // 实际存入金额减少

            if (planfg == E_PLANFG.SET) {
                long seqnum = DpSaveDrawDao.selKnaSavePlanMaxSeqno(CommTools.getBaseRunEnvs().getBusi_org_id(), acctno, true);
                tblSavepl = KnaSavePlanDao.selectOne_odb1(acctno, seqnum, true);
            }

        } else if (fcflag == E_FCFLAG.FIX) { // 定期
            tblFxsv = KnaFxsvDao.selectOne_odb1(acctno, true);
            planfg = tblFxsv.getPlanfg();
            postwy = tblFxsv.getPostwy();
            if (planfg == E_PLANFG.SET) {
                long seqnum = DpSaveDrawDao.selKnaFxsvPlanMaxSeqno(CommTools.getBaseRunEnvs().getBusi_org_id(), acctno, true);
                tblFxsvpl = KnaFxsvPlanDao.selectOne_odb1(acctno, seqnum, true);
            }
        } else {
        	throw DpModuleError.DpstAcct.BNAS0843(fcflag.toString());
        }

        if (planfg == E_PLANFG.SET) {

            if (fcflag == E_FCFLAG.CURRENT) {
                tblSavepl.setResvam(tblSavepl.getResvam().subtract(tranam)); // 金额
                tblSavepl.setResvnm(tblSavepl.getResvnm() - 1); // 次数
                if (postwy == E_POSTWY.AMCL) { // 控制金额
                    if (CommUtil.compare(tblSavepl.getResvam(),
                            tblSavepl.getPlmony()) >= 0) { // 实际金额大于计划金额
                        tblSavepl.setSpbkfg(E_YES___.NO);
                        tblSavepl.setPlstat(E_PLSTAT.DLSU);
                    } else if (CommUtil.compare(tblSavepl.getResvam(),
                            tblSavepl.getPlmony()) < 0
                            && CommUtil.compare(tblSavepl.getResvam(),
                                    BigDecimal.ZERO) > 0) {
                        tblSavepl.setSpbkfg(E_YES___.NO);
                        tblSavepl.setPlstat(E_PLSTAT.DLNG);
                    } else {
                        tblSavepl.setSpbkfg(E_YES___.NO);
                        tblSavepl.setPlstat(E_PLSTAT.NODL);
                    }
                } else if (postwy == E_POSTWY.TMCL) {
                    if (tblSavepl.getResvnm() >= tblSavepl.getPltime()) {
                        tblSavepl.setSpbkfg(E_YES___.NO);
                        tblSavepl.setPlstat(E_PLSTAT.DLSU);
                    } else if (tblSavepl.getResvnm() < tblSavepl.getPltime()
                            && tblSavepl.getResvnm() > 0) {
                        tblSavepl.setSpbkfg(E_YES___.NO);
                        tblSavepl.setPlstat(E_PLSTAT.DLNG);
                    } else {
                        tblSavepl.setSpbkfg(E_YES___.NO);
                        tblSavepl.setPlstat(E_PLSTAT.NODL);
                    }
                } else if (postwy == E_POSTWY.ATMC) {
                    if (CommUtil.compare(tblSavepl.getResvam(),
                            tblSavepl.getPlmony()) >= 0
                            && tblSavepl.getResvnm() >= tblSavepl.getPltime()) {
                        tblSavepl.setSpbkfg(E_YES___.NO);
                        tblSavepl.setPlstat(E_PLSTAT.DLSU);
                    } else if ((CommUtil.compare(tblSavepl.getResvam(),
                            tblSavepl.getPlmony()) < 0 && CommUtil.compare(
                            tblSavepl.getResvam(), BigDecimal.ZERO) > 0)
                            || (tblSavepl.getResvnm() < tblSavepl.getPltime() && tblSavepl
                                    .getResvnm() > 0)) {
                        tblSavepl.setSpbkfg(E_YES___.NO);
                        tblSavepl.setPlstat(E_PLSTAT.DLNG);
                    } else {
                        tblSavepl.setSpbkfg(E_YES___.NO);
                        tblSavepl.setPlstat(E_PLSTAT.NODL);
                    }
                } // end 存入控制方法
                  // =======处理存入控制中的计划违约标志=========
                long count = DpSaveDrawDao.selKnaSavePlanDpbkfgCounts(false);
                if (count > 0) {
                    tblSave.setSpbkfg(E_YES___.YES);
                } else {
                    tblSave.setSpbkfg(E_YES___.NO);
                }

                KnaSavePlanDao.updateOne_odb1(tblSavepl);

            } else if (fcflag == E_FCFLAG.FIX) {

                tblFxsvpl.setResvam(tblFxsvpl.getResvam().subtract(tranam)); // 金额
                tblFxsvpl.setResvnm(tblFxsvpl.getResvnm() - 1); // 次数
                if (postwy == E_POSTWY.AMCL) { // 控制金额
                    if (CommUtil.compare(tblFxsvpl.getResvam(),
                            tblFxsvpl.getPlmony()) >= 0) { // 实际金额大于计划金额
                        tblFxsvpl.setSpbkfg(E_YES___.NO);
                        tblFxsvpl.setPlstat(E_PLSTAT.DLSU);
                    } else if (CommUtil.compare(tblFxsvpl.getResvam(),
                            tblFxsvpl.getPlmony()) < 0
                            && CommUtil.compare(tblFxsvpl.getResvam(),
                                    BigDecimal.ZERO) > 0) {
                        tblFxsvpl.setSpbkfg(E_YES___.NO);
                        tblFxsvpl.setPlstat(E_PLSTAT.DLNG);
                    } else {
                        tblFxsvpl.setSpbkfg(E_YES___.NO);
                        tblFxsvpl.setPlstat(E_PLSTAT.NODL);
                    }
                } else if (postwy == E_POSTWY.TMCL) {
                    if (tblFxsvpl.getResvnm() >= tblFxsvpl.getPltime()) {
                        tblFxsvpl.setSpbkfg(E_YES___.NO);
                        tblFxsvpl.setPlstat(E_PLSTAT.DLSU);
                    } else if (tblFxsvpl.getResvnm() < tblFxsvpl.getPltime()
                            && tblFxsvpl.getResvnm() > 0) {
                        tblFxsvpl.setSpbkfg(E_YES___.NO);
                        tblFxsvpl.setPlstat(E_PLSTAT.DLNG);
                    } else {
                        tblFxsvpl.setSpbkfg(E_YES___.NO);
                        tblFxsvpl.setPlstat(E_PLSTAT.NODL);
                    }
                } else if (postwy == E_POSTWY.ATMC) {
                    if (CommUtil.compare(tblFxsvpl.getResvam(),
                            tblFxsvpl.getPlmony()) >= 0
                            && tblFxsvpl.getResvnm() >= tblFxsvpl.getPltime()) {
                        tblFxsvpl.setSpbkfg(E_YES___.NO);
                        tblFxsvpl.setPlstat(E_PLSTAT.DLSU);
                    } else if ((CommUtil.compare(tblFxsvpl.getResvam(),
                            tblFxsvpl.getPlmony()) < 0 && CommUtil.compare(
                            tblFxsvpl.getResvam(), BigDecimal.ZERO) > 0)
                            || (tblFxsvpl.getResvnm() < tblFxsvpl.getPltime() && tblFxsvpl
                                    .getResvnm() > 0)) {
                        tblFxsvpl.setSpbkfg(E_YES___.NO);
                        tblFxsvpl.setPlstat(E_PLSTAT.DLNG);
                    } else {
                        tblFxsvpl.setSpbkfg(E_YES___.NO);
                        tblFxsvpl.setPlstat(E_PLSTAT.NODL);
                    }
                } // end 存入控制方法
                  // =======处理存入控制中的计划违约标志=========
                long count = DpSaveDrawDao.selKnaFxsvPlanDpbkfgCounts(false);
                if (count > 0) {
                    tblSave.setSpbkfg(E_YES___.YES);
                } else {
                    tblSave.setSpbkfg(E_YES___.NO);
                }
                // 更新定期存入计划表
                KnaFxsvPlanDao.updateOne_odb1(tblFxsvpl);
            }

        }
        // 更新存入控制表
        KnaSaveDao.updateOne_odb1(tblSave);
		**/
        // 利息调整 TODO

        /**
         * 登记会计流水
         */
        IoAccounttingIntf cplIoAccounttingIntf = SysUtil
                .getInstance(IoAccounttingIntf.class);
        cplIoAccounttingIntf.setCuacno(custac);
        cplIoAccounttingIntf.setAcctno(acctno);
        cplIoAccounttingIntf.setAcseno(bill.getAcseno());
        cplIoAccounttingIntf.setProdcd(onlnblEntity.getProdcd());
        cplIoAccounttingIntf.setDtitcd(onlnblEntity.getDtitcd());
        cplIoAccounttingIntf.setCrcycd(entity.getCrcycd());
        cplIoAccounttingIntf.setTranam(tranam);
        cplIoAccounttingIntf.setAcctdt(onlnblEntity.getAcctdt());// 应入账日期
        cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
        cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
        cplIoAccounttingIntf.setAcctbr(onlnblEntity.getAcctbr());
        cplIoAccounttingIntf.setAmntcd(amntcd);
        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
        cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE);
        // 登记交易信息，供总账解析
        if (CommUtil.equals(
                "1",
                KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
                        true).getParm_value1())) {
            KnpParameter para = SysUtil.getInstance(KnpParameter.class);
            para = KnpParameterDao.selectOne_odb1("GlAnalysis", "1010000", "%", "%",
                    true);
            cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息 20160701
                                                             // 产品增加
        }
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                cplIoAccounttingIntf);

        // add 20161107 slw 通过电子账户找到对应的法人代码设置到运行环境变量中，先保存原交易法人，服务结束时再更新回去
        //		CommTools.getBaseRunEnvs().setBusi_org_id(corpno);

    }

    /**
     * 支取冲正方法
     * 
     * @param entity
     */
    public static void strikeDrawAcctDp(DpSaveEntity entity) {
        String acctno = entity.getAcctno();
        String custac = entity.getCustac();
        /**
         * 输入项检查
         */
        if (CommUtil.isNull(acctno))
        	throw DpModuleError.DpstComm.BNAS1577();
        if (CommUtil.isNull(custac))
        	throw DpModuleError.DpstComm.BNAS1578();

        // add 20161107 slw 通过电子账户找到对应的法人代码设置到运行环境变量中，先保存原交易法人，服务结束时再更新回去
        IoCaSevQryTableInfo ioCaSerQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
        IoCaKnaAcdc caKnaAcdc = ioCaSerQryTableInfo.getKnaAcdcOdb1(custac,
                E_DPACST.NORMAL, false);

        if (CommUtil.isNotNull(caKnaAcdc)) {

            // CommTools.getBaseRunEnvs().setBusi_org_id(caKnaAcdc.getCorpno());
        } else {
        	throw DpModuleError.DpstComm.BNAS0740();
        }

        String tmstmp = DateTools2.getCurrentTimestamp();

        /**
         * 冲正检查
         */
        KnlBill bill = SysUtil.getInstance(KnlBill.class);
        HKnlBill KnlBill = SysUtil.getInstance(HKnlBill.class);
        bill = KnlBillDao.selectOne_odb1(acctno, entity.getDetlsq(),
                entity.getOrtrdt(), false);

        if (CommUtil.isNull(bill) || CommUtil.isNull(bill.getAcctno())) {

            KnlBill = HKnlBillDao.selectOne_odb1(acctno, entity.getDetlsq(),
                    entity.getOrtrdt(), true);
            bill.setAcctbl(KnlBill.getAcctbl());
            bill.setAcctna(KnlBill.getAcctna());
            bill.setAcctno(KnlBill.getAcctno());
            bill.setAcseno(KnlBill.getAcseno());
            bill.setAmntcd(KnlBill.getAmntcd());
            bill.setBankcd(KnlBill.getBankcd());
            bill.setBankna(KnlBill.getBankna());
            bill.setBgindt(KnlBill.getBgindt());
            bill.setCardno(KnlBill.getCardno());
            bill.setCstrfg(KnlBill.getCstrfg());
            bill.setCkaccd(KnlBill.getCkaccd());
            bill.setCorrtg(KnlBill.getCorrtg());
            bill.setCsextg(KnlBill.getCsextg());
            bill.setCustac(KnlBill.getCustac());
            bill.setDcbtno(KnlBill.getDcbtno());
            bill.setDcmttp(KnlBill.getDcmttp());
            bill.setDcsrno(KnlBill.getDcsrno());
            bill.setDetlsq(KnlBill.getDetlsq());
            bill.setIntrcd(KnlBill.getIntrcd());
            bill.setMachdt(KnlBill.getMachdt());
            bill.setMsacdt(KnlBill.getMsacdt());
            bill.setMsacsq(KnlBill.getMsacsq());
            bill.setOpacna(KnlBill.getOpacna());
            bill.setOpcuac(KnlBill.getOpcuac());
            bill.setOpenbr(KnlBill.getOpenbr());
            bill.setOppomk(KnlBill.getOppomk());
            bill.setOrigpq(KnlBill.getOrigpq());
            bill.setPmcrac(KnlBill.getPmcrac());
            bill.setProcsq(KnlBill.getProcsq());
            bill.setRemark(KnlBill.getRemark());
            bill.setServtp(KnlBill.getServtp());
            bill.setSmrycd(KnlBill.getSmrycd());
            bill.setSmryds(KnlBill.getSmryds());
            bill.setStacps(KnlBill.getStacps());
            bill.setStrktp(KnlBill.getStrktp());
            bill.setTranam(KnlBill.getTranam());
            bill.setTranbr(KnlBill.getTranbr());
            bill.setTrancy(KnlBill.getTrancy());
            bill.setTrandt(KnlBill.getTrandt());
            bill.setTransq(KnlBill.getTransq());
            bill.setTrantm(KnlBill.getTrantm());
            bill.setUserid(KnlBill.getUserid());
            bill.setUssqno(KnlBill.getUssqno());

        }

        if (E_PMCRAC.NORMAL != bill.getPmcrac())
        	throw DpModuleError.DpstComm.BNAS1579();
        if (CommUtil.compare(entity.getTranam(), bill.getTranam()) != 0)
        	throw DpModuleError.DpstComm.BNAS1580();
        if (bill.getAmntcd() != entity.getOramnt())
        	throw DpModuleError.DpstComm.BNAS1581();

        // 现金不允许隔日冲账
        if (E_CSTRFG.CASH == bill.getCstrfg()) {
            // 是否隔日
            if (CommUtil.compare(CommTools.getBaseRunEnvs().getTrxn_date(),
                    entity.getOrtrdt()) != 0)
            	throw DpModuleError.DpstComm.BNAS1582();
        }

        // 检查是定期产品还是活期产品
        // kna_accs accs = Kna_accsDao.selectOne_odb2(acctno, false);
        IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
        IoCaKnaAccs accs = caqry.getKnaAccsOdb2(acctno, false);
        if (CommUtil.isNull(accs))
        	throw DpModuleError.DpstAcct.BNAS1125(entity.getCustac(), acctno);

        if (CommUtil.isNull(accs.getFcflag()))
        	throw DpModuleError.DpstAcct.BNAS0844();
        // 定活标志
        E_FCFLAG fcflag = accs.getFcflag();

        BigDecimal bal = BigDecimal.ZERO;
        if (E_FCFLAG.CURRENT == fcflag) {

            /*
             * IoCaKnaSignDetl cplkna_sign_detl =
             * SysUtil.getInstance(IoCaSevQryTableInfo.class)
             * .kna_sign_detl_selectFirst_odb2(acctno, E_SIGNST.QY, false);
             * 
             * // 存在转存签约明细信息则取资金池可用余额 if (CommUtil.isNotNull(cplkna_sign_detl))
             * { bal = DpAcctProc.getProductBal(custac, entity.getCrcycd(),
             * E_YES___.NO, true); } else { // 其他取账户余额,正常的支取交易排除冻结金额 bal =
             * DpAcctProc.getAcctOnlnblForFrozbl(acctno, E_YES___.NO, true); }
             */

            // 可用余额 add by xiongzhao 2016/12/22
            // 冲正不扣减冻结金额 modify by chenlk 20161229
            bal = SysUtil.getInstance(DpAcctSvcType.class).getAcctaAvaBal(
                    custac, acctno, entity.getCrcycd(), E_YES___.NO,
                    E_YES___.YES);

        } else if (E_FCFLAG.FIX == fcflag) {
            bal = DpAcctProc.getAcctBal(acctno, fcflag);

            /* 获取定期是否明细汇总标志 update by renjinghua in 20150906 */
            // 判断是否为明细汇总,只有定期才有此属性
            KnaFxsv kxsv = KnaFxsvDao.selectOne_odb1(acctno, false);
            if (CommUtil.isNull(kxsv)) {
            	throw DpModuleError.DpstComm.BNAS0841();
            }

            entity.setDetlfg(kxsv.getDetlfg());
        } else {
        	throw DpModuleError.DpstAcct.BNAS0843(fcflag.toString());
        }
        bizlog.debug("账户余额===========" + bal);
        if (CommUtil.compare(entity.getTranam().add(bal), BigDecimal.ZERO) < 0) {
        	throw DpModuleError.DpstComm.BNAS0177();
        }

        // 修改原余额变更账单
        /**
         * Modified by wanggl 2015-08-28 13：44 解决问题：隔日冲正未更新历史明细表记录状态
         */

        // mdy by zhanga 将日期对比改为判断KnlBill是否非空
        if (CommUtil.isNotNull(KnlBill)
                && CommUtil.isNotNull(KnlBill.getAcctno())) {
            KnlBill.setPmcrac(E_PMCRAC.TOSTRIK);
            KnlBill.setStacps(entity.getStacps());// 冲正冲账分类
            KnlBill.setCorrtg(E_CORRTG._1);// 设置抹账标志
            HKnlBillDao.updateOne_odb1(KnlBill);

        } else {
            // 修改原余额变更账单
            bill.setPmcrac(E_PMCRAC.TOSTRIK);// 冲补账标志
            bill.setStacps(entity.getStacps());// 冲正冲账分类
            bill.setCorrtg(E_CORRTG._1);// 设置抹账标志
            KnlBillDao.updateOne_odb1(bill);
        }

        /**
         * 修改账户余额
         */
        BigDecimal onlnbl = BigDecimal.ZERO;
        BigDecimal instam = BigDecimal.ZERO;
        BigDecimal intxam = BigDecimal.ZERO;
        E_AMNTCD amntcd = null;
        DpAcctOnlnblEntity onlnblEntity = new DpAcctOnlnblEntity();
        // 同向红字记账
        if (E_COLOUR.RED == entity.getColrfg()) {
            onlnbl = entity.getTranam().negate();// 交易金额
            //20170809新增判断
            if (!CommUtil.isNull(entity.getInstam())) {
                instam = entity.getInstam().negate(); // 利息
            }
            if (!CommUtil.isNull(entity.getIntxam())) {
                intxam = entity.getIntxam().negate();// 利息税
            }
            amntcd = entity.getOramnt();// 借贷标志
        } else if (E_COLOUR.BLUE == entity.getColrfg()) {
            // 反向蓝字记
            onlnbl = entity.getTranam(); // 交易金额
            if (!CommUtil.isNull(entity.getInstam())) {

                instam = entity.getInstam().negate(); // 利息
            }
            if (!CommUtil.isNull(entity.getIntxam())) {
                intxam = entity.getIntxam().negate();// 利息税
            }

            if (entity.getOramnt() == E_AMNTCD.CR) {
                amntcd = E_AMNTCD.DR; // 借贷标志
            } else {
                amntcd = E_AMNTCD.CR; // 借贷标志
            }
        } else {
        	throw DpModuleError.DpstComm.BNAS1583(entity.getColrfg().toString());
        }

        // add liaojc
        // 亲情钱包支付后退款时，从亲情钱包支付的相应款项退回亲情钱包子账户，如果该亲情钱包已关闭的，退款直接转入创建人的个人结算主账户
        if (E_FCFLAG.CURRENT == fcflag) {
            // 获取账户信息
            KnaAcct acct = KnaAcctDao.selectOneWithLock_odb1(acctno, true);

            // 亲情钱包
            if (E_ACSETP.FW == acct.getAcsetp()
                    || E_ACSETP.MA == acct.getAcsetp()) {
                if (acct.getAcctst() == E_DPACST.CLOSE) {// 销户
                    // 更新为结算户负债账号，入账活期结算户。后面变更账户余额后再更新回来
                    acctno = CapitalTransDeal.getSettKnaAcctAc(custac)
                            .getAcctno();
                }
            }
        }

        onlnblEntity.setAcctno(acctno);
        onlnblEntity.setCrcycd(entity.getCrcycd());
        onlnblEntity.setTranam(onlnbl);// 交易金额
        onlnblEntity.setAmntcd(amntcd);
        onlnblEntity.setCardno(bill.getCardno());
        onlnblEntity.setCustac(custac);
        onlnblEntity.setAcseno(bill.getAcseno());
        onlnblEntity.setToacct(bill.getOpcuac());
        onlnblEntity.setOpacna(bill.getOpacna());
        // 原交易日期
        onlnblEntity.setOrtrdt(entity.getOrtrdt());
        // 原主交易流水
        onlnblEntity.setOrtrsq(bill.getTransq());
        // 原业务流水
        onlnblEntity.setOrigpq(bill.getProcsq());
        // 原柜员流水
        onlnblEntity.setOrigaq(bill.getUssqno());
        // 摘要码20170809--update
        //onlnblEntity.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
        onlnblEntity.setSmrycd(bill.getSmrycd());
        onlnblEntity.setSmryds(bill.getSmryds());
        onlnblEntity.setRemark("支取冲正");
        // 账户状态
        if (CommUtil.isNotNull(entity.getAcctst())) {
            onlnblEntity.setAcctst(entity.getAcctst());
        }

        DpAcctProc.updateDpAcctOnlnbl(onlnblEntity, fcflag, E_YES___.YES);

        // 负债账号更新为原账号
        acctno = entity.getAcctno();

        /**
         * 支取控制变更
         */
        E_YES___ setpwy = null;// 是否设置支取计划
        KnaDraw tblKnaDraw = null;// 负债活期账户支取控制
        KnaFxdr tblKnaFxdr = null;// 负债定期账户支取控制
        KnaDrawPlan tblKnaDrawPlan = null;// 债活期账户支取计划表
        KnaFxdrPlan tblKnaFxdrPlan = null;// 债账户支取计划表
        E_CTRLWY ctrlwy = null;

        if (E_FCFLAG.CURRENT == fcflag) {
            // 获取负债活期账户支取控制信息
            tblKnaDraw = KnaDrawDao.selectOne_odb1(acctno, false);
            if (CommUtil.isNull(tblKnaDraw)) {
            	throw CaError.Eacct.BNAS0667();
            }
            setpwy = tblKnaDraw.getSetpwy();// 是否设置支取计划
            ctrlwy = tblKnaDraw.getCtrlwy();// 支取控制方法
            tblKnaDraw.setRedqam(tblKnaDraw.getRedqam().subtract(
                    entity.getTranam()));
            tblKnaDraw.setRedwnm(tblKnaDraw.getRedwnm() - 1);
            KnaDrawDao.updateOne_odb1(tblKnaDraw);// 更新活期支取控制表
            if (E_YES___.YES == setpwy) {// 如果设置了支取计划，则按计划控制
                Long maxSeqnum = DpSaveDrawDao.selKnaDrawPlanMaxSeqno(CommTools.getBaseRunEnvs().getBusi_org_id(), acctno, true);
                tblKnaDrawPlan = KnaDrawPlanDao.selectOne_odb1(acctno,
                        maxSeqnum, true);
            }
        } else if (E_FCFLAG.FIX == fcflag) {
            // 获取负债定期账户支取控制信息
            bizlog.debug("负债账号=============" + acctno);
            tblKnaFxdr = KnaFxdrDao.selectOne_odb1(acctno, false);
            if (CommUtil.isNull(tblKnaFxdr)) {
            	throw CaError.Eacct.BNAS0840();
            }
            ctrlwy = tblKnaFxdr.getCtrlwy();// 支取控制方法
            setpwy = tblKnaFxdr.getSetpwy();// 是否设置支取计划
            tblKnaFxdr.setRedqam(tblKnaFxdr.getRedqam().subtract(
                    entity.getTranam()));
            tblKnaFxdr.setRedwnm(tblKnaFxdr.getRedwnm() - 1);
            KnaFxdrDao.updateOne_odb1(tblKnaFxdr);// 更新定期支取控制表
            if (E_YES___.YES == setpwy) {// 如果设置了支取计划，则按计划控制
                Long maxSeqnum = DpSaveDrawDao.selKnaFxdrPlanMaxSeqno(CommTools.getBaseRunEnvs().getBusi_org_id(), acctno, true);
                tblKnaFxdrPlan = KnaFxdrPlanDao.selectOne_odb1(acctno,
                        maxSeqnum, true);

            }
        } else {
        	throw DpModuleError.DpstAcct.BNAS0843(fcflag.toString());
        }
        /**
         * 支取计划登记
         */
        if (E_YES___.YES == setpwy) {
            if (E_FCFLAG.CURRENT == fcflag) { // 活期支取计划
                tblKnaDrawPlan.setRedqam(tblKnaDrawPlan.getRedqam().subtract(
                        entity.getTranam()));
                tblKnaDrawPlan.setRedwnm(tblKnaDrawPlan.getRedwnm() - 1);
                /*-----------------------违约处理-----------------begin--------------*/
                if (ctrlwy == E_CTRLWY.TMCL) {// 支取方式控制 按次数控制
                    if (tblKnaDrawPlan.getRedwnm() <= tblKnaDrawPlan
                            .getPltime()) {// 实际支取次数大于计划总数-违约
                        tblKnaDrawPlan.setDpbkfg(E_YES___.NO);
                    }
                } else if (ctrlwy == E_CTRLWY.AMCL) {// 支取方式控制 按金额控制
                    if (CommUtil.compare(tblKnaDrawPlan.getRedqam(),
                            tblKnaDrawPlan.getPlmony()) < 0) {// 实际支取次数大于计划总数-违约
                        tblKnaDrawPlan.setDpbkfg(E_YES___.NO);
                    }
                } else if (ctrlwy == E_CTRLWY.ATML) {// 支取方式控制 按次数和金额控制
                    if (CommUtil.compare(tblKnaDrawPlan.getRedqam(),
                            tblKnaDrawPlan.getPlmony()) < 0
                            && tblKnaDrawPlan.getRedwnm() <= tblKnaDrawPlan
                                    .getPltime()) {// 实际支取次数大于计划总数且实际支取次数大于计划总数-违约
                        tblKnaDrawPlan.setDpbkfg(E_YES___.NO);
                    }
                }
                /*-----------------------违约处理-----------------end--------------*/
                Long dpbkfgCounts = DpSaveDrawDao
                        .selKnaDrawPlanDpbkfgCounts(false);// 违约记录数
                if (0 == dpbkfgCounts) {// 不存在违约则更新支取控制表中的违约标志为 【未违约】
                    tblKnaDraw.setDpbkfg(E_YES___.NO);
                    KnaDrawDao.updateOne_odb1(tblKnaDraw);
                }
                /*-----------------------计划处理状态更新-----------------begin--------------*/
                // 达到计划支取总额
                if (CommUtil.compare(tblKnaDrawPlan.getRedqam(),
                        tblKnaDrawPlan.getPlmony()) < 0) {
                    tblKnaDrawPlan.setPlstat(E_PLSTAT.DLSU);// 处理完成
                } else {
                    tblKnaDrawPlan.setPlstat(E_PLSTAT.DLNG);// 处理中
                }
                /*-----------------------计划处理状态更新-----------------end--------------*/

                KnaDrawPlanDao.updateOne_odb1(tblKnaDrawPlan);
            } else if (E_FCFLAG.FIX == fcflag) {// 定期支取计划
                tblKnaFxdrPlan.setRedqam(tblKnaFxdrPlan.getRedqam().subtract(
                        entity.getTranam()));
                tblKnaFxdrPlan.setRedwnm(tblKnaFxdrPlan.getRedwnm() - 1);
                /*-----------------------违约处理-----------------begin--------------*/
                if (ctrlwy == E_CTRLWY.TMCL) {// 支取方式控制 按次数控制
                    if (tblKnaFxdrPlan.getRedwnm() <= tblKnaFxdrPlan
                            .getPltime()) {// 实际支取次数大于计划总数-违约
                        tblKnaFxdrPlan.setDpbkfg(E_YES___.NO);
                    }
                } else if (ctrlwy == E_CTRLWY.AMCL) {// 支取方式控制 按金额控制
                    if (CommUtil.compare(tblKnaFxdrPlan.getRedqam(),
                            tblKnaFxdrPlan.getPlmony()) <= 0) {// 实际支取次数大于计划总数-违约
                        tblKnaFxdrPlan.setDpbkfg(E_YES___.NO);
                    }
                } else if (ctrlwy == E_CTRLWY.ATML) {// 支取方式控制 按金额和次数控制
                    if (CommUtil.compare(tblKnaFxdrPlan.getRedqam(),
                            tblKnaFxdrPlan.getPlmony()) < 0
                            && tblKnaFxdrPlan.getRedwnm() <= tblKnaFxdrPlan
                                    .getPltime()) {// 实际支取次数大于计划总数且实际支取次数大于计划总数-违约
                        tblKnaFxdrPlan.setDpbkfg(E_YES___.NO);
                    }
                }
                /*-----------------------违约处理-----------------end--------------*/
                Long dpbkfgCounts = DpSaveDrawDao
                        .selKnaFxdrPlanDpbkfgCounts(false);// 违约记录数
                if (0 == dpbkfgCounts) {// 不存在违约则更新支取控制表中的违约标志为 【未违约】
                    tblKnaFxdr.setDpbkfg(E_YES___.NO);
                    KnaFxdrDao.updateOne_odb1(tblKnaFxdr);
                }
                /*-----------------------计划处理状态更新-----------------begin--------------*/
                // 达到计划支取总额
                if (CommUtil.compare(tblKnaFxdrPlan.getRedqam(),
                        tblKnaFxdrPlan.getPlmony()) < 0) {
                    tblKnaFxdrPlan.setPlstat(E_PLSTAT.DLSU);// 处理完成
                } else {
                    tblKnaFxdrPlan.setPlstat(E_PLSTAT.DLNG);// 处理中
                }
                /*-----------------------计划处理状态更新-----------------end--------------*/

                KnaFxdrPlanDao.updateOne_odb1(tblKnaFxdrPlan);

            } else {
            	throw DpModuleError.DpstAcct.BNAS0843(fcflag.toString());
            }
        }

        // 增加支取利息冲正登记会计流水，解决支取时支出利息冲正 modify by renjinghua in 20150828
        if (CommUtil.isNotNull(entity.getInstam())
                && CommUtil.compare(entity.getInstam(), BigDecimal.ZERO) != 0) {

            bizlog.debug("登记利息支取冲正会计流水>>>>>>>>>>>>>>>>>" + entity.getInstam());

            IoAccounttingIntf cplIoAccounttingInrt = SysUtil
                    .getInstance(IoAccounttingIntf.class);
            cplIoAccounttingInrt.setCuacno(custac); // 电子账号
            cplIoAccounttingInrt.setAcctno(acctno); // 账号
            cplIoAccounttingInrt.setAcseno(bill.getAcseno()); // 子户号
            cplIoAccounttingInrt.setProdcd(onlnblEntity.getProdcd()); // 产品编号
            cplIoAccounttingInrt.setDtitcd(onlnblEntity.getDtitcd()); // 核算口径
            cplIoAccounttingInrt.setCrcycd(entity.getCrcycd()); // 币种
            cplIoAccounttingInrt.setTranam(instam); // 利息
            cplIoAccounttingInrt.setAcctdt(onlnblEntity.getAcctdt());// 应入账日期
            cplIoAccounttingInrt.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
            cplIoAccounttingInrt.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
            cplIoAccounttingInrt.setAcctbr(onlnblEntity.getAcctbr());
            cplIoAccounttingInrt.setAmntcd(amntcd); // 借方
            cplIoAccounttingInrt.setAtowtp(E_ATOWTP.DP); // 存款
            cplIoAccounttingInrt.setTrsqtp(E_ATSQTP.ACCOUNT); // 会计流水类型，账务
            cplIoAccounttingInrt.setBltype(E_BLTYPE.PYIN); // 余额属性利息支出
            // 登记交易信息，供总账解析
            if (CommUtil.equals(
                    "1",
                    KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
                            true).getParm_value1())) {
                KnpParameter para = SysUtil.getInstance(KnpParameter.class);
                para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%",
                        "%", true);
                cplIoAccounttingInrt.setTranms(para.getParm_value1());// 登记交易信息
                                                                 // 20160701
                                                                 // 结息
            }

            SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                    cplIoAccounttingInrt);

        }

        // 冲正付息明细信息
        DpAcctQryDao.updIndlstKnbPidlByPyinsq(acctno, entity.getOrtrdt(),
                bill.getTransq(), tmstmp);

        /**
         * 登记本金会计流水
         */
        IoAccounttingIntf cplIoAccounttingIntf = SysUtil
                .getInstance(IoAccounttingIntf.class);
        cplIoAccounttingIntf.setCuacno(custac);
        cplIoAccounttingIntf.setAcctno(acctno);
        cplIoAccounttingIntf.setAcseno(bill.getAcseno());
        cplIoAccounttingIntf.setProdcd(onlnblEntity.getProdcd());
        cplIoAccounttingIntf.setDtitcd(onlnblEntity.getDtitcd());
        cplIoAccounttingIntf.setCrcycd(entity.getCrcycd());
        cplIoAccounttingIntf.setTranam(onlnbl);
        cplIoAccounttingIntf.setAcctdt(onlnblEntity.getAcctdt());// 应入账日期
        cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
        cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
        cplIoAccounttingIntf.setAcctbr(onlnblEntity.getAcctbr());
        cplIoAccounttingIntf.setAmntcd(amntcd);
        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
        cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE);
        // 登记交易信息，供总账解析
        if (CommUtil.equals(
                "1",
                KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
                        true).getParm_value1())) {
            KnpParameter para = SysUtil.getInstance(KnpParameter.class);
            para = KnpParameterDao.selectOne_odb1("GlAnalysis", "1020000", "%", "%",
                    true);
            cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息 20160701
                                                             // 产品减少
        }
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                cplIoAccounttingIntf);

        /** 登记支取利息税会计流水，入账指令 **/
        IoAccounttingIntf cplIoAccounttingIntx = SysUtil
                .getInstance(IoAccounttingIntf.class);
        if (CommUtil.isNotNull(entity.getIntxam())
                && CommUtil.compare(entity.getIntxam(), BigDecimal.ZERO) != 0) {

            cplIoAccounttingIntx.setCuacno(custac);
            cplIoAccounttingIntx.setAcctno(acctno);
            cplIoAccounttingIntx.setAcseno(bill.getAcseno());
            cplIoAccounttingIntx.setProdcd(onlnblEntity.getProdcd());
            cplIoAccounttingIntx.setDtitcd(onlnblEntity.getDtitcd());
            cplIoAccounttingIntx.setCrcycd(entity.getCrcycd());
            cplIoAccounttingIntx.setTranam(intxam); // 利息税
            cplIoAccounttingIntx.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 应入账日期
            cplIoAccounttingIntx.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
            cplIoAccounttingIntx.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); // 账务日期
            cplIoAccounttingIntx.setAcctbr(onlnblEntity.getAcctbr());
            cplIoAccounttingIntx.setAmntcd(E_AMNTCD.CR);
            cplIoAccounttingIntx.setAtowtp(E_ATOWTP.DP);
            cplIoAccounttingIntx.setTrsqtp(E_ATSQTP.ACCOUNT);
            cplIoAccounttingIntx.setBltype(E_BLTYPE.INTAX);
            // 登记交易信息，供总账解析
            if (CommUtil.equals(
                    "1",
                    KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
                            true).getParm_value1())) {
                KnpParameter para = SysUtil.getInstance(KnpParameter.class);
                para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3020100", "%",
                        "%", true);
                cplIoAccounttingIntx.setTranms(para.getParm_value1());// 登记交易信息
            }
            SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                    cplIoAccounttingIntx);

        }
        // add 20161107 slw 通过电子账户找到对应的法人代码设置到运行环境变量中，先保存原交易法人，服务结束时再更新回去

        //处理追缴利息冲正
        if (CommUtil.compare(entity.getPyafamount(), BigDecimal.ZERO) > 0) {
            DpKnaFxac.strikePayAfter(entity, entity.getPyafamount(), bill);

            if (CommUtil.isNotNull(KnlBill)
                    && CommUtil.isNotNull(KnlBill.getAcctno())) {
                HKnlBill KnlBillPy = HKnlBillDao.selectOne_odb1(acctno, entity.getPydetlsq(),
                        entity.getOrtrdt(), true);
                KnlBillPy.setPmcrac(E_PMCRAC.TOSTRIK);
                KnlBillPy.setStacps(entity.getStacps());// 冲正冲账分类
                KnlBillPy.setCorrtg(E_CORRTG._1);// 设置抹账标志
                HKnlBillDao.updateOne_odb1(KnlBill);
            } else {
                KnlBill billPy = KnlBillDao.selectOne_odb1(acctno, entity.getPydetlsq(),
                        entity.getOrtrdt(), false);
                billPy.setPmcrac(E_PMCRAC.TOSTRIK);// 冲补账标志
                billPy.setStacps(entity.getStacps());// 冲正冲账分类
                billPy.setCorrtg(E_CORRTG._1);// 设置抹账标志
                KnlBillDao.updateOne_odb1(billPy);
            }
        }

    }

    /**
     * 
     * @Author renjinghua
     *         <p>
     *         <li>2015年9月15日-下午3:41:45</li>
     *         <li>功能说明:存款存入时签约，到期转存签约等</li>
     *         </p>
     * @param acctno
     *        定期账号
     * @param signtp
     *        签约类型
     * @param custac
     *        电子账号
     * @param custno
     *        客户号
     * @param prodcd
     *        产品编号
     * @param trdptp
     *        转存类型
     * @param trprod
     *        转存产品
     * @param autofg
     *        自动签约标志
     * 
     */
    public static void prcDpSign(String acctno, E_SIGNTP signtp, String custac,
            String custno, String prodcd, E_TRDPTP trdptp, String trprod,
            E_YES___ autofg) {

        KnaDpsg tblKnaDpsg = SysUtil.getInstance(KnaDpsg.class);
        tblKnaDpsg.setAcctno(acctno);
        tblKnaDpsg.setSigntp(signtp); // 签约类型
        tblKnaDpsg.setCustac(custac); // 电子账号
        tblKnaDpsg.setCustno(custno); // 客户号
        tblKnaDpsg.setProdcd(prodcd); // 产品编号
        tblKnaDpsg.setTrdptp(trdptp); // 转存类型
        tblKnaDpsg.setTrprod(trprod); // 转存产品
        tblKnaDpsg.setSigndt(CommTools.getBaseRunEnvs().getTrxn_date()); // 签约日期
        tblKnaDpsg.setSignsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 签约流水
        tblKnaDpsg.setAutofg(autofg); // 是否自动签约
        tblKnaDpsg.setSignst(E_SIGNST.QY); // 签约状态

        KnaDpsgDao.insert(tblKnaDpsg);

    }
    
    /**
     * 
     * <p>Title:fxtocu </p>
     * <p>Description: 实时处理活期账户余额余额不足-定转活的场景 ！
     * 处理逻辑： 
     *         1.1 定期支取;
     *         1.2 活期入账;
     * </p>
     * @author add by huangwh
     * @date   2018年10月24日 
     * @param acctno 负债账号
     * @param acctno 不足金额
     * 
     */
    public static void fxtocu(String acctno,BigDecimal handam){
        
        bizlog.debug("=================智能储蓄转电子账户开始！=================");

        /**
         * 校验智能储蓄定转活标志
         */
        String stopfg = KnpParameterDao.selectOne_odb1("DpParm.auacct", "fxtocu", "%", "%", false).getParm_value2();
        if (CommUtil.equals(stopfg, "STOP")) {
            throw ApError.BusiAplt.E0000("智能储蓄定转活已暂停。");
        }

        //String trandt = DateTools2.getDateInfo().getSystdt();
        
        E_SIGNST signst = E_SIGNST.QY;//1
        cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP signtp = 
                cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP.ZNCXL;//01
        bizlog.debug("=================参数为=================[#负债账号:" + acctno + "#签约状态:" + signst + "#签约类型:" + signtp + "]");
        AuacinTranData auacinTranData = DpAcctQryDao.selAuacinTranDataByAcctno(signst, signtp,acctno,false);

        bizlog.debug("账户名称：[" + auacinTranData.getAcctna() + "]");
        bizlog.debug("子账户号：[" + auacinTranData.getAcseno() + "]");
        bizlog.debug("电子账号：[" + auacinTranData.getCardno() + "]");
        bizlog.debug("账号ID：[" + auacinTranData.getCustac() + "]");
        bizlog.debug("币种：[" + auacinTranData.getCrcycd() + "]");
        bizlog.debug("原始金额：[" + auacinTranData.getOnlnbl() + "]");
        bizlog.debug("存款账号：[" + auacinTranData.getAuacno() + "]");
        bizlog.debug("结算账号：[" + auacinTranData.getAcctno() + "]");

        BigDecimal upAmt = CommUtil.nvl(auacinTranData.getOtupam(),BigDecimal.ZERO); //转出递增金额
        BigDecimal miAmt = CommUtil.nvl(auacinTranData.getOtmiam(),BigDecimal.ZERO); //转出最小金额

        //手动设置交易金额
        bizlog.debug("=================参数为=================[#电子账户交易差额:" + handam + "]");
        BigDecimal tranam = handam; //交易金额
        BigDecimal acctbl = BigDecimal.ZERO;//产品余额

        IoCaKnaAccs accs = DpAcctQryDao.selKnaAccsByAcctno(auacinTranData.getAuacno(), false);

        if (CommUtil.isNull(accs))
            throw DpModuleError.DpstAcct.BNAS1125(auacinTranData.getCustac(), auacinTranData.getAuacno());

        if (CommUtil.isNull(accs.getFcflag()))
            throw DpModuleError.DpstAcct.BNAS0844();

        KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
        KnaFxac tblKnaFxac = SysUtil.getInstance(KnaFxac.class);
        String brchno = null;
        if(accs.getFcflag() == E_FCFLAG.CURRENT){
            tblKnaAcct = ActoacDao.selKnaAcct(auacinTranData.getAuacno(), true);
            acctbl = tblKnaAcct.getOnlnbl();
            brchno = tblKnaAcct.getBrchno();
        }else if(accs.getFcflag() == E_FCFLAG.FIX){
            tblKnaFxac = ActoacDao.selKnaFxac(auacinTranData.getAuacno(), true);
            acctbl = tblKnaFxac.getOnlnbl();
            brchno = tblKnaFxac.getBrchno();
        }else{
            throw DpModuleError.DpstAcct.BNAS1739();
        }
        //标记最小转出金额
        if(CommUtil.compare(tranam, miAmt) <= 0){
            tranam = miAmt;
        }

        if(CommUtil.compare(upAmt, BigDecimal.ZERO) > 0){
            //BigDecimal tmp = tranam.subtract(miAmt).divide(upAmt, 0, BigDecimal.ROUND_DOWN); //向下取整
            BigDecimal[] tmp = tranam.subtract(miAmt).divideAndRemainder(upAmt); //参数0：商数 参数1：余数
            if(!CommUtil.equals(tmp[1], BigDecimal.ZERO)){ //余数不为0
                tranam = miAmt.add(upAmt.multiply(tmp[0].add(BigDecimal.valueOf(1)))); //计算新的交易金额
            }
        }

        if(CommUtil.compare(tranam, acctbl) > 0){
            tranam = acctbl;
        }
        // 冻结的账户不进行处理。
        bizlog.debug("智能储蓄账号[" + auacinTranData.getAuacno() + "]转出开始，金额[" + tranam + "]");
        bizlog.debug("存款账号金额：[" + acctbl + "]");
        bizlog.debug("转出最小金额：[" + miAmt + "]");
        bizlog.debug("转出递增金额：[" + upAmt + "]");

        // 每一笔交易重新生成一笔流水，用来进行平衡性检查
        // MsSystemSeq.getTrxnSeq();
        //已经做过解约的，且交易金额等于账户余额的，直接做销户处理
        if(CommUtil.isNotNull(auacinTranData.getCncldt()) && CommUtil.compare(tranam, acctbl) == 0){ 
            KnaAcct setKnaAcct = CapitalTransDeal.getSettKnaAcctSub(auacinTranData.getCustac(), E_ACSETP.SA);
            IoDpCloseIN clsin = SysUtil.getInstance(IoDpCloseIN.class);
            clsin.setCardno("");
            clsin.setToacct(setKnaAcct.getAcctno());
            clsin.setTobrch(setKnaAcct.getBrchno());
            clsin.setToname(setKnaAcct.getAcctna());
            clsin.setSmrycd(BusinessConstants.SUMMARY_XH);
            clsin.setSmryds("销户");

            if(CommUtil.isNotNull(tblKnaAcct) && CommUtil.isNotNull(tblKnaAcct.getAcctno())){
                //获取可售产品名称
                KnaAcctProd tblKnaAcctProd = DpAcctDao.selKnaAcctProdByAcctno(auacinTranData.getAuacno(), false);
                if(CommUtil.isNotNull(tblKnaAcctProd)){
                    clsin.setRemark(tblKnaAcctProd.getObgaon());
                }
                InterestAndIntertax cplint  =   DpCloseAcctno.prcCurrInterest(tblKnaAcct, clsin);
                BigDecimal interest = cplint.getInstam();//利息
                BigDecimal intxam = cplint.getIntxam();//利息税
                BigDecimal onlnbl = DpCloseAcctno.prcCurrOnbal(tblKnaAcct, clsin);
                prcPostAcct(setKnaAcct, tblKnaAcct.getAcctno(), onlnbl.add(interest),intxam);
            }
            if(CommUtil.isNotNull(tblKnaFxac) && CommUtil.isNotNull(tblKnaAcct.getAcctno())){
                DrawDpAcctOut  drawDpAcctOut=DpCloseAcctno.prcClsFxacAcct(tblKnaFxac, setKnaAcct.getAcctno(), E_YES___.YES);
                BigDecimal interest = drawDpAcctOut.getInstam();//利息
                BigDecimal intxam = drawDpAcctOut.getIntxam();//利息税
                prcPostAcct(setKnaAcct, tblKnaFxac.getAcctno(), tblKnaFxac.getOnlnbl().add(interest),intxam);
            }

            SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).custUnSign(auacinTranData.getSignno(), E_YES___.YES);
        }else{//无需销户

            //备注要求是 产品名称
            String remark = "";
            KnaAcctProd tblacprod = KnaAcctProdDao.selectOne_odb1(auacinTranData.getAuacno(), false);
            KnaFxacProd tblfxprod = KnaFxacProdDao.selectOne_odb1(auacinTranData.getAuacno(), false);
            if(CommUtil.isNotNull(tblacprod)){
                remark = tblacprod.getObgaon();
            }
            if(CommUtil.isNotNull(tblfxprod)){
                remark = tblfxprod.getObgaon();
            }

            // 智能储蓄支取
            DpSaveEntity input_draw = SysUtil.getInstance(DpSaveEntity.class);
            input_draw.setAcctno(auacinTranData.getAuacno());
            input_draw.setAcseno(auacinTranData.getAusbac());
            input_draw.setCardno(auacinTranData.getCardno());
            input_draw.setCrcycd(auacinTranData.getCrcycd());
            input_draw.setCustac(auacinTranData.getCustac());
            input_draw.setOpacna(auacinTranData.getAcctna());
            input_draw.setToacct(auacinTranData.getAcctno());
            input_draw.setOpbrch(brchno);
            input_draw.setSmrycd(BusinessConstants.SUMMARY_ZC);
            input_draw.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_ZC));
            input_draw.setRemark("结算主账户-"+remark);
            input_draw.setTranam(tranam);
            input_draw.setAuacfg(E_YES___.NO);// 不是普通的智能储蓄存取
            bizlog.debug("智能储蓄定期支取");
            DpPublicServ.drawAcctDp(input_draw);
            bizlog.debug("智能储蓄定期支取完成");

            // 活期存入
            DpSaveEntity input_post = SysUtil.getInstance(DpSaveEntity.class);
            input_post.setAcctno(auacinTranData.getAcctno());
            input_post.setAcseno(auacinTranData.getAcseno());
            input_post.setCardno(auacinTranData.getCardno());
            input_post.setCrcycd(auacinTranData.getCrcycd());
            input_post.setCustac(auacinTranData.getCustac());
            input_post.setOpacna(auacinTranData.getAcctna());
            input_post.setToacct(auacinTranData.getAuacno());
            input_post.setSmrycd(BusinessConstants.SUMMARY_ZR);
            input_post.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_ZR));
            input_post.setRemark(remark);
            input_post.setTranam(tranam);
            input_post.setAuacfg(E_YES___.NO);// 不是普通的智能储蓄存取
            bizlog.debug("电子账户存入");
            DpPublicServ.postAcctDp(input_post);
            bizlog.debug("电子账户存入完成");
        }
        
        // 检查平衡
        String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
        String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
        IoCheckBalance ioCheckBalanceSrv = SysUtil.getInstance(IoCheckBalance.class);
        ioCheckBalanceSrv.checkBalance(trandt, transq,null);
        //      CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);
        
        bizlog.debug("=================智能储蓄转电子账户结束！=================");
    }
    
    /**
     * 
     * <p>Title:prcPostAcct </p>
     * <p>Description: 结算户存入记账服务	</p>
     * @author add by huangwh
     * @date   2018年10月24日 
     * @param acct
     * @param toacct
     * @param tranam
     * @param intxam
     */
    public static void prcPostAcct(KnaAcct acct, String toacct, BigDecimal tranam,BigDecimal intxam){

        String remark = "";//可售产品名称
        //获取可售产品名称值
        KnaAcctProd tblAcctProd = DpAcctDao.selKnaAcctProdByAcctno(toacct, false);
        KnaFxacProd tblFxacProd = DpAcctDao.selKnaFxacProdByAcctno(toacct, false);

        if(CommUtil.isNotNull(tblAcctProd)){
            remark = "活期-"+tblAcctProd.getObgaon();
        }else if(CommUtil.isNotNull(tblFxacProd)){
            remark = "定期-"+tblFxacProd.getObgaon();
        }

        if(CommUtil.compare(tranam, BigDecimal.ZERO) > 0){

            SaveDpAcctIn cplSaveIn = SysUtil.getInstance(SaveDpAcctIn.class);

            cplSaveIn.setAcctno(acct.getAcctno());
            cplSaveIn.setAcseno(null);//
            cplSaveIn.setBankcd(null);
            cplSaveIn.setBankna(null);
            cplSaveIn.setCardno(null);
            cplSaveIn.setCrcycd(acct.getCrcycd());
            cplSaveIn.setCustac(acct.getCustac());
            cplSaveIn.setLinkno(null);
            cplSaveIn.setOpacna(acct.getAcctna());
            cplSaveIn.setOpbrch(acct.getBrchno());
            cplSaveIn.setRemark(remark);
            cplSaveIn.setSmrycd(BusinessConstants.SUMMARY_TZ);
            cplSaveIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_TZ));
            cplSaveIn.setToacct(toacct);
            cplSaveIn.setTranam(tranam);//本金+利息（包含利息税）
            cplSaveIn.setIschck(E_YES___.NO);

            SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveIn);
        }
        //利息税入账
        DrawDpAcctIn drawin = SysUtil.getInstance(DrawDpAcctIn.class); //支取服务输入参数
        if(CommUtil.compare(intxam, BigDecimal.ZERO) > 0){
            //结算户支取记账处理             
            drawin.setAcctno(acct.getAcctno()); //做支取的负债账号
            drawin.setAuacfg(E_YES___.NO);
            drawin.setCardno(null);
            drawin.setCrcycd(acct.getCrcycd());
            drawin.setCustac(acct.getCustac());
            drawin.setLinkno(null);
            drawin.setOpacna(acct.getAcctna());
            drawin.setToacct(acct.getAcctno()); //结算账号
            drawin.setTranam(intxam);
            drawin.setSmrycd(BusinessConstants.SUMMARY_JS);
            drawin.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_JS));
            drawin.setRemark(remark);
            SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawin);

        }       

    }


}
