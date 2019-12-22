package cn.sunline.ltts.busi.dp.acct;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.DpAcinDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbDfir;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbDfirDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDetl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDetlDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDrdl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDrdlDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdr;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdrDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.CalInterTax;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpClsChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpClsChkOT;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpClsChkOTPdlist;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CAINPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRRTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BSINDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TEARTP;

/**
 * 存款利息收益试算
 * */
public class DpTestCalcInterest {

    private static BizLog log = BizLogUtil.getBizLog(DpTestCalcInterest.class);

    /**
     * @Title: CalcTestInterest
     * @Description: 销户试算、定期产品提前支取试算
     * @param chkin
     * @return
     * @author zhangan
     * @date 2016年12月14日 上午10:50:17
     * @version V2.3.0
     */
    public static IoDpClsChkOT CalcTestInterest(IoDpClsChkIN chkin) {

        IoDpClsChkOT chkot = SysUtil.getInstance(IoDpClsChkOT.class);
        String custac = chkin.getCustac();
        String acctno = chkin.getAcctno();
        BigDecimal tranam = CommUtil.nvl(chkin.getTranam(), BigDecimal.ZERO);
        E_YES___ isdraw = chkin.getIsdraw(); //定期存款支取试算

        List<KnaAcct> lst_acct = new ArrayList<>();
        List<KnaFxac> lst_fxac = new ArrayList<>();

        if (isdraw == E_YES___.YES) { //试算除去定期智能储蓄的定期存款的支取利息
            if (CommUtil.isNull(acctno)) {
                throw DpModuleError.DpstComm.BNAS0396();
            }
            if (CommUtil.equals(tranam, BigDecimal.ZERO)) {
                throw DpModuleError.DpstComm.BNAS0394();
            }

            KnaFxac fxac = ActoacDao.selKnaFxac(acctno, false);
            if (CommUtil.isNull(fxac) || fxac.getDebttp() == E_DEBTTP.DP2404) {
                throw DpModuleError.DpstComm.BNAS0879();
            }

            fxac.setOnlnbl(tranam);
            lst_fxac.add(fxac);
        } else {
            //获取电子账户的活期存款信息
            if (chkin.getIssett() == E_YES___.YES) {
                KnaAcct KnaAcct = CapitalTransDeal.getSettKnaAcctAc(custac);
                lst_acct.add(KnaAcct);
            } else {
                //ActoacDao.selAll
                lst_acct = KnaAcctDao.selectAll_odb6(custac, false);
                lst_fxac = KnaFxacDao.selectAll_odb5(custac, false);
            }
        }

        //获取电子账户的定期存款信息
        String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
        BigDecimal totalintr = BigDecimal.ZERO; //总利息
        BigDecimal interest = BigDecimal.ZERO; //利息变量
        BigDecimal intertax = BigDecimal.ZERO; //利息税变量
        BigDecimal totalbal = BigDecimal.ZERO; //总余额
        BigDecimal dppbbal = BigDecimal.ZERO; //存款产品总余额  活期存款和定期存款
        BigDecimal fxbal = BigDecimal.ZERO;
        BigDecimal cubal = BigDecimal.ZERO;
        BigDecimal fxInst = BigDecimal.ZERO; //定期利息
        BigDecimal cuInst = BigDecimal.ZERO; //活期利息

        for (KnaAcct acct : lst_acct) {

            if (acct.getAcctst() == E_DPACST.CLOSE) {
                continue;
            }

            interest = BigDecimal.ZERO;

            KnbAcin acin = KnbAcinDao.selectOne_odb1(acct.getAcctno(), false);
            if (CommUtil.isNull(acin)) {
                throw DpModuleError.DpstAcct.BNAS0710();
            }
            BigDecimal onlnbl = DpAcctProc.getAcctBalance(acct);
            if (E_INBEFG.INBE == acin.getInbefg()) {// 计息标志为计息

                IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);

                IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                intrEntity.setCorpno(acin.getCorpno()); //法人代码
                intrEntity.setBrchno(acct.getBrchno());//机构号
                intrEntity.setTranam(onlnbl);//交易金额
                intrEntity.setTrandt(trandt);//交易日期
                intrEntity.setIntrcd(acin.getIntrcd()); //利率代码 
                intrEntity.setIncdtp(acin.getIncdtp()); //利率代码类型
                intrEntity.setCrcycd(acin.getCrcycd());//币种
                intrEntity.setInbebs(acin.getTxbebs()); //计息基础
                intrEntity.setIntrwy(acin.getIntrwy()); //靠档方式
                intrEntity.setBgindt(acin.getBgindt()); //起息日期
                intrEntity.setDepttm(E_TERMCD.T000);
                intrEntity.setEdindt(trandt); //止息日

                intrEntity.setLevety(acin.getLevety());
                if (acin.getIntrdt() == E_INTRDT.OPEN) {
                    intrEntity.setTrandt(acin.getOpendt());
                    intrEntity.setTrantm("999999");
                }
                pbpub.countInteresRate(intrEntity);

                BigDecimal intrvl = intrEntity.getIntrvl();

                KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acin.getAcctno(), true);

                // 利率优惠后执行利率
                intrvl = intrvl.add(intrvl.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                        divide(BigDecimal.valueOf(100))));

                //mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
                //利率的最大范围值
                BigDecimal intrvlmax = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                //利率的最小范围值
                BigDecimal intrvlmin = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

                if (CommUtil.compare(intrvl, intrvlmin) < 0) {
                    intrvl = intrvlmin;
                } else if (CommUtil.compare(intrvl, intrvlmax) > 0) {
                    intrvl = intrvlmax;
                }

                CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
                calInterTax.setAcctno(acctno);
                calInterTax.setTranam(onlnbl);
                calInterTax.setCuusin(intrvl);
                calInterTax.setInbebs(acin.getTxbebs());

                InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(acin, calInterTax, false);

                interest = interestAndTax.getInstam();
                intertax = interestAndTax.getIntxam();

