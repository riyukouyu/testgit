package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcct;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcctDao;
import cn.sunline.ltts.busi.dp.type.DpProdConfType.KupDppbAcctInfo;
import cn.sunline.ltts.busi.dptran.trans.intf.Addpat.Input.Dpaccts;


public class addpat {

	public static void addKupDppbAcct( final Dpaccts dpacct){
		for(KupDppbAcctInfo info : dpacct.getDpacct()){
			KupDppbAcct acct = SysUtil.getInstance(KupDppbAcct.class);
			acct.setAcctcd(info.getAcctcd());
			acct.setDepttm(info.getDepttm());
			acct.setProdcd(info.getProdcd());
			KupDppbAcctDao.insert(acct);
		}
	}

}
