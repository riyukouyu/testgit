package cn.sunline.ltts.busi.dp.froz;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstProd;
import cn.sunline.edsp.busi.dp.type.jfbase.JFBaseEnumType;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.base.DecryptConstant;
import cn.sunline.ltts.busi.dp.domain.DpUnfrEntity;
import cn.sunline.ltts.busi.dp.namedsql.DpFrozDao;
import cn.sunline.ltts.busi.dp.serviceimpl.DpAcctSvcImpl;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozAcctDetl;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozAcctDetlDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetl;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetlDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwne;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwneDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbUnfr;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbUnfrDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoCustInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoCustacDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoCustacInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpUnStopPayIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpUnStopPayOt;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpUnStopayIn;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSTOP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SPTYPE;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_STOPTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_STTMCT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_STUNTP;

public class DpUnfrProc {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(DpAcctSvcImpl.class);
	
	/**
	 * 解冻检查
	 * 
	 * @param cpliodpunfrozin
	 */
	
	public static void unfrByLawCheck(IoDpUnStopPayIn cpliodpunfrozin) {
		
		if (CommUtil.isNull(cpliodpunfrozin.getFrozwy())) {
			throw DpModuleError.DpstComm.BNAS0816();
		}
		
		if(cpliodpunfrozin.getFrozwy() != E_FROZWY.TSOLVE){
			throw DpModuleError.DpstComm.BNAS0815();
		}
		
		if (CommUtil.isNull(cpliodpunfrozin.getCardno())) {
			throw DpModuleError.DpstProd.BNAS0926();
		}
		
		/*if(JFBaseEnumType.E_STACTP.STSA == cpliodpunfrozin.getStactp()) {
        	if (CommUtil.isNull(cpliodpunfrozin.getAcalno())) {
                throw JFDpError.EAcct.E0034();
            }
        }*/
		 
		/*if (CommUtil.isNull(cpliodpunfrozin.getCustna())) {
			throw DpModuleError.DpstComm.BNAS0524();
		}*/
		
		if (CommUtil.isNull(cpliodpunfrozin.getCrcycd())) {
			throw DpModuleError.DpstComm.BNAS0665();
		}
		/*if (CommUtil.isNull(cpliodpunfrozin.getFrogna())) {
			throw DpModuleError.DpstComm.BNAS0103();
		}*/
		if (CommUtil.isNull(cpliodpunfrozin.getOdfrno())) {
			throw DpModuleError.DpstComm.BNAS0220();
		}
		/*if (CommUtil.isNull(cpliodpunfrozin.getStactp())) {
			throw BizAcct.E0011();
		}
		if (CommUtil.isNull(cpliodpunfrozin.getUfexog())) {
			throw DpModuleError.DpstComm.BNAS0106();
		}
		if (CommUtil.isNull(cpliodpunfrozin.getUfctno())) {
			throw DpModuleError.DpstComm.BNAS0584();
		}
		if (CommUtil.isNull(cpliodpunfrozin.getUfna01())) {
			throw DpModuleError.DpstComm.BNAS0103();
		}
		if (CommUtil.isNull(cpliodpunfrozin.getIdno01())) {
			throw DpModuleError.DpstComm.BNAS0102();
		}
		if (CommUtil.isNull(cpliodpunfrozin.getIdtp01())) {
			throw DpModuleError.DpstComm.BNAS0100();
		}
		if (CommUtil.isNull(cpliodpunfrozin.getUfna02())) {
			throw DpModuleError.DpstComm.BNAS0099();
		}
		if (CommUtil.isNull(cpliodpunfrozin.getIdno02())) {
			throw DpModuleError.DpstComm.BNAS1599();
		}
		if (CommUtil.isNull(cpliodpunfrozin.getIdtp02())) {
			throw DpModuleError.DpstComm.BNAS0098();
		}*/
		//校验证件类型、证件号码
//		if(cpliodpunfrozin.getIdtp01() == E_IDTFTP.SFZ){
//			cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp = cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP.SFZ;
//			BusiTools.chkCertnoInfo(idtftp, cpliodpunfrozin.getIdno01());
//		}
		//校验证件类型、证件号码
//		if(cpliodpunfrozin.getIdtp02() == E_IDTFTP.SFZ){
//			cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp = cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP.SFZ;
//			BusiTools.chkCertnoInfo(idtftp, cpliodpunfrozin.getIdno02());
//		}
		/*if (CommUtil.isNull(cpliodpunfrozin.getUfreas())) {
			throw DpModuleError.DpstComm.BNAS0583();
	    }*/
		if (CommUtil.equals(cpliodpunfrozin.getCrcycd(), BusiTools.getDefineCurrency()) && CommUtil.isNotNull(cpliodpunfrozin.getCsextg())) {
			throw DpModuleError.DpstComm.BNAS0664();
		}
	}

	/**
	 *  解冻基本检查
	 * 
	 * @param cpliodpunfrozin
	 */
	private static void unStopPayCheck(IoDpUnStopPayIn cpliodpunfrozin,
			KnbFroz tblKnbFroz, DpUnfrEntity entity) {

		if(CommUtil.isNull(tblKnbFroz)){
			throw DpModuleError.DpstComm.BNAS0217();
		}
		
		/*if(E_FROZST.INVALID == tblKnbFroz.getFrozst()){
			throw DpModuleError.DpstComm.BNAS0732();
		}
		
		if(!cpliodpunfrozin.getUfexog().equals(tblKnbFroz.getFrexog())){
			throw DpModuleError.DpstComm.BNAS0582();
		}*/
		
//		if(!CommUtil.equals(tblKnbFroz.getTranbr(), CommTools.getBaseRunEnvs().getTrxn_branch())){
//			throw DpModuleError.DpstComm.E9999("解冻机构必须为原冻结机构");
//		}
		
		if(!cpliodpunfrozin.getCrcycd().equals(tblKnbFroz.getCrcycd())){
			throw DpModuleError.DpstComm.BNAS1103();
		}
		
		/*if(!cpliodpunfrozin.getFrogna().equals(tblKnbFroz.getFrogna())){
			throw DpModuleError.DpstComm.BNAS0105();
		}*/
		
		IoCaKnaAcdc caKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).
				getKnaAcdcOdb2(cpliodpunfrozin.getCardno(), false);
		if(CommUtil.isNull(caKnaAcdc)){
			throw DpModuleError.DpstComm.BNAS0692();
		}
		
		if(!caKnaAcdc.getCustac().equals(tblKnbFroz.getCustac())) {
				throw DpModuleError.DpAcct.AT020067();
		}
		
/*//		if(caKnaAcdc.getStatus() != E_DPACST.NORMAL){
//			throw DpModuleError.DpstComm.E9999("根据电子账号获取电子账号ID状态不正常");
//		}
		String custac = caKnaAcdc.getCustac();
		
		// 获取客户账号信息
		IoCaKnaCust tblKnaCust = SysUtil.getInstance(IoCaSevQryTableInfo.class).
				getKnaCustByCustacOdb1(custac, true);
		
		// 交易发起法人需与电子账号法人一致
		if (!CommUtil.equals(tblKnaCust.getCorpno(), CommTools.getBaseRunEnvs().getBusi_org_id())) {
			throw DpModuleError.DpstComm.BNAS0793();
		}*/
		// 客户账户状态检查
//		if (tblKna_cust.getAcctst() != E_ACCTST.NORMAL) {
//			throw DpModuleError.DpstComm.E9999("客户账号[" + tblKna_cust.getCustac()+ "]不正常，不能做冻结业务");
//		}
		//账户状态为预开户、预销户、销户的，交易拒绝，报错：“电子账户状态为***状态，无法操作！”。
//		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
//		bizlog.debug("+++++++++++++++++++++++++++++"+"电子账户状态为"+ cuacst +"+++++++++++++++++++++++++");
		
//		if(cuacst == E_CUACST.CLOSED) {
//			throw DpModuleError.DpstComm.E9999("电子账户状态为销户状态，无法操作！");
//		
//		}else if(cuacst == E_CUACST.PREOPEN){
//			throw DpModuleError.DpstComm.E9999("电子账户状态为预开户状态，无法操作！");
//		}
//		else if(cuacst == E_CUACST.PRECLOS){
//			throw DpModuleError.DpstComm.E9999("电子账户状态为预销户状态，无法操作！");
//		
//		}
		/*if(tblKnbFroz.getStactp() != cpliodpunfrozin.getStactp()) {
			throw BizAcct.E0010();
		}
		 
		if (!CommUtil.equals(custac, tblKnbFroz.getCustac())) {
			throw DpModuleError.DpstComm.BNAS0912();
		}

		//根据电子账号查找户名
//		String custna = tblKnaCust.getCustna();
		
//		if(!cpliodpunfrozin.getCustna().equals(custna)){
//			throw DpModuleError.DpstComm.BNAS0532();
//		}
		
		// 输入的冻结日期和原处于冻结状态下的冻结日期比较检查
		/*KnbFroz tblKnbFroz8 = KnbFrozDao.selectOne_odb8(cpliodpunfrozin.getOdfrno(), getMaxFrozstFrozsq1(cpliodpunfrozin), true);
		if (!CommUtil.equals(cpliodpunfrozin.getFrozdt(), tblKnbFroz8.getFrbgdt())) {
			throw DpModuleError.DpstComm.BNAS0825();
		}
		
		// 输入的冻结到期日和原处于冻结状态下的冻结到期日比较检查
		if (!CommUtil.equals(cpliodpunfrozin.getFreddt(), tblKnbFroz8.getFreddt())) {
			throw DpModuleError.DpstComm.BNAS0832();
		}*/
		
