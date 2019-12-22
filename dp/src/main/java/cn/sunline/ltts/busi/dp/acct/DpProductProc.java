package cn.sunline.ltts.busi.dp.acct;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.logging.LogConfigManager.SystemType;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.namedsql.DpSaveDrawDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctAddt;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctAddtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDraw;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDrawDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDrawPlan;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDrawPlanDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdr;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdrDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdrPlan;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdrPlanDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxsv;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxsvDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxsvPlan;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxsvPlanDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSave;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSaveDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSavePlan;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSavePlanDao;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DppbDetailInfo;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstProd;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DRAWCT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DWBKDL;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DWBKLI;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVBKAD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVBKDL;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVBKLI;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVPLFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AMNTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CTRLWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PLSTAT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TIMEWY;

public class DpProductProc {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(DpProductProc.class);
	
	/**
	 * 
	 * @author renjinghua
	 * 		<p>
	 *	    <li>2016年7月5日-下午3:49:11<li>
	 *      <li>功能描述：处理活期存入计划</li>
	 *      </p>
	 * 
	 * @param acctno 负债账号
	 * @param tranam 交易金额
	 */
	public static void  prcKnaSavePlan(String acctno, BigDecimal tranam){
		
		bizlog.method("prcKnaSavePlan bigin>>>>>>>>>>>>>>>>>>>>>>>");
		bizlog.parm("acctno [%s]", acctno);
		bizlog.parm("tranam [%s]", tranam);
		
		//获取法人代码
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
				
		KnaSave tblKnaSave = KnaSaveDao.selectOne_odb1(acctno, true);
		
		//存入计划是否违约，存入控制表中有总控标志，代表整个存入计划是否违约，而非单条计划
		//本次存入先前判断存入计划是否违约，违约后做相关处理
		if(E_YES___.YES == tblKnaSave.getSpbkfg()){
			if(E_SVBKDL.REFUSE == tblKnaSave.getDfltwy()){
				throw DpModuleError.DpstAcct.BNAS1745();
			}
			else if(E_SVBKDL.VAR_INST == tblKnaSave.getDfltwy()){
				//TODO 分段计息，计提结息时使用，这里暂时不做处理
				
			}
			else if(E_SVBKDL.DELT_INRT ==  tblKnaSave.getDfltwy()){
				//TODO 按违约利率计息，计提结息时使用，这里暂时不做处理
				
			}
		}
		
		//获取存入计划表中状态为未处理成功的最小序号
		long seqno = DpSaveDrawDao.selKnaSavePlanMinsSeqno(corpno, acctno, false);
		
		if(CommUtil.isNull(seqno)){
			throw DpModuleError.DpstAcct.BNAS1746();
		}
		
		//根据序号获取未处理存入计划
		KnaSavePlan tblSavePlan = KnaSavePlanDao.selectOne_odb1(acctno, seqno, true);
		
		//获取相关信息
		String plstad = tblSavePlan.getPlstad(); //计划起始日期
		String ploved = tblSavePlan.getPloved(); //计划结束日期
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		
//		String 
		
		//交易日期小于起始日期
		if(CommUtil.compare(trandt, plstad) < 0){
			// TODO 不允许预存入
			throw DpModuleError.DpstAcct.BNAS1754();
		}
		//交易日期大于终止日期
		else if(CommUtil.compare(trandt, ploved) > 0){
			
			//漏存控制次数
			long svletm = tblKnaSave.getSvletm(); //漏存次数
			long leakSvtm = tblKnaSave.getResltm(); //实际漏存次数
			
			//未达到最大漏存次数
			if(CommUtil.compare(leakSvtm, svletm) < 0){
				
				//查询当前存入计划信息
				KnaSavePlan tblSavePlan2 = DpSaveDrawDao.selKnaSavePlanCurrent(corpno, acctno, trandt, true);
				
				/* 未违约，可以进行补齐 */
				if(E_SVBKAD.REFUSE == tblKnaSave.getSvlewy()){
					//不允许补足
					throw DpModuleError.DpstAcct.BNAS1747();
				}else if(E_SVBKAD.ALL == tblKnaSave.getSvlewy()){
					//全部补足，查询当前日期的所有未处理与正在处理的记录，
					//存入金额必须=当前日期之前违约记录的金额累加 + 当前日期需要存入的金额，如果不是，则报错
					BigDecimal bigSumSvbkam = DpSaveDrawDao.selKnaSavePlanSumSvbkam(corpno, acctno, trandt, true);
					
					if(CommUtil.compare(tranam, bigSumSvbkam.add(tblSavePlan2.getPlmony())) < 0){
						throw DpModuleError.DpstAcct.BNAS1748();
					}else{
						//将当前日期之前所有未处理与正在处理的记录计划改为处理完成
						long minSvbksq = DpSaveDrawDao.selKnaSavePlanMinSvbksq(corpno, acctno, trandt, false);
						long maxSvbksq = DpSaveDrawDao.selKnaSavePlanMaxSvbksq(corpno, acctno, trandt, false);
						
						if(CommUtil.isNull(minSvbksq)){
							minSvbksq = 0;
						}
						
						if(CommUtil.isNull(maxSvbksq)){
							maxSvbksq = 0;
						}
						
						//实际存入次数、实际存入金额不做更新，这样可以看出这些计划是通过补齐的方式完成的
						for(long i = minSvbksq; i <= maxSvbksq; i++){
							KnaSavePlan tblSave_plan = KnaSavePlanDao.selectOne_odb1(acctno, i, false);
							if(CommUtil.isNotNull(tblSave_plan)){
								tblSave_plan.setPlstat(E_PLSTAT.DLSU);
								KnaSavePlanDao.updateOne_odb1(tblSave_plan);
							}
						}
						
						tblSavePlan2.setResvam(tblSavePlan2.getPlmony());
						tblSavePlan2.setResvnm(tblSavePlan2.getResvnm() + 1);
						tblSavePlan2.setPlstat(E_PLSTAT.DLSU);
						
					}
					
				}else if(E_SVBKAD.COUNT == tblKnaSave.getSvlewy()){
					//控制漏补次数
					if(CommUtil.compare(tblKnaSave.getReistm(), tblKnaSave.getMaxisp()) >= 0){
						throw DpModuleError.DpstAcct.BNAS1751();
					}
					
					//实际漏补次数加1
					tblKnaSave.setReistm(tblKnaSave.getReistm() + 1);
					
				}else if(E_SVBKAD.THREE == tblKnaSave.getSvlewy()){
					//漏一补三
					if(tblKnaSave.getResltm() == 1){
						
						//当期日期之前漏存的金额总和
						BigDecimal bigSvbkam = tblSavePlan.getPlmony().subtract(tblSavePlan.getResvam());
						//查询下一计划记录
						KnaSavePlan tblSavePlan3 = KnaSavePlanDao.selectOne_odb1(acctno, tblSavePlan2.getSeqnum() + 1, true);
						//统计漏一补三时，总共应该存入的金额
						BigDecimal bigSumDeptam = tblSavePlan2.getPlmony().subtract(tblSavePlan2.getResvam());
						bigSumDeptam = bigSumDeptam.add(tblSavePlan3.getPlmony()).add(bigSvbkam);
						
						//如果存入金额大于应该存入金额，则交易拒绝；等于，则正常处理，实际漏存次数恢复；小于，则记录违约
						if(CommUtil.compare(tranam, bigSumDeptam) == 0){
							//更新下一计划为处理完成
							tblSavePlan3.setResvam(tblSavePlan3.getPlmony());
							tblSavePlan3.setResvnm(tblSavePlan3.getResvnm() + 1);
							tblSavePlan3.setPlstat(E_PLSTAT.DLSU);
							
							
							//更新当期计划为处理完成
							tblSavePlan2.setResvam(tblSavePlan2.getPlmony());
							tblSavePlan2.setResvnm(tblSavePlan2.getResvnm() + 1);
							tblSavePlan2.setPlstat(E_PLSTAT.DLSU);
							
							
							//更新漏存计划为处理完成
							tblSavePlan.setResvam(tblSavePlan.getPlmony());
							tblSavePlan.setResvnm(tblSavePlan.getResvnm() + 1);
							tblSavePlan.setPlstat(E_PLSTAT.DLSU);
							
							
							//实际漏存次数恢复
							tblKnaSave.setResltm(ConvertUtil.toLong(0)); //
							
						}else if(CommUtil.compare(tranam, bigSumDeptam) < 0){
							//处理存入金额，分别摊销到各计划中
							BigDecimal bigRedpam = tranam;
							//处理漏存计划
							if(CommUtil.compare(bigRedpam, bigSvbkam) >= 0){
								//更新漏存计划为处理完成
								tblSavePlan.setResvam(tblSavePlan.getResvam().add(bigSvbkam));
								tblSavePlan.setResvnm(tblSavePlan.getResvnm() + 1);
								tblSavePlan.setPlstat(E_PLSTAT.DLSU);
								bigRedpam = bigRedpam.subtract(bigSvbkam);
							}else{
								//处理漏存计划为处理中
								tblSavePlan.setResvam(tblSavePlan.getResvam().add(bigRedpam));
								tblSavePlan.setResvnm(tblSavePlan.getResvnm() + 1);
								tblSavePlan.setPlstat(E_PLSTAT.DLNG);
								
								bigRedpam =  BigDecimal.ZERO;
							}
							
							//处理当期计划
							if(CommUtil.compare(bigRedpam, BigDecimal.ZERO) > 0){
								if(CommUtil.compare(bigRedpam, tblSavePlan2.getPlmony()) >= 0){
									tblSavePlan2.setResvam(tblSavePlan2.getPlmony());
									tblSavePlan2.setResvnm(tblSavePlan2.getResvnm() + 1);
									tblSavePlan2.setPlstat(E_PLSTAT.DLSU);
									
									bigRedpam = bigRedpam.subtract(tblSavePlan2.getPlmony());//存在漏存，当期存入金额一定是零
								}else{
									tblSavePlan2.setResvam(bigRedpam);
									tblSavePlan2.setResvnm(tblSavePlan2.getResvnm() + 1);
									tblSavePlan2.setPlstat(E_PLSTAT.DLNG);
									
									bigRedpam = BigDecimal.ZERO;
								}
							}
							
							//处理下一计划
							if(CommUtil.compare(bigRedpam, BigDecimal.ZERO) > 0){
								if(CommUtil.compare(bigRedpam, tblSavePlan3.getPlmony()) >= 0){
									tblSavePlan3.setResvam(tblSavePlan3.getPlmony());
									tblSavePlan3.setResvnm(tblSavePlan3.getResvnm() + 1);
									tblSavePlan3.setPlstat(E_PLSTAT.DLSU);
									
									bigRedpam = bigRedpam.subtract(tblSavePlan3.getPlmony());
								}else{
									tblSavePlan3.setResvam(bigRedpam);
									tblSavePlan3.setResvnm(tblSavePlan3.getResvnm() + 1);
									tblSavePlan3.setPlstat(E_PLSTAT.DLNG);
									
									bigRedpam = BigDecimal.ZERO;
								}
							}
							
							//未全部补足，则置为违约
							tblKnaSave.setResltm(tblKnaSave.getResltm() + 1);
							tblKnaSave.setSpbkfg(E_YES___.YES);
						}else{
							throw DpModuleError.DpstAcct.BNAS1752();
						}
						
						//更新下一计划
						KnaSavePlanDao.updateOne_odb1(tblSavePlan3);
						
					}else{
						throw DpModuleError.DpstAcct.BNAS1753(tblKnaSave.getSvlewy().getLongName());
					}
					
					//更新当前计划
					KnaSavePlanDao.updateOne_odb1(tblSavePlan2);
					
					//更新漏存计划
					KnaSavePlanDao.updateOne_odb1(tblSavePlan);
				}
			}else{
				//违约处理方式,已经漏存
				if(E_SVBKDL.REFUSE == tblKnaSave.getDfltwy()){
					throw DpModuleError.DpstAcct.BNAS1745();
				}
				else if(E_SVBKDL.VAR_INST == tblKnaSave.getDfltwy()){
					//TODO 分段计息，计提结息时使用，这里暂时不做处理
					
				}
				else if(E_SVBKDL.DELT_INRT ==  tblKnaSave.getDfltwy()){
					//TODO 按违约利率计息，计提结息时使用，这里暂时不做处理
					
				}
			}
		}
		//交易日期大于等于起始日期，小于等于终止日期
		else if(CommUtil.compare(trandt, plstad) >= 0 && CommUtil.compare(trandt, ploved) <= 0){
			//正常存入
			//正常存入时，存入金额不能大于当前计划剩余应该存入金额
			if(CommUtil.compare(tblSavePlan.getPlmony().subtract(tblSavePlan.getResvam()), tranam) < 0){
				throw DpModuleError.DpstAcct.BNAS1752();
			}
			
			E_SVPLFG svplfg = tblKnaSave.getPscrwy();
			
			//控制次数
			if(E_SVPLFG.COUNT == svplfg){
				if(CommUtil.compare(tblSavePlan.getPltime(), tblSavePlan.getResvnm()) == 0){
					throw DpModuleError.DpstAcct.BNAS1749();
				}
			}
			//控制总额
			else if(E_SVPLFG.TOTAL == tblKnaSave.getPscrwy()){
				if(CommUtil.compare(tblSavePlan.getResvam(), tblSavePlan.getPlmony()) >= 0){
					throw DpModuleError.DpstAcct.BNAS1750();
				}
			}
			//控制总额和次数
			else if (E_SVPLFG.C_T == tblKnaSave.getPscrwy()){
				if(CommUtil.compare(tblSavePlan.getPltime(), tblSavePlan.getResvnm()) == 0){
					throw DpModuleError.DpstAcct.BNAS1749();
				}
				
				if(CommUtil.compare(tblSavePlan.getResvam(), tblSavePlan.getPlmony()) >= 0){
					throw DpModuleError.DpstAcct.BNAS1750();
				}
			}
			
			//该条计划，实际存入次数
			long resvtm = tblSavePlan.getResvnm() + 1;
			
			//该条计划，实际存入总额
			BigDecimal bigResvam = tblSavePlan.getResvam().add(tranam);
			
			//如果次数已经达到，则更新该条存入计划为处理完成
			if(CommUtil.compare(resvtm, tblSavePlan.getPltime()) == 0){
				tblSavePlan.setPlstat(E_PLSTAT.DLSU);
			}else{
				tblSavePlan.setPlstat(E_PLSTAT.DLNG);
			}
			
			//如果金额已达到，则更新该条存入计划为处理完成
			if(CommUtil.compare(bigResvam, tblSavePlan.getPlmony()) >= 0){
				tblSavePlan.setPlstat(E_PLSTAT.DLSU);
			}else{
				tblSavePlan.setPlstat(E_PLSTAT.DLNG);
				//正常存入时，未处理完成，漏存次数加1
				tblKnaSave.setResltm(tblKnaSave.getResltm() + 1);
			}
			
			tblSavePlan.setResvam(bigResvam);
			tblSavePlan.setResvnm(resvtm);
			KnaSavePlanDao.updateOne_odb1(tblSavePlan);
		}
		
		
		//更新存入控制实际存入次数、实际存入金额
		tblKnaSave.setResvam(tblKnaSave.getResvam().add(tranam));
		tblKnaSave.setResvnm(tblKnaSave.getResvnm() + 1);
		
		//存入计划违约标志
		//账户漏存，则设置违约标志为是
		if(E_SVBKLI.AMOUNT == tblKnaSave.getDfltsd()){
			if(CommUtil.isNotNull(tblKnaSave.getResltm())
					&& CommUtil.compare(tblKnaSave.getResltm(), ConvertUtil.toLong(0)) > 0){
				tblKnaSave.setSpbkfg(E_YES___.YES);
			}
		}
		//账户漏存次数超过漏存控制次数，则更新违约标志为是
		else if(E_SVBKLI.COUNT == tblKnaSave.getDfltsd()){
			if(CommUtil.compare(tblKnaSave.getResltm(), tblKnaSave.getSvletm()) >= 0){
				tblKnaSave.setSpbkfg(E_YES___.YES);
			}
		}
		
		KnaSaveDao.updateOne_odb1(tblKnaSave);
		
		bizlog.method("prcKnaSavePlan end<<<<<<<<<<<<<<<<<<<<<<<<<");
	}
	
