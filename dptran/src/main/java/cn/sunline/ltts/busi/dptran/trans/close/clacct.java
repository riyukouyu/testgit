package cn.sunline.ltts.busi.dptran.trans.close;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.acct.DpCloseAcctno;
import cn.sunline.ltts.busi.dp.acct.DpCloseCustac;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.AccChngbrDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcal;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCacd;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaSignDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbClac;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseOT;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CLOSST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SLEPST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SPPRST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSATP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSTAT;
import cn.sunline.ltts.busi.sys.type.PbEnumType;
import cn.sunline.ltts.busi.wa.type.WaAcctType.IoWaKnaRelt;


public class clacct {
	/**
	 * 
	 * @Title: prcCloseTransBefore
	 * @Description: (客户端销户交易前处理)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年7月12日 下午8:19:27
	 * @version V2.3.0
	 */
	public static void prcCloseTransBefore(
			final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Output output) {

		String sCardno = input.getCardno();// 电子账号
		String sCustna = input.getCustna();// 账户名称
		BigDecimal bigClosam = input.getClosam();// 销户金额
		String sTracno = input.getTracno();// 转入账号
		String sTracna = input.getTracna();// 转入户名
		E_CLSATP sTractp = input.getTractp();// 收款人账户类型
		String sSmrycd = BusiTools.getBusiRunEnvs().getSmrycd();// 摘要码

		String timetm =DateTools2.getCurrentTimestamp();
		
		
		// 输入接口校验
		// 电子账号
		if (CommUtil.isNull(sCardno)) {
			throw DpModuleError.DpstComm.BNAS0311();
		}
		// 账户名称
		if (CommUtil.isNull(sCustna)) {
			throw DpModuleError.DpstComm.BNAS0303();
		}
		// 销户金额
		if (CommUtil.isNull(bigClosam)) {
			throw DpModuleError.DpstComm.BNAS0305();
		}
		// 摘要码
		if (CommUtil.isNull(sSmrycd)) {
			throw DpModuleError.DpstComm.BNAS0304();
		}
		
		// 若销户金额不为零，检查转入方校验参数
		if (!CommUtil.equals(bigClosam, BigDecimal.ZERO)) {
			
			// 转入账号
			if (CommUtil.isNull(sTracno)) {
				throw DpModuleError.DpstComm.BNAS1259();
			}
			// 转入户名
			if (CommUtil.isNull(sTracna)) {
				throw DpModuleError.DpstComm.BNAS1260();
			}
			// 收款人账户类型
			if (CommUtil.isNull(sTractp)) {
				throw DpModuleError.DpstComm.BNAS0306();
			}

			// 检查转入类型
			if (sTractp == E_CLSATP.INACCT) {
				throw DpModuleError.DpstComm.BNAS0539();
			}
			
			// 检查转入类型
			if (sTractp == E_CLSATP.CUSTAC) {
				throw DpModuleError.DpstComm.BNAS0540();
			}

			// 客户端销户同名校验
			if (!CommUtil.equals(sCustna, sTracna)) {
				throw DpModuleError.DpstComm.BNAS0889();
			}

			// 查询电子账户信息
			IoCaKnaCust cplKnaCust = SysUtil.getInstance(
					IoCaSevQryTableInfo.class).getKnaCustByCardnoOdb1(
					sCardno, true);

			// 查询出电子账户的账户分类
			E_ACCATP eAccatp = SysUtil.getInstance(
					IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(
					cplKnaCust.getCustac());
			
			//add by xiongzhao 20170106 新增签约校验
			KnaAcct tblKnaAcct = CapitalTransDeal.getSettKnaAcctAc(cplKnaCust.getCustac());
			// 获取转存签约明细信息
			IoCaKnaSignDetl cplkna_sign_detl = ActoacDao.selKnaSignDetl(tblKnaAcct.getAcctno(),
					E_SIGNTP.ZNCXL, E_SIGNST.QY, false);
			if (CommUtil.isNotNull(cplkna_sign_detl)) {
				throw DpModuleError.DpstAcct.BNAS0996();
			}
			
			// 理财功能户转出必须为绑定账户
			if (eAccatp == E_ACCATP.FINANCE) {
				IoCaKnaCacd cplKnaCacd = SysUtil.getInstance(
						IoCaSevQryTableInfo.class).getKnaCacdOdb1(
						cplKnaCust.getCustac(), sTracno, false);
				if (CommUtil.isNull(cplKnaCacd)
						|| cplKnaCacd.getStatus() != E_DPACST.NORMAL) {
					throw DpModuleError.DpstComm
							.BNAS0491();
				}
			}
		}
		
		// 调用方法检查电子账户销户需要满足的状态和状态字并获取电子账号ID
		IoCaKnaCust cplKnaCust = clsoeAcctStautsCheck.acctStautsCheck(sCardno);
		String sCustac = cplKnaCust.getCustac();// 电子账号ID
		
		//add by songkl 20161219 新增校验
		//查询是否开通亲情钱包，若开通亲情钱包则允许销户
		List<IoWaKnaRelt> tblknarelt = DpAcctQryDao.selknareltbycustac(cplKnaCust.getCustac(), false);
		if(CommUtil.isNotNull(tblknarelt)){
			throw DpModuleError.DpstAcct.BNAS1939();
		}

		//检查销户登记簿是否有未处理完成的记录
		IoCaKnbClac tblKnbClac = DpAcctDao.selKnbClacByDoubleStat(sCustac, E_CLSTAT.DEAL, E_CLSTAT.TRSC, false);
		if(CommUtil.isNotNull(tblKnbClac)){
			if(tblKnbClac.getStatus() == E_CLSTAT.DEAL){ //如果是预销户状态，则作废该条记录，并继续向下做销户处理
				DpAcctDao.updKnbClacStatBySeq(E_CLSTAT.FAIL, tblKnbClac.getClossq(),timetm);
			}else{ //如果是转账成功，则需要等待转账结果，不允许继续做销户交易
				throw DpModuleError.DpstComm.BNAS0998();	
			}
		}
		
		// 查询出电子账户的账户分类
		E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(cplKnaCust.getCustac());

		// 查询销户的电子账户客户信息
		//IoCucifCust cplCucifCust = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_selectOne_odb1(cplKnaCust.getCustno(), true);
//		IoSrvCfPerson.IoGetCifCust.InputSetter queryCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//		queryCust.setCustno(cplKnaCust.getCustno());
//		IoSrvCfPerson.IoGetCifCust.Output cplCucifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//		SysUtil.getInstance(IoSrvCfPerson.class).getCifCust(queryCust, cplCucifCust);
		
		// 查询出用户ID
		//IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_accsByCustno(cplKnaCust.getCustno(), true, E_STATUS.NORMAL);
		
		// 查询出绑定手机号
		IoCaKnaAcal cplKnaAcal = AccChngbrDao.selKnaacalByCus(sCustac, E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);			
		
		//若存在绑定手机号则登记不存在不做登记
		if(CommUtil.isNotNull(cplKnaAcal)){
			property.setAcalno(cplKnaAcal.getAcalno());// 电子账户绑定手机号
		}		
		
		// 将查询出的电子账号ID映射到属性区
		property.setCustac(sCustac);// 电子账号ID //交易后处理需要用到参数
		property.setCustid(cplKnaCust.getCustno());// 用户ID
		property.setAccatp(eAccatp);// 电子账户分类
		property.setBrchno(cplKnaCust.getBrchno());// 电子账户开户机构
		//property.setAcalno(cplKnaAcal.getAcalno());// 电子账户绑定手机号
		
		// 将销户校验字段映射到属性区
		property.getCplClsAcctIn().setCardno(sCardno);// 电子账号
		property.getCplClsAcctIn().setCustac(sCustac);// 电子账号ID
		property.getCplClsAcctIn().setCustna(sCustna);// 账户名称
//		property.getCplClsAcctIn().setCustno(cplCucifCust.getCustno());// 客户号
//		property.getCplClsAcctIn().setIdtfno(cplCucifCust.getIdtfno());// 证件号码
//		property.getCplClsAcctIn().setIdtftp(cplCucifCust.getIdtftp());// 证件类型
		
		property.setClstat(E_CLSTAT.DEAL); //初始化执行状态标志
		
		//获取电子账户客户号状态,并设置后续步骤的执行标志,如果客户化状态是预销户，则可直接进入交易后处理，在交易后处理中获取原销户流水和日期进行返回
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(sCustac);
		property.setCuacst(cuacst);
		if(cuacst == E_CUACST.PRECLOS){ //预销户
			property.setClstat(E_CLSTAT.SUCC); //设置为空，表示后续步骤不执行
			
			return;
		}
	}

	/**
	 * 
	 * @Title: prcCloseMidd
	 * @Description: (客户端销户交易处理中)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年7月12日 下午8:20:22
	 * @version V2.3.0
	 */
	public static void prcCloseMidd(
			final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Output output) {
		
		BigDecimal interest = BigDecimal.ZERO;// 利息
		
		// 查询电子账户个人结算户信息
		KnaAcct cplKnaAcct = CapitalTransDeal.getSettKnaAcctAc(property.getCustac());
		
		IoDpCloseIN to_clsin = SysUtil.getInstance(IoDpCloseIN.class);
		
		IoDpCloseIN clsin = SysUtil.getInstance(IoDpCloseIN.class);
		
		clsin.setAccatp(property.getAccatp());
		clsin.setCardno(input.getCardno());
		clsin.setCrcycd(cplKnaAcct.getCrcycd());
		clsin.setCustac(property.getCustac());
		clsin.setToacct(input.getTracno());
		clsin.setToname(input.getTracna());
		clsin.setSmrycd(BusinessConstants.SUMMARY_CZ);
		
		IoDpCloseOT clsot = DpCloseCustac.CloseCustac(clsin); //调用电子账户销户服务
		
		InterestAndIntertax cplint = DpCloseAcctno.prcCurrInterest(cplKnaAcct, to_clsin);
		interest = cplint.getDiffam();
		property.setSettbl(clsot.getSettbl().add(cplKnaAcct.getOnlnbl()).add(interest));
		property.setCrcycd(cplKnaAcct.getCrcycd());
		property.setAcctno(cplKnaAcct.getAcctno());
		
		SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class); //电子账户存入记账复合类型
		DrawDpAcctIn drawin = SysUtil.getInstance(DrawDpAcctIn.class); //支取服务输入参数
		
		if(CommUtil.compare(cplint.getInstam(), BigDecimal.ZERO) > 0){
			//调用存入服务，存入利息\
							
			saveIn.setAcctno(cplKnaAcct.getAcctno()); //结算账户、钱包账户
			saveIn.setBankcd("");
			saveIn.setBankna("");
			saveIn.setCardno(input.getCardno());
			saveIn.setCrcycd(cplKnaAcct.getCrcycd());
			saveIn.setCustac(cplKnaAcct.getCustac());
			saveIn.setOpacna(cplKnaAcct.getAcctna());
			saveIn.setOpbrch(cplKnaAcct.getBrchno());
			saveIn.setRemark("子账户销户结息转入");
			saveIn.setSmrycd(BusinessConstants.SUMMARY_SX);
			saveIn.setToacct(cplKnaAcct.getAcctno());
			saveIn.setTranam(cplint.getInstam());
			saveIn.setIschck(E_YES___.NO);
			
			SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(saveIn);
			
		}
		//利息税入账
		if(CommUtil.compare(cplint.getIntxam(), BigDecimal.ZERO) > 0){
			//结算户支取记账处理				
			drawin.setAcctno(cplKnaAcct.getAcctno()); //做支取的负债账号
			drawin.setAuacfg(E_YES___.NO);
			drawin.setCardno(input.getCardno());
			drawin.setCrcycd(cplKnaAcct.getCrcycd());
			drawin.setCustac(cplKnaAcct.getCustac());
			drawin.setLinkno(null);
			drawin.setOpacna(cplKnaAcct.getAcctna());
			drawin.setToacct(cplKnaAcct.getAcctno()); //结算账号
			drawin.setTranam(cplint.getIntxam());
			drawin.setSmrycd(BusinessConstants.SUMMARY_JS);// 缴税
			drawin.setSmryds("缴税");
			drawin.setRemark("利息税入账");
			SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(drawin);
						
		}
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._01);

