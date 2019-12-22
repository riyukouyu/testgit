package cn.sunline.ltts.busi.dptran.trans;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.dp.tables.DpTransferAcct.KnbAcctSign;
import cn.sunline.ltts.busi.dp.tables.DpTransferAcct.KnbAcctSignDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;

public class tranct {

	public static void imtranct(
			final cn.sunline.ltts.busi.dptran.trans.intf.Tranct.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Tranct.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Tranct.Output output) {
		if (CommUtil.isNotNull(input.getTranam())
				&& CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0) {
			throw DpModuleError.DpstAcct.E9999("划转金额需大于0");
		}
		String custac = CaTools.getCustacByCardnoCheckStatu(input.getCardno());
		KnaAcct knaAcct = KnaAcctDao.selectFirst_odb9(E_ACSETP.SA, custac, true);	
		KnbAcctSign knbASign = KnbAcctSignDao.selectOne_odb1(
				knaAcct.getAcctno(), false);
		if (CommUtil.isNull(knbASign)) {
			KnbAcctSign knbAcctSign = SysUtil.getInstance(KnbAcctSign.class);
			knbAcctSign.setAcctno(knaAcct.getAcctno());
			knbAcctSign.setCorpno(BusiTools.getTranCorpno());
			knbAcctSign.setCustac(custac);
			knbAcctSign.setTranam(input.getTranam());
			knbAcctSign.setTgacct(input.getTgctno());
			knbAcctSign.setTrdate(input.getTrdate());
			knbAcctSign.setCreadt(CommTools.getBaseRunEnvs().getTrxn_date());
			knbAcctSign.setCardno(input.getCardno());
			knbAcctSign.setCustno(knaAcct.getCustno());
			KnbAcctSignDao.insert(knbAcctSign);	
		} else {
			knbASign.setTranam(input.getTranam());
			knbASign.setTgacct(input.getTgctno());
			knbASign.setTrdate(input.getTrdate());
			knbASign.setUpdadt(CommTools.getBaseRunEnvs().getTrxn_date());
			KnbAcctSignDao.updateOne_odb1(knbASign);
		}
		//poc增加审计日志
		ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
		apAudit.regLogOnInsertBusiPoc(null);
	}
}
