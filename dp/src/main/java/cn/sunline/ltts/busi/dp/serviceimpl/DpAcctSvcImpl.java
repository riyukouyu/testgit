package cn.sunline.ltts.busi.dp.serviceimpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.clwj.msap.util.sequ.MsSeqUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.edsp.busi.dp.errors.DpAcError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCuad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCuadDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSignDetl;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpacDao;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.acct.DpCloseAcctno;
import cn.sunline.ltts.busi.dp.acct.DpCloseCustac;
import cn.sunline.ltts.busi.dp.acct.DpTestCalcInterest;
import cn.sunline.ltts.busi.dp.acct.OpenSubAcctDeal;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.base.DpPublicServ;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.dayend.DpInterest;
import cn.sunline.ltts.busi.dp.domain.DpOpenAcctEntity;
import cn.sunline.ltts.busi.dp.domain.DpSaveEntity;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcinDao;
import cn.sunline.ltts.busi.dp.namedsql.YhtDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.QryEacctSelpay.Input;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.selHqTranDetl.Output;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsq;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbdl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbdlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbdlFix;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbdlFixDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbEpcb;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbEpcbDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbEpcbNxtm;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbEpcbNxtmDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbInrtAdjt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbInrtAdjtDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPidl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwne;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwneDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfir;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfirDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPost;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostDao;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbStat;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbStatDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDetl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDrdl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDrdlDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacMatu;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacMatuDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbFxacTrfe;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbFxacTrfeDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBill;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBillDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapot;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapotChrg;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapotDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnsTranEror;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnsTranErorDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInstPrcIn;
import cn.sunline.ltts.busi.icore.parent.errors.ItError;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaStaPublic;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.IoDpTable;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.InknlcnapotDetl;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpAcctblList;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpDpAssetHisQry;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpDpAssetsQry;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbAcin;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbInrtAdjt;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbPidl;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpTranDetlDqQry;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpTranDetlHqQry;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpTranDetlHqQryAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlIobl;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.KnbCbdlFixInfo;
import cn.sunline.ltts.busi.iobus.type.IoInWriteOffComplex.KnsStrkInput;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.cf.IoCuTable.IoCifCustAccs;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.AcctCabrForCabrdt;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.AcctCabrInfo;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.AcctCabrInfoList;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.AcctPredCabrInfo;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.AcctPredCabrInfos;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.AddSubAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ClsAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ClsAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DpInsAll;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DpInstCal;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DpIntrvlInfo;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DpOpenSbacPostIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DpOpenSbacPostOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.IoDpTranfeInfo;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.QryDpAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.QryDpAcctOutMsg;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaSign;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaSignDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnpAcctType;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaSelAcctno;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEARelaIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaOpSubOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AdjustInterest_IN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.ChrgIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpBasePart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseDetailOT;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseOT;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpOpenSub;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpAcctProdInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpAcdcOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpClsChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpClsChkOT;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoEacctSelpayInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
import cn.sunline.ltts.busi.sys.dict.DpDict;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.errors.PbError.Intr;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SETYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AVBLDT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CAINPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CAINRD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CUSTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DETLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IOTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRRTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PROCST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_QRACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SPECTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PDTPDL;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SLEPST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SPPRST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_ACOUTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_ADJTTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSTAT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CYCLTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DRINTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IBAMMD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEBS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRDPWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_REPRWY;
import cn.sunline.ltts.busi.sys.type.FnEnumType.E_WARNTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_REBUWA;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_STATUS;
import cn.sunline.ltts.busi.sys.type.yht.E_TRANTP;

