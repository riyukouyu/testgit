package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcct;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcctDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;


public class datdel {

public static void delDat( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Datdel.Input Input){
	String prodcd = Input.getProdcd();
	E_TERMCD depttm=Input.getDepttm();
	//传入值检查
	if(CommUtil.isNull(prodcd)){
		DpModuleError.DpstProd.BNAS1054();
	}
	if(CommUtil.isNull(depttm)){
		DpModuleError.DpstProd.BNAS1025();
	}
	//判断纪录是否存在
	KupDppbAcct entity = SysUtil.getInstance(KupDppbAcct.class);
	entity = KupDppbAcctDao.selectOne_odb1(prodcd, depttm, false);
	if(CommUtil.isNull(entity)){
		throw DpModuleError.DpstComm.BNAS1966();
	}
	//删除原纪录
	KupDppbAcctDao.deleteOne_odb1(prodcd, depttm);
	
}
}
