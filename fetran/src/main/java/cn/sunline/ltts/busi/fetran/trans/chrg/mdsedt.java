package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeSceneDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDetl;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDetlDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_MODULE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mdsedt {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mdsedt.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：场景代码明细修改
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void mdsedt( final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdsedt.Input input,final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdsedt.Property property) {
		
		bizlog.method("mdsedt begin >>>>>>");
		
		String scencd = input.getScencd(); // 场景代码
		E_MODULE module = E_MODULE.CG; // 默认费用
		String scends = input.getScends(); // 场景名称
		String evetcd = input.getEvetcd(); // 事件编号
		String dimecg = input.getDimecg(); // 维度类别
		String dimevl = input.getDimevl(); // 维度值
		String remark = input.getRemark(); // 备注
		
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}
		if (CommUtil.isNull(FeDimeDao.selone_kcp_dime_cg(dimecg, false))) {
			throw FeError.Chrg.BNASF250();
		}
		if (CommUtil.isNull(FeDimeDao.selone_evl_dime(dimevl, dimecg, false))) {
			throw FeError.Chrg.BNASF258();
		}
		
		KcpScevDetl tblkcp_scev_detl = KcpScevDetlDao.selectOne_odb1(
				scencd, module, evetcd, dimecg, false);
		if (CommUtil.isNotNull(tblkcp_scev_detl)) {
			if (CommUtil.compare(scencd, tblkcp_scev_detl.getScencd()) == 0
					&& CommUtil.compare(scends, tblkcp_scev_detl.getScends()) == 0
					&& CommUtil.compare(evetcd, tblkcp_scev_detl.getEvetcd()) == 0
					&& CommUtil.compare(dimecg, tblkcp_scev_detl.getDimecg()) == 0
					&& CommUtil.compare(dimevl, tblkcp_scev_detl.getDimevl()) == 0
					&& CommUtil.compare(remark, tblkcp_scev_detl.getRemark()) == 0) {
				throw FeError.Chrg.BNASF317();
			}
			KcpScevDetl oldEntity = CommTools.clone(KcpScevDetl.class,
					tblkcp_scev_detl);
			
			//明细登记簿维护
			Long num = (long) 0; //序列
			
			if (CommUtil.compare(scends, tblkcp_scev_detl.getScends()) != 0) { // 场景名称
				if(FeSceneDao.selna_kcp_scev_detl(scends, false).size() > 0){
					throw FeError.Chrg.BNASF018();
				}
				num++;
				tblkcp_scev_detl.setScends(scends);
			}

			if (CommUtil.compare(evetcd, tblkcp_scev_detl.getEvetcd()) != 0) { // 事件编号
				num++;

				tblkcp_scev_detl.setEvetcd(evetcd);
			}

			if (CommUtil.compare(dimevl, tblkcp_scev_detl.getDimevl()) != 0) { // 维度值
				num++;
				tblkcp_scev_detl.setDimevl(dimevl);
			}

			if (CommUtil.compare(remark, tblkcp_scev_detl.getRemark()) != 0) { // 备注
				num++;
				tblkcp_scev_detl.setRemark(remark);
			}
			KcpScevDetlDao.updateOne_odb1(tblkcp_scev_detl);
			ApDataAudit.regLogOnUpdateParameter(oldEntity, tblkcp_scev_detl);
			
			//20161026 add 同步场景名称、备注
			List<KcpScevDetl> lstkcp_scev_detl = FeSceneDao.selall_kcp_scev_detl_list(scencd, false);
			for(KcpScevDetl tblkcp_scev_detl_scen : lstkcp_scev_detl){
				
				KcpScevDetl oldKcpScevDtlScen = CommTools.clone(KcpScevDetl.class,
						tblkcp_scev_detl_scen);
				if(CommUtil.isNotNull(scends)){
					tblkcp_scev_detl_scen.setScends(scends); //场景名称
				}
				if(CommUtil.isNotNull(remark)){
					tblkcp_scev_detl_scen.setRemark(remark); //备注
				}else{
					//若没传则置空
					tblkcp_scev_detl_scen.setRemark("");
				}
				
				//更新场景代码明细
				KcpScevDetlDao.updateOne_odb1(tblkcp_scev_detl_scen);
				ApDataAudit.regLogOnUpdateParameter(oldKcpScevDtlScen, tblkcp_scev_detl_scen);
			}
			
		} else {
			throw FeError.Chrg.BNASF152();
		}
		
		bizlog.method("mdsedt end >>>>>>");
	}
}
