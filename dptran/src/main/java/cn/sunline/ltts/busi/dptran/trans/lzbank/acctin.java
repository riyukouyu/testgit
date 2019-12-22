
package cn.sunline.ltts.busi.dptran.trans.lzbank;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.iobus.servicetype.dp.UnionPayCostSvcType;
import cn.sunline.edsp.busi.dp.iobus.servicetype.risk.DpRiskService;
import cn.sunline.edsp.busi.dp.iobus.type.dp.DpdebitAcctnos.UnionCastInfo;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.KnbFrozOutput;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CARDTP;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUPSST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_FINSTY;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_PASTAT;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_RETYPE;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SBRAND;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SERVCD;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_TLEWAY;
import cn.sunline.edsp.busi.ds.iobus.servicetype.ds.IoDsManage;
import cn.sunline.edsp.busi.ds.iobus.type.DsComplex.calMechAmtInput;
import cn.sunline.edsp.busi.ds.iobus.type.DsComplex.calMechAmtOutput;
import cn.sunline.edsp.busi.ds.iobus.type.DsEnumType.E_BUSITP;
import cn.sunline.edsp.busi.ds.iobus.type.DsEnumType.E_INTYPE;
import cn.sunline.edsp.busi.ds.iobus.type.DsEnumType.E_MECHTP;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.base.DecryptConstant;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.dp.base.DpTools;
import cn.sunline.ltts.busi.dp.base.DpTransfer;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdm;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmContro;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmControDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

public class acctin {

	public static void checkDeal(final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Input input,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Property property,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Output output) {

		if (CommUtil.isNull(input.getPrepsq())) {// posp流水
			throw DpModuleError.DpTrans.TS010029();
		}

		if (CommUtil.isNull(input.getPrepdt())) {// posp日期
			throw DpModuleError.DpTrans.TS010030();
		}

		if (CommUtil.isNull(input.getPreptm())) {// posp时间
			throw DpModuleError.DpTrans.TS010031();
		}

		if (CommUtil.isNull(input.getInmeid())) {// 内部商户号
			throw DpModuleError.DpTrans.TS020013();
		}

		if (CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0) { // 校验交易金额
			throw DpModuleError.DpstComm.BNAS0627();
		}

		if (CommUtil.isNull(input.getFinsty())) {
			throw DpModuleError.DpTrans.TS010028();
		}

		if (CommUtil.isNull(input.getUntrst())) {
			throw DpModuleError.DpTrans.TS010032();
		}

		if (CommUtil.isNull(input.getFlowid())) {
			throw DpModuleError.DpTrans.TS020041();
		}

		//新增预授权完成服务吗判断
		if (CommUtil.equals(E_SERVCD.PAY10001.getValue(), input.getSvcode().getValue())
				|| CommUtil.equals(E_SERVCD.PAY10003.getValue(), input.getSvcode().getValue())
				|| CommUtil.equals(E_SERVCD.PAY10005.getValue(), input.getSvcode().getValue())
				|| CommUtil.equals(E_SERVCD.PAY50003.getValue(), input.getSvcode().getValue())) {
			property.setServtp(E_RETYPE.CUPSZZ);
		} else if (CommUtil.equals(E_SERVCD.PAY10004.getValue(), input.getSvcode().getValue())) {
			property.setServtp(E_RETYPE.ALIWKZZ);
		} else if (CommUtil.equals(E_SERVCD.PAY10002.getValue(), input.getSvcode().getValue())) {
			property.setServtp(E_RETYPE.WXINWKZZ);
		} else {
			throw DpModuleError.DpTrans.TS020060();
		}

		KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(input.getInmeid(), false);
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

			KnaCacd knaCacd = KnaCacdDao.selectFirst_odb3(property.getInacct(), E_DPACST.DEFAULT, false);

			if (CommUtil.isNotNull(knaCacd)) {
				property.setBindfg(E_YES___.YES);
				property.setKnaCacd(knaCacd);
				output.setTmbankph(knaCacd.getTmbankph());
				output.setTmcdopac(knaCacd.getTmcdopac());
			} else if (CommUtil.isNull(knaCacd)
					&& CommUtil.equals(input.getFinsty().getValue(), E_FINSTY.D0.getValue())) {
				property.setRemark("未绑定默认结算卡，结算类型D0转T1");
			}

		} else {
			throw DpModuleError.DpTrans.TS020027();
		}

