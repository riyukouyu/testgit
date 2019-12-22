package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostPlan;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostPlanDao;
import cn.sunline.ltts.busi.dp.type.DpProdConfType.KupDppbPostPlanInfo;
import cn.sunline.ltts.busi.dptran.trans.intf.Adptpl.Input.Dpptpls;

public class adptpl {

	public static void addKupDppbPostPlan( final Dpptpls dpptpl){
		for(KupDppbPostPlanInfo info : dpptpl.getDpptpl()){
			KupDppbPostPlan plan = SysUtil.getInstance(KupDppbPostPlan.class);
			plan.setAdjtpd(info.getAdjtpd());
			plan.setCrcycd(info.getCrcycd());
			plan.setDfltsd(info.getDfltsd());
			plan.setDfltwy(info.getDfltwy());
			plan.setEndtwy(info.getEndtwy());
			plan.setGentwy(info.getGentwy());
			plan.setMaxibl(info.getMaxibl());
			plan.setMaxisp(info.getMaxisp());
			plan.setMaxibl(info.getMaxibl());
			plan.setPlanfg(info.getPlanfg());
			plan.setPlanwy(info.getPlanwy());
			plan.setProdcd(info.getProdcd());
			plan.setPscrwy(info.getPscrwy());
			plan.setSvlepd(info.getSvlepd());
			plan.setSvletm(info.getSvletm());
			plan.setSvlewy(info.getSvlewy());
			KupDppbPostPlanDao.insert(plan);
		}
	}
}