/**
 * 负债账户服务实现 负债账户服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
public class DpAcctSvcImpl implements
        cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType {

    private static final BizLog bizlog = BizLogUtil
            .getBizLog(DpAcctSvcImpl.class);
    private static final String delimt = "^"; // 分隔符

    /**
     * 新增负债账户
     * 
     */
    public AddSubAcctOut addAcct(
            final cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.AddAcct.Input Input) {
        String custac = Input.getCustac();
        String custno = Input.getCustno();
        String acctna = Input.getAcctna();

        //IoPbStaPublic pbpub = SysUtil.getInstance(IoPbStaPublic.class);

        //E_CUSACT cacttp = null; // 客户账号类型
        // cacttp = KubCorpProc.getCsactp(Input.getCacttp());
        //cacttp = pbpub.kubCorpProcGetCsactp(Input.getCacttp());
        // 检查输入客户账号类型
        if (CommUtil.isNotNull(Input.getCacttp())) {
            // knp_acct_type tblAcct_type =
            // Knp_acct_typeDao.selectOne_odb1(cacttp, false);
            IoCaSevQryTableInfo caqry = SysUtil
                    .getInstance(IoCaSevQryTableInfo.class);
            IoCaKnpAcctType tblAcct_type = caqry.getKnpAcctTypeOdb1(
                    Input.getCacttp(), false);

            if (CommUtil.isNull(tblAcct_type)) {
                throw DpAcError.DpDeptAcct.BNAS0515();
            }
        }

        DpOpenAcctEntity entity = new DpOpenAcctEntity();
        entity.setProdcd(Input.getProdcd());
        entity.setCacttp(Input.getCacttp());
        entity.setAcctna(acctna);
        entity.setCustno(custno);
        entity.setCustac(custac);
        entity.setDepttm(Input.getDepttm());
        entity.setCrcycd(Input.getCrcycd());
        entity.setCusttp(CommUtil.nvl(Input.getCusttp(), E_CUSTTP.PERSON));
        entity.setOpenir(Input.getOpenir());
        entity.setTranam(Input.getTranam());
        entity.setAcsetp(Input.getAcsetp());
        
        DpPublicServ.openAcct(entity);

        // 设置输出接口参数。
        AddSubAcctOut cplAddSubAcctOut = SysUtil
                .getInstance(AddSubAcctOut.class);
        cplAddSubAcctOut.setAcctno(entity.getAcctno());
        cplAddSubAcctOut.setPddpfg(entity.getPddpfg());
        cplAddSubAcctOut.setProdcd(entity.getProdcd());

        /**
         * 注册账号路由
         */
        //取消注册路由信息  modified by sunzy 20191114
//        ApAcctRoutTools.register(entity.getAcctno(), E_ACCTROUTTYPE.DEPOSIT);
//        bizlog.debug(entity.getProdcd()+"的定活标志为"+entity.getPddpfg());
        return cplAddSubAcctOut;
    }

    /**
     * 存入记账服务
     * 
     */
    public void addPostAcctDp(SaveDpAcctIn cplSaveAcctIn) {

        DpSaveEntity entity = new DpSaveEntity();
        entity.setAcctno(cplSaveAcctIn.getAcctno());
        entity.setCrcycd(cplSaveAcctIn.getCrcycd());
        entity.setTranam(cplSaveAcctIn.getTranam());
        entity.setCardno(cplSaveAcctIn.getCardno());
        entity.setCustac(cplSaveAcctIn.getCustac());
        entity.setAcseno(cplSaveAcctIn.getAcseno());
        entity.setToacct(cplSaveAcctIn.getToacct());
        entity.setOpacna(cplSaveAcctIn.getOpacna());
        entity.setLinkno(cplSaveAcctIn.getLinkno());
        entity.setOpbrch(cplSaveAcctIn.getOpbrch()); // 对方账号所属机构
        entity.setBankcd(cplSaveAcctIn.getBankcd()); // 对方金融机构代码
        entity.setBankna(cplSaveAcctIn.getBankna()); // 对方金融机构名称
        entity.setSmrycd(cplSaveAcctIn.getSmrycd()); // 摘要代码
        entity.setSmryds(cplSaveAcctIn.getSmryds()); // 摘要描述
        entity.setRemark(cplSaveAcctIn.getRemark()); // 备注
        entity.setStrktg(cplSaveAcctIn.getStrktg()); // 是否允许冲正
        entity.setIschck(cplSaveAcctIn.getIschck()); // 是否校验规则标志
        entity.setNegafg(cplSaveAcctIn.getNegafg());
        entity.setMacdrs(cplSaveAcctIn.getMacdrs());
        entity.setDetlsq(cplSaveAcctIn.getDetlsq());
        entity.setTeleno(cplSaveAcctIn.getTeleno());
        entity.setImeino(cplSaveAcctIn.getImeino());
        entity.setUdidno(cplSaveAcctIn.getUdidno());
        entity.setTrands(cplSaveAcctIn.getTrands());
        entity.setServtp(cplSaveAcctIn.getServtp());
        entity.setIntrcd(cplSaveAcctIn.getIntrcd());
        if(cplSaveAcctIn.getIssucc()== E_YES___.YES){
            entity.setTransq(cplSaveAcctIn.getTransq());
        }else{
            entity.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
        }
        
        DpPublicServ.postAcctDp(entity);
    }

    /**
     * 支取记账处理
     * 
     */
    public DrawDpAcctOut addDrawAcctDp(DrawDpAcctIn cplDrawAcctIn) {

        DpSaveEntity entity = new DpSaveEntity();
        entity.setAcctno(cplDrawAcctIn.getAcctno());
        entity.setCrcycd(cplDrawAcctIn.getCrcycd());
        entity.setTranam(cplDrawAcctIn.getTranam());
        entity.setCardno(cplDrawAcctIn.getCardno());
        entity.setCustac(cplDrawAcctIn.getCustac());
        entity.setAcseno(cplDrawAcctIn.getAcseno());
        entity.setToacct(cplDrawAcctIn.getToacct());
        entity.setOpacna(cplDrawAcctIn.getOpacna());
        entity.setLinkno(cplDrawAcctIn.getLinkno());
        entity.setAuacfg(cplDrawAcctIn.getAuacfg());
        entity.setOpbrch(cplDrawAcctIn.getOpbrch()); // 对方账号所属机构
        entity.setBankcd(cplDrawAcctIn.getBankcd()); // 对方金融机构
        entity.setBankna(cplDrawAcctIn.getBankna()); // 对方金融机构名称
        entity.setSmrycd(cplDrawAcctIn.getSmrycd()); // 摘要代码
        entity.setSmryds(cplDrawAcctIn.getSmryds()); // 摘要描述
        entity.setRemark(cplDrawAcctIn.getRemark()); // 备注
        entity.setStrktg(cplDrawAcctIn.getStrktg()); //是否允许冲正
        entity.setIschck(cplDrawAcctIn.getIschck()); //是否校验规则标志

        entity.setIsdedu(cplDrawAcctIn.getIsdedu()); //是否扣划
        entity.setDedutp(cplDrawAcctIn.getDedutp()); //扣划类型

        entity.setMacdrs(cplDrawAcctIn.getMacdrs());
        entity.setDetlsq(cplDrawAcctIn.getDetlsq());
        entity.setTeleno(cplDrawAcctIn.getTeleno());
        entity.setImeino(cplDrawAcctIn.getImeino());
        entity.setUdidno(cplDrawAcctIn.getUdidno());
        entity.setTrands(cplDrawAcctIn.getTrands());
        entity.setServtp(cplDrawAcctIn.getServtp());
        entity.setIntrcd(cplDrawAcctIn.getIntrcd());
        if(cplDrawAcctIn.getIssucc()== E_YES___.YES){
            entity.setTransq(cplDrawAcctIn.getTransq());
        }else{
            entity.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
        }

        //start: add by huangwh 20181026  
        //计算交易额和电子账户余额差值，判断是否需要智能储蓄转电子账户

        String acctno = cplDrawAcctIn.getAcctno();
        KnaAcct knaAcct = KnaAcctDao.selectOne_odb1(acctno, false);
        if(knaAcct!=null && knaAcct.getOnlnbl().compareTo(cplDrawAcctIn.getTranam())<0){//null值判断  && 金额比较  && 资金池余额校验在交易前处理已经校验了
            BigDecimal handam = cplDrawAcctIn.getTranam().subtract(knaAcct.getOnlnbl());
            //实时处理余额为负的情况:智能存款支出，活期账户入账。
            bizlog.debug("=================负债账号:[" + acctno + "]" + "#活期余额:[" + knaAcct.getOnlnbl() + "]" + "#交易金额:[" + handam + "]=================");
            DpPublicServ.fxtocu(acctno,handam);
            //定期部分支取日终会结息 
        }

        //end: add by huangwh 20181026 

        //支取方法
        DpPublicServ.drawAcctDp(entity);

        DrawDpAcctOut cplDrawAcctout = SysUtil.getInstance(DrawDpAcctOut.class);

        cplDrawAcctout.setInstam(entity.getInstam());//利息
        cplDrawAcctout.setIntxam(entity.getIntxam());//利息税
        cplDrawAcctout.setPyafam(entity.getPyafamount());//追缴利息
        //add by zdj 20181024
        cplDrawAcctout.setDrdate(CommTools.getBaseRunEnvs().getTrxn_date());//支取日期
        //add end

        return cplDrawAcctout;

    }

    /**
     * 电子账户销户处理
     * 
     * 1.检查输入账户要素是否正确 2.检查账户余额情况 3.注销电子账户及负债账户等
     */
    @Override
    public ClsAcctOut clsAcct(ClsAcctIn cplClsAcctIn) {
        // 1.检查输入账户相关信息是否符合规范
        DpAcctProc.chkClsAcctInfo(cplClsAcctIn);

        // 2.检查负债账户余额和积数情况，所有账户余额和积数等于0时才允许销户
        String custac = cplClsAcctIn.getCustac(); // 电子账号
        BigDecimal bal = BigDecimal.ZERO; // 账户余额
        BigDecimal cut = BigDecimal.ZERO; // 账户积数
        BigDecimal drawam = BigDecimal.ZERO; // 智能储蓄支取明细金额
        Map<String, BigDecimal> blncs = DpAcctProc.getAllAcctBal(
                cplClsAcctIn.getCustac(), bal, cut);

        bal = blncs.get("bal");
        cut = blncs.get("cut");
        drawam = blncs.get("drawam");

        bizlog.debug("<<<<<<<<<<<<<<<<<<负债账户余额>>>>>>>>>>>>>>>>>>>：bal=" + bal);

        // 账户余额不全为0
        if (CommUtil.compare(bal, BigDecimal.ZERO) > 0) {
            throw DpAcError.DpDeptAcct.BNAS1273(custac);
        }

        // 账户积数不全为0
        if (CommUtil.compare(cut, BigDecimal.ZERO) > 0) {
            throw DpAcError.DpDeptComm.BNAS0954(custac);
        }

        // 智能储蓄未全部结息
        if (CommUtil.compare(drawam, BigDecimal.ZERO) > 0) {
            throw DpAcError.DpDeptAcct.BNAS0953(custac);
        }

        // 3.电子账户销户处理，更新状态
        DpAcctProc.prcAcctst(cplClsAcctIn);

        ClsAcctOut cplClsAcctOut = SysUtil.getInstance(ClsAcctOut.class);
        cplClsAcctOut.setCardno(cplClsAcctIn.getCardno());
        cplClsAcctOut.setCustac(cplClsAcctIn.getCustac());
        cplClsAcctOut.setCustna(cplClsAcctIn.getCustna());

        return cplClsAcctOut;

    }

    /**
     * 智能储蓄预期收益查询
     */
    @Override
    public AcctCabrInfoList selAcctCabrInfo(String custac) {
        String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); // 交易日期
        String lstrdt = CommTools.getBaseRunEnvs().getLast_date(); // 上次交易日期
        String tmstmp = DateTools2.getCurrentTimestamp();//时间戳

        BigDecimal bigOnlnbl = BigDecimal.ZERO; // 活期账户当前余额

        AcctCabrInfoList cabrInfoList = SysUtil
                .getInstance(AcctCabrInfoList.class);
        // 先删除临时表中数据
        DpAcctDao.delKnbEpcb(custac);
        // 活期计提明细
        DpAcctDao.insAcctToKnbEpcb(custac, lstrdt, tmstmp);
        // 智能储蓄未支取部分
        DpAcctDao.insFxacDetlToKnbEpcd(custac, lstrdt, tmstmp);
        // 智能储蓄支取部分
        DpAcctDao.insFxacDrdlToKnbEpcd(custac, tmstmp);

        // 计算到靠下一档所需天数
        // 查询定期未支取部分计提信息、及活期账户
        List<KnbEpcb> lstKnbEpcb = DpAcctDao.selKnbEpcbForFixSave(custac,
                false);
        for (KnbEpcb tblKnbEpcb : lstKnbEpcb) {
            String dayllm = tblKnbEpcb.getRemark(); // 档期天数
            E_TERMCD rfirtm = null; // 下一档期的对应存期
            long nxdylm = 0; // 下一档期天数

            if (CommUtil.equals(tblKnbEpcb.getRemark(), "0")) {
                if (CommUtil.compare(tblKnbEpcb.getOnlnbl(), BigDecimal.ZERO) < 0) {
                    bigOnlnbl = bigOnlnbl.add(tblKnbEpcb.getOnlnbl()).negate();
                    tblKnbEpcb.setOnlnbl(BigDecimal.ZERO);
                    KnbEpcbDao.updateOne_odb3(tblKnbEpcb);
                }
            } else {
                switch (dayllm) {
                case "1":
                    rfirtm = E_TERMCD.T107;
                    nxdylm = 7;
                    break;
                case "7":
                    rfirtm = E_TERMCD.T203;
                    nxdylm = 90;
                    break;
                case "90":
                    rfirtm = E_TERMCD.T206;
                    nxdylm = 180;
                    break;
                case "180":
                    rfirtm = E_TERMCD.T301;
                    nxdylm = 360;
                    break;
                case "360":
                    rfirtm = E_TERMCD.T302;
                    nxdylm = 720;
                    break;
                case "720":
                    rfirtm = E_TERMCD.T303;
                    nxdylm = 1080;
                    break;
                case "1080":
                    rfirtm = E_TERMCD.T305;
                    nxdylm = 1800;
                    break;
                default:
                    break;
                }
                // 下一档期到期日期
                String nxmudt = null;
                long diffdays = 0;
                if (!CommUtil.equals(dayllm, "1800")) {
                    nxmudt = DateTools2.calDateByTerm(tblKnbEpcb.getBgindt(),
                            rfirtm);
                    // 靠下一档所需天数
                    diffdays = DateTools2.calDays(trandt, nxmudt, 0, 0);
                }

                tblKnbEpcb.setNxfdtm(nxdylm);
                tblKnbEpcb.setNxmudt(nxmudt);
                tblKnbEpcb.setDiffdy(diffdays);

                KnbEpcbDao.updateOne_odb3(tblKnbEpcb);
            }
        }
        // 删除智能储蓄预下一档临时表信息
        DpAcctDao.delKnbEpcbNxtm(custac);
        // 插入电子账户各档期预下一档信息
        DpAcctDao.insKnbEpcbNxtm(custac);
        // 查询预期收益总额
        BigDecimal cabrsm = DpAcctDao.selTotalCabrByCustac(custac, false);
        // 查询电子账户下智能储蓄最高档期
        String mxdylm = DpAcctDao.selMaxDayllmForKnbEpcb(custac, false);
        // 查询预期收益明细
        List<AcctCabrInfo> acctCabrInfos = DpAcctDao.selKnbEpcb(custac, false);
        for (AcctCabrInfo cabrInfo : acctCabrInfos) {
            // 查询当前档预下一档
            bizlog.debug(">>>>>>>>>>>>>>>>>档期天数" + cabrInfo.getRemark());
            if (!CommUtil.equals(cabrInfo.getRemark(), "0")) {
                // 当前活期账户余额小于0时，将透支金额分摊到定期各档期，按档期从小到大扣减，当前当金额不足扣下一档
                if (CommUtil.compare(bigOnlnbl, BigDecimal.ZERO) > 0) {
                    if (CommUtil.compare(cabrInfo.getAcblam(), bigOnlnbl) >= 0) {
                        cabrInfo.setAcblam(cabrInfo.getAcblam().subtract(
                                bigOnlnbl));
                        bigOnlnbl = BigDecimal.ZERO;
                    } else {
                        bigOnlnbl = bigOnlnbl.subtract(cabrInfo.getAcblam());
                        cabrInfo.setAcblam(BigDecimal.ZERO);
                    }
                }
                // 从临时表去预靠下一档金额及剩余天数
                KnbEpcbNxtm tblKnbEpcbNxtm = KnbEpcbNxtmDao
                        .selectOne_odb1(custac, cabrInfo.getRemark(), false);
                if (CommUtil.isNotNull(tblKnbEpcbNxtm)) {
                    cabrInfo.setDiffdy(tblKnbEpcbNxtm.getDiffdy());
                    cabrInfo.setIntram(tblKnbEpcbNxtm.getIntram());
                    // 预靠下一档金额大于当前档余额，则取当前档余额
                    if (CommUtil.compare(cabrInfo.getIntram(),
                            cabrInfo.getAcblam()) > 0) {
                        cabrInfo.setIntram(cabrInfo.getAcblam());
                    }
                }
            }

            cabrInfoList.getCabrInfo().add(cabrInfo);
        }

        cabrInfoList.setCabrsm(cabrsm);
        cabrInfoList.setMxdylm(mxdylm);

        // 清理数据
        // DpAcctDao.delKnbEpcb(custac);

        return cabrInfoList;
    }

    // 查询日期区间每天计提利息(预期收益)
    @Override
    public AcctPredCabrInfos selAcctPredCabrForSeven(String custac,
            String bigndt, String endddt) {
        String tmstmp = DateTools2.getCurrentTimestamp();
        // 输出
        AcctPredCabrInfos cplPredCabrInfos = SysUtil
                .getInstance(AcctPredCabrInfos.class);
        // 删除临时表中数据
        DpAcctDao.delCainCabrinByCustac(custac);
        // 插入传入日期期间计提数据到临时表
        DpAcctDao.insAcctCabrToCain(custac, bigndt, endddt, tmstmp);
        // 获取上一日期
        String lsbgdt = DateTools2.dateAdd (-1, bigndt);
        String lseddt = DateTools2.dateAdd (-1, endddt);
        // 查询上一日期区间的计提信息
        List<AcctCabrForCabrdt> cplLastCabrInfos = DpAcctDao
                .selAcctCabrByCabrdt(custac, lsbgdt, lseddt, false);
        // 更新上一日计提利息到临时表
        for (AcctCabrForCabrdt cplLastCabrInfo : cplLastCabrInfos) {
            String cabrdt = DateTools2.dateAdd (1, cplLastCabrInfo.getCabrdt());
            KnbCbin tblKnbCbin = KnbCbinDao.selectOne_odb1(custac, cabrdt,
                    false);
            if (CommUtil.isNotNull(tblKnbCbin)) {
                tblKnbCbin.setLscbin(cplLastCabrInfo.getCabrin());
                KnbCbinDao.updateOne_odb1(tblKnbCbin);
            }
        }
        // 更新每一日计提利息
        DpAcctDao.updKnbCbinPrcbin(custac, tmstmp);

        // 查询临时表计提信息，并把明细放入集合中
        List<AcctPredCabrInfo> cplCabrInfos = DpAcctDao.selKnbCbinByCustac(
                custac, bigndt, endddt, false);
        for (AcctPredCabrInfo cplCabrInfo : cplCabrInfos) {
            cplPredCabrInfos.getCplPreCabrInfo().add(cplCabrInfo);
        }

        return cplPredCabrInfos;
    }

    /**
     * 查询智能储蓄产品各档期利率
     */
    /*
     * @Override public SmartAcctInstOut qryAcctInst(String ecpdcd, String
     * cupdcd) { SmartAcctInstOut cplAcctInstOut = SysUtil
     * .getInstance(SmartAcctInstOut.class);
     * 
     * String curIntrcd = "I001"; // 活期利率代码 String fixIntrcd = "I002"; // 定期利率代码
     * 
     * IoPbStaPublic pbpub = SysUtil.getInstance(IoPbStaPublic.class);
     * 
     * KupDppb tblKupDppb = KupDppbDao.selectOne_odb1(cupdcd, true); KupDppb
     * tblKupDppb1 = KupDppbDao.selectOne_odb1(ecpdcd, true);
     * 
     * KupDppb_intr tblIntr = KupDppb_intrDao.selectOne_odb1(cupdcd,
     * tblKupDppb.getPdcrcy(), false); KupDppb_intr tblIntr1 =
     * KupDppb_intrDao.selectOne_odb1(ecpdcd, tblKupDppb1.getPdcrcy(), false);
     * 
     * if (CommUtil.isNotNull(tblIntr)) { curIntrcd = tblIntr.getIntrcd(); }
     * 
     * if (CommUtil.isNotNull(tblIntr1)) { fixIntrcd = tblIntr1.getIntrcd(); }
     * 
     * // IntrPublicEntity entity = new IntrPublicEntity(); IoPbIntrPublicEntity
     * entity = SysUtil .getInstance(IoPbIntrPublicEntity.class);
     * 
     * String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); // 交易日期
     * 
     * // 计算活期利率 entity.setIntrcd(curIntrcd);
     * entity.setCrcycd(tblKupDppb.getPdcrcy());
     * entity.setDepttm(E_TERMCD.T000); entity.setTrandt(trandt);
     * 
     * pbpub.IntrPublic_genIntrvl(entity, 0);
     * 
     * cplAcctInstOut.setBaseir(entity.getIntrvl()); // 活期利率
     * 
     * // 计算定期各档期利率 // IntrPublicEntity entity1 = new IntrPublicEntity();
     * IoPbIntrPublicEntity entity1 = SysUtil
     * .getInstance(IoPbIntrPublicEntity.class); entity1.setIntrcd(fixIntrcd);
     * entity1.setCrcycd(tblKupDppb1.getPdcrcy());
     * entity1.setDepttm(E_TERMCD.T101); entity1.setTrandt(trandt);
     * 
     * // 1天 pbpub.IntrPublic_genIntrvl(entity1, 1);
     * 
     * cplAcctInstOut.setOnedir(entity1.getIntrvl()); // 一天利率
     * 
     * // 7天 entity1.setDepttm(E_TERMCD.T107);
     * pbpub.IntrPublic_genIntrvl(entity1, 7);
     * cplAcctInstOut.setSevdir(entity1.getIntrvl()); // 7天利率
     * 
     * // 3月 entity1.setDepttm(E_TERMCD.T203);
     * pbpub.IntrPublic_genIntrvl(entity1, 90);
     * cplAcctInstOut.setThrmir(entity1.getIntrvl()); // 3月利率
     * 
     * // 6月 entity1.setDepttm(E_TERMCD.T206);
     * pbpub.IntrPublic_genIntrvl(entity1, 180);
     * cplAcctInstOut.setSixmir(entity1.getIntrvl()); // 6月利率
     * 
     * // 一年 entity1.setDepttm(E_TERMCD.T301);
     * pbpub.IntrPublic_genIntrvl(entity1, 360);
     * cplAcctInstOut.setOneyir(entity1.getIntrvl()); // 一年利率
     * 
     * // 两年 entity1.setDepttm(E_TERMCD.T302);
     * pbpub.IntrPublic_genIntrvl(entity1, 720);
     * cplAcctInstOut.setTwoyir(entity1.getIntrvl()); // 两年利率
     * 
     * // 三年 entity1.setDepttm(E_TERMCD.T303);
     * pbpub.IntrPublic_genIntrvl(entity1, 1080);
     * cplAcctInstOut.setThryir(entity1.getIntrvl()); // 三年利率
     * 
     * // 五年 entity1.setDepttm(E_TERMCD.T305);
     * pbpub.IntrPublic_genIntrvl(entity1, 1800);
     * cplAcctInstOut.setFivyir(entity1.getIntrvl()); // 五年利率
     * 
     * return cplAcctInstOut; }
     */

    /**
     * 存款账户数据文件生成
     * 
     * @author JX.Chang
     */
    @Override
    public void genDpCustFiles(String lstrdt) {

        // 产生文件的日期目录
        String lstrdtPath = lstrdt + "/";

        /******* 活期账户（导出数据）开始 ***********/
        // 获取文件生产路径
        KnpParameter para1 = KnpParameterDao.selectOne_odb1("ACCT", "dpfile", "01", "%",
                true);
        String path1 = para1.getParm_value1();
        path1 = para1.getParm_value1() + lstrdtPath;
        bizlog.debug("文件产生路径 path:[" + path1 + "]");
        // 获取文件名
        String filename1 = para1.getParm_value2();
        bizlog.debug("文件名称 filename:[" + filename1 + "]");
        // 获取是否产生文件标志
        String isCreateFlg1 = CommUtil.nvl(para1.getParm_value3(), "Y");
        bizlog.debug("文件产生标志 :[" + isCreateFlg1 + "]");
        // 获取加载模式（增量/全量）
        String createMode1 = CommUtil.nvl(para1.getParm_value5(), "ZL");
        bizlog.debug("文件加载模式 :[" + createMode1 + "]");
        if (CommUtil.equals(isCreateFlg1, "Y")) {
            final LttsFileWriter file = new LttsFileWriter(path1, filename1);
            Params params = new Params();
            String namedSqlId = "";// 查询数据集的命名sql
            if (CommUtil.equals(createMode1, "QL")) {
                namedSqlId = DpAcctDao.namedsql_selKnaAcct;
            }
            if (true) {
                file.open();
                try {
                    DaoUtil.selectList(namedSqlId, params,
                            new CursorHandler<KnaAcct>() {
                                @Override
                                public boolean handle(int index, KnaAcct entity) {
                                    // 写文件
                                    StringBuffer file_Info = SysUtil
                                            .getInstance(StringBuffer.class);
                                    String acctno = entity.getAcctno();
                                    String acctna = entity.getAcctna();
                                    String custno = entity.getCustno();
                                    String crcycd = entity.getCrcycd();
                                    String csextg = entity.getCsextg()
                                            .getValue();
                                    String depttm = (CommUtil.isNotNull(entity
                                            .getDepttm()) ? entity.getDepttm()
                                            .getValue() : "");
                                    String matudt = (CommUtil.isNotNull(entity
                                            .getMatudt()) ? entity.getMatudt()
                                            : "");
                                    String bgindt = entity.getBgindt();
                                    String brchno = entity.getBrchno();
                                    String opendt = entity.getOpendt();
                                    String opensq = entity.getOpensq();
                                    String closdt = (CommUtil.isNotNull(entity
                                            .getClosdt()) ? entity.getClosdt()
                                            : "");
                                    String clossq = (CommUtil.isNotNull(entity
                                            .getClossq()) ? entity.getClossq()
                                            : "");
                                    String onlnbl = entity.getOnlnbl()
                                            .toString();
                                    String upbldt = (CommUtil.isNotNull(entity
                                            .getUpbldt()) ? entity.getUpbldt()
                                            : "");
                                    String lastbl = entity.getLastbl()
                                            .toString();
                                    String lstrdt1 = entity.getLstrdt();
                                    String lstrsq = (CommUtil.isNotNull(entity
                                            .getLstrsq()) ? entity.getLstrsq()
                                            : "");
                                    String prodcd = entity.getProdcd();
                                    String pddpfg = entity.getPddpfg()
                                            .getValue();
                                    String hdmxmy = (CommUtil.isNotNull(entity
                                            .getHdmxmy()) ? entity.getHdmxmy()
                                            .toString() : "");
                                    String hdmimy = (CommUtil.isNotNull(entity
                                            .getHdmimy()) ? entity.getHdmimy()
                                            .toString() : "");
                                    String custac = entity.getCustac();
                                    String trsvtp = (CommUtil.isNotNull(entity
                                            .getTrsvtp()) ? entity.getTrsvtp()
                                            .getValue() : "");
                                    String bkmony = (CommUtil.isNotNull(entity
                                            .getBkmony()) ? entity.getBkmony()
                                            .toString() : "");
                                    String opmony = entity.getOpmony()
                                            .toString();
                                    String debttp = entity.getDebttp()
                                            .getValue();
                                    String acctst = entity.getAcctst()
                                            .getValue();
                                    String sleptg = entity.getSleptg()
                                            .getValue();
                                    String spectp = entity.getSpectp()
                                            .getValue();
                                    String accttp = entity.getAccttp()
                                            .getValue();
                                    String acctcd = entity.getAcctcd();
                                    String corpno = (CommUtil.isNotNull(entity
                                            .getCorpno()) ? entity.getCorpno()
                                            : "");
                                    String tmstmp = (CommUtil.isNotNull(entity
                                            .getTmstmp()) ? entity.getTmstmp()
                                            .toString() : "");
                                    String fengefu = "^";
                                    file_Info.append(acctno).append(fengefu)
                                            .append(acctna);
                                    file_Info.append(fengefu).append(custno)
                                            .append(fengefu).append(crcycd)
                                            .append(fengefu).append(csextg);
                                    file_Info.append(fengefu).append(depttm)
                                            .append(fengefu).append(matudt)
                                            .append(fengefu).append(bgindt);
                                    file_Info.append(fengefu).append(brchno)
                                            .append(fengefu).append(opendt)
                                            .append(fengefu).append(opensq);
                                    file_Info.append(fengefu).append(closdt)
                                            .append(fengefu).append(clossq)
                                            .append(fengefu).append(onlnbl);
                                    file_Info.append(fengefu).append(upbldt)
                                            .append(fengefu).append(lastbl)
                                            .append(fengefu).append(lstrdt1);
                                    file_Info.append(fengefu).append(lstrsq)
                                            .append(fengefu).append(prodcd)
                                            .append(fengefu).append(pddpfg);
                                    file_Info.append(fengefu).append(hdmxmy)
                                            .append(fengefu).append(hdmimy)
                                            .append(fengefu).append(custac);
                                    file_Info.append(fengefu).append(trsvtp)
                                            .append(fengefu).append(bkmony)
                                            .append(fengefu).append(opmony);
                                    file_Info.append(fengefu).append(debttp)
                                            .append(fengefu).append(acctst)
                                            .append(fengefu).append(sleptg);
                                    file_Info.append(fengefu).append(spectp)
                                            .append(fengefu).append(accttp)
                                            .append(fengefu).append(acctcd);
                                    file_Info.append(fengefu).append(corpno)
                                            .append(fengefu).append(tmstmp);

                                    file.write(file_Info.toString());
                                    return true;
                                }
                            });

                } finally {
                    file.close();
                }

            }

            bizlog.debug("活期账户（导出数据）" + filename1 + "文件产生完成");
        }
        /******* 活期账户（导出数据）结束 ***********/

        /******* 智能储蓄账户（导出数据）开始 ***********/
        // 获取文件生产路径
        KnpParameter para2 = KnpParameterDao.selectOne_odb1("ACCT", "dpfile", "02", "%",
                true);
        String path2 = para2.getParm_value1();
        path2 = para2.getParm_value1() + lstrdtPath;
        bizlog.debug("文件产生路径 path:[" + path2 + "]");
        // 获取文件名
        String filename2 = para2.getParm_value2();
        bizlog.debug("文件名称 filename:[" + filename2 + "]");
        // 获取是否产生文件标志
        String isCreateFlg2 = CommUtil.nvl(para2.getParm_value3(), "Y");
        bizlog.debug("文件产生标志 :[" + isCreateFlg2 + "]");
        // 获取加载模式（增量/全量）
        String createMode2 = CommUtil.nvl(para2.getParm_value5(), "ZL");
        bizlog.debug("文件加载模式 :[" + createMode2 + "]");
        if (CommUtil.equals(isCreateFlg2, "Y")) {
            final LttsFileWriter file = new LttsFileWriter(path2, filename2);
            // List<KnaFxac> entities = null;
            Params params = new Params();
            String namedSqlId = "";// 查询数据集的命名sql
            if (CommUtil.equals(createMode2, "QL")) {
                // entities = DpAcctDao.selKnaFxac(false);
                namedSqlId = DpAcctDao.namedsql_selKnaFxac;
            }
            // else {
            // entities = CuDao.selCustInfoByDate(lstrdt, false);
            // }
            if (true) {
                file.open();
                try {
                    DaoUtil.selectList(namedSqlId, params,
                            new CursorHandler<KnaFxac>() {
                                @Override
                                public boolean handle(int index, KnaFxac entity) {
                                    // 写文件
                                    StringBuffer file_Info = SysUtil
                                            .getInstance(StringBuffer.class);
                                    String acctno = entity.getAcctno();
                                    String acctna = entity.getAcctna();
                                    String custno = entity.getCustno();
                                    String crcycd = entity.getCrcycd();
                                    String csextg = entity.getCsextg()
                                            .getValue();
                                    String depttm = (CommUtil.isNotNull(entity
                                            .getDepttm()) ? entity.getDepttm()
                                            .getValue() : "");
                                    String matudt = (CommUtil.isNotNull(entity
                                            .getMatudt()) ? entity.getMatudt()
                                            : "");
                                    String bgindt = entity.getBgindt();
                                    String brchno = entity.getBrchno();
                                    String opendt = entity.getOpendt();
                                    String opensq = entity.getOpensq();
                                    String closdt = (CommUtil.isNotNull(entity
                                            .getClosdt()) ? entity.getClosdt()
                                            : "");
                                    String clossq = (CommUtil.isNotNull(entity
                                            .getClossq()) ? entity.getClossq()
                                            : "");
                                    String onlnbl = entity.getOnlnbl()
                                            .toString();
                                    String upbldt = (CommUtil.isNotNull(entity
                                            .getUpbldt()) ? entity.getUpbldt()
                                            : "");
                                    String lastbl = entity.getLastbl()
                                            .toString();
                                    String lstrdt1 = entity.getLstrdt();
                                    String lstrsq = (CommUtil.isNotNull(entity
                                            .getLstrsq()) ? entity.getLstrsq()
                                            : "");
                                    String prodcd = entity.getProdcd();
                                    String pddpfg = entity.getPddpfg()
                                            .getValue();
                                    String hdmxmy = (CommUtil.isNotNull(entity
                                            .getHdmxmy()) ? entity.getHdmxmy()
                                            .toString() : "");
                                    String hdmimy = (CommUtil.isNotNull(entity
                                            .getHdmimy()) ? entity.getHdmimy()
                                            .toString() : "");
                                    String custac = entity.getCustac();
                                    String trsvtp = (CommUtil.isNotNull(entity
                                            .getTrsvtp()) ? entity.getTrsvtp()
                                            .getValue() : "");
                                    String bkmony = (CommUtil.isNotNull(entity
                                            .getBkmony()) ? entity.getBkmony()
                                            .toString() : "");
                                    String opmony = entity.getOpmony()
                                            .toString();
                                    String debttp = entity.getDebttp()
                                            .getValue();
                                    String acctst = entity.getAcctst()
                                            .getValue();
                                    String sleptg = entity.getSleptg()
                                            .getValue();
                                    String spectp = entity.getSpectp()
                                            .getValue();
                                    String accttp = entity.getAccttp()
                                            .getValue();
                                    String acctcd = entity.getAcctcd();
                                    String corpno = (CommUtil.isNotNull(entity
                                            .getCorpno()) ? entity.getCorpno()
                                            : "");
                                    /*
                                     * String datetm =
                                     * (CommUtil.isNotNull(entity .getDatetm())
                                     * ? entity.getDatetm() : "");
                                     */
                                    String tmstmp = (CommUtil.isNotNull(entity
                                            .getTmstmp()) ? entity.getTmstmp()
                                            .toString() : "");
                                    String fengefu = "^";
                                    // 拼接打印
                                    file_Info.append(acctno).append(fengefu)
                                            .append(acctna);
                                    file_Info.append(fengefu).append(custno)
                                            .append(fengefu).append(crcycd)
                                            .append(fengefu).append(csextg);
                                    file_Info.append(fengefu).append(depttm)
                                            .append(fengefu).append(matudt)
                                            .append(fengefu).append(bgindt);
                                    file_Info.append(fengefu).append(brchno)
                                            .append(fengefu).append(opendt)
                                            .append(fengefu).append(opensq);
                                    file_Info.append(fengefu).append(closdt)
                                            .append(fengefu).append(clossq)
                                            .append(fengefu).append(onlnbl);
                                    file_Info.append(fengefu).append(upbldt)
                                            .append(fengefu).append(lastbl)
                                            .append(fengefu).append(lstrdt1);
                                    file_Info.append(fengefu).append(lstrsq)
                                            .append(fengefu).append(prodcd)
                                            .append(fengefu).append(pddpfg);
                                    file_Info.append(fengefu).append(hdmxmy)
                                            .append(fengefu).append(hdmimy)
                                            .append(fengefu).append(custac);
                                    file_Info.append(fengefu).append(trsvtp)
                                            .append(fengefu).append(bkmony)
                                            .append(fengefu).append(opmony);
                                    file_Info.append(fengefu).append(debttp)
                                            .append(fengefu).append(acctst)
                                            .append(fengefu).append(sleptg);
                                    file_Info.append(fengefu).append(spectp)
                                            .append(fengefu).append(accttp)
                                            .append(fengefu).append(acctcd);
                                    file_Info.append(fengefu).append(corpno)
                                            // .append(fengefu).append(datetm)
                                            .append(fengefu).append(tmstmp);
                                    // 打印文件
                                    file.write(file_Info.toString());
                                    return true;
                                }
                            });
                    // if (CommUtil.isNotNull(entities)) {
                    // KnaFxac entity = SysUtil.getInstance(KnaFxac.class);
                    // for (int i = 0; i < entities.size(); i++) {
                    // entity = entities.get(i);
                    // // 写文件
                    // StringBuffer file_Info =
                    // SysUtil.getInstance(StringBuffer.class);
                    // String acctno = entity.getAcctno();
                    // String acctna = entity.getAcctna();
                    // String custno = entity.getCustno();
                    // String crcycd = entity.getCrcycd().getValue();
                    // String csextg = entity.getCsextg().getValue();
                    // String depttm = (CommUtil.isNotNull(entity.getDepttm()) ?
                    // entity.getDepttm().getValue() : "");
                    // String matudt = (CommUtil.isNotNull(entity.getMatudt()) ?
                    // entity.getMatudt() : "");
                    // String bgindt = entity.getBgindt();
                    // String brchno = entity.getBrchno();
                    // String opendt = entity.getOpendt();
                    // String opensq = entity.getOpensq();
                    // String closdt = (CommUtil.isNotNull(entity.getClosdt()) ?
                    // entity.getClosdt() : "");
                    // String clossq = (CommUtil.isNotNull(entity.getClossq()) ?
                    // entity.getClossq() : "");
                    // String onlnbl = entity.getOnlnbl().toString();
                    // String upbldt = (CommUtil.isNotNull(entity.getUpbldt()) ?
                    // entity.getUpbldt() : "");
                    // String lastbl = entity.getLastbl().toString();
                    // String lstrdt1 = entity.getLstrdt();
                    // String lstrsq = (CommUtil.isNotNull(entity.getLstrsq()) ?
                    // entity.getLstrsq() : "");
                    // String prodcd = entity.getProdcd();
                    // String pddpfg = entity.getPddpfg().getValue();
                    // String hdmxmy = (CommUtil.isNotNull(entity.getHdmxmy()) ?
                    // entity.getHdmxmy().toString() : "");
                    // String hdmimy = (CommUtil.isNotNull(entity.getHdmimy()) ?
                    // entity.getHdmimy().toString() : "");
                    // String custac = entity.getCustac();
                    // String trsvtp = (CommUtil.isNotNull(entity.getTrsvtp()) ?
                    // entity.getTrsvtp().getValue() : "");
                    // String bkmony = (CommUtil.isNotNull(entity.getBkmony()) ?
                    // entity.getBkmony().toString() : "");
                    // String opmony = entity.getOpmony().toString();
                    // String debttp = entity.getDebttp().getValue();
                    // String acctst = entity.getAcctst().getValue();
                    // String sleptg = entity.getSleptg().getValue();
                    // String spectp = entity.getSpectp().getValue();
                    // String accttp = entity.getAccttp().getValue();
                    // String acctcd = entity.getAcctcd();
                    // String corpno = (CommUtil.isNotNull(entity.getCorpno()) ?
                    // entity.getCorpno() : "");
                    // String datetm = (CommUtil.isNotNull(entity.getDatetm()) ?
                    // entity.getDatetm() : "");
                    // String tmstmp = (CommUtil.isNotNull(entity.gettmstmp()) ?
                    // entity.gettmstmp().toString() : "");
                    // String fengefu = "^";
                    // //拼接打印
                    // file_Info.append(acctno).append(fengefu).append(acctna);
                    // file_Info.append(fengefu).append(custno).append(fengefu).append(crcycd).append(fengefu).append(csextg);
                    // file_Info.append(fengefu).append(depttm).append(fengefu).append(matudt).append(fengefu).append(bgindt);
                    // file_Info.append(fengefu).append(brchno).append(fengefu).append(opendt).append(fengefu).append(opensq);
                    // file_Info.append(fengefu).append(closdt).append(fengefu).append(clossq).append(fengefu).append(onlnbl);
                    // file_Info.append(fengefu).append(upbldt).append(fengefu).append(lastbl).append(fengefu).append(lstrdt1);
                    // file_Info.append(fengefu).append(lstrsq).append(fengefu).append(prodcd).append(fengefu).append(pddpfg);
                    // file_Info.append(fengefu).append(hdmxmy).append(fengefu).append(hdmimy).append(fengefu).append(custac);
                    // file_Info.append(fengefu).append(trsvtp).append(fengefu).append(bkmony).append(fengefu).append(opmony);
                    // file_Info.append(fengefu).append(debttp).append(fengefu).append(acctst).append(fengefu).append(sleptg);
                    // file_Info.append(fengefu).append(spectp).append(fengefu).append(accttp).append(fengefu).append(acctcd);
                    // file_Info.append(fengefu).append(corpno).append(fengefu).append(datetm).append(fengefu).append(tmstmp);
                    // //打印文件
                    // file.write(file_Info.toString());
                    // }
                    // }
                } finally {
                    file.close();
                }

            }

            bizlog.debug("智能储蓄账户（导出数据）" + filename2 + "文件产生完成");
        }
        /******* 智能储蓄账户（导出数据）结束 ***********/

        /******* 智能储蓄交易（导出数据）开始 ***********/
        // 获取文件生产路径
        KnpParameter para3 = KnpParameterDao.selectOne_odb1("ACCT", "dpfile", "03", "%",
                true);
        String path3 = para3.getParm_value1();
        path3 = para3.getParm_value1() + lstrdtPath;
        bizlog.debug("文件产生路径 path:[" + path3 + "]");
        // 获取文件名
        String filename3 = para3.getParm_value2();
        bizlog.debug("文件名称 filename:[" + filename3 + "]");
        // 获取是否产生文件标志
        String isCreateFlg3 = CommUtil.nvl(para3.getParm_value3(), "Y");
        bizlog.debug("文件产生标志 :[" + isCreateFlg3 + "]");
        // 获取加载模式（增量/全量）
        String createMode3 = CommUtil.nvl(para3.getParm_value5(), "ZL");
        bizlog.debug("文件加载模式 :[" + createMode3 + "]");
        if (CommUtil.equals(isCreateFlg3, "Y")) {
            final LttsFileWriter file = new LttsFileWriter(path3, filename3);
            // List<KnlBill> entities = null;
            Params params = new Params();
            String namedSqlId = "";// 查询数据集的命名sql
            String datetm = null;
            if (CommUtil.equals(createMode3, "QL")) {
                // entities = DpAcctDao.selKnlBill(false);
                namedSqlId = DpAcctDao.namedsql_selKnlBill;
            } else {
                // entities = DpAcctDao.selKnlBillByDate(lstrdt, false);
                namedSqlId = DpAcctDao.namedsql_selKnlBillByDate;
                datetm = lstrdt;
            }
            params.add("datetm", datetm);
            if (true) {
                file.open();
                try {
                    DaoUtil.selectList(namedSqlId, params,
                            new CursorHandler<KnlBill>() {
                                @Override
                                public boolean handle(int index, KnlBill entity) {
                                    // 写文件
                                    StringBuffer file_Info = SysUtil
                                            .getInstance(StringBuffer.class);
                                    String acctno = (CommUtil.nvl(
                                            entity.getAcctno(), ""));
                                    String detlsq = (CommUtil.isNotNull(entity
                                            .getDetlsq()) ? entity.getDetlsq()
                                            .toString() : "");
                                    String trandt = (CommUtil.nvl(
                                            entity.getTrandt(), ""));
                                    String tranbr = (CommUtil.nvl(
                                            entity.getTranbr(), ""));
                                    String openbr = (CommUtil.nvl(
                                            entity.getOpenbr(), ""));
                                    String acctna = (CommUtil.nvl(
                                            entity.getAcctna(), ""));
                                    String amntcd = (CommUtil.isNotNull(entity
                                            .getAmntcd()) ? entity.getAmntcd()
                                            .getValue() : "");
                                    String trancy = (CommUtil.isNotNull(entity
                                            .getTrancy()) ? entity.getTrancy()
                                            : "");
                                    String csextg = (CommUtil.isNotNull(entity
                                            .getCsextg()) ? entity.getCsextg()
                                            .getValue() : "");
                                    String tranam = (CommUtil.isNotNull(entity
                                            .getTranam()) ? entity.getTranam()
                                            .toString() : "");
                                    String acctbl = (CommUtil.isNotNull(entity
                                            .getAcctbl()) ? entity.getAcctbl()
                                            .toString() : "");
                                    String cardno = (CommUtil.nvl(
                                            entity.getCardno(), ""));
                                    String custac = (CommUtil.nvl(
                                            entity.getCustac(), ""));
                                    String acseno = (CommUtil.nvl(
                                            entity.getAcseno(), ""));
                                    String dcmttp = (CommUtil.isNotNull(entity
                                            .getDcmttp()) ? entity.getDcmttp()
                                            .getValue() : "");
                                    String dcbtno = (CommUtil.nvl(
                                            entity.getDcbtno(), ""));
                                    String dcsrno = (CommUtil.nvl(
                                            entity.getDcsrno(), ""));
                                    String smrycd = (CommUtil.isNotNull(entity
                                            .getSmrycd()) ? entity.getSmrycd() : "");
                                    String smryds = (CommUtil.nvl(
                                            entity.getSmryds(), ""));
                                    String bankcd = (CommUtil.nvl(
                                            entity.getBankcd(), ""));
                                    String bankna = (CommUtil.nvl(
                                            entity.getBankna(), ""));
                                    String opcuac = (CommUtil.nvl(
                                            entity.getOpcuac(), ""));
                                    String opacna = (CommUtil.nvl(
                                            entity.getOpacna(), ""));
                                    String oppomk = (CommUtil.nvl(
                                            entity.getOppomk(), ""));
                                    String remark = (CommUtil.nvl(
                                            entity.getRemark(), ""));
                                    String bgindt = (CommUtil.nvl(
                                            entity.getBgindt(), ""));
                                    String servtp = (CommUtil.isNotNull(entity
                                            .getServtp()) ? entity.getServtp()
                                            		: "");
                                    String ckaccd = (CommUtil.nvl(
                                            entity.getCkaccd(), ""));
                                    String intrcd = (CommUtil.nvl(
                                            entity.getIntrcd(), ""));
                                    String cdtrfg = (CommUtil.isNotNull(entity
                                            .getCstrfg()) ? entity.getCstrfg()
                                            .getValue() : "");
                                    String ussqno = (CommUtil.nvl(
                                            entity.getUssqno(), ""));
                                    String userid = (CommUtil.nvl(
                                            entity.getUserid(), ""));
                                    String machdt = (CommUtil.nvl(
                                            entity.getMachdt(), ""));
                                    String transq = (CommUtil.nvl(
                                            entity.getTransq(), ""));
                                    String procsq = (CommUtil.nvl(
                                            entity.getProcsq(), ""));
                                    String trantm = (CommUtil.isNotNull(entity
                                            .getTrantm()) ? entity.getTrantm()
                                            .toString() : "");
                                    String strktp = (CommUtil.isNotNull(entity
                                            .getStrktp()) ? entity.getStrktp()
                                            .getValue() : "");
                                    String pmcrac = (CommUtil.isNotNull(entity
                                            .getPmcrac()) ? entity.getPmcrac()
                                            .getValue() : "");
                                    String msacdt = (CommUtil.nvl(
                                            entity.getMsacdt(), ""));
                                    String msacsq = (CommUtil.nvl(
                                            entity.getMsacsq(), ""));
                                    String origtq = (CommUtil.nvl(
                                            entity.getOrigtq(), ""));
                                    String origpq = (CommUtil.nvl(
                                            entity.getOrigpq(), ""));
                                    String stacps = (CommUtil.isNotNull(entity
                                            .getStacps()) ? entity.getStacps()
                                            .getValue() : "");
                                    String corrtg = (CommUtil.isNotNull(entity
                                            .getCorrtg()) ? entity.getCorrtg()
                                            .getValue() : "");
                                    String corpno = entity.getCorpno();
                                    /*
                                     * String datetm = (CommUtil.nvl(
                                     * entity.getDatetm(), ""));
                                     */
                                    String tmstmp = (CommUtil.isNotNull(entity
                                            .getTmstmp()) ? entity.getTmstmp()
                                            .toString() : "");
                                    String fengefu = "^";
                                    // 拼接打印
                                    file_Info.append(acctno).append(fengefu)
                                            .append(detlsq);
                                    file_Info.append(fengefu).append(trandt)
                                            .append(fengefu).append(tranbr)
                                            .append(fengefu).append(openbr);
                                    file_Info.append(fengefu).append(acctna)
                                            .append(fengefu).append(amntcd)
                                            .append(fengefu).append(trancy);
                                    file_Info.append(fengefu).append(csextg)
                                            .append(fengefu).append(tranam)
                                            .append(fengefu).append(acctbl);
                                    file_Info.append(fengefu).append(cardno)
                                            .append(fengefu).append(custac)
                                            .append(fengefu).append(acseno);
                                    file_Info.append(fengefu).append(dcmttp)
                                            .append(fengefu).append(dcbtno)
                                            .append(fengefu).append(dcsrno);
                                    file_Info.append(fengefu).append(smrycd)
                                            .append(fengefu).append(smryds)
                                            .append(fengefu).append(bankcd);
                                    file_Info.append(fengefu).append(bankna)
                                            .append(fengefu).append(opcuac)
                                            .append(fengefu).append(opacna);
                                    file_Info.append(fengefu).append(oppomk)
                                            .append(fengefu).append(remark)
                                            .append(fengefu).append(bgindt);
                                    file_Info.append(fengefu).append(servtp)
                                            .append(fengefu).append(ckaccd)
                                            .append(fengefu).append(intrcd);
                                    file_Info.append(fengefu).append(cdtrfg)
                                            .append(fengefu).append(ussqno)
                                            .append(fengefu).append(userid);
                                    file_Info.append(fengefu).append(machdt)
                                            .append(fengefu).append(transq)
                                            .append(fengefu).append(procsq);
                                    file_Info.append(fengefu).append(trantm)
                                            .append(fengefu).append(strktp)
                                            .append(fengefu).append(pmcrac);
                                    file_Info.append(fengefu).append(msacdt)
                                            .append(fengefu).append(msacsq)
                                            .append(fengefu).append(origtq);
                                    file_Info.append(fengefu).append(origpq)
                                            .append(fengefu).append(stacps)
                                            .append(fengefu).append(corrtg);
                                    file_Info.append(fengefu).append(corpno)
                                            // .append(fengefu).append(datetm)
                                            .append(fengefu).append(tmstmp);
                                    file.write(file_Info.toString());
                                    return true;
                                }
                            });

                    // if (CommUtil.isNotNull(entities)) {
                    // KnlBill entity = SysUtil.getInstance(KnlBill.class);
                    // for (int i = 0; i < entities.size(); i++) {
                    // entity = entities.get(i);
                    // // 写文件
                    // StringBuffer file_Info =
                    // SysUtil.getInstance(StringBuffer.class);
                    // String acctno = (CommUtil.nvl(entity.getAcctno(), ""));
                    // String detlsq = (CommUtil.isNotNull(entity.getDetlsq()) ?
                    // entity.getDetlsq().toString() : "");
                    // String trandt = (CommUtil.nvl(entity.getTrandt(), ""));
                    // String tranbr = (CommUtil.nvl(entity.getTranbr(), ""));
                    // String openbr = (CommUtil.nvl(entity.getOpenbr(), ""));
                    // String acctna = (CommUtil.nvl(entity.getAcctna(), ""));
                    // String amntcd = (CommUtil.isNotNull(entity.getAmntcd()) ?
                    // entity.getAmntcd().getValue() : "");
                    // String trancy = (CommUtil.isNotNull(entity.getTrancy()) ?
                    // entity.getTrancy().getValue() : "");
                    // String csextg = (CommUtil.isNotNull(entity.getCsextg()) ?
                    // entity.getCsextg().getValue() : "");
                    // String tranam = (CommUtil.isNotNull(entity.getTranam()) ?
                    // entity.getTranam().toString() : "");
                    // String acctbl = (CommUtil.isNotNull(entity.getAcctbl()) ?
                    // entity.getAcctbl().toString() : "");
                    // String cardno = (CommUtil.nvl(entity.getCardno(), ""));
                    // String custac = (CommUtil.nvl(entity.getCustac(), ""));
                    // String acseno = (CommUtil.nvl(entity.getAcseno(), ""));
                    // String dcmttp = (CommUtil.isNotNull(entity.getDcmttp()) ?
                    // entity.getDcmttp().getValue() : "");
                    // String dcbtno = (CommUtil.nvl(entity.getDcbtno(), ""));
                    // String dcsrno = (CommUtil.nvl(entity.getDcsrno(), ""));
                    // String smrycd = (CommUtil.nvl(entity.getSmrycd(), ""));
                    // String smryds = (CommUtil.nvl(entity.getSmryds(), ""));
                    // String bankcd = (CommUtil.nvl(entity.getBankcd(), ""));
                    // String bankna = (CommUtil.nvl(entity.getBankna(), ""));
                    // String opcuac = (CommUtil.nvl(entity.getOpcuac(), ""));
                    // String opacna = (CommUtil.nvl(entity.getOpacna(), ""));
                    // String oppomk = (CommUtil.nvl(entity.getOppomk(), ""));
                    // String remark = (CommUtil.nvl(entity.getRemark(), ""));
                    // String bgindt = (CommUtil.nvl(entity.getBgindt(), ""));
                    // String servtp = (CommUtil.nvl(entity.getServtp(), ""));
                    // String ckaccd = (CommUtil.nvl(entity.getCkaccd(), ""));
                    // String intrcd = (CommUtil.nvl(entity.getIntrcd(), ""));
                    // String cdtrfg = (CommUtil.isNotNull(entity.getCdtrfg()) ?
                    // entity.getCdtrfg().getValue() : "");
                    // String ussqno = (CommUtil.nvl(entity.getUssqno(), ""));
                    // String userid = (CommUtil.nvl(entity.getUserid(), ""));
                    // String machdt = (CommUtil.nvl(entity.getMachdt(), ""));
                    // String transq = (CommUtil.nvl(entity.getTransq(), ""));
                    // String procsq = (CommUtil.nvl(entity.getProcsq(), ""));
                    // String trantm = (CommUtil.isNotNull(entity.getTrantm()) ?
                    // entity.getTrantm().toString() : "");
                    // String strktp = (CommUtil.isNotNull(entity.getStrktp()) ?
                    // entity.getStrktp().getValue() : "");
                    // String pmcrac = (CommUtil.isNotNull(entity.getPmcrac()) ?
                    // entity.getPmcrac().getValue() : "");
                    // String msacdt = (CommUtil.nvl(entity.getMsacdt(), ""));
                    // String msacsq = (CommUtil.nvl(entity.getMsacsq(), ""));
                    // String origtq = (CommUtil.nvl(entity.getOrigtq(), ""));
                    // String origpq = (CommUtil.nvl(entity.getOrigpq(), ""));
                    // String stacps = (CommUtil.isNotNull(entity.getStacps()) ?
                    // entity.getStacps().getValue() : "");
                    // String corrtg = (CommUtil.isNotNull(entity.getCorrtg()) ?
                    // entity.getCorrtg().getValue() : "");
                    // String corpno = entity.getCorpno();
                    // String datetm = (CommUtil.nvl(entity.getDatetm(), ""));
                    // String tmstmp = (CommUtil.isNotNull(entity.gettmstmp()) ?
                    // entity.gettmstmp().toString() : "");
                    // String fengefu = "^";
                    // //拼接打印
                    // file_Info.append(acctno).append(fengefu).append(detlsq);
                    // file_Info.append(fengefu).append(trandt).append(fengefu).append(tranbr).append(fengefu).append(openbr);
                    // file_Info.append(fengefu).append(acctna).append(fengefu).append(amntcd).append(fengefu).append(trancy);
                    // file_Info.append(fengefu).append(csextg).append(fengefu).append(tranam).append(fengefu).append(acctbl);
                    // file_Info.append(fengefu).append(cardno).append(fengefu).append(custac).append(fengefu).append(acseno);
                    // file_Info.append(fengefu).append(dcmttp).append(fengefu).append(dcbtno).append(fengefu).append(dcsrno);
                    // file_Info.append(fengefu).append(smrycd).append(fengefu).append(smryds).append(fengefu).append(bankcd);
                    // file_Info.append(fengefu).append(bankna).append(fengefu).append(opcuac).append(fengefu).append(opacna);
                    // file_Info.append(fengefu).append(oppomk).append(fengefu).append(remark).append(fengefu).append(bgindt);
                    // file_Info.append(fengefu).append(servtp).append(fengefu).append(ckaccd).append(fengefu).append(intrcd);
                    // file_Info.append(fengefu).append(cdtrfg).append(fengefu).append(ussqno).append(fengefu).append(userid);
                    // file_Info.append(fengefu).append(machdt).append(fengefu).append(transq).append(fengefu).append(procsq);
                    // file_Info.append(fengefu).append(trantm).append(fengefu).append(strktp).append(fengefu).append(pmcrac);
                    // file_Info.append(fengefu).append(msacdt).append(fengefu).append(msacsq).append(fengefu).append(origtq);
                    // file_Info.append(fengefu).append(origpq).append(fengefu).append(stacps).append(fengefu).append(corrtg);
                    // file_Info.append(fengefu).append(corpno).append(fengefu).append(datetm).append(fengefu).append(tmstmp);
                    // file.write(file_Info.toString());
                    // }
                    // }
                } finally {
                    file.close();
                }

            }

            bizlog.debug("智能储蓄交易（导出数据）" + filename3 + "文件产生完成");
        }
        /******* 智能储蓄交易（导出数据）结束 ***********/

        /******* 负债账户信息表（导出数据）开始 ***********/

        // //获取文件生成路径
        // KnpParameter para4 = KnpParameterDao.selectOne_odb1("ACCT", "dpfile", "04",
        // "%", true);
        // //文件生成路径
        // String path4 = para4.getParm_value1() + lstrdtPath;
        // // 获取文件名
        // String filename4 = para4.getParm_value2();
        //
        // // 获取是否产生文件标志
        // String isCreateFlg4 = CommUtil.nvl(para4.getParm_value3(), "Y");
        //
        // // 获取加载模式（增量/全量）
        // String createMode4 = CommUtil.nvl(para4.getParm_value5(), "ZL");
        // //判断是否生成文件
        // if (CommUtil.equals(isCreateFlg4, "Y")) {
        // final LttsFileWriter file = new LttsFileWriter(path4, filename4);
        // Params params = new Params();
        // String namedSqlId = "";//查询数据集的命名sql
        // String datetm = null;
        // //判断是否加载模式是增量ZL
        // if (CommUtil.equals(createMode4, "ZL")) {
        // //查询所有的卡客户账号对照表信息
        // namedSqlId = DpAcctDao.namedsql_selHKnaAcctByTrandt;
        // datetm = lstrdt;
        // }
        // params.add("acctdt", datetm);
        // file.open();
        // try {
        // DaoUtil.selectList(namedSqlId, params, new
        // CursorHandler<cn.sunline.ltts.busi.dp.tables.DpHisDepo.h_KnaAcct>()
        // {
        // @Override
        // public boolean handle(int index,
        // cn.sunline.ltts.busi.dp.tables.DpHisDepo.h_KnaAcct entity) {
        // String fengefu = "^";
        // StringBuffer sbAcct = new StringBuffer();
        //
        // String acctdt = (CommUtil.isNotNull(entity.getAcctdt()) ?
        // entity.getAcctdt() : "");
        // String acctno = (CommUtil.isNotNull(entity.getAcctno()) ?
        // entity.getAcctno() : "");
        // String acctna = (CommUtil.isNotNull(entity.getAcctna()) ?
        // entity.getAcctna() : "");
        // String custac = (CommUtil.isNotNull(entity.getCustac()) ?
        // entity.getCustac() : "");
        // String crcycd = (CommUtil.isNotNull(entity.getCrcycd()) ?
        // entity.getCrcycd().getValue() : "");
        // String csextg = (CommUtil.isNotNull(entity.getCsextg()) ?
        // entity.getCsextg().getValue() : "");
        // String brchno = (CommUtil.isNotNull(entity.getBrchno()) ?
        // entity.getBrchno() : "");
        // BigDecimal onlnbl = (CommUtil.isNull(entity.getOnlnbl()) ?
        // BigDecimal.ZERO : entity.getOnlnbl());
        // String upbldt = (CommUtil.isNotNull(entity.getUpbldt()) ?
        // entity.getUpbldt() : "");
        // BigDecimal lastbl = (CommUtil.isNull(entity.getLastbl()) ?
        // BigDecimal.ZERO : entity.getLastbl());
        // String acctst = (CommUtil.isNotNull(entity.getAcctst()) ?
        // entity.getAcctst().getValue() : "");
        // String acctcd = (CommUtil.isNotNull(entity.getAcctcd()) ?
        // entity.getAcctcd() : "");
        // String opendt = (CommUtil.isNotNull(entity.getOpendt()) ?
        // entity.getOpendt() : "");
        //
        // sbAcct.append(acctdt).append(fengefu).append(acctno).append(fengefu).append(acctna).append(fengefu)
        // .append(custac).append(fengefu).append(crcycd).append(fengefu).append(csextg)
        // .append(fengefu).append(brchno).append(fengefu).append(onlnbl).append(fengefu)
        // .append(upbldt).append(fengefu).append(lastbl).append(fengefu).append(acctst)
        // .append(fengefu).append(acctcd).append(fengefu).append(opendt);
        //
        // file.write(sbAcct.toString());
        //
        // return true;
        // }
        // });
        //
        // } finally{
        // file.close();
        // }
        // }
        /******* 负债账户信息表（导出数据）结束 ***********/
    }

    @Override
    public void genKnaAcctFile(String lstrdt) {
        // final String delimt = "^"; //分隔符
        String lstrdtPath = lstrdt + "/"; // 日期目录
        KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("ACCT", "dpfile", "01",
                "%", true);
        String path = tblKnpParameter.getParm_value1(); // 文件路径
        path += lstrdtPath;
        bizlog.debug("文件产生路径:[" + path + "]");

        String fileName = tblKnpParameter.getParm_value2(); // 文件名
        bizlog.debug("文件名:[" + fileName + "]");

        String creatFlag = CommUtil.nvl(tblKnpParameter.getParm_value3(), "Y"); // 是否创建文件标志
        bizlog.debug("是否创建文件标志:[" + creatFlag + "]");

        String creatMode = CommUtil.nvl(tblKnpParameter.getParm_value5(), "QL"); // 加载模式(增量/全量)
        bizlog.debug("文件加载模式:[" + creatMode + "]");

        if (creatFlag.equals("Y")) {
            final LttsFileWriter file = new LttsFileWriter(path, fileName);
            Params params = new Params();
            String namedSqlId = "";
            bizlog.debug("creatMode:[" + creatMode + "]");
            if (CommUtil.equals("QL", creatMode)) {
                namedSqlId = DpAcctDao.namedsql_selKnaAcct;
            }

            file.open();
            try {
                DaoUtil.selectList(namedSqlId, params,
                        new CursorHandler<IoDpTable.IoDpKnaAcct>() {
                            @Override
                            public boolean handle(int index,
                                    IoDpTable.IoDpKnaAcct entity) {
                                String acctno = entity.getAcctno();
                                String acctna = entity.getAcctna();
                                String custno = entity.getCustno();
                                String crcycd = entity.getCrcycd();
                                String csextg = entity.getCsextg().getValue();
                                String depttm = (CommUtil.isNotNull(entity
                                        .getDepttm()) ? entity.getDepttm()
                                        .getValue() : "");
                                String matudt = (CommUtil.isNotNull(entity
                                        .getMatudt()) ? entity.getMatudt() : "");
                                String bgindt = entity.getBgindt();
                                String brchno = entity.getBrchno();
                                String opendt = entity.getOpendt();
                                String opensq = entity.getOpensq();
                                String closdt = (CommUtil.isNotNull(entity
                                        .getClosdt()) ? entity.getClosdt() : "");
                                String clossq = (CommUtil.isNotNull(entity
                                        .getClossq()) ? entity.getClossq() : "");
                                String onlnbl = entity.getOnlnbl().toString();
                                String upbldt = (CommUtil.isNotNull(entity
                                        .getUpbldt()) ? entity.getUpbldt() : "");
                                String lastbl = entity.getLastbl().toString();
                                String lstrdt1 = entity.getLstrdt();
                                String lstrsq = (CommUtil.isNotNull(entity
                                        .getLstrsq()) ? entity.getLstrsq() : "");
                                String prodcd = entity.getProdcd();
                                String pddpfg = entity.getPddpfg().getValue();
                                String hdmxmy = (CommUtil.isNotNull(entity
                                        .getHdmxmy()) ? entity.getHdmxmy()
                                        .toString() : "");
                                String hdmimy = (CommUtil.isNotNull(entity
                                        .getHdmimy()) ? entity.getHdmimy()
                                        .toString() : "");
                                String custac = entity.getCustac();
                                String trsvtp = (CommUtil.isNotNull(entity
                                        .getTrsvtp()) ? entity.getTrsvtp()
                                        .getValue() : "");
                                String bkmony = (CommUtil.isNotNull(entity
                                        .getBkmony()) ? entity.getBkmony()
                                        .toString() : "");
                                String opmony = entity.getOpmony().toString();
                                String debttp = entity.getDebttp().getValue();
                                String acctst = entity.getAcctst().getValue();
                                String sleptg = entity.getSleptg().getValue();
                                String spectp = entity.getSpectp().getValue();
                                String accttp = entity.getAccttp().getValue();
                                String acctcd = entity.getAcctcd();
                                String corpno = (CommUtil.isNotNull(entity
                                        .getCorpno()) ? entity.getCorpno() : "");
                                String datetm = (CommUtil.isNotNull(entity
                                        .getDatetm()) ? entity.getDatetm() : "");
                                /*
                                 * String tmstmp = (CommUtil.isNotNull(entity
                                 * .gettmstmp()) ? entity.gettmstmp() : "");
                                 */

                                StringBuffer fileInfo = SysUtil
                                        .getInstance(StringBuffer.class);
                                fileInfo.append(acctno).append(delimt)
                                        .append(acctna);
                                fileInfo.append(delimt).append(custno)
                                        .append(delimt).append(crcycd)
                                        .append(delimt).append(csextg);
                                fileInfo.append(delimt).append(depttm)
                                        .append(delimt).append(matudt)
                                        .append(delimt).append(bgindt);
                                fileInfo.append(delimt).append(brchno)
                                        .append(delimt).append(opendt)
                                        .append(delimt).append(opensq);
                                fileInfo.append(delimt).append(closdt)
                                        .append(delimt).append(clossq)
                                        .append(delimt).append(onlnbl);
                                fileInfo.append(delimt).append(upbldt)
                                        .append(delimt).append(lastbl)
                                        .append(delimt).append(lstrdt1);
                                fileInfo.append(delimt).append(lstrsq)
                                        .append(delimt).append(prodcd)
                                        .append(delimt).append(pddpfg);
                                fileInfo.append(delimt).append(hdmxmy)
                                        .append(delimt).append(hdmimy)
                                        .append(delimt).append(custac);
                                fileInfo.append(delimt).append(trsvtp)
                                        .append(delimt).append(bkmony)
                                        .append(delimt).append(opmony);
                                fileInfo.append(delimt).append(debttp)
                                        .append(delimt).append(acctst)
                                        .append(delimt).append(sleptg);
                                fileInfo.append(delimt).append(spectp)
                                        .append(delimt).append(accttp)
                                        .append(delimt).append(acctcd);
                                fileInfo.append(delimt).append(corpno)
                                        .append(delimt).append(datetm)
                                        .append(delimt);// .append(tmstmp);

                                file.write(fileInfo.toString());
                                return true;
                            }
                        });
            } finally {
                file.close();
            }
            bizlog.debug("活期负债子账户（导出数据）" + fileName + "文件产生完成");
        }
    }

    @Override
    public void genKnaFxacFile(String lstrdt) {
        // final String delimt = "^"; //分隔符
        String lstrdtPath = lstrdt + "/"; // 日期目录
        KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("ACCT", "dpfile", "02",
                "%", true);
        String path = tblKnpParameter.getParm_value1(); // 文件路径
        path += lstrdtPath;
        bizlog.debug("文件产生路径:[" + path + "]");

        String fileName = tblKnpParameter.getParm_value2(); // 文件名
        bizlog.debug("文件名:[" + fileName + "]");

        String creatFlag = CommUtil.nvl(tblKnpParameter.getParm_value3(), "Y"); // 是否创建文件标志
        bizlog.debug("是否创建文件标志:[" + creatFlag + "]");

        String creatMode = CommUtil.nvl(tblKnpParameter.getParm_value5(), "QL"); // 加载模式(增量/全量)
        bizlog.debug("文件加载模式:[" + creatMode + "]");

        if (creatFlag.equals("Y")) {
            final LttsFileWriter file = new LttsFileWriter(path, fileName);
            Params params = new Params();
            String namedSqlId = "";
            bizlog.debug("creatMode:[" + creatMode + "]");
            if (CommUtil.equals("QL", creatMode)) {
                namedSqlId = DpAcctDao.namedsql_selKnaFxac;
            }

            file.open();
            try {
                DaoUtil.selectList(namedSqlId, params,
                        new CursorHandler<IoDpTable.IoKnaFxac>() {
                            @Override
                            public boolean handle(int index,
                                    IoDpTable.IoKnaFxac entity) {
                                // 写文件
                                StringBuffer fileInfo = SysUtil
                                        .getInstance(StringBuffer.class);
                                String acctno = entity.getAcctno();
                                String acctna = entity.getAcctna();
                                String custno = entity.getCustno();
                                String crcycd = entity.getCrcycd();
                                String csextg = entity.getCsextg().getValue();
                                String depttm = (CommUtil.isNotNull(entity
                                        .getDepttm()) ? entity.getDepttm()
                                        .getValue() : "");
                                String matudt = (CommUtil.isNotNull(entity
                                        .getMatudt()) ? entity.getMatudt() : "");
                                String bgindt = entity.getBgindt();
                                String brchno = entity.getBrchno();
                                String opendt = entity.getOpendt();
                                String opensq = entity.getOpensq();
                                String closdt = (CommUtil.isNotNull(entity
                                        .getClosdt()) ? entity.getClosdt() : "");
                                String clossq = (CommUtil.isNotNull(entity
                                        .getClossq()) ? entity.getClossq() : "");
                                String onlnbl = entity.getOnlnbl().toString();
                                String upbldt = (CommUtil.isNotNull(entity
                                        .getUpbldt()) ? entity.getUpbldt() : "");
                                String lastbl = entity.getLastbl().toString();
                                String lstrdt1 = entity.getLstrdt();
                                String lstrsq = (CommUtil.isNotNull(entity
                                        .getLstrsq()) ? entity.getLstrsq() : "");
                                String prodcd = entity.getProdcd();
                                String pddpfg = entity.getPddpfg().getValue();
                                String hdmxmy = (CommUtil.isNotNull(entity
                                        .getHdmxmy()) ? entity.getHdmxmy()
                                        .toString() : "");
                                String hdmimy = (CommUtil.isNotNull(entity
                                        .getHdmimy()) ? entity.getHdmimy()
                                        .toString() : "");
                                String custac = entity.getCustac();
                                String trsvtp = (CommUtil.isNotNull(entity
                                        .getTrsvtp()) ? entity.getTrsvtp()
                                        .getValue() : "");
                                String bkmony = (CommUtil.isNotNull(entity
                                        .getBkmony()) ? entity.getBkmony()
                                        .toString() : "");
                                String opmony = entity.getOpmony().toString();
                                String debttp = entity.getDebttp().getValue();
                                String acctst = entity.getAcctst().getValue();
                                String sleptg = entity.getSleptg().getValue();
                                String spectp = entity.getSpectp().getValue();
                                String accttp = entity.getAccttp().getValue();
                                String acctcd = entity.getAcctcd();
                                String corpno = (CommUtil.isNotNull(entity
                                        .getCorpno()) ? entity.getCorpno() : "");
                                String datetm = (CommUtil.isNotNull(entity
                                        .getDatetm()) ? entity.getDatetm() : "");
                                String tmstmp = (CommUtil.isNotNull(entity
                                        .getTmstmp()) ? entity.getTmstmp()
                                        .toString() : "");

                                // 拼接打印
                                fileInfo.append(acctno).append(delimt)
                                        .append(acctna);
                                fileInfo.append(delimt).append(custno)
                                        .append(delimt).append(crcycd)
                                        .append(delimt).append(csextg);
                                fileInfo.append(delimt).append(depttm)
                                        .append(delimt).append(matudt)
                                        .append(delimt).append(bgindt);
                                fileInfo.append(delimt).append(brchno)
                                        .append(delimt).append(opendt)
                                        .append(delimt).append(opensq);
                                fileInfo.append(delimt).append(closdt)
                                        .append(delimt).append(clossq)
                                        .append(delimt).append(onlnbl);
                                fileInfo.append(delimt).append(upbldt)
                                        .append(delimt).append(lastbl)
                                        .append(delimt).append(lstrdt1);
                                fileInfo.append(delimt).append(lstrsq)
                                        .append(delimt).append(prodcd)
                                        .append(delimt).append(pddpfg);
                                fileInfo.append(delimt).append(hdmxmy)
                                        .append(delimt).append(hdmimy)
                                        .append(delimt).append(custac);
                                fileInfo.append(delimt).append(trsvtp)
                                        .append(delimt).append(bkmony)
                                        .append(delimt).append(opmony);
                                fileInfo.append(delimt).append(debttp)
                                        .append(delimt).append(acctst)
                                        .append(delimt).append(sleptg);
                                fileInfo.append(delimt).append(spectp)
                                        .append(delimt).append(accttp)
                                        .append(delimt).append(acctcd);
                                fileInfo.append(delimt).append(corpno)
                                        .append(delimt).append(datetm)
                                        .append(delimt).append(tmstmp);
                                // 打印文件
                                file.write(fileInfo.toString());
                                return true;
                            }
                        });
            } finally {
                file.close();
            }
            bizlog.debug("定期负债子账户（导出数据）" + file + "文件产生完成");
        }
    }

    /**
     * 定期存款开户存入服务
     */
    @Override
    public DpOpenSbacPostOut prcOpenSbacPost(DpOpenSbacPostIn cplDpopin) {

        String custac = cplDpopin.getCustac(); // 电子账号
        String prodcd = cplDpopin.getProdcd(); // 产品编号
        String crcycd = cplDpopin.getCrcycd(); // 币种
        E_TERMCD depttm = cplDpopin.getDepttm(); // 存期
        BigDecimal tranam = cplDpopin.getTranam(); // 交易金额
        // kna_cust tblKna_cust = null; //电子账户信息
        IoCaKnaCust tblKna_cust = SysUtil.getInstance(IoCaKnaCust.class);
        // kna_accs tblKna_accs = SysUtil.getInstance(kna_accs.class);
        // //电子账户与子账户关联
        IoCaKnaAccs tblKna_accs = SysUtil.getInstance(IoCaKnaAccs.class);
        String custna = null; // 客户名称
        String custno = null; // 客户号
        String brchno = null; // 机构

        // 货币代号
        if (CommUtil.isNull(crcycd)) {
            throw CaError.Eacct.BNAS1690();
        }

        // 产品编号
        if (CommUtil.isNull(prodcd)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Prod.prodcd.getLongName());
        }

        // 存期
        if (CommUtil.isNull(depttm)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Prod.depttm.getLongName());
        }

        // 交易金额
        if (CommUtil.isNull(tranam)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.tranam.getLongName());
        }

        // 客户类型
        if (CommUtil.isNull(cplDpopin.getCusttp())) {
            throw DpAcError.DpDeptAcct.BNAS0535();
        }

        // 电子账号
        if (CommUtil.isNull(custac)) {
            throw DpModuleError.DpstAcct.BNAS0311();
        } else {
            // tblKna_cust = Kna_custDao.selectOne_odb1(custac, false);
            IoCaSevQryTableInfo caqry = SysUtil
                    .getInstance(IoCaSevQryTableInfo.class);
            tblKna_cust = caqry.getKnaCustByCustacOdb1(custac, false);

            if (CommUtil.isNull(tblKna_cust)) {
                throw DpAcError.DpDeptComm.BNAS0750();
            }
            if (E_ACCTST.NORMAL != tblKna_cust.getAcctst()) {
                throw DpAcError.DpDeptAcct.BNAS0905(tblKna_cust.getAcctst().getLongName());
            }

            custna = tblKna_cust.getCustna(); // 客户名称
            custno = tblKna_cust.getCustno(); // 客户号
            brchno = tblKna_cust.getBrchno(); // 机构
        }

        // 检查客户是否符合购买条件
        KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("FreeDefineDP_CK",
                prodcd, "%", "%", false);

        if (CommUtil.isNotNull(tblKnpParameter)
                && CommUtil.isNotNull(tblKnpParameter.getParm_value1())) {
//客户信息相关取消掉，模块拆分
//            IoSrvCfPerson.IoGetCifCust.InputSetter queryCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//            IoSrvCfPerson.IoGetCifCust.Output tblCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//            IoSrvCfPerson cifCustServ = SysUtil.getInstance(IoSrvCfPerson.class);
//            queryCifCust.setCustno(custno);
//            cifCustServ.getCifCust(queryCifCust, tblCifCust);

//            if (CommUtil.isNull(tblCifCust)) {
//                throw DpAcError.DpDeptAcct.BNAS0946(custac);
//            } else {
//                E_IDTFTP idtftp = tblCifCust.getIdtftp(); // 证件类型
//                String idtfno = tblCifCust.getIdtfno(); // 证件号码
//                if (CommUtil.isNotNull(idtfno)
//                        && (E_IDTFTP.LS == idtftp || E_IDTFTP.SFZ == idtftp)
//                        && CommUtil.equals(idtfno.substring(0, 2),
//                                tblKnpParameter.getParm_value1())) {
//
//                    throw DpAcError.DpDeptProd.BNAS1117(tblKnpParameter.getParm_value5());
//                }
//            }
        }

        // 定期账户开户
        DpOpenAcctEntity entity = SysUtil.getInstance(DpOpenAcctEntity.class);
        entity.setCustac(custac); // 电子账号
        entity.setCrcycd(crcycd); // 货币代号
        entity.setDepttm(depttm); // 存期
        entity.setProdcd(prodcd); // 产品编号
        entity.setAcctna(custna); // 客户名称
        entity.setCustno(custno); // 客户号
        entity.setCusttp(cplDpopin.getCusttp());// 客户类型
        entity.setCacttp(tblKna_cust.getCacttp()); // 客户账号类型
        entity.setOpacfg(E_YES___.NO); // 首开户标志
        entity.setTranam(tranam); // 存入金额
        DpPublicServ.openAcct(entity);
        bizlog.debug("新开存期为[" + depttm.getLongName() + "]的定期负债账号["
                + entity.getAcctno() + "]");

        // 关联电子账号
        IoCaSrvGenEAccountInfo caService = SysUtil
                .getInstance(IoCaSrvGenEAccountInfo.class);
        IoCaAddEARelaIn connectEntity = SysUtil
                .getInstance(IoCaAddEARelaIn.class);
        connectEntity.setAcctno(entity.getAcctno());
        connectEntity.setCrcycd(crcycd);
        connectEntity.setCustac(custac);
        connectEntity.setFcflag(E_FCFLAG.FIX);
        connectEntity.setProdcd(entity.getProdcd());
        connectEntity.setProdtp(E_PRODTP.DEPO);
        caService.prcAddEARela(connectEntity);

        // 支持零开户，定期账户存入
        if (CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {

            // 查询活期账户信息
            // tblKna_accs = CaTools.getAcctAccs(custac, crcycd);
            IoCaStaPublic casta = SysUtil.getInstance(IoCaStaPublic.class);
            tblKna_accs = casta.CaTools_getAcctAccs(custac, crcycd);

            DpSaveEntity input_save = SysUtil.getInstance(DpSaveEntity.class);
            input_save.setAcctno(entity.getAcctno());
            input_save.setCrcycd(crcycd);
            input_save.setCustac(custac);
            input_save.setOpacna(custna);
            input_save.setToacct(tblKna_accs.getAcctno());
            input_save.setTranam(tranam);
            input_save.setOpbrch(brchno);
            input_save.setAuacfg(E_YES___.NO);// 不是普通的智能储蓄存取
            bizlog.debug("[" + entity.getAcctno() + "]定期存入开始");
            DpPublicServ.postAcctDp(input_save);
            bizlog.debug("定期存期为[" + depttm.getLongName() + "]存入完成");
        }

        // 签约
        DpPublicServ.prcDpSign(entity.getAcctno(), cplDpopin.getSigntp(),
                custac, custno, prodcd, cplDpopin.getTrdptp(), null,
                E_YES___.YES);

        // 设置输出参数
        DpOpenSbacPostOut cplPostOut = SysUtil
                .getInstance(DpOpenSbacPostOut.class);
        cplPostOut.setAcctno(entity.getAcctno());
        cplPostOut.setCustna(custna);
        cplPostOut.setCustno(custno);
        cplPostOut.setToacct(tblKna_accs.getAcctno());
        cplPostOut.setTosbac(tblKna_accs.getSubsac());
        cplPostOut.setMatudt(KnaFxacDao.selectOne_odb1(entity.getAcctno(),
                true).getMatudt());

        return cplPostOut;
    }

    /**
     * 查询定存账户总金额与利息 参数描述：
     * 
     * @param custac
     *        电子账户
     * @param prodcd
     *        产品编号
     * @param crcycd
     *        货币编号
     */
    @Override
    public DpInsAll selInteByCustac(String custac, String prodcd,
            String crcycd) {

        IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);

        if (CommUtil.isNull(custac)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.custac.getLongName());
        }
        if (CommUtil.isNull(prodcd)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Prod.prodcd.getLongName());
        }
        if (CommUtil.isNull(crcycd)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.crcycd.getLongName());
        }
        
        /**
         * add by xj 20180918 修改支持卡号查询
         */
        KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(custac, false);
        if(CommUtil.isNotNull(tblKnaAcdc)){
        	custac = tblKnaAcdc.getCustac();
        }
        /**end*/
        
        DpInsAll dpInsAll = SysUtil.getInstance(DpInsAll.class);
        KupDppb dppb = KupDppbDao.selectOne_odb1(prodcd, true);

        // 判断定、活期，目前只算定期
        if (E_FCFLAG.FIX.equals(dppb.getPddpfg())) {

            Options<DpInstCal> dplist = new DefaultOptions<DpInstCal>();
            // 获取账户信息
            List<KnaFxac> list = DpAcctQryDao.selKnaFxacByCustacAndProdcd(
                    custac, prodcd, crcycd, false);

            // 总金额，初值为0
            BigDecimal totlam = BigDecimal.ZERO;
            // 总当前利息，初值为0
            BigDecimal tocuin = BigDecimal.ZERO;
            // 总到期利息，初值为0
            BigDecimal tomuin = BigDecimal.ZERO;

            for (KnaFxac fxac : list) {
                DpInstCal dpInstCal = SysUtil.getInstance(DpInstCal.class);
                // 定期子账号
                dpInstCal.setAcctno(fxac.getAcctno());
                // 交易金额
                dpInstCal.setTranam(fxac.getOnlnbl());

                // 总金额
                totlam = dpInstCal.getTranam().add(totlam);
                // 存入日期
                dpInstCal.setBgindt(fxac.getBgindt());
                // 到期利息
                dpInstCal.setMatudt(fxac.getMatudt());

                // 获取当前日期
                dpInstCal.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());

                // 获取当前利率
                KupDppbDfir tblDppbDfir = KupDppbDfirDao.selectOne_odb1(
                        fxac.getProdcd(), fxac.getCrcycd(), E_DRINTP.TQZQ,
                        "8888", E_INTRTP.ZHENGGLX, false);
                if (CommUtil.isNotNull(tblDppbDfir)) {
                    if (CommUtil.isNotNull(tblDppbDfir.getAdincd())) {

                        // 计算利息，使用行内基准的活期利率
                        // IntrPublicEntity intrEntity = new IntrPublicEntity();
                        IoPbIntrPublicEntity intrEntity = SysUtil
                                .getInstance(IoPbIntrPublicEntity.class);
                        intrEntity.setCrcycd(fxac.getCrcycd()); // 币种
                        intrEntity.setIntrcd(tblDppbDfir.getAdincd()); // 利率代码
                        // 如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                        if (tblDppbDfir.getInclfg() == E_YES___.YES) {
                            intrEntity.setIncdtp(E_IRCDTP.LAYER); // 利率代码类型
                            intrEntity.setIntrwy(E_IRDPWY.CURRENT); // 靠档方式
                        } else {
                            intrEntity.setIncdtp(E_IRCDTP.BASE); // 利率代码类型
                            intrEntity.setDepttm(E_TERMCD.T000); // 存期
                        }
                        intrEntity.setTrandt(dpInstCal.getTrandt());
                        intrEntity.setBgindt(fxac.getBgindt()); // 起始日期
                        intrEntity.setEdindt(dpInstCal.getTrandt()); // 结束日期

                        intrEntity.setLevety(tblDppbDfir.getLevety());
                        if (tblDppbDfir.getIntrdt() == E_INTRDT.OPEN) {
                            intrEntity.setTrandt(fxac.getOpendt());
                            intrEntity.setTrantm("999999");
                        }

                        pbpub.countInteresRate(intrEntity);

                        dpInstCal.setCuinrt(intrEntity.getIntrvl());
                    }
                }

                // 获取定期利率(当前执行利率)
                KubInrt tblcurrin = KubInrtDao.selectOne_odb1(
                        fxac.getAcctno(), false);
                if (CommUtil.isNull(tblcurrin)) {
                    throw DpAcError.DpDeptAcct.BNAS1065();
                }
                BigDecimal currin = tblcurrin.getCuusin();
                dpInstCal.setCuusin(currin);

                // 查询交易基础，计算利息时用
                KnbAcin tblacin = KnbAcinDao.selectOne_odb1(
                        dpInstCal.getAcctno(), true);
                E_INBEBS inbebs = tblacin.getTxbebs();

                // 当前利息，用的是实际天数
                BigDecimal bigNow = pbpub.countInteresRateByAmounts(currin,
                        dpInstCal.getBgindt(), dpInstCal.getTrandt(),
                        dpInstCal.getTranam(), inbebs);
                bigNow = BusiTools.roundByCurrency(crcycd, bigNow, null);
                dpInstCal.setCurrin(bigNow);

                // 总当前利息
                tocuin = dpInstCal.getCurrin().add(tocuin);

                // 到期利息，用的是实际天数
                BigDecimal bigF = pbpub.countInteresRateByAmounts(currin,
                        dpInstCal.getBgindt(), dpInstCal.getMatudt(),
                        dpInstCal.getTranam(), inbebs);
                bigF = BusiTools.roundByCurrency(crcycd, bigF, null);
                dpInstCal.setMatuin(bigF);

                // 总到期利息
                tomuin = dpInstCal.getMatuin().add(tomuin);

                // 存期
                dpInstCal.setDepttm(fxac.getDepttm());
                // 存期天数
                dpInstCal.setDeptdy(fxac.getDeptdy());

                KupDppbPost kdp = KupDppbPostDao.selectOne_odb1(prodcd,
                        crcycd, false);
                if (CommUtil.isNull(kdp)) {
                    throw DpAcError.DpDeptAcct.BNAS1078();
                }
                // 单人限购份数
                dpInstCal.setSvrule(kdp.getSvrule());
                // 最低限额
                dpInstCal.setMiniam(kdp.getMiniam());
                // 最高限额
                dpInstCal.setMaxiam(kdp.getMaxiam());

                KupDppb kd = KupDppbDao.selectOne_odb1(prodcd, false);
                if (CommUtil.isNull(kdp)) {
                    throw DpAcError.DpDeptAcct.BNAS1073();
                }
                // 发行份数
                dpInstCal.setPresal(kd.getPresal());

                dplist.add(dpInstCal);
            }
            dpInsAll.setDpInstCal(dplist);
            totlam = BusiTools.roundByCurrency(crcycd, totlam, null);
            dpInsAll.setTotlam(totlam);
            tocuin = BusiTools.roundByCurrency(crcycd, tocuin, null);
            dpInsAll.setTocuin(tocuin);
            tomuin = BusiTools.roundByCurrency(crcycd, tomuin, null);
            dpInsAll.setTomuin(tomuin);
        }

        return dpInsAll;
    }

    /**
     * 根据账户查询存款相关利率信息
     */
    @Override
    public DpIntrvlInfo selIntrvlByAcctno(String acctno) {

        if (CommUtil.isNull(acctno)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.acctno.getLongName());
        }

        IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);

        BigDecimal bigCuintr = BigDecimal.ZERO; // 当前利率/违约利率
        BigDecimal bigCuusin = BigDecimal.ZERO; // 账户利率

        String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); // 交易日期

        // kna_accs tblKna_accs = Kna_accsDao.selectOne_odb2(acctno, false);
        IoCaKnaAccs tblKna_accs = SysUtil.getInstance(IoCaKnaAccs.class);
        if (CommUtil.isNull(tblKna_accs)) {
            throw DpAcError.DpDeptAcct.BNAS0767();
        }

        // E_TERMCD depttm = null; //存期
        String bgindt = null; // 起息日期
        String matudt = null; // 到期日期
        String opendt = "";
        // 查询账户利率
        if (E_FCFLAG.CURRENT == tblKna_accs.getFcflag()) {
            // 活期查询活期账户及账户利率信息
            KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(acctno, true);
            KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acctno, true);
            // depttm = tblKnaAcct.getDepttm(); //存期
            bgindt = tblKnaAcct.getBgindt(); // 起息日期
            matudt = tblKnaAcct.getMatudt(); // 到期日期
            opendt = tblKnaAcct.getOpendt();
            bigCuusin = tblKubInrt.getCuusin(); // 账户利率
        } else if (E_FCFLAG.FIX == tblKna_accs.getFcflag()) {
            // 定期查询定期账户级账户利率信息
            KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(acctno, true);
            KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acctno, true);
            // depttm = tblKnaFxac.getDepttm(); //存期
            bgindt = tblKnaFxac.getBgindt(); // 起息日期
            matudt = tblKnaFxac.getMatudt(); // 到期日期
            opendt = tblKnaFxac.getOpendt();
            bigCuusin = tblKubInrt.getCuusin(); // 账户利率
        }

        KupDppbDfir tblDppbDfir = KupDppbDfirDao.selectOne_odb1(
                tblKna_accs.getProdcd(), tblKna_accs.getCrcycd(),
                E_DRINTP.TQZQ, "8888", E_INTRTP.ZHENGGLX, false);

        if (CommUtil.isNotNull(tblDppbDfir)) {
            if (CommUtil.isNotNull(tblDppbDfir.getAdincd())) {

                // 计算利息，使用行内基准的活期利率
                // IntrPublicEntity intrEntity = new IntrPublicEntity();
                IoPbIntrPublicEntity intrEntity = SysUtil
                        .getInstance(IoPbIntrPublicEntity.class);
                intrEntity.setCrcycd(tblKna_accs.getCrcycd()); // 币种
                intrEntity.setIntrcd(tblDppbDfir.getAdincd()); // 利率代码
                // 如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                if (tblDppbDfir.getInclfg() == E_YES___.YES) {
                    intrEntity.setIncdtp(E_IRCDTP.LAYER); // 利率代码类型
                    intrEntity.setIntrwy(E_IRDPWY.CURRENT); // 靠档方式
                } else {
                    intrEntity.setIncdtp(E_IRCDTP.BASE); // 利率代码类型
                    intrEntity.setDepttm(E_TERMCD.T000); // 存期
                }
                intrEntity.setTrandt(trandt);
                intrEntity.setBgindt(bgindt); // 起始日期
                intrEntity.setEdindt(trandt); // 结束日期

                intrEntity.setLevety(tblDppbDfir.getLevety());
                if (tblDppbDfir.getIntrdt() == E_INTRDT.OPEN) {
                    intrEntity.setTrandt(opendt);
                    intrEntity.setTrantm("999999");
                }
                pbpub.countInteresRate(intrEntity);

                bigCuintr = intrEntity.getIntrvl();
            }

        }

        DpIntrvlInfo cplIntr = SysUtil.getInstance(DpIntrvlInfo.class);
        cplIntr.setCuinrt(bigCuintr); // 当前利率
        cplIntr.setCuusin(bigCuusin); // 账户利率
        cplIntr.setTrandt(trandt); // 当前日期
        cplIntr.setBgindt(bgindt); // 起息日期
        cplIntr.setMatudt(matudt); // 到期日期

        return cplIntr;
    }

    /**
     * 使用销售工厂对接的接口开立负债子账号
     * 
     * */
    @Override
    public AddSubAcctOut OpenSubAcct(IoDpOpenSub openInfo) {
        // 基础属性非空检查
        IoDpBasePart open = openInfo.getBase();
        if (CommUtil.isNull(open.getProdcd())) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Prod.prodcd.getLongName());
        }

        if (CommUtil.isNull(open.getDepttm())) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Prod.depttm.getLongName());
        }

        if (CommUtil.isNull(open.getCrcycd())) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.crcycd.getLongName());
        }

        if (CommUtil.isNull(open.getCustac())) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.custac.getLongName());
        }

        if (CommUtil.isNull(open.getCustno())) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.custno.getLongName());
        }
        if (CommUtil.isNull(open.getCustna())) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.custna.getLongName());
        }
        if (CommUtil.isNull(open.getAcctcd())) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Prod.acctcd.getLongName());
        }
        if (CommUtil.isNull(open.getSprdid())) {
            throw DpAcError.DpDeptComm.BNAS0545();
        }
        if (CommUtil.isNull(open.getSprdvr())) {
            throw DpAcError.DpDeptComm.BNAS0981();
        }
        if (CommUtil.isNull(open.getSprdna())) {
            throw DpAcError.DpDeptComm.BNAS0544();
        }

        return OpenSubAcctDeal.OpenSub(openInfo);
    }

    /**
     * 
     * @Title: selHqTranDetl
     * @Description: 活期智能存款交易明细查询
     * @param input
     *        查询条件
     * @param output
     *        查询结果
     * @author zhangjunlei
     * @date 2016年7月7日 上午9:34:55
     * @version V2.3.0
     */
    @Override
    public void selHqTranDetl(
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.selHqTranDetl.Input input,
            Output output) {

        int totlCount = 0; // 记录总数
        int startno = (input.getPageno() - 1) * input.getPagesz();// 起始记录数
        String acctno = input.getAcctno();// 负债账号
        String trancy = input.getCrcycd();// 币种
        String cardno = input.getCardno();
        E_DETLTP detltp = input.getDetltp();

        // 负债账户
        if (CommUtil.isNull(acctno)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.acctno.getLongName());
        }
        // 交易币种
        if (CommUtil.isNull(trancy)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.crcycd.getLongName());
        }
        // 当前页码
        if (CommUtil.isNull(input.getPageno())) {
            throw CaError.Eacct.BNAS0977();
        }
        // 页容量
        if (CommUtil.isNull(input.getPagesz())) {
            throw CaError.Eacct.BNAS0463();
        }
        if (CommUtil.isNull(cardno)) {
            throw CaError.Eacct.BNAS0570();
        }

        if (CommUtil.isNull(detltp)) {
            throw CaError.Eacct.BNAS0459();
        }
        //		//判断查询明细类型
        //		if(detltp != E_DETLTP.NOW){
        //			throw CaError.Eacct.E0001("只支持当前持有查询");
        //		}
        // 根据卡号获取电子账号
        IoCaKnaAcdc cplKnaAcdc = DpAcctDao.selKnaAcdcByCardno(cardno, false);
        if (CommUtil.isNull(cplKnaAcdc)) {
            throw CaError.Eacct.BNAS0464(cardno);
        }

        // 根据负债子账号获取电子账号
        String custac = null;
        custac = DpAcctDao.selKnaAccsByAcctNo(acctno, false);
        if (CommUtil.isNull(custac)) {
            throw CaError.Eacct.BNAS0465(acctno);
        }

        if (!CommUtil.equals(cplKnaAcdc.getCustac(), custac)) {
            throw CaError.Eacct.BNAS0569();
        }

        // 获取活期负债账户信息
        KnaAcct tblKnaAcct = KnaAcctDao
                .selectOne_odb5(acctno, trancy, false);

        // 活期账户信息为空
        if (CommUtil.isNull(tblKnaAcct)) {
            throw DpAcError.DpDeptAcct.BNAS0668();
        }

        // 判断产品是否为活期
        if (E_FCFLAG.CURRENT != tblKnaAcct.getPddpfg()) {
            throw DpAcError.DpDeptAcct.BNAS0760();
        }

        // 查询活期智能存款明细
        Page<KnlBill> knlBills = KnlBillDao.selectPageWithCount_odb2(
                acctno, trancy, startno, input.getPagesz(), totlCount, false);

        // 获取查询信息
        List<KnlBill> KnlBill = knlBills.getRecords();

        // 把查询信息循环赋值给复合类型
        for (KnlBill KnlBills : KnlBill) {
            IoDpTranDetlHqQry info = SysUtil
                    .getInstance(IoDpTranDetlHqQry.class);
            info.setTrandt(KnlBills.getTrandt());// 交易日期
            info.setTrantm(KnlBills.getTrantm());// 交易时间
            info.setTranam(KnlBills.getTranam());// 交易金额
            info.setAcctbl(KnlBills.getAcctbl());// 账户余额
            info.setAcctno(KnlBills.getAcctno());// 负债账号
            info.setTrancy(KnlBills.getTrancy());// 交易币种
            info.setTrantp(KnlBills.getAmntcd());//交易类型

            output.getHqtrdt().add(info);

            bizlog.debug("<<<<<<<<<<<<<<<<<<活期智能存款交易明细>>>>>>>>>>>>>>>>>>>：output="
                    + output);
        }

        // 设置总记录数
        output.setCounts(ConvertUtil.toInteger(knlBills.getRecordCount()));
        // 设置报文头总记录条数
        CommTools.getBaseRunEnvs().setTotal_count(
                knlBills.getRecordCount());
    }

    /**
     * 
     * @Title: selDqTranDetl
     * @Description: 定期智能存款交易明细查询
     * @param input
     *        查询条件
     * @param output
     *        查询结果
     * @author zhangjunlei
     * @date 2016年7月7日 上午9:34:55
     * @version V2.3.0
     */
    @Override
    public void selDqTranDetl(
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.selDqTranDetl.Input input,
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.selDqTranDetl.Output output) {

        int totlCount = 0; // 记录总数
        int startno = (input.getPageno() - 1) * input.getPagesz();// 起始记录数
        String acctno = input.getAcctno();// 负债账号
        String crcycd = input.getCrcycd();// 币种
        String cardno = input.getCardno();
        E_DETLTP detltp = input.getDetltp();

        // 负债账户
        if (CommUtil.isNull(acctno)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.acctno.getLongName());
        }
        // 币种
        if (CommUtil.isNull(crcycd)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.crcycd.getLongName());
        }
        // 页码
        if (CommUtil.isNull(input.getPageno())) {
            throw CaError.Eacct.BNAS0977();
        }
        // 页容量
        if (CommUtil.isNull(input.getPagesz())) {
            throw CaError.Eacct.BNAS0463();
        }
        if (CommUtil.isNull(cardno)) {
            throw CaError.Eacct.BNAS0570();
        }

        if (CommUtil.isNull(detltp)) {
            throw CaError.Eacct.BNAS0459();
        }

        //		//判断查询明细类型
        //		if(detltp != E_DETLTP.NOW){
        //			throw CaError.Eacct.E0001("只支持当前持有查询");
        //		}

        // 根据卡号获取电子账号
        IoCaKnaAcdc cplKnaAcdc = DpAcctDao.selKnaAcdcByCardno(cardno, false);
        if (CommUtil.isNull(cplKnaAcdc)) {
            throw CaError.Eacct.BNAS0464(cardno);
        }

        // 根据负债子账号获取电子账号
        String custac = null;
        custac = DpAcctDao.selKnaAccsByAcctNo(acctno, false);
        if (CommUtil.isNull(custac)) {
            throw CaError.Eacct.BNAS0465(acctno);
        }

        if (!CommUtil.equals(cplKnaAcdc.getCustac(), custac)) {
            throw CaError.Eacct.BNAS0569();
        }

        // 查询定期存款信息
        KnaFxac KnaFxac = KnaFxacDao.selectOne_odb1(acctno, false);

        // 判断定期存款信息是否存在
        if (CommUtil.isNull(KnaFxac)) {
            throw DpAcError.DpDeptAcct.BNAS0842();
        }

        //查询定期存款交易明细
        Page<KnlBill> cplKnlbill = KnlBillDao.selectPageWithCount_odb2(acctno, crcycd, startno, input.getPagesz(), totlCount, false);

        //获取查询信息
        List<KnlBill> knlBill = cplKnlbill.getRecords();

        for (KnlBill knlBillInfos : knlBill) {
            IoDpTranDetlDqQry info = SysUtil.getInstance(IoDpTranDetlDqQry.class);

            BigDecimal intrvl = BigDecimal.ZERO;//结息总额
            BigDecimal rlintr = BigDecimal.ZERO;//每条记录结息

            info.setTrandt(knlBillInfos.getTrandt());// 交易日期
            info.setTmstmp(knlBillInfos.getTmstmp());// 交易时间
            info.setTranam(knlBillInfos.getTranam());// 交易金额

            //获取结算利息
            List<KnbPidl> tblKnbPidls = DpAcctDao.selKnbPidlByPyinsq(knlBillInfos.getAcctno(), knlBillInfos.getTransq(), false);
            if (tblKnbPidls.size() > 0) {
                for (KnbPidl tblKnbPidl : tblKnbPidls) {
                    rlintr = tblKnbPidl.getRlintr();
                    intrvl = intrvl.add(rlintr);
                }
            }
            info.setIntrvl(intrvl);// 支取金额利息	
            info.setAcctno(knlBillInfos.getAcctno());// 负债账号
            info.setCrcycd(knlBillInfos.getTrancy());// 币种
            info.setTrantp(knlBillInfos.getAmntcd());//交易类型

            output.getDqtrdt().add(info);

            bizlog.debug("<<<<<<<<<<<<<<<<<<定期智能存款交易明细>>>>>>>>>>>>>>>>>>>：output="
                    + output);
        }
        // 设置总记录数
        output.setCounts(ConvertUtil.toInteger(cplKnlbill.getRecordCount()));

        // 设置报文头总记录条数
        CommTools.getBaseRunEnvs().setTotal_count(
                cplKnlbill.getRecordCount());
    }

    /**
     * 
     * @Title: qryEacctSelpay
     * @Description: 查询电子账号支付方式
     * @param input
     * @param output
     * @author huangzhikai
     * @date 2016年7月7日 上午11:29:58
     * @version V2.3.0
     */
    @Override
    public void qryEacctSelpay(
            Input input,
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.QryEacctSelpay.Output output) {
        // 电子账号ID
        String custid = input.getCustid();// 用户ID

        //mod 20161130 songkl 需求修改，原传入custac现改为custid
        if (CommUtil.isNull(custid)) {
            throw CaError.Eacct.BNAS0241();
        }

        // 获取客户关联关系信息
        IoCifCustAccs tblCifCustAccs = DpAcctDao.selCifCustAccsByCustid(custid, false);

        if (CommUtil.isNull(tblCifCustAccs)) {
            //throw DpAcError.DpDeptAcct.E9999("根据用户ID查询用户信息失败，请检查用户ID！");
            return;
        }

        String custac = tblCifCustAccs.getCustac();// 电子账号
        String custno = tblCifCustAccs.getCustno();// 客户号

        // 获取结果实例
        List<IoEacctSelpayInfo> paylist = new ArrayList<>();

        // 获取亲情钱包服务
        IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);

