package cn.sunline.ltts.busi.dp.serviceimpl;


import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntr;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntrDao;
import cn.sunline.ltts.busi.iobus.type.dp.IoProRateSel.ProInfoAll;
import cn.sunline.ltts.busi.pb.namedsql.intr.ProintrSelDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;

 /**
  * 根据产品编码查询税率编码
  * 根据产品编码查询税率编码
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoProRateSelinfoImpl", longname="根据产品编码查询税率编码", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoProRateSelinfoImpl implements cn.sunline.ltts.busi.iobus.servicetype.dp.IoProRategetintr{
 /**
  * 根据产品编码查询税率编码
  *
  */

@Override
public cn.sunline.ltts.busi.iobus.servicetype.dp.IoProRategetintr.IoProRateSelinfo.Output IoProRateSelinfo(String prodcd) {
		KupDppbIntr intr = KupDppbIntrDao.selectOne_odb1(prodcd, BusiTools.getDefineCurrency(), false);
		if (CommUtil.isNull(intr)) {
			throw DpModuleError.DpstComm.BNAS1710(prodcd);
		}

		IoProRateSelinfo.Output outinfo = SysUtil.getInstance(IoProRateSelinfo.Output.class);
		
		outinfo.setIntrcd(intr.getIntrcd());//利率编码
		outinfo.setIncdtp(intr.getIncdtp());//利率代码类型
		outinfo.setTaxecd(intr.getTaxecd());//税率编号
		outinfo.setIntrwy(intr.getIntrwy());//靠档方式

		return outinfo;
}

@Override
public ProInfoAll IoProInfoByProdno(String prodcd,String corpno) {
	ProInfoAll infoAll = ProintrSelDao.selProdInfoByProdno(prodcd, corpno, false);
	return infoAll;
}

}

