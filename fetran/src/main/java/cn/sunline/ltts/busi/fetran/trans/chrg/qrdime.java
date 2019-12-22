package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpDime;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_WAYTYP;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrdime {
	private static final BizLog bizlog = BizLogUtil.getBizLog(qrdime.class);		
	
    /**
     * 
     * @Title: qrdime 
     * @Description: 维度信息参数表查询 
     * @param input
     * @param output
     * @author songliangwei
     * @date 2016年7月8日 下午2:32:07 
     * @version V2.3.0
     */
	public static void qrdime( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrdime.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrdime.Output output){
		
		bizlog.method("<<<<<< qrdime begin >>>>>>");
		
		//维度类型
		E_WAYTYP waytyp = input.getWaytyp();
		//维度类别
	    String dimecg = input.getDimecg(); 
	    //维度名称
	    String dimena = input.getDimena();
	    
	    //页码
	 	long pageno = CommTools.getBaseRunEnvs().getPage_start();
	 	//页容量
	 	long pgsize = CommTools.getBaseRunEnvs().getPage_size();
	 	
		long totlCount = 0;
		
		Page<KcpDime> lstkcp_dime = FeDimeDao.
                selall_kcp_dime(waytyp, dimecg,dimena, (pageno-1)*pgsize, pgsize, totlCount, false);
        Options<KcpDime> optkcp_dime = new DefaultOptions<KcpDime>();// 初始化输出对象
        
        optkcp_dime.addAll(lstkcp_dime.getRecords());
        output.setPinfos(optkcp_dime);
	 	
	    CommTools.getBaseRunEnvs().setTotal_count(lstkcp_dime.getRecordCount());// 记录总数
	}
}
