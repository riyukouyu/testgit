package cn.sunline.ltts.busi.dptran.trans.lzbank;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIobl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;


public class qrsctr {

	/**
	 * 本行卡明细查询
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void qrSelfcardTran( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qrsctr.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qrsctr.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qrsctr.Output output){
		String fronsq = input.getFronsq();
		String frondt = input.getFrondt();
		if(CommUtil.isNull(fronsq)){
			throw DpModuleError.DpstComm.E9999("支付前置流水不能为空");
		}
		if(CommUtil.isNull(frondt)){
			throw DpModuleError.DpstComm.E9999("支付前置日期不能为空");
		}
		KnlIobl tblKnlIobl = KnlIoblDao.selectOne_odb1(fronsq, frondt, false);
		if(CommUtil.isNotNull(tblKnlIobl)){
			CommUtil.copyProperties(output, tblKnlIobl);
			output.setTranst(tblKnlIobl.getStatus());
		}
	}
}
