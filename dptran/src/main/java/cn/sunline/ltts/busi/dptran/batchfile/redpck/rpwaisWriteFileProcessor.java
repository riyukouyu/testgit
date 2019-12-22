package cn.sunline.ltts.busi.dptran.batchfile.redpck;


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
import cn.sunline.ltts.busi.dp.namedsql.redpck.RpBatchTransDao;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

	 /**
	  * 可入账账户信息同步反盘文件
	  *
	  */

public class  rpwaisWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Input,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Property,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.rpwais.Header,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.rpwais.Body,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.rpwais.Foot,cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrCbai>{
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(rpwaisWriteFileProcessor.class);
	private kapb_wjplxxb tblkapbwjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Property property){
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>>>>getFileName begin>>>>>>>>>>>>>>>>>>>>>>");
		
		tblkapbwjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		String pathname = tblkapbwjplxxb.getLocaph() + tblkapbwjplxxb.getUpfena();
		
		bizlog.debug("pathname = [%s]", pathname);
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>>>>getFileName end>>>>>>>>>>>>>>>>>>>>>>");
		
		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.rpwais.Header getHeader(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Property property){
//		long fail = 0L;
//		long succ = 0L;
//		List<Map<String,Object>> lst = RpBatchTransDao.selGroupCountByFilesq(input.getFilesq(),input.getTrandt(), false);
//		for(int i = 0; i < lst.size(); i++){
//			if(lst.get(i).get("transt") == E_TRANST.SUCCESS){
//				succ = ConvertUtil.toLong(lst.get(i).get("record"));
//			}
//			if(lst.get(i).get("transt") == E_TRANST.FAIL){
//				fail = ConvertUtil.toLong(lst.get(i).get("record"));
//			}
//		}
//		tblkapbwjplxxb.setSuccnm(succ);
//		tblkapbwjplxxb.setFailnm(fail);
//		Kapb_wjplxxbDao.updateOne_odb1(tblkapbwjplxxb);
//		Header header = SysUtil.getInstance(Header.class);
//		
//		header.setChenggbs(tblkapbwjplxxb.getSuccnm());
//		header.setChulizbs(tblkapbwjplxxb.getDistnm());
//		header.setShibaibs(tblkapbwjplxxb.getFailnm());
//		header.setTaskid(tblkapbwjplxxb.getBtchno());
//		header.setZongbish(tblkapbwjplxxb.getTotanm());
		
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
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrCbai> getFileBodyDataWalker(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Property property){

		bizlog.debug(">>>>>>>>>>>>>>>>>>>>>>BatchDataWalker begin>>>>>>>>>>>>>>>>>>>>");
		
		Params parm = new Params();
		
		parm.add("filesq", input.getFilesq()); //批次号
		parm.add("trandt", tblkapbwjplxxb.getAcctdt()); //交易日期
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>>>>>BatchDataWalker end>>>>>>>>>>>>>>>>>>>");
		
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrCbai>(RpBatchTransDao.namedsql_selAccInfoByFilesq , parm); 
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrCbai dateItem , cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.rpwais.Body body, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Property property) {

	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.rpwais.Foot getFoot(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Property property) {
		
		bizlog.debug(">>>>>>>>>>>>>>>>beforeWriteFileTranProcess begin>>>>>>>>>>>>>>>>>");
/*		
		tblkapbwjplxxb.setBtfest(E_BTFEST.RESTING);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbwjplxxb);*/
		
		bizlog.debug(">>>>>>>>>>>>>>>beforeWriteFileTranProcess end>>>>>>>>>>>>>>>>>");
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Property property, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrCbai dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Property property){
	
		tblkapbwjplxxb.setBtfest(E_BTFEST.RTNS);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbwjplxxb);
		
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpwais.Property property,
			Throwable t) {

		DaoUtil.executeInNewTransation(new RunnableWithReturn<kapb_wjplxxb>() {
			@Override
			public kapb_wjplxxb execute() {
				tblkapbwjplxxb.setBtfest(E_BTFEST.FAIL);
				Kapb_wjplxxbDao.updateOne_odb1(tblkapbwjplxxb);
				return null;
			}
		});
		
		throw ExceptionUtil.wrapThrow(t);
		
	}

}

