package cn.sunline.ltts.busi.dptran.trans;


import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;

public class fxactf {

	public static void checkBefore(
			final cn.sunline.ltts.busi.dptran.trans.intf.Fxactf.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Fxactf.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Fxactf.Output output) {
		
		//校验电子账户
		KnaCust knaCust = KnaCustDao.selectOne_odb1(input.getOucuac(), false);
		if(CommUtil.isNull(knaCust)){
			throw DpModuleError.DpstComm.BNAS0754();
		}
		IoCaKnaCust knaCust1 = ActoacDao.selKnaCustByCustacCorp(input.getIncuac(), input.getIncpno(), false);
		if(CommUtil.isNull(knaCust1)){
			throw DpModuleError.DpstComm.E9999("受让电子账号不存在");
		}
		//校验负债账号
		KnaFxac tblKnaFxac = SysUtil.getInstance(KnaFxac.class);
		tblKnaFxac = ActoacDao.selKnaFxac(input.getAcctno(), false);
		if(CommUtil.isNull(tblKnaFxac)){
			throw DpModuleError.DpstAcct.BNAS0767();
		}
		//校验负债账号和户名一致性
		if(CommUtil.isNotNull(input.getOucuna()) && !CommUtil.equals(input.getOucuna(), tblKnaFxac.getAcctna())){
			throw DpModuleError.DpstComm.BNAS0892();
		}
		//校验产品
		if(CommUtil.isNotNull(input.getProdcd())){
			KupDppb kupdppb = KupDppbDao.selectOne_odb1(input.getProdcd(), false);
			if(CommUtil.isNull(kupdppb)){
				throw DpModuleError.DpstComm.E9999("改产品未配置");
			}else{
				if(!CommUtil.equals(input.getProdtx(), kupdppb.getProdtx())){
					throw DpModuleError.DpstComm.E9999("产品编号与产品名称不一致");
				}
			}
		}
	}
}
