package cn.sunline.ltts.busi.inltran.batchfile;

import java.io.File;

import com.alibaba.fastjson.JSON;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaBatchWarnInfo;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;

	 /**
	  * 总分核对供数明细
	  *
	  */

public class  glfewtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Input,cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Property,cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.glfewt.Header,cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.glfewt.Body,cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.glfewt.Foot,cn.sunline.ltts.busi.in.tables.In.KnbGlfaBatch>{
	private static final BizLog bizlog = BizLogUtil.getBizLog(glfewtWriteFileProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class); 
	private String tblsrc = "knb_glfa_batch";// 表名
	KnpParameter  tbl = KnpParameterDao.selectOne_odb1(tblsrc, "%", "%", "%", true);
	private String datatp = tbl.getParm_value1();// 增全量标识
	private int totalRecords;//总记录数
	private String trandt = DateTools2.getDateInfo().getLastdt();
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Property property){
		String filename = "knb_glfa_batch_"+"AAAAA_"+trandt+"_"+datatp+"_NAS"+".txt";
		String filePath = KnpParameterDao.selectOne_odb1("DATAPRO", datatp, "%", "%", true).getParm_value2();
		String path = filePath+File.separator+trandt;
		
        bizlog.method(">>>>>>>>>>>>>>>>>>>>getFileName begin>>>>>>>>>>>>>>>>>>>");
		String seqno = CommTools.getBaseRunEnvs().getTrxn_seq();
		
		filetab.setUpfena(filename);
		filetab.setUpfeph(path);
		filetab.setBtchno(seqno);
		filetab.setFiletp(E_FILETP.IN030300);
		filetab.setBtfest(E_BTFEST.GIVING);
		Kapb_wjplxxbDao.insert(filetab);
		
		filetab = Kapb_wjplxxbDao.selectOne_odb1(seqno, true);
		String pathname = filetab.getUpfeph() + File.separator + filetab.getUpfena();
		
		bizlog.parm("-------------pathname[%s]", pathname);
		bizlog.method(">>>>>>>>>>>>>>>>>>>getFileName end>>>>>>>>>>>>>>>>>>>>");
		
		return path+"\\"+filename;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.glfewt.Header getHeader(cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 基于游标的文件数据遍历器
	 * 返回文件体数据遍历器
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return 
	 * 注：写文件体支持并发查数据库和写文件，最后合并，所以如果需要有顺序的需自带排序功能
	 */
	public BatchDataWalker<cn.sunline.ltts.busi.in.tables.In.KnbGlfaBatch> getFileBodyDataWalker(cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Property property){
		String trandt = DateTools2.getDateInfo().getLastdt();
		Params param = new Params();
		param.put("filesq", trandt+"0001");
		param.put("trandt", trandt);
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.in.tables.In.KnbGlfaBatch>(InDayEndSqlsDao.namedsql_selglfaWriteData , param); 
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.in.tables.In.KnbGlfaBatch dateItem , cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.glfewt.Body body, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Property property) {
		totalRecords = index ;

		bizlog.method(">>>>>>>>>>>>Begin>>>>>>>>>>>>");
		bizlog.parm("input [%s]", input);
		bizlog.parm("property [%s]", property);

		body.setLineid(String.valueOf(index));//设置行数
	
		bizlog.method("<<<<<<<<<<<<End<<<<<<<<<<<<");
	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.glfewt.Foot getFoot(cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Property property) {
		filetab.setBtfest(E_BTFEST.RESTING);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Property property, cn.sunline.ltts.busi.in.tables.In.KnbGlfaBatch dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Property property){
		filetab.setBtfest(E_BTFEST.RESTSUCC);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		KnpParameter  tbl_KnpParameter = SysUtil.getInstance(KnpParameter.class);
		tbl_KnpParameter = KnpParameterDao.selectOne_odb1("DATAPRO", datatp, "%", "%", true);
		   String filename = tblsrc+"_" + "AAAAA_" + trandt + "_" + datatp + "_NAS" + ".txt";	
		    String filenm2 = tblsrc+"_" + "AAAAA_" + trandt + "_" + datatp + "_NAS" + ".flg";   	  
		    String flpath = tbl_KnpParameter.getParm_value2()+File.separator+trandt;//路径加上日期		 
		    String pathname= flpath.concat(File.separator).concat(filename);
		    File f = new File(pathname);			
			final LttsFileWriter file = new LttsFileWriter(flpath, filenm2, "UTF-8");
			file.open();
			try{	
				String ret  =filename  +" "+f.length()  +" "+totalRecords;
				file.write(ret);
			}finally{
				file.close();
			}
		
		//通知数据子系统
//		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBack(input.getFilesq(), input.getTrandt(), filetab.getUpfena(), filetab.getUpfeph());
		bizlog.method(">>>>>>>>>>>>>>>>dubbo外调开始>>>>>>>>>>>>>>>>>>>>>>>");
		//IoApFileBatch ioApFile =   SysUtil.getInstanceProxyByBind(IoApFileBatch.class,"glfewt");
		//ioApFile.doBatchSubmitBack(input.getFilesq(), input.getTrandt(), filetab.getUpfena(), filetab.getUpfeph());
		bizlog.method(">>>>>>>>>>>>>>>>dubbo外调结束>>>>>>>>>>>>>>>>>>>>>>>");
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Glfewt.Property property,
			Throwable t) {
		
		//监控预警平台
		KnpParameter para = KnpParameterDao.selectOne_odb1("DAYENDNOTICE", "%", "%", "%", true);
		
		String bdid = para.getParm_value1();// 服务绑定ID
		
		String mssdid = CommTools.getMySysId();// 随机生成消息ID
		
		String mesdna = para.getParm_value2();// 媒介名称
		
//		E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介//rambo delete
		
		IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
		
		IoCaOtherService.IoCaDayEndFailNotice.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaDayEndFailNotice.InputSetter.class);
		
		String timetm = DateTools2.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS000");
		IoCaBatchWarnInfo content = SysUtil.getInstance(IoCaBatchWarnInfo.class);
		content.setPljioyma("glfewt");
		content.setPljyzbsh("50010");
		content.setPljyzwmc("总分核对供数写文件");
		content.setErrmsg("总分核对供数写文件异常");
		content.setTrantm(timetm);
		
		// 发送消息
		mqInput.setMsgid(mssdid); // 消息ID
//		mqInput.setMedium(mssdtp); // 消息媒介//rambo delete
		mqInput.setMdname(mesdna); // 媒介名称
		mqInput.setTypeCode("NAS");
		mqInput.setTypeName("网络金融核心平台-电子账户核心系统");
		mqInput.setItemId("NAS_BATCH_WARN");
		mqInput.setItemName("电子账户核心批量执行错误预警");
		String str =JSON.toJSONString(content);
		mqInput.setContent(str);
		
		mqInput.setWarnTime(timetm);
		
		caOtherService.dayEndFailNotice(mqInput);
		
		//throw ExceptionUtil.wrapThrow(t);
	}
}

