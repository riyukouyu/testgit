package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;


public class inprod {

public static void inprod( String busino,  final cn.sunline.ltts.busi.dptran.trans.intf.Inprod.Output output){
	
	if(CommUtil.isNull(busino)){
		throw DpModuleError.DpstComm.BNAS0457();
	}
	/**
	 * 高海瑞
	 * 2017.11.17
	 * 原因：因为查询的时候，产品代码会有多条。所以添加了法人号，来进行区分。
	 */
	String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
	String busina = DpAcctDao.selKnbParaMenuBusino(busino,corpno, true);
	
	output.setBusina(busina);
	output.setBusino(busino);
}
}
