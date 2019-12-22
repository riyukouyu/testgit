package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.type.DpProdType.KupDppbAcctInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.edsp.base.lang.Options;


public class datsel {

public static void selDat( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Datsel.Input Input,  final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Datsel.Output.Userinfos userinfos){
	long pageno = Input.getPageno();
	long pagesize = Input.getPagesize();
	String prodcd = Input.getProdcd();
	E_TERMCD depttm=Input.getDepttm();
	//传入参数检查
	if(CommUtil.isNull(pageno)){
		throw DpModuleError.DpstComm.BNAS0977();
	}
	if(CommUtil.isNull(pagesize)){
		throw DpModuleError.DpstComm.BNAS0463();
	}
	//查询总记录数
	long cnt = DpProductDao.selCntKupDppbAcct(prodcd, depttm, false);
	//查询记录明细
	if(cnt != 0){                                                         
		List<KupDppbAcctInfo> entities = DpProductDao.selPageKupDppbAcct(prodcd, depttm, (pageno - 1)*pagesize, pagesize, false);
		Options<KupDppbAcctInfo> instance = SysUtil.getInstance(Options.class);
		instance.addAll(entities);
		userinfos.setInfos(instance);
	}
	userinfos.setCount(cnt);
	
	
	
}
}
