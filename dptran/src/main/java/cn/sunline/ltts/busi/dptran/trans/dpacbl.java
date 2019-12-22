package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;


public class dpacbl {
	
	/**
	 * 
	 * @Title: queryTran 
	 * @Description: 交易前处理
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangjunlei
	 * @date 2016年7月7日 上午10:02:01 
	 * @version V2.3.0
	 */

public static void queryTran( final cn.sunline.ltts.busi.dptran.trans.intf.Dpacbl.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Dpacbl.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Dpacbl.Output output){
	
	int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());//页码
	int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());//页容量
	
	property.setPageno(pageno);//页码
	property.setPagesz(pgsize);//页容量
}
}