		//根据冻结明细登记簿中该冻结编号在冻结状态下最小冻结序号而查到的唯一记录
		KnbFrozDetl tblKnbFrozDetl2 = KnbFrozDetlDao.selectOne_odb2(cpliodpunfrozin.getOdfrno(), getMinFrozDetlsq(cpliodpunfrozin), true);
		
		// 解冻只能和冻结同渠道（日终不检查）

		// 解冻金额检查
		if (tblKnbFroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
			if (CommUtil.isNull(cpliodpunfrozin.getUnfram())) {
				throw DpModuleError.DpstComm.BNAS0585();
			}
			if (CommUtil.compare(cpliodpunfrozin.getUnfram(), BigDecimal.ZERO) <= 0) {
				throw DpModuleError.DpstComm.BNAS0588();
			}
			if (CommUtil.compare(cpliodpunfrozin.getUnfram(),tblKnbFrozDetl2.getFrozbl()) > 0) {
				throw DpModuleError.DpstComm.BNAS0586();
			}
		} else {
			// 非金额冻结不能输入金额
			if (CommUtil.compare((CommUtil.nvl(cpliodpunfrozin.getUnfram(), BigDecimal.ZERO)),BigDecimal.ZERO)!=0) {
				throw DpModuleError.DpstComm.BNAS0218();
			}
		}
		
//		long num = DpFrozDao.selUnfrctno(cpliodpunfrozin.getUfexog(), tblKnbFroz.getFrogna(), cpliodpunfrozin.getUfctno(), true);
//		if (num > 0) {
//			throw DpModuleError.DpstComm.E9999("同一解冻通知书不能解冻多次");
//		}
		
