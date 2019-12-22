package cn.sunline.ltts.busi.dptran.trans;

import java.io.File;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;

//import com.alibaba.druid.sql.visitor.functions.Substring;


import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapot;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;


public class cnaphv {
	private static final BizLog bizlog = BizLogUtil.getBizLog(cnaphv.class);
	private static final String fengefu = "|";
    private static String trandt = DateTools2.getDateInfo().getSystdt();
    private static String trantm = DateTools2.getDateInfo().getTrantm().substring(0, 14);
//    private static String transq = CommTools.getBaseRunEnvs().getBstrsq();
    // 获取文件名
    private static String filename1 = "KnlCnapot_0_"+trantm+".txt";
    private static String filename2 = "KnlCnapot_1_"+trantm+".txt";
    private static String filename3 = "KnlCnapot"+trantm+".ok";
//  private static String filePath = KnpParameterDao.selectOne_odb1("DPTRAN", "DPFILE","%", "%",true).getParm_value1() + trandt + "\\";

    private static KnpParameter tbKnpParameter = KnpParameterDao.selectOne_odb1("DPTRAN", "DPFILE", "%", "%", true);
    //大额对账下载路径
    private static String filePath1 = tbKnpParameter.getParm_value1()+ E_FILETP.DP020800 + File.separator 
			+ CommTools.getBaseRunEnvs().getTrxn_date() + File.separator;	
	//小额对账下载路径
    private static String filePath2 = tbKnpParameter.getParm_value1()+ E_FILETP.DP020700 + File.separator 
			+ CommTools.getBaseRunEnvs().getTrxn_date() + File.separator;	

public static void wtcnaphv( String subsys,  String chfcnb,  
		final cn.sunline.ltts.busi.dptran.trans.intf.Cnaphv.Property property,  
		final cn.sunline.ltts.busi.dptran.trans.intf.Cnaphv.Output output){
	
	if(CommUtil.isNull(subsys)){
		throw DpModuleError.DpstComm.BNAS1361();
	}
	if(CommUtil.isNull(chfcnb)){
		throw DpModuleError.DpstComm.BNAS1362();
	}
	
	List<KnlCnapot> lstknlcnapol = DpDayEndDao.selKnlCnapotData(subsys, chfcnb, false);
	
	if(CommUtil.equals(subsys, "0")){//大额支付
				
		if(lstknlcnapol.size()>0){
			//生成大额对账文件
			writeCnaphvFile(chfcnb);
			afterTranProcess(subsys);	
		}else{
			throw DpModuleError.DpstComm.BNAS1363();
		}	
	}else if(CommUtil.equals(subsys, "1")){//小额支付
				
		if(lstknlcnapol.size()>0){
			//生成小额对账文件
			writeCnapbeFile(chfcnb);
			afterTranProcess(subsys);
		}else{
			throw DpModuleError.DpstComm.BNAS1364();
		}		
	}else{
		throw DpModuleError.DpstComm.BNAS1365();
	}
	
	if(CommUtil.equals(subsys, "0")){
		output.setFilena(filename1);
	}else if(CommUtil.equals(subsys, "1")){
		output.setFilena(filename2);
	}
	output.setTrandt(trandt);
//	output.setTrsnsq(transq);
}


