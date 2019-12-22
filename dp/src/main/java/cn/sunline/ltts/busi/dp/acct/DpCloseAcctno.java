package cn.sunline.ltts.busi.dp.acct;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.dayend.DpDayEndInt;
import cn.sunline.ltts.busi.dp.domain.DpSaveEntity;
import cn.sunline.ltts.busi.dp.layer.LayerAcctSrv;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPidl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPidlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.DpHisDepo.HKnlBill;
import cn.sunline.ltts.busi.dp.tables.DpHisDepo.HKnlBillDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBill;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBillDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.CalInterTax;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseIN;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSTRFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STRKTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PMCRAC;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CORRTG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

/***
 * 
 * @ClassName: DpCloseAcctno 
 * @Description: 活期负债子账户销户
 * @author zhangan
 * @date 2016年7月7日 下午6:36:11 
 *
 */
public class DpCloseAcctno {

	
	private static BizLog log = BizLogUtil.getBizLog(DpCloseAcctno.class);
	
	/***
	 * @Title: prcFxacDeposit 
	 * @Description: 负债定期账户结息处理（单结息操作，不涉及销户处理）
	 * @param fxac 定期负债账户表记录数据
	 * @author zhangan
	 * @date 2016年7月7日 下午6:39:47 
	 * @version V2.3.0
	 */
	public static InterestAndIntertax prcFxacInterest(KnaFxac fxac, IoDpCloseIN clsin, KnbAcin acin){
		
//		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//		CommTools.getBaseRunEnvs().setBusi_org_id(fxac.getCorpno());
        
        DpSaveEntity entity = SysUtil.getInstance(DpSaveEntity.class);
        entity.setAcctno(fxac.getAcctno());
        entity.setTranam(fxac.getOnlnbl());
        entity.setDetlfg(acin.getDetlfg());
        
        //计算利息，登记付息明细记录
        DpAcctProc.prcDrawCalcin(entity, fxac.getPddpfg());
        
        BigDecimal interest = entity.getInstam(); //利息
        BigDecimal intxam = entity.getIntxam();//利息税
        interest = BusiTools.roundByCurrency(fxac.getCrcycd(), interest,null);
		
		
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String mntrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		String brchno = fxac.getBrchno();
		InterestAndIntertax cplint = SysUtil.getInstance(InterestAndIntertax.class);

		//借利息支出
		if(CommUtil.compare(interest, BigDecimal.ZERO) > 0){
			IoAccounttingIntf account = SysUtil.getInstance(IoAccounttingIntf.class);
			account.setCuacno(fxac.getCustac());
			account.setAcctno(fxac.getAcctno());
//			account.setAcseno(fxac.get());
			account.setProdcd(fxac.getProdcd());
			account.setDtitcd(fxac.getAcctcd());
			account.setCrcycd(fxac.getCrcycd());
			account.setTranam(interest);
			account.setAcctdt(trandt); // 应入账日期
			account.setTransq(transq); // 交流水易
			account.setMntrsq(mntrsq); //
			account.setTrandt(trandt);
			account.setAcctbr(brchno);
			account.setAmntcd(E_AMNTCD.DR); // 借方
			account.setAtowtp(E_ATOWTP.DP); // 存款
			account.setTrsqtp(E_ATSQTP.ACCOUNT); // 账务流水
			account.setBltype(E_BLTYPE.PYIN); // 余额属性：利息支出
			// 登记交易信息，供总账解析
			if (CommUtil.equals(
					"1",
					KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
							true).getParm_value1())) {
				KnpParameter para = SysUtil.getInstance(KnpParameter.class);
				para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%",
						"%", true);
				account.setTranms(para.getParm_value1());// 登记交易信息
			}
			SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(account);
		}
		
		//利息税记账
		IoAccounttingIntf cplIoAccounttingIntx = SysUtil.getInstance(IoAccounttingIntf.class);
		if (CommUtil.isNotNull(intxam) && CommUtil.compare(intxam, BigDecimal.ZERO) != 0) {
			
			cplIoAccounttingIntx.setCuacno(fxac.getCustac());
			cplIoAccounttingIntx.setAcctno(fxac.getAcctno());
			cplIoAccounttingIntx.setProdcd(fxac.getProdcd());
			cplIoAccounttingIntx.setDtitcd(fxac.getAcctcd());
			cplIoAccounttingIntx.setCrcycd(fxac.getCrcycd());
			cplIoAccounttingIntx.setTranam(intxam); // 利息税
			cplIoAccounttingIntx.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 应入账日期
			cplIoAccounttingIntx.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
			cplIoAccounttingIntx.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); // 账务日期
			cplIoAccounttingIntx.setAcctbr(brchno);
			cplIoAccounttingIntx.setAmntcd(E_AMNTCD.CR);
			cplIoAccounttingIntx.setAtowtp(E_ATOWTP.DP);
			cplIoAccounttingIntx.setTrsqtp(E_ATSQTP.ACCOUNT);
			cplIoAccounttingIntx.setBltype(E_BLTYPE.INTAX);
			// 登记交易信息，供总账解析
			if (CommUtil.equals(
					"1",
					KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
							true).getParm_value1())) {
				KnpParameter para = SysUtil.getInstance(KnpParameter.class);
				para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3020100", "%",
						"%", true);
				cplIoAccounttingIntx.setTranms(para.getParm_value1());// 登记交易信息
			}					
			SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
					cplIoAccounttingIntx);
			
		}	
		if (CommUtil.isNull(interest)) {
			interest = BigDecimal.ZERO;
		}
		if (CommUtil.isNull(intxam)) {
			intxam = BigDecimal.ZERO;
		}
		cplint.setInstam(interest);
		cplint.setIntxam(intxam);
		cplint.setDiffam(interest.subtract(intxam));
