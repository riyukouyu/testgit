
package cn.sunline.edsp.busi.dptran.batchtran.ca;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.util.sequ.MsSeqUtil;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.namedsql.ca.DpBatchAccountingDao;
import cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatchPaid.Input;
import cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatchPaid.Property;
import cn.sunline.edsp.busi.dptran.tools.DpConstantDefine;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.dp.serviceimpl.DpAcctSvcImpl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.Knb_batch_paidDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_paid;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * 批量代发记账
 * 
 * @author
 * @Date
 */

public class BatchPaidDataProcessor extends
		AbstractBatchDataProcessor<cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatchPaid.Input, cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatchPaid.Property, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_paid> {
	// 初始化日志
	private static final BizLog bizLog = BizLogUtil.getBizLog(BatchPaidDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param job
	 *            批次作业ID
	 * @param index
	 *            批次作业第几笔数据(从1开始)
	 * @param dataItem
	 *            批次数据项
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(String jobId, int index, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_paid dataItem,
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatchPaid.Input input, cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatchPaid.Property property) {
		String newTrxnSeq = CommTools.createNewTrxnSeq();
		// 重置流水
		CommTools.getBaseRunEnvs().setMain_trxn_seq(newTrxnSeq);

		BigDecimal toanam = dataItem.getTranam();
		if (toanam.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(dataItem.getInmeid(), false);
		property.setTrandt(dataItem.getTrandt());
		property.setTransq(dataItem.getTransq());
		if (CommUtil.isNull(tblKnaSbad)) {
			throw DpModuleError.DpAcct.AT020054(dataItem.getInmeid());
		}

		// 查询账户信息
		KnaAcct knaAcct = KnaAcctDao.selectOne_odb1(tblKnaSbad.getAcctno(), false);
		if (CommUtil.isNull(knaAcct)) {
			throw DpModuleError.DpAcct.AT020055(tblKnaSbad.getAcctno());
		}
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("EdmSettleAccount", "220210", "%", "%", true);

		DpAcctSvcImpl dpAcctSvcImpl = SysUtil.getInstance(DpAcctSvcImpl.class);
		DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
		IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
		IoInAccount ioInAccount = SysUtil.getInstance(IoInAccount.class);

		// 内部户贷记
		iaAcdrInfo.setCrcycd(knaAcct.getCrcycd());
		iaAcdrInfo.setAcbrch(knaAcct.getBrchno());
		iaAcdrInfo.setTrantp(E_TRANTP.TR);//交易类型 
		iaAcdrInfo.setBusino(knpParameter.getParm_value3());
		iaAcdrInfo.setItemcd(knpParameter.getParm_value1());
		iaAcdrInfo.setSubsac(knpParameter.getParm_value2());
		iaAcdrInfo.setTranam(toanam);
		iaAcdrInfo.setToacct(knaAcct.getAcctno());
		iaAcdrInfo.setToacna(knaAcct.getAcctna());
		iaAcdrInfo.setStrktg(E_YES___.NO); // 不注册冲正
		IaTransOutPro iaTransOutPro = ioInAccount.ioInAccr(iaAcdrInfo);

		// 支取记账服务
		cplDrawAcctIn.setAcctno(tblKnaSbad.getAcctno());
		cplDrawAcctIn.setCrcycd(knaAcct.getCrcycd());
		cplDrawAcctIn.setTranam(toanam);
		cplDrawAcctIn.setCustac(knaAcct.getCustac());
		cplDrawAcctIn.setOpacna(iaTransOutPro.getAcctna());
		cplDrawAcctIn.setToacct(iaTransOutPro.getAcctno());
		cplDrawAcctIn.setSmrycd(DpConstantDefine.TRAN_PAID_SMRYCD);// 摘要代码 - T1批量代发
		cplDrawAcctIn.setSmryds(ApSmryTools.getText(DpConstantDefine.TRAN_PAID_SMRYCD));
		cplDrawAcctIn.setRemark(DpConstantDefine.TRAN_PAID_REMARK);
		cplDrawAcctIn.setIssucc(E_YES___.YES);
		cplDrawAcctIn.setDetlsq(MsSeqUtil.genSeqId("detlsq"));
		cplDrawAcctIn.setStrktg(E_YES___.NO); // 不注册冲正
		dpAcctSvcImpl.addDrawAcctDp(cplDrawAcctIn);

		// 查询代发登记簿
		dataItem.setTranst(E_TRANST.SUCCESS); // 9-成功
		dataItem.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq()); // 交易流水
		dataItem.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); // 交易日期
		dataItem.setTaskid(this.getTaskId()); // 批次号
		// 更新数据
		Knb_batch_paidDao.updateOne_knb_batch_paid_odb1(dataItem);
	}

	@Override
	public void tranExceptionProcess(String taskId, Input input, Property property, Throwable t) {
		bizLog.method(" BatchPaidDataProcessor.tranExceptionProcess begin >>>>>>>>>>>>>>>>");
		knb_batch_paid tblBatch_paid = Knb_batch_paidDao.selectOne_knb_batch_paid_odb1(property.getTransq(), property.getTrandt(), false);
		tblBatch_paid.setTranst(E_TRANST.FAIL); // 8-失败
		tblBatch_paid.setErrsck(t.getMessage()); // 错误栈
		// 更新数据
		Knb_batch_paidDao.updateOne_knb_batch_paid_odb1(tblBatch_paid);
		bizLog.method(" BatchPaidDataProcessor.tranExceptionProcess end <<<<<<<<<<<<<<<<");
	}

	/**
	 * 获取数据遍历器。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_paid> getBatchDataWalker(
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatchPaid.Input input, cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatchPaid.Property property) {
		Params params = new Params();

		params.add("filesq", input.getFilesq()); // 文件批次号
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_paid>(DpBatchAccountingDao.namedsql_selectBatchPaidByFilesq, params);
	}
}
