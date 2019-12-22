package cn.sunline.ltts.busi.ca.serviceimpl;

import cn.sunline.edsp.base.lang.Options;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BatchFileSubmit;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.targetList;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService.IoCaClientSysCheck.Input;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService.IoCaClientSysCheck.Output;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;

/**
 * 交易外调服务实现 交易外调服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoCaOtherServiceImpl", longname = "交易外调服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoCaOtherServiceImpl implements
cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService {
	/**
	 * 恐怖名单检查
	 * 
	 */
	public void chkTerrorAcct() {

	}

	/**
	 * 资金查控平台涉案账户可疑账户检查
	 * 
	 */
	public void chkInvolved() {

	}

	/**
	 * 统一认证系统校验密码
	 * 
	 */
	public void chkPass(
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService.IoCaChkpassword.Input input,
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService.IoCaChkpassword.Output output) {

	}

	/**
	 * 发送电子账户状态变更消息
	 * 
	 */

	/*
	public void sedAccountMessage(
			String mssdid,
			cn.sunline.ltts.busi.ltts.zjrc.infrastructure.dap.plugin.type.ECustPlugin.E_MEDIUM mssdtp,
			String mesdna, String custid,
			cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp,
			String idtfno, String cardno,
			cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP accatp,
			cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST cuacst,
			String brchno) {

	}
	 */

	/**
	 * 发送电子账户开户升级结果消息
	 * 
	 */
	public void openUpgSendMsg(
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService.IoCaOpenUpgSendMsg.Input input) {

	}

	/**
	 * 发送电子账户销户结果消息
	 * 
	 */
	public void closeAcctSendMsg(
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService.IoCaCloseAcctSendMsg.Input input) {

	}

	/**
	 * 开户升级协议发合约库
	 * 
	 */
	public void sendContractMsg(
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService.IoCaSendContractMsg.Input input) {

	}

	/**
	 * 电子账户销户发合约库
	 * 
	 */
	public void clAcSendContractMsg(
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService.IoCaClAcSendContractMsg.Input input) {

	}

	/**
	 * 电子账户绑定账户通知服务MQ
	 * 
	 */
	public void bindMqService(
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService.IoCaBindMqService.Input input) {

	}

	/**
	 * 发送理财签约相关信息到合约库
	 * 
	 */
	@Override
	public void sendMonMsg(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService.IoCaSendMonMsg.Input input) {


	}

	/*@Override
	public void callDataSysNotice(E_SYSCCD source, E_SYSCCD target,
			E_FILETP dataid, String busseq, String acctdt, String status,
			String descri, Options<BatchFileSubmit> fileList) {

	}*/
	public void callDataSysNotice( E_SYSCCD source,E_SYSCCD target,  E_FILETP dataid,  
			String busseq,  String acctdt,  String status,  String descri,  
			Options<cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BatchFileSubmit> fileList){

	}
	/**
	 * 
	 * @Title: clientSysCheck
	 * @Description: (外调客户信息系统身份核查)
	 * @author xiongzhao
	 * @date 2016年8月28日 下午3:22:00
	 * @version V2.3.0
	 */
	@Override
	public void clientSysCheck(Input input, Output output) {

	}

	@Override
	public void callDataSysSynNotice(E_SYSCCD source, String acctdt,
			E_FILETP dataid, String filenm, String filemd,
			Options<targetList> target) {
		// TODO Auto-generated method stub

	}

	@Override
	public void chkRedpack(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService.IoCaChkRedpack.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService.IoCaChkRedpack.Output output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dayEndFailNotice(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService.IoCaDayEndFailNotice.Input input) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sedAccountMessage(String mssdid, String mesdna, String custid,
			E_IDTFTP idtftp, String idtfno, String cardno, E_ACCATP accatp,
			E_CUACST cuacst, String brchno) {
		// TODO Auto-generated method stub

	}


	@Override
	public void doBatchSubmitBackSyn(E_SYSCCD source, E_SYSCCD target,
			E_FILETP dataid, String busseq, String acctdt,
			Options<BatchFileSubmit> fileList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caSetuProdSoldOutNotice(String sprdid, String sprdvr) {
		// TODO Auto-generated method stub

	}


	public void toAppSendMsg( final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService.ToAppSendMsg.Input input,  final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService.ToAppSendMsg.Output output){

	}

	@Override
	public void callDataSysNoticeToAll(E_SYSCCD arg0, E_FILETP arg1, String arg2, String arg3, String arg4,
			Options<targetList> arg5) {
		// TODO Auto-generated method stub
		
	}

}
