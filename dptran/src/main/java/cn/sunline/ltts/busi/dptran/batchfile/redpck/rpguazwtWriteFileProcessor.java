package cn.sunline.ltts.busi.dptran.batchfile.redpck;

import java.util.List;
import java.util.Map;

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
import cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.rpguazwt.Header;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;

  /**
   * 
   * @ClassName: rpguazwtWriteFileProcessor 
   * @Description: 红包批量挂账反盘文件 
   * @author huangzhikai
   * @date 2016年7月7日 上午10:31:08 
   *
   */

public class  rpguazwtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Input,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Property,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.rpguazwt.Header,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.rpguazwt.Body,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.rpguazwt.Foot,cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach>{
	
	private static BizLog log = BizLogUtil.getBizLog(rpbackwtWriteFileProcessor.class); 
	private kapb_wjplxxb tblkapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	
	/**
	 * 
	 * @Title: getFileName 
	 * @Description: 获取生成文件的文件名(含路径) 
	 * @param input
	 * @param property
	 * @return String
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:31:34 
	 * @version V2.3.0
	 */
	public String getFileName(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Property property){
		
//		KnpParameter  tbl_KnpParameter = SysUtil.getInstance(KnpParameter.class); 
//		tbl_KnpParameter = KnpParameterDao.selectOne_odb1("DPTRAN", "RPPATH", "%", "%", true);
		
		
		tblkapbWjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		
		log.debug("反盘文件路径：" + tblkapbWjplxxb.getUpfeph());
		
		String pathname = tblkapbWjplxxb.getLocaph() + tblkapbWjplxxb.getUpfena();
		return pathname;
	}

	/**
	 * 
	 * @Title: getHeader 
	 * @Description: 获取文件头信息 
	 * @param input
	 * @param property
	 * @return
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:32:15 
	 * @version V2.3.0
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.rpguazwt.Header getHeader(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Property property){
		long succ = 0l;
		long fail = 0l;
		
		List<Map<String,Object>> lst = RpBatchTransDao.selGroupCountByFilesq(tblkapbWjplxxb.getBtchno(), tblkapbWjplxxb.getAcctdt(),false);
		for(int i = 0; i < lst.size(); i++){
			if(lst.get(i).get("transt") == E_TRANST.SUCCESS){
				succ = (long) lst.get(i).get("record");
			}
			if(lst.get(i).get("transt") == E_TRANST.FAIL){
				fail = (long) lst.get(i).get("record");
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
		return header;
	}
	
 	/**
	 * 
	 * @Title: getFileBodyDataWalker 
	 * @Description: 基于游标的文件数据遍历器 
	 * @param input
	 * @param property
	 * @return CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach>
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:33:38 
	 * @version V2.3.0
	 */
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach> getFileBodyDataWalker(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Property property){
		Params param = new Params();
		param.put("filesq", tblkapbWjplxxb.getBtchno());
		param.put("sourdt", tblkapbWjplxxb.getAcctdt());
		
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach>(RpBatchTransDao.namedsql_selBatchWriteData , param); 
	}

	 /**
	  * 
	  * @Title: bodyProcess 
	  * @Description: 写文件体的每条记录前提供回调处理 
	  * @param index
	  * @param dateItem
	  * @param body
	  * @param input
	  * @param property
	  * @author huangzhikai
	  * @date 2016年7月7日 上午10:47:25 
	  * @version V2.3.0
	  */
	public void bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach dateItem , cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.rpguazwt.Body body, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Property property) {

	}
	
	/**
	 * 
	 * @Title: getFoot 
	 * @Description: 返回文件尾信息
	 * @param input
	 * @param property
	 * @return cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.rpguazwt.Foot
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:48:06 
	 * @version V2.3.0
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.rpguazwt.Foot getFoot(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Property property){
		
		return null;
	}

	/**
	 * 
	 * @Title: uploadFile 
	 * @Description: 上传生成的批量文件
	 * @param input
	 * @param property
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:48:43 
	 * @version V2.3.0
	 */
	public void uploadFile(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Property property){

	}
	
	/**
	 * 
	 * @Title: beforeWriteFileTranProcess 
	 * @Description: 写文件前处理回调 
	 * @param taskId
	 * @param input
	 * @param property
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:49:19 
	 * @version V2.3.0
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Property property) {
		
		/*tblkapbWjplxxb.setBtfest(E_BTFEST.RESTING);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);*/
	}
	
	/**
	 * 
	 * @Title: writeHeaderExceptionProcess 
	 * @Description: 写文件头异常处理回调 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:49:57 
	 * @version V2.3.0
	 */
	 
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 
	 * @Title: writeBodyExceptionProcess 
	 * @Description: 写文件体(单笔)异常处理回调 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param dateItem
	 * @param t
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:50:22 
	 * @version V2.3.0
	 */
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Property property, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach dateItem,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 
	 * @Title: writeBodyExceptionProcess 
	 * @Description: 写文件体异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:50:40 
	 * @version V2.3.0
	 */
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 
	 * @Title: writeFootExceptionProcess 
	 * @Description: 写文件尾异常处理回调 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:51:01 
	 * @version V2.3.0
	 */
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 
	 * @Title: afterWriteFileTranProcess 
	 * @Description: 写文件交易结束后回调 
	 * @param taskId
	 * @param input
	 * @param property
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:51:24 
	 * @version V2.3.0
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Property property){
		
		tblkapbWjplxxb.setBtfest(E_BTFEST.RTNS);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		
	}
	
	/**
	 * 
	 * @Title: writeFileTranExceptionProcess 
	 * @Description: 写文件交易异常处理回调 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:51:40 
	 * @version V2.3.0
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazwt.Property property,
			Throwable t) {
		log.debug("<<===================批量提现交易失败后修改状态======================>>");
		DaoUtil.executeInNewTransation(new RunnableWithReturn<kapb_wjplxxb>() {
			@Override
			public kapb_wjplxxb execute() {
				tblkapbWjplxxb.setBtfest(E_BTFEST.FAIL);
				Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
				return null;
			}
		});
		log.debug("<<===================批量提现交易失败后修改状态结束======================>>");
		throw ExceptionUtil.wrapThrow(t);
	}

}

