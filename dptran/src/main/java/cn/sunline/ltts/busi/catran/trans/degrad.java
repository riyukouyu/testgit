package cn.sunline.ltts.busi.catran.trans;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.namedsql.AccountLimitDao;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbDeom;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbDeomDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbProm;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbPromDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppbProtn;
import cn.sunline.ltts.busi.iobus.type.cf.IoCuTable.IoCucifCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAgrtInfos;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.errors.CaError.Eacct;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.WaError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CKTNTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PROMST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PROMSV;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_MEDIUM;
import cn.sunline.ltts.busi.sys.type.WaEnumType.E_RELTST;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class degrad {

    private static BizLog bizlog = BizLogUtil.getBizLog(upgrad.class);
    private static KnpParameter tblKnpParameter2 = KnpParameterDao.selectOne_odb1("IsOpacctOne", "OpenWallet", "%", "%", false);

    public static void prcDegBefore(
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Input input,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Property property,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Output output) {

        String sCardno = input.getCardno();// 电子账号
        E_ACCATP eOdactp = input.getOdactp();// 原账户分类
        E_ACCATP eDwactp = input.getDwactp();// 降级账户分类
        String version = input.getVesion();// 版本号
        String agrtno = input.getAgrtno();// 协议模板号

        // 非空字段判断
        if (CommUtil.isNull(sCardno)) {
            throw Eacct.BNAS0570();
        }
        if (CommUtil.isNull(eOdactp)) {
            throw Eacct.BNAS1291();
        }
        if (CommUtil.isNull(eDwactp)) {
            throw Eacct.BNAS1695();
        }
        if (CommUtil.isNull(version)) {
            throw Eacct.BNAS1295();
        }
        if (CommUtil.isNull(agrtno)) {
            throw Eacct.BNAS1696();
        }
        // 查询卡客户账户对照表
        KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(sCardno, false);
        if (CommUtil.isNull(tblKnaAcdc)
                || tblKnaAcdc.getStatus() != E_DPACST.NORMAL) {
            throw Eacct.BNAS0568();
        }
        // 查询电子账户表
        KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(
                tblKnaAcdc.getCustac(), false);
        if (CommUtil.isNull(tblKnaCust)) {
            throw Eacct.BNAS1205();
        }

        String sCustac = tblKnaAcdc.getCustac();// 电子账号ID

        // 检查电子账户状态
        E_CUACST eCustac = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
                .selCaStInfo(sCustac);
        if (eCustac == E_CUACST.PREOPEN) {
            throw CaError.Eacct.BNAS0441();
        }
        if (eCustac == E_CUACST.NOENABLE) {
            throw CaError.Eacct.BNAS0441();
        }
        if (eCustac == E_CUACST.DORMANT) {
            throw CaError.Eacct.BNAS0441();
        }
        if (eCustac == E_CUACST.OUTAGE) {
            throw CaError.Eacct.BNAS0441();
        }
        if (eCustac == E_CUACST.PRECLOS) {
            throw CaError.Eacct.BNAS0441();
        }
        if (eCustac == E_CUACST.CLOSED) {
            throw CaError.Eacct.BNAS0441();
        }

        // 调用DP模块服务查询冻结状态，检查电子账户状态字
        // 调用查询电子账户状态字服务
        IoDpFrozSvcType ioDpFrozSvcType = SysUtil
                .getInstance(IoDpFrozSvcType.class);
        IoDpAcStatusWord cplAcStatusWord = ioDpFrozSvcType
                .getAcStatusWord(sCustac);

        if (E_YES___.YES == cplAcStatusWord.getPtfroz()) {
            throw WaError.Wacct.E0001("电子账户已冻结！");
        }
        if (E_YES___.YES == cplAcStatusWord.getDbfroz()) {
            // throw DpError.DeptFroz.E0815();
            throw DpModuleError.DpstComm.BNAS0430();
        }

        if (E_YES___.YES == cplAcStatusWord.getBrfroz()) {
            // throw DpError.DeptFroz.E0816();
            throw DpModuleError.DpstComm.BNAS0432();
        }
        if (E_YES___.YES == cplAcStatusWord.getPtstop()) {
            throw WaError.Wacct.E0001("电子账户已冻结！");

        }
        if (E_YES___.YES == cplAcStatusWord.getBkalsp()) {
            // throw DpError.DeptFroz.E0817();
            throw WaError.Wacct.E0001("电子账户已冻结！");
        }

        if (E_YES___.YES == cplAcStatusWord.getClstop()) {
            // throw DpError.DeptFroz.E0818();
            throw WaError.Wacct.E0001("电子账户已冻结！");
        }

        if (E_YES___.YES == cplAcStatusWord.getOtalsp()) {
            // throw DpError.DeptFroz.E0819();
            throw WaError.Wacct.E0001("电子账户已冻结！");
        }
        if (E_YES___.YES == cplAcStatusWord.getCertdp()) {
            throw WaError.Wacct.E0001("电子账户已冻结！");
        }

        // 查询电子账户分类，并判断该电子账号是否允许降级
        E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
                .qryAccatpByCustac(tblKnaCust.getCustac());

        if (input.getOdactp() != eAccatp) {
            throw Eacct.E0001("原账户分类与电子账户分类不一致");
        }

        if (eAccatp == E_ACCATP.WALLET) {
            throw Eacct.E0001("电子钱包账户不允许降级！");
        }
        if (eAccatp == E_ACCATP.GLOBAL && eDwactp == E_ACCATP.GLOBAL) {
            throw Eacct.E0001("全功能账户不允许降级为全功能账户");
        }
        if (eAccatp == E_ACCATP.FINANCE && eDwactp != E_ACCATP.WALLET) {
            throw Eacct.E0001("理财账户只能降级为钱包账户");
        }

        // 查询是否存在升级申请
        KnbProm tblKnbProm1 = KnbPromDao.selectFirst_odb2(sCustac,
                E_PROMST.APPLY, false);
        KnbProm tblKnbProm2 = KnbPromDao.selectFirst_odb2(sCustac,
                E_PROMST.ACCEPTED, false);
        if (CommUtil.isNotNull(tblKnbProm1) || CommUtil.isNotNull(tblKnbProm2)) {
            throw Eacct.E0001("存在升级申请、已受理！");
        }

        // 判断电子账户降级为电子钱包账户
        if (eDwactp == E_ACCATP.WALLET) {

            // 检查是否存款未到期的存款产品、理财产品、和亲情钱包账户
            BigDecimal count = EacctMainDao.selProdInfoByCustac(sCustac, false);
            if (CommUtil.compare(count, BigDecimal.ZERO) > 0) {
                throw Eacct.E0001("存款未到期的存款产品、理财产品、和亲情钱包账户！");
            }

            // 检查是否存在待确认的亲情钱包关系
//            IoWaKnaReltCount.Output reltCount = SysUtil.getInstance(IoWaKnaReltCount.Output.class);
//            SysUtil.getInstance(IoWaSrvWalletAccountType.class).IoWaKnaReltCount(sCustac, E_RELTST.CLOSE, reltCount);
//            int num = reltCount.getNum();
//            if (CommUtil.isNotNull(num) && num > 0) {
//                throw Eacct.E0001("存在待确认的亲情钱包关系");
//            }
        }

        // 查询电子账户个人结算户信息
        IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(IoDpKnaAcct.class);
        if (input.getDwactp() == E_ACCATP.FINANCE) {
            cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class)
                    .getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.SA);
            //			if (CommUtil.compare(BusiTools.getDefineCurrency(), cplKnaAcct.getCrcycd()) == 0 ) {
            //				throw Eacct.E0001("币种必须为人民币");
            //			}
            // 将结算户负债子账号映射到属性区，提供新开负债账户后的签约处理
            property.setAcctnm(cplKnaAcct.getAcctno());// 需解约的负债账号,后面更新签约明细表会用到

        } else if (input.getDwactp() == E_ACCATP.WALLET) {
            if (CommUtil.isNull(tblKnpParameter2) || "0".equals(tblKnpParameter2.getParm_value1())) {
                cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class)
                        .getSettKnaAcctSub(tblKnaAcdc.getCustac(), E_ACSETP.MA);
                property.setAcctno(cplKnaAcct.getAcctno());// 钱包账户账号
            }
        }

        // 查询客户信息表
