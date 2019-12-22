package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccsDao;
import cn.sunline.ltts.busi.dp.base.DpPublicServ;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.domain.DpSaveEntity;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProd;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.AuacinTranData;
import cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Auacin.Input;
import cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Auacin.Property;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaStaPublic;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_TROTTP;

/**
 * 电子账户转智能储蓄 满足指定金额，则转换为定期
 * 
 */

public class auacinDataProcessor
		extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Auacin.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Auacin.Property, cn.sunline.ltts.busi.dp.type.DpDayEndType.AuacinTranData> {

	private static final BizLog bizlog = BizLogUtil
			.getBizLog(auacinDataProcessor.class);
	private static final String Parm_code = "DpParm.auacct";// 智能储蓄参数名
//	private static final String auacpd = getAuacpd();// 智能储蓄产品号
	private String trandt = DateTools2.getDateInfo().getSystdt();

	/**
	 * 获取智能储蓄产品号
	 * 
	 * @return
	 */
//	private static String getAuacpd() {
//		return KnpParameterDao.selectOne_odb1(Parm_code, "prodcd", "%", "%", false)
//				.getParm_value1();
//	}

	
	/**
	 * 获取自动转存起存金额
	 * 
	 * @return
	 */
	private static BigDecimal getSrdpam() {
		return BigDecimal
				.valueOf((Double.parseDouble(KnpParameterDao.selectOne_odb1(Parm_code,
						"auacin", "%", "%", false).getParm_value1())));
	}

	// 暂停标志
	private static String getStopfg() {
		return KnpParameterDao.selectOne_odb1(Parm_code, "auacin", "%", "%", false)
				.getParm_value2();
	}
	
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
			cn.sunline.ltts.busi.dp.type.DpDayEndType.AuacinTranData dataItem,
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Auacin.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Auacin.Property property) {
	    

		// 智能在这里进行上日张记账日期的设置
//		CommTools.getBaseRunEnvs().setServtp(ApUtil.DP_DAYEND_CHANNEL);// 日终渠道
		CommTools.getBaseRunEnvs().setChannel_id("NK"); //日终渠道   暂时本系统作为日终渠道
		String acctdt = DateTools2.getDateInfo().getLastdt(); // 记上日账
		
		//***************
		//acctdt = DateTools2.dateAdd (-1, trandt); 
		//***************
		
		CommTools.getBaseRunEnvs().setTrxn_date(acctdt);
//		CommTools.getBaseRunEnvs().setJiaoyirq(acctdt);
		
		BigDecimal amt = calcAmt(dataItem); //计算转存金额
		String curr_date = dataItem.getNextdt(); //下个处理日
		//String next_date = DateTools2.calDateByFreq(curr_date, dataItem.getFrequy()); //下个处理日
		//String next_date = DateTools2.calDateByTerm(trandt, dataItem.getFrequy()+ dataItem.getPeriod());
		String next_date = DateTools2.calDateByFreq(trandt, dataItem.getFrequy());
		bizlog.debug("交易日期：[" + CommTools.getBaseRunEnvs().getTrxn_date() + "]");
		bizlog.debug("上个记账日期：[" + acctdt + "]");
		bizlog.debug("账号[" + dataItem.getAcctno() + "]转入智能储蓄开始，金额["+ amt + "]");
		bizlog.debug("[" + dataItem.getAcctna() + "]");
		bizlog.debug("[" + dataItem.getAcseno() + "]");
		bizlog.debug("[" + dataItem.getCardno() + "]");
		bizlog.debug("[" + dataItem.getCustac() + "]");
		bizlog.debug("[" + dataItem.getCrcycd() + "]");
		bizlog.debug("[" + amt + "]");
		bizlog.debug("[" + dataItem.getSrdpam() + "]");
		bizlog.debug("[" + dataItem.getAuacno() + "]");
		bizlog.debug("[" + dataItem.getAusbac() + "]");

		// 每一笔交易重新生成一笔流水，用来进行平衡性检查
		
		//==============mdy by zhanga==============
		//签约即开立负债子账户，因此不需要在此处开户
		/** 
		try {
			// 如果没有智能储蓄定期存款，则开智能储蓄
			if (CommUtil.isNull(dataItem.getAuacno())) {
				bizlog.debug("开智能储蓄");
				CucaInfo cucaInfo = DpDayEndDao.selCucaInfo(
						dataItem.getCustac(), true);
				DpOpenAcctEntity entity = SysUtil
						.getInstance(DpOpenAcctEntity.class);
				entity.setAcctna(dataItem.getAcctna());
				entity.setAcctno(dataItem.getAcctno());
				entity.setCacttp(cucaInfo.getCacttp());
				entity.setCrcycd(dataItem.getCrcycd());
				entity.setCustac(dataItem.getCustac());
				entity.setCustno(dataItem.getCustno());
				entity.setCusttp(E_CUSTTP.PERSON);// TODO 客户类型
				entity.setDepttm(E_TERMCD.T305);
				entity.setOpacfg(E_YES___.NO);
				entity.setProdcd(auacpd);
				DpPublicServ.openAcct(entity);
				bizlog.debug("新开智能储蓄负债账号[" + entity.getAcctno() + "]");

				// 关联电子账号
				IoCaSrvGenEAccountInfo caService = SysUtil
						.getInstance(IoCaSrvGenEAccountInfo.class);
				IoCaAddEARelaIn connectEntity = SysUtil
						.getInstance(IoCaAddEARelaIn.class);
				connectEntity.setAcctno(entity.getAcctno());
				connectEntity.setCrcycd(entity.getCrcycd());
				connectEntity.setCustac(dataItem.getCustac());
				connectEntity.setFcflag(E_FCFLAG.FIX);
				connectEntity.setProdcd(entity.getProdcd());
				connectEntity.setProdtp(E_PRODTP.DEPO);
				caService.prcAddEARela(connectEntity);

				// 客户后需要设置智能储蓄定期的账号
				dataItem.setAuacno(entity.getAcctno());
			}
			**/
		try{
			//检查账户状态
			CapitalTransCheck.ChkAcctstOT(dataItem.getCustac());
			
			//调用查询电子账户状态字服务
			IoDpFrozSvcType ioDpFrozSvcType = SysUtil.getInstance(IoDpFrozSvcType.class);
			IoDpAcStatusWord  cplAcStatusWord = ioDpFrozSvcType.getAcStatusWord(dataItem.getCustac());

			if(E_YES___.YES == cplAcStatusWord.getDbfroz()){
				throw DpModuleError.DpstComm.E9999("您的账户状态字为双冻，无法操作！");

			}
					
			if(E_YES___.YES == cplAcStatusWord.getBrfroz()){
				throw DpModuleError.DpstComm.E9999("您的账户状态字为借冻，无法操作！");

			}
					
			if(E_YES___.YES == cplAcStatusWord.getBkalsp()){
				throw DpModuleError.DpstComm.E9999("您的电子账户状态字已全止(银行止付)，无法操作");

			}
					
			if(E_YES___.YES == cplAcStatusWord.getClstop()){
				throw DpModuleError.DpstComm.E9999("您的电子账户状态字已账户保护，无法操作");

			}
					
			if(E_YES___.YES == cplAcStatusWord.getOtalsp()){
				throw DpModuleError.DpstComm.E9999("您的电子账户状态字已全止(外部止付)，无法操作");
			}
		}catch(Exception e){
			bizlog.debug(e.getLocalizedMessage());
			return;
		}
		
		if(CommUtil.compare(amt, BigDecimal.ZERO) > 0){
			 MsSystemSeq.getTrxnSeq();												
			
//			try {
				//备注要求是 产品名称
				String remark = "";
				String remark1 = "";
//				KnaAcctProd tblacprod = KnaAcctProdDao.selectOne_odb1(dataItem.getAuacno(), false);
//				KnaFxacProd tblfxprod = KnaFxacProdDao.selectOne_odb1(dataItem.getAuacno(), false);
				
				//获取产品可售产品名称
				KnaAcctProd tblacprod = DpAcctDao.selKnaAcctProdByAcctno(dataItem.getAuacno(),false);
				KnaFxacProd tblfxprod = DpAcctDao.selKnaFxacProdByAcctno(dataItem.getAuacno(),false);
				if(CommUtil.isNotNull(tblacprod)){
					remark = "活期-"+tblacprod.getObgaon();
					remark1 = tblacprod.getObgaon();
				}
				if(CommUtil.isNotNull(tblfxprod)){
					remark = "定期-"+tblfxprod.getObgaon();
					remark1 = tblfxprod.getObgaon();
				}
				
				// 活期支取
				DpSaveEntity input_draw = SysUtil.getInstance(DpSaveEntity.class);
				input_draw.setAcctno(dataItem.getAcctno());
				input_draw.setAcseno(dataItem.getAcseno());
				input_draw.setCardno(dataItem.getCardno());
				input_draw.setCrcycd(dataItem.getCrcycd());
				input_draw.setCustac(dataItem.getCustac());
				input_draw.setOpacna(dataItem.getAcctna());
				input_draw.setToacct(dataItem.getAuacno());
				input_draw.setSmrycd(BusinessConstants.SUMMARY_TZ);
				input_draw.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_TZ));
				input_draw.setRemark(remark);
				
				input_draw.setTranam(amt);
				input_draw.setAuacfg(E_YES___.NO);// 不是普通的智能储蓄存取
				input_draw.setNgblfg(E_YES___.YES);// 允许透支
				//input_draw.setOpbrch(dataItem.get);
				bizlog.debug("[" + dataItem.getAcctno() + "]智能储蓄活期支取");
				DpPublicServ.drawAcctDp(input_draw);
				bizlog.debug("智能储蓄活期支取完成");
	
				// 智能储蓄存入
				DpSaveEntity input_save = SysUtil.getInstance(DpSaveEntity.class);
				input_save.setAcctno(dataItem.getAuacno());
				input_save.setAcseno(dataItem.getAusbac());
				input_save.setCardno(dataItem.getCardno());
				input_save.setCrcycd(dataItem.getCrcycd());
				input_save.setCustac(dataItem.getCustac());
				input_save.setOpacna(dataItem.getAcctna());
				input_save.setToacct(dataItem.getAcctno());
				input_save.setSmrycd(BusinessConstants.SUMMARY_ZR);
				input_save.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_ZR));
				input_save.setRemark("结算主账户-"+remark1);
				
				input_save.setTranam(amt);
				input_save.setAuacfg(E_YES___.NO);// 不是普通的智能储蓄存取
				bizlog.debug("[" + dataItem.getAuacno() + "]智能储蓄定期存入");
				DpPublicServ.postAcctDp(input_save);
				bizlog.debug("智能储蓄定期存入完成");
