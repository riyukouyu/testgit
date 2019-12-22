package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDraw;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BSPKTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DRAWCT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AMNTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CTRLWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DRRULE;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SELFWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TIMEWY;


public class ddrupd {

public static void updDdr( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Ddrupd.Input Input){

	String prodcd = Input.getProdcd();
	String crcycd = Input.getCrcycd();
	E_DRAWCT drawtp =Input.getDrawtp();
	E_CTRLWY ctrlwy =Input.getCtrlwy();
	E_SELFWY selfwy= Input.getSelfwy();
	E_BSPKTP ordrwy =Input.getOrdrwy();
	E_AMNTWY dramwy =Input.getDramwy();
	BigDecimal drmiam =Input.getDrmiam();
	BigDecimal drmxam =Input.getDrmxam();
	E_TIMEWY drtmwy =Input.getDrtmwy();
	long drmitm =Input.getDrmitm();
	long drmxtm =Input.getDrmxtm();
	E_DRRULE drrule=Input.getDrrule();


	// 传入值检查
	if (CommUtil.isNull(prodcd)) {
		DpModuleError.DpstProd.BNAS1328();
	}
	if (CommUtil.isNull(crcycd)) {
		DpModuleError.DpstComm.BNAS1101();
	}


	// 判断原记录是否存在
	KupDppbDraw tmp = KupDppbDrawDao.selectOne_odb1(prodcd, crcycd,
			false);
	if (CommUtil.isNull(tmp)) {
		throw DpModuleError.DpstComm.BNAS1980();
	}
	// 更新新纪录
	tmp.setProdcd(prodcd);
	tmp.setCrcycd(crcycd);
	tmp.setDramwy(dramwy);
	tmp.setDrawtp(drawtp);
	tmp.setCtrlwy(ctrlwy);
	tmp.setSelfwy(selfwy);
	tmp.setOrdrwy(ordrwy);
	tmp.setDrmiam(drmiam);
	tmp.setDrmitm(drmitm);
	tmp.setDrmxam(drmxam);
	tmp.setDrmxtm(drmxtm);
	tmp.setDrrule(drrule);
	tmp.setDrtmwy(drtmwy);

	KupDppbDrawDao.updateOne_odb1(tmp);
	
}
}
