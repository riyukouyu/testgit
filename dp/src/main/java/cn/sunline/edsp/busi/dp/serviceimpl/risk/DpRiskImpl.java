package cn.sunline.edsp.busi.dp.serviceimpl.risk;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.core.exception.AdpDaoException;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.DBTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.collection.CollectionUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.iobus.servicetype.risk.DpRiskService;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskEnumType;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskEnumType.E_CBAKST;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskEnumType.E_DEAPST;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskEnumType.E_ENFLAG;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskEnumType.E_FRAPST;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskEnumType.E_FRDEST;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskEnumType.E_FREXOG;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskEnumType.E_OPDESC;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.AddDeduInfo;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.AgentFrozenInput;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.ChabakList;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.EnterChabakImp;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.FrozenApplyImport;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.ImportDeduList;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.KnbAplyDto;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.KnbAplyListInfo;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.KnbFrozDetaListInfo;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.KnbFrozOutput;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.KnlIoblCupsEdmContro;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.RiskDeduDetail;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.RiskDeduListOutput;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.RiskKnaAcct;
import cn.sunline.edsp.busi.dp.namedsql.DpRiskDao;
import cn.sunline.edsp.busi.dp.namedsql.ca.AccountFlowDao;
import cn.sunline.edsp.busi.dp.tables.risk.DpRisk.KnbAply;
import cn.sunline.edsp.busi.dp.tables.risk.DpRisk.KnbAplyDao;
import cn.sunline.edsp.busi.dp.tables.risk.DpRisk.KnbChabck;
import cn.sunline.edsp.busi.dp.tables.risk.DpRisk.KnbChabckDao;
import cn.sunline.edsp.busi.dp.tables.risk.DpRisk.KnbFrozDeta;
import cn.sunline.edsp.busi.dp.tables.risk.DpRisk.KnbThaw;
import cn.sunline.edsp.busi.dp.tables.risk.DpRisk.KnbThawDao;
import cn.sunline.edsp.busi.dp.type.jfbase.JFBaseEnumType.E_STACTP;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaadDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.froz.DpFrozProc;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwne;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwneDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

