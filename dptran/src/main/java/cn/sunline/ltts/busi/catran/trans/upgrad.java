package cn.sunline.ltts.busi.catran.trans;

import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.namedsql.AccountLimitDao;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
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
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BACATP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BDCART;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CHEKRE;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CKTNTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_IDCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_MPCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PROMSV;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;

/**
 * 
 * @ClassName: upgrad
 * @Description: (客户端升级交易前处理)
 * @author xiongzhao
 * @date 2016年7月11日 下午8:30:22
 * 
 */
public class upgrad {

	public static void prcUpgBefore(
			final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Output output) {
		
		
		
		String sCardno = input.getCardno();// 电子账号
		String sCkrtsq = input.getCkrtsq();// 核查流水号
		E_ACCATP eOdactp = input.getOdactp();// 原账户分类
		E_ACCATP eUpactp = input.getUpactp();// 升级账户分类
		E_IDCKRT eIdckrt = input.getIdckrt();// 身份核查结果
		E_MPCKRT eMpckrt = input.getMpckrt();// 人脸识别结果
		E_PROMSV ePromsv = input.getPromsv();// 升级渠道
		E_YES___ eRisklv = input.getRisklv();// 风险承受等级
		String sCdopac = input.getCdopac(); // 绑定账户
		String sCdopna = input.getCdopna(); // 绑定账户名称
		E_BACATP eCardtp = input.getCardtp(); // 绑定账户类型
		E_YES___ eIsbkca = input.getIsbkca(); // 是否本行账户
		E_BDCART eBdcart = input.getBdcart();// 绑卡认证结果
		String sAgrtno = input.getAgrtno();// 协议模板编号
		String sVesion = input.getVesion();// 版本号
	    String sOpbrch = input.getOpbrch();// 账户开户行号
	    String sBrchna = input.getBrchna();// 账户开户行名称
	    E_YES___ eBingfg = null;//是否绑卡标志
	    
	    
	    // 检查输入接口必输项是否为空
		// 电子账号
		if (CommUtil.isNull(sCardno)) {
			throw DpModuleError.DpstProd.BNAS0926();
		}
		// 原账户分类
		if (CommUtil.isNull(eOdactp)) {
			throw CaError.Eacct.BNAS1291();
		}
		// 升级账户分类
		if (CommUtil.isNull(eUpactp)) {
			throw CaError.Eacct.BNAS1292();
		}
		// 身份核查结果
		if (CommUtil.isNull(eIdckrt)) {
			throw CaError.Eacct.BNAS0368();
		}
		// 人脸识别结果
		if (CommUtil.isNull(eMpckrt)) {
			throw CaError.Eacct.BNAS0378();
		}
		// 升级渠道
		if (CommUtil.isNull(ePromsv)) {
			throw CaError.Eacct.BNAS1293();
		}
		// 风险承受等级
		if (CommUtil.isNull(eRisklv)) {
			throw CaError.Eacct.BNAS0771();
		}
		
		// 升级交易备注
		BusiTools.getBusiRunEnvs().setRemark(ePromsv.getLongName()+"升级");
		
		// 如果升级渠道为手机，需要校验核查流水号，协议模板编号，版本号不为空
		if (ePromsv == E_PROMSV.TELCLIENT) {
			// 核查流水号
			if (CommUtil.isNull(sCkrtsq)) {
				throw CaError.Eacct.BNAS0678();
			}
			// 协议模板编号
			if (CommUtil.isNull(sAgrtno)) {
				throw CaError.Eacct.BNAS1294();
			}
			// 版本号
			if (CommUtil.isNull(sVesion)) {
				throw CaError.Eacct.BNAS1295();
			}
		}
		
	    // 查询是否绑卡参数
	    KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("upgbindca", "%", "%", "%", true);
	    if (CommUtil.equals("Y", tblKnpParameter.getParm_value1())) {
	    	eBingfg = E_YES___.YES;
	    }else {
	    	eBingfg = E_YES___.NO;
	    }
	    if (eBingfg == E_YES___.YES) {
	    	if (CommUtil.isNotNull(sCdopac)) {
	    		if (eBdcart == E_BDCART.SUCCESS) {
	    			property.setBdcafg(E_YES___.YES);
	    		}else {
	    			property.setBdcafg(E_YES___.NO);
	    		}
	    	} else {
	    		property.setBdcafg(E_YES___.YES);
	    	}
	    } else {
	    	property.setBdcafg(E_YES___.YES);
	    }
	    
	    // 是否绑卡标识
	    property.setBindfg(eBingfg);

		// 查询卡客户账户对照表
		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(sCardno, false);
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
		KnaAcal tblKnaAcal = CaDao.selKnaAcalByCustac(tblKnaAcdc.getCustac(), E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);
		if (CommUtil.isNotNull(tblKnaAcal)) {
			property.setAcalno(tblKnaAcal.getTlphno());// 电子账户绑定手机号
		}
		
		// 查询客户信息表
		/*
		IoCucifCust cplCifCust = SysUtil.getInstance(
				IoCuSevQryTableInfo.class).cif_cust_selectOne_odb1(
				tblKnaCust.getCustno(), true);
				*/
		
//		IoSrvCfPerson.IoGetCifCust.InputSetter queryCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//		queryCust.setCustno(tblKnaCust.getCustno());
//		IoSrvCfPerson.IoGetCifCust.Output cplCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//		SysUtil.getInstance(IoSrvCfPerson.class).getCifCust(queryCust, cplCifCust);
		
	
		// 如果升级账户分类为二,三类户，且无下挂借记账户的，抛出异常
		if (eUpactp == E_ACCATP.FINANCE || eUpactp == E_ACCATP.WALLET) {

			// 升级理财功能户不允许提交线下申请
			if (ePromsv == E_PROMSV.INCASE) {
				throw CaError.Eacct.BNAS1296();
			}

			// 查询电子账户下绑卡认证通过的信息
			List<KnaCacd> lstKnaCacd = KnaCacdDao.selectAll_odb3(
					tblKnaCust.getCustac(), E_DPACST.NORMAL, false);

			// 判断借记账户张数，二类户至少绑定一张同名一类借记账户
			if (lstKnaCacd.size() < 1) {
				// 判断传进来的参数：账户开户行号不能为空
				if (CommUtil.isNull(sOpbrch)) {
					throw CaError.Eacct.BNAS0179();
				}
				// 判断传进来的参数：账户开户行名称不能为空
				if (CommUtil.isNull(sBrchna)) {
					throw CaError.Eacct.BNAS0178();
				}
				// 判断传进来的参数：绑定账户不能为空
				if (CommUtil.isNull(sCdopac)) {
					throw CaError.Eacct.BNAS1109();
				}
				// 判断传进来的参数：绑定账户名称不能为空
				if (CommUtil.isNull(sCdopna)) {
					throw CaError.Eacct.BNAS1107();
				}
				// 判断传进来的参数：绑定账户类型不能为空
				if (CommUtil.isNull(eCardtp)) {
					throw CaError.Eacct.BNAS1108();
				}
				// 判断传进来的参数：是否本行账户标识不能为空
				if (CommUtil.isNull(eIsbkca)) {
					throw CaError.Eacct.BNAS0354();
				}
				// 理财功能户,电子钱包账户至少绑定一张认证通过的账户
				if (eBdcart != E_BDCART.SUCCESS) {
					throw CaError.Eacct.BNAS1297();
				}
			}
		}

		// 根据升级账户分类获取开户产品号
		KnpParameter tblKnpParameter2 = SysUtil.getInstance(DpProdSvcType.class)
				.qryProdcd(input.getUpactp());

		// 查询电子账户个人结算户信息
		IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(IoDpKnaAcct.class);
		if (input.getOdactp() == E_ACCATP.GLOBAL
				|| input.getOdactp() == E_ACCATP.FINANCE) {
			cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctSub(tblKnaAcdc.getCustac(),E_ACSETP.SA);
			// 将结算户负债子账号映射到属性区，提供新开负债账户后的签约处理
			property.setAcctnm(cplKnaAcct.getAcctno());// 需解约的负债账号

		} else if (input.getOdactp() == E_ACCATP.WALLET) {
			cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctSub(tblKnaAcdc.getCustac(),E_ACSETP.MA);
		}
		
		// 根据电子账号查询用户ID
		/*
		IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(
				IoCuSevQryTableInfo.class).cif_cust_accsByCustno(
				tblKnaCust.getCustno(), false, E_STATUS.NORMAL);
		
		if (CommUtil.isNull(cplCifCustAccs)) {
			throw CaError.Eacct.E0001("查询不到电子账号关联的用户ID！");
		}
		*/
		
		//核查结果返回，若升级渠道为手机端返回结果，若为柜面不返回
		if(ePromsv == E_PROMSV.TELCLIENT){
			if(eIdckrt == E_IDCKRT.CHECKING || eMpckrt == E_MPCKRT.CHECKING){
				output.setChekre(E_CHEKRE.CHECKING);
			}
			if(eIdckrt == E_IDCKRT.SUCCESS && eMpckrt == E_MPCKRT.SUCCESS){
				output.setChekre(E_CHEKRE.SUCCESS);
			}
		}
		
		
		// 将查询出的值映射到属性字段
//		property.setIdtfno(cplCifCust.getIdtfno());// 证件号码
//		property.setIdtftp(cplCifCust.getIdtftp());// 证件类型
		property.setCustid(tblKnaCust.getCustno());// 用户ID
		property.setCktntp(E_CKTNTP.UPGRADE);// 核查交易类型
		property.setProcst(E_PROCST.SUSPEND);// 处理状态
		property.setCustno(tblKnaCust.getCustno());// 客户号
		property.setCustac(tblKnaCust.getCustac());// 电子账号
		property.setCacttp(tblKnaCust.getCacttp());// 客户账户类型
		property.setCustna(tblKnaCust.getCustna());// 客户名称
		property.setBrchno(tblKnaCust.getBrchno());// 归属机构
		property.setBaprcd(tblKnpParameter2.getParm_value1());// 基础负债产品
		property.setCrcycd(cplKnaAcct.getCrcycd());// 币种
		property.setCsextg(cplKnaAcct.getCsextg());// 钞汇标识

	}

