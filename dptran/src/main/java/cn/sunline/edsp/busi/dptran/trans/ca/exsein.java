
package cn.sunline.edsp.busi.dptran.trans.ca;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.util.PropertyUtil;
import cn.sunline.edsp.busi.dp.namedsql.ca.EdmAfterDayBatchDao;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccount;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUPSST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SETTST;
import cn.sunline.edsp.busi.ds.iobus.type.DsEnumType.E_BUSITP;
import cn.sunline.ltts.busi.sys.errors.ApError;

public class exsein {
    private static BizLog bizlog = BizLogUtil.getBizLog(exsein.class);

    public static void exportSettlement(final cn.sunline.edsp.busi.dptran.trans.ca.intf.Exsein.Input input,
        final cn.sunline.edsp.busi.dptran.trans.ca.intf.Exsein.Property property,
        final cn.sunline.edsp.busi.dptran.trans.ca.intf.Exsein.Output output) {
        String orgaid = input.getOrgaid(); // 品牌id
        E_BUSITP busitp = input.getBusitp(); // 业务类型
        String inmeid = input.getInmeid(); // 内部商户号
        String teleno = input.getTeleno(); // 手机号
        String mntrsq = input.getMntrsq(); // 主交易流水
        String stardt = input.getStardt(); // 结算开始日期
        String endtdt = input.getEndtdt(); // 结算结束日期
        E_CUPSST transt = input.getTranst();
        E_SETTST settst = input.getSettst();   //结算单状态
		String taskid = input.getTaskid();     //批次号

        List<EdmSettleAccount> edms = EdmAfterDayBatchDao.selectEdmSettleAccountInfo(orgaid, inmeid, busitp, teleno,
            stardt, endtdt, mntrsq, transt,settst,taskid, false);
        for (EdmSettleAccount edmSettleAccount : edms) {
            cn.sunline.edsp.busi.dptran.trans.ca.intf.Exsein.Output.List data =
                SysUtil.getInstance(cn.sunline.edsp.busi.dptran.trans.ca.intf.Exsein.Output.List.class);
            try {
                PropertyUtil.copyProperties(edmSettleAccount, data, true, false);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                bizlog.error("导出结算单列表时copyProperties异常", e);
                throw ApError.Sys.E9006();
            }
            data.setServtp(edmSettleAccount.getServtp() == null ? null : edmSettleAccount.getServtp().getLongName());
            data.setTranst(edmSettleAccount.getTranst() == null ? null : edmSettleAccount.getTranst().getLongName());
            data.setSettst(edmSettleAccount.getSettst() == null ? null : edmSettleAccount.getSettst().getLongName());
            output.getList().add(data);
        }
    }
}
