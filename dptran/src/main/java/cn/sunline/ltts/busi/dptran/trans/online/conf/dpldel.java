package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostPlan;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostPlanDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;




public class dpldel {

public static void delDpl( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dpldel.Input Input){
	String prodcd = Input.getProdcd();
	String crcycd = Input.getCrcycd();
	//传入值检查
	if(CommUtil.isNull(prodcd)){
		DpModuleError.DpstProd.BNAS1054();
	}
	if(CommUtil.isNull(crcycd)){
		DpModuleError.DpstComm.BNAS1101();;
	}
	//判断纪录是否存在
	KupDppbPostPlan entity = SysUtil.getInstance(KupDppbPostPlan.class);
	entity = KupDppbPostPlanDao.selectOne_odb1(prodcd, crcycd, false);
	if(CommUtil.isNull(entity)){
		throw DpModuleError.DpstComm.BNAS2079();
	}
	//删除原纪录
	KupDppbPostPlanDao.deleteOne_odb1(prodcd, crcycd);
}
}
