package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbMatuTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbMatuTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_HDDLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRSVTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRINWY;

public class dmaupd {

	public static void updDma(
			final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dmaupd.Input Input) {

		// 校验机构只有省级机构才能操作
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());

		// 查看部件信息
		KupDppbPartTemp selPartTemp = KupDppbPartTempDao.selectOne_odb1(
				E_BUSIBI.DEPO, Input.getProdcd(), E_PARTCD._CK08, false);

		if (CommUtil.isNotNull(selPartTemp)) {
			if (selPartTemp.getPartfg() == E_YES___.YES) {
				String prodcd = Input.getProdcd();// 产品号
				E_HDDLTP festdl = Input.getFestdl();// 遇节假日处理方式
				E_YES___ delyfg = Input.getDelyfg();// 是否根据存款顺延到期日
				String matupd = Input.getMatupd();// 是否根据存款顺延到期日
				E_YES___ trdpfg = Input.getTrdpfg();// 允许转存标志
				E_YES___ trpdfg = Input.getTrpdfg();// 可以更换转存产品号
				String trprod = Input.getTrprod();// 转存产品
				E_TRINWY trinwy = Input.getTrinwy();// 转存利率调整方式
				E_TRSVTP trprwy = Input.getTrprwy();// 转存方式
				Integer trprtm = Input.getTrprtm();// 转存次数

				// 传入值检查
				if (CommUtil.isNull(prodcd)) {
					DpModuleError.DpstProd.BNAS1054();
				}

				if (CommUtil.isNull(festdl)) {
					DpModuleError.DpstComm.BNAS2066();
				}

				if (CommUtil.isNull(delyfg)) {
					DpModuleError.DpstComm.BNAS2067();
				}

				// 当该属性选择1-是时，到期宽限期为必输项。
				if (delyfg == E_YES___.YES) {
					if (CommUtil.isNull(matupd)) {
						DpModuleError.DpstComm.BNAS2068();
					}
					if (!BusiTools.isNum(matupd)) {
						DpModuleError.DpstComm.BNAS2069();
					}
					if (ConvertUtil.toInteger(matupd) <= 0) {
						DpModuleError.DpstComm.BNAS2070();
					}
				}

				if (CommUtil.isNull(trdpfg)) {
					DpModuleError.DpstComm.BNAS2071();
				}

				// 当该属性选择1-允许时，是否可以更换转存产品号、转存次数、转存方式和转存利率调整方式为必输项。
				if (trdpfg == E_YES___.YES) {
					if (CommUtil.isNull(trpdfg)) {
						DpModuleError.DpstComm.BNAS2072();
					}

					if (CommUtil.isNull(trprwy)) {
						DpModuleError.DpstComm.BNAS2073();
					}

					if (CommUtil.isNull(trinwy)) {
						DpModuleError.DpstComm.BNAS2074();
					}

					if (CommUtil.isNull(trprtm)) {
						DpModuleError.DpstComm.BNAS2075();
					}
				}

				// 该属性选择1-可以时，转存金融基础产品编号为必输项
				if (trpdfg == E_YES___.YES) {
					if (CommUtil.isNull(trprod)) {
						DpModuleError.DpstComm.BNAS2076();
					}
				}

				// 查询产品基础临时表
				KupDppbTemp dppb = KupDppbTempDao.selectOne_odb1(prodcd, false);

				String crcycd = dppb.getPdcrcy();// 获取币种

				if (CommUtil.isNull(crcycd)) {
					DpModuleError.DpstComm.BNAS0761();
				}
				// 判断原记录是否存在
				KupDppbMatuTemp tmp = KupDppbMatuTempDao.selectOne_odb1( prodcd, crcycd, false);
				if (CommUtil.isNull(tmp)) {
					throw DpModuleError.DpstComm.BNAS2078();
				}
				// 更新新纪录
				tmp.setProdcd(prodcd);
				tmp.setCrcycd(crcycd);
				tmp.setFestdl(festdl);
				tmp.setDelyfg(delyfg);
				tmp.setMatupd(matupd);
				tmp.setTrdpfg(trdpfg);
				tmp.setTrpdfg(trpdfg);
				tmp.setTrprod(trprod);
				tmp.setTrinwy(trinwy);
				tmp.setTrsvtp(trprwy);
				tmp.setTrintm(trprtm);

				KupDppbMatuTempDao.updateOne_odb1(tmp);
			} else {
				throw DpModuleError.DpstComm.BNAS2077();
			}
		} else {
			throw DpModuleError.DpstComm.BNAS2062();
		}
	}
}
