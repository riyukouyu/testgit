package cn.sunline.ltts.busi.intran.batchtran.dayend;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;

/**
 * 备份会计流水，删除30天前数据
 * 
 */

public class bkacsqDataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Bkacsq.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Bkacsq.Property> {

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Bkacsq.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Bkacsq.Property property) {
		String lstrdt = CommTools.getBaseRunEnvs().getLast_date();
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		//备份上日会计流水(kns_acsq备份到h_kns_acsq)
		InacSqlsDao.backupKnsAcsq(lstrdt,corpno);

		//默认删除30天前数据(kns_acsq)
		String delDate = null;
		KnpParameter para = KnpParameterDao.selectOne_odb1("DELETE_DATA_DAYS", "kns_acsq", "%", "%", false);
		if(CommUtil.isNotNull(para) && CommUtil.isNotNull(para.getParm_value1())) {
			int daysAgo = Integer.parseInt(para.getParm_value1());
			delDate = DateTimeUtil.dateAdd("day", lstrdt, daysAgo);
		} else {
			delDate = DateTimeUtil.dateAdd("day", lstrdt, -30);
		}
		InacSqlsDao.deleteKnsAcsqByDay(delDate,corpno);
	}

}
