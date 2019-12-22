package cn.sunline.ltts.busi.dp.client;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevGenBindCard;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCacd;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkOT;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.ChkCuad;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CNBSTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * @author zhangan
 * 资金类交易检查
 */
public class CapitalTransCheck {

	
	//电子账户转入状态校验
	private static BizLog log = BizLogUtil.getBizLog(CapitalTransCheck.class);
	
	
	/**
	 * @Title: chkTranfe 
	 * @Description: 资金交易类检查，检查账户分类、状态和状态字  
	 * @param chkIN
	 * @return
	 * @author zhangan
	 * @date 2017年1月9日 上午9:02:20 
	 * @version V2.3.0
	 */
	public static AcTranfeChkOT chkTranfe(AcTranfeChkIN chkIN){
		
		String cardno = chkIN.getCardno();
		String custac = chkIN.getCustac();
		
		String opcard = chkIN.getOpcard();
		String oppoac = chkIN.getOppoac(); //对方账号
		String custna = chkIN.getCustna();
		String oppona = chkIN.getOppona();
		
		E_CAPITP capitp = chkIN.getCapitp();
		String servtp = chkIN.getServtp(); //来源渠道
		E_ACCATP accatp = chkIN.getAccatp();
		
		E_CUACST cuacst = null;
		
		E_YES___ isbind = E_YES___.NO; //是否绑定卡标识
		E_YES___ facesg = E_YES___.NO; //面签标识
		E_YES___ opface = E_YES___.NO; //面签标识
		
		AcTranfeChkOT chkOT = SysUtil.getInstance(AcTranfeChkOT.class);
		
		E_YES___ isname = E_YES___.NO; //是否检查同名标识
		
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		para = KnpParameterDao.selectOne_odb1("TranCheck","%", "%", "%", false);
		if(CommUtil.isNotNull(para)){
			isname = CommUtil.toEnum(E_YES___.class, para.getParm_value1());
		}
		
		
		
		// 借记卡转入、贷记卡转入、银联在线转入、内部户转入 AS_NAS_DP_TRANIN ATM转账转入  银联CUPS转入 AS_NAS_DP_CUPSTR AS_NAS_DP_CUPSCF
		//柜面销户转电子账户 AS_NAS_ACC_CLACBT
		if(capitp == E_CAPITP.IN101 || capitp == E_CAPITP.IN102 || capitp == E_CAPITP.IN103 
				|| capitp == E_CAPITP.IN108 || capitp == E_CAPITP.CL701 || capitp == E_CAPITP.IN120
				|| capitp == E_CAPITP.IN122){
			
			//客户化状态检查是否允许转入
			cuacst = ChkAcctstIN(custac);
			//检查对方账号是否绑定账户
			IoCaKnaCacd cacd = SysUtil.getInstance(IoCaSevGenBindCard.class).selBindByCard(custac, opcard, E_DPACST.NORMAL, false);
			//获取面签属性
			ChkCuad cuad = ActoacDao.selKnaCuad(custac, true); 
			//账户类型检查
			if(accatp == E_ACCATP.FINANCE || accatp == E_ACCATP.WALLET){ //二类户、三类户
				if(CommUtil.isNull(cacd)){ //非绑定卡需要校验面签标志
					if(CommUtil.isNull(cuad.getFacesg()) || cuad.getFacesg() == E_YES___.NO){ //非绑定卡转入、且账户未面签，不允许转入
						throw DpModuleError.DpstComm.BNAS0878();
					}
				}
			}
			
			
			if(CommUtil.isNotNull(cuad.getFacesg()) && cuad.getFacesg() == E_YES___.YES){
				facesg = E_YES___.YES;
			}
			
			if(CommUtil.isNotNull(cacd)){
				isbind = E_YES___.YES;
			}
			
			
			
			if(isbind == E_YES___.YES && CommUtil.equals(servtp, "NM")){
				ChkAcctFrozIN_2(custac);
			}else{
				ChkAcctFrozIN(custac);
			}
			
		}else if(capitp == E_CAPITP.IN104 || capitp == E_CAPITP.IN105 || capitp == E_CAPITP.IN109 || capitp == E_CAPITP.IN111){
			
			//客户化状态检查是否允许转入，取消判断sunzy20190709
//			cuacst = ChkAcctstIN(custac);
			if(CommUtil.isNotNull(opcard)){
				//检查对方账号是否绑定账户
				IoCaKnaCacd cacd = SysUtil.getInstance(IoCaSevGenBindCard.class).selBindByCard(custac, opcard, E_DPACST.NORMAL, false);
				//获取面签属性
				ChkCuad cuad = ActoacDao.selKnaCuad(custac, true); 
				//账户类型检查
				if(accatp == E_ACCATP.FINANCE || accatp == E_ACCATP.WALLET){ //二类户、三类户
					if(CommUtil.isNull(cacd)){ //非绑定卡需要校验面签标志
						if(CommUtil.isNull(cuad.getFacesg()) || cuad.getFacesg() == E_YES___.NO){ //非绑定卡转入、且账户未面签，不允许转入
							throw DpModuleError.DpstComm.BNAS0878();
						}
					}
				}
				
				
				if(CommUtil.isNotNull(cuad.getFacesg()) && cuad.getFacesg() == E_YES___.YES){
					facesg = E_YES___.YES;
				}
				
				if(CommUtil.isNotNull(cacd)){
					isbind = E_YES___.YES;
				}
			
			}
			
			ChkAcctFrozIN(custac);
			
		}
		//转借记卡、还信用卡、银联在线转出、还贷款 AS_NAS_DP_TRANOT
		else if(capitp == E_CAPITP.OT201 || capitp == E_CAPITP.OT202 || capitp == E_CAPITP.OT203 || capitp == E_CAPITP.OT205){
			//客户化状态检查，取消判断sunzy20190709
//			cuacst = ChkAcctstOT(custac);
			//状态字检查
			ChkAcctstWord(custac);
			//检查对方账号是否绑定账户
			IoCaKnaCacd cacd = SysUtil.getInstance(IoCaSevGenBindCard.class).selBindByCard(custac, opcard, E_DPACST.NORMAL, false);
			//获取面签属性
			ChkCuad cuad = ActoacDao.selKnaCuad(custac, true); 
			
			if(CommUtil.isNotNull(cuad.getFacesg()) && cuad.getFacesg() == E_YES___.YES){
				facesg = E_YES___.YES;
			}
			
			if(CommUtil.isNotNull(cacd)){
				isbind = E_YES___.YES;
			}
			
			if(isname == E_YES___.YES && accatp == E_ACCATP.WALLET && !CommUtil.equals(custna, oppona)) {
				throw DpModuleError.DpstComm.BNAS0958();
			}
		}
		//ATM存现 AS_NAS_DP_ATMDIN
		else if(capitp == E_CAPITP.IN106){
			if(accatp == E_ACCATP.WALLET){
				throw DpModuleError.DpstComm.BNAS0957();
			}
			
			//获取面签属性
			ChkCuad cuad = ActoacDao.selKnaCuad(custac, true);
			if(CommUtil.isNotNull(cuad.getFacesg()) && cuad.getFacesg() == E_YES___.YES){
				facesg = E_YES___.YES;
			}
			//客户化状态检查是否允许转入，取消判断sunzy20190709
//			cuacst = ChkAcctstIN(custac);
			
			ChkAcctFrozIN(custac);
		}
		//ATM取现 AS_NAS_DP_ATMDOT
		else if(capitp == E_CAPITP.OT204){
			/**
			 * by liuz
			 */
//			if(accatp == E_ACCATP.WALLET){
//				throw DpModuleError.DpstComm.BNAS0957();
//			}
			//检查对方账号是否绑定账户
			IoCaKnaCacd cacd = SysUtil.getInstance(IoCaSevGenBindCard.class).selBindByCard(custac, opcard, E_DPACST.NORMAL, false);
			if(CommUtil.isNotNull(cacd)){
				isbind = E_YES___.YES;
			}
			
			//获取面签属性
			ChkCuad cuad = ActoacDao.selKnaCuad(custac, true);
			if(CommUtil.isNotNull(cuad.getFacesg()) && cuad.getFacesg() == E_YES___.YES){
				facesg = E_YES___.YES;
			}
			//客户化状态检查，取消判断sunzy20190709
//			cuacst = ChkAcctstOT(custac);
			//状态字检查
			ChkAcctstWord(custac);
		}
		//电子账户转电子账户
		else if(capitp == E_CAPITP.NT301){
			//对方电子账户的分类
			E_ACCATP opactp = chkIN.getOpactp();
			E_CUACST opacst = null;
			
			//===========转出方电子账户检查==============
			//客户化状态检查，取消判断sunzy20190709
//			cuacst = ChkAcctstOT(custac);
			//账户类型检查
			
			//状态字检查
			ChkAcctstWord(custac);
			//获取面签属性
			ChkCuad cuad1 = ActoacDao.selKnaCuad(custac, true);
			if(CommUtil.isNull(cuad1.getFacesg()) || cuad1.getFacesg() == E_YES___.NO){
				facesg = E_YES___.NO;
			}else{
				facesg = E_YES___.YES;
			}
			
			
			//==========转入方电子账户检查===============
			
			//客户化状态检查是否允许转入
			opacst = ChkAcctstIN(oppoac);
			//检查对方账号是否绑定账户
			IoCaKnaCacd cacd = SysUtil.getInstance(IoCaSevGenBindCard.class).selBindByCard(oppoac, cardno, E_DPACST.NORMAL, false);
			//获取面签属性
			ChkCuad cuad = ActoacDao.selKnaCuad(oppoac, true); 
			//账户类型检查
			if(opactp == E_ACCATP.FINANCE || opactp == E_ACCATP.WALLET){ //二类户、三类户
				if(CommUtil.isNull(cacd)){ //非绑定卡需要校验面签标志
					if(CommUtil.isNull(cuad.getFacesg()) || cuad.getFacesg() == E_YES___.NO){ //非绑定卡转入、且账户未面签，不允许转入
						throw DpModuleError.DpstComm.BNAS0878();
					}
				}
			}
			
			if(CommUtil.isNotNull(cuad.getFacesg()) && cuad.getFacesg() == E_YES___.YES){
				opface = E_YES___.YES;
			}
			
			if(CommUtil.isNotNull(cacd)){
				isbind = E_YES___.YES;
			}
			
			if(isbind == E_YES___.YES && CommUtil.equals(servtp, "NM")){
				ChkAcctFrozIN_2(oppoac);
			}else{
				ChkAcctFrozIN(oppoac);
			}
			
			
			chkOT.setOpacst(opacst);
			chkOT.setOpactp(opactp);
		}
		//大小额来账 AS_NAS_DP_LSAMIN
		else if(capitp == E_CAPITP.IN107){
			String busitp = chkIN.getDime01();
			/*if(CommUtil.equals(busitp, E_CNBSTP.A105.getValue())){ //退汇
//				cuacst = CapitalTransCheck.ChkCnapotAcctstIN(custac); //1.转出方电子账户状态校验 退汇不用检查预销户状态，取消判断sunzy20190709
			}else{
//				cuacst = CapitalTransCheck.ChkAcctstIN(custac); ，取消判断sunzy20190709
			}*/
			
			//检查对方账号是否绑定账户
			IoCaKnaCacd cacd = SysUtil.getInstance(IoCaSevGenBindCard.class).selBindByCard(custac, opcard, E_DPACST.NORMAL, false);
			//获取面签属性
			ChkCuad cuad = ActoacDao.selKnaCuad(custac, true); 
			if(accatp == E_ACCATP.FINANCE || accatp == E_ACCATP.WALLET){ //二类户、三类户
				if(CommUtil.isNull(cacd)){ //非绑定卡需要校验面签标志
					if(CommUtil.isNull(cuad.getFacesg()) || cuad.getFacesg() == E_YES___.NO){ //非绑定卡转入、且账户未面签，不允许转入
						throw DpModuleError.DpstComm.BNAS0878();
					}
				}
			}
			
			if(CommUtil.isNotNull(cuad.getFacesg()) && cuad.getFacesg() == E_YES___.YES){
				facesg = E_YES___.YES;
			}
			
			if(CommUtil.isNotNull(cacd)){
				isbind = E_YES___.YES;
			}
			//状态字检查,从客户端主动发起的 绑定卡转入需要控制账户保护状态不允许转入
			if(isbind == E_YES___.YES && CommUtil.equals(servtp, "NM")){
				ChkAcctFrozIN_2(custac);
			}else{
				ChkAcctFrozIN(custac);
			}
			
			
		}
		//大小额往账 AS_NAS_DP_LSAMOT
		else if(capitp == E_CAPITP.OT206){
			//客户化状态检查，取消判断sunzy20190709
//			cuacst = ChkAcctstOT(custac);
			//状态字检查
			ChkAcctstWord(custac);
			//检查对方账号是否绑定账户
			IoCaKnaCacd cacd = SysUtil.getInstance(IoCaSevGenBindCard.class).selBindByCard(custac, opcard, E_DPACST.NORMAL, false);
			//获取面签属性
			ChkCuad cuad = ActoacDao.selKnaCuad(custac, true); 
			
			if(CommUtil.isNotNull(cuad.getFacesg()) && cuad.getFacesg() == E_YES___.YES){
				facesg = E_YES___.YES;
			}
			
			if(CommUtil.isNotNull(cacd)){
				isbind = E_YES___.YES;
			}
			
			if(isname == E_YES___.YES && accatp == E_ACCATP.WALLET && !CommUtil.equals(custna, oppona)) {
				throw DpModuleError.DpstComm.BNAS0958();
			}
		}
		//银联传统CUPS贷记，消费撤销和退货   CUPSIN CUPSCX
		else if(capitp == E_CAPITP.IN129 || capitp == E_CAPITP.IN130){
			//客户化状态检查 入，取消判断sunzy20190709
//			cuacst = ChkAcctstIN(custac);
			//状态字检查 入
			ChkAcctFrozIN(custac);
			//检查对方账号是否绑定账户
			IoCaKnaCacd cacd = SysUtil.getInstance(IoCaSevGenBindCard.class).selBindByCard(custac, opcard, E_DPACST.NORMAL, false);
			//获取面签属性-消费类不需要进行面签检查
			ChkCuad cuad = ActoacDao.selKnaCuad(custac, true); 
			
			if(CommUtil.isNotNull(cuad.getFacesg()) && cuad.getFacesg() == E_YES___.YES){
				facesg = E_YES___.YES;
			}
			
			if(CommUtil.isNotNull(cacd)){
				isbind = E_YES___.YES;
			}
		}
		//银联传统CUPS消费 CUPSOT
		else if(capitp == E_CAPITP.OT228 || capitp == E_CAPITP.OT208 || capitp == E_CAPITP.OT229){
			//客户化状态检查，取消判断sunzy20190709
//			cuacst = ChkAcctstOT(custac);
			//状态字检查
			ChkAcctstWord(custac);
			//检查对方账号是否绑定账户
			IoCaKnaCacd cacd = SysUtil.getInstance(IoCaSevGenBindCard.class).selBindByCard(custac, opcard, E_DPACST.NORMAL, false);
			//获取面签属性
			ChkCuad cuad = ActoacDao.selKnaCuad(custac, true); 
			
			if(CommUtil.isNotNull(cuad.getFacesg()) && cuad.getFacesg() == E_YES___.YES){
				facesg = E_YES___.YES;
			}
			
			if(CommUtil.isNotNull(cacd)){
				isbind = E_YES___.YES;
			}
		}
		
		chkOT.setAccatp(accatp);
//		chkOT.setCuacst(cuacst);
		chkOT.setIsbind(isbind);
		
		chkOT.setFacesg(facesg);
		chkOT.setOpface(opface);
		
		return chkOT;
	}
	

