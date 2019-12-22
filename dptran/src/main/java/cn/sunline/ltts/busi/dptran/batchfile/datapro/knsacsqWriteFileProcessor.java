package cn.sunline.ltts.busi.dptran.batchfile.datapro;

import java.io.File;
import java.util.Map;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.namedsql.ApSysBatchDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.edsp.base.lang.Params;

	 /**
	  * 会计流水表数仓抽数
	  *
	  */

public class  knsacsqWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Input,cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Property,cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.knsacsq.Header,cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.knsacsq.Body,cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.knsacsq.Foot,java.util.Map>{
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	private String tblsrc = "kns_acsq";// 表名
	KnpParameter  tbl = KnpParameterDao.selectOne_odb1(tblsrc, "%", "%", "%", true);
	private String datatp = tbl.getParm_value1();// 增全量标识
	private String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期
	private String lstdt  = CommTools.getBaseRunEnvs().getLast_date();// 上次交易日期
	private String filename = tblsrc+"_" + "AAAAA_" + lstdt + "_" + datatp + "_NAS" + ".txt";
	private int totalRecords;//总记录数
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Input input, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Property property){
		KnpParameter  tbl_KnpParameter = SysUtil.getInstance(KnpParameter.class); 
		tbl_KnpParameter = KnpParameterDao.selectOne_odb1("DATAPRO", datatp, "%", "%", true);
		//String filepath = tbl_KnpParameter.getParm_value2();
		String filepath = tbl_KnpParameter.getParm_value2()+File.separator+lstdt;//路径加上日期
		String pathname= filepath.concat(File.separator).concat(filename);
		String seqno = BusiTools.getSequence("fileseq", 5);
		String filesq = trandt.concat(CommUtil.lpad(seqno, 12, "0"));
		//将文件路径信息插入文件批量信息表
		filetab.setBusseq(input.getBusseq());
		filetab.setBtfest(E_BTFEST.DOWNSUCC);
		filetab.setDownph(filepath);
	    filetab.setFiletp(E_FILETP.DATAPRO);
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
	public cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.knsacsq.Header getHeader(cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Input input, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Property property){
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
	public BatchDataWalker<java.util.Map> getFileBodyDataWalker(cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Input input, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Property property){
		Params param = new Params();
		param.put("tblsrc", tblsrc);// 表名
		if(datatp.equals("add")){
			param.put("mtdate", lstdt);// 维护日期
		}
		
		return new CursorBatchDataWalker<Map>( ApSysBatchDao.namedsql_selTableInfo, param);
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
	public void bodyProcess(int index, java.util.Map dateItem , cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.knsacsq.Body body, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Input input, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Property property) {
		totalRecords = index ;
	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.knsacsq.Foot getFoot(cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Input input, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Input input, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Input input, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Property property) {
		
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Input input, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Input input, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Property property, java.util.Map dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Input input, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Input input, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Input input, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Property property){
		KnpParameter  tbl_KnpParameter = SysUtil.getInstance(KnpParameter.class);
		int total = totalRecords;
		tbl_KnpParameter = KnpParameterDao.selectOne_odb1("DATAPRO", datatp, "%", "%", true);
		   String filename = tblsrc+"_" + "AAAAA_" + lstdt + "_" + datatp + "_NAS" + ".txt";	
		    String filenm2 = tblsrc+"_" + "AAAAA_" + lstdt + "_" + datatp + "_NAS" + ".flg";   	  
		    String flpath = tbl_KnpParameter.getParm_value2()+File.separator+lstdt;//路径加上日期		 
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
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Input input, cn.sunline.ltts.busi.dptran.batchfile.datapro.intf.Knsacsq.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

}

