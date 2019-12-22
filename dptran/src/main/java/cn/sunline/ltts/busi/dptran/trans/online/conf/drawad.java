package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDraw;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DRAWCT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AMNTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CTRLWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DRRULE;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TIMEWY;

public class drawad {

	/**
	 * 
	 * @Title: drwadd
	 * @Description: 新增存款支取控制部件信息
	 * @param input
	 * @author zhangjunlei
	 * @date 2016年7月14日 上午10:10:33
	 * @version V2.3.0
	 */
	public static void drwadd(
			final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Drawad.Input input) {

		String prodcd = input.getProdcd();// 产品编号
		E_DRAWCT drawtp = input.getDrawtp();// 支取控制方式
		E_CTRLWY ctrlwy = input.getCtrlwy();// 支取控制方法
		E_AMNTWY dramwy = input.getDramwy();// 支取金额控制方式
		BigDecimal drmiam = input.getDrmiam();// 单次支取最小金额
		BigDecimal drmxam = input.getDrmxam();// 单次支取最大金额
		E_TIMEWY drtmwy = input.getDrtmwy();// 支取次数控制方式
		Long drmitm = input.getDrmitm();// 最小支取次数
		Long drmxtm = input.getDrmxtm();// 最大支取次数
		E_DRRULE drrule = input.getDrrule();// 支取规则
		BigDecimal minibl = input.getMinibl();// 账户留存最小余额
		E_YES___ ismibl = input.getIsmibl();// 是否允许小于账户留存最小余额

		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());// 操作网点权限验证

		// 产品编号
		if (CommUtil.isNull(prodcd)) {
			throw DpModuleError.DpstProd.BNAS1054();
		}

		// 支取控制方式
		if (CommUtil.isNull(drawtp)) {
			throw DpModuleError.DpstComm.BNAS2125();
		}

//		// 支取规则
//		if (CommUtil.isNull(drrule)) {
//			throw DpModuleError.DpstComm.E9999("支取规则不能为空");
//		}
		

		// 账户留存最小余额
		if (CommUtil.isNotNull(minibl)) {
			if(CommUtil.compare(minibl, BigDecimal.ZERO) < 0){
				throw DpModuleError.DpstComm.BNAS2122();
			}
		}

		// 是否允许小于账户留存最小余额
		if (CommUtil.isNull(ismibl)) {
			throw DpModuleError.DpstComm.BNAS2123();
		}
		
		if(CommUtil.compare(minibl, BigDecimal.ZERO) == 0 && E_YES___.NO != ismibl){
			throw DpModuleError.DpstComm.BNAS2124();
		}

		// 支取控制方式为有条件允许支取，支取控制方法不能为空
		if (drawtp == E_DRAWCT.COND) {

			if (CommUtil.isNull(ctrlwy)) {// 支取控制方法不能为空
				throw DpModuleError.DpstComm.BNAS0122();
			} else if (CommUtil.isNotNull(ctrlwy)) {// 支取控制方法不为空

				// 支取控制方法为金额控制
				if (ctrlwy == E_CTRLWY.AMCL) {
					ckdramwy(dramwy, drmiam, drmxam);// 支取控制方法为金额控制时检查
 
					// 支取次数控制应为空
					if (CommUtil.isNotNull(drtmwy)
							|| CommUtil.isNotNull(drmitm)
							|| CommUtil.isNotNull(drmxtm)) {
						throw DpModuleError.DpstComm.BNAS2126();
					}

					// 支取控制方法为次数控制
				} else if (ctrlwy == E_CTRLWY.TMCL) {
					ckdrtmwy(drtmwy, drmitm, drmxtm);// 支取控制方法为次数控制时检查

					// 支取金额控制应为空
					if (CommUtil.isNotNull(dramwy)
							|| CommUtil.isNotNull(drmiam)
							|| CommUtil.isNotNull(drmxam)) {
						throw DpModuleError.DpstComm.BNAS2127();
					}

					// 支取控制方法为金额和次数控制
				} else if (ctrlwy == E_CTRLWY.ATML) {
					ckdramwy(dramwy, drmiam, drmxam);// 对金额控制检查
					ckdrtmwy(drtmwy, drmitm, drmxtm);// 对次数控制检查
				}
			}

			// 支取控制方式为无条件允许支取时条件为空
		} else if (drawtp == E_DRAWCT.YES) {
			if (CommUtil.isNotNull(ctrlwy) || CommUtil.isNotNull(dramwy)
					|| CommUtil.isNotNull(drmiam) || CommUtil.isNotNull(drmxam)
					|| CommUtil.isNotNull(drtmwy) || CommUtil.isNotNull(drmitm)
					|| CommUtil.isNotNull(drmxtm)) {
				throw DpModuleError.DpstComm.BNAS2128();
			}
		}

		// 产品基础属性表
		List<KupDppb> tblDppb = KupDppbDao.selectAll_odb4(prodcd, false);

