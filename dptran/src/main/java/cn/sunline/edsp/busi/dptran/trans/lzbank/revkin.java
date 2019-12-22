package cn.sunline.edsp.busi.dptran.trans.lzbank;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.iobus.servicetype.risk.DpRiskService;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.toTransThaw;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUPSST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_FINSTY;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SERVCD;
import cn.sunline.edsp.busi.ds.iobus.servicetype.ds.IoDsManage;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.dp.base.DpTransfer;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBill;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBillDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsRe;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsReDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcSaveStrikeInput;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

public class revkin {

	public static void revokeDeal(final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Revkin.Input input,
			final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Revkin.Property property,
			final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Revkin.Output output) {
		// 取消无用判断
		// String mntrcq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		// String tranct = CommTools.getBaseRunEnvs().getTrxn_date();
		// KnlIoblCups cups = ActoacDao.selKnlIoblCupsDetl(tranct, mntrcq, false);
		// if (CommUtil.isNotNull(cups)) {
		// // 交易重复
		// throw DpModuleError.DpTrans.TS020055();
		// }

		// 服务编码
		E_SERVCD svcode = input.getSvcode();
		// 结算类型
		E_FINSTY finsty = input.getFinsty();

		if (!(CommUtil.equals(svcode.getValue(), E_SERVCD.PAY40001.getValue())
				|| CommUtil.equals(svcode.getValue(), E_SERVCD.PAY50004.getValue())
				|| CommUtil.equals(svcode.getValue(), E_SERVCD.PAYX0000.getValue()))) {
			throw DpModuleError.DpTrans.TS020040();
		}

		if (CommUtil.isNull(svcode)) {
			throw DpModuleError.DpTrans.TS010035();
		}

		if (CommUtil.isNull(finsty)) {
			throw DpModuleError.DpTrans.TS010028();
		}

		// if (!CommUtil.equals(finsty.getValue(), E_FINSTY.T1.getValue())) {
		// throw DpModuleError.DpTrans.TS020037();
		// }

		if (CommUtil.isNull(input.getReflid())) {
			throw DpModuleError.DpTrans.TS020038();
		}

		KnlIoblCups knlIoblCups = KnlIoblCupsDao.selectOneWithLock_odb3(input.getReflid(), false);

		if (CommUtil.isNull(knlIoblCups)) {
			throw DpModuleError.DpTrans.TS020017();
		}

		if (CommUtil.isNotNull(knlIoblCups)
				&& CommUtil.equals(knlIoblCups.getTranst().getValue(), E_CUPSST.CNCL.getValue())) {
			output.setIsresu(E_YES___.YES);
			return;
		}

		// 判断原流水交易类型是否是T1
		if (!CommUtil.equals(knlIoblCups.getAcfist().getValue(), E_FINSTY.T1.getValue())) {
			throw DpModuleError.DpTrans.TS020039();
		}

		if (!CommUtil.equals(E_CUPSST.SUCC.getValue(), knlIoblCups.getTranst().getValue())) {
			throw DpModuleError.DpTrans.TS020053(knlIoblCups.getTranst().getLongName());
		}

		// 当前交易日期
//		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();

		//判断反向交易psop日期是否相等
		if (!CommUtil.equals(knlIoblCups.getPrepdt(), input.getPrepdt())) {
			throw DpModuleError.DpTrans.TS020036();
		}

		KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(input.getInmeid(), false);
		if (CommUtil.isNull(tblKnaSbad)) {
			throw DpModuleError.DpTrans.TS020027();
		}

		KnaAcct knaAcct = KnaAcctDao.selectOne_odb1(tblKnaSbad.getAcctno(), false);
		if (CommUtil.isNull(knaAcct)) {// 品牌子户号对应不存在
			throw DpModuleError.DpTrans.TS020014();
		}
		//增加是否冻结判断
		if (CommUtil.equals(E_YES___.YES.getValue(), knlIoblCups.getFrozfg().getValue())) {
			// 针对调单冻结进行解冻
			DpRiskService dpRiskService = SysUtil.getInstance(DpRiskService.class);
			toTransThaw toTransThaw = SysUtil.getInstance(DpRiskType.toTransThaw.class);
			toTransThaw.setTdtrsq(knlIoblCups.getMntrsq());
			toTransThaw.setTdtrdt(knlIoblCups.getTrandt());
			toTransThaw.setCustac(tblKnaSbad.getCustac());
			toTransThaw.setTranam(knlIoblCups.getToanam());
			toTransThaw.setAcctno(knaAcct.getAcctno());
			toTransThaw.setCardno(knlIoblCups.getCardno());
			
            dpRiskService.transThaw(toTransThaw);
		}
		

		// 业务编码查询赋值
		KnpParameter knpParameterBusino = KnpParameterDao.selectOne_odb1("DP.ACCTIN", knlIoblCups.getServtp().getId(),
				"%", "%", false);
		if (CommUtil.isNull(knpParameterBusino)) {
			throw DpModuleError.DpTrans.TS020021();
		}

		// 初始化内部户记账服务
		IoInAccount ioInAccount = SysUtil.getInstance(IoInAccount.class);
		IaAcdrInfo iaTransOutPro = SysUtil.getInstance(InacTransComplex.IaAcdrInfo.class);

		// 内部户借方记账 待清算反向处理
		iaTransOutPro.setBusino(knpParameterBusino.getParm_value1());
		iaTransOutPro.setAcbrch(knaAcct.getBrchno());
		iaTransOutPro.setCrcycd(knlIoblCups.getCrcycd());
		iaTransOutPro.setTranam(knlIoblCups.getTranam());

		ioInAccount.ioInAccr(iaTransOutPro);

		if (!CommUtil.equals(knlIoblCups.getChrgam(), BigDecimal.ZERO)) {
			// 银联成本手续费反向处理
			iaTransOutPro.setBusino(knpParameterBusino.getParm_value1());
			iaTransOutPro.setAcbrch(knaAcct.getBrchno());
			iaTransOutPro.setCrcycd(knlIoblCups.getCrcycd());
			iaTransOutPro.setTranam(knlIoblCups.getChrgam());

			ioInAccount.ioInAcdr(iaTransOutPro);
		}
		// 业务编码查询赋值 （抵扣金额处理）
		KnpParameter knpParameterSbrand = KnpParameterDao.selectOne_odb1("DP.ACCTIN", tblKnaSbad.getSbrand(), "%", "%",
				false);
		if (CommUtil.isNull(knpParameterSbrand)) {
			throw DpModuleError.DpTrans.TS020021();
		}

		// 分润计算服务调用冲正
		IoDsManage ioDsManage = SysUtil.getInstance(IoDsManage.class);
		ioDsManage.rollbkCalProfit(knlIoblCups.getTrandt(), knlIoblCups.getMntrsq());

		if (CommUtil.equals(E_YES___.NO.getValue(), knlIoblCups.getFrodfg().getValue())) {

			BigDecimal Jnanam = knlIoblCups.getJnanam().subtract(knlIoblCups.getChrgam()).add(knlIoblCups.getCoupon());
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
				cplIoAccounttingIntf.setCrcycd(knlIoblCups.getCrcycd());
				cplIoAccounttingIntf.setAcctbr(knaAcct.getBrchno());
				cplIoAccounttingIntf.setTranam(Jnanam.negate());
				cplIoAccounttingIntf.setTobrch(knaAcct.getBrchno());
				cplIoAccounttingIntf.setAtowtp(E_ATOWTP.IN);
				cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
				cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE);
				cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
				cplIoAccounttingIntf.setCorpno(knlIoblCups.getCorpno());

				IoSaveIoTransBill ioSaveIoTransBillf = SysUtil.getInstance(IoSaveIoTransBill.class);
				ioSaveIoTransBillf.SaveKnlAcsq(cplIoAccounttingIntf, E_BLNCDN.get(knpParameterSbrand.getParm_value2()));
			}

