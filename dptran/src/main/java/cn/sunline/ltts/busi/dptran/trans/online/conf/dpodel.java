package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPost;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;



public class dpodel {

public static void delDpo( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dpodel.Input Input){
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
	KupDppbPost entity = SysUtil.getInstance(KupDppbPost.class);
	entity = KupDppbPostDao.selectOne_odb1(prodcd, crcycd, false);
	if(CommUtil.isNull(entity)){
		throw DpModuleError.DpstComm.BNAS1974();
	}
	//删除原纪录
	KupDppbPostDao.deleteOne_odb1(prodcd, crcycd);
}
}
