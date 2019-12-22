package cn.sunline.ltts.busi.dp.dayend;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbdp;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbdpDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDetl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDetlDao;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInstCabrOut;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInstPrcIn;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AVBLDT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CAINPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CYCLTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IBAMMD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_REPRWY;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 
 * @author renjinghua
 *         <p>
 *         <li>2015年4月10日 下午21:00</li>
 *         <li>计算负债利息相关</li>
 *         </p>
 */
public class DpInterest {

    private final static BizLog bizlog = BizLogUtil.getBizLog(BizLog.class);

    /**
     * @author renjinghua
     *         <p>
     *         <li>2015年4月11日 下午 14:50</li>
     *         <li>计算负债账户日终计提利息，及更新负债账户计息信息</li>
     *         </p>
     * 
     * @param tblKnbAcin 负债账户计息信息
     * @param tblKubInrt 负债账户利率信息
     * @param cplDpInstPrcIn 负债账户计提处理输入
     * @return 日终计提利息
     * 
     */
    public static Map<String, BigDecimal> prcInstMain(KnbAcin tblKnbAcin, KubInrt tblKubInrt, DpInstPrcIn cplDpInstPrcIn) {
        //返回结果
        Map<String, BigDecimal> mapCabrin = new HashMap<String, BigDecimal>();
        //计提天数
        //int cabrDays = 0;
        //计提利息
        BigDecimal bigCabrin = BigDecimal.ZERO;
        //计提利息税
        BigDecimal bigIntxam = BigDecimal.ZERO;
        //负债账号
        String acctno = tblKnbAcin.getAcctno();
        //交易日期
        String trandt = cplDpInstPrcIn.getTrandt();
        //上次交易日期
        String lstrdt = cplDpInstPrcIn.getLstrdt();
        //币种
        String crcycd = cplDpInstPrcIn.getCrcycd();
        //活期总积数
        BigDecimal totalAcmltn = BigDecimal.ZERO;
        //执行利率
        BigDecimal intrvl = BigDecimal.ZERO;
        //活期账户
        //BigDecimal curIntTx = BigDecimal.ZERO;

        IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);

