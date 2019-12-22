package cn.sunline.ltts.busi.dp.acct;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.dayend.DpDayEndInt;
import cn.sunline.ltts.busi.dp.domain.DpAcctOnlnblEntity;
import cn.sunline.ltts.busi.dp.froz.DpFrozTools;
import cn.sunline.ltts.busi.dp.layer.LayerAcctSrv;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctAddt;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctAddtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSave;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSaveDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SAVECT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AMNTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PLANFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_POSTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TIMEWY;

/**
 * 活期存款公共处理类
 * 
 * @author cuijia
 * 
 */
public class DpKnaAcct {

	private static final BizLog bizlog = BizLogUtil.getBizLog(DpKnaAcct.class);

	/**
	 * 存入检查方法
	 */
	public static void validatePost(String acctno, BigDecimal tranam,
			E_FCFLAG fcflag, String prodcd, String crcycd,
			BaseEnumType.E_YES___ auacfg, String custac,
			BaseEnumType.E_YES___ ngblfg) {
		// 检查产品属性
		validatePostProductProperty(acctno, tranam, fcflag, prodcd, crcycd);
		// 检查冻结
		if (auacfg != E_YES___.NO) {
			if (!DpFrozTools.getSaveFg(E_FROZOW.AUACCT, custac)) {
				throw DpModuleError.DpstComm.BNAS0431();
			}
		}
		// 余额检查
		// 记红字账时
		if (CommUtil.compare(tranam, BigDecimal.ZERO) < 0) {
			BigDecimal bal = BigDecimal.ZERO;
			if (auacfg != E_YES___.NO) {
				// 可用余额 addby xiongzhao 20161223
				bal = SysUtil.getInstance(DpAcctSvcType.class)
						.getAcctaAvaBal(custac, acctno, crcycd, E_YES___.YES,
								E_YES___.YES);

			} else {
				bal = DpAcctProc.getAcctOnlnbl(acctno, true);
			}

			if (ngblfg != E_YES___.YES
					&& CommUtil.compare(tranam.negate(), bal) > 0) {
				bizlog.debug("可用余额=================[" + bal + "]");
				throw DpModuleError.DpstComm.BNAS0177();
			}
		}
	}

