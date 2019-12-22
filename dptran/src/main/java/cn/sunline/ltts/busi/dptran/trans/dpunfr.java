
package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;

public class dpunfr {

	public static void selAcdcByAcctid( final cn.sunline.ltts.busi.dptran.trans.intf.Dpunfr.Input input,  
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpunfr.Property property,  
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpunfr.Output output){
		if (CommUtil.isNull(input.getAcctid())) {
			throw DpModuleError.DpAcct.AT010026();
		}
		
		KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(input.getAcctid(), false);
		if (CommUtil.isNull(tblKnaSbad)) {
			throw DpModuleError.DpAcct.AT020054(input.getAcctid());
		}
		
		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectFirst_odb3(tblKnaSbad.getCustac(), false);
		if (CommUtil.isNull(tblKnaAcdc)) {
			throw DpModuleError.DpAcct.AT020028();
		}
		
		property.setCardno2(tblKnaAcdc.getCardno());
		
	}
}