		if (tblDppb.size() > 0) {
			throw DpModuleError.DpstComm.BNAS1999();
		}
		
		
		// 存款产品部件临时表
		KupDppbPartTemp tblPartTemp = KupDppbPartTempDao
				.selectFirst_odb3(prodcd, false);
		if (CommUtil.isNull(tblPartTemp)) {
			throw DpModuleError.DpstComm.BNAS1997();
		}

		// 产品支取控制表
		KupDppbDraw tblKupDraw = KupDppbDrawDao.selectOne_odb2(prodcd,
				false);
		if (CommUtil.isNotNull(tblKupDraw)) {
			throw DpModuleError.DpstComm.BNAS2129();
		}

		// 产品基础属性临时表
		KupDppbTemp tblDppb_temp = KupDppbTempDao.selectOne_odb1(prodcd,
				false);

		if (CommUtil.isNull(tblDppb_temp)) {
			throw DpModuleError.DpstComm.BNAS1999();
		}
		
		String crcycd = tblDppb_temp.getPdcrcy();// 产品适用币种
		
		// 产品存入控制临时表
		KupDppbPostTemp tblKup = KupDppbPostTempDao.selectOne_odb1(prodcd,crcycd, false);
		if (CommUtil.isNull(tblKup)) {
			throw DpModuleError.DpstComm.BNAS2130();
		}
		if(tblKup.getDetlfg()==E_YES___.YES && CommUtil.isNull(drrule)){
			throw DpModuleError.DpstComm.BNAS2131();
		}
		if(tblKup.getDetlfg()==E_YES___.NO && CommUtil.isNotNull(drrule)){
			throw DpModuleError.DpstComm.BNAS2132();
		}
		
		// 产品支取控制临时表
		KupDppbDrawTemp tblDppb_draw_temp = SysUtil.getInstance(KupDppbDrawTemp.class);

		if (CommUtil.isNotNull(KupDppbDrawTempDao.selectOne_odb1(prodcd,
				crcycd, false))) {
			throw DpModuleError.DpstComm.BNAS2133();
		}

		// 存款产品部件临时表
		KupDppbPartTemp tblDppb_part_temp = KupDppbPartTempDao
				.selectOne_odb1(E_BUSIBI.DEPO, prodcd, E_PARTCD._CK06, false);

		if (CommUtil.isNull(tblDppb_part_temp)) {
			throw DpModuleError.DpstComm.BNAS2001();
		}

