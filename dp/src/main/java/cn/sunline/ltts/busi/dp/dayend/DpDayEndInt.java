package cn.sunline.ltts.busi.dp.dayend;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.engine.sequence.SequenceManager;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.acct.DpInterestAndTax;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.base.DpPublicServ;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.domain.DpSaveEntity;
import cn.sunline.ltts.busi.dp.layer.LayerAcctSrv;
import cn.sunline.ltts.busi.dp.layer.LayerCalcu;
import cn.sunline.ltts.busi.dp.namedsql.DpAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCabrFxdr;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCabrFxdrDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbdl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbdlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPidl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPidlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDrdl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDrdlDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.CalInterTax;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInstFaxIn;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInstPrcDetailIn;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInstPrcIn;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInstSett;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpIntxamInInstam;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
import cn.sunline.ltts.busi.sys.errors.PbError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CAINPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEBS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRDPWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_LYINWY;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

/**
 * 
 * @author renjinghua
 *         <p>
 *         <li>2015年4月10日 下午15:30</li>
 *         <li>负债账户日终计提利息相关处理</li>
 *         </p>
 */
public class DpDayEndInt {

    private final static BizLog bizlog = BizLogUtil.getBizLog(BizLog.class);

    /**
     * @author renjinghua
     *         <p>
     *         <li>2015年4月10日 下午 15:40</li>
     *         <li>负债账户日终计息处理</li>
     *         </p>
     * 
     * @param tblKnbAcin 负债账户计息信息
     * @param lstrdt 上次交易日期
     * @param trandt 交易日期
     * @param isAdd 是否增量计提
     */
    public static void prcCrcabr(KnbAcin tblKnbAcin, String lstrdt, String trandt, E_YES___ isAdd) {
        //计提天数：按照算头不算尾的原则，计提天数就是交易日期减下次计息日期, 开户时下次计息日期是开户日
        //int lCrcabrDays = DateTimeUtil.dateDiff("day", tblKnbAcin.getLaindt(), trandt);

        //负债账号
        String sacctno = tblKnbAcin.getAcctno();

        bizlog.debug("开始计息，账号 = [%s]", sacctno);

        //E_INTRTP eIntrtp = tblKnbAcin.getIntrtp();

        // 只实现正利息和到期利息
        //if(!(CommUtil.equals(E_INTRTP.ZHENGGLX.getValue(), eIntrtp.getValue()) || CommUtil.equals(E_INTRTP.DAOQIILX.getValue(), eIntrtp.getValue()))){
        //	bizlog.error("负债账号利息类型不支持[%s]", tblKnbAcin.getIntrtp().getLongName());
        //	return;
        //}

        // 利息税率默认为零
        //BigDecimal bigTaxRate = BigDecimal.ZERO;
        //if(CommUtil.equals(tblKnbAcin.getTxbefg().getValue(), E_YES___.YES.getValue())){
        //
        //}

        //不计息只滚积数的处理
        if (CommUtil.equals(E_INBEFG.NOINBE.getValue(), tblKnbAcin.getInbefg().getValue())) {
            //			tblKnbAcin.setLaindt(lstrdt); // 上次计息日： 必须填上日交易日期
            //			tblKnbAcin.setNxindt(trandt); // 下次计息日：必须填交易日期
            //			tblKnbAcin.setLsinop(E_INDLTP.CAIN); // 上次利息操作
            tblKnbAcin.setIndtds(DateTools2.calDays(tblKnbAcin.getBgindt(), trandt, 1, 0)); //计提天数
            //			tblKnbAcin.setLastdt(lstrdt); //最近更新日期
            tblKnbAcin.setPlanin(BigDecimal.ZERO); //计提利息

            KnbAcinDao.updateOne_odb1(tblKnbAcin);

            bizlog.parm("只滚积数不计息[%s]", tblKnbAcin.getAcctno());

            return;
        }

        //获取账户利率信息
        KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(sacctno, true);

        /* 测试环境可能跳跑日终, 上次交易日期lstrdt和交易日期的上一日sEdindt是很可能不相等的 */
        String sEdindt = DateTimeUtil.dateAdd("day", trandt, -1);

        //计算计提程序输入
        DpInstPrcIn cplDpInstPrcIn = SysUtil.getInstance(DpInstPrcIn.class);

        cplDpInstPrcIn.setInoptp(E_INDLTP.CAIN);
        cplDpInstPrcIn.setTrandt(trandt);
        cplDpInstPrcIn.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
        cplDpInstPrcIn.setLstrdt(lstrdt);
        cplDpInstPrcIn.setEdindt(sEdindt);

        //登记计提明细输入
        DpInstPrcDetailIn cplDpInstPrcDetailIn = SysUtil.getInstance(DpInstPrcDetailIn.class);

        //准备计提明细数据
        //活期账户
        if (CommUtil.equals(E_FCFLAG.CURRENT.getValue(), tblKnbAcin.getPddpfg().getValue())) {
            //获取活期账户信息
            KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(sacctno, true);
            BigDecimal onlnbl = DpAcctProc.getAcctBalance(tblKnaAcct);
            //设置数据
            cplDpInstPrcDetailIn.setAcctno(tblKnaAcct.getAcctno()); //负债账号
            cplDpInstPrcDetailIn.setBrchno(tblKnaAcct.getBrchno()); //所属机构
            cplDpInstPrcDetailIn.setAcctcd(tblKnaAcct.getAcctcd()); //核算代码
            cplDpInstPrcDetailIn.setCrcycd(tblKnaAcct.getCrcycd()); //货币代号
            cplDpInstPrcDetailIn.setProdcd(tblKnaAcct.getProdcd()); //产品编号
            //			cplDpInstPrcDetailIn.setIntrvl(tblKubInrt.getCuusin());  //执行利率

            cplDpInstPrcIn.setOnlnbl(onlnbl); //当期账户余额
            cplDpInstPrcIn.setCrcycd(tblKnaAcct.getCrcycd()); //账户货币代号
            cplDpInstPrcIn.setBrchno(tblKnaAcct.getBrchno()); //所属机构
            cplDpInstPrcIn.setProdcd(tblKnaAcct.getProdcd()); //产品编号
            cplDpInstPrcIn.setAcctcd(tblKnaAcct.getAcctcd()); //核算代码

            //modify by songkl 2017/8/4 增量计提问题修改  
            //若子账户状态为销户的  基数记为0
            if (tblKnaAcct.getAcctst() == E_DPACST.CLOSE) {
                cplDpInstPrcDetailIn.setAcmltn(BigDecimal.ZERO);
                BigDecimal bigCabrin = BigDecimal.ZERO;//负债账户表计提
                BigDecimal bigIntxam = BigDecimal.ZERO;//利息税

                //登记计提明细信息
                regCrcabrDetail(cplDpInstPrcDetailIn, lstrdt, bigCabrin, bigIntxam, isAdd);
                return;
            }

        } else {//定期
                //获取定期账户信息
            KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(sacctno, true);
            //设置数据
            cplDpInstPrcDetailIn.setAcctno(tblKnaFxac.getAcctno()); //负债账号
            cplDpInstPrcDetailIn.setBrchno(tblKnaFxac.getBrchno()); //所属机构
            cplDpInstPrcDetailIn.setAcctcd(tblKnaFxac.getAcctcd()); //核算代码
            cplDpInstPrcDetailIn.setCrcycd(tblKnaFxac.getCrcycd()); //货币代号
            cplDpInstPrcDetailIn.setProdcd(tblKnaFxac.getProdcd()); //产品编号
            cplDpInstPrcDetailIn.setIntrvl(BigDecimal.ZERO); //执行利率，定期设置为0

            cplDpInstPrcIn.setOnlnbl(tblKnaFxac.getOnlnbl()); //当前账户余额
            cplDpInstPrcIn.setCrcycd(tblKnaFxac.getCrcycd()); //账户货币代号
            cplDpInstPrcIn.setBrchno(tblKnaFxac.getBrchno()); //所属机构
            cplDpInstPrcIn.setProdcd(tblKnaFxac.getProdcd()); //产品编号
            cplDpInstPrcIn.setAcctcd(tblKnaFxac.getAcctcd()); //核算代码

            //modify by songkl 2017/8/4 增量计提问题修改  
            //若子账户状态为销户的  基数记为0
            if (tblKnaFxac.getAcctst() == E_DPACST.CLOSE) {
                cplDpInstPrcDetailIn.setAcmltn(BigDecimal.ZERO);
                BigDecimal bigCabrin = BigDecimal.ZERO;//负债账户表计提
                BigDecimal bigIntxam = BigDecimal.ZERO;//利息税

                //登记计提明细信息
                regCrcabrDetail(cplDpInstPrcDetailIn, lstrdt, bigCabrin, bigIntxam, isAdd);
                return;
            }
        }

        //计算计提利息
        Map<String, BigDecimal> mapCabrin = DpInterest.prcInstMain(tblKnbAcin, tblKubInrt, cplDpInstPrcIn);

        BigDecimal bigCabrin = mapCabrin.get("bigCabrin"); //负债账户计提
        BigDecimal bigDrawCabrin = mapCabrin.get("bigDrawCabrin"); //智能储蓄支出计提
        BigDecimal totalAcmltn = mapCabrin.get("totalAcmltn"); //活期总积数
        BigDecimal intrvl = mapCabrin.get("intrvl"); //执行利率
        BigDecimal bigIntxam = mapCabrin.get("bigIntxam"); //利息税
        //活期账户
        if (CommUtil.equals(E_FCFLAG.CURRENT.getValue(), tblKnbAcin.getPddpfg().getValue())) {
            cplDpInstPrcDetailIn.setIntrvl(intrvl); //执行利率		
        }

        bizlog.debug("智能储蓄支出计提>>>>>>>>>>>>>>>>>>" + bigDrawCabrin);

        //登记智能储蓄支出计提明细
        if (CommUtil.equals(E_FCFLAG.FIX.getValue(), tblKnbAcin.getPddpfg().getValue())) {
            if (CommUtil.equals(E_YES___.YES.getValue(), tblKnbAcin.getDetlfg().getValue())) {
                if (CommUtil.isNotNull(bigDrawCabrin)) {
                    regFixdrCrbrDetail(cplDpInstPrcDetailIn, lstrdt, bigDrawCabrin);
                }
            }
            cplDpInstPrcDetailIn.setAcmltn(BigDecimal.ZERO);
        } else {
            cplDpInstPrcDetailIn.setAcmltn(totalAcmltn); //活期总积数
        }

        //登记计提明细信息
        regCrcabrDetail(cplDpInstPrcDetailIn, lstrdt, bigCabrin, bigIntxam, isAdd);

        bizlog.parm("计提利息[%s]", bigCabrin);

    }

