package cn.sunline.ltts.busi.dp.acct;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.domain.DpAcctIntrEntity;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntr;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntrDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctAddt;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctAddtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDraw;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDrawDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacSort;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacSortDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdr;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdrDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxsv;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxsvDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSave;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSaveDao;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.AddSubAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkOpenAcctOut;
import cn.sunline.ltts.busi.iobus.type.pb.IoItpfComplexType.IoLayInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoItpfComplexType.IoLayInfoIn;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CUSTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRFLPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRRTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SPECTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FVRBFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_INTRTY;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;

/**
 * 负债子账户开立服务
 * */
public class OpenCurrAcctProc {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(OpenCurrAcctProc.class);
	
	public static String openCurrAcct(AddSubAcctIn cplAddSubAcctIn,ChkOpenAcctOut cplOpenAcctOut){
		E_TERMCD depttm = cplAddSubAcctIn.getDepttm();
		String acctac = cplAddSubAcctIn.getCustac();
		long deptdy = cplOpenAcctOut.getDeptdy(); //存期天数
		String acctno = null;
		
		//判断定活
		if(E_FCFLAG.CURRENT == cplOpenAcctOut.getPddpfg()){
			//活期就存至活期表
			acctno = OpenCurrAcctProc.addToCurrAcct(cplOpenAcctOut,cplAddSubAcctIn.getAcctna(),acctac,cplAddSubAcctIn.getCustno(),cplAddSubAcctIn.getProdcd(),cplAddSubAcctIn.getCusttp());
			//生成存入控制信息
			OpenCurrAcctProc.addSaveControlInfo(cplOpenAcctOut,acctno);
			//生成支取控制信息
			OpenCurrAcctProc.addDrawControlInfo(cplOpenAcctOut,acctno);
		} else if(E_FCFLAG.FIX == cplOpenAcctOut.getPddpfg() ){
			bizlog.debug("是否明细2======"+cplOpenAcctOut.getDetlfg());
			//定期就存到定期对应表
			acctno = OpenCurrAcctProc.addToFixAcct(cplOpenAcctOut, cplAddSubAcctIn.getAcctna(),acctac,depttm,deptdy,cplAddSubAcctIn.getCustno(),cplAddSubAcctIn.getProdcd(),cplAddSubAcctIn.getCusttp());
			//生成存入控制信息
			OpenCurrAcctProc.addSaveControlInfo1(cplOpenAcctOut,acctno);
			//生成支取控制信息
			OpenCurrAcctProc.addDrawControlInfo1(cplOpenAcctOut,acctno);
			
			//判断是否为明细汇总产品，如果是将负债账户最大序列号初始化
			if(E_YES___.YES == cplOpenAcctOut.getDetlfg()){
				addKnaFixaSort(acctno);
			}
			
		}
		bizlog.debug("存期======"+depttm);
		
		
		
		DpAcctIntrEntity entity = new DpAcctIntrEntity();
		entity.setAcctno(acctno);
		entity.setCrcycd(cplOpenAcctOut.getCrcycd());
		entity.setProdcd(cplAddSubAcctIn.getProdcd());
		entity.setOpenir(cplAddSubAcctIn.getOpenir());
		entity.setDepttm(depttm);
		entity.setPddpfg(cplOpenAcctOut.getPddpfg());
		entity.setDetlfg(cplOpenAcctOut.getDetlfg());
		entity.setDeptdy(deptdy); //存期天数
		entity.setTranam(cplAddSubAcctIn.getTranam()); //开户金额
		//账户利息
		addAcctIntr(entity);
		
		return acctno;
	}

