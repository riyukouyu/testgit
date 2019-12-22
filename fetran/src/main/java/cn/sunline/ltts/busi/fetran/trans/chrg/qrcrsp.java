package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.fe.namedsql.FeShareDao;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgCrsp;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrcrsp {
	private static final BizLog bizlog = BizLogUtil.getBizLog(qrcrsp.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：查询分润管理
	 *         </p>
	 * @param @param input
	 * @param @param output
	 * @return void
	 * @throws
	 */

	public static void qrcrsp( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcrsp.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcrsp.Output output){
		
		bizlog.method("<<<<<< qrdvid begin >>>>>>"); 
		
		String chrgcd = input.getChrgcd();
		long pageno = CommTools.getBaseRunEnvs().getPage_start();//页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();//页容量
		
		long totlCount = 0;
		
		Page<IoCgCrsp> lstkcp_chrg_dvid =  FeShareDao.selall_kcp_chrg_dvid(chrgcd, (pageno-1)*pgsize, pgsize, totlCount, false);
				
		Options<IoCgCrsp> optkcp_chrg_dvid = new DefaultOptions<IoCgCrsp>();// 初始化输出对象
		optkcp_chrg_dvid.addAll(lstkcp_chrg_dvid.getRecords());
		output.setPinfos(optkcp_chrg_dvid);
		
		CommTools.getBaseRunEnvs().setTotal_count(lstkcp_chrg_dvid.getRecordCount());// 记录总数
	
		bizlog.method("<<<<<< qrdvid end >>>>>>");
	}
}
