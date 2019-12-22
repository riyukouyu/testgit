package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;
import java.text.ParseException;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.froz.DpFrozProc;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCary;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RECPAY;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRNESS;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;


public class actoac {

	public static void DealTransBefore(
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Input input,
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Property property,
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Output output) {

		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq(); 
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); 
		
		KnlCary cary = ActoacDao.selKnlCary(transq, trandt, false);
		if(CommUtil.isNotNull(cary)){
			property.setIssucc(E_YES___.YES);
			output.setMntrdt(cary.getTrandt());
			output.setMntrsq(cary.getTransq()); // 主流水
			output.setMntrtm(cary.getTrantm());
			return;
		}else{
			property.setIssucc(E_YES___.NO);
		}
		
		
		property.setIschrg(E_YES___.NO); //初始化收费控制标志
		if(CommUtil.compare(input.getTlcgam(), BigDecimal.ZERO) > 0){
			property.setIschrg(E_YES___.YES); //初始化收费控制标志
			property.setChgflg(E_CHGFLG.ALL); //设置记账标志
		}

		E_CAPITP capitp = E_CAPITP.NT301; //交易类型
		
		
		IoCaKnaAcdc otacdc = ActoacDao.selKnaAcdc(input.getOtcard(), false);
		if(CommUtil.isNull(otacdc)){
			throw DpModuleError.DpstComm.BNAS1902();
		}
		if(otacdc.getStatus() == E_DPACST.CLOSE){
			throw DpModuleError.DpstComm.BNAS1903();
		}
		
		//		IoCaKnaAcdc inacdc = ActoacDao.selKnaAcdc(input.getIncard(), false);
		//		if(CommUtil.isNull(inacdc)){
		//			throw DpModuleError.DpstComm.BNAS1904();
		//		}
		//	if(inacdc.getStatus() == E_DPACST.CLOSE){
		//		throw DpModuleError.DpstComm.BNAS1905();
		//	}
		
		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		E_ACCATP otaccatp = cagen.qryAccatpByCustac(otacdc.getCustac()); //转出方电子账户类型
		//	E_ACCATP inaccatp = cagen.qryAccatpByCustac(inacdc.getCustac()); //转入方电子账户类型
		

		KnaAcct otacct = SysUtil.getInstance(KnaAcct.class); // 转出方子账号
		//	KnaAcct inacct = SysUtil.getInstance(KnaAcct.class); // 转入方子账号
		
		
		if(otaccatp == E_ACCATP.WALLET){
			otacct = CapitalTransDeal.getSettKnaAcctSub(otacdc.getCustac(), E_ACSETP.MA);
			input.getChkqtn().setSbactp(E_SBACTP._12);
		}else{
			otacct = CapitalTransDeal.getSettKnaAcctSub(otacdc.getCustac(), E_ACSETP.SA);
			input.getChkqtn().setSbactp(E_SBACTP._11);
		}
		
		//转入方电子账户校验
		//		if (inaccatp == E_ACCATP.WALLET) {
		//		inacct = CapitalTransDeal.getSettKnaAcctSub(inacdc.getCustac(), E_ACSETP.MA);
		//		input.getChkqtn2().setSbactp(E_SBACTP._12);
		//	}else{
		//		inacct = CapitalTransDeal.getSettKnaAcctSub(inacdc.getCustac(), E_ACSETP.SA);
		//		input.getChkqtn2().setSbactp(E_SBACTP._11);
		//}
		
		//	if(CommUtil.isNotNull(input.getInacna()) && !CommUtil.equals(input.getInacna(), inacct.getAcctna())){
		//		throw DpModuleError.DpstComm.BNAS0892();
		//}
		
		if(CommUtil.isNotNull(input.getOtacna()) && !CommUtil.equals(input.getOtacna(), otacct.getAcctna())){
			throw DpModuleError.DpstComm.BNAS0892();
		}
		
		input.getChkqtn().setBrchno(otacct.getBrchno());// 交易机构号
		input.getChkqtn().setAccttp(otaccatp); //额度中心参数
		input.getChkqtn().setCustac(otacdc.getCustac()); //额度中心参数
		
		//	input.getChkqtn2().setBrchno(inacct.getBrchno());
		//	input.getChkqtn2().setAccttp(inaccatp);
		//	input.getChkqtn2().setCustac(inacdc.getCustac());
		
		//电子账户状态，状态字检查
		//AcTranfeChkIN chkIN = SysUtil.getInstance(AcTranfeChkIN.class);
		//chkIN.setAccatp(otaccatp);
		//chkIN.setCardno(otacdc.getCardno()); //电子账号卡号
		//chkIN.setCustac(otacdc.getCustac()); //电子账号ID
		//chkIN.setCustna(otacct.getAcctna());
		//chkIN.setCapitp(capitp);
		//chkIN.setOpactp(inaccatp);
		//chkIN.setOpcard(inacdc.getCardno());
		//chkIN.setOppoac(inacdc.getCustac());
		//chkIN.setOppona(inacct.getAcctna());
		//chkIN.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		
		//AcTranfeChkOT chkOT = CapitalTransCheck.chkTranfe(chkIN);
				
