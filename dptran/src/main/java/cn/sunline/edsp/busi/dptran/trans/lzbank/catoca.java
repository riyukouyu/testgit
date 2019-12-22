
package cn.sunline.edsp.busi.dptran.trans.lzbank;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;

public class catoca {

	public static void beforeTranDeal(final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Catoca.Input input,
			final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Catoca.Property property,
			final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Catoca.Output output) {
		//渠道流水
		if (CommUtil.isNull(input.getServsq())) {
			throw DpModuleError.DpTrans.TS010003();
		}
		//渠道日期
		if (CommUtil.isNull(input.getServdt())) {
			throw DpModuleError.DpTrans.TS010004();
		}
		//渠道类型
		if (CommUtil.isNull(input.getServtp())) {
			throw DpModuleError.DpTrans.TS010005();
		}
		//转入账号
		if (CommUtil.isNull(input.getInacno())) {
			throw DpModuleError.DpstAcct.BNAS0028();
		}
		//交易金额
		if (CommUtil.isNull(input.getTranam())) {
			throw DpModuleError.DpstAcct.BNAS0623();
		}
		//转出账号
		if (CommUtil.isNull(input.getOtacno())) {
			throw DpModuleError.DpstComm.BNAS0041();
		}
		
		
		KnaSbad knaSbadOt = KnaSbadDao.selectOne_odb2(input.getOtacno(), false);
		if (CommUtil.isNull(knaSbadOt)) {
			throw DpModuleError.DpTrans.TS020022();
		}
		
		property.setOtcsac(knaSbadOt.getCustac());
		property.setOtcard(KnaAcdcDao.selectFirst_odb1(knaSbadOt.getCustac(), E_DPACST.NORMAL, false).getCardno());
		
		KnaSbad knaSbadIn = KnaSbadDao.selectOne_odb2(input.getInacno(), false);
		if (CommUtil.isNull(knaSbadIn)) {
			throw DpModuleError.DpTrans.TS020023();
		}
		
		property.setIncsac(knaSbadIn.getCustac());
		property.setIncard(KnaAcdcDao.selectFirst_odb1(knaSbadIn.getCustac(), E_DPACST.NORMAL, false).getCardno());
	}

	public static void afterTranDeal(final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Catoca.Input input,
			final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Catoca.Property property,
			final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Catoca.Output output) {

		// 平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(),
						CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._35);
		
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
		
	}
}
