package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;

public class qrback {

	public static void qrbackCheck(
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrback.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrback.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrback.Output output) {

		// 分页查询页码、页容量设置
		int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());
		int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());

		property.setPageno(pageno);
		property.setPgsize(pgsize);

		if (CommUtil.isNull(input.getCardno())) {
			throw DpModuleError.DpstProd.BNAS0926();
		}

	}
}
