package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActpDao;
import cn.sunline.ltts.busi.dp.type.DpProdConfType.KupDppbActpInfo;
import cn.sunline.ltts.busi.dptran.trans.intf.Addpap.Input.Dpactps;


public class addpap {

	public static void addKupDppbActp( final Dpactps dpactp){
		for(KupDppbActpInfo info : dpactp.getDpactp()){
			KupDppbActp actp = SysUtil.getInstance(KupDppbActp.class);
			actp.setAcolfg(info.getAcolfg());
			actp.setCacttp(info.getCacttp());
			actp.setDcmtcn(info.getDcmtcn());
			actp.setDcmtfg(info.getDcmtfg());
			actp.setDcmttp(info.getDcmttp());
			actp.setProdcd(info.getProdcd());
			actp.setSactcn(info.getSactcn());
			KupDppbActpDao.insert(actp);
		}
	}

}
