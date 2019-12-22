package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostPlan;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostPlanDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVBKAD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVBKDL;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVBKLI;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVPLAJ;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVPLFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVPLGN;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_ENDTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PLANFG;

public class dplupd {

public static void updDpl( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dplupd.Input Input){
	String prodcd = Input.getProdcd();
	String crcycd = Input.getCrcycd();
	E_PLANFG planfg=Input.getPlanfg();
	E_SVPLAJ planwy=Input.getPlanwy();
	String adjtpd=Input.getAdjtpd();
	E_ENDTWY endtwy=Input.getEndtwy();
	E_SVPLGN gentwy=Input.getGentwy();
	String svlepd=Input.getSvlepd();
	E_SVBKAD svlewy=Input.getSvlewy();
	long maxisp=Input.getMaxisp();
	E_SVBKLI dfltsd=Input.getDfltsd();
	long svletm=Input.getSvletm();
	E_SVBKDL dfltwy=Input.getDfltwy();
	E_SVPLFG pscrwy =Input.getPscrwy();
	BigDecimal maxibl=Input.getMaxibl();

	// 传入值检查
	if (CommUtil.isNull(prodcd)) {
		DpModuleError.DpstProd.BNAS1054();
	}
	if (CommUtil.isNull(crcycd)) {
		DpModuleError.DpstComm.BNAS1101();
	}

	// 判断原记录是否存在
	KupDppbPostPlan tmp = KupDppbPostPlanDao.selectOne_odb1(prodcd, crcycd,   false);
	if (CommUtil.isNull(tmp)) {
		throw DpModuleError.DpstComm.BNAS2080();
	}
	// 更新新纪录
	tmp.setProdcd(prodcd);
	tmp.setCrcycd(crcycd);
	tmp.setPlanfg(planfg);
	tmp.setPlanwy(planwy);
	tmp.setAdjtpd(adjtpd);
	tmp.setEndtwy(endtwy);
	tmp.setGentwy(gentwy);
	tmp.setSvlepd(svlepd);
	tmp.setSvlewy(svlewy);
	tmp.setMaxisp(maxisp);
	tmp.setDfltsd(dfltsd);
	tmp.setSvletm(svletm);
	tmp.setDfltwy(dfltwy);
	tmp.setPscrwy(pscrwy);
	tmp.setMaxibl(maxibl);

	KupDppbPostPlanDao.updateOne_odb1(tmp);
}
}
