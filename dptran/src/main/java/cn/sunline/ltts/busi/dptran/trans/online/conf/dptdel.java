package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDptd;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDptdDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;

public class dptdel {

public static void delDpt( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dptdel.Input Input){
	String prodcd = Input.getProdcd();
	E_PRODTP prodtp = Input.getProdtp();
	//传入值检查
	if(CommUtil.isNull(prodcd)){
		DpModuleError.DpstProd.BNAS1054();
	}
	if(CommUtil.isNull(prodtp)){
		DpModuleError.DpstComm.BNAS1051();
	}
	//判断纪录是否存在
	KupDptd entity = SysUtil.getInstance(KupDptd.class);
	entity = KupDptdDao.selectOne_odb2(prodtp, prodcd, false);
	if(CommUtil.isNull(entity)){
		throw DpModuleError.DpstComm.BNAS2119();
	}
	//删除原纪录
	KupDptdDao.deleteOne_odb2( prodtp,prodcd);
}
}
