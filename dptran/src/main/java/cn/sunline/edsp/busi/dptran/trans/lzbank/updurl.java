
package cn.sunline.edsp.busi.dptran.trans.lzbank;

import cn.sunline.edsp.busi.dp.namedsql.ca.AccountFlowDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsDao;

public class updurl {

    public static void updateSignim(final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Updurl.Input input,
        final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Updurl.Property property,
        final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Updurl.Output output) {
        KnlIoblCups knlIoblCups = KnlIoblCupsDao.selectOne_odb2(input.getMntrsq(), input.getTrandt(), true);
        knlIoblCups.setSignim(input.getSignim());
        AccountFlowDao.updateKnlIoblCupsSignim(knlIoblCups);
    }
}