//        IoCucifCust cplCifCust = SysUtil.getInstance(
//                IoCuSevQryTableInfo.class).cif_cust_selectOne_odb1(
//                tblKnaCust.getCustno(), true);

        // 根据电子账号查询用户ID
        //		IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(
        //				IoCuSevQryTableInfo.class).cif_cust_accs_selectFirst_odb2(
        //						tblKnaCust.getCustno(), E_STATUS.CLOSE , false);
        //
        //		if (CommUtil.isNull(cplCifCustAccs)) {
        //			throw Eacct.E0001("客户信息关联信息不存在");
        //		}

        // 查询绑定手机号
        KnaAcal tblKnaAcal = CaDao.selKnaAcalByCustac(tblKnaAcdc.getCustac(),
                E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);
        if (CommUtil.isNotNull(tblKnaAcal)) {
            property.setAcalno(tblKnaAcal.getTlphno());// 电子账户绑定手机号
        }

        // 根据降级账户分类获取开户产品号
        KnpParameter tblKnpParameter = SysUtil.getInstance(DpProdSvcType.class)
                .qryProdcd(input.getDwactp());

        property.setCustac(sCustac);
        property.setCustna(tblKnaCust.getCustna());
        property.setBrchno(tblKnaCust.getBrchno());
//        property.setIdtftp(cplCifCust.getIdtftp());
//        property.setIdtfno(cplCifCust.getIdtfno());
        if (CommUtil.isNull(tblKnpParameter2) || "0".equals(tblKnpParameter2.getParm_value1())) {
            property.setCrcycd(cplKnaAcct.getCrcycd());
        } else {
            property.setCrcycd("CNY");
        }
        property.setCustno(tblKnaCust.getCustno());
        //		property.setCustid(cplCifCustAccs.getCustid());
        property.setCacttp(tblKnaCust.getCacttp());
        property.setBaprcd(tblKnpParameter.getParm_value1());
        property.setWaprcd(tblKnpParameter.getParm_value2());
        BusiTools.getBusiRunEnvs().setRemark("手机端降级");

        property.setIonefg(E_YES___.NO);
        if (CommUtil.isNotNull(tblKnpParameter2) && "1".equals(tblKnpParameter2.getParm_value1())) {
            property.setIonefg(E_YES___.YES);
        }
    }

    // 登记电子账户降级登记簿
    public static void caAccountDegrade(
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Input input,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Property property,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Output output) {
        // 登记电子账户降级登记簿
        KnbDeom tblKnbDeom = SysUtil.getInstance(KnbDeom.class);
        tblKnbDeom.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
        tblKnbDeom.setCustac(property.getCustac());
        tblKnbDeom.setCustna(property.getCustna());
        tblKnbDeom.setOdactp(input.getOdactp());
        tblKnbDeom.setDeactp(input.getDwactp());
        tblKnbDeom.setBrchno(property.getBrchno());
        tblKnbDeom.setIdtftp(property.getIdtftp());
        tblKnbDeom.setIdtfno(property.getIdtfno());
        tblKnbDeom.setAplydt(CommTools.getBaseRunEnvs().getTrxn_date());// 申请日期
        tblKnbDeom.setDeomsv(E_PROMSV.TELCLIENT);// 降级渠道
        tblKnbDeom.setDeomdt(CommTools.getBaseRunEnvs().getTrxn_date());// 降级日期
        tblKnbDeom.setDeomst(E_PROMST.SUCCESS);// 降级状态
        tblKnbDeom.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
        tblKnbDeom.setMesssq(CommTools.getBaseRunEnvs().getTrxn_seq());
        KnbDeomDao.insert(tblKnbDeom);
    }

    // 检查结息后的钱包账户的余额是否大于最大限额
    public static void chkAcctBal(
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Input input,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Property property,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Output output) {
        if (CommUtil.isNull(tblKnpParameter2) || "0".equals(tblKnpParameter2.getParm_value1())) {
            // 根据负债账号查询负债账号信息
            IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(
                    IoDpSrvQryTableInfo.class).getKnaAcctOdb1(
                    property.getAcctno(), true);
            BigDecimal num = BigDecimal.valueOf(1000L);
            // Integer a = new Integer(1000);
            // BigDecimal num = new BigDecimal(a);
            if (CommUtil.compare(cplKnaAcct.getOnlnbl(), num) > 0) {
                throw Eacct.E0001("当前账户余额不能大于1000");
            }
        }
    }

    // 将原一类户的签约明细信息更新为二类户的负债账号和基础产品号
    public static void upSigndetlInfo(
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Input input,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Property property,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Output output) {

        //		String mtdate = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期

        String timetm = DateTools2.getCurrentTimestamp();

        // 将签约信息更新为二类户的负债账户签约
        CaDao.updKnaSignDetlinfo(property.getAcctno(), property.getBaprcd(),
                property.getAcctnm(), E_SIGNST.QY, timetm);

        SysUtil.getInstance(IoCheckBalance.class).checkBalance(
                CommTools.getBaseRunEnvs().getTrxn_date(),
                CommTools.getBaseRunEnvs().getMain_trxn_seq(), null);

    }

    public static void prcTransInform(
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Input input,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Property property,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Output output) {

        E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE; // 消息媒介

        KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("OPUPGD", "CUSTSM", "%",
                "%", true);

        String bdid = tblKnaPara.getParm_value1();// 服务绑定ID

        IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind(
                IoCaOtherService.class, bdid);

        // 1.降级成功发送降级结果到客户信息
        String mssdid = CommTools.getMySysId();// 消息ID
        String mesdna = tblKnaPara.getParm_value2();// 媒介名称

        IoCaOtherService.IoCaOpenUpgSendMsg.InputSetter upgdSendMsgInput = SysUtil.getInstance(IoCaOtherService.IoCaOpenUpgSendMsg.InputSetter.class);

        upgdSendMsgInput.setMsgid(mssdid); // 发送消息ID
        upgdSendMsgInput.setMedium(mssdtp); // 消息媒介
        upgdSendMsgInput.setMdname(mesdna); // 媒介名称
        upgdSendMsgInput.setOpacrt(E_YES___.YES);// 是否开户成功
        upgdSendMsgInput.setCustid(property.getCustid());// 用户ID
        upgdSendMsgInput.setBrchno(property.getBrchno());// 机构号
        upgdSendMsgInput.setAccatp(input.getDwactp());// 账户分类
        upgdSendMsgInput.setCktntp(E_CKTNTP.DEGRADE);// 交易类型

        caOtherService.openUpgSendMsg(upgdSendMsgInput);

        // 2.降级成功发送协议到合约库
        KnpParameter tblKnaPara1 = KnpParameterDao.selectOne_odb1("OPUPGD", "AGRTSM",
                "%", "%", true);

        String mssdid1 = CommTools.getMySysId();// 消息ID
        String mesdna1 = tblKnaPara1.getParm_value2();// 媒介名称

        IoCaOtherService.IoCaSendContractMsg.InputSetter upgdSendAgrtInput = SysUtil.getInstance(IoCaOtherService.IoCaSendContractMsg.InputSetter.class);

        String sAgdata = property.getIdtftp() + "|" + property.getIdtfno()
                + "|" + property.getCustna() + "|" + input.getCardno() + "|"
                + input.getDwactp() + "|" + property.getBrchno() + "|"
                + CommTools.getBaseRunEnvs().getTrxn_date();// 协议回填字段

        upgdSendAgrtInput.setMsgid(mssdid1); // 发送消息ID
        upgdSendAgrtInput.setMedium(mssdtp); // 消息媒介
        upgdSendAgrtInput.setMdname(mesdna1); // 媒介名称
        upgdSendAgrtInput.setUserId(property.getCustid());// 用户ID
        upgdSendAgrtInput.setOpenOrg(property.getBrchno());// 机构号
        upgdSendAgrtInput.setAcctNo(input.getCardno());// 电子账号
        upgdSendAgrtInput.setAcctName(property.getCustna());// 户名
        upgdSendAgrtInput.setRecordCount(ConvertUtil.toInteger(1));// 记录数
        upgdSendAgrtInput.setOpenFlag(E_CKTNTP.DEGRADE);// 开户降级级标志
        upgdSendAgrtInput.setTransTime(DateTools2.getCurrentTimestamp());// 操作时间
        upgdSendAgrtInput.setAcctType(input.getDwactp());
        upgdSendAgrtInput.setIdType(property.getIdtftp());
        upgdSendAgrtInput.setIdNumber(property.getIdtfno());

        // modify by songkl 2017/06/13 需求修改，模板编号有一个变多个
        // 电子账户降级级后只能是二类或三类类户 所以产品代码取结算户产品代码
        String prodcd = null;
        if (input.getDwactp() == E_ACCATP.GLOBAL
                || input.getDwactp() == E_ACCATP.FINANCE) {
            prodcd = property.getBaprcd();
        }
        // 若电子账户为钱包账户则取钱包账户产品号
        if (input.getDwactp() == E_ACCATP.WALLET) {
            prodcd = property.getWaprcd();
        }
        bizlog.debug("+++++prodcd001+++++" + prodcd);
        // 根据产品号获取协议模板编号kup_dppb_prot
        List<IoDpKupDppbProtn> tblkupdppbprot = CaDao.selkupdppbprotbyprod(
                prodcd, true);

        // 将协议模板编号插入列表
        for (IoDpKupDppbProtn Iodpprot : tblkupdppbprot) {
            IoCaAgrtInfos cplAgrtInfos = SysUtil.getInstance(IoCaAgrtInfos.class);
            cplAgrtInfos.setAgrTemplateNo(Iodpprot.getProtno());// 协议模板编号
            cplAgrtInfos.setVersion(input.getVesion());// 版本号
            cplAgrtInfos.setAgrData(sAgdata);// 协议回填字段
            upgdSendAgrtInput.getAgreementList().add(cplAgrtInfos);// 协议列表
        }

        bizlog.debug("++++++++协议列表+++++："
                + upgdSendAgrtInput.getAgreementList());

        // IoCaAgrtInfos cplAgrtInfos =
        // SysUtil.getInstance(IoCaAgrtInfos.class);
        // cplAgrtInfos.setAgrTemplateNo(input.getAgrtno());// 协议模板编号
        // cplAgrtInfos.setVersion(input.getVesion());// 版本号
        // cplAgrtInfos.setAgrData(sAgdata);// 协议回填字段
        // upgdSendAgrtInput.getAgreementList().add(cplAgrtInfos);// 协议列表

        caOtherService.sendContractMsg(upgdSendAgrtInput);

        // 3.降级为二类户成功将理财签约相关信息发送到合约库
        if (input.getDwactp() == E_ACCATP.FINANCE) {
            KnpParameter tblKnaPara3 = KnpParameterDao.selectOne_odb1("OPENMN", "AGRTSM",
                    "%", "%", true);

            String mssdid2 = CommTools.getMySysId();// 消息ID
            String mesdna2 = tblKnaPara3.getParm_value2();// 媒介名称

            IoCaOtherService.IoCaSendMonMsg.InputSetter sendMonMsgInput = SysUtil.getInstance(IoCaOtherService.IoCaSendMonMsg.InputSetter.class);

            sendMonMsgInput.setMedium(mssdtp); // 消息媒介
            sendMonMsgInput.setMsgid(mssdid2); // 发送消息ID
            sendMonMsgInput.setMdname(mesdna2); // 媒介名称
            sendMonMsgInput.setUserId(property.getCustid());// 用户ID
            sendMonMsgInput.setAcctNo(input.getCardno());// 电子账号
            sendMonMsgInput.setAcctName(property.getCustna());// 客户姓名
            sendMonMsgInput.setCertNo(property.getIdtfno());// 证件号码
            sendMonMsgInput.setCertType(property.getIdtftp());// 证件类型
            sendMonMsgInput.setTransBranch(property.getBrchno());// 机构编号
            sendMonMsgInput.setMobileNo(property.getAcalno());// 绑定手机号
            sendMonMsgInput.setTransTime(DateTools2.getCurrentTimestamp());// 操作时间

            caOtherService.sendMonMsg(sendMonMsgInput);
        }
    }

    public static void updTrdStatus(
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Input input,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Property property,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Output output) {

        // 登记电子账户状态
        IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
        cplDimeInfo.setCustac(property.getCustac());
        // cplDimeInfo.setDime01(input.getOdactp().getValue());
        cplDimeInfo.setFacesg(null);// 面签标识
        SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(
                cplDimeInfo);

    }

    public static void clearTsscTram(
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Input input,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Property property,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Output output) {

        AccountLimitDao.updTsscAcct(property.getCustac());
        // .updTsscAcct(property.getCustac(), CommTools// .prcRunEnvs().getTrandt());

    }

    public static void afterTrans(
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Input input,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Property property,
            final cn.sunline.ltts.busi.catran.trans.intf.Degrad.Output output) {
        output.setCardno(input.getCardno());
        output.setCustna(property.getCustna());
        output.setIdtfno(property.getIdtfno());
        output.setIdtftp(property.getIdtftp());
    }
}
