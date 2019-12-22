package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.dp.dayend.DpDayEndInt;
import cn.sunline.ltts.busi.dp.layer.LayerAccrued;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCabrFxdrDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbdpDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.CrcabrProcData;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;

/**
 * 负债账户日终增量计提
 * 
 */

public class addabrDataProcessor
		extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.intf.Addabr.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Addabr.Property, cn.sunline.ltts.busi.dp.type.DpDayEndType.CrcabrProcData> {
	private final static BizLog bizlog = BizLogUtil.getBizLog(BizLog.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param job
	 *            批次作业ID
	 * @param index
	 *            批次作业第几笔数据(从1开始)
	 * @param dataItem
	 *            批次数据项
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(String jobId, int index,
			cn.sunline.ltts.busi.dp.type.DpDayEndType.CrcabrProcData dataItem,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Addabr.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Addabr.Property property) {


		int count = 0;
		E_YES___ isAdd = E_YES___.NO;// 是否增量计提标志

		KnbAcin tblKnbAcin = KnbAcinDao.selectOneWithLock_odb1(
				dataItem.getAcctno(), true);
		// 最后预额变动日
		String lstTranDay = "";
		// 新开户日期
		String OpenDay = "";
		if (CommUtil.equals(E_FCFLAG.CURRENT.getValue(), tblKnbAcin
				.getPddpfg().getValue())) {
			// 获取活期账户信息
			KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(
					dataItem.getAcctno(), true);
			lstTranDay = tblKnaAcct.getUpbldt();
			OpenDay = tblKnaAcct.getOpendt();
		} else {
			// 获取定期账户信息
			KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(
					dataItem.getAcctno(), true);
			lstTranDay = tblKnaFxac.getUpbldt();
			OpenDay = tblKnaFxac.getOpendt();
		}

		
		String sEdindt = DateTools2.getDateInfo().getLastdt();
		
		String systdt = DateTools2.getDateInfo().getSystdt();
		
		systdt = DateTools2.dateAdd (-1, systdt);

		// 新开户不用清理已预计提数据，直接正常计提
		// if(CommUtil.compare(OpenDay, sEdindt)==0){
		//
		// //根据计提明细表中是否有已增量计提数据，判断是否已增量计提
		// count = DpDayEndDao.seladdcbdlacct(dataItem.getLstrdt(),
		// dataItem.getAcctno(), false);
		// if(CommUtil.compare(count, 0)>0){
		// bizlog.error("负债账户"+dataItem.getAcctno()+"计提日期[%s]已增量计提，将跳过不重复增量计提",
		// dataItem.getLstrdt());
		// return;
		// }
		//
		// DpDayEndInt.prcCrcabr(tblKnbAcin,dataItem.getLstrdt(),dataItem.getTrandt(),E_YES___.YES);
		// }else
		
		// 增加检查是否跳日也需要增量计提, 备注：如果跳日增量计提上一日的计提利息与登记日期可能不相符
		if (CommUtil.compare(lstTranDay, sEdindt) >= 0 || CommUtil.compare(systdt, sEdindt) > 0) { // 判断余额是否变动，若变动，则清除已预计提数据，重新增量计提

			// 根据计提明细表中是否有已增量计提数据，判断是否已增量计提
			count = DpDayEndDao.seladdcbdlacct(dataItem.getLstrdt(),dataItem.getAcctno(), false);
			if (CommUtil.compare(count, 0) > 0) {
				bizlog.error("负债账户" + dataItem.getAcctno() + "计提日期[%s]已增量计提，将跳过不重复增量计提", dataItem.getLstrdt());
				return;
			}
			
			isAdd = E_YES___.YES;

		} else {
			// 根据计提明细表中是否有已预计提数据，判断是否已预计提，若未预计提则补计提
			count = DpDayEndDao.selBefCbdlAcct(dataItem.getLstrdt(),dataItem.getAcctno(), false);
			if (CommUtil.compare(count, 0) == 0) {
				isAdd = E_YES___.YES;
			}
		}

		if (CommUtil.equals(isAdd.getValue(), E_YES___.YES.getValue())) {
			// 删除已登记智能储蓄支出计提明细
			if (CommUtil.equals(E_FCFLAG.FIX.getValue(), tblKnbAcin
					.getPddpfg().getValue())) {
				if (CommUtil.equals(E_YES___.YES.getValue(), tblKnbAcin.getDetlfg().getValue())) {
					KnbCbdpDao.delete_odb3(dataItem.getLstrdt(),dataItem.getAcctno());
					KnbCabrFxdrDao.deleteOne_odb1(dataItem.getLstrdt(),dataItem.getAcctno());
				}
			}

			DpDayEndDao.delknbcbdlData(dataItem.getLstrdt(),dataItem.getAcctno()); // 删除已预计提数据

			// 重新正常计提上日数据
			String trandt = dataItem.getTrandt();
			String lasttrandt = dataItem.getLstrdt();
			if (tblKnbAcin.getIncdtp() == E_IRCDTP.LAYER && tblKnbAcin.getInclfg() == E_YES___.NO) { // 分层计提
				LayerAccrued.prcCrcLay(tblKnbAcin, lasttrandt, trandt,E_YES___.YES);
			} else {
				DpDayEndInt.prcCrcabr(tblKnbAcin, lasttrandt, trandt,E_YES___.YES);
			}

		}
	}

	/**
	 * 获取数据遍历器。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<cn.sunline.ltts.busi.dp.type.DpDayEndType.CrcabrProcData> getBatchDataWalker(
			cn.sunline.ltts.busi.dptran.batchtran.intf.Addabr.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Addabr.Property property) {
		
		ApSysDateStru cplDate = DateTools2.getDateInfo();
		String trandt = cplDate.getSystdt();
		String lstrdt = cplDate.getLastdt();
		String dedate = DateTools2.dateAdd (-1, trandt);//系统日期减一天

		Params params = new Params();
		params.add("trandt", trandt);
		params.add("lstrdt", lstrdt);
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		if(CommUtil.compareIgnoreCase(dedate, lstrdt)>0){ 
			//跳跑全量计提
			params.add("abrall", "1");
		}else{
			
			params.add("abrall", "0");	
		}
		

		return new CursorBatchDataWalker<CrcabrProcData>(
				DpDayEndDao.namedsql_selCrcabrAcctData, params);
	}

}
