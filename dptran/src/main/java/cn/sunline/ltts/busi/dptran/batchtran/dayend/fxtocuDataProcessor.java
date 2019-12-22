package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.acct.DpCloseAcctno;
import cn.sunline.ltts.busi.dp.base.DpPublicServ;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.domain.DpSaveEntity;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProdDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.AuacinTranData;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseIN;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;


/**
 * 智能储蓄转电子账户 智能储蓄转电子账户 1、查询活期负债账户中余额小于0的所有数据 2、定期支取 3、活期入账
 * 
 * @author Guanglin
 * 
 */

public class fxtocuDataProcessor
		extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.intf.Fxtocu.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Fxtocu.Property, cn.sunline.ltts.busi.dp.type.DpDayEndType.AuacinTranData> {
	private static final BizLog bizlog = BizLogUtil
			.getBizLog(auacinDataProcessor.class);

	private static final String Parm_code = "DpParm.auacct";// 智能储蓄参数名

	/**
	 * 获取智能储蓄产品号
	 * 
	 * @return
	 */
	private static String getAuacpd() {
		return KnpParameterDao.selectOne_odb1(Parm_code, "prodcd", "%", "%", false)
				.getParm_value1();
	}

	// 暂停标志
	private static String getStopfg() {
		return KnpParameterDao.selectOne_odb1(Parm_code, "fxtocu", "%", "%", false)
				.getParm_value2();
	}

	private String trandt = DateTools2.getDateInfo().getSystdt();

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
			cn.sunline.ltts.busi.dp.type.DpDayEndType.AuacinTranData dataItem,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Fxtocu.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Fxtocu.Property property) {
//		CommTools.getBaseRunEnvs().setServtp(ApUtil.DP_DAYEND_CHANNEL);// 日终渠道
		
		
//		String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		
//		CommTools.getBaseRunEnvs().setBusi_org_id(dataItem.getCorpno());
		
		
		CommTools.getBaseRunEnvs().setChannel_id("NK"); //日终渠道    暂时本系统最为日终渠道
		String acctdt = DateTools2.getDateInfo().getLastdt(); // 记上日账
		CommTools.getBaseRunEnvs().setTrxn_date(acctdt);
//		CommTools.getBaseRunEnvs().setJiaoyirq(acctdt);

		bizlog.debug("交易日期：[" + CommTools.getBaseRunEnvs().getTrxn_date() + "]");
		
		bizlog.debug("账户名称：[" + dataItem.getAcctna() + "]");
		bizlog.debug("子账户号：[" + dataItem.getAcseno() + "]");
		bizlog.debug("电子账号：[" + dataItem.getCardno() + "]");
		bizlog.debug("账号ID：[" + dataItem.getCustac() + "]");
		bizlog.debug("币种：[" + dataItem.getCrcycd() + "]");
		bizlog.debug("原始金额：[" + dataItem.getOnlnbl() + "]");
		bizlog.debug("存款账号：[" + dataItem.getAuacno() + "]");
		bizlog.debug("结算账号：[" + dataItem.getAcctno() + "]");
		

		
		BigDecimal upAmt = CommUtil.nvl(dataItem.getOtupam(),BigDecimal.ZERO); //转出递增金额
		BigDecimal miAmt = CommUtil.nvl(dataItem.getOtmiam(),BigDecimal.ZERO); //转出最小金额
		
		BigDecimal tranam = dataItem.getOnlnbl(); //交易金额
		BigDecimal acctbl = BigDecimal.ZERO;
		
		IoCaKnaAccs accs = DpAcctQryDao.selKnaAccsByAcctno(dataItem.getAuacno(), false);
		
		if (CommUtil.isNull(accs))
            throw DpModuleError.DpstAcct.BNAS1125(dataItem.getCustac(), dataItem.getAuacno());

        if (CommUtil.isNull(accs.getFcflag()))
            throw DpModuleError.DpstAcct.BNAS0844();
        
        KnaAcct tblKnaAcct = null;
        KnaFxac tblKnaFxac = SysUtil.getInstance(KnaFxac.class);
        String brchno = null;
        if(accs.getFcflag() == E_FCFLAG.CURRENT){
        	tblKnaAcct = ActoacDao.selKnaAcct(dataItem.getAuacno(), true);
        	acctbl = tblKnaAcct.getOnlnbl();
        	brchno = tblKnaAcct.getBrchno();
        }else if(accs.getFcflag() == E_FCFLAG.FIX){
        	tblKnaFxac = ActoacDao.selKnaFxac(dataItem.getAuacno(), true);
        	acctbl = tblKnaFxac.getOnlnbl();
        	brchno = tblKnaFxac.getBrchno();
        }else{
        	throw DpModuleError.DpstAcct.BNAS1739();
        }
        
		//标记最小转出金额
		if(CommUtil.compare(tranam, miAmt) <= 0){
			tranam = miAmt;
		}
		
		if(CommUtil.compare(upAmt, BigDecimal.ZERO) > 0){
			//BigDecimal tmp = tranam.subtract(miAmt).divide(upAmt, 0, BigDecimal.ROUND_DOWN); //向下取整
			BigDecimal[] tmp = tranam.subtract(miAmt).divideAndRemainder(upAmt); //参数0：商数 参数1：余数
			if(!CommUtil.equals(tmp[1], BigDecimal.ZERO)){ //余数不为0
				tranam = miAmt.add(upAmt.multiply(tmp[0].add(BigDecimal.valueOf(1)))); //计算新的交易金额
			}
		}
		
		if(CommUtil.compare(tranam, acctbl) > 0){
			tranam = acctbl;
		}
		// 冻结的账户不进行处理。
		bizlog.debug("智能储蓄账号[" + dataItem.getAuacno() + "]转出开始，金额[" + tranam + "]");
		bizlog.debug("存款账号金额：[" + acctbl + "]");
		bizlog.debug("转出最小金额：[" + miAmt + "]");
		bizlog.debug("转出递增金额：[" + upAmt + "]");
		// 每一笔交易重新生成一笔流水，用来进行平衡性检查
		 MsSystemSeq.getTrxnSeq();
		//已经做过解约的，且交易金额等于账户余额的，直接做销户处理
		if(CommUtil.isNotNull(dataItem.getCncldt()) && CommUtil.compare(tranam, acctbl) == 0){ 
			
			KnaAcct setKnaAcct = CapitalTransDeal.getSettKnaAcctSub(dataItem.getCustac(), E_ACSETP.SA);
			IoDpCloseIN clsin = SysUtil.getInstance(IoDpCloseIN.class);
			
			clsin.setCardno("");
			clsin.setToacct(setKnaAcct.getAcctno());
			clsin.setTobrch(setKnaAcct.getBrchno());
			clsin.setToname(setKnaAcct.getAcctna());
			clsin.setSmrycd(BusinessConstants.SUMMARY_XH);
			clsin.setSmryds("销户");
			
			if(CommUtil.isNotNull(tblKnaAcct)){
				
				//获取可售产品名称
				KnaAcctProd tblKnaAcctProd = DpAcctDao.selKnaAcctProdByAcctno(dataItem.getAuacno(), false);
				if(CommUtil.isNotNull(tblKnaAcctProd)){
					clsin.setRemark(tblKnaAcctProd.getObgaon());
				}
				
				InterestAndIntertax cplint	=	DpCloseAcctno.prcCurrInterest(tblKnaAcct, clsin);
				BigDecimal interest = cplint.getInstam();//利息
				BigDecimal intxam = cplint.getIntxam();//利息税
				BigDecimal onlnbl = DpCloseAcctno.prcCurrOnbal(tblKnaAcct, clsin);
				prcPostAcct(setKnaAcct, tblKnaAcct.getAcctno(), onlnbl.add(interest),intxam);
			}
			
			if(CommUtil.isNotNull(tblKnaFxac) && CommUtil.isNotNull(tblKnaFxac.getAcctno())){
				
				
				DrawDpAcctOut  drawDpAcctOut=DpCloseAcctno.prcClsFxacAcct(tblKnaFxac, setKnaAcct.getAcctno(), E_YES___.YES);
				BigDecimal interest = drawDpAcctOut.getInstam();//利息
				BigDecimal intxam = drawDpAcctOut.getIntxam();//利息税
				prcPostAcct(setKnaAcct, tblKnaFxac.getAcctno(), tblKnaFxac.getOnlnbl().add(interest),intxam);
			}
			
			SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).custUnSign(dataItem.getSignno(), E_YES___.YES);
		}else{
//			try {
				
				//备注要求是 产品名称
				String remark = "";
				KnaAcctProd tblacprod = KnaAcctProdDao.selectOne_odb1(dataItem.getAuacno(), false);
				KnaFxacProd tblfxprod = KnaFxacProdDao.selectOne_odb1(dataItem.getAuacno(), false);
				if(CommUtil.isNotNull(tblacprod)){
					remark = tblacprod.getObgaon();
				}
				if(CommUtil.isNotNull(tblfxprod)){
					remark = tblfxprod.getObgaon();
				}
				
				// 智能储蓄支取
				DpSaveEntity input_draw = SysUtil.getInstance(DpSaveEntity.class);
				input_draw.setAcctno(dataItem.getAuacno());
				input_draw.setAcseno(dataItem.getAusbac());
				input_draw.setCardno(dataItem.getCardno());
				input_draw.setCrcycd(dataItem.getCrcycd());
				input_draw.setCustac(dataItem.getCustac());
				input_draw.setOpacna(dataItem.getAcctna());
				input_draw.setToacct(dataItem.getAcctno());
				input_draw.setOpbrch(brchno);
				input_draw.setSmrycd(BusinessConstants.SUMMARY_ZC);
				input_draw.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_ZC));
				input_draw.setRemark("结算主账户-"+remark);
				
				input_draw.setTranam(tranam);
				input_draw.setAuacfg(E_YES___.NO);// 不是普通的智能储蓄存取
				bizlog.debug("智能储蓄定期支取");
				DpPublicServ.drawAcctDp(input_draw);
				bizlog.debug("智能储蓄定期支取完成");

				// 活期存入
				DpSaveEntity input_post = SysUtil.getInstance(DpSaveEntity.class);
				input_post.setAcctno(dataItem.getAcctno());
				input_post.setAcseno(dataItem.getAcseno());
				input_post.setCardno(dataItem.getCardno());
				input_post.setCrcycd(dataItem.getCrcycd());
				input_post.setCustac(dataItem.getCustac());
				input_post.setOpacna(dataItem.getAcctna());
				input_post.setToacct(dataItem.getAuacno());
				input_post.setSmrycd(BusinessConstants.SUMMARY_ZR);
				input_post.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_ZR));
				input_post.setRemark(remark);
				
				input_post.setTranam(tranam);
				input_post.setAuacfg(E_YES___.NO);// 不是普通的智能储蓄存取
				bizlog.debug("电子账户存入");
				DpPublicServ.postAcctDp(input_post);
				bizlog.debug("电子账户存入完成");
