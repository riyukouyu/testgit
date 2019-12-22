package cn.sunline.ltts.busi.ca.serviceimpl;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccsDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInterestAndPrincipal;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaCloseSvc.IoCaChkStatus.Input;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaCloseSvc.IoCaChkStatus.Output;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;
import cn.sunline.ltts.busi.sys.type.LnEnumType.E_ACCTST;
 /**
  * 电子账户销户服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoCaCloseSvcImpl", longname="电子账户销户服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoCaCloseSvcImpl implements cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaCloseSvc{
 /**
  * 柜面销户前检查贷款
  *
  */
	public void chkLoan( final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaCloseSvc.IoCaChkLoan.Input input){
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
            throw DpModuleError.DpstComm
            .E9999("电子账号" + cardno + "下存在未结清的贷款");
        }
	}
 /**
  * 柜面销户前检查理财
  *
  */
	public Boolean chkFina(String custac){
//		BigDecimal hdbkam = BigDecimal.ZERO;
		boolean isable = true;
//		Options<IoFnFnaAcct> fnlist = SysUtil.getInstance(IoFnSevQryTableInfo.class).fna_acct_selectAll_odb2(custac);
//		for(IoFnFnaAcct fnacct : fnlist){
//			if(fnacct.getAcctst() == BaseEnumType.E_DPACST.NORMAL){
//				throw DpModuleError.DpstComm.BNAS0231();
//			}
//		}
//		// 查询冻结登记簿
//		Options<IoDpKnbFroz> lstKnbFroz = SysUtil.getInstance(
//				IoDpFrozSvcType.class).qryKnbFroz(custac, E_FROZST.VALID);
//		for (IoDpKnbFroz knbfroz : lstKnbFroz) {
//			if (knbfroz.getFroztp() == E_FROZTP.FNFROZ) {
//				if (knbfroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
//					hdbkam = hdbkam.add(knbfroz.getFrozam());// 保留金额
//				}
//			}
//		}
//		if (CommUtil.compare(hdbkam,BigDecimal.ZERO) > 0) {
//			throw DpModuleError.DpstComm.BNAS0997();
//		}
		return isable;
	}
 /**
  * 柜面销户前其他透支款项、贵金属等检查
  *
  */
	public void chkOther(){
		
	}
	
	@Override
	public void chkStatus(Input input, Output output) {
		String custac = input.getCustac();
		
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		
		if(cuacst == E_CUACST.PREOPEN){
			throw DpModuleError.DpstComm.BNAS0163();
		}
		if(cuacst == E_CUACST.OUTAGE){
			throw DpModuleError.DpstComm.BNAS0165();
		}
		if(cuacst == E_CUACST.CLOSED){
			throw DpModuleError.DpstComm.BNAS0164();
		}
//		if(cuacst == E_CUACST.NOENABLE){
//			throw DpModuleError.DpstComm.E9999("账户状态为未启用!");
//		}
		//查询电子账户冻结状态 查询冻结主体登记簿
		IoDpAcStatusWord froz = SysUtil.getInstance(IoDpFrozSvcType.class)
				.getAcStatusWord(custac);
		if (froz.getBrfroz() == E_YES___.YES) {
			throw CaError.Eacct.BNAS0866();
		}
		if (froz.getDbfroz() == E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0859();
		}
		if (froz.getPtfroz() == E_YES___.YES) {
			throw CaError.Eacct.BNAS0869();
		}
		if (froz.getBkalsp() == E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0860();
		}
		if (froz.getOtalsp() == E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0862();
		}
		if (froz.getPtstop() == BaseEnumType.E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0867();
		}
		if (froz.getClstop() == E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0856();
		}
		if (froz.getCertdp() == E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0865();
		}
		if (froz.getPledge() == E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0855();
		}

		E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac);
		
		output.setAccatp(accatp);
		
	}
}

