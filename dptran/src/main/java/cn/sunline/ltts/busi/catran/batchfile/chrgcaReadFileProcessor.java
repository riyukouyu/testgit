package cn.sunline.ltts.busi.catran.batchfile;


import java.io.File;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.file.ReadFileProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

	 /**
	  * 批量更换绑定卡号
	  * 批量更换绑定卡号
	  *
	  */

public class chrgcaReadFileProcessor extends ReadFileProcessor<cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Input,cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Property,cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb,cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.chrgca.Body,cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.chrgca.Foot>{
	
	private static BizLog bizlog = BizLogUtil.getBizLog(rdopacReadFileProcessor.class);
	private kapb_wjplxxb tblkapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	
	/**
	  *  获取待处理文件（通常为下载）
	  *  
	  *  @return 文件路径 
	  **/
	public String downloadFile(cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Property property) {
		
		bizlog.debug("downloadFile begin===========================>>");
		
/*		KnpParameter tblknp_para = SysUtil.getInstance(KnpParameter.class);
		tblknp_para  = KnpParameterDao.selectOne_odb1("CATRAN", "CHRGCA", "%", "%", true);
		
		String filepath = tblknp_para.getParm_value1()+ E_FILETP.CA010600 + File.separator + CommTools.getBaseRunEnvs().getTrxn_date() + File.separator;
		
		property.setFilena("CORE_BWFMXJDZ_20171220_ALL_999999" + ".del");
		bizlog.debug("文件批次号:" + input.getFilesq());
		bizlog.debug("文件名称:" + property.getFilena());
		bizlog.debug("文件路径:" + filepath);
		bizlog.debug("文件类型:" + E_FILETP.CA010600.getLongName());*/
		
		//tblkapbWjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		
		String pathname = property.getDwpath() + File.separator + property.getFilena(); //文件名(包括路径)
		
		bizlog.debug("downloadFile end<<===========================");

		return pathname;
	}

    /**
	  *  解析文件头后转换为对应的javabean对象后，提供处理。
	  *  @parm header 文件头对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *
	  */
	public boolean headerProcess(cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb mapping ,cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Property property) {
				
		//解析文件头信息
		//设置总笔数，更新文件状态
		//tblkapbWjplxxb.setTotanm(mapping.getTotanm()); //总笔数
/*		tblkapbWjplxxb.setBtfest(E_BTFEST.PARSEING); //文件状态
				
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);*/
				
		bizlog.debug("headerProcess end<<===========================");
				
		return false;
	}
	
	 /**
	  *  解析文件体后转换为对应的javabean对象后，提供处理。
	  *  @parm body 文件体对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *  注：该方法将会分发到作业服务器进行并发执行，需要配置交易控制器中的执行模式为"3"
	  */
	public boolean bodyProcess(int index, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.chrgca.Body body , cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Property property) {
		
		String odcard = BusiTools.delDoubleQuotationMarks(body.getOdcard());
		String nwcard = BusiTools.delDoubleQuotationMarks(body.getNwcard());
		
		//根据旧卡号查询绑定卡信息表
		List<KnaCacd> tblKnaCacd = KnaCacdDao.selectAll_odb6(odcard, false);
		
		//更新绑定卡号
		if(CommUtil.isNotNull(tblKnaCacd) && tblKnaCacd.size() > 0){
				bizlog.debug("开始替换卡号：[" + odcard + "]更换为：[" + nwcard +"]");
				for(KnaCacd knacacd : tblKnaCacd){
					knacacd.setCdopac(nwcard); //设置新绑定卡号
					KnaCacdDao.update_odb6(knacacd); //更新数据库
				}
				
		}
 		return true;
	}
	/**
	 * 解析文件尾后转换为对应的javabean对象后，提供处理。
	 * 
	 * @param foot 文件尾对象
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return 后续是否执行自动入库操作。该返回值仅在配置了mapping属性后才有效。
	 */
	public boolean footProcess(cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.chrgca.Foot foot ,cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Property property) {
	 	
	 	return false;
	}
	