		// 重复性校验
		KnlIoblCups knlIoblCups = KnlIoblCupsDao.selectOneWithLock_odb3(input.getFlowid(), false);
		if (CommUtil.isNotNull(knlIoblCups)) {
			if (CommUtil.equals(E_CUPSST.SUCC.getValue(), knlIoblCups.getTranst().getValue())
					|| CommUtil.equals(E_CUPSST.FAIL.getValue(), knlIoblCups.getTranst().getValue())) {
				output.setMntrdt(knlIoblCups.getTrandt());
				output.setMntrsq(knlIoblCups.getMntrsq());
				output.setMntrtm(knlIoblCups.getMntrtm());
				output.setCuanam(knlIoblCups.getToanam());
				output.setFinsty(knlIoblCups.getFinsty());
				output.setJnanam(knlIoblCups.getJnanam());
				output.setFrozfg(knlIoblCups.getFrozfg());

				// 设置交易是否存在
				property.setIsexit(E_YES___.YES);
				// 判断发送成功且D0类型下可用额度是否足够,该笔流水是否冻结
				if (CommUtil.equals(E_CUPSST.SUCC.getValue(), knlIoblCups.getTranst().getValue())
						&& CommUtil.equals(knlIoblCups.getAcfist().getValue(), E_FINSTY.D0.getValue())
						&& CommUtil.equals(E_YES___.YES.getValue(), input.getTfcrfg().getValue())
						&& CommUtil.equals(E_YES___.NO.getValue(), knlIoblCups.getFrozfg().getValue())) {
					if (CommUtil.compare(input.getAvalcr(), knlIoblCups.getToanam()) >= 0) {
						property.setEdflge(E_YES___.YES);
						property.setCuanam(knlIoblCups.getToanam());// 设置商户存入金额
					} else {
						// 进行冲账处理
						IoDpStrikeSvcType ioDpStrikeSvcType = SysUtil.getInstance(IoDpStrikeSvcType.class);

						ioDpStrikeSvcType.ProAcctinStrike(input.getFlowid(), property.getBrchno(), input.getCrcycd());

						// 生成新的流水进行记账todo
						property.setIsexit(E_YES___.NO);
						output.setFinsty(E_FINSTY.T1);
						property.setDtrsnt(E_YES___.YES);
						property.setPrcsfg(E_YES___.YES);
						property.setKnlioblcups(knlIoblCups);
						property.setRemark("额度不足，结算类型D0转T1");
					}
				}
			} else if (CommUtil.equals(E_CUPSST.PRC.getValue(), knlIoblCups.getTranst().getValue())
					&& CommUtil.equals(input.getUntrst().getValue(), E_PASTAT.SUCC.getValue())) {
				// 存在处理中交易进行处理
				property.setKnlioblcups(knlIoblCups);
				property.setPrcsfg(E_YES___.YES);
			} else if (CommUtil.equals(E_CUPSST.PRC.getValue(), knlIoblCups.getTranst().getValue())
					&& CommUtil.equals(input.getUntrst().getValue(), E_PASTAT.FAIL.getValue())) {
				// 存在处理中二次返回失败交易进行处理
				property.setIsexit(E_YES___.NO);
				property.setPrcsfg(E_YES___.YES);
			} else if ((CommUtil.equals(E_CUPSST.PRC.getValue(), knlIoblCups.getTranst().getValue())
					&& CommUtil.equals(input.getUntrst().getValue(), E_PASTAT.PROC.getValue()))
					|| (CommUtil.equals(E_CUPSST.FAIL.getValue(), knlIoblCups.getTranst().getValue())
							&& CommUtil.equals(input.getUntrst().getValue(), E_PASTAT.FAIL.getValue()))) {
				property.setIsexit(E_YES___.YES);// 交易存在
				output.setFinsty(E_FINSTY.T1);// 设置结算方式为T1
				return;
			}
		}

		if (!CommUtil.equals(input.getUntrst().getValue(), E_PASTAT.SUCC.getValue())
				|| (CommUtil.isNotNull(input.getTleway())
						&& CommUtil.equals(input.getTleway().getValue(), E_TLEWAY.ZJ.getValue()))) {
			property.setRgflfg(E_YES___.YES);
			return;
		}

