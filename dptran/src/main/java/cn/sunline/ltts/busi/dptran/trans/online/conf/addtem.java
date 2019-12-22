package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupMode;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupModeDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupModePart;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupModePartDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupPart;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupPartDao;
import cn.sunline.ltts.busi.dp.type.DpProdType.kupModeinsInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_MODEST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;


public class addtem {

	public static void instem( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Addtem.Input input, 
						final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Addtem.Output output){
		//操作网点权限验证
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();// 交易机构号
		
		String modena = input.getModena(); //模板名称
		
		E_BUSIBI busibi = input.getBusibi();//业务大类
		
		String prodbi = "";//产品大类
		
		//判断输入值是否为空
		if(CommUtil.isNull(modena)){
			throw DpModuleError.DpstComm.BNAS1945();
		}
		
		if(CommUtil.isNull(busibi)){
			throw DpModuleError.DpstComm.BNAS1946();
		}
		
		if (E_BUSIBI.DEPO == busibi) {
			prodbi = "01";
		} else if (E_BUSIBI.INNE == busibi){
			prodbi = "99";
		} else {
			throw DpModuleError.DpstComm.BNAS1947();
		}
			
		//根据业务大类和模板名称来判断产品模板表中是否存在该记录
		KupMode mode = DpProductDao.selKupMode(busibi, modena, false);
		
		if(CommUtil.isNotNull(mode)){
			throw DpModuleError.DpstComm.BNAS1948();
		}
		
		//自动生成模板编号
		String modeno = DpPublic.getModeno(prodbi, brchno);		
		//产品模板表插入新纪录
		KupMode entity = SysUtil.getInstance(KupMode.class);
		entity.setModeno(modeno);//模板编号
		entity.setBusibi(busibi);//业务大类		
		entity.setModena(modena);//模板名称		
		entity.setModest(E_MODEST.NORMAL);//模板状态
		KupModeDao.insert(entity);
		
		//获取多条记录
		List<kupModeinsInfo> infos = input.getInsModes();
		// 获取部件定义表信息
		List<KupPart> cplKupPart = KupPartDao.selectAll_odb2(busibi, true);
		//循环新增模板部件信息
		if(infos.size()== cplKupPart.size()){
			if(E_BUSIBI.DEPO == busibi){	
				for(KupPart KupPart : cplKupPart){
					int i=0;
					//判断输入的记录是否在部件定义表中存在
					for(kupModeinsInfo info: infos){
						if(KupPart.getPartcd()==info.getPartcd()){
							i++;
							E_PARTCD partcd = info.getPartcd(); //部件编号
							E_YES___ partfg = info.getPartfg();//部件启用标志
							
							if(CommUtil.isNull(partcd)){
								throw DpModuleError.DpstComm.BNAS1949();
							}
							
							if(CommUtil.isNull(partfg)){
								throw DpModuleError.DpstComm.BNAS1950();
							}
							
							//检查部件编号为CK01的部件启用标志是否为是
							if(partcd ==E_PARTCD._CK01){
								if(partfg != E_YES___.YES){
									throw DpModuleError.DpstComm.BNAS1951();
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
								    throw DpModuleError.DpstComm.E9999("存款产品利息利率控制部件必须选择");
								}
							}
							if (E_PARTCD._CK11 == info.getPartcd()) {
								if (E_YES___.NO == info.getPartfg()) {
									throw DpModuleError.DpstComm.BNAS1956();
								}
							}
							
							//查询部件定义表，获取部件名称
							KupPart kupart = KupPartDao.selectOne_odb1(busibi, partcd, true);
							String partna = kupart.getPartna();//部件名称
							//判断部件名称是否存在
							if(CommUtil.isNull(partna)){
								DpModuleError.DpstComm.BNAS1957();
							}
							
//							//校验部件编号是否与部件定义表的部件编号一致
//							if(partcd != KupPart.getPartcd()){
//								throw DpModuleError.DpstComm.E9999("传入的部件编号与部件定义表中不一致");
//							}
							
							//模板部件表插入新记录
							KupModePart modepart = SysUtil.getInstance(KupModePart.class);
							modepart.setBusibi(busibi);
							modepart.setModeno(modeno);
							modepart.setPartcd(partcd);
							modepart.setPartna(partna);
							modepart.setPartfg(partfg);
							KupModePartDao.insert(modepart);
							break;
						}
					}
					if(i==0){
						throw DpModuleError.DpstComm.BNAS1958();
					}	
				}
			}else if(E_BUSIBI.INNE == busibi){
				
					for(kupModeinsInfo info: infos){
						E_PARTCD partcd = info.getPartcd(); //部件编号
						E_YES___ partfg = info.getPartfg();//部件启用标志
						
						if(CommUtil.isNull(partcd)){
							DpModuleError.DpstComm.BNAS1949();
						}
						
						if(CommUtil.isNull(partfg)){
							DpModuleError.DpstComm.BNAS1950();
						}
						
						if(partcd ==E_PARTCD._NB01){
							if(partfg != E_YES___.YES){
								throw DpModuleError.DpstComm.BNAS1959();
							}
						}else{
							throw DpModuleError.DpstComm.BNAS1959();
						}
						
						//查询部件定义表，获取部件名称
						KupPart KupPart = KupPartDao.selectOne_odb1(busibi, partcd, false);
						String partna = KupPart.getPartna();//部件名称
						//判断部件名称是否存在
						if(CommUtil.isNull(partna)){
							DpModuleError.DpstComm.BNAS1957();
						}
						
						//模板部件表插入新记录
						KupModePart modepart = SysUtil.getInstance(KupModePart.class);
						modepart.setBusibi(busibi);
						modepart.setModeno(modeno);
						modepart.setPartcd(partcd);
						modepart.setPartna(partna);
						modepart.setPartfg(partfg);
						
						KupModePartDao.insert(modepart);
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1960();
			}
		}else{
			throw DpModuleError.DpstComm.BNAS1958();
		}
		output.setModeno(modeno);
	}
}
