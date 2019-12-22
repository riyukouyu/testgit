package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;


public class hseaqu {

	/**
	 * 
	 * @Title: qryTranInfo 
	 * @Description: 交易前对页码和页容量进行处理
	 * @param input 输入接口
	 * @param property 属性接口
	 * @param output 输出接口
	 * @author zhangjunlei
	 * @date 2016年7月7日 上午9:34:55 
	 * @version V2.3.0
	 */
public static void qryTranInfo( final cn.sunline.ltts.busi.dptran.trans.intf.Hseaqu.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Hseaqu.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Hseaqu.Output output){
	
	//分页查询页码、页容量设置
	int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());//页码
	int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());//页容量
	
	property.setPageno(pageno);//页码
	property.setPagesz(pgsize);//页容量
}
}