		//entity.setCustno(tblKnaCust.getCustno());
	}

	/**
	 * @author douwenbo
	 * @date 2016-04-22 16:32
	 * 解冻处理
	 * 
	 * @param cpliodpunfrozin
	 * @param tblKnbFroz
	 */
	public static void unFrozDo(IoDpUnStopPayIn cpliodpunfrozin,
			KnbFroz tblKnbFroz) {
		
		// 设置解冻登记簿信息
		KnbUnfr tblKnbunfr = SysUtil.getInstance(KnbUnfr.class);
		tblKnbunfr.setOdfrno(tblKnbFroz.getFrozno());
		String custac="";
		custac=tblKnbFroz.getCustac();
		tblKnbunfr.setCustac(custac);
		tblKnbunfr.setUfcttp(cpliodpunfrozin.getUfcttp());
		tblKnbunfr.setUfctno(cpliodpunfrozin.getUfctno());
		tblKnbunfr.setUfexog(cpliodpunfrozin.getUfexog());
		tblKnbunfr.setUfogna(tblKnbFroz.getFrogna());
		tblKnbunfr.setIdno01(cpliodpunfrozin.getIdno01());
		tblKnbunfr.setIdtp01(cpliodpunfrozin.getIdtp01());
		tblKnbunfr.setUfna01(cpliodpunfrozin.getUfna01());
		tblKnbunfr.setIdno02(cpliodpunfrozin.getIdno02());
		tblKnbunfr.setIdtp02(cpliodpunfrozin.getIdtp02());
		tblKnbunfr.setUfna02(cpliodpunfrozin.getUfna02());
		tblKnbunfr.setUnfrdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 解冻日期
		tblKnbunfr.setUnfrtm(CommTools.getBaseRunEnvs().getComputer_time()); // 解冻时间
		tblKnbunfr.setUfreas(cpliodpunfrozin.getUfreas()); // 解冻原因
		tblKnbunfr.setUnmark(cpliodpunfrozin.getRemark()); //解冻备注
		tblKnbunfr.setUnfram(cpliodpunfrozin.getUnfram()); //解冻金额
		tblKnbunfr.setIsflag(E_YES___.NO);//是否冲正
		tblKnbunfr.setMtdate(CommTools.getBaseRunEnvs().getTrxn_date());//维护日期

		tblKnbunfr.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
		tblKnbunfr.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq()); // 交易流水
		tblKnbunfr.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller()); // 交易柜员
		tblKnbunfr.setCkuser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 授权柜员
		//tblKnbunfr.setCptrcd(CommTools.getBaseRunEnvs().getCptrcd()); // 对账代码
		tblKnbunfr.setLttscd(CommTools.getBaseRunEnvs().getTrxn_code()); // 内部交易码
		tblKnbunfr.setPruser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 审批柜员
		tblKnbunfr.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
		tblKnbunfr.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
		
		tblKnbunfr.setCrcycd(tblKnbFroz.getCrcycd());//设置币种
		tblKnbunfr.setUnfrsq(getUnfrsqAddOne(cpliodpunfrozin.getOdfrno()));//设置解冻序号为当前续冻记录的冻结序号
		tblKnbunfr.setOdfrsq(tblKnbFroz.getFrozsq());//原冻结序号
		tblKnbunfr.setStactp(cpliodpunfrozin.getStactp());// 冻结账户类型
		if(CommUtil.isNotNull(cpliodpunfrozin.getIdno01())) {
			tblKnbunfr.setTmidno01(DecryptConstant.maskIdCard(cpliodpunfrozin.getIdno01()));
		}
	    if(CommUtil.isNotNull(cpliodpunfrozin.getIdno02())) {
			tblKnbunfr.setTmidno02(DecryptConstant.maskIdCard(cpliodpunfrozin.getIdno02()));
		}
		if(CommUtil.isNotNull(cpliodpunfrozin.getUfna01())) {
			tblKnbunfr.setTmufna01(DecryptConstant.maskName(cpliodpunfrozin.getUfna01()));
		}
		if(CommUtil.isNotNull(cpliodpunfrozin.getUfna02())) {
			tblKnbunfr.setTmufna02(DecryptConstant.maskName(cpliodpunfrozin.getUfna02()));
		}
		/*add  by  zhx  20180110
		if (tblKnbFroz.getFrozow() != E_FROZOW.AUACCT) {
			throw DpModuleError.DpstComm.BNAS0198();
		}
		*/
		
		//查询当前最大冻结明细信息
		KnbFrozDetl detlInfo = KnbFrozDetlDao.selectOne_odb2(tblKnbFroz.getFrozno(), tblKnbFroz.getFrozsq(), true);

		//		List<KnbFrozDetl> lstKnbFrozDetl = null;
		switch (tblKnbFroz.getFrozow()) {
		/*case CUSTAC:
			custacUnStopPay(tblKnbunfr, tblKnbFroz);
			break;*/
		case AUACCT://智能储蓄
			// 解止时，指定金额解止必须等于原止付金额
			if (tblKnbFroz.getFrlmtp() == E_FRLMTP.AMOUNT
			    && tblKnbFroz.getFroztp() == E_FROZTP.BANKSTOPAY
			    && !CommUtil.equals(detlInfo.getFrozbl(),cpliodpunfrozin.getUnfram())) {
				throw DpModuleError.DpstComm.BNAS1625();
			}
			auacctUnFroz(detlInfo, tblKnbFroz, cpliodpunfrozin);
			break;
		case ACCTNO: 
			// 获取冻结明细信息并循环处理
			List<KnbFrozDetl> lstKnbFrozDetl = KnbFrozDetlDao.selectAll_odb1(
					tblKnbFroz.getFrozno(), false);
			for (KnbFrozDetl tblKnbfrozdetl_tmp : lstKnbFrozDetl) {

				/*
				 * 单笔解冻检查 1.原冻结是金额冻结，则检查解冻金额
				 */
				if (tblKnbFroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
					if (!CommUtil.equals(tblKnbfrozdetl_tmp.getFrozbl(),
							cpliodpunfrozin.getUnfram())) {
						throw DpModuleError.DpstComm.E9999("解冻金额必须等于冻结余额");
					}

					// 设置冻结余额=冻结余额-解冻金额
					tblKnbfrozdetl_tmp.setFrozbl(tblKnbfrozdetl_tmp.getFrozbl()
							.subtract(cpliodpunfrozin.getUnfram()));
				}
				// 单笔解冻
				oneAcctUnStopPay(tblKnbfrozdetl_tmp, tblKnbunfr, tblKnbFroz);
			}
			break;
		default:
			throw DpModuleError.DpstComm.BNAS1082();
		}
		
		// 获取原冻结登记簿信息
		KnbFroz tblKnbFroz8 = KnbFrozDao.selectOne_odb8(cpliodpunfrozin.getOdfrno(), getMinFrozstFrozsq(cpliodpunfrozin), false);

		Long odfrsq = tblKnbFroz.getFrozsq();
		Long odfrsq2 = tblKnbFroz8.getFrozsq();
		
		if(odfrsq.equals(odfrsq2)){//处于冻结下的记录只有唯一一条
			// 登记解冻登记簿
			tblKnbunfr.setOdfrsq(odfrsq);
			KnbUnfrDao.insert(tblKnbunfr);
		}else{
			// 登记解冻登记簿(生效冻结的解冻记录)
			tblKnbunfr.setOdfrsq(odfrsq2);
			tblKnbunfr.setUnfrsq(getUnfrsqAddOne(cpliodpunfrozin.getOdfrno()));
			KnbUnfrDao.insert(tblKnbunfr);
			
			//日终批量解冻则不需要登记续冻解冻记录，手工解冻则同步生成续冻解冻记录  dpunfr--手工解冻交易码   auunfr--日终跑批解冻交易码
			if(CommUtil.equals(BusiTools.getBusiRunEnvs().getTrxn_code(), "dpunfr") || CommUtil.equals(BusiTools.getBusiRunEnvs().getTrxn_code(), "deduct")){
				
				//登记续冻的冻结解冻记录
				KnbUnfr tblKnbunfr2 = SysUtil.getInstance(KnbUnfr.class);
				CommUtil.copyProperties(tblKnbunfr2, tblKnbunfr);
				tblKnbunfr2.setOdfrsq(odfrsq);
				tblKnbunfr2.setUnfrsq(getUnfrsqAddOne(cpliodpunfrozin.getOdfrno()));
				
				//判断当前续冻余额是否大于解冻金额
				if(CommUtil.compare(cpliodpunfrozin.getUnfram(), detlInfo.getFrozbl()) > 0){
					tblKnbunfr2.setUnfram(detlInfo.getFrozbl());
					
				}else{
					tblKnbunfr2.setUnfram(cpliodpunfrozin.getUnfram());
					
				}
				
				KnbUnfrDao.insert(tblKnbunfr2);
				
			}
			
			
			//批量执行SQL，插入数据到解冻登记簿中
//			List<KnbUnfr> knbUnfrList = new ArrayList<KnbUnfr>();
//			knbUnfrList.add(tblKnbunfr);
//			knbUnfrList.add(tblKnbunfr2);
//			DaoUtil.insertBatch(KnbUnfr.class, knbUnfrList);
		}
	
	}

	/**
	 * @author douwenbo
	 * @date 2016-04-26 14:24
	 * @param cplioctfrozin
	 * 获取冻结明细登记簿中处于冻结状态下的最大的冻结序号(冻结明细登记簿)
	 * @return frozsq冻结序号
	 */
	public static long getMaxDetlFrozsq(IoDpUnStopPayIn cpliodpunfrozin){
		long frozsq = 1;
		if(CommUtil.isNull(DpFrozDao.selFrozDetlMaxseq(cpliodpunfrozin.getOdfrno(),E_FROZST.VALID, false))){
			return frozsq;
		}else{
			return DpFrozDao.selFrozDetlMaxseq(cpliodpunfrozin.getOdfrno(),E_FROZST.VALID, false);
		}
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-04-22 11:09
	 * 解冻处理 先解冻明明细登记簿再解冻冻结登记簿 (部分解冻暂不实现)
	 * 
	 * @param cpliodpunfrozin
	 */
	public static void prcUnfroz(IoDpUnStopPayIn cpliodpunfrozin, DpUnfrEntity entity) {
		
		//判断当前冻结编号是否存在
		List<KnbFroz> InfoList = KnbFrozDao.selectAll_odb4(cpliodpunfrozin.getOdfrno(), false);
		if(CommUtil.isNull(InfoList)){
			throw DpModuleError.DpstComm.BNAS0739();
		}
		
		//判断当前冻结编号是否存在冻结
		List<KnbFroz> frozList = KnbFrozDao.selectAll_odb5(cpliodpunfrozin.getOdfrno(), E_FROZST.VALID, false);
		if(CommUtil.isNull(frozList)){
			throw DpModuleError.DpstComm.BNAS0733();
		}
		
		//获取原冻结编号在冻结状态下的最大序号(冻结登记簿)
		long maxFrozsq = DpFrozDao.selFrozFrozstMaxseq(cpliodpunfrozin.getOdfrno(),E_FROZST.VALID, false);
		if(CommUtil.isNull(maxFrozsq)){
			throw DpModuleError.DpstComm.BNAS0738();
		}
		
		// 获取原冻结登记簿信息
		KnbFroz tblKnbFroz8 = KnbFrozDao.selectOne_odb8(cpliodpunfrozin.getOdfrno(), maxFrozsq, false);

		// 解冻基本检查
		unStopPayCheck(cpliodpunfrozin, tblKnbFroz8, entity);

		// 解冻处理
		unFrozDo(cpliodpunfrozin, tblKnbFroz8);
	    cpliodpunfrozin.setAcctno(tblKnbFroz8.getCustac());
		
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-04-22 10:20
	 * @param cpliodpunfrozin
	 * 设置解冻序号默认为1，同一个冻结编号解冻时在最大序号上加1
	 * @return unfrsq解冻序号
	 */
	public static long getUnfrsqAddOne(String odfrno){
		long unfrsq = 1;
		if(CommUtil.isNull(DpFrozDao.selUnFrMaxseq(odfrno, false))){
			return unfrsq;
		}else{
			return DpFrozDao.selUnFrMaxseq(odfrno, false) + 1;
		}
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-04-25 16:43
	 * @param cpliodpunfrozin
	 * 获取最大冻结序号(冻结登记簿)
	 * @return frozsq解冻序号
	 */
	public static long getMaxFrozsq(IoDpUnStopPayIn cpliodpunfrozin){
		long frozsq = 1;
		if(CommUtil.isNull(DpFrozDao.selFrozMaxseq(cpliodpunfrozin.getOdfrno(), false))){
			return frozsq;
		}else{
			return DpFrozDao.selFrozMaxseq(cpliodpunfrozin.getOdfrno(), false);
		}
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-05-05 10:26
	 * @param cpliodpunfrozin
	 * 获取在冻结状态下的最大冻结序号(冻结登记簿)
	 * @return frozsq解冻序号
	 */
	public static long getMaxFrozstFrozsq(IoDpUnStopPayIn cpliodpunfrozin){
		long frozsq = 1;
		if(CommUtil.isNull(DpFrozDao.selFrozFrozstMaxseq(cpliodpunfrozin.getOdfrno(),E_FROZST.VALID, false))){
			return frozsq;
		}else{
			return DpFrozDao.selFrozFrozstMaxseq(cpliodpunfrozin.getOdfrno(),E_FROZST.VALID, false);
		}
	}
	
	
	/**
	 * @author douwenbo
	 * @date 2016-04-28 09:57
	 * @param cpliodpunfrozin
	 * 获取在冻结状态下的最小冻结序号(冻结登记簿)
	 * @return frozsq解冻序号
	 */
	public static long getMinFrozstFrozsq(IoDpUnStopPayIn cpliodpunfrozin){
		long frozsq = 1;
		if(CommUtil.isNull(DpFrozDao.selFrozMinseq(cpliodpunfrozin.getOdfrno(),E_FROZST.VALID, false))){
			return frozsq;
		}else{
			return DpFrozDao.selFrozMinseq(cpliodpunfrozin.getOdfrno(),E_FROZST.VALID, false);
		}
	}
	public static long getMaxFrozstFrozsq1(IoDpUnStopPayIn cpliodpunfrozin){
		long frozsq = 1;
		if(CommUtil.isNull(DpFrozDao.selFrozMaxseq1(cpliodpunfrozin.getOdfrno(),E_FROZST.VALID, false))){
			return frozsq;
		}else{
			return DpFrozDao.selFrozMaxseq1(cpliodpunfrozin.getOdfrno(),E_FROZST.VALID, false);
		}
	}
	/**
	 * @author douwenbo
	 * @date 2016-04-29 10:30
	 * @param cpliodpunfrozin
	 * 获取在冻结状态下的最小冻结序号(冻结明细登记簿)
	 * @return frozsq解冻序号
	 */
	public static long getMinFrozDetlsq(IoDpUnStopPayIn cpliodpunfrozin){
		long frozsq = 1;
		if(CommUtil.isNull(DpFrozDao.selFrozDetlMinseq(cpliodpunfrozin.getOdfrno(),E_FROZST.VALID, false))){
			return frozsq;
		}else{
			return DpFrozDao.selFrozDetlMinseq(cpliodpunfrozin.getOdfrno(),E_FROZST.VALID, false);
		}
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-04-21 09:42
	 * 
	 * 解冻----设置字段到输出接口中
	 * @param cplIoDpUnStopPayOt 解冻的输出接口
	 * @param cpliodpunfrozin    解冻的输入接口
	 */
	public static IoDpUnStopPayOt setUnfiOutPut(IoDpUnStopPayIn cpliodpunfrozin, DpUnfrEntity entity) {
		
		//根据冻结编号查找冻结终止日期、有权机构
		List<KnbFroz> tblKnaFroz = KnbFrozDao.selectAll_odb4(cpliodpunfrozin.getOdfrno(), false);
		if(CommUtil.isNull(tblKnaFroz)){
			throw DpModuleError.DpstComm.BNAS1626();
		}
		
		IoDpUnStopPayOt cplIoDpUnStopPayOt = SysUtil.getInstance(IoDpUnStopPayOt.class);
		
		CommUtil.copyProperties(cplIoDpUnStopPayOt, cpliodpunfrozin);
		
		cplIoDpUnStopPayOt.setCustno(entity.getCustno());//客户号
		cplIoDpUnStopPayOt.setRemark(cpliodpunfrozin.getRemark());//解冻备注
		cplIoDpUnStopPayOt.setOdfrno(cpliodpunfrozin.getOdfrno());//原冻结编号
		
		return cplIoDpUnStopPayOt;
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-05-19 22:20
	 * 解止处理
	 * @param cpliodpunfrozin
	 * @param tblKnbFroz
	 */
	public static void unStopPayDo(IoDpUnStopayIn cpliodpunfrozin,KnbFroz tblKnbFroz) {

		tblKnbFroz.setFrozow(E_FROZOW.AUACCT);//设置冻结主体类型为智能储蓄
		
		// 设置解冻登记簿信息
		KnbUnfr tblKnbunfr = SysUtil.getInstance(KnbUnfr.class);
		tblKnbunfr.setOdfrno(tblKnbFroz.getFrozno());//止付编号
		tblKnbunfr.setCustac(tblKnbFroz.getCustac());//电子账号
		tblKnbunfr.setUfcttp("");
		tblKnbunfr.setUfctno(cpliodpunfrozin.getStbkno());//解止通知书编号
		tblKnbunfr.setUfexog(cpliodpunfrozin.getStdptp());//解止执法部门类型
		tblKnbunfr.setUfogna(tblKnbFroz.getFrogna());//解止部门名称
		tblKnbunfr.setIdno01(cpliodpunfrozin.getIdno01());
		tblKnbunfr.setIdtp01(cpliodpunfrozin.getIdtp01());
		tblKnbunfr.setUfna01(cpliodpunfrozin.getFrna01());
		tblKnbunfr.setIdno02(cpliodpunfrozin.getIdno02());
		tblKnbunfr.setIdtp02(cpliodpunfrozin.getIdtp02());
		tblKnbunfr.setUfna02(cpliodpunfrozin.getFrna02());
		tblKnbunfr.setUnfrdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 解止日期
//		tblKnbunfr.setUnfrtm(BusiTools.getBusiRunEnvs().getTrantm()); // 解止时间
		tblKnbunfr.setUnfrtm(CommTools.getBaseRunEnvs().getComputer_time());
		tblKnbunfr.setUfreas(cpliodpunfrozin.getSfreas()); // 解止原因
		tblKnbunfr.setUnfrsq(getUnfrsqAddOne(tblKnbFroz.getFrozno()));//设置解冻序号
		tblKnbunfr.setOdfrsq(tblKnbFroz.getFrozsq());//原冻结序号
		tblKnbunfr.setMtdate(CommTools.getBaseRunEnvs().getTrxn_date());//维护日期

		tblKnbunfr.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
		tblKnbunfr.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq()); // 交易流水
		tblKnbunfr.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller()); // 交易柜员
//		tblKnbunfr.setCkuser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 授权柜员
		tblKnbunfr.setCkuser(CommTools.getBaseRunEnvs().getTrxn_teller());
		//tblKnbunfr.setCptrcd(CommTools.getBaseRunEnvs().getCptrcd()); // 对账代码
//		tblKnbunfr.setLttscd(BusiTools.getBusiRunEnvs().getLttscd()); // 内部交易码
		tblKnbunfr.setLttscd(CommTools.getBaseRunEnvs().getTrxn_code());
		tblKnbunfr.setPruser(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); // 审批柜员
		tblKnbunfr.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
		tblKnbunfr.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
		tblKnbunfr.setStactp(cpliodpunfrozin.getStactp());
		if(CommUtil.isNotNull(cpliodpunfrozin.getIdno01())) {
			tblKnbunfr.setTmidno01(DecryptConstant.maskIdCard(cpliodpunfrozin.getIdno01()));
		}
		if(CommUtil.isNotNull(cpliodpunfrozin.getIdno02())) {
			tblKnbunfr.setTmidno02(DecryptConstant.maskIdCard(cpliodpunfrozin.getIdno02()));
		}
		if(CommUtil.isNotNull(cpliodpunfrozin.getFrna01())) {
			tblKnbunfr.setTmufna01(DecryptConstant.maskName(cpliodpunfrozin.getFrna01()));
		}
		if(CommUtil.isNotNull(cpliodpunfrozin.getFrna02())) {
			tblKnbunfr.setTmufna02(DecryptConstant.maskName(cpliodpunfrozin.getFrna02()));
		}
//		List<KnbFrozDetl> lstKnbFrozDetl = null;
		switch (tblKnbFroz.getFrozow()) {
		/*case CUSTAC:
			custacUnStopPay(tblKnbunfr, tblKnbFroz);
			break;*/
		case AUACCT:
			KnbFrozDetl tblKnbfrozdetl = KnbFrozDetlDao.selectOne_odb2(
					tblKnbFroz.getFrozno(), tblKnbFroz.getFrozsq(), true);
			// 解止时，指定金额解止必须等于原止付金额
			if (tblKnbFroz.getFrlmtp() == E_FRLMTP.AMOUNT
					&& tblKnbFroz.getFroztp() == E_FROZTP.BANKSTOPAY
					&& !CommUtil.equals(tblKnbfrozdetl.getFrozbl(),cpliodpunfrozin.getStopam())) {
				throw DpModuleError.DpstComm.BNAS0580();
			}
			// 解冻时，指定金额解冻必须小于等于原冻结金额
			if (tblKnbFroz.getFrlmtp() == E_FRLMTP.AMOUNT
					&& (tblKnbFroz.getFroztp() == E_FROZTP.JUDICIAL || tblKnbFroz.getFroztp() == E_FROZTP.ADD)
					&& (CommUtil.compare(cpliodpunfrozin.getStopam(),tblKnbfrozdetl.getFrozbl())>0)) {
				throw DpModuleError.DpstComm.BNAS0587();
			}
			// 存款证明撤销时解止金额为原止付金额
			if (tblKnbFroz.getFroztp() == E_FROZTP.DEPRSTOPAY) {
				cpliodpunfrozin.setStopam(tblKnbfrozdetl.getFrozam());
			}
			auacctUnStopay(tblKnbfrozdetl, tblKnbFroz, cpliodpunfrozin);
			break;
		/*case ACCTNO: {
			// 获取冻结明细信息并循环处理
			lstKnbFrozDetl = KnbFrozDetlDao.selectAll_odb1(tblKnbFroz.getFrozno(), false);
			for (KnbFrozDetl tblKnbfrozdetl_tmp : lstKnbFrozDetl) {

				*//**
				 * 单笔解冻检查 1.原冻结是金额冻结，则检查解冻金额
				 *//*
				if (tblKnbFroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
					if (!CommUtil.equals(tblKnbfrozdetl_tmp.getFrozbl(),cpliodpunfrozin.getStopam())) {
						throw DpModuleError.DpstComm.E9999("解冻金额必须等于冻结余额");
					}

					// 设置冻结余额=冻结余额-解冻金额
					tblKnbfrozdetl_tmp.setFrozbl(tblKnbfrozdetl_tmp.getFrozbl().subtract(cpliodpunfrozin.getStopam()));
				}
				// 单笔解冻
				oneAcctUnStopPay(tblKnbfrozdetl_tmp, tblKnbunfr, tblKnbFroz);
			}
			break;
		}*/
		default:
			throw DpModuleError.DpstComm.BNAS1082();
		}

		// 更新原冻结登记簿状态和解除日期
		tblKnbFroz.setFrozst(E_FROZST.INVALID);
		tblKnbFroz.setUnfrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		KnbFrozDao.updateOne_odb2(tblKnbFroz);

		// 登记解冻登记簿
		KnbUnfrDao.insert(tblKnbunfr);

	}

	/**
	 * TODO 单户解冻 更改冻结明细登记簿冻结余额，冻结状态 更改账户冻结相关标志
	 * 
	 * @param tblKnbfrozdetl
	 * @param tblKnbunfr
	 */
	public static void oneAcctUnStopPay(KnbFrozDetl tblKnbfrozdetl,
			KnbUnfr tblKnbunfr, KnbFroz tblKnbFroz) {

		 tblKnbfrozdetl.setFrozst(E_FROZST.INVALID);
		
		 KnbFrozDetlDao.updateOne_odb2(tblKnbfrozdetl);
		
		 /*先delete,后续优化   by zhx */
		 KnbFrozOwneDao.deleteOne_odb1(tblKnbfrozdetl.getFrozow(),
		 tblKnbfrozdetl.getFrowid());
		 
		 KnbFroz knbFroz = KnbFrozDao.selectOne_odb1(tblKnbFroz.getFrozno(), E_FROZST.VALID, false);
		 if(CommUtil.isNotNull(knbFroz)) {
			 knbFroz.setFrozst(E_FROZST.INVALID);
			 KnbFrozDao.updateOne_odb2(knbFroz);
		 }
		 
		 /* 先把锁住
		 KnbFrozOwneDao.selectOneWithLock_odb1(tblKnbfrozdetl.getFrozow(),
		 tblKnbfrozdetl.getFrowid(), true);
		 long count = DpFrozDao.selFrozCount(tblKnbfrozdetl.getFrozow(),
		 tblKnbfrozdetl.getFrowid(), tblKnbFroz.getFrlmtp(),
		 E_FROZST.VALID, false);
		 if (0 == count) {
		 updFrozAcct(tblKnbfrozdetl.getFrowid(), tblKnbFroz.getFrlmtp());
		 }*/
	}

	/**
	 * TODO 客户账号解冻
	 * 
	 * @param tblKnbunfr
	 * @param tblKnbFroz
	 */
	public static void custacUnStopPay(KnbUnfr tblKnbunfr, KnbFroz tblKnbFroz) {
		// updForzCustac(tblKnbunfr.getCustac(), tblKnbFroz.getFrlmtp());

	}

	/**
	 * 智能储蓄解冻（解冻使用）
	 * 
	 * @param tblKnbunfr
	 * @param tblKnbFroz
	 */
	public static void auacctUnStopPay(KnbFrozDetl tblKnbfrozdetl,
			KnbFroz tblKnbFroz, IoDpUnStopPayIn cpliodpunfrozin) {
		// 设置冻结余额=冻结余额-解冻金额
		tblKnbfrozdetl.setFrozbl(tblKnbfrozdetl.getFrozbl().subtract(
				cpliodpunfrozin.getUnfram()));
		tblKnbfrozdetl.setFrozst(E_FROZST.INVALID);

		KnbFrozDetlDao.updateOne_odb2(tblKnbfrozdetl);

		updateKnbFrozOwne(tblKnbfrozdetl.getFrozow(),
				tblKnbfrozdetl.getFrowid(), tblKnbFroz.getFrlmtp(),
				cpliodpunfrozin.getUnfram());

	}
	
	/**
	 * @author douwenbo
	 * @date 2016-05-19 22:31
	 * 智能储蓄解止（解止使用）
	 * 
	 * @param tblKnbfrozdetl
	 * @param tblKnbFroz
	 * @param cpliodpunfrozin
	 */
	public static void auacctUnStopay(KnbFrozDetl tblKnbfrozdetl,KnbFroz tblKnbFroz, IoDpUnStopayIn cpliodpunfrozin) {
		// 设置冻结余额=冻结余额-解冻金额
		tblKnbfrozdetl.setFrozbl(tblKnbfrozdetl.getFrozbl().subtract(cpliodpunfrozin.getStopam()));
		tblKnbfrozdetl.setFrozst(E_FROZST.INVALID);

		KnbFrozDetlDao.updateOne_odb2(tblKnbfrozdetl);
        //判断是否为客户止付
		if(tblKnbFroz.getFroztp() == E_FROZTP.CUSTSTOPAY){
			mntnKnbFrozOwne(tblKnbfrozdetl.getFrozow(),tblKnbfrozdetl.getFrowid(), tblKnbFroz.getFrlmtp(),cpliodpunfrozin.getStopam(),tblKnbFroz.getCustac());
        
		}else {
        	updateKnbFrozOwne(tblKnbfrozdetl.getFrozow(),tblKnbfrozdetl.getFrowid(), tblKnbFroz.getFrlmtp(),cpliodpunfrozin.getStopam());
        }
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-04-25 16:13
	 * @param tblKnbfrozdetl
	 * @param tblKnbFroz
	 * @param cpliodpunfrozin
	 */
	public static void auacctUnFroz(KnbFrozDetl tblKnbfrozdetl,KnbFroz tblKnbFroz, IoDpUnStopPayIn cpliodpunfrozin) {
		
		// 获取原冻结明细登记簿信息
		KnbFrozDetl tblKnbfrozdetl2 = KnbFrozDetlDao.selectOne_odb2(cpliodpunfrozin.getOdfrno(), getMinFrozDetlsq(cpliodpunfrozin), true);
		
		//更新冻结明细登记簿状态,更新冻结登记簿状态
		updateFrozDetlSt(tblKnbfrozdetl, tblKnbFroz,cpliodpunfrozin,tblKnbfrozdetl2);
		
		// 即富改造
		if (E_YES___.YES == tblKnbFroz.getFricfg()) {
			updKnbFrozOwne(tblKnbfrozdetl.getFrozno(), tblKnbfrozdetl.getFrozow(),
					tblKnbfrozdetl.getFrowid(), tblKnbFroz.getFrlmtp(),
					cpliodpunfrozin.getUnfram());
		} else {
			updateKnbFrozOwne(tblKnbfrozdetl.getFrozow(),
					tblKnbfrozdetl.getFrowid(), tblKnbFroz.getFrlmtp(),
					cpliodpunfrozin.getUnfram());
		}
		
	}
	
	/**
	 * add by lishuyao 20191211
	 * @param frozno
	 * @param frozow
	 * @param frowid
	 * @param frlmtp
	 * @param unfram
	 */
	private static void updKnbFrozOwne(String frozno, E_FROZOW frozow, String frowid, E_FRLMTP frlmtp, BigDecimal unfram) {

		KnbFrozOwne tblKnbFrozOwne = SysUtil.getInstance(KnbFrozOwne.class);
		List<KnbFrozAcctDetl> lstKnbFrozAcctDetl = KnbFrozAcctDetlDao.selectAll_odb2(frozno, false);
		BigDecimal remdam = BigDecimal.ZERO;// 冻结剩余金额 
		if (!lstKnbFrozAcctDetl.isEmpty()) {
			for (KnbFrozAcctDetl tblKnbFrozAcctDetl : lstKnbFrozAcctDetl) {
				if (CommUtil.compare(BigDecimal.ZERO, unfram) >= 0) {
					break;
				}
				
				if (CommUtil.compare(BigDecimal.ZERO, tblKnbFrozAcctDetl.getFzrmam()) == 0) {
					continue;
				}
				
				remdam = CommUtil.compare(tblKnbFrozAcctDetl.getFzrmam(), unfram) >= 0 ? unfram : tblKnbFrozAcctDetl.getFzrmam();
				
				tblKnbFrozAcctDetl.setFzrmam(tblKnbFrozAcctDetl.getFzrmam().subtract(remdam));
				KnbFrozAcctDetlDao.updateOne_odb1(tblKnbFrozAcctDetl);
				tblKnbFrozOwne = KnbFrozOwneDao.selectOne_odb1(tblKnbFrozAcctDetl.getFrozow(), tblKnbFrozAcctDetl.getFrowid(), true);
				tblKnbFrozOwne.setFrozbl(tblKnbFrozOwne.getFrozbl().subtract(remdam));
				KnbFrozOwneDao.updateOne_odb1(tblKnbFrozOwne);
				
				unfram = unfram.subtract(remdam);
			}
		} 
		
	}

	/**
	 * @author douwenbo
	 * @date 2016-05-05 09:07
	 * 更新冻结明细登记簿的金额、余额
	 * @param tblKnbfrozdetl
	 * @param tblKnbfrozdetl2
	 * @param cpliodpunfrozin
	 */
	public static void updateFrozDetlBl(KnbFrozDetl tblKnbfrozdetl,KnbFrozDetl tblKnbfrozdetl2,IoDpUnStopPayIn cpliodpunfrozin){
		
		if(CommUtil.compare(tblKnbfrozdetl.getFrozbl().subtract(cpliodpunfrozin.getUnfram()), BigDecimal.ZERO) < 0){
			
			tblKnbfrozdetl.setFrozbl(BigDecimal.ZERO);
		}else{
			tblKnbfrozdetl.setFrozbl(tblKnbfrozdetl.getFrozbl().subtract(cpliodpunfrozin.getUnfram()));
		}
		
		if(!tblKnbfrozdetl.getFrozsq().equals(tblKnbfrozdetl2.getFrozsq())){//不是同一条记录
			
			tblKnbfrozdetl2.setFrozbl(tblKnbfrozdetl2.getFrozbl().subtract(cpliodpunfrozin.getUnfram()));
		}
		
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-04-26 19:18
	 * 
	 * @param tblKnbfrozdetl
	 * @param tblKnbFroz
	 * @return frozst 冻结状态
	 */
	public static void updateFrozDetlSt(KnbFrozDetl tblKnbfrozdetl,KnbFroz tblKnbFroz,
									  IoDpUnStopPayIn cpliodpunfrozin,KnbFrozDetl tblKnbfrozdetl2){
		
		// 获取原冻结登记簿信息
		KnbFroz tblKnbFroz8 = KnbFrozDao.selectOne_odb8(cpliodpunfrozin.getOdfrno(), getMinFrozstFrozsq(cpliodpunfrozin), false);
		
		E_FROZST frozst = E_FROZST.VALID;
		E_FROZST frozst2 = E_FROZST.VALID;
		String unfrdt = null;
		String unfrdt2 = null;
		
		if(tblKnbFroz.getFrlmtp() != E_FRLMTP.AMOUNT){//原限制类型不是指定金额
			
			if(tblKnbFroz.getFrozsq().equals(tblKnbFroz8.getFrozsq())){//冻结信息唯一的
				
				// 设置冻结余额为零
				tblKnbfrozdetl.setFrozbl(BigDecimal.ZERO);
				frozst = E_FROZST.INVALID;
				//更新冻结明细登记簿状态
				tblKnbfrozdetl.setFrozst(frozst);
				KnbFrozDetlDao.updateOne_odb2(tblKnbfrozdetl);
				
				//更新冻结登记簿状态
				tblKnbFroz.setFrozst(frozst);
				//更新解冻日期
				tblKnbFroz.setUnfrdt(CommTools.getBaseRunEnvs().getTrxn_date());
				
				KnbFrozDao.updateOne_odb8(tblKnbFroz);
			}
			if(!tblKnbFroz.getFrozsq().equals(tblKnbFroz8.getFrozsq())){//冻结信息不唯一的
				
				// 设置冻结余额为零
				tblKnbfrozdetl.setFrozbl(BigDecimal.ZERO);
				tblKnbfrozdetl2.setFrozbl(BigDecimal.ZERO);
				
				frozst = E_FROZST.INVALID;
				frozst2 = E_FROZST.INVALID;
				tblKnbfrozdetl.setFrozst(frozst);
				//更新冻结明细登记簿状态
				tblKnbfrozdetl2.setFrozst(frozst2);
				
				KnbFrozDetlDao.updateOne_odb2(tblKnbfrozdetl);
				KnbFrozDetlDao.updateOne_odb2(tblKnbfrozdetl2);
				
				//更新冻结登记簿状态
				tblKnbFroz.setFrozst(frozst);
				tblKnbFroz8.setFrozst(frozst2);
				//更新解冻日期
				tblKnbFroz.setUnfrdt(CommTools.getBaseRunEnvs().getTrxn_date());
				tblKnbFroz8.setUnfrdt(CommTools.getBaseRunEnvs().getTrxn_date());
				
				KnbFrozDao.updateOne_odb8(tblKnbFroz);
				KnbFrozDao.updateOne_odb8(tblKnbFroz8);
			}
		}else {
			if(tblKnbFroz.getFrozsq().equals(tblKnbFroz8.getFrozsq())){//冻结信息唯一的
					if(CommUtil.equals(cpliodpunfrozin.getUnfram(),tblKnbfrozdetl.getFrozbl())){//如果解冻金额等于冻结余额
						
						// 设置冻结余额=冻结余额-解冻金额
						updateFrozDetlBl(tblKnbfrozdetl,tblKnbfrozdetl2,cpliodpunfrozin);
						
						frozst = E_FROZST.INVALID;
						
						//更新冻结明细登记簿状态
						tblKnbfrozdetl.setFrozst(frozst);
						KnbFrozDetlDao.updateOne_odb2(tblKnbfrozdetl);
						
						//更新冻结登记簿状态
						tblKnbFroz.setFrozst(frozst);
						//更新解冻日期
						tblKnbFroz.setUnfrdt(CommTools.getBaseRunEnvs().getTrxn_date());
						
						KnbFrozDao.updateOne_odb8(tblKnbFroz);
					}else {
						// 设置冻结余额=冻结余额-解冻金额
						updateFrozDetlBl(tblKnbfrozdetl,tblKnbfrozdetl2,cpliodpunfrozin);
						
						frozst = E_FROZST.VALID;
						
						//更新冻结明细登记簿状态
						tblKnbfrozdetl.setFrozst(frozst);
						KnbFrozDetlDao.updateOne_odb2(tblKnbfrozdetl);
						
						//更新冻结登记簿状态
						tblKnbFroz.setFrozst(frozst);
						//更新解冻日期
						tblKnbFroz.setUnfrdt("");
						
						KnbFrozDao.updateOne_odb8(tblKnbFroz);
					}
			}
			if(!tblKnbFroz.getFrozsq().equals(tblKnbFroz8.getFrozsq())){//冻结的信息不唯一
				
				if(CommUtil.compare(cpliodpunfrozin.getUnfram(),tblKnbfrozdetl.getFrozbl()) >= 0){//如果解冻金额大于等于续冻的冻结余额
					
					//设置冻结余额
					tblKnbfrozdetl.setFrozbl(BigDecimal.ZERO);
					frozst = E_FROZST.INVALID;
					unfrdt = CommTools.getBaseRunEnvs().getTrxn_date();		
					
					if(CommUtil.compare(cpliodpunfrozin.getUnfram(),tblKnbfrozdetl2.getFrozbl()) == 0){
						frozst2 = E_FROZST.INVALID;
						//解冻日期
						unfrdt2 = CommTools.getBaseRunEnvs().getTrxn_date();
					}
					
					tblKnbfrozdetl2.setFrozbl(tblKnbfrozdetl2.getFrozbl().subtract(cpliodpunfrozin.getUnfram()));
				}
				if(CommUtil.compare(cpliodpunfrozin.getUnfram(),tblKnbfrozdetl.getFrozbl()) < 0){//如果解冻金额小于续冻的冻结余额
					frozst = E_FROZST.VALID;
					unfrdt = "";
					// 设置冻结余额=冻结余额-解冻金额
					tblKnbfrozdetl.setFrozbl(tblKnbfrozdetl.getFrozbl().subtract(cpliodpunfrozin.getUnfram()));
					tblKnbfrozdetl2.setFrozbl(tblKnbfrozdetl2.getFrozbl().subtract(cpliodpunfrozin.getUnfram()));
				}
				
				
				tblKnbfrozdetl.setFrozst(frozst);
				tblKnbfrozdetl2.setFrozst(frozst2);
				//更新冻结明细登记簿状态
				KnbFrozDetlDao.updateOne_odb2(tblKnbfrozdetl);
				KnbFrozDetlDao.updateOne_odb2(tblKnbfrozdetl2);
				
				//更新冻结登记簿状态
				tblKnbFroz.setFrozst(frozst);
				tblKnbFroz.setUnfrdt(unfrdt);
				tblKnbFroz8.setFrozst(frozst2);
				tblKnbFroz8.setUnfrdt(unfrdt2);
				KnbFrozDao.updateOne_odb8(tblKnbFroz);
				KnbFrozDao.updateOne_odb8(tblKnbFroz8);
				
			}
		}
	}
	
	/**
	 * 
	 * @param frozow
	 * @param frowid
	 * @param frlmtp
	 * @param unfram
	 */

	// 更改冻结主体信息
	public static void updateKnbFrozOwne(E_FROZOW frozow, String frowid,
			E_FRLMTP frlmtp, BigDecimal unfram) {

		KnbFrozOwne tblKnbfrozowne = KnbFrozOwneDao.selectOneWithLock_odb1(
				frozow, frowid, true);

		if (frlmtp != E_FRLMTP.AMOUNT) {

			long count = DpFrozDao.selFrozCount(frozow, frowid, frlmtp,
					E_FROZST.VALID, false);

			if (count == 0) {
				if (frlmtp == E_FRLMTP.ALL) // 全额冻结
					tblKnbfrozowne.setFralfg(E_YES___.NO);
				else if (frlmtp == E_FRLMTP.IN) // 只付不收
					tblKnbfrozowne.setFrinfg(E_YES___.NO);
				else if (frlmtp == E_FRLMTP.OUT) // 只收不付
					tblKnbfrozowne.setFrotfg(E_YES___.NO);
			}

		} else {
			tblKnbfrozowne.setFrozbl(tblKnbfrozowne.getFrozbl()
					.subtract(unfram));
		}
		KnbFrozOwneDao.updateOne_odb1(tblKnbfrozowne);
	}
	
	// 更改冻结主体信息(只实用于客户止付)
	public static void mntnKnbFrozOwne(E_FROZOW frozow, String frowid,
			E_FRLMTP frlmtp, BigDecimal unfram, String custac) {

		KnbFrozOwne tblKnbfrozowne = DpFrozDao.selKnbFrozOwneInfo(frozow, frowid, true);
		
		// 调用DP模块服务查询冻结状态，检查电子账户状态字 
		IoDpFrozSvcType froz = SysUtil.getInstance(IoDpFrozSvcType.class);
		IoDpAcStatusWord cplGetAcStWord = froz.getAcStatusWord(custac);
		
		//判断该电子账户除了客户止付状态字，是否还有其他只收不付的状态
		if(E_YES___.YES == cplGetAcStWord.getBrfroz()
				|| E_YES___.YES == cplGetAcStWord.getBkalsp()
				|| E_YES___.YES == cplGetAcStWord.getOtalsp()){
			
			tblKnbfrozowne.setFrotfg(E_YES___.YES);
		}else{
			tblKnbfrozowne.setFrotfg(E_YES___.NO);
		}
		
		//更新冻结主体登记簿    
		KnbFrozOwneDao.updateOne_odb1(tblKnbfrozowne);
	}
		
   
	/**
	 * @author douwenbo
	 * @date 2016-05-19 22:05
	 * 解止基本检查
	 * 
	 * @param stunpyin
	 * @param tblKnbFroz
	 * @param acctInfo 
	 * @param stactp 
	 */
	private static void stunPaymentCheck(IoDpUnStopayIn stunpyin,KnbFroz tblKnbFroz, IoDpKnaAcct acctInfo) {

		if(CommUtil.isNull(tblKnbFroz)){
			throw DpModuleError.DpstComm.BNAS0702();
		}
		
		if(tblKnbFroz.getFroztp() == E_FROZTP.BANKSTOPAY){
			if(stunpyin.getStoptp() != E_STOPTP.BANKSTOPAY){
				throw DpModuleError.DpstComm.BNAS0579();
			}
		}
		
		if(tblKnbFroz.getFroztp() == E_FROZTP.CUSTSTOPAY){
			if(stunpyin.getStoptp() != E_STOPTP.CUSTSTOPAY){
				throw DpModuleError.DpstComm.BNAS0579();
			}
		}
		
		if(tblKnbFroz.getFroztp() == E_FROZTP.EXTSTOPAY){
			if(stunpyin.getStoptp() != E_STOPTP.EXTSTOPAY){
				throw DpModuleError.DpstComm.BNAS0579();
			}
		}
		
		//判断当前解止记录是否为客户止付
		if(tblKnbFroz.getFroztp() == E_FROZTP.CUSTSTOPAY){
			//根据电子账号查询电子账号ID
			IoCustacInfo custacInfo = DpFrozDao.selCustacInfoByCardno(stunpyin.getCardno(), true);
			bizlog.debug("++++++++++++"+custacInfo.getCorpno()+"+++++++++++++");
			String custac = custacInfo.getCustac();
			
			 if(JFBaseEnumType.E_STACTP.STMA==stunpyin.getStactp()) {
				//电子账号ID
					
					
					E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
					
					if (cuacst == E_CUACST.CLOSED) {
						throw DpModuleError.DpstComm.BNAS1597();
					
					}else if (cuacst == E_CUACST.PRECLOS) {
						throw DpModuleError.DpstComm.BNAS0846();
					
					}else if (cuacst == E_CUACST.PREOPEN) {
						throw DpModuleError.DpstComm.BNAS1598();
					
					}else if(cuacst == E_CUACST.OUTAGE){
						throw DpModuleError.DpstComm.BNAS0850();
						
					}else if(cuacst == E_CUACST.NOENABLE){
						throw DpModuleError.DpstComm.BNAS0848();
						
					}
		            }else if(JFBaseEnumType.E_STACTP.STSA==stunpyin.getStactp()) {
		            	 if(CommUtil.isNotNull(acctInfo)) {
		            	  E_DPACST cuacst=acctInfo.getAcctst();
		            	  if (cuacst == E_DPACST.CLOSE) {
		                      throw DpModuleError.DpstComm.BNAS1597();

		                  } 
		            	 }
		            }
			if(E_FROZTP.CUSTSTOPAY == tblKnbFroz.getFroztp()){
				//解止法人必须与开户法人一致
				if(!CommUtil.equals(CommTools.getBaseRunEnvs().getBusi_org_id(), custacInfo.getCorpno())){
					throw DpModuleError.DpstComm.BNAS0789();
				}
			}
			 if(JFBaseEnumType.E_STACTP.STSA==stunpyin.getStactp()) {
				 custac=acctInfo.getAcctno();
			 }
			if (!CommUtil.equals(custac, tblKnbFroz.getCustac())) {
				throw DpModuleError.DpstComm.BNAS0911();
			}
			
			// 止付日期和输入的止付日期比较检查
//			if (!CommUtil.equals(stunpyin.getStopdt(), tblKnbFroz.getFrozdt())) {
//				throw DpModuleError.DpstComm.E9999("止付日期与原止付信息中的止付日期不一致。");
//			}
			
			//根据电子账号查询电子账号信息
			IoCustacDetl custacDl = DpFrozDao.selCustacDetl(custac, true);
			
//			if(! stunpyin.getCustna().equals(custacDl.getCustna())){
//				throw DpModuleError.DpstComm.BNAS0525();
//			}
			
			//根据客户号查询客户信息
			IoCustInfo custInfo = DpFrozDao.selCustInfo(custacDl.getCustno(), true);
			
			if(stunpyin.getStoptp() == E_STOPTP.CUSTSTOPAY && (!CommUtil.equals(stunpyin.getIdno01(), custInfo.getIdtfno()))){
				throw DpModuleError.DpstComm.BNAS0537();
			}
			
		//外部止付和银行止付	
		}else {
			IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			
			IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(stunpyin.getCardno(), false);
			
			if(CommUtil.isNull(caKnaAcdc)){
				throw DpModuleError.DpstComm.BNAS0692();
			}
			
			//电子账号ID
			String custac = caKnaAcdc.getCustac();
			
           
			
			
			
			  if(JFBaseEnumType.E_STACTP.STMA==stunpyin.getStactp()) {
		            // 账户状态为预开户、转久悬、预销户、销户的，交易拒绝，报错：“电子账户状态为***状态，无法操作！”。
				  E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		            bizlog.debug("+++++++++++++++++++++++++++++" + "电子账户状态为" + cuacst
		                    + "+++++++++++++++++++++++++");
		            if (cuacst == E_CUACST.CLOSED) {
						throw DpModuleError.DpstComm.BNAS1597();
					
					}else if (cuacst == E_CUACST.PREOPEN) {
						throw DpModuleError.DpstComm.BNAS0849();
					
					}
		        }else if(JFBaseEnumType.E_STACTP.STSA==stunpyin.getStactp()) {
		        	 if(acctInfo!=null) {
		            	  E_DPACST cuacst=acctInfo.getAcctst();
		            	  if (cuacst == E_DPACST.CLOSE) {
		                      throw DpModuleError.DpstComm.BNAS1597();

		                  } 
		        	 }
		            }
//			else if (cuacst == E_CUACST.PRECLOS) {
//				throw DpModuleError.DpstComm.E9999("电子账户状态为预销户状态，无法操作！");
//			
//			}
			
			//判断是否为银行止付和外部止付
			if(E_FROZTP.BANKSTOPAY == tblKnbFroz.getFroztp() || E_FROZTP.EXTSTOPAY == tblKnbFroz.getFroztp()){
				// 解止机构必须是和原止付机构一致
//				if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), tblKnbFroz.getTranbr())) {
//					throw DpModuleError.DpstComm.E9999("解止机构和原止付机构不一致。");
//				}
				IoCustacInfo custacInfo = DpFrozDao.selCustacInfoByCardno(stunpyin.getCardno(), true);
				
				if(!CommUtil.equals(CommTools.getBaseRunEnvs().getBusi_org_id(), custacInfo.getCorpno())){
					throw DpModuleError.DpstComm.BNAS0793();
				}
			}
			
			if(E_FROZTP.EXTSTOPAY == tblKnbFroz.getFroztp()){
				if(CommUtil.compare(stunpyin.getStopms(), tblKnbFroz.getStopms()) != 0){
					throw DpModuleError.DpstComm.BNAS0578();
				}
				
				//新增止付时长，解止/质押到期日期校验
				if(tblKnbFroz.getSttmct() == E_STTMCT.HOUR){
					if(CommUtil.isNull(stunpyin.getSttmle())){
						throw DpModuleError.DpstComm.BNAS0077();
					}
					if(CommUtil.compare(stunpyin.getSttmle(), tblKnbFroz.getSttmle()) != 0){
						throw DpModuleError.DpstComm.BNAS0075();
					}
					if(CommUtil.isNotNull(stunpyin.getStpldt()) && stunpyin.getStuntp()!=E_STUNTP.UNSTPL){
						throw DpModuleError.DpstComm.BNAS0078();
					}
				}
				if(tblKnbFroz.getSttmct() == E_STTMCT.DAY){
					if(CommUtil.isNull(stunpyin.getStpldt())){
						throw DpModuleError.DpstComm.BNAS0079();
					}
					if(CommUtil.compare(stunpyin.getStpldt(), tblKnbFroz.getFreddt()) != 0){
						throw DpModuleError.DpstComm.BNAS0581();
					}
				}
			}
			
			// 解止信息中的客户账号与客户账号匹配性检查
//			custac = CommUtil.nvl(stunpyin.getCustac(), custac);
			 if(JFBaseEnumType.E_STACTP.STSA==stunpyin.getStactp()) {
				 if(acctInfo!=null) {
				 custac=acctInfo.getAcctno();
				 }
			 }
			if (!CommUtil.equals(custac, tblKnbFroz.getCustac())) {
				throw DpModuleError.DpstComm.BNAS0908();
			}
	        // 止付日期和输入的止付日期比较检查
			if (!CommUtil.equals(stunpyin.getStopdt(), tblKnbFroz.getFrozdt())) {
				throw DpModuleError.DpstComm.BNAS0407();
			}
			
			// 原止付是非部止时，则冻结金额不用输入
			if (tblKnbFroz.getFrlmtp() != E_FRLMTP.AMOUNT && CommUtil.compare(stunpyin.getStopam(),BigDecimal.ZERO) != 0) {
				throw DpModuleError.DpstComm.BNAS0212();
			}
			
//			IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			
			
//			if(JFBaseEnumType.E_STACTP.STSA==stunpyin.getStactp()) {
//				 if(acctInfo!=null) {
//				if(! stunpyin.getCustna().equals(acctInfo.getAcctna())){
//					throw DpModuleError.DpstComm.BNAS0525();
//				}
//				 }
//			}else {
//			IoCaKnaCust tblKnaCust = caqry.getKnaCustWithLockByCustacOdb1(custac, true);
//			if(! stunpyin.getCustna().equals(tblKnaCust.getCustna())){
//				throw DpModuleError.DpstComm.BNAS0525();
//			}
//			}
//			IoCuSevQryTableInfo cuqry = SysUtil.getInstance(IoCuSevQryTableInfo.class);
//			IoCucifCust cucifCust = cuqry.cif_cust_selectOne_odb1(tblKnaCust.getCustno(), false);
//			
//			if(stunpyin.getStoptp() == E_STOPTP.CUSTSTOPAY && CommUtil.compare(stunpyin.getIdno01(),  cucifCust.getIdtfno()) >0){
//				throw DpModuleError.DpstComm.E9999("客户解止付时,证件号码必须为本人");
//			}
		}
	}
	
	
	/**
	 * @author douwenbo
	 * @param stactp 
	 * @param acctno 
	 * @param acalno 
	 * @date 2016-05-19 20:53
	 * 解止基本检查
	 * 
	 * @param IoDpUnStopayIn
	 */
	public static void stunpyCheck(IoDpUnStopayIn stunpyin) {
		IoDpKnaAcct acctInfo=SysUtil.getInstance(IoDpKnaAcct.class);    
    
       	 //查询子账户信息
    	//custInfo=DpFrozDao.selCardCustacByAcctno(stunpyin.getAcalno(),false);  
   		if(JFBaseEnumType.E_STACTP.STSA==stunpyin.getStactp()) {
   		  acctInfo=DpFrozDao.selAcctInfoByAcalno(stunpyin.getAcctno(), false);
   		  if(acctInfo!=null) {
   			stunpyin.setAcctno(acctInfo.getAcctno());    
   		  }
   		}
    	
		if (CommUtil.isNull(stunpyin.getSptype()) && CommUtil.isNull(stunpyin.getCustop())) {
			throw DpModuleError.DpstComm.BNAS0091();
		}
		
		if (CommUtil.isNull(stunpyin.getCardno())) {
			throw DpModuleError.DpstProd.BNAS0926();
		}
		
		if (CommUtil.isNull(stunpyin.getCustna())) {
			throw DpModuleError.DpstComm.BNAS0533();
		}
		
		//转换，如果传进止付类型为1-银行止付转为 3-银行止付
		if(stunpyin.getSptype()==E_SPTYPE.BANKSTOPAY){
			stunpyin.setStoptp(E_STOPTP.BANKSTOPAY);
		}
		
		//转换，如果传进止付类型为2-外部止付转为 4-外部止付
		if(stunpyin.getSptype() == E_SPTYPE.EXTSTOPAY){
			stunpyin.setStoptp(E_STOPTP.EXTSTOPAY);
		}
		
		if(CommUtil.isNotNull(stunpyin.getCustop())){
			
			if(E_CUSTOP.ACUNSTOP != stunpyin.getCustop()){
				
				throw DpModuleError.DpstComm.BNAS0724();
			}else{
				
				stunpyin.setStoptp(E_STOPTP.CUSTSTOPAY);
			}
		}
		
		//外部止付和银行止付字段笔数校验
		if(E_STOPTP.BANKSTOPAY == stunpyin.getStoptp() || E_STOPTP.EXTSTOPAY == stunpyin.getStoptp()){
			
			if (CommUtil.isNull(stunpyin.getStuntp())) {
				throw DpModuleError.DpstComm.BNAS0069();
			}
			
			if(!(stunpyin.getStuntp() == E_STUNTP.UNSTPL)){
				throw DpModuleError.DpstComm.BNAS0070();
			}
			
			// 获取原止付登记簿信息
			KnbFroz tblKnbFroz = KnbFrozDao.selectOneWithLock_odb1(stunpyin.getStopno(), E_FROZST.VALID, false);
			if(CommUtil.isNull(tblKnbFroz)){
				throw DpModuleError.DpAcct.AT020037();
			}
			
			if(!CommUtil.equals(tblKnbFroz.getFroztp().toString(), stunpyin.getStoptp().toString())){
				throw DpModuleError.DpstComm.BNAS0072();
			}
			
			if(stunpyin.getStoptp() == E_STOPTP.EXTSTOPAY){
				if(CommUtil.isNull(stunpyin.getStopms())){
					throw DpModuleError.DpstComm.BNAS0089();
				}
			}
			
			if(CommUtil.isNull(stunpyin.getCrcycd())){
				throw DpModuleError.DpstComm.BNAS1101();
			}
			
			if (CommUtil.isNull(stunpyin.getStopno())) {
				throw DpModuleError.DpstComm.BNAS0074();
			}
			
			
						
			if(E_FROZTP.DEPRSTOPAY == tblKnbFroz.getFroztp()){
				throw DpModuleError.DpstComm.BNAS0979();
			}
			
			if(stunpyin.getStoptp() == E_STOPTP.CUSTSTOPAY || stunpyin.getStoptp() == E_STOPTP.EXTSTOPAY){
				if(!CommUtil.equals(stunpyin.getStopam(), BigDecimal.ZERO)){
					throw DpModuleError.DpstComm.BNAS0180();
				}
			}
			
			if(CommUtil.equals(stunpyin.getCrcycd(), BusiTools.getDefineCurrency()) && CommUtil.isNotNull(stunpyin.getCsextg())){
				throw DpModuleError.DpstComm.BNAS1099();
			}
			
			if (CommUtil.isNull(stunpyin.getStbkno()) && stunpyin.getStoptp() == E_STOPTP.BANKSTOPAY) {
				throw DpModuleError.DpstComm.BNAS1627();
			}
			
			if(stunpyin.getStoptp() == E_STOPTP.EXTSTOPAY && CommUtil.isNull(stunpyin.getStdptp())){
				throw DpModuleError.DpstComm.BNAS0976();
			}
			
			if(CommUtil.isNull(stunpyin.getFrna01())){
				throw DpModuleError.DpstComm.BNAS0103();
			}
			
			if(CommUtil.isNull(stunpyin.getIdtp01())){
				throw DpModuleError.DpstComm.BNAS0101();
			}
			
			if(CommUtil.isNull(stunpyin.getIdno01())){
				throw DpModuleError.DpAcct.AT020036();
			}
			
			if(CommUtil.isNull(stunpyin.getSfreas())){
				throw DpModuleError.DpstComm.BNAS0590();
			}
			
			//校验证件类型、证件号码
//			if(stunpyin.getIdtp01() == E_IDTFTP.SFZ){
//				cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp = cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP.SFZ;
//				BusiTools.chkCertnoInfo(idtftp, stunpyin.getIdno01());
//			}
			
			if(stunpyin.getStoptp() == E_STOPTP.EXTSTOPAY){
				if(CommUtil.isNull(stunpyin.getFrna02())){
					throw DpModuleError.DpstComm.BNAS0975();
				}
				if(CommUtil.isNull(stunpyin.getIdtp02())){
					throw DpModuleError.DpstComm.BNAS1628();
				}
				if(CommUtil.isNull(stunpyin.getIdno02())){
					throw DpModuleError.DpstComm.BNAS0974();
				}
			}
			
			if((stunpyin.getStoptp() == E_STOPTP.BANKSTOPAY || stunpyin.getStoptp() == E_STOPTP.EXTSTOPAY)
					&& CommUtil.isNull(stunpyin.getStladp())){
				throw DpModuleError.DpstComm.BNAS1629();
			}
//			bizlog.debug("智能储蓄交易（导出数据）----" + stunpyin.getStopno().length()+"" + "---文件产生完成");
//			bizlog.debug("智能储蓄交易（导出数924716据）----" + stunpyin.getStopno().substring(0, 2) + "---文件产生完成");
			
			if(!CommUtil.equals(stunpyin.getStopno().length()+"","16") || !CommUtil.equals(stunpyin.getStopno().substring(0, 2),"22"))
			{
				throw DpModuleError.DpstComm.BNAS0073();
			}
		}
		
		//客户止付字段必输校验
		if(E_STOPTP.CUSTSTOPAY == stunpyin.getStoptp()){
			

			if(CommUtil.isNull(stunpyin.getIdtp01())){
				throw DpModuleError.DpstComm.BNAS0150();

			}
			
			if(CommUtil.isNull(stunpyin.getIdno01())){
				throw DpModuleError.DpstComm.BNAS0157();
			}
			
			if(CommUtil.isNull(stunpyin.getCureas())){
				throw DpModuleError.DpstComm.BNAS0213();
			}
		}
	}
	
	/**
	 * 解止处理 先解止明明细登记簿再解止冻结登记簿 
	 * @param stactp 
	 * @param acctno 
	 * @param acalno 
	 * 
	 * @param cpliodpunfrozin
	 */
	public static void stunPayment(IoDpUnStopayIn stunpyin) {
		IoDpKnaAcct acctInfo=SysUtil.getInstance(IoDpKnaAcct.class);    

   		if(JFBaseEnumType.E_STACTP.STSA==stunpyin.getStactp()) {
   		  acctInfo=DpFrozDao.selAcctInfoByAcalno(stunpyin.getAcctno(), false);
   		  if(acctInfo!=null) {
   			stunpyin.setAcctno(acctInfo.getAcctno());    
   		  }
   		}
		//根据电子账号查询电子账号ID
		IoCustacInfo custacInfo = DpFrozDao.selCustacInfoByCardno(stunpyin.getCardno(), false);
		if(CommUtil.isNull(custacInfo)){
			throw CaError.Eacct.BNAS0750();
		}
		
		if(E_STOPTP.CUSTSTOPAY == stunpyin.getStoptp()){
			
			if(!CommUtil.equals(custacInfo.getCorpno(), CommTools.getBaseRunEnvs().getBusi_org_id())){
				throw DpModuleError.DpstComm.BNAS0790();
			}
			
			KnbFroz tbl_KnbFroz = DpFrozDao.selStopInfoByCustac(custacInfo.getCustac(), false);
			
			if(CommUtil.isNull(tbl_KnbFroz)){
				throw DpModuleError.DpstComm.BNAS0746();
			}
			stunpyin.setStopno(tbl_KnbFroz.getFrozno());
		}
		
		//当前解止记录是否为客户止付解止
		KnbFroz tblKnbFroz = DpFrozDao.selCustStopInfo(stunpyin.getStopno(), false);
		
		if(CommUtil.isNotNull(tblKnbFroz)){
			//若为客户解止，设置开户行法人为当前法人
//			CommTools.getBaseRunEnvs().setBusi_org_id(tblKnbFroz.getCorpno());
		}
		
		if(CommUtil.isNull(tblKnbFroz)){
			// 获取原止付登记簿信息
			tblKnbFroz = KnbFrozDao.selectOneWithLock_odb1(stunpyin.getStopno(), E_FROZST.VALID, false);
			if(CommUtil.isNull(tblKnbFroz)){
				throw DpModuleError.DpAcct.AT020037();
			}
			
			if(E_FROZTP.DEPRSTOPAY == tblKnbFroz.getFroztp()){
				throw DpModuleError.DpstComm.BNAS0979();
			}
			
		}
		
		// 解止基本检查
		stunPaymentCheck(stunpyin, tblKnbFroz,acctInfo);
		if(JFBaseEnumType.E_STACTP.STSA==stunpyin.getStactp()) {
			String acct=stunpyin.getAcctno();
			tblKnbFroz.setCustac(acct);
		}
		// 解止处理
		unStopPayDo(stunpyin, tblKnbFroz);
		stunpyin.setAcctno(tblKnbFroz.getCustac());
		
	}
}