		//input.getChkqtn().setCustie(chkOT.getIsbind());
		//input.getChkqtn().setFacesg(chkOT.getFacesg());
		input.getChkqtn().setRecpay(E_RECPAY.PAY);
		
		//input.getChkqtn2().setCustie(chkOT.getIsbind());
		//input.getChkqtn2().setFacesg(chkOT.getOpface());
		input.getChkqtn2().setRecpay(E_RECPAY.REC);
		
		//E_CUACST opacst = chkOT.getOpacst();
		
		if(CommUtil.isNotNull(input.getOtacno()) && !CommUtil.equals(otacct.getAcctno(), input.getOtacno())){
			throw DpModuleError.DpstComm.BNAS1906();
		}
		
		//if(CommUtil.isNotNull(input.getInacno()) && !CommUtil.equals(inacct.getAcctno(), input.getInacno())){
		//	throw DpModuleError.DpstComm.BNAS1907();
		//}
		
		if(!CommUtil.equals(input.getCrcycd(),otacct.getCrcycd())){
			throw DpModuleError.DpstComm.BNAS1908();
		}
		
		
		if(input.getTrantp() == E_TRNESS.T02 || input.getTrantp() == E_TRNESS.T03){
			KnpParameter para = KnpParameterDao.selectOne_odb1("InParm.actoac", "%", "%", "%", true);
			
			property.setIntrtp(E_TRANTP.TR);
			property.setBusino(para.getParm_value1());
			property.setSubsac(para.getParm_value2());
			
			String datetime = CommTools.getBaseRunEnvs().getTrxn_date() + BusiTools.getBusiRunEnvs().getTrantm();
			if(input.getTrantp() == E_TRNESS.T02){
				int hours = ConvertUtil.toInteger(para.getParm_value3()); //普通转账延时小时数
				try {
					String n_datetime = DpFrozProc.getFreddt(hours, datetime);
					property.setAcdate(n_datetime.substring(0,8));
					property.setActime(n_datetime.substring(8));
				} catch (ParseException e) {

					throw DpModuleError.DpstComm.BNAS1909();

				}
			}else{
				property.setAcdate(CommTools.getBaseRunEnvs().getNext_date());
				property.setActime("000000");
			}
			
		}
		
		
		//property.setCuacst(opacst); //转入账户的客户化状态
		//property.setTblKnaAcct(inacct); //传入转入账户
		
