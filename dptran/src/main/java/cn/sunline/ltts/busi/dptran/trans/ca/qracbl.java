package cn.sunline.ltts.busi.dptran.trans.ca;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.busi.dp.errors.DpAcError;
import cn.sunline.edsp.busi.dp.errors.DpCaError;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.sys.dict.DpDict;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;


public class qracbl {

	public static void qracblToOpdppr( final cn.sunline.ltts.busi.dptran.trans.ca.intf.Qracbl.Input input,  final cn.sunline.ltts.busi.dptran.trans.ca.intf.Qracbl.Property property,  final cn.sunline.ltts.busi.dptran.trans.ca.intf.Qracbl.Output output){
		if (CommUtil.isNull(input.getCardno())) {
			throw DpAcError.DpDeptComm.E9027(DpDict.Acct.cardno.getLongName());
		}
		IoCaKnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCard(input.getCardno(), false);
		if (CommUtil.isNull(tblKnaAcdc)) {
			throw DpCaError.DpEacct.E0021();
		}
		
		IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(tblKnaAcdc.getCustac());
		
		// 总资产可支取余额
        BigDecimal acctbl = SysUtil.getInstance(DpAcctSvcType.class)
                .getDrawnBalance(tblKnaAcdc.getCustac(),cplKnaAcct.getCrcycd(), E_YES___.NO);
        //查询账户身份信息
//        IoCuCustSvcType IoCuCustSvcType = SysUtil.getInstance(IoCuCustSvcType.class);
//        CustInfo tblCifCust = IoCuCustSvcType.selByCustNo(cplKnaAcct.getCustno());
        output.setAcctbl(acctbl); // 总资产可支取余额
        output.setCrcycd(cplKnaAcct.getCrcycd());//账户币种
        output.setCsextg(cplKnaAcct.getCsextg());//钞汇标识
//        output.setCustna(tblCifCust.getCustna());//客户名称
//        output.setIdtfno(tblCifCust.getIdtfno());//证件号码
//        output.setIdtftp(tblCifCust.getIdtftp());//证件类型
        
        //poc增加审计日志
        KnaAcdc kacdc=KnaAcdcDao.selectFirst_odb1(tblKnaAcdc.getCustac(), E_DPACST.NORMAL, false);
        if(CommUtil.isNotNull(kacdc)){
        	ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
    		apAudit.regLogOnInsertBusiPoc(kacdc.getCardno());
        }

	}
}
