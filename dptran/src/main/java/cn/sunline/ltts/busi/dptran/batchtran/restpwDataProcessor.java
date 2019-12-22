package cn.sunline.ltts.busi.dptran.batchtran;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.TabDpPassword.DpbPswd;
import cn.sunline.ltts.busi.dp.tables.TabDpPassword.DpbPswdDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PSDWST;
import cn.sunline.edsp.base.lang.Params;
	 /**
	  * 批量密码重置
	  *
	  */

public class restpwDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.intf.Restpw.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Restpw.Property, cn.sunline.ltts.busi.dp.tables.TabDpPassword.DpbPswd> {
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.dp.tables.TabDpPassword.DpbPswd dataItem, cn.sunline.ltts.busi.dptran.batchtran.intf.Restpw.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Restpw.Property property) {
			
			dataItem.setPwerct(0);
			dataItem.setPsdwst(E_PSDWST.NORMAL);
			DpbPswdDao.updateOne_odb1(dataItem);
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.TabDpPassword.DpbPswd> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.intf.Restpw.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Restpw.Property property) {

			Params param = new Params();
			param.put("pwerct", "0");
			param.put("psdwst", E_PSDWST.LOSS);
			param.put("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
			
			return new CursorBatchDataWalker<DpbPswd>(DpDayEndDao.namedsql_selAllDpbPwdErrors, param);
		}

}


