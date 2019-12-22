package cn.sunline.ltts.busi.dptran.trans.online.conf;


import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActpDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DCMTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT;


public class dapupd {

public static void updDap( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dapupd.Input Input){
	String prodcd = Input.getProdcd();
	E_CUSACT cacttp=Input.getCacttp();
	E_YES___ acolfg=Input.getAcolfg();
	E_YES___ dcmtfg=Input.getDcmtfg();
	E_DCMTTP dcmttp=Input.getDcmttp();
	String sactcn=Input.getSactcn();
	String dcmtcn=Input.getDcmtcn();

	// 传入值检查
	if (CommUtil.isNull(prodcd)) {
		DpModuleError.DpstProd.BNAS1054();
	}
	if (CommUtil.isNull(cacttp)) {
		DpModuleError.DpstComm.BNAS1963();
	}

	// 判断原记录是否存在
	KupDppbActp tmp = KupDppbActpDao.selectOne_odb1(prodcd, cacttp,   false);
	if (CommUtil.isNull(tmp)) {
		throw DpModuleError.DpstComm.BNAS1965();
	}
	// 更新新纪录
	tmp.setProdcd(prodcd);
	tmp.setCacttp(cacttp);
	tmp.setAcolfg(acolfg);
	tmp.setDcmtfg(dcmtfg);
	tmp.setDcmttp(dcmttp);
	tmp.setSactcn(sactcn);
	tmp.setDcmtcn(dcmtcn);
	KupDppbActpDao.updateOne_odb1(tmp);
}
}
