package cn.sunline.ltts.busi.dptran.batchfile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.base.util.security.encrypt.MD5EncryptUtil;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BatchFileSubmit;
import cn.sunline.ltts.busi.aplt.para.ApBatchFileParams;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatch;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;

	 /**
	  * 支付2.0对账文件
	  *
	  */

public class  payckawtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Input,cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Property,cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.paycka.Header,cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.paycka.Body,cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.paycka.Foot,cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPaycBatch>{
	

    private static final BizLog bizlog = BizLogUtil
            .getBizLog(nasodwtWriteFileProcessor.class);
    private static String trandt = CommTools.getBaseRunEnvs().getLast_date();  //交易日期
    //private static String corpno = BusiTools.getCenterCorpno();             //交易法人 
    // 存放目录更改
    private static String filePath = ""; // 文件路径
    private static String fileName = ""; // 文件名
   // private static String idx = ""; // 文件名后缀

    private static E_FILETP filetp = E_FILETP.DP939500;
    //private static String separators = "|@|";
    private static String filesq = "";// 统一生成一个流水号
    private static String md5 = ""; // MD5值
    DefaultOptions<BatchFileSubmit> ls = new DefaultOptions<BatchFileSubmit>();
    
	private kapb_wjplxxb tblkapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Property property){
		tblkapbWjplxxb = Kapb_wjplxxbDao
				.selectOne_odb1(input.getFilesq(), true);
//		String pathname = tblkapbWjplxxb.getLocaph()+tblkapbWjplxxb.getUpfena();
//		KnpParameter KnpParameter = KnpParameterDao.selectOne_odb1("IPPDZ", "dzfile", "01", "%", true);
//		String chckdt = input.getChckdt();//文件日期
//		String str = "0";
//		filePath = KnpParameter.getParm_value1() + chckdt + File.separator;
//
//		
		
//		
//        // 若参数表中val4为空或者小于当前交易日期，将idx值初始化为1，并+1后更新参数表，将当前日期替换val4
//        if (CommUtil.isNull(KnpParameter.getParm_value4())
//                || CommUtil.compare(chckdt, KnpParameter.getParm_value4()) > 0) {
//
//            idx = "1";
//
//            Integer seqno = (ConvertUtil.toInteger(idx) + 1);
//            KnpParameter.setParm_value3(seqno.toString());
//            KnpParameter.setParm_value4(chckdt);
//            KnpParameterDao.updateOne_odb1(KnpParameter);
//            // 若参数表中的val4等于当前交易日期，则取val3为idx值，然后加1更新参数表
//        } else if (CommUtil.equals(chckdt, KnpParameter.getParm_value4())) {
//
//            if (CommUtil.isNull(KnpParameter.getParm_value3()) || "0" == KnpParameter.getParm_value3()) {
//                idx = "1";
//            } else {
//                idx = KnpParameter.getParm_value3();
//            }
//            Integer seqno = (ConvertUtil.toInteger(idx) + 1);
//            KnpParameter.setParm_value3(seqno.toString());
//            KnpParameterDao.updateOne_odb1(KnpParameter);
//        }
//
//        // 若后缀小于3为，则补到3位
//      idx = CommUtil.lpad(idx, 4, str);
//		        
//		fileName ="IPP_"+filetp+"_"+chckdt+"_"+idx+".txt";
//		property.setChekdt(chckdt);
		//return filePath+fileName;

//		String pathname = tblkapbWjplxxb.getLocaph()
//				+ tblkapbWjplxxb.getUpfena();
		//filePath = tblkapbWjplxxb.getLocaph();
//		String dnc = CommTools.getBaseRunEnvs().getCurrent_dcn();

		String pathname = tblkapbWjplxxb.getUpfeph()+tblkapbWjplxxb.getUpfena();

		filePath = tblkapbWjplxxb.getUpfeph();
		
		fileName = tblkapbWjplxxb.getUpfena();
		
		bizlog.debug("文件产生路径 path:[" + pathname + "]");
		
		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.paycka.Header getHeader(cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Property property){
		//TODO
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
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPaycBatch> getFileBodyDataWalker(cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Property property){
		Params param = new Params();
		param.put("servdt", input.getChckdt());
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPaycBatch>(DpAcctQryDao.namedsql_selKnbpaycbatchByFile , param); 
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPaycBatch dateItem , cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.paycka.Body body, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Property property) {

	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.paycka.Foot getFoot(cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Property property) {
		//删除已生成的对账明细
		DpAcctQryDao.delKnbpaycbatchByday(input.getChckdt());
		
		//将knl_iobl对账明细插入文件生成临时表KnbPaycBatch
		DpAcctQryDao.insKnbpaycbatchByiobl(input.getChckdt());
		
		//将knl_cary对账明细插入文件生成临时表KnbPaycBatch
		//DpAcctQryDao.insKnbpaycbatchBycary(input.getChckdt());
		//将Knb_Rptr_Detl对账明细插入文件生成临时表KnbPaycBatch
		DpAcctQryDao.insKnbpaycbatchBydetl(input.getChckdt());
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Property property, cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPaycBatch dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Property property){
		
		tblkapbWjplxxb.setBtfest(E_BTFEST.RTNS);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		
		generationMD5();
        E_SYSCCD target = E_SYSCCD.IPP;
        String status = E_FILEST.SUCC.getValue();
        String descri = "核心与统一支付对账";
        SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBackNotice(
                status, descri, target, filetp, ls);
		
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Payckawt.Property property,
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
	
	   /**
  * 
  * <p>Title:generationMD5 </p>
  * <p>Description: 生成MD5，并写入文件</p>
  * 
  * @author Xiaoyu Luo
  * @date 2017年8月11日
  */
 public void generationMD5() {

     BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
     Map<String, String> map = new HashMap<String, String>();
     md5 = MD5EncryptUtil.getFileMD5String(new File(filePath.concat(
	         File.separator).concat(fileName)));

     map.put(ApBatchFileParams.BATCH_PMS_FILESQ,filesq);
     map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

     batch.setFilenm(fileName);
     batch.setFlpath(filePath);
     batch.setFilemd(md5);
     batch.setParams(JSON.toJSONString(map));

     ls.add(batch);
 }

}

