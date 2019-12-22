package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntr;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntrDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;


public class dindel {

public static void delDin( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dindel.Input Input){
	String prodcd = Input.getProdcd();
	String crcycd = Input.getCrcycd();
	E_INTRTP intrtp= Input.getIntrtp();
	//传入值检查
	if(CommUtil.isNull(prodcd)){
		DpModuleError.DpstProd.BNAS1054();
	}
	if(CommUtil.isNull(crcycd)){
		DpModuleError.DpstComm.BNAS1101();
	}
	if(CommUtil.isNull(intrtp)){
		DpModuleError.DpstComm.BNAS0473();
	}
	//判断纪录是否存在
	KupDppbIntr entity = SysUtil.getInstance(KupDppbIntr.class);
	entity = KupDppbIntrDao.selectOne_odb2(prodcd, crcycd,intrtp, false);
	if(CommUtil.isNull(entity)){
		throw DpModuleError.DpstComm.BNAS2041();
	}
	//删除原纪录
	KupDppbIntrDao.deleteOne_odb2(prodcd, crcycd,intrtp);
	
	
}
}
