package cn.sunline.ltts.busi.dptran.batchfile.redpck;


import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.file.ReadFileProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.dp.namedsql.redpck.RpBatchTransDao;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBachDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;

	 /**
	  * 红包批量退款上送文件
	  *
	  */

public class rpbackrdReadFileProcessor extends ReadFileProcessor<cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Input,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Property,cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb,cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.rpbackrd.Foot>{
	
	
	private static BizLog log = BizLogUtil.getBizLog(rpbackrdReadFileProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	/**
	  *  获取待处理文件（通常为下载）
	  *  
	  *  @return 文件路径 
	  **/
	public String downloadFile(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Property property) {
		filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		
		// 删除本批次数据
		KnbRptrBachDao.delete_odb2(input.getFilesq());
		
		String pathname = filetab.getDownph() +  filetab.getDownna();
		log.debug("文件批次号:" + input.getFilesq());
		log.debug("文件名称:" + filetab.getDownna());
		log.debug("文件路径:" + filetab.getDownph());
		log.debug("文件类型" + filetab.getFiletp());
		
		return pathname;
	}

    /**
	  *  解析文件头后转换为对应的javabean对象后，提供处理。
	  *  @parm header 文件头对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *
	  */
	public boolean headerProcess(cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb mapping ,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Property property) {
		
		if (log.isDebugEnabled()) {
			log.debug("<<===========================>>");
			log.debug("文件头处理");
			log.debug("<<===========================>>");
		}
		filetab.setTotanm(mapping.getTotanm()); //总笔数
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		return false;
	}
	
	 /**
	  *  解析文件体后转换为对应的javabean对象后，提供处理。
	  *  @parm body 文件体对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *  注：该方法将会分发到作业服务器进行并发执行，需要配置交易控制器中的执行模式为"3"
	  */
	public boolean bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach mapping , cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Property property) {
			//TODO: 
		log.debug("<<=============文件体解析处理==============>>");
		
		String transq = MsSystemSeq.getTrxnSeq();
		mapping.setTransq(transq);
		mapping.setFilesq(input.getFilesq());
		mapping.setTranst(E_TRANST.WAIT);
		int groupCount = 100;
		mapping.setDataid(String.valueOf(index % groupCount));
		log.debug("<<=============文件体解析完成==============>>");
		return true;
	}
	/**
	 * 解析文件尾后转换为对应的javabean对象后，提供处理。
	 * 
	 * @param foot 文件尾对象
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return 后续是否执行自动入库操作。该返回值仅在配置了mapping属性后才有效。
	 */
	public boolean footProcess(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.rpbackrd.Foot foot ,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Property property) {
	 	//TODO
	 	return false;
	}
	
	/**
	 * 读文件交易前处理
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Property property) {
		
	}

	/**
	 * 文件头解析异常处理(包括文件的下载、文件的打开、文件头的解析)
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Property property, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	
	/**
	 * 文件体一个批次处理并入库后回调(调用时间与设置的事务提交间隔相关)
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param addSuccessCount
	 * @param addErrorCount
	 */
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Property property,
			String jobId, int addSuccessCount, int addErrorCount) {}

	/**
	 * 文件体单行记录解析异常处理器
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param line
	 * @param t
	 */
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Property property,
			String jobId, String line, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 文件体一个批次解析异常处理器
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param totalSuccessCount
	 * @param totalErrorCount
	 * @param t
	 */
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 读文件交易处理结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Property property) {
		
		log.debug("<<=============文件处理完成后处理==============>>");
		
		Long total = RpBatchTransDao.selCountByFilesq(input.getFilesq(), filetab.getAcctdt(),false);

		log.debug("文件记录数：" + filetab.getTotanm());
		log.debug("数据记录数：" + total);
		if(CommUtil.compare(filetab.getTotanm(), total) != 0){
			throw DpModuleError.DpstComm.E9999("文件记录数与总记录数不匹配");
		}
		
		
		filetab.setDistnm(total);
		filetab.setBtfest(E_BTFEST.READ);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
	}

	/**
	 * 文件体一个批次解析结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param totalSuccessCount
	 * @param totalErrorCount
	 */
	public void afterBodyResolveProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount) {}

	/**
	 * 读文件交易处理异常后回调(交易的所有异常都会进入到该方法，包括之前已处理过并继续抛出的异常)
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackrd.Property property,
			Throwable t) {
		
		DaoUtil.executeInNewTransation(new RunnableWithReturn <kapb_wjplxxb>() {
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

