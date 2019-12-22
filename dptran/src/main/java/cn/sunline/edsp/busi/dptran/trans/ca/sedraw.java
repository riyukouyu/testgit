
package cn.sunline.edsp.busi.dptran.trans.ca;

import cn.sunline.adp.cedar.base.engine.BatchConfigConstant;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.util.BatchUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.namedsql.ca.EdmAfterDayBatchDao;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccountContro;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccountControDao;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_GENSTA;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;

/**
 * T1代发结算扣款通知
 * 
 * @author sunline16
 *
 */
public class sedraw {
	// 初始化日志信息
	private static final BizLog bizLog = BizLogUtil.getBizLog(sedraw.class);

	public static void settleDrawAccts(final cn.sunline.edsp.busi.dptran.trans.ca.intf.Sedraw.Input input,
			final cn.sunline.edsp.busi.dptran.trans.ca.intf.Sedraw.Property property,
			final cn.sunline.edsp.busi.dptran.trans.ca.intf.Sedraw.Output output) {
		bizLog.method("----------settleDrawAccts begin----------");
		String sTaskid = input.getSetkid();
		// 若存在 结算单生成中/代发中 的数据抛出异常
		if (EdmAfterDayBatchDao.selSettleControlStatistics(true) > 0) {
			throw DpModuleError.DpstComm.E9990("系统正在生成结算单或代发中，请稍后！");
		}
		// 初始化
		EdmSettleAccountContro tblacctContr = EdmSettleAccountControDao.selectOne_odb03(sTaskid, false);

		if (E_GENSTA.DELETE == tblacctContr.getTranst()) {
			throw DpModuleError.DpstComm.E9990("此结算单已作废！");
		}
		if (E_GENSTA.FAILURE == tblacctContr.getTranst()) {
			throw DpModuleError.DpstComm.E9990("此批次已代发失败！");
		}
		if (E_GENSTA.GENERATION == tblacctContr.getTranst()) {
			throw DpModuleError.DpstComm.E9990("批次代发中，请稍等！");
		}
		if (E_GENSTA.COMPLETED == tblacctContr.getTranst()) {
			throw DpModuleError.DpstComm.E9990("此批次已代发成功，请勿重复代发！");
		}
		tblacctContr.setSutitm(CommTools.getBaseRunEnvs().getTrxn_teller()); // 代发柜员
		tblacctContr.setSuusid(CommTools.getBaseRunEnvs().getTimestamp()); // 代发时间戳
		// 更新数据
		EdmSettleAccountControDao.updateOne_odb01(tblacctContr);

		// 更新结算表核心交易状态 - 代发中
		EdmAfterDayBatchDao.updSettleAcctByTaskid(sTaskid);
		EdmAfterDayBatchDao.updSetActControlByTaskid(sTaskid, E_GENSTA.GENERATION);

		// 转异步 调用批量组
		bizLog.debug("----------settdw批量扣款调度开始----------");
		DataArea dataArea = DataArea.buildWithEmpty();
		dataArea.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE, CommTools.getBaseRunEnvs().getTrxn_date());
		dataArea.getInput().setString("taskid", sTaskid);// 批次号
		BatchUtil.submitAndRunBatchTranGroup(BatchUtil.getTaskId(), "ST1001", dataArea);
		bizLog.debug("----------settdw批量扣款调度结束----------");
		bizLog.method("----------settleDrawAccts end----------");
	}
}
