
package cn.sunline.edsp.busi.dptran.batchtran.ca;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessorWithJobDataItem;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.util.sequ.MsSeqUtil;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.busi.dp.namedsql.ca.EdmAfterDayBatchDao;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccount;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccountContro;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccountControDao;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccountDao;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUPSST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_FINSTY;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_GENSTA;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_PASTAT;
import cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.ApDataGroupNo;
import cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.SettleDrawAccounts;
import cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Settdw.Input;
import cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Settdw.Property;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.dp.base.DpTools;
import cn.sunline.ltts.busi.dp.serviceimpl.DpAcctSvcImpl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdm;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoSettleSumInfo;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * T1批量代发扣款并生成扣款文件
 * 
 * @author shangdw
 * @Date 20191012
 */

public class settdwDataProcessor extends
		AbstractBatchDataProcessorWithJobDataItem<cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Settdw.Input, cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Settdw.Property, cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.ApDataGroupNo, cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.SettleDrawAccounts> {
	// 初始化日志信息
	private static final BizLog bizLog = BizLogUtil.getBizLog(settdwDataProcessor.class);

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
	public void process(String jobId, int index,
			cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.SettleDrawAccounts dataItem,
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Settdw.Input input,
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Settdw.Property property) {
		bizLog.method(" settdwDataProcessor.process begin >>>>>>>>>>>>>>>>");

		String newTrxnSeq = CommTools.createNewTrxnSeq();
		// 重置流水
		CommTools.getBaseRunEnvs().setMain_trxn_seq(newTrxnSeq);
		
		String acctno = dataItem.getAcctno();
		String trandt = dataItem.getTrandt();
		String mntrsq = dataItem.getMntrsq();
		EdmSettleAccount edmSettleAccount = EdmSettleAccountDao.selectOne_odb01(mntrsq, trandt, false);
		if (CommUtil.isNull(edmSettleAccount)) {
			return;
		}
		// 结算金额
		BigDecimal toanam = edmSettleAccount.getToanam();
		if (toanam.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		property.setTranam(toanam);
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("EdmSettleAccount", "220210", "%", "%", true);
		String crcycd = BusiTools.getDefineCurrency();
		DpAcctSvcImpl dpAcctSvcImpl = SysUtil.getInstance(DpAcctSvcImpl.class);
		DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
		// 支取记账服务，借 电子账户-负债类-减少-借方
		cplDrawAcctIn.setAcctno(acctno);
		cplDrawAcctIn.setCrcycd(crcycd);
		cplDrawAcctIn.setTranam(toanam);
		cplDrawAcctIn.setCardno(edmSettleAccount.getCardno());
		cplDrawAcctIn.setCustac(edmSettleAccount.getCustac());
		cplDrawAcctIn.setAcseno(edmSettleAccount.getSubsac());
		cplDrawAcctIn.setToacct(knpParameter.getParm_value1());// 对方账号 - 预收账款科目
		cplDrawAcctIn.setOpacna("T1代发待清算");// 对方户名
		cplDrawAcctIn.setSmrycd("STDR");// 摘要代码 - T1批量代发
		cplDrawAcctIn.setRemark("T1批量代发扣款");
		cplDrawAcctIn.setIssucc(E_YES___.YES);
		cplDrawAcctIn.setDetlsq(MsSeqUtil.genSeqId("detlsq"));
		dpAcctSvcImpl.addDrawAcctDp(cplDrawAcctIn);

		// 内部户记账，产品编码:9922021000000001 科目:2202 子户号:0000001
		// 根据acctno获取机构号
		KnaAcct knaAcct = KnaAcctDao.selectOne_odb1(edmSettleAccount.getAcctno(), false);

		// 此处可以优化 - 可考虑汇总一笔记账
		IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
		iaAcdrInfo.setCrcycd(crcycd);
		iaAcdrInfo.setAcbrch(knaAcct.getBrchno());
		iaAcdrInfo.setBusino(knpParameter.getParm_value3());
		iaAcdrInfo.setItemcd(knpParameter.getParm_value1());
		iaAcdrInfo.setSubsac(knpParameter.getParm_value2());
		iaAcdrInfo.setTranam(toanam);
		iaAcdrInfo.setToacct(acctno);
		iaAcdrInfo.setToacna(edmSettleAccount.getInmena());
		IoInAccount ioInAccount = SysUtil.getInstance(IoInAccount.class);
		ioInAccount.ioInAccr(iaAcdrInfo);
		//更改代发流水生成规则
		String transq = DpTools.genSequenceWithTrandt("edtrsq", 10);
		edmSettleAccount.setSettdt(CommTools.getBaseRunEnvs().getTrxn_date());
		edmSettleAccount.setTranst(E_CUPSST.PRC);
		edmSettleAccount.setSettsq(transq);
		edmSettleAccount.setSettid(transq);
		edmSettleAccount.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq()); // 出账流水
		edmSettleAccount.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); // 出账日期
		
		EdmSettleAccountDao.updateOne_odb01(edmSettleAccount);
		KnaCacd knacacd=KnaCacdDao.selectFirst_odb3(edmSettleAccount.getAcctno(), E_DPACST.DEFAULT, false);
		// 登记代发登记簿
		KnlIoblEdm knlIoblEdm = SysUtil.getInstance(KnlIoblEdm.class);
		knlIoblEdm.setMntrsq(transq);
		knlIoblEdm.setTrandt(trandt);
		knlIoblEdm.setMntrtm(BusiTools.getTraxTmInRunEnv());
		knlIoblEdm.setTranam(edmSettleAccount.getToanam());
		knlIoblEdm.setTranst(E_CUPSST.PRC);
		knlIoblEdm.setAcctid(edmSettleAccount.getAcctid());
		knlIoblEdm.setFinsty(E_FINSTY.T1);
		knlIoblEdm.setSabkna(edmSettleAccount.getSabkna()); // 结算卡银行名称
		knlIoblEdm.setSacdno(edmSettleAccount.getSacdno()); // 结算卡账号
		knlIoblEdm.setBankno(edmSettleAccount.getBankno()); // 联行号
		knlIoblEdm.setTmsacdno(edmSettleAccount.getTmsacdno()); // 结算卡账号
		knlIoblEdm.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());
		knlIoblEdm.setCardtp(knacacd.getCardtp());//结算卡类型
		knlIoblEdm.setSacdna(knacacd.getAcctna());//结算卡账号名称
		knlIoblEdm.setTmsacdna(knacacd.getTmacctna());//结算卡账号名称
		knlIoblEdm.setPuacfg(edmSettleAccount.getPuacfg());//账户类型
		KnlIoblEdmDao.insert(knlIoblEdm);

		EdmSettleAccountContro tblacctContr = EdmSettleAccountControDao.selectOne_odb03(input.getTaskid(), false);
		tblacctContr.setSutkid(this.getTaskId()); // 代发批次号
		tblacctContr.setSutrdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 交易日期
		EdmSettleAccountControDao.updateOne_odb01(tblacctContr);
		//add by zhd 2019/11/12修改插入数据收单状态为1成功
		//add by hhh 2019/11/21设置recdver为固定值1
		long recdver=1;
		EdmAfterDayBatchDao.insIoblEdmControlByT1Data(mntrsq,transq, trandt, E_FINSTY.T1, E_CUPSST.SUCC, E_PASTAT.PROC,
				CommTools.getBaseRunEnvs().getTimestamp(),recdver);
		bizLog.method(" settdwDataProcessor.process end <<<<<<<<<<<<<<<<");
	}

	/**
	 * 获取数据遍历器。- 第一次任务拆分
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.ApDataGroupNo> getBatchDataWalker(
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Settdw.Input input,
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Settdw.Property property) {
		// 第一次拆分，根据stardt和endtdt查找结算单登记薄的散列值和交易日期
		Params parm = new Params();
		parm.add("taskid", input.getTaskid());
		return new CursorBatchDataWalker<ApDataGroupNo>(EdmAfterDayBatchDao.namedsql_selDataHashInfo, parm);
	}

	/**
	 * 获取作业数据遍历器 - 第二次任务拆分
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @param dataItem
	 *            批次数据项
	 * @return
	 */
	public BatchDataWalker<cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.SettleDrawAccounts> getJobBatchDataWalker(
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Settdw.Input input,
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Settdw.Property property,
			cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.ApDataGroupNo dataItem) {
		// 第二次拆分，根据散列值和交易日期查找 状态<>9的结算数据。
		Params parm = new Params();
		parm.add("taskid", input.getTaskid());
		parm.add("hashvl", dataItem.getHashvl());
		return new CursorBatchDataWalker<SettleDrawAccounts>(EdmAfterDayBatchDao.namedsql_selSettleDataInfo, parm);
	}

	@Override
	public void afterJobProcess(String taskId, Input input, Property property, String jobId, int totalSuccessCount,
			int totalErrorCount) {
		bizLog.method(" settdwDataProcessor.afterJobProcess begin >>>>>>>>>>>>>>>>");
		EdmSettleAccountContro tblAcctControl = EdmSettleAccountControDao.selectOne_odb03(input.getTaskid(), false);
		tblAcctControl.setSuccam(tblAcctControl.getSuccam().add(property.getTranam())); // 总金额
		tblAcctControl.setSuccnm(tblAcctControl.getSuccnm() + 1); // 总笔数
		EdmSettleAccountControDao.updateOne_odb01(tblAcctControl);
		bizLog.method(" settdwDataProcessor.afterJobProcess end <<<<<<<<<<<<<<<<");
	}

	/**
	 * 交易后处理
	 */
	@Override
	public void afterTranProcess(String taskId, Input input, Property property) {
		bizLog.method(" settdwDataProcessor.afterTranProcess begin >>>>>>>>>>>>>>>>");
		// 更新放重表为代发完成
		EdmAfterDayBatchDao.updSetActControlByTaskid(input.getTaskid(), E_GENSTA.COMPLETED);
		bizLog.method(" settdwDataProcessor.afterTranProcess end <<<<<<<<<<<<<<<<");
	}

	/**
	 * 交易失败处理
	 */
	@Override
	public void tranExceptionProcess(String taskId, Input input, Property property, Throwable t) {
		bizLog.method(" settdwDataProcessor.tranExceptionProcess begin >>>>>>>>>>>>>>>>");
		super.tranExceptionProcess(taskId, input, property, t);
		EdmAfterDayBatchDao.updSetActControlByTaskid(input.getTaskid(), E_GENSTA.FAILURE);
		EdmSettleAccountContro tblAcctControl = EdmSettleAccountControDao.selectOne_odb03(input.getTaskid(), false);
		tblAcctControl.setEstack(t.getMessage()); // 异常堆栈
		EdmSettleAccountControDao.updateOne_odb01(tblAcctControl);
		bizLog.method(" settdwDataProcessor.tranExceptionProcess end <<<<<<<<<<<<<<<<");
	}

	/**
	 * 作业失败处理
	 */
	@Override
	public void jobExceptionProcess(String taskId, Input input, Property property, String jobId,
			SettleDrawAccounts dataItem, Throwable t) {
		bizLog.method(" settdwDataProcessor.jobExceptionProcess begin >>>>>>>>>>>>>>>>");
		EdmSettleAccount edmSettleAccount = EdmSettleAccountDao.selectOne_odb01(dataItem.getMntrsq(),
				dataItem.getTrandt(), false);
		if (CommUtil.isNull(edmSettleAccount)) {
			return;
		}
		edmSettleAccount.setTranst(E_CUPSST.FAIL);
		EdmSettleAccountDao.updateOne_odb01(edmSettleAccount);
		bizLog.method(" settdwDataProcessor.jobExceptionProcess end <<<<<<<<<<<<<<<<");
	}
}
