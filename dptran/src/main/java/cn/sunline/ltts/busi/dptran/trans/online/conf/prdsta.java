package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrch;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoKnpBusi;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRTRTP;

public class prdsta {

	public static void prdsta(final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Prdsta.Input input, final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Prdsta.Output output) {
		
		// 获取输入项信息
		E_BUSIBI busibi = input.getBusibi();//业务大类
		String prodcd = input.getProdcd();//产品编号
		String efctdt = input.getEfctdt();//启用日期
		String inefdt = input.getInefdt();//停用日期
	//	String mtdate = CommTools.getBaseRunEnvs().getTrxn_date();
		// 当前时间戳
		String timetm = DateTools2.getCurrentTimestamp();
		//操作网点权限验证
		//DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
			
		// 输入项非空检查
		if(CommUtil.isNull(busibi)){
			throw DpModuleError.DpstComm.BNAS1946();
		}
		
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
			throw DpModuleError.DpstComm.BNAS2100();
		}
		
		// 存款产品启用
		if (E_BUSIBI.DEPO == busibi) {
			// 获取产品基础属性信息
			KupDppb tblKupDppb = KupDppbDao.selectOne_odb1(prodcd, false);
			if (CommUtil.isNull(tblKupDppb)) {

				throw DpModuleError.DpstComm.BNAS2198();
			}
		
			// 只有产品状态为装配生效或装配停用才可做产品启用
			if (E_PRODST.EFFE == tblKupDppb.getProdst()) {

				// 获取产品适用机构
				List<KupDppbBrch> tblKupDppbBrchs = DpProductDao.selKupDppbBrchByprodcd(prodcd, false);

				// 检查产品启用日期和停用日期
				for (KupDppbBrch tblKupDppbBrch : tblKupDppbBrchs) {

					// 调整后产品启用日期必须小于等于适用机构启用日期
					if (CommUtil.compare(efctdt, tblKupDppbBrch.getEfctdt()) > 0) {

						throw DpModuleError.DpstComm.BNAS2199();
					}

					// 调整后产品停用日期大于等于适用机构停用日期
					if (CommUtil.compare(inefdt, tblKupDppbBrch.getEfctdt()) < 0) {

						throw DpModuleError.DpstComm.BNAS2200();
					}

				}
				
			} else if (E_PRODST.DISA == tblKupDppb.getProdst()) {
				
				
				
				// 装配停用产品启用，初始化适用机构启用和停用日期
				DpProductDao.updBrchByprodcd(prodcd, efctdt, inefdt, timetm);
				
			} else {

				throw DpModuleError.DpstComm.BNAS2201();
			}

			// 更新产品生效和失效日期
			tblKupDppb.setEfctdt(efctdt);// 生效日期
			tblKupDppb.setInefdt(inefdt);// 失效日期
			tblKupDppb.setProdst(E_PRODST.EFFE);// 产品状态
	
			KupDppbDao.updateOne_odb1(tblKupDppb);
			
			// 产品操作柜员登记
			SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.DEPO, prodcd, E_PRTRTP.STAR);
			
		// 内部户产品	
		} else if (E_BUSIBI.INNE == busibi) {
			// 获取内部户产品信息
			String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
			IoKnpBusi cplIoKnpBusi = DpProductDao.selGlKnpBusiByBusino(prodcd,corpno, false);
			if (CommUtil.isNull(cplIoKnpBusi)) {
				throw DpModuleError.DpstComm.BNAS2202();
			}
			
			// 只有产品状态为装配生效或装配停用才可做产品启用
			if (E_PRODST.EFFE != cplIoKnpBusi.getBusist() && E_PRODST.DISA != cplIoKnpBusi.getBusist()) {
				
				throw DpModuleError.DpstComm.BNAS2201();
			}
			// 内部户产品启用操作
			int count = DpProductDao.updGlKnpBusiEnable(prodcd, efctdt, inefdt,E_PRODST.EFFE,timetm,corpno);
			
			// 更新结果确认
			if (count != 1) {
				throw DpModuleError.DpstComm.BNAS2203();
			}
			
			// 产品操作柜员登记
			SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.INNE, prodcd, E_PRTRTP.STAR);
			
		} else {
			
			throw DpModuleError.DpstComm.BNAS2204();
		}
		
	}
}
