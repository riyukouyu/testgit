package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.fe.namedsql.FeCodeDao;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrcaab {
	private static final BizLog bizlog = BizLogUtil.getBizLog(qrcaab.class);
		/**
		 * 
		 * @Author levi
		 *         <p>
		 *         功能说明：查询费种代码核算属性
		 *         </p>
		 * @param @param input
		 * @return void
		 * @throws
		 */
	public static void qrcaab( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcaab.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcaab.Output output){
		
		bizlog.method("<<<<<< qrcaab begin >>>>>>");
		
		String chrgcd = input.getChrgcd();//费种代码
		String chrgna = input.getChrgna();//费种代码名称
		String scencd = input.getScencd(); //场景代码
		long pageno = CommTools.getBaseRunEnvs().getPage_start();//页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();//页容量
		
		long totlCount = 0;
		
        Page<IoFeChrgComplexType.IoCgCaab> lstCgCaab = FeCodeDao.selall_kcp_chrg_subj(chrgcd, chrgna, scencd, (pageno-1)*pgsize, pgsize, totlCount, false);
        Options<IoFeChrgComplexType.IoCgCaab> optCgCaab = new DefaultOptions<IoFeChrgComplexType.IoCgCaab>();
        optCgCaab.addAll(lstCgCaab.getRecords());
        output.setPinfos(optCgCaab);
        
		CommTools.getBaseRunEnvs().setTotal_count(lstCgCaab.getRecordCount());// 记录总数

	}
}
