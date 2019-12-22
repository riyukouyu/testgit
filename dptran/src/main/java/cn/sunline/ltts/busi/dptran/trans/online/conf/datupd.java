package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcct;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcctDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;


public class datupd {

public static void updDat( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Datupd.Input Input){
	String prodcd = Input.getProdcd();
	E_TERMCD depttm=Input.getDepttm();
	String acctcd=Input.getAcctcd();

	// 传入值检查
	if (CommUtil.isNull(prodcd)) {
		DpModuleError.DpstProd.BNAS1054();
	}
	if (CommUtil.isNull(depttm)) {
		DpModuleError.DpstProd.BNAS1025();
	}

	// 判断原记录是否存在
	KupDppbAcct tmp = KupDppbAcctDao.selectOne_odb1(prodcd, depttm,   false);
	if (CommUtil.isNull(tmp)) {
		throw DpModuleError.DpstComm.BNAS1966();
	}
	// 更新新纪录
	tmp.setProdcd(prodcd);
	tmp.setDepttm(depttm);
	tmp.setAcctcd(acctcd);

	KupDppbAcctDao.updateOne_odb1(tmp);
}
}
