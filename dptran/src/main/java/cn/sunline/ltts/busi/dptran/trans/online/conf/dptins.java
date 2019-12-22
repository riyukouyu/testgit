package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDptd;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDptdDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;


public class dptins {

public static void insDpt( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dptins.Input Input){
	String prodcd = Input.getProdcd();
	E_PRODTP prodtp = Input.getProdtp();
	if (CommUtil.isNull(prodcd)) {
		DpModuleError.DpstProd.BNAS1054();
	}
	if (CommUtil.isNull(prodtp)) {
		DpModuleError.DpstComm.BNAS1051();
	}

	// 判断原记录是否存在
	KupDptd tmp = KupDptdDao.selectOne_odb2(prodtp,prodcd , false);
	if (CommUtil.isNotNull(tmp)) {
		throw DpModuleError.DpstComm.BNAS2120();
	}
	// 插入新纪录
	KupDptd entity = SysUtil.getInstance(KupDptd.class);
	entity.setProdcd(prodcd);
	entity.setProdtp(prodtp);
	
	KupDptdDao.insert(entity);
}
}
