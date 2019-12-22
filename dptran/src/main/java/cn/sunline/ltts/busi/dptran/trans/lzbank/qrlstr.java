package cn.sunline.ltts.busi.dptran.trans.lzbank;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapot;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapotDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;


public class qrlstr {

	/**
	 * 大小额明细查询
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void qrLsamTran( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qrlstr.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qrlstr.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qrlstr.Output output){
		String fronsq = input.getFronsq();
		String frondt = input.getFrondt();
		if(CommUtil.isNull(fronsq)){
			throw DpModuleError.DpstComm.E9999("支付前置流水不能为空");
		}
		if(CommUtil.isNull(frondt)){
			throw DpModuleError.DpstComm.E9999("支付前置日期不能为空");
		}
		KnlCnapot tblKnlCnapot = KnlCnapotDao.selectOne_odb1(fronsq, frondt, false);
		if(CommUtil.isNotNull(tblKnlCnapot)){
			CommUtil.copyProperties(output, tblKnlCnapot);
			output.setTranst(tblKnlCnapot.getStatus());
		}
	}
}
