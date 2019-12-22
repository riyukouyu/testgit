package cn.sunline.ltts.busi.dp.acct;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.engine.sequence.SequenceManager;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.domain.DpAcctOnlnblEntity;
import cn.sunline.ltts.busi.dp.domain.DpSaveEntity;
import cn.sunline.ltts.busi.dp.namedsql.DpBaseProdDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPidl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPidlDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDetl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDetlDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxsv;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxsvDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBill;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SAVECT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AMNTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PLANFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_POSTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODCT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TIMEWY;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

/**
 * 定期共用类
 * 
 * @author cuijia
 * 
 */
public class DpKnaFxac {

    private static final BizLog bizlog = BizLogUtil.getBizLog(DpKnaAcct.class);

    /**
     * 存入检查方法
     */
    public static void validatePost(String acctno, BigDecimal tranam,
            E_FCFLAG fcflag, String prodcd, String crcycd,
            BaseEnumType.E_YES___ ngblfg) {
        // 产品属性检查
        validatePostProductProperty(acctno, tranam, fcflag, prodcd, crcycd);
        // 余额检查
        if (CommUtil.compare(tranam, BigDecimal.ZERO) < 0) {
            BigDecimal bal = BigDecimal.ZERO;

            bal = DpAcctProc.getAcctBal(acctno, fcflag);
            KnaFxsv kxsv = KnaFxsvDao.selectOne_odb1(acctno, false);
            if (CommUtil.isNull(kxsv)) {
                throw DpModuleError.DpstComm.BNAS0841();
            }
            if (ngblfg != E_YES___.YES
                    && CommUtil.compare(tranam.negate(), bal) > 0) {
                bizlog.debug("可用余额=================[" + bal + "]");
                throw DpModuleError.DpstComm.BNAS0841();
            }
        }
    }

