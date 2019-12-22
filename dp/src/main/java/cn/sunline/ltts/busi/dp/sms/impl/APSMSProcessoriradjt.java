package cn.sunline.ltts.busi.dp.sms.impl;

/*
 * 电子账号利息调整
 */
public class APSMSProcessoriradjt {

//	@Override
//	public String process(DataArea dataArea, Map<String, String> smsParm) {
//		String custac =null;
//		String cardno = dataArea.getInput().getString("custac"); //由于利息调整正传入的卡号
//		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//系统时间
//		String adjttp = dataArea.getInput().getString("adjttp");
//		IoCaKnaAcdc caKnaAcdc = SysUtil.getInstance(IoCaKnaAcdc.class);
//		  caKnaAcdc = CaDao.selKnaAcdcByCard(cardno, false);
//		  custac  =caKnaAcdc.getCustac();
//		IoCaQryMesgOut mesgOut = SysUtil.getInstance(IoCaQryMesgOut.class);
//		IoCaSrvGenEAccountInfo eAccountInfo = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
//		  mesgOut = eAccountInfo.selMesgInfo(custac, dataArea.getCommReq().getString("trandt"), 
//				  dataArea.getCommReq().getString("trantm"));
//		    String year = trandt.substring(0,4);
//		    String month = mesgOut.getTramon();
//			String day = mesgOut.getTraday();
//			String hour = mesgOut.getTrahou();
//			String minute = mesgOut.getTramin();
//			String second = mesgOut.getTrasec();
//			String date = year+"年"+month+"月"+day+"日"+hour+":"+minute+":"+second;
//			if(E_ADJTTP.ADD.getValue() == adjttp){
//				smsParm.put("brchna", mesgOut.getBrchna());// 机构名
//				smsParm.put("cardno", mesgOut.getLastnm());// 尾号
//				smsParm.put("date", date);//交易日期
//				smsParm.put("irstjt", dataArea.getInput().getString("irstjt"));// 调整金额
//			}else{
//				 smsParm.put("mesflg", "0");
//			}
//		
//		
//		return mesgOut.getAcalno();//返回用户手机号
//	
//	}

}
