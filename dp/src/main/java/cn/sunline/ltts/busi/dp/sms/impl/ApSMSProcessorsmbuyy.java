package cn.sunline.ltts.busi.dp.sms.impl;

public class ApSMSProcessorsmbuyy {

//	@Override
//	public String process(DataArea dataArea, Map<String, String> smsParm) {
//		String cardno = dataArea.getProperty().getString("cardno");
//		KnaAcdc knaacdc = KnaAcdcDao.selectOne_odb2(cardno, false);
//		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
//		String trantm = BusiTools.getBusiRunEnvs().getTrantm();
//		String tranam = dataArea.getInput().getString("tranam");
//		String custac = knaacdc.getCustac();
//		IoCaQryMesgOut mesgOut = SysUtil.getInstance(IoCaQryMesgOut.class);
//        IoCaSrvGenEAccountInfo eAccountInfo = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
//		mesgOut = eAccountInfo.selMesgInfo(custac, dataArea.getCommReq().getString("trandt"), dataArea.getCommReq().getString("trantm"));
//		
//		String year = trandt.substring(0, 4);
//		String month = trandt.substring(4, 6);
//		String day = trandt.substring(6, 8);
//		String hour = trantm.substring(0,2);
//		String minute = trantm.substring(2,4);
//		String second = trantm.substring(4,6);
//		String date = year+"年"+month+"月"+day+"日"+hour+":"+minute+":"+second;
//		
//        smsParm.put("brchna", mesgOut.getBrchna());// 机构名
//        smsParm.put("cardno", cardno);// 尾号
//        smsParm.put("date", date);//交易日期
//        smsParm.put("tranam", tranam);//交易金额
//        return mesgOut.getAcalno();
//	}

}