    /**
     * 定期余额更新处理
     * 
     * @param entity
     * @param fcflag
     * @param strkfg
     */
    public static void updateDpAcctOnlnbl(DpAcctOnlnblEntity entity,
            E_FCFLAG fcflag, E_YES___ strkfg) {

        String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期
        String acctdt = trandt;// 应入账日期，默认为交易日期
        String acctno = entity.getAcctno();
        E_AMNTCD amntcd = entity.getAmntcd();
        BigDecimal tranam = entity.getTranam();
        BigDecimal onlnbl = BigDecimal.ZERO;
        BigDecimal lastbl = BigDecimal.ZERO;
        // 修改定期余额
        KnaFxac acct = KnaFxacDao.selectOneWithLock_odb1(acctno, true);

        // 冲正交易时，先冲正账户状态，再更新账户余额等信息
        if (E_YES___.YES == strkfg) {
            // 冲正交易，存入冲正后余额为0，暂不更新账户状态为销户
            // 如果冲正登记的原账户状态与当前账户状态不一致，则更新为原账户状态
            if (CommUtil.isNotNull(entity.getAcctst())
                    && entity.getAcctst() != acct.getAcctst()) {
                if (E_DPACST.CLOSE == acct.getAcctst()) {
                    acct.setClosdt(null);
                    acct.setClossq(null);
                }
                acct.setAcctst(entity.getAcctst());
            }
        }
        // ===============mdy by zhanga 休眠账户也需要支持转入=============
        // 如果负债账号状态不正常，返回错误信息
        /*
         * if(acct.getAcctst()!=E_DPACST.NORMAL){ throw
         * DpModuleError.DpstProd.E0007(acctno); }
         */
        if (acct.getAcctst() == E_DPACST.CLOSE) {
            throw DpModuleError.DpstProd.BNAS1130(acctno);
        }

        // 获取应入账日期
        // acctdt = ApDCN.getAccountDateOneDCN(acctno, trandt,
        // acct.getUpbldt());

        // 如果是当天第一笔交易则更新上日余额及余额更新时间
        if (CommUtil.compare(trandt, acct.getUpbldt()) > 0) {
            /* 过账:更新余额最新更新日期 */
            acct.setUpbldt(acctdt);
            bizlog.debug(">> 更新账户[%s] = 账户上日余额 = [%s]", acctno,
                    acct.getOnlnbl());
            acct.setLastbl(acct.getOnlnbl());
        }

        onlnbl = acct.getOnlnbl();
        lastbl = acct.getLastbl();

        // 判断借贷标志
        // 贷
        if (E_AMNTCD.CR == amntcd) {
            onlnbl = onlnbl.add(tranam);
            acct.setOnlnbl(onlnbl);
            // 日切后记上日账需要变动上日余额
            if (CommUtil.compare(trandt, DateTools2.getDateInfo().getSystdt()) < 0) {
                if (CommUtil.compare(trandt, acct.getUpbldt()) < 0) {
                    lastbl = lastbl.add(tranam);
                    acct.setLastbl(lastbl);
                }
            }
        } else if (E_AMNTCD.DR == amntcd) {
            onlnbl = onlnbl.subtract(tranam);
            acct.setOnlnbl(onlnbl);
            // 日切后记上日账需要变动上日余额
            if (CommUtil.compare(trandt, DateTools2.getDateInfo().getSystdt()) < 0) {
                if (CommUtil.compare(trandt, acct.getUpbldt()) < 0) {
                    lastbl = lastbl.subtract(tranam);
                    acct.setLastbl(lastbl);
                }
            }
        }

        // 如果最后一笔交易时间小于等于交易日期，需要更新最后交易日期和流水
        if (CommUtil.compare(acct.getLstrdt(), trandt) <= 0) {
            acct.setLstrdt(trandt);
            acct.setLstrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
        }

        /* 更新账户状态 add by renjinghua in 20150924 */
        // 传统定期支取后账户余额为0，则置账户状态为"2-关闭"，即销户
        bizlog.debug(">> 子账号[%s] = 冲正标志[%s] = 账户余额[%s] = 借贷方向[%s] = 明细标志[%s]", acctno, strkfg,
                acct.getOnlnbl(), amntcd, entity.getDetlfg());
        if (E_YES___.NO == strkfg) {
            // 正常交易
            if (E_AMNTCD.DR == amntcd && entity.getDetlfg() == E_YES___.NO
                    && CommUtil.compare(acct.getOnlnbl(), BigDecimal.ZERO) == 0) {
                // 正交易时需要先登记更新前状态，后面更新后冲正使用
                entity.setAcctst(acct.getAcctst());
                // 更新账户状态
                acct.setClosdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 销户日期
                acct.setClossq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 销户流水
                acct.setAcctst(E_DPACST.CLOSE); // 账户状态
            }
        } else {
            // 对私产品冲正后余额为0，则账户更新状态为销户
            String coprno = BusiTools.getCenterCorpno();// 省级法人代码
            // KupDppb tblDppb = KupDppbDao.selectOne_odb1(acct.getProdcd(),
            // true);
            KupDppb tblDppb = DpBaseProdDao.selKupDppb(coprno,
                    acct.getProdcd(), true);

            if (entity.getDetlfg() == E_YES___.NO
                    && CommUtil.compare(acct.getOnlnbl(), BigDecimal.ZERO) == 0
                    && tblDppb.getProdtp() == E_PRODCT.PRIV) {
                // 更新账户状态
                acct.setClosdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 销户日期
                acct.setClossq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 销户流水
                acct.setAcctst(E_DPACST.CLOSE); // 账户状态
            }
        }

        // 修改账户余额
        KnaFxacDao.updateOne_odb1(acct);

        // 定期附加产品信息表
        KnaFxacProd tblKnaFProd = KnaFxacProdDao.selectOneWithLock_odb1(acctno,
                true);

        Long detlsq = ConvertUtil.toLong(CommUtil.nvl(tblKnaFProd.getObgatw(), 0)) + 1;// 明细序号
        // 更新明细序号
        tblKnaFProd.setObgatw(Long.toString(detlsq));// 明细序号
        KnaFxacProdDao.updateOne_odb1(tblKnaFProd);

        // 判断是否为明细汇总,只有定期才有此属性
        KnaFxsv kxsv = KnaFxsvDao.selectOne_odb1(acctno, false);
        if (CommUtil.isNull(kxsv)) {
        	throw DpModuleError.DpstComm.BNAS0841();
        }

        /*
         * 定期自动转存 余额=（原余额+交易金额） 起息日为交易日期， 到期日为新的到期日
         */
        if (entity.getFxaufg() == E_YES___.YES) {
            // 更新KnaFxacDetl
            if (E_YES___.YES == kxsv.getDetlfg()) {
                KnaFxacDetl fxac_detl = KnaFxacDetlDao.selectOne_odb1(acctno,
                        detlsq, true);
                fxac_detl.setOnlnbl(fxac_detl.getOnlnbl().add(tranam));
                fxac_detl.setBgindt(trandt);
                fxac_detl.setMatudt(DateTools2.calDateByTerm(trandt,
                        acct.getDepttm()));
                KnaFxacDetlDao.updateOne_odb1(fxac_detl);
            } else {
                // 更新KnaFxac
                KnaFxac tblKnafxac = KnaFxacDao.selectOne_odb1(acctno, true);
                tblKnafxac.setOnlnbl(tblKnafxac.getOnlnbl().add(tranam));
                tblKnafxac.setBgindt(trandt);
                // 到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
                String termcd = acct.getDepttm().getValue();
                if (CommUtil.equals(termcd.substring(0, 1), "9")) {
                    tblKnafxac.setMatudt(DateTools2.dateAdd (tblKnafxac
                            .getDeptdy().intValue(), tblKnafxac.getBgindt()));
                } else {
                    tblKnafxac.setMatudt(DateTools2.calDateByTerm(trandt,
                            acct.getDepttm()));
                }
                KnaFxacDao.updateOne_odb1(tblKnafxac);
            }
        } else {
            if (E_YES___.YES == kxsv.getDetlfg()) {
                if (E_AMNTCD.CR == amntcd) { //add by sh 判断是存入时新增定期账户明细记录，支取时这里不做处理后续处理定期账户明细时更新
                    // 插入负债定期账户明细表
                    KnaFxacDetl fxac_detl = SysUtil.getInstance(KnaFxacDetl.class);
                    fxac_detl.setAcctno(acctno);
                    //fxac_detl.setDetlsq(Long.valueOf(SequenceManager.nextval("KnaFxacDetl_seq")));
                    fxac_detl.setDetlsq(Long.valueOf(CoreUtil.nextValue("KnaFxacDetl_seq")));
                    fxac_detl.setCrcycd(entity.getCrcycd());
                    fxac_detl.setCsextg(E_CSEXTG.EXCHANGE);
                    fxac_detl.setOpendt(trandt);
                    fxac_detl.setOpensq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
                    fxac_detl.setBgindt(trandt);

                    String termcd = acct.getDepttm().getValue();
                    if (CommUtil.equals(termcd.substring(0, 1), "9")) {
                        fxac_detl.setMatudt(DateTools2.dateAdd (acct
                                .getDeptdy().intValue(), fxac_detl.getBgindt()));
                    } else {
                        fxac_detl.setMatudt(DateTools2.calDateByTerm(trandt,
                                acct.getDepttm()));
                    }
                    fxac_detl.setOnlnbl(tranam);
                    fxac_detl.setAcctst(E_DPACST.NORMAL);

                    KnaFxacDetlDao.insert(fxac_detl);
                }
            }
        }

        entity.setDetlsq(detlsq);
        // 账户余额
        entity.setOnlnbl(onlnbl);
        // 返回开户机构
        entity.setOpenbr(acct.getBrchno());
        // 返回账户名称
        entity.setAcctna(acct.getAcctna());
        // 返回产品号
        entity.setProdcd(acct.getProdcd());
        // 返回核算代码
        entity.setDtitcd(acct.getAcctcd());
        // 返回存期
        entity.setTermcd(acct.getDepttm());
        // 返回存期天数
        entity.setDeptdy(acct.getDeptdy());
        // 返回账户所属机构
        entity.setAcctbr(acct.getBrchno());
        // 入账日期
        entity.setAcctdt(acctdt);

    }

