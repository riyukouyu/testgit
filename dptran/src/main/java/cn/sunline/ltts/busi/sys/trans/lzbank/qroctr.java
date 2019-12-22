package cn.sunline.ltts.busi.sys.trans.lzbank;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUPSST;


public class qroctr {

	/**
	 * 云闪付他行卡交易查询
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void qrOtherCardTran( final cn.sunline.ltts.busi.sys.trans.lzbank.intf.Qroctr.Input input,  final cn.sunline.ltts.busi.sys.trans.lzbank.intf.Qroctr.Property property,  final cn.sunline.ltts.busi.sys.trans.lzbank.intf.Qroctr.Output output){
		String fronsq = input.getFronsq();
		String frondt = input.getFrondt();
		if(CommUtil.isNull(fronsq)){
			throw DpModuleError.DpstComm.E9999("支付前置流水不能为空");
		}
		if(CommUtil.isNull(frondt)){
			throw DpModuleError.DpstComm.E9999("支付前置日期不能为空");
		}
		KnlIoblCups tblKnlIoblCups = KnlIoblCupsDao.selectOne_odb2(input.getFronsq(), input.getFrondt(), false);
		if(CommUtil.isNotNull(tblKnlIoblCups)){
			CommUtil.copyProperties(output, tblKnlIoblCups);
		}
	}
}
