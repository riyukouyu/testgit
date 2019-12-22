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

public class depoup {

	/**
	 * 
	 * @Title: depuup
	 * @Description: 修改存款存入控制部件信息
	 * @param input
	 * @author zhangjunlei
	 * @date 2016年7月13日 下午4:45:24
	 * @version V2.3.0
	 */
	public static void depuup(
			final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Depoup.Input input) {

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
					depoad.ckamntwy(amntwy, miniam, maxiam);// 存入控制方法为金额控制时检查

					// 支取次数控制应为空
					if (CommUtil.isNotNull(timewy)
							|| CommUtil.isNotNull(minitm)
							|| CommUtil.isNotNull(maxitm)) {
						throw DpModuleError.DpstComm.BNAS2038();
					}

					// 存入控制方法为次数控制
				} else if (postwy == E_POSTWY.TMCL) {
					depoad.cktimewy(timewy, minitm, maxitm);// 存入控制方法为次数控制时检查

					// 存入金额控制应为空
					if (CommUtil.isNotNull(amntwy)
							|| CommUtil.isNotNull(miniam)
							|| CommUtil.isNotNull(maxiam)) {
						throw DpModuleError.DpstComm.BNAS2039();
					}

					// 存入控制方法为金额和次数控制
				} else if (postwy == E_POSTWY.ATMC) {
					depoad.ckamntwy(amntwy, miniam, maxiam);// 对金额控制检查
					depoad.cktimewy(timewy, minitm, maxitm);// 对次数控制检查
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

		// 产品基础属性临时表
		KupDppbTemp tblDppb_temp = KupDppbTempDao.selectOne_odb1(prodcd,
				false);

		if (CommUtil.isNull(tblDppb_temp)) {
			throw DpModuleError.DpstComm.BNAS1999();
		}
		
		if(tblDppb_temp.getPddpfg() == E_FCFLAG.CURRENT && detlfg != E_YES___.NO){
			throw DpModuleError.DpstComm.BNAS2013();
		}
		
		String crcycd = tblDppb_temp.getPdcrcy();// 产品适用币种

		KupDppbPostTemp tblKup = SysUtil.getInstance(KupDppbPostTemp.class);// 产品存入控制临时表

		// 根据产品编号查询存入控制方式信息是否存在
		tblKup = KupDppbPostTempDao.selectOne_odb1(prodcd, crcycd, false);

		// 该产品存入控制信息不存在，不能修改
		if (CommUtil.isNull(tblKup)) {
			throw DpModuleError.DpstComm.BNAS2040();
		} else {

			// 存款产品部件临时表
			KupDppbPartTemp tblDppb_part_temp = KupDppbPartTempDao
					.selectOne_odb1(E_BUSIBI.DEPO, prodcd, E_PARTCD._CK04,
							false);

			if (CommUtil.isNull(tblDppb_part_temp)) {
				throw DpModuleError.DpstComm.BNAS2001();
			}

			// 判断存入控制部件是否启用
			if (tblDppb_part_temp.getPartfg() == E_YES___.NO) {
				throw DpModuleError.DpstComm.BNAS2002();
			} else {

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

				KupDppbPostTempDao.updateOne_odb1(tblKup);// 修改存入控制部件信息
			}
		}
	}
}
