package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.dp.namedsql.DpAcinDao;
import cn.sunline.ltts.busi.dp.type.DpInterestType.KupIntxInfo;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrintx {

public static void qryintx( final cn.sunline.ltts.busi.dptran.trans.intf.Qrintx.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrintx.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrintx.Output output){
	RunEnvsComm runEnves = CommTools.getBaseRunEnvs();
	String corpno = runEnves.getBusi_org_id();
	
	Page<KupIntxInfo> page = DpAcinDao.selIntxInfo(corpno, input.getIntxcd(), input.getEfctdt(), (runEnves.getPage_start() - 1)*runEnves.getPage_size(), runEnves.getPage_size(), runEnves.getTotal_count(),false);
	runEnves.setTotal_count(page.getRecordCount());
	Options<KupIntxInfo> info = new DefaultOptions<KupIntxInfo>();
	info.setValues(page.getRecords());
	
	output.setList01(info);
	//poc增加审计日志
	ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
	apAudit.regLogOnInsertBusiPoc(null);
}
}