//        IoWaSrvWalletAccountType waqry = SysUtil.getInstance(IoWaSrvWalletAccountType.class);

        // 卡号
        String cardno = "";
        // 结算账户
        KnaAcct acct1 = SysUtil.getInstance(KnaAcct.class);
        // 钱包账户
        KnaAcct acct2 = SysUtil.getInstance(KnaAcct.class);

        // 有开立电子账户
        if (CommUtil.isNotNull(custac)) {

            IoCaKnaAcdc knaAcdc = caqry.getKnaAcdcOdb1(custac, E_DPACST.NORMAL, false);
            if (CommUtil.isNotNull(knaAcdc)) {
                cardno = knaAcdc.getCardno();
            }

            //查询电子账户状态
            E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);

            // 账户分类
            E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac);
            if (eAccatp == E_ACCATP.WALLET) {
                // 查询钱包账户信息
                acct2 = CapitalTransDeal.getSettKnaAcctSub(custac, E_ACSETP.MA);
            } else {
                // 查询结算账户信息
                acct1 = CapitalTransDeal.getSettKnaAcctSub(custac, E_ACSETP.SA);
                if (cuacst != E_CUACST.PRECLOS) {
                    // 查询钱包账户信息
                    acct2 = CapitalTransDeal.getSettKnaAcctSub(custac, E_ACSETP.MA);
                }
            }
        }

        // 查询亲情钱包账户信息
