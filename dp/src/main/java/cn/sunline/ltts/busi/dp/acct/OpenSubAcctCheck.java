package cn.sunline.ltts.busi.dp.acct;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcct;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcctDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActpDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCust;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCustDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfir;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfirDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDraw;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawPlan;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawPlanDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntr;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntrDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbMatu;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbMatuDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPost;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostPlan;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostPlanDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpBasePart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpBrchPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCustPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDfirPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDppbPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDrawPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDrawplPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpGentTab;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpIntrPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpMatuPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpOpenSub;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpPostPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpPostplPart;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstProd;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DRAWCT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRRTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MADTBY;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SAVECT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SPECTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVBKAD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVBKLI;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVPLGN;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRSVTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AMNTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BRCHFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BSINDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CTRLWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DRRULE;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IBAMMD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INADTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INEDSC;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_ONLYFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PLANFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_POSTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_REPRWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TEARTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TIMEWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRINWY;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;

/**
 * 描述:开负债子账户处理
 * 
 * */
public class OpenSubAcctCheck {

	private static BizLog log = BizLogUtil.getBizLog(OpenSubAcctCheck.class);

	/**
	 * 属性间关系检查
	 * */
	public static void DealTransBefore(IoDpOpenSub openInfo) {

		IoDpBasePart base = openInfo.getBase(); // 开户基础属性
		IoDpDppbPart dppb = openInfo.getDppb(); // 产品基础属性
		IoDpCustPart cust = openInfo.getCust(); // 开户控制
		Options<IoDpBrchPart> brch = openInfo.getBrch(); // 机构控制
		IoDpPostPart post = openInfo.getPost(); // 存入控制
		IoDpDrawPart draw = openInfo.getDraw(); // 支取控制
		IoDpIntrPart intr = openInfo.getIntr(); // 利率利息

		openInfo.getBase().setAcctno(null); // 子账号初始化为空

		openInfo.getBase().setPddpfg(openInfo.getDppb().getProdmt());

		// 属性间关系检查。
		if (CommUtil.isNull(dppb.getProdbt())) {
			throw DpModuleError.DpstComm.BNAS0255();
		}
		if (CommUtil.isNull(dppb.getProdmt())) {
			throw DpModuleError.DpstComm.BNAS0258();
		}
		if (CommUtil.isNull(dppb.getProdlt())) {
			throw DpModuleError.DpstComm.BNAS0262();
		}

		if (CommUtil.isNull(cust.getMadtby())) {
			throw DpModuleError.DpstComm.BNAS0967();
		}
		if (CommUtil.isNull(intr.getInprwy())) {
			throw DpModuleError.DpstComm.BNAS0476();
		}
		if (E_MADTBY.SET == cust.getMadtby()) {
			if (CommUtil.isNotNull(base.getDepttm())) {
				throw DpModuleError.DpstComm.BNAS0965();
			}
		}
		// if (E_MADTBY.NO == cust.getMadtby()) {
		// if (CommUtil.isNotNull(base.getDepttm())) {
		// if (E_TERMCD.T000 != base.getDepttm()) {
		// throw DpModuleError.DpstComm.E9999("到期日确定方式为1-无到期日存期智能选000或空");
		// }
		// }
		// }
		if (E_MADTBY.NO != cust.getMadtby() && E_MADTBY.SET != cust.getMadtby()) {
			if (CommUtil.isNull(base.getDepttm())) {
				throw DpModuleError.DpstProd.BNAS1025();
			}

			// 存期检查
			if (CommUtil.equals(base.getDepttm().getValue().substring(0, 1), "9")) {
				if (CommUtil.isNull(base.getDeptdy()) || base.getDeptdy() <= 0) {
					throw DpModuleError.DpstComm.BNAS0015();
				}
			}
		}

		// 业务中类和业务小类的检查
		String a = dppb.getProdbt().getValue();
		String b = dppb.getProdmt().getValue().substring(0, 2);
		if (!CommUtil.equals(a, b)) {
			throw DpModuleError.DpstComm.BNAS1048();
		}

		// 业务小类和业务细类的检查
		a = dppb.getProdmt().getValue();
		b = dppb.getProdlt().getValue().substring(0, 4);
		if (!CommUtil.equals(a, b)) {
			throw DpModuleError.DpstComm.BNAS0260();
		}

		// 产品中类检查
		if (dppb.getProdmt() == E_FCFLAG.CURRENT) {
			if (cust.getMadtby() != E_MADTBY.NO) { // 活期产品不检查到期日确定方式
				throw DpModuleError.DpstComm.BNAS0966();
			}
			if (intr.getInprwy() == E_IRRTTP.MT || intr.getInprwy() == E_IRRTTP.QD) {
				throw DpModuleError.DpstComm.BNAS0475();
			}
			if (dppb.getProdlt() == E_DEBTTP.DP2401) {
				if (base.getDepttm() != E_TERMCD.T000) { //
					throw DpModuleError.DpstComm.BNAS1024();
				}
			}
		} else if (dppb.getProdmt() == E_FCFLAG.FIX) {

			if (intr.getInprwy() != E_IRRTTP.MT || intr.getInprwy() != E_IRRTTP.QD) {
				// throw DpModuleError.DpstComm.E9999("利率重订价方式输入有误");
			}
		}

		// 机构币种检查，该机构是否开通该币种业务
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
		String crcycd = openInfo.getBase().getCrcycd();
		/*
		 * JF Modify：外调。
		SysUtil.getInstance(IoSrvPbBranch.class).inspectBranchLegality(tranbr, crcycd);
		 */
		SysUtil.getRemoteInstance(IoSrvPbBranch.class).inspectBranchLegality(tranbr, crcycd);

		// 机构控制标志检查
		if (dppb.getBrchfg() == E_BRCHFG.USE) {
			if (CommUtil.isNotNull(brch) && brch.size() <= 0) {
				throw DpModuleError.DpstComm.BNAS0654();
			} else {
				boolean isable = false;
				for (IoDpBrchPart brchpt : brch) {
					if (CommUtil.equals(brchpt.getBrchno(), tranbr)) {
						isable = true;
					}
				}
				if (!isable) {
					throw DpModuleError.DpstComm.BNAS0980();
				}

			}
		}
		// 开户控制检查
		// if (CommUtil.isNotNull(base.getDepttm())) {
		// if (cust.getMadtby() == E_MADTBY.NO
		// && base.getDepttm() != E_TERMCD.T000) {
		// throw DpModuleError.DpstComm.E9999("存期输入有误");
		// }
		// }

		if (cust.getMadtby() == E_MADTBY.NO && CommUtil.isNull(base.getDepttm())) {
			throw DpModuleError.DpstProd.BNAS1025();
		}
		// //检查指定到期日
		// if (cust.getMadtby() == E_MADTBY.SET &&
		// CommUtil.isNull(cust.getMatudt())) {
		// throw DpModuleError.DpstComm.E9027("指定到期日");
		// }
		// 到期日确定方式为根据首次存入日和开户日确定时，检查存期
		if ((cust.getMadtby() == E_MADTBY.T_OR_S || cust.getMadtby() == E_MADTBY.TERMCD)
				&& base.getDepttm() == E_TERMCD.T000) {
			throw DpModuleError.DpstComm.BNAS1024();
		}
		// 存入计划中的明细汇总与值计划中的支取规则检查
		if (post.getPosttp() == E_SAVECT.COND) {
			if (post.getDetlfg() == E_YES___.YES) {

				if (CommUtil.isNull(draw.getDrrule())) {
					throw DpModuleError.DpstComm.BNAS0134();
				}

				if (draw.getDrrule() != E_DRRULE.HJXC && draw.getDrrule() != E_DRRULE.XJXC) {
					throw DpModuleError.DpstComm.BNAS0133();
				}
			}
		}
		// 存入控制检查
		if (CommUtil.isNotNull(openInfo.getPost())) {
			CheckPost(post);
		}
		// 支取控制检查
		if (CommUtil.isNotNull(openInfo.getDraw())) {
			CheckDraw(draw);
		}

		// 存入计划检查
		if (CommUtil.isNotNull(openInfo.getPostpl())) {
			CheckPostpl(openInfo);
		}

		// 支取计划控制检查
		if (CommUtil.isNotNull(openInfo.getDrawpl())) {
			CheckDrawpl(openInfo);
		}
		// 违约支取控制检查
		if (CommUtil.isNotNull(openInfo.getDfir()) && openInfo.getDfir().size() > 0) {
			CheckDfir(openInfo);
		}
		// 利率利息部件检查
		CheckIntr(openInfo);
		// 到期控制检查
		if (CommUtil.isNotNull(openInfo.getMatu())) {
			CheckMatu(openInfo);
		}
	}

