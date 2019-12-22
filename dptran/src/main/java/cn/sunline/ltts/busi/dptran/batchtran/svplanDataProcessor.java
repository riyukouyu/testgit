package cn.sunline.ltts.busi.dptran.batchtran;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpSaveDrawDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSavePlan;
import cn.sunline.ltts.busi.dp.type.DpAcctType.KnaAcctFxacInfo;
import cn.sunline.ltts.busi.dptran.batchtran.redpck.rpbackDataProcessor;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;


/**
 * 存入计划批量处理
 * 
 */

public class svplanDataProcessor
		extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.intf.Svplan.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Svplan.Property, cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSavePlan> {
	private static BizLog log = BizLogUtil.getBizLog(rpbackDataProcessor.class);
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
			cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSavePlan dataItem,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Svplan.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Svplan.Property property) {
		String acctno_plan = dataItem.getAcctno();//存入计划账号
		BigDecimal bal = BigDecimal.ZERO;//可用余额
		//查询负债账户信息
		IoCaKnaAccs cplKnaAccs = DpAcctQryDao.selKnaAccsByAcctno(acctno_plan, true);
		try{
			// 判断当前转出账户状态字是否异常
			CapitalTransCheck.ChkAcctstWord(cplKnaAccs.getCustac());
			// 判断当前转出账户状态是否异常
			CapitalTransCheck.ChkAcctstOT(cplKnaAccs.getCustac());
		}catch(Exception e){
			return ;
		}
		//设置数据
		KnaAcctFxacInfo dpAcct = SysUtil.getInstance(KnaAcctFxacInfo.class);
		dpAcct = setPostIn(cplKnaAccs);
		if(dpAcct.getAcctst() == E_DPACST.NORMAL){
			//查询该客户的结算账户
			E_ACSETP acsetp = E_ACSETP.SA;
			E_DPACST acctst = E_DPACST.NORMAL;
			KnaAcct ouAcct = ActoacDao.selKnaAcctByAcst(acsetp, cplKnaAccs.getCustac(), acctst, true);
			
			// 查询结算账户的可用余额  
			bal = SysUtil.getInstance(DpAcctSvcType.class)
					.getAcctaAvaBal(cplKnaAccs.getCustac(), ouAcct.getAcctno(),
							ouAcct.getCrcycd(), E_YES___.YES, E_YES___.NO);
			
			//检查子账户余额是否能足额支取
			if(CommUtil.compare(bal, dataItem.getPlmony()) >= 0){
				//调用支取记账服务
				drawAcctDp(ouAcct,dataItem.getPlmony());
				//调用存入记账服务
				postAcctDp(dpAcct,dataItem.getPlmony());
			}
		}
	}
	/**
	 * 调用存入服务
	 * @param dataItem
	 * @param acctIn
	 * @param acdcIn
	 * @param swpamt
	 */
	private void postAcctDp(
			KnaAcctFxacInfo acctIn, BigDecimal swpamt) {
		SaveDpAcctIn save = SysUtil.getInstance(SaveDpAcctIn.class);
		DpAcctSvcType dpsvc = SysUtil.getInstance(DpAcctSvcType.class);
		
		E_YES___ isflag = E_YES___.YES;
		save.setAcctno(acctIn.getAcctno());
		save.setAcseno("");
		//save.setCardno(acctIn.getAcctno());
		save.setCrcycd(BusiTools.getDefineCurrency());
		save.setCustac(acctIn.getCustac());
		save.setOpacna(acctIn.getAcctna());
		save.setToacct(acctIn.getAcctno());
		save.setTranam(swpamt);
		save.setSmrycd(BusinessConstants.SUMMARY_ZR);
		save.setIschck(isflag);
		save.setRemark(BusiTools.getBusiRunEnvs().getRemark());
		dpsvc.addPostAcctDp(save);
	}
	/**
	 * 调用支取服务
	 * @param acctOut
	 * @param acdcOut
	 * @param swpamt
	 */
	private void drawAcctDp(
			KnaAcct acctOut, BigDecimal swpamt) {
		DpAcctSvcType dpAcct = SysUtil.getInstance(DpAcctSvcType.class);
		DrawDpAcctIn dpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
		dpAcctIn.setAcctno(acctOut.getAcctno());
		dpAcctIn.setAcseno("");
		dpAcctIn.setCustac(acctOut.getCustac());
		dpAcctIn.setCrcycd(BusiTools.getDefineCurrency());
		dpAcctIn.setOpacna(acctOut.getAcctna());
		dpAcctIn.setToacct(acctOut.getAcctno());
		dpAcctIn.setTranam(swpamt);
		dpAcctIn.setSmrycd(BusinessConstants.SUMMARY_ZC);
		dpAcctIn.setRemark(BusiTools.getBusiRunEnvs().getRemark());
		dpAcct.addDrawAcctDp(dpAcctIn);
		
	}
	/**
	 * 数据设置
	 * @param cplKnaAccs
	 * @return
	 */
	private KnaAcctFxacInfo setPostIn(IoCaKnaAccs cplKnaAccs){
		KnaAcctFxacInfo dpAcct = SysUtil.getInstance(KnaAcctFxacInfo.class);
		if(E_FCFLAG.CURRENT == cplKnaAccs.getFcflag()){
			KnaAcct knaAcct = ActoacDao.selKnaAcct(cplKnaAccs.getAcctno(), true);
			dpAcct.setAcctno(knaAcct.getAcctno());
			dpAcct.setAcctna(knaAcct.getAcctna());
			dpAcct.setCustac(knaAcct.getCustac());
			dpAcct.setAcctst(knaAcct.getAcctst());
		}else if (E_FCFLAG.FIX == cplKnaAccs.getFcflag()) {
			KnaFxac knaFxac =ActoacDao.selKnaFxac(cplKnaAccs.getAcctno(), false);
			dpAcct.setAcctno(knaFxac.getAcctno());
			dpAcct.setAcctna(knaFxac.getAcctna());
			dpAcct.setCustac(knaFxac.getCustac());
			dpAcct.setAcctst(knaFxac.getAcctst());
		}
		return dpAcct;
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
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSavePlan> getBatchDataWalker(
			cn.sunline.ltts.busi.dptran.batchtran.intf.Svplan.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Svplan.Property property) {
		log.info("<<===================获取作业数据集合开始======================>>");
		Params param = new Params();
		param.add("trandt", CommTools.getBaseRunEnvs().getTrxn_date());
		log.info("<<===================获取作业数据集合结束======================>>");
		return new CursorBatchDataWalker<KnaSavePlan>(
				DpSaveDrawDao.namedsql_selKnaSavePlansCurrt,param);
	}

}
