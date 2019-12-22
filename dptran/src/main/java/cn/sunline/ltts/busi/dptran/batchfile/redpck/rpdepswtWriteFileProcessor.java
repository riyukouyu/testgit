package cn.sunline.ltts.busi.dptran.batchfile.redpck;

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
import cn.sunline.ltts.busi.dp.namedsql.redpck.RpBatchTransDao;
import cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.postalwt.Header;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
/**
 * 红包文件批量交易写文件
 * 
 */

public class rpdepswtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Property, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.postalwt.Header, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.postalwt.Body, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.postalwt.Foot, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach> {
	
			
	private static BizLog log = BizLogUtil.getBizLog(rpdepsrdReadFileProcessor.class); 
	private kapb_wjplxxb tblkapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
			/**
	 * 获取生成文件的文件名(含路径)
	 * 
	 * @param input
	 *            批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Input input,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Property property) {
		
		if(CommUtil.isNull(input.getFilesq())){
	       	 throw DpModuleError.DpstComm.E9999("文件批次号不能为空！");
	    }		
		/*KnpParameter  tbl_KnpParameter = SysUtil.getInstance(KnpParameter.class); 
		tbl_KnpParameter = KnpParameterDao.selectOne_odb1("DPTRAN", "RPPATH", "%", "%", true);*/
		
		tblkapbWjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		String filepath = tblkapbWjplxxb.getDownph();
		log.debug("反盘文件路径：" + filepath);
		
		String pathname = tblkapbWjplxxb.getLocaph() + tblkapbWjplxxb.getUpfena();
		return pathname;
	}

	/**
	 * @Title: getHeader 
	 * @Description: 获取文件头信息 
	 * @param input
	 * @param property
	 * @return
	 * @author wuzhixiang
	 * @date 2016年9月12日 上午10:32:15 
	 * @version V2.3.0
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.postalwt.Header getHeader(
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Input input,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Property property) {
		
		
		long fail = 0L;
		long succ = 0L;
		List<Map<String,Object>> lst = RpBatchTransDao.selGroupCountByFilesq(input.getFilesq(),tblkapbWjplxxb.getAcctdt(), false);
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
		//header.setTaskid(filetab.getBtchno());
		header.setZongbish(tblkapbWjplxxb.getTotanm());
		
		return header;
	}

	/**
	 * 基于游标的文件数据遍历器 返回文件体数据遍历器
	 * 
	 * @param input
	 *            批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return 注：写文件体支持并发查数据库和写文件，最后合并，所以如果需要有顺序的需自带排序功能
	 */
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach> getFileBodyDataWalker(
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Input input,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Property property) {

		Params param = new Params();
		param.put("filesq", input.getFilesq());
		param.put("sourdt", tblkapbWjplxxb.getAcctdt());
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach>(
				RpBatchTransDao.namedsql_selBatchWriteData, param);
	}
	/**
	 * 写文件体的每条记录前提供回调处理
	 * 
	 * @param index
	 *            序号，从1开始
	 * @param body
	 *            文件体对象
	 * @param input
	 *            批量交易的输入接口
	 * @param property批量交易的属性接口
	 * 
	 */
	public void bodyProcess(
			int index,
			cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach dateItem,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.postalwt.Body body,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Input input,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Property property) {

	}

	/**
	 * 返回文件尾信息
	 * 
	 * @param input
	 *            批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.postalwt.Foot getFoot(
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Input input,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Property property) {
		// TODO
		return null;
	}

	/**
	 * 上传生成的批量文件
	 * 
	 * @param input
	 *            批量交易的输入接口
	 * @param property
	 *            批量交易的属性接口
	 */
	public void uploadFile(
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Input input,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Property property) {
		// TODO
	}

	/**
	 * 写文件前处理回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(
			String taskId,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Input input,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Property property) {
		
/*		tblkapbWjplxxb.setBtfest(E_BTFEST.RESTING);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);*/
		
	}

	/**
	 * 写文件头异常处理回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(
			String taskId,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Input input,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件体(单笔)异常处理回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param dataItem
	 * @param t
	 */
	public void writeBodyExceptionProcess(
			String taskId,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Input input,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Property property,
			cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach dateItem,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件体异常处理回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeBodyExceptionProcess(
			String taskId,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Input input,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件尾异常处理回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFootExceptionProcess(
			String taskId,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Input input,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(
			String taskId,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Input input,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Property property) {
		
		tblkapbWjplxxb.setBtfest(E_BTFEST.SUCC);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		
	}

	/**
	 * 写文件交易异常处理回调
	 * 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(
			String taskId,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Input input,
			cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpdepswt.Property property,
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
