
package cn.sunline.edsp.busi.dptran.trans.jfaccount;


import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.servicetype.MsReversalService;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsReversal;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_REVERSALTYPE;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_REVERSAL_RESULT;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_YESORNO;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountStrkeInput;
import cn.sunline.ltts.busi.dp.jfaccounting.JfDpAccountStrk;

public class jfactk {

	public static void jfactkAccount( 
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfactk.Input input,  
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfactk.Property property,  
			final cn.sunline.edsp.busi.dptran.trans.jfaccount.intf.Jfactk.Output output){
		
		//调用记账冲正服务
		DpAccountStrkeInput dpAccountStrkeInput = SysUtil.getInstance(DpAccountStrkeInput.class);
		CommUtil.copyProperties(dpAccountStrkeInput, input);
		JfDpAccountStrk.process(dpAccountStrkeInput);
		
		//输出信息
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrsq(CommTools.getBaseRunEnvs().getTrxn_seq());
		output.setMntrtm(CommTools.getBaseRunEnvs().getComputer_time());
		
	}
}
