package cn.sunline.ltts.busi.dptran.batchfile.redpck;

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
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.dp.namedsql.redpck.RpBatchTransDao;
import cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.rpchckwt.Header;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
  /**
   * 
   * @ClassName: rpchckwtWriteFileProcessor 
   * @Description: 红包对账交易写文件 
   * @author huangzhikai
   * @date 2016年7月7日 上午11:02:46 
   *
   */

public class  rpchckwtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Input,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Property,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.rpchckwt.Header,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.rpchckwt.Body,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.rpchckwt.Foot,cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrDetl>{
	private static BizLog bizlog = BizLogUtil.getBizLog(rpchckwtWriteFileProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	 
	/**
	  * 
	  * @Title: getFileName 
	  * @Description: 获取生成文件的文件名(含路径) 
	  * @param input
	  * @param property
	  * @return String
	  * @author huangzhikai
	  * @date 2016年7月7日 上午11:03:04 
	  * @version V2.3.0
	  */
	public String getFileName(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Property property){
		
		
		if(CommUtil.isNull(input.getFilesq())){
	       	 throw DpModuleError.DpstComm.E9999("文件批次号不能为空");
	    }
		
		filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		
		/*KnpParameter  tbl_KnpParameter = SysUtil.getInstance(KnpParameter.class); 
		tbl_KnpParameter = KnpParameterDao.selectOne_odb1("DPTRAN", "RPPATH", "%", "%", true);
		
		String filepath = tbl_KnpParameter.getParm_value2()+ E_FILETP.DP020200 + File.separator + CommTools.getBaseRunEnvs().getTrxn_date() + File.separator;
		String filename = property.getDwname();
		String filesq = input.getFilesq();
		System.out.println("文件路径：" + filepath);
		System.out.println("文件名称：" + filename);
		System.out.println("批次号:" + filesq);
		String pathname = filepath.concat(File.separator).concat(filename);
		
		//将文件路径信息插入文件批量信息表
		filetab.setBusseq(input.getBusseq());
		filetab.setBtfest(E_BTFEST.DOWNSUCC);
		filetab.setDownph(filepath);
	    filetab.setFiletp(E_FILETP.DP020200);
		filetab.setDownna(filename);
		filetab.setUpfeph(filepath);
		filetab.setBtchno(filesq);
		filetab.setUpfena(filename);
		Kapb_wjplxxbDao.insert(filetab);*/
		
		bizlog.debug("文件路径：[%s]", filetab.getUpfeph());
		bizlog.debug("文件名称：[%s]", filetab.getUpfena());
		bizlog.debug("批次号:[%s]" , input.getFilesq());
		
		String pathname = filetab.getLocaph().concat(filetab.getUpfena());
		

		
		bizlog.debug("pathname:[%s]", pathname);

		return pathname;
				
	}

	/**
	 * 
	 * @Title: getHeader 
	 * @Description: 返回文件头信息 
	 * @param input
	 * @param property
	 * @return
	 * @author huangzhikai
	 * @date 2016年7月7日 上午11:03:36 
	 * @version V2.3.0
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.rpchckwt.Header getHeader(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Property property){
		long fail = 0L;
		Long count = RpBatchTransDao.selGroupCountDetailBySourdt(input.getChckdt(), true);
		
		filetab.setSuccnm(count);
		filetab.setFailnm(fail);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
		Header header = SysUtil.getInstance(Header.class);
		
		header.setChenggbs(count);
		header.setChulizbs(count);
		header.setShibaibs(fail);
		header.setZongbish(count);
		
		return header;
	}
	
	/**
	 * 
	 * @Title: getFileBodyDataWalker 
	 * @Description: 基于游标的文件数据遍历器 
	 * @param input
	 * @param property
	 * @return
	 * @author huangzhikai
	 * @date 2016年7月7日 上午11:03:56 
	 * @version V2.3.0
	 */
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrDetl> getFileBodyDataWalker(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Property property){
		Params param = new Params();
		param.put("sourdt", filetab.getAcctdt());
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrDetl>(RpBatchTransDao.namedsql_selWriteDataCheckDetail, param); 
	}

	/**
	 * 
	 * @Title: bodyProcess 
	 * @Description: 写文件体的每条记录前提供回调处理 
	 * @param index
	 * @param dateItem
	 * @param body
	 * @param input
	 * @param property
	 * @author huangzhikai
	 * @date 2016年7月7日 上午11:04:11 
	 * @version V2.3.0
	 */
	public void bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrDetl dateItem , cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.rpchckwt.Body body, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Property property) {

	}
	
