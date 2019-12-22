package cn.sunline.ltts.busi.dp.jfaccounting;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.clwj.msap.biz.servicetype.MsReversalService;
import cn.sunline.clwj.msap.biz.tables.MsOnlTable.MslEvent;
import cn.sunline.clwj.msap.biz.tables.MsOnlTable.MslEventDao;
import cn.sunline.clwj.msap.biz.tables.MsOnlTable.MssTransaction;
import cn.sunline.clwj.msap.biz.transaction.MsEventControl;
import cn.sunline.clwj.msap.biz.transaction.MsTrxn;
import cn.sunline.clwj.msap.core.errors.MsCoreError;
import cn.sunline.clwj.msap.core.parameter.MsChannel;
import cn.sunline.clwj.msap.core.parameter.MsGlobalParm;
import cn.sunline.clwj.msap.core.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.type.MsCoreComplexType.MsChannelInfo;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsInterface;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsReversal;
import cn.sunline.clwj.msap.sys.dict.MsDict;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_EVNTST;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_REDBLUEWORDIND;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_REVERSALTYPE;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_REVERSAL_RESULT;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_TRXNSTATUS;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_YESORNO;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpAccountStrkeInput;
import cn.sunline.edsp.busi.dp.type.ca.DpAccounting.DpCommAcctNormInput;
import cn.sunline.edsp.microcore.spi.ExtensionLoader;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqRgst;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqRgstDao;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * 即富记账交易冲正处理
 * @author sunline37
 *
 */
public class JfDpAccountStrk {

