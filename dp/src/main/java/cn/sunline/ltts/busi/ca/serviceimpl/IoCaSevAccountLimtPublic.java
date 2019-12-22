package cn.sunline.ltts.busi.ca.serviceimpl;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.CaPublic;
import cn.sunline.ltts.busi.ca.namedsql.AccountLimitDao;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KubTsscAcct;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupAcrtBrch;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtCmqtDao;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtCmsaDao;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtCmsvDao;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtLimt;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtSbac;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtServ;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountLimitInfo.IoAccLimitIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountLimitInfo.SelLimitOut;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTKD;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTST;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_PYTLTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.QtError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BRCHLV;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BRMPTP;

public class IoCaSevAccountLimtPublic {
	public static final BigDecimal LIMIT_DEFAULT = BigDecimal
			.valueOf(999999999999L);

	/**
	 * 
	 * 输入数据检查
	 */
	public static void accLimitInputCheck(IoAccLimitIn input) {
		if (CommUtil.isNull(input.getBrchno())) {
			throw QtError.Custa.BNASE099();
		}
		if (CommUtil.isNull(input.getAcctrt())) {

			throw QtError.Custa.BNASE094();

		}
		if (E_ACCTROUTTYPE.CORPORATION == input.getAcctrt()
				|| E_ACCTROUTTYPE.INTERBANK == input.getAcctrt()) {
			throw QtError.Custa.BNASE019();
		}
		if (CommUtil.isNull(input.getLimttp())) {
			throw QtError.Custa.BNASE117();
		}
		if (CommUtil.isNull(input.getLimtkd())) {
			throw QtError.Custa.BNASE111();
		}
		if (CommUtil.isNull(input.getServtp())) {
			throw QtError.Custa.BNASE051();
		}
		if (CommUtil.isNull(input.getAccttp())) {
			throw QtError.Custa.BNASE016();
		}
		
		if (!CommUtil.equals(input.getAccttp(), E_ACCATP.GLOBAL.getValue()) 
				&& !CommUtil.equals(input.getAccttp(), E_ACCATP.FINANCE.getValue())
				&& !CommUtil.equals(input.getAccttp(), E_ACCATP.WALLET.getValue())){
			throw QtError.Custa.BNASE017();
		}
		if ((CommUtil.equals(input.getAccttp(), E_ACCATP.GLOBAL.getValue()) 
				|| CommUtil.equals(input.getAccttp(), E_ACCATP.FINANCE.getValue())
				|| CommUtil.equals(input.getAccttp(), E_ACCATP.WALLET.getValue())) 
				&&CommUtil.isNull(input.getSbactp())) {
			throw QtError.Custa.BNASE005();
		}
		
		if ((CommUtil.equals(input.getAccttp(), E_ACCATP.GLOBAL.getValue()) 
				|| CommUtil.equals(input.getAccttp(), E_ACCATP.FINANCE.getValue())
				|| CommUtil.equals(input.getAccttp(), E_ACCATP.WALLET.getValue())) 
				&&CommUtil.isNotNull(input.getSbactp()) 
				&& CommUtil.isNull(KupCurtCmsaDao.selectOne_odb1(input.getSbactp(), false))) {
			throw QtError.Custa.BNASE006();
		}

		// 当账户分类为电子账户Ⅲ类户时，子账户类型可选项有“钱包账户”和“电子现金账户
//		if (CommUtil.equals(input.getAccttp(), E_ACCATP.WALLET.getValue())
//				&& (!(CommUtil.equals(input.getSbactp(),
//						E_SBACTP._12.getValue()) || CommUtil.equals(
//						input.getSbactp(), E_SBACTP._13.getValue())))) {
//			throw QtError.Custa
//					.E0001("当账户分类为电子账户Ⅲ类户时，子账户类型可选项有“钱包账户”和“电子现金账户”");
//		}

		// 当账户分类为电子账户II类户时，不得设置存现和取现限额
//		if (CommUtil.equals(input.getAccttp(), E_ACCATP.FINANCE.getValue())
//				&& (CommUtil.equals(input.getLimttp().toString(),
//						E_LIMTTP.DM.getValue()) || CommUtil.equals(input
//						.getLimttp().toString(), E_LIMTTP.DP.getValue()))) {
//			throw QtError.Custa.E0001("当账户分类为电子账户II类户时，不得设置存现和取现限额");
//		}

		// 当额度类型是转账时，子账户类型“电子现金账户”、“钱包账户”和“亲情钱包账户”不可选。
		// 当额度类型是存现、取现时，“电子现金账户”、“钱包账户”和“亲情钱包账户”不可选。

		// 当额度类型不为支付，支付工具必须为空
//		if (CommUtil.isNotNull(input.getPytltp())
//				&& input.getLimttp() != E_LIMTTP.PY.getValue()
//				&& input.getLimttp() != E_LIMTTP.TP.getValue()) {
//			throw QtError.Custa.E0001("额度类型不为支付，支付工具必须为空");
//		}
		if (CommUtil.isNull(input.getPytltp())) {
			throw QtError.Custa.BNASE052();
		}
		if (!CommUtil.equals(input.getLimttp(), E_LIMTTP.PY.getValue()) 
				&& !CommUtil.equals(input.getPytltp().getValue(),E_PYTLTP._99.getValue())) {
			throw QtError.Custa.BNASE115();
		}

//		if ((input.getLimttp() == E_LIMTTP.TR.getValue()
//				|| input.getLimttp() == E_LIMTTP.DM.getValue() || input.getLimttp() == E_LIMTTP.DP.getValue())
//				&& (CommUtil.equals(input.getSbactp(), E_SBACTP._12.getValue())
//						|| CommUtil.equals(input.getSbactp(),
//								E_SBACTP._13.getValue()) || CommUtil.equals(
//						input.getSbactp(), E_SBACTP._14.getValue()))) {
//			throw QtError.Custa
//					.E0001("当额度类型转账，存现，取现时，子账户类型不能选“电子现金账户”、“钱包账户”和“亲情钱包账户”");
//		}

		// if (input.getLimttp() == E_LIMTTP.PY &&
		// CommUtil.isNull(input.getPytltp())) {
		// throw QtError.Custa.E0001("额度类型为支付时，支付工具不能为空");
		// }
		if (CommUtil.compare(input.getLmtmax(), BigDecimal.ZERO) <= 0) {
			throw QtError.Custa.BNASE076();
		}
		if (CommUtil.compare(input.getLmtmin(), BigDecimal.ZERO) < 0) {
			throw QtError.Custa.BNASE073();
		}
		if (CommUtil.compare(input.getLmtmax(), input.getLmtmin()) < 0) {
//			throw QtError.Custa.BNASE075();
			throw DpModuleError.DpstComm.E9999("可设区间(最大值)必须大于等于可设区间(最小值)");
		}
		if (CommUtil.compare(input.getLmtval(), BigDecimal.ZERO) == 0) {
			throw QtError.Custa.BNASE026();
		}
		if (CommUtil.compare(input.getLmtval(), input.getLmtmin()) < 0
				|| CommUtil.compare(input.getLmtval(), input.getLmtmax()) > 0) {
			throw QtError.Custa.BNASE027();
		}
		if (CommUtil.isNull(KupCurtCmqtDao.selectOne_odb1(input.getLimttp(), false))) {
	    	throw QtError.Custa.BNASE154();
	    }
	    
	    if (CommUtil.isNull(KupCurtCmsvDao.selectOne_odb1(input.getServtp(), false))) {
	    	throw QtError.Custa.BNASE002();
	    }
	}

