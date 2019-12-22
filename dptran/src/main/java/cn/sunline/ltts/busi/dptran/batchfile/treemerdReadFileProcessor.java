package cn.sunline.ltts.busi.dptran.batchfile;

import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
//import com.alibaba.fastjson.JSON;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
//import cn.sunline.ltts.busi.ltts.zjrc.infrastructure.dap.plugin.type.ECustPlugin.E_MEDIUM;
import cn.sunline.adp.cedar.server.batch.file.ReadFileProcessor;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.namedsql.ProdClearBatchDao;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnbParaMenu;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnbParaMenuDao;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnbParaMenuHist;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnbParaMenuHistDao;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnbParaMenuTemp;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnbParaMenuTempDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaBatchWarnInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

	 /**
	  * 目录树读文件
	  *
	  */

public class treemerdReadFileProcessor extends ReadFileProcessor<cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Input,cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Property,cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb,cn.sunline.ltts.busi.dp.tables.DpAccount.KnbParaMenuTemp,cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.treemerd.Foot>{
	private static BizLog log = BizLogUtil.getBizLog(treemerdReadFileProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	 	
	/**
	  *  获取待处理文件（通常为下载）
	  *  
	  *  @return 文件路径 
	  **/
	public String downloadFile(cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Property property) {
		if(CommUtil.isNull(input.getFilesq())){
			throw DpModuleError.DpstComm.E9999("文件批次号不能为空");
		}
		
//		if(CommUtil.isNull(input.getTrandt())){
//			throw DpModuleError.DpstComm.E9999("原交易日期不能为空");
//		}
		String filesq = input.getFilesq();
		KnbParaMenuTempDao.delete_odb3(filesq);
		filetab =Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
//		filetab.setBtfest(E_BTFEST.DOWNING);
//		filetab.setDownph("D:\\test/");	//property.getDwpath()
//		filetab.setDownna("cux_ea_cp_v_20170210.txt");//property.getDwname()
//		filetab.setDownph(property.getDwpath());	
//		filetab.setDownna(property.getDwname());
//		filetab.setFiletp(E_FILETP.DP020900);
//		filetab.setUpfena(property.getDwname() + ".RET");
//		filetab.setUpfeph(property.getDwpath());
//		filetab.setBtchno(input.getFilesq());
//		Kapb_wjplxxbDao.insert(filetab);
//		String pathname = "D:\\test" + File.separator +"cux_ea_cp_v_20170222.txt"; //文件名(包括路径)
		String pathname =filetab.getDownph()  + filetab.getDownna();// property.getDwpath() + File.separator + property.getDwname();
		log.debug("文件批次号:" + input.getFilesq() );
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
	public boolean headerProcess(cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb mapping ,cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Property property) {
		log.debug("<<===========================>>");
		log.debug("文件头处理");
		log.debug("<<===========================>>");
//		filetab.setTotanm(mapping.getTotanm()); //总笔数
		//filetab.setBtfest(E_BTFEST.PARSEING);
//		Kapb_wjplxxbDao.updateOne_odb1(filetab);
	 	return false;
	}
	
	 /**
	  *  解析文件体后转换为对应的javabean对象后，提供处理。
	  *  @parm body 文件体对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *  注：该方法将会分发到作业服务器进行并发执行，需要配置交易控制器中的执行模式为"3"
	  */
	@Override
	public boolean bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.DpAccount.KnbParaMenuTemp mapping , cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Property property) {
		log.debug("<<=============文件体解析处理==============>>");

		mapping.setFilesq(input.getFilesq());
		mapping.setTrandt(filetab.getAcctdt());
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
	public boolean footProcess(cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.treemerd.Foot foot ,cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Property property) {
	 	//TODO
	 	return false;
	}
	
	/**
	 * 读文件交易前处理
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Property property) {
		//清空临时表数据
		DpAcctQryDao.delMenuTemp();
	}

	/**
	 * 文件头解析异常处理(包括文件的下载、文件的打开、文件头的解析)
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Property property, Throwable t) {
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
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 读文件交易处理结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Property property) {
		log.debug("<<=============文件处理完成后处理==============>>");
		Long total = ProdClearBatchDao.selBatchCountByfilesq(input.getFilesq(), filetab.getAcctdt(),false);
		
		log.debug("文件记录数：" + filetab.getTotanm());
		log.debug("数据记录数：" + total);
//		if(CommUtil.compare(filetab.getTotanm(), total) != 0){
//			throw DpModuleError.DpstComm.E9999("文件记录数与总记录数不匹配");
//		}
		
		filetab.setDistnm(total);
		filetab.setBtfest(E_BTFEST.SUCC);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
		KnbParaMenuTempDao.delete_odb1(filetab.getBtchno(), "T");
		
		//更新子值代码为空的记录
		DpAcctQryDao.updKnbParaMenuTempvalue(filetab.getBtchno());
		
		String trandt = DateTools2.getDateInfo().getSystdt();
		
		//获取当前满足条件的目录信息（使用范围为11，12,13的数据）
		List<KnbParaMenuTemp> listInfo = DpAcctQryDao.selMenuInfo(trandt, false);
		
		if(CommUtil.isNotNull(listInfo)){
			for(KnbParaMenuTemp info : listInfo){
				insMenuInfo(info);
				/*//判断使用范围是11 
				if(CommUtil.equals("11", info.getUsesys())){
					//递归调用插入目录数据信息
					insMenuInfo(info);
					
				}else if(CommUtil.equals("12", info.getUsesys())){
					//递归调用插入目录数据信息
					insMenuInfo(info);
				
				}else{
					//递归调用插入目录数据信息
					insMenuInfo(info);
				}*/
			}
		}
		//更新目录树最后一级内部户级别为LEV4
		DpDayEndDao.updKnbParaMenuLV();
		
