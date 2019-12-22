package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
/**
 * 
 * @ClassName: cominp 
 * @Description: (存款产品录入提交) 
 * @author leipeng
 * @date 2016年7月18日 下午2:50:22 
 *
 */

public class cominp {

private static BizLog log = BizLogUtil.getBizLog(cominp.class);

/**
 * 
 * @Title: updinp 
 * @Description: (录入前先校验所有产品表的信息) 
 * @param input
 * @author leipeng
 * @date 2016年7月18日 下午2:51:47 
 * @version V2.3.0
 */
public static void updinp( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Cominp.Input input,  final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Cominp.Output output){
	
		// 校验机构只有省级机构才能操作
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
	//	String mtdate =CommTools.getBaseRunEnvs().getTrxn_date();
		String timetm =DateTools2.getCurrentTimestamp();
		// 输入项非空检查
		if (CommUtil.isNull(input.getBusibi())) {
			
			throw DpModuleError.DpstComm.BNAS1961();
		}
		if (CommUtil.isNull(input.getProdcd())) {

			throw DpModuleError.DpstProd.BNAS1054();
		}
		
		// 业务大类必须为存款
		if (E_BUSIBI.DEPO != input.getBusibi()) {
			
			throw DpModuleError.DpstComm.BNAS1962();
		}
		
		log.debug("<<<<<<<<<<<<<<<<" + "更新产品基础属性表产品状态" + ">>>>>>>>>>>>>>>>");
		// 获取产品信息
		KupDppbTemp seltemp = KupDppbTempDao.selectOne_odb1(input.getProdcd(), false);
		if (CommUtil.isNull(seltemp)) {

			throw DpModuleError.DpstComm.BNAS0761();
		}
		
		// 产品录入校验
		DpPublic.chkProdInfos(input.getProdcd(), "0");// 录入校验
		
		// 产品状态为产品装配
		seltemp.setProdst(E_PRODST.ASSE);
		String tyinno = "";// 录入编号
		// 第一次录入提交
		if (CommUtil.isNull(seltemp.getTyinno())) {

			// 录入编号5位录入编号（机构号3位+年份日期8位+序号4位t）
			tyinno = DpPublic.getProdTyinno();
			seltemp.setTyinno(tyinno);
		
	    // 第二次以后不重新产生录入编号
		} else {
			
			tyinno = seltemp.getTyinno();
		}
		
		// 更新基础属性临时表
		DpProductDao.updKupDppbTempProdst(input.getProdcd(), tyinno, E_PRODST.ASSE, null,timetm);
		
		// 输出录入编号
		output.setTyinno(tyinno);
	}


	
}
