package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeCodeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrg;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdfDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmexDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class adcpal {
	private static final BizLog bizlog = BizLogUtil.getBizLog(adcpal.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：超额优惠解析新增
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void adcpal( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcpal.Input input){
		bizlog.method("adcpal begin >>>>>>");
		String chrgcd = input.getChrgcd();//费种代码
		String smfacd = input.getSmfacd();//超额优惠代码
		String crcycd = BusiTools.getDefineCurrency(); //币种 默认人民币
		String efctdt = input.getEfctdt(); //生效日期
		String inefdt = input.getInefdt(); //失效日期
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date();//交易日期
		Long num = new Long((long) 0);//序号
		
		if(CommUtil.isNull(chrgcd)){
			throw FeError.Chrg.BNASF076();
		}
		if(CommUtil.isNull(smfacd)){
			throw FeError.Chrg.BNASF028();
		}
		if (CommUtil.isNull(efctdt)) {
			throw FeError.Chrg.BNASF207();
		}
		if (CommUtil.isNull(inefdt)) {
			throw FeError.Chrg.BNASF212();
		}
		if (CommUtil.isNull(input.getDimevl())) {
			throw FeError.Chrg.BNASF259();
		}		
		if (CommUtil.isNull(input.getFadmtp())) {
			throw FeError.Chrg.BNASF251();
		}
		if (CommUtil.isNull(FeDimeDao.selone_evl_dime(input.getDimevl(), input.getFadmtp(), false))) {
			throw FeError.Chrg.BNASF252();
		}
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}
		if (DateUtil.compareDate(efctdt, sTime) <= 0) {
			throw FeError.Chrg.BNASF204();
		}
		
		if (CommUtil.isNotNull(FeDiscountDao.selone_kcp_favo_smex(chrgcd, smfacd, input.getDimevl(), input.getFadmtp(), false))) {
			throw FeError.Chrg.BNASF098();
		}
		
		KcpChrg kcpChrg = FeCodeDao.selone_kcp_chrg(chrgcd, false);
		if (CommUtil.isNull(kcpChrg)) {
			throw FeError.Chrg.BNASF074();
		} else {
		}
		
		KcpFavoSmdf kcpFavoSmdf = KcpFavoSmdfDao.selectOne_odb1(smfacd, false);
		if (CommUtil.isNull(kcpFavoSmdf)) {
			throw FeError.Chrg.BNASF027();
		} else {
			//20161108 mod 生效日期与定义的失效日期进行比较
			/*if(DateUtil.compareDate(efctdt, kcpFavoSmdf.getInefdt()) > 0){
				throw FeError.Chrg.E9999("生效日期必须小于超额优惠定义的失效日期");
			}*/
			if (CommUtil.compare(efctdt, kcpFavoSmdf.getEfctdt()) < 0
					|| CommUtil.compare(inefdt, kcpFavoSmdf.getInefdt()) > 0) {
				throw FeError.Chrg.BNASF054();
			}
		}
		
		KcpFavoSmex tblFavosmex = SysUtil.getInstance(KcpFavoSmex.class);
		
		tblFavosmex.setChrgcd(chrgcd); //费种代码
		tblFavosmex.setSmfacd(smfacd);//超额优惠代码
		tblFavosmex.setCrcycd(crcycd); //币种
		tblFavosmex.setFadmtp(input.getFadmtp()); //维度类别
		tblFavosmex.setDimevl(input.getDimevl()); //维度值
		tblFavosmex.setEfctdt(efctdt); //生效日期
		tblFavosmex.setInefdt(inefdt); //失效日期
		tblFavosmex.setExplan(input.getExplan());
		KcpFavoSmexDao.insert(tblFavosmex);
		
		//增加审计
		ApDataAudit.regLogOnInsertParameter(tblFavosmex);
		
        
	}
}
