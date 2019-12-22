package cn.sunline.ltts.busi.cgtran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;


public class qrdist {

public static void doTransBefore( final cn.sunline.ltts.busi.cgtran.trans.intf.Qrdist.Input input,  final cn.sunline.ltts.busi.cgtran.trans.intf.Qrdist.Property property,  final cn.sunline.ltts.busi.cgtran.trans.intf.Qrdist.Output output){
	//分页查询页码、页容量设置
	int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());
	int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());
	property.setPageno(pageno);
	property.setPagesize(pgsize);
	
}
}
