package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrch;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrchDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpSelKupDppbBrchIn;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BRCHFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRTRTP;
import cn.sunline.edsp.base.lang.Options;

public class stadtu {

	public static void stadtu( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Stadtu.Input input, final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Stadtu.Output output) {

		// 获取输入项信息
		String prodcd = input.getProdcd();//产品编号
		String efctdt = input.getEfctdt();//启用日期
		String inefdt = input.getInefdt();//停用日期

		//操作网点权限验证
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
			
		if(CommUtil.isNull(prodcd)){
			throw DpModuleError.DpstProd.BNAS1054();
		}
		
		if(CommUtil.isNull(efctdt)){
			throw DpModuleError.DpstComm.BNAS2195();
		}
		
		if(CommUtil.isNull(inefdt)){
			throw DpModuleError.DpstComm.BNAS2196();
		}
		
		if(CommUtil.compare(efctdt, CommTools.getBaseRunEnvs().getTrxn_date()) <= 0){
			throw DpModuleError.DpstComm.BNAS2197();
		}
		
		if(CommUtil.compare(efctdt, inefdt) >= 0){
			throw DpModuleError.DpstComm.BNAS2219();
		}
		
		Options<IoDpSelKupDppbBrchIn> dpSelKupDppbBrchInList= input.getBrchInfos();
		
		if(CommUtil.isNull(dpSelKupDppbBrchInList) || dpSelKupDppbBrchInList.size() == 0){
			throw DpModuleError.DpstComm.BNAS2220();
		}
		
		KupDppb tblKupDppb = KupDppbDao.selectOne_odb1(prodcd, false);
		if (CommUtil.isNull(tblKupDppb)) {
			throw DpModuleError.DpstComm.BNAS0761();
		}
		
		// 检查机构控制标志，只有为适用机构时才可以做适用机构启用日期调整
		if (E_BRCHFG.USE != tblKupDppb.getBrchfg() && CommUtil.isNotNull(tblKupDppb.getBrchfg())) {
			throw DpModuleError.DpstComm.BNAS2221();
		}
		
		// 存款产品适用机构启用日期调整
		for(IoDpSelKupDppbBrchIn brchnoInfo : dpSelKupDppbBrchInList){
			
			String brchno =  brchnoInfo.getBrchno();// 机构号
			
			KupDppbBrch tblKupDppbBrch = DpProductDao.selKupDppbBrchByBrchno(prodcd, brchno, false); 
			
			if(CommUtil.isNull(tblKupDppbBrch)){
				throw DpModuleError.DpstComm.BNAS2222();
			}
			
			if(CommUtil.compare(efctdt, tblKupDppb.getEfctdt()) < 0){
				throw DpModuleError.DpstComm.BNAS2223();
			}
			
			if(CommUtil.compare(inefdt, tblKupDppb.getInefdt()) > 0){
				throw DpModuleError.DpstComm.BNAS2224();
			}
			
			tblKupDppbBrch.setEfctdt(efctdt);
			tblKupDppbBrch.setInefdt(inefdt);
			
			KupDppbBrchDao.updateOne_odb6(tblKupDppbBrch);
			
		}
		// 产品操作柜员登记
		SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.DEPO, prodcd, E_PRTRTP.STDT);
	}
}
