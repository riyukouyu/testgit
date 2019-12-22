
package cn.sunline.edsp.busi.dptran.batchfile.ca;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.busi.aplt.tools.ApKnpParameter;
import cn.sunline.ltts.busi.ca.namedsql.CaBatchTransDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.Knb_file_infoDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_file_info;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

/**
 * 写对账文件
 */
public class writeReconWriteFileProcessor extends
		SimpleWriteFileBatchDataProcessor<cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Input, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Property, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.writeRecon.Header, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.writeRecon.Body, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.writeRecon.Foot, cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqRgst> {
	// 初始化日志信息
	private static final BizLog bizLog = BizLogUtil.getBizLog(writeReconWriteFileProcessor.class);
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
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqRgst> getFileBodyDataWalker(cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Property property) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(format.parse(CommTools.getBaseRunEnvs().getComputer_date()));
		}
		catch (ParseException e) {
			bizLog.debug(e.getMessage());
		}

		c.add(Calendar.DAY_OF_MONTH, -1);
		Params parm = new Params();
		property.setLastdt(c.getTime().toString());
		parm.add("cktrdt", property.getLastdt());
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqRgst>(CaBatchTransDao.namedsql_selectAcsqRgstByCktrdt, parm);
	}

	/**
	 * 获取生成文件的文件名(含路径)
	 * 
	 * @param input
	 *            批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Input input, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Property property) {
		// 查询参数信息
		KnpParameter tblKnpParameter = ApKnpParameter.getKnpParameter("DP.FILEPATH", "RECONCIFILE");

		filetab.setBtchno(this.getTaskId());
		filetab.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());
		filetab.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		filetab.setDownph(tblKnpParameter.getParm_value1().concat(property.getLastdt()));
		filetab.setDownna(tblKnpParameter.getParm_value2());
		filetab.setBtfest(E_BTFEST.DOWNSUCC);

		Knb_file_infoDao.insert(filetab);

		String pathname = tblKnpParameter.getParm_value1().concat(tblKnpParameter.getParm_value2());

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
	public cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.writeRecon.Header getHeader(cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Property property) {
		return null;
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqRgst dateItem, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.writeRecon.Body body,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Input input, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Property property) {

	}

	/**
	 * 返回文件尾信息
	 * 
	 * @param input
	 *            批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.writeRecon.Foot getFoot(cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Property property) {
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
	public void uploadFile(cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Input input, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Property property) {
	}

	/**
	 * 写文件前处理回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Property property) {

	}

	/**
	 * 写文件头异常处理回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Property property, Throwable t) {
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Property property, cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqRgst dateItem, Throwable t) {
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Property property, Throwable t) {
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Property property, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Property property) {
	}

	/**
	 * 写文件交易异常处理回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Input input,
			cn.sunline.edsp.busi.dptran.batchfile.ca.intf.WriteRecon.Property property, Throwable t) {
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
