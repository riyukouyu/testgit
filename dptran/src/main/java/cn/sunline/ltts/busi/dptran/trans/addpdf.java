package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfir;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfirDao;
import cn.sunline.ltts.busi.dp.type.DpProdConfType.KupDppbDfitInfo;
import cn.sunline.ltts.busi.dptran.trans.intf.Addpdf.Input.Dpdfits;


public class addpdf {

	public static void addKupDppbDfintr( final Dpdfits dpdfit){
		for(KupDppbDfitInfo info : dpdfit.getDpdfit()){
			KupDppbDfir dfin = SysUtil.getInstance(KupDppbDfir.class);
			dfin.setAdincd(info.getAdincd());
			dfin.setAmlvcd(info.getAmlvcd());
			dfin.setAmlvef(info.getAmlvef());
			dfin.setAmlvrl(info.getAmlvrl());
			dfin.setAmlvsr(info.getAmlvsr());
			dfin.setBsinam(info.getBsinam());
			dfin.setBsincd(info.getBsincd());
			dfin.setBsindt(info.getBsindt());
			dfin.setBsinef(info.getBsinef());
			dfin.setBsinrl(info.getBsinrl());
			dfin.setCrcycd(info.getCrcycd());
			dfin.setDrdein(info.getDrdein());
			dfin.setDrinsc(info.getDrinsc());
			dfin.setDrintp(info.getDrintp());
			dfin.setDrintx(info.getDrintx());
			dfin.setDtlvef(info.getDtlvef());
			dfin.setDtlvrl(info.getDtlvrl());
			dfin.setDtlvsr(info.getDtlvsr());
			dfin.setDtsrcd(info.getDtsrcd());
			dfin.setInadtp(info.getInadtp());
			dfin.setInclfg(info.getInclfg());
			dfin.setInedsc(info.getInedsc());
			dfin.setIngpcd(info.getIngpcd());
			dfin.setInsrwy(info.getInsrwy());
			dfin.setIntrtp(info.getIntrtp());
			dfin.setProdcd(info.getProdcd());
			dfin.setSminad(info.getSminad());
			dfin.setTeartp(info.getTeartp());
			KupDppbDfirDao.insert(dfin);
		}
	}


}