                log.debug("账号:[%s],账号类型:[%s],余额:[%s],利息:[%s]", acct.getAcctno(), acct.getAcsetp(), onlnbl, interest);
            }
            cuInst = cuInst.add(interest).subtract(intertax);//活期利息
            cubal = cubal.add(acct.getOnlnbl());

            if (acct.getAcsetp() == E_ACSETP.SA) { //结算户
                chkot.setAcctbl(onlnbl);
            } else if (acct.getAcsetp() == E_ACSETP.MA) {
                chkot.setWallet(onlnbl);
            } else if (acct.getAcsetp() == E_ACSETP.HQ) {
                dppbbal = dppbbal.add(onlnbl); //存款余额累加
            } else if (acct.getAcsetp() == E_ACSETP.FW) {
                chkot.setWactbl(onlnbl);
            }

            IoDpClsChkOTPdlist pdlst = SysUtil.getInstance(IoDpClsChkOTPdlist.class);
            pdlst.setPddpfg(acct.getPddpfg());
            pdlst.setProdcd(acct.getProdcd());

            chkot.getPdlist().add(pdlst);
        }

        String drintpName = "";
        for (KnaFxac fxac : lst_fxac) {

            if (fxac.getAcctst() == E_DPACST.CLOSE) {
                continue;
            }

            IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);

            fxbal = fxbal.add(fxac.getOnlnbl());
            KnbDfir tblKnbDfir = SysUtil.getInstance(KnbDfir.class);
            E_YES___ dpbkfg = E_YES___.NO;

            KnaFxdr tblKnaFxdr = KnaFxdrDao.selectOne_odb1(fxac.getAcctno(), false);
            if (CommUtil.isNotNull(tblKnaFxdr)) {
                dpbkfg = tblKnaFxdr.getDpbkfg();
            }

            KnbAcin acin = KnbAcinDao.selectOne_odb1(fxac.getAcctno(), false);
            if (CommUtil.isNull(acin)) {
                throw DpModuleError.DpstAcct.BNAS0710();
            }
            KubInrt inrt = KubInrtDao.selectOne_odb1(fxac.getAcctno(), false);
            if (CommUtil.isNull(inrt)) {
                throw DpModuleError.DpstAcct.BNAS0174();
            }
            if (E_INBEFG.INBE == acin.getInbefg()) {// 计息标志为计息
                if (acin.getDetlfg() == E_YES___.YES) {
                    List<KnaFxacDetl> lstDetl = KnaFxacDetlDao.selectAll_odb3(fxac.getAcctno(), E_DPACST.NORMAL, false);
                    for (KnaFxacDetl detl : lstDetl) {
                        IoPbIntrPublicEntity entity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                        entity.setCrcycd(detl.getCrcycd());
                        entity.setIntrcd(inrt.getIntrcd());
                        entity.setIncdtp(inrt.getIncdtp());
                        entity.setTrandt(trandt);
                        entity.setIntrwy(inrt.getIntrwy());
                        entity.setBgindt(detl.getBgindt());
                        entity.setEdindt(trandt);
                        entity.setDepttm(fxac.getDepttm());
                        entity.setTranam(detl.getOnlnbl());
                        entity.setInbebs(acin.getTxbebs()); //计息基础
                        entity.setCorpno(acin.getCorpno());
                        entity.setBrchno(fxac.getBrchno());
                        entity.setLevety(acin.getLevety()); //靠档类型
                        if (acin.getIntrdt() == E_INTRDT.OPEN) {
                            entity.setTrandt(detl.getOpendt()); //开户日期 
                            entity.setTrantm("999999");
                        }
                        pbpub.countInteresRate(entity);

                        BigDecimal cuusin = entity.getIntrvl();
                        // 利率优惠后执行利率
                        cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(inrt.getFavort(), BigDecimal.ZERO).
                                divide(BigDecimal.valueOf(100))));

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

                        //计算利息
                        interest = pbpub.countInteresRateByAmounts(
                                cuusin, detl.getBgindt(),
                                trandt, detl.getOnlnbl(), acin.getTxbebs());

                        CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
                        calInterTax.setAcctno(acctno);
                        calInterTax.setTranam(detl.getOnlnbl());
                        calInterTax.setBegndt(detl.getBgindt());
                        calInterTax.setEnddat(trandt);
                        calInterTax.setCuusin(cuusin);
                        calInterTax.setInstam(interest);
                        calInterTax.setInbebs(acin.getTxbebs());

                        InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(acin, calInterTax, false);
                        intertax = interestAndTax.getIntxam();

                        fxInst = fxInst.add(BusiTools.roundByCurrency(detl.getCrcycd(), interest, null)).subtract(intertax);

                        log.debug("账号:[%s],明细序号:[%s],账号类型:[%s],明细余额:[%s],利息:[%s],利息税:[%s]", fxac.getAcctno(), detl.getDetlsq(), fxac.getAcsetp(), detl.getOnlnbl(), interest,
                                intertax);
                    }
                } else {

                    // 特殊处理通知存款违约支取
                    if (fxac.getDebttp() == E_DEBTTP.DP2506) {

                        if (E_TERMCD.T107 == fxac.getDepttm()) {
                            if (CommUtil.compare(DateTools2.calDays(fxac.getBgindt(), trandt, 0, 0) - 7, 0) < 0) {

                                fxac.setMatudt(DateTimeUtil.dateAdd("day", fxac.getBgindt(), 7));
                            } else {
                                fxac.setMatudt(trandt);
                            }
                        } else if (E_TERMCD.T101 == fxac.getDepttm()) {
                            if (CommUtil.compare(DateTools2.calDays(fxac.getBgindt(), trandt, 0, 0) - 1, 0) < 0) {

                                fxac.setMatudt(DateTimeUtil.dateAdd("day", fxac.getBgindt(), 1));
                            } else {
                                fxac.setMatudt(trandt);
                            }
                        }

                    }

                    if (fxac.getDebttp() == E_DEBTTP.DP2505) { //定活两便
                        //计算利息，使用行内基准的活期利率
                        IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                        intrEntity.setCrcycd(fxac.getCrcycd()); //币种
                        intrEntity.setIntrcd(acin.getIntrcd()); //利率代码

                        //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                        intrEntity.setIncdtp(acin.getIncdtp()); //利率代码类型
                        intrEntity.setIntrwy(acin.getIntrwy()); //靠档方式
                        intrEntity.setTrandt(trandt);
                        //					intrEntity.setBgindt(fxac.getBgindt()); //起始日期
                        //					intrEntity.setEdindt(trandt); //结束日期
                        intrEntity.setTranam(fxac.getOnlnbl()); //交易金额
                        intrEntity.setInbebs(acin.getTxbebs()); //计息基础
                        intrEntity.setCorpno(fxac.getCorpno());//法人代码
                        intrEntity.setBrchno(fxac.getBrchno());//机构
                        intrEntity.setLevety(acin.getLevety());
                        if (acin.getIntrdt() == E_INTRDT.OPEN) {
                            intrEntity.setTrandt(fxac.getOpendt());
                            intrEntity.setTrantm("999999");
                        }

                        //					pbpub.countInteresRate(intrEntity);

                        // 定活两便执行利率小于活期利率时取活期利率 add liaojc 20170220
                        // 取活期执行利率
                        intrEntity.setBgindt(trandt); //起始日期
                        intrEntity.setEdindt(trandt); //结束日期
                        pbpub.countInteresRate(intrEntity);
                        BigDecimal accsin = intrEntity.getIntrvl();// 活期执行利率
                        // 利率优惠后执行利率
                        accsin = accsin.add(accsin.multiply(CommUtil.nvl(inrt.getFavort(), BigDecimal.ZERO).
                                divide(BigDecimal.valueOf(100))));

                        //mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
                        //利率的最大范围值
                        BigDecimal intrvlmax = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                        //利率的最小范围值
                        BigDecimal intrvlmin = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

                        if (CommUtil.compare(accsin, intrvlmin) < 0) {
                            accsin = intrvlmin;
                        } else if (CommUtil.compare(accsin, intrvlmax) > 0) {
                            accsin = intrvlmax;
                        }

                        // 获取当前日期执行利率
                        intrEntity.setBgindt(fxac.getBgindt()); //起始日期
                        intrEntity.setEdindt(trandt); //结束日期

                        pbpub.countInteresRate(intrEntity);
                        BigDecimal actsin = intrEntity.getIntrvl();// 实际执行利率

                        // 利率优惠后执行利率
                        actsin = actsin.add(actsin.multiply(CommUtil.nvl(inrt.getFavort(), BigDecimal.ZERO).
                                divide(BigDecimal.valueOf(100))));

                        //mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
                        //利率的最大范围值
                        intrvlmax = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                        //利率的最小范围值
                        intrvlmin = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

                        if (CommUtil.compare(actsin, intrvlmin) < 0) {
                            actsin = intrvlmin;
                        } else if (CommUtil.compare(actsin, intrvlmax) > 0) {
                            actsin = intrvlmax;
                        }

                        if (CommUtil.compare(accsin, actsin) < 0) {
                            accsin = actsin;// 执行利率
                        }

                        //计算利息
                        BigDecimal bigInstam = pbpub.countInteresRateByAmounts(
                                accsin, fxac.getBgindt(),
                                trandt, fxac.getOnlnbl(), acin.getTxbebs());

                        CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
                        calInterTax.setAcctno(acctno);
                        calInterTax.setTranam(fxac.getOnlnbl());
                        calInterTax.setBegndt(fxac.getBgindt());
                        calInterTax.setEnddat(trandt);
                        calInterTax.setCuusin(accsin);
                        calInterTax.setInstam(bigInstam);
                        calInterTax.setInbebs(acin.getTxbebs());

                        InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(acin, calInterTax, false);
                        intertax = interestAndTax.getIntxam();

                        fxInst = fxInst.add(BusiTools.roundByCurrency(fxac.getCrcycd(), bigInstam, null)).subtract(intertax);
                        log.debug("定活两便，账号:[%s],账号类型:[%s],余额:[%s],利息:[%s],利息税:[%s]", fxac.getAcctno(), fxac.getAcsetp(), fxac.getOnlnbl(), bigInstam, intertax);
                    } else {
                        //违约支取,按活期结息
                        if (dpbkfg == E_YES___.YES) {

                            /* 按活期利率计算 */
                            tblKnbDfir = KnbDfirDao.selectOne_odb1(fxac.getAcctno(), E_TEARTP.TQZQ, false);

                            drintpName = E_TEARTP.TQZQ.getLongName();

                            //检查违约支取利息定义信息
                            if (CommUtil.isNull(tblKnbDfir)) {
                                throw DpModuleError.DpstAcct.BNAS1148();
                            }

                            //计算利息，使用行内基准的活期利率
                            IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                            intrEntity.setCrcycd(fxac.getCrcycd()); //币种
                            intrEntity.setIntrcd(tblKnbDfir.getIntrcd()); //利率代码
                            //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                            //						if(tblKnbDfir.getInclfg() == E_YES___.YES){
                            //							intrEntity.setIntrwy(tblKnbDfir.getIntrwy()); //靠档方式
                            //						}

                            if (E_IRCDTP.BASE == tblKnbDfir.getIncdtp() ||
                                    E_IRCDTP.Reference == tblKnbDfir.getIncdtp()) {
                                intrEntity.setDepttm(E_TERMCD.T000);
                            } else if (E_IRCDTP.LAYER == tblKnbDfir.getIncdtp()) {
                                if (tblKnbDfir.getInclfg() == E_YES___.YES) {
                                    intrEntity.setIntrwy(tblKnbDfir.getIntrwy()); //靠档方式
                                }
                            }

                            //						intrEntity.setDepttm(fxac.getDepttm()); //存期/
                            intrEntity.setIncdtp(tblKnbDfir.getIncdtp()); //利率代码类型
                            intrEntity.setTrandt(trandt);
                            intrEntity.setBgindt(fxac.getMatudt()); //起始日期
                            intrEntity.setEdindt(trandt); //结束日期
                            intrEntity.setTranam(fxac.getOnlnbl()); //交易金额
                            intrEntity.setInbebs(tblKnbDfir.getBsinrl()); //计息基础
                            intrEntity.setCorpno(fxac.getCorpno());//法人代码
                            intrEntity.setBrchno(fxac.getBrchno());//机构

                            intrEntity.setLevety(tblKnbDfir.getLevety());
                            if (tblKnbDfir.getIntrdt() == E_INTRDT.OPEN) {
                                intrEntity.setTrandt(fxac.getOpendt());
                                intrEntity.setTrantm("999999");
                            }

                            pbpub.countInteresRate(intrEntity);

                            CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
                            calInterTax.setAcctno(acctno);
                            calInterTax.setTranam(fxac.getOnlnbl());
                            calInterTax.setBegndt(fxac.getBgindt());
                            calInterTax.setEnddat(trandt);
                            calInterTax.setCuusin(intrEntity.getIntrvl());
                            calInterTax.setInstam(intrEntity.getInamnt());
                            calInterTax.setInbebs(tblKnbDfir.getBsinrl());

                            InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(acin, calInterTax, false);
                            intertax = interestAndTax.getIntxam();

                            fxInst = fxInst.add(BusiTools.roundByCurrency(fxac.getCrcycd(), intrEntity.getInamnt(), null)).subtract(intertax);
                            log.debug("提前支取违约，账号:[%s],账号类型:[%s],余额:[%s],利息:[%s],利息税:[%s]", fxac.getAcctno(), fxac.getAcsetp(), fxac.getOnlnbl(), intrEntity.getInamnt(), intertax);
                        } else {
                            //未按支取计划管理则按照传统处理方式
                            //提前支取
                            if (CommUtil.compare(fxac.getMatudt(), trandt) > 0) {
                                //查询提前销户的违约利率
                                tblKnbDfir = KnbDfirDao.selectOne_odb1(fxac.getAcctno(), E_TEARTP.TQXH, false);
                                drintpName = E_TEARTP.TQXH.getLongName();
                                String instdt = null; //提前支取起息日期
                                //检查违约支取利息定义信息
                                if (CommUtil.isNull(tblKnbDfir)) {
                                    throw DpModuleError.DpstAcct.BNAS1148();
                                }
                                //若账户违约支取表中起息日来源为 起息日，则取定期表中起息日期
                                if (tblKnbDfir.getBsindt() == E_BSINDT.QXR) {
                                    instdt = fxac.getBgindt(); //计息起始日期
                                } else if (tblKnbDfir.getBsindt() == E_BSINDT.SCFX) {
                                    //若账户违约表中起息日来源为上次付息日，则取计提表中上次结息日期
                                    //若计提表中上次付息日期为空则取定期表中起息日期
                                    if (CommUtil.isNotNull(acin.getLcindt())) {
                                        instdt = acin.getLcindt();// 计息起始日期
                                    } else {
                                        instdt = fxac.getBgindt(); // 计息起始日期
                                    }

                                    //若结息日当天支取则修改计息日为当天日期（暂时这样修改  未确定）
                                    if (CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_date(), acin.getNcindt())) {
                                        instdt = CommTools.getBaseRunEnvs().getTrxn_date();
                                    }

                                } else {
                                    throw DpModuleError.DpstAcct.BNAS0004();
                                }

                                //计算利息，使用行内基准的活期利率
                                IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                                intrEntity.setCrcycd(fxac.getCrcycd()); //币种
                                intrEntity.setIntrcd(tblKnbDfir.getIntrcd()); //利率代码
                                //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期

                                if (E_IRCDTP.BASE == tblKnbDfir.getIncdtp() ||
                                        E_IRCDTP.Reference == tblKnbDfir.getIncdtp()) {
                                    intrEntity.setDepttm(E_TERMCD.T000);
                                } else if (E_IRCDTP.LAYER == tblKnbDfir.getIncdtp()) {
                                    if (tblKnbDfir.getInclfg() == E_YES___.YES) {
                                        intrEntity.setIntrwy(tblKnbDfir.getIntrwy()); //靠档方式
                                    }
                                }

                                intrEntity.setIncdtp(tblKnbDfir.getIncdtp()); //利率代码类型
                                intrEntity.setTrandt(trandt);
                                //intrEntity.setBgindt(fxac.getBgindt()); //起始日期
                                intrEntity.setBgindt(acin.getBgindt()); //起息日期
                                intrEntity.setEdindt(trandt); //结束日期
                                intrEntity.setTranam(fxac.getOnlnbl()); //交易金额
                                intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                                intrEntity.setInbebs(tblKnbDfir.getBsinrl()); //计息基础
                                intrEntity.setCorpno(fxac.getCorpno());//法人代码
                                intrEntity.setBrchno(fxac.getBrchno());//机构

                                intrEntity.setLevety(tblKnbDfir.getLevety());
                                if (tblKnbDfir.getIntrdt() == E_INTRDT.OPEN) {
                                    intrEntity.setTrandt(fxac.getOpendt());
                                    intrEntity.setTrantm("999999");
                                }
                                pbpub.countInteresRate(intrEntity);
                                interest = intrEntity.getInamnt();
                                BigDecimal cuusin = intrEntity.getIntrvl();
                                /*  interest = pbpub.countInteresRateByAmounts(
                                          cuusin, acin.getBgindt(),
                                          fxac.getMatudt(), fxac.getOnlnbl(), acin.getTxbebs());*/

                                /**
                                 * cuijia
                                 * 提前支取时，追缴已付利息
                                 * 
                                 * 获取上次提前支取到当前的已付利息金额
                                 * 已付利息为零和当前提前支取金额计算利息相等，不做处理
                                 * 已付利息大于提前支取金额计算利息，差额从客户本金中追缴
                                 * 已付利息小于提前支取金额计算利息，调整支取结息金额为差额
                                 */
                                if (tblKnbDfir.getDrdein() == E_YES___.YES) {

                                	
                                	/*
                                    BigDecimal acinAmt = DpAcinDao.getKnbPidlBetweenDrawAmount(acctno, false);

                                    if (CommUtil.isNull(acinAmt)||CommUtil.compare(acinAmt, BigDecimal.ZERO) == 0 || CommUtil.compare(acinAmt, interest) == 0) {

                                    } else if (CommUtil.compare(acinAmt, interest) > 0) {
                                        interest = BigDecimal.ZERO;
                                    } else {
                                        interest = interest.subtract(acinAmt);
                                    }
                                    */
                                	
                                	//判断在有提前支取的情况下，计算两次提前支取间的已付利息

                                    BigDecimal acinAmt = BigDecimal.ZERO;
                                    String lcindt = acin.getLcindt(); //上次结息日
                                    //判断是否已结息
                                    if (CommUtil.isNotNull(lcindt) && CommUtil.compare(lcindt, instdt) > 0) {
                                        //检查是否有分段利率，有需要按照分段利率计算，付息表中的利率不准确
                                        List<KnbIndl> knbIndlListDO = DpAcinDao.listKnbIndl(acctno, E_INTRTP.ZHENGGLX, instdt, lcindt, false);
                                        String instDt = instdt; //计息开始日期
                                        for (KnbIndl knbIndlDO : knbIndlListDO) {
                                            IoPbIntrPublicEntity intrDO = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                                            intrDO.setCrcycd(fxac.getCrcycd()); //币种
                                            intrDO.setIntrcd(knbIndlDO.getIntrcd()); //利率代码
                                            if (E_IRCDTP.BASE == knbIndlDO.getIncdtp() ||
                                                    E_IRCDTP.Reference == knbIndlDO.getIncdtp()) {
                                                intrDO.setDepttm(fxac.getDepttm());
                                            } else if (E_IRCDTP.LAYER == knbIndlDO.getIncdtp()) {
                                                intrDO.setIntrwy(knbIndlDO.getIntrwy()); //靠档方式
                                            }
                                            intrDO.setIncdtp(knbIndlDO.getIncdtp()); //利率代码类型
                                            intrDO.setTrandt(knbIndlDO.getInstdt());
                                            intrDO.setBgindt(knbIndlDO.getInstdt()); //起始日期
                                            intrDO.setEdindt(knbIndlDO.getIneddt()); //结束日期
                                            intrDO.setTranam(tranam); //交易金额
                                            intrDO.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                                            intrDO.setInbebs(knbIndlDO.getTxbebs()); //计息基础
                                            intrDO.setCorpno(fxac.getCorpno());//法人代码
                                            intrDO.setBrchno(fxac.getBrchno());//机构
                                            intrDO.setLevety(tblKnbDfir.getLevety());
                                            pbpub.countInteresRate(intrDO);

                                            acinAmt = acinAmt.add(intrDO.getInamnt());

                                            instDt = knbIndlDO.getIneddt();
                                        }
                                        IoPbIntrPublicEntity intrDO = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                                        intrDO.setCrcycd(fxac.getCrcycd()); //币种
                                        intrDO.setIntrcd(acin.getIntrcd()); //利率代码
                                        if (E_IRCDTP.BASE == acin.getIncdtp() ||
                                                E_IRCDTP.Reference == acin.getIncdtp()) {
                                            intrDO.setDepttm(fxac.getDepttm());
                                        } else if (E_IRCDTP.LAYER == acin.getIncdtp()) {
                                            intrDO.setIntrwy(acin.getIntrwy()); //靠档方式
                                        }
                                        intrDO.setIncdtp(acin.getIncdtp()); //利率代码类型
                                        intrDO.setTrandt(instDt);
                                        intrDO.setBgindt(instDt); //起始日期
                                        intrDO.setEdindt(lcindt); //结束日期
                                        intrDO.setTranam(tranam); //交易金额
                                        intrDO.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                                        intrDO.setInbebs(acin.getTxbebs()); //计息基础
                                        intrDO.setCorpno(fxac.getCorpno());//法人代码
                                        intrDO.setBrchno(fxac.getBrchno());//机构
                                        intrDO.setLevety(tblKnbDfir.getLevety());
                                        pbpub.countInteresRate(intrDO);

                                        acinAmt = acinAmt.add(intrDO.getInamnt());
                                    }
                                    if (CommUtil.compare(acinAmt, BigDecimal.ZERO) == 0 || CommUtil.compare(acinAmt, interest) >= 0) {
                                        interest = BigDecimal.ZERO;
                                    } else if (CommUtil.compare(acinAmt, interest) < 0) {
                                        interest = interest.subtract(acinAmt);
                                    }
                                }

                                CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
                                calInterTax.setAcctno(acctno);
                                calInterTax.setTranam(fxac.getOnlnbl());
                                //calInterTax.setBegndt(fxac.getBgindt());
                                calInterTax.setBegndt(acin.getBgindt());
                                calInterTax.setEnddat(trandt);
                                calInterTax.setCuusin(intrEntity.getIntrvl());
                                calInterTax.setInstam(interest);
                                calInterTax.setInbebs(tblKnbDfir.getBsinrl());

                                InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(acin, calInterTax, false);
                                intertax = interestAndTax.getIntxam();

                                fxInst = fxInst.add(BusiTools.roundByCurrency(fxac.getCrcycd(), interest, null)).subtract(intertax);
                                log.debug("提前销户违约，账号:[%s],账号类型:[%s],余额:[%s],利息:[%s],利息税:[%s]", fxac.getAcctno(), fxac.getAcsetp(), fxac.getOnlnbl(), interest, intertax);
                            } else if (CommUtil.compare(fxac.getMatudt(), trandt) == 0) {//到期日支取

                                KubInrt tblKubinrt = KubInrtDao.selectOne_odb1(fxac.getAcctno(), true);

                                if (acin.getInprwy() == E_IRRTTP.NO) {

                                    if (E_IRCDTP.LAYER == acin.getIncdtp()) {

                                        IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                                        intrEntity.setCrcycd(fxac.getCrcycd()); //币种
                                        intrEntity.setIntrcd(acin.getIntrcd()); //利率代码

                                        //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                                        intrEntity.setIncdtp(acin.getIncdtp()); //利率代码类型
                                        intrEntity.setIntrwy(acin.getIntrwy()); //靠档方式
                                        intrEntity.setTrandt(trandt);
                                        intrEntity.setBgindt(fxac.getBgindt()); //起始日期
                                        intrEntity.setEdindt(trandt); //结束日期
                                        intrEntity.setTranam(fxac.getOnlnbl()); //交易金额
                                        intrEntity.setInbebs(acin.getTxbebs()); //计息基础
                                        intrEntity.setCorpno(fxac.getCorpno());//法人代码
                                        intrEntity.setBrchno(fxac.getBrchno());//机构

                                        intrEntity.setLevety(acin.getLevety());
                                        if (acin.getIntrdt() == E_INTRDT.OPEN) {
                                            intrEntity.setTrandt(fxac.getOpendt());
                                            intrEntity.setTrantm("999999");
                                        }
                                        pbpub.countInteresRate(intrEntity);

                                        BigDecimal cuusin = intrEntity.getIntrvl();
                                        // 利率优惠后执行利率
                                        cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(inrt.getFavort(), BigDecimal.ZERO).
                                                divide(BigDecimal.valueOf(100))));

                                        //mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
                                        //利率的最大范围值
                                        BigDecimal intrvlmax = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                                        //利率的最小范围值
                                        BigDecimal intrvlmin = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

                                        if (CommUtil.compare(cuusin, intrvlmin) < 0) {
                                            cuusin = intrvlmin;
                                        } else if (CommUtil.compare(cuusin, intrvlmax) > 0) {
                                            cuusin = intrvlmax;
                                        }
                                        //mod by leipeng   优惠后判断时候超出基础浮动范围20170220  end--

                                        //计算利息
                                        interest = pbpub.countInteresRateByAmounts(
                                                cuusin, acin.getBgindt(),
                                                fxac.getMatudt(), fxac.getOnlnbl(), acin.getTxbebs());

                                        CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
                                        calInterTax.setAcctno(acctno);
                                        calInterTax.setTranam(fxac.getOnlnbl());
                                        //calInterTax.setBegndt(fxac.getBgindt());
                                        calInterTax.setBegndt(acin.getBgindt());
                                        calInterTax.setEnddat(trandt);
                                        calInterTax.setCuusin(cuusin);
                                        calInterTax.setInstam(interest);
                                        calInterTax.setInbebs(acin.getTxbebs());

                                        InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(acin, calInterTax, false);
                                        intertax = interestAndTax.getIntxam();

                                        fxInst = fxInst.add(BusiTools.roundByCurrency(fxac.getCrcycd(), interest, null).subtract(intertax));

                                    } else {

                                        /*  利率确定日期为支取日时，需要重新获取当前执行利率  modify by liaojc in 20161214 */
                                        BigDecimal cuusin = inrt.getCuusin(); // 账户利率表执行利率

                                        IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                                        if (acin.getIntrdt() == E_INTRDT.DRAW) {

                                            intrEntity.setCrcycd(fxac.getCrcycd()); //币种
                                            intrEntity.setIntrcd(acin.getIntrcd()); //利率代码

                                            //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                                            intrEntity.setIncdtp(acin.getIncdtp()); //利率代码类型
                                            intrEntity.setIntrwy(acin.getIntrwy()); //靠档方式
                                            intrEntity.setTrandt(trandt);
                                            intrEntity.setDepttm(fxac.getDepttm());// 存期
                                            intrEntity.setBgindt(fxac.getBgindt()); //起始日期
                                            intrEntity.setEdindt(trandt); //结束日期
                                            intrEntity.setTranam(fxac.getOnlnbl()); //交易金额
                                            intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                                            intrEntity.setInbebs(acin.getTxbebs()); //计息基础
                                            intrEntity.setCorpno(fxac.getCorpno());//法人代码
                                            intrEntity.setBrchno(fxac.getBrchno());//机构

                                            intrEntity.setLevety(acin.getLevety());
                                            pbpub.countInteresRate(intrEntity);

                                            cuusin = intrEntity.getIntrvl(); //当前执行利率
                                            // 利率优惠后执行利率
                                            cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(inrt.getFavort(), BigDecimal.ZERO).
                                                    divide(BigDecimal.valueOf(100))));

                                            //mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
                                            //利率的最大范围值
                                            BigDecimal intrvlmax = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                                            //利率的最小范围值
                                            BigDecimal intrvlmin = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

                                            if (CommUtil.compare(cuusin, intrvlmin) < 0) {
                                                cuusin = intrvlmin;
                                            } else if (CommUtil.compare(cuusin, intrvlmax) > 0) {
                                                cuusin = intrvlmax;
                                            }
                                            //mod by leipeng   优惠后判断时候超出基础浮动范围20170220  end--
                                        }

                                        //计算利息
                                        interest = pbpub.countInteresRateByAmounts(
                                                cuusin, acin.getBgindt(),
                                                fxac.getMatudt(), fxac.getOnlnbl(), acin.getTxbebs());

                                        CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
                                        calInterTax.setAcctno(acctno);
                                        calInterTax.setTranam(fxac.getOnlnbl());
                                        //calInterTax.setBegndt(fxac.getBgindt());
                                        calInterTax.setBegndt(acin.getBgindt());
                                        calInterTax.setEnddat(trandt);
                                        calInterTax.setCuusin(cuusin);
                                        calInterTax.setInstam(interest);
                                        calInterTax.setInbebs(acin.getTxbebs());

                                        InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(acin, calInterTax, false);
                                        intertax = interestAndTax.getIntxam();

                                        fxInst = fxInst.add(BusiTools.roundByCurrency(fxac.getCrcycd(), interest, null)).subtract(intertax);
                                    }
                                } else if (acin.getInprwy() == E_IRRTTP.QD) {
                                    //计算利息，使用行内基准的活期利率
                                    //IntrPublicEntity intrMatuEntity = new IntrPublicEntity();
                                    IoPbIntrPublicEntity intrMatuEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                                    intrMatuEntity.setCrcycd(fxac.getCrcycd()); //币种
                                    intrMatuEntity.setIntrcd(tblKubinrt.getIntrcd()); //利率代码
                                    intrMatuEntity.setIncdtp(tblKubinrt.getIncdtp()); //利率代码类型
                                    intrMatuEntity.setIntrwy(tblKubinrt.getIntrwy()); //靠档方式
                                    intrMatuEntity.setDepttm(fxac.getDepttm()); //存期
                                    intrMatuEntity.setTrandt(trandt);
                                    intrMatuEntity.setBgindt(fxac.getBgindt()); //起始日期
                                    intrMatuEntity.setEdindt(fxac.getMatudt()); //结束日期
                                    intrMatuEntity.setTranam(tranam); //交易金额
                                    intrMatuEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                                    intrMatuEntity.setInbebs(acin.getTxbebs()); //计息基础
                                    intrMatuEntity.setCorpno(fxac.getCorpno());//法人代码
                                    intrMatuEntity.setBrchno(fxac.getBrchno());//机构

                                    intrMatuEntity.setLevety(acin.getLevety());
                                    if (acin.getIntrdt() == E_INTRDT.OPEN) {
                                        intrMatuEntity.setTrandt(fxac.getOpendt());
                                        intrMatuEntity.setTrantm("999999");
                                    }
                                    pbpub.countInteresRate(intrMatuEntity);

                                    //bigInstam = intrMatuEntity.getInamnt();

                                    //acbsin = intrMatuEntity.getBaseir(); //账户基准利率
                                    BigDecimal cuusin = intrMatuEntity.getIntrvl(); //当前执行利率

                                    // 利率优惠后执行利率
                                    cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubinrt.getFavort(), BigDecimal.ZERO).
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
                                    //mod by leipeng   优惠后判断时候超出基础浮动范围20170220  end--

                                    //计算利息
                                    interest = pbpub.countInteresRateByAmounts(
                                            cuusin, fxac.getBgindt(),
                                            fxac.getMatudt(), fxac.getOnlnbl(), acin.getTxbebs());

                                    CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
                                    calInterTax.setAcctno(acctno);
                                    calInterTax.setTranam(fxac.getOnlnbl());
                                    calInterTax.setBegndt(fxac.getBgindt());
                                    calInterTax.setEnddat(trandt);
                                    calInterTax.setCuusin(cuusin);
                                    calInterTax.setInstam(interest);
                                    calInterTax.setInbebs(acin.getTxbebs());

                                    InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(acin, calInterTax, false);
                                    intertax = interestAndTax.getIntxam();

                                    fxInst = fxInst.add(BusiTools.roundByCurrency(fxac.getCrcycd(), interest, null)).subtract(intertax);

                                } else {
                                    throw DpModuleError.DpstAcct.BNAS0201();
                                }

                                log.debug("到期支取，账号:[%s],账号类型:[%s],余额:[%s],利息:[%s],利息税:[%s]", fxac.getAcctno(), fxac.getAcsetp(), fxac.getOnlnbl(), interest, intertax);
                            } else { //到期后逾期支取

                                KubInrt tblKubinrt = KubInrtDao.selectOne_odb1(fxac.getAcctno(), true);
                                if (acin.getInprwy() == E_IRRTTP.NO) {
                                    if (E_IRCDTP.LAYER == acin.getIncdtp()) {

                                        IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                                        intrEntity.setCrcycd(fxac.getCrcycd()); //币种
                                        intrEntity.setIntrcd(acin.getIntrcd()); //利率代码

                                        //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                                        intrEntity.setIncdtp(acin.getIncdtp()); //利率代码类型
                                        intrEntity.setIntrwy(acin.getIntrwy()); //靠档方式
                                        intrEntity.setTrandt(trandt);
                                        intrEntity.setBgindt(fxac.getBgindt()); //起始日期
                                        intrEntity.setEdindt(trandt); //结束日期
                                        intrEntity.setTranam(fxac.getOnlnbl()); //交易金额
                                        intrEntity.setInbebs(acin.getTxbebs()); //计息基础
                                        intrEntity.setCorpno(fxac.getCorpno());//法人代码
                                        intrEntity.setBrchno(fxac.getBrchno());//机构

                                        intrEntity.setLevety(acin.getLevety());
                                        if (acin.getIntrdt() == E_INTRDT.OPEN) {
                                            intrEntity.setTrandt(fxac.getOpendt());
                                            intrEntity.setTrantm("999999");
                                        }
                                        pbpub.countInteresRate(intrEntity);

                                        BigDecimal cuusin = intrEntity.getIntrvl();
                                        // 利率优惠后执行利率
                                        cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(inrt.getFavort(), BigDecimal.ZERO).
                                                divide(BigDecimal.valueOf(100))));

                                        //mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
                                        //利率的最大范围值
                                        BigDecimal intrvlmax = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                                        //利率的最小范围值
                                        BigDecimal intrvlmin = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

                                        if (CommUtil.compare(cuusin, intrvlmin) < 0) {
                                            cuusin = intrvlmin;
                                        } else if (CommUtil.compare(cuusin, intrvlmax) > 0) {
                                            cuusin = intrvlmax;
                                        }
                                        //mod by leipeng   优惠后判断时候超出基础浮动范围20170220  end--

                                        //计算利息
                                        interest = pbpub.countInteresRateByAmounts(
                                                cuusin, fxac.getBgindt(),
                                                fxac.getMatudt(), fxac.getOnlnbl(), acin.getTxbebs());
                                        CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
                                        calInterTax.setAcctno(acctno);
                                        calInterTax.setTranam(fxac.getOnlnbl());
                                        calInterTax.setBegndt(fxac.getBgindt());
                                        calInterTax.setEnddat(fxac.getMatudt());
                                        calInterTax.setCuusin(cuusin);
                                        calInterTax.setInstam(interest);
                                        calInterTax.setInbebs(acin.getTxbebs());

                                        InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(acin, calInterTax, false);
                                        intertax = interestAndTax.getIntxam();

                                        fxInst = fxInst.add(BusiTools.roundByCurrency(fxac.getCrcycd(), interest, null)).subtract(intertax);

                                    } else {

                                        /*  利率确定日期为支取日时，需要重新获取当前执行利率  modify by liaojc in 20161214 */
                                        BigDecimal cuusin = inrt.getCuusin(); // 账户利率表执行利率

                                        IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                                        if (acin.getIntrdt() == E_INTRDT.DRAW) {

                                            intrEntity.setCrcycd(fxac.getCrcycd()); //币种
                                            intrEntity.setIntrcd(acin.getIntrcd()); //利率代码

                                            //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                                            intrEntity.setIncdtp(acin.getIncdtp()); //利率代码类型
                                            intrEntity.setIntrwy(acin.getIntrwy()); //靠档方式
                                            intrEntity.setTrandt(trandt);
                                            intrEntity.setDepttm(fxac.getDepttm());// 存期
                                            intrEntity.setBgindt(fxac.getBgindt()); //起始日期
                                            intrEntity.setEdindt(trandt); //结束日期
                                            intrEntity.setTranam(fxac.getOnlnbl()); //交易金额
                                            intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                                            intrEntity.setInbebs(acin.getTxbebs()); //计息基础
                                            intrEntity.setCorpno(fxac.getCorpno());//法人代码
                                            intrEntity.setBrchno(fxac.getBrchno());//机构

                                            intrEntity.setLevety(acin.getLevety());
                                            pbpub.countInteresRate(intrEntity);

                                            cuusin = intrEntity.getIntrvl(); //当前执行利率
                                            // 利率优惠后执行利率
                                            cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(inrt.getFavort(), BigDecimal.ZERO).
                                                    divide(BigDecimal.valueOf(100))));

                                            //mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
                                            //利率的最大范围值
                                            BigDecimal intrvlmax = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                                            //利率的最小范围值
                                            BigDecimal intrvlmin = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

                                            if (CommUtil.compare(cuusin, intrvlmin) < 0) {
                                                cuusin = intrvlmin;
                                            } else if (CommUtil.compare(cuusin, intrvlmax) > 0) {
                                                cuusin = intrvlmax;
                                            }
                                            //mod by leipeng   优惠后判断时候超出基础浮动范围20170220  end--
                                        }

                                        //计算利息
                                        interest = pbpub.countInteresRateByAmounts(
                                                cuusin, fxac.getBgindt(),
                                                fxac.getMatudt(), fxac.getOnlnbl(), acin.getTxbebs());

                                        CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
                                        calInterTax.setAcctno(acctno);
                                        calInterTax.setTranam(fxac.getOnlnbl());
                                        calInterTax.setBegndt(fxac.getBgindt());
                                        calInterTax.setEnddat(fxac.getMatudt());
                                        calInterTax.setCuusin(cuusin);
                                        calInterTax.setInstam(interest);
                                        calInterTax.setInbebs(acin.getTxbebs());

                                        InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(acin, calInterTax, false);
                                        intertax = interestAndTax.getIntxam();

                                        fxInst = fxInst.add(BusiTools.roundByCurrency(fxac.getCrcycd(), interest, null)).subtract(intertax);
                                    }
                                } else if (acin.getInprwy() == E_IRRTTP.QD) {
                                    //计算利息，使用行内基准的活期利率
                                    //IntrPublicEntity intrMatuEntity = new IntrPublicEntity();
                                    IoPbIntrPublicEntity intrMatuEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                                    intrMatuEntity.setCrcycd(fxac.getCrcycd()); //币种
                                    intrMatuEntity.setIntrcd(tblKubinrt.getIntrcd()); //利率代码
                                    intrMatuEntity.setIncdtp(tblKubinrt.getIncdtp()); //利率代码类型
                                    intrMatuEntity.setIntrwy(tblKubinrt.getIntrwy()); //靠档方式
                                    intrMatuEntity.setDepttm(fxac.getDepttm()); //存期
                                    intrMatuEntity.setTrandt(trandt);
                                    intrMatuEntity.setBgindt(fxac.getBgindt()); //起始日期
                                    intrMatuEntity.setEdindt(fxac.getMatudt()); //结束日期
                                    intrMatuEntity.setTranam(tranam); //交易金额
                                    intrMatuEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                                    intrMatuEntity.setInbebs(acin.getTxbebs()); //计息基础
                                    intrMatuEntity.setCorpno(fxac.getCorpno());//法人代码
                                    intrMatuEntity.setBrchno(fxac.getBrchno());//机构

                                    intrMatuEntity.setLevety(acin.getLevety());
                                    if (acin.getIntrdt() == E_INTRDT.OPEN) {
                                        intrMatuEntity.setTrandt(fxac.getOpendt());
                                        intrMatuEntity.setTrantm("999999");
                                    }
                                    pbpub.countInteresRate(intrMatuEntity);

                                    //bigInstam = intrMatuEntity.getInamnt();

                                    //acbsin = intrMatuEntity.getBaseir(); //账户基准利率
                                    BigDecimal cuusin = intrMatuEntity.getIntrvl(); //当前执行利率

                                    // 利率优惠后执行利率
                                    cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubinrt.getFavort(), BigDecimal.ZERO).
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
                                    //mod by leipeng   优惠后判断时候超出基础浮动范围20170220  end--

                                    //计算利息
                                    interest = pbpub.countInteresRateByAmounts(
                                            cuusin, fxac.getBgindt(),
                                            fxac.getMatudt(), fxac.getOnlnbl(), acin.getTxbebs());

                                    CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
                                    calInterTax.setAcctno(acctno);
                                    calInterTax.setTranam(fxac.getOnlnbl());
                                    calInterTax.setBegndt(fxac.getBgindt());
                                    calInterTax.setEnddat(fxac.getMatudt());
                                    calInterTax.setCuusin(cuusin);
                                    calInterTax.setInstam(interest);
                                    calInterTax.setInbebs(acin.getTxbebs());

                                    InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(acin, calInterTax, false);
                                    intertax = interestAndTax.getIntxam();

                                    fxInst = fxInst.add(BusiTools.roundByCurrency(fxac.getCrcycd(), interest, null)).subtract(intertax);

                                } else {
                                    throw DpModuleError.DpstAcct.BNAS0201();
                                }

                                log.debug("逾期支取，账号:[%s],账号类型:[%s],余额:[%s],利息:[%s]", fxac.getAcctno(), fxac.getAcsetp(), fxac.getOnlnbl(), interest);

                                /* 计算逾期部分利息，按活期利率计算 */
                                tblKnbDfir = KnbDfirDao.selectOne_odb1(fxac.getAcctno(), E_TEARTP.OVTM, false);

                                drintpName = E_TEARTP.OVTM.getLongName();

                                // 检查违约支取利息定义信息

                                if (CommUtil.isNull(tblKnbDfir)) {
                                    throw DpModuleError.DpstAcct.BNAS1209(fxac.getProdcd(), drintpName);
                                }

                                //计算利息，使用行内基准的活期利率
                                IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                                intrEntity.setCrcycd(fxac.getCrcycd()); //币种
                                intrEntity.setIntrcd(tblKnbDfir.getIntrcd()); //利率代码
                                //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期

                                if (E_IRCDTP.BASE == tblKnbDfir.getIncdtp() ||
                                        E_IRCDTP.Reference == tblKnbDfir.getIncdtp()) {
                                    intrEntity.setDepttm(E_TERMCD.T000);
                                } else if (E_IRCDTP.LAYER == tblKnbDfir.getIncdtp()) {
                                    if (tblKnbDfir.getInclfg() == E_YES___.YES) {
                                        intrEntity.setIntrwy(tblKnbDfir.getIntrwy()); //靠档方式
                                    }
                                }

                                intrEntity.setIncdtp(tblKnbDfir.getIncdtp()); //利率代码类型
                                intrEntity.setTrandt(trandt);
                                intrEntity.setBgindt(fxac.getMatudt()); //起始日期
                                intrEntity.setEdindt(trandt); //结束日期
                                intrEntity.setTranam(fxac.getOnlnbl()); //交易金额
                                intrEntity.setInbebs(tblKnbDfir.getBsinrl()); //计息基础
                                intrEntity.setCorpno(fxac.getCorpno());//法人代码
                                intrEntity.setBrchno(fxac.getBrchno());//机构

                                intrEntity.setLevety(tblKnbDfir.getLevety());
                                if (tblKnbDfir.getIntrdt() == E_INTRDT.OPEN) {
                                    intrEntity.setTrandt(fxac.getOpendt());
                                    intrEntity.setTrantm("999999");
                                }
                                pbpub.countInteresRate(intrEntity);
                                interest = BusiTools.roundByCurrency(fxac.getCrcycd(), intrEntity.getInamnt(), null);

                                CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
                                calInterTax.setAcctno(acctno);
                                calInterTax.setTranam(fxac.getOnlnbl());
                                calInterTax.setBegndt(fxac.getMatudt());
                                calInterTax.setEnddat(trandt);
                                calInterTax.setCuusin(intrEntity.getIntrvl());
                                calInterTax.setInstam(interest);
                                calInterTax.setInbebs(tblKnbDfir.getBsinrl());

                                InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(acin, calInterTax, false);
                                intertax = interestAndTax.getIntxam();

                                fxInst = fxInst.add(interest).subtract(intertax);
                                log.debug("逾期支取逾期部分，账号:[%s],账号类型:[%s],余额:[%s],利息:[%s],利息税:[%s]", fxac.getAcctno(), fxac.getAcsetp(), fxac.getOnlnbl(), interest, intertax);
                            }
                        }

                    }

                }
            }
            List<KnaFxacDrdl> lstKnaFxacDrdl = KnaFxacDrdlDao.selectAll_odb2(fxac.getAcctno(), fxac.getCrcycd(), E_ACCTST.NORMAL, false);
            interest = BigDecimal.ZERO;
            for (KnaFxacDrdl tblKnaFxacDrdl : lstKnaFxacDrdl) {
                // 支取后更新状态为关闭
                interest = interest.add(tblKnaFxacDrdl.getIntram()).subtract(tblKnaFxacDrdl.getIntxam());
                fxInst = fxInst.add(interest);
            }

            IoDpClsChkOTPdlist pdlst = SysUtil.getInstance(IoDpClsChkOTPdlist.class);
            pdlst.setPddpfg(fxac.getPddpfg());
            pdlst.setProdcd(fxac.getProdcd());

            chkot.getPdlist().add(pdlst);

            log.debug("明细汇总支取部分利息，账号:[%s],账号类型:[%s],余额:[%s],利息:[%s]", fxac.getAcctno(), fxac.getAcsetp(), fxac.getOnlnbl(), interest);
        }

        totalintr = fxInst.add(cuInst);
        totalbal = fxbal.add(cubal);

        chkot.setFixddp(fxbal);
        chkot.setDppdbl(dppbbal);
        chkot.setIntrvl(totalintr);
        chkot.setTotlam(totalbal.add(totalintr));
        //		CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
        log.debug("电子账号:[%s],子账号:[%s],余额:[%s],利息:[%s],销户金额:[%s]", custac, acctno, totalbal, totalintr, chkot.getTotlam());

        return chkot;
    }

}
