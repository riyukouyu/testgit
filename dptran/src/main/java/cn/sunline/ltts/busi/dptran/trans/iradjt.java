package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.cf.IoCuTable.IoCifCustAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_ACLMFG;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_REBKTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RECPAY;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RISKLV;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STATUS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_ADJTTP;


public class iradjt {


public static void befTran( final cn.sunline.ltts.busi.dptran.trans.intf.Iradjt.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Iradjt.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Iradjt.Output output){
	property.setAclmfg(E_ACLMFG._3);
	property.setAuthtp("02");
	property.setAcctrt(E_ACCTROUTTYPE.PERSON);
	property.setRebktp(E_REBKTP._99);
	property.setRisklv(E_RISKLV._01);
	property.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
	property.setSbactp(E_SBACTP._11);
	property.setAccttp(E_ACCATP.GLOBAL);
	property.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
	property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 业务跟踪编号
	property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 交易日期
	
	if(CommUtil.equals(input.getAdjttp().toString(), E_ADJTTP.DEL.toString())){//调减
		property.setLimttp(E_LIMTTP.TR);
		property.setDcflag1(E_RECPAY.PAY);
	}else{
		property.setLimttp(E_LIMTTP.TI);
		property.setDcflag1(E_RECPAY.REC);
	}
}

	public static void sendCmqToApp( final cn.sunline.ltts.busi.dptran.trans.intf.Iradjt.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Iradjt.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Iradjt.Output output){
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String trantm = BusiTools.getBusiRunEnvs().getTrantm();
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaAcdc acdc = caqry.getKnaAcdcOdb2(input.getAdacno(), false);
		IoCaKnaCust cust = caqry.getKnaCustByCustacOdb1(acdc.getCustac(), true);
//		IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(IoSrvCfPerson.class).getCifCustAccsByCustno(cust.getCustno(), E_STATUS.NORMAL, true);
		String adjttp=input.getAdjttp().toString();
		//判断利息调增时发送消息
		if(CommUtil.equals(E_ADJTTP.ADD.toString(),adjttp)){
		//消息推送至APP客户端
//		MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//		mri.setMtopic("Q0101005");
//		//mri.setTdcnno("R00");  //测试指定DCN
//		ToAppSendMsg toAppSendMsg = SysUtil.getInstance(ToAppSendMsg.class);
//		
//		// 消息内容
//		toAppSendMsg.setUserId(cplCifCustAccs.getCustid()); //用户ID
//		toAppSendMsg.setOutNoticeId("Q0101005"); //外部消息ID
//		
//		
//		toAppSendMsg.setNoticeTitle("资金变动"); //公告标题
//		StringBuffer sb=new StringBuffer();
//		String date  = CommTools.getBaseRunEnvs().getTrxn_date().substring(0,4)+"年"
//                +CommTools.getBaseRunEnvs().getTrxn_date().substring(4,6)+"月"+ 
//                        CommTools.getBaseRunEnvs().getTrxn_date().substring(6,8)+"日"+
//                        BusiTools.getBusiRunEnvs().getTrantm().substring(0,2)+":"+
//                        BusiTools.getBusiRunEnvs().getTrantm().substring(2,4)+":"+
//                        BusiTools.getBusiRunEnvs().getTrantm().substring(4,6);
//		sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易时间：").append(date).
//		append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易类型：").append("付息").
//		append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易金额：").append(input.getIrstjt()).
//		append("元，请点击查看详情。");
//		/*toAppSendMsg.setContent("交易时间："+(DateTools2.getMonth(DateTools2.covStringToDate(trandt))+1)+
//				"月"+DateTools2.getDay(DateTools2.covStringToDate(trandt))+"日 ，"
//				+trantm.substring(0, 2)+ ":"+trantm.substring(2, 4)
//				+"交易类型：付息，交易账户："+input.getAdacno().substring(input.getAdacno().length() - 4, input.getAdacno().length())+"交易金额："+input.getIrstjt()+"元，请点击查看详情。"); 
//
//				*/		
//		toAppSendMsg.setContent(sb.toString());
//		toAppSendMsg.setSendtime(CommTools.getBaseRunEnvs().getTrxn_date()+BusiTools.getBusiRunEnvs().getTrantm()); //消息生成时间
//		toAppSendMsg.setTransType(E_APPTTP.PAYINTER); //交易类型
//		toAppSendMsg.setTirggerSys(SysUtil.getSystemId()); //触发系统
//		toAppSendMsg.setClickType(E_CLIKTP.YES);   //点击动作类型
//		toAppSendMsg.setClickValue("LOGINURL||/page/electronicAcct/bill/electAcctBill.html"); //点击动作值
//		
//		mri.setMsgtyp("ApSmsType.ToAppSendMsg");
//		mri.setMsgobj(toAppSendMsg); 
//		AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
		}
	}
}
