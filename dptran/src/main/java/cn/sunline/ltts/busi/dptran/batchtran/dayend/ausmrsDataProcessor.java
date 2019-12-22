package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.acct.DpCloseAcctno;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProd;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.AutoUnSign;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseIN;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_UNSGTP;
import cn.sunline.edsp.base.lang.Params;

	 /**
	  * 签约到期自动解约
	  *
	  */

public class ausmrsDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Ausmrs.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Ausmrs.Property, cn.sunline.ltts.busi.dp.type.DpDayEndType.AutoUnSign> {
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.dp.type.DpDayEndType.AutoUnSign dataItem, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Ausmrs.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Ausmrs.Property property) {
			
//			String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();

			 MsSystemSeq.getTrxnSeq();
			if(dataItem.getUnsgtp() == E_UNSGTP.T1){
				KnaAcct acct = KnaAcctDao.selectOne_odb1(dataItem.getAcctno(), true); //结算账号
				
				IoDpCloseIN clsin = SysUtil.getInstance(IoDpCloseIN.class);
				
				clsin.setCardno("");
				clsin.setToacct(acct.getAcctno());
				clsin.setTobrch(acct.getBrchno());
				clsin.setToname(acct.getAcctna());
				clsin.setSmrycd(BusinessConstants.SUMMARY_XH);
				clsin.setSmryds("销户");
				
				KnaAcct curr = KnaAcctDao.selectOne_odb1(dataItem.getFxacct(), false); //存款子账号
				if(CommUtil.isNotNull(curr)){
					
					//获取可售产品名称
					KnaAcctProd tblKnaAcctProd = DpAcctDao.selKnaAcctProdByAcctno(dataItem.getFxacct(), false);
					if(CommUtil.isNotNull(tblKnaAcctProd)){
						clsin.setRemark(tblKnaAcctProd.getObgaon());
					}
					curr.setOnlnbl(DpAcctProc.getAcctBalance(curr));
					InterestAndIntertax cplint = DpCloseAcctno.prcCurrInterest(curr, clsin);
					BigDecimal interest = cplint.getInstam();
					BigDecimal intxam = cplint.getIntxam();
					BigDecimal onlnbl = DpCloseAcctno.prcCurrOnbal(curr, clsin);
					prcPostAcct(acct, curr.getAcctno(), onlnbl.add(interest),intxam);
				}
				
				KnaFxac fxac = KnaFxacDao.selectOne_odb1(dataItem.getFxacct(), false); //存款子账号
				if(CommUtil.isNotNull(fxac)){
					//BigDecimal interest = DpCloseAcctno.prcClsFxacAcct(fxac, acct.getAcctno(), E_YES___.YES).getInstam();
					
					DrawDpAcctOut  drawDpAcctOut=DpCloseAcctno.prcClsFxacAcct(fxac, acct.getAcctno(), E_YES___.YES);
					BigDecimal interest = drawDpAcctOut.getInstam();//利息
					BigDecimal intxam = drawDpAcctOut.getIntxam();//利息税
					
					prcPostAcct(acct, fxac.getAcctno(), fxac.getOnlnbl().add(interest),intxam);
				}
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
			}
			//解约服务
			SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).custUnSign(dataItem.getSignno(), E_YES___.NO);
			//平衡性检查
//			CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);
			
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
				cplSaveIn.setTranam(tranam);
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
				drawin.setSmrycd(BusinessConstants.SUMMARY_JS);// 摘要码-缴税
				drawin.setRemark(remark);
				SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawin);
							
			}					
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<AutoUnSign> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Ausmrs.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Ausmrs.Property property) {
			//TODO:	
			Params params = new Params();
			params.put("signst", E_SIGNST.QY);
			params.put("effedt", CommTools.getBaseRunEnvs().getTrxn_date());
			params.put("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
			return new CursorBatchDataWalker<AutoUnSign>(DpDayEndDao.namedsql_selAutoUnSignData, params);
		}

}


