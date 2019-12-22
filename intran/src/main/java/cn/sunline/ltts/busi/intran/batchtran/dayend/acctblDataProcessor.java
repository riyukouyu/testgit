package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.io.File;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.in.excel.AcctBalExcelWriter;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.type.InExcelTypes.AcctBalInfo;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
	 /**
	  * 客户余额统计
	  *
	  */

public class acctblDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acctbl.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acctbl.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(acctblDataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acctbl.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acctbl.Property property) {
		 bizlog.debug("---------开始客户余额统计-------");
			String trandt = CommTools.getBaseRunEnvs().getLast_date();
			List<AcctBalInfo> dataList = InQuerySqlsDao.selAcctBalInfos(false);
			String bizCalFilePath = "/share/NAS/BIZ/" + trandt + "/";
			String bizCalFileName = "ACCT_BAL_" + trandt + ".xlsx";
			File filePath = new File(bizCalFilePath);
			if(!filePath.exists()) {
				filePath.mkdirs();
			}
			bizlog.debug("---------导入到excel-------");
			File file = new File(bizCalFilePath + bizCalFileName);
			try {
				String sheetName = "余额表";
				AcctBalExcelWriter writer = new AcctBalExcelWriter(file,sheetName);
				writer.Write(dataList,sheetName);
				writer.Extract(); 
			} catch (Exception e) {
				bizlog.debug("[%s]", e);
//				e.printStackTrace();
			}
	}

}


