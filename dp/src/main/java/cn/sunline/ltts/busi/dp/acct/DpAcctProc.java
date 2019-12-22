package cn.sunline.ltts.busi.dp.acct;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstProd;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.domain.DpAcctOnlnblEntity;
import cn.sunline.ltts.busi.dp.domain.DpSaveEntity;
import cn.sunline.ltts.busi.dp.froz.DpFrozInfoEntity;
import cn.sunline.ltts.busi.dp.froz.DpFrozTools;
import cn.sunline.ltts.busi.dp.namedsql.AccChngbrDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcinDao;
import cn.sunline.ltts.busi.dp.namedsql.DpBaseProdDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAmtn;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAmtnDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbDfir;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbDfirDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPidl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPidlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDptd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.HKnlInfo;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.HKnlInfoDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDetl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDetlDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDrdl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDrdlDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdr;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdrDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBill;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBillDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlInfo;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlInfoDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.FundAcctOnlnbl;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.CalInterTax;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInterestAndPrincipal;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbInRaSelSvc;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ClsAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaDpbPswd;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCacd;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CAINPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSTRFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DRAWCT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRRTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PSDWST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STATUS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STRKTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_MANTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AMNTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BSINDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CTRLWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DRRULE;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEBS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRDPWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PMCRAC;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TEARTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CORRTG;

public class DpAcctProc {

	private static BizLog bizlog = BizLogUtil.getBizLog(DpAcctProc.class);

