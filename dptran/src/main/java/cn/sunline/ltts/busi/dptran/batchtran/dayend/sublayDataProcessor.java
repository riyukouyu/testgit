package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.ListBatchDataWalker;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.layer.LayerAcctSrv;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.LayerAdinType;
import cn.sunline.ltts.busi.iobus.servicetype.IoPbTableSvr;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.pb.IoIntrComplexType.BankIntrInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoIntrComplexType.DpInstRfir;
import cn.sunline.ltts.busi.iobus.type.pb.IoItpfComplexType.IoLayInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoItpfComplexType.IoLayInfoIn;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.PbError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRSRPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_LYINWY;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_INTRTY;

/**
 * 利率调整(分层账户分段处理)
 * 
 */

public class sublayDataProcessor
		extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Sublay.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Sublay.Property, cn.sunline.ltts.busi.dp.type.DpDayEndType.LayerAdinType> {

	private static final BizLog bizlog = BizLogUtil
			.getBizLog(sublayDataProcessor.class);

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
	public void process(
			String jobId,
			int index,
			LayerAdinType dataItem,
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Sublay.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Sublay.Property property) {

		bizlog.debug("开始调整账户[" + dataItem.getAcctno() + "]的利率");
		bizlog.debug("dataItem====================" + dataItem);
		
		String timetm = DateTools2.getCurrentTimestamp();
		
		List<KubInrt> lstInrt = LayerAcctSrv.getKubInrt(dataItem.getAcctno()); // 获取账户利率信息
		
		BigDecimal onlnbl = DpPublic.getOnlnblByAcctno(dataItem.getAcctno());
		String currdt = CommTools.getBaseRunEnvs().getTrxn_date();
		dataItem.setOnlnbl(onlnbl);
		int mark = LayerAcctSrv.getLayerMark(lstInrt, 0L, onlnbl);
		// 写入负债账户利息明细表
		dealIndl(dataItem, lstInrt, onlnbl, mark);

		// 修改负债账户计息明细
		DpDayEndDao.upAcinForSubByAcctno(dataItem.getAcctno(), currdt, currdt,
				BigDecimal.ZERO, E_INDLTP.UPIR, currdt,timetm);
		// 修改负债账户利率明细
		DpDayEndDao.updAdinLayerData(BigDecimal.ZERO, currdt, BigDecimal.ZERO,
				dataItem.getAcctno(),timetm);

		// 查询当前执行利率和基准利率
		IoLayInfoIn entity = SysUtil.getInstance(IoLayInfoIn.class);

		entity.setCrcycd(dataItem.getCrcycd());
		entity.setIntrcd(dataItem.getIntrcd());
		entity.setIntrkd(E_INTRTY.DP);
//		entity.setEfctdt(currdt);
		Options<IoLayInfo> layInfo = SysUtil.getInstance(
				IoSrvPbInterestRate.class).getLayerIntr(entity);
		// 修改账户利率信息表
		for (IoLayInfo info : layInfo) {
			DpDayEndDao.updAdinLayerIntr(info.getBaseir(), info.getIntrvl(),
					dataItem.getAcctno(), info.getInplsq(),timetm);
		}

	}

	private static void dealIndl(LayerAdinType dataItem,
			List<KubInrt> inrts, BigDecimal onlnbl, int mark) {
		String currdt = CommTools.getBaseRunEnvs().getTrxn_date();
		String lsamdt = dataItem.getLaamdt();
		KnbIndl indl = SysUtil.getInstance(KnbIndl.class);
		BigDecimal bal = BigDecimal.ZERO;
		for (int i = 0; i < inrts.size(); i++) {
			indl.setAcbsin(inrts.get(i).getBsintr());// 基准利率
			indl.setAcctno(dataItem.getAcctno());
			if (dataItem.getLyinwy() == E_LYINWY.ALL) { // 全额累进
				if (i == mark) {
					bal = onlnbl;
					indl.setAcmltn(DpPublic.calRealTotalAmt(inrts.get(i)
							.getClvsmt(), bal, currdt, lsamdt));// 积数
				} else {
					indl.setAcmltn(inrts.get(i).getClvsmt());// 积数
				}
				// indl.setCuusin(inrts.get(i).getCuusin());//当前执行利率
			} else if (dataItem.getLyinwy() == E_LYINWY.OVER) {
				if (i < mark) {
					bal = inrts.get(i).getLvamot();
					indl.setAcmltn(DpPublic.calRealTotalAmt(inrts.get(i)
							.getClvsmt(), bal, currdt, lsamdt));// 积数
				} else if (i == mark) {
					bal = onlnbl.subtract(inrts.get(i).getLvamot());
					indl.setAcmltn(DpPublic.calRealTotalAmt(inrts.get(i)
							.getClvsmt(), bal, currdt, lsamdt));// 积数
				} else {
					indl.setAcmltn(inrts.get(i).getClvsmt());// 积数
				}

			}
			indl.setCuusin(inrts.get(i).getCuusin());// 当前执行利率
			indl.setDetlsq(getDetlsq(dataItem.getAcctno(), dataItem.getIntrtp()));// 明细序号
			indl.setIncdtp(dataItem.getIncdtp());// 利率代码类型
			indl.setIndlst(E_INDLST.YOUX);// 负债利息明细状态
			indl.setIndxno(getIndexNo(dataItem.getAcctno(),
					dataItem.getIntrtp()));
			indl.setIneddt(currdt);// 计息终止日期
			indl.setInstdt(dataItem.getInstdt());// 计息开始日期
			indl.setIntrcd(dataItem.getIntrcd());// 利率编号
			indl.setIntrdt(currdt);// 计息日期
			indl.setIntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 计息流水
			indl.setIntrwy(inrts.get(i).getIntrwy());// 利率靠档方式
			indl.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id()); //法人代码

			indl.setLsinoc(E_INDLTP.UPIR);// 上次利息操作代码
			indl.setLvamot(inrts.get(i).getLvamot()); // 层次金额
			indl.setLvindt(inrts.get(i).getLvindt()); // 层次利率存期
			indl.setLyinwy(dataItem.getLyinwy()); // 分层计息方式
			indl.setTxbebs(dataItem.getTxbebs()); // 计息基础
			indl.setIntrtp(dataItem.getIntrtp()); // 利息类型
			
			try {
				KnbIndlDao.insert(indl);
			} catch (Exception e) {
				DpModuleError.DpstAcct.BNAS1735();
			}
		}
	}

	// 循序号 和 明细序号
	private static Long getDetlsq(String acctno, E_INTRTP intrtp) {
		// 查询该账户信息是否存在该表中
		long count = 0;
		try {
			count = DpDayEndDao.getAcctnoIsInIndl(acctno, intrtp, true);
		} catch (Exception e) {
			PbError.Intr.E9999("查询序号失败，其他错误");
		}
		return count + 1;

	}

	// 明细序号 暂时都为1
	private static Long getIndexNo(String acctno, E_INTRTP intrtp) {

		return new Long(1);
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
	public BatchDataWalker<LayerAdinType> getBatchDataWalker(
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Sublay.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Sublay.Property property) {
		// 1、扫行内利率表
		IoPbTableSvr pbtab = SysUtil.getInstance(IoPbTableSvr.class);
		List<BankIntrInfo> infolist = new ArrayList<BankIntrInfo>();
		List<LayerAdinType> data = new ArrayList<>();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		bizlog.debug("当前日期============" + trandt);
		bizlog.debug("当前日期============" + trandt);
		E_TERMCD depttm = E_TERMCD.T000;
		//infolist = IntrDao.selBankIntrByTerm(trandt, depttm, false);
		infolist = pbtab.IntrDao_selBankIntrByTerm(trandt, depttm, false);
		for (BankIntrInfo info : infolist) {
			bizlog.debug("kaishi=====================" + info.getRfirtm()
					+ "||" + info.getRfircd());
			if (info.getIntrsr() == E_IRSRPF.CK) {
				DpInstRfir rifr = pbtab.IntrDao_selRfir(info.getRfirtm(), trandt,
						info.getRfircd(), false);
				if (CommUtil.isNotNull(rifr)) {
					String intrcd = info.getIntrcd();
					List<LayerAdinType> list = new ArrayList<>();
					try {
						list = DpDayEndDao.selAdinLayerData(intrcd,
								info.getCrcycd(), trandt, E_IRCDTP.LAYER, true);

					} catch (Exception e) {

						return new ListBatchDataWalker<LayerAdinType>(data);
					} 
					bizlog.debug("list===================" + list);
					bizlog.debug("shuju=====================" + data);
					data.addAll(list);
				}
			}
		}
		bizlog.debug("shuju=====================" + data);
		return new ListBatchDataWalker<LayerAdinType>(data);
	}

}
