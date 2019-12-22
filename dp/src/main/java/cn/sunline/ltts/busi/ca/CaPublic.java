package cn.sunline.ltts.busi.ca;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.namedsql.AccountLimitDao;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtLimt;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtSbac;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtSbacDao;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtServ;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;

public class CaPublic {
	public static BigDecimal setValueIfZero(BigDecimal val) {
		if (CommUtil.compare(val, BigDecimal.ZERO) == 0) {
			return BigDecimal.valueOf(999999999);
		} else {
			return val;
		}
	}
	
	/** 
	 * 剩余额度处理
	 * @param setVal 设置的限额
	 * @param calVal 累计的额度
	 * @return
	 */
	public static BigDecimal setValue(BigDecimal setVal, BigDecimal calVal) {
		if (CommUtil.compare(setVal, BigDecimal.ZERO) == 0) {
			return BigDecimal.valueOf(999999999);
		} else {
			return setVal.subtract(calVal);
		}
	}
	/** 
	 * 剩余额度处理
	 * @param setVal 设置的限额
	 * @param calVal 累计的额度
	 * @return
	 */
	public static long setValue(long setVal, long calVal) {
		if (Long.compare(setVal, 0) == 0) {
			return 999999999;
		} else {
			return setVal - calVal;
		}
	}
	
	public static String genTransq() {
		return BusiTools.getSequence("catsdt_sq", 15);
	}
	
	public static String transString(String str) {
		return str.substring(0, 4) + "-" + str.substring(4, 6) + "-" + str.substring(6, 8);
	}
	
	/**
	 * 获取组合渠道类型
	 * @author chengen
	 * @param strServtp 单一渠道
	 * @return serv 组合渠道
	 */
	public static String QryCmsvtps(String strServtp) {
		String serv = "";
		List<KupCurtServ> servList = AccountLimitDao.selCurtServByServtp(
				strServtp, false);
		if (servList.size() < 1) {
			if (CommUtil.isNotNull(strServtp)){
				serv = "'"+strServtp+"'";
			} else {
				return null;
			}
			
		} else {
			for (KupCurtServ tbserv : servList) {
				if (serv.length() > 0) {
					serv = serv + ",'" + tbserv.getCmsvtp()+"'";
				} else {
					serv = "'"+tbserv.getCmsvtp()+"'";
				}
			}
		}
		return serv;
	}
	
	/**
	 * 扣减获取组合渠道类型
	 * @author chengen
	 * @param strServtp 单一渠道
	 * @return serv 组合渠道
	 */
	public static String QryCmsvtpsAcc(String strServtp) {
		String serv = "";
		List<KupCurtServ> servList = AccountLimitDao.selCurtServByServtp(
				strServtp, false);
		if (servList.size() < 1) {
			return null;			
		} else {
			for (KupCurtServ tbserv : servList) {
				if (serv.length() > 0) {
					serv = serv + ",'" + tbserv.getCmsvtp()+"'";
				} else {
					serv = "'"+tbserv.getCmsvtp()+"'";
				}
			}
		}
		return serv;
	}
	
	/**
	 * 获取组合账户限额类型
	 * @author chengen
	 * @param limttp 单一额度类型
	 * @return limt 组合额度类型
	 */
	public static String QryCmqttps(String limttp) {
		String limt = "";
		List<KupCurtLimt> limtList = AccountLimitDao.selCurtLimt(
				"'"+limttp+"'",null, false);
		if (limtList.size() < 1) {
			if (CommUtil.isNotNull(limttp)) {
				limt = "'" + limttp + "'";
			} else {
				return null;
			}
		} else {
			for (KupCurtLimt tblimt : limtList) {
				if (limt.length() > 0) {
					limt = limt + ",'" + tblimt.getCmqttp()+ "'";
				} else {
					limt = "'" +tblimt.getCmqttp()+ "'";
				}
			}
		}
		return limt;
	}
	
	/**
	 * 获取组合子账户类型
	 * @author chengen
	 * @param sbactp 子账户类型
	 * @return sbac 子账户组合
	 */
	public static String QrySbattps (String sbactp) {
		String sbac = "";
		List<KupCurtSbac> sbacList = KupCurtSbacDao.selectAll_odb3(sbactp, false);
		if(sbacList.size() < 1){
			return null;
		}else{
			for (KupCurtSbac tbSbac : sbacList){
				if (sbac.length() > 0){
					sbac = sbac + ",'" + tbSbac.getCmsatp()+ "'";
				}else {
					sbac = "'" +tbSbac.getCmsatp()+ "'";
				}
			}
		}
		return sbac;
	}
	
	/**
	 * 计算实际积数
	 * @param curram 当前积数
	 * @param onlnbl 账户余额
	 * @param currdt 当前日期
	 * @param lsamdt 积数变更日期
	 * @return 积数
	 * 
	 * 
	 * 计提/结息时由于积数是在账户余额变更时才变动，所有要计算出实际积数值
	 * 传入当前积数、账户余额、积数变更日期和当前日期
	 * 
	 * 
	 * 在账户余额变更时计算积数
	 * 传入当前积数、上日账户余额、积数变更日期和应入账日期
	 * 
	 */
	public static BigDecimal calRealTotalAmt(BigDecimal curram,BigDecimal onlnbl, String currdt,String lsamdt){

		//modify by chenlk  2016-12-1 删除积数不能为负数的校验
//		if(CommUtil.compare(curram, BigDecimal.ZERO) < 0)
//			throw DpModuleError.DpstProd.E0010("当前积数不能为负数");
		if(CommUtil.compare(onlnbl, BigDecimal.ZERO) < 0)
			onlnbl=BigDecimal.ZERO;
		
		BigDecimal days = new BigDecimal(DateTools2.calDays(lsamdt, currdt, 0, 0));
		if(CommUtil.compare(days, BigDecimal.ZERO) < 0)
			throw DpModuleError.DpstComm.BNAS1584();
		return curram.add(onlnbl.multiply(days));
	}
}
