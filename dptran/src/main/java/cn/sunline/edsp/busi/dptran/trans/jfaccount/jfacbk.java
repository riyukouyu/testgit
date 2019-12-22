
package cn.sunline.edsp.busi.dptran.trans.jfaccount;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountBackInput;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.jfaccounting.JfDpAccountBack;

public class jfacbk {

	public static void jfacbkAccount( 
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfacbk.Input input,  
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfacbk.Property property,  
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfacbk.Output output){
		
		//调用通用记账服务进行记账
		DpAccountBackInput dpAccountBackInput = SysUtil.getInstance(DpAccountBackInput.class);
		CommUtil.copyProperties(dpAccountBackInput, input);
		JfDpAccountBack.process(dpAccountBackInput);
		
		//输出信息
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrsq(CommTools.getBaseRunEnvs().getTrxn_seq());
		output.setMntrtm(CommTools.getBaseRunEnvs().getComputer_time());
		
	}
}
