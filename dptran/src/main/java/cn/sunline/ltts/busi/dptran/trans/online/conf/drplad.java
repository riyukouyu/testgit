package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawPlan;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawPlanDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrplTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrplTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DWBKDL;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DWBKLI;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVPLFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVPLGN;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BEINFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;

public class drplad {

	/**
	 * 
	 * @Title: drpadd
	 * @Description: 新增存款支取计划控制部件信息
	 * @param input
	 * @author zhangjunlei
	 * @date 2016年7月14日 上午11:15:33
	 * @version V2.3.0
	 */
	public static void drpadd(
			final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Drplad.Input input) {

		String prodcd = input.getProdcd();// 产品编号
		E_SVPLGN dradwy = input.getDradwy();// 支取计划生成方式
		String gendpd = input.getGendpd();// 支取计划生成周期
		E_SVPLFG drcrwy = input.getDrcrwy();// 支取计划控制方式
		E_DWBKLI drdfsd = input.getDrdfsd();// 支取违约标准
		E_DWBKDL drdfwy = input.getDrdfwy();// 支取违约处理方式
		E_BEINFG beinfg = input.getBeinfg();// 支取时结息处理标志

		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());// 操作网点权限验证

		// 产品编号
		if (CommUtil.isNull(prodcd)) {
			throw DpModuleError.DpstProd.BNAS1054();
		}
		
		// 支取计划生成周期是否合法
		if (CommUtil.isNotNull(gendpd)) {
			if (!DateTools2.chkFrequence(gendpd)) {
				throw DpModuleError.DpstComm.BNAS2158();
			}

		}
				
		// 支取计划生成方式
		if (CommUtil.isNull(dradwy)) {
			throw DpModuleError.DpstComm.BNAS0128();
		} else {

			// 当支取计划生成方式定义为按周期均分时，支取计划生成周期必输
			if (dradwy == E_SVPLGN.T1) {
				if (CommUtil.isNull(gendpd)) {
					throw DpModuleError.DpstComm
							.BNAS2159();
				}

				// 当支取计划生成方式定义为按计划分时，支取计划生成周期必空
			} else if (dradwy == E_SVPLGN.T2) {
				if (CommUtil.isNotNull(gendpd)) {
					throw DpModuleError.DpstComm
							.BNAS2160();
				}
			}
		}

		// 产品计划控制方式
		if (CommUtil.isNull(drcrwy)) {
			throw DpModuleError.DpstComm.BNAS2161();
		}

		// 支取违约标准
		if (CommUtil.isNull(drdfsd)) {
			throw DpModuleError.DpstComm.BNAS0116();
		}

		// 支取违约处理方式
		if (CommUtil.isNull(drdfwy)) {
			throw DpModuleError.DpstComm.BNAS0114();
		}

		// 支取时结息处理方式
		if (CommUtil.isNull(beinfg)) {
			throw DpModuleError.DpstComm.BNAS2162();
		}

		// 产品基础属性表
		List<KupDppb> tblDppb = KupDppbDao.selectAll_odb4(prodcd, false);

		if (tblDppb.size() > 0) {
			throw DpModuleError.DpstComm.BNAS1996();
		}

		// 存款产品部件临时表
		KupDppbPartTemp tblPartTemp = KupDppbPartTempDao
				.selectFirst_odb3(prodcd, false);
		if (CommUtil.isNull(tblPartTemp)) {
			throw DpModuleError.DpstComm.BNAS1997();
		}

		// 产品支取计划控制信息
		KupDppbDrawPlan tblDppb_draw_plan = KupDppbDrawPlanDao
				.selectOne_odb2(prodcd, false);

		if (CommUtil.isNotNull(tblDppb_draw_plan)) {
			throw DpModuleError.DpstComm.BNAS2163();
		}

		// 产品基础属性临时表
		KupDppbTemp tblDppb_temp = KupDppbTempDao.selectOne_odb1(prodcd,
				false);

		if (CommUtil.isNull(tblDppb_temp)) {
			throw DpModuleError.DpstComm.BNAS1999();
		}

		String crcycd = tblDppb_temp.getPdcrcy();// 产品适用币种

		// 产品支取计划控制临时表
		KupDppbDrplTemp tblDppb_drpl_temp = SysUtil.getInstance(KupDppbDrplTemp.class);

		if (CommUtil.isNotNull(KupDppbDrplTempDao.selectOne_odb1(prodcd,
				crcycd, false))) {
			throw DpModuleError.DpstComm.BNAS2164();
		}

		// 存款产品部件临时表
		KupDppbPartTemp tblDppb_part_temp = KupDppbPartTempDao
				.selectOne_odb1(E_BUSIBI.DEPO, prodcd, E_PARTCD._CK07, false);

		if (CommUtil.isNull(tblDppb_part_temp)) {
			throw DpModuleError.DpstComm.BNAS2001();
		}

		// 判断存入控制部件是否启用
		if (tblDppb_part_temp.getPartfg() == E_YES___.NO) {
			throw DpModuleError.DpstComm.BNAS2002();
		} else {
			
			tblDppb_drpl_temp.setProdcd(prodcd);// 产品编号
			tblDppb_drpl_temp.setCrcycd(crcycd);// 币种
			tblDppb_drpl_temp.setDradwy(dradwy);// 支取计划生成方式
			tblDppb_drpl_temp.setGendpd(gendpd);// 支取计划生成周期
			tblDppb_drpl_temp.setDrcrwy(drcrwy);// 支取计划控制方式
			tblDppb_drpl_temp.setDrdfsd(drdfsd);// 支取违约标准
			tblDppb_drpl_temp.setDrdfwy(drdfwy);// 支取违约处理方式
			tblDppb_drpl_temp.setBeinfg(beinfg);// 支取时结息处理标志

			KupDppbDrplTempDao.insert(tblDppb_drpl_temp);// 新增存款支取计划控制部件信息
		}
	}
}
