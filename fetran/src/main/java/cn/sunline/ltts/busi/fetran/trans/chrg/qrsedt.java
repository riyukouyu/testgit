package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.fe.namedsql.FeSceneDao;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgSedt;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrsedt {
	private static final BizLog bizlog = BizLogUtil.getBizLog(qrsedt.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：场景事件明细查询
	 *         </p>
	 * @param @param input
	 * @param @param output
	 * @return void
	 * @throws
	 */

	public static void qrsedt( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrsedt.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrsedt.Output output){
		
		bizlog.method("<<<<<< qrsedt begin >>>>>>");
		
		String scencd = input.getScencd();//场景代码
		String scends = input.getScends();//场景名称
		long pageno = CommTools.getBaseRunEnvs().getPage_start();//页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();//页容量
		
		long totlCount = 0;
	
		//根据场景代码/场景名称查询场景代码明细，支持模糊查询
		Page<IoCgSedt> lstkcp_scev_detl = FeSceneDao.selall_kcp_scev_detl(scencd, scends, (pageno-1)*pgsize, pgsize, totlCount, false);
		Options<IoCgSedt> optkcp_scev_detl = new DefaultOptions<IoCgSedt>();// 初始化输出对象
		
		optkcp_scev_detl.addAll(lstkcp_scev_detl.getRecords());
		//输出	
		output.setPinfos(optkcp_scev_detl);
		
		CommTools.getBaseRunEnvs().setTotal_count(lstkcp_scev_detl.getRecordCount());// 记录总数

	}
}