		// 判断存入控制部件是否启用
		if (tblDppb_part_temp.getPartfg() == E_YES___.NO) {
			throw DpModuleError.DpstComm.BNAS2002();
		} else {
			
			tblDppb_draw_temp.setProdcd(prodcd);// 产品编号
			tblDppb_draw_temp.setCrcycd(crcycd);// 币种
			tblDppb_draw_temp.setDrawtp(drawtp);// 支取控制方式
			tblDppb_draw_temp.setCtrlwy(ctrlwy);// 支取控制方法
			tblDppb_draw_temp.setDramwy(dramwy);// 支取金额控制方式
			tblDppb_draw_temp.setDrmiam(drmiam);// 单次支取最小金额
			tblDppb_draw_temp.setDrmxam(drmxam);// 单次支取最大金额
			tblDppb_draw_temp.setDrtmwy(drtmwy);// 支取次数控制方式
			tblDppb_draw_temp.setDrmitm(drmitm);// 最小支取次数
			tblDppb_draw_temp.setDrmxtm(drmxtm);// 最大支取次数
			tblDppb_draw_temp.setDrrule(drrule);// 支取规则
			tblDppb_draw_temp.setMinibl(minibl);// 账户留存最小余额
			tblDppb_draw_temp.setIsmibl(ismibl);// 是否允许小于账户留存最小余额

			KupDppbDrawTempDao.insert(tblDppb_draw_temp);// 新增产品支取控制信息
		}
	}

	/**
	 * 
	 * @Title: ckdramwy
	 * @Description: 支取控制方法为金额控制时检查
	 * @param dramwy
	 *            支取金额控制方式
	 * @param drmiam
	 *            单次支取最小金额
	 * @param drmxam
	 *            单次支取最大金额
	 * @author zhangjunlei
	 * @date 2016年7月14日 上午9:19:33
	 * @version V2.3.0
	 */
	public static void ckdramwy(E_AMNTWY dramwy, BigDecimal drmiam,
			BigDecimal drmxam) {

		if (CommUtil.isNull(dramwy)) {// 支取金额控制方式不能为空
			throw DpModuleError.DpstComm.BNAS2002();
		} else if (CommUtil.isNotNull(dramwy)) {// 支取金额控制方式不为空

			if (dramwy == E_AMNTWY.MNAC) {// 支取金额控制方式为控制最小金额

				// 单次支取最小金额不能为空
				if (CommUtil.isNull(drmiam)) {
					throw DpModuleError.DpstComm
							.BNAS2134();
				}
				
				if(CommUtil.compare(drmiam, BigDecimal.ZERO) < 0){
					throw DpModuleError.DpstComm
							.BNAS2135();
				}

				// 单次支取最大金额应为空
				if (!CommUtil.equals(drmxam , BigDecimal.ZERO) && CommUtil.isNotNull(drmxam)) {
					throw DpModuleError.DpstComm.BNAS2136();
				}

			} else if (dramwy == E_AMNTWY.MXAC) {// 支取金额控制方式为控制最大金额

				// 单次支取最大金额不能为空
				if (CommUtil.isNull(drmxam)) {
					throw DpModuleError.DpstComm
							.BNAS2137();
				}
				
				if(CommUtil.compare(drmxam, BigDecimal.ZERO) < 0){
					throw DpModuleError.DpstComm
							.BNAS2138();
				}

				// 单次支取最小金额应为空
				if (!CommUtil.equals(drmiam, BigDecimal.ZERO) && CommUtil.isNotNull(drmiam)) {
					throw DpModuleError.DpstComm.BNAS2139();
				}

			} else if (dramwy == E_AMNTWY.SCAC) {// 支取金额控制方式为控制金额范围

				// 单次支取最小金额不能为空
				if (CommUtil.isNull(drmiam)) {
					throw DpModuleError.DpstComm
							.BNAS2140();
				}
				
				if(CommUtil.compare(drmiam, BigDecimal.ZERO) < 0){
					throw DpModuleError.DpstComm
							.BNAS2141();
				}

				// 单次支取最大金额不能为空
				if (CommUtil.isNull(drmxam)) {
					throw DpModuleError.DpstComm
							.BNAS2142();
				}
				
				if(CommUtil.compare(drmxam, BigDecimal.ZERO) < 0){
					throw DpModuleError.DpstComm
							.BNAS2143();
				}

				// 单次支取最小金额不能大于单次支取最大金额
				if (CommUtil.compare(drmiam, drmxam) > 0) {
					throw DpModuleError.DpstComm
							.BNAS2144();
				}
			}
		}
	}

	/**
	 * 
	 * @Title: ckdrtmwy
	 * @Description: 支取控制方法为次数控制时检查
	 * @param drtmwy
	 *            支取次数控制方式
	 * @param drmitm
	 *            最小支取次数
	 * @param drmxtm
	 *            最大支取次数
	 * @author zhangjunlei
	 * @date 2016年7月14日 上午9:21:16
	 * @version V2.3.0
	 */
	public static void ckdrtmwy(E_TIMEWY drtmwy, Long drmitm, Long drmxtm) {

		// 支取次数控制方式不能为空
		if (CommUtil.isNull(drtmwy)) {
			throw DpModuleError.DpstComm.BNAS2145();
		} else if (CommUtil.isNotNull(drtmwy)) {// 支取金额控制方式不为空

			if (drtmwy == E_TIMEWY.MNTM) {// 支取次数控制方式为控制最小次数

				// 最小支取次数不能为空
				if (CommUtil.isNull(drmitm)) {
					throw DpModuleError.DpstComm.BNAS2146();
				}
				
				if(drmitm < 0){
					throw DpModuleError.DpstComm.BNAS2147();
				}

				// 最大支取次数应为空
				if (CommUtil.isNotNull(drmxtm)) {
					throw DpModuleError.DpstComm.BNAS2148();
				}

			} else if (drtmwy == E_TIMEWY.MXTM) {// 支取次数控制方式为控制最大次数

				// 最大支取次数不能为空
				if (CommUtil.isNull(drmxtm)) {
					throw DpModuleError.DpstComm.BNAS2149();
				}
				
				if(drmxtm < 0){
					throw DpModuleError.DpstComm.BNAS2150();
				}

				// 最小支取次数应为空
				if (CommUtil.isNotNull(drmitm)) {
					throw DpModuleError.DpstComm.BNAS2151();
				}

			} else if (drtmwy == E_TIMEWY.SCTM) {// 支取金额控制方式为控制次数范围

				// 最小支取次数不能为空
				if (CommUtil.isNull(drmitm)) {
					throw DpModuleError.DpstComm.BNAS2152();
				}
				
				if(drmitm < 0){
					throw DpModuleError.DpstComm.BNAS2153();
				}

				// 最大支取次数不能为空
				if (CommUtil.isNull(drmxtm)) {
					throw DpModuleError.DpstComm.BNAS2154();
				}
				
				if(drmxtm < 0){
					throw DpModuleError.DpstComm.BNAS2155();
				}

				// 最小支取次数不能大于最大支取次数
				if (CommUtil.compare(drmitm, drmxtm) > 0) {
					throw DpModuleError.DpstComm
							.BNAS2156();
				}
			}
		}
	}
}
