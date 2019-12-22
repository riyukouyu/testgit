package cn.sunline.ltts.busi.dptran.trans.ca;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;


public class qrysms {

	public static void beforeTran( final cn.sunline.ltts.busi.dptrans.trans.ca.intf.Qrysms.Input input,  final cn.sunline.ltts.busi.dptrans.trans.ca.intf.Qrysms.Property property,  final cn.sunline.ltts.busi.dptrans.trans.ca.intf.Qrysms.Output output){
		String cardno = input.getCardno();
		KnaAcdc knaAcdc = KnaAcdcDao.selectOne_odb2(cardno, true);
		property.setCustac(knaAcdc.getCustac());
		KnaCust knaCust = KnaCustDao.selectOne_odb1(knaAcdc.getCustac(), true);
//		CifCust cifCust = CifCustDao.selectOne_odb1(knaCust.getCustno(), true);
//		property.setTeleno(cifCust.getTeleno());
//		output.setTeleno(cifCust.getTeleno());
		

		//poc增加审计日志
		ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
		apAudit.regLogOnInsertBusiPoc(cardno);
	}

}
