package cn.sunline.ltts.busi.ca.serviceimpl;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.busi.dp.tables.agent.AgentRelation.Knb_agentDao;
import cn.sunline.edsp.busi.dp.tables.agent.AgentRelation.knb_agent;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.ca.base.DecryptConstant;
import cn.sunline.ltts.busi.ca.card.process.CaCardProc;
import cn.sunline.ltts.busi.ca.eacct.process.CaEAccountProc;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KcdCard;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCuad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCuadDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaadDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSign;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSignDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSignDetl;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSignDetlDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaTrtp;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaTrtpDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbCasp;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbCaspDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpbt;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnpAcst;
import cn.sunline.ltts.busi.ca.type.CaCustInfo;
import cn.sunline.ltts.busi.ca.type.FacctInfo;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.IoQryBindCardInfo.Input;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStaPublic;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApCaUpacstIn;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaHknsAcsq;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaSelAcctno;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEARelaIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEAccountOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddJFEacctOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaOpenAccInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryMesgOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.QryEacctInfosIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.QryEacctInfosOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.QryFacctInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.qrbkCaIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.qrbkCaOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypeGenBindCard.IoAgentList;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpClsChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpClsChkOT;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctBtoprtInfo;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.errors.CaError.Eacct;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.errors.PbError;
import cn.sunline.ltts.busi.sys.type.ApSmsType.ToAppSendMsg;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSTRFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEALST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IFBICA;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INOTFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_RESULT;
import cn.sunline.ltts.busi.sys.type.CaEnumType;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BKUPRS;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CKTRIF;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CKTRTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_IDCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_MPCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_OPACWY;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_OPENRS;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_UNSGTP;
import cn.sunline.ltts.busi.sys.type.FnEnumType.E_QRYCON;
import cn.sunline.ltts.busi.sys.type.FnEnumType.E_WARNTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_REBUWA;

/**
 * 电子账户服务 电子账户服务
 * 
 * @param <Output>
 * 
 */