//		CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
		
		return cplint;
	}
	
	
	/**
	 * @Title: prcDrawFxac 
	 * @Description:  定期账户销户记账
	 * @param fxac
	 * @param toacct
	 * @param tranam
	 * @return
	 * @author zhangan
	 * @date 2016年12月7日 下午3:48:58 
	 * @version V2.3.0
	 */
	public static DrawDpAcctOut prcClsFxacAcct(KnaFxac fxac, String toacct, E_YES___ detlfg){
		
		DrawDpAcctOut cplDrawOt = SysUtil.getInstance(DrawDpAcctOut.class);
		
		String trandt =CommTools.getBaseRunEnvs().getTrxn_date();
		String tmstmp =DateTools2.getCurrentTimestamp();
		
		
		if(CommUtil.compare(fxac.getOnlnbl(), BigDecimal.ZERO) > 0){
			DrawDpAcctIn cplDrawIn = SysUtil.getInstance(DrawDpAcctIn.class); //支取输入
			cplDrawIn.setAcctno(fxac.getAcctno());
			cplDrawIn.setBankcd(null);
			cplDrawIn.setBankna(null);
			cplDrawIn.setCardno(null);
			cplDrawIn.setCrcycd(fxac.getCrcycd());
			cplDrawIn.setCustac(fxac.getCustac());
			cplDrawIn.setIschck(E_YES___.NO);
			cplDrawIn.setOpacna(fxac.getAcctna());
			cplDrawIn.setOpbrch(fxac.getBrchno());
			
			//取值产品名称
			String remark = "";
			String remark1 = "";
			KnaAcctProd tblAcctProd = DpAcctDao.selKnaAcctProdByAcctno(fxac.getAcctno(), false);
			KnaFxacProd tblFxacProd = DpAcctDao.selKnaFxacProdByAcctno(fxac.getAcctno(), false);
			if(CommUtil.isNotNull(tblAcctProd)){
				remark = "活期-"+tblAcctProd.getObgaon();
				remark1 = tblAcctProd.getObgaon();
			}else if(CommUtil.isNotNull(tblFxacProd)){
				remark = "定期-"+tblFxacProd.getObgaon();
				remark1 = tblFxacProd.getObgaon();
			}
			
			//获取处理码给交易码设置不同值
			String prcscd = CommTools.getBaseRunEnvs().getTrxn_code();
			if(CommUtil.equals(prcscd, "acmatu")){
				cplDrawIn.setRemark(remark);
				cplDrawIn.setSmrycd(BusinessConstants.SUMMARY_TZ);
				cplDrawIn.setSmryds("投资");
			}else {
				cplDrawIn.setSmrycd(BusinessConstants.SUMMARY_XH);
				cplDrawIn.setSmryds("销户");
				cplDrawIn.setRemark(remark1);
			}

			cplDrawIn.setToacct(toacct);
			cplDrawIn.setTranam(fxac.getOnlnbl());
			
			cplDrawOt = SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawIn);
		}
		
		if(CommUtil.isNull(cplDrawOt.getInstam())){
			cplDrawOt.setInstam(BigDecimal.ZERO);
		}
		
		if(detlfg == E_YES___.YES){
			//String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
			String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
			//获取公共报文头的备注
			String remark = BusiTools.getBusiRunEnvs().getRemark();
			
			KnaFxacProd tblFxacProd = DpAcctDao.selKnaFxacProdByAcctno(fxac.getAcctno(), false);
			//获取可售产品名称
			if(CommUtil.isNotNull(tblFxacProd)){
				BusiTools.getBusiRunEnvs().setRemark(tblFxacProd.getObgaon());
			}
			//处理明细汇总支取明细记录
			DpDayEndInt.prcDrawInstPay(fxac.getAcctno(), fxac.getCrcycd(), trandt);
			
			//将原有的公共报文里面的备注set公共报文头备注
			BusiTools.getBusiRunEnvs().setRemark(remark);
			
			BigDecimal lastbl = fxac.getLastbl();
			String upbldt = fxac.getUpbldt();
			
			//String acctdt = ApDCN.getAccountDateOneDCN(fxac.getAcctno(), trandt, fxac.getUpbldt());
				
			//如果是当天第一笔交易则更新上日余额及余额更新时间
			if (CommUtil.compare(trandt, upbldt) > 0) {
				/* 过账:更新余额最新更新日期 */
				lastbl = fxac.getOnlnbl();
				upbldt = trandt;
			}
			
			DpAcctDao.updKnaFxacClose(transq, trandt, E_DPACST.CLOSE, BigDecimal.ZERO, lastbl, upbldt, fxac.getAcctno(), fxac.getCorpno(),tmstmp);
		}
		DpAcctDao.updKnaAccsClose(E_DPACST.CLOSE, fxac.getAcctno(), fxac.getCorpno(),tmstmp);
		
		return cplDrawOt;
		
	}
	
	/***
	 * @Title: prcCurrInterest 
	 * @Description: 处理活期负债表中的智能存款的利息记账
	 * @author zhangan
	 * @date 2016年7月7日 下午8:19:46 
	 * @version V2.3.0
	 */
	public static InterestAndIntertax prcCurrInterest(KnaAcct acct, IoDpCloseIN clsin){
		
		
//		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//		CommTools.getBaseRunEnvs().setBusi_org_id(acct.getCorpno());
		
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String mntrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		String brchno = acct.getBrchno();
		
		String tmstmp = DateTools2.getCurrentTimestamp();
		//利息和利息税
		InterestAndIntertax cplint = SysUtil.getInstance(InterestAndIntertax.class);
		String acctno = acct.getAcctno();
		String crcycd = acct.getCrcycd();
		
		BigDecimal interest = BigDecimal.ZERO;
		BigDecimal intertax = BigDecimal.ZERO; //利息税
		
		E_ACSETP acsetp = acct.getAcsetp();
		BigDecimal onlnbl = acct.getOnlnbl();
		KnbAcin acin = KnbAcinDao.selectOne_odb1(acctno, true);
				
		if (E_INBEFG.INBE == acin.getInbefg()) {// 计息标志为计息

			List<KubInrt> lstInrt = LayerAcctSrv.getKubInrt(acctno);

			IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
			
			 IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
			 intrEntity.setCorpno(acin.getCorpno());  //法人代码
			 intrEntity.setBrchno(acct.getBrchno());//机构号
			 intrEntity.setTranam(onlnbl);//交易金额
			 intrEntity.setTrandt(trandt);//交易日期
			 intrEntity.setIntrcd(acin.getIntrcd());   //利率代码 
			 intrEntity.setIncdtp(acin.getIncdtp());  //利率代码类型
			 intrEntity.setCrcycd(acin.getCrcycd());//币种
			 intrEntity.setInbebs(acin.getTxbebs());   //计息基础
			 intrEntity.setIntrwy(acin.getIntrwy());  //靠档方式
			 intrEntity.setBgindt(acin.getBgindt());  //起息日期
			 intrEntity.setEdindt(trandt);  //止息日
			 intrEntity.setDepttm(E_TERMCD.T000);  
			 
			 intrEntity.setLevety(acin.getLevety());
			 if(acin.getIntrdt() == E_INTRDT.OPEN){
				 intrEntity.setTrandt(acin.getOpendt());
				 intrEntity.setTrantm("999999");
			 }
			 pbpub.countInteresRate(intrEntity);
			 
			BigDecimal intrvl = intrEntity.getIntrvl();
			
			KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acin.getAcctno(), true);
			 
			 // 利率优惠后执行利率
			 intrvl = intrvl.add(intrvl.multiply(CommUtil.nvl(tblKubInrt.getFavort(),BigDecimal.ZERO).
					 divide(BigDecimal.valueOf(100))));
			 
			 //mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
			 //利率的最大范围值
			 BigDecimal intrvlmax = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
			 //利率的最小范围值
			 BigDecimal intrvlmin = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
			 
			 if(CommUtil.compare(intrvl, intrvlmin)<0){
				 intrvl = intrvlmin;
			 }else if(CommUtil.compare(intrvl, intrvlmax)>0){
				 intrvl = intrvlmax;
			 }
			 			 
			
			
			//查询分段积数
			CalInterTax calInterTax = SysUtil.getInstance(CalInterTax.class);
			calInterTax.setAcctno(acctno);
			calInterTax.setTranam(onlnbl);
			calInterTax.setCuusin(intrvl);
			calInterTax.setInbebs(acin.getTxbebs());
			
			InterestAndIntertax interestAndTax = 	DpInterestAndTax.calcInterAndTax(acin,calInterTax, true);				
			
			interest =  interestAndTax.getInstam(); // 利息
			intertax =  interestAndTax.getIntxam(); // 利息税
			
			log.debug("<<=====账号:[%s],分段产生的利息:[%s],利息税:[%s]=====>>", acct.getAcctno(),interest,intertax);


		//登记付息明细
		if (!CommUtil.equals(interest, BigDecimal.ZERO)) {
			DpCloseAcctno.insKnbPidl(acin, onlnbl, interest, lstInrt, acsetp);
		}
		//1.修改计息表中的积数
		DpAcctDao.updKubInrtToClose(trandt, acctno,tmstmp);
		//2.修改账户利率表中的值
		acin.setPlanin(BigDecimal.ZERO);
		acin.setLastdt(trandt);
		acin.setLcindt(trandt);
		acin.setLaamdt(trandt);
		acin.setCutmam(BigDecimal.ZERO);
		KnbAcinDao.updateOne_odb1(acin);
		//3.修改分段表中的值
		DpAcctDao.updknbIndlToClose(acctno,tmstmp);
		
		//利息支出记账处理
		//借利息支出
		if(CommUtil.compare(interest, BigDecimal.ZERO) > 0){
			IoAccounttingIntf account = SysUtil.getInstance(IoAccounttingIntf.class);
			account.setCuacno(acct.getCustac());
			account.setAcctno(acctno);
			account.setProdcd(acct.getProdcd());
			account.setDtitcd(acct.getAcctcd());
			account.setCrcycd(BusiTools.getDefineCurrency());
			account.setTranam(interest);
			account.setAcctdt(trandt); // 应入账日期
			account.setTransq(transq);
			account.setMntrsq(mntrsq);
			account.setTrandt(trandt);
			account.setAcctbr(brchno);
			account.setAmntcd(E_AMNTCD.DR); // 借方
			account.setAtowtp(E_ATOWTP.DP); // 存款
			account.setTrsqtp(E_ATSQTP.ACCOUNT); // 账务流水
			account.setBltype(E_BLTYPE.PYIN); // 余额属性：利息支出
			// 登记交易信息，供总账解析
			if (CommUtil.equals(
					"1",
					KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
							true).getParm_value1())) {
				KnpParameter para = SysUtil.getInstance(KnpParameter.class);
				para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%",
						"%", true);
				account.setTranms(para.getParm_value1());// 登记交易信息
			}				
			SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(account);
		}
		IoAccounttingIntf cplIoAccounttingIntx = SysUtil.getInstance(IoAccounttingIntf.class);
		if (CommUtil.compare(intertax, BigDecimal.ZERO) != 0) {
			
			cplIoAccounttingIntx.setCuacno(acct.getCustac());
			cplIoAccounttingIntx.setAcctno(acctno);
			cplIoAccounttingIntx.setAcseno(acctno);
			cplIoAccounttingIntx.setProdcd(acct.getProdcd());
			cplIoAccounttingIntx.setDtitcd(acct.getAcctcd());
			cplIoAccounttingIntx.setCrcycd(crcycd);
			cplIoAccounttingIntx.setTranam(intertax); // 利息税
			cplIoAccounttingIntx.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 应入账日期
			cplIoAccounttingIntx.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
			cplIoAccounttingIntx.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); // 账务日期
			cplIoAccounttingIntx.setAcctbr(brchno);
			cplIoAccounttingIntx.setAmntcd(E_AMNTCD.CR);
			cplIoAccounttingIntx.setAtowtp(E_ATOWTP.DP);
			cplIoAccounttingIntx.setTrsqtp(E_ATSQTP.ACCOUNT);
			cplIoAccounttingIntx.setBltype(E_BLTYPE.INTAX);
			// 登记交易信息，供总账解析
			if (CommUtil.equals(
					"1",
					KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
							true).getParm_value1())) {
				KnpParameter para = SysUtil.getInstance(KnpParameter.class);
				para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3020100", "%",
						"%", true);
				cplIoAccounttingIntx.setTranms(para.getParm_value1());// 登记交易信息
			}					
			SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
					cplIoAccounttingIntx);
			
		}		
	}	
