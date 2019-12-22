
package cn.sunline.edsp.busi.dptran.trans.ca;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.busi.dp.namedsql.ca.EdmAfterDayBatchDao;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccount;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUPSST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SETTST;
import cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.ToTranSettle;
import cn.sunline.edsp.busi.ds.iobus.type.DsEnumType.E_BUSITP;

public class seinfo {

	public static void seinfo(final cn.sunline.edsp.busi.dptran.trans.ca.intf.Seinfo.Input input,
			final cn.sunline.edsp.busi.dptran.trans.ca.intf.Seinfo.Property property,
			final cn.sunline.edsp.busi.dptran.trans.ca.intf.Seinfo.Output output) {
		String orgaid = input.getOrgaid();   	//品牌id		 		
		E_BUSITP busitp = input.getBusitp();   	//业务类型	 		
		String inmeid = input.getInmeid();   	//内部商户号	 	
		String teleno = input.getTeleno();   	//手机号		 		
		String mntrsq = input.getMntrsq();   	//主交易流水	 	
		String stardt = input.getStardt();   	//结算开始日期
		String endtdt = input.getEndtdt();		//结算结束日期
		E_CUPSST transt = input.getTranst();
		E_SETTST settst = input.getSettst();   //结算单状态
		String taskid = input.getTaskid();     //批次号

		Long pageSize = CommTools.getBaseRunEnvs().getPage_size();
		Long pageStart = CommTools.getBaseRunEnvs().getPage_start();

        Page<EdmSettleAccount> edms = EdmAfterDayBatchDao.selectEdmSettleAccountInfo(orgaid, inmeid, busitp, teleno, stardt, endtdt, mntrsq, transt,settst,taskid, (pageStart - 1) * pageSize, pageSize, 0, false);
        ToTranSettle totalTran = EdmAfterDayBatchDao.selTotalTran(orgaid, inmeid, busitp, teleno, stardt, endtdt, mntrsq, transt,settst,taskid, false);      
        if (edms.getRecordCount() == 0) {
        	output.setTotran(new BigDecimal("0"));
            output.setTotoan(new BigDecimal("0"));
			return;
		}

        if(CommUtil.isNotNull(totalTran)) 
        {
        	output.setTotran(totalTran.getTotran());
            output.setTotoan(totalTran.getTotoan());
        	
        }
        else {
        	output.setTotran(new BigDecimal("0"));
            output.setTotoan(new BigDecimal("0"));
        }
        output.getDpToSettleAccounts().addAll(edms.getRecords());
        CommTools.getBaseRunEnvs().setTotal_count(edms.getRecordCount());
	}
}
