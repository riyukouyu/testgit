package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.dayend.DpDayEndInt;
import cn.sunline.ltts.busi.dp.froz.DpFrozTools;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozEdct;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozEdctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProd;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.InstpyTranData;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.edsp.base.lang.Params;

	 /**
	  * 定期支取每日结息
	  *
	  */

public class drinstDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Drinst.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Drinst.Property, cn.sunline.ltts.busi.dp.type.DpDayEndType.InstpyTranData> {
	private static final String trandt = DateTools2.getDateInfo().getLastdt();
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.dp.type.DpDayEndType.InstpyTranData dataItem, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Drinst.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Drinst.Property property) {
			
			
			CommTools.getBaseRunEnvs().setTrxn_date(trandt);
			CommTools.getBaseRunEnvs().setLast_date(DateTools2.getDateInfo().getBflsdt());
			
			
			//获取对照表
			//kna_accs accs = Kna_accsDao.selectOne_odb2(dataItem.getAcctno(), true);
			IoCaKnaAccs accs = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAccsOdb2(dataItem.getAcctno(), true);
			
			boolean flag = DpFrozTools.isFroz(E_FROZOW.AUACCT, accs.getCustac(), E_YES___.YES, E_YES___.NO, E_YES___.YES);
			// 不能进的冻结，写入登记簿，直接退出
			if (flag) {
				// 写入登记簿
				KnbFrozEdct tblKnbfrozedct = SysUtil
						.getInstance(KnbFrozEdct.class);
				tblKnbfrozedct.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
				tblKnbfrozedct.setCustac(accs.getCustac());
				tblKnbfrozedct.setAcctno(dataItem.getAcctno());
				tblKnbfrozedct.setFailds("已被冻结，结息失败。");
				KnbFrozEdctDao.insert(tblKnbfrozedct);
				return;
			}
			
			//取值产品名称
			String remark = "";
			KnaFxacProd tblFxacProd = DpAcctDao.selKnaFxacProdByAcctno(dataItem.getAcctno(), false);
			
			if(CommUtil.isNotNull(tblFxacProd)){
				remark = "定期-"+tblFxacProd.getObgaon();
			}
			BusiTools.getBusiRunEnvs().setRemark(remark);
			BusiTools.getBusiRunEnvs().setSmrycd(BusinessConstants.SUMMARY_FX);
			BusiTools.getBusiRunEnvs().setRemark(ApSmryTools.getText(BusinessConstants.SUMMARY_FX));
			//定期支取部分结息
			DpDayEndInt.prcDrawInstPay(dataItem.getAcctno(),accs.getCrcycd(),trandt);
			
			
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.dp.type.DpDayEndType.InstpyTranData> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Drinst.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Drinst.Property property) {
			//trandt = DateTools2.getDateInfo().getSystdt();
			Params params = new Params();
			params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
			return new CursorBatchDataWalker<InstpyTranData>(
					DpDayEndDao.namedsql_selDrawAcctData, params);
		}

}


