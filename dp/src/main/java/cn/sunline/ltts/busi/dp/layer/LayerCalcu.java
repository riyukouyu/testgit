package cn.sunline.ltts.busi.dp.layer;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInstCalc;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbInRaSelSvc;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CAINRD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.edsp.base.lang.Options;

/**
 * 分层账户利率调整日终分段处理
 * */
public class LayerCalcu {
	
//	private static final BizLog bizlog = BizLogUtil.getBizLog(LayerCalcu.class);

	/**
	 * 
	 * */
	
	
	public static BigDecimal getAvgAmt(KnbAcin KnbAcin, String begin, String end){
		
		BigDecimal avgAmt = BigDecimal.ZERO;
//		int days = 0;
//		if(KnbAcin.getTxbebs() == E_INBEBS.STADSTAD){
//			days = DateTools2.calDays(begin, end, 1, 0);
//		}else{
//			days = DateTools2.calDays(begin, end, 0, 0);
//		}
		
		return avgAmt;
	}
	
	public static BigDecimal getLayerSmt(KnbAcin KnbAcin, Options<KubInrt> inrts, BigDecimal onlnbl){

		
		return BigDecimal.ZERO;
	}
	
	public static BigDecimal calcIntr(BigDecimal smt, BigDecimal rate, KnbAcin KnbAcin){
		
		BigDecimal interest = BigDecimal.ZERO;
		if(KnbAcin.getHutxfg() == E_CAINRD.SQ){ //舍弃角分，即舍弃小数点
			smt = smt.setScale(0, BigDecimal.ROUND_DOWN);
		}
		IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
//		if(KnbAcin.getTxbebs() == E_INBEBS.REAL365){
//			interest = smt.multiply(rate).divide(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(365),50, BigDecimal.ROUND_HALF_UP);
//		}else if(KnbAcin.getTxbebs() == E_INBEBS.REALSTAD){
//			interest = smt.multiply(rate).divide(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(360),50, BigDecimal.ROUND_HALF_UP);
//		}else if(KnbAcin.getTxbebs() == E_INBEBS.STADSTAD){
//			interest = smt.multiply(rate).divide(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(360),50, BigDecimal.ROUND_HALF_UP);
//		}
		
		interest = pbpub.countInteresRateByBase(rate, smt);
		return interest;
	}
	
	/**
	 * 功能:计算利息 利息税
	 * */
	public static DpInstCalc calc(BigDecimal smt, KnbIndl indl, KnbAcin KnbAcin){
		BigDecimal interest = BigDecimal.ZERO;
		BigDecimal intrtax = BigDecimal.ZERO;
		IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
		if(KnbAcin.getHutxfg() == E_CAINRD.SQ){ //舍弃角分，即舍弃小数点
			smt = smt.setScale(0, BigDecimal.ROUND_DOWN);
		}
		
		DpInstCalc rtn = SysUtil.getInstance(DpInstCalc.class);
		
		interest = pbpub.countInteresRateByBase(indl.getCuusin(), smt);
		if(KnbAcin.getTxbefg() == E_YES___.YES){ //是否计税标志
		//	获取利息税税率,计算利息税
			intrtax= interest.multiply(indl.getCatxrt());
			
		}
		rtn.setInstam(interest);
		rtn.setIntxam(intrtax);
		return rtn;
		
	}
	/**
	 * 功能:计算正常部分利息 利息税 
	 * */
	public static DpInstCalc calc(BigDecimal smt, BigDecimal rate, KnbAcin KnbAcin){
		BigDecimal interest = BigDecimal.ZERO;
		BigDecimal intrtax = BigDecimal.ZERO;
		BigDecimal  curIntTx =  BigDecimal.ZERO;//当前利息税
		IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
		if(KnbAcin.getHutxfg() == E_CAINRD.SQ){ //舍弃角分，即舍弃小数点
			smt = smt.setScale(0, BigDecimal.ROUND_DOWN);
		}
		
		DpInstCalc rtn = SysUtil.getInstance(DpInstCalc.class);
		
		interest = pbpub.countInteresRateByBase(rate, smt);
		if(KnbAcin.getTxbefg() == E_YES___.YES){ //是否计税标志		
			//税率编码对应的最新税率
				if(CommUtil.isNotNull(KnbAcin.getTaxecd())){
					
					curIntTx = SysUtil.getInstance(IoPbInRaSelSvc.class).inttxRate(KnbAcin.getTaxecd()).getTaxrat();
				}
				if(CommUtil.isNull(curIntTx)){
					curIntTx = BigDecimal.ZERO;
				}			
			
				intrtax= interest.multiply(curIntTx);
				
		}
		rtn.setInstam(interest);
		rtn.setIntxam(intrtax);
		return rtn;
		
	}	
	
}
