package cn.sunline.ltts.busi.intran.trans;

import cn.sunline.ltts.busi.intran.batchtran.dayend.glaccrDataProcessor;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class upacif {
	private static final BizLog bizlog = BizLogUtil.getBizLog(glaccrDataProcessor.class);
	public static void afterProcess( final cn.sunline.ltts.busi.intran.trans.intf.Upacif.Input input,  
			final cn.sunline.ltts.busi.intran.trans.intf.Upacif.Output output){
//		if(DcnUtil.isAdminDcn(DcnUtil.getCurrDCN())) {
//			String acctno = input.getAcctno();
//			GlKnaAcct acctInfo = InQuerySqlsDao.sel_GlKnaAcct_by_acct(acctno, true);
//			if(acctInfo.getRlbltg() == E_RLBLTG._1) {//实时余额内部户都在管理节点，不需要调用零售节点维护
//				return;
//			}
//			List<String> dcnList = DcnUtil.findAllDcnNosWithAdmin();
//			for(String dcn : dcnList) {
//				if(!DcnUtil.isAdminDcn(dcn)) {
//					bizlog.debug("call retail dcn:[%s]", dcn);
//					UpInacInfo upInacInfoServ = SysUtil.getRemoteInstance(UpInacInfo.class);
//					UpInacInfo.ModInacName.InputSetter remoteInput = SysUtil.getInstance(UpInacInfo.ModInacName.InputSetter.class);
//					UpInacInfo.ModInacName.Output remoteoutput = SysUtil.getInstance(UpInacInfo.ModInacName.Output.class);
//					CommUtil.copyProperties(remoteInput, input);
//					remoteInput.setCdcnno(dcn);
//					upInacInfoServ.modInacName(remoteInput,remoteoutput);
//					bizlog.debug("UpInacInfo.modInacName call retail dcn:[%s],output:[%s]", dcn,remoteoutput);
//				}
//			}
//		}
	}
}