        if (CommUtil.equals(tblKnbAcin.getPddpfg().getValue(), E_FCFLAG.CURRENT.getValue())) {
            //由于行内利率会出现变化，固活期需要分段计息
            BigDecimal bigCurrCabrin = BigDecimal.ZERO; //当前利率下计提利息
            BigDecimal bigBeforCabrin = BigDecimal.ZERO; //变更前利率下计提利息

            //BigDecimal bigCurrIntxam = BigDecimal.ZERO; //当前利率下计提利息税
            //BigDecimal bigBeforIntxam = BigDecimal.ZERO; //变更前利率下计提利息税

            BigDecimal cutmam = tblKnbAcin.getCutmam(); //本期积数
            BigDecimal avgtranam = BigDecimal.ZERO; //平均余额

            //循环负债账户计息明细表，获取历史利率下的所有需计提的信息
            List<KnbIndl> tblKnbIndls = KnbIndlDao.selectAll_odb4(acctno, E_INDLST.YOUX, false);

            if (CommUtil.isNotNull(tblKnbIndls)) {
                for (KnbIndl tblKnbIndl : tblKnbIndls) {
                    totalAcmltn = totalAcmltn.add(tblKnbIndl.getAcmltn());
                    //计算活期分段计提利息
                    BigDecimal bigOneCabrin = pbpub.countInteresRateByBase(tblKnbIndl.getCuusin(), tblKnbIndl.getAcmltn());
                    //计算活期分段计提利息税
                    //BigDecimal bigOneIntxam = bigOneCabrin.multiply(tblKnbIndl.getCatxrt());

                    //更新分段利息表的相关计提信息
                    tblKnbIndl.setRlintr(bigOneCabrin); //分段计提利息，无此字段，赋值给实际利息发生额
                    tblKnbIndl.setIntrdt(lstrdt); //计息日期
                    tblKnbIndl.setIntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //计息流水
                    tblKnbIndl.setLsinoc(E_INDLTP.CAIN); //上次利息操作代码
                    KnbIndlDao.updateOne_odb1(tblKnbIndl);

                    //当前段计提利息加上分段计提利息
                    bigBeforCabrin = bigBeforCabrin.add(bigOneCabrin);

                }
            }

            //实际积数
            BigDecimal realCutmam = DpPublic.calRealTotalAmt(cutmam, cplDpInstPrcIn.getOnlnbl(), cplDpInstPrcIn.getTrandt(), tblKnbAcin.getLaamdt());

            totalAcmltn = totalAcmltn.add(realCutmam); //加实际积数	

            // 如果不为后段调整则用重新计算积数
            if (!CommUtil.equals("1", tblKnbAcin.getInprwy().getValue())) {
	            if (E_REPRWY.BACK == tblKnbAcin.getReprwy()) {
	                //不处理''
	            } else if (E_REPRWY.ALL == tblKnbAcin.getReprwy()) {
	                realCutmam = totalAcmltn;
	                bigBeforCabrin = BigDecimal.ZERO;
	            } else {
	                throw DpModuleError.DpstComm.BNAS1591();
	            }
            }
            if (E_IRCDTP.LAYER == tblKnbAcin.getIncdtp()) {

                int days = 1; //计提天数					

                if (CommUtil.compare(tblKnbAcin.getInammd(), E_IBAMMD.ACCT) == 0) { //账户余额				

                    throw DpModuleError.DpstComm.BNAS1592();

                } else if (CommUtil.compare(tblKnbAcin.getInammd(), E_IBAMMD.AVG) == 0) {//平均余额
                    days = calAvgDays(tblKnbAcin.getIrwptp(), tblKnbAcin.getBldyca(), tblKnbAcin.getTxbefr(),
                            tblKnbAcin.getLcindt(), tblKnbAcin.getNcindt(), cplDpInstPrcIn.getTrandt());
                    /*
                    if (CommUtil.equals(totalAcmltn, BigDecimal.ZERO)) {
                        avgtranam = cplDpInstPrcIn.getOnlnbl();//平均余额
                    } else {
                        avgtranam = totalAcmltn.divide(BigDecimal.valueOf(days), 2, BigDecimal.ROUND_HALF_UP);//平均余额
                    }
                    */
                    //平均余额
                    avgtranam = totalAcmltn.divide(BigDecimal.valueOf(days), 2, BigDecimal.ROUND_HALF_UP);

                    IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                    intrEntity.setCorpno(tblKnbAcin.getCorpno()); //法人代码
                    intrEntity.setBrchno(cplDpInstPrcIn.getBrchno());//机构号
                    intrEntity.setTranam(avgtranam);//交易金额
                    intrEntity.setTrandt(cplDpInstPrcIn.getLstrdt());//交易日期
                    intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码 
                    intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                    intrEntity.setCrcycd(cplDpInstPrcIn.getCrcycd());//币种
                    intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                    intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                    intrEntity.setCainpf(E_CAINPF.T1); //计息规则
                    intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起息日期
                    intrEntity.setEdindt(cplDpInstPrcIn.getTrandt()); //止息日

                    intrEntity.setLevety(tblKnbAcin.getLevety());
                    if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
                        intrEntity.setTrandt(tblKnbAcin.getOpendt());
                        intrEntity.setTrantm("999999");
                    }
                    pbpub.countInteresRate(intrEntity);

                    intrvl = intrEntity.getIntrvl();

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
                    //mod by leipeng   优惠后判断时候超出基础浮动范围20170220  end--

                    bigCurrCabrin = pbpub.countInteresRateByBase(intrvl, realCutmam);
                    //bigCurrCabrin = BusiTools.roundByCurrency(crcycd, bigCurrCabrin); //按币种取有效值
                    //取当前税率计算利息税
                    //					bigCurrIntxam = bigCurrCabrin.multiply(curIntTx);//利息税
                    //bigCurrIntxam = BusiTools.roundByCurrency(crcycd, bigCurrIntxam); //按币种取有效值
                    
                } else if(CommUtil.compare(tblKnbAcin.getInammd(), E_IBAMMD.SUM) == 0) {//积数
                    /** add by huangwh 20181121 start  积数靠档 */
                    
                    IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                    intrEntity.setCorpno(tblKnbAcin.getCorpno()); //法人代码
                    intrEntity.setBrchno(cplDpInstPrcIn.getBrchno());//机构号
                    intrEntity.setTranam(totalAcmltn);/** 交易金额   = 活期总积数 */
                    intrEntity.setTrandt(cplDpInstPrcIn.getLstrdt());//交易日期
                    intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码 
                    intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                    intrEntity.setCrcycd(cplDpInstPrcIn.getCrcycd());//币种
                    intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                    intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                    intrEntity.setCainpf(E_CAINPF.T1); //计息规则
                    intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起息日期
                    intrEntity.setEdindt(cplDpInstPrcIn.getTrandt()); //止息日

                    intrEntity.setLevety(tblKnbAcin.getLevety());
                    if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
                        intrEntity.setTrandt(tblKnbAcin.getOpendt());
                        intrEntity.setTrantm("999999");
                    }
                    pbpub.countInteresRate(intrEntity);

                    intrvl = intrEntity.getIntrvl();

                    // 利率优惠后执行利率
                    intrvl = intrvl.add(intrvl.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                            divide(BigDecimal.valueOf(100))));

