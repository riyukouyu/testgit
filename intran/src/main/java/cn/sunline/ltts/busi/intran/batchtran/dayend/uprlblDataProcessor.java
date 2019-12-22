package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcctDao;
import cn.sunline.ltts.busi.in.type.InDayEndTypes.InacRlbl;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INACST;

/**
 * 非实时余额入账 非实时余额入账
 * 
 */

public class uprlblDataProcessor
		extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Uprlbl.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Uprlbl.Property, cn.sunline.ltts.busi.in.type.InDayEndTypes.InacRlbl> {

	private static final BizLog bizlog = BizLogUtil
			.getBizLog(uprlblDataProcessor.class);

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
			cn.sunline.ltts.busi.in.type.InDayEndTypes.InacRlbl dataItem,
			cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Uprlbl.Input input,
			cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Uprlbl.Property property) {
			
			String oldCorpno = BusiTools.getBusiRunEnvs().getSpcono();
//			String mtdate = DateTools2.getDateInfo().getSystdt();
			String tmstmp = DateTools2.getCurrentTimestamp();
			
			BigDecimal tranam = dataItem.getTranam();
			// 金额为零不处理
			if (CommUtil.compare(tranam, BigDecimal.ZERO) == 0) {
				return;
			}
			// 修改余额
			String acctno = dataItem.getAcctno();
			GlKnaAcct tblAcct = InQuerySqlsDao.sel_GlKnaAcct_by_acct(acctno, true);
			BusiTools.getBusiRunEnvs().setSpcono(tblAcct.getCorpno()); //交易前设置法人		
					
					
			E_AMNTCD amntcd = dataItem.getAmntcd();
			String trandt = DateTools2.getDateInfo().getLastdt(); // 取上日日期
			String transq = null;
			E_YES___ pmodck = E_YES___.YES;
			E_YES___ edblfg = E_YES___.YES;
			accting(acctno, amntcd, tranam, trandt, transq, pmodck, edblfg);
			// 更新非实时入账标志为已入账
			bizlog.debug(">>>>>>>>>>>>修改状态", "");
			int rowcount = InDayEndSqlsDao.upGlKnaGlvc(acctno, trandt, tmstmp);
			
			//检查记录数是否相同
			if( rowcount != dataItem.getRecord()) {
				throw InError.comm.E0003("更新表非实时余额记录数不一致["+rowcount+"]");
			}
			BusiTools.getBusiRunEnvs().setSpcono(oldCorpno);
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
	public BatchDataWalker<cn.sunline.ltts.busi.in.type.InDayEndTypes.InacRlbl> getBatchDataWalker(
			cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Uprlbl.Input input,
			cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Uprlbl.Property property) {
		Params params = SysUtil.getInstance(Params.class);
		bizlog.info("trandt>>>>>>>>>>>>>>>>>>>>>[%s]", CommTools.getBaseRunEnvs().getTrxn_date());
		bizlog.info("corpno>>>>>>>>>>>>>>>>>>>>>[%s]", CommTools.getBaseRunEnvs().getBusi_org_id());
		params.add("trandt", DateTools2.getDateInfo().getLastdt());
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		bizlog.info(">>>>>>>>>>>>>>>>>>[%s]",  params);
		bizlog.info(">>>>>>>>>>>>>>>>>>[%s]", DateTools2.getDateInfo().getLastdt());
		return new CursorBatchDataWalker<InacRlbl>(
				InDayEndSqlsDao.namedsql_selRlblList, params);
	}

    @Override
    public void afterTranProcess(String taskId, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Uprlbl.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Uprlbl.Property property) {
        
        String trandt = DateTools2.getDateInfo().getLastdt(); // 取上日日期
        // 删除非实时余额发生汇总明细数据
        InDayEndSqlsDao.delGlKnsRlblByTrandt(trandt, CommTools.getBaseRunEnvs().getBusi_org_id());
        
    }	
	
	/**
	 * 非实时余额入账记账
	 * 
	 * @Author wanggl
	 *         <p>
	 *         <li>2015年7月27日-上午10:19:36</li>
	 *         <li>功能说明</li>
	 *         </p>
	 * @param acctno
	 * @param amntcd
	 * @param tranam
	 * @param trandt
	 * @param transq
	 * @param pmodck
	 * @param edblfg
	 * 
	 */
	private void accting(String acctno, E_AMNTCD amntcd, BigDecimal tranam,
			String trandt, String transq, E_YES___ pmodck, E_YES___ edblfg) {
		BigDecimal zero = BigDecimal.valueOf(0.00);
		BigDecimal tranbl = zero;
		BigDecimal l_drctbl = zero;
		BigDecimal l_crctbl = zero;
		BigDecimal l_dredbl = zero;
		BigDecimal l_credbl = zero;
		E_BLNCDN l_blncdn;
		BigDecimal onlnbl = zero; // 联机余额
		BigDecimal edctbl = zero; // 日终余额

		String timetm = DateTools2.getCurrentTimestamp();	
		
		// 1、查询账户状态
		GlKnaAcct acct =  GlKnaAcctDao.selectOneWithLock_odb1(acctno, false);
		if (CommUtil.isNull(acct)) {
			throw InError.comm.E0003("账户[" + acctno + "]不存在");
		}

		E_YES___ ovdftg = acct.getPmodtg(); // 账户透支标志
		E_BLNCDN itmcdn = acct.getBusidn(); // 科目方向
		if (acct.getAcctst() == E_INACST.CLOSED) {
			throw InError.comm.E0003("账户[" + acctno + "]状态已经关闭");
		}
		//计算联机余额和日终余额
		if (itmcdn == E_BLNCDN.D || itmcdn == E_BLNCDN.R) {
			onlnbl = acct.getDrctbl().subtract(acct.getCrctbl());
			edctbl = acct.getDredbl().subtract(acct.getCredbl());
		} else {
			onlnbl = acct.getCrctbl().subtract(acct.getDrctbl());
			edctbl = acct.getCredbl().subtract(acct.getDredbl());
		}

		if (itmcdn == E_BLNCDN.C || itmcdn == E_BLNCDN.P) { // 账户科目方向为贷方、付方处理
			// 确定余额方向
			l_blncdn = itmcdn;
			// 确定借贷方联机余额
			l_drctbl = zero;
			if (amntcd == E_AMNTCD.CR || amntcd == E_AMNTCD.PY) {
				l_crctbl = onlnbl.add(tranam);
				tranbl = l_crctbl;
			} else {
				l_crctbl = onlnbl.subtract(tranam);
				tranbl = l_crctbl;
			}
			// 确定借贷方日终余额
			l_dredbl = zero;
			if (amntcd == E_AMNTCD.CR || amntcd == E_AMNTCD.PY) {
				l_credbl = edctbl.add(tranam);
			} else {
				l_credbl = edctbl.subtract(tranam);
			}
		} else if (itmcdn == E_BLNCDN.D || itmcdn == E_BLNCDN.R) { // 账户科目方向为借方、收方处理
			// 确定余额方向
			l_blncdn = itmcdn;
			// 确定借贷方联机余额
			l_crctbl = zero;
			if (amntcd == E_AMNTCD.DR || amntcd == E_AMNTCD.RV) {
				l_drctbl = onlnbl.add(tranam);
				tranbl = l_drctbl;
			} else {
				l_drctbl = onlnbl.subtract(tranam);
				tranbl = l_drctbl;
			}
			// 确定借贷方日终余额
			l_credbl = zero;
			if (amntcd == E_AMNTCD.DR || amntcd == E_AMNTCD.RV) {
				l_dredbl = edctbl.add(tranam);
			} else {
				l_dredbl = edctbl.subtract(tranam);
			}
		} else if (itmcdn == E_BLNCDN.B || itmcdn == E_BLNCDN.Z) { // 账户科目方向为轧差处理

			if (amntcd == E_AMNTCD.DR || amntcd == E_AMNTCD.RV) { // 交易方向为借方、收方相同处理
				// 轧差确定联机余额和余额方向
				if ((onlnbl.subtract(tranam).compareTo(zero) > 0)) {
					l_crctbl = onlnbl.subtract(tranam);
					l_blncdn = E_BLNCDN.C;
					l_drctbl = zero;
					tranbl = l_crctbl;
				} else {
					l_drctbl = tranam.subtract(onlnbl);
					l_blncdn = E_BLNCDN.D;
					l_crctbl = zero;
					tranbl = l_drctbl;
				}
				// 轧差确定日终余额
				if ((edctbl.subtract(tranam).compareTo(zero) > 0)) {
					l_credbl = edctbl.subtract(tranam);
					l_dredbl = zero;
				} else {
					l_dredbl = tranam.subtract(edctbl);
					l_credbl = zero;
				}
			} else { // 交易方向为贷方、付方相同处理
				// 轧差确定联机余额和余额方向
				if ((onlnbl.add(tranam).compareTo(zero) > 0)) {
					l_crctbl = onlnbl.add(tranam);
					l_blncdn = E_BLNCDN.C;
					l_drctbl = zero;
					tranbl = l_crctbl;
				} else {
					l_drctbl = onlnbl.add(tranam);
					l_drctbl = zero.subtract(l_drctbl);
					l_blncdn = E_BLNCDN.D;
					l_crctbl = zero;
					tranbl = l_drctbl;
				}
				// 轧差确定日终余额
				if ((edctbl.add(tranam).compareTo(zero) > 0)) {
					l_credbl = edctbl.add(tranam);
					l_dredbl = zero;
				} else {
					l_dredbl = edctbl.add(tranam);
					l_dredbl = zero.subtract(l_dredbl);
					l_credbl = zero;
				}
			}

		} else {
			throw InError.comm.E0003("账户科目余额方向错误");

		}
		// 透支检查
		if (ovdftg == E_YES___.NO
				&& (itmcdn == E_BLNCDN.D || itmcdn == E_BLNCDN.C || itmcdn == E_BLNCDN.R)) {
			if (CommUtil.compare(tranbl, zero) < 0) {
				throw InError.comm.E0003("账户[" + acctno + "]可用余额不足");
			}
		}

		// 更新余额
		try {
			int count = InacSqlsDao.updateGlAcctBalance(acctno, l_blncdn,
					l_crctbl, l_drctbl, l_credbl, l_dredbl, trandt, transq ,timetm);
			if (count != 1) {
				throw InError.comm.E0003("更新账号余额错误[" + acctno + "]");
			}
		} catch (Exception e) {
			throw InError.comm.E0003("更新账号余额错误[" + acctno + "]");
		}

	}
	
}
