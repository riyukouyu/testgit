package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPost;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SAVECT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AMNTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_POSTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TIMEWY;

public class depoad {

	/**
	 * 
	 * @Title: dpoadd
	 * @Description: 新增存款存入控制部件信息
	 * @param input
	 * @author zhangjunlei
	 * @date 2016年7月13日 下午3:19:42
	 * @version V2.3.0
	 */
	public static void dpoadd(
			final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Depoad.Input input) {

		String prodcd = input.getProdcd();// 产品编号
		E_SAVECT posttp = input.getPosttp();// 存入控制方式
		E_POSTWY postwy = input.getPostwy();// 存入控制方法
		E_AMNTWY amntwy = input.getAmntwy();// 存入金额控制方式
		BigDecimal miniam = input.getMiniam();// 单次存入最小金额
		BigDecimal maxiam = input.getMaxiam();// 单次存入最大金额
		E_TIMEWY timewy = input.getTimewy();// 存入次数控制方式
		Long minitm = input.getMinitm();// 最小存入次数
		Long maxitm = input.getMaxitm();// 最大存入次数
		E_YES___ detlfg = input.getDetlfg();// 是否明细汇总
		BigDecimal maxibl = input.getMaxibl();// 账户留存最大余额

		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());// 操作网点权限验证

		// 产品编号
		if (CommUtil.isNull(prodcd)) {
			throw DpModuleError.DpstProd.BNAS1054();
		}

		// 存入控制方式
		if (CommUtil.isNull(posttp)) {
			throw DpModuleError.DpstComm.BNAS2004();
		}

		// 是否明细汇总
		if (CommUtil.isNull(detlfg)) {
			throw DpModuleError.DpstComm.BNAS2005();
		}
		
		// 账户留存最大余额
		if (CommUtil.isNotNull(maxibl)) {
			if (CommUtil.compare(maxibl, BigDecimal.ZERO) < 0) {
				throw DpModuleError.DpstComm.BNAS2006();
			}
		}

		// 存入控制方式为有条件允许存入，存入控制方法不能为空
		if (posttp == E_SAVECT.COND) {

			if (CommUtil.isNull(postwy)) {// 存入控制方法不能为空
				throw DpModuleError.DpstComm.BNAS2007();
			} else if (CommUtil.isNotNull(postwy)) {// 存入控制方法不为空

				// 存入控制方法为金额控制
				if (postwy == E_POSTWY.AMCL) {
					ckamntwy(amntwy, miniam, maxiam);// 存入控制方法为金额控制时检查

					// 存入次数控制应为空
					if (CommUtil.isNotNull(timewy)
							|| CommUtil.isNotNull(minitm)
							|| CommUtil.isNotNull(maxitm)) {
						throw DpModuleError.DpstComm.BNAS2008();
					}

					// 存入控制方法为次数控制
				} else if (postwy == E_POSTWY.TMCL) {
					
					cktimewy(timewy, minitm, maxitm);// 存入控制方法为次数控制时检查

					// 支取金额控制应为空
					if (CommUtil.isNotNull(amntwy)
							|| CommUtil.isNotNull(miniam)
							|| CommUtil.isNotNull(maxiam)) {
						throw DpModuleError.DpstComm.BNAS2009();
					}

					// 存入控制方法为金额和次数控制
				} else if (postwy == E_POSTWY.ATMC) {
					ckamntwy(amntwy, miniam, maxiam);// 对金额控制检查
					cktimewy(timewy, minitm, maxitm);// 对次数控制检查
				}
			}

			// 存入控制方式为无条件允许存入时条件为空
		} else if (posttp == E_SAVECT.YES) {
			if (CommUtil.isNotNull(postwy) || CommUtil.isNotNull(amntwy)
					|| CommUtil.isNotNull(miniam) || CommUtil.isNotNull(maxiam)
					|| CommUtil.isNotNull(timewy) || CommUtil.isNotNull(minitm)
					|| CommUtil.isNotNull(maxitm)) {
				throw DpModuleError.DpstComm.BNAS2010();
			}
		}

		// 产品基础属性表
		List<KupDppb> tblDppb = KupDppbDao.selectAll_odb4(prodcd, false);

		if (tblDppb.size() > 0) {
			throw DpModuleError.DpstComm.BNAS2011();
		}

