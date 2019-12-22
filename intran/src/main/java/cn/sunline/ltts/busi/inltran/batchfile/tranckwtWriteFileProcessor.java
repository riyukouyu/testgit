package cn.sunline.ltts.busi.inltran.batchfile;



import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

	 /**
	  * 支付2.0系统流水勾兑写文件
	  *
	  */

public class  tranckwtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Input,cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Property,cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.tranckwt.Header,cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.tranckwt.Body,cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.tranckwt.Foot,cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlIobl>{
	private static final BizLog bizlog = BizLogUtil.getBizLog(tranckwtWriteFileProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	
	String trandt = DateTools2.getDateInfo().getLastdt();
	
	
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Property property){
		bizlog.method(">>>>>>>>>>>>>>>>>>>>getFileName begin>>>>>>>>>>>>>>>>>>>");

		filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		
		String pathname = filetab.getLocaph() +  filetab.getUpfena();
		
		bizlog.parm("-------------pathname[%s]", pathname);
		bizlog.method(">>>>>>>>>>>>>>>>>>>getFileName end>>>>>>>>>>>>>>>>>>>>");
		
		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.tranckwt.Header getHeader(cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 基于游标的文件数据遍历器
	 * 返回文件体数据遍历器
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return 
	 * 注：写文件体支持并发查数据库和写文件，最后合并，所以如果需要有顺序的需自带排序功能
	 */
	public BatchDataWalker<cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlIobl> getFileBodyDataWalker(cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Property property){
//		String trandt = DateTools2.getDateInfo().getLastdt();
//		String nextdt = DateTimeUtil.dateAdd("dd", trandt, 1);
//		String lastdt = DateTimeUtil.dateAdd("dd", trandt, -1);
		String nextdt = DateTools2.getDateInfo().getSystdt();
		String lastdt = DateTools2.getDateInfo().getBflsdt();

		Params param = new Params();
		param.put("nextdt", nextdt);
		param.put("lastdt", lastdt);
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlIobl>(InDayEndSqlsDao.namedsql_selknlioblWriteData , param); 
	}

	/**
	 * 写文件体的每条记录前提供回调处理
	 * 
	 * @param index 序号，从1开始
	 * @param body 文件体对象
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * 
	 */
	public void bodyProcess(int index, cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlIobl dateItem , cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.tranckwt.Body body, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Property property) {
//		bizlog.method(">>>>>>>>>>>>Begin>>>>>>>>>>>>");
//		bizlog.parm("input [%s]", input);
//		bizlog.parm("property [%s]", property);	
//		bizlog.method("<<<<<<<<<<<<End<<<<<<<<<<<<");
	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.tranckwt.Foot getFoot(cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Property property) {
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 写文件体(单笔)异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param dataItem
	 * @param t
	 */
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Property property, cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlIobl dateItem,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 写文件体异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 写文件尾异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Property property){
		
		filetab.setBtfest(E_BTFEST.RTNS);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Tranckwt.Property property,
			Throwable t) {
		
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

