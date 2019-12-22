package cn.sunline.ltts.busi.dp.sms.impl;

public class ApSMSProcessoropaccd  {
//    private static final BizLog bizlog = BizLogUtil.getBizLog(ApSMSProcessoropaccd.class);
//    @Override
//    public String process(DataArea dataArea, Map<String, String> smsParm) {
//        String custac = dataArea.getOutput().getString("custac");
//        IoCaQryMesgOut mesgOut = SysUtil.getInstance(IoCaQryMesgOut.class);
//        IoCaSrvGenEAccountInfo eAccountInfo = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
//        mesgOut = eAccountInfo.selMesgInfo(custac, dataArea.getCommReq().getString("trandt"), dataArea.getCommReq().getString("trantm"));
//        E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac);
//
//        if(CommUtil.isNull(eAccatp)){
//            bizlog.debug("****************开户人脸识别或身份识别失败,预开户不发送短信****************");//使错误明显
//            smsParm.put("mesflg", "0");
//        }else {
//        	smsParm.put("brchna", mesgOut.getBrchna());// 机构名
//            smsParm.put("cardno", mesgOut.getLastnm());// 尾号
//            smsParm.put("accttype", eAccatp.getLongName());// 账户分类名称,如果开户人脸识别或身份识别失败,会导致此处空指针
//            smsParm.put("openbrchname", mesgOut.getBrchna());// 开户行名称
//		}
//
//        return mesgOut.getAcalno();
//    }

}
