
package cn.sunline.edsp.busi.dptran.trans;


import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.busi.dp.iobus.type.dp.DpdebitAcctnos.AcctnoList;
import cn.sunline.edsp.busi.dp.namedsql.soc.SocQyAcctsDao;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * 根据电子账号查询子账户详情
 * @author sunline16
 *
 */
public class qyaccs {

	public static void qyAccts(final cn.sunline.edsp.busi.dptran.trans.intf.Qyaccs.Input input,
			final cn.sunline.edsp.busi.dptran.trans.intf.Qyaccs.Property property,
			final cn.sunline.edsp.busi.dptran.trans.intf.Qyaccs.Output output) {
		String custac = input.getCustac();
		Options<AcctnoList> options = SysUtil.getInstance(Options.class);
		List<AcctnoList> list = SocQyAcctsDao.selAcctnosByCustno(custac, false);
		String crcycd = BusiTools.getDefineCurrency();
		if (CommUtil.isNotNull(list) && list.size() > 0) {
			for (AcctnoList custacList : list) {
				String acctno = custacList.getAcctno();
				//计算每个子账户的余额和可用余额
				BigDecimal bal = SysUtil.getInstance(DpAcctSvcType.class).getAcctaAvaBal(custac, acctno, crcycd, E_YES___.YES,E_YES___.NO);
				custacList.setCanusa(bal);
				options.add(custacList);
			}
		}
		output.setQrAcctnoList(options);
	}
}
