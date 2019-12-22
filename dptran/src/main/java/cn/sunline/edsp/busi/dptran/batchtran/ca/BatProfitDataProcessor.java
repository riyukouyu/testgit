
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
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.namedsql.ca.DpBatchAccountingDao;
import cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatProfit.Input;
import cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatProfit.Property;
import cn.sunline.edsp.busi.dptran.tools.DpConstantDefine;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.Knb_batch_profitDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_profit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

/**
 * 批量分润记账
 * 
 * @author
 * @Date
 */

public class BatProfitDataProcessor extends
		AbstractBatchDataProcessor<cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatProfit.Input, cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatProfit.Property, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_profit> {
	// 初始化日志
	private static final BizLog bizLog = BizLogUtil.getBizLog(BatProfitDataProcessor.class);

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
	public void process(String jobId, int index, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_profit dataItem,
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatProfit.Input input, cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatProfit.Property property) {
		bizLog.method(">>>>>>>>>>批次分润记账开始>>>>>>>>>>");
		String mntrsq = CommTools.createNewTrxnSeq();
		String trandt = CommTools.getBaseRunEnvs().getLast_date();

		CommTools.getBaseRunEnvs().setTrxn_seq(mntrsq);
		BigDecimal prftam = dataItem.getTranam();

		// 金额为零则直接返回
		if (CommUtil.compare(BigDecimal.ZERO, prftam) == 0) {
			return;
		}

		property.setTrandt(dataItem.getTrandt());
		property.setTransq(dataItem.getTransq());
		KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(dataItem.getInmeid(), false);

		if (CommUtil.isNull(tblKnaSbad)) {
			throw DpModuleError.DpAcct.AT020054(dataItem.getInmeid());
		}
		// 配置不同渠道 ：点刷和新即付宝 具体值需要在数据库配置
		KnpParameter sbrandPara = KnpParameterDao.selectOne_odb1("DpParam.EndDay", "share", "profit", tblKnaSbad.getSbrand(), false);

		KnaAcct knaAcct = KnaAcctDao.selectOne_odb1(tblKnaSbad.getAcctno(), false);
		if (CommUtil.isNull(knaAcct)) {
			throw DpModuleError.DpAcct.AT020055(tblKnaSbad.getAcctno());
		}

		IoAccounttingIntf intf = SysUtil.getInstance(IoAccounttingIntf.class);
		intf.setTranms(DpConstantDefine.TRAN_FRACT_PROVISION_INFO); // 交易信息
		intf.setTrsqtp(E_ATSQTP.ACCOUNT); // 会计流水类型
		intf.setAtowtp(E_ATOWTP.IN); // 会计主体类型
		intf.setTrandt(trandt); // 交易日期
		intf.setCrcycd(knaAcct.getCrcycd()); // 币种
		intf.setCuacno(sbrandPara.getParm_value2()); // 记账账号
		intf.setDtitcd(sbrandPara.getParm_value2()); // 核算口径
		intf.setAcctno(sbrandPara.getParm_value2()); // 负债账号
		intf.setProdcd(sbrandPara.getParm_value2()); // 产品编号
		intf.setAcctdt(trandt); // 应入账日期
		intf.setAcctbr(knaAcct.getBrchno()); // 账务机构
		intf.setBltype(E_BLTYPE.BALANCE); // 余额属性
		intf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
		intf.setToacct(knaAcct.getAcctno()); // 对方账号
		intf.setToacna(knaAcct.getAcctna()); // 对方户名
		if (CommUtil.equals(DpConstantDefine.TRAN_FRACT_SIGN_PROVISION, sbrandPara.getParm_value1())) {
			intf.setAmntcd(E_AMNTCD.CR); // 借贷标志
			intf.setTranam(prftam.negate()); // 交易金额
		}
		else if (CommUtil.equals(DpConstantDefine.TRAN_FRACT_SIGN_REVERSE, sbrandPara.getParm_value1())) {
			intf.setAmntcd(E_AMNTCD.DR); // 借贷标志
			intf.setTranam(prftam); // 交易金额
		}
		CommTools.getBaseRunEnvs().setChannel_id("NM");
		SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsq(intf, E_BLNCDN.C);

		// 存入
		if (CommUtil.compare(BigDecimal.ZERO, prftam) < 0) {
			SaveDpAcctIn cplSaveAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
			cplSaveAcctIn.setAcctno(knaAcct.getAcctno());
			cplSaveAcctIn.setCrcycd(knaAcct.getCrcycd());
			cplSaveAcctIn.setCustac(knaAcct.getCustac());
			cplSaveAcctIn.setIntrcd(CommTools.getBaseRunEnvs().getTrxn_code());
			cplSaveAcctIn.setNegafg(E_YES___.NO);
			cplSaveAcctIn.setOpacna(sbrandPara.getParm_value3());
			cplSaveAcctIn.setRemark(DpConstantDefine.TRAN_FRACT_PROVISION_REMARK); // 备注
			cplSaveAcctIn.setSmrycd(DpConstantDefine.TRAN_FRACT_PROVISION_SMRYCD); // 摘要码
			cplSaveAcctIn.setSmryds(ApSmryTools.getText(DpConstantDefine.TRAN_FRACT_PROVISION_SMRYCD)); // 摘要描述
			cplSaveAcctIn.setStrktg(E_YES___.YES);
			cplSaveAcctIn.setToacct(sbrandPara.getParm_value2());
			cplSaveAcctIn.setTranam(prftam);
			cplSaveAcctIn.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));
			cplSaveAcctIn.setStrktg(E_YES___.NO); // 不注册冲正
			SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn);

		}
		// 冲正
		else if (CommUtil.compare(BigDecimal.ZERO, prftam) > 0) {
			DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
			cplDrawAcctIn.setAcctno(knaAcct.getAcctno());
			cplDrawAcctIn.setCrcycd(knaAcct.getCrcycd());
			cplDrawAcctIn.setCustac(knaAcct.getCustac());
			cplDrawAcctIn.setIntrcd(CommTools.getBaseRunEnvs().getTrxn_code());
			cplDrawAcctIn.setOpacna(sbrandPara.getParm_value3());
			cplDrawAcctIn.setRemark(DpConstantDefine.TRAN_FRACT_REVERSE_REMARK); // 备注
			cplDrawAcctIn.setSmrycd(DpConstantDefine.TRAN_FRACT_REVERSE_SMRYCD); // 摘要吗
			cplDrawAcctIn.setSmryds(ApSmryTools.getText(DpConstantDefine.TRAN_FRACT_REVERSE_SMRYCD)); // 摘要描述
			cplDrawAcctIn.setStrktg(E_YES___.YES);
			cplDrawAcctIn.setToacct(sbrandPara.getParm_value2());
			cplDrawAcctIn.setTranam(prftam.negate());
			cplDrawAcctIn.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));
			cplDrawAcctIn.setStrktg(E_YES___.NO); // 不注册冲正
			SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
		}
		// 平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(trandt, mntrsq, null);

		// 更新批量分润登记簿
		dataItem.setTranst(E_TRANST.SUCCESS); // 9-成功
		dataItem.setTaskid(this.getTaskId()); // 批次号
		dataItem.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq()); // 交易流水
		dataItem.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); // 交易日期
		// 更新数据
		Knb_batch_profitDao.updateOne_knb_batch_profit_odb1(dataItem);
		bizLog.method(">>>>>>>>>>批次分润记账结束>>>>>>>>>>");
	}

	@Override
	public void tranExceptionProcess(String taskId, Input input, Property property, Throwable t) {
		bizLog.method(" BatchFractionalBookkeepingDataProcessor.tranExceptionProcess begin >>>>>>>>>>>>>>>>");
		knb_batch_profit tblBatch_profit = Knb_batch_profitDao.selectOne_knb_batch_profit_odb1(property.getTransq(), property.getTrandt(), false);
		tblBatch_profit.setTranst(E_TRANST.FAIL); // 8-失败
		tblBatch_profit.setErrsck(t.getMessage()); // 错误栈
		// 更新数据
		Knb_batch_profitDao.updateOne_knb_batch_profit_odb1(tblBatch_profit);
		bizLog.method(" BatchFractionalBookkeepingDataProcessor.tranExceptionProcess end <<<<<<<<<<<<<<<<");
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
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_profit> getBatchDataWalker(
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatProfit.Input input, cn.sunline.edsp.busi.dptran.batchtran.ca.intf.BatProfit.Property property) {
		Params params = new Params();

		params.add("filesq", input.getFilesq()); // 文件批次号
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_profit>(DpBatchAccountingDao.namedsql_selectBatchProfitByFilesq, params);
	}
}
