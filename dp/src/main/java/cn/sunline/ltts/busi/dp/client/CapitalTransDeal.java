package cn.sunline.ltts.busi.dp.client;


import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCuad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCuadDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpStrikeSqlDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.type.DpAcctType.ChkQtIN;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbSmsSvcType;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.cf.IoCuTable.IoCifCustAccs;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbSlep;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkOT;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.CupsTranfe;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubSqrd;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_APPTTP;
import cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_CLIKTP;
import cn.sunline.ltts.busi.sys.type.ApSmsType.ToAppSendMsg;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STATUS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SLEPST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SPPRST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOTYPE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class CapitalTransDeal {

	private static BizLog log = BizLogUtil.getBizLog(CapitalTransDeal.class);
	
	
	/**
	 * @Title: getSettKnaAcctAc 
	 * @Description:不带锁获取具有结算功能的子账户信息  
	 * @param custac 电子账户ID
	 */
	public static KnaAcct getSettKnaAcctAc(String custac){
				
		return getAcct(custac, E_YES___.NO, null);
	}
	/**
	 * @Title: getSettKnaAcctAcLock
	 * @Description:带锁获取具有结算功能的子账户信息  
	 * @param custac 电子账户ID
	 */
	public static KnaAcct getSettKnaAcctAcLock(String custac){
		
		return getAcct(custac, E_YES___.YES, null);
	}
	/**
	 * @Title: getSettKnaAcctSubAcLock 
	 * @Description:	带锁查询指定类型的结算子账户
	 * @param custac	电子账号
	 * @param acsetp	子账号类型
	 */
	public static KnaAcct getSettKnaAcctSubAcLock(String custac, E_ACSETP acsetp){
		
		return getAcct(custac, E_YES___.YES, acsetp);
	}
	/**
	 * @Title: getIOSettKnaAcct 
	 * @Description:(iobus服务)获取具有结算功能的子账户信息  
	 * @param custac 电子账户ID
	 */
	public static IoDpKnaAcct getIOSettKnaAcct(String custac){
		
		KnaAcct KnaAcct = getAcct(custac, E_YES___.NO, null);
		IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(IoDpKnaAcct.class);
		CommUtil.copyProperties(cplKnaAcct , KnaAcct);
		return cplKnaAcct;
	}
	/**
	 * @Title: getIOSettKnaAcctSub 
	 * @Description:	(iobus服务)不带锁查询指定类型的结算子账户
	 * @param custac	电子账号
	 * @param acsetp	子账号类型
	 */
	public static IoDpKnaAcct getIOSettKnaAcctSub(String custac,E_ACSETP acsetp){
		
		KnaAcct KnaAcct = getSettKnaAcctSub(custac,acsetp);
		IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(IoDpKnaAcct.class);
		CommUtil.copyProperties(cplKnaAcct , KnaAcct);
		return cplKnaAcct;
	}
	/**
	 * @Title: getSettKnaAcctSub 
	 * @Description:	不带锁查询指定类型的结算子账户
	 * @param custac	电子账号
	 * @param acsetp	子账号类型
	 */
	public static KnaAcct getSettKnaAcctSub(String custac, E_ACSETP acsetp){
		
		return getAcct(custac, null, acsetp);
	}
	/**
	 * @Title: getAcct 
	 * @Description:	查询不等于销户状态的结算子账户
	 * @param custac	电子账号
	 * @param islock	是否加锁
	 * @param acsetp	子账号类型
	 */
	private static KnaAcct getAcct(String custac, E_YES___ islock, E_ACSETP acsetp){
		KnaAcct KnaAcct = SysUtil.getInstance(KnaAcct.class);
		if(CommUtil.isNotNull(acsetp)){
			if(islock == E_YES___.YES){
				KnaAcct = ActoacDao.selKnaAcctAcWithLock(acsetp, custac, E_DPACST.CLOSE, false);
			}else{
				KnaAcct = ActoacDao.selKnaAcctAc(acsetp, custac, E_DPACST.CLOSE, false);
			}
		}else{
			E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac);
			if(islock == E_YES___.YES){
				if (eAccatp == E_ACCATP.GLOBAL || eAccatp == E_ACCATP.FINANCE) {
					KnaAcct = ActoacDao.selKnaAcctAcWithLock(E_ACSETP.SA, custac, E_DPACST.CLOSE, false);
				} else if (eAccatp == E_ACCATP.WALLET) {
					KnaAcct = ActoacDao.selKnaAcctAcWithLock(E_ACSETP.MA, custac, E_DPACST.CLOSE, false);
				}
			}else{
				if (eAccatp == E_ACCATP.GLOBAL || eAccatp == E_ACCATP.FINANCE) {
					KnaAcct = ActoacDao.selKnaAcctAc(E_ACSETP.SA, custac, E_DPACST.CLOSE, false);
				} else if (eAccatp == E_ACCATP.WALLET) {
					KnaAcct = ActoacDao.selKnaAcctAc(E_ACSETP.MA, custac, E_DPACST.CLOSE, false);
				}
			}
			
		}
		
		if(CommUtil.isNull(KnaAcct) && CommUtil.isNull(KnaAcct.getAcctno())){
			throw DpModuleError.DpstComm.BNAS1597();
		}
		
		return KnaAcct;
	}
	
	/**
	 * @param cuacst 转入账户的电子客户化状态
	 * @param inacct 转入账户的结算账户信息或钱包账户信息
	 */
	public static void dealAcctStatAndSett(E_CUACST cuacst, KnaAcct inacct){
		
		//E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(inacct.getCustac());
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String trantm = BusiTools.getBusiRunEnvs().getTrantm();
		String tmstmp =DateTools2.getCurrentTimestamp();
		IoCaKnbSlep tblknbslep = SysUtil.getInstance(IoCaKnbSlep.class);
		if(cuacst == E_CUACST.DORMANT){ //休眠
			log.debug("<<==休眠户转正常，电子账号ID：[%s],负债子账号：[%s],结息==>>",inacct.getCustac(), inacct.getAcctno());
			if(CommUtil.isNull(inacct.getCustac()) || CommUtil.isNull(inacct.getAcctno())){
				throw DpModuleError.DpstComm.BNAS0934();
			}
			
			tblknbslep = DpAcctQryDao.selknbslepinfo(inacct.getCustac(), E_SLEPST.SLEP, false);
			if(CommUtil.isNull(tblknbslep)){
				throw CaError.Eacct.BNAS0923();
			}
			
			
			/**
			IoDpCloseIN clsin = SysUtil.getInstance(IoDpCloseIN.class);
			BigDecimal interest = DpCloseAcctno.prcCurrInterest(inacct, clsin);
			
			if(CommUtil.compare(interest, BigDecimal.ZERO) > 0){
				
				SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class); //电子账户存入记账复合类型
				
				saveIn.setAcctno(inacct.getAcctno()); //结算账户、钱包账户
				saveIn.setBankcd("");
				saveIn.setBankna("");
				saveIn.setCardno(inacct.getCustac());
				saveIn.setCrcycd(inacct.getCrcycd());
				saveIn.setCustac(inacct.getCustac());
				saveIn.setOpacna(inacct.getAcctna());
				saveIn.setOpbrch(inacct.getBrchno());
				saveIn.setRemark("休眠户转正常结息");
				saveIn.setSmrycd(E_SMRYCD.TR);
				saveIn.setToacct(inacct.getAcctno());
				saveIn.setTranam(interest);
				saveIn.setIschck(E_YES___.NO);
				CommTools.getRemoteInstance(DpAcctSvcType.class).addPostAcctDp(saveIn);
			}*/
			
			
			//mdy by zhanga 改为服务调用，需要使用冲正注册
		/*	ActoacDao.updKnbSlepStat(E_SPPRST.NORMAL, trandt, trantm, E_SLEPST.CANCEL, inacct.getCustac(), E_SLEPST.SLEP,trandt,tmstmp);
			
			//休眠登记簿修改登记冲正事件
			IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
			cplInput.setCustac(inacct.getCustac());
			cplInput.setEvent1(trandt);
			cplInput.setEvent2(trantm);
			cplInput.setEvent3(E_SLEPST.SLEP.getValue());
			cplInput.setTranev(ApUtil.TRANS_EVENT_SLEP);
			ApStrike.regBook(cplInput);
			*/
			
			CommTools.getRemoteInstance(DpAcctSvcType.class).updKnbSlepStat(E_SPPRST.NORMAL, trandt, trantm, E_SLEPST.CANCEL, inacct.getCustac(), E_SLEPST.SLEP,trandt,tmstmp);
			//add by jizhirong  修改电子账户状态
			KnaCuad knaCuad = KnaCuadDao.selectOne_knaCuadOdx1(inacct.getCustac(), true);
			knaCuad.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());
			knaCuad.setDime01("3");
			KnaCuadDao.updateOne_knaCuadOdx1(knaCuad);
		}
		
		if(cuacst == E_CUACST.DORMANT || cuacst == E_CUACST.NOACTIVE){
			
			log.debug("<<==非正常修改账户状态，电子账号ID：[%s],负债子账号：[%s],原状态：[%s]==>>",inacct.getCustac(), inacct.getAcctno(),cuacst.getLongName());
			
			
			ActoacDao.updKnaCustToNormal(E_ACCTST.NORMAL, inacct.getCustac(),tmstmp);
			ActoacDao.updKnaAcctToNormal(E_DPACST.NORMAL, inacct.getCustac(), E_DPACST.SLEEP,tmstmp);
			
			IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
			cplDimeInfo.setCustac(inacct.getCustac());
			
			if(cuacst == E_CUACST.NOACTIVE){
				cplDimeInfo.setDime01(cuacst.getValue()); //维度1 更新前状态
			}else{
				cplDimeInfo.setDime01(cuacst.getValue());
				cplDimeInfo.setDime02(tblknbslep.getCuacst().getValue()); //维度1 更新前状态
			}
			
			/**
			 *  -- mdy by zhanga --
			 * 与行方叶群讨论，休眠转入状态变更正常后，冲正时不需要冲回原休眠状态
			 * 将调用方式改为非remote调用，不登记服务流水
			 * IoCaUpdAcctstOut acstOT = CommTools.getRemoteInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);
			 */
			cplDimeInfo.setIsstrk(E_YES___.NO); //不需要冲正注册
			//IoCaUpdAcctstOut acstOT = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);
			
			//20161207 add by songlw 短信流水登记
			IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
			//cplKubSqrd.setAppsid(CommTools.getBaseRunEnvs().getAppsid());// app推送ID
			cplKubSqrd.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());// 内部交易码
			cplKubSqrd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
			cplKubSqrd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());// 交易时间
			cplKubSqrd.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
			cplKubSqrd.setTmstmp(DateTools2.getCurrentTimestamp());// 时间戳
			cplKubSqrd.setPmvl01("xmzzc"); //参数1
			// 调用短信流水登记服务
			SysUtil.getInstance(IoPbSmsSvcType.class).pbTransqReg(cplKubSqrd);
			
			
			IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			IoCaKnaCust cust = caqry.getKnaCustByCustacOdb1(inacct.getCustac(), true);
			//取消客户信息相关内容
			//			IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(IoSrvCfPerson.class).getCifCustAccsByCustno(cust.getCustno(), E_STATUS.NORMAL, true);
			//消息推送至APP客户端
			//MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
			//mri.setMtopic("Q0101005");
			//mri.setTdcnno("R00");  //测试指定DCN
			ToAppSendMsg toAppSendMsg = SysUtil.getInstance(ToAppSendMsg.class);
			
			// 消息内容
			toAppSendMsg.setUserId(""); //用户ID  暂时设置为空
			toAppSendMsg.setOutNoticeId("Q0101005"); //外部消息ID
			toAppSendMsg.setNoticeTitle("您的电子账户已恢复正常使用"); //公告标题
