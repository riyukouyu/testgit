package cn.sunline.ltts.busi.dptran.batchtran;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KcbChrgAgbl;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KcbChrgAgblDao;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsq;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

	 /**
	  * 费种汇总余额
	  *
	  */

public class cragblDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.intf.Cragbl.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Cragbl.Property, cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsq> {
	  
	  private static final BizLog bizlog = BizLogUtil.getBizLog(cragblDataProcessor.class);
	  
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsq dataItem, cn.sunline.ltts.busi.dptran.batchtran.intf.Cragbl.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Cragbl.Property property) {

			bizlog.method(">>>>>>>>>>>>>>>>>>>>>费用汇总余额开始>>>>>>>>>>>>>>>>>>>>>>>");
			
			String trandt = DateTools2.getDateInfo().getLastdt(); //上一日日期
			String tranms = dataItem.getTranms(); // 交易信息
			String prodcd = dataItem.getProdcd();//产品编号
			String crcycd = dataItem.getCrcycd(); //币种
			BigDecimal tranam = dataItem.getTranam();//交易金额
			bizlog.debug("----------交易金额[%s]---------", tranam);

			if(CommUtil.isNotNull(tranam) && CommUtil.isNotNull(prodcd)){
				KcbChrgAgbl tblKcbChrgAgbl = SysUtil.getInstance(KcbChrgAgbl.class);
				
				//根据交易信息和产品编号查询费用汇总信息
				tblKcbChrgAgbl = KcbChrgAgblDao.selectOne_odb1(tranms, prodcd, crcycd, false);
				
				if(CommUtil.isNotNull(tblKcbChrgAgbl)){
					if(dataItem.getAmntcd().equals(E_AMNTCD.DR)){
						tranam = tblKcbChrgAgbl.getChrgam().add(tranam);
					}else if(dataItem.getAmntcd().equals(E_AMNTCD.CR)){
						tranam = tblKcbChrgAgbl.getChrgam().subtract(tranam);
					}else{
						throw DpModuleError.DpstComm.E9999("借贷标志错误");
					}
					
					tblKcbChrgAgbl.setTrandt(trandt);
					tblKcbChrgAgbl.setTranms(tranms);
					tblKcbChrgAgbl.setProdcd(prodcd);
					tblKcbChrgAgbl.setCrcycd(crcycd);
					tblKcbChrgAgbl.setChrgam(tranam);
					tblKcbChrgAgbl.setAmntcd(E_AMNTCD.DR);
					
					//更新登记簿
					KcbChrgAgblDao.updateOne_odb1(tblKcbChrgAgbl);
				}else{
					tblKcbChrgAgbl = SysUtil.getInstance(KcbChrgAgbl.class);
					tblKcbChrgAgbl.setTrandt(trandt);
					tblKcbChrgAgbl.setTranms(tranms);
					tblKcbChrgAgbl.setProdcd(prodcd);
					tblKcbChrgAgbl.setCrcycd(crcycd);
					tblKcbChrgAgbl.setChrgam(tranam);
					tblKcbChrgAgbl.setAmntcd(E_AMNTCD.DR);
					
					//新增登记簿
					KcbChrgAgblDao.insert(tblKcbChrgAgbl);
				}
			}
			bizlog.method(">>>>>>>>>>>>>>>>>>>>>费用汇总余额结束>>>>>>>>>>>>>>>>>>>>>>>");
			
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsq> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.intf.Cragbl.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Cragbl.Property property) {

			bizlog.method(">>>>>>>>>>>>>>>>>>>>>>>>>BatchDataWalker begin>>>>>>>>>>>>>>>>>>>>>");
			
			Params para = new Params();
			para.add("trandt", DateTools2.getDateInfo().getLastdt());
			
			bizlog.method(">>>>>>>>>>>>>>>>>>>>>>>>>BatchDataWalker end>>>>>>>>>>>>>>>>>>>>>");
			
			return new CursorBatchDataWalker<KnsAcsq>(DpDayEndDao.namedsql_selChgSumAmtByTra, para);
		}

}


