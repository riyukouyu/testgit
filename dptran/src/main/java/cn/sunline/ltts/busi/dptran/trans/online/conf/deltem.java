package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupMode;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupModeDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_MODEST;


public class deltem {

	public static void deltem( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Deltem.Input input){
		
		//操作网点权限验证
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());

		E_BUSIBI busibi = input.getBusibi();//业务大类
		String modeno = input.getModeno();  //模板编号
		
		//传入值检查
		if(CommUtil.isNull(busibi)){
			DpModuleError.DpstComm.BNAS1946();
		}
		
		if(CommUtil.isNull(modeno)){
			DpModuleError.DpstComm.BNAS1982();
		}
		
		//判断记录是否存在
		KupMode mode = KupModeDao.selectOne_odb2(busibi, modeno, false);
		if(CommUtil.isNull(mode)||E_MODEST.DELETE == mode.getModest()){
			throw DpModuleError.DpstComm.BNAS1983();
		}
		
		//设置模板状态为删除并更新模板表
		mode.setModest(E_MODEST.DELETE);
		KupModeDao.updateOne_odb2(mode);
	}
}
