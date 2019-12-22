package cn.sunline.ltts.busi.catran.batchfile;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.file.ReadFileProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.ca.namedsql.CaBatchTransDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpbtDao;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;

	 /**
	  * 读取批量开户文件
	  * 读取批量开户文件
	  *
	  */

public class rdopacReadFileProcessor extends ReadFileProcessor<cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Input,cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Property,cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.rdopac.Header,cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpbt,cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.rdopac.Foot>{
	
	private static BizLog bizlog = BizLogUtil.getBizLog(rdopacReadFileProcessor.class);
	private kapb_wjplxxb tblkapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	/**
	  *  获取待处理文件（通常为下载）
	  *  
	  *  @return 文件路径 
	  **/
	public String downloadFile(cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Property property) {
        bizlog.debug("downloadFile begin===========================>>");
        
    	tblkapbWjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
        
		bizlog.debug("文件批次号:" + input.getFilesq());
		bizlog.debug("文件名称:" + tblkapbWjplxxb.getDownna());
		bizlog.debug("文件路径:" + tblkapbWjplxxb.getDownph());
		bizlog.debug("文件类型:" + E_FILETP.CA010100.getLongName());

		// 删除该批次号下业务表数据
		KnbOpbtDao.delete_odb2(input.getFilesq());
		
		String pathname = tblkapbWjplxxb.getDownph() + tblkapbWjplxxb.getDownna();
		
		bizlog.debug("downloadFile end<<===========================");

		return pathname;
	}

    /**
	  *  解析文件头后转换为对应的javabean对象后，提供处理。
	  *  @parm header 文件头对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *
	  */
	public boolean headerProcess(cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.rdopac.Header head ,cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Property property) {
		
		bizlog.debug("headerProcess begin===========================>>");
		
		//解析文件头信息
		//设置总笔数，更新文件状态
		tblkapbWjplxxb.setTotanm(head.getCounts()); //总笔数
		
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		
		//处理文件头数据到属性中，后续使用
		property.setAgntna(head.getAgntna()); //代理单位名称
		property.setAccatp(head.getAccatp()); //账户分类
		property.setBrchno(head.getBrchno()); //所属机构
		property.setServtp(head.getServtp()); //交易渠道
		property.setAghmna(head.getAghmna());//代理人名称
		property.setAgidtp(head.getAgidtp());//代理人证件种类
		property.setAgidno(head.getAgidno());//代理人证件号码
		property.setTranus(head.getTranus());//操作柜员
		
		bizlog.debug("headerProcess end<<===========================");
		
	 	return false;
	}
	
	 /**
	  *  解析文件体后转换为对应的javabean对象后，提供处理。
	  *  @parm body 文件体对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *  注：该方法将会分发到作业服务器进行并发执行，需要配置交易控制器中的执行模式为"3"
	  */
	public boolean bodyProcess(int index, cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpbt mapping , cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Property property) {
		bizlog.debug("bodyProcess begin===========================>>");
		
		//解析文件体信息
		//处理信息数据
		mapping.setBtchno(tblkapbWjplxxb.getBtchno()); //批次号
		mapping.setAcctdt(tblkapbWjplxxb.getAcctdt()); //批次日期
		mapping.setFilena(tblkapbWjplxxb.getDownna()); //批次文件名称
		mapping.setAngtna(property.getAgntna()); //代理单位名称
		mapping.setAccttp(property.getAccatp()); //账户分类
		mapping.setBrchno(property.getBrchno()); //所属机构
		mapping.setServtp(property.getServtp()); //交易渠道
		mapping.setAghmna(property.getAghmna()); //代理人名称
		mapping.setAgidtp(property.getAgidtp()); //代理人证件类型
		mapping.setAgidno(property.getAgidno()); //代理人证件号码
		mapping.setTranus(property.getTranus()); //操作柜员
		mapping.setBtchnm(index);// 批次序号
		mapping.setCorpno(property.getBrchno().substring(0, 3));// 法人代码
		bizlog.debug("bodyProcess end<<===========================");
		
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
	public boolean footProcess(cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.rdopac.Foot foot ,cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Property property) {
	 	//TODO
	 	return false;
	}
	
	/**
	 * 读文件交易前处理
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Property property) {
		
	}

	/**
	 * 文件头解析异常处理(包括文件的下载、文件的打开、文件头的解析)
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Property property, Throwable t) {
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
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 读文件交易处理结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Property property) {
		
		bizlog.debug("afterReadFileTranProcess begin===============================>>");
		
		//查询批量开户文件中总笔数
		long tlcout = CaBatchTransDao.selBtchOpenacCountByBtchno(input.getFilesq(), tblkapbWjplxxb.getAcctdt(), false);
		
		bizlog.debug("文件总笔数[%s]", tlcout);
		bizlog.debug("文件总记录数[%s]", tblkapbWjplxxb.getTotanm());
		
		//比较文件总记录数文件总笔数，不一致则报错
		if(CommUtil.compare(tlcout, tblkapbWjplxxb.getTotanm()) != 0){
			throw CaError.Eacct.E0015();
		}
		
		tblkapbWjplxxb.setDistnm(tlcout); //处理总笔数
		tblkapbWjplxxb.setBtfest(E_BTFEST.READ); //文件状态
		
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		
		bizlog.debug("afterReadFileTranProcess end<<===============================");
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
	public void afterBodyResolveProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount) {}

	/**
	 * 读文件交易处理异常后回调(交易的所有异常都会进入到该方法，包括之前已处理过并继续抛出的异常)
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rdopac.Property property,
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

