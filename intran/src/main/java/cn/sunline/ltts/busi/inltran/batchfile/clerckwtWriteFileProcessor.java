package cn.sunline.ltts.busi.inltran.batchfile;


import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

	 /**
	  * 待清算账户核准写文件
	  *
	  */

public class  clerckwtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Input,cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Property,cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.clerckwt.Header,cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.clerckwt.Body,cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.clerckwt.Foot,cn.sunline.ltts.busi.in.type.InQueryTypes.InClerckInfo>{
	private static final BizLog bizlog = BizLogUtil.getBizLog(clerckwtWriteFileProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	/*	BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);

	String trandt = DateTools2.getDateInfo().getLastdt();
	String filename = "nas_knsacsqcler_"+trandt+".txt";
	String seqno = CommTools.genTransq();
	KnpParameter tbKnpParameter = KnpParameterDao.selectOne_odb1("INTRAN", "INCLERLIST", "%", "%", false);
	String path = tbKnpParameter.getParm_value1()+ E_FILETP.IN030800 + File.separator + CommTools.getBaseRunEnvs().getTrxn_date() + File.separator;	
*/
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Property property){
		
        bizlog.method(">>>>>>>>>>>>>>>>>>>>getFileName begin>>>>>>>>>>>>>>>>>>>");
		
		filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		String pathname = filetab.getLocaph()  + filetab.getUpfena();
		
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
	public cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.clerckwt.Header getHeader(cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Property property){
		
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
	public BatchDataWalker<cn.sunline.ltts.busi.in.type.InQueryTypes.InClerckInfo> getFileBodyDataWalker(cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Property property){
		String nextdt = DateTools2.getDateInfo().getSystdt();
		String lastdt = DateTools2.getDateInfo().getBflsdt();
		
		Params param = new Params();
		param.put("nextdt", nextdt);
		param.put("lastdt", lastdt);
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.in.type.InQueryTypes.InClerckInfo>(InDayEndSqlsDao.namedsql_selKnsAcsqClerInfo , param); 
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.in.type.InQueryTypes.InClerckInfo dateItem , cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.clerckwt.Body body, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Property property) {
		bizlog.method(">>>>>>>>>>>>Begin>>>>>>>>>>>>");
		bizlog.parm("input [%s]", input);
		bizlog.parm("property [%s]", property);	
		bizlog.method("<<<<<<<<<<<<End<<<<<<<<<<<<");
	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.clerckwt.Foot getFoot(cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Property property){
		
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Property property){
		
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Property property) {
		/*filetab.setBtfest(E_BTFEST.RESTING);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);*/
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Property property, cn.sunline.ltts.busi.in.type.InQueryTypes.InClerckInfo dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Property property){
/*		DefaultOptions<BatchFileSubmit> ls = new DefaultOptions<>();
		Map<String,String> map = new HashMap<String,String>();*/
		
		filetab.setBtfest(E_BTFEST.RTNS);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
/*		String flpath ="";//处理后的相对路径
		flpath = path.substring(tbKnpParameter.getParm_value1().length()-1);
		
		String md5 = ""; 	//MD5值
		try {
       	 md5 = MD5EncryptUtil.getFileMD5String(new File(path.concat(File.separator).concat(filename)));
		} catch (Exception e) {
			throw ApError.BusiAplt.E0042(filename);
		}
		
        map.put(ApBatchFileParams.BATCH_PMS_FILESQ, seqno);
        map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);
        
        batch.setFilenm(filename);
        batch.setFlpath(flpath);
        batch.setFilemd(md5);
        batch.setParams(JSON.toJSONString(map));
        ls.add(batch);
        
        E_FILETP filetp = E_FILETP.IN030800; 
		String status=E_FILEST.SUCC.getValue();
		String descri = "待清算账户核准";
		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBackNotice(status, descri, E_SYSCCD.IPP, filetp, ls);
		*/
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
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clerckwt.Property property,
			Throwable t) {
		
		DaoUtil.executeInNewTransation(new RunnableWithReturn<kapb_wjplxxb>() {
			@Override
			public kapb_wjplxxb execute() {
				filetab.setBtfest(E_BTFEST.FAIL);
				Kapb_wjplxxbDao.updateOne_odb1(filetab);
				return null;
			}
		});
		
		throw ExceptionUtil.wrapThrow(t);
		
	}

}

