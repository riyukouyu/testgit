package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.KnsAcsqColl;
import cn.sunline.ltts.busi.in.tables.In.KnsAcsqCollDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acctcl.Input;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acctcl.Property;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.IoInTable.IoKnsAcsqColl;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CLERST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INPTSR;

	 /**
	  * 清算记账
	  *
	  */

public class acctclDataProcessor extends

  AbstractBatchDataProcessor<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acctcl.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acctcl.Property, cn.sunline.ltts.busi.iobus.type.IoInTable.IoKnsAcsqColl> {
	private final static BizLog bizlog = BizLogUtil.getBizLog(acctclDataProcessor.class);

	/**
		 * 批次数据项处理逻辑。
		 * 
		 * @param job 批次作业ID
		 * @param index  批次作业第几笔数据(从1开始)
		 * @param dataItem 批次数据项
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 */
		@Override
		public void process(String jobId, int index, cn.sunline.ltts.busi.iobus.type.IoInTable.IoKnsAcsqColl dataItem, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acctcl.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acctcl.Property property) {
			
			bizlog.debug("----------账号："+dataItem.getAcctno()+" 进入清算记账----------");
			//重新设置交易流水
		
 			BigDecimal Damt = dataItem.getDranam();//借方金额
			BigDecimal Camt = dataItem.getCranam();//贷方金额

			
			GlKnaAcct acctnoInfo = SysUtil.getInstance(GlKnaAcct.class);
			GlKnaAcct toacctInfo = SysUtil.getInstance(GlKnaAcct.class);
			acctnoInfo = InQuerySqlsDao.sel_GlKnaAcct_by_acct(dataItem.getAcctno(),true);
			toacctInfo = InQuerySqlsDao.sel_GlKnaAcct_by_acct(dataItem.getToacct(),true);
			
			bizlog.debug("----------新流水："+CommTools.getBaseRunEnvs().getTrxn_seq()+" 进入清算记账----------");
			//借方记账
			if(CommUtil.compare(Damt, BigDecimal.ZERO) !=0 ){
				 MsSystemSeq.getTrxnSeq();;//modify by chenlk 如果一笔流水记完 会导致 9930410405 发生额不对 
				// 调用内部户借方记账服务
				IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
				
				iaAcdrInfo.setTrantp(E_TRANTP.TR);
				iaAcdrInfo.setInptsr(E_INPTSR.GL00);
				iaAcdrInfo.setAcctno(dataItem.getAcctno());
				iaAcdrInfo.setTranam(Damt);// 记账金额
				iaAcdrInfo.setCrcycd(dataItem.getCrcycd());// 币种
				iaAcdrInfo.setAcbrch(BusiTools.getBusiRunEnvs().getCentbr());//清算中心	
				iaAcdrInfo.setDscrtx("清算");
				iaAcdrInfo.setToacct(dataItem.getToacct());//对方账号
				iaAcdrInfo.setToacna(toacctInfo.getAcctna());//对方户名
				
				SysUtil.getInstance(IoInAccount.class).ioInAcdr(
						iaAcdrInfo);
				
				//系统间往来对应贷方记账
				IaAcdrInfo iaAcdrInfo2 = SysUtil.getInstance(IaAcdrInfo.class);
				iaAcdrInfo2.setTrantp(E_TRANTP.TR);
				iaAcdrInfo2.setInptsr(E_INPTSR.GL00);
				iaAcdrInfo2.setAcctno(dataItem.getToacct());
				iaAcdrInfo2.setTranam(Damt); // 记账金额
				iaAcdrInfo2.setAcbrch(dataItem.getBrchno());
				iaAcdrInfo2.setCrcycd(dataItem.getCrcycd());
				iaAcdrInfo2.setToacct(acctnoInfo.getAcctno());
				iaAcdrInfo2.setToacna(acctnoInfo.getAcctna());
				iaAcdrInfo2.setDscrtx("清算");

				
				SysUtil.getInstance(IoInAccount.class).ioInAccr(
						iaAcdrInfo2);// 内部户贷方服
				
				
			} 
			
			if(CommUtil.compare(Camt, BigDecimal.ZERO) !=0 ){
				 MsSystemSeq.getTrxnSeq();	//modify by chenlk 如果一笔流水记完 会导致 9930410405 发生额不对 
				// 调用内部户贷方记账服务
				
				IaAcdrInfo iaAcdrInfo2 = SysUtil.getInstance(IaAcdrInfo.class);
				iaAcdrInfo2.setTrantp(E_TRANTP.TR);
				iaAcdrInfo2.setInptsr(E_INPTSR.GL00);
				iaAcdrInfo2.setAcctno(dataItem.getAcctno());
				iaAcdrInfo2.setTranam(Camt); // 记账金额
				iaAcdrInfo2.setAcbrch(BusiTools.getBusiRunEnvs().getCentbr());
				iaAcdrInfo2.setCrcycd(dataItem.getCrcycd());
				iaAcdrInfo2.setToacct(toacctInfo.getAcctno());
				iaAcdrInfo2.setToacna(toacctInfo.getAcctna());
				iaAcdrInfo2.setDscrtx("清算");
				
				SysUtil.getInstance(IoInAccount.class).ioInAccr(
						iaAcdrInfo2);// 内部户贷方服
				
				//系统间往来对应借方记账
				IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
				iaAcdrInfo.setTrantp(E_TRANTP.TR);
				iaAcdrInfo.setInptsr(E_INPTSR.GL00);
				iaAcdrInfo.setAcctno(dataItem.getToacct());
				iaAcdrInfo.setTranam(Camt);// 记账金额
				iaAcdrInfo.setCrcycd(dataItem.getCrcycd());// 币种
				iaAcdrInfo.setAcbrch(dataItem.getBrchno());//账户机构	
				iaAcdrInfo.setDscrtx("清算");
				iaAcdrInfo.setToacct(acctnoInfo.getAcctno());//对方账号
				iaAcdrInfo.setToacna(acctnoInfo.getAcctna());//对方户名
				
				SysUtil.getInstance(IoInAccount.class).ioInAcdr(
						iaAcdrInfo);
				
			}
			
			//清算记账成功，更新清算汇总明细状态为已清算
			KnsAcsqColl tbknsacsqcoll =  SysUtil.getInstance(KnsAcsqColl.class);
			tbknsacsqcoll.setBathid(dataItem.getBathid());
			tbknsacsqcoll.setAcctno(dataItem.getAcctno());
			tbknsacsqcoll.setBrchno(dataItem.getBrchno());
			tbknsacsqcoll.setClactp(dataItem.getClactp());
			tbknsacsqcoll.setClerdt(dataItem.getClerdt());
			tbknsacsqcoll.setCranam(dataItem.getCranam());
			tbknsacsqcoll.setCrcunt(dataItem.getCrcunt());
			tbknsacsqcoll.setCrcycd(dataItem.getCrcycd());
			tbknsacsqcoll.setDranam(dataItem.getDranam());
			tbknsacsqcoll.setDrcunt(dataItem.getDrcunt());
			tbknsacsqcoll.setProdcd(dataItem.getProdcd());
			tbknsacsqcoll.setStatus(E_CLERST._2);
			tbknsacsqcoll.setTrandt(dataItem.getTrandt());
			tbknsacsqcoll.setClenum(dataItem.getClenum());
			tbknsacsqcoll.setToacct(dataItem.getToacct());
			tbknsacsqcoll.setCorest(dataItem.getCorest());
			tbknsacsqcoll.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());//先登记后一笔流水（暂时没什么用，先这样）
			KnsAcsqCollDao.update_odb1(tbknsacsqcoll);	
					
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.iobus.type.IoInTable.IoKnsAcsqColl> getBatchDataWalker(cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acctcl.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acctcl.Property property) {
			String trandt = DateTools2.getDateInfo().getSystdt();
			
			Params params = new Params();
			params.add("trandt", trandt);
			params.add("status", E_CLERST._1);

			return new CursorBatchDataWalker<IoKnsAcsqColl>(
					InDayEndSqlsDao.namedsql_selKnsacsqcollByDay, params);
		}
		@Override
		public void beforeTranProcess(String taskId, Input input,
					Property property) {
			
		}

}