	/**
	 * 存入控制检查
	 * */
	public static void CheckPost(IoDpPostPart post) {

		if (post.getPosttp() == E_SAVECT.COND) { // 存入控制方式

			if (CommUtil.isNull(post.getPostwy())) {
				throw DpModuleError.DpstComm.BNAS1009();
			}

			boolean am = false;
			boolean tm = false;
			if (post.getPostwy() == E_POSTWY.AMCL) { // 存入控制方法

				if (CommUtil.isNull(post.getAmntwy())) {
					throw DpModuleError.DpstComm.BNAS1012();
				}
				am = true;
			} else if (post.getPostwy() == E_POSTWY.TMCL) {

				if (CommUtil.isNull(post.getTimewy())) {
					throw DpModuleError.DpstComm.BNAS1021();
				}
				tm = true;
			} else if (post.getPostwy() == E_POSTWY.ATMC) {

				if (CommUtil.isNull(post.getAmntwy())) {
					throw DpModuleError.DpstComm.BNAS1012();
				}

				if (CommUtil.isNull(post.getTimewy())) {
					throw DpModuleError.DpstComm.BNAS1021();
				}

				am = true;
				tm = true;
			} else {
				throw DpModuleError.DpstComm.BNAS1008();
			}
			if (am) {
				if (post.getAmntwy() == E_AMNTWY.MNAC) {
					if (CommUtil.isNull(post.getMiniam())) {
						throw DpModuleError.DpstComm.BNAS0986();
					}
				} else if (post.getAmntwy() == E_AMNTWY.MXAC) {
					if (CommUtil.isNull(post.getMaxiam())) {
						throw DpModuleError.DpstComm.BNAS0987();
					}
				} else if (post.getAmntwy() == E_AMNTWY.SCAC) {
					if (CommUtil.isNull(post.getMiniam())) {
						throw DpModuleError.DpstComm.BNAS0986();
					}
					if (CommUtil.isNull(post.getMaxiam())) {
						throw DpModuleError.DpstComm.BNAS0987();
					}
				} else {
					throw DpModuleError.DpstComm.BNAS1011();
				}
			}
			if (tm) {
				if (post.getTimewy() == E_TIMEWY.MNTM) {
					if (CommUtil.isNull(post.getMinitm())) {
						throw DpModuleError.DpstComm.BNAS0004();
					}
				} else if (post.getTimewy() == E_TIMEWY.MXTM) {
					if (CommUtil.isNull(post.getMaxitm())) {
						throw DpModuleError.DpstComm.BNAS0009();
					}
				} else if (post.getTimewy() == E_TIMEWY.SCTM) {
					if (CommUtil.isNull(post.getMinitm())) {
						throw DpModuleError.DpstComm.BNAS0004();
					}
					if (CommUtil.isNull(post.getMaxitm())) {
						throw DpModuleError.DpstComm.BNAS0009();
					}
				} else {
					throw DpModuleError.DpstComm.BNAS1021();
				}
			}
		}
	}

	public static void CheckPostpl(IoDpOpenSub openInfo) {

		IoDpPostplPart postpl = openInfo.getPostpl();
		Options<IoDpGentTab> postpldt = openInfo.getPostpldt();
		IoDpBasePart base = openInfo.getBase();
		// IoDpPostplPart postpl,Options<IoDpGentTab> postpldt
		if (base.getDepttm() == E_TERMCD.T000 && postpl.getPlanfg() == E_PLANFG.SET) {
			throw DpModuleError.DpstComm.BNAS1023();
		}
		if (CommUtil.isNull(postpl.getPlanfg()) || postpl.getPlanfg() == E_PLANFG.NOSET) {
			log.debug("不设置存入计划");
		} else if (postpl.getPlanfg() == E_PLANFG.SET) {

			if (CommUtil.isNull(postpl.getGentwy())) {
				throw DpModuleError.DpstComm.BNAS1014();
			}
			if (postpl.getGentwy() == E_SVPLGN.T1) {
				if (CommUtil.isNull(postpl.getPlanpd())) {
					throw DpModuleError.DpstComm.BNAS1013();
				}
				DateTools2.chkFrequence(postpl.getPlanpd());
			} else if (postpl.getGentwy() == E_SVPLGN.T2) {

				if (postpldt.size() <= 0) {
					throw DpModuleError.DpstComm.BNAS1213();
				}
			}

			if (CommUtil.isNull(postpl.getSvlewy())) {
				throw DpModuleError.DpstComm.BNAS1006();
			}
			if (postpl.getSvlewy() == E_SVBKAD.COUNT) {

				if (CommUtil.isNull(postpl.getMaxisp())) {
					throw DpModuleError.DpstComm.BNAS0010();
				}
			}
			if (CommUtil.isNull(postpl.getDfltsd())) {
				throw DpModuleError.DpstComm.BNAS1004();
			}
			if (postpl.getDfltsd() == E_SVBKLI.COUNT) {

				if (CommUtil.isNull(postpl.getSvletm())) {
					throw DpModuleError.DpstComm.BNAS0468();
				}
			}
			if (CommUtil.isNull(postpl.getDfltwy())) {
				throw DpModuleError.DpstComm.BNAS1002();
			}
			if (CommUtil.isNull(postpl.getPscrwy())) {
				throw DpModuleError.DpstComm.BNAS1017();
			}

			if (CommUtil.isNull(postpl.getPsamtp())) {
				throw DpModuleError.DpstComm.BNAS1019();
			}

		} else {
			throw DpModuleError.DpstComm.BNAS1015();
		}
	}

