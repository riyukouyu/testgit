package cn.sunline.ltts.busi.catran.trans;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.type.CaCustInfo.accoutinfos;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAcctInfoList;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryLedgerInfosOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpQryFrozTableInfo;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_QRACWY;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_QRYCTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PRODTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

/**
 * 
 * @ClassName: qraccu 
 * @Description: (柜员分户账查询) 
 * @author xiongzhao
 * @date 2016年12月19日 下午7:08:09 
 *
 */
public class qraccu {

	public static final BigDecimal ZERO = new BigDecimal("0.00");
	
	public static void sevLedgerQuery(
			final cn.sunline.ltts.busi.catran.trans.intf.Qraccu.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Qraccu.Output output) {
		String custac = null;// 电子账号
		String cardno = input.getCardno();// 卡号
		String idtfno = input.getIdtfno();// 证件号码
		E_IDTFTP idtftp = input.getIdtftp();// 证件类型
		E_PRODTP prodtp = input.getProdtp();// 产品类型
		E_CUACST cuacst = input.getAcctst();// 账户状态
		String crcycd = input.getCrcycd();// 币种
		E_QRACWY qracwy = input.getQracwy();// 查询方式
		E_QRYCTP qractp = input.getQractp();// 查询类型
		BigDecimal acctbl = ZERO;// 可支取余额
		BigDecimal frozam = ZERO;// 冻结金额
		BigDecimal stopam = ZERO;// 止付金额
		BigDecimal hdbkam = ZERO;// 保留金额
		BigDecimal prauam = ZERO;// 预授权金额
		BigDecimal acctam = ZERO;// 总资产
		BigDecimal debtam = ZERO;// 总负债
		BigDecimal dpacbl = ZERO;// 存款余额
		String sCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();// 法人代码
		
		// 输入字段为空校验
		// 卡号和证件信息不能同时为空
		if (CommUtil.isNull(cardno)) {
			if (CommUtil.isNull(idtfno) && CommUtil.isNull(idtftp)) {
//				throw CaError.Eacct.BNAS0921();
				throw DpModuleError.DpstComm.E9999("电子账号不能为空");
			}
		}
		// 产品类型
		if (CommUtil.isNull(prodtp)) {
			throw CaError.Eacct.BNAS1051();
		}
		// 币种
		if (CommUtil.isNull(crcycd)) {
			throw DpModuleError.DpstAcct.BNAS0634();
		}
		// 查询方式
		if (CommUtil.isNull(qracwy)) {
			throw CaError.Eacct.BNAS1072();
		}
		// 查询类型
		if (CommUtil.isNull(qractp)) {
			throw CaError.Eacct.BNAS1272();
		}

		// 证件类型证件号码必须都不为空，或都为空
		if (CommUtil.isNull(idtfno)) {
			if (CommUtil.isNotNull(idtftp)) {
				throw CaError.Eacct.BNAS0152();
			}
		} else {
			if (CommUtil.isNull(idtftp)) {
				throw CaError.Eacct.BNAS0156();
			}
		}

		// 若为客户查询，法人代码传入null，查询所有法人下电子账户
		if (qracwy == E_QRACWY.CUSQRY) {
			sCorpno = null;
		}
		
		//输出接口定义
		Options<IoCaQryLedgerInfosOut> LedgerInfos = new DefaultOptions<IoCaQryLedgerInfosOut>();
		
		if (CommUtil.equals(CommTools.getBaseRunEnvs().getBusi_org_id(), "999")) {
			sCorpno = null;
		} 
		
		// 查询电子账户基本信息
		List<accoutinfos> accoutinfos = EacctMainDao.selLedgerQuery(
				cardno, idtfno, idtftp, cuacst, sCorpno, false);
		
		if (qracwy == E_QRACWY.CUSQRY) {
			if (CommUtil.isNull(accoutinfos) || accoutinfos.size() <= 0) {
				throw CaError.Eacct.BNAS0902();
			}
		} else {
			if (CommUtil.isNotNull(cardno)) {
				IoCaKnaAcdc cplKnaAcdc = CaDao.selKnaAcdcByCard(cardno, false);
				if (CommUtil.isNull(cplKnaAcdc)) {
					throw CaError.Eacct.BNAS0902();
				} else {
					if (!CommUtil.equals(CommTools.getBaseRunEnvs().getBusi_org_id(),
							"999")) {
						if (!CommUtil.equals(
								CommTools.getBaseRunEnvs().getBusi_org_id(),
								cplKnaAcdc.getCorpno())) {
							throw CaError.Eacct.BNAS1089();
						}
					}
				}
			}
			if (CommUtil.isNull(accoutinfos) || accoutinfos.size() <= 0) {
				throw CaError.Eacct.BNAS1195();
			}
		}
		
		for (accoutinfos infos : accoutinfos) {
			
			IoCaQryLedgerInfosOut LedgerInfo1 = SysUtil.getInstance(IoCaQryLedgerInfosOut.class);
			
			if (infos.getAcctst() == E_CUACST.PREOPEN ) {
				
				LedgerInfo1.setCustnm(idtftp + idtfno);// 统一后管客户号
				
				//若账户为结清状态则销户日期返回空
				if(infos.getActtst() == E_ACCTST.SETTLE){
					infos.setClosdt(null);
				}
				CommUtil.copyProperties(LedgerInfo1, infos);// 将单条记录放入输出中
				LedgerInfos.add(LedgerInfo1);	//输出返回		
			}
			else if (infos.getAcctst() == E_CUACST.DELETE) {
				// 作废数据直接跳过
				continue;
			}
			else {
				
				//若账户为结清状态则销户日期返回空
				if(infos.getActtst() == E_ACCTST.SETTLE){
					infos.setClosdt(null);
				}
				
				// 将查询出的电子账户法人放入环境变脸中
//				CommTools.getBaseRunEnvs().setBusi_org_id(infos.getCorpno());					
		
				custac = infos.getCustac();// 电子账号
				
				CommUtil.copyProperties(LedgerInfo1, infos);// 将单条记录放入输出中
				
				// 查询电子账户账户分类
				E_ACCATP eAccatp = SysUtil.getInstance(
						IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac);
				
				// 电子账户状态字查询
				IoDpAcStatusWord cplGetAcStWord = SysUtil.getInstance(
						IoDpFrozSvcType.class).getAcStatusWord(custac);
				
				KnaAcal tblKnaAcal = SysUtil.getInstance(KnaAcal.class);
				// 查询绑定手机号
				if (infos.getAcctst() == E_CUACST.CLOSED) {
					tblKnaAcal = CaDao.selKnaAcalByCustac(custac, E_ACALTP.CELLPHONE, E_ACALST.INVALID, false);
				}else {
					tblKnaAcal = CaDao.selKnaAcalByCustac(custac, E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);
				}
				
				// 查询当前余额
				IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(IoDpKnaAcct.class);
				if (eAccatp == E_ACCATP.GLOBAL || eAccatp == E_ACCATP.FINANCE) {
					cplKnaAcct = EacctMainDao.selKnaAcctByacsetpAndcrcycd(custac,E_ACSETP.SA,crcycd,false);
				} else if (eAccatp == E_ACCATP.WALLET) {
					cplKnaAcct = EacctMainDao.selKnaAcctByacsetpAndcrcycd(custac,E_ACSETP.MA,crcycd,false);
				}
				// 当前账户余额
				BigDecimal onlnbl = BigDecimal.ZERO;	
				// 查询昨日余额
				BigDecimal lastbl = BigDecimal.ZERO;
				
				if(CommUtil.isNotNull(cplKnaAcct)){
					onlnbl = cplKnaAcct.getOnlnbl();
				if (CommUtil.compare(CommTools.getBaseRunEnvs().getTrxn_date(),
						cplKnaAcct.getUpbldt()) == 0) {
					lastbl = cplKnaAcct.getLastbl();
				}
				if (CommUtil.compare(CommTools.getBaseRunEnvs().getTrxn_date(),
						cplKnaAcct.getUpbldt()) < 0) {
					lastbl = cplKnaAcct.getOnlnbl();
				}
				}
/*				// 获取转存签约明细信息
				IoCaKnaSignDetl cplkna_sign_detl = SysUtil.getInstance(
						IoCaSevQryTableInfo.class).kna_sign_detl_selectFirst_odb2(
						cplKnaAcct.getAcctno(), E_SIGNST.QY, false);
	
				// 存在转存签约明细信息则取资金池可用余额
				if (CommUtil.isNotNull(cplkna_sign_detl)) {
					acctbl = SysUtil.getInstance(DpAcctSvcType.class)
							.getProductBal(custac, crcycd, false);
				} else {
					// 其他取账户余额,正常的支取交易排除冻结金额
					acctbl = SysUtil.getInstance(DpAcctSvcType.class)
							.getOnlnblForFrozbl(cplKnaAcct.getAcctno(), false);
				}*/
				
				// 可支取余额
				acctbl = SysUtil.getInstance(DpAcctSvcType.class)
						.getDrawnBalance(custac, crcycd, E_YES___.NO);

				// 查询冻结登记簿
				Options<IoDpKnbFroz> lstKnbFroz = SysUtil.getInstance(
						IoDpFrozSvcType.class).qryKnbFroz(custac, E_FROZST.VALID);
	
				for (IoDpKnbFroz knbfroz : lstKnbFroz) {
/*					if (knbfroz.getFroztp() == E_FROZTP.ADD
							|| knbfroz.getFroztp() == E_FROZTP.JUDICIAL) {
						if (knbfroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
							frozam = frozam.add(knbfroz.getFrozam());// 冻结余额
						}
					}*/
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
				
				// 冻结金额需要根据日期来计算，排除掉未生效的续冻
				List<IoDpQryFrozTableInfo> lstFroz = EacctMainDao.selFrozInfosByCustac(custac, false);
				for (IoDpQryFrozTableInfo cplFroz : lstFroz) {
					frozam = frozam.add(cplFroz.getFrozbl());// 冻结余额
				}
				// 查询预授权金额（一期暂无） TODO
	
				// 计算负债活期账户总余额
				BigDecimal bal = EacctMainDao.selSumEacctBlH(custac, crcycd, true);
	
				// 计算负债定期账户总余额
				BigDecimal bal1 = EacctMainDao.selSumEacctBlFx(custac, crcycd,
						true);
	
				// 计算存款余额
				dpacbl = bal.add(bal1);
	
				// 计算理财账户总余额
				BigDecimal bal2 = EacctMainDao.selSumFnacctBill(custac, crcycd,
						true);
	
				// 计算总资产
				acctam = bal.add(bal1).add(bal2);
	
				IoCaAcctInfoList cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);
	
				// 活期存款
				if (prodtp == E_PRODTP.CTDEPOSIT) {
					cplAcctInfo.setMxpdtp(prodtp);
					cplAcctInfo.setProdam(bal);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
				}
				// 定期存款
				else if (prodtp == E_PRODTP.FIXDEPOSIT) {
					cplAcctInfo.setMxpdtp(prodtp);
					cplAcctInfo.setProdam(bal1);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
				}
				// 理财
				else if (prodtp == E_PRODTP.MANAGE) {
					cplAcctInfo.setMxpdtp(prodtp);
					cplAcctInfo.setProdam(bal2);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
				}
				// 基金
				else if (prodtp == E_PRODTP.FUND) {
					cplAcctInfo.setMxpdtp(prodtp);
					cplAcctInfo.setProdam(BigDecimal.ZERO);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
				}
				// 贵金属
				else if (prodtp == E_PRODTP.PRECIOUS) {
					cplAcctInfo.setMxpdtp(prodtp);
					cplAcctInfo.setProdam(BigDecimal.ZERO);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
				}
				// 贷款
				else if (prodtp == E_PRODTP.LOAN) {
					cplAcctInfo.setMxpdtp(prodtp);
					cplAcctInfo.setProdam(BigDecimal.ZERO);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
				}
				// 信用卡
				else if (prodtp == E_PRODTP.BLUECARD) {
					cplAcctInfo.setMxpdtp(prodtp);
					cplAcctInfo.setProdam(BigDecimal.ZERO);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
				} else if (prodtp == E_PRODTP.LKBLCARD) {
					cplAcctInfo.setMxpdtp(prodtp);
					cplAcctInfo.setProdam(BigDecimal.ZERO);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
				}
				// 全部
				else if (prodtp == E_PRODTP.ALL) {
					cplAcctInfo.setMxpdtp(E_PRODTP.CTDEPOSIT);
					cplAcctInfo.setProdam(bal);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
					cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);
					cplAcctInfo.setMxpdtp(E_PRODTP.FIXDEPOSIT);
					cplAcctInfo.setProdam(bal1);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
					cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);
					cplAcctInfo.setMxpdtp(E_PRODTP.MANAGE);
					cplAcctInfo.setProdam(bal2);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
					cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);
					cplAcctInfo.setMxpdtp(E_PRODTP.FUND);
					cplAcctInfo.setProdam(BigDecimal.ZERO);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
					cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);
					cplAcctInfo.setMxpdtp(E_PRODTP.PRECIOUS);
					cplAcctInfo.setProdam(BigDecimal.ZERO);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
					cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);
					cplAcctInfo.setMxpdtp(E_PRODTP.LOAN);
					cplAcctInfo.setProdam(BigDecimal.ZERO);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
					cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);
					cplAcctInfo.setMxpdtp(E_PRODTP.BLUECARD);
					cplAcctInfo.setProdam(BigDecimal.ZERO);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
					cplAcctInfo = SysUtil.getInstance(IoCaAcctInfoList.class);
					cplAcctInfo.setMxpdtp(E_PRODTP.LKBLCARD);
					cplAcctInfo.setProdam(BigDecimal.ZERO);
					LedgerInfo1.getAcctInfoList().add(cplAcctInfo);
				} else {
					throw CaError.Eacct.BNAS1286();
				}
				
				// 面签标识查询
				E_YES___ facesg = EacctMainDao.selFacesgByCustac(custac, true);
	
				// 赋值给输出
				if (CommUtil.isNotNull(tblKnaAcal)) {
					LedgerInfo1.setTeleno(tblKnaAcal.getTlphno());
				}
				LedgerInfo1.setAccttp(eAccatp);// 账户分类
				LedgerInfo1.setAcstsz(cplGetAcStWord.getAcstsz());// 账户状态字3
				LedgerInfo1.setAcctam(acctam);// 总资产
				LedgerInfo1.setDebtam(debtam);// 总负债
				LedgerInfo1.setAcctbl(acctbl);// 可支取余额
				LedgerInfo1.setDpacbl(dpacbl);// 存款余额
				LedgerInfo1.setHdbkam(hdbkam);// 保留金额
				LedgerInfo1.setLastbl(lastbl);// 昨日余额
				LedgerInfo1.setOnlnbl(onlnbl);// 当前余额
				LedgerInfo1.setPrauam(prauam);// 预授权金额
				LedgerInfo1.setStopam(stopam);// 止付金额
				LedgerInfo1.setFrozam(frozam);// 冻结金额
				LedgerInfo1.setCustnm(idtftp + idtfno);// 统一后管客户号
				LedgerInfo1.setFacesg(facesg);// 面签标识
				LedgerInfos.add(LedgerInfo1);
			}
		}
		output.getLedgerInfoOut().addAll(LedgerInfos);
	}
}
