
package cn.sunline.edsp.busi.dptran.trans.ca;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.busi.dp.namedsql.ca.EdmAfterDayBatchDao;
import cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.DpToSettleAccounts;
import cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.ToTranSettle;
import cn.sunline.edsp.busi.ds.iobus.type.DsEnumType.E_BUSITP;

/**
 * 
 * @author sunline16
 *
 */
public class toseac {

	public static void toseac(final cn.sunline.edsp.busi.dptran.trans.ca.intf.Toseac.Input input,
			final cn.sunline.edsp.busi.dptran.trans.ca.intf.Toseac.Property property,
			final cn.sunline.edsp.busi.dptran.trans.ca.intf.Toseac.Output output) {
		String orgaid = input.getOrgaid();   	//品牌id		 		
		E_BUSITP busitp = input.getBusitp();   	//业务类型	 		
		String inmeid = input.getInmeid();   	//内部商户号	 	
		String teleno = input.getTeleno();   	//手机号		 		
		String mntrsq = input.getMntrsq();   	//主交易流水	 	
		String stardt = input.getStardt();   	//结算开始日期
		String endtdt = input.getEndtdt();		//结算结束日期
		String ordeno = input.getOrdeno();	    //订单号
		String refeno = input.getRefeno();		//参考号

		Long pageSize = CommTools.getBaseRunEnvs().getPage_size();
		Long pageStart = CommTools.getBaseRunEnvs().getPage_start();
		
        Page<DpToSettleAccounts> DpToSettleAccounts = EdmAfterDayBatchDao.selectKnlIoblCupsInfo(orgaid, inmeid, busitp, teleno, stardt, endtdt, mntrsq, ordeno,refeno,(pageStart - 1) * pageSize, pageSize, 0, false);
        ToTranSettle totalTran = EdmAfterDayBatchDao.selTotalYuTran(orgaid, inmeid, busitp, teleno, stardt, endtdt, mntrsq,ordeno,refeno, false);           
         
        if (DpToSettleAccounts.getRecordCount() == 0) {
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
        
        
        BigDecimal rate = new BigDecimal("0");
		BigDecimal rate1 = new BigDecimal("0");
		BigDecimal rate4 = new BigDecimal("0");
		BigDecimal rate6 = new BigDecimal("0");
		
		for(DpToSettleAccounts cup :DpToSettleAccounts.getRecords()) {
			String merate = cup.getInflrt();
			if(CommUtil.isNotNull(merate)) {
				String [] str = merate.split("\\|");
				String str1 = str[0];
				String str4 = str[4];
				String str6 = str[5];
				if(CommUtil.isNotNull(str1)&&!"null".equals(str1)) {
					rate1= new BigDecimal(str1);
				}
				if(CommUtil.isNotNull(str4)&&!"null".equals(str4)) {
					rate4= new BigDecimal(str4);
				}
				if(CommUtil.isNotNull(str6)&&!"null".equals(str6)) {
					rate6= new BigDecimal(str6);
				}
				rate = rate1.add(rate4);
				cup.setInflrt(rate.toString());
				cup.setScflpo(rate6.toString());
				
			}
		}
        output.getDpToSettleAccounts().addAll(DpToSettleAccounts.getRecords());
        CommTools.getBaseRunEnvs().setTotal_count(DpToSettleAccounts.getRecordCount());

	}
}