	public static void CheckDraw(IoDpDrawPart draw) {
		if (draw.getDrawtp() == E_DRAWCT.COND) {

			if (CommUtil.isNull(draw.getCtrlwy())) {
				throw DpModuleError.DpstComm.BNAS0122();
			}
			boolean am = false;
			boolean tm = false;
			if (draw.getCtrlwy() == E_CTRLWY.AMCL) {

				if (CommUtil.isNull(draw.getDramwy())) {
					throw DpModuleError.DpstComm.BNAS0125();
				}
				am = true;
			} else if (draw.getCtrlwy() == E_CTRLWY.TMCL) {

				if (CommUtil.isNull(draw.getDrtmwy())) {
					throw DpModuleError.DpstComm.BNAS0137();
				}
				tm = true;
			} else if (draw.getCtrlwy() == E_CTRLWY.ATML) {

				if (CommUtil.isNull(draw.getDramwy())) {
					throw DpModuleError.DpstComm.BNAS0125();
				}
				if (CommUtil.isNull(draw.getDrtmwy())) {
					throw DpModuleError.DpstComm.BNAS0137();
				}
				am = true;
				tm = true;
			} else {
				throw DpModuleError.DpstComm.BNAS0121();
			}
			if (am) {
				if (draw.getDramwy() == E_AMNTWY.MNAC) {

					if (CommUtil.isNull(draw.getDrmiam())) {
						throw DpModuleError.DpstComm.BNAS0983();
					}
				} else if (draw.getDramwy() == E_AMNTWY.MXAC) {

					if (CommUtil.isNull(draw.getDrmxam())) {
						throw DpModuleError.DpstComm.BNAS0985();
					}
				} else if (draw.getDramwy() == E_AMNTWY.SCAC) {
					if (CommUtil.isNull(draw.getDrmiam())) {
						throw DpModuleError.DpstComm.BNAS0983();
					}
					if (CommUtil.isNull(draw.getDrmxam())) {
						throw DpModuleError.DpstComm.BNAS0985();
					}
					if (CommUtil.compare(draw.getDrmxam(), draw.getDrmiam()) < 0) {
						throw DpModuleError.DpstComm.BNAS0984();
					}
				} else {
					throw DpModuleError.DpstComm.BNAS0124();
				}
			}
			if (tm) {
				if (draw.getDrtmwy() == E_TIMEWY.MNTM) {

					if (CommUtil.isNull(draw.getDrmitm())) {
						throw DpModuleError.DpstComm.BNAS0002();
					}
				} else if (draw.getDrtmwy() == E_TIMEWY.MXTM) {

					if (CommUtil.isNull(draw.getDrmxtm())) {
						throw DpModuleError.DpstComm.BNAS0006();
					}
				} else if (draw.getDrtmwy() == E_TIMEWY.SCTM) {

					if (CommUtil.isNull(draw.getDrmitm())) {
						throw DpModuleError.DpstComm.BNAS0002();
					}
					if (CommUtil.isNull(draw.getDrmxtm())) {
						throw DpModuleError.DpstComm.BNAS0006();
					}
					if (CommUtil.compare(draw.getDrmxtm(), draw.getDrmitm()) < 0) {
						throw DpModuleError.DpstComm.BNAS0005();
					}
				} else {
					throw DpModuleError.DpstComm.BNAS0136();
				}
			}
			if ((CommUtil.isNull(draw.getMinibl()) || CommUtil.compare(draw.getMinibl(), BigDecimal.ZERO) == 0)
					&& draw.getIsmibl() != E_YES___.NO) {
				throw DpModuleError.DpstComm.BNAS0345();
			} else {

				if (CommUtil.isNull(draw.getIsmibl())) {
					throw DpModuleError.DpstComm.BNAS0345();
				}
				if (draw.getIsmibl() != E_YES___.NO && draw.getIsmibl() != E_YES___.YES) {
					throw DpModuleError.DpstComm.BNAS0345();
				}
			}
		}
	}

	public static void CheckDrawpl(IoDpOpenSub openInfo) {

		IoDpDrawplPart drawpl = openInfo.getDrawpl();
		Options<IoDpGentTab> drawpldt = openInfo.getDrawpldt();
		IoDpBasePart base = openInfo.getBase();
		// IoDpPostplPart postpl,Options<IoDpGentTab> postpldt
		if (base.getDepttm() == E_TERMCD.T000 && drawpl.getSetpwy() == E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS1022();
		}

		if (CommUtil.isNull(drawpl.getSetpwy()) || drawpl.getSetpwy() == E_YES___.NO) {
			log.debug("不设置支取计划");
		} else if (drawpl.getSetpwy() == E_YES___.YES) {
			if (CommUtil.isNull(drawpl.getDradwy())) {
				throw DpModuleError.DpstComm.BNAS0128();
			}
			if (drawpl.getDradwy() == E_SVPLGN.T1) {
				if (CommUtil.isNull(drawpl.getGendpd())) {
					throw DpModuleError.DpstComm.BNAS0126();
				}
				DateTools2.chkFrequence(drawpl.getGendpd());
			} else if (drawpl.getDradwy() == E_SVPLGN.T2) {
				if (drawpldt.size() <= 0) {
					throw DpModuleError.DpstComm.BNAS1212();
				} else {

				}
			}
			if (CommUtil.isNull(drawpl.getDrcrwy())) {
				throw DpModuleError.DpstComm.BNAS0131();
			}
			if (CommUtil.isNull(drawpl.getDrdfsd())) {
				throw DpModuleError.DpstComm.BNAS0116();
			}
			if (CommUtil.isNull(drawpl.getDrdfwy())) {
				throw DpModuleError.DpstComm.BNAS0114();
			}
			if (CommUtil.isNull(drawpl.getBeinfg())) {
				throw DpModuleError.DpstComm.BNAS0119();
			}
		} else {
			throw DpModuleError.DpstComm.BNAS0129();
		}
	}

