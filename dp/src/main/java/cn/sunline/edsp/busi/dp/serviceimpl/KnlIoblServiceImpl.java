package cn.sunline.edsp.busi.dp.serviceimpl;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.servicetype.KnlIoblService.queryKnlIobl.Input;
import cn.sunline.edsp.busi.dp.servicetype.KnlIoblService.queryKnlIobl.Output;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUPSST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_EDMTYP;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_FINSTY;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpKnlIoblSqlDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsRe;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsReDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.KnlIoblStateInfo;
import cn.sunline.ltts.busi.dp.type.DpTransfer.DpKnlIobl;
import cn.sunline.ltts.busi.sys.dict.InDict.inac;
import oracle.net.aso.l;

/**
 * 清结算平账详情查询
 *
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "KnlIoblServiceImpl", longname = "清结算平账详情查询", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class KnlIoblServiceImpl implements cn.sunline.edsp.busi.dp.servicetype.KnlIoblService {
	/**
	 * 清结算平账详情
	 *
	 */
	/*
	 * public cn.sunline.edsp.busi.dp.servicetype.KnlIoblService.queryKnlIobl.Output
	 * queryKnlIobl(final
	 * cn.sunline.edsp.busi.dp.servicetype.KnlIoblService.queryKnlIobl.Input input){
	 * DpKnlIobl iobl=DpKnlIoblSqlDao.queryKnlIobl(CommTools.getBusiOrgId(),
	 * input.getMntrsq(), false); // out }
	 */

	@Override
	public void queryKnlIobl(Input input, Output output) {
		// TODO Auto-generated method stub
		DpKnlIobl iobl = DpKnlIoblSqlDao.queryKnlIobl(CommTools.getBusiOrgId(), input.getMntrsq(), false);
		output.setIobl(iobl);

	}

	/**
	 * 查询代发状态
	 */
	public void selKnlIoblStateInfo(
			final cn.sunline.edsp.busi.dp.servicetype.KnlIoblService.selKnlIoblStateInfo.Input input,
			final cn.sunline.edsp.busi.dp.servicetype.KnlIoblService.selKnlIoblStateInfo.Output output) {
		String sbrand = input.getSbrand();
		String inmeid = input.getInmeid();
		String refeno = input.getRefeno();
		String prepsq = input.getPrepsq();
		String sacdno = input.getSacdno();
		String sacdna = input.getSacdna();
		E_EDMTYP servtp = input.getServtp();
		E_CUPSST transt = input.getTranst();
		String stadat = input.getStadat();
		String enddat = input.getEnddat();
		//by huang.shoutao 2019-12-06 新增查询条件【订单号】【结算方式】
		String ordeno = input.getOrdeno();
		E_FINSTY finsty = input.getFinsty();

		if (CommUtil.isNull(stadat)) {
			if (CommUtil.isNotNull(enddat)) {
				throw DpModuleError.DpTrans.TS020057();
			}
		} else {
			if (CommUtil.isNull(enddat)) {
				throw DpModuleError.DpTrans.TS020057();
			} else if (CommUtil.compare(stadat, enddat) > 0) {
				throw DpModuleError.DpTrans.TS020057();
			}
		}

		RunEnvsComm runEnves = CommTools.getBaseRunEnvs();
		long start = (runEnves.getPage_start() - 1) * runEnves.getPage_size();
		long count = runEnves.getPage_size();
		long totlCount = runEnves.getTotal_count();

		Page<KnlIoblStateInfo> pageInfo = DpAcctDao.selKnlIoblStateInfo(sbrand, inmeid, refeno, prepsq, sacdno, sacdna,
				servtp, transt, stadat, enddat,ordeno,finsty, start, count, totlCount, false);

		BigDecimal rate0 = new BigDecimal("0");
		BigDecimal rate4 = new BigDecimal("0");
		BigDecimal rate5 = new BigDecimal("0");
		String merate0;
		String merate4;
		String merate5;
		for (KnlIoblStateInfo listParam : pageInfo.getRecords()) {
			// 获取【客户费率】+【客户秒到费率】
			String merate = listParam.getMerate();
			if (CommUtil.isNotNull(merate)) {
				String[] merates = merate.split("\\|");
				merate0 = merates[0];
				merate4 = merates[4];
				merate5 = merates[5];
				if (CommUtil.isNotNull(merate0) && !"null".equals(merate0)) {
					rate0 = new BigDecimal(merate0);
				}
				if (CommUtil.isNotNull(merate4) && !"null".equals(merate4)) {
					rate4 = new BigDecimal(merate4);
				}
				if (CommUtil.isNotNull(merate5) && !"null".equals(merate5)) {
					rate5 = new BigDecimal(merate5);
				}
				listParam.setInflrt(rate0.add(rate4));
				listParam.setScflpo(rate5);

			}

		}

		output.getDfstateList().setValues(pageInfo.getRecords());
		// 设置报文头总记录条数
		CommTools.getBaseRunEnvs().setTotal_count(pageInfo.getRecordCount());

	}

	public void geterrorthrow(final cn.sunline.edsp.busi.dp.servicetype.KnlIoblService.geterrorthrow.Input input) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			@Override
			public Void execute() {
				KnlIoblCups knlIoblCups = KnlIoblCupsDao.selectOne_odb3(input.getFlowid(), false);
				if (CommUtil.isNotNull(knlIoblCups)) {
					if (CommUtil.equals(E_CUPSST.FAIL.getValue(), knlIoblCups.getTranst().getValue())
							|| CommUtil.equals(E_CUPSST.PRC.getValue(), knlIoblCups.getTranst().getValue())) {
						Throwable tranException = EngineContext.getTranException();
						knlIoblCups.setRemark(tranException.toString());
						knlIoblCups.setTranst(E_CUPSST.FAIL);
						KnlIoblCupsDao.updateOne_odb3(knlIoblCups);
					}
				} else {
					KnlIoblCups entity = SysUtil.getInstance(KnlIoblCups.class);
					Throwable tranException = EngineContext.getTranException();
					entity.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 主交易流水
					entity.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
					CommUtil.copyProperties(entity, input);
					entity.setRemark(tranException.toString());
					entity.setTranst(E_CUPSST.FAIL);
					KnlIoblCupsDao.insert(entity);
				}
				return null;

			}
		});
	}

	@Override
	public void insertErrorKnlIoblCupsRe(
			cn.sunline.edsp.busi.dp.servicetype.KnlIoblService.insertErrorKnlIoblCupsRe.Input input) {
		
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			@Override
			public Void execute() {
				KnlIoblCupsRe knlIoblCupsRe = KnlIoblCupsReDao.selectOne_odb01(input.getFlowid(), false);
				if (CommUtil.isNotNull(knlIoblCupsRe)) {
					if (CommUtil.equals(E_CUPSST.FAIL.getValue(), knlIoblCupsRe.getTranst().getValue())
							|| CommUtil.equals(E_CUPSST.PRC.getValue(), knlIoblCupsRe.getTranst().getValue())) {
						Throwable tranException = EngineContext.getTranException();
						knlIoblCupsRe.setRemark(tranException.toString());
						knlIoblCupsRe.setTranst(E_CUPSST.FAIL);
						KnlIoblCupsReDao.updateOne_odb01(knlIoblCupsRe);
					}
				} else {
					KnlIoblCupsRe entity = SysUtil.getInstance(KnlIoblCupsRe.class);
					Throwable tranException = EngineContext.getTranException();
					entity.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 主交易流水
					entity.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
					CommUtil.copyProperties(entity, input);
					entity.setRemark(tranException.toString());
					entity.setTranst(E_CUPSST.FAIL);
					KnlIoblCupsReDao.insert(entity);
				}
				return null;
				
			}
		});
	}

}
