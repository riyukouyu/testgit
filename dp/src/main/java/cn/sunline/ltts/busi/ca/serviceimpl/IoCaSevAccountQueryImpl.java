package cn.sunline.ltts.busi.ca.serviceimpl;

import java.math.BigDecimal;
//import java.util.ArrayList;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
//import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.CaPublic;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccsDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.type.CaCustInfo;
import cn.sunline.ltts.busi.dp.namedsql.YhtDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
//import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
//import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.type.DpYhtType.DpInterestInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaFxacDetlInfo;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbAcin;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKubInrtInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaSignDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAcctInfoList;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaClientLedgerOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaDpAcctfos;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaDpacctInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaDpacctInfoList;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryLedgerInfosIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryLedgerInfosOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AVBLDT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CAINPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEPYHT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_QRACWY;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_QRYCTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PRODTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PROYHT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CYCLTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IBAMMD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.yht.E_ASTATE;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

/**
 * 电子账户分户账明细账查询服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoCaSevAccountQueryImpl", longname = "电子账户分户账明细账查询服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoCaSevAccountQueryImpl implements
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountQuery {

	/**
	 * 
	 * @Title: qryAcctLedger
	 * @Description: (柜面分户账查询)
	 * @param cardno
	 * @param idtfno
	 * @param idtftp
	 * @param crcycd
	 * @param prodtp
	 * @param acctst
	 * @return
	 * @author xiongzhao
	 * @date 2016年7月29日 上午10:04:57
	 * @version V2.3.0
	 */
	@Override
	public IoCaQryLedgerInfosOut qryAcctLedger(IoCaQryLedgerInfosIn cplLedger) {

		String custac = null;// 电子账号
		String cardno = cplLedger.getCardno();// 卡号
		String idtfno = cplLedger.getIdtfno();// 证件号码
		E_IDTFTP idtftp = cplLedger.getIdtftp();// 证件类型
		E_PRODTP prodtp = cplLedger.getProdtp();// 产品类型
		E_CUACST acctst = cplLedger.getAcctst();// 账户状态
		String crcycd = cplLedger.getCrcycd();// 币种
		E_QRACWY qracwy = cplLedger.getQracwy();// 查询方式
		E_QRYCTP qractp = cplLedger.getQractp();// 查询类型
		BigDecimal acctbl = BigDecimal.ZERO;// 可支取余额
		BigDecimal frozam = BigDecimal.ZERO;// 冻结金额
		BigDecimal stopam = BigDecimal.ZERO;// 止付金额
		BigDecimal hdbkam = BigDecimal.ZERO;// 保留金额
		BigDecimal prauam = BigDecimal.ZERO;// 预授权金额
		BigDecimal acctam = BigDecimal.ZERO;// 总资产
		BigDecimal debtam = BigDecimal.ZERO;// 总负债
		BigDecimal dpacbl = BigDecimal.ZERO;// 存款余额
		String sCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();// 原法人代码

		// 输入字段为空校验
		// 卡号和证件信息不能同时为空
		if (CommUtil.isNull(cardno)) {
			if (CommUtil.isNull(idtfno) && CommUtil.isNull(idtftp)) {
				throw CaError.Eacct.BNAS0921();
			}
		}
		// 产品类型
		if (CommUtil.isNull(prodtp)) {
			throw CaError.Eacct.BNAS1051();
		}
		// 币种
		if (CommUtil.isNull(crcycd)) {
			throw DpModuleError.DpstComm.BNAS1101();
		}
		// 查询方式
		if (CommUtil.isNull(qracwy)) {
			throw CaError.Eacct.E0001("查询方式不能为空！");
		}
		// 查询类型
		if (CommUtil.isNull(qractp)) {
			throw CaError.Eacct.BNAS1272();
		}

		// 证件类型证件号码必须都不为空，或都为空
		if (CommUtil.isNull(idtfno)) {
			if (CommUtil.isNotNull(idtftp)) {
				throw CaError.Eacct.E0001("证件号码为空时，证件类型必须为空！");
			}
		} else {
			if (CommUtil.isNull(idtftp)) {
				throw CaError.Eacct.E0001("证件号码不为空时，证件类型必须不为空！");
			}
		}

		// 电子账号和证件类型证件号码都不为空，进行匹配校验
		if (CommUtil.isNotNull(cardno) && CommUtil.isNotNull(idtfno)
				&& CommUtil.isNotNull(idtftp)) {

			// 根据证件类型证件号码查询电子账户表
			IoCaKnaCust cplKnaCust = EacctMainDao.selByCusInfo(idtftp, idtfno,
					false);

			// 检查查询记录是否为空
			if (CommUtil.isNull(cplKnaCust)) {
				throw CaError.Eacct.E0001("证件信息无对应的电子账号信息！");
			}

			// 该交易支持省中心查询
//			CommTools.getBaseRunEnvs().setBusi_org_id(cplKnaCust.getCorpno());

			// 查询卡客户账户对照表
			KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(cardno, false);

			// 检查查询记录是否为空
			if (CommUtil.isNull(tblKnaAcdc)) {
				throw CaError.Eacct.E0001("电子账号不存在！");
			}

			// 检查证件类型，证件号码和电子账号是否匹配
			if (!CommUtil
					.equals(tblKnaAcdc.getCustac(), cplKnaCust.getCustac())) {
				throw CaError.Eacct.E0001("电子账号和证件号码不匹配");
			}

			custac = tblKnaAcdc.getCustac();
		}

		if (CommUtil.isNotNull(cardno)) {

			// 查询卡客户账号对照表
			IoCaKnaAcdc cplKnaAcdc = CaDao.selKnaAcdcByCard(cardno, false);

			// 检查查询记录是否为空
			if (CommUtil.isNull(cplKnaAcdc)) {
				throw CaError.Eacct.E0001("请输入正确的电子账号");
			}

			// 该交易支持省中心查询
//			CommTools.getBaseRunEnvs().setBusi_org_id(cplKnaAcdc.getCorpno());

			custac = cplKnaAcdc.getCustac();

		}

		if (CommUtil.isNull(cardno)) {
			if (CommUtil.isNotNull(idtfno) && CommUtil.isNotNull(idtftp)) {

				// 根据证件类型证件号码查询电子账户表
				IoCaKnaCust cplKnaCust = EacctMainDao.selByCusInfo(idtftp,
						idtfno, false);

				// 检查查询记录是否为空
				if (CommUtil.isNull(cplKnaCust)) {
					throw CaError.Eacct.E0001("请输入正确的证件信息");
				}

				// 该交易支持省中心查询
//				CommTools.getBaseRunEnvs().setBusi_org_id(cplKnaCust.getCorpno());

				custac = cplKnaCust.getCustac();

			}
		}

		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(custac, false);
		if (CommUtil.isNotNull(tblKnaCust)) {
			// 如果查询方式为柜员查询，只能查询本行社电子账户信息
			if (qracwy == E_QRACWY.TELQRY) {
				if (CommUtil.compare(sCorpno, tblKnaCust.getCorpno()) != 0) {
					throw CaError.Eacct.E0001("柜员只能查询本行社电子账户信息");
				}
			}
		}

		// 定义输出接口实例
		IoCaQryLedgerInfosOut cplLedgerInfosOut = SysUtil.getInstance(IoCaQryLedgerInfosOut.class);

		// 查询当前库
		if (qractp == E_QRYCTP.NOW) {

			// 查询电子账户账户分类
			E_ACCATP eAccatp = SysUtil.getInstance(
					IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac);

			// 查询电子账户状态
			E_CUACST cuacst = SysUtil.getInstance(
					IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
			if (CommUtil.isNotNull(acctst)) {
				if (acctst != cuacst) {
					return null;
				}
			}

			// 电子账户状态字查询
			IoDpAcStatusWord cplGetAcStWord = SysUtil.getInstance(
					IoDpFrozSvcType.class).getAcStatusWord(custac);

			// 查询电子账户基本信息
			CaCustInfo.accoutinfos accoutinfos = EacctMainDao
					.selCustInfobyCustac(custac, E_ACALST.NORMAL,
							E_ACALTP.CELLPHONE, false);

			// 检查查询记录是否为空
			if (CommUtil.isNull(accoutinfos)) {
				throw CaError.Eacct.E0001("请输入正确的电子账号ID");
			}

			// 将查询出的电子账户分类,电子账户状态添加到电子账户基本信息复合类型
			accoutinfos.setAccttp(eAccatp);
			accoutinfos.setAcctst(cuacst);

			// 查询当前余额
			IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);

			BigDecimal onlnbl = cplKnaAcct.getOnlnbl();// 当前账户余额

			// 查询昨日余额
			BigDecimal lastbl = BigDecimal.ZERO;
			if (CommUtil.compare(CommTools.getBaseRunEnvs().getTrxn_date(),
					cplKnaAcct.getUpbldt()) == 0) {
				lastbl = cplKnaAcct.getLastbl();
			}
			if (CommUtil.compare(CommTools.getBaseRunEnvs().getTrxn_date(),
					cplKnaAcct.getUpbldt()) < 0) {
				lastbl = cplKnaAcct.getOnlnbl();
			}

			// 获取转存签约明细信息
			IoCaKnaSignDetl cplkna_sign_detl = SysUtil.getInstance(
					IoCaSevQryTableInfo.class).getKnaSignDetlFirstOdb2(
					cplKnaAcct.getAcctno(), E_SIGNST.QY, false);

			// 存在转存签约明细信息则取资金池可用余额
			if (CommUtil.isNotNull(cplkna_sign_detl)) {
				acctbl = SysUtil.getInstance(DpAcctSvcType.class)
						.getProductBal(custac, crcycd, false);
			} else {
				// 其他取账户余额,正常的支取交易排除冻结金额
				acctbl = SysUtil.getInstance(DpAcctSvcType.class)
						.getOnlnblForFrozbl(cplKnaAcct.getAcctno(), false);
			}

			// 查询冻结登记簿
			Options<IoDpKnbFroz> lstKnbFroz = SysUtil.getInstance(
					IoDpFrozSvcType.class).qryKnbFroz(custac, E_FROZST.VALID);

			for (IoDpKnbFroz knbfroz : lstKnbFroz) {
				if (knbfroz.getFroztp() == E_FROZTP.ADD
						|| knbfroz.getFroztp() == E_FROZTP.JUDICIAL) {
					if (knbfroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
						frozam = frozam.add(knbfroz.getFrozam());// 冻结余额
					}
				}
				if (knbfroz.getFroztp() == E_FROZTP.BANKSTOPAY
						|| knbfroz.getFroztp() == E_FROZTP.DEPRSTOPAY
						|| knbfroz.getFroztp() == E_FROZTP.EXTSTOPAY) {
					if (knbfroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
						stopam = stopam.add(knbfroz.getFrozam());// 止付余额
					}
				}
				if (knbfroz.getFroztp() == E_FROZTP.FNFROZ) {
					if (knbfroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
						hdbkam = hdbkam.add(knbfroz.getFrozam());// 保留金额
					}
				}
			}

			// 查询预授权金额（一期暂无） TODO

			// 计算负债活期账户总余额
			BigDecimal bal = EacctMainDao.selSumEacctBlH(custac, crcycd, false);

			// 计算负债定期账户总余额
			BigDecimal bal1 = EacctMainDao.selSumEacctBlFx(custac, crcycd,
					false);

			// 计算存款余额
			dpacbl = bal.add(bal1);

			// 计算理财账户总余额
			BigDecimal bal2 = EacctMainDao.selSumFnacctBill(custac, crcycd,
					false);

			// 计算总资产
			acctam = bal.add(bal1).add(bal2);

			IoCaAcctInfoList cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);

			// 活期存款
			if (prodtp == E_PRODTP.CTDEPOSIT) {
				cplAcctInfo.setMxpdtp(prodtp);
				cplAcctInfo.setProdam(bal);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
			}
			// 定期存款
			else if (prodtp == E_PRODTP.FIXDEPOSIT) {
				cplAcctInfo.setMxpdtp(prodtp);
				cplAcctInfo.setProdam(bal1);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
			}
			// 理财
			else if (prodtp == E_PRODTP.MANAGE) {
				cplAcctInfo.setMxpdtp(prodtp);
				cplAcctInfo.setProdam(bal2);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
			}
			// 基金
			else if (prodtp == E_PRODTP.FUND) {
				cplAcctInfo.setMxpdtp(prodtp);
				cplAcctInfo.setProdam(BigDecimal.ZERO);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
			}
			// 贵金属
			else if (prodtp == E_PRODTP.PRECIOUS) {
				cplAcctInfo.setMxpdtp(prodtp);
				cplAcctInfo.setProdam(BigDecimal.ZERO);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
			}
			// 贷款
			else if (prodtp == E_PRODTP.LOAN) {
				cplAcctInfo.setMxpdtp(prodtp);
				cplAcctInfo.setProdam(BigDecimal.ZERO);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
			}
			// 信用卡
			else if (prodtp == E_PRODTP.BLUECARD) {
				cplAcctInfo.setMxpdtp(prodtp);
				cplAcctInfo.setProdam(BigDecimal.ZERO);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
			} else if (prodtp == E_PRODTP.LKBLCARD) {
				cplAcctInfo.setMxpdtp(prodtp);
				cplAcctInfo.setProdam(BigDecimal.ZERO);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
			}
			// 全部
			else if (prodtp == E_PRODTP.ALL) {
				cplAcctInfo.setMxpdtp(E_PRODTP.CTDEPOSIT);
				cplAcctInfo.setProdam(bal);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
				cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);
				cplAcctInfo.setMxpdtp(E_PRODTP.FIXDEPOSIT);
				cplAcctInfo.setProdam(bal1);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
				cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);
				cplAcctInfo.setMxpdtp(E_PRODTP.MANAGE);
				cplAcctInfo.setProdam(BigDecimal.ZERO);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
				cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);
				cplAcctInfo.setMxpdtp(E_PRODTP.FUND);
				cplAcctInfo.setProdam(BigDecimal.ZERO);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
				cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);
				cplAcctInfo.setMxpdtp(E_PRODTP.PRECIOUS);
				cplAcctInfo.setProdam(BigDecimal.ZERO);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
				cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);
				cplAcctInfo.setMxpdtp(E_PRODTP.LOAN);
				cplAcctInfo.setProdam(BigDecimal.ZERO);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
				cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);
				cplAcctInfo.setMxpdtp(E_PRODTP.BLUECARD);
				cplAcctInfo.setProdam(BigDecimal.ZERO);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
				cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);
				cplAcctInfo.setMxpdtp(E_PRODTP.LKBLCARD);
				cplAcctInfo.setProdam(BigDecimal.ZERO);
				cplLedgerInfosOut.getAcctInfoList().add(cplAcctInfo);
			} else {
				throw CaError.Eacct.E0001("请输入正确的产品类型");
			}

			// 赋值给输出
			CommUtil.copyProperties(cplLedgerInfosOut, accoutinfos);
			cplLedgerInfosOut.setAcstsz(cplGetAcStWord.getAcstsz());// 账户状态字
			cplLedgerInfosOut.setAcctam(acctam);// 总资产
			cplLedgerInfosOut.setDebtam(debtam);// 总负债
			cplLedgerInfosOut.setAcctbl(acctbl);// 可支取余额
			cplLedgerInfosOut.setDpacbl(dpacbl);// 存款余额
			cplLedgerInfosOut.setHdbkam(hdbkam);// 保留金额
			cplLedgerInfosOut.setLastbl(lastbl);// 昨日余额
			cplLedgerInfosOut.setOnlnbl(onlnbl);// 当前余额
			cplLedgerInfosOut.setPrauam(prauam);// 预授权金额
			cplLedgerInfosOut.setStopam(stopam);// 止付金额
			cplLedgerInfosOut.setFrozam(frozam);// 冻结金额
			cplLedgerInfosOut.setCustnm(idtftp + idtfno);// 统一后管客户号
		}
		// 查询历史库
		else if (qractp == E_QRYCTP.HIS) {

			throw CaError.Eacct.E0001("暂不支持历史库！");
		}

		return cplLedgerInfosOut;
	}

	/**
	 * 电子账户客户端分户账户查询服务实现
	 * 
	 */
	public IoCaClientLedgerOut qryClientLedger(String cardno, E_IDTFTP idtftp,
			String idtfno, String custid) {

		String ecarno = "";// 卡号
		String custac = "";//电子账号
		
		IoCaClientLedgerOut infoOut = SysUtil.getInstance(IoCaClientLedgerOut.class);

		if (CommUtil.isNotNull(cardno)) {
			ecarno = cardno;

			KnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCardno(ecarno,
					E_DPACST.NORMAL, false);
			if (CommUtil.isNull(tblKnaAcdc)) {
				throw CaError.Eacct.E0001("电子账号不存在");
			}
			
			custac = tblKnaAcdc.getCustac();
//			CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaAcdc.getCorpno());// 更新法人
		}

		// 输入的证件号码
		if (CommUtil.isNotNull(idtfno) || CommUtil.isNotNull(idtftp)) {

			if (CommUtil.isNull(idtftp)) {
				throw CaError.Eacct.E0001("证件类型和证件号码必须成对出现");
			}
			if (CommUtil.isNull(idtfno)) {
				throw CaError.Eacct.E0001("证件类型和证件号码必须成对出现");
			}

			// 根据证件查询电子账号
			ecarno = CaDao.selCardnoByIdtftpIdtfno(idtftp, idtfno, false);

			if (CommUtil.isNull(ecarno)) {
				throw CaError.Eacct.E0001("该证件对应电子账号不存在");
			}

			if (CommUtil.isNotNull(cardno)) {
				if (!CommUtil.equals(cardno, ecarno)) {
					throw CaError.Eacct.E0001("输入的证件号码与电子账号不匹配");
				}
			}

			KnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCardno(ecarno,
					E_DPACST.NORMAL, false);
			if (CommUtil.isNull(tblKnaAcdc)) {
				throw CaError.Eacct.E0001("电子账号不存在");
			}
			custac = tblKnaAcdc.getCustac();
//			CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaAcdc.getCorpno());// 更新法人

		}

		// 输入的用户ID
		if (CommUtil.isNotNull(custid)) {

			// 根据用户ID查询电子账号
			KnaCust tblKnaCust = CaDao.selCustacByCustid(custid, false);
			if (CommUtil.isNull(tblKnaCust)) {
				throw CaError.Eacct.E0001("用户ID不存在");
			}

//			CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaCust.getCorpno());// 更新法人

			KnaAcdc tblKnaAcdc = KnaAcdcDao.selectFirst_odb1(
					tblKnaCust.getCustac(), E_DPACST.NORMAL, false);
			if (CommUtil.isNull(tblKnaAcdc)) {
				throw CaError.Eacct.E0001("用户ID无对应状态为正常的电子账号");
			}

			ecarno = tblKnaAcdc.getCardno();
			custac = tblKnaAcdc.getCustac();
			
			if (CommUtil.isNotNull(cardno)) {

				if (!CommUtil.equals(cardno, ecarno)) {
					throw CaError.Eacct.E0001("输入的电子账号与用户ID不匹配");
				}
			}
			if (CommUtil.isNotNull(ecarno)) {
				// 根据证件查询电子账号
				String idcard = CaDao.selCardnoByIdtftpIdtfno(idtftp, idtfno,
						false);

				if (CommUtil.isNotNull(idcard)) {
					if (!CommUtil.equals(idcard, ecarno)) {
						throw CaError.Eacct.E0001("输入的证件号码与用户ID不匹配");
					}
				}
			}
			if (CommUtil.isNull(ecarno)) {
				throw CaError.Eacct.E0001("电子账号不存在");
			}

		}
		
		//新增电子账户状态和状态字检查 add by songkl 20170104
		if(CommUtil.isNotNull(custac)){
			IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
			if(E_YES___.YES == cplAcStatus.getClstop()){
				throw DpModuleError.DpstComm.E9999("电子账户处于账户保护状态！");
			}
			//电子账户状态检查
			E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
			if (CommUtil.isNotNull(cuacst)) {
				
				if (cuacst == E_CUACST.CLOSED) { // 销户
					throw DpModuleError.DpstComm.BNAS0883();

				} else if (cuacst == E_CUACST.OUTAGE) { // 停用
					throw DpModuleError.DpstComm.E9999("电子账户为停用");

				} else if (cuacst == E_CUACST.PREOPEN) { // 预开户
					throw DpModuleError.DpstComm.BNAS0881();
				}
			} else {
				throw DpModuleError.DpstComm.E9999("未找到电子账户客户化状态信息，请检查");
			}
		}

		// 查询信息
		infoOut = CaTools.qryClientLedgerByCustac(ecarno);

		//poc增加审计日志
		ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
		apAudit.regLogOnInsertBusiPoc(ecarno);
		
		return infoOut;
	}

	/**
	 * 查询电子账户下存款类子账户信息
	 */
	@Override
	public IoCaDpacctInfoList qryDpAcctInfo(IoCaQryAcctIn cplQracctIn) {

		String custac = null;
		String acctCorpno = null;

		// 校验相关信息
		E_CUACST cuacst = cplQracctIn.getCuacst();

		if (CommUtil.isNull(cuacst)) {
			throw CaError.Eacct.E0001("电子账户状态不能为空");
		}
		
		//mod by zdj 取消产品类型校验，银户通不传入产品类型  内管校验放到下面
		// 产品类型
		/*if (CommUtil.isNull(cplQracctIn.getProdtp())) {
			throw CaError.Eacct.E0001("产品类型不能为空");
		}*/
		//mod end

		// 币种
		String crcycd = cplQracctIn.getCrcycd();
		if (CommUtil.isNull(crcycd)) {
			throw DpModuleError.DpstComm.BNAS1101();
		}

		// 页数
		if (CommUtil.isNull(cplQracctIn.getPageno())) {
			throw CaError.Eacct.E0001("页数不能为空");
		}

		if (cplQracctIn.getPageno() <= 0) {
			throw CaError.Eacct.E0001("页数必须大于0");
		}

		// 页容量
		if (CommUtil.isNull(cplQracctIn.getPgsize())) {
			throw CaError.Eacct.E0001("页容量不能为空");
		}

		if (cplQracctIn.getPgsize() <= 0) {
			throw CaError.Eacct.E0001("页容量必须大于0");
		}

		// 证件类型不为空时，证件号码不能为空
		if (CommUtil.isNotNull(cplQracctIn.getIdtftp())
				&& CommUtil.isNull(cplQracctIn.getIdtfno())) {
			throw CaError.Eacct.E0001("证件号码不能为空");
		}

		// 证件号码不为空时，证件类型不能为空
		if (CommUtil.isNotNull(cplQracctIn.getIdtfno())
				&& CommUtil.isNull(cplQracctIn.getIdtftp())) {
			throw CaError.Eacct.E0001("证件类型不能为空");
		}

		// 新增查询方式、查询类型两个字段
		E_QRACWY qracwy = cplQracctIn.getQracwy();// 查询方式
		E_QRYCTP qractp = cplQracctIn.getQractp();// 查询类型

		if (CommUtil.isNull(qracwy)) {
			throw CaError.Eacct.BNAS1051();
		}

		if (CommUtil.isNull(qractp)) {
			throw CaError.Eacct.BNAS1272();
		}

		IoCaDpacctInfoList cplDpacctInfos = SysUtil
				.getInstance(IoCaDpacctInfoList.class);
		//add by zdj 20181026
		//目前暂不支持根据身份证查询，只支持电子账号进行查询
		Options<IoCaDpacctInfo> resultsAll = new DefaultOptions<IoCaDpacctInfo>();
		//银户通持仓查询不要显示Ⅱ类个人结算账户
		//KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("YHT", "qrdpai", "prodcd", "%", true);
		//银户通可能会根据产品编号进行查询
		//如果传入产品编号，则根据产品编号查询（可以查出Ⅱ类个人结算账户）
		//如果没有传入产品编号，则不查出Ⅱ类个人结算账户
		/*String prodcd = cplQracctIn.getProdcd();
		if(CommUtil.isNull(prodcd)){
		    prodcd = tblKnpParameter.getParm_value1();
		}*/
		int number = 0;
		Long countsAll = 0L;
		String cardno = null;
		boolean flag = false;
		//银户通的存期和系统内使用的存期枚举类型不同   银户通存期为depyht 系统内存期为depttm
		String depttm = null;//系统内存期为depttm
		//String depyht = null;//银户通存期为depyht
		//银户通的产品类型枚举和系统内使用的产品类型枚举不同
		E_PRODTP prodtp = null;//系统内产品类型
		E_PROYHT proyht = null;//银户通产品类型
		//银户通方不要利率上限和下限，只要获取执行利率，他们获取的利率会按一定规则排序，他们拿取执行利率在界面上显示当做最高利率，
		//DpInterestInfo interestInfo = null;//执行利率，利率上限，利率下限
		//为适应银户通请求，增加账号列表集合。内管传入的卡号不在账号列表集合中
		if(CommUtil.isNull(cplQracctIn.getCardno())){//内管调用传入卡号
		    //不是内管调用模式
		    flag = false;
		    for(int i=0;i<cplQracctIn.getAcctList().size();i++){
		        if(CommUtil.isNull(cplQracctIn.getAcctList().get(i).getCardno())){//银户通调用传入卡号
		            throw DpModuleError.DpstComm.E9999("电子账号不能为空");
		        }
		    }
		    number = cplQracctIn.getAcctList().size();
		    /*if(CommUtil.isNotNull(cplQracctIn.getDepyht())){//银户通要求，如果传入存期，也不根据存期进行查询
		        //无论银户通传入存期多少，系统内使用的存期都为空，都默认不根据存期查询
		        depyht = cplQracctIn.getDepyht().toString();
		        depttm = convertToDepttm(depyht.toString()).toString();
		    }*/
		    if(CommUtil.isNotNull(cplQracctIn.getProyht())){
		        prodtp = convertToProdtp(cplQracctIn.getProyht());
		    }else{//如果银户通没有传入产品类型，则默认查询所有产品
		        prodtp = E_PRODTP.ALL;
		    }
		}else{
		    // 产品类型
	        if (CommUtil.isNull(cplQracctIn.getProdtp())) {
	            throw CaError.Eacct.E0001("产品类型不能为空");
	        }
		    //是内管调用模式
		    flag = true;
		    number = 1;
		    prodtp = cplQracctIn.getProdtp();
		    if(CommUtil.isNotNull(cplQracctIn.getDepttm())){
		        depttm = cplQracctIn.getDepttm().toString();
		    }
		}
		//add end

		// 查当前库
		if (qractp == E_QRYCTP.NOW) {
			// 电子账号与证件信息不能同时为空
		    for(int i=0;i<number;i++){
		        if(flag){//是内管调用模式
		            cardno = cplQracctIn.getCardno();
		        }else{
		            cardno = cplQracctIn.getAcctList().get(i).getCardno();
		        }
		        if (CommUtil.isNull(cardno)) {
		            if (CommUtil.isNull(cplQracctIn.getIdtftp())
		                    || CommUtil.isNull(cplQracctIn.getIdtfno())) {
//					throw CaError.Eacct.BNAS0910();
		                throw DpModuleError.DpstComm.E9999("电子账号不能为空");
		            }
		            
		            E_IDTFTP idtftp = cplQracctIn.getIdtftp();
		            String idtfno = cplQracctIn.getIdtfno();
		            
		            KnaCust tblKnaCust = EacctMainDao.selKnaCustByIdtfnoAndAcctst(idtftp, idtfno, cuacst, false);
		            
		            
		            if (CommUtil.isNull(tblKnaCust)) {
		                throw CaError.Eacct.BNAS1249();
		            }
		            
		            if (qracwy == E_QRACWY.CUSQRY) {
//					CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaCust.getCorpno());
		            }
		            
		            custac = tblKnaCust.getCustac();
		            acctCorpno = tblKnaCust.getCorpno();
		        } else {
		            IoCaKnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCard(cardno, false);
		            if (CommUtil.isNull(tblKnaAcdc)) {
		                throw CaError.Eacct.BNAS0750();
		            }
		            
		            if (qracwy == E_QRACWY.CUSQRY) {
//					CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaAcdc.getCorpno());
		            }
		            
		            if (CommUtil.isNotNull(cplQracctIn.getIdtfno())
		                    && CommUtil.isNotNull(cplQracctIn.getIdtftp())) {
		                
		                // 查询证件信息对应的电子账号
		                E_IDTFTP idtftp = cplQracctIn.getIdtftp();
		                String idtfno = cplQracctIn.getIdtfno();
		                
		                KnaCust tblKnaCust = EacctMainDao
		                        .selKnaCustByIdtfnoAndAcctst(idtftp, idtfno,
		                                cuacst, false);
		                
		                if (CommUtil.isNull(tblKnaCust)) {
		                    throw CaError.Eacct.BNAS1249();
		                }
		                
		                // 判断电子账号与证件对应的电子账号是否一致
		                if (!CommUtil.equals(tblKnaAcdc.getCustac(),
		                        tblKnaCust.getCustac())) {
		                    throw CaError.Eacct.BNAS0909();
		                }
		            }
		            
		            custac = tblKnaAcdc.getCustac();
		            acctCorpno = tblKnaAcdc.getCorpno();
		        }
		        
		        // 如果查询方式为柜员查询，只能查询本行社电子账户信息
		        if (qracwy == E_QRACWY.TELQRY) {
		            if (!CommUtil.equals(CommTools.getBaseRunEnvs().getBusi_org_id(), "999")) {
		                String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		                if (CommUtil.compare(corpno, acctCorpno) != 0) {
		                    throw CaError.Eacct.BNAS0112();
		                }
		            }
		        }
		        //add by zdj 20181026
		        if(CommUtil.isNotNull(depttm)){
		            //无论银户通传入存期多少，系统内使用的存期都为空，都默认不根据存期查询
		            depttm = "";//存期为所有时，则不根据存期查询
		            //如果传入存期，则根据存期查询
		            if(CommUtil.toEnum(E_TERMCD.class, depttm) ==E_TERMCD.T000){//活期存款
		                if(prodtp !=E_PRODTP.CTDEPOSIT && prodtp !=E_PRODTP.ALL){
		                    throw CaError.Eacct.E0001("查询期限和产品类型不符合");
		                }
		                cplQracctIn.setProdtp(E_PRODTP.CTDEPOSIT);//直接查询活期存款
		            }else if(CommUtil.toEnum(E_TERMCD.class, depttm) == E_TERMCD.ALL || CommUtil.isNull(depttm)){//查询全部存期或者没有传入存期，则查所有活期和定期存款
		                if(prodtp != E_PRODTP.CTDEPOSIT){
		                    cplQracctIn.setProdtp(E_PRODTP.ALL);//查询所有存款
		                }
		                if(prodtp == E_PRODTP.CTDEPOSIT){
		                    System.out.println("活期");
		                    cplQracctIn.setProdtp(E_PRODTP.CTDEPOSIT);//直接查询活期存款
		                }
		            }else{//查询有期限的定期存款
		                if(prodtp !=E_PRODTP.FIXDEPOSIT && prodtp !=E_PRODTP.ALL){
		                    throw CaError.Eacct.E0001("查询期限和产品类型不符合");
		                }
		                cplQracctIn.setProdtp(E_PRODTP.FIXDEPOSIT);//直接查询定期存款
		            }
		        }
		        
		        //查询所有存款
		        if (E_PRODTP.ALL == prodtp) {
		            int count = 0; // 总笔数
		            String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		            int pageno = cplQracctIn.getPageno(); // 页码
		            int pgsize = cplQracctIn.getPgsize(); // 页容量
		            int starno = (pageno - 1) * pgsize; // 起始数
		            
		            String acctno = "";// 升级销掉结算户子账号
		            List<IoDpKnaAcct> lstKnaAcct = EacctMainDao.selKnaAcctByactp(custac, E_ACSETP.SA, false);
		            if (CommUtil.isNotNull(lstKnaAcct) && lstKnaAcct.size() > 1) {
		                acctno = lstKnaAcct.get(0).getAcctno();
		            }
		            
		            //查询所有存款中的查询活期存款
					Page<IoCaDpacctInfo> lstDpacctInfo = SysUtil.getInstance(Page.class);
                    if(flag){//是内管调用模式
                        lstDpacctInfo = EacctMainDao.selDpAcctInfoByCustac(custac, trandt, crcycd, prodtp, acctno ,cuacst.getValue(),starno, pgsize, count,  false);
                    }else{//银户通调用可以根据产品编号和资产状态查询
                        if(CommUtil.isNotNull(cplQracctIn.getAstate())){
                            if(cplQracctIn.getAstate()==E_ASTATE.WDQ){//未到期
                                lstDpacctInfo = YhtDao.selDpAcctInfoByCustacAstateWdqProdcd(custac, trandt, crcycd, prodtp, acctno, cuacst.getValue(), cplQracctIn.getProdcd(), starno, pgsize, count, false);
                            }else if(cplQracctIn.getAstate()==E_ASTATE.DQ){//到期
                                lstDpacctInfo = YhtDao.selDpAcctInfoByCustacAstateDqProdcd(custac, trandt, crcycd, prodtp, acctno, cuacst.getValue(), cplQracctIn.getProdcd(), starno, pgsize, count, false);
                            }
                        }else{
                            lstDpacctInfo = YhtDao.selDpAcctInfoByCustacAstateProdcd(custac, trandt, crcycd, prodtp, acctno, cuacst.getValue(), cplQracctIn.getProdcd(), starno, pgsize, count, false);
                        }
                    //在命名sql中联表查询增加订单登记薄，所以不会查询出II类结算账户
                    /*}else{//银户通没有传入产品编号，则查出所有存款产品，不包括II类结算账户
                        if(CommUtil.isNotNull(cplQracctIn.getAstate())){
                            if(cplQracctIn.getAstate()==E_ASTATE.WDQ){//未到期
                                lstDpacctInfo = YhtDao.selDpAcctInfoByCustacAstateWdqProdcdProdII(custac, trandt, crcycd, prodtp, acctno, cuacst.getValue(), tblKnpParameter.getParm_value1(), starno, pgsize, count, false);
                            }else if(cplQracctIn.getAstate()==E_ASTATE.DQ){//到期
                                lstDpacctInfo = YhtDao.selDpAcctInfoByCustacAstateDqProdcdProdII(custac, trandt, crcycd, prodtp, acctno, cuacst.getValue(), tblKnpParameter.getParm_value1(), starno, pgsize, count, false);
                            }
                        }else{
                            lstDpacctInfo = YhtDao.selDpAcctInfoByCustacAstateProdcdProdII(custac, trandt, crcycd, prodtp, acctno, cuacst.getValue(), tblKnpParameter.getParm_value1(), starno, pgsize, count, false);
                        }*/
                    }
		            if (CommUtil.isNotNull(lstDpacctInfo)) {
		                Options<IoCaDpacctInfo> cplDpaccts = new DefaultOptions<IoCaDpacctInfo>();
		                
		                cplDpaccts.addAll(lstDpacctInfo.getRecords());
		                
		                //循环遍历每条记录，获取存款账号的利率并更新到该记录中
		                for(IoCaDpacctInfo cplDpAcctInfo:cplDpaccts){
		                    
		                    //获取负债账户计息信息
		                    IoDpKnbAcin cplKnbAcin= EacctMainDao.selKnbAcinInfoByAcctno(cplDpAcctInfo.getAcctno(), true);
		                    //将查询的负债账户信息和定期信息传入方法中，获取对应产品的利率
		                    BigDecimal cuusin = prcAcctIntrvlDetl(cplKnbAcin,cplDpAcctInfo);
		                    cplDpAcctInfo.setIntrvl(cuusin);
		                    KupDppb tblKupDppb = KupDppbDao.selectOne_odb1(cplDpAcctInfo.getProdcd(), false);
		                    KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(cplDpAcctInfo.getAcctno(), false);
		                    if(CommUtil.isNull(tblKupDppb)){
		                        throw CaError.Eacct.E0001("产品编号不存在");
		                    }
		                    if(CommUtil.isNotNull(tblKnaAcct)){
		                        if(CommUtil.isNull(tblKnaAcct.getMatudt())){
		                            cplDpAcctInfo.setAstate(null);//资产状态
		                        }else{
		                            if(CommUtil.compare(tblKnaAcct.getMatudt(), CommTools.getBaseRunEnvs().getTrxn_date())>0){
		                                cplDpAcctInfo.setAstate(E_ASTATE.WDQ);//资产状态
		                            }else if(CommUtil.compare(tblKnaAcct.getMatudt(), CommTools.getBaseRunEnvs().getTrxn_date())<=0){
		                                cplDpAcctInfo.setAstate(E_ASTATE.DQ);//资产状态
		                            }
		                        }
		                        cplDpAcctInfo.setDepttm(tblKnaAcct.getDepttm());//存期
		                    }
		                    if(CommUtil.isNotNull(cardno)){
		                        cplDpAcctInfo.setCardno(cardno);//卡号
		                    }else if(CommUtil.isNull(cardno)){
		                        KnaAccs tblKnaAccs = KnaAccsDao.selectOne_odb2(cplDpAcctInfo.getAcctno(), false);
		                        KnaAcdc tblKnaAcdc = KnaAcdcDao.selectFirst_odb3(tblKnaAccs.getAcctno(), false);
		                        cplDpAcctInfo.setCardno(tblKnaAcdc.getCardno());//卡号
		                    }
		                    cplDpAcctInfo.setProdna(tblKupDppb.getProdtx());//产品名称
		                    cplDpAcctInfo.setAcctdt("");//签约日期/购买日期
		                    if(CommUtil.isNull(YhtDao.selKnbCbdlByAcctnoOne(cplDpAcctInfo.getAcctno(), CommTools.getBaseRunEnvs().getTrxn_date(), false))){
		                        //没有计提明细，则累计收益为0
		                        cplDpAcctInfo.setTotaea(BigDecimal.ZERO);
		                    }else{
		                        cplDpAcctInfo.setTotaea(YhtDao.selKnbCbdlByAcctnoOne(cplDpAcctInfo.getAcctno(), CommTools.getBaseRunEnvs().getTrxn_date(), true).getCabrin());//累计收益
		                    }
		                    //cplDpAcctInfo.setIntrup(interestInfo.getIntrup());//利率上限
		                    //cplDpAcctInfo.setIntrfl(interestInfo.getIntrfl());//利率下限
		                    cplDpAcctInfo.setProdtp(E_PRODTP.CTDEPOSIT);//活期查询方法，查询出产品类型为活期
		                    resultsAll.add(cplDpAcctInfo);
		                }
                        if(flag){//true是内管调用模式
                            
                        }else{//false不是内管调用模式
                            for(int j=0;j<resultsAll.size();j++){//把存期对应的枚举类型转换成银户通存期对应的枚举类型
                                E_DEPYHT depYht = convertToDepyht(resultsAll.get(j).getDepttm().toString());
                                proyht = convertToProyht(resultsAll.get(j).getProdtp());
                                resultsAll.get(j).setDepyht(depYht);
                                resultsAll.get(j).setProyht(proyht);
                            }
                        }
		            }
		            
		            //查询所有存款中的查询定期存款
		            // 分页查询存款类子账户信息
//		            Page<IoCaDpacctInfo> lstDpFxacInfo = EacctMainDao.selDpFxacInfoByCustac(custac, trandt, crcycd, cplQracctIn.getProdtp(),cuacst.getValue(), starno, pgsize, count, false);
		            Page<IoCaDpacctInfo> lstDpFxacInfo = SysUtil.getInstance(Page.class);
		            
                    if(CommUtil.isNotNull(cplQracctIn.getAstate())){
                        if(cplQracctIn.getAstate()==E_ASTATE.WDQ){//未到期
                            lstDpFxacInfo = YhtDao.selDpFxacInfoByCustacAstateWdqProdcdDepttm(custac, trandt, crcycd, prodtp, cuacst.getValue(), cplQracctIn.getProdcd(), depttm, starno, pgsize, count, false);
                        }else if(cplQracctIn.getAstate()==E_ASTATE.DQ){//到期
                            lstDpFxacInfo = YhtDao.selDpFxacInfoByCustacAstateDqProdcdDepttm(custac, trandt, crcycd, prodtp, cuacst.getValue(), cplQracctIn.getProdcd(), depttm, starno, pgsize, count, false);
                        }
                    }else{
                        lstDpFxacInfo = YhtDao.selDpFxacInfoByCustacAstateProdcdDepttm(custac, trandt, prodtp, crcycd, cuacst.getValue(), cplQracctIn.getProdcd(), depttm, starno, pgsize, count, false);
                    }
		            
		            if (CommUtil.isNotNull(lstDpFxacInfo)) {
		                Options<IoCaDpacctInfo> cplDpaccts = new DefaultOptions<IoCaDpacctInfo>();
		                cplDpaccts.addAll(lstDpFxacInfo.getRecords());
		                
		                //循环遍历每条记录，获取存款账号的利率并更新到该记录中
		                for(IoCaDpacctInfo cplDpFxacInfo:cplDpaccts){
		                    
		                    //获取负债账户计息信息
		                    IoDpKnbAcin cplKnbAcin= EacctMainDao.selKnbAcinInfoByAcctno(cplDpFxacInfo.getAcctno(), true);
		                    //将查询的负债账户信息和定期信息传入方法中，获取对应产品的利率
		                    BigDecimal cuusin = prcFxacIntrvlDetl(cplKnbAcin,cplDpFxacInfo);
		                    cplDpFxacInfo.setIntrvl(cuusin);
		                    KupDppb tblKupDppb = KupDppbDao.selectOne_odb1(cplDpFxacInfo.getProdcd(), false);
		                    //KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(cplDpFxacInfo.getAcctno(), false);
		                    KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(cplDpFxacInfo.getAcctno(), false);
		                    if(CommUtil.isNull(tblKupDppb)){
		                        throw CaError.Eacct.E0001("产品编号不存在");
		                    }
		                    if(CommUtil.isNotNull(tblKnaFxac)){
		                        if(CommUtil.compare(tblKnaFxac.getMatudt(), CommTools.getBaseRunEnvs().getTrxn_date())>0){
		                            cplDpFxacInfo.setAstate(E_ASTATE.WDQ);
		                        }else if(CommUtil.compare(tblKnaFxac.getMatudt(), CommTools.getBaseRunEnvs().getTrxn_date())<=0){
		                            cplDpFxacInfo.setAstate(E_ASTATE.DQ);
		                        }
		                        cplDpFxacInfo.setDepttm(tblKnaFxac.getDepttm());//存期
		                    }
		                    if(CommUtil.isNotNull(cardno)){
		                        cplDpFxacInfo.setCardno(cardno);//卡号
		                    }else if(CommUtil.isNull(cardno)){
		                        KnaAccs tblKnaAccs = KnaAccsDao.selectOne_odb2(cplDpFxacInfo.getAcctno(), false);
		                        KnaAcdc tblKnaAcdc = KnaAcdcDao.selectFirst_odb3(tblKnaAccs.getAcctno(), false);
		                        cplDpFxacInfo.setCardno(tblKnaAcdc.getCardno());//卡号
		                    }
		                    cplDpFxacInfo.setProdna(tblKupDppb.getProdtx());//产品名称
		                    cplDpFxacInfo.setAcctdt("");//签约日期/购买日期
		                    if(CommUtil.isNull(YhtDao.selKnbCbdlByAcctnoOne(cplDpFxacInfo.getAcctno(), CommTools.getBaseRunEnvs().getTrxn_date(), false))){
                                //没有计提明细，则累计收益为0
		                        cplDpFxacInfo.setTotaea(BigDecimal.ZERO);
                            }else{
                                cplDpFxacInfo.setTotaea(YhtDao.selKnbCbdlByAcctnoOne(cplDpFxacInfo.getAcctno(), CommTools.getBaseRunEnvs().getTrxn_date(), true).getCabrin());//累计收益
                            }
		                    //cplDpFxacInfo.setIntrup(interestInfo.getIntrup());//利率上限
		                    //cplDpFxacInfo.setIntrfl(interestInfo.getIntrfl());//利率下限
		                    cplDpFxacInfo.setProdtp(E_PRODTP.FIXDEPOSIT);//定期查询方法，查询出产品类型为定期
		                    resultsAll.add(cplDpFxacInfo);
		                }
		            }
		            if(flag){//true是内管调用模式
		                
		            }else{//false不是内管调用模式
		                for(int j=0;j<resultsAll.size();j++){//把存期对应的枚举类型转换成银户通存期对应的枚举类型
		                    E_DEPYHT depYht = convertToDepyht(resultsAll.get(j).getDepttm().toString());
		                    proyht = convertToProyht(resultsAll.get(j).getProdtp());
                            resultsAll.get(j).setDepyht(depYht);
                            resultsAll.get(j).setProyht(proyht);
		                }
		            }
		            cplDpacctInfos.setCplQracctInfo(resultsAll);
		            CommTools.getBaseRunEnvs().setTotal_count(lstDpFxacInfo.getRecordCount()+lstDpacctInfo.getRecordCount()+countsAll); // 暂时先在服务中映射
		        }
		        //add end
		        // 查询活期类存款
		        else if (E_PRODTP.CTDEPOSIT == prodtp) {
		            
		            int count = 0; // 总笔数
		            String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		            int pageno = cplQracctIn.getPageno(); // 页码
		            int pgsize = cplQracctIn.getPgsize(); // 页容量
		            int starno = (pageno - 1) * pgsize; // 起始数
		            
		            String acctno = "";// 升级销掉结算户子账号
		            List<IoDpKnaAcct> lstKnaAcct = EacctMainDao.selKnaAcctByactp(custac, E_ACSETP.SA, false);
		            if (CommUtil.isNotNull(lstKnaAcct) && lstKnaAcct.size() > 1) {
		                acctno = lstKnaAcct.get(0).getAcctno();
		            }
		            
		            //mod by zdj 20181117 增加根据资产状态和产品编号查询
		            // 分页查询存款类子账户信息
//		            Page<IoCaDpacctInfo> lstDpacctInfo = EacctMainDao.selDpAcctInfoByCustac(custac, trandt, crcycd, cplQracctIn.getProdtp(), acctno ,cuacst.getValue(),starno, pgsize, count,  false);
		            Page<IoCaDpacctInfo> lstDpacctInfo = SysUtil.getInstance(Page.class);
		            if(flag){//是内管调用模式
		                lstDpacctInfo = EacctMainDao.selDpAcctInfoByCustac(custac, trandt, crcycd, prodtp, acctno ,cuacst.getValue(),starno, pgsize, count,  false);
		            }else{//银户通调用可以根据产品编号和资产状态查询
	                    if(CommUtil.isNotNull(cplQracctIn.getAstate())){
	                        if(cplQracctIn.getAstate()==E_ASTATE.WDQ){//未到期
	                            lstDpacctInfo = YhtDao.selDpAcctInfoByCustacAstateWdqProdcd(custac, trandt, crcycd, prodtp, acctno, cuacst.getValue(), cplQracctIn.getProdcd(), starno, pgsize, count, false);
	                        }else if(cplQracctIn.getAstate()==E_ASTATE.DQ){//到期
	                            lstDpacctInfo = YhtDao.selDpAcctInfoByCustacAstateDqProdcd(custac, trandt, crcycd, prodtp, acctno, cuacst.getValue(), cplQracctIn.getProdcd(), starno, pgsize, count, false);
	                        }
	                    }else{
	                        lstDpacctInfo = YhtDao.selDpAcctInfoByCustacAstateProdcd(custac, trandt, crcycd, prodtp, acctno, cuacst.getValue(), cplQracctIn.getProdcd(), starno, pgsize, count, false);
	                    }
	                  //在命名sql中联表查询增加订单登记薄，所以不会查询出II类结算账户 
	                /*}else{//银户通没有传入产品编号，则查出所有存款产品，不包括II类结算账户
	                    if(CommUtil.isNotNull(cplQracctIn.getAstate())){
                            if(cplQracctIn.getAstate()==E_ASTATE.WDQ){//未到期
                                lstDpacctInfo = YhtDao.selDpAcctInfoByCustacAstateWdqProdcdProdII(custac, trandt, crcycd, prodtp, acctno, cuacst.getValue(), tblKnpParameter.getParm_value1(), starno, pgsize, count, false);
                            }else if(cplQracctIn.getAstate()==E_ASTATE.DQ){//到期
                                lstDpacctInfo = YhtDao.selDpAcctInfoByCustacAstateDqProdcdProdII(custac, trandt, crcycd, prodtp, acctno, cuacst.getValue(), tblKnpParameter.getParm_value1(), starno, pgsize, count, false);
                            }
                        }else{
                            lstDpacctInfo = YhtDao.selDpAcctInfoByCustacAstateProdcdProdII(custac, trandt, crcycd, prodtp, acctno, cuacst.getValue(), tblKnpParameter.getParm_value1(), starno, pgsize, count, false);
                        }*/
		            }
                    //mod by zdj end
		            if (CommUtil.isNotNull(lstDpacctInfo)) {
		                Options<IoCaDpacctInfo> cplDpaccts = new DefaultOptions<IoCaDpacctInfo>();
		                
		                cplDpaccts.addAll(lstDpacctInfo.getRecords());
		                //mod by zdj
		                //将下面遍历过的记录更新利率后塞到results集合中
		                //Options<IoCaDpacctInfo> results = new DefaultOptions<IoCaDpacctInfo>();//将下面遍历过的记录塞到resultsAll集合中
		                //mod end
		                //循环遍历每条记录，获取存款账号的利率并更新到该记录中
		                for(IoCaDpacctInfo cplDpAcctInfo:cplDpaccts){
		                    
		                    //获取负债账户计息信息
		                    IoDpKnbAcin cplKnbAcin= EacctMainDao.selKnbAcinInfoByAcctno(cplDpAcctInfo.getAcctno(), true);
		                    //将查询的负债账户信息和定期信息传入方法中，获取对应产品的利率
		                    BigDecimal cuusin = prcAcctIntrvlDetl(cplKnbAcin,cplDpAcctInfo);
		                    cplDpAcctInfo.setIntrvl(cuusin);
		                    //add by zdj 20181025
		                    KupDppb tblKupDppb = KupDppbDao.selectOne_odb1(cplDpAcctInfo.getProdcd(), false);
		                    KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(cplDpAcctInfo.getAcctno(), false);
		                    if(CommUtil.isNull(tblKupDppb)){
		                        throw CaError.Eacct.E0001("产品编号不存在");
		                    }
		                    if(CommUtil.isNotNull(tblKnaAcct)){
		                        if(CommUtil.isNull(tblKnaAcct.getMatudt())){
		                            cplDpAcctInfo.setAstate(E_ASTATE.WDQ);
		                        }else{
		                            if(CommUtil.compare(tblKnaAcct.getMatudt(), CommTools.getBaseRunEnvs().getTrxn_date())>0){
		                                cplDpAcctInfo.setAstate(E_ASTATE.WDQ);
		                            }else if(CommUtil.compare(tblKnaAcct.getMatudt(), CommTools.getBaseRunEnvs().getTrxn_date())<=0){
		                                cplDpAcctInfo.setAstate(E_ASTATE.DQ);
		                            }
		                        }
		                    }
		                    cplDpAcctInfo.setCardno(cardno);//卡号
		                    cplDpAcctInfo.setProdna(tblKupDppb.getProdtx());//产品名称
		                    cplDpAcctInfo.setAcctdt("");//签约日期/购买日期
		                    if(CommUtil.isNull(YhtDao.selKnbCbdlByAcctnoOne(cplDpAcctInfo.getAcctno(), CommTools.getBaseRunEnvs().getTrxn_date(), false))){
                                //没有计提明细，则累计收益为0
		                        cplDpAcctInfo.setTotaea(BigDecimal.ZERO);
                            }else{
                                cplDpAcctInfo.setTotaea(YhtDao.selKnbCbdlByAcctnoOne(cplDpAcctInfo.getAcctno(), CommTools.getBaseRunEnvs().getTrxn_date(), true).getCabrin());//累计收益
                            }
		                    //cplDpAcctInfo.setIntrup(interestInfo.getIntrup());//利率上限
		                    //cplDpAcctInfo.setIntrfl(interestInfo.getIntrfl());//利率下限
		                    cplDpAcctInfo.setProdtp(E_PRODTP.CTDEPOSIT);//活期查询方法，查询出产品类型为活期
		                    resultsAll.add(cplDpAcctInfo);
		                }
		                if(flag){//true是内管调用模式
                            
                        }else{//false不是内管调用模式
                            for(int j=0;j<resultsAll.size();j++){//把存期对应的枚举类型转换成银户通存期对应的枚举类型
                                E_DEPYHT depYht = convertToDepyht(resultsAll.get(j).getDepttm().toString());
                                proyht = convertToProyht(resultsAll.get(j).getProdtp());
                                resultsAll.get(j).setDepyht(depYht);
                                resultsAll.get(j).setProyht(proyht);
                            }
                        }
		                //add end
		                cplDpacctInfos.setCplQracctInfo(resultsAll);
		            }
		            
		            //cplDpacctInfos.setCounts(ConvertUtil.toInteger(lstDpacctInfo.getRecordCount())); // 总条数
		            CommTools.getBaseRunEnvs().setTotal_count(
		                    lstDpacctInfo.getRecordCount()+countsAll); // 暂时先在服务中映射
		        }
		        // 查询电子账户定期存款产品信息
		        else if (E_PRODTP.FIXDEPOSIT == prodtp) {
		            int count = 0;
		            String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		            int pageno = cplQracctIn.getPageno(); // 页码
		            int pgsize = cplQracctIn.getPgsize(); // 页容量
		            int starno = (pageno - 1) * pgsize; // 起始数
		            Page<IoCaDpacctInfo> lstDpFxacInfo = null;
		            //mod by zdj 20181114  增加根据期限、资产状态、产品编号查询定期产品
	                // 分页查询存款类子账户信息
//		            lstDpFxacInfo = EacctMainDao.selDpFxacInfoByCustac(custac, trandt, crcycd, cplQracctIn.getProdtp(),cuacst.getValue(), starno, pgsize, count, false);
	                if(CommUtil.isNotNull(cplQracctIn.getAstate())){
	                    if(cplQracctIn.getAstate()==E_ASTATE.WDQ){//未到期
	                        lstDpFxacInfo = YhtDao.selDpFxacInfoByCustacAstateWdqProdcdDepttm(custac, trandt, crcycd, prodtp, cuacst.getValue(), cplQracctIn.getProdcd(), depttm, starno, pgsize, count, false);
	                    }else if(cplQracctIn.getAstate()==E_ASTATE.DQ){//到期
	                        lstDpFxacInfo = YhtDao.selDpFxacInfoByCustacAstateDqProdcdDepttm(custac, trandt, crcycd, prodtp, cuacst.getValue(), cplQracctIn.getProdcd(), depttm, starno, pgsize, count, false);
	                    }
	                }else{
	                    lstDpFxacInfo = YhtDao.selDpFxacInfoByCustacAstateProdcdDepttm(custac, trandt, prodtp, crcycd, cuacst.getValue(), cplQracctIn.getProdcd(), depttm, starno, pgsize, count, false);
	                }
		            //mod end
		            if (CommUtil.isNotNull(lstDpFxacInfo)) {
		                Options<IoCaDpacctInfo> cplDpaccts = new DefaultOptions<IoCaDpacctInfo>();
		                cplDpaccts.addAll(lstDpFxacInfo.getRecords());
		                
		                //mod by zdj 
		                //将下面遍历过的记录塞到results集合中
		                //Options<IoCaDpacctInfo> results = new DefaultOptions<IoCaDpacctInfo>();//将下面遍历过的记录塞到resultsAll集合中
		                //mod end
		                //循环遍历每条记录，获取存款账号的利率并更新到该记录中
		                for(IoCaDpacctInfo cplDpFxacInfo:cplDpaccts){
		                    
		                    //获取负债账户计息信息
		                    IoDpKnbAcin cplKnbAcin= EacctMainDao.selKnbAcinInfoByAcctno(cplDpFxacInfo.getAcctno(), true);
		                    //将查询的负债账户信息和定期信息传入方法中，获取对应产品的利率
		                    BigDecimal cuusin = prcFxacIntrvlDetl(cplKnbAcin,cplDpFxacInfo);
		                    cplDpFxacInfo.setIntrvl(cuusin);
		                    //add by zdj 20181025
		                    KupDppb tblKupDppb = KupDppbDao.selectOne_odb1(cplDpFxacInfo.getProdcd(), false);
		                    //KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(cplDpFxacInfo.getAcctno(), false);
		                    KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(cplDpFxacInfo.getAcctno(), false);
		                    if(CommUtil.isNull(tblKupDppb)){
		                        throw CaError.Eacct.E0001("产品编号不存在");
		                    }
		                    if(CommUtil.isNotNull(tblKnaFxac)){
		                        if(CommUtil.compare(tblKnaFxac.getMatudt(), CommTools.getBaseRunEnvs().getTrxn_date())>0){
		                            cplDpFxacInfo.setAstate(E_ASTATE.WDQ);
		                        }else if(CommUtil.compare(tblKnaFxac.getMatudt(), CommTools.getBaseRunEnvs().getTrxn_date())<=0){
		                            cplDpFxacInfo.setAstate(E_ASTATE.DQ);
		                        }
		                    }
		                    cplDpFxacInfo.setCardno(cardno);//卡号
		                    cplDpFxacInfo.setProdna(tblKupDppb.getProdtx());//产品名称
		                    cplDpFxacInfo.setAcctdt("");//签约日期/购买日期
		                    if(CommUtil.isNull(YhtDao.selKnbCbdlByAcctnoOne(cplDpFxacInfo.getAcctno(), CommTools.getBaseRunEnvs().getTrxn_date(), false))){
                                //没有计提明细，则累计收益为0
		                        cplDpFxacInfo.setTotaea(BigDecimal.ZERO);
                            }else{
                                cplDpFxacInfo.setTotaea(YhtDao.selKnbCbdlByAcctnoOne(cplDpFxacInfo.getAcctno(), CommTools.getBaseRunEnvs().getTrxn_date(), true).getCabrin());//累计收益
                            }
		                    //cplDpFxacInfo.setIntrup(interestInfo.getIntrup());//利率上限
		                    //cplDpFxacInfo.setIntrfl(interestInfo.getIntrfl());//利率下限
                            cplDpFxacInfo.setProdtp(E_PRODTP.FIXDEPOSIT);//定期查询方法，查询出产品类型为定期
		                    //add end
		                    //result.add(cplDpFxacInfo);
		                    resultsAll.add(cplDpFxacInfo);
		                }
		                if(flag){//true是内管调用模式
		                    
		                }else{//false不是内管调用模式
		                    for(int j=0;j<resultsAll.size();j++){//把存期对应的枚举类型转换成银户通存期对应的枚举类型
		                        E_DEPYHT depYht = convertToDepyht(resultsAll.get(j).getDepttm().toString());
		                        proyht = convertToProyht(resultsAll.get(j).getProdtp());
		                        resultsAll.get(j).setDepyht(depYht);
		                        resultsAll.get(j).setProyht(proyht);
		                    }
		                }
		                cplDpacctInfos.setCplQracctInfo(resultsAll);
		            }
		            
		            //cplDpacctInfos.setCounts(ConvertUtil.toInteger(lstDpFxacInfo.getRecordCount())); // 总条数
		            CommTools.getBaseRunEnvs().setTotal_count(lstDpFxacInfo.getRecordCount()+countsAll); // 暂时先在服务中映射
		        } else {
		            throw CaError.Eacct.BNAS0725(prodtp.getLongName());
		        }
		    }
		} else {

			// TODO 查历史库
			throw CaError.Eacct.BNAS0210();
		}

		return cplDpacctInfos;
	}

	/**
	 * 
	 * <p>Title:convertToProdtp </p>
	 * <p>Description:将系统内的产品枚举类型转换成银户通要求的产品枚举类型	</p>
	 * @author Administrator
	 * @date   2018年12月5日 
	 * @param proyht
	 * @return
	 */
	private E_PROYHT convertToProyht(E_PRODTP prodtp){
	    E_PROYHT proyht = null;
	    if(CommUtil.compare(prodtp, E_PRODTP.CTDEPOSIT)==0){//活期存款
	        proyht = E_PROYHT.CTDEPOSIT;
	    }else if(CommUtil.compare(prodtp, E_PRODTP.FIXDEPOSIT)==0){//定期存款
	        proyht = E_PROYHT.FIXDEPOSIT;
	    }else {
	        throw CaError.Eacct.E0001("暂不支持该产品类型的转换");
	    }
	    return proyht;
	}
	/**
	 * 
	 * <p>Title:convertToProdtp </p>
	 * <p>Description:将银户通要求的产品枚举类型转换成系统内的产品枚举类型	</p>
	 * @author Administrator
	 * @date   2018年12月5日 
	 * @param proyht
	 * @return
	 */
	private E_PRODTP convertToProdtp(E_PROYHT proyht) {
        E_PRODTP prodtp = null;
        if(CommUtil.compare(proyht, E_PROYHT.CTDEPOSIT)==0){//活期存款
            prodtp = E_PRODTP.CTDEPOSIT;
        }else if(CommUtil.compare(proyht, E_PROYHT.FIXDEPOSIT)==0){//定期存款
            prodtp = E_PRODTP.FIXDEPOSIT;
        }else {
            throw CaError.Eacct.E0001("暂不支持该产品类型的转换");
        }
        return prodtp;
    }
	/**
	 * 
	 * <p>Title:convertToDepttm </p>
	 * <p>Description:将银户通要求的存期枚举类型转换成系统内的存期枚举类型	</p>
	 * @author Administrator
	 * @date   2018年12月5日 
	 * @param zdj
	 * @return
	 */
	/*private E_TERMCD convertToDepttm(String depyht) {
	    E_TERMCD depttm = null;
	    if(CommUtil.compare(depyht, "00")==0){//活期
	        depttm = E_TERMCD.T000;
        }else if(CommUtil.compare(depyht, "1D")==0){//一天
            depttm = E_TERMCD.T101;
        }else if(CommUtil.compare(depyht, "7D")==0){//七天
            depttm = E_TERMCD.T107;
        }else if(CommUtil.compare(depyht, "1M")==0){//一个月
            depttm = E_TERMCD.T201;
        }else if(CommUtil.compare(depyht, "3M")==0){//三个月
            depttm = E_TERMCD.T203;
        }else if(CommUtil.compare(depyht, "6M")==0){//六个月
            depttm = E_TERMCD.T206;
        }else if(CommUtil.compare(depyht, "1Y")==0){//一年
            depttm = E_TERMCD.T301;
        }else if(CommUtil.compare(depyht, "2Y")==0){//两年
            depttm = E_TERMCD.T302;
        }else if(CommUtil.compare(depyht, "3Y")==0){//三年
            depttm = E_TERMCD.T303;
        }else if(CommUtil.compare(depyht, "5Y")==0){//五年
            depttm = E_TERMCD.T305;
        }else if(CommUtil.compare(depyht, "6Y")==0){//六年
            depttm = E_TERMCD.T306;
        }
        return depttm;
    }*/
	/**
	 * 
	 * <p>Title:convertToDepttm </p>
	 * <p>Description:将系统内的存期枚举类型转换成银户通要求的存期枚举类型	</p>
	 * @author zdj
	 * @date   2018年12月4日 
	 * @param depttm
	 * @return
	 */
	private E_DEPYHT convertToDepyht(String depttm){
	    E_DEPYHT depyht = null;
	    if(CommUtil.compare(depttm, "000")==0){//活期
	        depyht = E_DEPYHT.HQ;
	    }else if(CommUtil.compare(depttm, "101")==0){//一天
	        depyht = E_DEPYHT.YT;
	    }else if(CommUtil.compare(depttm, "107")==0){//七天
	        depyht = E_DEPYHT.QT;
        }else if(CommUtil.compare(depttm, "201")==0){//一个月
            depyht = E_DEPYHT.YY;
        }else if(CommUtil.compare(depttm, "203")==0){//三个月
            depyht = E_DEPYHT.SY;
        }else if(CommUtil.compare(depttm, "206")==0){//六个月
            depyht = E_DEPYHT.LY;
        }else if(CommUtil.compare(depttm, "301")==0){//一年
            depyht = E_DEPYHT.YN;
        }else if(CommUtil.compare(depttm, "302")==0){//两年
            depyht = E_DEPYHT.TN;
        }else if(CommUtil.compare(depttm, "303")==0){//三年
            depyht = E_DEPYHT.SN;
        }else if(CommUtil.compare(depttm, "305")==0){//五年
            depyht = E_DEPYHT.WN;
        }else if(CommUtil.compare(depttm, "306")==0){//六年
            depyht = E_DEPYHT.LN;
        }
	    return depyht;
	}
	
	private BigDecimal prcAcctIntrvlDetl(IoDpKnbAcin cplKnbAcin, IoCaDpacctInfo cplDpAcctInfo) {

	    DpInterestInfo interestInfo = SysUtil.getInstance(DpInterestInfo.class);//利率基础信息复合类型
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//当前日期
		//由于行内利率会出现变化，固活期需要分段计息
		
		BigDecimal cutmam = cplKnbAcin.getCutmam(); //本期积数
		BigDecimal avgtranam = BigDecimal.ZERO; //平均余额
		
		//活期总积数
		BigDecimal totalAcmltn = BigDecimal.ZERO;
		//执行利率
		BigDecimal intrvl = BigDecimal.ZERO;
		//实际积数
		BigDecimal realCutmam = CaPublic.calRealTotalAmt(cutmam, cplDpAcctInfo.getOnlnbl(), trandt, cplKnbAcin.getLaamdt());
		
		totalAcmltn = totalAcmltn.add(realCutmam); //加实际积数			
		
		IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
		
		//获取账户利率信息
		IoDpKubInrtInfo cplKubInrt = EacctMainDao.selKubInrtInfoByAcctno(cplDpAcctInfo.getAcctno(), true);

		
		if (E_IRCDTP.LAYER == cplKnbAcin.getIncdtp()) {
			
			int days=1;	//计提天数					
			
			if(CommUtil.compare(cplKnbAcin.getInammd(), E_IBAMMD.ACCT)==0){ //账户余额				
				
				throw DpModuleError.DpstComm.BNAS1635();
				
			}else if(CommUtil.compare(cplKnbAcin.getInammd(), E_IBAMMD.AVG)==0){//平均余额
				
				days = calAvgDays(cplKnbAcin.getIrwptp(),cplKnbAcin.getBldyca(), cplKnbAcin.getTxbefr(), 
											cplKnbAcin.getLcindt(), cplKnbAcin.getNcindt(),trandt);
				
				if(CommUtil.equals(totalAcmltn, BigDecimal.ZERO)){
					avgtranam =cplDpAcctInfo.getOnlnbl();//平均余额
				}else{
					avgtranam = totalAcmltn.divide(BigDecimal.valueOf(days), 2,BigDecimal.ROUND_HALF_UP);//平均余额
				}
				
				IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
				intrEntity.setCorpno(cplKnbAcin.getCorpno());  //法人代码
				intrEntity.setBrchno(cplDpAcctInfo.getOpenbr());//机构号
				intrEntity.setTranam(avgtranam);//交易金额
				intrEntity.setTrandt(trandt);//交易日期
				intrEntity.setIntrcd(cplKnbAcin.getIntrcd());   //利率代码 
				intrEntity.setIncdtp(cplKnbAcin.getIncdtp());  //利率代码类型
				intrEntity.setCrcycd(cplDpAcctInfo.getCrcycd());//币种
				intrEntity.setInbebs(cplKnbAcin.getTxbebs());   //计息基础
				intrEntity.setIntrwy(cplKnbAcin.getIntrwy());  //靠档方式
				intrEntity.setCainpf(E_CAINPF.T1);              //计息规则
				intrEntity.setBgindt(cplKnbAcin.getBgindt());  //起息日期
				//到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
//				if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
//					String termcd = acinfo.getDepttm().getValue();
//					if(CommUtil.equals(termcd.substring(0, 1),"9")){
//						intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
//					}else{
//						intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
//					}
//				}
				
				if(CommUtil.isNotNull(cplDpAcctInfo)){
					if(CommUtil.isNotNull(cplDpAcctInfo.getMatudt())){
						intrEntity.setEdindt(cplDpAcctInfo.getMatudt());//止息日
					}else{
						intrEntity.setEdindt(trandt); //止息日
					}
				}
				
				if(CommUtil.isNull(intrEntity.getEdindt())){
					intrEntity.setEdindt(trandt); //止息日
				}
//				intrEntity.setEdindt(cplDpInstPrcIn.getTrandt());  //止息日
				
				intrEntity.setLevety(cplKnbAcin.getLevety());
				if(cplKnbAcin.getIntrdt() == E_INTRDT.OPEN){
					intrEntity.setTrandt(cplKnbAcin.getOpendt());
					intrEntity.setTrantm("999999");
				}
				pbpub.countInteresRate(intrEntity);
				
				BigDecimal cuusin = intrEntity.getIntrvl();//获取利率
				//利率可取最大值
				BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
				//利率可取最小值
				BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

				// 利率优惠后执行利率
				cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(cplKubInrt.getFavort(),BigDecimal.ZERO).
								divide(BigDecimal.valueOf(100))));
				//若优惠后的利率小于最大可取利率则赋值为最大可取利率
				if(CommUtil.compare(cuusin, maxval)>0){
					cuusin = maxval;
				}
				//若优惠后的利率小于最小可取利率则赋值为最小可取利率
				if(CommUtil.compare(cuusin, minval)<0){
					cuusin = minval;
				}
				intrvl = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);
				//add by zdj 20181026
				interestInfo.setIntrex(intrvl);//利率
				interestInfo.setIntrfl(minval);//利率下限
				interestInfo.setIntrup(maxval);//利率上限
				//add end
			}else if(CommUtil.compare(cplKnbAcin.getInammd(), E_IBAMMD.SUM)==0){//积数
                /** add by huangwh 20181121 start   积数靠档 */
			    
			    IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                intrEntity.setCorpno(cplKnbAcin.getCorpno());  //法人代码
                intrEntity.setBrchno(cplDpAcctInfo.getOpenbr());//机构号
                intrEntity.setTranam(totalAcmltn);/** 交易金额   = 活期总积数 */
                intrEntity.setTrandt(trandt);//交易日期
                intrEntity.setIntrcd(cplKnbAcin.getIntrcd());   //利率代码 
                intrEntity.setIncdtp(cplKnbAcin.getIncdtp());  //利率代码类型
                intrEntity.setCrcycd(cplDpAcctInfo.getCrcycd());//币种
                intrEntity.setInbebs(cplKnbAcin.getTxbebs());   //计息基础
                intrEntity.setIntrwy(cplKnbAcin.getIntrwy());  //靠档方式
                intrEntity.setCainpf(E_CAINPF.T1);              //计息规则
                intrEntity.setBgindt(cplKnbAcin.getBgindt());  //起息日期
                //到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
//              if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
//                  String termcd = acinfo.getDepttm().getValue();
//                  if(CommUtil.equals(termcd.substring(0, 1),"9")){
//                      intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
//                  }else{
//                      intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));           
//                  }
//              }
                
                if(CommUtil.isNotNull(cplDpAcctInfo)){
                    if(CommUtil.isNotNull(cplDpAcctInfo.getMatudt())){
                        intrEntity.setEdindt(cplDpAcctInfo.getMatudt());//止息日
                    }else{
                        intrEntity.setEdindt(trandt); //止息日
                    }
                }
                
                if(CommUtil.isNull(intrEntity.getEdindt())){
                    intrEntity.setEdindt(trandt); //止息日
                }
//              intrEntity.setEdindt(cplDpInstPrcIn.getTrandt());  //止息日
                
                intrEntity.setLevety(cplKnbAcin.getLevety());
                if(cplKnbAcin.getIntrdt() == E_INTRDT.OPEN){
                    intrEntity.setTrandt(cplKnbAcin.getOpendt());
                    intrEntity.setTrantm("999999");
                }
                pbpub.countInteresRate(intrEntity);
                
                BigDecimal cuusin = intrEntity.getIntrvl();//获取利率
                //利率可取最大值
                BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                //利率可取最小值
                BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

                // 利率优惠后执行利率
                cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(cplKubInrt.getFavort(),BigDecimal.ZERO).
                                divide(BigDecimal.valueOf(100))));
                //若优惠后的利率小于最大可取利率则赋值为最大可取利率
                if(CommUtil.compare(cuusin, maxval)>0){
                    cuusin = maxval;
                }
                //若优惠后的利率小于最小可取利率则赋值为最小可取利率
                if(CommUtil.compare(cuusin, minval)<0){
                    cuusin = minval;
                }
                intrvl = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);
                //add by zdj 20181026
                interestInfo.setIntrex(intrvl);//利率
                interestInfo.setIntrfl(minval);//利率下限
                interestInfo.setIntrup(maxval);//利率上限
			    
                /** add by huangwh 20181121 end */
			}else{
				throw DpModuleError.DpstComm.BNAS1636();
			}											
			
		} else if (E_IRCDTP.Reference == cplKnbAcin.getIncdtp()||E_IRCDTP.BASE == cplKnbAcin.getIncdtp()) { //参考利率
			
			// modify 20161221 liaojc 当利率确定日期为支取日，重新获取当前执行利率
			BigDecimal cuusin = cplKubInrt.getCuusin(); // 账户利率表执行利率
			
			if (cplKnbAcin.getIntrdt() == E_INTRDT.DRAW) {
				
				IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
				intrEntity.setCrcycd(cplDpAcctInfo.getCrcycd()); //币种
				intrEntity.setIntrcd(cplKnbAcin.getIntrcd()); //利率代码
				
				//如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
				intrEntity.setIncdtp(cplKnbAcin.getIncdtp()); //利率代码类型
				intrEntity.setIntrwy(cplKnbAcin.getIntrwy()); //靠档方式
				intrEntity.setTrandt(trandt);
				intrEntity.setDepttm(E_TERMCD.T000);// 存期
				intrEntity.setBgindt(cplKnbAcin.getBgindt()); //起始日期
				//到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
//				if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
//					String termcd = acinfo.getDepttm().getValue();
//					if(CommUtil.equals(termcd.substring(0, 1),"9")){
//						intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
//					}else{
//						intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
//					}
//				}
				
				
				if(CommUtil.isNotNull(cplDpAcctInfo.getMatudt())){
					intrEntity.setEdindt(cplDpAcctInfo.getMatudt());//止息日
				}else{
					intrEntity.setEdindt(trandt); //止息日
				}
				
				if(CommUtil.isNull(intrEntity.getEdindt())){
					intrEntity.setEdindt(trandt); //止息日
				}
				intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
				intrEntity.setInbebs(cplKnbAcin.getTxbebs()); //计息基础
				intrEntity.setCorpno(cplKnbAcin.getCorpno());//法人代码
				intrEntity.setBrchno(cplDpAcctInfo.getOpenbr());//机构
				
				intrEntity.setLevety(cplKnbAcin.getLevety());
				pbpub.countInteresRate(intrEntity);
				
				cuusin = intrEntity.getIntrvl(); //当前执行利率
				
				//利率可取最大值
				BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
				//利率可取最小值
				BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
				// 利率优惠后执行利率
				cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(cplKubInrt.getFavort(),BigDecimal.ZERO).
								divide(BigDecimal.valueOf(100))));
				
				//若优惠后的利率大于最大可取利率则赋值为最大可取利率
				if(CommUtil.compare(cuusin, maxval)>0){
					cuusin = maxval;
				}
				//若优惠后的利率小于最小可取利率则赋值为最小可取利率
				if(CommUtil.compare(cuusin, minval)<0){
					cuusin = minval;
				}
				cuusin = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);
			}
		
			intrvl = cuusin;
		} else { 
			throw DpModuleError.DpstComm.BNAS1081();
		}
		return intrvl;
	}

	private int calAvgDays(E_CYCLTP irwptp, E_AVBLDT bldyca, String txbefr,String lcindt, String ncindt, String trandt) {

	    int days;
	    
	    if(CommUtil.isNull(ncindt)){
	    	throw DpModuleError.DpstComm.BNAS1594();
	    }
	    if(CommUtil.isNull(lcindt)){
	    	if(CommUtil.isNull(txbefr)){
	    		throw DpModuleError.DpstComm.BNAS1595();
	    	}
//	    	lcindt = DateTools2.calDateByFreq(ncindt, txbefr, "", "", 3, 2);
	    	lcindt = DateTools2.calDateByFreq(ncindt, txbefr, null, 2);
	    }
	    
	    if(CommUtil.compare(bldyca, E_AVBLDT.T1)==0){//实际天数
			days = DateTools2.calDays(lcindt, ncindt, 0, 0); // 实际天数
		}else{
			days = DateTools2.calDays(lcindt, ncindt, 1, 0); // 储蓄天数
		}		
		
		return days;
	}
	
	private BigDecimal prcFxacIntrvlDetl(IoDpKnbAcin cplKnbAcin, IoCaDpacctInfo cplDpFxacInfo) {
		
		BizLog log = BizLogUtil.getBizLog(IoSrvPbInterestRate.class);
		DpInterestInfo interestInfo = SysUtil.getInstance(DpInterestInfo.class);//利率基础信息复合类型
		//定期账户
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//当前日期
		
		//执行利率
		BigDecimal intrvl = BigDecimal.ZERO;
		
		IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
		//传统定期
		if(CommUtil.equals(E_YES___.NO.getValue(), cplKnbAcin.getDetlfg().getValue())){
			
			// 获取定期负债账户信息
//			kna_fxac tblKnaFxac = Kna_fxacDao.selectOne_odb1(cplDpFxacInfo.getAcctno(), true);
			
			//计算计提利息
				
			if (E_IRCDTP.Reference == cplKnbAcin.getIncdtp() || E_IRCDTP.BASE == cplKnbAcin.getIncdtp()) { //参考利率
				//获取账户利率信息
				IoDpKubInrtInfo cplKubInrt = EacctMainDao.selKubInrtInfoByAcctno(cplDpFxacInfo.getAcctno(), true);
				 
				// modify 20161221 liaojc 当利率确定日期为支取日，重新获取当前执行利率
				BigDecimal cuusin = cplKubInrt.getCuusin(); // 账户利率表执行利率
				
				if (cplKnbAcin.getIntrdt() == E_INTRDT.DRAW) {
					
					IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
					intrEntity.setCrcycd(cplDpFxacInfo.getCrcycd()); //币种
					intrEntity.setIntrcd(cplKnbAcin.getIntrcd()); //利率代码
					
					//如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
					intrEntity.setIncdtp(cplKnbAcin.getIncdtp()); //利率代码类型
					intrEntity.setIntrwy(cplKnbAcin.getIntrwy()); //靠档方式
					intrEntity.setTrandt(trandt);
					intrEntity.setDepttm(cplDpFxacInfo.getDepttm());// 存期
					intrEntity.setBgindt(cplKnbAcin.getBgindt()); //起始日期
					//到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
//					if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
//						String termcd = acinfo.getDepttm().getValue();
//						if(CommUtil.equals(termcd.substring(0, 1),"9")){
//							intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
//						}else{
//							intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
//						}
//					}
					//获取负债账号签约明细信息
					IoCaKnaSignDetl cplKnaSignDetl = EacctMainDao.selKnaSignDetlInfoByAcctno(cplDpFxacInfo.getAcctno(), false);
					
						if(CommUtil.isNotNull(cplDpFxacInfo.getMatudt())){
							intrEntity.setEdindt(cplDpFxacInfo.getMatudt());//止息日
						}else if(CommUtil.isNotNull(cplKnaSignDetl)){
							if(CommUtil.isNotNull(cplKnaSignDetl.getEffedt())){
								intrEntity.setEdindt(cplKnaSignDetl.getEffedt()); //止息日
							}
						}else{
							intrEntity.setEdindt("20991231");
						}
					
					if(CommUtil.isNull(intrEntity.getEdindt())){
						intrEntity.setEdindt("20991231"); //止息日
					}
//					intrEntity.setEdindt(trandt); //结束日期
					intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
					intrEntity.setInbebs(cplKnbAcin.getTxbebs()); //计息基础
					intrEntity.setCorpno(cplKnbAcin.getCorpno());//法人代码
					intrEntity.setBrchno(cplDpFxacInfo.getOpenbr());//机构
					
					intrEntity.setLevety(cplKnbAcin.getLevety());
					pbpub.countInteresRate(intrEntity);
					
					cuusin = intrEntity.getIntrvl(); //当前执行利率
					
					log.debug("执行利率-------------cuusin："+ cuusin);
					
					//利率可取最大值
					BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
					//利率可取最小值
					BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
					// 利率优惠后执行利率
					cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(cplKubInrt.getFavort(),BigDecimal.ZERO).
									divide(BigDecimal.valueOf(100))));
					
					//若优惠后的利率小于最大可取利率则赋值为最大可取利率
					if(CommUtil.compare(cuusin, maxval)>0){
						cuusin = maxval;
					}
					//若优惠后的利率小于最小可取利率则赋值为最小可取利率
					if(CommUtil.compare(cuusin, minval)<0){
						cuusin = minval;
					}
					cuusin = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);
					
					log.debug("优惠比较后的执行利率-------------cuusin："+ cuusin);
				}
				
					intrvl = cuusin;
					log.debug("执行利率-------------："+ intrvl);
			}else if (E_IRCDTP.LAYER == cplKnbAcin.getIncdtp()) {
					
					IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity. class);
					intrEntity.setCorpno(cplKnbAcin.getCorpno());  //法人代码
					intrEntity.setBrchno(cplDpFxacInfo.getOpenbr());//机构号
					intrEntity.setTranam(cplDpFxacInfo.getOnlnbl());//交易金额
					intrEntity.setTrandt(trandt);//交易日期
					intrEntity.setIntrcd(cplKnbAcin.getIntrcd());   //利率代码 
					intrEntity.setIncdtp(cplKnbAcin.getIncdtp());  //利率代码类型
					intrEntity.setCrcycd(cplDpFxacInfo.getCrcycd());//币种
					intrEntity.setInbebs(cplKnbAcin.getTxbebs());   //计息基础
					intrEntity.setIntrwy(cplKnbAcin.getIntrwy());  //靠档方式
					intrEntity.setCainpf(E_CAINPF.T1);              //计息规则
					intrEntity.setBgindt(cplKnbAcin.getBgindt());  //起息日期

					//到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