	/***
	 * 检查电子账户是否允许转出
	 * @param custac 电子账号
	 * 
	 */
	public static E_CUACST ChkAcctstOT(String custac){
		
		// 检查电子账户客户化状态
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		if (CommUtil.isNotNull(cuacst)) {
			
			if (cuacst == E_CUACST.PRECLOS) { // 预销户
				throw DpModuleError.DpstComm.BNAS0880();
				
			} else if (cuacst == E_CUACST.CLOSED) { // 销户
				throw DpModuleError.DpstComm.BNAS0883();
				
			} else if (cuacst == E_CUACST.PREOPEN) { // 预开户
				throw DpModuleError.DpstComm.BNAS0881();
				
			} else if (cuacst == E_CUACST.NOACTIVE) { // 未激活
				throw DpModuleError.DpstComm.BNAS0885();
				
			} else if (cuacst == E_CUACST.NOENABLE) { // 未启用
				throw DpModuleError.DpstComm.BNAS0884();
				
			} else if (cuacst == E_CUACST.DORMANT) { // 休眠
				throw DpModuleError.DpstComm.BNAS0882();
				
			} else if (cuacst == E_CUACST.OUTAGE) { // 停用
				throw DpModuleError.DpstComm.BNAS0886();
				
			}

		} else {
			throw DpModuleError.DpstComm.BNAS1206();
		}
		
		return cuacst;
	}
	/***
	 * add by jizhirong 北京poc版本
	 * 检查电子账户是否允许转出
	 * @param custac 电子账号
	 * 
	 */
	public static E_CUACST pocChkAcctstOT(String custac){
		
		// 检查电子账户客户化状态
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		return cuacst;
	}
	
