package cn.sunline.ltts.busi.catran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.ca.namedsql.CaBatchTransDao;

	 /**
	  * 核查结果处理失败补偿处理
	  *
	  */

public class ckcoveDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.catran.batchtran.intf.Ckcove.Input, cn.sunline.ltts.busi.catran.batchtran.intf.Ckcove.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.catran.batchtran.intf.Ckcove.Input input, cn.sunline.ltts.busi.catran.batchtran.intf.Ckcove.Property property) {
		// 更新当前接收消息列表中状态为处理失败的，处理次数为一次的，MQ队列名为AS_NAS_ACC_CKRTAC的记录为待处理，再处理一次
		CaBatchTransDao.updCusAcmsMesgst();
	}

}


