package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDptd;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDptdDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;

public class dptupd {

public static void updDpt( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dptupd.Input Input){
	String prodcd = Input.getProdcd();
	E_PRODTP prodtp = Input.getProdtp();
	if (CommUtil.isNull(prodcd)) {
		DpModuleError.DpstProd.BNAS1054();
	}
	if (CommUtil.isNull(prodtp)) {
		DpModuleError.DpstComm.BNAS1051();;
	}

	// 判断原记录是否存在
	KupDptd tmp = KupDptdDao.selectOne_odb2(prodtp,prodcd,
			false);
	if (CommUtil.isNull(tmp)) {
		throw DpModuleError.DpstComm.BNAS2121();
	}
	// 更新新纪录
	tmp.setProdcd(prodcd);
	tmp.setProdtp(prodtp);

	KupDptdDao.updateOne_odb2(tmp);
}
}
