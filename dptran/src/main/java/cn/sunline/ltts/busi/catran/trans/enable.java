package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAgrtInfos;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.ApSmsType.DpBindMqService;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BACATP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BDCART;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BINDTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CHEKRE;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CKTNTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_IDCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_MPCKRT;

public class enable {
	/**
	 * 
	 * @author xiongzhao
	 *         <p>
	 *         <li>2016年7月6日-下午5:33:41</li>
	 *         <li>功能描述：电子账户启用</li>
	 *         </p>
	 * 
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void prcAcEnable(
			final cn.sunline.ltts.busi.catran.trans.intf.Enable.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Enable.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Enable.Output output) {

		E_BACATP eCardtp = input.getCardtp();// 绑定账户类型
		String sCdopac = input.getCdopac();// 绑定账户
		String sCdopna = input.getCdopna();// 绑定账户名称
		String sCustac = input.getCustac();// 电子账号ID
		String sCustna = input.getCustna();// 账户名称
		E_IDCKRT eIdckrt = input.getIdckrt();// 身份核查结果
		E_YES___ eIsbkca = input.getIsbkca();// 是否本行标志
		E_MPCKRT eMpckrt = input.getMpckrt();// 人脸识别结果
		String sTeleno = input.getTeleno();// 绑定手机号
		String sOpbrch = input.getOpbrch();// 账户开户行号
		String sBrchna = input.getBrchna();// 账户开户行名称
		E_BDCART eBdcart = input.getBdcart();// 绑卡认证结果
		E_YES___ eFacesg = input.getFacesg();// 面签标志
		
		// 将面签标志先放到属性中
		property.setFacelg(eFacesg);
		
		// 检查输入接口必输项是否为空
		if (CommUtil.isNull(sCustac)) {
			throw DpModuleError.DpstProd.BNAS0935();
		}
		if (CommUtil.isNull(sCustna)) {
			throw CaError.Eacct.BNAS0173();
		}
		if (CommUtil.isNull(eIdckrt)) {
			throw CaError.Eacct.BNAS0368();
		}
		if (CommUtil.isNull(eMpckrt)) {
			throw CaError.Eacct.BNAS0378();
		}
		if (CommUtil.isNull(sTeleno)) {
			throw CaError.Eacct.BNAS1110();
		}

		// 检查手机号长度和是否为全为数字
		if (sTeleno.length() != 11) {
			throw CaError.Eacct.BNAS0397();
		}

		if (!BusiTools.isNum(sTeleno)) {
			throw CaError.Eacct.BNAS0319();
		}

		// 查询电子账户账户分类
		E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
				.qryAccatpByCustac(sCustac);
		
		if (eAccatp == E_ACCATP.FINANCE || eAccatp == E_ACCATP.WALLET) {
			if (CommUtil.isNull(sOpbrch)) {
				throw CaError.Eacct.BNAS0179();
			}
			if (CommUtil.isNull(sBrchna)) {
				throw CaError.Eacct.BNAS0178();
			}
			if (CommUtil.isNull(eCardtp)) {
				throw CaError.Eacct.BNAS1108();
			}
			if (CommUtil.isNull(sCdopac)) {
				throw CaError.Eacct.BNAS1109();
			}
			if (CommUtil.isNull(sCdopna)) {
				throw CaError.Eacct.BNAS1107();
			}
			if (CommUtil.isNull(eIsbkca)) {
				throw CaError.Eacct.BNAS0354();
			}
		}
			
		// 检查统一后管交易人脸识别标志是否控制为成功
		if (CommUtil.equals(CommTools.getBaseRunEnvs().getChannel_id(), "EB")) {
			if (eMpckrt != E_MPCKRT.SUCCESS) {
				throw CaError.Eacct.BNAS1234();
			}
		}
		
		// 根据电子账号ID查询出未启用的电子账户信息
		KnaCust tblKnaCust = CaDao.selKnaCustByCustac(sCustac, false);
		if (CommUtil.isNull(tblKnaCust)) {
			throw CaError.Eacct.BNAS0989();
		}
		
		// 若启用机构法人和开户机构法人不一致，则不打面签标志
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getBusi_org_id(), tblKnaCust.getCorpno())) {
			property.setFacelg(E_YES___.NO);
		}
		
//		CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaCust.getCorpno());
		
		// 查询电子账号
		KnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByAcStutas(sCustac, E_DPACST.NORMAL, false);
		if (CommUtil.isNull(tblKnaAcdc)) {
			throw CaError.Eacct.BNAS0989();
		}

		// 查询出用户ID
		/*
		IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(
				IoCuSevQryTableInfo.class).cif_cust_accsByCustno(
				tblKnaCust.getCustno(), true, E_STATUS.NORMAL);
		*/
		// 查询证件类型证件号码
		/*
		IoCucifCust cplCifCust = SysUtil.getInstance(
				IoCuSevQryTableInfo.class).cif_cust_selectOne_odb1(
				tblKnaCust.getCustno(), true);
		*/
//		IoSrvCfPerson.IoGetCifCust.InputSetter queryCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//		queryCust.setCustno(tblKnaCust.getCustno());
//		//queryCust.setIdtfno(input.);
//		IoSrvCfPerson.IoGetCifCust.Output cplCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//		SysUtil.getInstance(IoSrvCfPerson.class).getCifCust(queryCust, cplCifCust);
		
		
		// 检查是否为未启用
		E_CUACST eCustac = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
				.selCaStInfo(sCustac);
		if (eCustac != E_CUACST.NOENABLE) {
			throw CaError.Eacct.BNAS0751();
		}

