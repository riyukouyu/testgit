
package cn.sunline.edsp.busi.dptran.trans;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaadDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;

public class quseca {

	/**
	 * @author zhangjian
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void quDeBank( final cn.sunline.edsp.busi.dptran.trans.intf.Quseca.Input input,  final cn.sunline.edsp.busi.dptran.trans.intf.Quseca.Property property,  final cn.sunline.edsp.busi.dptran.trans.intf.Quseca.Output output){
		String mactid = input.getMactid();
		String cdopac = input.getCdopac();
		// 主账户开户标识
	    if(CommUtil.isNull(mactid)) {
	    	throw DpModuleError.DpAcct.AT010013();
	    }
	    // 检查电子主账户信息是否存在。
 		KnaMaad tblKnaMaad = KnaMaadDao.selectOne_odb2(mactid, false);
 		if(CommUtil.isNull(tblKnaMaad)) {
 			throw DpModuleError.DpAcct.AT020028();
 		}
 		String custac = tblKnaMaad.getCustac();//电子账户
 		List<KnaCacd> lsKnaCacd = null;
 		if(CommUtil.isNull(cdopac)) {
 			lsKnaCacd = KnaCacdDao.selectAll_odb9(E_DPACST.DEFAULT, custac, false);//查询主电子账户下所有默认卡
 		}else {
 			lsKnaCacd = KnaCacdDao.selectAll_odb8(cdopac, E_DPACST.DEFAULT, custac, false);//查询主电子账户下是否存在该默认卡
 		}
 		output.getCardList().addAll(lsKnaCacd);;

	}
}
