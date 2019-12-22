
package cn.sunline.edsp.busi.dptran.trans.jfaccount;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpCommAcctNormInput;
import cn.sunline.ltts.busi.dp.jfaccounting.JfDpAccountFirst;

public class jfscsk {

	public static void firstAccount(final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfscsk.Input input,
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfscsk.Property property,
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfscsk.Output output) {
		
		//调用通用记账服务进行记账
		DpCommAcctNormInput dpAccountFirstInput = SysUtil.getInstance(DpCommAcctNormInput.class);
		CommUtil.copyProperties(dpAccountFirstInput, input);
		JfDpAccountFirst.process(dpAccountFirstInput);
		
		//输出信息
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrsq(CommTools.getBaseRunEnvs().getTrxn_seq());
		output.setMntrtm(CommTools.getBaseRunEnvs().getComputer_time());

	}
}
