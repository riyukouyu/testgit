package cn.sunline.ltts.busi.dptran.batchtran;

import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.ListBatchDataWalker;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;


	 /**
	  * 并发测试
	  *
	  */

public class tredtsDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.intf.Tredts.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Tredts.Property, String> {
	
	private static final BizLog bizlog = BizLogUtil
			.getBizLog(tredtsDataProcessor.class);
	
	  /**
		 * 批次数据项处理逻辑。
		 * 
		 * @param job 批次作业ID
		 * @param index  批次作业第几笔数据(从1开始)
		 * @param dataItem 批次数据项
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 */
		@Override
		public void process(String jobId, int index, String dataItem, cn.sunline.ltts.busi.dptran.batchtran.intf.Tredts.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Tredts.Property property) {
			//TODO:
			bizlog.debug("线程测试-线程ID["+Thread.currentThread().getId()+"]["+jobId+index+dataItem+"]");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				bizlog.debug(e.getMessage());
				Thread.currentThread().interrupt();
			}
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<String> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.intf.Tredts.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Tredts.Property property) {
			//TODO:	
			List<KnaAcct> acctdata =DaoUtil.selectAll(KnaAcct.class);// CommonDaoFactory.getInstance().getEntityDao(KnaAcct.class).selectAll();
			List<String> data  = new ArrayList<String>(); 
			for(KnaAcct acct : acctdata){
				data.add(acct.getAcctno());
			}
			return new ListBatchDataWalker<String>(data);
		}

}


