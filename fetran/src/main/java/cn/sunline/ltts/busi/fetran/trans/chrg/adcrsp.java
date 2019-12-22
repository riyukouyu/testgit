package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrg;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgDvid;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgDvidDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class adcrsp {
	private static final BizLog bizlog = BizLogUtil.getBizLog(adcrsp.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：新增分润管理
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void adcrsp( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcrsp.Input input,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcrsp.Property property){
		
		bizlog.method("adcrsp begin >>>>>>");
		
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}
		
		String chrgcd = input.getChrgcd(); //费种代码
		BigDecimal indvrt = input.getIndvrt();//转入行分成比率
		BigDecimal oudvrt = input.getOudvrt();//转出行分成比率
		BigDecimal trdvrt = input.getTrdvrt();//交易行分成比率
		BigDecimal spavrt = input.getSpavrt();//备用行分成比率
		String crcycd = input.getCrcycd(); //币种
		String efctdt = input.getEfctdt(); //生效日期
		String inefdt = input.getInefdt(); //失效日期
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		
		if(CommUtil.isNull(chrgcd)){
			throw FeError.Chrg.BNASF076();
		}
		
		KcpChrg tblKcpChrg = KcpChrgDao.selectOne_odb1(chrgcd, BusiTools.getDefineCurrency(), false);
		if(CommUtil.isNull(tblKcpChrg)){
			throw FeError.Chrg.BNASF074();
		}
		
		if(CommUtil.isNull(crcycd)){
			throw FeError.Chrg.BNASF156();
		}
		
		
		if(CommUtil.compare(indvrt, BigDecimal.ZERO) < 0){
			throw FeError.Chrg.BNASF222();
		}
		
		if(CommUtil.compare(oudvrt, BigDecimal.ZERO) < 0){
			throw FeError.Chrg.BNASF090();
		}
		
		if(CommUtil.compare(trdvrt, BigDecimal.ZERO) < 0){
			throw FeError.Chrg.BNASF354();
		}
		
		if(CommUtil.compare(spavrt, BigDecimal.ZERO) < 0){
			throw FeError.Chrg.BNASF355();
		}
		
		if(CommUtil.isNull(efctdt)){
			throw FeError.Chrg.BNASF207();
		}
		
		if(CommUtil.isNull(inefdt)){
			throw FeError.Chrg.BNASF212();
		}
		BigDecimal sumvrt = indvrt.add(spavrt).add(oudvrt).add(trdvrt);
		if (!CommUtil.equals(sumvrt,BigDecimal.valueOf(100))) {
			throw FeError.Chrg.BNASF089();
		}
		
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}
		if (DateUtil.compareDate(efctdt, trandt) <= 0) {
			throw FeError.Chrg.BNASF204();
		}
		
		//判断记录是否已存在
		List<KcpChrgDvid> dvid = KcpChrgDvidDao.selectAll_odb4(chrgcd, crcycd, false);
		
		for(KcpChrgDvid  chrgdvid : dvid ){
			if (DateUtil.compareDate(chrgdvid.getInefdt(), trandt) >= 0) {
				throw FeError.Chrg.BNASF096();
			}
			
		}
		
		if (CommUtil.isNotNull(dvid)) {
			throw FeError.Chrg.BNASF099();
		}
		
		KcpChrgDvid tblKcpchrgdvid = SysUtil.getInstance(KcpChrgDvid.class);
		tblKcpchrgdvid.setChrgcd(chrgcd);
		tblKcpchrgdvid.setIndvrt(indvrt);
		tblKcpchrgdvid.setCrcycd(crcycd);
		tblKcpchrgdvid.setOudvrt(oudvrt);
		tblKcpchrgdvid.setTrdvrt(trdvrt);
		tblKcpchrgdvid.setSpavrt(spavrt);
		tblKcpchrgdvid.setEfctdt(efctdt);
		tblKcpchrgdvid.setInefdt(inefdt);
		KcpChrgDvidDao.insert(tblKcpchrgdvid);
		
		//增加审计
		ApDataAudit.regLogOnInsertParameter(tblKcpchrgdvid);

	}
}