	/***
	 * 检查电子账户是否允许退货
	 * @param custac 电子账号
	 * 
	 */
	public static E_CUACST ChkAcctstRe(String custac){
		
		// 检查电子账户客户化状态
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		if (CommUtil.isNotNull(cuacst)) {
			
			if (cuacst == E_CUACST.PRECLOS) { // 预销户
				throw DpModuleError.DpstComm.BNAS0880();

			} else if (cuacst == E_CUACST.CLOSED) { // 销户
				throw DpModuleError.DpstComm.BNAS0883();

			} else if (cuacst == E_CUACST.PREOPEN) { // 预开户
				throw DpModuleError.DpstComm.BNAS0881();

			}
			
		}
		
		return cuacst;
	}
	
	/***
	 * 检查电子账户是否允许退货
	 * @param custac 电子账号
	 * add by jizhirong    北京poc
	 */
	public static String pocChkAcctstRe(String custac){
		String msg = "";
		// 检查电子账户客户化状态
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		if (CommUtil.isNotNull(cuacst)) {
			if (cuacst == E_CUACST.PRECLOS) { // 预销户
				//throw DpModuleError.DpstComm.BNAS0880();
				msg = "预销户";
			} else if (cuacst == E_CUACST.CLOSED) { // 销户
				//throw DpModuleError.DpstComm.BNAS0883();
				msg = "销户";
			} else if (cuacst == E_CUACST.PREOPEN) { // 预开户
				//throw DpModuleError.DpstComm.BNAS0881();
				msg = "预开户";
			}
		}
		return msg;
	}
	/**
	 * @Title: ChkAcctstIN 
	 * @Description:  转账转入，电子账户客户号状态校验
	 * @param custac
	 * @version V2.3.0
	 */
	public static E_CUACST ChkAcctstIN(String custac){
		
		
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		if (CommUtil.isNotNull(cuacst)) {
			
			if (cuacst == E_CUACST.PRECLOS) { // 预销户
				throw DpModuleError.DpstComm.BNAS0880();

			} else if (cuacst == E_CUACST.CLOSED) { // 销户
				throw DpModuleError.DpstComm.BNAS0883();

			} else if (cuacst == E_CUACST.PREOPEN) { // 预开户
				throw DpModuleError.DpstComm.BNAS0881();

			} else if (cuacst == E_CUACST.OUTAGE) { // 停用
				// throw DpModuleError.DpstComm.E9999("电子账户为停用");

			} 

		} else {
			throw DpModuleError.DpstComm.BNAS1206();
		}
	
		return cuacst;
	}
	/**
	 * @Title: ChkCnapotAcctstIN 
	 * @Description:  大小额交易来账，电子账户客户号状态校验
	 * @param custac
	 * @version V2.3.0
	 */
	public static E_CUACST ChkCnapotAcctstIN(String custac){
		
		
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		if (CommUtil.isNotNull(cuacst)) {
			
			if (cuacst == E_CUACST.PRECLOS) { // 预销户
				//throw DpModuleError.DpstComm.BNAS0880("电子账户为预销户");
				log.debug("电子账号：[%s]，客户化状态：[%s]，转入异常退汇", custac, cuacst.getLongName());
			} else if (cuacst == E_CUACST.CLOSED) { // 销户
				throw DpModuleError.DpstComm.BNAS0883();

			} else if (cuacst == E_CUACST.PREOPEN) { // 预开户
				throw DpModuleError.DpstComm.BNAS0881();

			} else if (cuacst == E_CUACST.OUTAGE) { // 停用
				// throw DpModuleError.DpstComm.E9999("电子账户为停用");

			} 

		} else {
			throw DpModuleError.DpstComm.BNAS1206();
		}
	
		return cuacst;
	}
	/**
	 * @Title: ChkAcctstWord 
	 * @Description:  转账转出，状态字校验
	 * @param custac
	 * @version V2.3.0
	 */
	public static void ChkAcctstWord(String custac) {
		// 电子账户状态字检查
		IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
		if (E_YES___.YES == cplAcStatus.getBrfroz()) {
			throw CaError.Eacct.BNAS0866();

		} else if (E_YES___.YES == cplAcStatus.getDbfroz()) {
			throw DpModuleError.DpstComm.BNAS0859();

		} else if (E_YES___.YES == cplAcStatus.getBkalsp()) {
			throw DpModuleError.DpstComm.BNAS0860();

		} else if (E_YES___.YES == cplAcStatus.getOtalsp()) {
			throw CaError.Eacct.BNAS0862();

		} else if (E_YES___.YES == cplAcStatus.getClstop()) {
			throw DpModuleError.DpstComm.BNAS0870();

		}
	}
	
