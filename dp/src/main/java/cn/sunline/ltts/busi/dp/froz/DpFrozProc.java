package cn.sunline.ltts.busi.dp.froz;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.util.sequ.MsSeqUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstProd;
import cn.sunline.edsp.busi.dp.iobus.type.DpFrozType.DpFrozInfo;
import cn.sunline.edsp.busi.dp.iobus.type.DpFrozType.DpFrozOutInfo;
import cn.sunline.edsp.busi.dp.iobus.type.DpFrozType.acctInfo;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskEnumType;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.KnbAplyDto;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.KnlIoblCupsEdmContro;
import cn.sunline.edsp.busi.dp.namedsql.DpRiskDao;
import cn.sunline.edsp.busi.dp.tables.risk.DpRisk.KnbAply;
import cn.sunline.edsp.busi.dp.tables.risk.DpRisk.KnbFrozDeta;
import cn.sunline.edsp.busi.dp.tables.risk.DpRisk.KnbFrozDetaDao;
import cn.sunline.edsp.busi.dp.tables.risk.DpRisk.KnbRecord;
import cn.sunline.edsp.busi.dp.tables.risk.DpRisk.KnbThaw;
import cn.sunline.edsp.busi.dp.type.jfbase.JFBaseEnumType;
import cn.sunline.edsp.busi.dp.type.jfbase.JFBaseEnumType.E_STACTP;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.base.DecryptConstant;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.domain.DpFrozEntity;
import cn.sunline.ltts.busi.dp.domain.DpOpprEntity;
import cn.sunline.ltts.busi.dp.domain.QrbackEntity;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpFrozDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDedu;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDeduDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDepr;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDeprDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDeprOwne;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDeprOwneDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDeprProf;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDeprProfDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozAcctDetl;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozAcctDetlDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetl;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetlDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozNumb;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozNumbDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwne;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwneDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbUnfr;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbUnfrDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnpFroz;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoQueryFeedBack.Output;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpDeprInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoCtfrozPayIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoCustacInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStFzIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStUfIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStopPayIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStopayIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStopayOt;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpUnStopPayIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpUnStopayIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpprovIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoFrozHistInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoQrBackInfoIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoQrBackInfoOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IofrozInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalFee_IN;
import cn.sunline.ltts.busi.sys.dict.DpDict;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSTRFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEPRBP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEPRTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPCGFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPRECY;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSTOP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZMD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SPECTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SPTYPE;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_STOPMS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_STOPTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_STTMCT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_STUNTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_VOCHST;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CGTRTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;

public class DpFrozProc {

	private static BizLog bizlog = BizLogUtil.getBizLog(DpFrozProc.class);

	private static String genFrozno(E_FROZTP froztp, String trandt, String tranbr) {
		return BusiTools.getSequence("frozsq", 10, "0");
	}

	// 生成存款证明流水号
	private static String genDeprsq() {
		return BusiTools.getSequence("deprsq", 10, "0");
	}

	// 生成存款证明编号
	private static String genDeprnm() {
		return BusiTools.getSequence("deprnm", 10, "0");
	}

	/**
	 * 生成冻结子序号
	 * 
	 * @param acctno
	 * @param frozlv
	 * @return
	 */
	public static long genSubFrozSq(String acctno, Long frozlv) {
		Long lReturn = 0l;

		KnbFrozNumb tblKnbFrozNumb = KnbFrozNumbDao.selectOneWithLock_odb1(acctno, false);

		if (CommUtil.isNull(tblKnbFrozNumb)) {

			tblKnbFrozNumb = SysUtil.getInstance(KnbFrozNumb.class);

			lReturn = 1l;
			tblKnbFrozNumb.setFrowid(acctno);
			tblKnbFrozNumb.setFrsbsq(lReturn);

			KnbFrozNumbDao.insert(tblKnbFrozNumb);

		} else {

			lReturn = tblKnbFrozNumb.getFrsbsq() + 1;
			tblKnbFrozNumb.setFrsbsq(lReturn);

			KnbFrozNumbDao.updateOne_odb1(tblKnbFrozNumb);
		}

		return lReturn;
	}

	/**
	 * 司法冻结检查
	 * 
	 * @param cpliodpfrozin
	 */
	public static void frozByLawCheck(IoDpStopPayIn cpliodpfrozin) {

		if (CommUtil.isNull(cpliodpfrozin.getFrozwy())) {
			throw DpModuleError.DpstComm.BNAS0816();
		}

		if (cpliodpfrozin.getFrozwy().equals(E_FROZWY.CONTIN) || cpliodpfrozin.getFrozwy().equals(E_FROZWY.TSOLVE)) {// 续冻或者解冻
			throw DpModuleError.DpstComm.BNAS0814();
		}

		if (cpliodpfrozin.getFrozwy().equals(E_FROZWY.BORROW)) {// 借冻
			cpliodpfrozin.setFrlmtp(E_FRLMTP.OUT);// 只进不出
		}

		if (cpliodpfrozin.getFrozwy().equals(E_FROZWY.DOUBLE)) {// 双冻
			cpliodpfrozin.setFrlmtp(E_FRLMTP.ALL);// 不进不出
		}

		if (cpliodpfrozin.getFrozwy().equals(E_FROZWY.PARTOF)) {// 部冻
			cpliodpfrozin.setFrlmtp(E_FRLMTP.AMOUNT);// 制定金额
		}

		if (E_YES___.YES == cpliodpfrozin.getFricfg() && !cpliodpfrozin.getFrozwy().equals(E_FROZWY.PARTOF)) {
			throw DpModuleError.DpTrans.TS020082();
		}
		/*if (CommUtil.isNull(cpliodpfrozin.getCardno())) {
			throw DpModuleError.DpstProd.BNAS0926();
		}

		if (CommUtil.isNull(cpliodpfrozin.getCustna())) {
			throw DpModuleError.DpstComm.BNAS0524();
		}*/

		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);

		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(cpliodpfrozin.getCardno(), false);

		if (CommUtil.isNull(caKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0692();
		}

		// if(caKnaAcdc.getStatus() != E_DPACST.NORMAL ){
		// throw FnError.FinaComm.E9999("根据电子账号获取电子账号ID的状态不正常");
		// }

		// 电子账号ID
		/*String custac = caKnaAcdc.getCustac();

		String custna = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(custac, false)
				.getCustna();

		if (!CommUtil.equals(cpliodpfrozin.getCustna(), custna)) {
			throw DpModuleError.DpstComm.BNAS0526();
		}*/

		// IoDpSrvQryTableInfo tblIoDpSrvQryTableInfo =
		// SysUtil.getInstance(IoDpSrvQryTableInfo.class);
		//
		// IoDpKnaAcct tblIoDpKnaAcct =
		// tblIoDpSrvQryTableInfo.KnaAcct_selectFirst_odb7(custac, E_ACSETP.SA,
		// false);
		//
		if (CommUtil.isNull(cpliodpfrozin.getCrcycd())) {
			throw DpModuleError.DpstComm.BNAS1101();
		}

		// IoDpKnaAcct cplKnaAcct =
		// SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(caKnaAcdc.getCustac());
		// if (!CommUtil.equals(cpliodpfrozin.getCrcycd(), cplKnaAcct.getCrcycd())) {
		// throw DpModuleError.DpstComm.E9999("输入币种与账户币种不一致");
		// }

		// if(cpliodpfrozin.getCrcycd() != tblIoDpKnaAcct.getCrcycd()){
		// throw DpModuleError.DpstComm.E9999("币种输入错误");
		// }

		if (!CommUtil.equals(cpliodpfrozin.getCrcycd(), BusiTools.getDefineCurrency())) {

			if (CommUtil.isNull(cpliodpfrozin.getCsextg())) {
				throw DpModuleError.DpstComm.BNAS1102();
			}

			// if(cpliodpfrozin.getCsextg() != tblIoDpKnaAcct.getCsextg()){
			// throw DpModuleError.DpstComm.E9999("币种不为人民币时账户钞汇标志输入错误");
			// }

		}
		if (CommUtil.equals(cpliodpfrozin.getCrcycd(), BusiTools.getDefineCurrency())
				&& CommUtil.isNotNull(cpliodpfrozin.getCsextg())) {
			throw DpModuleError.DpstComm.BNAS1098();
		}

		// 冻结日期 借冻、双冻、部冻必须为系统当前日期；续冻默认为上一次冻结到期日；解冻时输入原冻结日期
		// 冻结到期日期 借冻、双冻、部冻录入；续冻时需大于原冻结到期日；解冻时回显，不可改
/*		if (CommUtil.isNull(cpliodpfrozin.getFrexog())) {
			throw DpModuleError.DpstComm.BNAS0106();
		}

		if (CommUtil.isNull(cpliodpfrozin.getFrogna())) {
			throw DpModuleError.DpstComm.BNAS0103();
		}

		if (CommUtil.isNull(cpliodpfrozin.getFrctno())) {
			throw DpModuleError.DpstComm.BNAS0824();
		}

		if (CommUtil.isNull(cpliodpfrozin.getFrna01())) {
			throw DpModuleError.DpstComm.BNAS0103();
		}

		if (CommUtil.isNull(cpliodpfrozin.getIdtp01())) {
			throw DpModuleError.DpstComm.BNAS0100();
		}

		if (CommUtil.isNull(cpliodpfrozin.getIdno01())) {
			throw DpModuleError.DpstComm.BNAS0102();
		}

		// 校验证件类型、证件号码
		// if (cpliodpfrozin.getIdtp01() == E_IDTFTP.SFZ) {
		// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp =
		// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP.SFZ;
		// BusiTools.chkCertnoInfo(idtftp, cpliodpfrozin.getIdno01());
		// }

		if (CommUtil.isNull(cpliodpfrozin.getFrna02())) {
			throw DpModuleError.DpstComm.BNAS0099();
		}

		if (CommUtil.isNull(cpliodpfrozin.getIdtp02())) {
			throw DpModuleError.DpstComm.BNAS0098();
		}

		if (CommUtil.isNull(cpliodpfrozin.getIdno02())) {
			throw DpModuleError.DpstComm.BNAS1599();
		}

		if (cpliodpfrozin.getIdtp01() == cpliodpfrozin.getIdtp02()
				&& cpliodpfrozin.getIdno01() == cpliodpfrozin.getIdno02()) {
			throw DpModuleError.DpstComm.BNAS0235();
		}*/

		// 校验证件类型、证件号码
		// if (cpliodpfrozin.getIdtp02() == E_IDTFTP.SFZ) {
		// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp =
		// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP.SFZ;
		// BusiTools.chkCertnoInfo(idtftp, cpliodpfrozin.getIdno02());
		// }

		if (CommUtil.isNull(cpliodpfrozin.getFrreas())) {
			throw DpModuleError.DpstComm.BNAS0097();
		}

		// long num = DpFrozDao.selFrctno(cpliodpfrozin.getFrexog(),
		// cpliodpfrozin.getFrogna(), cpliodpfrozin.getFrctno(), true);
		// if (num > 0) {
		// throw DpModuleError.DpstComm.E9999("同一冻结通知书不能冻结多次");
		// }

		// 冻结日期检查
		if (CommUtil.isNull(cpliodpfrozin.getFrozdt())) {
			// throw DpModuleError.DpstComm.E9027(DpDict.Froz.frozdt.getLongName());
			throw DpModuleError.DpstComm.BNAS0828();
		}

		if (!DateTools2.chkIsDate(cpliodpfrozin.getFrozdt())) {
			throw DpModuleError.DpstComm.BNAS0827();
		}
		if (CommUtil.compare(cpliodpfrozin.getFrozdt(), CommTools.getBaseRunEnvs().getTrxn_date()) != 0) {
			throw DpModuleError.DpstComm.BNAS0308(cpliodpfrozin.getFrozdt(), CommTools.getBaseRunEnvs().getTrxn_date());
		}

		/*// 到期日期格式检查
		if (CommUtil.isNull(cpliodpfrozin.getFreddt())) {
			// throw DpModuleError.DpstComm.E9027(DpDict.Froz.freddt.getLongName());
			throw DpModuleError.DpstComm.BNAS0833();
		}

		if (!DateTools2.chkIsDate(cpliodpfrozin.getFreddt())) {
			throw DpModuleError.DpstComm.BNAS0818();
		}

		// 到期日检查
		if (CommUtil.compare(cpliodpfrozin.getFreddt(), CommTools.getBaseRunEnvs().getTrxn_date()) < 0) {
			throw DpModuleError.DpstComm.BNAS0819(cpliodpfrozin.getFreddt());
		}

		// 冻结终止日期和冻结日期检查
		if (CommUtil.compare(cpliodpfrozin.getFreddt(), cpliodpfrozin.getFrozdt()) <= 0) {
			throw DpModuleError.DpstComm.BNAS1600(cpliodpfrozin.getFreddt(), cpliodpfrozin.getFrozdt());
		}

		// 司法冻结冻结时间不能超过1年
		// String sMaxDate = DateTimeUtil.dateAdd("mm",
		// CommTools.getBaseRunEnvs().getTrxn_date(), 12);

		// sMaxDate = DateTimeUtil.dateAdd("dd", sMaxDate, 0);
		// String date =
		// DateTools2.calDateByTerm(CommTools.getBaseRunEnvs().getTrxn_date(), "1Y",
		// null, null, 3, "A");
		String date = DateTools2.calDateByTerm(CommTools.getBaseRunEnvs().getTrxn_date(), "1Y", null, "A");
		if (CommUtil.compare(cpliodpfrozin.getFreddt(), date) > 0) {
			// throw DpModuleError.DpstComm.E9999("司法冻结时，冻结期限不能超过一年，当前有效的最大冻结日期应为[" + sMaxDate
			// + "]");超出冻结期限，请核实
			throw DpModuleError.DpstComm.BNAS1046();
		}*/
		
		if(CommUtil.isNull(cpliodpfrozin.getStactp())) {
			cpliodpfrozin.setStactp(E_STACTP.STMA);
		}
		
		if (JFBaseEnumType.E_STACTP.STSA == cpliodpfrozin.getStactp() && CommUtil.isNull(cpliodpfrozin.getAcctno())) {
			throw DpModuleError.DpAcct.AT020058(E_FROZMD.ONE_DPAC.getLongName());
		}
		
		if(JFBaseEnumType.E_STACTP.STSA != cpliodpfrozin.getStactp() && CommUtil.isNotNull(cpliodpfrozin.getAcctno())) {
			throw DpModuleError.DpAcct.AT020060();
		}
		
		if(CommUtil.isNull(cpliodpfrozin.getStactp())) {
			cpliodpfrozin.setStactp(E_STACTP.STMA);
		}
		
		if (JFBaseEnumType.E_STACTP.STSA == cpliodpfrozin.getStactp() && CommUtil.isNull(cpliodpfrozin.getAcctno())) {
			throw DpModuleError.DpAcct.AT020058(E_FROZMD.ONE_DPAC.getLongName());
		}
		
		if(JFBaseEnumType.E_STACTP.STSA != cpliodpfrozin.getStactp() && CommUtil.isNotNull(cpliodpfrozin.getAcctno())) {
			throw DpModuleError.DpAcct.AT020060();
		}
	}

	/**
	 * 冻结反馈有权机构信息输入参数基本检查
	 * 
	 * @author douwenbo
	 * @date 2016-06-14 19:26
	 * @param ioQrBackInfoIn
	 */
	public static void checkQrBackInfoIn(IoQrBackInfoIn ioQrBackInfoIn, QrbackEntity entity, Output output) {

		int totlCount = 0; // 记录总数

		int startno = (ioQrBackInfoIn.getPageno() - 1) * ioQrBackInfoIn.getPgsize();// 起始记录数

		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);

		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(ioQrBackInfoIn.getCardno(), false);

		if (CommUtil.isNull(caKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0692();
		}

		if (caKnaAcdc.getStatus() != E_DPACST.NORMAL) {
			throw DpModuleError.DpstComm.BNAS0693();
		}

		// 电子账号ID
		String custac = caKnaAcdc.getCustac();

		// 获取冻结集合输出接口
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String trandt2 = CommTools.getBaseRunEnvs().getTrxn_date();
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
		E_FROZST frozst = E_FROZST.VALID;
		E_FROZTP froztp = E_FROZTP.JUDICIAL;
		E_FROZTP froztp2 = E_FROZTP.ADD;

		Page<IoQrBackInfoOut> qrBackInfoOuts = DpFrozDao.selFrozInfoForFeedBack(custac, tranbr, trandt, trandt2, frozst,
				froztp, froztp2, startno, ioQrBackInfoIn.getPgsize(), totlCount, false);

		output.getIoQrBackInfoOut().addAll(qrBackInfoOuts.getRecords());

		output.setCounts(ConvertUtil.toInteger(qrBackInfoOuts.getRecordCount()));

		// 设置报文头总记录条数
		CommTools.getBaseRunEnvs().setTotal_count(qrBackInfoOuts.getRecordCount());

	}

	/**
	 * 系统冻结检查
	 * 
	 * @author douwenbo
	 * @date 2016-06-01 15:44
	 * @param cpliodpfrozin
	 *            系统冻结输入接口
	 */
	public static void syetemFrozCheck(IoDpStFzIn cpliodpstfzin) {

		if (CommUtil.isNull(cpliodpstfzin.getCardno())) {
			throw DpModuleError.DpstProd.BNAS0926();
		}

		if (CommUtil.isNull(cpliodpstfzin.getCrcycd())) {
			throw DpModuleError.DpstComm.BNAS1101();
		}

		if (CommUtil.isNull(cpliodpstfzin.getFroztp())) {
			throw DpModuleError.DpstComm.BNAS0820();
		}

		if (CommUtil.isNull(cpliodpstfzin.getFrozam())) {
			throw DpModuleError.DpstComm.BNAS0829();
		}

		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);

		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(cpliodpstfzin.getCardno(), false);

		if (CommUtil.isNull(caKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0753();
		}

		if (!CommUtil.equals(caKnaAcdc.getCorpno(), CommTools.getBaseRunEnvs().getBusi_org_id())) {
			throw DpModuleError.DpstComm.BNAS0796();
		}

		// if(caKnaAcdc.getStatus() != E_DPACST.NORMAL ){
		// throw DpModuleError.DpstComm.E9999("根据电子账号获取电子账号ID的状态不正常");
		// }

		// 电子账号ID
		String custac = caKnaAcdc.getCustac();

		// 账户状态为预开户、转久悬、预销户、销户的，交易拒绝，报错：“电子账户状态为***状态，无法操作！”。
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		bizlog.debug("+++++++++++++++++++++++++++++" + "电子账户状态为" + cuacst + "+++++++++++++++++++++++++");

		if (cuacst == E_CUACST.CLOSED) {
			throw DpModuleError.DpstComm.BNAS1597();

		} else if (cuacst == E_CUACST.PRECLOS) {
			throw DpModuleError.DpstComm.BNAS0846();

		} else if (cuacst == E_CUACST.PREOPEN) {
			throw DpModuleError.DpstComm.BNAS1598();

		} else if (cuacst == E_CUACST.NOACTIVE) {
			throw DpModuleError.DpstComm.BNAS1601();

			/*
			 * } else if (cuacst == E_CUACST.DORMANT) { throw DpModuleError.DpstComm.BNAS0847();
			 */

		} else if (cuacst == E_CUACST.OUTAGE && !CommUtil.equals("disabl", CommTools.getBaseRunEnvs().getTrxn_code())) {
			throw DpModuleError.DpstComm.BNAS0850();

		} else if (cuacst == E_CUACST.NOENABLE) {
			throw DpModuleError.DpstComm.BNAS0848();

		}

		// 检查当前当前账户状态字是否异常
		// CapitalTransCheck.ChkAcctstWord(custac);
		CapitalTransCheck.ChkAcctFrozIN(custac);

		// 获取客户账号信息
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaCust tblKnaCust = caqry.getKnaCustByCustacOdb1(custac, true);

		// 电子账户是否涉案
		IoCaSevQryAccout.IoCaQryInacInfo.Output output = SysUtil
				.getInstance(IoCaSevQryAccout.IoCaQryInacInfo.Output.class);
		SysUtil.getInstance(IoCaSevQryAccout.class).qryInac(cpliodpstfzin.getCardno(), tblKnaCust.getCustna(), null,
				null, output);

		final String otcard_in = cpliodpstfzin.getCardno();// 转出账号
		final String otacna_in = tblKnaCust.getCustna();// 转出账户名称
		final String otbrch_in = CommTools.getBaseRunEnvs().getTrxn_branch();// 转出机构
		final String incard_in = null;// 转入账号
		final String inacna_in = null;// 转入账号名称
		final String inbrch_in = CommTools.getBaseRunEnvs().getTrxn_branch();// 转入机构
		final BigDecimal tranam_in = cpliodpstfzin.getFrozam();// 交易金额
		final String crcycd = cpliodpstfzin.getCrcycd();// 币种

		if (E_INSPFG.INVO == output.getInspfg()) {
			// 独立事务
			DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
				@Override
				public Void execute() {
					// 获取涉案账户交易信息登记输入信息
					IoCaKnbTrinInput cplKnbTrin = SysUtil.getInstance(IoCaKnbTrinInput.class);

					cplKnbTrin.setOtcard(otcard_in);// 转出账号
					cplKnbTrin.setOtacna(otacna_in);// 转出账号名称
					cplKnbTrin.setOtbrch(otbrch_in);// 转出账户机构
					cplKnbTrin.setIncard(incard_in);// 转入账号
					cplKnbTrin.setInacna(inacna_in);// 转入账户名称
					cplKnbTrin.setInbrch(inbrch_in);// 转入账户机构
					cplKnbTrin.setTranam(tranam_in);// 交易金额
					cplKnbTrin.setCrcycd(crcycd);// 币种
					cplKnbTrin.setIssucc(E_YES___.NO);// 交易是否成功

					// 涉案账户交易信息登记
					SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(cplKnbTrin);

					return null;
				}
			});

