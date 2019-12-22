package cn.sunline.ltts.busi.catran.trans;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSign;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSignDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbCasp;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbCaspDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbClac;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbClacDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbDisa;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbDisaDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEALST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_RESULT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_APPRWY;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSATP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSTAT;

public class disabl {
	/**
	 * 
	 * @Title: prcDisablTransBefore
	 * @Description: (停用转久悬交易前处理)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年7月20日 下午2:45:54
	 * @version V2.3.0
	 */
	public static void prcDisablTransBefore(
			final cn.sunline.ltts.busi.catran.trans.intf.Disabl.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Disabl.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Disabl.Output output) {

		String sCradno = input.getCardno();// 卡号
		E_YES___ eApprfg = input.getApprfg();// 是否征得被冒用人同意
		E_APPRWY eApprwy = input.getApprwy();// 征求方式
		BigDecimal bigropam = input.getPropam();// 转久悬金额

		// 检查交易接口必输项
		// 卡号
		if (CommUtil.isNull(sCradno)) {
			throw DpModuleError.DpstComm.BNAS0955();
		}
		// 是否征得被冒用人同意
		if (CommUtil.isNull(eApprfg)) {
			throw CaError.Eacct.BNAS0344();
		}
		// 征求方式
		if (CommUtil.isNull(eApprwy)) {
			throw CaError.Eacct.BNAS0160();
		}
		// 转久悬金额
		if (CommUtil.isNull(bigropam)) {
			throw CaError.Eacct.BNAS0034();
		}

		// 如未征得被冒用人统一，不允许转久悬
		if (eApprfg == E_YES___.NO) {
			throw CaError.Eacct.BNAS1202();
		}

		// 根据卡号查询出电子账号
		KnaAcdc tblKnaCacd = KnaAcdcDao.selectOne_odb2(sCradno, false);

		// 检查查询记录是否存在
		if (CommUtil.isNull(tblKnaCacd)
				|| tblKnaCacd.getStatus() == E_DPACST.CLOSE) {
			throw CaError.Eacct.BNAS0750();
		}

		// 查询电子账户表
		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(
				tblKnaCacd.getCustac(), false);

		// 检查查询记录是否存在
		if (CommUtil.isNull(tblKnaCust)) {
			throw CaError.Eacct.BNAS0750();
		}

		// 查询证件类型证件号码
		/*
		IoCucifCust cplCifCust = SysUtil.getInstance(
				IoCuSevQryTableInfo.class).cif_cust_selectOne_odb1(
				tblKnaCust.getCustno(), true);
				*/
//		IoSrvCfPerson.IoGetCifCust.InputSetter queryCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//		queryCust.setCustno(tblKnaCust.getCustno());
//		IoSrvCfPerson.IoGetCifCust.Output cplCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//		SysUtil.getInstance(IoSrvCfPerson.class).getCifCust(queryCust, cplCifCust);
		
		// 查询出用户ID
		/*
		IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(
				IoCuSevQryTableInfo.class).cif_cust_accsByCustno(
				tblKnaCust.getCustno(), true, E_STATUS.NORMAL);
				*/
		
		// 查询绑定手机号
		KnaAcal tblKnaAcal = CaDao.selKnaAcalByCustac(tblKnaCust.getCustac(), E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);
		if (CommUtil.isNotNull(tblKnaAcal)) {
			property.setAcalno(tblKnaAcal.getTlphno());// 电子账户绑定手机号
		}
		
		// 查询出电子账户的账户分类
		E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
				.qryAccatpByCustac(tblKnaCacd.getCustac());

		// 查询电子账户个人结算户信息
		IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(tblKnaCacd.getCustac());

		// 将电子账户基本信息映射到属性区
		property.setAccatp(eAccatp);// 账户分类
		property.setCustac(tblKnaCust.getCustac());// 电子账号
		property.setCustid(tblKnaCust.getCustno());// 用户ID
//		property.setIdtfno(cplCifCust.getIdtfno());// 证件号码
//		property.setIdtftp(cplCifCust.getIdtftp());// 证件类型
		property.setCrcycd(cplKnaAcct.getCrcycd());// 币种
		property.setCsextg(cplKnaAcct.getCsextg());// 钞汇标识
		property.setCustna(tblKnaCust.getCustna());// 账户名称

		// 查询内部户久悬未取款业务代码
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("DISABLKEY", "busino",
				"%", "%", true);
		IoInacInfo acinfo = CommTools.getRemoteInstance(IoInQuery.class)
				.selInacInfoByBusino(tblKnpParameter.getParm_value1(),
						cplKnaAcct.getBrchno(), cplKnaAcct.getCrcycd(), null);
		if (CommUtil.isNull(acinfo.getAcctno())) {
			property.setAcbrno(null);// 总账账号
			property.setAcbrna(null);// 总账名称
		} else {
			property.setAcbrno(acinfo.getAcctno());// 总账账号
			property.setAcbrna(acinfo.getAcctna());// 总账名称
		}

		// 将久悬未取款业务代码映射到属性区
		property.setBusino(tblKnpParameter.getParm_value1());// 业务代码
		// 将内部户入账属性映射到属性区
		property.setAcbrch(cplKnaAcct.getBrchno());// 机构号
		property.setTrantp(E_TRANTP.TR);

		// 将销户校验字段映射到属性区
		property.getCplClsAcctIn().setCardno(input.getCardno());// 卡号
		property.getCplClsAcctIn().setCustac(tblKnaCacd.getCustac());// 电子账号
		property.getCplClsAcctIn().setCustna(tblKnaCust.getCustna());// 账户名称
//		property.getCplClsAcctIn().setCustno(cplCifCust.getCustno());// 客户号
//		property.getCplClsAcctIn().setIdtfno(cplCifCust.getIdtfno());// 证件号码
//		property.getCplClsAcctIn().setIdtftp(cplCifCust.getIdtftp());// 证件类型

		// 查询签约表获取签约号
		KnaSign tblKnaSign = KnaSignDao.selectFirst_odb2(
				tblKnaCacd.getCustac(), E_SIGNTP.ZNCXL, E_SIGNST.QY, false);
		if (CommUtil.isNull(tblKnaSign)) {
			property.setSignno(null);
			property.setIssign(E_YES___.NO);
		} else {
			property.setSignno(tblKnaSign.getSignno());
			property.setIssign(E_YES___.YES);
		}

	}