//        List<IoQryWactInfo> wadetail = waqry.qryWactInfo(custno);
        // 查询绑定卡(外部卡)信息，存在绑定多张卡
        List<IoEacctSelpayInfo> resultInfo = DpAcctQryDao.selKnaCacdInfo(custac, false);

        //结算账户
        if (CommUtil.isNotNull(acct1) && CommUtil.isNotNull(acct1.getAcctno())) {
            BigDecimal usebal = SysUtil.getInstance(DpAcctSvcType.class)
                    .getAcctaAvaBal(custac, acct1.getAcctno(),
                            acct1.getCrcycd(), E_YES___.YES, E_YES___.NO);

            IoEacctSelpayInfo ioInfo1 = SysUtil.getInstance(IoEacctSelpayInfo.class);

            ioInfo1.setAcsetp(E_ACSETP.SA.getValue()); // 结算户
            ioInfo1.setBrchna(null);
            ioInfo1.setBrchno(null);
            ioInfo1.setIsbkca(null);
            ioInfo1.setCardtp(null);
            ioInfo1.setAcctno(acct1.getAcctno());
            ioInfo1.setAcctna(acct1.getAcctna());
            ioInfo1.setStatus(acct1.getAcctst());
            ioInfo1.setCardno(cardno);
            ioInfo1.setAcoutp(E_ACOUTP.ACC.getValue());
            ioInfo1.setAcount(custac);
            ioInfo1.setUsebal(usebal);
            paylist.add(ioInfo1);
        }
        //钱包账户
        if (CommUtil.isNotNull(acct2) && CommUtil.isNotNull(acct1.getAcctno())) {
            IoEacctSelpayInfo ioInfo2 = SysUtil
                    .getInstance(IoEacctSelpayInfo.class);

            BigDecimal usebal = SysUtil.getInstance(DpAcctSvcType.class)
                    .getAcctaAvaBal(custac, acct2.getAcctno(),
                            acct2.getCrcycd(), E_YES___.YES, E_YES___.NO);

            ioInfo2.setAcsetp(E_ACSETP.MA.getValue()); // 钱包账户
            ioInfo2.setBrchna(null);
            ioInfo2.setBrchno(null);
            ioInfo2.setIsbkca(null);
            ioInfo2.setCardtp(null);
            ioInfo2.setAcctno(acct2.getAcctno());
            ioInfo2.setAcctna(acct2.getAcctna());
            ioInfo2.setStatus(acct2.getAcctst());
            ioInfo2.setCardno(cardno);
            ioInfo2.setAcount(custac);
            ioInfo2.setAcoutp(E_ACOUTP.ACC.getValue());
            ioInfo2.setUsebal(usebal);
            paylist.add(ioInfo2);
        }
        // 亲情钱包账户  取消亲情模块内容
