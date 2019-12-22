package cn.sunline.ltts.busi.catran.trans;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpCaError;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcalDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCuad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCuadDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.type.CaCustInfo;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;

public class qryact {
	/**
	 * 
	 * @author xiongzhao
	 *         <p>
	 *         <li>2016年6月25日-下午2:50:34</li>
	 *         <li>功能描述：电子账户基本信息查询</li>
	 *         </p>
	 * 
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void prcQryAcInfos(
			final cn.sunline.ltts.busi.catran.trans.intf.Qryact.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Qryact.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Qryact.Output output) {

		// 检查输入项
		String sCustac = "";// 电子账号ID1
		String sCustac1 = "";// 电子账号ID1
		String sCustac2 = "";// 电子账号ID2
		String sCustac3 = "";// 电子账号ID3
		String sCustac4 = "";// 电子账号ID4
		String sCardno = input.getCardno();// 电子账号
		String sAcalno = input.getAcalno();// 绑定手机号
		String sCustid = input.getCustcd();// 用户ID
		E_IDTFTP eIdtftp = input.getIdtftp();// 证件类型
		String sIdtfno = input.getIdtfno();// 证件号码
		BigDecimal acctbl = BigDecimal.ZERO;// 可支取余额
		BigDecimal onlnbl = BigDecimal.ZERO; // 当前账户余额
		String crcycd = null;// 币种
		E_CSEXTG csextg = null;// 钞汇标识
		
		// 电子账号、证件号码、绑定手机号、用户ID不能同时为空
		if (CommUtil.isNull(sIdtfno) && CommUtil.isNull(sAcalno)
				&& CommUtil.isNull(sCustid) && CommUtil.isNull(sCardno)) {
			throw DpCaError.DpEacct.BNAS0950();
		}

		// 如果电子账号不为空，根据电子账号查询出电子账号ID
		if (CommUtil.isNotNull(sCardno)) {
			IoCaKnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCard(sCardno, false);
			if (CommUtil.isNull(tblKnaAcdc)) {
				throw DpCaError.DpEacct.ET033002();
			}
			CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaAcdc.getCorpno());
			sCustac1 = tblKnaAcdc.getCustac();
			sCustac = sCustac1;
			
			// 如果用户ID不为空，根据用户ID查询出电子账号	
		} else if (CommUtil.isNotNull(sCustid)) {
			KnaCust tblKnaCust = CaDao.selCustInfoByCustid(sCustid, false);
			if (CommUtil.isNull(tblKnaCust)) {
				throw DpCaError.DpEacct.E0021();
			}
			CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaCust.getCorpno());
			sCustac2 = tblKnaCust.getCustac();
			sCustac = sCustac2;
			

			// 如果手机号不为空，根据绑定手机号查询出电子账号ID
		} else if (CommUtil.isNotNull(sAcalno)) {
			
			//检查手机号长度和是否为全为数字
			if (sAcalno.length() != 11) {
				throw DpCaError.DpEacct.BNAS0397();
			}
			
			if (!BusiTools.isNum(sAcalno)) {
				throw DpCaError.DpEacct.BNAS0319();
			}
			
			KnaAcal tblKnaAcal = CaDao.selKnaAcalByAcalnoAndAcalst(sAcalno, E_ACALTP.CELLPHONE, false);
			if (CommUtil.isNull(tblKnaAcal)) {
				throw DpCaError.DpEacct.E0021();
			}
			CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaAcal.getCorpno());
			sCustac3 = tblKnaAcal.getCustac();
			sCustac = sCustac3;

			// 如果证件类型证件号码不为空，查询出电子账号ID
		} else if (CommUtil.isNotNull(sIdtfno) && CommUtil.isNotNull(eIdtftp)) {
			
			//校验证件类型、证件号码
	        BusiTools.chkCertnoInfo(eIdtftp, sIdtfno);
	        
			IoCaKnaCust cplKnaCust = EacctMainDao.selCustInfoByIdtfno(eIdtftp, sIdtfno, false);
			if (CommUtil.isNull(cplKnaCust)) {
				throw DpCaError.DpEacct.E0021();
			}
			CommTools.getBaseRunEnvs().setBusi_org_id(cplKnaCust.getCorpno());
			sCustac4 = cplKnaCust.getCustac();
			sCustac = sCustac4;
			
		} else if (!(CommUtil.isNull(sIdtfno) && CommUtil.isNull(eIdtftp))) {
			
			throw DpCaError.DpEacct.BNAS0148();
		}
		
		//查询电子账户状态
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(sCustac);
		
		if (cuacst == E_CUACST.DELETE) {
			throw DpCaError.DpEacct.BNAS1284();
		}
		
		// 查询电子账户基本信息
		CaCustInfo.accoutinfos accoutinfos = SysUtil.getInstance(CaCustInfo.accoutinfos.class);
		
		if(CommUtil.isNull(sCardno)){
			 accoutinfos = EacctMainDao.selCustInfobyCustac(sCustac,
					E_ACALST.NORMAL, E_ACALTP.CELLPHONE, false);
		}else{
			//accoutinfos = EacctMainDao.selCustInfobyCardno(sCustac, E_ACALTP.CELLPHONE, false);
			
			KnaCust knaCustDO = KnaCustDao.selectOne_odb1(sCustac, false);
			accoutinfos.setCustac(sCustac);
			accoutinfos.setActtst(knaCustDO.getAcctst());
			accoutinfos.setCuacst(knaCustDO.getCuacst());
			accoutinfos.setBrchno(knaCustDO.getBrchno());
			accoutinfos.setOpendt(knaCustDO.getOpendt());
			accoutinfos.setOpenbr(knaCustDO.getBrchno());
			accoutinfos.setClosdt(knaCustDO.getClosdt());
			accoutinfos.setOpensv((knaCustDO.getUschnl().toString()));
			accoutinfos.setClossv(knaCustDO.getClossv());
			//取消掉客户信息相关内容，模块拆分
//			CifCust cifCustDO = CifCustDao.selectOne_odb1(knaCustDO.getCustno(), false);
//			if(CommUtil.isNotNull(cifCustDO)){
//			    accoutinfos.setIdtftp(cifCustDO.getIdtftp());
//	            accoutinfos.setIdtfno(cifCustDO.getIdtfno());
//	            accoutinfos.setCustna(cifCustDO.getCustna());
//			}
			
			KnaCuad knaCuad = KnaCuadDao.selectOne_knaCuadOdx1(sCustac, false);
			if(CommUtil.isNotNull(knaCuad)){
//			    cif_cust_accs cifCustAccsDO = Cif_cust_accsDao.selectOne_odb1(knaCustDO.getCustno(), knaCuad.getCustid(), false);
				//mod by xj 20180613
//				cif_cust_accs cifCustAccsDO = Cif_cust_accsDao.selectOne_odb1(knaCustDO.getCustno(),BaseEnumType.E_STATUS.NORMAL, false);
//			    if(CommUtil.isNotNull(cifCustAccsDO)){
//			        accoutinfos.setCustid(cifCustAccsDO.getCustid());
//		            accoutinfos.setCustcd(cifCustAccsDO.getCustcd());
//			    }
			}
			
			KnaAcdc knaAcdcDO = KnaAcdcDao.selectFirst_odb3(sCustac, false);
			if(CommUtil.isNotNull(knaAcdcDO)){
			    accoutinfos.setCardno(knaAcdcDO.getCardno());
			}
			
			KnaAcal knaAcalDO = KnaAcalDao.selectFirst_odb6(sCustac, E_ACALTP.CELLPHONE, false);
			if(CommUtil.isNotNull(knaAcalDO)){
			    accoutinfos.setTeleno(knaAcalDO.getTlphno());
            }
		}
		
		if (CommUtil.isNull(accoutinfos)) {
			throw DpCaError.DpEacct.BNAS1284();
		}
		

		//查询电子账户分类
		E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
				.qryAccatpByCustac(sCustac);
		
		//判断状态字
		if(cuacst == E_CUACST.PREOPEN || cuacst == E_CUACST.CLOSED || cuacst == E_CUACST.PRECLOS){
			if(cuacst == E_CUACST.CLOSED || cuacst == E_CUACST.PRECLOS){
				// 查询销户信息
				IoDpKnaAcct tblKnaAcct = SysUtil.getInstance(IoDpKnaAcct.class);
				if (eAccatp == E_ACCATP.GLOBAL || eAccatp == E_ACCATP.FINANCE) {
					tblKnaAcct = EacctMainDao.selKnaAcctByacsetp(sCustac, E_ACSETP.SA, true);
				} else if (eAccatp == E_ACCATP.WALLET) {
					tblKnaAcct = EacctMainDao.selKnaAcctByacsetp(sCustac, E_ACSETP.MA, true);
				}
				onlnbl = tblKnaAcct.getOnlnbl(); // 当前账户余额
				crcycd = tblKnaAcct.getCrcycd();// 币种
				csextg = tblKnaAcct.getCsextg();// 钞汇标识
			}
		}else{
			// 查询当前余额
			IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(sCustac);
			// 检查查询结果是否为空
			if (CommUtil.isNotNull(cplKnaAcct)) {
				
			    onlnbl = cplKnaAcct.getOnlnbl(); // 当前账户余额
				crcycd = cplKnaAcct.getCrcycd();// 币种
				csextg = cplKnaAcct.getCsextg();// 钞汇标识
				
				// 可用余额
				acctbl = SysUtil.getInstance(DpAcctSvcType.class)
						.getAcctaAvaBal(sCustac, cplKnaAcct.getAcctno(),
								crcycd, E_YES___.YES, E_YES___.NO);

			}
		}
		
		// 电子账户状态字查询
		IoDpAcStatusWord cplGetAcStWord =  SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(sCustac);
		
		// 面签标识查询
		E_YES___ facesg = EacctMainDao.selFacesgByCustac(sCustac, true);
		
		output.setOnlnbl(onlnbl);// 当前账户余额
		output.setAcalno(accoutinfos.getTeleno());// 绑定手机号码
		output.setAcctbl(acctbl);// 账户余额
		output.setAcctst(cuacst);// 账户状态
		output.setAccttp(eAccatp);// 电子账户分类
		output.setAcstsz(cplGetAcStWord.getAcstsz());// 电子账户状态字
		output.setBrchno(accoutinfos.getBrchno());// 归属机构
		output.setCardno(accoutinfos.getCardno());// 虚拟交易卡号
		output.setClosdt(accoutinfos.getClosdt());// 销户日期
		output.setCrcycd(crcycd);// 币种
		output.setCsextg(csextg);// 钞汇标识
		output.setCustac(sCustac);// 电子账号	
		output.setCustcd(accoutinfos.getCustcd());// 客户内码
		output.setCustid(accoutinfos.getCustid());// 用户ID
		output.setCustna(accoutinfos.getCustna());// 客户名称
		output.setIdtfno(accoutinfos.getIdtfno());// 证件号码
		output.setIdtftp(accoutinfos.getIdtftp());// 证件类型
		output.setOpenbr(accoutinfos.getOpenbr());// 开户结构
		output.setOpendt(accoutinfos.getOpendt());// 开户日期
		output.setFacesg(facesg);// 面签标识
		

		//poc增加审计日志
		ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
		apAudit.regLogOnInsertBusiPoc(accoutinfos.getCardno());
	}
	
}
