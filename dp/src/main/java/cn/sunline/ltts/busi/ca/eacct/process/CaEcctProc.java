package cn.sunline.ltts.busi.ca.eacct.process;

import java.math.BigDecimal;
import java.util.Calendar;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctBlHtyDtl;
import cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctBlHtyDtlOut;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;

public class CaEcctProc {
	
//	private static final BizLog bizLog = BizLogUtil.getBizLog(CaEcctProc.class);
	
	/**
	 * 查询前检查
	 * @param ecctno
	 * @param trandt
	 * @param crcycd
	 */
	public static void checkBeforePrc(String ecctno, String trandt,
			String crcycd){
		if (CommUtil.isNull(ecctno)) {
			throw DpModuleError.DpstProd.BNAS0926();
		}

		if (CommUtil.isNull(crcycd)) {
			throw CaError.Eacct.BNAS0663();
		}

		// 如果日期为空，默认为当前日期
		if (CommUtil.isNull(trandt)) {
			trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		} else {
			BusiTools.checkEffectDate(trandt);
		}
		//日期不能大于当前日期
		if (DateUtil.compareDate(trandt, CommTools.getBaseRunEnvs().getTrxn_date()) > 0) {
			throw CaError.Eacct.BNAS0606();
		}

		// 判断电子账号是否存在
		KnaCust tblKna_cust = KnaCustDao.selectOne_odb1(ecctno, false);
		if (CommUtil.isNull(tblKna_cust)) {
			throw CaError.Eacct.BNAS0750();
		}
		if (tblKna_cust.getAcctst() == E_ACCTST.CLOSE) {
			throw CaError.Eacct.BNAS0906();
		}
	}
	
	/**
	 * 返回固定天数的余额
	 * @param ecctno
	 * @param trandt
	 * @param crcycd
	 * @param day
	 * @return
	 */
	public static EacctBlHtyDtlOut eacctBlHtyList(String ecctno,String trandt,String crcycd,int day){
		
		EacctBlHtyDtlOut out = SysUtil.getInstance(EacctBlHtyDtlOut.class);
		BigDecimal tranam = BigDecimal.ZERO;
		String acctdt = null; 							//账务日期
//		String nextdt = null; 							//上一账务日期
		String today = CommTools.getBaseRunEnvs().getTrxn_date();
		String lastdt = CommTools.getBaseRunEnvs().getLast_date();
		/*List<h_kna_acct> lsth_kna_acct = null;
		List<h_kna_fxac> lsth_kna_fxac = null;*/
		for(int i=day;i>=0;i--){
			acctdt = DateUtil.dateAdd(Calendar.DATE, -i, trandt);
//			nextdt = DateUtil.dateAdd(Calendar.DATE, -i+1, trandt);
			tranam = BigDecimal.ZERO;
			EacctBlHtyDtl dtl = SysUtil.getInstance(EacctBlHtyDtl.class);
			
			//当日余额
			if(CommUtil.equals(acctdt,today)){
				BigDecimal fdbl = EacctMainDao.selSumFundBl(ecctno, crcycd, false);			//基金余额
//				BigDecimal holdbl=DpFrozTools.getFrozBala(E_FROZOW.AUACCT, ecctno);			//冻结余额
				BigDecimal holdbl=BigDecimal.ZERO;			//冻结余额
				DpAcctSvcType dpAcctSvc = SysUtil.getInstance(DpAcctSvcType.class);
				tranam = dpAcctSvc.getProductBal(ecctno, crcycd, false);  //可用余额
				tranam = tranam.add(fdbl).add(holdbl);
			}else if(CommUtil.equals(lastdt,acctdt)){
				tranam = getBal(ecctno,crcycd);
			}else{
				/*lsth_kna_acct = H_kna_acctDao.selectAll_odb4(nextdt , ecctno, crcycd, false);
				lsth_kna_fxac = H_kna_fxacDao.selectAll_odb4(nextdt , ecctno, crcycd, false);
				//活期历史余额
				for(h_kna_acct acct:lsth_kna_acct){
					//最后更新日期等于账务日期
					if(CommUtil.equals(acct.getUpbldt(), acct.getAcctdt())){
						tranam = tranam.add(acct.getLastbl());
					}else{
						tranam = tranam.add(acct.getOnlnbl());
					}
				}
				
				//定期历史余额
				for(h_kna_fxac fxac:lsth_kna_fxac){
					//最后更新日期等于账务日期
					if(CommUtil.equals(fxac.getUpbldt(), fxac.getAcctdt())){
						tranam = tranam.add(fxac.getLastbl());
					}else{
						tranam = tranam.add(fxac.getOnlnbl());
					}
				}*/
			}
			dtl.setTrandt(acctdt);			//交易日期
			dtl.setTranam(tranam);			//每天余额=活期+定期
			out.getTrandl().add(dtl);
			out.setEcctno(ecctno);
		}
		return out;
	}
	
	public static BigDecimal getBal(String custac,String crcycd){
//		String today = CommTools.getBaseRunEnvs().getTrxn_date();
		BigDecimal bal = BigDecimal.ZERO;
		//查询活期产品资金产品池
		/*List<kup_dptd> dptds = Kup_dptdDao.selectAll_odb1(CommTools.getBaseRunEnvs().getBusi_org_id(), false);
		for(kup_dptd dptd : dptds){
			String prodcd = dptd.getProdcd();
			if(E_PRODTP.DEPO == dptd.getProdtp()){
				//存款
				//查询活期账户
				List<kna_acct> accts = Kna_acctDao.selectAll_odb3(crcycd ,prodcd, custac, false);
				for(kna_acct acct : accts){
					if(!CommUtil.equals(acct.getUpbldt(),today)){
						bal = bal.add(acct.getOnlnbl());
					}else{
						bal = bal.add(acct.getLastbl());
					}
				}
				//查询定期账户
				List<kna_fxac> fxacs = Kna_fxacDao.selectAll_odb2(crcycd ,prodcd, custac, false);
				for(kna_fxac fxac : fxacs){
					if(!CommUtil.equals(fxac.getUpbldt(),today)){
						bal = bal.add(fxac.getOnlnbl());
					}else{
						bal = bal.add(fxac.getLastbl());
					}
				}
			}
		}*/
		return bal;
	}
}