	private static void writeCnaphvFile(String chfcnb) {	        
	    // 获取文件生产路径
	    String path1 = filePath1;
	   
	    bizlog.debug("文件产生路径 path:[" + path1 + "]");
	    
	    bizlog.debug("文件名称 filename:[" + filename1 + "]");
	    // 获取是否产生文件标志
	    String isCreateFlg1 = "Y";
	    bizlog.debug("文件产生标志 :[" + isCreateFlg1 + "]");
	    
	    if (CommUtil.equals(isCreateFlg1, "Y")) {
	    	final LttsFileWriter file = new LttsFileWriter(path1, filename1);
	//        List<cif_cust> entities = null;
	    	
	    	
	        Params params = new Params();
	        params.put("chfcnb", chfcnb);
	        params.put("subsys", "0");
	        String namedSqlId = "";//查询数据集的命名sql
	        
	        namedSqlId = DpDayEndDao.namedsql_selKnlCnapotData;	            
	        
	        if (true) {
	        	file.open();
	        	try {
	        		DaoUtil.selectList(namedSqlId, params, new CursorHandler<KnlCnapot>() {
	
						@Override
						public boolean handle(int index, KnlCnapot entity) {
							// 写文件
							StringBuffer file_Info = SysUtil.getInstance(StringBuffer.class);//拼接字符串
							String iotype = (CommUtil.isNotNull(entity.getIotype().getValue()) ? entity.getIotype().getValue() : "");
							String msetdt = (CommUtil.isNotNull(entity.getMsetdt()) ? entity.getMsetdt() : "");
							String msetsq = (CommUtil.isNotNull(entity.getMsetsq()) ? entity.getMsetsq() : "");
							String crdbtg = (CommUtil.isNotNull(entity.getCrdbtg()) ? entity.getCrdbtg() : "");
							String mesgtp = (CommUtil.isNotNull(entity.getMesgtp()) ? entity.getMesgtp() : "");
							String pyercd = (CommUtil.isNotNull(entity.getPyercd()) ? entity.getPyercd() : "");
							String pyerac = (CommUtil.isNotNull(entity.getPyerac()) ? entity.getPyerac() : "");
							String pyeecd = (CommUtil.isNotNull(entity.getPyeecd()) ? entity.getPyeecd() : "");
							String pyeeac = (CommUtil.isNotNull(entity.getPyeeac()) ? entity.getPyeeac() : "");
							String tranam = (CommUtil.isNotNull(entity.getTranam()) ? entity.getTranam().toString() : "");
							String keepdt = (CommUtil.isNotNull(entity.getKeepdt()) ? entity.getKeepdt() : "");
							/**
							 * mod 报错，临时注释
							 */
//							String frondt = (CommUtil.isNotNull(entity.getFrondt()) ? entity.getFrondt() : "");
//							String fronsq = (CommUtil.isNotNull(entity.getFronsq()) ? entity.getFronsq() : "");
	
							//String timetm = (CommUtil.isNotNull(entity.getTimetm()) ? entity.getTimetm() : "");
							
							//字符串拼接
							file_Info.append(iotype).append(fengefu).append(msetdt).append(fengefu).append(msetsq).append(fengefu).append(crdbtg);
							file_Info.append(fengefu).append(mesgtp).append(fengefu).append(pyercd).append(fengefu).append(pyerac).append(fengefu).append(pyeecd);
							file_Info.append(fengefu).append(pyeeac).append(fengefu).append(tranam).append(fengefu).append(keepdt).append(fengefu)
							/**
							 * mod 报错，临时注释
							 */
//							.append(frondt).append(fengefu).append(fronsq)
							;
	
							//打印文件
	                        file.write(file_Info.toString());
							return true;
						}});
	        	} finally {
	                file.close();
	            }
	        }
	    }   
	}

