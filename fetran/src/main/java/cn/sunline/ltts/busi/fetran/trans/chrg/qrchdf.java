package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.fe.namedsql.FeCodeDao;
import cn.sunline.ltts.busi.fe.type.FeConfigTranType.CgKcpChrg;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_CGPYRV;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_FETYPE;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

public class qrchdf {
	private static final BizLog bizlog = BizLogUtil.getBizLog(qrchdf.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：查询费种代码定义表
	 *         </p>
	 * @param @param input
	 * @param @param output
	 * @return void
	 * @throws
	 */
	public static void qrchdf(final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrchdf.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrchdf.Output output) {
		
		bizlog.method("<<<<<< qrchdf begin >>>>>>");
		
		String chrgcd = input.getChrgcd();
		String chrgna = input.getChrgna();
		E_FETYPE fetype = input.getFetype();
		E_CGPYRV cgpyrv = input.getCgpyrv();
//		E_CRCYCD crcycd = E_CRCYCD.RMB; // 默认人民币

		long pageno = CommTools.getBaseRunEnvs().getPage_start();// 页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();// 页容量

		long totlCount = 0;
		
		// 查询费种代码定义，支持模糊查询
		Page<CgKcpChrg> lstKcpchrg = FeCodeDao.selall_kcp_chrg(chrgcd, chrgna,
				null, fetype, cgpyrv, (pageno - 1) * pgsize, pgsize, totlCount, false);
		Options<CgKcpChrg> optKcpchrg = new DefaultOptions<CgKcpChrg>();// 初始化输出对象
		optKcpchrg.addAll(lstKcpchrg.getRecords());
		// 输出
		output.setPinfos(optKcpchrg);
		
		CommTools.getBaseRunEnvs().setTotal_count(lstKcpchrg.getRecordCount());// 记录总数

	}
}
