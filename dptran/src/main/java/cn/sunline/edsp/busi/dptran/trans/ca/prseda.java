
package cn.sunline.edsp.busi.dptran.trans.ca;

import java.math.BigDecimal;

import org.exolab.castor.xml.handlers.ValueOfFieldHandler;

import cn.sunline.adp.cedar.base.engine.BatchConfigConstant;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.util.BatchUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.namedsql.ca.EdmAfterDayBatchDao;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccountContro;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccountControDao;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_GENSTA;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;

/**
 * 
 * @author sunline16 结算单生成 - 支持重复生成
 */
public class prseda {
	// 初始化日志信息
	private static final BizLog bizLog = BizLogUtil.getBizLog(prseda.class);

	public static void productSetDatas(final cn.sunline.edsp.busi.dptran.trans.ca.intf.Prseda.Input input,
			final cn.sunline.edsp.busi.dptran.trans.ca.intf.Prseda.Property property,
			final cn.sunline.edsp.busi.dptran.trans.ca.intf.Prseda.Output output) {
		bizLog.method("----------productSetDatas begin mntrsq[%s]----------", CommTools.getBaseRunEnvs().getTrxn_seq());
		/** 基本逻辑校验 */
		// 日期校验
		if (CommUtil.compare(input.getStardt(), input.getEndtdt()) > 0) {
			throw DpModuleError.DpstComm.E9990("结束日期不能小于开始日期！");
		}
		// 若存在 结算单生成中/代发中 的数据抛出异常
		if (EdmAfterDayBatchDao.selSettleControlStatistics(true) > 0) {
			throw DpModuleError.DpstComm.E9990("系统正在生成结算单或代发中，请稍后！");
		}
		// 判断是否存在结算数据
		if (EdmAfterDayBatchDao.selIoblCupsCout(input.getStardt(), input.getEndtdt(), true) <= 0) {
			throw DpModuleError.DpstComm.E9990("没有需要结算的数据！");
		}

		// 交易流水
		String sMntrsq = CommTools.getBaseRunEnvs().getTrxn_seq();
		// 交易日期
		String sTrandt = CommTools.getBaseRunEnvs().getTrxn_date();

		/** 登记结算防重表 */
		// 初始化
		EdmSettleAccountContro edmsettleaccount = SysUtil.getInstance(EdmSettleAccountContro.class);
		edmsettleaccount.setMntrsq(sMntrsq); // 交易流水
		edmsettleaccount.setTrandt(sTrandt); // 交易日期
		edmsettleaccount.setTotanm(0L); // 总笔数-默认值
		edmsettleaccount.setTotlam(BigDecimal.valueOf(0.00)); // 总金额-默认值
		edmsettleaccount.setSuccnm(0L); // 成功笔数-默认值
		edmsettleaccount.setSuccam(BigDecimal.valueOf(0.00)); // 成功金额-默认值
		edmsettleaccount.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch()); // 机构
		edmsettleaccount.setTranst(E_GENSTA.SETTLEMENTBEGIN); // 结算单状态 1-结算单生成中
		edmsettleaccount.setSeusid(CommTools.getBaseRunEnvs().getTrxn_teller()); // 柜员号
		edmsettleaccount.setSetitm(CommTools.getBaseRunEnvs().getTimestamp()); // 时间戳
		EdmSettleAccountControDao.insert(edmsettleaccount);

		/** 调用批量生成结算单 */
		DataArea dataArea = DataArea.buildWithEmpty();
		dataArea.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE, CommTools.getBaseRunEnvs().getTrxn_date());
		dataArea.getInput().setString("stardt", input.getStardt());// 开始日期
		dataArea.getInput().setString("endtdt", input.getEndtdt());// 结束日期
		dataArea.getInput().setString("pltrsq", sMntrsq); // 交易流水
		dataArea.getInput().setString("pltrdt", sTrandt); // 交易日期
		BatchUtil.submitAndRunBatchTran(BatchUtil.getTaskId(), "ST1002", "segene", dataArea);

		bizLog.method("----------productSetDatas end----------");
	}
}
