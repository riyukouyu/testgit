
package cn.sunline.edsp.busi.dptran.batchfile.ca;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.file.ReadFileProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.Knb_file_infoDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_file_info;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

/**
 * 分润记账读文件
 */
public class profitReadReadFileProcessor extends
		ReadFileProcessor<cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Input, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Property, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.fractionalBookkeepRead.Header, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_profit, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.fractionalBookkeepRead.Foot> {
	// 初始化日志信息
	private static final BizLog bizLog = BizLogUtil.getBizLog(profitReadReadFileProcessor.class);
	// 文件信息
	private knb_file_info filetab = SysUtil.getInstance(knb_file_info.class);

	/**
	 * 获取待处理文件（通常为下载）
	 * 
	 * @return 文件路径
	 **/
	public String downloadFile(cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Input input, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Property property) {
		if (CommUtil.isNull(input.getFilesq())) {
			throw DpModuleError.DpstComm.E9999("文件批次号不能为空！");
		}
		filetab = Knb_file_infoDao.selectOne_odb1(input.getFilesq(), true);

		bizLog.debug("文件路径：" + filetab.getLocaph());
		bizLog.debug("文件名称：" + filetab.getDownna());
		bizLog.debug("批次号:" + input.getFilesq());
		String pathname = filetab.getLocaph().concat("/").concat(filetab.getDownna());

		return pathname;
	}

	/**
	 * 解析文件头后转换为对应的javabean对象后，提供处理。
	 * 
	 * @parm header 文件头对象
	 * @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	 */
	public boolean headerProcess(cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.fractionalBookkeepRead.Header head,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Input input, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Property property) {
		filetab.setTotanm(head.getTotlnm()); // 总笔数
		Knb_file_infoDao.updateOne_odb1(filetab);
		return false;
	}

	/**
	 * 解析文件体后转换为对应的javabean对象后，提供处理。
	 * 
	 * @parm body 文件体对象
	 * @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	 *         注：该方法将会分发到作业服务器进行并发执行，需要配置交易控制器中的执行模式为"3"
	 */
	public boolean bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_batch_profit mapping,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Input input, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Property property) {
		mapping.setFilesq(input.getFilesq());
		return true;
	}

	/**
	 * 解析文件尾后转换为对应的javabean对象后，提供处理。
	 * 
	 * @param foot
	 *            文件尾对象
	 * @param input
	 *            批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return 后续是否执行自动入库操作。该返回值仅在配置了mapping属性后才有效。
	 */
	public boolean footProcess(cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.fractionalBookkeepRead.Foot foot,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Input input, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Property property) {
		return false;
	}

	/**
	 * 读文件交易前处理
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Property property) {
	}

	/**
	 * 文件头解析异常处理(包括文件的下载、文件的打开、文件头的解析)
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Property property, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 文件体一个批次处理并入库后回调(调用时间与设置的事务提交间隔相关)
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param addSuccessCount
	 * @param addErrorCount
	 */
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Property property, String jobId, int addSuccessCount, int addErrorCount) {
	}

	/**
	 * 文件体单行记录解析异常处理器
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param line
	 * @param t
	 */
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Property property, String jobId, String line, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 文件体一个批次解析异常处理器
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param totalSuccessCount
	 * @param totalErrorCount
	 * @param t
	 */
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Property property, String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 读文件交易处理结束后回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Property property) {
		bizLog.debug("<<=============文件处理完成后处理==============>>");
		Long total = DpAcctDao.selectBatchProfitCount(input.getFilesq(), false).getCounts();

		bizLog.debug("文件记录数：" + filetab.getTotanm());
		bizLog.debug("数据记录数：" + total);
		if (CommUtil.compare(filetab.getTotanm(), total) != 0) {
			throw DpModuleError.DpstComm.E9999("文件记录数与总记录数不匹配");
		}
		filetab.setSuccnm(total);
		Knb_file_infoDao.updateOne_odb1(filetab);
	}

	/**
	 * 文件体一个批次解析结束后回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param totalSuccessCount
	 * @param totalErrorCount
	 */
	public void afterBodyResolveProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Property property, String jobId, int totalSuccessCount, int totalErrorCount) {
	}

	/**
	 * 读文件交易处理异常后回调(交易的所有异常都会进入到该方法，包括之前已处理过并继续抛出的异常)
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.ProfitRead.Property property, Throwable t) {
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
