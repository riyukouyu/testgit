package cn.sunline.ltts.busi.dptran.batchfile;


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
import cn.sunline.ltts.busi.dp.namedsql.KqBatchDao;
import cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.queryw.Header;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

	 /**
	  * 查询用户签约购买产品是否提前解约支取写文件
	  *
	  */

public class  querywWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Input,cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Property,cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.queryw.Header,
	cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.queryw.Body,
	cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.queryw.Foot,
	cn.sunline.ltts.busi.dp.tables.RpMgr.knbQeryBach>{
		
	private static BizLog log = BizLogUtil.getBizLog(querywWriteFileProcessor.class); 
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Property property){
		filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		
        log.debug("反盘文件路径：" + filetab.getUpfeph());
		
		String pathname = filetab.getUpfeph() + filetab.getUpfena();
		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.queryw.Header getHeader(cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Property property){
		
		Header header = SysUtil.getInstance(Header.class);
		
		header.setTotanm(filetab.getTotanm());
		
		return header;
	}
	
	/**
	 * 基于游标的文件数据遍历器
	 * 返回文件体数据遍历器
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return 
	 * 注：写文件体支持并发查数据库和写文件，最后合并，所以如果需要有顺序的需自带排序功能
	 */
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.knbQeryBach> getFileBodyDataWalker(cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Property property){
		Params param = new Params();
		 
		param.put("filesq", input.getFilesq());
		
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.knbQeryBach>(KqBatchDao.namedsql_selProdstWriteData , param); 
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.RpMgr.knbQeryBach dateItem , cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.queryw.Body body, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Property property) {

	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.queryw.Foot getFoot(cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Property property) {
		
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Property property, cn.sunline.ltts.busi.dp.tables.RpMgr.knbQeryBach dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Property property){
		
		filetab.setBtfest(E_BTFEST.WAIT_REPORT);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Queryw.Property property,
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

