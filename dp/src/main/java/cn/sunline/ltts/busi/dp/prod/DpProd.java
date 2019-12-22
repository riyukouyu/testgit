package cn.sunline.ltts.busi.dp.prod;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActpDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCust;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCustDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.type.DpPoc.AcctnoInfo;
import cn.sunline.ltts.busi.dp.type.DpPoc.CustacInfo;
import cn.sunline.ltts.busi.dp.type.DpPoc.TranInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstProd;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT;

public class DpProd {

	// 获取产品基本信息
	public static KupDppb getKupDppb(String procdcd) {
		return KupDppbDao.selectOne_odb1(procdcd, true);
	}

	/**
	 * 获取产品账户类型控制信息
	 * 
	 * @param prodcd
	 *            产品号
	 * @param crcycd
	 *            币种
	 * @return
	 */
	public static KupDppbActp getKupDppbActp(String prodcd, E_CUSACT cacttp) {
		return KupDppbActpDao.selectOne_odb1(prodcd, cacttp, true);
	}

	/**
	 * 产品账户类型控制
	 * 
	 * @param cplKupDppbActp
	 */
	public static void checkCacttp(CustacInfo cplCustacInfo,
			AcctnoInfo cplAcctnoInfo, KupDppb cplKupDppb,
			KupDppbActp cplKupDppbActp) {
		// 账户下唯一标识
		if (BaseEnumType.E_YES___.YES == cplKupDppbActp.getAcolfg()) {

			switch (cplKupDppb.getPddpfg()) {
			case CURRENT: {
				// 查询该产品编号下是否已开负债账户
				List<KnaAcct> tblKnaAccts = KnaAcctDao.selectAll_odb3(
						cplAcctnoInfo.getCrcycd(), cplKupDppb.getProdcd(),
						cplCustacInfo.getCustac(), false);

				// 发现多条数据跑出异常
				if (tblKnaAccts.size() > 0) {
					throw DpModuleError.DpstProd.BNAS1724();
				}

				break;
			}
			case FIX: {
				List<KnaFxac> tblKnaFxacs = KnaFxacDao.selectAll_odb2(
						cplAcctnoInfo.getCrcycd(), cplKupDppb.getProdcd(),
						cplCustacInfo.getCustac(), false);
				// 发现多条数据跑出异常
				if (tblKnaFxacs.size() > 0) {
					throw DpModuleError.DpstProd.BNAS1724();
				}
				break;
			}
			default:
				throw DpModuleError.DpstComm.BNAS1083();
			}
		}
	}

	/**
	 * 获取产品开户控制信息
	 * 
	 * @param prodcd
	 *            产品号
	 * @param crcycd
	 *            币种
	 * @return
	 */
	public static KupDppbCust getKupDppbCust(String prodcd, String crcycd) {
		return KupDppbCustDao.selectOne_odb1(prodcd, crcycd, true);
	}

	/**
	 * 产品开户控制
	 * 
	 * @param cplCustacInfo
	 * @param cplAcctnoInfo
	 * @param cplKupDppb
	 * @param cplKupDppbCust
	 */
	public static void checkOpen(CustacInfo cplCustacInfo,
			AcctnoInfo cplAcctnoInfo, TranInfo cplTranInfo,
			KupDppb cplKupDppb, KupDppbCust cplKupDppbCust) {
		if (DpEnumType.E_ONLYFG.ONLO == cplKupDppbCust.getOnlyfg()) {
			switch (cplKupDppb.getPddpfg()) {
			case CURRENT: {
				List<KnaAcct> tblKnaAccts = KnaAcctDao.selectAll_odb2(
						cplCustacInfo.getCustno(), cplKupDppb.getProdcd(),
						false);
				if (tblKnaAccts.size() > 0) {
					throw DpModuleError.DpstProd.BNAS1714();
				}
				break;
			}
			case FIX: {
				List<KnaFxac> tblKnaFxacs = KnaFxacDao.selectAll_odb3(
						cplCustacInfo.getCustno(), cplKupDppb.getProdcd(),
						false);
				if (tblKnaFxacs.size() > 1) {
					throw DpModuleError.DpstProd.BNAS1714();
				}
				break;
			}
			default:
				throw DpModuleError.DpstComm.BNAS1083();
			}
		}

		// 起存金额控制
		if (CommUtil.compare(cplTranInfo.getTranam(),
				cplKupDppbCust.getSrdpam()) < 0) {
			throw DpModuleError.DpstComm.BNAS0629(cplTranInfo.getTranam().toString(),cplKupDppbCust.getSrdpam().toString());
		}

	}
}
