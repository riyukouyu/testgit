package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIobl;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevGenBindCard;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCacd;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
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
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BACATP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;



public class tranfe {


	
	/***
	 * 转账前处理
	 * */
	private static BizLog log = BizLogUtil.getBizLog(tranfe.class);
	
	/***
	 * 转账交易前处理
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranfe.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranfe.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranfe.Output output){
		
		// 输入项非空检查
		if (CommUtil.isNull(input.getCapitp())) {
			throw DpModuleError.DpstComm.BNAS0023();
		}
		if (CommUtil.isNull(input.getCrcycd())) {
			throw DpModuleError.DpstComm.BNAS1101();
		}
		if (CommUtil.isNull(input.getCsextg())) {
			throw DpModuleError.DpstComm.BNAS1047();
		}
		if (CommUtil.isNull(input.getTranam())) {
			throw DpModuleError.DpstProd.BNAS0620();
		}
		if (CommUtil.isNull(input.getSmrycd())) {
			throw DpModuleError.DpstComm.BNAS0195();
		}
		if (CommUtil.isNull(input.getTlcgam())) {
			throw DpModuleError.DpstComm.BNAS0331();
		}
		if (CommUtil.isNull(input.getChckdt())) {
			throw DpModuleError.DpstComm.BNAS0808();
		}
		if (CommUtil.isNull(input.getKeepdt())) {
			throw DpModuleError.DpstComm.BNAS0399();
		}
		if (CommUtil.isNull(input.getChkqtn().getIsckqt())) {
			throw DpModuleError.DpstComm.BNAS0802();
		}
//		if(input.getCrcycd() == BusiTools.getDefineCurrency() && CommUtil.isNotNull(input.getCsextg())){
//			throw CaError.Eacct.E0001("当币种为人民币时，钞汇属性不可选");
//		}
//		if(input.getCrcycd() != BusiTools.getDefineCurrency() && CommUtil.isNull(input.getCsextg())){
//			throw CaError.Eacct.E0001("若币种选择非人民币选项时，钞汇属性必选");
//		}
		
		//交易前检查原流水是否存在
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		KnlIobl iobl = ActoacDao.selKnlIoblDetl(transq, trandt, false);
		if(CommUtil.isNotNull(iobl)){
			property.setIssucc(E_YES___.YES);
			output.setMntrdt(iobl.getTrandt());	//
			output.setMntrsq(iobl.getTransq()); // 主流水
			output.setMntrtm(iobl.getTrantm());
			return;
		}else{
			property.setIssucc(E_YES___.NO);
		}
		
		E_CAPITP capitp = input.getCapitp(); //交易类型
		log.debug("<<==交易类型：[%s],交易名称：[%s]==>>", capitp.getValue(),capitp.getLongName());
		
		if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0){ //校验交易金额
			throw DpModuleError.DpstComm.BNAS0627();
		}
		if(CommUtil.compare(input.getTlcgam(), BigDecimal.ZERO) < 0){ //校验收费总金额
			throw DpModuleError.DpstComm.BNAS0328();
		}
		
		
		//出入金标志
		E_IOFLAG ioflag = null;
		
		if(E_CAPITP.E_CAPIIN.in(capitp)){ //转入类交易
			ioflag = E_IOFLAG.IN;
		}else if(E_CAPITP.E_CAPIOT.in(capitp)){ //转出类交易
			ioflag = E_IOFLAG.OUT;
		}else{
			throw DpModuleError.DpstComm.BNAS1933();
		}
		
		if(ioflag == E_IOFLAG.OUT){ //电子账户转出
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
				}
				
				if(!CommUtil.equals(totPaidam, input.getTlcgam())){
					throw DpModuleError.DpstComm.BNAS0243();
				}
			}
		
		}
		
		if(capitp == E_CAPITP.IN106 || capitp == E_CAPITP.IN105){ //CUPS转入 无卡存款
			if(CommUtil.isNull(input.getIncard())){
				throw DpModuleError.DpstComm.BNAS0030();
			}
		}else if(capitp == E_CAPITP.OT204){
			if(CommUtil.isNull(input.getOtcard())){
				throw DpModuleError.DpstComm.BNAS0042();
			}
		}else{
			if(CommUtil.isNull(input.getIncard())){
				throw DpModuleError.DpstComm.BNAS0030();
			}
			if(CommUtil.isNull(input.getInacna())){
				throw DpModuleError.DpstComm.BNAS0032();
			}
			if(CommUtil.isNull(input.getOtcard())){
				throw DpModuleError.DpstComm.BNAS0042();
			}
			if(CommUtil.isNull(input.getOtacna())){
				throw DpModuleError.DpstComm.BNAS0045();
			}
		}
		
		IoCaSevGenBindCard bind = SysUtil.getInstance(IoCaSevGenBindCard.class);//电子账户绑卡服务
		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);//电子账户服务
		
		
		property.setIschrg(E_YES___.NO); //初始化收费的参数
		
		//1.查询卡客户账号对照表
		if(ioflag == E_IOFLAG.OUT){ //电子账户转出
			
			IoCaKnaAcdc otacdc = ActoacDao.selKnaAcdc(input.getOtcard(), false);
			if(CommUtil.isNull(otacdc)){
				throw DpModuleError.DpstComm.BNAS0750();
			}
			
			if(otacdc.getStatus() == E_DPACST.CLOSE){
				throw DpModuleError.DpstComm.BNAS0441();
			}
			//转出电子账户信息
			
			//String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//			CommTools.getBaseRunEnvs().setBusi_org_id(otacdc.getCorpno());
			
			//账户类型
			E_ACCATP accatp = cagen.qryAccatpByCustac(otacdc.getCustac()); 
			//设置额度控制参数
			
			//检查转出金额与额度控制参数的金额是否一致
//			if(input.getChkqtn().getIsckqt() == E_YES___.YES){
//				if(CommUtil.compare(input.getTranam(), input.getChkqtn().getTranam())!=0){
//					throw DpModuleError.DpstComm.E9999("转出金额与额度控制金额不一致");
//				}
//			}
			//电子账户还信用卡,不校验客户类型
			if(capitp != E_CAPITP.OT202){ 
				CapitalTransCheck.ChkAccttp(capitp, accatp);
			}
			//1.转出方电子账户状态字校验
			CapitalTransCheck.ChkAcctstOT(otacdc.getCustac()); 
			//2.转出方状态字检查
			CapitalTransCheck.ChkAcctFrozOT(otacdc.getCustac());
			//获取结算户或钱包户
			KnaAcct otacct = SysUtil.getInstance(KnaAcct.class); //转出方子账号
			if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){
				otacct = CapitalTransDeal.getSettKnaAcctSub(otacdc.getCustac(), E_ACSETP.SA);
				input.getChkqtn().setSbactp(E_SBACTP._11);
				if(CommUtil.isNotNull(input.getOtcstp()) && input.getOtcstp() != E_ACSETP.SA){
					throw DpModuleError.DpstComm.BNAS0029();
				}
			}else{
				otacct = CapitalTransDeal.getSettKnaAcctSub(otacdc.getCustac(), E_ACSETP.MA);
				//property.setAuacfg(E_YES___.NO); //存取标志
				input.getChkqtn().setSbactp(E_SBACTP._12);
				if(CommUtil.isNotNull(input.getOtcstp()) && input.getOtcstp() != E_ACSETP.MA){
					throw DpModuleError.DpstComm.BNAS0029();
				}
			}
			
			input.getChkqtn().setAccttp(accatp);
			input.getChkqtn().setCustac(otacdc.getCustac());
			input.getChkqtn().setBrchno(otacct.getBrchno());// 交易机构号
			
			//币种校验
			if(!CommUtil.equals(input.getCrcycd(), otacct.getCrcycd())){
				throw DpModuleError.DpstComm.BNAS0632();
			}
			//检查转出机构与电子账户机构是否一致
			if(CommUtil.isNotNull(input.getOtbrch())){
				if(!CommUtil.equals(input.getOtbrch(), otacct.getBrchno())){
					throw DpModuleError.DpstComm.BNAS0043();
				}
			}
			//绑定卡验证
			if(accatp == E_ACCATP.FINANCE || accatp == E_ACCATP.WALLET){ //二三类只能绑定卡转出
				
				IoCaKnaCacd cacd = bind.selBindByCard(otacdc.getCustac(), input.getIncard(), E_DPACST.NORMAL, false);
				
				if(CommUtil.isNull(cacd)){
					throw DpModuleError.DpstComm.BNAS1366();
				}
				if(cacd.getIsbkca() == E_YES___.NO && cacd.getCardtp() == E_BACATP.DRCARD){
					throw DpModuleError.DpstComm.BNAS1934();
				}
			}
			
			//3.转出方可用余额校验
			BigDecimal realam = input.getTranam().add(input.getTlcgam()); //该金额用于校验可用余额是否满足交易金额和费用的总额
			if(accatp == E_ACCATP.WALLET){ //还信用卡允许使用三类户，需要校验三类户金额
				if(CommUtil.compare(otacct.getOnlnbl(), realam) < 0){
					throw DpModuleError.DpstComm.BNAS0442();
				}
			}else{
				
				//BigDecimal usebal = DpAcctProc.getProductBal(otacdc.getCustac(), input.getCrcycd(), false);
				
				// 可用余额 addby xiongzhao 20161223 
				BigDecimal usebal = SysUtil.getInstance(DpAcctSvcType.class)
						.getAcctaAvaBal(otacdc.getCustac(), otacct.getAcctno(),
								otacct.getCrcycd(), E_YES___.YES, E_YES___.NO);
				
				if(CommUtil.compare(usebal, realam) < 0){
					throw DpModuleError.DpstComm.BNAS0442();
				}
			}
			
			log.debug("<<==电子账户：[%s],电子账户类型：[%s],子账号：[%s],交易类型：[%s]==>>",input.getIncard(),accatp.getLongName(),otacct.getAcctno(),capitp.getLongName());
			
			property.setOtcsac(otacct.getCustac()); //电子账号ID
			property.setOtchld(otacct.getAcctno()); //子账号
//			CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);
			
			//设置收费参数
			if(CommUtil.compare(input.getTlcgam(), BigDecimal.ZERO) > 0){
				property.setIschrg(E_YES___.YES); //收费金额大于0才使用公共收费
				property.setChgflg(E_CHGFLG.ALL); //设置记账标志
			}
		}
		
		//电子账户转入
		if(ioflag == E_IOFLAG.IN){ 
			//转入电子账户不控制额度
			if (E_YES___.YES == input.getChkqtn().getIsckqt()) {
				throw DpModuleError.DpstComm.BNAS0802();
			}
			
			IoCaKnaAcdc inacdc = ActoacDao.selKnaAcdc(input.getIncard(), false);
			if(CommUtil.isNull(inacdc)){
				throw DpModuleError.DpstComm.BNAS0902();
			}
			
			if(inacdc.getStatus() == E_DPACST.CLOSE){
				throw DpModuleError.DpstComm.BNAS0441();
			}
			
			//String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//			CommTools.getBaseRunEnvs().setBusi_org_id(inacdc.getCorpno());
			
			//获取客户账户类型
			E_ACCATP accatp = cagen.qryAccatpByCustac(inacdc.getCustac());
			//1.转出方电子账户类型校验
			CapitalTransCheck.ChkAccttp(capitp, accatp); 
			//1.转出方电子账户状态校验
			E_CUACST cuacst = CapitalTransCheck.ChkAcctstIN(inacdc.getCustac()); 
			//2.转入方状态字检查
			CapitalTransCheck.ChkAcctFrozIN(inacdc.getCustac());
			//同名转入校验
			if(accatp == E_ACCATP.WALLET){
				if(capitp != E_CAPITP.IN104 && capitp != E_CAPITP.IN105 && capitp != E_CAPITP.IN108){
					if(!CommUtil.equals(input.getOtacna(), input.getInacna())){
						throw DpModuleError.DpstComm.BNAS1935();
					}
				}
			}
			
			
			KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
			//获取转入子账号
			if(accatp == E_ACCATP.GLOBAL || accatp == E_ACCATP.FINANCE){ //结算户
				tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(inacdc.getCustac(), E_ACSETP.SA);
				if(CommUtil.isNotNull(input.getIncstp()) && input.getIncstp() != E_ACSETP.SA){
					throw DpModuleError.DpstComm.BNAS0029();
				}
			}else{ // 钱包户
				tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(inacdc.getCustac(), E_ACSETP.MA);
				if(CommUtil.isNotNull(input.getIncstp()) && input.getIncstp() != E_ACSETP.MA){
					throw DpModuleError.DpstComm.BNAS0029();
				}
			}
			//币种校验
			if(!CommUtil.equals(input.getCrcycd(), tblKnaAcct.getCrcycd())){
				throw DpModuleError.DpstComm.BNAS0632();
			}
			//机构检查
			if(CommUtil.isNotNull(input.getInbrch())){
				if(!CommUtil.equals(input.getInbrch(), tblKnaAcct.getBrchno())){
					throw DpModuleError.DpstComm.BNAS0031();
				}
			}
			
			
//			CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);
			
			property.setIncsac(tblKnaAcct.getCustac()); //电子账号ID
			property.setInchld(tblKnaAcct.getAcctno()); //子账号
			property.setTblKnaAcct(tblKnaAcct);
			property.setCuacst(cuacst);
			property.setInactp(accatp);
		}
		//电子账户检查结束
		//4.设置属性值
		property.setIncorp("");
		property.setOtcorp("");
//		property.setBusisq(CommTools.getBaseRunEnvs().getBstrsq()); //业务跟踪编号 
		property.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); //交易渠道 
		//modify by wenbo 20170807  Mntrsq 改为 inpusq
		property.setServsq(CommTools.getBaseRunEnvs().getInitiator_seq()); //渠道来源流水  上送流水
		property.setServdt(CommTools.getBaseRunEnvs().getInitiator_date()); //渠道来源日期  上送日期
		property.setLinkno(null); //连笔号
		property.setPrcscd("tranfe"); //交易码
		property.setDscrtx(capitp.getLongName()); //描述
		property.setIoflag(ioflag);
		//出金
		property.setTrantp(E_TRANTP.TR); //交易类型 
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();	
		//add by sh 20170929 金谷项目，内部户记账机构为交易机构
		//String acbrch = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(brchno).getBrchno();
		//property.setAcbrch(acbrch); //省中心
		property.setAcbrch(brchno);
		
		if(capitp == E_CAPITP.IN120){//银联全渠道转电子账户
			property.setClactp(E_CLACTP._18);
		}else if(capitp == E_CAPITP.IN122){//通联代扣转电子账户
			property.setClactp(E_CLACTP._16);
		}else if(capitp == E_CAPITP.OT220){//电子账户银联全渠道转出
			property.setClactp(E_CLACTP._19);
		}else if(capitp == E_CAPITP.OT222){//电子账户通联代付转出
			property.setClactp(E_CLACTP._17);
		}else{
			throw DpModuleError.DpstAcct.BNAS0207();
		}
		
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", property.getClactp().getValue(), "%", true);
		
		property.setBusino(para.getParm_value1());//业务编码
		property.setSubsac(para.getParm_value2());//子户号
	}
	
	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranfe.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranfe.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranfe.Output output){
		//平衡性检查
		
		if(property.getIssucc() == E_YES___.YES){
			
		}else{
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),property.getClactp());
			
			output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
			output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
		}
		
	}

	/***
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void DealAcctStatAndSett( final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranfe.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranfe.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranfe.Output output){
		//修改账户状态，休眠转正常结息
		//转入账户的电子账户信息，转入账户的结算户信息或钱包户信息
		CapitalTransDeal.dealAcctStatAndSett(property.getCuacst(), property.getTblKnaAcct());
	}
	
	/**
	 * 涉案账号交易信息登记
	 * @Title: prcyInacRegister 
	 * @Description: 涉案账号交易信息登记
	 * @param input
	 * @param property
	 * @param output
	 * @author liaojincai
	 * @date 2016年8月2日 上午9:15:33 
	 * @version V2.3.0
	 */
	public static void prcyInacRegister( final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranfe.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranfe.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Tranfe.Output output){
		
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
					
					// 本行ATM无卡存款和本行ATM无卡取现
					if (E_CAPITP.IN106 == input.getCapitp() || E_CAPITP.OT204 == input.getCapitp()){
						cplKnbTrin.setTrantp(cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRANTP.CASH);// 交易类型
					} else {
						cplKnbTrin.setTrantp(cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRANTP.TRANSFER);// 交易类型
					}
					
					cplKnbTrin.setOtcard(input.getOtcard());// 转出账号
					cplKnbTrin.setOtacna(input.getOtacna());// 转出账号名称
					cplKnbTrin.setOtbrch(input.getOtbrch());// 转出账户机构
					cplKnbTrin.setIncard(input.getIncard());// 转入账号
					cplKnbTrin.setInacna(input.getInacna());// 转入账户名称
					cplKnbTrin.setInbrch(input.getInbrch());// 转入账户机构
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
}