	/**
	 * @Title: ChkAcctstWord 
	 * @Description:  转账转出，状态字校验
	 * @param custac
	 * @version V2.3.0
	 * add by jizhrong 北京poc
	 */
	public static String pocChkAcctstWord(String custac) {
		String msg = "";
		// 电子账户状态字检查
		IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
		if (E_YES___.YES == cplAcStatus.getBrfroz()) {
			msg = "借冻";

		} else if (E_YES___.YES == cplAcStatus.getDbfroz()) {
			msg = "双冻";
		} else if (E_YES___.YES == cplAcStatus.getBkalsp()) {
			msg = "已全止(银行止付)";
		} else if (E_YES___.YES == cplAcStatus.getOtalsp()) {
			msg = "已全止(外部止付)";
		} else if (E_YES___.YES == cplAcStatus.getClstop()) {
			msg = "已被保护";
		}
		return msg;
	}
	/**
	 * 检查状态字是否允许退货
	 * 
	 */
	public static void ChkAcctstRet(String custac) {
		// 电子账户状态字检查
		IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
		
		if (E_YES___.YES == cplAcStatus.getDbfroz()) {
			throw DpModuleError.DpstComm.BNAS0859();

		} else if (E_YES___.YES == cplAcStatus.getBkalsp()) {
			throw DpModuleError.DpstComm.BNAS0860();

		} else if (E_YES___.YES == cplAcStatus.getOtalsp()) {
			throw CaError.Eacct.BNAS0862();

		} else if (E_YES___.YES == cplAcStatus.getClstop()) {
			throw DpModuleError.DpstComm.BNAS0864();

		}
	}
	
	
	public static void ChkAccttp(E_CAPITP capitp,E_ACCATP accatp){
		if(capitp == E_CAPITP.IN105){ //ATM转入
			if(accatp == E_ACCATP.WALLET){
				throw DpModuleError.DpstComm.BNAS1585();
			}
		}else if(capitp == E_CAPITP.IN106){ //ATM存款
			if(accatp != E_ACCATP.GLOBAL){
				throw DpModuleError.DpstComm.BNAS1585();
			}
		}else if(capitp == E_CAPITP.OT204){ //ATM取款
			if(accatp != E_ACCATP.GLOBAL){
				throw DpModuleError.DpstComm.BNAS1586();
			}
		}else if(capitp == E_CAPITP.OT205){
			if(accatp != E_ACCATP.GLOBAL){
				throw DpModuleError.DpstComm.BNAS1587();
			}
		}else if(capitp == E_CAPITP.OT201 ||  
				capitp  == E_CAPITP.OT203 || capitp  == E_CAPITP.OT206){
			if(accatp == E_ACCATP.WALLET){
				throw DpModuleError.DpstComm.BNAS0743();
			}
		}
	}
	
	
	//转出账户状态字检查
	public static void ChkAcctFrozOT(String custac){
		ChkAcctstWord(custac);
	}
	//转入账户状态字检查
	public static void ChkAcctFrozIN(String custac){
		
		IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
		if (E_YES___.YES == cplAcStatus.getDbfroz()) {
			throw DpModuleError.DpstComm.BNAS0859();

		} 
	}
	//转入账户状态字检查  add by jizhriong   北京poc
	public static void pocChkAcctFrozIN(String custac){
		
		IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
		if (E_YES___.YES == cplAcStatus.getDbfroz()) {
			//throw DpModuleError.DpstComm.BNAS0859();
			log.info("电子账户已双冻！");
			return ;
		} 
	}
	
