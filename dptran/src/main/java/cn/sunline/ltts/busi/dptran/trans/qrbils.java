package cn.sunline.ltts.busi.dptran.trans;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.busi.dp.errors.DpAcError;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnlBill;
import cn.sunline.ltts.busi.sys.dict.DpDict;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;


public class qrbils {
	public static void qrKnlBillList(
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrbils.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrbils.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrbils.Output output) {

		if (CommUtil.isNull(input.getCardno())) {
			throw DpAcError.DpDeptComm.E9027(DpDict.Acct.cardno.getLongName());
		}
		if (CommUtil.isNull(input.getCrcycd())) {
			throw DpAcError.DpDeptComm.E9027(DpDict.Acct.crcycd.getLongName());
		}
		if (CommUtil.isNull(input.getPageno())) {
			throw DpModuleError.DpstComm.BNAS0249();
		}
		if (CommUtil.isNull(input.getPagect())) {
			throw DpModuleError.DpstComm.BNAS0463();
		}
		
		long start = (input.getPageno() - 1) * input.getPagect();
		long count = input.getPagect();
		String corpno = BusiTools.getTranCorpno();
		String custac = CaTools.getCustacByCardno(input.getCardno());
		KnaAcct	knaAcct = CapitalTransDeal.getSettKnaAcctSub(custac, E_ACSETP.SA);
		
		Options<IoDpKnlBill> knlBills = SysUtil.getInstance(Options.class);
		long counts = DpAcctDao.selKnlBillByAcctnoCount(knaAcct.getAcctno(), corpno, false);
		if (counts > 0) {
			List<IoDpKnlBill> ioDpKnlBills = DpAcctDao.selKnlBillByAcctno(knaAcct.getAcctno(), corpno, start, count, false);
			for (IoDpKnlBill ioDpKnlBill : ioDpKnlBills) {
				knlBills.add(ioDpKnlBill);
			}
			output.setBillls(knlBills);
		}
		CommTools.getBaseRunEnvs().setTotal_count(counts);
	}
}