		// 存款产品部件临时表
		KupDppbPartTemp tblPartTemp = KupDppbPartTempDao
				.selectFirst_odb3(prodcd, false);
		if (CommUtil.isNull(tblPartTemp)) {
			throw DpModuleError.DpstComm.BNAS1997();
		}

		// 产品存入控制表
		KupDppbPost tblPost = KupDppbPostDao.selectOne_odb2(prodcd, false);
		if (CommUtil.isNotNull(tblPost)) {
			throw DpModuleError.DpstComm.BNAS1998();
		}

		// 产品基础属性临时表
		KupDppbTemp tblDppb_temp = KupDppbTempDao.selectOne_odb1(prodcd,
				false);

		if (CommUtil.isNull(tblDppb_temp)) {
			throw DpModuleError.DpstComm.BNAS2012();
		}
		
		
		if(tblDppb_temp.getPddpfg() == E_FCFLAG.CURRENT && detlfg != E_YES___.NO){
			throw DpModuleError.DpstComm.BNAS2013();
		}
		
		String crcycd = tblDppb_temp.getPdcrcy();// 产品适用币种

		KupDppbPostTemp tblKup = SysUtil.getInstance(KupDppbPostTemp.class);// 产品存入控制临时表

		// 该产品存入控制信息已存在，不能新增
		if (CommUtil.isNotNull(KupDppbPostTempDao.selectOne_odb1(prodcd,
				crcycd, false))) {
			throw DpModuleError.DpstComm.BNAS2000();
		}

		// 存款产品部件临时表
		KupDppbPartTemp tblDppb_part_temp = KupDppbPartTempDao
				.selectOne_odb1(E_BUSIBI.DEPO, prodcd, E_PARTCD._CK04, false);

		if (CommUtil.isNull(tblDppb_part_temp)) {
			throw DpModuleError.DpstComm.BNAS2001();
		}

