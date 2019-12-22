package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbUnat;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbUnatDao;
import cn.sunline.ltts.busi.catran.batchtran.unactiDataProcessor;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class acteac {

	private static BizLog bizlog = BizLogUtil.getBizLog(unactiDataProcessor.class);
	public static void acteac( final cn.sunline.ltts.busi.catran.trans.intf.Acteac.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Acteac.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Acteac.Output output){
		
		String cardno = input.getCardno();
		String custna = input.getCustna();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//当前交易时间
		String timetm = DateTools2.getCurrentTimestamp();// 当前交易日期时间戳
		
		if(CommUtil.isNull(cardno)){
			throw CaError.Eacct.BNAS0570();
		}
		
		if(CommUtil.isNull(custna)){
			throw DpModuleError.DpstComm.BNAS1260();
		}
		
		KnbUnat tblknaunat = KnbUnatDao.selectFirst_odb1(cardno, custna, E_YES___.YES, false);
		
		
		if(CommUtil.isNull(tblknaunat)){
			throw CaError.Eacct.BNAS1203();
		}
		
		String custac = tblknaunat.getCustac();
		
		//更新激活日期及非活跃标识
		tblknaunat.setActidt(trandt);
		tblknaunat.setIsacti(E_YES___.NO);
		KnbUnatDao.update_odb2(tblknaunat);
		
	
		
		// 根据电子账户获取电子账户表数据
		IoCaKnaCust cplCaKnaCust = SysUtil.getInstance(
							IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(custac,
							false);
		if (CommUtil.isNull(cplCaKnaCust)) {
						// throw DpModuleError.DpstComm.E9999("电子账号不存在! ");
						bizlog.error("电子账号不存在! ", trandt);
						return;
					}
		// 更新电子账户账户状态为非活跃
		DpDayEndDao.updKnaCustSleep(E_ACCTST.NORMAL,
										cplCaKnaCust.getCustac(),timetm);
		// 更新电子账户负债活期子账户状态为非活跃
		DpDayEndDao.updKnaAcctSleep(E_ACCTST.NORMAL,
										cplCaKnaCust.getCustac(), E_ACCTST.INTIVE,timetm);
		
		//更新客户化状态
		// 更新为活跃状态
		IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
		cplDimeInfo.setCustac(custac);
					SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(
							cplDimeInfo);
					
					
		
	}
}
