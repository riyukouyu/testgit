package cn.sunline.ltts.busi.dptran.trans;

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
import cn.sunline.ltts.busi.dptran.trans.intf.Addpbh.Input.AddBrchInfos;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BRSTUS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BRCHFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRENTP;

public class addpbh {
 
	/**
	 * 
	 * @Title: addKupDppbBrch 
	 * @Description: 增加产品机构控制部件信息
	 * @param input
	 * @author xvdawei
	 * @date 2016年7月25日 下午2:54:33 
	 * @version V2.3.0
	 */
	public static void addKupDppbBrch( final cn.sunline.ltts.busi.dptran.trans.intf.Addpbh.Input input) {

		// 校验机构只有省级机构才能操作
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
		
		// 获取输入产品号
		String prodcd = input.getProdcd();
		
		
		if (CommUtil.isNull(prodcd)) {
			throw DpModuleError.DpstProd.BNAS1328();
		}
	
		// 查看基础属性表中产品编号对应的币种
		KupDppbTemp dppbtemp = KupDppbTempDao.selectOne_odb1(prodcd, false);
		if (CommUtil.isNull(dppbtemp)) {
			throw DpModuleError.DpstProd.BNAS1329();
		}
		// 币种
		String crcycd = dppbtemp.getPdcrcy();
		
		List<KupDppbBrchTemp> tblbrchTmpes = DpProductDao.selKupDppbBrchTempByProdcd(prodcd, false);
		if (CommUtil.isNotNull(tblbrchTmpes) || tblbrchTmpes.size() > 0) {
			throw DpModuleError.DpstProd.BNAS1330();
		}
		
		// 产品适用机构新增判断该产品机构控制部件是否启用
		KupDppbPartTemp tblpart_temp = KupDppbPartTempDao.selectOne_odb1(E_BUSIBI.DEPO, prodcd, E_PARTCD._CK02, false);
		if (CommUtil.isNull(tblpart_temp) || E_YES___.YES != tblpart_temp.getPartfg()) {
			
			throw DpModuleError.DpstProd.BNAS1331();
		}
		
		// 销售工厂启用的产品不允许添加适用机构
		if (E_PRENTP.SALE == dppbtemp.getPrentp()) {

			throw DpModuleError.DpstProd.BNAS1332();
		}

		// 机构控制标志为1-全部机构时，不允许添加适用机构
		if (E_BRCHFG.ALL == dppbtemp.getBrchfg()) {
			if(input.getAddBrchInfos().size() != 0){
				throw DpModuleError.DpstProd.BNAS1333();
			}
		}else{
			if (input.getAddBrchInfos().size() == 0 ) {
				throw DpModuleError.DpstProd.BNAS1334();
			}
		}
		// 将输入记录循环取出
		for (AddBrchInfos info : input.getAddBrchInfos()) {
			
			// 机构号
			String brchno = info.getBrchno();
			
			// 判断记录是否已经存在
			KupDppbBrchTemp tempbrch = DpProductDao.selKupDppbBrchTemp(prodcd, crcycd, brchno, false);
			if (CommUtil.isNotNull(tempbrch)) {
				throw DpModuleError.DpstProd.BNAS1335();
			}

			// 根据机构查询对应法人
			IoBrchInfo cplIoBrchInfo = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(brchno);
			if (CommUtil.isNull(cplIoBrchInfo)) {
				throw DpModuleError.DpstProd.BNAS1336();
			}
			
			if (E_BRSTUS.invalid == cplIoBrchInfo.getBrstus()) {
				throw DpModuleError.DpstComm.BNAS1167(brchno);
			}
			
			// 添加机构信息
			KupDppbBrchTemp temp = SysUtil.getInstance(KupDppbBrchTemp.class);
			temp.setBrchno(brchno);
			temp.setCrcycd(crcycd);
			temp.setProdcd(prodcd);
			temp.setEfctdt(dppbtemp.getEfctdt());
			temp.setInefdt(dppbtemp.getInefdt());
			temp.setCorpno(cplIoBrchInfo.getCorpno());

			KupDppbBrchTempDao.insert(temp);

		}

	}

}
