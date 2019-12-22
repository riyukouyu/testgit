package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDraw;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;



public class ddrdel {

public static void delDdr( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Ddrdel.Input Input){
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
	KupDppbDraw entity = SysUtil.getInstance(KupDppbDraw.class);
	entity = KupDppbDrawDao.selectOne_odb1(prodcd, crcycd, false);
	if(CommUtil.isNull(entity)){
		throw DpModuleError.DpstComm.BNAS1979();
	}
	//删除原纪录
	KupDppbDrawDao.deleteOne_odb1(prodcd, crcycd);
	
}
}
