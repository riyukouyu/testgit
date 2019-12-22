
package cn.sunline.edsp.busi.dptran.trans;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppHold;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbCommSvr;
import cn.sunline.ltts.busi.iobus.type.IoApPubComplex.AppHoldInfo;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlCary;

public class holdad {

	private static final BizLog bizlog = BizLogUtil
			.getBizLog(holdad.class);
	public static void holidDayAdd(final cn.sunline.edsp.busi.dptran.trans.intf.Holdad.Input input,
			final cn.sunline.edsp.busi.dptran.trans.intf.Holdad.Property property,
			final cn.sunline.edsp.busi.dptran.trans.intf.Holdad.Output output) {

//		IoSaveIoTransBill ioSaveIoTransBill = SysUtil.getInstance(IoSaveIoTransBill.class);
//		IoKnlCary cary = SysUtil.getInstance(IoKnlCary.class);
//		cary.setAcctno("1231");
//		cary.setServdt("20190904");
//		cary.setServsq("2019090410001");
//		
//		ioSaveIoTransBill.SaveActoacPeerReversal(cary);
//		
//		IoPbCommSvr ioPbCommSvr = SysUtil.getRemoteInstance(IoPbCommSvr.class);
//		AppHoldInfo appHoldInfo = SysUtil.getInstance(AppHoldInfo.class);
//		AppHold appHold = input.getHold();
//		appHoldInfo.setCorpno(appHold.getCorpno());
//		appHoldInfo.setEfctdt(appHold.getEfctdt());
//		appHoldInfo.setFilesq(appHold.getFilesq());
//		appHoldInfo.setHolday(appHold.getHolday());
//		appHoldInfo.setHoldcd(appHold.getHoldcd());
//		appHoldInfo.setHoldtp(appHold.getHoldtp());
//		appHoldInfo.setRemark(appHold.getRemark());
	
		bizlog.debug("CommTools.getBaseRunEnvs().getCall_out_seq():"+CommTools.getBaseRunEnvs().getCall_out_seq());
		bizlog.debug("CommTools.getBranchSeq():"+CommTools.getBranchSeq());
		bizlog.debug("CommTools.getBaseRunEnvs().getInitiator_seq():"+CommTools.getBaseRunEnvs().getInitiator_seq());
		
//		ioPbCommSvr.insHolidayInfo(appHoldInfo);
		
		throw DpModuleError.DpTrans.TS020021();
		
		
		
	}
}
