package cn.sunline.ltts.busi.inltran.batchfile;

import java.io.File;

import com.alibaba.fastjson.JSON;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.adp.cedar.server.batch.file.ReadFileProcessor;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCrcy;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCrcyDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaBatchWarnInfo;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SYDTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


	 /**
	  * 货币参数表同步
	  * 货币参数表同步
	  *
	  */

public class huobcsrdReadFileProcessor extends ReadFileProcessor<cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Input,cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Property,cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb,cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.huobcsrd.Body,cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.huobcsrd.Foot>{
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(huobcsrdReadFileProcessor.class);
	
	private kapb_wjplxxb tblkapbwjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	/**
	  *  获取待处理文件（通常为下载）
	  *  
	  *  @return 文件路径 
	  **/
	public String downloadFile(cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Property property) {
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>downloadFile begin>>>>>>>>>>>>>>>>>>>>");
		
		tblkapbwjplxxb.setBtfest(E_BTFEST.DOWNING);
		tblkapbwjplxxb.setDownph(property.getDwpath());
		tblkapbwjplxxb.setDownna(input.getFilena());
		tblkapbwjplxxb.setFiletp(E_FILETP.IN030400);
		tblkapbwjplxxb.setUpfena(input.getFilena());
		tblkapbwjplxxb.setUpfeph(property.getDwpath());
		tblkapbwjplxxb.setBtchno(input.getFilesq());
		Kapb_wjplxxbDao.insert(tblkapbwjplxxb);
		
		String pathname = property.getDwpath() + File.separator + input.getFilena() ; //文件名(包括路径)
		
		bizlog.debug("文件批次号:[%s], 文件名称: [%s],文件路径:[%s], 文件类型: [%s]", input.getFilesq(), input.getFilena(), property.getDwpath(), E_FILETP.IN030400.getLongName());
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>downloadFile end>>>>>>>>>>>>>>>>>>>>>");
		
		return pathname;
	}

    /**
	  *  解析文件头后转换为对应的javabean对象后，提供处理。
	  *  @parm header 文件头对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *
	  */
	public boolean headerProcess(cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb mapping ,cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Property property) {
		
		bizlog.debug(">>>>>>>>>>>>>>>>>headerProcess begin>>>>>>>>>>>>>>>>>>>>>");
		
		tblkapbwjplxxb.setTotanm(mapping.getTotanm()); //总笔数
		tblkapbwjplxxb.setBtfest(E_BTFEST.PARSEING);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbwjplxxb);
		
		bizlog.debug(">>>>>>>>>>>>>>>>>headerProcess end>>>>>>>>>>>>>>>>>>>>");
		