    /**
     * @author renjinghua
     *         <p>
     *         <li>2015年4月13日 上午 11::30</li>
     *         <li>登记定期（智能储蓄）支取部分计提明细</li>
     *         </p>
     * 
     * @param cplDpInstPrcDetailIn 计提明细输入
     * @param lstrdt 上次交易日期
     * @param bigDrawCabrin 计提利息
     */
    private static void regFixdrCrbrDetail(DpInstPrcDetailIn cplDpInstPrcDetailIn, String lstrdt, BigDecimal bigDrawCabrin) {
        KnbCabrFxdr tblKnbCabrFxdr = SysUtil.getInstance(KnbCabrFxdr.class);

        String crcycd = cplDpInstPrcDetailIn.getCrcycd();

        //bigDrawCabrin = CommTools.roundByCrcy(crcycd, bigDrawCabrin);

        tblKnbCabrFxdr.setCabrdt(lstrdt); //计提日期
        tblKnbCabrFxdr.setAcctno(cplDpInstPrcDetailIn.getAcctno()); //负债账号
        tblKnbCabrFxdr.setBrchno(cplDpInstPrcDetailIn.getBrchno()); //所属机构
        tblKnbCabrFxdr.setAcctcd(cplDpInstPrcDetailIn.getAcctcd()); //核算代码
        tblKnbCabrFxdr.setCrcycd(crcycd); //货币代号
        tblKnbCabrFxdr.setProdcd(cplDpInstPrcDetailIn.getProdcd()); //产品编号
        tblKnbCabrFxdr.setCabrin(bigDrawCabrin); //计提利息

        KnbCabrFxdrDao.insert(tblKnbCabrFxdr);

    }

    /**
     * @author renjinghua
     *         <p>
     *         <li>2015年4月11日 上午12:00</li>
     *         <li>登记计提明细</li>
     *         </p>
     * 
     * @param cplDpInstPrcDetailIn 计提明细输入
     * @param lstrdt 上次交易日期
     * @param bigCabrin 计提利息
     */
    private static void regCrcabrDetail(DpInstPrcDetailIn cplDpInstPrcDetailIn, String lstrdt, BigDecimal bigCabrin, BigDecimal bigIntxam, E_YES___ isAdd) {
        KnbCbdl tblKnbCbdl = SysUtil.getInstance(KnbCbdl.class);

        String crcycd = cplDpInstPrcDetailIn.getCrcycd();

        //bigCabrin = CommTools.roundByCrcy(crcycd, bigCabrin);

        tblKnbCbdl.setCabrdt(lstrdt); //计提日期
        tblKnbCbdl.setAcctno(cplDpInstPrcDetailIn.getAcctno()); //负债账号
        tblKnbCbdl.setBrchno(cplDpInstPrcDetailIn.getBrchno()); //所属机构
        tblKnbCbdl.setAcctcd(cplDpInstPrcDetailIn.getAcctcd()); //核算代码
        tblKnbCbdl.setCrcycd(crcycd); //货币代号
        tblKnbCbdl.setProdcd(cplDpInstPrcDetailIn.getProdcd()); //产品编号
        tblKnbCbdl.setCabrin(bigCabrin); //计提利息
        tblKnbCbdl.setIntxam(bigIntxam); //计提利息税
        tblKnbCbdl.setAcmltn(cplDpInstPrcDetailIn.getAcmltn()); //积数
        tblKnbCbdl.setIntrvl(cplDpInstPrcDetailIn.getIntrvl()); //执行利率
        tblKnbCbdl.setIsadd(isAdd); //是否增量计提

        checkFixCabrin(tblKnbCbdl);
        
        KnbCbdlDao.insert(tblKnbCbdl);
    }

    /**
     * 检查调整计提利息登记簿
     * @param tblKnbCbdl
     */
    private static void checkFixCabrin(KnbCbdl tblKnbCbdl) {
    	String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
    	String cabrdt = tblKnbCbdl.getCabrdt();
    	String acctno = tblKnbCbdl.getAcctno();
    	String brchno = tblKnbCbdl.getBrchno();
    	String acctcd = tblKnbCbdl.getAcctcd();
    	String crcycd = tblKnbCbdl.getCrcycd();
    	String prodcd = tblKnbCbdl.getProdcd();
    	
		BigDecimal fixCabrin = DpAcinDao.selKnbCbdlFixCabrin(corpno, cabrdt, acctno, brchno, acctcd, crcycd, prodcd, false);
		if(CommUtil.isNotNull(fixCabrin)) {
			tblKnbCbdl.setCabrin(tblKnbCbdl.getCabrin().add(fixCabrin));//调整利息
		}
		
		//更新状态
		//DpAcinDao.updKnbCbdlFixStatus(corpno, cabrdt, acctno, brchno, acctcd, crcycd, prodcd);
		DpAcinDao.updKnbCbdlFixStatus(corpno, cabrdt, acctno, crcycd, prodcd);
	}