		//property.setInbrch2(inacct.getBrchno());
		property.setOtbrch2(otacct.getBrchno());
		property.setOtcorp(otacct.getCorpno());
		//property.setIncorp(inacct.getCorpno());
		property.setOtcsac(otacct.getCustac()); // 电子账号ID
		property.setOtchld(otacct.getAcctno()); // 子账号
		//property.setIncsac(inacct.getCustac()); // 电子账号ID
		//property.setInchld(inacct.getAcctno()); // 子账号
//		property.setBusisq(CommTools.getBaseRunEnvs().getBstrsq()); // 业务跟踪编号
		property.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
		property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 业务跟踪编号
		property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 业务跟踪编号
		property.setLinkno(""); // 连笔号
		property.setPrcscd("actoac"); // 交易码
		property.setDcflag1(E_RECPAY.REC);//额度扣减收方
		property.setDcflag2(E_RECPAY.PAY);//额度扣减付方
		property.setSmryci(BusinessConstants.SUMMARY_ZR);//转入方摘要码
		property.setSmryco(BusinessConstants.SUMMARY_ZC);//转出方摘要码

	}
	/**
	 * @Title: changeAcctStuts 
	 * @Description:  休眠转正常
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年11月17日 上午10:27:56 
	 * @version V2.3.0
	 */
	public static void changeAcctStuts( final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Output output){
		//修改账户状态，休眠转正常结息
		//转入账户的电子账户信息，转入账户的结算户信息或钱包户信息
		CapitalTransDeal.dealAcctStatAndSett(property.getCuacst(), property.getTblKnaAcct());
	}
	
	/**
	 * @Title: DealTransAfter 
	 * @Description:  交易后处理
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年11月17日 上午10:28:12 
	 * @version V2.3.0
	 */
	public static void DealTransAfter(
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Input input,
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Property property,
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Output output) {

		if(property.getIssucc() == E_YES___.NO){
			
			// 平衡性检查
			E_CLACTP clactp=null;
			if(!CommUtil.equals(property.getInbrch2(), property.getOtbrch2())){
				clactp=E_CLACTP._10;
			}
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(
					CommTools.getBaseRunEnvs().getTrxn_date(),
					CommTools.getBaseRunEnvs().getMain_trxn_seq(),clactp);
	
			output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
			output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
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
	 * @date 2016年8月2日 上午9:19:10 
	 * @version V2.3.0
	 */
	public static void prcyInacRegister( final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Output output){
		
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
					cplKnbTrin.setOtcard(input.getOtcard());// 转出账号
					cplKnbTrin.setOtacna(input.getOtacna());// 转出账号名称
					cplKnbTrin.setOtbrch(property.getOtbrch2());// 转出机构
					cplKnbTrin.setIncard(input.getIncard());// 转入账号
					cplKnbTrin.setInacna(input.getInacna());// 转入账户名称
					cplKnbTrin.setInbrch(property.getInbrch2());// 转入账户机构
					cplKnbTrin.setTranam(input.getTranam());// 交易金额
					cplKnbTrin.setCrcycd(input.getCrcycd());// 币种
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
	 * @Title: chkParam 
	 * @Description: 输入参数检查  
	 * @param input
	 * @return
	 * @author zhangan
	 * @date 2017年1月4日 上午10:29:07 
	 * @version V2.3.0
	 */
	public static BigDecimal chkParam(final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Input input){
		
		BigDecimal tlcgam = BigDecimal.ZERO;
		
		if(input.getTrantp() == E_TRNESS.T02 || input.getTrantp() == E_TRNESS.T03){
			throw DpModuleError.DpstComm.BNAS1912();
		}
		                
		
		if(CommUtil.isNull(input.getSmrycd())){
			throw DpModuleError.DpstComm.BNAS0195();
		}
		
		if (CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0) {
			throw DpModuleError.DpstComm.BNAS0394();
		}
		
		if(CommUtil.compare(input.getTlcgam(),BigDecimal.ZERO) > 0){
			// 收费交易金额检查
			if(input.getChrgpm().size() <= 0){
				throw DpModuleError.DpstComm.BNAS0395();
			}
			BigDecimal totPaidam = BigDecimal.ZERO;
			for (IoCgCalCenterReturn IoCgCalCenterReturn : input.getChrgpm()) {
				BigDecimal tranam = IoCgCalCenterReturn.getTranam();// 交易金额
				BigDecimal clcham = IoCgCalCenterReturn.getClcham();// 应收费用金额（未优惠）
				BigDecimal dircam = IoCgCalCenterReturn.getDircam();// 优惠后应收金额
				BigDecimal paidam = IoCgCalCenterReturn.getPaidam();// 实收金额
				
				if (CommUtil.isNotNull(tranam)) {
					if(CommUtil.compare(tranam, BigDecimal.ZERO) < 0){ //交易金额金额
						throw DpModuleError.DpstComm.BNAS0338();
					}
					if(CommUtil.compare(tranam, input.getTranam()) != 0){ //交易金额金额
						throw DpModuleError.DpstComm.BNAS0337();
					}
				}
				if (CommUtil.isNotNull(clcham)) {
					if(CommUtil.compare(clcham, BigDecimal.ZERO) < 0){ //交易金额金额
						throw DpModuleError.DpstComm.BNAS0244();
					}
				}
				if (CommUtil.isNotNull(dircam)) {
					if(CommUtil.compare(dircam, BigDecimal.ZERO) < 0){ //交易金额金额
						throw DpModuleError.DpstComm.BNAS0237();
					}
				}
				if (CommUtil.isNotNull(paidam)) {
					if(CommUtil.compare(paidam, BigDecimal.ZERO) < 0){ //交易金额金额
						throw DpModuleError.DpstComm.BNAS0355();
					}
				}
				
				
				totPaidam = totPaidam.add(paidam);
				tlcgam = tlcgam.add(paidam);
			}
			
			if(!CommUtil.equals(totPaidam, input.getTlcgam())){
				throw DpModuleError.DpstComm.BNAS0243();
			}
		}
		
//		if (CommUtil.isNull(input.getChkqtn().getIsckqt())) {
//			throw DpModuleError.DpstComm.E9027("额度验证扣减标志");
//		}
		
		
		if (CommUtil.compare(input.getOtcard(), input.getIncard()) == 0) {
			throw DpModuleError.DpstComm.BNAS1913();
		}
		
		return tlcgam;
		
	}
	public static void FrozTranBefore( final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Actoac.Output output){
	    property.setSprdid(null);
	    property.setFroztp(E_FROZTP.AM); // 冻结业务类型 -- 金额冻结
	    property.setSprdvr(null); // 版本号
	    property.setSprdna(null); // 产品名称
	    property.setFtrate(null); // 预计年化收益率（最低）
	    property.setTtrate(null); // 预计年化收益率（最高）
	    property.setEndday(DateTools2.calDateByNextFreq(CommTools.getBaseRunEnvs().getTrxn_date(), "3DA")); // 冻结终止日期--三天后
    }
}