	/**
	 * 
	 * 生成序号
	 */
	public static String genAcrtsq(String str) {
		return BusiTools.getSequence(str, 20, "0");
	}

	public static String genAcrtno(String str) {
		return BusiTools.getSequence(str, 10, "0");
	}


	public static void accLimitContrlCheck2(IoAccLimitIn input) {
		String brchno = input.getBrchno();// 机构号
		E_ACCTROUTTYPE acctrt = input.getAcctrt();// 客户类型
		String limttp = input.getLimttp();// 额度类型
		E_LIMTKD limtkd = input.getLimtkd();// 额度种类
		String servtp = input.getServtp();// 渠道
		String accttp = input.getAccttp();// 账户分类
		String sbactp = input.getSbactp();// 子账户类型
		E_PYTLTP pytltp = input.getPytltp();// 支付工具
		E_LIMTST limtst = E_LIMTST.NL;// 记录状态
//		BigDecimal lmtmax = input.getLmtmax();// 可设区间(最大值)
//		BigDecimal lmtmin = input.getLmtmin();// 可设区间(最小值)
		BigDecimal lmtval = input.getLmtval();// 限额

		/**
		 * 判断是否同条件下单笔金额<=日累计金额 <=月累计金额<=年累计金额 日累计次数<=月累计次数<=年累计次数
		 * 
		 */
		// List<KupAcrtBrch> brch1 = KupAcrtBrchDao.selectAll_odb3(brchno,
		// custtp, limttp, servtp, accttp, sbactp, pytltp, limtst, false);
		List<KupAcrtBrch> brch1 = AccountLimitDao.selAcrtBrchAll(brchno,
				acctrt, limttp, servtp, accttp, sbactp, pytltp, false);
		if (brch1.size() > 0) {

			for (KupAcrtBrch curtbrch : brch1) {
				// 当额度种类为金额时
				if (CommUtil.compare(curtbrch.getLimtkd().getValue(),E_LIMTKD._11.getValue()) < 0
						&& CommUtil.compare(limtkd.getValue(),E_LIMTKD._11.getValue()) < 0) {
					// 输入的额度种类小于同条件的额度种类，区间和限额不能超过已设的区间和限额。
					if (CommUtil.compare(limtkd.getValue(), curtbrch
							.getLimtkd().getValue()) < 0) {
						// 当额度类型小与其他记录时 额度上限 、下限 限额 都要小于该记录
//						if (CommUtil.compare(lmtmax, curtbrch.getLmtmax()) > 0) {
//							throw QtError.Custa
//									.E0001("同条件下 额度上限 、下限  限额  单笔金额<=日累计金额 <=月累计金额<=年累计金额   ");
//						}
//						if (CommUtil.compare(lmtmin, curtbrch.getLmtmin()) > 0) {
//							throw QtError.Custa
//									.E0001("同条件下 额度上限 、下限  限额  单笔金额<=日累计金额 <=月累计金额<=年累计金额   ");
//						}
						if (CommUtil.compare(lmtval, curtbrch.getLmtval()) > 0) {
							throw QtError.Custa
									.BNASE039();
						}
					}

					if (CommUtil.compare(limtkd.getValue(), curtbrch
							.getLimtkd().getValue()) > 0) {
						// 当额度类型大与其他记录时 额度上限 、下限 限额 都要大于该记录
//						if (CommUtil.compare(lmtmax, curtbrch.getLmtmax()) < 0) {
//							throw QtError.Custa
//									.E0001("同条件下 额度上限 、下限  限额  单笔金额<=日累计金额 <=月累计金额<=年累计金额   ");
//						}
//						if (CommUtil.compare(lmtmin, curtbrch.getLmtmin()) < 0) {
//							throw QtError.Custa
//									.E0001("同条件下 额度上限 、下限  限额  单笔金额<=日累计金额 <=月累计金额<=年累计金额   ");
//						}
						if (CommUtil.compare(lmtval, curtbrch.getLmtval()) < 0) {
							throw QtError.Custa
									.BNASE039();
						}

					}
					// if (CommUtil.compare(limtkd.getValue(),
					// curtbrch.getLimtkd().getValue()) == 0) {
					// throw QtError.Custa.E0001("该限额已设置   ");
					// }

				}
				// 当额度种类为次数时
				if (CommUtil.compare(curtbrch.getLimtkd().getValue(),E_LIMTKD._04.getValue()) > 0
						&& CommUtil.compare(curtbrch.getLimtkd().getValue(),E_LIMTKD._21.getValue()) < 0
						&& CommUtil.compare(limtkd.getValue(),E_LIMTKD._04.getValue()) > 0
						&& CommUtil.compare(limtkd.getValue(),E_LIMTKD._21.getValue())< 0) {
					if (CommUtil.compare(limtkd.getValue(), curtbrch
							.getLimtkd().getValue()) < 0) {
						// 当额度类型小与其他记录时 额度上限 、下限 限额 都要小于该记录
//						if (CommUtil.compare(lmtmax, curtbrch.getLmtmax()) > 0) {
//							throw QtError.Custa
//									.E0001("同条件下 额度上限 、下限  限额  单笔金额<=日累计金额 <=月累计金额<=年累计金额   ");
//						}
//						if (CommUtil.compare(lmtmin, curtbrch.getLmtmin()) < 0) {
//							throw QtError.Custa
//									.E0001("同条件下 额度上限 、下限  限额  单笔金额<=日累计金额 <=月累计金额<=年累计金额   ");
//						}
						if (CommUtil.compare(lmtval, curtbrch.getLmtval()) > 0) {
							throw QtError.Custa
									.BNASE038();
						}

					}

					if (CommUtil.compare(limtkd.getValue(), curtbrch
							.getLimtkd().getValue()) > 0) {
						// 当额度类型大与其他记录时 额度上限 、下限 限额 都要大于该记录
//						if (CommUtil.compare(lmtmax, curtbrch.getLmtmax()) < 0) {
//							throw QtError.Custa
//									.E0001("同条件下 额度上限 、下限  限额  单笔金额<=日累计金额 <=月累计金额<=年累计金额   ");
//						}
//						if (CommUtil.compare(lmtmin, curtbrch.getLmtmin()) > 0) {
//							throw QtError.Custa
//									.E0001("同条件下 额度上限 、下限  限额  单笔金额<=日累计金额 <=月累计金额<=年累计金额   ");
//						}
						if (CommUtil.compare(lmtval, curtbrch.getLmtval()) < 0) {
							throw QtError.Custa
									.BNASE038();
						}

					}
					// if (CommUtil.compare(limtkd.getValue(),
					// curtbrch.getLimtkd().getValue()) == 0) {
					// throw QtError.Custa.E0001("该限额已设置   ");
					// }
				}
			}
		}
		
		boolean sevFlag = true; // 渠道为组合
		boolean limFlag = true; // 额度类型为组合
		boolean sbtFlag = true; // 子账户类型为组合
		/**
		 * 渠道组合限额值大于渠道限额值
		 */
		List<KupCurtServ> listServ = AccountLimitDao.selCurtServ("'"+servtp+"'", null, false);
		
		//传入渠道值
		String servtps = CaPublic.QryCmsvtps(servtp);
		if(CommUtil.isNotNull(servtps)){
			servtps = servtps.replace(servtp, "");
		}
		if(listServ.size() > 0 && CommUtil.equals(listServ.get(0).getCmsvtp(), listServ.get(0).getServtp())){
			sevFlag = false;
			if(!CommUtil.equals(servtps, "''")){
				KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(brchno,acctrt, "'"+limttp+"'",limtkd, servtps, accttp, "'"+sbactp+"'", pytltp,limtst, false);
				if(CommUtil.isNotNull(acrtbrch) && CommUtil.compare(acrtbrch.getLmtval(), lmtval) <= 0){
					throw QtError.Custa.BNASE047();
				}
			}
		}
		//传入渠道组合值
		StringBuffer sbfsev = new StringBuffer();
		if(listServ.size() > 0
				&& !CommUtil.equals(listServ.get(0).getCmsvtp(), listServ.get(0).getServtp())){
			for (KupCurtServ tmps:listServ) {
				if(sbfsev.length()>0){
					sbfsev.append(",'").append(tmps.getServtp()).append("'");
				}else{
					sbfsev.append("'").append(tmps.getServtp()).append("'");
				}   
			}
			KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(brchno,acctrt, "'"+limttp+"'",limtkd, sbfsev.toString(), accttp, "'"+sbactp+"'", pytltp,limtst, false);
			if(CommUtil.isNotNull(acrtbrch) && CommUtil.compare(acrtbrch.getLmtval(), lmtval) >= 0){
				throw QtError.Custa.BNASE047();
			}
		}
		
		/**
		 * 额度类型组合限额值大于额度类型限额值
		 */
		List<KupCurtLimt> listLimt = AccountLimitDao.selCurtLimt(null, "'"+limttp+"'", false);
		
		//判断传入额度类型值是否为组合值
		//传入额度类型值
		String limttps = CaPublic.QryCmqttps(limttp);
		if(CommUtil.isNotNull(limttps)){
			limttps = limttps.replace(limttp, "");
		}
		if (listLimt.size() > 0 && CommUtil.equals(listLimt.get(0).getCmqttp(), listLimt.get(0).getLimttp())) {
			limFlag = false;
			if(!CommUtil.equals(limttps, "''")){
				KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(brchno,
						acctrt, limttps, limtkd, "'"+servtp+"'", accttp, "'"+sbactp+"'", pytltp,
						limtst, false);
				if (CommUtil.isNotNull(acrtbrch)
						&& CommUtil.compare(acrtbrch.getLmtval(), lmtval) <= 0) {
					throw QtError.Custa.BNASE113();
				}
			}
		}
		// 传入额度类型组合值
		StringBuffer sbfLim = new StringBuffer();
		if (listLimt.size() > 0 && !CommUtil.equals(listLimt.get(0)
				.getCmqttp(), listLimt.get(0).getLimttp())) {
			
			for (KupCurtLimt tmps : listLimt) {
				if (sbfLim.length() > 0) {
					sbfLim.append(",'").append(tmps.getLimttp()).append("'");
				} else {
					sbfLim.append("'").append(tmps.getLimttp()).append("'");
				}
			}
			KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(brchno,
					acctrt, sbfLim.toString(), limtkd, "'"+servtp+"'", accttp, "'"+sbactp+"'", pytltp,
					limtst, false);
			if (CommUtil.isNotNull(acrtbrch)
					&& CommUtil.compare(acrtbrch.getLmtval(), lmtval) >= 0) {
				throw QtError.Custa.BNASE113();
			}

		}
		// add by cjun 20170227
//		if(sevFlag != limFlag){
//			throw QtError.Custa.E0001("渠道与额度类型必须同时设置为组合或单一类型");
//		}
		
		// 渠道与额度类型同时为组合或同时不为组合时
		if(sevFlag == true && limFlag == true && listServ.size() > 0 && listLimt.size() > 0){
			KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(brchno,
					acctrt, sbfLim.toString(), limtkd, sbfsev.toString(), accttp, "'"+sbactp+"'", pytltp,
					limtst, false);
			if (CommUtil.isNotNull(acrtbrch)
					&& CommUtil.compare(acrtbrch.getLmtval(), lmtval) >= 0) {
				throw QtError.Custa.BNASE113();
			}
		}
		
		if(sevFlag == false && limFlag == false){
			if((!CommUtil.equals(limttps, "''")) && (!CommUtil.equals(servtps, ""))){
				KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(brchno,
						acctrt, limttps, limtkd, servtps, accttp, "'"+sbactp+"'", pytltp,
						limtst, false);
				if (CommUtil.isNotNull(acrtbrch)
						&& CommUtil.compare(acrtbrch.getLmtval(), lmtval) <= 0) {
					throw QtError.Custa.BNASE113();
				}
			}
		}
		
		/**
		 * 账户子类型组合限额值大于子账户限额值
		 */
		List<KupCurtSbac> listSbac = AccountLimitDao.selCurtSmb("'"+sbactp+"'", null, false);
		
		//判断传入账户子类型值是否为组合值
		//传入账户子类型值
		String sbactps = CaPublic.QrySbattps(sbactp);
		if(CommUtil.isNotNull(sbactps)){
			sbactps = sbactps.replace(sbactp, "");
		}
		if (listSbac.size() > 0 && CommUtil.equals(listSbac.get(0).getCmsatp(), listSbac.get(0).getSbactp())) {
			sbtFlag = false;
			if(!CommUtil.equals(sbactps, "''")){
				KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(brchno,
						acctrt, "'"+limttp+"'", limtkd, "'"+servtp+"'", accttp, sbactps, pytltp,
						limtst, false);
				if (CommUtil.isNotNull(acrtbrch)
						&& CommUtil.compare(acrtbrch.getLmtval(), lmtval) <= 0) { //子账户组合限额-子账户限额<=0
					throw QtError.Custa.BNASE003();
				}
			}
		}
		// 传入账户子类型组合值
		StringBuffer sbfsb = new StringBuffer();
		if (listSbac.size() > 0 && !CommUtil.equals(listSbac.get(0)
				.getCmsatp(), listSbac.get(0).getSbactp())) {
			for (KupCurtSbac tmps : listSbac) {
				if (sbfsb.length() > 0) {
					sbfsb.append(",'").append(tmps.getSbactp()).append("'");
				} else {
					sbfsb.append("'").append(tmps.getSbactp()).append("'");
				}
			}
			KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(brchno,
					acctrt, "'"+limttp+"'", limtkd, "'"+servtp+"'", accttp, sbfsb.toString(), pytltp,
					limtst, false);
			if (CommUtil.isNotNull(acrtbrch)
					&& CommUtil.compare(acrtbrch.getLmtval(), lmtval) >= 0) {
				throw QtError.Custa.BNASE003();
			}

		}
		
		// 渠道与额度类型、子账户类型同时为组合或同时不为组合时
		if(sevFlag == true && limFlag == true &&  sbtFlag == true && listServ.size() > 0 && listLimt.size() > 0 && listSbac.size() > 0){
			KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(brchno,
					acctrt, sbfLim.toString(), limtkd, sbfsev.toString(), accttp, sbfsb.toString(), pytltp,
					limtst, false);
			if (CommUtil.isNotNull(acrtbrch)
					&& CommUtil.compare(acrtbrch.getLmtval(), lmtval) >= 0) {
				throw QtError.Custa.BNASE113();
			}
		}
		if(sevFlag == false && limFlag == false &&  sbtFlag == false){
			if((!CommUtil.equals(limttps, "''")) && (!CommUtil.equals(servtps, ""))&& (!CommUtil.equals(sbactps, ""))){
				KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(brchno,
						acctrt, limttps, limtkd, servtps, accttp, sbactps, pytltp,
						limtst, false);
				if (CommUtil.isNotNull(acrtbrch)
						&& CommUtil.compare(acrtbrch.getLmtval(), lmtval) <= 0) {
					throw QtError.Custa.BNASE113();
				}
			}
		}
	}
	
