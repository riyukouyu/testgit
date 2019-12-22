package cn.sunline.ltts.busi.dptran.batchfile.redpck;

import java.io.File;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.base.util.security.encrypt.MD5EncryptUtil;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BatchFileSubmit;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.FileBatchTools;
import cn.sunline.ltts.busi.dp.namedsql.redpck.RpBatchTransDao;
import cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.rpbackwt.Header;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatch;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
	 /**
	  * 红包批量退款翻盘文件
	  *
	  */

public class  rpbackwtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Input,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Property,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.rpbackwt.Header,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.rpbackwt.Body,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.rpbackwt.Foot,cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach>{
	
	private static BizLog log = BizLogUtil.getBizLog(rpbackwtWriteFileProcessor.class); 
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Property property){

		filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		
		//knp_bach tblKnp_bach = Knp_bachDao.selectOne_odb1(input.getFilesq(), filetab.getAcctdt(), true);
//		String filena = "NAS" + "_" + filetab.getFiletp() + "_" + CommTools.getBaseRunEnvs().getTrxn_date() + "_" + BusiTools.getSequence("rpseq", 8) + ".txt" ;
		
		
/*		String filepath = filetab.getDownph();
		log.debug("反盘文件路径：" + filepath);
		filetab.setUpfeph(filepath);
		filetab.setUpfena(filena);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);*/
		log.debug("反盘文件路径：" + filetab.getUpfeph());
		
		String pathname = filetab.getUpfeph() + filetab.getUpfena();
		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.rpbackwt.Header getHeader(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Property property){
		long fail = 0L;
		long succ = 0L;
		long dist = 0L;
		List<Map<String,Object>> lst = RpBatchTransDao.selGroupCountByFilesq(input.getFilesq(),filetab.getAcctdt(), false);
		for(int i = 0; i < lst.size(); i++){
			if(lst.get(i).get("transt") == E_TRANST.SUCCESS){
				succ = ConvertUtil.toLong(lst.get(i).get("record"));
			}
			if(lst.get(i).get("transt") == E_TRANST.FAIL){
				fail = ConvertUtil.toLong(lst.get(i).get("record"));
			}
		}
		filetab.setSuccnm(succ);
		filetab.setFailnm(fail);
		dist = Long.parseLong(String.valueOf(lst.size()));
		filetab.setDistnm(dist);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		Header header = SysUtil.getInstance(Header.class);
		
		header.setSuccnm(filetab.getSuccnm());
		header.setDistnm(filetab.getDistnm());
		header.setFailnm(filetab.getFailnm());
		//header.setBtchno(filetab.getBtchno());
		header.setTotanm(filetab.getTotanm());
		
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
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach> getFileBodyDataWalker(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Property property){

		Params param = new Params();
	 
		param.put("filesq", input.getFilesq());
		param.put("sourdt", filetab.getAcctdt());
		param.put("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach>(
				RpBatchTransDao.namedsql_selBatchWriteData, param); 
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach dateItem , cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.rpbackwt.Body body, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Property property) {

	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.rpbackwt.Foot getFoot(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Property property) {
		
	/*	filetab.setBtfest(E_BTFEST.RESTING);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);*/
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Property property, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Property property){
		log.debug("***********************红包批量退款翻盘文件处理后外调营销系统通知开始********************************");
		filetab.setBtfest(E_BTFEST.SUCC);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		KnpParameter para = KnpParameterDao.selectOne_odb1("Batch.File", "%", "%", "%", true);
		DefaultOptions<BatchFileSubmit> ls = new DefaultOptions<BatchFileSubmit>();
		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
		batch.setFilenm(filetab.getUpfena());
		batch.setFlpath(FileBatchTools.subPathByString(para.getParm_value1(), filetab.getUpfeph()));
		String pathname = filetab.getUpfeph() + filetab.getUpfena();
		batch.setFilemd(MD5EncryptUtil.getFileMD5String(new File(pathname)));
		ls.add(batch);
		E_SYSCCD target = E_SYSCCD.CMP;
		String status = E_FILEST.SUCC.getValue();
		String descri = "未领取红包退款反盘文件生产通知";
		/*if(E_FILETP.DP9I1004.getValue().equals(filetab.getFiletp())){
			   descri = "活动剩余红包退款 反盘文件生产通知";
		}*/
		
		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBackNotice(
				status, descri, target, filetab.getFiletp(), ls);
		log.debug("***********************红包批量退款翻盘文件处理后外调营销系统通知结束********************************");
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpbackwt.Property property,
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

