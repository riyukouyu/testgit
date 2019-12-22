
package cn.sunline.ltts.busi.dptran.trans.lzbank;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.iobus.servicetype.dp.UnionPayCostSvcType;
import cn.sunline.edsp.busi.dp.iobus.type.dp.DpdebitAcctnos.UnionCastInfo;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CARDTP;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUPSST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_FINSTY;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SERVCD;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SETTST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_TLEWAY;
import cn.sunline.edsp.busi.ds.iobus.servicetype.ds.IoDsManage;
import cn.sunline.edsp.busi.ds.iobus.type.DsComplex.calMechAmtInput;
import cn.sunline.edsp.busi.ds.iobus.type.DsComplex.calMechAmtOutput;
import cn.sunline.edsp.busi.ds.iobus.type.DsEnumType.E_BUSITP;
import cn.sunline.edsp.busi.ds.iobus.type.DsEnumType.E_INTYPE;
import cn.sunline.edsp.busi.ds.iobus.type.DsEnumType.E_MECHTP;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

public class erorhd {

	public static void dealErorhd(final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Input input,
			final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Property property, final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Output output) {
		String inmeid = input.getInmeid();
		BigDecimal tranam =input.getTranam();
		String flowid = input.getFlowid();
		if (CommUtil.isNull(inmeid)) {// 内部商户号
			throw DpModuleError.DpTrans.TS020013();
		}
		if (CommUtil.compare(tranam, BigDecimal.ZERO) <= 0) { // 校验交易金额
			throw DpModuleError.DpstComm.BNAS0627();
		}
		if (CommUtil.isNull(flowid)) {
			throw DpModuleError.DpTrans.TS020041();
		}
		KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(inmeid, false);
		if (CommUtil.isNotNull(tblKnaSbad)) {
			// 查询卡号和负债子账号、户名
			KnaAcdc knaAcdc = KnaAcdcDao.selectFirst_odb3(tblKnaSbad.getCustac(), false);
			property.setCardno(knaAcdc.getCardno());
			property.setInacct(tblKnaSbad.getAcctno());
			KnaAcct knaAcct = KnaAcctDao.selectOne_odb1(tblKnaSbad.getAcctno(), false);
			property.setInacna(knaAcct.getAcctna());
			property.setBrchno(knaAcct.getBrchno());
			property.setSbrand(tblKnaSbad.getSbrand());
			property.setCustac(knaAcct.getCustac());
			KnlIoblCups entity = SysUtil.getInstance(KnlIoblCups.class);
			property.setKnlioblcups(entity);
			// 设置交易时间
			property.setTrantm(BusiTools.getTraxTmInRunEnv());
		}else {
			throw DpModuleError.DpTrans.TS020027();
		}
		//查询原交易流水
		KnlIoblCups knlIoblCups = KnlIoblCupsDao.selectOneWithLock_odb3(flowid, false);
		if (CommUtil.isNotNull(knlIoblCups)) {
			if (CommUtil.equals(E_CUPSST.FAIL.getValue(), knlIoblCups.getTranst().getValue())) {
				// 设置交易是否存在
				property.setIsexit(E_YES___.YES);
				property.setEdflge(E_YES___.NO);
				property.setCuanam(knlIoblCups.getToanam());// 设置商户存入金额
				property.setDtrsnt(E_YES___.YES);
				property.setPrcsfg(E_YES___.YES);
				property.setFrodfg(knlIoblCups.getFrodfg());
				property.setKnlioblcups(knlIoblCups);
				if(!CommUtil.equals(inmeid, knlIoblCups.getInmeid())) {
					throw DpModuleError.DpTrans.TS020062();
				}
				if(!CommUtil.equals(tranam, knlIoblCups.getTranam())) {
					throw DpModuleError.DpTrans.TS020063();
				}
			}else {
				throw DpModuleError.DpTrans.TS020061();
			}
		}else {
			throw DpModuleError.DpTrans.TS010002();
		}
		String tleway = knlIoblCups.getTleway().getValue();
		if ((CommUtil.isNotNull(tleway) && CommUtil.equals(tleway, E_TLEWAY.ZJ.getValue()))) {
			property.setRgflfg(E_YES___.YES);
			return;
		}
		// 业务编码查询赋值
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", knlIoblCups.getServtp().getId(), "%", "%", false);
		if (CommUtil.isNull(knpParameter)) {
			throw DpModuleError.DpTrans.TS020021();
		}
		// 待清算业务编码
		property.setBusino(knpParameter.getParm_value1());

	}
	
