package cn.sunline.ltts.busi.intran.batchfile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.base.util.security.encrypt.MD5EncryptUtil;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BatchFileSubmit;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.KnsAcsqColl;
import cn.sunline.ltts.busi.in.tables.In.KnsAcsqCollDao;
import cn.sunline.ltts.busi.in.type.InQueryTypes.InClerckInfo;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatch;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaBatchWarnInfo;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CLERST;


	 /**
	  * 清算并账
	  *
	  */

public class  clfilewtWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Input,cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Property,cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.clfilewt.Header,cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.clfilewt.Body,cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.clfilewt.Foot,cn.sunline.ltts.busi.in.tables.In.KnsAcsqColl>{
	private static final BizLog bizlog = BizLogUtil.getBizLog(clfilewtWriteFileProcessor.class);
	private  kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	private  String trandt = null;
	private  String  maxcut = null;//获取最大清算场次参数
	private  Integer curnct = null;//当前系统清算场次
	private  Integer clenum = 1;//供数清算场次
	private  String clerdt=null;//供数清算日期
    private  String path = null;
    private  String path2 =null;
    private  String filename= null;
    private  String filesq = null;
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */	
	public String getFileName(cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Property property){
		
			
		bizlog.method(">>>>>>>>>>>>>>>>>>>>getFileName begin>>>>>>>>>>>>>>>>>>>");
		String seqno = CommTools.getBaseRunEnvs().getTrxn_seq();
	
		
 		filetab.setUpfeph(path);
		filetab.setUpfena(filename);
		filetab.setFiletp(E_FILETP.IN030600);
		filetab.setBtfest(E_BTFEST.GIVING);
		filetab.setBtchno(seqno);
		Kapb_wjplxxbDao.insert(filetab);

		filetab = Kapb_wjplxxbDao.selectOne_odb1(seqno, true);
		String pathname = filetab.getLocaph() + filetab.getUpfena();
		
		bizlog.parm("-------------pathname[%s]", pathname);
		bizlog.method(">>>>>>>>>>>>>>>>>>>getFileName end>>>>>>>>>>>>>>>>>>>>");
		
		return path+"\\"+filename;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.clfilewt.Header getHeader(cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Property property){
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
	public BatchDataWalker<cn.sunline.ltts.busi.in.tables.In.KnsAcsqColl> getFileBodyDataWalker(cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Property property){
		
		

		
		Params param = new Params();
		if(CommUtil.compare(input.getClenum(), 0)>0&&CommUtil.isNotNull(input.getClerdt())){
			
			param.put("clerdt", input.getClerdt());
			param.put("clenum",input.getClenum());
		}else{
			
			param.put("clerdt", clerdt);
			param.put("clenum",clenum);
		}
 		return new CursorBatchDataWalker<cn.sunline.ltts.busi.in.tables.In.KnsAcsqColl>(InacSqlsDao.namedsql_selKnsAcsqColl , param); 
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
	public void bodyProcess(int index, cn.sunline.ltts.busi.in.tables.In.KnsAcsqColl dateItem , cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.clfilewt.Body body, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Property property) {
//		1-	网络核心内定时清算
//		2-	网络核心柜面核心定时清算
//		3-	网络核心信用卡核心定时清算
//		4-	网络核心大额支付定时清算
//		5-	网络核心小额支付定时清算
//		6-	网络核心农信银定时清算
//		7-	网络核心跨行网银定时清算
//		8-	网络核心银联跨行贷方定时清算
//		9-	网络核心银联跨行借方定时清算
//		10-	网络核心中间业务定时清算
//		11-	银联在线代收        
		
		body.setTrandt(DateTools2.getCurrentTimestamp().substring(0, 8));//日期
		body.setTimetm(DateTools2.getCurrentLocalTime());//时间
		body.setFlagtp("1");//标志位默认送1
		if(body.getClactp().equals("01")){
			//网络柜面
			body.setClactp("2");
			
		}else if(body.getClactp().equals("02")){
			//中间业务
			body.setClactp("10");
			
		}else if(body.getClactp().equals("03")){
			//银联借记业务
			body.setClactp("9");
			
		}else if(body.getClactp().equals("04")){
			//银联贷记业务
			
			body.setClactp("8");
			
		}else if(body.getClactp().equals("05")){
			//大额支付系统
			body.setClactp("4");
			
		}else if(body.getClactp().equals("06")){
			//小额支付系统
			body.setClactp("5");
			
		}else if(body.getClactp().equals("07")){
			//农信银支付系统
			body.setClactp("6");
			
		}else if(body.getClactp().equals("08")){
			//跨行网银支付系统
			body.setClactp("7");
			
		}else if(body.getClactp().equals("09")){
			//信用卡
			body.setClactp("3");
			
		}else if(body.getClactp().equals("10")){
			//系统内
			body.setClactp("1");
			
		}else if(body.getClactp().equals("11")){
			//银联在线代收
			body.setClactp("11");
			
		}
		
	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.clfilewt.Foot getFoot(cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Property property) {

		 trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		  maxcut =  KnpParameterDao.selectOne_odb1("APTRAN", "ap011", "%", "%", false).getParm_value1();//获取最大清算场次参数
		 curnct = BusiTools.getBusiRunEnvs().getClenum();//当前系统清算场次
			if(CommUtil.compare(input.getClenum(), 0)>0&&CommUtil.isNotNull(input.getClerdt())){
				
				clerdt=input.getClerdt();
				clenum=input.getClenum();
			}else{				
				clenum = findClerdtAndClenum(curnct).getClenum();//供数清算场次
				clerdt=findClerdtAndClenum(curnct).getClerdt();//供数清算日期
			}
						 
	     path = KnpParameterDao.selectOne_odb1("CLFILE", "%","%", "%",true).getParm_value1()  + File.separator + trandt+ File.separator;
	     path2 = KnpParameterDao.selectOne_odb1("CLFILE", "%","%", "%",true).getParm_value2()   + trandt+ File.separator;
	    filename= "nas" + "_" +"030600"+ "_" + clerdt + "_" + CommUtil.lpad(clenum.toString(), 5, "0")+ ".txt";		
		filesq = BusiTools.getSequence("fileseq", 6);
		
		filetab.setBtfest(E_BTFEST.RESTING);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		String mtdate = DateTools2.getDateInfo().getSystdt();
		String timetm = DateTools2.getCurrentTimestamp();	

		
		if(E_YES___.YES!=input.getIswrte()){

			//重新生成数据			
			KnpParameter para_OUT = SysUtil.getInstance(KnpParameter.class);
			
			para_OUT = KnpParameterDao.selectOne_odb1("InParm.clearbusi",
					"out", "%", "%", true);
			// 系统内往来业务代码
			String syotbu = para_OUT.getParm_value1();
			//插入系统内的待清算账户数据
			InacSqlsDao.insertKnsAcsqColl(clerdt, clenum,syotbu);
			//更行记录为已经同步
			InacSqlsDao.updKnsAcsqCler(clerdt,clenum,timetm);
			//将汇总表的记录清算失败的金额相加后重新下更新到汇总表
			//当前批次的异常重跑，通过Iswrte控制，不重新生成数据，防止金额翻倍重复清算
			List<KnsAcsqColl> cplCollList = InacSqlsDao.selKnsAscqCollBuStatus(clerdt, clenum, false);
			if(CommUtil.isNotNull(cplCollList)){
				
				for (KnsAcsqColl coll:cplCollList){
					KnsAcsqColl cplColl = KnsAcsqCollDao.selectOne_odb3(clerdt, clenum, coll.getBrchno(), coll.getCrcycd(), coll.getProdcd(), coll.getClactp(), false);
					if(CommUtil.isNull(cplColl)){
						
						coll.setClerdt(clerdt);
						coll.setClenum(clenum);
						coll.setStatus(E_CLERST._0);
						KnsAcsqCollDao.insert(coll);
						
					}else{
						
						cplColl.setCranam(cplColl.getCranam().add(coll.getCranam()));
						cplColl.setDranam(cplColl.getDranam().add(coll.getDranam()));
						cplColl.setCrcunt(cplColl.getCrcunt()+coll.getCrcunt());
						cplColl.setDrcunt(cplColl.getDrcunt()+coll.getDrcunt());
						cplColl.setStatus(E_CLERST._0);
						KnsAcsqCollDao.updateOne_odb3(cplColl);
						
					}
				}         
				//更新状态为3的记录为2
				InacSqlsDao.updKnsAcsqCollData();
			}   
			
		}
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Property property,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Property property, cn.sunline.ltts.busi.in.tables.In.KnsAcsqColl dateItem,
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
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Property property,
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
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Property property){
		filetab.setBtfest(E_BTFEST.RESTSUCC);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);

		
       
        E_SYSCCD target = E_SYSCCD.IFS;
		E_FILETP filetp = E_FILETP.IN030600; 
		
	    // 获取文件名
	    String md5 = ""; 	//MD5值	    
	    md5 = MD5EncryptUtil.getFileMD5String(new File(path.concat(File.separator).concat(filename)));
	    Map<String,String> map = new HashMap<String,String>();
	    
	   List<KnsAcsqColl> list =  InacSqlsDao.selKnsAcsqColl(clerdt, clenum, false);
	   //渠道码‘NK’+ 并账标志‘B’+日期‘161129’+6位顺序号
	   String bathid="";
	   if(E_YES___.YES!=input.getIswrte()){
		   
		    bathid ="NK"+"B" +trandt.substring(2, 8)+filesq;//流水
	   }else{
		   if(CommUtil.isNotNull(list)){
			   
			   bathid= list.get(0).getBathid();
		   }
	   }
	   int count = 0;
	   if(CommUtil.isNotNull(list)){
		   count= list.size();
	   }
	   //更新批次号到kns_acsq_coll 
	   for (KnsAcsqColl tblColl : list ){
		   tblColl.setBathid(bathid);
		   tblColl.setStatus(E_CLERST._0);//之前清算失败的记录，重新清算修改状态为未清算
		   KnsAcsqCollDao.update_odb1(tblColl);
	   }
	   
	    String   BGR04CNT1 =String.valueOf(count);
	    
	    map.put("BGR04DATE1", trandt);
        map.put("BGR04BTNO1", bathid);
        map.put("BGR04CNT1", BGR04CNT1);
       
       BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);

       batch.setFilenm(filename);
       batch.setFlpath(path2);
       batch.setFilemd(md5);
       batch.setParams(JSON.toJSONString(map));
       

       
       
       DefaultOptions<BatchFileSubmit> ls = new DefaultOptions();
       ls.add(batch);		
		
		String status=E_FILEST.SUCC.getValue();
		String descri = "清算并账";
		
		
		bizlog.parm("-------------status[%s]", status);
		bizlog.parm("-------------descri[%s]", descri);
		bizlog.parm("-------------target[%s]", target);
		bizlog.parm("-------------filetp[%s]", filetp);
       bizlog.parm("-------------ls-filename[%s]", filename);
       bizlog.parm("-------------ls-flpath[%s]", path2);
       bizlog.parm("-------------ls-filemd[%s]", md5);		
		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBackNotice(status, descri, target, filetp, ls);
		}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Input input, cn.sunline.ltts.busi.intran.batchfile.intf.Clfilewt.Property property,
			Throwable t) {
		//监控预警
		KnpParameter para = KnpParameterDao.selectOne_odb1("DAYENDNOTICE", "%", "%", "%", true);
		
		String bdid = para.getParm_value1();// 服务绑定ID
		
		String mssdid = CommTools.getMySysId();// 随机生成消息ID
		
		String mesdna = para.getParm_value2();// 媒介名称
		
//		E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介//rambo 
		
		IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
		
		IoCaOtherService.IoCaDayEndFailNotice.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaDayEndFailNotice.InputSetter.class);
		
		String timetm = DateTools2.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS000");
		IoCaBatchWarnInfo content = SysUtil.getInstance(IoCaBatchWarnInfo.class);
		content.setPljioyma("clfilewt");
		content.setPljyzbsh("0306");
		content.setPljyzwmc("清算并账写文件异常预警");
		content.setErrmsg("清算并账写文件失败");
		content.setTrantm(timetm);
		
		// 发送消息
		mqInput.setMsgid(mssdid); // 消息ID
//		mqInput.setMedium(mssdtp); // 消息媒介//rambo delete
		mqInput.setMdname(mesdna); // 媒介名称
		mqInput.setTypeCode("NAS");
		mqInput.setTypeName("网络金融核心平台-电子账户核心系统");
		mqInput.setItemId("NAS_BATCH_WARN");
		mqInput.setItemName("电子账户核心批量执行错误预警");
		
		String str =JSON.toJSONString(content);
		mqInput.setContent(str);
		
		mqInput.setWarnTime(timetm);
		
		caOtherService.dayEndFailNotice(mqInput);
	}

	//就按
	public  InClerckInfo  findClerdtAndClenum(Integer clenum){
		
		
		//一天一个批次
		if(CommUtil.equals(maxcut, "1")){
			//一天一个清算场次，同步的数据都是上日的
			clerdt= BusiTools.getBusiRunEnvs().getLast_date();
		}else if(CommUtil.compare(maxcut, "1")>0){
			//一天多清算场次
			if(CommUtil.compare(clenum, 1)>0){
				//当前清算场次大于0 ，取上一个清算场次的数据
				 clenum--;
				clerdt =  BusiTools.getBusiRunEnvs().getTrxn_date();
			}else{
				//当前清算场次为0，则取上一清算日期最后一个清算场次数据
				
				clenum =Integer.valueOf(maxcut) ;
				clerdt= BusiTools.getBusiRunEnvs().getLast_date();
			}
			
		}	
		InClerckInfo info = SysUtil.getInstance(InClerckInfo.class);
		info.setClerdt(clerdt);
		info.setClenum(clenum);
	   
		return info ;
	}	
}