	/*
	 * 新增记录时，相同或互斥记录检查
	 */
	public static void checkSameRecord(IoAccLimitIn input) {
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
		E_ACCTROUTTYPE acctrt = input.getAcctrt();
		String limttp = input.getLimttp();
		E_LIMTKD limtkd = input.getLimtkd();
		String servtp = input.getServtp();
		String accttp = input.getAccttp();
		String strSbactps = input.getSbactp();
		E_PYTLTP pytltp = input.getPytltp();
		E_LIMTST limtst = E_LIMTST.NL;

		List<KupAcrtBrch> listBrch = AccountLimitDao.selAcrtBrchAll2(
				tranbr, acctrt, "'"+limttp+"'", limtkd, "'"+servtp+"'", accttp, "'"+strSbactps+"'",
				pytltp, limtst, false);
		// 如果根据子账户类型能够查到记录，即出现互斥
		if (listBrch.size() > 0) {
			throw QtError.Custa.BNASE079();
		}
	}
	
	/*
	 * 修改记录时，相同或互斥记录检查
	 */
	public static void checkSameRecord2(IoAccLimitIn input) {
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
		E_ACCTROUTTYPE acctrt = input.getAcctrt();
		String limttp = input.getLimttp();
		E_LIMTKD limtkd = input.getLimtkd();
		String servtp = input.getServtp();
		String accttp = input.getAccttp();
		String strSbactps = input.getSbactp();
		E_PYTLTP pytltp = input.getPytltp();
		E_LIMTST limtst = E_LIMTST.NL;// 记录状态

		KupAcrtBrch selAcrtBrch = AccountLimitDao.selAcrtBrchBysq(
				input.getAcrtsq(), input.getBrchno(), false);
		if (CommUtil.isNull(selAcrtBrch)) {
			throw QtError.Custa.BNASE015();
		}
		
		List<KupAcrtBrch> listBrch = AccountLimitDao.selAcrtBrchAll2(tranbr, acctrt, "'"+limttp+"'", limtkd, 
				"'"+servtp+"'", accttp,"'"+strSbactps+"'", pytltp, limtst, false);
		
		for (KupAcrtBrch brchtmp : listBrch) {
			if(!CommUtil.equals(selAcrtBrch.getAcrtsq(), brchtmp.getAcrtsq())){
				throw QtError.Custa.BNASE079();
			}
		}
	}

