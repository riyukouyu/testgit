package cn.sunline.ltts.busi.dp.sms.impl;

import java.util.Map;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryMesgOut;
import cn.sunline.adp.cedar.base.engine.data.DataArea;

public class APSMSProcessordisabl  {
	 public String process(DataArea dataArea, Map<String, String> smsParm) {
		    String custac =null;
			String cardno = dataArea.getInput().getString("cardno");
			IoCaKnaAcdc caKnaAcdc = SysUtil.getInstance(IoCaKnaAcdc.class);
			  caKnaAcdc = CaDao.selKnaAcdcByCard(cardno, false);
			  custac  =caKnaAcdc.getCustac();
	        IoCaQryMesgOut mesgOut = SysUtil.getInstance(IoCaQryMesgOut.class);
	        IoCaSrvGenEAccountInfo eAccountInfo = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
	        mesgOut = eAccountInfo.selMesgInfo(custac, dataArea.getCommReq().getString("trandt"), dataArea.getCommReq().getString("trantm"));

	        smsParm.put("brchna", mesgOut.getBrchna());// 机构名
	        smsParm.put("cardno", mesgOut.getLastnm());// 尾号

	        return mesgOut.getAcalno();
	    }
}
