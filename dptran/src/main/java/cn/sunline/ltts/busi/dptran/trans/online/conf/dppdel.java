package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;


public class dppdel {

	public static void delDpp( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dppdel.Input Input){
		String prodcd = Input.getProdcd();
		//传入值检查
		if(CommUtil.isNull(prodcd)){
			DpModuleError.DpstProd.BNAS1054();
		}
		
		//判断纪录是否存在
		KupDppb entity = SysUtil.getInstance(KupDppb.class);
		entity = KupDppbDao.selectOne_odb1(prodcd, false);
		if(CommUtil.isNull(entity)){
			throw DpModuleError.DpstComm.BNAS2081();
		}
		//删除原纪录
		DpProductDao.delallbyprodcd(prodcd);	
		DpProductDao.delcustbyprodcd(prodcd);
		DpProductDao.delbrchbyprodcd(prodcd);
		DpProductDao.delmatubyprodcd(prodcd);
		DpProductDao.delpostbyprodcd(prodcd);
		DpProductDao.delpostplanbyprodcd(prodcd);
		DpProductDao.deldrawbyprodcd(prodcd);
		DpProductDao.deldrawplanbyprodcd(prodcd);
		DpProductDao.delintrbyprodcd(prodcd);
		DpProductDao.delacctbyprodcd(prodcd);
		DpProductDao.delactpbyprodcd(prodcd);
//		DpProductDao.deldfintrbyprodcd(prodcd);
		DpProductDao.deltermbyprodcd(prodcd);
		
	}
}