    private static void validatePostProductProperty(String acctno,
            BigDecimal tranam, E_FCFLAG fcflag, String prodcd, String crcycd) {
        // 金额控制方式
        E_AMNTWY amntwy = null;
        // 最小控制金额
        BigDecimal miniam = BigDecimal.ZERO;
        // 最大控制金额
        BigDecimal maxiam = BigDecimal.ZERO;
        // 存入次数控制方式
        E_TIMEWY timewy = null;
        // 实际存入次数
        Long resvnm = 0l;
        // 最小存入次数
        Long minitm = 0l;
        // 最大存入次数
        Long maxitm = 0l;

        // 存入控制方式、方法改成从账户层获取 update in 20160627
        // 存入控制方式
        E_SAVECT posttp = null;
        // 存入控制方法
        E_POSTWY postwy = null;
        // 设置存入计划标志
        E_PLANFG planfg = null;
        KnaFxsv tblKnaFxsv = null;
        // 获取负债定期账户存入控制信息
        tblKnaFxsv = KnaFxsvDao.selectOne_odb1(acctno, false);
        if (CommUtil.isNull(tblKnaFxsv)) {

        	throw DpModuleError.DpstComm.BNAS0841();
        }
        amntwy = tblKnaFxsv.getAmntwy();// 存入金额控制方式
        miniam = tblKnaFxsv.getMiniam();// 单次存入最小金额
        maxiam = tblKnaFxsv.getMaxiam();// 单次存入最大金额
        timewy = tblKnaFxsv.getTimewy();// 存入次数控制方式
        resvnm = tblKnaFxsv.getResvnm() + 1;// 实际存入次数
        minitm = tblKnaFxsv.getMinitm();// 最小存入次数
        maxitm = tblKnaFxsv.getMaxitm();// 最大存入次数
        posttp = tblKnaFxsv.getPosttp(); // 存入控制方式
        postwy = tblKnaFxsv.getPostwy(); // 存入控制方法
        planfg = tblKnaFxsv.getPlanfg(); // 设置存入计划标志

        // 如果是设置了存入计划，则判断存入计划相关控制，如果没有设置存入计划，则判断存入控制相关控制逻辑，其中为空也为未设置
        if (E_PLANFG.NOSET == planfg || CommUtil.isNull(planfg)) {
            if (E_SAVECT.YES == posttp) {
                return;
            } else if (E_SAVECT.COND == posttp) {
                if (E_POSTWY.AMCL == postwy) {
                    // 金额控制
                    DpProductProc.chkAmtControl(amntwy, tranam, miniam, maxiam);
                    tblKnaFxsv.setResvam(tblKnaFxsv.getResvam().add(tranam));
                    KnaFxsvDao.updateOne_odb1(tblKnaFxsv);
                } else if (E_POSTWY.TMCL == postwy) {
                    // 次数控制
                    DpProductProc
                            .chkTimeControl(timewy, resvnm, minitm, maxitm);
                    tblKnaFxsv.setResvnm(tblKnaFxsv.getResvnm() + 1);
                    KnaFxsvDao.updateOne_odb1(tblKnaFxsv);
                } else if (E_POSTWY.ATMC == postwy) {
                    // 金额和次数都控制
                    DpProductProc.chkAmtControl(amntwy, tranam, miniam, maxiam);
                    DpProductProc
                            .chkTimeControl(timewy, resvnm, minitm, maxitm);

                    tblKnaFxsv.setResvam(tblKnaFxsv.getResvam().add(tranam));
                    tblKnaFxsv.setResvnm(tblKnaFxsv.getResvnm() + 1);
                    KnaFxsvDao.updateOne_odb1(tblKnaFxsv);
                } else {
                    throw DpModuleError.DpstComm.BNAS1008();
                }
            } else {
                throw DpModuleError.DpstComm.BNAS1008();
            }
        } else if (E_PLANFG.SET == planfg) {
            DpProductProc.prcFxacSavePlan(acctno, tranam);
        } else {
            throw DpModuleError.DpstComm.BNAS1151(planfg.toString());
        }
    }

