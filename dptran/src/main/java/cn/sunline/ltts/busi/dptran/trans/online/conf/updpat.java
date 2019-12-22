package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrchTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfirTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrplTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbMatuTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPoplTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpInrpat;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_ADDMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRENTP;


public class updpat {

	public static void updpat( final cn.sunline.ltts.busi.dptran.trans.intf.Updpat.Input input){
		
		E_BUSIBI busibi = input.getBusibi(); //业务大类
		String prodcd = input.getProdcd();//产品编号
		String prodtx =input.getProdtx();// 产品名称
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();// 法人代码
		
		List<IoDpInrpat> dpInrpatInfos = input.getUpdPatInfos();// 需修改的产品部件列表信息
		
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());//操作网点权限验证
		
		if(CommUtil.isNull(busibi)){
			
			throw DpModuleError.DpstComm.BNAS1946();
		}
		
		if(busibi != E_BUSIBI.DEPO ){
			throw DpModuleError.DpstComm.BNAS1962();
		}
		
		if(CommUtil.isNull(prodcd)){
			
			throw DpModuleError.DpstProd.BNAS1054();
		}
		
		if (CommUtil.isNull(prodtx)) {

			throw DpModuleError.DpstComm.BNAS2169();
		}
		
		  // 产品名称唯一检查
 		String prodcd1 = DpProductDao.selDppbByProdtx(prodtx, corpno, false);
 		if (CommUtil.isNotNull(prodcd1) && !CommUtil.equals(prodcd, prodcd1)) {
 			throw DpModuleError.DpstComm.BNAS2094();
 		}
		
		// 如果产品名称有变更则更新基础属性信息
		KupDppbTemp tblKupDppbT = KupDppbTempDao.selectOne_odb1(prodcd, true);
		if (!CommUtil.equals(prodtx, tblKupDppbT.getProdtx())) {
			
			tblKupDppbT.setProdtx(prodtx);// 产品名称
			KupDppbTempDao.updateOne_odb1(tblKupDppbT);
			
		}
		
		// 循环修改产品部件信息
		for(IoDpInrpat info : dpInrpatInfos){
			
			E_PARTCD partcd = info.getPartcd();//部件编号
			E_YES___ partfg = info.getPartfg();//启用标志
			
			if(CommUtil.isNull(partcd)){
				
				throw DpModuleError.DpstComm.BNAS1949();
			}
			
			if(CommUtil.isNull(info.getPartfg())){
				
				throw DpModuleError.DpstComm.BNAS1950();
			}
			
			// 获取原产品部件信息
			 KupDppbPartTemp tbltemp = KupDppbPartTempDao.selectOne_odb1(busibi, prodcd, partcd, false);
			
			if (CommUtil.isNull(tbltemp)) {
				
				throw DpModuleError.DpstComm.BNAS2228(""+partcd+"");
			}
			
			//新增模式为模板模式或者复制模式的不能修改
			if(tbltemp.getAddmtp() == E_ADDMTP.COPY || tbltemp.getAddmtp() == E_ADDMTP.MODE){
				
				throw DpModuleError.DpstComm.BNAS2229();
				
			}
			
			if (E_PARTCD._CK02 == info.getPartcd()) {
				if (E_YES___.NO == info.getPartfg()) {
					if (E_PRENTP.ASSE == tbltemp.getPrentp()) {
						throw DpModuleError.DpstComm.BNAS2176();
					}
				} else {
					if (E_PRENTP.SALE == tbltemp.getPrentp()) {
						throw DpModuleError.DpstComm.BNAS2177();
					}
				}
			}
			
			// 必选部件检查
			if (E_PARTCD._CK01 == info.getPartcd()) {
				if (E_YES___.NO == info.getPartfg()) {
					throw DpModuleError.DpstComm.BNAS2178();
				}
			}
			if (E_PARTCD._CK03 == info.getPartcd()) {
				if (E_YES___.NO == info.getPartfg()) {
					throw DpModuleError.DpstComm.BNAS1952();
				}
			}
			if (E_PARTCD._CK04 == info.getPartcd()) {
				if (E_YES___.NO == info.getPartfg()) {
					throw DpModuleError.DpstComm.BNAS1953();
				}
			}
			if (E_PARTCD._CK06 == info.getPartcd()) {
				if (E_YES___.NO == info.getPartfg()) {
					throw DpModuleError.DpstComm.BNAS1954();
				}
			}
			if (E_PARTCD._CK09 == info.getPartcd()) {
				if (E_YES___.NO == info.getPartfg()) {
					throw DpModuleError.DpstComm.BNAS1955();
				}
			}
			if (E_PARTCD._CK11 == info.getPartcd()) {
				if (E_YES___.NO == info.getPartfg()) {
					throw DpModuleError.DpstComm.BNAS1956();
				}
			}
			
			// 不启用部件删除相应数据
			// 机构控制部件
			if (E_PARTCD._CK02 == info.getPartcd()) {
				if (E_YES___.NO == info.getPartfg()) {
					
					KupDppbBrchTempDao.delete_odb4(prodcd);
				}
			}
			
			// 存入计划控制部件
			if (E_PARTCD._CK05 == info.getPartcd()) {
				if (E_YES___.NO == info.getPartfg()) {
					
					KupDppbPoplTempDao.deleteOne_odb2(prodcd);
					
				}
			}
			
			// 支取计划控制部件
			if (E_PARTCD._CK07 == info.getPartcd()) {
				if (E_YES___.NO == info.getPartfg()) {
					
					KupDppbDrplTempDao.deleteOne_odb2(prodcd);
				}
			}
			
			// 到期控制部件
			if (E_PARTCD._CK08 == info.getPartcd()) {
				if (E_YES___.NO == info.getPartfg()) {
				
					KupDppbMatuTempDao.deleteOne_odb2(prodcd);
				}
			}
			
			// 违约部件
			if (E_PARTCD._CK10 == info.getPartcd()) {
				if (E_YES___.NO == info.getPartfg()) {
					
					KupDppbDfirTempDao.delete_odb4(prodcd);
				}
			}
			
			// 有修改启用的标志的更新数据
			if (partfg != tbltemp.getPartfg()) {
				
				tbltemp.setPartfg(partfg);
				KupDppbPartTempDao.updateOne_odb1(tbltemp);
				
			}
			
		}	
		
	}
	
}
