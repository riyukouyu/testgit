package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.acct.DpCloseAcctno;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.dayend.DpDayEndInt;
import cn.sunline.ltts.busi.dp.domain.DpAcctOnlnblEntity;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProd;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.AcmatuData;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbSmsSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.cf.IoCuTable.IoCifCustAccs;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseIN;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubSqrd;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STATUS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRSVTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRINWY;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;


/**
 * 存款产品到期处理
 * 存款产品到期处理，包括定期和活期
 * 
 */

public class acmatuDataProcessor extends
        AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Acmatu.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Acmatu.Property, cn.sunline.ltts.busi.dp.type.DpDayEndType.AcmatuData> {

    private static BizLog log = BizLogUtil.getBizLog(acmatuDataProcessor.class);

    //private static String lstrdt = null;
    //private static String bflsdt = null;
    /**
     * 批次数据项处理逻辑。
     * 
     * @param job 批次作业ID
     * @param index 批次作业第几笔数据(从1开始)
     * @param dataItem 批次数据项
     * @param input 批量交易输入接口
     * @param property 批量交易属性接口
     */
    @Override
    public void process(String jobId, int index, cn.sunline.ltts.busi.dp.type.DpDayEndType.AcmatuData dataItem,
            cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Acmatu.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Acmatu.Property property) {
        //			String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
        //			CommTools.getBaseRunEnvs().setBusi_org_id(dataItem.getCorpno());

        if (dataItem.getIsdetl() == E_YES___.YES) { //明细汇总的明细记录
            log.debug("************ 负债账号:[%s],明细序号：[%s],到期日期:[%s]************", dataItem.getAcctno(), dataItem.getDetlsq(), dataItem.getMatudt());
            prcDetl(dataItem);

            sendMail(dataItem.getCustac(), dataItem.getTrdpfg());

        } else { //非明细记录-普通定期，明细汇总定期的负债账户，活期智能存款
            log.debug("************ 转存账号:[%s],到期日期:[%s],定活标志:[%s]*************", dataItem.getAcctno(), dataItem.getMatudt(), dataItem.getPddpfg());

            if (dataItem.getDebttp() == E_DEBTTP.DP2404) { //个人智能活期存款
                prcCurr(dataItem);

            } else { //个人定期

                prcFxac(dataItem);

                sendMail(dataItem.getCustac(), dataItem.getTrdpfg());

            }
        }

        SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), null);
        //			CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);
        sendCmqToApp(dataItem.getCustac());
    }

    /*
     * 个人活期
     */
    protected static void prcCurr(AcmatuData item) {

         MsSystemSeq.getTrxnSeq(); //产生一笔新流水

        KnaAcct acct = CapitalTransDeal.getSettKnaAcctSub(item.getCustac(), E_ACSETP.SA);

        KnaAcct curr = SysUtil.getInstance(KnaAcct.class);

        CommUtil.copyProperties(curr, item);

        KnbAcin acin = KnbAcinDao.selectOne_odb1(curr.getAcctno(), true);

        IoDpCloseIN clsin = SysUtil.getInstance(IoDpCloseIN.class);

        clsin.setCardno("");
        clsin.setToacct(acct.getAcctno());
        clsin.setTobrch(acct.getBrchno());
        clsin.setToname(acct.getAcctna());
        clsin.setSmrycd(BusinessConstants.SUMMARY_TZ);

        if (item.getTrdpfg() == E_YES___.NO) { //转存标志不允许转存

            InterestAndIntertax cplint = DpCloseAcctno.prcCurrInterest(curr, clsin);
            BigDecimal onlnbl = DpCloseAcctno.prcCurrOnbal(curr, clsin);
            prcPostAcct(acct, item.getAcctno(), onlnbl.add(cplint.getInstam()), cplint.getIntxam(), BusinessConstants.SUMMARY_TZ);
        } else if (item.getTrdpfg() == E_YES___.YES) { //允许转存
            //销户转产品
            int dumpum = item.getDumpnm(); //转存次数
            int undpum = item.getUndump(); //已转存次数
            if (dumpum != 0 && undpum >= dumpum) { //转存次数达到最大，不能转存
                //子账户销户
                InterestAndIntertax cplint = DpCloseAcctno.prcCurrInterest(curr, clsin);
                BigDecimal onlnbl = DpCloseAcctno.prcCurrOnbal(curr, clsin);
                prcPostAcct(acct, item.getAcctno(), onlnbl.add(cplint.getInstam()), cplint.getIntxam(), BusinessConstants.SUMMARY_TZ);
            } else {
                if (item.getTrpdfg() == E_YES___.YES) { //可以更换产品号
                    throw DpModuleError.DpstComm.E9999("暂不支持转换产品");
                }
                if (item.getTrsvtp() == E_TRSVTP.BALC) { //本金转存 

                    clsin.setIsdetl(E_YES___.NO); //不处理明细记录

                    InterestAndIntertax cplint = DpCloseAcctno.prcCurrInterest(curr, clsin);
                    prcPostAcct(acct, item.getAcctno(), cplint.getInstam(), cplint.getIntxam(), BusinessConstants.SUMMARY_SX); //结算户利息存入
                    prcCurrAcct(curr); //修改负债账户的起息日，到期日等，修改明细的起息日、到期日等

                } else if (item.getTrsvtp() == E_TRSVTP.BL_IN) { //本息转存

                    clsin.setIsdetl(E_YES___.NO); //不处理明细记录

                    InterestAndIntertax cplint = DpCloseAcctno.prcCurrInterest(curr, clsin);

                    prcPostAcct(curr, acct.getAcctno(), cplint.getInstam(), cplint.getIntxam(), BusinessConstants.SUMMARY_TZ); //存款子账户存入利息
                    prcCurrAcct(curr); //修改负债账户的起息日，到期日等，修改明细的起息日、到期日等

                } else if (item.getTrsvtp() == E_TRSVTP.INOTHER) { //转入其他账号 产品属性中不包含该属性，因此不处理

                }

                prcIntr(curr.getBrchno(), curr.getAcctno(), curr.getDepttm(), curr.getDeptdy(), curr.getCrcycd(), item.getTrinwy(), acin);
            }
        }
    }

    /**
     * 个人定期处理
     * */
    protected static void prcFxac(AcmatuData item) {

         MsSystemSeq.getTrxnSeq(); //产生一笔新流水

        KnaAcct acct = CapitalTransDeal.getSettKnaAcctSub(item.getCustac(), E_ACSETP.SA);
        KnaFxac fxac = SysUtil.getInstance(KnaFxac.class);

        CommUtil.copyProperties(fxac, item);

        KnbAcin acin = KnbAcinDao.selectOne_odb1(fxac.getAcctno(), true);

        IoDpCloseIN clsin = SysUtil.getInstance(IoDpCloseIN.class);

        clsin.setCardno("");
        clsin.setToacct(acct.getAcctno());
        clsin.setTobrch(acct.getBrchno());
        clsin.setToname(acct.getAcctna());
        clsin.setSmrycd(BusinessConstants.SUMMARY_TZ);

        BigDecimal credbal = BigDecimal.ZERO;

        //1.定期处理
        if (item.getTrdpfg() != E_YES___.YES) { //转存标志不允许转存

            DrawDpAcctOut drawDpAcctOut = DpCloseAcctno.prcClsFxacAcct(fxac, acct.getAcctno(), acin.getDetlfg());
            credbal = fxac.getOnlnbl().add(drawDpAcctOut.getInstam());

            prcPostAcct(acct, item.getAcctno(), credbal, drawDpAcctOut.getIntxam(), BusinessConstants.SUMMARY_TZ);
        } else if (item.getTrdpfg() == E_YES___.YES) { //允许转存
            //销户转产品
            int dumpum = item.getDumpnm(); //转存次数
            int undpum = item.getUndump(); //已转存次数
            if (dumpum != 0 && undpum >= dumpum) { //转存次数达到最大，不能转存
                //子账户销户
                DrawDpAcctOut drawDpAcctOut = DpCloseAcctno.prcClsFxacAcct(fxac, acct.getAcctno(), acin.getDetlfg());
                credbal = fxac.getOnlnbl().add(drawDpAcctOut.getInstam());

                prcPostAcct(acct, item.getAcctno(), credbal, drawDpAcctOut.getIntxam(), BusinessConstants.SUMMARY_TZ);
            } else {
                if (item.getTrpdfg() == E_YES___.YES) { //可以更换产品号
                    throw DpModuleError.DpstComm.E9999("暂不支持转换产品");
                }
                if (item.getTrsvtp() == E_TRSVTP.BALC) { //本金转存 
                    BigDecimal interest = BigDecimal.ZERO;
                    BigDecimal intxam = BigDecimal.ZERO;

                    if (acin.getDetlfg() == E_YES___.NO) { //普通定期

                        InterestAndIntertax cplint = DpCloseAcctno.prcFxacInterest(fxac, clsin, acin);

                        interest = cplint.getInstam();
                        intxam = cplint.getIntxam();
                    } else if (acin.getDetlfg() == E_YES___.YES) {
                        //明细汇总负债子账号的资金在明细记录中，负债账户只需要修改转存信息即可
                        interest = BigDecimal.ZERO;
                    } else {
                        throw DpModuleError.DpstComm.E9999("是否明细汇总标志错误");
                    }
                    prcPostAcct(acct, item.getAcctno(), interest, intxam, BusinessConstants.SUMMARY_SX); //结算户利息存入
                    prcFxacAcct(fxac); //修改负债账户的起息日，到期日等，修改明细的起息日、到期日等

                } else if (item.getTrsvtp() == E_TRSVTP.BL_IN) { //本息转存

                    BigDecimal interest = BigDecimal.ZERO;
                    if (acin.getDetlfg() == E_YES___.NO) { //普通定期
                        //TODO 利息税暂做处理，客户张不体现利息税扣除明细
                        interest = DpCloseAcctno.prcFxacInterest(fxac, clsin, acin).getDiffam();
                    } else if (acin.getDetlfg() == E_YES___.YES) {
                        //明细汇总负债子账号的资金在明细记录中，负债账户只需要修改转存信息即可
                        interest = BigDecimal.ZERO;
                    } else {
                        throw DpModuleError.DpstComm.E9999("是否明细汇总标志错误");
                    }

                    prcPostFxac(fxac, item.getAcctno(), interest); //存款子账户存入利息
                    prcFxacAcct(fxac); //修改负债账户的起息日，到期日等，修改明细的起息日、到期日等
                } else if (item.getTrsvtp() == E_TRSVTP.INOTHER) { //转入其他账号 产品属性中不包含该属性，因此不处理
                    throw DpModuleError.DpstComm.E9999("转存方式暂不支持转入其他账号");
                }

                prcIntr(fxac.getBrchno(), fxac.getAcctno(), fxac.getDepttm(), fxac.getDeptdy(), fxac.getCrcycd(), item.getTrinwy(), acin);
            }
        }

    }

    /*
     * 单个明细记录
     */
    protected static void prcDetl(AcmatuData item) {

        KnaFxac fxac = KnaFxacDao.selectOne_odb1(item.getAcctno(), true);
        if (fxac.getAcctst() == E_DPACST.CLOSE) { //已销户的账户不需处理
            return;
        }

         MsSystemSeq.getTrxnSeq(); //产生一笔新流水

        //			KnaAcct acct = CapitalTransDeal.getSettKnaAcctSub(fxac.getCustac(), E_ACSETP.SA); //获取对应的结算户

        if (item.getTrdpfg() == E_YES___.NO) {
            //在处理负债子账户时进行处理
        } else if (item.getTrdpfg() == E_YES___.YES) { //允许转存
            int dumpum = item.getDumpnm(); //转存次数
            int undpum = item.getUndump(); //已转存次数
            if (dumpum != 0 && undpum >= dumpum) { //转存次数达到最大，不能转存
                //在处理负债子账户时进行处理
            } else {
                if (item.getTrpdfg() == E_YES___.YES) { //可以更换产品号
                    throw DpModuleError.DpstComm.E9999("暂不支持转换产品");
                }
                if (item.getTrsvtp() == E_TRSVTP.BALC) { //本金转存 
                    prcDetlIntr(fxac, item); //获取利息并记账

                    //prcPostAcct(acct, item.getAcctno(), instam); //利息存入结算户
                    prcFxacDetl(fxac, item.getDetlsq()); //修改明细的起息日期等
                } else if (item.getTrsvtp() == E_TRSVTP.BL_IN) { //本息转存
                    prcDetlIntr(fxac, item); //获取利息、利息税并记账

                    //prcPostFxac(fxac, item.getAcctno(), instam); //利息存入存款子账户
                    prcFxacDetl(fxac, item.getDetlsq()); //修改明细的起息日期等
                } else if (item.getTrsvtp() == E_TRSVTP.INOTHER) { //转入其他账号 产品属性中不包含该属性，因此不处理
                    throw DpModuleError.DpstComm.E9999("转存方式暂不支持转入其他账号");
                }
            }
        }

    }

    /*
     * 处理明细记录的结息
     */
    public static BigDecimal prcDetlIntr(KnaFxac fxac, AcmatuData item) {
        DpAcctOnlnblEntity entity = SysUtil.getInstance(DpAcctOnlnblEntity.class);

        entity.setTermcd(fxac.getDepttm());
        entity.setDetlfg(E_YES___.YES);
        entity.setDetlsq(item.getDetlsq());
        entity.setOpbrch(fxac.getBrchno());
        entity.setRemark("到期转存结息处理");
        entity.setInterest(BigDecimal.ZERO);
        entity.setSigle(E_YES___.YES);

        DpAcctProc.prcFxacDetl(fxac.getAcctno(), entity, item.getOnlnbl());
        //利息、利息税记账
        DpDayEndInt.prcDrawInstPay(fxac.getAcctno(), fxac.getCrcycd(), CommTools.getBaseRunEnvs().getTrxn_date());

        //BigDecimal instam = entity.getInterest();

        //prcAccount(fxac, instam);

        return BigDecimal.ZERO;
    }

    /***
     * @Title: prcCurrAcct
     * @Description: 修改活期账户表数据
     * @author zhangan
     * @date 2016年7月20日 下午4:30:32
     * @version V2.3.0
     */
    public static void prcCurrAcct(KnaAcct curr) {
        String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //当前日期
        String timetm = DateTools2.getCurrentTimestamp();
        String matudt = "";
        E_TERMCD termcd = curr.getDepttm(); //存期
        long deptdy = curr.getDeptdy(); //存期天数

        if (termcd.getValue().startsWith("9")) {
            matudt = DateTools2.dateAdd (ConvertUtil.toInteger(deptdy), trandt);
        } else {
            matudt = DateTools2.calDateByTerm(trandt, termcd);
        }
        //修改为新的起息日期和到期日期
        DpDayEndDao.updKnaAcct(trandt, matudt, CommTools.getBaseRunEnvs().getMain_trxn_seq(), curr.getAcctno(), curr.getCorpno(), timetm);
        //修改存入控制表
        DpDayEndDao.updKnaSave(curr.getAcctno(), curr.getCorpno(), timetm);
        //修改支取控制表
        DpDayEndDao.updKnaDraw(curr.getAcctno(), curr.getCorpno(), timetm);
        //修改到期控制表
        DpDayEndDao.updKnaAcctMatu(curr.getAcctno(), curr.getCorpno(), timetm);
    }

    /***
     * @Title: prcFxacAcct
     * @Description: 修改定期账户表数据
     * @author zhangan
     * @date 2016年7月20日 下午4:30:32
     * @version V2.3.0
     */
    public static void prcFxacAcct(KnaFxac fxac) {

        String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //当前日期
        String timetm = DateTools2.getCurrentTimestamp();
        String matudt = "";

        E_TERMCD termcd = fxac.getDepttm(); //存期
        long deptdy = fxac.getDeptdy(); //存期天数

        if (termcd.getValue().startsWith("9")) {
            matudt = DateTools2.dateAdd (ConvertUtil.toInteger(deptdy), trandt);
        } else {
            matudt = DateTools2.calDateByTerm(trandt, termcd);
        }

        //修改为新的起息日期和到期日期
        DpDayEndDao.updKnaFxac(trandt, matudt, CommTools.getBaseRunEnvs().getMain_trxn_seq(), fxac.getAcctno(), fxac.getCorpno(), timetm);
        //修改存入控制表
        DpDayEndDao.updKnaFxsv(fxac.getAcctno(), fxac.getCorpno(), timetm);
        //修改支取控制表
        DpDayEndDao.updKnaFxdr(fxac.getAcctno(), fxac.getCorpno(), timetm);
        //修改到期控制表
        DpDayEndDao.updKnaFxacMatu(fxac.getAcctno(), fxac.getCorpno(), timetm);
    }

    /**
     * @Title: prcFxacDetl
     * @Description: 修改明细记录的起息日到期日等
     * @param fxac
     * @param detlsq
     * @author zhangan
     * @date 2016年12月8日 下午3:47:19
     * @version V2.3.0
     */
    public static void prcFxacDetl(KnaFxac fxac, long detlsq) {

        String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //当前日期
        String timetm = DateTools2.getCurrentTimestamp();
        String matudt = "";

        E_TERMCD termcd = fxac.getDepttm(); //存期
        long deptdy = fxac.getDeptdy(); //存期天数

        if (termcd.getValue().startsWith("9")) {
            matudt = DateTools2.dateAdd (ConvertUtil.toInteger(deptdy), trandt);
        } else {
            matudt = DateTools2.calDateByTerm(trandt, termcd);
        }

        //修改明细的起息日期和到期日期，开户日期，开户流水，上次交易日期，上次交易流水
        DpDayEndDao.updKnaFxacDetl(trandt, CommTools.getBaseRunEnvs().getMain_trxn_seq(), matudt, fxac.getAcctno(), fxac.getCorpno(), detlsq, timetm);
    }

    /***
     * @Title: prcCred
     * @Description: 贷记存款客户账
     * @author zhangan
     * @date 2016年7月20日 下午7:45:46
     * @version V2.3.0
     */
    public static void prcCred(KnaFxac fxac, BigDecimal tranam) {

        String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //当前日期
        if (CommUtil.compare(tranam, BigDecimal.ZERO) != 0) {
            /* 应入账日期 */
            IoAccounttingIntf account = SysUtil.getInstance(IoAccounttingIntf.class);

            account.setCuacno(fxac.getCustac());
            account.setAcctno(fxac.getAcctno());
            //				account.setAcseno(acct.get());
            account.setProdcd(fxac.getProdcd());
            account.setDtitcd(fxac.getAcctcd());
            account.setCrcycd(BusiTools.getDefineCurrency());
            account.setTranam(tranam);
            account.setAcctdt(trandt); // 应入账日期
            account.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
            account.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
            account.setTrandt(trandt);
            account.setAcctbr(fxac.getBrchno());
            account.setAmntcd(E_AMNTCD.CR); // 借方
            account.setAtowtp(E_ATOWTP.DP); // 存款
            account.setTrsqtp(E_ATSQTP.ACCOUNT); // 账务流水
            account.setBltype(E_BLTYPE.BALANCE); // 余额属性：本金科目
            // 登记交易信息，供总账解析
            if (CommUtil.equals(
                    "1",
                    KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
                            true).getParm_value1())) {
                KnpParameter para = SysUtil.getInstance(KnpParameter.class);
                para = KnpParameterDao.selectOne_odb1("GlAnalysis", "1010000", "%",
                        "%", true);
                account.setTranms(para.getParm_value1());// 登记交易信息 结息
            }
            SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(account);
        }
    }

    /*
     * 记一笔利息支出
     */
    public static void prcAccount(KnaFxac fxac, BigDecimal instam) {

        if (CommUtil.compare(instam, BigDecimal.ZERO) > 0) {
            String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //当前日期

            log.debug("登记利息支取会计流水>>>>>>>>>>>>>>>>>" + instam);

            IoAccounttingIntf cplIoAccounttingInrt = SysUtil.getInstance(IoAccounttingIntf.class);
            cplIoAccounttingInrt.setCuacno(fxac.getCustac()); //电子账号
            cplIoAccounttingInrt.setAcctno(fxac.getAcctno()); //账号
            //  cplIoAccounttingInrt.setAcseno(acseno); //子户号
            cplIoAccounttingInrt.setProdcd(fxac.getProdcd()); //产品编号
            cplIoAccounttingInrt.setDtitcd(fxac.getAcctcd()); //核算口径
            cplIoAccounttingInrt.setCrcycd(fxac.getCrcycd()); //币种
            cplIoAccounttingInrt.setTranam(instam); //利息
            cplIoAccounttingInrt.setAcctdt(trandt);// 应入账日期
            cplIoAccounttingInrt.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
            cplIoAccounttingInrt.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
            cplIoAccounttingInrt.setAcctbr(fxac.getBrchno()); //登记账户机构
            cplIoAccounttingInrt.setAmntcd(E_AMNTCD.DR); //借方
            cplIoAccounttingInrt.setAtowtp(E_ATOWTP.DP); //存款
            cplIoAccounttingInrt.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型，账务
            cplIoAccounttingInrt.setBltype(E_BLTYPE.PYIN); //余额属性利息支出
            //登记交易信息，供总账解析
            if (CommUtil.equals("1", KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%", true).getParm_value1())) {
                KnpParameter para = SysUtil.getInstance(KnpParameter.class);
                para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%", "%", true);
                cplIoAccounttingInrt.setTranms(para.getParm_value1());//登记交易信息 20160701  结息           	
            }

            SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingInrt);
        }
    }

    /***
     * @Title: prcIntr
     * @Description: 处理利率表和利息定义表
     * @param fxac
     * @param trinwy
     * @author zhangan
     * @date 2016年7月20日 下午8:26:21
     * @version V2.3.0
     */
    public static void prcIntr(String brchno, String acctno, E_TERMCD depttm, long deptdy, String crcycd, E_TRINWY trinwy, KnbAcin acin) {

        String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //当前日期

        IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);

        acin = KnbAcinDao.selectOne_odb1(acin.getAcctno(), false);

        if (trinwy == E_TRINWY.NOAD) { //不调整利率

        } else if (trinwy == E_TRINWY.RPAD) {

            KubInrt inrt = KubInrtDao.selectOne_odb1(acctno, true);

            //暂时到期重新获取利率不要考虑利率优惠 start
            inrt.setFavort(BigDecimal.ZERO);
            //end  mod by leipeng 20170209

            IoPbIntrPublicEntity entity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
            entity.setCrcycd(crcycd);
            entity.setDepttm(depttm);
            entity.setIntrcd(inrt.getIntrcd());
            entity.setIncdtp(inrt.getIncdtp());
            entity.setTrandt(trandt);
            entity.setIntrwy(inrt.getIntrwy());
            entity.setBgindt(trandt);
            entity.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
            entity.setBrchno(brchno);

            entity.setLevety(acin.getLevety());
            if (acin.getIntrdt() == E_INTRDT.OPEN) {
                entity.setTrandt(trandt);
                entity.setTrantm("999999");
            }
            pbpub.countInteresRate(entity);

            BigDecimal tmpBase = entity.getBaseir();
            BigDecimal tmpIntr = entity.getIntrvl();
            //新的执行利率
            BigDecimal cuusin = tmpIntr.add(inrt.getFavovl()).add(tmpIntr.multiply(inrt.getFavort().divide(BigDecimal.valueOf(100))));

            //mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
            //利率的最大范围值
            BigDecimal intrvlmax = entity.getBaseir().multiply(BigDecimal.ONE.add(entity.getFlmxsc().divide(BigDecimal.valueOf(100))));
            //利率的最小范围值
            BigDecimal intrvlmin = entity.getBaseir().multiply(BigDecimal.ONE.add(entity.getFlmnsc().divide(BigDecimal.valueOf(100))));

            if (CommUtil.compare(cuusin, intrvlmin) < 0) {
                cuusin = intrvlmin;
            } else if (CommUtil.compare(cuusin, intrvlmax) > 0) {
                cuusin = intrvlmax;
            }
            //mod by leipeng   优惠后判断时候超出基础浮动范围20170220  end--
            inrt.setBsintr(tmpBase);
            inrt.setInflpo(entity.getFlirvl());
            inrt.setInflrt(entity.getFlirrt());
            inrt.setCuusin(cuusin);

            KubInrtDao.updateOne_odb1(inrt);

        } else {
            throw DpModuleError.DpstComm.E9999("转出利率调整方式不支持");
        }

        acin.setOpendt(trandt);
        //acin.setLaindt(trandt);
        //acin.setNxindt(trandt);
        //存在结息频率的产品，才更新下次结息日  update by renjh in 20170811
        if (CommUtil.isNotNull(acin.getTxbefr())) {
            acin.setLcindt(trandt);
            String ncindt = DpPublic.getNextPeriod(acin.getNcindt(), CommTools.getBaseRunEnvs().getNext_date(), acin.getTxbefr());
            acin.setNcindt(ncindt);
        }

        acin.setBgindt(trandt);
        if (depttm.getValue().startsWith("9")) {
            acin.setEdindt(DateTools2.dateAdd (ConvertUtil.toInteger(deptdy), trandt));
        } else {
            acin.setEdindt(DateTools2.calDateByTerm(trandt, depttm));
        }
        acin.setPlanin(BigDecimal.ZERO);
        acin.setLastdt(trandt);
        acin.setLaamdt(trandt);
        acin.setPlblam(BigDecimal.ZERO);
        acin.setMustin(BigDecimal.ZERO);
        acin.setEvrgbl(BigDecimal.ZERO);
        acin.setCutmam(BigDecimal.ZERO);
        acin.setCutmin(BigDecimal.ZERO);
        acin.setCutmis(BigDecimal.ZERO);
        acin.setAmamfy(BigDecimal.ZERO);
        acin.setDiffct(BigDecimal.ZERO);
        acin.setDiffin(BigDecimal.ZERO);

        KnbAcinDao.updateOne_odb1(acin);

    }

    /***
     * @Title: prcPostAcct
     * @Description: 结算户存入记账服务
     * @param acct
     * @param toacct
     * @param tranam
     * @author zhangan
     * @date 2016年7月20日 下午4:29:15
     * @version V2.3.0
     */
    public static void prcPostAcct(KnaAcct acct, String toacct, BigDecimal tranam, BigDecimal intxam, String smrycd) {
        //取值产品名称
        String remark = "";
        String remark1 = "";
        KnaAcctProd tblAcctProd = DpAcctDao.selKnaAcctProdByAcctno(toacct, false);
        KnaFxacProd tblFxacProd = DpAcctDao.selKnaFxacProdByAcctno(toacct, false);

        if (CommUtil.isNotNull(tblAcctProd)) {
            remark = "活期-" + tblAcctProd.getObgaon();
            remark1 = tblAcctProd.getObgaon();
        } else if (CommUtil.isNotNull(tblFxacProd)) {
            remark = "定期-" + tblFxacProd.getObgaon();
            remark1 = tblFxacProd.getObgaon();
        }
        //利息入客户账
        if (CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {

            //取值产品名称
            /*
            String remark = "";
            String remark1 = "";
            KnaAcctProd tblAcctProd = DpAcctDao.selKnaAcctProdByAcctno(toacct, false);
            KnaFxacProd tblFxacProd = DpAcctDao.selKnaFxacProdByAcctno(toacct, false);

            if (CommUtil.isNotNull(tblAcctProd)) {
                remark = "活期-" + tblAcctProd.getObgaon();
                remark1 = tblAcctProd.getObgaon();
            } else if (CommUtil.isNotNull(tblFxacProd)) {
                remark = "定期-" + tblFxacProd.getObgaon();
                remark1 = tblFxacProd.getObgaon();
            }
            */
            SaveDpAcctIn cplSaveIn = SysUtil.getInstance(SaveDpAcctIn.class);

            //判断摘要码类型
            if (CommUtil.equals(BusinessConstants.SUMMARY_TZ, smrycd)) {
                cplSaveIn.setRemark(remark);
            } else if (CommUtil.equals(BusinessConstants.SUMMARY_SX, smrycd)) {
                cplSaveIn.setRemark(remark1 + "结息");
            }
            cplSaveIn.setSmrycd(smrycd);
            cplSaveIn.setSmryds(ApSmryTools.getText(smrycd));

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
            cplSaveIn.setToacct(toacct);
            cplSaveIn.setTranam(tranam);//利息（包含利息税）
            cplSaveIn.setIschck(E_YES___.NO);

            SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveIn);
        }
        //利息税从客户账支取
        DrawDpAcctIn drawin = SysUtil.getInstance(DrawDpAcctIn.class); //支取服务输入参数
        if (CommUtil.compare(intxam, BigDecimal.ZERO) > 0) {
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
            drawin.setSmrycd(BusinessConstants.SUMMARY_JS);//摘要码-缴税
            drawin.setRemark(remark);
            SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawin);

        }
    }

    /***
     * @Title: prcPostFxac
     * @Description: 定期子账户存入记账服务
     * @param fxac
     * @param toacct
     * @param tranam
     * @author zhangan
     * @date 2016年7月20日 下午4:29:15
     * @version V2.3.0
     */
    public static void prcPostFxac(KnaFxac fxac, String toacct, BigDecimal tranam) {

        if (CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {
            SaveDpAcctIn cplSaveIn = SysUtil.getInstance(SaveDpAcctIn.class);

            KnaFxacProd tblFxacProd = DpAcctDao.selKnaFxacProdByAcctno(fxac.getAcctno(), true);

            cplSaveIn.setAcctno(fxac.getAcctno());
            cplSaveIn.setAcseno(null);//
            cplSaveIn.setBankcd(null);
            cplSaveIn.setBankna(null);
            cplSaveIn.setCardno(null);
            cplSaveIn.setCrcycd(fxac.getCrcycd());
            cplSaveIn.setCustac(fxac.getCustac());
            cplSaveIn.setLinkno(null);
            cplSaveIn.setOpacna(fxac.getAcctna());
            cplSaveIn.setOpbrch(fxac.getBrchno());
            cplSaveIn.setRemark("定期-" + tblFxacProd.getObgaon());
            cplSaveIn.setSmrycd(BusinessConstants.SUMMARY_TZ);
            cplSaveIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_TZ));
            //				cplSaveIn.setSmryds("到期自动转存");
            cplSaveIn.setToacct(toacct);
            cplSaveIn.setTranam(tranam);
            cplSaveIn.setIschck(E_YES___.NO);

            SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveIn);
        }
    }

    // add 2016/12/27 songlw 智能存款转存短信发送
    public static void sendMail(String custac, E_YES___ trdpfg) {

        log.debug("<<===========custac[%s], trdpfg[%s]===============>>", custac, trdpfg);

        //不自动转存发送app站内信
        if (E_YES___.NO == trdpfg) {
            log.debug("<<============短信流水登记服务开始===============>>");
            IoCaKnaCust cplCaKnaCust = SysUtil.getInstance(IoCaSevQryTableInfo.class).
                    getKnaCustByCustacOdb1(custac, false);//根据电子账户获取电子账户表数据

            if (CommUtil.isNotNull(cplCaKnaCust)) {
                // 查询客户关联关系表
                /*
                IoCifCustAccs cplCifCustAccs = SysUtil.getInstance( IoCuSevQryTableInfo.class).cif_cust_accsByCustno(
                		cplCaKnaCust.getCustno(), false, E_STATUS.NORMAL);
                */
                // 短信流水登记
                IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
                /*
                if (CommUtil.isNotNull(cplCifCustAccs)) {
                	cplKubSqrd.setAppsid(cplCifCustAccs.getAppsid());// app推送ID
                }
                */
                cplKubSqrd.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());// 内部交易码
                cplKubSqrd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
                cplKubSqrd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());// 交易时间
                cplKubSqrd.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
                cplKubSqrd.setTmstmp(DateTools2.getCurrentTimestamp());// 时间戳
                cplKubSqrd.setPmvl01(custac);
                // 调用短信流水登记服务
                SysUtil.getInstance(IoPbSmsSvcType.class).pbTransqReg(cplKubSqrd);
            } else {
                log.error("电子账号不存在! ", CommTools.getBaseRunEnvs().getTrxn_date());
            }
            log.debug("<<============短信流水登记服务结束===============>>");
        }
    }

    /**
     * 获取数据遍历器。
     * 
     * @param input 批量交易输入接口
     * @param property 批量交易属性接口
     * @return 数据遍历器
     */
    @Override
    public BatchDataWalker<cn.sunline.ltts.busi.dp.type.DpDayEndType.AcmatuData> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Acmatu.Input input,
            cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Acmatu.Property property) {

        //CommTools.getBaseRunEnvs().setTrxn_date("20160406");

        String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
        //			lstrdt = CommTools.getBaseRunEnvs().getLast_date();
        //			bflsdt = DateTools2.getDateInfo().getBflsdt();
        Params params = new Params();
        params.add("acctst", E_DPACST.NORMAL);
        params.add("matudt", trandt);
        params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
        return new CursorBatchDataWalker<AcmatuData>(DpDayEndDao.namedsql_selAcmatuData, params);
    }
    
    public static void sendCmqToApp(String custac){
    	String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String trantm = BusiTools.getBusiRunEnvs().getTrantm();
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaCust cust = caqry.getKnaCustByCustacOdb1(custac, true);
//		IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(IoSrvCfPerson.class).getCifCustAccsByCustno(cust.getCustno(), E_STATUS.NORMAL, true);
		//消息推送至APP客户端
//		MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//		mri.setMtopic("Q0101005");
//		//mri.setTdcnno("R00");  //测试指定DCN
//		ToAppSendMsg toAppSendMsg = SysUtil.getInstance(ToAppSendMsg.class);
//		
//		// 消息内容
//		toAppSendMsg.setUserId(cplCifCustAccs.getCustid()); //用户ID
//		toAppSendMsg.setOutNoticeId("Q0101005"); //外部消息ID
//		toAppSendMsg.setNoticeTitle("资金变动"); //公告标题
//		toAppSendMsg.setContent("	您有一款存款产品已到期赎回.交易时间："+DateTools2.getMonth(DateTools2.covStringToDate(trandt))+
//				"月"+DateTools2.getDay(DateTools2.covStringToDate(trandt))+"日，"
//				+trantm.substring(0, 2)+ ":"+trantm.substring(2, 4)
//				+"请点击查看详情。"); //公告内容
//		toAppSendMsg.setSendtime(CommTools.getBaseRunEnvs().getTrxn_date()+BusiTools.getBusiRunEnvs().getTrantm()); //消息生成时间
//		toAppSendMsg.setTransType(E_APPTTP.CUACCH); //交易类型
//		toAppSendMsg.setTirggerSys(SysUtil.getSystemId()); //触发系统
//		toAppSendMsg.setClickType(E_CLIKTP.NO);   //点击动作类型
//		//toAppSendMsg.setClickValue(clickValue); //点击动作值
//		
//		mri.setMsgtyp("ApSmsType.ToAppSendMsg");
//		mri.setMsgobj(toAppSendMsg); 
//		AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
	}

}
