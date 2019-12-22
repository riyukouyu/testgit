
package cn.sunline.edsp.busi.dptran.trans.ca.jfca;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.util.PropertyUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoKnsAcsqInfoWithBradna;
import cn.sunline.ltts.busi.sys.errors.ApError;

public class expqsq {
	private static BizLog bizlog = BizLogUtil.getBizLog(expqsq.class);
public static void expacsq( final cn.sunline.edsp.busi.dptran.trans.ca.jfca.intf.Expqsq.Input input,  final cn.sunline.edsp.busi.dptran.trans.ca.jfca.intf.Expqsq.Property property,  final cn.sunline.edsp.busi.dptran.trans.ca.jfca.intf.Expqsq.Output output){
	bizlog.debug("expqra begin >>>>>>>>>>>>>>>>>>>>>>");
	
	List<IoKnsAcsqInfoWithBradna> acsqInfoWithBradnaList = DpAcctDao.selKnsAcsqInfosByAcctOrSqWithBradnaExp(input.getMntrsq(),input.getBgindt(), input.getEndddt(), input.getAcctno(), input.getSortno(),input.getAcctna(), input.getAmntcd(), input.getAtowtp(), input.getTranam(), input.getBradna(), input.getCardno(), false);
    
	if (CommUtil.isNull(acsqInfoWithBradnaList)) {
		return;
	}
	
	for (IoKnsAcsqInfoWithBradna ioKnsAcsqInfoWithBradna : acsqInfoWithBradnaList) {
		cn.sunline.edsp.busi.dptran.trans.ca.jfca.intf.Expqsq.Output.Exportlist data = SysUtil.getInstance(cn.sunline.edsp.busi.dptran.trans.ca.jfca.intf.Expqsq.Output.Exportlist.class);
		
		try {
			PropertyUtil.copyProperties(ioKnsAcsqInfoWithBradna, data, true, false);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw ApError.Sys.E9006();
		}
		
		// 转义字段
		if (CommUtil.isNotNull(ioKnsAcsqInfoWithBradna.getAmntcd())) {
			data.setAmntcd(ioKnsAcsqInfoWithBradna.getAmntcd().getLocalLongName());
		}
		if (CommUtil.isNotNull(ioKnsAcsqInfoWithBradna.getAtowtp())) {
			data.setAtowtp(ioKnsAcsqInfoWithBradna.getAtowtp().getLocalLongName());
		}
		if (CommUtil.isNotNull(ioKnsAcsqInfoWithBradna.getBltype())) {
			data.setBltype(ioKnsAcsqInfoWithBradna.getBltype().getLocalLongName());
		}
		/*
		if (CommUtil.isNotNull(ioKnsAcsqInfoWithBradna.getStrksq())) {
			data.setStrksq(ioKnsAcsqInfoWithBradna.getStrksq().getLocalLongName());
		}*/
		if (CommUtil.isNotNull(ioKnsAcsqInfoWithBradna.getStatus())) {
			data.setStatus(ioKnsAcsqInfoWithBradna.getStatus().getLocalLongName());
		}
		if (CommUtil.isNotNull(ioKnsAcsqInfoWithBradna.getSacotg())) {
			data.setSacotg(ioKnsAcsqInfoWithBradna.getSacotg().getLocalLongName());
		}
			
		output.getExportlist().add(data);
	}
			
	bizlog.debug("expqra end >>>>>>>>>>>>>>>>>>>>>>");
}
}
