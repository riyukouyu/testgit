package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccChngbrSvc;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAgrtInfos;
//import cn.sunline.ltts.busi.ltts.zjrc.infrastructure.dap.plugin.type.ECustPlugin.E_MEDIUM;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CKTNTP;

public class chngbr {

	/**
	 * 
	 * @Title: prcCkrtacBefore
	 * @Description: (变更机构前处理)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年10月27日 下午7:39:02
	 * @version V2.3.0
	 */
	public static void prcCkrtacBefore(
			final cn.sunline.ltts.busi.dptran.trans.intf.Chngbr.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Chngbr.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Chngbr.Output output) {

		// 判断输入电子账号是否为空
		if (CommUtil.isNull(input.getCardno())) {
			throw DpModuleError.DpstProd.BNAS0926();
		}
		// 判断输入新账户所属机构号是否为空
		if (CommUtil.isNull(input.getNwbrno())) {
			throw DpModuleError.DpstComm.BNAS0273();
		}

		// 根据电子账号获取电子账号ID
		IoCaKnaAcdc cplCaKnaAcdc = SysUtil.getInstance(
				IoCaSevQryTableInfo.class).getKnaAcdcByCardno(
				input.getCardno(), false);

		if (CommUtil.isNull(cplCaKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0754();
		}

		// 将环境变量中法人替换为电子账户的原法人
//		CommTools.getBaseRunEnvs().setBusi_org_id(cplCaKnaAcdc.getCorpno());
		
		// 检查电子账户是否只开过户
		E_YES___ obopfg = SysUtil.getInstance(IoAccChngbrSvc.class)
				.judgeOpenJust(cplCaKnaAcdc.getCustac());
		if (obopfg == E_YES___.NO) {
			throw DpModuleError.DpstComm.BNAS0897();
		}

		// 查询电子账户表
		IoCaKnaCust cplKnaCust = SysUtil.getInstance(
				IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(
				cplCaKnaAcdc.getCustac(), false);
		if (CommUtil.isNull(cplKnaCust)) {
			throw DpModuleError.DpstComm.BNAS0754();
		}

		// 查询电子账户的账户分类
		E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
				.qryAccatpByCustac(cplCaKnaAcdc.getCustac());

		// 查询电子账户客户信息
		/*
		IoCucifCust cplCucifCust = SysUtil.getInstance(
				IoCuSevQryTableInfo.class).cif_cust_selectOne_odb1(
				cplKnaCust.getCustno(), true);
		*/
//		IoSrvCfPerson.IoGetCifCust.InputSetter queryCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//		queryCust.setCustno(cplKnaCust.getCustno());
//		IoSrvCfPerson.IoGetCifCust.Output cplCucifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//		SysUtil.getInstance(IoSrvCfPerson.class).getCifCust(queryCust, cplCucifCust);
		

		property.setCustac(cplCaKnaAcdc.getCustac());// 电子账号
		property.setAccatp(eAccatp);// 账户分类
//		property.setIdtfno(cplCucifCust.getIdtfno());// 证件号码
//		property.setIdtftp(cplCucifCust.getIdtftp());// 证件类型
		property.setCustna(cplKnaCust.getCustna());// 客户名称
		output.setBrchno(input.getNwbrno());
	}

	/**
	 * 
	 * @Title: sedOpenInfoMessage
	 * @Description: (变更机构开户MQ通知)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年10月27日 下午7:39:43
	 * @version V2.3.0
	 */

	public static void sedOpenInfoMessage(
			final cn.sunline.ltts.busi.dptran.trans.intf.Chngbr.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Chngbr.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Chngbr.Output output) {

//		E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE; // 消息媒介

		KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("OPUPGD", "CUSTSM", "%",
				"%", true);

		String bdid = tblKnaPara.getParm_value1();// 服务绑定ID

		IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind(
				IoCaOtherService.class, bdid);

		// 1.开户成功发送开户结果到客户信息
		String mssdid = CommTools.getMySysId();// 消息ID
		String mesdna = tblKnaPara.getParm_value2();// 媒介名称

		IoCaOtherService.IoCaOpenUpgSendMsg.InputSetter openSendMsgInput = SysUtil
				.getInstance(IoCaOtherService.IoCaOpenUpgSendMsg.InputSetter.class);

//		openSendMsgInput.setMedium(mssdtp); // 消息媒介
		openSendMsgInput.setMsgid(mssdid); // 发送消息ID
		openSendMsgInput.setMdname(mesdna); // 媒介名称
		openSendMsgInput.setOpacrt(E_YES___.YES);// 是否开户成功
		openSendMsgInput.setCustid(property.getCustid());// 用户ID
		openSendMsgInput.setBrchno(input.getNwbrno());// 机构号
		openSendMsgInput.setAccatp(property.getAccatp());// 账户分类
		openSendMsgInput.setCktntp(E_CKTNTP.OPEN); //交易类型

		caOtherService.openUpgSendMsg(openSendMsgInput);

		// 2.开户成功发送协议到合约库
		KnpParameter tblKnaPara1 = KnpParameterDao.selectOne_odb1("OPUPGD", "AGRTSM",
				"%", "%", true);

		String mssdid1 = CommTools.getMySysId();// 消息ID
		String mesdna1 = tblKnaPara1.getParm_value2();// 媒介名称

		IoCaOtherService.IoCaSendContractMsg.InputSetter openSendAgrtInput = SysUtil.getInstance(IoCaOtherService.IoCaSendContractMsg.InputSetter.class);

		String sAgdata = property.getIdtftp() + "|" + property.getIdtfno()
				+ "|" + property.getCustna() + "|" + input.getCardno() + "|"
				+ property.getAccatp() + "|" + input.getNwbrno() + "|"
				+ CommTools.getBaseRunEnvs().getTrxn_date();// 协议回填字段

//		openSendAgrtInput.setMedium(mssdtp); // 消息媒介
		openSendAgrtInput.setMsgid(mssdid1); // 发送消息ID
		openSendAgrtInput.setMdname(mesdna1); // 媒介名称
		openSendAgrtInput.setUserId(property.getCustid());// 用户ID
		openSendAgrtInput.setOpenOrg(input.getNwbrno());// 机构号
		openSendAgrtInput.setAcctNo(input.getCardno());// 电子账号
		openSendAgrtInput.setAcctName(property.getCustna());// 户名
		openSendAgrtInput.setRecordCount(ConvertUtil.toInteger(1));// 记录数
		openSendAgrtInput.setOpenFlag(E_CKTNTP.OPEN);// 开户升级类型
		openSendAgrtInput.setTransTime(DateTools2.getCurrentTimestamp());// 操作时间

		IoCaAgrtInfos cplAgrtInfos = SysUtil.getInstance(IoCaAgrtInfos.class);
		cplAgrtInfos.setAgrTemplateNo(property.getAgrtno());// 协议模板编号
		cplAgrtInfos.setVersion(property.getVesion());// 版本号
		cplAgrtInfos.setAgrData(sAgdata);// 协议回填字段
		openSendAgrtInput.getAgreementList().add(cplAgrtInfos);// 协议列表

		caOtherService.sendContractMsg(openSendAgrtInput);

		// 3.开立二类户成功将理财签约相关信息发送到合约库
		if (property.getAccatp() == E_ACCATP.FINANCE) {
			KnpParameter tblKnaPara3 = KnpParameterDao.selectOne_odb1("OPENMN", "AGRTSM",
					"%", "%", true);

			String mssdid2 = CommTools.getMySysId();// 消息ID
			String mesdna2 = tblKnaPara3.getParm_value2();// 媒介名称

			IoCaOtherService.IoCaSendMonMsg.InputSetter sendMonMsgInput = SysUtil.getInstance(IoCaOtherService.IoCaSendMonMsg.InputSetter.class);

//			sendMonMsgInput.setMedium(mssdtp); // 消息媒介
			sendMonMsgInput.setMsgid(mssdid2); // 发送消息ID
			sendMonMsgInput.setMdname(mesdna2); // 媒介名称
			sendMonMsgInput.setUserId(property.getCustid());// 用户ID
			sendMonMsgInput.setAcctNo(input.getCardno());// 电子账号
			sendMonMsgInput.setAcctName(property.getCustna());// 客户姓名
			sendMonMsgInput.setCertNo(property.getIdtfno());// 证件号码
			sendMonMsgInput.setCertType(property.getIdtftp());// 证件类型
			sendMonMsgInput.setTransBranch(input.getNwbrno());// 机构编号
			sendMonMsgInput.setMobileNo(property.getAcalno());// 绑定手机号
			sendMonMsgInput.setTransTime(DateTools2.getCurrentTimestamp());//操作时间

			caOtherService.sendMonMsg(sendMonMsgInput);

		}
	}

}