@cn.sunline.adp.core.annotation.Generated
public class IoCaSrvGenEAccountInfoImpl<Output>
		implements cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo {

	public static final int PWD_TM = 6; // 密码最大错误次数
	private static final BizLog bizlog = BizLogUtil.getBizLog(IoCaSrvGenEAccountInfoImpl.class);

	/**
	 * 开立电子账户服务
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEAccountOut prcAddEAccount(
			final cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEAccountIn cplAddEAccountIn) {
		// 获取输出接口实例
		IoCaAddEAccountOut cplAddEAccountOut = SysUtil.getInstance(IoCaAddEAccountOut.class);
		// 产生电子账户信息
		cplAddEAccountOut = CaEAccountProc.addEAccountInfo(cplAddEAccountIn);
		// 如果开卡标志不为否，且身份核查或人脸识别为中间状态则不生成内部卡号，或者开户方式为批量开户的情况，直接开立卡号
		if (cplAddEAccountIn.getIsopcd() != BaseEnumType.E_YES___.NO) {
			// 产生卡信息，并返回卡号
			String cardno = CaCardProc.prcCardInfo(cplAddEAccountIn, cplAddEAccountOut);
			// 注册内部卡账号路由表 取消 modified by sunzy 20191114
			// ApAcctRoutTools.register(cardno, E_ACCTROUTTYPE.CARD);
			// 设置卡号到输出接口
			cplAddEAccountOut.setCardno(cardno);
			// 建立电子账号与卡号关联关系
			CaCardProc.prcEacctCardLink(cplAddEAccountOut.getCustac(), cplAddEAccountOut.getCardno());

			// 登记开户登记簿
			IoCaOpenAccInfo cplOpenAccInfo = SysUtil.getInstance(IoCaOpenAccInfo.class);
			cplOpenAccInfo.setCustac(cplAddEAccountOut.getCustac());
			cplOpenAccInfo.setAccttp(cplAddEAccountIn.getAccttp());
			cplOpenAccInfo.setCustna(cplAddEAccountIn.getCustna());
			cplOpenAccInfo.setCrcycd(cplAddEAccountIn.getCrcycd());
			cplOpenAccInfo.setUschnl(cplAddEAccountIn.getUschnl());
			if (CommUtil.equals(cplAddEAccountIn.getCrcycd(), BusiTools.getDefineCurrency())) {
				cplOpenAccInfo.setCsextg(E_CSEXTG.CASH);
			} else {
				cplOpenAccInfo.setCsextg(cplAddEAccountIn.getCsextg());
			}
			cplOpenAccInfo.setTlphno(cplAddEAccountIn.getTlphno());
			cplOpenAccInfo.setBrchno(cplAddEAccountOut.getBrchno());
			cplOpenAccInfo.setOpendt(cplAddEAccountOut.getOpendt());
			cplOpenAccInfo.setOpensq(cplAddEAccountOut.getOpensq());

			// mod by xj 20180613
			// cplOpenAccInfo.setChnlid(cplAddEAccountIn.getChnlid());//开户渠道
			CaEAccountProc.addOpenAcctBook(cplOpenAccInfo);

		}
		// }else{
		// 设置卡号到输出接口
		// cplAddEAccountOut.setCardno(tblKnaAcdc.getCardno());
		// }

		// 登记客户化状态 取消掉客户化内容modified by sunzy 20191114
		// IoCaUpdAcctstIn cplDimeInfo = SysUtil
		// .getInstance(IoCaUpdAcctstIn.class);
		// cplDimeInfo.setCustac(cplAddEAccountOut.getCustac());
		// if (CommUtil.isNotNull(cplAddEAccountIn.getAccttp())) {
		// cplDimeInfo.setDime01(cplAddEAccountIn.getAccttp().getValue()); // 维度1
		// // 账户类型
		// }
		// if (CommUtil.isNotNull(cplAddEAccountIn.getIdckrt())) {
		// cplDimeInfo.setDime02(cplAddEAccountIn.getIdckrt().getValue()); // 维度2
		// }
		// if (CommUtil.isNotNull(cplAddEAccountIn.getMpckrt())) {
		// cplDimeInfo.setDime03(cplAddEAccountIn.getMpckrt().getValue()); // 维度3
		// }
		// cplDimeInfo.setCustno(cplAddEAccountIn.getCustno()); // 客户号
		// cplDimeInfo.setFacesg(cplAddEAccountIn.getFacesg()); // 面签标识
		// updCustAcctst(cplDimeInfo);
		// 注册账号路由表
		// 路由注册去掉modified by sunzy 20191114
		// ApAcctRoutTools.register(cplAddEAccountOut.getCustac(),
		// E_ACCTROUTTYPE.CUSTAC);

		/*
		 * JF Add：登记电子主账户附加信息。
		 */
		KnaMaad tblKnaMaad = SysUtil.getInstance(KnaMaad.class);
		tblKnaMaad.setCustac(cplAddEAccountOut.getCustac());
		tblKnaMaad.setCardno(cplAddEAccountOut.getCardno());
		tblKnaMaad.setCustno(cplAddEAccountIn.getCustno());
		tblKnaMaad.setCustna(cplAddEAccountIn.getCustna());
		tblKnaMaad.setTmcustna(DecryptConstant.maskName(cplAddEAccountIn.getCustna()));
		tblKnaMaad.setIdtfno(cplAddEAccountIn.getIdtfno());
		tblKnaMaad.setTmidtfno(DecryptConstant.maskIdCard(cplAddEAccountIn.getIdtfno()));
		tblKnaMaad.setIdtftp(cplAddEAccountIn.getIdtftp());
		tblKnaMaad.setUschnl(cplAddEAccountIn.getUschnl());
		tblKnaMaad.setUsertp(cplAddEAccountIn.getUsertp());
		tblKnaMaad.setMactid(cplAddEAccountIn.getMactid());
		tblKnaMaad.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		tblKnaMaad.setTmstmp(CommTools.getBaseRunEnvs().getTimestamp());

		KnaMaadDao.insert(tblKnaMaad);

		return cplAddEAccountOut;
	}

	@Override
	/**
	 * 负债子账户与电子账户关联服务
	 */
	public void prcAddEARela(IoCaAddEARelaIn cplAddEARelaIn) {
		// 生成电子账户子户号，获得相关负债子户与电子账户信息，进行处理
		CaEAccountProc.prcAddEARelaInfo(cplAddEARelaIn);

	}

	/**
	 * 电子账户信息列表查询（管理平台用）
	 * 
	 * @param ecctin
	 * @param Output
	 */
	@Override
	public QryEacctInfosOut qryEacctInfos(QryEacctInfosIn ecctin) {

		String custna = ecctin.getCustna();
		String ecctno = ecctin.getEcctno();
		E_IDTFTP idtftp = ecctin.getIdtftp();
		String idtfno = ecctin.getIdtfno();
		String teleno = ecctin.getTeleno();

		// 电子账号、证件号码、手机号 不能同时为空
		if (CommUtil.isNull(idtftp) && CommUtil.isNull(idtfno) && CommUtil.isNull(teleno) && CommUtil.isNull(ecctno)) {
			throw CaError.Eacct.BNAS0950();
		}

		long startt = ecctin.getStartt();
		long record = ecctin.getRecord();
		QryEacctInfosOut out = SysUtil.getInstance(QryEacctInfosOut.class);
		List<cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.QryEacctInfos> infos = CaDao
				.getEacctInfos(ecctno, teleno, custna, idtftp, idtfno, startt, record, false);
		out.setEaccts(
				new DefaultOptions<cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.QryEacctInfos>(infos));
		out.setRecdsm(CaDao.countEacctInfos(ecctno, teleno, custna, idtftp, idtfno, false));

		return out;
	}

	/**
	 * 生成电子账户数据文件
	 * 
	 * @author JX.Chang
	 * 
	 */
	@Override
	public void genCaCustFiles(String lstrdt) {

		// 产生文件的日期目录
		String lstrdtPath = lstrdt + "/";

		/******* 客户信息（导出数据）开始 ***********/
		// 获取文件生产路径
		KnpParameter para1 = KnpParameterDao.selectOne_odb1("ACCT", "cafile", "01", "%", true);
		String path1 = para1.getParm_value1();
		path1 = para1.getParm_value1() + lstrdtPath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");
		// 获取文件名
		String filename1 = para1.getParm_value2();
		bizlog.debug("文件名称 filename:[" + filename1 + "]");
		// 获取是否产生文件标志
		String isCreateFlg1 = CommUtil.nvl(para1.getParm_value3(), "Y");
		bizlog.debug("文件产生标志 :[" + isCreateFlg1 + "]");
		// 获取加载模式（增量/全量）
		String createMode1 = CommUtil.nvl(para1.getParm_value5(), "ZL");
		bizlog.debug("文件加载模式 :[" + createMode1 + "]");
		if (CommUtil.equals(isCreateFlg1, "Y")) {
			final LttsFileWriter file = new LttsFileWriter(path1, filename1);
			// List<KnaCust> entities = null;
			Params params = new Params();
			String namedSqlId = "";// 查询数据集的命名sql
			if (CommUtil.equals(createMode1, "QL")) {
				// entities = CaDao.selKnCustAll(false);
				namedSqlId = CaDao.namedsql_selKnCustAll;
			}
			// else {
			// entities = CuDao.selCustInfoByDate(lstrdt, false);
			// }
			if (true) {
				file.open();
				try {
					DaoUtil.selectList(namedSqlId, params, new CursorHandler<KnaCust>() {
						@Override
						public boolean handle(int index, KnaCust entity) {
							// 写文件
							String custac = (CommUtil.isNotNull(entity.getCustac()) ? entity.getCustac() : "");
							String cacttp = (CommUtil.isNotNull(entity.getCacttp().getValue())
									? entity.getCacttp().getValue()
									: "");
							String custno = (CommUtil.isNotNull(entity.getCustno()) ? entity.getCustno() : "");
							String custna = (CommUtil.isNotNull(entity.getCustna()) ? entity.getCustna() : "");
							String opendt = (CommUtil.isNotNull(entity.getOpendt()) ? entity.getOpendt() : "");
							String opensq = (CommUtil.isNotNull(entity.getOpensq()) ? entity.getOpensq() : "");
							String closdt = (CommUtil.isNotNull(entity.getClosdt()) ? entity.getClosdt() : "");
							String clossq = (CommUtil.isNotNull(entity.getClossq()) ? entity.getClossq() : "");
							String cardno = (CommUtil.isNotNull(entity.getCardno()) ? entity.getCardno() : "");
							String accttp = (CommUtil.isNotNull(entity.getAccttp()) ? entity.getAccttp().getValue()
									: "");
							String brchno = (CommUtil.isNotNull(entity.getBrchno()) ? entity.getBrchno() : "");
							String acctst = (CommUtil.isNotNull(entity.getAcctst()) ? entity.getAcctst().getValue()
									: "");
							/*
							 * String datetm = (CommUtil.isNotNull(entity .getDatetm()) ? entity.getDatetm()
							 * : "");
							 */
							String timetm = (CommUtil.isNotNull(entity.getTmstmp()) ? entity.getTmstmp().toString()
									: "");

							file.write(custac + "^" + cacttp + "^" + custno + "^" + custna + "^" + opendt + "^" + opensq
									+ "^" + closdt + "^" + clossq + "^" + cardno + "^" + accttp + "^" + brchno + "^"
									+ acctst + "^" + "^" + timetm);
							return true;
						}
					});

					// if (CommUtil.isNotNull(entities)) {
					// KnaCust entity = SysUtil.getInstance(KnaCust.class);
					// for (int i = 0; i < entities.size(); i++) {
					// entity = entities.get(i);
					// // 写文件
					// String custac = (CommUtil.isNotNull(entity.getCustac()) ?
					// entity.getCustac() : "");
					// String cacttp = (CommUtil.isNotNull(entity.getCacttp()) ?
					// entity.getCacttp() : "");
					// String custno = (CommUtil.isNotNull(entity.getCustno()) ?
					// entity.getCustno() : "");
					// String custna = (CommUtil.isNotNull(entity.getCustna()) ?
					// entity.getCustna() : "");
					// String opendt = (CommUtil.isNotNull(entity.getOpendt()) ?
					// entity.getOpendt() : "");
					// String opensq = (CommUtil.isNotNull(entity.getOpensq()) ?
					// entity.getOpensq() : "");
					// String closdt = (CommUtil.isNotNull(entity.getClosdt()) ?
					// entity.getClosdt() : "");
					// String clossq = (CommUtil.isNotNull(entity.getClossq()) ?
					// entity.getClossq() : "");
					// String cardno = (CommUtil.isNotNull(entity.getCardno()) ?
					// entity.getCardno() : "");
					// String accttp = (CommUtil.isNotNull(entity.getAccttp()) ?
					// entity.getAccttp().getValue() : "");
					// String brchno = (CommUtil.isNotNull(entity.getBrchno()) ?
					// entity.getBrchno() : "");
					// String acctst = (CommUtil.isNotNull(entity.getAcctst()) ?
					// entity.getAcctst().getValue() : "");
					// String datetm = (CommUtil.isNotNull(entity.getDatetm()) ?
					// entity.getDatetm() : "");
					// String timetm = (CommUtil.isNotNull(entity.getTimetm()) ?
					// entity.getTimetm().toString() : "");
					//
					// file.write(custac + "^" + cacttp + "^" + custno
					// + "^" + custna + "^" + opendt + "^" + opensq
					// + "^" + closdt + "^" + clossq + "^" + cardno
					// + "^" + accttp + "^" + brchno + "^" + acctst
					// + "^" + datetm + "^" + timetm
					// );
					// }
					// }
				} finally {
					file.close();
				}

			}

			bizlog.debug("电子账户（导出数据）" + filename1 + "文件产生完成");
		}
		/******* 客户信息（导出数据）结束 ***********/

		/******* 电子账户与子账户关联表（导出数据）开始 ***********/
		// 获取文件生产路径
		KnpParameter para2 = KnpParameterDao.selectOne_odb1("ACCT", "cafile", "02", "%", true);
		String path2 = para2.getParm_value1();
		path2 = para2.getParm_value1() + lstrdtPath;
		bizlog.debug("文件产生路径 path:[" + path2 + "]");
		// 获取文件名
		String filename2 = para2.getParm_value2();
		bizlog.debug("文件名称 filename:[" + filename2 + "]");
		// 获取是否产生文件标志
		String isCreateFlg2 = CommUtil.nvl(para2.getParm_value3(), "Y");
		bizlog.debug("文件产生标志 :[" + isCreateFlg2 + "]");
		// 获取加载模式（增量/全量）
		String createMode2 = CommUtil.nvl(para2.getParm_value5(), "ZL");
		bizlog.debug("文件加载模式 :[" + createMode2 + "]");
		if (CommUtil.equals(isCreateFlg2, "Y")) {
			final LttsFileWriter file = new LttsFileWriter(path2, filename2);
			// List<KnaAccs> entities = null;
			Params params = new Params();
			String namedSqlId = "";// 查询数据集的命名sql
			if (CommUtil.equals(createMode2, "QL")) {
				// entities = CaDao.selKnaAccsAll(false);
				namedSqlId = CaDao.namedsql_selKnaAccsAll;
			}
			// else {
			// entities = CuDao.selCustInfoByDate(lstrdt, false);
			// }
			if (true) {
				file.open();
				try {
					DaoUtil.selectList(namedSqlId, params, new CursorHandler<KnaAccs>() {
						@Override
						public boolean handle(int index, KnaAccs entity) {
							// 写文件
							String corpno = CommUtil.nvl(entity.getCorpno(), "");
							String custac = CommUtil.nvl(entity.getCustac(), "");
							String subsac = CommUtil.nvl(entity.getSubsac(), "");
							String acctno = CommUtil.nvl(entity.getAcctno(), "");
							String crcycd = (CommUtil.isNotNull(entity.getCrcycd()) ? entity.getCrcycd() : "");
							String csextg = (CommUtil.isNotNull(entity.getCsextg()) ? entity.getCsextg().getValue()
									: "");
							String fcflag = (CommUtil.isNotNull(entity.getFcflag()) ? entity.getFcflag().getValue()
									: "");
							String prodtp = (CommUtil.isNotNull(entity.getProdtp()) ? entity.getProdtp().getValue()
									: "");
							String prodcd = CommUtil.nvl(entity.getProdcd(), "");
							String acctst = (CommUtil.isNotNull(entity.getAcctst()) ? entity.getAcctst().getValue()
									: "");
							/*
							 * String datetm = CommUtil.nvl( entity.getDatetm(), "");
							 */
							String timetm = (CommUtil.isNotNull(entity.getTmstmp()) ? entity.getTmstmp().toString()
									: "");

							file.write(corpno + "^" + custac + "^" + subsac + "^" + acctno + "^" + crcycd + "^" + csextg
									+ "^" + fcflag + "^" + prodtp + "^" + prodcd + "^" + acctst + "^" + timetm);
							// + datetm + "^" + timetm);
							return true;
						}
					});

					// if (CommUtil.isNotNull(entities)) {
					// KnaAccs entity = SysUtil.getInstance(KnaAccs.class);
					// for (int i = 0; i < entities.size(); i++) {
					// entity = entities.get(i);
					// // 写文件
					// String corpno = CommUtil.nvl(entity.getCorpno(), "");
					// String custac = CommUtil.nvl(entity.getCustac(), "");
					// String subsac = CommUtil.nvl(entity.getSubsac(), "");
					// String acctno = CommUtil.nvl(entity.getAcctno(), "");
					// String crcycd = (CommUtil.isNotNull(entity.getCrcycd()) ?
					// entity.getCrcycd().getValue() : "");
					// String csextg = (CommUtil.isNotNull(entity.getCsextg()) ?
					// entity.getCsextg().getValue() : "");
					// String fcflag = (CommUtil.isNotNull(entity.getFcflag()) ?
					// entity.getFcflag().getValue() : "");
					// String prodtp = (CommUtil.isNotNull(entity.getProdtp()) ?
					// entity.getProdtp().getValue() : "");
					// String prodcd = CommUtil.nvl(entity.getProdcd(), "");
					// String acctst = (CommUtil.isNotNull(entity.getAcctst()) ?
					// entity.getAcctst().getValue() : "");
					// String datetm = CommUtil.nvl(entity.getDatetm(), "");
					// String timetm = (CommUtil.isNotNull(entity.getTimetm()) ?
					// entity.getTimetm().toString() : "");
					//
					// file.write(corpno + "^" + custac + "^" + subsac + "^" +
					// acctno + "^" + crcycd + "^" +
					// csextg + "^" + fcflag + "^" + prodtp + "^" + prodcd + "^"
					// + acctst + "^" +
					// datetm + "^" + timetm
					// );
					// }
					// }
				} finally {
					file.close();
				}

			}

			bizlog.debug("电子账户与子账户关联表（导出数据）" + filename2 + "文件产生完成");
		}
		/******* 电子账户与子账户关联表（导出数据）结束 ***********/

		/******* 内部卡（导出数据）开始 ***********/
		// 获取文件生产路径
		KnpParameter para3 = KnpParameterDao.selectOne_odb1("ACCT", "cafile", "03", "%", true);
		String path3 = para3.getParm_value1();
		path3 = para3.getParm_value1() + lstrdtPath;
		bizlog.debug("文件产生路径 path:[" + path3 + "]");
		// 获取文件名
		String filename3 = para3.getParm_value2();
		bizlog.debug("文件名称 filename:[" + filename3 + "]");
		// 获取是否产生文件标志
		String isCreateFlg3 = CommUtil.nvl(para3.getParm_value3(), "Y");
		bizlog.debug("文件产生标志 :[" + isCreateFlg3 + "]");
		// 获取加载模式（增量/全量）
		String createMode3 = CommUtil.nvl(para3.getParm_value5(), "ZL");
		bizlog.debug("文件加载模式 :[" + createMode3 + "]");
		if (CommUtil.equals(isCreateFlg3, "Y")) {
			final LttsFileWriter file = new LttsFileWriter(path3, filename3);
			// List<KcdCard> entities = null;
			Params params = new Params();
			String namedSqlId = "";// 查询数据集的命名sql
			if (CommUtil.equals(createMode3, "QL")) {
				// entities = CaDao.selKcdCardAll(false);
				namedSqlId = CaDao.namedsql_selKcdCardAll;
			}
			// else {
			// entities = CuDao.selCustInfoByDate(lstrdt, false);
			// }
			if (true) {
				file.open();
				try {
					DaoUtil.selectList(namedSqlId, params, new CursorHandler<KcdCard>() {
						@Override
						public boolean handle(int index, KcdCard entity) {
							// 写文件
							String cardno = (CommUtil.isNotNull(entity.getCardno()) ? entity.getCardno() : "");
							String prodcd = (CommUtil.isNotNull(entity.getProdcd()) ? entity.getProdcd() : "");
							String vrcdtp = (CommUtil.isNotNull(entity.getVrcdtp()) ? entity.getVrcdtp().getValue()
									: "");
							String opendt = (CommUtil.isNotNull(entity.getOpendt()) ? entity.getOpendt() : "");
							String matudt = (CommUtil.isNotNull(entity.getMatudt()) ? entity.getMatudt() : "");
							String closdt = (CommUtil.isNotNull(entity.getClosdt()) ? entity.getClosdt() : "");
							String chckfs = (CommUtil.isNotNull(entity.getChckfs()) ? entity.getChckfs().getValue()
									: "");
							String tranpw = (CommUtil.isNotNull(entity.getTranpw()) ? entity.getTranpw() : "");
							String qurypw = (CommUtil.isNotNull(entity.getQurypw()) ? entity.getQurypw() : "");
							String pwerct = (CommUtil.isNotNull(entity.getPwerct()) ? entity.getPwerct().toString()
									: "");
							String maxerr = (CommUtil.isNotNull(entity.getMaxerr()) ? entity.getMaxerr().toString()
									: "");
							String dcmtst = (CommUtil.isNotNull(entity.getDcmtst()) ? entity.getDcmtst().getValue()
									: "");
							/*
							 * String datetm = (CommUtil.isNotNull(entity .getDatetm()) ? entity.getDatetm()
							 * : "");
							 */
							String timetm = (CommUtil.isNotNull(entity.getTmstmp()) ? entity.getTmstmp().toString()
									: "");

							file.write(cardno + "^" + prodcd + "^" + vrcdtp + "^" + opendt + "^" + matudt + "^" + closdt
									+ "^" + chckfs + "^" + tranpw + "^" + qurypw + "^" + pwerct + "^" + maxerr + "^"
									+ dcmtst + "^" + "^" + timetm);
							return true;
						}
					});

					// if (CommUtil.isNotNull(entities)) {
					// KcdCard entity = SysUtil.getInstance(KcdCard.class);
					// for (int i = 0; i < entities.size(); i++) {
					// entity = entities.get(i);
					// // 写文件
					// String cardno = (CommUtil.isNotNull(entity.getCardno()) ?
					// entity.getCardno() : "");
					// String prodcd = (CommUtil.isNotNull(entity.getProdcd()) ?
					// entity.getProdcd() : "");
					// String vrcdtp = (CommUtil.isNotNull(entity.getVrcdtp()) ?
					// entity.getVrcdtp().getValue() : "");
					// String opendt = (CommUtil.isNotNull(entity.getOpendt()) ?
					// entity.getOpendt() : "");
					// String matudt = (CommUtil.isNotNull(entity.getMatudt()) ?
					// entity.getMatudt() : "");
					// String closdt = (CommUtil.isNotNull(entity.getClosdt()) ?
					// entity.getClosdt() : "");
					// String chckfs = (CommUtil.isNotNull(entity.getChckfs()) ?
					// entity.getChckfs().getValue() : "");
					// String tranpw = (CommUtil.isNotNull(entity.getTranpw()) ?
					// entity.getTranpw() : "");
					// String qurypw = (CommUtil.isNotNull(entity.getQurypw()) ?
					// entity.getQurypw() : "");
					// String pwerct = (CommUtil.isNotNull(entity.getPwerct()) ?
					// entity.getPwerct().toString() : "");
					// String maxerr = (CommUtil.isNotNull(entity.getMaxerr()) ?
					// entity.getMaxerr().toString() : "");
					// String dcmtst = (CommUtil.isNotNull(entity.getDcmtst()) ?
					// entity.getDcmtst().getValue() : "");
					// String datetm = (CommUtil.isNotNull(entity.getDatetm()) ?
					// entity.getDatetm() : "");
					// String timetm = (CommUtil.isNotNull(entity.getTimetm()) ?
					// entity.getTimetm().toString() : "");
					//
					// file.write(cardno + "^" + prodcd + "^" + vrcdtp
					// + "^" + opendt + "^" + matudt + "^" + closdt
					// + "^" + chckfs + "^" + tranpw + "^" + qurypw
					// + "^" + pwerct + "^" + maxerr + "^" + dcmtst
					// + "^" + datetm + "^" + timetm
					// );
					// }
					// }
				} finally {
					file.close();
				}

			}

			bizlog.debug("内部卡（导出数据）" + filename3 + "文件产生完成");
		}
		/******* 内部卡（导出数据）结束 ***********/

		/******* 内部卡（导出数据）开始 ***********/
		// 获取文件生产路径
		KnpParameter para4 = KnpParameterDao.selectOne_odb1("ACCT", "cafile", "04", "%", true);
		String path4 = para4.getParm_value1();
		path4 = para4.getParm_value1() + lstrdtPath;
		bizlog.debug("文件产生路径 path:[" + path4 + "]");
		// 获取文件名
		String filename4 = para4.getParm_value2();
		bizlog.debug("文件名称 filename:[" + filename4 + "]");
		// 获取是否产生文件标志
		String isCreateFlg4 = CommUtil.nvl(para4.getParm_value3(), "Y");
		bizlog.debug("文件产生标志 :[" + isCreateFlg4 + "]");
		// 获取加载模式（增量/全量）
		String createMode4 = CommUtil.nvl(para4.getParm_value5(), "ZL");
		bizlog.debug("文件加载模式 :[" + createMode4 + "]");
		if (CommUtil.equals(isCreateFlg4, "Y")) {
			final LttsFileWriter file = new LttsFileWriter(path4, filename4);
			// List<KnaCacd> entities = null;
			Params params = new Params();
			String namedSqlId = "";// 查询数据集的命名sql
			if (CommUtil.equals(createMode4, "QL")) {
				// entities = CaDao.selKnaCacdAll(false);
				namedSqlId = CaDao.namedsql_selKnaCacdAll;
			}
			// else {
			// entities = CuDao.selCustInfoByDate(lstrdt, false);
			// }
			if (true) {
				file.open();
				try {
					DaoUtil.selectList(namedSqlId, params, new CursorHandler<KnaCacd>() {
						@Override
						public boolean handle(int index, KnaCacd entity) {
							// 写文件
							String custac = (CommUtil.isNotNull(entity.getAcbdno()) ? entity.getAcbdno() : "");
							String cardno = (CommUtil.isNotNull(entity.getCdopac()) ? entity.getCdopac() : "");
							String brchno = (CommUtil.isNotNull(entity.getOpbrch()) ? entity.getOpbrch() : "");
							String acctna = (CommUtil.isNotNull(entity.getAcctna()) ? entity.getAcctna() : "");
							String brchna = (CommUtil.isNotNull(entity.getBrchna()) ? entity.getBrchna() : "");
							String status = (CommUtil.isNotNull(entity.getStatus()) ? entity.getStatus().getValue()
									: "");
							String binddt = (CommUtil.isNotNull(entity.getBinddt()) ? entity.getBinddt() : "");
							/*
							 * String datetm = (CommUtil.isNotNull(entity .getDatetm()) ? entity.getDatetm()
							 * : "");
							 */
							String timetm = (CommUtil.isNotNull(entity.getTmstmp()) ? entity.getTmstmp().toString()
									: "");

							file.write(custac + "^" + cardno + "^" + brchno + "^" + acctna + "^" + brchna + "^" + status
									+ "^" + binddt + "^" + "^" + timetm);
							return true;
						}
					});

					// if (CommUtil.isNotNull(entities)) {
					// KnaCacd entity = SysUtil.getInstance(KnaCacd.class);
					// for (int i = 0; i < entities.size(); i++) {
					// entity = entities.get(i);
					// // 写文件
					// String custac = (CommUtil.isNotNull(entity.getCustac()) ?
					// entity.getCustac() : "");
					// String cardno = (CommUtil.isNotNull(entity.getCardno()) ?
					// entity.getCardno() : "");
					// String brchno = (CommUtil.isNotNull(entity.getBrchno()) ?
					// entity.getBrchno() : "");
					// String acctna = (CommUtil.isNotNull(entity.getAcctna()) ?
					// entity.getAcctna() : "");
					// String brchna = (CommUtil.isNotNull(entity.getBrchna()) ?
					// entity.getBrchna() : "");
					// String status = (CommUtil.isNotNull(entity.getStatus()) ?
					// entity.getStatus().getValue() : "");
					// String binddt = (CommUtil.isNotNull(entity.getBinddt()) ?
					// entity.getBinddt() : "");
					// String datetm = (CommUtil.isNotNull(entity.getDatetm()) ?
					// entity.getDatetm() : "");
					// String timetm = (CommUtil.isNotNull(entity.getTimetm()) ?
					// entity.getTimetm().toString() : "");
					//
					// file.write(custac + "^" + cardno + "^" + brchno
					// + "^" + acctna + "^" + brchna + "^" + status
					// + "^" + binddt + "^" + datetm + "^" + timetm
					// );
					// }
					// }
				} finally {
					file.close();
				}

			}

			bizlog.debug("内部卡（导出数据）" + filename4 + "文件产生完成");
		}
		/******* 内部卡（导出数据）结束 ***********/
	}

	@Override
	public void custSign(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.CustSign.Input input) {
		if (CommUtil.isNull(input.getSigntp()))
			return;
		if (CommUtil.isNull(input.getCustac()))
			throw DpModuleError.DpstAcct.BNAS0311();

		E_SIGNTP signtp = input.getSigntp();

		if (E_SIGNTP.ZNCXL == signtp) {
			// 智能储蓄签约

			// 判断是否已经签约
			KnaSign tbl_sign = KnaSignDao.selectFirst_odb2(input.getCustac(), signtp, E_SIGNST.QY, false);
			if (CommUtil.isNull(tbl_sign)) {
				// 登记签约

				String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
				// Long signno = Long.valueOf(SequenceManager.nextval("KnaSign_seq"));
				Long signno = Long.valueOf(CoreUtil.nextValue("KnaSign_seq"));
				// 签约表赋值
				tbl_sign = SysUtil.getInstance(KnaSign.class);
				tbl_sign.setSignno(signno);
				tbl_sign.setSigntp(input.getSigntp());
				tbl_sign.setCustac(input.getCustac());
				tbl_sign.setAutofg(BaseEnumType.E_YES___.YES);

				tbl_sign.setSigndt(trandt);
				tbl_sign.setSignst(E_SIGNST.QY);

				KnaSignDao.insert(tbl_sign);

				// 签约明细表
				KnaSignDetl sign_detl = SysUtil.getInstance(KnaSignDetl.class);

				sign_detl.setSigntp(signtp);
				sign_detl.setSignno(signno);
				sign_detl.setCustno(input.getCustno());
				sign_detl.setCustac(input.getCustac());
				sign_detl.setAcctno(input.getAcctno());
				sign_detl.setProdcd(input.getProdcd());
				sign_detl.setTrprod(input.getTrprod());
				sign_detl.setPeriod(input.getPeriod());
				sign_detl.setFrequy(input.getFrequy());
				sign_detl.setTrottp(input.getTrottp());
				sign_detl.setUnsgtp(input.getUnsgtp());
				if (CommUtil.isNull(input.getMiniam())) { // 转入最小金额
					sign_detl.setMiniam(BigDecimal.ZERO);
				} else {
					sign_detl.setMiniam(input.getMiniam());
				}
				if (CommUtil.isNull(input.getKeepam())) { // 保留最低金额
					sign_detl.setKeepam(BigDecimal.ZERO);
				} else {
					sign_detl.setKeepam(input.getKeepam());
				}
				if (CommUtil.isNull(input.getTrmiam())) { // 转出最小金额
					sign_detl.setTrmiam(BigDecimal.ZERO);
				} else {
					sign_detl.setTrmiam(input.getTrmiam());
				}
				if (CommUtil.isNull(input.getUpamnt())) { // 递增金额
					sign_detl.setUpamnt(BigDecimal.ZERO);
				} else {
					sign_detl.setUpamnt(input.getUpamnt());
				}
				if (CommUtil.isNull(input.getSignam())) { // 签约金额
					sign_detl.setSignam(BigDecimal.ZERO);
				} else {
					sign_detl.setSignam(input.getSignam());
				}
				if (CommUtil.isNull(input.getOtmiam())) {
					sign_detl.setOtmiam(BigDecimal.ZERO);
				} else {
					sign_detl.setOtmiam(input.getOtmiam());
				}
				if (CommUtil.isNull(input.getOtupam())) {
					sign_detl.setOtupam(BigDecimal.ZERO);
				} else {
					sign_detl.setOtupam(input.getOtupam());
				}

				sign_detl.setSigndt(trandt);
				sign_detl.setSignst(E_SIGNST.QY);
				sign_detl.setCurrdt(trandt);
				sign_detl.setFxacct(input.getFxacct()); // 存款子账号
				sign_detl.setEffedt(input.getEffedt()); // 失效日期
				// sign_detl.setNextdt(DateTools2.calDateByTerm(trandt,
				// input.getFrequy()+ input.getPeriod()));
				sign_detl.setNextdt(DateTools2.calDateByFreq(trandt, input.getFrequy()));

				KnaSignDetlDao.insert(sign_detl);
			} else {
				// 修改签约明细
				KnaSignDetl sign_detl = KnaSignDetlDao.selectOne_odb1(tbl_sign.getSignno(), true);

				sign_detl.setFxacct(input.getFxacct());

				KnaSignDetlDao.updateOne_odb1(sign_detl);
			}
		} else {
			throw CaError.Eacct.BNAS1154(signtp.toString());
		}

	}

	/**
	 * 
	 * <p>
	 * <li>作者 ：zll</li>
	 * <li>日期 ：2015年11月10日下午5:16:05</li>
	 * <li>方法描述：查询账户绑卡信息</li> 参数描述：
	 * 
	 * @param Input
	 * @param Output
	 */
	@Override
	public void qryBindCardInfo(Input Input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.IoQryBindCardInfo.Output Output) {

		// 检查证件号码是否与注册时一致
		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(Input.getCustac(), false);
		if (CommUtil.isNull(tblKnaCust)) {
			throw DpModuleError.DpstComm.BNAS1648(Input.getCustac());
		}
		String custno = tblKnaCust.getCustno();

		// 客户信息相关，模块拆分
		// IoSrvCfPerson.IoGetCifCust.InputSetter queryCifCust = SysUtil
		// .getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
		// IoSrvCfPerson.IoGetCifCust.Output tblcif_cust = SysUtil
		// .getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
		// IoSrvCfPerson cifCustServ = SysUtil.getInstance(IoSrvCfPerson.class);
		// queryCifCust.setCustno(custno);
		// cifCustServ.getCifCust(queryCifCust, tblcif_cust);

		// String idno = tblcif_cust.getIdtfno(); // 注册时证件号码
		if (!CommUtil.equals(Input.getIdtfno(), "")) {
			throw DpModuleError.DpstComm.BNAS1649(Input.getIdtfno());
		}

		// 获取卡信息
		List<KnaCacd> lstKnaCacd = KnaCacdDao.selectAll_odb3(Input.getCustac(), E_DPACST.NORMAL, false);

		if (CommUtil.isNotNull(lstKnaCacd)) {
			if (lstKnaCacd.size() > 1) {
				throw DpModuleError.DpstComm.BNAS1650();
			} else {
				for (KnaCacd card : lstKnaCacd) {
					Output.setAcctna(card.getAcctna());
					Output.setBrchna(card.getBrchna());
					Output.setBrchno(card.getOpbrch());
					Output.setCardno(card.getCdopac());
					Output.setIfbica(E_IFBICA.BIND);
				}
			}
		} else {
			Output.setIfbica(E_IFBICA.NOTBIND);
		}
	}

	/**
	 * 
	 * <p>
	 * <li>作者 ：zhangll</li>
	 * <li>日期 ：2016年1月15日下午3:31:04</li>
	 * <li>方法描述：校验是否开户和绑卡</li> 参数描述：
	 * 
	 * @param Input
	 *            idtpno 证件号码 idtftp 证件类型
	 * @param Output
	 */
	@Override
	public void checkAcctIsOpen(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.IoCheckAcctIsOpen.Input Input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.IoCheckAcctIsOpen.Output Output) {
		String idtfno = Input.getIdtfno(); // 证件号码
		E_IDTFTP idtftp = Input.getIdtftp(); // 证件类型
		if (CommUtil.isNull(idtfno)) {
			throw CaError.Eacct.BNAS0157();
		}
		// 1.判断是否开户
		// 取消掉客户信息相关内容，模块拆分
		// IoSrvCfPerson.IoExistsCifCust.InputSetter queryCifCust = SysUtil
		// .getInstance(IoSrvCfPerson.IoExistsCifCust.InputSetter.class);
		// IoSrvCfPerson.IoExistsCifCust.Output tblCust = SysUtil
		// .getInstance(IoSrvCfPerson.IoExistsCifCust.Output.class);
		// IoSrvCfPerson cifCustServ = SysUtil.getInstance(IoSrvCfPerson.class);
		// queryCifCust.setIdtfno(idtfno);
		// queryCifCust.setIdtftp(idtftp);
		// cifCustServ.existsCifCust(queryCifCust, tblCust);

		// if (Boolean.FALSE.equals(tblCust.getExistflag())) {
		// Output.setIsopac(E_YES___.NO);
		// Output.setIsbind(E_YES___.NO);
		// return;
		// } else {
		// Output.setIsopac(E_YES___.YES);
		// IoSrvCfPerson.IoGetCifCust.InputSetter getCifCustInput = SysUtil
		// .getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
		// IoSrvCfPerson.IoGetCifCust.Output getCifCustOutput = SysUtil
		// .getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
		// IoSrvCfPerson getCifCustServer = SysUtil
		// .getInstance(IoSrvCfPerson.class);
		// getCifCustInput.setIdtfno(idtfno);
		// getCifCustInput.setIdtftp(idtftp);
		// getCifCustServer.getCifCust(getCifCustInput, getCifCustOutput);
		//
		// // 2.判断是否绑卡
		// KnaCust tblKnaCust = KnaCustDao.selectFirst_odb2(E_CUSACT.ACC,
		// getCifCustOutput.getCustno(), true);
		// KnaCacd tblCard = KnaCacdDao.selectFirst_odb3(
		// tblKnaCust.getCustac(), E_DPACST.NORMAL, false);
		// if (CommUtil.isNull(tblCard)) {
		// Output.setIsopac(E_YES___.YES);
		// Output.setIsbind(E_YES___.NO);
		//
		// } else {
		// Output.setIsbind(E_YES___.YES);
		// Output.getChekou().setCardno(tblCard.getCardno()); // 绑定卡号
		// }
		//
		// Output.getChekou().setAcctst(tblKnaCust.getAcctst()); // 电子账户状态
		// Output.getChekou().setAccttp(tblKnaCust.getAccttp()); // 电子账户性质
		// Output.getChekou().setBrchno(tblKnaCust.getBrchno()); // 开户机构
		// Output.getChekou().setCustac(tblKnaCust.getCustac()); // 电子账号
		// Output.getChekou().setCustna(tblKnaCust.getCustna()); // 客户名称
		// Output.getChekou().setCustno(tblKnaCust.getCustno()); // 客户号
		// Output.getChekou().setOpendt(getCifCustOutput.getOpendt());// 开户日期
		// Output.getChekou().setOpensq(getCifCustOutput.getOpensq());// 开户流水
		// Output.getChekou().setClosdt(getCifCustOutput.getClosdt());// 销户日期
		// Output.getChekou().setClossq(getCifCustOutput.getClossq());// 销户流水
		// }

	}

	@Override
	/**
	 * 查询电子账户活期负债信息-公共
	 */
	public QryFacctInfo qryFacctByCustac(String custac) {

		FacctInfo.facctInfo facctInfo = CaDao.selFacctByCustac(custac, false);
		QryFacctInfo Info = SysUtil.getInstance(QryFacctInfo.class);
		CommUtil.copyProperties(Info, facctInfo, false);
		return Info;
	}

	@Override
	public void custUnSign(Long signno, BaseEnumType.E_YES___ isclos) {

		KnaSignDetl detl = KnaSignDetlDao.selectOne_odb1(signno, true);
		detl.setCncldt(CommTools.getBaseRunEnvs().getTrxn_date()); // 解约日期

		KnaSign sign = KnaSignDao.selectOne_odb1(signno, true);
		sign.setCncldt(CommTools.getBaseRunEnvs().getTrxn_date()); // 解约日期
		if (isclos == BaseEnumType.E_YES___.YES) {
			detl.setSignst(E_SIGNST.JY);
			sign.setSignst(E_SIGNST.JY);
		} else {
			if (detl.getUnsgtp() == E_UNSGTP.T2) { // 解约不销户
				detl.setSignst(E_SIGNST.QY);
				sign.setSignst(E_SIGNST.QY);
			} else { // 解约及销户
				detl.setSignst(E_SIGNST.JY);
				sign.setSignst(E_SIGNST.JY);
			}
		}

		KnaSignDetlDao.updateOne_odb1(detl);
		KnaSignDao.updateOne_odb1(sign); // 更新签约表

	}

	/**
	 * 检查电子账户是否允许转入 input.getTrantp: 01-ATM存现 02-ATM取现 03-ATM转入 04-银联在线转入
	 * 05-银联CUPS转入 06-本行借记卡转入 07-本行贷记卡转入 output.isable : 01-允许 02-不允许 03-账户不存在
	 **/
	@Override
	public void checkAcCarry(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.IoCaCheckAcCarry.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.IoCaCheckAcCarry.Output output) {

		if (CommUtil.isNull(input.getAcctac())) {
			throw DpModuleError.DpstAcct.BNAS0311();
		}
		if (CommUtil.isNull(input.getTrantp())) {
			throw CaError.Eacct.BNAS0615();
		}
		if (CommUtil.isNull(input.getTranam())) {
			// throw CaError.Eacct.E0001("交易金额不能为空");
		}
		// 校验电子账户
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoDpSrvQryTableInfo dpqry = SysUtil.getInstance(IoDpSrvQryTableInfo.class);
		IoCaKnaAcdc KnaAcdc = caqry.getKnaAcdcOdb2(input.getAcctac(), false); // 获得对内的电子账号ID

		if (CommUtil.isNull(KnaAcdc)) {
			output.setIsable(E_CKTRIF.NOAC);
			output.setRemark("电子账号不存在");
			return;
		}
		IoCaKnaCust KnaCust = caqry.getKnaCustByCustacOdb1(KnaAcdc.getCustac(), true); // 根据电子账号ID查询电子账户信息
		// 电子账户状态检查
		if (KnaCust.getAcctst() != E_ACCTST.NORMAL) {
			output.setIsable(E_CKTRIF.UNOK);
			output.setRemark("电子账号状态不正常");
			return;
		}
		E_CKTRTP trantp = input.getTrantp();
		boolean out = false; // 转出
		boolean in = false; // 转入
		// 电子账户类型检查和限额检查
		if (trantp == E_CKTRTP.ATMCR) { // ATM转账转入
			if (KnaCust.getAccttp() == E_ACCATP.WALLET) {
				output.setIsable(E_CKTRIF.UNOK);
				output.setRemark("钱包账户不允许ATM转入");
				return;
			}
			in = true;
		} else if (trantp == E_CKTRTP.ATMDP) { // ATM无卡存款
			if (KnaCust.getAccttp() != E_ACCATP.GLOBAL) {
				output.setIsable(E_CKTRIF.UNOK);
				output.setRemark("非全功能账户不允许ATM无卡存款");
				return;
			}
			in = true;
		} else if (trantp == E_CKTRTP.ATMDR) { // ATM无卡取款
			if (KnaCust.getAccttp() != E_ACCATP.GLOBAL) {
				output.setIsable(E_CKTRIF.UNOK);
				output.setRemark("非全功能账户不允许ATM无卡取款");
				return;
			}
			out = true;
			// TODO：余额检查
		} else if (trantp == E_CKTRTP.CRCCR || trantp == E_CKTRTP.CUPCR || trantp == E_CKTRTP.DBCCR
				|| trantp == E_CKTRTP.UNNCR) {
			// 检查限额
			if (KnaCust.getAccttp() == E_ACCATP.WALLET) {
				if (CommUtil.isNull(input.getTranam())) {
					throw CaError.Eacct.BNAS0623();
				}
				Options<IoDpKnaAcct> kna_acct = dpqry.listKnaAcctOdb6(KnaAcdc.getCustac(), true);
				for (IoDpKnaAcct acct : kna_acct) {
					if (acct.getAcctst() == E_DPACST.NORMAL && acct.getDebttp() == E_DEBTTP.DP2402) { // 钱包存款
						if (CommUtil.compare(input.getTranam(), acct.getHdmxmy().subtract(acct.getOnlnbl())) > 0) {
							output.setIsable(E_CKTRIF.UNOK);
							output.setRemark("电子钱包户限额不足");
							return;
						}
					}
				}
			}
			in = true;
		}
		// 电子账户状态字检查
		IoDpStaPublic dppub = SysUtil.getInstance(IoDpStaPublic.class);
		Options<IoDpKnbFroz> knb_froz = dppub.KnbFroz_getInfoByCustac(KnaAcdc.getCustac(), false); // 根据电子账号ID查询冻结信息

		if (out) {
			for (IoDpKnbFroz froz : knb_froz) {
				if (froz.getFroztp() == E_FROZTP.ADD || froz.getFroztp() == E_FROZTP.JUDICIAL) {
					if (froz.getFrlmtp() == E_FRLMTP.OUT) {
						output.setIsable(E_CKTRIF.UNOK);
						output.setRemark("电子账户已借冻");
						return;
					} else if (froz.getFrlmtp() == E_FRLMTP.ALL) {
						output.setIsable(E_CKTRIF.UNOK);
						output.setRemark("电子账户已双冻");
						return;
					}
				} else if (froz.getFroztp() == E_FROZTP.BANKSTOPAY) {
					output.setIsable(E_CKTRIF.UNOK);
					output.setRemark("电子账户已全止(银行止付)");
					return;
				} else if (froz.getFroztp() == E_FROZTP.CUSTSTOPAY) {
					output.setIsable(E_CKTRIF.UNOK);
					output.setRemark("电子账户已全止(客户止付)");
					return;
				} else if (froz.getFroztp() == E_FROZTP.EXTSTOPAY) {
					output.setIsable(E_CKTRIF.UNOK);
					output.setRemark("电子账户已全止(外部止付)");
					return;
				}
			}
		}
		if (in) {
			for (IoDpKnbFroz froz : knb_froz) {
				if (froz.getFroztp() == E_FROZTP.ADD || froz.getFroztp() == E_FROZTP.JUDICIAL) {
					if (froz.getFrlmtp() == E_FRLMTP.IN) {
						output.setIsable(E_CKTRIF.UNOK);
						output.setRemark("电子账户已借冻");
						return;
					} else if (froz.getFrlmtp() == E_FRLMTP.ALL) {
						output.setIsable(E_CKTRIF.UNOK);
						output.setRemark("电子账户已双冻");
						return;
					}
				}
			}
		}
		output.setIsable(E_CKTRIF.ENOK);
		output.setCustnm(KnaCust.getCustna());

	}

	@Override
	public cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.selAcctnoInfo.Output selAcctnoInfo(
			String cardno, String acctno, String prodcd, E_FCFLAG pddpfg, String sprdid, String sprdna, Long startno,
			Long count, Long totlCount) {

		KnaAcdc KnaAcdc = KnaAcdcDao.selectOne_odb2(cardno, false);

		if (CommUtil.isNull(KnaAcdc)) {
			throw CaError.Eacct.BNAS1651();
		}

		KnaCust KnaCust = KnaCustDao.selectOne_odb1(KnaAcdc.getCustac(), false);
		if (CommUtil.isNull(KnaCust)) {
			throw CaError.Eacct.BNAS0871();
		}

		Page<IoCaSelAcctno> knaAccs = CaDao.selAcctno(cardno, acctno, prodcd, pddpfg, sprdid, sprdna, startno, count,
				totlCount, false);

		List<IoCaSelAcctno> KnaAccs = knaAccs.getRecords();
		Options<IoCaSelAcctno> results = new DefaultOptions<IoCaSelAcctno>();
		results.addAll(KnaAccs);
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.selAcctnoInfo.Output output = SysUtil
				.getInstance(
						cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.selAcctnoInfo.Output.class);
		output.setSelact(results);
		output.setCounts(knaAccs.getRecordCount());
		bizlog.debug("<<<<<<<<<<<<<<<<<电子账号与负债账号关联关系>>>>>>>>>>>>>>>>>>>：output=" + output);
		return output;

	}

	/**
	 * 查询批量开户结果
	 * 
	 **/
	@Override
	public void qryOpenrtResult(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.QryOpenrtResult.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.QryOpenrtResult.Output output) {

		String btchno = input.getBtchno();
		E_OPENRS openrt = input.getOpenrt();
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();

		Long pageno = input.getPageno();
		Long pagesize = input.getPgsize();

		if (CommUtil.isNull(btchno)) {
			throw CaError.Eacct.BNAS0427();
		}
		// long count = 0l;
		Page<EacctBtoprtInfo> btoprtInfoList = SysUtil.getInstance(Page.class);
		long starno = (pageno - 1) * pagesize; // 起始数
		long totlCount = 0;
		// 判断开户结果是否为空，若是则查全部
		if (CommUtil.isNull(openrt)) {
			// 获取分页数据
			btoprtInfoList = CaDao.selOpenrtResult(btchno, corpno, openrt, starno, pagesize, totlCount, true);
			// String totalCount = CaDao.selOpenrtResultCount(btchno, openrt,
			// true);
			// count = Long.parseLong(totalCount);

		} else if (openrt == E_OPENRS.SUCCESS) {
			btoprtInfoList = CaDao.selSuccOpenrtResult(btchno, corpno, starno, pagesize, totlCount, true);
			// String totalCount = CaDao.selSuccOpenrtResultForCount(btchno,
			// true);
			// count = Long.parseLong(totalCount);

		} else if (openrt == E_OPENRS.FAIL) {
			btoprtInfoList = CaDao.selFailOpenrtResult(btchno, corpno, starno, pagesize, totlCount, true);
			// String totalCount = CaDao.selFailOpenrtResultForCount(btchno,
			// true);
			// count = Long.parseLong(totalCount);
		}

		if (btoprtInfoList.getRecordCount() == 0) {
			throw CaError.Eacct.BNAS0426();
		}

		output.setCount(btoprtInfoList.getRecordCount());
		output.getBtoprtInfoList().addAll(btoprtInfoList.getRecords());
		// 设置报文头总记录条数
		CommTools.getBaseRunEnvs().setTotal_count(btoprtInfoList.getRecordCount());
	}

	/**
	 * 查询电子账户分类
	 * 
	 * @param custac
	 *            电子账号ID
	 * @return 账户分类
	 */
	public E_ACCATP qryAccatpByCustac(String custac) {
		E_ACCATP accatp = E_ACCATP.FINANCE;

		// List<E_ACCATP> list = CaDao.selAccatpByCustac(custac, false);
		// if (list.contains(E_ACCATP.GLOBAL)) {
		// return E_ACCATP.GLOBAL;
		// } else if (list.contains(E_ACCATP.FINANCE)) {
		// return E_ACCATP.FINANCE;
		// } else if (list.contains(E_ACCATP.WALLET)) {
		// return E_ACCATP.WALLET;
		// }

		return accatp;
	}

	/**
	 * 
	 * @Title: IoCaStopAc
	 * @Description: 电子账户停用
	 * @param input
	 *            停用条件
	 * @author zhangjunlei
	 * @date 2016年7月7日 上午10:38:44
	 * @version V2.3.0
	 */
	@Override
	public void IoCaStopAc(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.IoCaStopAc.Input input) {

		E_BKUPRS bkuprs = input.getBkuprs();// 停用原因
		String dclrna = input.getDclrna();// 声明人姓名
		String dclrtl = input.getDclrtl();// 声明人联系方式
		String cardno = input.getCardno();// 电子账号
		String remark = input.getRemark();// 备注
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();// 法人代码

		// 停用原因
		if (CommUtil.isNull(bkuprs)) {
			throw CaError.Eacct.BNAS1236();
		}

		// 电子账号
		if (CommUtil.isNull(cardno)) {
			throw DpModuleError.DpstAcct.BNAS0311();
		}

		// 备注
		if (CommUtil.isNull(remark)) {
			throw CaError.Eacct.BNAS1652();
		}

		// 停用原因是客户声明时声明人姓名和声明人联系方式都不能为空
		if (E_BKUPRS.STATE == bkuprs) {
			if (CommUtil.isNull(dclrna) || CommUtil.isNull(dclrtl)) {
				throw CaError.Eacct.BNAS1235();
			}
		}

		// 根据电子账号查询出电子账号ID
		KnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCardno(cardno, E_DPACST.NORMAL, false);

		// 卡客户账号信息
		if (CommUtil.isNull(tblKnaAcdc)) {
			throw CaError.Eacct.BNAS0568();
		}

		String custac = tblKnaAcdc.getCustac();// 电子账号ID

		KnaCust tblKnaCust = CaDao.selKnaCustByCustac(custac, false);// 根据电子账号ID查询电子账户表信息

		// 电子账户信息
		if (CommUtil.isNull(tblKnaCust.getCustac())) {
			throw CaError.Eacct.BNAS0873();
		}

		// 操作柜员只能办理本行社的电子账户停用
		if (!CommUtil.equals(corpno, tblKnaCust.getCorpno())) {
			throw CaError.Eacct.BNAS0795();
		}
		// cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.IoCaSelCaStInfo.Output
		// output = null;
		// 获取电子账户状态
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		// 检查电子账户状态
		if (cuacst == E_CUACST.PREOPEN) {
			throw DpModuleError.DpstComm.BNAS0881();
		} else if (cuacst == E_CUACST.OUTAGE) {
			throw CaError.Eacct.BNAS0858();
		} else if (cuacst == E_CUACST.PRECLOS) {
			throw CaError.Eacct.BNAS0906();
		} else if (cuacst == E_CUACST.CLOSED) {
			throw CaError.Eacct.BNAS0857();
		}

		// 调用DP模块服务查询冻结状态，检查电子账户状态字
		IoDpFrozSvcType froz = SysUtil.getInstance(IoDpFrozSvcType.class);
		IoDpAcStatusWord cplGetAcStWord = froz.getAcStatusWord(custac);
		if (cplGetAcStWord.getPtfroz() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
			throw CaError.Eacct.E0001("电子账户已部冻！");
		}
		if (cplGetAcStWord.getCertdp() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
			throw CaError.Eacct.E0001("电子账户已开立存款证明！");
		}
		if (cplGetAcStWord.getBrfroz() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
			throw CaError.Eacct.E0001("电子账户已借冻！");
		}
		if (cplGetAcStWord.getDbfroz() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
			throw CaError.Eacct.E0001("电子账户已双冻！");
		}
		if (cplGetAcStWord.getBkalsp() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
			throw CaError.Eacct.E0001("电子账户已全止-银行止付！");
		}
		if (cplGetAcStWord.getOtalsp() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
			throw CaError.Eacct.E0001("电子账户已全止-外部止付");
		}
		if (cplGetAcStWord.getPtstop() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
			throw CaError.Eacct.E0001("电子账户已部止！");
		}

		// 客户声明
		if (bkuprs == E_BKUPRS.STATE) {
			// 检查声明人与户名是否一致
			if (!CommUtil.equals(dclrna, tblKnaCust.getCustna())) {
				throw CaError.Eacct.BNAS0360();
			}
			// 客户信息相关内容取消，模块拆分
			// IoCuCustSvcType IoCuCustSvcType = SysUtil.getInstance(IoCuCustSvcType.class);
			// CustInfo tblCifCust = IoCuCustSvcType.selByCustNo(tblKnaCust
			// .getCustno());
			// 检查声明人证件号码是否与客户身份证号码一致
			// bizlog.debug("输入身份证号" + input.getIdtfno() + ",客户身份证号"
			// + tblCifCust.getIdtfno());
			if (CommUtil.compare(input.getIdtftp(), E_IDTFTP.SFZ) == 0
					&& CommUtil.compare(input.getIdtfno(), "") != 0) {
				throw CaError.Eacct.BNAS3001();
			}
		}

		// 查询电子账户分类
		E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac);

		// 电子账户停用登记簿
		KnbCasp KnbCasp = SysUtil.getInstance(KnbCasp.class);
		KnbCasp.setCustac(custac);// 电子账号ID
		KnbCasp.setCustna(tblKnaCust.getCustna());// 客户名称
		KnbCasp.setAccttp(accatp);// 账户分类
		KnbCasp.setBrchno(tblKnaCust.getBrchno());// 归属机构
		String bkupdt = CommTools.getBaseRunEnvs().getTrxn_date();// 停用日期
		KnbCasp.setBkupdt(bkupdt);
		// Long bkuptm = BusiTools.getBusiRunEnvs().getTrantm();//停用时间
		KnbCasp.setBkuptm(BusiTools.getBusiRunEnvs().getTrantm());
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();// 交易流水
		KnbCasp.setTransq(transq);// 交易流水
		KnbCasp.setBkuprs(bkuprs);// 停用原因

		// 停用原因为客户声明时，联系方式为输入接口中的声明联系方式，其他声明不登记联系人手机号
		if (E_BKUPRS.STATE == bkuprs) {
			KnbCasp.setDclrtl(dclrtl);// 声明人联系方式
		}

		// 查询电子账号信息
		KnaCust KnaCust = KnaCustDao.selectOne_odb1(custac, false);

		// 电子账号信息
		if (CommUtil.isNull(KnaCust)) {
			throw CaError.Eacct.BNAS0914();
		}

		String custno = KnaCust.getCustno();// 获取客户号

		// 通过客户号查询客户信息
		// IoSrvCfPerson.IoGetCifCust.InputSetter queryCifCust = SysUtil
		// .getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
		// IoSrvCfPerson.IoGetCifCust.Output custinfo = SysUtil
		// .getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
		// IoSrvCfPerson cifCustServ = SysUtil.getInstance(IoSrvCfPerson.class);
		// queryCifCust.setCustno(custno);
		// cifCustServ.getCifCust(queryCifCust, custinfo);

		// E_IDTFTP idtftp = custinfo.getIdtftp();// 证件类型
		// String idtfno = custinfo.getIdtfno();// 证件号码
		KnbCasp.setDclrna(dclrna);// 声明人姓名
		// KnbCasp.setDcidtp(idtftp);// 声明人证件类型
		// KnbCasp.setDcidno(idtfno);// 声明人证件号码
		String sphdid = CommTools.getBaseRunEnvs().getTrxn_teller();// 操作柜员
		KnbCasp.setUserid(sphdid);
		String ussqno = CommTools.getBaseRunEnvs().getTrxn_seq();// 柜员流水
		KnbCasp.setUssqno(ussqno);
		String spatid = BusiTools.getBusiRunEnvs().getAuthvo().getAuthus();// 授权柜员
		KnbCasp.setAuthus(spatid);
		String authsq = BusiTools.getBusiRunEnvs().getAuthvo().getAuthsq();// 授权柜员流水
		KnbCasp.setAuthsq(authsq);
		KnbCasp.setRemark(remark);// 停用备注
		KnbCasp.setProcst(E_DEALST.UNDEAL);// 处理状态
		KnbCasp.setOdacst(cuacst);// 原客户化状态

		tblKnaCust.setAcctst(E_ACCTST.SLEEP);
		KnaCustDao.updateOne_odb1(tblKnaCust);// 修改账户状态为停用
		KnbCaspDao.insert(KnbCasp);// 登记停用登记簿信息

		// 登记电子账户状态
		IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
		cplDimeInfo.setCustac(tblKnaAcdc.getCustac());
		SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);

		/**
		 * app消息推送
		 * 
		 */

		// MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
		// mri.setMtopic("Q0101005");
		ToAppSendMsg AppSendMsgInput = SysUtil.getInstance(ToAppSendMsg.class);
		// 查询电子账户信息
		CaCustInfo.accoutinfos accoutinfos = SysUtil.getInstance(CaCustInfo.accoutinfos.class);
		accoutinfos = EacctMainDao.selCustInfobyCustac(custac, E_ACALST.NORMAL, E_ACALTP.CELLPHONE, false);

		AppSendMsgInput.setUserId(accoutinfos.getCustid()); // 用户ID
		AppSendMsgInput.setOutNoticeId("Q0101005");// 外部消息ID
		AppSendMsgInput.setNoticeTitle("您的电子账户已停用");// 公告标题
		String date = CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4) + "年"
				+ CommTools.getBaseRunEnvs().getTrxn_date().substring(4, 6) + "月"
				+ CommTools.getBaseRunEnvs().getTrxn_date().substring(6, 8) + "日"
				+ BusiTools.getBusiRunEnvs().getTrantm().substring(0, 2) + ":"
				+ BusiTools.getBusiRunEnvs().getTrantm().substring(2, 4) + ":"
				+ BusiTools.getBusiRunEnvs().getTrantm().substring(4, 6);

		AppSendMsgInput.setContent("您的ThreeBank电子账户已停用。 如有疑问可咨询我行客服，电话0471-96616。"); // 内容
		AppSendMsgInput.setSendtime(date);// 消息生成时间
		AppSendMsgInput.setClickType(cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_CLIKTP.NO);
		AppSendMsgInput.setClickValue("");// 点击动作值
		// AppSendMsgInput.setTirggerSys(CommTools.getBaseRunEnvs().getSystcd());// 触发系统
		AppSendMsgInput.setTransType(cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_APPTTP.CUACCH);

		// mri.setMsgtyp("ApSmsType.ToAppSendMsg");
		// mri.setMsgobj(AppSendMsgInput);
		// AsyncMessageUtil.add(mri);
		CommTools.addMessagessToContext("Q0101005", date);

	}

	/**
	 * 
	 * @Title: IoCaReOpen
	 * @Description: (停用重启)
	 * @param cardno
	 * @param redesc
	 * @author xiongzhao
	 * @date 2016年7月20日 上午9:58:26
	 * @version V2.3.0
	 */
	@Override
	public void IoCaReOpen(String cardno, String redesc) {

		if (CommUtil.isNull(cardno)) {
			throw DpModuleError.DpstAcct.BNAS0311();
		}
		if (CommUtil.isNull(redesc)) {
			throw CaError.Eacct.BNAS1653();
		}

		// 获取custac
		KnaAcdc KnaAcdc = KnaAcdcDao.selectOne_odb2(cardno, false);

		if (CommUtil.isNull(KnaAcdc)) {
			throw CaError.Eacct.BNAS1654();
		}

		// 查询账户信息
		KnaCust KnaCust = KnaCustDao.selectOne_odb1(KnaAcdc.getCustac(), false);

		if (CommUtil.isNull(KnaCust)) {
			throw CaError.Eacct.BNAS1655();
		}

		// 操作柜员只能办理本行社的电子账户重启
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		if (!CommUtil.equals(corpno, KnaCust.getCorpno())) {
			throw CaError.Eacct.BNAS1656();
		}

		E_CUACST eCustac = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(KnaAcdc.getCustac());
		if (eCustac != E_CUACST.OUTAGE) {
			throw CaError.Eacct.BNAS1657();
		}

		KnbCasp tblKnbCasp = SysUtil.getInstance(KnbCasp.class);
		// 查询停用登记簿信息
		List<KnbCasp> lstKnbCasp = KnbCaspDao.selectAll_odb1(KnaAcdc.getCustac(), E_DEALST.UNDEAL, false);
		if (CommUtil.isNotNull(lstKnbCasp) && lstKnbCasp.size() != 1) {
			throw CaError.Eacct.BNAS1000();
		} else {
			tblKnbCasp = lstKnbCasp.get(0);
		}

		if (CommUtil.isNull(tblKnbCasp.getCustac())) {
			throw CaError.Eacct.BNAS0924();
		}

		tblKnbCasp.setProcdt(CommTools.getBaseRunEnvs().getTrxn_date());// 处理日期
		tblKnbCasp.setProctm(BusiTools.getBusiRunEnvs().getTrantm());// 处理时间
		tblKnbCasp.setProcrs(E_RESULT.STATSP);// 处理结果
		tblKnbCasp.setProcst(E_DEALST.PROCED);// 处理状态
		tblKnbCasp.setProcsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 处理流水
		tblKnbCasp.setProcds(redesc);// 重启说明
		tblKnbCasp.setDlhdid(CommTools.getBaseRunEnvs().getTrxn_teller());// 操作柜员
		tblKnbCasp.setDlussq(CommTools.getBaseRunEnvs().getTrxn_seq());// 柜员流水
		tblKnbCasp.setDlatid(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus());// 授权柜员
		tblKnbCasp.setDlatsq(BusiTools.getBusiRunEnvs().getAuthvo().getAuthsq());// 授权柜员流水

		KnbCaspDao.updateOne_odb3(tblKnbCasp);// 修改停用登记簿信息

		KnaCust.setAcctst(E_ACCTST.NORMAL);

		KnaCustDao.updateOne_odb1(KnaCust);// 修改账户状态为正常

		// 登记电子账户状态
		if (CommUtil.isNull(tblKnbCasp.getOdacst())) {
			throw CaError.Eacct.BNAS1658();
		} else {
			IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
			cplDimeInfo.setCustac(KnaAcdc.getCustac());
			cplDimeInfo.setDime01(tblKnbCasp.getOdacst().getValue());
			SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);
		}

	}

	@Override
	public void IoCaCkdsac(String cardno,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.IoCaCkdsac.Output output) {
		if (CommUtil.isNull(cardno)) {
			throw DpModuleError.DpstAcct.BNAS0311();
		}

		// 根据电子账号查询出电子账号ID
		KnaAcdc KnaAcdc = KnaAcdcDao.selectOne_odb2(cardno, false);
		if (CommUtil.isNull(KnaAcdc) || KnaAcdc.getStatus() == E_DPACST.CLOSE) {
			throw CaError.Eacct.BNAS1659();
		}

		String custac = KnaAcdc.getCustac();

		// 根据电子账号查询电子账户表信息
		KnaCust KnaCust = KnaCustDao.selectOne_odb1(custac, false);

		// 操作柜员只能办理本行社的电子账户停用
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		if (!CommUtil.equals(corpno, KnaCust.getCorpno())) {
			throw CaError.Eacct.BNAS0794();
		}

		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		// 检查电子账户状态
		if (cuacst != E_CUACST.OUTAGE) {
			throw CaError.Eacct.BNAS0877();
		}

		// 调用DP模块服务查询冻结状态，检查电子账户状态字
		IoDpFrozSvcType froz = SysUtil.getInstance(IoDpFrozSvcType.class);
		IoDpAcStatusWord cplGetAcStWord = froz.getAcStatusWord(custac);
		if (cplGetAcStWord.getBrfroz() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
			throw CaError.Eacct.BNAS0866();
		}
		if (cplGetAcStWord.getDbfroz() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
			throw CaError.Eacct.BNAS0859();
		}
		if (cplGetAcStWord.getPtfroz() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
			throw CaError.Eacct.BNAS0869();
		}
		if (cplGetAcStWord.getBkalsp() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
			throw CaError.Eacct.BNAS0861();
		}
		if (cplGetAcStWord.getOtalsp() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
			throw CaError.Eacct.BNAS0862();
		}
		if (cplGetAcStWord.getPtstop() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
			throw CaError.Eacct.BNAS0868();
		}
		if (cplGetAcStWord.getCertdp() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0865();
		}

		// 检查是否有未领取红包
		IoCaOtherService.IoCaChkRedpack.InputSetter chkIN = SysUtil
				.getInstance(IoCaOtherService.IoCaChkRedpack.InputSetter.class);
		IoCaOtherService.IoCaChkRedpack.Output chkOT = SysUtil
				.getInstance(IoCaOtherService.IoCaChkRedpack.Output.class);

		chkIN.setAcctno(cardno);

		IoCaOtherService dubboSrv = SysUtil.getInstanceProxyByBind(IoCaOtherService.class, "otsevdb");
		try {
			dubboSrv.chkRedpack(chkIN, chkOT);
		} catch (Exception e) {

			throw CaError.Eacct.BNAS1660();

		}

		if (chkOT.getExisfg() == BaseEnumType.E_YES___.YES) {
			throw DpModuleError.DpstComm.E9999(chkOT.getReason());
		}

		// 查询电子账户分类
		E_ACCATP acctap = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac);

		// 查询转久悬余额
		DpAcctSvcType dpAcctSvcType = SysUtil.getInstance(DpAcctSvcType.class);

		IoDpClsChkIN chkin = SysUtil.getInstance(IoDpClsChkIN.class);

		chkin.setCustac(custac);
		chkin.setIssett(cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.NO);

		IoDpClsChkOT dpClsChkOT = dpAcctSvcType.TestInterest(chkin);

		BigDecimal propam = dpClsChkOT.getTotlam();

		output.setPropam(propam);
		output.setAccttp(acctap);
		output.setCustna(KnaCust.getCustna());
		output.setBrchno(KnaCust.getBrchno());

	}

	/**
	 * 
	 * @author renjinghua
	 *         <p>
	 *         <li>2016年7月15日-上午9:48:29
	 *         <li>
	 *         <li>功能描述：电子账户状态更新服务，根据传入的参数维度确定电子账户内部状态及客户化状态更新情况</li>
	 *         </p>
	 * 
	 * @param cplDimeInfo
	 *            更新状态输入
	 * @return 更新状态输出
	 */
	@Override
	public IoCaUpdAcctstOut updCustAcctst(IoCaUpdAcctstIn cplDimeInfo) {
		IoCaUpdAcctstOut cplAcctstOut = SysUtil.getInstance(IoCaUpdAcctstOut.class);

		// ---开户
		// opaccd（开户） ： 3维度 ； 维度1：账户类型； 维度2：身份核查结果 ； 维度3：人脸识别结果
		// btopac （批量开户）： 3维度 ； 维度1：账户类型； 维度2：身份核查结果 ； 维度3：人脸识别结果

		// ---启用
		// enable （账号启用） ：1维度； 维度1：账户类型

		// ---入金
		// tranfe (电子账户出入金（转账）) ： 1维度: 维度1：处理前状态 TODO 貌似非同名转三类户成功
		// geacct (通用记账交易) ： 1维度: 维度1：处理前状态 TODO 此交易报错，是否不支持三类户

		// ---休眠
		// acslep (电子账户休眠) ： 0维度

		// ---销户
		// clacbt （统一后管销户） : 1维度； 维度1： 转入账号类型
		// disabl （停用转久悬） : 1维度； 维度1： 转入账号类型
		// clacct （移动前端销户） : 1维度； 维度1： 转入账号类型

		// 停用
		// stopal （电子账户停用）： 0维度

		// 重启
		// reopen （电子账户停用重启） ：1 维度；维度1： 客户化状态

		// 作废
		// ckrtcl （核查未返回超时处理【开户】）0 维度
		// ckopac （人脸识别核查结果确认【开户】）1维度 ；维度1：传入人脸识别结果
		// ckrtac （身份核查结果确认【开户】）1维度 ； 维度1：传入身份核查结果

		// 变更机构
		// chngbr (电子账户机构变更) ： 1维度； 维度1：电子账户类型 TODO

		// 亲情账户创建
		// famwal (亲情账户创建) ： 3维度 ； 维度1：账户类型；

		// String prcscd = BusiTools.getBusiRunEnvs().getLttscd(); // 交易码
		String prcscd = CommTools.getBaseRunEnvs().getTrxn_code(); // 交易码
		String custac = cplDimeInfo.getCustac(); // 电子账号
		String dime01 = null; // 维度1
		String dime02 = null; // 维度2
		String dime03 = null; // 维度3
		String dime04 = null; // 维度4
		String dime05 = null; // 维度5
		String dime06 = null; // 维度6
		String custno = cplDimeInfo.getCustno(); // 用户ID
		cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___ facesg = cplDimeInfo.getFacesg();// 面签标识
		if (CommUtil.isNull(facesg)) {
			facesg = cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.NO;
		}

		// 如果账户分类为空，则取全部%
		if (CommUtil.isNull(custac)) {
			throw DpModuleError.DpstAcct.BNAS0311();
		}

		// 获取维度值
		if (CommUtil.isNull(cplDimeInfo.getDime01())) {
			dime01 = "%";
		} else {
			dime01 = cplDimeInfo.getDime01();
		}

		if (CommUtil.isNull(cplDimeInfo.getDime02())) {
			dime02 = "%";
		} else {
			dime02 = cplDimeInfo.getDime02();
		}

		if (CommUtil.isNull(cplDimeInfo.getDime03())) {
			dime03 = "%";
		} else {
			dime03 = cplDimeInfo.getDime03();
		}

		if (CommUtil.isNull(cplDimeInfo.getDime04())) {
			dime04 = "%";
		} else {
			dime04 = cplDimeInfo.getDime04();
		}

		if (CommUtil.isNull(cplDimeInfo.getDime05())) {
			dime05 = "%";
		} else {
			dime05 = cplDimeInfo.getDime05();
		}

		if (CommUtil.isNull(cplDimeInfo.getDime06())) {
			dime06 = "%";
		} else {
			dime06 = cplDimeInfo.getDime06();
		}

		// 查询电子账户状态参数化信息
		// KnpAcst tbl = KnpAcstDao.selectOne_odb1(prcscd, dime01, dime02,
		// dime03, dime04, dime05, dime06, false);
		KnpAcst tbl = EacctMainDao.selKnpAcstByIdx1(prcscd, CommTools.getBaseRunEnvs().getBusi_org_id(), dime01, dime02,
				dime03, dime04, dime05, dime06, false);
		// 如果状态参数表中新状态为空，则报错
		if (CommUtil.isNull(tbl) || CommUtil.isNull(tbl.getCuacst())) {
			throw CaError.Eacct.E0016();
		}

		// 更新电子账户信息
		KnaCuad tblKnaCuad = KnaCuadDao.selectOne_knaCuadOdx1(custac, false);

		if (CommUtil.isNotNull(tblKnaCuad) && cplDimeInfo.getIsstrk() != BaseEnumType.E_YES___.NO) {
			/**
			 * 冲正登记
			 */
			IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
			cplInput.setCustac(tblKnaCuad.getCustac());// 电子账号
			cplInput.setTranev(ApUtil.TRANS_EVENT_UPACST);// 交易事件
			cplInput.setEvent1(tblKnaCuad.getDime01());// 维度1
			cplInput.setEvent2(tblKnaCuad.getDime02());// 维度2
			cplInput.setEvent3(tblKnaCuad.getDime03());// 维度3
			cplInput.setEvent4(tblKnaCuad.getDime04());// 维度4
			cplInput.setEvent5(tblKnaCuad.getDime05());// 维度5
			cplInput.setEvent6(tblKnaCuad.getDime06());// 维度6
			cplInput.setEvent7(tblKnaCuad.getPrcscd());// 交易码
			if (CommUtil.isNotNull(tblKnaCuad.getFacesg())) {
				cplInput.setEvent8(tblKnaCuad.getFacesg().getValue());// 面签标识
			}

			// ApStrike.regBook(cplInput);
			IoMsRegEvent input = SysUtil.getInstance(IoMsRegEvent.class);
			input.setReversal_event_id(ApUtil.TRANS_EVENT_UPACST);
			input.setInformation_value(SysUtil.serialize(cplInput));
			MsEvent.register(input, true);
		}

		boolean insFlag = false; // 新增标志
		if (CommUtil.isNull(tblKnaCuad) || CommUtil.isNull(tblKnaCuad.getCustac())) {

			// 记录不存在，新增
			tblKnaCuad = SysUtil.getInstance(KnaCuad.class);
			tblKnaCuad.setCustac(custac); // 新增时，需设置电子账号
			insFlag = true;
		}

		tblKnaCuad.setPrcscd(prcscd);
		tblKnaCuad.setDime01(dime01);
		tblKnaCuad.setDime02(dime02);
		tblKnaCuad.setDime03(dime03);
		tblKnaCuad.setDime04(dime04);
		tblKnaCuad.setDime05(dime05);
		tblKnaCuad.setDime06(dime06);
		// tblKnaCuad.setMtdate(CommTools.getBaseRunEnvs().getTrxn_date());

		if (!insFlag) {
			// 面签标识不为1-已面签时，更新面签标识
			if (cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES != tblKnaCuad.getFacesg()) {
				tblKnaCuad.setFacesg(facesg);
			}

			KnaCuadDao.updateOne_knaCuadOdx1(tblKnaCuad); // 记录存在，更新

		} else {

			tblKnaCuad.setFacesg(facesg);// 面签标识
			tblKnaCuad.setCustno(custno); // 用户ID
			KnaCuadDao.insert(tblKnaCuad); // 记录不存在，新增
		}

		// 设置输出参数
		cplAcctstOut.setCuacst(tbl.getCuacst()); // 客户化状态
		return cplAcctstOut;
	}

	@Override
	public String getCardnoByIdtftpIdtfno(E_IDTFTP idtftp, String idtfno) {

		String cardno = null;

		cardno = EacctMainDao.selCardnoByIdtftpIdtfno(idtftp, idtfno, false);

		if (CommUtil.isNull(cardno)) {
			throw CaError.Eacct.BNAS1661();
		}

		return cardno;
	}

	@Override
	public void ioCaSelCaEcInfo(qrbkCaIn qrbkCaIn,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.IoCaSelCaEcInfo.Output output) {

		int totlCount = 0; // 记录总数

		int startno = (qrbkCaIn.getPageno() - 1) * qrbkCaIn.getPgsize();// 起始记录数

		// 机构号
		String brchno = qrbkCaIn.getTranbr();

		// 起始日期
		String bgdate = CommUtil.isNotNull(qrbkCaIn.getStdate()) ? qrbkCaIn.getStdate() : "";

		// 终止日期
		String endate = CommUtil.isNotNull(qrbkCaIn.getEndate()) ? qrbkCaIn.getEndate() : "";

		// 电子账号
		String cardno = qrbkCaIn.getQrycon() == E_QRYCON.ACCOUNT ? qrbkCaIn.getQryval() : "";

		// 证件号码
		String idtfno = qrbkCaIn.getQrycon() == E_QRYCON.PAPERS
				? qrbkCaIn.getQryval().substring(3, qrbkCaIn.getQryval().length())
				: "";

		// 销户金额
		BigDecimal tranam = qrbkCaIn.getQrycon() == E_QRYCON.AM ? ConvertUtil.toBigDecimal(qrbkCaIn.getQryval()) : null;

		// 根据电子账号获取电子账号ID
		if (CommUtil.isNotNull(cardno)) {

			IoCaKnaAcdc caKnaAcdc = CaDao.selKnaAcdcByCard(cardno, false);

			if (CommUtil.isNull(caKnaAcdc)) {

				throw DpModuleError.DpstComm.BNAS0692();
			}

		}

		// 根据证件号码获取电子账号ID
		if (CommUtil.isNotNull(idtfno)) {
			// 1.根据证件类型和证件号码获取客户号

			// 证件种类
			E_IDTFTP idtftp = CommUtil.toEnum(E_IDTFTP.class, qrbkCaIn.getQryval().substring(0, 3));
			if (qrbkCaIn.getWarntp() == E_WARNTP.CLOSING) {
				// 根据证件类型证件号码查询电子账户表
				IoCaKnaCust cplKnaCust = EacctMainDao.selByCusInfoFirst(idtftp, idtfno, false);

				// 检查查询记录是否为空
				if (CommUtil.isNull(cplKnaCust)) {
					throw CaError.Eacct.BNAS0389();
				}

				IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class)
						.getKnaAcdcOdb1(cplKnaCust.getCustac(), E_DPACST.CLOSE, true);
				cardno = tblKnaAcdc.getCardno();

			} else {

				// 根据证件类型证件号码查询电子账户表
				IoCaKnaCust cplKnaCust = EacctMainDao.selByCusInfo(idtftp, idtfno, false);

				// 检查查询记录是否为空
				if (CommUtil.isNull(cplKnaCust)) {
					throw CaError.Eacct.BNAS0389();
				}

				IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class)
						.getKnaAcdcOdb1(cplKnaCust.getCustac(), E_DPACST.NORMAL, true);
				cardno = tblKnaAcdc.getCardno();
			}
		}
		if (qrbkCaIn.getWarntp() == E_WARNTP.CLOSING) {

			Page<qrbkCaOut> pageKnbClacOuts = CaDao.selCloseAbnormal(brchno, bgdate, endate, cardno, tranam, startno,
					qrbkCaIn.getPgsize(), totlCount, false);

			output.getQrbkCaInfoList().addAll(pageKnbClacOuts.getRecords());

			output.setCounts(ConvertUtil.toInteger(pageKnbClacOuts.getRecordCount()));

			// 设置报文头总记录条数
			CommTools.getBaseRunEnvs().setTotal_count(pageKnbClacOuts.getRecordCount());
		} else {
			Page<qrbkCaOut> pageKnbClacOuts = CaDao.selTransNormal(brchno, bgdate, endate, cardno, tranam, startno,
					qrbkCaIn.getPgsize(), totlCount, false);

			output.getQrbkCaInfoList().addAll(pageKnbClacOuts.getRecords());

			output.setCounts(ConvertUtil.toInteger(pageKnbClacOuts.getRecordCount()));
			CommTools.getBaseRunEnvs().setTotal_count(pageKnbClacOuts.getRecordCount());
		}

	}

	/**
	 * @Author chenjk
	 *         <p>
	 *         <li>功能说明：电子账号状态查询</li>
	 *         </p>
	 * @param custac
	 *            电子账号
	 * @param output
	 *            输出信息
	 * @return
	 */
	/*
	 * @Override public void selCaStInfo( String custac, cn.sunline.ltts.busi.iobus
	 * .servicetype.dp.IoCaSrvGenEAccountInfo.IoCaSelCaStInfo.Output output) {
	 * 
	 * if(CommUtil.isNull(custac)){ throw CaError.Eacct.E0001("输入电子账号不能为空"); }
	 * 
	 * //维度信息查询 KnaCuad tblKnaCuad = KnaCuadDao.selectOne_KnaCuad_odx1(custac,
	 * false);
	 * 
	 * if(CommUtil.isNull(tblKnaCuad) || CommUtil.isNull(tblKnaCuad.getCustac())){
	 * //查询为空，直接返回 return; }
	 * 
	 * //状态信息查询 //KnpAcst tbl = KnpAcstDao.selectOne_odb1(tblKnaCuad.getPrcscd(),
	 * tblKnaCuad.getDime01(), tblKnaCuad.getDime02(), tblKnaCuad.getDime03(),
	 * tblKnaCuad.getDime04(), tblKnaCuad.getDime05(), tblKnaCuad.getDime06(),
	 * false); KnpAcst tbl = EacctMainDao.selByIdx1(tblKnaCuad.getPrcscd(),
	 * tblKnaCuad.getDime01(), tblKnaCuad.getDime02(), tblKnaCuad.getDime03(),
	 * tblKnaCuad.getDime04(), tblKnaCuad.getDime05(), tblKnaCuad.getDime06(),
	 * false); output.setCuacst(tbl.getCuacst());
	 * 
	 * }
	 */

	@Override
	public E_CUACST selCaStInfo(String custac) {
		if (CommUtil.isNull(custac)) {
			throw DpModuleError.DpstAcct.BNAS0311();
		}

		// 维度信息查询
		KnaCuad tblKnaCuad = KnaCuadDao.selectOne_knaCuadOdx1(custac, false);

		if (CommUtil.isNull(tblKnaCuad) || CommUtil.isNull(tblKnaCuad.getCustac())) {
			// 查询为空，直接返回
			return null;
		}

		// 状态信息查询
		// KnpAcst tbl = KnpAcstDao.selectOne_odb1(tblKnaCuad.getPrcscd(),
		// tblKnaCuad.getDime01(), tblKnaCuad.getDime02(),
		// tblKnaCuad.getDime03(), tblKnaCuad.getDime04(),
		// tblKnaCuad.getDime05(), tblKnaCuad.getDime06(), false);
		KnpAcst tbl = EacctMainDao.selKnpAcstByIdx1(tblKnaCuad.getPrcscd(), CommTools.getBaseRunEnvs().getBusi_org_id(),
				tblKnaCuad.getDime01(), tblKnaCuad.getDime02(), tblKnaCuad.getDime03(), tblKnaCuad.getDime04(),
				tblKnaCuad.getDime05(), tblKnaCuad.getDime06(), false);

		if (CommUtil.isNull(tbl)) {
			throw DpModuleError.DpstFroz.AT021919();
		}

		return tbl.getCuacst();
		// output.setCuacst(tbl.getCuacst());

	}

	/**
	 * 
	 * @Title: selMesgInfo
	 * @Description: (查询短信发送所需账户信息)
	 * @param cardno
	 * @param custac
	 * @return
	 * @author xiongzhao
	 * @date 2016年9月22日 下午8:20:38
	 * @version V2.3.0
	 */
	@Override
	public IoCaQryMesgOut selMesgInfo(String custac, String trandt, String trantm) {

		String cardno = null;// 虚拟卡号
		String lastnm = null;// 尾号

		IoCaQryMesgOut cplMesgInfo = SysUtil.getInstance(IoCaQryMesgOut.class);

		// 查询电子账户状态
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);

		// 查询电子账号归属机构
		KnaCust tblKnaCust = CaDao.selKnaCustByCustac(custac, false);

		// 查询电子账户虚拟卡号
		KnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCustac(custac, false);
		if (CommUtil.isNotNull(tblKnaAcdc)) {
			cardno = tblKnaAcdc.getCardno();
			lastnm = cardno.substring(cardno.length() - 4, cardno.length());
		}

		// 查询机构名
		IoBrchInfo cplBrchInfo = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(tblKnaCust.getBrchno());

		if (cuacst == E_CUACST.NOENABLE) {
			KnbOpbt tblKnbOpbt = CaDao.selKnbOpbtByCuacst(custac, false);
			if (CommUtil.isNotNull(tblKnbOpbt)) {
				cplMesgInfo.setAcalno(tblKnbOpbt.getTeleno());// 手机号
			}
		} else {
			// 查询绑定手机号
			KnaAcal tblKnaAcal = null;
			tblKnaAcal = CaDao.selKnaAcalByCustac(custac, E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);
			if (CommUtil.isNull(tblKnaAcal)) {
				tblKnaAcal = CaDao.selKnaAcalByCustac(custac, E_ACALTP.CELLPHONE, E_ACALST.INVALID, false);
			}

			if (CommUtil.isNotNull(tblKnaAcal)) {
				cplMesgInfo.setAcalno(tblKnaAcal.getTlphno());// 手机号
			}
		}

		// 查询用户ID
		KnaCuad tblKnaCuad = CaDao.selKnaCuadByCustac(custac, false);
		if (CommUtil.isNotNull(tblKnaCuad)) {
			cplMesgInfo.setCustid(tblKnaCuad.getCustno());// 用户ID
		}

		cplMesgInfo.setBrchno(tblKnaCust.getBrchno());// 电子账号归属机构
		cplMesgInfo.setBrchna(cplBrchInfo.getBrchna());// 机构名
		cplMesgInfo.setCustna(tblKnaCust.getCustna());// 客户名称
		cplMesgInfo.setTrayea(trandt.substring(0, 4));// 年
		cplMesgInfo.setTramon(trandt.substring(4, 6));// 月
		cplMesgInfo.setTraday(trandt.substring(6, 8));// 日
		cplMesgInfo.setTrahou(trantm.substring(0, 2));// 时193850
		cplMesgInfo.setTramin(trantm.substring(2, 4));// 分
		cplMesgInfo.setTrasec(trantm.substring(4, 6));// 秒
		cplMesgInfo.setLastnm(lastnm);// 尾号

		return cplMesgInfo;
	}

	@Override
	public void procUpAcctst(IoApCaUpacstIn upacstInput) {

		String prcscd = upacstInput.getPrcscd(); // 交易码
		String custac = upacstInput.getCustac(); // 电子账号
		String dime01 = upacstInput.getDime01(); // 维度1
		String dime02 = upacstInput.getDime02(); // 维度2
		String dime03 = upacstInput.getDime03(); // 维度3
		String dime04 = upacstInput.getDime04(); // 维度4
		String dime05 = upacstInput.getDime05(); // 维度5
		String dime06 = upacstInput.getDime06(); // 维度6
		cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___ facesg = upacstInput.getFacesg(); // 面签标识

		// 查询电子账户状态参数化信息
		// KnpAcst tbl = KnpAcstDao.selectOne_odb1(prcscd, dime01, dime02,
		// dime03, dime04, dime05, dime06, false);
		KnpAcst tbl = EacctMainDao.selKnpAcstByIdx1(prcscd, CommTools.getBaseRunEnvs().getBusi_org_id(), dime01, dime02,
				dime03, dime04, dime05, dime06, false);
		// 如果状态参数表中新状态为空，则报错
		if (CommUtil.isNull(tbl) || CommUtil.isNull(tbl.getCuacst())) {
			throw CaError.Eacct.E0016();
		}

		// 更新电子账户信息
		KnaCuad tblKnaCuad = KnaCuadDao.selectOne_knaCuadOdx1(custac, false);

		boolean insFlag = false; // 新增标志
		if (CommUtil.isNull(tblKnaCuad) || CommUtil.isNull(tblKnaCuad.getCustac())) {

			// 记录不存在，新增
			tblKnaCuad = SysUtil.getInstance(KnaCuad.class);
			tblKnaCuad.setCustac(custac); // 新增时，需设置电子账号
			insFlag = true;
		}

		tblKnaCuad.setPrcscd(prcscd);
		tblKnaCuad.setDime01(dime01);
		tblKnaCuad.setDime02(dime02);
		tblKnaCuad.setDime03(dime03);
		tblKnaCuad.setDime04(dime04);
		tblKnaCuad.setDime05(dime05);
		tblKnaCuad.setDime06(dime06);
		tblKnaCuad.setFacesg(facesg);
		// tblKnaCuad.setMtdate(CommTools.getBaseRunEnvs().getTrxn_date());

		if (!insFlag) {

			KnaCuadDao.updateOne_knaCuadOdx1(tblKnaCuad); // 记录存在，更新
		} else {

			KnaCuadDao.insert(tblKnaCuad); // 记录不存在，新增
		}

	}

	/**
	 * 
	 * @Title: IoCatrantpControl
	 * @Description: 交易账户类型控制
	 * @param input
	 * @author songkailei
	 * @date 2016年12月14日 上午10:51:48
	 * @version V2.3.0
	 */
	@Override
	public void IoCatrantpControl(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.IoCatrantpControl.Input input) {

		String prcscd = input.getPrcscd();// 交易码
		String servtp = input.getServtp();// 渠道
		String crcycd = input.getCrcycd();// 币种
		E_ACCATP accttp = input.getAccttp();// 账户分类
		E_CSTRFG cstrfg = input.getCstrfg();// 现转标志
		E_CSEXTG csextg = input.getCsextg();// 钞汇标识
		cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___ fatofa = input.getFatofa();// 面签标识
		E_INOTFG inotfg = input.getInotfg();// 转出转入标志
		cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___ isbind = input.getIsbind();// 是否绑定账户
		cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___ isthbk = input.getIsthbk();// 是否本行账户
		String diem01 = input.getDiem01();// 自定义维度1
		String diem02 = input.getDiem02();// 自定义维度2
		String diem03 = input.getDiem03();// 自定义维度3
		String diem04 = input.getDiem04();// 自定义维度4
		String diem05 = input.getDiem05();// 自定义维度5

		// 交易码
		if (CommUtil.isNull(prcscd)) {
			prcscd = CommTools.getBaseRunEnvs().getTrxn_code();
		}
		// 渠道
		if (CommUtil.isNull(servtp)) {
			servtp = CommTools.getBaseRunEnvs().getChannel_id();
		}
		// 币种
		if (CommUtil.isNull(crcycd)) {
			crcycd = BusiTools.getDefineCurrency();// 默认本外合计
		}
		// 账户分类
		if (CommUtil.isNull(accttp)) {
			throw CaError.Eacct.BNAS1662();
		}
		// 现转标志
		if (CommUtil.isNull(cstrfg)) {
			throw CaError.Eacct.BNAS1663();
		}
		// 钞汇标识
		if (CommUtil.isNull(csextg)) {
			throw CaError.Eacct.BNAS1664();
		}
		// 转出转入标志
		if (CommUtil.isNull(inotfg)) {
			inotfg = E_INOTFG.ALL;// 默认不分转入转出
			// throw CaError.Eacct.E0001("转出转入标志不能为空！");
		}
		// 是否绑定账户
		if (CommUtil.isNull(isbind)) {
			throw CaError.Eacct.BNAS1665();
		}
		// 是否本行账户
		if (CommUtil.isNull(isthbk)) {
			isthbk = cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES;// 默认本行卡
			// throw CaError.Eacct.E0001("是否本行账户不能为空！");
		}
		// 面签标识
		if (CommUtil.isNull(fatofa)) {
			fatofa = cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.NO;// 默认未面签
		}

		// 自定义维度1
		if (CommUtil.isNull(diem01)) {
			diem01 = "%";
		}
		// 自定义维度2
		if (CommUtil.isNull(diem02)) {
			diem02 = "%";
		}
		// 自定义维度3
		if (CommUtil.isNull(diem03)) {
			diem03 = "%";
		}
		// 自定义维度4
		if (CommUtil.isNull(diem04)) {
			diem04 = "%";
		}
		// 自定义维度5
		if (CommUtil.isNull(diem05)) {
			diem05 = "%";
		}

		KnaTrtp tblknatrtp = KnaTrtpDao.selectOne_odb1(prcscd, fatofa, servtp, crcycd, accttp, cstrfg, csextg, inotfg,
				isbind, isthbk, diem01, diem02, diem03, diem04, diem05, false);

		if (CommUtil.isNotNull(tblknatrtp)) {
			if (tblknatrtp.getIsctfg() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
				throw CaError.Eacct.BNAS1666();
			}
		}
	}

	public cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEAccountOut IoCaAddMRAcctount(
			final cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEAccountIn cplAddEAccountIn) {

		// 获取输出接口实例
		IoCaAddEAccountOut cplAddEAccountOut = SysUtil.getInstance(IoCaAddEAccountOut.class);
		// 产生电子账户信息
		cplAddEAccountOut = CaEAccountProc.addEAccountInfo(cplAddEAccountIn);
		// 后台开户服务，不校验绑卡信息，身份核查，人脸识别
		if (cplAddEAccountIn.getIsopcd() != BaseEnumType.E_YES___.NO) {

			// 产生卡信息，并返回卡号
			String cardno = CaCardProc.prcCardInfo(cplAddEAccountIn, cplAddEAccountOut);

			// 设置卡号到输出接口
			cplAddEAccountOut.setCardno(cardno);
			// 建立电子账号与卡号关联关系
			CaCardProc.prcEacctCardLink(cplAddEAccountOut.getCustac(), cplAddEAccountOut.getCardno());

			// 登记开户登记簿
			IoCaOpenAccInfo cplOpenAccInfo = SysUtil.getInstance(IoCaOpenAccInfo.class);
			cplOpenAccInfo.setCustac(cplAddEAccountOut.getCustac());
			cplOpenAccInfo.setAccttp(cplAddEAccountIn.getAccttp());
			cplOpenAccInfo.setCustna(cplAddEAccountIn.getCustna());
			cplOpenAccInfo.setCrcycd(cplAddEAccountIn.getCrcycd());
			cplOpenAccInfo.setUschnl(cplAddEAccountIn.getUschnl());// 渠道
			if (CommUtil.equals(cplAddEAccountIn.getCrcycd(), BusiTools.getDefineCurrency())) {
				cplOpenAccInfo.setCsextg(E_CSEXTG.CASH);
			} else {
				cplOpenAccInfo.setCsextg(cplAddEAccountIn.getCsextg());
			}
			cplOpenAccInfo.setTlphno(cplAddEAccountIn.getTlphno());
			cplOpenAccInfo.setBrchno(cplAddEAccountOut.getBrchno());
			cplOpenAccInfo.setOpendt(cplAddEAccountOut.getOpendt());
			cplOpenAccInfo.setOpensq(cplAddEAccountOut.getOpensq());
			CaEAccountProc.addOpenAcctBook(cplOpenAccInfo);

		}
		// }else{
		// 设置卡号到输出接口
		// cplAddEAccountOut.setCardno(tblKnaAcdc.getCardno());
		// }

		// 登记客户化状态
		IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
		cplDimeInfo.setCustac(cplAddEAccountOut.getCustac());
		if (CommUtil.isNotNull(cplAddEAccountIn.getAccttp())) {
			cplDimeInfo.setDime01(cplAddEAccountIn.getAccttp().getValue()); // 维度1
																			// 账户类型
		}
		if (CommUtil.isNotNull(cplAddEAccountIn.getIdckrt())) {
			cplDimeInfo.setDime02(cplAddEAccountIn.getIdckrt().getValue()); // 维度2
		}
		if (CommUtil.isNotNull(cplAddEAccountIn.getMpckrt())) {
			cplDimeInfo.setDime03(cplAddEAccountIn.getMpckrt().getValue()); // 维度3
		}
		cplDimeInfo.setCustno(cplAddEAccountIn.getCustno()); // 客户ID
		cplDimeInfo.setFacesg(cplAddEAccountIn.getFacesg()); // 面签标识
		updCustAcctst(cplDimeInfo);

		/**
		 * 注释路由注册 by xj20180501
		 */
		// // 注册账号路由表
		// ApAcctRoutTools.register(cplAddEAccountOut.getCustac(),
		// E_ACCTROUTTYPE.CUSTAC);

		return cplAddEAccountOut;

	}

	public void traninCheck(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.TraninCheck.Input input) {
		bizlog.debug("客户账号状态检查开始==========");

		// 调入电子账号信息
		IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb2(input.getInacno(),
				false);
		if (CommUtil.isNull(tblKnaAcdc)) {
			throw InError.comm.E0003("该转入账号[" + input.getInacno() + "]不存在！");
		}
		E_CUACST status = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(tblKnaAcdc.getCustac());// 查询电子账户状态信息
		if (status == E_CUACST.PREOPEN || status == E_CUACST.CLOSED || status == E_CUACST.DELETE
				|| status == E_CUACST.PRECLOS || status == E_CUACST.OUTAGE) {
			throw PbError.PbComm.E2015("该转入账号[" + input.getInacno() + "]状态为[" + status.getLongName() + "]，不允许交易！");
		}
		IoDpAcStatusWord cplGetAcStWord = SysUtil.getInstance(IoDpFrozSvcType.class)
				.getAcStatusWord(tblKnaAcdc.getCustac());
		if (cplGetAcStWord.getDbfroz() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
			throw DpModuleError.DpstAcct.E9999("交易失败，已被冻结！");
		}
		IoCaKnaCust tblKnaCust = SysUtil.getInstance(IoCaSevQryTableInfo.class)
				.getKnaCustByCustacOdb1(tblKnaAcdc.getCustac(), true);
		bizlog.debug("注册户名为：" + tblKnaCust.getCustna() + ",输入户名为：" + input.getInacna());

		if (!CommUtil.equals(tblKnaCust.getCustna(), input.getInacna())) {
			throw InError.comm.E0003("输入户名[" + input.getInacna() + "]与客户账号注册户名[" + tblKnaCust.getCustna() + "]不匹配");
		}
		bizlog.debug("客户账号状态检查结束==========");

	}

	public void checkNestbkStatus(String custac, String prtrsq,
			cn.sunline.ltts.busi.sys.type.InEnumType.E_REBUWA rebuwo,
			cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD amntcd, java.math.BigDecimal happbl) {

		if (CommUtil.isNotNull(prtrsq)) {
			// 电子账户校验流水
			checkAcsq(custac, prtrsq, amntcd, happbl);
		} else {
			throw InError.comm.E0003("内部账转客户账冲正必须输入原交易流水");
		}

		// 校验电子账户状态
		checkTrans(custac, amntcd, rebuwo, happbl);
	}

	/**
	 * 
	 * @Auther xionglz
	 *         <p>
	 *         <li>2017年12月09日-上午11:27:40</li>
	 *         <li>功能说明：电子账户调账控制</li>
	 *         <p>
	 * 
	 * @param
	 */
	private void checkTrans(String custac, E_AMNTCD amntcd, E_REBUWA rebuwa, BigDecimal happal) {
		// 查询电子账户状态信息
		E_CUACST status = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		// 查询电子账户状态字
		IoDpAcStatusWord cplGetAcStWord = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
		// 查询电子账户卡信息
		IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb1(custac, E_DPACST.NORMAL,
				false);
		if (CommUtil.isNull(tblKnaAcdc) || CommUtil.isNull(tblKnaAcdc.getCardno())) {
			throw DpModuleError.DpstAcct.BNAS1695();
		}
		// 电子账户状态字为双冻
		if (cplGetAcStWord.getDbfroz() == BaseEnumType.E_YES___.YES) {
			throw DpModuleError.DpstAcct.BNAS1696(tblKnaAcdc.getCardno());
		} else if (amntcd == E_AMNTCD.DR && rebuwa == E_REBUWA.B || amntcd == E_AMNTCD.CR && rebuwa == E_REBUWA.R) {// 方向为借方蓝字或者贷方红字
			if (cplGetAcStWord.getOtalsp() == BaseEnumType.E_YES___.YES
					|| cplGetAcStWord.getBrfroz() == BaseEnumType.E_YES___.YES) {// 电子账户状态字为全止或借冻
				throw DpModuleError.DpstAcct.BNAS3005();
			} else {// 电子账户状态字为部冻或部止
					// 判断电子账户可用余额是否小于调账
				BigDecimal acctbl = BigDecimal.ZERO;
				IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);
				// 获取电子账户可用余额
				acctbl = SysUtil.getInstance(DpAcctSvcType.class).getAcctaAvaBal(tblKnaAcdc.getCustac(),
						cplKnaAcct.getAcctno(), cplKnaAcct.getCrcycd(), BaseEnumType.E_YES___.YES,
						BaseEnumType.E_YES___.NO);
				// 判断调账金额是否小于可用余额
				if (CommUtil.compare(acctbl, happal) < 0) {
					throw DpModuleError.DpstAcct.BNAS3006();
				}
			}
		}
	}

	/**
	 * @Auther xionglz
	 *         <p>
	 *         <li>2017年12月09日-上午11:27:40</li>
	 *         <li>功能说明：电子账户调账校验原交易流水</li>
	 *         <p>
	 * 
	 * @param
	 **/
	private void checkAcsq(String custac, String prtrsq, E_AMNTCD amntcd, BigDecimal happal) {
		// 使用电子账户调账，记账方向必须在贷方
		if (amntcd.equals(E_AMNTCD.DR)) {
			throw InError.comm.E0003("电子账户不允许借方调账！");
		}
		// 根据电子账号查询结算账户
		IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);
		if (CommUtil.isNull(cplKnaAcct)) {
			throw InError.comm.E0003("未找到有效结算账号！");
		}
		// 查询会计流水
		Options<IoCaHknsAcsq> tblhknsacsq = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnsAcsqOdb1(prtrsq,
				cplKnaAcct.getAcctno());
		if (tblhknsacsq.isEmpty()) {
			throw InError.comm.E0003("未找到交易流水或交易流水不匹配！");
		}
		// 汇总会计流水发生额
		BigDecimal tranam = BigDecimal.ZERO;
		for (int i = 0; i < tblhknsacsq.size(); i++) {
			tranam = tranam.add(tblhknsacsq.get(i).getTranam());
		}
		// 调账金额必须小于原交易发生额
		if (CommUtil.compare(happal, tranam) > 0) {
			throw InError.comm.E0003("调账金额不能大于原交易发生额！");
		}
	}

	public cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddJFEacctOut addJFEacct(
			final cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddJFEacctIn eactin) {

		String custna = eactin.getCustna();
		String custid = eactin.getCustid();
		String brchno = eactin.getBrchno();
		String teleno = eactin.getTeleno();
		DpEnumType.E_CUSACT cacttp = eactin.getCacttp();
		BaseEnumType.E_ACCATP accatp = eactin.getAccatp();
		CaEnumType.E_IDCKRT idckrt = eactin.getIdckrt();
		CaEnumType.E_MPCKRT mpckrt = eactin.getMpckrt();

		// 创建电子账户信息
		String custac = BusiTools.genCustac();
		KnaCust tblKna_cust = SysUtil.getInstance(KnaCust.class);
		tblKna_cust.setCustac(custac); // 电子账户
		tblKna_cust.setCustno(custid); // 客户号
		tblKna_cust.setCustna(custna); // 客户名称
		tblKna_cust.setOpendt(CommTools.getBaseRunEnvs().getTrxn_date()); // 开户日期
		tblKna_cust.setOpensq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 开户流水
		tblKna_cust.setUschnl(CommUtil.toEnum(BaseEnumType.E_USCHNL.class, CommTools.getBaseRunEnvs().getChannel_id())); // 开户渠道
		tblKna_cust.setBrchno(brchno);
		tblKna_cust.setCacttp(cacttp); // 客户账号类型
		tblKna_cust.setAccttp(accatp); // 电子账户分类
		tblKna_cust.setIdckrt(idckrt); // 身份核查结果
		tblKna_cust.setMpckrt(mpckrt); // 人脸识别结果
		tblKna_cust.setAcctst(E_ACCTST.NORMAL);
		KnaCustDao.insert(tblKna_cust);

		// 创建卡信息
		KnpParameter cardPara = SysUtil.getInstance(KnpParameter.class);
		String sortcd = null;
		if (BaseEnumType.E_ACCATP.FINANCE == accatp) {
			cardPara = KnpParameterDao.selectOne_odb1("KcdProd.cardbn", "kabin", "lzbank", "2", true);
			sortcd = "Cardno12_17_2";
		} else {
			throw CaError.Eacct.E0901("账户分类不对。");
		}
		if (CommUtil.isNull(cardPara)) {
			throw CaError.Eacct.E0901("开户卡bin未配置。");
		}
		String kabin = cardPara.getParm_value1();
		if (CommUtil.isNull(kabin)) {
			throw Eacct.BNAS1427();
		}
		// 生成卡号
		StringBuffer cardBuff = new StringBuffer();
		cardBuff = cardBuff.append(kabin).append(cardPara.getParm_value2()).append(BusiTools.getSequence(sortcd, 7));
		String chkNo = CaTools.countParityBit(cardBuff);
		String cardno = cardBuff.append(chkNo).toString();

		// 建立电子账号与卡号关联
		KnaAcdc tblKna_acdc = SysUtil.getInstance(KnaAcdc.class);
		tblKna_acdc.setCardno(cardno);
		tblKna_acdc.setCustac(custac);
		tblKna_acdc.setStatus(E_DPACST.NORMAL);
		KnaAcdcDao.insert(tblKna_acdc);

		// 登记开户登记簿
		IoCaOpenAccInfo cplOpenAccInfo = SysUtil.getInstance(IoCaOpenAccInfo.class);
		cplOpenAccInfo.setCustac(custac);
		cplOpenAccInfo.setAccttp(accatp);
		cplOpenAccInfo.setCustna(custna);
		cplOpenAccInfo.setTlphno(teleno);
		cplOpenAccInfo.setBrchno(brchno);
		cplOpenAccInfo.setOpendt(CommTools.getBaseRunEnvs().getTrxn_date());
		cplOpenAccInfo.setOpensq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		CaEAccountProc.addOpenAcctBook(cplOpenAccInfo);

		// 返回信息
		IoCaAddJFEacctOut cplAddEAccountOut = SysUtil.getInstance(IoCaAddJFEacctOut.class);
		cplAddEAccountOut.setCustac(tblKna_cust.getCustac());
		cplAddEAccountOut.setCustid(tblKna_cust.getCustno());
		cplAddEAccountOut.setCardno(cardno);
		cplAddEAccountOut.setOpendt(tblKna_cust.getOpendt());
		cplAddEAccountOut.setOpensq(tblKna_cust.getOpensq());

		return cplAddEAccountOut;
	}

	public void addJFAgents(
			final Options<cn.sunline.ltts.busi.iobus.type.dp.IoCaTypeGenBindCard.IoAgentList> lsAgentRelation,
			String acctid) {

		for (IoAgentList agent : lsAgentRelation) {

			knb_agent tblKnb_agent = SysUtil.getInstance(knb_agent.class);
			tblKnb_agent.setAgntid(agent.getAgntid());
			tblKnb_agent.setMechid(acctid);
			tblKnb_agent.setCreadt(CommTools.getBaseRunEnvs().getTrxn_date());
			Knb_agentDao.insert(tblKnb_agent);

		}

	}
}
