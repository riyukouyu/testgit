package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTerm;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTermDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;


public class dtedel {

public static void delDte( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dtedel.Input Input){
	String prodcd = Input.getProdcd();
	String crcycd = Input.getCrcycd();
	E_TERMCD depttm=Input.getDepttm();
	//传入值检查
	if(CommUtil.isNull(prodcd)){
		DpModuleError.DpstProd.BNAS1054();
	}
	if(CommUtil.isNull(crcycd)){
		DpModuleError.DpstComm.BNAS1101();
	}
	if(CommUtil.isNull(depttm)){
		DpModuleError.DpstProd.BNAS1025();
	}
	//判断纪录是否存在
	KupDppbTerm entity = SysUtil.getInstance(KupDppbTerm.class);
	entity = KupDppbTermDao.selectOne_odb1(prodcd, crcycd,depttm, false);
	if(CommUtil.isNull(entity)){
		throw DpModuleError.DpstComm.BNAS1974();
	}
	//删除原纪录
	KupDppbTermDao.deleteOne_odb1(prodcd, crcycd,depttm);
}
}
