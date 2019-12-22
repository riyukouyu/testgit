package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.biz.tables.MsOnlTable.MssTransaction;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;


public class transq {

public static void QryKnsTranInfo( final cn.sunline.ltts.busi.dptran.trans.intf.Transq.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Transq.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Transq.Output output){
	
	// 输入项检查。
	String transq = input.getTransq();
	String trandt = input.getTrandt(); 
	/*
	 * M：不检查。
	if (CommUtil.isNull(transq) && CommUtil.isNull(trandt)){
		throw DpModuleError.BizAcct.E0001();
	}
	 */
	Long pageSize = CommTools.getBaseRunEnvs().getPage_size();
	Long pageStart = CommTools.getBaseRunEnvs().getPage_start();
	
	Page<MssTransaction> psMssTran = CaDao.selMssTranInfo(transq, trandt, (pageStart-1)*pageSize, pageSize, 0, false);
	// 返回查询列表信息。
	output.getMssTranInfo().setValues(psMssTran.getRecords());
	CommTools.getBaseRunEnvs().setTotal_count(psMssTran.getRecordCount());
}
}
