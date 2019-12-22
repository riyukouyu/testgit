package cn.sunline.ltts.busi.dptran.trans.lzbank;


import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.dp.type.CustInfoCountType.LituqrTurnoverStatus;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CLERST;


public class lituqr {
	/**
	 * 清算流水状态查询
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void liTuqrTran( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Lituqr.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Lituqr.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Lituqr.Output output){
		String clerdt = input.getClerdt();
		E_CLERST status = input.getStatus();
		
		  //页码
	 	long pageno = CommTools.getBaseRunEnvs().getPage_start();
	 	//页容量
	 	long pgsize = CommTools.getBaseRunEnvs().getPage_size();
	 	
		long totlCount = 0;
		
		Page<LituqrTurnoverStatus> list = CaDao.selKnsacsqcoll(clerdt, status, (pageno-1)*pgsize, pgsize, totlCount, false);
		if(list.getRecordCount()>0){
			output.getResults().addAll(list.getRecords());
			CommTools.getBaseRunEnvs().setTotal_count(list.getRecordCount());// 记录总数
		}		
	}
}
