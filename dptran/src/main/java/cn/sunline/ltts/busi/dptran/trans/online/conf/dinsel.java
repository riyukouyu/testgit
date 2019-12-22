package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.type.DpProdType.KupDppbIntrInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.edsp.base.lang.Options;


public class dinsel {

public static void selDin( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dinsel.Input Input,  final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dinsel.Output.Userinfos userinfos){
	long pageno = Input.getPageno();
	long pagesize = Input.getPagesize();
	String prodcd = Input.getProdcd();
	String crcycd = Input.getCrcycd();
	E_INTRTP intrtp = Input.getIntrtp();
	//传入参数检查
	if(CommUtil.isNull(pageno)){
		throw DpModuleError.DpstComm.BNAS0977();
	}
	if(CommUtil.isNull(pagesize)){
		throw DpModuleError.DpstComm.BNAS0463();
	}
	//查询总记录数
	long cnt = DpProductDao.selCntKupDppbIntr(prodcd, crcycd, intrtp,false);
	//查询记录明细
	if(cnt != 0){
		List<KupDppbIntrInfo> entities = DpProductDao.selPageKupDppbIntr(prodcd, crcycd, intrtp,(pageno - 1)*pagesize, pagesize, false);
		Options<KupDppbIntrInfo> instance = SysUtil.getInstance(Options.class);
		instance.addAll(entities);
		userinfos.setInfos(instance);
	}
	userinfos.setCount(cnt);
}
}
