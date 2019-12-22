package cn.sunline.ltts.busi.dptran.trans;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.acct.DpInterestAndTax;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbDfir;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbDfirDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.CalInterTax;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CAINPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TEARTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 
 * @author songkailei
 * 
 * 
 */
public class qrdfir {

    private static BizLog log = BizLogUtil.getBizLog(qrdfir.class);

    public static void Dpqrdfir(final cn.sunline.ltts.busi.dptran.trans.intf.Qrdfir.Input input, final cn.sunline.ltts.busi.dptran.trans.intf.Qrdfir.Property property,
            final cn.sunline.ltts.busi.dptran.trans.intf.Qrdfir.Output output) {

        String custac = input.getCustac();//电子账号
        String acctno = input.getAcctno();//负债子账号
        BigDecimal tranam = input.getTranam();//交易金额
        String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//交易日期
        BigDecimal intertax = BigDecimal.ZERO;//利息税
        BigDecimal cuusin = BigDecimal.ZERO;//利率
        BigDecimal calmon = BigDecimal.ZERO;//利息
        //BigDecimal interest = BigDecimal.ZERO; //利息变量		

        //非空校验
        if (CommUtil.isNull(custac)) {
            throw DpModuleError.DpstProd.BNAS0926();
        }

        if (CommUtil.isNull(acctno)) {
            throw DpModuleError.DpstProd.BNAS1759();
        }

        if (CommUtil.isNull(tranam)) {
            throw DpModuleError.DpstAcct.BNAS0623();
        }

        //检查输入的电子账号ID是否和输入的存款子账号对应的电子账号iD匹配
        KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(input.getAcctno(), true);
        if (!CommUtil.equals(input.getCustac(), tblKnaFxac.getCustac())) {
            throw DpModuleError.DpstAcct.BNAS0903();
        }

        KnbAcin acin = KnbAcinDao.selectOne_odb1(acctno, false);
        if (CommUtil.isNull(acin)) {
            throw DpModuleError.DpstAcct.BNAS0710();
        }

        KubInrt inrt = KubInrtDao.selectOne_odb1(acctno, false);
        if (CommUtil.isNull(inrt)) {
            throw DpModuleError.DpstAcct.BNAS0174();
        }

        IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
        KnbDfir tblKnbDfir = SysUtil.getInstance(KnbDfir.class);

        //根据金额判断是提前支取还是销户
        if (CommUtil.compare(tranam, tblKnaFxac.getOnlnbl()) > 0) {
            throw DpModuleError.DpstComm.BNAS0177();
        }

        //销户
        if (CommUtil.compare(tranam, tblKnaFxac.getOnlnbl()) == 0) {
            tblKnbDfir = KnbDfirDao.selectOne_odb1(acctno, E_TEARTP.TQXH, false);
            String drintpName = E_TEARTP.TQXH.getLongName();

            //检查违约支取利息定义信息
            if (CommUtil.isNull(tblKnbDfir)) {
                throw DpModuleError.DpstComm.BNAS1209(tblKnaFxac.getProdcd(), drintpName);
            }

            //计算利息，使用行内基准的活期利率
            IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
            intrEntity.setCrcycd(tblKnaFxac.getCrcycd()); //币种
            intrEntity.setIntrcd(tblKnbDfir.getIntrcd()); //利率代码
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
            //由于存在丰收瑞利定期产品（定期每年结息）    修改起始日期为上次结息日期 modify by songkl 2017/07/17
            //		intrEntity.setBgindt(fxac.getBgindt()); //起始日期
            intrEntity.setBgindt(acin.getBgindt()); //起始日期
            intrEntity.setEdindt(trandt); //结束日期
            intrEntity.setTranam(tranam); //交易金额
            intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
            intrEntity.setInbebs(tblKnbDfir.getBsinrl()); //计息基础
            intrEntity.setCorpno(tblKnaFxac.getCorpno());//法人代码
            intrEntity.setBrchno(tblKnaFxac.getBrchno());//机构

            intrEntity.setLevety(tblKnbDfir.getLevety());
            if (tblKnbDfir.getIntrdt() == E_INTRDT.OPEN) {
                intrEntity.setTrandt(tblKnaFxac.getOpendt());
                intrEntity.setTrantm("999999");
            }

            
            pbpub.countInteresRate(intrEntity);
            //获取利率
            cuusin = intrEntity.getIntrvl();

            /*
            interest = pbpub.countInteresRateByAmounts(
                    cuusin, acin.getBgindt(),
                    tblKnaFxac.getMatudt(), tblKnaFxac.getOnlnbl(), acin.getTxbebs());
                    */

            CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
            calInterTax.setAcctno(acctno);
            calInterTax.setTranam(tblKnaFxac.getOnlnbl());

            //由于存在丰收瑞利定期产品（定期每年结息）    修改起始日期为上次结息日期 modify by songkl 2017/07/17
            calInterTax.setBegndt(acin.getBgindt());
            calInterTax.setEnddat(trandt);
            calInterTax.setCuusin(intrEntity.getIntrvl());
            calInterTax.setInstam(intrEntity.getInamnt());
            calInterTax.setInbebs(tblKnbDfir.getBsinrl());

            InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(acin, calInterTax, false);
            intertax = interestAndTax.getIntxam();

            calmon = calmon.add(BusiTools.roundByCurrency(tblKnaFxac.getCrcycd(), intrEntity.getInamnt(), BaseEnumType.E_CARRTP.ROUND)).subtract(intertax);

        } else {
            //提前支取
            // 特殊处理通知存款违约支取
            if (tblKnaFxac.getDebttp() == E_DEBTTP.DP2506) {

                if (E_TERMCD.T107 == tblKnaFxac.getDepttm()) {
                    if (CommUtil.compare(DateTools2.calDays(tblKnaFxac.getBgindt(), trandt, 0, 0) - 7, 0) < 0) {

                        tblKnaFxac.setMatudt(DateTimeUtil.dateAdd("day", tblKnaFxac.getBgindt(), 7));
                    } else {
                        tblKnaFxac.setMatudt(trandt);
                    }
                } else if (E_TERMCD.T101 == tblKnaFxac.getDepttm()) {
                    if (CommUtil.compare(DateTools2.calDays(tblKnaFxac.getBgindt(), trandt, 0, 0) - 1, 0) < 0) {

                        tblKnaFxac.setMatudt(DateTimeUtil.dateAdd("day", tblKnaFxac.getBgindt(), 1));
                    } else {
                        tblKnaFxac.setMatudt(trandt);
                    }
                }

            }

            if (tblKnaFxac.getDebttp() == E_DEBTTP.DP2505) { //定活两便
                //计算利息，使用行内基准的活期利率
                IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                intrEntity.setCrcycd(tblKnaFxac.getCrcycd()); //币种
                intrEntity.setIntrcd(acin.getIntrcd()); //利率代码

                //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                intrEntity.setIncdtp(acin.getIncdtp()); //利率代码类型
                intrEntity.setIntrwy(acin.getIntrwy()); //靠档方式
                intrEntity.setTrandt(trandt);
                intrEntity.setTranam(tblKnaFxac.getOnlnbl()); //交易金额
                intrEntity.setInbebs(acin.getTxbebs()); //计息基础
                intrEntity.setCorpno(tblKnaFxac.getCorpno());//法人代码
                intrEntity.setBrchno(tblKnaFxac.getBrchno());//机构
                intrEntity.setLevety(acin.getLevety());
                if (acin.getIntrdt() == E_INTRDT.OPEN) {
                    intrEntity.setTrandt(tblKnaFxac.getOpendt());
                    intrEntity.setTrantm("999999");
                }

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
                intrEntity.setBgindt(tblKnaFxac.getBgindt()); //起始日期
                intrEntity.setEdindt(trandt); //结束日期

                pbpub.countInteresRate(intrEntity);
                BigDecimal actsin = intrEntity.getIntrvl();// 实际执行利率

                // 利率优惠后执行利率
                actsin = actsin.add(actsin.multiply(CommUtil.nvl(inrt.getFavort(), BigDecimal.ZERO).
                        divide(BigDecimal.valueOf(100))));
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
                        accsin, tblKnaFxac.getBgindt(),
                        trandt, tblKnaFxac.getOnlnbl(), acin.getTxbebs());

                CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
                calInterTax.setAcctno(acctno);
                calInterTax.setTranam(tranam);
                calInterTax.setBegndt(tblKnaFxac.getBgindt());
                calInterTax.setEnddat(trandt);
                calInterTax.setCuusin(accsin);
                calInterTax.setInstam(bigInstam);
                calInterTax.setInbebs(acin.getTxbebs());

                InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(acin, calInterTax, false);
                intertax = interestAndTax.getIntxam();

                calmon = calmon.add(BusiTools.roundByCurrency(tblKnaFxac.getCrcycd(), bigInstam, BaseEnumType.E_CARRTP.ROUND)).subtract(intertax);
                log.debug("定活两便，账号:[%s],账号类型:[%s],余额:[%s],利息:[%s],利息税:[%s]", tblKnaFxac.getAcctno(), tblKnaFxac.getAcsetp(), tblKnaFxac.getOnlnbl(), bigInstam, intertax);
            } else {
                //其他存款产品  暂只支持提前支取的查询
                /* 按活期利率计算 */
                tblKnbDfir = KnbDfirDao.selectOne_odb1(tblKnaFxac.getAcctno(), E_TEARTP.TQZQ, false);

                String drintpName = E_TEARTP.TQZQ.getLongName();

                //检查违约支取利息定义信息
                if (CommUtil.isNull(tblKnbDfir)) {
                    throw DpModuleError.DpstComm.BNAS1209(tblKnaFxac.getProdcd(), drintpName);
                }

                //计算利息，使用行内基准的活期利率
                IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                intrEntity.setCrcycd(tblKnaFxac.getCrcycd()); //币种
                intrEntity.setIntrcd(tblKnbDfir.getIntrcd()); //利率代码
                if (E_IRCDTP.BASE == tblKnbDfir.getIncdtp() ||
                        E_IRCDTP.Reference == tblKnbDfir.getIncdtp()) {
                    intrEntity.setDepttm(E_TERMCD.T000);
                } else if (E_IRCDTP.LAYER == tblKnbDfir.getIncdtp()) {
                    if (tblKnbDfir.getInclfg() == E_YES___.YES) {
                        intrEntity.setIntrwy(tblKnbDfir.getIntrwy()); //靠档方式
                    }
                }

                //				intrEntity.setDepttm(fxac.getDepttm()); //存期/
                intrEntity.setIncdtp(tblKnbDfir.getIncdtp()); //利率代码类型
                intrEntity.setTrandt(trandt);
                intrEntity.setBgindt(acin.getBgindt()); //起始日期
                intrEntity.setEdindt(trandt); //结束日期
                intrEntity.setTranam(tranam); //交易金额
                intrEntity.setInbebs(tblKnbDfir.getBsinrl()); //计息基础
                intrEntity.setCorpno(tblKnaFxac.getCorpno());//法人代码
                intrEntity.setBrchno(tblKnaFxac.getBrchno());//机构

                intrEntity.setLevety(tblKnbDfir.getLevety());
                if (tblKnbDfir.getIntrdt() == E_INTRDT.OPEN) {
                    intrEntity.setTrandt(tblKnaFxac.getOpendt());
                    intrEntity.setTrantm("999999");
                }

                pbpub.countInteresRate(intrEntity);
                //获取利率
                cuusin = intrEntity.getIntrvl();

                CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
                calInterTax.setAcctno(acctno);
                calInterTax.setTranam(tranam);
                calInterTax.setBegndt(acin.getBgindt());
                calInterTax.setEnddat(trandt);
                calInterTax.setCuusin(intrEntity.getIntrvl());
                calInterTax.setInstam(intrEntity.getInamnt());
                calInterTax.setInbebs(tblKnbDfir.getBsinrl());

                InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(acin, calInterTax, false);
                intertax = interestAndTax.getIntxam();

                calmon = calmon.add(BusiTools.roundByCurrency(tblKnaFxac.getCrcycd(), intrEntity.getInamnt(), BaseEnumType.E_CARRTP.ROUND)).subtract(intertax);
                log.debug("提前支取违约，账号:[%s],账号类型:[%s],余额:[%s],利息:[%s],利息税:[%s]", tblKnaFxac.getAcctno(), tblKnaFxac.getAcsetp(), tblKnaFxac.getOnlnbl(), intrEntity.getInamnt(),
                        intertax);
            }
        }

        output.setCalmon(calmon);
        output.setCuusin(cuusin);

    }

}
