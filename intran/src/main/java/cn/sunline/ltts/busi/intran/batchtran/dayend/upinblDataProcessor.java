package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.in.type.InDayEndTypes.InacTranam;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Upinbl.Input;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Upinbl.Property;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;

/**
 * 内部户余额过账 余额更新方式为日终更新的内部户进行余额更新
 * 
 */

public class upinblDataProcessor
		extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Upinbl.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Upinbl.Property, cn.sunline.ltts.busi.in.type.InDayEndTypes.InacTranam> {

	private static final BizLog bizlog = BizLogUtil
			.getBizLog(upinblDataProcessor.class);
    
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
			cn.sunline.ltts.busi.in.type.InDayEndTypes.InacTranam dataItem,
			cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Upinbl.Input input,
			cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Upinbl.Property property) {

        // 20151130 wxq add 年结预处理期间暂不做交易过账
/*        E_YENDST eYendst = E_YENDST.NORMAL; //年结状态
        KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1(sParm_code, sPmkey1, "%", "%", false); //公共参数表
        if (CommUtil.isNull(tblKnpParameter)) {
            throw ApError.BusiAplt.E0000("年结状态公共参数未配置!");
        }
        eYendst = CommUtil.toEnum(E_YENDST.class, tblKnpParameter.getParm_value1()); //年结状态	    
        if (eYendst == E_YENDST.PRETRET) {
            return;
        }
	       */
		
		//String mtdate = DateTools2.getDateInfo().getSystdt();
		String timetm = DateTools2.getCurrentTimestamp();
		
	    //获取内部户信息
        InacTranam cplGlAcctInfo = InDayEndSqlsDao.selGlAcctInfoWithLock(dataItem.getAcctno(), true);
		bizlog.debug("upinbl 账号[" + dataItem.getAcctno() + "]过账开始。");
		bizlog.debug("上日余额日期：[" + cplGlAcctInfo.getLastdt() + "]");
		bizlog.debug("交易日期：[" + dataItem.getTrandt() + "]");
		if (CommUtil.compare(cplGlAcctInfo.getLastdt(), dataItem.getTrandt()) < 0) {
			
			BigDecimal new_drltbl = BigDecimal.ZERO;// 新的上日借方余额
			BigDecimal new_crltbl = BigDecimal.ZERO;// 新的上日贷方余额
			E_BLNCDN new_lastdn = null;

			// 检查表内外标志与记账方向的合法性
			switch (cplGlAcctInfo.getIoflag()) {
			case IN: {
				if (!(dataItem.getAmntcd() == E_AMNTCD.CR || dataItem
						.getAmntcd() == E_AMNTCD.DR))
					throw InError.comm.E0003("表内账号[" + dataItem.getAcctno()
							+ "]记账方向[" + dataItem.getAmntcd() + "]不合法。");
				break;
			}
			case OUT:
				if (!CommUtil.in(dataItem.getAmntcd(), E_AMNTCD.RV, E_AMNTCD.PY))
					throw InError.comm.E0003("表外账号[" + dataItem.getAcctno()
							+ "]记账方向[" + dataItem.getAmntcd() + "]不合法。");
				break;
			default:
				throw InError.comm.E0003("账号[" + dataItem.getAcctno()
						+ "]记账方向[" + dataItem.getAmntcd() + "]不合法。");
			}

			// 计算新上日余额及余额方向
			BigDecimal onlnbl = BigDecimal.ZERO;
			if (dataItem.getAmntcd() == E_AMNTCD.CR
					|| dataItem.getAmntcd() == E_AMNTCD.PY) {
				onlnbl = cplGlAcctInfo.getDrltbl().subtract(cplGlAcctInfo.getCrltbl())
						.subtract(dataItem.getTranam());
			} else {
				onlnbl = cplGlAcctInfo.getDrltbl().subtract(cplGlAcctInfo.getCrltbl())
						.add(dataItem.getTranam());
			}

			bizlog.debug("初次计算上日余额：[" + onlnbl.toString() + "]");

			// 计算余额方向及余额金额正负
			bizlog.debug("账号科目方向：[" + cplGlAcctInfo.getItmcdn() + "]");
			switch (cplGlAcctInfo.getItmcdn()) {
			case D:
				new_lastdn = E_BLNCDN.D;
				new_drltbl = onlnbl;
				new_crltbl = BigDecimal.ZERO;
				break;
			case C:
				new_lastdn = E_BLNCDN.C;
				new_crltbl = onlnbl.negate();
				new_drltbl = BigDecimal.ZERO;
				break;
			case R:
				new_lastdn = E_BLNCDN.R;
				new_drltbl = onlnbl;
				new_crltbl = BigDecimal.ZERO;
				break;
			case B:
				if (CommUtil.compare(onlnbl, BigDecimal.ZERO) > 0) {
					new_lastdn = E_BLNCDN.D;
					new_drltbl = onlnbl;
					new_crltbl = BigDecimal.ZERO;
				} else if (CommUtil.compare(onlnbl, BigDecimal.ZERO) == 0) {
					new_lastdn = cplGlAcctInfo.getLastdn();
					new_crltbl = BigDecimal.ZERO;
					new_drltbl = BigDecimal.ZERO;
				} else {
					new_lastdn = E_BLNCDN.C;
					new_crltbl = onlnbl.abs();
					new_drltbl = BigDecimal.ZERO;
				}
				break;
			case Z:
				if (CommUtil.compare(onlnbl, BigDecimal.ZERO) > 0) {
					new_lastdn = E_BLNCDN.D;
					new_drltbl = onlnbl;
					new_crltbl = BigDecimal.ZERO;
				} else if (CommUtil.compare(onlnbl, BigDecimal.ZERO) == 0) {
					new_lastdn = cplGlAcctInfo.getLastdn();
					new_crltbl = BigDecimal.ZERO;
					new_drltbl = BigDecimal.ZERO;
				} else {
					new_lastdn = E_BLNCDN.C;
					new_crltbl = onlnbl.abs();
					new_drltbl = BigDecimal.ZERO;
				}
				break;
			default:
				break;
			}
			bizlog.debug("账号余额上日余额方向[" + new_lastdn + "]");
			bizlog.debug("根据余额方向转化后，上日借/收方余额：[" + new_drltbl + "]");
			bizlog.debug("根据余额方向转化后，上日贷/付方余额：[" + new_crltbl + "]");

			// 更新上日余额表
			bizlog.debug("更新上日余额表。");
			int rowNum = InDayEndSqlsDao.upGlKnaLsbl(dataItem.getAcctno(),
			        cplGlAcctInfo.getDrltbl(), cplGlAcctInfo.getCrltbl(),
			        cplGlAcctInfo.getLastdn(), dataItem.getTrandt(), new_lastdn,
					new_drltbl, new_crltbl, CommTools.getBaseRunEnvs().getTrxn_date(), timetm);
			if (rowNum != 1)
				throw InError.comm.E0003("更新账号[" + dataItem.getAcctno()
						+ "]上日余额异常，更新条数[" + rowNum + "]");

/*			BigDecimal edctbl = cplGlAcctInfo.getDredbl().subtract(
			        cplGlAcctInfo.getCredbl());// 日终借方余额

			// 检查与edctbl一致性
			bizlog.debug("账号[" + dataItem.getAcctno() + "]检查与edctbl一致性");
			bizlog.debug("当日借方余额onlnbl =[" + onlnbl + "]");
			bizlog.debug("当日借方日终余额edctbl =[" + edctbl + "]");
			if (!CommUtil.equals(onlnbl, edctbl)) {
				throw InError.comm.E0003("账号[" + dataItem.getAcctno()
						+ "]当日余额错误。当日借方日终余额[" + edctbl + "]!=当日借方余额[" + onlnbl
						+ "]");
			}*/

			BigDecimal onctbl = cplGlAcctInfo.getDrctbl().subtract(
			        cplGlAcctInfo.getCrctbl());// 日终借方余额
			// 用于日终账户过账,检查是否有次日帐务发生,计算轧账期间发生额
			if (!CommUtil.equals(onlnbl, onctbl)) {
				BigDecimal nextTranam = InDayEndSqlsDao.selAcctTranam(
						dataItem.getTrandt(), dataItem.getAcctno(), false);// 日切后发生额
				if (!CommUtil.equals(onlnbl.add(nextTranam), onctbl)) {
					throw InError.comm.E0003("账号[" + dataItem.getAcctno()
							+ "]联机余额错误。当日借方联机余额[" + onctbl + "]!=上日借方余额["
							+ onlnbl + "]+日切后发生额[" + nextTranam + "]");
				}
			}

		}
		bizlog.debug("upinbl 账号[" + dataItem.getAcctno() + "]过账结束。");
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
	public BatchDataWalker<cn.sunline.ltts.busi.in.type.InDayEndTypes.InacTranam> getBatchDataWalker(
			cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Upinbl.Input input,
			cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Upinbl.Property property) {
		Params params = SysUtil.getInstance(Params.class);
		bizlog.info("corpno>>>>>>>>>>>>>>>>>>>>>[%s]", CommTools.getBaseRunEnvs().getBusi_org_id());
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		
		return new CursorBatchDataWalker<InacTranam>(
				InDayEndSqlsDao.namedsql_selAcctDatas, params);
	}

	@Override
	public void beforeTranProcess(String taskId, Input input, Property property) {
		String trandt = DateTools2.getDateInfo().getLastdt();
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		
		// 准备待过账数据
		InDayEndSqlsDao.delGlktpGlvc(corpno);
		InDayEndSqlsDao.insGlKtpGlvc(trandt, corpno);
	}

}