	private static void addDrawControlInfo1(ChkOpenAcctOut acctOut,
			String acctno) {
		KnaFxdr tblKnaDraw = SysUtil.getInstance(KnaFxdr.class);
		tblKnaDraw.setAcctno(acctno);
		tblKnaDraw.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		//tblKnaDraw.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
		tblKnaDraw.setDpbkfg(E_YES___.NO);
		tblKnaDraw.setDprosq(null);
		//tblKnaDraw.setDrawtp(acctOut.getdra);
		tblKnaDraw.setDradpd(acctOut.getDradpd());
		tblKnaDraw.setDramwy(acctOut.getDramwy());
		tblKnaDraw.setDrcrwy(acctOut.getDrcrwy());
		tblKnaDraw.setDrdfsd(acctOut.getDrdfsd());
		tblKnaDraw.setDredwy(acctOut.getDredwy());
		tblKnaDraw.setDrdfwy(acctOut.getDrdfwy());
		tblKnaDraw.setDrmiam(acctOut.getDrmiam());
		tblKnaDraw.setDrmitm(acctOut.getDrmitm());
		tblKnaDraw.setDrmxam(acctOut.getDrmxam());
		tblKnaDraw.setDrmxtm(acctOut.getDrmxtm());
		tblKnaDraw.setDrtmwy(acctOut.getDrtmwy());
		tblKnaDraw.setDwperi(null);
		tblKnaDraw.setOrdrwy(acctOut.getOrdrwy());
		tblKnaDraw.setProtmd(null);
		tblKnaDraw.setRedqam(new BigDecimal(0));
		tblKnaDraw.setRedwnm(new Long(0));
		tblKnaDraw.setSetpwy(E_YES___.NO);
		tblKnaDraw.setTermwy(acctOut.getTermwy());
		tblKnaDraw.setTermwy(acctOut.getTermwy());
		tblKnaDraw.setDrrule(acctOut.getDrrule());
		KnaFxdrDao.insert(tblKnaDraw);
	}

	private static void addSaveControlInfo1(ChkOpenAcctOut acctOut,
			String acctno) {
		KnaFxsv save = SysUtil.getInstance(KnaFxsv.class);
		save.setAcctno(acctno);
		save.setAdjtpd(acctOut.getAdjtpd());
		save.setAmntwy(acctOut.getAmntwy());
		save.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		//save.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
		save.setDfltsd(acctOut.getDfltsd());
		save.setDfltwy(acctOut.getDfltwy());
		save.setEndtwy(acctOut.getDredwy());
		save.setGentwy(acctOut.getGentwy());
		save.setMaxiam(acctOut.getMaxiam());
		save.setMaxisp(acctOut.getMaxisp());
		save.setMaxitm(acctOut.getMaxitm());
		save.setMiniam(acctOut.getMiniam());
		save.setMinitm(acctOut.getMinitm());
		save.setPlanfg(acctOut.getPlanfg());
		save.setPlanwy(acctOut.getPlanwy());
		save.setPscrwy(acctOut.getPscrwy());
		save.setResvam(new BigDecimal(0));
		save.setSpbkfg(E_YES___.NO);
		save.setSvlepd(acctOut.getSvlepd());
		save.setSvletm(acctOut.getSvletm());
		save.setSvlewy(acctOut.getSvlewy());
		save.setTimewy(acctOut.getTimewy());
		bizlog.debug("是否明细3======="+acctOut.getDetlfg());
		save.setDetlfg(acctOut.getDetlfg());
		
		KnaFxsvDao.insert(save);
	}

	private static void addDrawControlInfo(ChkOpenAcctOut acctOut, String acctno) {
		KnaDraw tblKnaDraw = SysUtil.getInstance(KnaDraw.class);
		tblKnaDraw.setAcctno(acctno);
		tblKnaDraw.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		//tblKnaDraw.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
		tblKnaDraw.setDpbkfg(E_YES___.NO);
		tblKnaDraw.setDprosq(null);
		tblKnaDraw.setDradpd(acctOut.getDradpd());
		tblKnaDraw.setDramwy(acctOut.getDramwy());
		tblKnaDraw.setDrcrwy(acctOut.getDrcrwy());
		tblKnaDraw.setDrdfsd(acctOut.getDrdfsd());
		tblKnaDraw.setDredwy(acctOut.getDredwy());
		tblKnaDraw.setDrdfwy(acctOut.getDrdfwy());
		tblKnaDraw.setDrmiam(acctOut.getDrmiam());
		tblKnaDraw.setDrmitm(acctOut.getDrmitm());
		tblKnaDraw.setDrmxam(acctOut.getDrmxam());
		tblKnaDraw.setDrmxtm(acctOut.getDrmxtm());
		tblKnaDraw.setDrtmwy(acctOut.getDrtmwy());
		tblKnaDraw.setDwperi(null);
		tblKnaDraw.setOrdrwy(acctOut.getOrdrwy());
		tblKnaDraw.setProtmd(null);
		tblKnaDraw.setRedqam(new BigDecimal(0));
		tblKnaDraw.setRedwnm(new Long(0));
		tblKnaDraw.setSetpwy(E_YES___.NO);
		tblKnaDraw.setTermwy(acctOut.getTermwy());
		tblKnaDraw.setDrrule(acctOut.getDrrule());
		KnaDrawDao.insert(tblKnaDraw);
		
	}