			throw DpModuleError.DpstComm.BNAS0437();

		}

		// 检查是否设恐
		SysUtil.getInstance(IoCaSevQryAccout.class).IoCaQryInwadeInfo(cpliodpstfzin.getCardno(), null, null, null);

		if (CommUtil.compare(cpliodpstfzin.getFrozam(), BigDecimal.ZERO) <= 0) {
			throw DpModuleError.DpstComm.BNAS0627();
		}

		// 可用余额 add by xiongzhao 2016/12/22 (查询结算子账户，计算可用余额)
		IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);

		BigDecimal tranbl = SysUtil.getInstance(DpAcctSvcType.class).getAcctaAvaBal(custac, cplKnaAcct.getAcctno(),
				cpliodpstfzin.getCrcycd(), E_YES___.YES, E_YES___.NO);

		// tranbl = tranbl.subtract(DpFrozTools.getFrozBala(E_FROZOW.AUACCT,
		// custac));

		if (CommUtil.compare(cpliodpstfzin.getFrozam(), tranbl) > 0) {
			throw DpModuleError.DpstComm.BNAS0442();
		}
	}

	/**
	 * 系统冻结
	 * 
	 * @author douwenbo
	 * @date 2016-06-01 15:49
	 * @param cpliodpfrozin
	 *            系统冻结输入接口
	 */
	public static void systemFrozDo(IoDpStFzIn cpliodpstfzin) {

		KnbFroz tblKnbFroz = SysUtil.getInstance(KnbFroz.class);

		// 生成冻结编号,系统内部定
		String frozno = "99" + CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4) + genFrozno(E_FROZTP.FNFROZ,
				CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getTrxn_branch());

		// 冻结序号
		long frozsq = ConvertUtil.toLong(BusiTools.getSequence("dpdtfz", 10, "0"));

		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);

		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(cpliodpstfzin.getCardno(), false);

		if (CommUtil.isNull(caKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0928();
		}

		if (caKnaAcdc.getStatus() != E_DPACST.NORMAL) {
			throw DpModuleError.DpstComm.BNAS0693();
		}

		// 电子账号ID
		String custac = caKnaAcdc.getCustac();

		// 基本冻结信息
		tblKnbFroz.setFrozno(frozno);// 冻结编号
		tblKnbFroz.setFrozsq(frozsq);// 冻结序号
		tblKnbFroz.setCustac(custac);// 客户账号
		tblKnbFroz.setFroztp(cpliodpstfzin.getFroztp()); // 冻结业务类型
		tblKnbFroz.setFrozcd("DEFAULT"); // 冻结分类码设置为DEFAULT
		tblKnbFroz.setFrozow(E_FROZOW.AUACCT); // 冻结主体类型(默认设置为智能储蓄)
		tblKnbFroz.setFrlmtp(E_FRLMTP.AMOUNT); // 限制类型
		tblKnbFroz.setFrozlv(99l); // 冻结级别

		tblKnbFroz.setFroztm(CommTools.getBaseRunEnvs().getComputer_time()); // 冻结时间
		tblKnbFroz.setFrozdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 冻结日期
		tblKnbFroz.setFrbgdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 冻结起始日期
		// tblKnbFroz.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
		tblKnbFroz.setMntrsq(CommTools.getBaseRunEnvs().getTrxn_seq()); // 上送系统流水

		/**
		 * add by huangwh 20181130: start description: 参数中有冻结终止日期就用参数中的！
		 */
		if (!CommUtil.isNull(cpliodpstfzin.getEndday())) {
			tblKnbFroz.setFreddt(cpliodpstfzin.getEndday()); // 冻结终止日期
		} else {
			tblKnbFroz.setFreddt("20990101"); // 冻结终止日期(默认设置为20990101)
		}
		if (!CommUtil.isNull(cpliodpstfzin.getSprdna())) {
			tblKnbFroz
					.setFrreas("存款冻结:产品名称[" + cpliodpstfzin.getSprdna() + "]产品编号:[" + cpliodpstfzin.getSprdid() + "]"); // 冻结原因
		} else {
			tblKnbFroz.setFrreas(null); // 冻结原因
		}
		/**
		 * add by huangwh 20181130: end
		 */
		tblKnbFroz.setFrozst(E_FROZST.VALID); // 冻结状态
		tblKnbFroz.setStopms(null);// 止付措施

		tblKnbFroz.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
		tblKnbFroz.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq()); // 交易流水
		tblKnbFroz.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller()); // 交易柜员
		tblKnbFroz.setCkuser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 授权柜员
		// tblKnbFroz.setCptrcd(CommTools.getBaseRunEnvs().getCptrcd()); // 对账代码
		tblKnbFroz.setLttscd(BusiTools.getBusiRunEnvs().getTrxn_code()); // 内部交易码

		// 系统冻结登记
		tblKnbFroz.setFrctno(null); // 冻结通知书编号
		tblKnbFroz.setFrcttp(null); // 冻结文书种类
		tblKnbFroz.setFrexog(null); // 执法部门类型
		tblKnbFroz.setFrogna(null); // 执法部门名称
		tblKnbFroz.setIdtp01(null); // 执法人员1证件种类
		tblKnbFroz.setIdno01(null); // 执法人员1证件号码
		tblKnbFroz.setFrna01(null); // 执法人员1名称
		tblKnbFroz.setIdtp02(null); // 执法人员2证件种类
		tblKnbFroz.setIdno02(null); // 执法人员2证件号码
		tblKnbFroz.setFrna02(null); // 执法人员2名称
		tblKnbFroz.setPruser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 审批柜员
		tblKnbFroz.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
		// MsSystemSeq.getTrxnSeq(); //重置流水
		tblKnbFroz.setCrcycd(cpliodpstfzin.getCrcycd()); // 币种
		tblKnbFroz.setCsextg(null); // 账户钞会标志
		tblKnbFroz.setRemark("系统冻结-" + cpliodpstfzin.getFroztp()); // 备注
		tblKnbFroz.setStactp(JFBaseEnumType.E_STACTP.STMA);
		// 登记冻结登记簿
		KnbFrozDao.insert(tblKnbFroz);

		KnbFrozDetl tblKnbfrozdetl = SysUtil.getInstance(KnbFrozDetl.class);
		tblKnbfrozdetl.setFrowid(custac);
		tblKnbfrozdetl.setFrozam(cpliodpstfzin.getFrozam());
		tblKnbfrozdetl.setFrozbl(cpliodpstfzin.getFrozam());
		tblKnbfrozdetl.setFrozsq(frozsq);

		auacctStopPay(tblKnbfrozdetl, tblKnbFroz);

	}

	/**
	 * 系统解冻检查
	 * 
	 * @author douwenbo
	 * @date 2016-06-22 15:44
	 * @param cpliodpstufin
	 *            系统冻结输入接口
	 */
	public static void syetemUnfrCheck(IoDpStUfIn cpliodpstufin) {

		if (CommUtil.isNull(cpliodpstufin.getTrandt())) {
			throw DpModuleError.DpstComm.BNAS0605();
		}

		if (!DateTools2.chkIsDate(cpliodpstufin.getTrandt())) {
			throw DpModuleError.DpstComm.BNAS0604();
		}

		if (CommUtil.isNull(cpliodpstufin.getMntrsq())) {
			throw DpModuleError.DpstComm.BNAS0050();
		}
	}

	/**
	 * 系统解冻
	 * 
	 * @author douwenbo
	 * @date 2016-06-22 15:46
	 * @param cpliodpstufin
	 *            系统冻结输入接口
	 */
	public static void systemUnfrDo(IoDpStUfIn cpliodpstufin) {
		KnbFroz tblKnbFroz = KnbFrozDao.selectOne_odb13(cpliodpstufin.getTrandt(), cpliodpstufin.getMntrsq(), false);
		if (CommUtil.isNull(tblKnbFroz)) {
			throw DpModuleError.DpstComm.BNAS0734();
		}

		if (E_FROZST.INVALID == tblKnbFroz.getFrozst()) {
			throw DpModuleError.DpstComm.BNAS0733();
		}

		// 调用DP模块服务查询冻结状态，检查电子账户状态字
		IoDpFrozSvcType froz = SysUtil.getInstance(IoDpFrozSvcType.class);
		IoDpAcStatusWord cplGetAcStWord = froz.getAcStatusWord(tblKnbFroz.getCustac());

		if (CommUtil.equals("NM", CommTools.getBaseRunEnvs().getChannel_id())) {
			if (cplGetAcStWord.getClstop() == E_YES___.YES) {
				throw CaError.Eacct.BNAS0436();
			}
		}

		KnbFrozDetl tblKnbFrozDetl = KnbFrozDetlDao.selectOne_odb2(tblKnbFroz.getFrozno(), tblKnbFroz.getFrozsq(),
				false);

		// 设置解冻登记簿信息
		KnbUnfr tblKnbunfr = SysUtil.getInstance(KnbUnfr.class);
		tblKnbunfr.setOdfrno(tblKnbFroz.getFrozno());
		tblKnbunfr.setCustac(tblKnbFroz.getCustac());
		tblKnbunfr.setUfcttp(null);
		tblKnbunfr.setUfctno(null);
		tblKnbunfr.setUfexog(null);
		tblKnbunfr.setUfogna(null);
		tblKnbunfr.setIdno01(null);
		tblKnbunfr.setIdtp01(null);
		tblKnbunfr.setUfna01(null);
		tblKnbunfr.setIdno02(null);
		tblKnbunfr.setIdtp02(null);
		tblKnbunfr.setUfna02(null);
		tblKnbunfr.setUnfrdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 解冻日期
		tblKnbunfr.setUnfrtm(CommTools.getBaseRunEnvs().getComputer_time()); // 解冻时间
		tblKnbunfr.setUfreas(null); // 解冻原因
		tblKnbunfr.setUnmark("系统解冻"); // 解冻备注
		tblKnbunfr.setUnfram(tblKnbFrozDetl.getFrozam()); // 解冻金额
		tblKnbunfr.setMtdate(CommTools.getBaseRunEnvs().getTrxn_date());// 维护日期

		tblKnbunfr.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
		tblKnbunfr.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq()); // 交易流水
		tblKnbunfr.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller()); // 交易柜员
		tblKnbunfr.setCkuser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 授权柜员
		// tblKnbunfr.setCptrcd(CommTools.getBaseRunEnvs().getCptrcd()); // 对账代码
		tblKnbunfr.setLttscd(BusiTools.getBusiRunEnvs().getTrxn_code()); // 内部交易码
		tblKnbunfr.setPruser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 审批柜员
		tblKnbunfr.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
		tblKnbunfr.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水

		tblKnbunfr.setCrcycd(tblKnbFroz.getCrcycd());// 设置币种
		tblKnbunfr.setUnfrsq(getUnfrsqAddOne(tblKnbFroz.getFrozno()));// 设置解冻序号

		KnbUnfrDao.insert(tblKnbunfr);

		IoDpUnStopPayIn cpliodpunfrozin = SysUtil.getInstance(IoDpUnStopPayIn.class);

		cpliodpunfrozin.setOdfrno(tblKnbFroz.getFrozno());
		cpliodpunfrozin.setUnfram(tblKnbFrozDetl.getFrozam());

		DpUnfrProc.auacctUnFroz(tblKnbFrozDetl, tblKnbFroz, cpliodpunfrozin);

		// // 成立清算解冻时不更新预警登记簿状态
		// if (CommUtil.equals(BusiTools.getBusiRunEnvs().getLttscd(), "setucl")) {
		//
		// String cardno = DpFrozDao.selKnaAcdcInfo(tblKnbFroz.getCustac(),
		// true);
		//
		// IoFnSevQryTableInfo fnQry = CommTools
		// .getInstance(IoFnSevQryTableInfo.class);
		//
		// // 判断当前该笔理财是否做过成立清算
		// IoFnfnBetuFail failInfo = fnQry.fnb_setu_fail_selectOne_odb1(
		// cardno, cpliodpstufin.getMntrsq(), false);
		// // 理财解冻成功，更新理财成立清算失败登记簿处理状态
		// if (CommUtil.isNotNull(failInfo)) {
		//
		// failInfo.setStatus(E_DEALST.SUCCESS);
		// failInfo.setStady1(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 登记统一后管流水
		// fnQry.updFnbSetuFailSt(failInfo);
		// }
		// }
	}

	public static long getUnfrsqAddOne(String Frozno) {

		long unfrsq = 1;

		if (CommUtil.isNull(DpFrozDao.selUnFrMaxseq(Frozno, false))) {
			return unfrsq;
		} else {
			return DpFrozDao.selUnFrMaxseq(Frozno, false) + 1;
		}
	}

	/**
	 * 基本冻结检查
	 * 
	 * @param cpliodpfrozin
	 */
	private static void stopPayCheck(IoDpStopPayIn cpliodpfrozin, KnpFroz tblKnpFroz) {

		// 交易日期
		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);

		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(cpliodpfrozin.getCardno(), false);

		if (CommUtil.isNull(caKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0692();
		}

		// 冻结主体不能为空
		if (CommUtil.isNull(cpliodpfrozin.getFrozow())) {
			throw DpModuleError.DpstComm.E9027(DpDict.Froz.frozow.getLongName());
		}

		// 暂不支持只出不进的冻结
		if (cpliodpfrozin.getFrlmtp() == E_FRLMTP.IN) {
			throw DpModuleError.DpstComm.BNAS0211();
		}

		// 金额冻结检查
		if (cpliodpfrozin.getFrlmtp() == E_FRLMTP.AMOUNT) {

			if (CommUtil.isNull(cpliodpfrozin.getFrozam())) {
				throw DpModuleError.DpstComm.E9027(DpDict.Froz.frozam.getLongName());
			}

			if (CommUtil.compare(cpliodpfrozin.getFrozam(), BigDecimal.ZERO) <= 0) {
				throw DpModuleError.DpstComm.BNAS0830();
			}

			// if (CommUtil.isNull(cpliodpfrozin.getSubsac())) {
			// throw DpModuleError.DpstComm.E9999("金额冻结必须指定子帐号。");
			// }
		} else {
			// 非金额冻结不能输入金额
			// if (CommUtil.isNotNull(cpliodpfrozin.getFrozam()) &&
			// !CommUtil.equals(cpliodpfrozin.getFrozam(),BigDecimal.ZERO)) {
			// throw DpModuleError.DpstComm.E9999("[" + cpliodpfrozin.getFrlmtp() +
			// "]无需指定冻结金额");
			// }
			if (!(CommUtil.isNull(cpliodpfrozin.getFrozam())
					|| CommUtil.equals(cpliodpfrozin.getFrozam(), BigDecimal.ZERO))) {
				throw DpModuleError.DpstComm.BNAS1137(cpliodpfrozin.getFrlmtp().getLongName());
			}
		}

		// 指定负债账号冻结，子帐号不能为空。
		if (cpliodpfrozin.getFrozow() == E_FROZOW.ACCTNO && CommUtil.isNull(cpliodpfrozin.getAcctno())) {
			throw DpModuleError.DpAcct.AT020058(E_FROZMD.ONE_DPAC.getLongName());
		}
		
		/* 定期子账号冻结必须输入定期负债账号 */
		if (cpliodpfrozin.getFrozow() == E_FROZOW.ACCTNO) {
			KnbFrozOwne KnbFrozOwne = KnbFrozOwneDao.selectOne_odb1(E_FROZOW.ACCTNO, cpliodpfrozin.getAcctno(), false);
			if (CommUtil.isNotNull(KnbFrozOwne)) {
				throw DpModuleError.DpAcct.AT020059();
			}
		}
	}

	/**
	 * @author douwenbo
	 * @date 2016-04-22 17:25 续冻最后检查
	 * 
	 * @param cpliodpfrozin
	 */
	private static void ctfrCheck(IoCtfrozPayIn cplioctfrozin, KnbFroz tblKnbFroz) {

		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);

		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(cplioctfrozin.getCardno(), false);

		if (CommUtil.isNull(caKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0928();
		}

		// if(caKnaAcdc.getStatus() != E_DPACST.NORMAL ){
		// throw DpModuleError.DpstComm.E9999("根据电子账号获取电子账号ID的状态不正常");
		// }

		// 电子账号ID
		String custac = caKnaAcdc.getCustac();

		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaCust tblKnaCust = caqry.getKnaCustWithLockByCustacOdb1(custac, true);

//		String custna = tblKnaCust.getCustna();

//		if (!cplioctfrozin.getCustna().equals(custna)) {
//			throw DpModuleError.DpstComm.BNAS0532();
//		}

		// 冻结编号的校验
		E_FROZTP JUDICIAL = E_FROZTP.JUDICIAL;// 司法冻结
		E_FROZTP ADD = E_FROZTP.ADD;// 续冻
		E_FROZST frozst = E_FROZST.VALID;// 冻结
		String frozno = tblKnbFroz.getFrozno();// 冻结编号

		KnbFroz tblFroz901 = KnbFrozDao.selectOne_odb9(frozno, JUDICIAL, frozst, false);
		if (JFBaseEnumType.E_STACTP.STSA == cplioctfrozin.getStactp()) {
			custac = cplioctfrozin.getAcctno();
		}
		if (CommUtil.isNull(tblFroz901)) {

			KnbFroz tblFroz902 = KnbFrozDao.selectOne_odb9(frozno, ADD, frozst, false);

			if (CommUtil.isNull(tblFroz902)) {
				throw DpModuleError.DpstComm.BNAS0690();
			}

			if (!CommUtil.equals(custac, tblFroz902.getCustac())) {
				throw DpModuleError.DpstComm.BNAS0219();
			}
		} else {
			if (!CommUtil.equals(custac, tblFroz901.getCustac())) {
				throw DpModuleError.DpstComm.BNAS0219();
			}
		}

		// if (tblKnbFroz.getFrozow() != E_FROZOW.AUACCT) {
		// throw DpModuleError.DpstComm.E9999("暂只支持智能储蓄续冻。");
		// }
		// 检查电子账号法人是否存在
		// String corpno = KnaAcctDao.selectFirst_odb6(custac,
		// false).getCorpno();

		// 交易发起法人需与电子账号法人一致
		if (!CommUtil.equals(tblKnaCust.getCorpno(), CommTools.getBaseRunEnvs().getBusi_org_id())) {
			throw DpModuleError.DpstComm.BNAS0793();
		}

		KnbFroz tblKnbFroz2 = KnbFrozDao.selectOne_odb8(tblKnbFroz.getFrozno(), getMinFrozsq(cplioctfrozin), false);

		// 交易发起机构需要是原冻结机构
		// if
		// (!CommUtil.equals(tblKnbFroz2.getTranbr(),CommTools.getBaseRunEnvs().getTrxn_branch()))
		// {
		// throw DpModuleError.DpstComm.E9999("续冻交易发起机构必须是原冻结机构");
		// }

		if (!cplioctfrozin.getFrogna().equals(tblKnbFroz2.getFrogna())) {
			throw DpModuleError.DpstComm.BNAS0105();
		}

		if (cplioctfrozin.getFrexog() != tblKnbFroz2.getFrexog()) {
			throw DpModuleError.DpstComm.BNAS0270();
		}

		// 冻结分类码不能为空
		// if (CommUtil.isNull(tblKnbFroz.getFrozcd())) {
		// throw DpModuleError.DpstComm.E9027(DpDict.Froz.frozcd.getLongName());
		// }

		// 获取冻结定义
		if (CommUtil.isNull(tblKnbFroz.getFrozcd())) {
			throw DpModuleError.DpstComm.BNAS0831(tblKnbFroz.getFrozcd());
		}

		// 冻结主体不能为空
		if (CommUtil.isNull(tblKnbFroz.getFrozow())) {
			throw DpModuleError.DpstComm.E9027(DpDict.Froz.frozow.getLongName());
		}

		// 续冻日期校验

		// 日期格式检查
		if (!DateTools2.chkIsDate(cplioctfrozin.getFreddt())) {
			throw DpModuleError.DpstComm.BNAS0818();
		}

		// 原冻结到期日
		String freddt = tblKnbFroz.getFreddt();

		// 最大序号对应的冻结开始日
		String frozdt = tblKnbFroz.getFrbgdt();

		if (CommUtil.compare(CommTools.getBaseRunEnvs().getTrxn_date(), frozdt) < 0) {
			throw DpModuleError.DpstComm.BNAS0735();
		}

		if (!cplioctfrozin.getFrozdt().equals(freddt)) {
			throw DpModuleError.DpstComm.BNAS0826();
		}

		// if (CommUtil.compare(freddt, CommTools.getBaseRunEnvs().getTrxn_date()) < 0)
		// {
		// throw DpModuleError.DpstComm.E9999("续冻手续应该在冻结期满前办理");
		// }

		// 到期日检查
		if (CommUtil.compare(cplioctfrozin.getFreddt(), freddt) <= 0) {
			throw DpModuleError.DpstComm.BNAS0272();
		}

		if (CommUtil.compare(cplioctfrozin.getFreddt(), CommTools.getBaseRunEnvs().getTrxn_date()) <= 0) {
			throw DpModuleError.DpstComm.BNAS0271(CommTools.getBaseRunEnvs().getTrxn_date());
		}
		// 司法冻结冻结时间不能超过12个月
		// String sMaxDate = DateTimeUtil.dateAdd("mm", freddt, 12);
		// String date = DateTools2.calDateByTerm(freddt, "1Y", null, null, 3,
		// "A");
		String date = DateTools2.calDateByTerm(freddt, "1Y", null, "A");
		// sMaxDate = DateTimeUtil.dateAdd("dd", sMaxDate, -1);

		if (CommUtil.compare(cplioctfrozin.getFreddt(), date) > 0) {
			throw DpModuleError.DpstComm.BNAS1046();
		}

		// 续冻金额校验
		if (tblKnbFroz.getFrlmtp() == E_FRLMTP.AMOUNT) {

			if (CommUtil.isNull(cplioctfrozin.getFrozam())) {
				throw DpModuleError.DpstComm.E9027(DpDict.Froz.frozam.getLongName());
			}

			if (CommUtil.compare(cplioctfrozin.getFrozam(), BigDecimal.ZERO) <= 0) {
				throw DpModuleError.DpstComm.BNAS0269();
			}
		} else {
			// 非金额冻结不能输入金额
			if (!(CommUtil.isNull(cplioctfrozin.getFrozam())
					|| CommUtil.equals(cplioctfrozin.getFrozam(), BigDecimal.ZERO))) {
				throw DpModuleError.DpstComm.BNAS1137(tblKnbFroz.getFrlmtp().toString());
			}
		}

		// 获取冻结明细登记薄中该冻结编号下冻结序号最大的唯一一条记录
		long frozsq = getDetlFrozsq(cplioctfrozin);

		KnbFrozDetl tblKnbFrozDetl = KnbFrozDetlDao.selectOne_odb2(cplioctfrozin.getFrozno(), frozsq, true);

		if (CommUtil.compare(cplioctfrozin.getFrozam(), tblKnbFrozDetl.getFrozbl()) > 0) {
			// throw DpModuleError.DpstComm.E9999("续冻时，续冻金额不能大于冻结余额，当前有效的最大续冻金额应为[" +
			// tblKnbFrozDetl.getFrozbl()+ "]");
			throw DpModuleError.DpstComm.BNAS0268();
		}

		// long num = DpFrozDao.selFrctno(cplioctfrozin.getFrexog(),
		// tblKnbFroz.getFrogna(), cplioctfrozin.getFrctno(), true);
		// if (num > 0) {
		// throw DpModuleError.DpstComm.E9999("同一续冻通知书不能续冻多次");
		// }
	}

	/**
	 * TODO 客户账号冻结
	 * 
	 * @param tblKnbFroz
	 */

	public static void custacStopPay(KnbFrozDetl tblKnbfrozdetl, KnbFroz tblKnbFroz) {

		// registForzDetl(tblKnbfrozdetl,tblKnbFroz);
	}

	/**
	 * 智能储蓄冻结(冻结和止付都有用到)
	 * 
	 */
	public static void auacctStopPay(KnbFrozDetl tblKnbfrozdetl, KnbFroz tblKnbFroz) {

		registForzDetl(tblKnbfrozdetl, tblKnbFroz);
	}
	

	/**
	 * @author douwenbo
	 * @date 2016-04-26 09:54
	 * 
	 *       智能储蓄续冻
	 * 
	 */
	public static void auacctCtFroz(KnbFrozDetl tblKnbfrozdetl, KnbFroz tblKnbFroz) {

		registCtFrozForzDetl(tblKnbfrozdetl, tblKnbFroz);
	}

	/**
	 * TODO 单户冻结
	 * 
	 */
	public static void oneAcctStopPay(KnbFrozDetl tblKnbfrozdetl, KnbFroz tblKnbFroz) {

		/* 登记冻结主体登记簿 */
		KnbFrozOwne KnbFrozOwne = KnbFrozOwneDao.selectOne_odb1(E_FROZOW.ACCTNO, tblKnbfrozdetl.getFrowid(), false);
		if (CommUtil.isNull(KnbFrozOwne)) {
			KnbFrozOwne knbFOwne = SysUtil.getInstance(KnbFrozOwne.class);
			knbFOwne.setCorpno(BusiTools.getTranCorpno());
			knbFOwne.setFrotfg(cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES);
			knbFOwne.setFrowid(tblKnbfrozdetl.getFrowid());
			knbFOwne.setFrozbl(tblKnbfrozdetl.getFrozam());
			knbFOwne.setFrozow(E_FROZOW.ACCTNO);
			KnbFrozOwneDao.insert(knbFOwne);
		}
		registForzDetl(tblKnbfrozdetl, tblKnbFroz);
	}

	/*
	 * 登记冻结信息（冻结和止付都有用到）
	 */
	public static void registForzDetl(KnbFrozDetl tblKnbfrozdetl, KnbFroz tblKnbFroz) {

		tblKnbfrozdetl.setFrozow(tblKnbFroz.getFrozow());
		tblKnbfrozdetl.setFrozno(tblKnbFroz.getFrozno());
		tblKnbfrozdetl.setFrozst(E_FROZST.VALID);

		tblKnbfrozdetl.setFrsbsq(genSubFrozSq(tblKnbfrozdetl.getFrowid(), tblKnbFroz.getFrozlv()));

		KnbFrozDetlDao.insert(tblKnbfrozdetl);

		// 若为客户止付需单独处理冻结主体登记簿
		if (tblKnbFroz.getFroztp() == E_FROZTP.CUSTSTOPAY) {// 客户止付
			mntnKnbFrozOwne(tblKnbFroz.getFrozow(), tblKnbfrozdetl.getFrowid(), tblKnbFroz.getFrlmtp(),
					tblKnbfrozdetl.getFrozam());
		} else {
			mergeKnbFrozOwne(tblKnbFroz.getFrozow(), tblKnbfrozdetl.getFrowid(), tblKnbFroz.getFrlmtp(),
					tblKnbfrozdetl.getFrozam());
		}

	}
	
	
	/**
	 * 即富特殊改造 add by lishuyao 20191211
	 * @param frozow
	 * @param frowid
	 * @param frlmtp
	 * @param frozam
	 * @param stactp
	 * @param fricfg
	 */
	private static void regKnbFrozOwne(Options<acctInfo> acctList, KnbFrozDetl tblKnbfrozdetl, KnbFroz tblKnbFroz) {
		E_FROZOW frozow = tblKnbfrozdetl.getFrozow();
		String frowid = tblKnbfrozdetl.getFrowid();
		String frozno = tblKnbfrozdetl.getFrozno();
		
		KnbFrozOwne tblKnbFrozOwne = KnbFrozOwneDao.selectOneWithLock_odb1(frozow, frowid, false);
		if (CommUtil.isNull(tblKnbFrozOwne)) {
			tblKnbFrozOwne = SysUtil.getInstance(KnbFrozOwne.class);
			tblKnbFrozOwne.setFrozow(tblKnbfrozdetl.getFrozow());
			tblKnbFrozOwne.setFrowid(tblKnbfrozdetl.getFrowid());
		}
		
		BigDecimal acfztm = crcleFrozAcct(frozno, frozow, frowid, acctList);
		bizlog.debug("<<<<<<<<<<<<<<<<<<<<<<<< 子账户冻结总额   >>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + acfztm);
		// 剩余冻结金额大于0，则将金额冻结到电子账户上
		if (CommUtil.compare(tblKnbfrozdetl.getFrozam(), acfztm) > 0) {
			// 查询电子账户冻结记录
			KnbFrozOwne tblKnbFrozOwne2 = KnbFrozOwneDao.selectOneWithLock_odb1(frozow, frowid, false);
			if (CommUtil.isNull(tblKnbFrozOwne2)) {
				tblKnbFrozOwne2.setFrozow(frozow);
				tblKnbFrozOwne2.setFrowid(frowid);
				tblKnbFrozOwne2.setFrozbl(tblKnbfrozdetl.getFrozam().subtract(acfztm));
				KnbFrozOwneDao.insert(tblKnbFrozOwne2);
			}else {
				tblKnbFrozOwne2.setFrozbl(tblKnbFrozOwne2.getFrozbl().add(tblKnbfrozdetl.getFrozam().subtract(acfztm)));
				KnbFrozOwneDao.updateOne_odb1(tblKnbFrozOwne2);
			}
			
			// 登记冻结明细
			KnbFrozAcctDetl tblKnbFrozAcctDetl = SysUtil.getInstance(KnbFrozAcctDetl.class);
			tblKnbFrozAcctDetl.setFrozno(frozno);
			tblKnbFrozAcctDetl.setFrozsq(Long.valueOf(acctList.size()));
			tblKnbFrozAcctDetl.setFrozow(frozow);
			tblKnbFrozAcctDetl.setFrowid(frowid);
			tblKnbFrozAcctDetl.setFrozam(tblKnbfrozdetl.getFrozam().subtract(acfztm));
			tblKnbFrozAcctDetl.setFzrmam(tblKnbfrozdetl.getFrozam().subtract(acfztm));
			KnbFrozAcctDetlDao.insert(tblKnbFrozAcctDetl);
		}
		
		if (CommUtil.compare(tblKnbfrozdetl.getFrozam(), acfztm) < 0) {
			throw DpModuleError.DpAcct.AT020057();
		}
	}

	public static BigDecimal crcleFrozAcct(String frozno, E_FROZOW frozow, String custac, 
			Options<acctInfo> acctList) {
		bizlog.debug("<<<<<<<<<<<<<<<<<<<< crcleFrozAcct >>>>>>>>>>>>>>>>>>>>>");
		KnbFrozAcctDetl tblKnbFrozAcctDetl = SysUtil.getInstance(KnbFrozAcctDetl.class);
		tblKnbFrozAcctDetl.setFrozno(frozno);
		tblKnbFrozAcctDetl.setFrozow(frozow);
		KnbFrozOwne tblKnbFrozOwne = null;
		BigDecimal acfztm = BigDecimal.ZERO;//子账户冻结总额
		for (int i=0;i<acctList.size();i++) {
			bizlog.debug("<<<<<<<<<<<<<<<<<<<< acctno >>>>>>>>>>>>>>>>>>>>>" + acctList.get(i).getAcctid());
			if (CommUtil.compare(BigDecimal.ZERO, acctList.get(i).getFrozam()) >= 0) {
				throw DpModuleError.DpAcct.AT020063();
			}
			
			KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(acctList.get(i).getAcctid(), false);
			if (CommUtil.isNull(tblKnaSbad)) {
				throw DpModuleError.DpAcct.AT020054(acctList.get(i).getAcctid());
			}
			
			if (!CommUtil.equals(custac, tblKnaSbad.getCustac())) {
				throw DpModuleError.DpAcct.AT020064(acctList.get(i).getAcctid());
			}
			
			tblKnbFrozOwne = KnbFrozOwneDao.selectOneWithLock_odb1(frozow, tblKnaSbad.getAcctno(), false);
			if (CommUtil.isNull(tblKnbFrozOwne)) {
				tblKnbFrozOwne = SysUtil.getInstance(KnbFrozOwne.class);
				tblKnbFrozOwne.setFrozbl(acctList.get(i).getFrozam());
				tblKnbFrozOwne.setFrozow(frozow);
				tblKnbFrozOwne.setFrowid(tblKnaSbad.getAcctno());
				KnbFrozOwneDao.insert(tblKnbFrozOwne);
			} else {
				tblKnbFrozOwne.setFrozbl(tblKnbFrozOwne.getFrozbl().add(acctList.get(i).getFrozam()));
				KnbFrozOwneDao.updateOne_odb1(tblKnbFrozOwne);
			}
			
			// 登记子账户冻结明细
			tblKnbFrozAcctDetl.setFrozsq(Long.valueOf(i));
			tblKnbFrozAcctDetl.setFrowid(tblKnaSbad.getAcctno());
			tblKnbFrozAcctDetl.setFrozam(acctList.get(i).getFrozam());
			tblKnbFrozAcctDetl.setFzrmam(acctList.get(i).getFrozam());
			KnbFrozAcctDetlDao.insert(tblKnbFrozAcctDetl);
			acfztm = acfztm.add(acctList.get(i).getFrozam());
		}
		
		return acfztm;
	}
	/**
	 * @author douwenbo
	 * @date 2016-04-26 10:29 续冻交易-登记冻结明细登记薄
	 */
	public static void registCtFrozForzDetl(KnbFrozDetl tblKnbfrozdetl, KnbFroz tblKnbFroz) {

		tblKnbfrozdetl.setFrozow(tblKnbFroz.getFrozow());
		tblKnbfrozdetl.setFrozno(tblKnbFroz.getFrozno());
		tblKnbfrozdetl.setFrozst(E_FROZST.VALID);

		tblKnbfrozdetl.setFrsbsq(genSubFrozSq(tblKnbfrozdetl.getFrowid(), tblKnbFroz.getFrozlv()));

		KnbFrozDetlDao.insert(tblKnbfrozdetl);
		// 续冻不需要更新冻结主体登记簿
		// mergeKnbFrozOwne(tblKnbFroz.getFrozow(), tblKnbfrozdetl.getFrowid(),
		// tblKnbFroz.getFrlmtp(), tblKnbfrozdetl.getFrozam());
	}

	// 更改或登记冻结主体信息
	public static void mergeKnbFrozOwne(E_FROZOW frozow, String frowid, E_FRLMTP frlmtp, BigDecimal frozam) {

		KnbFrozOwne tblKnbFrozOwne = null;

		try {
			tblKnbFrozOwne = KnbFrozOwneDao.selectOneWithLock_odb1(frozow, frowid, true);

			if (frlmtp == E_FRLMTP.ALL) // 全额冻结
				tblKnbFrozOwne.setFralfg(E_YES___.YES);

			else if (frlmtp == E_FRLMTP.IN) // 只付不收
				tblKnbFrozOwne.setFrinfg(E_YES___.YES);

			else if (frlmtp == E_FRLMTP.OUT) // 只收不付
				tblKnbFrozOwne.setFrotfg(E_YES___.YES);

			else if (frlmtp == E_FRLMTP.AMOUNT) // 指定金额
				tblKnbFrozOwne.setFrozbl(tblKnbFrozOwne.getFrozbl().add(frozam));

			KnbFrozOwneDao.updateOne_odb1(tblKnbFrozOwne);
		} catch (Exception e) {

			tblKnbFrozOwne = SysUtil.getInstance(KnbFrozOwne.class);
			tblKnbFrozOwne.setFrozow(frozow);
			tblKnbFrozOwne.setFrowid(frowid);
			tblKnbFrozOwne.setFrozbl(frozam);
			if (frlmtp == E_FRLMTP.ALL) // 全额冻结
				tblKnbFrozOwne.setFralfg(E_YES___.YES);

			else if (frlmtp == E_FRLMTP.IN) // 只付不收
				tblKnbFrozOwne.setFrinfg(E_YES___.YES);

			else if (frlmtp == E_FRLMTP.OUT) // 只收不付
				tblKnbFrozOwne.setFrotfg(E_YES___.YES);

			else if (frlmtp == E_FRLMTP.AMOUNT) // 指定金额
				tblKnbFrozOwne.setFrozbl(frozam);

			KnbFrozOwneDao.insert(tblKnbFrozOwne);
		}

	}

	// 维护冻结主体信息(只实用于客户止付)
	public static void mntnKnbFrozOwne(E_FROZOW frozow, String frowid, E_FRLMTP frlmtp, BigDecimal frozam) {

		KnbFrozOwne tblKnbFrozOwne = null;

		try {

			tblKnbFrozOwne = DpFrozDao.selKnbFrozOwneInfo(frozow, frowid, true);

			tblKnbFrozOwne.setFrotfg(E_YES___.YES);// 限制类型为只收不付

			// 更新冻结主体登记簿
			KnbFrozOwneDao.updateOne_odb1(tblKnbFrozOwne);

		} catch (Exception e) {

			tblKnbFrozOwne = SysUtil.getInstance(KnbFrozOwne.class);
			tblKnbFrozOwne.setFrozow(frozow);
			tblKnbFrozOwne.setFrowid(frowid);
			tblKnbFrozOwne.setFrotfg(E_YES___.YES);
			KnbFrozOwneDao.insert(tblKnbFrozOwne);
		}
	}

	public static void prcFroz(IoDpStopPayIn cpliodpfrozin, DpFrozEntity entity) {

		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();

		long frozsq = 1l;// 冻结序号。默认为1，续冻时在最大序号上加1

		if (cpliodpfrozin.getFroztp() == E_FROZTP.ADD) {
			frozsq = DpFrozDao.selFrozMaxseq(cpliodpfrozin.getFrozno(), false);
			frozsq += 1;
		}

		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);

		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(cpliodpfrozin.getCardno(), false);

		if (CommUtil.isNull(caKnaAcdc)) {
			throw DpModuleError.DpAcct.AT020028();
		}

		// if(caKnaAcdc.getStatus() != E_DPACST.NORMAL ){
		// throw DpModuleError.DpstComm.E9999("根据电子账号获取电子账号ID的状态不正常");
		// }
		// 电子账号ID
		String custac = caKnaAcdc.getCustac();

		// TODO 暂只支持智能储蓄冻结，指定子户冻结可能会造成智能储蓄定活互转日终步骤失败及结息失败
		// if (cpliodpfrozin.getFrozow() != E_FROZOW.AUACCT) {
		// throw DpModuleError.DpstComm.E9999("暂只支持智能储蓄冻结。");
		// }
		// String custac = null;// 电子账号
		// // 如果有卡号则获取对应的电子账号
		// if (CommUtil.isNotNull(cpliodpfrozin.getCardno())) {
		// custac = Kna_acdcDao
		// .selectOne_odb2(cpliodpfrozin.getCardno(), true)
		// .getCustac();
		// }
		//
		// // 输入的电子账号与卡都不为空则进行匹配性检查
		// if (CommUtil.isNotNull(cpliodpfrozin.getCustac())
		// && CommUtil.isNotNull(cpliodpfrozin.getCardno())) {
		// if (!CommUtil.equals(cpliodpfrozin.getCustac(), custac)) {
		// throw DpModuleError.DpstComm.E9999("输入卡号["
		// + cpliodpfrozin.getCardno() + "]与电子账号["
		// + cpliodpfrozin.getCustac() + "]不匹配");
		// }
		// }

		// // 电子账号为空，则用卡查询出的电子账号进行赋值
		// if (CommUtil.isNull(cpliodpfrozin.getCustac())) {
		// cpliodpfrozin.setCustac(custac);
		// }

		// 获取冻结定义
		// KnpFroz tblKnpFroz =
		// KnpFrozDao.selectOne_odb1(cpliodpfrozin.getFrozcd(), false);

		KnpFroz tblKnpFroz = DpFrozDao.selFrozInfoByFrozcd(cpliodpfrozin.getFrozcd(),
				CommTools.getBaseRunEnvs().getBusi_org_id(), false);

		if (CommUtil.isNull(tblKnpFroz))
			throw DpModuleError.DpstComm.BNAS0831(cpliodpfrozin.getFrozcd());

		// 获取客户账号信息
		IoCaKnaCust tblKna_cust = SysUtil.getInstance(IoCaSevQryTableInfo.class).
				getKnaCustByCustacOdb1(custac, true);

		// 客户账户状态检查
		// if (tblKna_cust.getAcctst() != E_ACCTST.NORMAL) {
		// throw DpModuleError.DpstComm.E9999("客户账号[" + tblKna_cust.getCustac()+
		// "]不正常，不能做冻结业务");
		// }
		// 账户状态为预开户、转久悬、预销户、销户的，交易拒绝，报错：“电子账户状态为***状态，无法操作！”。
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);

		if (cuacst == E_CUACST.CLOSED) {
			throw DpModuleError.DpstComm.BNAS1597();

		} else if (cuacst == E_CUACST.PREOPEN) {
			throw DpModuleError.DpstComm.BNAS0849();
		}
		// else if (cuacst == E_CUACST.PRECLOS) {
		// throw DpModuleError.DpstComm.E9999("电子账户状态为预销户状态，无法操作！");
		//
		// }

		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		// 交易发起法人需与电子账号法人一致
		if (!CommUtil.equals(tblKna_cust.getCorpno(), corpno)) {
			throw DpModuleError.DpstComm.BNAS0793();
		}

		// 冻结基本检查
		stopPayCheck(cpliodpfrozin, tblKnpFroz);

		// 生成冻结编号
		KnpParameter tblknp_para = KnpParameterDao.selectOne_odb1("IS_CHECK", "SPECTP", "%", "%", false);
		if (CommUtil.isNull(tblknp_para)) {
			throw DpModuleError.DpstComm.BNAS1210();
		}

		String frozno = null;

		if (CommUtil.equals("Y", tblknp_para.getParm_value1())) {
			String specno = "21" + CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4);

			frozno = getMaxSpecno(E_SPECTP.FREEZE, specno);

		} else if (CommUtil.equals("N", tblknp_para.getParm_value1())) {
			// 生成冻结编号
			frozno = "21" + CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4)
					+ genFrozno(tblKnpFroz.getFroztp(), CommTools.getBaseRunEnvs().getTrxn_date(),
							CommTools.getBaseRunEnvs().getTrxn_branch());
		}

		KnbFroz tblKnbFroz = SysUtil.getInstance(KnbFroz.class);

		// 基本冻结信息
		tblKnbFroz.setFrozsq(frozsq);// 冻结序号
		tblKnbFroz.setFroztp(cpliodpfrozin.getFroztp()); // 冻结业务类型
		tblKnbFroz.setFrozcd(cpliodpfrozin.getFrozcd()); // 冻结分类码
		tblKnbFroz.setFrozow(cpliodpfrozin.getFrozow()); // 冻结主体类型
		tblKnbFroz.setFrlmtp(cpliodpfrozin.getFrlmtp()); // 限制类型
		tblKnbFroz.setFrozlv(tblKnpFroz.getFrozlv()); // 冻结级别
		tblKnbFroz.setFrozno(frozno);// 冻结编号
		if (JFBaseEnumType.E_STACTP.STSA == cpliodpfrozin.getStactp()) {
			custac = cpliodpfrozin.getAcctno();
		}
		tblKnbFroz.setCustac(custac); // 客户账号
		tblKnbFroz.setFrozdt(trandt); // 冻结日期
		tblKnbFroz.setFroztm(CommTools.getBaseRunEnvs().getComputer_time()); // 冻结时间
		tblKnbFroz.setFrbgdt(trandt); // 冻结起始日期
		tblKnbFroz.setFreddt(cpliodpfrozin.getFreddt()); // 冻结终止日期
		tblKnbFroz.setFrreas(cpliodpfrozin.getFrreas()); // 冻结原因
		tblKnbFroz.setFrozst(E_FROZST.VALID); // 解冻标志
		tblKnbFroz.setStopms(null);// 止付措施

		tblKnbFroz.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
		tblKnbFroz.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq()); // 交易流水
		tblKnbFroz.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller()); // 交易柜员
		tblKnbFroz.setCkuser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 授权柜员
		// tblKnbFroz.setCptrcd(CommTools.getBaseRunEnvs().getCptrcd()); // 对账代码
		tblKnbFroz.setLttscd(BusiTools.getBusiRunEnvs().getTrxn_code()); // 内部交易码

		// 司法冻结登记
		tblKnbFroz.setFrctno(cpliodpfrozin.getFrctno()); // 冻结通知书编号
		tblKnbFroz.setFrcttp(cpliodpfrozin.getFrcttp()); // 冻结文书种类
		tblKnbFroz.setFrexog(cpliodpfrozin.getFrexog()); // 执法部门类型
		tblKnbFroz.setFrogna(cpliodpfrozin.getFrogna()); // 执法部门名称
		tblKnbFroz.setIdtp01(cpliodpfrozin.getIdtp01()); // 执法人员1证件种类
		tblKnbFroz.setIdno01(cpliodpfrozin.getIdno01()); // 执法人员1证件号码
		tblKnbFroz.setFrna01(cpliodpfrozin.getFrna01()); // 执法人员1名称
		tblKnbFroz.setIdtp02(cpliodpfrozin.getIdtp02()); // 执法人员2证件种类
		tblKnbFroz.setIdno02(cpliodpfrozin.getIdno02()); // 执法人员2证件号码
		tblKnbFroz.setFrna02(cpliodpfrozin.getFrna02()); // 执法人员2名称
		tblKnbFroz.setPruser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 审批柜员
		tblKnbFroz.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
		tblKnbFroz.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
		// tblKnbFroz.setFrspfg(cpliodpfrozin.getFrspfg()); // 轮候冻结标志
		tblKnbFroz.setCrcycd(cpliodpfrozin.getCrcycd()); // 币种
		tblKnbFroz.setCsextg(cpliodpfrozin.getCsextg()); // 账户钞会标志
		tblKnbFroz.setRemark(cpliodpfrozin.getRemark()); // 备注
		tblKnbFroz.setStactp(cpliodpfrozin.getStactp());
		if (CommUtil.isNotNull(cpliodpfrozin.getIdno01())) {
			tblKnbFroz.setTmidno01(DecryptConstant.maskIdCard(cpliodpfrozin.getIdno01()));
		}
		if (CommUtil.isNotNull(cpliodpfrozin.getIdno02())) {
			tblKnbFroz.setTmidno02(DecryptConstant.maskIdCard(cpliodpfrozin.getIdno02()));
		}
		if (CommUtil.isNotNull(cpliodpfrozin.getFrna01())) {
			tblKnbFroz.setTmfrna01(DecryptConstant.maskName(cpliodpfrozin.getFrna01()));
		}
		if (CommUtil.isNotNull(cpliodpfrozin.getFrna02())) {
			tblKnbFroz.setTmfrna02(DecryptConstant.maskName(cpliodpfrozin.getFrna02()));
		}
		// 登记冻结登记簿
		KnbFrozDao.insert(tblKnbFroz);

		KnbFrozDetl tblKnbfrozdetl = SysUtil.getInstance(KnbFrozDetl.class);

		// 冻结模式按冻结对象的类型来划分，不要将所有子户冻结也放在此处
		switch (cpliodpfrozin.getFrozow()) {
		case ACCTNO:
			// TODO 单户冻结检查
			// 币种，钞汇标志检查
			// accsCheck(cpliodpfrozin, tblKna_accs);

			tblKnbfrozdetl.setFrowid(cpliodpfrozin.getAcctno());
			tblKnbfrozdetl.setFrozam(cpliodpfrozin.getFrozam());
			tblKnbfrozdetl.setFrozbl(cpliodpfrozin.getFrozam());
			tblKnbfrozdetl.setFrozsq(frozsq);
			oneAcctStopPay(tblKnbfrozdetl, tblKnbFroz);
			break;
		// case ALL_DPAC:
		// // TODO 冻结产品指定为？？还是不限制？？
		// List<kna_accs> lstKna_accs = Kna_accsDao.selectAll_odb7(
		// cpliodpfrozin.getCustac(), "100001", false);
		//
		// // TODO 所有子户冻结检查
		// for (kna_accs tblKna_accs_tmp : lstKna_accs) {
		//
		// tblKnbfrozdetl.setAcctno(tblKna_accs_tmp.getAcctno());
		// tblKnbfrozdetl.setFrozam(cpliodpfrozin.getFrozam());
		// tblKnbfrozdetl.setFrozbl(cpliodpfrozin.getFrozam());
		// tblKnbfrozdetl.setFrozsq(frozsq);
		// oneAcctStopPay(tblKnbfrozdetl,tblKnbFroz);
		// frozsq++;
		// }
		// throw DpModuleError.DpstComm.E9999("暂不支持所有子户冻结。");
		/*
		 * case CUSTAC: // TODO 客户账号冻结检查(客户账号冻结暂不实现) tblKnbfrozdetl.setFrowid(custac);
		 * tblKnbfrozdetl.setFrozam(cpliodpfrozin.getFrozam());
		 * tblKnbfrozdetl.setFrozbl(cpliodpfrozin.getFrozam());
		 * tblKnbfrozdetl.setFrozsq(frozsq);
		 * 
		 * custacStopPay(tblKnbfrozdetl, tblKnbFroz); break;
		 */
		case AUACCT:
			if (JFBaseEnumType.E_STACTP.STSA == cpliodpfrozin.getStactp()) {
				custac = cpliodpfrozin.getAcctno();
			}
			tblKnbfrozdetl.setFrowid(custac);
			tblKnbfrozdetl.setFrozam(cpliodpfrozin.getFrozam());
			tblKnbfrozdetl.setFrozbl(cpliodpfrozin.getFrozam());
			tblKnbfrozdetl.setFrozsq(frozsq);

			auacctStopPay(tblKnbfrozdetl, tblKnbFroz);
			break;
		default:
			throw DpModuleError.DpstComm.BNAS1145(cpliodpfrozin.getFrozow().getLongName());
		}

		// 需要返回的实体进行赋值
		entity.setFrozno(frozno);
		entity.setCustno(tblKna_cust.getCustno());
	}

	/**
	 * @author douwenbo
	 * @date 2016-04-26 20:11 止付基本检查
	 * 
	 * @param stopayin
	 */
	public static void stopayCheck(IoDpStopayIn stopayin) {
		IoDpKnaAcct acctInfo = SysUtil.getInstance(IoDpKnaAcct.class);

		if (JFBaseEnumType.E_STACTP.STSA == stopayin.getStactp()) {

			acctInfo = DpFrozDao.selAcctInfoByAcalno(stopayin.getAcctno(), false);
			if (acctInfo != null) {
				stopayin.setAcctno(acctInfo.getAcctno());
			} else {
				throw DpModuleError.DpAcct.AT020035();
			}
		}

		if (CommUtil.isNull(stopayin.getSptype()) && CommUtil.isNull(stopayin.getCustop())) {
			throw DpModuleError.DpstComm.BNAS0091();
		}

		/*
		 * 4JF：目前止付场景只有1-银行止付（即富止付）。
		 */
		// 转换，如果传进止付类型为1-银行止付转为 3-银行止付
		if (stopayin.getSptype() == E_SPTYPE.BANKSTOPAY) {
			stopayin.setStoptp(E_STOPTP.BANKSTOPAY);
		}

		// 转换，如果传进止付类型为2-外部止付转为 4-外部止付
		if (stopayin.getSptype() == E_SPTYPE.EXTSTOPAY) {
			stopayin.setStoptp(E_STOPTP.EXTSTOPAY);
		}

		// 账户保护转换 :1-账户保护转换为5-客户止付
		if (E_CUSTOP.ACCTSTOP == stopayin.getCustop()) {
			stopayin.setStoptp(E_STOPTP.CUSTSTOPAY);
		}

		if (CommUtil.isNotNull(stopayin.getCustop())) {
			if (E_CUSTOP.ACCTSTOP != stopayin.getCustop()) {
				throw DpModuleError.DpstComm.BNAS1602();
			}
		}

		/*
		 * 4JF
		 */
		if (CommUtil.isNull(stopayin.getCardno())) {
			throw DpModuleError.DpstProd.BNAS0926();
		}

		/*
		 * 4JF：查询电子账户是否存在。
		 */
		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(stopayin.getCardno(), false);
		if (CommUtil.isNull(caKnaAcdc)) {
			throw CaError.Eacct.BNAS1659();
		}
		/*
		 * 4JF：查询币种是否与子户开户币种一致。
		 */
		// IoDpKnaAcct cplKnaAcct=SysUtil.getInstance(IoDpKnaAcct.class);
		// if(JFBaseEnumType.E_STACTP.STMA==stopayin.getStactp()) {
		// cplKnaAcct =
		// SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(caKnaAcdc.getCustac());
		// if (!CommUtil.equals(stopayin.getCrcycd(), cplKnaAcct.getCrcycd())) {
		// throw DpModuleError.DpstComm.BNAS0313();
		// }
		if (JFBaseEnumType.E_STACTP.STSA == stopayin.getStactp()) {
			if (acctInfo != null) {
				if (!CommUtil.equals(stopayin.getCrcycd(), acctInfo.getCrcycd())) {
					throw DpModuleError.DpstComm.BNAS0313();
				}
			} else {
				throw DpModuleError.DpAcct.AT020035();
			}
		}

		/*
		 * 4JF
		 */
		// 银行止付或者外部止付时字段校验必输
		if (E_STOPTP.BANKSTOPAY == stopayin.getStoptp() || E_STOPTP.EXTSTOPAY == stopayin.getStoptp()) {

			// 止解方式
			if (CommUtil.isNull(stopayin.getStuntp())) {
				throw DpModuleError.DpstComm.BNAS0069();
			}

			// 币种
			if (CommUtil.isNull(stopayin.getCrcycd())) {
				throw DpModuleError.DpstComm.BNAS1101();
			}

			// 钞汇标志针对外币使用，如果是人民币，钞汇标志为Null。
			if (CommUtil.equals(stopayin.getCrcycd(), BusiTools.getDefineCurrency())
					&& CommUtil.isNotNull(stopayin.getCsextg())) {
				throw DpModuleError.DpstComm.BNAS1099();
			}

			// 止付类型是1-银行止付时，止付措施为Null。
			if (stopayin.getStoptp() != E_STOPTP.EXTSTOPAY) {
				if (CommUtil.isNotNull(stopayin.getStopms())) {
					throw DpModuleError.DpstComm.BNAS0783();
				}
			}

			// 止付类型客户止付或者外部止付时，止解方式不能为部止或质押
			if (stopayin.getStoptp() == E_STOPTP.CUSTSTOPAY || stopayin.getStoptp() == E_STOPTP.EXTSTOPAY) {
				if (stopayin.getStuntp().equals(E_STUNTP.PORSTO) || stopayin.getStuntp().equals(E_STUNTP.PLEDGE)) {
					throw DpModuleError.DpstComm.BNAS0090();
				}
			}

			// 止付类型为银行止付或者客户止付时，止付措施不可选
			if (stopayin.getStoptp().equals(E_STOPTP.BANKSTOPAY) || stopayin.getStoptp().equals(E_STOPTP.CUSTSTOPAY)) {
				if (CommUtil.isNotNull(stopayin.getStopms())) {
					throw DpModuleError.DpstComm.BNAS0088();
				}
			}

			// 止付类型为外部止付时，止付措施不能为空
			if (stopayin.getStoptp().equals(E_STOPTP.EXTSTOPAY)) {
				if (CommUtil.isNull(stopayin.getStopms())) {
					throw DpModuleError.DpstComm.BNAS0089();
				}
			}

			// 止解方式为全止时，限制类型设置为只进不出冻结
			if (stopayin.getStuntp().equals(E_STUNTP.ALLSTOP)) {
				stopayin.setFrlmtp(E_FRLMTP.OUT);
				if (CommUtil.compare(stopayin.getStopam(), BigDecimal.ZERO) != 0) {
					throw DpModuleError.DpstComm.BNAS0379();
				}
			}

			// 止解方式为部止或者质押时，限制类型设置为指定金额
			if (stopayin.getStuntp().equals(E_STUNTP.PORSTO) || stopayin.getStuntp().equals(E_STUNTP.PLEDGE)) {
				stopayin.setFrlmtp(E_FRLMTP.AMOUNT);
				if (CommUtil.isNull(stopayin.getStopam())) {
					throw DpModuleError.DpstComm.BNAS0067();
				}
				if (CommUtil.compare(stopayin.getStopam(), BigDecimal.ZERO) <= 0) {
					throw DpModuleError.DpstComm.BNAS1128("止解方式为部止时，止付金额必须大于零");
				}
			}
			/*
			 * JF Modify：即富客户电子账户可用余额计算。 可支取余额为0分两种情况： 1.当电子账户处于银行全止时；
			 * 2.当电子账户处于银行部止时，存款总额（活期总额+定期总额）小于冻结余额。
			 */
			if (stopayin.getStuntp().equals(E_STUNTP.PORSTO)) {
				BigDecimal acctbl = BigDecimal.ZERO;// 可支取余额

				// modify by yusheng 该地方取的是总资产的可用余额，而不是活期账户的余额。
				// 总资产可支取余额
				if (JFBaseEnumType.E_STACTP.STMA == stopayin.getStactp()) {
					acctbl = SysUtil.getInstance(DpAcctSvcType.class).getDrawnBalance(caKnaAcdc.getCustac(),
							stopayin.getCrcycd(), E_YES___.NO);
					/*
					 * acctbl = SysUtil.getInstance(DpAcctSvcType.class)
					 * .getAcctaAvaBal(caKnaAcdc.getCustac(), cplKnaAcct.getAcctno(),
					 * stopayin.getCrcycd(), E_YES___.YES, E_YES___.NO);
					 */
				} else if (JFBaseEnumType.E_STACTP.STSA == stopayin.getStactp()) {
					if (acctInfo != null) {
						acctbl = acctInfo.getOnlnbl();
					} else {
						throw DpModuleError.DpAcct.AT020035();
					}

				}
				bizlog.debug("当前可用余额：" + acctbl + ",止付余额：" + stopayin.getStopam());
				// if(CommUtil.compare(stopayin.getStopam(), acctbl)>0){
				// throw DpModuleError.DpstComm.BNAS0177();
				// }
			}

			if (CommUtil.isNull(stopayin.getStbkno()) && stopayin.getStoptp() == E_STOPTP.BANKSTOPAY) {
				throw DpModuleError.DpstComm.BNAS0968();
			}

			if (CommUtil.isNull(stopayin.getStbkno()) && stopayin.getStoptp() == E_STOPTP.EXTSTOPAY) {
				throw DpModuleError.DpstComm.BNAS0972();
			}

			// if (CommUtil.isNotNull(stopayin.getStbkno()) &&
			// stopayin.getStoptp() == E_STOPTP.CUSTSTOPAY) {
			// throw DpModuleError.DpstComm.E9999("当止付类型为客户止付时，止付书编号不能录入");
			// }
			if (CommUtil.isNull(stopayin.getSttmct())) {
				throw DpModuleError.DpstComm.BNAS0080();
			}

			/*
			 * 4JF
			 */
			// 银行止付或客户止付时，止付时间计算必须为天
			if (stopayin.getStoptp().equals(E_STOPTP.BANKSTOPAY)) {
				if (!stopayin.getSttmct().equals(E_STTMCT.DAY)) {
					throw DpModuleError.DpstComm.BNAS0969();
				}
			}

			// 如果止付时间按小时，止付时间必输;止付日期不可录入;止付(质押)到期日不可录入
			if (stopayin.getSttmct().equals(E_STTMCT.HOUR)) {

				if (CommUtil.isNull(stopayin.getSttmle())) {
					throw DpModuleError.DpstComm.BNAS0081();
				}

				if (CommUtil.compare(Long.parseLong(stopayin.getSttmle()), 0l) <= 0) {
					throw DpModuleError.DpstComm.BNAS1248();
				}

				if (CommUtil.compare(Long.parseLong(stopayin.getSttmle()), 48l) > 0) {
					throw DpModuleError.DpstComm.BNAS0076();
				}

				// 若按小时，取当前交易时间
				if (CommUtil.isNotNull(stopayin.getStoptm())) {
					throw DpModuleError.DpstComm.BNAS0082();
				}

				if (CommUtil.isNull(stopayin.getStoptm())) {
					stopayin.setStoptm(CommTools.getBaseRunEnvs().getComputer_time());
				}

				if (CommUtil.isNotNull(stopayin.getStpldt())) {
					throw DpModuleError.DpstComm.BNAS0083();
				}

			}

			/*
			 * 4JF
			 */
			// 止付时间计算选择按天时，止付类型选1-银行止付、3-客户止付时止付到期日期置灰不可输，选2-外部止付时到期日期可输
			// 如果止付时间按天，止付日期必输;止付时间不可录入;外部止付时到期日必输，银行止付和客户止付时到日期默认永久止付
			if (stopayin.getSttmct() == E_STTMCT.DAY) {

				if (CommUtil.isNull(stopayin.getStopdt())) {
					throw DpModuleError.DpstComm.BNAS0085();
				}
				// 止付日期检查
				if (!DateTools2.chkIsDate(stopayin.getStopdt())) {
					throw DpModuleError.DpAcct.AT020049();
				}
				if (CommUtil.compare(stopayin.getStopdt(), CommTools.getBaseRunEnvs().getTrxn_date()) != 0) {
					throw DpModuleError.DpAcct.AT020050(stopayin.getStopdt(), CommTools.getBaseRunEnvs().getTrxn_date());
				}
				if (CommUtil.isNull(stopayin.getSttmle())) {
					throw DpModuleError.DpstComm.BNAS0084();
				}

				if (E_STOPTP.EXTSTOPAY == stopayin.getStoptp()) {
					if (CommUtil.isNull(stopayin.getStpldt())) {
						throw DpModuleError.DpstComm.BNAS1128("止付到期日不能为空");
					}
				}

				if (stopayin.getStoptp() == E_STOPTP.BANKSTOPAY || stopayin.getStoptp() == E_STOPTP.CUSTSTOPAY) {
					if (CommUtil.isNotNull(stopayin.getStpldt())) {
						throw DpModuleError.DpstComm.BNAS0086();
					}
				}

				if (stopayin.getStoptp().equals(E_STOPTP.BANKSTOPAY)
						|| stopayin.getStoptp().equals(E_STOPTP.CUSTSTOPAY)) {
					if (CommUtil.isNotNull(stopayin.getStpldt())) {
						throw DpModuleError.DpstComm.BNAS0087();
					}
				}
			}

			// 日期格式检查
			if (CommUtil.isNotNull(stopayin.getStpldt())) {

				if (!DateTools2.chkIsDate(stopayin.getStpldt())) {
					throw DpModuleError.DpstComm.BNAS0094();
				}

				if (CommUtil.compare(stopayin.getStpldt(), CommTools.getBaseRunEnvs().getTrxn_date()) < 0) {
					throw DpModuleError.DpstComm.BNAS0095();
				}
			}

			// 解/止付部门类型，银行止付或客户止付时不可录入，外部止付时必输
			if (stopayin.getStoptp() == E_STOPTP.EXTSTOPAY && CommUtil.isNull(stopayin.getStdptp())) {
				throw DpModuleError.DpstComm.BNAS0973();
			}

			if ((stopayin.getStoptp() == E_STOPTP.BANKSTOPAY) && CommUtil.isNotNull(stopayin.getStdptp())) {
				throw DpModuleError.DpstComm.BNAS0970();
			}

			// 解/止付部门,银行止付与外部止付时必输，客户止付时不可录入
			if (CommUtil.isNull(stopayin.getStladp())) {
				throw DpModuleError.DpstComm.BNAS0971();
			}

			if (CommUtil.isNull(stopayin.getFrna01())) {
				throw DpModuleError.DpstComm.BNAS0093();
			}

			if (CommUtil.isNull(stopayin.getIdtp01())) {
				throw DpModuleError.DpstComm.BNAS0092();
			}

			if (CommUtil.isNull(stopayin.getIdno01())) {
				throw DpModuleError.DpstComm.BNAS1603();
			}

			// // 校验证件类型、证件号码
			// if (stopayin.getIdtp01() == E_IDTFTP.SFZ) {
			// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp =
			// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP.SFZ;
			// BusiTools.chkCertnoInfo(idtftp, stopayin.getIdno01());
			// }

			// 经办人2,外部止付和存款证明止付时必输；客户止付时不可录入
			if (stopayin.getStoptp() == (E_STOPTP.EXTSTOPAY)) {

				if (CommUtil.isNull(stopayin.getFrna02())) {
					throw DpModuleError.DpstComm.BNAS1232();
				}

				if (CommUtil.isNull(stopayin.getIdtp02())) {
					throw DpModuleError.DpstComm.BNAS1231();
				}

				if (CommUtil.isNull(stopayin.getIdno02())) {
					throw DpModuleError.DpstComm.BNAS1604();
				}

				// 校验证件类型、证件号码
				// if (stopayin.getIdtp02() == E_IDTFTP.SFZ) {
				// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp =
				// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP.SFZ;
				// BusiTools.chkCertnoInfo(idtftp, stopayin.getIdno02());
				// }

			}

			if (stopayin.getStoptp() != E_STOPTP.EXTSTOPAY && stopayin.getStopms() == E_STOPMS.emergc) {
				throw DpModuleError.DpstComm.BNAS0107();
			}

			if (CommUtil.isNull(stopayin.getSfreas())) {
				throw DpModuleError.DpstComm.BNAS0590();
			}
		}

		// 客户止付时字段必输校验
		if (E_STOPTP.CUSTSTOPAY == stopayin.getStoptp()) {

			if (CommUtil.isNull(stopayin.getCustna())) {
				throw DpModuleError.DpstComm.BNAS0672();
			}

			if (CommUtil.isNull(stopayin.getIdtp01())) {
				throw DpModuleError.DpstComm.BNAS0150();
			}

			if (CommUtil.isNull(stopayin.getIdno01())) {
				throw DpModuleError.DpstComm.BNAS0155();
			}

			if (CommUtil.isNull(stopayin.getCureas())) {
				throw DpModuleError.DpstComm.BNAS0184();
			}

			if (CommUtil.isNull(stopayin.getCrcycd())) {
				throw DpModuleError.DpstComm.BNAS1101();
			}

			// 账户保护原因转换 和银行止付，外部止付共用表中同一字段
			stopayin.setSfreas(stopayin.getCureas().getValue());

			// 校验证件类型、证件号码
			// if (stopayin.getIdtp01() == E_IDTFTP.SFZ) {
			// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp =
			// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP.SFZ;
			// BusiTools.chkCertnoInfo(idtftp, stopayin.getIdno01());
			// }

			String servtp = CommTools.getBaseRunEnvs().getChannel_id();
			bizlog.debug("渠道号为：" + "++++++++++++" + servtp + "-----------------");

			bizlog.debug("法人代码为：" + "------------" + CommTools.getBaseRunEnvs().getBusi_org_id() + "-----------------");
			// 判断机构是否为省中心机构
			/*
			 * if (CommUtil.equals(CommTools.getBaseRunEnvs().getBusi_org_id(), "999")) { if
			 * ("EB" == servtp) { throw DpModuleError.DpstComm.BNAS0358(); } }
			 */

		}

		// if (stopayin.getStoptp().equals(E_STOPTP.CUSTSTOPAY)) {
		//
		// if (CommUtil.isNotNull(stopayin.getFrna02())) {
		// throw DpModuleError.DpstComm.E9999("客户止付时,止付经办人2姓名不可录入");
		// }
		//
		// if (CommUtil.isNotNull(stopayin.getIdtp02())) {
		// throw DpModuleError.DpstComm.E9999("客户止付时,止付经办人2证件种类不可录入");
		// }
		//
		// if (CommUtil.isNotNull(stopayin.getIdno02())) {
		// throw DpModuleError.DpstComm.E9999("客户止付时,止付经办人2证件号码不可录入");
		// }
		// }
	}

	/**
	 * 止付
	 * 
	 * @param stopayin
	 * @throws ParseException
	 * @throws NumberFormatException
	 */
	public static void stopPayment(IoDpStopayIn stopayin, DpFrozEntity entity) {
		IoDpKnaAcct acctInfo = SysUtil.getInstance(IoDpKnaAcct.class);
		// 查询子账户信息
		if (JFBaseEnumType.E_STACTP.STSA == stopayin.getStactp()) {

			acctInfo = DpFrozDao.selAcctInfoByAcalno(stopayin.getAcctno(), false);
			if (acctInfo != null) {
				stopayin.setAcctno(acctInfo.getAcctno());
			} else {
				throw DpModuleError.DpAcct.AT020035();
			}
		}
		// 冻结主体类型设置为智能储蓄
		stopayin.setFrozow(E_FROZOW.AUACCT);

		// 设置冻结分类码为DEFAULT
		stopayin.setFrozcd("DEFAULT");

		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();

		// 冻结序号。只有所有子户冻结此序号会变，其他都为1
		long frozsq = 1l;

		// 获取冻结定义
		// KnpFroz tblKnpFroz = KnpFrozDao.selectOne_odb1(stopayin.getFrozcd(),
		// false);
		KnpFroz tblKnpFroz = DpFrozDao.selFrozInfoByFrozcd(stopayin.getFrozcd(),
				CommTools.getBaseRunEnvs().getBusi_org_id(), false);

		if (CommUtil.isNull(tblKnpFroz))

			throw DpModuleError.DpstComm.BNAS0831(stopayin.getFrozcd());
		String custac = "";
		IoCustacInfo custacInfo = DpFrozDao.selCustacInfoByCardno(stopayin.getCardno(), true);
		if (JFBaseEnumType.E_STACTP.STMA == stopayin.getStactp()) {
			custac = custacInfo.getCustac();
		} else if (JFBaseEnumType.E_STACTP.STSA == stopayin.getStactp()) {
			custac = stopayin.getAcctno();
		}
		// 查询电子账号的所有未解冻的冻结，止付等信息
		List<KnbFroz> listKnbFroz = DpFrozDao.selAllFrozInfoByCustac(custac, E_FROZST.VALID, false);
		List<KnbFroz> KnbFroz2 = null;
		if (JFBaseEnumType.E_STACTP.STSA == stopayin.getStactp()) {
			KnbFroz2 = DpFrozDao.selAllFrozInfoByCustac(custac, E_FROZST.VALID, false);
			if (JFBaseEnumType.E_STACTP.STSA == stopayin.getStactp() && KnbFroz2.size() > 0) {
				for (KnbFroz ent : KnbFroz2) {
					if (E_FRLMTP.OUT == ent.getFrlmtp()) {
						throw DpModuleError.DpAcct.AT020033();
					}
				}
			}
		}
		for (KnbFroz knbFroz : listKnbFroz) {
			// 电子账号在借冻，双冻状态下，不允许银行止付，允许外部止付和客户止付。
			bizlog.debug("++++" + "电子账户状态为" + knbFroz.getFroztp() + "+++++++++++++++++++++++++" + knbFroz.getFrlmtp());
			if ((knbFroz.getFroztp() == E_FROZTP.JUDICIAL || knbFroz.getFroztp() == E_FROZTP.ADD) // 冻结，续冻
					&& (knbFroz.getFrlmtp() == E_FRLMTP.ALL || knbFroz.getFrlmtp() == E_FRLMTP.OUT) // 借冻，双冻
					&& stopayin.getStoptp() == E_STOPTP.BANKSTOPAY) { // 银行止付

				throw DpModuleError.DpstComm.BNAS0925();
			}

			/*
			 * 4JF
			 */
			// 银行止付（全止）状态下，不允许银行止付（全止，部止）
			if (knbFroz.getFroztp() == E_FROZTP.BANKSTOPAY // 银行止付
					&& knbFroz.getFrlmtp() == E_FRLMTP.OUT // 全止（只进不出）
					&& stopayin.getStoptp() == E_STOPTP.BANKSTOPAY) { // 银行止付

				throw DpModuleError.DpstComm.BNAS0913();
			}

			// delete by songkl 需求变更 银行部止状态下允许做银行全止 -begin
			/*
			 * // 银行止付（部止）状态下，不允许银行止付（全止） if (knbFroz.getFroztp() == E_FROZTP.BANKSTOPAY
			 * //银行止付 && knbFroz.getFrlmtp() == E_FRLMTP.AMOUNT //部止（指定金额） &&
			 * stopayin.getStoptp() == E_STOPTP.BANKSTOPAY //银行止付 && stopayin.getFrlmtp() ==
			 * E_FRLMTP.OUT) { //全止（只进不出）
			 * 
			 * throw DpModuleError.DpstComm.E9999("电子账号银行止付（部止）状态下，不允许银行止付（全止）"); }
			 */
			// delete by songkl 需求变更 银行部止状态下允许做银行全止 -end

			// 外部止付状态下，不允许银行止付（全止，部止），外部止付
			if (knbFroz.getFroztp() == E_FROZTP.EXTSTOPAY // 外部止付
					&& (stopayin.getStoptp() == E_STOPTP.BANKSTOPAY)) { // 银行止付

				throw DpModuleError.DpstComm.BNAS0917();
			}

			// 客户止付状态下，不允许客户止付
			if (knbFroz.getFroztp() == E_FROZTP.CUSTSTOPAY // 客户止付
					&& (stopayin.getStoptp() == E_STOPTP.CUSTSTOPAY)) { // 银行止付,外部止付

				throw DpModuleError.DpstComm.BNAS0907();
			}
		}

		// 判断是否为客户止付
		if (stopayin.getStoptp() == E_STOPTP.CUSTSTOPAY) {
			// 根据电子账号查询电子账号ID
			/*
			 * IoCustacInfo custacInfo = DpFrozDao.selCustacInfoByCardno(
			 * stopayin.getCardno(), true);
			 */
			bizlog.debug("++++++++++++" + custacInfo.getCorpno() + "+++++++++++++");

			// String corpno = custacInfo.getCorpno();
			// 暂使用开户行法人处理业务逻辑
			// CommTools.getBaseRunEnvs().setBusi_org_id(corpno);

			if (CommUtil.isNull(custacInfo.getCustac())) {
				throw DpModuleError.DpstComm.BNAS0928();
			}

			// 电子账号ID
			custac = custacInfo.getCustac();

			// 根据电子账号查询电子账号信息
//			IoCustacDetl custacDl = DpFrozDao.selCustacDetl(custac, true);
//			if (JFBaseEnumType.E_STACTP.STMA == stopayin.getStactp()) {
//				if (!stopayin.getCustna().equals(custacDl.getCustna())) {
//					throw DpModuleError.DpstComm.BNAS0525();
//				}
//			}
//             if (JFBaseEnumType.E_STACTP.STSA == stopayin.getStactp()) {
//             if (acctInfo != null) {
//             if (!stopayin.getCustna().equals(acctInfo.getAcctna())) {
//             throw DpModuleError.DpstComm.BNAS0525();
//             }
//             } else {
//             throw DpModuleError.DpAcct.AT020035();
//             }
//            
//             }
			// 账户状态为预开户、转久悬、预销户、销户的，交易拒绝，报错：“电子账户状态为***状态，无法操作！”。
			E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
			bizlog.debug("+++++++++++++++++++++++++++++" + "电子账户状态为" + cuacst + "+++++++++++++++++++++++++");
			if (cuacst == E_CUACST.CLOSED) {
				throw DpModuleError.DpstComm.BNAS1597();

			} else if (cuacst == E_CUACST.PRECLOS) {
				throw DpModuleError.DpstComm.BNAS0846();

			} else if (cuacst == E_CUACST.PREOPEN) {
				throw DpModuleError.DpstComm.BNAS1598();

			} else if (cuacst == E_CUACST.OUTAGE) {

				throw DpModuleError.DpstComm.BNAS0850();
			} else if (cuacst == E_CUACST.NOENABLE) {

				throw DpModuleError.DpstComm.BNAS0848();
			}
			if (JFBaseEnumType.E_STACTP.STSA == stopayin.getStactp()) {
				E_DPACST cuacst1 = acctInfo.getAcctst();
				if (acctInfo != null) {
					if (cuacst1 == E_DPACST.CLOSE) {
						throw DpModuleError.DpstComm.BNAS1597();

					}
				}

			}

			// 根据客户号查询客户信息
			// IoCustInfo custInfo = DpFrozDao.selCustInfo(custacDl.getCustno(),
			// true);

			// 客户止付时，经办人1的姓名和证件号码必须为客户本人
			// if(stopayin.getStoptp() == E_STOPTP.CUSTSTOPAY ){
			//
			// if(!stopayin.getFrna01().equals(custacDl.getCustna())){
			// throw DpModuleError.DpstComm.E9999("客户姓名不一致,非客户本人办理");
			// }
			//
			// if(!stopayin.getIdno01().equals(custInfo.getIdtfno())){
			// throw DpModuleError.DpstComm.E9999("证件号码不一致,非客户本人办理");
			// }
			// }

			// 生成止付编号
			KnpParameter tblknp_para = KnpParameterDao.selectOne_odb1("IS_CHECK", "SPECTP", "%", "%", false);
			if (CommUtil.isNull(tblknp_para)) {
				throw DpModuleError.DpstComm.BNAS1210();
			}

			String frozno = null;

			if (CommUtil.equals("Y", tblknp_para.getParm_value1())) {
				String specno = "22" + CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4);

				frozno = getMaxSpecno(E_SPECTP.CUSTOP, specno);

			} else if (CommUtil.equals("N", tblknp_para.getParm_value1())) {
				// 生成冻结编号
				frozno = "22" + CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4)
						+ genFrozno(tblKnpFroz.getFroztp(), CommTools.getBaseRunEnvs().getTrxn_date(),
								CommTools.getBaseRunEnvs().getTrxn_branch());
			}

			KnbFroz tblKnbFroz = SysUtil.getInstance(KnbFroz.class);

			// 银行止付或者客户止付时，止付终止日期默认设置为20990101
			// if(stopayin.getStoptp() == E_STOPTP.BANKSTOPAY ||
			// stopayin.getStoptp() == E_STOPTP.CUSTSTOPAY){
			// stopayin.setStpldt("20990101");
			// }

			// 止付时间
			// long froztm = DateTools.getCurrentTime();

			// 限制类型
			// E_FRLMTP frlmtp = stopayin.getFrlmtp();

			// 紧急止付
			// if(stopayin.getStopms() == E_STOPMS.emergc){
			// froztm = getTopHour(); //获取整点信息
			// frlmtp = E_FRLMTP.OUT;
			// }

			// 基本止付信息
			tblKnbFroz.setFrozsq(frozsq);// 冻结序号
			tblKnbFroz.setFroztp(CommUtil.toEnum(E_FROZTP.class, stopayin.getStoptp().getValue())); // 冻结业务类型
			tblKnbFroz.setFrozcd(stopayin.getFrozcd()); // 冻结分类码
			tblKnbFroz.setFrozow(stopayin.getFrozow()); // 冻结主体类型
			tblKnbFroz.setFrlmtp(E_FRLMTP.OUT); // 限制类型
			tblKnbFroz.setFrozlv(tblKnpFroz.getFrozlv()); // 冻结级别
			tblKnbFroz.setFrozno(frozno);// 止付编号
			if (JFBaseEnumType.E_STACTP.STSA == stopayin.getStactp()) {
				custac = stopayin.getAcctno();
			}
			tblKnbFroz.setCustac(custac); // 客户账号
			tblKnbFroz.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());// 法人代码
			tblKnbFroz.setFrozdt(trandt); // 止付日期
			tblKnbFroz.setFroztm(CommTools.getBaseRunEnvs().getComputer_time()); // 止付时间
			tblKnbFroz.setFrbgdt(trandt); // 止付起始日期
			tblKnbFroz.setFreddt(stopayin.getStpldt()); // 止付终止日期
			tblKnbFroz.setFrreas(stopayin.getSfreas()); // 止付原因
			tblKnbFroz.setFrozst(E_FROZST.VALID); // 解止标志
			tblKnbFroz.setCrcycd(stopayin.getCrcycd());// 币种
			tblKnbFroz.setCsextg(stopayin.getCsextg());// 账户钞汇标志
			tblKnbFroz.setStopms(stopayin.getStopms());// 止付措施
			tblKnbFroz.setSttmct(stopayin.getSttmct());// 止付时间计算
			tblKnbFroz.setSttmle(stopayin.getSttmle());// 止付时长

			// 公共变量
			tblKnbFroz.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
			tblKnbFroz.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq()); // 交易流水
			tblKnbFroz.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller()); // 交易柜员
			tblKnbFroz.setCkuser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 授权柜员
			// tblKnbFroz.setCptrcd(CommTools.getBaseRunEnvs().getCptrcd()); // 对账代码
			tblKnbFroz.setLttscd(BusiTools.getBusiRunEnvs().getTrxn_code()); // 内部交易码

			// 止付内容
			tblKnbFroz.setFrctno(stopayin.getStbkno()); // 止付通知书编号
			tblKnbFroz.setFrcttp(stopayin.getFrcttp()); // 止付文书种类
			tblKnbFroz.setFrexog(stopayin.getStdptp()); // 止付部门类型
			tblKnbFroz.setFrogna(stopayin.getStladp()); // 止付部门名称
			tblKnbFroz.setIdtp01(stopayin.getIdtp01()); // 止付经办人1证件类型
			tblKnbFroz.setIdno01(stopayin.getIdno01()); // 止付经办人1证件号码
			tblKnbFroz.setFrna01(stopayin.getFrna01()); // 止付经办人1姓名
			tblKnbFroz.setIdtp02(stopayin.getIdtp02()); // 止付经办人2证件类型
			tblKnbFroz.setIdno02(stopayin.getIdno02()); // 止付经办人2证件号码
			tblKnbFroz.setFrna02(stopayin.getFrna02()); // 止付经办人2姓名
			tblKnbFroz.setPruser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 审批柜员
			tblKnbFroz.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
			tblKnbFroz.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
			// tblKnbFroz.setFrspfg(stopayin.getFrspfg()); // 轮候冻结标志
			tblKnbFroz.setStactp(stopayin.getStactp());
			if (CommUtil.isNotNull(stopayin.getIdno01())) {
				tblKnbFroz.setTmidno01(DecryptConstant.maskIdCard(stopayin.getIdno01()));
			}
			if (CommUtil.isNotNull(stopayin.getIdno02())) {
				tblKnbFroz.setTmidno02(DecryptConstant.maskIdCard(stopayin.getIdno02()));
			}
			if (CommUtil.isNotNull(stopayin.getFrna01())) {
				tblKnbFroz.setTmfrna01(DecryptConstant.maskName(stopayin.getFrna01()));
			}
			if (CommUtil.isNotNull(stopayin.getFrna02())) {
				tblKnbFroz.setTmfrna02(DecryptConstant.maskName(stopayin.getFrna02()));
			}
			// 登记冻结登记簿
			KnbFrozDao.insert(tblKnbFroz);

			KnbFrozDetl tblKnbfrozdetl = SysUtil.getInstance(KnbFrozDetl.class);

			// 冻结模式按冻结对象的类型来划分，不要将所有子户冻结也放在此处
			switch (stopayin.getFrozow()) {
			// case ACCTNO:
			// kna_accs tblKna_accs =
			// Kna_accsDao.selectOne_odb1(stopayin.getCustac(),
			// stopayin.getSubsac(), true);

			// TODO 单户冻结检查
			// 币种，钞汇标志检查
			// accsCheck(stopayin, tblKna_accs);
			//
			// tblKnbfrozdetl.setFrowid(tblKna_accs.getAcctno());
			// tblKnbfrozdetl.setFrozam(stopayin.getFrozam());
			// tblKnbfrozdetl.setFrozbl(stopayin.getFrozam());
			// tblKnbfrozdetl.setFrozsq(frozsq);
			// oneAcctStopPay(tblKnbfrozdetl, tblKnbFroz);
			// break;
			/*
			 * case CUSTAC: // TODO 客户账号冻结检查(客户账号冻结暂不实现) tblKnbfrozdetl.setFrowid(custac);
			 * tblKnbfrozdetl.setFrozam(stopayin.getStopam());
			 * tblKnbfrozdetl.setFrozbl(stopayin.getStopam());
			 * tblKnbfrozdetl.setFrozsq(frozsq); custacStopPay(tblKnbfrozdetl, tblKnbFroz);
			 * break;
			 */
			case AUACCT:
				if (JFBaseEnumType.E_STACTP.STSA == stopayin.getStactp()) {
					custac = stopayin.getAcctno();
				}
				tblKnbfrozdetl.setFrowid(custac);
				tblKnbfrozdetl.setFrozam(stopayin.getStopam());
				tblKnbfrozdetl.setFrozbl(stopayin.getStopam());
				tblKnbfrozdetl.setFrozsq(frozsq);
				auacctStopPay(tblKnbfrozdetl, tblKnbFroz);
				break;
			default:
				throw DpModuleError.DpstComm.BNAS1145(stopayin.getFrozow().getLongName());
			}

			// 需要返回的实体进行赋值
			entity.setFrozno(frozno);

			// 外部止付和银行止付
		} else {

			IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);

			IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(stopayin.getCardno(), false);

			if (CommUtil.isNull(caKnaAcdc)) {
				throw DpModuleError.DpstComm.BNAS0753();
			}

			if (CommUtil.isNull(caKnaAcdc.getCustac())) {
				throw DpModuleError.DpstComm.BNAS0692();
			}

			// 查询电子账户状态
			E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(caKnaAcdc.getCustac());
			bizlog.debug("+++++++++++++++++++++++++++++" + "电子账户状态为" + cuacst + "+++++++++++++++++++++++++");

			if (cuacst == E_CUACST.CLOSED) {
				throw DpModuleError.DpstComm.BNAS1597();

			} else if (cuacst == E_CUACST.PREOPEN) {
				throw DpModuleError.DpstComm.BNAS1598();

			}
			// else if (cuacst == E_CUACST.PRECLOS) {
			// throw DpModuleError.DpstComm.E9999("电子账户状态为预销户状态，无法操作！");
			//
			// }

			// if(caKnaAcdc.getStatus() != E_DPACST.NORMAL ){
			// throw DpModuleError.DpstComm.E9999("根据电子账号获取电子账号ID的状态不正常");
			// }

			// 电子账号ID
			custac = caKnaAcdc.getCustac();

			// 获取客户账号信息
			IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			IoCaKnaCust tblKnaCust = caqry.getKnaCustByCustacOdb1(custac, true);
			// 注意客户姓名
			// 银行止付-检查是否涉案
			if (stopayin.getStoptp() == E_STOPTP.BANKSTOPAY) {
				// 电子账户是否涉案
				IoCaSevQryAccout.IoCaQryInacInfo.Output output = SysUtil
						.getInstance(IoCaSevQryAccout.IoCaQryInacInfo.Output.class);
				SysUtil.getInstance(IoCaSevQryAccout.class).qryInac(stopayin.getCardno(), stopayin.getCustna(), null,
						null, output);

				final String otcard_in = stopayin.getCardno();// 转出账号
				final String otacna_in = stopayin.getCustna();// 转出账户名称
				final String otbrch_in = CommTools.getBaseRunEnvs().getTrxn_branch();// 转出机构
				final String incard_in = null;// 转入账号
				final String inacna_in = null;// 转入账号名称
				final String inbrch_in = CommTools.getBaseRunEnvs().getTrxn_branch();// 转入机构
				final BigDecimal tranam_in = stopayin.getStopam();// 交易金额
				final String crcycd = stopayin.getCrcycd();// 币种

				if (E_INSPFG.INVO == output.getInspfg()) {
					// 独立事务
					DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
						@Override
						public Void execute() {
							// 获取涉案账户交易信息登记输入信息
							IoCaKnbTrinInput cplKnbTrin = SysUtil.getInstance(IoCaKnbTrinInput.class);

							cplKnbTrin.setOtcard(otcard_in);// 转出账号
							cplKnbTrin.setOtacna(otacna_in);// 转出账号名称
							cplKnbTrin.setOtbrch(otbrch_in);// 转出账户机构
							cplKnbTrin.setIncard(incard_in);// 转入账号
							cplKnbTrin.setInacna(inacna_in);// 转入账户名称
							cplKnbTrin.setInbrch(inbrch_in);// 转入账户机构
							cplKnbTrin.setTranam(tranam_in);// 交易金额
							cplKnbTrin.setCrcycd(crcycd);// 币种
							cplKnbTrin.setIssucc(E_YES___.NO);// 交易是否成功

							// 涉案账户交易信息登记
							SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(cplKnbTrin);

							return null;
						}
					});
				}
			}

			// 银行止付和外部止付，止付业务同一核算主体的经办柜员能止付该核算主体下的电子账户存款，即检查是否同一法人代码
			if ((stopayin.getStoptp() == E_STOPTP.EXTSTOPAY || stopayin.getStoptp() == E_STOPTP.BANKSTOPAY)
					&& !CommUtil.equals(tblKnaCust.getCorpno(), CommTools.getBaseRunEnvs().getBusi_org_id())) {

				throw DpModuleError.DpstComm.BNAS0784();

			}

			// 客户账户状态检查
			// if (tblKnaCust.getAcctst() != E_ACCTST.NORMAL) {
			// throw DpModuleError.DpstComm.E9999("客户账号[" + tblKnaCust.getCustac()+
			// "]不正常，不能做止付业务");
			// }