//        if (CommUtil.isNotNull(wadetail)) {
//
//            for (IoQryWactInfo wactInfo : wadetail) {
//
//                IoEacctSelpayInfo ioInfo3 = SysUtil.getInstance(IoEacctSelpayInfo.class);
//
//                BigDecimal usebal = SysUtil.getInstance(DpAcctSvcType.class)
//                        .getAcctaAvaBal(wactInfo.getCustac(), wactInfo.getAcctno(),
//                                wactInfo.getCrcycd(), E_YES___.YES, E_YES___.NO);
//
//                ioInfo3.setAcctno(wactInfo.getAcctno());
//                ioInfo3.setAcctna(wactInfo.getAcctna());
//                ioInfo3.setUsebal(usebal);
//                ioInfo3.setStatus(wactInfo.getAcctst());
//                ioInfo3.setBrchna(null);
//                ioInfo3.setBrchno(null);
//                ioInfo3.setIsbkca(null);
//                ioInfo3.setCardtp(null);
//                ioInfo3.setAcount(wactInfo.getCustac());
//                ioInfo3.setAcsetp(E_ACSETP.FW.getValue());
//                ioInfo3.setCardno(wactInfo.getCardno());
//                ioInfo3.setAcoutp(E_ACOUTP.ACC.getValue());
//                paylist.add(ioInfo3);
//            }
//
//        }
        //外部绑定卡信息
        if (CommUtil.isNotNull(resultInfo)) {
            for (IoEacctSelpayInfo cardInfo : resultInfo) {
                IoEacctSelpayInfo ioInfo3 = SysUtil.getInstance(IoEacctSelpayInfo.class);

                ioInfo3.setAcctno(null);
                ioInfo3.setAcctna(null);
                ioInfo3.setUsebal(null);
                ioInfo3.setStatus(null);
                ioInfo3.setBrchna(cardInfo.getBrchna());
                ioInfo3.setBrchno(cardInfo.getBrchno());
                ioInfo3.setIsbkca(cardInfo.getIsbkca());
                ioInfo3.setCardtp(cardInfo.getCardtp());
                ioInfo3.setAcount(cardInfo.getCardno()); //绑定卡卡号
                ioInfo3.setAcsetp(null);
                ioInfo3.setCardno(cardno);
                ioInfo3.setAcoutp(E_ACOUTP.CRD.getValue());
                paylist.add(ioInfo3);
            }
        }

        output.getPaylist().addAll(paylist);

    }

    /**
     * 
     * @Title: selDpDpacbl
     * @Description: 查询电子账户存款类账户交易明细(柜面)
     * @param input
     *        查询条件
     * @param output
     *        查询结果
     * @author zhangjunlei
     * @date 2016年7月7日 上午10:02:01
     * @version V2.3.0
     */
    @Override
    public void selDpDpacbl(
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.selDpDpacbl.Input input,
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.selDpDpacbl.Output output) {

        // 获取输入接口变量
        int totlCount = 0; // 记录总数
        int startno = (input.getPageno() - 1) * input.getPagesz();// 起始记录数
        String acctno = input.getAcctno();// 负债账号
        E_PDTPDL dptptl = input.getDptptl();// 产品类型细分
        E_SETYPE qractp = input.getQractp();// 查询类型
        E_AMNTCD amntcd = input.getAmntcd();// 借贷标志
        String bgindt = input.getBgindt();// 起始日期
        String endddt = input.getEndddt();// 终止日期
        BigDecimal mntram = input.getMntram();// 最小交易金额
        BigDecimal mxtram = input.getMxtram();// 最大交易金额
        String smrycd = input.getSmrycd();// 摘要
        String opcuac = input.getOpcuac();// 对方账号
        String opcuna = input.getOpcuna();// 对方户名
        String sTime = CommTools.getBaseRunEnvs().getTrxn_date();// 当前日期

        // 负债账号
        if (CommUtil.isNull(acctno)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.acctno.getLongName());
        }

        // 产品类型细分
        if (CommUtil.isNull(dptptl)) {
            throw DpAcError.DpDeptAcct.BNAS1050();
        }

        // 查询类型
        if (CommUtil.isNull(qractp)) {
            throw CaError.Eacct.BNAS1272();
        }

        // 开日日期
        if (CommUtil.isNull(bgindt)) {
            throw DpAcError.DpDeptAcct.BNAS0554();
        }

        // 终止日期
        if (CommUtil.isNull(endddt)) {
            throw CaError.Eacct.BNAS0061();
        }

        // 判断起始日期是否大于终止日期
        if (bgindt.compareTo(endddt) > 0) {
            throw CaError.Eacct.BNAS1688();
        }

        // 起始日期不能大于当前日期
        if (DateUtil.compareDate(bgindt, sTime) > 0) {
            throw DpAcError.DpDeptAcct.BNAS0413();
        }

        // 截止日期不能大于当前日期
        if (DateUtil.compareDate(endddt, sTime) > 0) {
            throw DpAcError.DpDeptComm.BNAS0592();
        }

        if (CommUtil.isNotNull(mntram)) {

            if (CommUtil.compare(mntram, BigDecimal.ZERO) < 0) {
                throw DpAcError.DpDeptAcct.BNAS0003();
            }
        }

        if (CommUtil.isNotNull(mxtram)) {
            if (CommUtil.compare(mxtram, BigDecimal.ZERO) < 0) {
                throw DpAcError.DpDeptComm.BNAS0008();
            }
        }

        if (CommUtil.isNotNull(mntram) && CommUtil.isNotNull(mxtram)) {

            if (CommUtil.compare(mxtram, mntram) < 0) {
                throw DpAcError.DpDeptComm.BNAS0007();
            }
        }

        // 当前页数
        if (CommUtil.isNull(input.getPageno())) {
            throw CaError.Eacct.BNAS0977();
        }

        // 页容量
        if (CommUtil.isNull(input.getPagesz())) {
            throw CaError.Eacct.BNAS0463();
        }

        /*		// 查询负债账户活期表
        		KnaAcct KnaAcct = KnaAcctDao.selectOne_odb1(acctno, false);

        		// 查询定期负债账户信息表
        		KnaFxac KnaFxac = KnaFxacDao.selectOne_odb1(acctno, false);

        		// 判断负债账户信息是否存在
        		if (CommUtil.isNull(KnaAcct) && CommUtil.isNull(KnaFxac)) {
        			throw DpAcError.DpDeptAcct.E9999("负债账号信息不存在");
        		}*/

        //add by songkl 20170109 现需求要求同时显示升级前账户的交易明细

        KnaAcct tblknact = SysUtil.getInstance(KnaAcct.class);
        //根据负债账号查询电子账号
        if (dptptl == E_PDTPDL.CUSEACCT) {
            //查询去除法人
            //tblknact = KnaAcctDao.selectOne_odb1(acctno, false);
            tblknact = DpAcctDao.selknaacctbyacctno(acctno, false);

        }

        List<KnaAcct> tblaccts = null;
        List<String> acctnos = new ArrayList<String>();

        if (CommUtil.isNotNull(tblknact) && CommUtil.isNotNull(tblknact.getAcctno())) {

            //判断输入的电子账号是否为结算户子账号
            if (E_ACSETP.SA == tblknact.getAcsetp()) {

                //根据电子账号和子账户类型查询结算户负债账号
                //				tblaccts = KnaAcctDao.selectAll_odb9(E_ACSETP.SA, tblknact.getCustac(), true);
                tblaccts = DpAcctDao.selknaacctbycustac(tblknact.getCustac(), true);

                if (CommUtil.isNotNull(tblaccts)) {
                    for (KnaAcct acct : tblaccts) {
                        acctnos.add(acct.getAcctno());
                    }
                }
            }
        }
        bizlog.debug("***********************tblaccts" + tblaccts + "********");
        bizlog.debug("acctnos*******" + acctnos + "***********S");
        // 声明复合类型
        List<IoDpAcctblList> info = new ArrayList<IoDpAcctblList>();
        Page<IoDpAcctblList> infos = null;

        if (qractp == E_SETYPE.NOW) {

            if (CommUtil.isNull(tblaccts)) {
                // 查询当前库 交易明细
                infos = DpAcctDao.selAcctblList(acctno,
                        amntcd, bgindt, endddt, smrycd, opcuac, opcuna, mxtram,
                        mntram, startno, input.getPagesz(), totlCount, false);
            } else {
                infos = DpAcctDao.selAcctblListAll(acctnos.toString().substring(1, acctnos.toString().length() - 1),
                        amntcd, bgindt, endddt, smrycd, opcuac, opcuna, mxtram,
                        mntram, startno, input.getPagesz(), totlCount, false);
            }

            // 获取查询信息
            info = infos.getRecords();

            // 设置总记录数
            output.setCounts(ConvertUtil.toInteger(infos.getRecordCount()));
            // 设置报文头总记录条数
            CommTools.getBaseRunEnvs().setTotal_count(
                   infos.getRecordCount());

        } else if (qractp == E_SETYPE.HIS) {

            throw DpAcError.DpDeptAcct.BNAS0205();

            /*	// 查询历史库
            	Page<IoDpAcctblList> infos = DpAcctDao.selHisAcctblList(acctno,
            			amntcd, bgindt, endddt, smrycd, opcuac, opcuna, mxtram,
            			mntram, startno, input.getPagesz(), totlCount, false);

            	// 胡群殴查询信息
            	info = infos.getRecords();

            	// 设置总记录数
            	output.setCounts(ConvertUtil.toInteger(infos.getRecordCount()));
            	// 设置报文头总记录条数
            	CommTools.getBaseRunEnvs().setTotal_count(
            			ConvertUtil.toInteger(infos.getRecordCount()));
            */
        } else {
            throw DpAcError.DpDeptAcct.BNAS1271();
        }

        output.getAcctblList().addAll(info);// 把复合类型中的数据赋值给output
    }

    /**
     * 利息调整
     * 
     * @return
     * 
     */
    @Override
    public void adjustInterest(AdjustInterest_IN input) {

        E_ADJTTP adjttp = input.getAdjttp();

        if (CommUtil.isNull(adjttp)) {

            throw ItError.intr.BNASL107();
        }

        if (adjttp == E_ADJTTP.ALL) {

            throw ItError.intr.BNASL108();
        }

        if (CommUtil.compare(input.getIrstjt(), BigDecimal.ZERO) <= 0) {

            throw ItError.intr.BNASL133();
        }

        if (CommUtil.isNull(input.getRemark())) {

            throw ItError.intr.BNASL134();
        }
        if (CommUtil.isNull(input.getCrcycd())) {

            throw ItError.intr.BNASL003();
        }
        if (CommUtil.isNull(input.getCustac())) {

            throw ItError.intr.BNASL022();
        }

        if (CommUtil.isNull(input.getAcctna())) {

            throw ItError.intr.BNASL065();
        }
        if (CommUtil.isNull(input.getAdacna())) {

            throw ItError.intr.BNASL137();
        }
        if (CommUtil.isNull(input.getAdacno())) {

            throw ItError.intr.BNASL136();
        }

        //通过调入账号获取其custac进行冻结检查，代码在后面 change by chenjk
        //		IoDpAcStatusWord cplGetAcStWord = SysUtil.getInstance(
        //				IoDpFrozSvcType.class).getAcStatusWord(input.getCustac());
        //		if (cplGetAcStWord.getAlstop() == E_YES___.YES || cplGetAcStWord.getDbfroz() == E_YES___.YES||
        //				cplGetAcStWord.getBrfroz() == E_YES___.YES){
        //			throw DpAcError.DpDeptAcct.E9999("交易失败，已被冻结！");		
        //		}

        //账户可用余额  TODO 卡号校验
        //		if (input.getAdacno().length() != 19
        //				|| !CommUtil
        //						.equals(input.getAdacno().substring(0, 6), "623540")) {
        //
        //			throw DpAcError.DpDeptAcct.E9999("调息账号非电子账户，请检查！");
        //		}
        //TODO 电子账号校验未定
        //		if (input.getCustac().length() != 19
        //				|| !CommUtil
        //						.equals(input.getCustac().substring(0, 6), "623540")) {
        //
        //			throw DpAcError.DpDeptAcct.E9999("电子账号输入非电子账户，请检查！");
        //		}

        // 调息电子账号信息
        IoCaKnaAcdc tblKnaAcdc1 = SysUtil.getInstance(
                IoCaSevQryTableInfo.class).getKnaAcdcOdb2(
                	input.getCustac(), true);
        IoCaKnaCust tblKnaCust1 = SysUtil.getInstance(
                IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(
                tblKnaAcdc1.getCustac(), true);

        if (!CommUtil.equals(input.getAcctna(), tblKnaCust1.getCustna())) {

            throw ItError.intr.BNASL024();
        }

        if (!CommUtil.equals(tblKnaCust1.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch())) {
            throw ItError.intr.BNASL026();
        }
        // 调入电子账号信息
        IoCaKnaAcdc tblKnaAcdc2 = SysUtil.getInstance(
                IoCaSevQryTableInfo.class).getKnaAcdcOdb2(
                input.getAdacno(), true);
        IoCaKnaCust tblKnaCust2 = SysUtil.getInstance(
                IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(
                tblKnaAcdc2.getCustac(), true);

        if (!CommUtil.equals(input.getAdacna(), tblKnaCust2.getCustna())) {

            throw ItError.intr.BNASL138();
        }

        //冻结检查借贷标识赋值 add by chenjk
        E_AMNTCD amntcd = E_AMNTCD.CR;
        if (adjttp == E_ADJTTP.DEL) {
            amntcd = E_AMNTCD.DR;
        }
        //调入账号冻结检查 add by chenjk
        SysUtil.getInstance(IoAccountSvcType.class).checkStatusBeforeAccount(tblKnaAcdc2.getCustac(), amntcd, null, null);

        KnaAcct acct = CapitalTransDeal.getSettKnaAcctAc(tblKnaAcdc2.getCustac());
        if (acct == null || CommUtil.isNull(acct.getAcctno())) {
            throw ItError.intr.BNASL135();
        }

        if (!CommUtil.equals(input.getCrcycd(), acct.getCrcycd())) {

            throw ItError.intr.BNASL084();
        }

        if (!CommUtil.equals(tblKnaCust1.getCustno(), tblKnaCust2.getCustno())) {

            throw ItError.intr.BNASL023();
        }

        // 客户帐记账
        DpAcctSvcType DPAccountingAPI = SysUtil.getInstance(DpAcctSvcType.class);
        if (CommUtil.isNull(input.getAcctno())) {
            throw ItError.intr.BNASL172();
        }
        // 查询调息负债账户信息
        QryDpAcctOut acctInfo = SysUtil.getInstance(DpProdSvcType.class)
                .QryDpByAcctno(input.getAcctno());
        //modify by wuzx 20170116 利息调整修改电子账户状态控制  beg
        if (E_ADJTTP.ADD == adjttp) {
            E_CUACST status = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(tblKnaAcdc2.getCustac());//查询电子账户状态信息
            if (status == E_CUACST.CLOSED || status == E_CUACST.DELETE || status == E_CUACST.OUTAGE) {
                throw ItError.intr.BNASL064();
            }
            //modify by wuzx 20170116 利息调整修改电子账户状态控制 end
            // 调增

            /*			D:应付 
            			C：客户 */
            SaveDpAcctIn cplSaveAcctIn = SysUtil
                    .getInstance(SaveDpAcctIn.class);
            cplSaveAcctIn.setCustac(tblKnaAcdc2.getCustac()); // 电子账号
            cplSaveAcctIn.setAcctno(acct.getAcctno()); // 结算户
            cplSaveAcctIn.setCardno(input.getAdacno()); // 卡号
            cplSaveAcctIn.setCrcycd(input.getCrcycd()); // 币种
            cplSaveAcctIn.setTranam(input.getIrstjt()); // 交易金额
        //  cplSaveAcctIn.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
            cplSaveAcctIn.setSmrycd(BusinessConstants.SUMMARY_FX);
            cplSaveAcctIn.setLinkno(""); // 逐笔标志
            cplSaveAcctIn.setOpacna(""); // 对方户名
            cplSaveAcctIn.setToacct(""); // 对方账号
            DPAccountingAPI.addPostAcctDp(cplSaveAcctIn); // 客户帐存入记帐处理

            // 登记会计流水
            IoAccounttingIntf cplIoAccounttingIntf = SysUtil
                    .getInstance(IoAccounttingIntf.class);
            cplIoAccounttingIntf.setProdcd(acctInfo.getProdcd());// 产品
            cplIoAccounttingIntf.setDtitcd(acctInfo.getAcctcd());
            cplIoAccounttingIntf.setCrcycd(acctInfo.getCrcycd());
            cplIoAccounttingIntf.setTranam(input.getIrstjt());
            cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
            cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
            cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
            cplIoAccounttingIntf.setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch());
            cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR);
            cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
            cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
            cplIoAccounttingIntf.setBltype(E_BLTYPE.PYIN);
            cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
            // 登记交易信息，供总账解析
            if (CommUtil.equals(
                    "1",
                    KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
                            true).getParm_value1())) {
                KnpParameter para = SysUtil.getInstance(KnpParameter.class);
                para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%",
                        "%", true);
                cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息 结息
            }
            SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                    cplIoAccounttingIntf);
        } else {
            E_CUACST status = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(tblKnaAcdc2.getCustac());//查询电子账户状态信息
            if (status == E_CUACST.OUTAGE || status == E_CUACST.CLOSED || status == E_CUACST.DELETE || status == E_CUACST.NOENABLE) {
                throw ItError.intr.BNASL064();
            }
            //调减
            /*			D:应付 红字
            			C：客户 红字*/

            SaveDpAcctIn cplSaveAcctIn = SysUtil
                    .getInstance(SaveDpAcctIn.class);
            cplSaveAcctIn.setCustac(tblKnaAcdc2.getCustac()); // 电子账号
            cplSaveAcctIn.setAcctno(acct.getAcctno()); // 结算户
            cplSaveAcctIn.setCardno(input.getAdacno()); // 卡号
            cplSaveAcctIn.setCrcycd(input.getCrcycd()); // 币种
            cplSaveAcctIn.setTranam(input.getIrstjt().negate()); // 交易金额,红字
            cplSaveAcctIn.setNegafg(E_YES___.YES);
            cplSaveAcctIn.setSmrycd(BusinessConstants.SUMMARY_FX);
            DPAccountingAPI.addPostAcctDp(cplSaveAcctIn); // 客户帐存入记帐处理			

            // 登记会计流水
            IoAccounttingIntf cplIoAccounttingIntf = SysUtil
                    .getInstance(IoAccounttingIntf.class);
            cplIoAccounttingIntf.setProdcd(acctInfo.getProdcd());
            cplIoAccounttingIntf.setDtitcd(acctInfo.getAcctcd());
            cplIoAccounttingIntf.setCrcycd(acctInfo.getCrcycd());
            cplIoAccounttingIntf.setTranam(input.getIrstjt().negate()); // 结息-红字调增应付利息
            cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
            cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
            cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
            cplIoAccounttingIntf.setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch());
            cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR);
            cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
            cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
            cplIoAccounttingIntf.setBltype(E_BLTYPE.PYIN);
            cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
            // 登记交易信息，供总账解析
            if (CommUtil.equals(
                    "1",
                    KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
                            true).getParm_value1())) {
                KnpParameter para = SysUtil.getInstance(KnpParameter.class);
                para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%",
                        "%", true);
                cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息 结息
            }
            SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                    cplIoAccounttingIntf);
        }

        //登记利息调整登记簿
        KnbInrtAdjt tbadjt = SysUtil.getInstance(KnbInrtAdjt.class);
        CommUtil.copyProperties(tbadjt, input);
        tbadjt.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
        tbadjt.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
        tbadjt.setAcctbr(acctInfo.getBrchno());
        tbadjt.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());
        tbadjt.setAuthus(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus());
        tbadjt.setStatus(E_STATUS.ZC);
        tbadjt.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq());//增加柜员流水 
        KnbInrtAdjtDao.insert(tbadjt);
        E_CLACTP clactp = null;

        //平衡检查
        if (!CommUtil.equals(tblKnaCust1.getBrchno(), tblKnaCust2.getBrchno())) {
            clactp = E_CLACTP._10;
        }
        IoCheckBalance ioCheckBalanceSrv = SysUtil
                .getInstance(IoCheckBalance.class);
        ioCheckBalanceSrv.checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(),
                CommTools.getBaseRunEnvs().getMain_trxn_seq(), clactp);

        //冲正注册		
        IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
        cplInput.setTranam(input.getIrstjt());
        cplInput.setTranac(tblKnaAcdc2.getCustac());
        cplInput.setEvent1(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期
        cplInput.setEvent2(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //交易流水
        cplInput.setCrcycd(input.getCrcycd());
        cplInput.setBgindt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
        cplInput.setTranev(ApUtil.TRANS_EVENT_IRADJT);

        //ApStrike.regBook(cplInput);
		IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);    		
		apinput.setReversal_event_id(cplInput.getTranev());
		apinput.setInformation_value(SysUtil.serialize(cplInput));
		MsEvent.register(apinput, true);


        /*		//机构、柜员额度验证
        		IoBrchUserQt ioBrchUserQt = SysUtil.getInstance(IoBrchUserQt.class);
        		ioBrchUserQt.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
        		ioBrchUserQt.setBusitp(E_BUSITP.TR);
        		ioBrchUserQt.setCrcycd(input.getCrcycd());
        		ioBrchUserQt.setTranam(input.getIrstjt());
        		ioBrchUserQt.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());
        		SysUtil.getInstance(IoSrvPbBranch.class).selBrchUserQt(ioBrchUserQt);*/

    }

    /**
     * 利息调整登记簿查询
     */
    @Override
    public Options<IoDpKnbInrtAdjt> selAdjustInterestRgst(String cardno,
            E_ADJTTP adjttp, String bgdate, String endate) {

    	 if (CommUtil.isNull(adjttp)) {

             throw ItError.intr.BNASL139();
         }

         if (CommUtil.isNull(bgdate)) {

             throw ItError.intr.BNASL119();
         }
         if (CommUtil.compare(bgdate, CommTools.getBaseRunEnvs().getTrxn_date()) > 0) {

             throw ItError.intr.BNASL117();
         }

         if (CommUtil.isNull(endate)) {

             throw ItError.intr.BNASL085();
         }

         if (bgdate.compareTo(endate) > 0) {

             throw ItError.intr.BNASL118();
         }
        if (CommUtil.isNotNull(cardno)) {
        	//获取卡bin
    		KnpParameter cardbn = KnpParameterDao.selectOne_odb1("KcdProd.cardbn", "kabin", "%", "%", true);
    		String kabin = cardbn.getParm_value1();
            if (cardno.length() != 19
                    || !CommUtil.equals(cardno.substring(0, 6), kabin)) {

                throw ItError.intr.BNASL168();
            }
            IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcByCardno(cardno, false);

            if (CommUtil.isNull(tblKnaAcdc) && CommUtil.isNotNull(cardno)) {
                throw ItError.intr.BNASL167(cardno);
            }

            if (!CommUtil.equals(CommTools.getBaseRunEnvs().getBusi_org_id(), tblKnaAcdc.getCorpno())) {
                throw ItError.intr.BNASL086();
            }
        }
        //查询下级机构插入临时表		
        long pageno = CommTools.getBaseRunEnvs().getPage_start();

        long pagesize = CommTools.getBaseRunEnvs().getPage_size();

        Options<IoDpKnbInrtAdjt> adjustInterestInfo = new DefaultOptions<>();

        String qrbrch = CommTools.getBaseRunEnvs().getTrxn_branch();

        Page<IoDpKnbInrtAdjt> list = DpAcctDao.selKnbInrtAdjtInfos(cardno,
                adjttp, bgdate, endate, qrbrch, (pageno - 1) * pagesize,
                pagesize, 0, false);

        adjustInterestInfo.addAll(list.getRecords());

        CommTools.getBaseRunEnvs().setTotal_count(list.getRecordCount());
        return adjustInterestInfo;
    }

    @Override
    public IoDpClsChkOT TestInterest(IoDpClsChkIN chkin) {

        return DpTestCalcInterest.CalcTestInterest(chkin);
    }

    /**
     * 
     * @Title: selDqCuasqu
     * @Description: 存款资产查询
     * @param input
     * @param output
     * @author zhangjunlei
     * @date 2016年7月7日 上午11:14:06
     * @version V2.3.0
     */
    @Override
    public void selDqCuasqu(
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.selDqCuasqu.Input input,
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.selDqCuasqu.Output output) {

        long totlCount = 0; // 记录总数
        long startno = (input.getPageno() - 1) * input.getPagesize();// 起始记录数

        // 获取输入数据
        String cardno = input.getCardno();// 电子账号
        String acctno = input.getAcctno();// 负债账号
        String prodcd = input.getProdcd();// 产品编号

        // 电子账号
        if (CommUtil.isNull(cardno)) {
            throw DpModuleError.DpstAcct.BNAS0311();
        }

        // 当前页码
        if (CommUtil.isNull(input.getPageno())) {
            throw CaError.Eacct.BNAS0977();
        }

        // 页容量
        if (CommUtil.isNull(input.getPagesize())) {
            throw CaError.Eacct.BNAS0463();
        }

        // 根据cardno，acctno，prodcd查询负债账号和电子账号对照信息,结果集为output
        cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.selAcctnoInfo.Output selact = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
                .selAcctnoInfo(cardno, acctno, prodcd, input.getPddpfg(), input.getSprdid(), input.getSprdna(),
                        startno, input.getPagesize(), totlCount);

        // 将获取的结果集由output转换为option
        Options<IoCaSelAcctno> results = new DefaultOptions<IoCaSelAcctno>();
        results = selact.getSelact();

        // 将结果集由option转换为list
        List<IoCaSelAcctno> kna_accs = new ArrayList<IoCaSelAcctno>();
        kna_accs = results.getValues();

        // 初始化变量
        BigDecimal totbal = BigDecimal.ZERO;// 存款总额
        BigDecimal curbal = BigDecimal.ZERO;// 活期总额
        BigDecimal fxabal = BigDecimal.ZERO;// 定期总额
        BigDecimal acctbl = BigDecimal.ZERO;// 余额
        String nowDate = CommTools.getBaseRunEnvs().getTrxn_date();//当前日期
        String lsdate = CommTools.getBaseRunEnvs().getLast_date();//上次交易日期
        E_TERMCD depttm = E_TERMCD.T000;// 存期
        long depday = 0;// 存期天数
        long stoday = 0;// 已存天数
        String iscurr = "N";// 是否有签约活期
        String lsisnl = "Y";// 列表是否为空
        String begndt = "";// 开始日期
        String fishdt = "";// 结束日期
        String faflag = "";// 定活标志
        //		E_YES___ trdpfg = E_YES___.NO;//是否转存标志

        //根据电子账号获取电子账号ID
        IoCaKnaAcdc cplKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class)
                .getKnaAcdcOdb2(cardno, false);
        //查询该电子账号是否签约
        IoCaKnaSign cplKnaSign = SysUtil.getInstance(IoCaSevQryTableInfo.class)
                .getKnaSignOdb1(cplKnaAcdc.getCustac(),
                        cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP.ZNCXL, E_SIGNST.QY, false);
        if (CommUtil.isNotNull(cplKnaSign)) {
            iscurr = "Y";
        }
        // 如果列表信息为空，返回数据都为0
        if (kna_accs.size() == 0) {

            output.setTotbal(totbal);// 存款总额
            output.setCurbal(BigDecimal.ZERO);// 活期总额
            output.setFxabal(BigDecimal.ZERO);// 定期总额
            output.setIscurr(iscurr);// 是否有签约活期
            output.setLsisnl(lsisnl);// 列表是否为空
            output.setPddetl(null);// 列表为空

        } else {

            // 循环获取列表信息
            for (IoCaSelAcctno acinfo : kna_accs) {

                IoDpDpAssetsQry acdetl = SysUtil.getInstance(IoDpDpAssetsQry.class);

                // 获取电子账号，定活标志，币种，产品编号
                acdetl.setAcctno(acinfo.getAcctno());
                acdetl.setCrcycd(acinfo.getCrcycd());
                acdetl.setProdcd(acinfo.getProdcd());
                E_FCFLAG faflag1 = acinfo.getFaflag();
                E_YES___ trdpfg = null;//是否转存标志

                //查询负债账户计息信息表中判断利率确定日期
                KnbAcin tblKnbAcin = KnbAcinDao.selectOne_odb1(acinfo.getAcctno(), false);
                if (CommUtil.isNotNull(tblKnbAcin)) {
                    if (faflag1 == E_FCFLAG.CURRENT) {
                        BigDecimal cuusin = prcAcctCuusin(tblKnbAcin, acinfo);
                        acdetl.setCuusin(cuusin);
                    } else if (faflag1 == E_FCFLAG.FIX) {
                        BigDecimal cuusin = prcFxacCuusin(tblKnbAcin, acinfo);
                        acdetl.setCuusin(cuusin);
                    }
                }

                // 活期
                if (faflag1 == E_FCFLAG.CURRENT) {

                    // 查询活期账信息
                    KnaAcct KnaAcct = KnaAcctDao.selectOne_odb1(acinfo.getAcctno(), false);

                    if (CommUtil.isNull(KnaAcct)) {

                    } else if (CommUtil.isNotNull(KnaAcct)) {

                        //查询负债账户计提明细的计提利息
                        KnbCbdl tblKnbCbdl = KnbCbdlDao.selectOne_odb2(lsdate, acinfo.getAcctno(), false);
                        if (CommUtil.isNotNull(tblKnbCbdl)) {
                            acdetl.setIntrvl(tblKnbCbdl.getCabrin());
                        }

                        //新增可售产品ID
                        KnaAcctProd tblKnaAcctProd = KnaAcctProdDao.selectOne_odb1(
                                acinfo.getAcctno(), false);

                        if (CommUtil.isNotNull(tblKnaAcctProd)) {
                            acdetl.setSprdid(tblKnaAcctProd.getSprdid());// 可售产品ID
                            acdetl.setSprdvr(tblKnaAcctProd.getSprdvr());// 当前版本号
                            acdetl.setSprdna(tblKnaAcctProd.getObgaon());//可售产品名称
                        }

                        // 活期账户信息不为空
                        acctbl = DpAcctProc.getAcctBalance(KnaAcct);// 余额
                        begndt = KnaAcct.getOpendt();// 开始日期

                        // 结束日期，有销户日期选销户日期，否则选到期日期
                        if (CommUtil.isNull(KnaAcct.getClosdt())) {
                            fishdt = KnaAcct.getMatudt();
                        } else {
                            fishdt = KnaAcct.getClosdt();
                        }

                        stoday = DateTools2.calDays(begndt, nowDate, 0, 0);// 已存天数
                        curbal = curbal.add(acctbl);// 活期总额
                        faflag = "01";// 定活标志
                    }
                } else if (faflag1 == E_FCFLAG.FIX) {

                    KnaFxac KnaFxac = KnaFxacDao.selectOne_odb1(acinfo.getAcctno(), false);// 查询定期信息

                    //获取账户利率表信息
                    KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acinfo.getAcctno(), true);

                    if (CommUtil.isNull(KnaFxac)) {

                    } else if (CommUtil.isNotNull(KnaFxac)) {

                        //计算定期预期收益
                        if (CommUtil.compare(KnaFxac.getUpbldt(), nowDate) >= 0 && tblKnbAcin.getDetlfg() == E_YES___.NO) {
                            String sEdindt = DateTimeUtil.dateAdd("day", nowDate, -1);
                            //计算计提程序输入
                            DpInstPrcIn cplDpInstPrcIn = SysUtil.getInstance(DpInstPrcIn.class);
                            cplDpInstPrcIn.setInoptp(E_INDLTP.CAIN);
                            cplDpInstPrcIn.setTrandt(nowDate);
                            cplDpInstPrcIn.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
                            cplDpInstPrcIn.setLstrdt(lsdate);
                            cplDpInstPrcIn.setEdindt(sEdindt);
                            cplDpInstPrcIn.setOnlnbl(KnaFxac.getOnlnbl()); //当前账户余额
                            cplDpInstPrcIn.setCrcycd(KnaFxac.getCrcycd()); //账户货币代号
                            cplDpInstPrcIn.setBrchno(KnaFxac.getBrchno()); //所属机构
                            cplDpInstPrcIn.setProdcd(KnaFxac.getProdcd()); //产品编号
                            cplDpInstPrcIn.setAcctcd(KnaFxac.getAcctcd()); //核算代码
                            //计算计提利息
                            Map<String, BigDecimal> mapCabrin = DpInterest.prcInstMain(tblKnbAcin, tblKubInrt, cplDpInstPrcIn);
                            acdetl.setIntrvl(mapCabrin.get("bigCabrin"));
                        } else {
                            //查询负债账户计提明细的计提利息
                            KnbCbdl tblKnbCbdl = KnbCbdlDao.selectOne_odb2(lsdate, acinfo.getAcctno(), false);
                            if (CommUtil.isNotNull(tblKnbCbdl)) {
                                acdetl.setIntrvl(tblKnbCbdl.getCabrin());
                            }
                        }

                        // chaiwenchang 新增可售产品ID 当前版本号输出接口
                        KnaFxacProd tblKnaFxacProd = KnaFxacProdDao
                                .selectOne_odb1(acinfo.getAcctno(), false);

                        if (CommUtil.isNotNull(tblKnaFxacProd)) {
                            acdetl.setSprdid(tblKnaFxacProd.getSprdid());// 可售产品ID
                            acdetl.setSprdvr(tblKnaFxacProd.getSprdvr());// 当前版本号
                            acdetl.setSprdna(tblKnaFxacProd.getObgaon());// 可售产品名称
                        }

                        KnaFxacMatu tblKnaFxacMatu = KnaFxacMatuDao.selectOne_odb1(KnaFxac.getAcctno(), false);
                        if (CommUtil.isNotNull(tblKnaFxacMatu)) {
                            trdpfg = tblKnaFxacMatu.getTrdpfg();
                        }
                        // 定期账户信息不为空
                        acctbl = KnaFxac.getOnlnbl();// 余额
                        begndt = KnaFxac.getOpendt();// 开始日期

                        // 结束日期，有销户日期选销户日期，否则选到期日期
                        if (CommUtil.isNull(KnaFxac.getClosdt())) {
                            fishdt = KnaFxac.getMatudt();
                        } else {
                            fishdt = KnaFxac.getClosdt();
                        }

                        depttm = KnaFxac.getDepttm();// 存期

                        if (CommUtil.isNotNull(KnaFxac.getMatudt())) {
                            depday = DateTools2.calDays(KnaFxac.getOpendt(), KnaFxac.getMatudt(), 0, 0);// 存期天数
                        }

                        stoday = DateTools2.calDays(begndt, nowDate, 0, 0);// 已存天数
                        fxabal = fxabal.add(acctbl);// 定期总额
                        faflag = "02";
                    }
                }

                acdetl.setFaflag(faflag);// 定活标志
                acdetl.setAcctbl(acctbl);// 余额
                acdetl.setBegndt(begndt);// 开始日期
                acdetl.setFishdt(fishdt);// 结束日期
                acdetl.setDepttm(depttm);// 存期
                acdetl.setDepday(depday);// 存期天数
                acdetl.setStoday(stoday);// 已存天数
                acdetl.setTrdpfg(trdpfg);//是否自动转存标识

                output.getPddetl().add(acdetl);// 把结果集返回给output
            }

            lsisnl = "N";// 列表是否为空

            totbal = curbal.add(fxabal);// 存款总额

            output.setTotbal(totbal);// 存款总额
            output.setCurbal(curbal);// 活期总额
            output.setFxabal(fxabal);// 定期总额
            output.setIscurr(iscurr);// 是否有签约活期
            output.setLsisnl(lsisnl);// 列表是否为空

        }

        // 设置总记录数
        output.setCounts(selact.getCounts());
        // 设置报文头总记录条数
        CommTools.getBaseRunEnvs().setTotal_count(selact.getCounts());

    }

    private BigDecimal prcFxacCuusin(KnbAcin tblKnbAcin, IoCaSelAcctno acinfo) {

        //定期账户
        String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//当前日期

        //执行利率
        BigDecimal intrvl = BigDecimal.ZERO;

        IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
        //传统定期
        if (CommUtil.equals(E_YES___.NO.getValue(), tblKnbAcin.getDetlfg().getValue())) {

            // 获取定期负债账户信息
            KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(acinfo.getAcctno(), true);

            //计算计提利息

            if (E_IRCDTP.Reference == tblKnbAcin.getIncdtp() || E_IRCDTP.BASE == tblKnbAcin.getIncdtp()) { //参考利率
                //获取账户利率信息
                KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acinfo.getAcctno(), true);

                // modify 20161221 liaojc 当利率确定日期为支取日，重新获取当前执行利率
                BigDecimal cuusin = tblKubInrt.getCuusin(); // 账户利率表执行利率

                if (tblKnbAcin.getIntrdt() == E_INTRDT.DRAW) {

                    IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                    intrEntity.setCrcycd(acinfo.getCrcycd()); //币种
                    intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码

                    //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                    intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                    intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                    intrEntity.setTrandt(trandt);
                    intrEntity.setDepttm(tblKnaFxac.getDepttm());// 存期
                    intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起始日期
                    //到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
                    //					if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
                    //						String termcd = acinfo.getDepttm().getValue();
                    //						if(CommUtil.equals(termcd.substring(0, 1),"9")){
                    //							intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
                    //						}else{
                    //							intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
                    //						}
                    //					}
                    //获取负债账号签约明细信息
                    IoCaKnaSignDetl cplKnaSignDetl = DpAcctDao.selKnaSignDetlInfo(acinfo.getAcctno(), false);

                    if (CommUtil.isNotNull(tblKnaFxac)) {
                        if (CommUtil.isNotNull(tblKnaFxac.getMatudt())) {
                            intrEntity.setEdindt(tblKnaFxac.getMatudt());//止息日
                        } else if (CommUtil.isNotNull(cplKnaSignDetl)) {
                            if (CommUtil.isNotNull(cplKnaSignDetl.getEffedt())) {
                                intrEntity.setEdindt(cplKnaSignDetl.getEffedt()); //止息日
                            }
                        } else {
                            intrEntity.setEdindt("20991231");
                        }
                    }

                    if (CommUtil.isNull(intrEntity.getEdindt())) {
                        intrEntity.setEdindt("20991231"); //止息日
                    }
                    //					intrEntity.setEdindt(trandt); //结束日期
                    intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                    intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                    intrEntity.setCorpno(tblKnbAcin.getCorpno());//法人代码
                    intrEntity.setBrchno(tblKnaFxac.getBrchno());//机构

                    intrEntity.setLevety(tblKnbAcin.getLevety());
                    pbpub.countInteresRate(intrEntity);

                    cuusin = intrEntity.getIntrvl(); //当前执行利率

                    //利率可取最大值
                    BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                    //利率可取最小值
                    BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
                    // 利率优惠后执行利率
                    cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                            divide(BigDecimal.valueOf(100))));

                    //若优惠后的利率小于最大可取利率则赋值为最大可取利率
                    if (CommUtil.compare(cuusin, maxval) > 0) {
                        cuusin = maxval;
                    }
                    //若优惠后的利率小于最小可取利率则赋值为最小可取利率
                    if (CommUtil.compare(cuusin, minval) < 0) {
                        cuusin = minval;
                    }
                    cuusin = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);
                }

                //自动转存重订价产品处理
                if (tblKnbAcin.getInprwy() == E_IRRTTP.QD) {

                    //计算利息，使用行内基准的活期利率
                    //IntrPublicEntity intrMatuEntity = new IntrPublicEntity();
                    IoPbIntrPublicEntity intrMatuEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                    intrMatuEntity.setCrcycd(tblKnaFxac.getCrcycd()); //币种
                    intrMatuEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码
                    intrMatuEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                    intrMatuEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                    intrMatuEntity.setDepttm(tblKnaFxac.getDepttm()); //存期
                    intrMatuEntity.setTrandt(trandt);
                    intrMatuEntity.setBgindt(tblKnaFxac.getBgindt()); //起始日期
                    intrMatuEntity.setEdindt(tblKnaFxac.getMatudt()); //结束日期
                    intrMatuEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                    intrMatuEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                    intrMatuEntity.setCorpno(tblKnaFxac.getCorpno());//法人代码
                    intrMatuEntity.setBrchno(tblKnaFxac.getBrchno());//机构

                    intrMatuEntity.setLevety(tblKnbAcin.getLevety());
                    if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
                        intrMatuEntity.setTrandt(tblKnaFxac.getOpendt());
                        intrMatuEntity.setTrantm("999999");
                    }
                    pbpub.countInteresRate(intrMatuEntity);

                    //bigInstam = intrMatuEntity.getInamnt();

                    cuusin = intrMatuEntity.getIntrvl(); //当前执行利率

                    // 利率优惠后执行利率
                    cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                            divide(BigDecimal.valueOf(100))));

                    //mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
                    //利率的最大范围值
                    BigDecimal intrvlmax = intrMatuEntity.getBaseir().multiply(BigDecimal.ONE.add(intrMatuEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                    //利率的最小范围值
                    BigDecimal intrvlmin = intrMatuEntity.getBaseir().multiply(BigDecimal.ONE.add(intrMatuEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

                    if (CommUtil.compare(cuusin, intrvlmin) < 0) {
                        cuusin = intrvlmin;
                    } else if (CommUtil.compare(cuusin, intrvlmax) > 0) {
                        cuusin = intrvlmax;
                    }
                }

                intrvl = cuusin;

            } else if (E_IRCDTP.LAYER == tblKnbAcin.getIncdtp()) {

                IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                intrEntity.setCorpno(tblKnbAcin.getCorpno()); //法人代码
                intrEntity.setBrchno(acinfo.getBrchno());//机构号
                intrEntity.setTranam(acinfo.getOnlnbl());//交易金额
                intrEntity.setTrandt(trandt);//交易日期
                intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码 
                intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                intrEntity.setCrcycd(acinfo.getCrcycd());//币种
                intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                intrEntity.setCainpf(E_CAINPF.T1); //计息规则
                intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起息日期

                //到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
                //					if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
                //						String termcd = acinfo.getDepttm().getValue();
                //						if(CommUtil.equals(termcd.substring(0, 1),"9")){
                //							intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
                //						}else{
                //							intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
                //						}
                //					}

                //获取负债账号签约明细信息
                IoCaKnaSignDetl cplKnaSignDetl = DpAcctDao.selKnaSignDetlInfo(acinfo.getAcctno(), false);

                if (CommUtil.isNotNull(tblKnaFxac)) {
                    if (CommUtil.isNotNull(tblKnaFxac.getMatudt())) {
                        intrEntity.setEdindt(tblKnaFxac.getMatudt());//止息日
                    } else if (CommUtil.isNotNull(cplKnaSignDetl)) {
                        if (CommUtil.isNotNull(cplKnaSignDetl.getEffedt())) {
                            intrEntity.setEdindt(cplKnaSignDetl.getEffedt()); //止息日
                        }
                    } else {
                        intrEntity.setEdindt("20991231");
                    }
                }

                if (CommUtil.isNull(intrEntity.getEdindt())) {
                    intrEntity.setEdindt("20991231"); //止息日
                }

                intrEntity.setLevety(tblKnbAcin.getLevety());
                if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
                    intrEntity.setTrandt(tblKnbAcin.getOpendt());
                    intrEntity.setTrantm("999999");
                }
                pbpub.countInteresRate(intrEntity);

                //获取账户利率信息
                KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acinfo.getAcctno(), true);

                //利率可取最大值
                BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                //利率可取最小值
                BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

                // 利率优惠后执行利率
                BigDecimal cuusin = intrEntity.getIntrvl();
                cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                        divide(BigDecimal.valueOf(100))));

                //若优惠后的利率小于最大可取利率则赋值为最大可取利率
                if (CommUtil.compare(cuusin, maxval) > 0) {
                    cuusin = maxval;
                }
                //若优惠后的利率小于最小可取利率则赋值为最小可取利率
                if (CommUtil.compare(cuusin, minval) < 0) {
                    cuusin = minval;
                }

                intrvl = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);

            } else { // 分层利率在利率优惠代码块中进行实现
                throw DpAcError.DpDeptComm.BNAS1081();
            }

        } else {//智能储蓄

            // 获取定期负债账户信息
            KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(acinfo.getAcctno(), true);

            if (E_IRCDTP.Reference == tblKnbAcin.getIncdtp() || E_IRCDTP.BASE == tblKnbAcin.getIncdtp()) { //参考利率
                //获取账户利率信息
                KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acinfo.getAcctno(), true);

                // modify 20161221 liaojc 当利率确定日期为支取日，重新获取当前执行利率
                BigDecimal cuusin = tblKubInrt.getCuusin(); // 账户利率表执行利率

                if (tblKnbAcin.getIntrdt() == E_INTRDT.DRAW) {

                    IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                    intrEntity.setCrcycd(acinfo.getCrcycd()); //币种
                    intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码

                    //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                    intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                    intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                    intrEntity.setTrandt(trandt);
                    intrEntity.setDepttm(tblKnaFxac.getDepttm());// 存期
                    intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起始日期
                    //到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
                    //					if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
                    //						String termcd = acinfo.getDepttm().getValue();
                    //						if(CommUtil.equals(termcd.substring(0, 1),"9")){
                    //							intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
                    //						}else{
                    //							intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
                    //						}
                    //					}
                    //获取负债账号签约明细信息
                    IoCaKnaSignDetl cplKnaSignDetl = DpAcctDao.selKnaSignDetlInfo(acinfo.getAcctno(), false);

                    if (CommUtil.isNotNull(tblKnaFxac)) {
                        if (CommUtil.isNotNull(tblKnaFxac.getMatudt())) {
                            intrEntity.setEdindt(tblKnaFxac.getMatudt());//止息日
                        } else if (CommUtil.isNotNull(cplKnaSignDetl)) {
                            if (CommUtil.isNotNull(cplKnaSignDetl.getEffedt())) {
                                intrEntity.setEdindt(cplKnaSignDetl.getEffedt()); //止息日
                            }
                        } else {
                            intrEntity.setEdindt("20991231");
                        }
                    }

                    if (CommUtil.isNull(intrEntity.getEdindt())) {
                        intrEntity.setEdindt("20991231"); //止息日
                    }
                    //					intrEntity.setEdindt(trandt); //结束日期
                    intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                    intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                    intrEntity.setCorpno(tblKnbAcin.getCorpno());//法人代码
                    intrEntity.setBrchno(tblKnaFxac.getBrchno());//机构

                    intrEntity.setLevety(tblKnbAcin.getLevety());
                    pbpub.countInteresRate(intrEntity);

                    cuusin = intrEntity.getIntrvl(); //当前执行利率

                    //利率可取最大值
                    BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                    //利率可取最小值
                    BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
                    // 利率优惠后执行利率
                    cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                            divide(BigDecimal.valueOf(100))));
                    //若优惠后的利率大于最大可取利率则赋值为最大可取利率
                    if (CommUtil.compare(cuusin, maxval) > 0) {
                        cuusin = maxval;
                    }
                    //若优惠后的利率小于最小可取利率则赋值为最小可取利率
                    if (CommUtil.compare(cuusin, minval) < 0) {
                        cuusin = minval;
                    }
                    cuusin = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);
                }

                intrvl = cuusin;

            } else if (E_IRCDTP.LAYER == tblKnbAcin.getIncdtp()) {

                IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                intrEntity.setCorpno(tblKnbAcin.getCorpno()); //法人代码
                intrEntity.setBrchno(acinfo.getBrchno());//机构号

                //查询定期明细表的记录
                KnaFxacDetl tblKnaFxacDetl = DpAcctDao.selKnaFxacDetlByAcctno(acinfo.getAcctno(), false);
                if (CommUtil.isNotNull(tblKnaFxacDetl)) {
                    intrEntity.setTranam(tblKnaFxacDetl.getOnlnbl());//交易金额
                    intrEntity.setBgindt(tblKnaFxacDetl.getBgindt()); //起息日期
                } else {
                    intrEntity.setTranam(acinfo.getOnlnbl());//交易金额
                    intrEntity.setBgindt(acinfo.getBgindt()); //起息日期
                }

                intrEntity.setTrandt(trandt);//交易日期
                intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码 
                intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                intrEntity.setCrcycd(acinfo.getCrcycd());//币种
                intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                intrEntity.setCainpf(E_CAINPF.T1); //计息规则

                //到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
                //					if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
                //						String termcd = acinfo.getDepttm().getValue();
                //						if(CommUtil.equals(termcd.substring(0, 1),"9")){
                //							intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
                //						}else{
                //							intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
                //						}
                //					}
                //					if(CommUtil.isNull(intrEntity.getEdindt())){
                //						intrEntity.setEdindt(trandt); //止息日
                //					}

                //获取负债账号签约明细信息
                IoCaKnaSignDetl cplKnaSignDetl = DpAcctDao.selKnaSignDetlInfo(acinfo.getAcctno(), false);

                if (CommUtil.isNotNull(tblKnaFxac)) {
                    if (CommUtil.isNotNull(tblKnaFxac.getMatudt())) {
                        intrEntity.setEdindt(tblKnaFxac.getMatudt());//止息日
                    } else if (CommUtil.isNotNull(cplKnaSignDetl)) {
                        if (CommUtil.isNotNull(cplKnaSignDetl.getEffedt())) {
                            intrEntity.setEdindt(cplKnaSignDetl.getEffedt()); //止息日
                        }
                    } else {
                        intrEntity.setEdindt("20991231");
                    }
                }

                if (CommUtil.isNull(intrEntity.getEdindt())) {
                    intrEntity.setEdindt("20991231"); //止息日
                }

                intrEntity.setLevety(tblKnbAcin.getLevety());
                if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
                    intrEntity.setTrandt(tblKnbAcin.getOpendt());
                    intrEntity.setTrantm("999999");
                }
                pbpub.countInteresRate(intrEntity);

                //获取账户利率信息
                KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acinfo.getAcctno(), true);
                //利率可取最大值
                BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                //利率可取最小值
                BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

                // 利率优惠后执行利率
                BigDecimal cuusin = intrEntity.getIntrvl();
                cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                        divide(BigDecimal.valueOf(100))));

                //若优惠后的利率小于最大可取利率则赋值为最大可取利率
                if (CommUtil.compare(cuusin, maxval) > 0) {
                    cuusin = maxval;
                }
                //若优惠后的利率小于最小可取利率则赋值为最小可取利率
                if (CommUtil.compare(cuusin, minval) < 0) {
                    cuusin = minval;
                }

                intrvl = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);

            } else { // 分层利率在利率优惠代码块中进行实现
                throw DpAcError.DpDeptComm.BNAS1081();
            }
        }

        return intrvl;
    }

    private BigDecimal prcAcctCuusin(KnbAcin tblKnbAcin, IoCaSelAcctno acinfo) {

        String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//当前日期
        //由于行内利率会出现变化，固活期需要分段计息

        BigDecimal cutmam = tblKnbAcin.getCutmam(); //本期积数
        BigDecimal avgtranam = BigDecimal.ZERO; //平均余额

        //活期总积数
        BigDecimal totalAcmltn = BigDecimal.ZERO;
        //执行利率
        BigDecimal intrvl = BigDecimal.ZERO;
        //实际积数
        BigDecimal realCutmam = DpPublic.calRealTotalAmt(cutmam, acinfo.getOnlnbl(), trandt, tblKnbAcin.getLaamdt());

        totalAcmltn = totalAcmltn.add(realCutmam); //加实际积数			

        IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);

        //查询账户利率表，获取利率优惠值
        KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(tblKnbAcin.getAcctno(), false);

        if (E_IRCDTP.LAYER == tblKnbAcin.getIncdtp()) {

            int days = 1; //计提天数					

            if (CommUtil.compare(tblKnbAcin.getInammd(), E_IBAMMD.ACCT) == 0) { //账户余额				

                throw Intr.E9999("活期产品暂不支持账户余额靠档利率");

            } else if (CommUtil.compare(tblKnbAcin.getInammd(), E_IBAMMD.AVG) == 0) {//平均余额

                days = calAvgDays(tblKnbAcin.getIrwptp(), tblKnbAcin.getBldyca(), tblKnbAcin.getTxbefr(),
                        tblKnbAcin.getLcindt(), tblKnbAcin.getNcindt(), trandt);

                if (CommUtil.equals(totalAcmltn, BigDecimal.ZERO)) {
                    avgtranam = acinfo.getOnlnbl();//平均余额
                } else {
                    avgtranam = totalAcmltn.divide(BigDecimal.valueOf(days), 2, BigDecimal.ROUND_HALF_UP);//平均余额
                }

                IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                intrEntity.setCorpno(tblKnbAcin.getCorpno()); //法人代码
                intrEntity.setBrchno(acinfo.getBrchno());//机构号
                intrEntity.setTranam(avgtranam);//交易金额
                intrEntity.setTrandt(trandt);//交易日期
                intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码 
                intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                intrEntity.setCrcycd(acinfo.getCrcycd());//币种
                intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                intrEntity.setCainpf(E_CAINPF.T1); //计息规则
                intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起息日期
                //到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
                //				if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
                //					String termcd = acinfo.getDepttm().getValue();
                //					if(CommUtil.equals(termcd.substring(0, 1),"9")){
                //						intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
                //					}else{
                //						intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
                //					}
                //				}

                KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(acinfo.getAcctno(), true);
                if (CommUtil.isNotNull(tblKnaAcct)) {
                    if (CommUtil.isNotNull(tblKnaAcct.getMatudt())) {
                        intrEntity.setEdindt(tblKnaAcct.getMatudt());//止息日
                    } else {
                        intrEntity.setEdindt(trandt); //止息日
                    }
                }

                if (CommUtil.isNull(intrEntity.getEdindt())) {
                    intrEntity.setEdindt(trandt); //止息日
                }
                //				intrEntity.setEdindt(cplDpInstPrcIn.getTrandt());  //止息日

                intrEntity.setLevety(tblKnbAcin.getLevety());
                if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
                    intrEntity.setTrandt(tblKnbAcin.getOpendt());
                    intrEntity.setTrantm("999999");
                }
                pbpub.countInteresRate(intrEntity);

                BigDecimal cuusin = intrEntity.getIntrvl();//获取利率
                //利率可取最大值
                BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                //利率可取最小值
                BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

                // 利率优惠后执行利率
                cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                        divide(BigDecimal.valueOf(100))));
                //若优惠后的利率小于最大可取利率则赋值为最大可取利率
                if (CommUtil.compare(cuusin, maxval) > 0) {
                    cuusin = maxval;
                }
                //若优惠后的利率小于最小可取利率则赋值为最小可取利率
                if (CommUtil.compare(cuusin, minval) < 0) {
                    cuusin = minval;
                }
                intrvl = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);

            } else if(CommUtil.compare(tblKnbAcin.getInammd(), E_IBAMMD.SUM) == 0){//积数靠档
                /** add by huangwh 20181122 start  积数靠档 */

                IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                intrEntity.setCorpno(tblKnbAcin.getCorpno()); //法人代码
                intrEntity.setBrchno(acinfo.getBrchno());//机构号
                intrEntity.setTranam(totalAcmltn);/** 交易金额   = 活期总积数 */
                intrEntity.setTrandt(trandt);//交易日期
                intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码 
                intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                intrEntity.setCrcycd(acinfo.getCrcycd());//币种
                intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                intrEntity.setCainpf(E_CAINPF.T1); //计息规则
                intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起息日期
                //到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
                //              if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
                //                  String termcd = acinfo.getDepttm().getValue();
                //                  if(CommUtil.equals(termcd.substring(0, 1),"9")){
                //                      intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
                //                  }else{
                //                      intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));           
                //                  }
                //              }

                KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(acinfo.getAcctno(), true);
                if (CommUtil.isNotNull(tblKnaAcct)) {
                    if (CommUtil.isNotNull(tblKnaAcct.getMatudt())) {
                        intrEntity.setEdindt(tblKnaAcct.getMatudt());//止息日
                    } else {
                        intrEntity.setEdindt(trandt); //止息日
                    }
                }

                if (CommUtil.isNull(intrEntity.getEdindt())) {
                    intrEntity.setEdindt(trandt); //止息日
                }
                //              intrEntity.setEdindt(cplDpInstPrcIn.getTrandt());  //止息日

                intrEntity.setLevety(tblKnbAcin.getLevety());
                if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
                    intrEntity.setTrandt(tblKnbAcin.getOpendt());
                    intrEntity.setTrantm("999999");
                }
                pbpub.countInteresRate(intrEntity);

                BigDecimal cuusin = intrEntity.getIntrvl();//获取利率
                //利率可取最大值
                BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                //利率可取最小值
                BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

                // 利率优惠后执行利率
                cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                        divide(BigDecimal.valueOf(100))));
                //若优惠后的利率小于最大可取利率则赋值为最大可取利率
                if (CommUtil.compare(cuusin, maxval) > 0) {
                    cuusin = maxval;
                }
                //若优惠后的利率小于最小可取利率则赋值为最小可取利率
                if (CommUtil.compare(cuusin, minval) < 0) {
                    cuusin = minval;
                }
                intrvl = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);
                
                /** add by huangwh 20181122 end */
            }else {
                throw DpAcError.DpDeptComm.BNAS1593();
            }

        } else if (E_IRCDTP.Reference == tblKnbAcin.getIncdtp() || E_IRCDTP.BASE == tblKnbAcin.getIncdtp()) { //参考利率

            // modify 20161221 liaojc 当利率确定日期为支取日，重新获取当前执行利率
            BigDecimal cuusin = tblKubInrt.getCuusin(); // 账户利率表执行利率

            if (tblKnbAcin.getIntrdt() == E_INTRDT.DRAW) {

                IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                intrEntity.setCrcycd(acinfo.getCrcycd()); //币种
                intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码

                //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                intrEntity.setTrandt(trandt);
                intrEntity.setDepttm(E_TERMCD.T000);// 存期
                intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起始日期
                //到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
                //				if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
                //					String termcd = acinfo.getDepttm().getValue();
                //					if(CommUtil.equals(termcd.substring(0, 1),"9")){
                //						intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
                //					}else{
                //						intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
                //					}
                //				}

                KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(acinfo.getAcctno(), false);
                if (CommUtil.isNotNull(tblKnaAcct)) {
                    if (CommUtil.isNotNull(tblKnaAcct.getMatudt())) {
                        intrEntity.setEdindt(tblKnaAcct.getMatudt());//止息日
                    } else {
                        intrEntity.setEdindt(trandt); //止息日
                    }
                }

                if (CommUtil.isNull(intrEntity.getEdindt())) {
                    intrEntity.setEdindt(trandt); //止息日
                }
                //				intrEntity.setEdindt(trandt); //结束日期
                intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                intrEntity.setCorpno(tblKnbAcin.getCorpno());//法人代码
                intrEntity.setBrchno(acinfo.getBrchno());//机构

                intrEntity.setLevety(tblKnbAcin.getLevety());
                pbpub.countInteresRate(intrEntity);

                cuusin = intrEntity.getIntrvl(); //当前执行利率

                //利率可取最大值
                BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                //利率可取最小值
                BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
                // 利率优惠后执行利率
                cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                        divide(BigDecimal.valueOf(100))));

                //若优惠后的利率大于最大可取利率则赋值为最大可取利率
                if (CommUtil.compare(cuusin, maxval) > 0) {
                    cuusin = maxval;
                }
                //若优惠后的利率小于最小可取利率则赋值为最小可取利率
                if (CommUtil.compare(cuusin, minval) < 0) {
                    cuusin = minval;
                }
                cuusin = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);
            }

            intrvl = cuusin;

        } else {
            throw DpAcError.DpDeptComm.BNAS1081();
        }
        return intrvl;
    }

    /**
     * @author yanghang
     *         <p>
     *         <li>2016年8月28日 </li>
     *         <li>计算平均余额对应周期天数</li>
     *         </p>
     * 
     * @param cycltp 周期类型
     * @param txbefr 结息频率
     * @param lcdt 上次结息日期
     * @param ncdt 下次结息日期
     * @param curdt 当前日期
     * @param avbldt 当前日期
     * @return 平均余额对应周期天数
     */

    private int calAvgDays(E_CYCLTP cycltp, E_AVBLDT avbldt, String txbefr, String lcdt, String ncdt, String curdt) {
        int days;

        if (CommUtil.isNull(ncdt)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Prod.ncindt.getLongName());
        }
        if (CommUtil.isNull(lcdt)) {
            if (CommUtil.isNull(txbefr)) {
                throw DpAcError.DpDeptComm.E9027(DpDict.Prod.txbefr.getLongName());
            }
            //	    	lcdt = DateTools2.calDateByFreq(ncdt, txbefr, "", "", 3, 2);
            lcdt = DateTools2.calDateByFreq(ncdt, txbefr, null, 2);
        }

        if (CommUtil.compare(avbldt, E_AVBLDT.T1) == 0) {//实际天数
            days = DateTools2.calDays(lcdt, ncdt, 0, 0); // 实际天数
        } else {
            days = DateTools2.calDays(lcdt, ncdt, 1, 0); // 储蓄天数
        }

        return days;
    }

    @Override
    public IoDpCloseOT CloseSubAcct(IoDpCloseIN clsin) {

        return DpCloseCustac.CloseCustac(clsin);
    }

    /**
     * 
     * @author xiongzhao
     *         <p>
     *         <li>2016年7月5日-下午8:14:22</li>
     *         <li>功能描述：电子账户客户端销户存款检查</li>
     *         </p>
     * 
     * @param custac
     *        电子账号ID
     * 
     */
    public void closeAcDpCheck(String custac) {

        BigDecimal drawam = BigDecimal.ZERO;// 智能储蓄支取明细

        //查询电子账户分类
        E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
                .qryAccatpByCustac(custac);

        // 输入接口检查
        if (CommUtil.isNull(custac)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.custac.getLongName());
        }

        // 检查是否有定期存款
        List<KnaFxac> lstKnaFxac = KnaFxacDao.selectAll_odb4(custac,
                E_DPACST.NORMAL, false);
        if (CommUtil.isNotNull(lstKnaFxac) && lstKnaFxac.size() > 0) {

            for (KnaFxac fxac : lstKnaFxac) {

                // 检查定期余额是否为0
                if (CommUtil.compare(fxac.getOnlnbl(), BigDecimal.ZERO) != 0) {
                    throw DpAcError.DpDeptComm.BNAS0398();
                }

                // 查询是否存在未结息的情况
                KnbAcin tblKnbAcin = KnbAcinDao.selectOne_odb1(
                        fxac.getAcctno(), true);
                if (CommUtil.compare(tblKnbAcin.getCutmam(), BigDecimal.ZERO) != 0) {
                    throw DpAcError.DpDeptComm.BNAS0398();
                }

                // 查询利息分段表
                List<KnbIndl> lstKnbIndls = KnbIndlDao.selectAll_odb4(
                        fxac.getAcctno(), E_INDLST.YOUX, false);
                if (CommUtil.isNotNull(lstKnbIndls) && lstKnbIndls.size() > 0) {
                    throw DpAcError.DpDeptComm.BNAS0398();
                }

                // 查询智能储蓄是否结息
                KnbAcin tblKnbAcin1 = KnbAcinDao.selectOne_odb1(
                        fxac.getAcctno(), true);

                // 计算定期当前计提利息
                drawam = drawam.add(BusiTools.roundByCurrency(fxac.getCrcycd(),
                        tblKnbAcin1.getPlanin(), null));

                // 查询定期支取明细是否结息完成，状态为正常的是还没有结息的
                List<KnaFxacDrdl> tblKnaFxacDrdls = KnaFxacDrdlDao
                        .selectAll_odb3(fxac.getAcctno(), false);

                // 智能储蓄处理
                if (CommUtil.isNotNull(tblKnaFxacDrdls)) {
                    for (KnaFxacDrdl tblFxac_drdl : tblKnaFxacDrdls) {
                        if (E_ACCTST.NORMAL != tblFxac_drdl.getTranst()) {
                            continue;
                        }
                        drawam = drawam.add(tblFxac_drdl.getIntram());
                    }
                }

                // 检查只能储蓄结息金额是否为0
                if (CommUtil.compare(drawam, BigDecimal.ZERO) != 0) {
                    throw DpAcError.DpDeptComm.BNAS0398();
                }
            }
        }

        // 检查活期存款
        List<KnaAcct> lstKnaAcct = KnaAcctDao.selectAll_odb4(custac,
                E_DPACST.NORMAL, false);
        if (CommUtil.isNotNull(lstKnaAcct) && lstKnaAcct.size() > 0) {

            for (KnaAcct acct : lstKnaAcct) {

                if (E_ACCATP.GLOBAL == accatp || E_ACCATP.FINANCE == accatp) {
                    if (E_ACSETP.SA == acct.getAcsetp()) {
                        continue;
                    }
                } else if (E_ACCATP.WALLET == accatp) {
                    if (E_ACSETP.MA == acct.getAcsetp()) {
                        continue;
                    }
                }

                // 检查除个人结算户外的活期账户
                if (CommUtil.compare(acct.getOnlnbl(), BigDecimal.ZERO) != 0) {
                    throw DpAcError.DpDeptComm.BNAS1133(acct.getAcsetp().getLongName());
                }

                /*		// 查询是否存在未结息的情况
                		KnbAcin tblKnbAcin = KnbAcinDao.selectOne_odb1(
                				acct.getAcctno(), false);
                		if (CommUtil.isNotNull(tblKnbAcin)) {
                			if (CommUtil.compare(tblKnbAcin.getCutmam(),
                					BigDecimal.ZERO) != 0) {
                				throw DpAcError.DpDeptComm.E9999("活期存款余额不为0！");
                			}
                		}

                		// 查询利息分段表
                		List<KnbIndl> lstKnbIndls = KnbIndlDao.selectAll_odb4(
                				acct.getAcctno(), E_INDLST.YOUX, false);
                		if (CommUtil.isNotNull(lstKnbIndls)
                				&& lstKnbIndls.size() > 0) {
                			throw DpAcError.DpDeptComm.E9999("活期存款余额不为0！");
                		}*/

            }
        }

    }

    public void saveIOknlnapot(InknlcnapotDetl inknlnapot) {

        KnlCnapot cnapot = SysUtil.getInstance(KnlCnapot.class);
        cnapot.setServdt(inknlnapot.getServdt());
        cnapot.setServsq(inknlnapot.getServsq());
        cnapot.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq()); // 业务跟踪编号
        cnapot.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 渠道

        cnapot.setSubsys(inknlnapot.getSubsys());
        cnapot.setMsetdt(inknlnapot.getMsetdt());
        cnapot.setMsetsq(inknlnapot.getMsetsq());
        cnapot.setCrdbtg(inknlnapot.getCrdbtg());
        cnapot.setMesgtp(inknlnapot.getMesgtp());
        cnapot.setIotype(inknlnapot.getIotype());
        cnapot.setCrcycd(inknlnapot.getCrcycd());
        cnapot.setCstrfg(inknlnapot.getCstrfg());
        cnapot.setCsextg(inknlnapot.getCsextg());
        cnapot.setPyercd(inknlnapot.getPyercd());
        cnapot.setPyeecd(inknlnapot.getPyeecd());
        cnapot.setPyerac(inknlnapot.getPyerac());
        cnapot.setPyerna(inknlnapot.getPyerna());
        cnapot.setPyeeac(inknlnapot.getPyeeac());
        cnapot.setPyeena(inknlnapot.getPyeena());
        cnapot.setPriotp(inknlnapot.getPriotp());
        cnapot.setAfeetg(inknlnapot.getAfeetg());
        cnapot.setTranam(inknlnapot.getTranam());
        cnapot.setAfeeam(inknlnapot.getAfeeam());
        cnapot.setFeeam1(inknlnapot.getFeeam1());
        cnapot.setChfcnb(inknlnapot.getChfcnb());
        cnapot.setBrchno(inknlnapot.getBrchno());
        cnapot.setUserid(inknlnapot.getUserid());
        cnapot.setCkbkus(inknlnapot.getCkbkus());
        cnapot.setAuthus(inknlnapot.getAuthus());
        cnapot.setKeepdt(inknlnapot.getKeepdt());
        cnapot.setNpcpdt(inknlnapot.getNpcpdt());
        cnapot.setNpcpbt(inknlnapot.getNpcpbt());
        cnapot.setRemark1(inknlnapot.getRemark1());
        cnapot.setRemark2(inknlnapot.getRemark2());

        cnapot.setPrcscd(inknlnapot.getPrcscd());
        cnapot.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
        cnapot.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
        cnapot.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());
        cnapot.setStatus(CommUtil.nvl(inknlnapot.getStatus(), E_TRANST.NORMAL));
        //cnapot.setStatus(inknlnapot.getStatus());
        cnapot.setClerdt(inknlnapot.getClerdt());
        cnapot.setClenum(inknlnapot.getClenum());
        cnapot.setChckdt(inknlnapot.getNpcpdt());
        cnapot.setIscler(E_YES___.NO); // 是否已清算
        cnapot.setIsspan(inknlnapot.getIsspan()); // 是否跨法人\

        KnlCnapotDao.insert(cnapot);

    }

    @Override
    public void saveKnlnapotchrg(
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.saveKnlnapotchrg.Input input) {
        List<KnlCnapotChrg> entity = new ArrayList<>();
        Options<ChrgIN> lstChrg = input.getChrg();
        KnlCnapotChrg chrg = SysUtil.getInstance(KnlCnapotChrg.class);
        chrg.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq());
        chrg.setServdt(input.getTrandt());
        chrg.setServsq(input.getTransq());
        for (ChrgIN ls : lstChrg) {
            chrg.setAmount(ls.getAmount());
            chrg.setChrgcd(ls.getChrgcd());
            chrg.setClcham(ls.getClcham());
            chrg.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
            chrg.setDioage(ls.getDioage());
            chrg.setDioamo(ls.getDioamo());
            chrg.setDircam(ls.getDircam());
            chrg.setDisrat(ls.getDisrat());
            chrg.setDitage(ls.getDitage());
            chrg.setDitamo(ls.getDitamo());
            chrg.setDiwage(ls.getDiwage());
            chrg.setDiwamo(ls.getDiwamo());
            chrg.setPaidam(ls.getPaidam());
            chrg.setPronum(ls.getPronum());
            chrg.setTranam(ls.getTranam());
            chrg.setTrinfo(ls.getTrinfo());
            entity.add(chrg);
        }

        DaoUtil.insertBatch(KnlCnapotChrg.class, entity);

    }

    @Override
    public void selHqTranDetlAcc(
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.selHqTranDetlAcc.Input input,
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.selHqTranDetlAcc.Output output) {
        
        int totlCount = 0; // 记录总数
        int startno = (input.getPageno() - 1) * input.getPagesz();// 起始记录数
        //String counts = null;//交易总笔数
        //String totalo = null;//总支出
        //String totali = null;//总收入
        E_IOTYPE iotype = null;//出入账
        String recvna = null;//收款方名称
        String recvno = null;//收款方卡号
        String paynam = null;//付款方名称
        String paycno = null;//付款方卡号
        String custac = input.getCustac();// 电子账户ID
        E_QRACTP qractp = input.getQractp();// 查询账户类型
        String crcycd = input.getCrcycd();// 币种
        String bgindt = input.getBgindt();// 起始日期
        String endddt = input.getEndddt();// 终止日期
        String smrycd = input.getSmrycd();// 摘要
        String sTime = CommTools.getBaseRunEnvs().getTrxn_date();// 当前日期
        BigDecimal onlnbl = BigDecimal.ZERO;// 当前余额
        String chnlid = null;//开户渠道

        //add by zdj 20181019
        if(CommUtil.isNull(custac)){
            if(CommUtil.isNull(input.getCardno())){
                throw DpAcError.DpDeptComm.E9999("电子账号不能为空");
            }else{
                custac = KnaAcdcDao.selectOne_odb2(input.getCardno(), false).getCustac();
            }
        }
        //end
        // 电子账户ID
        if (CommUtil.isNull(custac)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.custac.getLongName());
        }

        // 查询账户类型
        if (CommUtil.isNull(qractp)) {
            throw DpAcError.DpDeptComm.BNAS1058();
        }

        // 币种
        if (CommUtil.isNull(crcycd)) {
            throw DpAcError.DpDeptComm.E9027(DpDict.Acct.crcycd.getLongName());
        }

        //		// 电子账户ID
        //		if (CommUtil.isNull(custac)) {
        //			throw DpAcError.DpDeptComm.E9999("电子账户ID不能为空");
        //		}

        // 当前页码
        if (CommUtil.isNull(input.getPageno())) {
            throw CaError.Eacct.BNAS0977();
        }

        // 页容量
        if (CommUtil.isNull(input.getPagesz())) {
            throw CaError.Eacct.BNAS0463();
        }
        
        if(CommUtil.isNotNull(KnbOpacDao.selectAll_odb1(custac, false))){
            //判断查询的电子账户开户渠道
            chnlid = KnbOpacDao.selectAll_odb1(custac, false).get(0).getUschnl().toString();
        }

        // 起始日期不为空时
        if (CommUtil.isNotNull(bgindt)) {

            if (CommUtil.isNull(endddt)) {
                throw DpAcError.DpDeptComm.BNAS0591();// 截止日期不能为空
            } else if (DateUtil.compareDate(bgindt, endddt) > 0) {
                throw DpAcError.DpDeptComm.BNAS0415(bgindt,endddt);// 起始日期不能大于终止日期
            }

            // 起始日期不能大于当前日期
            if (DateUtil.compareDate(bgindt, sTime) > 0) {
                throw DpAcError.DpDeptComm.BNAS0416(bgindt,sTime);
            }

            if(CommUtil.equals(chnlid, "YT")){//根据银户通只能按照月来查询，进行更改    例如20181101--20181130
                if(CommUtil.compare(endddt.substring(0, 4), sTime.substring(0, 4))>0){//银户通前端传的年份大于当前系统年份
                    throw CaError.Eacct.E0001("查询日期已超过"+sTime.substring(0, 4)+"年");
                }
                if(CommUtil.compare(endddt.substring(0, 4), sTime.substring(0, 4))==0){//银户通前端传的年份等于当前系统年份
                    if(CommUtil.compare(endddt.substring(4, 6), sTime.substring(4, 6))>0){//银户通前端传的月份大于当前系统月份
                        throw CaError.Eacct.E0001("查询日期已超过"+sTime.substring(4, 6)+"月");
                    }else if(CommUtil.compare(endddt.substring(4, 6), sTime.substring(4, 6))==0){//如果是当前月份，则按照系统当前日期进行查询
                        endddt = sTime;
                    }
                }
            }else{
                // 截止日期不能大于当前日期
                if (DateUtil.compareDate(endddt, sTime) > 0) {
                    throw CaError.Eacct.BNAS1691(endddt,sTime);
                }
            }
            
        } else if (CommUtil.isNotNull(endddt)) {
            // 截止日期不能为空时起始日期不能为空
            throw DpAcError.DpDeptComm.BNAS0411();
        }

        // 查询电子账户分类
        E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
                .qryAccatpByCustac(custac);
        List<KnaAcct> tblacct = null;
        List<String> acctnos = new ArrayList<>();
        //String acctnos = null;
        // 根据查询账户类型转换成对应的储种
        KnaAcct cplKnaAcct = SysUtil.getInstance(KnaAcct.class);
        if (qractp == E_QRACTP.PERSONAL) {
            if (eAccatp == E_ACCATP.GLOBAL || eAccatp == E_ACCATP.FINANCE) {
                //cplKnaAcct = CapitalTransDeal.getSettKnaAcctSub(custac, E_ACSETP.SA);
                //查询所有负债子账户
                tblacct = ActoacDao.selknaAcctclose(E_ACSETP.SA, custac, false);

                if (CommUtil.isNull(tblacct)) {
                    throw DpAcError.DpDeptComm.BNAS0915();
                }
                if (tblacct.size() == 1) {
                    CommUtil.copyProperties(cplKnaAcct, tblacct.get(0));
                }
                if (tblacct.size() > 1) {
                    for (KnaAcct accts : tblacct) {
                        if (accts.getAcctst() == E_DPACST.NORMAL) {
                            CommUtil.copyProperties(cplKnaAcct, accts);
                        }
                        acctnos.add(accts.getAcctno());
                    }
                }
            } else {
                cplKnaAcct = CapitalTransDeal.getSettKnaAcctSub(custac, E_ACSETP.MA);
                if (CommUtil.isNull(cplKnaAcct)) {
                    throw DpAcError.DpDeptComm.BNAS0915();
                }

            }
        } else if (qractp == E_QRACTP.WALLET) {
            cplKnaAcct = CapitalTransDeal.getSettKnaAcctSub(custac, E_ACSETP.MA);
            if (CommUtil.isNull(cplKnaAcct)) {
                throw DpAcError.DpDeptComm.BNAS0915();
            }
        } else if (qractp == E_QRACTP.ACCT) {// 电子现金账户信息为空
            cplKnaAcct = CapitalTransDeal.getSettKnaAcctSub(custac, E_ACSETP.CH);
            if (CommUtil.isNull(cplKnaAcct)) {
                throw DpAcError.DpDeptComm.BNAS0915();
            }
        } else {
            throw DpAcError.DpDeptComm.BNAS1057();
        }

        onlnbl = cplKnaAcct.getOnlnbl();

        /*
         * if (CommUtil.isNull(tblKnaAccts)) { throw
         * DpAcError.DpDeptComm.E9999("电子账号对应负债信息不存在"); }
         */

        String acctno = cplKnaAcct.getAcctno();// 负债账户

        Page<IoDpTranDetlHqQryAcct> infopPage;
        if (CommUtil.isNotNull(acctnos) && acctnos.size() > 1) {
            //add by zdj 20181218
            //当银户通调用此交易明细查询时候，要求不查出冲正的和被冲正的那条记录
            //场景：用户提现时候，可能会出现失败，然后出现三条记录。即一笔提现一笔冲正，和一笔提现
            String acctnostr = null;
            if(CommUtil.equals(chnlid, "YT")){//银户通账户
                //银户通调用
                // 分页获取交易明细
                //根据原SQL DpAcctDao.selAllHqTranDetl增加限制查询没有冲正交易
                acctnostr = acctnos.toString().substring(1, acctnos.toString().length() - 1);
                infopPage = YhtDao.selAllHqTranDetl(acctnostr, crcycd, bgindt, endddt, smrycd, startno,
                    input.getPagesz(), totlCount, false);
            }else{
                //原交易
                // 分页获取交易明细
                acctnostr = acctnos.toString().substring(1, acctnos.toString().length() - 1);
                infopPage = DpAcctDao.selAllHqTranDetl(acctnostr, crcycd, bgindt, endddt, smrycd, startno,
                        input.getPagesz(), totlCount, false);
            }
            //add end
            
            //add by zdj 20181020
            //list = YhtDao.selKnlbillByTranDetl(acctnostr, crcycd, bgindt, endddt, smrycd, false);
            //counts = YhtDao.selCountByTranDetl(acctnostr, crcycd, bgindt, endddt, smrycd, false);
            //totalo = YhtDao.selAmountByTranDetl(acctnostr, crcycd, bgindt, endddt, smrycd, "D", false);//总支出
            //totali = YhtDao.selAmountByTranDetl(acctnostr, crcycd, bgindt, endddt, smrycd, "C", false);//总收入
            //add end 20181020
        } else {
            //add by zdj 20181218
            //当银户通调用此交易明细查询时候，要求不查出冲正的和被冲正的那条记录
            //场景：用户提现时候，可能会出现失败，然后出现三条记录。即一笔提现一笔冲正，和一笔提现
            if(CommUtil.equals(chnlid, "YT")){//银户通卡号
                // 分页获取交易明细
                //根据原SQL DpAcctDao.selHqTranDetl增加限制查询没有冲正交易
                infopPage = YhtDao.selHqTranDetl(
                        acctno, crcycd, bgindt, endddt, smrycd, startno,
                        input.getPagesz(), totlCount, false);
            }else{
                //原交易
                // 分页获取交易明细
                infopPage = DpAcctDao.selHqTranDetl(
                        acctno, crcycd, bgindt, endddt, smrycd, startno,
                        input.getPagesz(), totlCount, false);
            }
            //add end
            
            //add by zdj 20181020
            //list = YhtDao.selKnlbillByTranDetl(acctno, crcycd, bgindt, endddt, smrycd, false);
            //counts = YhtDao.selCountByTranDetl(acctno, crcycd, bgindt, endddt, smrycd, false);
            //totalo = YhtDao.selAmountByTranDetl(acctno, crcycd, bgindt, endddt, smrycd, "D", false);//总支出
            //totali = YhtDao.selAmountByTranDetl(acctno, crcycd, bgindt, endddt, smrycd, "C", false);//总收入
            //add end 20181020
        }

        //		// 分页获取交易明细
        //		Page<IoDpTranDetlHqQryAcct> infopPage = DpAcctDao.selHqTranDetl(
        //				acctno, crcycd, bgindt, endddt, smrycd, startno,
        //				input.getPagesz(), totlCount, false);
        
        //add by zdj 20181020