//			toAppSendMsg.setContent("您的ThreeBank电子账户已解除休眠。 感谢您对金谷农商银行的支持！"); //公告内容
			toAppSendMsg.setSendtime(CommTools.getBaseRunEnvs().getTrxn_date()+BusiTools.getBusiRunEnvs().getTrantm()); //消息生成时间
			toAppSendMsg.setTransType(E_APPTTP.CUACCH); //交易类型
			toAppSendMsg.setTirggerSys(SysUtil.getSystemId()); //触发系统
			toAppSendMsg.setClickType(E_CLIKTP.NO);   //点击动作类型
			//toAppSendMsg.setClickValue(clickValue); //点击动作值
			
			//mri.setMsgtyp("ApSmsType.ToAppSendMsg");
			//mri.setMsgobj(toAppSendMsg); 
			//AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
			CommTools.addMessagessToContext("Q0101005", "ApSmsType.PbinusSendMsg");
		}
	}
	/**
	 * @param custac 电子账号ID
	 * @param trandt 原修改日期
	 * @param trantm 原修改时间
	 * @param slepst 原状态
	 */
	public static void strikeKnbSlep(String custac, String trandt, String trantm, E_SLEPST slepst){
		
		String transq = DpStrikeSqlDao.selKnbSlepOdByDate(trandt, trantm, custac, false);
		if(CommUtil.isNotNull(transq)){
			//ActoacDao.updKnbSlepStat(null, null, null, E_SLEPST.SLEP, custac, E_SLEPST.CANCEL);
			DpStrikeSqlDao.strikeKnbSlep(null, null, null, slepst, transq);
		}
	}
	
	/***
	 * 功能:银联CUPS转入记账处理
	 * @param cupsIN CUPS转入记账复合类型
	 * @param qtIN 额度扣减复合类型
	 */
	public static void dealCUPSTranfe(CupsTranfe cupsIN, ChkQtIN qtIN){
		/**
		 * 1.调用额度扣减服务
		 * 2.调用内部户借记服务
		 * 3.调用电子账户存入服务
		 * 4.调用更新电子账户状态的方法
		 * 5.登记出入金记录，平衡性检查
		 */
		
		String smrycd = BusiTools.getBusiRunEnvs().getSmrycd();
		IoCaKnaAcdc acdc = ActoacDao.selKnaAcdc(cupsIN.getInacct(), false);
		if(CommUtil.isNull(acdc) || acdc.getStatus() == E_DPACST.CLOSE){
			
			//CommTools.getBaseRunEnvs().setIscose(E_YES___.YES);
			throw CaError.Eacct.BNAS0750();
		}
		
		IoCaKnaCust cust = ActoacDao.selKnaCust(acdc.getCustac(), false);
		if(CommUtil.isNull(cust)){
			throw CaError.Eacct.BNAS0750();
		}
		
		E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(acdc.getCustac()); //获取电子账户的账户类型
		
		//状态、类型 状态字控制
		AcTranfeChkIN chkIN = SysUtil.getInstance(AcTranfeChkIN.class);
		chkIN.setAccatp(accatp);
		chkIN.setCardno(acdc.getCardno()); //电子账号卡号
		chkIN.setCustac(acdc.getCustac()); //电子账号ID
		chkIN.setCustna(cust.getCustna());
		chkIN.setCapitp(E_CAPITP.IN104);
		chkIN.setOpcard(cupsIN.getOtacct());
		chkIN.setOppona(cupsIN.getOtacna());
		chkIN.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		//资金交易类检查，检查账户分类、状态和状态字 
		AcTranfeChkOT chkOT = CapitalTransCheck.chkTranfe(chkIN);
		
		
		E_CUACST cuacst = chkOT.getCuacst();
		
		KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
		if(accatp == E_ACCATP.WALLET){
			tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(acdc.getCustac(), E_ACSETP.MA);
			qtIN.setSbactp(E_SBACTP._12);
		}else{
			tblKnaAcct = CapitalTransDeal.getSettKnaAcctSub(acdc.getCustac(), E_ACSETP.SA);
			qtIN.setSbactp(E_SBACTP._11);
		}
		//币种校验
		if(CommUtil.isNotNull(cupsIN.getCrcycd())){
			if(!CommUtil.equals(cupsIN.getCrcycd(), tblKnaAcct.getCrcycd())){
				throw DpModuleError.DpstComm.BNAS0632();
			}
		}
		//转入机构校验
		if(CommUtil.isNotNull(cupsIN.getInbrch())){
			if(!CommUtil.equals(cupsIN.getInbrch(), tblKnaAcct.getBrchno())){
				throw DpModuleError.DpstComm.BNAS1588();
			}
		}
		
		//设置机构和币种的回传
		cupsIN.setCrcycd(tblKnaAcct.getCrcycd());
		cupsIN.setInbrch(tblKnaAcct.getBrchno());
		cupsIN.setInacna(tblKnaAcct.getAcctna());
		
		
		//额度检查扣减
		//if(qtIN.getIsckqt() == E_YES___.YES){ 
			IoCaSevAccountLimit qt = CommTools.getRemoteInstance(IoCaSevAccountLimit.class); //获取电子账户额度服务
			
			//获取输入复合类型
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter qtIn = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter.class);
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output	qtOt = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output.class);
			
			qtIn.setBrchno(tblKnaAcct.getBrchno());
			qtIn.setAclmfg(qtIN.getAclmfg());
			qtIn.setAccttp(accatp);
			//qtIn.setAuthtp(qtIN.getAuthtp().toString());
			qtIn.setCustac(acdc.getCustac());
			qtIn.setCustid(qtIN.getCustid());
			qtIn.setCustlv(qtIN.getCustlv());
			qtIn.setAcctrt(qtIN.getAcctrt());
			qtIn.setLimttp(qtIN.getLimttp());
			qtIn.setPytltp(qtIN.getPytltp());
			qtIn.setRebktp(qtIN.getRebktp());
			qtIn.setRisklv(qtIN.getRisklv());
			qtIn.setSbactp(qtIN.getSbactp());
			qtIn.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());
			qtIn.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			qtIn.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
			qtIn.setTranam(cupsIN.getTranam());
			
			qtIn.setCustie(chkOT.getIsbind()); //是否绑定卡标识
			qtIn.setFacesg(chkOT.getFacesg());	//是否面签标识
			qtIn.setRecpay(null); //收付方标识，电子账户转电子账户需要输入
			
			
			qt.SubAcctQuota(qtIn, qtOt); //额度检查扣减
			
		//}
		
		//内部户借记
		
		IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
