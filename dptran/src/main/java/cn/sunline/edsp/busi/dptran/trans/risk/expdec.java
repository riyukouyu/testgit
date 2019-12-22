package cn.sunline.edsp.busi.dptran.trans.risk;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.util.PropertyUtil;
import cn.sunline.edsp.base.util.collection.CollectionUtil;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.RiskDeduListInput;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.RiskDeduListOutput;
import cn.sunline.edsp.busi.dp.namedsql.DpRiskDao;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SBRAND;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;

public class expdec {
    private static BizLog bizlog = BizLogUtil.getBizLog(expdec.class);

    public static void exportDeducList(final cn.sunline.edsp.busi.dptran.trans.risk.intf.Expdec.Input input,
        final cn.sunline.edsp.busi.dptran.trans.risk.intf.Expdec.Property property,
        final cn.sunline.edsp.busi.dptran.trans.risk.intf.Expdec.Output output) {
        RiskDeduListInput selInput = SysUtil.getInstance(RiskDeduListInput.class);
        CommUtil.copyProperties(selInput, input);
        selInput.setFroztp(E_FROZTP.QKFROZ);
        List<RiskDeduListOutput> knbAplyListInfos = DpRiskDao.selRiskDeducList(selInput, false);
        if (CollectionUtil.isEmpty(knbAplyListInfos)) {
            return;
        }
        for (RiskDeduListOutput info : knbAplyListInfos) {
            info.setFrozal(info.getFrozam().subtract(info.getFrozbl()));
            cn.sunline.edsp.busi.dptran.trans.risk.intf.Expdec.Output.DeduList data =
                SysUtil.getInstance(cn.sunline.edsp.busi.dptran.trans.risk.intf.Expdec.Output.DeduList.class);
            try {
                PropertyUtil.copyProperties(info, data, true, false);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                bizlog.error("导出强扣列表时copyProperties异常", e);
                throw ApError.Sys.E9006();
            }
            data.setFrapst(info.getFrapst().getLongName());
            data.setFrozow(info.getFrozow().getLongName());
            if (CommUtil.isNotNull(info.getDeapst())) {
                data.setDeapst(info.getDeapst().getLongName());
            }
            if (CommUtil.isNotNull(info.getEntime())) {
                data.setEntime(DpPublic.parseTimestamp(info.getEntime()));
            }
            if (CommUtil.isNotNull(info.getDeentm())) {
                data.setDeentm(DpPublic.parseTimestamp(info.getDeentm()));
            }
            if (CommUtil.isNotNull(info.getDetime())) {
                data.setDetime(DpPublic.parseTimestamp(info.getDetime()));
            }
            if (StringUtils.isNotBlank(info.getSbrand())) {
                data.setBradna(CommUtil.toEnum(E_SBRAND.class, info.getSbrand()).getLongName());
            }
            output.getDeduList().add(data);
        }
    }
}