	 	return false;
	}
	
	 /**
	  *  解析文件体后转换为对应的javabean对象后，提供处理。
	  *  @parm body 文件体对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *  注：该方法将会分发到作业服务器进行并发执行，需要配置交易控制器中的执行模式为"3"
	  */
	public boolean bodyProcess(int index, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.huobcsrd.Body body , cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Property property) {
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>>bodyProcess begin>>>>>>>>>>>>>>>>>>>");
		
 		String  crcycd = BusiTools.delDoubleQuotationMarks(body.getFccaccyc()); //币种

		bizlog.debug(crcycd +"币种处理中.");
		AppCrcy kapp_huobcs = AppCrcyDao.selectOne_odb1(crcycd, false);
		
		boolean insFlag = false; //新增标志
		long acmnun = 2l;
		
		if(CommUtil.isNull(kapp_huobcs) || CommUtil.isNull(kapp_huobcs.getCrcycd())){
			
			//未找到该条信息，新增标志为真
			insFlag = true;
			kapp_huobcs = SysUtil.getInstance(AppCrcy.class);
			kapp_huobcs.setCrcycd(crcycd); //新增时需操作币种
		}else{
			E_SYDTST  status =null;
			String fccars1b = BusiTools.delDoubleQuotationMarks(body.getFccars1b());
			if(CommUtil.isNotNull(status)){
				
			  status= CommUtil.toEnum(E_SYDTST.class, fccars1b);
			}
			//该记录存在，且同步状态为删除
			if(CommUtil.isNotNull(status) && status == E_SYDTST.DEL){
				AppCrcyDao.deleteOne_odb1(crcycd);
				return true;
			}
		}
		
		
		kapp_huobcs.setCntycd(BusiTools.delDoubleQuotationMarks(body.getFccacnty())); //国别代码
		kapp_huobcs.setCcynum(BusiTools.delDoubleQuotationMarks(body.getFccanm12())); //货币符号
		kapp_huobcs.setCrcyna(BusiTools.delDoubleQuotationMarks(body.getFccanm20())); //货币名称
		kapp_huobcs.setCcymin(Long.valueOf(acmnun)); //最小货币单位
		String fccbun02 = BusiTools.delDoubleQuotationMarks(body.getFccbun02());
		if(CommUtil.isNotNull(fccbun02)){
			kapp_huobcs.setIamnun(Long.valueOf(fccbun02)); //最小计息单位
		}
		kapp_huobcs.setBasefg(E_YES___.YES); //货币等级
		
		if(insFlag){
			AppCrcyDao.insert(kapp_huobcs);
		}else{
			AppCrcyDao.updateOne_odb1(kapp_huobcs);
		}
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>>bodyProcess end>>>>>>>>>>>>>>>>>>");
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
	public boolean footProcess(cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.huobcsrd.Foot foot ,cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Property property) {
	 	return false;
	}
	
	/**
	 * 读文件交易前处理
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Property property) {}

	/**
	 * 文件头解析异常处理(包括文件的下载、文件的打开、文件头的解析)
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Property property, Throwable t) {
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
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 读文件交易处理结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Property property) {
		tblkapbwjplxxb.setBtfest(E_BTFEST.SUCC);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbwjplxxb);
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
	public void afterBodyResolveProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount) {}

	/**
	 * 读文件交易处理异常后回调(交易的所有异常都会进入到该方法，包括之前已处理过并继续抛出的异常)
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Huobcsrd.Property property,
			Throwable t) {
		
				tblkapbwjplxxb.setBtfest(E_BTFEST.FAIL);
				Kapb_wjplxxbDao.updateOne_odb1(tblkapbwjplxxb);
				
				//监控预警平台
				KnpParameter para = KnpParameterDao.selectOne_odb1("DAYENDNOTICE", "%", "%", "%", true);
				
				String bdid = para.getParm_value1();// 服务绑定ID
				
				String mssdid = CommTools.getMySysId();// 随机生成消息ID
				
				String mesdna = para.getParm_value2();// 媒介名称
				
//				E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介//rambo delete
				
				IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
				
				IoCaOtherService.IoCaDayEndFailNotice.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaDayEndFailNotice.InputSetter.class);
				
				String timetm = DateTools2.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS000");
				IoCaBatchWarnInfo content = SysUtil.getInstance(IoCaBatchWarnInfo.class);
				content.setPljioyma("huobcsrd");
				content.setPljyzbsh("0304");
				content.setPljyzwmc("货币参数同步");
				content.setErrmsg("货币参数同步异常");
				content.setTrantm(timetm);
				
				// 发送消息
				mqInput.setMsgid(mssdid); // 消息ID
//				mqInput.setMedium(mssdtp); // 消息媒介//rambo delete
				mqInput.setMdname(mesdna); // 媒介名称
				mqInput.setTypeCode("NAS");
				mqInput.setTypeName("网络金融核心平台-电子账户核心系统");
				mqInput.setItemId("NAS_BATCH_WARN");
				mqInput.setItemName("电子账户核心批量执行错误预警");
				String str =JSON.toJSONString(content);
				mqInput.setContent(str);
				
				mqInput.setWarnTime(timetm);
				
				caOtherService.dayEndFailNotice(mqInput);
	}
}

