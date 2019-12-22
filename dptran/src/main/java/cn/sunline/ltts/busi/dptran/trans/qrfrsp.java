package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;


public class qrfrsp {

public static void prcQryFzInfoBefore( final cn.sunline.ltts.busi.dptran.trans.intf.Qrfrsp.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrfrsp.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrfrsp.Output output){
	//分页查询页码、页容量设置
	int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());
	int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());
	property.setPageno(pageno);
	property.setPgsize(pgsize);
}
}