	// 支取部分利息结息
    public static void prcDrawInstPay(String acctno, String crcycd,
            String trandt) {

        bizlog.parm("支取开始付息处理, 定期账号 = [%s]", acctno);
        //KnbAcin tblKnbAcin = KnbAcinDao.selectOneWithLock_odb1(acctno, true);

        DpIntxamInInstam cplInstamTx = fxDrawInst(acctno, crcycd, trandt); // 利息、利息税
        // 四舍五入为最小计算单位
        BigDecimal instam = BusiTools.roundByCurrency(crcycd, cplInstamTx.getInstam(), null);

        BigDecimal intxam = BusiTools.roundByCurrency(crcycd, cplInstamTx.getIntxam(), null);// 利息税
        //String custac = Kna_accsDao.selectOne_odb2(acctno, true).getCustac();
        IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
        String custac = caqry.getKnaAccsOdb2(acctno, true).getCustac();
        //String cuacct = CaTools.getAcctno(custac, crcycd); // 获取活期账号
        //String cuacct = casta.CaTools_getAcctno(custac, crcycd); // 获取活期账号
        String cuacct = CapitalTransDeal.getSettKnaAcctSub(custac, E_ACSETP.SA).getAcctno();
        KnaFxac tblKnaFxac = KnaFxacDao.selectOneWithLock_odb1(acctno, true);
        if (CommUtil.compare(instam, BigDecimal.ZERO) == 0) {
            bizlog.parm("账号[%s]待支付利息为零", acctno);

            return;
        }
        prcPayMain(cuacct, instam, intxam);

        // 将利息、利息税登记到会计流水
        DpInstFaxIn cplInst = SysUtil.getInstance(DpInstFaxIn.class);
        cplInst.setEcctno(custac);
        cplInst.setAcctno(cuacct);
        cplInst.setInstam(instam); // 利息
        cplInst.setIntxam(intxam); // 利息税
        DpDayEndInt.regACSerailForFxac(cplInst, tblKnaFxac);

    }

    /**
     * 定期支取部分利息结算
     * 
     * @param ecctno
     * @param crcycd
     * @param trandt
     * @return
     */
    public static DpIntxamInInstam fxDrawInst(String acctno, String crcycd, String trandt) {

        DpIntxamInInstam cplInstamTx = SysUtil.getInstance(DpIntxamInInstam.class);
        BigDecimal sumInstam = BigDecimal.ZERO;
        BigDecimal sumIntxam = BigDecimal.ZERO;
        List<KnaFxacDrdl> lstKnaFxacDrdl = null;

        // 账户利率信息
        KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acctno, true);

        lstKnaFxacDrdl = KnaFxacDrdlDao.selectAll_odb2(acctno, crcycd, E_ACCTST.NORMAL, false);