	/**
	 * 活期余额更新处理
	 * 
	 * @param entity
	 * @param fcflag
	 * @param strkfg
	 */
	public static void updateDpAcctOnlnbl(DpAcctOnlnblEntity entity,
			E_FCFLAG fcflag, E_YES___ strkfg) {
		int cnt = 0;
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期

		String acctno = entity.getAcctno();
		E_AMNTCD amntcd = entity.getAmntcd();
		BigDecimal tranam = entity.getTranam();
		BigDecimal onlnbl = BigDecimal.ZERO;
		BigDecimal lastbl = BigDecimal.ZERO;

		// 修改活期余额
		KnaAcct acct = KnaAcctDao.selectOneWithLock_odb1(acctno, true);
		// 如果负债账号状态不正常，返回错误信息

		// ===============mdy by zhanga 休眠账户也需要支持转入=============
		if (acct.getAcctst() == E_DPACST.CLOSE) {
			throw DpModuleError.DpstProd.BNAS1130(acctno);
		}
		// TODO 计算上日余额
		onlnbl = DpAcctProc.getAcctBalance(acct);
//		onlnbl = acct.getOnlnbl();
		lastbl = acct.getLastbl();
		// 获取应入账日期
		String acctdt = null;
		if (BusiTools.getDeptAcctdt()) {
			acctdt = DpAcctDcn.getAccoutDate(acctno, trandt, acct.getUpbldt());
		} else {
			// 应入账日期，默认为交易日期
			acctdt = CommTools.getBaseRunEnvs().getTrxn_date();
		}

		// 如果是当天第一笔交易则更新上日余额及余额更新时间
		if (CommUtil.compare(acctdt, acct.getUpbldt()) > 0) {
			/* 过账:更新余额最新更新日期 */
			acct.setUpbldt(acctdt);
			bizlog.debug(">> 更新账户[%s] = 账户上日余额 = [%s]", acctno,
					onlnbl);
			acct.setLastbl(onlnbl);
			// 更新积数
			KnbAcin acin = KnbAcinDao.selectOneWithLock_odb1(acctno, true);
			// ------------------分层账户动户滚积数-----------------
			if (acin.getIncdtp() == E_IRCDTP.LAYER
					&& acin.getInclfg() == E_YES___.NO) {
				// TODO:分层账户动户滚积数
				LayerAcctSrv.rollCumulate(acctno, 0L, onlnbl,
						acin.getLyinwy());
			}
			// ------------------分层账户动户滚积数-----------------

			// 计算积数
			BigDecimal cutmam = DpPublic.calRealTotalAmt(acin.getCutmam(),
					onlnbl, acctdt, acin.getLaamdt());
			acin.setCutmam(cutmam);
			acin.setAmamfy(acin.getAmamfy().add(cutmam));
			acin.setLaamdt(acctdt);
			KnbAcinDao.updateOne_odb1(acin);
		}
		// 判断借贷标志
		if (E_AMNTCD.CR == amntcd) {
			onlnbl = onlnbl.add(tranam);
			acct.setOnlnbl(onlnbl);
			// 日切后记上日账需要变动上日余额
			if (CommUtil.compare(trandt, DateTools2.getDateInfo().getSystdt()) < 0) {
				// 如果很多天没有发生交易，记上日账只需要改变联机余额，不需要改变上日余额，注意余额更新日期已经在上面修改过！！！
				// 记账日期小于余额更新日期时，表示继上日后已经发生了交易，余额已过账，需要改变上日余额。
				if (CommUtil.compare(trandt, acct.getUpbldt()) < 0) {
					lastbl = lastbl.add(tranam);
					acct.setLastbl(lastbl);

					// 更新积数
					KnbAcin acin = KnbAcinDao.selectOneWithLock_odb1(acctno,
							true);

					// 分层户记上日账修改积数和上日余额TODO:

					// 计算积数
					acin.setCutmam(acin.getCutmam().add(tranam));
					acin.setAmamfy(acin.getAmamfy().add(tranam));
					// acin.setLaamdt(acctdt);
					KnbAcinDao.updateOne_odb1(acin);

				}
			}
		} else if (E_AMNTCD.DR == amntcd) {
			onlnbl = onlnbl.subtract(tranam);
			acct.setOnlnbl(onlnbl);
			// 日切后记上日账需要变动上日余额
			if (CommUtil.compare(trandt, DateTools2.getDateInfo().getSystdt()) < 0) {
				if (CommUtil.compare(trandt, acct.getUpbldt()) < 0) {
					lastbl = lastbl.subtract(tranam);
					acct.setLastbl(lastbl);
					// 更新积数
					KnbAcin acin = KnbAcinDao.selectOneWithLock_odb1(acctno,
							true);
					// 分层户记上日账修改积数和上日余额 TODO:
					// 计算积数
					acin.setCutmam(acin.getCutmam().subtract(tranam));
					acin.setAmamfy(acin.getAmamfy().subtract(tranam));
					KnbAcinDao.updateOne_odb1(acin);

				}
			}
		}

		// 如果最后一笔交易时间小于等于交易日期，需要更新最后交易日期和流水
		if (CommUtil.compare(acct.getLstrdt(), trandt) <= 0) {
			acct.setLstrdt(trandt);
			acct.setLstrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		}

		// 修改账户余额
		cnt = KnaAcctDao.updateOne_odb1(acct);
		if (cnt != 1) {
			throw DpModuleError.DpstAcct.BNAS1742(Long.valueOf(cnt));
		}

		// 定期附加产品信息表
//		KnaAcctProd tblKnaAProd = KnaAcctProdDao.selectOneWithLock_odb1(acctno,
//				true);
//		Long detlsq = ConvertUtil.toLong(CommUtil.nvl(tblKnaAProd.getObgatw(), 0)) + 1;// 明细序号
//		// 更新明细序号
//		tblKnaAProd.setObgatw(Long.toString(detlsq));// 明细序号
//		KnaAcctProdDao.updateOne_odb1(tblKnaAProd);
		
		/**
		 * 倒起息处理
		 */
		// 联机存入时的特殊倒起息处理
		if (BusiTools.getDeptAcctdt() && strkfg == E_YES___.NO ) {
			// 实际入账日期大于主调DCN系统日期，则补这天的利息
			if (CommUtil.compare(acctdt, CommTools.getBaseRunEnvs().getInitiator_date()) > 0) {
				KnbAcin acin = KnbAcinDao.selectOneWithLock_odb1(acctno, true);
				//补一天积数
				acin.setCutmam(acin.getCutmam().add(tranam));
				acin.setAmamfy(acin.getAmamfy().add(tranam));
				KnbAcinDao.updateOne_odb1(acin);
				//补计息起始日期
				String lastdt = CommTools.getBaseRunEnvs().getInitiator_date(); // T日
				//删掉已计提数据，再重新计算
				DpDayEndDao.delknbcbdlData(lastdt,acctno); // 删除已预计提数据
				DpDayEndInt.prcCrcabr(acin,lastdt,acctdt,E_YES___.NO);  
				
				//登记冲正登记簿
				//TODO
			}
		}

//		entity.setDetlsq(detlsq);
		// 账户余额
		entity.setOnlnbl(onlnbl);
		// 返回开户机构
		entity.setOpenbr(acct.getBrchno());
		// 返回账户名称
		entity.setAcctna(acct.getAcctna());
		// 返回产品号
		entity.setProdcd(acct.getProdcd());
		// 返回核算代码
		entity.setDtitcd(acct.getAcctcd());
		// 返回存期
		entity.setTermcd(acct.getDepttm());
		// 返回账户所属机构
		entity.setAcctbr(acct.getBrchno());
		// 入账日期
		entity.setAcctdt(acctdt);

	}

