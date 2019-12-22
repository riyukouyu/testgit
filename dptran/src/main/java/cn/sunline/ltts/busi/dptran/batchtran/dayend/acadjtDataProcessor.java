package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctAdjst;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctAdjstDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ACUTTP;

	 /**
	  * 批量调账
	  *
	  */

public class acadjtDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Acadjt.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Acadjt.Property, String> {
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
		public void process(String jobId, int index, String dataItem, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Acadjt.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Acadjt.Property property) {
			bizlog.debug("<<===================处理批次号======================>>"+dataItem);
			List<KnbAcctAdjst> adjst = DpDayEndDao.selKnbAcctAdjstInfo(dataItem, false);
			if(adjst.size()<1){
				bizlog.debug("<<===================调账批次明细为空跳过！======================>>"+dataItem);
				return;
			}
			
			 MsSystemSeq.getTrxnSeq();
			
			for(KnbAcctAdjst tbadjst:adjst){
				if (ApAcctRoutTools.getRouteType(tbadjst.getCustac()) == E_ACCTROUTTYPE.INSIDE){
					
					IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
					iaAcdrInfo.setTrantp(E_TRANTP.TR);
					iaAcdrInfo.setAcctno(tbadjst.getCustac());
					iaAcdrInfo.setTranam(tbadjst.getTranam());//记账金额
					iaAcdrInfo.setAcuttp(E_ACUTTP._1);//记账类型正常 
//					iaAcdrInfo.setDscrtx(knsStrk2.getReason());
					

					iaAcdrInfo.setCrcycd(tbadjst.getCrcycd());//币种
					
					
					//挂账处理
//					if(dataItem.getCharlg() == E_PAYATP._1){
//						Options<IaPayaDetail> iaPayaDetailOption = new DefaultOptions<IaPayaDetail>();
//						List<KnsPaya> knsPayaList = new ArrayList<KnsPaya>();
//						knsPayaList = KnsPayaDao.selectAll_kns_paya_odx3(knsStrk1.getNumbsq(), knsStrk1.getCustac(), trandt, true);
//						for(KnsPaya knsPaya : knsPayaList){
//							if(knsPaya.getPayast() ==E_PAYAST.ZF){
//								continue;
//							}
//							IaPayaDetail iaPayaDetail = SysUtil.getInstance(IaPayaDetail.class);
//							iaPayaDetail.setPayaac(knsPaya.getPayaac());
//							iaPayaDetail.setPayabr(knsPaya.getPayabr());
//							iaPayaDetail.setPayamn(knsPaya.getPayamn());
//							iaPayaDetail.setPayasq(knsPaya.getPayasq());
//							iaPayaDetail.setToacna(knsPaya.getToacna());
//							iaPayaDetail.setToacno(knsPaya.getToacct());
//							iaPayaDetailOption.add(iaPayaDetail);
//						}
//						iaAcdrInfo.setPayadetail(iaPayaDetailOption);
//					}
//					
//					//销账处理
//					if(dataItem.getCharlg() == E_PAYATP._2){
//						
//						Options<IaPaydDetail> iaPaydDetailOption = new DefaultOptions<IaPaydDetail>();
//						List<KnsPayd> knsPaydList = new ArrayList<KnsPayd>();
//						knsPaydList = KnsPaydDao.selectAll_kns_payd_odx3(knsStrk1.getNumbsq(), knsStrk1.getCustac(), trandt, true);
//						
//						for(KnsPayd knsPayd : knsPaydList){
//							if(knsPayd.getPaydst() ==E_PAYDST.ZF){
//								continue;
//							}
//							IaPaydDetail iaPaydDetail = SysUtil.getInstance(IaPaydDetail.class);
//							iaPaydDetail.setPaydmn(knsPayd.getPayamn());
//							iaPaydDetail.setPrpysq(knsPayd.getPayasq());
//							iaPaydDetail.setPaydac(knsPayd.getPaydac());
//							iaPaydDetail.setRsdlmn(knsPayd.getRsdlmn());
//							iaPaydDetail.setTotlmn(knsPayd.getTotlmn());
//							iaPaydDetail.setPaydsq(knsPayd.getPaydsq());
//							iaPaydDetailOption.add(iaPaydDetail);
//						}
//						iaAcdrInfo.setPayddetail(iaPaydDetailOption);
//					}
					
					//记账方向
					E_AMNTCD amntcd = tbadjst.getAmntcd();		
					//调用内部户记账服务
					IoInAccount ioInAcctount = CommTools.getRemoteInstance(IoInAccount.class);

					switch (amntcd){
						case DR:
							iaAcdrInfo.setSmrycd(BusinessConstants.SUMMARY_ZC);
							ioInAcctount.ioInAcdr(iaAcdrInfo);//内部户借方服务
							break;
						case CR:
							iaAcdrInfo.setSmrycd(BusinessConstants.SUMMARY_ZR);
							ioInAcctount.ioInAccr(iaAcdrInfo);//内部户贷方服务
							break;
						default:
							throw InError.comm.E0003("记账方向:"+amntcd.getValue()+"["+amntcd.getLongName()+"]不支持");
					}
				}else{
					//客户账入账
					IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcByCardno(tbadjst.getCustac(), true);

					
					IoDpKnaAcct ioDpKnaAcct= SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(tblKnaAcdc.getCustac());
					E_AMNTCD amntcd = tbadjst.getAmntcd();
					BigDecimal tranam = BigDecimal.ZERO;
					
					tranam = tbadjst.getTranam();							
					
//					String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//					CommTools.getBaseRunEnvs().setBusi_org_id(ioDpKnaAcct.getCorpno());
					
		
					
					if(E_AMNTCD.DR==amntcd){
						
						SysUtil.getInstance(IoAccountSvcType.class).checkStatusBeforeAccount(tblKnaAcdc.getCustac(), cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD.DR,E_YES___.YES, E_YES___.NO);
					}else{
						
						SysUtil.getInstance(IoAccountSvcType.class).checkStatusBeforeAccount(tblKnaAcdc.getCustac(), cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD.CR,E_YES___.YES, E_YES___.NO);
					}
					
			
					//客户账记账输入类字段赋值
					if(E_AMNTCD.CR==amntcd){
						//设置法人为当前转出账户法人
//						CommTools.getBaseRunEnvs().setBusi_org_id(ioDpKnaAcct.getCorpno());
						bizlog.debug("法人代码------"+CommTools.getBaseRunEnvs().getBusi_org_id());
						
						SaveDpAcctIn saveDpAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
						saveDpAcctIn.setAcctno(ioDpKnaAcct.getAcctno());
						saveDpAcctIn.setCustac(ioDpKnaAcct.getCustac());
						saveDpAcctIn.setCardno(tbadjst.getCustac());
						saveDpAcctIn.setOpacna(tbadjst.getAcctna());
						saveDpAcctIn.setToacct(tbadjst.getToacct());
						saveDpAcctIn.setCrcycd(tbadjst.getCrcycd());
						saveDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_ZR);
						saveDpAcctIn.setRemark(tbadjst.getRemark());
						saveDpAcctIn.setTranam(tranam);
						SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(saveDpAcctIn);
					} else {
//						CommTools.getBaseRunEnvs().setBusi_org_id(ioDpKnaAcct.getCorpno());
						//调支取服务
						DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
						drawDpAcctIn.setAcctno(ioDpKnaAcct.getAcctno());
						drawDpAcctIn.setCustac(ioDpKnaAcct.getCustac());
						drawDpAcctIn.setCardno(tbadjst.getCustac());
						drawDpAcctIn.setOpacna(tbadjst.getAcctna());
						drawDpAcctIn.setToacct(tbadjst.getCustac());
						drawDpAcctIn.setCrcycd(tbadjst.getCrcycd());
						drawDpAcctIn.setIschck(E_YES___.NO);
						drawDpAcctIn.setTranam(tranam);
						drawDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_ZC);
						drawDpAcctIn.setRemark(tbadjst.getRemark());
						SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawDpAcctIn);
					}
					
					
//					CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
				}								
				//更新记账状态
				tbadjst.setTranst(E_TRANST.SUCCESS);
				tbadjst.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
				tbadjst.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
				KnbAcctAdjstDao.updateOne_odb1(tbadjst);
			}
			
			//交易平衡性检查
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), 
					CommTools.getBaseRunEnvs().getMain_trxn_seq(),adjst.get(0).getClactp());
			
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<String> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Acadjt.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Acadjt.Property property) {
			bizlog.debug("<<===================获取作业编号开始======================>>");
			Params params = new Params();
			params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
//			ListBatchDataWalker<String>(ls);
//			List<String> ls = DpDayEndDao.selKnbAcctAdjstAll(taskId, true);
			return new CursorBatchDataWalker<String>(DpDayEndDao.namedsql_selKnbAcctAdjstAll,params);
		}
		
		public void jobExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Acadjt.Input input,
				cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Acadjt.Property property, String jobId, String dataItem, Throwable t) {

			// DaoUtil.rollbackTransaction(); //主事物回滚
			bizlog.debug("<<==================处理异常，更新批量失败开始======================>>");
			
			DpDayEndDao.updKnbAcctAdjstSt(E_TRANST.FAIL, dataItem);

			bizlog.debug("<<==================处理异常，更新批量失败结束======================>>");
			// super.jobExceptionProcess(taskId, input, property, jobId, dataItem,
			// t);

		}

}


