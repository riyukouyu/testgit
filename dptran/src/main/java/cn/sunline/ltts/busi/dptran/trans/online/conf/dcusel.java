package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.type.DpProdType.KupDppbCustInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.base.lang.Options;


public class dcusel {

public static void selDcu( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dcusel.Input Input,  final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dcusel.Output.Userinfos userinfos){
	

	long pageno = Input.getPageno();
	long pagesize = Input.getPagesize();
	String prodcd = Input.getProdcd();
	String crcycd = Input.getCrcycd();
	//传入参数检查
	if(CommUtil.isNull(pageno)){
		throw DpModuleError.DpstComm.BNAS0977();
	}
	if(CommUtil.isNull(pagesize)){
		throw DpModuleError.DpstComm.BNAS0463();
	}
	//查询总记录数
	long cnt = DpProductDao.selCntKupDppbCust(prodcd, crcycd, false);
	//查询记录明细
	if(cnt != 0){
		List<KupDppbCustInfo> entities = DpProductDao.selPageKupDppbCust(prodcd, crcycd, (pageno - 1)*pagesize, pagesize, false);
		Options<KupDppbCustInfo> instance = SysUtil.getInstance(Options.class);
		instance.addAll(entities);
		userinfos.setInfos(instance);
	}
	userinfos.setCount(cnt);
}
}