	private static void validatePostProductProperty(String acctno,
			BigDecimal tranam, E_FCFLAG fcflag, String prodcd, String crcycd) {
		// 存入控制方式、方法改成从账户层获取 update in 20160627
		// 存入控制方式
		E_SAVECT posttp = null;
		// 存入控制方法
		E_POSTWY postwy = null;
		// 设置存入计划标志
		E_PLANFG planfg = null;
		KnaSave tblKnaSave = null;
		KnaAcct tblKnaAcct = KnaAcctDao.selectOneWithLock_odb1(acctno, false);
		if (CommUtil.isNull(tblKnaAcct)) {
			throw DpModuleError.DpstAcct.BNAS0769(acctno);
		}
		// 检查三类户最高限额控制,排除部分交易
		// liaojc 20160628 add Ⅲ类户最高限额控制
		BigDecimal maxbin = BigDecimal.ZERO;// 最高限额
		// 获取三类户最高限额
		KnaAcctAddt tblKnaAcctAddt = KnaAcctAddtDao
				.selectOne_odb1(acctno, true);

		// 三类户最高限额控制检查,亲情钱包账户暂不检查，先写死
		if (E_ACCATP.WALLET == tblKnaAcctAddt.getAccatp()
				&& tblKnaAcct.getAcsetp() != E_ACSETP.FW) {
			// 查询最高余额控制参数
			String lttscd = BusiTools.getBusiRunEnvs().getLttscd();
			KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("MAXBAL_CONTROL",
					tblKnaAcctAddt.getAccatp().getValue(), lttscd, "%", false);

			String chkFlag = "0"; // 校验最高余额标志
			// 查询控制参数不为空，则赋值配置参数
			if (CommUtil.isNotNull(tblKnpParameter)) {
				chkFlag = tblKnpParameter.getParm_value1();
			}

			// 校验标志为1时，不校验
			if (!CommUtil.equals(chkFlag, "1")) {

				// if else 中都要执行的查询，拉出来放在外面执行 modify chenlk 2016-8-21
				// 获取参数表中三类户最高限额
				KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm.maxbln", "3",
						"%", "%", false);
				if (CommUtil.isNull(para)) {

					throw DpModuleError.DpstComm.BNAS1134();
				}

				if (CommUtil.compare(tblKnaAcctAddt.getHigham(),
						BigDecimal.ZERO) > 0) {

					maxbin = tblKnaAcctAddt.getHigham();// 最高限额

					BigDecimal maxbin1 = new BigDecimal(para.getParm_value1());// 参数表最高限额

					// 客户设置限额大于参数表最高限额
					if (CommUtil.compare(maxbin, maxbin1) > 0) {
						throw DpModuleError.DpstAcct.BNAS1135(maxbin.toString(),maxbin1.toString());
					}
				} else {
					maxbin = new BigDecimal(para.getParm_value1());
				}
				if (CommUtil.compare(maxbin, BigDecimal.ZERO) > 0) {
					BigDecimal onlnbl = BigDecimal.ZERO;// 余额
					onlnbl = DpAcctProc.getAcctBalance(tblKnaAcct);// 当前余额
					onlnbl = onlnbl.add(tranam);
					if (CommUtil.compare(onlnbl, maxbin) > 0) {// 存入后余额大于最高限额

						bizlog.debug("存入金额[%s]过大，存入后余额[%s]超出账户最高限额[%s]",
								tranam, onlnbl, maxbin.toString());

						throw DpModuleError.DpstComm.BNAS1045();
					}
				}
			}
		}

