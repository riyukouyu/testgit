package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;


public class opense {

	/**
	 * 
	 * @Title: qryTranInfo 
	 * @Description: 交易前对页码和页容量处理
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangjunlei
	 * @date 2016年7月7日 上午10:38:44 
	 * @version V2.3.0
	 */
public static void qryTranInfo( final cn.sunline.ltts.busi.catran.trans.intf.Opense.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Opense.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Opense.Output output){
	
	//分页查询页码、页容量设置
	int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());//页码
	int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());//页容量

	property.setPageno(pageno);//页码
	property.setPageSize(pgsize);//页容量
}
}
