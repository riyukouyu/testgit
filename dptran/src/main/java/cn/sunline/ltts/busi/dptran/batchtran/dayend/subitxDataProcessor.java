package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.IntrTaxList;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.KtpAdinType;
import cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitx.Input;
import cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitx.Property;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbInRaSelSvc;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;

	 /**
	  * 利息税率调整
	  * 税率
	  *
	  */

public class subitxDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitx.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitx.Property, cn.sunline.ltts.busi.dp.type.DpDayEndType.KtpAdinType> {
	  
	private static BizLog bizlog = BizLogUtil.getBizLog(subitxDataProcessor.class);
	
	private static Map<String, IntrTaxList> mapTax = null;
	
	private static boolean isrun = true;
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
	public void process(String jobId, int index, cn.sunline.ltts.busi.dp.type.DpDayEndType.KtpAdinType dataItem, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitx.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitx.Property property) {
		bizlog.debug("开始调整账户["+dataItem.getAcctno()+"]的税率");
		bizlog.debug("dataItem===================="+dataItem);
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String timetm = DateTools2.getCurrentTimestamp();
		String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//		CommTools.getBaseRunEnvs().setBusi_org_id(dataItem.getCorpno());
		
		E_DPACST acctst = null;
		BigDecimal onlnbl = BigDecimal.ZERO;
		
		if(dataItem.getPddpfg() == E_FCFLAG.CURRENT){
			KnaAcct acct = KnaAcctDao.selectOne_odb1(dataItem.getAcctno(), true);
			acctst = acct.getAcctst();
			onlnbl = DpAcctProc.getAcctBalance(acct);
		}else if(dataItem.getPddpfg() == E_FCFLAG.FIX){
			KnaFxac fxac = KnaFxacDao.selectOne_odb1(dataItem.getAcctno(), true);
			acctst = fxac.getAcctst();
			onlnbl = fxac.getOnlnbl();
		}else{
			
		}
		
		if(acctst != E_DPACST.NORMAL){ //非正常账户排除
		    bizlog.debug("非正常账户排除");
			return;
		}
	 String qrdate =DateTimeUtil.dateAdd("dd", trandt, -1);//取前一天的日期
		
		
		BigDecimal taxrat = SysUtil.getInstance(IoPbInRaSelSvc.class).getNearTxTate(dataItem.getIntxcd(), qrdate).getTaxrat();//税率
/*		BigDecimal taxrat = BigDecimal.ZERO;
		IntrTaxList intrTax = mapTax.get(dataItem.getIntxcd());
		if(CommUtil.isNotNull(intrTax)){
			taxrat = intrTax.getTaxrat().divide(new BigDecimal(intrTax.getTxunit()), 7, BigDecimal.ROUND_HALF_UP);
		}*/
		
		//1、计算账户积数
		BigDecimal curram = dataItem.getCutmam();
		//BigDecimal onlnbl = DpPublic.getOnlnblByAcctno(dataItem.getAcctno());
		
		bizlog.debug("当前日期：["+trandt+"]", "");
		String lsamdt = dataItem.getLaamdt();
		bizlog.debug("当前日期：["+lsamdt+"]", "");
		BigDecimal cut = DpPublic.calRealTotalAmt(curram, onlnbl, trandt, lsamdt);
		BigDecimal rlintr = BigDecimal.ZERO;// 利息
		
		KubInrt inrt = KubInrtDao.selectOne_odb1(dataItem.getAcctno(), true);
		
		// 利息税发生额  计算值并不准确，此处不做计算 20170223 chenlk
		BigDecimal taxCut =  BigDecimal.ZERO;
/*		if(!CommUtil.equals(BigDecimal.ZERO, cut)){
			IoPbStaPublic pbpub = SysUtil.getInstance(IoPbStaPublic.class);
			rlintr = pbpub.IntrPublic_calInstByCutmam(inrt.getCuusin(), cut);
			taxCut = rlintr.multiply(taxrat).divide(BigDecimal.valueOf(100));
		}*/
		
		//2、记录到负债账户利息明细表
		KnbIndl indl = SysUtil.getInstance(KnbIndl.class);
		
		indl.setGradin(BigDecimal.ZERO);//档次计息余额
		indl.setTotlin(BigDecimal.ZERO);//总计息余额
		indl.setRlintr(rlintr);//实际利息发生额
		indl.setCatxrt(taxrat);//计提税率
		indl.setRlintx(taxCut);//实际利息税发生额
		
		indl.setAcbsin(inrt.getBsintr());//基准利率
		indl.setAcctno(dataItem.getAcctno());//负债账号
		indl.setAcmltn(cut);//积数
		indl.setCuusin(inrt.getCuusin());//当前执行利率
		indl.setDetlsq(getDetlsq(dataItem.getAcctno(),dataItem.getIntrtp()));//明细序号
		indl.setIncdtp(inrt.getIncdtp());//利率代码类型
		indl.setIndlst(E_INDLST.YOUX);//负债利息明细状态
		indl.setIndxno(getIndexNo(dataItem.getAcctno(),dataItem.getIntrtp()));
		indl.setIneddt(trandt);//计息终止日期
		indl.setInstdt(dataItem.getInstdt());//计息开始日期
		indl.setIntrcd(dataItem.getIntrcd());//利率编号
		indl.setIntrdt(trandt);//计息日期
		indl.setIntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//计息流水
		indl.setIntrwy(inrt.getIntrwy());//利率靠档方式
		indl.setLsinoc(E_INDLTP.UPIR);//上次利息操作代码
		indl.setLvamot(inrt.getLvamot());
		indl.setLvindt(inrt.getLvindt());
		indl.setLyinwy(inrt.getLyinwy());
		indl.setTxbebs(dataItem.getTxbebs());
		indl.setIntrtp(inrt.getIntrtp());
		try {
			KnbIndlDao.insert(indl);
		} catch (Exception e) {
			DpModuleError.DpstAcct.BNAS1735();
		}
		//修改负债账户计息明细
		DpDayEndDao.upAcinForSubByAcctno(dataItem.getAcctno(), trandt, trandt, BigDecimal.ZERO , E_INDLTP.UPIR, trandt,timetm);
//		CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);
	}
	
