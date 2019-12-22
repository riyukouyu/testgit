package cn.sunline.ltts.busi.intran.batchtran.dayend;


import com.alibaba.fastjson.JSON;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaBatchWarnInfo;

	 /**
	  * 清算并账异常情况预警
	  *
	  */

public class chckclDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Chckcl.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Chckcl.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Chckcl.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Chckcl.Property property) {
		
		 int count = InDayEndSqlsDao.selKnsAcsqCollStatus(false);
		 if(count>0){
				//存在清算异常记录通知监控预警平台
				KnpParameter para = KnpParameterDao.selectOne_odb1("DAYENDNOTICE", "%", "%", "%", true);
				
				String bdid = para.getParm_value1();// 服务绑定ID
				
				String mssdid = CommTools.getMySysId();// 随机生成消息ID
				
				String mesdna = para.getParm_value2();// 媒介名称
				
//				E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介 // rambo delete
				
				IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
				
				IoCaOtherService.IoCaDayEndFailNotice.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaDayEndFailNotice.InputSetter.class);
				
				String timetm = DateTools2.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS000");
				IoCaBatchWarnInfo content = SysUtil.getInstance(IoCaBatchWarnInfo.class);
				content.setPljioyma("chckcl");
				content.setPljyzbsh("0315");
				content.setPljyzwmc("清算并账异常情况预警");
				content.setErrmsg("清算并账异常，未收到柜面核心记账返回");
				content.setTrantm(timetm);
				
				// 发送消息
				mqInput.setMsgid(mssdid); // 消息ID
//				mqInput.setMedium(mssdtp); // 消息媒介//rambo delete
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
	}

}


