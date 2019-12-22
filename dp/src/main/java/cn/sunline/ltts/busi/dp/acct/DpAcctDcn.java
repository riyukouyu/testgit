package cn.sunline.ltts.busi.dp.acct;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;

/**
 * 处理存款跨DCN逻辑类
 * 
 * @author cuijia
 * 
 */
public class DpAcctDcn {

	private static final BizLog bizlog = BizLogUtil.getBizLog(DpAcctDcn.class);

	/**
	 * 处理DCN节点系统日期不一致，判断应入账日期
	 * 
	 * @param acctno
	 * @param trandt
	 * @param lastdt
	 * @return 应入账日期
	 */
	public static String getAccoutDate(String acctno, String trandt,
			String lastdt) {
		bizlog.parm("获得会计流水应入账日期: 账号[%s], 交易日期[%s], 账户最后余额更新日期 [%s]", acctno,
				trandt, lastdt);

		String acctdt = trandt; // 应入账日期
		
		//当账户日期大于当前系统日期时，已账户过日日期为准
		if (CommUtil.compare(lastdt, trandt) > 0) {
			int iDays = DateTimeUtil.dateDiff("dd", trandt, lastdt);
			if (iDays > 1) {
				throw DpModuleError.DpstAcct.BNAS3010();
			}
			acctdt = lastdt;
			return acctdt;
		}
		// 上送系统日期为主调节点DCN的系统日期
		String mainDcnDate = CommTools.getBaseRunEnvs().getInitiator_date();
		if (CommUtil.compare(trandt, mainDcnDate) == 0) {
			// 主调DCN系统日期等于当前DCN日期，默认即可
		} else if (CommUtil.compare(trandt, mainDcnDate) > 0) {
			// 主调DCN系统日期小于当前DCN日期
			if(CommUtil.compare(trandt, lastdt) > 0){
				//账户没有过日，同时未计息，入账日为T
				int count = DpDayEndDao.seladdcbdlacct(mainDcnDate,acctno, false);
				if(count == 0){
					acctdt = mainDcnDate;
				}
			}
		} else {
			//主调DCN系统日期大于当前DCN日期，以主DCN为准。会出现账户过日日期大于当前系统日期一天，因为主调DCN先日切。
			acctdt = mainDcnDate;
		}

		bizlog.parm("账号[%s], 应入账日期 [%s]", acctno, acctdt);

		return acctdt;
	}
}