	private static void writeCnapbeFile(String chfcnb) {	        
	    // 获取文件生产路径
	    String path1 = filePath2;
	   
	    bizlog.debug("文件产生路径 path:[" + path1 + "]");
	    
	    bizlog.debug("文件名称 filename:[" + filename2 + "]");
	    // 获取是否产生文件标志
	    String isCreateFlg1 = "Y";
	    bizlog.debug("文件产生标志 :[" + isCreateFlg1 + "]");
	    
	    if (CommUtil.equals(isCreateFlg1, "Y")) {
	    	final LttsFileWriter file = new LttsFileWriter(path1, filename2);	    	
	    	
	        Params params = new Params();
	        params.put("chfcnb", chfcnb);
	        params.put("subsys", "1");
	        String namedSqlId = "";//查询数据集的命名sql
	        
	        namedSqlId = DpDayEndDao.namedsql_selKnlCnapotData;	            
	        
	        if (true) {
	        	file.open();
	        	try {
	        		DaoUtil.selectList(namedSqlId, params, new CursorHandler<KnlCnapot>() {
	
						@Override
						public boolean handle(int index, KnlCnapot entity) {
							// 写文件
							StringBuffer file_Info = SysUtil.getInstance(StringBuffer.class);//拼接字符串
							String iotype = (CommUtil.isNotNull(entity.getIotype().getValue()) ? entity.getIotype().getValue() : "");
							String msetdt = (CommUtil.isNotNull(entity.getMsetdt()) ? entity.getMsetdt() : "");
							String msetsq = (CommUtil.isNotNull(entity.getMsetsq()) ? entity.getMsetsq() : "");
							String crdbtg = (CommUtil.isNotNull(entity.getCrdbtg()) ? entity.getCrdbtg() : "");
							String mesgtp = (CommUtil.isNotNull(entity.getMesgtp()) ? entity.getMesgtp() : "");
							String pyercd = (CommUtil.isNotNull(entity.getPyercd()) ? entity.getPyercd() : "");
							String pyerac = (CommUtil.isNotNull(entity.getPyerac()) ? entity.getPyerac() : "");
							String pyeecd = (CommUtil.isNotNull(entity.getPyeecd()) ? entity.getPyeecd() : "");
							String pyeeac = (CommUtil.isNotNull(entity.getPyeeac()) ? entity.getPyeeac() : "");
							String tranam = (CommUtil.isNotNull(entity.getTranam()) ? entity.getTranam().toString() : "");
							String pakgdt = (CommUtil.isNotNull(entity.getPakgdt()) ? entity.getPakgdt() : "");
							String pakgsq = (CommUtil.isNotNull(entity.getPakgsq()) ? entity.getPakgsq() : "");
							String keepdt = (CommUtil.isNotNull(entity.getKeepdt()) ? entity.getKeepdt() : "");
							String npcpdt = (CommUtil.isNotNull(entity.getNpcpdt()) ? entity.getNpcpdt() : "");
							String npcpbt = (CommUtil.isNotNull(entity.getNpcpbt()) ? entity.getNpcpbt() : "");
							String reproc = (CommUtil.isNotNull(entity.getPrcscd()) ? entity.getPrcscd() : "");
	
							//String timetm = (CommUtil.isNotNull(entity.getTimetm()) ? entity.getTimetm() : "");
							
							//字符串拼接
							file_Info.append(iotype).append(fengefu).append(msetdt).append(fengefu).append(msetsq).append(fengefu).append(crdbtg);
							file_Info.append(fengefu).append(mesgtp).append(fengefu).append(pyercd).append(fengefu).append(pyerac).append(fengefu).append(pyeecd);
							file_Info.append(fengefu).append(pyeeac).append(fengefu).append(tranam).append(fengefu).append(pakgdt).append(fengefu).append(pakgsq).append(fengefu).append(keepdt).append(fengefu).append(npcpdt).append(fengefu).append(npcpbt).append(fengefu).append(reproc);
	
							//打印文件
	                        file.write(file_Info.toString());
							return true;
						}});
	        	} finally {
	                file.close();
	            }
	        }
	    }	    
	}
	
	private static void afterTranProcess(String subsys) {
		    String filenm = filename3;
		    LttsFileWriter file = SysUtil.getInstance(LttsFileWriter.class);
		    if(CommUtil.equals(subsys, "0")){
		    	file = new LttsFileWriter(filePath1, filenm, "UTF-8");
			}else if(CommUtil.equals(subsys, "1")){
				file = new LttsFileWriter(filePath2, filenm, "UTF-8");
			}
							
			file.open();				
			try{								
				String ret  ="";					
				file.write(ret);					
			}finally{
			    if(file != null){
			        try {
                        file.close();
                    } catch (Exception e) {
                    	bizlog.debug("[%s]", e);
//                        e.printStackTrace();
                    }
			    }
		}		
	}

}
