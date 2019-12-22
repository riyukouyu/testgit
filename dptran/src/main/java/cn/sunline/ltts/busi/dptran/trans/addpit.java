package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntr;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntrDao;
import cn.sunline.ltts.busi.dp.type.DpProdConfType.KupDppbIntrInfo;
import cn.sunline.ltts.busi.dptran.trans.intf.Addpit.Input.Dpintrs;


public class addpit {

public static void addKupDppbIntr( final Dpintrs dpintr){
	for(KupDppbIntrInfo info : dpintr.getDpintr()){
		KupDppbIntr intr = SysUtil.getInstance(KupDppbIntr.class);
		intr.setBldyca(info.getBldyca());
		intr.setCrcycd(info.getCrcycd());
		intr.setFvrbfg(info.getFvrbfg());
		intr.setFvrblv(info.getFvrblv());
		intr.setHutxfg(info.getHutxfg());
		intr.setInadlv(info.getInadlv());
		intr.setInammd(info.getInammd());
		intr.setIncdtp(info.getIncdtp());
		intr.setInbefg(info.getInbefg());
		intr.setInprwy(info.getInprwy());
		intr.setIntrcd(info.getIntrcd());
		intr.setIntrtp(info.getIntrtp());
		intr.setIntrwy(info.getIntrwy());
		intr.setLyinwy(info.getLyinwy());
		intr.setProdcd(info.getProdcd());
		intr.setReprwy(info.getReprwy());
		intr.setTaxecd(info.getTaxecd());
		intr.setTxbebs(info.getTxbebs());
		intr.setTxbefg(info.getTxbefg());
		intr.setTxbefr(info.getTxbefr());
		
		KupDppbIntrDao.insert(intr);
	}
}
}