//			} catch (Exception e) {
//				throw ApError.BusiAplt.E0000("账号[" + dataItem.getAcctno() + "]转入智能储蓄失败。",e);
//				//bizlog.debug("账号[" + dataItem.getAcctno() + "]转入智能储蓄失败" + e.getLocalizedMessage());
//			}
//			
			
			// 检查平衡
			String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
			String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
			
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(trandt, transq, null); //E_CLACTP.
		}
		
		//修改下次日期
		SysUtil.getInstance(IoCaStaPublic.class).CaDao_updKnaSignDetlBySignno(curr_date,
				next_date, dataItem.getSignno(), E_SIGNST.QY, E_SIGNTP.ZNCXL);
		

		bizlog.debug("账号[" + dataItem.getAcctno() + "]转入智能储蓄结束");
	}

	
	private static BigDecimal calcAmt(AuacinTranData dataItem){

		BigDecimal miniam = dataItem.getMiniam(); //转入最小金额
		BigDecimal keepam = dataItem.getKeepam(); //转出保留最低余额
		BigDecimal trmiam = dataItem.getTrmiam(); //转出最小金额
		BigDecimal upamnt = dataItem.getUpamnt(); //递增金额
		BigDecimal signam = dataItem.getSignam(); //签约金额
		E_TROTTP trottp = dataItem.getTrottp(); //转出类型
		
		BigDecimal onlnbl = dataItem.getOnlnbl(); //结算账户余额
		BigDecimal minbal = onlnbl.subtract(keepam); //可被转存的余额
		BigDecimal amt = trmiam;
		
		if(trottp == E_TROTTP.CZQE){
			amt = onlnbl;
		}else if(trottp == E_TROTTP.DBJE){
			if(CommUtil.compare(signam, onlnbl) > 0){ //签约金额大于账户余额
				amt = BigDecimal.ZERO;
			}else{
				amt = signam;
			}
		}else{
			//只有账户余额大于转出最小金额，且大于转入最小金额，且大于保留余额才能进行转出
			if(CommUtil.compare(minbal, trmiam) >= 0 && CommUtil.compare(minbal, miniam) >= 0){
			
				if(CommUtil.compare(upamnt, BigDecimal.ZERO) == 0){
					amt = minbal;
				}else{
					BigDecimal tmp = minbal.subtract(trmiam).divide(upamnt, 0, BigDecimal.ROUND_DOWN);
					amt = amt.add(upamnt.multiply(tmp));
				}
				
				if(CommUtil.compare(amt, miniam) < 0){
					amt = BigDecimal.ZERO;
				}
			}else{
				amt = BigDecimal.ZERO;
			}
			
			/**
			 * add by xj 20180925 柳行定活宝：当定活宝账户余额为0时，需活期余额需大于起存金额1w元
			 */
			//获取定活宝产品号
			KnpParameter dhbPara = KnpParameterDao.selectOne_odb1("DpParm.dppb", "dppb_dhb", "%", "%", false);
			//获取转入账号产品号
			KnaAccs tblAccs = KnaAccsDao.selectOne_odb2(dataItem.getAuacno(), false);
			//定活产品做账户余额处理
			if(CommUtil.isNotNull(dhbPara.getParm_value1()) && CommUtil.isNotNull(tblAccs) && CommUtil.compare(dhbPara.getParm_value1(),tblAccs.getProdcd())==0){
				//查询定活宝账户余额
				KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(dataItem.getAuacno(), false);
				//当定活宝账户余额为0时，需满足起存金额限制
				if(CommUtil.compare(BigDecimal.ZERO, tblKnaFxac.getOnlnbl())==0){
					//定活宝产品起存金额 控制
					BigDecimal dponbl = new BigDecimal(dhbPara.getParm_value2());
					if(CommUtil.compare(onlnbl, dponbl)<0){
						amt = BigDecimal.ZERO;
					}
				}
			}
			/**end*/
		}
		return amt;
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
	public BatchDataWalker<cn.sunline.ltts.busi.dp.type.DpDayEndType.AuacinTranData> getBatchDataWalker(
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Auacin.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Auacin.Property property) {
		
		if (CommUtil.equals(getStopfg(), "STOP")) {
			throw ApError.BusiAplt.E0000("智能储蓄活转定已暂停。");
		}
		
		Params params = new Params();
		params.add("trandt", trandt); //交易日期
		params.add("signst", E_SIGNST.QY); //签约状态 签约
		params.add("signtp", E_SIGNTP.ZNCXL); //签约类型 智能储蓄

		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.type.DpDayEndType.AuacinTranData>(
				DpDayEndDao.namedsql_selAuacinTranDatas, params);
	}

	@Override
	public void beforeTranProcess(String taskId, Input input, Property property) {
	    
//		bizlog.debug("智能储蓄产品号[" + auacpd + "]");
		bizlog.debug("交易机构[" + CommTools.getBaseRunEnvs().getTrxn_branch() + "]");

		
	
		
//		trandt = "20160302";
//		CommTools.getBaseRunEnvs().setTrxn_date(trandt);
//		
//		CommTools.getBaseRunEnvs().setLstrdt(DateTools2.dateAdd (-1, trandt));
//		
//		DateTools2.getDateInfo().setLastdt(DateTools2.dateAdd (-1, trandt));
//		
		
		//以上是测试用
		
		// DpDayEndDao.delKtpAuac();
		// DpDayEndDao.insKtpAuac(trandt, getSrdpam(), auacpd);
		//
		//
		// //统计冻结余额
		// DpDayEndDao.delKtpFrozBala();
		// DpDayEndDao.insKtpFrozBala(E_FROZST.VALID);
		//
		// //减掉冻结余额
		// DpDayEndDao.updKtpAuac();
	}
	@Override
	public void jobExceptionProcess(String taskId, Input input,
			Property property, String jobId, AuacinTranData dataItem,
			Throwable t) {
		//super.jobExceptionProcess(taskId, input, property, jobId, dataItem, t);
	}
}
