package cn.sunline.ltts.busi.dptran.trans;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTran;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTranDao;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dptran.trans.intf.Adjust.Input;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbAcin;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoKnsAcsqInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.errors.PbError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ADJACD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AJTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_RVFXST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ACUTTP;


public class adjust {

	public static void AdjustDeal(final cn.sunline.ltts.busi.dptran.trans.intf.Adjust.Input input, final cn.sunline.ltts.busi.dptran.trans.intf.Adjust.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Adjust.Output output) {

		/* 有原调整 */
		if (input.getAjtype() == E_AJTYPE.OADJ) {
			
			doTraninTranotAdjust(input, false);
			
			KnsTran tran = KnsTranDao.selectOne_odb1(input.getOrigsq(), input.getOrigdt(), false);
			if (input.getAdjacd() == E_ADJACD.GAPADJ){
				tran.setRvfxst(E_RVFXST.ADJUST_PART);
			}
			else {
				tran.setRvfxst(E_RVFXST.ADJUST);
			}
			
			tran.setRverbr(CommTools.getBaseRunEnvs().getTrxn_branch());
			tran.setRverus(CommTools.getBaseRunEnvs().getTrxn_teller());
			tran.setRverdt(CommTools.getBaseRunEnvs().getTrxn_date());
			tran.setRversq(CommTools.getBaseRunEnvs().getTrxn_seq());
			tran.setSpaco1(BusiTools.getBusiRunEnvs().getRemark());
			// 修改原交易流水信息
			//ApJournal.updateKnsTran(tran);

		}
		/* 调整撤销 */
		else if (input.getAjtype() == E_AJTYPE.DADJ) {

			doTraninTranotAdjust(input, true);
			
			KnsTran tran = KnsTranDao.selectOne_odb1(input.getOrigsq(), input.getOrigdt(), false);
			tran.setRvfxst(E_RVFXST.ADJUST);
			tran.setRverbr(CommTools.getBaseRunEnvs().getTrxn_branch());
			tran.setRverus(CommTools.getBaseRunEnvs().getTrxn_teller());
			tran.setRverdt(CommTools.getBaseRunEnvs().getTrxn_date());
			tran.setRversq(CommTools.getBaseRunEnvs().getTrxn_seq());
			tran.setSpaco1(BusiTools.getBusiRunEnvs().getRemark());
			// 修改原交易流水信息
			//ApJournal.updateKnsTran(tran);
		}
		/* 无原调整 */
		else if (input.getAjtype() == E_AJTYPE.NADJ) {
			KnpParameter para = SysUtil.getInstance(KnpParameter.class);
			para = KnpParameterDao.selectOne_odb1(input.getOprscd(), input.getOsrvtp(), input.getAmntcd().toString(), "%", true);

			// 获取电子账号
			IoCaKnaAcdc inacdc = ActoacDao.selKnaAcdc(input.getCardno(), false);
			if (CommUtil.isNull(inacdc)) {
				throw DpModuleError.DpstComm.BNAS0750();
			}

			// 获取客户账户类型
			E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(inacdc.getCustac());

			// 获取转入子账号
			KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
			if (accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE) { // 结算户
				tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(inacdc.getCustac(), E_ACSETP.SA);
			} else { // 钱包户
				tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(inacdc.getCustac(), E_ACSETP.MA);
			}

			if (E_AMNTCD.CR == input.getAmntcd()) {
				// 内部户借方服务
				IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
				iaAcdrInfo.setBusino(para.getParm_value1()); // 业务编码
				iaAcdrInfo.setSubsac(para.getParm_value2());// 子户号
				iaAcdrInfo.setCrcycd(input.getCrcycd());// 币种
				iaAcdrInfo.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
				iaAcdrInfo.setTranam(input.getTranam());// 记账金额
				iaAcdrInfo.setTrantp(E_TRANTP.TR);
				iaAcdrInfo.setToacct(input.getCardno());
				iaAcdrInfo.setToacna(tblKnaAcct.getAcctna());
				iaAcdrInfo.setAcuttp(E_ACUTTP._1);// 记账类型正常
				iaAcdrInfo.setSmrycd(BusinessConstants.SUMMARY_AJ);
				E_CAPITP capitp = CommUtil.toEnum(E_CAPITP.class, para.getParm_value3());
				iaAcdrInfo.setDscrtx(capitp.getLongName()); // 描述

				SysUtil.getInstance(IoInAccount.class).ioInAcdr(iaAcdrInfo);// 内部户借方服务

				SaveDpAcctIn saveDpAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
				saveDpAcctIn.setAcctno(tblKnaAcct.getAcctno());
				saveDpAcctIn.setCustac(inacdc.getCustac());
				saveDpAcctIn.setCardno(input.getCardno());
				saveDpAcctIn.setOpacna(tblKnaAcct.getAcctna());
				saveDpAcctIn.setCrcycd(input.getCrcycd());
				saveDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_AJ);
				saveDpAcctIn.setRemark("调整");
				saveDpAcctIn.setTranam(input.getTranam());
				saveDpAcctIn.setOpbrch(tblKnaAcct.getBrchno());
				saveDpAcctIn.setLinkno(null);

				SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(saveDpAcctIn);
			} else if (E_AMNTCD.DR == input.getAmntcd()) {
				// 调支取服务
				DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
				drawDpAcctIn.setAcctno(tblKnaAcct.getAcctno());
				drawDpAcctIn.setCustac(inacdc.getCustac());
				drawDpAcctIn.setCardno(input.getCardno());
				drawDpAcctIn.setOpacna(tblKnaAcct.getAcctna());
				drawDpAcctIn.setCrcycd(input.getCrcycd());
				drawDpAcctIn.setIschck(E_YES___.NO);
				drawDpAcctIn.setTranam(input.getTranam());
				drawDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_AJ);
				drawDpAcctIn.setLinkno(null);
				drawDpAcctIn.setRemark("调整");

				SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawDpAcctIn);

				IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
				iaAcdrInfo.setBusino(para.getParm_value1()); // 业务编码
				iaAcdrInfo.setSubsac(para.getParm_value2());// 子户号
				iaAcdrInfo.setTrantp(E_TRANTP.TR);
				iaAcdrInfo.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
				iaAcdrInfo.setToacct(input.getCardno());
				iaAcdrInfo.setToacna(tblKnaAcct.getAcctna());
				iaAcdrInfo.setTranam(input.getTranam());// 记账金额
				iaAcdrInfo.setAcuttp(E_ACUTTP._1);// 记账类型正常
				iaAcdrInfo.setCrcycd(input.getCrcycd());// 币种
				iaAcdrInfo.setSmrycd(BusinessConstants.SUMMARY_AJ);
				iaAcdrInfo.setTrantp(E_TRANTP.TR);

				SysUtil.getInstance(IoInAccount.class).ioInAccr(iaAcdrInfo);// 内部户贷方服务
			}

		}
		/* 内部户调整 */
		else if (input.getAjtype() == E_AJTYPE.IADJ) {
			IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
			iaAcdrInfo.setTrantp(E_TRANTP.TR);
			iaAcdrInfo.setAcctno(input.getAcctno());
			iaAcdrInfo.setTranam(input.getTranam());// 记账金额
			iaAcdrInfo.setAcuttp(E_ACUTTP._1);// 记账类型正常
			iaAcdrInfo.setCrcycd(input.getCrcycd());// 币种

			// 调用内部户记账服务
			IoInAccount ioInAcctount = SysUtil.getInstance(IoInAccount.class);

			switch (input.getAmntcd()) {
			case DR:
				iaAcdrInfo.setSmrycd(BusinessConstants.SUMMARY_ZC);
				ioInAcctount.ioInAcdr(iaAcdrInfo);// 内部户借方服务
				break;
			case CR:
				iaAcdrInfo.setSmrycd(BusinessConstants.SUMMARY_ZR);
				ioInAcctount.ioInAccr(iaAcdrInfo);// 内部户贷方服务
				break;
			default:
				throw InError.comm.E0003("记账方向:" + input.getAmntcd().getValue() + "[" + input.getAmntcd().getLongName() + "]不支持");
			}
		}
		else {
			throw InError.comm.E0003("调整类型:" + input.getAjtype() + "[" + input.getAjtype() + "]不支持");
		}
		output.setAdjseq(CommTools.getBaseRunEnvs().getTrxn_seq());
	}

	private static void doTraninTranotAdjust(Input input, boolean isCancel) {

		if (CommUtil.isNull(input.getOrigsq())) {
			CommTools.fieldNotNull(input.getOrigsq(), BaseDict.Comm.transq.getId(), BaseDict.Comm.transq.getLongName());
		}
		long start_no  =1;
		long page_size =10;
	
		Options<IoKnsAcsqInfo> queryKnsAcsq = SysUtil.getInstance(IoAccountSvcType.class)
				.queryKnsAcsq(null, null, null, input.getOrigsq(), CommTools.getBaseRunEnvs().getBusi_org_id(), start_no, page_size,input.getCardno());
		if (CommUtil.isNull(queryKnsAcsq)) {
			throw PbError.PbComm.E9999("无对应的流水号交易记录");
		}
		if (queryKnsAcsq.size() != 2) {
			throw PbError.PbComm.E9999("原流水号对应的交易记录异常");
		}
		IoKnsAcsqInfo custacInfo = queryKnsAcsq.get(0);
		IoKnsAcsqInfo acctnoInfo = queryKnsAcsq.get(1);
		if (custacInfo.getCuacno().length() != 13) {
			IoKnsAcsqInfo temp = custacInfo;
			custacInfo = acctnoInfo;
			acctnoInfo = temp;
		}
		if(CommUtil.compare(input.getOrgamt(), custacInfo.getTranam()) != 0) {
			throw PbError.PbComm.E9999("输入参数原金额不正确");
		}
		if(CommUtil.compare(input.getAmntcd(), custacInfo.getAmntcd()) != 0) {
			throw PbError.PbComm.E9999("输入参数原记账方向不正确");
		}
		if(CommUtil.compare(input.getOprscd(), custacInfo.getPrcscd()) != 0) {
			throw PbError.PbComm.E9999("输入参数交易码不正确");
		}
		E_ADJACD adjacd = input.getAdjacd();
		if (isCancel) {
			adjacd = E_ADJACD.ALLADJ;
		}
		// 先红字冲
		custacInfo.setTranam(acctnoInfo.getTranam().negate());
		acctnoInfo.setTranam(acctnoInfo.getTranam().negate());
		doInAccount(acctnoInfo);
		doCustacAccount(custacInfo, acctnoInfo);

		if (adjacd == E_ADJACD.ALLADJ) {// 冲0

		} else if (adjacd == E_ADJACD.CHGADJ) {
			E_AMNTCD amntcd1 = acctnoInfo.getAmntcd() == E_AMNTCD.CR ? E_AMNTCD.CR : E_AMNTCD.DR;
			E_AMNTCD amntcd2 = custacInfo.getAmntcd() == E_AMNTCD.CR ? E_AMNTCD.CR : E_AMNTCD.DR;
			acctnoInfo.setAmntcd(amntcd1);
			custacInfo.setAmntcd(amntcd2);
			acctnoInfo.setTranam(acctnoInfo.getTranam().negate());
			doInAccount(acctnoInfo);
			doCustacAccount(custacInfo, acctnoInfo);
		} else if (adjacd == E_ADJACD.GAPADJ) {
			acctnoInfo.setTranam(input.getTranam());
			custacInfo.setTranam(input.getTranam());
			acctnoInfo.setTranam(acctnoInfo.getTranam().negate());
			doInAccount(acctnoInfo);
			doCustacAccount(custacInfo, acctnoInfo);
		} else if (adjacd == E_ADJACD.GCHADJ) {
			E_AMNTCD amntcd1 = acctnoInfo.getAmntcd() == E_AMNTCD.CR ? E_AMNTCD.CR : E_AMNTCD.DR;
			E_AMNTCD amntcd2 = custacInfo.getAmntcd() == E_AMNTCD.CR ? E_AMNTCD.CR : E_AMNTCD.DR;
			acctnoInfo.setAmntcd(amntcd1);
			custacInfo.setAmntcd(amntcd2);
			acctnoInfo.setTranam(input.getTranam());
			custacInfo.setTranam(input.getTranam());
			acctnoInfo.setTranam(acctnoInfo.getTranam().negate());
			doInAccount(acctnoInfo);
			doCustacAccount(custacInfo, acctnoInfo);
		}
	}

	private static void doInAccount(IoKnsAcsqInfo acctnoInfo) {
		IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
		iaAcdrInfo.setTrantp(E_TRANTP.TR);
		iaAcdrInfo.setAcctno(acctnoInfo.getCuacno());
		iaAcdrInfo.setTranam(acctnoInfo.getTranam());// 记账金额
		iaAcdrInfo.setAcuttp(E_ACUTTP._3);// 记账类型蓝字
		iaAcdrInfo.setSmrycd(BusinessConstants.SUMMARY_AJ);
		iaAcdrInfo.setCrcycd(acctnoInfo.getCrcycd());// 币种
		iaAcdrInfo.setAcbrch(acctnoInfo.getAcctbr());// 账户机构
		// 记账方向
		E_AMNTCD amntcd = acctnoInfo.getAmntcd();
		// 调用内部户记账服务
		IoInAccount ioInAcctount = SysUtil.getInstance(IoInAccount.class);

		switch (amntcd) {
		case DR:
			ioInAcctount.ioInAcdr(iaAcdrInfo);// 内部户借方服务
			break;
		case CR:
			ioInAcctount.ioInAccr(iaAcdrInfo);// 内部户贷方服务
			break;
		default:
			throw InError.comm.E0003("记账方向:" + amntcd.getValue() + "[" + amntcd.getLongName() + "]不支持");
		}
	}

	private static void doCustacAccount(IoKnsAcsqInfo custacInfo, IoKnsAcsqInfo inAcctnoInfo) {
		String custac = custacInfo.getCuacno();
		KnaAcdc knaAcdc = KnaAcdcDao.selectFirst_odb3(custac, true);
		// 客户账入账
		IoDpKnaAcct ioDpKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);
		E_AMNTCD amntcd = custacInfo.getAmntcd();
		BigDecimal tranam = custacInfo.getTranam();

		if (E_AMNTCD.DR == amntcd) {
			SysUtil.getInstance(IoAccountSvcType.class).checkStatusBeforeAccount(custac, E_AMNTCD.DR, E_YES___.YES, E_YES___.NO);
		} else if (E_AMNTCD.CR == amntcd) {
			SysUtil.getInstance(IoAccountSvcType.class).checkStatusBeforeAccount(custac, E_AMNTCD.CR, E_YES___.YES, E_YES___.NO);
		}

		// 客户账记账输入类字段赋值
		if (E_AMNTCD.CR == amntcd) {
			// 红字，调存入服务，记负数
			SaveDpAcctIn saveDpAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
			saveDpAcctIn.setAcctno(ioDpKnaAcct.getAcctno());
			saveDpAcctIn.setCustac(ioDpKnaAcct.getCustac());
			saveDpAcctIn.setCardno(knaAcdc.getCardno());
			saveDpAcctIn.setOpacna(inAcctnoInfo.getAcctna());
			saveDpAcctIn.setToacct(inAcctnoInfo.getAcctno());
			saveDpAcctIn.setCrcycd(custacInfo.getCrcycd());
			saveDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_AJ);
			saveDpAcctIn.setTranam(tranam);
			saveDpAcctIn.setNegafg(E_YES___.YES);// 支持红字记账
			SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(saveDpAcctIn);
		} else {
			// 蓝字，调支取服务，记正数
			DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
			drawDpAcctIn.setAcctno(ioDpKnaAcct.getAcctno());
			drawDpAcctIn.setCustac(ioDpKnaAcct.getCustac());
			drawDpAcctIn.setCardno(knaAcdc.getCardno());
			drawDpAcctIn.setOpacna(inAcctnoInfo.getAcctna());
			drawDpAcctIn.setToacct(inAcctnoInfo.getAcctno());
			drawDpAcctIn.setCrcycd(custacInfo.getCrcycd());
			drawDpAcctIn.setIschck(E_YES___.NO);
			drawDpAcctIn.setTranam(tranam);
			drawDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_AJ);
			SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawDpAcctIn);
		}
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		// 积数调整
		int ibdays = DateTools2.calDays(custacInfo.getTrandt(), trandt, 0, 0);// 天数差
		BigDecimal acmlbl1 = custacInfo.getTranam().multiply(new BigDecimal(ibdays));// 积数
		if (ibdays > 0) {
			IoDpKnbAcin cplAcin1 = SysUtil.getInstance(IoDpSrvQryTableInfo.class).getKnbAcinOdb1(ioDpKnaAcct.getAcctno(), true);
			if ((E_AMNTCD.CR == amntcd && CommUtil.compare(tranam, BigDecimal.ZERO) > 0) || E_AMNTCD.DR == amntcd && CommUtil.compare(tranam, BigDecimal.ZERO) < 0) {

				cplAcin1.setCutmam(cplAcin1.getCutmam().add(acmlbl1));// 调增
			} else {
				cplAcin1.setCutmam(cplAcin1.getCutmam().subtract(acmlbl1));// 调减
			}
			// 更新积数
			SysUtil.getInstance(IoDpSrvQryTableInfo.class).updateKnbAcinOdb1(cplAcin1);
		}
	}
}
