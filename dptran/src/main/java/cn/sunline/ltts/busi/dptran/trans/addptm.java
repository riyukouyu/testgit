package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTerm;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTermDao;
import cn.sunline.ltts.busi.dp.type.DpProdConfType.KupDppbTermInfo;
import cn.sunline.ltts.busi.dptran.trans.intf.Addptm.Input.Dpterms;



public class addptm {

	public static void addKupDppbTerm( final Dpterms dpterm){
		for(KupDppbTermInfo info : dpterm.getDpterm()){
			KupDppbTerm term = SysUtil.getInstance(KupDppbTerm.class);
			term.setCrcycd(info.getCrcycd());
			term.setDepttm(info.getDepttm());
			term.setProdcd(info.getProdcd());
			term.setDeptdy(info.getDeptdy());
			KupDppbTermDao.insert(term);
		}
	}


}