//			} catch (Exception e) {
//				e.printStackTrace();
//				String errmsg = "账号[" + dataItem.getAcctno() + "]智能储蓄定期转入活期失败。["+ e.getMessage() + "]";
//				bizlog.error(errmsg);
//				// 如果需要登记错误信息到数据库中，需要用下面的方法
//				// DaoUtil.executeInNewTransation(new
//				// RunnableWithReturn<Integer>(){
//				//
//				// @Override
//				// public Integer execute() {
//				//
//				// return 0;
//				// }
//				//
//				// });
//				throw ApError.BusiAplt.E0000(errmsg);
//			}
		}
		
		
		

		// 检查平衡
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		IoCheckBalance ioCheckBalanceSrv = SysUtil
				.getInstance(IoCheckBalance.class);
		ioCheckBalanceSrv.checkBalance(trandt, transq,null);
		
//		CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);
	}

	/***
	 * @Title: prcPostAcct 
	 * @Description: 结算户存入记账服务 
	 * @param acct
	 * @param toacct
	 * @param tranam
	 * @author zhangan
	 * @date 2016年7月20日 下午4:29:15 
	 * @version V2.3.0
	 */
	public static void prcPostAcct(KnaAcct acct, String toacct, BigDecimal tranam,BigDecimal intxam){
		
		String remark = "";//可售产品名称
		//获取可售产品名称值
		KnaAcctProd tblAcctProd = DpAcctDao.selKnaAcctProdByAcctno(toacct, false);
		KnaFxacProd tblFxacProd = DpAcctDao.selKnaFxacProdByAcctno(toacct, false);
		
		if(CommUtil.isNotNull(tblAcctProd)){
			remark = "活期-"+tblAcctProd.getObgaon();
		}else if(CommUtil.isNotNull(tblFxacProd)){
			remark = "定期-"+tblFxacProd.getObgaon();
		}
		
		if(CommUtil.compare(tranam, BigDecimal.ZERO) > 0){
		
			SaveDpAcctIn cplSaveIn = SysUtil.getInstance(SaveDpAcctIn.class);
			
			cplSaveIn.setAcctno(acct.getAcctno());
			cplSaveIn.setAcseno(null);//
			cplSaveIn.setBankcd(null);
			cplSaveIn.setBankna(null);
			cplSaveIn.setCardno(null);
			cplSaveIn.setCrcycd(acct.getCrcycd());
			cplSaveIn.setCustac(acct.getCustac());
			cplSaveIn.setLinkno(null);
			cplSaveIn.setOpacna(acct.getAcctna());
			cplSaveIn.setOpbrch(acct.getBrchno());
			cplSaveIn.setRemark(remark);
			cplSaveIn.setSmrycd(BusinessConstants.SUMMARY_TZ);
			cplSaveIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_TZ));
			cplSaveIn.setToacct(toacct);
			cplSaveIn.setTranam(tranam);//本金+利息（包含利息税）
			cplSaveIn.setIschck(E_YES___.NO);
			
			SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveIn);
		}
		//利息税入账
		DrawDpAcctIn drawin = SysUtil.getInstance(DrawDpAcctIn.class); //支取服务输入参数
		if(CommUtil.compare(intxam, BigDecimal.ZERO) > 0){
			//结算户支取记账处理				
			drawin.setAcctno(acct.getAcctno()); //做支取的负债账号
			drawin.setAuacfg(E_YES___.NO);
			drawin.setCardno(null);
			drawin.setCrcycd(acct.getCrcycd());
			drawin.setCustac(acct.getCustac());
			drawin.setLinkno(null);
			drawin.setOpacna(acct.getAcctna());
			drawin.setToacct(acct.getAcctno()); //结算账号
			drawin.setTranam(intxam);
			drawin.setSmrycd(BusinessConstants.SUMMARY_JS);
			drawin.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_JS));
			drawin.setRemark(remark);
			SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawin);
						
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
	public BatchDataWalker<AuacinTranData> getBatchDataWalker(
			cn.sunline.ltts.busi.dptran.batchtran.intf.Fxtocu.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Fxtocu.Property property) {

		if (CommUtil.equals(getStopfg(), "STOP")) {
			throw ApError.BusiAplt.E0000("智能储蓄定转活已暂停。");
		}

		Params params = new Params();
		params.add("trandt", trandt);
		params.add("signst", E_SIGNST.QY);
		params.add("signtp", E_SIGNTP.ZNCXL);
		CursorBatchDataWalker<cn.sunline.ltts.busi.dp.type.DpDayEndType.AuacinTranData> cursorBatchDataWalker
		    = new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.type.DpDayEndType.AuacinTranData>(
	                DpDayEndDao.namedsql_selFxToCuData, params);
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.type.DpDayEndType.AuacinTranData>(
				DpDayEndDao.namedsql_selFxToCuData, params);
	}
}