//		CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
		cplint.setInstam(interest);
		cplint.setIntxam(intertax);
		cplint.setDiffam( interest.subtract(intertax));
		return cplint;
	}
	
	/***
	 * @Title: prcCurrOnbal 
	 * @Description: 活期负债表中的智能存款账户余额记账,[结算户销户时返回的是结算户中的余额]
	 * @param acct
	 * @return
	 * @author zhangan
	 * @date 2016年7月8日 上午9:05:10 
	 * @version V2.3.0
	 */
	public static BigDecimal prcCurrOnbal(KnaAcct acct, IoDpCloseIN clsin){
		
		//获取账户总余额
    	//BigDecimal onbal = KnaAcctDao.selectOne_odb1(acct.getAcctno(), true).getOnlnbl();
    	
    	
    	//acct = KnaAcctDao.selectOne_odb1(acct.getAcctno(), true);
//    	acct = ActoacDao.selKnaAcct(acct.getAcctno(), true);
    	BigDecimal onbal = acct.getOnlnbl();
    	
//    	String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//		CommTools.getBaseRunEnvs().setBusi_org_id(acct.getCorpno());
    	
    	
    	String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		String mntrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq(); // 主交易流水
		String brchno = acct.getBrchno(); // 交易机构
    	String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//交易日期
		String acctdt = trandt;//应入账日期，默认为交易日期
		String tmstmp =DateTools2.getCurrentTimestamp();
			
		//如果是当天第一笔交易则更新上日余额及余额更新时间
		
		BigDecimal lastbl = acct.getLastbl();
		String upbldt = acct.getUpbldt();
		
		//acctdt = ApDCN.getAccountDateOneDCN(acct.getAcctno(), trandt, acct.getUpbldt());
			
		//如果是当天第一笔交易则更新上日余额及余额更新时间
		if (CommUtil.compare(trandt, upbldt) > 0) {
			/* 过账:更新余额最新更新日期 */
			lastbl = onbal;
			upbldt = trandt;
		}
		
		// 如果当前余额为0，则余额更新日期不改变
		if (CommUtil.compare(onbal, BigDecimal.ZERO) == 0) {
			upbldt = acct.getUpbldt();
		}
		
		DpAcctDao.updKnaAcctClose(transq, trandt, E_DPACST.CLOSE, BigDecimal.ZERO, lastbl, upbldt, acct.getAcctno(), acct.getCorpno(),tmstmp);
		DpAcctDao.updKnaAccsClose(E_DPACST.CLOSE, acct.getAcctno(), acct.getCorpno(),tmstmp);
		
		
		// 定期附加产品信息表
		KnaAcctProd tblKnaAProd = KnaAcctProdDao.selectOneWithLock_odb1(acct.getAcctno(), true);
		
		long detlsq = ConvertUtil.toLong(CommUtil.nvl(tblKnaAProd.getObgatw(),0)) + 1 ;// 明细序号
		
		// 更新明细序号
		tblKnaAProd.setObgatw(Long.toString(detlsq));// 明细序号
		KnaAcctProdDao.updateOne_odb1(tblKnaAProd);
		
		//登记余额明细
		KnlBill bill = SysUtil.getInstance(KnlBill.class);
		
		bill.setAcctbl(BigDecimal.ZERO);
		bill.setAcctna(acct.getAcctna());
		bill.setAcctno(acct.getAcctno());
		bill.setBgindt(acct.getBgindt());
		bill.setCustac(acct.getCustac());
		bill.setOpenbr(acct.getBrchno()); //账户开户机构
		bill.setTranam(acct.getOnlnbl()); //交易金额
		bill.setTrancy(acct.getCrcycd());
		bill.setDetlsq(detlsq); //明细序号
		bill.setCorpno(acct.getCorpno());
		DpCloseAcctno.insKnlBill(bill, clsin);
		
    	//记账
		//借个人存款本金科目
    	if (CommUtil.compare(onbal, BigDecimal.ZERO) != 0) {
			
			/* 应入账日期 */
			IoAccounttingIntf account = SysUtil.getInstance(IoAccounttingIntf.class);

			account.setCuacno(acct.getCustac());
			account.setAcctno(acct.getAcctno());
//			account.setAcseno(acct.get());
			account.setProdcd(acct.getProdcd());
			account.setDtitcd(acct.getAcctcd());
			account.setCrcycd(BusiTools.getDefineCurrency());
			account.setTranam(onbal);
			account.setAcctdt(acctdt); // 应入账日期
			account.setTransq(transq);
			account.setMntrsq(mntrsq);
			account.setTrandt(trandt);
			account.setAcctbr(brchno);
			account.setAmntcd(E_AMNTCD.DR); // 借方
			account.setAtowtp(E_ATOWTP.DP); // 存款
			account.setTrsqtp(E_ATSQTP.ACCOUNT); // 账务流水
			account.setBltype(E_BLTYPE.BALANCE); // 余额属性：本金科目
			
			// 登记交易信息，供总账解析
			if (CommUtil.equals(
					"1",
					KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
							true).getParm_value1())) {
				KnpParameter para = SysUtil.getInstance(KnpParameter.class);
				para = KnpParameterDao.selectOne_odb1("GlAnalysis", "1020000", "%",
						"%", true);
				account.setTranms(para.getParm_value1());// 登记交易信息
			}				
			SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(account);
		}
    	
