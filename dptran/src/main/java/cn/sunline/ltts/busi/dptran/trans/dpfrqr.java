
package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

public class dpfrqr {

	public static void selAcctByAcctid( final cn.sunline.ltts.busi.dptran.trans.intf.Dpfrqr.Input input,  
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpfrqr.Property property,  
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpfrqr.Output output){
		if (CommUtil.isNotNull(input.getAcctid()) && CommUtil.isNull(input.getFrozno())) {
			KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(input.getAcctid(), false);
			if (CommUtil.isNull(tblKnaSbad)) {
				throw DpModuleError.DpAcct.AT020054(input.getAcctid());
			}
			
			property.setCustac2(tblKnaSbad.getCustac());
			
			if (E_YES___.YES == input.getSubsac()) {
				property.setAcctno(tblKnaSbad.getAcctno());
			}
		}
	}

}