//        CommUtil.copyProperties(list, infopPage);
        for(IoDpTranDetlHqQryAcct info : infopPage.getRecords()){
            IoDpTranDetlHqQryAcct e = SysUtil.getInstance(IoDpTranDetlHqQryAcct.class);
            List<KnaSignDetl> tblKnaSignDetl = YhtDao.selKnaSignDetlByAcctno(acctno, false);
            KnlIoblCups tblKnlIoblCups = DpAcctQryDao.selKnlIoblCupsByTransq(info.getTransq(), false);
            IoKnlIobl ioKnlIobl = DpAcctQryDao.selKnlIoblbyTransq(info.getTransq(), false);
            InknlcnapotDetl ioKnlCnapot = DpAcctQryDao.selKnlCnapotbytransq(info.getTransq(), false);
            e.setTranam(info.getTranam());//交易金额
            e.setTrantm(info.getTrandt()+info.getTrantm());//交易时间
            e.setTrandt(info.getTrandt());//交易日期
            e.setCrcycd(crcycd);//币种
            e.setAmntcd(info.getAmntcd());//借贷标志
            e.setTmstmp(info.getTmstmp());//时间戳
            String intrcd = info.getIntrcd();
            String smrycd1 = null;
            //获取电子账号的开户渠道
            if(CommUtil.equals(chnlid, "YT")){
                e.setTrserv("银户通");//银户通查询时候交易渠道 就固定为银户通
            }else{
                e.setServtp(info.getServtp());//交易渠道
            }
            if(info.getAmntcd() == E_AMNTCD.CR || info.getAmntcd() == E_AMNTCD.RV){
                //前置传入的转入、转出有问题，生产有存量数据，增加根据借贷标志判断转入、转出
                if(info.getAmntcd() == E_AMNTCD.CR){
                    if(intrcd.contains("lsamin") || intrcd.contains("suprin") 
                            || intrcd.contains("cupsin") || intrcd.contains("sfcdin") 
                            || intrcd.contains("alchin") || intrcd.contains("otcdin")){
                        smrycd1 = "ZR";
                    }else{
                        smrycd1 = info.getSmrycd();
                    }
                }
                if(CommUtil.equals(chnlid, "YT")){//银户通开户
                    if(info.getRemark().contains("冲正") && CommUtil.compare(info.getTranam(), BigDecimal.ZERO)<0){//如果冲正，则入账改为出账，出账改为入账
                        //银户通要求不返回冲正的数据
                        continue;
                        //iotype = E_IOTYPE.OUT;//出账
                    }else{
                        iotype = E_IOTYPE.IN;//入账
                    }
                }
                KnlBill tblKnlBill = YhtDao.selKnlbillByTransq(info.getTransq(), info.getAmntcd(), false);
                if(CommUtil.isNull(tblKnlBill)){
                    throw DpAcError.DpDeptComm.E9999("该流水不存在");
                }
                if("cupsin".equals(tblKnlBill.getIntrcd()) || "cupsot".equals(tblKnlBill.getIntrcd())){//保持查询出来的明细和短信通知的一致
                    if(CommUtil.isNull(tblKnlIoblCups)){
                        throw DpAcError.DpDeptComm.E9999("该流水不存在");
                    }
//                    recvno = tblKnlIoblCups.getInacct();//转入账号
//                    recvna = tblKnlIoblCups.getInacna();//收款方名称
//                    paycno = tblKnlIoblCups.getOtacct();//转出账号
//                    paynam = tblKnlIoblCups.getOtacna();//付款方名称
                }else{
                    paycno = tblKnlBill.getOpcuac();//付款方卡号
                    paynam = tblKnlBill.getOpacna();//付款方名称
                    recvno = tblKnlBill.getCardno();//收款方卡号
                    recvna = tblKnlBill.getAcctna();//收款方名称
                }
                
            }else if(info.getAmntcd() == E_AMNTCD.DR || info.getAmntcd() == E_AMNTCD.PY){
                //前置传入的转入、转出有问题，生产有存量数据，增加根据借贷标志判断转入、转出
                if(info.getAmntcd() == E_AMNTCD.DR){
                    if(intrcd.contains("suprot") || intrcd.contains("sfcdot") 
                            || intrcd.contains("lsamot") || intrcd.contains("cupsot") 
                            || intrcd.contains("alchot") || intrcd.contains("otcdot")){
                        smrycd1 = "ZC";
                    }else{
                        smrycd1 = info.getSmrycd();
                    }
                }
                if(CommUtil.equals(chnlid, "YT")){//银户通卡号
                    if(info.getRemark().contains("冲正") && CommUtil.compare(info.getTranam(), BigDecimal.ZERO)<0){//如果冲正，则入账改为出账，出账改为入账
                        //银户通要求不返回冲正的数据
                        continue;
                        //iotype = E_IOTYPE.IN;//出账
                    }else{
                        iotype = E_IOTYPE.OUT;//出账
                    }
                }
                KnlBill tblKnlBill = YhtDao.selKnlbillByTransq(info.getTransq(), info.getAmntcd(), false);
                if(CommUtil.isNull(tblKnlBill)){
                    throw DpAcError.DpDeptComm.E9999("该流水不存在");
                }
                if("cupsin".equals(tblKnlBill.getIntrcd()) || "cupsot".equals(tblKnlBill.getIntrcd())){//保持查询出来的明细和短信通知的一致
                    if(CommUtil.isNull(tblKnlIoblCups)){
                        throw DpAcError.DpDeptComm.E9999("该流水不存在");
                    }
//                    recvno = tblKnlIoblCups.getInacct();//转入账号
//                    recvna = tblKnlIoblCups.getInacna();//收款方名称
//                    paycno = tblKnlIoblCups.getOtacct();//转出账号
//                    paynam = tblKnlIoblCups.getOtacna();//付款方名称
                }else{
                    recvno = tblKnlBill.getOpcuac();//收款方卡号
                    recvna = tblKnlBill.getOpacna();//收款方名称
                    paycno = tblKnlBill.getCardno();//付款方卡号
                    paynam = tblKnlBill.getAcctna();//付款方名称
                }
            }
            
            if("ZR".equals(smrycd1)){
                e.setTrantp(E_TRANTP.CZ.getLongName());//充值
            }else if("ZC".equals(smrycd1)){
                e.setTrantp(E_TRANTP.TX.getLongName());//提现
            }else if("FK".equals(smrycd1)){
                e.setTrantp(E_TRANTP.FK.getLongName());//贷款到账
            }else if("HK".equals(smrycd1)){
                e.setTrantp(E_TRANTP.HK.getLongName());//还款
            }else if("SX".equals(smrycd1)){//单独结息
                e.setTrantp(E_TRANTP.JX.getLongName());//单独结息
            }else if("TZ".equals(smrycd1)){//投资
                if(CommUtil.isNotNull(tblKnaSignDetl)){
                    if(info.getIntrcd().contains("auacin")){//如果存在电子账户转智能储蓄交易
                        e.setTrantp(E_TRANTP.QY.getLongName());//签约
                    }else if(info.getIntrcd().contains("smterm")){
                        e.setTrantp(E_TRANTP.JY.getLongName());//解约
                    }else{
                        e.setTrantp(E_TRANTP.QT.getLongName());//其他
                    }
                }else{
                    if(info.getIntrcd().contains("smbuyy")){
                        e.setTrantp(E_TRANTP.CKCR.getLongName());//购买
                    }else if(info.getIntrcd().contains("smrans")){
                        KnsAcsq tblKnsAcsq = YhtDao.selKnsacsqByTransqBltype(info.getTransq(), E_BLTYPE.PYIN, false);
                        if(CommUtil.isNotNull(tblKnsAcsq)){
                            e.setTrantp(E_TRANTP.ZQJX.getLongName());//支取结息
                        }else{
                            e.setTrantp(E_TRANTP.CKZQ.getLongName());//支取
                        }
                    }
                }
            }else if(info.getRemark().contains("冲正") && ConvertUtil.toInteger(info.getTranam())<0){
                e.setTrantp(E_TRANTP.CHZ.getLongName());//冲正
            }else if("AJ".equals(smrycd1)){//差错处理时候，摘要为AJ"调整"
                e.setTrantp("调整");
            }else{
                e.setTrantp(E_TRANTP.QT.getLongName());//交易类型
            }
            e.setIotype(iotype);//出账/入账
            e.setRecvna(recvna);//收款人名称
            e.setRecvno(recvno);//收款方卡号
            e.setPaynam(paynam);//付款方名称
            e.setPaycno(paycno);//付款方卡号
            if(CommUtil.equals(chnlid, "YT")){//银户通卡号
                e.setAcctbl(null);//账户余额
                e.setRemark(ApSmryTools.getText(smrycd1));//备注
            }else{
                e.setAcctbl(info.getAcctbl());//账户余额
                //获取不同登记薄中的备注字段
                if(intrcd.contains("suprot") || intrcd.contains("suprin") || intrcd.contains("sfcdot") || intrcd.contains("sfcdin")){//出入金登记薄
                    e.setRemark(ioKnlIobl.getRemark());//备注
                    e.setSmryds(ApSmryTools.getText(smrycd1));//摘要描述
                }else if(intrcd.contains("lsamin") ||intrcd.contains("lsamot")){//大小额
                    e.setRemark(ioKnlCnapot.getRemark());//备注
                    e.setSmryds(ApSmryTools.getText(smrycd1));//摘要描述
                }else if(intrcd.contains("cupsin") || intrcd.contains("cupsot") || intrcd.contains("alchin") || intrcd.contains("alchot") || intrcd.contains("otcdin") || intrcd.contains("otcdot")){//银联
                    e.setRemark(tblKnlIoblCups.getRemark());//备注
                    e.setSmryds(ApSmryTools.getText(smrycd1));//摘要描述
                }else{
                    e.setRemark("");//备注
                    e.setSmryds(info.getSmryds());//摘要描述
                }
            }
            
            e.setBanksq(info.getTransq());//银行处理流水
            e.setRecvba("");//收款银行
            e.setPayban("");//收款银行
            output.getHatrdt().add(e);//交易金额
        }

        output.setCardno(input.getCardno());//返回电子账号
        output.setTranno(infopPage.getRecordCount()+"");//交易总笔数
        output.setTotalo("");//总支出
        output.setTotali("");//总收入
        output.setPageno(input.getPageno().toString());//页码
        //add end 20181020
        
