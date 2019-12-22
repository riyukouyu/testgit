package cn.sunline.ltts.busi.intran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.errors.PbError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CMBKTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CNTSYS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class iacuck {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(iacuck.class);
	
	public static void befroeCheck( final cn.sunline.ltts.busi.intran.trans.intf.Iacuck.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Iacuck.Property property){
		
		bizlog.debug("==========内部账转客户账复核检查开始==========");
		
		final cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Input iacutrInput = SysUtil.getInstance(cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Input.class);
		
		CommUtil.copyProperties(iacutrInput, input, false);
		
		E_CNTSYS cntsys = iacutr.custInputCheck(iacutrInput, E_CMBKTP.CHK);
		
		if(CommUtil.isNull(input.getCntsys())){
			throw InError.comm.E0003("获取跨系统转账标志失败！");
		}
		
		bizlog.debug("==========内部账转客户账复核检查结束==========");
	}

	public static void custIrregular( final cn.sunline.ltts.busi.intran.trans.intf.Iacuck.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Iacuck.Property property){
		
		bizlog.debug("客户账号状态检查开始==========");		
		// 调入电子账号信息
		IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb2(input.getInacno(), false);
		if(CommUtil.isNull(tblKnaAcdc)){
			throw InError.comm.E0003("该转入账号["+input.getInacno()+"]不存在！");
		}
		E_CUACST status = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(tblKnaAcdc.getCustac());//查询电子账户状态信息
		if(status==E_CUACST.PREOPEN||status==E_CUACST.CLOSED||status==E_CUACST.DELETE||status==E_CUACST.PRECLOS){
			throw PbError.PbComm.E2015("该转入账号["+input.getInacno()+"]状态为["+status.getLongName()+"]，不允许交易！");
		}
		//状态字校验
	  //SysUtil.getInstance(IoAccountSvcType.class).checkStatusBeforeAccount(tblKnaAcdc.getCustac(), E_AMNTCD.C,cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.NO);
		IoDpAcStatusWord cplGetAcStWord = SysUtil.getInstance(
				IoDpFrozSvcType.class).getAcStatusWord(tblKnaAcdc.getCustac());
		if (cplGetAcStWord.getDbfroz() == E_YES___.YES){
			throw DpModuleError.DpstAcct.E9999("交易失败，已被冻结！");		
		}
		IoCaKnaCust tblKnaCust = SysUtil.getInstance(
				IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(
				tblKnaAcdc.getCustac(), true);
		
		bizlog.debug("注册户名为：" +tblKnaCust.getCustna()  + ",输入户名为：" + input.getInacna());
		
		if(!CommUtil.equals(tblKnaCust.getCustna(), input.getInacna())){
			throw InError.comm.E0003("输入户名与客户账号注册户名不匹配");
		}
		bizlog.debug("客户账号状态检查结束==========");
		
	}

}