	public static void CheckDfir(IoDpOpenSub open) {

		Options<IoDpDfirPart> dfirs = open.getDfir();
		Set<String> set = new HashSet<String>();
		for (IoDpDfirPart dfir : dfirs) {
			if (CommUtil.isNull(dfir.getTeartp())) {
				throw DpModuleError.DpstComm.BNAS1219();
			}
			if (set.contains(dfir.getTeartp().getValue())) {
				throw DpModuleError.DpstComm.BNAS1217();
			} else {
				set.add(dfir.getTeartp().getValue());
			}
			if (CommUtil.isNotNull(dfir.getTeartp())) { // 如果有设置违约支取利息类型
				if (CommUtil.isNull(dfir.getTeartp())) {
					throw DpModuleError.DpstComm.BNAS1218();
				}

				if (CommUtil.isNull(dfir.getIntrcd())) {
					throw DpModuleError.DpstComm.BNAS1223();
				}
				if (CommUtil.isNull(dfir.getBsinrl())) {
					throw DpModuleError.DpstComm.BNAS1230();
				}
				if (CommUtil.isNull(dfir.getInadtp())) {
					throw DpModuleError.DpstComm.BNAS1221();
				}
				if (CommUtil.isNull(dfir.getIntrdt())) {
					throw DpModuleError.DpstComm.BNAS0478();
				}
				if (CommUtil.isNull(dfir.getInsrwy())) {
					throw DpModuleError.DpstComm.BNAS1222();
				}
				if (CommUtil.isNull(dfir.getBsinam())) {
					throw DpModuleError.DpstComm.BNAS1227();
				}
				if (CommUtil.isNull(dfir.getBsindt())) {
					throw DpModuleError.DpstComm.BNAS1226();
				}
				if (CommUtil.isNull(dfir.getInedsc())) {
					throw DpModuleError.DpstComm.BNAS1225();
				}
				if (CommUtil.isNull(dfir.getDrdein())) {
					throw DpModuleError.DpstComm.BNAS0117();
				}

				if (dfir.getTeartp() == E_TEARTP.TQXH) {
					if (dfir.getInadtp() != E_INADTP.QETZ) {
						throw DpModuleError.DpstComm.BNAS1220();
					}
				}

				if (dfir.getTeartp() == E_TEARTP.TQZQ || dfir.getTeartp() == E_TEARTP.ZDLC
						|| dfir.getTeartp() == E_TEARTP.BTMN || dfir.getTeartp() == E_TEARTP.OVTM) {
					if (dfir.getInadtp() != E_INADTP.BFZQ) {
						throw DpModuleError.DpstComm.BNAS1220();
					}
				}

				if (dfir.getTeartp() == E_TEARTP.OVTM && !(dfir.getBsindt() == E_BSINDT.TMDT)
						&& !(dfir.getInedsc() == E_INEDSC.DQJY)) {
					throw DpModuleError.DpstComm.BNAS1228();
				}

				if (dfir.getTeartp() != E_TEARTP.OVTM && !(dfir.getInedsc() == E_INEDSC.DQJY)) {
					throw DpModuleError.DpstComm.BNAS1229();
				}

				if (E_IRCDTP.LAYER == dfir.getIncdtp()) {
					if (CommUtil.isNull(dfir.getInclfg())) {
						throw DpModuleError.DpstComm.BNAS1224();
					}
				}

				if (E_IRCDTP.Reference == dfir.getIncdtp() || E_IRCDTP.BASE == dfir.getIncdtp()) {
					if (CommUtil.isNotNull(dfir.getInclfg()) && dfir.getInclfg() == E_YES___.YES) {
						throw DpModuleError.DpstComm.BNAS0486();
					}
				}

				if (dfir.getInclfg() == E_YES___.YES) {
					if (CommUtil.isNull(dfir.getIntrwy())) {
						throw DpModuleError.DpstComm.BNAS0481();
					}

					if (CommUtil.isNull(dfir.getLevety())) {
						throw DpModuleError.DpstComm.BNAS0551();
					}
				}
			}
		}
	}

	public static void CheckIntr(IoDpOpenSub open) {

		IoDpIntrPart intr = open.getIntr();
		if (CommUtil.isNull(intr.getIntrcd())) {
			throw DpModuleError.DpstComm.BNAS0489();
		}
		if (CommUtil.isNull(intr.getIntrtp())) {
			throw DpModuleError.DpstComm.BNAS0473();
		}
		if (CommUtil.isNull(intr.getInbefg())) {
			throw DpModuleError.DpstComm.BNAS0647();
		}
		if (intr.getInbefg() == E_INBEFG.INBE && CommUtil.isNull(intr.getTebehz())) {

			throw DpModuleError.DpstComm.BNAS0640();
		}
		if (CommUtil.isNull(intr.getTxbefg())) {
			throw DpModuleError.DpstComm.BNAS0649();
		}
		if (intr.getTxbefg() == E_YES___.YES && CommUtil.isNull(intr.getTaxecd())) {

			throw DpModuleError.DpstComm.BNAS1245();
		}
		if (CommUtil.isNull(intr.getTxbebs())) {
			throw DpModuleError.DpstComm.BNAS0643();
		}
		if (CommUtil.isNull(intr.getHutxfg())) {
			throw DpModuleError.DpstComm.BNAS0374();
		}
		if (CommUtil.isNull(intr.getIntrdt())) {
			throw DpModuleError.DpstComm.BNAS0478();
		}

		if (CommUtil.isNull(intr.getIncdtp())) {
			throw DpModuleError.DpstComm.BNAS0487();
		}
		if (intr.getIncdtp() == E_IRCDTP.LAYER) {
			if (intr.getInclfg() == E_YES___.YES) {
				if (CommUtil.isNull(intr.getIntrwy())) {
					throw DpModuleError.DpstComm.BNAS0481();
				}
				if (CommUtil.isNull(intr.getLevety())) {
					throw DpModuleError.DpstComm.BNAS0551();
				}

			} else if (intr.getInclfg() == E_YES___.NO) {
				if (CommUtil.isNull(intr.getLyinwy())) {
					throw DpModuleError.DpstComm.BNAS0483();
				}
				if (CommUtil.isNull(intr.getIsrgdt())) {

					throw DpModuleError.DpstComm.BNAS0353();
					// open.getIntr().setIsrgdt(E_YES___.NO);
				}
				if (CommUtil.isNull(intr.getLydttp())) {
					// open.getIntr().setLydttp(null);
					throw DpModuleError.DpstComm.BNAS0774();
				}
			} else {
				throw DpModuleError.DpstComm.BNAS0480();
			}
		}

		if (E_IRCDTP.Reference == intr.getIncdtp() || E_IRCDTP.BASE == intr.getIncdtp()) {
			if (CommUtil.isNotNull(intr.getInclfg()) && intr.getInclfg() == E_YES___.YES) {
				throw DpModuleError.DpstComm.BNAS1265();
			}
		}

		if (CommUtil.isNull(intr.getInammd())) {
			throw DpModuleError.DpstComm.BNAS0642();
		}
		if (E_IBAMMD.AVG == intr.getInammd()) {
			if (CommUtil.isNull(intr.getBldyca())) {
				throw DpModuleError.DpstComm.BNAS0424();
			}
			// if (CommUtil.isNull(intr.getCycltp())) {
			// throw DpModuleError.DpstComm.E9999("平均余额天数计算方式周期类型不能为空");
			// }
			// if(intr.getCycltp() == E_CYCLTP.APP){
			// if(CommUtil.compare(intr.getSpeday(), 0) <= 0){
			// throw DpModuleError.DpstComm.E9999("指定天数不能小于0");
			// }
			// }
		}

		if (CommUtil.isNull(intr.getInprwy())) {
			throw DpModuleError.DpstComm.BNAS0476();
		}
		if (intr.getInprwy() == E_IRRTTP.CK || intr.getInprwy() == E_IRRTTP.AZ) {
			if (intr.getReprwy() != E_REPRWY.BACK && intr.getReprwy() != E_REPRWY.ALL) {
				throw DpModuleError.DpstComm.BNAS0055();
			}
		} else if (intr.getInprwy() == E_IRRTTP.QD) {
			if (intr.getReprwy() != E_REPRWY.BACK) {
				throw DpModuleError.DpstComm.BNAS0055();
			}
		} else if (intr.getInprwy() == E_IRRTTP.MT) {
			if (intr.getReprwy() != E_REPRWY.PART) {
				throw DpModuleError.DpstComm.BNAS0055();
			}
		}
		if (intr.getInprwy() == E_IRRTTP.AZ && CommUtil.isNull(intr.getInadlv())) {
			throw DpModuleError.DpstComm.BNAS1415();
		}
		if (intr.getTxbefg() == E_YES___.YES && CommUtil.isNull(intr.getTaxecd())) {
			throw DpModuleError.DpstComm.BNAS1245();
		}
	}

