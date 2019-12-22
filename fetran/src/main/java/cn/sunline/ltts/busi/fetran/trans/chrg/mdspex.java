package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSpex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSpexDao;
import cn.sunline.ltts.busi.iobus.servicetype.IoPbTableSvr;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubBrch;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class mdspex {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mdspex.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：单一优惠管理修改
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void mdspex( final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdspex.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdspex.Property property){
		bizlog.method("mdspex begin >>>>>>");
		String chrgcd = input.getChrgcd(); //费种代码
		String brchno = input.getBrchno(); //机构号
		String efctdt = input.getEfctdt(); //生效日期
		String inefdt = input.getInefdt(); //失效日期
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		
		if (CommUtil.isNull(chrgcd)) {
			throw FeError.Chrg.BNASF076();
		}
		
		if (CommUtil.isNull(brchno)) {
			throw FeError.Chrg.BNASF131();
		}
		
		//判断输入机构是否存在
		IoPbTableSvr tbl = CommTools.getRemoteInstance(IoPbTableSvr.class);
		IoPbKubBrch tblkub_brch = tbl.kub_brch_selectOne_odb1(brchno, false);
		if(CommUtil.isNull(tblkub_brch)){
			throw FeError.Chrg.BNASF130();
		}
		
		if (CommUtil.isNull(input.getFasttp())) {
			throw FeError.Chrg.BNASF042();
		}
		
		if (CommUtil.isNull(input.getFastam())) {
			throw FeError.Chrg.BNASF040();
		}
		
		if (CommUtil.compare(input.getFastam(), BigDecimal.ZERO) < 0) {
			throw FeError.Chrg.BNASF039();
		}
		
		if (CommUtil.isNull(input.getFatype())) {
			throw FeError.Chrg.BNASF251();
		}
		
		if (CommUtil.isNull(input.getFavoir())) {
			throw FeError.Chrg.BNASF297();
		}
		
		if (CommUtil.compare(input.getFavoir(), BigDecimal.ZERO) <= 0 
				||	CommUtil.compare(input.getFavoir(), BigDecimal.TEN.multiply(BigDecimal.TEN)) > 0) {
			throw FeError.Chrg.BNASF296();
		}
		
		if (CommUtil.isNull(input.getFavalu())) {
			throw FeError.Chrg.BNASF259();
		}
		
		if (CommUtil.isNull(efctdt)) {
			throw FeError.Chrg.BNASF207();
		}
		
		if (CommUtil.isNull(inefdt)) {
			throw FeError.Chrg.BNASF212();
		}
		
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}
		
		if (DateUtil.compareDate(inefdt, sTime) <= 0) {
			throw FeError.Chrg.BNASF209();
		}
		
		//省县两级参数管理员均有操作权限，县级行社参数管理员只允许新增本行社
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), BusiTools.getBusiRunEnvs().getCentbr()) &&
				!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), input.getBrchno())) {
			throw FeError.Chrg.BNASF158();
		}
		
		KcpFavoSpex tblFavospex =  KcpFavoSpexDao.selectOne_odb1(chrgcd, brchno, input.getFasttp(), input.getFastam(), input.getFatype(), input.getFavalu(), false);
		
		if(CommUtil.isNull(tblFavospex)){
			throw FeError.Chrg.BNASF037();
		}
		
		if (CommUtil.isNotNull(tblFavospex)) {
			
		    if(!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), BusiTools.getBusiRunEnvs().getCentbr()) &&
		    		!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), tblFavospex.getBrchno())){
		    	throw FeError.Chrg.BNASF128();
		    }
			
			//已生效单一优惠优惠比例、失效日期不能修改
			if(CommUtil.compare(sTime, tblFavospex.getEfctdt()) >= 0 && CommUtil.compare(sTime, tblFavospex.getInefdt()) < 0){
				if(CommUtil.compare(efctdt, tblFavospex.getEfctdt()) != 0
						||CommUtil.compare(input.getFavoir(), tblFavospex.getFavoir())!=0){
					throw FeError.Chrg.BNASF292();
				}
			}else if(CommUtil.compare(tblFavospex.getEfctdt(), sTime) > 0){ //未生效
				if (DateUtil.compareDate(efctdt, sTime) <= 0) {
					throw FeError.Chrg.BNASF204();
				}
			}
			if(CommUtil.compare(sTime, tblFavospex.getInefdt()) >= 0 ){
				throw FeError.Chrg.BNASF289();
			}
			if(CommUtil.compare(input.getFavoir(), tblFavospex.getFavoir()) == 0
					&&CommUtil.compare(efctdt, tblFavospex.getEfctdt()) == 0
					&&CommUtil.compare(inefdt, tblFavospex.getInefdt()) == 0){
				throw FeError.Chrg.BNASF317();
			}
			
			KcpFavoSpex oldEntity = CommTools.clone(KcpFavoSpex.class,
					tblFavospex);
			
			//明细登记簿维护
			Long num = (long) 0; //序列
			
			if(CommUtil.compare(input.getFavoir(), tblFavospex.getFavoir()) != 0){ //优惠比例
			  num++;
			  tblFavospex.setFavoir(input.getFavoir());
			}
			if(CommUtil.compare(efctdt, tblFavospex.getEfctdt()) != 0){ //生效日期
			  num++;
			  tblFavospex.setEfctdt(efctdt);
			}
			if(CommUtil.compare(inefdt, tblFavospex.getInefdt()) != 0){ //失效日期
			  num++;
			  tblFavospex.setInefdt(inefdt);	
			}
			KcpFavoSpexDao.updateOne_odb1(tblFavospex);  
			ApDataAudit.regLogOnUpdateParameter(oldEntity, tblFavospex);
	
		} else {
			throw FeError.Chrg.BNASF152();
		}
	}
}
