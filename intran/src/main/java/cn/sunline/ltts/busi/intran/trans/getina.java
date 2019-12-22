package cn.sunline.ltts.busi.intran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.sys.errors.InError;


public class getina {

public static void getItemna( String itemcd,  final cn.sunline.ltts.busi.intran.trans.intf.Getina.Output Output){
	String itemna = InQuerySqlsDao.SelItemnmByCd(itemcd, false);
	if(CommUtil.isNull(itemna)){
		throw InError.comm.E0003("查询科目号["+itemcd+"]信息不存在");
	}
	Output.setItemna(itemna);
}
}
