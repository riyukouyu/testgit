package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlLedgerSum;
import cn.sunline.ltts.busi.in.tables.In.GlLedgerSumDao;
import cn.sunline.ltts.busi.in.type.InDayEndTypes;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 总分核对供数
 * 
 */

public class glfendDataProcessor extends BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.intf.Glfend.Input, cn.sunline.ltts.busi.intran.batchtran.intf.Glfend.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(glfendDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.ltts.busi.intran.batchtran.intf.Glfend.Input input, cn.sunline.ltts.busi.intran.batchtran.intf.Glfend.Property property) {
		bizlog.debug("---------开始进行计总分核对供数-------");
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		// 日期为日切前日期
		String trandt = DateTools2.getDateInfo().getSystdt();
		// 上上日日期
		
		String lstdt = DateTools2.getDateInfo().getLastdt();
//		String lstlstdt = DateTools2.getDateInfo().getBflsdt();
		InDayEndSqlsDao.delGlLedgerByDay(lstdt,corpno);
		// 删除当日总分核对汇总数据
		/*InDayEndSqlsDao.delglfabatchByDay(lstdt);
		
		// 删除当日余额发生额汇总
		InDayEndSqlsDao.delknbfatlAcsq(lstdt);

		// 生成数据批次号
		String taskid = lstdt + "0001";

		// 将活期、定期总分核对汇总数据插入文件读写表knb_glfa_batch
		 InDayEndSqlsDao.insglfabatchByAcct(trandt,lstdt, taskid);
		
		// 将内部户总分核对汇总数据插入文件读写表knb_glfa_batch 扎差、共同的余额以借方为正处理
		 InDayEndSqlsDao.insglfabatchByGlAcct(lstdt, taskid);
		 */

		//将活期、定期生成总分核对数据 插入汇总表gl_ledger_sum
		InDayEndSqlsDao.insglledgerByDepositAcct(trandt, lstdt, corpno);

		//贷款生成总分核对数据 插入汇总表gl_ledger_sum
		insglledgerByDepositLoanAcct(lstdt, corpno);
		
		//内部户生成总分核对数据 插入汇总表gl_ledger_sum
		InDayEndSqlsDao.insglledgerByGlAcct(lstdt, corpno);

		// 获取上日发生额汇总明细
		// InDayEndSqlsDao.insknbFatlAcsq(lstdt); //借方正数、贷方负数 knb_fatl_acsq

