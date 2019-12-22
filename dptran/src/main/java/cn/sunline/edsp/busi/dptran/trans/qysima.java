
package cn.sunline.edsp.busi.dptran.trans;

import java.util.List;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.busi.dp.iobus.type.dp.DpdebitAcctnos.CustacList;
import cn.sunline.edsp.busi.dp.namedsql.soc.SocQyAcctsDao;

/**
 * 根据签约主体名称和标识查询电子账户信息
 * @author sunline16
 *
 */
public class qysima {

	public static void qySignMaster(final cn.sunline.edsp.busi.dptran.trans.intf.Qysima.Input input,
			final cn.sunline.edsp.busi.dptran.trans.intf.Qysima.Property property,
			final cn.sunline.edsp.busi.dptran.trans.intf.Qysima.Output output) {
		String custna = input.getCustna();
		List<CustacList> list = SocQyAcctsDao.selCustnoInfos(custna, false);
		Options<CustacList> lists = SysUtil.getInstance(Options.class);
		if (CommUtil.isNotNull(list) && list.size() > 0) {
			for (CustacList custacList : list) {
				lists.add(custacList);
			}
		}
		output.setQrCustnoList(lists);
	}
}
