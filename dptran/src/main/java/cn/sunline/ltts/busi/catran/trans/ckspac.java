package cn.sunline.ltts.busi.catran.trans;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccsDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInterestAndPrincipal;
import cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;
import cn.sunline.ltts.busi.sys.type.LnEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.PbEnumType;

public class ckspac {
	/**
	 * 
	 * @author xiongzhao
	 *         <p>
	 *         <li>2016年6月29日-下午8:10:56</li>
	 *         <li>功能描述：电子账户停用前检查</li>
	 *         </p>
	 * 
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void prcCheckOutage(
			final cn.sunline.ltts.busi.catran.trans.intf.Ckspac.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Ckspac.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Ckspac.Output output) {
		
		String sCardno = input.getCardno();//电子账号
		String sDclrna = input.getDclrna();//声明人姓名
		String sCustac = "";//电子账号ID
		String sCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();//法人代码
		
		//检查输入接口必输项是否为空
		if (CommUtil.isNull(sCardno)) {
			throw DpModuleError.DpstComm.BNAS0955();
		}
		
		//根据电子账号查询出电子账号ID
		KnaAcdc tblKnaAcdc= CaDao.selKnaAcdcByCardno(sCardno, E_DPACST.NORMAL, false);
		if (CommUtil.isNull(tblKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0754();
		}
		sCustac = tblKnaAcdc.getCustac();
		property.setCustac(sCustac);
		
		//根据电子账号查询电子账户表信息
		KnaCust tblKnaCust = CaDao.selKnaCustByCustac(sCustac, false);//根据电子账号ID查询电子账户表信息
		if (CommUtil.isNull(tblKnaCust)) {
			throw DpModuleError.DpstComm.BNAS0754();
		}
		
		//操作柜员只能办理本行社的电子账户停用
		if (!CommUtil.equals(sCorpno, tblKnaCust.getCorpno())) {
			throw CaError.Eacct.BNAS0795();
		}
		
		//检查电子账户状态
		E_CUACST eCustac = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(sCustac);
		if (eCustac == E_CUACST.PREOPEN) {
			throw DpModuleError.DpstComm.BNAS0881();
		} else if (eCustac == E_CUACST.OUTAGE) {
			throw CaError.Eacct.BNAS0858();
		} /*else if (eCustac == E_CUACST.PRECLOS) {
			throw CaError.Eacct.E0001("电子账户为预销户!");
		} */else if (eCustac == E_CUACST.CLOSED) {
			throw CaError.Eacct.BNAS0857();
		} 
		
/*		// 调用DP模块服务查询冻结状态，检查电子账户状态字 
		IoDpFrozSvcType froz = SysUtil.getInstance(IoDpFrozSvcType.class);
		IoDpAcStatusWord cplGetAcStWord = froz.getAcStatusWord(sCustac);
		if (cplGetAcStWord.getPtfroz() == E_YES___.YES) {
			throw CaError.Eacct.E0001("电子账户已部冻！");
		}
		if (cplGetAcStWord.getCertdp() == E_YES___.YES) {
			throw CaError.Eacct.E0001("电子账户已开立存款证明！");
		}
		if (cplGetAcStWord.getBrfroz() == E_YES___.YES) {
			throw CaError.Eacct.E0001("电子账户已借冻！");
		}
		if (cplGetAcStWord.getDbfroz() == E_YES___.YES) {
			throw CaError.Eacct.E0001("电子账户已双冻！");
		}
		if (cplGetAcStWord.getBkalsp() == E_YES___.YES) {
			throw CaError.Eacct.E0001("电子账户已全止-银行止付！");
		}
		if (cplGetAcStWord.getOtalsp() == E_YES___.YES) {
			throw CaError.Eacct.E0001("电子账户已全止-外部止付");
		}
		if (cplGetAcStWord.getPtstop() == E_YES___.YES) {
			throw CaError.Eacct.E0001("电子账户已部止！");
		}
		
		//新增 是否有未赎回的理财产品判断
		Options<IoFnFnaAcct> fnlist = SysUtil.getInstance(IoFnSevQryTableInfo.class).fna_acct_selectAll_odb2(sCustac);
		for(IoFnFnaAcct fnacct : fnlist){
			if(fnacct.getDpacst() == FnEnumType.E_DPACST.NORMAL){
				throw FnError.FinaComm.E9999("有未赎回的理财产品");
			}
		}
		
		// 查询冻结登记簿,检查是否有理财冻结
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
		if (CommUtil.compare(hdbkam, BigDecimal.ZERO) > 0) {
			throw FnError.FinaComm.E9999("存在未解除的产品协议");
		}
		*/	
		
		//检查声明人与户名是否一致
		if (CommUtil.isNotNull(sDclrna)) {
			if (!CommUtil.equals(sDclrna, tblKnaCust.getCustna())) {
				throw CaError.Eacct.BNAS0360();
			}
		}

		//检查是否有欠款未清偿 TODO
		
		//查询电子账户分类
		E_ACCATP eAccatp = SysUtil.getInstance(
				IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(sCustac);
		
		//将字段set到输出接口
		output.setAccttp(eAccatp);
		output.setBrchno(tblKnaCust.getBrchno());
		output.setCustna(tblKnaCust.getCustna());
		output.setDebtfg(E_YES___.NO);
	}

	public static void chkNotChrgFee( final cn.sunline.ltts.busi.catran.trans.intf.Ckspac.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Ckspac.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Ckspac.Output output){
		PbEnumType.E_YES___ isfee = SysUtil.getInstance(IoCgChrgSvcType.class).CgChageRgstNotChargeByCustac(property.getCustac());
		if(isfee == PbEnumType.E_YES___.YES){
			output.setDebtfg(E_YES___.YES);
		}
	}

	public static void checkLoan( final cn.sunline.ltts.busi.catran.trans.intf.Ckspac.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Ckspac.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Ckspac.Output output){
		/*
         * add by wenbo 20170918
         * 1.检查是否存在未结清的贷款
         */
        String cardno = input.getCardno();
        String custac = CaTools.getCustacByCardno(cardno);
        BigDecimal intest = BigDecimal.ZERO;//利息
        BigDecimal principal = BigDecimal.ZERO;//本金
        List<KnaAccs> listKnaAccs = KnaAccsDao.selectAll_odb5(custac, false); 
        for (KnaAccs tblknaAccs : listKnaAccs) {
            if(tblknaAccs.getAcctst()==E_DPACST.NORMAL&&E_PRODTP.LOAN == tblknaAccs.getProdtp()){
                //贷款状态为正常
                DpInterestAndPrincipal dpInterestAndPrincipal = DpAcctQryDao.selPrincipalAndInterestByAcctno(tblknaAccs.getAcctno(), E_ACCTST.NORMAL, false);
                if (CommUtil.isNull(dpInterestAndPrincipal)) {
					//不做处理
				}else {
					intest = intest.add(dpInterestAndPrincipal.getIntest());// 利息赋值值
	                principal = principal.add(dpInterestAndPrincipal.getPrincipal());// 本金赋值
				}
            }
        }
        if(CommUtil.compare(intest.add(principal), BigDecimal.ZERO)>0){
        	output.setDebtfg(E_YES___.YES);
        }
	}
}
