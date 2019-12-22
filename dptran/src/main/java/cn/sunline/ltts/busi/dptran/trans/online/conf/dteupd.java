package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTerm;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTermDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;



public class dteupd {

public static void updDte( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dteupd.Input Input){
	String prodcd = Input.getProdcd();
	String crcycd = Input.getCrcycd();
	E_TERMCD depttm =Input.getDepttm();

	// 传入值检查
	if (CommUtil.isNull(prodcd)) {
		DpModuleError.DpstProd.BNAS1054();
	}
	if (CommUtil.isNull(crcycd)) {
		DpModuleError.DpstComm.BNAS1101();
	}
	if (CommUtil.isNull(depttm)) {
		DpModuleError.DpstProd.BNAS1025();
	}


	// 判断原记录是否存在
	KupDppbTerm tmp = KupDppbTermDao.selectOne_odb1(prodcd, crcycd,depttm,
			false);
	if (CommUtil.isNull(tmp)) {
		throw DpModuleError.DpstComm.BNAS2166();
	}
	// 更新新纪录
	tmp.setProdcd(prodcd);
	tmp.setCrcycd(crcycd);
	tmp.setDepttm(depttm);
	tmp.setDeptdy(Input.getDeptdy()); //存期天数

	KupDppbTermDao.updateOne_odb1(tmp);
}
}
