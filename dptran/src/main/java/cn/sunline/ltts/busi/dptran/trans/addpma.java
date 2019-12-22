package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbMatu;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbMatuDao;
import cn.sunline.ltts.busi.dp.type.DpProdConfType.KupDppbMatuInfo;
import cn.sunline.ltts.busi.dptran.trans.intf.Addpma.Input.Dpmatus;


public class addpma {

	public static void addKupDppbMatu( final Dpmatus dpmatu){
		for(KupDppbMatuInfo info : dpmatu.getDpmatu()){
			KupDppbMatu matu = SysUtil.getInstance(KupDppbMatu.class);
			matu.setCrcycd(info.getCrcycd());
			matu.setDelyfg(info.getDelyfg());
			matu.setFestdl(info.getFestdl());
			matu.setMatupd(info.getMatupd());
			matu.setProdcd(info.getProdcd());
			matu.setTrdpfg(info.getTrdpfg());
			matu.setTrinwy(info.getTrinwy());
			matu.setTrpdfg(info.getTrpdfg());
			matu.setTrprod(info.getMatupd());
			KupDppbMatuDao.insert(matu);
		}
	}
}
