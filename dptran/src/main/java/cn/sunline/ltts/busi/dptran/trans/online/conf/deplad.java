package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPoplTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPoplTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostPlan;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostPlanDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVBKAD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVBKDL;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVBKLI;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVPLFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVPLGN;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PSAMTP;

public class deplad {

	/**
	 * 
	 * @Title: depadd
	 * @Description: 新增存款存入计划控制部件信息
	 * @param input
	 * @author zhangjunlei
	 * @date 2016年7月13日 下午10:05:29
	 * @version V2.3.0
	 */

	public static void depadd(
			final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Deplad.Input input) {

		String prodcd = input.getProdcd();// 产品编号
		E_SVPLGN gentwy = input.getGentwy();// 存入计划生成方式
		String planpd = input.getPlanpd();// 存入计划生成周期
		String svlepd = input.getSvlepd();// 漏存补足宽限期
		E_SVBKAD svlewy = input.getSvlewy();// 存入漏补方式
		Long maxisp = input.getMaxisp();// 最大补足次数
		E_SVBKLI dfltsd = input.getDfltsd();// 存入违约标准
		Long svletm = input.getSvletm();// 漏存次数
		E_SVBKDL dfltwy = input.getDfltwy();// 存入违约处理方式
		E_SVPLFG pscrwy = input.getPscrwy();// 存入计划控制方式
		E_PSAMTP psamtp = input.getPsamtp();// 存入计划金额类型

		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());// 操作网点权限验证

		// 产品编号
		if (CommUtil.isNull(prodcd)) {
			throw DpModuleError.DpstProd.BNAS1054();
		}

		// 存入计划生成方式
		if (CommUtil.isNull(gentwy)) {
			throw DpModuleError.DpstComm.BNAS1014();
		}

		// 漏存补足宽限期
		if (CommUtil.isNotNull(svlepd)) {
			if (!BusiTools.isNum(svlepd)) {
				throw DpModuleError.DpstComm.BNAS1984();
			}
			if (ConvertUtil.toInteger(svlepd) < 0) {
				throw DpModuleError.DpstComm.BNAS1985();
			}
		}
		
		// 存入计划金额类型
		if (CommUtil.isNull(psamtp)) {
			throw DpModuleError.DpstComm.BNAS1019();
		}
		
		// 存入计划生成周期是否合法
		if (CommUtil.isNotNull(planpd)) {
			if (!DateTools2.chkFrequence(planpd)) {
				throw DpModuleError.DpstComm.BNAS1013();
			}

		}


		// 存入计划生成方式按周期分
		if (gentwy == E_SVPLGN.T1) {

			// 当存入计划生成方式按周期分时，存入计划生成周期不能为空
			if (CommUtil.isNull(planpd)) {
				throw DpModuleError.DpstComm.BNAS1986();
			}

			// 存入计划生成方式按计划分
		} else if (gentwy == E_SVPLGN.T2) {

			// 当存入计划生成方式按计划分时，存入计划生成周期应为空
			if (CommUtil.isNotNull(planpd)) {
				throw DpModuleError.DpstComm.BNAS1987();
			}
		}

		// 存入漏补方式
		if (CommUtil.isNull(svlewy)) {
			throw DpModuleError.DpstComm.BNAS1988();
		} else {

			// 存入漏补方式为控制漏补次数时，最大补足次数必输
			if (svlewy == E_SVBKAD.COUNT) {
				if (CommUtil.isNull(maxisp)) {
					throw DpModuleError.DpstComm.BNAS1989();
				}
				if(maxisp < 0){
					throw DpModuleError.DpstComm.BNAS1990();
				}
			} else {

				// 存入漏补方式不为控制漏补次数时，最大补足次数必空
				if (CommUtil.isNotNull(maxisp)) {
					throw DpModuleError.DpstComm.BNAS1991();
				}
			}
		}

		// 存入违约标准
		if (CommUtil.isNull(dfltsd)) {
			throw DpModuleError.DpstComm.BNAS1992();
		} else {

			// 当存入违约标准为大于漏存次数时，漏存次数必输
			if (dfltsd == E_SVBKLI.COUNT) {
				if (CommUtil.isNull(svletm)) {
					throw DpModuleError.DpstComm.BNAS1993();
				}
				if(svletm < 0){
					throw DpModuleError.DpstComm.BNAS1994();
				}
			} else {

				// 当存入违约标准不为大于漏存次数时，漏存次数必空
				if (CommUtil.isNotNull(svletm)) {
					throw DpModuleError.DpstComm.BNAS1995();
				}
			}
		}

		// 存入违约处理方式
		if (CommUtil.isNull(dfltwy)) {
			throw DpModuleError.DpstComm.BNAS1002();
		}

		// 存入计划控制方式
		if (CommUtil.isNull(pscrwy)) {
			throw DpModuleError.DpstComm.BNAS1017();
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
		
		// 产品存入控制计划表
		KupDppbPostPlan tblPopl = KupDppbPostPlanDao.selectOne_odb2(
				prodcd, false);
		if (CommUtil.isNotNull(tblPopl)) {
			throw DpModuleError.DpstComm.BNAS1998();
		}

		// 产品基础属性临时表
		KupDppbTemp tblDppb_temp = KupDppbTempDao.selectOne_odb1(prodcd,
				false);

		if (CommUtil.isNull(tblDppb_temp)) {
			throw DpModuleError.DpstComm.BNAS1999();
		}

		String crcycd = tblDppb_temp.getPdcrcy();// 产品适用币种

		KupDppbPoplTemp tblKup = SysUtil.getInstance(KupDppbPoplTemp.class);// 存入计划控制临时表

		// 判断存入存入计划控制临时表有没有已存在的产品
		if (CommUtil.isNotNull(KupDppbPoplTempDao.selectOne_odb1(prodcd,
				crcycd, false))) {
			throw DpModuleError.DpstComm.BNAS2000();
		}

		// 存款产品部件临时表
		KupDppbPartTemp tblDppb_part_temp = KupDppbPartTempDao
				.selectOne_odb1(E_BUSIBI.DEPO, prodcd, E_PARTCD._CK05, false);

		if (CommUtil.isNull(tblDppb_part_temp)) {
			throw DpModuleError.DpstComm.BNAS2001();
		}

		// 判断存入控制部件是否启用
		if (tblDppb_part_temp.getPartfg() == E_YES___.NO) {
			throw DpModuleError.DpstComm.BNAS2002();
		} else {
			
			tblKup.setProdcd(prodcd);// 产品编号
			tblKup.setCrcycd(crcycd);// 币种
			tblKup.setGentwy(gentwy);// 存入计划生成方式
			tblKup.setPlanpd(planpd);// 存入计划生成周期
			tblKup.setSvlepd(svlepd);// 漏存补足宽限期
			tblKup.setSvlewy(svlewy);// 存入漏补方式
			tblKup.setMaxisp(maxisp);// 最大补足次数
			tblKup.setDfltsd(dfltsd);// 存入违约标准
			tblKup.setSvletm(svletm);// 漏存次数
			tblKup.setDfltwy(dfltwy);// 存入违约处理方式
			tblKup.setPscrwy(pscrwy);// 存入计划控制方式
			tblKup.setPsamtp(psamtp);// 存入计划金额类型

			KupDppbPoplTempDao.insert(tblKup);// 新增存款存入计划控制部件信息
		}
	}
}
