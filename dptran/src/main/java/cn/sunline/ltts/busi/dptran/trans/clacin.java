package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpClsChkIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpClsChkOT;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;


public class clacin {

	public static void proCalAcin( final cn.sunline.ltts.busi.dptran.trans.intf.Clacin.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Clacin.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Clacin.Output output){
		//判空处理
		if(CommUtil.isNull(input.getCustac())){

			throw DpModuleError.DpstProd.BNAS0935(); 

		}
		if(CommUtil.isNull(input.getAcctno())){
			throw DpModuleError.DpstAcct.BNAS1027(); 
		}
		if(CommUtil.isNull(input.getIsdraw())){
			throw DpModuleError.DpstAcct.BNAS0343(); 
		}
		if(CommUtil.isNull(input.getTranam())){
			throw DpModuleError.DpstComm.BNAS0125(); 
		}
//电子账号ID不对外暴露，通过对外暴露的电子账号（卡号）获取电子账号ID
		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(input.getCustac(), false);
		if (CommUtil.isNull(tblKnaAcdc)) {
			throw CaError.Eacct.E0001("请输入正确的电子账号!");
		}
		String custac = tblKnaAcdc.getCustac(); //电子账号ID
		//检查输入的电子账号ID是否和输入的存款子账号对应的电子账号iD匹配
		KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(input.getAcctno(), false);
		if(CommUtil.isNull(tblKnaFxac)){
			throw DpModuleError.DpstAcct.BNAS1207(input.getAcctno()); 
		}else{

		   if(!CommUtil.equals(custac, tblKnaFxac.getCustac())){
			   throw DpModuleError.DpstAcct.BNAS0903(); 

		    }
		}
		//获取试算利息复合类型并赋值	
		IoDpClsChkIN chkIN = SysUtil.getInstance(IoDpClsChkIN.class);
		chkIN.setCustac(custac);
		chkIN.setAcctno(input.getAcctno());
		chkIN.setIsdraw(input.getIsdraw());
		chkIN.setTranam(input.getTranam());
		
		DpAcctSvcType dpAcctSvcType = SysUtil.getInstance(DpAcctSvcType.class);

		IoDpClsChkOT chkOT = dpAcctSvcType.TestInterest(chkIN);
		output.setCalmon(chkOT.getIntrvl());//存款试算利息
		
		//poc增加审计日志
		 KnaAcdc kacdc=KnaAcdcDao.selectFirst_odb1(custac, E_DPACST.NORMAL, false);
	        if(CommUtil.isNotNull(kacdc)){
	        	ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
	    		apAudit.regLogOnInsertBusiPoc(kacdc.getCardno());
	        }
	}
}
