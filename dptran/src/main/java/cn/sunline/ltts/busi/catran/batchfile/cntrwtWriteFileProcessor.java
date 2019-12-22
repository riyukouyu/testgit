package cn.sunline.ltts.busi.catran.batchfile;

import java.io.File;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Knp_bachDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.knp_bach;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.namedsql.CaBatchTransDao;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCuad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCuadDao;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatch;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FLBTST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

	 /**
	  * 存款产品合约库写文件
	  *
	  */

public class  cntrwtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Input,cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Property,cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.cntrwt.Header,cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.cntrwt.Body,cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.cntrwt.Foot,cn.sunline.ltts.busi.ca.type.CaCustInfo.CntrwtDetail>{
	
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	private static BizLog log = BizLogUtil.getBizLog(cntrwtWriteFileProcessor.class);
	
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Property property){
		
		long fail = 0L;
		Long count = CaBatchTransDao.selGroupCountSign(property.getTrandt(), E_SIGNST.QY, E_SIGNST.JY, false);
		
		if(CommUtil.compare(count, 0L) == 0){ //抛出文件内容为空的错误，并在后处理中捕获
			//throw ApError.BusiAplt.E0050();
		}
		
		
		filetab.setSuccnm(count);
		filetab.setFailnm(fail);
		
		
		
		//String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String trandt = CommTools.getBaseRunEnvs().getLast_date();
		KnpParameter  tbl_KnpParameter = SysUtil.getInstance(KnpParameter.class); 
		tbl_KnpParameter = KnpParameterDao.selectOne_odb1("DPTRAN", "RPPATH", "%", "%", true);
		
		String filepath = tbl_KnpParameter.getParm_value2()+ E_FILETP.CA011003 + File.separator + CommTools.getBaseRunEnvs().getTrxn_date() + File.separator;
		String filename = "DEPOSIT_CONTRACT_".concat(trandt).concat(".TXT");
		String seqno = BusiTools.getSequence("fileseq", 5);
		String filesq = trandt.concat(CommUtil.lpad(seqno, 12, "0"));
		
		System.out.println("文件路径：" + filepath);
		System.out.println("文件名称：" + filename);
		System.out.println("批次号:" + filesq);
		String pathname = filepath.concat(filename);
		
		//将文件路径信息插入文件批量信息表
		filetab.setBusseq(filesq);
		filetab.setBtfest(E_BTFEST.DOWNSUCC);
		filetab.setDownph(filepath);
	    filetab.setFiletp(E_FILETP.CA011003);
		filetab.setDownna(filename);
		filetab.setUpfeph(filepath);
		filetab.setBtchno(filesq);
		filetab.setUpfena(filename);
		
		Kapb_wjplxxbDao.insert(filetab);
		
		knp_bach plr = SysUtil.getInstance(knp_bach.class);
		plr.setBusisq(filesq);
		plr.setDataid(E_FILETP.CA011003);
		plr.setDatast(E_FLBTST.ZZCL);
		plr.setFilesq(filesq);
		plr.setSource(E_SYSCCD.NAS);
		plr.setTarget(E_SYSCCD.CCL);
		plr.setTrandt(trandt);
		plr.setAcctdt(trandt);
		
		Knp_bachDao.insert(plr);
		
		property.setBusseq(filesq);
		property.setDwname(filename);
		property.setDwpath(filepath);
		property.setTrandt(trandt);
		property.setFilesq(filesq);
		
		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.cntrwt.Header getHeader(cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Property property){
//		long fail = 0L;
//		Long count = CaBatchTransDao.selGroupCountSign(property.getTrandt(), E_SIGNST.QY, E_SIGNST.JY, false);
//		
//		filetab.setSuccnm(count);
//		filetab.setFailnm(fail);
//		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
		//Header header = SysUtil.getInstance(Header.class);
		
//		header.setChenggbs(filetab.getSuccnm());
//		header.setChulizbs(filetab.getSuccnm());
//		header.setShibaibs(filetab.getFailnm());
//		header.setZongbish(filetab.getSuccnm());
		
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
	public BatchDataWalker<cn.sunline.ltts.busi.ca.type.CaCustInfo.CntrwtDetail> getFileBodyDataWalker(cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Property property){
		Params param = new Params();
		param.put("trandt", property.getTrandt());
		param.put("signst1", E_SIGNST.QY);
		param.put("signst2", E_SIGNST.JY);
		
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.ca.type.CaCustInfo.CntrwtDetail>(CaBatchTransDao.namedsql_selAllSignDetail, param);  
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
	@Override
	public void bodyProcess(int index, cn.sunline.ltts.busi.ca.type.CaCustInfo.CntrwtDetail dateItem , cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.cntrwt.Body body, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Property property) {
		IoCaKnaAccs accs = CaDao.selKnaAccsByAcctno(dateItem.getFxacct(), true);
		
		KnaCuad cuad = KnaCuadDao.selectOne_knaCuadOdx1(accs.getCustac(), true);
		
		body.setCustno(cuad.getCustno());
		body.setTrandt(property.getTrandt());
		body.setTranbr(dateItem.getBrchno());
		body.setServtp("NM");
		body.setFcflag(accs.getFcflag());
		
		log.debug("渠道:[%s],客户号:[%s],定活标志:[%s]",body.getServtp(), body.getCustno(), body.getFcflag().getLongName());
	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.cntrwt.Foot getFoot(cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Property property) {
		filetab.setBtfest(E_BTFEST.RESTING);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Property property, cn.sunline.ltts.busi.ca.type.CaCustInfo.CntrwtDetail dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Property property){
		
		filetab.setBtfest(E_BTFEST.RESTSUCC);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
//		if(CommUtil.compare(filetab.getSuccnm(), 0L) == 0){
//			ApSysBatchDao.updKnpBachStatus(E_FLBTST.CLCG, property.getFilesq(), property.getTrandt());
//			log.debug("<<============文件内容为空，交易结束============>>");
//			return;
//		}
		String status=E_FILEST.SUCC.getValue();
		String descri="处理成功";
		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBack(property.getBusseq(), property.getFilesq(), property.getTrandt(), filetab.getUpfena(), filetab.getUpfeph(), status, descri); 
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Cntrwt.Property property,
			Throwable t) {
		
//		if(t.getCause() instanceof Exception){
//			Exception lbe = (Exception) t.getCause();
//			if(CommUtil.equals(lbe.getCode(), ApError.BusiAplt.F_E0050)){
//				log.debug("<<============文件内容为空，交易结束============>>");
//			}
//		}else{
			//String status=E_FILEST.FAIL.getValue();
			//String descri=t.getMessage();
			//SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBack(property.getBusseq(),property.getFilesq(), property.getTrandt(), filetab.getUpfena(), filetab.getUpfeph(), status, descri); 
			
			throw ExceptionUtil.wrapThrow(t);
//		}
		
	}

}