                    //mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
                    //利率的最大范围值
                    BigDecimal intrvlmax = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                    //利率的最小范围值
                    BigDecimal intrvlmin = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
                    //若优惠后的利率小于最小可取利率则赋值为最小可取利率，若优惠后的利率大于最大可取利率则赋值为最大可取利率。
                    if (CommUtil.compare(intrvl, intrvlmin) < 0) {
                        intrvl = intrvlmin;
                    } else if (CommUtil.compare(intrvl, intrvlmax) > 0) {
                        intrvl = intrvlmax;
                    }
                    //根据积数计算利息
                    bigCurrCabrin = pbpub.countInteresRateByBase(intrvl, realCutmam);

                    /** add by huangwh 20181121 end */
                }else {
                    throw DpModuleError.DpstComm.BNAS1593();
                }

            } else if (E_IRCDTP.Reference == tblKnbAcin.getIncdtp() || E_IRCDTP.BASE == tblKnbAcin.getIncdtp()) { //参考利率

                // modify 20161221 liaojc 当利率确定日期为支取日，重新获取当前执行利率
                BigDecimal cuusin = tblKubInrt.getCuusin(); // 账户利率表执行利率

                if (tblKnbAcin.getIntrdt() == E_INTRDT.DRAW) {

                    IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                    intrEntity.setCrcycd(cplDpInstPrcIn.getCrcycd()); //币种
                    intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码

                    //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                    intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                    intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                    intrEntity.setTrandt(trandt);
                    intrEntity.setDepttm(E_TERMCD.T000);// 存期
                    intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起始日期
                    intrEntity.setEdindt(trandt); //结束日期
                    intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                    intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                    intrEntity.setCorpno(tblKnbAcin.getCorpno());//法人代码
                    intrEntity.setBrchno(cplDpInstPrcIn.getBrchno());//机构

                    intrEntity.setLevety(tblKnbAcin.getLevety());
                    pbpub.countInteresRate(intrEntity);

                    cuusin = intrEntity.getIntrvl(); //当前执行利率

                    //					//获取离开户日最近的税率
                    //					BigDecimal befIntTx = null;
                    //					if(CommUtil.isNotNull(tblKnbAcin.getTaxecd())){
                    //						befIntTx = SysUtil.getInstance(IoPbInRaSelSvc.class).getNearTxTate(tblKnbAcin.getTaxecd(), trandt).getTaxrat();
                    //					}
                    //					if(CommUtil.isNull(befIntTx)){
                    //						befIntTx = BigDecimal.ZERO;
                    //					}										
                    //					curIntTx = befIntTx;

                    // 利率优惠后执行利率
                    cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
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

                intrvl = cuusin;

                bigCurrCabrin = pbpub.countInteresRateByBase(cuusin, realCutmam);
                //bigCurrCabrin = BusiTools.roundByCurrency(crcycd, bigCurrCabrin); //按币种取有效值
                //利息税
                //				bigCurrIntxam = bigCurrCabrin.multiply(curIntTx);//利息税
                //bigCurrIntxam = BusiTools.roundByCurrency(crcycd, bigCurrIntxam); //按币种取有效值

            } else {
                throw DpModuleError.DpstComm.BNAS1081();
            }

            //活期账户计提利息等于当前利率下计提利息 + 变更前利率下计提利息
            bigCabrin = BusiTools.roundByCurrency(crcycd, bigCabrin.add(bigCurrCabrin).add(bigBeforCabrin), null);

        } else {//定期账户
            BigDecimal bigTradFixInstam = BigDecimal.ZERO; //传统定期计提利息
            //BigDecimal bigTradFixIntxam = BigDecimal.ZERO; //传统定期计提利息税
            BigDecimal bigCapadpInstam = BigDecimal.ZERO; //智能储蓄计提利息
            //传统定期
            if (CommUtil.equals(E_YES___.NO.getValue(), tblKnbAcin.getDetlfg().getValue())) {

                // 获取定期负债账户信息
                KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(acctno, true);

                //计算计提利息

                if (E_IRCDTP.Reference == tblKnbAcin.getIncdtp() || E_IRCDTP.BASE == tblKnbAcin.getIncdtp()) { //参考利率
                    //获取账户利率信息
                    tblKubInrt = KubInrtDao.selectOne_odb1(acctno, true);

                    // modify 20161221 liaojc 当利率确定日期为支取日，重新获取当前执行利率
                    BigDecimal cuusin = tblKubInrt.getCuusin(); // 账户利率表执行利率

                    if (tblKnbAcin.getIntrdt() == E_INTRDT.DRAW) {

                        IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                        intrEntity.setCrcycd(cplDpInstPrcIn.getCrcycd()); //币种
                        intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码

                        //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                        intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                        intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                        intrEntity.setTrandt(trandt);
                        intrEntity.setDepttm(tblKnaFxac.getDepttm());// 存期
                        intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起始日期
                        intrEntity.setEdindt(trandt); //结束日期
                        intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                        intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                        intrEntity.setCorpno(tblKnbAcin.getCorpno());//法人代码
                        intrEntity.setBrchno(tblKnaFxac.getBrchno());//机构

                        intrEntity.setLevety(tblKnbAcin.getLevety());
                        pbpub.countInteresRate(intrEntity);

                        cuusin = intrEntity.getIntrvl(); //当前执行利率

                        // 利率优惠后执行利率
                        cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
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

                    intrvl = cuusin;

                    bigTradFixInstam = pbpub.countInteresRateByAmounts(
                            cuusin, tblKnbAcin.getBgindt(),
                            trandt, cplDpInstPrcIn.getOnlnbl(),
                            tblKnbAcin.getTxbebs());

                    bigTradFixInstam = BusiTools.roundByCurrency(crcycd, bigTradFixInstam, null); //按币种取有效值
                    bigCabrin = bigCabrin.add(bigTradFixInstam);

                    //					//利息税
                    //					bigTradFixIntxam = bigCabrin.multiply(curIntTx);
                    //					bigIntxam = BusiTools.roundByCurrency(crcycd,bigTradFixIntxam );

                } else if (E_IRCDTP.LAYER == tblKnbAcin.getIncdtp()) {

                    IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                    intrEntity.setCorpno(tblKnbAcin.getCorpno()); //法人代码
                    intrEntity.setBrchno(cplDpInstPrcIn.getBrchno());//机构号
                    intrEntity.setTranam(cplDpInstPrcIn.getOnlnbl());//交易金额
                    intrEntity.setTrandt(cplDpInstPrcIn.getTrandt());//交易日期
                    intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码 
                    intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                    intrEntity.setCrcycd(cplDpInstPrcIn.getCrcycd());//币种
                    intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                    intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                    intrEntity.setCainpf(E_CAINPF.T1); //计息规则
                    //intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起息日期
                    intrEntity.setBgindt(tblKnaFxac.getBgindt());  //起息日期
                    intrEntity.setEdindt(cplDpInstPrcIn.getTrandt()); //止息日

                    intrEntity.setLevety(tblKnbAcin.getLevety());
                    if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
                        intrEntity.setTrandt(tblKnbAcin.getOpendt());
                        intrEntity.setTrantm("999999");
                    }
                    pbpub.countInteresRate(intrEntity);

                    BigDecimal bigOvduInstam = intrEntity.getInamnt();

                    // 利率优惠后执行利率
                    BigDecimal cuusin = intrEntity.getIntrvl();
                    cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
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

                    intrvl = cuusin;
                    bigTradFixInstam = pbpub.countInteresRateByAmounts(
                            cuusin, tblKnbAcin.getBgindt(),
                            trandt, cplDpInstPrcIn.getOnlnbl(),
                            tblKnbAcin.getTxbebs());

                    bigTradFixInstam = BusiTools.roundByCurrency(crcycd, bigOvduInstam, null); //按币种取有效值
                    bigCabrin = bigCabrin.add(bigTradFixInstam);

                    //						//利息税
                    //						bigTradFixIntxam = bigCabrin.multiply(curIntTx);
                    //						bigIntxam = BusiTools.roundByCurrency(crcycd,bigTradFixIntxam );						

                    //						bigTradFixInstam = pbpub.IntrPublic_calInstBytranam(
                    //						intrEntity.getIntrvl(), tblKnbAcin.getBgindt(),
                    //						trandt, cplDpInstPrcIn.getOnlnbl(),
                    //						tblKnbAcin.getTxbebs());
                    //						bigTradFixInstam = BusiTools.roundByCurrency(crcycd, bigTradFixInstam); //按币种取有效值
                    //						bigCabrin = bigCabrin.add(bigTradFixInstam);
                } else { // 分层利率在利率优惠代码块中进行实现
                    throw DpModuleError.DpstComm.BNAS1081();
                }

            } else {//智能储蓄
                BigDecimal bigSaveInstam = BigDecimal.ZERO; //智能储蓄存入计提利息
                BigDecimal bigdrawInstam = BigDecimal.ZERO; //智能储蓄支出计提利息

                bigSaveInstam = prcFixSave(acctno, tblKubInrt, cplDpInstPrcIn, tblKnbAcin);
                bigdrawInstam = prcFixDraw(acctno, crcycd);

                bizlog.debug("智能储蓄存入计提利息===========" + bigSaveInstam);
                bizlog.debug("智能储蓄支出计提利息=============" + bigdrawInstam);

                //智能储蓄支出计提利息  放入返回结果集
                mapCabrin.put("bigDrawCabrin", bigdrawInstam);

                bigCapadpInstam = bigCapadpInstam.add(bigSaveInstam).add((CommUtil.isNull(bigdrawInstam) ? BigDecimal.ZERO : bigdrawInstam));

                bigCabrin = bigCapadpInstam;
            }
        }
        //计提天数
        //cabrDays = DateTools2.calDays(tblKnbAcin.getBgindt(), trandt, 1, 0);
        

        //负债账户计提放入返回结果集
        mapCabrin.put("bigCabrin", bigCabrin);
        mapCabrin.put("bigIntxam", bigIntxam);
        mapCabrin.put("totalAcmltn", totalAcmltn); //活期总积数
        mapCabrin.put("intrvl", intrvl); //执行利率

        return mapCabrin;
    }

    /**
     * @author renjinghua
     *         <p>
     *         <li>2015年4月11日 下午17:30</li>
     *         <li>计算智能储蓄未支取部分明细利息，登记各账户存入明细计提明细</li>
     *         </p>
     * @param acctno 负债账户
     * @param tblKubInrt 账户利率信息
     * @param cplDpInstPrcIn 负债主计息程序处理输入
     * @param tblKnbAcin 账户计息定义信息
     * @return 智能储蓄未支取部分计提利息
     */
    public static BigDecimal prcFixSave(String acctno, KubInrt tblKubInrt, DpInstPrcIn cplDpInstPrcIn, KnbAcin tblKnbAcin) {
        BigDecimal bigSaveCabrin = BigDecimal.ZERO;
        String crcycd = cplDpInstPrcIn.getCrcycd();
        String trandt = cplDpInstPrcIn.getTrandt();
        String lstrdt = cplDpInstPrcIn.getLstrdt();

        //IntrPublicEntity entity = null;
        IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
        IoPbIntrPublicEntity entity = SysUtil.getInstance(IoPbIntrPublicEntity.class);

        List<KnaFxacDetl> tblKnaFxacDetls = KnaFxacDetlDao.selectAll_odb2(acctno, false);
        if (CommUtil.isNotNull(tblKnaFxacDetls)) {
            for (KnaFxacDetl tblKnaFxacDetl : tblKnaFxacDetls) {
                //状态不正常的明细记录不计算计提利息
                if (!CommUtil.equals(E_DPACST.NORMAL.getValue(), tblKnaFxacDetl.getAcctst().getValue())) {
                    continue;
                }

                String bgindt = tblKnaFxacDetl.getBgindt();

                //准备计算靠档利率数据
                //entity = new IntrPublicEntity();
                entity.setCrcycd(crcycd);
                entity.setIntrcd(tblKubInrt.getIntrcd());
                entity.setIncdtp(tblKubInrt.getIncdtp());
                entity.setTrandt(trandt);
                entity.setIntrwy(tblKubInrt.getIntrwy());
                entity.setBgindt(bgindt);
                entity.setEdindt(trandt);
                entity.setTranam(tblKnaFxacDetl.getOnlnbl());
                entity.setCorpno(tblKnbAcin.getCorpno());
                entity.setBrchno(cplDpInstPrcIn.getBrchno());
                entity.setCainpf(E_CAINPF.T1); //计息规则
                entity.setDepttm(tblKubInrt.getLvindt());//存期

                entity.setLevety(tblKnbAcin.getLevety());
                if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
                    entity.setTrandt(tblKnaFxacDetl.getOpendt());
                    entity.setTrantm("999999");
                }
                
                /**
                 * add by xj 20180926 柳行定活宝产品 金额靠档 使用定活宝定期账户合计余额，不使用交易金额或子账户余额
                 */
                //获取定活宝产品号
    			KnpParameter dhbPara = KnpParameterDao.selectOne_odb1("DpParm.dppb", "dppb_dhb", "%", "%", false);
    			if(CommUtil.isNotNull(dhbPara.getParm_value1()) && CommUtil.compare(dhbPara.getParm_value1(),tblKnbAcin.getProdcd())==0){
    				//查询定活宝账户余额
    				KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(tblKnbAcin.getAcctno(), false);
    				if(CommUtil.isNotNull(tblKnaFxac) && CommUtil.compare(tblKnaFxac.getOnlnbl(),BigDecimal.ZERO)>0){
    					//使用定活宝定期负债账号余额进行金额靠档
    					entity.setTranam(tblKnaFxac.getOnlnbl());
    				}
    			}
                /**end*/
                
                //获取靠档执行利率
                pbpub.countInteresRate(entity);

                BigDecimal onlnbl = tblKnaFxacDetl.getOnlnbl();

                // 利率优惠后执行利率
                BigDecimal cuusin = entity.getIntrvl();
                cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
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

                //计算智能储蓄未支取部分计提利息
                BigDecimal bigSave = pbpub.countInteresRateByAmounts(cuusin, bgindt, trandt, onlnbl, tblKnbAcin.getTxbebs());
                bigSave = BusiTools.roundByCurrency(crcycd, bigSave, null); //按币种取有效值

                //登记智能储蓄未支取明细部分计提利息
                KnbCbdp tblKnbCbdp = SysUtil.getInstance(KnbCbdp.class);
                tblKnbCbdp.setCabrdt(lstrdt); //计提日期
                tblKnbCbdp.setAcctno(acctno); //负债账号
                tblKnbCbdp.setBrchno(cplDpInstPrcIn.getBrchno()); //所属机构
                tblKnbCbdp.setCrcycd(crcycd); //币种
                tblKnbCbdp.setProdcd(cplDpInstPrcIn.getProdcd()); //产品编号
                tblKnbCbdp.setAcctcd(cplDpInstPrcIn.getAcctcd()); //核算代码
                tblKnbCbdp.setCabrin(bigSave); //计提利息
                tblKnbCbdp.setIndxno(tblKnaFxacDetl.getDetlsq()); //顺序号
                tblKnbCbdp.setIntrvl(entity.getIntrvl()); //执行利率
                tblKnbCbdp.setRemark(entity.getRemark()); //靠档利率描述
                tblKnbCbdp.setAcmltn(onlnbl); //计提金额
                KnbCbdpDao.insert(tblKnbCbdp);

                //各部分明细计提利息相加
                bigSaveCabrin = bigSaveCabrin.add(bigSave);
            }
        }
        return bigSaveCabrin;
    }

    /**
     * @author renjinghua
     *         <p>
     *         <li>2015年4月11日 下午17:30</li>
     *         <li>计算智能储蓄支出部分计提利息</li>
     *         </p>
     * 
     * @param acctno 负债账号
     * @return 智能储蓄支出计提利息
     */
    public static BigDecimal prcFixDraw(String acctno, String crcycd) {
        BigDecimal bigDrawCabrin = BigDecimal.ZERO;

        DpInstCabrOut cplDpInstCabrOut = DpDayEndDao.selFxacDrdlInstamByAcctno(acctno, E_DPACST.NORMAL, false);
        bizlog.debug("支出计提利息++++++++++++++" + cplDpInstCabrOut);

        if (CommUtil.isNull(cplDpInstCabrOut)) {
            return null;
        }

        bigDrawCabrin = bigDrawCabrin.add(cplDpInstCabrOut.getCabrin());
        bigDrawCabrin = BusiTools.roundByCurrency(crcycd, bigDrawCabrin, null); //按币种取有效值

        return bigDrawCabrin;
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
    public static int calAvgDays(E_CYCLTP cycltp, E_AVBLDT avbldt, String txbefr, String lcdt, String ncdt, String curdt) {
        int days;

        if (CommUtil.isNull(ncdt)) {
            throw DpModuleError.DpstComm.BNAS1594();
        }
        if (CommUtil.isNull(lcdt)) {
            if (CommUtil.isNull(txbefr)) {
                throw DpModuleError.DpstComm.BNAS1595();
            }
            //		    	lcdt = DateTools2.calDateByFreq(ncdt, txbefr, "", "", 3, 2);
            lcdt = DateTools2.calDateByFreq(ncdt, txbefr, null, 2);
        }

        if (CommUtil.compare(avbldt, E_AVBLDT.T1) == 0) {//实际天数
            days = DateTools2.calDays(lcdt, ncdt, 0, 0); // 实际天数
        } else {
            days = DateTools2.calDays(lcdt, ncdt, 1, 0); // 储蓄天数
        }

        return days;
    }

}
