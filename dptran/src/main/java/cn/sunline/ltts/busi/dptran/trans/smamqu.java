package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;


public class smamqu {

	/**
	 * 
	 * @Title: queryTran 
	 * @Description: 交易前处理
	 * @param input 输入接口
	 * @param property 属性接口
	 * @param output 输出接口
	 * @author zhangjunlei
	 * @date 2016年7月7日 上午9:59:55 
	 * @version V2.3.0
	 */
public static void queryTran( final cn.sunline.ltts.busi.dptran.trans.intf.Smamqu.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Smamqu.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Smamqu.Output output){
	
	int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());//页码
	int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());//页容量
	
	property.setPageno(pageno);//页码
	property.setPagesz(pgsize);//页容量
	//poc
	ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
	apAudit.regLogOnInsertBusiPoc(input.getCardno());

}
}
