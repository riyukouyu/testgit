package cn.sunline.ltts.busi.dp.sms.impl;

public class APSMSProcessordeduct {
//	  public String process(DataArea dataArea, Map<String, String> smsParm) {
//	String custac =null;
//	String cardno = dataArea.getInput().getString("cardno");
//	String deduam = dataArea.getInput().getString("deduam");
//	String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//系统时间
//	IoCaKnaAcdc caKnaAcdc = SysUtil.getInstance(IoCaKnaAcdc.class);
//	  caKnaAcdc = CaDao.selKnaAcdcByCard(cardno, false);
//	  custac  =caKnaAcdc.getCustac();
//    IoCaQryMesgOut mesgOut = SysUtil.getInstance(IoCaQryMesgOut.class);
//    IoCaSrvGenEAccountInfo eAccountInfo = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
//    mesgOut = eAccountInfo.selMesgInfo(custac, dataArea.getCommReq().getString("trandt"), dataArea.getCommReq().getString("trantm"));
//    
//    smsParm.put("deduam", deduam);// 扣划金额
//    smsParm.put("brchna", mesgOut.getBrchna());// 机构名
//    smsParm.put("cardno", mesgOut.getLastnm());// 尾号
//    smsParm.put("tramon", trandt.substring(0,4)+"年"+
//    		              mesgOut.getTramon());// 月
//    smsParm.put("traday", mesgOut.getTraday());// 日
//    smsParm.put("trahou", mesgOut.getTrahou());// 时
//    smsParm.put("tramin", mesgOut.getTramin());// 分
//    smsParm.put("trasec", mesgOut.getTrasec());// 秒
//    
//    
//
//    return mesgOut.getAcalno();
//	  }
}
