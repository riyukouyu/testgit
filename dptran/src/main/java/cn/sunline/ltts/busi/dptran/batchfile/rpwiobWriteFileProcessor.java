package cn.sunline.ltts.busi.dptran.batchfile;

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
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.rpwiob.Header;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

	 /**
	  * 电子账户出入金信息写文件
	  *
	  */

public class  rpwiobWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Input,cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Property,cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.rpwiob.Header,cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.rpwiob.Body,cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.rpwiob.Foot,cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups>{
	
	
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	private BizLog bizlog = BizLogUtil.getBizLog(rpwiobWriteFileProcessor.class);
	
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Property property){
		if(CommUtil.isNull(input.getFilesq())){
			throw DpModuleError.DpstComm.E9999("文件批次号不能为空");
		}
		
		filetab =Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
//		String filename = input.getDwname();
//		String filepath = input.getDwpath();
//		String filesq = input.getFilesq();
		System.out.println("文件路径：" + filetab.getDownph());
		System.out.println("文件名称：" + filetab.getDownna());
		System.out.println("批次号:" + input.getFilesq());
		//String pathname = filepath.concat(File.separator).concat(filename);
		String pathname =filetab.getLocaph() + filetab.getUpfena();
		//将文件路径信息插入文件批量信息表
//		filetab.setBusseq(input.getBusseq());
//		filetab.setBtfest(E_BTFEST.DOWNSUCC);
//		filetab.setDownph(filepath);
//	    filetab.setFiletp(E_FILETP.DP021300);
//		filetab.setDownna(filename);
//		filetab.setUpfeph(filepath);
//		filetab.setBtchno(filesq);
//		filetab.setUpfena(filename);
//		Kapb_wjplxxbDao.insert(filetab);
		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.rpwiob.Header getHeader(cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Property property){
		
		long fail = 0L;
		long count = DpAcctDao.selCUPSCountByTrandt(input.getTrandt(), true);
		
		filetab.setSuccnm(count);
		filetab.setFailnm(fail);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
		Header header = SysUtil.getInstance(Header.class);
		header.setTaskid(input.getFilesq());
		header.setChenggbs(count);
		header.setChulizbs(count);
		header.setShibaibs(fail);
		header.setZongbish(count);

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
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups> getFileBodyDataWalker(cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Property property){
		//
		Params param = new Params();
		param.put("trandt", input.getTrandt());
		
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups>(DpAcctDao.namedsql_selCUPSCheckDetail , param); 
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups dateItem , cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.rpwiob.Body body, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Property property) {

	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.rpwiob.Foot getFoot(cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Property property) {
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
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Property property, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Property property){
		
		filetab.setBtfest(E_BTFEST.SUCC);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
//		File fp = new File(filetab.getDownph());
//		
//		if(!fp.exists()){
//			fp.mkdirs();
//		}
//		
//		String ok = filetab.getDownna().concat(".ok");
//		String pathname =filetab.getDownph().concat(ok);
//		
//		bizlog.debug("<<==========生成文件路径[%s]", pathname);
//		
//		File file = new File(pathname);
//		
//		if(!file.exists()){
//			try {
//				file.createNewFile();
//			} catch (Exception e) {
//				
//				throw DpModuleError.DpstComm.E9999("产生OK文件错误");
//			}
//		}
		
		/**
		String status=E_FILEST.SUCC.getValue();
		String descri="处理成功";
		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBack(input.getBusseq(),input.getFilesq(), input.getTrandt(), filetab.getUpfena(), filetab.getUpfeph(), status, descri);
		**/ 
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Rpwiob.Property property,
			Throwable t) {
		/**
		String status=E_FILEST.FAIL.getValue();
		String descri=t.getMessage();
		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBack(input.getBusseq(),input.getFilesq(), input.getTrandt(), filetab.getUpfena(), filetab.getUpfeph(), status, descri); 
		**/
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

