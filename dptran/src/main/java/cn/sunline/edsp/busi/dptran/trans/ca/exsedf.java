
package cn.sunline.edsp.busi.dptran.trans.ca;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.util.PropertyUtil;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.busi.dp.namedsql.ca.EdmAfterDayBatchDao;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccount;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUPSST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_EDMTYP;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_FINSTY;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_SBRAND;
import cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.DpToSettleAccounts;
import cn.sunline.edsp.busi.ds.iobus.type.DsEnumType.E_BUSITP;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.KnlIoblStateInfo;
import cn.sunline.ltts.busi.sys.errors.ApError;

public class exsedf {
    private static BizLog bizlog = BizLogUtil.getBizLog(exsein.class);

	public static void exportUnderTake(final cn.sunline.edsp.busi.dptran.trans.ca.intf.Exsedf.Input input, final cn.sunline.edsp.busi.dptran.trans.ca.intf.Exsedf.Property property,
			final cn.sunline.edsp.busi.dptran.trans.ca.intf.Exsedf.Output output) {
        String orgaid = input.getOrgaid(); // 品牌id
		String inmeid = input.getInmeid();
		String refeno = input.getRefeno();
		String prepsq = input.getPrepsq();
		String sacdno = input.getSacdno();
		String sacdna = input.getSacdna();
		E_EDMTYP servtp = input.getServtp();
		E_CUPSST transt = input.getTranst();
		String stadat = input.getStadat();
		String enddat = input.getEnddat();
		//by huang.shoutao 2019-12-06 新增查询条件【订单号】【结算方式】
		String ordeno = input.getOrdeno();
		E_FINSTY finsty = input.getFinsty();
		
		List<KnlIoblStateInfo> pageInfo = DpAcctDao.selKnlIoblStateInfo(orgaid, inmeid, refeno, prepsq, sacdno, sacdna,
				servtp, transt, stadat, enddat,ordeno,finsty,false);
		  for (KnlIoblStateInfo edmSettleAccount : pageInfo) {
	            cn.sunline.edsp.busi.dptran.trans.ca.intf.Exsedf.Output.List data =
	                SysUtil.getInstance(cn.sunline.edsp.busi.dptran.trans.ca.intf.Exsedf.Output.List.class);
	            try {
	                PropertyUtil.copyProperties(edmSettleAccount, data, true, false);
	            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
	                bizlog.error("导出代发记录列表时copyProperties异常", e);
	                throw ApError.Sys.E9006();
	            }
	            if(StringUtils.isNotBlank(edmSettleAccount.getSbrand()))
	            {
	            	 data.setSbrand(CommUtil.toEnum(E_SBRAND.class,edmSettleAccount.getSbrand()).getLongName());
	            }
	            data.setBusitp(edmSettleAccount.getBusitp() == null ? null : edmSettleAccount.getBusitp().getLongName());  
	            data.setCardtp(edmSettleAccount.getCardtp() == null ? null : edmSettleAccount.getCardtp().getLongName());
	            data.setPuacfg(edmSettleAccount.getPuacfg() == null ? null : edmSettleAccount.getPuacfg().getLongName());  
	            data.setFinsty(edmSettleAccount.getFinsty() == null ? null : edmSettleAccount.getFinsty().getLongName());
	            data.setAcfist(edmSettleAccount.getAcfist() == null ? null : edmSettleAccount.getAcfist().getLongName());
	            data.setServtp(edmSettleAccount.getServtp() == null ? null : edmSettleAccount.getServtp().getLongName());  
	            data.setTranst(edmSettleAccount.getTranst() == null ? null : edmSettleAccount.getTranst().getLongName());

	            output.getList().add(data);
	        }
	}
}
