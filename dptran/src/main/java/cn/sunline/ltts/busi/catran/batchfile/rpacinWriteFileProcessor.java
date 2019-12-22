package cn.sunline.ltts.busi.catran.batchfile;


import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.accinf.Header;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

	 /**
	  * 电子账户当日开户信息反盘文件
	  * 返回当日符合相关活动条件的电子账户开户记录，按开户时间升序排序
	  *
	  */

public class  rpacinWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Input,cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Property,cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.accinf.Header,cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.accinf.Body,cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.accinf.Foot,cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAccOpenInfo>{
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(rpacinWriteFileProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Property property){
		
		if (CommUtil.isNull(input.getFilesq())) {
			throw DpModuleError.DpstComm.E9999("文件批次号不能为空");
		}
		
		filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
	
		bizlog.debug("文件路径：[%s]", filetab.getUpfeph());
		bizlog.debug("文件名称：[%s]", filetab.getUpfena());
		bizlog.debug("批次号:[%s]" , input.getBrchno());
		
		String pathname = filetab.getLocaph().concat(filetab.getUpfena());
		

		
		bizlog.debug("pathname:[%s]", pathname);

		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.accinf.Header getHeader(cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Property property){
        
		bizlog.debug(">>>>>>>>>>>>>>>>>>getHeader begin>>>>>>>>>>>>>>>>");
		
		long fail = 0L;
		Long count = 0L;
		count = EacctMainDao.selCountAccOpenInfo(input.getBrchno(), input.getMaxnum(), input.getOpendt(), false);
		
		if(count > input.getMaxnum()){
			count = Long.valueOf(input.getMaxnum());
		}
		
		filetab.setSuccnm(count);
		filetab.setFailnm(fail);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		Header header = SysUtil.getInstance(Header.class);
        header.setMaxnum(input.getMaxnum());
        header.setRelnum(count);
        header.setReltid(input.getReltid());
        
        bizlog.debug(">>>>>>>>>>>>>>>>getHeader end>>>>>>>>>>>>>>>>>>");
        
		return header;
	}
	
	/**
	 * 基于游标的文件数据遍历器
	 * 返回文件体数据遍历器
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return 
	 * 注：写文件体支持并发查数据库和写文件，最后合并，所以如果需要有顺序的需自带排序功能
	 */
	public BatchDataWalker<cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAccOpenInfo> getFileBodyDataWalker(cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Property property){

		bizlog.debug(">>>>>>>>>>>>>>>>>>>>getFileBodyDataWalker begin>>>>>>>>>>>>>>>>>>>>");
		
		Params parm = new Params();
		parm.add("brchno", input.getBrchno());
		parm.add("maxnum", input.getMaxnum());
		parm.add("trandt", input.getOpendt());
		parm.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		bizlog.debug(">>>>>>>>>>>>>>>>>>>>getFileBodyDataWalker end>>>>>>>>>>>>>>>>>>>");
	
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAccOpenInfo>(EacctMainDao.namedsql_selAccOpenInfo , parm); 
	}

	/**
	 * 写文件体的每条记录前提供回调处理
	 * 
	 * @param index 序号，从1开始
	 * @param body 文件体对象
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * 
	 */
	public void bodyProcess(int index, cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAccOpenInfo dateItem , cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.accinf.Body body, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Property property) {

	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.accinf.Foot getFoot(cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Property property){
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>>上传生成的批量文件>>>>>>>>>>>>>>>>>>>>>");
	
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Property property) {
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>>beforeWriteFileTranProcess begin>>>>>>>>>>>>>>>>>>");
		
		
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>beforeWriteFileTranProcess end>>>>>>>>>>>>>>>>>>>>>");	
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 写文件体(单笔)异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param dataItem
	 * @param t
	 */
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Property property, cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAccOpenInfo dateItem,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 写文件体异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 写文件尾异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Property property){

		bizlog.debug(">>>>>>>>>>>>>>>>>>>>>>afterWriteFileTranProcess begin>>>>>>>>>>>>>>>>");
		
		filetab.setErrotx(E_BTFEST.RTNS.getLongName());
		filetab.setBtfest(E_BTFEST.RTNS); //登记为反盘文件写成功
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>>>>afterWriteFileTranProcess end>>>>>>>>>>>>>>>>>>>");
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Rpacin.Property property,
			Throwable t) {
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>writeFileTranExceptionProcess begin>>>>>>>>>>>>>>>>>>");
		
		bizlog.debug("写文件交易异常信息[%s]", t.getMessage());
		
		DaoUtil.executeInNewTransation(new RunnableWithReturn<kapb_wjplxxb>() {
			@Override
			public kapb_wjplxxb execute() {
				filetab.setBtfest(E_BTFEST.FAIL);
				Kapb_wjplxxbDao.updateOne_odb1(filetab);
				return null;
			}
		});
		
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>writeFileTranExceptionProcess end[%]>>>>>>>>>>>>>>>>>>>>", t);
		
		//throw ExceptionUtil.wrapThrow(t);
	}

}

