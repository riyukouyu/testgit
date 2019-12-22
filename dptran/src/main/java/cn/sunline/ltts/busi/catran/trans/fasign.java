package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCuad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCuadDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * 
 * @ClassName: fasign
 * @author xiongzhao
 * @date 2017年3月23日 下午3:15:56
 * 
 */
public class fasign {

	/**
	 * 
	 * @Title: faceSign
	 * @Description: (电子账户面签)
	 * @param cardno
	 * @author xiongzhao
	 * @date 2017年3月23日 下午3:16:14
	 * @version V2.3.0
	 */
	public static void faceSign( final cn.sunline.ltts.busi.catran.trans.intf.Fasign.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Fasign.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Fasign.Output output) {

		String custac = "";// 电子账号

		// 检查输入接口交易卡号是否为空
		if (CommUtil.isNull(input.getCardno())) {
			throw DpModuleError.DpstComm.BNAS1285(input.getCardno());
		}

		// 根据交易卡号查询电子账号
		IoCaKnaAcdc cplKnaAcdc = CaDao.selKnaAcdcByCard(input.getCardno(), false);
		if (CommUtil.isNotNull(cplKnaAcdc)) {
			custac = cplKnaAcdc.getCustac();
		}
		
		// 根据电子账号查询电子账户信息表
		KnaCust tblKnaCust = CaDao.selKnaCustByCustac(custac, true);
		property.setCustna(tblKnaCust.getCustna());
		// 检查面签法人机构和电子账户归属法人机构是否为同一法人机构
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getBusi_org_id(), tblKnaCust.getCorpno())) {
			throw CaError.Eacct.E0023();
		}
		
		// 打上面签标志
		KnaCuad tblKnaCuad = KnaCuadDao.selectOne_knaCuadOdx1(custac, false);
		tblKnaCuad.setFacesg(E_YES___.YES);
		KnaCuadDao.updateOne_knaCuadOdx1(tblKnaCuad);

	}

		public static void qryinac( final cn.sunline.ltts.busi.catran.trans.intf.Fasign.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Fasign.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Fasign.Output output){
			E_INSPFG inspfg = property.getInspfg();
			
			if(E_INSPFG.INVO == inspfg){ //账户涉案
				throw DpModuleError.DpstAcct.BNAS0770();
			}
			if(E_INSPFG.SUSP == inspfg){ //账户可疑
				throw DpModuleError.DpstAcct.BNAS3004();//电子账户为可疑账户，不允许面签
			}
		}


	
}
