package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeSceneDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDefn;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDefnDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_MODULE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mdsedf {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mdsedf.class);

	/**
	 * 
	 * @Title: mdsedf 
	 * @Description: (编辑场景事件) 
	 * @param input
	 * @param property
	 * @author leipeng
	 * @date 2016年7月7日 下午7:53:48 
	 * @version V2.3.0
	 */
	public static void mdsedf( final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdsedf.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdsedf.Property property){
	    String evetcd = input.getEvetcd(); //事件编号
	    String evetna = input.getEvetna(); //事件名称
	    String remark = input.getRemark(); //说明
	    E_MODULE module = E_MODULE.CG;//默认费用
		
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}
		
	    if (CommUtil.isNull(evetcd)) {
	        throw FeError.Chrg.BNASF217();
	    }

	    if (CommUtil.isNull(module)) {
	        throw FeError.Chrg.BNASF188();
	    }
	    
	    if (CommUtil.isNull(evetna)) {
	    	throw FeError.Chrg.BNASF395();
	    }
	    
		
	    KcpScevDefn tblkcp_scev_defn = KcpScevDefnDao.selectOne_odb1(module, evetcd, false);
		
		if (CommUtil.isNotNull(tblkcp_scev_defn)) {
			 if(CommUtil.compare(evetcd, tblkcp_scev_defn.getEvetcd()) == 0 &&
					 CommUtil.compare(evetna, tblkcp_scev_defn.getEvetna()) == 0 &&
					 CommUtil.compare(remark, tblkcp_scev_defn.getRemark()) == 0){
				 throw FeError.Chrg.BNASF317();
			 }
			 KcpScevDefn oldEntity = CommTools.clone(KcpScevDefn.class,
					 tblkcp_scev_defn);
			
			//明细登记簿维护
			Long num = (long) 0; //序列
		  if(CommUtil.compare(evetna, tblkcp_scev_defn.getEvetna()) != 0){ //事件名称
			  if(CommUtil.isNotNull(FeSceneDao.sellna_kcp_scev_defn(evetna, false))){
					throw FeError.Chrg.BNASF218();
				}
			 
			 num++;
			 tblkcp_scev_defn.setEvetna(evetna);
		  }

		  if(CommUtil.compare(remark, tblkcp_scev_defn.getRemark()) != 0){ //备注
			 num++;
			 tblkcp_scev_defn.setRemark(remark);
		  }
		  KcpScevDefnDao.updateOne_odb1(tblkcp_scev_defn);
		  ApDataAudit.regLogOnUpdateParameter(oldEntity, tblkcp_scev_defn);
	  		
		 }else{
			 throw FeError.Chrg.BNASF152();
		 }
	}
}
