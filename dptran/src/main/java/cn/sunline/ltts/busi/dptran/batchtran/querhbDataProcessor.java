package cn.sunline.ltts.busi.dptran.batchtran;

import java.io.File;
import java.util.List;

import cn.sunline.adp.cedar.busi.sdk.biz.global.FileUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.edsp.base.file.FileDataExecutor;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.base.util.security.encrypt.MD5EncryptUtil;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BatchFileSubmit;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplsubDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplsub;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.FileBatchTools;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatch;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;
	 /**
	  * 查询产品提前支取状态合并批量
	  *
	  */

public class querhbDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.intf.Querhb.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Querhb.Property> {
  
	private final StringBuffer bodyCache = new StringBuffer();//存放拆分后的文件 <DCN，String[]>
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.intf.Querhb.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Querhb.Property property) {

		 long totanm = 0; //总笔数
	     
		 //1、根据主批次号查询拆分后的子文件处理信息
		 List<kapb_wjplsub> wjplsubs = Kapb_wjplsubDao.selectAll_odb5(input.getFilesq(),E_BTFEST.WAIT_MERGE, false);
		 
		 //2、下载各个待合并的文件到本地
		 for(kapb_wjplsub wjplsub:wjplsubs){
			 
			 totanm = totanm + wjplsub.getTotanm();
			 
			 File file = new File(wjplsub.getLocaph());
			 if(!file.exists() && file.isDirectory() ){
				 file.mkdirs();  //没有这个目录就创建
			 }
			 
			 FileUtil.copyFile(wjplsub.getUpfeph()+wjplsub.getUpfena(), wjplsub.getLocaph()+wjplsub.getUpfena());
			 
			 //3、读取文件
			 FileUtil.readFile(wjplsub.getLocaph()+wjplsub.getUpfena(), new FileDataExecutor() {
				
				@Override
				public void process(int arg0, String arg1) {
					if( arg0 != 1){
						bodyCache.append(arg1).append("\n");
					}
				}
			},"UTF-8");
		 }
		 
		 kapb_wjplxxb wjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), false);
		 String header = totanm + "\n";
		 
		 //4、写合并后的文件
		 FileUtil.writeToFile(wjplxxb.getLocaph()+wjplxxb.getUpfena(), new StringBuffer(header).append(bodyCache).toString(), "UTF-8", true);
		 
		 //5、上传到NAS
		 File file = new File(wjplxxb.getUpfeph());
		 if(!file.exists() && file.isDirectory() ){
			 file.mkdirs();  //没有这个目录就创建
		 }
		 
		 FileUtil.copyFile(wjplxxb.getLocaph()+wjplxxb.getUpfena(), wjplxxb.getUpfeph()+wjplxxb.getUpfena());
		 
		 
		 //6、更新文件信息子表文件状态为交易成功
		 List<kapb_wjplsub> wjsubs = Kapb_wjplsubDao.selectAll_odb5(input.getFilesq(),E_BTFEST.WAIT_MERGE, false);
		 for(kapb_wjplsub wjplsub:wjsubs){
			 wjplsub.setBtfest(E_BTFEST.SUCC);
			 Kapb_wjplsubDao.updateOne_odb1(wjplsub);
		 }
		 //7、更新文件信息表文件状态为交易成功
		 
		 wjplxxb.setTotanm(totanm);
		 wjplxxb.setBtfest(E_BTFEST.SUCC);
		 Kapb_wjplxxbDao.updateOne_odb1(wjplxxb);
		 
         // 通知外围系统
			KnpParameter para = KnpParameterDao.selectOne_odb1("Batch.File", "%", "%", "%", true);
			DefaultOptions<BatchFileSubmit> ls = new DefaultOptions<BatchFileSubmit>();
			BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
			batch.setFilenm(wjplxxb.getUpfena());
			batch.setFlpath(FileBatchTools.subPathByString(para.getParm_value1(), wjplxxb.getUpfeph()));
			String pathname = wjplxxb.getUpfeph() + wjplxxb.getUpfena();
			try {
				batch.setFilemd(MD5EncryptUtil.getFileMD5String(new File(pathname)));
			} catch (Exception e) {
				throw ExceptionUtil.wrapThrow(e);
			}
			ls.add(batch);
			E_SYSCCD target = E_SYSCCD.CMP;
			String status = E_FILEST.SUCC.getValue();
			String descri = "查询批量产品状态反盘文件生产通知";
			
			SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBackNotice(
					status, descri, target, wjplxxb.getFiletp(), ls);
	}

}


