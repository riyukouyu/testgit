package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.util.List;

import cn.sunline.ltts.busi.fe.namedsql.FeSceneDao;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoScenInfo;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrsccd {
	private static final BizLog bizlog = BizLogUtil.getBizLog(qrsccd.class);
	
/*
 * 查询场景代码信息
 */
public static void qrsccd( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Qrsccd.Input input,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Qrsccd.Output output){
	
	bizlog.method(">>>>> qrsccd begin>>>>>>>>>>>>>>>>>");
	
	List<IoScenInfo> lstIoScenInfo = FeSceneDao.selall_kcp_scev_detl_info(input.getScencd(), input.getScends(), false);
	Options<IoScenInfo> optIoScenInfo = new DefaultOptions<IoScenInfo>();// 初始化输出对象
	
	optIoScenInfo.addAll(lstIoScenInfo);
	
    output.setSeinfo(optIoScenInfo);
    
	}
}
