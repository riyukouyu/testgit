package cn.sunline.ltts.busi.catran.batchfile;
import cn.sunline.adp.cedar.server.batch.file.ReadFileProcessor;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbAcif;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbAcifDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaBatchWarnInfo;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


	 /**
	  * 涉案可疑名单同步
	  *
	  */

public class blksynReadFileProcessor extends ReadFileProcessor<cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Input,
cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Property,cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.blksyn.Header,cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.blksyn.Body,cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.blksyn.Foot>{
	private static final BizLog bizlog = BizLogUtil.getBizLog(blksynReadFileProcessor.class);
	private kapb_wjplxxb tblkapbwjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	
	/**
	  *  获取待处理文件（通常为下载）
	  *  
	  *  @return 文件路径 
	  **/
	public String downloadFile(cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Property property) {
		
		
		
		
	    //String pathname = "D:\\11" + File.separator + "black_20170107103002.txt"; //文件名(包括路径)
		String pathname =tblkapbwjplxxb.getDownph() + tblkapbwjplxxb.getDownna();
		
		bizlog.debug("文件批次号:[%s], 文件名称: [%s],文件路径:[%s], 文件类型: [%s]", input.getFilesq(), tblkapbwjplxxb.getDownna(), tblkapbwjplxxb.getDownph(), E_FILETP.CA010400.getLongName());
		
		bizlog.debug(">>>>>>>>>>>>>>>>>>downloadFile end>>>>>>>>>>>>>>>>>>>>>");
		
		return pathname;
	}

    /**
	  *  解析文件头后转换为对应的javabean对象后，提供处理。
	  *  @parm header 文件头对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *
	  */
	public boolean headerProcess(cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.blksyn.Header head ,cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Property property) {
	 	//TODO
	 	return false;
	}
	
	 /**
	  *  解析文件体后转换为对应的javabean对象后，提供处理。
	  *  @parm body 文件体对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *  注：该方法将会分发到作业服务器进行并发执行，需要配置交易控制器中的执行模式为"3"
	  */
	public boolean bodyProcess(int index, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.blksyn.Body body , cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Property property) {
		bizlog.debug(">>>>>>>>>>>>>>>>>>>bodyProcess begin>>>>>>>>>>>>>>>>>>>");
		bizlog.debug("文件时间:[%s]", tblkapbwjplxxb.getAcctdt());
		//文件操作类型
		String type = property.getFiletype();
		String cur_cardno = body.getCardno().substring(0,2);
		//涉案名单同步
		if(CommUtil.equals(type, "1")||CommUtil.equals(type, "2")){
			KnbAcif tbknbacif = KnbAcifDao.selectOne_odb4(body.getDatatype(), body.getCardno(), body.getCustna(), body.getInevir(), false);
			boolean insFlag = false; //新增标志
			
			if(CommUtil.isNull(tbknbacif)){
				tbknbacif = SysUtil.getInstance(KnbAcif.class);
				//未找到该条信息，新增标志为真
				insFlag = true;
			}else{				
				//该记录存在，且同步状态为删除
				if(CommUtil.isNotNull(tbknbacif) && CommUtil.equals(body.getAddtype(), "-")){
//					KnbAcifDao.deleteOne_odb4(body.getDatatype(), body.getCardno(), body.getCustna(), body.getInevir());
					return false;
				}
			} 
			
		
		if(CommUtil.equals(body.getAddtype(), "+")){ //新增
			if(CommUtil.equals(body.getDatatype(), "IDType_IDNumber")){
				if(CommUtil.equals("99", cur_cardno)){
					tbknbacif.setCardno("9"+body.getCardno());
				}
				else if (CommUtil.equals("07", cur_cardno)) {
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("111"+cardno);
				}
				else if(CommUtil.equals("09", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("120"+cardno);
				}
				else if(CommUtil.equals("10", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("112"+cardno);
				}else if(CommUtil.equals("11", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("181"+cardno);
				}else if(CommUtil.equals("12", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("113"+cardno);
				}else if(CommUtil.equals("13", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("203"+cardno);
				}else if(CommUtil.equals("14", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("204"+cardno);
				}else if(CommUtil.equals("15", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("202"+cardno);
				}else if(CommUtil.equals("16", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("999"+cardno);
				}
				else{
					tbknbacif.setCardno("1"+body.getCardno());
				}
			}
			else {
				tbknbacif.setCardno(body.getCardno());
			}
				tbknbacif.setDatatp(body.getDatatype());
				tbknbacif.setDotype(body.getAddtype());
				tbknbacif.setSource(body.getSource());
				//tbknbacif.setCasetp(body.getType());
				tbknbacif.setCustna(body.getCustna());
				tbknbacif.setBrchno(body.getBrchno());
				tbknbacif.setPlname(body.getPlname());
				tbknbacif.setPlctel(body.getPltel());
				tbknbacif.setPlcktm(body.getPltm());
				tbknbacif.setOpentu(body.getOpentu());
				tbknbacif.setValidt(body.getValdt());
				tbknbacif.setRemark(body.getRemark());
				tbknbacif.setInevir(body.getInevir());
				tbknbacif.setInevil(E_INSPFG.INVO);
				tbknbacif.setIseffe(E_YES___.YES);
				tbknbacif.setFilesq(input.getFilesq());
				//tbknbacif.setCorpno("999");
			}
		if(CommUtil.equals(body.getAddtype(), "-")){ //
			if(CommUtil.equals(body.getDatatype(), "IDType_IDNumber")){
				
				if(CommUtil.equals("99", cur_cardno)){
					tbknbacif.setCardno("9"+body.getCardno());
				}
				else if (CommUtil.equals("07", cur_cardno)) {
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("111"+cardno);
				}
				else if(CommUtil.equals("09", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("120"+cardno);
				}
				else if(CommUtil.equals("10", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("112"+cardno);
				}else if(CommUtil.equals("11", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("181"+cardno);
				}else if(CommUtil.equals("12", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("113"+cardno);
				}else if(CommUtil.equals("13", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("203"+cardno);
				}else if(CommUtil.equals("14", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("204"+cardno);
				}else if(CommUtil.equals("15", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("202"+cardno);
				}else if(CommUtil.equals("16", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacif.setCardno("999"+cardno);
				}
				else{
					tbknbacif.setCardno("1"+body.getCardno());
				
				}
			}
			else{
				tbknbacif.setCardno(body.getCardno());
			}
				tbknbacif.setDatatp(body.getDatatype());
				tbknbacif.setDotype(body.getAddtype());
				tbknbacif.setSource(body.getSource());
				//tbknbacif.setCasetp(body.getType());
				tbknbacif.setCustna(body.getCustna());
				tbknbacif.setBrchno(body.getBrchno());
				tbknbacif.setPlname(body.getPlname());
				tbknbacif.setPlctel(body.getPltel());
				tbknbacif.setPlcktm(body.getPltm());
				tbknbacif.setOpentu(body.getOpentu());
				tbknbacif.setValidt(body.getValdt());
				tbknbacif.setRemark(body.getRemark());
				tbknbacif.setInevir(body.getInevir());
				tbknbacif.setInevil(E_INSPFG.INVO);
				tbknbacif.setIseffe(E_YES___.NO);
				tbknbacif.setFilesq(input.getFilesq());
				//tbknbacif.setCorpno("999");
			}
			if(insFlag){
				KnbAcifDao.insert(tbknbacif);
			}else{
				KnbAcifDao.updateOne_odb1(tbknbacif);
			}
			//可疑名单
		}else if(CommUtil.equals(type, "3")||CommUtil.equals(type, "4")){
			KnbAcif tbknbacdb = KnbAcifDao.selectOne_odb4(body.getDatatype(), body.getCardno(), body.getCustna(), body.getInevir(), false);
			boolean insFlag = false; //新增标志
			
			if(CommUtil.isNull(tbknbacdb)){	
				tbknbacdb = SysUtil.getInstance(KnbAcif.class);
				//未找到该条信息，新增标志为真
				insFlag = true;
			}else{				
				//该记录存在，且同步状态为删除
				if(CommUtil.isNotNull(tbknbacdb) && CommUtil.equals(body.getAddtype(), "-")){
//					KnbAcifDao.deleteOne_odb4(body.getDatatype(), body.getCardno(), body.getCustna(), body.getInevir());
					tbknbacdb.setIseffe(E_YES___.NO);
					return false;
				}
			}

		if(CommUtil.equals(body.getAddtype(), "+")){ //新增
			if(CommUtil.equals(body.getDatatype(), "IDType_IDNumber")){
				
				if(CommUtil.equals("99", cur_cardno)){
					tbknbacdb.setCardno("9"+body.getCardno());
				}
				else if (CommUtil.equals("07", cur_cardno)) {
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("111"+cardno);
				}
				else if(CommUtil.equals("09", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("120"+cardno);
				}
				else if(CommUtil.equals("10", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("112"+cardno);
				}else if(CommUtil.equals("11", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("181"+cardno);
				}else if(CommUtil.equals("12", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("113"+cardno);
				}else if(CommUtil.equals("13", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("203"+cardno);
				}else if(CommUtil.equals("14", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("204"+cardno);
				}else if(CommUtil.equals("15", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("202"+cardno);
				}else if(CommUtil.equals("16", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("999"+cardno);
				}
				else{
				tbknbacdb.setCardno("1"+body.getCardno());
				}
			}
			else{
				tbknbacdb.setCardno(body.getCardno());
			}
				tbknbacdb.setDatatp(body.getDatatype());
				tbknbacdb.setDotype(body.getAddtype());
				tbknbacdb.setSource(body.getSource());
				//tbknbacdb.setCasetp(body.getType());
				tbknbacdb.setCustna(body.getCustna());
				tbknbacdb.setBrchno(body.getBrchno());
				tbknbacdb.setPlname(body.getPlname());
				tbknbacdb.setPlctel(body.getPltel());
				tbknbacdb.setPlcktm(body.getPltm());
				tbknbacdb.setOpentu(body.getOpentu());
				tbknbacdb.setValidt(body.getValdt());
				tbknbacdb.setRemark(body.getRemark());
				tbknbacdb.setInevir(body.getInevir());
				tbknbacdb.setInevil(E_INSPFG.SUSP);
				tbknbacdb.setIseffe(E_YES___.YES);
				tbknbacdb.setFilesq(input.getFilesq());
				//tbknbacdb.setCorpno("999");
			}
		if(CommUtil.equals(body.getAddtype(), "-")){ //
			if(CommUtil.equals(body.getDatatype(), "IDType_IDNumber")){
				
				if(CommUtil.equals("99", cur_cardno)){
					tbknbacdb.setCardno("9"+body.getCardno());
				}
				else if (CommUtil.equals("07", cur_cardno)) {
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("111"+cardno);
				}
				else if(CommUtil.equals("09", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("120"+cardno);
				}
				else if(CommUtil.equals("10", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("112"+cardno);
				}else if(CommUtil.equals("11", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("181"+cardno);
				}else if(CommUtil.equals("12", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("113"+cardno);
				}else if(CommUtil.equals("13", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("203"+cardno);
				}else if(CommUtil.equals("14", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("204"+cardno);
				}else if(CommUtil.equals("15", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("202"+cardno);
				}else if(CommUtil.equals("16", cur_cardno)){
					String cardno = body.getCardno().substring(2);
					tbknbacdb.setCardno("999"+cardno);
				}
				else{
				    tbknbacdb.setCardno("1"+body.getCardno());
				}
			}
			else{
				 tbknbacdb.setCardno(body.getCardno());
			}
				tbknbacdb.setDatatp(body.getDatatype());
				tbknbacdb.setDotype(body.getAddtype());
				tbknbacdb.setSource(body.getSource());
				//tbknbacdb.setCasetp(body.getType());
				tbknbacdb.setCustna(body.getCustna());
				tbknbacdb.setBrchno(body.getBrchno());
				tbknbacdb.setPlname(body.getPlname());
				tbknbacdb.setPlctel(body.getPltel());
				tbknbacdb.setPlcktm(body.getPltm());
				tbknbacdb.setOpentu(body.getOpentu());
				tbknbacdb.setValidt(body.getValdt());
				tbknbacdb.setRemark(body.getRemark());
				tbknbacdb.setInevir(body.getInevir());
				tbknbacdb.setInevil(E_INSPFG.SUSP);
				tbknbacdb.setIseffe(E_YES___.NO);
				tbknbacdb.setFilesq(input.getFilesq());
				//tbknbacdb.setCorpno("999");
			}
			if(insFlag){
				KnbAcifDao.insert(tbknbacdb);
				bizlog.debug("文件时间:[%s]", tblkapbwjplxxb.getAcctdt());
			}else{
				KnbAcifDao.updateOne_odb1(tbknbacdb);
				bizlog.debug("文件时间:[%s]", tblkapbwjplxxb.getAcctdt());
			}
			
		}else{
			bizlog.debug(">>>>>>>>>>>>>>>>>>>未知文件类型:"+type+">>>>>>>>>>>>>>>>>>");
	 		return false;
		}
				
		bizlog.debug(">>>>>>>>>>>>>>>>>>>同步名单成功:"+body.getCardno()+">>>>>>>>>>>>>>>>>>");
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
	public boolean footProcess(cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.blksyn.Foot foot ,cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Property property) {
	 	//TODO
	 	return false;
	}
	
	/**
	 * 读文件交易前处理
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Property property) {
		//文件同步类型
		bizlog.debug("文件时间:[%s]", tblkapbwjplxxb.getAcctdt());
				String type = "";
				tblkapbwjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
				String str[] = tblkapbwjplxxb.getDownna().trim().split("_");
				if(CommUtil.equals(str[0], "all")){

					if(CommUtil.equals(str[1], "black")){
						bizlog.debug(">>>>>>>>>>>>>>>>>>>文件类型：涉案名单全量同步>>>>>>>>>>>>>>>>>>>");				
						type = "1";		
						//全量删除涉案名单
						//CaDao.delknbacifall();
						KnbAcifDao.delete_odb2(E_INSPFG.INVO);
					}else if(CommUtil.equals(str[1], "gray")){
						bizlog.debug(">>>>>>>>>>>>>>>>>>>文件类型：可疑名单全量同步>>>>>>>>>>>>>>>>>>>");
						type = "3";
						//全量删除可疑名单
						KnbAcifDao.delete_odb2(E_INSPFG.SUSP);
					}else{
						bizlog.debug(">>>>>>>>>>>>>>>>>>>文件名称对应文件类型未知>>>>>>>>>>>>>>>>>>>");
						
					}
				}else if(CommUtil.equals(str[0], "black")){
					bizlog.debug(">>>>>>>>>>>>>>>>>>>文件类型：涉案名单增量同步>>>>>>>>>>>>>>>>>>>");
					type = "2";
				}else if(CommUtil.equals(str[0], "gray")){
					bizlog.debug(">>>>>>>>>>>>>>>>>>>文件类型：可疑名单增量同步>>>>>>>>>>>>>>>>>>>");
					type = "4";
				}else{
					bizlog.debug(">>>>>>>>>>>>>>>>>>>文件名称对应文件类型未知>>>>>>>>>>>>>>>>>>>");
					
				}
				property.setFiletype(type);
				bizlog.debug("文件时间:[%s]", tblkapbwjplxxb.getAcctdt());
	}

	/**
	 * 文件头解析异常处理(包括文件的下载、文件的打开、文件头的解析)
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Property property, Throwable t) {
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
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Property property,
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
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 读文件交易处理结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Property property) {
		
		bizlog.debug("文件时间:[%s]", tblkapbwjplxxb.getAcctdt());
		tblkapbwjplxxb.setBtfest(E_BTFEST.SUCC);// 状态
		Kapb_wjplxxbDao.updateOne_odb1(tblkapbwjplxxb);
		bizlog.debug("业务流水:[%s], 文件批次号: [%s],文件时间:[%s]", property.getBusseq(), tblkapbwjplxxb.getBtchno(), tblkapbwjplxxb.getAcctdt());
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
	public void afterBodyResolveProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount) {}

	
	
	/**
	 * 读文件交易处理异常后回调(交易的所有异常都会进入到该方法，包括之前已处理过并继续抛出的异常)
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Input input, cn.sunline.ltts.busi.catran.batchfile.intf.Blksyn.Property property,
			Throwable t) {
		
				tblkapbwjplxxb.setBtfest(E_BTFEST.FAIL);
				Kapb_wjplxxbDao.updateOne_odb1(tblkapbwjplxxb);
				
				//监控预警平台
				KnpParameter para = KnpParameterDao.selectOne_odb1("DAYENDNOTICE", "%", "%", "%", true);
				
				String bdid = para.getParm_value1();// 服务绑定ID
				
				String mssdid = CommTools.getMySysId();// 随机生成消息ID
				
				String mesdna = para.getParm_value2();// 媒介名称
				
				IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
				
				IoCaOtherService.IoCaDayEndFailNotice.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaDayEndFailNotice.InputSetter.class);
				
				String timetm = DateTools2.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS000");
				IoCaBatchWarnInfo content = SysUtil.getInstance(IoCaBatchWarnInfo.class);
				content.setPljioyma("blksyn");
				content.setPljyzbsh("0104");
				content.setPljyzwmc("涉案可疑名单同步");
				content.setErrmsg("涉案可疑名单同步异常");
				content.setTrantm(timetm);
				
				// 发送消息
				mqInput.setMsgid(mssdid); // 消息ID
				mqInput.setMdname(mesdna); // 媒介名称
				mqInput.setTypeCode("NAS");
				mqInput.setTypeName("网络金融核心平台-电子账户核心系统");
				mqInput.setItemId("NAS_BATCH_WARN");
				mqInput.setItemName("电子账户核心批量执行错误预警");
//				String str =JSON.toJSONString(content);
//				mqInput.setContent(str);
				
				mqInput.setWarnTime(timetm);
				
				caOtherService.dayEndFailNotice(mqInput);
		
	}
	
}

