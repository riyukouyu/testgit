package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCustTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCustTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTermTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTermTempDao;
import cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dcuupd.Input.DepttmInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MADTBY;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_MGINFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_ONLYFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;

public class dcuupd {

	public static void updDcu( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dcuupd.Input input){
		
		String prodcd = input.getProdcd();// 产品编号
		E_MGINFG mginfg = input.getMginfg();// 早起息许可标志
		Integer mgindy = input.getMgindy();// 早起息天数
		E_MADTBY madtby = input.getMadtby();// 到期日确认方式
		E_ONLYFG onlyfg = input.getOnlyfg();// 客户下唯一标志
		BigDecimal srdpam = input.getSrdpam();// 起存金额
		BigDecimal stepvl = input.getStepvl();// 步长值

		// 校验机构只有省级机构才能操作
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());

		// 交易输入数据的合法性
		if (CommUtil.isNull(prodcd)) {
			throw DpModuleError.DpstProd.BNAS1328();
		}

		KupDppbTemp tblkup_dppbt = KupDppbTempDao.selectOne_odb1(prodcd, false);
		if (CommUtil.isNull(tblkup_dppbt)) {
			throw DpModuleError.DpstProd.BNAS1337();
		}
		
		if (CommUtil.isNull(mginfg)) {
			throw DpModuleError.DpstProd.BNAS1338();
		}
		
		// 当早起息标志为是时则早起息天数必填
		if (E_MGINFG.PMIT == mginfg) {
			if (CommUtil.isNull(mgindy)) {
				throw DpModuleError.DpstProd.BNAS1339();
			}
			if (mgindy <= 0) {
				throw DpModuleError.DpstProd.BNAS1340();
			}
			
		} else {
			if (CommUtil.isNotNull(mgindy)) {
				throw DpModuleError.DpstProd.BNAS1341();
			}
		}

		if (CommUtil.isNull(madtby)) {
			throw DpModuleError.DpstProd.BNAS1342();
		}
		
		// 业务小类为活期时，到期日确定方式只能为无到期日
		if (E_FCFLAG.CURRENT == tblkup_dppbt.getPddpfg()) {
			if (E_MADTBY.NO != madtby) {

				throw DpModuleError.DpstProd.BNAS1343();
			}
		}

		/*// 当属性为 指定到期日时，存期只能为 空
		if (E_MADTBY.SET == madtby) {

			if (CommUtil.isNotNull(input.getDepttmInfo()) || input.getDepttmInfo().size() != 0) {
				throw DpModuleError.DpstComm.E9999("到期日确定方式为指定到期日时，存期信息必须为空");
			}
		
		// 到期日确定方式为无到期日时，存在存期信息只能为活期或空
		} else if (madtby == E_MADTBY.NO) {
			
			if (CommUtil.isNotNull(input.getDepttmInfo())) {
				if (input.getDepttmInfo().size() !=1 ) {
					throw DpModuleError.DpstComm.E9999("到期日确定方式为无到期日时，存在存期信息只能为活期");
				}
				if (E_TERMCD.T000 != input.getDepttmInfo().get(0).getDepttm()) {
					throw DpModuleError.DpstComm.E9999("到期日确定方式为无到期日时，存在存期信息只能为活期");
				}
			}
			
		} else {*/
			
		if (madtby != E_MADTBY.SET && madtby != E_MADTBY.NO) {
			if (CommUtil.isNull(input.getDepttmInfo()) || input.getDepttmInfo().size() == 0) {
				throw DpModuleError.DpstProd.BNAS1344();
			}
		}
			
		HashSet<String> set = new HashSet<String>();// 去重的存期
		HashSet<Long> set2 = new HashSet<Long>();// 去重的存期天数
		List<DepttmInfo> list = new ArrayList<DepttmInfo>();// 自定义存期
			