	/**
	 * 
	 * @Title: getFoot 
	 * @Description: 返回文件尾信息 
	 * @param input
	 * @param property
	 * @return
	 * @author huangzhikai
	 * @date 2016年7月7日 上午11:04:30 
	 * @version V2.3.0
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.rpchckwt.Foot getFoot(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Property property){
		
		return null;
	}
	
	/**
	 * 
	 * @Title: uploadFile 
	 * @Description: 上传生成的批量文件 
	 * @param input
	 * @param property
	 * @author huangzhikai
	 * @date 2016年7月7日 上午11:04:50 
	 * @version V2.3.0
	 */
	public void uploadFile(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Property property){

	}
	
	/**
	 * 
	 * @Title: beforeWriteFileTranProcess 
	 * @Description: 写文件前处理回调 
	 * @param taskId
	 * @param input
	 * @param property
	 * @author huangzhikai
	 * @date 2016年7月7日 上午11:05:06 
	 * @version V2.3.0
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Property property) {
		filetab.setBtfest(E_BTFEST.DING);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
	}
	
	/**
	 * 
	 * @Title: writeHeaderExceptionProcess 
	 * @Description:  
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 * @author huangzhikai
	 * @date 2016年7月7日 上午11:05:25 
	 * @version V2.3.0
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	 /**
	  * 
	  * @Title: writeBodyExceptionProcess 
	  * @Description: 写文件体(单笔)异常处理回调 
	  * @param taskId
	  * @param input
	  * @param property
	  * @param dateItem
	  * @param t
	  * @author huangzhikai
	  * @date 2016年7月7日 上午11:14:36 
	  * @version V2.3.0
	  */
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Property property, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrDetl dateItem,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	 /**
	  * 
	  * @Title: writeBodyExceptionProcess 
	  * @Description: 写文件体异常处理回调
	  * @param taskId
	  * @param input
	  * @param property
	  * @param t
	  * @author huangzhikai
	  * @date 2016年7月7日 上午11:14:59 
	  * @version V2.3.0
	  */
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
     /**
      * 
      * @Title: writeFootExceptionProcess 
      * @Description: 写文件尾异常处理回调 
      * @param taskId
      * @param input
      * @param property
      * @param t
      * @author huangzhikai
      * @date 2016年7月7日 上午11:15:22 
      * @version V2.3.0
      */
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	 /**
	  * 
	  * @Title: afterWriteFileTranProcess 
	  * @Description: 写文件交易结束后回调
	  * @param taskId
	  * @param input
	  * @param property
	  * @author huangzhikai
	  * @date 2016年7月7日 上午11:15:42 
	  * @version V2.3.0
	  */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Property property){
		filetab.setBtfest(E_BTFEST.SUCC);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
		
		/*String status=E_FILEST.SUCC.getValue();
		String descri="处理成功";
		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBack(input.getBusseq(),input.getFilesq(), input.getChckdt(), filetab.getUpfena(), filetab.getUpfeph(), status, descri);*/ 
	}
	
	/**
	 * 
	 * @Title: writeFileTranExceptionProcess 
	 * @Description: 写文件交易异常处理回调 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 * @author huangzhikai
	 * @date 2016年7月7日 上午11:15:59 
	 * @version V2.3.0
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchckwt.Property property,
			Throwable t) {
		
		/*String status=E_FILEST.FAIL.getValue();
		String descri=t.getMessage();
		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBack(input.getBusseq(),input.getFilesq(), input.getChckdt(), filetab.getUpfena(), filetab.getUpfeph(), status, descri);*/ 
		
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

