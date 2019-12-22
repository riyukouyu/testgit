
package cn.sunline.edsp.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.busi.dp.namedsql.ca.AccountFlowDao;
//import cn.sunline.ltts.busi.dp.type.DpTransfer.DpKnlTrpdqrInfo;

public class trpdqr {

//public static void qryTrpdqrInfo( final cn.sunline.edsp.busi.dptran.trans.intf.Trpdqr.Input input,  final cn.sunline.edsp.busi.dptran.trans.intf.Trpdqr.Property property,  final cn.sunline.edsp.busi.dptran.trans.intf.Trpdqr.Output output){
//	
//	
//	
//	Long pageSize = CommTools.getBaseRunEnvs().getPage_size();
//	Long pageStart = CommTools.getBaseRunEnvs().getPage_start();
//
//	String atsvna = input.getAtsvna();
//	Options<String> prepdt = input.getPrepdt();
//	String inmeid = input.getInmeid();
//	String teleno = input.getTeleno();
//	String busitp = null;
//
//
//	String prepbg = null;
//	String preped = null;
//	if(CommUtil.isNotNull(prepdt)) {
//		prepbg = prepdt.get(0);
//		preped = prepdt.get(1);
//	}
//	
//	if(CommUtil.isNotNull(input.getBusitp())) {
//		busitp = input.getBusitp().getValue();
//	}

//	Page<DpKnlTrpdqrInfo> knlTrpdqrInfo = AccountFlowDao.selectKnlTrpdqrInfo(atsvna, busitp, inmeid, teleno, preped, prepbg, (pageStart - 1) * pageSize, pageSize, 0, false);
//			
//	output.getTrpdqrInfoList().addAll(knlTrpdqrInfo.getRecords());
//	 CommTools.getBaseRunEnvs().setTotal_count(knlTrpdqrInfo.getRecordCount());
//}
}
