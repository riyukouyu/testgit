
package cn.sunline.edsp.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.ltts.busi.ca.type.CaCustInfo.AccountBalanceInfo;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBill;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;

public class qractl {

public static void QryAccBalance( 
		final cn.sunline.edsp.busi.dptran.trans.intf.Qractl.Input input, 
		final cn.sunline.edsp.busi.dptran.trans.intf.Qractl.Property property,  
		final cn.sunline.edsp.busi.dptran.trans.intf.Qractl.Output output){
	
	String idtfno = input.getIdtfno();
    E_IDTFTP idtftp = input.getIdtftp();
    String tlphno = input.getPhone();
    
	if(CommUtil.isNotNull(idtftp)){
		if (CommUtil.isNull(idtfno)) {
			throw DpModuleError.DpstComm.E9999("证件号码不能为空！");
		}
	}else if(CommUtil.isNotNull(idtfno)) {
		throw DpModuleError.DpstComm.E9999("证件类型不能为空！");
	}
    
	String bgindt = input.getBgindt();
	String enddt = input.getEnddt(); 
	Long pageSize = CommTools.getBaseRunEnvs().getPage_size();
	Long pageStart = CommTools.getBaseRunEnvs().getPage_start();
	Page<AccountBalanceInfo> knlBillInfo = DpAcctDao.selKnlBillByTrandtAndMerInfo(bgindt, enddt, tlphno, idtftp, idtfno, (pageStart-1)*pageSize, pageSize, 0, false);
	output.getKnlBillInfo().setValues(knlBillInfo.getRecords());
	CommTools.getBaseRunEnvs().setTotal_count(knlBillInfo.getRecordCount());
	
}
}