	/**
	 * 
	 * @author renjinghua
	 * 		<p>
	 *	    <li>2016年7月5日-上午9:15:38<li>
	 *      <li>功能描述：处理定期存入计划</li>
	 *      </p>
	 * 
	 * @param acctno 负债账号
	 * @param tranam 交易金额
	 */
	public static void prcFxacSavePlan(String acctno, BigDecimal tranam){
		
		bizlog.method("prcFxacSavePlan bigin>>>>>>>>>>>>>>>>>>>>>>>");
		bizlog.parm("acctno [%s]", acctno);
		bizlog.parm("tranam [%s]", tranam);
		
		//获取法人代码
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		
		KnaFxsv tblKnaFxsv = KnaFxsvDao.selectOne_odb1(acctno, true);
		
		//存入计划是否违约，存入控制表中有总控标志，代表整个存入计划是否违约，而非单条计划
		//本次存入先前判断存入计划是否违约，违约后做相关处理
		if(E_YES___.YES == tblKnaFxsv.getSpbkfg()){
			if(E_SVBKDL.REFUSE == tblKnaFxsv.getDfltwy()){
				throw DpModuleError.DpstAcct.BNAS1745();
			}
			else if(E_SVBKDL.VAR_INST == tblKnaFxsv.getDfltwy()){
				//TODO 分段计息，计提结息时使用，这里暂时不做处理
				
			}
			else if(E_SVBKDL.DELT_INRT ==  tblKnaFxsv.getDfltwy()){
				//TODO 按违约利率计息，计提结息时使用，这里暂时不做处理
				
			}
		}
		//modify 20180309
		//获取存入计划表中状态为未处理成功的最小序号
		long seqno = DpSaveDrawDao.selKnaFxsvPlanMinsSeqno(corpno, acctno, false);
		
		if(CommUtil.isNull(seqno)){
			throw DpModuleError.DpstAcct.BNAS1746();
		}
		
		//根据序号获取未处理存入计划
		KnaFxsvPlan tblFxsvPlan = KnaFxsvPlanDao.selectOne_odb1(acctno, seqno, true);
		
		//获取相关信息
		String plstad = tblFxsvPlan.getPlstad(); //计划起始日期
		String ploved = tblFxsvPlan.getPloved(); //计划结束日期
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		
//		String 
		
		//交易日期小于起始日期
		if(CommUtil.compare(trandt, plstad) < 0){
			// TODO
			//不允许预存入
			throw DpModuleError.DpstAcct.BNAS1752();
		}
		//交易日期大于终止日期
		else if(CommUtil.compare(trandt, ploved) > 0){
				
			//漏存控制次数
			long svletm = tblKnaFxsv.getSvletm(); //漏存次数
			long leakSvtm = tblKnaFxsv.getResltm(); //实际漏存次数
			
			//未达到最大漏存次数
			if(CommUtil.compare(leakSvtm, svletm) < 0){
				
				//查询当前存入计划信息
				KnaFxsvPlan tblFxsvPlan2 = DpSaveDrawDao.selKnaFxsvPlanCurrent(corpno, acctno, trandt, true);
				
				/* 未违约，可以进行补齐 */
				if(E_SVBKAD.REFUSE == tblKnaFxsv.getSvlewy()){
					//不允许补足
					throw DpModuleError.DpstAcct.BNAS1747();
				}else if(E_SVBKAD.ALL == tblKnaFxsv.getSvlewy()){
					//全部补足，查询当前日期的所有未处理与正在处理的记录，
					//存入金额必须=当前日期之前违约记录的金额累加 + 当前日期需要存入的金额，如果不是，则报错
					BigDecimal bigSumSvbkam = DpSaveDrawDao.selKnaSavePlanSumSvbkam(corpno, acctno, trandt, true);
					
					if(CommUtil.compare(tranam, bigSumSvbkam) < 0){
						throw DpModuleError.DpstAcct.BNAS1748();
					}else{
						//将当前日期之前所有未处理与正在处理的记录计划改为处理完成
						long minSvbksq = DpSaveDrawDao.selKnaSavePlanMinSvbksq(corpno, acctno, trandt, false);
						long maxSvbksq = DpSaveDrawDao.selKnaSavePlanMaxSvbksq(corpno, acctno, trandt, false);
						
						if(CommUtil.isNull(minSvbksq)){
							minSvbksq = 0;
						}
						
						if(CommUtil.isNull(maxSvbksq)){
							maxSvbksq = 0;
						}
						
						//实际存入次数、实际存入金额不做更新，这样可以看出这些计划是通过补齐的方式完成的
						for(long i = minSvbksq; i <= maxSvbksq; i++){
							KnaFxsvPlan tblFxsv_plan = KnaFxsvPlanDao.selectOne_odb1(acctno, i, false);
							if(CommUtil.isNotNull(tblFxsv_plan)){
								tblFxsv_plan.setPlstat(E_PLSTAT.DLSU);
								KnaFxsvPlanDao.updateOne_odb1(tblFxsv_plan);
							}
						}
						
						//当期计划为处理完成
						tblFxsvPlan2.setResvam(tblFxsvPlan2.getPlmony());
						tblFxsvPlan2.setResvnm(tblFxsvPlan2.getResvnm() + 1);
						tblFxsvPlan2.setPlstat(E_PLSTAT.DLSU);
					}
					
				}else if(E_SVBKAD.COUNT == tblKnaFxsv.getSvlewy()){
					//控制漏补次数
					if(CommUtil.compare(tblKnaFxsv.getReistm(), tblKnaFxsv.getMaxisp()) >= 0){
						throw DpModuleError.DpstAcct.BNAS1751();
					}
					
					//实际漏补次数加1
					tblKnaFxsv.setReistm(tblKnaFxsv.getReistm() + 1);
					
				}else if(E_SVBKAD.THREE == tblKnaFxsv.getSvlewy()){
					//漏一补三
					if(tblKnaFxsv.getResltm() == 1){
						
						//当期日期之前漏存的金额总和
						BigDecimal bigSvbkam = tblFxsvPlan.getPlmony().subtract(tblFxsvPlan.getResvam());
						//查询下一计划记录
						KnaFxsvPlan tblFxsvPlan3 = KnaFxsvPlanDao.selectOne_odb1(acctno, tblFxsvPlan2.getSeqnum() + 1, true);
						//统计漏一补三时，总共应该存入的金额
						BigDecimal bigSumDeptam = tblFxsvPlan2.getPlmony().subtract(tblFxsvPlan2.getResvam());
						bigSumDeptam = bigSumDeptam.add(tblFxsvPlan3.getPlmony()).add(bigSvbkam);
						
						//如果存入金额大于应该存入金额，则交易拒绝；等于，则正常处理，实际漏存次数恢复；小于，则记录违约
						if(CommUtil.compare(tranam, bigSumDeptam) == 0){
							//更新下一计划为处理完成
							tblFxsvPlan3.setResvam(tblFxsvPlan3.getPlmony());
							tblFxsvPlan3.setResvnm(tblFxsvPlan3.getResvnm() + 1);
							tblFxsvPlan3.setPlstat(E_PLSTAT.DLSU);
							
							//更新当期计划为处理完成
							tblFxsvPlan2.setResvam(tblFxsvPlan2.getPlmony());
							tblFxsvPlan2.setResvnm(tblFxsvPlan2.getResvnm() + 1);
							tblFxsvPlan2.setPlstat(E_PLSTAT.DLSU);
							
							//更新漏存计划为处理完成
							tblFxsvPlan.setResvam(tblFxsvPlan.getPlmony());
							tblFxsvPlan.setResvnm(tblFxsvPlan.getResvnm() + 1);
							tblFxsvPlan.setPlstat(E_PLSTAT.DLSU);
							
							//实际漏存次数恢复
							tblKnaFxsv.setResltm(ConvertUtil.toLong(0)); //
							
						}else if(CommUtil.compare(tranam, bigSumDeptam) < 0){
							//处理存入金额，分别摊销到各计划中
							BigDecimal bigRedpam = tranam;
							//处理漏存计划
							if(CommUtil.compare(bigRedpam, bigSvbkam) >= 0){
								//更新漏存计划为处理完成
								tblFxsvPlan.setResvam(tblFxsvPlan.getResvam().add(bigSvbkam));
								tblFxsvPlan.setResvnm(tblFxsvPlan.getResvnm() + 1);
								tblFxsvPlan.setPlstat(E_PLSTAT.DLSU);
								
								bigRedpam = bigRedpam.subtract(bigSvbkam);
							}else{
								//更新漏存计划为处理中
								tblFxsvPlan.setResvam(tblFxsvPlan.getResvam().add(bigRedpam));
								tblFxsvPlan.setResvnm(tblFxsvPlan.getResvnm() + 1);
								tblFxsvPlan.setPlstat(E_PLSTAT.DLNG);
								
								bigRedpam =  BigDecimal.ZERO;
							}
							
							//处理当期计划
							if(CommUtil.compare(bigRedpam, BigDecimal.ZERO) > 0){
								//剩余存入金额大于等于当期计划存入金额，则更新当期计划为处理完成
								if(CommUtil.compare(bigRedpam, tblFxsvPlan2.getPlmony()) >= 0){
									tblFxsvPlan2.setResvam(tblFxsvPlan2.getPlmony());
									tblFxsvPlan2.setResvnm(tblFxsvPlan2.getResvnm() + 1);
									tblFxsvPlan2.setPlstat(E_PLSTAT.DLSU);
									
									bigRedpam = bigRedpam.subtract(tblFxsvPlan2.getPlmony());//存在漏存，当期存入金额一定是零
								}
								//剩余存入金额小于当期计划存入金额，则更新当期计划为处理中
								else{
									tblFxsvPlan2.setResvam(bigRedpam);
									tblFxsvPlan2.setResvnm(tblFxsvPlan2.getResvnm() + 1);
									tblFxsvPlan2.setPlstat(E_PLSTAT.DLNG);
									
									bigRedpam = BigDecimal.ZERO;
								}
							}
							
							//处理下一计划
							if(CommUtil.compare(bigRedpam, BigDecimal.ZERO) > 0){
								//剩余存入金额大于等于下一计划存入金额，则更新下一计划为处理完成
								if(CommUtil.compare(bigRedpam, tblFxsvPlan3.getPlmony()) >= 0){
									tblFxsvPlan3.setResvam(tblFxsvPlan3.getPlmony());
									tblFxsvPlan3.setResvnm(tblFxsvPlan3.getResvnm() + 1);
									tblFxsvPlan3.setPlstat(E_PLSTAT.DLSU);
									
									bigRedpam = bigRedpam.subtract(tblFxsvPlan3.getPlmony());
								}
								//剩余存入金额小于下一计划存入金额，则更新下一计划为处理中
								else{
									tblFxsvPlan3.setResvam(bigRedpam);
									tblFxsvPlan3.setResvnm(tblFxsvPlan3.getResvnm() + 1);
									tblFxsvPlan3.setPlstat(E_PLSTAT.DLNG);
									
									bigRedpam = BigDecimal.ZERO;
								}
							}
							
							//未全部补足，则置为违约
							tblKnaFxsv.setResltm(tblKnaFxsv.getResltm() + 1);
							tblKnaFxsv.setSpbkfg(E_YES___.YES);
						}else{
							throw DpModuleError.DpstAcct.BNAS1752();
						}
						
						//更新下一计划
						KnaFxsvPlanDao.updateOne_odb1(tblFxsvPlan3);
						
					}else{
						throw DpModuleError.DpstAcct.BNAS1753(tblKnaFxsv.getSvlewy().getLongName());
					}
					
					//更新当期计划
					KnaFxsvPlanDao.updateOne_odb1(tblFxsvPlan2);
				}
			}else{
				//违约处理方式,已经漏存
				if(E_SVBKDL.REFUSE == tblKnaFxsv.getDfltwy()){
					throw DpModuleError.DpstAcct.BNAS1745();
				}
				else if(E_SVBKDL.VAR_INST == tblKnaFxsv.getDfltwy()){
					//TODO 分段计息，计提结息时使用，这里暂时不做处理
					
				}
				else if(E_SVBKDL.DELT_INRT ==  tblKnaFxsv.getDfltwy()){
					//TODO 按违约利率计息，计提结息时使用，这里暂时不做处理
					
				}
			}
		}
		//交易日期大于等于起始日期，小于等于终止日期
		else if(CommUtil.compare(trandt, plstad) >= 0 && CommUtil.compare(trandt, ploved) <= 0){
			//正常存入
			E_SVPLFG svplfg = tblKnaFxsv.getPscrwy();
			
			//控制次数
			if(E_SVPLFG.COUNT == svplfg){
				if(CommUtil.compare(tblFxsvPlan.getPltime(), tblFxsvPlan.getResvnm()) == 0){
					throw DpModuleError.DpstAcct.BNAS1749();
				}
			}
			//控制总额
			else if(E_SVPLFG.TOTAL == tblKnaFxsv.getPscrwy()){
				if(CommUtil.compare(tblFxsvPlan.getResvam(), tblFxsvPlan.getPlmony()) >= 0){
					throw DpModuleError.DpstAcct.BNAS1750();
				}
			}
			//控制总额和次数
			else if (E_SVPLFG.C_T == tblKnaFxsv.getPscrwy()){
				if(CommUtil.compare(tblFxsvPlan.getPltime(), tblFxsvPlan.getResvnm()) == 0){
					throw DpModuleError.DpstAcct.BNAS1749();
				}
				
				if(CommUtil.compare(tblFxsvPlan.getResvam(), tblFxsvPlan.getPlmony()) >= 0){
					throw DpModuleError.DpstAcct.BNAS1750();
				}
			}
			
			//该条计划，实际存入次数
			long resvtm = tblFxsvPlan.getResvnm() + 1;
			
			//该条计划，实际存入总额
			BigDecimal bigResvam = tblFxsvPlan.getResvam().add(tranam);
			
			//如果次数已经达到，则更新该条存入计划为处理完成
			if(CommUtil.compare(resvtm, tblFxsvPlan.getPltime()) == 0){
				tblFxsvPlan.setPlstat(E_PLSTAT.DLSU);
			}else{
				tblFxsvPlan.setPlstat(E_PLSTAT.DLNG);
			}
			
			//如果金额已达到，则更新该条存入计划为处理完成
			if(CommUtil.compare(bigResvam, tblFxsvPlan.getPlmony()) >= 0){
				tblFxsvPlan.setPlstat(E_PLSTAT.DLSU);
			}else{
				tblFxsvPlan.setPlstat(E_PLSTAT.DLNG);
				//正常存入时，未处理完成，漏存次数加1
				tblKnaFxsv.setResltm(tblKnaFxsv.getResltm() + 1);
			}
			
			tblFxsvPlan.setResvam(bigResvam);
			tblFxsvPlan.setResvnm(resvtm);
			KnaFxsvPlanDao.updateOne_odb1(tblFxsvPlan);
		}
		
		//更新存入控制表中的实际存入金额与实际存入次数
		tblKnaFxsv.setResvam(tblKnaFxsv.getResvam().add(tranam));
		tblKnaFxsv.setResvnm(tblKnaFxsv.getResvnm() + 1);
		
		//存入计划违约标志
		//账户漏存，则设置违约标志为是
		if(E_SVBKLI.AMOUNT == tblKnaFxsv.getDfltsd()){
			if(CommUtil.isNotNull(tblKnaFxsv.getResltm())
					&& CommUtil.compare(tblKnaFxsv.getResltm(), ConvertUtil.toLong(0)) > 0){
				tblKnaFxsv.setSpbkfg(E_YES___.YES);
			}
		}
		//账户漏存次数超过漏存控制次数，则更新违约标志为是
		else if(E_SVBKLI.COUNT == tblKnaFxsv.getDfltsd()){
			if(CommUtil.compare(tblKnaFxsv.getResltm(), tblKnaFxsv.getSvletm()) >= 0){
				tblKnaFxsv.setSpbkfg(E_YES___.YES);
			}
		}
		
		KnaFxsvDao.updateOne_odb1(tblKnaFxsv);
		
		bizlog.method("prcFxacSavePlan end<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * 支取控制检查
	 * @param acctno 负债账号
	 * @param tranam 交易金额
	 * @param fcflag 定活标志
	 * @param prodcd 产品号
	 * @param crcycd 币种
	 */
	public static void chkDpDraw(String acctno, BigDecimal tranam,E_FCFLAG fcflag,String prodcd,String crcycd) {
		if(CommUtil.isNull(tranam)){
			throw DpModuleError.DpstProd.BNAS0620();
		}
		//金额控制方式
		E_AMNTWY dramwy = null;
		//最小金额
		BigDecimal drmiam = BigDecimal.ZERO;
		//最大金额
		BigDecimal drmxam = BigDecimal.ZERO;
		//次数控制方式
		E_TIMEWY drtmwy = null;
		//实际支取次数
		Long count = 0l;
		//最小支取次数
		Long minitm = 0l;
		//最大支取次数
		Long maxitm = 0l;
		E_DRAWCT posttp = null;//支取控制方式		
		E_CTRLWY postwy = null;//支取控制方法
		E_YES___ setpwy = null;//是否设置支取计划
		E_DWBKLI drdfsd = null;//支取违约标准
		E_DWBKDL drdfwy = null;//违约处理方式
		E_SVPLFG drcrwy = null;//支取计划完成方式
		BigDecimal minibl = BigDecimal.ZERO; //最低留存余额
		E_YES___ ismibl = null; //是否允许小于最低留存余额
		E_YES___ dpbkfg=null;
		
		//KupDppb_draw dppb_draw = KupDppb_drawDao.selectOne_odb1(prodcd, crcycd, true);
		
		KnaDraw tblKnaDraw = null;//负债活期账户支取控制
		KnaFxdr tblKnaFxdr = SysUtil.getInstance(KnaFxdr.class);//负债定期账户支取控制
		KnaDrawPlan tblKnaDrawPlan =null;//债活期账户支取计划表
		KnaFxdrPlan tblKnaFxdrPlan = SysUtil.getInstance(KnaFxdrPlan.class);//债账户支取计划表
		KnaFxac tblKnafxac = SysUtil.getInstance(KnaFxac.class);// 定期负债账户表
		KnaAcct tblKnaAcct = null;// 活期复制账户表
		

		if(E_FCFLAG.CURRENT == fcflag){
			// 获取负债活期账户支取控制信息
			tblKnaDraw = KnaDrawDao.selectOne_odb1(acctno, false);
			if(CommUtil.isNull(tblKnaDraw)){
				throw CaError.Eacct.BNAS0667();
			}
			dramwy = tblKnaDraw.getDramwy();//支取金额控制方式
			drmiam = tblKnaDraw.getDrmiam();//单次支取最小金额
			drmxam = tblKnaDraw.getDrmxam();//单次支取最大金额
			drtmwy = tblKnaDraw.getDrtmwy();//支取次数控制方式
			count  = tblKnaDraw.getRedwnm()+1;//实际支取次数
			minitm = tblKnaDraw.getDrmitm();//最小支取次数
			maxitm = tblKnaDraw.getDrmxtm();//最大支取次数
			posttp = tblKnaDraw.getDrawtp();//支取控制方式
			postwy = tblKnaDraw.getCtrlwy();//支取控制方法	
			drdfsd = tblKnaDraw.getDrdfsd();//支取违约标准
			drdfwy = tblKnaDraw.getDrdfwy();//违约支取处理方式
			drcrwy = tblKnaDraw.getDrcrwy();//支取计划控制完成方式  --这个控制没有意义，不作为控制参数
			setpwy = tblKnaDraw.getSetpwy();//是否设置支取计划
			ismibl = tblKnaDraw.getIsmamt(); //是否允许小于最低留存余额
			dpbkfg =tblKnaDraw.getDpbkfg();//是否违约标志 

			if(E_YES___.YES==setpwy){//如果设置了支取计划，则按计划控制
				Long minSeqnum = DpSaveDrawDao.selKnaDrawPlanMinSeqno(CommTools.getBaseRunEnvs().getBusi_org_id(), acctno, true);
				tblKnaDrawPlan=  KnaDrawPlanDao.selectOne_odb1(acctno, minSeqnum, true);
				drmiam = tblKnaDrawPlan.getDrmiam();//单次支取最小金额
				drmxam = tblKnaDrawPlan.getDrmxam();//单次支取最大金额
				
			}
		}else if(E_FCFLAG.FIX == fcflag){
			// 获取负债定期账户支取控制信息
			bizlog.debug("负债账号=============" + acctno);
			tblKnaFxdr = KnaFxdrDao.selectOne_odb1(acctno, false);
			if(CommUtil.isNull(tblKnaFxdr)){
				throw CaError.Eacct.BNAS0840();
			}
			
			dramwy = tblKnaFxdr.getDramwy();//支取金额控制方式
			drmiam = tblKnaFxdr.getDrmiam();//单次支取最小金额
			drmxam = tblKnaFxdr.getDrmxam();//单次支取最大金额
			drtmwy = tblKnaFxdr.getDrtmwy();//支取次数控制方式
			count  = tblKnaFxdr.getRedwnm()+1;//实际支取次数
			minitm = tblKnaFxdr.getDrmitm();//最小支取次数
			maxitm = tblKnaFxdr.getDrmxtm();//最大支取次数
			posttp = tblKnaFxdr.getDrawtp();//支取控制方式
			postwy = tblKnaFxdr.getCtrlwy();//支取控制方法
			drdfsd = tblKnaFxdr.getDrdfsd();//支取违约标准
			drdfwy = tblKnaFxdr.getDrdfwy();//违约支取处理方式
			drcrwy = tblKnaFxdr.getDrcrwy();//支取计划控制完成方式			
			setpwy = tblKnaFxdr.getSetpwy();//是否设置支取计划
			ismibl = tblKnaFxdr.getIsmamt(); //是否允许小于最低留存余额
			dpbkfg = tblKnaFxdr.getDpbkfg();//是否违约标志 

			if(E_YES___.YES==setpwy){//如果设置了支取计划，则按计划控制
				Long minSeqnum = DpSaveDrawDao.selKnaFxdrPlanMinSeqno(CommTools.getBaseRunEnvs().getBusi_org_id(), acctno, true);
				tblKnaFxdrPlan=  KnaFxdrPlanDao.selectOne_odb1(acctno, minSeqnum, true);
				drmiam = tblKnaFxdrPlan.getDrmiam();//单次支取最小金额
				drmxam = tblKnaFxdrPlan.getDrmxam();//单次支取最大金额
			
				
			}		
		}else{
			throw DpModuleError.DpstAcct.BNAS0843(fcflag.toString());
		}
		
		if(fcflag == E_FCFLAG.CURRENT){
			tblKnaAcct = KnaAcctDao.selectOne_odb1(acctno, true); //查询活期账户表
			
			// 检查三类户最高限额控制,排除部分交易
			BigDecimal maxbin = BigDecimal.ZERO;// 最高限额
			// 获取三类户最高限额
			KnaAcctAddt tblKnaAcctAddt = KnaAcctAddtDao.selectOne_odb1(acctno, true);
			
			// 三类户最高限额控制检查,亲情钱包账户暂不检查，先写死  交易金额小于零的进行判断 add by chenlk 20170120 
			if (CommUtil.compare(tranam, BigDecimal.ZERO)<0&&E_ACCATP.WALLET == tblKnaAcctAddt.getAccatp() 
					&& tblKnaAcct.getAcsetp() != E_ACSETP.FW) {
				
				//查询最高余额控制参数
				String lttscd = BusiTools.getBusiRunEnvs().getLttscd();
				KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1(
						"MAXBAL_CONTROL", tblKnaAcctAddt.getAccatp()
								.getValue(), lttscd, "%", false);
				
				String chkFlag = "0"; //校验最高余额标志
				//查询控制参数不为空，则赋值配置参数
				if(CommUtil.isNotNull(tblKnpParameter) ){
					chkFlag = tblKnpParameter.getParm_value1();
				}
				
				//校验标志为1时，不校验
				if(!CommUtil.equals(chkFlag, "1")){
					
					//if else 中都要执行的查询，拉出来放在外面执行 modify chenlk 2016-8-21
					// 获取参数表中三类户最高限额
					KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm.maxbln", "3", "%", "%", false);
					if (CommUtil.isNull(para)) {
						
						DpstComm.BNAS1134();
					}				
					
					if (CommUtil.compare(tblKnaAcctAddt.getHigham(), BigDecimal.ZERO) > 0) {
						
						maxbin = tblKnaAcctAddt.getHigham();// 最高限额
						
						BigDecimal maxbin1 = new BigDecimal(para.getParm_value1());// 参数表最高限额
						
						// 客户设置限额大于参数表最高限额
						if (CommUtil.compare(maxbin, maxbin1) > 0) {
							throw DpModuleError.DpstAcct.BNAS1135(maxbin.toString(),maxbin1.toString());
						}
						
					} else {
						
						maxbin = new BigDecimal(para.getParm_value1());
					}
					if (CommUtil.compare(maxbin, BigDecimal.ZERO) > 0){
						BigDecimal tranbl = BigDecimal.ZERO;// 余额
						tranbl = DpAcctProc.getAcctBalance(tblKnaAcct);// 当前余额
						tranbl = tranbl.add(tranam.abs());
						if (CommUtil.compare(tranbl, maxbin) > 0) {// 存入后余额大于最高限额
							
							bizlog.debug("存入金额[%s]过大，存入后余额[%s]超出账户最高限额[%s]", tranam.abs(), tranbl, maxbin.toString());
							
							throw DpModuleError.DpstComm.BNAS1045();
						}
						
					}
				}
			}			
			
			minibl = tblKnaAcct.getHdmimy(); //最低留存余额
			
			//判断最低留存余额,支取后账户小于最低留存余额，不允许支取,暂时只判断联机交易
			if(SysUtil.getCurrentSystemType() == SystemType.onl){
				
				if(E_YES___.YES != ismibl && CommUtil.compare(minibl, BigDecimal.ZERO) > 0){
					//支取后余额
					BigDecimal bigacctbl = DpAcctProc.getAcctBalance(tblKnaAcct).subtract(tranam);
					if(CommUtil.compare(bigacctbl, BigDecimal.ZERO) > 0 
							&& CommUtil.compare(bigacctbl, minibl) < 0){
						throw DpModuleError.DpstAcct.BNAS1131(bigacctbl, minibl);
					}
				}
			}
		}else if(fcflag == E_FCFLAG.FIX){
			String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
			tblKnafxac = KnaFxacDao.selectOne_odb1(acctno, true); //查询定期账户表
			
			minibl = tblKnafxac.getHdmimy(); //最低留存余额
			
			//判断是否到期后支取或者是否全部支取，如果是的话，则不检查控制信息
			if ((CommUtil.isNull(tblKnafxac.getMatudt())
					|| (CommUtil.compare(tblKnafxac.getMatudt(), trandt) > 0))
					&& CommUtil.compare(tranam, tblKnafxac.getOnlnbl()) != 0) {

				//判断最低留存余额,支取后账户小于最低留存余额，不允许支取,暂时只判断联机交易
				if(SysUtil.getCurrentSystemType() == SystemType.onl){
					
					if(E_YES___.YES != ismibl){
						//支取后余额
						BigDecimal bigacctbl = tblKnafxac.getOnlnbl().subtract(tranam);
						if(CommUtil.compare(bigacctbl, BigDecimal.ZERO) > 0 
								&& CommUtil.compare(bigacctbl, minibl) < 0){
							throw DpModuleError.DpstAcct.BNAS1131(bigacctbl, minibl);
						}
					}
				}
			}
		}
		//支取控制方式
//		if(E_DRAWCT.NO == posttp){
//			throw DpModuleError.DpstAcct.E9999("不允许支取");
//		}else 
		if(E_DRAWCT.YES == posttp){
			return;
		}else if(E_DRAWCT.COND == posttp){
						
			if(fcflag == E_FCFLAG.CURRENT){
								
				if(postwy == E_CTRLWY.AMCL){ //金额控制
					chkDrawAmtControl(dramwy,tranam,drmiam,drmxam);
				}else if(postwy == E_CTRLWY.TMCL){ //次数控制
					chkDrawTimeControl(drtmwy,count,minitm,maxitm);
				}else if(postwy == E_CTRLWY.ATML){ //金额和次数控制
					chkDrawAmtControl(dramwy,tranam,drmiam,drmxam);
					chkDrawTimeControl(drtmwy,count,minitm,maxitm);
				}else{
					throw DpModuleError.DpstComm.BNAS0121();
				}				
				tblKnaDraw.setRedqam(tblKnaDraw.getRedqam().add(tranam));
				tblKnaDraw.setRedwnm(tblKnaDraw.getRedwnm() + 1);
				KnaDrawDao.updateOne_odb1(tblKnaDraw);
			}else if(fcflag == E_FCFLAG.FIX){
				
				String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
				//判断是否到期后支取或者是否全部支取，如果是的话，则不检查控制信息
				if ((CommUtil.isNull(tblKnafxac.getMatudt())
						|| (CommUtil.compare(tblKnafxac.getMatudt(), trandt) > 0))
						&& CommUtil.compare(tranam, tblKnafxac.getOnlnbl()) != 0) {

					//如果违约后拒绝支取，则抛出异常
					if(E_DWBKDL.REFUSE == drdfwy && dpbkfg == E_YES___.YES){
						throw DpModuleError.DpstAcct.BNAS1409();
					}
					
					if(postwy == E_CTRLWY.AMCL){ //金额控制
						chkDrawAmtControl(dramwy,tranam,drmiam,drmxam);
					}else if(postwy == E_CTRLWY.TMCL){ //次数控制
						chkDrawTimeControl(drtmwy,count,minitm,maxitm);
					}else if(postwy == E_CTRLWY.ATML){ //金额和次数控制
						
						chkDrawAmtControl(dramwy,tranam,drmiam,drmxam);
						chkDrawTimeControl(drtmwy,count,minitm,maxitm);
						
					}else{
						throw DpModuleError.DpstComm.BNAS0121();
					}
					
					tblKnaFxdr.setRedqam(tblKnaFxdr.getRedqam().add(tranam));
					tblKnaFxdr.setRedwnm(tblKnaFxdr.getRedwnm() + 1);					
				}else{
					tblKnaFxdr.setRedqam(tblKnaFxdr.getRedqam().add(tranam));
					tblKnaFxdr.setRedwnm(tblKnaFxdr.getRedwnm() + 1);
				}
				KnaFxdrDao.updateOne_odb1(tblKnaFxdr);
			}else{
				throw DpModuleError.DpstAcct.BNAS0843(fcflag.toString());
			}
												
				/**
				 * 支取计划登记
				 */
				if(E_YES___.YES==setpwy){
					if (E_FCFLAG.CURRENT == fcflag) {	//活期支取计划				
						tblKnaDrawPlan.setRedqam(tblKnaDrawPlan.getRedqam().add(tranam));
						tblKnaDrawPlan.setRedwnm(tblKnaDrawPlan.getRedwnm() + 1);
						/*-----------------------违约处理-----------------begin--------------*/
						if(drdfsd == E_DWBKLI.COUNT){//支取违约标准 按次数控制
							if(tblKnaDrawPlan.getRedwnm()> tblKnaDrawPlan.getPltime()){//实际支取次数大于计划总数-违约
								tblKnaDrawPlan.setDpbkfg(E_YES___.YES);
							}						
						}else if (drdfsd == E_DWBKLI.TOTAL){//支取违约标准 按金额控制
							if(CommUtil.compare(tblKnaDrawPlan.getRedqam(), tblKnaDrawPlan.getPlmony())>0){//实际支取次数大于计划总数-违约
								tblKnaDrawPlan.setDpbkfg(E_YES___.YES);
							}							
						}else if (drdfsd == E_DWBKLI.C_T){//支取违约标准 按次数和金额控制
							if(CommUtil.compare(tblKnaDrawPlan.getRedqam(), tblKnaDrawPlan.getPlmony())>0
									&& tblKnaDrawPlan.getRedwnm()> tblKnaDrawPlan.getPltime()){//实际支取次数大于计划总数或实际支取次数大于计划总数-违约
								tblKnaDrawPlan.setDpbkfg(E_YES___.YES);
							}						
						}else {
							throw DpModuleError.DpstAcct.BNAS1755( drcrwy);
						}
						/*-----------------------违约处理-----------------end--------------*/
						//支取控制表中的违约标志更新
						if(E_YES___.YES==tblKnaDrawPlan.getDpbkfg()){//只要有一个计划违约，则支取控制中违约标志改为违约
							tblKnaDraw.setDpbkfg(E_YES___.YES);
							KnaDrawDao.updateOne_odb1(tblKnaDraw);
						}						
						/*-----------------------计划处理状态更新-----------------begin--------------*/

						//达到计划支取总额
	
						if(CommUtil.compare(tblKnaDrawPlan.getRedqam(), tblKnaDrawPlan.getPlmony())>=0){
							tblKnaDrawPlan.setPlstat(E_PLSTAT.DLSU);//处理完成
						}else{
							tblKnaDrawPlan.setPlstat(E_PLSTAT.DLNG);//处理中
						}							
						/*-----------------------计划处理状态更新-----------------end--------------*/
						
						KnaDrawPlanDao.updateOne_odb1(tblKnaDrawPlan);
					} else if (E_FCFLAG.FIX == fcflag) {//定期支取计划
						tblKnaFxdrPlan.setRedqam(tblKnaFxdrPlan.getRedqam().add(tranam));
						tblKnaFxdrPlan.setRedwnm(tblKnaFxdrPlan.getRedwnm() + 1);
						/*-----------------------违约处理-----------------begin--------------*/
						if(drdfsd == E_DWBKLI.COUNT){//支取违约标准 按次数控制
							if(tblKnaFxdrPlan.getRedwnm()> tblKnaFxdrPlan.getPltime()){//实际支取次数大于计划总数-违约
								tblKnaFxdrPlan.setDpbkfg(E_YES___.YES);
							}						
						}else if (drdfsd == E_DWBKLI.TOTAL){//支取违约标准 按金额控制
							if(CommUtil.compare(tblKnaFxdrPlan.getRedqam(), tblKnaFxdrPlan.getPlmony())>0){//实际支取次数大于计划总数-违约
								tblKnaFxdrPlan.setDpbkfg(E_YES___.YES);
							}							
						}else if (drdfsd == E_DWBKLI.C_T){// 支取违约标准 按金额和次数控制
							if(CommUtil.compare(tblKnaFxdrPlan.getRedqam(), tblKnaFxdrPlan.getPlmony())>0
									&& tblKnaFxdrPlan.getRedwnm()> tblKnaFxdrPlan.getPltime()){//实际支取次数大于计划总数且实际支取次数大于计划总数-违约
								tblKnaFxdrPlan.setDpbkfg(E_YES___.YES);
							}						
						}else {
							throw DpModuleError.DpstAcct.BNAS1755(drcrwy);
						}
						
						/*-----------------------违约处理-----------------end--------------*/

						/*-----------------------计划处理状态更新-----------------begin--------------*/		
						//达到计划支取总额
						if(CommUtil.compare(tblKnaFxdrPlan.getRedqam(), tblKnaFxdrPlan.getPlmony())>=0){
							tblKnaFxdrPlan.setPlstat(E_PLSTAT.DLSU);//处理完成
						}else{
							tblKnaFxdrPlan.setPlstat(E_PLSTAT.DLNG);//处理中
						}	
						//如果支取计划结束日期前未完成，则违约						
						if(CommUtil.compare(CommTools.getBaseRunEnvs().getTrxn_date(), tblKnaFxdrPlan.getPloved())>0){
							tblKnaFxdrPlan.setDpbkfg(E_YES___.YES);
						}							
						
						KnaFxdrPlanDao.updateOne_odb1(tblKnaFxdrPlan);							
						/*-----------------------计划处理状态更新-----------------end--------------*/
						//支取控制表中的违约标志更新
						if(E_YES___.YES==tblKnaFxdrPlan.getDpbkfg()){//只要有一个计划违约，则支取控制中违约标志改为违约
							tblKnaFxdr.setDpbkfg(E_YES___.YES);
							KnaFxdrDao.updateOne_odb1(tblKnaFxdr);
						}

						
					} else {
						throw DpModuleError.DpstAcct.BNAS0843(fcflag.toString());
					}					
				}
		}else{
			throw DpModuleError.DpstAcct.BNAS1410();
		}
		
	}
	
	/**
	 * 存入金额控制
	 * @param amntwy 金额控制方式
	 * @param tranam 交易金额
	 * @param miniam 最小金额
	 * @param maxiam 最大金额
	 */
	public static void chkAmtControl(E_AMNTWY amntwy,BigDecimal tranam,BigDecimal miniam,BigDecimal maxiam){
		//存入金额检查
		//控制方式为最小金额
		if(E_AMNTWY.MNAC == amntwy){
			//交易金额小于最小存入金额
			if(CommUtil.compare(tranam,miniam)<0){
				throw DpModuleError.DpstProd.BNAS1718(tranam, miniam);
			}
		}else if(E_AMNTWY.MXAC == amntwy){
			//交易金额大于最大存入金额
			if(CommUtil.compare(tranam,maxiam)>0){
				throw DpModuleError.DpstProd.BNAS1719(tranam, maxiam);
			}
		}else if(E_AMNTWY.SCAC == amntwy){
			//交易金额不在最小存入金额和最大存入金额之间
			if(CommUtil.compare(tranam,miniam) < 0 || CommUtil.compare(tranam,maxiam) > 0){
				throw DpModuleError.DpstProd.BNAS1720(tranam, miniam, maxiam);
			}
		}else{
			throw DpModuleError.DpstAcct.BNAS1187(amntwy.toString());
		}
	}
	
	/**
	 * 支取金额控制
	 * @param amntwy 金额控制方式
	 * @param tranam 交易金额
	 * @param miniam 最小金额
	 * @param maxiam 最大金额
	 */
	public static void chkDrawAmtControl(E_AMNTWY amntwy,BigDecimal tranam,BigDecimal miniam,BigDecimal maxiam){
		//支取金额检查
		//控制方式为最小金额
		if(E_AMNTWY.MNAC == amntwy){
			//交易金额小于最小支取金额
			if(CommUtil.compare(tranam,miniam)<0){
				throw DpModuleError.DpstProd.BNAS1115(tranam, miniam);
			}
		}else if(E_AMNTWY.MXAC == amntwy){
			//交易金额大于最大支取金额
			if(CommUtil.compare(tranam,maxiam)>0){
				throw DpModuleError.DpstProd.BNAS1729(tranam, maxiam);
			}
		}else if(E_AMNTWY.SCAC == amntwy){
			//交易金额不在最小支取金额和最大支取金额之间
			if(CommUtil.compare(tranam,miniam) < 0 || CommUtil.compare(tranam,maxiam) > 0){
				throw DpModuleError.DpstProd.BNAS1114(tranam, miniam, maxiam);
			}
		}else{
			throw DpModuleError.DpstAcct.BNAS1140(amntwy.toString());
		}
	}
	
	/**
	 * 存入次数控制方式
	 * @param timewy 次数控制方式
	 * @param resvnm 实际次数
	 * @param minitm 最小次数
	 * @param maxitm 最大次数
	 */
	public static void chkTimeControl(E_TIMEWY timewy,Long resvnm,Long minitm,Long maxitm){
		//存入次数检查
		//存入方式为最小存入次数
		if(E_TIMEWY.MNTM == timewy){
			//存入次数小于最小存入次数
			if(resvnm < minitm){
				throw DpModuleError.DpstProd.BNAS1721(resvnm, minitm);
			}
		//存入方式为最大存入次数
		}else if(E_TIMEWY.MXTM == timewy){
			//存入次数大于最大存入次数
			if(resvnm > maxitm){
				throw DpModuleError.DpstProd.BNAS1722(resvnm, maxitm);
			}
		//存入方式为最小与最大次数之间
		}else if(E_TIMEWY.SCTM == timewy){
			if(resvnm < minitm || resvnm > maxitm){
				throw DpModuleError.DpstProd.BNAS1723(resvnm, minitm, maxitm);
			}
		}else{
			throw DpModuleError.DpstAcct.BNAS1411(timewy.toString()); 
		}
	}
	
	/**
	 * 支取次数控制方式
	 * @param timewy 次数控制方式
	 * @param resvnm 实际次数
	 * @param minitm 最小次数
	 * @param maxitm 最大次数
	 */
	public static void chkDrawTimeControl(E_TIMEWY timewy,Long resvnm,Long minitm,Long maxitm){
		//支取次数检查
		//支取方式为最小支取次数
		if(E_TIMEWY.MNTM == timewy){
			//支取次数小于最小支取次数
			if(resvnm < minitm){
				throw DpModuleError.DpstProd.BNAS1726(resvnm, minitm);
			}
		//支取方式为最大次数
		}else if(E_TIMEWY.MXTM == timewy){
			//支取次数大于最大支取次数
			if(resvnm > maxitm){
			    /*
			     * 2018/03/02，ouyt
			     * 根据业务修改提示语为：产品如果不允许提前支取，抛错只允许一次性全额支取，如果累计次数达到最大支取次数，抛错已累计多少次，先只能全额支取
			     * */
				if(maxitm == 0){
					throw DpModuleError.DpstAcct.BNAS1148();
				}
				if(maxitm >0){
					Long redwnm = resvnm -1; //累计支取次数
					throw DpModuleError.DpstProd.BNAS1727(redwnm);
				}
			}
		//支取方式为最小与最大次数之间
		}else if(E_TIMEWY.SCTM == timewy){
			if(resvnm < minitm || resvnm > maxitm){
				throw DpModuleError.DpstProd.BNAS1728(resvnm, minitm, maxitm);
			}
		}else{
			throw DpModuleError.DpstAcct.BNAS1411(timewy.toString()); 
		}
	}
	
	/**
	 * 根据产品编号查询产品信息（定期/活期均可）
	 * @param prodcd
	 * @return
	 */
	public static DppbDetailInfo getDppbInfo(String prodcd) {
		
		
		
		DppbDetailInfo pbinfo = SysUtil.getInstance(DppbDetailInfo.class);
		
		//获取产品基础信息
		KupDppb dppb = KupDppbDao.selectOne_odb1(prodcd,true);
		//已售产品份额
		long count;
		//检查产品属于活期还是定期
		if(E_FCFLAG.CURRENT == dppb.getPddpfg()){
			count = DpAcctQryDao.selKnaAcctCountByPordcd(prodcd, true);
		}else if(E_FCFLAG.FIX == dppb.getPddpfg()){
			count = DpAcctQryDao.selKnaFxacCountByPordcd(prodcd, true);
		}else{
			throw DpModuleError.DpstAcct.BNAS0843(dppb.getPddpfg().getLongName());
		}
		
		//查询产品详细信息
		List<DppbDetailInfo> dpinfos = DpProductDao.selProdDetailByprodcd(prodcd, false);
		if (dpinfos.size() <= 0) {
			throw DpModuleError.DpstProd.BNAS1055(prodcd);
		}
		pbinfo = dpinfos.get(0);
		
		if (pbinfo.getPresal() > 0 && count >= 0) {
			Long remanm = pbinfo.getPresal() - count;
			//剩余份数
			pbinfo.setRemanm(remanm);
		}
		return pbinfo;
	}
}
