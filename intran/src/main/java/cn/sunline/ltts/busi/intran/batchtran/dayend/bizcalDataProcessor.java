package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.in.excel.BizCalExcelWriter;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.type.InExcelTypes.BizCalInfo;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Bizcal.Input;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Bizcal.Property;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 生成业务量统计数据
 * 
 */

public class bizcalDataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Bizcal.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Bizcal.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(bizcalDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Bizcal.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Bizcal.Property property) {
		bizlog.debug("---------开始进行计业务量统计数据-------");
		String trandt = CommTools.getBaseRunEnvs().getLast_date();
		List<BizCalInfo> dataList = InQuerySqlsDao.selBizCalInfos(trandt, false);
		Iterator<BizCalInfo> it = dataList.iterator();
		while(it.hasNext()){
			BizCalInfo info = it.next();
		    if(CommUtil.isNull(info.getPrcscd()) || CommUtil.isNull(info.getPrcsna())){
		        it.remove();
		    }
		}
		// update by liyc 20180728 修改参数可配置 begin
		KnpParameter tblknp = KnpParameterDao.selectOne_odb1("BIZ_FILE", "BIZCAL", "%", "%", true);
		/*String bizCalFilePath = "/share/NAS/BIZ/" + trandt + "/";
		 String bizCalFileName = "BIZ_CAL_" + trandt + ".xlsx";*/
		String bizCalFilePath = tblknp.getParm_value1() + trandt + "/";
		String bizCalFileName = tblknp.getParm_value2() + trandt + tblknp.getParm_value3();
		// update by liyc 20180728 修改参数可配置 end
		File filePath = new File(bizCalFilePath);
		if(!filePath.exists()) {
			filePath.mkdirs();
		}
		bizlog.debug("---------导入到excel-------");
		File file = new File(bizCalFilePath + bizCalFileName);
		try {
			BizCalExcelWriter writer = new BizCalExcelWriter(file);
			writer.Write(dataList);
			writer.Extract(); 
		} catch (Exception e) {
			bizlog.debug("[%s]", e);
//			e.printStackTrace();
		}
	}

	@Override
	public void afterTranProcess(String taskId, Input input, Property property) {
		super.afterTranProcess(taskId, input, property);
	}

}
