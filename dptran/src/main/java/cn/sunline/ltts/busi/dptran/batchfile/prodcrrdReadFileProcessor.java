package cn.sunline.ltts.busi.dptran.batchfile;

import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
//import com.alibaba.fastjson.JSON;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.adp.cedar.server.batch.file.ReadFileProcessor;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
//import cn.sunline.ltts.busi.ltts.zjrc.infrastructure.dap.plugin.type.ECustPlugin.E_MEDIUM;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsProdCler;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsProdClerDao;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsProdClerHist;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsProdClerHistDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaBatchWarnInfo;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

	 /**
	  * 产品核算读文件
	  *
	  */

public class prodcrrdReadFileProcessor extends ReadFileProcessor<cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Input,cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Property,cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb,cn.sunline.ltts.busi.dp.tables.DpAccount.KnsProdClerHist,cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.prodcrrd.Foot>{
	private static BizLog log = BizLogUtil.getBizLog(prodcrrdReadFileProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	/**
	  *  获取待处理文件（通常为下载）
	  *  
	  *  @return 文件路径 
	  **/
	public String downloadFile(cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Property property) {
			filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		
			// 删除本批次数据
			KnsProdClerHistDao.delete_odb1(input.getFilesq());
			
			String pathname =  filetab.getDownph()  + filetab.getDownna();
			log.debug("文件批次号:" + input.getFilesq());
			log.debug("文件名称:" + filetab.getDownna());
			log.debug("文件路径:" + filetab.getDownph());
			log.debug("文件类型" + filetab.getFiletp().getLongName());
			
			return pathname;
	}

    /**
	  *  解析文件头后转换为对应的javabean对象后，提供处理。
	  *  @parm header 文件头对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *
	  */
	public boolean headerProcess(cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb mapping ,cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Property property) {
	 	//TODO
	 	return false;
	}
	
	 /**
	  *  解析文件体后转换为对应的javabean对象后，提供处理。
	  *  @parm body 文件体对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *  注：该方法将会分发到作业服务器进行并发执行，需要配置交易控制器中的执行模式为"3"
	  */
	public boolean bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.DpAccount.KnsProdClerHist mapping , cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Property property) {
		log.debug("<<=============文件体解析处理==============>>");

		mapping.setFilesq(input.getFilesq());
		mapping.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		
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
	public boolean footProcess(cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.prodcrrd.Foot foot ,cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Property property) {
	 	//TODO
	 	return false;
	}
	
	/**
	 * 读文件交易前处理
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Property property) {}

	/**
	 * 文件头解析异常处理(包括文件的下载、文件的打开、文件头的解析)
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Property property, Throwable t) {
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
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 读文件交易处理结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Property property) {}

	/**
	 * 文件体一个批次解析结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param totalSuccessCount
	 * @param totalErrorCount
	 */
	public void afterBodyResolveProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount) {
		log.debug("<<=============文件处理完成后处理==============>>");

		filetab.setBtfest(E_BTFEST.SUCC);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
		List<KnsProdClerHist> infoList = KnsProdClerHistDao.selectAll_odb1(input.getFilesq(), false);
		
		if(CommUtil.isNotNull(infoList)){
			//删除产品核算正式表信息
			DpAcctQryDao.delProdInfo();
			
			for(KnsProdClerHist tblKnsProdClerHist : infoList){
				KnsProdCler tblKnsProdCler  = SysUtil.getInstance(KnsProdCler.class);
				CommUtil.copyProperties(tblKnsProdCler, tblKnsProdClerHist);
				KnsProdClerDao.insert(tblKnsProdCler);
			}
		}
		
		//计算一个月前的日期
		String lastdt = DateTimeUtil.dateAdd("day", CommTools.getBaseRunEnvs().getTrxn_date(), -30);
		
		//保留历史表中一个月的数据
		DpAcctQryDao.delKnsProdClerHist(lastdt);
	}

	/**
	 * 读文件交易处理异常后回调(交易的所有异常都会进入到该方法，包括之前已处理过并继续抛出的异常)
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Prodcrrd.Property property,
			Throwable t) {
		
				filetab.setBtfest(E_BTFEST.FAIL);
				Kapb_wjplxxbDao.updateOne_odb1(filetab);
				
				//监控预警平台
				KnpParameter para = KnpParameterDao.selectOne_odb1("DAYENDNOTICE", "%", "%", "%", true);
				
				String bdid = para.getParm_value1();// 服务绑定ID
				
				String mssdid = CommTools.getMySysId();// 随机生成消息ID
				
				String mesdna = para.getParm_value2();// 媒介名称
				
//				E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介
				
				IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
				
				IoCaOtherService.IoCaDayEndFailNotice.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaDayEndFailNotice.InputSetter.class);
				
				String timetm = DateTools2.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS000");
				IoCaBatchWarnInfo content = SysUtil.getInstance(IoCaBatchWarnInfo.class);
				content.setPljioyma("prodcrrd");
				content.setPljyzbsh("0210");
				content.setPljyzwmc("产品核算同步");
				content.setErrmsg("产品核算同步异常");
				content.setTrantm(timetm);
				
				// 发送消息
				mqInput.setMsgid(mssdid); // 消息ID
//				mqInput.setMedium(mssdtp); // 消息媒介
				mqInput.setMdname(mesdna); // 媒介名称
				mqInput.setTypeCode("NAS");
				mqInput.setTypeName("网络金融核心平台-电子账户核心系统");
				mqInput.setItemId("NAS_BATCH_WARN");
				mqInput.setItemName("电子账户核心批量执行错误预警");
//				String str =JSON.toJSONString(content);
//				mqInput.setContent(str);
				
				mqInput.setWarnTime(timetm);
				
				caOtherService.dayEndFailNotice(mqInput);
		
	}
	
}