		// 业务编码查询赋值
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", property.getServtp().getId(), "%", "%",
				false);
		if (CommUtil.isNull(knpParameter)) {
			throw DpModuleError.DpTrans.TS020021();
		}
		// 待清算业务编码
		property.setBusino(knpParameter.getParm_value1());

	}

	public static void afterTranDeal(final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Input input,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Property property,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Output output) {
		// 登记流水
		KnlIoblCups entity = property.getKnlioblcups();

		E_YES___ frozfg = E_YES___.NO;

		BigDecimal frozam = BigDecimal.ZERO;

		if (CommUtil.equals(input.getFrodfg().getValue(), E_YES___.NO.getValue())
				&& CommUtil.equals(property.getRgflfg().getValue(), E_YES___.NO.getValue())
				&& (CommUtil.equals(input.getUntrst().getValue(), E_PASTAT.SUCC.getValue()))) {
			if (CommUtil.equals(input.getFinsty().getValue(), E_FINSTY.D0.getValue())
					&& CommUtil.equals(property.getDtrsnt().getValue(), E_YES___.NO.getValue())) {
				// 登记代发防重登记簿
				KnlIoblEdmContro knlIoblEdmContro = SysUtil.getInstance(KnlIoblEdmContro.class);
				knlIoblEdmContro.setEdmflg(E_PASTAT.UNTREAT);
				knlIoblEdmContro.setRetrdt(CommTools.getBaseRunEnvs().getTrxn_date());
				knlIoblEdmContro.setRetrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
				knlIoblEdmContro.setFinsty(input.getFinsty());
				knlIoblEdmContro.setTranst(E_CUPSST.SUCC);

				KnlIoblEdmControDao.insert(knlIoblEdmContro);

			}
			// 冻结止付处理 收单暂时不冻结 需要进行风控冻结检查
			DpRiskService dpRiskService = SysUtil.getInstance(DpRiskService.class);

			DpRiskType.toTransFroz toTransFro = SysUtil.getInstance(DpRiskType.toTransFroz.class);
			toTransFro.setAcctno(property.getInacct());
			toTransFro.setCustac(property.getCustac());
			toTransFro.setCardno(property.getCardno());
			toTransFro.setTdtrdt(CommTools.getBaseRunEnvs().getTrxn_date());
			toTransFro.setTdtrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			toTransFro.setTranam(property.getCuanam());
			toTransFro.setInmeid(input.getInmeid());
			toTransFro.setRefeno(input.getRefeno());
			toTransFro.setSbrand(input.getOrgaid());
			KnbFrozOutput knbFrozOutput = dpRiskService.transFroz(toTransFro);

			if (CommUtil.isNotNull(knbFrozOutput)) {
				frozfg = knbFrozOutput.getIsFrozen();
				frozam = knbFrozOutput.getFrozam();
			}

			if (CommUtil.equals(E_YES___.YES.getValue(), frozfg.getValue())) {
				property.setRemark("该笔流水已冻结");
			}
		}

		// 如果存在交易流水场景
		if ((CommUtil.equals(property.getPrcsfg().getValue(), E_YES___.YES.getValue())
				&& CommUtil.equals(input.getUntrst().getValue(), E_PASTAT.SUCC.getValue()))
				|| CommUtil.equals(property.getDtrsnt().getValue(), E_YES___.YES.getValue())
				|| (CommUtil.equals(property.getPrcsfg().getValue(), E_YES___.YES.getValue())
						&& CommUtil.equals(input.getUntrst().getValue(), E_PASTAT.FAIL.getValue()))) {
			// 将输入接口中字段复制到交易流水表
			CommUtil.copyProperties(entity, input);

			if (CommUtil.equals(E_YES___.YES.getValue(), property.getDtrsnt().getValue())) {
				entity.setAcfist(E_FINSTY.T1);
				// // 更新代发控制表原D0流水状态
				// KnlIoblEdmContro KnlIoblEdmContro =
				// KnlIoblEdmControDao.selectOne_edmOdb01(entity.getMntrsq(),
				// entity.getTrandt(), false);
				// KnlIoblEdmContro.setTranst(E_CUPSST.STRK);
				// KnlIoblEdmControDao.updateOne_edmOdb01(KnlIoblEdmContro);
			} else {
				entity.setAcfist(input.getFinsty());
			}

			// 处理状态
			if (CommUtil.equals(input.getUntrst().getValue(), E_PASTAT.SUCC.getValue())) {
				entity.setTranst(E_CUPSST.SUCC);// 交易状态成功
			} else if (CommUtil.equals(input.getUntrst().getValue(), E_PASTAT.PROC.getValue())) {
				entity.setTranst(E_CUPSST.PRC);// 交易状态处理中
			} else {
				entity.setTranst(E_CUPSST.FAIL);// 交易状态失败
			}

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
			entity.setUntrst(input.getUntrst());// 交易状态
			entity.setMerate(property.getMerate());// 商户费率
			entity.setFrozfg(frozfg);// 该笔流水是否冻结
			entity.setRemark(property.getRemark());
			entity.setFrozam(frozam);// 冻结金额
			entity.setTmteleno(DecryptConstant.maskMobile(input.getTeleno()));
			entity.setTmfrteno(DecryptConstant.maskMobile(input.getFrteno()));
			entity.setTmpabkno(DecryptConstant.maskBankCard(input.getPabkno()));
			entity.setBscdam(property.getUnionCastInfo().getBscdam());
			entity.setSvntam(property.getUnionCastInfo().getSvntam());
			entity.setUnbdam(property.getUnionCastInfo().getUnbdam());

			if (CommUtil.isNotNull(property.getDtrsnt())
					&& CommUtil.equals(E_YES___.YES.getValue(), property.getDtrsnt().getValue())) {
				entity.setAcfist(E_FINSTY.T1);
				entity.setCoupon(property.getCoupon());// 设置实际抵扣值
			} else {
				entity.setAcfist(input.getFinsty());
				entity.setCoupon(property.getCoupon());// 设置实际抵扣值
			}

			entity.setServtp(property.getServtp());

			KnlIoblCupsDao.updateOne_odb3(entity);
		} else {
			// 将输入接口中字段复制到交易流水表
			CommUtil.copyProperties(entity, input);

			entity.setTmteleno(DecryptConstant.maskMobile(input.getTeleno()));
			entity.setTmfrteno(DecryptConstant.maskMobile(input.getFrteno()));
			entity.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 主交易流水
			entity.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
			entity.setMntrtm(property.getTrantm());
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
			entity.setTmpabkno(DecryptConstant.maskBankCard(input.getPabkno()));
			entity.setBscdam(property.getUnionCastInfo().getBscdam());
			entity.setSvntam(property.getUnionCastInfo().getSvntam());
			entity.setUnbdam(property.getUnionCastInfo().getUnbdam());

			// 处理状态
			if (CommUtil.equals(input.getUntrst().getValue(), E_PASTAT.SUCC.getValue())) {
				entity.setTranst(E_CUPSST.SUCC);// 交易状态成功
			} else if (CommUtil.equals(input.getUntrst().getValue(), E_PASTAT.PROC.getValue())) {
				entity.setTranst(E_CUPSST.PRC);// 交易状态处理中
			} else {
				entity.setTranst(E_CUPSST.FAIL);// 交易状态失败
			}

			// D0转T1结算类型变更
			if (CommUtil.equals(E_YES___.YES.getValue(), property.getDtrsnt().getValue())
					|| CommUtil.equals(E_YES___.NO.getValue(), property.getBindfg().getValue())) {
				entity.setAcfist(E_FINSTY.T1);
				entity.setCoupon(property.getCoupon());// 设置实际抵扣值
			} else {
				entity.setAcfist(input.getFinsty());
				entity.setCoupon(property.getCoupon());// 设置实际抵扣值
			}

			entity.setServtp(property.getServtp());

			KnlIoblCupsDao.insert(entity);

		}

		// 输出接口赋值
		output.setMntrsq(entity.getMntrsq());
		output.setMntrdt(entity.getTrandt());
		output.setMntrtm(entity.getMntrtm());
		output.setCuanam(property.getCuanam());
		output.setFinsty(entity.getAcfist());
		output.setJnanam(entity.getJnanam());
		output.setFrozfg(entity.getFrozfg());
	}

	public static void custDeptDeal(final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Input input,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Property property,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Output output) {
		// 扣减结算费用金额
		BigDecimal cuanam = BigDecimal.ZERO;
		if (CommUtil.equals(input.getFrodfg().getValue(), E_YES___.NO.getValue())) {
			cuanam = input.getTranam().subtract(property.getJnanam());
		} else {
			cuanam = input.getTranam().subtract(property.getReanam());
		}
		property.setCuanam(cuanam);

		property.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));

	}

	public static void shareBenefitDeal(final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Input input,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Property property,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Output output) {
		IoDsManage ioDsManage = SysUtil.getInstance(IoDsManage.class);

		calMechAmtInput calMechAmtInput = SysUtil.getInstance(calMechAmtInput.class);

		//更改服务类型转换方式
		E_BUSITP busitp = DpTransfer.svcodeToBusitp(input.getSvcode());

		property.setBusitp(busitp);

		calMechAmtInput.setBusitp(busitp);// 业务类型
		calMechAmtInput.setMrchno(input.getInmeid());// 内部商户号
		calMechAmtInput.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
		calMechAmtInput.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
		calMechAmtInput.setTrantm(property.getTrantm());// 交易时间
		calMechAmtInput.setTranam(input.getTranam());// 交易金额

		if ((CommUtil.isNotNull(property.getDtrsnt())
				&& CommUtil.equals(E_YES___.YES.getValue(), property.getDtrsnt().getValue()))
				|| CommUtil.equals(E_YES___.NO.getValue(), property.getBindfg().getValue())) {
			calMechAmtInput.setSttltp(E_INTYPE.T1);// 结算方式
		} else {
			calMechAmtInput.setSttltp(E_INTYPE.valueOf(input.getFinsty().getValue()));// 结算方式
		}
		calMechAmtInput.setPosnum(input.getPosnum());// pos终端号
		calMechAmtInput.setSbrand(property.getSbrand());// 品牌id
		calMechAmtInput.setTrnctp(input.getCardtp());// 卡类型
		if (CommUtil.isNotNull(input.getMercfg())) {
			calMechAmtInput.setMechtp(E_MECHTP.get(input.getMercfg().getValue()));
		}
		calMechAmtInput.setMarkfe(input.getMarkfe());
		calMechAmtOutput calMechAmtOutput = ioDsManage.calMechAmt(calMechAmtInput);
		BigDecimal fitamt = calMechAmtOutput.getProfitamt();
		property.setJnanam(fitamt);
		property.setMerate(calMechAmtOutput.getRate_mech());
		property.setAmanam(fitamt.subtract(property.getReanam()).add(calMechAmtOutput.getCoupon()));
		property.setCoupon(calMechAmtOutput.getCoupon());
	}

	public static void unionPayDeal(final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Input input,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Property property,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Output output) {

		UnionCastInfo unionCastInfo = SysUtil.getInstance(UnionPayCostSvcType.class).unionPayCostDeal(
				E_CARDTP.valueOf(input.getCardtp().getValue()), input.getSpcipy(), input.getMercfg(), input.getTranam(),
				input.getSvcode());
		if (CommUtil.isNotNull(unionCastInfo)) {
			property.setReanam(unionCastInfo.getChrgam());
			property.setUnionCastInfo(unionCastInfo);
		}
	}

	public static void shareBenefitPostDeal(final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Input input,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Property property,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Output output) {

		// 业务编码查询赋值
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", property.getSbrand(), "%", "%", false);
		if (CommUtil.isNull(knpParameter)) {
			throw DpModuleError.DpTrans.TS020021();
		}
		String acctno = knpParameter.getParm_value1();// 内部户账号
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
		cplIoAccounttingIntf.setCrcycd(input.getCrcycd());
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

	public static void cashPledgePostDeal(final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Input input,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Property property,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Output output) {

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
		cplIoAccounttingIntf.setCrcycd(input.getCrcycd());
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

	public static void checkBalance(final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Input input,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Property property,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Output output) {
		// 平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(),
				CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._35);
	}

	public static void edBeforeCheckDeal(final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Input input,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Property property,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Output output) {
		// 根据收单流水查询是否代发成功
		KnlIoblEdmContro knlIoblEdmContro = KnlIoblEdmControDao.selectOneWithLock_edmOdb01(output.getMntrsq(),
				output.getMntrdt(), false);

		if (CommUtil.isNull(knlIoblEdmContro)) {
			throw DpModuleError.DpTrans.TS020020();
		}

		// 判断金额是否小于等于零
		if (CommUtil.compare(property.getCuanam(), BigDecimal.ZERO) <= 0) {
			throw DpModuleError.DpTrans.TS020058();
		}

		// 判断该笔流水是否冻结
		if (CommUtil.equals(property.getKnlioblcups().getFrozfg().getValue(), E_YES___.YES.getValue())) {
			throw DpModuleError.DpTrans.TS020059();
		}

		// 如果有记录直接返回
		if (!CommUtil.equals(knlIoblEdmContro.getEdmflg().getValue(), E_PASTAT.UNTREAT.getValue())
				&& CommUtil.equals(property.getEdflge().getValue(), E_YES___.YES.getValue())) {
			KnlIoblEdm knlIoblEdm = KnlIoblEdmDao.selectOne_odb01(knlIoblEdmContro.getMntrsq(),
					knlIoblEdmContro.getTrandt(), false);

			output.setEdtrdt(knlIoblEdm.getTrandt());
			output.setEdtrsq(knlIoblEdm.getMntrsq());
			output.setEdtrtm(knlIoblEdm.getMntrtm());
			output.setCdopac(knlIoblEdm.getSacdno());
			output.setAcctna(knlIoblEdm.getSacdna());
			output.setOpbrch(knlIoblEdm.getBankno());
			output.setBrchna(knlIoblEdm.getSabkna());
			output.setEdmflg(E_YES___.YES);
			property.setEdflge(E_YES___.NO);
			return;
		}

		property.setEdtrsq(DpTools.genSequenceWithTrandt("edtrsq", 10));
		property.setEdtrdt(CommTools.getBaseRunEnvs().getComputer_date());//代发日期为服务器日期

		knlIoblEdmContro.setEdmflg(E_PASTAT.PROC);// 是否代发
		knlIoblEdmContro.setMntrsq(property.getEdtrsq());
		knlIoblEdmContro.setTrandt(property.getEdtrdt());

		KnlIoblEdmControDao.updateOne_edmOdb01(knlIoblEdmContro);

		property.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));

		// 业务编码查询赋值
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", "D0", "%", "%", false);
		if (CommUtil.isNull(knpParameter)) {
			throw DpModuleError.DpTrans.TS020021();
		}
		// 待清算业务编码
		property.setEdbino(knpParameter.getParm_value1());
	}

	public static void afterDeal(final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Input input,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Property property,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Output output) {

		// 查询绑卡信息
		KnaCacd knaCacd = property.getKnaCacd();
		// 去掉截取生成流水
		// String mntrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq().substring(8,
		// 25);
		// 后期是否需要登记原支付前置流水和日期
		KnlIoblEdm knlIoblEdm = SysUtil.getInstance(KnlIoblEdm.class);
		knlIoblEdm.setAcctid(input.getInmeid());
		knlIoblEdm.setFinsty(input.getFinsty());
		knlIoblEdm.setMntrsq(property.getEdtrsq());
		knlIoblEdm.setTrandt(property.getEdtrdt());
		knlIoblEdm.setMntrtm(BusiTools.getTraxTmInRunEnv());
		knlIoblEdm.setTranam(property.getCuanam());
		knlIoblEdm.setTranst(E_CUPSST.CLWC);
		knlIoblEdm.setRetrsq(output.getMntrsq());
		knlIoblEdm.setRetrdt(output.getMntrdt());
		knlIoblEdm.setSacdno(knaCacd.getCdopac());
		knlIoblEdm.setTmsacdno(knaCacd.getTmcdopac());
		knlIoblEdm.setSacdna(knaCacd.getAcctna());
		knlIoblEdm.setTmsacdna(knaCacd.getTmacctna());
		knlIoblEdm.setBankno(knaCacd.getOpbrch());
		knlIoblEdm.setSabkna(knaCacd.getBrchna());
		knlIoblEdm.setPuacfg(knaCacd.getCopefg());
		knlIoblEdm.setCardtp(knaCacd.getCardtp());
		knlIoblEdm.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		
		KnlIoblEdmDao.insert(knlIoblEdm);

		// 平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(),
				CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._35);

		output.setEdtrsq(property.getEdtrsq());
		output.setEdtrdt(property.getEdtrdt());
		output.setEdtrtm(knlIoblEdm.getMntrtm());
		output.setCdopac(knaCacd.getCdopac());
		output.setAcctna(knaCacd.getAcctna());
		output.setOpbrch(knaCacd.getOpbrch());
		output.setBrchna(knaCacd.getBrchna());
		output.setEdmflg(E_YES___.YES);
	}

	public static void voucfePostDeal(final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Input input,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Property property,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctin.Output output) {
		// 商户存款加上抵扣金额
		// property.setCuanam(property.getCuanam().add(property.getCoupon()));

		// 业务编码查询赋值
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTIN", property.getSbrand(), "%", "%", false);
		if (CommUtil.isNull(knpParameter)) {
			throw DpModuleError.DpTrans.TS020021();
		}
		String acctno = knpParameter.getParm_value1();// 内部户账号
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
		cplIoAccounttingIntf.setCrcycd(input.getCrcycd());
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
}