	/**
	 * 读文件交易前处理
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Property property) {}

	/**
	 * 文件头解析异常处理(包括文件的下载、文件的打开、文件头的解析)
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Property property, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	
	/**
	 * 文件体一个批次处理并入库后回调(调用时间与设置的事务提交间隔相关)
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param addSuccessCount
	 * @param addErrorCount
	 */
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Property property,
			String jobId, int addSuccessCount, int addErrorCount) {}

	/**
	 * 文件体单行记录解析异常处理器
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param line
	 * @param t
	 */
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Property property,
			String jobId, String line, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 文件体一个批次解析异常处理器
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param totalSuccessCount
	 * @param totalErrorCount
	 * @param t
	 */
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 读文件交易处理结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Property property) {
		
		tblkapbWjplxxb.setBtfest(E_BTFEST.SUCC);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		
/*		KnpParameter tblknp_para = SysUtil.getInstance(KnpParameter.class);
		tblknp_para  = KnpParameterDao.selectOne_odb1("CATRAN", "CHRGCA", "%", "%", true);
		
		String filepath = tblknp_para.getParm_value1()+ E_FILETP.CA010600 + File.separator + CommTools.getBaseRunEnvs().getTrxn_date() + File.separator;
		
//		String status=E_FILEST.SUCC.getValue();
//		String descri="处理成功";
		
		//将待发送系统转为数组类型
		String str = tblknp_para.getParm_value4();
		String[] strs = str.split(",");
		
		
		E_SYSCCD srocue = E_SYSCCD.NAS;//原系统
		String filepath1 = File.separator + E_FILETP.CA010600 + File.separator + CommTools.getBaseRunEnvs().getTrxn_date() + File.separator; //相对路径
		String pathname= property.getFilena();
		String md5 = ""; 	//MD5值
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		try {                                  
			 md5 = MD5EncryptUtil.getFileMD5String(new File(filepath.concat(File.separator).concat(pathname)));
		} catch (Exception e) {
			throw ApError.BusiAplt.E0042(pathname);
		}
		
//		Options<BatchFileSubmit> optSmt = new DefaultOptions<>();
//        Map<String,Object> map = new HashMap<String,Object>();
//        BatchFileSubmit smt = SysUtil.getInstance(BatchFileSubmit.class);
//        smt.setFilenm(property.getFilena());
//        smt.setFlpath(filepath1);
//        smt.setFilemd(md5);
//		map.put(ApBatchFileParams.BATCH_PMS_FILESQ, input.getFilesq());
//		map.put(ApBatchFileParams.BATCH_PMS_TRANDT, CommTools.getBaseRunEnvs().getTrxn_date());
//		smt.setParams(JSON.toJSONString(map));			
//		optSmt.add(smt); 
		
		DefaultOptions<targetList> ls =new DefaultOptions<targetList>();
		//循环发送消息
		for(int i = 0;i < strs.length; i++){
			E_SYSCCD target = E_SYSCCD.valueOf(strs[i].toUpperCase(Locale.ENGLISH));//字符串转枚举，不区分大小写
			
			targetList targets = SysUtil.getInstance(targetList.class);
			targets.setTarget(target);
			ls.add(targets);
//			SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBackNotice(status, descri, target, E_FILETP.CA010600, optSmt);
		}
		
		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBackSynNotice(srocue, trandt, E_FILETP.CA010600, pathname, md5, ls);*/
	}

	/**
	 * 文件体一个批次解析结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param totalSuccessCount
	 * @param totalErrorCount
	 */
	public void afterBodyResolveProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount) {}

	/**
	 * 读文件交易处理异常后回调(交易的所有异常都会进入到该方法，包括之前已处理过并继续抛出的异常)
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Chrgca.Property property,
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

