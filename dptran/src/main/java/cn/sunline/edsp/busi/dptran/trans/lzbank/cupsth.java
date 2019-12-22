package cn.sunline.edsp.busi.dptran.trans.lzbank;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.iobus.servicetype.risk.DpRiskService;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.FrozenApplyImport;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.toTransThaw;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUPSST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_FINSTY;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_RETUST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SERVCD;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_TLEWAY;
import cn.sunline.edsp.busi.ds.iobus.servicetype.ds.IoDsManage;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.base.DpTransfer;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

public class cupsth {

	public static void cupsthDeal(final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Cupsth.Input input,
			final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Cupsth.Property property,
			final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Cupsth.Output output) {
		
		// 服务编码
		E_SERVCD svcode = input.getSvcode();
		// 结算类型
		E_FINSTY finsty = input.getFinsty();

		if (CommUtil.isNull(svcode)) {
			throw DpModuleError.DpTrans.TS010035();
		}
		// 退貨場景：60001 - 有卡退款；60002 - 银联二维码退款；60003 - 支付宝退款；60004 - 微信退款
		if (!(CommUtil.equals(svcode.getValue(), E_SERVCD.PAY60001.getValue())
				|| CommUtil.equals(svcode.getValue(), E_SERVCD.PAY60002.getValue())
				|| CommUtil.equals(svcode.getValue(), E_SERVCD.PAY60003.getValue())
				|| CommUtil.equals(svcode.getValue(), E_SERVCD.PAY60004.getValue()))) {
			throw DpModuleError.DpTrans.TS020052();
		}

		if (CommUtil.isNull(finsty)) {
			throw DpModuleError.DpTrans.TS010028();
		}

		// if (!CommUtil.equals(finsty.getValue(), E_FINSTY.T1.getValue())) {
		// throw DpModuleError.DpTrans.TS020048();
		// }

		if (CommUtil.isNull(input.getReflid())) {
			throw DpModuleError.DpTrans.TS020049();
		}

		// 檢查原收单交易是否存在
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

		if(!CommUtil.equals(E_CUPSST.SUCC.getValue(), knlIoblCups.getTranst().getValue())) {
			throw DpModuleError.DpTrans.TS020051(knlIoblCups.getTranst().getLongName());
		}
		
		if (CommUtil.equals(knlIoblCups.getTleway().getValue(), E_TLEWAY.ZJ.getValue())) {
			throw DpModuleError.DpTrans.TS020056();
		}

		/*
		 * // 当前交易日期 String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		 * 
		 * if (!CommUtil.equals(knlIoblCups.getTrandt(), trandt)) { throw
		 * DpModuleError.DpTrans.TS020050(); }
		 */
		
		KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(input.getInmeid(), false);
		if (CommUtil.isNull(tblKnaSbad)) {
			throw DpModuleError.DpTrans.TS020027();
		} 
		
		KnaAcdc knaAcdc = KnaAcdcDao.selectFirst_odb3(tblKnaSbad.getCustac(), false);

		KnaAcct knaAcct = KnaAcctDao.selectOne_odb1(tblKnaSbad.getAcctno(), false);
		if (CommUtil.isNull(knaAcct)) {// 品牌子户号对应不存在
			throw DpModuleError.DpTrans.TS020014();
		}
		
		//增加是否冻结判断
		if (CommUtil.equals(E_YES___.YES.getValue(), knlIoblCups.getFrozfg().getValue())) {
			// 初始化冻结解冻服务
			DpRiskService dpRiskService = SysUtil.getInstance(DpRiskService.class);
			
			
			// 针对调单冻结进行解冻
			toTransThaw toTransThaw = SysUtil.getInstance(DpRiskType.toTransThaw.class);
			toTransThaw.setTdtrsq(knlIoblCups.getMntrsq());
			toTransThaw.setTdtrdt(knlIoblCups.getTrandt());
			toTransThaw.setCustac(tblKnaSbad.getCustac());
			toTransThaw.setTranam(knlIoblCups.getToanam());
			toTransThaw.setAcctno(knaAcct.getAcctno());
			toTransThaw.setCardno(knaAcdc.getCardno());
			toTransThaw.setFroztp(E_FROZTP.CORRECTED);
			
            dpRiskService.transThaw(toTransThaw);
		}
		
		// 账户余额不够时，做冻结处理，每天日终时，自动扣款
		if(CommUtil.compare(knlIoblCups.getToanam(), BigDecimal.ZERO)!= 0) {
			// 获取子账户可用余额
			BigDecimal onlnbl = DpAcctProc.getAcctBalWithLock(knaAcct.getAcctno());
			
			if(CommUtil.compare(onlnbl,knlIoblCups.getToanam())<0) {
				
				FrozenApplyImport e =  SysUtil.getInstance(DpRiskType.FrozenApplyImport.class);
				Options<FrozenApplyImport> importList = SysUtil.getInstance(Options.class);
				
				e.setFrozam(knlIoblCups.getToanam());// 冻结总金额
				e.setFroztp(E_FROZTP.CORRECTED);// 冻结业务类型-退货冻结
				e.setRefeno(knlIoblCups.getRefeno());// 参考号
				e.setRetrdt(knlIoblCups.getPrepdt());// 收单交易日期
				
				importList.add(e);
				// 初始化冻结解冻服务
				DpRiskService dpRiskService = SysUtil.getInstance(DpRiskService.class);
				dpRiskService.addFrozenApplies(importList);

				output.setIsresu(E_YES___.NO);// 退货失败
				return;
			}
		}
		
		
		// 业务编码查询赋值
		KnpParameter knpParameterBusino = KnpParameterDao.selectOne_odb1("DP.ACCTIN", knlIoblCups.getServtp().getId(), "%", "%", false);
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

		// 银联成本手续费反向处理
		if(CommUtil.compare(knlIoblCups.getChrgam(), BigDecimal.ZERO)!= 0) {
			iaTransOutPro.setBusino(knpParameterBusino.getParm_value1());
			iaTransOutPro.setAcbrch(knaAcct.getBrchno());
			iaTransOutPro.setCrcycd(knlIoblCups.getCrcycd());
			iaTransOutPro.setTranam(knlIoblCups.getChrgam());

			ioInAccount.ioInAcdr(iaTransOutPro);
		}
			
		// 业务编码查询赋值	（抵扣金额处理）
		KnpParameter knpParameterSbrand = KnpParameterDao.selectOne_odb1("DP.ACCTIN", tblKnaSbad.getSbrand(), "%", "%",
				false);
		if (CommUtil.isNull(knpParameterSbrand)) {
			throw DpModuleError.DpTrans.TS020021();
		}
		
		// 分润计算服务调用冲正
		IoDsManage ioDsManage = SysUtil.getInstance(IoDsManage.class);
		ioDsManage.rollbkCalProfit(knlIoblCups.getTrandt(), knlIoblCups.getMntrsq());

		if (CommUtil.equals(E_YES___.NO.getValue(), knlIoblCups.getFrodfg().getValue())) {
			// 分润费用红字处理
			BigDecimal jnanam = knlIoblCups.getJnanam().subtract(knlIoblCups.getChrgam()).add(knlIoblCups.getCoupon());
			if(CommUtil.compare(jnanam, BigDecimal.ZERO)!= 0) {
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
				cplIoAccounttingIntf.setTranam(jnanam.negate());
				cplIoAccounttingIntf.setTobrch(knaAcct.getBrchno());
				cplIoAccounttingIntf.setAtowtp(E_ATOWTP.IN);
				cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
				cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE);
				cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
				cplIoAccounttingIntf.setCorpno(knlIoblCups.getCorpno());
				
				IoSaveIoTransBill ioSaveIoTransBillf = SysUtil.getInstance(IoSaveIoTransBill.class);
				ioSaveIoTransBillf.SaveKnlAcsq(cplIoAccounttingIntf, E_BLNCDN.get(knpParameterSbrand.getParm_value2()));
			}
			
			// 活动抵扣金额处理
			if(CommUtil.compare(knlIoblCups.getVoucfe(), BigDecimal.ZERO)!= 0 
					&& CommUtil.equals(knlIoblCups.getAcfist().getValue(), E_FINSTY.D0.getValue())) {
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
			
			// 客户支取记账
			if(CommUtil.compare(knlIoblCups.getToanam(), BigDecimal.ZERO)!= 0) {
				DpAcctSvcType dpAcctSvcType = SysUtil.getInstance(DpAcctSvcType.class);
				DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
				cplDrawAcctIn.setAcctno(knaAcct.getAcctno());
				cplDrawAcctIn.setTranam(knlIoblCups.getToanam());
				cplDrawAcctIn.setCrcycd(knaAcct.getCrcycd());
				cplDrawAcctIn.setOpacna(knaAcct.getAcctna());
				cplDrawAcctIn.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));
				cplDrawAcctIn.setCustac(knaAcct.getCustac());
				cplDrawAcctIn.setCardno(knaAcdc.getCardno());
				cplDrawAcctIn.setSmrycd("TK");
				
				dpAcctSvcType.addDrawAcctDp(cplDrawAcctIn);	
			}
		}
		
		// 修改原交易状态
		knlIoblCups.setTranst(E_CUPSST.RTGD);
		knlIoblCups.setRetust(E_RETUST.ALL);
		knlIoblCups.setRegoam(knlIoblCups.getTranam());
		KnlIoblCupsDao.updateOne_odb2(knlIoblCups);

		// 新增一条记录
		KnlIoblCups knlIoblCupsNew = SysUtil.getInstance(KnlIoblCups.class);

		CommUtil.copyProperties(knlIoblCupsNew, input);

		knlIoblCupsNew.setTranst(E_CUPSST.SUCC);
		knlIoblCupsNew.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 主交易流水
		knlIoblCupsNew.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
		knlIoblCupsNew.setMntrtm(BusiTools.getTraxTmInRunEnv());
		knlIoblCupsNew.setAcctno(knlIoblCups.getAcctno());
		knlIoblCupsNew.setInmeid(knlIoblCups.getInmeid());
		knlIoblCupsNew.setServtp(knlIoblCups.getServtp());
		knlIoblCupsNew.setJnanam(knlIoblCups.getJnanam());
		knlIoblCupsNew.setChrgam(knlIoblCups.getChrgam());
		knlIoblCupsNew.setProccd(CommTools.getBaseRunEnvs().getTrxn_code());// 处理码
		knlIoblCupsNew.setToanam(knlIoblCups.getToanam());
		knlIoblCupsNew.setCoupon(knlIoblCups.getCoupon());
		knlIoblCupsNew.setCardno(knlIoblCups.getCardno());
		knlIoblCupsNew.setBusitp(DpTransfer.svcodeToBusitp(input.getSvcode()));//更改服务类型转换
		knlIoblCupsNew.setInmena(knlIoblCups.getInmena());
		knlIoblCupsNew.setMercfg(knlIoblCups.getMercfg());
		knlIoblCupsNew.setTmpabkno(knlIoblCups.getTmpabkno());
		knlIoblCupsNew.setChantp(knlIoblCups.getChantp());

		KnlIoblCupsDao.insert(knlIoblCupsNew);

		// 平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(),
				CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._35);

}
}