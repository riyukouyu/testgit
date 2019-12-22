package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupMode;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupModeDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupModePart;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupModePartDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupPart;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupPartDao;
import cn.sunline.ltts.busi.dp.type.DpProdType.kupModeupdInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_MODEST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;


public class updtem {

	public static void updtem( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Updtem.Input input){
			//操作网点权限验证
			DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
				
			E_BUSIBI busibi = input.getBusibi();//业务大类
			String modena = input.getModena();//模板名称
			String modeno = input.getModeno();//模板编号
				
			if(CommUtil.isNull(busibi)){
				DpModuleError.DpstComm.BNAS1946();
			}
				
			if(CommUtil.isNull(modena)){
				DpModuleError.DpstComm.BNAS1945();
			}
				
			if(CommUtil.isNull(modeno)){
				DpModuleError.DpstComm.BNAS1982();
			}
				
			//查询模板表是否存在记录
			KupMode mode = KupModeDao.selectOne_odb2(busibi, modeno, false);
			if(CommUtil.isNull(mode)||E_MODEST.DELETE == mode.getModest()){
				throw DpModuleError.DpstComm.BNAS2230();
			}
			
			if(!CommUtil.equals(mode.getModena(), modena)){
				//判断修改的名称在记录中是否存在
				KupMode mode1 = KupModeDao.selectOne_odb1(busibi, modena, false);
				
				if(CommUtil.isNotNull(mode1) && E_MODEST.NORMAL == mode1.getModest()){
					throw DpModuleError.DpstComm.BNAS2231();
				}
//				if(CommUtil.isNotNull(mode1)){
//					throw DpModuleError.DpstComm.E9999("该模板名称已存在");
//				}
			}
			
			//更新模板表记录
			mode.setModena(modena);
			KupModeDao.updateOne_odb2(mode);
				
			//获取传入的模板部件表字段
			List<kupModeupdInfo> updInfos = input.getUpdModes();
				
			for(kupModeupdInfo updInfo : updInfos){
				E_PARTCD partcd = updInfo.getPartcd();//部件编号
				E_YES___ partfg = updInfo.getPartfg();//部件启用标志
				
				if(CommUtil.isNull(partcd)){
					DpModuleError.DpstComm.BNAS1949();
				}
				
				if(CommUtil.isNull(partfg)){
					DpModuleError.DpstComm.BNAS1950();
				}
				
				//校验部件编号是否与部件定义表的部件编号一致
				KupPart KupPart = KupPartDao.selectOne_odb1(busibi, partcd, false);
				if(partcd != KupPart.getPartcd()){
					throw DpModuleError.DpstComm.BNAS2232();
				}

				//查询模板部件表是否存在记录	
				KupModePart KupModePart = KupModePartDao.selectOne_odb1(busibi, modeno, partcd, false);
				if(CommUtil.isNull(KupModePart)){
					throw DpModuleError.DpstComm.BNAS2233();
				}
				
				//检查若部件编号为CK01的部件启用标志是否为是
				if(partcd == E_PARTCD._CK01){
					if(partfg != E_YES___.YES){
						throw DpModuleError.DpstComm.BNAS1951();
					}
				}
				if (E_PARTCD._CK03 == partcd) {
					if (E_YES___.NO == partfg) {
						throw DpModuleError.DpstComm.BNAS1952();
					}
				}
				if (E_PARTCD._CK04 == partcd) {
					if (E_YES___.NO == partfg) {
						throw DpModuleError.DpstComm.BNAS1953();
					}
				}
				if (E_PARTCD._CK06 == partcd) {
					if (E_YES___.NO == partfg) {
						throw DpModuleError.DpstComm.BNAS1954();
					}
				}
				if (E_PARTCD._CK09 == partcd) {
					if (E_YES___.NO == partfg) {
						throw DpModuleError.DpstComm.BNAS1955();
					}
				}
				if (E_PARTCD._CK11 == partcd) {
					if (E_YES___.NO == partfg) {
						throw DpModuleError.DpstComm.BNAS1956();
					}
				}
				
				//更新模板部件表记录
				KupModePart.setPartfg(partfg);
				KupModePartDao.updateOne_odb1(KupModePart);
				
		}
	}
}