	/**
	 * 县级行社检查
	 */
	public static void checkXianJiInfo(IoAccLimitIn input,KupAcrtBrch selAcrtBrch) {
		if (CommUtil.isNull(input.getAcctrt())) {
			throw QtError.Custa.BNASE094();
		}
		if (CommUtil.isNull(input.getLimttp())) {
			throw QtError.Custa.BNASE117();
		}
		if (CommUtil.isNull(input.getLimtkd())) {
			throw QtError.Custa.BNASE111();
		}
		if (CommUtil.isNull(input.getServtp())) {
			throw QtError.Custa.BNASE051();
		}
		if (CommUtil.isNull(input.getAccttp())) {
			throw QtError.Custa.BNASE016();
		}
		if ((CommUtil.equals(input.getAccttp(), E_ACCATP.GLOBAL.getValue()) 
				|| CommUtil.equals(input.getAccttp(), E_ACCATP.FINANCE.getValue())
				|| CommUtil.equals(input.getAccttp(), E_ACCATP.WALLET.getValue())) 
				&&CommUtil.isNull(input.getSbactp())) {
			throw QtError.Custa.BNASE005();
		}
	
		if (CommUtil.isNull(input.getPytltp())) {
			throw QtError.Custa.BNASE052();
		}
		
		if (CommUtil.compare(input.getLmtval(), BigDecimal.ZERO) == 0) {
			throw QtError.Custa.BNASE026();
		}
		if (!CommUtil.equals(input.getAcctrt().toString(), selAcrtBrch
				.getAcctrt().toString())) {
			throw QtError.Custa.BNASE068();
		}

		if (!CommUtil.equals(input.getLimttp().toString(), selAcrtBrch
				.getLimttp().toString())) {
			throw QtError.Custa.BNASE116();
		}

		if (!CommUtil.equals(input.getLimtkd().toString(), selAcrtBrch
				.getLimtkd().toString())) {
			throw QtError.Custa.BNASE112();
		}

		if (!CommUtil.equals(input.getSbactp(), selAcrtBrch.getSbactp())) {
			throw QtError.Custa.BNASE004();
		}

		if (CommUtil.isNotNull(input.getPytltp())
				&& input.getPytltp() != selAcrtBrch.getPytltp()) {
			throw QtError.Custa.BNASE010();
		}

		if (!CommUtil.equals(input.getServtp(), selAcrtBrch.getServtp())) {
			throw QtError.Custa.BNASE050();
		}

		if (!CommUtil.equals(input.getLmtmax(), selAcrtBrch.getLmtmax())) {
			throw QtError.Custa.BNASE080();
		}

		if (!CommUtil.equals(input.getLmtmin(), selAcrtBrch.getLmtmin())) {
			throw QtError.Custa.BNASE128();
		}

		if (CommUtil.compare(input.getLmtval(), selAcrtBrch.getLmtmax()) > 0
				|| CommUtil.compare(input.getLmtval(), selAcrtBrch.getLmtmin()) < 0) {
			throw QtError.Custa.BNASE027();
		}
	}

