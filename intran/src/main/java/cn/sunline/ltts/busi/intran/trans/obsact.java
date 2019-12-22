package cn.sunline.ltts.busi.intran.trans;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;


public class obsact {

public static void beforeTransCheck( final cn.sunline.ltts.busi.intran.trans.intf.Obsact.Input Input,  final cn.sunline.ltts.busi.intran.trans.intf.Obsact.Property Property,  final cn.sunline.ltts.busi.intran.trans.intf.Obsact.Output Output){
	//输入为空检查
	if(CommUtil.isNull(Input.getAcctno())){
		throw InError.comm.E0003("内部户帐号不能为空");
	}
	if(CommUtil.isNull(Input.getAmntcd())){
		throw InError.comm.E0003("借贷方向不能为空");
	}
	if(!(Input.getAmntcd()==E_AMNTCD.PY || Input.getAmntcd()==E_AMNTCD.RV)){
		throw InError.comm.E0003("借贷方向["+Input.getAmntcd()+"]不正确，[P,R]");
	}
	if(CommUtil.isNull(Input.getCrcycd())){
		throw InError.comm.E0003("币种不能为空");
	}
//	if(CommUtil.isInEnum(E_CRCYCD.class, Input.getCrcycd())){
//		throw InError.comm.E0003("币种["+Input.getCrcycd()+"]未定义");
//	}
	if(CommUtil.isNull(Input.getTranam())||CommUtil.compare(Input.getTranam(), BigDecimal.ZERO)==0){
		throw InError.comm.E0003("交易金额不能为空或为0");
	}
}

public static void afterTransCheck( final cn.sunline.ltts.busi.intran.trans.intf.Obsact.Input Input,  final cn.sunline.ltts.busi.intran.trans.intf.Obsact.Property Property,  final cn.sunline.ltts.busi.intran.trans.intf.Obsact.Output Output){
	
}
}
