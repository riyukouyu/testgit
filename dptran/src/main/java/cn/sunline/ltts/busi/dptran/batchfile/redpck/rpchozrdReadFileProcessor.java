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
 * 
 * @ClassName: rpchozrdReadFileProcessor 
 * @Description: 红包批量冲正上送文件 
 * @author huangzhikai
 * @date 2016年7月18日 下午09:15:07 
 *
 */
public class rpchozrdReadFileProcessor extends ReadFileProcessor<cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Input,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Property,cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb,cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.rpchozrd.Foot>{
	private static BizLog log = BizLogUtil.getBizLog(rpchozrdReadFileProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	/**
	  *  获取待处理文件（通常为下载）
	  *  
	  *  @return 文件路径 
	  **/
	public String downloadFile(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Property property) {
		
		
		KnbRptrBachDao.delete_odb2(input.getFilesq());// 先删除该批次号数据
		
		filetab = Kapb_wjplxxbDao.selectOneWithLock_odb1(input.getFilesq(), true);
		
		String pathname = filetab.getDownph() +  filetab.getDownna();
		log.debug("文件批次号:" + input.getFilesq());
		log.debug("文件名称:" + filetab.getDownna());
		log.debug("文件路径:" + filetab.getDownph());
		log.debug("文件类型" + filetab.getFiletp());
		
		return pathname;
	}

	/**
	 * 
	 * @Title: headerProcess 
	 * @Description: 解析文件头后转换为对应的javabean对象后，提供处理 
	 * @param mapping
	 * @param input
	 * @param property
	 * @return boolean
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:19:08 
	 * @version V2.3.0
	 */
	public boolean headerProcess(cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb mapping ,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Property property) {
		log.debug("<<===========================>>");
		log.debug("文件头处理");
		log.debug("<<===========================>>");
		filetab.setTotanm(mapping.getTotanm()); //总笔数
		filetab.setBtfest(E_BTFEST.DING);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
	 	return false;
	}
	
	/**
	 * 
	 * @Title: bodyProcess 
	 * @Description: 解析文件体后转换为对应的javabean对象后，提供处理 
	 * @param index
	 * @param mapping
	 * @param input
	 * @param property
	 * @return boolean 
	 * @author huangzhikai
	 * @date 2016年7月18日 上午10:20:50 
	 * @version V2.3.0
	 */
	public boolean bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach mapping , cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Property property) {
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
	 * 
	 * @Title: footProcess 
	 * @Description: 解析文件尾后转换为对应的javabean对象后，提供处理 
	 * @param foot
	 * @param input
	 * @param property
	 * @return boolean
	 * @author huangzhikai
	 * @date 2016年7月18日 上午10:25:01 
	 * @version V2.3.0
	 */
	public boolean footProcess(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.rpchozrd.Foot foot ,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Property property) {
	 	return false;
	}
	
	/**
	 * 
	 * @Title: beforeReadFileTranProcess 
	 * @Description: 读文件交易前处理 
	 * @param taskId
	 * @param input
	 * @param property
	 * @author huangzhikai
	 * @date 2016年7月18日 上午10:21:50 
	 * @version V2.3.0
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Property property) {
		
	}

	/**
	 * 
	 * @Title: headerResolveExceptionProcess 
	 * @Description: 文件头解析异常处理 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 * @author huangzhikai
	 * @date 2016年7月20日 上午08:56:34 
	 * @version V2.3.0
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Property property, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	
	/**
	 * 
	 * @Title: afterBodyResolveCommitProcess 
	 * @Description: 文件体一个批次处理并入库后回调 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param addSuccessCount
	 * @param addErrorCount
	 * @author huangzhikai
	 * @date 2016年7月19日 上午08:57:00 
	 * @version V2.3.0
	 */
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Property property,
			String jobId, int addSuccessCount, int addErrorCount) {}

	/**
	 * 
	 * @Title: bodyResolveExceptionProcess 
	 * @Description: 文件体单行记录解析异常处理器 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param line
	 * @param t
	 * @author huangzhikai
	 * @date 2016年7月19日 上午08:57:22 
	 * @version V2.3.0
	 */
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Property property,
			String jobId, String line, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 
	 * @Title: bodyResolveExceptionProcess 
	 * @Description: 文件体一个批次解析异常处理器 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param totalSuccessCount
	 * @param totalErrorCount
	 * @param t
	 * @author huangzhikai
	 * @date 2016年7月19日 上午08:56:04 
	 * @version V2.3.0
	 */
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 
	 * @Title: afterReadFileTranProcess 
	 * @Description: 读文件交易处理结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @author huangzhikai
	 * @date 2016年7月19日 上午08:57:33 
	 * @version V2.3.0
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Property property) {
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
	 * 
	 * @Title: afterBodyResolveProcess 
	 * @Description: 文件体一个批次解析结束后回调 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param totalSuccessCount
	 * @param totalErrorCount
	 * @author huangzhikai
	 * @date 2016年7月19日 上午08:58:03 
	 * @version V2.3.0
	 */
	public void afterBodyResolveProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount) {
		
	}

	/**
	 * 
	 * @Title: readFileTranExceptionProcess 
	 * @Description: 读文件交易处理异常后回调 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 * @author huangzhikai
	 * @date 2016年7月19日 上午08:58:22
	 * @version V2.3.0
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpchozrd.Property property,
			Throwable t) {
		
		DaoUtil.executeInNewTransation(new RunnableWithReturn<kapb_wjplxxb>() {
			@Override
			public kapb_wjplxxb execute() {
				filetab.setBtfest(E_BTFEST.FAIL);
				Kapb_wjplxxbDao.updateOne_odb1(filetab);
				return null;
			}
		});
		
		log.debug("文件时间:[%s]", filetab.getAcctdt());
		log.debug("业务流水:[%s], 文件批次号: [%s],文件时间:[%s]", filetab.getBusseq(), filetab.getBtchno(), filetab.getAcctdt());
		
		 
		throw ExceptionUtil.wrapThrow(t);
	}
	
}

