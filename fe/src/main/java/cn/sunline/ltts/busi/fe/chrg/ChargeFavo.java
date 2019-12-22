package cn.sunline.ltts.busi.fe.chrg;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcbFavoBddl;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcbFavoBddlDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpDime;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPlex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPlexDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPljo;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdl;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSpex;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgJhyhbl;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgLjyhbl;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgMaxCgprRate;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgSfyhbl;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.CgPreDiInfo;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_RELVFG;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_CJSIGN;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_FASTTP;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_WAYTYP;

/**
 * <p>
 * 文件功能说明：费用优惠
 * </p>
 * @author songliangwei
 *
 */
public class ChargeFavo {

	private static final BizLog bizlog = BizLogUtil.getBizLog(ChargeFavo.class);
    
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：单一优惠
	 *         </p>
	 * @param @param cplCgprRate
	 * @param @return
	 * @return CgSfyhbl
	 * @throws
	 */
	public static CgSfyhbl getMaxSfyh(CgMaxCgprRate cplCgprRate) {
		bizlog.method("getMaxSfyh begin >>>>>>>>>>>>>>>>>>>>>>");
		bizlog.parm("cplCgprRate[%s]", cplCgprRate);

		CgSfyhbl tblCgprRate = SysUtil.getInstance(CgSfyhbl.class);
		tblCgprRate.setFavoir(BigDecimal.ZERO); // 初始化优惠比例0.00
		tblCgprRate.setExpmsg("-"); //备注

		String sChrgcd = cplCgprRate.getChrgcd(); 
		Long lTranct = cplCgprRate.getAmount();//交易笔数
		BigDecimal bigTranam = cplCgprRate.getTranam();//交易金额
		String tranbr = cplCgprRate.getTranbr(); //交易机构
		String sTrandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		String sTrackRule = "";
		List<CgPreDiInfo> lstcplCgprRate = cplCgprRate.getWdinfo();
		String sFarendma = CommTools.getBaseRunEnvs().getBusi_org_id();
		List<KcpFavoSpex> lstList = null;
		
		if(CommUtil.isNull(sChrgcd)){
			throw FeError.Chrg.BNASF076();
		}
		
		String ctcono = CommTools.getBaseRunEnvs().getCenter_org_id();
		String centbr = BusiTools.getBusiRunEnvs().getCentbr();		
		//add 2016/12/23 songlw 增加县辖维护控制
		if(E_CJSIGN.YES == cplCgprRate.getCjsign()){
			
			//1、根据基础信息查找优惠解析表
			lstList = FeDiscountDao.selFavoSpex(sFarendma, sChrgcd, tranbr,BusiTools.getDefineCurrency(), sTrandt, false);
			
			if(lstList.size() <= 0){
				
				lstList = FeDiscountDao.selFavoSpex(ctcono, sChrgcd, centbr, BusiTools.getDefineCurrency(), sTrandt, false);
			}
		}else{
			
			lstList = FeDiscountDao.selFavoSpex(ctcono, sChrgcd, centbr, BusiTools.getDefineCurrency(), sTrandt, false);

		}
		
		if(CommUtil.isNull(lstList)){
			bizlog.debug("费种代码[%s]，未找到对应单一优惠记录", sChrgcd);
		}else{
			//2、遍历优惠解析对象，根据维度类型查找最大优惠的维度及优惠比例
			for (KcpFavoSpex tblPref : lstList) {	
				
				bizlog.debug("单一优惠设定维度列表lstList[%s]", lstList);
				
				if (E_FASTTP.AMT == tblPref.getFasttp()) {
					if (CommUtil.compare(bigTranam, tblPref.getFastam()) < 0)
						continue;
				}
				else if (E_FASTTP.NUM == tblPref.getFasttp()) {
					if (CommUtil.compare(BigDecimal.valueOf(lTranct), tblPref.getFastam()) < 0)
						continue;
				}
				else {
					continue;
				}
	
				String sYouhuifl = tblPref.getFatype();  //优惠类型->维度类型
				sTrackRule = "起点(" + sYouhuifl + ",";
				String favalu = tblPref.getFavalu();
				
				bizlog.debug("优惠维度列表lstcplCgprRate[%s]", lstcplCgprRate);
				
				for(CgPreDiInfo preInfo : lstcplCgprRate){
					String dimecg = preInfo.getDimecg(); //优惠维度类别
					String dimevl = preInfo.getDimevl(); //优惠维度值
					bizlog.debug("费用维度类别dimecg[%s]", dimecg);
					bizlog.debug("费用维度值dimevl[%s]", dimevl);
					bizlog.debug("优惠维度类型sYouhuifl[%s]", sYouhuifl);
					bizlog.debug("优惠维度值favalu[%s]", favalu);
					if(CommUtil.compare(sYouhuifl, dimecg) == 0){
						if(CommUtil.compare(favalu, dimevl) == 0 ){
							 
							sTrackRule = dimecg + "=" + dimevl;
							
							if(CommUtil.compare((tblPref.getFavoir().divide(new BigDecimal("100"))), tblCgprRate.getFavoir()) >=0 )
							{
								bizlog.debug("单一优惠最终优惠比tblPref.getFavoir()[%s]", tblPref.getFavoir());
								tblCgprRate.setFavoir(tblPref.getFavoir().divide(new BigDecimal("100")));
								tblCgprRate.setExpmsg(sTrackRule);
							}
						}else{
							continue;
						}
					}else{
						continue;
					}
				}
					
				bizlog.parm("tblCgprRate[%s]", tblCgprRate);
			}
		}
	
		bizlog.method("getMaxSfyh end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

		return tblCgprRate;
	}
	
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：超额优惠
	 *         </p>
	 * @param @param cplCgsmInput
	 * @param @return
	 * @return CgLjyhbl
	 * @throws
	 */
	public static CgLjyhbl prcLjyhbl(CgMaxCgprRate cplCgsmInput) {
		bizlog.method("prcLjyhbl begin >>>>>>>>>>>>>>>>>>>>>>");
		bizlog.parm("cplCgsmInput[%s]", cplCgsmInput);

		String sChrgcd = cplCgsmInput.getChrgcd(); // 费种代码
		Long lTranct = cplCgsmInput.getAmount();//交易笔数
		BigDecimal bigTranam = cplCgsmInput.getTranam();//交易金额
		String sTrandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		String sCustno = cplCgsmInput.getCustno(); //客户号
		String sAcctno = cplCgsmInput.getCustac(); //客户账号
		
		String ctcono = CommTools.getBaseRunEnvs().getCenter_org_id();//中心法人代码

		CgLjyhbl tblCgsmRate = SysUtil.getInstance(CgLjyhbl.class); // 累积优惠信息
		tblCgsmRate.setFavoir(BigDecimal.ZERO);
		tblCgsmRate.setExpmsg("-");
		tblCgsmRate.setSmfacd(null);

		List<KcpFavoSmex> lstLjyhjx = new ArrayList<KcpFavoSmex>();
		String sFarendma = CommTools.getBaseRunEnvs().getBusi_org_id();
		try {
			
			//add 2016/12/23 songlw 增加县辖维护控制
			if(E_CJSIGN.YES == cplCgsmInput.getCjsign()){
				lstLjyhjx = FeDiscountDao.selFavoSmexByChrgcdCrcycd(sFarendma, sChrgcd, BusiTools.getDefineCurrency(), sTrandt, false);
				
				//add 若本机构未设置超额优惠 则使用省联社统一设置的超额优惠信息
				if(lstLjyhjx.size() <= 0){
					lstLjyhjx = FeDiscountDao.selFavoSmexByChrgcdCrcycd(ctcono, sChrgcd, BusiTools.getDefineCurrency(), sTrandt, false);
				}
				
			}else{
				
				lstLjyhjx = FeDiscountDao.selFavoSmexByChrgcdCrcycd(ctcono, sChrgcd, BusiTools.getDefineCurrency(), sTrandt, false);
			
			}
		}catch (Exception e) {
			bizlog.debug("未找到累计优惠解析表记录", e);
			}
		
		bizlog.debug("累积优惠解析列表lstLjyhjx[%s]", lstLjyhjx);
		
		int iFlag = 1;
		String sFadmtp = null;
		String sDimevl = null;
		List<CgPreDiInfo> lstcplCgprRate = cplCgsmInput.getWdinfo();
		for (KcpFavoSmex tbTmp : lstLjyhjx) {
			sFadmtp = tbTmp.getFadmtp(); //维度类型
			sDimevl = tbTmp.getDimevl(); //维度值
			bizlog.debug("累积优惠维度类型sFadmtp[%s]", sFadmtp);
			bizlog.debug("累积优惠维度类型sDimevl[%s]", sDimevl);
			
			KcpDime tblWdmx = FeDimeDao.selkcp_scev_dimeOne_odb1(E_WAYTYP.PRIVILEGE_FEE, sFadmtp, false);
			
			if(CommUtil.isNull(tblWdmx)){
				throw FeError.Chrg.BNASF357();
			}

			for(CgPreDiInfo preDimeInfo : lstcplCgprRate){
				if(CommUtil.compare(sFadmtp, preDimeInfo.getDimecg()) == 0){
					if(CommUtil.compare(sDimevl, preDimeInfo.getDimevl()) != 0 
							&& CommUtil.compare(sDimevl, "%") != 0){
						continue;
					}else{
						iFlag = 0;
					}
					break;
				}
				
			}
			bizlog.debug("iFlag[%s]", iFlag);
			
			if (iFlag == 0) {
				List<KcpFavoSmex> lstCgsms;
				try {
					
					//add 2016/12/23 songlw 增加县辖维护控制
					if(E_CJSIGN.YES == cplCgsmInput.getCjsign()){
						lstCgsms = FeDiscountDao.selFavoSemx(sFarendma, sChrgcd, sFadmtp, BusiTools.getDefineCurrency(), sTrandt, false);
						if(lstCgsms.size() <= 0){
							lstCgsms = FeDiscountDao.selFavoSemx(ctcono, sChrgcd, sFadmtp, BusiTools.getDefineCurrency(), sTrandt, false);
						}
						
					}else{
						lstCgsms = FeDiscountDao.selFavoSemx(ctcono, sChrgcd, sFadmtp, BusiTools.getDefineCurrency(), sTrandt, false);
					}
				}
				catch (Exception e) {
					bizlog.info("无对应的累计优惠记录", e);
					return tblCgsmRate;
				}

				for (KcpFavoSmex tblCgsmCgtp : lstCgsms) {
					KcpFavoSmdf tblCgsm = SysUtil.getInstance(KcpFavoSmdf.class);

					try {
						tblCgsm = FeDiscountDao.selKcp_favo_smdfOne_odb1(tblCgsmCgtp.getSmfacd(), true);
					}
					catch (Exception e) {
						bizlog.debug("收费类型[%]下指定的累积优惠代码[%s]不存在！", sChrgcd, tblCgsmCgtp.getSmfacd());
						continue;
					}

					if (CommUtil.compare(tblCgsm.getEfctdt(), sTrandt) > 0)
						continue;

					if (CommUtil.isNotNull(tblCgsm.getInefdt()) && tblCgsm.getInefdt().length() == 8 && CommUtil.compare(tblCgsm.getInefdt(), sTrandt) < 0)
						continue;

					// 根据累积笔数和金额获取最大优惠比例
					BigDecimal bigCgprrt = calLjyhbl(tblCgsm, sChrgcd, BusiTools.getDefineCurrency(), bigTranam, lTranct, sTrandt, sCustno, sAcctno);
					bizlog.debug("累积优惠比例bigCgprrt[%s]", bigCgprrt);

					if (CommUtil.isNull(bigCgprrt))
						continue;

					if (CommUtil.compare(bigCgprrt.divide(new BigDecimal("100")), tblCgsmRate.getFavoir()) > 0) {
						tblCgsmRate.setSmfacd(tblCgsm.getSmfacd());
						tblCgsmRate.setExpmsg(tblCgsm.getExplan());
						tblCgsmRate.setCrcycd(tblCgsmCgtp.getCrcycd());
						tblCgsmRate.setFavoir(bigCgprrt.divide(new BigDecimal("100")));
					}
				}

			}
		}

		bizlog.parm("tblCgsmRate[%s]", tblCgsmRate);
		bizlog.method("prcLjyhbl end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		return tblCgsmRate;

	}
	
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：累积交易信息,对应到不同档次下的优惠比例
	 *         </p>
	 * @param eJizhanbz
	 * @param cgsm
	 * @param sChrgcd
	 * @param sCrcycd
	 * @param bigTranam
	 * @param lTranct
	 * @param sTrandt
	 * @param sCustno
	 * @param sAcctno
	 * @throws
	 */
	public static BigDecimal calLjyhbl(KcpFavoSmdf cgsm, String sChrgcd, String sCrcycd, BigDecimal bigTranam, long lTranct, String sTrandt, String sCustno,
			String sAcctno) {

		bizlog.method("calLjyhbl begin >>>>>>>>>>>>>>>>>>>>>");
		bizlog.parm("cgsm[%s]sChrgcd[%s]sCrcycd[%s]bigTranam[%s]lTranct[%s]sTrandt[%s]sCustno[%s]sAcctno[%s]", cgsm, sChrgcd, sCrcycd, bigTranam, lTranct,
				sTrandt, sCustno, sAcctno);

		BigDecimal bigCgprrt = BigDecimal.ZERO;
		String sCurprd = null;
		BigDecimal bigChrgam = null;
		String sSmacct = null;

		switch (cgsm.getSmbdtp()) {
		case ACCTNO:
			sSmacct = sAcctno;
			break;
		case CUSTNO:
			sSmacct = sCustno;
			break;
		}

		switch (cgsm.getCgsmtp()) {
		case AMOUNT:
			bigChrgam = bigTranam;
			break;
		case TIMES:
			bigChrgam = BigDecimal.valueOf(lTranct);
			break;
		}

		switch (cgsm.getPdunit()) {
		case DAY:
			sCurprd = sTrandt;
			break;
		case MONTH:
			sCurprd = sTrandt.substring(0, 6);
			break;
		case SEASON:
			String sDangqy = sTrandt.substring(4, 6);
			if (CommUtil.compare(sDangqy, "01") == 0 || CommUtil.compare(sDangqy, "02") == 0 || CommUtil.compare(sDangqy, "03") == 0) {
				sCurprd = sTrandt.substring(0, 4) + "1";
			}
			else if (CommUtil.compare(sDangqy, "04") == 0 || CommUtil.compare(sDangqy, "05") == 0 || CommUtil.compare(sDangqy, "06") == 0) {
				sCurprd = sTrandt.substring(0, 4) + "2";
			}
			else if (CommUtil.compare(sDangqy, "07") == 0 || CommUtil.compare(sDangqy, "08") == 0 || CommUtil.compare(sDangqy, "09") == 0) {
				sCurprd = sTrandt.substring(0, 4) + "3";
			}
			else if (CommUtil.compare(sDangqy, "10") == 0 || CommUtil.compare(sDangqy, "11") == 0 || CommUtil.compare(sDangqy, "12") == 0) {
				sCurprd = sTrandt.substring(0, 4) + "4";
			}
			break;
		case YEAR:
			sCurprd = sTrandt.substring(0, 4);
			break;
		default:
			break;
		}

		KcbFavoBddl tblSbct = SysUtil.getInstance(KcbFavoBddl.class);

//		tblSbct = Kcb_favo_bddlDao.selectOneWithLock_odb1(sChrgcd, eCrcycd, cgsm.getSmfacd(), sSmacct, sCurprd, false);  //withLock
		tblSbct = FeDiscountDao.selKcb_favo_bddlOne_odb1(sChrgcd, sCrcycd, cgsm.getSmfacd(), sSmacct, sCurprd, false);
		
		bizlog.debug("累积主体明细tblSbct[%s]", tblSbct);

		if (CommUtil.isNull(tblSbct)) {
			KcbFavoBddl tblSbctNew = SysUtil.getInstance(KcbFavoBddl.class);
			tblSbctNew.setChrgcd(sChrgcd);
			tblSbctNew.setCrcycd(sCrcycd);
			tblSbctNew.setSmfacd(cgsm.getSmfacd());
			tblSbctNew.setSmbody(sSmacct);
			tblSbctNew.setChrgpd(sCurprd);
			tblSbctNew.setSmfavl(bigChrgam);
			tblSbctNew.setEndate(sTrandt);
			
			KcbFavoBddlDao.insert(tblSbctNew);
	
			tblSbct = tblSbctNew;
		}
		else {
			tblSbct.setSmfavl(tblSbct.getSmfavl().add(bigChrgam));
			tblSbct.setEndate(sTrandt);

			KcbFavoBddlDao.updateOne_odb1(tblSbct);
		}

		bigChrgam = tblSbct.getSmfavl();
		
		bizlog.debug("[%s]累积优惠[%s]", sSmacct, bigChrgam);
		
		List<KcpFavoSmdl> lstDetls = new ArrayList<KcpFavoSmdl>();
		try {
			//以累积起点优惠值order by desc，起点优惠值由大到小
			lstDetls = FeDiscountDao.selFavoSmdl(cgsm.getSmfacd(), bigChrgam, sTrandt, true);
		}
		catch (Exception e) {
			bizlog.debug("无对应的累积优惠优惠信息");
		}

		bizlog.debug("累积优惠明细列表lstDetls[%s]", lstDetls);
		if (CommUtil.isNotNull(lstDetls) && lstDetls.size() > 0) {
//			for (int i = 0; i < lstDetls.size(); ) {
				KcpFavoSmdl tblTmp = lstDetls.get(0);
				BigDecimal bigJieshangx = BigDecimal.ZERO;
				if (CommUtil.isNotNull(tblTmp.getSmblup())) {
					bigJieshangx = tblTmp.getSmblup();
				}
				bizlog.debug("累积金额上限bigJieshangx[%s]", bigJieshangx);
				if (CommUtil.compare(bigJieshangx, BigDecimal.ZERO) != 0) { //如果不为0,0表示未设置金额上限
					if (CommUtil.compare(bigJieshangx, bigChrgam) >= 0)// 当金额
					{
						bigCgprrt = tblTmp.getSmfapc();// 只取最靠近的一笔
					}
				}
				else {
					bigCgprrt = lstDetls.get(0).getSmfapc();
				}
//				break;

//			}

		}
		bizlog.parm("bigCgprrt[%s]", bigCgprrt);
		bizlog.method("calLjyhbl end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		return bigCgprrt;
	}
	
	
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：优惠计划
	 *         </p>
	 * @param @param cplncplInput
	 * @param @return
	 * @return CgJhyhbl
	 * @throws
	 */
	public static CgJhyhbl getMaxYhjh(CgMaxCgprRate cgprRateInput) {
		bizlog.method("getMaxYhjh begin >>>>>>>>>>>>>>>>>>>>>>");
		bizlog.parm("cplncplInput[%s]", cgprRateInput);
		String sChrgcd = cgprRateInput.getChrgcd(); //费种代码
		Long lTranct = cgprRateInput.getAmount();//交易笔数
		BigDecimal bigTranam = cgprRateInput.getTranam();//交易金额
		String sTrandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		String sBrchno = cgprRateInput.getTranbr(); //机构号
		CgJhyhbl tblNcplRate = SysUtil.getInstance(CgJhyhbl.class);
		tblNcplRate.setFavoir(BigDecimal.ZERO);// 初始化优惠比例0.00
		String centbr = BusiTools.getBusiRunEnvs().getCentbr();//省清算中心机构号 
		
		if (CommUtil.isNull(sChrgcd)) {
			throw FeError.Chrg.BNASF076();
		}

		if (CommUtil.isNull(sTrandt)) {
			throw FeError.Chrg.BNASF358();
		}
		
		List<CgPreDiInfo> lstcplCgprRate = cgprRateInput.getWdinfo();
		List<KcpFavoPlex> lstKcp_favo_plex = null;
		
		//add 2016/12/23 songlw 增加县辖维护控制
		if(E_CJSIGN.YES == cgprRateInput.getCjsign()){
			lstKcp_favo_plex = FeDiscountDao.selKcp_favo_plexAll_odb1(sBrchno, sChrgcd, BusiTools.getDefineCurrency(), sTrandt, false);
				
			if(lstKcp_favo_plex.size() <= 0){
				lstKcp_favo_plex = FeDiscountDao.selKcp_favo_plexAll_odb1(centbr, sChrgcd, BusiTools.getDefineCurrency(), sTrandt, false);
			}
		}else{
			lstKcp_favo_plex = FeDiscountDao.selKcp_favo_plexAll_odb1(centbr, sChrgcd, BusiTools.getDefineCurrency(), sTrandt, false);
		}
		
		for(KcpFavoPlex tblPlex : lstKcp_favo_plex){
			String sNcplcd = tblPlex.getDiplcd(); //优惠计划代码
			bizlog.debug("sNcplcd[%s]", sNcplcd);

			BigDecimal bigChprrt = tblPlex.getFavoir();
			BigDecimal bigChrgam1 = tblPlex.getFastam();

			if (E_FASTTP.AMT == tblPlex.getFasttp()) {
				if ((CommUtil.compare(bigTranam, bigChrgam1) < 0)){
					bizlog.debug("不符合规定的优惠起点");
					continue;
				}
				
			}
			else if (E_FASTTP.NUM == tblPlex.getFasttp()) {
				if (CommUtil.compare(BigDecimal.valueOf(lTranct), bigChrgam1) < 0){
					bizlog.debug("不符合规定的优惠起点");
					continue;
				}

			}
			else {
				bizlog.debug("未找到对应优惠计费类型");
				continue;
			}
			
			List<KcpFavoPljo> lstTmp = FeDiscountDao.selFavoPldf(sTrandt, sChrgcd, BusiTools.getDefineCurrency(), sBrchno, sNcplcd, false);
			
			bizlog.debug("lstTmp[%s]", lstTmp);
			
			boolean flag = false;
			int wNum = 0;
			int rNum = 0;
			
			for (KcpFavoPljo tblKnpmx : lstTmp) {
				String sDimecg = tblKnpmx.getDimecg(); //维度类别
				String sFadmvl = tblKnpmx.getFadmvl(); //维度值
				E_RELVFG relvfg = tblKnpmx.getRelvfg(); //关联标志
				
				bizlog.debug("优惠计划维度类别sDimecg:[%s]", sDimecg);
				bizlog.debug("优惠计划维度值sFadmvl:[%s]", sFadmvl);
				
				if(CommUtil.compare(E_RELVFG.RELEVN, relvfg) == 0){
					for (CgPreDiInfo preInfo : lstcplCgprRate) {
						
						bizlog.debug("优惠维度类别[%s]", preInfo.getDimecg());
						bizlog.debug("优惠维度值[%s]", preInfo.getDimevl());
						
						if (CommUtil.compare(sDimecg, preInfo.getDimecg()) == 0){
							
							if(CommUtil.compare(sFadmvl, preInfo.getDimevl()) != 0){
								
								flag = false;
								wNum ++;
								
							}else{
								
								flag = true;
								rNum ++;
							}
							
							break;
							
						}
						
						//若rNum说明未找到对应的维度类别，则返回false
						if(rNum == 0){
							flag = false;
						}
						
					}
				} else if (CommUtil.compare(E_RELVFG.NORELV, relvfg) == 0) {
					for (CgPreDiInfo preInfo : lstcplCgprRate) {
						
						bizlog.debug("优惠维度类别[%s]", preInfo.getDimecg());
						bizlog.debug("优惠维度值[%s]", preInfo.getDimevl());
						
						if (CommUtil.compare(sDimecg, preInfo.getDimecg()) == 0) {
							
							if (CommUtil.compare(sFadmvl, preInfo.getDimevl()) != 0) {

								flag = true;
								
							} else {

								flag = false;
								wNum ++;
								
							}
							
							break;
							
						}
					}
				}
			
				bizlog.debug("relvfg[%s]", relvfg);
				bizlog.debug("iFlag[%s]", flag);
				bizlog.debug("wNum[%s]", wNum);
				
				//若已有不符合条件则直接退出循环
				if(wNum > 0){
					break;
				}
				
			}
			bizlog.debug("iFlag[%s], wNum[%s]", flag, wNum);
			if(flag == true && wNum == 0){
				if (CommUtil.compare(bigChprrt.divide(new BigDecimal("100")), tblNcplRate.getFavoir()) > 0) {
					tblNcplRate.setFavoir(bigChprrt.divide(new BigDecimal("100")));
					tblNcplRate.setDiplcd(sNcplcd);
				}
			}
		}
		
		bizlog.parm("tblNcplRate[%s]", tblNcplRate);
		bizlog.method("getMaxYhjh end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		return tblNcplRate;

	}
	
	/**
	 * @Author L H
	 *         <p>
	 *         <li>2015年10月27日19:06:54</li>
	 *         <li>功能说明：根据费种代码和优惠计划代码查询优惠计划解析表</li>
	 *         </p>
	 * @param sChrgcd
	 *            费种代码
	 * @param sCrcycd
	 *            币种
	 * @param sNcplcd
	 *            优惠计划代码
	 * @return
	 */
	public static KcpFavoPlex getYhjhjx(String sChrgcd, String sCrcycd, String sNcplcd) {
		bizlog.method("getYhjhjx begin >>>>>>>>>>>>>>>>>>>>>");
		bizlog.parm("sChrgcd[%s]sCrcycd[%s]sNcplcd[%s]", sChrgcd, sCrcycd, sNcplcd);

		KcpFavoPlex lstTmp = SysUtil.getInstance(KcpFavoPlex.class);
		if (CommUtil.isNull(sCrcycd)) {
			throw FeError.Chrg.BNASF156();
		}

		if (CommUtil.isNull(sChrgcd)) {
			throw FeError.Chrg.BNASF076();

		}
		if (CommUtil.isNull(sNcplcd)) {
			throw FeError.Chrg.BNASF303();
		}
		try {
			lstTmp = KcpFavoPlexDao.selectOne_odb1(sChrgcd, sNcplcd, true);
		}
		catch (Exception e) {
			throw FeError.Chrg.BNASF359();

		}
		bizlog.parm("lstTmp[%s]", lstTmp);
		bizlog.method("getYhjhjx end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		return lstTmp;

	}
	
}