	/**
	 * 获取账户限额
	 * 
	 * @param brchno
	 * @param custtp
	 * @param limttp
	 * @param limtkd
	 * @param servtp
	 * @param accttp
	 * @param sbactp
	 * @param pytltp
	 * @return 限额值
	 */
	public static SelLimitOut getAcrtLimitVal(String brchno, E_ACCTROUTTYPE acctrt,
			E_LIMTTP limttp, E_LIMTKD limtkd, String servtp, E_ACCATP accttp,
			E_SBACTP sbactp, E_PYTLTP pytltp) {
		SelLimitOut limitout = null;

		if (limttp == E_LIMTTP.PY || limttp == E_LIMTTP.TR) { // 支付 或 转账
			limitout = AccountLimitDao.selAcrtLimitAppPayOrTrans(brchno,
					acctrt, limttp, limtkd, servtp, accttp, sbactp, pytltp,
					false);

			// 查找上级机构配置
			if (CommUtil.isNull(limitout)) {
				E_BRCHLV brchlv = SysUtil.getInstance(IoSrvPbBranch.class)
						.getBranch(brchno).getBrchlv();
				if (brchlv == E_BRCHLV.COUNT) {
					String upbrch = SysUtil.getInstance(IoSrvPbBranch.class)
							.getUpprBranch(brchno, E_BRMPTP.M, BusiTools.getDefineCurrency())
							.getBrchno();
					limitout = AccountLimitDao.selAcrtLimitAppPayOrTrans(
							upbrch, acctrt, limttp, limtkd, servtp, accttp,
							sbactp, pytltp, false);
				}
			}

		} else {

			limitout = AccountLimitDao.selAcrtLimitApp(brchno, acctrt, limttp,
					limtkd, servtp, accttp, sbactp, pytltp, false);

			// 查找上级机构配置
			if (CommUtil.isNull(limitout)) {
				E_BRCHLV brchlv = SysUtil.getInstance(IoSrvPbBranch.class)
						.getBranch(brchno).getBrchlv();
				if (brchlv == E_BRCHLV.COUNT) {
					String upbrch = SysUtil.getInstance(IoSrvPbBranch.class)
							.getUpprBranch(brchno, E_BRMPTP.M, BusiTools.getDefineCurrency())
							.getBrchno();
					limitout = AccountLimitDao.selAcrtLimitApp(upbrch, acctrt,
							limttp, limtkd, servtp, accttp, sbactp, pytltp,
							false);
				}
			}
		}

		return limitout;
	}
	
	/**
	 *  查询行社设置账户限额最小限额值记录
	 * @param brchno
	 * @param custtp
	 * @param limttp
	 * @param limtkd
	 * @param servtp
	 * @param accttp
	 * @param sbactp
	 * @param pytltp
	 * @param limtst
	 * @return
	 */
	public static KupAcrtBrch getAcrtLimitMin(String brchno, E_ACCTROUTTYPE acctrt,
			String limttp, E_LIMTKD limtkd, String servtp, String accttp,
			String sbactp, E_PYTLTP pytltp,E_LIMTST limtst) {

			KupAcrtBrch acrt_brch = AccountLimitDao.selAcrtBrchOne(brchno, acctrt, limttp, limtkd, servtp, accttp, sbactp, pytltp, limtst, false);

			// 查找上级机构配置
			if (CommUtil.isNull(acrt_brch)) {
				E_BRCHLV brchlv = SysUtil.getInstance(IoSrvPbBranch.class)
						.getBranch(brchno).getBrchlv();
				if (brchlv == E_BRCHLV.COUNT) {
					String upbrch = SysUtil.getInstance(IoSrvPbBranch.class)
							.getUpprBranch(brchno, E_BRMPTP.M, BusiTools.getDefineCurrency())
							.getBrchno();
					
					acrt_brch = AccountLimitDao.selAcrtBrchOne(upbrch, acctrt, limttp, limtkd, servtp, accttp, sbactp, pytltp, limtst, false);
				}
			}
		return acrt_brch;
	}

	/**
	 * 根据组合类型获取账户限额
	 * 
	 * @param brchno
	 * @param custtp
	 * @param limttp
	 * @param limtkd
	 * @param servtp
	 * @param accttp
	 * @param sbactp
	 * @param pytltp
	 * @return 限额值
	 */
	public static SelLimitOut getAcrtLimitValCM(String brchno, E_ACCTROUTTYPE acctrt,
			String limttp, E_LIMTKD limtkd, String servtp, E_ACCATP accttp,
			String sbactp, E_PYTLTP pytltp) {
		SelLimitOut limitout = null;

		limitout = AccountLimitDao.selAcrtLimitAppPayOrTrans2(brchno, acctrt,
				limttp, limtkd, servtp, accttp, sbactp, pytltp, false);

		// 查找上级机构配置
		SelLimitOut limitoutPro = SysUtil.getInstance(SelLimitOut.class);
		E_BRCHLV brchlv = SysUtil.getInstance(IoSrvPbBranch.class)
				.getBranch(brchno).getBrchlv();
		if (brchlv == E_BRCHLV.COUNT) {
			String upbrch = SysUtil.getInstance(IoSrvPbBranch.class)
					.getUpprBranch(brchno, E_BRMPTP.M, BusiTools.getDefineCurrency())
					.getBrchno();
			limitoutPro = AccountLimitDao.selAcrtLimitAppPayOrTrans2(upbrch,
					acctrt, limttp, limtkd, servtp, accttp, sbactp, pytltp,
					false);
		}
		
		//比较上级机构限额与交易机构限额值，满足上级机构限额编号不与下级机构限额编号相同（下级限额不由该上级限额维护所得），限额编号取较小值返回
		if (CommUtil.isNotNull(limitoutPro) && CommUtil.isNotNull(limitout) && !CommUtil.equals(limitout.getAcrtno(), limitoutPro.getAcrtno())) {
			if (CommUtil.isNotNull(limitoutPro.getLmtval()) && CommUtil.isNotNull(limitout)){
				if (CommUtil.compare(limitoutPro.getLmtval(), limitout.getLmtval()) < 0) {
					limitout.setLmtval(limitoutPro.getLmtval());
				}
			}
		}
		//交易机构（下级）查询为空，返回上级机构
		if (CommUtil.isNull(limitout) && CommUtil.isNotNull(limitoutPro)) {
			return limitoutPro;
		} else {
			return limitout;
		}
		
	}

