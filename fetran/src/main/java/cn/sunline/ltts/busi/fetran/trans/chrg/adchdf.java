package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeCodeDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrg;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_CGPYRV;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_CJSIGN;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_FETYPE;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_ISFAVO;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_LYSPTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class adchdf {
	private static final BizLog bizlog = BizLogUtil.getBizLog(adchdf.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：费种代码定义表新增
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */

	public static void adchdf( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adchdf.Input input,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adchdf.Property property,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adchdf.Output output){
		bizlog.method("adchdf begin >>>>>>");
		
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}
		
		Long num = new Long((long) 0);//序号
		E_CGPYRV cgpyrv = input.getCgpyrv();//费用收付标志
		E_CJSIGN cjsign = input.getCjsign();//县辖维护标志
		String crcycd = input.getCrcycd();//货币代号
		String efctdt = input.getEfctdt(); //生效日期
		String inefdt = input.getInefdt(); //失效日期
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		
		/**
		 * 不为空判断
		 */
		if(CommUtil.isNull(input.getChrgna())){
			throw FeError.Chrg.BNASF081();
		}
		
		//费用收付标志不能为空
		if(CommUtil.isNull(cgpyrv)){
			throw FeError.Chrg.BNASF067();
		}
		
		if(CommUtil.isNull(input.getFetype())){
			throw FeError.Chrg.BNASF070();
		}
		
		//货币代号不能为空
		if(CommUtil.isNull(crcycd)){
			throw FeError.Chrg.BNASF125();
		}
		
		if(CommUtil.isNull(input.getMndecm())){
			throw FeError.Chrg.BNASF346();
		}
		
		if(CommUtil.isNull(input.getCarrtp())){
			throw FeError.Chrg.BNASF241();
		}
		
		if(CommUtil.isNull(input.getLysptp())){
			throw FeError.Chrg.BNASF087();
		}
		
		if(E_LYSPTP.MANUAL == input.getLysptp() || E_LYSPTP.OVAMNT == input.getLysptp()){
			if(CommUtil.isNull(input.getFelytp())){
				throw FeError.Chrg.BNASF227();
			}
		}
		
		if(CommUtil.isNull(input.getIsfavo())){
			throw FeError.Chrg.BNASF220();
		}
		
		if(CommUtil.isNull(input.getFedive())){
			throw FeError.Chrg.BNASF228();
		}
		
		if(CommUtil.isNull(input.getChrgsr())){
			throw FeError.Chrg.BNASF230();
		}
		
		//县辖维护标志不能为空
		if(CommUtil.isNull(cjsign)){
			throw FeError.Chrg.BNASF272();
		}
		
		if(CommUtil.isNull(efctdt)){
			throw FeError.Chrg.BNASF207();
		}
		
		if(CommUtil.isNull(inefdt)){
			throw FeError.Chrg.BNASF212();
		}
		 
		if(CommUtil.isNotNull(FeCodeDao.selall_kcp_chrg_na(input.getChrgna(), false))) {
			throw FeError.Chrg.BNASF082();
		}
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}
		if (DateUtil.compareDate(efctdt, trandt) <= 0) {
			throw FeError.Chrg.BNASF204();
		}
		
		if(input.getIsfavo() == E_ISFAVO.YES){ //允许优惠
			if(CommUtil.isNull(input.getMnfvrt())){
				throw FeError.Chrg.BNASF348();
			}
			if(CommUtil.isNull(input.getMxfvrt())){
				throw FeError.Chrg.BNASF353();
			}
		if(CommUtil.compare(input.getMnfvrt(), BigDecimal.ZERO) < 0 || CommUtil.compare(input.getMxfvrt(), BigDecimal.valueOf(100)) > 0){
			  throw FeError.Chrg.BNASF278();
		  }
		if(CommUtil.compare(input.getMxfvrt(), input.getMnfvrt()) <= 0){
			  throw FeError.Chrg.BNASF351();
		  }
		}
		
//		if(E_ISFAVO.NO == input.getIsfavo()){ //不允许优惠
//			if(CommUtil.isNotNull(input.getMnfvrt()) || CommUtil.isNotNull(input.getMxfvrt())){
//				throw FeError.Chrg.E9999("已勾选不允许优惠，最低/最高优惠比例不能输入");
//			}
//		}
		KcpChrg tblKcpchrg = SysUtil.getInstance(KcpChrg.class);
		
		//定义费种代码
		String chrgcd = "FZ" + input.getCgpyrv() + input.getFetype(); //费种代码
		
		if(E_FETYPE.gb == input.getFetype()){ //01工本费
			chrgcd = chrgcd + BusiTools.getSequence("chrg_gb_seq", 3);
		}else if(E_FETYPE.sx == input.getFetype()){ //02手续费
			chrgcd = chrgcd + BusiTools.getSequence("chrg_sx_seq", 3);
		}else if(E_FETYPE.hh == input.getFetype()){ //03汇划费
			chrgcd = chrgcd + BusiTools.getSequence("chrg_hh_seq", 3);
		}else if(E_FETYPE.yd == input.getFetype()){ //04邮电费
			chrgcd = chrgcd + BusiTools.getSequence("chrg_yd_seq", 3);
		}else if(E_FETYPE.fw == input.getFetype()){ //05服务费
			chrgcd = chrgcd + BusiTools.getSequence("chrg_fw_seq", 3);
		}else if(E_FETYPE.gl == input.getFetype()){ //06管理(年)费
			chrgcd = chrgcd + BusiTools.getSequence("chrg_gl_seq", 3);
		}else if(E_FETYPE.fm == input.getFetype()){ //07罚没费
			chrgcd = chrgcd + BusiTools.getSequence("chrg_fm_seq", 3);
		}else if(E_FETYPE.wy == input.getFetype()){ //08违约费
			chrgcd = chrgcd + BusiTools.getSequence("chrg_wy_seq", 3);
		}else if(E_FETYPE.xf == input.getFetype()){ //80税费
			chrgcd = chrgcd + BusiTools.getSequence("chrg_sf_seq", 3);
		}else if(E_FETYPE.xh == input.getFetype()){ //09信函
			chrgcd = chrgcd + BusiTools.getSequence("chrg_xh_seq", 3);
		}else {
			throw FeError.Chrg.E9999("费种大类类型错误");
		}

		bizlog.debug("费种代码[%s]费种大类[%s]", chrgcd, input.getFetype());
		
		tblKcpchrg.setChrgcd(chrgcd); //费种代码
		tblKcpchrg.setChrgna(input.getChrgna()); //费种代码名称
		tblKcpchrg.setCgpyrv(input.getCgpyrv()); //费种收付标志
		tblKcpchrg.setFetype(input.getFetype()); //费种大类
		tblKcpchrg.setCrcycd(crcycd); //币种 
		tblKcpchrg.setMndecm(input.getMndecm()); //最低小数位数
		tblKcpchrg.setCarrtp(input.getCarrtp()); //四舍五入方式
		tblKcpchrg.setLysptp(input.getLysptp()); //分层方式
		tblKcpchrg.setFelytp(input.getFelytp()); //收费分层取值类型
		tblKcpchrg.setIsfavo(input.getIsfavo()); //是否允许优惠
		tblKcpchrg.setMnfvrt(input.getMnfvrt()); //最低优惠比例
		tblKcpchrg.setMxfvrt(input.getMxfvrt()); //最高优惠比例
		tblKcpchrg.setFedive(input.getFedive()); //收费分润标志
		tblKcpchrg.setChrgsr(input.getChrgsr()); //收费金额来源
		tblKcpchrg.setCjsign(input.getCjsign()); //县辖维护标志
//		tblKcpchrg.setPreway(input.getPreway()); //优惠方式
//		tblKcpchrg.setPrertt(input.getPrertt()); //优惠返还时间
//		tblKcpchrg.setChrgtp(input.getChrgtp()); //收取方式
		tblKcpchrg.setEfctdt(efctdt); //生效日期
		tblKcpchrg.setInefdt(inefdt); //失效日期
		//end
		KcpChrgDao.insert(tblKcpchrg);
		
		ApDataAudit.regLogOnInsertParameter(tblKcpchrg);
		
//		输出
		output.setChrgcd(chrgcd);
	}
	
	

}
