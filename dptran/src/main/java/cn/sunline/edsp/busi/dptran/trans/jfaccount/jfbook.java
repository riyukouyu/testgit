
package cn.sunline.edsp.busi.dptran.trans.jfaccount;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpCommAcctNormInput;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.jfaccounting.JfDpAccountingPublic;

public class jfbook {
	public static void jfbookAccount( 
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfbook.Input input,  
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfbook.Property property,  
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfbook.Output output){
		
		//调用通用记账服务进行记账
		DpCommAcctNormInput dpCommAcctNormInput = SysUtil.getInstance(DpCommAcctNormInput.class);
		CommUtil.copyProperties(dpCommAcctNormInput, input); //输入字段赋值
		JfDpAccountingPublic.jfCommAcctDeal(dpCommAcctNormInput);
		
		//输出信息
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrtm(CommTools.getBaseRunEnvs().getComputer_time());
	
	}
}
