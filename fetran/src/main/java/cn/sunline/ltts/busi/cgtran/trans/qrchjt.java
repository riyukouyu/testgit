package cn.sunline.ltts.busi.cgtran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;

public class qrchjt {

	public static void qryTranInfo(
			final cn.sunline.ltts.busi.cgtran.trans.intf.Qrchjt.Input input,
			final cn.sunline.ltts.busi.cgtran.trans.intf.Qrchjt.Property property,
			final cn.sunline.ltts.busi.cgtran.trans.intf.Qrchjt.Output output) {

		// 分页查询页码、页容量设置
		int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());// 页码
		int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());// 页容量

		property.setPageno(pageno);// 页码
		property.setPagesz(pgsize);// 页容量
	}
}