	private static void addSaveControlInfo(ChkOpenAcctOut acctOut, String acctno) {
		
		KnaSave save = SysUtil.getInstance(KnaSave.class);
		save.setAcctno(acctno);
		save.setAdjtpd(acctOut.getAdjtpd());
		save.setAmntwy(acctOut.getAmntwy());
		save.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		//save.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
		save.setDfltsd(acctOut.getDfltsd());
		save.setDfltwy(acctOut.getDfltwy());
		save.setEndtwy(acctOut.getDredwy());
		save.setGentwy(acctOut.getGentwy());
		save.setMaxiam(acctOut.getMaxiam());
		save.setMaxisp(acctOut.getMaxisp());
		save.setMaxitm(acctOut.getMaxitm());
		save.setMiniam(acctOut.getMiniam());
		save.setMinitm(acctOut.getMinitm());
		save.setPlanfg(acctOut.getPlanfg());
		save.setPlanwy(acctOut.getPlanwy());
		save.setPscrwy(acctOut.getPscrwy());
		save.setResvam(new BigDecimal(0));
		save.setSpbkfg(E_YES___.NO);
		save.setSvlepd(acctOut.getSvlepd());
		save.setSvletm(acctOut.getSvletm());
		save.setSvlewy(acctOut.getSvlewy());
		save.setTimewy(acctOut.getTimewy());
		
		KnaSaveDao.insert(save);
	}

	private static String addToFixAcct(ChkOpenAcctOut acctOut, String acctna,String acctac,
			E_TERMCD depttm,long deptdy,String custno,String prodcd,E_CUSTTP custtp) {
		KnaFxac acct = SysUtil.getInstance(KnaFxac.class);
		String acctno = BusiTools.getAcctNo("0", custtp.getValue());
		acct.setAcctno(acctno);
		acct.setAcctna(acctna);
		acct.setAcctcd(acctOut.getAcctcd());
		acct.setAcctst(E_DPACST.NORMAL);
		acct.setAccttp(E_YES___.YES);
		acct.setBgindt(CommTools.getBaseRunEnvs().getTrxn_date());
		acct.setBkmony(new BigDecimal(0));
		acct.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
		acct.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		acct.setCrcycd(acctOut.getCrcycd());
		acct.setCsextg(E_CSEXTG.CASH);
		acct.setCustac(acctac);
		acct.setCustno(custno);
		acct.setDebttp(E_DEBTTP.DP2401);
		//acct.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
		acct.setHdmimy(acctOut.getMinibl());
		acct.setHdmxmy(acctOut.getMaxibl());
		acct.setOnlnbl(new BigDecimal(0));
		acct.setOpendt(CommTools.getBaseRunEnvs().getTrxn_date());
		acct.setOpensq(CommTools.getBaseRunEnvs().getTrxn_seq());
		acct.setOpmony(new BigDecimal(0));
		acct.setPddpfg(acctOut.getPddpfg());
		acct.setProdcd(prodcd);
		acct.setSleptg(E_YES___.NO);
		acct.setSpectp(E_SPECTP.PERSON_BASE);
		acct.setDepttm(depttm);
		
		//到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
		String termcd = depttm.getValue();
		if(CommUtil.equals(termcd.substring(0, 1),"9")){
			acct.setMatudt(DateTools2.dateAdd ((int)deptdy, acct.getBgindt()));
			acct.setDeptdy(deptdy); //存期天数
		}else{			
			acct.setMatudt(DateTools2.calDateByTerm(CommTools.getBaseRunEnvs().getTrxn_date(), depttm));
		}
		acct.setLstrdt(DateTools2.dateAdd (-1, CommTools.getBaseRunEnvs().getTrxn_date()));
		KnaFxacDao.insert(acct);
		return acctno;
	}

