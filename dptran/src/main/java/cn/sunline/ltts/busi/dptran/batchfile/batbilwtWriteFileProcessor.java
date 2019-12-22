package cn.sunline.ltts.busi.dptran.batchfile;

import java.math.BigDecimal;

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
import cn.sunline.ltts.busi.dp.namedsql.redpck.BilBatchDao;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

	 /**
	  * 信用卡批量还款写文件
	  *
	  */

public class  batbilwtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Input,cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Property,cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.batbilwt.Header,cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.batbilwt.Body,cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.batbilwt.Foot,cn.sunline.ltts.busi.dp.tables.RpMgr.KnbCrcdBach>{
	
	private static BizLog log = BizLogUtil.getBizLog(batbilwtWriteFileProcessor.class); 
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Property property){
	//	String filena = "NAS" + "_" + E_FILETP.DP020600 + "_" + CommTools.getBaseRunEnvs().getTrxn_date() + "_" + BusiTools.getSequence("fileseq", 5) + ".txt" ;
		
		filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
//		String filepath = filetab.getDownph();
		//String downna = filetab.getDownna();
		
		//文件名按读取文件格式命名
		//String filena = KnpParameterDao.selectOne_odb1("BATBILWT", "FILENA", "%", "%", true).getParm_value1() +  downna.substring(downna.length() -8);
		
		log.debug("反盘文件路径：" + filetab.getDownph());
		//filetab.setUpfeph(filepath);
		//filetab.setUpfena(filena);
		//Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
		String pathname = filetab.getLocaph() + filetab.getUpfena();
		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.batbilwt.Header getHeader(cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Property property){
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
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbCrcdBach> getFileBodyDataWalker(cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Property property){
		
		Params param = new Params();
		 
		param.put("filesq", input.getFilesq());
		param.put("trandt", filetab.getAcctdt());
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbCrcdBach>(
				BilBatchDao.namedsql_selBatchWriteData, param); 
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbCrcdBach dateItem , cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.batbilwt.Body body, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Property property) {

		//金额去小数点
		long trmlng = dateItem.getTranam().multiply(new BigDecimal(100)).longValue();
		body.setTranam(String.valueOf(trmlng));

		long remlng = dateItem.getRealam().multiply(new BigDecimal(100)).longValue();
		body.setRealam(String.valueOf(remlng));

	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.batbilwt.Foot getFoot(cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Property property) {
		
//		filetab.setBtfest(E_BTFEST.RESTING);
//		Kapb_wjplxxbDao.updateOne_odb1(filetab);
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Property property, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbCrcdBach dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Property property){
		filetab.setBtfest(E_BTFEST.SUCC);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilwt.Property property,
			Throwable t) {
		//String status=E_FILEST.FAIL.getValue();
	//	String descri=t.getMessage();
		//SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBack(input.getBusseq(),input.getFilesq(), input.getAcctdt(), filetab.getUpfena(), filetab.getUpfeph(), status, descri); 
		DaoUtil.executeInNewTransation(new RunnableWithReturn<kapb_wjplxxb>() {
			public kapb_wjplxxb execute() {
				
				
				filetab.setBtfest(E_BTFEST.FAIL);
				Kapb_wjplxxbDao.updateOne_odb1(filetab);
				return null;
			}
		});
		throw ExceptionUtil.wrapThrow(t);
	}

}

