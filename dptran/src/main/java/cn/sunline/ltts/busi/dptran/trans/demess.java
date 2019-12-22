package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.DpMessTable.KnpPromMess;
import cn.sunline.ltts.busi.dp.tables.DpMessTable.KnpPromMessDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.LnError;

public class demess {

	public static void messageDelete(
			final cn.sunline.ltts.busi.dptran.trans.intf.Demess.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Demess.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Demess.Output output) {

		String messno = input.getMessno();
		if (CommUtil.isNull(messno)) {
			throw DpModuleError.DpstComm.E9027("提示编码");
		}
		KnpPromMess knpPromMess = KnpPromMessDao.selectOne_odb1(messno, false);
		if (CommUtil.isNull(knpPromMess)) {
			throw LnError.geno.E0000("提示标示不存在！");
		}
		KnpPromMessDao.deleteOne_odb1(messno);
	}
}
