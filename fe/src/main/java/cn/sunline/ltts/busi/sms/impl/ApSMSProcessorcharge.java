package cn.sunline.ltts.busi.sms.impl;

import java.util.Map;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryMesgOut;

//public class ApSMSProcessorcharge implements ApSMSProcessor {
public class ApSMSProcessorcharge  {
    private static final BizLog bizlog = BizLogUtil.getBizLog(ApSMSProcessorcharge.class);

	
	public String process(DataArea dataArea, Map<String, String> smsParm) {
		String acctno =dataArea.getInput().getString("acctno");
		String trantp = dataArea.getInput().getString("trantp");
		if(!CommUtil.equals(trantp, "1")){
			bizlog.debug("**************交易类别不是收费时，不发送短息************");
			smsParm.put("mesflg","0");
			return null;
		};
		String custac = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAccsOdb3(acctno, true).getCustac();
		String trandt = dataArea.getCommReq().getString("trandt");
		String trantm = dataArea.getCommReq().getString("trantm");
		
        IoCaSrvGenEAccountInfo eAccountInfo = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		IoCaQryMesgOut mesgOut = eAccountInfo.selMesgInfo(custac, trandt, trantm);
		String year = trandt.substring(0,4);
		String month = trandt.substring(4,6);;
		String day = trandt.substring(6,8);
		String hour = trantm.substring(0,2);
		String minute = trantm.substring(2,4);
		String second = trantm.substring(4,6);
		String date = year+"年"+month+"月"+day+"日"+hour+":"+minute+":"+second;
		smsParm.put("date", date);//交易日期
	    smsParm.put("tranam", dataArea.getInput().getString("totlam"));// 交易金额
		

		return mesgOut.getAcalno();
	}

}
