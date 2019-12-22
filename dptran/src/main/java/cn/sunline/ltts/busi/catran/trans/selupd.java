package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;


public class selupd {

public static void qryModInfo( final cn.sunline.ltts.busi.catran.trans.qt.intf.Selupd.Input input,  final cn.sunline.ltts.busi.catran.trans.qt.intf.Selupd.Property property,  final cn.sunline.ltts.busi.catran.trans.qt.intf.Selupd.Output output){
	int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());
	int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());
	property.setPageno(pageno);
	property.setPagesize(pgsize);
}
}