			if (!CommUtil.equals(knlIoblCups.getVoucfe(), BigDecimal.ZERO)
					&& CommUtil.equals(knlIoblCups.getAcfist().getValue(), E_FINSTY.D0.getValue())) {
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
				cplIoAccounttingInth.setCrcycd(input.getCrcycd());
				cplIoAccounttingInth.setAcctbr(knaAcct.getBrchno());
				cplIoAccounttingInth.setTranam(knlIoblCups.getCoupon());
				cplIoAccounttingInth.setTobrch(knaAcct.getBrchno());
				cplIoAccounttingInth.setAtowtp(E_ATOWTP.IN);
				cplIoAccounttingInth.setTrsqtp(E_ATSQTP.ACCOUNT);
				cplIoAccounttingInth.setBltype(E_BLTYPE.BALANCE);
				cplIoAccounttingInth.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
				cplIoAccounttingInth.setCorpno(knlIoblCups.getCorpno());

				IoSaveIoTransBill ioSaveIoTransBillh = SysUtil.getInstance(IoSaveIoTransBill.class);
				ioSaveIoTransBillh.SaveKnlAcsq(cplIoAccounttingInth, E_BLNCDN.get(knpParameterSbrand.getParm_value2()));
			}

			ProcSaveStrikeInput procSaveStrikeInput = SysUtil.getInstance(ProcSaveStrikeInput.class);

