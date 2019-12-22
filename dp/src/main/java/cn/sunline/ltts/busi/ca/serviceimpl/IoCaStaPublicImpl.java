package cn.sunline.ltts.busi.ca.serviceimpl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.ca.eacct.process.CaEAccountProc;
import cn.sunline.ltts.busi.ca.eacct.process.GenFile;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT;
 /**
  * 电子账户对外提供方法实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoCaStaPublicImpl", longname="电子账户对外提供方法实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoCaStaPublicImpl implements cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaStaPublic{
 /**
  * 通过客户账号类型获取默认的产品编号
  *
  */
	public String CaEAccountProc_getProdcd(E_CUSACT csactp) {

		return CaEAccountProc.getProdcd(csactp);
	}

 /**
  * 获取活期账户
  *
  */
	public String CaTools_getAcctno( String custac,  String crcycd){
		
		return CaTools.getAcctno(custac, crcycd);
	}
 /**
  * 获取默认活期产品账户信息
  *
  */
	public cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs CaTools_getAcctAccs( String custac,  String crcycd){
		
		KnaAccs tblKnaAccs = CaTools.getAcctAccs(custac, crcycd);
		IoCaKnaAccs knaAccs = null;
		if(CommUtil.isNotNull(tblKnaAccs)){
			knaAccs = SysUtil.getInstance(IoCaKnaAccs.class);
			CommUtil.copyProperties(knaAccs, tblKnaAccs);
		}
		

		return knaAccs;
	}

	@Override
	public void GenFile_GenKnaCustInfo(String trandt) {
		
		SysUtil.getInstance(GenFile.class).genKnaCustFile(trandt);
	}

	@Override
	public void CaDao_updKnaSignDetlBySignno(String currdt, String nextdt,
			Long signno, E_SIGNST signst, E_SIGNTP signtp) {
		String timetm=DateTools2.getCurrentTimestamp();
		
		CaDao.updKnaSignDetlBySignno(currdt, nextdt, signno, signst, signtp,timetm);
	}
}

