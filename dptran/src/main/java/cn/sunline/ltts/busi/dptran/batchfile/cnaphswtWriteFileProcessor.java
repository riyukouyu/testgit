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
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.cnaphswt.Header;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

	 /**
	  * 小额对账文件下载
	  *
	  */

public class  cnaphswtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Input,cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Property,cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.cnaphswt.Header,cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.cnaphswt.Body,cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.cnaphswt.Foot,cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapot>{
	
		// 日志实例化
		private static BizLog bizlog = BizLogUtil.getBizLog(cnaphswtWriteFileProcessor.class);
		private kapb_wjplxxb tblkapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Property property){
		
		tblkapbWjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		String pathname = tblkapbWjplxxb.getLocaph() + tblkapbWjplxxb.getUpfena();
		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.cnaphswt.Header getHeader(cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Property property){
		long succ = 0l;
		long fail = 0l;
		succ = DpDayEndDao.selKnlCnapotDataCount(input.getTrandt(), false);
				
		tblkapbWjplxxb.setSuccnm(succ);
		tblkapbWjplxxb.setFailnm(fail);
		
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		
		Header header = SysUtil.getInstance(Header.class);
		//header.setZongbish(succ);
		header.setTotanm(succ);
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
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapot> getFileBodyDataWalker(cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Property property){
		Params param = new Params();
		param.put("trandt", input.getTrandt());
		
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapot>(
				DpDayEndDao.namedsql_selKnlCnapotData, param);
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapot dateItem , cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.cnaphswt.Body body, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Property property) {

	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.cnaphswt.Foot getFoot(cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Property property){
		
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Property property){

	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Property property) {
		bizlog.method(">>>>>>>>>>>>beforeWriteFileTranProcess Begin>>>>>>>>>>>>");
		
/*		String upname = "AGRMENT_"+trandt+".TXT";
		//String downname = "CPGMQR_"+trandt+"_XX";
		
		tblkapbWjplxxb.setBtfest(E_BTFEST.DOWNING);
		tblkapbWjplxxb.setUpfeph(property.getDwpath());	
		tblkapbWjplxxb.setUpfena(upname);
		tblkapbWjplxxb.setFiletp(E_FILETP.DP020700);
		tblkapbWjplxxb.setDownph(null);
		tblkapbWjplxxb.setDownna(null);
		tblkapbWjplxxb.setBtchno(input.getFilesq());
		tblkapbWjplxxb.setBtfest(E_BTFEST.GIVSING);
		Kapb_wjplxxbDao.insert(tblkapbWjplxxb);*/
		
		bizlog.method("<<<<<<<<<<<<beforeWriteFileTranProcess End<<<<<<<<<<<<");
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Property property, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapot dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Property property){
		tblkapbWjplxxb.setBtfest(E_BTFEST.RTNS);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cnaphswt.Property property,
			Throwable t) {
		
		DaoUtil.executeInNewTransation(new RunnableWithReturn<kapb_wjplxxb>() {
			@Override
			public kapb_wjplxxb execute() {
				tblkapbWjplxxb.setBtfest(E_BTFEST.FAIL);
				Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
				return null;
			}
		});

		throw ExceptionUtil.wrapThrow(t);
	}

}

