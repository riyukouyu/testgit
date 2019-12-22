package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgDiva;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrdiva {
	private static final BizLog bizlog = BizLogUtil.getBizLog(qrdiva.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：查询维度值管理
	 *         </p>
	 * @param @param input
	 * @param @param output
	 * @return void
	 * @throws
	 */

	public static void qrdiva( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrdiva.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrdiva.Output output){
		
		bizlog.method("<<<<<< bizlog begin >>>>>>");
		
		String dimecg = input.getDimecg(); // 维度类别
		String dimevl = input.getDimevl(); // 维度值
		String expmsg = input.getExpmsg(); // 维度名称
		long pageno = CommTools.getBaseRunEnvs().getPage_start();// 页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();// 页容量

		long totlCount = 0;

		Page<IoCgDiva> lstKcpscevdime = FeDimeDao.
				selall_kcp_scev_dime(dimecg, dimevl, expmsg, (pageno - 1) * pgsize, pgsize, totlCount, false);
		Options<IoCgDiva> optKcpscevdime = new DefaultOptions<IoCgDiva>();// 初始化输出对象
		optKcpscevdime.addAll(lstKcpscevdime.getRecords());
		output.setPinfos(optKcpscevdime);

		CommTools.getBaseRunEnvs().setTotal_count(lstKcpscevdime.getRecordCount());// 记录总数

	}
}
