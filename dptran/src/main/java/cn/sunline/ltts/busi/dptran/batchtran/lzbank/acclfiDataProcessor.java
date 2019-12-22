package cn.sunline.ltts.busi.dptran.batchtran.lzbank;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.ca.namedsql.CaBatchTransDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.AcctClerInfos;
import cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Acclfi.Input;
import cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Acclfi.Property;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.KnsAcsqColl;
import cn.sunline.ltts.busi.in.tables.In.KnsAcsqCollDao;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.sys.type.InEnumType;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.edsp.base.lang.Params;
	 /**
	  * 来往账清算汇总及文件生成
	  *
	  */

public class acclfiDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Acclfi.Input, cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Acclfi.Property> {
	//private static final BizLog bizlog = cn.sunline.ltts.busi.aplt.tools.BizLogUtil.getBizLog(acclfiDataProcessor.class);
	
//	private static final String fileSepa1 = "|";//文件分隔符
//  private static final String fileSepa1 = "^";
//  private static final String fileSepa1 = "|$|";
	private static final String fileSepa1 = "~";
	
	
	String trandt = DateTools2.getDateInfo().getSystdt();//交易日期
	String lstrdt = DateTools2.getDateInfo().getLastdt();//上次交易日期
	
//	//获取清算日期参数
//	AppCldt tblAppCldt = AppCldtDao.selectOne_odb1(CommTools.getBaseRunEnvs().getBusi_org_id(), false);
	
