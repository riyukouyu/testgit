package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupPart;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupPartDao;
import cn.sunline.ltts.busi.dp.type.DpProdType.KupPartInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;


public class prtmad {

	public static void prtmad( cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI busibi,  final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Prtmad.Output output){
		if(CommUtil.isNull(busibi)){
			DpModuleError.DpstComm.BNAS1946();
		}
		
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());//操作网点权限验证
		
		//活期存款产品部件定义信息列表
		List<KupPart> KupPart = KupPartDao.selectAll_odb2(busibi, false);
		for(KupPart info:KupPart){
			
			KupPartInfo infos = SysUtil.getInstance(KupPartInfo.class);
			infos.setBusibi(busibi); //业务大类
			infos.setPartcd(info.getPartcd()); //部件编号
			infos.setPartna(info.getPartna()); //部件名称
			
			output.getPrtInfos().add(infos);
		}
	}
}
