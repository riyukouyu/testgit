package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.cf.IoCuTable.IoCifCustAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STATUS;


public class reopen {

	public static void sendCmqToApp( String cardno,  String redesc){
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaAcdc acdc = caqry.getKnaAcdcOdb2(cardno, false);
		IoCaKnaCust cust = caqry.getKnaCustByCustacOdb1(acdc.getCustac(), true);
//		IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(IoSrvCfPerson.class).getCifCustAccsByCustno(cust.getCustno(), E_STATUS.NORMAL, true);
		//消息推送至APP客户端
//		MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//		mri.setMtopic("Q0101005");
//		//mri.setTdcnno("R00");  //测试指定DCN
//		ToAppSendMsg toAppSendMsg = CommTools
//				.getInstance(ToAppSendMsg.class);
//		
//		// 消息内容
//		toAppSendMsg.setUserId(cplCifCustAccs.getCustid()); //用户ID
//		toAppSendMsg.setOutNoticeId("Q0101005"); //外部消息ID
//		toAppSendMsg.setNoticeTitle("您的电子账户已恢复正常使用"); //公告标题
//		toAppSendMsg.setContent("您的ThreeBank电子账户已恢复正常使用，感谢您对金谷农商银行云端金融-ThreeBank的支持！"); //公告内容
//		toAppSendMsg.setSendtime(CommTools.getBaseRunEnvs().getTrxn_date()+BusiTools.getBusiRunEnvs().getTrantm()); //消息生成时间
//		toAppSendMsg.setTransType(E_APPTTP.CUACCH); //交易类型
//		toAppSendMsg.setTirggerSys(SysUtil.getSystemId()); //触发系统
//		toAppSendMsg.setClickType(E_CLIKTP.NO);   //点击动作类型
//		//toAppSendMsg.setClickValue(clickValue); //点击动作值
//		
//		mri.setMsgtyp("ApSmsType.ToAppSendMsg");
//		mri.setMsgobj(toAppSendMsg); 
//		AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
	}
}