	//大小额隔日冲正状态字检查
	public static void ChkAcctFrozCnapre(String custac){
		
		IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
	
		if (E_YES___.YES == cplAcStatus.getBrfroz()) {
			throw CaError.Eacct.BNAS0866();

		} else if (E_YES___.YES == cplAcStatus.getDbfroz()) {
			throw DpModuleError.DpstComm.BNAS0859();
			
		} else if (E_YES___.YES == cplAcStatus.getOtalsp()) {
			throw CaError.Eacct.BNAS0862();
		}

	}
	//转入账户状态字检查账户保护状态字
	public static void ChkAcctFrozIN_2(String custac){
		
		IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
		if (E_YES___.YES == cplAcStatus.getDbfroz()) {
			throw DpModuleError.DpstComm.BNAS0859();

		} 
//		if (E_YES___.YES == cplAcStatus.getClstop()){
//			throw DpModuleError.DpstComm.E9902("电子账户已被保护");
//		}
	}
	
	/***
	 * 电子账户支出冻结状态检查
	 */
	public static void CkhFrozstOT(IoCaKnaAcdc acdc){
		
		ChkAcctstWord(acdc.getCustac());
	}

	/*
	 * 电子账户 柜面销户 状态字检查
	 */
	public static void ChkFrozstClacbt(String custac){
		IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
		if (E_YES___.YES == cplAcStatus.getBrfroz()) {
			throw CaError.Eacct.BNAS0866();

		} else if (E_YES___.YES == cplAcStatus.getDbfroz()) {
			throw DpModuleError.DpstComm.BNAS0859();

		} else if (E_YES___.YES == cplAcStatus.getBkalsp()) {
			throw DpModuleError.DpstComm.BNAS0860();

		} else if (E_YES___.YES == cplAcStatus.getOtalsp()) {
			throw CaError.Eacct.BNAS0862();

		} else if (E_YES___.YES == cplAcStatus.getClstop()) {
			throw DpModuleError.DpstComm.BNAS0870();

		} else if (cplAcStatus.getPtfroz() == E_YES___.YES){
			throw CaError.Eacct.BNAS0869();
			
		} else if (cplAcStatus.getPledge() == E_YES___.YES){
			throw DpModuleError.DpstComm.BNAS0855();
			
		} else if (cplAcStatus.getCertdp() == E_YES___.YES){
			throw DpModuleError.DpstComm.BNAS0865();
			
		}
	}
}