	//循序号 和 明细序号
	private Long getDetlsq(String acctno, E_INTRTP intrtp) {
		//查询该账户信息是否存在该表中
		long count = 0;
		try {
			count = DpDayEndDao.getAcctnoIsInIndl(acctno, intrtp, true);
		} catch (Exception e) {
			return new Long(1);
		} 
		return count+1;
		
	}
	//明细序号 暂时都为1
	private Long getIndexNo(String acctno, E_INTRTP intrtp) {
		
		return new Long(1);
	}
	
	/**
	 * 获取数据遍历器。
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<cn.sunline.ltts.busi.dp.type.DpDayEndType.KtpAdinType> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitx.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitx.Property property) {
		//String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
//			
//			List<IntrTaxList> taxList = DpDayEndDao.selKupIntxByTrandt(trandt, false);
//			
//			return new ListBatchDataWalker<IntrTaxList>(taxList);
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		Params params = new Params();
		//params.add("intxcd", dataItem.getIntxcd());
		params.add("currdt", trandt);
		params.add("corpno",CommTools.getBaseRunEnvs().getBusi_org_id());
		
		
		return new CursorBatchDataWalker<KtpAdinType>(DpDayEndDao.namedsql_selKupIntxTranData, params);
	}

	
	/**
	 * 交易前处理，获取当日生效的税率信息，并登记在全局变量中
	 */
	@Override
	public void beforeTranProcess(String taskId, Input input, Property property) {
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		
		mapTax = new HashMap<String, IntrTaxList>();
		
		List<IntrTaxList> taxList = DpDayEndDao.selKupIntxByTrandt(trandt, false);
		for(IntrTaxList tax : taxList){
			mapTax.put(tax.getIntxcd(), tax);
		}
		
		if(taxList.size() == 0){
			isrun = false;
		}
		
	}
		
}


