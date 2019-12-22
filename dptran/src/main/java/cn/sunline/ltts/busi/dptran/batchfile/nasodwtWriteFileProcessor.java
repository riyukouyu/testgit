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
import cn.sunline.ltts.busi.dp.namedsql.ProSalSqlDao;
import cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.nasodwt.Header;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatch;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;

	 /**
	  * 电子账户订单核对文件生成
	  *
	  */

public class  nasodwtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Input,cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Property,cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.nasodwt.Header,cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.nasodwt.Body,cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.nasodwt.Foot,cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbRegi>{

    private static final BizLog bizlog = BizLogUtil
            .getBizLog(nasodwtWriteFileProcessor.class);
    private static String trandt = CommTools.getBaseRunEnvs().getTrxn_date();  //交易日期
    //private static String corpno = BusiTools.getCenterCorpno();             //交易法人 
    // 存放目录更改
    private static String filePath = ""; // 文件路径
    private static String fileName = ""; // 文件名
   // private static String idx = ""; // 文件名后缀

    private static E_FILETP filetp = E_FILETP.LN901400;
    //private static String separators = "|@|";
    private static String filesq = "NASPSS" + trandt;// 统一生成一个流水号

    private static String md5 = ""; // MD5值
    private static String path2 = "";
    DefaultOptions<BatchFileSubmit> ls = new DefaultOptions<BatchFileSubmit>();
    
	private kapb_wjplxxb tblkapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Property property){
/*		String filePath = "";
		String trandt = tblkapbWjplxxb.getAcctdt();
		String filesq = input.getFilesq();
		// BatchFileSubmit batch1 =
		// SysUtil.getInstance(BatchFileSubmit.class);
		KnpParameter ts = KnpParameterDao.selectOne_odb1("NASOD", "dpwrit", "01", "%",
				true);
		// 文件路径
		filePath = ts.getParm_value1() + filetp + File.separator + trandt
				+ File.separator;
		String filename1 = "nas_" + "021700" + "_trans" + "_" + "chk" + ".txt";
		String pathname = filePath.concat(File.separator).concat(filename1);*/

		tblkapbWjplxxb = Kapb_wjplxxbDao
				.selectOne_odb1(input.getFilesq(), true);
//		String pathname = tblkapbWjplxxb.getLocaph()
//				+ tblkapbWjplxxb.getUpfena();
		//filePath = tblkapbWjplxxb.getLocaph();
		String dnc = CommTools.getBaseRunEnvs().getCurrent_dcn();
		String[] str= tblkapbWjplxxb.getUpfena().split("\\.");
		String filena = str[0].concat(dnc).concat(".txt");
		
		String pathname = tblkapbWjplxxb.getUpfeph()+filena;
		filePath = tblkapbWjplxxb.getUpfeph();
		fileName = tblkapbWjplxxb.getUpfena();

		path2 = tblkapbWjplxxb.getUpfeph().substring(tblkapbWjplxxb.getUpfeph().indexOf("/",2))+filena;
		filesq = input.getFilesq();
		filetp = tblkapbWjplxxb.getFiletp();
		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.nasodwt.Header getHeader(cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Property property){
		String trandt = tblkapbWjplxxb.getAcctdt();
		long count = ProSalSqlDao.selNasOdCount(trandt,CommTools.getBaseRunEnvs().getBusi_org_id() ,true);
		Header header = SysUtil.getInstance(Header.class);
		header.setTotanm(count);
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
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbRegi> getFileBodyDataWalker(cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Property property){
		Params parm = new Params();
		
		String trandt =tblkapbWjplxxb.getAcctdt();
		parm.add("trandt", trandt);
		parm.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbRegi>(ProSalSqlDao.namedsql_selNasOdByChkDate , parm); 

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
	public void bodyProcess(int index, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbRegi dateItem , cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.nasodwt.Body body, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Property property) {
//		String trandt = tblkapbWjplxxb.getAcctdt();
//		List<KnbRegi> regi = ProSalSqlDao.selNasOdByChkDate(trandt,CommTools.getBaseRunEnvs().getBusi_org_id(), false);
//		body.setCckdtt(trandt);
//	for(KnbRegi KnbRegi : regi){
//		if(E_CORDST.SUCCESS == KnbRegi.getCordst()){
//			body.setErorcd("00000000");
//			//body.setErrotx("交易成功");
//		}
//	if(E_CORDST.SUCCESS != KnbRegi.getCordst()){
//		body.setErorcd("999999999");
//		//body.setErrotx("交易失败");
//			}
//		}
	}
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.nasodwt.Foot getFoot(cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Property property) {
/*		tblkapbWjplxxb.setBtfest(E_BTFEST.RESTING);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);*/
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Property property, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbRegi dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Property property){
		
		tblkapbWjplxxb.setBtfest(E_BTFEST.RTNS);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		
		generationMD5();
        E_SYSCCD target = E_SYSCCD.PSS;
        String status = E_FILEST.SUCC.getValue();
        String descri = "核心和销售工厂对账";
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
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Input input, cn.sunline.ltts.busi.dptran.batchfile.intf.Nasodwt.Property property,
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

        map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
        map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

        batch.setFilenm(fileName);
        batch.setFlpath(path2);
        batch.setFilemd(md5);
        batch.setParams(JSON.toJSONString(map));

        ls.add(batch);
    }

}

