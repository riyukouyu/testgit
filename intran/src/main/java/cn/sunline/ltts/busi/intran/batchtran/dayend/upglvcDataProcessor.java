package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnsGlvc;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
	 /**
	  * 日切更新传票流水表余额
	  * @author wuzhixiang
	  * @data March 9 16:00pm
	  */

public class upglvcDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.intran.batchtran.intf.Upglvc.Input, cn.sunline.ltts.busi.intran.batchtran.intf.Upglvc.Property, cn.sunline.ltts.busi.in.tables.In.GlKnaLsbl> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(upclrbDataProcessor.class);
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
		public void process(String jobId, int index, final cn.sunline.ltts.busi.in.tables.In.GlKnaLsbl dataItem, cn.sunline.ltts.busi.intran.batchtran.intf.Upglvc.Input input, cn.sunline.ltts.busi.intran.batchtran.intf.Upglvc.Property property) {
			String acctno = dataItem.getAcctno();
			String lastdt = CommTools.getBaseRunEnvs().getLast_date();
			 //根据账号和日期查询内部户传票流水表，更新记录
			bizlog.debug("*********************获取需要更新的传票记录*********************");
			final BigDecimal tranbl_f ;
			if (dataItem.getLastdn() == E_BLNCDN.D || dataItem.getLastdn() == E_BLNCDN.R ||dataItem.getLastdn() == E_BLNCDN.Z) {
				tranbl_f = dataItem.getDrltbl();
			} else{
				tranbl_f = dataItem.getCrltbl();
			} 
			String sql="";
			final Params params = new Params();
			params.add("acctno", acctno);
			params.add("trandt", lastdt);
			params.add("tranbl_l", tranbl_f);
			
			sql = InQuerySqlsDao.namedsql_selGlknsglvcByAcctno;
			
			DaoUtil.selectList(sql, params, new CursorHandler<GlKnsGlvc>(){
					@Override
					public boolean handle(int index, GlKnsGlvc glvc) {
						bizlog.debug("第"+index+"条数据余额为:" +glvc.getTranbl());
						BigDecimal tranbl =BigDecimal.ZERO;
						BigDecimal l_tranbl =BigDecimal.ZERO;
						BigDecimal tranbl_l = (BigDecimal) params.get("tranbl_l");
						tranbl = tranbl_l;
						E_BLNCDN l_blncdn = dataItem.getLastdn();
							if (l_blncdn == E_BLNCDN.D) {
								l_tranbl = tranbl;

								if (glvc.getBlncdn() == E_BLNCDN.C) {// 余额方向 为贷方处理
									if (glvc.getAmntcd() == E_AMNTCD.CR
											|| glvc.getAmntcd() == E_AMNTCD.PY) {
										tranbl = tranbl.subtract(glvc.getTranam());
									} else {
										tranbl = tranbl.add(glvc.getTranam());
									}
								} else if (glvc.getBlncdn() == E_BLNCDN.D) {// 余额方向 为借方处理
									if (glvc.getAmntcd() == E_AMNTCD.DR
											|| glvc.getAmntcd() == E_AMNTCD.RV) {
										tranbl = tranbl.subtract(glvc.getTranam());
									} else {
										tranbl = tranbl.add(glvc.getTranam());
									}
								} else if (glvc.getBlncdn() == E_BLNCDN.Z) {// 余额方向为扎差类
									if (glvc.getAmntcd() == E_AMNTCD.CR
											|| glvc.getAmntcd() == E_AMNTCD.PY) {
										if ((tranbl.subtract(glvc.getTranam()).compareTo(
												BigDecimal.ZERO) < 0)) {
											tranbl = glvc.getTranam().add(tranbl);
										} else {
											tranbl = tranbl.add(glvc.getTranam());
										}
									} else {
										// 记借、收
										if ((tranbl.subtract(glvc.getTranam()).compareTo(
												BigDecimal.ZERO) < 0)) {
											tranbl = glvc.getTranam().subtract(tranbl);
											l_blncdn = E_BLNCDN.C;
										} else {
											tranbl = tranbl.subtract(glvc.getTranam());

										}
									}
								}
							} else if (l_blncdn == E_BLNCDN.C) {

								l_tranbl = tranbl;

								if (glvc.getBlncdn() == E_BLNCDN.C) {// 余额方向 为贷方处理
									if (glvc.getAmntcd() == E_AMNTCD.CR
											|| glvc.getAmntcd() == E_AMNTCD.PY) {
										tranbl = tranbl.subtract(glvc.getTranam());
									} else {
										tranbl = tranbl.add(glvc.getTranam());
									}
								} else if (glvc.getBlncdn() == E_BLNCDN.D) {// 余额方向 为借方处理
									if (glvc.getAmntcd() == E_AMNTCD.DR
											|| glvc.getAmntcd() == E_AMNTCD.RV) {
										tranbl = tranbl.subtract(glvc.getTranam());
									} else {
										tranbl = tranbl.add(glvc.getTranam());
									}
								} else if (glvc.getBlncdn() == E_BLNCDN.Z) {// 余额方向为扎差类
									if (glvc.getAmntcd() == E_AMNTCD.CR
											|| glvc.getAmntcd() == E_AMNTCD.PY) {
										if ((tranbl.subtract(glvc.getTranam()).compareTo(
												BigDecimal.ZERO) < 0)) {
											tranbl = glvc.getTranam().subtract(tranbl);
											l_blncdn = E_BLNCDN.D;
										} else {
											tranbl = tranbl.subtract(glvc.getTranam());

										}
									} else {
										// 记借、收	
										if ((tranbl.subtract(glvc.getTranam()).compareTo(
												BigDecimal.ZERO) < 0)) {
											tranbl = glvc.getTranam().add(tranbl);
											l_blncdn = E_BLNCDN.D;
										} else {
											tranbl = tranbl.add(glvc.getTranam());

										}
									}
								}
							}else if(l_blncdn == E_BLNCDN.Z && CommUtil.compare(l_tranbl, BigDecimal.ZERO)==0){
								l_tranbl = tranbl;

								if (glvc.getBlncdn() == E_BLNCDN.C) {// 余额方向 为贷方处理
									if (glvc.getAmntcd() == E_AMNTCD.CR
											|| glvc.getAmntcd() == E_AMNTCD.PY) {
										tranbl = tranbl.subtract(glvc.getTranam());
									} else {
										tranbl = tranbl.add(glvc.getTranam());
									}
								} else if (glvc.getBlncdn() == E_BLNCDN.D) {// 余额方向 为借方处理
									if (glvc.getAmntcd() == E_AMNTCD.DR
											|| glvc.getAmntcd() == E_AMNTCD.RV) {
										tranbl = tranbl.subtract(glvc.getTranam());
									} else {
										tranbl = tranbl.add(glvc.getTranam());
									}
								} else if (glvc.getBlncdn() == E_BLNCDN.Z) {// 余额方向为扎差类
									if (glvc.getAmntcd() == E_AMNTCD.CR
											|| glvc.getAmntcd() == E_AMNTCD.PY) {
										if ((tranbl.subtract(glvc.getTranam()).compareTo(
												BigDecimal.ZERO) < 0)) {
											tranbl = glvc.getTranam().add(tranbl);
										} else {
											tranbl = tranbl.add(glvc.getTranam());
										}
									} else {
										// 记借、收
										if ((tranbl.subtract(glvc.getTranam()).compareTo(
												BigDecimal.ZERO) < 0)) {
											tranbl = glvc.getTranam().subtract(tranbl);
											l_blncdn = E_BLNCDN.C;
										} else {
											tranbl = tranbl.subtract(glvc.getTranam());

										}
									}
								}
							}
							params.remove("tranbl_l");
							params.add("tranbl_l", tranbl);
							
							InQuerySqlsDao.updGlvcByGlvcsq(l_tranbl,glvc.getTrandt(), glvc.getGlvcsq(),DateTools2.getCurrentTimestamp());
							bizlog.debug("第"+index+"条更新余额为:" +l_tranbl);
							bizlog.debug("第"+index+"*********************条更新传票记录结束*********************");
						return true;
				
			}
					
					
			});
			
		}

		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.in.tables.In.GlKnaLsbl> getBatchDataWalker(cn.sunline.ltts.busi.intran.batchtran.intf.Upglvc.Input input, cn.sunline.ltts.busi.intran.batchtran.intf.Upglvc.Property property) {
			Params params = new Params();
			return new CursorBatchDataWalker<cn.sunline.ltts.busi.in.tables.In.GlKnaLsbl>(
					InQuerySqlsDao.namedsql_selAcctnoByTrandt, params);
		}

}