	/**
	 * 开立活期账户
	 * @param acctOut
	 * @param acctna
	 * @param acctac
	 * @param custno
	 * @param prodcd
	 * @return
	 */
	private static String addToCurrAcct(ChkOpenAcctOut acctOut, String acctna,String acctac,String custno,String prodcd,E_CUSTTP custtp) {
		KnaAcct acct = SysUtil.getInstance(KnaAcct.class);
		String acctno = BusiTools.getAcctNo("0",custtp.getValue());
		acct.setAcctno(acctno);
		acct.setAcctna(acctna);
		acct.setAcctcd(acctOut.getAcctcd());
		acct.setAcctst(E_DPACST.NORMAL);
		acct.setAccttp(E_YES___.YES);
		acct.setBgindt(CommTools.getBaseRunEnvs().getTrxn_date());
		acct.setBkmony(new BigDecimal(0));
		acct.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
		acct.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		acct.setCrcycd(acctOut.getCrcycd());
		acct.setCsextg(E_CSEXTG.CASH);
		acct.setCustac(acctac);
		acct.setCustno(custno);
		acct.setDebttp(E_DEBTTP.DP2401);
		//acct.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
		acct.setHdmimy(acctOut.getMinibl());
		acct.setHdmxmy(acctOut.getMaxibl());
		acct.setOnlnbl(new BigDecimal(0));
		acct.setOpendt(CommTools.getBaseRunEnvs().getTrxn_date());
		acct.setOpensq(CommTools.getBaseRunEnvs().getTrxn_seq());
		acct.setOpmony(new BigDecimal(0));
		acct.setPddpfg(acctOut.getPddpfg());
		acct.setProdcd(prodcd);
		acct.setSleptg(E_YES___.NO);
		acct.setSpectp(E_SPECTP.PERSON_BASE);
		acct.setDepttm(E_TERMCD.T000);
		acct.setLstrdt(DateTools2.dateAdd (-1, CommTools.getBaseRunEnvs().getTrxn_date()));
		KnaAcctDao.insert(acct);
		
		// 20160629 add 负债账户附加信息表新增
		if (E_FCFLAG.CURRENT == acctOut.getPddpfg()) {
			KnaAcctAddt addt = SysUtil.getInstance(KnaAcctAddt.class);
			addt.setAccatp(acctOut.getAccatp());
			addt.setAcctno(acctno);
			addt.setHigham(new BigDecimal(0));

			KnaAcctAddtDao.insert(addt);
		}
		
		return acctno;
	}

	/**
	 * 生成负债定期账户最大序号表
	 * @param acctno
	 */
	public static void addKnaFixaSort(String acctno){
		KnaFxacSort sort = SysUtil.getInstance(KnaFxacSort.class);
		sort.setAcctno(acctno);
		sort.setDetlsq(Long.valueOf(0));
		
		KnaFxacSortDao.insert(sort);
	}
	
