package cn.sunline.ltts.busi.dp.sms.impl;

/*
 * 电子账户密码重置
 */
public class APSMSProcessorrepswd {

//	@Override
//	public String process(DataArea dataArea, Map<String, String> smsParm) {
//		String custac =null;
//		String cardno = dataArea.getInput().getString("custac"); //传入的是电子账号
//		IoCaKnaAcdc caKnaAcdc = SysUtil.getInstance(IoCaKnaAcdc.class);
//		  caKnaAcdc = CaDao.selKnaAcdcByCard(cardno, false);
//		  custac  =caKnaAcdc.getCustac();
//		IoCaQryMesgOut mesgOut = SysUtil.getInstance(IoCaQryMesgOut.class);
//		IoCaSrvGenEAccountInfo eAccountInfo = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
//		  mesgOut = eAccountInfo.selMesgInfo(custac, dataArea.getCommReq().getString("trandt"), 
//				  dataArea.getCommReq().getString("trantm"));
//		    smsParm.put("brchna", mesgOut.getBrchna());// 机构名
//	        smsParm.put("cardno", mesgOut.getLastnm());// 尾号
//		return mesgOut.getAcalno();//返回用户手机号;
//	}

}
