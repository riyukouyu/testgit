package cn.sunline.ltts.busi.dp.sms.impl;

/*
 * 电子账号转出
 */
public class APSMSProcessortranot {

//	@Override
//	public String process(DataArea dataArea, Map<String, String> smsParm) {
//		String cardno = dataArea.getInput().getString("otcard"); //转出方账号
//		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//系统时间
//			String custac = DpAcctDao.selKnaAcdcByCardno(cardno, true).getCustac();
//			IoCaQryMesgOut mesgOut = SysUtil.getInstance(IoCaQryMesgOut.class);
//			IoCaSrvGenEAccountInfo eAccountInfo = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
//			mesgOut = eAccountInfo
//					.selMesgInfo(custac, dataArea.getCommReq().getString("trandt"),
//							dataArea.getCommReq().getString("trantm"));
//            String year = trandt.substring(0,4);
//			String month = mesgOut.getTramon();;
//			String day = mesgOut.getTraday();
//			String hour = mesgOut.getTrahou();
//			String minute = mesgOut.getTramin();
//			String second = mesgOut.getTrasec();
//			String date = year+"年"+ month+"月"+day+"日"+hour+":"+minute+":"+second;
//			smsParm.put("brchna", mesgOut.getBrchna());// 机构名
//			smsParm.put("cardno", mesgOut.getLastnm());// 转出方账户尾号
//			smsParm.put("inacna", dataArea.getInput().getString("inacna"));// 转入方户名
//			smsParm.put("date", date);//交易日期
//		    smsParm.put("tranam", dataArea.getInput().getString("tranam"));// 交易金额
//		    smsParm.put("tlcgam", dataArea.getInput().getString("tlcgam"));// 手续费金额
//			smsParm.put("openbrchname", mesgOut.getBrchna());// 开户行名称
//		return mesgOut.getAcalno();
//	}

}