	private final static BizLog bizlog = BizLogUtil.getBizLog(JfDpAccountStrk.class);
	/**
	 * 即富记账冲正处理
	 * @param msReversalInput
	 */
	public static void process(DpAccountStrkeInput dpAccountStrkeInput) {
		
		
		bizlog.debug(String.format("原交易流水:[]", dpAccountStrkeInput.getOrtrsq()));
		bizlog.debug(String.format("原交易日期:[]", dpAccountStrkeInput.getOrtrdt()));
		
		IoMsReversal msReversalInput = SysUtil.getInstance(IoMsReversal.class);
		
		msReversalInput.setOrig_initiator_seq(dpAccountStrkeInput.getOrtrsq());
		msReversalInput.setReversal_type(E_REVERSALTYPE.BUSINESS);
		msReversalInput.setChrg_reversal_ind(E_YESORNO.YES);
		
		KnsAcsqRgst tblKnsAcsqRgst = JfDpAccountingPublic.chckOrtrsq(msReversalInput.getOrig_initiator_seq(), dpAccountStrkeInput.getOrtrdt());
		
//		MsReversalService msReversalServ = SysUtil.getInstance(MsReversalService.class);
//		E_REVERSAL_RESULT result = msReversalServ.reversal(msReversalInput);
		
		E_REVERSAL_RESULT result = processTran(msReversalInput);
		
//		if (result == E_REVERSAL_RESULT.SUCCEED) {
//			tblKnsAcsqRgst.setTranst(E_TRANST.STRIKED);
//			tblKnsAcsqRgst.setStrksq(CommTools.getBaseRunEnvs().getTrxn_seq());
//			tblKnsAcsqRgst.setStrkdt(CommTools.getBaseRunEnvs().getTrxn_date());
//			tblKnsAcsqRgst.setStrktm(CommTools.getBaseRunEnvs().getComputer_time());
//			
//			updOrtrsq(tblKnsAcsqRgst);
//		}
		
		insStrksq(tblKnsAcsqRgst, dpAccountStrkeInput);
		
		// 平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(),
				CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._35);
		
	}
	
	private static E_REVERSAL_RESULT processTran(IoMsReversal input) {
		String origInitSeq = input.getOrig_initiator_seq();

		CommTools.fieldNotNull(origInitSeq, MsDict.Comm.orig_initiator_seq.getId(),
				MsDict.Comm.orig_initiator_seq.getLongName());

		IoMsInterface ioInterface = SysUtil.getInstance(IoMsInterface.class);

		// 红蓝字冲账标识
		ioInterface.setReversal_accounting_type(getReversalAccMode());
		// 默认都冲回去
		ioInterface.setChrg_reversal_ind(CommUtil.nvl(input.getChrg_reversal_ind(), E_YESORNO.YES));
		ioInterface.setVoch_reversal_ind(CommUtil.nvl(input.getVoch_reversal_ind(), E_YESORNO.YES));
		ioInterface.setUnfroze_reversal_ind(CommUtil.nvl(input.getUnfroze_reversal_ind(), E_YESORNO.YES));
		ioInterface.setReversal_type(CommUtil.nvl(input.getReversal_type(), E_REVERSALTYPE.BUSINESS));
		ioInterface.setOrig_initiator_seq(origInitSeq);

		RunEnvsComm envs = CommTools.getBaseRunEnvs();
		
		//检查冲正事件表是否有待冲正事件
		
		// 反向处理业务数据,按data_sort从大到小排序
		List<MslEvent> eventInfo = MslEventDao.selectAll_odb3(input.getOrig_initiator_seq(), false);

		//if (eventInfo.size() == 0 && CommTools.isFlowTran()) // 20190408 外调失败，登记了外调事件，但是被调方没有登记金融事件。
		if (CommUtil.isNull(eventInfo) || eventInfo.size() == 0)
			throw MsCoreError.Comm.E0094(input.getOrig_initiator_seq());
		
		for (MslEvent event : eventInfo) {
			if (event.getEvent_status() == E_EVNTST.STRIKED) {
				throw MsCoreError.Comm.E0094(input.getOrig_initiator_seq());
			}
		}
		

/*		// 渠道一致性检查
		MsChannelInfo channelInfo = MsChannel.getChannel(envs.getChannel_id());
		if (channelInfo.getCross_reversal_ind() == E_YESORNO.NO) {
			if (CommUtil.compare(orgTrxn.getChannel_id(), channelInfo.getChannel_id()) != 0) {
				// throw 原交易渠道[%s]不允许跨渠道冲账
				throw MsCoreError.Comm.E0069(orgTrxn.getChannel_id());
			}
		} else {
			envs.setChannel_id(orgTrxn.getChannel_id());
		}

		// 柜面校验
		if (MsChannel.isCounter(envs.getChannel_id())) {
			// 判断是否同机构
			if (CommUtil.compare(orgTrxn.getTrxn_branch(), envs.getTrxn_branch()) != 0) {
				throw MsCoreError.Comm.E0052();
			}
			// 判断是否同柜员
			if (CommUtil.compare(orgTrxn.getTrxn_teller(), envs.getTrxn_teller()) != 0) {
				throw MsCoreError.Comm.E0053();
			}
		}*/
		
		
		
		bizlog.debug("原调用流水[%s]待冲正事件数[%s]", input.getOrig_initiator_seq(), eventInfo.size());
		for (MslEvent event : eventInfo) {
			try {
				processEvent(event, ioInterface);
			} catch (InvocationTargetException e) {
				bizlog.error("transasction reverse faild! [%s]", e, e.getMessage());
				throw MsCoreError.Comm.E0016(input.getOrig_initiator_seq(), event.getReversal_event_id(),
						e.getTargetException().getMessage(), e.getTargetException());
			} catch (Exception ee) {
				throw MsCoreError.Comm.E0016(input.getOrig_initiator_seq(), event.getReversal_event_id(),
						ee.getMessage(), ee);
			}

		}

		return E_REVERSAL_RESULT.SUCCEED;
	}
	
		
	// 处理单个冲正事件
	private static void processEvent(MslEvent event, IoMsInterface input) throws Exception {

		bizlog.debug("Process Start Reversal, Current Event id = " + event.getReversal_event_id());

		// 部分冲账需求: 对手续费双方处理的事件做略过
		if (event.getChrg_context_ind() == E_YESORNO.YES && input.getChrg_reversal_ind() == E_YESORNO.NO) {
			bizlog.debug("Fees context, no need to reversal. [%s]-[%s]-[%s]", event.getTrxn_seq(), event.getData_sort(),
					event.getCall_out_seq());
			return;
		}

		input.setReversal_event_id(event.getReversal_event_id());
		input.setTxn_event_level(event.getTxn_event_level());
		input.setCall_out_seq(event.getCall_out_seq());
		input.setEvent_status(event.getEvent_status());
		input.setIs_across_dcn(event.getIs_across_dcn());
		input.setService_id(event.getService_id());
		input.setSub_system_id(event.getSub_system_id());
		input.setTarget_org_id(event.getTarget_org_id());
		input.setTarget_dcn(event.getTarget_dcn());
		input.setInformation_value(event.getInformation_value());

		// 调用spi实现进行冲正
		MsEventControl mec = ExtensionLoader.getExtensionLoader(MsEventControl.class).getExtensionById(event.getReversal_event_id());
		mec.doReversalProcess(input);

		// 变更事件为已冲正状态

		event.setEvent_status(E_EVNTST.STRIKED);
		MslEventDao.updateOne_odb1(event);

	}
	
	/**
	 * 冲正记账模式
	 * 
	 * @return
	 */
	public static E_REDBLUEWORDIND getReversalAccMode() {
		String modeValue = MsGlobalParm.getValue("REVERSAL_ACCOUNTING_TYPE");

		return E_REDBLUEWORDIND.get(modeValue);
	}
	
	/**
	 * 登记该笔冲正交易流水
	 * @param tblKnsAcsqRgst
	 */
	private static void insStrksq(KnsAcsqRgst tblKnsAcsqRgst, DpAccountStrkeInput dpAccountStrkeInput) {
		DpCommAcctNormInput dpCommAcctNormInput = SysUtil.getInstance(DpCommAcctNormInput.class);
		
		
		dpCommAcctNormInput.setIschck(E_YES___.NO);
		dpCommAcctNormInput.setChantp(tblKnsAcsqRgst.getChantp());
		dpCommAcctNormInput.setCktrdt(dpAccountStrkeInput.getCktrdt());
		dpCommAcctNormInput.setCltrdt(CommUtil.nvl(dpAccountStrkeInput.getCltrdt(), CommTools.getBaseRunEnvs().getTrxn_date()));
		dpCommAcctNormInput.setCltrsq(CommUtil.nvl(dpAccountStrkeInput.getCltrsq(), CommTools.getBaseRunEnvs().getTrxn_seq()));
		dpCommAcctNormInput.setCrcycd(tblKnsAcsqRgst.getCrcycd());
		dpCommAcctNormInput.setDivdam(tblKnsAcsqRgst.getDivdam());
		dpCommAcctNormInput.setDivdfe(tblKnsAcsqRgst.getDivdfe());
		dpCommAcctNormInput.setInmeid(tblKnsAcsqRgst.getInmeid());
		dpCommAcctNormInput.setJftrtp(dpAccountStrkeInput.getJftrtp());
		dpCommAcctNormInput.setMarkfe(tblKnsAcsqRgst.getMarkfe());
		dpCommAcctNormInput.setOtbkna(tblKnsAcsqRgst.getOtbkna());
		dpCommAcctNormInput.setPabkno(tblKnsAcsqRgst.getPabkno());
		dpCommAcctNormInput.setPamena(tblKnsAcsqRgst.getPamena());
		dpCommAcctNormInput.setPameno(tblKnsAcsqRgst.getPameno());
		dpCommAcctNormInput.setPosnum(tblKnsAcsqRgst.getPosnum());
		dpCommAcctNormInput.setRemark(tblKnsAcsqRgst.getRemark());
		dpCommAcctNormInput.setSvcode(tblKnsAcsqRgst.getSvcode());
		dpCommAcctNormInput.setTranam(tblKnsAcsqRgst.getTranam());
		dpCommAcctNormInput.setUnamfe(tblKnsAcsqRgst.getUnamfe());
		dpCommAcctNormInput.setVoucfe(tblKnsAcsqRgst.getVoucfe());
		dpCommAcctNormInput.setYfmcam(tblKnsAcsqRgst.getYfmcam());
		dpCommAcctNormInput.setYsunam(tblKnsAcsqRgst.getYsunam());
		
		JfDpAccountingPublic.insertAcsqRgstStrke(dpCommAcctNormInput, E_YES___.NO);
	}
	
	
}
