package cn.sunline.ltts.busi.dptran.trans.lzbank;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class pswdck {

public static void checkPassword( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Pswdck.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Pswdck.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Pswdck.Output output){
    BizLog bizLog = BizLogUtil.getBizLog(pswdck.class);
    bizLog.info("===============================交易密码验证============================");
    //String cryptoPassword=EncryTools.encryPassword(input.getPasswd(),input.getAuthif(),input.getCardno());
   // DpPassword.validatePassword(input.getCardno(), cryptoPassword,
    //        MsType.U_CHANNEL.ALL, "%");
}
}
