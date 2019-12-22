package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrch;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrchDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;



public class dbrdel {

public static void delDbr( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dbrdel.Input Input){
	String prodcd = Input.getProdcd();
	String crcycd = Input.getCrcycd();
	String brchno =Input.getBrchno();
	//传入值检查
	if(CommUtil.isNull(prodcd)){
		DpModuleError.DpstProd.BNAS1054();
	}
	if(CommUtil.isNull(crcycd)){
		DpModuleError.DpstComm.BNAS1101();
	}
	if(CommUtil.isNull(brchno)){
		DpModuleError.DpstComm.BNAS0655();
	}
	//判断纪录是否存在
	KupDppbBrch entity = SysUtil.getInstance(KupDppbBrch.class);
	entity = KupDppbBrchDao.selectOne_odb2(prodcd, crcycd, brchno, false);
	if(CommUtil.isNull(entity)){
		throw DpModuleError.DpstComm.BNAS1967();
	}
	//删除原纪录
	KupDppbBrchDao.deleteOne_odb2(prodcd, crcycd,brchno);
}
}
