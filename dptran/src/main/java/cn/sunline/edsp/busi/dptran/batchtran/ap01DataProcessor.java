
package cn.sunline.edsp.busi.dptran.batchtran;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
	 /**
	  * 系统换日操作
	  * @author 
	  * @Date 
	  */

public class ap01DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.edsp.busi.dptran.batchtran.intf.Ap01.Input, cn.sunline.edsp.busi.dptran.batchtran.intf.Ap01.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.edsp.busi.dptran.batchtran.intf.Ap01.Input input, cn.sunline.edsp.busi.dptran.batchtran.intf.Ap01.Property property) {
		 //AppSydt tblKapp_sysdat = 
		 DateTools2.chgSystemDate();

			// 登记日终控制点 日切结束
		//ApDayendPoint.register(tblKapp_sysdat.getSystdt(), E_DYEDCT.RIQJS);
	}

}


