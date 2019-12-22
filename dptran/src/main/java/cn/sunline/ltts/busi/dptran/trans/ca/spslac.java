package cn.sunline.ltts.busi.dptran.trans.ca;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.ca.CaPublic;
import cn.sunline.ltts.busi.ca.namedsql.AccountLimitDao;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.serviceimpl.IoCaSevAccountLimtPublic;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KubTsscAcct;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupAcrtBrch;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtLimt;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtSbac;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtServ;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtServDao;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupPrcsLimt;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupPrcsLimtDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.type.CaCustInfo;
import cn.sunline.ltts.busi.ca.type.CaCustInfo.acctAllInfos;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.type.DpAcctType.ChkQtIN;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkOT;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_ACLMFG;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTKD;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_PYTLTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RECPAY;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RISKLV;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.QtError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CHNLID;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BACATP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class spslac {

	private static final BizLog log = BizLogUtil
			.getBizLog(spslac.class);

	public static void SelAcctAllAndBindInfos( final cn.sunline.ltts.busi.dptran.trans.ca.intf.Spslac.Input input,  final cn.sunline.ltts.busi.dptran.trans.ca.intf.Spslac.Property property,  final cn.sunline.ltts.busi.dptran.trans.ca.intf.Spslac.Output output){
		// 检查输入项
		String custac = "";// 电子账号ID
		E_BACATP sCardtp = input.getCardtp();//绑定账户类型
		String sCdopac = input.getCdopac();//绑定账户
		String sCardno = input.getCardno();// 电子账号
		//		String sAcalno = input.getAcalno();// 绑定手机号
		//		String sCustid = input.getCustid();// 用户ID
		//		E_IDTFTP eIdtftp = input.getIdtftp();// 证件类型
		//		String sIdtfno = input.getIdtfno();// 证件号码
		E_CHNLID servtp = input.getServtp2();//渠道类型
		BigDecimal acctbl = BigDecimal.ZERO;// 可支取余额
		BigDecimal onlnbl = BigDecimal.ZERO; // 当前账户余额
		String crcycd = null;// 币种
		E_CSEXTG csextg = null;// 钞汇标识	
		E_SBACTP sbactp = null;//子账户类型
		ChkQtIN chkqtn = input.getChkqtn();//额度中心参数  

		if (CommUtil.isNull(sCardno)) {
			throw DpModuleError.DpstComm.BNAS0572();		
		}

		E_ACCTROUTTYPE routeType1 = ApAcctRoutTools.getRouteType(sCardno);
		E_YES___ isflag=E_YES___.NO;
        if(routeType1 == E_ACCTROUTTYPE.INSIDE){
           isflag = E_YES___.YES;//转入方为内部户
        }else{
           isflag = E_YES___.NO;//转入方为电子账户
        }

		if (isflag==E_YES___.YES){
			output.setIsswin(E_YES___.YES); //转入许可
			output.setAcctInfosList(null);
		}else{

			if (CommUtil.isNull(sCardtp)) {
				throw DpModuleError.DpstComm.E9999("绑定账户类型不能為空");		
			}
			// ，根据电子账号查询出电子账号ID
			IoCaKnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCard(sCardno, false);
			if (CommUtil.isNull(tblKnaAcdc)) {
				throw CaError.Eacct.BNAS1279();
			}
			//			CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaAcdc.getCorpno());
			custac = tblKnaAcdc.getCustac();
			//		List<CaCustInfo.acctAllInfos> result = new ArrayList<CaCustInfo.acctAllInfos>();
			Options<CaCustInfo.acctAllInfos> results = new DefaultOptions<CaCustInfo.acctAllInfos>();
			//循环获取的每个电子账户

			//查询电子账户状态
			E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);	
			if (cuacst == E_CUACST.CLOSED || cuacst == E_CUACST.DELETE || cuacst == E_CUACST.INACTIVE) {
				throw DpModuleError.DpstComm.BNAS0441();
			}

			//将法人set到公共报文
			//			KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(custac, false);
			//			KnaCust tblKnaCust = CaDao.selKnaCustByCustac(custac, false);
			//			CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaCust.getCorpno());

			// 查询电子账户基本信息
			List<acctAllInfos> acctInfos = null;
			if(CommUtil.isNull(servtp)){
				acctInfos=EacctMainDao.selCustInfosbyCustac(custac, E_ACALTP.CELLPHONE, false);
			}else{
				acctInfos=EacctMainDao.selCustInfosByCustacAndServtp(custac, E_ACALTP.CELLPHONE, servtp, false);
			}

			//查询电子账户分类
			E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac);


			IoDpKnaAcct tblKnaAcct = SysUtil.getInstance(IoDpKnaAcct.class);
			// 查询销户信息
			if (eAccatp == E_ACCATP.GLOBAL || eAccatp == E_ACCATP.FINANCE) {
				tblKnaAcct = EacctMainDao.selKnaAcctByacsetp(custac, E_ACSETP.SA, true);
				sbactp = E_SBACTP._11;
			} else if (eAccatp == E_ACCATP.WALLET) {
				tblKnaAcct = EacctMainDao.selKnaAcctByacsetp(custac, E_ACSETP.MA, true);
				sbactp = E_SBACTP._12;
			}
			//判断状态字
			if(cuacst == E_CUACST.PREOPEN || cuacst == E_CUACST.CLOSED){
				if(cuacst == E_CUACST.CLOSED){
					onlnbl = tblKnaAcct.getOnlnbl(); // 当前账户余额
					crcycd = tblKnaAcct.getCrcycd();// 币种
					csextg = tblKnaAcct.getCsextg();// 钞汇标识
				}
			}else{
				// 查询当前余额
				tblKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);
				// 检查查询结果是否为空
				if (CommUtil.isNotNull(tblKnaAcct)) {

					onlnbl = tblKnaAcct.getOnlnbl(); // 当前账户余额
					crcycd = tblKnaAcct.getCrcycd();// 币种
					csextg = tblKnaAcct.getCsextg();// 钞汇标识

					// 可用余额
					acctbl = SysUtil.getInstance(DpAcctSvcType.class)
							.getAcctaAvaBal(custac, tblKnaAcct.getAcctno(),
									crcycd, E_YES___.YES, E_YES___.NO);

					/*// 查询可支取余额
						// 获取转存签约明细信息
						IoCaKnaSignDetl cplkna_sign_detl = SysUtil.getInstance(
								IoCaSevQryTableInfo.class).kna_sign_detl_selectFirst_odb2(
								cplKnaAcct.getAcctno(), E_SIGNST.QY, false);

						// 存在转存签约明细信息则取资金池可用余额
						if (CommUtil.isNotNull(cplkna_sign_detl)) {
							acctbl = SysUtil.getInstance(DpAcctSvcType.class)
									.getProductBal(custac, cplKnaAcct.getCrcycd(), false);
						} else {
							// 其他取账户余额,正常的支取交易排除冻结金额
							acctbl = SysUtil.getInstance(DpAcctSvcType.class)
									.getOnlnblForFrozbl(cplKnaAcct.getAcctno(), false);
						}
					 */
				}
			}

			// 面签标识查询
			E_YES___ facesg = EacctMainDao.selFacesgByCustac(custac, true);


			AcTranfeChkOT chkOT = SysUtil.getInstance(AcTranfeChkOT.class);

			chkOT.setFacesg(facesg);
			//判断绑定关系			
			KnaCacd tblKnaCacd = KnaCacdDao.selectOne_odb2(custac,sCdopac,E_DPACST.NORMAL, false);
			if (CommUtil.isNotNull(tblKnaCacd)){
				if (tblKnaCacd.getCardtp().compareTo(sCardtp) != 0){
					throw DpModuleError.DpstComm.E9999("绑定账户类型不一致");
				}
				chkOT.setIsbind(E_YES___.YES);
			}else{
				chkOT.setIsbind(E_YES___.NO);
			}


			//给每个电子账户输出的字段赋值
			for(acctAllInfos acctInfo:acctInfos){

				//若电子账户状态为结清则销户日期返回空
				if(acctInfo.getActtst() == E_ACCTST.SETTLE){
					acctInfo.setClosdt(null);
				}

				// 电子账户状态字查询
				IoDpAcStatusWord cplGetAcStWord =  SysUtil.getInstance(IoDpFrozSvcType.class)
						.getAcStatusWord(custac);

				if (chkOT.getIsbind() == E_YES___.YES){

					//初始化电子账户全部信息输出信息
					CaCustInfo.acctAllInfos acctAllInfos = SysUtil.getInstance(CaCustInfo.acctAllInfos.class);

					acctAllInfos.setOnlnbl(onlnbl);// 当前账户余额
					acctAllInfos.setAcalno(acctInfo.getAcalno());// 绑定手机号码
					acctAllInfos.setAcalst(acctInfo.getAcalst());//绑定手机状态
					acctAllInfos.setAcctbl(acctbl);// 账户余额
					acctAllInfos.setAcctst(cuacst);// 账户状态
					acctAllInfos.setAccttp(eAccatp);// 电子账户分类
					acctAllInfos.setAcstsz(cplGetAcStWord.getAcstsz());// 电子账户状态字
					acctAllInfos.setBrchno(acctInfo.getBrchno());// 归属机构
					acctAllInfos.setCardno(acctInfo.getCardno());// 虚拟交易卡号
					acctAllInfos.setClosdt(acctInfo.getClosdt());// 销户日期
					acctAllInfos.setCrcycd(crcycd);// 币种
					acctAllInfos.setCsextg(csextg);// 钞汇标识
					acctAllInfos.setCustac(custac);// 电子账号	
					acctAllInfos.setCustcd(acctInfo.getCustcd());// 客户内码
					acctAllInfos.setCustid(acctInfo.getCustid());// 用户ID
					acctAllInfos.setCustna(acctInfo.getCustna());// 客户名称
					acctAllInfos.setIdtfno(acctInfo.getIdtfno());// 证件号码
					acctAllInfos.setIdtftp(acctInfo.getIdtftp());// 证件类型
					acctAllInfos.setOpenbr(acctInfo.getOpenbr());// 开户结构
					acctAllInfos.setOpendt(acctInfo.getOpendt());// 开户日期
					acctAllInfos.setFacesg(facesg); //面签标识
					results.add(acctAllInfos);
					output.setIsswin(chkOT.getIsbind()); //转入许可
				}else{
					if (facesg == E_YES___.YES){

						//初始化电子账户全部信息输出信息
						CaCustInfo.acctAllInfos acctAllInfos = SysUtil.getInstance(CaCustInfo.acctAllInfos.class);

						acctAllInfos.setOnlnbl(onlnbl);// 当前账户余额
						acctAllInfos.setAcalno(acctInfo.getAcalno());// 绑定手机号码
						acctAllInfos.setAcalst(acctInfo.getAcalst());//绑定手机状态
						acctAllInfos.setAcctbl(acctbl);// 账户余额
						acctAllInfos.setAcctst(cuacst);// 账户状态
						acctAllInfos.setAccttp(eAccatp);// 电子账户分类
						acctAllInfos.setAcstsz(cplGetAcStWord.getAcstsz());// 电子账户状态字
						acctAllInfos.setBrchno(acctInfo.getBrchno());// 归属机构
						acctAllInfos.setCardno(acctInfo.getCardno());// 虚拟交易卡号
						acctAllInfos.setClosdt(acctInfo.getClosdt());// 销户日期
						acctAllInfos.setCrcycd(crcycd);// 币种
						acctAllInfos.setCsextg(csextg);// 钞汇标识
						acctAllInfos.setCustac(custac);// 电子账号	
						acctAllInfos.setCustcd(acctInfo.getCustcd());// 客户内码
						acctAllInfos.setCustid(acctInfo.getCustid());// 用户ID
						acctAllInfos.setCustna(acctInfo.getCustna());// 客户名称
						acctAllInfos.setIdtfno(acctInfo.getIdtfno());// 证件号码
						acctAllInfos.setIdtftp(acctInfo.getIdtftp());// 证件类型
						acctAllInfos.setOpenbr(acctInfo.getOpenbr());// 开户结构
						acctAllInfos.setOpendt(acctInfo.getOpendt());// 开户日期
						acctAllInfos.setFacesg(facesg); //面签标识
						results.add(acctAllInfos);

						//校验额度
						cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter qtIn = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter.class);
						cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output	qtOt = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output.class);		
						qtIn.setBrchno(tblKnaAcct.getBrchno());
						qtIn.setAclmfg(chkqtn.getAclmfg());
						qtIn.setAccttp(eAccatp);
						qtIn.setCustac(tblKnaAcdc.getCustac());
						qtIn.setCustid(chkqtn.getCustid());
						qtIn.setCustlv(chkqtn.getCustlv());
						qtIn.setAcctrt(chkqtn.getAcctrt());
						qtIn.setLimttp(chkqtn.getLimttp());
						qtIn.setPytltp(chkqtn.getPytltp());
						qtIn.setRebktp(chkqtn.getRebktp());
						qtIn.setRisklv(chkqtn.getRisklv());
						qtIn.setSbactp(sbactp);
						qtIn.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());
						qtIn.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
						qtIn.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
						qtIn.setTranam(input.getTranam());
						qtIn.setCustie(chkOT.getIsbind());//是否绑定卡标识
						qtIn.setFacesg(chkOT.getFacesg());//是否面签标识
						qtIn.setRecpay(null);//收付方标识，电子账户转电子账户需要输入
						selAcctQuota(qtIn, qtOt);//额度检查扣减
						output.setIsswin(E_YES___.YES); //转入许可
					}else{
						throw DpModuleError.DpstComm.BNAS0878();
					}
				}

			}	
			output.setAcctInfosList(results);
		}
		//判断绑定关系			
		//				KnaCacd tblKnaCacd = KnaCacdDao.selectOne_odb2(custac,sCdopac,E_DPACST.NORMAL, false);
		//				if (CommUtil.isNotNull(tblKnaCacd)){
		//					if (tblKnaCacd.getCardtp().compareTo(sCardtp) != 0){
		//						throw DpModuleError.DpstComm.E9999("绑定账户类型不一致");
		//					}
		//					output.setIsswin(E_YES___.YES);
		//				}else{
		//					
		//				}


	}			


	public static void selAcctQuota(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output output) {		

		log.debug("---------------------电子账户额度扣减------------------------");


		E_ACLMFG aclmfg;    //累计限额标识
		E_PYTLTP pytltp;    //支付工具
		E_RISKLV risklv;    //风险承受等级

		if(CommUtil.isNull(input.getAclmfg())){
			aclmfg = E_ACLMFG._2;      
		}else{
			aclmfg = input.getAclmfg();  
		}

		if(CommUtil.isNull(input.getPytltp())|| input.getPytltp() == E_PYTLTP.ALL){
			pytltp = E_PYTLTP._99; 
		}else{
			pytltp = input.getPytltp();  
		}

		if(CommUtil.isNull(input.getRisklv())){
			risklv = E_RISKLV._01; 
		}else{
			risklv = input.getRisklv();  
		} 

		String custid = input.getCustid(); //客户id
		E_LIMTTP limttp = input.getLimttp(); //额度类型
		String servtp = input.getServtp(); //渠道
		String servdt = input.getServdt(); //渠道交易日期
		String servsq = input.getServsq(); //渠道流水
		BigDecimal tranam = input.getTranam(); //交易金额
		//		E_ACCATP accttp = input.getAccttp(); //账户分类
		E_SBACTP sbactp = input.getSbactp(); //子账户类型
		String custac = input.getCustac(); //电子账号
		E_RECPAY dcflag = input.getDcflag();//收付标志

		//获取组合账户限额类型
		String prcscd = CommTools.getBaseRunEnvs().getTrxn_code();
		KupPrcsLimt tblPrcs = SysUtil.getInstance(KupPrcsLimt.class);
		if(CommUtil.isNull(dcflag)){ //
			tblPrcs = KupPrcsLimtDao.selectFirst_odb1(prcscd, false);
		}else{
			tblPrcs = AccountLimitDao.selPrcsLimtByRemak(prcscd, dcflag.getValue().toString(), false);
		}

		//		if (CommUtil.isNull(tblPrcs)) {
		//			BizLog log = BizLogUtil.getBizLog(IoCaSevAccountLimitImpl.class);
		//			log.debug("---------------------"+prcscd+"该交易未在额度类型表中配置------------------------");
		//			return;
		//		}

		if (CommUtil.isNull(tblPrcs)) {
			log.debug("---------------------"+prcscd+"该交易未在额度类型表中配置------------------------");
			return;
		}

		if(CommUtil.isNull(input.getLimttp())){
			limttp = CommUtil.toEnum(E_LIMTTP.class, tblPrcs.getLimttp()); 
		}

		if(input.getCustie()==E_YES___.YES){
			return;
		}

		/*	if (CommUtil.isNull(input.getAuthtp())) {
			throw QtError.Custa.BNASE046();
		}*/

		//		if (CommUtil.isNull(brchno)) {
		//			throw QtError.Custa.E0001("客户所属机构号不能为空");
		//		}
		//		if (CommUtil.isNull(custtp)) {
		//			throw QtError.Custa.E0001("客户类型不能为空");
		//		}
		//		if (CommUtil.isNull(limttp)) {
		//			throw QtError.Custa.BNASE117();
		//		}
		if (CommUtil.isNull(servtp)) {
			throw QtError.Custa.BNASE051();
		}
		if (CommUtil.isNull(servsq)) {
			throw QtError.Custa.BNASE048();
		}
		if (CommUtil.isNull(servdt)) {
			throw QtError.Custa.BNASE049();
		}
		//		if (CommUtil.isNull(accttp)) {
		//			throw QtError.Custa.E0001("账户分类不能为空");
		//		}
		if (CommUtil.isNull(sbactp)) {
			throw QtError.Custa.BNASE005();
		}

		if (CommUtil.isNull(input.getOldate()) && CommUtil.compare(input.getTranam(), BigDecimal.ZERO) < 0) {
			throw QtError.Custa.BNASE156();
		}
		if (CommUtil.isNull(custac)) {
			throw QtError.Custa.BNASE157();
		}
		//		if (CommUtil.isNull(risklv)) {
		//			throw QtError.Custa.BNASE104();
		//		}
		//		if (CommUtil.isNull(input.getAclmfg())) {
		//			throw QtError.Custa.BNASE028();
		//		}

		KnaCust knaCust = AccountLimitDao.selKnaCust(input.getCustac(), true);

		// 根据电子账号查询用户ID
		if(CommUtil.isNull(custid)){
			custid = CaDao.selCustidByCustac(custac, false);
			if (CommUtil.isNull(custid)) {
				throw QtError.Custa.BNASE158();
			}
		}

		String brchno = knaCust.getBrchno(); //客户所属机构号
		//		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();//法人代码
		//		String corpnotemp = knaCust.getBrchno().substring(0, 3);//账户所属法人
		//		CommTools.getBaseRunEnvs().setBusi_org_id(corpnotemp);//设置账户所属法人

		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		E_ACCATP accttp = cagen.qryAccatpByCustac(input.getCustac()); //转出方电子账户类型

		if(accttp == E_ACCATP.WALLET && CommUtil.isNull(sbactp)){			
			sbactp =E_SBACTP._12;
		}else if (accttp != E_ACCATP.WALLET && CommUtil.isNull(sbactp)){		
			sbactp =E_SBACTP._11;
		}

		String currdt = CommTools.getBaseRunEnvs().getTrxn_date();

		BigDecimal sglmam = BigDecimal.ZERO; //单笔交易限额
		BigDecimal dylmam = BigDecimal.ZERO; //日累计限额

		BigDecimal mtlmam = BigDecimal.ZERO; //月累计限额
		BigDecimal yrlmam = BigDecimal.ZERO; //年累计限额
		BigDecimal dylmtm = BigDecimal.ZERO; //日累计次数
		BigDecimal mtlmtm = BigDecimal.ZERO; //月累计次数
		BigDecimal yrlmtm = BigDecimal.ZERO; //年累计次数



		/*if (CommUtil.isNull(pytltp) || pytltp == E_PYTLTP.ALL) {
			pytltp = CommUtil.toEnum(E_PYTLTP.class, "99");
		}*/

		//获取组合渠道类型
		String serv = CaPublic.QryCmsvtpsAcc(servtp);
		if (CommUtil.isNull(serv)) {
			throw QtError.Custa.BNASE053();
		}

		//获取组合子账户类型
		String sbac = CaPublic.QrySbattps(sbactp.getValue());
		if (CommUtil.isNull(sbac)) {
			throw QtError.Custa.BNASE006();
		}

		String limtStr = "";
		if (CommUtil.isNotNull(tblPrcs) && CommUtil.isNotNull(tblPrcs.getLimttp())) {
			limtStr = tblPrcs.getLimttp();
		} else {
			throw QtError.Custa.BNASE159();
		}

		//取额度类型中文名
		String limttpName = CommUtil.isNotNull(tblPrcs.getRemak2()) ? tblPrcs.getRemak2() : "";

		//获取组合账户额度类型
		String limt = CaPublic.QryCmqttps(limtStr);
		//log.debug("-----认证方式限额有效记录数："+aurtList.size());

		//		E_ACLMFG aclmfg = E_ACLMFG._3;

		E_ACCTROUTTYPE accounttype = ApAcctRoutTools.getRouteType(knaCust.getCustno());

		/*
		if (CommUtil.isNull(input.getLimttp())){
		    throw DpModuleError.DpstAcct.E9999("额度类型输入不能为空!");
		}*/


		// 红包回退、隔日冲正 传入负值，不金额额度控制
		if(CommUtil.compare(tranam, BigDecimal.ZERO)>0){
			log.debug("-----额度控制正交易进入-------");
			IoBrchInfo supKubBrch =SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(brchno);
			// 查询所有配置信息
			List<KupAcrtBrch> acrtup  =AccountLimitDao.selKupAcrtBrchAll(supKubBrch.getBrchno(), accounttype, limttp.getValue(), null, null, accttp.getValue(), input.getSbactp().getValue(), pytltp, false);
			List<KupAcrtBrch> acrtcou  =AccountLimitDao.selKupAcrtBrchAll(brchno, accounttype, limttp.getValue(), null, null, accttp.getValue(),  input.getSbactp().getValue(), pytltp, false);
			// 取出有效账户配置信息
			List<KupAcrtBrch> acrtList = new ArrayList<>();
			for(KupAcrtBrch acrt_tmp:acrtup){
				if(limt.indexOf("'"+acrt_tmp.getLimttp()+"'")>=0 && serv.indexOf("'"+acrt_tmp.getServtp()+"'") >=0 && sbac.indexOf("'"+acrt_tmp.getSbactp()+"'") >=0){
					boolean flag = false;
					for(KupAcrtBrch acrtC:acrtcou){
						if(acrtC.getAcrtno().equals(acrt_tmp.getAcrtno())){
							acrtList.add(acrtC);
							flag = true;
						}
					}
					if(flag == false){
						acrtList.add(acrt_tmp);
					}
				}
			}
			acrtup = null;
			acrtcou=null;
			log.debug("-----额度限额有效信息记录数："+acrtList.size());
			//  查客户累计信息
			List<KubTsscAcct>  acctList = AccountLimitDao.selKubTsscAcct2(custac, accounttype, limttp.getValue(), null , input.getSbactp() , pytltp, false);
			log.debug("-----客户限额累计信息记录数："+acctList.size());

			// 查询额度类型、渠道、子账户类型明细信息
			// 查询额度明细类型 
			List<KupCurtLimt> listLim =  AccountLimitDao.selCurtLimt(null,null, false);

			//查询渠道明细类型
			//List<KupCurtServ> listSer = AccountLimitDao.selCurtServ(null, null, false);

			//查询子账户明细类型
			List<KupCurtSbac> listSba = AccountLimitDao.selCurtSmb(null, null,  false);

			// 根据查询到的有效配置信息控制限额
			for(KupAcrtBrch acrt_tmp:acrtList){
				// 单笔
				if(CommUtil.compare(acrt_tmp.getLimtkd(), E_LIMTKD._01) == 0){
					log.debug("单笔限额序号："+acrt_tmp.getAcrtno()+"，限额值："+acrt_tmp.getLmtval());
					if (CommUtil.compare(tranam, acrt_tmp.getLmtval()) > 0 && CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {
						throw QtError.Custa.BNASE160();
					}
				}
				// 日累计金额
				if(CommUtil.compare(acrt_tmp.getLimtkd(), E_LIMTKD._02) == 0){
					// String strServ = IoCaSevAccountLimtPublic.getLowerList(listSer, acrt_tmp.getServtp(), "0");
					String strServ;
					//　modify by zhuxw　20171024
					List<KupCurtServ> listSer1 = KupCurtServDao.selectAll_odb2(acrt_tmp.getServtp(), false);
					StringBuffer sbf = new StringBuffer();
					if(CommUtil.isNotNull(listSer1)){
						for(KupCurtServ KupCurtServ_tmp:listSer1){
							sbf.append("'").append(KupCurtServ_tmp.getServtp()).append("'");
						}
					}
					strServ = sbf.toString();
					//　modify by zhuxw　20171024
					String strLim =  IoCaSevAccountLimtPublic.getLowerList(listLim, acrt_tmp.getLimttp(), "1");
					String strSba =  IoCaSevAccountLimtPublic.getLowerList(listSba, acrt_tmp.getSbactp(), "2");
					BigDecimal talVal = IoCaSevAccountLimtPublic.getTsscAccVal(strLim, strServ, strSba, E_LIMTKD._02, currdt, acctList);
					log.debug("日累计金额限额序号："+acrt_tmp.getAcrtno()+"，限额值："+acrt_tmp.getLmtval()+"，客户累计额："+talVal);
					if(aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1  //日累计
							&& CommUtil.compare(tranam.add(talVal), acrt_tmp.getLmtval()) > 0 
							&& CommUtil.compare(tranam, BigDecimal.ZERO) > 0){
						throw QtError.Custa.BNASE161();
					}
				}
				//月累计金额
				if(CommUtil.compare(acrt_tmp.getLimtkd(), E_LIMTKD._03) == 0){
					// String strServ = IoCaSevAccountLimtPublic.getLowerList(listSer, acrt_tmp.getServtp(), "0");
					String strServ;
					//　modify by zhuxw　20171024
					List<KupCurtServ> listSer1 = KupCurtServDao.selectAll_odb2(acrt_tmp.getServtp(), false);
					StringBuffer sbf = new StringBuffer();
					if(CommUtil.isNotNull(listSer1)){
						for(KupCurtServ KupCurtServ_tmp:listSer1){
							sbf.append("'").append(KupCurtServ_tmp.getServtp()).append("'");
						}
					}
					strServ = sbf.toString();
					//　modify by zhuxw　20171024
					String strLim =  IoCaSevAccountLimtPublic.getLowerList(listLim, acrt_tmp.getLimttp(), "1");
					String strSba =  IoCaSevAccountLimtPublic.getLowerList(listSba, acrt_tmp.getSbactp(), "2");
					BigDecimal talVal = IoCaSevAccountLimtPublic.getTsscAccVal(strLim, strServ, strSba, E_LIMTKD._03, currdt, acctList);
					log.debug("月累计金额限额序号："+acrt_tmp.getAcrtno()+"，限额值："+acrt_tmp.getLmtval()+"，客户累计额："+talVal);
					if(aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1
							&& CommUtil.compare(tranam.add(talVal), acrt_tmp.getLmtval()) > 0
							&& CommUtil.compare(tranam, BigDecimal.ZERO) > 0){
						throw QtError.Custa.BNASE162();
					}
				}

				//年累计金额
				if(CommUtil.compare(acrt_tmp.getLimtkd(), E_LIMTKD._04) == 0){
					// String strServ = IoCaSevAccountLimtPublic.getLowerList(listSer, acrt_tmp.getServtp(), "0");
					String strServ;
					//　modify by zhuxw　20171024
					List<KupCurtServ> listSer1 = KupCurtServDao.selectAll_odb2(acrt_tmp.getServtp(), false);
					StringBuffer sbf = new StringBuffer();
					if(CommUtil.isNotNull(listSer1)){
						for(KupCurtServ KupCurtServ_tmp:listSer1){
							sbf.append("'").append(KupCurtServ_tmp.getServtp()).append("'");
						}
					}
					strServ = sbf.toString();
					//　modify by zhuxw　20171024
					String strLim =  IoCaSevAccountLimtPublic.getLowerList(listLim, acrt_tmp.getLimttp(), "1");
					String strSba =  IoCaSevAccountLimtPublic.getLowerList(listSba, acrt_tmp.getSbactp(), "2");
					BigDecimal talVal = IoCaSevAccountLimtPublic.getTsscAccVal(strLim, strServ, strSba, E_LIMTKD._04, currdt, acctList);
					log.debug("年累计金额限额序号："+acrt_tmp.getAcrtno()+"，限额值："+acrt_tmp.getLmtval()+"，客户累计额："+talVal);
					if(aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1
							&& CommUtil.compare(tranam.add(talVal), acrt_tmp.getLmtval()) > 0
							&& CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {
						throw QtError.Custa.BNASE163();
					}
				}

				// 日累计次数
				if(CommUtil.compare(acrt_tmp.getLimtkd(), E_LIMTKD._11) == 0){
					// String strServ = IoCaSevAccountLimtPublic.getLowerList(listSer, acrt_tmp.getServtp(), "0");
					String strServ;
					//　modify by zhuxw　20171024
					List<KupCurtServ> listSer1 = KupCurtServDao.selectAll_odb2(acrt_tmp.getServtp(), false);
					StringBuffer sbf = new StringBuffer();
					if(CommUtil.isNotNull(listSer1)){
						for(KupCurtServ KupCurtServ_tmp:listSer1){
							sbf.append("'").append(KupCurtServ_tmp.getServtp()).append("'");
						}
					}
					strServ = sbf.toString();
					//　modify by zhuxw　20171024
					String strLim =  IoCaSevAccountLimtPublic.getLowerList(listLim, acrt_tmp.getLimttp(), "1");
					String strSba =  IoCaSevAccountLimtPublic.getLowerList(listSba, acrt_tmp.getSbactp(), "2");
					BigDecimal talVal = IoCaSevAccountLimtPublic.getTsscAccVal(strLim, strServ, strSba, E_LIMTKD._11, currdt, acctList);
					log.debug("日累计次数限额序号："+acrt_tmp.getAcrtno()+"，限额值："+acrt_tmp.getLmtval()+"，客户累计额："+talVal);
					if(aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1
							&& CommUtil.compare(talVal.add(BigDecimal.ONE), acrt_tmp.getLmtval()) > 0
							&& CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {
						throw QtError.Custa.BNASE088();
					}
				}

				// 月累计次数
				if(CommUtil.compare(acrt_tmp.getLimtkd(), E_LIMTKD._12) == 0){
					// String strServ = IoCaSevAccountLimtPublic.getLowerList(listSer, acrt_tmp.getServtp(), "0");
					String strServ;
					//　modify by zhuxw　20171024
					List<KupCurtServ> listSer1 = KupCurtServDao.selectAll_odb2(acrt_tmp.getServtp(), false);
					StringBuffer sbf = new StringBuffer();
					if(CommUtil.isNotNull(listSer1)){
						for(KupCurtServ KupCurtServ_tmp:listSer1){
							sbf.append("'").append(KupCurtServ_tmp.getServtp()).append("'");
						}
					}
					strServ = sbf.toString();
					//　modify by zhuxw　20171024
					String strLim =  IoCaSevAccountLimtPublic.getLowerList(listLim, acrt_tmp.getLimttp(), "1");
					String strSba =  IoCaSevAccountLimtPublic.getLowerList(listSba, acrt_tmp.getSbactp(), "2");
					BigDecimal talVal = IoCaSevAccountLimtPublic.getTsscAccVal(strLim, strServ, strSba, E_LIMTKD._12, currdt, acctList);
					log.debug("月累计次数限额序号："+acrt_tmp.getAcrtno()+"，限额值："+acrt_tmp.getLmtval()+"，客户累计额："+talVal);
					if(aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1
							&& CommUtil.compare(talVal.add(BigDecimal.ONE), acrt_tmp.getLmtval()) > 0
							&& CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {
						throw QtError.Custa.BNASE089();
					}
				}

				// 年累计次数
				if(CommUtil.compare(acrt_tmp.getLimtkd(), E_LIMTKD._13) == 0){
					// String strServ = IoCaSevAccountLimtPublic.getLowerList(listSer, acrt_tmp.getServtp(), "0");
					String strServ;
					//　modify by zhuxw　20171024
					List<KupCurtServ> listSer1 = KupCurtServDao.selectAll_odb2(acrt_tmp.getServtp(), false);
					StringBuffer sbf = new StringBuffer();
					if(CommUtil.isNotNull(listSer1)){
						for(KupCurtServ KupCurtServ_tmp:listSer1){
							sbf.append("'").append(KupCurtServ_tmp.getServtp()).append("'");
						}
					}
					strServ = sbf.toString();
					//　modify by zhuxw　20171024
					String strLim =  IoCaSevAccountLimtPublic.getLowerList(listLim, acrt_tmp.getLimttp(), "1");
					String strSba =  IoCaSevAccountLimtPublic.getLowerList(listSba, acrt_tmp.getSbactp(), "2");
					BigDecimal talVal = IoCaSevAccountLimtPublic.getTsscAccVal(strLim, strServ, strSba, E_LIMTKD._13, currdt, acctList);
					log.debug("年累计次数限额序号："+acrt_tmp.getAcrtno()+"，限额值："+acrt_tmp.getLmtval()+"，客户累计额："+talVal);
					if (aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1
							&& CommUtil.compare(talVal.add(BigDecimal.ONE), acrt_tmp.getLmtval()) > 0
							&& CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {
						throw QtError.Custa.BNASE090();
					}
				}
			}

			sglmam = CaPublic.setValueIfZero(sglmam);
			dylmam = CaPublic.setValueIfZero(dylmam);
			mtlmam = CaPublic.setValueIfZero(mtlmam);
			yrlmam = CaPublic.setValueIfZero(yrlmam);
			dylmtm = CaPublic.setValueIfZero(dylmtm);
			mtlmtm = CaPublic.setValueIfZero(mtlmtm);
			yrlmtm = CaPublic.setValueIfZero(yrlmtm);

			BigDecimal talam01 = BigDecimal.ZERO; // 日累计金额
			BigDecimal talam02 = BigDecimal.ZERO; // 月累计金额
			BigDecimal talam03 = BigDecimal.ZERO; // 年累计金额
			long taltm01 = 0;// 日累计次数
			long taltm02 = 0;// 月累计次数
			long taltm03 = 0;// 年累计次数

			// 如果行社没有设置限额走下面控制，设置限额走下面控制也不受影响
			if (CommUtil.compare(tranam, sglmam) > 0 && CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {
				throw QtError.Custa.BNASE160();
			}
			if (aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1){
				talam01 = IoCaSevAccountLimtPublic.getTsscAccVal(limtStr, servtp, sbactp.getValue(), E_LIMTKD._02, currdt, acctList);
				if(CommUtil.compare(tranam.add(talam01), dylmam) > 0){
					throw QtError.Custa.BNASE161();
				}

				talam02 = IoCaSevAccountLimtPublic.getTsscAccVal(limtStr, servtp, sbactp.getValue(), E_LIMTKD._02, currdt, acctList);
				if(CommUtil.compare(tranam.add(talam02), mtlmam) > 0){
					throw QtError.Custa.BNASE162();
				}

				talam03 = IoCaSevAccountLimtPublic.getTsscAccVal(limtStr, servtp, sbactp.getValue(), E_LIMTKD._02, currdt, acctList);
				if( CommUtil.compare(tranam.add(talam03), yrlmam) > 0) {
					throw QtError.Custa.BNASE163();
				}

				taltm01 = IoCaSevAccountLimtPublic.getTsscAccVal(limtStr, servtp, sbactp.getValue(), E_LIMTKD._02, currdt, acctList).longValue();
				if( CommUtil.compare(taltm01 + 1, dylmtm.longValue()) > 0) {
					throw QtError.Custa.BNASE088();
				}

				taltm02 = IoCaSevAccountLimtPublic.getTsscAccVal(limtStr, servtp, sbactp.getValue(), E_LIMTKD._02, currdt, acctList).longValue();
				if(CommUtil.compare(taltm02 + 1, mtlmtm.longValue()) > 0) {
					throw QtError.Custa.BNASE089();
				}

				taltm03 = IoCaSevAccountLimtPublic.getTsscAccVal(limtStr, servtp, sbactp.getValue(), E_LIMTKD._02, currdt, acctList).longValue();
				if (CommUtil.compare(taltm03 + 1, yrlmtm.longValue()) > 0) {
					throw QtError.Custa.BNASE090();
				}
			}
		}	


	}
}
