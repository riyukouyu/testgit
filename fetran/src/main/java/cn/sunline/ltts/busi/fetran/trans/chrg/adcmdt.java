package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdt;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdtDao;
import cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcmdt.Input.Listfm;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_CUFETP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class adcmdt {
	private static final BizLog bizlog = BizLogUtil.getBizLog(adcmdt.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：计费公式明细表新增
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void adcmdt( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcmdt.Input input,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcmdt.Property property){
		bizlog.method("adcmdt begin >>>>>>");
		Long num = (long)0;//序号
		
		if(input.getListfm().size() <= 0){
			throw FeError.Chrg.BNASF147();
		}
		
		for (Listfm listFm : input.getListfm()) {
			String chrgfm = listFm.getChrgfm(); //计费公式代码
			String brchno = listFm.getBrchno(); //机构号
			BigDecimal limiam = listFm.getLimiam(); //档次区间下限
			String crcycd = listFm.getCrcycd(); //币种
			E_CUFETP cufetp = listFm.getCufetp(); //计费类型
			BigDecimal cgmxam = listFm.getCgmxam();	//最高金额
			BigDecimal cgmnam = listFm.getCgmnam();	//最低金额
			String remark = listFm.getRemark(); //备注
			
		    if (CommUtil.isNull(chrgfm)) {
		        throw FeError.Chrg.BNASF142();
		    }
		    
		    if(CommUtil.isNull(crcycd)){
		    	throw FeError.Chrg.BNASF156();
		    }
		    
		    if (CommUtil.isNull(brchno)) {
		        throw FeError.Chrg.BNASF131();
		    }
		    
		    if (CommUtil.isNull(limiam) || CommUtil.compare(limiam, BigDecimal.ZERO) < 0) {
		        throw FeError.Chrg.BNASF046();
		    }
		    
		    if(CommUtil.isNull(cufetp)){
		    	throw FeError.Chrg.BNASF149();
		    }
		    
		    if(CommUtil.compare(cufetp, E_CUFETP.R) == 0){
		    	
		    	if(CommUtil.isNull(listFm.getChrgrt())){
		    		throw FeError.Chrg.BNASF135();
		    	}
		    	
		    	if(CommUtil.compare(listFm.getChrgrt(), BigDecimal.ZERO) < 0 
		    			|| CommUtil.compare(listFm.getChrgrt(), BigDecimal.TEN.multiply(BigDecimal.TEN)) > 0){
		 	    	throw FeError.Chrg.BNASF136();
		 	    }
		    	
			    if(CommUtil.isNull(cgmxam)){
			    	throw FeError.Chrg.BNASF349();
			    }
			    
			    if(CommUtil.isNull(cgmnam)){
			    	throw FeError.Chrg.BNASF343();
			    }
			    
			    if(CommUtil.compare(cgmnam, BigDecimal.ZERO) < 0) {
			    	throw FeError.Chrg.BNASF342();
			    }
			    
			    if(CommUtil.compare(cgmxam, cgmnam) <=0 ){
			    	throw FeError.Chrg.BNASF350();
			    }
			    
		    }
		    
		   if(CommUtil.compare(cufetp, E_CUFETP.S) == 0 || CommUtil.compare(cufetp, E_CUFETP.N) == 0){
			  
			   if(CommUtil.isNull(listFm.getPecgam())){
				   throw FeError.Chrg.BNASF137();
			   }
			   
			   if(CommUtil.compare(listFm.getPecgam(), BigDecimal.ZERO) < 0){
				   throw FeError.Chrg.BNASF138();
			   }
		   }
		   
		   //判断传入机构是否存在
		   IoSrvPbBranch brchSvrType = SysUtil.getInstance(IoSrvPbBranch.class);
		   try{
			   IoBrchInfo brchinfo = brchSvrType.getBranch(brchno);
		   }catch(Exception e){
			   throw FeError.Chrg.E9999(e.getMessage());
		   }
			

			//省县两级参数管理员均有操作权限，县级行社参数管理员只允许新增本行社
			if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), property.getBrchno()) &&
					!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), listFm.getBrchno())) {
				throw FeError.Chrg.BNASF158();
			}
		   
			KcpChrgFmdt tblChrgfmdt = SysUtil.getInstance(KcpChrgFmdt.class);
			
			tblChrgfmdt.setChrgfm(chrgfm); //计费公式代码
			tblChrgfmdt.setBrchno(brchno); //机构号
			tblChrgfmdt.setCrcycd(crcycd); //币种
			tblChrgfmdt.setLimiam(limiam); //档次区间下限
			tblChrgfmdt.setCufetp(cufetp); //计费类型
			tblChrgfmdt.setChrgrt(listFm.getChrgrt()); //计费比例
			tblChrgfmdt.setPecgam(listFm.getPecgam()); //计费单价
			tblChrgfmdt.setCgmnam(cgmnam); //最低金额
			tblChrgfmdt.setCgmxam(cgmxam); //最高金额
			tblChrgfmdt.setRemark(remark); //备注
			KcpChrgFmdtDao.insert(tblChrgfmdt);
			
			//增加审计
			ApDataAudit.regLogOnInsertParameter(tblChrgfmdt);
			
		}
		
	}
}