	public static void CheckMatu(IoDpOpenSub openInfo) {
		IoDpIntrPart intr = openInfo.getIntr();
		IoDpMatuPart matu = openInfo.getMatu();

		if (intr.getIncdtp() == E_IRCDTP.LAYER && matu.getTrinwy() != E_TRINWY.NOAD) { // 分档利率
			if (CommUtil.isNotNull(matu.getTrinwy())) {
				throw DpModuleError.DpstComm.BNAS0772();
			}
		}

		if (CommUtil.isNotNull(matu.getTrprod()) && CommUtil.isNull(matu.getTrdptm())) {
			throw DpModuleError.DpstComm.BNAS0036();
		}

		if (matu.getDelyfg() == E_YES___.YES) {

		}

		if (matu.getTrdpfg() == E_YES___.YES) {
			if (CommUtil.isNull(matu.getTrsvtp())) {
				throw DpModuleError.DpstComm.BNAS0963();
			}
			if (matu.getTrsvtp() == E_TRSVTP.NO) {
				throw DpModuleError.DpstComm.BNAS0963();
			}
		}
		// if(matu.get)
	}

	/**
	 * 交易检查
	 * */
	public static void TransCheck(IoDpOpenSub open) {

		// 子账户类型校验。
		if (CommUtil.isNull(open.getBase().getAcsetp())) {
			E_DEBTTP debttp = open.getDppb().getProdlt();
			if (debttp == E_DEBTTP.DP2401) {
				open.getBase().setAcsetp(E_ACSETP.SA);
				open.getBase().setSpectp(E_SPECTP.PERSON_BASE);
				if (CommUtil.isNull(open.getBase().getAccatp())) {
					throw CaError.Eacct.BNAS0182();
				}
			} else if (debttp == E_DEBTTP.DP2402) {
				open.getBase().setAcsetp(E_ACSETP.MA);
				open.getBase().setSpectp(E_SPECTP.PERSON_01);
				if (CommUtil.isNull(open.getBase().getAccatp())) {
					throw CaError.Eacct.BNAS0182();
				}
			} else if (debttp == E_DEBTTP.DP2403) {
				open.getBase().setAcsetp(E_ACSETP.FW);
				open.getBase().setSpectp(E_SPECTP.PERSON_01);
				if (CommUtil.isNull(open.getBase().getAccatp())) {
					throw CaError.Eacct.BNAS0182();
				}
			} else if (debttp == E_DEBTTP.DP2404) {
				open.getBase().setSpectp(E_SPECTP.PERSON_01);
				open.getBase().setAcsetp(E_ACSETP.HQ);
			} else if (debttp == E_DEBTTP.DP2509) {
				open.getBase().setSpectp(E_SPECTP.PERSON_01);
				open.getBase().setAcsetp(E_ACSETP.DQ);
			} else {
				open.getBase().setSpectp(E_SPECTP.PERSON_01);
				open.getBase().setAcsetp(E_ACSETP.ZZ);
			}
		}
		// 首开户检查
		if (open.getBase().getOpacfg() == E_YES___.YES) {

		}
		// 结算户检查 账户性质检查

		if (open.getDppb().getProdmt() == E_FCFLAG.CURRENT) {
			if (open.getDppb().getProdlt() == E_DEBTTP.DP2401 || open.getDppb().getProdlt() == E_DEBTTP.DP2402
					|| open.getDppb().getProdlt() == E_DEBTTP.DP2403) {
				open.getBase().setAccttp(E_YES___.YES);
				if (open.getBase().getDepttm() != E_TERMCD.T000) {
					throw DpModuleError.DpstComm.BNAS0596();
				}
			} else {
				open.getBase().setAccttp(E_YES___.NO);
				// if(open.getBase().getDepttm() == E_TERMCD.T000){
				// throw DpModuleError.DpstComm.E9999("非结算户存期设置有误");
				// }
			}
		} else {
			open.getBase().setAccttp(E_YES___.NO);
		}
		// open.getBase().setSpectp(E_SPECTP.PERSON_BASE);
		// 处理币种
		// if(CommUtil.isNull(open.getBase().getCrcycd())){
		// if(CommUtil.isNull(open.getDppb().getCrcycd())){
		// throw DpModuleError.DpstComm.E9027("币种");
		// }else{
		// open.getBase().setCrcycd(open.getDppb().getCrcycd());
		// }
		// }
		// 账户下唯一标志检查
		if (open.getActp().getAcolfg() == E_YES___.YES) {
			if (open.getBase().getPddpfg() == E_FCFLAG.CURRENT) {
				// List<KnaAcct> lsKnaAcct =
				// KnaAcctDao.selectAll_odb11(open.getBase().getCrcycd(),
				// open.getBase().getProdcd(), open.getBase().getCustac(),
				// E_DPACST.NORMAL, false);

				List<KnaAcct> lsKnaAcct = ActoacDao.selAllKnaAcctByClose(open.getBase().getCrcycd(), open.getBase()
						.getProdcd(), open.getBase().getCustac(), E_DPACST.CLOSE, false);

				if (lsKnaAcct.size() > 1) {
					throw DpModuleError.DpstProd.BNAS1724();
				} else if (lsKnaAcct.size() == 1) {
					// open.getBase().setAcctno(lsKnaAcct.get(0).getAcctno());
					throw DpModuleError.DpstProd.BNAS1724();
				}
			}
			if (open.getBase().getPddpfg() == E_FCFLAG.FIX) {
				// List<KnaFxac> lsKnaFxac =
				// KnaFxacDao.selectAll_odb6(open.getBase().getCrcycd(),
				// open.getBase().getProdcd(), open.getBase().getCustac(),
				// E_DPACST.NORMAL, false);

				List<KnaFxac> lsKnaFxac = ActoacDao.selAllKnaFxacByClose(open.getBase().getCrcycd(), open.getBase()
						.getProdcd(), open.getBase().getCustac(), E_DPACST.CLOSE, false);
				if (lsKnaFxac.size() > 1) {
					throw DpModuleError.DpstProd.BNAS1724();
				} else if (lsKnaFxac.size() == 1) {
					// open.getBase().setAcctno(lsKnaFxac.get(0).getAcctno());
					throw DpModuleError.DpstProd.BNAS1724();
				}
			}
		}
		// 起存金额、递增金额检查
		BigDecimal opmony = open.getBase().getOpmony();
		BigDecimal srdpam = open.getCust().getSrdpam();
		BigDecimal stepvl = open.getCust().getStepvl();

		// add by xiongzhao 起存金额存在且大于零的情况下采取检查交易金额是否大于起存金额
		if (CommUtil.isNotNull(srdpam) && (CommUtil.compare(srdpam, BigDecimal.ZERO) > 0)) {
			if (CommUtil.compare(opmony, srdpam) < 0) {
				throw DpModuleError.DpstComm.BNAS0563();
			}
		}

		if (CommUtil.isNotNull(stepvl) && CommUtil.compare(stepvl, BigDecimal.ZERO) > 0) {
			if (CommUtil.compare(opmony.subtract(srdpam).remainder(stepvl), BigDecimal.ZERO) != 0) {
				throw DpModuleError.DpstComm.BNAS0562();
			}
		}
		// 客户下唯一标志检查
		if (open.getCust().getOnlyfg() == E_ONLYFG.ONLO) {

			if (open.getBase().getPddpfg() == E_FCFLAG.CURRENT) {
				// List<KnaAcct> tblKnaAccts =
				// KnaAcctDao.selectAll_odb10(open.getBase().getCustno(),
				// open.getBase().getProdcd(), E_DPACST.NORMAL, false);

				List<KnaAcct> tblKnaAccts = ActoacDao.selAllKnaAcctByCustnoClose(open.getBase().getProdcd(), open
						.getBase().getCustno(), E_DPACST.CLOSE, false);
				if (tblKnaAccts.size() > 1) {
					throw DpModuleError.DpstProd.BNAS1714();
				} else if (tblKnaAccts.size() == 1) {
					// open.getBase().setAcctno(tblKnaAccts.get(0).getAcctno());
					throw DpModuleError.DpstProd.BNAS1714();
				}
			}
			// 检查定期账户
			if (open.getBase().getPddpfg() == E_FCFLAG.FIX) {
				// List<KnaFxac> tblKnaFxacs =
				// KnaFxacDao.selectAll_odb7(open.getBase().getCustno(),
				// open.getBase().getProdcd(), E_DPACST.NORMAL, false);
				List<KnaFxac> tblKnaFxacs = ActoacDao.selAllKnaFxacByCustnoClose(open.getBase().getProdcd(), open
						.getBase().getCustno(), E_DPACST.CLOSE, false);
				if (tblKnaFxacs.size() > 1) {
					throw DpModuleError.DpstProd.BNAS1714();
				} else if (tblKnaFxacs.size() == 1) {
					// open.getBase().setAcctno(tblKnaFxacs.get(0).getAcctno());
					throw DpModuleError.DpstProd.BNAS1714();
				}
			}
		}
		// 机构检查
		// 存入控制和支取控制检查
		if (open.getPost().getPosttp() == E_SAVECT.COND) { // 有条件允许存入

		} else if (open.getPost().getPosttp() == E_SAVECT.YES) { // 无条件存入
			// open.getPost().setAmntwy(E_AMNTWY.);
			// }else if(open.getPost().getPosttp() == E_SAVECT.){

		}

		CheckLocalDppb(open);
	}

