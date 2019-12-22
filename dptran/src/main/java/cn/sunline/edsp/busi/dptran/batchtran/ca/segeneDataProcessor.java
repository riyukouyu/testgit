
package cn.sunline.edsp.busi.dptran.batchtran.ca;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.util.sequ.MsSeqUtil;
import cn.sunline.edsp.busi.dp.namedsql.ca.EdmAfterDayBatchDao;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccountContro;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccountControDao;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUPSST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_GENSTA;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SETTST;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoSettleSumInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;

/**
 * T1批量生成结算单
 * 
 * @author
 * @Date
 */

public class segeneDataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Segene.Input, cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Segene.Property> {
	// 初始化日志信息
	private final static BizLog bizLog = BizLogUtil.getBizLog(BizLog.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Segene.Input input,
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Segene.Property property) {
		bizLog.method("----------T1批量生成结算单处理开始----------");
		long lPrepsq = MsSeqUtil.genSeqId("SettleAccount");
		String sTrandt = CommTools.getBaseRunEnvs().getComputer_date();
		EdmSettleAccountContro edmsettleaccountcontro = EdmSettleAccountControDao.selectOne_odb01(input.getPltrsq(),
				input.getPltrdt(), true);
		if (E_GENSTA.SETTLEMENTBEGIN != edmsettleaccountcontro.getTranst()) {
			throw DpModuleError.DpstComm.E9990("结算单状态异常");

		}
		try {
			/** 更新未代发的数据作废 */
			// 更新区间内未代发的防重表作废
			EdmAfterDayBatchDao.updSetActContorlInva(input.getStardt(), input.getEndtdt());
			// 更新区间内未代发的关联表作废
			EdmAfterDayBatchDao.updRcSetInva(input.getStardt(), input.getEndtdt());
			// 更新区间内未代发的结算单作废
			EdmAfterDayBatchDao.updSetActInva(input.getStardt(), input.getEndtdt());

			/** 新增结算单数据 */
			// 新增结算单表
			EdmAfterDayBatchDao.insSettleAccounts(input.getStardt(), input.getEndtdt(), Long.toString(lPrepsq),
					this.getTaskId(), sTrandt, E_SETTST.CLWC, E_CUPSST.DCL, CommTools.getBaseRunEnvs().getTimestamp());
			// 新增关联表
			EdmAfterDayBatchDao.insRreceiptSettle(Long.toString(lPrepsq), E_SETTST.CLWC,
					CommTools.getBaseRunEnvs().getTimestamp(), input.getStardt(), input.getEndtdt(), sTrandt);

			IoSettleSumInfo setSum = EdmAfterDayBatchDao.selSetActASumInfo(this.getTaskId(), false);
			edmsettleaccountcontro.setTranst(E_GENSTA.SETTLEMENT); // 处理状态 3-结算单已生成
			edmsettleaccountcontro.setSetkid(this.getTaskId()); // 批次号
			edmsettleaccountcontro.setSetrdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 结算单生成日期
			edmsettleaccountcontro.setTotanm(setSum.getSuccnm()); // 总笔数
			edmsettleaccountcontro.setTotlam(setSum.getSuccam()); // 总金额
			EdmSettleAccountControDao.updateOne_odb01(edmsettleaccountcontro);
		} catch (Exception e) {
			bizLog.debug("----------结算单生成异常----------");
			edmsettleaccountcontro.setTranst(E_GENSTA.STATEMENTFAILED); // 处理状态 2-结算单生成失败
			edmsettleaccountcontro.setSetkid(this.getTaskId()); // 批次号
			edmsettleaccountcontro.setEstack(e.getMessage()); // 错误堆栈
			EdmSettleAccountControDao.updateOne_odb01(edmsettleaccountcontro);
		}

		bizLog.method("----------T1批量生成结算单处理结束----------");
	}
}
