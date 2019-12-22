package cn.sunline.ltts.busi.dp.serviceimpl;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUPSST;
import cn.sunline.edsp.busi.ds.iobus.servicetype.ds.IoDsManage;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.acct.DpCloseAcctno;
import cn.sunline.ltts.busi.dp.acct.OpenSubAcctDeal;
import cn.sunline.ltts.busi.dp.base.DpPublicServ;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.client.CapitalTransLsamStrike;
import cn.sunline.ltts.busi.dp.client.CapitalTransPayStrike;
import cn.sunline.ltts.busi.dp.domain.DpSaveEntity;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpFrozDao;
import cn.sunline.ltts.busi.dp.namedsql.DpStrikeSqlDao;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbInrtAdjt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbInrtAdjtDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozAcctDetl;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozAcctDetlDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetl;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetlDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwne;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwneDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBill;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBillDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmContro;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmControDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.InknlcnapotDetl;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlIobl;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.ioKnlSpnd;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.QryDpAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKcdCard;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbClac;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcCloseStrikeInput;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcDrawStrikeInput;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcSaveStrikeInput;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.SvError.StrikeError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SLEPST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_ADJTTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSTAT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DCMTST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_STATUS;

/**
 * 负债相关冲正服务实现
 *
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoDpStrikeSvcTypeImpl", longname = "负债相关冲正服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoDpStrikeSvcTypeImpl implements cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType {

	private static final BizLog bizlog = BizLogUtil.getBizLog(IoDpStrikeSvcTypeImpl.class);

	/**
	 * 销户冲正
	 *
	 */
	public void procCloseStrike(
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType.ProcCloseStrike.Input input) {
		bizlog.debug("销户冲正业务处理开始======================================");
		String tmstmp = DateTools2.getCurrentTimestamp();

		ProcCloseStrikeInput cplStrikeIn = input.getStrikeInput();
		// 冲账检查：不允许隔日冲销户
		if (CommUtil.compare(CommTools.getBaseRunEnvs().getTrxn_date(), cplStrikeIn.getOrtrdt()) != 0) {
			throw StrikeError.E0001();
		}
		IoCaSevQryTableInfo qryCaTable = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaCust cplKnaCust = qryCaTable.getKnaCustByCustacOdb1(cplStrikeIn.getCustac(), true);
		// 1.账户状态必须销户
		if (cplKnaCust.getAcctst() != E_ACCTST.CLOSE) {
			throw DpModuleError.DpstComm.BNAS1708(cplStrikeIn.getCustac());
		}
		// 2.卡状态必须销户
		IoCaKcdCard cplKcdCard = qryCaTable.getKcdCardOdb1(cplStrikeIn.getCardno(), true);
		if (cplKcdCard.getDcmtst() != E_DCMTST.CLOSED) {
			throw DpModuleError.DpstComm.BNAS1709(cplStrikeIn.getCardno());
		}

		String custac = cplStrikeIn.getCustac();
		String closdt = cplStrikeIn.getOrtrdt();
		String clossq = cplStrikeIn.getClossq();

		// 电子账号与子账户关联关系
		DpAcctDao.updKnaAccsAcctstByCLosInfo(E_DPACST.NORMAL, tmstmp, closdt, clossq);

		// 1.销户所有负债子账户
		DpAcctDao.updKnaAcctAcctstByCustacStrik(custac, E_DPACST.NORMAL, cplStrikeIn.getOrtrdt(),
				cplStrikeIn.getClossq(), tmstmp); // 活期
		DpAcctDao.updKnaFxacAcctstByCustacStrik(custac, E_DPACST.NORMAL, cplStrikeIn.getOrtrdt(),
				cplStrikeIn.getClossq(), tmstmp); // 定期
		// xiejun 20161220 目前没有保险和基金

		// DpAcctDao.updFdaFundAcctstByCustac(custac, E_ACCTST.NORMAL,trandt,tmstmp); //
		// 基金

		// DpAcctDao.updDfaHoldAcctstByCustac(custac, E_ACCTST.NORMAL, E_PRODTP.INSU);
		// //保险
		// 其他子户 TODO

		// 2.销户卡、电子账户
		// 电子账户
		E_ACCTST acctst = cplStrikeIn.getAcctst(); // 原电子账户状态
		cplKnaCust.setAcctst(acctst);
		// modify by songkl 原状态为结清的不冲销户日期和销户流水
		if (acctst != E_ACCTST.SETTLE) {
			cplKnaCust.setClosdt("");
			cplKnaCust.setClossq("");
		}
		qryCaTable.updateKnaCustOdb1(cplKnaCust);
		// 卡
		cplKcdCard.setDcmtst(E_DCMTST.NORMAL);
		qryCaTable.updateKcdCardOdb1(cplKcdCard);

		// 3.销户关联关系
		// 卡帐关联关系
		DpAcctDao.updKnaAcdcAcctstByCustac(custac, E_DPACST.NORMAL, tmstmp);
		// 外卡与电子账户绑定
		DpAcctDao.updKnaCacdAcctstByClossq(cplStrikeIn.getClossq(), E_DPACST.NORMAL, tmstmp);

		// 2017/1/17 add by songlw 增加关联表和别名状态冲正
		// 4.用户关联关系表 cif_cust_accs
		String custid = qryCaTable.getKnaCuadOdb1(custac, true).getCustid();
		DpAcctDao.updCifCustAccsByCustac(custid);
		// 5.账户别名表kna_acal
		IoCaKnbClac clac = qryCaTable.getKnbClacOdb1(cplStrikeIn.getOrcssq(), false);
		if (CommUtil.isNotNull(clac)) {
			DpAcctDao.updKnaAcalByAcalno(clac.getAcalno(), custac, tmstmp, E_ACALST.NORMAL);
		}

		bizlog.debug("销户冲正业务处理结束======================================");
	}

	/**
	 * 存入冲正
	 *
	 */
	public void procSaveStrike(
			final cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcSaveStrikeInput strikeInput) {
		DpSaveEntity entity = new DpSaveEntity();
		// ProcSaveStrikeInput param = input.getStrikeInput();
		entity.setAcctno(strikeInput.getAcctno());
		entity.setCustac(strikeInput.getCustac());
		entity.setOramnt(strikeInput.getAmntcd());
		entity.setColrfg(strikeInput.getColrfg());
		entity.setCrcycd(strikeInput.getCrcycd());
		entity.setDetlsq(strikeInput.getDetlsq());
		entity.setOrtrdt(strikeInput.getOrtrdt());
		entity.setStacps(strikeInput.getStacps());
		entity.setTranam(strikeInput.getTranam());

		DpPublicServ.strikePostAcctDp(entity);
	}

	/**
	 * 支取冲正
	 *
	 */
	public void procDrawStrike(final ProcDrawStrikeInput input) {
		
		DpSaveEntity entity = new DpSaveEntity();
		//ProcDrawStrikeInput param = input.getStrikeInput();
		entity.setAcctno(input.getAcctno());
		entity.setCustac(input.getCustac());
		entity.setOramnt(input.getAmntcd());
		entity.setColrfg(input.getColrfg());
		entity.setCrcycd(input.getCrcycd());
		entity.setDetlsq(input.getDetlsq());
		entity.setOrtrdt(input.getOrtrdt());
		entity.setStacps(input.getStacps());
		entity.setTranam(input.getTranam());
		entity.setInstam(input.getInstam()); // 支取利息
		entity.setIntxam(input.getIntxam()); // 利息税
		entity.setAcctst(input.getAcctst()); // 账户状态
		entity.setPyafamount(input.getPyafam()); // 追缴金额
		entity.setPydetlsq(input.getPydlsq());

		DpPublicServ.strikeDrawAcctDp(entity);
	}

	/**
	 * 出入金登记簿冲正
	 */
	@Override
	public void procSaveIoBillStrike(String fronsq, String frondt, E_TRANST status) {

		String mtdate = CommTools.getBaseRunEnvs().getTrxn_date();
		String tmstmp = DateTools2.getCurrentTimestamp();
		String revrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		DpStrikeSqlDao.strikeKnlIoblStatus(fronsq, frondt, status, revrsq, mtdate, tmstmp);

	}

	@Override
	public void procKnlCaryStrike(String fronsq, String frondt, E_TRANST status) {
		String mtdate = CommTools.getBaseRunEnvs().getTrxn_date();
		String tmstmp = DateTools2.getCurrentTimestamp();
		String revrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		DpStrikeSqlDao.strikeKnlCaryStatus(fronsq, frondt, status, revrsq, mtdate, tmstmp);
	}

	/**
	 * 电子账户消费登记簿冲正
	 */
	@Override
	public void procAccConStrike(String fronsq, String frondt, E_TRANST status) {

		String mtdate = CommTools.getBaseRunEnvs().getTrxn_date();
		String tmstmp = DateTools2.getCurrentTimestamp();
		String revrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		DpStrikeSqlDao.strikeKnlSpndStatus(fronsq, frondt, status, revrsq, mtdate, tmstmp);

	}

	@Override
	public void procOpenSubAcctStrike(String custac, String acctno, String retrdt, String retrsq, E_FCFLAG fcflag) {

		OpenSubAcctDeal.strikeOpenSubAcct(custac, acctno, retrdt, retrsq, fcflag);

	}

	@Override
	public void procCloseAcctStrike(IoApRegBook strike, E_COLOUR colour) {

		DpCloseAcctno.strikeCloseAcctno(strike, colour);
	}

	@Override
	public void procInstAdjustStrike(String transq, String trandt) {

		KnbInrtAdjt tbladjt = SysUtil.getInstance(KnbInrtAdjt.class);

		tbladjt = KnbInrtAdjtDao.selectOne_odb1(trandt, transq, true);

		E_ADJTTP adjttp = tbladjt.getAdjttp();
		// 查询调息负债账户信息
		QryDpAcctOut acctInfo = SysUtil.getInstance(DpProdSvcType.class).QryDpByAcctno(tbladjt.getAcctno());

		if (E_ADJTTP.ADD == adjttp) {
			// 调增
			// 登记会计流水
			IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
			cplIoAccounttingIntf.setProdcd(acctInfo.getProdcd());// 产品
			cplIoAccounttingIntf.setDtitcd(acctInfo.getAcctcd());
			cplIoAccounttingIntf.setCrcycd(acctInfo.getCrcycd());
			cplIoAccounttingIntf.setTranam(tbladjt.getIrstjt().negate());// 冲正红字
			cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
			cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
			cplIoAccounttingIntf.setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch());
			cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR);
			cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
			cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
			cplIoAccounttingIntf.setBltype(E_BLTYPE.PYIN);
			cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
			// 登记交易信息，供总账解析
			if (CommUtil.equals("1",
					KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%", true).getParm_value1())) {
				KnpParameter para = SysUtil.getInstance(KnpParameter.class);
				para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%", "%", true);
				cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息 结息
			}
			SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);
		} else {

			// 调减
			// 登记会计流水
			IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
			cplIoAccounttingIntf.setProdcd(acctInfo.getProdcd());
			cplIoAccounttingIntf.setDtitcd(acctInfo.getAcctcd());
			cplIoAccounttingIntf.setCrcycd(acctInfo.getCrcycd());
			cplIoAccounttingIntf.setTranam(tbladjt.getIrstjt()); // 结息-蓝字调减应付利息
			cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
			cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
			cplIoAccounttingIntf.setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch());
			cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR);
			cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
			cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
			cplIoAccounttingIntf.setBltype(E_BLTYPE.PYIN);
			cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
			// 登记交易信息，供总账解析
			if (CommUtil.equals("1",
					KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%", true).getParm_value1())) {
				KnpParameter para = SysUtil.getInstance(KnpParameter.class);
				para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%", "%", true);
				cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息 结息
			}
			SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);
		}

		tbladjt.setStatus(E_STATUS.CZ);
		KnbInrtAdjtDao.updateOne_odb1(tbladjt);

	}

	@Override
	public void procKnbSlepStrike(String trandt, String trantm, String custac, E_SLEPST slepst) {

		CapitalTransDeal.strikeKnbSlep(custac, trandt, trantm, slepst);
	}

	@Override
	public void procKnbClacStrike(String clossq, String custac, E_CLSTAT clstat) {

		String tmstmp = DateTools2.getCurrentTimestamp();
		DpAcctDao.updKnbClacStatBySeq(E_CLSTAT.FAIL, clossq, tmstmp);
	}

	@Override
	public void procKnlIoblCupsStrike(String mntrsq, String trandt, E_YES___ istrcf) {
		DpStrikeSqlDao.strikeKnlIoblCups(mntrsq, trandt, E_CUPSST.STRK);

		if (istrcf == E_YES___.YES) {
			DpStrikeSqlDao.StrikeDelKnsTranError(mntrsq, trandt);
		}

	}

	@Override
	public void procCnapreStrikeHold(InknlcnapotDetl knlCnapot, String errmes) {

		CapitalTransLsamStrike.prcLsamStrikeHold(knlCnapot, errmes);
	}

	@Override
	public void procCnapreStrikeDieb(InknlcnapotDetl knlCnapot) {

		CapitalTransLsamStrike.prcLsamStrikeDieb(knlCnapot);
	}

	@Override
	public void procStrkpyIobl(IoKnlIobl iobl, E_IOFLAG ioflag, String errmsg) {

		CapitalTransPayStrike.procStrkpyIobl(iobl, ioflag, errmsg);
	}

	@Override
	public void procStrkpySpnd(ioKnlSpnd spnd, E_IOFLAG ioflag, String errmsg) {

		CapitalTransPayStrike.procStrkpySpnd(spnd, ioflag, errmsg);
	}

	// 订单冲正
	@Override
	public void ProcRegi(String cordno) {

		if (CommUtil.isNotNull(cordno)) {
			// 更新登记簿状态
			DpStrikeSqlDao.strikeKnbRegi(cordno);
		}

	}

	/**
	 * 清算冲正
	 */
	@Override
	public void ProcStrAcqsCler(String mntrsq, String trandt) {

		DpStrikeSqlDao.strikeKnsAcsqClerTranst(mntrsq, trandt, E_TRANST.STRIKED);

	}

	@Override
	public void ProAcctinStrike(String flowid, String brchno, String crcycd) {

		// 查询原流水
		KnlIoblCups tblKnlIoblCupsRe = KnlIoblCupsDao.selectOne_odb3(flowid, false);

		if (CommUtil.isNull(tblKnlIoblCupsRe)) {// 判断原交易流水是否存在
			throw DpModuleError.DpTrans.TS020017();
		}

		// 业务编码查询赋值
		KnpParameter knpParameterBusino = KnpParameterDao.selectOne_odb1("DP.ACCTIN",
				tblKnlIoblCupsRe.getServtp().getId(), "%", "%", false);
		if (CommUtil.isNull(knpParameterBusino)) {
			throw DpModuleError.DpTrans.TS020021();
		}

		// 初始化内部户记账服务
		IoInAccount ioInAccount = SysUtil.getInstance(IoInAccount.class);
		IaAcdrInfo iaTransOutPro = SysUtil.getInstance(InacTransComplex.IaAcdrInfo.class);

		// 内部户借方记账 待清算反向处理
		iaTransOutPro.setBusino(knpParameterBusino.getParm_value1());
		iaTransOutPro.setAcbrch(brchno);
		iaTransOutPro.setCrcycd(tblKnlIoblCupsRe.getCrcycd());
		iaTransOutPro.setTranam(tblKnlIoblCupsRe.getTranam());

		ioInAccount.ioInAccr(iaTransOutPro);

		// 银联成本手续费反向处理
		iaTransOutPro.setBusino(knpParameterBusino.getParm_value1());
		iaTransOutPro.setAcbrch(brchno);
		iaTransOutPro.setCrcycd(tblKnlIoblCupsRe.getCrcycd());
		iaTransOutPro.setTranam(tblKnlIoblCupsRe.getChrgam());

		ioInAccount.ioInAcdr(iaTransOutPro);

		// 业务编码查询赋值 （抵扣金额处理）
		KnpParameter knpParameterSbrand = KnpParameterDao.selectOne_odb1("DP.ACCTIN", tblKnlIoblCupsRe.getOrgaid(), "%",
				"%", false);
		if (CommUtil.isNull(knpParameterSbrand)) {
			throw DpModuleError.DpTrans.TS020021();
		}

		// 分润计算服务调用冲正
		IoDsManage ioDsManage = SysUtil.getInstance(IoDsManage.class);
		ioDsManage.rollbkCalProfit(tblKnlIoblCupsRe.getTrandt(), tblKnlIoblCupsRe.getMntrsq());

		if (CommUtil.equals(E_YES___.NO.getValue(), tblKnlIoblCupsRe.getFrodfg().getValue())) {

			BigDecimal Jnanam = tblKnlIoblCupsRe.getJnanam().subtract(tblKnlIoblCupsRe.getChrgam()).add(tblKnlIoblCupsRe.getCoupon());
			if (!CommUtil.equals(Jnanam, BigDecimal.ZERO)) {
				// 分润费用红字处理
				IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
				cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
				cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
				cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
				cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR);
				cplIoAccounttingIntf.setAcctno(knpParameterSbrand.getParm_value1());
				cplIoAccounttingIntf.setCuacno(knpParameterSbrand.getParm_value1());
				cplIoAccounttingIntf.setAcseno(knpParameterSbrand.getParm_value1());
				cplIoAccounttingIntf.setProdcd(knpParameterSbrand.getParm_value3());// 内部户产品
				cplIoAccounttingIntf.setDtitcd(knpParameterSbrand.getParm_value3());
				cplIoAccounttingIntf.setCrcycd(tblKnlIoblCupsRe.getCrcycd());
				cplIoAccounttingIntf.setAcctbr(brchno);
				cplIoAccounttingIntf.setTranam(Jnanam.negate());
				cplIoAccounttingIntf.setTobrch(brchno);
				cplIoAccounttingIntf.setAtowtp(E_ATOWTP.IN);
				cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
				cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE);
				cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
				cplIoAccounttingIntf.setCorpno(tblKnlIoblCupsRe.getCorpno());

				IoSaveIoTransBill ioSaveIoTransBillf = SysUtil.getInstance(IoSaveIoTransBill.class);
				ioSaveIoTransBillf.SaveKnlAcsq(cplIoAccounttingIntf, E_BLNCDN.get(knpParameterSbrand.getParm_value2()));
			}

			if (!CommUtil.equals(tblKnlIoblCupsRe.getVoucfe(), BigDecimal.ZERO)) {
				// 活动抵扣金额处理
				IoAccounttingIntf cplIoAccounttingInth = SysUtil.getInstance(IoAccounttingIntf.class);
				cplIoAccounttingInth.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
				cplIoAccounttingInth.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
				cplIoAccounttingInth.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
				cplIoAccounttingInth.setAmntcd(E_AMNTCD.CR);
				cplIoAccounttingInth.setAcctno(knpParameterSbrand.getParm_value1());
				cplIoAccounttingInth.setCuacno(knpParameterSbrand.getParm_value1());
				cplIoAccounttingInth.setAcseno(knpParameterSbrand.getParm_value1());
				cplIoAccounttingInth.setProdcd(knpParameterSbrand.getParm_value3());// 内部户产品
				cplIoAccounttingInth.setDtitcd(knpParameterSbrand.getParm_value3());
				cplIoAccounttingInth.setCrcycd(crcycd);
				cplIoAccounttingInth.setAcctbr(brchno);
				cplIoAccounttingInth.setTranam(tblKnlIoblCupsRe.getCoupon());
				cplIoAccounttingInth.setTobrch(brchno);
				cplIoAccounttingInth.setAtowtp(E_ATOWTP.IN);
				cplIoAccounttingInth.setTrsqtp(E_ATSQTP.ACCOUNT);
				cplIoAccounttingInth.setBltype(E_BLTYPE.BALANCE);
				cplIoAccounttingInth.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
				cplIoAccounttingInth.setCorpno(tblKnlIoblCupsRe.getCorpno());

				IoSaveIoTransBill ioSaveIoTransBillh = SysUtil.getInstance(IoSaveIoTransBill.class);
				ioSaveIoTransBillh.SaveKnlAcsq(cplIoAccounttingInth, E_BLNCDN.get(knpParameterSbrand.getParm_value2()));
			}

			ProcSaveStrikeInput procSaveStrikeInput = SysUtil.getInstance(ProcSaveStrikeInput.class);

			KnlBill knlBill = KnlBillDao.selectOne_odb3(tblKnlIoblCupsRe.getMntrsq(), false);

			if (CommUtil.isNotNull(knlBill)) {
				procSaveStrikeInput.setAcctno(knlBill.getAcctno());
				procSaveStrikeInput.setAmntcd(knlBill.getAmntcd());
				procSaveStrikeInput.setColrfg(E_COLOUR.RED);
				procSaveStrikeInput.setCrcycd(knlBill.getTrancy());
				procSaveStrikeInput.setCustac(knlBill.getCustac());
				procSaveStrikeInput.setDetlsq(knlBill.getDetlsq());
				procSaveStrikeInput.setOrtrdt(knlBill.getTrandt());
				procSaveStrikeInput.setStacps(E_STACPS.POSITIVE);
				procSaveStrikeInput.setTranam(knlBill.getTranam());

				IoDpStrikeSvcType ioDpStrikeSvcType = SysUtil.getInstance(IoDpStrikeSvcType.class);

				ioDpStrikeSvcType.procSaveStrike(procSaveStrikeInput);
			}

		}

		tblKnlIoblCupsRe.setTranst(E_CUPSST.STRK);

		KnlIoblCupsDao.updateOne_odb2(tblKnlIoblCupsRe);

		KnlIoblEdmContro knlIoblEdmContro = KnlIoblEdmControDao.selectOne_edmOdb01(tblKnlIoblCupsRe.getMntrsq(),
				tblKnlIoblCupsRe.getTrandt(), false);

		knlIoblEdmContro.setTranst(E_CUPSST.STRK);

		KnlIoblEdmControDao.updateOne_edmOdb01(knlIoblEdmContro);

		// 平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(),
				CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._35);

	}

	/**
	 * 冻结冲正
	 */
	@Override
	public void proFrozStrike(String frozno) {
		KnbFroz tblKnbFroz = KnbFrozDao.selectOne_odb1(frozno, E_FROZST.VALID, false);
		if (CommUtil.isNotNull(tblKnbFroz)) {
			tblKnbFroz.setFrozst(E_FROZST.INVALID);
			tblKnbFroz.setUnfrdt(CommTools.getBaseRunEnvs().getTrxn_date());
			KnbFrozDao.updateOne_odb2(tblKnbFroz);
		}
		
		KnbFrozDetl tblKnbFrozDetl = KnbFrozDetlDao.selectFirst_odb1(frozno, false);
		if (CommUtil.isNotNull(tblKnbFrozDetl)) {
			tblKnbFrozDetl.setFrozst(E_FROZST.INVALID);
			tblKnbFrozDetl.setFrozbl(BigDecimal.ZERO);
			KnbFrozDetlDao.updateOne_odb2(tblKnbFrozDetl);
		}
		
		if (E_YES___.YES == tblKnbFroz.getFricfg()) {
			List<KnbFrozAcctDetl> lstKnbFrozAcctDetl = KnbFrozAcctDetlDao.selectAll_odb2(frozno, false);
			if (!lstKnbFrozAcctDetl.isEmpty()) {
				for (KnbFrozAcctDetl tblKnbFrozAcctDetl : lstKnbFrozAcctDetl) {
					tblKnbFrozAcctDetl.setFzrmam(BigDecimal.ZERO);
					KnbFrozAcctDetlDao.updateOne_odb1(tblKnbFrozAcctDetl);
					
					KnbFrozOwne tblKnbFrozOwne = 
							KnbFrozOwneDao.selectOne_odb1(tblKnbFroz.getFrozow(), tblKnbFrozAcctDetl.getFrowid(), false);
					tblKnbFrozOwne.setFrozbl(tblKnbFrozOwne.getFrozbl().subtract(tblKnbFrozAcctDetl.getFrozam()));
					KnbFrozOwneDao.updateOne_odb1(tblKnbFrozOwne);
				}
			}
			
		} else {
			KnbFrozOwne tblKnbFrozOwne = 
					KnbFrozOwneDao.selectOne_odb1(tblKnbFroz.getFrozow(), tblKnbFrozDetl.getFrowid(), false);
			if (E_FRLMTP.AMOUNT != tblKnbFroz.getFrlmtp()) {

				long count = DpFrozDao.selFrozCount(tblKnbFroz.getFrozow(), tblKnbFrozDetl.getFrowid(), tblKnbFroz.getFrlmtp(),
						E_FROZST.VALID, false);

				if (count == 0) {
					if (tblKnbFroz.getFrlmtp() == E_FRLMTP.ALL) // 全额冻结
						tblKnbFrozOwne.setFralfg(E_YES___.NO);
					else if (E_FRLMTP.IN == tblKnbFroz.getFrlmtp()) // 只付不收
						tblKnbFrozOwne.setFrinfg(E_YES___.NO);
					else if (E_FRLMTP.OUT == tblKnbFroz.getFrlmtp()) // 只收不付
						tblKnbFrozOwne.setFrotfg(E_YES___.NO);
				}

			} else {
				tblKnbFrozOwne.setFrozbl(tblKnbFrozOwne.getFrozbl().subtract(tblKnbFrozDetl.getFrozam()));
			}
			KnbFrozOwneDao.updateOne_odb1(tblKnbFrozOwne);
		}
	}

	/**
	 * 解冻冲正
	 */
	public void proUnfrStrike( String frozno,  java.math.BigDecimal frozam){
		KnbFroz tblKnbFroz = KnbFrozDao.selectFirst_odb4(frozno, false);
		if (CommUtil.isNotNull(tblKnbFroz) && E_FROZST.INVALID == tblKnbFroz.getFrozst()) {
			tblKnbFroz.setFrozst(E_FROZST.VALID);
			tblKnbFroz.setUnfrdt(null);
			KnbFrozDao.updateOne_odb2(tblKnbFroz);
		}
		
		KnbFrozDetl tblKnbFrozDetl = KnbFrozDetlDao.selectFirst_odb1(frozno, false);
		if (CommUtil.isNotNull(tblKnbFrozDetl)) {
			if (E_FROZST.INVALID == tblKnbFrozDetl.getFrozst()) {
				tblKnbFrozDetl.setFrozst(E_FROZST.VALID);
			}
			tblKnbFrozDetl.setFrozbl(tblKnbFrozDetl.getFrozbl().add(frozam));
			KnbFrozDetlDao.updateOne_odb2(tblKnbFrozDetl);
		}
		
		if (E_YES___.YES == tblKnbFroz.getFricfg()) {
			List<KnbFrozAcctDetl> lstKnbFrozAcctDetl = KnbFrozAcctDetlDao.selectAll_odb2(frozno, false);
			if (!lstKnbFrozAcctDetl.isEmpty()) {
				for (KnbFrozAcctDetl tblKnbFrozAcctDetl : lstKnbFrozAcctDetl) {
					if (CommUtil.compare(BigDecimal.ZERO, frozam) >= 0) {
						break;
					}
					
					KnbFrozOwne tblKnbFrozOwne = 
							KnbFrozOwneDao.selectOne_odb1(tblKnbFroz.getFrozow(), tblKnbFrozAcctDetl.getFrowid(), false);
					if (CommUtil.compare(tblKnbFrozAcctDetl.getFrozam().subtract(tblKnbFrozAcctDetl.getFzrmam()), frozam) <= 0) {
						tblKnbFrozOwne.setFrozbl(tblKnbFrozOwne.getFrozbl()
								.add(tblKnbFrozAcctDetl.getFrozam().subtract(tblKnbFrozAcctDetl.getFzrmam())));
						tblKnbFrozAcctDetl.setFzrmam(tblKnbFrozAcctDetl.getFrozam());
					} else {
						tblKnbFrozOwne.setFrozbl(tblKnbFrozOwne.getFrozbl().add(frozam));
						tblKnbFrozAcctDetl.setFzrmam(tblKnbFrozAcctDetl.getFzrmam().add(frozam));
						
					}
					KnbFrozOwneDao.updateOne_odb1(tblKnbFrozOwne);
					KnbFrozAcctDetlDao.updateOne_odb1(tblKnbFrozAcctDetl);
					
					frozam = frozam.subtract(tblKnbFrozAcctDetl.getFrozam().subtract(tblKnbFrozAcctDetl.getFzrmam()));
				}
			}	
		} else {
			KnbFrozOwne tblKnbFrozOwne = 
					KnbFrozOwneDao.selectOne_odb1(tblKnbFroz.getFrozow(), tblKnbFrozDetl.getFrowid(), false);
			if (E_FRLMTP.AMOUNT != tblKnbFroz.getFrlmtp()) {

				if (tblKnbFroz.getFrlmtp() == E_FRLMTP.ALL) // 全额冻结
					tblKnbFrozOwne.setFralfg(E_YES___.YES);
				else if (E_FRLMTP.IN == tblKnbFroz.getFrlmtp()) // 只付不收
					tblKnbFrozOwne.setFrinfg(E_YES___.YES);
				else if (E_FRLMTP.OUT == tblKnbFroz.getFrlmtp()) // 只收不付
					tblKnbFrozOwne.setFrotfg(E_YES___.YES);

			} else {
				tblKnbFrozOwne.setFrozbl(tblKnbFrozOwne.getFrozbl().add(frozam));
			}
			KnbFrozOwneDao.updateOne_odb1(tblKnbFrozOwne);
		}
	}

}
