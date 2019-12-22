package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrch;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrchDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Probrh.Input.DelBrchInfos;
import cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Probrh.Input.InsBrchInfos;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BRSTUS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BRCHFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRENTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRTRTP;


public class probrh {

public static void proBrchUp( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Probrh.Input input){
	
	
		//操作网点权限验证
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
		// 交易日期
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		// 获取输入项产品号
		String prodcd = input.getProdcd();
		// 输入项非空检查
		if (CommUtil.isNull(prodcd)) {

			throw DpModuleError.DpstProd.BNAS1054();
		}

		if (CommUtil.isNull(input.getInsBrchInfos())
				&& input.getInsBrchInfos().size() == 0
				&& CommUtil.isNull(input.getDelBrchInfos())
				&& input.getDelBrchInfos().size() == 0) {

			throw DpModuleError.DpstComm.BNAS2212();
		}

		// 基础属性正式表
		KupDppb tblKupDppb = KupDppbDao.selectOne_odb1(prodcd, false);
		if (CommUtil.isNull(tblKupDppb)) {

			throw DpModuleError.DpstComm.BNAS0761();
		}
		
		// 适用机构调整权限检查
		if (E_BRCHFG.ALL == tblKupDppb.getBrchfg()) {
			throw DpModuleError.DpstComm.BNAS2213();
		}
		if (E_PRENTP.SALE == tblKupDppb.getPrentp()) {
			throw DpModuleError.DpstComm.BNAS2214();
		}

		// 币种
		String pdcrcy = tblKupDppb.getPdcrcy();

		// 循环删除适用机构
		for (DelBrchInfos cplDelBrchInfos : input.getDelBrchInfos()) {

			if (CommUtil.isNull(cplDelBrchInfos.getBrchno())) {

				throw DpModuleError.DpstComm.BNAS1970();
			}
			// 机构号
			String brchno = cplDelBrchInfos.getBrchno();

			// 判断记录是否存在
			KupDppbBrch tblbrch = DpProductDao.selKupDppbBrch(prodcd, pdcrcy, brchno, false);
			if (CommUtil.isNull(tblbrch)) {

				throw DpModuleError.DpstComm.BNAS1971( prodcd ,brchno );
			}

			// 删除适用机构
			DpProductDao.delKupDppbBrch(prodcd, pdcrcy, brchno);

		}

		// 循环新增适用机构
		for (InsBrchInfos cplInsBrchInfos : input.getInsBrchInfos()) {

			// 机构号
			String brchno = cplInsBrchInfos.getBrchno();
			// 生效日期
			String efctdt = cplInsBrchInfos.getEfctdt();
			// 失效日期
			String inefdt = cplInsBrchInfos.getInefdt();

			// 非空项检查
			if (CommUtil.isNull(brchno)) {

				throw DpModuleError.DpstComm.BNAS1972();
			}
			if (CommUtil.isNull(efctdt)) {

				throw DpModuleError.DpstComm.BNAS2195();
			}
			if (CommUtil.isNull(inefdt)) {

				throw DpModuleError.DpstComm.BNAS2196();
			}

			// 适用机构日期检查
			if (CommUtil.compare(efctdt, trandt) <= 0) {

				throw DpModuleError.DpstComm.BNAS2101();
			}

			if (CommUtil.compare(efctdt, tblKupDppb.getEfctdt()) < 0) {

				throw DpModuleError.DpstComm.BNAS2215();
			}
			if (CommUtil.compare(inefdt, efctdt) <= 0) {

				throw DpModuleError.DpstComm.BNAS2100();
			}

			if (CommUtil.compare(inefdt, tblKupDppb.getInefdt()) > 0) {

				throw DpModuleError.DpstComm.BNAS2216();
			}

			// 判断记录是否已经存在
			KupDppbBrch tblbrch = DpProductDao.selKupDppbBrch(prodcd, pdcrcy, brchno, false);
			if (CommUtil.isNotNull(tblbrch)) {

				throw DpModuleError.DpstComm.BNAS1973(prodcd, brchno);
			}

			// 根据机构查询对应法人
			IoBrchInfo cplIoBrchInfo = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(brchno);
			if (CommUtil.isNull(cplIoBrchInfo)) {
				throw DpModuleError.DpstComm.BNAS1168( brchno );
			}
			
			if (E_BRSTUS.invalid == cplIoBrchInfo.getBrstus()) {
				throw DpModuleError.DpstComm.BNAS1167( brchno );
			}

			// 添加机号构信息
			KupDppbBrch temp = SysUtil.getInstance(KupDppbBrch.class);
			temp.setBrchno(brchno);// 机构号
			temp.setCrcycd(pdcrcy);// 币种
			temp.setProdcd(prodcd);// 产品编号
			temp.setEfctdt(efctdt);// 生效日期
			temp.setInefdt(inefdt);// 失效日期
			temp.setCorpno(cplIoBrchInfo.getCorpno());// 法人代码

			// 新增适用机构
			KupDppbBrchDao.insert(temp);

		}
		
		// 机构控制标志为2-适用机构
		if (E_BRCHFG.USE == tblKupDppb.getBrchfg()) {
			List<KupDppbBrch> tblKupDppbBrch = DpProductDao.selKupDppbBrchByprodcd(prodcd, false);
			if (CommUtil.isNull(tblKupDppbBrch) || tblKupDppbBrch.size() == 0) {
				throw DpModuleError.DpstComm.BNAS2217();
			}
		}
		
		// 产品操作柜员登记
		SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.DEPO, prodcd, E_PRTRTP.ADJU);
	}
}