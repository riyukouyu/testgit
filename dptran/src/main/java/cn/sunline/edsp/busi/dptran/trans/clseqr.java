
package cn.sunline.edsp.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.busi.dp.namedsql.ca.AccountFlowDao;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUPSST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_PASTAT;
import cn.sunline.edsp.busi.ds.iobus.type.DsEnumType.E_BUSITP;
import cn.sunline.ltts.busi.dp.type.DpTransfer.DpKnlCleaseInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoKnsAcsqInfo;

public class clseqr {

	public static void qryCleseInfo(final cn.sunline.edsp.busi.dptran.trans.intf.Clseqr.Input input,
			final cn.sunline.edsp.busi.dptran.trans.intf.Clseqr.Property property,
			final cn.sunline.edsp.busi.dptran.trans.intf.Clseqr.Output output) {

		Long pageSize = CommTools.getBaseRunEnvs().getPage_size();
		Long pageStart = CommTools.getBaseRunEnvs().getPage_start();

		String sbrand = input.getSbrand();
		Options<String> unkpdt = input.getUnkpdt();
		String busitp = null;
		String inmeid = input.getInmeid();
		String teleno = input.getTeleno();
		String mntrsq = input.getMntrsq();
//		E_PASTAT untrst = input.getUntrst();
//		E_CUPSST transt = input.getTranst();

		String untrst = null;
		String transt = null;
		
		String unttbg = null;
		String untted = null;
		if(CommUtil.isNotNull(unkpdt)) {
			unttbg = unkpdt.get(0);
			untted = unkpdt.get(1);
		}
		
		if(CommUtil.isNotNull(input.getBusitp())) {
			busitp = input.getBusitp().getValue();
		}

		if(CommUtil.isNotNull(input.getUntrst()))
		{
			untrst = input.getUntrst().getValue();
		}
		
		if(CommUtil.isNotNull(input.getTranst()))
		{
			transt = input.getTranst().getValue();
		}
		
		Page<DpKnlCleaseInfo> knlCleaseInfo = AccountFlowDao.selectKnlCleaseInfo(sbrand, unttbg, untted,
				busitp, inmeid, teleno, mntrsq, untrst, transt,
				(pageStart - 1) * pageSize, pageSize, 0, false);

		output.getCleaseInfoList().addAll(knlCleaseInfo.getRecords());
		 CommTools.getBaseRunEnvs().setTotal_count(knlCleaseInfo.getRecordCount());
	
	}
}
