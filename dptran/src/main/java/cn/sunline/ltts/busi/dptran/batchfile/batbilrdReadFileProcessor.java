package cn.sunline.ltts.busi.dptran.batchfile;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.file.ReadFileProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.dp.namedsql.redpck.BilBatchDao;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbCrcdBachDao;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;

	 /**
	  * 信用卡批量还款读文件
	  *
	  */

public class batbilrdReadFileProcessor extends ReadFileProcessor<cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Input,cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Property,cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.batbilrd.Header,cn.sunline.ltts.busi.dp.tables.RpMgr.KnbCrcdBach,cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.batbilrd.Foot>{
	
	private static BizLog log = BizLogUtil.getBizLog(batbilrdReadFileProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	/**
	  *  获取待处理文件（通常为下载）
	  *  
	  *  @return 文件路径 
	  **/
	public String downloadFile(cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Property property) {
		
		//文件为空时报错 add by chenjk 20161227
		/*if(CommUtil.isNull(property.getDwname())){
			throw DpModuleError.DpstComm.E9999("获取文件失败，请核查");
		}*/

		if(CommUtil.isNull(input.getFilesq())){
	       	 throw DpModuleError.DpstComm.E9999("文件批次号不能为空！");
	    }
		String filesq =input.getFilesq();
		KnbCrcdBachDao.delete_odb2(filesq);
		filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
//		filetab.setBtfest(E_BTFEST.DOWNING);
//		filetab.setDownph(property.getDwpath());
//		filetab.setDownna(property.getDwname());
		
//		filetab.setDownph("D:\\testDatas");
//		filetab.setDownna("3");
//		filetab.setFiletp(E_FILETP.DP020600);
	//	filetab.setUpfena(property.getDwname() + ".RET");
	//filetab.setUpfeph(property.getDwpath());
//		filetab.setBtchno(input.getFilesq());
//		Kapb_wjplxxbDao.insert(filetab);
		
		String pathname = filetab.getDownph() + filetab.getDownna();
//		String pathname = property.getDwpath() + File.separator + property.getDwname();
		// 获取文件信息表中的记录数据
		// filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		log.debug("<<===========================>>");
		log.debug("文件批次号:" + input.getFilesq());
		log.debug("文件名称:" + filetab.getDownna());
		log.debug("文件路径:" + filetab.getDownph());
		log.debug("文件类型:" + filetab.getFiletp());
		log.debug("<<===========================>>");		
		// TODO:执行文件下载

		return pathname;
	}

    /**
	  *  解析文件头后转换为对应的javabean对象后，提供处理。
	  *  @parm header 文件头对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *
	  */
	public boolean headerProcess(cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.batbilrd.Header head ,cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Property property) {
	 	//TODO
	 	return false;
	}
	
	 /**
	  *  解析文件体后转换为对应的javabean对象后，提供处理。
	  *  @parm body 文件体对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *  注：该方法将会分发到作业服务器进行并发执行，需要配置交易控制器中的执行模式为"3"
	  */
	public boolean bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbCrcdBach mapping , cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Property property) {
		
		log.debug("<<=============文件体解析处理==============>>");
		String trastr = mapping.getDealms().replaceAll("^(0+)", "");
		int traint = Integer.parseInt(trastr);
		mapping.setTranam(new BigDecimal(traint).multiply(BigDecimal.valueOf(0.01)));
		mapping.setFilesq(input.getFilesq());
		int groupCount = 100;
		mapping.setDataid(String.valueOf(index % groupCount));
		mapping.setTrandt(filetab.getAcctdt());
		 MsSystemSeq.getTrxnSeq();
		mapping.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		mapping.setAcctna(mapping.getCracna());
		mapping.setIdtfno(mapping.getCridno());
		mapping.setIdtftp(E_IDTFTP.SFZ);
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
	public boolean footProcess(cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.batbilrd.Foot foot ,cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Property property) {
	 	//TODO
	 	return false;
	}
	
	/**
	 * 读文件交易前处理
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Property property) {}

	/**
	 * 文件头解析异常处理(包括文件的下载、文件的打开、文件头的解析)
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Property property, Throwable t) {
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
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 读文件交易处理结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Property property) {
		
		filetab.setBtfest(E_BTFEST.SUCC);
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
	public void afterBodyResolveProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount) {
		
		log.debug("<<=============文件处理完成后处理==============>>");
		Long total = BilBatchDao.selCountByFilesq(input.getFilesq(), filetab.getAcctdt(),false);
//		log.debug("文件记录数：" + filetab.getTotanm());
		log.debug("数据记录数：" + total);
//		if(CommUtil.compare(filetab.getTotanm(), total) != 0){
//			throw DpModuleError.DpstComm.E9999("文件记录数与总记录数不匹配");
//		}
		
		
		filetab.setDistnm(total);
		//filetab.setBtfest(E_BTFEST.PARSESUCC);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
	}

	/**
	 * 读文件交易处理异常后回调(交易的所有异常都会进入到该方法，包括之前已处理过并继续抛出的异常)
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Batbilrd.Property property,
			Throwable t) {
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