		//如果原状态是休眠,则修改休眠登记簿状态是休眠转销户
		if(property.getCuacst() == E_CUACST.DORMANT){
			String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
			String trantm = BusiTools.getBusiRunEnvs().getTrantm();
			String timetm =DateTools2.getCurrentTimestamp();
			ActoacDao.updKnbSlepStat(E_SPPRST.CNCL, trandt, trantm, E_SLEPST.CANCEL, cplKnaAcct.getCustac(), E_SLEPST.SLEP,timetm);
		}
		
		// 销户金额对比
		if (!CommUtil.equals(input.getClosam(), property.getSettbl())) {
			throw DpModuleError.DpstComm.BNAS0288();
		}
		
	}

	/**
	 * 
	 * @Title: prcCloseTransAfter
	 * @Description: (客户端销户交易后处理)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年7月12日 下午8:20:53
	 * @version V2.3.0
	 */
	public static void prcCloseTransAfter(
			final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Output output) {

		IoCaKnbClac cplKnbClac = SysUtil.getInstance(IoCaKnbClac.class);
		
//		if(property.getClstat() == E_CLSTAT.DEAL){
		// 登记销户登记簿
		cplKnbClac.setAccttp(property.getAccatp());// 账户分类
		cplKnbClac.setClosam(property.getSettbl());// 销户金额
		cplKnbClac.setClosbr(CommTools.getBaseRunEnvs().getTrxn_branch());// 销户机构
		cplKnbClac.setClosdt(CommTools.getBaseRunEnvs().getTrxn_date());// 销户日期
		cplKnbClac.setClossq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 销户流水
		cplKnbClac.setClossv(CommTools.getBaseRunEnvs().getChannel_id());// 销户渠道
		cplKnbClac.setClosus(CommTools.getBaseRunEnvs().getTrxn_teller());// 销户柜员
		cplKnbClac.setCrcycd(property.getCrcycd());// 币种
		cplKnbClac.setCsextg(E_CSEXTG.CASH);// 钞汇标志
		cplKnbClac.setCustac(property.getCustac());// 电子账号
		cplKnbClac.setCustna(input.getCustna());// 户名
		cplKnbClac.setTnacna(input.getTracna());// 转入户名
		cplKnbClac.setTnacno(input.getTracno());// 转入账号
		cplKnbClac.setTnactp(input.getTractp());// 转入账户类型
		if(CommUtil.compare(property.getSettbl(), BigDecimal.ZERO) <= 0){
			cplKnbClac.setStatus(E_CLSTAT.SUCC);// 处理状态
		}else{
			cplKnbClac.setStatus(E_CLSTAT.DEAL);// 处理状态
		}
		cplKnbClac.setAcalno(property.getAcalno());// 绑定手机号
		cplKnbClac.setCustid(property.getCustid());// 用户ID
		cplKnbClac.setAcctno(property.getAcctno());
		
		String timetm = DateTools2.getCurrentTimestamp();
		
		SysUtil.getInstance(IoCaSevQryTableInfo.class).saveKnbClac(cplKnbClac);
			
			
//		}else{
//			cplKnbClac = DpAcctDao.selKnbClac(property.getCustac(), E_CLSTAT.SUCC, false);
//		}

		if(property.getClstat() == E_CLSTAT.DEAL){
			//零金额销户
			if(CommUtil.compare(property.getSettbl(), BigDecimal.ZERO) <= 0){
				// 注销电子账户
				//DpAcctProc.prcAcctst(property.getCplClsAcctIn());
				property.getCplClsAcctIn().setClossq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
				SysUtil.getInstance(DpAcctSvcType.class).acctStatusUpd(property.getCplClsAcctIn());
				property.setCuacst(E_CUACST.CLOSED);
				//output.setClosst(E_CLOSST.CLOSEAC);// 零金额改为销户
			}else{
				property.setCuacst(E_CUACST.PRECLOS);
				//修改客户信息表记录为结清
				ActoacDao.updKnaCustStat(E_ACCTST.SETTLE,E_CUACST.CLOSED, CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), property.getCustac(),timetm,CommTools.getBaseRunEnvs().getChannel_id());
				
				//预销户状态需要计息计提，因此此处不修改子账户状态
				
				//ActoacDao.updKnaAcctStat(E_DPACST.CLOSE, CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), property.getAcctno());
			}
			
			
			//登记客户化状态
			IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
			cplDimeInfo.setCustac(property.getCustac());
			if(CommUtil.compare(property.getSettbl(), BigDecimal.ZERO) > 0){
				cplDimeInfo.setDime01(input.getTractp().getValue()); //收款人账户类型
			}
			
			// 更新客户化状态
			SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);

