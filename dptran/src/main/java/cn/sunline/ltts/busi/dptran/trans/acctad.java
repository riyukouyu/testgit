package cn.sunline.ltts.busi.dptran.trans;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcctTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcctTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCustTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCustTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTermTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTermTempDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MADTBY;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class acctad {
	
	private static final BizLog log = BizLogUtil.getBizLog(acctad.class);
	
	/**
	 * 
	 * @Title: InsDpAcct 
	 * @Description: 新增存款核算部件
	 * @param input
	 * @param output
	 * @author huangzhikai
	 * @date 2016年7月13日 下午17:00:58 
	 * @version V2.3.0
	 */
	public static void InsDpAcct( String prodcd,  String acctcd,  final cn.sunline.ltts.busi.dptran.trans.online.conf.Acctad.Output output){
		
		//操作网点权限验证
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
		
		// 输入项非空检查
		if(CommUtil.isNull(prodcd)){
			throw DpModuleError.DpstProd.BNAS1054();
		}
		
		if(CommUtil.isNull(acctcd)){
			throw DpModuleError.DpstComm.BNAS1269();
		}
		
		KupDppbTemp tblKupDppbTemp = KupDppbTempDao.selectOne_odb1(prodcd, false);
		//判断产品基础部件的币种是否存在
		if(CommUtil.isNull(tblKupDppbTemp)){
			throw DpModuleError.DpstProd.BNAS1298();
		}
		
		//log.debug("<<<<<<<<<<<产品核算代码["+ prodcd.substring(8, 15) + "]>>>>>>>>>");
		// 检查核算代码是否合规

		/*if (!CommUtil.equals(prodcd.substring(8, 15).concat("000"), acctcd)) {
			throw DpModuleError.DpstComm.E9999("核算代码填写错误请检查");
		}*/

		/*if (!CommUtil.equals(prodcd.substring(8, 15).concat("000"), acctcd)) {
			throw DpModuleError.DpstProd.BNAS1299();
		}*/

		
		KupDppbPartTemp tblKupDppbPartTemp = KupDppbPartTempDao.selectOne_odb1(DpEnumType.E_BUSIBI.DEPO, prodcd, E_PARTCD._CK11, false);
		//判断违约利息产品是否配置部件
		if(CommUtil.isNull(tblKupDppbPartTemp)){
			throw DpModuleError.DpstProd.BNAS1300();
		}else{
			if(CommUtil.isNotNull(tblKupDppbPartTemp.getPartfg())){
				if(tblKupDppbPartTemp.getPartfg() == BaseEnumType.E_YES___.NO){
					throw DpModuleError.DpstProd.BNAS1301();
				}
			}
		}
		
		// 查询开户控制部件信息
		KupDppbCustTemp tblKupDppbCustTemp = KupDppbCustTempDao.selectOne_odb2(prodcd, false);
		if (CommUtil.isNull(tblKupDppbCustTemp)) {
			throw DpModuleError.DpstProd.BNAS1302();
		}
		
		// 查询核算信息
		KupDppbAcctTemp tblKupDppbAcctTemp = KupDppbAcctTempDao.selectFirst_odb2(prodcd, false);
		if(CommUtil.isNotNull(tblKupDppbAcctTemp)){
			throw DpModuleError.DpstProd.BNAS1303();
		}
		
		// 到期日确定方式为[1-无到期日]
		if (E_MADTBY.NO == tblKupDppbCustTemp.getMadtby()) {
			// 查询存期信息
			List<KupDppbTermTemp> termList = KupDppbTermTempDao.selectAll_odb2(prodcd, false);
			
			if (CommUtil.isNull(termList) || termList.size() == 0) {
				KupDppbAcctTemp entity = SysUtil.getInstance(KupDppbAcctTemp.class);
				entity.setDepttm(E_TERMCD.ALL);// 存期
				entity.setAcctcd(acctcd);// 核算代码
				entity.setProdcd(prodcd);// 产品编号
				// 插入核算部件临时表
				KupDppbAcctTempDao.insert(entity);
			} else {
				
				KupDppbAcctTemp entity = SysUtil.getInstance(KupDppbAcctTemp.class);
				entity.setDepttm(termList.get(0).getDepttm());// 存期
				entity.setAcctcd(acctcd);// 核算代码
				entity.setProdcd(prodcd);// 产品编号
				// 插入核算部件临时表
				KupDppbAcctTempDao.insert(entity);
			}
			
		// 到期日确定方式为非[2-指定到期日]	
		} else if (E_MADTBY.SET != tblKupDppbCustTemp.getMadtby()) {

			// 查询存期信息
			List<KupDppbTermTemp> termList = KupDppbTermTempDao.selectAll_odb2(prodcd, false);
			if (CommUtil.isNull(termList) || termList.size() == 0) {
				throw DpModuleError.DpstProd.BNAS1304();
			}

			// 获取表实例化对象
			for (KupDppbTermTemp term_temp : termList) {
				KupDppbAcctTemp entity = SysUtil.getInstance(KupDppbAcctTemp.class);
				entity.setDepttm(term_temp.getDepttm());// 存期
				entity.setAcctcd(acctcd);// 核算代码
				entity.setProdcd(prodcd);// 产品编号

				// 插入核算部件临时表
				KupDppbAcctTempDao.insert(entity);
			}
			// 到期日确定方式为[2-指定到期日],设置为全部
		} else {

			KupDppbAcctTemp entity = SysUtil.getInstance(KupDppbAcctTemp.class);
			entity.setDepttm(E_TERMCD.ALL);// 存期
			entity.setAcctcd(acctcd);// 核算代码
			entity.setProdcd(prodcd);// 产品编号
			
			// 插入核算部件临时表
			KupDppbAcctTempDao.insert(entity);

		}

	}

}