	/**
	 * 
	 * @Title: prcTransInform
	 * @Description: (升级信息通知)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年7月26日 下午7:51:58
	 * @version V2.3.0
	 */
	public static void prcTransInform(
			final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Output output) {

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
		upgdSendMsgInput.setAccatp(input.getUpactp());// 账户分类
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
				+ property.getCustna() + "|" + input.getCardno() + "|"
				+ input.getUpactp() + "|" + property.getBrchno() + "|"
				+ CommTools.getBaseRunEnvs().getTrxn_date();// 协议回填字段
		
		upgdSendAgrtInput.setMsgid(mssdid1); // 发送消息ID
		upgdSendAgrtInput.setMdname(mesdna1); // 媒介名称
		upgdSendAgrtInput.setUserId(property.getCustid());// 用户ID
		upgdSendAgrtInput.setOpenOrg(property.getBrchno());// 机构号
		upgdSendAgrtInput.setAcctNo(input.getCardno());// 电子账号
		upgdSendAgrtInput.setAcctName(property.getCustna());// 户名
		upgdSendAgrtInput.setRecordCount(ConvertUtil.toInteger(1));// 记录数
		upgdSendAgrtInput.setOpenFlag(E_CKTNTP.UPGRADE);// 开户升级标志
		upgdSendAgrtInput.setTransTime(DateTools2.getCurrentTimestamp());// 操作时间
		
		IoCaAgrtInfos cplAgrtInfos = SysUtil.getInstance(IoCaAgrtInfos.class);
		cplAgrtInfos.setAgrTemplateNo(input.getAgrtno());// 协议模板编号
		cplAgrtInfos.setVersion(input.getVesion());// 版本号
		cplAgrtInfos.setAgrData(sAgdata);// 协议回填字段
		upgdSendAgrtInput.getAgreementList().add(cplAgrtInfos);// 协议列表

		caOtherService.sendContractMsg(upgdSendAgrtInput);
		
		// 3.升级二类户成功将理财签约相关信息发送到合约库
		if (input.getUpactp() == E_ACCATP.FINANCE) {
			KnpParameter tblKnaPara3 = KnpParameterDao.selectOne_odb1("OPENMN", "AGRTSM",
					"%", "%", true);

			String mssdid2 = CommTools.getMySysId();// 消息ID
			String mesdna2 = tblKnaPara3.getParm_value2();// 媒介名称

			IoCaOtherService.IoCaSendMonMsg.InputSetter sendMonMsgInput = SysUtil
					.getInstance(IoCaOtherService.IoCaSendMonMsg.InputSetter.class);

			sendMonMsgInput.setMsgid(mssdid2); // 发送消息ID
			sendMonMsgInput.setMdname(mesdna2); // 媒介名称
			sendMonMsgInput.setUserId(property.getCustid());// 用户ID
			sendMonMsgInput.setAcctNo(input.getCardno());// 电子账号
			sendMonMsgInput.setAcctName(property.getCustna());// 客户姓名
			sendMonMsgInput.setCertNo(property.getIdtfno());// 证件号码
			sendMonMsgInput.setCertType(property.getIdtftp());// 证件类型
			sendMonMsgInput.setTransBranch(property.getBrchno());// 机构编号
			sendMonMsgInput.setMobileNo(property.getAcalno());// 绑定手机号
			sendMonMsgInput.setTransTime(DateTools2.getCurrentTimestamp());//操作时间

			caOtherService.sendMonMsg(sendMonMsgInput);
		}
	}

