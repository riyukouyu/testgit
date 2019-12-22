package cn.sunline.ltts.busi.dptran.trans.close;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.namedsql.AccChngbrDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbClac;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaClAcctMesgInfo;
//import cn.sunline.ltts.busi.ltts.zjrc.infrastructure.dap.plugin.type.ECustPlugin.E_MEDIUM;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;


public class clmesg {

public static void sendClMesg(){
	
}

public static void sendClMesg(String cardno){
	
	if (CommUtil.isNull(cardno)) {
		throw DpModuleError.DpstComm.BNAS0311();
	}

	// 查询发送销户信息所需字段
	IoCaClAcctMesgInfo mesgInfo = AccChngbrDao.selClosInfo(cardno, true);
	
	//查询电子账户状态
	E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(mesgInfo.getCustac());
	
	//查询电子账户分类
	E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
					.qryAccatpByCustac(mesgInfo.getCustac());
	
		if (cuacst == E_CUACST.CLOSED) {

			// 查询销户登记簿
			IoCaKnbClac cplKnbClac = SysUtil.getInstance(IoCaKnbClac.class);
			List<IoCaKnbClac> lstKnbClac = AccChngbrDao.selKnbclacByCus(
					mesgInfo.getCustac(), false);
			if (CommUtil.isNotNull(lstKnbClac) && lstKnbClac.size() != 0) {
				cplKnbClac = lstKnbClac.get(0);
			}

//			//修改销户cmq通知  modify lull
//			MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//			mri.setMtopic("Q0101004");
//			IoCaCloseAcctSendMsg closeSendMsgInput = SysUtil.getInstance(IoCaCloseAcctSendMsg.class);
//			closeSendMsgInput.setCustid(mesgInfo.getCustid()); // 用户ID
//			closeSendMsgInput.setBrchno(cplKnbClac.getClosbr());// 操作机构
//			closeSendMsgInput.setUserid(cplKnbClac.getClosus());// 操作柜员
//			closeSendMsgInput.setClossv(cplKnbClac.getClossv());// 销户渠道
//			if (cplKnbClac.getDrawwy() == E_CLSDTP.MGD) {
//				closeSendMsgInput.setClosfg(E_YES___.YES);// 是否挂失销户标志
//			} else {
//				closeSendMsgInput.setClosfg(E_YES___.NO);// 是否挂失销户标志
//			}
//
//			mri.setMsgtyp("ApSmsType.IoCaCloseAcctSendMsg");
//			mri.setMsgobj(closeSendMsgInput); 
//			AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
//			E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介

			/*KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("CLOSAC", "CUSTSM",
					"%", "%", true);

			String bdid = tblKnaPara.getParm_value1();// 服务绑定ID

			IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind(
					IoCaOtherService.class, bdid);

			// 1.销户成功发送销户结果到客户信息
			String mssdid = CommTools.getMySysId();// 消息ID
			String mesdna = tblKnaPara.getParm_value2();// 媒介名称

			IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter closeSendMsgInput = SysUtil.getInstance(IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter.class);

			closeSendMsgInput.setMsgid(mssdid); // 发送消息ID
//			closeSendMsgInput.setMedium(mssdtp); // 消息媒介
			closeSendMsgInput.setMdname(mesdna); // 媒介名称
			closeSendMsgInput.setCustid(mesgInfo.getCustid()); // 用户ID
			closeSendMsgInput.setClosbr(cplKnbClac.getClosbr());// 操作机构
			closeSendMsgInput.setClosus(cplKnbClac.getClosus());// 操作柜员
			closeSendMsgInput.setClossv(cplKnbClac.getClossv());// 销户渠道
			if (cplKnbClac.getDrawwy() == E_CLSDTP.MGD) {
				closeSendMsgInput.setClosfg(E_YES___.YES);// 是否挂失销户标志
			} else {
				closeSendMsgInput.setClosfg(E_YES___.NO);// 是否挂失销户标志
			}

			caOtherService.closeAcctSendMsg(closeSendMsgInput);

			// 2.销户成功发送销户结果到合约库
			KnpParameter tblKnaPara1 = KnpParameterDao.selectOne_odb1("CLOSAC", "AGRTSM",
					"%", "%", true);

			String mssdid1 = CommTools.getMySysId();// 消息ID

			String mesdna1 = tblKnaPara1.getParm_value2();// 媒介名称

			IoCaOtherService.IoCaClAcSendContractMsg.InputSetter closeSendAgrtInput = SysUtil.getInstance(IoCaOtherService.IoCaClAcSendContractMsg.InputSetter.class);

			closeSendAgrtInput.setMsgid(mssdid1); // 发送消息ID
//			closeSendAgrtInput.setMedium(mssdtp); // 消息媒介
			closeSendAgrtInput.setMdname(mesdna1); // 媒介名称
			closeSendAgrtInput.setUserId(mesgInfo.getCustid()); // 用户ID
			closeSendAgrtInput.setAcctType(eAccatp);// 账户分类
			closeSendAgrtInput.setOrgId(mesgInfo.getBrchno());// 归属机构
			closeSendAgrtInput.setAcctNo(cardno);// 电子账号
			closeSendAgrtInput.setAcctStat(E_CUACST.CLOSED);// 客户化状态
			closeSendAgrtInput.setAcctName(mesgInfo.getCustna());// 户名
			closeSendAgrtInput.setCertNo(mesgInfo.getIdtfno());// 证件号码
			closeSendAgrtInput.setCertType(mesgInfo.getIdtftp());// 证件类型
			closeSendAgrtInput.setTransTime(DateTools2.getCurrentTimestamp());//操作时间

			caOtherService.clAcSendContractMsg(closeSendAgrtInput);*/
		}

}
}
