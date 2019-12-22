package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeSceneDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDetl;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDetlDao;
import cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adsedt.Input.Sedtif;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_MODULE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class adsedt {
	private static final BizLog bizlog = BizLogUtil.getBizLog(adsedt.class);

	/**
	 * 
	 * @Title: adsedt 
	 * @Description: TODO(增加场景明细) 
	 * @param input
	 * @param property
	 * @author leipeng
	 * @date 2016年7月7日 下午8:56:40 
	 * @version V2.3.0
	 */
	public static void adsedt( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adsedt.Input input,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adsedt.Property property,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adsedt.Output output){
		bizlog.method("adsedt begin >>>>>>");
		String scencd =  "CJ" + BusiTools.getSequence("sedt_seq", 6); //场景代码
		E_MODULE module = E_MODULE.CG; //默认费用
		String scends = input.getScends(); //场景名称
		String evetcd = input.getEvetcd(); //事件编号
		
		String remark = input.getRemark(); //备注
		
		output.setScencd(scencd);
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}
		
		if(CommUtil.isNotNull(FeSceneDao.selna_kcp_scev_detl(scends, false))){
			throw FeError.Chrg.BNASF018();
		}
		
		if (CommUtil.isNull(scencd)) {
			throw FeError.Chrg.BNASF016();
		}
		
		if (CommUtil.isNull(module)) {
			throw FeError.Chrg.BNASF188();
		}
		
		if (CommUtil.isNull(evetcd)) {
			throw FeError.Chrg.BNASF217();
		}
		
		if (CommUtil.isNull(FeSceneDao.selone_kcp_scev_defn(evetcd, false))) {
			throw FeError.Chrg.BNASF216();
		}
		
		if (input.getSedtif().size() <= 0) {
			throw FeError.Chrg.BNASF017();
		}
		
		for (Sedtif sedtif : input.getSedtif()) {
			String dimecg = sedtif.getDimecg(); //维度类别
			String dimevl = sedtif.getDimevl(); //维度值
			
			if (CommUtil.isNull(dimecg)) {
				throw FeError.Chrg.BNASF251();
			}
			if (CommUtil.isNull(dimevl)) {
				throw FeError.Chrg.BNASF259();
			}
			
			long cnt = FeSceneDao.selcnt_kcp_dime(dimecg, false);
			if(cnt == 0){
				throw FeError.Chrg.BNASF250();
			}
			
			if (CommUtil.isNull(FeDimeDao.selone_evl_dime(dimevl, dimecg, false))) {
				throw FeError.Chrg.BNASF258();
			}
									
			KcpScevDetl tblkcp_scev_detl = SysUtil.getInstance(KcpScevDetl.class);
			tblkcp_scev_detl.setScencd(scencd);
			tblkcp_scev_detl.setScends(scends);
			tblkcp_scev_detl.setEvetcd(evetcd);
			tblkcp_scev_detl.setDimecg(dimecg);
			tblkcp_scev_detl.setDimevl(dimevl);
			tblkcp_scev_detl.setRemark(remark);
			tblkcp_scev_detl.setModule(module);
		
			KcpScevDetlDao.insert(tblkcp_scev_detl);
			
			//增加审计
			ApDataAudit.regLogOnInsertParameter(tblkcp_scev_detl);
			
			
		}
		
		
	}

	
}