	//获取文件参数
	KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "file", "acclfi", "%", true);
	String path = para.getParm_value1();
	String fileName = para.getParm_value2()+trandt+para.getParm_value3();
	String encoding = para.getParm_value5();//文件编码
	
	
	/**
	 * 清算汇总
	 */
	@Override
	public void beforeTranProcess(String taskId, Input input, Property property) {
//		if(CommUtil.isNull(tblAppCldt)){
//			throw DpModuleError.DpstComm.E9999("清算日期参数[app_cldt]未配置");
//		}
		
		//获取当日 
		List<AcctClerInfos> clerInfos = CaBatchTransDao.selAcctClerFromKnsAcsqCler(lstrdt, false);
		
		if(CommUtil.isNotNull(clerInfos)){
			for(AcctClerInfos clerInfo:clerInfos){
			    
				KnsAcsqColl entity = SysUtil.getInstance(KnsAcsqColl.class); 
				entity.setBathid("LZB"+trandt+BusiTools.getSequence("cler_bathid", 8));//批次号  如：LZB2018051100000001       
				entity.setClerdt(trandt);//清算日期      
//				entity.setClenum();//清算场次      
				entity.setTrandt(lstrdt);//记账日期      
				entity.setBrchno(clerInfo.getAcctbr());//机构号        
				entity.setCrcycd(clerInfo.getCrcycd());//币种          
				entity.setAcctno(clerInfo.getAcctno());//内部户账号    
				entity.setProdcd(clerInfo.getProdcd());//产品编号      
				entity.setClactp(clerInfo.getClactp());//系统内账号类型
				entity.setToacct(clerInfo.getToacct());//对方账号      
				entity.setDranam(CommUtil.compare(clerInfo.getSumamt(), BigDecimal.ZERO) >=0?
						clerInfo.getSumamt():BigDecimal.ZERO);//借方金额      
//				entity.setDrcunt();//借方笔数      
				entity.setCranam(CommUtil.compare(clerInfo.getSumamt(), BigDecimal.ZERO) <0?
						clerInfo.getSumamt().multiply(new BigDecimal(-1)):BigDecimal.ZERO);//贷方金额      
//				entity.setCrcunt();//贷方笔数
				if(CommUtil.compare(clerInfo.getSumamt(), BigDecimal.ZERO)!=0){
				    entity.setStatus(InEnumType.E_CLERST._0);//状态       
				}else{
				    //金额为0时不需要去清算
				    entity.setStatus(InEnumType.E_CLERST._4);
				}   
//				entity.setCorest();//柜面记账状态  
//				entity.setTransq();//网络核心流水号
//				entity.setFailrs();//失败原因       
				KnsAcsqCollDao.insert(entity);
			    
			}
		}

		//清算汇总后，更新清算明细的同步状态为已同步
		CaBatchTransDao.updClerstFromKnsAcsqCler(lstrdt);
		
		super.beforeTranProcess(taskId, input, property);
	}
	
	
	/**
	 * 生成清算文件
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Acclfi.Input input, cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Acclfi.Property property) {
		//查询条件
		Params params = new Params();
		String namedsql =InacSqlsDao.namedsql_selKnsAcsqCollByStatus;
		final LttsFileWriter file = new LttsFileWriter(path, fileName,encoding);
    	file.open();
    	try {
    		DaoUtil.selectList(namedsql, params, new CursorHandler<KnsAcsqColl>() {
    			@Override
    			public boolean handle(int arg0, KnsAcsqColl arg1) {
    				StringBuffer lineInfo = SysUtil.getInstance(StringBuffer.class);
                    lineInfo.append(arg1.getBathid()).append(fileSepa1);//批次号        
                    lineInfo.append(arg1.getClerdt()).append(fileSepa1);//清算日期      
//                    lineInfo.append(arg1.getClenum()).append(fileSepa1);//清算场次      
                    lineInfo.append(arg1.getTrandt()).append(fileSepa1);//记账日期                       
                    lineInfo.append(arg1.getBrchno()).append(fileSepa1);//机构号        
                    lineInfo.append(arg1.getCrcycd()).append(fileSepa1);//币种                           
                    lineInfo.append(arg1.getAcctno()).append(fileSepa1);//内部户账号    
                    lineInfo.append(arg1.getProdcd()).append(fileSepa1);//产品编号                       
                    lineInfo.append(arg1.getClactp()).append(fileSepa1);//系统内账号类型
                    lineInfo.append(arg1.getToacct()).append(fileSepa1);//对方账号                       
                    lineInfo.append(arg1.getDranam()).append(fileSepa1);//借方金额      
//                    lineInfo.append(arg1.getDrcunt()).append(fileSepa1);//借方笔数                       
                    lineInfo.append(arg1.getCranam()).append(fileSepa1);//贷方金额      
//                    lineInfo.append(arg1.getCrcunt());//贷方笔数                       
//                    lineInfo.append(arg1.getStatus()).append(fileSepa1);//状态                           
//                    lineInfo.append(arg1.getCorest()).append(fileSepa1);//柜面记账状态  
//                    lineInfo.append(arg1.getTransq()).append(fileSepa1);//网络核心流水号                 
//                    lineInfo.append(arg1.getFailrs());//失败原因                      
                    file.write(lineInfo.toString());
    				return true;
    			}
    		});
		} finally{
			file.close();
		}
	}

	 
	 /**
	  * 外调通知支付系统
	  */
	@Override
	public void afterTranProcess(String taskId, Input input, Property property) {
//		String status=E_FILEST.SUCC.getValue();//状态1
//		String descri = "清算并账";//描述2
//        E_SYSCCD target = E_SYSCCD.IPPS;//目标系统-支付系统3
//		E_FILETP filetp = E_FILETP.IN030600;//文件类型-清算并账 4
//		DefaultOptions<BatchFileSubmit> ls = new DefaultOptions<BatchFileSubmit>();//文件集合5
//	    
//		//获取MD5值(文件集合)
//	    String md5 = ""; 		    
//	    try {
//	    	MD5EncryptUtil.getFileMD5String(new File(path.concat(File.separator).concat(fileName)));
//		} catch (Exception e) {
//			throw ApError.BusiAplt.E0042(fileName);
//		}
//
//	    //附加信息(文件集合)
//	    Map<String,String> map = new HashMap<String,String>();
//	    map.put("BGR04DATE1", trandt);
//        map.put("BGR04BTNO1", null);
//        map.put("BGR04CNT1", null);
//       
//        //组装文件集合
//	    BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
//	    batch.setFlpath(path);//文件路径
//	    batch.setFilenm(fileName);//文件名	    
//	    batch.setFilemd(md5);//文件md5标志
//	    batch.setParams(JSON.toJSONString(map));//附加参数
//	    ls.add(batch);		
//		
//		bizlog.parm("-------------status[%s]", status);
//		bizlog.parm("-------------descri[%s]", descri);
//		bizlog.parm("-------------target[%s]", target);
//		bizlog.parm("-------------filetp[%s]", filetp);
//		bizlog.parm("-------------ls-filename[%s]", fileName);
//		bizlog.parm("-------------ls-flpath[%s]", path);
//		bizlog.parm("-------------ls-filemd[%s]", md5);		
//		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBackNotice(status, descri, target, filetp, ls);
		
		super.afterTranProcess(taskId, input, property);
	}

}