//        output.getHatrdt().addAll(infopPage.getRecords());//原来根据页数查询出来的集合
        output.setCounts(ConvertUtil.toInteger(infopPage.getRecordCount()));
//        output.setOnlnbl(onlnbl);
        output.setAcctno(cplKnaAcct.getAcctno());
        CommTools.getBaseRunEnvs().setTotal_count(infopPage.getRecordCount());
        
    }

    /**
     * 
     * @Title: acctStatusUpd
     * @Description: (电子账户销户注销电子账户服务)
     * @param cplClsAcctIn
     * @author xiongzhao
     * @date 2016年7月30日 下午4:17:54
     * @version V2.3.0
     */
    @Override
    public void acctStatusUpd(ClsAcctIn cplClsAcctIn) {
        // 注销电子账户
        DpAcctProc.prcAcctst(cplClsAcctIn);
    }

    @Override
    /**
     * 开户入账服务
     *@author wuzhixiang 
     *@data 20160731-15:32am
     */
    public void openAcctAndTranfe(
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.openAcctAndTranfe.Input input,
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.openAcctAndTranfe.Output output) {
        // 根据电子账号ID和账户类型查询正常状态下的账户信息
        List<KnaAcct> tblKnaAccts = KnaAcctDao.selectAll_odb2(input
                .getTranfe().getToacct(), input.getTranfe().getProdcd(), false);

        IoDpTranfeInfo khzInfo = input.getTranfe();

        final cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpOpenSub openInfo = SysUtil
                .getInstance(cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpOpenSub.class);

        CommUtil.copyProperties(khzInfo, openInfo);// 拷贝方法

        for (KnaAcct acct : tblKnaAccts) {

            SysUtil.getInstance(DpAcctSvcType.class).OpenSubAcct(openInfo);// 开户
            // 电子账户存入
            SaveDpAcctIn saveDpAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);

            saveDpAcctIn.setAcctno(acct.getAcctno());// 负债账号
            saveDpAcctIn.setCustac(acct.getCustac());// 电子账号
            saveDpAcctIn.setToacct(input.getTranfe().getToacct());// 入账账号
            saveDpAcctIn.setTranam(input.getTranfe().getTranam());// 交易金额
            saveDpAcctIn.setCrcycd(acct.getCrcycd());// 币种

            SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(
                    saveDpAcctIn);// 存入记账处理
        }
    }

    /**
     * IoDpProdParts.IoDpCloseOT
     * 
     * @Title: CloseSubAcctWa
     * @Description: 亲情钱包销户
     * @param clsin
     * @return
     * @author liaojincai
     * @date 2016年9月18日 下午8:45:25
     * @version V2.3.0
     */
    @Override
    public IoDpCloseOT CloseSubAcctWa(String acctno, IoDpCloseIN clsin) {

        // 获取负债账户信息
        KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(acctno, true);

        IoCaKnaAcdc cplKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb1(tblKnaAcct.getCustac(), E_DPACST.NORMAL, true);
        clsin.setCardno(cplKnaAcdc.getCardno());// 卡号
        clsin.setToname(tblKnaAcct.getAcctna());// 对方户名
        clsin.setTobrch(tblKnaAcct.getBrchno());// 对方机构
        clsin.setToacct(tblKnaAcct.getCustac());// 对方客户账号
        clsin.setSmrycd(BusinessConstants.SUMMARY_XH);// 摘要码
        // 处理销户利息

        InterestAndIntertax cplint = DpCloseAcctno.prcCurrInterest(tblKnaAcct, clsin);

        // 处理销户余额信息
        BigDecimal onlnbl = DpCloseAcctno.prcCurrOnbal(tblKnaAcct, clsin);

        IoDpCloseOT cplIoDpCloseOT = SysUtil.getInstance(IoDpCloseOT.class);
        cplIoDpCloseOT.setSettbl(cplint.getInstam().add(onlnbl));// 本息和
        cplIoDpCloseOT.setCrcycd(tblKnaAcct.getCrcycd());// 币种
        cplIoDpCloseOT.setIntxam(cplint.getIntxam());//利息税
        return cplIoDpCloseOT;
    }

    /**
     * 销户转账交易中调用该服务进行结算户的销户服务
     * 电子账户已是预销户状态，因此此处不需要结息
     */
    @Override
    public BigDecimal closeAcctno(String acctno, IoDpCloseIN clsin) {

        KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
        tblKnaAcct.setAcctno(acctno);

        BigDecimal onlbal = DpCloseAcctno.prcCurrOnbal(tblKnaAcct, clsin);

        return onlbal;
    }

    @Override
    public BigDecimal getProductBal(String custac, String crcycd,
            Boolean islock) {

        return DpAcctProc.getProductBal(custac, crcycd, islock);
    }

    /**
     * 
     * @author renjinghua
     *         <p>
     *         <li>2016年9月26日-下午4:53:41<li>
     *         <li>功能描述：查询非签约活期存款账户可用余额，排除冻结金额</li>
     *         </p>
     * 
     * @param acctno 负债账户
     * @return 可用余额
     */
    @Override
    public BigDecimal getOnlnblForFrozbl(String acctno, Boolean islock) {

        return DpAcctProc.getAcctOnlnblForFrozbl(acctno, islock);
    }

    /**
     * @Title: getSettKnaAcctAc
     * @Description: (根据电子账号账户分类查询结算户信息)
     * @param custac
     */
    @Override
    public IoDpKnaAcct getSettKnaAcctAc(String custac) {

        return CapitalTransDeal.getIOSettKnaAcct(custac);
    }

    /**
     * @Title: getSettKnaAcctSub
     * @Description: (查询结算子账号或钱包子账号)
     * @param custac
     * @param acsetp
     */
    @Override
    public IoDpKnaAcct getSettKnaAcctSub(String custac, E_ACSETP acsetp) {

        return CapitalTransDeal.getIOSettKnaAcctSub(custac, acsetp);
    }

    /**
     * @Title: selDpHsasqu
     * @Description: (查询存款资产历史记录)
     * @param input
     * @param output
     */
    @Override
    public void selDpHsasqu(
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.selDpHsasqu.Input input,
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.selDpHsasqu.Output output) {

        int totlCount = 0; // 记录总数
        int startno = (input.getPageno() - 1) * input.getPagesize();// 起始记录数

        // 获取输入数据
        String cardno = input.getCardno();// 电子账号

        // 电子账号
        if (CommUtil.isNull(cardno)) {
            throw DpModuleError.DpstAcct.BNAS0311();
        }

        // 当前页码
        if (CommUtil.isNull(input.getPageno())) {
            throw CaError.Eacct.BNAS0977();
        }

        // 页容量
        if (CommUtil.isNull(input.getPagesize())) {
            throw CaError.Eacct.BNAS0463();
        }
        // 判断cardno在表中是否存在
        IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class)
                .getKnaAcdcOdb2(cardno, false);
        if (CommUtil.isNull(tblKnaAcdc)) {
            throw CaError.Eacct.BNAS1651();
        }

        IoCaKnaCust tblKnaCust = SysUtil.getInstance(IoCaSevQryTableInfo.class)
                .getKnaCustByCustacOdb1(tblKnaAcdc.getCustac(), false);

        if (CommUtil.isNull(tblKnaCust)) {
            throw CaError.Eacct.BNAS0871();
        }
        Page<IoCaSelAcctno> cplKnaAccs = DpAcctDao.selAcctno(cardno, startno, input.getPagesize(), totlCount, false);

        List<IoCaSelAcctno> cplKnaAccsInfo = cplKnaAccs.getRecords();

        // 初始化变量
        String begndt = "";// 开始日期
        String fishdt = "";// 结束日期

        // 如果列表信息为空，返回数据都为0
        if (cplKnaAccsInfo.size() > 0) {
            // 循环获取列表信息
            for (IoCaSelAcctno acinfo : cplKnaAccsInfo) {

                IoDpDpAssetHisQry acdetl = SysUtil.getInstance(IoDpDpAssetHisQry.class);

                // 获取电子账号，产品编号
                acdetl.setAcctno(acinfo.getAcctno());
                acdetl.setProdcd(acinfo.getProdcd());
                acdetl.setProdlt(acinfo.getFaflag());
                acdetl.setCrcycd(acinfo.getCrcycd());
                E_FCFLAG faflag1 = acinfo.getFaflag();

                // 活期
                if (faflag1 == E_FCFLAG.CURRENT) {

                    // 查询活期账信息
                    KnaAcct KnaAcct = KnaAcctDao.selectOne_odb1(acinfo.getAcctno(), false);
                    if (CommUtil.isNull(KnaAcct)) {
                        // 活期账户信息为空
                        begndt = "";// 开始日期
                        fishdt = "";// 结束日期

                    } else if (CommUtil.isNotNull(KnaAcct)) {

                        //新增可售产品ID 当前版本号输出接口
                        KnaAcctProd tblKnaAcctProd = KnaAcctProdDao.selectOne_odb1(acinfo.getAcctno(), false);
                        if (CommUtil.isNotNull(tblKnaAcctProd)) {
                            acdetl.setSprdid(tblKnaAcctProd.getSprdid());// 可售产品ID
                            acdetl.setSprdvr(tblKnaAcctProd.getSprdvr());// 当前版本号
                            acdetl.setSprdna(tblKnaAcctProd.getObgaon());// 当前版本号
                        }

                        BigDecimal totbal = BigDecimal.ZERO;// 收益合计
                        //本息收益合计
                        totbal = DpAcctDao.selKnbPidlInfoByCurAcct(acinfo.getAcctno(), false);
                        acdetl.setAcctbl(totbal);

                        // 活期账户信息不为空
                        begndt = KnaAcct.getOpendt();// 开始日期

                        // 结束日期，有销户日期选销户日期，否则选到期日期
                        if (CommUtil.isNull(KnaAcct.getClosdt())) {
                            fishdt = KnaAcct.getMatudt();
                        } else {
                            fishdt = KnaAcct.getClosdt();
                        }
                    }
                } else if (faflag1 == E_FCFLAG.FIX) {

                    KnaFxac KnaFxac = KnaFxacDao.selectOne_odb1(acinfo.getAcctno(), false);// 查询定期信息
                    if (CommUtil.isNull(KnaFxac)) {
                        // 定期账户信息为空
                        begndt = "";// 开始日期
                        fishdt = "";// 结束日期

                    } else if (CommUtil.isNotNull(KnaFxac)) {
                        //新增可售产品ID 当前版本号输出接口
                        KnaFxacProd tblKnaFxacProd = KnaFxacProdDao
                                .selectOne_odb1(acinfo.getAcctno(), false);
                        if (CommUtil.isNotNull(tblKnaFxacProd)) {
                            acdetl.setSprdid(tblKnaFxacProd.getSprdid());// 可售产品ID
                            acdetl.setSprdvr(tblKnaFxacProd.getSprdvr());// 当前版本号
                            acdetl.setSprdna(tblKnaFxacProd.getObgaon());// 当前版本号
                        }
                        BigDecimal totbal = BigDecimal.ZERO;// 收益合计
                        //本息收益合计
                        totbal = DpAcctDao.selKnbPidlByAcctno(acinfo.getAcctno(), KnaFxac.getBgindt(), false);
                        acdetl.setAcctbl(totbal);

                        // 定期账户信息不为空
                        begndt = KnaFxac.getOpendt();// 开始日期

                        // 结束日期，有销户日期选销户日期，否则选到期日期
                        if (CommUtil.isNull(KnaFxac.getClosdt())) {
                            fishdt = KnaFxac.getMatudt();
                        } else {
                            fishdt = KnaFxac.getClosdt();
                        }
                    }
                }
                acdetl.setOpendt(begndt);// 开始日期
                acdetl.setClosdt(fishdt);// 结束日期
                output.getPddetl().add(acdetl);// 把结果集返回给output
            }
        }
        // 设置总记录数
        CommTools.getBaseRunEnvs().setTotal_count(cplKnaAccs.getRecordCount());
    }

    @Override
    public void registCnapotTransError(final InknlcnapotDetl inknlnapot) {

        DaoUtil.executeInNewTransation(new RunnableWithReturn<Integer>() {

            @Override
            public Integer execute() {

                KnsTranEror tblKnsTranEror = SysUtil.getInstance(KnsTranEror.class);
                tblKnsTranEror.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
                tblKnsTranEror.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
                tblKnsTranEror.setErortp(E_WARNTP.ACOUNT);
                tblKnsTranEror.setBrchno(inknlnapot.getBrchno());
                tblKnsTranEror.setOtacct(inknlnapot.getPyerac());
                tblKnsTranEror.setOtacna(inknlnapot.getPyerna());
                tblKnsTranEror.setOtbrch("");
                tblKnsTranEror.setInacct(inknlnapot.getPyeeac());
                tblKnsTranEror.setInacna(inknlnapot.getPyeena());
                tblKnsTranEror.setInbrch(inknlnapot.getBrchno());
                tblKnsTranEror.setTranam(inknlnapot.getTranam());
                tblKnsTranEror.setCrcycd(inknlnapot.getCrcycd());
                tblKnsTranEror.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
                tblKnsTranEror.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());
                tblKnsTranEror.setGlacct("");
                tblKnsTranEror.setGlacna("");
                tblKnsTranEror.setGlseeq("");

                tblKnsTranEror.setDescrb("大小额来账入账失败！");
                return KnsTranErorDao.insert(tblKnsTranEror);

            }

        });

    }

    public BigDecimal getProductBal(String custac, String crcycd,
            E_YES___ isdfam, Boolean islock) {

        return DpAcctProc.getProductBal(custac, crcycd, isdfam, islock);
    }

    /**
     * 获取子账户可用余额
     * 
     * @Title: getAcctAvabal
     * @Description: 获取子账户可用余额
     * @param acctno 负债账号
     * @param isdfam 是否减去冻结金额
     * @param islock 是否加锁查询
     * @return 可用余额
     * @author liaojincai
     * @date 2016年12月6日 下午8:45:03
     * @version V2.3.0
     */
    public BigDecimal getAcctAvabal(String acctno, E_YES___ isdfam,
            Boolean islock) {

        return DpAcctProc.getAcctOnlnblForFrozbl(acctno, isdfam, islock);
    }

    /**
     * 获取账户下活期资金池可用余额
     * 
     * @Title: getProductAvabal
     * @Description: 获取账户下活期资金池可用余额
     * @param custac 电子账号
     * @param crcycd 币种
     * @param isdfam 是否减去冻结金额
     * @param islock 是否加锁查询
     * @return 可用余额
     * @author liaojincai
     * @date 2016年12月6日 下午8:26:33
     * @version V2.3.0
     */
    public BigDecimal getProductAvabal(String custac, String crcycd,
            E_YES___ isdfam, Boolean islock) {

        return DpAcctProc.getProductBal(custac, crcycd, isdfam, islock);
    }

    @Override
    public void updKnbClacStat(String clossq, String custac, E_CLSTAT clstat) {

        String tmstmp = DateTools2.getCurrentTimestamp();

        DpAcctDao.updKnbClacStatBySeq(clstat, clossq, tmstmp);

        IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
        //获得电子账号、卡号
        //冲正注册
        cplInput.setCustac(custac);
        cplInput.setEvent1(clossq);
        cplInput.setEvent2(clstat.getValue());

        //注册修改销户登记簿事件
        cplInput.setTranev(ApUtil.TRANS_EVENT_CLACST);

        //ApStrike.regBook(cplInput);
		IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);    		
		apinput.setReversal_event_id(cplInput.getTranev());
		apinput.setInformation_value(SysUtil.serialize(cplInput));
		MsEvent.register(apinput, true);

    }

    /**
     * 
     * @Title: getDrawnBalance
     * @Description: (查询账户的可支取余额)
     * @param custac
     * @return
     * @author xiongzhao
     * @date 2016年12月19日 上午9:47:36
     * @version V2.3.0
     */
    @Override
    public BigDecimal getDrawnBalance(String custac, String crcycd,
            E_YES___ islock) {

        BigDecimal acctbl = BigDecimal.ZERO;// 可支取余额
        BigDecimal dpacbl = BigDecimal.ZERO;// 存款余额
        BigDecimal nuseam = BigDecimal.ZERO;// 冻结，止付，存款证明，预授权，开单，质押金额
        BigDecimal curram = BigDecimal.ZERO;// 活期总余额
        BigDecimal fixeam = BigDecimal.ZERO;// 定期总余额

        // 电子账户状态字查询
        IoDpAcStatusWord cplGetAcStWord = SysUtil.getInstance(
                IoDpFrozSvcType.class).getAcStatusWord(custac);

        // 计算负债活期账户总余额
        if (islock == E_YES___.YES) {
            curram = DpAcctDao.selSumEacctBlHWithLock(custac, crcycd, true);
        } else {
            curram = DpAcctDao.selSumEacctBlH(custac, crcycd, true);
        }

        // 计算负债定期账户总余额
        if (islock == E_YES___.YES) {
            fixeam = DpAcctDao.selSumEacctBlFxWithLock(custac, crcycd, true);
        } else {
            fixeam = DpAcctDao.selSumEacctBlFx(custac, crcycd, true);
        }

        dpacbl = curram.add(fixeam);// 存款余额

        // 冻结，止付，存款证明，预授权，开单，质押金额
        KnbFrozOwne tblKnbFrozOwne = SysUtil.getInstance(KnbFrozOwne.class);

        if (islock == E_YES___.YES) {
            tblKnbFrozOwne = KnbFrozOwneDao.selectOneWithLock_odb1(E_FROZOW.AUACCT, custac, false);
        } else {
            tblKnbFrozOwne = KnbFrozOwneDao.selectOne_odb1(E_FROZOW.AUACCT, custac, false);
        }

        if (CommUtil.isNotNull(tblKnbFrozOwne)) {
            nuseam = tblKnbFrozOwne.getFrozbl();// 冻结，止付，存款证明，预授权，开单，质押金额
        }

        if (cplGetAcStWord.getBrfroz() == E_YES___.YES
                || cplGetAcStWord.getDbfroz() == E_YES___.YES
                || cplGetAcStWord.getBkalsp() == E_YES___.YES
                || cplGetAcStWord.getOtalsp() == E_YES___.YES
                || cplGetAcStWord.getClstop() == E_YES___.YES) {
            // 当账户处于借冻，双冻，银行全止，外部全止的，可支取余额显示为0
        	// 增加客户止付时，返回可支取余额为0  add by lull 2017/10/14
        	/*
        	 * 4JF：当电子账户处于银行全止时，可支取余额为0。
        	 */
            acctbl = BigDecimal.ZERO;// 可支取余额

        } else {
            if (CommUtil.compare(dpacbl, nuseam) >= 0) {
                // 当前存款资产余额大于等于当前冻结，止付，存款证明，预授权，开单，质押金额总额时
                acctbl = dpacbl.subtract(nuseam);// 可支取余额
            } else {
                // 当前存款资产余额小于当前冻结，止付，存款证明，预授权，开单，质押金额总额时
                acctbl = BigDecimal.ZERO;// 可支取余额
            }
        }
        bizlog.debug(">>>>>>>>>>>可支取余额：" + acctbl + ">>>>>>>>>>>");
        return acctbl;

    }

    /**
     * 
     * @Title: getAcctaAvaBal
     * @Description: (查询子账户可用余额)
     * @param custac
     * @param acctno
     * @param isdfam
     * @param islock
     * @return
     * @author xiongzhao
     * @date 2016年12月20日 下午2:37:58
     * @version V2.3.0
     */
    @Override
    public BigDecimal getAcctaAvaBal(String custac, String acctno,
            String crcycd, E_YES___ isdfam, E_YES___ islock) {

        String custac1 = "";
        Boolean islkfg = true;
        BigDecimal avabal = BigDecimal.ZERO;// 可用余额
        BigDecimal hdbkam = BigDecimal.ZERO;// 理财冻结金额 
        KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);

        if (CommUtil.isNull(custac)) {
            throw DpAcError.DpDeptComm.BNAS1069();
        }
        if (CommUtil.isNull(acctno)) {
            throw DpAcError.DpDeptComm.BNAS1066();
        }
        if (CommUtil.isNull(crcycd)) {
            throw DpAcError.DpDeptComm.BNAS1070();
        }
        if (CommUtil.isNull(isdfam)) {
            throw DpAcError.DpDeptComm.BNAS1067();
        }
        if (CommUtil.isNull(islock)) {
            throw DpAcError.DpDeptComm.BNAS1068();
        }

        if (islock == E_YES___.YES) {
            // 不作处理
        } else {
            islkfg = false;
        }

        if (islock == E_YES___.YES) {
            tblKnaAcct = ActoacDao.selKnaAcctWithLock(acctno, true);

        } else {
            tblKnaAcct = ActoacDao.selKnaAcct(acctno, true);
        }

        // 获取转存签约明细信息
        IoCaKnaSignDetl cplkna_sign_detl = ActoacDao.selKnaSignDetl(acctno,
                E_SIGNTP.ZNCXL, E_SIGNST.QY, false);

        // 存在转存签约明细信息则取资金池可用余额
        if (CommUtil.isNotNull(cplkna_sign_detl)) {
            avabal = SysUtil.getInstance(DpAcctSvcType.class)
                    .getProductAvabal(custac, crcycd, E_YES___.NO, islkfg);
            custac1 = cplkna_sign_detl.getCustac();

        } else {
            avabal = tblKnaAcct.getOnlnbl();
            custac1 = tblKnaAcct.getCustac();
        }

        // 电子账号和负债账号是否匹配校验
        if (!CommUtil.equals(custac, custac1)) {
            throw DpAcError.DpDeptComm.BNAS1692();
        }

        // 若传入账号为结算户，则减掉理财系统冻结金额
        if (tblKnaAcct.getAcsetp() == E_ACSETP.SA && isdfam == E_YES___.YES) {
            // 查询冻结登记簿
            Options<IoDpKnbFroz> lstKnbFroz = SysUtil.getInstance(
                    IoDpFrozSvcType.class).qryKnbFroz(custac,
                    E_FROZST.VALID);
            for (IoDpKnbFroz knbfroz : lstKnbFroz) {
                if (knbfroz.getFroztp() == E_FROZTP.FNFROZ) {
                    if (knbfroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
                        hdbkam = hdbkam.add(knbfroz.getFrozam());// 保留金额
                    }
                }
            }
            avabal = avabal.subtract(hdbkam);
        }

        // 根据是否扣减冻结金额标志去做可用余额和总资产可支取余额对比
        if (isdfam == E_YES___.YES) {

            // 总资产可支取余额
            BigDecimal acctbl = SysUtil.getInstance(DpAcctSvcType.class)
                    .getDrawnBalance(custac, crcycd, islock);

            if (CommUtil.compare(avabal, acctbl) <= 0) {
                // 金额返回不作处理
            } else {
                avabal = acctbl;
            }
        } else {
            // 金额返回不作处理
        }
        bizlog.debug(">>>>>>>>>>>可用余额：" + avabal + ">>>>>>>>>>>");
        return avabal;
    }

    public void dealAcctStatAndSett(final cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct ioDpKnaAcct, cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST cuacst) {
        KnaAcct inacct = SysUtil.getInstance(KnaAcct.class);
        CommUtil.copyProperties(inacct, ioDpKnaAcct);
        CapitalTransDeal.dealAcctStatAndSett(cuacst, inacct);
    }

    /**
     * 
     * @Title: selDpType
     * @Description: 持有中存款类查询
     * @param input
     * @param output
     * @author chaiwenchang
     * @date 2017年02月07日 下午18:12:40
     * @version V2.3.0
     */
    @Override
    public void selDpType(
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.selDpType.Input input,
            cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.selDpType.Output output) {

        int totlCount = 0; // 记录总数
        int startno = (input.getPageno() - 1) * input.getPagesize();// 起始记录数

        // 电子账号
        if (CommUtil.isNull(input.getCardno())) {
            throw DpModuleError.DpstAcct.BNAS0311();
        }

        // 当前页码
        if (CommUtil.isNull(input.getPageno())) {
            throw CaError.Eacct.BNAS0977();
        }

        // 页容量
        if (CommUtil.isNull(input.getPagesize())) {
            throw CaError.Eacct.BNAS0463();
        }

        // 判断cardno在表中是否存在
        IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class)
                .getKnaAcdcOdb2(input.getCardno(), false);
        if (CommUtil.isNull(tblKnaAcdc)) {
            throw CaError.Eacct.BNAS1651();
        }

        BigDecimal totbal = DpAcctDao.selDpAcTotbalByCardno(input.getCardno(), false);
        Page<IoDpAcctProdInfo> dpAcctInfo = DpAcctDao.selDpAcctInfo(input.getCardno(), startno, input.getPagesize(), totlCount, false);
        List<IoDpAcctProdInfo> dpAcctInfos = dpAcctInfo.getRecords();
        Options<IoDpAcctProdInfo> results = new DefaultOptions<IoDpAcctProdInfo>();
        results.addAll(dpAcctInfos);
        output.setDpProdInfo(results);
        output.setTotbal(totbal);
        output.setCounts(ConvertUtil.toInteger(dpAcctInfo.getRecordCount()));
    }

    @Override
    public void updKnbSlepStat(E_SPPRST spprst, String spprdt, String spprtm,
            E_SLEPST slepst, String custac, E_SLEPST slepst2, String mtdate,
            String tmstmp) {
        ActoacDao.updKnbSlepStat(E_SPPRST.NORMAL, spprdt, spprtm, E_SLEPST.CANCEL, custac, E_SLEPST.SLEP, tmstmp);

        //休眠登记簿修改登记冲正事件
        IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
        cplInput.setCustac(custac);
        cplInput.setEvent1(spprdt);
        cplInput.setEvent2(spprtm);
        cplInput.setEvent3(E_SLEPST.SLEP.getValue());
        cplInput.setTranev(ApUtil.TRANS_EVENT_SLEP);
        //ApStrike.regBook(cplInput);
		IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);    		
		apinput.setReversal_event_id(cplInput.getTranev());
		apinput.setInformation_value(SysUtil.serialize(cplInput));
		MsEvent.register(apinput, true);

    }

    /**
     * 检查账户积数是否小于零
     * add by chenlk 20170302
     */
    @Override
    public void chkAmbl(String cardno) {

        // 查询电子账户个人结算户信息
        IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
        IoCaKnaAcdc acdc = caqry.getKnaAcdcOdb2(cardno, false);

        //更新法人号
        //		CommTools.getBaseRunEnvs().setBusi_org_id(acdc.getCorpno());
        //考虑到钱包户隔日冲正积数成负数，然后账户升级后进行销户，这时候需要检查结算户和钱包户两个账号的积数
        //KnaAcct cplKnaAcct = CapitalTransDeal.getSettKnaAcctAc(acdc.getCustac());
        List<KnaAcct> acctList = new ArrayList<KnaAcct>();
        KnaAcct cplKnaAcct_SA = ActoacDao.selKnaAcctAc(E_ACSETP.SA, acdc.getCustac(), E_DPACST.CLOSE, false);
        if (CommUtil.isNotNull(cplKnaAcct_SA)) {

            acctList.add(cplKnaAcct_SA);
        }

        KnaAcct cplKnaAcct_MA = ActoacDao.selKnaAcctAc(E_ACSETP.MA, acdc.getCustac(), E_DPACST.CLOSE, false);
        if (CommUtil.isNotNull(cplKnaAcct_MA)) {

            acctList.add(cplKnaAcct_MA);
        }
        for (KnaAcct cplKnaAcct : acctList) {

            List<KnbIndl> tblKnbIndls = KnbIndlDao.selectAll_odb4(cplKnaAcct.getAcctno(), E_INDLST.YOUX, false);
            KnbAcin tblKnbAcin = KnbAcinDao.selectOne_odb1(cplKnaAcct.getAcctno(), false);
            if(CommUtil.isNull(tblKnbAcin)){
            	throw DpAcError.DpDeptAcct.BNAS0767();
            }

            BigDecimal totalAcmltn = BigDecimal.ZERO;
            for (KnbIndl indl : tblKnbIndls) {

                totalAcmltn = totalAcmltn.add(indl.getAcmltn());

            }
            //实际积数
            BigDecimal realCutmam = DpPublic.calRealTotalAmt(tblKnbAcin.getCutmam(), cplKnaAcct.getOnlnbl(), CommTools.getBaseRunEnvs().getTrxn_date(), tblKnbAcin.getLaamdt());

            totalAcmltn = totalAcmltn.add(realCutmam); //加实际积数

            if (CommUtil.compare(totalAcmltn, BigDecimal.ZERO) < 0) {
                throw DpAcError.DpDeptComm.BNAS0181();
            }
        }

//        CommTools.getBaseRunEnvs().setBusi_org_id(odcorp);

    }

    /**
     * 根据负债帐号查询负债账户
     * 
     */

    public void selAcctByAcctno(String acctno, final cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.QryDpAcctOutMsg qryAcctMsg) {
        if (CommUtil.isNull(acctno)) {
            throw CaError.Eacct.BNAS1693(acctno);
        }
        KnaAcct knaAcct = KnaAcctDao.selectOne_odb1(acctno, true);
//        KubBrch brch = KubBrchDao.selectOne_odb1(knaAcct.getBrchno(), true);
        KnaAcdc acdc = KnaAcdcDao.selectFirst_odb3(knaAcct.getCustac(), true);
        QryDpAcctOutMsg out = SysUtil.getInstance(QryDpAcctOutMsg.class);
        out.setAcctbr(knaAcct.getBrchno());
        out.setAcctna(knaAcct.getAcctna());
        out.setAcctno(acctno);
        out.setAcctst(knaAcct.getAcctst());
//        out.setBrchna(brch.getBrchna());
        out.setCardno(acdc.getCardno());
    }

    
    /**
	 * 销户利息清单查询
	 * add by songkl 2017/04/01
	 *
	 */

	public Options<IoDpCloseDetailOT> Qrclirlist(
			String transq, String trandt, String cardno) {

		// 非空判断
		if (CommUtil.isNull(transq)) {
			throw DpAcError.DpDeptComm.E9027(transq);
		}

		if (CommUtil.isNull(trandt)) {
			throw DpAcError.DpDeptComm.E9027(trandt);
		}

		if (CommUtil.isNull(cardno)) {
			throw DpAcError.DpDeptComm.E9027(cardno);
		}

		IoDpAcdcOut cplKnaAcdc = DpAcctDao.selKnaAcdcInfoByCardno(cardno, true);
		
		Options<IoDpCloseDetailOT> results = new DefaultOptions<>();
		
		List<IoDpCloseDetailOT> lstKnaProd = DpAcctDao.selAcctProdByClossq(transq,
				cplKnaAcdc.getCustac(), false);
		
		BigDecimal saclbl = BigDecimal.ZERO;//结算户销户本金
		for (IoDpCloseDetailOT cplCloseDetailOT :lstKnaProd) {
			if (E_ACSETP.SA != cplCloseDetailOT.getDpactp()){
				saclbl = saclbl.add(cplCloseDetailOT.getClprcp()).add(cplCloseDetailOT.getClinst());
			}
			if (E_ACSETP.SA == cplCloseDetailOT.getDpactp()) {
				cplCloseDetailOT.setClprcp(cplCloseDetailOT.getClprcp().subtract(saclbl));
			}
		}
		
		if (CommUtil.isNotNull(lstKnaProd) && lstKnaProd.size() > 0)
			results.addAll(lstKnaProd);
//		results.setValues(lstKnaProd);
		
		return results;

	}


	@Override
	public void dpAccNestcm(cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.DpAccNestcm.Input input, cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.DpAccNestcm.Output output) {

		KnsStrkInput knsStrk1 = SysUtil.getInstance(KnsStrkInput.class);
		KnsStrkInput knsStrk2 = SysUtil.getInstance(KnsStrkInput.class);
		
		knsStrk1.setAmntcd(input.getAmntcd());
		knsStrk1.setRebuwo(input.getRebuwo());
		knsStrk1.setHappbl(input.getHappbl());
		knsStrk1.setCustac(input.getCustac());
		knsStrk1.setCrcycd(input.getCrcycd());
		knsStrk1.setReason(input.getReason());
		
		knsStrk2.setAcctna(input.getAcctna2());
		knsStrk2.setCustac(input.getCustac2());
		
		String prtrdt = input.getPrtrdt(); 
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		int ibdays=DateTools2.calDays(prtrdt, trandt, 0, 0);//天数差
		//客户账入账
		IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcByCardno(knsStrk1.getCustac(), true);

		
		IoDpKnaAcct ioDpKnaAcct= SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(tblKnaAcdc.getCustac());
		E_AMNTCD amntcd = knsStrk1.getAmntcd();
		BigDecimal tranam = BigDecimal.ZERO;
		if(knsStrk1.getRebuwo() ==E_REBUWA.B){
			tranam = knsStrk1.getHappbl();
		}else if (knsStrk1.getRebuwo() ==E_REBUWA.R){
			tranam = knsStrk1.getHappbl().negate();
		}
		
		output.setOtacno(tblKnaAcdc.getCustac());
		output.setTranam(tranam);
		output.setAmntcd(amntcd);
		
//		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//		CommTools.getBaseRunEnvs().setBusi_org_id(ioDpKnaAcct.getCorpno());
		
		//add 20160206 songlw 参数控制 是否利息和积数同时调整
		if(CommUtil.equals("Y", KnpParameterDao.selectOne_odb1("nestcm_isInat", "%", "%", "%", true).getParm_value1())){
			//处理计算调整利息，并记账,返回利息调整对应天数
			int lcDays = prcAdjustInst(ioDpKnaAcct, knsStrk1, tranam, prtrdt, amntcd);
			
			//积数调整天数重新计算，减去利息调整的天数
			ibdays = ibdays - lcDays;
		
			bizlog.debug("===============调整利息计算天数："+lcDays);
			bizlog.debug("===============调整利息后调整积数计算天数："+ibdays);
		}	
		
		if((E_AMNTCD.DR==amntcd&&knsStrk1.getRebuwo() ==E_REBUWA.B)||(E_AMNTCD.CR==amntcd&&knsStrk1.getRebuwo() ==E_REBUWA.R)){
			
			SysUtil.getInstance(IoAccountSvcType.class).checkStatusBeforeAccount(tblKnaAcdc.getCustac(), E_AMNTCD.DR,E_YES___.YES, E_YES___.NO);
		}else{
			
			SysUtil.getInstance(IoAccountSvcType.class).checkStatusBeforeAccount(tblKnaAcdc.getCustac(), E_AMNTCD.CR,E_YES___.YES, E_YES___.NO);
		}
		

		//客户账记账输入类字段赋值
		if(E_AMNTCD.CR==amntcd){
			//红字，调存入服务，记负数
			SaveDpAcctIn saveDpAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
			saveDpAcctIn.setAcctno(ioDpKnaAcct.getAcctno());
			saveDpAcctIn.setCustac(ioDpKnaAcct.getCustac());
			saveDpAcctIn.setCardno(knsStrk1.getCustac());
			saveDpAcctIn.setOpacna(knsStrk2.getAcctna());
			saveDpAcctIn.setToacct(knsStrk2.getCustac());
			saveDpAcctIn.setCrcycd(knsStrk1.getCrcycd());
			saveDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_CZ);
			saveDpAcctIn.setRemark(knsStrk1.getReason());
			saveDpAcctIn.setTranam(tranam);
			saveDpAcctIn.setNegafg(E_YES___.YES);//支持红字记账
			SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(saveDpAcctIn);
		} else {
			//蓝字，调支取服务，记正数
			DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
			drawDpAcctIn.setAcctno(ioDpKnaAcct.getAcctno());
			drawDpAcctIn.setCustac(ioDpKnaAcct.getCustac());
			drawDpAcctIn.setCardno(knsStrk1.getCustac());
			drawDpAcctIn.setOpacna(knsStrk2.getAcctna());
			drawDpAcctIn.setToacct(knsStrk2.getCustac());
			drawDpAcctIn.setCrcycd(knsStrk1.getCrcycd());
			drawDpAcctIn.setIschck(E_YES___.NO);
			drawDpAcctIn.setTranam(tranam);
			drawDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_CZ);
			drawDpAcctIn.setRemark(knsStrk1.getReason());
			SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawDpAcctIn);
		}
		
		//积数调整
		BigDecimal acmlbl1 = BigDecimal.ZERO;// 积数
		acmlbl1 = knsStrk1.getHappbl().multiply(new BigDecimal(ibdays));//积数
		
		IoDpKnbAcin cplAcin1 = SysUtil.getInstance(IoDpSrvQryTableInfo.class).getKnbAcinOdb1(ioDpKnaAcct.getAcctno(), true);
		if((E_AMNTCD.CR==amntcd&&CommUtil.compare(tranam, BigDecimal.ZERO)>0)||E_AMNTCD.DR==amntcd&&CommUtil.compare(tranam, BigDecimal.ZERO)<0){
			
			cplAcin1.setCutmam(cplAcin1.getCutmam().add(acmlbl1));//调增
		}else{
			cplAcin1.setCutmam(cplAcin1.getCutmam().subtract(acmlbl1));//调减
		}
		//更新积数
		SysUtil.getInstance(IoDpSrvQryTableInfo.class).updateKnbAcinOdb1(cplAcin1);
		
	}
	
	/**
	 * 
	 * @Auther renjinghua
	 *		<p>
	 *  	<li>2017年1月12日-下午8:07:28</li>
	 * 		<li>功能说明： 调账时处理调整利息</li>
	 * 		<p>
	 *
	 * @param ioDpKnaAcct 活期账户表信息
	 * @param knsStrk1  隔日冲正登记簿
	 * @param tranam 冲正金额
	 * @param prtrdt 原交易日期
	 * @param amntcd 借贷方向
	 * @return 利息调整天数
	 */
	private int prcAdjustInst(IoDpKnaAcct ioDpKnaAcct, KnsStrkInput knsStrk, BigDecimal tranam, String prtrdt, E_AMNTCD amntcd) {
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		int lcDays = 0; //调整积数天数
		
		//查询计息定义信息，获取上次结息日期
		String acctno = ioDpKnaAcct.getAcctno();
		bizlog.debug("========================"+CommTools.getBaseRunEnvs().getBusi_org_id());
		IoDpKnbAcin cplDpKnbAcin = SysUtil.getInstance(IoDpSrvQryTableInfo.class).getKnbAcinOdb1(acctno, true);
		String lcindt = cplDpKnbAcin.getLcindt(); // 上次结息日
		
		//如果冲正调账日期与原交易日期之间存在结息记录，则取上次结息时利率，调整利息，如果调减利息补足扣减，则报错返回相差金额
		if(CommUtil.isNotNull(lcindt) && CommUtil.compare(prtrdt, lcindt) < 0 && CommUtil.compare(trandt, lcindt) > 0) {
			// 查询上次结息日结息记录
			IoDpKnbPidl cplDpKnbPidl = DpAcinDao.selknbPidlByAcctnoPyindt(acctno, lcindt, false);
			
			// 如果存在结息记录则取结息利率进行利息调整
			if(CommUtil.isNotNull(cplDpKnbPidl)){
				lcDays = DateTools2.calDays(prtrdt, lcindt, 0, 0); //计算调整利息天数差
				//计算调整利息对应积数,并根据上次结息利率计算调整利息
				BigDecimal bigChangAcmlbl = tranam.abs().multiply(new BigDecimal(lcDays)); //积数
				bizlog.debug("=================调整利息对应积数为：" + bigChangAcmlbl);
				//计算调整利息
				BigDecimal bigAdjInst = SysUtil.getInstance(IoSrvPbInterestRate.class).countInteresRateByBase(cplDpKnbPidl.getCuusin(), bigChangAcmlbl);
				bigAdjInst = BusiTools.roundByCurrency(knsStrk.getCrcycd(), bigAdjInst, null);
				
				bizlog.debug("=================调整利息为：" + bigAdjInst +",借贷方向：" + amntcd + ",红蓝字：" + knsStrk.getRebuwo());
				
				
				//调整利息不等于0 进行记账
				if(CommUtil.compare(bigAdjInst, BigDecimal.ZERO) != 0){
					
					knsStrk.setAdinst(bigAdjInst.abs()); //登记调整利息
					
					if((E_AMNTCD.DR == amntcd && E_REBUWA.B == knsStrk.getRebuwo()) 
							|| (E_AMNTCD.CR == amntcd && E_REBUWA.R == knsStrk.getRebuwo())){
						//查询电子账户可用余额
						BigDecimal bigAcctbl = SysUtil.getInstance(DpAcctSvcType.class)
								.getAcctaAvaBal(ioDpKnaAcct.getCustac(), acctno, knsStrk.getCrcycd(),
										E_YES___.YES, E_YES___.NO);
						
						bizlog.debug("==================电子账户可用余额为：" + bigAcctbl);
						
						BigDecimal bigAdjam = tranam.abs().add(bigAdjInst.abs());
						
						if(CommUtil.compare(bigAdjam, bigAcctbl) > 0){
							BigDecimal bigDiffam = bigAdjam.subtract(bigAcctbl);
							bizlog.debug("=================调减利息时相差金额为：" + bigDiffam);
							throw InError.comm.E0003("调减利息时账户余额补足，还需补足金额："+ bigDiffam);
						}
						
						//调减利息时，调整利息记账为红字记账
						bigAdjInst = bigAdjInst.negate();
					}
					
					//调整利息记账，利息会计流水方向及D-借，客户账户记账方向为C-贷
					SaveDpAcctIn saveDpAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
					saveDpAcctIn.setAcctno(ioDpKnaAcct.getAcctno());
					saveDpAcctIn.setCustac(ioDpKnaAcct.getCustac());
					saveDpAcctIn.setCardno(knsStrk.getCustac());
					saveDpAcctIn.setCrcycd(knsStrk.getCrcycd());
					saveDpAcctIn.setTranam(bigAdjInst);
					saveDpAcctIn.setNegafg(E_YES___.YES);//支持红字记账
					SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(saveDpAcctIn);
					
					/** 登记支取利息会计流水，入账指令 **/
					IoAccounttingIntf cplIoAccounttingInrt = SysUtil
		                    .getInstance(IoAccounttingIntf.class);
		            cplIoAccounttingInrt.setCuacno(ioDpKnaAcct.getCustac()); //电子账号
		            cplIoAccounttingInrt.setAcctno(acctno); //账号
		            cplIoAccounttingInrt.setProdcd(ioDpKnaAcct.getProdcd()); //产品编号
		            cplIoAccounttingInrt.setDtitcd(ioDpKnaAcct.getAcctcd()); //核算口径
		            cplIoAccounttingInrt.setCrcycd(knsStrk.getCrcycd()); //币种
		            cplIoAccounttingInrt.setTranam(bigAdjInst); //利息
		            cplIoAccounttingInrt.setAcctdt(trandt);// 应入账日期
		            cplIoAccounttingInrt.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		            cplIoAccounttingInrt.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		            cplIoAccounttingInrt.setAcctbr(ioDpKnaAcct.getBrchno()); //登记账户机构
		            cplIoAccounttingInrt.setAmntcd(E_AMNTCD.DR); //借方
		            cplIoAccounttingInrt.setAtowtp(E_ATOWTP.DP); //存款
		            cplIoAccounttingInrt.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型，账务
		            cplIoAccounttingInrt.setBltype(E_BLTYPE.PYIN); //余额属性利息支出
		            //登记交易信息，供总账解析
		            if(CommUtil.equals("1", KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",true).getParm_value1())){
		            	KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		            	para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%", "%",true);
		            	cplIoAccounttingInrt.setTranms(para.getParm_value1());//登记交易信息 20160701  结息           	
		            }              
		            SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
		            		cplIoAccounttingInrt);
				}
				
			}
		}
		return lcDays;
	}

	@Override
	public void dpIavccm(cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.DpIavccm.Input input) {

		//通过客户卡号获取电子账号
		IoCaKnaAcdc ioCaKnaAcdc =  SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb2(input.getInacno(), true);
		if(CommUtil.isNull(ioCaKnaAcdc)){
			throw InError.comm.E0003("账号不存在！");
		}
		String custac = ioCaKnaAcdc.getCustac();
		IoDpKnaAcct ioDpKnaAcct= SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);
		E_CUACST status = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);//查询电子账户状态信息
		if(E_CUACST.INACTIVE == status){
			throw InError.comm.E0003("电子账户为非活跃状态");
		}
		//客户账记账输入类字段赋值
		if(CommUtil.isNull(input.getInamcd())){
			
			throw InError.comm.E0003("该笔客户账对应借贷标志为空，请核查");
		}
		
		SaveDpAcctIn saveDpAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
		saveDpAcctIn.setAcctno(ioDpKnaAcct.getAcctno());
		saveDpAcctIn.setCustac(custac);
		saveDpAcctIn.setCardno(input.getInacno());
		saveDpAcctIn.setOpacna(input.getOtacna());
		saveDpAcctIn.setToacct(input.getOtacno());
		saveDpAcctIn.setTranam(input.getTranam());
		saveDpAcctIn.setCrcycd(input.getCrcycd());
		saveDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_ZR);
		saveDpAcctIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_ZR));
		saveDpAcctIn.setRemark(input.getSmrytx());
		//saveDpAcctIn.setSmryds(knsCmbk.getSmrytx());//摘要码
		//SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(saveDpAcctIn);
		//modify by sh 20170802 支持外调服务处理
		SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(saveDpAcctIn);
		//客户化休眠处理
		//SysUtil.getInstance(DpAcctSvcType.class).dealAcctStatAndSett(ioDpKnaAcct, status);
		SysUtil.getInstance(DpAcctSvcType.class).dealAcctStatAndSett(ioDpKnaAcct, status);
		
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
	}


	/**
	 * 新增利息调整记录
	 */
	@Override
	public void insKnsCbdlFixInfo(KnbCbdlFixInfo addIn) {
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		//判断负债账号合法性
		KnbCbdlDao.selectFirst_odb1(addIn.getAcctno(), true);
		
		KnbCbdlFix entity = SysUtil.getInstance(KnbCbdlFix.class);
		CommUtil.copyProperties(entity, addIn);
		entity.setFixseq(MsSeqUtil.genSeq("KNB_CBDL_FIX_NO",trandt));
		entity.setDealst(E_PROCST.SUSPEND);
		KnbCbdlFixDao.insert(entity);
	}

	/**
	 * 查询利息调整记录
	 */
	@Override
	public Options<KnbCbdlFixInfo> qryKnsCbdlFixInfo(cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.QryKnsCbdlFixInfo.Input input) {
		RunEnvsComm runEnves = CommTools.getBaseRunEnvs();
		String corpno = runEnves.getBusi_org_id();
		String cabrdt = input.getCabrdt();
		String acctno = input.getAcctno();
		String brchno = input.getBrchno();
		String acctcd = input.getAcctcd();
		String crcycd = input.getCrcycd();
		String prodcd = input.getProdcd();
		String adjttp = null;
		if(CommUtil.isNotNull(input.getAdjttp())) {
			adjttp = input.getAdjttp().getValue();
		}
		String dealst = null;
		if(CommUtil.isNotNull(input.getDealst())) {
			dealst = input.getDealst().getValue();
		}
		long start = (runEnves.getPage_start() - 1)*runEnves.getPage_size();
		long count = runEnves.getPage_size();
		long totlCount = runEnves.getTotal_count();
		
		Page<KnbCbdlFixInfo> page = DpAcinDao.selKnbCbdlFixInfo(corpno, cabrdt, acctno, brchno, acctcd, crcycd, prodcd, adjttp, dealst, start, count, totlCount, false);
		runEnves.setTotal_count(page.getRecordCount());
		Options<KnbCbdlFixInfo> info = new DefaultOptions<KnbCbdlFixInfo>();
		info.setValues(page.getRecords());
		
		return info;
	}
	/**
	 * 大额存单转让
	 */
	public void dealLaAmOderTranfer(
			final cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.DealLaAmOderTranfer.Input input,
			final cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.DealLaAmOderTranfer.Output output) {
		String oucuac = input.getOucuac();// 出让电子账号
		String incuac = input.getIncuac();// 受让电子账号
		String acctno = input.getAcctno();// 负债账号
		String oucuna = input.getOucuna();// 出让客户名
		String incuna = input.getIncuna();// 受让客户名
		BigDecimal tranam = input.getTranam();// 交易金额
		String prodcd = input.getProdcd();// 产品编号
		String prodtx = input.getProdtx();
		String transq = CommTools.getBaseRunEnvs().getTrxn_seq()
;// 交易流水
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String subsac = null;
		String Parm_code = "FXAC_TRANSFER";

		List<KnpParameter> knpPare = KnpParameterDao.selectAll_odb2(Parm_code, false);
		if (CommUtil.isNotNull(knpPare)) {
			for (KnpParameter KnpParameter1 : knpPare) {
				if (CommUtil.equals(prodcd, KnpParameter1.getParm_value1())) {
					// 受让电子账户信息
					IoCaKnaCust inKnaCust = ActoacDao.selKnaCust(incuac, true);

					KnaFxac tblKnaFxac = SysUtil.getInstance(KnaFxac.class);
					tblKnaFxac = ActoacDao.selKnaFxac(acctno, true);
					tranam = tblKnaFxac.getOnlnbl();
					
					//更新定期负债账户表
					ActoacDao.updKnaFxacByAcctno(input.getIncpno(), inKnaCust.getCustno(), inKnaCust.getCustna(), inKnaCust.getCustac(), acctno);
					
					//更新负债账号客户账号对照表
					//重新生成子户号
					subsac = BusiTools.genSubEAccountno();					
					ActoacDao.updKnaAccsByAcct(input.getIncpno(), incuac, subsac, acctno);
					// 登记转存记录
					KnbFxacTrfe knbFxacTrfe = SysUtil.getInstance(KnbFxacTrfe.class);
					knbFxacTrfe.setAcctno(acctno);
					knbFxacTrfe.setIncuac(incuac);
					knbFxacTrfe.setIncuna(incuna);
					knbFxacTrfe.setOucuac(oucuac);
					knbFxacTrfe.setOucuna(oucuna);
					knbFxacTrfe.setTranam(tranam);
					knbFxacTrfe.setTrandt(trandt);
					knbFxacTrfe.setTransq(transq);
					knbFxacTrfe.setProdcd(prodcd);
					knbFxacTrfe.setProdtx(prodtx);

					KnbFxacTrfeDao.insert(knbFxacTrfe);
				}
			}
		}
		output.setAcctno(acctno);
		output.setIncuac(incuac);
		output.setOucuac(oucuac);
		output.setTranam(tranam);
		output.setIncuna(incuna);
		output.setOucuna(oucuna);
	}

	/**
	 * 
	 * 登记对账单功能
	 */
    public void addStat( String cardno,  String maildr){
    	if(CommUtil.isNull(cardno)){
    	    
    	}
    	if(CommUtil.isNull(maildr)){
    	    
    	}
    	
    	// 查询电子账户信息
    	KnaAcct tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(KnaAcdcDao.selectOne_odb2(cardno, true).getCustac(), E_ACSETP.SA);
    	// 电子账户信息附表
    	KnaCuad tblKnaCuad = KnaCuadDao.selectOne_knaCuadOdx1(tblKnaAcct.getCustac(), false);
    	
    	KnbStat tblKnbStat = SysUtil.getInstance(KnbStat.class);
    	tblKnbStat.setUserid(tblKnaCuad.getCustno()); // 用户ID
    	tblKnbStat.setCustno(tblKnaAcct.getCustno()); // 客户号
    	tblKnbStat.setCardno(cardno); // 卡号
    	tblKnbStat.setAcctna(tblKnaAcct.getAcctna()); // 客户名称
     	tblKnbStat.setMaildr(maildr); // 邮箱地址
    	tblKnbStat.setBegndt(CommTools.getBaseRunEnvs().getTrxn_date()); // 账单开始日期 
    	tblKnbStat.setDealst(E_PROCST.SUSPEND); // 处理状态--待处理
    	tblKnbStat.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); // 交易日期
    	tblKnbStat.setFilesq(CommTools.getBaseRunEnvs().getTrxn_seq()
); // 文件批次号
    	tblKnbStat.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq()
); // 交易流水号
    	
    	KnbStatDao.insert(tblKnbStat);
    }

	public cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaOpSubOut addJFSubAcct( final cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaOpSubIn pssbin){
		
		String custna = pssbin.getCustna();
		String crcycd = pssbin.getCrcycd();
		String custac = pssbin.getCustac();
		String custid = pssbin.getCustid();
		String acctid = pssbin.getAcctid();
		
		//建立POS品牌子账户信息
		KnaAcct acct = SysUtil.getInstance(KnaAcct.class);
		String acctno = BusiTools.getAcctno();
		acct.setAcctno(acctno);
		acct.setAcsetp(E_ACSETP.SA);
		acct.setAcctna(custna);
		acct.setAcctcd("");
		acct.setAcctst(E_DPACST.NORMAL);
		acct.setAccttp(E_YES___.YES); //是否结算户
		acct.setBkmony(BigDecimal.ZERO); //备用金额
		acct.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch()); //机构
		acct.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id()); //法人
		acct.setCrcycd(crcycd); //币种
		acct.setCustac(custac); //电子账户
		acct.setCustno(custid); //客户号
		acct.setOnlnbl(BigDecimal.ZERO); //账户余额 调用存入服务时写入
		acct.setOpendt(CommTools.getBaseRunEnvs().getTrxn_date()); //开户日期
		acct.setOpensq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //开户流水
		acct.setProdcd(acctid); //产品编号
		acct.setSleptg(E_YES___.NO); //形态转移标志
		acct.setSpectp(E_SPECTP.PERSON_BASE); //账户性质
		acct.setIsdrft(E_YES___.YES); //是否允许透支
		acct.setLstrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		acct.setLastbl(BigDecimal.ZERO);//新开户上日账户余额舒为0， add by xieqq -20170624
		acct.setCsextg(E_CSEXTG.NONE);
		acct.setBgindt(CommTools.getBaseRunEnvs().getTrxn_date());
		acct.setOpmony(BigDecimal.ZERO);
		acct.setAcctcd("A_ 220209");//核算代码暂时设成默认值
		KnaAcctDao.insert(acct);
		
		//子户计息表信息
		KnbAcin tblKnbAcin = SysUtil.getInstance(KnbAcin.class);
		
		tblKnbAcin.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		tblKnbAcin.setAcctno(acctno);
		tblKnbAcin.setProdcd(acctid);
		tblKnbAcin.setPddpfg(E_FCFLAG.CURRENT);
		tblKnbAcin.setDetlfg(E_YES___.NO);
		tblKnbAcin.setIntrtp(E_INTRTP.ZHENGGLX);
		tblKnbAcin.setCrcycd("CNY");
		tblKnbAcin.setInbefg(E_INBEFG.NOINBE);
		tblKnbAcin.setTxbefg(E_YES___.NO);
		tblKnbAcin.setTxbebs(E_INBEBS.REALSTAD);
		tblKnbAcin.setHutxfg(E_CAINRD.QE);
		tblKnbAcin.setInammd(E_IBAMMD.ACCT);
		tblKnbAcin.setTxbefr("1QA21E");
		tblKnbAcin.setReprwy(E_REPRWY.ALL);
		tblKnbAcin.setIntrcd("010000001");
		tblKnbAcin.setTaxecd("01");
		tblKnbAcin.setInprwy(E_IRRTTP.CK);
		tblKnbAcin.setIntrdt(E_INTRDT.OPEN);
		tblKnbAcin.setNxindt(CommTools.getBaseRunEnvs().getTrxn_date());
		tblKnbAcin.setNcindt(CommTools.getBaseRunEnvs().getTrxn_date());
		tblKnbAcin.setOpendt(CommTools.getBaseRunEnvs().getTrxn_date());
		tblKnbAcin.setBgindt(CommTools.getBaseRunEnvs().getTrxn_date());
		tblKnbAcin.setPlanin(BigDecimal.ZERO);
		tblKnbAcin.setLastdt(CommTools.getBaseRunEnvs().getTrxn_date());
		tblKnbAcin.setPlblam(BigDecimal.ZERO);
		tblKnbAcin.setNxdtin(BigDecimal.ZERO);
		tblKnbAcin.setMustin(BigDecimal.ZERO);
		tblKnbAcin.setLsinop(E_INDLTP.CAIN);
		tblKnbAcin.setIndtds(0);
		tblKnbAcin.setEvrgbl(BigDecimal.ZERO);
		tblKnbAcin.setCutmin(BigDecimal.ZERO);
		tblKnbAcin.setCutmis(BigDecimal.ZERO);
		tblKnbAcin.setCutmam(BigDecimal.ZERO);
		tblKnbAcin.setAmamfy(BigDecimal.ZERO);
		tblKnbAcin.setLaamdt(CommTools.getBaseRunEnvs().getTrxn_date());
		tblKnbAcin.setLyamam(BigDecimal.ZERO);
		tblKnbAcin.setDiffin(BigDecimal.ZERO);
		tblKnbAcin.setDiffct(BigDecimal.ZERO);
		tblKnbAcin.setLsinsq("0");
		tblKnbAcin.setIncdtp(E_IRCDTP.Reference);
		tblKnbAcin.setIrwpdy(0);
		
		
		KnbAcinDao.insert(tblKnbAcin);
		
		//返回信息
		IoCaOpSubOut opSubRlt = SysUtil.getInstance(IoCaOpSubOut.class);
		opSubRlt.setAcctno(acctno);
		
		return opSubRlt;
	}

    /**
     * 
     * @Description: 获取负债子账号
     * @param custac
     *            电子账户
     * @param acctno
     *            子账号
     * @author xuxiaoli
     * @date 2019/09/11
     */
    @Override
    public IoDpKnaAcct getKnaAcctSub(String custac, String acctno) {
        KnaAcct knaAcct = ActoacDao.selKnaAcct(acctno, false);
        if (knaAcct == null || !StringUtil.equals(custac, knaAcct.getCustac())
            || knaAcct.getAcctst() == E_DPACST.CLOSE) {
            throw InError.comm.E0003("电子账号子账号[" + acctno + "]不存在或不可用");
        }
        IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(IoDpKnaAcct.class);
        CommUtil.copyProperties(cplKnaAcct, knaAcct);
        return cplKnaAcct;
    }

}