		// 检查传入户名和待启用电子账户户名是否一致
		if (!CommUtil.equals(sCustna, tblKnaCust.getCustna())) {
			throw CaError.Eacct.BNAS1043();
		}

		// 检查传入的身份核查结果和人脸识别结果
		if (eIdckrt == E_IDCKRT.FAILD || eMpckrt == E_MPCKRT.FAILD || eBdcart == E_BDCART.FAILD) {
			throw CaError.Eacct.BNAS0681();
		}
		if (eIdckrt == E_IDCKRT.CHECKING || eMpckrt == E_MPCKRT.CHECKING || eBdcart == E_BDCART.CHECKING) {
			tblKnaCust.setIdckrt(eIdckrt);
			tblKnaCust.setMpckrt(eMpckrt);
			KnaCustDao.updateOne_odb1(tblKnaCust);
			output.setChekre(E_CHEKRE.CHECKING);
		}
		if (eIdckrt == E_IDCKRT.SUCCESS && eMpckrt == E_MPCKRT.SUCCESS && eBdcart == E_BDCART.SUCCESS) {
			tblKnaCust.setIdckrt(eIdckrt);
			tblKnaCust.setMpckrt(eMpckrt);
			tblKnaCust.setAcctst(E_ACCTST.NORMAL);
			KnaCustDao.updateOne_odb1(tblKnaCust);
			output.setChekre(E_CHEKRE.SUCCESS);
		}

