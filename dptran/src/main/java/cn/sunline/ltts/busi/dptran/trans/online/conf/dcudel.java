package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCust;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCustDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;



public class dcudel {

public static void delDcu( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dcudel.Input Input){
	String prodcd = Input.getProdcd();
	String crcycd = Input.getCrcycd();
	//传入值检查
	if(CommUtil.isNull(prodcd)){
		DpModuleError.DpstProd.BNAS1328();
	}
	if(CommUtil.isNull(crcycd)){
		DpModuleError.DpstComm.BNAS1101();
	}
	//判断纪录是否存在
	KupDppbCust entity = SysUtil.getInstance(KupDppbCust.class);
	entity = KupDppbCustDao.selectOne_odb1(prodcd, crcycd, false);
	if(CommUtil.isNull(entity)){
		throw DpModuleError.DpstComm.BNAS1974();
	}
	//删除原纪录
	KupDppbCustDao.deleteOne_odb1(prodcd, crcycd);
}
}
