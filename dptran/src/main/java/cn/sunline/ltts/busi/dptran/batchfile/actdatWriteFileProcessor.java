package cn.sunline.ltts.busi.dptran.batchfile;

import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.type.CaCustInfo.UserOpenDatasIn;
import cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.actdat.Header;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

	 /**
	  * 营销活动写文件
	  *
	  */

public class  actdatWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Input,cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Property,cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.actdat.Header,
	cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.actdat.Body,
	cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.actdat.Foot,
	cn.sunline.ltts.busi.ca.type.CaCustInfo.DataOfActiid>{
	
	private static BizLog log = BizLogUtil.getBizLog(actdatWriteFileProcessor.class); 
	private kapb_wjplxxb kapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Property property){
		if(CommUtil.isNull(input.getFilesq())){
	       	 throw DpModuleError.DpstComm.E9999("文件批次号不能为空！");
	    }		
		
		Map<String,Object> map = JsonUtil.parse(kapbWjplxxb.getFiletx());
		property.setActiid(String.valueOf(map.get("actiid")));
		property.setCyendt(String.valueOf(map.get("cyendt")));
		log.debug("写文件路径：" + kapbWjplxxb.getUpfeph());
		log.debug("写文件名称：" + kapbWjplxxb.getUpfena());
		
		String pathname = kapbWjplxxb.getUpfeph() + kapbWjplxxb.getUpfena();
		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.actdat.Header getHeader(cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Property property){
		Header header = SysUtil.getInstance(Header.class);
		header.setActiid(property.getActiid());
		header.setTotals(property.getTotals());
		header.setCyendt(property.getCyendt());
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
	public BatchDataWalker<cn.sunline.ltts.busi.ca.type.CaCustInfo.DataOfActiid> getFileBodyDataWalker(cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Property property){
		Params parm = new Params();
		Map<String,Object> map = JsonUtil.parse(kapbWjplxxb.getFiletx());
		String startdt = String.valueOf(map.get("cystdt")).substring(0, 10).replace("-", "");
		String enddt = String.valueOf(map.get("cyendt")).substring(0, 10).replace("-", "");
		
		UserOpenDatasIn datas = SysUtil.getInstance(UserOpenDatasIn.class);
		datas.setCyendt(DateTools2.covDateToString(DateTools2.addDays(DateTools2.covStringToDate(enddt), -property.getKaihsj())));
		datas.setCystdt(DateTools2.covDateToString(DateTools2.addDays(DateTools2.covStringToDate(startdt), -property.getKaihsj())));
		datas.setBrchno(String.valueOf(map.get("brchno")));
		datas.setSyunum(Integer.parseInt(String.valueOf(map.get("syunum"))));
		
		parm.put("cystdt", datas.getCystdt());//周期开始时间
		parm.put("cyendt", datas.getCyendt() ); //周期结束时间
		parm.put("brchno", datas.getBrchno() );//机构号
		parm.put("syunum", datas.getSyunum());//活动剩余名额
		
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.ca.type.CaCustInfo.DataOfActiid>(EacctMainDao.namedsql_selCustacOpenDatas , parm); 
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.ca.type.CaCustInfo.DataOfActiid dateItem , cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.actdat.Body body, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Property property) {

	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.actdat.Foot getFoot(cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Property property) {
		kapbWjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		Map<String,Object> map = JsonUtil.parse(kapbWjplxxb.getFiletx()) ;
		property.setKaihsj(Integer.parseInt((String) map.get("kaihsj")));
		String startdt = String.valueOf(map.get("cystdt")).substring(0, 10).replace("-", "");
		String enddt = String.valueOf(map.get("cyendt")).substring(0, 10).replace("-", "");
	
        String cystdt = DateTools2.covDateToString(DateTools2.addDays(DateTools2.covStringToDate(startdt), -property.getKaihsj()));//周期开始时间
        String cyendt = DateTools2.covDateToString(DateTools2.addDays(DateTools2.covStringToDate(enddt), -property.getKaihsj())); //周期结束时间
		
		String brchno = (String) map.get("brchno");
		int syunum = Integer.parseInt((String) map.get("syunum")) ;
		int totals = EacctMainDao.selCountCustacOpenDatas(cystdt, cyendt, brchno, syunum, false);
		property.setTotals(totals);
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Property property, cn.sunline.ltts.busi.ca.type.CaCustInfo.DataOfActiid dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Property property){
		log.debug("**********************文件批量写文件后处理开始****************************");
		kapbWjplxxb.setBtfest(E_BTFEST.WAIT_REPORT);
		Kapb_wjplxxbDao.updateOne_odb1(kapbWjplxxb);
		log.debug("**********************文件批量写文件后处理结束****************************");
		
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Actdat.Property property,
			Throwable t) {
		
		DaoUtil.executeInNewTransation(new RunnableWithReturn<kapb_wjplxxb>() {
			@Override
			public kapb_wjplxxb execute() {
				kapbWjplxxb.setBtfest(E_BTFEST.FAIL);
				Kapb_wjplxxbDao.updateOne_odb1(kapbWjplxxb);
				return null;
			}
		});
		
		throw ExceptionUtil.wrapThrow(t);
	}

}