//    	CommTools.getBaseRunEnvs().setBusi_org_id(corpno);

		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
		//获得电子账号、卡号
		//冲正注册
		cplInput.setCustac(acct.getCustac());
		cplInput.setEvent1(acct.getAcctcd()); 
		cplInput.setEvent2(trandt);
		cplInput.setEvent3(lastbl.toString()); //上日余额
		cplInput.setEvent4(upbldt); //上次余额更新日期
		cplInput.setEvent5(acct.getProdcd());
		cplInput.setEvent6(transq); //add 2017/1/12 songlw 销户流水
		cplInput.setEvent7(acct.getCorpno()); //add 2017/02/14 法人代码  用于冲正knaAccs
		cplInput.setTranac(acct.getAcctno());
		cplInput.setTranam(onbal);
		cplInput.setTranno(detlsq);
		cplInput.setCrcycd(acct.getCrcycd());
		
		cplInput.setAmntcd(E_AMNTCD.DR);
		
		cplInput.setTranev(ApUtil.TRANS_EVENT_CLSACT);

		//ApStrike.regBook(cplInput);
		IoMsRegEvent input = SysUtil.getInstance(IoMsRegEvent.class);    		
		input.setReversal_event_id(ApUtil.TRANS_EVENT_CLSACT);
		input.setInformation_value(SysUtil.serialize(cplInput));
		MsEvent.register(input, true);
    	
    	return onbal;
	}
	/**
	 * @Title: strikeCloseAcctno 
	 * @Description:  结算户销户冲正
	 * @param strike
	 * @author zhangan
	 * @date 2016年11月2日 上午10:01:58 
	 * @version V2.3.0
	 */
	public static void strikeCloseAcctno(IoApRegBook strike, E_COLOUR colour){
		
		String acctcd = strike.getEvent1();
		String trandt = strike.getEvent2();
		BigDecimal lastbl = ConvertUtil.toBigDecimal(strike.getEvent3());
		String upbldt = strike.getEvent4();
		String acctno = strike.getTranac();
		BigDecimal onlbal = strike.getTranam();
		String prodcd = strike.getEvent5();
		long detlsq = strike.getTranno();
		String custac = strike.getCustac();
		String crcycd = strike.getCrcycd();
		String tmstmp = DateTools2.getCurrentTimestamp();
		/**
         * 冲正检查
         */
		String acseno = "";
		
		String corpno = strike.getEvent7();
		
		//冲正金额不为0 ，则不处理账单
		if(CommUtil.compare(onlbal, BigDecimal.ZERO) != 0){
			
			KnlBill bill = KnlBillDao.selectOne_odb1(acctno, detlsq, trandt, true);
			KnlBill billdata = SysUtil.getInstance(KnlBill.class);
			
			if (CommUtil.isNull(bill)) {
				HKnlBill h_bill = HKnlBillDao.selectOne_odb1(acctno, detlsq, trandt, true);
				h_bill.setPmcrac(E_PMCRAC.TOSTRIK);
				h_bill.setStacps(E_STACPS.POSITIVE);// 冲正冲账分类
				h_bill.setCorrtg(E_CORRTG._1);// 设置抹账标志
				HKnlBillDao.updateOne_odb1(h_bill);
				
				acseno = h_bill.getAcseno();
				corpno = h_bill.getCorpno();
				
				//在账单表中插入冲正入账信息
				//借贷方向不变  金额取负数  余额加交易金额
				BigDecimal tranam = h_bill.getTranam().negate();//金额取反
				BigDecimal acctbl = h_bill.getAcctbl().add(h_bill.getTranam());//余额加交易金额
				
				billdata.setAcctbl(acctbl);//账户余额
				billdata.setAcctna(h_bill.getAcctna());//账户名称
				billdata.setAcctno(h_bill.getAcctno());//负债账号
				billdata.setAcseno(h_bill.getAcseno());//子账户序号
				billdata.setAmntcd(h_bill.getAmntcd());//借贷标志
				billdata.setBankcd(h_bill.getBankcd());//对方金融机构
				billdata.setBankna(h_bill.getBankna());//对方金融机构名称
				billdata.setBgindt(h_bill.getBgindt());//起息日期
				billdata.setCardno(h_bill.getCardno());//卡号
				billdata.setCstrfg(h_bill.getCstrfg());//现转标志
				billdata.setCkaccd(h_bill.getCkaccd());//对账代码
				billdata.setCorrtg(h_bill.getCorrtg());//抹账标志
				billdata.setCsextg(h_bill.getCsextg());//账户钞汇标志
				billdata.setCustac(h_bill.getCustac());//客户账号
				billdata.setDcbtno(h_bill.getDcbtno());//凭证批号
				billdata.setDcmttp(h_bill.getDcmttp());//凭证种类
				billdata.setDcsrno(h_bill.getDcsrno());//凭证序号
				billdata.setIntrcd(BusiTools.getBusiRunEnvs().getLttscd());//内部交易吗
				billdata.setMachdt(DateUtil.getNow());//主机日期
				
				billdata.setOpacna(h_bill.getOpacna());//对方户名
				billdata.setOpcuac(h_bill.getOpcuac());//对方客户账号
				billdata.setOpenbr(h_bill.getOpenbr());//账户开户机构
				billdata.setOppomk(h_bill.getOppomk());//对方备注
				
				billdata.setRemark(BusiTools.getBusiRunEnvs().getRemark());//备注
				billdata.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道
				
				//冲正交易
				if (CommUtil.compare(CommTools.getBaseRunEnvs().getTrxn_date(), h_bill.getTrandt()) == 0){
					billdata.setStrktp(E_STRKTP.TODAY);
				}else{
					billdata.setStrktp(E_STRKTP.LAST);
				}
				billdata.setPmcrac(E_PMCRAC.STRIK);
				billdata.setCorrtg(E_CORRTG._0);
				billdata.setOrigtq(h_bill.getTransq());//原主交易流水
				billdata.setOrigpq(h_bill.getProcsq());//原业务流水
				billdata.setMsacsq(h_bill.getUssqno());//原柜员流水
				billdata.setMsacdt(h_bill.getTrandt());//原错账日期
				
				billdata.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());//摘要代码
				billdata.setSmryds(ApSmryTools.getText(BusiTools.getBusiRunEnvs().getSmrycd()));//摘要描述
				billdata.setStacps(h_bill.getStacps());//冲正冲账分类
				billdata.setTranam(tranam);//交易金额
				
				if (CommUtil.isNotNull(CommTools.getBaseRunEnvs().getTrxn_branch())) {//交易机构
					billdata.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());
				} else {
					billdata.setTranbr(BusiTools.getBusiRunEnvs().getCentbr());
				}
				
				billdata.setTrancy(h_bill.getTrancy());//交易币种
				billdata.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
				billdata.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
				billdata.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());//交易时间
				billdata.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());//操作柜员
				billdata.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq());//柜员流水号
				billdata.setAuthus(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); //授权柜员串
				billdata.setAuthsq(BusiTools.getBusiRunEnvs().getAuthvo().getAuthsq()); //授权流水
				
			} else {
				
				bill.setPmcrac(E_PMCRAC.TOSTRIK);// 冲补账标志
				bill.setStacps(E_STACPS.POSITIVE);// 冲正冲账分类
				bill.setCorrtg(E_CORRTG._1);// 设置抹账标志
				KnlBillDao.updateOne_odb1(bill);
				
				acseno = bill.getAcseno();
				corpno = bill.getCorpno();
				
				//在账单表中插入冲正入账信息
				//借贷方向不变  金额取负数  余额加交易金额
				BigDecimal tranam = BigDecimal.ZERO .subtract(bill.getTranam());//金额取负数
				BigDecimal acctbl = bill.getAcctbl().add(bill.getTranam());//余额加交易金额
				
				billdata.setAcctbl(acctbl);//账户余额
				billdata.setAcctna(bill.getAcctna());//账户名称
				billdata.setAcctno(bill.getAcctno());//负债账号
				billdata.setAcseno(bill.getAcseno());//子账户序号
				billdata.setAmntcd(bill.getAmntcd());//借贷标志
				billdata.setBankcd(bill.getBankcd());//对方金融机构
				billdata.setBankna(bill.getBankna());//对方金融机构名称
				billdata.setBgindt(bill.getBgindt());//起息日期
				billdata.setCardno(bill.getCardno());//卡号
				billdata.setCstrfg(bill.getCstrfg());//现转标志
				billdata.setCkaccd(bill.getCkaccd());//对账代码
				billdata.setCorrtg(bill.getCorrtg());//抹账标志
				billdata.setCsextg(bill.getCsextg());//账户钞汇标志
				billdata.setCustac(bill.getCustac());//客户账号
				billdata.setDcbtno(bill.getDcbtno());//凭证批号
				billdata.setDcmttp(bill.getDcmttp());//凭证种类
				billdata.setDcsrno(bill.getDcsrno());//凭证序号
				//billdata.setDetlsq(bill.getDetlsq());//明细序号
				
				
				billdata.setIntrcd(BusiTools.getBusiRunEnvs().getLttscd());//内部交易吗
				billdata.setMachdt(DateUtil.getNow());//主机日期
				
				billdata.setOpacna(bill.getOpacna());//对方户名
				billdata.setOpcuac(bill.getOpcuac());//对方客户账号
				billdata.setOpenbr(bill.getOpenbr());//账户开户机构
				billdata.setOppomk(bill.getOppomk());//对方备注
				
				billdata.setRemark(BusiTools.getBusiRunEnvs().getRemark());//备注
				billdata.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道
				
				//冲正交易
				if (CommUtil.compare(CommTools.getBaseRunEnvs().getTrxn_date(), bill.getTrandt()) == 0){
					billdata.setStrktp(E_STRKTP.TODAY);
				}else{
					billdata.setStrktp(E_STRKTP.LAST);
				}
				billdata.setPmcrac(E_PMCRAC.STRIK);
				billdata.setCorrtg(E_CORRTG._0);
				billdata.setOrigtq(bill.getTransq());//原主交易流水
				billdata.setOrigpq(bill.getProcsq());//原业务流水
				billdata.setMsacsq(bill.getUssqno());//原柜员流水
				billdata.setMsacdt(bill.getTrandt());//原错账日期
				
				billdata.setSmrycd(BusinessConstants.SUMMARY_XH);//摘要代码
				billdata.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_XH));//摘要描述
				billdata.setStacps(bill.getStacps());//冲正冲账分类
				billdata.setTranam(tranam);//交易金额
				billdata.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());//交易机构
				billdata.setTrancy(bill.getTrancy());//交易币种
				billdata.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
				billdata.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
				billdata.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());//交易时间
				billdata.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());//操作柜员
				billdata.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq());//柜员流水号
				billdata.setAuthus(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); //授权柜员串
				billdata.setAuthsq(BusiTools.getBusiRunEnvs().getAuthvo().getAuthsq()); //授权流水
				//billdata.setCkaccd(CommTools.getBaseRunEnvs().getCptrcd()); //对账代码    从公共运行变量中取
				
				
				
			}
			
			// 定期附加产品信息表
			KnaAcctProd tblKnaAProd = KnaAcctProdDao.selectOneWithLock_odb1(acctno, true);
			
			long new_detlsq = ConvertUtil.toLong(CommUtil.nvl(tblKnaAProd.getObgatw(),0)) + 1 ;// 明细序号
			
			// 更新明细序号
			tblKnaAProd.setObgatw(Long.toString(new_detlsq));// 明细序号
			KnaAcctProdDao.updateOne_odb1(tblKnaAProd);
			
			billdata.setDetlsq(new_detlsq);//明细序号
			//插入账单表
			KnlBillDao.insert(billdata);
			
		}
        
        
        DpAcctDao.updKnaAcctClose("", "", E_DPACST.NORMAL, onlbal, lastbl, upbldt, acctno, corpno,tmstmp);
        DpAcctDao.updKnaAccsClose(E_DPACST.NORMAL, acctno, corpno,tmstmp);
        
        E_AMNTCD amntcd = strike.getAmntcd();
        
        // 同向红字记账
        if (E_COLOUR.RED == colour) {
        	onlbal = onlbal.negate();// 交易金额
        } else if (E_COLOUR.BLUE == colour) {
            // 反向蓝字记
            if (amntcd == E_AMNTCD.CR) {
                amntcd = E_AMNTCD.DR; // 借贷标志
            } else {
                amntcd = E_AMNTCD.CR;// 借贷标志
            }
        } else {
            throw DpModuleError.DpstAcct.BNAS1756(colour);
        }
        /**
         * 登记会计流水
         */
        KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(acctno, true);
        
        IoAccounttingIntf cplIoAccounttingIntf = SysUtil
                .getInstance(IoAccounttingIntf.class);
        cplIoAccounttingIntf.setCuacno(custac);
        cplIoAccounttingIntf.setAcctno(acctno);
        cplIoAccounttingIntf.setAcseno(acseno);
        cplIoAccounttingIntf.setProdcd(prodcd);
        cplIoAccounttingIntf.setDtitcd(acctcd);
        cplIoAccounttingIntf.setCrcycd(crcycd);
        cplIoAccounttingIntf.setTranam(onlbal);
        cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
        cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
        cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
        cplIoAccounttingIntf.setAcctbr(tblKnaAcct.getBrchno());
        cplIoAccounttingIntf.setAmntcd(amntcd);
        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
        cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE);
        //登记交易信息，供总账解析
        if(CommUtil.equals("1", KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",true).getParm_value1())){
        	KnpParameter para = SysUtil.getInstance(KnpParameter.class);
        	para = KnpParameterDao.selectOne_odb1("GlAnalysis", "1020000", "%", "%",true);
        	cplIoAccounttingIntf.setTranms(para.getParm_value1());//登记交易信息 20160701   产品增加            	
        }   
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                cplIoAccounttingIntf);
        
	}
	
	public static void insKnlBill(KnlBill bill, IoDpCloseIN clsin){
		//登记余额明细，登记付息明细
		
		if(CommUtil.compare(bill.getTranam(), BigDecimal.ZERO) != 0){
			//KnlBill bill = SysUtil.getInstance(KnlBill.class);
			//bill.setAcctbl(acct.getOnlnbl());
			//bill.setAcctna(acct.getAcctna());
			//bill.setAcctno(acct.getAcctno());
			//bill.setAcseno(acct.);
			bill.setAmntcd(E_AMNTCD.DR);
			bill.setAuthsq(BusiTools.getBusiRunEnvs().getAuthvo().getAuthsq());
			bill.setAuthus(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus());
			//bill.setBankcd(CommTools.getBaseRunEnvs().getb); //对方金融代码
			//bill.setBankna(bankna); //对方金融名称
			//bill.setBgindt(acct.getBgindt());
			bill.setCardno(clsin.getCardno()); //卡号
			bill.setCstrfg(E_CSTRFG.TRNSFER);
			//bill.setCkaccd(ckaccd); //对账代码
			//bill.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
			bill.setCorrtg(E_CORRTG._0);
			bill.setCsextg(E_CSEXTG.CASH);
			//bill.setCustac(acct.getCustac());
			//bill.setDcbtno(""); //凭证批号
			//bill.setDcmttp(""); //凭证种类
			//bill.setDcsrno(dcsrno); //凭证序号
			//bill.setDetlsq(Long.parseLong(SequenceManager.nextval("KnlBill"))); //明细序号
			bill.setIntrcd(BusiTools.getBusiRunEnvs().getLttscd()); //内部交易码
			bill.setMachdt(DateUtil.getNow()); //主机日期
			//bill.setMsacdt(msacdt); //错账原日期
			//bill.setMsacsq(msacsq); //错账原流水
			
			bill.setOpacna(clsin.getToname()); //对方户名
			bill.setOpbrch(clsin.getTobrch()); //对方机构
			bill.setOpcuac(clsin.getToacct()); //对方客户账号
			
			//bill.setOpenbr(acct.getBrchno()); //账户开户机构
			//bill.setOppomk(oppomk); //对方备注
			//bill.setOrigpq(origpq); //原业务流水
			//bill.setOrigtq(origtq); //原主交易流水
			//bill.setPmcrac(pmcrac); //被冲正标志
			bill.setProcsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //业务流水
			//bill.setRemark(remark);
			bill.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); //渠道
			bill.setSmrycd(clsin.getSmrycd()); //摘要代码
			if(CommUtil.isNotNull(clsin.getSmryds())){
				bill.setSmryds(clsin.getSmryds()); //摘要描述
			}else {
				if(CommUtil.isNotNull(clsin.getSmrycd())){
					bill.setSmryds(ApSmryTools.getText(clsin.getSmrycd()));
				}		
			}
			//bill.setStacps(stacps); //冲正冲账分类
			//bill.setStrktp(strktp); //冲正标志
			//bill.setTranam(acct.getOnlnbl()); //交易金额
			bill.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());
			//bill.setTrancy(acct.getCrcycd());
			bill.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
			bill.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());
			bill.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			bill.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());
			bill.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq());
			if(CommUtil.isNotNull(clsin.getRemark())){
				bill.setRemark(clsin.getRemark()); //交易备注
			}else {
				bill.setRemark(BusiTools.getBusiRunEnvs().getRemark());
			}
			KnlBillDao.insert(bill);
		}
		
	}
	
	
	public static void insKnbPidl(KnbAcin acin, BigDecimal onbal, BigDecimal interest, List<KubInrt> lsinrt, E_ACSETP acsetp){
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String edindt = CommTools.getBaseRunEnvs().getTrxn_date();
		if(CommUtil.isNotNull(acin.getEdindt())){
		    edindt=acin.getEdindt();
		}
		for(KubInrt inrt : lsinrt){
			KnbPidl pidl = SysUtil.getInstance(KnbPidl.class);
	        pidl.setAcctno(acin.getAcctno()); //负债账号
	        pidl.setAcsetp(acsetp);
	        pidl.setIntrtp(E_INTRTP.ZHENGGLX); //利息类型
	        //Long detlsq = Long.parseLong(SequenceManager.nextval("KnbPidl"));
	        Long detlsq = Long.parseLong(CoreUtil.nextValue("KnbPidl"));
	        pidl.setIndxno(detlsq); //顺序号
	        pidl.setDetlsq(detlsq); //明细序号
	        pidl.setIndlst(E_INDLST.YOUX); //付息明细状态
	        pidl.setLsinoc(E_INDLTP.PYIN); //上次利息操作代码
	        pidl.setInstdt(acin.getBgindt()); //计息起始日期
	        pidl.setIneddt(edindt); //计息终止日期
	        pidl.setIntrcd(acin.getIntrcd()); //利率编号
	        pidl.setIncdtp(acin.getIncdtp()); //利率代码类型
	        pidl.setLyinwy(acin.getLyinwy()); //分层计息方式
	        pidl.setIntrwy(acin.getIntrwy()); //利率靠档方式
	        pidl.setLvamot(BigDecimal.ZERO); //分层金额
	        pidl.setLvindt(null); //层次利率存期
	        pidl.setGradin(BigDecimal.ZERO); //档次计息余额
	        pidl.setTotlin(onbal); //总计息余额
	        pidl.setAcmltn(acin.getCutmam()); //积数
	        pidl.setTxbebs(acin.getTxbebs()); //计息基础
	        pidl.setRlintr(interest); //实际利息发生额
	        pidl.setRlintx(BigDecimal.ZERO); //实际利率税发生额
	        pidl.setIntrdt(trandt); //计息日期
	        pidl.setIntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //计息流水
	        pidl.setPyindt(trandt); //付息流水
	        pidl.setPyinsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //付息流水
	        pidl.setRemark(""); //靠档天数
        
        	pidl.setAcbsin(inrt.getBsintr());
        	pidl.setCuusin(inrt.getCuusin());
        	KnbPidlDao.insert(pidl);
        }
        
	}
}


