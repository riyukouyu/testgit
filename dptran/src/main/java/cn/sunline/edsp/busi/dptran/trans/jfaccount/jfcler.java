
package cn.sunline.edsp.busi.dptran.trans.jfaccount;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountClearInput;
import cn.sunline.ltts.busi.dp.jfaccounting.JfDpAccountClear;

public class jfcler {

	public static void jfClerAccount( 
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfcler.Input input,  
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfcler.Property property,  
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfcler.Output output){
		
		//调用通用记账服务进行记账
		DpAccountClearInput dpAccountClearInput = SysUtil.getInstance(DpAccountClearInput.class);
		CommUtil.copyProperties(dpAccountClearInput, input);
		JfDpAccountClear.process(dpAccountClearInput);
		
		//输出信息
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrsq(CommTools.getBaseRunEnvs().getTrxn_seq());
		output.setMntrtm(CommTools.getBaseRunEnvs().getComputer_time());
		
	}
}