			// 检查存期是否合法
		for (DepttmInfo tmInfo : input.getDepttmInfo()) {

			if (CommUtil.isNull(tmInfo.getDepttm())) {
				throw DpModuleError.DpstProd.BNAS1345();
			}

				// 当属性为 无到期日时，存期只能为 活期
//				if (E_MADTBY.NO == madtby) {
//
//					if (E_TERMCD.T000 != tmInfo.getDepttm()) {
//						throw DpModuleError.DpstComm
//								.E9999("存期当属性为 无到期日时，存期只能为 活期且输入不能为空");
//					}
//				}

				// 当属性为根据开户日计算到期日或者根据首次存入日计算到期日时，存期可选 活期除外的其他存期
			if (E_MADTBY.T_OR_S == madtby || E_MADTBY.TERMCD == madtby) {

				if (E_TERMCD.T000 == tmInfo.getDepttm()) {
					throw DpModuleError.DpstProd.BNAS1347();
				}
			}

			// 判断当存期为自定义时 需要录入具体的天数
			String testa = tmInfo.getDepttm().getValue().substring(0, 2);
			String testb = "90";
			if (CommUtil.equals(testa, testb)) {

				if (CommUtil.isNull(tmInfo.getDeptdy())) {
					throw DpModuleError.DpstProd.BNAS1348();
				}
			} else {

				if (CommUtil.isNotNull(tmInfo.getDeptdy())) {
					throw DpModuleError.DpstProd.BNAS1349();
				}
			}
			
			set.add(tmInfo.getDepttm().getValue());// 去重后的存期
			
			if (CommUtil.equals(tmInfo.getDepttm().getValue().substring(0, 1), "9")) {
				list.add(tmInfo);
				set2.add(tmInfo.getDeptdy());// 去重后的存期天数
			}
		}
			
		if (set.size() != input.getDepttmInfo().size()) {
			throw DpModuleError.DpstProd.BNAS1350();
		}
		
		if (set2.size() != list.size()) {
			throw DpModuleError.DpstProd.BNAS1351();
		}

//		}
		if (CommUtil.isNull(onlyfg)) {
			throw DpModuleError.DpstProd.BNAS1352();
		}

		if (CommUtil.isNotNull(srdpam)) {
			if (CommUtil.compare(srdpam, BigDecimal.ZERO) < 0) {
				throw DpModuleError.DpstProd.BNAS1353();
			}
		}
		
		if (CommUtil.isNotNull(stepvl)) {
			if (CommUtil.compare(stepvl, BigDecimal.ZERO) < 0) {
				throw DpModuleError.DpstProd.BNAS1354();
			}
		}
		

		// 判断产品部件是否启用是否存在
		KupDppbPartTemp parttemp = KupDppbPartTempDao.selectOne_odb1(E_BUSIBI.DEPO, prodcd, E_PARTCD._CK03, false);
		if (CommUtil.isNull(parttemp) || parttemp.getPartfg() != E_YES___.YES) {
			throw DpModuleError.DpstProd.BNAS1355();
		}

		// *修改纪录 产品基础属性临时表
		tblkup_dppbt.setMginfg(mginfg);
		tblkup_dppbt.setMgindy(mgindy);

		KupDppbTempDao.updateOne_odb1(tblkup_dppbt);

		// *修改产品开户控制临时表
		KupDppbCustTemp custtemp = KupDppbCustTempDao.selectOne_odb1(prodcd, tblkup_dppbt.getPdcrcy(), false);
		if (CommUtil.isNull(custtemp)) {
			throw DpModuleError.DpstComm.BNAS1975();
		}

		custtemp.setMadtby(madtby);
		custtemp.setOnlyfg(onlyfg);
		custtemp.setProdcd(prodcd);
		custtemp.setSrdpam(srdpam);
		custtemp.setStepvl(stepvl);
		custtemp.setCrcycd(tblkup_dppbt.getPdcrcy());

		KupDppbCustTempDao.updateOne_odb1(custtemp);

		// *修改产品存期控制表
//		KupDppbTermTempDao.selectFirst_odb2(prodcd, false);
		KupDppbTermTempDao.delete_odb2(prodcd);
		
		for (DepttmInfo tmInfo : input.getDepttmInfo()) {
			
			
			KupDppbTermTemp termtemp = SysUtil.getInstance(KupDppbTermTemp.class);
			
			termtemp.setCrcycd(tblkup_dppbt.getPdcrcy());
			termtemp.setDeptdy(tmInfo.getDeptdy());
			termtemp.setDepttm(tmInfo.getDepttm());
			termtemp.setProdcd(prodcd);

			KupDppbTermTempDao.insert(termtemp);
		}

	}
}