		//删除历史表信息
		DpAcctQryDao.delMenuHistInfo(input.getFilesq());
		
		//删除正式表信息
		DpAcctQryDao.delMenuInfo(input.getFilesq());
		
		//获取当前正式表数据信息
		List<KnbParaMenu> menuInfo = KnbParaMenuDao.selectAll_odb1(input.getFilesq(), true);
		
		//将正式表数据转入历史表
		for(KnbParaMenu tblKnbParaMenu : menuInfo){
			KnbParaMenuHist tblKnbParaMenuHist = SysUtil.getInstance(KnbParaMenuHist.class);
			CommUtil.copyProperties(tblKnbParaMenuHist, tblKnbParaMenu);
			KnbParaMenuHistDao.insert(tblKnbParaMenuHist);
		}
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
	public void afterBodyResolveProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount) {
		
		
	}

	/**
	 * 读文件交易处理异常后回调(交易的所有异常都会进入到该方法，包括之前已处理过并继续抛出的异常)
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Treemerd.Property property,
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
				content.setPljioyma("treemerd");
				content.setPljyzbsh("0209");
				content.setPljyzwmc("目录树同步");
				content.setErrmsg("目录树同步异常");
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
	
	/**
	 * 递归插入目录信息
	 * @param info
	 * @param property
	 */
	public void insMenuInfo(KnbParaMenuTemp info){
		
		//插入当前层级目录的信息
		KnbParaMenu tblKnbParaMenu = SysUtil.getInstance(KnbParaMenu.class);
		CommUtil.copyProperties(tblKnbParaMenu, info);
		
		if(CommUtil.isNull(tblKnbParaMenu.getChcode())){
			tblKnbParaMenu.setChcode("end");
		}
		
//		tblKnbParaMenu.setChcode("end");
		KnbParaMenuDao.insert(tblKnbParaMenu);
		
		//查找临时表父级目录信息
		KnbParaMenuTemp tblKnbParaMenuTemp = DpAcctQryDao.selKnbparamenuTempOne(filetab.getBtchno(), info.getCodevl(), false);
	    //查找正式表父级目录信息
		tblKnbParaMenu = DpAcctQryDao.selKnbparamenuOne(filetab.getBtchno(), info.getCodevl(), false);
		
		if(CommUtil.isNotNull(tblKnbParaMenuTemp)){
			if(CommUtil.isNull(tblKnbParaMenu)){
				//递归调用插入父级目录
				insMenuInfo(tblKnbParaMenuTemp);
			}else{
				//父级目录已插入正式表，不许插入
			}
		}else{
			//父级目录不存在
		}
	}
}

