package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.type.DpProdType.KupQrydirTree;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_LECODE;


public class qrytre {
	
public static void prcQryTre( String cataid,  E_LECODE lecode,  final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Qrytre.Output output){
	
		// 校验机构只有省级机构才能操作
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
		// 输入项检查
		if (CommUtil.isNull(cataid) && CommUtil.isNotNull(lecode)) {
			throw DpModuleError.DpstComm.BNAS2218();
		}
		if (CommUtil.isNotNull(cataid) && CommUtil.isNull(lecode)) {
			throw DpModuleError.DpstComm.BNAS2218();
			
		}
		// 查询一级目录：业务大类
		if (CommUtil.isNull(cataid) && CommUtil.isNull(lecode)) {
			lecode = E_LECODE.LEV1;
			List<KupQrydirTree> cplKupQrydirTree = DpProductDao.seleMenuByLev1(lecode, false);
			
			// 输出
			output.getCataInfos().addAll(cplKupQrydirTree);
			
		}
		// 查询下级目录
		if (CommUtil.isNotNull(cataid) && CommUtil.isNotNull(lecode)) {
			
			// 层级代码为LEV4，查询产品
			if (E_LECODE.LEV4 == lecode) {
				
				List<KupQrydirTree> cplKupQrydirTree = DpProductDao.selProdcdByCataid(cataid, false);
				
				// 输出
				output.getCataInfos().addAll(cplKupQrydirTree);
				
			// 查询下级目录	
			} else {
				
				List<KupQrydirTree> cplKupQrydirTree = DpProductDao.selMenuByLecode(cataid, lecode, false);
				
				// 输出
				output.getCataInfos().addAll(cplKupQrydirTree);
			}
		}

	}
}
