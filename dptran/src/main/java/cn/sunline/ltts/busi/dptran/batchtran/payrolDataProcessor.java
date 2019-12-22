
package cn.sunline.ltts.busi.dptran.batchtran;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessorWithJobDataItem;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctBach;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctBachDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStFzIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStFzOt;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStUfIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpAcdcOut;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;

	 /**
	  * 代发工资
	  *
	  */


public class payrolDataProcessor extends
  AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.dptran.batchtran.intf.Payrol.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Payrol.Property, String, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctBach> {
	  	
		private BizLog log = BizLogUtil.getBizLog(payrolDataProcessor.class);
		private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
		private IoDpAcdcOut ioDpAcdcOut;//卡客户账户信息
		private KnaAcct knaAcctOut;//转出方账户信息
		
		
		/**
		 * 交易前处理
		 */
		@Override
		public void beforeTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchtran.intf.Payrol.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Payrol.Property property){
			log.info("<<===================批量易前更新处理状态======================>>");
			filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
			filetab.setBtfest(E_BTFEST.DING);
			Kapb_wjplxxbDao.updateOne_odb1(filetab);
			property.setSourdt(filetab.getAcctdt());
			log.info("<<===================filetab======================>>"+filetab.toString());
			
			
			log.info("<<===================冻结转出方金额======================>>");
			//检查转出方账户状态
			KnbAcctBach knbAcctBach = DpAcctDao.selPayrolAcctInfo(input.getFilesq(),true);//查询当前转出方的账号及转出总额
			ioDpAcdcOut = DpAcctDao.selKnaAcdcInfoByCardno(knbAcctBach.getPyacct(), true);
			// 1. 判断当前转出账户状态字是否异常
			CapitalTransCheck.ChkAcctstWord(ioDpAcdcOut.getCustac());
			CapitalTransCheck.ChkAcctstOT(ioDpAcdcOut.getCustac());
			//2.统计需转出金额
			
			//3.计算需冻结金额
			knaAcctOut = CapitalTransDeal.getSettKnaAcctSubAcLock(ioDpAcdcOut.getCustac(), E_ACSETP.SA);//查询结算户信息
			BigDecimal avaBal = SysUtil.getInstance(DpAcctSvcType.class).getAcctaAvaBal(ioDpAcdcOut.getCustac(), knaAcctOut.getAcctno(),knaAcctOut.getCrcycd(), E_YES___.YES, E_YES___.NO);//获取结算户可用余额
			log.info("<<===================结算户可用余额："+avaBal);
			if(avaBal.compareTo(knbAcctBach.getSwpamt())>=0){
				//可用余额>=待转出金额，冻结待转出金额，否则冻结全部可用余额
				avaBal = knbAcctBach.getSwpamt();
			}
			log.info("<<===================冻结金额："+avaBal);
			//4.冻结待转出金额
			IoDpStFzIn ioDpStFzIn = SysUtil.getInstance(IoDpStFzIn.class);//冻结输入接口
			ioDpStFzIn.setCardno(ioDpAcdcOut.getCardno());
			ioDpStFzIn.setCrcycd(knaAcctOut.getCrcycd());
			ioDpStFzIn.setFrozam(avaBal);
			ioDpStFzIn.setFroztp(E_FROZTP.AM);
			IoDpFrozSvcType ioDpFrozSvcType = SysUtil.getInstance(IoDpFrozSvcType.class);//冻结服务
			IoDpStFzOt ioDpStFzOt = ioDpFrozSvcType.IoDpStFz(ioDpStFzIn);
			property.setFrzout(ioDpStFzOt);//冻结输出信息
			log.info("<<===================冻结转出方金额完成======================>>");
			super.beforeTranProcess(taskId, input, property);
		}
	
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctBach dataItem, cn.sunline.ltts.busi.dptran.batchtran.intf.Payrol.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Payrol.Property property) {
			log.info("<<===================开始转账======================>>");
			//1.检查转入方账户状态
			// 检查是否涉案
			IoCaSevQryAccout.IoCaQryInacInfo.Output out = SysUtil.getInstance(IoCaSevQryAccout.IoCaQryInacInfo.Output.class);
			SysUtil.getInstance(IoCaSevQryAccout.class).qryInac(dataItem.getAcctno(), dataItem.getAcctna(), null, null, out);
			if (E_INSPFG.INVO == out.getInspfg()) {
				throw DpModuleError.DpstComm.E9999("转入账户为涉案账户");
			}
			// 判断当前转入账户状态字是否异常
			IoDpAcdcOut ioDpAcdcIn = SysUtil.getInstance(IoDpAcdcOut.class);
			ioDpAcdcIn = DpAcctDao.selKnaAcdcByAcctno(dataItem.getAcctno(), true);
			CapitalTransCheck.ChkAcctFrozIN(ioDpAcdcIn.getCustac());
			CapitalTransCheck.ChkAcctstRe(ioDpAcdcIn.getCustac());
			//2.调用转入服务
			KnaAcct knaAcctIn = SysUtil.getInstance(KnaAcct.class);
			knaAcctIn = CapitalTransDeal.getSettKnaAcctAcLock(ioDpAcdcIn.getCustac());//获取结算户信息
			SaveDpAcctIn saveDpAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);//存入输入接口
			DpAcctSvcType dpAcctSvcType = SysUtil.getInstance(DpAcctSvcType.class);//存入服务
			saveDpAcctIn.setAcctno(knaAcctIn.getAcctno());
			saveDpAcctIn.setAcseno("");
			saveDpAcctIn.setCardno(ioDpAcdcIn.getCardno());
			saveDpAcctIn.setCrcycd(BusiTools.getDefineCurrency());
			saveDpAcctIn.setCustac(knaAcctIn.getCustac());
			saveDpAcctIn.setOpacna(dataItem.getPyacna());
			saveDpAcctIn.setToacct(dataItem.getPyacct());
			saveDpAcctIn.setTranam(dataItem.getSwpamt());
			saveDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_ZR);
			saveDpAcctIn.setRemark("代发工资存入");
			dpAcctSvcType.addPostAcctDp(saveDpAcctIn);
			//更新KnbAcctBach表状态为处理中
			dataItem.setTranst(E_TRANST.SUCCESS);
			KnbAcctBachDao.updateOne_odb2(dataItem);
			log.info("<<===================["+dataItem.getAcctno()+"]存入转账完成======================>>");
		}
		
		/**
		 * 交易后处理
		 * @param taskId
		 * @param input
		 * @param property
		 */
		@Override
		public void afterTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchtran.intf.Payrol.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Payrol.Property property){
			//1.转出方额度解冻
			IoDpStUfIn ioDpStUfIn = SysUtil.getInstance(IoDpStUfIn.class);//解冻输入接口
			ioDpStUfIn.setMntrsq(property.getFrzout().getMntrsq());
			ioDpStUfIn.setTrandt(property.getFrzout().getFrozdt());
			IoDpFrozSvcType ioDpFrozSvcType = SysUtil.getInstance(IoDpFrozSvcType.class);//解冻结服务
			ioDpFrozSvcType.IoDpStUf(ioDpStUfIn);
			//2.查询处理中金额总额
			BigDecimal alpaym = DpAcctDao.selPayrolAcctTranst(input.getFilesq(),true).getSwpamt();
			log.info("<<===================转出金额["+alpaym+"]开始=====================>>");
			//3.调用转出服务
			DpAcctSvcType dpAcctSvcType = SysUtil.getInstance(DpAcctSvcType.class);
			DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
			drawDpAcctIn.setAcctno(knaAcctOut.getAcctno());
			drawDpAcctIn.setAcseno("");
			drawDpAcctIn.setCardno(ioDpAcdcOut.getCardno());
			drawDpAcctIn.setCustac(ioDpAcdcOut.getCustac());
			drawDpAcctIn.setCrcycd(BusiTools.getDefineCurrency());
			drawDpAcctIn.setTranam(alpaym);
			drawDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_ZC);
			drawDpAcctIn.setRemark("代发工资支取");
			dpAcctSvcType.addDrawAcctDp(drawDpAcctIn);
		}
		
		/**
		 * 交易异常处理
		 */
		@Override
		public void jobExceptionProcess(String taskId, cn.sunline.ltts.busi.dptran.batchtran.intf.Payrol.Input input,
				cn.sunline.ltts.busi.dptran.batchtran.intf.Payrol.Property property, String jobId, KnbAcctBach dataItem, Throwable t) {

			// DaoUtil.rollbackTransaction(); //主事物回滚
			log.info("<<==================处理异常，更新批量失败开始======================>>");
			dataItem.setTranst(E_TRANST.FAIL);
			dataItem.setDescrb(t.getMessage());
			KnbAcctBachDao.updateOne_odb2(dataItem);
			log.info("<<==================处理异常，更新批量失败结束======================>>");

		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<String> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.intf.Payrol.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Payrol.Property property) {
			log.info("<<===================获取作业编号开始======================>>");
			Params param = new Params();
			param.put("filesq", input.getFilesq());
			param.put("sourdt", property.getSourdt());
			return new CursorBatchDataWalker<String>(DpAcctDao.namedsql_selDataidByFilesq,param);
		}
		
		/**
		 * 获取作业数据遍历器
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @param dataItem 批次数据项
		 * @return
		 */
		public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbAcctBach> getJobBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.intf.Payrol.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Payrol.Property property, String dataItem) {
			log.info("<<===================获取作业数据集合开始======================>>");
			Params param = new Params();
			param.put("filesq", input.getFilesq());
			param.put("sourdt", property.getSourdt());
			param.put("dataid",dataItem);
			log.info("<<===================获取作业数据集合结束======================>>");
			return new CursorBatchDataWalker<KnbAcctBach>(DpAcctDao.namedsql_selAcctTranferInfo, param);
		}
	  

}


