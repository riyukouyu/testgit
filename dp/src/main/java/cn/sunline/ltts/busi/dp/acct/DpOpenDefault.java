package cn.sunline.ltts.busi.dp.acct;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpBaseProdDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcct;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAddt;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrch;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCust;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfir;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDraw;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawPlan;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntr;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbMatu;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPost;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostPlan;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTerm;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkOpenAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpActpPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpBasePart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpBrchPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCustPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDfirPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDppbPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDrawPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDrawplPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpIntrPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpMatuPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpOpenSub;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpPostPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpPostplPart;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstProd;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SPECTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;

public class DpOpenDefault {
	
	
	private static BizLog log = BizLogUtil.getBizLog(DpOpenDefault.class);

	/**
	 * 开户产品检查
	 * @param prodcd 产品号
	 * @param crcycd 币种
	 * @param depttm 存期
	 * @param cacttp 客户账号类型
	 * @param custno 客户号
	 * @param custac 客户账号
	 * 
	 * 1.检查产品基本信息 
	 * 2.检查客户账号类型控制 
	 * 3.检查开户控制 
	 * 4.检查机构控制 
	 * 5.检查存期控制 
	 * 6.获取存入控制信息 
	 * 7.获取支取控制信息
	 * 8.获取存入计划信息 
	 * 9.获取支取计划信息 
	 * 10.获取核算信息
	 */
	//public static ChkOpenAcctOut chkOpenAcct(ChkOpenAcctIn cplOpenAcctIn) {
	public static IoDpOpenSub chkOpenAcct(ChkOpenAcctIn cplOpenAcctIn) {
		
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();// 交易机构
		
		IoDpOpenSub openInfo = SysUtil.getInstance(IoDpOpenSub.class);
		
		boolean nullException = true;
		String acctno = null;
		/**
		 * 检查产品基本信息 
		 * 
		 * 1.产品是否在有效期内 
		 * 2.产品状态是否有效
		 */
		String coprno = CommTools.getBaseRunEnvs().getBusi_org_id();// 当前法人代码
		KupDppb dppb = DpBaseProdDao.selKupDppb(coprno, cplOpenAcctIn.getProdcd(), false);
		if (CommUtil.isNull(dppb)) {
			coprno = BusiTools.getCenterCorpno();// 省级法人代码
			dppb = DpBaseProdDao.selKupDppb(coprno, cplOpenAcctIn.getProdcd(), true);
		}
		// 产品是否在有效期内
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		if (CommUtil.compare(trandt, dppb.getEfctdt()) < 0 || CommUtil.compare(trandt, dppb.getInefdt()) >= 0) {
			throw DpModuleError.DpstProd.BNAS1712();
		}
		// 产品状态是否有效
		if (BaseEnumType.E_PRODST.NORMAL != dppb.getProdst()) {
			throw DpModuleError.DpstProd.BNAS1713();
		}
		
		// 开户机构是否停用产品
		KupDppbBrch tblBrch = DpBaseProdDao.selKupDppbBrchByBrchno(cplOpenAcctIn.getProdcd(), brchno, false);
		if (CommUtil.isNotNull(tblBrch)) {
			if (CommUtil.compare(trandt, tblBrch.getEfctdt()) < 0 || CommUtil.compare(trandt, tblBrch.getInefdt()) >= 0) {
				throw DpModuleError.DpstProd.BNAS1712();
			}
		}

		// 处理币种
		String crcycd = null;
		if (CommUtil.isNull(cplOpenAcctIn.getCrcycd())) {
			crcycd = dppb.getPdcrcy();
		} else {
			crcycd = cplOpenAcctIn.getCrcycd();
		}
		
		// 获取产品定活标志。
		String pddpfg = dppb.getPddpfg().getValue();
		
		/** 检查预售份数是否超过限制 add by renjinghua in 20150912**/
		if(CommUtil.isNotNull(dppb.getPresal()) && dppb.getPresal().intValue() > 0){
			//查询该产品下账户笔数
			int count;
			if(E_FCFLAG.CURRENT == dppb.getPddpfg()){
				count = DpAcctQryDao.selKnaAcctCountByPordcd(cplOpenAcctIn.getProdcd(), true);
			}else if(E_FCFLAG.FIX == dppb.getPddpfg()){
				count = DpAcctQryDao.selKnaFxacCountByPordcd(cplOpenAcctIn.getProdcd(), true);
			}else{
				throw DpModuleError.DpstComm.BNAS1181(dppb.getPddpfg().getLongName());
			}
			
			//如果账户笔数达到限购份数，则不能再做业务
			if(count >= dppb.getPresal().intValue()){
				throw DpModuleError.DpstProd.BNAS1731(cplOpenAcctIn.getProdcd(), dppb.getPresal());
			}
		}

		/**
		 * 检查客户账号类型控制 
		 * 1.账户下唯一标识
		 */
		KupDppbActp actp = DpBaseProdDao.selKupDppbActp(coprno, cplOpenAcctIn.getProdcd(), cplOpenAcctIn.getCacttp(), true);
		// 账户下唯一标识
		if (BaseEnumType.E_YES___.YES == actp.getAcolfg()) {
			//检查活期账户
			if(CommUtil.equals(pddpfg, E_FCFLAG.CURRENT.getValue())){
				//查询该产品编号下是否已开负债账户
				//List<KnaAcct> tblKnaAccts = KnaAcctDao.selectAll_odb11(crcycd,cplOpenAcctIn.getProdcd(),
				//		cplOpenAcctIn.getCustac(), E_DPACST.NORMAL, false);
				List<KnaAcct> tblKnaAccts = ActoacDao.selAllKnaAcctByClose(crcycd, cplOpenAcctIn.getProdcd(), cplOpenAcctIn.getCustac(), E_DPACST.CLOSE, false);
				//发现一条数据，获取已开立的负债子账号
				if(tblKnaAccts.size() == 1){
					throw DpModuleError.DpstProd.BNAS1724();
				}
				//发现多条数据跑出异常
				if(tblKnaAccts.size() > 1){
					throw DpModuleError.DpstProd.BNAS1724();
				}
			}
			//检查定期账户
			if(CommUtil.equals(pddpfg, E_FCFLAG.FIX.getValue())){
				//List<KnaFxac> tblKnaFxacs = KnaFxacDao.selectAll_odb6(crcycd,cplOpenAcctIn.getProdcd(),
				//		cplOpenAcctIn.getCustac(), E_DPACST.NORMAL, false);
				List<KnaFxac> tblKnaFxacs = ActoacDao.selAllKnaFxacByClose(crcycd, cplOpenAcctIn.getProdcd(), cplOpenAcctIn.getCustac(), E_DPACST.CLOSE, false);
				//发现一条数据，获取已开立的负债子账号
				if(tblKnaFxacs.size() == 1){
					//acctno = tblKnaFxacs.get(0).getAcctno();
					throw DpModuleError.DpstProd.BNAS1724();
				}
				//发现多条数据跑出异常
				if(tblKnaFxacs.size() > 1){
					throw DpModuleError.DpstProd.BNAS1724();
				}
			}
		}

		/**
		 * 检查开户控制 
		 * 1.检查起存金额 2.客户下唯一标识，
		 */
		KupDppbCust cust = DpBaseProdDao.selKupDppbCust(coprno, cplOpenAcctIn.getProdcd(), crcycd, true);
		
		//1.检查起存金额
		// add by xiongzhao 起存金额存在且大于零的情况下采取检查交易金额是否大于起存金额
		if (CommUtil.isNotNull(cust.getSrdpam())
				&& (CommUtil.compare(cust.getSrdpam(), BigDecimal.ZERO) > 0)) {
			if (CommUtil.isNotNull(cplOpenAcctIn.getTranam())) {
				if (CommUtil.compare(cplOpenAcctIn.getTranam(),
						cust.getSrdpam()) < 0) {
					throw DpModuleError.DpstProd.BNAS1733(cplOpenAcctIn.getTranam(),
							cust.getSrdpam());
				}
			}
		}
		//2.检查客户唯一标识
		if (DpEnumType.E_ONLYFG.ONLO == cust.getOnlyfg()) {
			//检查活期账户
			if(CommUtil.equals(pddpfg, E_FCFLAG.CURRENT.getValue())){
				//List<KnaAcct> tblKnaAccts = KnaAcctDao.selectAll_odb10(cplOpenAcctIn.getCustno(),
				//		cplOpenAcctIn.getProdcd(), E_DPACST.NORMAL, false);
				
				List<KnaAcct> tblKnaAccts = ActoacDao.selAllKnaAcctByCustnoClose(cplOpenAcctIn.getProdcd(), cplOpenAcctIn.getCustno(), E_DPACST.CLOSE, false);
				//发现一条数据，获取已开立的负债子账号
				if(tblKnaAccts.size() == 1){
					//acctno = tblKnaAccts.get(0).getAcctno();
					throw DpModuleError.DpstProd.BNAS1714();
				}
				
				if(tblKnaAccts.size() > 1){
					throw DpModuleError.DpstProd.BNAS1714();
				}
			}
			//检查定期账户
			if(CommUtil.equals(pddpfg, E_FCFLAG.FIX.getValue())){
				//List<KnaFxac> tblKnaFxacs = KnaFxacDao.selectAll_odb7(cplOpenAcctIn.getCustno(),
				//		cplOpenAcctIn.getProdcd(), E_DPACST.NORMAL, false);
				List<KnaFxac> tblKnaFxacs = ActoacDao.selAllKnaFxacByCustnoClose(cplOpenAcctIn.getProdcd(), cplOpenAcctIn.getCustno(), E_DPACST.CLOSE, false);
				//发现一条数据，获取已开立的负债子账号
				if(tblKnaFxacs.size() == 1){
					throw DpModuleError.DpstProd.BNAS1714();
				}
				
				if(tblKnaFxacs.size() > 1){
					throw DpModuleError.DpstProd.BNAS1714();
				}
			}
		}
		
		/**
		 * 开户机构检查
		 */
		List<KupDppbBrch> brchs = null;
		if(DpEnumType.E_BRCHFG.ALL != dppb.getBrchfg()){
			brchs = DpBaseProdDao.selKupDppbBrch(CommTools.getBaseRunEnvs().getBusi_org_id(), cplOpenAcctIn.getProdcd(), crcycd, false);
						//适用
			if(DpEnumType.E_BRCHFG.USE == dppb.getBrchfg()){
				
				if (CommUtil.isNull(brchs)){
					throw DpModuleError.DpstProd.BNAS1407();
				}
				
				boolean brchFlag =  false;
				
				for(KupDppbBrch brch : brchs){
					if(brch.getBrchno().equals(CommTools.getBaseRunEnvs().getTrxn_branch())){
						brchFlag = true;
					}
				}
				
				if(!brchFlag){
					throw DpModuleError.DpstProd.BNAS1715();
				}
			}
			
			
		}
		
		/**
		 * 检查存期控制
		 */
		log.debug("存期控制================="+cplOpenAcctIn.getDepttm());
	
		KupDppbTerm term = DpBaseProdDao.selKupDppbTerm(coprno, cplOpenAcctIn.getProdcd(), crcycd, cplOpenAcctIn.getDepttm(), false);
		
		if(CommUtil.isNull(term)){
			throw DpModuleError.DpstProd.BNAS1716();
		}else{
			E_TERMCD depttm = term.getDepttm();
			if(CommUtil.equals(depttm.getValue().substring(0, 1), "9")){
				if(CommUtil.isNull(term.getDeptdy()) || term.getDeptdy().intValue() <= 0){
					throw DpModuleError.DpstProd.BNAS1408();
				}
			}
		}
		
		/**
		 * 查询存入控制
		 */
		KupDppbPost post = DpBaseProdDao.selKupDppbPost(coprno, cplOpenAcctIn.getProdcd(), crcycd, false);
		
		/** 检查产品每人可购买份数  add by renjinghua in 20150912 **/
		if(CommUtil.isNotNull(post)){
			//检查每人限购份数
			if(CommUtil.isNotNull(post.getSvrule()) && post.getSvrule().longValue() > ConvertUtil.toLong(0)){	
				//活期
				if(E_FCFLAG.CURRENT == dppb.getPddpfg()){
					//查询该客户下该产品的活期账户信息
					List<KnaAcct> lstKnaAcct = DpAcctQryDao.selKnaAcctByPordcdAndCustno(cplOpenAcctIn.getProdcd(),
							cplOpenAcctIn.getCustno(), false);
					//如果账户数量大于等于每人限购值，则不能再购买
					if(CommUtil.isNotNull(lstKnaAcct) && lstKnaAcct.size() >= post.getSvrule().intValue()){
						throw DpModuleError.DpstProd.BNAS1732(cplOpenAcctIn.getProdcd(), post.getSvrule());
					}
				}
				//定期
				if(E_FCFLAG.FIX == dppb.getPddpfg()){
					//查询该客户下该产品的定期账户信息
					List<KnaFxac> lstKnaFxac = DpAcctQryDao.selKnaFxacByProdcdAndCustno(
									cplOpenAcctIn.getProdcd(),
									cplOpenAcctIn.getCustno(), false);
					//如果账户数量大于等于每人限购值，则不能再购买
					if(CommUtil.isNotNull(lstKnaFxac) && lstKnaFxac.size() >= post.getSvrule().intValue()){
						throw DpModuleError.DpstProd.BNAS1732(cplOpenAcctIn.getProdcd(), post.getSvrule());
					}
				}
			}
		}
		
		/**
		 * 查询支取控制
		 */
		KupDppbDraw draw = DpBaseProdDao.selKupDppbDraw(coprno, cplOpenAcctIn.getProdcd(), crcycd, false);
		
		/**
		 * 查询存入计划
		 */
		KupDppbPostPlan ptplan = DpBaseProdDao.selKupDppbPostPlan(coprno, cplOpenAcctIn.getProdcd(), crcycd, false);
		
		/**
		 * 查询支取计划
		 */
		KupDppbDrawPlan drplan = DpBaseProdDao.selKupDppbDrawPlan(coprno, cplOpenAcctIn.getProdcd(), crcycd, false);
		
		/**
		 * 查询核算
		 */
		KupDppbAcct acct = DpBaseProdDao.selKupDppbAcct(coprno, cplOpenAcctIn.getProdcd(), cplOpenAcctIn.getDepttm(), true);
		
		/**
		 * 查询到期
		 */
		KupDppbMatu matu = DpBaseProdDao.selKupDppbMatu(coprno, cplOpenAcctIn.getProdcd(), crcycd, false);
		
		
		KupDppbIntr intr = DpBaseProdDao.selKupDppbIntr(coprno, cplOpenAcctIn.getProdcd(), crcycd, true);
		
		
		List<KupDppbDfir> lsdfir = DpBaseProdDao.selKupDppbDfir(coprno, cplOpenAcctIn.getProdcd(), crcycd, false);
		/**
		 * 查询产品附加属性表
		 */
		KupDppbAddt addt = null;
		if (E_FCFLAG.CURRENT == dppb.getPddpfg()) {
			addt = DpBaseProdDao.selKupDppbAddt(coprno, cplOpenAcctIn.getProdcd(), nullException);
		}	
		
		//开负债账户基本参数
		IoDpBasePart base = SysUtil.getInstance(IoDpBasePart.class);
		if(CommUtil.isNotNull(addt)){
			base.setAccatp(addt.getAccatp());
		}
		base.setOpmony(cplOpenAcctIn.getTranam());
		base.setAcctcd(acct.getAcctcd()); //核算代码
		base.setAcctno(acctno);
		base.setAccttp(E_YES___.YES); //结算户标志
		base.setCacttp(cplOpenAcctIn.getCacttp()); //客户账号类型
		//base.setCardno(cplOpenAcctIn.);
		base.setCrcycd(crcycd);
		base.setCustac(cplOpenAcctIn.getCustac()); //电子账号ID
		base.setCustna(cplOpenAcctIn.getAcctna()); //客户名称
		base.setCustno(cplOpenAcctIn.getCustno()); //客户号
		//base.setCusttp(); TODO:客户类型
		base.setDeptdy(term.getDeptdy()); //存期天数
		base.setDepttm(term.getDepttm()); //存期
		//base.setOpacfg(opacfg); TODO:首开户标志
		base.setPddpfg(dppb.getPddpfg());
		base.setProdcd(dppb.getProdcd());
		if(dppb.getDebttp() == E_DEBTTP.DP2401){
			base.setSpectp(E_SPECTP.PERSON_BASE);
		}else{
			base.setSpectp(E_SPECTP.PERSON_01);
		}
		
		//账户类型控制部件
		//Options<IoDpActpPart> tblActp = new DefaultOptions<>();
		IoDpActpPart ptactp = SysUtil.getInstance(IoDpActpPart.class);
		ptactp.setAcolfg(actp.getAcolfg());
		ptactp.setCacttp(actp.getCacttp());
		//tblActp.add(ptactp);
		
		IoDpDppbPart ptdppb = SysUtil.getInstance(IoDpDppbPart.class);
		//ptdppb.setAcolfg(actp.getAcolfg());
		ptdppb.setBrchfg(dppb.getBrchfg());
		//ptdppb.setCrcycd(crcycd);
		ptdppb.setIsdrft(dppb.getIsdrft()); //是否允许透支标志 TODO
		ptdppb.setProdbt(dppb.getProdtp()); //产品大类 产品所属对象
		ptdppb.setProdmt(dppb.getPddpfg()); //定活
		ptdppb.setProdlt(dppb.getDebttp()); //储蓄种类
		
		IoDpCustPart ptcust = SysUtil.getInstance(IoDpCustPart.class);
		ptcust.setMadtby(cust.getMadtby());
		//ptcust.setMatudt(cust.get);到期日期
		ptcust.setMginfg(dppb.getMginfg()); //早起息标志
		ptcust.setMgindy(dppb.getMgindy()); //早起息天数
		ptcust.setOnlyfg(cust.getOnlyfg());
		ptcust.setSrdpam(cust.getSrdpam());
		ptcust.setStepvl(cust.getStepvl());
		
		//存入控制
		IoDpPostPart ptpost = null;
		if(CommUtil.isNotNull(post)){
			ptpost = SysUtil.getInstance(IoDpPostPart.class);
			ptpost.setAmntwy(post.getAmntwy()); //金额控制方式
			ptpost.setDetlfg(post.getDetlfg()); //明细汇总
			ptpost.setMaxiam(post.getMaxiam());
			ptpost.setMaxibl(post.getMaxibl());
			ptpost.setMaxitm(post.getMaxitm());
			ptpost.setMiniam(post.getMiniam());
			ptpost.setMinitm(post.getMinitm());
			ptpost.setPosttp(post.getPosttp());
			ptpost.setPostwy(post.getPostwy());
			ptpost.setTimewy(post.getTimewy());
		}
		
		IoDpDrawPart ptdraw = null;
		if(CommUtil.isNotNull(draw)){
			ptdraw = SysUtil.getInstance(IoDpDrawPart.class);
			ptdraw.setCtrlwy(draw.getCtrlwy()); //支取控制方法
			ptdraw.setDramwy(draw.getDramwy()); //金额控制方式
			ptdraw.setDrawtp(draw.getDrawtp()); //支取控制方式
			ptdraw.setDrmiam(draw.getDrmiam());
			ptdraw.setDrmitm(draw.getDrmitm());
			ptdraw.setDrmxam(draw.getDrmxam());
			ptdraw.setDrmxtm(draw.getDrmxtm());
			ptdraw.setDrrule(draw.getDrrule()); //支取规则
			ptdraw.setDrtmwy(draw.getDrtmwy()); //次数控制方式
			ptdraw.setIsmibl(draw.getIsmibl());
			ptdraw.setMinibl(draw.getMinibl());
		}
		
		IoDpPostplPart postpl = null;
		if(CommUtil.isNotNull(ptplan)){
			postpl = SysUtil.getInstance(IoDpPostplPart.class);
			postpl.setDfltsd(ptplan.getDfltsd()); //存入违约标准
			postpl.setDfltwy(ptplan.getDfltwy()); //存入违约处理方式
			postpl.setGentwy(ptplan.getGentwy()); //计划生成方式
			postpl.setMaxisp(ptplan.getMaxisp()); //最大补足次数
			postpl.setPlanfg(ptplan.getPlanfg());
			postpl.setPlanpd(ptplan.getPlanpd());
			postpl.setPscrwy(ptplan.getPscrwy());
			postpl.setSvlepd(ptplan.getSvlepd());
			postpl.setSvletm(ptplan.getSvletm());
			postpl.setSvlewy(ptplan.getSvlewy());
			postpl.setPsamtp(ptplan.getPsamtp()); //存入计划金额类型
		}
		
		IoDpDrawplPart drawpl = null;
		if(CommUtil.isNotNull(ptplan)){
			drawpl = SysUtil.getInstance(IoDpDrawplPart.class);
			drawpl.setBeinfg(drplan.getBeinfg()); //支取时结息处理
			drawpl.setGendpd(drplan.getGendpd()); //计划调整周期
			drawpl.setDradwy(drplan.getDradwy());
			drawpl.setDrcrwy(drplan.getDrcrwy());
			drawpl.setDrdfsd(drplan.getDrdfsd());
			drawpl.setDrdfwy(drplan.getDrdfwy());
			drawpl.setSetpwy(drplan.getSetpwy());
		}
		
		Options<IoDpDfirPart> tblDfir = null;
		if(CommUtil.isNotNull(lsdfir) && lsdfir.size() > 0){
			tblDfir = new DefaultOptions<>();
			for(KupDppbDfir dfir : lsdfir){
				IoDpDfirPart ptdfir = SysUtil.getInstance(IoDpDfirPart.class);
				ptdfir.setBsinam(dfir.getBsinam());
				ptdfir.setBsindt(dfir.getBsindt());
				ptdfir.setBsinrl(dfir.getBsinrl());
				ptdfir.setDrdein(dfir.getDrdein());
				ptdfir.setInadtp(dfir.getInadtp());
				ptdfir.setIncdtp(dfir.getIncdtp());
				ptdfir.setInclfg(dfir.getInclfg());
				ptdfir.setInedsc(dfir.getInedsc());
				ptdfir.setIntrcd(dfir.getBsincd());
				ptdfir.setInsrwy(dfir.getInsrwy());
				ptdfir.setIntrwy(dfir.getIntrwy());
				ptdfir.setTeartp(dfir.getTeartp());
				ptdfir.setIntrdt(dfir.getIntrdt()); //利率确定日期方式
				ptdfir.setLevety(dfir.getLevety()); //靠档规则
				
				tblDfir.add(ptdfir);
			}
		}
		
		IoDpIntrPart ptintr = null;
		if(CommUtil.isNotNull(intr)) {
			ptintr = SysUtil.getInstance(IoDpIntrPart.class);
		ptintr.setBldyca(intr.getBldyca()); //平均余额天数计算方式
		ptintr.setFvrbfg(intr.getFvrbfg()); //优惠变化调整优惠标志
		ptintr.setHutxfg(intr.getHutxfg()); //舍弃角分计息标志
		ptintr.setInadlv(intr.getInadlv()); //利率调整频率
		ptintr.setInammd(intr.getInammd()); //计息金额模式
		ptintr.setInbefg(intr.getInbefg()); //计息标志
		ptintr.setIncdtp(intr.getIncdtp()); //利率代码类型
		ptintr.setInclfg(intr.getInwytp()); //利率靠档标志
		ptintr.setInprwy(intr.getInprwy()); //利率重订价方式
		ptintr.setIntrcd(intr.getIntrcd()); //利率daim
		ptintr.setIntrtp(intr.getIntrtp()); //利息类型
		ptintr.setIntrwy(intr.getIntrwy()); //利率靠档方式
		ptintr.setIsrgdt(intr.getIsrgdt()); //是否登记分层明细
		ptintr.setLydttp(intr.getLydttp()); //分层明细积数调整方式
		ptintr.setLyinwy(intr.getLyinwy()); //分层计息方式
		ptintr.setReprwy(intr.getReprwy()); //重订价利息处理方式
		ptintr.setTaxecd(intr.getTaxecd()); //税率编号
		ptintr.setTebehz(intr.getTebehz()); //计息频率
		ptintr.setTxbebs(intr.getTxbebs()); //计息基础
		ptintr.setTxbefg(intr.getTxbefg()); //计税标志
		ptintr.setTxbefr(intr.getTxbefr()); //结息频率
		ptintr.setIntrdt(intr.getIntrdt()); //利率确定日期方式
		ptintr.setLevety(intr.getLevety()); //靠档规则
		}
		
		IoDpMatuPart ptmatu = null;
		if(CommUtil.isNotNull(matu)){
			ptmatu = SysUtil.getInstance(IoDpMatuPart.class);
			ptmatu.setDelyfg(matu.getDelyfg()); //是否根据存款顺延到期日
			ptmatu.setFestdl(matu.getFestdl()); //遇节假日处理方式
			ptmatu.setMatupd(matu.getMatupd()); //到期宽限期
			ptmatu.setTrdpfg(matu.getTrdpfg()); //允许转存标志
			ptmatu.setTrdptm(matu.getTrdptm()); //转存产品存期
			ptmatu.setTrintm(matu.getTrintm()); //可转存次数
			ptmatu.setTrinwy(matu.getTrinwy()); //转存利率调整方式
			ptmatu.setTrpdfg(matu.getTrpdfg()); //是否可更换转存产品
			ptmatu.setTrprod(matu.getTrprod()); //转存产品编号
			ptmatu.setTrsvtp(matu.getTrsvtp()); //转存方式
		}
		
		//增加机构控制列表
		Options<IoDpBrchPart> ptBrchParts = new DefaultOptions<IoDpBrchPart>();
		if(CommUtil.isNotNull(brchs) && brchs.size() > 0){
			for(KupDppbBrch tblDppb_brch : brchs){				
				IoDpBrchPart cplBrchPart = SysUtil.getInstance(IoDpBrchPart.class);
				cplBrchPart.setBrchno(tblDppb_brch.getBrchno());
				ptBrchParts.add(cplBrchPart);
			}
		}
		
		openInfo.setBase(base);
		openInfo.setActp(ptactp);
		//openInfo.setBrch(brch); //机构控制
		openInfo.setCust(ptcust);
		openInfo.setDfir(tblDfir);
		openInfo.setDppb(ptdppb);
		openInfo.setDraw(ptdraw);
		openInfo.setDrawpl(drawpl);
		//openInfo.setDrawpldt();
		openInfo.setIntr(ptintr);
		openInfo.setMatu(ptmatu);
		openInfo.setPost(ptpost);
		openInfo.setPostpl(postpl);
		//openInfo.setPostpldt();
		
		openInfo.setBrch(ptBrchParts);
		
		return openInfo;
		
		
				
		/**
		 * 设置输出参数
		 */
//		ChkOpenAcctOut prodInfo = SysUtil.getInstance(ChkOpenAcctOut.class);
//		prodInfo.setCrcycd(crcycd);
//		prodInfo.setMadtby(cust.getMadtby());
//		prodInfo.setPddpfg(dppb.getPddpfg());
//		prodInfo.setMaxibl(ptplan.getMaxibl());
//		prodInfo.setMinibl(drplan.getMinibl());
//		prodInfo.setDebttp(dppb.getDebttp());
//		prodInfo.setAcctcd(acct.getAcctcd());
//		
//		log.debug("是否明细1======"+post.getDetlfg());
//		prodInfo.setAmntwy(post.getAmntwy());
//		prodInfo.setMiniam(post.getMiniam());
//		prodInfo.setMaxiam(post.getMaxiam());
//		prodInfo.setTimewy(post.getTimewy());
//		prodInfo.setMinitm(post.getMinitm());
//		prodInfo.setMaxitm(post.getMaxitm());
//		prodInfo.setDetlfg(post.getDetlfg());
//		
//		prodInfo.setPlanfg(ptplan.getPlanfg());
//		prodInfo.setPlanwy(ptplan.getPlanwy());
//		prodInfo.setAdjtpd(ptplan.getAdjtpd());
//		prodInfo.setEndtwy(ptplan.getEndtwy());
//		prodInfo.setGentwy(ptplan.getGentwy());
//		prodInfo.setSvlewy(ptplan.getSvlewy());
//		prodInfo.setMaxisp(ptplan.getMaxisp());
//		prodInfo.setSvlepd(ptplan.getSvlepd());
//		prodInfo.setDfltsd(ptplan.getDfltsd());
//		prodInfo.setSvletm(ptplan.getSvletm());
//		prodInfo.setDfltwy(ptplan.getDfltwy());
//		prodInfo.setPscrwy(ptplan.getPscrwy());
//		
//		prodInfo.setOrdrwy(draw.getOrdrwy());
//		prodInfo.setDramwy(draw.getDramwy());
//		prodInfo.setDrmiam(draw.getDrmiam());
//		prodInfo.setDrmxam(draw.getDrmxam());
//		prodInfo.setDrtmwy(draw.getDrtmwy());
//		prodInfo.setDrmitm(draw.getDrmitm());
//		prodInfo.setDrmxtm(draw.getDrmxtm());
//		prodInfo.setDrrule(draw.getDrrule());
//		
//		prodInfo.setSetpwy(drplan.getSetpwy());
//		prodInfo.setTermwy(drplan.getTermwy());
//		prodInfo.setDradpd(drplan.getDradpd());
//		prodInfo.setDredwy(drplan.getDredwy());
//		prodInfo.setDrcrwy(drplan.getDrcrwy());
//		prodInfo.setDrdfsd(drplan.getDrdfsd());
//		prodInfo.setDrdfwy(drplan.getDrdfwy());
//		
//		prodInfo.setDeptdy(term.getDeptdy()); //存期天数
//		
//		if (CommUtil.isNotNull(addt)){
//			prodInfo.setAccatp(addt.getAccatp());
//		}
//		
//		prodInfo.setAcctno(acctno);
//		
//		return prodInfo;
		
	}
}
