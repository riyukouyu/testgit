
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
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.KnbAplyListInfo;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.SelRiskFrozenListInput;
import cn.sunline.edsp.busi.dp.namedsql.DpRiskDao;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SBRAND;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;

public class exrsfr {
    private static BizLog bizlog = BizLogUtil.getBizLog(exrsfr.class);

    public static void exportFrozenList(final cn.sunline.edsp.busi.dptran.trans.risk.intf.Exrsfr.Input input,
        final cn.sunline.edsp.busi.dptran.trans.risk.intf.Exrsfr.Property property,
        final cn.sunline.edsp.busi.dptran.trans.risk.intf.Exrsfr.Output output) {
        SelRiskFrozenListInput selInput = SysUtil.getInstance(SelRiskFrozenListInput.class);
        CommUtil.copyProperties(selInput, input);
        selInput.setFroztp(E_FROZTP.FKFROZ);
        List<KnbAplyListInfo> knbAplyListInfos = DpRiskDao.selectKnbAplyPageList(selInput, false);
        if (CollectionUtil.isEmpty(knbAplyListInfos)) {
            return;
        }
        for (KnbAplyListInfo info : knbAplyListInfos) {
            info.setFrozal(info.getFrozam().subtract(info.getFrozbl()));
            cn.sunline.edsp.busi.dptran.trans.risk.intf.Exrsfr.Output.List data =
                SysUtil.getInstance(cn.sunline.edsp.busi.dptran.trans.risk.intf.Exrsfr.Output.List.class);
            try {
                PropertyUtil.copyProperties(info, data, true, false);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                bizlog.error("导出冻结列表时copyProperties异常", e);
                throw ApError.Sys.E9006();
            }
            data.setFrapst(info.getFrapst().getLongName());
            data.setFrozow(info.getFrozow().getLongName());
            if (CommUtil.isNotNull(info.getEntime())) {
                data.setEntime(DpPublic.parseTimestamp(info.getEntime()));
            }
            if (CommUtil.isNotNull(info.getFrtime())) {
                data.setFrtime(DpPublic.parseTimestamp(info.getFrtime()));
            }
            if (StringUtils.isNotBlank(info.getSbrand())) {
                data.setBradna(CommUtil.toEnum(E_SBRAND.class, info.getSbrand()).getLongName());
            }
            output.getList().add(data);
        }
    }
}
