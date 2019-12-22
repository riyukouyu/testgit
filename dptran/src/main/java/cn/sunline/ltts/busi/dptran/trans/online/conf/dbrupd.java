package cn.sunline.ltts.busi.dptran.trans.online.conf;


import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrchTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrchTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dbrupd.Input.DelBrchInfos;
import cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dbrupd.Input.InsBrchInfos;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BRSTUS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BRCHFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRENTP;

public class dbrupd {

public static void updDbr( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dbrupd.Input input){
		
		// 校验机构只有省级机构才能操作
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());

		// 获取输入项
		String prodcd = input.getProdcd();// 产品号

		// 输入项非空检查
		if (CommUtil.isNull(prodcd)) {

			throw DpModuleError.DpstProd.BNAS1328();
		}
		if (input.getDelBrchInfos().size() == 0 && input.getInsBrchInfos().size() == 0) {

			throw DpModuleError.DpstComm.BNAS1968();
		}

		// 查询产品临时表信息
		KupDppbTemp tblKupDppbTemp = KupDppbTempDao.selectOne_odb1(prodcd, false);
		if (CommUtil.isNull(tblKupDppbTemp)) {
			throw DpModuleError.DpstProd.BNAS1329();
		}
		
		String brchno = "";// 机构号
		// 币种
		String pdcrcy = tblKupDppbTemp.getPdcrcy();
		
		// 检查是否有新增适用机构
		List<KupDppbBrchTemp> tblbrchTmpes = DpProductDao.selKupDppbBrchTempByProdcd(prodcd, false);
		if (CommUtil.isNull(tblbrchTmpes) || tblbrchTmpes.size() ==  0) {
			throw DpModuleError.DpstComm.BNAS1969();
		}
		
		// 循环删除适用机构
		for (DelBrchInfos cplDelBrchInfos : input.getDelBrchInfos()) {

			if (CommUtil.isNull(cplDelBrchInfos.getBrchno())) {

				throw DpModuleError.DpstComm.BNAS1970();
			}
			// 机构号
			brchno = cplDelBrchInfos.getBrchno();

			// 判断记录是否存在
			KupDppbBrchTemp tblbrch_temp = DpProductDao.selKupDppbBrchTemp(
					prodcd, pdcrcy, brchno, false);
			if (CommUtil.isNull(tblbrch_temp)) {

				throw DpModuleError.DpstComm.BNAS1971(prodcd,brchno );
			}

			// 删除适用机构
			DpProductDao.delKupDppbBrchTemp(prodcd, pdcrcy, brchno);

		}

		// 产品适用机构新增判断该产品机构控制部件是否启用
		KupDppbPartTemp tblpart_temp = KupDppbPartTempDao.selectOne_odb1( E_BUSIBI.DEPO, prodcd, E_PARTCD._CK02, false);
		if (CommUtil.isNull(tblpart_temp)
				|| E_YES___.YES != tblpart_temp.getPartfg()) {

			throw DpModuleError.DpstProd.BNAS1331();
		}
		
		// 循环新增适用机构
		for (InsBrchInfos cplInsBrchInfos : input.getInsBrchInfos()) {

			// 销售工厂启用的产品不允许添加适用机构
			if (E_PRENTP.SALE == tblKupDppbTemp.getPrentp()) {
				
				throw DpModuleError.DpstProd.BNAS1332();
			}
			
			// 机构控制标志为1-全部机构时，不允许添加适用机构
			if (E_BRCHFG.ALL == tblKupDppbTemp.getBrchfg()) {

				throw DpModuleError.DpstProd.BNAS1333();
			}
			
			if (CommUtil.isNull(cplInsBrchInfos.getBrchno())) {

				throw DpModuleError.DpstComm.BNAS1972();
			}
			// 机构号
			brchno = cplInsBrchInfos.getBrchno();

			// 判断记录是否已经存在
			KupDppbBrchTemp tblbrch_temp = DpProductDao.selKupDppbBrchTemp(
					prodcd, pdcrcy, brchno, false);
			if (CommUtil.isNotNull(tblbrch_temp)) {

				throw DpModuleError.DpstComm.BNAS1973( prodcd ,brchno );
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
			KupDppbBrchTemp temp = SysUtil.getInstance(KupDppbBrchTemp.class);
			temp.setBrchno(brchno);// 机构号
			temp.setCrcycd(pdcrcy);// 币种
			temp.setProdcd(prodcd);// 产品编号
			temp.setEfctdt(tblKupDppbTemp.getEfctdt());
			temp.setInefdt(tblKupDppbTemp.getInefdt());
			temp.setCorpno(cplIoBrchInfo.getCorpno());// 法人代码

			// 新增适用机构
			KupDppbBrchTempDao.insert(temp);

		}

	}

}
