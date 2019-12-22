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

public class prdsto {

	public static void prdsto(final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Prdsto.Input input, final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Prdsto.Output output) {
		
		// 操作网点权限验证
		//DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());

		// 获取输入项信息
		E_BUSIBI busibi = input.getBusibi();// 业务大类
		String prodcd = input.getProdcd();// 产品编号
		String inefdt = input.getInefdt();// 停用日期
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期

		// 输入项非空检查
		if (CommUtil.isNull(busibi)) {
			throw DpModuleError.DpstComm.BNAS1946();
		}
//		if (E_BUSIBI.DEPO != busibi) {
//			throw DpModuleError.DpstComm.E9999("业务大类必须为存款");
//		}

		if (CommUtil.isNull(prodcd)) {
			throw DpModuleError.DpstComm.BNAS2182();
		}

		if (CommUtil.isNull(inefdt)) {
			throw DpModuleError.DpstComm.BNAS2196();
		}

		
		
		// 存款产品启用
		if (E_BUSIBI.DEPO == busibi) {
			
			// 停用日期
			if (CommUtil.compare(inefdt, trandt) < 0) {
				throw DpModuleError.DpstComm.BNAS2205();
			}
			
			// 获取产品基础属性信息
			KupDppb tblKupDppb = KupDppbDao.selectOne_odb1(prodcd, false);
			if (CommUtil.isNull(tblKupDppb)) {
				
				throw DpModuleError.DpstComm.BNAS2206();
			}
			
			// 只有产品状态为装配生效或装配启用才可做产品停用
			if (E_PRODST.EFFE != tblKupDppb.getProdst() && E_PRODST.NORMAL != tblKupDppb.getProdst()) {
				
				throw DpModuleError.DpstComm.BNAS2207();
			}
			
			// 获取产品适用机构
			List<KupDppbBrch> tblKupDppbBrchs = DpProductDao.selKupDppbBrchByprodcd(prodcd, false);
	
			// 检查产品启用日期和停用日期
			for (KupDppbBrch tblKupDppbBrch : tblKupDppbBrchs) {
	
				// 调整后产品停用日期大于等于适用机构停用日期
				if (CommUtil.compare(inefdt, tblKupDppbBrch.getInefdt()) < 0) {
	
					throw DpModuleError.DpstComm.BNAS2200();
				}
				
			}
			
			// 停用日期为当天日期的即时停用
			if (CommUtil.compare(inefdt, trandt) == 0) {
				
				tblKupDppb.setProdst(E_PRODST.DISA);// 产品状态
			}
			
			// 更新产品基础属性表
			tblKupDppb.setInefdt(inefdt);
			KupDppbDao.updateOne_odb1(tblKupDppb);
			
			// 产品操作柜员登记
			SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.DEPO, prodcd, E_PRTRTP.STOP);
			
		} else if (E_BUSIBI.INNE == busibi) {
			String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
			
			// 停用日期
			if (CommUtil.compare(inefdt, trandt) != 0) {
				throw DpModuleError.DpstComm.BNAS2208();
			}
			
			// 获取内部户产品信息
			IoKnpBusi cplIoKnpBusi = DpProductDao.selGlKnpBusiByBusino(prodcd,corpno, false);
			if (CommUtil.isNull(cplIoKnpBusi)) {
				throw DpModuleError.DpstComm.BNAS2202();
			}
			
			// 只有产品状态为装配生效或装配停用才可做产品启用
			if (E_PRODST.EFFE != cplIoKnpBusi.getBusist() && E_PRODST.NORMAL != cplIoKnpBusi.getBusist()) {
				
				throw DpModuleError.DpstComm.BNAS2209();
			}
			
			// 检查是否存在状态正常的内部户账户
			int cnt = DpProductDao.selCntGlKnpBusiByBusino(prodcd, false);
			if (cnt >0) {
				throw DpModuleError.DpstComm.BNAS2210();
			}
			String timetm = DateTools2.getCurrentTimestamp();
			// 内部户产品停用操作
			int count = DpProductDao.updGlKnpBusiDisable(prodcd, inefdt,timetm,corpno);
			
			// 更新结果确认
			if (count != 1) {
				throw DpModuleError.DpstComm.BNAS2211();
			}
						
			// 产品操作柜员登记
			SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.INNE, prodcd, E_PRTRTP.STOP);
			
		} else {
			
			throw DpModuleError.DpstComm.BNAS2204();
		}
		
		
	}
}
