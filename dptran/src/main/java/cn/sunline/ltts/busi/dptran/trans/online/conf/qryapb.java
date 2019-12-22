package cn.sunline.ltts.busi.dptran.trans.online.conf;


import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.type.DpProdType.KupDppbBrchAppOut;
import cn.sunline.ltts.busi.dp.type.DpProdType.KupDppbInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRENTP;
import cn.sunline.edsp.base.lang.Page;

public class qryapb {

	public static void qryapb( String prodcd,  String brchno,  final cn.sunline.ltts.busi.dptran.trans.intf.Qryapb.Output output){
		
		// 获取输入项信息
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();// 获取法人代码
		long pageno = CommTools.getBaseRunEnvs().getPage_start();// 页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();// 页容量
		long start = (pageno - 1) * pgsize;
		long totlCount = 0;// 总记录数
		
		// 操作网点权限验证
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
		
		// 产品号非空检查
		if (CommUtil.isNull(prodcd)) {
			throw DpModuleError.DpstProd.BNAS1054();
		}
		
		// 获取产品基础信息
		KupDppbInfo kubdpp = DpProductDao.selprodcd(prodcd, corpno, false);
		if (CommUtil.isNull(kubdpp)) {
			
			throw  DpModuleError.DpstComm.BNAS0761();
		}
		
		E_PRODST prodst = kubdpp.getProdst();// 产品状态

		// 如果启用方式为销售工厂启用 则返回空值
		if (E_PRENTP.SALE == kubdpp.getPrentp()) {

			// 销售工厂启用不做处理，返回空

		// 启用方式为直接装配
		} else {

			// 产品状态为产品录入或产品装配的查询临时表数据
			if (E_PRODST.ASSE == prodst || E_PRODST.INPUT == prodst) {
				// 分页查询适用机构临时表信息
				Page<KupDppbBrchAppOut> cplKupBrchs = DpProductDao.selPageKupDppbBrchTemp(prodcd, brchno, start, pgsize, totlCount, false);

				// 设置输出
				output.getAppBrchsOutput().addAll(cplKupBrchs.getRecords());
				// 设置报文头总记录条数
				CommTools.getBaseRunEnvs().setTotal_count(cplKupBrchs.getRecordCount());

			} else {

				// 分页查询适用机构正式表信息
				Page<KupDppbBrchAppOut> cplKupBrchs = DpProductDao.selPageKupDppbBrch(prodcd, brchno, start, pgsize, totlCount, false);

				// 设置输出
				output.getAppBrchsOutput().addAll(cplKupBrchs.getRecords());
				// 设置报文头总记录条数
				CommTools.getBaseRunEnvs().setTotal_count(cplKupBrchs.getRecordCount());
			}
		}
	}
}
