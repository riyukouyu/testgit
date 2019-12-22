
package cn.sunline.edsp.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.busi.dp.namedsql.ca.EdmAfterDayBatchDao;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoSettleInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoSettleSumInfo;

/**
 * <p>
 * 文件功能说明：结算单批次信息查询
 * 
 * </p>
 * 
 * @Author songlw
 *         <p>
 *         <li>2019年10月28日-下午6:54:45</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2019年10月28日</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class qrstbt {

	/**
	 * @Author songlw
	 *         <p>
	 *         <li>2019年10月28日-下午6:54:49</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void qrstbt(final cn.sunline.edsp.busi.dptran.trans.intf.Qrstbt.Input input,
			final cn.sunline.edsp.busi.dptran.trans.intf.Qrstbt.Property property,
			final cn.sunline.edsp.busi.dptran.trans.intf.Qrstbt.Output output) {
		Long pageNo = CommTools.getBaseRunEnvs().getPage_start();
		Long pageSize = CommTools.getBaseRunEnvs().getPage_size();

		Page<IoSettleInfo> pgeSetActInfo = EdmAfterDayBatchDao.selSettleActControlInfo(input.getTransq(),
				input.getSetkid(), input.getSutkid(), input.getTranst(), input.getStatdt(), input.getEndxdt(),
				(pageNo - 1) * pageSize, pageSize, CommTools.getBaseRunEnvs().getTotal_count(), false);

		IoSettleSumInfo info = EdmAfterDayBatchDao.selSettleActSumlInfo(input.getTransq(), input.getSetkid(),
				input.getSutkid(), input.getTranst(), input.getStatdt(), input.getEndxdt(), false);
		if (CommUtil.isNotNull(pgeSetActInfo) && pgeSetActInfo.getRecordCount() > 0) {
			output.getSetInfo().addAll(pgeSetActInfo.getRecords());
		}
		CommTools.getBaseRunEnvs().setTotal_count(pgeSetActInfo.getRecordCount());
		output.setSuccnm(info.getSuccnm());
		output.setSuccam(info.getSuccam());
		output.setTotanm(info.getTotanm());
		output.setTotlmt(info.getTotlam());
	}
}
