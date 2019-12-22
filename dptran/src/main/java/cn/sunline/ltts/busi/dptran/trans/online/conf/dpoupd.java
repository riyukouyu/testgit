package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPost;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SAVECT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AMNTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_POSTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TIMEWY;


public class dpoupd {

public static void updDpo( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dpoupd.Input Input){
	String prodcd = Input.getProdcd();
	String crcycd = Input.getCrcycd();
	E_SAVECT posttp = Input.getPosttp();
	E_POSTWY postwy = Input.getPostwy();
	E_AMNTWY amntwy = Input.getAmntwy();
	BigDecimal miniam = Input.getMiniam();
	BigDecimal maxiam = Input.getMaxiam();
	E_TIMEWY timewy=Input.getTimewy();
	Long minitm =Input.getMinitm();
	Long maxitm =Input.getMaxitm();
	E_YES___ detlfg =Input.getDetlfg();
	Long svrule = Input.getSvrule();

	// 传入值检查
	if (CommUtil.isNull(prodcd)) {
		DpModuleError.DpstProd.BNAS1054();
	}
	if (CommUtil.isNull(crcycd)) {
		DpModuleError.DpstComm.BNAS1101();
	}
	if (CommUtil.isNull(svrule)) {
		svrule = 0L;
	}
	// 判断原记录是否存在
	KupDppbPost tmp = KupDppbPostDao.selectOne_odb1(prodcd, crcycd,   false);
	if (CommUtil.isNull(tmp)) {
		throw DpModuleError.DpstComm.BNAS1965();
	}
	// 更新新纪录
	tmp.setProdcd(prodcd);
	tmp.setCrcycd(crcycd);
	tmp.setPostwy(postwy);
	tmp.setPosttp(posttp);
	tmp.setAmntwy(amntwy);
	tmp.setMaxiam(maxiam);
	tmp.setMiniam(miniam);
	tmp.setMaxitm(maxitm);
	tmp.setMinitm(minitm);
	tmp.setTimewy(timewy);
	tmp.setDetlfg(detlfg);
	tmp.setSvrule(svrule);

	KupDppbPostDao.updateOne_odb1(tmp);
}
}
