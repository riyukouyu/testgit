package cn.sunline.ltts.busi.dptran.batchtran;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublicServ;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.domain.DpOpenAcctEntity;
import cn.sunline.ltts.busi.dp.domain.DpSaveEntity;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDprebypd;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDprebypdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.type.DpPoc.BuyProductKey;
import cn.sunline.ltts.busi.dptran.batchtran.intf.Dptmre.Input;
import cn.sunline.ltts.busi.dptran.batchtran.intf.Dptmre.Property;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaStaPublic;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEARelaIn;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CUSTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CYCLES;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_REBYPE;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_REBYST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SIGNTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRDPTP;
import cn.sunline.edsp.base.lang.Params;


/**
 * 日终预约购买产品
 * 
 */

public class dptmreDataProcessor extends
        AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.intf.Dptmre.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Dptmre.Property, cn.sunline.ltts.busi.dp.type.DpPoc.BuyProductKey> {

    // 预约购买产品信息记录表
    private KupDprebypd tblKupDprebypd = SysUtil.getInstance(KupDprebypd.class);

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
    public void process(String jobId, int index, cn.sunline.ltts.busi.dp.type.DpPoc.BuyProductKey dataItem, cn.sunline.ltts.busi.dptran.batchtran.intf.Dptmre.Input input,
            cn.sunline.ltts.busi.dptran.batchtran.intf.Dptmre.Property property) {
        // 预约购买产品信息记录表
        tblKupDprebypd = KupDprebypdDao.selectOne_odb1(dataItem.getCardno(), dataItem.getProdcd(), false);

        // 检查卡号的合法性
        IoCaKnaAcdc otacdc = ActoacDao.selKnaAcdc(dataItem.getCardno(), false);
        if (CommUtil.isNull(otacdc)) {
            tblKupDprebypd.setRebyst(E_REBYST.STOP); // 预约状态
            // 更新预约购买产品信息记录表为暂停状态
            KupDprebypdDao.updateOne_odb1(tblKupDprebypd);
            return;
        }
        if (otacdc.getStatus() == E_DPACST.CLOSE) {
            tblKupDprebypd.setRebyst(E_REBYST.STOP); // 预约状态
            // 更新预约购买产品信息记录表为暂停状态
            KupDprebypdDao.updateOne_odb1(tblKupDprebypd);
            return;
        }

        // 查询电子账户分类
        E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(otacdc.getCustac());
        //获取结算户或钱包户
        KnaAcct otacct = SysUtil.getInstance(KnaAcct.class); //转出方子账号
        if (accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE) {
            otacct = CapitalTransDeal.getSettKnaAcctSub(otacdc.getCustac(), E_ACSETP.SA);
        } else {
            otacct = CapitalTransDeal.getSettKnaAcctSub(otacdc.getCustac(), E_ACSETP.MA);
        }

        BigDecimal usebal = SysUtil.getInstance(DpAcctSvcType.class)
                .getAcctaAvaBal(otacdc.getCustac(), otacct.getAcctno(),
                        otacct.getCrcycd(), E_YES___.YES, E_YES___.NO);

        BigDecimal cktranam = BigDecimal.ZERO;
        BigDecimal tranam = BigDecimal.ZERO;
        if (E_REBYPE.DEGM == tblKupDprebypd.getRebype()) {
            cktranam = tblKupDprebypd.getQuanmy();
            tranam = tblKupDprebypd.getQuanmy();
        } else if (E_REBYPE.CEGM == tblKupDprebypd.getRebype()) {
            cktranam = tblKupDprebypd.getHoldmy();
            tranam = usebal.subtract(tblKupDprebypd.getHoldmy());
        }
        // 检查账户余额
        if (CommUtil.compare(usebal, cktranam) < 0) {
            StringBuilder SnRenxdt = new StringBuilder();
            if (E_CYCLES.DAY == tblKupDprebypd.getCycles()) {
                SnRenxdt.append(tblKupDprebypd.getRecycl()).append(tblKupDprebypd.getCycles().getValue()).append("A");
            } else {
                SnRenxdt.append(tblKupDprebypd.getRecycl()).append(tblKupDprebypd.getCycles().getValue()).append("A").append("D");
            }
            String renxdt = DateTools2.calDateByFreq(tblKupDprebypd.getRenxdt(), SnRenxdt.toString());
            // 更新预约购买产品信息记录表为下次预约日期
            tblKupDprebypd.setRenxdt(renxdt);
            KupDprebypdDao.updateOne_odb1(tblKupDprebypd);
            return;
        }

        //处理368及788两款定存产品每个客户只能购买一款
        String prodcdOne = "010010003";
        String prodcdTwo = "010010004";
        int saveCount = 1; //限制份数

        String custac = otacdc.getCustac(); //电子账号
        String custno = null; //客户号
        //kna_cust tblKna_cust = null;
        IoCaKnaCust tblKna_cust = SysUtil.getInstance(IoCaKnaCust.class);
        KupDppb tblKupDppb1 = KupDppbDao.selectOne_odb1(prodcdOne, true);
        KupDppb tblKupDppb2 = KupDppbDao.selectOne_odb1(prodcdTwo, true);

        //电子账号
        if (CommUtil.isNull(custac)) {
            throw DpModuleError.DpstComm.BNAS0955();
        } else {
            //tblKna_cust = Kna_custDao.selectOne_odb1(custac, false);
            tblKna_cust = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(custac, false);
            if (CommUtil.isNull(tblKna_cust)) {
                throw DpModuleError.DpstComm.BNAS0754();
            }
            if (E_ACCTST.NORMAL != tblKna_cust.getAcctst()) {
                throw DpModuleError.DpstAcct.BNAS0905(tblKna_cust.getAcctst().getLongName());
            }

            custno = tblKna_cust.getCustno(); //客户号
        }

        List<KnaFxac> lstKnaFxac = DpAcctQryDao.selKnaFxacCountByCustno(prodcdOne, prodcdTwo, custno, false);

        if (CommUtil.isNotNull(lstKnaFxac) && lstKnaFxac.size() >= saveCount) {
            KnaFxac tblKnaFxac = lstKnaFxac.get(0);
            String prodtx = null;
            if (CommUtil.equals(tblKnaFxac.getProdcd(), prodcdOne)) {
                prodtx = tblKupDppb1.getProdtx();
            } else if (CommUtil.equals(tblKnaFxac.getProdcd(), prodcdTwo)) {
                prodtx = tblKupDppb2.getProdtx();
            }
            throw DpModuleError.DpstAcct.BNAS0247(prodtx);
        }

        String prodcd = tblKupDprebypd.getProdcd(); // 产品编号
        String crcycd = tblKupDprebypd.getCrcycd(); // 币种
        E_TERMCD depttm = tblKupDprebypd.getDepttm(); // 存期
        // kna_cust tblKna_cust = null; //电子账户信息
        // kna_accs tblKna_accs = SysUtil.getInstance(kna_accs.class);
        // //电子账户与子账户关联
        IoCaKnaAccs tblKna_accs = SysUtil.getInstance(IoCaKnaAccs.class);
        String custna = null; // 客户名称

        // 货币代号
        if (CommUtil.isNull(crcycd)) {
            throw CaError.Eacct.BNAS1690();
        }

        // 产品编号
        if (CommUtil.isNull(prodcd)) {
            throw DpModuleError.DpstComm.BNAS1480();
        }

        // 存期
        if (CommUtil.isNull(depttm)) {
            throw DpModuleError.DpstProd.BNAS1025();
        }

        // 交易金额
        if (CommUtil.isNull(tranam)) {
            throw DpModuleError.DpstAcct.BNAS0623();
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
                throw DpModuleError.DpstComm.BNAS0754();
            }
            if (E_ACCTST.NORMAL != tblKna_cust.getAcctst()) {
                throw DpModuleError.DpstAcct.BNAS0905(tblKna_cust.getAcctst().getLongName());
            }

            custna = tblKna_cust.getCustna(); // 客户名称
            custno = tblKna_cust.getCustno(); // 客户号
        }

        // 检查客户是否符合购买条件
        KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("FreeDefineDP_CK",
                prodcd, "%", "%", false);

        if (CommUtil.isNotNull(tblKnpParameter)
                && CommUtil.isNotNull(tblKnpParameter.getParm_value1())) {

//            IoSrvCfPerson.IoGetCifCust.InputSetter queryCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//            IoSrvCfPerson.IoGetCifCust.Output tblCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//            IoSrvCfPerson cifCustServ = SysUtil.getInstance(IoSrvCfPerson.class);
//            queryCifCust.setCustno(custno);
//            cifCustServ.getCifCust(queryCifCust, tblCifCust);

//            if (CommUtil.isNull(tblCifCust)) {
//                throw DpModuleError.DpstAcct.BNAS0946(custac);
//            } else {
//                E_IDTFTP idtftp = tblCifCust.getIdtftp(); // 证件类型
//                String idtfno = tblCifCust.getIdtfno(); // 证件号码
//                if (CommUtil.isNotNull(idtfno)
//                        && (E_IDTFTP.LS == idtftp || E_IDTFTP.SFZ == idtftp)
//                        && CommUtil.equals(idtfno.substring(0, 2),
//                                tblKnpParameter.getParm_value1())) {
//
//                    throw DpModuleError.DpstProd.BNAS1117(tblKnpParameter.getParm_value5());
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
        entity.setCusttp(E_CUSTTP.PERSON);// 客户类型
        entity.setCacttp(tblKna_cust.getCacttp()); // 客户账号类型
        entity.setOpacfg(E_YES___.NO); // 首开户标志
        entity.setTranam(tranam); // 存入金额
        DpPublicServ.openAcct(entity);

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
            input_save.setAuacfg(E_YES___.NO);// 不是普通的智能储蓄存取
            DpPublicServ.postAcctDp(input_save);
        }

        // 签约
        DpPublicServ.prcDpSign(entity.getAcctno(), E_SIGNTP.TRDP,
                custac, custno, prodcd, E_TRDPTP.TRDPCR, null,
                E_YES___.YES);

        //调支取服务
        DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
        drawDpAcctIn.setAcctno(otacct.getAcctno());
        drawDpAcctIn.setCustac(otacdc.getCustac());
        drawDpAcctIn.setCardno(dataItem.getCardno());
        drawDpAcctIn.setOpacna(otacct.getAcctna());
        drawDpAcctIn.setToacct(otacct.getCustac());
        drawDpAcctIn.setCrcycd(crcycd);
        drawDpAcctIn.setIschck(E_YES___.NO);
        drawDpAcctIn.setTranam(tranam);
        drawDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_ZC);
        drawDpAcctIn.setRemark(null);
        SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawDpAcctIn);

        //平衡性检查
        String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
        String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();

        IoCheckBalance ioCheckBalance = SysUtil.getInstance(IoCheckBalance.class);
        ioCheckBalance.checkBalance(trandt, transq, null);
    }

    /**
     * 获取数据遍历器。
     * 
     * @param input 批量交易输入接口
     * @param property 批量交易属性接口
     * @return 数据遍历器
     */
    @Override
    public BatchDataWalker<cn.sunline.ltts.busi.dp.type.DpPoc.BuyProductKey> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.intf.Dptmre.Input input,
            cn.sunline.ltts.busi.dptran.batchtran.intf.Dptmre.Property property) {
        // 参数实例化
        Params parm = new Params();
        parm.put("renxdt", CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
        // 查询满足条件的购买产品的账号
        return new CursorBatchDataWalker<BuyProductKey>(DpProductDao.namedsql_selKupDprebypdKey, parm);
    }

    @Override
    public void jobExceptionProcess(String taskId, Input input, Property property, String jobId, BuyProductKey dataItem, Throwable t) {
        super.jobExceptionProcess(taskId, input, property, jobId, dataItem, t);
        // 预约购买产品信息记录表
        tblKupDprebypd.setRebyst(E_REBYST.STOP); // 预约状态
        // 更新预约购买产品信息记录表为暂停状态
        KupDprebypdDao.updateOne_odb1(tblKupDprebypd);
        return;
    }

    @Override
    public void afterJobProcess(String taskId, Input input, Property property, String jobId, int totalSuccessCount, int totalErrorCount) {
        super.afterJobProcess(taskId, input, property, jobId, totalSuccessCount, totalErrorCount);
        StringBuilder SnRenxdt = new StringBuilder();
        if (E_CYCLES.DAY == tblKupDprebypd.getCycles()) {
            SnRenxdt.append(tblKupDprebypd.getRecycl()).append(tblKupDprebypd.getCycles().getValue()).append("A");
        } else {
            SnRenxdt.append(tblKupDprebypd.getRecycl()).append(tblKupDprebypd.getCycles().getValue()).append("A").append("D");
        }
        String renxdt = DateTools2.calDateByFreq(tblKupDprebypd.getRenxdt(), SnRenxdt.toString());
        // 更新预约购买产品信息记录表为下次预约日期
        tblKupDprebypd.setRenxdt(renxdt);
        KupDprebypdDao.updateOne_odb1(tblKupDprebypd);
    }

}
