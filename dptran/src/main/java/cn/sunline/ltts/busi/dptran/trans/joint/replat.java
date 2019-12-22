package cn.sunline.ltts.busi.dptran.trans.joint;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnbPlatInfo;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnbPlatInfoDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;

public class replat {

	public static void registeredPlatform(
			final cn.sunline.ltts.busi.dptran.trans.intf.Replat.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Replat.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Replat.Output output) {

		if (CommUtil.isNull(input.getPlatno())) {
			throw DpModuleError.DpstComm.E9027("平台编号");
		} else if (input.getPlatno().length() != 4) {
			throw DpModuleError.DpstComm.E9901("平台编号长度必须是四位！");
		}
		if (CommUtil.isNull(input.getPlatna())) {
			throw DpModuleError.DpstComm.E9027("平台名称");
		}
		if (CommUtil.isNull(input.getPllqbu())) {
			throw DpModuleError.DpstComm.E9027("平台待清算账户");
		}
		if (CommUtil.isNull(input.getPlowbu())) {
			throw DpModuleError.DpstComm.E9027("平台自有资金账户");
		}

		String platno = CommTools.getBaseRunEnvs().getTrxn_branch()
				+ input.getPlatno();
		KnbPlatInfo selKnbPlatInfo = KnbPlatInfoDao.selectOne_odb1(platno,
				false);
		if (CommUtil.isNotNull(selKnbPlatInfo)) {
			throw DpModuleError.DpstComm.E9027("平台信息已存在！");
		}
		KnbPlatInfo knbPlatInfo = SysUtil.getInstance(KnbPlatInfo.class);
		knbPlatInfo.setPlatno(platno);
		knbPlatInfo.setPlatna(input.getPlatna());
		knbPlatInfo.setPllqbu(input.getPllqbu());
		knbPlatInfo.setPlowbu(input.getPlowbu());
		KnbPlatInfoDao.insert(knbPlatInfo);

	}
}
