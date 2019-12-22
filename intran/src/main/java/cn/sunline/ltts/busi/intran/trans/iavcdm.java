package cn.sunline.ltts.busi.intran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;


public class iavcdm {

	/**
	 * 
	 * @param acstno
	 * @param trandt
	 * @param transq
	 * @param acctdt
	 * @param property
	 * 2016年12月12日-下午3:48:34
	 * @auther chenjk
	 */
	public static void AfterTransferCheck( String acstno){
		
		//平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._10);

	}
}
