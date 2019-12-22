package cn.sunline.ltts.busi.dptran.batchtran;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.namedsql.CaBatchTransDao;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDelayDetl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDelayDetlDao;
import cn.sunline.ltts.busi.dp.type.DpTransfer;
import cn.sunline.ltts.busi.dp.type.DpTransfer.DpknaDelayBatch;
import cn.sunline.ltts.busi.dptran.batchtran.intf.Dptter.Input;
import cn.sunline.ltts.busi.dptran.batchtran.intf.Dptter.Property;
import cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill.SaveActoac;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill.SaveActoacPeer;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType.IoCaTsfInCheck;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStUfIn;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalFee_IN;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSTRFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DELAYT;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.lang.Params;

/**
 * 延迟转账批量定时任务
 * 
 */

public class dptterDataProcessor extends
        AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.intf.Dptter.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Dptter.Property, cn.sunline.ltts.busi.dp.type.DpTransfer.DpknaDelayBatch> {
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
    public void process(String jobId, int index, cn.sunline.ltts.busi.dp.type.DpTransfer.DpknaDelayBatch dataItem, cn.sunline.ltts.busi.dptran.batchtran.intf.Dptter.Input input,
            cn.sunline.ltts.busi.dptran.batchtran.intf.Dptter.Property property) {
        KnaDelayDetl tblKnaDelayDetl = KnaDelayDetlDao.selectOne_odb1(dataItem.getTransq(), false);
        String servtp = CommTools.getBaseRunEnvs().getChannel_id();
        // 本行转本行
        if (E_CAPITP.NT301 == dataItem.getCapitp()) {

            // 转入账户检查
            cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType.IoCaTsfInCheck.InputSetter ioCaTsfInCheckInput = SysUtil.getInstance(IoCaTsfInCheck.InputSetter.class);
            ioCaTsfInCheckInput.setAccttp(tblKnaDelayDetl.getAccttp());
            ioCaTsfInCheckInput.setCardno(dataItem.getToacct());
            ioCaTsfInCheckInput.setInacna(dataItem.getInacna());
            ioCaTsfInCheckInput.setInacno(dataItem.getAcctno());
            ioCaTsfInCheckInput.setIncard(dataItem.getCardno());
            ioCaTsfInCheckInput.setOtacna(dataItem.getOtacna());
            ioCaTsfInCheckInput.setOtcsac(dataItem.getToscac());
            cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType.IoCaTsfInCheck.Output ioCaTsfInCheckOutput = SysUtil.getInstance(IoCaTsfInCheck.Output.class);
            SysUtil.getInstance(ServEacctSvcType.class).ioCaTsfInCheck(ioCaTsfInCheckInput, ioCaTsfInCheckOutput);

            // 收方电子账户系统扣减客户额度
            InputSetter SubAcctQuotaInput = SysUtil.getInstance(IoAcSubQuota.InputSetter.class);
            Output SubAcctQuotaOuput = SysUtil.getInstance(IoAcSubQuota.Output.class);
            SubAcctQuotaInput.setAccttp(tblKnaDelayDetl.getAccttp());
            SubAcctQuotaInput.setAclmfg(tblKnaDelayDetl.getAclmfg());
            SubAcctQuotaInput.setAuthtp(tblKnaDelayDetl.getAuthtp());
            SubAcctQuotaInput.setBrchno(dataItem.getBrchno());
            SubAcctQuotaInput.setCustac(tblKnaDelayDetl.getCustac());
            SubAcctQuotaInput.setCustid(tblKnaDelayDetl.getCustid());
            SubAcctQuotaInput.setCustlv(tblKnaDelayDetl.getCustlv());
            SubAcctQuotaInput.setAcctrt(tblKnaDelayDetl.getAcctrt());
            SubAcctQuotaInput.setLimttp(tblKnaDelayDetl.getLimttp());
            SubAcctQuotaInput.setPytltp(tblKnaDelayDetl.getPytltp());
            SubAcctQuotaInput.setRebktp(tblKnaDelayDetl.getRebktp());
            SubAcctQuotaInput.setRisklv(tblKnaDelayDetl.getRisklv());
            SubAcctQuotaInput.setSbactp(tblKnaDelayDetl.getSbactp());
            SubAcctQuotaInput.setFacesg(tblKnaDelayDetl.getFacesg());
            SubAcctQuotaInput.setCustie(tblKnaDelayDetl.getCustie());
            SubAcctQuotaInput.setRecpay(tblKnaDelayDetl.getRecpay());
            SubAcctQuotaInput.setServdt(tblKnaDelayDetl.getServdt());
            SubAcctQuotaInput.setServsq(tblKnaDelayDetl.getServsq());
            SubAcctQuotaInput.setServtp(dataItem.getServtp());
            SubAcctQuotaInput.setTranam(dataItem.getTranam());
            SubAcctQuotaInput.setDcflag(tblKnaDelayDetl.getDcflag());
            SysUtil.getInstance(IoCaSevAccountLimit.class).SubAcctQuota(SubAcctQuotaInput, SubAcctQuotaOuput);
        }

        // 电子账户支取记账服务
        DrawDpAcctIn addDrawAcctDpinput = SysUtil.getInstance(DrawDpAcctIn.class);
        addDrawAcctDpinput.setAcctno(dataItem.getToacno());
        addDrawAcctDpinput.setAuacfg(tblKnaDelayDetl.getAuacfg());
        addDrawAcctDpinput.setCardno(dataItem.getToacct());
        addDrawAcctDpinput.setCrcycd(dataItem.getCrcycd());
        addDrawAcctDpinput.setCustac(dataItem.getToscac());
        addDrawAcctDpinput.setLinkno(tblKnaDelayDetl.getLinkno());
        addDrawAcctDpinput.setOpacna(dataItem.getInacna());
        addDrawAcctDpinput.setToacct(dataItem.getCardno());
        addDrawAcctDpinput.setTranam(dataItem.getTranam());
        addDrawAcctDpinput.setSmrycd(tblKnaDelayDetl.getSmryco());
        addDrawAcctDpinput.setOpbrch(dataItem.getBrchno());
        SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(addDrawAcctDpinput);

        // 本行转本行
        if (E_CAPITP.NT301 == dataItem.getCapitp()) {
            // 电子账户存入记账处理
            SaveDpAcctIn addPostAcctDpInput = SysUtil.getInstance(SaveDpAcctIn.class);
            addPostAcctDpInput.setAcctno(dataItem.getAcctno());
            addPostAcctDpInput.setCardno(dataItem.getCardno());
            addPostAcctDpInput.setCrcycd(dataItem.getCrcycd());
            addPostAcctDpInput.setCustac(dataItem.getCuacno());
            addPostAcctDpInput.setLinkno(tblKnaDelayDetl.getLinkno());
            addPostAcctDpInput.setOpacna(dataItem.getOtacna());
            addPostAcctDpInput.setToacct(dataItem.getToacct());
            addPostAcctDpInput.setTranam(dataItem.getTranam());
            addPostAcctDpInput.setOpbrch(dataItem.getTobrch());
            addPostAcctDpInput.setSmrycd(tblKnaDelayDetl.getSmryci());
            SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(addPostAcctDpInput);
        }
        // 公共收费
        if (E_YES___.YES == tblKnaDelayDetl.getIschrg()) {
            IoCgCalFee_IN CalChargeInput = SysUtil.getInstance(IoCgCalFee_IN.class);
            IoCgCalCenterReturn cal = SysUtil.getInstance(IoCgCalCenterReturn.class);
            cal.setCustno(tblKnaDelayDetl.getCustno());
            cal.setCustac(tblKnaDelayDetl.getCustad());
            cal.setTranam(dataItem.getTranam());
            cal.setAmount(tblKnaDelayDetl.getAmount());
            cal.setClcham(tblKnaDelayDetl.getClcham());
            cal.setDisrat(tblKnaDelayDetl.getDisrat());
            cal.setDircam(tblKnaDelayDetl.getDircam());
            cal.setPaidam(tblKnaDelayDetl.getPaidam());
            cal.setChnotp(tblKnaDelayDetl.getChnotp());
            cal.setChnona(tblKnaDelayDetl.getChnona());
            cal.setPronum(tblKnaDelayDetl.getPronum());
            cal.setTrinfo(tblKnaDelayDetl.getTrinfo());
            cal.setServtp(tblKnaDelayDetl.getServtp());
            cal.setDioage(tblKnaDelayDetl.getDioage());
            cal.setDiwage(tblKnaDelayDetl.getDiwage());
            cal.setDitage(tblKnaDelayDetl.getDitage());
            cal.setDifage(tblKnaDelayDetl.getDifage());
            cal.setDioamo(tblKnaDelayDetl.getDioamo());
            cal.setDiwamo(tblKnaDelayDetl.getDiwamo());
            cal.setDitamo(tblKnaDelayDetl.getDitamo());
            cal.setDifamo(tblKnaDelayDetl.getDifamo());
            cal.setScencd(tblKnaDelayDetl.getScencd());
            cal.setScends(tblKnaDelayDetl.getScends());
            Options<IoCgCalCenterReturn> calcenter = new DefaultOptions<IoCgCalCenterReturn>();
            calcenter.add(cal);
            CalChargeInput.setCalcenter(calcenter);
            CalChargeInput.setCstrfg(E_CSTRFG.TRNSFER);
            CalChargeInput.setChgflg(tblKnaDelayDetl.getChgflg());
            CalChargeInput.setChrgcy(dataItem.getCrcycd());
            CalChargeInput.setCsexfg(tblKnaDelayDetl.getCsextg());
            CalChargeInput.setCustac(dataItem.getToscac());
            CalChargeInput.setTrancy(dataItem.getCrcycd());
            SysUtil.getInstance(IoCgChrgSvcType.class).CalCharge(CalChargeInput);
        }
        // 本行转本行
        if (E_CAPITP.NT301 == dataItem.getCapitp()) {
            // 登记电子账户转电子账户登记簿
            cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill.SaveActoac.InputSetter SaveActoacInput = SysUtil.getInstance(SaveActoac.InputSetter.class);
            SaveActoacInput.setAcctno(dataItem.getToacno());
            SaveActoacInput.setBrchno(dataItem.getTobrch());
            SaveActoacInput.setBusisq(tblKnaDelayDetl.getBusisq());
            SaveActoacInput.setCardno(dataItem.getCardno());
            SaveActoacInput.setChckdt(tblKnaDelayDetl.getChckdt());
            SaveActoacInput.setCrcycd(dataItem.getCrcycd());
            SaveActoacInput.setCuacno(dataItem.getCuacno());
            SaveActoacInput.setIncorp(tblKnaDelayDetl.getIncorp());
            SaveActoacInput.setKeepdt(tblKnaDelayDetl.getKeepdt());
            SaveActoacInput.setPrcscd(tblKnaDelayDetl.getPrcscd());
            SaveActoacInput.setServdt(tblKnaDelayDetl.getServdt());
            SaveActoacInput.setServsq(tblKnaDelayDetl.getServsq());
            SaveActoacInput.setServtp(dataItem.getServtp());
            
            CommTools.getBaseRunEnvs().setChannel_id(dataItem.getServtp());
            SaveActoacInput.setTlcgam(dataItem.getTlcgam());
            SaveActoacInput.setToacct(dataItem.getToacct());
            SaveActoacInput.setToacno(dataItem.getToacno());
            SaveActoacInput.setTobrch(dataItem.getTobrch());
            SaveActoacInput.setTocorp(tblKnaDelayDetl.getOtcorp());
            SaveActoacInput.setToscac(dataItem.getToscac());
            SaveActoacInput.setTranam(dataItem.getTranam());
            SaveActoacInput.setTrandt(dataItem.getAcdate());
            SaveActoacInput.setTrantp(dataItem.getTrantp());
            SysUtil.getInstance(IoSaveIoTransBill.class).SaveActoac(SaveActoacInput);
            
        }
        // 登记电子账户转电子账户登记簿(转出)
        cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill.SaveActoacPeer.InputSetter SaveActoacPeerInput = SysUtil.getInstance(SaveActoacPeer.InputSetter.class);
        SaveActoacPeerInput.setAcctno(dataItem.getToacno());
        SaveActoacPeerInput.setBrchno(dataItem.getTobrch());
        SaveActoacPeerInput.setBusisq(tblKnaDelayDetl.getBusisq());
        SaveActoacPeerInput.setCardno(dataItem.getCardno());
        SaveActoacPeerInput.setChckdt(tblKnaDelayDetl.getChckdt());
        SaveActoacPeerInput.setCrcycd(dataItem.getCrcycd());
        SaveActoacPeerInput.setCuacno(dataItem.getCuacno());
        SaveActoacPeerInput.setIncorp(tblKnaDelayDetl.getIncorp());
        SaveActoacPeerInput.setKeepdt(tblKnaDelayDetl.getKeepdt());
        SaveActoacPeerInput.setPrcscd(tblKnaDelayDetl.getPrcscd());
        SaveActoacPeerInput.setServdt(tblKnaDelayDetl.getServdt());
        SaveActoacPeerInput.setServsq(tblKnaDelayDetl.getServsq());
        SaveActoacPeerInput.setServtp(dataItem.getServtp());
        SaveActoacPeerInput.setTlcgam(dataItem.getTlcgam());
        SaveActoacPeerInput.setToacct(dataItem.getToacct());
        SaveActoacPeerInput.setToacno(dataItem.getToacno());
        SaveActoacPeerInput.setTobrch(dataItem.getTobrch());
        SaveActoacPeerInput.setTocorp(tblKnaDelayDetl.getOtcorp());
        SaveActoacPeerInput.setToscac(dataItem.getToscac());
        SaveActoacPeerInput.setTranam(dataItem.getTranam());
        SaveActoacPeerInput.setTrandt(dataItem.getAcdate());
        SaveActoacPeerInput.setTrantp(dataItem.getTrantp());
        SysUtil.getInstance(IoSaveIoTransBill.class).SaveActoacPeer(SaveActoacPeerInput);
        // 本行转本行
        if (E_CAPITP.NT301 == dataItem.getCapitp()) {
            // 修改电子账户状态
            IoDpKnaAcct ioDpKnaAcctInput = SysUtil.getInstance(IoDpKnaAcct.class);
            KnaAcct tblKnaacct = KnaAcctDao.selectOne_odb1(tblKnaDelayDetl.getAcctno(), false);
            ioDpKnaAcctInput.setAcctcd(tblKnaacct.getAcctcd());
            ioDpKnaAcctInput.setAcctna(tblKnaacct.getAcctna());
            ioDpKnaAcctInput.setAcctno(tblKnaacct.getAcctno());
            ioDpKnaAcctInput.setAcctst(tblKnaacct.getAcctst());
            ioDpKnaAcctInput.setAccttp(tblKnaacct.getAccttp());
            ioDpKnaAcctInput.setBgindt(tblKnaacct.getBgindt());
            ioDpKnaAcctInput.setBkmony(tblKnaacct.getBkmony());
            ioDpKnaAcctInput.setBrchno(tblKnaacct.getBrchno());
            ioDpKnaAcctInput.setClosdt(tblKnaacct.getClosdt());
            ioDpKnaAcctInput.setClossq(tblKnaacct.getClossq());
            ioDpKnaAcctInput.setCorpno(tblKnaacct.getCorpno());
            ioDpKnaAcctInput.setCrcycd(tblKnaacct.getCrcycd());
            ioDpKnaAcctInput.setCsextg(tblKnaacct.getCsextg());
            ioDpKnaAcctInput.setCustac(tblKnaacct.getCustac());
            ioDpKnaAcctInput.setCustno(tblKnaacct.getCustno());
            ioDpKnaAcctInput.setDatetm(tblKnaDelayDetl.getChckdt());
            ioDpKnaAcctInput.setDebttp(tblKnaacct.getDebttp());
            ioDpKnaAcctInput.setDepttm(tblKnaacct.getDepttm());
            ioDpKnaAcctInput.setHdmimy(tblKnaacct.getHdmimy());
            ioDpKnaAcctInput.setHdmxmy(tblKnaacct.getHdmxmy());
            ioDpKnaAcctInput.setIsdrft(tblKnaacct.getIsdrft());
            ioDpKnaAcctInput.setLastbl(tblKnaacct.getLastbl());
            ioDpKnaAcctInput.setLstrdt(tblKnaacct.getLstrdt());
            ioDpKnaAcctInput.setLstrsq(tblKnaacct.getLstrsq());
            ioDpKnaAcctInput.setMatudt(tblKnaacct.getMatudt());
            ioDpKnaAcctInput.setOnlnbl(DpAcctProc.getAcctBalance(tblKnaacct));
            ioDpKnaAcctInput.setOpendt(tblKnaacct.getOpendt());
            ioDpKnaAcctInput.setOpensq(tblKnaacct.getOpensq());
            ioDpKnaAcctInput.setOpmony(tblKnaacct.getOpmony());
            ioDpKnaAcctInput.setPddpfg(tblKnaacct.getPddpfg());
            ioDpKnaAcctInput.setProdcd(tblKnaacct.getProdcd());
            ioDpKnaAcctInput.setSleptg(tblKnaacct.getSleptg());
            ioDpKnaAcctInput.setSpectp(tblKnaacct.getSpectp());
            ioDpKnaAcctInput.setTmstmp(tblKnaacct.getTmstmp());
            ioDpKnaAcctInput.setTrsvtp(tblKnaacct.getTrsvtp());
            ioDpKnaAcctInput.setUpbldt(tblKnaacct.getUpbldt());
            SysUtil.getInstance(DpAcctSvcType.class).dealAcctStatAndSett(ioDpKnaAcctInput, tblKnaDelayDetl.getCuacst());

            // 平衡性检查
            E_CLACTP clactp = null;
            if (!CommUtil.equals(dataItem.getBrchno(), dataItem.getTobrch())) {
                clactp = E_CLACTP._10;
            }
            SysUtil.getInstance(IoCheckBalance.class).checkBalance(
                    CommTools.getBaseRunEnvs().getTrxn_date(),
                    dataItem.getTransq(), clactp);
        }

        // 解冻
        IoDpStUfIn cpliodpstufin = SysUtil.getInstance(IoDpStUfIn.class);
        cpliodpstufin.setTrandt(dataItem.getTrandt());
        cpliodpstufin.setMntrsq(dataItem.getTransq());
        SysUtil.getInstance(IoDpFrozSvcType.class).IoDpStUf(cpliodpstufin);
        
        CaBatchTransDao.updKnaDelay(E_DELAYT.SUCCESS, dataItem.getTransq());
        CommTools.getBaseRunEnvs().setChannel_id(servtp);
    }

    /**
     * 获取数据遍历器。
     * 
     * @param input 批量交易输入接口
     * @param property 批量交易属性接口
     * @return 数据遍历器
     */
    @Override
    public BatchDataWalker<cn.sunline.ltts.busi.dp.type.DpTransfer.DpknaDelayBatch> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.intf.Dptter.Input input,
            cn.sunline.ltts.busi.dptran.batchtran.intf.Dptter.Property property) {
        Params para = new Params();
        para.put("acdate", CommTools.getBaseRunEnvs().getTrxn_date());
        para.put("actime", BusiTools.getBusiRunEnvs().getTrantm());
        return new CursorBatchDataWalker<DpTransfer.DpknaDelayBatch>(CaBatchTransDao.namedsql_selKnaDelayMessgst, para);
    }

    @Override
    public void jobExceptionProcess(String taskId, Input input, Property property, String jobId, DpknaDelayBatch dataItem, Throwable t) {
        // TODO Auto-generated method stub
        super.jobExceptionProcess(taskId, input, property, jobId, dataItem, t);
        CaBatchTransDao.updKnaDelay(E_DELAYT.FAILURE, dataItem.getTransq());
    }

}
