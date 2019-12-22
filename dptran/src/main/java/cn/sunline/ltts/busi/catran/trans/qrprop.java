package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;


public class qrprop {

	public static void DealTransBefore( final cn.sunline.ltts.busi.catran.trans.intf.Qrprop.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Qrprop.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Qrprop.Output output){

		//分页查询页码、页容量设置
		int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());
		int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());
		property.setPageno(pageno);
		property.setPgsize(pgsize);

	}


}
