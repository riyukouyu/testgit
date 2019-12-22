package cn.sunline.ltts.busi.intran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
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


public class mncutr {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(mncutr.class);
	
	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-13 09：18</li>
	 *         <li>内部账转客户账维护</li>
	 *         </p>
	 * @param input
	 *           内部账转客户账录入信息
	 * */
	 public static void befroeCheck( final cn.sunline.ltts.busi.intran.trans.intf.Mncutr.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Mncutr.Property property){
		
		bizlog.debug("==========内部账转客户账维护检查开始==========");
		
		final cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Input iacutrInput = SysUtil.getInstance(cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Input.class);
		CommUtil.copyProperties(iacutrInput, input, false);
		
		//内部账转客户账输入检查
		E_CNTSYS cntsys = iacutr.custInputCheck(iacutrInput, E_CMBKTP.UPD);
		
		if(CommUtil.isNull(input.getCntsys())){
			throw InError.comm.E0003("获取跨系统转账标志失败！");
		}
		
	    bizlog.debug("==========内部账转客户账维护检查结束==========");
		
	}

	//客户账号不存在、已销户、停用、双冻时，或户名与输入户名不相同时报错
	public static void custIrregular( final cn.sunline.ltts.busi.intran.trans.intf.Mncutr.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Mncutr.Property property){
		
		bizlog.debug("客户账号状态检查开始==========");
		
		// 调入电子账号信息
		IoCaKnaAcdc tblKnaAcdc = CommTools.getRemoteInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb2(input.getInacno(), false);
		if(CommUtil.isNull(tblKnaAcdc)){
			throw InError.comm.E0003("该转入账号["+input.getInacno()+"]不存在！");
		}
		E_CUACST status = CommTools.getRemoteInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(tblKnaAcdc.getCustac());//查询电子账户状态信息
		if(status==E_CUACST.PREOPEN||status==E_CUACST.CLOSED||status==E_CUACST.DELETE||status==E_CUACST.PRECLOS){
			throw PbError.PbComm.E2015("该转入账号["+input.getInacno()+"]状态为["+status.getLongName()+"]，不允许交易！");
		}
		IoDpAcStatusWord cplGetAcStWord = CommTools.getRemoteInstance(
				IoDpFrozSvcType.class).getAcStatusWord(tblKnaAcdc.getCustac());
		if (cplGetAcStWord.getDbfroz() == E_YES___.YES){
			throw DpModuleError.DpstAcct.E9999("交易失败，已被冻结！");		
		}
		IoCaKnaCust tblKnaCust = CommTools.getRemoteInstance(
				IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(
				tblKnaAcdc.getCustac(), true);
		bizlog.debug("注册户名为：" +tblKnaCust.getCustna()  + ",输入户名为：" + input.getInacna());
		
		if(!CommUtil.equals(tblKnaCust.getCustna(), input.getInacna())){
			throw InError.comm.E0003("输入户名["+input.getInacna()+"]与客户账号注册户名["+tblKnaCust.getCustna()+"]不匹配");
		}
		bizlog.debug("客户账号状态检查结束==========");
		
	}
		
}
		