//		String acbrch = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch().getBrchno(); //获取省中心机构号
		String acbrch = BusiTools.getBusiRunEnvs().getCentbr();
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", E_CLACTP._04.getValue(), "%", true);
		
		acdrIn.setAcbrch(acbrch);
		acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
		acdrIn.setCrcycd(tblKnaAcct.getCrcycd());
		acdrIn.setSmrycd(smrycd);
		acdrIn.setToacct(cupsIN.getInacct());
		acdrIn.setToacna(cupsIN.getInacna());
		acdrIn.setTranam(cupsIN.getTranam());
		acdrIn.setBusino(para.getParm_value1()); //业务编码
		acdrIn.setSubsac(para.getParm_value2());//子户号
		
		IoInAccount inAcctSer = SysUtil.getInstance(IoInAccount.class);
		inAcctSer.ioInAcdr(acdrIn);
		
		//电子账户存入记账
		DpAcctSvcType dpSrv = CommTools.getRemoteInstance(DpAcctSvcType.class); //存款记账服务
		
		SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class); //电子账户存入记账复合类型
		
		saveIn.setAcctno(tblKnaAcct.getAcctno()); //结算账户、钱包账户
		saveIn.setBankcd(cupsIN.getOtbrch());
		saveIn.setBankna("");
		saveIn.setCardno(acdc.getCardno());
		saveIn.setCrcycd(tblKnaAcct.getCrcycd());
		saveIn.setCustac(acdc.getCustac());
		saveIn.setOpacna(cupsIN.getOtacna());
		saveIn.setOpbrch(acbrch);
		//saveIn.setRemark("CUPS转入电子账户");
		saveIn.setSmrycd(smrycd);
		saveIn.setToacct(cupsIN.getOtacct());
		saveIn.setTranam(cupsIN.getTranam());
		
		dpSrv.addPostAcctDp(saveIn);
		
		//休眠转正常结息处理 + 修改电子账户状态
		CapitalTransDeal.dealAcctStatAndSett(cuacst, tblKnaAcct);
		
		
		
		
	}
	
	/**
	 * @Title: dealCUPSConfrim 
	 * @Description:  银联CUPS来账转入确认交易
	 * @param cupsIN
	 * @return
	 * @author zhangan
	 * @date 2016年11月17日 下午3:32:56 
	 * @version V2.3.0
	 */
	public static IaTransOutPro dealCUPSConfrim(CupsTranfe cupsIN){
		
		String smrycd = BusiTools.getBusiRunEnvs().getSmrycd();
		
		IoInAccount inSrv = CommTools.getRemoteInstance(IoInAccount.class);
		
		IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
		IaTransOutPro acdrOt = SysUtil.getInstance(IaTransOutPro.class);
		
		String acbrch = BusiTools.getBusiRunEnvs().getCentbr(); //获取省中心机构号
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", E_CLACTP._04.getValue(), "%", true);
		
		//内部户借方记账服务
		acdrIn.setAcbrch(acbrch);
		acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
		acdrIn.setCrcycd(cupsIN.getCrcycd());
		acdrIn.setSmrycd(smrycd);
		acdrIn.setToacct(cupsIN.getOtacct());
		acdrIn.setToacna(cupsIN.getOtacna());
		acdrIn.setTranam(cupsIN.getTranam());
		acdrIn.setBusino(para.getParm_value1()); //业务编码
		acdrIn.setSubsac(para.getParm_value2());//子户号
		
		inSrv.ioInAcdr(acdrIn); //内部户借方记账
		
		
		//内部户贷方记账服务
		KnpParameter para2 = SysUtil.getInstance(KnpParameter.class);
		IaAcdrInfo acdrIn2 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
		para2 = KnpParameterDao.selectOne_odb1("InParm.cupsconfrim","in", "04", "%", true);
		acdrIn2.setAcbrch(cupsIN.getInbrch()); //挂账所属机构挂电子账户机构
		acdrIn2.setTrantp(E_TRANTP.TR); //交易类型 
		acdrIn2.setCrcycd(cupsIN.getCrcycd());
		acdrIn2.setSmrycd(smrycd);
		acdrIn2.setToacct(cupsIN.getOtacct());
		acdrIn2.setToacna(cupsIN.getOtacna());
		acdrIn2.setTranam(cupsIN.getTranam());
		acdrIn2.setBusino(para2.getParm_value1()); //业务编码
		acdrIn2.setSubsac(para2.getParm_value2());//子户号
		acdrIn2.setDscrtx(cupsIN.getRemark());
		
		acdrOt = inSrv.ioInAccr(acdrIn2); //贷记服务
		
		return acdrOt;
	}
	
	
	/**
	 * @Title: dealCnapotVN 
	 * @Description:大小额来往帐 电子汇划来账、电子账户往账科目记账  
	 * @author zhangan
	 * @date 2016年11月3日 下午2:45:59 
	 * @version V2.3.0
	 */
	public static void dealCnapotVN(CupsTranfe cplCnapot, E_IOTYPE iotype){
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		para = KnpParameterDao.selectOne_odb1("InParm.cnapotvn","in", "%", "%", true);
		
		String busino = "";
		String subsac = "";
		//999	9930010101	电子汇划往账
		//999	9930020101	电子汇划来账
		if(iotype == E_IOTYPE.IN){ //来账
			busino = para.getParm_value1(); //电子汇划来账 业务编号
			subsac = para.getParm_value2();
		}else if(iotype == E_IOTYPE.OUT){ //往账
			busino = para.getParm_value3(); //电子汇划往账业务编号
			subsac = para.getParm_value4();
		}
		
		
		IoInAccount inSrv = CommTools.getRemoteInstance(IoInAccount.class);
		IaAcdrInfo acdrIn1 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
		IaAcdrInfo acdrIn2 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
		
		acdrIn2.setAcbrch(cplCnapot.getInbrch()); //挂账所属机构挂电子账户机构
		acdrIn2.setTrantp(E_TRANTP.TR); //交易类型 
		acdrIn2.setCrcycd(cplCnapot.getCrcycd());
		acdrIn2.setSmrycd(cplCnapot.getSmrycd());
		acdrIn2.setToacct(cplCnapot.getOtacct());
		acdrIn2.setToacna(cplCnapot.getOtacna());
		acdrIn2.setTranam(cplCnapot.getTranam());
		acdrIn2.setDscrtx("电子汇划贷记");
		acdrIn2.setBusino(busino); //业务编码
		acdrIn2.setSubsac(subsac);//子户号
		
		inSrv.ioInAccr(acdrIn2);
		
		acdrIn1.setAcbrch(cplCnapot.getInbrch()); //挂账所属机构挂电子账户机构
		acdrIn1.setTrantp(E_TRANTP.TR); //交易类型 
		acdrIn1.setCrcycd(cplCnapot.getCrcycd());
		acdrIn1.setSmrycd(cplCnapot.getSmrycd());
		acdrIn1.setToacct(cplCnapot.getInacct());
		acdrIn1.setToacna(cplCnapot.getInacna());
		acdrIn1.setTranam(cplCnapot.getTranam());
		acdrIn1.setDscrtx("电子汇划借记");
		acdrIn1.setBusino(busino); //业务编码
		acdrIn1.setSubsac(subsac);//子户号
		
		inSrv.ioInAcdr(acdrIn1);
	}
	/**
	 * @Title: chkRedpack 
	 * @Description:销户前检查是否  
	 * @param acctno
	 * @param userid
	 * @author zhangan
	 * @date 2016年11月25日 上午10:39:42 
	 * @version V2.3.0
	 */
	public static void chkRedpack(String cardno, String userid){
		
		IoCaOtherService.IoCaChkRedpack.InputSetter chkIN = SysUtil.getInstance(IoCaOtherService.IoCaChkRedpack.InputSetter.class);
		IoCaOtherService.IoCaChkRedpack.Output		chkOT = SysUtil.getInstance(IoCaOtherService.IoCaChkRedpack.Output.class);
		
		chkIN.setAcctno(cardno);
		chkIN.setUserid(userid);
		
		log.debug("*********************************************");
		log.debug("销户前检查是否有未落地红包：[%s]", chkIN.getAcctno());
		log.debug("*********************************************");
		
		IoCaOtherService dubboSrv = SysUtil.getInstanceProxyByBind(IoCaOtherService.class, "otsevdb");
		try {
			dubboSrv.chkRedpack(chkIN, chkOT);
		} catch (Exception e) {

			throw DpModuleError.DpstComm.BNAS0290();

		}
		
		if(chkOT.getExisfg() == E_YES___.YES){
			throw DpModuleError.DpstComm.E9999(chkOT.getReason());
		}
	}
}
