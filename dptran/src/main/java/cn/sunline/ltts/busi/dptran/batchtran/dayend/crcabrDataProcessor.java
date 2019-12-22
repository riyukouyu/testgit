package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.dayend.DpDayEndInt;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.CrcabrProcData;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;

	 /**
	  * 负债账户日终计提
	  * 负债账户日终计提
	  *
	  */

public class crcabrDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Crcabr.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Crcabr.Property, cn.sunline.ltts.busi.dp.type.DpDayEndType.CrcabrProcData> {
	private final static BizLog bizlog = BizLogUtil.getBizLog(BizLog.class);
	
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
		public void process(
				String jobId, 
				int index, 
				cn.sunline.ltts.busi.dp.type.DpDayEndType.CrcabrProcData dataItem, 
				cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Crcabr.Input input, 
				cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Crcabr.Property property) {
			
			KnbAcin tblKnbAcin = KnbAcinDao.selectOneWithLock_odb1(dataItem.getAcctno(), true);
			
			//计息处理: 计提是在日切后执行, 计提后下次计息日期就更新为交易日期, 因此下次计息日小于交易日期是计息执行的条件
			if(CommUtil.compare(tblKnbAcin.getNxindt(), dataItem.getTrandt())<0){

//				if(tblKnbAcin.getIncdtp() == E_IRCDTP.LAYER){ //分层计提
//					DpDayEndLayerClcIntr.prcCrcLay(tblKnbAcin,dataItem.getLstrdt(),dataItem.getTrandt());
//					return;
//				}


				DpDayEndInt.prcCrcabr(tblKnbAcin,dataItem.getLstrdt(),dataItem.getTrandt(),E_YES___.NO); 
			}else{
				bizlog.error("负债账户"+tblKnbAcin.getAcctno()+"计提日期[%s]不满足计提条件，不计提", tblKnbAcin.getNxindt());
			}
			
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.dp.type.DpDayEndType.CrcabrProcData> getBatchDataWalker(
				cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Crcabr.Input input, 
				cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Crcabr.Property property) {
			
			String trandt = DateTools2.getDateInfo().getSystdt();
			String lstrdt = DateTools2.getDateInfo().getLastdt();
			
			Params params = new Params();
			params.add("trandt", trandt);
			params.add("lstrdt", lstrdt);
			return new CursorBatchDataWalker<CrcabrProcData>(
					DpDayEndDao.namedsql_selCrcabrAcctData, params);

		}

}