	/**
	 * 
	 * @Title: prcDisablTransAfter
	 * @Description: (停用转久悬交易后处理)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年7月20日 下午2:46:18
	 * @version V2.3.0
	 */
	public static void prcDisablTransAfter(
			final cn.sunline.ltts.busi.catran.trans.intf.Disabl.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Disabl.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Disabl.Output output) {

		// 金额校验
		if (!CommUtil.equals(input.getPropam(), property.getClosam())) {
			throw CaError.Eacct.BNAS0033();
		}

		// 注销电子账户
		property.getCplClsAcctIn().setClossq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		CommTools.getRemoteInstance(DpAcctSvcType.class).acctStatusUpd(
				property.getCplClsAcctIn());

		// 查询停用登记簿信息
		List<KnbCasp> lstKnbCasp = KnbCaspDao.selectAll_odb1(
				property.getCustac(), E_DEALST.UNDEAL, false);

		// 检查查询记录是否存在且只有一条
		KnbCasp tblKnbCasp = SysUtil.getInstance(KnbCasp.class);
		if (CommUtil.isNotNull(lstKnbCasp) && lstKnbCasp.size() != 1) {
			throw CaError.Eacct.BNAS1000();
		} else {
			// 取记录中第一条记录
			tblKnbCasp = lstKnbCasp.get(0);
		}

		// 检查查询取出记录是否为空值
		if (CommUtil.isNull(tblKnbCasp.getCustac())) {
			throw CaError.Eacct.BNAS0924();
		}

		tblKnbCasp.setProcdt(CommTools.getBaseRunEnvs().getTrxn_date());// 处理日期
		tblKnbCasp.setProctm(BusiTools.getBusiRunEnvs().getTrantm());// 处理时间
		tblKnbCasp.setProcrs(E_RESULT.CLPROP);// 处理结果
		tblKnbCasp.setProcst(E_DEALST.PROCED);// 处理状态
		tblKnbCasp.setProcds(input.getRemark());// 处理说明
		tblKnbCasp.setProcsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 处理流水
		tblKnbCasp.setDlhdid(CommTools.getBaseRunEnvs().getTrxn_teller());// 操作柜员
		tblKnbCasp.setDlussq(CommTools.getBaseRunEnvs().getInitiator_seq());// 柜员流水
		tblKnbCasp.setDlatid(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus());// 授权柜员
		tblKnbCasp.setDlatsq(BusiTools.getBusiRunEnvs().getAuthvo().getAuthsq());// 授权柜员流水

		// 更新停用登记簿信息
		KnbCaspDao.updateOne_odb3(tblKnbCasp);

		// 登记销户登记簿
		KnbClac tblKnbClac = SysUtil.getInstance(KnbClac.class);
		tblKnbClac.setAccttp(property.getAccatp());// 账户分类
		tblKnbClac.setClosam(property.getClosam());// 销户金额
		tblKnbClac.setClosbr(CommTools.getBaseRunEnvs().getTrxn_branch());// 销户机构
		tblKnbClac.setClosdt(CommTools.getBaseRunEnvs().getTrxn_date());// 销户日期
		tblKnbClac.setClostm(BusiTools.getBusiRunEnvs().getTrantm());// 销户时间
		tblKnbClac.setClossq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 销户流水
		tblKnbClac.setClossv(CommTools.getBaseRunEnvs().getChannel_id());// 销户渠道
		tblKnbClac.setClosus(CommTools.getBaseRunEnvs().getTrxn_teller());// 销户柜员
		tblKnbClac.setCrcycd(property.getCrcycd());// 币种
		tblKnbClac.setCsextg(property.getCsextg());// 钞汇标志
		tblKnbClac.setCustac(property.getCustac());// 电子账号
		tblKnbClac.setCustna(property.getCustna());// 户名
		tblKnbClac.setTnacna(property.getAcbrna());// 转入户名
		tblKnbClac.setTnacno(property.getAcbrno());// 转入账号
		tblKnbClac.setTnactp(property.getTractp());// 转入账户类型
		tblKnbClac.setStatus(E_CLSTAT.SUCC);// 处理状态

		// 数据插入登记销户登记簿
		KnbClacDao.insert(tblKnbClac);

		// 登记停用转久悬登记簿
		KnbDisa tblKnbDisa = SysUtil.getInstance(KnbDisa.class);
		tblKnbDisa.setCustac(property.getCustac());// 电子账号
		tblKnbDisa.setCustna(property.getCustna());// 户名
		tblKnbDisa.setAccttp(property.getAccatp());// 账户分类
		tblKnbDisa.setBrchno(property.getAcbrch());// 归属机构
		tblKnbDisa.setBkupdt(CommTools.getBaseRunEnvs().getTrxn_date());// 转久悬日期
		tblKnbDisa.setBkuptm(BusiTools.getBusiRunEnvs().getTrantm());// 转久悬时间
		tblKnbDisa.setPropam(property.getClosam());// 转久悬金额
		tblKnbDisa.setBkuprs(tblKnbCasp.getBkuprs());// 停用原因
		tblKnbDisa.setRemark(tblKnbCasp.getRemark());// 备注
		tblKnbDisa.setApprfg(input.getApprfg());// 是否征得本人同意
		tblKnbDisa.setApprwy(input.getApprwy());// 征求方式
		tblKnbDisa.setApwyds(input.getRemark());// 征求方式备注
		tblKnbDisa.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());// 操作柜员
		tblKnbDisa.setUssqno(CommTools.getBaseRunEnvs().getInitiator_seq());// 操作柜员流水
		tblKnbDisa.setAuthus(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus());// 授权柜员
		tblKnbDisa.setAuthsq(BusiTools.getBusiRunEnvs().getAuthvo().getAuthsq());// 授权柜员流水
		tblKnbDisa.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 停用转久悬流水
		if (CommUtil.equals(input.getPropam(), BigDecimal.ZERO)) {
			tblKnbDisa.setDlhdid(CommTools.getBaseRunEnvs().getTrxn_teller());// 处理操作柜员
			tblKnbDisa.setDlussq(CommTools.getBaseRunEnvs().getInitiator_seq());// 处理操作柜员流水
			tblKnbDisa.setDlatid(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus());// 处理授权柜员
			tblKnbDisa.setDlatsq(BusiTools.getBusiRunEnvs().getAuthvo().getAuthsq());// 处理授权柜员流水
			tblKnbDisa.setProcdt(CommTools.getBaseRunEnvs().getTrxn_date());// 处理日期
			tblKnbDisa.setProctm(BusiTools.getBusiRunEnvs().getTrantm());// 处理时间
			tblKnbDisa.setProcrs(E_RESULT.ZEROCLOSE);// 处理结果
			tblKnbDisa.setProcst(E_DEALST.PROCED);// 处理状态
			tblKnbDisa.setProcsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 处理流水
		} else {
			tblKnbDisa.setProcst(E_DEALST.UNDEAL);// 处理状态
		}