		// 获取上日余额汇总数据
		/*List<Iofatlacsq>lstfatlacsq = InDayEndSqlsDao.selknbGlfaBatch(lstdt, false);
		 if(CommUtil.isNotNull(lstfatlacsq)){

			 for( IoDpTable.Iofatlacsq cain:lstfatlacsq) {		
				 //上日余额
				 BigDecimal lastbl = cain.getTranam();  //余额汇总金额  借方正数 贷方负数
				 				 
				 //上上日余额汇总 knb_glfa_batch 
				 BigDecimal lsttranam = BigDecimal.ZERO;
				 KnbGlfaBatch  glfa = InDayEndSqlsDao.selglfaInfo(lstlstdt, cain.getBrchno(), cain.getProdcd(), cain.getCrcycd(), false);
				 if(CommUtil.isNotNull(glfa)){
					 lsttranam = glfa.getTranam();
				 }
				 //上日余额发生额
				 BigDecimal changetranam = InDayEndSqlsDao.selglacsqInfo(lstdt, cain.getBrchno(), cain.getProdcd(), cain.getCrcycd(), false);
				 if(CommUtil.isNull(changetranam)){
					 changetranam = BigDecimal.ZERO;
				 }
				 BigDecimal lstsum =BigDecimal.ZERO;//上上日余额+上日发生
				 BigDecimal tranam = BigDecimal.ZERO;//上日发生
				 if(CommUtil.equals(cain.getDirection(), "DR")){
					 tranam = changetranam; 
				 }else{
					 tranam =  changetranam.negate();
				 }
				 lstsum  = lsttranam.add(tranam);
				 if(CommUtil.compare(lstsum, lastbl)!=0){
					 throw ApError.BusiAplt.E0000("行所："+cain.getBrchno()+" 日期："+lstdt+" 产品："+cain.getProdcd()+" 币种："+cain.getCrcycd()+" 当日余额"+lastbl+" 不等于上日余额"+lsttranam+"+加上当日发生额"+tranam+"的和"+lstsum+"！");
				 }
				 
			 }
			 
			 //将计提利息汇总数据插入总分核对数据中
			 InDayEndSqlsDao.InscbtlToglfaData(lstdt, taskid);
			 
			 //插入当日费用账务发生
			 InDayEndSqlsDao.InsChrgToglfaData(lstdt, taskid);
			 
			//删除30天前数据
			 String backupdt = DateTimeUtil.dateAdd("day", lstdt, -30);
			 InDayEndSqlsDao.delglfabatchByDay(backupdt);
		}*/
	}

	/**
	 * 汇总贷款各余额金额  插入总分核对汇总数据表
	 * @param acctdt
	 */
	private void insglledgerByDepositLoanAcct(String acctdt, String corpno) {
		// 删除当日贷款汇总临时数据
		//GltCbglLoanDao.delete_odb2(acctdt);

		// 汇总贷款各余额金额
		List<InDayEndTypes.CbglLoanTranData> loanSumDatas = InDayEndSqlsDao.selLoanSum(acctdt, corpno, false);
		for (InDayEndTypes.CbglLoanTranData loanData : loanSumDatas) {
//			GltCbglLoan cbglLoan = SysUtil.getInstance(GltCbglLoan.class);
			GlLedgerSum ledger = SysUtil.getInstance(GlLedgerSum.class);

//			cbglLoan.setAcctdt(acctdt);
//			cbglLoan.setBrchno(loanData.getBrchno());
//			cbglLoan.setCrcycd(loanData.getCrcycd());
//			cbglLoan.setDtitcd(loanData.getDtitcd());
			ledger.setCorpno(corpno);
			ledger.setTrxn_date(acctdt);
			ledger.setAcct_branch(loanData.getBrchno());
			ledger.setCcy_code(loanData.getCrcycd());
			ledger.setAccounting_alias(loanData.getDtitcd());
			ledger.setAccounting_subject(E_ATOWTP.LN);
			ledger.setBal_type(E_DEBITCREDIT.DEBIT);
			

			// 赋值不同的余额组成
			// 1.正常本金
//			cbglLoan.setDetlbl(loanData.getLnnpbl());
//			cbglLoan.setLnactp(E_LNACTP.L1);
//			GltCbglLoanDao.insert(cbglLoan);
			ledger.setBal_attributes(E_BLTYPE.BALANCE.getValue());
			ledger.setAcct_bal(loanData.getLnnpbl());
			GlLedgerSumDao.insert(ledger);

			// 2.逾期本金
//			cbglLoan.setDetlbl(loanData.getLnopbl());
//			cbglLoan.setLnactp(E_LNACTP.L2);
//			GltCbglLoanDao.insert(cbglLoan);
			ledger.setBal_attributes(E_BLTYPE.L1.getValue());
			ledger.setAcct_bal(loanData.getLnopbl());
			GlLedgerSumDao.insert(ledger);

			// 3.呆滞本金
//			cbglLoan.setDetlbl(loanData.getLndpbl());
//			cbglLoan.setLnactp(E_LNACTP.L3);
//			GltCbglLoanDao.insert(cbglLoan);
			ledger.setBal_attributes(E_BLTYPE.L2.getValue());
			ledger.setAcct_bal(loanData.getLndpbl());
			GlLedgerSumDao.insert(ledger);

			// 4.呆账本金
//			cbglLoan.setDetlbl(loanData.getLnbpbl());
//			cbglLoan.setLnactp(E_LNACTP.L4);
//			GltCbglLoanDao.insert(cbglLoan);
			ledger.setBal_attributes(E_BLTYPE.L3.getValue());
			ledger.setAcct_bal(loanData.getLnbpbl());
			GlLedgerSumDao.insert(ledger);

			/*
			// 5.应收应计利息
//			cbglLoan.setDetlbl(loanData.getYsyjni().add(loanData.getYsyjpi()).add(loanData.getYjxxti()));
//			cbglLoan.setLnactp(E_LNACTP.LB);
//			GltCbglLoanDao.insert(cbglLoan);
			ledger.setAcct_bal(loanData.getYsyjni().add(loanData.getYsyjpi()).add(loanData.getYjxxti()));
			ledger.setBal_attributes(E_BLTYPE.L4.getValue());// 类型不对应 ?
			GlLedgerSumDao.insert(ledger);
			
			// 6.催收应计利息
			// cbglLoan.setDetlbl(loanData.getCsyjni());
			// cbglLoan.setLnactp(E_LNACTP.LB);
			// GltCbglLoanDao.insert(cbglLoan);

			// 7.应收欠款
//			cbglLoan.setDetlbl(loanData.getYsxxoi().add(loanData.getYsxxpi()).add(loanData.getYsxxti()));
//			cbglLoan.setLnactp(E_LNACTP.L5);
//			GltCbglLoanDao.insert(cbglLoan);
			ledger.setBal_attributes(E_BLTYPE.L5.getValue());//类型不对应 ?
			ledger.setAcct_bal(loanData.getYsxxoi().add(loanData.getYsxxpi()).add(loanData.getYsxxti()));
			GlLedgerSumDao.insert(ledger);

			// 8.催收欠息+催收罚息+催收应计利息+催收应计罚息+应计复息+复息
//			cbglLoan.setDetlbl(loanData.getCsxxoi().add(loanData.getCsxxpi()).add(loanData.getCsyjni()).add(loanData.getCsyjpi()));
//			cbglLoan.setLnactp(E_LNACTP.L6);
//			GltCbglLoanDao.insert(cbglLoan);
			ledger.setBal_attributes(E_BLTYPE.L4.getValue());
			ledger.setAcct_bal(loanData.getCsxxoi().add(loanData.getCsxxpi()).add(loanData.getCsyjni()).add(loanData.getCsyjpi()));
			GlLedgerSumDao.insert(ledger);

			// 9.应收应计罚息
			// cbglLoan.setDetlbl(loanData.getYsyjpi());
			// cbglLoan.setLnactp(E_LNACTP.LB);
			// GltCbglLoanDao.insert(cbglLoan);

			// 10.催收应计罚息
			// cbglLoan.setDetlbl(loanData.getCsyjpi());
			// cbglLoan.setLnactp(E_LNACTP.LB);
			// GltCbglLoanDao.insert(cbglLoan);

			// 11.应收罚息
			// cbglLoan.setDetlbl(loanData.getYsxxpi());
			// cbglLoan.setLnactp(E_LNACTP.L5);
			// GltCbglLoanDao.insert(cbglLoan);

			// 12.催收罚息
			// cbglLoan.setDetlbl(loanData.getCsxxpi());
			// cbglLoan.setLnactp(E_LNACTP.L6);
			// GltCbglLoanDao.insert(cbglLoan);

			// 13.应计复息
			// cbglLoan.setDetlbl(loanData.getYjxxci());
			// cbglLoan.setLnactp(E_LNACTP.LB);
			// GltCbglLoanDao.insert(cbglLoan);

			// 14.复息
			// cbglLoan.setDetlbl(loanData.getCixxxx());
			// cbglLoan.setLnactp(E_LNACTP.L5);
			// GltCbglLoanDao.insert(cbglLoan);

			// 15.应计贴息
			// cbglLoan.setDetlbl(loanData.getYjxxti());
			// cbglLoan.setLnactp(E_LNACTP.LB);
			// GltCbglLoanDao.insert(cbglLoan);

			// 16.应收贴息
			// cbglLoan.setDetlbl(loanData.getYsxxti());
			// cbglLoan.setLnactp(E_LNACTP.L5);
			// GltCbglLoanDao.insert(cbglLoan);

			// 18.核销本金
//			cbglLoan.setDetlbl(loanData.getHxxxpr());
//			cbglLoan.setLnactp(E_LNACTP.LG);
//			GltCbglLoanDao.insert(cbglLoan);
			ledger.setAcct_bal(loanData.getHxxxpr());
			ledger.setBal_attributes(E_BLTYPE.L6.getValue());
			GlLedgerSumDao.insert(ledger);
			
			// 19.核销利息
//			cbglLoan.setDetlbl(loanData.getHxxxin());
//			cbglLoan.setLnactp(E_LNACTP.LH);
//			GltCbglLoanDao.insert(cbglLoan);
			ledger.setAcct_bal(loanData.getHxxxin());
			ledger.setBal_attributes(E_BLTYPE.L7.getValue());
			GlLedgerSumDao.insert(ledger);

			// 20.利息收入
			//cbglLoan.setDetlbl(loanData.getSrxxin());
			//cbglLoan.setLnactp(E_LNACTP.L7); GltCbglLoanDao.insert(cbglLoan);

			// 21.准备金
//			cbglLoan.setDetlbl(loanData.getResvam());
//			cbglLoan.setLnactp(E_LNACTP.L9);
//			GltCbglLoanDao.insert(cbglLoan);
			ledger.setAcct_bal(loanData.getHxxxin());
			ledger.setBal_attributes(E_BLTYPE.L9.getValue());
			GlLedgerSumDao.insert(ledger);
			*/
		}

	}

}
