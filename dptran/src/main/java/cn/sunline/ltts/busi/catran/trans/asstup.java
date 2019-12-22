package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.namedsql.AccountLimitDao;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbProm;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbPromDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAgrtInfos;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PROCST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CHEKRE;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CKTNTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_IDCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_MPCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PROMST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PROMSV;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;

public class asstup {

	/**
	 * 
	 * @Title: prcIncaseUpBefore
	 * @Description: (柜面升级交易前处理)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年7月11日 下午9:35:22
	 * @version V2.3.0
	 */
	public static void prcIncaseUpBefore(
			final cn.sunline.ltts.busi.catran.trans.intf.Asstup.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Asstup.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Asstup.Output output) {
		
		// 检查输入接口必输项是否为空
		// 电子账号ID
		if (CommUtil.isNull(input.getCustac())) {
			throw DpModuleError.DpstProd.BNAS0935();
		}
		// 身份核查结果
		if (CommUtil.isNull(input.getIdckrt())) {
			throw CaError.Eacct.BNAS0368();
		}
		// 人脸识别结果
		if (CommUtil.isNull(input.getMpckrt())) {
			throw CaError.Eacct.BNAS0378();
		}
		
		// 查询升级登记簿
		KnbProm tblKnbProm = KnbPromDao.selectFirst_odb1(input.getCustac(),
				E_PROMSV.INCASE, E_PROMST.APPLY, false);
		
		// 检查查询结果是否为空
		if (CommUtil.isNull(tblKnbProm)) {
			throw CaError.Eacct.BNAS0362();
		}
		
		// 柜面只允许升级一类户
		if (tblKnbProm.getUpactp() != E_ACCATP.GLOBAL) {
			throw CaError.Eacct.BNAS1280();
		}
		
		// 根据电子账号获取电子账户分类
		E_ACCATP eAccatp = SysUtil.getInstance(
				IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(
				input.getCustac());
		
		// 根据账户分类获取开户产品号
		KnpParameter tblKnpParameter = SysUtil.getInstance(DpProdSvcType.class)
				.qryProdcd(tblKnbProm.getUpactp());

		// 查询卡客户账户对照表表
		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectFirst_odb1(input.getCustac(),
				E_DPACST.NORMAL, false);
		if (CommUtil.isNull(tblKnaAcdc)
				|| tblKnaAcdc.getStatus() != E_DPACST.NORMAL) {
			throw DpModuleError.DpstComm.BNAS0754();
		}
		
		// 查询电子账户表
		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(
				tblKnaAcdc.getCustac(), false);
		if (CommUtil.isNull(tblKnaCust)) {
			throw DpModuleError.DpstComm.BNAS0754();
		}
		
		// 查询绑定手机号
		KnaAcal tbl = CaDao.selKnaAcalByCustac(tblKnaAcdc.getCustac(), E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);
		if (CommUtil.isNotNull(tbl)) {
			property.setAcalno(tbl.getTlphno());// 电子账户绑定手机号
		}
		
		// 查询证件类型证件号码
		/*
		IoCucifCust cplCifCust = SysUtil.getInstance(
				IoCuSevQryTableInfo.class).cif_cust_selectOne_odb1(
				tblKnaCust.getCustno(), true);
		if (CommUtil.isNull(cplCifCust)) {
			throw CaError.Eacct.E0001("查询不到电子账号对应的证件信息! ");
		}*/
		
//		IoSrvCfPerson.IoGetCifCust.InputSetter queryCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//		queryCust.setCustno(tblKnaCust.getCustno());
//		IoSrvCfPerson.IoGetCifCust.Output cplCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//		SysUtil.getInstance(IoSrvCfPerson.class).getCifCust(queryCust, cplCifCust);
		
		
		// 根据电子账号查询用户ID
		/*
		IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(
				IoCuSevQryTableInfo.class).cif_cust_accsByCustno(
				tblKnaCust.getCustno(), false, E_STATUS.NORMAL);
		if (CommUtil.isNull(cplCifCustAccs)) {
			throw CaError.Eacct.E0001("查询不到电子账号关联的用户ID！");
		}
		*/

		// 查询电子账户个人结算户信息
		IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(IoDpKnaAcct.class);
		if (eAccatp == E_ACCATP.GLOBAL || eAccatp == E_ACCATP.FINANCE) {
			cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctSub(tblKnaAcdc.getCustac(),E_ACSETP.SA);
			// 将结算户负债子账号映射到属性区，提供新开负债账户后的签约处理
			property.setAcctnm(cplKnaAcct.getAcctno());// 需解约的负债账号
						
		} else if (eAccatp == E_ACCATP.WALLET) {
			cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctSub(tblKnaAcdc.getCustac(),E_ACSETP.MA);
		}
		
		// 如果输入接口核查流水不为空，映射到属性
		if (CommUtil.isNotNull(input.getCkrtsq())) {
			property.setCheksq(input.getCkrtsq());// 核查流水
		}
		property.setIdckrs(input.getIdckrt());// 升级身份核查结果
		
		// 若交易渠道为移动平板 ,检查传入的身份核查标志，人脸识别标志
		if (CommUtil.equals(CommTools.getBaseRunEnvs().getChannel_id(), "MT")) {/*
			// 检查身份核查，人脸识别是否为核查中
			if (input.getIdckrt() != E_IDCKRT.CHECKING || input.getMpckrt() != E_MPCKRT.CHECKING) {
				throw CaError.Eacct.E0001("移动平板升级传入的身份核查标志，人脸识别标志必须为核查中!");
			}
			
			// 外调客户信息身份核查
			IoCaOtherService dubboServ = SysUtil.getInstanceProxyByBind(
					IoCaOtherService.class, "otsevdb");

			IoCaOtherService.IoCaClientSysCheck.InputSetter identityCheck = CommTools
					.getInstance(IoCaOtherService.IoCaClientSysCheck.InputSetter.class);
			IoCaOtherService.IoCaClientSysCheck.Output Output = CommTools
					.getInstance(IoCaOtherService.IoCaClientSysCheck.Output.class);

			// 对外调交易接口参数赋值
			CommTools.getBaseRunEnvs().setCustid(cplCifCustAccs.getCustid());
			identityCheck.setCustid(cplCifCustAccs.getCustid());// 用户ID
			identityCheck.setCustna(tblKnaCust.getCustna());// 姓名
			identityCheck.setIdtfno(cplCifCust.getIdtfno());// 证件号码
			identityCheck.setChckfg(E_YES___.NO);// 是否检查反洗钱要素
			identityCheck.setMsgflg(E_YES___.NO);// 消息通知标志
			
			try {
				dubboServ.clientSysCheck(identityCheck, Output);
			} catch(Exception e) {
				throw CaError.Eacct.E0001("外调客户信息身份核查失败!");
			}
			
			// 更新身份核查结果以及核查流水
			tblKnbProm.setIdckrt(Output.getChkStat());// 身份核查结果
			KnbPromDao.update_odb1(tblKnbProm);
			property.setCheksq(Output.getChkSeq());// 核查流水号
			property.setIdckrs(Output.getChkStat());// 升级身份核查结果

		*/}
		//新增核查结果返回字段
		if(input.getIdckrt() == E_IDCKRT.CHECKING || input.getMpckrt() == E_MPCKRT.CHECKING){
			output.setChekre(E_CHEKRE.CHECKING);
		}
		if(input.getIdckrt() == E_IDCKRT.SUCCESS && input.getMpckrt() == E_MPCKRT.SUCCESS){
			output.setChekre(E_CHEKRE.SUCCESS);
		}
		
		//摘要描述默认设"升级渠道+升级"
		if (CommUtil.isNotNull(tblKnbProm.getPromsv())) {
			BusiTools.getBusiRunEnvs().setRemark(tblKnbProm.getPromsv().getLongName()+"升级");
		}
		// 将查询出的值映射到属性字段
//		property.setIdtfno(cplCifCust.getIdtfno());// 证件号码
//		property.setIdtftp(cplCifCust.getIdtftp());// 证件类型
//		property.setCustid(cplCifCust.getCustno());// 用户ID
		property.setCktntp(E_CKTNTP.UPGRADE);// 核查交易类型
		property.setProcst(E_PROCST.SUSPEND);// 处理状态
		property.setOdactp(eAccatp);// 原账户分类
		property.setAccatp(tblKnbProm.getUpactp());// 账户分类
		property.setCardno(tblKnaAcdc.getCardno());// 卡号
		property.setCustno(tblKnaCust.getCustno());// 客户号
		property.setCacttp(tblKnaCust.getCacttp());// 客户账户类型
		property.setCustna(tblKnaCust.getCustna());// 客户名称
		property.setBrchno(tblKnaCust.getBrchno());// 归属机构
		property.setBaprcd(tblKnpParameter.getParm_value1());// 基础负债产品
		property.setCrcycd(cplKnaAcct.getCrcycd());// 币种
		property.setCsextg(cplKnaAcct.getCsextg());// 钞汇标识
		
		//poc
		ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
        apAudit.regLogOnInsertBusiPoc(null);

	}

	/**
	 * 
	 * @Title: prcTransInform
	 * @Description: (升级消息通知)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年7月26日 下午7:55:13
	 * @version V2.3.0
	 */
	public static void prcTransInform(
			final cn.sunline.ltts.busi.catran.trans.intf.Asstup.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Asstup.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Asstup.Output output) {


		KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("OPUPGD", "CUSTSM",
				"%", "%", true);
		
		String bdid = tblKnaPara.getParm_value1();// 服务绑定ID
		
		IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind(
				IoCaOtherService.class, bdid);

		// 1.升级成功发送升级结果到客户信息
		String mssdid = CommTools.getMySysId();// 消息ID
		String mesdna = tblKnaPara.getParm_value2();// 媒介名称

		IoCaOtherService.IoCaOpenUpgSendMsg.InputSetter upgdSendMsgInput = SysUtil
				.getInstance(IoCaOtherService.IoCaOpenUpgSendMsg.InputSetter.class);
		
		upgdSendMsgInput.setMsgid(mssdid); // 发送消息ID
		upgdSendMsgInput.setMdname(mesdna); // 媒介名称
		upgdSendMsgInput.setOpacrt(E_YES___.YES);// 是否开户成功
		upgdSendMsgInput.setCustid(property.getCustid());// 用户ID
		upgdSendMsgInput.setBrchno(property.getBrchno());// 机构号
		upgdSendMsgInput.setAccatp(property.getAccatp());// 账户分类
		upgdSendMsgInput.setCktntp(E_CKTNTP.UPGRADE);// 交易类型

		caOtherService.openUpgSendMsg(upgdSendMsgInput);

		// 2.升级成功发送协议到合约库
		KnpParameter tblKnaPara1 = KnpParameterDao.selectOne_odb1("OPUPGD", "AGRTSM",
				"%", "%", true);

		String mssdid1 = CommTools.getMySysId();// 消息ID
		String mesdna1 = tblKnaPara1.getParm_value2();// 媒介名称

		IoCaOtherService.IoCaSendContractMsg.InputSetter upgdSendAgrtInput = SysUtil
				.getInstance(IoCaOtherService.IoCaSendContractMsg.InputSetter.class);
		
		String sAgdata = property.getIdtftp() + "|" + property.getIdtfno() + "|"
				+ property.getCustna() + "|" + property.getCardno() + "|"
				+ property.getAccatp() + "|" + property.getBrchno() + "|"
				+ CommTools.getBaseRunEnvs().getTrxn_date();// 协议回填字段
		
		upgdSendAgrtInput.setMsgid(mssdid1); // 发送消息ID
		upgdSendAgrtInput.setMdname(mesdna1); // 媒介名称
		upgdSendAgrtInput.setUserId(property.getCustid());// 用户ID
		upgdSendAgrtInput.setOpenOrg(property.getBrchno());// 机构号
		upgdSendAgrtInput.setAcctNo(property.getCardno());// 电子账号
		upgdSendAgrtInput.setAcctName(property.getCustna());// 户名
		upgdSendAgrtInput.setRecordCount(ConvertUtil.toInteger(1));// 记录数
		upgdSendAgrtInput.setOpenFlag(E_CKTNTP.UPGRADE);// 开户升级标志
		upgdSendAgrtInput.setTransTime(DateTools2.getCurrentTimestamp());// 操作时间
		
		IoCaAgrtInfos cplAgrtInfos = SysUtil.getInstance(IoCaAgrtInfos.class);
		cplAgrtInfos.setAgrTemplateNo(null);// 协议模板编号
		cplAgrtInfos.setVersion(null);// 版本号
		cplAgrtInfos.setAgrData(sAgdata);// 协议回填字段
		upgdSendAgrtInput.getAgreementList().add(cplAgrtInfos);// 协议列表

		caOtherService.sendContractMsg(upgdSendAgrtInput);
		
		// 3.升级二类户成功将理财签约相关信息发送到合约库
		if (property.getAccatp() == E_ACCATP.FINANCE) {
			KnpParameter tblKnaPara3 = KnpParameterDao.selectOne_odb1("OPENMN", "AGRTSM",
					"%", "%", true);

			String mssdid2 = CommTools.getMySysId();// 消息ID
			String mesdna2 = tblKnaPara3.getParm_value2();// 媒介名称

			IoCaOtherService.IoCaSendMonMsg.InputSetter sendMonMsgInput = SysUtil
					.getInstance(IoCaOtherService.IoCaSendMonMsg.InputSetter.class);

			sendMonMsgInput.setMsgid(mssdid2); // 发送消息ID
			sendMonMsgInput.setMdname(mesdna2); // 媒介名称
			sendMonMsgInput.setUserId(property.getCustid());// 用户ID
			sendMonMsgInput.setAcctNo(property.getCardno());// 电子账号
			sendMonMsgInput.setAcctName(property.getCustna());// 客户姓名
			sendMonMsgInput.setCertNo(property.getIdtfno());// 证件号码
			sendMonMsgInput.setCertType(property.getIdtftp());// 证件类型
			sendMonMsgInput.setTransBranch(property.getBrchno());// 机构编号
			sendMonMsgInput.setMobileNo(property.getAcalno());// 绑定手机号
			sendMonMsgInput.setTransTime(DateTools2.getCurrentTimestamp());//操作时间

			caOtherService.sendMonMsg(sendMonMsgInput);
		}
	}

	/**
	 * 
	 * @Title: prcCancelContract
	 * @Description: (二类户升级为一类户更新原签约信息)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年9月12日 上午10:37:49
	 * @version V2.3.0
	 */
	public static void prcCancelContract( final cn.sunline.ltts.busi.catran.trans.intf.Asstup.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Asstup.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Asstup.Output output){
		
		// 将签约信息更新为一类户的负债账户签约
		
		String timetm =DateTools2.getCurrentTimestamp();
		
		CaDao.updKnaSignDetlinfo(property.getAcctno(), property.getBaprcd(),
				property.getAcctnm(), E_SIGNST.QY,timetm);
		
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._01);
	}

	/**
	 * 
	 * @Title: updTrdStatus
	 * @Description: (更新电子账户状态)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年9月24日 上午11:19:48
	 * @version V2.3.0
	 */
	public static void updTrdStatus(
			final cn.sunline.ltts.busi.catran.trans.intf.Asstup.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Asstup.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Asstup.Output output) {

		// 登记电子账户状态
		IoCaUpdAcctstIn cplDimeInfo = SysUtil
				.getInstance(IoCaUpdAcctstIn.class);
		cplDimeInfo.setCustac(input.getCustac());
//		cplDimeInfo.setDime01(property.getOdactp().getValue());
		cplDimeInfo.setFacesg(input.getFacesg());// 面签标识
		SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(
				cplDimeInfo);

	}

	/**
	 * @Title: updTrdStatus
	 * @Description: (升级成功后清除账户原交易累计金额)
	 * @param input
	 * @param property
	 * @param output
	 * @author chengen
	 */
	
	public static void clearTsscTram( final cn.sunline.ltts.busi.catran.trans.intf.Asstup.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Asstup.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Asstup.Output output){
			AccountLimitDao.updTsscAcct(input.getCustac());
			
	}

}
