package cn.sunline.ltts.busi.intran.trans;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.sys.errors.InError;


public class cpcltn {

	public static void chkTransfBefore( final cn.sunline.ltts.busi.intran.trans.intf.Cpcltn.Input Input,  final cn.sunline.ltts.busi.intran.trans.intf.Cpcltn.Property Property,  final cn.sunline.ltts.busi.intran.trans.intf.Cpcltn.Output Output){
		BigDecimal feeaam = Input.getFeeaam(); //手续费
		BigDecimal tranam = Input.getTranam(); //代收金额
		
		if(CommUtil.isNull(feeaam)){
			throw InError.comm.E0003("手续费不能为空");
		}
		
		if(CommUtil.isNull(tranam)){
			throw InError.comm.E0003("代收金额不能为空");
		}
		
		if(CommUtil.compare(feeaam, BigDecimal.ZERO) < 0){
			throw InError.comm.E0003("手续费不能小于0");
		}
		
		if(CommUtil.compare(tranam, BigDecimal.ZERO) < 0){
			throw InError.comm.E0003("代收金额不能小于0");
		}
		
		Property.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch()); //机构号
		/*Property.setLiquam(tranam.subtract(feeaam));  //清算金额
*/		
		
	}

	public static void CheckTransfAfter( final cn.sunline.ltts.busi.intran.trans.intf.Cpcltn.Input Input,  final cn.sunline.ltts.busi.intran.trans.intf.Cpcltn.Property Property,  final cn.sunline.ltts.busi.intran.trans.intf.Cpcltn.Output Output){
		//平衡性检查 开关开时检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
	}
}
