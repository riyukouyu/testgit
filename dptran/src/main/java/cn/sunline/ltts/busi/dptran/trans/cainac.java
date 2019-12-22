package cn.sunline.ltts.busi.dptran.trans;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRANTP;


public class cainac {

public static void becainac( final cn.sunline.ltts.busi.dptran.trans.intf.Cainac.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Cainac.Output output){
	
	String otcard = input.getOtcard();//转出方账户
	String otcuac = input.getOtcuna();//转出方户名
	String otbrch = input.getOtbrch();//转出方机构
	String otbkno = input.getOtbkno();//转出银行行号
	String incard = input.getIncard();//转入方账户
	String incuna = input.getIncuna();//转入方户名
	String inbrch = input.getInbrch();//转入方机构
	String inbkno = input.getInbkno();//转入银行行号
	E_TRANTP trantp = input.getTrantp();//交易类型
	BigDecimal tranam = input.getTranam();//交易金额
	String crcycd = input.getCycrcd();//币种
	
	//初始化转出转入涉案可疑标识
	E_INSPFG inspfg = E_INSPFG.NONE;
	E_INSPFG inspfg1 = E_INSPFG.NONE;
	//若输入账号则必须输入账户户名
	if(CommUtil.isNotNull(otcard)){
		if(CommUtil.isNull(otcuac)){
			throw DpModuleError.DpstAcct.BNAS0387();
		}else{
			otbrch = CommTools.getBaseRunEnvs().getTrxn_branch();
		}
	}
	if(CommUtil.isNotNull(incard)){
		if(CommUtil.isNull(incuna)){
			throw DpModuleError.DpstAcct.BNAS0385();
		}else{
			inbrch = CommTools.getBaseRunEnvs().getTrxn_branch();
		}
	}
	//若输入账户户名则必须输入账号
	if(CommUtil.isNotNull(otcuac)){
		if(CommUtil.isNull(otcard)){
			throw DpModuleError.DpstAcct.BNAS0388();
		}
	}
	if(CommUtil.isNotNull(incuna)){
		if(CommUtil.isNull(incard)){
			throw DpModuleError.DpstAcct.BNAS0386();
		}
	}
	if(CommUtil.isNotNull(tranam)){
		if(CommUtil.compare(tranam, BigDecimal.ZERO) < 0){
			throw DpModuleError.DpstAcct.BNAS0626();
		}
	}
}
}
