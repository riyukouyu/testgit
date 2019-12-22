package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgCplj;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrcplj {
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：查询优惠计划明细
	 *         </p>
	 * @param @param input
	 * @param @param output
	 * @return void
	 * @throws
	 */

	public static void qrcplj( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcplj.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcplj.Output output){
		String diplcd = input.getDiplcd(); //优惠计划代码
	 	long pageno = CommTools.getBaseRunEnvs().getPage_start();//页码
	 	long pgsize = CommTools.getBaseRunEnvs().getPage_size();//页容量
	
		long totlCount = 0;
	 
	    Page<IoCgCplj> lstkcp_favo_pljo = FeDiscountDao. selall_kcp_favo_pljo(diplcd, null,null, (pageno-1)*pgsize, pgsize,totlCount, false);
	    Options<IoCgCplj> optkcp_favo_pljo = new DefaultOptions<IoCgCplj>();// 初始化输出对象
	    optkcp_favo_pljo.addAll(lstkcp_favo_pljo.getRecords());
	    output.setPinfos(optkcp_favo_pljo);
	     
		CommTools.getBaseRunEnvs().setTotal_count(lstkcp_favo_pljo.getRecordCount());// 记录总数
	
	}
}
