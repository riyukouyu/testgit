package cn.sunline.ltts.busi.intran.batchfile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.DateTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.base.util.security.encrypt.MD5EncryptUtil;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BatchFileSubmit;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.type.InQueryTypes.SupplyDetail;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatch;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaBatchWarnInfo;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLERST;

	 /**
	  * 清算头寸汇总写文件
	  *
	  */

public class  supplywtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Input,cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Property,cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.supplywt.Header,cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.supplywt.Body,cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.supplywt.Foot,cn.sunline.ltts.busi.in.type.InQueryTypes.SupplyDetail>{
	
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	private static BizLog log = BizLogUtil.getBizLog(supplywtWriteFileProcessor.class);
	private String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
	private String fileno = BusiTools.getSequence("fileseq", 6);
	
	KnpParameter para = KnpParameterDao.selectOne_odb1("InParm.supply", "in", "%", "%", true);
	String seqno = BusiTools.getSequence("fileseq", 5);
	String filesq = trandt.concat(CommUtil.lpad(seqno, 12, "0"));
	String path = para.getParm_value1(); //文件存入根目录
    String path2 = File.separator.concat("030700").concat(File.separator).concat(trandt).concat(File.separator);
	String filename = "NAS" + "_" +"030700"+ "_".concat(trandt).concat(CommUtil.lpad(String.valueOf(seqno), 4, "0")).concat(".txt");
	String filepath = path.concat("030700").concat(File.separator).concat(trandt).concat(File.separator);

	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Property property){
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		
		property.setTrandt(trandt);
		
		System.out.println("文件路径：" + filepath);
		System.out.println("文件名称：" + filename);
		System.out.println("批次号:" + filesq);
		String pathname = filepath.concat(File.separator).concat(filename);
		
		//将文件路径信息插入文件批量信息表
		filetab.setBusseq(filesq);
		filetab.setBtfest(E_BTFEST.DOWNSUCC);
		filetab.setDownph(filepath);
	    filetab.setFiletp(E_FILETP.IN030700);
		filetab.setDownna(filename);
		filetab.setUpfeph(filepath);
		filetab.setBtchno(filesq);
		filetab.setUpfena(filename);
		Kapb_wjplxxbDao.insert(filetab);
		
		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.supplywt.Header getHeader(cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Property property){
//		long fail = 0L;
//		Long count = InacSqlsDao.selSupplyCount(property.getTrandt(), E_CLERST.WAIT, false);
//		
//		filetab.setSuccnm(count);
//		filetab.setFailnm(fail);
//		Kapb_wjplxxbDao.updateOne_odb1(filetab);
//		
//		Header header = SysUtil.getInstance(Header.class);
//		
//		header.setChenggbs(count);
//		header.setChulizbs(count);
//		header.setShibaibs(fail);
//		header.setZongbish(count);
//		return header;
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
	public BatchDataWalker<cn.sunline.ltts.busi.in.type.InQueryTypes.SupplyDetail> getFileBodyDataWalker(cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Property property){
		Params param = new Params();
		param.put("clerst", E_CLERST.WAIT);
		
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.in.type.InQueryTypes.SupplyDetail>(InacSqlsDao.namedsql_selSupplyDetail , param);  
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.in.type.InQueryTypes.SupplyDetail dateItem , cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.supplywt.Body body, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Property property) {
//		1-	网络核心内定时清算
//		2-	网络核心柜面核心定时清算
//		3-	网络核心信用卡核心定时清算
//		4-	网络核心大额支付定时清算
//		5-	网络核心小额支付定时清算
//		6-	网络核心农信银定时清算
//		7-	网络核心跨行网银定时清算
//		8-	网络核心银联跨行贷方定时清算
//		9-	网络核心银联跨行借方定时清算
//		10-	网络核心中间业务定时清算
//		11-	银联在线代收        
		
		body.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//日期
		body.setTimetm(DateTools2.getCurrentLocalTime());//时间
		body.setMarkcd("0");//标志位默认送0
		if(body.getClactp().equals("01")){
			//网络柜面
			body.setClactp("2");
			
		}else if(body.getClactp().equals("02")){
			//中间业务
			body.setClactp("10");
			
		}else if(body.getClactp().equals("03")){
			//银联借记业务
			body.setClactp("9");
			
		}else if(body.getClactp().equals("04")){
			//银联贷记业务
			
			body.setClactp("8");
			
		}else if(body.getClactp().equals("05")){
			//大额支付系统
			body.setClactp("4");
			
		}else if(body.getClactp().equals("06")){
			//小额支付系统
			body.setClactp("5");
			
		}else if(body.getClactp().equals("07")){
			//农信银支付系统
			body.setClactp("6");
			
		}else if(body.getClactp().equals("08")){
			//跨行网银支付系统
			body.setClactp("7");
			
		}else if(body.getClactp().equals("09")){
			//信用卡
			body.setClactp("3");
			
		}else if(body.getClactp().equals("10")){
			//系统内
			body.setClactp("1");
			
		}else if(body.getClactp().equals("11")){
			//银联在线代收
			body.setClactp("11");
			
		}
		
		dateItem.setTrandt(property.getTrandt());
		dateItem.setTmstmp(DateTools.getCurrentTime());
	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.supplywt.Foot getFoot(cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Property property) {
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
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Property property, cn.sunline.ltts.busi.in.type.InQueryTypes.SupplyDetail dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Property property){

		filetab.setBtfest(E_BTFEST.RESTSUCC);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);

        E_SYSCCD target = E_SYSCCD.IFS;
		E_FILETP filetp = E_FILETP.IN030700; 
		
	    // 获取文件名
	    String md5 = ""; 	//MD5值	    
	    md5 = MD5EncryptUtil.getFileMD5String(new File(filepath.concat(filename)));
	    Map<String,String> map = new HashMap<String,String>();
	    
	    //渠道码‘NK’+ 头寸标志‘T’+日期‘161129’+6位顺序号
	    String bathid ="NK"+"T" +trandt.substring(2, 8)+fileno;//流水
	  
	    //记录交易笔数
	    long count = 0;
	    List<SupplyDetail> list = InacSqlsDao.selSupplyDetail(E_CLERST.WAIT, false);
	    if(CommUtil.isNotNull(list) && list.size()>0){
	    		count = list.size();
	    }
	    
	    String  BGR04CNT1 =String.valueOf(count);
	    
	    map.put("BGR04DATE1", trandt);
        map.put("BGR04BTNO1", bathid);
        map.put("BGR04CNT1", BGR04CNT1);
       
        BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
 
        batch.setFilenm(filename);
        batch.setFlpath(path2);
        batch.setFilemd(md5);
        batch.setParams(JSON.toJSONString(map));
       
        DefaultOptions<BatchFileSubmit> ls = new DefaultOptions();
        ls.add(batch);		
		
		String status=E_FILEST.SUCC.getValue();
		String descri = "清算头寸汇总";
		
		log.parm("-------------status[%s]", status);
		log.parm("-------------descri[%s]", descri);
		log.parm("-------------target[%s]", target);
		log.parm("-------------filetp[%s]", filetp);
		log.parm("-------------ls-filename[%s]", filename);
		log.parm("-------------ls-flpath[%s]", path2);
		log.parm("-------------ls-filemd[%s]", md5);		
		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBackNotice(status, descri, target, filetp, ls);
		
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Supplywt.Property property,
			Throwable t) {
		//监控预警平台
		KnpParameter para = KnpParameterDao.selectOne_odb1("DAYENDNOTICE", "%", "%", "%", true);
		
		String bdid = para.getParm_value1();// 服务绑定ID
		
		String mssdid = CommTools.getMySysId();// 随机生成消息ID
		
		String mesdna = para.getParm_value2();// 媒介名称
		
//		E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介//rambo
		
		IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
		
		IoCaOtherService.IoCaDayEndFailNotice.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaDayEndFailNotice.InputSetter.class);
		
		String timetm = DateTools2.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS000");
		IoCaBatchWarnInfo content = SysUtil.getInstance(IoCaBatchWarnInfo.class);
		content.setPljioyma("supplywt");
		content.setPljyzbsh("0307");
		content.setPljyzwmc("清算头寸写文件异常预警");
		content.setErrmsg("清算头寸写文件失败");
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
	}

}