		// 数据插入停用转久悬登记簿
		KnbDisaDao.insert(tblKnbDisa);
		
		//登记客户化状态
		IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
		cplDimeInfo.setCustac(property.getCustac());
		cplDimeInfo.setDime01(E_CLSATP.INACCT.getValue()); //收款人账户类型
		SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);
		
		// 调用平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._10);
		
	}

	/**
	 * 
	 * @Title: sendCloseInfoMsg
	 * @Description: (销户信息通知)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年7月26日 下午9:44:40
	 * @version V2.3.0
	 */
	public static void sendCloseInfoMsg(
			final cn.sunline.ltts.busi.catran.trans.intf.Disabl.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Disabl.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Disabl.Output output) {


/*		// 查询销户登记簿处理状态为成功的记录
		KnbClac tblKnbClac = CaDao.selKnbClacByCustac(property.getCustac(),
				E_CLSTAT.SUCC, false);*/
		
		// 查询电子账户状态
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(property.getCustac());
		if (cuacst == E_CUACST.CLOSED) {

//			mri.setMsgtyp("ApSmsType.IoCaCloseAcctSendMsg");
//			mri.setMsgobj(closeSendMsgInput); 
//			AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
//			
//			//消息推送至APP客户端
//			MessageRealInfo mri2 = SysUtil.getInstance(MessageRealInfo.class);
//			mri2.setMtopic("Q0101005");
//			//mri.setTdcnno("R00");  //测试指定DCN
//			ToAppSendMsg toAppSendMsg = CommTools
//					.getInstance(ToAppSendMsg.class);
//			// 消息内容
//			toAppSendMsg.setUserId(cplCifCustAccs.getCustid()); //用户ID
//			toAppSendMsg.setOutNoticeId("Q0101005"); //外部消息ID
//			toAppSendMsg.setNoticeTitle("您的电子账户已停用转久悬"); //公告标题
//			toAppSendMsg.setContent("您的ThreeBank电子账户已转久悬。如有疑问可咨询我行客服，电话0471-96616。"); //公告内容
//			toAppSendMsg.setSendtime(CommTools.getBaseRunEnvs().getTrxn_date()+BusiTools.getBusiRunEnvs().getTrantm()); //消息生成时间
//			toAppSendMsg.setTransType(E_APPTTP.CUACCH); //交易类型
//			toAppSendMsg.setTirggerSys(SysUtil.getSystemId()); //触发系统
//			toAppSendMsg.setClickType(E_CLIKTP.NO);   //点击动作类型
//			//toAppSendMsg.setClickValue(clickValue); //点击动作值
//			
//			mri2.setMsgtyp("ApSmsType.ToAppSendMsg");
//			mri2.setMsgobj(toAppSendMsg); 
//			AsyncMessageUtil.add(mri2); //将待发送消息放入当前交易暂存区，commit后发送
			
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
			closeSendMsgInput.setMdname(mesdna); // 媒介名称
			closeSendMsgInput.setCustid(property.getCustid()); // 用户ID
			closeSendMsgInput.setClosfg(E_YES___.NO);// 是否挂失销户标志（转久悬销户不会出现挂失销户）
			closeSendMsgInput.setClosbr(CommTools.getBaseRunEnvs().getTrxn_branch());// 操作机构
			closeSendMsgInput.setClosus(CommTools.getBaseRunEnvs().getTrxn_teller());// 操作柜员
			closeSendMsgInput.setClossv(CommTools.getBaseRunEnvs().getChannel_id());// 销户渠道
			caOtherService.closeAcctSendMsg(closeSendMsgInput);
			
			// 2.销户成功发送销户结果到合约库
			KnpParameter tblKnaPara1 = KnpParameterDao.selectOne_odb1("CLOSAC", "AGRTSM",
					"%", "%", true);

			String mssdid1 = CommTools.getMySysId();// 消息ID

			String mesdna1 = tblKnaPara1.getParm_value2();// 媒介名称

			IoCaOtherService.IoCaClAcSendContractMsg.InputSetter closeSendAgrtInput = SysUtil.getInstance(IoCaOtherService.IoCaClAcSendContractMsg.InputSetter.class);
	
			closeSendAgrtInput.setMsgid(mssdid1); // 发送消息ID
			closeSendAgrtInput.setMdname(mesdna1); // 媒介名称
			closeSendAgrtInput.setUserId(property.getCustid()); // 用户ID
			closeSendAgrtInput.setAcctType(property.getAccatp());// 账户分类
			closeSendAgrtInput.setOrgId(property.getAcbrch());// 归属机构
			closeSendAgrtInput.setAcctNo(input.getCardno());// 电子账号
			closeSendAgrtInput.setAcctStat(E_CUACST.CLOSED);// 客户化状态
			closeSendAgrtInput.setAcctName(property.getCustna());// 户名
			closeSendAgrtInput.setCertNo(property.getIdtfno());// 证件号码
			closeSendAgrtInput.setCertType(property.getIdtftp());// 证件类型
			closeSendAgrtInput.setTransTime(DateTools2.getCurrentTimestamp());//操作时间

			caOtherService.clAcSendContractMsg(closeSendAgrtInput);*/
			
		}

	  
	}

	/**
	 * 
	 * @Title: chkRedpack
	 * @Description: (停用转久悬前检查是否有未落地红包)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年11月26日 下午2:14:01
	 * @version V2.3.0
	 */
	public static void chkRedpack(
			final cn.sunline.ltts.busi.catran.trans.intf.Disabl.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Disabl.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Disabl.Output output) {
		
		IoCaOtherService.IoCaChkRedpack.InputSetter chkIN = SysUtil.getInstance(IoCaOtherService.IoCaChkRedpack.InputSetter.class);
		IoCaOtherService.IoCaChkRedpack.Output		chkOT = SysUtil.getInstance(IoCaOtherService.IoCaChkRedpack.Output.class);
		
		chkIN.setAcctno(input.getCardno());
		chkIN.setUserid(property.getCustid());
		
		IoCaOtherService dubboSrv = SysUtil.getInstanceProxyByBind(IoCaOtherService.class, "otsevdb");
		try {
			dubboSrv.chkRedpack(chkIN, chkOT);
		} catch (Exception e) {

			throw DpModuleError.DpstComm.BNAS1282();

		}
		
		if(chkOT.getExisfg() == E_YES___.YES){
			throw DpModuleError.DpstComm.E9999(chkOT.getReason());
		}
	
	}

	public static void qryinac( final cn.sunline.ltts.busi.catran.trans.intf.Disabl.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Disabl.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Disabl.Output output){
		
		E_INSPFG inspfg = property.getInspfg();
		
		if(E_INSPFG.INVO == inspfg){ //账户涉案
			//交易信息登记输入
			IoCaKnbTrinInput entity = SysUtil.getInstance(IoCaKnbTrinInput.class);
			entity.setCrcycd(property.getCrcycd());//币种
			entity.setInacna(property.getAcbrna());//转入账户名称
			entity.setInbank(property.getAcbrch());//转入银行行号
			entity.setIncard(property.getAcbrno());//转入账号
			entity.setIssucc(E_YES___.NO);//是否成功
			entity.setOtacna(property.getCustna());//转出账户名称
			entity.setOtbrch(CommTools.getBaseRunEnvs().getTrxn_branch());//转出账户机构
			entity.setOtcard(input.getCardno());//转出账号
			entity.setTranam(input.getPropam());//交易金额
			entity.setTrantp(cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRANTP.TRANSFER);//交易类型
			//调用涉案可疑交易信息登记服务
			SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(entity);
			throw DpModuleError.DpstAcct.BNAS0770();
		}
		
		if(E_INSPFG.SUSP == inspfg){ //账户可疑
			//交易信息登记输入
			IoCaKnbTrinInput entity = SysUtil.getInstance(IoCaKnbTrinInput.class);
			
			entity.setCrcycd(property.getCrcycd());//币种
			entity.setInacna(property.getAcbrna());//转入账户名称
			entity.setInbank(property.getAcbrch());//转入银行行号
			entity.setIncard(property.getAcbrno());//转入账号
			entity.setIssucc(E_YES___.NO);//是否成功
			entity.setOtacna(property.getCustna());//转出账户名称
			entity.setOtbrch(CommTools.getBaseRunEnvs().getTrxn_branch());//转出账户机构
			entity.setOtcard(input.getCardno());//转出账号
			entity.setTranam(input.getPropam());//交易金额
			entity.setTrantp(cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRANTP.TRANSFER);//交易类型
			
			//调用涉案可疑交易信息登记服务
			SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(entity);
		}
	}
}