	/***
	 * 如果销售工厂上送的产品号与本地数据库一致，则检查基础属性是否被修改
	 */
	public static void CheckLocalDppb(IoDpOpenSub open) {

//		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();// 交易法人号

//		CommTools.getBaseRunEnvs().setBusi_org_id(CommTools.getBaseRunEnvs().getCenter_org_id());// 省中心法人


		//CommTools.getBaseRunEnvs().setBusi_org_id(CommTools.getBaseRunEnvs().getCenter_org_id());// 省中心法人

		KupDppb dppb = KupDppbDao.selectOne_odb1(open.getBase().getProdcd(), false);
		if (CommUtil.isNotNull(dppb)) {
			if (open.getDppb().getProdbt() != dppb.getProdtp()) {
				throw DpModuleError.DpstComm.BNAS0254();
			}
			if (open.getDppb().getProdlt() != dppb.getDebttp()) {
				throw DpModuleError.DpstComm.BNAS0259();
			}
			if (open.getDppb().getProdmt() != dppb.getPddpfg()) {
				throw DpModuleError.DpstComm.BNAS0256();
			}
			if (!CommUtil.equals(open.getBase().getCrcycd(),dppb.getPdcrcy())) {
				throw DpModuleError.DpstComm.BNAS1097();
			}

			// 产品账户类型控制表
			KupDppbActp actp = KupDppbActpDao.selectOne_odb1(open.getBase().getProdcd(), E_CUSACT.ACC, true);
			if (CommUtil.isNull(actp.getAcolfg()) || open.getActp().getAcolfg() != actp.getAcolfg()) {
				throw DpModuleError.DpstComm.BNAS0172();
			}

			if (CommUtil.isNull(actp.getCacttp()) || open.getActp().getCacttp() != actp.getCacttp()) {
				throw DpModuleError.DpstComm.BNAS0522();
			}

			// 产品开户控制表
			KupDppbCust tblCust = KupDppbCustDao.selectOne_odb1(dppb.getProdcd(), dppb.getPdcrcy(), true);
			if (CommUtil.isNull(tblCust)) {
				if (CommUtil.isNotNull(open.getCust().getMadtby())) {
					throw DpModuleError.DpstComm.BNAS0964();
				}
				if (CommUtil.isNotNull(open.getCust().getOnlyfg())) {
					throw DpModuleError.DpstComm.BNAS0528();
				}
			} else {
				if (open.getCust().getMadtby() != tblCust.getMadtby()) {
					throw DpModuleError.DpstComm.BNAS0964();
				}
				if (open.getCust().getOnlyfg() != tblCust.getOnlyfg()) {
					throw DpModuleError.DpstComm.BNAS0528();
				}
			}

			// 产品存入控制标志
			KupDppbPost tblPost = KupDppbPostDao.selectOne_odb1(dppb.getProdcd(), dppb.getPdcrcy(), true);
			if (open.getPost().getPosttp() != tblPost.getPosttp()) {
				throw DpModuleError.DpstComm.BNAS1007();
			}
			if (open.getPost().getPostwy() != tblPost.getPostwy()) {
				throw DpModuleError.DpstComm.BNAS1416();
			}
			if (open.getPost().getAmntwy() != tblPost.getAmntwy()) {
				throw DpModuleError.DpstComm.BNAS1010();
			}
			if (open.getPost().getTimewy() != tblPost.getTimewy()) {
				throw DpModuleError.DpstComm.BNAS1020();
			}
			if (open.getPost().getDetlfg() != tblPost.getDetlfg()) {
				throw DpModuleError.DpstComm.BNAS0349();
			}

			// 产品存入计划
			KupDppbPostPlan tblPostP = KupDppbPostPlanDao.selectOne_odb1(dppb.getProdcd(), dppb.getPdcrcy(), false);
			if (CommUtil.isNull(tblPostP)) {

				/*
				 * JF Modify：即富收单存款产品增加非空检查。
				 */
				if(CommUtil.isNotNull(open.getPostpl())) {
					if (CommUtil.isNotNull(open.getPostpl().getSvlewy())) {
						throw DpModuleError.DpstComm.BNAS1005();
					}
					if (CommUtil.isNotNull(open.getPostpl().getDfltsd())) {
						throw DpModuleError.DpstComm.BNAS1003();
					}
					if (CommUtil.isNotNull(open.getPostpl().getDfltwy())) {
						throw DpModuleError.DpstComm.BNAS1001();
					}
					if (CommUtil.isNotNull(open.getPostpl().getPscrwy())) {
						throw DpModuleError.DpstComm.BNAS1016();
					}
					if (CommUtil.isNotNull(open.getPostpl().getPsamtp())) {
						throw DpModuleError.DpstComm.BNAS1018();
					}
				}
			} else {

				if (open.getPostpl().getSvlewy() != tblPostP.getSvlewy()) {
					throw DpModuleError.DpstComm.BNAS1005();
				}
				if (open.getPostpl().getDfltsd() != tblPostP.getDfltsd()) {
					throw DpModuleError.DpstComm.BNAS1003();
				}
				if (open.getPostpl().getDfltwy() != tblPostP.getDfltwy()) {
					throw DpModuleError.DpstComm.BNAS1001();
				}
				if (open.getPostpl().getPscrwy() != tblPostP.getPscrwy()) {
					throw DpModuleError.DpstComm.BNAS1016();
				}
				if (open.getPostpl().getPsamtp() != tblPostP.getPsamtp()) {
					throw DpModuleError.DpstComm.BNAS1018();
				}
			}

			// 产品支取控制表
			KupDppbDraw tblDraw = KupDppbDrawDao.selectOne_odb1(dppb.getProdcd(), dppb.getPdcrcy(), true);
			if (CommUtil.isNull(open.getDraw())) {
				throw DpModuleError.DpstComm.BNAS0120();
			}
			if (open.getDraw().getDrawtp() != tblDraw.getDrawtp()) {
				throw DpModuleError.DpstComm.BNAS1417();
			}
			if (open.getDraw().getCtrlwy() != tblDraw.getCtrlwy()) {
				throw DpModuleError.DpstComm.BNAS1418();
			}
			if (open.getDraw().getDramwy() != tblDraw.getDramwy()) {
				throw DpModuleError.DpstComm.BNAS0123();
			}
			if (open.getDraw().getDrtmwy() != tblDraw.getDrtmwy()) {
				throw DpModuleError.DpstComm.BNAS0135();
			}
			if (open.getDraw().getDrrule() != tblDraw.getDrrule()) {
				throw DpModuleError.DpstComm.BNAS0132();
			}
			if (open.getDraw().getIsmibl() != tblDraw.getIsmibl()) {
				throw DpModuleError.DpstComm.BNAS1419();
			}

			// 产品支取计划控制
			KupDppbDrawPlan tblDrawP = KupDppbDrawPlanDao.selectOne_odb1(dppb.getProdcd(), dppb.getPdcrcy(), false);
			if (CommUtil.isNull(tblDrawP)) {
				/*
				 * JF Modify：即富收单存款产品增加非空检查。
				 */
				if(CommUtil.isNotNull(open.getDrawpl())) {
					if (CommUtil.isNotNull(open.getDrawpl().getDradwy())) {
						throw DpModuleError.DpstComm.BNAS0127();
					}
					if (CommUtil.isNotNull(open.getDrawpl().getDrcrwy())) {
						throw DpModuleError.DpstComm.BNAS0130();
					}
					if (CommUtil.isNotNull(open.getDrawpl().getDrdfsd())) {
						throw DpModuleError.DpstComm.BNAS0115();
					}
					if (CommUtil.isNotNull(open.getDrawpl().getDrdfwy())) {
						throw DpModuleError.DpstComm.BNAS0113();
					}
					if (CommUtil.isNotNull(open.getDrawpl().getBeinfg())) {
						throw DpModuleError.DpstComm.BNAS0118();
					}
				}
			} else {
				if (open.getDrawpl().getDradwy() != tblDrawP.getDradwy()) {
					throw DpModuleError.DpstComm.BNAS0127();
				}
				if (open.getDrawpl().getDrcrwy() != tblDrawP.getDrcrwy()) {
					throw DpModuleError.DpstComm.BNAS0130();
				}
				if (open.getDrawpl().getDrdfsd() != tblDrawP.getDrdfsd()) {
					throw DpModuleError.DpstComm.BNAS0115();
				}
				if (open.getDrawpl().getDrdfwy() != tblDrawP.getDrdfwy()) {
					throw DpModuleError.DpstComm.BNAS0113();
				}
				if (open.getDrawpl().getBeinfg() != tblDrawP.getBeinfg()) {
					throw DpModuleError.DpstComm.BNAS0118();
				}
			}

			// 产品到期控制表
			KupDppbMatu tblMatu = KupDppbMatuDao.selectOne_odb1(dppb.getProdcd(), dppb.getPdcrcy(), false);
			if (CommUtil.isNull(tblMatu)) {
				/*
				 * JF Modify：即富收单存款产品增加非空检查。
				 */
				if(CommUtil.isNotNull(open.getMatu())) {
					if (CommUtil.isNotNull(open.getMatu().getFestdl())) {
						throw DpModuleError.DpstComm.BNAS0223();
					}
					if (CommUtil.isNotNull(open.getMatu().getDelyfg())) {
						throw DpModuleError.DpstComm.BNAS0351();
					}
					if (CommUtil.isNotNull(open.getMatu().getDelyfg())) {
						throw DpModuleError.DpstComm.BNAS0351();
					}
					if (CommUtil.isNotNull(open.getMatu().getTrpdfg())) {
						throw DpModuleError.DpstComm.BNAS0350();
					}
					if (CommUtil.isNotNull(open.getMatu().getTrprod())) {
						throw DpModuleError.DpstComm.BNAS0035();
					}
				}
			} else {
				if (open.getMatu().getFestdl() != tblMatu.getFestdl()) {
					throw DpModuleError.DpstComm.BNAS0223();
				}
				if (open.getMatu().getDelyfg() != tblMatu.getDelyfg()) {
					throw DpModuleError.DpstComm.BNAS0351();
				}
				if (open.getMatu().getDelyfg() != tblMatu.getDelyfg()) {
					throw DpModuleError.DpstComm.BNAS0351();
				}
				if (open.getMatu().getTrpdfg() != tblMatu.getTrpdfg()) {
					throw DpModuleError.DpstComm.BNAS0350();
				}
				if (!CommUtil.equals(open.getMatu().getTrprod(), tblMatu.getTrprod())) {
					throw DpModuleError.DpstComm.BNAS0035();
				}
			}

			// 产品利息利率控制表
			KupDppbIntr tblIntr = KupDppbIntrDao.selectOne_odb1(dppb.getProdcd(), dppb.getPdcrcy(), true);
			if (open.getIntr().getIntrtp() != tblIntr.getIntrtp()) {
				throw DpModuleError.DpstComm.BNAS0472();
			}
			if (open.getIntr().getInbefg() != tblIntr.getInbefg()) {
				throw DpModuleError.DpstComm.BNAS0646();
			}
			if (open.getIntr().getTxbefg() != tblIntr.getTxbefg()) {
				throw DpModuleError.DpstComm.BNAS0648();
			}
			if (!CommUtil.equals(open.getIntr().getTaxecd(), tblIntr.getTaxecd())) {
				throw DpModuleError.DpstComm.BNAS1244();
			}
			if (open.getIntr().getTxbebs() != tblIntr.getTxbebs()) {
				throw DpModuleError.DpstComm.BNAS0644();
			}
			if (open.getIntr().getHutxfg() != tblIntr.getHutxfg()) {
				throw DpModuleError.DpstComm.BNAS0373();
			}
			if (!CommUtil.equals(open.getIntr().getTebehz(), tblIntr.getTebehz())) {
				throw DpModuleError.DpstComm.BNAS0639();
			}
			/**
			 * mod by xj 20180829 柳行取息宝 结息频率是定义
			 */
//			if (!CommUtil.equals(open.getIntr().getTxbefr(), tblIntr.getTxbefr())) {
//				throw DpModuleError.DpstComm.BNAS0593();
//			}
			/**end*/
			if (open.getIntr().getIncdtp() != tblIntr.getIncdtp()) {
				throw DpModuleError.DpstComm.BNAS1420();
			}
			if (!CommUtil.equals(open.getIntr().getIntrcd(), tblIntr.getIntrcd())) {
				throw DpModuleError.DpstComm.BNAS0484();
			}
			if (open.getIntr().getIntrdt() != tblIntr.getIntrdt()) {
				throw DpModuleError.DpstComm.BNAS0477();
			}
			if (open.getIntr().getInclfg() != tblIntr.getInwytp()) {
				throw DpModuleError.DpstComm.BNAS0482();
			}
			if (open.getIntr().getInclfg() != tblIntr.getInwytp()) {
				throw DpModuleError.DpstComm.BNAS0482();
			}
			if (open.getIntr().getLevety() != tblIntr.getLevety()) {
				throw DpModuleError.DpstComm.BNAS0550();
			}
			if (open.getIntr().getIntrwy() != tblIntr.getIntrwy()) {
				throw DpModuleError.DpstComm.BNAS0479();
			}
			if (open.getIntr().getLyinwy() != tblIntr.getLyinwy()) {
				throw DpModuleError.DpstComm.BNAS0775();
			}
			if (open.getIntr().getIsrgdt() != tblIntr.getIsrgdt()) {
				throw DpModuleError.DpstComm.BNAS0352();
			}
			if (open.getIntr().getLydttp() != tblIntr.getLydttp()) {
				throw DpModuleError.DpstComm.BNAS0773();
			}
			if (open.getIntr().getInammd() != tblIntr.getInammd()) {
				throw DpModuleError.DpstComm.BNAS0641();
			}
			if (open.getIntr().getBldyca() != tblIntr.getBldyca()) {
				throw DpModuleError.DpstComm.BNAS0423();
			}
			if (open.getIntr().getInprwy() != tblIntr.getInprwy()) {
				throw DpModuleError.DpstComm.BNAS0474();
			}
			if (open.getIntr().getReprwy() != tblIntr.getReprwy()) {
				throw DpModuleError.DpstComm.BNAS0054();
			}
			if (open.getIntr().getReprwy() != tblIntr.getReprwy()) {
				throw DpModuleError.DpstComm.BNAS0054();
			}

			// 产品违约利息利率
			List<KupDppbDfir> tblDfir = KupDppbDfirDao.selectAll_odb2(dppb.getProdcd(), dppb.getPdcrcy(), false);
			if (CommUtil.isNotNull(tblDfir) && CommUtil.isNotNull(open.getDfir())
					&& tblDfir.size() != open.getDfir().size()) {
				throw DpModuleError.DpstComm.BNAS1049();
			}

			// 检查核算代码是否正确
			E_TERMCD depttm = open.getBase().getDepttm();
			if (CommUtil.isNull(depttm)) {
				depttm = E_TERMCD.ALL;
			}
			KupDppbAcct tblDppb_acct = KupDppbAcctDao.selectOne_odb1(open.getBase().getProdcd(), depttm, false);

			if (CommUtil.isNotNull(tblDppb_acct)) {
				if (CommUtil.compare(open.getBase().getAcctcd(), tblDppb_acct.getAcctcd()) != 0) {
					throw DpModuleError.DpstComm.BNAS0677();
				}
			}

		} else {
			throw DpModuleError.DpstComm.BNAS0652();
		}

//		CommTools.getBaseRunEnvs().setBusi_org_id(corpno);// 交易法人
	}

}