		// 将相关字段映射到属性区登记
		property.setAccatp(eAccatp);
		property.setCustid(tblKnaCust.getCustno());
		property.setBrchno(tblKnaCust.getBrchno());
//		property.setIdtfno(cplCifCust.getIdtfno());
//		property.setIdtftp(cplCifCust.getIdtftp());
		property.setCardno(tblKnaAcdc.getCardno());
		
	}

	public static void bindmq(
			final cn.sunline.ltts.busi.catran.trans.intf.Enable.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Enable.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Enable.Output output) {

		// 通过电子账号ID查询电子账号
		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil
				.getInstance(IoCaSevQryTableInfo.class);

		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb1(
				input.getCustac(), E_DPACST.NORMAL, false);

		if (CommUtil.isNull(caKnaAcdc.getCardno())) {
			throw CaError.Eacct.BNAS0695();
		}

		String cardno = caKnaAcdc.getCardno();

		// 根据电子账号查询出用户ID
		IoCaKnaCust knacust = SysUtil.getInstance(IoCaSevQryTableInfo.class)
				.getKnaCustByCardnoOdb1(cardno, true);
		/*
		IoCifCustAccs cifcustaccs = SysUtil.getInstance(
				IoCuSevQryTableInfo.class).cif_cust_accsByCustno(
				knacust.getCustno(), true, E_STATUS.NORMAL);
				*/

		/*KnpParameter para = KnpParameterDao.selectOne_odb1("BDCAMQ", "%", "%", "%", true);

		String bdid = para.getParm_value1();// 服务绑定ID

		String mssdid = CommTools.getMySysId();// 随机生成消息ID

		String mesdna = para.getParm_value2();// 媒介名称

		

		IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind(
				IoCaOtherService.class, bdid);

		IoCaOtherService.IoCaBindMqService.InputSetter mqInput = CommTools
				.getInstance(IoCaOtherService.IoCaBindMqService.InputSetter.class);
		*/
		E_BINDTP bindtp = E_BINDTP.BIND;// 绑定方式
		//MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
		//mri.setMtopic("Q0101003");
		DpBindMqService mqInput = SysUtil.getInstance(DpBindMqService.class);

		//mqInput.setMsgid(mssdid); // 发送消息ID
		//mqInput.setMdname(mesdna); // 媒介名称
		mqInput.setBindst(bindtp); // 绑定方式
		mqInput.setEactno(cardno); // 电子账号
		mqInput.setBindno(input.getCdopac()); // 绑定账户
		mqInput.setAtbkno(input.getOpbrch()); // 账户开户行号
		mqInput.setAcusna(input.getCdopna()); // 绑定账户名称
		mqInput.setAtbkna(input.getBrchna()); // 绑定机构名称
		mqInput.setAccttp(input.getCardtp()); // 绑定账户类型
		mqInput.setIsiner(input.getIsbkca()); // 是否本行卡
		mqInput.setCustid(knacust.getCustno());// 用户ID

		CommTools.addMessagessToContext("T0101004", "ApSmsType.PbinusSendMsg");
		//mri.setMsgtyp("ApSmsType.DpBindMqService");
		//mri.setMsgobj(mqInput); 
		//AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
		//caOtherService.bindMqService(mqInput);

	}

	// 更新客户化状态
	public static void updCustStatus(
			final cn.sunline.ltts.busi.catran.trans.intf.Enable.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Enable.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Enable.Output output) {

		// 登记客户化状态
		IoCaUpdAcctstIn cplDimeInfo = SysUtil
				.getInstance(IoCaUpdAcctstIn.class);
		cplDimeInfo.setCustac(input.getCustac());
		E_ACCATP accttp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
				.qryAccatpByCustac(input.getCustac()); // 获取电子账户分类

		if (CommUtil.isNull(accttp)) {

			throw CaError.Eacct.BNAS0660();
		}
		
		cplDimeInfo.setDime01(accttp.getValue()); // 维度1 电子账户类型
		cplDimeInfo.setFacesg(property.getFacelg());// 面签标识
		SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(
				cplDimeInfo);

	}

	/**
	 * 
	 * @Title: sedOpenInfoMessage
	 * @Description: (启用成功发送消息)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年12月8日 上午10:17:53
	 * @version V2.3.0
	 */
	public static void sedOpenInfoMessage(
			final cn.sunline.ltts.busi.catran.trans.intf.Enable.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Enable.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Enable.Output output) {

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

		openSendMsgInput.setMsgid(mssdid); // 发送消息ID
		openSendMsgInput.setMdname(mesdna); // 媒介名称
		openSendMsgInput.setOpacrt(E_YES___.YES);// 是否开户成功
		openSendMsgInput.setCustid(property.getCustid());// 用户ID
		openSendMsgInput.setBrchno(property.getBrchno());// 机构号
		openSendMsgInput.setAccatp(property.getAccatp());// 账户分类
		openSendMsgInput.setCktntp(E_CKTNTP.OPEN); //交易类型

		caOtherService.openUpgSendMsg(openSendMsgInput);

		// 2.开户成功发送协议到合约库
		KnpParameter tblKnaPara1 = KnpParameterDao.selectOne_odb1("OPUPGD", "AGRTSM",
				"%", "%", true);

		String mssdid1 = CommTools.getMySysId();// 消息ID
		String mesdna1 = tblKnaPara1.getParm_value2();// 媒介名称

		IoCaOtherService.IoCaSendContractMsg.InputSetter openSendAgrtInput = SysUtil
				.getInstance(IoCaOtherService.IoCaSendContractMsg.InputSetter.class);

		String sAgdata = property.getIdtftp() + "|" + property.getIdtfno() + "|"
				+ input.getCustna() + "|" + property.getCardno() + "|"
				+ property.getAccatp() + "|" + property.getBrchno() + "|"
				+ CommTools.getBaseRunEnvs().getTrxn_date();// 协议回填字段

		openSendAgrtInput.setMsgid(mssdid1); // 发送消息ID
		openSendAgrtInput.setMdname(mesdna1); // 媒介名称
		openSendAgrtInput.setUserId(property.getCustid());// 用户ID
		openSendAgrtInput.setOpenOrg(property.getBrchno());// 机构号
		openSendAgrtInput.setAcctNo(property.getCardno());// 电子账号
		openSendAgrtInput.setAcctName(input.getCustna());// 户名
		openSendAgrtInput.setRecordCount(ConvertUtil.toInteger(1));// 记录数
		openSendAgrtInput.setOpenFlag(E_CKTNTP.OPEN);// 开户升级标志
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

			IoCaOtherService.IoCaSendMonMsg.InputSetter sendMonMsgInput = SysUtil
					.getInstance(IoCaOtherService.IoCaSendMonMsg.InputSetter.class);

			sendMonMsgInput.setMsgid(mssdid2); // 发送消息ID
			sendMonMsgInput.setMdname(mesdna2); // 媒介名称
			sendMonMsgInput.setUserId(property.getCustid());// 用户ID
			sendMonMsgInput.setAcctNo(property.getCardno());// 电子账号
			sendMonMsgInput.setAcctName(input.getCustna());// 客户姓名
			sendMonMsgInput.setCertNo(property.getIdtfno());// 证件号码
			sendMonMsgInput.setCertType(property.getIdtftp());// 证件类型
			sendMonMsgInput.setTransBranch(property.getBrchno());// 机构编号
			sendMonMsgInput.setMobileNo(input.getTeleno());// 注册手机号
			sendMonMsgInput.setTransTime(DateTools2.getCurrentTimestamp());//操作时间

			caOtherService.sendMonMsg(sendMonMsgInput);
		}

	}

}
