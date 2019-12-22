package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActpDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT;


public class dapdel {

public static void delDap( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dapdel.Input Input){
	String prodcd = Input.getProdcd();
	E_CUSACT cacttp = Input.getCacttp();
	//传入值检查
	if(CommUtil.isNull(prodcd)){
		DpModuleError.DpstProd.BNAS1054();
	}
	if(CommUtil.isNull(cacttp)){
		DpModuleError.DpstComm.BNAS1963();
	}
	//判断纪录是否存在
	KupDppbActp entity = SysUtil.getInstance(KupDppbActp.class);
	entity = KupDppbActpDao.selectOne_odb1(prodcd, cacttp, false);
	if(CommUtil.isNull(entity)){
		throw DpModuleError.DpstComm.BNAS1964();
	}
	//删除原纪录
	KupDppbActpDao.deleteOne_odb1(prodcd, cacttp);
	
}
}
