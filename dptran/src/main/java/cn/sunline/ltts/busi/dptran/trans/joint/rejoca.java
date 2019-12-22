package cn.sunline.ltts.busi.dptran.trans.joint;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.ca.base.DpCheckPubic;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIobl;
import cn.sunline.ltts.busi.dptran.trans.client.cupstr;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMONDR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_JOTRTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class rejoca {

	private static BizLog bizLog = BizLogUtil.getBizLog(cupstr.class);

	public static void beforeMethod(
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Rejoca.Input input,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Rejoca.Property property,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Rejoca.Output output) {

		RunEnvsComm runEnvs = CommTools.getBaseRunEnvs();
		String cardno = input.getCardno();
		String crcycd = input.getCrcycd();
		String joinac = input.getJoinac();
		String platno = input.getPlatno();
		BigDecimal tranam = input.getTranam();

		if (CommUtil.isNull(cardno)) {
			throw DpModuleError.DpstComm.E9027("交易卡号");
		}
		if (CommUtil.isNull(crcycd)) {
			throw DpModuleError.DpstComm.E9027("币种");
		}
		if (CommUtil.isNull(joinac)) {
			throw DpModuleError.DpstComm.E9027("联名账号");
		}
		if (CommUtil.isNull(platno)) {
			throw DpModuleError.DpstComm.E9027("平台编号");
		}
		if (CommUtil.isNull(tranam)) {
			throw DpModuleError.DpstComm.E9027("交易金额");
		} else if (CommUtil.compare(tranam, BigDecimal.ZERO) < 0) {
			throw DpModuleError.DpstProd.E0010("交易金额必须大于0！");
		}

		String custac = CaTools.getCustacByCardnoCheckStatu(cardno);
		String busino = DpCheckPubic.getBusinoByPlatno(platno);
		DpCheckPubic.chkJointInfo(custac, platno, tranam, E_JOTRTP.CZ);
		
		// 重复提交检查
		String transq = runEnvs.getMain_trxn_seq();
		String trandt = runEnvs.getTrxn_date();
		String brchno = runEnvs.getTrxn_branch();
		KnlIobl iobl = ActoacDao.selKnlIoblDetl(transq, trandt, false);
		if (CommUtil.isNotNull(iobl)) {
			property.setIssucc(E_YES___.YES);
			output.setTransq(transq);
			output.setTrandt(trandt);
			output.setTranam(tranam);
			output.setAcctbl(BigDecimal.ZERO); // TODO 添加交易后金额
			bizLog.debug("验证流水已存在！");
			return;
		} else {
			property.setIssucc(E_YES___.NO);
			bizLog.debug("验证流水不存在！");
		}

		DpCheckPubic.chkCardno(cardno);
		DpCheckPubic.chkAcctStatus(custac);
		String acctno = DpCheckPubic.chkUsebal(custac, tranam);
	
		property.setCustac(custac); // 电子账号ID
		property.setAcctno(acctno); // 子账号
		property.setIncorp("");
		property.setOtcorp("");
		property.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
		property.setServsq(runEnvs.getMain_trxn_seq()); // 交易流水
		property.setServdt(trandt); // 交易日期
		property.setFrondt(runEnvs.getInitiator_seq());// 渠道来源日期
		property.setFronsq(runEnvs.getInitiator_seq()); // 渠道来源日期
		property.setLinkno(null); // 连笔号
		property.setPrcscd(runEnvs.getTrxn_code()); // 交易码
		property.setIoflag(E_IOFLAG.OUT);
		// 出金
		property.setTrantp(E_TRANTP.TR); // 交易类型
		property.setBrchno(brchno);

		property.setClactp(E_CLACTP._10);
		property.setAcbrch(brchno);
		property.setBusino(busino); // 业务编码
		property.setAmondr(E_AMONDR.UP);
	}

	public static void afterMethod(
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Rejoca.Input input,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Rejoca.Property property,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Rejoca.Output output) {
		RunEnvsComm runEnvs = CommTools.getBaseRunEnvs();
		output.setTransq(runEnvs.getTrxn_seq());
		output.setTrandt(runEnvs.getTrxn_date());
		output.setTranam(input.getTranam());
	}
}
