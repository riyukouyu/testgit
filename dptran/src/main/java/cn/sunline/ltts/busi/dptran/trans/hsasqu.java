package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;


public class hsasqu {

public static void qryTranInfo( final cn.sunline.ltts.busi.dptran.trans.intf.Hsasqu.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Hsasqu.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Hsasqu.Output output){

		String cardno = input.getCardno();
		if(CommUtil.isNull(cardno)){
			throw DpModuleError.DpstComm.BNAS0955();
		}
		IoCaKnaAcdc tblacdc = SysUtil.getInstance(IoCaSevQryTableInfo.class)
				.getKnaAcdcOdb2(cardno, false);
		if(CommUtil.isNull(tblacdc)){
			throw DpModuleError.DpstComm.BNAS0444();
		}
		if(E_DPACST.CLOSE == tblacdc.getStatus()){
			throw DpModuleError.DpstComm.BNAS0443();
		}
	    //电子账户状态字检查
		String custac = tblacdc.getCustac();
		IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
		if(E_YES___.YES == cplAcStatus.getClstop()){
			throw DpModuleError.DpstComm.BNAS0440();
		}
		//电子账户状态检查
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		if (CommUtil.isNotNull(cuacst)) {
			
			if (cuacst == E_CUACST.OUTAGE) { // 停用
				throw DpModuleError.DpstComm.BNAS0895();
			}
		} else {
			throw DpModuleError.DpstComm.BNAS1206();
		}
	
	//分页查询页码、页容量设置
	int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());//页码
	int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());//页容量
	
	property.setPageno(pageno);//页码
	property.setPagesize(pgsize);//页容量

	}
}