		// 判断存入控制部件是否启用
		if (tblDppb_part_temp.getPartfg() == E_YES___.NO) {
			throw DpModuleError.DpstComm.BNAS2002();
		} else {

			// 已启用
			tblKup.setProdcd(prodcd);// 产品编号
			tblKup.setCrcycd(crcycd);// 币种
			tblKup.setPosttp(posttp);// 存入控制方式
			tblKup.setPostwy(postwy);// 存入控制方法
			tblKup.setAmntwy(amntwy);// 存入金额控制方式
			tblKup.setMiniam(miniam);// 单次存入最小金额
			tblKup.setMaxiam(maxiam);// 单次存入最大金额
			tblKup.setTimewy(timewy);// 存入次数控制方式
			tblKup.setMinitm(minitm);// 最小存入次数
			tblKup.setMaxitm(maxitm);// 最大存入次数
			tblKup.setDetlfg(detlfg);// 是否明细汇总
			tblKup.setMaxibl(maxibl);// 账户留存最大余额

			KupDppbPostTempDao.insert(tblKup);// 新增产品存入控制信息
		}

	}

	/**
	 * 
	 * @Title: ckamntwy
	 * @Description: 存入控制方法为金额控制时检查
	 * @param amntwy
	 *            存入金额控制方式
	 * @param miniam
	 *            单次存入最小金额
	 * @param maxiam
	 *            单次存入最大金额
	 * @author zhangjunlei
	 * @date 2016年7月13日 下午4:04:12
	 * @version V2.3.0
	 */
	public static void ckamntwy(E_AMNTWY amntwy, BigDecimal miniam,
			BigDecimal maxiam) {

		if (CommUtil.isNull(amntwy)) {// 存入金额控制方式不能为空
			throw DpModuleError.DpstComm.BNAS2014();
		} else if (CommUtil.isNotNull(amntwy)) {// 存入金额控制方式不为空

			if (amntwy == E_AMNTWY.MNAC) {// 存入金额控制方式为控制最小金额

				// 单次存入最小金额不能为空
				if (CommUtil.isNull(miniam)) {
					throw DpModuleError.DpstComm.BNAS2015();
				}
				
				if(CommUtil.compare(miniam, BigDecimal.ZERO) < 0){
					throw DpModuleError.DpstComm.BNAS2016();
				}

				// 单次存入最大金额应为空
				if (CommUtil.isNotNull(maxiam)) {
					throw DpModuleError.DpstComm.BNAS2017();
				}

			} else if (amntwy == E_AMNTWY.MXAC) {// 存入金额控制方式为控制最大金额

				// 单次存入最大金额不能为空
				if (CommUtil.isNull(maxiam)) {
					throw DpModuleError.DpstComm
							.BNAS2018();
				}
				
				if(CommUtil.compare(maxiam, BigDecimal.ZERO) < 0){
					throw DpModuleError.DpstComm.BNAS2019();
				}

				// 单次存入最小金额应为空
				if (CommUtil.isNotNull(miniam)) {
					throw DpModuleError.DpstComm.BNAS2020();
				}

			} else if (amntwy == E_AMNTWY.SCAC) {// 存入金额控制方式为控制金额范围

				// 单次存入最小金额不能为空
				if (CommUtil.isNull(miniam)) {
					throw DpModuleError.DpstComm
							.BNAS2021();
				}
				
				if(CommUtil.compare(miniam, BigDecimal.ZERO) < 0){
					throw DpModuleError.DpstComm
							.BNAS2022();
				}

				// 单次存入最大金额不能为空
				if (CommUtil.isNull(maxiam)) {
					throw DpModuleError.DpstComm
							.BNAS2023();
				}
				
				if(CommUtil.compare(maxiam, BigDecimal.ZERO) < 0){
					throw DpModuleError.DpstComm
							.BNAS2024();
				}

				// 单次存入最小金额不能大于单次存入最大金额
				if (CommUtil.compare(miniam, maxiam) > 0) {
					throw DpModuleError.DpstComm
							.BNAS2025();
				}
			}
		}
	}

	/**
	 * 
	 * @Title: cktimewy
	 * @Description: 存入控制方法为次数控制时检查
	 * @param timewy
	 *            存入次数控制方式
	 * @param minitm
	 *            最小存入次数
	 * @param maxitm
	 *            最大存入次数
	 * @author zhangjunlei
	 * @date 2016年7月13日 下午4:11:48
	 * @version V2.3.0
	 */
	public static void cktimewy(E_TIMEWY timewy, Long minitm, Long maxitm) {

		// 存入次数控制方式不能为空
		if (CommUtil.isNull(timewy)) {
			throw DpModuleError.DpstComm.BNAS2026();
		} else if (CommUtil.isNotNull(timewy)) {// 存入金额控制方式不为空

			if (timewy == E_TIMEWY.MNTM) {// 存入次数控制方式为控制最小次数

				// 最小存入次数不能为空
				if (CommUtil.isNull(minitm)) {
					throw DpModuleError.DpstComm.BNAS2027();
				}
				
				if(minitm < 0){
					throw DpModuleError.DpstComm.BNAS2028();
				}

				// 最大存入次数应为空
				if (CommUtil.isNotNull(maxitm)) {
					throw DpModuleError.DpstComm.BNAS2029();
				}

			} else if (timewy == E_TIMEWY.MXTM) {// 存入次数控制方式为控制最大次数

				// 最大存入次数不能为空
				if (CommUtil.isNull(maxitm)) {
					throw DpModuleError.DpstComm.BNAS2030();
				}
				
				if(maxitm <0){
					throw DpModuleError.DpstComm.BNAS2031();
				}

				// 最小存入次数应为空
				if (CommUtil.isNotNull(minitm)) {
					throw DpModuleError.DpstComm.BNAS2032();
				}

			} else if (timewy == E_TIMEWY.SCTM) {// 存入金额控制方式为控制次数范围

				// 最小存入次数不能为空
				if (CommUtil.isNull(minitm)) {
					throw DpModuleError.DpstComm.BNAS2033();
				}
				
				if(minitm < 0){
					throw DpModuleError.DpstComm.BNAS2034();
				}

				// 最大存入次数不能为空
				if (CommUtil.isNull(maxitm)) {
					throw DpModuleError.DpstComm.BNAS2035();
				}
				
				if(maxitm < 0){
					throw DpModuleError.DpstComm.BNAS2036();
				}

				// 最小存入次数不能大于最大存入次数
				if (CommUtil.compare(minitm, maxitm) > 0) {
					throw DpModuleError.DpstComm
							.BNAS2037();
				}
			}
		}
	}
}