	/**
	 * 待清算户口转出
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void ioInAcdrAdm(final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Property property){
		KnlIoblCups cups = property.getKnlioblcups();
		IaAcdrInfo iaacdrinfo = SysUtil.getInstance(IaAcdrInfo.class);
		iaacdrinfo.setAcbrch(property.getBrchno());
		iaacdrinfo.setBusino(property.getBusino());
		iaacdrinfo.setCrcycd(cups.getCrcycd());
		iaacdrinfo.setTranam(cups.getTranam());
		SysUtil.getInstance(IoInAccount.class).IoInAcdrAdm(iaacdrinfo);
		
	}
	
	/**
	 * 银联手续费扣除
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void ioInAccr(final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Property property){
		KnlIoblCups cups = property.getKnlioblcups();
		IaAcdrInfo iaacdrinfo = SysUtil.getInstance(IaAcdrInfo.class);
		iaacdrinfo.setAcbrch(property.getBrchno());
		iaacdrinfo.setBusino(property.getBusino());
		iaacdrinfo.setCrcycd(cups.getCrcycd());
		iaacdrinfo.setTranam(cups.getTranam());
		SysUtil.getInstance(IoInAccount.class).ioInAccr(iaacdrinfo);
	}

	/**
	 * 存入记账处理
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void addPostAcctDp(final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Property property){
		KnlIoblCups cups = property.getKnlioblcups();
		SaveDpAcctIn saveDpAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
		saveDpAcctIn.setCardno(property.getCardno());
		saveDpAcctIn.setCrcycd(cups.getCrcycd());
		saveDpAcctIn.setRemark(property.getRemark());
		saveDpAcctIn.setTranam(property.getCuanam());
		saveDpAcctIn.setAcctno(property.getInacct());
		saveDpAcctIn.setDetlsq(property.getDetlsq());
		saveDpAcctIn.setIschck(property.getIschck());
		saveDpAcctIn.setSmrycd(property.getSmrycd());
		saveDpAcctIn.setCustac(property.getCustac());
		SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(saveDpAcctIn);
	}
	
	public static void unionPayDeal( final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Input input,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Property property,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Output output){
		KnlIoblCups cups = property.getKnlioblcups();
		UnionCastInfo unionCastInfo = SysUtil.getInstance(UnionPayCostSvcType.class).unionPayCostDeal(
				E_CARDTP.valueOf(cups.getCardtp().getValue()), cups.getSpcipy(), cups.getMercfg(), input.getTranam(),
				cups.getSvcode());
		if (CommUtil.isNotNull(unionCastInfo)) {
			property.setReanam(unionCastInfo.getChrgam());
			property.setUnionCastInfo(unionCastInfo);
		}
	}

	public static void shareBenefitDeal( final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Input input,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Property property,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Output output){
		KnlIoblCups cups = property.getKnlioblcups();
		IoDsManage ioDsManage = SysUtil.getInstance(IoDsManage.class);
		calMechAmtInput calMechAmtInput = SysUtil.getInstance(calMechAmtInput.class);
		E_BUSITP busitp = null;
		String svcode = cups.getSvcode().getValue();
		// 服务编码转换
		if (CommUtil.equals(E_SERVCD.PAY10001.getValue(), svcode)) {
			busitp = E_BUSITP._1;
		} else if (CommUtil.equals(E_SERVCD.PAY10002.getValue(), svcode)) {
			busitp = E_BUSITP._4;
		} else if (CommUtil.equals(E_SERVCD.PAY10003.getValue(), svcode)) {
			busitp = E_BUSITP._15;
		} else if (CommUtil.equals(E_SERVCD.PAY10004.getValue(), svcode)) {
			busitp = E_BUSITP._2;
		} else if (CommUtil.equals(E_SERVCD.PAY10005.getValue(), svcode)) {
			busitp = E_BUSITP._6;
		} else if (CommUtil.equals(E_SERVCD.PAY50003.getValue(), svcode)) {
			busitp = E_BUSITP._1;
		}
		property.setBusitp(busitp);
		calMechAmtInput.setBusitp(busitp);// 业务类型
		calMechAmtInput.setMrchno(input.getInmeid());// 内部商户号
		calMechAmtInput.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
		calMechAmtInput.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
		calMechAmtInput.setTrantm(property.getTrantm());// 交易时间
		calMechAmtInput.setTranam(input.getTranam());// 交易金额

		calMechAmtInput.setSttltp(E_INTYPE.T1);// 结算方式
		calMechAmtInput.setPosnum(cups.getPosnum());// pos终端号
		calMechAmtInput.setSbrand(property.getSbrand());// 品牌id
		calMechAmtInput.setTrnctp(cups.getCardtp());// 卡类型
		if (CommUtil.isNotNull(cups.getMercfg())) {
			calMechAmtInput.setMechtp(E_MECHTP.get(cups.getMercfg().getValue()));
		}
		calMechAmtInput.setMarkfe(cups.getMarkfe());
		calMechAmtOutput calMechAmtOutput = ioDsManage.calMechAmt(calMechAmtInput);
		BigDecimal fitamt = calMechAmtOutput.getProfitamt();
		property.setJnanam(fitamt);
		property.setMerate(calMechAmtOutput.getRate_mech());
		property.setAmanam(fitamt.subtract(property.getReanam()).add(calMechAmtOutput.getCoupon()));
		property.setCoupon(calMechAmtOutput.getCoupon());
	}

	public static void custDeptDeal( final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Input input,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Property property,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Output output){
		KnlIoblCups cups = property.getKnlioblcups();
		// 扣减结算费用金额
		BigDecimal cuanam = BigDecimal.ZERO;
		if (CommUtil.equals(cups.getFrodfg().getValue(), E_YES___.NO.getValue())) {
			cuanam = input.getTranam().subtract(property.getJnanam());
		} else {
			cuanam = input.getTranam().subtract(property.getReanam());
		}
		property.setCuanam(cuanam);
		property.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));
	}

	public static void shareBenefitPostDeal( final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Input input,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Property property,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Output output){
		// 业务编码查询赋值
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", property.getSbrand(), "%", "%", false);
		if (CommUtil.isNull(knpParameter)) {
			throw DpModuleError.DpTrans.TS020021();
		}
		String busino = knpParameter.getParm_value3();// 内部户账号
		property.setBusidn(E_BLNCDN.get(knpParameter.getParm_value2()));

		IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
		cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
		cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR);
		// cplIoAccounttingIntf.setAcctno(acctno);
		// cplIoAccounttingIntf.setCuacno(acctno);
		// cplIoAccounttingIntf.setAcseno(acctno);
		cplIoAccounttingIntf.setProdcd(busino);// 内部户产品
		cplIoAccounttingIntf.setDtitcd(busino);
		cplIoAccounttingIntf.setCrcycd(property.getCrcycd());
		cplIoAccounttingIntf.setAcctbr(property.getBrchno());
		cplIoAccounttingIntf.setTranam(property.getAmanam());
		cplIoAccounttingIntf.setToacct(property.getInacct());
		cplIoAccounttingIntf.setToacna(property.getInacna());
		cplIoAccounttingIntf.setTobrch(property.getBrchno());
		cplIoAccounttingIntf.setAtowtp(E_ATOWTP.IN);
		cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
		cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE);
		cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
		cplIoAccounttingIntf.setCorpno(property.getCorpno());

		property.setClerinfo(cplIoAccounttingIntf);

		IoSaveIoTransBill ioSaveIoTransBill = SysUtil.getInstance(IoSaveIoTransBill.class);
		ioSaveIoTransBill.SaveKnlAcsq(cplIoAccounttingIntf, property.getBusidn());
	}

	public static void voucfePostDeal( final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Input input,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Property property,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Output output){

		// 商户存款加上抵扣金额
		// property.setCuanam(property.getCuanam().add(property.getCoupon()));

		// 业务编码查询赋值
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", property.getSbrand(), "%", "%", false);
		if (CommUtil.isNull(knpParameter)) {
			throw DpModuleError.DpTrans.TS020021();
		}
		String busino = knpParameter.getParm_value3();// 内部户账号
		property.setBusidn(E_BLNCDN.get(knpParameter.getParm_value2()));

		IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
		cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
		cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR);
		// cplIoAccounttingIntf.setAcctno(acctno);
		// cplIoAccounttingIntf.setCuacno(acctno);
		// cplIoAccounttingIntf.setAcseno(acctno);
		cplIoAccounttingIntf.setProdcd(busino);// 内部户产品
		cplIoAccounttingIntf.setDtitcd(busino);
		cplIoAccounttingIntf.setCrcycd(property.getCrcycd());
		cplIoAccounttingIntf.setAcctbr(property.getBrchno());
		cplIoAccounttingIntf.setTranam(property.getCoupon().negate());
		cplIoAccounttingIntf.setToacct(property.getInacct());
		cplIoAccounttingIntf.setToacna(property.getInacna());
		cplIoAccounttingIntf.setTobrch(property.getBrchno());
		cplIoAccounttingIntf.setAtowtp(E_ATOWTP.IN);
		cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
		cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE);
		cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
		cplIoAccounttingIntf.setCorpno(property.getCorpno());

		property.setClerinfo(cplIoAccounttingIntf);

		IoSaveIoTransBill ioSaveIoTransBill = SysUtil.getInstance(IoSaveIoTransBill.class);
		ioSaveIoTransBill.SaveKnlAcsq(cplIoAccounttingIntf, property.getBusidn());
	
	}

	public static void cashPledgePostDeal( final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Input input,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Property property,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Output output){


		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", "service", "%", "%", false);
		if (CommUtil.isNull(knpParameter)) {
			throw DpModuleError.DpTrans.TS020021();
		}

		// String acctno = knpParameter.getParm_value1();// 内部户账号
		String busino = knpParameter.getParm_value1();// 业务编码
		property.setBusidn(E_BLNCDN.get(knpParameter.getParm_value2()));

		IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
		cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
		cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR);
		// cplIoAccounttingIntf.setAcctno(acctno);
		// cplIoAccounttingIntf.setCuacno(acctno);
		// cplIoAccounttingIntf.setAcseno(acctno);
		cplIoAccounttingIntf.setProdcd(busino);// 内部户产品
		cplIoAccounttingIntf.setDtitcd(busino);
		cplIoAccounttingIntf.setCrcycd(property.getCrcycd());
		cplIoAccounttingIntf.setAcctbr(property.getBrchno());
		cplIoAccounttingIntf.setTranam(property.getCuanam());
		cplIoAccounttingIntf.setToacct(property.getInacct());
		cplIoAccounttingIntf.setToacna(property.getInacna());
		cplIoAccounttingIntf.setTobrch(property.getBrchno());
		cplIoAccounttingIntf.setAtowtp(E_ATOWTP.IN);
		cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
		cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE);
		cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
		cplIoAccounttingIntf.setCorpno(property.getCorpno());

		property.setClerinfo(cplIoAccounttingIntf);

		IoSaveIoTransBill ioSaveIoTransBill = SysUtil.getInstance(IoSaveIoTransBill.class);
		ioSaveIoTransBill.SaveKnlAcsq(cplIoAccounttingIntf, property.getBusidn());
	
	}
	
	public static void checkBalance( final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Input input,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Property property,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Output output){
		// 平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(),
						CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._35);
	}

	public static void afterTranDeal( final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Input input,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Property property,  final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Erorhd.Output output){
		// 登记流水
		KnlIoblCups entity = property.getKnlioblcups();
		E_YES___ frozfg = E_YES___.NO;
		BigDecimal frozam = BigDecimal.ZERO;
		
		// 如果存在交易流水场景
		if (CommUtil.equals(property.getPrcsfg().getValue(), E_YES___.YES.getValue())) {
			// 将输入接口中字段复制到交易流水表
			entity.setAcfist(E_FINSTY.T1);
			entity.setTranst(E_CUPSST.SUCC);// 交易状态成功
			entity.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 主交易流水
			entity.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
			entity.setMntrtm(property.getTrantm());// 交易时间
			entity.setCardno(property.getCardno());// 交易卡号
			entity.setChrgam(property.getReanam());// 银联手续费
			entity.setProccd(CommTools.getBaseRunEnvs().getTrxn_code());// 处理码
			entity.setToanam(property.getCuanam());
			entity.setBusitp(property.getBusitp());
			entity.setJnanam(property.getJnanam());
			entity.setInmena(property.getInacna());
//			entity.setSbrand(E_SBRAND.get(property.getSbrand()).getLongName());
			entity.setAcctno(property.getInacct());// 负债账号
			entity.setMerate(property.getMerate());// 商户费率
			entity.setFrozfg(frozfg);// 该笔流水是否冻结
			entity.setRemark(property.getRemark());
			entity.setFrozam(frozam);// 冻结金额
			entity.setBscdam(property.getUnionCastInfo().getBscdam());
			entity.setSvntam(property.getUnionCastInfo().getSvntam());
			entity.setUnbdam(property.getUnionCastInfo().getUnbdam());
			entity.setCoupon(property.getCoupon());// 设置实际抵扣值
//			entity.setServtp(property.getServtp());
			KnlIoblCupsDao.updateOne_odb3(entity);
		} else {
			// TODO 新增暂不考虑 
		}
		output.setFlowid(entity.getFlowid());
		output.setStatus(E_SETTST.SUCC.getValue());
		output.setMesage(E_SETTST.SUCC.getLongName());
	}
	

}