	public static BigDecimal setValueIfZero(BigDecimal val) {
		if (CommUtil.compare(val, BigDecimal.ZERO) == 0) {
			return LIMIT_DEFAULT;
		} else {
			return val;
		}
	}



	/**
	 * 县级行社维护值检查
	 * 
	 * @param proBrchno
	 *            省联社机构号
	 * @param couBrchno
	 *            县级行社机构号
	 * @param input
	 */
	public static void accLimitContrlCheck3(String proBrchno, String couBrchno,
			IoAccLimitIn input) {
		E_ACCTROUTTYPE acctrt = input.getAcctrt();// 客户类型
		String limttp = input.getLimttp();// 额度类型
		E_LIMTKD limtkd = input.getLimtkd();// 额度种类
		String servtp = input.getServtp();// 渠道
		String accttp = input.getAccttp();// 账户分类
		String sbactp = input.getSbactp();// 子账户类型
		E_PYTLTP pytltp = input.getPytltp();// 支付工具
		BigDecimal lmtval = input.getLmtval();// 限额
		E_LIMTST limtst = E_LIMTST.NL;

		/**
		 * 判断是否同条件下单笔金额<=日累计金额 <=月累计金额<=年累计金额 日累计次数<=月累计次数<=年累计次数 县级行社只能设置额度限额
		 */
		List<KupAcrtBrch> proAcrt = AccountLimitDao.selAcrtBrchAll(proBrchno,acctrt, limttp, servtp, accttp, sbactp, pytltp, false);

		List<KupAcrtBrch> couAcrt = AccountLimitDao.selAcrtBrchAll(couBrchno,acctrt, limttp, servtp, accttp, sbactp, pytltp, false);
		if (proAcrt.size() > 0) {
			for (KupAcrtBrch curtbrch : proAcrt) {
					// 如果县级行社有设置限额则按县级行社的判断
					for (KupAcrtBrch curtbrch2 : couAcrt) {
						if (CommUtil.equals(curtbrch.getServtp(),
								curtbrch2.getServtp())
								&& CommUtil.equals(curtbrch.getSbactp(),curtbrch2.getSbactp())
								&& CommUtil.compare(curtbrch.getPytltp(),curtbrch2.getPytltp()) == 0
								&& CommUtil.compare(curtbrch.getLimttp(),curtbrch2.getLimttp()) == 0
								&& CommUtil.compare(curtbrch.getAccttp(),curtbrch2.getAccttp()) == 0
								&& CommUtil.compare(curtbrch.getLimtkd(),curtbrch2.getLimtkd()) == 0) {
							curtbrch.setLmtval(curtbrch2.getLmtval());
						}
					}
			}
			for (KupAcrtBrch curtbrch : proAcrt) {
				// 当额度种类为金额时
				if (CommUtil.compare(curtbrch.getLimtkd().getValue(),E_LIMTKD._11.getValue()) < 0
						&& CommUtil.compare(limtkd.getValue(),E_LIMTKD._11.getValue()) < 0 ) {
					// 输入的额度种类小于同条件的额度种类，限额不能超过已设的限额。
					if (CommUtil.compare(limtkd.getValue(), curtbrch.getLimtkd().getValue()) < 0) {
						// 当额度类型小与其他记录时 额度 限额 都要小于该记录
						if (CommUtil.compare(lmtval, curtbrch.getLmtval()) > 0) {
							throw QtError.Custa.BNASE039();
						}
					}

					if (CommUtil.compare(limtkd.getValue(), curtbrch.getLimtkd().getValue()) > 0) {
						// 当额度类型大与其他记录时 额度 限额 都要大于该记录
						if (CommUtil.compare(lmtval, curtbrch.getLmtval()) < 0) {
							throw QtError.Custa.BNASE039();
						}
					}
				}
				// 当额度种类为次数时
				if (CommUtil.compare(curtbrch.getLimtkd().getValue(),E_LIMTKD._04.getValue()) > 0
						&& CommUtil.compare(curtbrch.getLimtkd().getValue(),E_LIMTKD._21.getValue()) < 0
						&& CommUtil.compare(limtkd.getValue(),E_LIMTKD._04.getValue()) > 0
						&& CommUtil.compare(limtkd.getValue(),E_LIMTKD._21.getValue()) < 0) {
					if (CommUtil.compare(limtkd.getValue(), curtbrch.getLimtkd().getValue()) < 0) {
						// 当额度类型小与其他记录时 额度 限额 都要小于该记录
						if (CommUtil.compare(lmtval, curtbrch.getLmtval()) > 0) {
							throw QtError.Custa.BNASE038();
						}
					}

					if (CommUtil.compare(limtkd.getValue(), curtbrch.getLimtkd().getValue()) > 0) {
						// 当额度类型大与其他记录时 额度 限额 都要大于该记录
						if (CommUtil.compare(lmtval, curtbrch.getLmtval()) < 0) {
							throw QtError.Custa.BNASE038();
						}
					}
				}
			}
		}
		
		boolean sevFlag = true; // 渠道为组合
		boolean limFlag = true; // 额度类型为组合
		boolean sbtFlag = true; // 子账户类型为组合
		/**
		 * 渠道组合限额值大于渠道限额值
		 */
		String servtps = CaPublic.QryCmsvtps(servtp);
		if(CommUtil.isNotNull(servtps)){
			servtps = servtps.replace(servtp, "");
		}
		List<KupCurtServ> listServ = AccountLimitDao.selCurtServ("'"+servtp+"'", null, false);
		//传入渠道值
		if(listServ.size() > 0 
				&& CommUtil.equals(listServ.get(0).getCmsvtp(), listServ.get(0).getServtp())){
			sevFlag = false;
			if(!CommUtil.equals(servtps, "''")){
				KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(couBrchno,acctrt, "'"+limttp+"'",limtkd, servtps, accttp, "'"+sbactp+"'", pytltp,limtst, false);
				if(CommUtil.isNull(acrtbrch)){
					acrtbrch = AccountLimitDao.selAcrtBrchOne(proBrchno,acctrt, "'"+limttp+"'",limtkd, servtps, accttp, "'"+sbactp+"'", pytltp,limtst, false);
				}
				if(CommUtil.isNotNull(acrtbrch) && CommUtil.compare(acrtbrch.getLmtval(), lmtval) <= 0){
					throw QtError.Custa.BNASE047();
				}
			}
		}
		//传入渠道组合值
		StringBuffer sbfsev = new StringBuffer();
		if(listServ.size() > 0
				&& !CommUtil.equals(listServ.get(0).getCmsvtp(), listServ.get(0).getServtp())){
			for (KupCurtServ tmps:listServ) {
				if(sbfsev.length()>0){
					sbfsev.append(",'").append(tmps.getServtp()).append("'");
				}else{
					sbfsev.append("'").append(tmps.getServtp()).append("'");
				}   
			}
			KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(couBrchno,acctrt, "'"+limttp+"'",limtkd, sbfsev.toString(), accttp, "'"+sbactp+"'", pytltp,limtst, false);
			if(CommUtil.isNull(acrtbrch)){
				acrtbrch = AccountLimitDao.selAcrtBrchOne(proBrchno,acctrt, "'"+limttp+"'",limtkd, sbfsev.toString(), accttp, "'"+sbactp+"'", pytltp,limtst, false);
			}
			if(CommUtil.isNotNull(acrtbrch) && CommUtil.compare(acrtbrch.getLmtval(), lmtval) >= 0){
				throw QtError.Custa.BNASE047();
			}
			
		}
		
		/**
		 * 额度类型组合限额值大于额度类型限额值
		 */
		List<KupCurtLimt> listLimt = AccountLimitDao.selCurtLimt(null, "'"+limttp+"'", false);
		
		//判断传入额度类型值是否为组合值
		//传入额度类型值
		String limttps = CaPublic.QryCmqttps(limttp);
		if(CommUtil.isNotNull(limttps)){
			limttps = limttps.replace(limttp, "");
		}
		if (listLimt.size() > 0
				&& CommUtil.equals(listLimt.get(0).getCmqttp(), listLimt.get(0).getLimttp())) {
			limFlag = false;
			if(!CommUtil.equals(limttps, "''")){
				KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(couBrchno,acctrt, limttps, limtkd, "'"+servtp+"'", accttp, "'"+sbactp+"'", pytltp,limtst, false);
				if(CommUtil.isNull(acrtbrch)){
					acrtbrch = AccountLimitDao.selAcrtBrchOne(proBrchno,acctrt, limttps, limtkd, "'"+servtp+"'", accttp, "'"+sbactp+"'", pytltp,limtst, false);
				}
				if (CommUtil.isNotNull(acrtbrch)
						&& CommUtil.compare(acrtbrch.getLmtval(), lmtval) <= 0) {
					throw QtError.Custa.BNASE113();
				}
			}
		}
		// 传入额度类型组合值
		StringBuffer sbfLim = new StringBuffer();
		if (listLimt.size() > 0 && !CommUtil.equals(listLimt.get(0)
				.getCmqttp(), listLimt.get(0).getLimttp())) {
			
			for (KupCurtLimt tmps : listLimt) {
				if (sbfLim.length() > 0) {
					sbfLim.append(",'").append(tmps.getLimttp()).append("'");
				} else {
					sbfLim.append("'").append(tmps.getLimttp()).append("'");
				}
			}
			KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(couBrchno,acctrt, sbfLim.toString(), limtkd, "'"+servtp+"'", accttp, "'"+sbactp+"'", pytltp,limtst, false);
			if(CommUtil.isNull(acrtbrch)){
				acrtbrch = AccountLimitDao.selAcrtBrchOne(proBrchno,acctrt, sbfLim.toString(), limtkd, "'"+servtp+"'", accttp, "'"+sbactp+"'", pytltp,limtst, false);
			}
			if (CommUtil.isNotNull(acrtbrch)
					&& CommUtil.compare(acrtbrch.getLmtval(), lmtval) >= 0) {
				throw QtError.Custa.BNASE113();
			}
		}
		// add by cjun 20170227
//		if(sevFlag != limFlag){
//			throw QtError.Custa.E0001("渠道与额度类型必须同时设置为组合或单一类型");
//		}
		
		// 渠道与额度类型同时为组合或同时不为组合时
		if(sevFlag == true && limFlag == true && listServ.size() > 0 && listLimt.size() > 0){
			KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(couBrchno,
					acctrt, sbfLim.toString(), limtkd, sbfsev.toString(), accttp, "'"+sbactp+"'", pytltp,
					limtst, false);
			if(CommUtil.isNull(acrtbrch)){
				acrtbrch = AccountLimitDao.selAcrtBrchOne(proBrchno,acctrt, sbfLim.toString(), limtkd, sbfsev.toString(), accttp, "'"+sbactp+"'", pytltp,limtst, false);
			}
			if (CommUtil.isNotNull(acrtbrch)
					&& CommUtil.compare(acrtbrch.getLmtval(), lmtval) >= 0) {
				throw QtError.Custa.BNASE113();
			}
		}
		if(sevFlag == false && limFlag == false){
			if((!CommUtil.equals(limttps, "''")) && (!CommUtil.equals(servtps, ""))){
				KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(couBrchno,acctrt, limttps, limtkd, servtps, accttp, "'"+sbactp+"'", pytltp,limtst, false);
				if(CommUtil.isNull(acrtbrch)){
					acrtbrch = AccountLimitDao.selAcrtBrchOne(proBrchno,acctrt, limttps, limtkd, servtps, accttp, "'"+sbactp+"'", pytltp,limtst, false);
				}
				if (CommUtil.isNotNull(acrtbrch)
						&& CommUtil.compare(acrtbrch.getLmtval(), lmtval) <= 0) {
					throw QtError.Custa.BNASE113();
				}
			}
		}
		
		/**
		 * 账户子类型组合限额值大于子账户限额值
		 */
		List<KupCurtSbac> listSbac = AccountLimitDao.selCurtSmb("'"+sbactp+"'", null, false);
		
		//判断传入账户子类型值是否为组合值
		//传入账户子类型值
		String sbactps = CaPublic.QrySbattps(sbactp);
		if(CommUtil.isNotNull(sbactps)){
			sbactps = sbactps.replace(sbactp, "");
		}
		if (listSbac.size() > 0
				&& CommUtil.equals(listSbac.get(0).getCmsatp(), listSbac.get(0)
						.getSbactp())) {
			sbtFlag = false;
			if(!CommUtil.equals(sbactps, "''")){
				KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(couBrchno,acctrt, "'"+limttp+"'", limtkd, "'"+servtp+"'", accttp, sbactps, pytltp,limtst, false);
				if(CommUtil.isNull(acrtbrch)){
					acrtbrch = AccountLimitDao.selAcrtBrchOne(proBrchno,acctrt, "'"+limttp+"'", limtkd, "'"+servtp+"'", accttp, sbactps, pytltp,limtst, false);
				}
				if (CommUtil.isNotNull(acrtbrch)
						&& CommUtil.compare(acrtbrch.getLmtval(), lmtval) <= 0) {
					throw QtError.Custa.BNASE003();
				}
			}
		}
		// 传入账户子类型组合值
		StringBuffer sbfsb = new StringBuffer();
		if (listSbac.size() > 0 && !CommUtil.equals(listSbac.get(0)
				.getCmsatp(), listSbac.get(0).getSbactp())) {
			for (KupCurtSbac tmps : listSbac) {
				if (sbfsb.length() > 0) {
					sbfsb.append(",'").append(tmps.getSbactp()).append("'");
				} else {
					sbfsb.append("'").append(tmps.getSbactp()).append("'");
				}
			}
			KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(couBrchno,acctrt, "'"+limttp+"'", limtkd, "'"+servtp+"'", accttp, sbfsb.toString(), pytltp,limtst, false);
			if(CommUtil.isNull(acrtbrch)){
				acrtbrch = AccountLimitDao.selAcrtBrchOne(proBrchno,acctrt, "'"+limttp+"'", limtkd, "'"+servtp+"'", accttp, sbfsb.toString(), pytltp,limtst, false);
			}
			if (CommUtil.isNotNull(acrtbrch) && CommUtil.compare(acrtbrch.getLmtval(), lmtval) >= 0) {
				throw QtError.Custa.BNASE003();
			}
		}
		
		// 渠道与额度类型、子账户类型同时为组合或同时不为组合时
		if(sevFlag == true && limFlag == true &&  sbtFlag == true && listServ.size() > 0 && listLimt.size() > 0 && listSbac.size() > 0){
			KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(couBrchno,
					acctrt, sbfLim.toString(), limtkd, sbfsev.toString(), accttp, sbfsb.toString(), pytltp,
					limtst, false);
			if(CommUtil.isNull(acrtbrch)){
				acrtbrch = AccountLimitDao.selAcrtBrchOne(proBrchno,
						acctrt, sbfLim.toString(), limtkd, sbfsev.toString(), accttp, sbfsb.toString(), pytltp,
						limtst, false);
			}
			if (CommUtil.isNotNull(acrtbrch)
					&& CommUtil.compare(acrtbrch.getLmtval(), lmtval) >= 0) {
				throw QtError.Custa.BNASE113();
			}
		}
		if(sevFlag == false && limFlag == false &&  sbtFlag == false){
			if((!CommUtil.equals(limttps, "''")) && (!CommUtil.equals(servtps, ""))&& (!CommUtil.equals(sbactps, ""))){
				KupAcrtBrch acrtbrch = AccountLimitDao.selAcrtBrchOne(couBrchno,
						acctrt, limttps, limtkd, servtps, accttp, sbactps, pytltp,
						limtst, false);
				if(CommUtil.isNull(acrtbrch)){
					acrtbrch = AccountLimitDao.selAcrtBrchOne(proBrchno,
							acctrt, limttps, limtkd, servtps, accttp, sbactps, pytltp,
							limtst, false);
				}
				if (CommUtil.isNotNull(acrtbrch)
						&& CommUtil.compare(acrtbrch.getLmtval(), lmtval) <= 0) {
					throw QtError.Custa.BNASE113();
				}
			}
		}
	}
	
