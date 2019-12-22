package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeCodeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeSceneDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgScdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgScdfDao;
import cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adsecd.Input.Secdif;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_MODULE;


public class adsecd {
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：新增场景计费管理
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void adsecd( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adsecd.Input input){
		
		String scencd = input.getScencd();//场景代码
		E_MODULE module = E_MODULE.CG;//默认费用
		
		
		if (input.getSecdif().size() <= 0) {
			throw FeError.Chrg.BNASF001();
		}
		
		for (Secdif secdif : input.getSecdif()) {
			String chrgcd = secdif.getChrgcd();//费种代码
			String seqnum = BusiTools.getSequence("chrgsc_seq", 8); //顺序号
			
			if(CommUtil.isNull(chrgcd)){
				throw FeError.Chrg.BNASF076();
			}
			if(CommUtil.isNull(scencd)){
				throw FeError.Chrg.BNASF016();
			}
			if (CommUtil.isNull(FeCodeDao.selone_kcp_chrg(chrgcd, false))) {
				throw FeError.Chrg.BNASF074();
			}
			
			if ((FeSceneDao.selall_kcp_scev_detl_list(scencd, false).size()) <= 0 ) {
				throw FeError.Chrg.BNASF015();
			}
			if (CommUtil.isNotNull(KcpChrgScdfDao.selectOne_odb3(scencd, chrgcd, false))) {
				throw FeError.Chrg.BNASF012();
			}

			KcpChrgScdf tblKcpchrgscdf = SysUtil.getInstance(KcpChrgScdf.class);
			
			tblKcpchrgscdf.setSeqnum(seqnum);
			tblKcpchrgscdf.setChrgcd(chrgcd);
			tblKcpchrgscdf.setScencd(scencd);
			tblKcpchrgscdf.setModule(module);
			tblKcpchrgscdf.setRemark(input.getRemark());
			
			KcpChrgScdfDao.insert(tblKcpchrgscdf);		
			
			//增加审计
			ApDataAudit.regLogOnInsertParameter(tblKcpchrgscdf);
			
		}
		
		
	}
}