	public static void bindmq( 
			final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Input input,  
			final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Property property,  
			final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Output output){
		
/*	    KnpParameter para = KnpParameterDao.selectOne_odb1("BDCAMQ", "%", "%", "%", true);
		
		String bdid = para.getParm_value1();// 服务绑定ID
		
		String mssdid = CommTools.getMySysId();// 随机生成消息ID
		
		String mesdna = para.getParm_value2();// 媒介名称
		
		
		IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
		
		IoCaOtherService.IoCaBindMqService.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaBindMqService.InputSetter.class);
		*/
//		MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//		mri.setMtopic("Q0101003");
//		DpBindMqService mqInput = SysUtil.getInstance(DpBindMqService.class);
//		E_BINDTP bindtp = E_BINDTP.BIND;// 绑定方式
//		
//		//mqInput.setMsgid(mssdid); //发送消息ID
//		//mqInput.setMdname(mesdna); //媒介名称
//		mqInput.setBindst(bindtp); //绑定方式
//		mqInput.setEactno(input.getCardno()); //电子账号
//		mqInput.setBindno(input.getCdopac()); //绑定账户
//		mqInput.setAtbkno(input.getOpbrch()); //账户开户行号
//		mqInput.setAcusna(input.getCdopna()); //绑定账户名称
//		mqInput.setAtbkna(input.getBrchna()); //绑定账户名称
//		mqInput.setAccttp(input.getCardtp()); //绑定账户类型
//		mqInput.setIsiner(input.getIsbkca()); //绑定账户类型
//		mqInput.setCustid(property.getCustid());// 用户ID
//		
//		mri.setMsgtyp("ApSmsType.DpBindMqService");
//		mri.setMsgobj(mqInput); 
//		AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
		//caOtherService.bindMqService(mqInput);
		
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
	public static void prcCancelContract(
			final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Output output) {
		
		
		String timetm =DateTools2.getCurrentTimestamp();
		
		// 将签约信息更新为一类户的负债账户签约
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
	 * @date 2016年9月24日 上午10:47:47
	 * @version V2.3.0
	 */
	public static void updTrdStatus(
			final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Output output) {

		// 登记电子账户状态
		IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
		cplDimeInfo.setCustac(property.getCustac());
//		cplDimeInfo.setDime01(input.getOdactp().getValue());
		cplDimeInfo.setFacesg(input.getFacesg());// 面签标识
		SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(
				cplDimeInfo);

	}

	public static void clearTsscTram( final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Upgrad.Output output){
		AccountLimitDao.updTsscAcct(property.getCustac());
	}
}
