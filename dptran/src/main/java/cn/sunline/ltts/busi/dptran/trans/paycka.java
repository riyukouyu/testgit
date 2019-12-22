package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;


public class paycka {

public static void paychkfile(){
	
}

public static void paychkfile( final cn.sunline.ltts.busi.dptran.trans.intf.Paycka.Input input){
	
	if(CommUtil.isNull(input.getServdt())){
		throw DpModuleError.DpstComm.BNAS0380();
	}
	//删除已生成的对账明细
	DpAcctQryDao.delKnbpaycbatchByday(input.getServdt());
	
	//将knl_iobl对账明细插入文件生成临时表knb_payc_batch
	DpAcctQryDao.insKnbpaycbatchByiobl(input.getServdt());
	
	//将knl_cary对账明细插入文件生成临时表knb_payc_batch
	DpAcctQryDao.insKnbpaycbatchBycary(input.getServdt());
	
}
}