/*			// 检查电子账户是否只开过户
			E_YES___ obopfg = SysUtil.getInstance(IoAccChngbrSvc.class)
					.judgeOpenJust(property.getCustac());
			
			// 若只开户标志为YES，则将销掉的卡号登记到电子账户卡号复用表
			if (obopfg == E_YES___.YES) {
				// 查询卡号是否已经登记了复用信息
				IoCaKnbRecl cplKnbRecl = SysUtil.getInstance(IoCaSevQryTableInfo.class).knb_recl_selectOne_odb1(input.getCardno(), false);
				if (CommUtil.isNull(cplKnbRecl)) {
					// 插入信息
					SysUtil.getInstance(IoCaSevQryTableInfo.class).knb_recl_insert(input.getCardno(),CommTools.getBaseRunEnvs().getTrxn_date(),E_ENABST.NOTAPPLY);
				} else {
					// 更新信息
					SysUtil.getInstance(IoCaSevQryTableInfo.class).knb_recl_updateOne_odb1(input.getCardno(),CommTools.getBaseRunEnvs().getTrxn_date(),E_ENABST.NOTAPPLY);
				}
			}*/
		}
		
		
		
		if(property.getCuacst() == E_CUACST.PRECLOS){
			output.setClosst(E_CLOSST.FORECLAC);
		}else{
			output.setClosst(E_CLOSST.CLOSEAC);
		}
		output.setPyinsq(cplKnbClac.getClossq());
		output.setPyindt(cplKnbClac.getClosdt());
		output.setSettbl(cplKnbClac.getClosam());
		output.setAccatp(cplKnbClac.getAccttp());
				
	}
	
	/*
	 * 销户前检查是否有未领取红包
	 */
	public static void chkRedpack( final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Output output){
		
		CapitalTransDeal.chkRedpack(input.getCardno(), property.getCustid());
	}

	/**
	 * 
	 * @Title: sendCloseInfoMsg
	 * @Description: (销户信息通知)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年7月26日 下午10:23:29
	 * @version V2.3.0
	 */
	public static void sendCloseInfoMsg(
			final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Output output) {

		// 查询销户登记簿处理状态为成功的记录
		//IoCaKnbClac cplKnbClac = DpAcctDao.selKnbClac(property.getCustac(),E_CLSTAT.SUCC, false);

		if (property.getCuacst() == E_CUACST.CLOSED) {
			
//			//修改销户cmq通知  modify lull
//			MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//			mri.setMtopic("Q0101004");
//			IoCaCloseAcctSendMsg closeSendMsgInput = SysUtil.getInstance(IoCaCloseAcctSendMsg.class);
//			closeSendMsgInput.setCustid(property.getCustid()); // 用户ID
//			closeSendMsgInput.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());// 操作机构
//			closeSendMsgInput.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());// 操作柜员
//			closeSendMsgInput.setClossv(CommTools.getBaseRunEnvs().getChannel_id());// 销户渠道
//			closeSendMsgInput.setClosfg(E_YES___.NO);
//
//			mri.setMsgtyp("ApSmsType.IoCaCloseAcctSendMsg");
//			mri.setMsgobj(closeSendMsgInput); 
//			AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
//			E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介

			/*KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("CLOSAC", "CUSTSM",
					"%", "%", true);
			
			String bdid = tblKnaPara.getParm_value1();// 服务绑定ID
			
			IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind(
					IoCaOtherService.class, bdid);
			
			// 1.销户成功发送销户结果到客户信息
			String mssdid = CommTools.getMySysId();// 消息ID
			String mesdna = tblKnaPara.getParm_value2();// 媒介名称

			IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter closeSendMsgInput = SysUtil.getInstance(IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter.class);
			
			closeSendMsgInput.setMsgid(mssdid); // 发送消息ID
//			closeSendMsgInput.setMedium(mssdtp); // 消息媒介
			closeSendMsgInput.setMdname(mesdna); // 媒介名称
			closeSendMsgInput.setCustid(property.getCustid()); // 用户ID
			closeSendMsgInput.setClosbr(CommTools.getBaseRunEnvs().getTrxn_branch());// 操作机构
			closeSendMsgInput.setClosus(CommTools.getBaseRunEnvs().getTrxn_teller());// 操作柜员
			closeSendMsgInput.setClossv(CommTools.getBaseRunEnvs().getChannel_id());// 销户渠道
			closeSendMsgInput.setClosfg(E_YES___.NO);// 是否挂失销户标志
			
			caOtherService.closeAcctSendMsg(closeSendMsgInput);*/
			
			// 2.销户成功发送销户结果到合约库
			/*KnpParameter tblKnaPara1 = KnpParameterDao.selectOne_odb1("CLOSAC", "AGRTSM",
					"%", "%", true);
			
			String mssdid1 = CommTools.getMySysId();// 消息ID
			
			String mesdna1 = tblKnaPara1.getParm_value2();// 媒介名称

			IoCaOtherService.IoCaClAcSendContractMsg.InputSetter closeSendAgrtInput = SysUtil.getInstance(IoCaOtherService.IoCaClAcSendContractMsg.InputSetter.class);
			
			closeSendAgrtInput.setMsgid(mssdid1); // 发送消息ID
//			closeSendAgrtInput.setMedium(mssdtp); // 消息媒介
			closeSendAgrtInput.setMdname(mesdna1); // 媒介名称
			closeSendAgrtInput.setUserId(property.getCustid()); // 用户ID
			closeSendAgrtInput.setAcctType(property.getAccatp());// 账户分类
			closeSendAgrtInput.setOrgId(property.getBrchno());// 归属机构
			closeSendAgrtInput.setAcctNo(input.getCardno());// 电子账号
			closeSendAgrtInput.setAcctStat(E_CUACST.CLOSED);// 客户化状态
			closeSendAgrtInput.setAcctName(input.getCustna());// 户名
			closeSendAgrtInput.setCertNo(property.getCplClsAcctIn().getIdtfno());// 证件号码
			closeSendAgrtInput.setCertType(property.getCplClsAcctIn().getIdtftp());// 证件类型
			closeSendAgrtInput.setTransTime(DateTools2.getCurrentTimestamp());//操作时间

			caOtherService.clAcSendContractMsg(closeSendAgrtInput);*/
			
		}

	  }
	
	/**
	 * 涉案账号交易信息登记
	 * @Title: prcyInacRegister 
	 * @Description: 涉案账号交易信息登记 
	 * @param input
	 * @param property
	 * @param output
	 * @author liaojincai
	 * @date 2016年8月2日 上午9:43:48 
	 * @version V2.3.0
	 */
	public static void prcyInacRegister( final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Output output){
		
		E_INSPFG invofg = property.getInvofg();// 转出账号是否涉案
		E_INSPFG invofg1 = property.getInvofg1();// 转入账号是否涉案

		// 涉案账户交易信息登记
		if (E_INSPFG.INVO == invofg || E_INSPFG.INVO == invofg1) {

			// 独立事务
			DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
				@Override
				public Void execute() {

					// 获取涉案账户交易信息登记输入信息
					IoCaKnbTrinInput cplKnbTrin = SysUtil.getInstance(IoCaKnbTrinInput.class);
					cplKnbTrin.setTrantp(cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRANTP.TRANSFER);// 交易类型
					cplKnbTrin.setOtcard(input.getCardno());// 转出账号
					cplKnbTrin.setOtacna(input.getCustna());// 转出账号名称
					cplKnbTrin.setOtbrch(CommTools.getBaseRunEnvs().getTrxn_branch());// 转出机构
					cplKnbTrin.setIncard(input.getTracno());// 转入账号
					cplKnbTrin.setInacna(input.getTracna());// 转入账户名称
					cplKnbTrin.setInbank(input.getTrbkno());// 转入账户机构
					cplKnbTrin.setTranam(input.getClosam());// 交易金额
					cplKnbTrin.setIssucc(E_YES___.NO);// 交易是否成功

					// 涉案账户交易信息登记
					SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(cplKnbTrin);

					return null;
				}
			});

			// 转出账号涉案
			if (E_INSPFG.INVO == invofg) {
				throw DpModuleError.DpstAcct.BNAS1910();
			}

			// 转入账号涉案
			if (E_INSPFG.INVO == invofg1) {
				throw DpModuleError.DpstAcct.BNAS1911();
			}

		}

	}
	/**
	 * @Title: chkNotChrgFee 
	 * @Description: 是否有未收讫费用检查  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年12月22日 下午2:02:04 
	 * @version V2.3.0
	 */
	public static void chkNotChrgFee( final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Clacct.Output output){
		PbEnumType.E_YES___ isfee = SysUtil.getInstance(IoCgChrgSvcType.class).CgChageRgstNotChargeByCustac(property.getCustac());
		if(isfee == PbEnumType.E_YES___.YES){
			throw DpModuleError.DpstAcct.BNAS0854();
		}
	}

	
}
