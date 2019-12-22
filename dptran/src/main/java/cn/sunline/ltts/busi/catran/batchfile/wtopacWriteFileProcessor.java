package cn.sunline.ltts.busi.catran.batchfile;

import java.io.File;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.namedsql.CaBatchTransDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpbt;
import cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.wtopac.Header;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatch;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

	 /**
	  * 电子账户批量开户回传文件
	  * 电子账户批量开户回传文件
	  *
	  */

public class  wtopacWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Input,cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Property,cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.wtopac.Header,cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.wtopac.Body,cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.wtopac.Foot,cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpbt>{
	
	private static BizLog bizlog = BizLogUtil.getBizLog(wtopacWriteFileProcessor.class); 
	private kapb_wjplxxb tblkapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Property property){
		bizlog.debug("getFileName begin=========================>>");
		
		
        String filena = "NAS" + "_" + E_FILETP.CA010100 + "_" + CommTools.getBaseRunEnvs().getTrxn_date() + "_" + BusiTools.getSequence("fileseq", 5) + ".txt" ;
		
        //获取文件批量信息
        tblkapbWjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		String filepath = tblkapbWjplxxb.getDownph();
		bizlog.debug("反盘文件路径：" + filepath);
		tblkapbWjplxxb.setUpfeph(filepath);
		tblkapbWjplxxb.setUpfena(filena);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		
		String pathname = tblkapbWjplxxb.getUpfeph() + File.separator + tblkapbWjplxxb.getUpfena();
		return pathname;
		
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.wtopac.Header getHeader(cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Property property){
		
		bizlog.debug("getHeader begin=========================>>");
		
		Header header = SysUtil.getInstance(Header.class);
		
		KnbOpbt tblKnbOpbt = CaBatchTransDao.selKnbOpbtFirstByBtchno(input.getFilesq(), input.getAcctdt(), false);
		
		if(CommUtil.isNotNull(tblKnbOpbt)){
			header.setAgntna(tblKnbOpbt.getAngtna()); //代理单位名称
			header.setAccatp(tblKnbOpbt.getAccttp()); //账户分类
			header.setBrchno(tblKnbOpbt.getBrchno()); //所属机构
			header.setServtp(tblKnbOpbt.getServtp()); //交易渠道
			header.setCounts(tblkapbWjplxxb.getTotanm()); //总笔数
		}
		
		bizlog.debug("getHeader end<<=========================");
		
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
	public BatchDataWalker<cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpbt> getFileBodyDataWalker(cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Property property){
		
		bizlog.debug("getFileBodyDataWalker begin=========================>>");
		
		Params parm = new Params();
		parm.put("btchno", input.getFilesq()); //当期批次号
		parm.put("acctdt", input.getAcctdt()); //批次日期
		
		bizlog.debug("getFileBodyDataWalker end<<=========================");
		
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpbt>(CaBatchTransDao.namedsql_selKnbOpbtAllByBtchno, parm); 
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpbt dateItem , cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.wtopac.Body body, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Property property) {

	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.wtopac.Foot getFoot(cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Property property) {
		tblkapbWjplxxb.setBtfest(E_BTFEST.RESTING);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Property property, cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpbt dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Property property){
		tblkapbWjplxxb.setBtfest(E_BTFEST.RESTING);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		
		String status=E_FILEST.SUCC.getValue();
		String descri="处理成功";
		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBack(input.getBusseq(),input.getFilesq(), input.getAcctdt(), tblkapbWjplxxb.getUpfena(), tblkapbWjplxxb.getUpfeph(), status, descri);
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Wtopac.Property property,
			Throwable t) {
		
		String status=E_FILEST.FAIL.getValue();
		String descri=t.getMessage();
		
		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBack(input.getBusseq(),input.getFilesq(), input.getAcctdt(), tblkapbWjplxxb.getUpfena(), tblkapbWjplxxb.getUpfeph(), status, descri);
		throw ExceptionUtil.wrapThrow(t);
	}

}

