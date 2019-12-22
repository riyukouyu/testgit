package cn.sunline.ltts.busi.intran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CMBKTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class iavcck {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(iavcck.class);

	public static void befroeCheck( final cn.sunline.ltts.busi.intran.trans.intf.Iavcck.Input input){
		
		bizlog.debug("==========内部户转内部户复核检查开始==========");
		
		
		bizlog.debug("==========内部户转内部户维护检查开始==========");
		
		final cn.sunline.ltts.busi.intran.trans.intf.Iavcbk.Input iavcbkInput = SysUtil.getInstance(cn.sunline.ltts.busi.intran.trans.intf.Iavcbk.Input.class);
		CommUtil.copyProperties(iavcbkInput, input, false);
		
		//内部账转客户账输入检查
		iavcbk.inputCheck(iavcbkInput, E_CMBKTP.CHK);
		
	    bizlog.debug("==========内部户转内部户维护检查结束==========");
	}
}
