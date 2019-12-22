package cn.sunline.ltts.busi.dptran.trans;



import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.type.DpProdType.KupQrydirInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSILV;
import cn.sunline.edsp.base.lang.Page;






public class qrydir {

public static void qrydirr( cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSILV busilv,  String qrycon,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrydir.Output output){
		
		// 校验机构只有省级机构才能操作
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
		// 公共变量区数据获取 
		long pageno = CommTools.getBaseRunEnvs().getPage_start();// 页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();// 页容量
		long startno = (pageno - 1) * pgsize;
		long totlCount = 0;
		
		// 输入项非空检查
		if (CommUtil.isNull(busilv)) {
			throw DpModuleError.DpstComm.BNAS1878();
		}
		if (CommUtil.isNull(qrycon)) {
			throw DpModuleError.DpstComm.BNAS1879();
		}
		
		String lecode = null;// 层级代码
		
		// 查询产品
		if (E_BUSILV.CATANA == busilv) {
			
			Page<KupQrydirInfo> cplKupQrydirInfo = DpProductDao.selDirByProdcd(qrycon, startno, pgsize, totlCount, false);
			
			// 设置总记录数
			CommTools.getBaseRunEnvs().setTotal_count(cplKupQrydirInfo.getRecordCount());// 记录总数
			
			// 输出
			output.getCataInfos().addAll(cplKupQrydirInfo.getRecords());
			
		// 查询目录	
		} else {
			// 根据产品业务级别获取层级代码
			if (E_BUSILV.BUSIBI == busilv) {
				lecode = "LEV1";// 业务大类

			} else if (E_BUSILV.PRODTP == busilv) {
				lecode = "LEV2";// 业务中类

			} else if (E_BUSILV.PDDPFG == busilv) {
				lecode = "LEV3";// 业务小类

			} else if (E_BUSILV.DEBTTP == busilv) {
				lecode = "LEV4";// 业务细类

			}
			
			Page<KupQrydirInfo> cplKupQrydirInfo = DpProductDao.selKnbParaMenuBylecode(lecode, qrycon, startno, pgsize, totlCount, false);
			
			// 设置总记录数
			CommTools.getBaseRunEnvs().setTotal_count(cplKupQrydirInfo.getRecordCount());// 记录总数
			
			// 输出
			output.getCataInfos().addAll(cplKupQrydirInfo.getRecords());
		}

	}
}
