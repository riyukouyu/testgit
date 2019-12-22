package cn.sunline.ltts.busi.catran.batchfile;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.file.ReadFileProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;

	 /**
	  * 读取身份核查对账文件
	  * 读取身份核查对账文件
	  *
	  */

public class rdckrtReadFileProcessor extends ReadFileProcessor<cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Input,cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Property,cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.rdckrt.Header,cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.rdckrt.Body,cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.rdckrt.Foot>{
	
	private static BizLog bizlog = BizLogUtil.getBizLog(rdopacReadFileProcessor.class);
	private kapb_wjplxxb tblkapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);	
	
	/**
	  *  获取待处理文件（通常为下载）
	  *  
	  *  @return 文件路径 
	  **/
	public String downloadFile(cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Property property) {
		bizlog.debug("downloadFile begin===========================>>");
		
		
		if (CommUtil.isNull(input.getFilesq())) {
			throw DpModuleError.DpstComm.E9999("文件批次号不能为空");
		}
		
		
		
		//获取文件批量信息
		tblkapbWjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		
		bizlog.debug("文件批次号:" + input.getFilesq());
		bizlog.debug("文件名称:" + tblkapbWjplxxb.getDownna());
		bizlog.debug("文件路径:" + tblkapbWjplxxb.getDownph());
		bizlog.debug("文件类型:" + E_FILETP.CA010200.getLongName());
		
		StringBuffer pathname = new StringBuffer();
		pathname = pathname.append(tblkapbWjplxxb.getDownph()).append(tblkapbWjplxxb.getDownna());
		
		bizlog.debug("downloadFile end<<===========================");
		
		return pathname.toString();
	}

    /**
	  *  解析文件头后转换为对应的javabean对象后，提供处理。
	  *  @parm header 文件头对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *
	  */
	public boolean headerProcess(cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.rdckrt.Header head ,cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Property property) {
		
		//解析文件头信息
		//设置总笔数，更新文件状态
		tblkapbWjplxxb.setTotanm(head.getCounts()); //总笔数
		tblkapbWjplxxb.setBtfest(E_BTFEST.DING); //文件状态
		
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		
		bizlog.debug("headerProcess end<<===========================");
		
	 	return false;
	}
	
	 /**
	  *  解析文件体后转换为对应的javabean对象后，提供处理。
	  *  @parm body 文件体对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *  注：该方法将会分发到作业服务器进行并发执行，需要配置交易控制器中的执行模式为"3"
	  */
	public boolean bodyProcess(int index, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.rdckrt.Body body , cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Property property) {
		/*
		// 根据消息ID查询MQ接收消息表
		cus_acms tblCusAcms = Cus_acmsDao.selectOne_odb_cus_acms_msgid(body.getMesgid(), false);
		
		// 如果查询结果为空，将消息插入MQ接收消息表，进行后续处理
		if (CommUtil.isNull(tblCusAcms)) {
			cus_acms tblCusAcms1 = SysUtil.getInstance(cus_acms.class);
			
			// 将未接收到的消息插入MQ接收消息表
			tblCusAcms1.setMesgid(body.getMesgid());
			tblCusAcms1.setMesgct(body.getMesgct());
			tblCusAcms1.setServid(body.getServid());
			tblCusAcms1.setMesgst(E_RECVSATE.E_RECV);
			Cus_acmsDao.insert(tblCusAcms1);
			
		} else {
			
			// 如果查询出的记录处理状态为处理失败，将状态置为接收，重新进行处理
			if (tblCusAcms.getMesgst() == E_RECVSATE.E_FAILED) {
				
				// 更新MQ接收消息表
				tblCusAcms.setMesgst(E_RECVSATE.E_RECV);
				Cus_acmsDao.updateOne_odb_cus_acms_msgid(tblCusAcms);
				
			}
		}
		*/
		
		return false;
	}
	/**
	 * 解析文件尾后转换为对应的javabean对象后，提供处理。
	 * 
	 * @param foot 文件尾对象
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return 后续是否执行自动入库操作。该返回值仅在配置了mapping属性后才有效。
	 */
	public boolean footProcess(cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.rdckrt.Foot foot ,cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Property property) {

		return false;
	}
	
	/**
	 * 读文件交易前处理
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Property property) {}

	/**
	 * 文件头解析异常处理(包括文件的下载、文件的打开、文件头的解析)
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Property property, Throwable t) {
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
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 读文件交易处理结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Property property) {
		
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
	public void afterBodyResolveProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount) {}

	/**
	 * 读文件交易处理异常后回调(交易的所有异常都会进入到该方法，包括之前已处理过并继续抛出的异常)
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdckrt.Property property,
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

