package cn.sunline.ltts.busi.catran.batchfile;

import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.file.ReadFileProcessor;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbAcif;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbAcifBach;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbAcifBachDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbAcifDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaBatchWarnInfo;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PROCST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

//import com.alibaba.fastjson.JSON;

	 /**
	  * 恐怖名单批量读文件
	  *
	  */

public class terrwlReadFileProcessor extends ReadFileProcessor<cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Input,cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Property,cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb,cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbAcifBach,cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.terrwl.Foot>{
	private static BizLog log = BizLogUtil.getBizLog(terrwlReadFileProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	//private static   E_FILETP filetp = E_FILETP.CA010400;
	/**
	  *  获取待处理文件（通常为下载）
	  *  
	  *  @return 文件路径 
	  **/
	public String downloadFile(cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Property property) {
		
		if (CommUtil.isNull(input.getFilesq())) {
			throw DpModuleError.DpstComm.E9999("文件批次号不能为空");
		}
		String filesq =input.getFilesq();
		KnbAcifBachDao.delete_odb1(filesq);
		filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);

		String pathname = filetab.getDownph()  + filetab.getDownna();
		log.debug("<<===========================>>");
		log.debug("文件批次号:" + input.getFilesq());
		//log.debug("文件名称:" + "test.txt");
		//log.debug("文件路径:" +"D:\\11");
		log.debug("文件名称:" + property.getDwname());
		log.debug("文件路径:" +property.getDwpath());
		log.debug("文件类型:" + E_FILETP.CA010700.getLongName());
		log.debug("<<===========================>>");
		return pathname;
	}

    /**
	  *  解析文件头后转换为对应的javabean对象后，提供处理。
	  *  @parm header 文件头对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *
	  */
	public boolean headerProcess(cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb mapping ,cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Property property) {

		if (log.isDebugEnabled()) {
			log.debug("<<===========================>>");
			log.debug("文件头处理");
			log.debug("<<===========================>>");
		}
		//filetab.setTotanm(mapping.getTotanm()); //总笔数
		filetab.setBtfest(E_BTFEST.DING);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
		
	 	return false;
	}
	
	 /**
	  *  解析文件体后转换为对应的javabean对象后，提供处理。
	  *  @parm body 文件体对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *  注：该方法将会分发到作业服务器进行并发执行，需要配置交易控制器中的执行模式为"3"
	  */
	public boolean bodyProcess(int index, cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbAcifBach mapping , cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Property property) {
		
		log.debug("<<=============文件体解析处理==============>>");

		mapping.setFilesq(input.getFilesq());
		mapping.setDealst((E_PROCST.SUSPEND));;
		mapping.setTrdtdt(CommTools.getBaseRunEnvs().getTrxn_date());
		
		
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
	public boolean footProcess(cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.terrwl.Foot foot ,cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Property property) {
	 	//TODO
	 	return false;
	}
	
	/**
	 * 读文件交易前处理
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Property property) {
		
		
	}

	
	
	/**
	 * 文件头解析异常处理(包括文件的下载、文件的打开、文件头的解析)
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Property property, Throwable t) {
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
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 读文件交易处理结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Property property) {
		log.debug("<<=============文件处理完成后处理==============>>");

		String filesq = input.getFilesq();
		List<KnbAcifBach> bach =KnbAcifBachDao.selectAll_odb1(filesq, false);
		for(KnbAcifBach bac:bach){
		KnbAcif acif=SysUtil.getInstance(KnbAcif.class);
	    String cardno =bac.getIdtftp()+"_"+bac.getIdtfno();
		acif.setDatatp("IDType_IDNumber");
		acif.setCardno(cardno);
		acif.setCustna(bac.getCustna());
		acif.setInevil(E_INSPFG.INEV);
		acif.setIseffe(E_YES___.YES);
		KnbAcifDao.insert(acif);
		}
       
		
		filetab.setBtfest(E_BTFEST.SUCC); //文件写成功
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
	public void afterBodyResolveProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount) {}

	/**
	 * 读文件交易处理异常后回调(交易的所有异常都会进入到该方法，包括之前已处理过并继续抛出的异常)
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Terrwl.Property property,
			Throwable t) {
				filetab.setBtfest(E_BTFEST.FAIL);
				Kapb_wjplxxbDao.updateOne_odb1(filetab);
				
				//监控预警平台
				KnpParameter para = KnpParameterDao.selectOne_odb1("DAYENDNOTICE", "%", "%", "%", true);
				
				String bdid = para.getParm_value1();// 服务绑定ID
				
				String mssdid = CommTools.getMySysId();// 随机生成消息ID
				
				String mesdna = para.getParm_value2();// 媒介名称
				
				IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
				
				IoCaOtherService.IoCaDayEndFailNotice.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaDayEndFailNotice.InputSetter.class);
				
				String timetm = DateTools2.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS000");
				IoCaBatchWarnInfo content = SysUtil.getInstance(IoCaBatchWarnInfo.class);
				content.setPljioyma("terrwl");
				content.setPljyzbsh("0107");
				content.setPljyzwmc("恐怖名单同步");
				content.setErrmsg("恐怖名单同步异常");
				content.setTrantm(timetm);
				
				// 发送消息
				mqInput.setMsgid(mssdid); // 消息ID
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
	