/**
 * 风控冻结强扣服务实现
 *
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "DpRiskImpl", longname = "风控冻结强扣服务实现",
    type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class DpRiskImpl implements cn.sunline.edsp.busi.dp.iobus.servicetype.risk.DpRiskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DpRiskImpl.class);
    private static final String CNY = "CNY";
    private static final String SCQK = "SCQK";

    /**
     * 查询风控冻结列表
     *
     */
    @Override
    public Options<cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.KnbAplyListInfo> selectRiskFrozenList(
        final cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.SelRiskFrozenListInput selRiskFrozenInput) {
        Long pageNo = CommTools.getBaseRunEnvs().getPage_start();
        Long pageSize = CommTools.getBaseRunEnvs().getPage_size();
        selRiskFrozenInput.setFroztp(E_FROZTP.FKFROZ);
        Page<KnbAplyListInfo> page = DpRiskDao.selectKnbAplyPageList(selRiskFrozenInput, (pageNo - 1) * pageSize,
            pageSize, CommTools.getBaseRunEnvs().getTotal_count(), false);
        for (KnbAplyListInfo info : page.getRecords()) {
            info.setFrozal(info.getFrozam().subtract(info.getFrozbl()));
            // 录入时间
            if (CommUtil.isNotNull(info.getEntime())) {
                info.setEntime(DpPublic.parseTimestamp(info.getEntime()));
            }
            if (CommUtil.isNotNull(info.getFrtime())) {
                info.setFrtime(DpPublic.parseTimestamp(info.getFrtime()));
            }
        }
        CommTools.getBaseRunEnvs().setTotal_count(page.getRecordCount());
        return new DefaultOptions<>(page.getRecords());
    }

    /**
     * 批量冻结
     * 
     * @return
     *
     */
    @Override
    public String batchFroz(final Options<cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.KnbAplyDto> knbAply) {
        int successNum = 0;
        for (KnbAplyDto aply : knbAply) {
            try {
                if (frozeAply(aply.getAplyno())) {
                    DBTools.commit();
                    successNum++;
                } else {
                    DBTools.rollback();
                }
            } catch (Exception e) {
                LOGGER.error("批量冻结异常，申请单号：{}，异常：{}", aply.getAplyno(), e);
                DBTools.rollback();
            }
        }
        String successMsg = "操作成功，共成功" + successNum + "条数据";
        return successMsg;
    }

    private boolean frozeAply(String aplyno) {
        KnbAply knbAply = KnbAplyDao.selectOne_odb1(aplyno, false);
        if (CommUtil.isNull(knbAply) || knbAply.getFrapst() != E_FRAPST.TO_FROZE) {
            return false;
        }
        Map<String, BigDecimal> owneMap = new HashMap<>();
        // 查找调单申请的交易流水，是否已代发和冻结
        KnlIoblCups aplyKnlIoblCups = AccountFlowDao.selectKnlIoblCupsByRefeno(knbAply.getRefeno(), false);
        // 交易流水子账户
        RiskKnaAcct accsDo = DpRiskDao.selOnlnblByAcctno(aplyKnlIoblCups.getAcctno(), true);
        // 子账户状态正常且有可用余额
        if (CommUtil.isNotNull(accsDo) && accsDo.getOnlnbl().compareTo(accsDo.getFrozbl()) > 0) {
            // 调单申请流水子账户冻结
            frozeAplyAcct(knbAply, accsDo, aplyKnlIoblCups, owneMap);
        }
        // 同品牌子账户流水冻结
        if (!isAplyFrozen(knbAply)) {
            List<RiskKnaAcct> accts =
                DpRiskDao.selFirstAcctByCustac(knbAply.getAplyid(), knbAply.getSbrand(), null, false);
            if (CommUtil.isNotNull(accsDo)) {
                Iterator<RiskKnaAcct> iterator = accts.iterator();
                while (iterator.hasNext()) {
                    RiskKnaAcct riskKnaAcct = iterator.next();
                    if (accsDo.getAcctno().equals(riskKnaAcct.getAcctno())) {
                        iterator.remove();
                    }
                }
            }
            for (RiskKnaAcct acct : accts) {
                if (isAplyFrozen(knbAply)) {
                    // 冻结申请单冻结完成
                    break;
                }
                // 有可用余额
                if (acct.getOnlnbl().compareTo(acct.getFrozbl()) > 0) {
                    frozeAcct(knbAply, acct, owneMap);
                }
            }
        }
        // 其余子账户流水总结
        if (!isAplyFrozen(knbAply)) {
            List<RiskKnaAcct> otherAccts =
                DpRiskDao.selAccsByCustac(knbAply.getAplyid(), knbAply.getSbrand(), null, false);
            for (RiskKnaAcct otherAcct : otherAccts) {
                if (isAplyFrozen(knbAply)) {
                    // 冻结申请单冻结完成
                    break;
                }
                // 有可用余额
                if (otherAcct.getOnlnbl().compareTo(otherAcct.getFrozbl()) > 0) {
                    frozeAcct(knbAply, otherAcct, owneMap);
                }
            }
        }
        if (knbAply.getFrapst() == E_FRAPST.TO_FROZE) {
            knbAply.setFrapst(E_FRAPST.PARTIAL_FROZEN);
        }
        knbAply.setOperator(CommTools.getBaseRunEnvs().getTrxn_teller());
        knbAply.setOptime(CommTools.getBaseRunEnvs().getTimestamp());
        knbAply.setFrbgdt(CommTools.getBaseRunEnvs().getComputer_date());
        knbAply.setFrtime(CommTools.getBaseRunEnvs().getTimestamp());
        knbAply.setTmstmp(CommTools.getBaseRunEnvs().getTimestamp());
        KnbAplyDao.updateOne_odb1(knbAply);
        DpFrozProc.insertKnbRecord(knbAply.getAplyno(), DpRiskEnumType.E_OPDESC.FROZE);
        for (Entry<String, BigDecimal> owneEntry : owneMap.entrySet()) {
            KnbFrozOwne owne = KnbFrozOwneDao.selectOneWithLock_odb1(E_FROZOW.AUACCT, owneEntry.getKey(), false);
            if (owne == null) {
                owne = DpFrozProc.setOwneInfo();
                owne.setFrowid(owneEntry.getKey());
                owne.setFrozbl(owneEntry.getValue());
                KnbFrozOwneDao.insert(owne);
            } else {
                owne.setFrozbl(owne.getFrozbl().add(owneEntry.getValue()));
                KnbFrozOwneDao.updateOne_odb1(owne);
            }
        }
        return true;
    }

    private boolean isAplyFrozen(KnbAply knbAply) {
        return knbAply.getFrapst() == E_FRAPST.FROZEN;
    }

    private void frozeAcct(KnbAply knbAply, RiskKnaAcct acct, Map<String, BigDecimal> owneMap) {
        // 查询交易流水
        List<KnlIoblCupsEdmContro> riskKnlIoblCups = AccountFlowDao.selectRiskKnlIoblCups(acct.getAcctno(), false);
        for (KnlIoblCupsEdmContro cups : riskKnlIoblCups) {
            if (isAplyFrozen(knbAply)) {
                // 冻结申请单冻结完成
                break;
            }
            frozeAcctKnlIoblCups(knbAply, cups, owneMap, acct);
        }
    }

    /**
     * 
     * @Title: frozeAplyAcct
     * @Description: 冻结调单交易电子账户
     * @author xuxiaoli
     * @param knbAply
     *            冻结申请单
     * @param accsDo
     *            交易电子账户
     * @param aplyKnlIoblCups
     *            调单交易流水
     * @param owneMap
     *            冻结主体表
     */
    private void frozeAplyAcct(KnbAply knbAply, RiskKnaAcct accsDo, KnlIoblCups aplyKnlIoblCups,
        Map<String, BigDecimal> owneMap) {
        // 查询交易流水
        List<KnlIoblCupsEdmContro> riskKnlIoblCups = AccountFlowDao.selectRiskKnlIoblCups(accsDo.getAcctno(), false);
        Iterator<KnlIoblCupsEdmContro> iterator = riskKnlIoblCups.iterator();
        while (iterator.hasNext()) {
            KnlIoblCupsEdmContro itCups = iterator.next();
            // 优先冻结调单交易流水
            if (aplyKnlIoblCups.getRefeno().equals(itCups.getRefeno())) {
                frozeAcctKnlIoblCups(knbAply, itCups, owneMap, accsDo);
                iterator.remove();
                break;
            }
        }
        for (KnlIoblCupsEdmContro cups : riskKnlIoblCups) {
            if (isAplyFrozen(knbAply)) {
                // 冻结申请单冻结完成
                break;
            }
            frozeAcctKnlIoblCups(knbAply, cups, owneMap, accsDo);
        }
    }

    /**
     * 
     * @Title: frozeAcctKnlIoblCups
     * @Description: 冻结交易流水
     * @author xuxiaoli
     * @param knbAply
     *            冻结申请单
     * @param knlIoblCups
     *            交易流水
     * @param owneMap
     *            冻结主体表
     * @param riskKnaAcct
     *            电子账户
     */
    private void frozeAcctKnlIoblCups(KnbAply knbAply, KnlIoblCupsEdmContro knlIoblCups,
        Map<String, BigDecimal> owneMap, RiskKnaAcct riskKnaAcct) {
        BigDecimal tranFrozenAmount = knlIoblCups.getFrozam() == null ? BigDecimal.ZERO : knlIoblCups.getFrozam();// 交易流水已冻结金额
        BigDecimal reToanam = knlIoblCups.getToanam().subtract(tranFrozenAmount);
        BigDecimal acctUseBal = riskKnaAcct.getOnlnbl().subtract(riskKnaAcct.getFrozbl())
            .subtract(owneMap.getOrDefault(riskKnaAcct.getAcctno(), BigDecimal.ZERO));
        // 冻结金额
        BigDecimal frozenAmount = reToanam;
        if (acctUseBal.compareTo(reToanam) < 0) {
            frozenAmount = acctUseBal;
        }
        if (knbAply.getFrozbl().compareTo(frozenAmount) < 0) {
            frozenAmount = knbAply.getFrozbl();
        }
        if (frozenAmount.compareTo(BigDecimal.ZERO) > 0) {
            DpFrozProc.insertDetalInfo(knbAply, frozenAmount, knlIoblCups);
            // 更新主体表金额
            owneMap.put(knlIoblCups.getAcctno(),
                owneMap.getOrDefault(knlIoblCups.getAcctno(), BigDecimal.ZERO).add(frozenAmount));
            // 更新申请表
            knbAply.setFrozbl(knbAply.getFrozbl().subtract(frozenAmount));
            if (knbAply.getFrozbl().compareTo(BigDecimal.ZERO) == 0) {
                knbAply.setFrapst(E_FRAPST.FROZEN);
            } else {
                knbAply.setFrapst(E_FRAPST.PARTIAL_FROZEN);
            }

            // 更新交易流水冻结金额和冻结状态
            knlIoblCups.setFrozam(tranFrozenAmount.add(frozenAmount));
            knlIoblCups.setOldfrozam(tranFrozenAmount);
            knlIoblCups.setFrozfg(E_YES___.YES);
            int updated = DpRiskDao.updateKnlIoblCupsForFrozen(knlIoblCups);
            if (updated == 1) {
                if (CommUtil.isNotNull(knlIoblCups.getOldrecdver())) {
                    knlIoblCups.setRecdver(knlIoblCups.getOldrecdver() + 1);
                    int edmUpdated = DpRiskDao.updateEdmControRecdver(knlIoblCups);
                    if (edmUpdated != 1) {
                        throw new AdpDaoException("冻结交易异常：" + knlIoblCups.getMntrsq());
                    }
                }
            } else {
                throw new AdpDaoException("冻结交易异常：" + knlIoblCups.getMntrsq());
            }
        }
    }

    /**
     * 批量解冻
     *
     */
    public String batchThaw(final cn.sunline.edsp.busi.dp.iobus.servicetype.risk.DpRiskService.BatchThaw.Input input) {
        List<KnbAplyDto> aplylist1 = input.getKnbAply();
        StringBuffer applyno = new StringBuffer("");
        for (int i = 0; i < aplylist1.size(); i++) {
            KnbAplyDto params = aplylist1.get(i);
            if (CommUtil.isNotNull(params)) {
                applyno.append(params.getAplyno());
                applyno.append(",");
            }
        }
        Integer sucessNm = 0;
        String applynoNew = applyno.toString();
        applynoNew = applynoNew.substring(0, applyno.length() - 1);

        String unfrozdt = CommTools.getBaseRunEnvs().getComputer_date();
        String unfroztm = CommTools.getBaseRunEnvs().getComputer_time();

        String status = StringUtils.joinWith(",", DpRiskEnumType.E_FRAPST.FROZEN.getValue(),
            DpRiskEnumType.E_FRAPST.PARTIAL_FROZEN.getValue());
        List<KnbAplyDto> aplylist =
            DpRiskDao.selectFrozenAplyInfos(applynoNew, status, DpEnumType.E_FROZTP.FKFROZ, null, null, false);

        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMinimumIntegerDigits(4);
        formatter.setGroupingUsed(false);
        long i = 1L;
        Map<String, BigDecimal> owneMap = new HashMap<>();
        for (KnbAplyDto ent : aplylist) {
            // 插入解冻申请表
            KnbThaw enThaw = SysUtil.getInstance(KnbThaw.class);
            enThaw = DpFrozProc.setKnbThawInfo(ent, CommTools.getBaseRunEnvs().getTrxn_seq() + formatter.format(i++));
            enThaw.setUnfram(ent.getFrozam().subtract(ent.getFrozbl()));// 解冻金额
            KnbThawDao.insert(enThaw);
            // 查询每个申请编号下的明细
            List<KnbFrozDeta> frozDetallist = DpRiskDao.selectDetails(ent.getAplyno(),
                DpRiskEnumType.E_FRDEST.FROZEN.getValue(), null, null, null, true);
            for (KnbFrozDeta entity : frozDetallist) {
                unfrozdt = CommTools.getBaseRunEnvs().getComputer_date();
                unfroztm = CommTools.getBaseRunEnvs().getComputer_time();
                String acctno = entity.getFrowid();
                entity.setFrozst(DpRiskEnumType.E_FRDEST.UNFROZEN);
                entity.setThawdt(unfrozdt);
                entity.setThawtm(unfroztm);
                entity.setThawam(entity.getFrozam());
                // 更新明细表状态，解冻日期等
                DpRiskDao.updateFrozenDetail(entity);
                // 更新交易明细
                KnlIoblCupsEdmContro knlIoblCupsEdmContro =
                    AccountFlowDao.selectRiskKnlIoblCupsByMntrsq(entity.getTdtrsq(), entity.getTdtrdt(), false);
                knlIoblCupsEdmContro.setOldfrozam(knlIoblCupsEdmContro.getFrozam());
                knlIoblCupsEdmContro.setFrozam(knlIoblCupsEdmContro.getFrozam().subtract(entity.getFrozam()));
                if (knlIoblCupsEdmContro.getFrozam().compareTo(BigDecimal.ZERO) == 0) {
                    knlIoblCupsEdmContro.setFrozfg(E_YES___.NO);
                }
                int updated = DpRiskDao.updateKnlIoblCupsForFrozen(knlIoblCupsEdmContro);
                if (updated != 1) {
                    throw new AdpDaoException("解冻交易异常：" + knlIoblCupsEdmContro.getMntrsq());
                }
                owneMap.put(acctno, owneMap.computeIfAbsent(acctno, k -> BigDecimal.ZERO).add(entity.getFrozam()));
            }
            KnbAplyDto ent1 = SysUtil.getInstance(KnbAplyDto.class);
            ent1.setAplyno(ent.getAplyno());
            ent1.setFrapst(DpRiskEnumType.E_FRAPST.UNFROZEN);
            ent1.setUnfrdt(unfrozdt);
            ent1.setOperator(CommTools.getBaseRunEnvs().getTrxn_teller());
            ent1.setOptime(CommTools.getBaseRunEnvs().getTimestamp());
            ent1.setTmstmp(CommTools.getBaseRunEnvs().getTimestamp());
            // 更新申请表状态，解冻日期等
            DpRiskDao.updateKnbAply(ent1);
            DpFrozProc.insertKnbRecord(ent.getAplyno(), DpRiskEnumType.E_OPDESC.UNFROZE);
            sucessNm++;
        }
        for (Entry<String, BigDecimal> owneEntry : owneMap.entrySet()) {
            KnbFrozOwne owne = KnbFrozOwneDao.selectOneWithLock_odb1(E_FROZOW.AUACCT, owneEntry.getKey(), false);
            owne.setFrozbl(owne.getFrozbl().subtract(owneEntry.getValue()));
            KnbFrozOwneDao.updateOne_odb1(owne);
        }
        String successMsg = "操作成功，共成功" + sucessNm + "条数据";
        return successMsg;
    }

    /**
     * 收单冻结
     */
    @Override
    public KnbFrozOutput transFroz(final cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.toTransFroz transFroz) {
        KnbFrozOutput output = SysUtil.getInstance(KnbFrozOutput.class);
        String custac = transFroz.getCustac();// 主账户号
        String acctno = transFroz.getAcctno();// 子账户号
        String tdtrdt = transFroz.getTdtrdt();// 收单日期
        String tdtrsq = transFroz.getTdtrsq();// 收单流水
        		// 新增商户编号、参考号
		String mrchno=transFroz.getInmeid();
		String refeno=transFroz.getRefeno();
		String sbrand=transFroz.getSbrand();
		String cardno=transFroz.getCardno();
		BigDecimal tranam = transFroz.getTranam();// 收单金额
		BigDecimal tranbl = transFroz.getTranam();// 收单冻结金额
		BigDecimal refrozbl = tranam;// 可用金额

        List<KnbAplyDto> list = DpRiskDao.selRecordByCustacOrAcctno(cardno, false); // 查找申请登记表信息
        if (CollectionUtil.isEmpty(list)) {
            output.setIsFrozen(E_YES___.NO);
            output.setFrozam(BigDecimal.ZERO);
            return output;
        }
        for (KnbAplyDto ent : list) {
            // 可用金额为0，不处理剩下的冻结申请
            if (refrozbl.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            if (ent.getFrozbl().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            // 冻结申请单未冻结完
            BigDecimal frozam = BigDecimal.ZERO; // 冻结的金额
            if (ent.getFrozbl().compareTo(refrozbl) > 0) {// 需要冻结金额>可操作余额
                frozam = refrozbl;
            } else {// 需要冻结金额<=可操作余额
                frozam = ent.getFrozbl();
            }
            // 插入明细
            DpFrozProc.insertDetalInfo(ent, frozam, acctno, tdtrsq, tdtrdt,mrchno,refeno,sbrand,E_YES___.NO);
            // 更新申请表
            ent.setOperator(CommTools.getBaseRunEnvs().getTrxn_teller());
            ent.setOptime(CommTools.getBaseRunEnvs().getTimestamp());
            DpRiskDao.updateKnbAply(ent);
            // 可用金额更新
            refrozbl = refrozbl.subtract(frozam);
            DpFrozProc.insertKnbRecord(ent.getAplyno(), DpRiskEnumType.E_OPDESC.FROZE);
        }
        if (refrozbl.compareTo(tranam) < 0) {

            BigDecimal frozenAmt = tranam.subtract(refrozbl);
            // 更新主体表
            KnbFrozOwne owne = KnbFrozOwneDao.selectOneWithLock_odb1(E_FROZOW.AUACCT, acctno, false);
            if (owne == null) {
                owne = DpFrozProc.setOwneInfo();
                owne.setFrowid(acctno);
                owne.setFrozbl(frozenAmt);
                KnbFrozOwneDao.insert(owne);
            } else {
                owne.setFrozbl(owne.getFrozbl().add(frozenAmt));
                KnbFrozOwneDao.updateOne_odb1(owne);
            }
            output.setIsFrozen(E_YES___.YES);
            output.setFrozam(frozenAmt);
        } else {
            output.setIsFrozen(E_YES___.NO);
            output.setFrozam(BigDecimal.ZERO);
        }
        return output;
    }

    /**
     * 查询风控冻结明细
     * 
     */
    @Override
    public Options<cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.KnbFrozDetaListInfo>
        selectFrozenDetailList(String aplyno) {
        KnbAply knbAply = KnbAplyDao.selectOne_odb1(aplyno, false);
        if (CommUtil.isNull(knbAply)) {
            return new DefaultOptions<>();
        }
        Long pageNo = CommTools.getBaseRunEnvs().getPage_start();
        Long pageSize = CommTools.getBaseRunEnvs().getPage_size();
        Page<KnbFrozDetaListInfo> details = DpRiskDao.selFrozDetaByAplyno(aplyno, (pageNo - 1) * pageSize, pageSize,
            CommTools.getBaseRunEnvs().getTotal_count(), false);
        for (KnbFrozDetaListInfo detail : details.getRecords()) {
            if (CommUtil.isNotNull(detail.getFroztm())) {
                detail.setFroztm(DpPublic.parseTime(detail.getFroztm()));
            }
        }
        CommTools.getBaseRunEnvs().setTotal_count(details.getRecordCount());
        return new DefaultOptions<>(details.getRecords());
    }

    /**
     * 录入冻结申请
     * 
     */
    @Override
    public void addFrozenApplies(
        final Options<cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.FrozenApplyImport> importList) {
        if (CollectionUtil.isEmpty(importList)) {
            throw DpModuleError.DpTrans.TS020042();
        }
        Options<KnbAplyDto> aply = SysUtil.getInstance(Options.class);
        Set<String> refenos = new HashSet<>();
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMinimumIntegerDigits(4);
        formatter.setGroupingUsed(false);
        long i = 1L;
        for (FrozenApplyImport applyImport : importList) {
            if (CommUtil.isNull(applyImport.getRefeno()) || CommUtil.isNull(applyImport.getRetrdt())
                || CommUtil.isNull(applyImport.getFrozam())) {
                throw DpModuleError.DpTrans.TS020043();
            }
            if (!refenos.add(applyImport.getRefeno())) {
                throw DpModuleError.DpTrans.TS020045();
            }
            // 查交易流水
            KnlIoblCups knlIoblCups = AccountFlowDao.selectKnlIoblCupsByRefeno(applyImport.getRefeno(), false);
            if (CommUtil.isNull(knlIoblCups)
                || CommUtil.compare(knlIoblCups.getPrepdt(), applyImport.getRetrdt()) != 0) {
                throw DpModuleError.DpTrans.TS020020();
            }
            // 校验是否交易已有冻结申请
            KnbAply existing = KnbAplyDao.selectOne_odb2(applyImport.getFroztp(), applyImport.getRefeno(), false);
            if (CommUtil.isNotNull(existing)) {
                throw DpModuleError.DpTrans.TS020044();
            }
            KnaMaad knaMaad = KnaMaadDao.selectOne_odb5(knlIoblCups.getCardno(), false);
            // 申请登记
            KnbAply apply = SysUtil.getInstance(KnbAply.class);
            apply.setAplyno(CommTools.getBaseRunEnvs().getTrxn_seq() + formatter.format(i));
            apply.setAplyid(knlIoblCups.getCardno());
            apply.setFrozam(applyImport.getFrozam());
            apply.setFrozbl(applyImport.getFrozam());
            // apply.setFroztp(E_FROZTP.FKFROZ);
            apply.setFroztp(applyImport.getFroztp());
            apply.setFrozow(E_STACTP.STMA);
            apply.setFrexog(E_FREXOG.FK);
            apply.setEnuser(CommTools.getBaseRunEnvs().getTrxn_teller());
            apply.setEntime(CommTools.getBaseRunEnvs().getTimestamp());
            apply.setOperator(CommTools.getBaseRunEnvs().getTrxn_teller());
            apply.setOptime(CommTools.getBaseRunEnvs().getTimestamp());
            apply.setRetrsq(knlIoblCups.getPrepsq());
            apply.setRetrdt(knlIoblCups.getPrepdt());
            apply.setRefeno(knlIoblCups.getRefeno());
            apply.setLttscd(CommTools.getBaseRunEnvs().getTrxn_code());
            apply.setMntrsq(CommTools.getBaseRunEnvs().getTrxn_seq());
            apply.setFrapst(E_FRAPST.TO_FROZE);
            apply.setSbrand(knlIoblCups.getOrgaid());
            apply.setBradna(knlIoblCups.getSbrand());
            apply.setMobile(knlIoblCups.getTeleno());
            apply.setMrchno(knlIoblCups.getInmeid());
            apply.setMrchna(knlIoblCups.getInmena());
            apply.setIdtfno(knaMaad.getIdtfno());
            apply.setAgntid(knlIoblCups.getAgid01());
            apply.setAgntna(knlIoblCups.getAd01na());
            apply.setEnflag(E_ENFLAG.BATCH);
            apply.setTrpodt(CommTools.getBaseRunEnvs().getComputer_date());
            apply.setTmidtfno(knaMaad.getTmidtfno());
            apply.setTmmobile(knlIoblCups.getTmteleno());
            KnbAplyDao.insert(apply);
            KnbAplyDto applydo = SysUtil.getInstance(KnbAplyDto.class);
            CommUtil.copyProperties(applydo, apply);
            aply.add(applydo);
            // 记录操作记录
            DpFrozProc.insertKnbRecord(apply.getAplyno(), E_OPDESC.ENTER);
            refenos.add(applyImport.getRefeno());
            i++;
        }
        if (importList.get(0).getFroztp().equals(E_FROZTP.CORRECTED)) {// CORRECTED
            DpRiskService dpRisk = SysUtil.getInstance(DpRiskService.class);
            dpRisk.batchFroz(aply);
        }
    }

    @Override
    public E_YES___ transThaw(final cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.toTransThaw transThaw) {
        BigDecimal backMoney = new BigDecimal(0);
        BigDecimal cupsFMoney = new BigDecimal(0);
        E_FRDEST frdest = E_FRDEST.CORRECTED;
        if (transThaw.getFroztp() == E_FROZTP.CORRECTED) {
            frdest = E_FRDEST.BACK;
        }
        // 收单流水查找明细表流水->明细表信息 ，对应主体表冻结金额，对应申请表冻结金额和剩余冻结金额
        List<KnbFrozDeta> details = DpRiskDao.selectDetails(null, E_FRDEST.FROZEN.getValue(), E_YES___.NO.getValue(),
            transThaw.getTdtrsq(), transThaw.getTdtrdt(), false);
        if (CollectionUtil.isEmpty(details)) {
            return E_YES___.NO;
        }
        try {
            Map<String, BigDecimal> aplyMap = new HashMap<>();
            for (KnbFrozDeta detail : details) {
                // 更新明细表状态
                detail.setFrozst(frdest);// 已冲正
                detail.setThawam(detail.getFrozam());
                detail.setThawdt(CommTools.getBaseRunEnvs().getComputer_date());
                detail.setThawtm(CommTools.getBaseRunEnvs().getComputer_time());
                int updated = DpRiskDao.updateFrozenDetail(detail);
                if (updated != 1) {
                    throw new AdpDaoException("冲正解冻明细异常：" + detail.getFrozsq());
                }
                // 申请表解冻金额
                aplyMap.put(detail.getAplyno(),
                    aplyMap.getOrDefault(detail.getAplyno(), BigDecimal.ZERO).add(detail.getFrozam()));
                // 累计冲正金额
                backMoney = backMoney.add(detail.getFrozam());
            }
            // 更新申请表 ——剩余冻结金额，冻结状态
            for (Entry<String, BigDecimal> entry : aplyMap.entrySet()) {
                KnbAply knbAply = KnbAplyDao.selectOneWithLock_odb1(entry.getKey(), true);
                knbAply.setFrozbl(knbAply.getFragbl().add(entry.getValue()));
                knbAply.setFrapst(E_FRAPST.PARTIAL_FROZEN);
                KnbAplyDao.updateOne_odb1(knbAply);
                if (DpRiskEnumType.E_FRDEST.CORRECTED == frdest) {
                    DpFrozProc.insertKnbRecord(knbAply.getAplyno(), DpRiskEnumType.E_OPDESC.CORRECTED);
                } else if (DpRiskEnumType.E_FRDEST.BACK == frdest) {
                    DpFrozProc.insertKnbRecord(knbAply.getAplyno(), DpRiskEnumType.E_OPDESC.BACK);
                }
            }
            // 更新主体表
            KnbFrozOwne owne = KnbFrozOwneDao.selectOneWithLock_odb1(E_FROZOW.AUACCT, transThaw.getAcctno(), false);
            owne.setFrozbl(owne.getFrozbl().subtract(backMoney));
            KnbFrozOwneDao.updateOne_odb1(owne);

            // 判断收单流水状态，更新收单流水
            KnlIoblCupsEdmContro knlIoblCupsEdmContro =
                AccountFlowDao.selectRiskKnlIoblCupsByMntrsq(transThaw.getTdtrsq(), transThaw.getTdtrdt(), false);
            knlIoblCupsEdmContro.setOldfrozam(knlIoblCupsEdmContro.getFrozam());
            knlIoblCupsEdmContro.setFrozam(knlIoblCupsEdmContro.getFrozam().subtract(backMoney));
            if (knlIoblCupsEdmContro.getFrozam().compareTo(BigDecimal.ZERO) == 0) {
                knlIoblCupsEdmContro.setFrozfg(E_YES___.NO);
            } else {
                knlIoblCupsEdmContro.setFrozfg(E_YES___.YES);
            }
            int updated = DpRiskDao.updateKnlIoblCupsForFrozen(knlIoblCupsEdmContro);
            if (updated != 1) {
                throw new AdpDaoException("冲正/退货解冻交易异常：" + knlIoblCupsEdmContro.getMntrsq());
            }
            return E_YES___.YES;
        } catch (Exception e) {
            return E_YES___.NO;
        }

    }

    @Override
    public void selRiskDeductList(
        final cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.RiskDeduListInput riskDeduListInput,
        final cn.sunline.edsp.busi.dp.iobus.servicetype.risk.DpRiskService.SelRiskDeductList.Output output) {
        long pageno = CommTools.getBaseRunEnvs().getPage_start();
        long pagesize = CommTools.getBaseRunEnvs().getPage_size();
        riskDeduListInput.setFroztp(E_FROZTP.QKFROZ);
        Page<RiskDeduListOutput> page = DpRiskDao.selRiskDeducList(riskDeduListInput, (pageno - 1) * pagesize, pagesize,
            CommTools.getBaseRunEnvs().getTotal_count(), false);
        for (RiskDeduListOutput info : page.getRecords()) {
            info.setFrozal(info.getFrozam().subtract(info.getFrozbl()));
            // 录入时间
            if (CommUtil.isNotNull(info.getEntime())) {
                info.setEntime(DpPublic.parseTimestamp(info.getEntime()));
            }
            if (CommUtil.isNotNull(info.getDeentm())) {
                info.setDeentm(DpPublic.parseTimestamp(info.getDeentm()));
            }
            if (CommUtil.isNotNull(info.getDetime())) {
                info.setDetime(DpPublic.parseTimestamp(info.getDetime()));
            }
        }
        CommTools.getBaseRunEnvs().setTotal_count(page.getRecordCount());
        output.getDeduList().addAll(page.getRecords());
    }

    @Override
    public void selRiskDeductDetailList(String aplyno,
        final cn.sunline.edsp.busi.dp.iobus.servicetype.risk.DpRiskService.SelRiskDeductDetailList.Output output) {
        long pageno = CommTools.getBaseRunEnvs().getPage_start();
        long pagesize = CommTools.getBaseRunEnvs().getPage_size();
        Options<RiskDeduDetail> result = new DefaultOptions<>();
        KnbAply knbAply = KnbAplyDao.selectOne_odb1(aplyno, false);
        if (CommUtil.isNull(knbAply)) {
            return;
        }
        Page<RiskDeduDetail> details = DpRiskDao.selDeduDetaByAplyno(aplyno, (pageno - 1) * pagesize, pagesize,
            CommTools.getBaseRunEnvs().getTotal_count(), false);
        
        for (RiskDeduDetail info : details.getRecords()) {                
            if (CommUtil.isNotNull(info.getForctm())) {
                info.setForctm(DpPublic.parseTime(info.getForctm()));
            }
            if (CommUtil.isNotNull(info.getThawtm())) {
                info.setThawtm(DpPublic.parseTime(info.getThawtm()));
            }
        }
        CommTools.getBaseRunEnvs().setTotal_count(details.getRecordCount());
        output.getDeduDetalst().addAll(details.getRecords());
    }

    /**
     * @author shenluyun
     * @Title DpRiskService
     * @Description TODO
     * @params selTransToEntry
     * @date 20192019年11月25日
     * @throw
     * 
     */
    @Override
    public void selTransToEntry( final cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.QryChabak qrychabak,
        final cn.sunline.edsp.busi.dp.iobus.servicetype.risk.DpRiskService.SelTransToEntry.Output output) {
        long pageno = CommTools.getBaseRunEnvs().getPage_start();
        long pagesize = CommTools.getBaseRunEnvs().getPage_size();
        Page<ChabakList> details = DpRiskDao.selChargebackList(qrychabak, (pageno - 1) * pagesize,
            pagesize, CommTools.getBaseRunEnvs().getTotal_count(), false);
        if (CommUtil.isNull(details)) {
            return;
        }
        for (ChabakList en:details.getRecords()) {
        	BigDecimal slysam=en.getSlysam()==null?BigDecimal.ZERO:en.getSlysam();
        	BigDecimal fd0028=en.getFd0028()==null?BigDecimal.ZERO:en.getFd0028();
        	BigDecimal yfuams=en.getYfuams()==null?BigDecimal.ZERO:en.getYfuams();
        	BigDecimal yshams=en.getYshams()==null?BigDecimal.ZERO:en.getYshams();             
            BigDecimal bakfee = new BigDecimal("0.00");
            bakfee = bakfee.add(slysam);
            bakfee = bakfee.add(fd0028);
            bakfee = bakfee.add(yfuams);
            bakfee = bakfee.subtract(yshams);
            en.setDeduam(en.getFd0004().subtract(bakfee));
            if (en.getFrozam() != null && en.getFrozbl() != null) {
                en.setFrozal(en.getFrozam().subtract(en.getFrozbl()));
            } else {
                en.setFrozal(BigDecimal.ZERO.setScale(2));
            }
        }
        CommTools.getBaseRunEnvs().setTotal_count(details.getRecordCount());
        output.getQryTransToEntryList().addAll(details.getRecords());
    }

    @Override
    public void
        addTransDeduct(final Options<cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.AddDeduInfo> deduAdd) {

        if (CommUtil.isNull(deduAdd)) {
            throw DpModuleError.DpRisk.RK020008();
        }

        Set<String> refenos = new HashSet<>();
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMinimumIntegerDigits(4);
        formatter.setGroupingUsed(false);
        int j=1;
        for (AddDeduInfo deduAddDo : deduAdd) {
            if (CommUtil.isNull(deduAddDo.getFd0037()) || CommUtil.isNull(deduAddDo.getRetrdt())
                || CommUtil.isNull(deduAddDo.getDeduam())) {
                throw DpModuleError.DpRisk.RK020005(String.valueOf(j));
            }
            if (!refenos.add(deduAddDo.getFd0037())) {
                throw DpModuleError.DpRisk.RK020006(String.valueOf(j));
            }
            if ( CommUtil.isNull(deduAddDo.getAgtperc())){
            	deduAddDo.setAgtperc(BigDecimal.ZERO);
            }
            BigDecimal div= new BigDecimal("100");
            deduAddDo.setAgtperc(deduAddDo.getAgtperc().divide(div));
            ChabakList chabak=DpRiskDao.selChabakListByRefeno(deduAddDo.getFd0037(), false);
            chabak.setSlysam(chabak.getSlysam()==null?BigDecimal.ZERO:chabak.getSlysam());
            chabak.setFd0028(chabak.getFd0028()==null?BigDecimal.ZERO:chabak.getFd0028());
            chabak.setYfuams(chabak.getYfuams()==null?BigDecimal.ZERO:chabak.getYfuams());
            chabak.setYshams(chabak.getYshams()==null?BigDecimal.ZERO:chabak.getYshams());
            
            BigDecimal bakfee=new BigDecimal("0.00");
            bakfee=bakfee.add(chabak.getSlysam());
            bakfee=bakfee.add(chabak.getFd0028());
            bakfee=bakfee.add(chabak.getYfuams());
            bakfee=bakfee.subtract(chabak.getYshams()); 
            BigDecimal deduam=chabak.getFd0004().subtract(bakfee);
            if(deduAddDo.getDeduam().compareTo(deduam)>0) {
            	throw DpModuleError.DpRisk.RK020009(String.valueOf(j));
            }
            RiskDeduListOutput aplyInfo = DpRiskDao.selRiskDeducByRefeno(deduAddDo.getFd0037(), false);
           
            String deenus = CommTools.getBaseRunEnvs().getTrxn_teller();
            String deentm = CommTools.getBaseRunEnvs().getTimestamp();
            if (CommUtil.isNotNull(aplyInfo)) {
                if (aplyInfo.getFrapst() == E_FRAPST.TO_FROZE) {
                    // 冻结
                    Options<KnbAplyDto> applydo = SysUtil.getInstance(Options.class);
                    KnbAplyDto aply = SysUtil.getInstance(KnbAplyDto.class);
                    CommUtil.copyProperties(aply, aplyInfo);
                    applydo.add(aply);
                    batchFroz(applydo);
                }
                DpRiskDao.updateAplyTpAndSt(aplyInfo.getAplyno(), deduAddDo.getFd0037(), deenus, deentm,
                    deduAddDo.getAgtperc(), E_ENFLAG.SINGLE);
                DpRiskDao.updateAplyToBakfee(bakfee,aplyInfo.getAplyno());
                DpRiskDao.updateChabakSt(deduAddDo.getFd0037());
                DpFrozProc.insertKnbRecord(aplyInfo.getAplyno(), E_OPDESC.DEDUCT_ENTER);
            } else {
                // 录入调单并冻结，然后更改状态
                // 查交易流水
                KnlIoblCups knlIoblCups = AccountFlowDao.selectKnlIoblCupsByRefeno(deduAddDo.getFd0037(), false);
                if (CommUtil.isNull(knlIoblCups)) {
                    throw DpModuleError.DpRisk.RK020007(String.valueOf(j));
                }
                // 校验是否交易已有冻结申请
                KnaMaad knaMaad = KnaMaadDao.selectOne_odb5(knlIoblCups.getCardno(), false);

                KnbAply apply = SysUtil.getInstance(KnbAply.class);
                apply.setAplyno(CommTools.getBaseRunEnvs().getTrxn_seq() + formatter.format(j));
                apply.setAplyid(knlIoblCups.getCardno());
                apply.setFrozam(deduAddDo.getDeduam());
                apply.setFrozbl(deduAddDo.getDeduam());
                // apply.setFroztp(E_FROZTP.FKFROZ);
                apply.setFroztp(E_FROZTP.FKFROZ);
                apply.setFrozow(E_STACTP.STMA);
                apply.setFrexog(E_FREXOG.FK);
                apply.setEnuser(CommTools.getBaseRunEnvs().getTrxn_teller());
                apply.setEntime(CommTools.getBaseRunEnvs().getTimestamp());
                apply.setOperator(CommTools.getBaseRunEnvs().getTrxn_teller());
                apply.setOptime(CommTools.getBaseRunEnvs().getTimestamp());
                apply.setRetrsq(knlIoblCups.getPrepsq());
                apply.setRetrdt(knlIoblCups.getPrepdt());
                apply.setRefeno(deduAddDo.getFd0037());
                apply.setLttscd(CommTools.getBaseRunEnvs().getTrxn_code());
                apply.setMntrsq(CommTools.getBaseRunEnvs().getTrxn_seq());
                apply.setFrapst(E_FRAPST.TO_FROZE);
                apply.setSbrand(knlIoblCups.getOrgaid());
                apply.setBradna(knlIoblCups.getSbrand());
                apply.setMobile(knlIoblCups.getTeleno());
                apply.setMrchno(knlIoblCups.getInmeid());
                apply.setMrchna(knlIoblCups.getInmena());
                apply.setIdtfno(knaMaad.getIdtfno());
                apply.setAgntid(knlIoblCups.getAgid01());
                apply.setAgntna(knlIoblCups.getAd01na());
                apply.setEnflag(E_ENFLAG.SINGLE);
                apply.setTrpodt(CommTools.getBaseRunEnvs().getComputer_date());
                apply.setTmidtfno(knaMaad.getTmidtfno());
                apply.setTmmobile(knlIoblCups.getTmteleno());
                apply.setBakfee(bakfee);
                KnbAplyDao.insert(apply);
                // 记录操作记录
                DpFrozProc.insertKnbRecord(apply.getAplyno(), E_OPDESC.ENTER);
                // 冻结
                Options<KnbAplyDto> applydo = SysUtil.getInstance(Options.class);
                KnbAplyDto aply = SysUtil.getInstance(KnbAplyDto.class);
                CommUtil.copyProperties(aply, apply);
                applydo.add(aply);
                batchFroz(applydo);
                // 更改为强扣状态
                DpRiskDao.updateAplyTpAndSt(apply.getAplyno(), apply.getRefeno(), deenus, deentm,
                    deduAddDo.getAgtperc(), E_ENFLAG.SINGLE);
                DpFrozProc.insertKnbRecord(apply.getAplyno(), E_OPDESC.DEDUCT_ENTER);
                DpRiskDao.updateChabakSt(deduAddDo.getFd0037());
            }
            // 记录操作记录
            refenos.add(deduAddDo.getFd0037());
            j++;
        }

    }

    @Override
    public void
        importDeductInfo(final Options<cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.ImportDeduList> impdelst) {
        if (CollectionUtil.isEmpty(impdelst)) {
            throw DpModuleError.DpTrans.TS020042();
        }
        Set<String> refenos = new HashSet<>();

        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMinimumIntegerDigits(4);
        formatter.setGroupingUsed(false);
        int j = 1;
        BigDecimal percent = new BigDecimal("100");
        for (ImportDeduList applyImport : impdelst) {
            if (CommUtil.isNull(applyImport.getRefeno()) || CommUtil.isNull(applyImport.getRetrdt())
                || CommUtil.isNull(applyImport.getFrozam())) {
                throw DpModuleError.DpRisk.RK020005(String.valueOf(j));
            }
            if (!refenos.add(applyImport.getRefeno())) {
                throw DpModuleError.DpRisk.RK020006(String.valueOf(j));
            }
            if (CommUtil.isNull(applyImport.getAgtperc())) {
                applyImport.setAgtperc(BigDecimal.ZERO);
            }
            BigDecimal agtperc = applyImport.getAgtperc();
            agtperc = agtperc.divide(percent);
            ChabakList chabak = DpRiskDao.selChabakListByRefeno(applyImport.getRefeno(), false);
            if (CommUtil.isNotNull(chabak)) {
                chabak.setSlysam(chabak.getSlysam() == null ? BigDecimal.ZERO : chabak.getSlysam());
                chabak.setFd0028(chabak.getFd0028() == null ? BigDecimal.ZERO : chabak.getFd0028());
                chabak.setYfuams(chabak.getYfuams() == null ? BigDecimal.ZERO : chabak.getYfuams());
                chabak.setYshams(chabak.getYshams() == null ? BigDecimal.ZERO : chabak.getYshams());
                BigDecimal bakfee = new BigDecimal("0.00");
                bakfee = bakfee.add(chabak.getSlysam());
                bakfee = bakfee.add(chabak.getFd0028());
                bakfee = bakfee.add(chabak.getYfuams());
                bakfee = bakfee.subtract(chabak.getYshams());
                RiskDeduListOutput aplyInfo = DpRiskDao.selRiskDeducByRefeno(applyImport.getRefeno(), false);
                String deenus = CommTools.getBaseRunEnvs().getTrxn_teller();
                String deentm = CommTools.getBaseRunEnvs().getTimestamp();
                if (CommUtil.isNotNull(aplyInfo)) {
                    if (aplyInfo.getFrapst() == E_FRAPST.TO_FROZE) {
                        // 冻结
                        Options<KnbAplyDto> applydo = SysUtil.getInstance(Options.class);
                        KnbAplyDto aply = SysUtil.getInstance(KnbAplyDto.class);
                        CommUtil.copyProperties(aply, aplyInfo);
                        applydo.add(aply);
                        batchFroz(applydo);
                    }
                    DpRiskDao.updateAplyTpAndSt(aplyInfo.getAplyno(), applyImport.getRefeno(), deenus, deentm, agtperc,
                        E_ENFLAG.BATCH);
                    DpRiskDao.updateAplyToBakfee(bakfee, aplyInfo.getAplyno());
                    DpRiskDao.updateChabakSt(applyImport.getRefeno());
                    DpFrozProc.insertKnbRecord(aplyInfo.getAplyno(), E_OPDESC.DEDUCT_ENTER);
                } else {
                    // 录入调单并冻结，然后更改状态
                    // 查交易流水
                    KnlIoblCups knlIoblCups = AccountFlowDao.selectKnlIoblCupsByRefeno(applyImport.getRefeno(), false);
                    if (CommUtil.isNull(knlIoblCups)
                        || CommUtil.compare(knlIoblCups.getPrepdt(), applyImport.getRetrdt()) != 0) {
                        throw DpModuleError.DpRisk.RK020007(String.valueOf(j));
                    }
                    // 校验是否交易已有冻结申请
                    KnaMaad knaMaad = KnaMaadDao.selectOne_odb5(knlIoblCups.getCardno(), false);

                    KnbAply apply = SysUtil.getInstance(KnbAply.class);
                    apply.setAplyno(CommTools.getBaseRunEnvs().getTrxn_seq() + formatter.format(j));
                    apply.setAplyid(knlIoblCups.getCardno());
                    apply.setFrozam(applyImport.getFrozam());
                    apply.setFrozbl(applyImport.getFrozam());
                    // apply.setFroztp(E_FROZTP.FKFROZ);
                    apply.setFroztp(E_FROZTP.FKFROZ);
                    apply.setFrozow(E_STACTP.STMA);
                    apply.setFrexog(E_FREXOG.FK);
                    apply.setEnuser(CommTools.getBaseRunEnvs().getTrxn_teller());
                    apply.setEntime(CommTools.getBaseRunEnvs().getTimestamp());
                    apply.setOperator(CommTools.getBaseRunEnvs().getTrxn_teller());
                    apply.setOptime(CommTools.getBaseRunEnvs().getTimestamp());
                    apply.setRetrsq(knlIoblCups.getPrepsq());
                    apply.setRetrdt(knlIoblCups.getPrepdt());
                    apply.setRefeno(knlIoblCups.getRefeno());
                    apply.setLttscd(CommTools.getBaseRunEnvs().getTrxn_code());
                    apply.setMntrsq(CommTools.getBaseRunEnvs().getTrxn_seq());
                    apply.setFrapst(E_FRAPST.TO_FROZE);
                    apply.setSbrand(knlIoblCups.getOrgaid());
                    apply.setBradna(knlIoblCups.getSbrand());
                    apply.setMobile(knlIoblCups.getTeleno());
                    apply.setMrchno(knlIoblCups.getInmeid());
                    apply.setMrchna(knlIoblCups.getInmena());
                    apply.setIdtfno(knaMaad.getIdtfno());
                    apply.setAgntid(knlIoblCups.getAgid01());
                    apply.setAgntna(knlIoblCups.getAd01na());
                    apply.setEnflag(E_ENFLAG.BATCH);
                    apply.setTrpodt(CommTools.getBaseRunEnvs().getComputer_date());
                    apply.setTmidtfno(knaMaad.getTmidtfno());
                    apply.setTmmobile(knlIoblCups.getTmteleno());
                    apply.setBakfee(bakfee);
                    KnbAplyDao.insert(apply);
                    // 记录操作记录
                    DpFrozProc.insertKnbRecord(apply.getAplyno(), E_OPDESC.ENTER);
                    // 冻结
                    Options<KnbAplyDto> applydo = SysUtil.getInstance(Options.class);
                    KnbAplyDto aply = SysUtil.getInstance(KnbAplyDto.class);
                    CommUtil.copyProperties(aply, apply);
                    applydo.add(aply);
                    batchFroz(applydo);
                    // 更改为强扣状态
                    DpRiskDao.updateAplyTpAndSt(apply.getAplyno(), apply.getRefeno(), deenus, deentm, agtperc,
                        E_ENFLAG.BATCH);
                    DpFrozProc.insertKnbRecord(apply.getAplyno(), E_OPDESC.DEDUCT_ENTER);
                    DpRiskDao.updateChabakSt(applyImport.getRefeno());
                }
            }
            // 记录操作记录
            refenos.add(applyImport.getRefeno());
            j++;
        }
    }

    /**
     * 调单强扣
     */
    @Override
    public void forceDeduct(String aplyno) {
        // 只允许执行待强扣、部分强扣
        KnbAply aply = KnbAplyDao.selectOne_odb1(aplyno, false);
        if (aply.getDeapst() != E_DEAPST.TO_DEDUCT && aply.getDeapst() != E_DEAPST.PARTIAL_DEDUCTED) {
            throw DpModuleError.DpRisk.RK020003();
        }

        // 1、待强扣申请单
        // 扣除调单冻结金额到待清算内部户，记录商户强扣金额
        // 如冻结金额不足，调取损失内部户金额到待清算内部户,按比例计算服务商强扣金额、服务商剩余强扣金额
        if (aply.getDeapst() == E_DEAPST.TO_DEDUCT) {
            firstDuduct(aply);
        }
        // 2、部分强扣
        // 扣除调单冻结金额到收益内部户，记录商户强扣金额
        // 如冻结金额不足，扣除服务商电子账户金额到收益内部户，记录服务商剩余强扣金额、服务商已强扣金额、服务商最终强扣金额
        // 当服务商剩余强扣金额为0后，扣除调单冻结金额到服务商电子账户，记录商户强扣金额、服务商最终强扣金额，直至商户强扣金额等于调单金额，即服务商最终强扣金额为0
        // 此时申请单强扣完成
        if (aply.getDeapst() == E_DEAPST.PARTIAL_DEDUCTED) {
            deductAgain(aply);
        }
    }

    // 再次强扣
    private void deductAgain(KnbAply aply) {
        Map<String, BigDecimal> merchentDeductMap = new HashMap<>();
        BigDecimal merchantDeductAmount = BigDecimal.ZERO;// 商户强扣金额
        List<KnbFrozDeta> merchantFrozenDetails = DpRiskDao.selectDetails(aply.getAplyno(), E_FRDEST.FROZEN.getValue(),
            E_YES___.NO.getValue(), null, null, false);// 商户冻结明细
        for (KnbFrozDeta knbFrozDetal : merchantFrozenDetails) {
            merchentDeductMap.put(knbFrozDetal.getFrowid(), merchentDeductMap
                .computeIfAbsent(knbFrozDetal.getFrowid(), k -> BigDecimal.ZERO).add(knbFrozDetal.getFrozam()));
            merchantDeductAmount = merchantDeductAmount.add(knbFrozDetal.getFrozam());
        }

        // 服务商要强扣金额, (调单金额-商户强扣金额)x服务商强扣比例-服务商已强扣金额
        BigDecimal agentToDeduct = (aply.getFrozam().subtract(aply.getDemram().add(merchantDeductAmount))
            .multiply(aply.getAgtperc()).setScale(2, BigDecimal.ROUND_HALF_UP)).subtract(aply.getDedufn());

        BigDecimal agentDeductAmount = BigDecimal.ZERO;
        List<KnbFrozDeta> agentFrozenDetails = new ArrayList<>();
        Map<String, BigDecimal> agentDeductMap = new HashMap<>();
        // 服务商需要强扣
        if (agentToDeduct.compareTo(BigDecimal.ZERO) > 0) {
            agentFrozenDetails = DpRiskDao.selectDetails(aply.getAplyno(), E_FRDEST.FROZEN.getValue(),
                E_YES___.YES.getValue(), null, null, false);// 服务商冻结明细
            for (KnbFrozDeta knbFrozDetal : agentFrozenDetails) {
                if (agentToDeduct.compareTo(agentDeductAmount) > 0) {
                    BigDecimal need = agentToDeduct.subtract(agentDeductAmount);
                    BigDecimal amount = knbFrozDetal.getFrozam();
                    knbFrozDetal.setFrozst(E_FRDEST.DEDUCTED);
                    if (need.compareTo(knbFrozDetal.getFrozam()) < 0) {
                        amount = need;
                        knbFrozDetal.setFrozst(E_FRDEST.PARTIAL_DEDUCT);
                        knbFrozDetal.setThawam(knbFrozDetal.getFrozam().subtract(need));
                    }
                    knbFrozDetal.setDeagbl(amount);
                    knbFrozDetal.setDeduam(amount);
                    agentDeductMap.put(knbFrozDetal.getFrowid(),
                        agentDeductMap.computeIfAbsent(knbFrozDetal.getFrowid(), k -> BigDecimal.ZERO).add(amount));
                    agentDeductAmount = agentDeductAmount.add(amount);
                } else {
                    knbFrozDetal.setFrozst(E_FRDEST.UNFROZEN);
                    knbFrozDetal.setThawam(knbFrozDetal.getFrozam());
                }
            }
        }
        List<KnbFrozDeta> agentDeductDetails = new ArrayList<>();
        Map<String, BigDecimal> agentIncomeMap = new HashMap<>();
        // 商户还款服务商
        if (agentToDeduct.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal merchantToAgent = agentToDeduct.negate();
            String statuses = StringUtils.join(new String[] {E_FRDEST.DEDUCTED.getValue(), E_FRDEST.REPAYING.getValue(),
                E_FRDEST.PARTIAL_DEDUCT.getValue()}, ",");
            agentDeductDetails =
                DpRiskDao.selectDetails(aply.getAplyno(), statuses, E_YES___.YES.getValue(), null, null, false);// 服务商扣款明细
            Iterator<KnbFrozDeta> iterator = agentDeductDetails.iterator();
            while (iterator.hasNext()) {
                KnbFrozDeta knbFrozDetal = iterator.next();
                if (merchantToAgent.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal amount = knbFrozDetal.getDeagbl();
                    if (knbFrozDetal.getDeagbl().compareTo(merchantToAgent) > 0) {
                        amount = merchantToAgent;
                    }
                    agentIncomeMap.put(knbFrozDetal.getFrowid(),
                        agentIncomeMap.computeIfAbsent(knbFrozDetal.getFrowid(), k -> BigDecimal.ZERO).add(amount));
                    knbFrozDetal.setDeagbl(knbFrozDetal.getDeagbl().subtract(amount));
                    if (knbFrozDetal.getDeagbl().compareTo(BigDecimal.ZERO) > 0) {
                        knbFrozDetal.setFrozst(E_FRDEST.REPAYING);
                    } else {
                        knbFrozDetal.setFrozst(E_FRDEST.REPAID);
                    }
                    merchantToAgent = merchantToAgent.subtract(amount);
                } else {
                    iterator.remove();
                }
            }
        }
        KnaAcdc knaAcdcMerchant = KnaAcdcDao.selectOne_odb2(aply.getAplyid(), false);
        KnaCust knaCustMerchant = KnaCustDao.selectOne_odb1(knaAcdcMerchant.getCustac(), false);
        String brchno = knaCustMerchant.getBrchno();
        KnpParameter knpParameterIncome = KnpParameterDao.selectOne_odb1("DP.FORCDE", aply.getSbrand(), "%", "%", true);
        BigDecimal incomeAmount = BigDecimal.ZERO;
        // 商户扣款
        for (Entry<String, BigDecimal> merchantEntry : merchentDeductMap.entrySet()) {
            // 商户还款服务商
            if (!agentIncomeMap.isEmpty()) {
                Iterator<Entry<String, BigDecimal>> agentIncomeIt = agentIncomeMap.entrySet().iterator();
                while (agentIncomeIt.hasNext()) {
                    Entry<String, BigDecimal> agentEntry = agentIncomeIt.next();
                    String agentAcctno = agentEntry.getKey();
                    BigDecimal amount = BigDecimal.ZERO;
                    if (merchantEntry.getValue().compareTo(agentEntry.getValue()) > 0) {
                        amount = agentEntry.getValue();
                        agentIncomeIt.remove();
                    } else {
                        amount = merchantEntry.getValue();
                        agentEntry.setValue(agentEntry.getValue().subtract(amount));
                    }
                    merchantEntry.setValue(merchantEntry.getValue().subtract(amount));
                    // 商户扣除
                    DrawDpAcctIn acctOut = generateDrawDpAcctIn(knaAcdcMerchant.getCustac(), merchantEntry.getKey(),
                        amount, agentAcctno, null);
                    SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(acctOut);
                    // 服务商收入
                    SaveDpAcctIn acctIn = SysUtil.getInstance(SaveDpAcctIn.class);
                    acctIn.setAcctno(agentAcctno);
                    acctIn.setTranam(amount);
                    acctIn.setCrcycd(CNY);
                    acctIn.setSmrycd(SCQK);
                    acctIn.setDetlsq(Long.parseLong(CoreUtil.nextValue("detlsq")));
                    acctIn.setToacct(merchantEntry.getKey());
                    SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(acctIn);
                    if (merchantEntry.getValue().compareTo(BigDecimal.ZERO) == 0) {
                        break;
                    }
                }
            }
            // 商户还款收入内部户
            if (merchantEntry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                DrawDpAcctIn acctOut = generateDrawDpAcctIn(knaAcdcMerchant.getCustac(), merchantEntry.getKey(),
                    merchantEntry.getValue(), knpParameterIncome.getParm_value1(), knpParameterIncome.getParm_value3());
                SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(acctOut);
                incomeAmount = incomeAmount.add(merchantEntry.getValue());
            }
        }
        KnaSbad knaSbadAgent = KnaSbadDao.selectOne_odb2(aply.getAgntid(), true);
        // 服务商扣款
        for (Entry<String, BigDecimal> agentDeductEntry : agentDeductMap.entrySet()) {
            DrawDpAcctIn acctOut = generateDrawDpAcctIn(knaSbadAgent.getCustac(), agentDeductEntry.getKey(),
                agentDeductEntry.getValue(), knpParameterIncome.getParm_value1(), knpParameterIncome.getParm_value3());
            SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(acctOut);
            incomeAmount = incomeAmount.add(agentDeductEntry.getValue());
        }
        // 贷：收入
        if (incomeAmount.compareTo(BigDecimal.ZERO) > 0) {
            IoAccounttingIntf incomeIoAccounttingIntf = generateIoAccounttingIntf(knpParameterIncome.getParm_value1(),
                incomeAmount, brchno, E_AMNTCD.CR, null, null);
            SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(incomeIoAccounttingIntf);
        }
        // 更新申请
        KnbAplyDto aplyUpdate = generateKnbAply(aply.getAplyno());
        aplyUpdate.setDemram(aply.getDemram().add(merchantDeductAmount));
        if (agentToDeduct.compareTo(BigDecimal.ZERO) > 0) {
            aplyUpdate.setFragbl(agentToDeduct.subtract(agentDeductAmount));
            aplyUpdate.setDedubl(agentToDeduct.subtract(agentDeductAmount));
            aplyUpdate.setDeduam(aply.getDeduam().add(agentDeductAmount));
            aplyUpdate.setDedufn(aplyUpdate.getDeduam());
            aplyUpdate.setAdvaamt(aply.getAdvaamt().subtract(merchantDeductAmount).subtract(agentDeductAmount));
        } else {
            aplyUpdate.setFragbl(BigDecimal.ZERO);
            aplyUpdate.setDedubl(BigDecimal.ZERO);
            aplyUpdate.setDedufn(aply.getDedufn().add(agentToDeduct));
            aplyUpdate.setAdvaamt(aply.getAdvaamt().subtract(merchantDeductAmount.add(agentToDeduct)));
        }
        if (aplyUpdate.getDemram().compareTo(aply.getFrozam()) < 0) {
            aplyUpdate.setDeapst(E_DEAPST.PARTIAL_DEDUCTED);
        } else {
            aplyUpdate.setDeapst(E_DEAPST.DEDUCTED);
        }
        DpRiskDao.updateKnbAply(aplyUpdate);
        // 更新商户冻结明细
        for (KnbFrozDeta detail : merchantFrozenDetails) {
            KnbFrozDeta froz_deta = SysUtil.getInstance(KnbFrozDeta.class);
            froz_deta.setAplyno(detail.getAplyno());
            froz_deta.setFrozsq(detail.getFrozsq());
            froz_deta.setForcdt(CommTools.getBaseRunEnvs().getComputer_date());
            froz_deta.setForctm(CommTools.getBaseRunEnvs().getComputer_time());
            froz_deta.setFrozst(E_FRDEST.DEDUCTED);
            froz_deta.setDeduam(detail.getFrozam());
            froz_deta.setTmstmp(CommTools.getBaseRunEnvs().getTimestamp());
            int updated = DpRiskDao.updateFrozenDetail(froz_deta);
            if (updated != 1) {
                throw new AdpDaoException("强扣交易异常：" + detail.getTdtrsq());
            }
        }
        // 更新服务商冻结明细
        for (KnbFrozDeta detail : agentFrozenDetails) {
            KnbFrozDeta froz_deta = SysUtil.getInstance(KnbFrozDeta.class);
            froz_deta.setAplyno(detail.getAplyno());
            froz_deta.setFrozsq(detail.getFrozsq());
            if (detail.getFrozst() == E_FRDEST.DEDUCTED || detail.getFrozst() == E_FRDEST.PARTIAL_DEDUCT) {
                froz_deta.setForcdt(CommTools.getBaseRunEnvs().getComputer_date());
                froz_deta.setForctm(CommTools.getBaseRunEnvs().getComputer_time());
            }
            if (detail.getFrozst() == E_FRDEST.UNFROZEN || detail.getFrozst() == E_FRDEST.PARTIAL_DEDUCT) {
                froz_deta.setThawdt(CommTools.getBaseRunEnvs().getComputer_date());
                froz_deta.setThawtm(CommTools.getBaseRunEnvs().getComputer_time());
            }
            froz_deta.setFrozst(detail.getFrozst());
            froz_deta.setTmstmp(CommTools.getBaseRunEnvs().getTimestamp());
            froz_deta.setDeduam(detail.getFrozam());
            froz_deta.setDeagbl(detail.getDeagbl());
            froz_deta.setThawam(detail.getThawam());
            int updated = DpRiskDao.updateFrozenDetail(froz_deta);
            if (updated != 1) {
                throw new AdpDaoException("强扣交易异常：" + detail.getTdtrsq());
            }
        }
        // 更新服务商强扣明细
        for (KnbFrozDeta knbFrozDetal : agentDeductDetails) {
            KnbFrozDeta froz_deta = SysUtil.getInstance(KnbFrozDeta.class);
            froz_deta.setAplyno(knbFrozDetal.getAplyno());
            froz_deta.setFrozsq(knbFrozDetal.getFrozsq());
            froz_deta.setFrozst(knbFrozDetal.getFrozst());
            froz_deta.setTmstmp(CommTools.getBaseRunEnvs().getTimestamp());
            froz_deta.setDeagbl(knbFrozDetal.getDeagbl());
            DpRiskDao.updateDeductDetail(froz_deta);
        }
        merchentDeductMap.putAll(agentDeductMap);
        // 更新主体表
        for (Entry<String, BigDecimal> entry : merchentDeductMap.entrySet()) {
            KnbFrozOwne owne = KnbFrozOwneDao.selectOneWithLock_odb1(E_FROZOW.AUACCT, entry.getKey(), false);
            owne.setFrozbl(owne.getFrozbl().subtract(entry.getValue()));
            KnbFrozOwneDao.updateOne_odb1(owne);
        }
    }

    // 第一次强扣
    private void firstDuduct(KnbAply aply) {
        List<KnbFrozDeta> frozenDetails = DpRiskDao.selectDetails(aply.getAplyno(), E_FRDEST.FROZEN.getValue(),
            E_YES___.NO.getValue(), null, null, false);// 已冻结明细
        KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.FORCDE", "DQS", "%", "%", false);
        Map<String, BigDecimal> merchentDeductMap = new HashMap<>();
        BigDecimal merchantDeductAmount = BigDecimal.ZERO;
        for (KnbFrozDeta knbFrozDetal : frozenDetails) {
            merchentDeductMap.put(knbFrozDetal.getFrowid(), merchentDeductMap
                .computeIfAbsent(knbFrozDetal.getFrowid(), k -> BigDecimal.ZERO).add(knbFrozDetal.getFrozam()));
            merchantDeductAmount = merchantDeductAmount.add(knbFrozDetal.getFrozam());
        }
        KnaAcdc knaAcdc = KnaAcdcDao.selectOne_odb2(aply.getAplyid(), false);
        KnaCust knaCust = KnaCustDao.selectOne_odb1(knaAcdc.getCustac(), false);
        String brchno = knaCust.getBrchno();
        // 商户扣款
        for (Entry<String, BigDecimal> entry : merchentDeductMap.entrySet()) {
            DrawDpAcctIn acctOut = generateDrawDpAcctIn(knaAcdc.getCustac(), entry.getKey(), entry.getValue(),
                knpParameter.getParm_value1(), knpParameter.getParm_value3());
            SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(acctOut);
        }
        BigDecimal lossAmount = BigDecimal.ZERO;
        if (aply.getFrozam().compareTo(merchantDeductAmount) > 0) {
            lossAmount = aply.getFrozam().subtract(merchantDeductAmount);
        }
        BigDecimal fee = aply.getBakfee() != null ? aply.getBakfee() : BigDecimal.ZERO;// 手续费
        // 借：银联强扣
        KnpParameter knpParameterLose = KnpParameterDao.selectOne_odb1("DP.FORCDE", "YLQK", "%", "%", true);
        IoAccounttingIntf lossIoAccounttingIntf = generateIoAccounttingIntf(knpParameterLose.getParm_value1(),
            lossAmount.add(fee), brchno, E_AMNTCD.DR, knpParameter.getParm_value1(), knpParameter.getParm_value3());
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(lossIoAccounttingIntf);
        // 贷：待清算
        IaAcdrInfo info = SysUtil.getInstance(IaAcdrInfo.class);
        info.setBusino(knpParameter.getParm_value1());
        info.setAcbrch(brchno);
        info.setCrcycd(CNY);
        info.setTranam(aply.getFrozam().add(fee));
        SysUtil.getInstance(IoInAccount.class).ioInAccr(info);
        // 更新调单申请
        KnbAplyDto aplyUpdate = generateKnbAply(aply.getAplyno());
        // 服务商要强扣金额, (调单金额-商户强扣金额)x服务商强扣比例
        BigDecimal deagam = aply.getFrozam().subtract(merchantDeductAmount).multiply(aply.getAgtperc()).setScale(2,
            BigDecimal.ROUND_HALF_UP);
        aplyUpdate.setDemram(merchantDeductAmount);
        aplyUpdate.setFragbl(deagam);
        aplyUpdate.setDedubl(deagam);
        aplyUpdate.setDeduam(BigDecimal.ZERO);
        aplyUpdate.setDedufn(BigDecimal.ZERO);
        if (aplyUpdate.getDemram().compareTo(aply.getFrozam()) < 0) {
            aplyUpdate.setDeapst(E_DEAPST.PARTIAL_DEDUCTED);
        } else {
            aplyUpdate.setDeapst(E_DEAPST.DEDUCTED);
        }
        aplyUpdate.setDetime(CommTools.getBaseRunEnvs().getTimestamp());
        aplyUpdate.setAdvaamt(aply.getFrozam().subtract(merchantDeductAmount));
        DpRiskDao.updateKnbAply(aplyUpdate);
        // 更新冻结明细
        for (KnbFrozDeta detail : frozenDetails) {
            KnbFrozDeta froz_deta = SysUtil.getInstance(KnbFrozDeta.class);
            froz_deta.setAplyno(detail.getAplyno());
            froz_deta.setFrozsq(detail.getFrozsq());
            froz_deta.setForcdt(CommTools.getBaseRunEnvs().getComputer_date());
            froz_deta.setForctm(CommTools.getBaseRunEnvs().getComputer_time());
            froz_deta.setFrozst(E_FRDEST.DEDUCTED);
            froz_deta.setTmstmp(CommTools.getBaseRunEnvs().getTimestamp());
            froz_deta.setDeduam(detail.getFrozam());
            int updated = DpRiskDao.updateFrozenDetail(froz_deta);
            if (updated != 1) {
                throw new AdpDaoException("强扣交易异常：" + detail.getTdtrsq());
            }
        }
        for (Entry<String, BigDecimal> entry : merchentDeductMap.entrySet()) {
            KnbFrozOwne owne = KnbFrozOwneDao.selectOneWithLock_odb1(E_FROZOW.AUACCT, entry.getKey(), false);
            owne.setFrozbl(owne.getFrozbl().subtract(entry.getValue()));
            KnbFrozOwneDao.updateOne_odb1(owne);
        }
    }

    private DrawDpAcctIn generateDrawDpAcctIn(String custac, String acctno, BigDecimal tranam, String toacct,
        String opacna) {
        DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
        drawDpAcctIn.setCustac(custac);
        drawDpAcctIn.setAcctno(acctno);
        drawDpAcctIn.setTranam(tranam);
        drawDpAcctIn.setCrcycd(CNY);
        drawDpAcctIn.setSmrycd(SCQK);
        drawDpAcctIn.setDetlsq(Long.parseLong(CoreUtil.nextValue("detlsq")));
        drawDpAcctIn.setToacct(toacct);
        drawDpAcctIn.setOpacna(opacna);
        drawDpAcctIn.setIsdedu(E_YES___.YES);
        return drawDpAcctIn;
    }

    private KnbAplyDto generateKnbAply(String aplyno) {
        KnbAplyDto aply = SysUtil.getInstance(KnbAplyDto.class);
        aply.setAplyno(aplyno);
        aply.setOperator(CommTools.getBaseRunEnvs().getTrxn_teller());
        aply.setOptime(CommTools.getBaseRunEnvs().getTimestamp());
        aply.setTmstmp(CommTools.getBaseRunEnvs().getTimestamp());
        return aply;
    }

    private IoAccounttingIntf generateIoAccounttingIntf(String busino, BigDecimal tranam, String brchno,
        E_AMNTCD amntcd, String toacct, String toacna) {
        IoAccounttingIntf ioAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
        ioAccounttingIntf.setCorpno(CommTools.getBusiOrgId());
        ioAccounttingIntf.setProdcd(busino);
        ioAccounttingIntf.setDtitcd(busino);
        ioAccounttingIntf.setCrcycd(CNY);
        ioAccounttingIntf.setTranam(tranam);
        ioAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());
        ioAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
        ioAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
        ioAccounttingIntf.setAcctbr(brchno);
        ioAccounttingIntf.setAmntcd(amntcd);
        ioAccounttingIntf.setAtowtp(E_ATOWTP.IN);
        ioAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
        ioAccounttingIntf.setBltype(E_BLTYPE.BALANCE);
        ioAccounttingIntf.setToacct(toacct);
        ioAccounttingIntf.setToacna(toacna);
        ioAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
        return ioAccounttingIntf;
    }

    public void
        enterChabak(final Options<cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.EnterChabakImp> chabaklist) {
        if (CollectionUtil.isEmpty(chabaklist)) {
            throw DpModuleError.DpTrans.TS020042();
        }
        Options<EnterChabakImp> aply = SysUtil.getInstance(Options.class);
        Set<String> refenos = new HashSet<>();
        long i = 1L;
        for (EnterChabakImp enterChabak : chabaklist) {
            if (CommUtil.isNull(enterChabak.getFd0037()) || CommUtil.isNull(enterChabak.getFd0903())
            // || CommUtil.isNull(enterChabak.getTranam())
            ) {
                throw DpModuleError.DpTrans.TS020043();
            }
            if (!refenos.add(enterChabak.getFd0037())) {
                throw DpModuleError.DpTrans.TS020045();
            }
            // 查交易流水
            KnlIoblCups knlIoblCups = AccountFlowDao.selectKnlIoblCupsByRefeno(enterChabak.getFd0037(), false);
            if (CommUtil.isNull(knlIoblCups)
                || CommUtil.compare(knlIoblCups.getPrepdt(), enterChabak.getFd0903()) != 0) {
                throw DpModuleError.DpTrans.TS020020();
            }
            KnaMaad knaMaad = KnaMaadDao.selectOne_odb5(knlIoblCups.getCardno(), false);
            BigDecimal div=new BigDecimal("100");
            // 申请登记
            KnbChabck chabak = SysUtil.getInstance(KnbChabck.class);
            chabak.setTrandt(enterChabak.getTrandt()); // 处理日期
            chabak.setErrtgs(enterChabak.getErrtgs()); // 差错交易标志
            chabak.setFd0032(enterChabak.getFd0032()); // 代理机构标识码
            chabak.setFd0033(enterChabak.getFd0033()); // 发送机构标识码
            chabak.setFd0011(enterChabak.getFd0011()); // 系统跟踪号
            chabak.setFd0007(enterChabak.getFd0007()); // 交易传输时间
            chabak.setFd0002(enterChabak.getFd0002()); // 主账号
            chabak.setFd0004(enterChabak.getFd0004().divide(div)); // 交易金额
            chabak.setMesgtp(enterChabak.getMesgtp()); // 报文类型
            chabak.setFd0003(enterChabak.getFd0003()); // 交易类型码
            chabak.setFd0018(enterChabak.getFd0018()); // 商户类型
            chabak.setFd0041(enterChabak.getFd0041()); // 受卡机终端标识码
            chabak.setFd0037(enterChabak.getFd0037()); // 上一笔交易检索参考号
            chabak.setFd0025(enterChabak.getFd0025()); // 服务点条件码
            chabak.setFd0038(enterChabak.getFd0038()); // 授权应答码
            chabak.setFd0100(enterChabak.getFd0100()); // 接收机构标识码
            chabak.setFkbkcd(enterChabak.getFkbkcd()); // 发卡银行标识码
            chabak.setFd0902(enterChabak.getFd0902()); // 上一笔交易的系统跟踪号
            chabak.setFd0039(enterChabak.getFd0039()); // 交易返回码
            chabak.setFd0022(enterChabak.getFd0022()); // 服务点输入方式
            chabak.setSlysam(enterChabak.getSlysam().divide(div)); // 受理方应收手续费
            chabak.setSlyfam(enterChabak.getSlyfam().divide(div)); // 受理方应付手续费
            chabak.setFqfams(enterChabak.getFqfams().divide(div)); // 分期付款附加手续费
            chabak.setFd0028(enterChabak.getFd0028().divide(div)); // 持卡人交易手续费
            chabak.setYshams(enterChabak.getYshams().divide(div)); // 应收费用
            chabak.setYfuams(enterChabak.getYfuams().divide(div)); // 应付费用
            chabak.setCacyin(enterChabak.getCacyin()); // 差错原因
            chabak.setZchucd(enterChabak.getZchucd()); // 接收机构标识码/转出机构标识码
            chabak.setFd0102(enterChabak.getFd0102()); // 转出卡号
            chabak.setZrucds(enterChabak.getZrucds()); // 转入机构标识码
            chabak.setFd0103(enterChabak.getFd0103()); // 转入卡号
            chabak.setFd0903(enterChabak.getFd0903()); // 上一笔交易的日期时间
            chabak.setFd0023(enterChabak.getFd0023()); // 卡片序列号
            chabak.setFd6022(enterChabak.getFd6022()); // 终端读取能力
            chabak.setFd6023(enterChabak.getFd6023()); // IC卡条件代码
            chabak.setFd0015(enterChabak.getFd0015()); // 上一笔交易清算日期
            chabak.setTranam(enterChabak.getTranam().divide(div)); // 上一笔交易金额
            chabak.setJydytp(enterChabak.getJydytp()); // 交易地域标志
            chabak.setEcitps(enterChabak.getEcitps()); // ECI标志
            chabak.setFd0042(enterChabak.getFd0042()); // 商户代码
            chabak.setFsqsjg(enterChabak.getFsqsjg()); // 发送方清算机构
            chabak.setZcqsjg(enterChabak.getZcqsjg()); // 接收方清算机构/转出方清算机构
            chabak.setZrqsjg(enterChabak.getZrqsjg()); // 转入方清算机构
            chabak.setFd6025(enterChabak.getFd6025()); // 上一笔交易终端类型
            chabak.setFd0043(enterChabak.getFd0043()); // 商户名称地址
            chabak.setFd6031(enterChabak.getFd6031()); // 特殊计费类型
            chabak.setFd6032(enterChabak.getFd6032()); // 特殊计费档次
            chabak.setTaccds(enterChabak.getTaccds()); // 保留使用
            chabak.setKcpxxs(enterChabak.getKcpxxs()); // 卡产品标识信息
            chabak.setYccdms(enterChabak.getYccdms()); // 引发差错交易的最原始交易的交易代码
            chabak.setFd6035(enterChabak.getFd6035()); // 交易发起方式
            chabak.setFd6038(enterChabak.getFd6038()); // 账户结算类型
            chabak.setFd0060(enterChabak.getFd0060()); // 保留使用
            chabak.setMobile(knlIoblCups.getTeleno());
            chabak.setTmmobile(knlIoblCups.getTmteleno());
            chabak.setIdtfno(knaMaad.getIdtfno());
            chabak.setTmidtfno(knaMaad.getTmidtfno());
            chabak.setMrchno(knlIoblCups.getInmeid());
            chabak.setBradna(knlIoblCups.getSbrand());
            chabak.setSbrand(knlIoblCups.getOrgaid());
            chabak.setCbakst(E_CBAKST.UNBACK);
            KnbChabckDao.insert(chabak);
            // knbAply applydo = SysUtil.getInstance(knbAply.class);
            // CommUtil.copyProperties(applydo, apply);
            // aply.add(applydo);
            // // 记录操作记录
            // insertRecord(apply,E_OPDESC.ENTER);
            refenos.add(enterChabak.getFd0037());
            i++;
        }
    }

    @Override
    public void agentFrozen(AgentFrozenInput agentFrozen) {
        List<KnbAplyDto> knbAplyDtos = DpRiskDao.selectFrozenAplyInfos(null, E_FRAPST.PARTIAL_FROZEN.getValue(), null,
            E_DEAPST.PARTIAL_DEDUCTED.getValue(), agentFrozen.getAgntid(), false);
        BigDecimal balance = agentFrozen.getTranam();
        for (KnbAplyDto knbAplyDto : knbAplyDtos) {
            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            if (knbAplyDto.getFragbl().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal amount = knbAplyDto.getFragbl();
            if (knbAplyDto.getFragbl().compareTo(balance) > 0) {
                amount = balance;
            }

            DpFrozProc.insertDetalInfo(knbAplyDto, amount, agentFrozen.getAcctno(), null, null, null, null,
                agentFrozen.getSbrand(), E_YES___.YES);
            // 更新申请表
            knbAplyDto.setOperator(CommTools.getBaseRunEnvs().getTrxn_teller());
            knbAplyDto.setOptime(CommTools.getBaseRunEnvs().getTimestamp());
            DpRiskDao.updateKnbAply(knbAplyDto);
            DpFrozProc.insertKnbRecord(knbAplyDto.getAplyno(), DpRiskEnumType.E_OPDESC.FROZE);
            balance = balance.subtract(amount);
        }
        if (balance.compareTo(agentFrozen.getTranam()) < 0) {
            BigDecimal frozenAmt = agentFrozen.getTranam().subtract(balance);
            // 更新主体表
            KnbFrozOwne owne = KnbFrozOwneDao.selectOneWithLock_odb1(E_FROZOW.AUACCT, agentFrozen.getAcctno(), false);
            if (owne == null) {
                owne = DpFrozProc.setOwneInfo();
                owne.setFrowid(agentFrozen.getAcctno());
                owne.setFrozbl(frozenAmt);
                KnbFrozOwneDao.insert(owne);
            } else {
                owne.setFrozbl(owne.getFrozbl().add(frozenAmt));
                KnbFrozOwneDao.updateOne_odb1(owne);
            }
        }
    }


}