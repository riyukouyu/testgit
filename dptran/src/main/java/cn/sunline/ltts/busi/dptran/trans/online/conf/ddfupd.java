package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfir;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfirDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INTREF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRAFBY;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AMLVSR;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BSINAM;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BSINDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DRINSC;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DRINTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DTLVSR;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INADTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEBS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INEDSC;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SMINAD;



public class ddfupd {

public static void updDdf( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Ddfupd.Input Input){
	String prodcd = Input.getProdcd();
	String crcycd = Input.getCrcycd();
	E_DRINTP drintp= Input.getDrintp();
	String ingpcd=Input.getIngpcd();
	E_INTRTP intrtp=Input.getIntrtp(); 
	String drintx=Input.getDrintx();
	E_INADTP inadtp =Input.getInadtp();
	E_SMINAD sminad=Input.getSminad();
	String adincd=Input.getAdincd();
	E_IRAFBY insrwy=Input.getInsrwy();
	E_YES___ inclfg=Input.getInclfg();
	E_BSINAM bsinam=Input.getBsinam();
	E_BSINDT bsindt=Input.getBsindt();
	E_INEDSC inedsc=Input.getInedsc();
	String bsincd=Input.getBsincd();
	E_INBEBS bsinrl=Input.getBsinrl();
	String bsinef=Input.getBsinef();
	E_DTLVSR dtlvsr =Input.getDtlvsr();
	String dtsrcd=Input.getDtsrcd();
	E_INBEBS dtlvrl=Input.getDtlvrl();
	E_INTREF dtlvef=Input.getDtlvef();
	E_AMLVSR amlvsr =Input.getAmlvsr();
	String amlvcd=Input.getAmlvcd();
	E_INBEBS amlvrl=Input.getAmlvrl();
	E_INTREF amlvef=Input.getAmlvef();
	E_DRINSC drinsc =Input.getDrinsc();
	E_YES___ drdein=Input.getDrdein();

	// 传入值检查
	if (CommUtil.isNull(prodcd)) {
		DpModuleError.DpstProd.BNAS1328();
	}
	if (CommUtil.isNull(crcycd)) {
		DpModuleError.DpstComm.BNAS1101();
	}
	if(CommUtil.isNull(drintp)){
		 DpModuleError.DpstComm.BNAS1218();
	}
	if(CommUtil.isNull(ingpcd)){
		 DpModuleError.DpstComm.BNAS1976();
	}
	if(CommUtil.isNull(intrtp)){
		DpModuleError.DpstComm.BNAS0473();
	}

	// 判断原记录是否存在
	KupDppbDfir tmp = KupDppbDfirDao.selectOne_odb1(prodcd, crcycd, drintp, ingpcd, intrtp,
			false);
	if (CommUtil.isNull(tmp)) {
		throw DpModuleError.DpstComm.BNAS1978();
	}
	// 更新新纪录
	tmp.setProdcd(prodcd);
	tmp.setCrcycd(crcycd);
	tmp.setDrintx(drintx);
	tmp.setInadtp(inadtp);
	tmp.setSminad(sminad);
	tmp.setAdincd(adincd);
	tmp.setInsrwy(insrwy);
	tmp.setInclfg(inclfg);
	tmp.setBsinam(bsinam);
	tmp.setBsindt(bsindt);
	tmp.setInedsc(inedsc);
	tmp.setBsincd(bsincd);
	tmp.setBsinrl(bsinrl);
	tmp.setBsinef(bsinef);
	tmp.setDtlvsr(dtlvsr);
	tmp.setDtsrcd(dtsrcd);
	tmp.setDtlvrl(dtlvrl);
	tmp.setDtlvef(dtlvef);
	tmp.setAmlvsr(amlvsr);
	tmp.setAmlvcd(amlvcd); 
	tmp.setAmlvrl(amlvrl);
	tmp.setAmlvef(amlvef);
	tmp.setDrinsc(drinsc);
	tmp.setDrdein(drdein);
	KupDppbDfirDao.updateOne_odb1(tmp);
}
}
