
package cn.sunline.edsp.busi.dptran.trans.jfaccount;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_JFTRTP;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpCommAcctNormInput;
import cn.sunline.ltts.busi.dp.jfaccounting.JfDpAccountingPublic;

public class jfacth {

	public static void jfReturnsAccount(
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfacth.Input input,
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfacth.Property property,
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfacth.Output output) {

		if (CommUtil.isNull(input.getJftrtp()) || input.getJftrtp() != E_JFTRTP.jfacth) {
			throw DpModuleError.DpTrans.TS010068(); //记账交易类型不匹配
		}
		//调用通用记账服务进行记账
		DpCommAcctNormInput dpCommAcctNormInput = SysUtil.getInstance(DpCommAcctNormInput.class);
		CommUtil.copyProperties(dpCommAcctNormInput, input);
		JfDpAccountingPublic.jfCommAcctDeal(dpCommAcctNormInput);
		
		//输出信息
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrtm(CommTools.getBaseRunEnvs().getComputer_time());
	}
}
