
package cn.sunline.edsp.busi.dptran.trans;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.dp.namedsql.ProdClearBatchDao;
import cn.sunline.ltts.busi.dp.type.DpTransfer.DpTrans;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;

public class qutran {

	public static void queryTrans(final cn.sunline.edsp.busi.dptran.trans.intf.Qutran.Input input,
			final cn.sunline.edsp.busi.dptran.trans.intf.Qutran.Property property,
			final cn.sunline.edsp.busi.dptran.trans.intf.Qutran.Output output) {
		
		String flowid = input.getFlowid();
		List<DpTrans> list = null;
		if (CommUtil.isNotNull(flowid)) {
			list = ProdClearBatchDao.selTrans(flowid, false);
			output.getTranList().setValues(list);
			return;
		}
		String inmeid = input.getInmeid();
		if (CommUtil.isNull(inmeid)) {
			throw DpModuleError.DpTrans.TS020013();
		}
		
		String trandt = LocalDate.now().minusMonths(3).format(DateTimeFormatter.ofPattern("yyyyMMdd"));//获取当前日期前三个月时间
		Integer pagenum = (null!=input.getPagenum()?input.getPagenum():1);
		Integer pagesiez = (null!=input.getPagesize()?input.getPagesize():10);
		Page<DpTrans> page = ProdClearBatchDao.selTransList(inmeid, trandt, 
				(pagenum - 1L) * pagesiez, pagesiez, 0, false);
		output.getTranList().addAll(page.getRecords());
		CommTools.getBaseRunEnvs().setTotal_count(page.getRecordCount());
	}
	
	
	
}
