package cn.sunline.ltts.busi.dptran.trans.ca;

import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;


public class updsms {

	public static void beforeTran( final cn.sunline.ltts.busi.dptrans.trans.ca.intf.Updsms.Input input,  final cn.sunline.ltts.busi.dptrans.trans.ca.intf.Updsms.Property property,  final cn.sunline.ltts.busi.dptrans.trans.ca.intf.Updsms.Output output){
		String cardno = input.getCardno();
		KnaAcdc knaAcdc = KnaAcdcDao.selectOne_odb2(cardno, true);
		property.setCustac(knaAcdc.getCustac());
		KnaCust knaCust = KnaCustDao.selectOne_odb1(knaAcdc.getCustac(), true);
//		CifCust cifCust = CifCustDao.selectOne_odb1(knaCust.getCustno(), true);
//		property.setTeleno(cifCust.getTeleno());
	}
}
