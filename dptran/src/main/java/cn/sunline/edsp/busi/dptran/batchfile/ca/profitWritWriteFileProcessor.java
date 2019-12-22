
package cn.sunline.edsp.busi.dptran.batchfile.ca;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.profitWrit.Header;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.Knb_file_infoDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_file_info;
import cn.sunline.ltts.busi.dp.type.DpTransfer.DpCount;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

/**
 * 分润记账写文件
 */
public class profitWritWriteFileProcessor extends
		SimpleWriteFileBatchDataProcessor<cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Input, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Property, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.profitWrit.Header, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.profitWrit.Body, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.profitWrit.Foot, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_profit> {
	// 初始化日志信息
	private static final BizLog bizLog = BizLogUtil.getBizLog(profitWritWriteFileProcessor.class);
	// 文件信息
	private knb_file_info filetab = SysUtil.getInstance(knb_file_info.class);

	/**
	 * 基于游标的文件数据遍历器 返回文件体数据遍历器
	 * 
	 * @param input
	 *            批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return 注：写文件体支持并发查数据库和写文件，最后合并，所以如果需要有顺序的需自带排序功能
	 */
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_profit> getFileBodyDataWalker(
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Input input, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Property property) {
		Params parm = new Params();
		parm.add("filesq", input.getFilesq());
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_profit>(DpAcctDao.namedsql_selectBatchProfitByFilesq, parm);
	}

	/**
	 * 获取生成文件的文件名(含路径)
	 * 
	 * @param input
	 *            批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Input input, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Property property) {
		if (CommUtil.isNull(input.getFilesq())) {
			throw DpModuleError.DpstComm.E9999("文件批次号不能为空！");
		}
		filetab = Knb_file_infoDao.selectOne_odb1(input.getFilesq(), true);

		bizLog.debug("文件路径：" + filetab.getLocaph());
		bizLog.debug("文件名称：" + filetab.getDownna());
		bizLog.debug("批次号:" + input.getFilesq());
		String pathname = filetab.getUpfeph().concat("/").concat(filetab.getUpfena());

		return pathname;
	}

	/**
	 * 返回文件头信息
	 * 
	 * @param input
	 *            批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.profitWrit.Header getHeader(cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Property property) {
		Header header = SysUtil.getInstance(Header.class);
		// 查询汇总金额数量
		DpCount agentPaidCount = DpAcctDao.selectBatchProfitCount(input.getFilesq(), false);
		header.setTotlam(agentPaidCount.getTranam()); // 总金额
		header.setTotlnm(agentPaidCount.getCounts()); // 总数量
		return header;
	}

	/**
	 * 写文件体的每条记录前提供回调处理
	 * 
	 * @param index
	 *            序号，从1开始
	 * @param body
	 *            文件体对象
	 * @param input
	 *            批量交易的输入接口
	 * @param property批量交易的属性接口
	 */
	public void bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_profit dateItem,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.profitWrit.Body body, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Property property) {

	}

	/**
	 * 返回文件尾信息
	 * 
	 * @param input
	 *            批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.profitWrit.Foot getFoot(cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Property property) {
		return null;
	}

	/**
	 * 上传生成的批量文件
	 * 
	 * @param input
	 *            批量交易的输入接口
	 * @param property
	 *            批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Input input, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Property property) {
	}

	/**
	 * 写文件前处理回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Property property) {

	}

	/**
	 * 写文件头异常处理回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Property property, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件体(单笔)异常处理回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param dataItem
	 * @param t
	 */
	public void writeBodyExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Property property, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_profit dateItem,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件体异常处理回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeBodyExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Property property, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件尾异常处理回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFootExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Property property, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Property property) {
	}

	/**
	 * 写文件交易异常处理回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitWrit.Property property, Throwable t) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<knb_file_info>() {
			@Override
			public knb_file_info execute() {
				filetab.setBtfest(E_BTFEST.FAIL);
				Knb_file_infoDao.updateOne_odb1(filetab);
				return null;
			}
		});
		throw ExceptionUtil.wrapThrow(t);
	}
}