	/**
	 * 增加账户利息
	 * @param prodcd 产品号
	 * @param acctno 负债账号
	 * @param crcycd 币种
	 * @param openir 开户利率
	 * @param depttm 存期
	 */
	public static void addAcctIntr(DpAcctIntrEntity intrEntity){
		boolean nullException = true;
		String prodcd = intrEntity.getProdcd();
		String acctno = intrEntity.getAcctno();
		String crcycd = intrEntity.getCrcycd();
		E_TERMCD depttm = intrEntity.getDepttm();
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String nextTranDt = CommTools.getBaseRunEnvs().getNext_date();
		
		KupDppbIntr intr = KupDppbIntrDao.selectOne_odb1(prodcd, crcycd, nullException);
		
		IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
		
		KnbAcin acin = SysUtil.getInstance(KnbAcin.class);
		acin.setAcctno(acctno);//账号
		acin.setIntrtp(intr.getIntrtp()); //利率类型
		acin.setCrcycd(crcycd); //币种
		acin.setInbefg(intr.getInbefg()); //计息标志
		acin.setTxbefg(intr.getTxbefg());//计税标志
		acin.setTxbebs(intr.getTxbebs());//计息基础
		acin.setHutxfg(intr.getHutxfg());//舍弃角分计息标志
		acin.setInammd(intr.getInammd()); //计息金额模式
		acin.setBldyca(intr.getBldyca());//平均余额天数计算方式
		acin.setTxbefr(intr.getTxbefr());//结息频率
		acin.setReprwy(intr.getReprwy());//重订价利息处理方式
		acin.setIntrcd(intr.getIntrcd()); //利率编号
		if(E_YES___.YES == intr.getTxbefg()){
			if(CommUtil.isNull(intr.getTaxecd()))
				throw DpModuleError.DpstAcct.BNAS1412(prodcd);
			acin.setTaxecd(intr.getTaxecd()); //税率编号
		}
		acin.setInprwy(intr.getInprwy()); //利率重定价方式
		if(E_IRRTTP.AZ == intr.getInprwy()){
			if(CommUtil.isNull(intr.getInadlv()))
				throw DpModuleError.DpstAcct.BNAS1413(prodcd);
			
			acin.setInadlv(intr.getInadlv()); //利率调整频率
		}
		acin.setFvrbfg(intr.getFvrbfg()); //优惠变化调整优惠标志
		if(E_FVRBFG.NO != intr.getFvrbfg()){
			if(CommUtil.isNull(intr.getFvrblv()))
				throw DpModuleError.DpstAcct.BNAS1414(prodcd);
			
			acin.setFvrblv(intr.getFvrblv());
			acin.setLafvdt(trandt);
			//计算出下次优惠更新日期
			String sYhxcriqi = DpPublic.getNextPeriod(trandt, nextTranDt, intr.getFvrblv());
			acin.setNxfvdt(sYhxcriqi);//优惠下次更新日
		}
		acin.setNxindt(trandt); //下次计息日
		if(CommUtil.isNotNull(intr.getTxbefr())){
			String sNextDate = DpPublic.getNextPeriod(trandt, nextTranDt, intr.getTxbefr());
			acin.setNcindt(sNextDate); //下次结息日
		}
		acin.setOpendt(trandt);//开户日期
		acin.setBgindt(trandt);//起息日期
		acin.setPlanin(BigDecimal.ZERO);//计提利息
		acin.setLastdt(trandt); //最近更新日期
		acin.setPlblam(BigDecimal.ZERO); //计息累计余额(积数)
		acin.setNxdtin(BigDecimal.ZERO); //上计提日利率
		acin.setMustin(BigDecimal.ZERO);//应缴税金
		acin.setLsinop(E_INDLTP.CAIN); //上次利息操作
		acin.setIndtds(0); //计息天数
		acin.setEvrgbl(BigDecimal.ZERO); //平均余额
		acin.setCutmin(BigDecimal.ZERO); //本期利息
		acin.setCutmis(BigDecimal.ZERO); //本期利息税
		acin.setCutmam(BigDecimal.ZERO);//本期积数
		acin.setAmamfy(BigDecimal.ZERO); //本年累计积数
		acin.setLyamam(BigDecimal.ZERO); //上年累计积数
		acin.setDiffin(BigDecimal.ZERO); //应加/减利息
		acin.setDiffct(BigDecimal.ZERO); //应加/减积数
		acin.setLsinsq(String.valueOf(0));//上次利息序号
		
		acin.setProdcd(prodcd); //产品号
		acin.setDetlfg(intrEntity.getDetlfg()); //明细汇总
		acin.setPddpfg(intrEntity.getPddpfg()); //定活标志
		acin.setLaamdt(trandt); //积数更新日期
		
		acin.setIncdtp(intr.getIncdtp()); //利率代码类型
		acin.setLyinwy(intr.getLyinwy()); //分层计息方式 全额、超额
		KnbAcinDao.insert(acin);
		
		//------------------------------mdy by zhangan-------------------------
		
		if(E_IRCDTP.LAYER == intr.getIncdtp()){ //分层利率处理
			
			IoLayInfoIn  layerIn = SysUtil.getInstance(IoLayInfoIn.class);
			IoSrvPbInterestRate pfIntr = SysUtil.getInstance(IoSrvPbInterestRate.class);
			
			layerIn.setIntrcd(intr.getIntrcd());
			layerIn.setCrcycd(intr.getCrcycd());
			layerIn.setIntrkd(E_INTRTY.DP); //存款利率
			
			Options<IoLayInfo> options = pfIntr.getLayerIntr(layerIn);
			for(IoLayInfo layer : options){
				KubInrt inrt = SysUtil.getInstance(KubInrt.class);
				
				inrt.setAcctno(acctno); //账号
				inrt.setIntrtp(intr.getIntrtp()); //利息类型
				inrt.setIndxno(layer.getInplsq());  //顺序号
				inrt.setIntrcd(intr.getIntrcd()); //利率编码
				inrt.setIntrwy(intr.getIntrwy()); //利率靠档方式
				inrt.setIncdtp(intr.getIncdtp()); //利率代码类型
				inrt.setLyinwy(intr.getLyinwy()); //分层计息方式
				
				inrt.setLvamot(layer.getLvamlm()); //层次金额下限
				inrt.setLvindt(layer.getRfirtm()); //层次存期
				inrt.setLvaday(layer.getDayllm()); //层次存期天数下限
				inrt.setOpintr(layer.getIntrvl()); //开户利率
				inrt.setBsintr(layer.getBaseir()); //基准利率
				inrt.setIrflby(layer.getFlirwy()); //浮动方式
				if(layer.getFlirwy() == E_IRFLPF.POINT){
					inrt.setInflpo(layer.getFlirvl()); //浮动点数
				}else if(layer.getFlirwy() == E_IRFLPF.RATE){
					inrt.setInflrt(layer.getFlirrt()); //浮动百分比
				}
				inrt.setCuusin(layer.getIntrvl()); //当前执行利率
				//inrt.setRealin(BigDecimal.ZERO);
				inrt.setIsfavo(E_YES___.get(layer.getIsfavo())); //是否优惠
				inrt.setPfirwy(layer.getPfirwy()); //优惠浮动方式
				inrt.setFavovl(layer.getPfirvl()); //优惠浮动值
				
				inrt.setLacain(BigDecimal.ZERO);
				inrt.setLastbl(BigDecimal.ZERO); //上日余额
				inrt.setLastdt(""); //上日余额更新日期
				
				inrt.setClvsmt(BigDecimal.ZERO); //当前层次积数
				inrt.setClvamt(BigDecimal.ZERO); //当前层级计息金额
				inrt.setClvudt(""); //当前层次计息金额更新日期
				
				KubInrtDao.insert(inrt);
			}
			
			return;
		}
		
		//-----------------------------end mdy by zhangan---------------------
		//账户利率信息
		KubInrt inrt = SysUtil.getInstance(KubInrt.class);
		inrt.setAcctno(acctno); //账号
		inrt.setIntrtp(intr.getIntrtp()); //利息类型
		inrt.setIndxno(1L);  //顺序号
		inrt.setIntrcd(intr.getIntrcd()); //利率编码
		inrt.setIntrwy(intr.getIntrwy()); //利率靠档方式
		inrt.setIncdtp(intr.getIncdtp()); //利率代码类型
		inrt.setLyinwy(intr.getLyinwy()); //分层计息方式
//		if(E_IRCDTP.AGREE == intr.getIncdtp()){
//			//协议利率
//			if (CommUtil.isNull(openir) || CommUtil.compare(openir, BigDecimal.ZERO) <= 0) {
//				// 协议利率的开户利率必须由报文上送
//				throw DpModuleError.DpstAcct.E0005();
//			}
//			inrt.setOpintr(openir); //开户利率
//			inrt.setCuusin(openir);//执行利率
//		}else{
			//IntrPublicEntity entity = new IntrPublicEntity();
			IoPbIntrPublicEntity entity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
			entity.setCrcycd(crcycd);
			entity.setDepttm(depttm);
			entity.setIntrcd(intr.getIntrcd());
			entity.setIncdtp(intr.getIncdtp());
			entity.setTrandt(trandt);
			entity.setIntrwy(intr.getIntrwy());
			entity.setBgindt(trandt);
			entity.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
			entity.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
			
			bizlog.debug("存期参数==========="+ depttm);
			
			//到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
			String termcd = depttm.getValue();
			if(CommUtil.equals(termcd.substring(0, 1),"9")){
				entity.setEdindt(DateTools2.dateAdd ((int)intrEntity.getDeptdy(), trandt));
			}else{
				entity.setEdindt(DateTools2.calDateByTerm(trandt, depttm));			
			}
			pbpub.countInteresRate(entity);
			
			inrt.setOpintr(entity.getIntrvl()); //开户利率
			inrt.setBsintr(entity.getBaseir()); //基准利率
			inrt.setIrflby(entity.getFlirwy()); //浮动方式
			inrt.setInflpo(entity.getFlirvl()); //浮动点数
			inrt.setInflrt(entity.getFlirrt()); //浮动百分比
			inrt.setCuusin(entity.getIntrvl()); //当前执行利率
//		}
		
		KubInrtDao.insert(inrt);
	}
}
