package cn.sunline.ltts.busi.dptran.batchtran;


import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.FileUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.file.FileDataExecutor;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.base.util.security.encrypt.MD5EncryptUtil;
import cn.sunline.ltts.busi.aplt.para.ApBatchFileParams;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplsubDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplsub;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;
	 /**
	  * 卡券文件拆分批量
	  *
	  */

public class cardspDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.intf.Cardsp.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Cardsp.Property> {
  
	private final Map<String, String> fileCache = new HashMap<String, String>();//存放拆分后的文件 <DCN，filepath+filename>
	private final Map<String, BigDecimal> amountCache = new HashMap<String, BigDecimal>();//存放拆分后的文件 <DCN，string[]>
	private final Map<String, StringBuffer> bodyCache = new HashMap<String, StringBuffer>();//存放拆分后的文件 <DCN，String[]>
	private final Map<String, BigDecimal> totalCache = new HashMap<String, BigDecimal>();//存放拆分后的文件 <DCN，String[]>
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.intf.Cardsp.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Cardsp.Property property) {
		 //1、根据文件批次号查询文件流水信息
		 final kapb_wjplxxb wjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), false);
		 final String splitor = "|@|"; //分隔符
		 String remotepath = wjplxxb.getDownph()+wjplxxb.getDownna();//上送nas上的文件
		 String localpath = wjplxxb.getLocaph()+wjplxxb.getDownna();//本地文件
		 String beformd5 = null;
		 try {
			 beformd5 = MD5EncryptUtil.getFileMD5String(new File(wjplxxb.getDownph()+wjplxxb.getDownna()));
		} catch (Exception e) {
			throw ExceptionUtil.wrapThrow(e);
		}
		 //2、下载远程文件到本地目录
		 File file = new File(wjplxxb.getLocaph());
		 if(!file.exists() && file.isDirectory() ){
			 file.mkdirs();  //没有这个目录就创建
		 }
		 FileUtil.copyFile(remotepath, localpath);
		 
		 String afterbmd5 = null;
		 try {
			   afterbmd5 = MD5EncryptUtil.getFileMD5String(new File(wjplxxb.getDownph()+wjplxxb.getDownna()));
		 } catch (Exception e) {
			 throw ExceptionUtil.wrapThrow(e);
		 }
		 
		 if(!CommUtil.equals(beformd5, afterbmd5)){
			 throw ExceptionUtil.wrapThrow("远程文件下载到本地MD5校验异常");
		 }
		 
		 //3、读取并拆分文，生产子文件
		 FileUtil.readFile(localpath,new FileDataExecutor() {
			   
				@Override
				public void process(int arg0, String arg1) {
					
					if(arg0!=1){//文件头不处理
					   
					    String ss[] = arg1.split("\\|@\\|"); 
					    //String dcn = DcnUtil.findDcnNoByCardNo(ss[13]);
					    String dcn = "000";
					    String filepath = wjplxxb.getLocaph()+wjplxxb.getDownna()+"_"+dcn;
					    BigDecimal amt = new BigDecimal(ss[9]);
					    if(CommUtil.isNull(totalCache.get(dcn)) || CommUtil.isNull(amountCache.get(dcn))){
					    	amountCache.put(dcn,new BigDecimal(BigInteger.ZERO));
					    	totalCache.put(dcn, new BigDecimal(BigInteger.ZERO));
					    }
					    amountCache.put(dcn, amountCache.get(dcn).add(amt)); //统计每个dcn的金额
						fileCache.put(dcn, filepath); //存放待上传的文件路径信息
						totalCache.put(dcn, totalCache.get(dcn).add(new BigDecimal(1))); //统计各dnc的记录条数
						if(CommUtil.isNull(bodyCache.get(dcn))){
							bodyCache.put(dcn, new StringBuffer(arg1+"\n"));
						}else{
							bodyCache.put(dcn, bodyCache.get(dcn).append(arg1).append("\n"));// 存储各DCN文件内容 TODO 待考虑是否有超大文件
						}
						
					}
				}
				
			},"UTF-8");
		 
		 
		 //4、上传拆分后的子文件到NAS并登记文件子表信息
		 Iterator<Map.Entry<String, String>> it = fileCache.entrySet().iterator();
		  while (it.hasNext()) {
			
		   Map.Entry<String, String> entry = it.next();
		   String fileContxt = totalCache.get(entry.getKey()) + splitor +amountCache.get(entry.getKey())+"\n";
		   FileUtil.writeToFile(entry.getValue(), new StringBuffer(fileContxt).append(bodyCache.get(entry.getKey())).toString(), "UTF-8",true);  
	       //4.1 上传文件
		   KnpParameter para = KnpParameterDao.selectOne_odb1("Batch.File", "%", "%", "%", true);
		   String upfeph = para.getParm_value1()+File.separator+E_SYSCCD.NAS+File.separator+E_SYSCCD.NAS+File.separator+CommTools.getBaseRunEnvs().getTrxn_date()+File.separator;
		   String filepath = upfeph+wjplxxb.getDownna()+"_"+entry.getKey();
		   File file1 = new File(upfeph);
			 if(!file1.exists() && file1.isDirectory()  ){
				 file1.mkdirs(); //没有就创建目录
			 }
			 
			 FileUtil.copyFile(entry.getValue(), filepath);  
		   //4.2 登记文件子表信息
			kapb_wjplsub wjplsub = SysUtil.getInstance(kapb_wjplsub.class);
			CommUtil.copyProperties(wjplsub, wjplxxb);
			try {
				wjplsub.setDownm5(MD5EncryptUtil.getFileMD5String(new File(filepath)));
			} catch (Exception e) {
				throw ExceptionUtil.wrapThrow(e);
			}
			wjplsub.setTdcnno(entry.getKey());
			wjplsub.setDownna(wjplxxb.getDownna()+"_"+entry.getKey());
			wjplsub.setSubbno(wjplxxb.getBtchno() + "-" + entry.getKey()); // 固定子批次号
			
			JSONObject obj = JSON.parseObject(wjplsub.getFiletx());
			obj.getString(ApBatchFileParams.BATCH_PMS_FILESQ);
			obj.put(ApBatchFileParams.BATCH_PMS_FILESQ, wjplxxb.getBtchno() + "-" + entry.getKey());
			String filetx = JSON.toJSONString(obj);
			wjplsub.setUpfena("");// 由各个零售节点返回文件名字
			wjplsub.setUpfeph(upfeph);  //拆分后的文件路径
			wjplsub.setFiletx(filetx);
			wjplsub.setBtfest(E_BTFEST.WAIT_DISTRIBUTE); //拆分完成，更新状态为待下发，由定时任务完成扫描下发。
			Kapb_wjplsubDao.insert(wjplsub);
		  }
		 
	}

}


