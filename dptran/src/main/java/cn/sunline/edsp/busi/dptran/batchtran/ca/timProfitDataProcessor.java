
package cn.sunline.edsp.busi.dptran.batchtran.ca;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.core.exception.AdpBusinessException;
import cn.sunline.clwj.msap.core.tools.DBTools;
import cn.sunline.edsp.busi.dptran.tools.DpConstantDefine;
import cn.sunline.edsp.busi.dptran.tools.SyncRemoteFileToLocalTools;
import cn.sunline.ltts.busi.aplt.tools.ApKnpParameter;

/**
 * 定时扫描批量分润文件
 * 
 * @author
 * @Date
 */

public class timProfitDataProcessor
		extends BatchDataProcessorWithoutDataItem<cn.sunline.edsp.busi.dptran.batchtran.ca.intf.TimProfit.Input, cn.sunline.edsp.busi.dptran.batchtran.ca.intf.TimProfit.Property> {
	// 初始化日志
	private static final BizLog bizLog = BizLogUtil.getBizLog(timProfitDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.edsp.busi.dptran.batchtran.ca.intf.TimProfit.Input input, cn.sunline.edsp.busi.dptran.batchtran.ca.intf.TimProfit.Property property) {
		try {
			String remoteDir = ApKnpParameter.getKnpParameter("DP.FILEPATH", "PROFITFILE").getParm_value1();
			bizLog.debug("----------------remoteDir[%s]----------------", remoteDir);
			String localDir = ApKnpParameter.getKnpParameter("DP.FILEPATH", "PROFITFILE").getParm_value2();
			bizLog.debug("----------------localPath[%s]----------------", localDir);
			String uploadDir = ApKnpParameter.getKnpParameter("DP.FILEPATH", "PROFITFILE").getParm_value3();
			bizLog.debug("----------------uploadDir[%s]----------------", localDir);
			SyncRemoteFileToLocalTools.syncRemoteFile2Local(remoteDir, localDir, uploadDir, DpConstantDefine.TRAN_TIMING_PROFIT_CODE, this.getTaskId());
		}
		catch (AdpBusinessException e) {
			bizLog.debug("LttsBusinessException e[%s]", e);
			DBTools.rollback();
		}
	}
}