			KnlBill knlBill = KnlBillDao.selectOne_odb3(knlIoblCups.getMntrsq(), false);

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

		// 平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(),
				CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._35);

		if (CommUtil.equals(svcode.getValue(), E_SERVCD.PAY40001.getValue())
				|| CommUtil.equals(svcode.getValue(), E_SERVCD.PAY50004.getValue())) {
			knlIoblCups.setTranst(E_CUPSST.CNCL);
		} else {
			knlIoblCups.setTranst(E_CUPSST.STRK);
		}

		knlIoblCups.setRevkst("SUCC");
		KnlIoblCupsDao.updateOne_odb2(knlIoblCups);

		KnlIoblCupsRe knlIoblCupsRe = SysUtil.getInstance(KnlIoblCupsRe.class);

		CommUtil.copyProperties(knlIoblCupsRe, input);
		knlIoblCupsRe.setTranst(E_CUPSST.SUCC);

		knlIoblCupsRe.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 主交易流水
		knlIoblCupsRe.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
		knlIoblCupsRe.setMntrtm(BusiTools.getTraxTmInRunEnv());
		knlIoblCupsRe.setAcctno(knlIoblCups.getAcctno());
		knlIoblCupsRe.setInmeid(knlIoblCups.getInmeid());
		knlIoblCupsRe.setJnanam(knlIoblCups.getJnanam());
		knlIoblCupsRe.setChrgam(knlIoblCups.getChrgam());
		knlIoblCupsRe.setProccd(CommTools.getBaseRunEnvs().getTrxn_code());// 处理码
		knlIoblCupsRe.setToanam(knlIoblCups.getToanam());
		knlIoblCupsRe.setServtp(knlIoblCups.getServtp());
		knlIoblCupsRe.setCoupon(knlIoblCups.getCoupon());
		knlIoblCupsRe.setCardno(knlIoblCups.getCardno());
		knlIoblCupsRe.setBusitp(DpTransfer.svcodeToBusitp(input.getSvcode()));//更改服务类型转换
		knlIoblCupsRe.setInmena(knlIoblCups.getInmena());
		knlIoblCupsRe.setMercfg(knlIoblCups.getMercfg());
		knlIoblCupsRe.setTmpabkno(knlIoblCups.getTmpabkno());
		knlIoblCupsRe.setChantp(knlIoblCups.getChantp());

		KnlIoblCupsReDao.insert(knlIoblCupsRe);

	}
}