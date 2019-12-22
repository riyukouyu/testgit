package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawPlan;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawPlanDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DWBKDL;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DWBKLI;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVPLFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BEINFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DWPLAJ;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_ENDTWY;



public class dwpupd {

public static void updDwp( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dwpupd.Input Input){
	String prodcd = Input.getProdcd();
	String crcycd = Input.getCrcycd();
	E_YES___ setpwy= Input.getSetpwy();
	E_DWPLAJ termwy=Input.getTermwy();
	String dradpd=Input.getDradpd();
	E_ENDTWY dredwy=Input.getDredwy();
	E_SVPLFG drcrwy=Input.getDrcrwy();
	E_DWBKLI drdfsd =Input.getDrdfsd();
	E_DWBKDL drdfwy=Input.getDrdfwy();
	BigDecimal minibl=Input.getMinibl();
	E_BEINFG beinfg=Input.getBeinfg();

	// 传入值检查
	if (CommUtil.isNull(prodcd)) {
		DpModuleError.DpstProd.BNAS1054();
	}
	if (CommUtil.isNull(crcycd)) {
		DpModuleError.DpstComm.BNAS1101();
	}

	// 判断原记录是否存在
	KupDppbDrawPlan tmp = KupDppbDrawPlanDao.selectOne_odb1(prodcd, crcycd,   false);
	if (CommUtil.isNull(tmp)) {
		throw DpModuleError.DpstComm.BNAS2167();
	}
	// 更新新纪录
	tmp.setProdcd(prodcd);
	tmp.setCrcycd(crcycd);
	tmp.setSetpwy(setpwy);
	tmp.setTermwy(termwy);
	tmp.setDradpd(dradpd);
	tmp.setDredwy(dredwy);
	tmp.setDrcrwy(drcrwy);
	tmp.setDrdfsd(drdfsd);
	tmp.setDrdfwy(drdfwy);
	tmp.setMinibl(minibl);
	tmp.setBeinfg(beinfg);

	KupDppbDrawPlanDao.updateOne_odb1(tmp);
}
}
