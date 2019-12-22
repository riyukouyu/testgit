package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;


public class closqr {

public static void prcQryInfoList( final cn.sunline.ltts.busi.catran.trans.intf.Closqr.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Closqr.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Closqr.Output output){
	//分页查询页码、页容量设置
		int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());
		int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());
		property.setPageno(pageno);
		property.setPagesize(pgsize);
}
}