	/**
	 * 账户余额变更
	 * 
	 * @param acctno
	 * @param amntcd
	 * @param tranam
	 */
	public static void updateDpAcctOnlnbl(DpAcctOnlnblEntity entity, E_FCFLAG fcflag, E_YES___ strkfg) {
		String acctno = entity.getAcctno();
		E_AMNTCD amntcd = entity.getAmntcd();
		BigDecimal tranam = entity.getTranam();
		String crcycd = entity.getCrcycd();
		String cardno = entity.getCardno();
		String custac = entity.getCustac();
		String acseno = entity.getAcseno();
		String toacct = entity.getToacct();
		String opacna = entity.getOpacna();
		String opbrch = entity.getOpbrch(); // 对方账号所属机构

		if (E_FCFLAG.CURRENT == fcflag) {
			DpKnaAcct.updateDpAcctOnlnbl(entity, fcflag, strkfg);
		} else if (E_FCFLAG.FIX == fcflag) {
			DpKnaFxac.updateDpAcctOnlnbl(entity, fcflag, strkfg);
		} else {
			throw DpModuleError.DpstAcct.BNAS0843(fcflag.toString());
		}
		String openbr = entity.getOpenbr();
		String acctna = entity.getAcctna();
		BigDecimal onlnbl = entity.getOnlnbl();
		// 登记余额明细表
		KnlBill bill = SysUtil.getInstance(KnlBill.class);
		bill.setAcctno(acctno);
		bill.setDetlsq(entity.getDetlsq());
		bill.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		bill.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());
		bill.setOpenbr(openbr);
		bill.setAcctna(acctna);
		bill.setAmntcd(amntcd);
		bill.setTrancy(crcycd);
		bill.setCsextg(E_CSEXTG.CASH);
		bill.setTranam(tranam);
		bill.setAcctbl(onlnbl);
		bill.setCardno(cardno);
		bill.setCustac(custac);
		bill.setAcseno(acseno);
		bill.setOpcuac(toacct);
		bill.setOpacna(opacna);
		bill.setOpbrch(opbrch); // 对方账号所属机构
		bill.setBgindt(CommTools.getBaseRunEnvs().getTrxn_date());
		bill.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());
		bill.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		bill.setIntrcd(BusiTools.getBusiRunEnvs().getLttscd());
		bill.setCstrfg(E_CSTRFG.TRNSFER);
		bill.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());
		bill.setMachdt(DateUtil.getNow());
		bill.setProcsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		bill.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());
		// bill.setUssqno(CommTools.getBaseRunEnvs().getUssqno()); //柜员流水
		bill.setAuthus(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 授权柜员串
		bill.setAuthsq(BusiTools.getBusiRunEnvs().getAuthvo().getAuthsq()); // 授权流水
		// bill.setCkaccd(CommTools.getBaseRunEnvs().getCptrcd()); //对账代码 从公共运行变量中取
		bill.setBankcd(entity.getBankcd()); // 对方金融机构代码
		bill.setBankna(entity.getBankna()); // 对方金融机构名称
		if (CommUtil.isNotNull(entity.getSmrycd())) {
			bill.setSmrycd(entity.getSmrycd()); // 摘要代码
		} else {
			bill.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd()); // 摘要代码
		}
		if (CommUtil.isNotNull(entity.getSmryds())) {
			bill.setSmryds(entity.getSmryds()); // 摘要描述
		} else {
			if (CommUtil.isNotNull(bill.getSmrycd())) {
				bill.setSmryds(ApSmryTools.getText(bill.getSmrycd())); // 摘要描述
			}
		}
		if (CommUtil.isNotNull(entity.getRemark())) {
			bill.setRemark(entity.getRemark()); // 备注
		} else {
			bill.setRemark(BusiTools.getBusiRunEnvs().getRemark()); // 备注
		}
		if (E_YES___.NO == strkfg) {
			// 正常交易
			bill.setStrktp(E_STRKTP.NO);
			bill.setPmcrac(E_PMCRAC.NORMAL);
			bill.setCorrtg(E_CORRTG._0);
		} else {
			// 冲正交易
			if (CommUtil.compare(CommTools.getBaseRunEnvs().getTrxn_date(), entity.getOrtrdt()) == 0) {
				bill.setStrktp(E_STRKTP.TODAY);
			} else {
				bill.setStrktp(E_STRKTP.LAST);
			}
			bill.setPmcrac(E_PMCRAC.STRIK);
			bill.setCorrtg(E_CORRTG._0);
			bill.setOrigtq(entity.getOrtrsq());// 原主交易流水
			bill.setOrigpq(entity.getOrigpq());// 原业务流水
			bill.setMsacsq(entity.getOrigaq());// 原柜员流水
			bill.setMsacdt(entity.getOrtrdt());// 原错账日期
		}
		KnlBillDao.insert(bill);

		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("upd", "knl_info", "%", "%", false);

		if (CommUtil.isNotNull(tblKnpParameter)) {
			if (CommUtil.equals(tblKnpParameter.getParm_value3(), "Y")) {
				updKnlInfo(entity);
			}
		}
	}

	private static void updKnlInfo(DpAcctOnlnblEntity entity) {
		KnlInfo tblKnlInfo = SysUtil.getInstance(KnlInfo.class);

		tblKnlInfo.setTransq(entity.getTransq()); // 主交易流水
		tblKnlInfo.setDetlsq(entity.getDetlsq()); // 明细序号
		tblKnlInfo.setProcsq(CommTools.getBaseRunEnvs().getTrxn_seq()); // 业务流水
		tblKnlInfo.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); // 交易日期
		tblKnlInfo.setAcctna(entity.getAcctna()); // 账户名称
		tblKnlInfo.setCustac(entity.getCustac()); // 客户账号
		tblKnlInfo.setServtp(entity.getServtp()); // 交易渠道
		tblKnlInfo.setIntrcd(entity.getIntrcd()); // 内部交易码
		tblKnlInfo.setIpdrss(CommTools.getBaseRunEnvs().getVirt_machine_id()); // 主机IP地址
		tblKnlInfo.setMacdrs(entity.getMacdrs()); // MAC地址
		tblKnlInfo.setTeleno(entity.getTeleno()); // 手机号码
		tblKnlInfo.setImeino(entity.getImeino()); // 设备信息IMEI
		tblKnlInfo.setUdidno(entity.getUdidno()); // 设备信息UDID
		tblKnlInfo.setTrands(entity.getTrands()); // 交易发生地点
		tblKnlInfo.setMtdate(CommTools.getBaseRunEnvs().getTrxn_date()); // 维护日期

		KnlInfoDao.insert(tblKnlInfo);

		HKnlInfo tblHKnlInfo = SysUtil.getInstance(HKnlInfo.class);

		CommUtil.copyProperties(tblHKnlInfo, tblKnlInfo);
		HKnlInfoDao.insert(tblHKnlInfo);

	}

	/**
	 * 获取账户下活期资金池可用余额 custac 电子账户 crcycd 币种 islock 是否加锁查询
	 * 
	 * @return 所有账户余额
	 */
	public static BigDecimal getProductBal(String custac, String crcycd, boolean islock) {
		return DpAcctProc.getProductBal(custac, crcycd, E_YES___.YES, islock);
	}

	/**
	 * 获取账户下活期资金池可用余额
	 * 
	 * @Title: getProductBal
	 * @Description: 获取账户下活期资金池可用余额
	 * @param custac
	 *            电子账号
	 * @param crcycd
	 *            币种
	 * @param isdfam
	 *            是否减去冻结金额
	 * @param islock
	 *            是否加锁查询
	 * @return 可用余额
	 * @author liaojincai
	 * @date 2016年12月6日 下午8:26:33
	 * @version V2.3.0
	 */
	public static BigDecimal getProductBal(String custac, String crcycd, E_YES___ isdfam, boolean islock) {
		BigDecimal bal = BigDecimal.ZERO;
		BigDecimal fixfro_bal = BigDecimal.ZERO;
		// 查询活期产品资金产品池
		List<KupDptd> dptds = DpBaseProdDao.selKupDptdAll(CommTools.getBaseRunEnvs().getBusi_org_id(), false);
		if (CommUtil.isNull(dptds)) {
			dptds = DpBaseProdDao.selKupDptdAll(BusiTools.getCenterCorpno(), false);
		}
		for (KupDptd dptd : dptds) {
			String prodcd = dptd.getProdcd();
			if (E_PRODTP.DEPO == dptd.getProdtp()) {
				// 存款
				// 查询活期账户
				List<KnaAcct> accts = new ArrayList<KnaAcct>();
				List<KnaFxac> fxacs = new ArrayList<KnaFxac>();

				if (islock) {
					accts = ActoacDao.selAllKnaAcctWithLock(crcycd, prodcd, custac, false);
					fxacs = ActoacDao.selAllKnaFxacWithLock(crcycd, prodcd, custac, false);
				} else {
					accts = KnaAcctDao.selectAll_odb3(crcycd, prodcd, custac, false);
					fxacs = KnaFxacDao.selectAll_odb2(crcycd, prodcd, custac, false);
				}
				for (KnaAcct acct : accts) {
					bal = bal.add(getAcctBalance(acct));
					bizlog.parm("活期当前金额onlnbl=[%s]", bal);
				}
				// 查询定期账户
				for (KnaFxac fxac : fxacs) {
					bal = bal.add(fxac.getOnlnbl());
					bizlog.parm("定期当前金额onlnbl=[%s]", bal);
					fixfro_bal = fixfro_bal.add(DpFrozTools.getFrozBala(E_FROZOW.AUACCT, fxac.getAcctno()));
				}
			} else if (E_PRODTP.LOAN == dptd.getProdtp()) {
				// 贷款
				throw DpModuleError.DpstProd.BNAS1711();
			} else if (E_PRODTP.FUND == dptd.getProdtp()) {
				// 基金
				throw DpModuleError.DpstProd.BNAS1711();
			} else {
				throw DpModuleError.DpstProd.BNAS1711();
			}
		}
		if (E_YES___.YES == isdfam) {
			// 减掉智能储蓄冻结余额
			bal = bal.subtract(DpFrozTools.getFrozBala(E_FROZOW.AUACCT, custac));
			// 减掉子账户冻结余额 by zhx
			bal = bal.subtract(fixfro_bal);
		}
		// 可用余额如果小于0，则返回0
		if (CommUtil.compare(bal, BigDecimal.ZERO) < 0)
			bal = BigDecimal.ZERO;
		bizlog.parm("账户余额", bal);
		return bal;
	}

	/**
	 * 获取账户所有可用余额总额
	 * 
	 * @param acctno
	 * @param crcycd
	 * @return
	 */
	public static BigDecimal getAcctBal(String custac, String crcycd) {
		BigDecimal bal = BigDecimal.ZERO;
		// 查询活期子账户信息
		List<KnaAcct> acctList = KnaAcctDao.selectAll_odb6(custac, false);
		// 获取活期子账户所有余额
		if (CommUtil.isNotNull(acctList)) {
			for (KnaAcct acct : acctList) {
				bal = bal.add(getAcctBalance(acct));
			}
		}
		// 查询定期子账户信息
		List<KnaFxac> fxacList = KnaFxacDao.selectAll_odb5(custac, false);
		// 获取活期子账户所有余额
		if (CommUtil.isNotNull(fxacList)) {
			for (KnaFxac fxac : fxacList) {
				bal = bal.add(fxac.getOnlnbl());
			}
		}
		// 减掉智能储蓄冻结余额
		bal = bal.subtract(DpFrozTools.getFrozBala(E_FROZOW.AUACCT, custac));
		// 可用余额如果小于0，则返回0
		if (CommUtil.compare(bal, BigDecimal.ZERO) < 0)
			bal = BigDecimal.ZERO;
		bizlog.parm("账户余额", bal);
		return bal;
	}

	/**
	 * 获取活期子户可用余额
	 * 
	 * @param acctno
	 * @param crcycd
	 * @return
	 */
	public static BigDecimal getAcctOnlnbl(String acctno, boolean islock) {
		BigDecimal onlnbl = BigDecimal.ZERO;
		KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
		/**
		 * mod by xj 20180502 IoHotCtrlSvcType没有服务实现,默认无热点账户
		 */
		/*
		 * E_YES___ hcflag =
		 * SysUtil.getInstance(IoHotCtrlSvcType.class).selHcpDefn(acctno);
		 */
		E_YES___ hcflag = E_YES___.NO;

		if (hcflag == E_YES___.YES) {
			onlnbl = DpPublic.getAvaiam(acctno);
		} else {
			if (islock) {
				tblKnaAcct = KnaAcctDao.selectOneWithLock_odb1(acctno, true);
			} else {
				tblKnaAcct = KnaAcctDao.selectOne_odb1(acctno, true);
			}
			onlnbl = tblKnaAcct.getOnlnbl();
		}
		// TODO 活期子账号冻结暂不支持，暂时不考虑活期子帐号的冻结部分余额。
		// onlnbl = onlnbl.subtract(DpFrozTools.getFrozBala(E_FROZOW.ACCTNO, acctno));
		return onlnbl;
	}

	/**
	 * 
	 * @author renjinghua
	 *         <p>
	 *         <li>2016年8月29日-下午4:29:36
	 *         <li>
	 *         <li>功能描述：查询活期子账号账户余额，排除全部电子账户冻结金额，该方法用于特殊冻结业务场景</li>
	 *         </p>
	 * 
	 * @param acctno
	 *            活期负债账号
	 * @return 活期子账户可用余额
	 */
	public static BigDecimal getAcctOnlnblForFrozbl(String acctno, boolean islock) {
		return DpAcctProc.getAcctOnlnblForFrozbl(acctno, E_YES___.YES, islock);
	}

	/**
	 * 获取子账户可用余额
	 * 
	 * @Title: getAcctOnlnblForFrozbl
	 * @Description: 获取子账户可用余额
	 * @param acctno
	 *            负债账号
	 * @param isdfam
	 *            是否减去冻结金额
	 * @param islock
	 *            是否加锁查询
	 * @return 可用余额
	 * @author liaojincai
	 * @date 2016年12月6日 下午8:45:03
	 * @version V2.3.0
	 */
	public static BigDecimal getAcctOnlnblForFrozbl(String acctno, E_YES___ isdfam, boolean islock) {
		BigDecimal onlnbl = BigDecimal.ZERO;
		KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
		if (islock) {
			tblKnaAcct = DpAcctDao.selKnaAcctWithLock(acctno, true);
		} else {
			tblKnaAcct = DpAcctDao.selOnlnByAcctno(acctno, true);
		}
		onlnbl = getAcctBalance(tblKnaAcct);
		// 排除电子账户全部冻结金额，主体类型暂时默认传智能储蓄，冻结相关代码默认为智能储蓄
		if (E_YES___.YES == isdfam) {
			onlnbl = onlnbl.subtract(DpFrozTools.getFrozBala(E_FROZOW.AUACCT, acctno));
		}
		// 可用余额如果小于0，则返回0
		if (CommUtil.compare(onlnbl, BigDecimal.ZERO) < 0) {
			onlnbl = BigDecimal.ZERO;
		}
		bizlog.parm("账户余额", onlnbl);
		return onlnbl;
	}

	/**
	 * 获取所有账户的账户余额、积数、贷款主账户内的利息总合、贷款主账户内的本金总合
	 * 
	 * @param custac
	 *            电子账户
	 * @param bal
	 *            余额
	 * @param cut
	 *            积数
	 * @param intest
	 *            贷款主账户内的利息总合
	 * @param intest
	 *            贷款主账户内的本金总合
	 */
	public static Map<String, BigDecimal> getAllAcctBal(String custac, BigDecimal bal, BigDecimal cut) {
		BigDecimal balance = BigDecimal.ZERO;// 账户余额
		BigDecimal cutmam = BigDecimal.ZERO;// 账户积数
		BigDecimal drawam = BigDecimal.ZERO;// 智能储蓄支取明细
		BigDecimal bigCurrInstam = BigDecimal.ZERO; // 活期积数计算利息
		BigDecimal bigRoundInstam = BigDecimal.ZERO; // 四舍五入后的计算利息
		BigDecimal bigCurrin = BigDecimal.ZERO; // 当前执行利率
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); // 交易日期
		String tmstmp = DateTools2.getCurrentTimestamp();// 时间戳
		// BigDecimal intest = BigDecimal.ZERO;// 利息、贴息、费用、罚息等利息（新增）
		// BigDecimal principal = BigDecimal.ZERO;// 正常本金、逾期本金、呆滞本金、呆账本金、核销本金等本金总合（新增）
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
		// List<kna_accs> accss = Kna_accsDao.selectAll_odb5(custac, true);
		List<IoCaKnaAccs> accss = caqry.listKnaAccsOdb5(custac, true);
		for (IoCaKnaAccs accs : accss) {
			if (E_PRODTP.DEPO == accs.getProdtp()) {
				// 存款
				if (E_FCFLAG.CURRENT == accs.getFcflag()) {
					// 查询活期账户
					KnaAcct acct = KnaAcctDao.selectOne_odb1(accs.getAcctno(), true);
					balance = balance.add(getAcctBalance(acct));
					// 查询分段积数
					List<KnbIndl> tblKnbIndls = KnbIndlDao.selectAll_odb4(accs.getAcctno(), E_INDLST.YOUX, false);
					for (KnbIndl tblKnbIndl : tblKnbIndls) {
						bizlog.debug("分段利率>>>>>>>>>>>>>>");
						// 积数大于0
						if (CommUtil.compare(tblKnbIndl.getAcmltn(), BigDecimal.ZERO) > 0) {
							// 计算分段积数计算利息 去除每段计算利息，汇总总积数后计算利息 modify by chenlk 20170216 beg
							// bigCurrInstam =
							// bigCurrInstam.add(pbpub.IntrPublic_calInstByCutmam(tblKnbIndl.getCuusin(),tblKnbIndl.getAcmltn()));
							// 积数相加
							cutmam = cutmam.add(tblKnbIndl.getAcmltn());
						}
					}
					// 查询积数
					KnbAcin acin = KnbAcinDao.selectOne_odb1(accs.getAcctno(), true);
					// 当前段实际积数
					BigDecimal bigRealCutmam = DpPublic.calRealTotalAmt(acin.getCutmam(), acct.getOnlnbl(), trandt,
							acin.getLaamdt());
					if (CommUtil.compare(bigRealCutmam, BigDecimal.ZERO) > 0) {
						// 积数相加
						cutmam = cutmam.add(bigRealCutmam);
					}
					// 查询利率信息
					KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(accs.getAcctno(), true);
					bigCurrin = tblKubInrt.getCuusin();
					// 计算总利息
					bigCurrInstam = bigCurrInstam.add(pbpub.countInteresRateByBase(tblKubInrt.getCuusin(), cutmam));
					// 去除每段计算利息，汇总总积数后计算利息 modify by chenlk 20170216 end
					bigRoundInstam = BusiTools.roundByCurrency(acct.getCrcycd(), bigCurrInstam, null);
					bizlog.debug("活期计算利息11111111111=" + bigCurrInstam);
					bizlog.debug("活期计算利息22222222222=" + bigRoundInstam);
					// 判断利息是否不做1分，不足则积数清零，并登记日志表
					if (CommUtil.compare(bigRoundInstam, new BigDecimal("0.01")) < 0
							&& CommUtil.compare(bigCurrInstam, BigDecimal.ZERO) > 0) {
						cutmam = BigDecimal.ZERO;
						String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
						// 插入日志表
						// 当期利率
						if (CommUtil.compare(acin.getCutmam(), BigDecimal.ZERO) > 0) {
							KnbAmtn tblKnbAmtn = SysUtil.getInstance(KnbAmtn.class);
							tblKnbAmtn.setTrandt(trandt);
							tblKnbAmtn.setTransq(transq);
							tblKnbAmtn.setAcctno(acin.getAcctno());
							tblKnbAmtn.setCutmam(acin.getCutmam());
							tblKnbAmtn.setCuusin(bigCurrin);
							KnbAmtnDao.insert(tblKnbAmtn);
						}
						bizlog.debug("插入>>>>>>>>>>>>>>>>>");
						// 分段利率
						DpAcctDao.insKnbIndlAcmltnByAcctno(acin.getAcctno(), trandt, transq, tmstmp);
						// 更新当前利率段积数
						acin.setCutmam(BigDecimal.ZERO); // 本期积数清零
						acin.setLaamdt(trandt); // 积数更新日期
						KnbAcinDao.updateOne_odb1(acin);
						// 更新分段利率积数
						DpAcctDao.updKnbIndlAcmltnByAcctno(accs.getAcctno(), tmstmp);
					}
				} else if (E_FCFLAG.FIX == accs.getFcflag()) {
					// 查询定期账户
					KnaFxac fxac = KnaFxacDao.selectOne_odb1(accs.getAcctno(), true);
					balance = balance.add(fxac.getOnlnbl());
					// 查询智能储蓄是否结息
					KnbAcin tblKnbAcin = KnbAcinDao.selectOne_odb1(accs.getAcctno(), true);
					// 计算定期当前计提利息
					drawam = drawam.add(BusiTools.roundByCurrency(fxac.getCrcycd(), tblKnbAcin.getPlanin(), null));
					// 查询定期支取明细是否结息完成，状态为正常的是还没有结息的
					List<KnaFxacDrdl> tblKnaFxacDrdls = KnaFxacDrdlDao.selectAll_odb3(accs.getAcctno(), false);
					// 传统定期处理
					if (CommUtil.isNotNull(tblKnaFxacDrdls)) {
						for (KnaFxacDrdl tblFxac_drdl : tblKnaFxacDrdls) {
							if (E_ACCTST.NORMAL != tblFxac_drdl.getTranst()) {
								continue;
							}
							drawam = drawam.add(tblFxac_drdl.getIntram());
						}
					}
				} else {
					throw DpModuleError.DpstAcct.BNAS1744(accs.getFcflag());
				}
			} else if (E_PRODTP.LOAN == accs.getProdtp() && accs.getAcctst() == E_DPACST.NORMAL) {
				BigDecimal intest = BigDecimal.ZERO;// 利息
				BigDecimal principal = BigDecimal.ZERO;// 本金
				// 贷款状态为正常
				DpInterestAndPrincipal dpInterestAndPrincipal = DpAcctQryDao.selPrincipalAndInterestByAcctno(
						accs.getAcctno(), cn.sunline.ltts.busi.sys.type.LnEnumType.E_ACCTST.NORMAL, false);
				if (CommUtil.isNull(dpInterestAndPrincipal)) {
					// 不做处理
				} else {
					intest = intest.add(dpInterestAndPrincipal.getIntest());// 利息赋值值
					principal = principal.add(dpInterestAndPrincipal.getPrincipal());// 本金赋值
				}
				if (CommUtil.compare(intest.add(principal), BigDecimal.ZERO) > 0) {
					throw DpModuleError.DpstComm.E9999("电子账号下存在未结清的贷款");
				}
				// 贷款
				// 查询正常本金、逾期本金、呆滞本金、呆账本金、核销本金等本金的总合
				// 查询利息、贴息、费用、罚息等利息的总合
				// DpInterestAndPrincipal dpInterestAndPrincipal = DpAcctQryDao
				// .selPrincipalAndInterestByAcctno(accs.getAcctno(),true);
				//
				// intest = intest.add(dpInterestAndPrincipal.getIntest());// 利息赋值值
				// principal = principal.add(dpInterestAndPrincipal.getPrincipal());// 本金赋值
				// throw DpModuleError.DpstProd.E0019();
			} else if (E_PRODTP.CARD == accs.getProdtp()) {
				// 信用卡
				throw DpModuleError.DpstProd.BNAS1711();
			} else if (E_PRODTP.FUND == accs.getProdtp()) {
				// 基金
				FundAcctOnlnbl cplFundAcctOnlnbl = DpAcctDao.selFdaFundOnlnblByAcctno(accs.getAcctno(), E_ACCTST.NORMAL,
						true);
				balance = balance.add(cplFundAcctOnlnbl.getOnlnbl());
				balance = balance.add(cplFundAcctOnlnbl.getFrozbl());
			} else if (E_PRODTP.INSU == accs.getProdtp()) {
				// 保险
				// 查询保险账号余额
				BigDecimal fdaOnlnbl = DpAcctDao.selDfaHoldOnlnblByAcctno(accs.getAcctno(), E_ACCTST.NORMAL, false);
				if (CommUtil.isNull(fdaOnlnbl)) {
					throw DpModuleError.DpstAcct.BNAS1400(accs.getAcctno());
				}
				balance = balance.add(fdaOnlnbl);
			} else if (E_PRODTP.NOTE == accs.getProdtp()) {
				// 票据
				throw DpModuleError.DpstProd.BNAS1711();
			} else if (E_PRODTP.FINA == accs.getProdtp()) {
				// 理财
				BigDecimal hdbkam = BigDecimal.ZERO;
				// Options<IoFnFnaAcct> fnlist =
				// SysUtil.getInstance(IoFnSevQryTableInfo.class).fna_acct_selectAll_odb2(custac);
				// for (IoFnFnaAcct fnacct : fnlist) {
				// if (fnacct.getAcctst() == BaseEnumType.E_DPACST.NORMAL) {
				// throw DpModuleError.DpstComm.BNAS0231();
				// }
				// }
				// 查询冻结登记簿
				Options<IoDpKnbFroz> lstKnbFroz = SysUtil.getInstance(IoDpFrozSvcType.class).qryKnbFroz(custac,
						E_FROZST.VALID);
				for (IoDpKnbFroz knbfroz : lstKnbFroz) {
					if (knbfroz.getFroztp() == E_FROZTP.FNFROZ) {
						if (knbfroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
							hdbkam = hdbkam.add(knbfroz.getFrozam());// 保留金额
						}
					}
				}
				if (CommUtil.compare(hdbkam, BigDecimal.ZERO) > 0) {
					throw DpModuleError.DpstComm.BNAS0997();
				}
			} else if (E_PRODTP.FNAC == accs.getProdtp()) {// 小马金融
				DpProdSvc.FnacInfo fnacinfo = DpAcctDao.selfnacOnlnblAndNdrcin(accs.getAcctno(), false);
				if (CommUtil.isNotNull(fnacinfo)) {
					balance = balance.add(fnacinfo.getOnlnbl());
				}
			} else {
				throw DpModuleError.DpstProd.BNAS1711();
			}
		}
		bal = bal.add(balance);
		cut = cut.add(cutmam);
		Map<String, BigDecimal> blncs = new HashMap<String, BigDecimal>();
		blncs.put("bal", bal);
		blncs.put("cut", cut);
		blncs.put("drawam", drawam);
		// blncs.put("principal",principal);//放置本金总合
		// blncs.put("intest",intest);//放置利息总合
		bizlog.debug("<<<<<<<<<<<<<<<<<<负债账户余额>>>>>>>>>>>>>>>>>>>：ballst=" + bal);
		return blncs;
	}

	/**
	 * 获取负债子户账户可用余额
	 * 
	 * @param 负债账号
	 * @param 定活标志
	 * @return 可用余额
	 */
	public static BigDecimal getAcctBal(String acctno, E_FCFLAG fcflag) {
		BigDecimal onlnbl = BigDecimal.ZERO;
		if (E_FCFLAG.CURRENT == fcflag) {
			KnaAcct acct = KnaAcctDao.selectOneWithLock_odb1(acctno, false);
			if (CommUtil.isNull(acct))
				throw DpModuleError.DpstAcct.BNAS1401(acctno);
			onlnbl = getAcctBalance(acct);
		} else if (E_FCFLAG.FIX == fcflag) {
			KnaFxac acct = KnaFxacDao.selectOneWithLock_odb1(acctno, false);
			if (CommUtil.isNull(acct))
				throw DpModuleError.DpstAcct.BNAS1402(acctno);
			onlnbl = acct.getOnlnbl();
		} else {
			throw DpModuleError.DpstAcct.BNAS0843(fcflag.toString());
		}
		return onlnbl;
	}

	/**
	 * 功能文件说明：检查电子账户销户输入账户要素是否符合规范
	 * 
	 * @author renjinghua
	 *         <p>
	 *         <li>2015年4月1日 下午20:40</li>
	 *         </p>
	 * 
	 * @param cplClsAcctIn
	 *            电子账户销户服务输入接口
	 * @return custac 电子账号
	 */
	public static String chkClsAcctInfo(ClsAcctIn cplClsAcctIn) {
		bizlog.debug("DpAcctProc.chkClsAcctIn  <<<<<<<<strat<<<<<<<<<<");
		String sCustac = null;
		String custac = cplClsAcctIn.getCustac();
		String cardno = cplClsAcctIn.getCardno();
		String custna = cplClsAcctIn.getCustna();
		String idtfno = cplClsAcctIn.getIdtfno();
		E_IDTFTP idtftp = cplClsAcctIn.getIdtftp();
		// 电子账户模块表查询服务
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		if (CommUtil.isNull(custna)) {
			throw CaError.Eacct.BNAS0876();
		}
		if (CommUtil.isNull(idtfno)) {
			throw CaError.Eacct.BNAS0875();
		}
		if (CommUtil.isNull(idtftp)) {
			throw CaError.Eacct.BNAS0874();
		}
		// 检验证件类型、证件号码
		BusiTools.chkCertnoInfo(idtftp, idtfno);
		// 证件类型
		// 客户模块表查询服务
		// 取消客户信息相关内容
		// IoSrvCfPerson cifPersonServ = SysUtil.getInstance(IoSrvCfPerson.class);
		// IoSrvCfPerson.IoGetCifCust.Output cfiCust =
		// SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
		// IoSrvCfPerson.IoGetCifCust.InputSetter cifCustCond =
		// SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
		// cifCustCond.setIdtfno(idtfno);
		// cifCustCond.setIdtftp(idtftp);
		// cifPersonServ.getCifCust(cifCustCond, cfiCust);
		// cif_cust tblCif_cust = Cif_custDao.selectOne_odb2(idtftp, idtfno, false);
		// if (CommUtil.isNull(cfiCust)) {
		// throw CaError.Eacct.BNAS1204();
		// }
		// //证件对应的客户信息与输入客户名称不一致
		// if (!CommUtil.equals(cfiCust.getCustna(), custna)) {
		// throw CaError.Eacct.BNAS0159(cfiCust.getCustna(), custna);
		// }
		// 卡号为空
		if (CommUtil.isNull(cardno)) {
			// 电子账号为空
			if (CommUtil.isNull(custac)) {
				throw CaError.Eacct.BNAS0658();
			}
			// 查询电子账号是否已存在，如果不存在，则返回错误
			// kna_cust tblKna_cust = Kna_custDao.selectOne_odb1(custac, false);
			IoCaKnaCust tblKna_cust = caqry.getKnaCustByCustacOdb1(custac, false);
			if (CommUtil.isNull(tblKna_cust)) {
				throw DpModuleError.DpstAcct.BNAS1126(custac);
			}
			// 卡号为空，则通过电子账号查询关联关系表获得对应卡号
			// kna_acdc tblkna_acdc = Kna_acdcDao.selectOne_odb1(custac, E_DPACST.NORMAL,
			// false);
			IoCaKnaAcdc tblkna_acdc = caqry.getKnaAcdcOdb1(custac, E_DPACST.NORMAL, false);
			// 卡账关联表状态不为正常，不允许销户
			if (CommUtil.isNull(tblkna_acdc)) {
				throw CaError.Eacct.BNAS1182(custac);
			}
			cplClsAcctIn.setCardno(tblkna_acdc.getCardno());
			cardno = tblkna_acdc.getCardno();
			sCustac = tblKna_cust.getCustac();
		} else {// 卡号不为空
				// 查询卡号是否已存在，如果不存在则返回错误
				// kcd_card tblKcd_card = Kcd_cardDao.selectOne_odb1(cardno, false);
				// IoCaKcdCard tblKcd_card = caqry.getKcdCardOdb1(cardno, false);
			IoCaDpbPswd tblDpbPswd = DpAcctQryDao.selDpbPswdbyAcctno(cardno, true);
			if (CommUtil.isNull(tblDpbPswd)) {
				throw DpModuleError.DpstAcct.BNAS1118(cardno);
			}
			// 电子账号不为空
			if (CommUtil.isNotNull(custac)) {
				// 如果卡号跟电子账号都不为空，则查询关联关系表，看是否有对应关系
				// kna_acdc tblKna_acdc = Kna_acdcDao.selectOne_odb2(cardno, true);
				IoCaKnaAcdc tblKna_acdc = caqry.getKnaAcdcOdb2(cardno, true);
				if (!CommUtil.equals(custac, tblKna_acdc.getCustac())) {
					throw CaError.Eacct.E0007(cardno, custac);
				}
			} else {
				// 电子账号为空，则通过卡号查询获取对应的电子账号
				// kna_acdc tblkna_acdc = Kna_acdcDao.selectOne_odb2(cardno, true);
				IoCaKnaAcdc tblkna_acdc = caqry.getKnaAcdcOdb2(cardno, true);
				custac = tblkna_acdc.getCustac();
				cplClsAcctIn.setCustac(custac);
			}
			sCustac = custac;
		}
		// kna_cust tblKna_cust = Kna_custDao.selectOne_odb1(custac, true);
		IoCaKnaCust tblKna_cust = caqry.getKnaCustByCustacOdb1(custac, true);
		// 输入客户名称是否与电子账号匹配
		// if (!CommUtil.equals(cfiCust.getCustno(), tblKna_cust.getCustno())) {
		// throw CaError.Eacct.BNAS1252(custac);
		// }
		// 如果电子账户状态已是关闭状态，则无需注销
		if (CommUtil.equals(tblKna_cust.getAcctst().getValue(), E_ACCTST.CLOSE.getValue())) {
			throw CaError.Eacct.BNAS1274(custac);
		}
		// 如果电子账户状态为挂失，则不允许销户
		if (CommUtil.equals(tblKna_cust.getAcctst().getValue(), E_ACCTST.SLEEP.getValue())) {
			throw CaError.Eacct.BNAS1275(custac);
		}
		// 如果卡号对应的电子账户状态已是关闭状态，则无需注销
		if (CommUtil.equals(tblKna_cust.getAcctst().getValue(), E_ACCTST.CLOSE.getValue())) {
			throw CaError.Eacct.BNAS1276(cardno);
		}
		// kcd_card tblKcd_card = Kcd_cardDao.selectOne_odb1(cardno, true);
		// IoCaKcdCard tblKcd_card = caqry.getKcdCardOdb1(cardno, true);
		IoCaDpbPswd tblDpbPswd = DpAcctQryDao.selDpbPswdbyAcctno(cardno, true);
		// 如果卡为关闭状态，不需销户
		if (CommUtil.equals(tblDpbPswd.getPsdwst().getValue(), E_PSDWST.CLOSE.getValue())) {
			throw CaError.Eacct.BNAS1277(cardno);
		}
		// 如果卡状态为挂失状态，则不允许销户
		if (!CommUtil.equals(tblDpbPswd.getPsdwst().getValue(), E_PSDWST.NORMAL.getValue())) {
			throw CaError.Eacct.BNAS1278(cardno);
		}
		// 产品类型为DEPO-存款
		// 活期
		List<KnaAcct> tblKnaAccts = KnaAcctDao.selectAll_odb4(custac, E_DPACST.SLEEP, false);
		if (CommUtil.isNotNull(tblKnaAccts) && tblKnaAccts.size() >= 1) {
			throw CaError.Eacct.BNAS0939(custac);
		}
		// 检查活期子账户中是否有冻结的账户，有冻结账户，不能销户
		chkCurrAcctnoIsFroz(custac);
		// 定期
		List<KnaFxac> tblKnaFxacs = KnaFxacDao.selectAll_odb4(custac, E_DPACST.SLEEP, false);
		if (CommUtil.isNotNull(tblKnaFxacs) && tblKnaFxacs.size() >= 1) {
			throw CaError.Eacct.BNAS0940(custac);
		}
		// 产品类型为FUND-基金
		List<String> fdAcctnos = DpAcctDao.selFdaFundByCustac(custac, E_ACCTST.SLEEP, false);
		if (CommUtil.isNotNull(fdAcctnos) && fdAcctnos.size() >= 1) {
			throw CaError.Eacct.BNAS0938(custac);
		}
		// 产品类型为INSU-保险
		// 保险账户状态为睡眠不能销户
		List<String> insuAcctnos = DpAcctDao.selDfaHoldByCustacAndAcctst(custac, E_ACCTST.SLEEP, false);
		if (CommUtil.isNotNull(insuAcctnos) && insuAcctnos.size() >= 1) {
			throw CaError.Eacct.BNAS0941(custac);
		}
		// 保险账户下有保单不为1-核保成功(待支付)、5-退保成功、6-已冲正、7-退保失败状态的，不能销户
		List<String> insuDetlAcctnos = DpAcctDao.selDfaHoldDetlByCustacForNorm(custac, false);
		if (CommUtil.isNotNull(insuDetlAcctnos) && insuDetlAcctnos.size() >= 1) {
			throw CaError.Eacct.BNAS0942(custac);
		}
		// 产品类型为其他的 TODO
		cplClsAcctIn.setCustac(custac);
		cplClsAcctIn.setCardno(cardno);
		return sCustac;
	}

	/**
	 * 功能文件说明：电子账户销户处理，更新状态
	 * 
	 * @author renjinghua
	 *         <p>
	 *         <li>2015年4月2日上午 10:00</li>
	 *         <li>更新电子账户、卡、负债子账户的状态及关联关系等的状态</>
	 *         </p>
	 *         1.销户所有负债子账户 2.销户卡及电子账户 3.销户关联关系
	 * @param cplClsAcctIn
	 *            电子账户销户服务输入接口
	 */
	public static void prcAcctst(ClsAcctIn cplClsAcctIn) {
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();// 法人代码
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易时间
		String tmstmp = DateTools2.getCurrentTimestamp();// 时间戳
		String servtp = CommTools.getBaseRunEnvs().getChannel_id();// 交易渠道
		// 冲正注册
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
		// 获得电子账号、卡号
		String custac = cplClsAcctIn.getCustac();
		String cardno = cplClsAcctIn.getCardno();
		String closdt = CommTools.getBaseRunEnvs().getTrxn_date();
		String clossq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		/*
		 * if(CommUtil.isNotNull(cplClsAcctIn.getClossq())){ clossq =
		 * cplClsAcctIn.getClossq(); }
		 */
		// 1.销户所有负债子账户
		DpAcctDao.updKnaAcctAcctstByCustac(custac, E_DPACST.CLOSE, closdt, clossq, tmstmp); // 活期
		DpAcctDao.updKnaFxacAcctstByCustac(custac, E_DPACST.CLOSE, closdt, clossq, tmstmp); // 定期
		/*
		 * //其他子户，暂时实现基金、保险 其他暂未实现 TODO DpAcctDao.updFdaFundAcctstByCustac(custac,
		 * E_ACCTST.CLOSE); //基金 DpAcctDao.updDfaHoldAcctstByCustac(custac,
		 * E_ACCTST.CLOSE, E_PRODTP.INSU); //保险
		 */
		// 电子账户模块表查询服务
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		// 2.销户卡、电子账户
		// 电子账户
		// kna_cust tblKna_cust = Kna_custDao.selectOne_odb1(custac, true);
		IoCaKnaCust tblKna_cust = caqry.getKnaCustByCustacOdb1(custac, true);
		cplInput.setEvent1(tblKna_cust.getAcctst().getValue());
		tblKna_cust.setAcctst(E_ACCTST.CLOSE);
		tblKna_cust.setClosdt(CommTools.getBaseRunEnvs().getTrxn_date());
		tblKna_cust.setClossq(clossq);
		tblKna_cust.setClossv(CommTools.getBaseRunEnvs().getChannel_id());
		tblKna_cust.setClossv(servtp);
		// Kna_custDao.updateOne_odb1(tblKna_cust);
		caqry.updateKnaCustOdb1(tblKna_cust);
		// 卡
		// kcd_card tblKcd_card = Kcd_cardDao.selectOne_odb1(cardno, true);
		// modify by songkl 20170203
		// 客户端销户允许跨机构操作
		// IoCaKcdCard tblKcd_card = caqry.kcd_card_selectOne_odb1(cardno, true);
		/*
		 * IoCaKcdCard tblKcd_card = DpAcctQryDao.selkcdcardbycardno(cardno, true);
		 * tblKcd_card.setDcmtst(E_DCMTST.CLOSED);
		 * //Kcd_cardDao.updateOne_odb1(tblKcd_card);
		 * caqry.updateKcdCardOdb1(tblKcd_card);
		 */
		IoCaDpbPswd tblDpbPswd = DpAcctQryDao.selDpbPswdbyAcctno(cardno, true);
		tblDpbPswd.setPsdwst(E_PSDWST.CLOSE);
		DpAcctQryDao.updDpbPswdbyAcctno(tblDpbPswd.getAcctno(), tblDpbPswd.getPsdwst());
		// 3.销户关联关系
		// 卡帐关联关系
		DpAcctDao.updKnaAcdcAcctstByCustac(custac, E_DPACST.CLOSE, tmstmp);
		// 登记绑定账户修改登记簿
		List<IoCaKnaCacd> cplKnaCacd = DpAcctDao.selKnaCacdInfoByCustac(custac, E_DPACST.NORMAL, false);
		for (IoCaKnaCacd tbkKnaCacd : cplKnaCacd) {
			AccChngbrDao.insKnbcacqInfo(corpno, custac, tbkKnaCacd.getCardno(), null, trandt, servtp, E_MANTWY.REMOVE,
					clossq, tmstmp);
		}
		// 外卡与电子账户绑定
		DpAcctDao.updKnaCacdAcctstByCustac(custac, E_DPACST.CLOSE, tmstmp);
		// 电子账号与子账户关联关系
		DpAcctDao.updKnaAccsAcctstByCustac(custac, E_DPACST.CLOSE, tmstmp);
		// 更新电子账户账户别名，绑定手机号
		DpAcctDao.updKnaAcalStatusByCustac(E_ACALST.INVALID, custac, tmstmp);
		// 更新客户信息关联状态
		int count = AccChngbrDao.selKnareltiInfo(tblKna_cust.getCustno(), false);
		if (count == 0) {
			DpAcctDao.updCifCustAccsStatusByCustac(E_STATUS.CLOSE, tblKna_cust.getCustno());
		}
		// 更新升级登记簿
		// DpAcctDao.updknbpromByCustac(custac, E_PROMST.CANCEL);
		cplInput.setEvent2(clossq);
		// add 20170314 songlw 登记销户原流水
		cplInput.setEvent3(cplClsAcctIn.getClossq());
		// 冲正注册
		cplInput.setCustac(custac);
		cplInput.setTranac(cardno);
		cplInput.setTranev(ApUtil.TRANS_EVENT_DPCLOS);
		// ApStrike.regBook(cplInput);
		IoMsRegEvent input = SysUtil.getInstance(IoMsRegEvent.class);
		input.setReversal_event_id(ApUtil.TRANS_EVENT_DPCLOS);
		input.setInformation_value(SysUtil.serialize(cplInput));
		MsEvent.register(input, true);
	}

	/**
	 * 检查活期账户是否处于冻结状态
	 * 
	 * @param custac
	 *            电子账户
	 */
	public static void chkCurrAcctnoIsFroz(String custac) {
		// List<KnaAcct> tblKnaAccts = KnaAcctDao.selectAll_odb4(custac,
		// E_DPACST.NORMAL, false);
		//
		// for(KnaAcct tblKnaAcct : tblKnaAccts){
		// String acctno = tblKnaAcct.getAcctno();
		// if(E_YES___.YES == tblKnaAcct.getFralfg()){
		// throw CaError.Eacct.E0001("电子账号"+custac+"下活期账户"+acctno+"处于封闭冻结状态，不允许销户");
		// }
		// if(E_YES___.YES == tblKnaAcct.getFrinfg()){
		// throw CaError.Eacct.E0001("电子账号"+custac+"下活期账户"+acctno+"处于只付不收状态，不允许销户");
		// }
		// if(E_YES___.YES == tblKnaAcct.getFrotfg()){
		// throw CaError.Eacct.E0001("电子账号"+custac+"下活期账户"+acctno+"处于只收不付状态，不允许销户");
		// }
		// }
		// 检查智能储蓄冻结
		if (DpFrozTools.isFroz(E_FROZOW.AUACCT, custac, E_YES___.YES, E_YES___.YES, E_YES___.YES)) {
			throw CaError.Eacct.BNAS0937(custac);
		}
		// TODO 暂无其他冻结
	}

	/**
	 * 处理定期明细
	 * 
	 * @param acctno
	 *            账号
	 * @param depttm
	 *            账户存期
	 * @param tranam
	 *            交易金额
	 */
	public static void prcFxacDetl(String acctno, DpAcctOnlnblEntity onlnblEntity, BigDecimal tranam) {
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String mntrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		E_TERMCD depttm = onlnblEntity.getTermcd();
		KnbAcin tblKnbAcin = KnbAcinDao.selectOne_odb1(acctno, false);
		if (CommUtil.isNull(tblKnbAcin)) {
			throw DpModuleError.DpstAcct.BNAS0710();
		}
		onlnblEntity.setDetlfg(tblKnbAcin.getDetlfg());
		// 计数金额
		BigDecimal countAmt = tranam;
		if (E_INBEFG.INBE == tblKnbAcin.getInbefg()) {// 计息标志为计息
			if (E_YES___.YES == onlnblEntity.getDetlfg()) {
				List<KnaFxacDetl> detls = new ArrayList<>();
				// mdy by zhanga -- 明细序号不为空的分支在到期处理acmatu批量交易中使用 --
				if (onlnblEntity.getSigle() == E_YES___.YES) {
					KnaFxacDetl fxac_detl = KnaFxacDetlDao.selectOne_odb1(acctno, onlnblEntity.getDetlsq(), true);
					detls.add(fxac_detl);
				} else {
					KnaFxdr fxdr = KnaFxdrDao.selectOne_odb1(acctno, false);
					if (CommUtil.isNull(fxdr)) {
						throw DpModuleError.DpstAcct.BNAS1403();
					}
					// 支取规则
					if (E_DRRULE.HJXC == fxdr.getDrrule()) {
						// 后进先出
						detls = DpAcctDao.selKnaFxacDetlByAcctDesc(acctno, true);
					} else if (E_DRRULE.XJXC == fxdr.getDrrule()) {
						// 先进先出
						detls = DpAcctDao.selKnaFxacDetlByAcctEsc(acctno, true);
					} else {
						throw DpModuleError.DpstAcct.BNAS1404(fxdr.getDrrule().toString());
					}
				}
				for (KnaFxacDetl detl : detls) {
					// 当前账户余额
					BigDecimal onlnbl = detl.getOnlnbl();
					// 支取金额
					BigDecimal drawam = BigDecimal.ZERO;
					if (CommUtil.compare(countAmt, onlnbl) >= 0) {
						// 减少计数金额，账户余额归零，账户销户
						drawam = onlnbl;
						countAmt = countAmt.subtract(onlnbl);
						detl.setAcctst(E_DPACST.CLOSE);
						detl.setOnlnbl(BigDecimal.ZERO);
						detl.setClosdt(trandt);
						detl.setClossq(mntrsq);
						KnaFxacDetlDao.updateOne_odb1(detl);
					} else {
						// 计数金额归零，账户余额支取部分
						drawam = countAmt;
						onlnbl = onlnbl.subtract(countAmt);
						countAmt = BigDecimal.ZERO;
						detl.setOnlnbl(onlnbl);
						KnaFxacDetlDao.updateOne_odb1(detl);
					}
					// 登记定期明细支取表
					KnaFxacDrdl drdl = SysUtil.getInstance(KnaFxacDrdl.class);
					drdl.setTrandt(trandt);
					drdl.setTransq(mntrsq);
					drdl.setAcctno(acctno);
					drdl.setDetlsq(detl.getDetlsq());
					drdl.setBgindt(detl.getBgindt());
					drdl.setCrcycd(detl.getCrcycd());
					drdl.setCsextg(detl.getCsextg());
					drdl.setEdindt(trandt);
					drdl.setDrawam(drawam);
					/*
					 * mdy by zhanga 查账户利率表有点多余 直接取账户利率信息表 KubInrt inrt =
					 * KubInrtDao.selectOne_odb1(acctno, false); if(CommUtil.isNull(inrt)) throw
					 * DeptAcct.E9999("账户利率信息为空");
					 */
					// IntrPublicEntity entity = new IntrPublicEntity();
					IoPbIntrPublicEntity entity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
					entity.setCrcycd(detl.getCrcycd());
					// entity.setIntrcd(inrt.getIntrcd());
					entity.setIntrcd(tblKnbAcin.getIntrcd());
					// entity.setIncdtp(inrt.getIncdtp());
					entity.setIncdtp(tblKnbAcin.getIncdtp());
					entity.setTrandt(trandt);
					// entity.setIntrwy(inrt.getIntrwy());
					entity.setIntrwy(tblKnbAcin.getIntrwy());
					entity.setBgindt(detl.getBgindt());
					entity.setEdindt(trandt);
					entity.setDepttm(depttm);
					entity.setTranam(drawam);
					entity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
					entity.setInbebs(tblKnbAcin.getTxbebs()); // 计息基础
					entity.setCorpno(tblKnbAcin.getCorpno());
					entity.setBrchno(onlnblEntity.getOpbrch());
					entity.setLevety(tblKnbAcin.getLevety()); // 靠档类型
					if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
						entity.setTrandt(detl.getOpendt()); // 开户日期
						entity.setTrantm("999999");
					}
					IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);

					/**
					 * add by xj 20180926 柳行定活宝产品 金额靠档 使用定活宝定期账户合计余额，不使用交易金额或子账户余额
					 */
					// 获取定活宝产品号
					KnpParameter dhbPara = KnpParameterDao.selectOne_odb1("DpParm.dppb", "dppb_dhb", "%", "%", false);
					if (CommUtil.isNotNull(dhbPara.getParm_value1())
							&& CommUtil.compare(dhbPara.getParm_value1(), tblKnbAcin.getProdcd()) == 0) {
						// 查询定活宝账户余额
						KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(tblKnbAcin.getAcctno(), false);
						if (CommUtil.isNotNull(tblKnaFxac)
								&& CommUtil.compare(tblKnaFxac.getOnlnbl(), BigDecimal.ZERO) > 0) {
							// 使用定活宝定期负债账号余额进行金额靠档
							entity.setTranam(tblKnaFxac.getOnlnbl());
						}
					}
					/** end */

					pbpub.countInteresRate(entity);
					KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acctno, true);
					BigDecimal cuusin = entity.getIntrvl();
					// 利率优惠后执行利率
					cuusin = cuusin.add(cuusin.multiply(
							CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).divide(BigDecimal.valueOf(100))));
					// mod by leipeng 优惠后判断利率是否超出基础浮动范围20170220 start--
					// 利率的最大范围值
					BigDecimal intrvlmax = entity.getBaseir()
							.multiply(BigDecimal.ONE.add(entity.getFlmxsc().divide(BigDecimal.valueOf(100))));
					// 利率的最小范围值
					BigDecimal intrvlmin = entity.getBaseir()
							.multiply(BigDecimal.ONE.add(entity.getFlmnsc().divide(BigDecimal.valueOf(100))));
					if (CommUtil.compare(cuusin, intrvlmin) < 0) {
						cuusin = intrvlmin;
					} else if (CommUtil.compare(cuusin, intrvlmax) > 0) {
						cuusin = intrvlmax;
					}
					// mod by leipeng 优惠后判断时候超出基础浮动范围20170220 end--
					// 计算利息
					BigDecimal interest = pbpub.countInteresRateByAmounts(cuusin, detl.getBgindt(), trandt, drawam,
							tblKnbAcin.getTxbebs());
					BigDecimal intxam = BigDecimal.ZERO;
					// 是否启用利息税
					if (tblKnbAcin.getTxbefg() == E_YES___.YES) {
						// 查询分段积数
						CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
						calInterTax.setAcctno(acctno);
						calInterTax.setTranam(drawam);
						calInterTax.setBegndt(detl.getBgindt());
						calInterTax.setEnddat(trandt);
						calInterTax.setCuusin(cuusin);
						calInterTax.setInstam(interest);
						calInterTax.setInbebs(tblKnbAcin.getTxbebs());
						InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(tblKnbAcin, calInterTax,
								true);
						intxam = interestAndTax.getIntxam();
					}
					interest = BusiTools.roundByCurrency(detl.getCrcycd(), interest, null);
					// 计算支取部分金额的利息，计提和结息时不用再计算
					drdl.setIntram(interest);
					// 获取执行利率
					drdl.setCuusin(cuusin);
					drdl.setIntxam(intxam);
					// mdy by zhanga -- 明细序号不为空的分支在到期处理acmatu批量交易中使用 --
					if (onlnblEntity.getSigle() == E_YES___.YES) {
						drdl.setTranst(E_ACCTST.CLOSE);
						onlnblEntity.setInterest(interest); // 回传利息
					} else {
						drdl.setTranst(E_ACCTST.NORMAL);
					}
					if (CommUtil.isNull(onlnblEntity.getRemark())) {
						onlnblEntity.setRemark("");
					}
					drdl.setRemark(onlnblEntity.getRemark());
					KnaFxacDrdlDao.insert(drdl);
					if (CommUtil.compare(countAmt, BigDecimal.ZERO) <= 0) {
						// 计数金额归零，跳出循环
						break;
					}
				}
			}
		}
	}

	/**
	 * 
	 * @Author renjinghua
	 *         <p>
	 *         <li>2015年8月27日-下午5:36:54</li>
	 *         <li>功能说明：支取时计算利息</li>
	 *         <li>处理传统定期支取部分结息</li>
	 *         <li>提前支取使用支取时的活期利率计算，到期后支取逾期部分按支取时的活期利率计算，未支取部分按开户定期利率计算</li>
	 *         </p>
	 * @param entity
	 *            支取计算对象
	 * @param fcflag
	 *            定活标志
	 * 
	 * 
	 * @author cuijia
	 * @datetime 20170913 代码重构并实现drdein（支取是否扣除已付利息）属性实现
	 */
	public static void prcDrawCalcin(DpSaveEntity entity, E_FCFLAG fcflag) {
		bizlog.debug("支取计算利息开始>>>>>>>>>>>>>>>>>>>>>>>>>");
		String acctno = entity.getAcctno();
		BigDecimal tranam = entity.getTranam();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String drintpName = null;
		String instdt = null; // 计息起始日期
		String intrcd = null; // 利率编号
		E_IRCDTP ircdtp = null; // 利率代码类型
		E_IRDPWY intrwy = null; // 利率靠档方式
		E_INDLTP lsinoc = E_INDLTP.PYIN; // 上次利率操作代码，默认支付利息
		BigDecimal acmltn = BigDecimal.ZERO;
		E_INBEBS txbebs = E_INBEBS.STADSTAD; // 计息基础
		BigDecimal acbsin = BigDecimal.ZERO; // 账户基准利率
		BigDecimal cuusin = BigDecimal.ZERO; // 当前执行利率
		String remark = null; // 靠档天数
		IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
		KnbAcin tblKnbAcin = KnbAcinDao.selectOneWithLock_odb1(acctno, true);
		// 计息的非智能存款的定期产品，计算支取时利息
		if (E_INBEFG.INBE == tblKnbAcin.getInbefg() && E_FCFLAG.FIX == fcflag && E_YES___.NO == entity.getDetlfg()) {
			BigDecimal bigInstam = BigDecimal.ZERO;
			BigDecimal intxam = BigDecimal.ZERO; // 利息税
			KnbDfir tblKnbDfir = null;
			// 检查支取控制表
			KnaFxdr tblKnaFxdr = KnaFxdrDao.selectOne_odb1(acctno, false);
			E_YES___ dpbkfg = CommUtil.isNotNull(tblKnaFxdr) ? tblKnaFxdr.getDpbkfg() : E_YES___.NO;
			// 查询定期账户表
			KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(acctno, true);
			// 特殊处理通知存款违约支取
			if (tblKnaFxac.getDebttp() == E_DEBTTP.DP2506) {
				// 通知存款计算起息日与交易日天数差
				int days = DateTools2.calDays(tblKnaFxac.getBgindt(), trandt, 0, 0);
				// 默认到期日为当前交易日，如果交易日小于（不足）通知存款天数按照通知天数计算为到期日
				String matudt = trandt;
				if (E_TERMCD.T107 == tblKnaFxac.getDepttm()) {
					if (CommUtil.compare(days - 7, 0) < 0) {
						matudt = DateTimeUtil.dateAdd("day", tblKnaFxac.getBgindt(), 7);
					}
				} else if (E_TERMCD.T101 == tblKnaFxac.getDepttm()) {
					if (CommUtil.compare(days - 1, 0) < 0) {
						matudt = DateTimeUtil.dateAdd("day", tblKnaFxac.getBgindt(), 1);
					}
				} else {
					throw DpModuleError.DpstProd.BNAS1772(tblKnaFxac.getProdcd(), tblKnaFxac.getDepttm());
				}
				tblKnaFxac.setMatudt(matudt);
			}
			bizlog.debug("定期账户余额>>>>>>>>>>>>>>>" + tblKnaFxac.getOnlnbl() + "交易金额" + tranam);
			// ================处理扣划结息==================
			if (entity.getIsdedu() == E_YES___.YES) { // 扣划标志
				BigDecimal drmiam = BigDecimal.ZERO; // 最小支取金额
				// 获取最小支取金额
				if (tblKnaFxdr.getDrawtp() == E_DRAWCT.COND && tblKnaFxdr.getCtrlwy() != E_CTRLWY.TMCL
						&& tblKnaFxdr.getDramwy() != E_AMNTWY.MXAC) {
					drmiam = tblKnaFxdr.getDrmiam();
				}
				if (tblKnaFxdr.getDrawtp() == E_DRAWCT.COND && tblKnaFxdr.getCtrlwy() != E_CTRLWY.AMCL
						&& CommUtil.compare(tblKnaFxdr.getDrmxtm(), 0L) == 0) {
					// drmiam = tblKnaFxac.getOnlnbl().add(tranam);
					// 因为计算支取利息代码逻辑提前，所以余额不用做处理
					drmiam = tblKnaFxac.getOnlnbl();
				}
				// 扣划金额小于最小支取金额
				if (CommUtil.compare(tranam, drmiam) < 0) {
					tblKnbDfir = KnbDfirDao.selectOne_odb1(acctno, E_TEARTP.BTMN, false);
				}
				// 提前扣划剩余不足最低留存金额
				if (CommUtil.compare(tblKnaFxac.getOnlnbl().subtract(tranam), tblKnaFxac.getHdmimy()) < 0) {
					tblKnbDfir = KnbDfirDao.selectOne_odb1(acctno, E_TEARTP.ZDLC, false);
				}
			}
			if (CommUtil.isNotNull(tblKnbDfir)) {
				// 修改起息日期获取方式 modify by songkl 2017/07/05 满足丰收瑞丽产品需求
				// 若账户违约支取表中起息日来源为 起息日，则取定期表中起息日期
				if (tblKnbDfir.getBsindt() == E_BSINDT.QXR) {
					instdt = tblKnaFxac.getBgindt(); // 计息起始日期
				} else if (tblKnbDfir.getBsindt() == E_BSINDT.SCFX) {
					// 若账户违约表中起息日来源为上次付息日，则取计提表中上次结息日期
					// 若计提表中上次付息日期为空则取定期表中起息日期
					if (CommUtil.isNotNull(tblKnbAcin.getLcindt())) {
						instdt = tblKnbAcin.getLcindt();// 计息起始日期
					} else {
						instdt = tblKnaFxac.getBgindt(); // 计息起始日期
					}
					// 若结息日当天支取则修改计息日为当天日期（暂时这样修改 未确定）
					if (CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_date(), tblKnbAcin.getNcindt())) {
						instdt = CommTools.getBaseRunEnvs().getTrxn_date();
					}
				} else {
					throw DpModuleError.DpstAcct.BNAS0004();
				}
				// end
				IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
				intrEntity.setCrcycd(tblKnaFxac.getCrcycd()); // 币种
				intrEntity.setIntrcd(tblKnbDfir.getIntrcd()); // 利率代码
				intrEntity.setIntrwy(tblKnbDfir.getIntrwy()); // 靠档方式
				intrEntity.setIncdtp(tblKnbDfir.getIncdtp()); // 利率代码类型
				intrEntity.setDepttm(E_TERMCD.T000);// 存期
				intrEntity.setTrandt(trandt);
				// 2017/07/05 songkl 修改起息日期
				intrEntity.setBgindt(instdt); // 起始日期
				// intrEntity.setBgindt(tblKnaFxac.getBgindt()); //起始日期
				intrEntity.setEdindt(trandt); // 结束日期
				intrEntity.setTranam(tranam); // 交易金额
				intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
				intrEntity.setInbebs(tblKnbDfir.getBsinrl()); // 计息基础
				intrEntity.setCorpno(tblKnaFxac.getCorpno());// 法人代码
				intrEntity.setBrchno(tblKnaFxac.getBrchno());// 机构
				intrEntity.setLevety(tblKnbDfir.getLevety());
				if (tblKnbDfir.getIntrdt() == E_INTRDT.OPEN) {
					intrEntity.setTrandt(tblKnaFxac.getOpendt());
					intrEntity.setTrantm("999999");
				}
				pbpub.countInteresRate(intrEntity);
				bigInstam = intrEntity.getInamnt();// 利息
				// 设置付息信息参数
				bizlog.debug("存储天数=============" + DateTools2.calDays(tblKnaFxac.getBgindt(), trandt, 1, 0) + "，利率====="
						+ intrEntity.getIntrvl());
				bizlog.debug("扣划利息>>>>>>>>>>>>>>>>>" + bigInstam);
				// instdt = tblKnaFxac.getBgindt(); //计息起始日期
				acbsin = intrEntity.getBaseir(); // 账户基准利率
				cuusin = intrEntity.getIntrvl(); // 当前执行利率
				intrcd = tblKnbDfir.getIntrcd(); // 利率编号
				ircdtp = tblKnbDfir.getIncdtp(); // 利率代码类型
				intrwy = tblKnbDfir.getIntrwy(); // 利率靠档方式
				CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
				calInterTax.setAcctno(acctno);
				calInterTax.setTranam(tranam);
				calInterTax.setBegndt(instdt);
				calInterTax.setEnddat(trandt);
				calInterTax.setCuusin(cuusin);
				calInterTax.setInstam(bigInstam);
				calInterTax.setInbebs(tblKnbDfir.getBsinrl());
				InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(tblKnbAcin, calInterTax, true);
				intxam = interestAndTax.getIntxam();
				// ================扣划结息处理结束==================
			} else {
				// 定活两便储蓄存款计算利息
				if (tblKnaFxac.getDebttp() == E_DEBTTP.DP2505) {
					KubInrt tblKubinrt = KubInrtDao.selectOne_odb1(acctno, true);
					// 计算利息，使用行内基准的活期利率
					// IntrPublicEntity intrEntity = new IntrPublicEntity();
					IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
					intrEntity.setCrcycd(tblKnaFxac.getCrcycd()); // 币种
					intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); // 利率代码
					// 如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
					intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); // 利率代码类型
					intrEntity.setDepttm(E_TERMCD.T000);// 存期
					intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); // 靠档方式
					intrEntity.setTrandt(trandt);
					intrEntity.setTranam(tranam); // 交易金额
					intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
					intrEntity.setInbebs(tblKnbAcin.getTxbebs()); // 计息基础
					intrEntity.setCorpno(tblKnaFxac.getCorpno());// 法人代码
					intrEntity.setBrchno(tblKnaFxac.getBrchno());// 机构
					intrEntity.setLevety(tblKnbAcin.getLevety());
					if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
						intrEntity.setTrandt(tblKnaFxac.getOpendt());
						intrEntity.setTrantm("999999");
					}
					// 定活两便执行利率小于活期利率时取活期利率 add liaojc 20170220
					// 取活期执行利率
					intrEntity.setBgindt(trandt); // 起始日期
					intrEntity.setEdindt(trandt); // 结束日期
					pbpub.countInteresRate(intrEntity);
					BigDecimal accsin = intrEntity.getIntrvl();// 活期执行利率
					// 利率优惠后执行利率
					accsin = accsin.add(accsin.multiply(
							CommUtil.nvl(tblKubinrt.getFavort(), BigDecimal.ZERO).divide(BigDecimal.valueOf(100))));
					// mod by leipeng 优惠后判断利率是否超出基础浮动范围20170220 start--
					// 利率的最大范围值
					BigDecimal intrvlmax = intrEntity.getBaseir()
							.multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
					// 利率的最小范围值
					BigDecimal intrvlmin = intrEntity.getBaseir()
							.multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
					if (CommUtil.compare(accsin, intrvlmin) < 0) {
						accsin = intrvlmin;
					} else if (CommUtil.compare(accsin, intrvlmax) > 0) {
						accsin = intrvlmax;
					}
					// 获取当前日期执行利率
					intrEntity.setBgindt(tblKnaFxac.getBgindt()); // 起始日期
					intrEntity.setEdindt(trandt); // 结束日期
					pbpub.countInteresRate(intrEntity);
					BigDecimal actsin = intrEntity.getIntrvl();// 实际执行利率
					// 利率优惠后执行利率
					actsin = actsin.add(actsin.multiply(
							CommUtil.nvl(tblKubinrt.getFavort(), BigDecimal.ZERO).divide(BigDecimal.valueOf(100))));
					// mod by leipeng 优惠后判断利率是否超出基础浮动范围20170220 start--
					// 利率的最大范围值
					intrvlmax = intrEntity.getBaseir()
							.multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
					// 利率的最小范围值
					intrvlmin = intrEntity.getBaseir()
							.multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
					if (CommUtil.compare(actsin, intrvlmin) < 0) {
						actsin = intrvlmin;
					} else if (CommUtil.compare(actsin, intrvlmax) > 0) {
						actsin = intrvlmax;
					}
					if (CommUtil.compare(accsin, actsin) < 0) {
						accsin = actsin;// 执行利率
					}
					// mod by leipeng 优惠后判断时候超出基础浮动范围20170220 end--
					// 计算利息
					bigInstam = pbpub.countInteresRateByAmounts(accsin, tblKnaFxac.getBgindt(), trandt, tranam,
							tblKnbAcin.getTxbebs());
					instdt = tblKnaFxac.getBgindt(); // 计息起始日期
					acbsin = intrEntity.getBaseir(); // 账户基准利率
					cuusin = accsin; // 当前执行利率
					intrcd = tblKnbAcin.getIntrcd(); // 利率编号
					ircdtp = tblKnbAcin.getIncdtp(); // 利率代码类型
					intrwy = tblKnbAcin.getIntrwy(); // 利率靠档方式
					CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
					calInterTax.setAcctno(acctno);
					calInterTax.setTranam(tranam);
					calInterTax.setBegndt(tblKnaFxac.getBgindt());
					calInterTax.setEnddat(trandt);
					calInterTax.setCuusin(cuusin);
					calInterTax.setInstam(bigInstam);
					calInterTax.setInbebs(tblKnbAcin.getTxbebs());
					InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(tblKnbAcin, calInterTax,
							true);
					intxam = interestAndTax.getIntxam();
				} else {
					// 违约支取,按活期结息
					if (dpbkfg == E_YES___.YES) {
						KubInrt tblKubinrt = KubInrtDao.selectOne_odb1(acctno, true);
						/* 按活期利率计算 */
						tblKnbDfir = KnbDfirDao.selectOne_odb1(acctno, E_TEARTP.TQZQ, false);
						drintpName = E_TEARTP.TQZQ.getLongName();
						// 检查违约支取利息定义信息
						if (CommUtil.isNull(tblKnbDfir)) {
							throw DpModuleError.DpstAcct.BNAS1209(tblKnaFxac.getProdcd(), drintpName);
						}
						// 起息日期获取方式修改 2017/07/05 modify by songkl
						// 若账户违约支取表中起息日来源为 起息日，则取定期表中起息日期
						if (tblKnbDfir.getBsindt() == E_BSINDT.QXR) {
							instdt = tblKnaFxac.getBgindt(); // 计息起始日期
						} else if (tblKnbDfir.getBsindt() == E_BSINDT.SCFX) {
							// 若账户违约表中起息日来源为上次付息日，则取计提表中上次结息日期
							// 若计提表中上次付息日期为空则取定期表中起息日期
							if (CommUtil.isNotNull(tblKnbAcin.getLcindt())) {
								instdt = tblKnbAcin.getLcindt();// 计息起始日期
							} else {
								instdt = tblKnaFxac.getBgindt(); // 计息起始日期
							}
							// 若结息日当天支取则修改计息日为当天日期（暂时这样修改 未确定）
							if (CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_date(), tblKnbAcin.getNcindt())) {
								instdt = CommTools.getBaseRunEnvs().getTrxn_date();
							}
						} else {
							throw DpModuleError.DpstAcct.BNAS0004();
						}
						// 计算利息，使用行内基准的活期利率
						IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
						intrEntity.setCrcycd(tblKnaFxac.getCrcycd()); // 币种
						intrEntity.setIntrcd(tblKnbDfir.getIntrcd()); // 利率代码
						if (E_IRCDTP.BASE == tblKnbDfir.getIncdtp() || E_IRCDTP.Reference == tblKnbDfir.getIncdtp()) {
							intrEntity.setDepttm(E_TERMCD.T000);
						} else if (E_IRCDTP.LAYER == tblKnbDfir.getIncdtp()) {
							if (tblKnbDfir.getInclfg() == E_YES___.YES) {
								intrEntity.setIntrwy(tblKnbDfir.getIntrwy()); // 靠档方式
							}
						}
						intrEntity.setIncdtp(tblKnbDfir.getIncdtp()); // 利率代码类型
						intrEntity.setTrandt(trandt);
						// 修改起息日期 2017/07/05 songkl
						intrEntity.setBgindt(instdt); // 起始日期
						// intrEntity.setBgindt(tblKnaFxac.getMatudt()); //起始日期
						intrEntity.setEdindt(trandt); // 结束日期
						intrEntity.setTranam(tranam); // 交易金额
						intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
						intrEntity.setInbebs(tblKnbDfir.getBsinrl()); // 计息基础
						intrEntity.setCorpno(tblKnaFxac.getCorpno());// 法人代码
						intrEntity.setBrchno(tblKnaFxac.getBrchno());// 机构
						intrEntity.setLevety(tblKnbDfir.getLevety());
						if (tblKnbDfir.getIntrdt() == E_INTRDT.OPEN) {
							intrEntity.setTrandt(tblKnaFxac.getOpendt());
							intrEntity.setTrantm("999999");
						}
						pbpub.countInteresRate(intrEntity);
						BigDecimal bigOvduInstam = intrEntity.getInamnt();
						bigInstam = bigOvduInstam;
						// 设置付息信息参数
						// instdt = tblKnaFxac.getBgindt(); //计息起始日期
						intrcd = tblKubinrt.getIntrcd(); // 利率编号
						ircdtp = tblKubinrt.getIncdtp(); // 利率代码类型
						intrwy = tblKubinrt.getIntrwy(); // 利率靠档方式
						cuusin = intrEntity.getIntrvl();
						CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
						calInterTax.setAcctno(acctno);
						calInterTax.setTranam(tranam);
						calInterTax.setBegndt(instdt);
						calInterTax.setEnddat(trandt);
						calInterTax.setCuusin(cuusin);
						calInterTax.setInstam(bigInstam);
						calInterTax.setInbebs(tblKnbDfir.getBsinrl());
						InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(tblKnbAcin, calInterTax,
								true);
						intxam = interestAndTax.getIntxam();
					} else {
						// 未按支取计划管理则按照传统处理方式
						// 提前支取
						if (CommUtil.compare(tblKnaFxac.getMatudt(), trandt) > 0) {
							lsinoc = E_INDLTP.HEAD; // 利息操作属于提前支取
							// 如果是部分支取，则取提前支取类型
							if (CommUtil.compare(tblKnaFxac.getOnlnbl().subtract(tranam), BigDecimal.ZERO) > 0) {
								tblKnbDfir = KnbDfirDao.selectOne_odb1(acctno, E_TEARTP.TQZQ, false);
								drintpName = E_TEARTP.TQZQ.getLongName();
							}
							// 如果是全部支取，则取提前销户类型
							else if (CommUtil.compare(tblKnaFxac.getOnlnbl().subtract(tranam), BigDecimal.ZERO) == 0) {
								tblKnbDfir = KnbDfirDao.selectOne_odb1(acctno, E_TEARTP.TQXH, false);
								drintpName = E_TEARTP.TQXH.getLongName();
							} else {
								throw DpModuleError.DpstAcct.BNAS1405(tblKnaFxac.getAcctno(),
										tblKnaFxac.getOnlnbl().toString());
							}
							// 检查违约支取利息定义信息
							if (CommUtil.isNull(tblKnbDfir)) {
								throw DpModuleError.DpstAcct.BNAS1148();
							}
							// 修改起息日期获取方式 modify by songkl 2017/07/05 满足丰收瑞丽产品需求
							// 若账户违约支取表中起息日来源为 起息日，则取定期表中起息日期
							if (tblKnbDfir.getBsindt() == E_BSINDT.QXR) {
								instdt = tblKnaFxac.getBgindt(); // 计息起始日期
							} else if (tblKnbDfir.getBsindt() == E_BSINDT.SCFX) {
								// 若账户违约表中起息日来源为上次付息日，则取计提表中上次结息日期
								// 若计提表中上次付息日期为空则取定期表中起息日期
								if (CommUtil.isNotNull(tblKnbAcin.getLcindt())) {
									instdt = tblKnbAcin.getLcindt();// 计息起始日期
								} else {
									instdt = tblKnaFxac.getBgindt(); // 计息起始日期
								}
								// 若结息日当天支取则修改计息日为当天日期（暂时这样修改 未确定）
								if (CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_date(),
										tblKnbAcin.getNcindt())) {
									instdt = CommTools.getBaseRunEnvs().getTrxn_date();
								}
							} else {
								throw DpModuleError.DpstAcct.BNAS0004();
							}
							// end
							// 计算利息，使用行内基准的活期利率
							IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
							intrEntity.setCrcycd(tblKnaFxac.getCrcycd()); // 币种
							intrEntity.setIntrcd(tblKnbDfir.getIntrcd()); // 利率代码
							if (E_IRCDTP.BASE == tblKnbDfir.getIncdtp()
									|| E_IRCDTP.Reference == tblKnbDfir.getIncdtp()) {
								intrEntity.setDepttm(E_TERMCD.T000);
							} else if (E_IRCDTP.LAYER == tblKnbDfir.getIncdtp()) {
								if (tblKnbDfir.getInclfg() == E_YES___.YES) {
									intrEntity.setIntrwy(tblKnbDfir.getIntrwy()); // 靠档方式
								}
							}
							intrEntity.setIncdtp(tblKnbDfir.getIncdtp()); // 利率代码类型
							intrEntity.setTrandt(trandt);
							// intrEntity.setBgindt(tblKnaFxac.getBgindt()); //起始日期
							// 修改起始日期 2017/07/05 modify by songkl
							intrEntity.setBgindt(instdt); // 起始日期
							intrEntity.setEdindt(trandt); // 结束日期
							intrEntity.setTranam(tranam); // 交易金额
							intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
							intrEntity.setInbebs(tblKnbDfir.getBsinrl()); // 计息基础
							intrEntity.setCorpno(tblKnaFxac.getCorpno());// 法人代码
							intrEntity.setBrchno(tblKnaFxac.getBrchno());// 机构
							intrEntity.setLevety(tblKnbDfir.getLevety());
							if (tblKnbDfir.getIntrdt() == E_INTRDT.OPEN) {
								intrEntity.setTrandt(tblKnaFxac.getOpendt());
								intrEntity.setTrantm("999999");
							}
							pbpub.countInteresRate(intrEntity);
							bigInstam = intrEntity.getInamnt();
							// 设置付息信息参数
							// instdt = tblKnaFxac.getBgindt(); //计息起始日期
							intrcd = tblKnbDfir.getIntrcd(); // 利率编号
							ircdtp = intrEntity.getIncdtp(); // 利率代码类型
							intrwy = intrEntity.getIntrwy(); // 利率靠档方式
							acbsin = intrEntity.getBaseir(); // 账户基准利率
							cuusin = intrEntity.getIntrvl(); // 当前执行利率
							remark = intrEntity.getRemark(); // 靠档天数
							// jym add 计算利息税
							CalInterTax calInterTax1 = SysUtil.getInstance(CalInterTax.class);
							calInterTax1.setAcctno(acctno);
							calInterTax1.setTranam(tranam);
							calInterTax1.setBegndt(instdt);
							calInterTax1.setEnddat(trandt);
							calInterTax1.setCuusin(cuusin);
							calInterTax1.setInstam(bigInstam);
							calInterTax1.setInbebs(tblKnbDfir.getBsinrl());

							InterestAndIntertax interestAndTax1 = DpInterestAndTax.calcInterAndTax(tblKnbAcin,
									calInterTax1, true);
							intxam = interestAndTax1.getIntxam();
							// end
							/**
							 * cuijia 提前支取时，追缴已付利息
							 * 
							 * 检查是否已结息，如果没有结息，不用做追缴，已结息重新计算支取部分金额的正常利息 已付利息为零和当前提前支取金额计算利息相等，不做处理
							 * 已付利息大于提前支取金额计算利息，差额从客户本金中追缴 已付利息小于提前支取金额计算利息，调整支取结息金额为差额
							 */
							if (tblKnbDfir.getDrdein() == E_YES___.YES) {
								// 判断在有提前支取的情况下，计算两次提前支取间的已付利息
								BigDecimal acin = BigDecimal.ZERO;
								BigDecimal acinTax = BigDecimal.ZERO;// 已扣除的利息税
								String lcindt = tblKnbAcin.getLcindt(); // 上次结息日
								// 判断是否已结息
								if (CommUtil.isNotNull(lcindt) && CommUtil.compare(lcindt, instdt) > 0) {
									// 检查是否有分段利率，有需要按照分段利率计算，付息表中的利率不准确
									List<KnbIndl> knbIndlListDO = DpAcinDao.listKnbIndl(acctno, E_INTRTP.ZHENGGLX,
											instdt, lcindt, false);
									String instDt = instdt; // 计息开始日期
									for (KnbIndl knbIndlDO : knbIndlListDO) {
										IoPbIntrPublicEntity intrDO = SysUtil.getInstance(IoPbIntrPublicEntity.class);
										intrDO.setCrcycd(tblKnaFxac.getCrcycd()); // 币种
										intrDO.setIntrcd(knbIndlDO.getIntrcd()); // 利率代码
										if (E_IRCDTP.BASE == knbIndlDO.getIncdtp()
												|| E_IRCDTP.Reference == knbIndlDO.getIncdtp()) {
											intrDO.setDepttm(tblKnaFxac.getDepttm());
										} else if (E_IRCDTP.LAYER == knbIndlDO.getIncdtp()) {
											intrDO.setIntrwy(knbIndlDO.getIntrwy()); // 靠档方式
										}
										intrDO.setIncdtp(knbIndlDO.getIncdtp()); // 利率代码类型
										intrDO.setTrandt(knbIndlDO.getInstdt());
										intrDO.setBgindt(knbIndlDO.getInstdt()); // 起始日期
										intrDO.setEdindt(knbIndlDO.getIneddt()); // 结束日期
										intrDO.setTranam(tranam); // 交易金额
										intrDO.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
										intrDO.setInbebs(knbIndlDO.getTxbebs()); // 计息基础
										intrDO.setCorpno(tblKnaFxac.getCorpno());// 法人代码
										intrDO.setBrchno(tblKnaFxac.getBrchno());// 机构
										intrDO.setLevety(tblKnbAcin.getLevety());
										pbpub.countInteresRate(intrDO);
										acin = acin.add(intrDO.getInamnt());
										// 计算利息税
										acinTax = acinTax.add(intrDO.getInamnt().multiply(knbIndlDO.getCatxrt()));
										instDt = knbIndlDO.getIneddt();
									}
									IoPbIntrPublicEntity intrDO = SysUtil.getInstance(IoPbIntrPublicEntity.class);
									intrDO.setCrcycd(tblKnaFxac.getCrcycd()); // 币种
									intrDO.setIntrcd(tblKnbAcin.getIntrcd()); // 利率代码
									if (E_IRCDTP.BASE == tblKnbAcin.getIncdtp()
											|| E_IRCDTP.Reference == tblKnbAcin.getIncdtp()) {
										intrDO.setDepttm(tblKnaFxac.getDepttm());
									} else if (E_IRCDTP.LAYER == tblKnbAcin.getIncdtp()) {
										intrDO.setIntrwy(tblKnbAcin.getIntrwy()); // 靠档方式
									}
									intrDO.setIncdtp(tblKnbAcin.getIncdtp()); // 利率代码类型
									intrDO.setTrandt(instDt);
									intrDO.setBgindt(instDt); // 起始日期
									intrDO.setEdindt(lcindt); // 结束日期
									intrDO.setTranam(tranam); // 交易金额
									intrDO.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
									intrDO.setInbebs(tblKnbAcin.getTxbebs()); // 计息基础
									intrDO.setCorpno(tblKnaFxac.getCorpno());// 法人代码
									intrDO.setBrchno(tblKnaFxac.getBrchno());// 机构
									intrDO.setLevety(tblKnbAcin.getLevety());
									pbpub.countInteresRate(intrDO);
									acin = acin.add(intrDO.getInamnt());
									// 计算利息税
									BigDecimal taxrat = BigDecimal.ZERO;
									if (tblKnbAcin.getTxbefg() == E_YES___.YES) {
										taxrat = SysUtil.getInstance(IoPbInRaSelSvc.class)
												.inttxRate(tblKnbAcin.getTaxecd()).getTaxrat();
									}
									acinTax = acinTax.add(intrDO.getInamnt().multiply(taxrat));
								}
								if (CommUtil.compare(acin, BigDecimal.ZERO) == 0
										|| CommUtil.compare(acin, bigInstam) == 0) {
								} else if (CommUtil.compare(acin, bigInstam) > 0) {
									BigDecimal diffAcin = BusiTools.roundByCurrency(tblKnaFxac.getCrcycd(),
											acin.subtract(bigInstam), null);
									// 追缴金额 = (已付利息-应付利息) - (已扣利息税 - 应扣利息税)
									diffAcin = diffAcin.subtract(acinTax.subtract(intxam));
									bigInstam = BigDecimal.ZERO;

									// 登记付息明细信息
									KnbPidl tblKnbPidl = SysUtil.getInstance(KnbPidl.class);
									tblKnbPidl.setAcctno(acctno); // 负债账号
									tblKnbPidl.setIntrtp(E_INTRTP.FUFUFULX); // 利息类型
									// Long detlsq = Long.parseLong(SequenceManager.nextval("KnbPidl"));
									Long detlsq = Long.parseLong(CoreUtil.nextValue("KnbPidl"));
									tblKnbPidl.setIndxno(detlsq); // 顺序号
									tblKnbPidl.setDetlsq(detlsq); // 明细序号
									tblKnbPidl.setIndlst(E_INDLST.YOUX); // 付息明细状态
									tblKnbPidl.setLsinoc(E_INDLTP.PYAFT); // 上次利息操作代码
									tblKnbPidl.setInstdt(instdt); // 计息起始日期
									tblKnbPidl.setIneddt(trandt); // 计息终止日期
									tblKnbPidl.setIntrcd(intrcd); // 利率编号
									tblKnbPidl.setIncdtp(ircdtp); // 利率代码类型
									tblKnbPidl.setLyinwy(null); // 分层计息方式
									tblKnbPidl.setIntrwy(intrwy); // 利率靠档方式
									tblKnbPidl.setLvamot(BigDecimal.ZERO); // 分层金额
									tblKnbPidl.setLvindt(null); // 层次利率存期
									tblKnbPidl.setGradin(BigDecimal.ZERO); // 档次计息余额
									tblKnbPidl.setTotlin(tranam); // 总计息余额
									tblKnbPidl.setAcmltn(acmltn); // 积数
									tblKnbPidl.setTxbebs(txbebs); // 计息基础
									tblKnbPidl.setAcbsin(acbsin); // 账户基准利率
									tblKnbPidl.setCuusin(cuusin); // 执行利率
									tblKnbPidl.setRlintr(diffAcin); // 实际利息发生额
									tblKnbPidl.setRlintx(BigDecimal.ZERO); // 实际利率税发生额
									tblKnbPidl.setIntrdt(trandt); // 计息日期
									tblKnbPidl.setIntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 计息流水
									tblKnbPidl.setPyindt(trandt); // 付息流水
									tblKnbPidl.setPyinsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 付息流水
									tblKnbPidl.setRemark("追缴利息金额"); // 靠档天数
									KnbPidlDao.insert(tblKnbPidl);
									// 将追缴的金额赋值，传出
									entity.setPyafamount(diffAcin);
								} else {
									bigInstam = bigInstam.subtract(acin);
								}
							}
							bizlog.debug("存储天数=============" + DateTools2.calDays(tblKnaFxac.getBgindt(), trandt, 1, 0)
									+ "，利率=====" + intrEntity.getIntrvl());
							bizlog.debug("提前支取利息>>>>>>>>>>>>>>>>>" + bigInstam);
							CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
							calInterTax.setAcctno(acctno);
							calInterTax.setTranam(tranam);
							calInterTax.setBegndt(instdt);
							calInterTax.setEnddat(trandt);
							calInterTax.setCuusin(cuusin);
							calInterTax.setInstam(bigInstam);
							calInterTax.setInbebs(tblKnbDfir.getBsinrl());
							InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(tblKnbAcin,
									calInterTax, true);
							intxam = interestAndTax.getIntxam();
						}
						// 到期日支取
						else if (CommUtil.compare(tblKnaFxac.getMatudt(), trandt) == 0) {
							KubInrt tblKubinrt = KubInrtDao.selectOne_odb1(acctno, true);
							if (tblKnbAcin.getInprwy() == E_IRRTTP.NO) {
								if (E_IRCDTP.LAYER == tblKnbAcin.getIncdtp()) {
									IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
									intrEntity.setCrcycd(tblKnaFxac.getCrcycd()); // 币种
									intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); // 利率代码
									// 如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
									intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); // 利率代码类型
									intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); // 靠档方式
									intrEntity.setTrandt(trandt);
									intrEntity.setBgindt(tblKnaFxac.getBgindt()); // 起始日期
									intrEntity.setEdindt(trandt); // 结束日期
									intrEntity.setTranam(tranam); // 交易金额
									intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
									intrEntity.setInbebs(tblKnbAcin.getTxbebs()); // 计息基础
									intrEntity.setCorpno(tblKnaFxac.getCorpno());// 法人代码
									intrEntity.setBrchno(tblKnaFxac.getBrchno());// 机构
									intrEntity.setLevety(tblKnbAcin.getLevety());
									if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
										intrEntity.setTrandt(tblKnaFxac.getOpendt());
										intrEntity.setTrantm("999999");
									}
									pbpub.countInteresRate(intrEntity);
									instdt = tblKnaFxac.getBgindt(); // 计息起始日期
									acbsin = intrEntity.getBaseir(); // 账户基准利率
									cuusin = intrEntity.getIntrvl(); // 当前执行利率
									intrcd = tblKnbAcin.getIntrcd(); // 利率编号
									ircdtp = tblKnbAcin.getIncdtp(); // 利率代码类型
									intrwy = tblKnbAcin.getIntrwy(); // 利率靠档方式
									// 利率优惠后执行利率
									cuusin = cuusin
											.add(cuusin.multiply(CommUtil.nvl(tblKubinrt.getFavort(), BigDecimal.ZERO)
													.divide(BigDecimal.valueOf(100))));
									// mod by leipeng 优惠后判断利率是否超出基础浮动范围20170220 start--
									// 利率的最大范围值
									BigDecimal intrvlmax = intrEntity.getBaseir().multiply(
											BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
									// 利率的最小范围值
									BigDecimal intrvlmin = intrEntity.getBaseir().multiply(
											BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
									if (CommUtil.compare(cuusin, intrvlmin) < 0) {
										cuusin = intrvlmin;
									} else if (CommUtil.compare(cuusin, intrvlmax) > 0) {
										cuusin = intrvlmax;
									}
									// mod by leipeng 优惠后判断时候超出基础浮动范围20170220 end--
									// 计算利息
									bigInstam = pbpub.countInteresRateByAmounts(cuusin, tblKnbAcin.getBgindt(),
											tblKnaFxac.getMatudt(), tranam, tblKnbAcin.getTxbebs());
									CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
									calInterTax.setAcctno(acctno);
									calInterTax.setTranam(tranam);
									calInterTax.setBegndt(tblKnbAcin.getBgindt());
									calInterTax.setEnddat(trandt);// 到期日
									calInterTax.setCuusin(cuusin);
									calInterTax.setInstam(bigInstam);
									calInterTax.setInbebs(tblKnbAcin.getTxbebs());
									InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(tblKnbAcin,
											calInterTax, true);
									intxam = interestAndTax.getIntxam();
								} else {
									/* 利率确定日期为支取日时，需要重新获取当前执行利率 modify by liaojc in 20161214 */
									cuusin = tblKubinrt.getCuusin(); // 账户利率表执行利率
									IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
									if (tblKnbAcin.getIntrdt() == E_INTRDT.DRAW) {
										intrEntity.setCrcycd(tblKnaFxac.getCrcycd()); // 币种
										intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); // 利率代码
										// 如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
										intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); // 利率代码类型
										intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); // 靠档方式
										intrEntity.setTrandt(trandt);
										intrEntity.setDepttm(tblKnaFxac.getDepttm());// 存期
										intrEntity.setBgindt(tblKnaFxac.getBgindt()); // 起始日期
										intrEntity.setEdindt(trandt); // 结束日期
										intrEntity.setTranam(tranam); // 交易金额
										intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
										intrEntity.setInbebs(tblKnbAcin.getTxbebs()); // 计息基础
										intrEntity.setCorpno(tblKnaFxac.getCorpno());// 法人代码
										intrEntity.setBrchno(tblKnaFxac.getBrchno());// 机构
										intrEntity.setLevety(tblKnbAcin.getLevety());
										pbpub.countInteresRate(intrEntity);
										cuusin = intrEntity.getIntrvl(); // 当前执行利率
										// 利率优惠后执行利率
										cuusin = cuusin.add(
												cuusin.multiply(CommUtil.nvl(tblKubinrt.getFavort(), BigDecimal.ZERO)
														.divide(BigDecimal.valueOf(100))));

										// mod by leipeng 优惠后判断利率是否超出基础浮动范围20170220 start--
										// 利率的最大范围值
										BigDecimal intrvlmax = intrEntity.getBaseir().multiply(BigDecimal.ONE
												.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
										// 利率的最小范围值
										BigDecimal intrvlmin = intrEntity.getBaseir().multiply(BigDecimal.ONE
												.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
										if (CommUtil.compare(cuusin, intrvlmin) < 0) {
											cuusin = intrvlmin;
										} else if (CommUtil.compare(cuusin, intrvlmax) > 0) {
											cuusin = intrvlmax;
										}
										// mod by leipeng 优惠后判断时候超出基础浮动范围20170220 end--
									}
									// 计算利息
									bigInstam = pbpub.countInteresRateByAmounts(cuusin, tblKnbAcin.getBgindt(),
											tblKnaFxac.getMatudt(), tranam, tblKnbAcin.getTxbebs());
									acbsin = tblKubinrt.getBsintr(); // 账户基准利率
									CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
									calInterTax.setAcctno(acctno);
									calInterTax.setTranam(tranam);
									calInterTax.setBegndt(tblKnbAcin.getBgindt());
									calInterTax.setEnddat(trandt);// 到期日
									calInterTax.setCuusin(cuusin);
									calInterTax.setInstam(bigInstam);
									calInterTax.setInbebs(tblKnbAcin.getTxbebs());
									InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(tblKnbAcin,
											calInterTax, true);
									intxam = interestAndTax.getIntxam();
								}
							} else if (tblKnbAcin.getInprwy() == E_IRRTTP.QD) {
								// 计算利息，使用行内基准的活期利率
								IoPbIntrPublicEntity intrMatuEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
								intrMatuEntity.setCrcycd(tblKnaFxac.getCrcycd()); // 币种
								intrMatuEntity.setIntrcd(tblKubinrt.getIntrcd()); // 利率代码
								intrMatuEntity.setIncdtp(tblKubinrt.getIncdtp()); // 利率代码类型
								intrMatuEntity.setIntrwy(tblKubinrt.getIntrwy()); // 靠档方式
								intrMatuEntity.setDepttm(tblKnaFxac.getDepttm()); // 存期
								intrMatuEntity.setTrandt(trandt);
								intrMatuEntity.setBgindt(tblKnaFxac.getBgindt()); // 起始日期
								intrMatuEntity.setEdindt(tblKnaFxac.getMatudt()); // 结束日期
								intrMatuEntity.setTranam(tranam); // 交易金额
								intrMatuEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
								intrMatuEntity.setInbebs(tblKnbAcin.getTxbebs()); // 计息基础
								intrMatuEntity.setCorpno(tblKnaFxac.getCorpno());// 法人代码
								intrMatuEntity.setBrchno(tblKnaFxac.getBrchno());// 机构
								intrMatuEntity.setLevety(tblKnbAcin.getLevety());
								if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
									intrMatuEntity.setTrandt(tblKnaFxac.getOpendt());
									intrMatuEntity.setTrantm("999999");
								}
								pbpub.countInteresRate(intrMatuEntity);
								acbsin = intrMatuEntity.getBaseir(); // 账户基准利率
								cuusin = intrMatuEntity.getIntrvl(); // 当前执行利率
								// 利率优惠后执行利率
								cuusin = cuusin.add(cuusin.multiply(CommUtil
										.nvl(tblKubinrt.getFavort(), BigDecimal.ZERO).divide(BigDecimal.valueOf(100))));
								// mod by leipeng 优惠后判断利率是否超出基础浮动范围20170220 start--
								// 利率的最大范围值
								BigDecimal intrvlmax = intrMatuEntity.getBaseir().multiply(
										BigDecimal.ONE.add(intrMatuEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
								// 利率的最小范围值
								BigDecimal intrvlmin = intrMatuEntity.getBaseir().multiply(
										BigDecimal.ONE.add(intrMatuEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
								if (CommUtil.compare(cuusin, intrvlmin) < 0) {
									cuusin = intrvlmin;
								} else if (CommUtil.compare(cuusin, intrvlmax) > 0) {
									cuusin = intrvlmax;
								}
								// mod by leipeng 优惠后判断时候超出基础浮动范围20170220 end--
								// 计算利息
								bigInstam = pbpub.countInteresRateByAmounts(cuusin, tblKnbAcin.getBgindt(),
										tblKnaFxac.getMatudt(), tranam, tblKnbAcin.getTxbebs());
								CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
								calInterTax.setAcctno(acctno);
								calInterTax.setTranam(tranam);
								// calInterTax.setBegndt(tblKnaFxac.getBgindt());
								calInterTax.setBegndt(tblKnbAcin.getBgindt());
								calInterTax.setEnddat(trandt);
								calInterTax.setCuusin(cuusin);
								calInterTax.setInstam(bigInstam);
								calInterTax.setInbebs(tblKnbAcin.getTxbebs());
								InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(tblKnbAcin,
										calInterTax, true);
								intxam = interestAndTax.getIntxam();
							} else {
								throw DpModuleError.DpstAcct.BNAS0201();
							}
							// 设置付息信息参数
							instdt = tblKnaFxac.getBgindt(); // 计息起始日期
							intrcd = tblKubinrt.getIntrcd(); // 利率编号
							ircdtp = tblKubinrt.getIncdtp(); // 利率代码类型
							intrwy = tblKubinrt.getIntrwy(); // 利率靠档方式
							bizlog.debug("正常支取利息>>>>>>>>>>>>>>>>>" + bigInstam);
						}
						// 到期后逾期支取
						else {
							KubInrt tblKubinrt = KubInrtDao.selectOne_odb1(acctno, true);
							BigDecimal bigNormInstam = BigDecimal.ZERO;
							BigDecimal bigNormIntxam = BigDecimal.ZERO;
							if (tblKnbAcin.getInprwy() == E_IRRTTP.NO) {
								if (E_IRCDTP.LAYER == tblKnbAcin.getIncdtp()) {
									IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
									intrEntity.setCrcycd(tblKnaFxac.getCrcycd()); // 币种
									intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); // 利率代码
									// 如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
									intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); // 利率代码类型
									intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); // 靠档方式
									intrEntity.setTrandt(trandt);
									intrEntity.setBgindt(tblKnaFxac.getBgindt()); // 起始日期
									intrEntity.setEdindt(trandt); // 结束日期
									intrEntity.setTranam(tranam); // 交易金额
									intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
									intrEntity.setInbebs(tblKnbAcin.getTxbebs()); // 计息基础
									intrEntity.setCorpno(tblKnaFxac.getCorpno());// 法人代码
									intrEntity.setBrchno(tblKnaFxac.getBrchno());// 机构
									intrEntity.setLevety(tblKnbAcin.getLevety());
									if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
										intrEntity.setTrandt(tblKnaFxac.getOpendt());
										intrEntity.setTrantm("999999");
									}
									pbpub.countInteresRate(intrEntity);
									instdt = tblKnaFxac.getBgindt(); // 计息起始日期
									acbsin = intrEntity.getBaseir(); // 账户基准利率
									cuusin = intrEntity.getIntrvl(); // 当前执行利率
									intrcd = tblKnbAcin.getIntrcd(); // 利率编号
									ircdtp = tblKnbAcin.getIncdtp(); // 利率代码类型
									intrwy = tblKnbAcin.getIntrwy(); // 利率靠档方式
									// 利率优惠后执行利率
									cuusin = cuusin
											.add(cuusin.multiply(CommUtil.nvl(tblKubinrt.getFavort(), BigDecimal.ZERO)
													.divide(BigDecimal.valueOf(100))));
									// mod by leipeng 优惠后判断利率是否超出基础浮动范围20170220 start--
									// 利率的最大范围值
									BigDecimal intrvlmax = intrEntity.getBaseir().multiply(
											BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
									// 利率的最小范围值
									BigDecimal intrvlmin = intrEntity.getBaseir().multiply(
											BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
									if (CommUtil.compare(cuusin, intrvlmin) < 0) {
										cuusin = intrvlmin;
									} else if (CommUtil.compare(cuusin, intrvlmax) > 0) {
										cuusin = intrvlmax;
									}
									// mod by leipeng 优惠后判断时候超出基础浮动范围20170220 end--
									// 计算利息
									bigNormInstam = pbpub.countInteresRateByAmounts(cuusin, tblKnbAcin.getBgindt(),
											tblKnaFxac.getMatudt(), tranam, tblKnbAcin.getTxbebs());
									CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
									calInterTax.setAcctno(acctno);
									calInterTax.setTranam(tranam);
									calInterTax.setBegndt(tblKnbAcin.getBgindt());
									calInterTax.setEnddat(tblKnaFxac.getMatudt());// 到期日
									calInterTax.setCuusin(cuusin);
									calInterTax.setInstam(bigNormInstam);
									calInterTax.setInbebs(tblKnbAcin.getTxbebs());
									InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(tblKnbAcin,
											calInterTax, true);
									bigNormIntxam = interestAndTax.getIntxam();
								} else {
									/* 利率确定日期为支取日时，需要重新获取当前执行利率 modify by liaojc in 20161214 */
									cuusin = tblKubinrt.getCuusin(); // 账户利率表执行利率
									IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
									if (tblKnbAcin.getIntrdt() == E_INTRDT.DRAW) {
										intrEntity.setCrcycd(tblKnaFxac.getCrcycd()); // 币种
										intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); // 利率代码
										// 如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
										intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); // 利率代码类型
										intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); // 靠档方式
										intrEntity.setTrandt(trandt);
										intrEntity.setDepttm(tblKnaFxac.getDepttm());// 存期
										intrEntity.setBgindt(tblKnaFxac.getBgindt()); // 起始日期
										intrEntity.setEdindt(trandt); // 结束日期
										intrEntity.setTranam(tranam); // 交易金额
										intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
										intrEntity.setInbebs(tblKnbAcin.getTxbebs()); // 计息基础
										intrEntity.setCorpno(tblKnaFxac.getCorpno());// 法人代码
										intrEntity.setBrchno(tblKnaFxac.getBrchno());// 机构
										intrEntity.setLevety(tblKnbAcin.getLevety());
										pbpub.countInteresRate(intrEntity);
										cuusin = intrEntity.getIntrvl(); // 当前执行利率
										// 利率优惠后执行利率
										cuusin = cuusin.add(
												cuusin.multiply(CommUtil.nvl(tblKubinrt.getFavort(), BigDecimal.ZERO)
														.divide(BigDecimal.valueOf(100))));

										// mod by leipeng 优惠后判断利率是否超出基础浮动范围20170220 start--
										// 利率的最大范围值
										BigDecimal intrvlmax = intrEntity.getBaseir().multiply(BigDecimal.ONE
												.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
										// 利率的最小范围值
										BigDecimal intrvlmin = intrEntity.getBaseir().multiply(BigDecimal.ONE
												.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
										if (CommUtil.compare(cuusin, intrvlmin) < 0) {
											cuusin = intrvlmin;
										} else if (CommUtil.compare(cuusin, intrvlmax) > 0) {
											cuusin = intrvlmax;
										}
										// mod by leipeng 优惠后判断时候超出基础浮动范围20170220 end--
									}
									// 计算利息
									bigNormInstam = pbpub.countInteresRateByAmounts(cuusin, tblKnbAcin.getBgindt(),
											tblKnaFxac.getMatudt(), tranam, tblKnbAcin.getTxbebs());
									CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
									calInterTax.setAcctno(acctno);
									calInterTax.setTranam(tranam);
									calInterTax.setBegndt(tblKnbAcin.getBgindt());
									calInterTax.setEnddat(tblKnaFxac.getMatudt());// 到期日
									calInterTax.setCuusin(cuusin);
									calInterTax.setInstam(bigNormInstam);
									calInterTax.setInbebs(tblKnbAcin.getTxbebs());
									InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(tblKnbAcin,
											calInterTax, true);
									bigNormIntxam = interestAndTax.getIntxam();
									acbsin = tblKubinrt.getBsintr(); // 账户基准利率
								}
							} else if (tblKnbAcin.getInprwy() == E_IRRTTP.QD) {
								// 计算利息，使用行内基准的活期利率
								// IntrPublicEntity intrMatuEntity = new IntrPublicEntity();
								IoPbIntrPublicEntity intrMatuEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
								intrMatuEntity.setCrcycd(tblKnaFxac.getCrcycd()); // 币种
								intrMatuEntity.setIntrcd(tblKubinrt.getIntrcd()); // 利率代码
								intrMatuEntity.setIncdtp(tblKubinrt.getIncdtp()); // 利率代码类型
								intrMatuEntity.setIntrwy(tblKubinrt.getIntrwy()); // 靠档方式
								intrMatuEntity.setDepttm(tblKnaFxac.getDepttm()); // 存期
								intrMatuEntity.setTrandt(trandt);
								intrMatuEntity.setBgindt(tblKnaFxac.getBgindt()); // 起始日期
								intrMatuEntity.setEdindt(tblKnaFxac.getMatudt()); // 结束日期
								intrMatuEntity.setTranam(tranam); // 交易金额
								intrMatuEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
								intrMatuEntity.setInbebs(tblKnbAcin.getTxbebs()); // 计息基础
								intrMatuEntity.setCorpno(tblKnaFxac.getCorpno());// 法人代码
								intrMatuEntity.setBrchno(tblKnaFxac.getBrchno());// 机构
								intrMatuEntity.setLevety(tblKnbAcin.getLevety());
								if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
									intrMatuEntity.setTrandt(tblKnaFxac.getOpendt());
									intrMatuEntity.setTrantm("999999");
								}
								pbpub.countInteresRate(intrMatuEntity);
								// 利率优惠后执行利率
								cuusin = intrMatuEntity.getIntrvl();
								cuusin = cuusin.add(cuusin.multiply(CommUtil
										.nvl(tblKubinrt.getFavort(), BigDecimal.ZERO).divide(BigDecimal.valueOf(100))));
								// mod by leipeng 优惠后判断利率是否超出基础浮动范围20170220 start--
								// 利率的最大范围值
								BigDecimal intrvlmax = intrMatuEntity.getBaseir().multiply(
										BigDecimal.ONE.add(intrMatuEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
								// 利率的最小范围值
								BigDecimal intrvlmin = intrMatuEntity.getBaseir().multiply(
										BigDecimal.ONE.add(intrMatuEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
								if (CommUtil.compare(cuusin, intrvlmin) < 0) {
									cuusin = intrvlmin;
								} else if (CommUtil.compare(cuusin, intrvlmax) > 0) {
									cuusin = intrvlmax;
								}
								// mod by leipeng 优惠后判断时候超出基础浮动范围20170220 end--
								// 计算利息
								bigNormInstam = pbpub.countInteresRateByAmounts(cuusin, tblKnbAcin.getBgindt(),
										tblKnaFxac.getMatudt(), tranam, tblKnbAcin.getTxbebs());
								acbsin = intrMatuEntity.getBaseir(); // 账户基准利率
								// cuusin = intrMatuEntity.getIntrvl(); //当前执行利率
								CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
								calInterTax.setAcctno(acctno);
								calInterTax.setTranam(tranam);
								calInterTax.setBegndt(tblKnbAcin.getBgindt());
								calInterTax.setEnddat(tblKnaFxac.getMatudt());// 到期日
								calInterTax.setCuusin(cuusin);
								calInterTax.setInstam(bigNormInstam);
								calInterTax.setInbebs(tblKnbAcin.getTxbebs());
								InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(tblKnbAcin,
										calInterTax, true);
								bigNormIntxam = interestAndTax.getIntxam();
							} else {
								throw DpModuleError.DpstAcct.BNAS0201();
							}
							bigInstam = BusiTools.roundByCurrency(tblKnaFxac.getCrcycd(), bigInstam.add(bigNormInstam),
									null);
							/* 计算逾期部分利息，按活期利率计算 */
							tblKnbDfir = KnbDfirDao.selectOne_odb1(acctno, E_TEARTP.OVTM, false);
							drintpName = E_TEARTP.OVTM.getLongName();
							// 检查违约支取利息定义信息
							if (CommUtil.isNull(tblKnbDfir)) {
								throw DpModuleError.DpstAcct.BNAS1209(tblKnaFxac.getProdcd(), drintpName);
							}
							// 计算利息，使用行内基准的活期利率
							IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
							intrEntity.setCrcycd(tblKnaFxac.getCrcycd()); // 币种
							intrEntity.setIntrcd(tblKnbDfir.getIntrcd()); // 利率代码
							if (E_IRCDTP.BASE == tblKnbDfir.getIncdtp()
									|| E_IRCDTP.Reference == tblKnbDfir.getIncdtp()) {
								intrEntity.setDepttm(E_TERMCD.T000);
							} else if (E_IRCDTP.LAYER == tblKnbDfir.getIncdtp()) {
								if (tblKnbDfir.getInclfg() == E_YES___.YES) {
									intrEntity.setIntrwy(tblKnbDfir.getIntrwy()); // 靠档方式
								}
							}
							intrEntity.setIncdtp(tblKnbDfir.getIncdtp()); // 利率代码类型
							intrEntity.setTrandt(trandt);
							intrEntity.setBgindt(tblKnaFxac.getMatudt()); // 起始日期
							intrEntity.setEdindt(trandt); // 结束日期
							intrEntity.setTranam(tranam); // 交易金额
							intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
							intrEntity.setInbebs(tblKnbDfir.getBsinrl()); // 计息基础
							intrEntity.setCorpno(tblKnaFxac.getCorpno());// 法人代码
							intrEntity.setBrchno(tblKnaFxac.getBrchno());// 机构
							intrEntity.setLevety(tblKnbDfir.getLevety());
							if (tblKnbDfir.getIntrdt() == E_INTRDT.OPEN) {
								intrEntity.setTrandt(tblKnaFxac.getOpendt());
								intrEntity.setTrantm("999999");
							}
							pbpub.countInteresRate(intrEntity);
							BigDecimal bigOvduInstam = intrEntity.getInamnt();
							// 设置付息信息参数
							instdt = tblKnaFxac.getBgindt(); // 计息起始日期
							intrcd = tblKubinrt.getIntrcd(); // 利率编号
							ircdtp = tblKubinrt.getIncdtp(); // 利率代码类型
							intrwy = tblKubinrt.getIntrwy(); // 利率靠档方式
							CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
							calInterTax.setAcctno(acctno);
							calInterTax.setTranam(tranam);
							calInterTax.setBegndt(tblKnaFxac.getMatudt());// 到期日
							calInterTax.setEnddat(trandt);// 交易日期
							calInterTax.setCuusin(intrEntity.getIntrvl());
							calInterTax.setInstam(bigOvduInstam);
							calInterTax.setInbebs(tblKnbDfir.getBsinrl());
							InterestAndIntertax interestAndTax = DpInterestAndTax.calcInterAndTax(tblKnbAcin,
									calInterTax, true);
							BigDecimal bigOvduIntxam = interestAndTax.getIntxam();// 逾期部分利息税
							bigInstam = BusiTools.roundByCurrency(tblKnaFxac.getCrcycd(), bigInstam.add(bigOvduInstam),
									null);
							intxam = bigNormIntxam.add(bigOvduIntxam);// 利息税,正常部分+逾期部分
							bizlog.debug("逾期支取利息>>>>>>>>>>>>>>>>>" + bigInstam);
						}
					}
				}
			}
			entity.setInstam(BusiTools.roundByCurrency(tblKnaFxac.getCrcycd(), bigInstam, null));
			entity.setIntxam(BusiTools.roundByCurrency(tblKnaFxac.getCrcycd(), intxam, null));
			// 登记付息明细信息
			KnbPidl tblKnbPidl = SysUtil.getInstance(KnbPidl.class);
			tblKnbPidl.setAcctno(acctno); // 负债账号
			tblKnbPidl.setIntrtp(E_INTRTP.ZHENGGLX); // 利息类型
			// Long detlsq = Long.parseLong(SequenceManager.nextval("KnbPidl"));
			Long detlsq = Long.parseLong(CoreUtil.nextValue("KnbPidl"));
			tblKnbPidl.setIndxno(detlsq); // 顺序号
			tblKnbPidl.setDetlsq(detlsq); // 明细序号
			tblKnbPidl.setIndlst(E_INDLST.YOUX); // 付息明细状态
			tblKnbPidl.setLsinoc(lsinoc); // 上次利息操作代码
			tblKnbPidl.setInstdt(instdt); // 计息起始日期
			tblKnbPidl.setIneddt(trandt); // 计息终止日期
			tblKnbPidl.setIntrcd(intrcd); // 利率编号
			tblKnbPidl.setIncdtp(ircdtp); // 利率代码类型
			tblKnbPidl.setLyinwy(null); // 分层计息方式
			tblKnbPidl.setIntrwy(intrwy); // 利率靠档方式
			tblKnbPidl.setLvamot(BigDecimal.ZERO); // 分层金额
			tblKnbPidl.setLvindt(null); // 层次利率存期
			tblKnbPidl.setGradin(BigDecimal.ZERO); // 档次计息余额
			tblKnbPidl.setTotlin(tranam); // 总计息余额
			tblKnbPidl.setAcmltn(acmltn); // 积数
			tblKnbPidl.setTxbebs(txbebs); // 计息基础
			tblKnbPidl.setAcbsin(acbsin); // 账户基准利率
			tblKnbPidl.setCuusin(cuusin); // 执行利率
			tblKnbPidl.setRlintr(entity.getInstam()); // 实际利息发生额
			tblKnbPidl.setRlintx(BigDecimal.ZERO); // 实际利率税发生额
			tblKnbPidl.setIntrdt(trandt); // 计息日期
			tblKnbPidl.setIntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 计息流水
			tblKnbPidl.setPyindt(trandt); // 付息流水
			tblKnbPidl.setPyinsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 付息流水
			tblKnbPidl.setRemark(remark); // 靠档天数
			KnbPidlDao.insert(tblKnbPidl);
			// 20160109 add by wuxf,付息后清除已计提利息。
			tblKnbAcin.setLastdt(trandt);
			tblKnbAcin.setLaamdt(trandt);
			tblKnbAcin.setCutmam(BigDecimal.ZERO);
			tblKnbAcin.setPlanin(BigDecimal.ZERO);
			KnbAcinDao.updateOne_odb1(tblKnbAcin);
		}
		bizlog.debug("支取计算利息结束<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * 获取负值账户的可用余额。当负债活期结算账户为热点账户时，取redis可用金额；否则取负债账户可用余额。
	 * 
	 * @author Xiaoyu Luo
	 * @param knaAcct
	 * @return 返回客户负债账户可用余额
	 */
	public static BigDecimal getBalance(String acctno) {
		KnaAcct knaAcct = KnaAcctDao.selectOne_odb1(acctno, true);
		return getAcctBalance(knaAcct);
	}

	public static BigDecimal getAcctBalance(KnaAcct knaAcct) {
		BigDecimal balance = knaAcct.getOnlnbl();
		if (knaAcct.getAcsetp() == E_ACSETP.SA) {

			/**
			 * mod by xj 20180503 IoHotCtrlSvcType没有服务实现,默认无热点账户
			 */

			/*
			 * E_YES___ hcflag = SysUtil.getInstance(IoHotCtrlSvcType.class)
			 * .selHcpDefn(knaAcct.getAcctno());
			 */
			E_YES___ hcflag = E_YES___.NO;

			if (hcflag == E_YES___.YES) {
				balance = DpPublic.getAvaiam(knaAcct.getAcctno());
			}
		}
		return balance;
	}

	/**
	 * 获取负债子户账户可用余额带锁
	 * 
	 * @param 负债账号
	 * @param 定活标志
	 * @return 可用余额
	 */
	public static BigDecimal getAcctBalWithLock(String acctno) {
		BigDecimal onlnbl = BigDecimal.ZERO;
		KnaAcct acct = KnaAcctDao.selectOneWithLock_odb1(acctno, false);
		if (CommUtil.isNull(acct)) {
			throw DpModuleError.DpstAcct.BNAS1401(acctno);
		}
		DpFrozInfoEntity cplDpFrozInfoEntity = DpFrozTools.getFrozInfo(E_FROZOW.AUACCT, acctno);
		onlnbl = acct.getOnlnbl().subtract(cplDpFrozInfoEntity.getFrozbl());
		if (CommUtil.compare(BigDecimal.ZERO , onlnbl) >= 0) {
			onlnbl = BigDecimal.ZERO;
		}
		
		if (cplDpFrozInfoEntity.getFralfg() == E_YES___.YES || cplDpFrozInfoEntity.getFrotfg() == E_YES___.YES) {
			onlnbl = BigDecimal.ZERO;
		}
		return onlnbl;
	}
	
	/**
	 * 获取负债子户账户可用余额不带锁
	 * 
	 * @param 负债账号
	 * @param 定活标志
	 * @return 可用余额
	 */
	public static BigDecimal getAcctBalNotLock(String acctno) {
		BigDecimal onlnbl = BigDecimal.ZERO;
		KnaAcct acct = KnaAcctDao.selectOne_odb1(acctno, false);
		if (CommUtil.isNull(acct)) {
			throw DpModuleError.DpstAcct.BNAS1401(acctno);
		}
		DpFrozInfoEntity cplDpFrozInfoEntity = DpFrozTools.getFrozInfo(E_FROZOW.ACCTNO, acctno);
		onlnbl = acct.getOnlnbl().subtract(cplDpFrozInfoEntity.getFrozbl());
		//判断金额可用余额小于等于零情况
		if (CommUtil.compare(BigDecimal.ZERO , onlnbl) >= 0) {
			onlnbl = BigDecimal.ZERO;
		}
		//判断只收不付、不收不付标志
		if (cplDpFrozInfoEntity.getFralfg() == E_YES___.YES || cplDpFrozInfoEntity.getFrotfg() == E_YES___.YES) {
			onlnbl = BigDecimal.ZERO;
		}
		return onlnbl;
	}
}
