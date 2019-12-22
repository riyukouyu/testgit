package cn.sunline.ltts.busi.intran.trans;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.type.InQueryTypes.BalanceOfCmda;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.sys.errors.InError;


public class fnsact {

	public static void beforeAcct( String commsq,  String trandt,  final cn.sunline.ltts.busi.intran.trans.intf.Fnsact.Output Output){
		//套票号
		if(CommUtil.isNull(commsq)){
			throw InError.comm.E0003("套票号不能为空");
		}
		//记账日期
		if(CommUtil.isNull(trandt)){
			throw InError.comm.E0003("记账日期不能为空");
		}
		
		//检查财务往来记账登记簿同一套账是否平衡
		List<BalanceOfCmda> cmdas = null;
		try {
			cmdas = InacSqlsDao.CheckBalanceOfFnsCmbk(commsq, trandt, true);
		} catch (Exception e) {
			
		}
		if(CommUtil.isNotNull(cmdas)){
			throw InError.comm.E0003("登记簿平衡性检查失败！");
		}
	}

	public static void afterAcct( String commsq,  String trandt,  final cn.sunline.ltts.busi.intran.trans.intf.Fnsact.Output Output){
		
		IoCheckBalance ioCheckBanlance = SysUtil.getInstance(IoCheckBalance.class);
		//平衡性检查
		ioCheckBanlance.checkBalance(trandt, CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
		
	}
}
