package cn.sunline.ltts.busi.dptran.batchtran;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.para.ApBatchFileParams;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplsubDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplsub;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
	 /**
	  * 营销活动拆分批量
	  *
	  */

public class actdspDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.intf.Actdsp.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Actdsp.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.intf.Actdsp.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Actdsp.Property property) {
		  
		  // 没有上送文件，直接登记文件子信息表，等待定时任务下发到各个零售节点
		    kapb_wjplxxb wjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), false);
		 
//		    List<String> dcns = DcnUtil.findAllRDcnNos();
//		    for(String dnc:dcns){
		    kapb_wjplsub wjplsub = SysUtil.getInstance(kapb_wjplsub.class);
			
		    CommUtil.copyProperties(wjplsub, wjplxxb);
			
			wjplsub.setSubbno(wjplxxb.getBtchno() + "-" + "000"); // 固定子批次号
			
			JSONObject obj = JSON.parseObject(wjplsub.getFiletx());
			
			obj.put("syunum", String.valueOf(Integer.valueOf(obj.getString("syunum"))/3));
			obj.put(ApBatchFileParams.BATCH_PMS_FILESQ, wjplxxb.getBtchno() + "-" + "000");
			
			String filetx = JSON.toJSONString(obj);
			
			wjplsub.setUpfena("");// 由各个零售节点返回文件名字
			wjplsub.setTotanm(Long.valueOf((String) obj.get("syunum")));
			wjplsub.setFiletx(filetx);
			wjplsub.setTdcnno("000");
			wjplsub.setBtfest(E_BTFEST.WAIT_DISTRIBUTE); //拆分完成，更新状态为待下发，由定时任务完成扫描下发。
			Kapb_wjplsubDao.insert(wjplsub);
			
//		    }
	}

}


