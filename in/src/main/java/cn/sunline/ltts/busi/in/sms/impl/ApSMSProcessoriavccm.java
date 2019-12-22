package cn.sunline.ltts.busi.in.sms.impl;

import java.util.Map;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryMesgOut;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.engine.data.DataArea;

public class ApSMSProcessoriavccm {
    private static final BizLog bizlog = BizLogUtil.getBizLog(ApSMSProcessoriavccm.class);

	public String process(DataArea dataArea, Map<String, String> smsParm) {
		String cardno =dataArea.getProperty().getString("cardno");
		String iscust = dataArea.getProperty().getString("iscust");
		if(CommUtil.equals(iscust, "0")){
			bizlog.debug("**************是否转客户帐标志为否时，不发送短息************");
			smsParm.put("mesflg","0");
			return null;
		};
		String custac = CommTools.getRemoteInstance(IoCaSevQryTableInfo.class).getKnaAcdcByCardno(cardno, true).getCustac();
		String trandt = dataArea.getCommReq().getString("trandt");
		String trantm = dataArea.getCommReq().getString("trantm");
		
        IoCaSrvGenEAccountInfo eAccountInfo = CommTools.getRemoteInstance(IoCaSrvGenEAccountInfo.class);
		IoCaQryMesgOut mesgOut = eAccountInfo.selMesgInfo(custac, trandt, trantm);
		String year = trandt.substring(0,4);
		String month = trandt.substring(4,6);;
		String day = trandt.substring(6,8);
		String hour = trantm.substring(0,2);
		String minute = trantm.substring(2,4);
		String second = trantm.substring(4,6);
		String date = year+"年"+month+"月"+day+"日"+hour+":"+minute+":"+second;
		smsParm.put("date", date);//交易日期
	    smsParm.put("tranam", dataArea.getProperty().getString("tranam"));// 交易金额
		

		return mesgOut.getAcalno();
	}

}
