package cn.sunline.ltts.busi.dptran.trans;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.dp.tables.DpMessTable.KnpPromMess;
import cn.sunline.ltts.busi.dp.tables.DpMessTable.KnpPromMessDao;
import cn.sunline.ltts.busi.dp.type.DpPoc.MessInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.base.lang.Options;

public class qrmess {

	public static void messageQuery(
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrmess.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrmess.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrmess.Output output) {

		String groupCD = input.getGropcd();
		if (CommUtil.isNull(groupCD)) {
			groupCD = ApUtil.MESS_GROUPCD;
		}
		
		long pageno = CommTools.getBaseRunEnvs().getPage_start();
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();
		long start = (pageno - 1) * pgsize;
				
		if (CommUtil.isNull(pageno)) {
			throw DpModuleError.DpstComm.E9027("页码");
		}
		if (CommUtil.isNull(pgsize)) {
			throw DpModuleError.DpstComm.E9027("每页容量");
		}
		List<KnpPromMess> knpPromMessList = KnpPromMessDao.selectPage_odb2(groupCD, start, pgsize, false);
		Options<MessInfo> messls = SysUtil.getInstance(Options.class);
		long count = 0;
		if (CommUtil.isNotNull(knpPromMessList)) {
			count = KnpPromMessDao.selectAll_odb2(groupCD, false).size();
			for (KnpPromMess knpPromMess : knpPromMessList) {
				MessInfo messInfo = SysUtil.getInstance(MessInfo.class);
				CommUtil.copyProperties(messInfo, knpPromMess);
				messls.add(messInfo);
			}
		}
		output.setMessls(messls);
		CommTools.getBaseRunEnvs().setTotal_count(count);
	}
}
