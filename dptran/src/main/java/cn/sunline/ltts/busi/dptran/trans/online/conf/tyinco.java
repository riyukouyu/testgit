package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbUser;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;


public class tyinco {

	/**
	 * 存款产品复核录入编号确认
	 * 
	 * @param prodcd
	 *            产品编号
	 * @param tyinno
	 *            录入编号
	 * @return userid 新增产品操作柜员
	 */
	public static void prcTyinco( String prodcd,  String tyinno,  final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Tyinco.Output output){
		
		// 权限验证，省级机构才有操作权限
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
		
		// 输入项非空检查
		if (CommUtil.isNull(prodcd)) {
			throw DpModuleError.DpstProd.BNAS1054();
		}
		if (CommUtil.isNull(tyinno)) {
			throw DpModuleError.DpstComm.BNAS1397();
		}
		
		// 获取产品信息
		KupDppbTemp tblkup_dppb = KupDppbTempDao.selectOne_odb1(prodcd, false);
		if (CommUtil.isNull(tblkup_dppb)) {
			throw DpModuleError.DpstProd.BNAS1329();
		}
		
		// 检查产品状态是否为待复核的状态
		if (E_PRODST.ASSE != tblkup_dppb.getProdst()) {
			throw DpModuleError.DpstComm.BNAS2227();
		}
		
		// 判断录入编号是否正确
		if (!CommUtil.equals(tyinno, tblkup_dppb.getTyinno())) {
			throw DpModuleError.DpstComm.BNAS1802();
		}
		
		KupDppbUser tblkup_dppbU = DpProductDao.selProdcdUserid(prodcd, true);
		output.setUserid(tblkup_dppbU.getTranus());// 交易柜员
	}

}
