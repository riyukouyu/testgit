package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;


public class dbrsel {

public static void selDbr( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dbrsel.Input Input,  final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dbrsel.Output.Userinfos userinfos){
	
	
	long pageno = Input.getPageno();
	long pagesize = Input.getPagesize();
	String prodcd = Input.getProdcd();
	String crcycd = Input.getCrcycd();
	String brchno =Input.getBrchno();
	//传入参数检查
	if(CommUtil.isNull(pageno)){
		throw DpModuleError.DpstComm.BNAS0977();
	}
	if(CommUtil.isNull(pagesize)){
		throw DpModuleError.DpstComm.BNAS0463();
	}
	//查询总记录数
	long cnt = DpProductDao.selCntKupDppbBrch(prodcd, crcycd, brchno, false);
	//查询记录明细
//	if(cnt != 0){                                                          
//		List<KupDppbBrchInfo> entities = DpProductDao.selPageKupDppbBrch(prodcd, crcycd, brchno, (pageno - 1)*pagesize, pagesize, false);
//		Options<KupDppbBrchInfo> instance = SysUtil.getInstance(Options.class);
//		instance.addAll(entities);
//		userinfos.setInfos(instance);
//	}
//	userinfos.setCount(cnt);
}
}
