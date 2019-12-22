package cn.sunline.ltts.busi.dptran.trans.online;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;


public class opsbac {

	public static void prcOpsbacOut( final cn.sunline.ltts.busi.dptran.trans.online.intf.Opsbac.Input Input,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opsbac.Property Property,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opsbac.Output Output){
		Output.setAcctno(Property.getAcctno());
		Output.setPddpfg(Property.getPddpfg());
	}

	public static void chkOpsbacInfo( final cn.sunline.ltts.busi.dptran.trans.online.intf.Opsbac.Input Input,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opsbac.Property Property,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opsbac.Output Output){
		String custac = Input.getCustac();
		String custno = Input.getCustno();
		String acctna = Input.getAcctna();
		String prodcd = Input.getProdcd();
		String crcycd = Input.getCrcycd();
		E_TERMCD depttm =Input.getDepttm();
		
		if(CommUtil.isNull(custno)){
			throw DpModuleError.DpstComm.BNAS0538();
		}
		if(CommUtil.isNull(acctna)){
			throw DpModuleError.DpstComm.BNAS0534();
		}
		if(CommUtil.isNull(custac)){
			throw DpModuleError.DpstComm.BNAS0541();
		}else{
			//kna_cust tblKna_cust = Kna_custDao.selectOne_odb1(custac, false);
			IoCaKnaCust tblKna_cust = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(custac, false);
			if(CommUtil.isNull(tblKna_cust)){
				throw DpModuleError.DpstComm.BNAS0754();
			}
			
		}
		//产品编号
		if(CommUtil.isNull(crcycd)){
			throw DpModuleError.DpstComm.BNAS1101();
		}
		
		//货币代号
		if(CommUtil.isNull(prodcd)){
			throw DpModuleError.DpstComm.BNAS0665();
		}
		
		//货币代号
		if(CommUtil.isNull(depttm)){
			throw DpModuleError.DpstProd.BNAS1025();
		}
		
	}
}
