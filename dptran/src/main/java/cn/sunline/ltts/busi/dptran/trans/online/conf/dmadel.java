package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbMatu;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbMatuDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;



public class dmadel {

public static void delDma( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dmadel.Input Input){
	String prodcd = Input.getProdcd();
	String crcycd = Input.getCrcycd();
	//传入值检查
	if(CommUtil.isNull(prodcd)){
		DpModuleError.DpstProd.BNAS1054();
	}
	if(CommUtil.isNull(crcycd)){
		DpModuleError.DpstComm.BNAS1101();
	}
	//判断纪录是否存在
	KupDppbMatu entity = SysUtil.getInstance(KupDppbMatu.class);
	entity = KupDppbMatuDao.selectOne_odb1(prodcd, crcycd, false);
	if(CommUtil.isNull(entity)){
		throw DpModuleError.DpstComm.BNAS2065();
	}
	//删除原纪录
	KupDppbMatuDao.deleteOne_odb1(prodcd, crcycd);
	
	
	
}
}
