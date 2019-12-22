package cn.sunline.ltts.busi.dp.base;

import java.math.BigDecimal;

import cn.sunline.ltts.busi.dp.acct.DpKnaAcct;
import cn.sunline.ltts.busi.dp.acct.DpKnaFxac;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;

/**
 * 活期定期对外共用方法
 * 
 * @author cuijia
 * 
 */
public class DpAcctComm {

	/**
	 * 存入控制检查
	 * 
	 * @param acctno
	 *            负债账号
	 * @param tranam
	 *            交易金额
	 * @param fcflag
	 *            定活标志
	 * @param prodcd
	 *            产品号
	 * @param crcycd
	 *            币种
	 */
	public static void checkDpSave(String acctno, BigDecimal tranam,
			E_FCFLAG fcflag, String prodcd, String crcycd,
			BaseEnumType.E_YES___ auacfg, String custac,
			BaseEnumType.E_YES___ ngblfg) {
		//活期检查
		if (E_FCFLAG.CURRENT == fcflag) {
			DpKnaAcct.validatePost(acctno, tranam, fcflag, prodcd, crcycd,
					auacfg, custac, ngblfg);
		} else if (E_FCFLAG.FIX == fcflag) {
			DpKnaFxac.validatePost(acctno, tranam, fcflag, prodcd, crcycd,
					ngblfg);
		} else {
			throw DpModuleError.DpstAcct.BNAS0843(fcflag.toString());
		}
	}
	
}
