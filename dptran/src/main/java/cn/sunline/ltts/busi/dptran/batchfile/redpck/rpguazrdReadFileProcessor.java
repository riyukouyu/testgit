package cn.sunline.ltts.busi.dptran.batchfile.redpck;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.file.ReadFileProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.dp.namedsql.redpck.RpBatchTransDao;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBachDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;

 /**
  * 
  * @ClassName: rpguazrdReadFileProcessor 
  * @Description: 红包批量挂账上送文件 
  * @author huangzhikai
  * @date 2016年7月7日 上午10:15:07 
  *
  */

public class rpguazrdReadFileProcessor extends ReadFileProcessor<cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Input,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Property,cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb,cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.rpguazrd.Foot>{
	
	private static BizLog log = BizLogUtil.getBizLog(rpguazrdReadFileProcessor.class);
	private kapb_wjplxxb tblkapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	/**
	 * 
	 * @Title: downloadFile 
	 * @Description: 获取待处理文件 
	 * @param input
	 * @param property
	 * @return String
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:16:45 
	 * @version V2.3.0
	 */
	public String downloadFile(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Property property) {
		
		tblkapbWjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		
		// 删除业务表该批次号下的数据
		KnbRptrBachDao.delete_odb2(input.getFilesq());
		
		String pathname = tblkapbWjplxxb.getDownph() + tblkapbWjplxxb.getDownna();
		log.debug("文件批次号:" + input.getFilesq() );
		log.debug("文件名称:" + property.getFilena());
		log.debug("文件路径:" + property.getDwpath());
		log.debug("文件类型" + E_FILETP.DP020100.getLongName());
		
		return pathname;
	}

	/**
	 * 
	 * @Title: headerProcess 
	 * @Description: 解析文件头后转换为对应的javabean对象后，提供处理 
	 * @param mapping
	 * @param input
	 * @param property
	 * @return boolean
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:19:08 
	 * @version V2.3.0
	 */
	public boolean headerProcess(cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb mapping ,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Property property) {
		
		log.debug("<<===========================>>");
		log.debug("文件头处理");
		log.debug("<<===========================>>");
		tblkapbWjplxxb.setTotanm(mapping.getTotanm()); //总笔数
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
	 	return false;
	}
	
	/**
	 * 
	 * @Title: bodyProcess 
	 * @Description: 解析文件体后转换为对应的javabean对象后，提供处理 
	 * @param index
	 * @param mapping
	 * @param input
	 * @param property
	 * @return boolean 
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:20:17 
	 * @version V2.3.0
	 */
	public boolean bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach mapping , cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Property property) {
		
		log.debug("<<=============文件体解析处理==============>>");

		String transq = MsSystemSeq.getTrxnSeq();
		mapping.setTransq(transq);
		mapping.setFilesq(input.getFilesq());
		mapping.setTranst(E_TRANST.WAIT);
		int groupCount = 100;
		mapping.setDataid(String.valueOf(index % groupCount));
		log.debug("<<=============文件体解析完成==============>>");
		return true;
	}
	
	/**
	 * 
	 * @Title: footProcess 
	 * @Description: 解析文件尾后转换为对应的javabean对象后，提供处理 
	 * @param foot
	 * @param input
	 * @param property
	 * @return boolean
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:21:01 
	 * @version V2.3.0
	 */
	public boolean footProcess(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.rpguazrd.Foot foot ,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Property property) {
	 	
		return false;
	}
	
	/**
	 * 
	 * @Title: beforeReadFileTranProcess 
	 * @Description: 读文件交易前处理 
	 * @param taskId
	 * @param input
	 * @param property
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:21:50 
	 * @version V2.3.0
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Property property) {
		
	}

	/**
	 * 
	 * @Title: headerResolveExceptionProcess 
	 * @Description: 文件头解析异常处理 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:22:34 
	 * @version V2.3.0
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Property property, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 
	 * @Title: afterBodyResolveCommitProcess 
	 * @Description: 文件体一个批次处理并入库后回调 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param addSuccessCount
	 * @param addErrorCount
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:23:00 
	 * @version V2.3.0
	 */
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Property property,
			String jobId, int addSuccessCount, int addErrorCount) {
		
	}
	
	/**
	 * 
	 * @Title: bodyResolveExceptionProcess 
	 * @Description: 文件体单行记录解析异常处理器 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param line
	 * @param t
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:23:24 
	 * @version V2.3.0
	 */
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Property property,
			String jobId, String line, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 
	 * @Title: bodyResolveExceptionProcess 
	 * @Description: 文件体一个批次解析异常处理器 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param totalSuccessCount
	 * @param totalErrorCount
	 * @param t
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:24:04 
	 * @version V2.3.0
	 */
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 
	 * @Title: afterReadFileTranProcess 
	 * @Description: 读文件交易处理结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:24:33 
	 * @version V2.3.0
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Property property) {
		
		log.debug("<<=============文件处理完成后处理==============>>");
		Long total = RpBatchTransDao.selCountByFilesq(input.getFilesq(), tblkapbWjplxxb.getAcctdt(),false);
		
		log.debug("文件记录数：" + tblkapbWjplxxb.getTotanm());
		log.debug("数据记录数：" + total);
		if(CommUtil.compare(tblkapbWjplxxb.getTotanm(), total) != 0){
			throw DpModuleError.DpstComm.E9999("文件记录数与总记录数不匹配");
		}
		
		tblkapbWjplxxb.setSuccnm(total);
		tblkapbWjplxxb.setBtfest(E_BTFEST.READ);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
	}

	/**
	 * 
	 * @Title: afterBodyResolveProcess 
	 * @Description: 文件体一个批次解析结束后回调 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param totalSuccessCount
	 * @param totalErrorCount
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:25:03 
	 * @version V2.3.0
	 */
	public void afterBodyResolveProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount) {
		
	}

	/**
	 * 
	 * @Title: readFileTranExceptionProcess 
	 * @Description: 读文件交易处理异常后回调 
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:27:03 
	 * @version V2.3.0
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rpguazrd.Property property,
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

