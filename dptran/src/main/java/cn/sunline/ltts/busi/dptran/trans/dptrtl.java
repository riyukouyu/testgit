package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;


public class dptrtl {

public static void qryTranInfo( final cn.sunline.ltts.busi.dptran.trans.intf.Dptrtl.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Dptrtl.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Dptrtl.Output output){
		
	//电子账户状态字检查
	String custac = input.getCustac();
	//add by zdj 20181019
	if(CommUtil.isNull(custac)){
        if(CommUtil.isNull(input.getCardno())){
            throw DpModuleError.DpstAcct.E9999("电子账号不能为空");
        }else{
            custac = KnaAcdcDao.selectOne_odb2(input.getCardno(), false).getCustac();
        }
    }
	//add end
	IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
	if(E_YES___.YES == cplAcStatus.getClstop()){
		throw DpModuleError.DpstComm.BNAS0898();
	}
	//电子账户状态检查
	E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
	if (CommUtil.isNotNull(cuacst)) {
		
		if (cuacst == E_CUACST.CLOSED) { // 销户
			throw DpModuleError.DpstComm.BNAS0883();

		} else if (cuacst == E_CUACST.OUTAGE) { // 停用
			throw DpModuleError.DpstComm.BNAS0886();

		} else if (cuacst == E_CUACST.PREOPEN) { // 预开户
			throw DpModuleError.DpstComm.BNAS0881();
		}
	} else {
		throw DpModuleError.DpstComm.BNAS1206();
	}
	
	//分页查询页码、页容量设置
		int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());//页码
		int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());//页容量
		
		property.setPageno(pageno);//页码
		property.setPagesz(pgsize);//页容量
		
		//poc
        KnaAcdc kacdc=KnaAcdcDao.selectFirst_odb1(custac, E_DPACST.NORMAL, false);
        if(CommUtil.isNotNull(kacdc)){
        	ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
    		apAudit.regLogOnInsertBusiPoc(kacdc.getCardno());
        }
}
}
