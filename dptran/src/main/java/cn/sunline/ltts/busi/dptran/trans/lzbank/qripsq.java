package cn.sunline.ltts.busi.dptran.trans.lzbank;

import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.dp.type.CustInfoCountType.InfoKnsredu;




public class qripsq {

public static void QryKnsReduInfo( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qripsq.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qripsq.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qripsq.Output output){

	String transq = input.getTransq();
	InfoKnsredu info = EacctMainDao.selinpusq(transq, false);
	output.setInfoKnsredu(info);
}
}
