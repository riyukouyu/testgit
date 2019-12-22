package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.fe.namedsql.FeFormulaDao;
import cn.sunline.ltts.busi.fe.type.FeConfigTranType.CgKcpChrgFmdf;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrcmdf {
	private static final BizLog bizlog = BizLogUtil.getBizLog(qrcmdf.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：计费公式定义表查询
	 *         </p>
	 * @param @param input
	 * @param @param output
	 * @return void
	 * @throws
	 */
	public static void qrcmdf( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcmdf.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcmdf.Output output){
		
		bizlog.method("<<<<<< qrcmdf begin >>>>>>");
	    
		String chrgfm = input.getChrgfm(); //计费公式代码
	    String fmunam = input.getFmunam(); //计费公式名称
		long pageno = CommTools.getBaseRunEnvs().getPage_start();//页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();//页容量
		long totlCount = 0;
		long count = 0;
	        
	    Page<CgKcpChrgFmdf> lstkcp_chrg_fmdf = FeFormulaDao.
	            selall_kcp_chrg_fmdf(chrgfm, fmunam, (pageno-1)*pgsize, pgsize, totlCount, false);
	    Options<CgKcpChrgFmdf> optkcp_chrg_fmdf = new DefaultOptions<CgKcpChrgFmdf>();// 初始化输出对象
	    optkcp_chrg_fmdf.addAll(lstkcp_chrg_fmdf.getRecords());
	    output.setPinfos(optkcp_chrg_fmdf);
	//	output.setPcount(count + lstkcp_chrg_fmdf.getRecordCount());
		
		CommTools.getBaseRunEnvs().setTotal_count(lstkcp_chrg_fmdf.getRecordCount());// 记录总数

	}
}
