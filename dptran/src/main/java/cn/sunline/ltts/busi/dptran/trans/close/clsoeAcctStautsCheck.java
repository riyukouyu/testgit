package cn.sunline.ltts.busi.dptran.trans.close;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;


public class clsoeAcctStautsCheck {
	
	/**
	 * 
	 * @Title: acctStautsCheck 
	 * @Description: (销户状态状态字检查,获取电子账号ID) 
	 * @param cardno
	 * @return
	 * @author xiongzhao
	 * @date 2016年7月7日 下午8:15:10 
	 * @version V2.3.0
	 */
	public static IoCaKnaCust acctStautsCheck(String cardno) {
		
		// 检查输入是否不为空
		if (CommUtil.isNull(cardno)) {
			throw DpModuleError.DpstComm.BNAS0311();
		}
		
		// 调用Ca模块服务查询电子账户信息
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		
        // 根据电子账户查询出电子账户ID
		IoCaKnaAcdc cplKnaAcdc = caqry.getKnaAcdcOdb2(cardno, false);
		if (CommUtil.isNull(cplKnaAcdc) || cplKnaAcdc.getStatus() == E_DPACST.CLOSE) {
			throw DpModuleError.DpstComm.BNAS0754();
		}
		
		// 查询电子账户表
		IoCaKnaCust cplKnaCust = caqry.getKnaCustByCustacOdb1(
				cplKnaAcdc.getCustac(), false);
		if (CommUtil.isNull(cplKnaCust)) {
			throw DpModuleError.DpstComm.BNAS0754();
		}

		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(cplKnaAcdc.getCustac());

		// 检查电子账户状态
		if (cuacst == E_CUACST.CLOSED) {
			throw DpModuleError.DpstComm.BNAS0441();
		}
		if (cuacst == E_CUACST.OUTAGE) {
			throw DpModuleError.DpstComm.BNAS0441();
		}
		if (cuacst == E_CUACST.PREOPEN) {
			throw DpModuleError.DpstComm.BNAS0441();
		}
		if (cuacst == E_CUACST.NOENABLE) {
			throw DpModuleError.DpstComm.BNAS0441();
		}

		// 查询电子账户冻结状态 查询冻结主体登记簿
		IoDpAcStatusWord froz = SysUtil.getInstance(IoDpFrozSvcType.class)
				.getAcStatusWord(cplKnaCust.getCustac());
		
		//检查电子账户状态字
		if (froz.getBrfroz() == BaseEnumType.E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0441();
		}
		if (froz.getDbfroz() == BaseEnumType.E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0441();
		}
		if (froz.getPtfroz() == BaseEnumType.E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0441();
		}
		if (froz.getBkalsp() == BaseEnumType.E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0441();
		}
		if (froz.getOtalsp() == BaseEnumType.E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0441();
		}
		if (froz.getPtstop() == BaseEnumType.E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0441();
		}
		if (froz.getClstop() == BaseEnumType.E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0441();
		}
		if (froz.getCertdp() == BaseEnumType.E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0441();
		}
		if (froz.getPledge() == BaseEnumType.E_YES___.YES) {
			throw DpModuleError.DpstComm.BNAS0441();
		}

		return cplKnaCust;
	}
	
}