		// 获取负债活期账户存入控制信息
		tblKnaSave = KnaSaveDao.selectOne_odb1(acctno, false);
		if (CommUtil.isNull(tblKnaSave)) {
			throw DpModuleError.DpstComm.E9905("活期存入控制信息表不存在!");
		}
		// 最小控制金额
		BigDecimal miniam = BigDecimal.ZERO;
		// 最大控制金额
		BigDecimal maxiam = BigDecimal.ZERO;
		// 实际存入次数
		Long resvnm = 0L;
		// 最小存入次数
		Long minitm = 0L;
		// 最大存入次数
		Long maxitm = 0L;

		E_AMNTWY amntwy = tblKnaSave.getAmntwy();// 存入金额控制方式
		miniam = tblKnaSave.getMiniam();// 单次存入最小金额
		maxiam = tblKnaSave.getMaxiam();// 单次存入最大金额
		E_TIMEWY timewy = tblKnaSave.getTimewy();// 存入次数控制方式
        resvnm = (tblKnaSave.getResvnm() == null ? 0L : tblKnaSave.getResvnm()) + 1;// 实际存入次数
		minitm = tblKnaSave.getMinitm();// 最小存入次数
		maxitm = tblKnaSave.getMaxitm();// 最大存入次数
		posttp = tblKnaSave.getPosttp(); // 存入控制方式
		postwy = tblKnaSave.getPostwy(); // 存入控制方法
		planfg = tblKnaSave.getPlanfg(); // 设置存入计划标志

		// 如果是设置了存入计划，则判断存入计划相关控制，如果没有设置存入计划，则判断存入控制相关控制逻辑，其中为空也为未设置
		if (E_PLANFG.NOSET == planfg || CommUtil.isNull(planfg)) {
			if (E_SAVECT.YES == posttp) {
				return;
			} else if (E_SAVECT.COND == posttp) {
				if (E_POSTWY.AMCL == postwy) {
					// 金额控制
					DpProductProc.chkAmtControl(amntwy, tranam, miniam, maxiam);
					// 存入控制登记
					tblKnaSave.setResvam(tblKnaSave.getResvam().add(tranam));
					KnaSaveDao.updateOne_odb1(tblKnaSave);
				} else if (E_POSTWY.TMCL == postwy) {
					// 次数控制
					DpProductProc
							.chkTimeControl(timewy, resvnm, minitm, maxitm);
					// 存入控制登记
					tblKnaSave.setResvnm(tblKnaSave.getResvnm() + 1);
					KnaSaveDao.updateOne_odb1(tblKnaSave);
				} else if (E_POSTWY.ATMC == postwy) {
					// 金额和次数都控制
					DpProductProc.chkAmtControl(amntwy, tranam, miniam, maxiam);
					DpProductProc
							.chkTimeControl(timewy, resvnm, minitm, maxitm);
					// 存入控制登记
					tblKnaSave.setResvam(tblKnaSave.getResvam().add(tranam));
					tblKnaSave.setResvnm(tblKnaSave.getResvnm() + 1);
					KnaSaveDao.updateOne_odb1(tblKnaSave);
				} else {
					throw DpModuleError.DpstComm.BNAS1011();
				}
			} else {
				throw DpModuleError.DpstComm.BNAS1011();
			}
		} else if (E_PLANFG.SET == planfg) {
			DpProductProc.prcKnaSavePlan(acctno, tranam);
		} else {
			throw DpModuleError.DpstComm.BNAS1151(planfg.toString());
		}
	}
}
