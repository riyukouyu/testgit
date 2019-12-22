package cn.sunline.ltts.busi.dptran.batchtran;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccsDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbSlep;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbSlepDao;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.froz.DpAcctStatus;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInterestAndPrincipal;
import cn.sunline.ltts.busi.dptran.batchtran.intf.Acslep.Input;
import cn.sunline.ltts.busi.dptran.batchtran.intf.Acslep.Property;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountManager;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbSlep;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryMesgOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpClsChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpClsChkOT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SLEPST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;

/**
 * 
 * @author xiongzhao
 *         <p>
 *         <li>2016年7月1日-下午4:00:45</li>
 *         <li>功能描述：电子账户休眠</li>
 *         </p>
 * 
 */

public class acslepDataProcessor
		extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.intf.Acslep.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Acslep.Property, IoDpKnaAcct > {
	private final static BizLog bizlog = BizLogUtil
			.getBizLog(acslepDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param job
	 *            批次作业ID
	 * @param index
	 *            批次作业第几笔数据(从1开始)
	 * @param dataItem
	 *            批次数据项
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(String jobId, int index, IoDpKnaAcct dataItem,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Acslep.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Acslep.Property property) {

		 BizLog bizLog1 = BizLogUtil.getBizLog(acslepDataProcessor.class);
		//设置批量的业务流水，推送消息
		CommTools.getBaseRunEnvs().setBusi_seq(CommTools.getBaseRunEnvs().getTrxn_seq());

		String sCustac = dataItem.getCustac();
		bizlog.info("电子账户开始休眠操作1"+sCustac);
		KnbSlep knbSlep= KnbSlepDao.selectFirst_odb1(sCustac, dataItem.getBrchno(), E_SLEPST.SLEP, false);
//		KnbSlepDao.selectOne_odb4(sCustac, E_SLEPST.SLEP, nullException);
		if (CommUtil.isNotNull(knbSlep)){
			bizlog.info("电子账户"+sCustac+"已休眠！");
			return ;
		}

		String sUpbldt = "";// 余额更新日期
		String sTrandt = CommTools.getBaseRunEnvs().getTrxn_date();// 当前交易日期
//		String sCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();// 当前环境变量法人代码
		String sSlepdt = "";// 电子账户休眠时间
		
		String timetm = DateTools2.getCurrentTimestamp();// 当前交易日期时间戳
		
		// 每笔处理交易流水重置
		 MsSystemSeq.getTrxnSeq();
		
		// 查询休眠控制参数
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("sleepday", "%", "%",
				"%", false);

		// 根据电子账户获取电子账户表数据
		IoCaKnaCust cplCaKnaCust = SysUtil.getInstance(
				IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(sCustac,
				false);
		if (CommUtil.isNull(cplCaKnaCust)) {
			// throw DpModuleError.DpstComm.E9999("电子账号不存在! ");
			bizlog.error("电子账号不存在! ", sTrandt);
			return;
		}
		bizlog.info("电子账户开始休眠操作2"+sCustac);
//		CommTools.getBaseRunEnvs().setBusi_org_id(cplCaKnaCust.getCorpno());

		// 查询电子账户分类
		E_ACCATP accttp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
				.qryAccatpByCustac(sCustac);

		KnaAcct cplKnaAcct = CapitalTransDeal.getSettKnaAcctAc(sCustac);
		if (CommUtil.isNotNull(cplKnaAcct)) {
			sUpbldt = cplKnaAcct.getUpbldt();
		}

		// 查询电子账户状态
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
				.selCaStInfo(sCustac);
		
		// 调用获取电子账户状态字方法
		IoDpAcStatusWord cplGetAcStWord = DpAcctStatus.GetAcStatus(sCustac);
		// 只有正常的允许休眠
		if (cuacst != E_CUACST.NORMAL) {
			bizlog.error("电子账户状态异常不允许休眠!", sTrandt);
			return;
		}
		bizlog.info("电子账户开始休眠操作3"+sCustac);
		// 部冻、借冻、双冻、部止、全止（银行止付）、全止（外部止付）、质押、存款证明、开单和预授权不允许休眠
		if (cplGetAcStWord.getPtfroz() == E_YES___.YES
				|| cplGetAcStWord.getBrfroz() == E_YES___.YES
				|| cplGetAcStWord.getDbfroz() == E_YES___.YES
				|| cplGetAcStWord.getPtstop() == E_YES___.YES
				|| cplGetAcStWord.getBkalsp() == E_YES___.YES
				|| cplGetAcStWord.getOtalsp() == E_YES___.YES
				|| cplGetAcStWord.getPledge() == E_YES___.YES
				|| cplGetAcStWord.getCertdp() == E_YES___.YES
				|| cplGetAcStWord.getBillin() == E_YES___.YES
				|| cplGetAcStWord.getPreaut() == E_YES___.YES) {
			// throw DpModuleError.DpstComm.E9999("电子账户状态字异常不允许休眠! ");
			bizlog.error("电子账户状态字异常不允许休眠!", sTrandt);
			return;
		}
		bizlog.info("电子账户开始休眠操作4"+sCustac);
		// 检查定期余额是否为0
		BigDecimal bigFixddp = DpAcctDao
				.selFxacAcctBlncByCustac(sCustac, false);
		if (CommUtil.isNotNull(bigFixddp)
				&& CommUtil.compare(bigFixddp, BigDecimal.ZERO) != 0) {
			// throw DpModuleError.DpstComm.E9999("定期账户余额不为0不允许休眠！ ");
			bizlog.error("定期账户"+sCustac+"余额不为0不允许休眠！", sTrandt);
			return;
		}
		
		// 检查是否有理财未赎回
//		Options<IoFnFnaAcct> fnlist = SysUtil.getInstance(IoFnSevQryTableInfo.class).fna_acct_selectAll_odb2(sCustac);
//		for(IoFnFnaAcct fnacct : fnlist){
//			if(fnacct.getAcctst() == BaseEnumType.E_DPACST.NORMAL){
//				bizlog.error("有未赎回的理财产品！", sTrandt);
//				return;
//			}
//			
//		}
		/*
		 * 检查贷款金额是否为0  修复： 电子账号有贷款产品，贷款金额不为0，六个月后仍转休眠
		 * 2017.12.11
		 * */
		
        BigDecimal intest = BigDecimal.ZERO;//利息
        BigDecimal principal = BigDecimal.ZERO;//本金
        List<KnaAccs> listKnaAccs = KnaAccsDao.selectAll_odb5(sCustac, false); 
        for (KnaAccs tblknaAccs : listKnaAccs) {
            if(tblknaAccs.getAcctst()==E_DPACST.NORMAL&&E_PRODTP.LOAN == tblknaAccs.getProdtp()){
                //贷款状态为正常
                DpInterestAndPrincipal dpInterestAndPrincipal = DpAcctQryDao.selPrincipalAndInterestByAcctno(tblknaAccs.getAcctno(),cn.sunline.ltts.busi.sys.type.LnEnumType.E_ACCTST.NORMAL, false);
                if(CommUtil.isNotNull(dpInterestAndPrincipal)){ 
                intest = intest.add(dpInterestAndPrincipal.getIntest());// 利息赋值值
                principal = principal.add(dpInterestAndPrincipal.getPrincipal());// 本金赋值
                }
                if(CommUtil.compare(intest.add(principal), BigDecimal.ZERO) > 0){
                    bizlog.debug("账户存在生效的余额不为0的贷款[" + sCustac + "]");
                    return;
                }
            }
        }
        bizlog.info("电子账户开始休眠操作5"+sCustac);
		// 查询冻结登记簿
		Options<IoDpKnbFroz> lstKnbFroz = SysUtil.getInstance(
				IoDpFrozSvcType.class).qryKnbFroz(sCustac, E_FROZST.VALID);
		BigDecimal hdbkam = BigDecimal.ZERO;
		for (IoDpKnbFroz knbfroz : lstKnbFroz) {
			if (knbfroz.getFroztp() == E_FROZTP.FNFROZ) {
				if (knbfroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
					hdbkam = hdbkam.add(knbfroz.getFrozam());// 保留金额
				}
			}
		}
		if (CommUtil.compare(hdbkam,BigDecimal.ZERO) > 0) {
			bizlog.error("存在未解除的产品协议!", sTrandt);
			return;
		}

		// 检查电子账户存款资金是否满足休眠条件
		IoDpClsChkIN chkin = SysUtil.getInstance(IoDpClsChkIN.class);
		chkin.setCustac(sCustac);
		chkin.setIssett(E_YES___.NO);
		IoDpClsChkOT cplDpClsChkOT = SysUtil.getInstance(DpAcctSvcType.class)
				.TestInterest(chkin);
		
		//若活期智能存款账户余额不为0不允许休眠
		List<KnaAcct> tblknaAcct = KnaAcctDao.selectAll_odb9(E_ACSETP.HQ, sCustac, false);
		if(CommUtil.isNotNull(tblknaAcct)){
			//如果活期智能存款账户不为0，不允许休眠
			for(KnaAcct accts : tblknaAcct){
				BigDecimal onlnbl = DpAcctProc.getAcctBalance(accts);
				if(CommUtil.compare(onlnbl, BigDecimal.ZERO) > 0){
					bizlog.debug("账户存在生效的余额不为0的活期智能存款[" + sCustac + "]");
					return;
				}
			}
		}
		
		// 检查个人结算户、钱包账户子账户合计余额是否在50元（含）以下
		BigDecimal bigTotlam = cplDpClsChkOT.getTotlam().subtract(
				cplDpClsChkOT.getIntrvl());
		if (accttp == E_ACCATP.GLOBAL || accttp == E_ACCATP.FINANCE) {
			if (CommUtil.compare(bigTotlam, ConvertUtil.toBigDecimal(tblKnpParameter.getParm_value3())) > 0) {
				// throw DpModuleError.DpstComm.E9999("账户资金金额不满足休眠条件！ ");
				bizlog.error("账户资金金额不满足休眠条件！", sTrandt);
				return;
			}
		}
		// 电子钱包账户账户余额为1元以下
		else if (accttp == E_ACCATP.WALLET) {
			if (CommUtil.compare(bigTotlam, ConvertUtil.toBigDecimal(tblKnpParameter.getParm_value4())) > 0) {
				// throw DpModuleError.DpstComm.E9999("电子钱包账户余额不满足休眠条件！ ");
				bizlog.error("电子钱包账户余额不满足休眠条件！ ", sTrandt);
				return;
			}
		}
		bizlog.info("电子账户开始休眠操作6"+sCustac);
		
		// 查询电子账户最后一次存取业务的交易日期
		if (CommUtil.isNotNull(sUpbldt)) {
			// 取当前账户明细表除结息业务以外最后一次交易日期
			String sTrandt1 = DpDayEndDao.selLastTrandt(sCustac, false);// 查询账户余额发生明细当前表
			if (CommUtil.isNotNull(sTrandt1)) {
				sSlepdt = DateTools2.calDateByTerm(sTrandt1,
						tblKnpParameter.getParm_value1());
			} else {
				// 取历史账户明细表除结息业务以外最后一次交易日期
				String sTrandt2 = DpDayEndDao.selHlastTrandt(sCustac, false);// 查询账户余额发生明细历史表
				if (CommUtil.isNotNull(sTrandt2)) {
					sSlepdt = DateTools2.calDateByTerm(sTrandt2,
							tblKnpParameter.getParm_value1());
				} else {
					// 取开户日期
					sSlepdt = DateTools2.calDateByTerm(cplCaKnaCust.getOpendt(),
							tblKnpParameter.getParm_value1());
				}
			}

		} else {
			bizlog.info("13获取的开户日期："+sSlepdt+"，当前的交易日期："+sTrandt);
			// 取开户日期
			sSlepdt = DateTools2.calDateByTerm(cplCaKnaCust.getOpendt(),
					tblKnpParameter.getParm_value1());
			bizlog.info("155获取的开户日期："+sSlepdt+"，当前的交易日期："+sTrandt);
		}
		bizlog.info("获取的开户日期："+sSlepdt+"，当前的交易日期："+sTrandt);
		if (CommUtil.compare(sSlepdt, sTrandt) < 0) {
			bizlog.info("7电子账户开始休眠操作"+sCustac);
			// 更新电子账户状态为休眠
			DpDayEndDao.updKnaCustSleep(E_ACCTST.SLEEP,
					cplCaKnaCust.getCustac(),timetm);
			// 更新电子账户负债活期子账户状态为休眠
			DpDayEndDao.updKnaAcctSleep(E_ACCTST.SLEEP,
					cplCaKnaCust.getCustac(), E_ACCTST.NORMAL,timetm);
			// 登记休眠登记簿
			IoCaKnbSlep cplKnbSlep = SysUtil.getInstance(IoCaKnbSlep.class);
			if(CommUtil.isNotNull(KnbSlepDao.selectFirst_odb1(sCustac, dataItem.getBrchno(), E_SLEPST.CANCEL, false))){
				KnbSlep tblKnbSlep = SysUtil.getInstance(KnbSlep.class);
				CommUtil.copyProperties(tblKnbSlep, cplKnbSlep);
				cplKnbSlep.setSlepst(E_SLEPST.SLEP);// 休眠状态
				KnbSlepDao.update_odb1(tblKnbSlep);
			}else{
				cplKnbSlep.setCustac(sCustac);// 电子账号
				cplKnbSlep.setCustna(cplCaKnaCust.getCustna());// 客户名称
				cplKnbSlep.setAccttp(accttp);// 电子账户分类
				cplKnbSlep.setBrchno(cplCaKnaCust.getBrchno());// 账户归属机构
				cplKnbSlep.setTrandt(sTrandt);// 交易日期
				cplKnbSlep.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());// 交易时间
				cplKnbSlep.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
				cplKnbSlep.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());// 操作柜员
				cplKnbSlep.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq());// 操作柜员流水
				cplKnbSlep.setSlepst(E_SLEPST.SLEP);// 休眠状态
				cplKnbSlep.setCuacst(cuacst);// 原客户账户状态
				SysUtil.getInstance(IoCaSevAccountManager.class).knbSlepInsert(
						cplKnbSlep);
			}
			bizlog.info("8电子账户开始休眠操作"+sCustac);
			// 登记客户化状态
			// 更新休眠状态，无维度
			IoCaUpdAcctstIn cplDimeInfo = SysUtil
					.getInstance(IoCaUpdAcctstIn.class);
			cplDimeInfo.setCustac(sCustac);
			SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(
					cplDimeInfo);
			bizlog.info("9电子账户开始休眠操作"+sCustac);
			// 查询客户关联关系表
			/*
			IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(
					IoCuSevQryTableInfo.class).cif_cust_accsByCustno(
					cplCaKnaCust.getCustno(), false, E_STATUS.NORMAL);
			*/
	/*		// 短信流水登记
			IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
			
			if (CommUtil.isNotNull(cplCifCustAccs)) {
				cplKubSqrd.setAppsid(cplCifCustAccs.getAppsid());// app推送ID
			}
			
			cplKubSqrd.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());// 内部交易码
			cplKubSqrd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
			cplKubSqrd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());// 交易时间
			cplKubSqrd.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
			cplKubSqrd.setTmstmp(DateTools2.getCurrentTimestamp());// 时间戳
			// 调用短信流水登记服务
			SysUtil.getInstance(IoPbSmsSvcType.class).pbTransqReg(cplKubSqrd);*/
			
			//批量短信发送
	        IoCaQryMesgOut mesgOut = SysUtil.getInstance(IoCaQryMesgOut.class);
	        IoCaSrvGenEAccountInfo eAccountInfo = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
	        mesgOut = eAccountInfo.selMesgInfo(sCustac, CommTools.getBaseRunEnvs().getTrxn_date(), BusiTools.getBusiRunEnvs().getTrantm());
//	        //ApMessageComplexType.SMSCType smscTypeDO = SysUtil.getInstance(ApMessageComplexType.SMSCType.class);
//	       
//	        KnpParameter KnpParameterDO = KnpParameterDao.selectOne_odb1("MESGSD", CommTools.getBaseRunEnvs().getTrxn_code(), false);
//	        
//	    	Map<String, String> msgParm = new HashMap<String, String>();
//	        if(CommUtil.isNotNull(KnpParameterDO) && CommUtil.equals(KnpParameterDO.getParm_value1(), "Y")){
//	        	String  Meteid = KnpParameterDO.getParm_value2(); //短息模版ID
//	    		 msgParm.put("brchna", mesgOut.getBrchna());// 机构名
//	    	        msgParm.put("cardno", mesgOut.getLastnm());// 尾号
//	    	        smscTypeDO.setMeteid(Meteid);
//	    		        smscTypeDO.setMobile(mesgOut.getAcalno());
//	    		        smscTypeDO.setMsgparm(msgParm);
//	    		        smscTypeDO.setNacode("86");//默认值
//	    		        
//	    		        SMSUtil.sendSMSMessage(smscTypeDO); 
//	        }
	        /*
	         * yusheng     
	         */
//	      /*********************休眠推送APP消息*********************************************************************/
//	              MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//	      		mri.setMtopic("Q0101005");
//	      		ToAppSendMsg AppSendMsgInput = SysUtil.getInstance(ToAppSendMsg.class);
//	      		 //查询电子账户信息
//	      		 CaCustInfo.accoutinfos accoutinfos = SysUtil.getInstance(CaCustInfo.accoutinfos.class);
//	      		 accoutinfos = EacctMainDao.selCustInfobyCustac(sCustac,
//	      					E_ACALST.NORMAL, E_ACALTP.CELLPHONE, false);
//	      	   bizLog1.debug("客户信息ID==》accoutinfos:"+accoutinfos.getCustid()+"<============");
//	      		AppSendMsgInput.setUserId(accoutinfos.getCustid()); // 用户ID
//	      		AppSendMsgInput.setOutNoticeId("Q0101005");//外部消息ID
//	      		AppSendMsgInput.setNoticeTitle("您的电子账户已休眠");//公告标题
//	      		/*String date  = CommTools.getBaseRunEnvs().getTrxn_date().substring(0,4)+"年"
//	      		+CommTools.getBaseRunEnvs().getTrxn_date().substring(4,6)+"月"+ 
//	      				CommTools.getBaseRunEnvs().getTrxn_date().substring(6,8)+"日"+
//	      				BusiTools.getBusiRunEnvs().getTrantm().substring(0,2)+":"+
//	      				BusiTools.getBusiRunEnvs().getTrantm().substring(2,4)+":"+
//	      				BusiTools.getBusiRunEnvs().getTrantm().substring(4,6);*/
//	      		
//	      		AppSendMsgInput.setContent("您的ThreeBank电子账户已休眠暂停使用。"
//	      				+ "您可以向电子账户转入任意金额激活账户，即可继续为您服务！"); //内容
//	      		AppSendMsgInput.setSendtime(CommTools.getBaseRunEnvs().getTrxn_date());//消息生成时间
//	      		AppSendMsgInput.setClickType(E_CLIKTP.NO);
//	      		AppSendMsgInput.setClickValue("");//点击动作值
//	      		AppSendMsgInput.setTirggerSys(CommTools.getBaseRunEnvs().getSystcd());//触发系统
//	      		AppSendMsgInput.setTransType(E_APPTTP.CUACCH);
//	      		bizLog1.debug("消息参数传入==》AppSendMsgInput:"+AppSendMsgInput+"<============");
//	      		mri.setMsgtyp("ApSmsType.ToAppSendMsg");
//	      		mri.setMsgobj(AppSendMsgInput); 
//	      		bizLog1.debug("消息参数添加==》mri:"+mri+"<============");
//	      		AsyncMessageUtil.add(mri); 
//	      		SysUtil.getInstance(IoApAsyncMessage.class).afterBatchSendSMS();

		}

		bizlog.info("10电子账户开始休眠操作"+sCustac);
		//批量短信发送
        IoCaQryMesgOut mesgOut = SysUtil.getInstance(IoCaQryMesgOut.class);
        IoCaSrvGenEAccountInfo eAccountInfo = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
        mesgOut = eAccountInfo.selMesgInfo(sCustac, CommTools.getBaseRunEnvs().getTrxn_date(), BusiTools.getBusiRunEnvs().getTrantm());
//        ApMessageComplexType.SMSCType smscTypeDO = SysUtil.getInstance(ApMessageComplexType.SMSCType.class);
//       
//        KnpParameter KnpParameterDO = KnpParameterDao.selectOne_odb1("MESGSD", CommTools.getBaseRunEnvs().getTrxn_code(), false);
//        
//    	Map<String, String> msgParm = new HashMap<String, String>();
//        if(CommUtil.isNotNull(KnpParameterDO) && CommUtil.equals(KnpParameterDO.getParm_value1(), "Y")){
//        	String  Meteid = KnpParameterDO.getParm_value2(); //短息模版ID
//    		 msgParm.put("brchna", mesgOut.getBrchna());// 机构名
//    	        msgParm.put("cardno", mesgOut.getLastnm());// 尾号
//    	        smscTypeDO.setMeteid(Meteid);
//    		        smscTypeDO.setMobile(mesgOut.getAcalno());
//    		        smscTypeDO.setMsgparm(msgParm);
//    		        smscTypeDO.setNacode("86");//默认值
//    		        
//    		        SMSUtil.sendSMSMessage(smscTypeDO); 
//        }
       


		// 将原法人设置回环境变量
//		CommTools.getBaseRunEnvs().setBusi_org_id(sCorpno);

	}

	@Override
	public void jobExceptionProcess(String taskId, Input input,
			Property property, String jobId, IoDpKnaAcct dataItem, Throwable t) {

		super.jobExceptionProcess(taskId, input, property, jobId, dataItem, t);
	}

	/**
	 * 获取数据遍历器。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<IoDpKnaAcct> getBatchDataWalker(
			cn.sunline.ltts.busi.dptran.batchtran.intf.Acslep.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Acslep.Property property) {

		Params params = new Params();
		String sTrandt = CommTools.getBaseRunEnvs().getTrxn_date();
		
		// 查询休眠控制参数
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("sleepday", "%", "%","%", false);
		Integer iTrandt = DateTools2.getDay(DateTools2.covStringToDate(sTrandt));
		bizlog.info("当前交易日期："+sTrandt+"，每月休眠日："+tblKnpParameter.getParm_value2());
//		if (CommUtil.compare(iTrandt.toString(), tblKnpParameter.getParm_value2()) != 0) {
//			// throw DpModuleError.DpstComm.E9999("账户休眠处理日期为每月5日日终！ ");
//			return null;
//		}
		bizlog.info("获取数据遍历器end");
		return new CursorBatchDataWalker<IoDpKnaAcct>(
				DpDayEndDao.namedsql_selAcSleepInfos, params);
	}


}