//			if (JFBaseEnumType.E_STACTP.STMA == stopayin.getStactp()) {
//				if (!stopayin.getCustna().equals(tblKnaCust.getCustna())) {
//					throw DpModuleError.DpstComm.BNAS0525();
//				}
//			} else if (JFBaseEnumType.E_STACTP.STSA == stopayin.getStactp()) {
//				if (acctInfo != null) {
//					if (!stopayin.getCustna().equals(acctInfo.getAcctna())) {
//						throw DpModuleError.DpstComm.BNAS0525();
//					}
//				} else {
//					throw DpModuleError.DpAcct.AT020035();
//				}
//			}

			// 查询电子账号的所有未解冻的冻结，止付等信息
			// List<KnbFroz> listKnbFroz = KnbFrozDao.selectAll_odb3(custac,
			// E_FROZST.VALID, false);
			//
			// for (KnbFroz knbFroz : listKnbFroz) {
			//
			// // 电子账号在借冻，双冻状态下，不允许银行止付，允许外部止付和客户止付。
			// // if ((knbFroz.getFroztp() == E_FROZTP.JUDICIAL ||
			// knbFroz.getFroztp() == E_FROZTP.ADD) //冻结，续冻
			// // && (knbFroz.getFrlmtp() == E_FRLMTP.ALL || knbFroz.getFrlmtp()
			// == E_FRLMTP.OUT) //借冻，双冻
			// // && stopayin.getStoptp() == E_STOPTP.BANKSTOPAY) { //银行止付
			// //
			// // throw DpModuleError.DpstComm.E9999("电子账号存在借冻或双冻，不允许银行止付");
			// // }
			//
			// // 银行止付（全止）状态下，不允许银行止付（全止，部止）
			// // if (knbFroz.getFroztp() == E_FROZTP.BANKSTOPAY //银行止付
			// // && knbFroz.getFrlmtp() == E_FRLMTP.OUT //全止（只进不出）
			// // && stopayin.getStoptp() == E_STOPTP.BANKSTOPAY) { //银行止付
			// //
			// // throw
			// DpModuleError.DpstComm.E9999("电子账号银行止付（全止）状态下，不允许银行止付（全止，部止）");
			// // }
			//
			// //delete by songkl 需求变更 银行部止状态下允许做银行全止 -begin
			// // 银行止付（部止）状态下，不允许银行止付（全止）
			// /*if (knbFroz.getFroztp() == E_FROZTP.BANKSTOPAY //银行止付
			// && knbFroz.getFrlmtp() == E_FRLMTP.AMOUNT //部止（指定金额）
			// && stopayin.getStoptp() == E_STOPTP.BANKSTOPAY //银行止付
			// && stopayin.getFrlmtp() == E_FRLMTP.OUT) { //全止（只进不出）
			//
			// throw DpModuleError.DpstComm.E9999("电子账号银行止付（部止）状态下，不允许银行止付（全止）");
			// }*/
			// //delete by songkl 需求变更 银行部止状态下允许做银行全止 -end
			//
			// // 外部止付状态下，不允许银行止付（全止，部止），外部止付
			// // if (knbFroz.getFroztp() == E_FROZTP.EXTSTOPAY //外部止付
			// // && (stopayin.getStoptp() == E_STOPTP.BANKSTOPAY )) { //银行止付
			// //
			// // throw DpModuleError.DpstComm.E9999("电子账号外部止付状态下，不允许银行止付（全止，部止）");
			// // }
			//
			// // 客户止付状态下，不允许客户止付
			// // if (knbFroz.getFroztp() == E_FROZTP.CUSTSTOPAY //客户止付
			// // && (stopayin.getStoptp() == E_STOPTP.CUSTSTOPAY)) {
			// //银行止付,外部止付
			// //
			// // throw DpModuleError.DpstComm.E9999("电子账号客户止付状态下，不允许客户止付");
			// // }
			// }

			// 生成止付编号
			KnpParameter tblknp_para = KnpParameterDao.selectOne_odb1("IS_CHECK", "SPECTP", "%", "%", false);
			if (CommUtil.isNull(tblknp_para)) {
				throw DpModuleError.DpstComm.BNAS1210();
			}

			String frozno = null;

			if (CommUtil.equals("Y", tblknp_para.getParm_value1())) {
				// 生成止付编号
				String specno = "22" + CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4);

				frozno = getMaxSpecno(E_SPECTP.STOPPY, specno);

			} else if (CommUtil.equals("N", tblknp_para.getParm_value1())) {
				// 生成冻结编号
				frozno = "22" + CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4)
						+ genFrozno(tblKnpFroz.getFroztp(), CommTools.getBaseRunEnvs().getTrxn_date(),
								CommTools.getBaseRunEnvs().getTrxn_branch());

			}

			KnbFroz tblKnbFroz = SysUtil.getInstance(KnbFroz.class);

			// 外部止付止付时间计算为2-按小时；需要登记到期日，为紧急止付到期自动解止处理
			if (stopayin.getStoptp() == E_STOPTP.EXTSTOPAY) {
				// 止付时间计算：按小时
				if (E_STTMCT.HOUR == stopayin.getSttmct()) {
					String fredtm = CommTools.getBaseRunEnvs().getComputer_time();
					String freddt;
					try {
						freddt = getFreddt(Integer.parseInt(stopayin.getSttmle()), trandt + fredtm);
						stopayin.setStpldt(freddt.substring(0, 8));
						tblKnbFroz.setFredtm(freddt.substring(8));
					} catch (Exception e) {

					}
				}
			}

			// 限制类型
			E_FRLMTP frlmtp = stopayin.getFrlmtp();

			// 紧急止付
			if (stopayin.getStopms() == E_STOPMS.emergc) {
				// froztm = getTopHour(); //获取整点信息
				frlmtp = E_FRLMTP.OUT;
			}

			// 基本止付信息
			tblKnbFroz.setFrozsq(frozsq);// 冻结序号
			tblKnbFroz.setFroztp(CommUtil.toEnum(E_FROZTP.class, stopayin.getStoptp().getValue())); // 冻结业务类型
			tblKnbFroz.setFrozcd(stopayin.getFrozcd()); // 冻结分类码
			tblKnbFroz.setFrozow(stopayin.getFrozow()); // 冻结主体类型
			tblKnbFroz.setFrlmtp(frlmtp); // 限制类型
			tblKnbFroz.setFrozlv(tblKnpFroz.getFrozlv()); // 冻结级别
			tblKnbFroz.setFrozno(frozno);// 止付编号
			if (JFBaseEnumType.E_STACTP.STMA == stopayin.getStactp()) {
				custac = custacInfo.getCustac();
			} else if (JFBaseEnumType.E_STACTP.STSA == stopayin.getStactp()) {
				custac = stopayin.getAcctno();
			}
			tblKnbFroz.setCustac(custac); // 客户账号
			tblKnbFroz.setCorpno(tblKnaCust.getCorpno());// 法人代码
			tblKnbFroz.setFrozdt(trandt); // 止付日期
			tblKnbFroz.setFroztm(CommTools.getBaseRunEnvs().getComputer_time()); // 止付时间
			tblKnbFroz.setFrbgdt(trandt); // 止付起始日期
			tblKnbFroz.setFreddt(stopayin.getStpldt()); // 止付终止日期
			tblKnbFroz.setFrreas(stopayin.getSfreas()); // 止付原因
			tblKnbFroz.setFrozst(E_FROZST.VALID); // 解止标志
			tblKnbFroz.setCrcycd(stopayin.getCrcycd());// 币种
			tblKnbFroz.setCsextg(stopayin.getCsextg());// 账户钞汇标志
			tblKnbFroz.setStopms(stopayin.getStopms());// 止付措施
			tblKnbFroz.setSttmct(stopayin.getSttmct());// 止付时间计算
			tblKnbFroz.setSttmle(stopayin.getSttmle());// 止付时长

			// 公共变量
			tblKnbFroz.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
			tblKnbFroz.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq()); // 交易流水
			tblKnbFroz.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller()); // 交易柜员
			tblKnbFroz.setCkuser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 授权柜员
			// tblKnbFroz.setCptrcd(CommTools.getBaseRunEnvs().getCptrcd()); // 对账代码
			tblKnbFroz.setLttscd(BusiTools.getBusiRunEnvs().getTrxn_code()); // 内部交易码

			// 止付内容
			tblKnbFroz.setFrctno(stopayin.getStbkno()); // 止付通知书编号
			tblKnbFroz.setFrcttp(stopayin.getFrcttp()); // 止付文书种类
			tblKnbFroz.setFrexog(stopayin.getStdptp()); // 止付部门类型
			tblKnbFroz.setFrogna(stopayin.getStladp()); // 止付部门名称
			tblKnbFroz.setIdtp01(stopayin.getIdtp01()); // 止付经办人1证件类型
			tblKnbFroz.setIdno01(stopayin.getIdno01()); // 止付经办人1证件号码
			tblKnbFroz.setFrna01(stopayin.getFrna01()); // 止付经办人1姓名
			tblKnbFroz.setIdtp02(stopayin.getIdtp02()); // 止付经办人2证件类型
			tblKnbFroz.setIdno02(stopayin.getIdno02()); // 止付经办人2证件号码
			tblKnbFroz.setFrna02(stopayin.getFrna02()); // 止付经办人2姓名
			tblKnbFroz.setPruser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 审批柜员
			tblKnbFroz.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
			tblKnbFroz.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
			// tblKnbFroz.setFrspfg(stopayin.getFrspfg()); // 轮候冻结标志

			// 登记冻结登记簿
			tblKnbFroz.setStactp(stopayin.getStactp());// 冻结序号
			if (CommUtil.isNotNull(stopayin.getIdno01())) {
				tblKnbFroz.setTmidno01(DecryptConstant.maskIdCard(stopayin.getIdno01()));
			}
			if (CommUtil.isNotNull(stopayin.getIdno02())) {
				tblKnbFroz.setTmidno02(DecryptConstant.maskIdCard(stopayin.getIdno02()));
			}
			if (CommUtil.isNotNull(stopayin.getFrna01())) {
				tblKnbFroz.setTmfrna01(DecryptConstant.maskName(stopayin.getFrna01()));
			}
			if (CommUtil.isNotNull(stopayin.getFrna02())) {
				tblKnbFroz.setTmfrna02(DecryptConstant.maskName(stopayin.getFrna02()));
			}
			KnbFrozDao.insert(tblKnbFroz);

			KnbFrozDetl tblKnbfrozdetl = SysUtil.getInstance(KnbFrozDetl.class);

			// 冻结模式按冻结对象的类型来划分，不要将所有子户冻结也放在此处
			switch (stopayin.getFrozow()) {
			// case ACCTNO:
			// kna_accs tblKna_accs =
			// Kna_accsDao.selectOne_odb1(stopayin.getCustac(),
			// stopayin.getSubsac(), true);

			// TODO 单户冻结检查
			// 币种，钞汇标志检查
			// accsCheck(stopayin, tblKna_accs);
			//
			// tblKnbfrozdetl.setFrowid(tblKna_accs.getAcctno());
			// tblKnbfrozdetl.setFrozam(stopayin.getFrozam());
			// tblKnbfrozdetl.setFrozbl(stopayin.getFrozam());
			// tblKnbfrozdetl.setFrozsq(frozsq);
			// oneAcctStopPay(tblKnbfrozdetl, tblKnbFroz);
			// break;
			/*
			 * case CUSTAC: // TODO 客户账号冻结检查(客户账号冻结暂不实现) tblKnbfrozdetl.setFrowid(custac);
			 * tblKnbfrozdetl.setFrozam(stopayin.getStopam());
			 * tblKnbfrozdetl.setFrozbl(stopayin.getStopam());
			 * tblKnbfrozdetl.setFrozsq(frozsq); custacStopPay(tblKnbfrozdetl, tblKnbFroz);
			 * break;
			 */
			case AUACCT:
				if (JFBaseEnumType.E_STACTP.STSA == stopayin.getStactp()) {
					custac = stopayin.getAcctno();
				}
				tblKnbfrozdetl.setFrowid(custac);
				tblKnbfrozdetl.setFrozam(stopayin.getStopam());
				tblKnbfrozdetl.setFrozbl(stopayin.getStopam());
				tblKnbfrozdetl.setFrozsq(frozsq);
				auacctStopPay(tblKnbfrozdetl, tblKnbFroz);
				break;
			default:
				throw DpModuleError.DpstComm.BNAS1145(stopayin.getFrozow().getLongName());
			}

			// 需要返回的实体进行赋值
			entity.setFrozno(frozno);

		}
		
		if (JFBaseEnumType.E_STACTP.STMA == stopayin.getStactp()) {
			 stopayin.setAcctno(custac);
		}
	}

	/**
	 * 获取整点信息
	 * 
	 * @author douwenbo
	 * @date 2016-07-12 14:59
	 * 
	 * @return
	 */
	public static long getTopHour() {

		Date date = new Date();

		SimpleDateFormat sdf = new SimpleDateFormat("HH");

		String str = sdf.format(date) + "0000000";

		return Long.parseLong(str);
	}

	/**
	 * @author douwenbo
	 * @date 2016-04-22 14:02 续冻基本检查
	 * 
	 * @param cplioctfrozin
	 */
	public static void ctfrozByLawCheck(IoCtfrozPayIn cplioctfrozin) {

		if (CommUtil.isNull(cplioctfrozin.getFrozwy())) {
			throw DpModuleError.DpstComm.BNAS0816();
		}

		if (!cplioctfrozin.getFrozwy().equals(E_FROZWY.CONTIN)) {// 续冻
			throw DpModuleError.DpstComm.BNAS0817();
		}

		if (CommUtil.isNull(cplioctfrozin.getFrozno())) {
			throw DpModuleError.DpstComm.BNAS0220();
		}

		if (CommUtil.isNull(cplioctfrozin.getCardno())) {
			throw DpModuleError.DpstProd.BNAS0926();
		}
		if (JFBaseEnumType.E_STACTP.STSA == cplioctfrozin.getStactp()) {
			if (CommUtil.isNull(cplioctfrozin.getAcalno())) {
				throw DpModuleError.DpAcct.AT010034();
			}
		}

		if (CommUtil.isNull(cplioctfrozin.getCustna())) {
			throw DpModuleError.DpstComm.BNAS0524();
		}

		if (CommUtil.isNull(cplioctfrozin.getCrcycd())) {
			throw DpModuleError.DpstComm.BNAS0665();
		}

		if (CommUtil.isNull(cplioctfrozin.getFrogna())) {
			throw DpModuleError.DpstComm.BNAS0103();
		}

		if (CommUtil.isNull(cplioctfrozin.getFrexog())) {
			throw DpModuleError.DpstComm.BNAS0106();
		}

		if (CommUtil.isNull(cplioctfrozin.getFrctno())) {
			throw DpModuleError.DpstComm.BNAS0266();
		}
		if (CommUtil.isNull(cplioctfrozin.getFrna01())) {
			throw DpModuleError.DpstComm.BNAS0103();
		}

		if (CommUtil.isNull(cplioctfrozin.getIdno01())) {
			throw DpModuleError.DpstComm.BNAS0102();
		}

		if (CommUtil.isNull(cplioctfrozin.getIdtp01())) {
			throw DpModuleError.DpstComm.BNAS0101();
		}

		if (CommUtil.isNull(cplioctfrozin.getFrna02())) {
			throw DpModuleError.DpstComm.BNAS0099();
		}

		if (CommUtil.isNull(cplioctfrozin.getIdno02())) {
			throw DpModuleError.DpstComm.BNAS1599();
		}

		if (CommUtil.isNull(cplioctfrozin.getIdtp02())) {
			throw DpModuleError.DpstComm.BNAS0098();
		}

		// 校验1证件类型、证件号码
		// if (cplioctfrozin.getIdtp01() == E_IDTFTP.SFZ) {
		// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp =
		// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP.SFZ;
		// BusiTools.chkCertnoInfo(idtftp, cplioctfrozin.getIdno01());
		// }

		// // 校验2证件类型、证件号码

		// if (cplioctfrozin.getIdtp02() == E_IDTFTP.SFZ) {
		// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp =
		// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP.SFZ;
		// BusiTools.chkCertnoInfo(idtftp, cplioctfrozin.getIdno02());
		// }

		if (CommUtil.isNull(cplioctfrozin.getCtfrre())) {
			throw DpModuleError.DpstComm.BNAS0265();
		}

		if (CommUtil.isNull(cplioctfrozin.getFrozdt())) {
			throw DpModuleError.DpstComm.BNAS0267();
		}

		if (CommUtil.isNull(cplioctfrozin.getFreddt())) {
			throw DpModuleError.DpstComm.BNAS0264();
		}

		if (CommUtil.equals(cplioctfrozin.getCrcycd(), BusiTools.getDefineCurrency())
				&& CommUtil.isNotNull(cplioctfrozin.getCsextg())) {
			throw DpModuleError.DpstComm.BNAS1098();
		}

		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(cplioctfrozin.getCardno(), false);
		if (CommUtil.isNull(caKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0711();
		}
		// IoDpKnaAcct cplKnaAcct =
		// SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(caKnaAcdc.getCustac());
		// if (!CommUtil.equals(cplioctfrozin.getCrcycd(), cplKnaAcct.getCrcycd())) {
		// throw DpModuleError.DpstComm.E9999("输入币种与账户币种不一致");
		// }

	}

	/**
	 * @author douwenbo
	 * @date 2016-04-26 08:50
	 * @param cplioctfrozin
	 *            获取冻结登记簿中最大的冻结序号
	 * @return frozsq解冻序号
	 */
	public static long getMaxFrozsq(IoCtfrozPayIn cplioctfrozin) {
		long frozsq = 1;
		if (CommUtil.isNull(DpFrozDao.selFrozMaxseq(cplioctfrozin.getFrozno(), false))) {
			return frozsq;
		} else {
			return DpFrozDao.selFrozMaxseq(cplioctfrozin.getFrozno(), false);
		}
	}

	/**
	 * @author douwenbo
	 * @date 2016-05-05 10:11
	 * @param cplioctfrozin
	 *            获取冻结登记簿中在冻结状态下最小的冻结序号
	 * @return frozsq解冻序号
	 */
	public static long getMinFrozsq(IoCtfrozPayIn cplioctfrozin) {
		long frozsq = 1;
		if (CommUtil.isNull(DpFrozDao.selFrozMinseq(cplioctfrozin.getFrozno(), E_FROZST.VALID, false))) {
			return frozsq;
		} else {
			return DpFrozDao.selFrozMinseq(cplioctfrozin.getFrozno(), E_FROZST.VALID, false);
		}
	}

	/**
	 * @author douwenbo
	 * @date 2016-05-05 09:42
	 * @param cplioctfrozin
	 *            获取冻结登记簿中在冻结状态下的最大的冻结序号
	 * @return frozsq解冻序号
	 */
	public static long getMaxFrozstFrozsq(IoCtfrozPayIn cplioctfrozin) {
		long frozsq = 1;
		if (CommUtil.isNull(DpFrozDao.selFrozFrozstMaxseq(cplioctfrozin.getFrozno(), E_FROZST.VALID, false))) {
			return frozsq;
		} else {
			return DpFrozDao.selFrozFrozstMaxseq(cplioctfrozin.getFrozno(), E_FROZST.VALID, false);
		}
	}

	/**
	 * @author douwenbo
	 * @date 2016-04-26 08:53
	 * @param cplioctfrozin
	 *            获取冻结登记簿中最大的冻结序号,再加一，默认为一
	 * @return frozsq解冻序号
	 */
	public static long getFrozsqAddOne(IoCtfrozPayIn cplioctfrozin) {
		long frozsq = 1;
		if (CommUtil.isNull(DpFrozDao.selFrozMaxseq(cplioctfrozin.getFrozno(), false))) {
			return frozsq;
		} else {
			return DpFrozDao.selFrozMaxseq(cplioctfrozin.getFrozno(), false) + 1;
		}
	}

	/**
	 * @author douwenbo
	 * @date 2016-04-26 09:31
	 * @param cplioctfrozin
	 *            获取冻结明细登记簿中最大的冻结序号
	 * @return frozsq冻结序号
	 */
	public static long getDetlFrozsq(IoCtfrozPayIn cplioctfrozin) {
		long frozsq = 1;
		if (CommUtil.isNull(DpFrozDao.selFrozDetlMaxseq(cplioctfrozin.getFrozno(), E_FROZST.VALID, false))) {
			return frozsq;
		} else {
			return DpFrozDao.selFrozDetlMaxseq(cplioctfrozin.getFrozno(), E_FROZST.VALID, false);
		}
	}

	/**
	 * @author douwenbo
	 * @date 2016-04-26 09:36
	 * @param cplioctfrozin
	 *            获取冻结明细登记簿中最大的冻结序号,再加一，默认为一
	 * @return frozsq冻结序号
	 */
	public static long getDetlFrozsqAddOne(IoCtfrozPayIn cplioctfrozin) {
		long frozsq = 1;
		if (CommUtil.isNull(DpFrozDao.selFrozDetlMaxseq(cplioctfrozin.getFrozno(), E_FROZST.VALID, false))) {
			return frozsq;
		} else {
			return DpFrozDao.selFrozDetlMaxseq(cplioctfrozin.getFrozno(), E_FROZST.VALID, false) + 1;
		}
	}

	/**
	 * @author douwenbo
	 * @date 2016-04-22 14:23 续冻
	 * @param cplioctfrozin
	 * @param entity
	 */
	public static void prcCtfr(IoCtfrozPayIn cplioctfrozin, DpFrozEntity entity) {

		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);

		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(cplioctfrozin.getCardno(), false);

		if (CommUtil.isNull(caKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0692();
		}

		// if(caKnaAcdc.getStatus() != E_DPACST.NORMAL ){
		// throw DpModuleError.DpstComm.E9999("根据电子账号获取电子账号ID的状态不正常");
		// }

		// 电子账号ID
		String custac = caKnaAcdc.getCustac();

		// 获取客户账号信息
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaCust tblKna_cust = caqry.getKnaCustByCustacOdb1(custac, true);
		String custno = tblKna_cust.getCustno();
		// 客户号

		List<KnbFroz> lstKnbFroz2 = KnbFrozDao.selectAll_odb4(cplioctfrozin.getFrozno(), false);
		if (CommUtil.isNull(lstKnbFroz2)) {
			throw DpModuleError.DpstComm.BNAS0729();
		}

		List<KnbFroz> lstKnbFroz1 = KnbFrozDao.selectAll_odb5(cplioctfrozin.getFrozno(), E_FROZST.VALID, false);

		if (CommUtil.isNull(lstKnbFroz1)) {
			throw DpModuleError.DpstComm.BNAS0726();
		}

		IofrozInfo frozInfo = DpAcctQryDao.selFrozInfoByFrozno(cplioctfrozin.getFrozno(), false);
		if (CommUtil.isNull(frozInfo)) {
			throw DpModuleError.DpstComm.BNAS0361();
		}
		if (JFBaseEnumType.E_STACTP.STSA == cplioctfrozin.getStactp()) {
			custac = cplioctfrozin.getAcctno();
		}
		if (lstKnbFroz2.size() > 0) {
			if (!lstKnbFroz2.get(0).getCustac().equals(custac)) {
				throw DpModuleError.DpstComm.BNAS0821();
			}
		}
		// List<KnbFroz> lstKnbFroz12 =
		// KnbFrozDao.selectAll_odb12(custac,CommTools.getBaseRunEnvs().getTrxn_branch(),E_FROZST.VALID,
		// false);

		// if (lstKnbFroz12.size() > 1) {
		// throw DpModuleError.DpstComm.E9999("同一家机构同一个电子账号冻结状态记录多于一条，不能续冻");
		// }

		// 原冻结在冻结状态下的最大序号
		if (CommUtil.isNull(DpFrozDao.selFrozFrozstMaxseq(cplioctfrozin.getFrozno(), E_FROZST.VALID, false))) {
			throw DpModuleError.DpstComm.BNAS0737();
		}

		long maxfrozfrozstsq = getMaxFrozstFrozsq(cplioctfrozin);

		KnbFroz tblKnbFroz8 = KnbFrozDao.selectOne_odb8(cplioctfrozin.getFrozno(), maxfrozfrozstsq, true);

		// 续冻检查
		ctfrCheck(cplioctfrozin, tblKnbFroz8);

		KnbFroz tblKnbFroz = SysUtil.getInstance(KnbFroz.class);

		long frozsq = getFrozsqAddOne(cplioctfrozin);

		// 基本冻结信息
		tblKnbFroz.setFrozsq(frozsq);// 冻结序号
		tblKnbFroz.setFroztp(E_FROZTP.ADD); // 冻结业务类型
		tblKnbFroz.setFrozcd(tblKnbFroz8.getFrozcd()); // 冻结分类码
		tblKnbFroz.setFrozow(E_FROZOW.AUACCT); // 冻结主体类型设置为智能储蓄
		tblKnbFroz.setFrlmtp(tblKnbFroz8.getFrlmtp()); // 限制类型
		tblKnbFroz.setFrozlv(tblKnbFroz8.getFrozlv()); // 冻结级别
		tblKnbFroz.setFrozno(cplioctfrozin.getFrozno());// 冻结编号
		if (JFBaseEnumType.E_STACTP.STSA == cplioctfrozin.getStactp()) {
			custac = cplioctfrozin.getAcctno();
		}
		tblKnbFroz.setCustac(custac); // 客户账号
		tblKnbFroz.setFrozdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 冻结日期
		tblKnbFroz.setFroztm(CommTools.getBaseRunEnvs().getComputer_time()); // 冻结时间
		tblKnbFroz.setFrbgdt(tblKnbFroz8.getFreddt()); // 冻结起始日期
		tblKnbFroz.setFreddt(cplioctfrozin.getFreddt()); // 续冻终止日期
		tblKnbFroz.setFrreas(cplioctfrozin.getCtfrre()); // 续冻原因
		tblKnbFroz.setFrozst(E_FROZST.VALID); // 冻结状态
		tblKnbFroz.setStopms(null);// 止付措施
		tblKnbFroz.setRemark(cplioctfrozin.getRemark());// 备注

		tblKnbFroz.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
		tblKnbFroz.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq()); // 交易流水
		tblKnbFroz.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller()); // 交易柜员
		tblKnbFroz.setCkuser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 授权柜员
		// tblKnbFroz.setCptrcd(CommTools.getBaseRunEnvs().getCptrcd()); // 对账代码
		tblKnbFroz.setLttscd(BusiTools.getBusiRunEnvs().getTrxn_code()); // 内部交易码

		// 司法冻结内容
		tblKnbFroz.setFrctno(cplioctfrozin.getFrctno()); // 冻结通知书编号
		tblKnbFroz.setFrcttp(cplioctfrozin.getFrcttp()); // 冻结文书种类
		tblKnbFroz.setFrexog(cplioctfrozin.getFrexog()); // 执法部门类型
		tblKnbFroz.setFrogna(tblKnbFroz8.getFrogna()); // 执法部门名称
		tblKnbFroz.setIdtp01(cplioctfrozin.getIdtp01()); // 执法人员1证件种类
		tblKnbFroz.setIdno01(cplioctfrozin.getIdno01()); // 执法人员1证件号码
		tblKnbFroz.setFrna01(cplioctfrozin.getFrna01()); // 执法人员1名称
		tblKnbFroz.setIdtp02(cplioctfrozin.getIdtp02()); // 执法人员2证件种类
		tblKnbFroz.setIdno02(cplioctfrozin.getIdno02()); // 执法人员2证件号码
		tblKnbFroz.setFrna02(cplioctfrozin.getFrna02()); // 执法人员2名称
		tblKnbFroz.setPruser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 审批柜员
		tblKnbFroz.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
		tblKnbFroz.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
		tblKnbFroz.setCrcycd(tblKnbFroz8.getCrcycd()); // 币种
		tblKnbFroz.setCsextg(tblKnbFroz8.getCsextg()); // 账户钞汇标志

		// 登记冻结登记簿
		tblKnbFroz.setStactp(cplioctfrozin.getStactp());// 冻结序号
		if (CommUtil.isNotNull(cplioctfrozin.getIdno01())) {
			tblKnbFroz.setTmidno01(DecryptConstant.maskIdCard(cplioctfrozin.getIdno01()));
		}
		if (CommUtil.isNotNull(cplioctfrozin.getIdno02())) {
			tblKnbFroz.setTmidno02(DecryptConstant.maskIdCard(cplioctfrozin.getIdno02()));
		}
		if (CommUtil.isNotNull(cplioctfrozin.getFrna01())) {
			tblKnbFroz.setTmfrna01(DecryptConstant.maskName(cplioctfrozin.getFrna01()));
		}
		if (CommUtil.isNotNull(cplioctfrozin.getFrna02())) {
			tblKnbFroz.setTmfrna02(DecryptConstant.maskName(cplioctfrozin.getFrna02()));
		}
		KnbFrozDao.insert(tblKnbFroz);

		KnbFrozDetl tblKnbfrozdetl = SysUtil.getInstance(KnbFrozDetl.class);

		// 冻结模式按冻结对象的类型来划分，不要将所有子户冻结也放在此处
		switch (tblKnbFroz8.getFrozow()) {
		/*
		 * case ACCTNO:
		 * 
		 * break; case CUSTAC: // TODO 客户账号冻结检查(客户账号冻结暂不实现)
		 * tblKnbfrozdetl.setFrowid(custac);
		 * tblKnbfrozdetl.setFrozam(cplioctfrozin.getFrozam());
		 * tblKnbfrozdetl.setFrozbl(cplioctfrozin.getFrozam());
		 * tblKnbfrozdetl.setFrozsq(getFrozsqAddOne(cplioctfrozin));
		 * 
		 * auacctCtFroz(tblKnbfrozdetl, tblKnbFroz); break;
		 */
		case AUACCT:
			if (JFBaseEnumType.E_STACTP.STSA == cplioctfrozin.getStactp()) {
				custac = cplioctfrozin.getAcctno();
			}
			tblKnbfrozdetl.setFrowid(custac);
			tblKnbfrozdetl.setFrozam(cplioctfrozin.getFrozam());
			tblKnbfrozdetl.setFrozbl(cplioctfrozin.getFrozam());
			tblKnbfrozdetl.setFrozsq(frozsq);

			auacctCtFroz(tblKnbfrozdetl, tblKnbFroz);
			break;
		default:
			throw DpModuleError.DpstComm.BNAS1145(tblKnbFroz8.getFrozow().getLongName());
		}

		// 需要返回的实体进行赋值
		entity.setFrozno(cplioctfrozin.getFrozno());
		entity.setCustno(custno);
	    cplioctfrozin.setAcctno(custac);
		
	}

	/**
	 * 存款证明输入检查
	 * 
	 * @param IoDpprovIn
	 */
	public static void dpprovCheck(IoDpprovIn opdpprin) {

		if (CommUtil.isNull(opdpprin.getDeprtp())) {
			throw DpModuleError.DpstComm.BNAS1028();
		}

		if (CommUtil.isNull(opdpprin.getDeprbp())) {
			throw DpModuleError.DpstComm.BNAS1031();
		}

		if (E_DPCGFG.Y == opdpprin.getIschge()) {
			if (opdpprin.getFretxt().size() == 0 || opdpprin.getFretxt() == null) {
				throw DpModuleError.DpstComm.BNAS0781();
			}
			if (CommUtil.compare(opdpprin.getFeeamt(), opdpprin.getFretxt().get(0).getChrgam()) != 0) {
				throw DpModuleError.DpstComm.BNAS0315();
			}
		}

		// 补打、撤销存款证明时，需输入原存款证明书编号
		if (opdpprin.getDeprbp() == E_DEPRBP.SP || opdpprin.getDeprbp() == E_DEPRBP.CL) {

			if (CommUtil.isNull(opdpprin.getDeprnm())) {

				throw DpModuleError.DpstComm.BNAS1030();
			}

			if (opdpprin.getDeprnm().length() != 16) {
				throw DpModuleError.DpstComm.BNAS1029();
			}
		}

		if (CommUtil.isNull(opdpprin.getIschge())) {
			throw DpModuleError.DpstComm.BNAS0348();
		}

		// 撤销时，不收手续费
		if (opdpprin.getDeprbp() == E_DEPRBP.CL) {
			if (opdpprin.getIschge() != E_DPCGFG.N) {
				throw DpModuleError.DpstComm.BNAS0348();
			}
		}

		if (CommUtil.isNull(opdpprin.getCardno())) {
			throw DpModuleError.DpstProd.BNAS0926();
		}

		if (CommUtil.isNull(opdpprin.getCustna())) {
			throw DpModuleError.DpstComm.BNAS0672();
		}

		if (CommUtil.isNull(opdpprin.getDepram()) || CommUtil.equals(opdpprin.getDepram(), BigDecimal.ZERO)) {
			throw DpModuleError.DpstComm.BNAS0142();
		}

		if (CommUtil.isNull(opdpprin.getStcenm()) || CommUtil.compare(opdpprin.getStcenm(), 0l) == 0) {
			throw DpModuleError.DpstComm.BNAS0418();
		}

		if (CommUtil.isNull(opdpprin.getEncenm()) || CommUtil.compare(opdpprin.getEncenm(), 0l) == 0) {
			throw DpModuleError.DpstComm.BNAS0063();
		}

		if (CommUtil.isNull(opdpprin.getNumber()) || CommUtil.compare(opdpprin.getNumber(), 0l) == 0) {
			throw DpModuleError.DpstComm.BNAS0422();
		}

		if (!opdpprin.getNumber().equals(opdpprin.getEncenm() - opdpprin.getStcenm() + 1)) {
			throw DpModuleError.DpstComm.BNAS0421();
		}

		if (CommUtil.isNull(opdpprin.getBegndt())) {
			throw DpModuleError.DpstComm.BNAS0417();
		}

		if (CommUtil.isNotNull(opdpprin.getDeprtp())) {

			if (opdpprin.getDeprtp() == E_DEPRTP.TP) {

				if (CommUtil.isNotNull(opdpprin.getEnddat())) {

					throw DpModuleError.DpstComm.BNAS0059();
				}

			} else {

				if (CommUtil.isNull(opdpprin.getEnddat())) {

					throw CaError.Eacct.BNAS0061();
				}

			}

		}

		if (CommUtil.isNull(opdpprin.getOpna01())) {
			throw DpModuleError.DpstComm.BNAS1266();
		}

		if (CommUtil.isNull(opdpprin.getOptp01())) {
			throw DpModuleError.DpstComm.BNAS0576();
		}

		if (CommUtil.isNull(opdpprin.getOpno01())) {
			throw DpModuleError.DpstComm.BNAS1605();
		}

		if (CommUtil.isNull(opdpprin.getCrcycd())) {
			throw DpModuleError.DpstComm.BNAS1101();
		}

		KnaAcdc knaAcdc = KnaAcdcDao.selectOne_odb2(opdpprin.getCardno(), true);
		IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(knaAcdc.getCustac());
		if (!CommUtil.equals(opdpprin.getCrcycd(), cplKnaAcct.getCrcycd())) {
			throw DpModuleError.DpstComm.BNAS0313();
		}

		if (CommUtil.equals(opdpprin.getCrcycd(), BusiTools.getDefineCurrency())
				&& CommUtil.isNotNull(opdpprin.getCsextg())) {
			throw DpModuleError.DpstComm.BNAS1098();
		}
		// 校验证件类型、证件号码
		// if(opdpprin.getOptp01() == E_IDTFTP.SFZ){
		// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp =
		// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP.SFZ;
		// BusiTools.chkCertnoInfo(idtftp, opdpprin.getOpno01());
		// }

		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(opdpprin.getCardno(), false);

		if (CommUtil.isNull(caKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0754();
		}

		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getBusi_org_id(), caKnaAcdc.getCorpno())) {
			throw DpModuleError.DpstComm.BNAS0796();
		}

		IoDpKnaAcct cplKnaAcct1 = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(caKnaAcdc.getCustac());
		if (!CommUtil.equals(opdpprin.getCrcycd(), cplKnaAcct1.getCrcycd())) {
			throw DpModuleError.DpstComm.E9999("输入币种与账户币种不一致");
		}

		// if(caKnaAcdc.getStatus() != E_DPACST.NORMAL ){
		// throw DpModuleError.DpstComm.E9999("根据电子账号获取电子账号ID的状态不正常");
		// }

		// 电子账户ID
		String custac = caKnaAcdc.getCustac();

		// 获取客户账号信息
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);

		IoCaKnaCust tblKnaCust = caqry.getKnaCustByCustacOdb1(custac, true);

		// 经办人检查

		// IoSrvCfPerson cifPersonServ = SysUtil.getInstance(IoSrvCfPerson.class);
		// IoSrvCfPerson.IoGetCifCust.Output cfiCust = SysUtil
		// .getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
		// IoSrvCfPerson.IoGetCifCust.InputSetter cifCustCond = SysUtil
		// .getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
		// cifCustCond.setCustno(tblKnaCust.getCustno());
		// cifPersonServ.getCifCust(cifCustCond, cfiCust);

		// if (opdpprin.getOptp01() == E_IDTFTP.SFZ) {
		// if (cfiCust.getIdtftp() !=
		// cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP.SFZ) {
		// throw DpModuleError.DpstComm.BNAS0788();
		// }
		// }

		// if (!cfiCust.getIdtfno().equals(opdpprin.getOpno01())
		// || !cfiCust.getCustna().equals(opdpprin.getOpna01())) {
		// throw DpModuleError.DpstComm.BNAS0788();
		// }

		if (E_DEPRBP.OP == opdpprin.getDeprbp() || E_DEPRBP.SP == opdpprin.getDeprbp()) {
			// 客户账户状态检查
			// if (tblKnaCust.getAcctst() != E_ACCTST.NORMAL) {
			// throw DpModuleError.DpstComm.E9999("客户账号[" + tblKnaCust.getCustac()
			// + "]不正常，不能开立存款证明");
			// }

			// 调用DP模块服务查询冻结状态，检查电子账户状态字
			IoDpFrozSvcType froz = SysUtil.getInstance(IoDpFrozSvcType.class);
			IoDpAcStatusWord cplGetAcStWord = froz.getAcStatusWord(custac);

			/*
			 * if (cplGetAcStWord.getBrfroz() == E_YES___.YES || cplGetAcStWord.getDbfroz()
			 * == E_YES___.YES || cplGetAcStWord.getBkalsp() == E_YES___.YES ||
			 * cplGetAcStWord.getOtalsp() == E_YES___.YES || cplGetAcStWord.getClstop() ==
			 * E_YES___.YES) { throw CaError.Eacct.BNAS0429(); }
			 */
			// 由行方要求分开抛错
			if (cplGetAcStWord.getBrfroz() == E_YES___.YES) {
				throw CaError.Eacct.BNAS0866();
			} else if (cplGetAcStWord.getDbfroz() == E_YES___.YES) {
				throw CaError.Eacct.BNAS0859();
			} else if (cplGetAcStWord.getBkalsp() == E_YES___.YES) {
				throw CaError.Eacct.BNAS0861();
			} else if (cplGetAcStWord.getOtalsp() == E_YES___.YES) {
				throw CaError.Eacct.BNAS0862();
			} else if (cplGetAcStWord.getClstop() == E_YES___.YES) {
				throw CaError.Eacct.BNAS0436();
			}

			// 账户状态为预开户、转久悬、预销户、销户的，交易拒绝，报错：“电子账户状态为***状态，无法操作！”。
			E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
			bizlog.debug("+++++++++++++++++++++++++++++" + "电子账户状态为" + cuacst + "+++++++++++++++++++++++++");

			if (cuacst == E_CUACST.CLOSED) {
				throw DpModuleError.DpstComm.BNAS1597();

			} else if (cuacst == E_CUACST.PRECLOS) {
				throw DpModuleError.DpstComm.BNAS0846();

			} else if (cuacst == E_CUACST.PREOPEN) {
				throw DpModuleError.DpstComm.BNAS1598();

			} else if (cuacst == E_CUACST.NOACTIVE) {
				throw DpModuleError.DpstComm.BNAS1601();

			} else if (cuacst == E_CUACST.DORMANT) {
				throw DpModuleError.DpstComm.BNAS0847();

			} else if (cuacst == E_CUACST.OUTAGE) {
				throw DpModuleError.DpstComm.BNAS0850();

			} else if (cuacst == E_CUACST.NOENABLE) {
				throw DpModuleError.DpstComm.BNAS0848();

			}

			// 电子账户是否涉案
			IoCaSevQryAccout.IoCaQryInacInfo.Output output = SysUtil
					.getInstance(IoCaSevQryAccout.IoCaQryInacInfo.Output.class);
			SysUtil.getInstance(IoCaSevQryAccout.class).qryInac(opdpprin.getCardno(), opdpprin.getCustna(), null, null,
					output);

			final String otcard_in = opdpprin.getCardno();// 转出账号
			final String otacna_in = opdpprin.getCustna();// 转出账户名称
			final String otbrch_in = CommTools.getBaseRunEnvs().getTrxn_branch();// 转出机构
			final String incard_in = null;// 转入账号
			final String inacna_in = null;// 转入账号名称
			final String inbrch_in = CommTools.getBaseRunEnvs().getTrxn_branch();// 转入机构
			final BigDecimal tranam_in = opdpprin.getDepram();// 交易金额
			final String crcycd = opdpprin.getCrcycd();// 币种

			if (E_INSPFG.INVO == output.getInspfg()) {
				// 独立事务
				DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
					@Override
					public Void execute() {
						// 获取涉案账户交易信息登记输入信息
						IoCaKnbTrinInput cplKnbTrin = SysUtil.getInstance(IoCaKnbTrinInput.class);

						cplKnbTrin.setOtcard(otcard_in);// 转出账号
						cplKnbTrin.setOtacna(otacna_in);// 转出账号名称
						cplKnbTrin.setOtbrch(otbrch_in);// 转出账户机构
						cplKnbTrin.setIncard(incard_in);// 转入账号
						cplKnbTrin.setInacna(inacna_in);// 转入账户名称
						cplKnbTrin.setInbrch(inbrch_in);// 转入账户机构
						cplKnbTrin.setTranam(tranam_in);// 交易金额
						cplKnbTrin.setCrcycd(crcycd);// 币种
						cplKnbTrin.setIssucc(E_YES___.NO);// 交易是否成功

						// 涉案账户交易信息登记
						SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(cplKnbTrin);

						return null;
					}
				});

				throw DpModuleError.DpstComm.BNAS0745();
			}

			// 电子账户是否设恐
			SysUtil.getInstance(IoCaSevQryAccout.class).IoCaQryInwadeInfo(opdpprin.getCardno(), null, null, null);
		}

		// 客户名称
		String custna = tblKnaCust.getCustna();

		if (!CommUtil.equals(custna, opdpprin.getCustna())) {
			throw DpModuleError.DpstComm.BNAS0542();
		}

		// 交易发起法人需与电子账号法人一致
		bizlog.debug("++++++++++++" + tblKnaCust.getCorpno() + "+++++++++++++++++");
		bizlog.debug("+++++++++++++++++++++++++++++");

		bizlog.debug("----------" + CommTools.getBaseRunEnvs().getBusi_org_id() + "---------------");
		if (!CommUtil.equals(tblKnaCust.getCorpno(), CommTools.getBaseRunEnvs().getBusi_org_id())) {
			throw DpModuleError.DpstComm.BNAS0793();
		}

		// 交易日期
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();

		// 日期格式检查
		if (!DateTools2.chkIsDate(opdpprin.getBegndt())) {
			throw DpModuleError.DpstComm.BNAS0409();
		}

		if (CommUtil.isNotNull(opdpprin.getEnddat())) {

			if (!DateTools2.chkIsDate(opdpprin.getEnddat())) {

				throw DpModuleError.DpstComm.BNAS0058();
			}
		}

		// 系统日期前一天
		// String preDate = DateTimeUtil.dateAdd("dd", trandt, -1);

		// 时点证明起始日期可小于系统日期,时期证明起始日期必须等于系统日期
		if (E_DEPRBP.OP == opdpprin.getDeprbp()) {

			// delete by songkl 20161109 新增开立历史存款证明，历史存款证明只能是时点类型 -begin
			/*
			 * if(opdpprin.getDeprtp() == E_DEPRTP.TP &&
			 * !CommUtil.equals(opdpprin.getBegndt(), trandt) &&
			 * !CommUtil.equals(opdpprin.getBegndt(), preDate)){
			 * 
			 * throw DpModuleError.DpstComm.E9999("时点证明起始日期必须为当前日期或当前日期的前一天"); }
			 */
			// delete by songkl 20161109 -end
			if (opdpprin.getDeprtp() == E_DEPRTP.TP && CommUtil.compare(opdpprin.getBegndt(), trandt) > 0) {
				throw DpModuleError.DpstComm.BNAS0560();
			}

			if (opdpprin.getDeprtp() == E_DEPRTP.TD && !CommUtil.equals(opdpprin.getBegndt(), trandt)) {

				throw DpModuleError.DpstComm.BNAS0559();
			}

			if (CommUtil.isNotNull(opdpprin.getEnddat())) {

				if (opdpprin.getDeprtp() == E_DEPRTP.TD && CommUtil.compare(opdpprin.getEnddat(), trandt) <= 0) {

					throw DpModuleError.DpstComm.BNAS0056();
				}
			}

		}
		// else if(E_DEPRBP.SP == opdpprin.getDeprbp()){
		//
		// if(opdpprin.getDeprtp() == E_DEPRTP.TD &&
		// !CommUtil.equals(opdpprin.getBegndt(), trandt)){
		// throw DpModuleError.DpstComm.E9999("补打时期证明时起始日期必须为当前日期");
		// }
		//
		// if(opdpprin.getDeprtp() == E_DEPRTP.TD &&
		// CommUtil.compare(opdpprin.getEnddat(), opdpprin.getBegndt()) <= 0){
		// throw DpModuleError.DpstComm.E9999("补打时期证明时终止日期不能小于起始日期");
		// }
		//
		// }

		if (opdpprin.getDeprbp() == E_DEPRBP.OP && CommUtil.compare(opdpprin.getDepram(), BigDecimal.ZERO) <= 0) {
			throw DpModuleError.DpstComm.BNAS0145();
		}

		// 交易日期

		// 可用余额 活期结算类存款+亲情钱包类存款+定期存款类存款-部冻金额-部止金额
		BigDecimal bal = DpAcctProc.getAcctBal(tblKnaCust.getCustac(), opdpprin.getCrcycd());

		bizlog.debug("++++++++++++" + bal + "++++++++++");

		// 若为开立存款证明
		if (opdpprin.getDeprbp() == E_DEPRBP.OP) {

			if (opdpprin.getIschge() == E_DPCGFG.Y) {

				// 开立、补打存款证明收手续费时，手续费金额必须大于0
				if (CommUtil.compare(opdpprin.getFeeamt(), BigDecimal.ZERO) <= 0) {
					throw DpModuleError.DpstComm.BNAS0317();
				}

				/*
				 * if (CommUtil.compare(opdpprin.getDepram().add(opdpprin.getFeeamt ()), bal) >
				 * 0) { throw DpModuleError.DpstComm.E9999("证明金额+手续费不能大于账户可用余额"); }
				 */

				// add by songkl 20161109 -begin
				// 若开立历史存款证明，手续费不得大于账户可用余额，若非历史存款证明，证明金额+手续费不得大于账户可用余额
				if (CommUtil.compare(opdpprin.getBegndt().toString(), trandt) < 0) {
					if (CommUtil.compare(opdpprin.getFeeamt(), bal) > 0) {
						throw DpModuleError.DpstComm.BNAS0318();
					}
				} else {
					if (CommUtil.compare(opdpprin.getDepram().add(opdpprin.getFeeamt()), bal) > 0) {
						throw DpModuleError.DpstComm.BNAS0146();
					}
				}
				// add by songkl 20161109 -end

			} else if (opdpprin.getIschge() == E_DPCGFG.N) {
				if (CommUtil.compare(opdpprin.getFeeamt(), BigDecimal.ZERO) != 0) {
					throw DpModuleError.DpstComm.BNAS0316();
				}
			}

			/*
			 * if (opdpprin.getIschge() == E_DPCGFG.N &&
			 * CommUtil.compare(opdpprin.getDepram(), bal) > 0) { throw
			 * DeptComm.E9999("证明金额不能大于账户可用余额"); }
			 */
			if (CommUtil.compare(opdpprin.getBegndt().toString(), trandt) >= 0) {
				if (opdpprin.getIschge() == E_DPCGFG.N && CommUtil.compare(opdpprin.getDepram(), bal) > 0) {
					throw DpModuleError.DpstComm.BNAS0143();
				}
			} else {

				// 查询账单余额
				BigDecimal hbal = BigDecimal.ZERO;
				IofrozInfo frozInfo = DpAcctDao.selKnlbillHisacctbl(opdpprin.getBegndt(), tblKnaCust.getCustac(),
						false);

				if (CommUtil.isNull(frozInfo)) {
					// 查询历史账单余额
					IofrozInfo hfrozInfo = DpAcctDao.selHistAcctblByCustac(tblKnaCust.getCustac(), opdpprin.getBegndt(),
							false);

					if (CommUtil.isNull(hfrozInfo)) {
						hbal = BigDecimal.ZERO;

					} else {
						hbal = hfrozInfo.getTranam();

					}

				} else {
					hbal = frozInfo.getTranam();

				}

				bizlog.debug("++++++++++++" + hbal + "++++++++++");

				if (CommUtil.compare(opdpprin.getDepram(), hbal) > 0) {
					throw DpModuleError.DpstComm.BNAS0144();
				}

			}

			// add by songkl 20161109 开立历史存款证明时，证明金额允许大于账户余额

			// 若为存款证明补打
		} else if (opdpprin.getDeprbp() == E_DEPRBP.SP) {
			KnbDepr tblKnbDepr = KnbDeprDao.selectFirst_odb2(opdpprin.getDeprnm(), opdpprin.getDeprtp(), E_DEPRBP.OP,
					false);

			if (CommUtil.isNull(tblKnbDepr)) {
				throw DpModuleError.DpstComm.BNAS0729();
			}

			KnbFroz tblKnbFroz = KnbFrozDao.selectOne_odb17(custac, tblKnbDepr.getMntrsq(), false);
			// 非历史存款证明
			if (CommUtil.isNotNull(tblKnbFroz)) {
				if (E_FROZST.INVALID == tblKnbFroz.getFrozst()) {
					throw DpModuleError.DpstComm.BNAS0756();
				}
			}

			// 补打和开立机构必须一致
			// if(!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
			// tblKnbDepr.getTranbr())){
			// throw DpModuleError.DpstComm.E9999("补打机构必须为原开立机构");
			// }

			// 比较输入的证明金额和开立存款证明金额是否一致
			if (!CommUtil.equals(tblKnbDepr.getDepram(), opdpprin.getDepram())) {
				throw DpModuleError.DpstComm.BNAS0141();
			}

			// 比较输入的开立日期和补打存款证明开立日期是否一致
			if (!CommUtil.equals(tblKnbDepr.getBegndt(), opdpprin.getBegndt())) {
				throw DpModuleError.DpstComm.BNAS1093();
			}

			// 比较输入的到期日期和补打存款证明到期日期是否一致
			if (!CommUtil.equals(tblKnbDepr.getEnddat(), opdpprin.getEnddat())) {
				throw DpModuleError.DpstComm.BNAS1094();
			}

			// 账户存款资产
			BigDecimal onlnbl = DpDeduct.getAcctBal(custac, opdpprin.getCrcycd());
			// 账户不可用余额
			BigDecimal unusebal = DpFrozTools.getFrozBala(E_FROZOW.AUACCT, custac);

			if (CommUtil.compare(onlnbl, unusebal) < 0) {
				throw DpModuleError.DpstComm.BNAS0175();
			}

			BigDecimal balance = onlnbl.subtract(unusebal);

			// 可用余额和手续费比较
			if (opdpprin.getIschge() == E_DPCGFG.Y) {

				if (CommUtil.compare(opdpprin.getFeeamt(), BigDecimal.ZERO) <= 0) {
					throw DpModuleError.DpstComm.BNAS0317();
				}

				if (CommUtil.compare(balance, opdpprin.getFeeamt()) < 0) {
					throw DpModuleError.DpstComm.BNAS0177();
				}
			} else if (opdpprin.getIschge() == E_DPCGFG.N) {
				if (CommUtil.compare(opdpprin.getFeeamt(), BigDecimal.ZERO) != 0) {
					throw DpModuleError.DpstComm.BNAS0316();
				}
			}

			// 若为存款证明撤销
		} else if (opdpprin.getDeprbp() == E_DEPRBP.CL) {
			DpDeprInfo tblKnbDepr = SysUtil.getInstance(DpDeprInfo.class);
			// 法人代码
			String corpno = null;

			if (CommUtil.isNotNull(opdpprin.getCorpno())) {
				corpno = opdpprin.getCorpno();
			} else {
				corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
			}
			tblKnbDepr = DpFrozDao.selOpdeprInfo(opdpprin.getDeprnm(), opdpprin.getDeprtp(), E_DEPRBP.OP, corpno,
					false);

			bizlog.debug("++++++++++++++++++" + corpno + "+++++++++++++++++++++++");
			// KnbDepr tblKnbDepr =
			// KnbDeprDao.selectOne_odb2(opdpprin.getDeprnm(),
			// opdpprin.getDeprtp(), E_DEPRBP.OP, false);

			if (CommUtil.isNull(tblKnbDepr)) {
				throw DpModuleError.DpstComm.BNAS0729();
			}

			KnbFroz tblKnbFroz = KnbFrozDao.selectOne_odb16(custac, tblKnbDepr.getMntrsq(), E_FROZST.VALID, false);
			if (CommUtil.isNull(tblKnbFroz)) {
				throw DpModuleError.DpstComm.E9999("该存款证明已撤销");
			}

			// 比较输入的证明金额和开立存款证明金额是否一致
			if (!CommUtil.equals(tblKnbDepr.getDepram(), opdpprin.getDepram())) {
				throw DpModuleError.DpstComm.BNAS0141();
			}

			KnbDeprOwne tblDpOw = KnbDeprOwneDao.selectOne_odb2(opdpprin.getDeprnm(), true);

			if (CommUtil.compare(opdpprin.getNumber(), tblDpOw.getNumbre()) > 0) {
				throw DpModuleError.DpstComm.BNAS1044();
			}

			// if(opdpprin.getStcenm() != tblKnbDepr.getStcenm()){
			// throw DpModuleError.DpstComm.E9999("撤销的起始凭证号码与开立时不一致");
			// }
			//
			// if(opdpprin.getEncenm() > tblKnbDepr.getStcenm()){
			// throw DpModuleError.DpstComm.E9999("撤销的终止证号码不能大于开立时终止凭证号码");
			// }

			if (CommUtil.isNull(opdpprin.getCorpno())) {
				if (CommUtil.isNotNull(opdpprin.getEnddat())) {

					if (opdpprin.getDeprtp() == E_DEPRTP.TD && CommUtil.compare(opdpprin.getEnddat(), trandt) < 0) {

						throw DpModuleError.DpstComm.BNAS0056();
					}
				}

				// 比较输入的开立日期和补打存款证明开立日期是否一致
				if (!CommUtil.equals(tblKnbDepr.getBegndt(), opdpprin.getBegndt())) {
					throw DpModuleError.DpstComm.BNAS1032();
				}

				// 比较输入的到期日期和补打存款证明到期日期是否一致
				if (!CommUtil.equals(tblKnbDepr.getEnddat(), opdpprin.getEnddat())) {
					throw DpModuleError.DpstComm.BNAS1033();
				}

				// 撤销和开立机构必须一致
				// if(!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				// tblKnbDepr.getTranbr())){
				// throw DpModuleError.DpstComm.E9999("撤销机构必须为原开立机构");
				// }
			}
		}
	}

	/**
	 * 存款证明
	 */
	public static void dpprovProc(IoDpprovIn opdpprin, DpOpprEntity entity) {

		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(opdpprin.getCardno(), false);

		if (CommUtil.isNull(caKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0692();
		}

		if (caKnaAcdc.getStatus() != E_DPACST.NORMAL) {
			throw DpModuleError.DpstComm.BNAS0693();
		}

		// 电子账户ID
		String custac = caKnaAcdc.getCustac();

		// 获取客户账号信息
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);

		IoCaKnaCust tblKnaCust = caqry.getKnaCustByCustacOdb1(custac, true);

		String frozno = null; // 冻结通知书编号
		if (opdpprin.getDeprbp() == E_DEPRBP.OP) { // 开立存款证明止付调用止付服务

			if (CommUtil.compare(opdpprin.getBegndt(), CommTools.getBaseRunEnvs().getTrxn_date()) >= 0) {
				IoDpStopayOt stopayout = SysUtil.getInstance(IoDpStopayOt.class);

				stopayout = callStoppay(opdpprin, tblKnaCust.getCustac());

				frozno = stopayout.getFrozno();
			} else {
				frozno = "";
			}
		}

		// 生成存款证明书流水号
		String deprsq = genDeprsq();

		// 存款证明书编号
		String deprnm = null;

		// 生成存款证明编号
		KnpParameter tblknp_para = KnpParameterDao.selectOne_odb1("IS_CHECK", "SPECTP", "%", "%", false);
		if (CommUtil.isNull(tblknp_para)) {
			throw DpModuleError.DpstComm.BNAS1210();
		}

		if (opdpprin.getDeprbp() == E_DEPRBP.OP) { // 开立存款证明生成存款证明书编号

			if (CommUtil.equals("Y", tblknp_para.getParm_value1())) {

				String specno = "28" + CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4);
				deprnm = getMaxSpecno(E_SPECTP.DPCTCT, specno);

			} else if (CommUtil.equals("N", tblknp_para.getParm_value1())) {
				// 生成冻结编号
				deprnm = "28" + CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4) + genDeprnm();
			}
			// if (opdpprin.getDeprbp() == E_DEPRBP.OP) { //开立存款证明生成存款证明书编号
			// String specno = "28" +
			// CommTools.getBaseRunEnvs().getTrxn_date().substring(0,4);
			//
			// deprnm = getMaxSpecno(E_SPECTP.DPCTCT,specno);
		}

		else if (opdpprin.getDeprbp() == E_DEPRBP.CL || // 撤销或补打存款证明获取输入
				opdpprin.getDeprbp() == E_DEPRBP.SP) {
			// 原冻结证明书编号
			deprnm = opdpprin.getDeprnm();
		}

		// 存款证明登记簿
		KnbDepr tblKnbDepr = SysUtil.getInstance(KnbDepr.class);

		tblKnbDepr.setDeprsq(deprsq); // 存款证明流水号
		tblKnbDepr.setDeprnm(deprnm); // 存款证明编号
		tblKnbDepr.setDeprtp(opdpprin.getDeprtp()); // 存款证明书类型
		tblKnbDepr.setDeprbp(opdpprin.getDeprbp()); // 存款证明业务类型
		tblKnbDepr.setIschge(opdpprin.getIschge()); // 是否收费
		tblKnbDepr.setCustac(custac); // 客户账号
		tblKnbDepr.setCustna(tblKnaCust.getCustna()); // 客户姓名
		tblKnbDepr.setDepram(opdpprin.getDepram()); // 证明金额
		tblKnbDepr.setCrcycd(opdpprin.getCrcycd()); // 币种
		tblKnbDepr.setCsexfg(opdpprin.getCsextg()); // 钞汇标志
		tblKnbDepr.setStcenm(opdpprin.getStcenm()); // 起始凭证号
		tblKnbDepr.setEncenm(opdpprin.getEncenm()); // 终止凭证号
		tblKnbDepr.setNumbre(opdpprin.getNumber()); // 份数
		tblKnbDepr.setBegndt(opdpprin.getBegndt()); // 起始日期
		tblKnbDepr.setEnddat(opdpprin.getEnddat()); // 终止日期
		tblKnbDepr.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); // 交易日期
		tblKnbDepr.setOptp01(opdpprin.getOptp01()); // 经办人证件种类
		tblKnbDepr.setOpno01(opdpprin.getOpno01()); // 经办人证件号码
		tblKnbDepr.setOpna01(opdpprin.getOpna01()); // 经办人名称

		tblKnbDepr.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
		tblKnbDepr.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
		tblKnbDepr.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller()); // 交易柜员
		tblKnbDepr.setLttscd(BusiTools.getBusiRunEnvs().getLttscd()); // 内部交易码
		tblKnbDepr.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水

		if (opdpprin.getDeprbp() == E_DEPRBP.OP) { // 开立存款证明
			KnbDeprDao.insert(tblKnbDepr);

			for (long i = opdpprin.getStcenm(); i <= opdpprin.getEncenm(); i++) {

				List<KnbDeprProf> info = KnbDeprProfDao.selectAll_odb3(i, false);

				if (CommUtil.isNotNull(info)) {
					for (KnbDeprProf tblKnbDeprProf : info) {
						KnbDepr tblKnbDepr1 = KnbDeprDao.selectFirst_odb3(tblKnbDeprProf.getDeprnm(), true);

						if (tblKnbDepr1.getDeprtp() == opdpprin.getDeprtp()) {
							throw DpModuleError.DpstComm.BNAS1179(i);
						}
					}
				}

				// 登记存款证明凭证登记簿
				KnbDeprProf depr_prof = SysUtil.getInstance(KnbDeprProf.class);
				depr_prof.setDeprnm(deprnm);// 存款证明编号
				depr_prof.setVochst(E_VOCHST.USING);// 凭证使用
				depr_prof.setVochnm(i);
				KnbDeprProfDao.insert(depr_prof);
			}

			// 若开立为历史存款证明，开立之后直接撤销
			// if(CommUtil.compare(opdpprin.getBegndt(),
			// CommTools.getBaseRunEnvs().getTrxn_date()) < 0){
			// //更新撤销日为当前日期
			// tblKnbDepr.setEnddat(CommTools.getBaseRunEnvs().getTrxn_date());
			// }
		} else if (opdpprin.getDeprbp() == E_DEPRBP.SP) {// 补打存款证明
			KnbDeprDao.insert(tblKnbDepr);// 登记补打存款证明记录
			for (long i = opdpprin.getStcenm(); i <= opdpprin.getEncenm(); i++) {

				List<KnbDeprProf> info = KnbDeprProfDao.selectAll_odb3(i, false);

				if (CommUtil.isNotNull(info)) {
					for (KnbDeprProf tblKnbDeprProf : info) {
						KnbDepr tblKnbDepr1 = KnbDeprDao.selectFirst_odb3(tblKnbDeprProf.getDeprnm(), true);

						if (tblKnbDepr1.getDeprtp() == opdpprin.getDeprtp()) {
							throw DpModuleError.DpstComm.BNAS0715(i);
						}
					}
				}

				KnbDeprProf tblprof = SysUtil.getInstance(KnbDeprProf.class);
				tblprof.setDeprnm(deprnm); // 存款证明编号
				tblprof.setVochnm(i);// 凭证号码
				tblprof.setVochst(E_VOCHST.USING);// 凭证状态

				KnbDeprProfDao.insert(tblprof);
			}
		} else if (opdpprin.getDeprbp() == E_DEPRBP.CL) { // 撤销存款证明
			KnbDeprDao.insert(tblKnbDepr);

			if (CommUtil.isNull(opdpprin.getCorpno())) {
				for (long i = opdpprin.getStcenm(); i <= opdpprin.getEncenm(); i++) {
					KnbDeprProf tblKnbDeprProf = KnbDeprProfDao.selectOne_odb2(deprnm, i, false);

					if (CommUtil.isNull(tblKnbDeprProf)) {
						throw DpModuleError.DpstComm.BNAS0716(i);
					}

					if (E_VOCHST.RETURN == tblKnbDeprProf.getVochst()) {
						throw DpModuleError.DpstComm.BNAS0717(i);
					}

					KnbDeprProf tblprof = SysUtil.getInstance(KnbDeprProf.class);
					tblprof.setDeprnm(deprnm); // 存款证明编号
					tblprof.setVochnm(i);// 凭证号码
					tblprof.setVochst(E_VOCHST.RETURN);// 凭证状态

					KnbDeprProfDao.updateOne_odb2(tblprof);
				}
			}
		}

		// 写入存款证明主体登记簿
		KnbDeprOwne tblKnbDeprOwne = SysUtil.getInstance(KnbDeprOwne.class);
		tblKnbDeprOwne.setDeprsq(deprsq); // 存款证明流水号
		tblKnbDeprOwne.setDeprnm(deprnm); // 存款证明编号
		tblKnbDeprOwne.setCustac(custac); // 客户账号

		tblKnbDeprOwne.setFrozam(opdpprin.getDepram()); // 冻结金额
		tblKnbDeprOwne.setFrozdt(opdpprin.getBegndt()); // 冻结日期
		tblKnbDeprOwne.setFroztm(CommTools.getBaseRunEnvs().getComputer_time()); // 冻结时间
		tblKnbDeprOwne.setFrozno(frozno); // 冻结编号
		// tblKnbDeprOwne.setIsrecy(E_DPRECY.N); //是否回收

		// 若开立历史存款证明，则开立当天立即回收，否则为未回收
		// if(CommUtil.compare(opdpprin.getBegndt(),
		// CommTools.getBaseRunEnvs().getTrxn_date()) < 0){
		// tblKnbDeprOwne.setIsrecy(E_DPRECY.Y);
		// }else{
		// tblKnbDeprOwne.setIsrecy(E_DPRECY.N);
		// }

		if (opdpprin.getDeprbp() == E_DEPRBP.OP) { // 开立存款证明
			// 历史存款证明
			if (CommUtil.compare(opdpprin.getBegndt(), CommTools.getBaseRunEnvs().getTrxn_date()) < 0) {
				tblKnbDeprOwne.setNumbre(opdpprin.getNumber()); // 凭证使用份数
				tblKnbDeprOwne.setUnfrdt(CommTools.getBaseRunEnvs().getTrxn_date());// 撤销日期
				tblKnbDeprOwne.setIsrecy(E_DPRECY.Y);// 凭证已回收
				tblKnbDeprOwne.setUndobr(CommTools.getBaseRunEnvs().getTrxn_teller());// 撤销柜员
				tblKnbDeprOwne.setUndois(CommTools.getBaseRunEnvs().getTrxn_branch());// 撤销机构

				KnbDeprOwneDao.insert(tblKnbDeprOwne);
				// 当前存款证明
			} else {
				tblKnbDeprOwne.setNumbre(opdpprin.getNumber()); // 凭证使用份数
				KnbDeprOwneDao.insert(tblKnbDeprOwne);
			}
		}

		else if (opdpprin.getDeprbp() == E_DEPRBP.CL) { // 撤销存款证明
			KnbDeprOwne tblDpOw = KnbDeprOwneDao.selectOne_odb2(deprnm, true);
			// 日终批量存款证明自动撤销
			if (CommUtil.isNotNull(opdpprin.getCorpno())) {

				tblDpOw.setIsrecy(E_DPRECY.Y); // 已完全回收
				tblDpOw.setUnfrdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 解冻日期
				tblDpOw.setUnfrtm(CommTools.getBaseRunEnvs().getComputer_time()); // 解冻时间
				tblDpOw.setUndois(CommTools.getBaseRunEnvs().getTrxn_branch()); // 撤销机构
				tblDpOw.setUndobr(CommTools.getBaseRunEnvs().getTrxn_teller()); // 撤销柜员
				// tblDpOw.setRetnum(opdpprin.getNumber());//凭证回收份数

				frozno = DpFrozDao.selFrozByAcAndTp(E_FROZTP.DEPRSTOPAY, tblKnaCust.getCustac(), opdpprin.getDeprnm(),
						true);

				callStunpay(opdpprin, tblKnaCust.getCustac(), frozno);
				// 手工完全撤销存款证明
			}
			if (CommUtil.isNull(opdpprin.getCorpno())
					&& (opdpprin.getNumber() + tblDpOw.getRetnum()) == tblDpOw.getNumbre()) {

				tblDpOw.setIsrecy(E_DPRECY.Y); // 已完全回收
				tblDpOw.setUnfrdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 解冻日期
				tblDpOw.setUnfrtm(CommTools.getBaseRunEnvs().getComputer_time()); // 解冻时间
				tblDpOw.setUndois(CommTools.getBaseRunEnvs().getTrxn_branch()); // 撤销机构
				tblDpOw.setUndobr(CommTools.getBaseRunEnvs().getTrxn_teller()); // 撤销柜员
				tblDpOw.setRetnum(opdpprin.getNumber() + tblDpOw.getRetnum());// 凭证回收份数

				frozno = DpFrozDao.selFrozByAcAndTp(E_FROZTP.DEPRSTOPAY, tblKnaCust.getCustac(), opdpprin.getDeprnm(),
						true);

				callStunpay(opdpprin, tblKnaCust.getCustac(), frozno);
			} else {// 凭证未完全回收
				if (CommUtil.isNull(opdpprin.getCorpno())
						&& CommUtil.compare(tblDpOw.getRetnum() + opdpprin.getNumber(), tblDpOw.getNumbre()) < 0) {

					tblDpOw.setUndois(CommTools.getBaseRunEnvs().getTrxn_branch()); // 撤销机构
					tblDpOw.setUndobr(CommTools.getBaseRunEnvs().getTrxn_teller()); // 撤销柜员

					tblDpOw.setRetnum(tblDpOw.getRetnum() + opdpprin.getNumber());
				}
			}

			KnbDeprOwneDao.updateOne_odb2(tblDpOw);

			// 凭证张数全部回收，才能对存款证明做解止处理,或者日终存款证明到期自动解除
			// if(CommUtil.isNotNull(opdpprin.getCorpno())){
			// frozno = DpFrozDao.selFrozByAcAndTp(E_FROZTP.DEPRSTOPAY,
			// tblKnaCust.getCustac(),opdpprin.getDeprnm(), true);
			//
			// callStunpay(opdpprin, tblKnaCust.getCustac(), frozno);
			// }
		}

		else if (opdpprin.getDeprbp() == E_DEPRBP.SP) { // 补打存款证明
			KnbDeprOwne tblDpOw = KnbDeprOwneDao.selectOne_odb2(deprnm, true);
			tblDpOw.setNumbre(opdpprin.getNumber() + tblDpOw.getNumbre());
			KnbDeprOwneDao.updateOne_odb2(tblDpOw);
		}

		entity.setCustna(tblKnaCust.getCustna()); // 户名
		entity.setAmount(BigDecimal.ZERO); // 可用余额
		entity.setCrcycd(opdpprin.getCrcycd()); // 币种
		entity.setCsextg(opdpprin.getCsextg()); // 钞汇属性
		entity.setDeprnm(deprnm); // 存款证明书编号

	}

	/**
	 * 补打存款证明检查
	 */
	public static void reDpprovCheck(IoDpprovIn opdpprin) {

		KnbDepr tblKnbDepr = KnbDeprDao.selectFirst_odb2(opdpprin.getDeprnm(), opdpprin.getDeprtp(), E_DEPRBP.OP,
				false);

		if (CommUtil.isNull(tblKnbDepr)) {
			throw DpModuleError.DpstComm.BNAS0729();
		}

		// 判断补打或撤销机构与原开立机构是否相同
		if (opdpprin.getDeprbp() == E_DEPRBP.SP) {
			IoCustacInfo custacInfo = DpFrozDao.selCustacInfoByCardno(opdpprin.getCardno(), true);

			if (!CommUtil.equals(CommTools.getBaseRunEnvs().getBusi_org_id(), custacInfo.getCorpno())) {
				throw DpModuleError.DpstComm.BNAS0793();
			}
		}

		KnbDeprOwne tblKnbDeprOwne = KnbDeprOwneDao.selectOne_odb1(tblKnbDepr.getDeprsq(), false);

		if (CommUtil.isNull(tblKnbDeprOwne)) {
			throw DpModuleError.DpstComm.BNAS0729();
		}

		// if (tblKnbDeprOwne.getIsrecy() == E_DPRECY.Y) {
		// throw DpModuleError.DpstComm.E9999("该记录已撤销");
		// }
	}

	/**
	 * 撤销存款证明检查
	 */
	public static void caDpprovCheck(IoDpprovIn opdpprin) {
		DpDeprInfo tblKnbDepr = SysUtil.getInstance(DpDeprInfo.class);
		// 法人代码
		String corpno = null;

		if (CommUtil.isNotNull(opdpprin.getCorpno())) {
			corpno = opdpprin.getCorpno();
		} else {
			corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		}
		tblKnbDepr = DpFrozDao.selOpdeprInfo(opdpprin.getDeprnm(), opdpprin.getDeprtp(), E_DEPRBP.OP, corpno, false);

		bizlog.debug("++++++++++++++++++" + corpno + "+++++++++++++++++++++++");
		// KnbDepr tblKnbDepr = KnbDeprDao.selectOne_odb2(opdpprin.getDeprnm(),
		// opdpprin.getDeprtp(), E_DEPRBP.OP, false);

		if (CommUtil.isNull(tblKnbDepr)) {
			throw DpModuleError.DpstComm.BNAS0729();
		}

		if (tblKnbDepr.getDeprbp() == E_DEPRBP.SP) {
			throw DpModuleError.DpstComm.BNAS0728();
		}

		// if (tblKnbDepr.getDeprbp() == E_DEPRBP.CL &&
		// (!opdpprin.getStcenm().equals(tblKnbDepr.getStcenm()) ||
		// !opdpprin.getEncenm().equals(tblKnbDepr.getEncenm()))) {
		// throw DpModuleError.DpstComm.E9999("证明撤销关联记录不存在或凭证已收回");
		// }

		// 判断补打或撤销机构与原开立机构是否相同
		if (opdpprin.getDeprbp() == E_DEPRBP.SP) {
			IoCustacInfo custacInfo = DpFrozDao.selCustacInfoByCardno(opdpprin.getCardno(), true);

			if (!CommUtil.equals(CommTools.getBaseRunEnvs().getBusi_org_id(), custacInfo.getCorpno())) {
				throw DpModuleError.DpstComm.BNAS0793();
			}
		}

		KnbDeprOwne tblKnbDeprOwne = KnbDeprOwneDao.selectOne_odb1(tblKnbDepr.getDeprsq(), false);

		if (CommUtil.isNull(tblKnbDeprOwne)) {
			throw DpModuleError.DpstComm.BNAS0729();
		}

		if (tblKnbDeprOwne.getIsrecy() == E_DPRECY.Y) {
			throw DpModuleError.DpstComm.BNAS0727();
		}
	}

	/**
	 * 调用止付服务
	 */
	public static IoDpStopayOt callStoppay(IoDpprovIn opdpprin, String custac) {

		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);

		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(opdpprin.getCardno(), false);

		if (CommUtil.isNull(caKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0692();
		}

		if (caKnaAcdc.getStatus() != E_DPACST.NORMAL) {
			throw DpModuleError.DpstComm.BNAS0693();
		}

		// 电子账号ID
		String custac1 = caKnaAcdc.getCustac();

		IoCaKnaCust caKnaCust = caSevQryTableInfo.getKnaCustByCustacOdb1(custac1, false);

		if (CommUtil.isNull(caKnaCust)) {
			throw DpModuleError.DpstComm.BNAS0694();
		}

		// 客户名称
		String custna = caKnaCust.getCustna();

		IoDpStopayIn stopayin = SysUtil.getInstance(IoDpStopayIn.class);
		stopayin.setStoptp(E_STOPTP.DEPRSTOPAY); // 业务类型
		stopayin.setFrlmtp(E_FRLMTP.AMOUNT); // 限制类型
		stopayin.setStuntp(E_STUNTP.PORSTO); // 止解方式
		stopayin.setCsextg(opdpprin.getCsextg()); // 账户钞汇属性
		stopayin.setSttmct(E_STTMCT.DAY); // 止付时间计算
		stopayin.setStopdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 止付时间
		stopayin.setCrcycd(opdpprin.getCrcycd()); // 币种
		stopayin.setCardno(opdpprin.getCardno()); // 客户账号
		stopayin.setCustna(custna); // 客户名称
		stopayin.setStopam(opdpprin.getDepram()); // 止付金额
		// stopayin.setFrozdt(opdpprin.getBegndt()); //冻结日期
		stopayin.setStpldt(opdpprin.getEnddat()); // 冻结终止日期
		stopayin.setIdtp01(opdpprin.getOptp01()); // 经办人证件类型
		stopayin.setIdno01(opdpprin.getOpno01()); // 经办人证件号码
		stopayin.setFrna01(opdpprin.getOpna01()); // 经办人证件姓名
		// stopayin.setIdtp02(opdpprin.getOptp01()); //经办人证件类型
		// stopayin.setIdno02(opdpprin.getOpno01()); //经办人证件号码
		// stopayin.setFrna02(opdpprin.getOpna01()); //经办人证件姓名
		stopayin.setStpldt(opdpprin.getEnddat()); // 冻结终止日期
		stopayin.setSptype(E_SPTYPE.DEPRSTOPAY); // 设默认值
		stopayin.setSfreas("开立存款证明"); // 止付原因
		stopayin.setFrozow(E_FROZOW.AUACCT); // 冻结主体设置为"智能存储"
		stopayin.setFrozcd("DEFAULT"); // 冻结分类码

		IoDpStopayOt stoppayout = SysUtil.getInstance(IoDpFrozSvcType.class).IoStopay(stopayin);

		return stoppayout;
	}

	/**
	 * 调用解止服务
	 */
	public static void callStunpay(IoDpprovIn opdpprin, String custac, String frozno) {

		IoDpUnStopayIn stunpyin = SysUtil.getInstance(IoDpUnStopayIn.class);

		stunpyin.setStopno(frozno); // 止付编号
		stunpyin.setCardno(opdpprin.getCardno()); // 客户账号
		stunpyin.setStopdt(opdpprin.getBegndt()); // 止付日期
		stunpyin.setCustna(opdpprin.getCustna()); // 客户名称
		stunpyin.setIdtp01(opdpprin.getOptp01()); // 经办人证件类型
		stunpyin.setIdno01(opdpprin.getOpno01()); // 经办人证件号码
		stunpyin.setFrna01(opdpprin.getOpna01()); // 经办人证件姓名
		stunpyin.setSfreas("存款证明止付");
		KnbFroz tblKnbFroz = KnbFrozDao.selectOneWithLock_odb1(stunpyin.getStopno(), E_FROZST.VALID, false);
		// 调用解止服务处理
		DpUnfrProc.unStopPayDo(stunpyin, tblKnbFroz);

	}

	/**
	 * 存款证明开立，补打收费
	 */
	public static void freeOfDeposit(IoDpprovIn opdpprin, DpOpprEntity entity) {
		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);

		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(opdpprin.getCardno(), false);

		if (CommUtil.isNull(caKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0692();
		}

		if (caKnaAcdc.getStatus() != E_DPACST.NORMAL) {
			throw DpModuleError.DpstComm.BNAS0693();
		}

		// 币种
		IoCaSrvGenEAccountInfo caType = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);

		E_ACCATP accatp = caType.qryAccatpByCustac(caKnaAcdc.getCustac());

		E_ACSETP acsetp = null;
		// 全功能账户或理财专用户
		if (accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE) {
			acsetp = E_ACSETP.SA;
		}

		// 电子钱包户
		if (accatp == E_ACCATP.WALLET) {
			acsetp = E_ACSETP.MA;
		}

		KnaAcct tblIoDpKnaAcct = CapitalTransDeal.getSettKnaAcctSub(caKnaAcdc.getCustac(), acsetp);
		if (E_ACSETP.SA == acsetp) {
			BigDecimal acctbl = SysUtil.getInstance(DpAcctSvcType.class).getAcctaAvaBal(caKnaAcdc.getCustac(),
					tblIoDpKnaAcct.getAcctno(), tblIoDpKnaAcct.getCrcycd(),
					cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.NO,
					cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES);

			if (CommUtil.compare(acctbl, opdpprin.getFeeamt()) < 0) {
				throw DpModuleError.DpstComm.BNAS1606();
			}

		} else if (E_ACSETP.MA == acsetp) {
			KnaAcct acct = CapitalTransDeal.getSettKnaAcctSub(caKnaAcdc.getCustac(), acsetp);
			if (CommUtil.compare(acct.getOnlnbl(), opdpprin.getFeeamt()) < 0) {
				throw DpModuleError.DpstComm.BNAS1607();
			}
		}

		// 电子账户ID
		String custac = caKnaAcdc.getCustac();
		// 统一收费输入
		IoCgCalFee_IN cgFeeIn = SysUtil.getInstance(IoCgCalFee_IN.class);
		cgFeeIn.setChgflg(E_CHGFLG.ALL);// 费用和客户账同时记账
		cgFeeIn.setTrancy(opdpprin.getCrcycd());// 交易币种
		cgFeeIn.setChrgcy(opdpprin.getCrcycd());// 收费币种
		cgFeeIn.setCstrfg(E_CSTRFG.TRNSFER);// 现转标志
		cgFeeIn.setCustac(custac);// 客户账号或内部户

		if (opdpprin.getDeprbp() == E_DEPRBP.OP) {
			cgFeeIn.setRemark("开立存款证明收费");
			cgFeeIn.setSmrycd(BusinessConstants.SUMMARY_SF);
			cgFeeIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_SF));

		} else if (opdpprin.getDeprbp() == E_DEPRBP.SP) {
			cgFeeIn.setRemark("补打存款证明收费");
			cgFeeIn.setSmrycd(BusinessConstants.SUMMARY_SF);
			cgFeeIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_SF));
		}

		// 计费中心返回，此交易直接前台输入
		for (cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.freeOfDeposit info : opdpprin.getFretxt()) {
			// 计费中心返回，此交易直接前台输入
			IoCgCalCenterReturn cginfo = SysUtil.getInstance(IoCgCalCenterReturn.class);
			if (CommUtil.isNull(info.getChrgcd())) {
				throw DpModuleError.DpstComm.BNAS0777();
			}
			if (info.getChrgcd().substring(2, 3).equals("0")) {// 收费费种代码
				if (info.getTrantp() == E_CGTRTP.PAY || info.getTrantp() == E_CGTRTP.OUTACCOUNT) {
					throw DpModuleError.DpstComm.BNAS0776();
				}
			} else if (info.getChrgcd().substring(2, 3).equals("1")) {
				if (info.getTrantp() == E_CGTRTP.SAVE || info.getTrantp() == E_CGTRTP.INACCOUNT) {
					throw DpModuleError.DpstComm.BNAS1608();
				}
			} else {
				throw DpModuleError.DpstComm.BNAS1609();
			}
			cginfo.setChrgcd(info.getChrgcd());// 费种代码

			if (CommUtil.isNull(info.getTrinfo())) {
				throw DpModuleError.DpstComm.BNAS0602();
			}
			cginfo.setTrinfo(info.getTrinfo());// 交易信息

			if (CommUtil.compare(info.getChrgam(), BigDecimal.ZERO) <= 0) {
				throw DpModuleError.DpstComm.BNAS0335();
			}
			cginfo.setPaidam(info.getChrgam());// 收费金额
			cginfo.setPronum(info.getProdcd());// 产品代码
			cginfo.setClcham(info.getChrgam());// 收费金额
			cginfo.setDircam(opdpprin.getFeeamt());// 优惠后应收金额

			if (CommUtil.isNull(info.getServtp())) {
				throw CaError.Eacct.BNAS0384();
			}
			cginfo.setServtp(info.getServtp());// 渠道

			cgFeeIn.getCalcenter().add(cginfo);
		}
		IoCgChrgSvcType cg = SysUtil.getInstance(IoCgChrgSvcType.class);
		// cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType cg = new
		// IoCgChrgSvcImpl();
		cg.CalCharge(cgFeeIn);
	}

	/**
	 * 在先冻结信息
	 */
	public static List<IoFrozHistInfo> getFrozHistInfo(IoDpStopPayIn cpliodpfrozin) {
		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);

		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(cpliodpfrozin.getCardno(), false);

		if (CommUtil.isNull(caKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0928();
		}
		String custac = "";
		if (JFBaseEnumType.E_STACTP.STMA == cpliodpfrozin.getStactp()) {
			custac = caKnaAcdc.getCustac();
		} else if (JFBaseEnumType.E_STACTP.STSA == cpliodpfrozin.getStactp()) {
			custac = cpliodpfrozin.getAcctno();
		}
		// 查询在先冻结信息
		List<IoFrozHistInfo> frozInfo = DpFrozDao.selFrozHistInfoByCustac(custac, false);

		if (CommUtil.isNotNull(frozInfo)) {
			for (IoFrozHistInfo info : frozInfo) {
				if (info.getFrlmtp() == DpEnumType.E_FRLMTP.ALL) {
					info.setOtsptp(DpEnumType.E_OTSPTP.ALLFROZ);
				}
				if (info.getFrlmtp() == DpEnumType.E_FRLMTP.AMOUNT) {
					info.setOtsptp(DpEnumType.E_OTSPTP.PCFROZ);
				}
				if (info.getFrlmtp() == DpEnumType.E_FRLMTP.OUT) {
					info.setOtsptp(DpEnumType.E_OTSPTP.CMFROZ);
				}
			}
		}

		return frozInfo;
	}

	/**
	 * 获取外部止付(按小时)的到期日期
	 * 
	 * @throws ParseException
	 */
	public static String getFreddt(int n, String str) throws ParseException {
		SimpleDateFormat sim = new SimpleDateFormat("yyyyMMddHHmmss");
		Date d = sim.parse(str);
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.HOUR_OF_DAY, n);
		String tmstmp = new SimpleDateFormat("yyyyMMddHHmmss").format(c.getTime());
		return tmstmp;
	}

	/**
	 * 获取账户余额
	 */
	public static BigDecimal accountBal(IoDpStopPayIn cpliodpfrozin) {
		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);

		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(cpliodpfrozin.getCardno(), false);

		if (CommUtil.isNull(caKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0928();
		}

		BigDecimal bal = BigDecimal.ZERO;
		// 查询活期子账户信息
		List<KnaAcct> acctList = KnaAcctDao.selectAll_odb6(caKnaAcdc.getCustac(), false);
		// 获取活期子账户所有余额
		if (CommUtil.isNotNull(acctList)) {
			for (KnaAcct acct : acctList) {
				bal = bal.add(DpAcctProc.getAcctBalance(acct));
			}
		}

		// 查询定期子账户信息
		List<KnaFxac> fxacList = KnaFxacDao.selectAll_odb5(caKnaAcdc.getCustac(), false);
		// 获取活期子账户所有余额
		if (CommUtil.isNotNull(fxacList)) {
			for (KnaFxac fxac : fxacList) {
				bal = bal.add(fxac.getOnlnbl());
			}
		}

		bizlog.parm("账户余额", bal);

		return bal;
	}

	/**
	 * 获取当前最大特殊业务编号
	 */
	public static String getMaxSpecno(E_SPECTP spectp, String specno) {
		String specnm = null;

		if (E_SPECTP.FREEZE == spectp) {// 冻结

			if (CommUtil.isNull(DpFrozDao.selMaxFrozno(false))) {
				specnm = specno + "0000000001";

			} else {
				KnbFroz tblKnbFroz = DpFrozDao.selMaxFrozno(false);

				bizlog.debug("----------------最大冻结序号为：" + tblKnbFroz.getFrozno());
				if (CommUtil.isNotNull(tblKnbFroz)) {
					// 判断当前系统年份是否相同
					if (CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4),
							tblKnbFroz.getFrozno().substring(2, 6))) {

						tblKnbFroz = KnbFrozDao.selectOneWithLock_odb8(tblKnbFroz.getFrozno(), tblKnbFroz.getFrozsq(),
								true);
						long frozno = Long.parseLong(tblKnbFroz.getFrozno()) + 1;
						specnm = String.valueOf(frozno);

					} else {

						specnm = specno + "0000000001";
					}
				}
			}

		} else if (E_SPECTP.DEDUCT == spectp) {// 扣划

			if (CommUtil.isNull(DpFrozDao.selMaxDeduno(specno, false))) {
				specnm = specno + "0000000001";

			} else {
				KnbDedu tblKnbDedu = DpFrozDao.selMaxDeduno(specno, false);

				// 判断当前系统年份是否相同
				if (CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4),
						tblKnbDedu.getDeduno().substring(2, 6))) {

					tblKnbDedu = KnbDeduDao.selectOneWithLock_odb1(tblKnbDedu.getDeduno(), true);
					long frozno = Long.parseLong(tblKnbDedu.getDeduno()) + 1;
					specnm = String.valueOf(frozno);

				} else {

					specnm = specno + "0000000001";
				}
			}

		} else if (E_SPECTP.STOPPY == spectp) {// 止付

			if (CommUtil.isNull(DpFrozDao.selMaxStopno(false))) {
				specnm = specno + "0000000001";

			} else {

				KnbFroz tblKnbFroz = DpFrozDao.selMaxStopno(false);
				bizlog.debug("----------------最大冻结序号为：" + tblKnbFroz.getFrozno());
				// 判断当前系统年份是否相同
				if (CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4),
						tblKnbFroz.getFrozno().substring(2, 6))) {

					tblKnbFroz = KnbFrozDao.selectOneWithLock_odb8(tblKnbFroz.getFrozno(), tblKnbFroz.getFrozsq(),
							true);
					long frozno = Long.parseLong(tblKnbFroz.getFrozno()) + 1;
					specnm = String.valueOf(frozno);

				} else {

					specnm = specno + "0000000001";
				}
			}

		} else if (E_SPECTP.DPCTCT == spectp) {// 存款证明

			if (CommUtil.isNull(DpFrozDao.selMaxDeprnm(specno, false))) {
				specnm = specno + "0000000001";

			} else {
				KnbDepr tblKnbDepr = DpFrozDao.selMaxDeprnm(specno, false);

				// 判断当前系统年份是否相同
				if (CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4),
						tblKnbDepr.getDeprnm().substring(2, 6))) {

					tblKnbDepr = KnbDeprDao.selectOneWithLock_odb1(tblKnbDepr.getDeprsq(), tblKnbDepr.getDeprbp(),
							true);
					long frozno = Long.parseLong(tblKnbDepr.getDeprnm()) + 1;
					specnm = String.valueOf(frozno);

				} else {

					specnm = specno + "0000000001";
				}
			}

		} else if (E_SPECTP.CUSTOP == spectp) {// 账户保护

			if (CommUtil.isNull(DpFrozDao.selMaxCustno(false))) {
				specnm = specno + "0000000001";

			} else {

				KnbFroz tblKnbFroz = DpFrozDao.selMaxCustno(false);

				// 判断当前系统年份是否相同
				if (CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4),
						tblKnbFroz.getFrozno().substring(2, 6))) {

					tblKnbFroz = KnbFrozDao.selectOneWithLock_odb8(tblKnbFroz.getFrozno(), tblKnbFroz.getFrozsq(),
							true);

					long frozno = Long.parseLong(tblKnbFroz.getFrozno()) + 1;
					specnm = String.valueOf(frozno);

				} else {

					specnm = specno + "0000000001";
				}
			}

		}

		return specnm;
	}

	/**
	 * 系统止付前检查
	 * 
	 * @param acctno
	 * @param frozam
	 * @param crcycd
	 * @param froztp
	 */
	public static void syetemStopayCheck(String acctno, BigDecimal frozam, String crcycd, E_FROZTP froztp) {

		if (CommUtil.isNull(acctno)) {
			throw DpModuleError.DpstComm.E9999("子户账号不能为空");
		}

		if (CommUtil.isNull(crcycd)) {
			throw DpModuleError.DpstComm.BNAS1101();
		}

		if (CommUtil.isNull(froztp)) {
			throw DpModuleError.DpstComm.BNAS0820();
		}

		if (CommUtil.isNull(frozam)) {
			throw DpModuleError.DpstComm.BNAS0829();
		}

		KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(acctno, false);

		if (CommUtil.isNull(tblKnaAcct)) {
			throw DpModuleError.DpstComm.E9999("请输入有效的子户账号");
		}

		if (tblKnaAcct.getAcctst() != E_DPACST.NORMAL) {
			throw DpModuleError.DpstComm.E9999("子户状态异常");
		}

		// 电子账号ID
		String custac = tblKnaAcct.getCustac();

		// 账户状态为预开户、转久悬、预销户、销户的，交易拒绝，报错：“电子账户状态为***状态，无法操作！”。
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		bizlog.debug("+++++++++++++++++++++++++++++" + "电子账户状态为" + cuacst + "+++++++++++++++++++++++++");

		if (cuacst == E_CUACST.CLOSED) {
			throw DpModuleError.DpstComm.BNAS1597();

		} else if (cuacst == E_CUACST.PRECLOS) {
			throw DpModuleError.DpstComm.BNAS0846();

		} else if (cuacst == E_CUACST.PREOPEN) {
			throw DpModuleError.DpstComm.BNAS1598();

		} else if (cuacst == E_CUACST.NOACTIVE) {
			throw DpModuleError.DpstComm.BNAS1601();

			/*
			 * } else if (cuacst == E_CUACST.DORMANT) { throw DpModuleError.DpstComm.BNAS0847();
			 */

		} else if (cuacst == E_CUACST.OUTAGE && !CommUtil.equals("disabl", CommTools.getBaseRunEnvs().getTrxn_code())) {
			throw DpModuleError.DpstComm.BNAS0850();

		} else if (cuacst == E_CUACST.NOENABLE) {
			throw DpModuleError.DpstComm.BNAS0848();

		}

		// 检查当前当前账户状态字是否异常
		// CapitalTransCheck.ChkAcctstWord(custac);
		CapitalTransCheck.ChkAcctFrozIN(custac);

		// 可用余额 add by xiongzhao 2016/12/22 (查询结算子账户，计算可用余额)
		// IoDpKnaAcct cplKnaAcct =
		// SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);

		BigDecimal tranbl = SysUtil.getInstance(DpAcctSvcType.class).getAcctaAvaBal(custac, acctno, crcycd,
				E_YES___.YES, E_YES___.NO);

		// tranbl = tranbl.subtract(DpFrozTools.getFrozBala(E_FROZOW.AUACCT,
		// custac));

		if (CommUtil.compare(frozam, tranbl) > 0) {
			throw DpModuleError.DpstComm.BNAS0442();
		}
	}

	/**
	 * 系统止付
	 * 
	 * @param cpliodpstfzin
	 */
	public static void systemStopayDo(String acctno, BigDecimal frozam, String crcycd, E_FROZTP froztp) {

		// 获取当前交易日期
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String frozno = null;
		long frozsq = 0L;

		KnbFroz tblKnbFroz1 = KnbFrozDao.selectOne_odb18(acctno, froztp, trandt, E_FROZST.VALID, false);

		// 若冻结明细表存在子账户
		if (CommUtil.isNotNull(tblKnbFroz1)) {
			// 获取原冻结编号
			frozno = tblKnbFroz1.getFrozno();
			frozsq = ConvertUtil.toLong(BusiTools.getSequence("dpdtfz", 10, "0"));

			tblKnbFroz1.setFrozsq(frozsq);
			tblKnbFroz1.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());
			tblKnbFroz1.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());
			tblKnbFroz1.setFroztm(CommTools.getBaseRunEnvs().getComputer_time());
			tblKnbFroz1.setMntrsq(CommTools.getBaseRunEnvs().getTrxn_seq());

			KnbFrozDao.updateOne_odb2(tblKnbFroz1);

			KnbFrozDetl tblKnbfrozdetl = SysUtil.getInstance(KnbFrozDetl.class);
			tblKnbfrozdetl.setFrowid(acctno);
			tblKnbfrozdetl.setFrozam(frozam);
			tblKnbfrozdetl.setFrozbl(frozam);
			tblKnbfrozdetl.setFrozsq(frozsq);

			auacctStopPay(tblKnbfrozdetl, tblKnbFroz1);
			// 冻结明细表中不存在当前子账户的记录，则生成冻结编号
		} else {
			// 生成冻结编号,系统内部定
			frozno = "99" + CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 8) + genFrozno(E_FROZTP.FNFROZ,
					CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getTrxn_branch());

			// 冻结序号
			frozsq = ConvertUtil.toLong(BusiTools.getSequence("dpdtfz", 10, "0"));

			KnbFroz tblKnbFroz = SysUtil.getInstance(KnbFroz.class);

			// 基本冻结信息
			tblKnbFroz.setFrozno(frozno);// 冻结编号
			tblKnbFroz.setFrozsq(frozsq);// 冻结序号
			tblKnbFroz.setCustac(acctno);// 客户账号
			tblKnbFroz.setFroztp(froztp); // 冻结业务类型
			tblKnbFroz.setFrozcd("DEFAULT"); // 冻结分类码设置为DEFAULT
			tblKnbFroz.setFrozow(E_FROZOW.AUACCT); // 冻结主体类型(默认设置为智能储蓄)
			tblKnbFroz.setFrlmtp(E_FRLMTP.AMOUNT); // 限制类型
			tblKnbFroz.setFrozlv(99l); // 冻结级别

			tblKnbFroz.setFroztm(CommTools.getBaseRunEnvs().getComputer_time()); // 冻结时间
			tblKnbFroz.setFrozdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 冻结日期
			tblKnbFroz.setFrbgdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 冻结起始日期
			// tblKnbFroz.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
			tblKnbFroz.setMntrsq(CommTools.getBaseRunEnvs().getTrxn_seq()); // 上送系统流水

			/**
			 * add by huangwh 20181130: start description: 参数中有冻结终止日期就用参数中的！
			 */
			// if(!CommUtil.isNull(cpliodpstfzin.getEndday())){
			// tblKnbFroz.setFreddt(cpliodpstfzin.getEndday()); // 冻结终止日期
			// }else{
			// tblKnbFroz.setFreddt("20990101"); // 冻结终止日期(默认设置为20990101)
			// }
			// if(!CommUtil.isNull(cpliodpstfzin.getSprdna())){
			// tblKnbFroz.setFrreas("存款冻结:产品名称["+cpliodpstfzin.getSprdna()+"]产品编号:["+cpliodpstfzin.getSprdid()+"]");
			// // 冻结原因
			// }else{
			// tblKnbFroz.setFrreas(null); // 冻结原因
			// }
			//
			tblKnbFroz.setFreddt(null); // 冻结终止日期
			tblKnbFroz.setFrreas(null); // 冻结原因
			/**
			 * add by huangwh 20181130: end
			 */
			tblKnbFroz.setFrozst(E_FROZST.VALID); // 冻结状态
			tblKnbFroz.setStopms(null);// 止付措施

			tblKnbFroz.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
			tblKnbFroz.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq()); // 交易流水
			tblKnbFroz.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller()); // 交易柜员
			tblKnbFroz.setCkuser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 授权柜员
			// tblKnbFroz.setCptrcd(CommTools.getBaseRunEnvs().getCptrcd()); // 对账代码
			tblKnbFroz.setLttscd(BusiTools.getBusiRunEnvs().getTrxn_code()); // 内部交易码

			// 系统止付登记
			tblKnbFroz.setFrctno(null); // 冻结通知书编号
			tblKnbFroz.setFrcttp(null); // 冻结文书种类
			tblKnbFroz.setFrexog(null); // 执法部门类型
			tblKnbFroz.setFrogna(null); // 执法部门名称
			tblKnbFroz.setIdtp01(null); // 执法人员1证件种类
			tblKnbFroz.setIdno01(null); // 执法人员1证件号码
			tblKnbFroz.setFrna01(null); // 执法人员1名称
			tblKnbFroz.setIdtp02(null); // 执法人员2证件种类
			tblKnbFroz.setIdno02(null); // 执法人员2证件号码
			tblKnbFroz.setFrna02(null); // 执法人员2名称
			tblKnbFroz.setPruser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 审批柜员
			tblKnbFroz.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
			// MsSystemSeq.getTrxnSeq(); //重置流水
			tblKnbFroz.setCrcycd(crcycd); // 币种
			tblKnbFroz.setCsextg(null); // 账户钞会标志
			tblKnbFroz.setRemark("系统止付"); // 备注
			tblKnbFroz.setStactp(JFBaseEnumType.E_STACTP.STSA);

			// 登记冻结登记簿
			KnbFrozDao.insert(tblKnbFroz);

			KnbFrozDetl tblKnbfrozdetl = SysUtil.getInstance(KnbFrozDetl.class);
			tblKnbfrozdetl.setFrowid(acctno);
			tblKnbfrozdetl.setFrozam(frozam);
			tblKnbfrozdetl.setFrozbl(frozam);
			tblKnbfrozdetl.setFrozsq(frozsq);

			auacctStopPay(tblKnbfrozdetl, tblKnbFroz);
		}
	}

    public static void insertDetalInfo(KnbAply aplyDo, BigDecimal frozenAmount,
        KnlIoblCupsEdmContro knlIoblCups) {
        KnbFrozDeta detalDo = SysUtil.getInstance(KnbFrozDeta.class);
		detalDo.setAplyno(aplyDo.getAplyno());// 申请编号
		detalDo.setFrozsq(String.valueOf(MsSeqUtil.genSeqId("froz_deta")));// 冻结序号
		detalDo.setFrozow(JFBaseEnumType.E_STACTP.STSA);// 冻结主体类型
		detalDo.setFrowid(knlIoblCups.getAcctno());// 冻结主体ID
		detalDo.setFrozam(frozenAmount);// 冻结金额
		detalDo.setFrozdt(CommTools.getBaseRunEnvs().getComputer_date());// 冻结日期
		detalDo.setFroztm(CommTools.getBaseRunEnvs().getComputer_time());// 冻结时间
		detalDo.setTdtrsq(knlIoblCups.getMntrsq());// 收单交易流水
		detalDo.setTdtrdt(knlIoblCups.getTrandt());// 收单交易日期
		detalDo.setFrozst(DpRiskEnumType.E_FRDEST.FROZEN);// 冻结状态
		detalDo.setIsagnt(E_YES___.NO); 
		detalDo.setFrozbl(aplyDo.getFrozbl().subtract(frozenAmount));
        // 新增商户编号、参考号
		detalDo.setMrchno(knlIoblCups.getInmeid());
		detalDo.setRefeno(knlIoblCups.getRefeno());
		detalDo.setBradna(knlIoblCups.getSbrand());
		detalDo.setSbrand(knlIoblCups.getOrgaid());
        KnbFrozDetaDao.insert(detalDo);
	}

    public static void insertDetalInfo(KnbAplyDto aplyDo, BigDecimal frozenAmount, String acctno, String tdtrsq,
        String tdtrdt, String inmeid, String refeno, String orgaid, E_YES___ isagnt) {
        KnbFrozDeta detalDo = SysUtil.getInstance(KnbFrozDeta.class);
		detalDo.setAplyno(aplyDo.getAplyno());// 申请编号
		detalDo.setFrozsq(String.valueOf(MsSeqUtil.genSeqId("froz_deta")));// 冻结序号
		detalDo.setFrozow(JFBaseEnumType.E_STACTP.STSA);// 冻结主体类型
		detalDo.setFrowid(acctno);// 冻结主体ID
		detalDo.setFrozam(frozenAmount);// 冻结金额
		detalDo.setFrozdt(CommTools.getBaseRunEnvs().getComputer_date());// 冻结日期
		detalDo.setFroztm(CommTools.getBaseRunEnvs().getComputer_time());// 冻结时间
		detalDo.setTdtrsq(tdtrsq);// 收单交易流水
		detalDo.setTdtrdt(tdtrdt);// 收单交易日期
		detalDo.setFrozst(DpRiskEnumType.E_FRDEST.FROZEN);// 冻结状态
		detalDo.setFrozbl(aplyDo.getFrozbl().subtract(frozenAmount));
        detalDo.setIsagnt(isagnt);
        detalDo.setMrchno(inmeid);
        detalDo.setRefeno(refeno);
        detalDo.setSbrand(orgaid);
        KnbFrozDetaDao.insert(detalDo);

		aplyDo.setFrozbl(aplyDo.getFrozbl().subtract(frozenAmount));
		if (aplyDo.getFrozbl().compareTo(BigDecimal.ZERO) == 0) {
			aplyDo.setFrapst(DpRiskEnumType.E_FRAPST.FROZEN);
		} else {
			aplyDo.setFrapst(DpRiskEnumType.E_FRAPST.PARTIAL_FROZEN);
		}
	}

	public static KnbFrozOwne setOwneInfo() {
		KnbFrozOwne ownedoo = SysUtil.getInstance(KnbFrozOwne.class);
		ownedoo.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		ownedoo.setFrozow(DpEnumType.E_FROZOW.AUACCT);
		ownedoo.setTmstmp(CommTools.getBaseRunEnvs().getTimestamp());
		return ownedoo;
	}

    public static KnbThaw setKnbThawInfo(KnbAplyDto ent, String seq) {
        KnbThaw enThaw = SysUtil.getInstance(KnbThaw.class);
		enThaw.setOdfrno(ent.getAplyno());// 原申请编号
		enThaw.setUnfrsq(seq);// 解冻序号
		enThaw.setAplyid(ent.getAplyid());// 解冻账号ID
		enThaw.setFrozow(JFBaseEnumType.E_STACTP.STSA);// 解冻账号类型
		enThaw.setUnexog(DpRiskEnumType.E_FREXOG.FK);// 解冻部门
		enThaw.setEnteruser(ent.getOperator());// 录入柜员
		enThaw.setEntertime(ent.getOptime());// 录入时间
		enThaw.setOperator(CommTools.getBaseRunEnvs().getTrxn_teller());// 操作人
		enThaw.setOperatetime(CommTools.getBaseRunEnvs().getComputer_time());// 操作时间
		enThaw.setLttscd(BusiTools.getBusiRunEnvs().getTrxn_code());// 内部交易码？？
		enThaw.setUnfrdt(CommTools.getBaseRunEnvs().getComputer_date());// 解冻日期
		enThaw.setUnfrtm(CommTools.getBaseRunEnvs().getComputer_time());// 解冻时间
		enThaw.setMntrsq(ent.getMntrsq());// 主交易流水
		enThaw.setUnmark("解冻");// 解冻备注
		enThaw.setMtdate("");// 维护日期
		return enThaw;
	}

    public static void insertKnbRecord(String aplyno, DpRiskEnumType.E_OPDESC en) {
        KnbRecord record = SysUtil.getInstance(KnbRecord.class);
        record.setAplyno(aplyno);
        record.setMntrsq(CommTools.getBaseRunEnvs().getTrxn_seq());
		record.setOperator(CommTools.getBaseRunEnvs().getTrxn_teller());
		record.setOptime(CommTools.getBaseRunEnvs().getTimestamp());
		record.setOpdesc(en);
		DpRiskDao.insertKnbRecord(record);
	}

    /**
     * 即富冻结检查
     * @param dpFrozInfo
     */
	public static void frozByLawCheckJF(DpFrozInfo cpliodpfrozin) {
		if (CommUtil.isNull(cpliodpfrozin.getFrozwy())) {
			throw DpModuleError.DpstComm.BNAS0816();
		}

		if (cpliodpfrozin.getFrozwy().equals(E_FROZWY.CONTIN) || cpliodpfrozin.getFrozwy().equals(E_FROZWY.TSOLVE)) {// 续冻或者解冻
			throw DpModuleError.DpstComm.BNAS0814();
		}

		if (cpliodpfrozin.getFrozwy().equals(E_FROZWY.BORROW)) {// 借冻
			cpliodpfrozin.setFrlmtp(E_FRLMTP.OUT);// 只进不出
		}

		if (cpliodpfrozin.getFrozwy().equals(E_FROZWY.DOUBLE)) {// 双冻
			cpliodpfrozin.setFrlmtp(E_FRLMTP.ALL);// 不进不出
		}

		if (cpliodpfrozin.getFrozwy().equals(E_FROZWY.PARTOF)) {// 部冻
			cpliodpfrozin.setFrlmtp(E_FRLMTP.AMOUNT);// 制定金额
		}
		
		if (!cpliodpfrozin.getAcctList().isEmpty() && !cpliodpfrozin.getFrozwy().equals(E_FROZWY.PARTOF)) {
			throw DpModuleError.DpAcct.AT010014();
		}

       	//查询账户信息
    	IoCaKnaAcdc custInfo = DpFrozDao.selCardCustacByAcctno(cpliodpfrozin.getCardno(),false);  	
       	if(custInfo!=null) {       	
       	//主账户冻结信息验证
       		List<IoDpKnbFroz> doubleList = DpFrozDao.selFrozListByFrozwy(custInfo.getCustac(),
       				DpEnumType.E_FROZST.VALID, DpEnumType.E_FROZTP.JUDICIAL, DpEnumType.E_FRLMTP.ALL, false);
       		if(doubleList.size() == 0 || doubleList == null) {
       			List<IoDpKnbFroz> DList = DpFrozDao.selFrozListByFrozwy(custInfo.getCustac(),
       					DpEnumType.E_FROZST.VALID, DpEnumType.E_FROZTP.JUDICIAL, DpEnumType.E_FRLMTP.OUT, false);
       		    if(DList.size() != 0 && DList != null) {
       		    	if(DpEnumType.E_FROZWY.DOUBLE != cpliodpfrozin.getFrozwy()) {
       		    		throw DpModuleError.DpAcct.AT020039();
       		    	}
       		    }
       		}else {
       			throw DpModuleError.DpAcct.AT020040();
       		}      		
       	}else {
       			throw DpModuleError.DpAcct.AT020028();
       	}
       	
		if (CommUtil.isNull(cpliodpfrozin.getCrcycd())) {
			throw DpModuleError.DpstComm.BNAS1101();
		}

		// 冻结日期检查
		if (CommUtil.isNull(cpliodpfrozin.getFrozdt())) {
			throw DpModuleError.DpstComm.BNAS0828();
		}

		if (!DateTools2.chkIsDate(cpliodpfrozin.getFrozdt())) {
			throw DpModuleError.DpstComm.BNAS0827();
		}
		
		/*if (CommUtil.compare(cpliodpfrozin.getFrozdt(), CommTools.getBaseRunEnvs().getTrxn_date()) != 0) {
			throw DpModuleError.DpstComm.BNAS0308(cpliodpfrozin.getFrozdt(), CommTools.getBaseRunEnvs().getTrxn_date());
		}*/

		// 金额冻结检查
		if (cpliodpfrozin.getFrlmtp() == E_FRLMTP.AMOUNT) {
			if (CommUtil.isNull(cpliodpfrozin.getFztoam())) {
				throw DpModuleError.DpstComm.E9027(DpDict.Froz.frozam.getLongName());
			}

			if (CommUtil.compare(cpliodpfrozin.getFztoam(), BigDecimal.ZERO) <= 0) {
				throw DpModuleError.DpstComm.BNAS0830();
			}
		} else {
			if (!(CommUtil.isNull(cpliodpfrozin.getFztoam())
					|| CommUtil.equals(cpliodpfrozin.getFztoam(), BigDecimal.ZERO))) {
				throw DpModuleError.DpstComm.BNAS1137(cpliodpfrozin.getFrlmtp().getLongName());
			}
		}
	}

	/**
	 * 即富冻结
	 * @param cpliodpfrozin
	 * @param entity
	 */
	public static void prcFrozJF(DpFrozInfo cpliodpfrozin, DpFrozOutInfo entity) {

		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String custac = cpliodpfrozin.getCustac();
		KnpFroz tblKnpFroz = DpFrozDao.selFrozInfoByFrozcd("DEFAULT",
				CommTools.getBaseRunEnvs().getBusi_org_id(), false);

		if (CommUtil.isNull(tblKnpFroz))
			throw DpModuleError.DpstComm.BNAS0831("DEFAULT");

		// 账户状态为预开户、转久悬、预销户、销户的，交易拒绝，报错：“电子账户状态为***状态，无法操作！”。
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(cpliodpfrozin.getCustac());

		if (cuacst == E_CUACST.CLOSED) {
			throw DpModuleError.DpstComm.BNAS1597();

		} else if (cuacst == E_CUACST.PREOPEN) {
			throw DpModuleError.DpstComm.BNAS0849();
		}

		// 生成冻结编号
		KnpParameter tblknp_para = KnpParameterDao.selectOne_odb1("IS_CHECK", "SPECTP", "%", "%", false);
		if (CommUtil.isNull(tblknp_para)) {
			throw DpModuleError.DpstComm.BNAS1210();
		}

		String frozno = null;

		if (CommUtil.equals("Y", tblknp_para.getParm_value1())) {
			String specno = "21" + CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4);

			frozno = getMaxSpecno(E_SPECTP.FREEZE, specno);

		} else if (CommUtil.equals("N", tblknp_para.getParm_value1())) {
			// 生成冻结编号
			frozno = "21" + CommTools.getBaseRunEnvs().getTrxn_date().substring(0, 4)
					+ genFrozno(tblKnpFroz.getFroztp(), CommTools.getBaseRunEnvs().getTrxn_date(),
							CommTools.getBaseRunEnvs().getTrxn_branch());
		}

		KnbFroz tblKnbFroz = SysUtil.getInstance(KnbFroz.class);

		// 基本冻结信息
		tblKnbFroz.setFrozsq(1L);// 冻结序号
		tblKnbFroz.setFroztp(E_FROZTP.JUDICIAL); // 冻结业务类型
		tblKnbFroz.setFrozcd("DEFAULT"); // 冻结分类码
		tblKnbFroz.setFrozow(E_FROZOW.AUACCT); // 冻结主体类型
		tblKnbFroz.setFrlmtp(cpliodpfrozin.getFrlmtp()); // 限制类型
		tblKnbFroz.setFrozlv(CommUtil.isNotNull(cpliodpfrozin.getFrozlv()) ? cpliodpfrozin.getFrozlv() : tblKnpFroz.getFrozlv()); // 冻结级别
		tblKnbFroz.setFrozno(frozno);// 冻结编号
		tblKnbFroz.setCustac(custac); // 客户账号
		tblKnbFroz.setFrozdt(trandt); // 冻结日期
		tblKnbFroz.setFroztm(CommTools.getBaseRunEnvs().getComputer_time()); // 冻结时间
		tblKnbFroz.setFrbgdt(cpliodpfrozin.getFrozdt()); // 冻结起始日期
		tblKnbFroz.setFrreas(cpliodpfrozin.getFrreas()); // 冻结原因
		tblKnbFroz.setFrozst(E_FROZST.VALID); // 解冻标志
		tblKnbFroz.setStopms(null);// 止付措施

		if (!cpliodpfrozin.getAcctList().isEmpty()) {
			tblKnbFroz.setFricfg(E_YES___.YES);
		}
		tblKnbFroz.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
		tblKnbFroz.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq()); // 交易流水
		tblKnbFroz.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller()); // 交易柜员
		tblKnbFroz.setCkuser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 授权柜员
		tblKnbFroz.setLttscd(BusiTools.getBusiRunEnvs().getTrxn_code()); // 内部交易码

		// 司法冻结登记
		tblKnbFroz.setPruser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 审批柜员
		tblKnbFroz.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
		tblKnbFroz.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
		tblKnbFroz.setCrcycd(cpliodpfrozin.getCrcycd()); // 币种
		// 登记冻结登记簿
		KnbFrozDao.insert(tblKnbFroz);

		KnbFrozDetl tblKnbfrozdetl = SysUtil.getInstance(KnbFrozDetl.class);
		tblKnbfrozdetl.setFrowid(custac);
		tblKnbfrozdetl.setFrozam(cpliodpfrozin.getFztoam());
		tblKnbfrozdetl.setFrozbl(cpliodpfrozin.getFztoam());
		tblKnbfrozdetl.setFrozsq(1L);
		tblKnbfrozdetl.setFrozow(tblKnbFroz.getFrozow());
		tblKnbfrozdetl.setFrozno(tblKnbFroz.getFrozno());
		tblKnbfrozdetl.setFrozst(E_FROZST.VALID);
		tblKnbfrozdetl.setFrsbsq(genSubFrozSq(tblKnbfrozdetl.getFrowid(), tblKnbFroz.getFrozlv()));
		KnbFrozDetlDao.insert(tblKnbfrozdetl);
		
		Options<acctInfo> acctList = cpliodpfrozin.getAcctList();
		
		auacctStopPayJF(acctList, tblKnbfrozdetl, tblKnbFroz);
		// 需要返回的实体进行赋值
		entity.setFrozno(frozno);
		entity.setCardno(cpliodpfrozin.getCardno());
	
	}

	/**
	 * 即富智能储蓄修改
	 * @param acctInfo
	 * @param tblKnbFroz
	 */
	private static void auacctStopPayJF(Options<acctInfo> acctList, KnbFrozDetl tblKnbfrozdetl, KnbFroz tblKnbFroz) {
		if (acctList.isEmpty()) {
			mergeKnbFrozOwne(tblKnbFroz.getFrozow(), tblKnbfrozdetl.getFrowid(), tblKnbFroz.getFrlmtp(),
					tblKnbfrozdetl.getFrozam());
		} else {
			regKnbFrozOwne(acctList, tblKnbfrozdetl, tblKnbFroz);
		}
	}
}

