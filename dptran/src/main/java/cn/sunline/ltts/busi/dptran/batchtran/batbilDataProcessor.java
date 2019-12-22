package cn.sunline.ltts.busi.dptran.batchtran;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessorWithJobDataItem;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.ListBatchDataWalker;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.redpck.BilBatchDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbCrcdBach;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbCrcdBachDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dptran.batchtran.intf.Batbil.Input;
import cn.sunline.ltts.busi.dptran.batchtran.intf.Batbil.Property;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaQryInacInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DEALST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRANTP;


/**
 * 信用卡批量还款处理
 * 
 */

public class batbilDataProcessor
		extends
		AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.dptran.batchtran.intf.Batbil.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Batbil.Property, String, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbCrcdBach> {

	private static BizLog log = BizLogUtil.getBizLog(batbilDataProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	private	E_DEALST dealst = null; //状态响应字

	@Override
	public void beforeTranProcess(String taskId,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batbil.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batbil.Property property) {

		log.debug("<<===================批量提现交易前更新处理状态======================>>");
		filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		//filetab.setBtfest(E_BTFEST.BUSIING);
		//Kapb_wjplxxbDao.updateOne_odb1(filetab);
		super.beforeTranProcess(taskId, input, property);
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
	public void process(String jobId, int index,
			cn.sunline.ltts.busi.dp.tables.RpMgr.KnbCrcdBach dataItem,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batbil.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batbil.Property property) {
		
		log.debug("<<===================批量退款交易处理开始======================>>");
		log.debug("<<=========================================>>");
		log.debug("贷方卡号：" + dataItem.getCrcard());
		log.debug("借方卡号：" + dataItem.getCardno());
		log.debug("<<=========================================>>");
		
		//获取账户余额时，会带法人查询，故先保存法人
//		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		
		//获取员交易流水作为记账流水
		CommTools.getBaseRunEnvs().setMain_trxn_seq(dataItem.getTransq());
//		 MsSystemSeq.getTrxnSeq();
		
		
		//TODO:恐怖名单检查
		if (CommUtil.isNotNull(dataItem.getCardno()) && CommUtil.isNotNull(dataItem.getCracna())){
			SysUtil.getInstance(IoCaSevQryAccout.class).IoCaQryInwadeInfo(dataItem.getCardno(), dataItem.getCracna(), null, null);
		}
		
		// 转出账号涉案账户查询
		if (CommUtil.isNotNull(dataItem.getCardno()) && CommUtil.isNotNull(dataItem.getCracna())) {
			
			final KnbCrcdBach bach = dataItem;
			IoCaQryInacInfo.Output outInfo = SysUtil.getInstance(IoCaQryInacInfo.Output.class);
			SysUtil.getInstance(IoCaSevQryAccout.class).qryInac(dataItem.getCardno(), dataItem.getCracna(), null, null, outInfo);
			
			// 涉案账户检查
			E_INSPFG invofg = outInfo.getInspfg();// 转出账号是否涉案

			// 涉案账户交易信息登记
			if (E_INSPFG.INVO == invofg) {

				// 独立事务
				DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
					@Override
					public Void execute() {

						// 获取涉案账户交易信息登记输入信息
						IoCaKnbTrinInput cplKnbTrin = SysUtil.getInstance(IoCaKnbTrinInput.class);
						cplKnbTrin.setTrantp(E_TRANTP.TRANSFER);// 交易类型
						cplKnbTrin.setOtcard(bach.getCardno());// 转出账号
						cplKnbTrin.setOtacna(bach.getCracna());// 转出账号名称
						cplKnbTrin.setOtbrch(CommTools.getBaseRunEnvs().getTrxn_branch());// 转出机构
						cplKnbTrin.setTranam(bach.getTranam());// 交易金额
						cplKnbTrin.setIssucc(E_YES___.NO);// 交易是否成功

						// 涉案账户交易信息登记
						SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(cplKnbTrin);

						return null;
					}
				});

				// 转出账号涉案
				dealst = E_DEALST.OTW; //黑名单，响应码设为其它原因
				throw DpModuleError.DpstAcct.BNAS0770();

			}

		}
			
		

		IaAcdrInfo acdr = SysUtil.getInstance(IaAcdrInfo.class); // 内部户记账参数
		IaTransOutPro inout = SysUtil.getInstance(IaTransOutPro.class); // 内部户记账输出
		
		IoInAccount insvc = SysUtil.getInstance(IoInAccount.class); // 内部户记账接口
		DpAcctSvcType dpsvc = SysUtil.getInstance(DpAcctSvcType.class); // 客户账记账接口
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		
		IoCaKnaAcdc acdc = caqry.getKnaAcdcOdb2(dataItem.getCardno(), false);
		if(CommUtil.isNull(acdc)){
			
			dealst = E_DEALST.CNW;
			throw DpModuleError.DpstComm.E9999("电子账户不存在");
		}
		
		//获得账户法人
//		CommTools.getBaseRunEnvs().setBusi_org_id(acdc.getCorpno());
		
		//客户化状态判断
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(acdc.getCustac());
		if(cuacst != E_CUACST.NORMAL){
			dealst = E_DEALST.CNW;
			
			throw DpModuleError.DpstComm.E9999("电子账户处于" + cuacst.getLongName() + "状态，请核查");
		}

		// 调用DP模块服务查询冻结状态，检查
		IoDpFrozSvcType froz = SysUtil.getInstance(IoDpFrozSvcType.class);
		IoDpAcStatusWord cplGetAcStWord = froz.getAcStatusWord(acdc.getCustac());
		if(cplGetAcStWord.getBkalsp() == E_YES___.YES){
			dealst = E_DEALST.OTW;
			throw DpModuleError.DpstComm.E9999("该电子账户处于银行止付全止状态");
		}
		if(cplGetAcStWord.getBrfroz() == E_YES___.YES){
			dealst = E_DEALST.OTW;
			throw DpModuleError.DpstComm.E9999("该电子账户处于借冻状态");
		}
		if(cplGetAcStWord.getDbfroz() == E_YES___.YES){
			dealst = E_DEALST.OTW;
			throw DpModuleError.DpstComm.E9999("该电子账户处于双冻状态");
		}
		if(cplGetAcStWord.getOtalsp() == E_YES___.YES){
			dealst = E_DEALST.OTW;
			throw DpModuleError.DpstComm.E9999("该电子账户处于外部止付全止状态");
		}
		if(cplGetAcStWord.getClstop() == E_YES___.YES){
			dealst = E_DEALST.OTW;
			throw DpModuleError.DpstComm.E9999("该电子账户处于账户保护全止状态");
		}
		
		
		//账户可为全功能账户或理财户
		E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(acdc.getCustac()); //账户类型
		if(accatp != E_ACCATP.GLOBAL && accatp != E_ACCATP.FINANCE){
			
			dealst = E_DEALST.CNW;
			throw DpModuleError.DpstComm.E9999("电子账户无此功能");
		}
		
		//带锁查询子账号  change by chenjk
		KnaAcct acct = CapitalTransDeal.getSettKnaAcctSubAcLock(acdc.getCustac(), E_ACSETP.SA);
		
		
		if(CommUtil.isNull(acct)){
			dealst = E_DEALST.CNW;
			throw DpModuleError.DpstComm.E9999("结算户不存在");
		}
		
		if(acct.getAcctst() != E_DPACST.NORMAL){
			dealst = E_DEALST.CNW;
			throw DpModuleError.DpstComm.E9999("结算户不处于正常状态，请核查");
		}
		
		//姓名检查
		if(!CommUtil.equals(acct.getAcctna(), dataItem.getCracna())){
			
			dealst = E_DEALST.NMW;
			throw DpModuleError.DpstComm.E9999("姓名不符，还款失败");
		}
		
		//TODO 币种检查，枚举值不一致，暂无法实现
		
		//余额检查，余额不足时，扣除全部余额
		BigDecimal tranam = dataItem.getTranam();
		BigDecimal usebal = BigDecimal.ZERO;
		
		/*   // 获取转存签约明细信息
		IoCaKnaSignDetl cplkna_sign_detl = SysUtil.getInstance(IoCaSevQryTableInfo.class)
				.kna_sign_detl_selectFirst_odb2(acct.getAcctno(), E_SIGNST.QY, false);
		
		// 存在转存签约明细信息则取资金池可用余额
		if (CommUtil.isNotNull(cplkna_sign_detl)) {
			usebal = DpAcctProc.getProductBal(acdc.getCustac(), BusiTools.getDefineCurrency(), E_YES___.YES, true);
		} else {
			// 其他取账户余额,正常的支取交易排除冻结金额
			usebal = DpAcctProc.getAcctOnlnblForFrozbl(acct.getAcctno(), E_YES___.YES, true);
		}*/
		
		// 可用余额 addby xiongzhao 20161223 
		usebal = SysUtil.getInstance(DpAcctSvcType.class)
				.getAcctaAvaBal(acdc.getCustac(), acct.getAcctno(),
						acct.getCrcycd(), E_YES___.YES, E_YES___.NO);
		
		if(CommUtil.equals(usebal, BigDecimal.ZERO)){
			dealst = E_DEALST.OTW;
			throw DpModuleError.DpstComm.E9999("账户可用余额为零");
		}
		
		if(CommUtil.compare(usebal, dataItem.getTranam()) < 0){
			
			tranam = usebal;
		}
		
		DrawDpAcctIn draw = SysUtil.getInstance(DrawDpAcctIn.class);
		
		dataItem.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		dataItem.setTrandt(filetab.getAcctdt());
		
		//59304001 待清算
		KnpParameter para = KnpParameterDao.selectOne_odb1("BATBIL", "%", "%", "%", true);
		
		draw.setAcctno(acct.getAcctno());
		draw.setAcseno(null);
		draw.setAuacfg(E_YES___.YES);
		draw.setCardno(dataItem.getCardno());
		draw.setCrcycd(acct.getCrcycd());
		draw.setCustac(acdc.getCustac());
		draw.setLinkno(null);
		draw.setOpacna(acct.getAcctna());
		draw.setToacct(dataItem.getCrcard());
		draw.setTranam(tranam);
		dpsvc.addDrawAcctDp(draw);
		
		//记账机构为省清算中心
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();
		acdr.setAcbrch(SysUtil.getRemoteInstance(IoSrvPbBranch.class).getCenterBranch(brchno).getBrchno());
		acdr.setBusino(para.getParm_value1());
		acdr.setSubsac(para.getParm_value2());//信用卡清算账户修改 modify by chenlk20161014
		acdr.setCrcycd(acct.getCrcycd());
		acdr.setSmrycd(BusinessConstants.SUMMARY_ZR); 
		acdr.setToacct(dataItem.getCardno());
		acdr.setToacna(acct.getAcctna());
		acdr.setTranam(tranam);
		acdr.setTrantp(cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP.TR);
		inout = insvc.ioInAccr(acdr);
		//		inout = insvc.ioInAcdr(acdr);
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(filetab.getAcctdt(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._09);
		
		dataItem.setIdtftp(E_IDTFTP.SFZ);
		dataItem.setIdtfno(dataItem.getCridno());
		dataItem.setTrandt(filetab.getAcctdt());
		dataItem.setRealam(tranam);
		dataItem.setDealms("成功完成");
		dataItem.setDealst(E_DEALST.SUC);
		dataItem.setRemark("成功完成");
		KnbCrcdBachDao.updateOne_odb1(dataItem);
		
		//返还原法人
//		CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
		
	}

	
	@Override
	public void jobExceptionProcess(String taskId, Input input,
			Property property, String jobId, KnbCrcdBach dataItem, Throwable t) {
		
		String dealms = t.getMessage().substring(t.getMessage().lastIndexOf("["));
		if(CommUtil.isNull(dealst)){
			dealst = E_DEALST.OTW;
		}
		
		dataItem.setTrandt(filetab.getAcctdt());
		dataItem.setRealam(BigDecimal.ZERO);
		dataItem.setDealst(dealst);
		dataItem.setDealms(dealms);
		dataItem.setRemark(dealms);
		KnbCrcdBachDao.updateOne_odb1(dataItem);
		
//		super.jobExceptionProcess(taskId, input, property, jobId, dataItem, t);
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
	public BatchDataWalker<String> getBatchDataWalker(
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batbil.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batbil.Property property) {
		
		log.debug("<<===================获取作业编号开始======================>>");
		List<String> ls = BilBatchDao.selDataidByFilesq(input.getFilesq(), filetab.getAcctdt(), true);
		return new ListBatchDataWalker<String>(ls);
	}

	/**
	 * 获取作业数据遍历器
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @param dataItem
	 *            批次数据项
	 * @return
	 */
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbCrcdBach> getJobBatchDataWalker(
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batbil.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Batbil.Property property,
			String dataItem) {

		log.debug("<<===================获取作业数据集合开始======================>>");
		Params param = new Params();
		param.put("filesq", input.getFilesq());
		param.put("trandt", filetab.getAcctdt());
		param.put("dataid", dataItem);
		log.debug("<<===================获取作业数据集合结束======================>>");
		return new CursorBatchDataWalker<KnbCrcdBach>(
				BilBatchDao.namedsql_selBatchDataByFilesq, param);

	}

	@Override
	public void afterTranProcess(String taskId, Input input, Property property) {
		
		log.debug("<<===================批量提现交易结束后修改状态======================>>");
//		filetab.setBtfest(E_BTFEST.BUSISUCC);
//		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		log.debug("<<===================批量提现交易结束后修改状态结束======================>>");
		super.afterTranProcess(taskId, input, property);
	}
}
