package cn.sunline.ltts.busi.intran.batchtran.dayend;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

	 /**
	  * 总账计提利息供数
	  *
	  */

public class necbtlDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.intf.Necbtl.Input, cn.sunline.ltts.busi.intran.batchtran.intf.Necbtl.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(necbtlDataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.intran.batchtran.intf.Necbtl.Input input, cn.sunline.ltts.busi.intran.batchtran.intf.Necbtl.Property property) {
		 bizlog.debug("---------开始进行计提利息汇总-------");
		 String acctdt = DateTools2.getDateInfo().getLastdt();
		 String lastdt = DateTools2.getDateInfo().getBflsdt();
		 String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		 InDayEndSqlsDao.Del_Knb_Cbtl(acctdt,corpno);		//删除当日计提利息汇总信息，支持重跑
		 InDayEndSqlsDao.Ins_new_knb_cbtl(acctdt,corpno); //登记计提利息汇总信息  knb_cbtl
		 
		 InDayEndSqlsDao.Del_Lnb_Cbtl(acctdt,corpno);		//删除当日贷款计息汇总信息，支持重跑
		 InDayEndSqlsDao.Ins_new_lnb_cbtl(acctdt,corpno); //登记计贷款计息汇总信息  lnb_cbtl
		 
		 //
		 /*
		 InDayEndSqlsDao.Del_Cbtl(acctdt);		//删除当日计提利息汇总信息，支持重跑
		 InDayEndSqlsDao.Ins_newcbtl(acctdt); //登记计提利息汇总信息  knb_cbtl
		 InDayEndSqlsDao.Del_Lttl(acctdt);	//删除上一日利息支出汇总信息，支持重跑
		 InDayEndSqlsDao.Ins_Lttl(acctdt,DateTools.getTransTimestamp());//登记上一日利息支出汇总信息 knb_lttl
		 
		 List<IoDpTable.IoKnbCbtl> cains = new ArrayList<IoDpTable.IoKnbCbtl>();
		 
		 //获取计提利息汇总明细数据 
		 cains = InDayEndSqlsDao.selknb_cbtl_selectAll(acctdt, false);


		 
		 if(CommUtil.isNotNull(cains)){
			 IoDpTable.IoKnbCbtl cain = SysUtil.getInstance(IoDpTable.IoKnbCbtl.class);
			 for (int i = 0; i < cains.size(); i++) {		
				 cain = cains.get(i);
				 
				 BigDecimal tranam = cain.getCabrin();  //计提汇总金额
				
				 //定位对应的上一日计提汇总信息
				 IoDpTable.IoKnbCbtl ltdcain = SysUtil.getInstance(IoDpTable.IoKnbCbtl.class);
				 ltdcain = InDayEndSqlsDao.sellstcbtl(lastdt, cain.getBrchno(), cain.getCrcycd(), cain.getProdcd(), false);  
				 if(CommUtil.isNotNull(ltdcain)){
					 BigDecimal ltdtranam = ltdcain.getCabrin();  //上一日计提汇总金额
					 tranam = tranam.subtract(ltdtranam);
					 bizlog.debug("---------上日计提利息汇总======="+ltdtranam);
				 }
								 
				 //定位上一日是否有对应的利息支出汇总
				 IoDpTable.IoKnbCbtl lstknb_lttl = SysUtil.getInstance(IoDpTable.IoKnbCbtl.class);
				 lstknb_lttl = InDayEndSqlsDao.selOnelttl(acctdt, cain.getBrchno(), cain.getCrcycd(), cain.getProdcd(), false);
				 if(CommUtil.isNotNull(lstknb_lttl)){
					 
					 tranam = tranam.add(lstknb_lttl.getCabrin());
					 //假设当前系统日期（已经日切）11号， 则差额计提利息= 10号的应付 -9号的利息计提+10号利息支出
					 
				 }
				 
				 
				 
				 IoDpTable.IoknblttlbatchInfo lstknblttl = SysUtil.getInstance(IoDpTable.IoknblttlbatchInfo.class);
				 lstknblttl.setTrandt(acctdt);
				 

				 String taskid =acctdt+"01";
				 
				 //利息调整不为0，才进行登记
				 if(CommUtil.compare(tranam, BigDecimal.ZERO)!=0){
					 
					 lstknblttl.setTaskid(taskid);//批次号
					 lstknblttl.setOpbrch("");
					 lstknblttl.setCompany(cain.getBrchno());
					 lstknblttl.setBrchno(cain.getBrchno());
					 lstknblttl.setProdcd(cain.getProdcd());
					 lstknblttl.setChnlbr(String.NK.getValue());//电子账户系统
					 lstknblttl.setIntercom("0");//合并往来
					 lstknblttl.setSource("93");//来源
					 lstknblttl.setSpare1("0");//备用1
					 lstknblttl.setSpare2("0");//备用2
					 lstknblttl.setCrcycd(cain.getCrcycd());//币种
					 lstknblttl.setTranam(tranam);//金额
					 lstknblttl.setTrantype("3010100");//交易类型
					 lstknblttl.setFiledate(DateTools2.getSystemDate());
					 int flag = 0;
					 flag = SysUtil.getInstance(IoInQuery.class).knbLttlBatchInsert(lstknblttl);
					 if(flag>0){
						 bizlog.debug("新增总账计提利息汇总文件明细成功"+cain.getBrchno());
					 }
				 }
				
			 }	
		 }*/
	 }

}


