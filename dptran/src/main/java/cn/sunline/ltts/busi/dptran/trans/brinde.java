package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfirTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfirTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TEARTP;


public class brinde {
	/**
	 * 
	 * @Title: delPrdInterst 
	 * @Description: 删除违约支取利息部件信息 
	 * @param input
	 * @param output
	 * @author huangzhikai
	 * @date 2016年7月21日 下午10:00:58 
	 * @version V2.3.0
	 */
	public static void delPrdInterst( final cn.sunline.ltts.busi.dptran.trans.online.conf.Brinde.Input input,  final cn.sunline.ltts.busi.dptran.trans.online.conf.Brinde.Output output){
		//操作网点权限验证
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
		
		if(CommUtil.isNull(input.getProdcd())){
			throw DpModuleError.DpstProd.BNAS1328();
		}
		
		if(CommUtil.isNull(input.getTeartp())){
			throw DpModuleError.DpstProd.BNAS1357();
		}
		
		KupDppbPartTemp tblKupDppbPartTemp = KupDppbPartTempDao.selectOne_odb1(DpEnumType.E_BUSIBI.DEPO, input.getProdcd(), E_PARTCD._CK10, false);
		//判断违约利息产品是否配置部件
		if(CommUtil.isNull(tblKupDppbPartTemp)){
			throw DpModuleError.DpstProd.BNAS1318();
		}else{
			if(CommUtil.isNotNull(tblKupDppbPartTemp.getPartfg())){
				if(tblKupDppbPartTemp.getPartfg() == BaseEnumType.E_YES___.NO){
					throw DpModuleError.DpstProd.BNAS1319();
				}
			}
		}
		
		KupDppbTemp tblKupDppbTemp = KupDppbTempDao.selectOne_odb1(input.getProdcd(), false);
		//判断产品基础部件的币种是否存在
		if(CommUtil.isNull(tblKupDppbTemp)){
			throw DpModuleError.DpstProd.BNAS1320();
		}
		
		//产品编号
		String prodcd = input.getProdcd();
		//违约支取利息类型
		E_TEARTP teartp = input.getTeartp();
		//利息组代码
		String ingpcd = "8888";
		//币种
		String crcycd = tblKupDppbTemp.getPdcrcy();
		
		//查询产品违约支取利息信息
		KupDppbDfirTemp tblKupDppbDfirTemp = KupDppbDfirTempDao.selectOne_odb1(prodcd, crcycd, teartp, ingpcd, E_INTRTP.ZHENGGLX, false);
				
		if(CommUtil.isNull(tblKupDppbDfirTemp)){
			throw DpModuleError.DpstProd.BNAS1358();
		}
		KupDppbDfirTempDao.deleteOne_odb1(prodcd, crcycd, teartp, ingpcd, E_INTRTP.ZHENGGLX);
		
	}
}
