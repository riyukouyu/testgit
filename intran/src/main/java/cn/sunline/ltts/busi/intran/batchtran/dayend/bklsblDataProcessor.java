package cn.sunline.ltts.busi.intran.batchtran.dayend;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;

	 /**
	  * 备份历史余额
	  *
	  */

public class bklsblDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Bklsbl.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Bklsbl.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Bklsbl.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Bklsbl.Property property) {
		 
		 String acctdt = CommTools.getBaseRunEnvs().getLast_date(); 
		 String corpno = CommTools.getBaseRunEnvs().getBusi_org_id(); 
		 
		 if(CommUtil.isNull(InDayEndSqlsDao.Sel_H_Acct(acctdt,false))){
			 InDayEndSqlsDao.Ins_Acct_Lsbl(acctdt,corpno);
		 }
		 if(CommUtil.isNull(InDayEndSqlsDao.Sel_H_Fxac(acctdt,false))){
			 InDayEndSqlsDao.Ins_Fxac_Lsbl(acctdt,corpno);
		 }
		 
		 if(CommUtil.isNull(InDayEndSqlsDao.selLnaAcctHistByAcctdt(acctdt,corpno, false))){
			 //插入账务日期为日切前的贷款账户信息到历史备份表
			 InDayEndSqlsDao.insLnaAcctHistByAcctdt(corpno, acctdt, Long.valueOf(DateTools2.getCurrentTimestamp()));
			//插入账务日期为日切后的贷款账户信息到历史备份表
			// InDayEndSqlsDao.insLnaAcctHistByLastdt(acctdt, DateTools2.getCurrentTimestamp());
		 }
		 
		 //20151202 wxq add knl_bill 历史数据转储
		 //账户余额发生明细数据转储 
		 InDayEndSqlsDao.insHKnlBill(acctdt, corpno);
		 
		 //默认删除30天前数据(knl_bill)
		 String delDate = null;
		 KnpParameter para = KnpParameterDao.selectOne_odb1("DELETE_DATA_DAYS", "knl_bill", "%", "%", false);
		if(CommUtil.isNotNull(para) && CommUtil.isNotNull(para.getParm_value1())) {
			int daysAgo = Integer.parseInt(para.getParm_value1());
			delDate = DateTimeUtil.dateAdd("day", acctdt, daysAgo);
		} else {
			delDate = DateTimeUtil.dateAdd("day", acctdt, -30);
		}
		 //删除30天前账户余额发生明细数据 
		 InDayEndSqlsDao.delKnlBillByTrandt(delDate,corpno);
		 
	}

}


