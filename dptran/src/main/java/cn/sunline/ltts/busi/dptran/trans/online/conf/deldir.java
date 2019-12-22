package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;


public class deldir {
	public static void dirdel( String cataid){
		//操作网点权限验证
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
		
		//输入值检查
		if(CommUtil.isNull(cataid)){
			DpModuleError.DpstComm.BNAS1981();
		}
		
		/*//检查产品目录编号是否在表中存在
		kup_cata cata = Kup_cataDao.selectOne_kup_cata_idx1(cataid, false);
		if(CommUtil.isNull(cata)){
			throw DpModuleError.DpstComm.E9999("产品目录编号在表中不存在");
		}
		
		//检查是否有下级目录
		List<kup_cata> infos = DpProductDao.selKupCataInfobydireid(cataid, false);
		if(infos.size()>1){
			throw DpModuleError.DpstComm.E9999("该产品目录存在下级目录");
		}
		
		//检查是否有基础产品
		List<KupDppb> listInfo = DpProductDao.selKupDppbbyprodcd(cataid, false);
		if(listInfo.size()>=1){
			throw DpModuleError.DpstComm.E9999("该产品目录存在基础产品");
		}
		
		//删除产品目录
		Kup_cataDao.deleteOne_kup_cata_idx1(cataid);*/
	}
}
