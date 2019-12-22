package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdl;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdlDao;
import cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcpdt.Input.Cpdtif;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class adcpdt {
	private static final BizLog bizlog = BizLogUtil.getBizLog(adcpdt.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：超额优惠明细新增
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void adcpdt( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcpdt.Input input){
		bizlog.method("adcpdt begin >>>>>>");
		
		if (input.getCpdtif().size() <= 0) {
			throw FeError.Chrg.BNASF033();
		}
		
		for (Cpdtif cpdtif : input.getCpdtif()) {
			String smfacd = cpdtif.getSmfacd();//超额优惠代码
			BigDecimal smstrt = cpdtif.getSmstrt();//超额起点
			String efctdt = cpdtif.getEfctdt(); //生效日期
			String inefdt = cpdtif.getInefdt(); //失效日期
			String sTime = CommTools.getBaseRunEnvs().getTrxn_date();
			BigDecimal smblup = cpdtif.getSmblup();
			
			List<KcpFavoSmdf> listKcpFavoSmdf = FeDiscountDao.sel_all_kcp_favo_smdf(smfacd, CommTools.getBaseRunEnvs().getTrxn_branch(), false);
			if(CommUtil.isNull(smfacd)){
				throw FeError.Chrg.BNASF028();
			}
			if(CommUtil.isNull(smstrt)){
				throw FeError.Chrg.BNASF023();
			}
			if(CommUtil.isNull(listKcpFavoSmdf)) {
				throw FeError.Chrg.BNASF027();
			}
			if (CommUtil.isNull(cpdtif.getSmfapc())) {
				throw FeError.Chrg.BNASF025();
			}
			if (CommUtil.compare(cpdtif.getSmfapc(), BigDecimal.ZERO) < 0 || CommUtil.compare(cpdtif.getSmfapc(), BigDecimal.valueOf(100)) > 0) {
				throw FeError.Chrg.BNASF024();
			}
			
				if (CommUtil.compare(smblup, BigDecimal.ZERO) < 0) 
					throw FeError.Chrg.BNASF186();
			
			if (CommUtil.isNull(efctdt)) {
				throw FeError.Chrg.BNASF207();
			}
			if (CommUtil.isNull(inefdt)) {
				throw FeError.Chrg.BNASF212();
			}

			if(CommUtil.compare(smstrt, BigDecimal.ZERO) < 0){
				throw FeError.Chrg.BNASF022();
			}

			if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
				throw FeError.Chrg.BNASF210();
			}
			if (DateUtil.compareDate(efctdt, sTime) <= 0) {
				throw FeError.Chrg.BNASF204();
			}
			for(KcpFavoSmdf tblKcpFavoSmdf : listKcpFavoSmdf){
				if(CommUtil.compare(efctdt, tblKcpFavoSmdf.getEfctdt()) < 0 || CommUtil.compare(inefdt, tblKcpFavoSmdf.getInefdt()) > 0){
					throw FeError.Chrg.BNASF397();
				}
			}
						
			KcpFavoSmdl tblFavosmdl = SysUtil.getInstance(KcpFavoSmdl.class);
			
			tblFavosmdl.setSmfacd(smfacd); //超额优惠代码
			tblFavosmdl.setSmstrt(smstrt); //超额起点
			tblFavosmdl.setSmfapc(cpdtif.getSmfapc()); //优惠比例
			tblFavosmdl.setSmblup(smblup); //累积交易额限制
			tblFavosmdl.setExplan(cpdtif.getExplan()); //备注
			tblFavosmdl.setEfctdt(efctdt);//生效日期
			tblFavosmdl.setInefdt(inefdt); //失效日期
			KcpFavoSmdlDao.insert(tblFavosmdl);
			
			//增加审计
			ApDataAudit.regLogOnInsertParameter(tblFavosmdl);
							      
		}
		
	}
}
