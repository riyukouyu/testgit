package cn.sunline.ltts.busi.dptran.batchfile.redpck;

import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.file.ReadFileProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpFrozDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.RpBatchTransDao;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrCbai;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrCbaiDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;

	 /**
	  * 可入账账户信息同步上送文件
	  *
	  */

public class rpraisReadFileProcessor extends ReadFileProcessor<cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Input,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Property,cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.rprais.Body,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.rprais.Foot>{
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(rpraisReadFileProcessor.class);
	private kapb_wjplxxb tblkapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	
	/**
	  *  获取待处理文件（通常为下载）
	  *  
	  *  @return 文件路径 
	  **/
	public String downloadFile(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Property property) {

		bizlog.debug(">>>>>>>>>>>>>>>>>>downloadFile begin>>>>>>>>>>>>>>>>>>>>");
		
		//清空可入账信息表
		RpBatchTransDao.delKnbRptrCbai();
		
		tblkapbWjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		
		String pathname = tblkapbWjplxxb.getDownph() + tblkapbWjplxxb.getDownna(); //文件名(包括路径)
		
		bizlog.debug("文件批次号:[%s], 文件名称: [%s],文件路径:[%s], 文件类型: [%s]", input.getFilesq(), property.getDwname(), property.getDwpath(), E_FILETP.DP021100.getLongName());
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>downloadFile end>>>>>>>>>>>>>>pathname =" + pathname +">>>>>>>");
		
		return pathname;
	}

    /**
	  *  解析文件头后转换为对应的javabean对象后，提供处理。
	  *  @parm header 文件头对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *
	  */
	public boolean headerProcess(cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb mapping ,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Property property) {

		bizlog.debug(">>>>>>>>>>>>>>>>>headerProcess begin>>>>>>>>>>>>>>>>>>>>>");
		
		tblkapbWjplxxb.setTotanm(mapping.getTotanm()); //总笔数
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		
		bizlog.debug(">>>>>>>>>>>>>>>>>headerProcess end>>>>>>>>>>>>>>>>>>>>");
		
	 	return false;
	}
	
	 /**
	  *  解析文件体后转换为对应的javabean对象后，提供处理。
	  *  @parm body 文件体对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *  注：该方法将会分发到作业服务器进行并发执行，需要配置交易控制器中的执行模式为"3"
	  */
	public boolean bodyProcess(int index, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.rprais.Body body , cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Property property) {

		bizlog.debug(">>>>>>>>>>>>>>>>>>>bodyProcess begin>>>>>>>>>>>>>>>>>>>");
		
		String acstid = body.getAcstid(); //账户用户ID
		String ccstid = body.getCcstid(); //渠道用户ID
		String trandt = tblkapbWjplxxb.getTrandt(); //交易日期
		E_FROZST frozst = E_FROZST.VALID;
		
		IoCaSevQryTableInfo caqry =  SysUtil.getInstance(IoCaSevQryTableInfo.class);
		KnbRptrCbai tblknbrptrcbai = SysUtil.getInstance(KnbRptrCbai.class);
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		java.util.Map<String,Object> custInfo = DpAcctDao.selCustInfoByCustid(acstid,corpno, false); //通过账户用户ID查询电子账户ID
		if(CommUtil.isNull(custInfo)){
			return true;
//			throw DpModuleError.DpstComm.E9999("未找到对应电子账户ID");
		}
		String custac = (String) custInfo.get("custac"); //电子账户
		
		if(CommUtil.isNull(custac)){
			return true;
//			throw DpModuleError.DpstComm.E9999("电子账号不存在");
		}
		
		IoCaKnaCust kna_cust = caqry.getKnaCustByCustacOdb1(custac, false); //根据电子账号ID查询电子账户信息
		
		if(CommUtil.isNull(kna_cust)){
			return true;
//			throw DpModuleError.DpstComm.E9999("电子账号状态不正常");
		}
		
		List<IoDpKnbFroz> knb_froz = DpFrozDao.selFrozInfoByCustac(custac, null, trandt, frozst, false); 
		for(IoDpKnbFroz froz : knb_froz){
			if(froz.getFroztp() == E_FROZTP.ADD || froz.getFroztp() == E_FROZTP.JUDICIAL){
				if(froz.getFrlmtp() == E_FRLMTP.IN){
					return true;
//					throw DpModuleError.DpstComm.E9999("电子账户已借冻");
				}else if(froz.getFrlmtp() == E_FRLMTP.ALL){
					return true;
//					throw DpModuleError.DpstComm.E9999("电子账户已双冻");
				}
			}
		}
		
		tblknbrptrcbai.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());
		tblknbrptrcbai.setFilesq(input.getFilesq());
		tblknbrptrcbai.setTrandt(tblkapbWjplxxb.getAcctdt());
		tblknbrptrcbai.setCardno((String) custInfo.get("cardno"));
		tblknbrptrcbai.setAcstid(acstid);
		tblknbrptrcbai.setCcstid(ccstid);
		tblknbrptrcbai.setCustna((String) custInfo.get("custna"));
		tblknbrptrcbai.setBrchno((String) custInfo.get("brchno"));
		KnbRptrCbaiDao.insert(tblknbrptrcbai); //更新可入账信息表
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>>bodyProcess end>>>>>>>>>>>>>>>>>>");
		
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
	public boolean footProcess(cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.rprais.Foot foot ,cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Property property) {
	 	
		return false;
	}
	
	/**
	 * 读文件交易前处理
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Property property) {}

	/**
	 * 文件头解析异常处理(包括文件的下载、文件的打开、文件头的解析)
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Property property, Throwable t) {
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
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 读文件交易处理结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Property property) {
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>>>>>afterReadFileTranProcess begin>>>>>>>>>>>>>>>>>>>>>");

		tblkapbWjplxxb.setBtfest(E_BTFEST.READ);
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>>>>>afterReadFileTranProcess end>>>>>>>>>>>>>>>>>>>>>");
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
	public void afterBodyResolveProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount) {}

	/**
	 * 读文件交易处理异常后回调(交易的所有异常都会进入到该方法，包括之前已处理过并继续抛出的异常)
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Input input, cn.sunline.ltts.busi.dptran.batchfile.redpck.intf.Rprais.Property property,
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

