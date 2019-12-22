package cn.sunline.ltts.busi.dptran.trans;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.namedsql.DpJointDao;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpJointType.IoDpKnbJointIoblInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.base.lang.Options;

public class qrjols {

	public static void qrKnbJointIoblList(
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrjols.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrjols.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrjols.Output output) {
		String joinac = input.getJoinac();
		if (CommUtil.isNull(input.getJoinac())) {
			throw DpModuleError.DpstProd.E0010("联名账户不能为空！");
		}
		if (CommUtil.isNull(input.getPageno())) {
			throw DpModuleError.DpstProd.E0010("页数不能为空！");
		}
		if (CommUtil.isNull(input.getPgsize())) {
			throw DpModuleError.DpstProd.E0010("每页记录数不能为空！");
		}
		long start = (input.getPageno() - 1) * input.getPgsize();
		long count = input.getPgsize();
		long counts = DpJointDao.selKnbJointIoblcount(joinac, BusiTools.getTranCorpno(), false);
		if (counts > 0) {
			List<IoDpKnbJointIoblInfo> knbJointIoblInfos = DpJointDao
					.selKnbJointIoblList(joinac, BusiTools.getTranCorpno(), start,
							count, false);
			Options<IoDpKnbJointIoblInfo> ioDpKnbJointIoblInfos = SysUtil
					.getInstance(Options.class);
			for (IoDpKnbJointIoblInfo ioDpKnbJointIoblInfo : knbJointIoblInfos) {
				ioDpKnbJointIoblInfos.add(ioDpKnbJointIoblInfo);
			}
			output.setIoblls(ioDpKnbJointIoblInfos);
		}
		CommTools.getBaseRunEnvs().setTotal_count(counts);
	}
}
