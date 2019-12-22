package cn.sunline.ltts.busi.intran.batchfile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.base.util.security.encrypt.MD5EncryptUtil;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BatchFileSubmit;
import cn.sunline.ltts.busi.aplt.para.ApBatchFileParams;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.inltran.batchfile.tranckwtWriteFileProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatch;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;


	 /**
	  * 红包过渡户发生额额导出写文件
	  * 用于红包系统过渡户发生总额核对
	  *
	  */

public class  rpsbalwtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Input,cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Property,cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.rpsbalwt.Header,cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.rpsbalwt.Body,cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.rpsbalwt.Foot,cn.sunline.ltts.busi.in.type.InQueryTypes.InTotalTranamInfo>{
	private static final BizLog bizlog = BizLogUtil.getBizLog(tranckwtWriteFileProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
	
	String trandt = DateTools2.getDateInfo().getLastdt();
	String filename = "nas_"+E_FILETP.IN031000+"_"+trandt+".txt";
	
	String seqno = CommTools.getBaseRunEnvs().getTrxn_seq();
	
	KnpParameter tbKnpParameter = KnpParameterDao.selectOne_odb1("InParm.rpsbalwt", "path", "%", "%", true);
	String path = tbKnpParameter.getParm_value1()+ E_FILETP.IN031000+ File.separator + CommTools.getBaseRunEnvs().getTrxn_date() + File.separator;
	
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Property property){
		bizlog.method(">>>>>>>>>>>>>>>>>>>>getFileName begin>>>>>>>>>>>>>>>>>>>");

		//将文件路径信息插入文件批量信息表
		filetab.setBusseq(input.getBusseq());
		filetab.setDownph(path);
		filetab.setDownna(filename);
		filetab.setUpfena(filename);
		filetab.setUpfeph(path);
		filetab.setBtchno(seqno);
		filetab.setFiletp(E_FILETP.IN031000);
		filetab.setBtfest(E_BTFEST.GIVING);
		Kapb_wjplxxbDao.insert(filetab);
		
		filetab = Kapb_wjplxxbDao.selectOne_odb1(seqno, true);
		String pathname = filetab.getUpfeph() + File.separator + filetab.getUpfena();
		
		bizlog.parm("-------------pathname[%s]", pathname);
		bizlog.method(">>>>>>>>>>>>>>>>>>>getFileName end>>>>>>>>>>>>>>>>>>>>");
		
		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.rpsbalwt.Header getHeader(cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Property property){
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
	public BatchDataWalker<cn.sunline.ltts.busi.in.type.InQueryTypes.InTotalTranamInfo> getFileBodyDataWalker(cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Property property){
 		Params param = new Params();
		param.put("trandt", input.getAcctdt());
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.in.type.InQueryTypes.InTotalTranamInfo>(InDayEndSqlsDao.namedsql_selGlKnsGlvcRpsAcctnoTranam, param); 
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.in.type.InQueryTypes.InTotalTranamInfo dateItem , cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.rpsbalwt.Body body, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Property property) {

	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.rpsbalwt.Foot getFoot(cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Property property) {
		
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Property property, cn.sunline.ltts.busi.in.type.InQueryTypes.InTotalTranamInfo dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Property property){
		DefaultOptions<BatchFileSubmit> ls = new DefaultOptions<>();
		Map<String,String> map = new HashMap<String,String>();
		
		filetab.setBtfest(E_BTFEST.RESTSUCC);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
		String md5 = ""; 	//MD5值
		md5 = MD5EncryptUtil.getFileMD5String(new File(path.concat(File.separator).concat(filename)));
		
        map.put(ApBatchFileParams.BATCH_PMS_FILESQ, seqno);
        map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);
        
        batch.setFilenm(filename);
        batch.setFlpath(File.separator + E_FILETP.IN031000+ File.separator + CommTools.getBaseRunEnvs().getTrxn_date() + File.separator);
        batch.setFilemd(md5);
        batch.setParams(JSON.toJSONString(map));
        ls.add(batch);

        E_FILETP filetp = E_FILETP.IN031000; 
		String status=E_FILEST.SUCC.getValue();
		String descri = "红包过渡户发生额核对";
		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBackNotice(status, descri, E_SYSCCD.RPS, filetp, ls);		
		//通知数据子系统
		bizlog.method(">>>>>>>>>>>>>>>>dubbo外调开始>>>>>>>>>>>>>>>>>>>>>>>");
		bizlog.method(">>>>>>>>>>>>>>>>dubbo外调结束>>>>>>>>>>>>>>>>>>>>>>>");		
		
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Rpsbalwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

}

