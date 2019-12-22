package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.fe.namedsql.FeSceneDao;
import cn.sunline.ltts.busi.fe.type.FeConfigTranType.CgkcpScevDefn;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrsedf {
	private static final BizLog bizlog = BizLogUtil.getBizLog(qrsedf.class);
	/**
	 * 
	 * @Title: qrsedf 
	 * @Description: (查询场景事件) 
	 * @param input
	 * @param output
	 * @author leipeng
	 * @date 2016年7月7日 下午8:17:04 
	 * @version V2.3.0
	 */
	 
	public static void qrsedf( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrsedf.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrsedf.Output output){
		
		bizlog.method("<<<<<< qrsedf begin >>>>>>");
		
		String evetcd = input.getEvetcd();
	    String evetna = input.getEvetna();
		long pageno = CommTools.getBaseRunEnvs().getPage_start();//页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();//页容量
		
		long totlCount = 0;
	    	
	    Page<CgkcpScevDefn> lstkcp_scev_defn = FeSceneDao.
	            selall_kcp_scev_defn(evetcd, evetna,(pageno-1)*pgsize, pgsize, totlCount, false);
	    Options<CgkcpScevDefn> optkcp_scev_defn = new DefaultOptions<CgkcpScevDefn>();// 初始化输出对象
	    optkcp_scev_defn.addAll(lstkcp_scev_defn.getRecords());
	    output.setPinfos(optkcp_scev_defn);
	    
		CommTools.getBaseRunEnvs().setTotal_count(lstkcp_scev_defn.getRecordCount());// 记录总数
	
	    bizlog.method("<<<<<< qrsedf end >>>>>>");
	}
}
