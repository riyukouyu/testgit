package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawPlan;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawPlanDao;
import cn.sunline.ltts.busi.dp.type.DpProdConfType.KupDppbDrawPlanInfo;
import cn.sunline.ltts.busi.dptran.trans.intf.Addwpl.Input.Dpdwpls;


public class addwpl {

public static void addKupDppbDrawPlan( final Dpdwpls dpdwpl){
	for(KupDppbDrawPlanInfo info : dpdwpl.getDpdwpl()){
		KupDppbDrawPlan plan = SysUtil.getInstance(KupDppbDrawPlan.class);
		plan.setBeinfg(info.getBeinfg());
		plan.setCrcycd(info.getCrcycd());
		plan.setDradpd(info.getDradpd());
		plan.setDrcrwy(info.getDrcrwy());
		plan.setDrdfsd(info.getDrdfsd());
		plan.setDrdfwy(info.getDrdfwy());
		plan.setDredwy(info.getDredwy());
		plan.setProdcd(info.getProdcd());
		plan.setMinibl(info.getMinibl());
		plan.setSetpwy(info.getSetpwy());
		plan.setTermwy(info.getTermwy());
		KupDppbDrawPlanDao.insert(plan);
	}
}
}
