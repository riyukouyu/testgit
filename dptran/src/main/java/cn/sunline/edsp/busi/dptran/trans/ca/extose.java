
package cn.sunline.edsp.busi.dptran.trans.ca;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.util.PropertyUtil;
import cn.sunline.edsp.busi.dp.namedsql.ca.EdmAfterDayBatchDao;
import cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.DpToSettleAccounts;
import cn.sunline.edsp.busi.ds.iobus.type.DsEnumType.E_BUSITP;
import cn.sunline.ltts.busi.sys.errors.ApError;

public class extose {
    private static BizLog bizlog = BizLogUtil.getBizLog(extose.class);

    public static void exportPreStatements(final cn.sunline.edsp.busi.dptran.trans.ca.intf.Extose.Input input,
        final cn.sunline.edsp.busi.dptran.trans.ca.intf.Extose.Property property,
        final cn.sunline.edsp.busi.dptran.trans.ca.intf.Extose.Output output) {
        String orgaid = input.getOrgaid(); // 品牌id
        E_BUSITP busitp = input.getBusitp(); // 业务类型
        String inmeid = input.getInmeid(); // 内部商户号
        String teleno = input.getTeleno(); // 手机号
        String mntrsq = input.getMntrsq(); // 主交易流水
        String stardt = input.getStardt(); // 结算开始日期
        String endtdt = input.getEndtdt(); // 结算结束日期
        String ordeno = input.getOrdeno();	    //订单号
		String refeno = input.getRefeno();		//参考号
        List<DpToSettleAccounts> dpToSettleAccounts =
            EdmAfterDayBatchDao.selectKnlIoblCupsInfo(orgaid, inmeid, busitp, teleno, stardt, endtdt, mntrsq,ordeno,refeno, false);

        for (DpToSettleAccounts dpToSettleAccount : dpToSettleAccounts) {
            String merate = dpToSettleAccount.getInflrt();
            if (CommUtil.isNotNull(merate)) {
                String[] str = merate.split("\\|");
                String str1 = str[0];
                String str4 = str[4];
                String str6 = str[5];
                BigDecimal rate = new BigDecimal("0");
                BigDecimal rate1 = new BigDecimal("0");
                BigDecimal rate4 = new BigDecimal("0");
                BigDecimal rate6 = new BigDecimal("0");
                if (CommUtil.isNotNull(str1) && !"null".equals(str1)) {
                    rate1 = new BigDecimal(str1);
                }
                if (CommUtil.isNotNull(str4) && !"null".equals(str4)) {
                    rate4 = new BigDecimal(str4);
                }
                if (CommUtil.isNotNull(str6) && !"null".equals(str6)) {
                    rate6 = new BigDecimal(str6);
                }
                rate = rate1.add(rate4);
                dpToSettleAccount.setInflrt(rate.toString());
                dpToSettleAccount.setScflpo(rate6.toString());
            }

            cn.sunline.edsp.busi.dptran.trans.ca.intf.Extose.Output.List data =
                SysUtil.getInstance(cn.sunline.edsp.busi.dptran.trans.ca.intf.Extose.Output.List.class);
            try {
                PropertyUtil.copyProperties(dpToSettleAccount, data, true, false);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                bizlog.error("导出预结算单列表时copyProperties异常", e);
                throw ApError.Sys.E9006();
            }

            data.setBusitp(dpToSettleAccount.getBusitp() == null ? null : dpToSettleAccount.getBusitp().getLongName());
            data.setCardtp(dpToSettleAccount.getCardtp() == null ? null : dpToSettleAccount.getCardtp().getLongName());
            data.setFinsty(dpToSettleAccount.getFinsty() == null ? null : dpToSettleAccount.getFinsty().getLongName());
            data.setMercfg(dpToSettleAccount.getMercfg() == null ? null : dpToSettleAccount.getMercfg().getLongName());
            data.setSpcipy(dpToSettleAccount.getSpcipy() == null ? null : dpToSettleAccount.getSpcipy().getLongName());
            output.getList().add(data);
        }
    }
}