//					if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
//						String termcd = acinfo.getDepttm().getValue();
//						if(CommUtil.equals(termcd.substring(0, 1),"9")){
//							intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
//						}else{
//							intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
//						}
//					}
					
					//获取负债账号签约明细信息
					IoCaKnaSignDetl cplKnaSignDetl = EacctMainDao.selKnaSignDetlInfoByAcctno(cplDpFxacInfo.getAcctno(), false);
					
						if(CommUtil.isNotNull(cplDpFxacInfo.getMatudt())){
							intrEntity.setEdindt(cplDpFxacInfo.getMatudt());//止息日
						}else if(CommUtil.isNotNull(cplKnaSignDetl)){
							if(CommUtil.isNotNull(cplKnaSignDetl.getEffedt())){
								intrEntity.setEdindt(cplKnaSignDetl.getEffedt()); //止息日
							}
						}else{
							intrEntity.setEdindt("20991231");
						}
					
					if(CommUtil.isNull(intrEntity.getEdindt())){
						intrEntity.setEdindt("20991231"); //止息日
					}
					
					intrEntity.setLevety(cplKnbAcin.getLevety());
					if(cplKnbAcin.getIntrdt() == E_INTRDT.OPEN){
						intrEntity.setTrandt(cplKnbAcin.getOpendt());
						intrEntity.setTrantm("999999");
					}
					pbpub.countInteresRate(intrEntity);
					
					//获取账户利率信息
					IoDpKubInrtInfo cplKubInrt = EacctMainDao.selKubInrtInfoByAcctno(cplDpFxacInfo.getAcctno(), true);
					
					//利率可取最大值
					BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
					//利率可取最小值
					BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

					// 利率优惠后执行利率
					BigDecimal cuusin = intrEntity.getIntrvl();
					
					cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(cplKubInrt.getFavort(),BigDecimal.ZERO).
									divide(BigDecimal.valueOf(100))));
					
					//若优惠后的利率小于最大可取利率则赋值为最大可取利率
					if(CommUtil.compare(cuusin, maxval)>0){
						cuusin = maxval;
					}
					//若优惠后的利率小于最小可取利率则赋值为最小可取利率
					if(CommUtil.compare(cuusin, minval)<0){
						cuusin = minval;
					}

					intrvl = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);
					
					log.debug("优惠比较后的执行利率-------------intrvl："+ intrvl);
					
				} else { // 分层利率在利率优惠代码块中进行实现
					throw DpModuleError.DpstComm.BNAS1081();
				}
				
		}else{//智能储蓄
			
			// 获取定期负债账户信息
//			kna_fxac tblKnaFxac = Kna_fxacDao.selectOne_odb1(cplDpFxacInfo.getAcctno(), true);
			
			if (E_IRCDTP.Reference == cplKnbAcin.getIncdtp() || E_IRCDTP.BASE == cplKnbAcin.getIncdtp()) { //参考利率
				//获取账户利率信息
				IoDpKubInrtInfo cplKubInrt = EacctMainDao.selKubInrtInfoByAcctno(cplDpFxacInfo.getAcctno(), true);
				
				// modify 20161221 liaojc 当利率确定日期为支取日，重新获取当前执行利率
				BigDecimal cuusin = cplKubInrt.getCuusin(); // 账户利率表执行利率
				
				if (cplKnbAcin.getIntrdt() == E_INTRDT.DRAW) {
					
					IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
					intrEntity.setCrcycd(cplDpFxacInfo.getCrcycd()); //币种
					intrEntity.setIntrcd(cplKnbAcin.getIntrcd()); //利率代码
					
					//如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
					intrEntity.setIncdtp(cplKnbAcin.getIncdtp()); //利率代码类型
					intrEntity.setIntrwy(cplKnbAcin.getIntrwy()); //靠档方式
					intrEntity.setTrandt(trandt);
					intrEntity.setDepttm(cplDpFxacInfo.getDepttm());// 存期
					intrEntity.setBgindt(cplKnbAcin.getBgindt()); //起始日期
					//到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
//					if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
//						String termcd = acinfo.getDepttm().getValue();
//						if(CommUtil.equals(termcd.substring(0, 1),"9")){
//							intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
//						}else{
//							intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
//						}
//					}
					//获取负债账号签约明细信息
					IoCaKnaSignDetl cplKnaSignDetl = EacctMainDao.selKnaSignDetlInfoByAcctno(cplDpFxacInfo.getAcctno(), false);
					
						if(CommUtil.isNotNull(cplDpFxacInfo.getMatudt())){
							intrEntity.setEdindt(cplDpFxacInfo.getMatudt());//止息日
						}else if(CommUtil.isNotNull(cplKnaSignDetl)){
							if(CommUtil.isNotNull(cplKnaSignDetl.getEffedt())){
								intrEntity.setEdindt(cplKnaSignDetl.getEffedt()); //止息日
							}
						}else{
							intrEntity.setEdindt("20991231");
						}
					
					if(CommUtil.isNull(intrEntity.getEdindt())){
						intrEntity.setEdindt("20991231"); //止息日
					}
					intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
					intrEntity.setInbebs(cplKnbAcin.getTxbebs()); //计息基础
					intrEntity.setCorpno(cplKnbAcin.getCorpno());//法人代码
					intrEntity.setBrchno(cplDpFxacInfo.getOpenbr());//机构
					
					intrEntity.setLevety(cplKnbAcin.getLevety());
					pbpub.countInteresRate(intrEntity);
					
					cuusin = intrEntity.getIntrvl(); //当前执行利率
					
					//利率可取最大值
					BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
					//利率可取最小值
					BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
					// 利率优惠后执行利率
					cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(cplKubInrt.getFavort(),BigDecimal.ZERO).
									divide(BigDecimal.valueOf(100))));
					//若优惠后的利率大于最大可取利率则赋值为最大可取利率
					if(CommUtil.compare(cuusin, maxval)>0){
						cuusin = maxval;
					}
					//若优惠后的利率小于最小可取利率则赋值为最小可取利率
					if(CommUtil.compare(cuusin, minval)<0){
						cuusin = minval;
					}
					cuusin = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);
					
					log.debug("利率优惠比较后的执行利率-------------cuusin："+cuusin);
				}
				
					intrvl = cuusin;
					
					log.debug("执行利率-------------intrvl："+ intrvl);
				
			}else if (E_IRCDTP.LAYER == cplKnbAcin.getIncdtp()) {
					
					IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
					intrEntity.setCorpno(cplKnbAcin.getCorpno());  //法人代码
					intrEntity.setBrchno(cplDpFxacInfo.getOpenbr());//机构号
					
					IoDpKnaFxacDetlInfo cplKnaFxacDetl = EacctMainDao.selKnaFxacDetlInfoByAcctno(cplDpFxacInfo.getAcctno(), false);
					//查询定期明细表的记录
					if(CommUtil.isNotNull(cplKnaFxacDetl)){
						intrEntity.setTranam(cplKnaFxacDetl.getOnlnbl());//交易金额
						intrEntity.setBgindt(cplKnaFxacDetl.getBgindt());  //起息日期
					}else{
						intrEntity.setTranam(cplDpFxacInfo.getOnlnbl());//交易金额
						intrEntity.setBgindt(cplDpFxacInfo.getBgindt());  //起息日期
					}
					
					intrEntity.setTrandt(trandt);//交易日期
					intrEntity.setIntrcd(cplKnbAcin.getIntrcd());   //利率代码 
					intrEntity.setIncdtp(cplKnbAcin.getIncdtp());  //利率代码类型
					intrEntity.setCrcycd(cplDpFxacInfo.getCrcycd());//币种
					intrEntity.setInbebs(cplKnbAcin.getTxbebs());   //计息基础
					intrEntity.setIntrwy(cplKnbAcin.getIntrwy());  //靠档方式
					intrEntity.setCainpf(E_CAINPF.T1);              //计息规则

					//到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
//					if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
//						String termcd = acinfo.getDepttm().getValue();
//						if(CommUtil.equals(termcd.substring(0, 1),"9")){
//							intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
//						}else{
//							intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
//						}
//					}
//					if(CommUtil.isNull(intrEntity.getEdindt())){
//						intrEntity.setEdindt(trandt); //止息日
//					}
					
					//获取负债账号签约明细信息
					IoCaKnaSignDetl cplKnaSignDetl = EacctMainDao.selKnaSignDetlInfoByAcctno(cplDpFxacInfo.getAcctno(), false);
					
						if(CommUtil.isNotNull(cplDpFxacInfo.getMatudt())){
							intrEntity.setEdindt(cplDpFxacInfo.getMatudt());//止息日
						}else if(CommUtil.isNotNull(cplKnaSignDetl)){
							if(CommUtil.isNotNull(cplKnaSignDetl.getEffedt())){
								intrEntity.setEdindt(cplKnaSignDetl.getEffedt()); //止息日
							}
						}else{
							intrEntity.setEdindt("20991231");
						}
					
					if(CommUtil.isNull(intrEntity.getEdindt())){
						intrEntity.setEdindt("20991231"); //止息日
					}
					
					intrEntity.setLevety(cplKnbAcin.getLevety());
					if(cplKnbAcin.getIntrdt() == E_INTRDT.OPEN){
						intrEntity.setTrandt(cplKnbAcin.getOpendt());
						intrEntity.setTrantm("999999");
					}
					pbpub.countInteresRate(intrEntity);
					
					//获取账户利率信息
					IoDpKubInrtInfo cplKubInrt = EacctMainDao.selKubInrtInfoByAcctno(cplDpFxacInfo.getAcctno(), true);
					//利率可取最大值
					BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
					//利率可取最小值
					BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

					// 利率优惠后执行利率
					BigDecimal cuusin = intrEntity.getIntrvl();
					cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(cplKubInrt.getFavort(),BigDecimal.ZERO).
									divide(BigDecimal.valueOf(100))));
					
					//若优惠后的利率小于最大可取利率则赋值为最大可取利率
					if(CommUtil.compare(cuusin, maxval)>0){
						cuusin = maxval;
					}
					//若优惠后的利率小于最小可取利率则赋值为最小可取利率
					if(CommUtil.compare(cuusin, minval)<0){
						cuusin = minval;
					}

					intrvl = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);
					
					log.debug("利率优惠比较后的执行利率-------------intrvl："+intrvl);
					
			} else { // 分层利率在利率优惠代码块中进行实现
					throw DpModuleError.DpstComm.BNAS1081();
			}
		}
		return intrvl;
	}

	/**
	 * 查询电子账户下存款类子账户信息(定期和活期无限制)
	 * 
	 */
	@Override
	public Options<IoCaDpAcctfos> queryDpAcctInfo(String custac, String begndt,
			String endate, Long pageno, Long pgsize) {
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();

		if (CommUtil.isNotNull(begndt)) {
			if (CommUtil.compare(begndt, trandt) > 0) {
				throw DpModuleError.DpstComm.BNAS1637();
			}
		}
		if (CommUtil.isNotNull(endate)) {
			if (CommUtil.compare(begndt, endate) > 0) {
				throw DpModuleError.DpstComm.BNAS1637();
			}
		}
		if (CommUtil.isNull(custac)) {
			throw DpModuleError.DpstComm.BNAS0901();
		}
		IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(
				IoCaSevQryTableInfo.class)
				.getKnaAcdcOdb2(custac, true);

		/*if (E_DPACST.NORMAL != tblKnaAcdc.getStatus()) {
			throw PbError.PbComm.E2015("该账户为非正常状态！");
		}*/

		Options<IoCaDpAcctfos> infos = new DefaultOptions<IoCaDpAcctfos>();

		long start = (pageno - 1) * pgsize; // 起始数
		int count = 0;// 总笔数
		// 根据电子账户和开户日期分页查询存款类子账户信息
		Page<IoCaDpAcctfos> lstDpacctInfo = EacctMainDao.selDpAcctInfoByOpdt(
				tblKnaAcdc.getCustac(), begndt, endate, start, pgsize, count,
				false);

		if (CommUtil.isNotNull(lstDpacctInfo)) {
			infos.addAll(lstDpacctInfo.getRecords());
			CommTools.getBaseRunEnvs().setTotal_count(lstDpacctInfo.getRecordCount());
		}

		return infos;
	}

	/**
	 * 机构币种下有效结算子户数查询
	 */
	@Override
	public Long selEffectAcctnoByCrcycd(String brchno, String crcycd) {

		long count = EacctMainDao
				.selEffectAcctnoByCrcycd(crcycd, brchno, false);

		return count;
	}

}