    /**
     * 
     * <p>Title:dealPayAfter </p>
     * <p>Description: 处理追缴定期本金和会计分录</p>
     * 
     * @author Cuijia
     * @date 2017年9月13日
     * @param entity
     * @param tranam
     */
    public static void dealPayAfter(DpSaveEntity entity, BigDecimal tranam) {
        DpAcctOnlnblEntity onlnblEntity = new DpAcctOnlnblEntity();
        onlnblEntity.setAcctno(entity.getAcctno());
        onlnblEntity.setAmntcd(E_AMNTCD.DR);
        onlnblEntity.setTranam(tranam);
        onlnblEntity.setCrcycd(entity.getCrcycd());
        onlnblEntity.setCardno(entity.getCardno());
        onlnblEntity.setCustac(entity.getCustac());
        onlnblEntity.setAcseno(entity.getAcseno());
        onlnblEntity.setToacct(entity.getToacct());
        onlnblEntity.setOpacna(entity.getOpacna());
        onlnblEntity.setLinkno(entity.getLinkno());
        onlnblEntity.setDetlfg(entity.getDetlfg()); // 是否明细汇总标志
        onlnblEntity.setOpbrch(entity.getOpbrch()); // 对方账户所属机构
        onlnblEntity.setBankcd(entity.getBankcd()); // 对方金融机构
        onlnblEntity.setBankna(entity.getBankna()); // 对方金融机构名称
        onlnblEntity.setSmrycd(entity.getSmrycd()); // 摘要代码
        onlnblEntity.setSmryds(entity.getSmryds()); // 摘要描述
        onlnblEntity.setRemark(entity.getRemark()); // 备注

        DpAcctProc.updateDpAcctOnlnbl(onlnblEntity, E_FCFLAG.FIX, E_YES___.NO);

        entity.setPydetlsq(onlnblEntity.getDetlsq());

        /**
         * 会计分录处理
         * Dr 存款本金
         * Cr 应付利息
         * Dr 利息支出（红字）
         * Cr 应付利息（红字）
         */
        IoAccounttingIntf cplIoAccounttingIntf = SysUtil
                .getInstance(IoAccounttingIntf.class);
        cplIoAccounttingIntf.setCuacno(entity.getCustac());
        cplIoAccounttingIntf.setAcctno(entity.getAcctno());
        cplIoAccounttingIntf.setAcseno(entity.getAcseno());
        cplIoAccounttingIntf.setToacct(entity.getToacct());// 对方账户
        cplIoAccounttingIntf.setToacna(entity.getOpacna());// 对方名称
        cplIoAccounttingIntf.setTobrch(entity.getOpbrch());// 对方账户所属机构
        cplIoAccounttingIntf.setProdcd(onlnblEntity.getProdcd());
        cplIoAccounttingIntf.setDtitcd(onlnblEntity.getDtitcd());
        cplIoAccounttingIntf.setCrcycd(entity.getCrcycd());
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
            KnpParameter para = KnpParameterDao.selectOne_odb1("GlAnalysis", "1020000", "%", "%",
                    true);
            cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息 20160701
                                                             // 产品减少
        }
        //Dr追缴本金
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                cplIoAccounttingIntf);

        //Cr 应付利息
        cplIoAccounttingIntf.setTranam(tranam);
        cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR);
        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
        cplIoAccounttingIntf.setBltype(E_BLTYPE.PYIN);
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                cplIoAccounttingIntf);

        /*
        //Dr 利息支出（红字）
        cplIoAccounttingIntf.setTranam(tranam.negate());
        cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR);
        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
        cplIoAccounttingIntf.setBltype(E_BLTYPE.CAIN);
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                cplIoAccounttingIntf);
        
         //Cr 应付利息（红字）
        cplIoAccounttingIntf.setTranam(tranam.negate());
        cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR);
        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
        cplIoAccounttingIntf.setBltype(E_BLTYPE.PYIN);
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                cplIoAccounttingIntf);
         */
    }

    /**
     * 
     * <p>Title:strikePayAfter </p>
     * <p>Description: 追缴利息冲正方法</p>
     * 
     * @author Cuijia
     * @date 2017年9月13日
     * @param entity
     * @param tranam
     */
    public static void strikePayAfter(DpSaveEntity entity, BigDecimal tranam, KnlBill bill) {
        DpAcctOnlnblEntity onlnblEntity = new DpAcctOnlnblEntity();
        onlnblEntity.setAcctno(entity.getAcctno());
        onlnblEntity.setAmntcd(E_AMNTCD.CR);
        onlnblEntity.setTranam(tranam);
        onlnblEntity.setCrcycd(entity.getCrcycd());
        onlnblEntity.setCustac(entity.getCustac());
        onlnblEntity.setLinkno(entity.getLinkno());
        onlnblEntity.setDetlfg(entity.getDetlfg()); // 是否明细汇总标志
        onlnblEntity.setOpbrch(entity.getOpbrch()); // 对方账户所属机构
        onlnblEntity.setBankcd(entity.getBankcd()); // 对方金融机构
        onlnblEntity.setBankna(entity.getBankna()); // 对方金融机构名称
        onlnblEntity.setSmrycd(entity.getSmrycd()); // 摘要代码
        onlnblEntity.setSmryds(entity.getSmryds()); // 摘要描述
        onlnblEntity.setRemark(entity.getRemark()); // 备注
        onlnblEntity.setCardno(bill.getCardno());
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
        // 摘要码
        onlnblEntity.setSmrycd(bill.getSmrycd());
        // 账户状态
        if (CommUtil.isNotNull(entity.getAcctst())) {
            onlnblEntity.setAcctst(entity.getAcctst());
        }

        DpAcctProc.updateDpAcctOnlnbl(onlnblEntity, E_FCFLAG.FIX, E_YES___.YES);

        //登记付息明细信息
        KnbPidl tblKnbPidl = KnbPidlDao.selectFirst_odb3(entity.getAcctno(), entity.getOrtrdt(), bill.getProcsq(), E_INDLTP.PYAFT, true);
        tblKnbPidl.setIndlst(E_INDLST.WUX); //付息明细状态

        KnbPidlDao.updateOne_odb4(tblKnbPidl);

        /**
         * 会计分录处理
         * Dr 存款本金
         * Cr 应付利息
         * Dr 利息支出（红字）
         * Cr 应付利息（红字）
         */
        IoAccounttingIntf cplIoAccounttingIntf = SysUtil
                .getInstance(IoAccounttingIntf.class);
        cplIoAccounttingIntf.setCuacno(entity.getCustac());
        cplIoAccounttingIntf.setAcctno(entity.getAcctno());
        cplIoAccounttingIntf.setAcseno(entity.getAcseno());
        cplIoAccounttingIntf.setToacct(entity.getToacct());// 对方账户
        cplIoAccounttingIntf.setToacna(entity.getOpacna());// 对方名称
        cplIoAccounttingIntf.setTobrch(entity.getOpbrch());// 对方账户所属机构
        cplIoAccounttingIntf.setProdcd(onlnblEntity.getProdcd());
        cplIoAccounttingIntf.setDtitcd(onlnblEntity.getDtitcd());
        cplIoAccounttingIntf.setCrcycd(entity.getCrcycd());
        cplIoAccounttingIntf.setTranam(tranam.negate());
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
            KnpParameter para = KnpParameterDao.selectOne_odb1("GlAnalysis", "1020000", "%", "%",
                    true);
            cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息 20160701
                                                             // 产品减少
        }
        //Dr追缴本金
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                cplIoAccounttingIntf);

        //Cr 应付利息
        cplIoAccounttingIntf.setTranam(tranam.negate());
        cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR);
        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
        cplIoAccounttingIntf.setBltype(E_BLTYPE.PYIN);
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                cplIoAccounttingIntf);
    }

}