        for (KnaFxacDrdl tblKnaFxacDrdl : lstKnaFxacDrdl) {

            // 支取后更新状态为关闭
            tblKnaFxacDrdl.setTranst(E_ACCTST.CLOSE);
            sumInstam = sumInstam.add(tblKnaFxacDrdl.getIntram());
            sumIntxam = sumIntxam.add(tblKnaFxacDrdl.getIntxam());
            KnaFxacDrdlDao.updateOne_odb1(tblKnaFxacDrdl);

            // 登记到付息明细
            KnbPidl tblKnbPidl = SysUtil.getInstance(KnbPidl.class);
            tblKnbPidl.setAcctno(tblKnaFxacDrdl.getAcctno());
            tblKnbPidl.setIntrtp(E_INTRTP.ZHENGGLX);
            //Long detlsq = Long.parseLong(SequenceManager.nextval("KnbPidl"));
            Long detlsq = Long.parseLong(CoreUtil.nextValue("KnbPidl"));
            tblKnbPidl.setIndxno(detlsq);
            tblKnbPidl.setDetlsq(detlsq);
            tblKnbPidl.setIndlst(E_INDLST.YOUX);
            tblKnbPidl.setLsinoc(E_INDLTP.PYIN);
            tblKnbPidl.setInstdt(tblKnaFxacDrdl.getBgindt()); // 计息起始日，3月前
            tblKnbPidl.setIneddt(tblKnaFxacDrdl.getEdindt());
            tblKnbPidl.setIntrcd(tblKubInrt.getIntrcd()); // 利率编号
            tblKnbPidl.setIncdtp(E_IRCDTP.LAYER);
            tblKnbPidl.setLyinwy(null); // 分层计息方式
            tblKnbPidl.setIntrwy(E_IRDPWY.CURRENT);
            tblKnbPidl.setLvamot(null); // 分层金额
            tblKnbPidl.setLvindt(null); // 层次利率存期
            tblKnbPidl.setGradin(BigDecimal.ZERO); // 档次计息余额
            tblKnbPidl.setTotlin(tblKnaFxacDrdl.getDrawam());// 总计息余额
            tblKnbPidl.setAcmltn(BigDecimal.ZERO);// 积数
            tblKnbPidl.setTxbebs(E_INBEBS.STADSTAD);
            tblKnbPidl.setAcbsin(tblKubInrt.getBsintr()); // 基准利率
            tblKnbPidl.setCuusin(tblKnaFxacDrdl.getCuusin()); // 执行利率
            tblKnbPidl.setRlintr(tblKnaFxacDrdl.getIntram());// 实际利息发生额
            tblKnbPidl.setRlintx(BigDecimal.ZERO);
            tblKnbPidl.setIntrdt(trandt);
            tblKnbPidl.setIntrsq(tblKnaFxacDrdl.getTransq()); // 计息流水
            tblKnbPidl.setPyindt(trandt);
            tblKnbPidl.setPyinsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
            tblKnbPidl.setRemark(tblKnaFxacDrdl.getRemark());

            // 移到已支付利息
            KnbPidlDao.insert(tblKnbPidl);
        }
        cplInstamTx.setInstam(sumInstam);
        cplInstamTx.setIntxam(sumIntxam);
        return cplInstamTx;
    }

    /**
     * 支取部分利息
     * 
     * @param ecctno
     * @param crcycd
     * @param trandt
     * @return
     */
    //		public static BigDecimal drawInst(String ecctno, String crcycd, String trandt) {
    //			//电子账户下所有的支取计提明细
    //			List<KnbCabrFxdr> lstKnbCabrFxdr = DpDayEndDao.selSumCabrFxdr(ecctno, crcycd,CommTools.getBaseRunEnvs().getLast_date(), false);
    //			BigDecimal sum = BigDecimal.ZERO;
    //			List<KnaFxacDrdl> lstKnaFxacDrdl=null;
    //			for (KnbCabrFxdr tblKnbCabrFxdr : lstKnbCabrFxdr) {
    //				
    //				//基金利息信息
    //				KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(tblKnbCabrFxdr.getAcctno(), true);
    //				
    //				sum = sum.add(tblKnbCabrFxdr.getCabrin());
    //				lstKnaFxacDrdl = KnaFxacDrdlDao.selectAll_odb2(tblKnbCabrFxdr.getAcctno(), crcycd, false);
    //				// 支取后更新状态为关闭
    //				for (KnaFxacDrdl tblKnaFxacDrdl : lstKnaFxacDrdl) {
    //					tblKnaFxacDrdl.setTranst(E_ACCTST.CLOSE);
    //					KnaFxacDrdlDao.updateOne_odb1(tblKnaFxacDrdl);
    //				}
    //				//登记到付息明细
    //				KnbPidl tblKnbPidl = SysUtil.getInstance(KnbPidl.class);
    //				tblKnbPidl.setAcctno(tblKnbCabrFxdr.getAcctno());
    //				tblKnbPidl.setIntrtp(E_INTRTP.ZHENGGLX);
    //				tblKnbPidl.setIndxno(Long.parseLong("1"));
    //				tblKnbPidl.setDetlsq(Long.parseLong("99"));
    //				tblKnbPidl.setIndlst(E_INDLST.YOUX);
    //				tblKnbPidl.setLsinoc(E_INDLTP.PYIN);
    //				tblKnbPidl.setInstdt(DateTimeUtil.dateAdd("qq", trandt, 1));		//计息起始日，3月前
    //				tblKnbPidl.setIneddt(trandt);
    //				tblKnbPidl.setIntrcd("0");						//利率编号
    //				tblKnbPidl.setIncdtp(E_IRCDTP.REAYON);
    //				tblKnbPidl.setLyinwy(null);						//分层计息方式
    //				tblKnbPidl.setIntrwy(E_IRDPWY.CURRENT);
    //				tblKnbPidl.setLvamot(null);						//分层金额
    //				tblKnbPidl.setLvindt(null);						//层次利率存期
    //				tblKnbPidl.setGradin(BigDecimal.ZERO);						//档次计息余额
    //				tblKnbPidl.setTotlin(tblKnbCabrFxdr.getCabrin());//总计息余额
    //				tblKnbPidl.setAcmltn(BigDecimal.ZERO);//积数
    //				tblKnbPidl.setTxbebs(E_INBEBS.STADSTAD);
    //				tblKnbPidl.setAcbsin(tblKubInrt.getBsintr());						//基准利率
    //				tblKnbPidl.setCuusin(tblKubInrt.getCuusin());						//执行利率
    //				tblKnbPidl.setRlintr(tblKnbCabrFxdr.getCabrin());//实际利息发生额
    //				tblKnbPidl.setRlintx(BigDecimal.ZERO);
    //				tblKnbPidl.setIntrdt(trandt);
    //				tblKnbPidl.setIntrsq("000");						//计息流水
    //				tblKnbPidl.setPyindt(trandt);
    //				tblKnbPidl.setPyinsq(CommTools.getBaseRunEnvs().getTrxn_seq());
    //
    //				// 移到已支付利息
    //				KnbPidlDao.insert(tblKnbPidl);
    //			}
    //			
    //			return sum;
    //		}

    /**
     * 支取部分利息计算公式
     * 
     * @param bgindt
     *        开始时间
     * @param edindt
     *        结束时间
     * @param drawam
     *        支取金额
     * @param cuusin
     *        利率
     * @return 电子账户下的支取部分总利息
     */
    public static BigDecimal calcDrawInst(String bgindt, String edindt,
            BigDecimal drawam, BigDecimal cuusin) {
        int count = DateTools2.calDays(bgindt, edindt, 1, 0); // 实际存储时间
        int year = 360;
        BigDecimal countB = BigDecimal.valueOf(count);
        BigDecimal yearB = BigDecimal.valueOf(year);
        BigDecimal cuusinB = cuusin.divide(BigDecimal.valueOf(100), 8,
                BigDecimal.ROUND_HALF_UP);

        /**
         * 金额*利率*天数/年/100
         */
        BigDecimal inst = drawam.multiply(cuusinB).multiply(countB)
                .divide(yearB, 8, BigDecimal.ROUND_HALF_UP);
        return inst;
    }

    /**
     * 活期付息处理
     * 
     * @param tblKnbAcin
     * @param trandt
     * @param transq
     * @return
     */
    public static DpInstSett prcInstDetlDayEnd(KnbAcin tblKnbAcin,
            String trandt, String transq) {

        // 实例化利息和利息税输出结果
        DpInstSett cplOut = SysUtil.getInstance(DpInstSett.class);
        IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);

        String acctno = tblKnbAcin.getAcctno();
        BigDecimal cb = tblKnbAcin.getPlanin(); // 计提利息
        BigDecimal tax = tblKnbAcin.getMustin(); // 应缴税金
        BigDecimal fdInst = BigDecimal.ZERO; // 分段利息总金额

        // 获取待付息明细,参数：负债账号，利息类型
        List<KnbIndl> lstKnbIndl = KnbIndlDao.selectAll_odb4(acctno, E_INDLST.YOUX, false);

        KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acctno, true);

        if (CommUtil.compare(cb, BigDecimal.ZERO) >= 0) {

            // 实例化付息信息
            KnbPidl tblKnbPidl = SysUtil.getInstance(KnbPidl.class);

            // 循环每一笔分段利息明细
            for (KnbIndl tblKnbIndl : lstKnbIndl) {

                //重新计算当期段利息
                BigDecimal currInterest = BusiTools.roundByCurrency(tblKnbAcin.getCrcycd(), pbpub.countInteresRateByBase(tblKubInrt.getCuusin(), tblKnbIndl.getAcmltn()), null);

                BigDecimal currIntertax = BusiTools.roundByCurrency(tblKnbAcin.getCrcycd(), currInterest.multiply(tblKnbIndl.getCatxrt()), null);
                // 插入付息明细
                tblKnbPidl.setAcctno(tblKnbIndl.getAcctno());
                tblKnbPidl.setIntrtp(tblKnbIndl.getIntrtp());
                tblKnbPidl.setIndxno(tblKnbIndl.getIndxno());
                tblKnbPidl.setDetlsq(tblKnbIndl.getDetlsq());
                tblKnbPidl.setIndlst(tblKnbIndl.getIndlst());
                tblKnbPidl.setLsinoc(tblKnbIndl.getLsinoc());
                tblKnbPidl.setInstdt(tblKnbIndl.getInstdt());
                tblKnbPidl.setIneddt(tblKnbIndl.getIneddt());
                tblKnbPidl.setIntrcd(tblKnbIndl.getIntrcd());
                tblKnbPidl.setIncdtp(tblKnbIndl.getIncdtp());
                tblKnbPidl.setLyinwy(tblKnbIndl.getLyinwy());
                tblKnbPidl.setIntrwy(tblKnbIndl.getIntrwy());
                tblKnbPidl.setLvamot(tblKnbIndl.getLvamot());
                tblKnbPidl.setLvindt(tblKnbIndl.getLvindt());
                tblKnbPidl.setGradin(tblKnbIndl.getGradin());
                tblKnbPidl.setTotlin(tblKnbIndl.getTotlin());
                tblKnbPidl.setAcmltn(tblKnbIndl.getAcmltn());
                tblKnbPidl.setTxbebs(tblKnbIndl.getTxbebs());
                tblKnbPidl.setAcbsin(tblKnbIndl.getAcbsin());
                tblKnbPidl.setCuusin(tblKnbIndl.getCuusin());
                tblKnbPidl.setRlintr(currInterest);//登记重算后的利息
                tblKnbPidl.setRlintx(currIntertax);//登记重算后的利息税
                tblKnbPidl.setIntrdt(tblKnbIndl.getIntrdt());
                tblKnbPidl.setIntrsq(tblKnbIndl.getIntrsq());
                tblKnbPidl.setPyindt(trandt);
                tblKnbPidl.setPyinsq(transq);

                fdInst = fdInst.add(tblKnbIndl.getRlintr()); //分段结息的总额

                // 移到已支付利息
                KnbPidlDao.insert(tblKnbPidl);
                tblKnbIndl.setIndlst(E_INDLST.WUX); //已结息后状态设为无效
                KnbIndlDao.updateOne_odb1(tblKnbIndl);

            }

            KnbPidl hqKnbIndl = SysUtil.getInstance(KnbPidl.class);
            //账户利率信息
            if (tblKnbAcin.getIncdtp() == E_IRCDTP.LAYER && tblKnbAcin.getInclfg() == E_YES___.NO) { //分层
                List<KubInrt> lstInrt = LayerAcctSrv.getKubInrt(acctno); //获取账户利率信息
                KnaAcct knaAcct = KnaAcctDao.selectOne_odb1(acctno, true);
                BigDecimal onlnbl = DpAcctProc.getAcctBalance(knaAcct);
                int mark = LayerAcctSrv.getLayerMark(lstInrt, 0L, onlnbl);
                BigDecimal smt = BigDecimal.ZERO;
                BigDecimal bal = BigDecimal.ZERO;
                for (int i = 0; i < lstInrt.size(); i++) {
                    hqKnbIndl.setAcctno(acctno);
                    hqKnbIndl.setIntrtp(tblKnbAcin.getIntrtp());
                    //Long detlsq = Long.parseLong(SequenceManager.nextval("KnbPidl"));
                    Long detlsq = Long.parseLong(CoreUtil.nextValue("KnbPidl"));
                    hqKnbIndl.setIndxno(detlsq);
                    hqKnbIndl.setDetlsq(detlsq);
                    //						hqKnbIndl.setIndxno(Long.parseLong("1"));
                    //						hqKnbIndl.setDetlsq(Long.parseLong("99"));
                    hqKnbIndl.setIndlst(E_INDLST.YOUX);
                    hqKnbIndl.setLsinoc(E_INDLTP.PYIN);
                    hqKnbIndl.setInstdt(tblKnbAcin.getBgindt());
                    hqKnbIndl.setIneddt(trandt);
                    hqKnbIndl.setIntrcd(tblKnbAcin.getIntrcd());
                    hqKnbIndl.setIncdtp(E_IRCDTP.BASE);
                    hqKnbIndl.setLyinwy(lstInrt.get(i).getLyinwy());// 分层计息方式
                    hqKnbIndl.setIntrwy(E_IRDPWY.CURRENT);
                    hqKnbIndl.setLvamot(lstInrt.get(i).getLvamot());// 分层金额
                    hqKnbIndl.setLvindt(lstInrt.get(i).getLvindt());// 层次利率存期
                    if (tblKnbAcin.getLyinwy() == E_LYINWY.ALL) {
                        if (i == mark) {
                            bal = onlnbl;
                            smt = DpPublic.calRealTotalAmt(lstInrt.get(i).getClvsmt(), bal, trandt, lstInrt.get(i).getLastdt());
                        } else {
                            smt = lstInrt.get(i).getClvsmt();
                        }
                    } else if (tblKnbAcin.getLyinwy() == E_LYINWY.OVER) {
                        if (i < mark) {
                            bal = lstInrt.get(i).getLastbl();
                            smt = DpPublic.calRealTotalAmt(lstInrt.get(i).getClvsmt(), bal, trandt, lstInrt.get(i).getLastdt());
                        } else if (i == mark) {
                            bal = onlnbl.subtract(lstInrt.get(i).getLvamot());
                            smt = DpPublic.calRealTotalAmt(lstInrt.get(i).getClvsmt(), bal, trandt, lstInrt.get(i).getLastdt());
                        } else {
                            smt = lstInrt.get(i).getClvsmt();
                        }
                    }
                    //hqKnbIndl.setGradin(tblKnbAcin.getPlanin().subtract(fdInst));
                    hqKnbIndl.setGradin(LayerCalcu.calcIntr(smt, lstInrt.get(i).getCuusin(), tblKnbAcin));
                    //hqKnbIndl.setTotlin(tblKnbAcin.getPlanin().subtract(fdInst));
                    hqKnbIndl.setTotlin(BigDecimal.ZERO);
                    //hqKnbIndl.setAcmltn(tblKnbAcin.getCutmam());
                    hqKnbIndl.setAcmltn(smt);
                    /**
                     * @author cuijia
                     * 20171208
                     * 计息标准赋值错误修改
                     */
                    //hqKnbIndl.setTxbebs(E_INBEBS.STADSTAD);
                    hqKnbIndl.setTxbebs(tblKnbAcin.getTxbebs());
                    hqKnbIndl.setAcbsin(lstInrt.get(i).getBsintr());// 基准利率
                    hqKnbIndl.setCuusin(lstInrt.get(i).getCuusin());// 执行利率
                    hqKnbIndl.setRlintr(tblKnbAcin.getPlanin().subtract(fdInst));//活期总额  = 计提总额- 分段总额
                    hqKnbIndl.setRlintx(BigDecimal.ZERO);
                    hqKnbIndl.setIntrdt(trandt);
                    hqKnbIndl.setIntrsq("000");// 计息流水
                    hqKnbIndl.setPyindt(trandt);
                    hqKnbIndl.setPyinsq(transq);
                    // 移到已支付利息
                    KnbPidlDao.insert(hqKnbIndl);
                }
            } else {
                KubInrt tblKubInrt1 = KubInrtDao.selectOne_odb1(acctno, true);
                // 插入活期付息明细
                hqKnbIndl.setAcctno(acctno);
                hqKnbIndl.setIntrtp(tblKnbAcin.getIntrtp());
                //Long detlsq = Long.parseLong(SequenceManager.nextval("KnbPidl"));
                Long detlsq = Long.parseLong(CoreUtil.nextValue("KnbPidl"));
                hqKnbIndl.setIndxno(detlsq);
                hqKnbIndl.setDetlsq(detlsq);
                //					hqKnbIndl.setIndxno(Long.parseLong("1"));
                //					hqKnbIndl.setDetlsq(Long.parseLong("99"));
                hqKnbIndl.setIndlst(E_INDLST.YOUX);
                hqKnbIndl.setLsinoc(E_INDLTP.PYIN);
                hqKnbIndl.setInstdt(tblKnbAcin.getBgindt());
                hqKnbIndl.setIneddt(trandt);
                hqKnbIndl.setIntrcd(tblKnbAcin.getIntrcd());
                hqKnbIndl.setIncdtp(E_IRCDTP.BASE);
                hqKnbIndl.setLyinwy(null);// 分层计息方式
                hqKnbIndl.setIntrwy(E_IRDPWY.CURRENT);
                hqKnbIndl.setLvamot(null);// 分层金额
                hqKnbIndl.setLvindt(null);// 层次利率存期
                //hqKnbIndl.setGradin(tblKnbAcin.getPlanin().subtract(fdInst));
                //hqKnbIndl.setTotlin(tblKnbAcin.getPlanin().subtract(fdInst));
                hqKnbIndl.setGradin(BigDecimal.ZERO);
                hqKnbIndl.setTotlin(BigDecimal.ZERO);
                hqKnbIndl.setAcmltn(tblKnbAcin.getCutmam());
                /**
                 * @author cuijia
                 * 20171208
                 * 计息标准赋值错误修改
                 */
                //hqKnbIndl.setTxbebs(E_INBEBS.STADSTAD);
                hqKnbIndl.setTxbebs(tblKnbAcin.getTxbebs());
                hqKnbIndl.setAcbsin(tblKubInrt1.getBsintr());// 基准利率
                hqKnbIndl.setCuusin(tblKubInrt1.getCuusin());// 执行利率
                hqKnbIndl.setRlintr(tblKnbAcin.getPlanin().subtract(fdInst));//活期总额  = 计提总额- 分段总额
                hqKnbIndl.setRlintx(BigDecimal.ZERO);
                hqKnbIndl.setIntrdt(trandt);
                hqKnbIndl.setIntrsq("000");// 计息流水
                hqKnbIndl.setPyindt(trandt);
                hqKnbIndl.setPyinsq(transq);

                // 移到已支付利息
                KnbPidlDao.insert(hqKnbIndl);
            }

        }

        String nextpaydt = tblKnbAcin.getNcindt();

        // 计算下次结息日期
        if (CommUtil.isNotNull(tblKnbAcin.getTxbefr())) {

            nextpaydt = DpPublic.getNextPeriod(nextpaydt, CommTools.getBaseRunEnvs().getNext_date(), tblKnbAcin.getTxbefr());
            
            /** update by Huangwh : 20190426 修改根据频率计算下次结息日bug   start
             *  eg : 当开户日为29、30、31号时，在计算下个月可能并没有当前日期，日期
             *       会往后移，再次计算时就会拿该次计算的日期去计算，导致以后每次结息
             *       都会提前！
             *  解决方案：重置结息频率 = 当前日与起息日相隔月数  + 结息频率
             */
            if(tblKnbAcin.getPddpfg() == E_FCFLAG.FIX){//定期账户结息日重计算
                bizlog.parm("定期账户:[%s]结息日重计算==========开始", tblKnbAcin.getAcctno());
                String opendt = tblKnbAcin.getOpendt();//开户日
                String ncindt = tblKnbAcin.getNcindt();//下次结息日
                Date bgindtDate = DateTools2.toDate(opendt, "yyyyMMdd");
                Date ncindtDate = DateTools2.toDate(ncindt, "yyyyMMdd");
                int months = DateTools2.calDiffMonths(bgindtDate, ncindtDate);//开始结束日期差
                bizlog.debug("开户日:==========[%s]", opendt);
                bizlog.debug("下次结息日:==========[%s]", ncindt);
                bizlog.debug("月数差:==========[%s]", months);
                
                String txbefr1 = tblKnbAcin.getTxbefr();//原始结息频率
                bizlog.debug("原始结息频率:==========[%s]", txbefr1);
                int iFlag = 0;
                for (int i = 0; i < txbefr1.length(); i++) {
                    if (!Character.isDigit(txbefr1.charAt(i))) {
                        iFlag = i;
                        break;
                    }
                }
                
                String sQiXian1 = txbefr1.substring(0, iFlag);//原始期限值
                String sQiXianDW = txbefr1.substring(iFlag, iFlag + 1);//期限单位："D W S M Q Y "
                
                StringBuilder txbefr2 =  new StringBuilder(txbefr1);//原始结息频率
                if("M".equals(sQiXianDW)){//只有结息期限单位为M的时候才处理
                    int sQiXian2 = Integer.parseInt(sQiXian1)+months;//重新计算期限值
                    
                    String txbefr3 = txbefr2.replace(0, iFlag, sQiXian2+"").toString();
                    bizlog.debug("重计算的频率:==========[%s]", txbefr3);
                    nextpaydt = DateTools2.calDateByNextFreq(opendt, txbefr3);
                }
                bizlog.parm("重新计算的下次结息日:==========[%s]", nextpaydt);
                
                bizlog.parm("定期账户:[%s]结息日重计算==========结束", tblKnbAcin.getAcctno());

            }
            /** update by Huangwh : 20190426 修改根据频率计算下次结息日bug   end*/
            
        }
        tblKnbAcin.setCutmam(BigDecimal.ZERO); //积数清0
        tblKnbAcin.setLsinop(E_INDLTP.PYIN);
        tblKnbAcin.setBgindt(trandt); // 更新起息日期
        tblKnbAcin.setLcindt(trandt); // 上次结息日就是交易日期
        tblKnbAcin.setNcindt(nextpaydt); // 下次结息日
        tblKnbAcin.setLastdt(trandt); // 最近更新日期
        tblKnbAcin.setLaamdt(trandt);//基数更新日期

        KnbAcinDao.updateOne_odb1(tblKnbAcin);

        cplOut.setInsttx(tax);
        cplOut.setIntest(cb);
        return cplOut;

    }

    /**
     * @Author Administrator
     *         <p>
     *         <li>功能说明：日终结息处理</li>
     *         </p>
     * @param tblKnbAcin
     */
    public static void prcPay(KnbAcin tblKnbAcin, String custac) {

        bizlog.parm("活期开始付息处理, 账号 = [%s]", tblKnbAcin.getAcctno());
        
        String trandt = CommTools.getBaseRunEnvs().getTrxn_date();

        // 流水重置(只有有待结转利息才申请新流水)
        if (CommUtil.compare(tblKnbAcin.getPlanin(), BigDecimal.ZERO) > 0) {
            //CommTools.genTransq();
             MsSystemSeq.getTrxnSeq();
        }

        //获取存款账户信息
        //KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(tblKnbAcin.getAcctno(), true);	

        //modify by songkl 20170712 新增定期结息，计算利息税时金额获取区分
        BigDecimal tranam = BigDecimal.ZERO;
        KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
        KnaFxac tblknaFxac = SysUtil.getInstance(KnaFxac.class);

        //活期取kna_acct表中金额 定期取kna_fxac表中金额
        if (tblKnbAcin.getPddpfg() == E_FCFLAG.CURRENT) {
            //获取存款账户信息
            tblKnaAcct = KnaAcctDao.selectOne_odb1(tblKnbAcin.getAcctno(), true);
            tranam = DpAcctProc.getAcctBalance(tblKnaAcct);
        } else if (tblKnbAcin.getPddpfg() == E_FCFLAG.FIX) {
            tblknaFxac = KnaFxacDao.selectOne_odb1(tblKnbAcin.getAcctno(), true);
            tranam = tblknaFxac.getOnlnbl();

            //如果日切后发生交易，则取上日余额作为计算利息税金额
            if (CommUtil.equals(trandt, tblknaFxac.getUpbldt())) {
                tranam = tblknaFxac.getLastbl();
            }
        }

        //是否启用利息税
        BigDecimal intxam = BigDecimal.ZERO;
        if (tblKnbAcin.getTxbefg() == E_YES___.YES) {
            //先计算利息税，因为计算利息的时候会更新积数日期并删除分段表中记录状态导致无法计算利息税
            CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
            if (tblKnbAcin.getPddpfg() == E_FCFLAG.CURRENT) {

                calInterTax.setTranam(tranam);
                calInterTax.setInbebs(tblKnbAcin.getTxbebs());
                calInterTax.setTrandt(DateTools2.getDateInfo().getLastdt());// 交易日期为上个交易日

            } else if (tblKnbAcin.getPddpfg() == E_FCFLAG.FIX) {
                calInterTax.setTranam(tranam);
                calInterTax.setInbebs(tblKnbAcin.getTxbebs()); //计息规则
                calInterTax.setBegndt(tblKnbAcin.getBgindt()); //起息日
                calInterTax.setEnddat(trandt); //止息日
                calInterTax.setInstam(tblKnbAcin.getPlanin()); //计提利息

                /* 如果是定期需要获取计提时的利率，用于计算分段利息税的各段利息 */
                BigDecimal cuusin = BigDecimal.ZERO; //利率
                IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
                KubInrt tblKub_inrt = SysUtil.getInstance(KubInrt.class);

                if (E_IRCDTP.Reference == tblKnbAcin.getIncdtp() || E_IRCDTP.BASE == tblKnbAcin.getIncdtp()) { //参考利率
                    //获取账户利率信息
                    tblKub_inrt = KubInrtDao.selectOne_odb1(tblknaFxac.getAcctno(), true);

                    //当利率确定日期为支取日，重新获取当前执行利率
                    cuusin = tblKub_inrt.getCuusin(); // 账户利率表执行利率

                    if (tblKnbAcin.getIntrdt() == E_INTRDT.DRAW) {

                        IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                        intrEntity.setCrcycd(tblknaFxac.getCrcycd()); //币种
                        intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码

                        //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                        intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                        intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                        intrEntity.setTrandt(CommTools.getBaseRunEnvs().getLast_date()); //结息时的确定日期为上一交易日期
                        intrEntity.setDepttm(tblknaFxac.getDepttm());// 存期
                        intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起始日期
                        intrEntity.setEdindt(trandt); //结束日期
                        intrEntity.setCainpf(E_CAINPF.T1);
                        intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                        intrEntity.setCorpno(tblKnbAcin.getCorpno());//法人代码
                        intrEntity.setBrchno(tblknaFxac.getBrchno());//机构

                        intrEntity.setLevety(tblKnbAcin.getLevety());
                        pbpub.countInteresRate(intrEntity);

                        cuusin = intrEntity.getIntrvl(); //当前执行利率

                        // 利率优惠后执行利率
                        cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKub_inrt.getFavort(), BigDecimal.ZERO).
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

                } else if (E_IRCDTP.LAYER == tblKnbAcin.getIncdtp()) {

                    IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                    intrEntity.setCorpno(tblKnbAcin.getCorpno()); //法人代码
                    intrEntity.setBrchno(tblknaFxac.getBrchno());//机构号
                    intrEntity.setTranam(tranam);//交易金额
                    intrEntity.setTrandt(trandt);//交易日期
                    intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码 
                    intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                    intrEntity.setCrcycd(tblknaFxac.getCrcycd());//币种
                    intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                    intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                    intrEntity.setCainpf(E_CAINPF.T1); //计息规则
                    intrEntity.setBgindt(tblknaFxac.getBgindt()); //起息日期
                    intrEntity.setEdindt(trandt); //止息日

                    intrEntity.setLevety(tblKnbAcin.getLevety());
                    if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
                        intrEntity.setTrandt(tblknaFxac.getOpendt());
                        intrEntity.setTrantm("999999");
                    }
                    pbpub.countInteresRate(intrEntity);

                    // 利率优惠后执行利率
                    cuusin = intrEntity.getIntrvl();
                    cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKub_inrt.getFavort(), BigDecimal.ZERO).
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

                } else { // 分层利率在利率优惠代码块中进行实现
                    throw PbError.Intr.E0007();
                }

                calInterTax.setCuusin(cuusin); //利率，设置上一日计提利率到计算利息税时使用
            }

            InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(tblKnbAcin, calInterTax, false);
            intxam = interestAndTax.getIntxam();
        }

        // 计息定义表和利息明细转付息处理
        DpInstSett cplInstSett = DpDayEndInt.prcInstDetlDayEnd(tblKnbAcin,
                CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs()
                        .getTrxn_date());

        // 将利息、税四舍五入到最小记账单位， 根据货币代号四舍五入最小记账单位
        BigDecimal instam = BusiTools.roundByCurrency(tblKnbAcin.getCrcycd(), cplInstSett.getIntest(), null);

        bizlog.parm("记账利息[%s] 记账息税[%s]", instam, intxam);

        if (CommUtil.compare(instam, BigDecimal.ZERO) < 0) {
            bizlog.parm("账号[%s]没有待支付利息, 日终利息[%s]小于0则留待下一次处理",
                    tblKnbAcin.getAcctno(), instam);

            return;
        }
        if (CommUtil.compare(instam, BigDecimal.ZERO) == 0) {
            bizlog.parm("账号[%s]没有待支付利息", tblKnbAcin.getAcctno());

            return;
        }

        //String settAcct = tblKnaAcct.getAcctno();
        String settAcct = tblKnbAcin.getAcctno();
        /*
        if (tblKnaAcct.getDebttp() == E_DEBTTP.DP2404) {
            settAcct = CapitalTransDeal.getSettKnaAcctSub(custac, E_ACSETP.SA).getAcctno();
        }*/
        if ((CommUtil.isNotNull(tblKnaAcct) && tblKnaAcct.getDebttp() == E_DEBTTP.DP2404) ||
                tblKnbAcin.getPddpfg() == E_FCFLAG.FIX) {
            settAcct = CapitalTransDeal.getSettKnaAcctSub(custac, E_ACSETP.SA).getAcctno();
        }
        prcPayMain(settAcct, instam, intxam);

        // 将利息、利息税登记到会计流水
        DpInstFaxIn cplInst = SysUtil.getInstance(DpInstFaxIn.class);

        cplInst.setEcctno(custac);
        cplInst.setAcctno(settAcct);
        cplInst.setInstam(instam); // 利息
        cplInst.setIntxam(intxam); // 利息税
        //DpDayEndInt.regACSerail(cplInst, tblKnaAcct);
        if (tblKnbAcin.getPddpfg() == E_FCFLAG.CURRENT) {
            DpDayEndInt.regACSerail(cplInst, tblKnaAcct);
        }
        if (tblKnbAcin.getPddpfg() == E_FCFLAG.FIX) {
            DpDayEndInt.regACSerailForFxac(cplInst, tblknaFxac);
        }
    }

    /**
     * 存入支取记账
     * 
     * @param acctno
     * @param instam
     * @param intxam
     */
    public static void prcPayMain(String acctno, BigDecimal instam,
            BigDecimal intxam) {
        // 存款账户入利息记账
        if (CommUtil.compare(instam, BigDecimal.ZERO) != 0) {

            IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
            // 活期账户
            //kna_accs tblKna_accs = Kna_accsDao.selectOne_odb2(acctno, true);
            //kna_acdc tblKna_acdc = Kna_acdcDao.selectOne_odb1(tblKna_accs.getCustac(), E_DPACST.NORMAL, false);
            //kna_cust tblKna_cust = Kna_custDao.selectOne_odb1(tblKna_accs.getCustac(), false);
            IoCaKnaAccs tblKna_accs = caqry.getKnaAccsOdb2(acctno, true);
            IoCaKnaAcdc tblKna_acdc = caqry.getKnaAcdcOdb1(tblKna_accs.getCustac(), E_DPACST.NORMAL, false);
            IoCaKnaCust tblKna_cust = caqry.getKnaCustByCustacOdb1(tblKna_accs.getCustac(), false);

            String subsac = tblKna_accs.getSubsac();
            String cardno = tblKna_acdc.getCardno();
            String crcycd = tblKna_accs.getCrcycd();
            String custac = tblKna_accs.getCustac();

            // 余额更新
            DpSaveEntity entity = new DpSaveEntity();
            entity.setAcctno(acctno);
            entity.setCrcycd(crcycd);
            entity.setTranam(instam);
            entity.setCardno(cardno);
            entity.setCustac(custac);
            entity.setAcseno(subsac);
            //entity.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
            //entity.setSmryds(BusiTools.getBusiRunEnvs().getRemark());
            entity.setSmrycd(BusinessConstants.SUMMARY_SX);
            entity.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_SX));
            //				entity.setOpacna(opacna);
            entity.setAuacfg(E_YES___.NO); //不是普通的存取
            DpPublicServ.postAcctDp(entity);

            // 存款账户入扣利息税
            if (CommUtil.compare(intxam, BigDecimal.ZERO) != 0) {

                // 余额更新
                entity = new DpSaveEntity();
                entity.setAcctno(acctno);
                entity.setCrcycd(crcycd);
                entity.setTranam(intxam);
                entity.setCardno(cardno);
                entity.setCustac(custac); // 对方账号
                entity.setAcseno(subsac);
                //					entity.setOpacna(opacna);
                entity.setOpbrch(tblKna_cust.getBrchno());
                entity.setAuacfg(E_YES___.NO); //不是普通的存取
                //entity.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());// 缴税
                //entity.setSmryds(BusiTools.getBusiRunEnvs().getRemark());
                entity.setSmrycd(BusinessConstants.SUMMARY_JS);
                entity.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_JS));
                DpPublicServ.drawAcctDp(entity);

            }
        }
    }

    /**
     * @Author baojk
     *         <p>
     *         <li>功能说明：利息、利息税登记会计流水登记</li>
     *         </p>
     * @param cplIn
     */
    public static void regACSerail(DpInstFaxIn cplIn, KnaAcct tblKnaAcct) {
        bizlog.parm("DpInterest.regSerail cplOut = [%s]", cplIn);

        IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
        //kna_accs tblKna_accs = Kna_accsDao.selectOne_odb2(tblKnaAcct.getAcctno(), true);
        IoCaKnaAccs tblKna_accs = caqry.getKnaAccsOdb2(tblKnaAcct.getAcctno(), true);

        // 利息金额不为零
        if (CommUtil.compare(cplIn.getInstam(), BigDecimal.ZERO) != 0) {
            String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
            String transq = CommTools.getBaseRunEnvs().getTrxn_seq();
            String mntrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq(); // 主交易流水
            //String brchno = CommTools.getBaseRunEnvs().getTrxn_branch(); // 交易机构
            String brchno = tblKnaAcct.getBrchno();
            //				/* 应入账日期 */
            //				String sYngrzriq = ApDCN.getAccountDateOneDCN("计息账号" + cplIn.getAcctno(),
            //						trandt, null);

            IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);

            cplIoAccounttingIntf.setCuacno(tblKnaAcct.getCustac());
            cplIoAccounttingIntf.setAcctno(tblKnaAcct.getAcctno());
            cplIoAccounttingIntf.setAcseno(tblKna_accs.getSubsac());
            cplIoAccounttingIntf.setProdcd(tblKnaAcct.getProdcd());
            cplIoAccounttingIntf.setDtitcd(tblKnaAcct.getAcctcd());
            cplIoAccounttingIntf.setCrcycd(tblKnaAcct.getCrcycd());
            cplIoAccounttingIntf.setTranam(cplIn.getInstam());
            cplIoAccounttingIntf.setAcctdt(trandt); // 应入账日期
            cplIoAccounttingIntf.setTransq(transq);
            cplIoAccounttingIntf.setMntrsq(mntrsq);
            cplIoAccounttingIntf.setTrandt(trandt);
            cplIoAccounttingIntf.setAcctbr(brchno);
            cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR); // 借方
            cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP); // 存款
            cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); // 账务流水
            cplIoAccounttingIntf.setBltype(E_BLTYPE.PYIN); // 余额属性：利息支出
            // 登记交易信息，供总账解析
            if (CommUtil.equals(
                    "1",
                    KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
                            true).getParm_value1())) {
                KnpParameter para = SysUtil.getInstance(KnpParameter.class);
                para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%",
                        "%", true);
                cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息
            }
            SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                    cplIoAccounttingIntf);

            // 利息税金额不为零
            if (CommUtil.compare(cplIn.getIntxam(), BigDecimal.ZERO) != 0) {

                cplIoAccounttingIntf.setCuacno(tblKnaAcct.getCustac());
                cplIoAccounttingIntf.setAcctno(tblKnaAcct.getAcctno());
                cplIoAccounttingIntf.setAcseno(tblKna_accs.getSubsac());
                cplIoAccounttingIntf.setProdcd(tblKnaAcct.getProdcd());
                cplIoAccounttingIntf.setDtitcd(tblKnaAcct.getAcctcd());
                cplIoAccounttingIntf.setCrcycd(tblKnaAcct.getCrcycd());
                cplIoAccounttingIntf.setTranam(cplIn.getIntxam()); // 利息税
                cplIoAccounttingIntf.setAcctdt(trandt); // 应入账日期
                cplIoAccounttingIntf.setTransq(transq);
                cplIoAccounttingIntf.setMntrsq(mntrsq); // 主交易流水
                cplIoAccounttingIntf.setTrandt(trandt); // 账务日期
                cplIoAccounttingIntf.setAcctbr(brchno);
                cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR);
                cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
                cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
                cplIoAccounttingIntf.setBltype(E_BLTYPE.INTAX);
                // 登记交易信息，供总账解析
                if (CommUtil.equals(
                        "1",
                        KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
                                true).getParm_value1())) {
                    KnpParameter para = SysUtil.getInstance(KnpParameter.class);
                    para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3020100", "%",
                            "%", true);
                    cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息
                }
                SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                        cplIoAccounttingIntf);

            }
        }
    }

    /**
     * @Author baojk
     *         <p>
     *         <li>功能说明：定期利息、利息税登记会计流水登记</li>
     *         </p>
     * @param cplIn
     */
    public static void regACSerailForFxac(DpInstFaxIn cplIn, KnaFxac tblKnaFxac) {
        bizlog.parm("DpInterest.regSerail cplOut = [%s]", cplIn);

        IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
        //kna_accs tblKna_accs = Kna_accsDao.selectOne_odb2(tblKnaFxac.getAcctno(), true);
        IoCaKnaAccs tblKna_accs = caqry.getKnaAccsOdb2(tblKnaFxac.getAcctno(), true);

        // 利息金额不为零
        if (CommUtil.compare(cplIn.getInstam(), BigDecimal.ZERO) != 0) {
            String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
            String transq = CommTools.getBaseRunEnvs().getTrxn_seq();
            String mntrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq(); // 主交易流水
            IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);

            cplIoAccounttingIntf.setCuacno(tblKnaFxac.getCustac());
            cplIoAccounttingIntf.setAcctno(tblKnaFxac.getAcctno());
            cplIoAccounttingIntf.setAcseno(tblKna_accs.getSubsac());
            cplIoAccounttingIntf.setProdcd(tblKnaFxac.getProdcd());
            cplIoAccounttingIntf.setDtitcd(tblKnaFxac.getAcctcd());
            cplIoAccounttingIntf.setCrcycd(tblKnaFxac.getCrcycd());
            cplIoAccounttingIntf.setTranam(cplIn.getInstam());
            cplIoAccounttingIntf.setAcctdt(trandt); // 应入账日期
            cplIoAccounttingIntf.setTransq(transq);
            cplIoAccounttingIntf.setMntrsq(mntrsq);
            cplIoAccounttingIntf.setTrandt(trandt);
            cplIoAccounttingIntf.setAcctbr(tblKnaFxac.getBrchno());
            cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR); // 借方
            cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP); // 存款
            cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); // 账务流水
            cplIoAccounttingIntf.setBltype(E_BLTYPE.PYIN); // 余额属性：利息支出
            // 登记交易信息，供总账解析
            if (CommUtil.equals(
                    "1",
                    KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
                            true).getParm_value1())) {
                KnpParameter para = SysUtil.getInstance(KnpParameter.class);
                para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%",
                        "%", true);
                cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息
            }
            SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                    cplIoAccounttingIntf);

            // 利息税金额不为零
            if (CommUtil.compare(cplIn.getIntxam(), BigDecimal.ZERO) != 0) {

                cplIoAccounttingIntf.setCuacno(tblKnaFxac.getCustac());
                cplIoAccounttingIntf.setAcctno(tblKnaFxac.getAcctno());
                cplIoAccounttingIntf.setAcseno(tblKna_accs.getSubsac());
                cplIoAccounttingIntf.setProdcd(tblKnaFxac.getProdcd());
                cplIoAccounttingIntf.setDtitcd(tblKnaFxac.getAcctcd());
                cplIoAccounttingIntf.setCrcycd(tblKnaFxac.getCrcycd());
                cplIoAccounttingIntf.setTranam(cplIn.getIntxam()); // 利息税
                cplIoAccounttingIntf.setAcctdt(trandt); // 应入账日期
                cplIoAccounttingIntf.setTransq(transq);
                cplIoAccounttingIntf.setMntrsq(mntrsq); // 主交易流水
                cplIoAccounttingIntf.setTrandt(trandt); // 账务日期
                cplIoAccounttingIntf.setAcctbr(tblKnaFxac.getBrchno());
                cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR);
                cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
                cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
                cplIoAccounttingIntf.setBltype(E_BLTYPE.INTAX);
                // 登记交易信息，供总账解析
                if (CommUtil.equals(
                        "1",
                        KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
                                true).getParm_value1())) {
                    KnpParameter para = SysUtil.getInstance(KnpParameter.class);
                    para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3020100", "%",
                            "%", true);
                    cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息
                }
                SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                        cplIoAccounttingIntf);

            }
        }
    }
}
