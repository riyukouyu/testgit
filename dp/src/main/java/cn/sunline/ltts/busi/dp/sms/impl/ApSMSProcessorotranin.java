package cn.sunline.ltts.busi.dp.sms.impl;

public class ApSMSProcessorotranin {

//	@Override
//	public String process(DataArea dataArea, Map<String, String> smsParm) {
//		String cardno = dataArea.getInput().getString("incard");
//	//	String trandt = DateTools2.getCurrentTimestamp();
//	//	String trantm = dataArea.getCommRes().getString("trantm");
////		String tranam = dataArea.getInput().getString("tranam");
//		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//系统时间
//		String custac = DpAcctDao.selKnaAcdcByCardno(cardno, true).getCustac();
//		IoCaQryMesgOut mesgOut = SysUtil.getInstance(IoCaQryMesgOut.class);
//		IoCaSrvGenEAccountInfo eAccountInfo = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
//		mesgOut = eAccountInfo
//				.selMesgInfo(custac, dataArea.getCommReq().getString("trandt"),
//						dataArea.getCommReq().getString("trantm"));
//		E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
//				.qryAccatpByCustac(custac);
//		
//		String year = trandt.substring(0,4);
//		String month = mesgOut.getTramon();;
//		String day = mesgOut.getTraday();
//		String hour = mesgOut.getTrahou();
//		String minute = mesgOut.getTramin();
//		String second = mesgOut.getTrasec();
//		String date = year+"年"+month+"月"+day+"日"+hour+":"+minute+":"+second;
//		smsParm.put("brchna", mesgOut.getBrchna());// 机构名
//		smsParm.put("cardno", mesgOut.getLastnm());// 尾号
//		smsParm.put("date", date);//交易日期
//	        smsParm.put("inacna", dataArea.getInput().getString("inacna"));// 转入方户名
//	        smsParm.put("tranam", dataArea.getInput().getString("tranam"));// 交易金额
//		smsParm.put("accttype", eAccatp.getLongName());// 账户分类名称
//		smsParm.put("openbrchname", mesgOut.getBrchna());// 开户行名称
//		
//
//		return mesgOut.getAcalno();
//	}

}