	/**
	 * 计算账户累计额
	 * @param limttp
	 * @param servtp
	 * @param sbactp
	 * @param limtkd
	 * @param currdt
	 * @param acctList
	 * @return
	 */
	@SuppressWarnings("incomplete-switch")
	public static BigDecimal getTsscAccVal(String limttp,String servtp,String sbactp,E_LIMTKD limtkd,String currdt,List<KubTsscAcct> acctList) {
		BigDecimal talVal = BigDecimal.ZERO;
		for(KubTsscAcct tmp:acctList){
			if(limttp.indexOf("'"+tmp.getLimttp().getValue()+"'")>=0 
					&& servtp.indexOf("'"+tmp.getServtp()+"'")>=0 
						&& sbactp.indexOf("'"+tmp.getSbactp().getValue()+"'")>=0){
				switch (limtkd) {
				case _02: // 日累计金额
					if ( currdt.equals(tmp.getLastdt())) {
						talVal = talVal.add(tmp.getDytlam());
					}
					break;
				case _11: // 日累计次数
					if (currdt.equals(tmp.getLastdt())) {
						talVal = talVal.add(BigDecimal.valueOf(tmp.getDytltm()));
					}
					break;
				case _03: // 月累计金额
					if (currdt.substring(0, 6).equals(tmp.getLastdt().substring(0, 6))) {
						talVal = talVal.add(tmp.getMhtlam());
					}
					break;
				case _12: // 月累计次数
					if ( currdt.substring(0, 6).equals(
							tmp.getLastdt().substring(0, 6))) {
						talVal = talVal.add(BigDecimal.valueOf(tmp.getMhtltm()));
					}
					break;
				case _04: // 年累计金额
					if ( currdt.substring(0, 4).equals(tmp.getLastdt().substring(0, 4))) {
						talVal = talVal.add(tmp.getYrtlam());
					}
					break;
				case _13: // 年累计次数
					if ( currdt.substring(0, 4).equals(
							tmp.getLastdt().substring(0, 4))) {
						talVal = talVal.add(BigDecimal.valueOf(tmp.getYrtltm()));
					}
					break;
				}
			}
		}
		
		return talVal;
	}
	
	/**
	 * 查询组合下明细信息
	 * @param list
	 * @param pastr
	 * @param mode,0 渠道，1 额度类型，2 子账户类型
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getLowerList(List list,String pastr,String mode){
		StringBuffer sbf = new StringBuffer();
		for(Object obj:list){
			if(mode.equals("0")){
				KupCurtServ sev = (KupCurtServ) obj;
				if(pastr.equals(sev.getCmsvtp())){
					sbf.append("'").append(sev.getServtp()).append("'");
				}
			}else if(mode.equals("1")){
				KupCurtLimt limt = (KupCurtLimt) obj;
				if(pastr.equals(limt.getCmqttp())){
					sbf.append("'").append(limt.getLimttp()).append("'");
				}
			}else if(mode.equals("2")){
				KupCurtSbac sbac = (KupCurtSbac) obj;
				if(pastr.equals(sbac.getCmsatp())){
					sbf.append("'").append(sbac.getSbactp()).append("'");
				}
			}
		}
		return sbf.toString();
	}
	
	
}
