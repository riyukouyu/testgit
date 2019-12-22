
package cn.sunline.edsp.busi.dptran.batchfile;

import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.file.ReadFileProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccount;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccountDao;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.KnbReceiptSettleDao;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.knbReceiptSettle;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUPSST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_PASTAT;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdm;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmContro;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmControDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

/**
 * T1批量代发扣款反盘读文件
 */
public class batpayrdReadFileProcessor extends
		ReadFileProcessor<cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Input, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Property, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.batpayrd.Header, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.batpayrd.Body, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.batpayrd.Foot> {
	// 初始化日志信息
	private static final BizLog bizLog = BizLogUtil.getBizLog(batpayrdReadFileProcessor.class);
	// 文件信息
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);

	/**
	 * 获取待处理文件（通常为下载）
	 * 
	 * @return 文件路径
	 **/
	public String downloadFile(cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Input input, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Property property) {
		if (CommUtil.isNull(input.getFilesq())) {
			throw DpModuleError.DpstComm.E9999("文件批次号不能为空！");
		}
		filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);

		bizLog.debug("文件路径：" + filetab.getDownph());
		bizLog.debug("文件名称：" + filetab.getDownna());
		bizLog.debug("批次号:" + input.getFilesq());
		String pathname = filetab.getDownph() + filetab.getDownna();

		return pathname;
	}

	/**
	 * 解析文件头后转换为对应的javabean对象后，提供处理。
	 * 
	 * @parm header 文件头对象
	 * @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	 */
	public boolean headerProcess(cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.batpayrd.Header head, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Property property) {
		return false;
	}

	/**
	 * 解析文件体后转换为对应的javabean对象后，提供处理。
	 * 
	 * @parm body 文件体对象
	 * @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	 *         注：该方法将会分发到作业服务器进行并发执行，需要配置交易控制器中的执行模式为"3"
	 */
	public boolean bodyProcess(int index, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.batpayrd.Body body, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Property property) {
		if ("00".equals(body.getRtcode())) {
			EdmSettleAccount tblSetAcct = EdmSettleAccountDao.selectOne_odb03(body.getTrandt(), body.getTransq(), true);
			tblSetAcct.setRetmsg(body.getRtdesc()); // 应答描述
			tblSetAcct.setRtcode(body.getRtcode()); // 应答码
			tblSetAcct.setTranst(E_CUPSST.SUCC); // 成功
			EdmSettleAccountDao.updateOne_odb01(tblSetAcct);

			List<knbReceiptSettle> lstReSet = KnbReceiptSettleDao.selectAll_knb_receipt_settle_odx2(tblSetAcct.getMntrsq(), true);
			for (knbReceiptSettle tblReSet : lstReSet) {
				
			
			KnlIoblEdmContro tblEdmControl = KnlIoblEdmControDao.selectOne_edmOdb01(tblReSet.getOrigsq(), tblReSet.getOrigdt(), true);
			tblEdmControl.setEdmflg(E_PASTAT.SUCC);
			
			//add by zhd 2019/11/12 修改代发防重表的代发标志为 S 代发成功
			KnlIoblEdmControDao.updateOne_edmOdb01(tblEdmControl);
			
			//add by hhh 2019/11/27 修改代发登记簿状态为成功
			KnlIoblEdm KnlIoblEdm = KnlIoblEdmDao.selectOne_odb01(tblEdmControl.getMntrsq(), tblEdmControl.getTrandt(), true);
			KnlIoblEdm.setServtp(body.getServna());
			KnlIoblEdm.setRetmsg(body.getRtdesc());
			KnlIoblEdm.setRtcode(body.getRtcode());
			KnlIoblEdm.setTranst(E_CUPSST.CLWC);
			KnlIoblEdmDao.updateOne_odb01(KnlIoblEdm);
			}
		}
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
	public boolean footProcess(cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.batpayrd.Foot foot, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Property property) {
		return false;
	}

	/**
	 * 读文件交易前处理
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Property property) {
	}

	/**
	 * 文件头解析异常处理(包括文件的下载、文件的打开、文件头的解析)
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Property property, Throwable t) {
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
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Property property, String jobId, int addSuccessCount, int addErrorCount) {
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Property property, String jobId, String line, Throwable t) {
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Property property, String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 读文件交易处理结束后回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Property property) {
		filetab.setBtfest(E_BTFEST.SUCC);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
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
	public void afterBodyResolveProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Property property, String jobId, int totalSuccessCount, int totalErrorCount) {
	}

	/**
	 * 读文件交易处理异常后回调(交易的所有异常都会进入到该方法，包括之前已处理过并继续抛出的异常)
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.intf.Batpayrd.Property property, Throwable t) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<kapb_wjplxxb>() {
			@Override
			public kapb_wjplxxb execute() {
				filetab.setBtfest(E_BTFEST.FAIL);
				Kapb_wjplxxbDao.updateOne_odb1(filetab);
				return null;
			}
		});

		throw ExceptionUtil.wrapThrow(t);
	}

}
