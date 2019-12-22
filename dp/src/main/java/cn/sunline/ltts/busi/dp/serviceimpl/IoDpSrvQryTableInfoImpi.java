package cn.sunline.ltts.busi.dp.serviceimpl;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbtl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbtlDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcct;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcctDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActpDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAddt;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAddtDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrch;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrchDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCust;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCustDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntr;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTerm;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTermDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctMatuDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDrawDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDrawPlanDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBill;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBillDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapot;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIobl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblDao;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.InknlcnapotDetl;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpHKnlBill;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbAcin;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnlBill;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppb;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppbAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppbActp;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppbAddt;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppbBrch;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppbCust;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppbTerm;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnbCbtl;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnbParaMenuInfo;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlCary;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlIobl;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.ioKnlSpnd;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoFrozInfoOut;
import cn.sunline.ltts.busi.pb.namedsql.intr.ProintrSelDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEBS;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

/**
 * 查询表信息服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoDpSrvQryTableInfoImpi", longname = "查询表信息服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoDpSrvQryTableInfoImpi implements
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo {
	/**
	 * 增加账户余额发生明细表记录
	 * 
	 */
	public Integer saveKnlBill(
			final cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnlBill knlBill) {
		int cont;

		KnlBill tblKnlBill = SysUtil.getInstance(KnlBill.class);
		CommUtil.copyProperties(tblKnlBill, knlBill, false);

		cont = KnlBillDao.insert(tblKnlBill);

		return cont;
	}

	/**
	 * 根据产品号查询产品基础属性表
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppb getKupDppbOdb1(
			String prodcd, Boolean isable) {
		KupDppb tblKupDppb = KupDppbDao.selectOne_odb1(prodcd, isable);
		IoDpKupDppb kupDppb = null;
		if (CommUtil.isNotNull(tblKupDppb)) {
			kupDppb = SysUtil.getInstance(IoDpKupDppb.class);
			CommUtil.copyProperties(kupDppb, tblKupDppb, false);
		}
		return kupDppb;
	}

	/**
	 * 根据产品编号客户账号类型查询产品账户类型控制表
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppbActp getKupDbbpActpOdb1(
			String prodcd,
			cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT cacttp,
			Boolean isable) {
		KupDppbActp tblKupDppbActp = KupDppbActpDao.selectOne_odb1(prodcd,
				cacttp, isable);
		IoDpKupDppbActp kupDppbActp = null;
		if (CommUtil.isNotNull(tblKupDppbActp)) {
			kupDppbActp = SysUtil.getInstance(IoDpKupDppbActp.class);
			CommUtil.copyProperties(kupDppbActp, tblKupDppbActp);
		}
		return kupDppbActp;
	}

	/**
	 * 根据产品币种查询开户控制表
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppbCust getKupDppbCustOdb1(
			String prodcd, String crcycd, Boolean isable) {
		KupDppbCust tblKupDppbCust = KupDppbCustDao.selectOne_odb1(prodcd,
				crcycd, isable);
		IoDpKupDppbCust kupDppbCust = null;
		if (CommUtil.isNotNull(tblKupDppbCust)) {
			kupDppbCust = SysUtil.getInstance(IoDpKupDppbCust.class);
			CommUtil.copyProperties(kupDppbCust, tblKupDppbCust);
		}
		return kupDppbCust;
	}

	/**
	 * 根据电子账号冻结状态查询冻结记录
	 * 
	 */
	public Options<IoDpKnbFroz> listKnbFrozOdb11(
			String custac,
			cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST frozst,
			Boolean isable) {
		List<KnbFroz> tblKnbFrozs = KnbFrozDao.selectAll_odb11(custac, frozst,
				isable);
		Options<IoDpKnbFroz> opts = new DefaultOptions<>();
		// IoDpKnbFroz froz = SysUtil.getInstance(IoDpKnbFroz.class);
		for (KnbFroz KnbFroz : tblKnbFrozs) {
			IoDpKnbFroz froz = SysUtil.getInstance(IoDpKnbFroz.class);
			CommUtil.copyProperties(froz, KnbFroz, false);
			opts.add(froz);
		}
		return opts;
	}

	/**
	 * 根据产品编号、存期查询存款产品核算表
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppbAcct getKupDppbAcctOdb1(
			String prodcd,
			cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD depttm,
			Boolean isable) {
		KupDppbAcct src = KupDppbAcctDao.selectOne_odb1(prodcd, depttm, isable);
		IoDpKupDppbAcct desc = null;
		if (CommUtil.isNotNull(src)) {
			desc = SysUtil.getInstance(IoDpKupDppbAcct.class);
			CommUtil.copyProperties(desc, src);
		}
		return desc;
	}

	/**
	 * 根据产品代码、币种查询产品存期控制表
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppbTerm getKupDppbTermOdb1(
			String prodcd, String crcycd,
			cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD depttm,
			Boolean isable) {
		KupDppbTerm src = KupDppbTermDao.selectOne_odb1(prodcd, crcycd, depttm,
				isable);
		IoDpKupDppbTerm desc = null;
		if (CommUtil.isNotNull(src)) {
			desc = SysUtil.getInstance(IoDpKupDppbTerm.class);
			CommUtil.copyProperties(desc, src);
		}
		return desc;
	}

	/**
	 * 根据产品代码和币种查询产品机构控制信息列表
	 * 
	 */
	public void getKupDppbBrchOdb1(
			String prodcd,
			String crcycd,
			Boolean isable,
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo.getKupDppbBrchOdb1.Output output) {
		List<KupDppbBrch> list = KupDppbBrchDao.selectAll_odb1(prodcd, crcycd,
				isable);
		DefaultOptions<IoDpKupDppbBrch> opts = new DefaultOptions<>();
		// IoDpKupDppbBrch brch = SysUtil.getInstance(IoDpKupDppbBrch.class);
		for (KupDppbBrch kup_brch : list) {
			IoDpKupDppbBrch brch = SysUtil.getInstance(IoDpKupDppbBrch.class);
			CommUtil.copyProperties(brch, kup_brch);
			opts.add(brch);
		}
		output.setList(opts);
	}

	/**
	 * 查询电子账户出入金明细
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlIobl getKnlIoblOdb1(
			String servsq, String servdt) {
		KnlIobl tbknliobl = KnlIoblDao.selectOne_odb1(servsq, servdt, false);
		IoKnlIobl knlIobl = null;
		if (CommUtil.isNotNull(tbknliobl)) {
			knlIobl = SysUtil.getInstance(IoKnlIobl.class);
			CommUtil.copyProperties(knlIobl, tbknliobl);
		}
		return knlIobl;
	}

	/**
	 * 更新电子账户出入金明细
	 * 
	 */
	public void updateKnlIoblOdb1(
			final cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlIobl knlIobl) {
		KnlIobl tbKnlIobl = SysUtil.getInstance(KnlIobl.class);
		CommUtil.copyProperties(tbKnlIobl, knlIobl);
		KnlIoblDao.updateOne_odb1(tbKnlIobl);
	}

	/**
	 * 按计提日期查询负债计提汇总
	 * 
	 */
	public Options<IoKnbCbtl> listKnbCbtlOdb1(
			String acctdt) {
		List<KnbCbtl> list = KnbCbtlDao.selectAll_odb2(acctdt, false);
		// DefaultOptions<IoKnbCbtl> opts = new DefaultOptions<>();
		// List<IoKnbCbtl> opts = new <IoKnbCbtl> ArrayList();
		Options<IoKnbCbtl> opts = new DefaultOptions<IoKnbCbtl>();

		for (KnbCbtl src : list) {
			IoKnbCbtl cbtl = SysUtil.getInstance(IoKnbCbtl.class);
			CommUtil.copyProperties(cbtl, src, false);
			opts.add(cbtl);
		}

		return opts;
	}

	/**
	 * 根据负债账号查询负债计息信息
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbAcin getKnbAcinOdb1(
			String acctno, Boolean isable) {
		KnbAcin tblKnbAcin = KnbAcinDao.selectOne_odb1(acctno, isable);
		IoDpKnbAcin cplKnbAcin = null;

		if (CommUtil.isNotNull(tblKnbAcin)) {
			cplKnbAcin = SysUtil.getInstance(IoDpKnbAcin.class);
			CommUtil.copyProperties(cplKnbAcin, tblKnbAcin);
		}

		return cplKnbAcin;
	}

	/**
	 * 根据电子账户查询负债账户信息列表
	 * 
	 */
	public Options<IoDpKnaAcct> listKnaAcctOdb6(
			String custac, Boolean isable) {
		List<KnaAcct> list = KnaAcctDao.selectAll_odb6(custac, isable);
		// IoDpKnaAcct dest = SysUtil.getInstance(IoDpKnaAcct.class);
		DefaultOptions<IoDpKnaAcct> opts = new DefaultOptions<>();
		for (KnaAcct KnaAcct : list) {
			IoDpKnaAcct dest = SysUtil.getInstance(IoDpKnaAcct.class);
			KnaAcct.setOnlnbl(DpAcctProc.getAcctBalance(KnaAcct));
			CommUtil.copyProperties(dest, KnaAcct);
			opts.add(dest);
		}
		return opts;
	}

	/**
	 * 根据负债账号查询子账号信息
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct getKnaAcctOdb1(
			String acctno, Boolean isable) {
		KnaAcct acct = KnaAcctDao.selectOne_odb1(acctno, isable);
		IoDpKnaAcct knaAcct = null;

		if (CommUtil.isNotNull(acct)) {
			knaAcct = SysUtil.getInstance(IoDpKnaAcct.class);
			acct.setOnlnbl(DpAcctProc.getAcctBalance(acct));
			CommUtil.copyProperties(knaAcct, acct);
		}

		return knaAcct;
	}

	/**
	 * 根据负债账号查询产品附加信息表
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppbAddt getKupDppbAddtOdb1(
			String prodcd, Boolean isable) {
		KupDppbAddt addt = KupDppbAddtDao.selectOne_odb1(prodcd, isable);
		IoDpKupDppbAddt kupDppbAddt = null;
		if (CommUtil.isNotNull(addt)) {
			kupDppbAddt = SysUtil.getInstance(IoDpKupDppbAddt.class);
			CommUtil.copyProperties(kupDppbAddt, addt);
		}

		return kupDppbAddt;
	}

	/**
	 * 根据电子账号删除负债活期账户信息表信息
	 * 
	 */
	public void removeKnaAcctOdb1(String acctno) {
		KnaAcctDao.deleteOne_odb1(acctno);
	}

	/**
	 * 根据电子账号删除负债活期账户支取控制表信息
	 * 
	 */
	public void removeKnaDrawOdb1(String acctno) {
		KnaDrawDao.deleteOne_odb1(acctno);
	}

	/**
	 * 根据电子账号删除负债活期账户支取计划表信息
	 * 
	 */
	public void removeKnaDrawPlanOdb2(String acctno) {
		KnaDrawPlanDao.delete_odb2(acctno);
	}

	/**
	 * 根据电子账号删除负债活期账户到期信息表信息
	 * 
	 */
	public void removeKnaAcctMatuOdb1(String acctno) {
		KnaAcctMatuDao.deleteOne_odb1(acctno);
	}

	/**
	 * 亲情钱包账户销户状态更新
	 * 
	 */
	public void updateKnaAcctOdb1(
			final cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct knaact) {
		KnaAcct tblknaacct = SysUtil.getInstance(KnaAcct.class);
		CommUtil.copyProperties(tblknaacct, knaact);
		KnaAcctDao.updateOne_odb1(tblknaacct);
	}

	/**
	 * 查询大小额往来账明细
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.InknlcnapotDetl getKnlCnapotOdb1(
			String msetdt, String msetsq, String pyercd,
			cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOTYPE iotype,
			cn.sunline.ltts.busi.sys.type.DpEnumType.E_CRDBTG crdbtg,
			cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST status,
			Boolean isable) {
		// KnlCnapot tblKnlCnapot = KnlCnapotDao.selectOne_odb2(msetdt, msetsq,
		// crdbtg, iotype, pyercd, status, isable);

		KnlCnapot tblKnlCnapot = DpAcctQryDao.selknlcnapotChk(msetdt, msetsq,
				pyercd, iotype, crdbtg, status, isable);

		InknlcnapotDetl inKnlCnapot = null;

		if (CommUtil.isNotNull(tblKnlCnapot)) {
			inKnlCnapot = SysUtil.getInstance(InknlcnapotDetl.class);
			CommUtil.copyProperties(inKnlCnapot, tblKnlCnapot);
		}

		return inKnlCnapot;
	}

	/**
	 * 更新大小额往来账明细
	 * 
	 */
	public void updateKnlCnapotOdb1(
			final cn.sunline.ltts.busi.iobus.type.IoDpTable.InknlcnapotDetl knlCnapot) {
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String Tmstmp = DateTools2.getCurrentTimestamp();
		String revrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		DpAcctQryDao.updknlcnapotCnapre(knlCnapot.getStatus(),
				knlCnapot.getTransq(), knlCnapot.getTrandt(), revrsq, trandt,
				Tmstmp);
	}

	/**
	 * 查询电子账户冻结信息
	 * 
	 */
	public Options<IoFrozInfoOut> listKnbFrozOdb15(
			String custac,
			cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP frlmtp,
			cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST frozst,
			Boolean isable) {
		List<KnbFroz> frozList = KnbFrozDao.selectAll_odb15(custac, frlmtp,
				frozst, isable);
		@SuppressWarnings("unchecked")
		Options<IoFrozInfoOut> info = SysUtil.getInstance(Options.class);
		for (KnbFroz frozInfo : frozList) {
			IoFrozInfoOut frozInfoOut = SysUtil
					.getInstance(IoFrozInfoOut.class);
			CommUtil.copyProperties(frozInfoOut, frozInfo);
			info.add(frozInfoOut);
		}
		return info;
	}

	/**
	 * 根据电子账户查询个人活期结算户信息（不带状态）
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct getKnaAcctFirstOdb12(
			String custac, Boolean isable) {
		KnaAcct acct = KnaAcctDao.selectFirst_odb12(custac, isable);
		IoDpKnaAcct knaAcct = null;

		if (CommUtil.isNotNull(acct)) {
			knaAcct = SysUtil.getInstance(IoDpKnaAcct.class);
			acct.setOnlnbl(DpAcctProc.getAcctBalance(acct));
			CommUtil.copyProperties(knaAcct, acct);
		}

		return knaAcct;
	}

	/**
	 * 根据交易信息和产品代码查询总账产品核算表
	 * 
	 */
	public Long getKnsProdClerOdb1(String prodcd, String eventp) {
		Long prodClerCount = DpProductDao.selectOne_kns_prod_cler(prodcd,
				eventp, false);
		return prodClerCount;
	}

	/**
	 * 更新积数
	 * 
	 */
	public void updateKnbAcinOdb1(
			final cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbAcin dpKnbAcin) {
		KnbAcin tblknaacin = SysUtil.getInstance(KnbAcin.class);
		CommUtil.copyProperties(tblknaacin, dpKnbAcin);
		KnbAcinDao.updateOne_odb1(tblknaacin);
	}

	/**
	 * 根据交易流水查询账户余额发生明细
	 * 
	 */
	public Options<IoDpKnlBill> getKnlBillOdb1(
			String transq, String trandt) {
		List<IoDpKnlBill> lstKnlBill = DpAcctDao.selKnlBillByTransq(transq,
				trandt, false);
		@SuppressWarnings("unchecked")
		Options<IoDpKnlBill> info = SysUtil.getInstance(Options.class);
		for (IoDpKnlBill knlBill : lstKnlBill) {
			IoDpKnlBill dpKnlBill = SysUtil.getInstance(IoDpKnlBill.class);
			CommUtil.copyProperties(dpKnlBill, knlBill);
			info.add(dpKnlBill);
		}
		return info;
	}

	/**
	 * 根据交易流水查询账户余额发生明细历史表
	 * 
	 */
	public Options<IoDpHKnlBill> getHKnlBillOdb1(
			String transq, String trandt) {
		List<IoDpHKnlBill> lstHKnlBil = DpAcctDao.selHknlBillByTransq(transq,
				trandt, false);
		@SuppressWarnings("unchecked")
		Options<IoDpHKnlBill> info = SysUtil.getInstance(Options.class);
		for (IoDpHKnlBill hKnlBill : lstHKnlBil) {
			IoDpHKnlBill dpHKnlBill = SysUtil.getInstance(IoDpHKnlBill.class);
			CommUtil.copyProperties(dpHKnlBill, hKnlBill);
			info.add(dpHKnlBill);
		}
		return info;
	}

	/**
	 * 查询产品的计息基础
	 * 
	 */

	@Override
	public E_INBEBS getKupDppbIntrOdb1(String corpno, String prodcd, String crcycd,
			Boolean isable) {
		KupDppbIntr info = ProintrSelDao.selProdIntrByProdno(corpno, prodcd, crcycd, isable);
		return info.getTxbebs();
	}

	/**
	 * 根据交易流水查询大小额往来账明细
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.InknlcnapotDetl getKnlCnapotByTransqOdb1(
			String transq) {
		if (CommUtil.isNull(transq)) {
			throw DpModuleError.DpstComm.BNAS0050();
		}

		InknlcnapotDetl knlcnapotDetl = DpAcctQryDao.selKnlCnapotbytransq(
				transq, false);

		return knlcnapotDetl;
	}

	/**
	 * 根据主交易流水查询电子账户出入金明细
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlIobl getKnlIoblOdb2(
			String transq) {
		if (CommUtil.isNull(transq)) {
			throw DpModuleError.DpstComm.BNAS0050();
		}

		IoKnlIobl knlIobl = DpAcctQryDao.selKnlIoblbyTransq(transq, false);

		return knlIobl;
	}

	/**
	 * 根据交易流水查询电子账户消费明细
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.ioKnlSpnd getKnlSpndOdb1(
			String transq) {
		if (CommUtil.isNull(transq)) {
			throw DpModuleError.DpstComm.BNAS0050();
		}

		ioKnlSpnd knlSpnd = DpAcctDao.selKnlSpndByTransq(transq, false);
			
		return knlSpnd;
	}

	/**
	 * 根据交易流水查询电子账户转电子账户登记簿
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlCary getKnlCaryOdb1(
			String transq) {
		if (CommUtil.isNull(transq)) {
			throw DpModuleError.DpstComm.BNAS0050();
		}
		IoKnlCary knlCary = DpAcctDao.selKnlCaryByTransq(transq, false);
		return knlCary;
	}

	/**
	 * 查询目录树信息
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnbParaMenuInfo getKnbParaMenuOdb1(
			String codevl) {
		IoKnbParaMenuInfo menu = DpAcctDao.selKnbParaMenuByCodevl(codevl, true);
		return menu;
	}
}
