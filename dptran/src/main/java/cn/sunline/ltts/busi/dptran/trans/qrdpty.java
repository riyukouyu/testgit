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
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;


public class qrdpty {

	public static void DealTranBefore( final cn.sunline.ltts.busi.dptran.trans.intf.Qrdpty.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrdpty.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrdpty.Output output){
		
		String cardno = input.getCardno();
		if(CommUtil.isNull(cardno)){
			throw DpModuleError.DpstProd.BNAS0926();
		}
		IoCaKnaAcdc tblacdc = SysUtil.getInstance(IoCaSevQryTableInfo.class)
				.getKnaAcdcOdb2(cardno, true);
		//检查电子账户状态
		if(tblacdc.getStatus() ==  E_DPACST.CLOSE){
			throw DpModuleError.DpstComm.BNAS0428();
		}
		
		//查询电子账户分类
		E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
				.qryAccatpByCustac(tblacdc.getCustac());
		if(eAccatp == E_ACCATP.WALLET){
			throw DpModuleError.DpstAcct.BNAS0813();
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
			
			if (cuacst == E_CUACST.CLOSED) { // 销户
				throw DpModuleError.DpstComm.BNAS0894();

			} else if (cuacst == E_CUACST.OUTAGE) { // 停用
				throw DpModuleError.DpstComm.BNAS0895();

			} else if (cuacst == E_CUACST.PREOPEN) { // 预开户
				throw DpModuleError.DpstComm.BNAS0893();
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
