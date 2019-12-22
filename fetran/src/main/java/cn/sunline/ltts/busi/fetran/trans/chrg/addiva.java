package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDime;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDimeDao;
import cn.sunline.ltts.busi.fetran.trans.chrg.intf.Addiva.Input.Diminf;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class addiva {
	private static final BizLog bizlog = BizLogUtil.getBizLog(addiva.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：新增维度值管理
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void addiva( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Addiva.Input input,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Addiva.Property property){
		
		bizlog.method("addiva begin >>>>>>");
		
		//判断当前机构是否为省中心机构
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}
		
		String dimecg = input.getDimecg(); //维度类别
		
		
		if (CommUtil.isNull(dimecg)) {
			throw FeError.Chrg.BNASF251();
		}
		
		if(CommUtil.isNull(FeDimeDao.selone_kcp_dime_cg(dimecg, false))) {
			throw FeError.Chrg.BNASF250();
		}
		
		if (input.getDiminf().size() <= 0) {
			throw FeError.Chrg.BNASF261();
		}
		
		for (Diminf diminf : input.getDiminf()) {
			String dimevl = diminf.getDimevl(); //维度值
			String expmsg = diminf.getExpmsg(); //维度值名称
			String seqnum = BusiTools.getSequence("dime_seq", 6);
			
			if (CommUtil.isNull(dimevl)) {
				throw FeError.Chrg.BNASF259();
			}
			
			if (CommUtil.isNull(expmsg)) {
				throw FeError.Chrg.BNASF262();
			}				
			
			if (CommUtil.isNotNull(FeDimeDao.selnam_evl_dime(dimecg, expmsg, false))) {
				throw FeError.Chrg.BNASF263();
			}
			
			KcpScevDime tblKcpscevdime = SysUtil.getInstance(KcpScevDime.class);
			
			if (CommUtil.isNotNull(FeDimeDao.selone_evl_dime(dimevl, dimecg, false))) {
				throw FeError.Chrg.BNASF264();
			}
			
			tblKcpscevdime.setDimecg(dimecg);
			tblKcpscevdime.setSeqnum(seqnum);
			tblKcpscevdime.setDimevl(dimevl);
			tblKcpscevdime.setExpmsg(expmsg);
			
			KcpScevDimeDao.insert(tblKcpscevdime);
			
			//增加审计
			ApDataAudit.regLogOnInsertParameter(tblKcpscevdime);
			
		}
		
	}
}
