package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.fe.namedsql.FeSceneDao;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgSecd;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrsecd {
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：查询场景计费管理
	 *         </p>
	 * @param @param input
	 * @param @param output
	 * @return void
	 * @throws
	 */

	public static void qrsecd( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrsecd.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrsecd.Output output){
   
		String scencd = input.getScencd(); //场景代码
	    String chrgcd = input.getChrgcd();//费种代码
		long pageno = CommTools.getBaseRunEnvs().getPage_start();//页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();//页容量
	
		long totlCount = 0;
	
	    Page<IoCgSecd> lstkcp_chrg_scdf = FeSceneDao.selall_kcp_chrg_scdf(scencd, chrgcd, (pageno-1)*pgsize, pgsize, totlCount, false);
	    Options<IoCgSecd> optkcp_chrg_scdf = new DefaultOptions<IoCgSecd>();// 初始化输出对象
	    optkcp_chrg_scdf.addAll(lstkcp_chrg_scdf.getRecords());
	    output.setPinfos(optkcp_chrg_scdf);
	    
		CommTools.getBaseRunEnvs().setTotal_count(lstkcp_chrg_scdf.getRecordCount());// 记录总数

	}
}
