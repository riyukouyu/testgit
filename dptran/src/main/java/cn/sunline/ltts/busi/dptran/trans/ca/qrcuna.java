package cn.sunline.ltts.busi.dptran.trans.ca;

import cn.sunline.clwj.msap.core.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.type.CaCustInfo.CustNaInfo;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrcuna {

	public static void queryCustnaList( final cn.sunline.ltts.busi.dptrans.trans.ca.intf.Qrcuna.Input input,  final cn.sunline.ltts.busi.dptrans.trans.ca.intf.Qrcuna.Output output){
		RunEnvsComm runEnvs = CommTools.getBaseRunEnvs();
		String custna = input.getCustna();
		Page<CustNaInfo> page = CaDao.selCustnaListInfo(custna, (runEnvs.getPage_start() - 1)*runEnvs.getPage_size(), runEnvs.getPage_size(), runEnvs.getTotal_count(), false);
		
		runEnvs.setTotal_count(page.getRecordCount());
		Options<CustNaInfo> info = new DefaultOptions<CustNaInfo>();
		info.setValues(page.getRecords());
		
		output.setList01(info);
	}

}
