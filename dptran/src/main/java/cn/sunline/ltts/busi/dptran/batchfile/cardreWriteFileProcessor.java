package cn.sunline.ltts.busi.dptran.batchfile;

import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.dp.namedsql.KqBatchDao;
import cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.cardre.Header;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;

	 /**
	  * 卡券批量记账反盘文件
	  *
	  */

public class  cardreWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Input,cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Property,cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.cardre.Header,
	cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.cardre.Body,
	cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.cardre.Foot,
	cn.sunline.ltts.busi.dp.tables.RpMgr.KnbKqjzBach>{
	
	private static BizLog log = BizLogUtil.getBizLog(cardreWriteFileProcessor.class); 
	private kapb_wjplxxb tblkapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Property property){
		if(CommUtil.isNull(input.getFilesq())){
	       	 throw DpModuleError.DpstComm.E9999("文件批次号不能为空！");
	    }		
		tblkapbWjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		
		log.debug("反盘文件路径：" + tblkapbWjplxxb.getUpfeph());
		log.debug("反盘文件名称：" + tblkapbWjplxxb.getUpfena());
		
		String pathname = tblkapbWjplxxb.getUpfeph() + tblkapbWjplxxb.getUpfena();
		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.cardre.Header getHeader(cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Property property){
	
		long fail = 0L;
		long succ = 0L;
		List<Map<String,Object>> lst = KqBatchDao.selGroupCountByFilesq(input.getFilesq(),tblkapbWjplxxb.getAcctdt(), false);
		for(int i = 0; i < lst.size(); i++){
			if(lst.get(i).get("transt") == E_TRANST.SUCCESS){
				succ = ConvertUtil.toLong(lst.get(i).get("record"));
			}
			if(lst.get(i).get("transt") == E_TRANST.FAIL){
				fail = ConvertUtil.toLong(lst.get(i).get("record"));
			}
		}
		
		tblkapbWjplxxb.setSuccnm(succ);
		tblkapbWjplxxb.setFailnm(fail);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		Header header = SysUtil.getInstance(Header.class);
		
		header.setChenggbs(tblkapbWjplxxb.getSuccnm());
		header.setChulizbs(tblkapbWjplxxb.getDistnm());
		header.setShibaibs(tblkapbWjplxxb.getFailnm());
		header.setZongbish(tblkapbWjplxxb.getTotanm());
		
		return  header;
	}
	
	/**
	 * 基于游标的文件数据遍历器
	 * 返回文件体数据遍历器
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return 
	 * 注：写文件体支持并发查数据库和写文件，最后合并，所以如果需要有顺序的需自带排序功能
	 */
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbKqjzBach> getFileBodyDataWalker(cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Property property){
		Params param = new Params();
		param.put("filesq", input.getFilesq());
		param.put("sourdt", tblkapbWjplxxb.getAcctdt());
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbKqjzBach>(
				KqBatchDao.namedsql_selBatchWriteData, param);
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbKqjzBach dateItem , cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.cardre.Body body, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Property property) {

	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.cardre.Foot getFoot(cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Property property) {
		
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Property property, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbKqjzBach dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Property property){
		
		tblkapbWjplxxb.setBtfest(E_BTFEST.WAIT_REPORT);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Cardre.Property property,
			Throwable t) {
		
		DaoUtil.executeInNewTransation(new RunnableWithReturn<kapb_wjplxxb>() {
			@Override
			public kapb_wjplxxb execute() {
				tblkapbWjplxxb.setBtfest(E_BTFEST.FAIL);
				Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
				return null;
			}
		});
		
		throw ExceptionUtil.wrapThrow(t);
	}

}

