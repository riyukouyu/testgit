package cn.sunline.ltts.busi.intran.trans;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.sys.errors.InError;


public class fnrgpb {

public static void beforeRegister( final cn.sunline.ltts.busi.intran.trans.intf.Fnrgpb.Input Input,  final cn.sunline.ltts.busi.intran.trans.intf.Fnrgpb.Output Output){
    //录入日期
    if(CommUtil.isNull(Input.getCommdt())){
        throw InError.comm.E0003("录入日期不能为空");
    }
    //录入流水
    if(CommUtil.isNull(Input.getInptsq())){
        throw InError.comm.E0003("录入流水不能为空");
    }
    //科目号
    if(CommUtil.isNull(Input.getItemcd())){
        throw InError.comm.E0003("科目号不能为空");
    }
    //记账方向
    if(CommUtil.isNull(Input.getAmntcd())){
        throw InError.comm.E0003("记账方向不能为空");
    }
    //记账金额
    if(CommUtil.isNull(Input.getTranam())){
        throw InError.comm.E0003("记账金额不能为空");
    }
    if(CommUtil.compare(Input.getTranam(), BigDecimal.ZERO)==0){
        throw InError.comm.E0003("交易金额不能为零");
    }
    //币种
    if(CommUtil.isNull(Input.getCrcycd())){
        throw InError.comm.E0003("币种不能为空");
    }
    /*if(!CommUtil.isInEnum(E_CRCYCD.class, Input.getCrcycd())){
        throw InError.comm.E0003("币种取值超出范围");
    }*/
    //类型代码
    if(CommUtil.isNull(Input.getTypecd())){
        throw InError.comm.E0003("类型代码不能为空");
    }
}
}
