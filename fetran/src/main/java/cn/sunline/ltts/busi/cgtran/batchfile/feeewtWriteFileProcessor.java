package cn.sunline.ltts.busi.cgtran.batchfile;


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
import cn.sunline.ltts.busi.cg.namedsql.charg.PBChargeRegisterDao;
import cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.feeewt.Header;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

	 /**
	  * 手续费写文件
	  * 1.准备参数（kapb_wjplxxb，ksys_jykzhq）
	  * 2.准备对应文件本地路径下的txt文件
	  * 3.执行交易
	  * 4.kapb_wjplxxb表中查看数据变动，txt文件查看写入的数据（交易结构）
	  */

public class  feeewtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Input,cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Property,cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.feeewt.Header,cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.feeewt.Body,cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.feeewt.Foot,cn.sunline.ltts.busi.iobus.type.cg.IoCgChrg.IoChargeAcctInfo>{
	private kapb_wjplxxb tblkapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Input input, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Property property){
/*		if(CommUtil.isNull(input.getAcctdt())){
	       	 throw DpModuleError.DpstComm.E9999("对账日期不能为空");
	        }
			
			if(CommUtil.isNull(input.getFilesq())){
		       	 throw DpModuleError.DpstComm.E9999("文件批次号不能为空");
		    }
			
			KnpParameter  tbl_KnpParameter = SysUtil.getInstance(KnpParameter.class); 
			tbl_KnpParameter = KnpParameterDao.selectOne_odb1("DPTRAN", "RPPATH", "%", "%", true);
			
			String filepath = tbl_KnpParameter.getParm_value2()+ E_FILETP.CG060100 + File.separator + CommTools.getBaseRunEnvs().getTrxn_date() + File.separator;
			String filename = property.getDwname();
			String filesq = input.getFilesq();
			System.out.println("文件路径：" + filepath);
			System.out.println("文件名称：" + filename);
			System.out.println("批次号:" + filesq);
			String pathname = filepath.concat(File.separator).concat(filename);*/
		
			tblkapbWjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
			String pathname = tblkapbWjplxxb.getLocaph() + tblkapbWjplxxb.getUpfena();
					
			return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.feeewt.Header getHeader(cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Input input, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Property property){
		long fail = 0L;
		Long count = PBChargeRegisterDao.selChargeAcctCount(tblkapbWjplxxb.getAcctdt(), false);
		
		tblkapbWjplxxb.setSuccnm(count);
		tblkapbWjplxxb.setFailnm(fail);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		
		Header header = SysUtil.getInstance(Header.class);
		
//		header.setChenggbs(count);
//		header.setChulizbs(count);
//		header.setShibaibs(fail);
//		header.setZongbish(count);
		
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
	public BatchDataWalker<cn.sunline.ltts.busi.iobus.type.cg.IoCgChrg.IoChargeAcctInfo> getFileBodyDataWalker(cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Input input, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Property property){
		Params param = new Params();
		param.put("acctdt", tblkapbWjplxxb.getAcctdt());
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.iobus.type.cg.IoCgChrg.IoChargeAcctInfo>(PBChargeRegisterDao.namedsql_selChargeAcctInfo , param); 
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.iobus.type.cg.IoCgChrg.IoChargeAcctInfo dateItem , cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.feeewt.Body body, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Input input, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Property property) {

	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.feeewt.Foot getFoot(cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Input input, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Property property){
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Input input, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Property property){
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Input input, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Property property) {
	/*	tblkapbWjplxxb.setBtfest(E_BTFEST.RESTING);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);*/
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Input input, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Input input, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Property property, cn.sunline.ltts.busi.iobus.type.cg.IoCgChrg.IoChargeAcctInfo dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Input input, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Input input, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Input input, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Property property){
		tblkapbWjplxxb.setBtfest(E_BTFEST.RTNS);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Input input, cn.sunline.ltts.busi.cgtran.batchfile.intf.Feeewt.Property property,
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

