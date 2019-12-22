package cn.sunline.ltts.busi.dp.sms.impl;

public class ApSMSProcessorstunpy {
//	 private static final BizLog bizlog = BizLogUtil.getBizLog(ApSMSProcessoropaccd.class);
//	@Override
//	public String process(DataArea dataArea, Map<String, String> smsParm) {
//		String custac = null;
//		String cardno = dataArea.getInput().getString("cardno");
//		IoCaKnaAcdc knaadcd = DpAcctDao.selKnaAcdcByCardno(cardno,true);
//		custac = knaadcd.getCustac();
//		IoCaQryMesgOut mesgOut = SysUtil.getInstance(IoCaQryMesgOut.class);
//        IoCaSrvGenEAccountInfo eAccountInfo = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
//		mesgOut = eAccountInfo.selMesgInfo(custac, dataArea.getCommReq().getString("trandt"), dataArea.getCommReq().getString("trantm"));
//		//如果为客户止付才发送短信 ，即账户保护，银行止付和外部止付不发送短信。
//		if(dataArea.getInput().getString("stoptp") == E_STOPTP.CUSTSTOPAY.getValue()){
//			smsParm.put("brchna",mesgOut.getBrchna());
//			smsParm.put("cardno",mesgOut.getLastnm());
//		}else{
//			//借用预开户不发短信特殊处理
//			 bizlog.debug("****************开户人脸识别或身份识别失败,预开户不发送短信****************");//使错误明显
//			 smsParm.put("mesflg", "0");
//		}
//		
//		return mesgOut.getAcalno();
//	}

}
