package cn.sunline.ltts.busi.inltran.batchfile;

import java.io.File;
import java.util.List;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoKnsProdClerInfo;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;

	 /**
	  * 总账计提利息汇总明细
	  *
	  */

public class  cbtlwtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Input,Cbtlwt.Property,cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.cbtlwt.Header,cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.cbtlwt.Body,cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.cbtlwt.Foot,cn.sunline.ltts.busi.in.tables.In.KnbCbtlBatch>{
	private static final BizLog bizlog = BizLogUtil.getBizLog(cbtlwtWriteFileProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	private String tblsrc = "knb_cbtl_batch";// 表名
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
	public String getFileName(cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Property property){
		
		String filename = "knb_cbtl_batch_"+"AAAAA_"+trandt+"_"+datatp+"_NAS"+".txt";
		//String filePath =  KnpParameterDao.selectOne_odb1("NOTIDFS", "dpfile","01", "%",true).getParm_value1() ;
		String filePath = KnpParameterDao.selectOne_odb1("DATAPRO", datatp, "%", "%", true).getParm_value2();

		String path = filePath+File.separator+trandt;
		String seqno = CommTools.getBaseRunEnvs().getTrxn_seq(); 
		
        bizlog.method(">>>>>>>>>>>>>>>>>>>>getFileName begin>>>>>>>>>>>>>>>>>>>");
		
		filetab.setUpfena(filename);
		filetab.setUpfeph(path);
		filetab.setBtchno(seqno);
		filetab.setFiletp(E_FILETP.IN030200);
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
	public cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.cbtlwt.Header getHeader(cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Property property){
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
	public BatchDataWalker<cn.sunline.ltts.busi.in.tables.In.KnbCbtlBatch> getFileBodyDataWalker(cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Property property){
		String trandt = DateTools2.getDateInfo().getLastdt();
		Params param = new Params();
		param.put("filesq", trandt+"0001");
		param.put("trandt", trandt);
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.in.tables.In.KnbCbtlBatch>(InDayEndSqlsDao.namedsql_selcbtlWriteDate , param); 
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.in.tables.In.KnbCbtlBatch dateItem , cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.cbtlwt.Body body, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Property property) {
		totalRecords = index ;
		bizlog.method(">>>>>>>>>>>>Begin>>>>>>>>>>>>");
		bizlog.parm("input [%s]", input);
		bizlog.parm("property [%s]", property);

		body.setOpbrch(String.valueOf(index));//设置行数
		List<IoKnsProdClerInfo> cplProdCler = SysUtil.getInstance(IoAccountSvcType.class).selKnsProdClerInfo(dateItem.getCorpno(),body.getProdcd(), body.getTrantype());//rambo add dateItem.getCorpno()
		
		if(CommUtil.isNull(cplProdCler)||cplProdCler.size()==0){
			
			throw InError.comm.E0003("产品编码["+body.getProdcd()+"]交易信息["+body.getTrantype()+"]总账系统未发布核算规则，导数失败！");
		}   	
		bizlog.method("<<<<<<<<<<<<End<<<<<<<<<<<<");		

	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.cbtlwt.Foot getFoot(cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Property property) {
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
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Property property, cn.sunline.ltts.busi.in.tables.In.KnbCbtlBatch dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Property property){
		
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
		//IoApFileBatch ioApFile =   SysUtil.getInstanceProxyByBind(IoApFileBatch.class,"cbtlwt");
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
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Cbtlwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

}

