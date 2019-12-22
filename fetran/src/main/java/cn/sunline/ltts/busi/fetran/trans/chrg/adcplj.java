package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPljo;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPljoDao;
import cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcplj.Input.Cpinfo;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_RELVFG;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class adcplj {
	private static final BizLog bizlog = BizLogUtil.getBizLog(adcplj.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：新增优惠计划明细
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void adcplj( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcplj.Input input){
		bizlog.method("adcplj begin >>>>>>");
		
		
		for (Cpinfo cpinfo : input.getCpinfo()) {
			String diplcd = cpinfo.getDiplcd(); //优惠计划代码
			String seqnum = BusiTools.getSequence("diplseqnum_seq", 10);
			String dimecg = cpinfo.getDimecg(); //维度类别
			E_RELVFG relvfg = cpinfo.getRelvfg();//关联标志
			
			if(CommUtil.isNull(diplcd)){
				throw FeError.Chrg.BNASF303();
			}
			if(CommUtil.isNull(dimecg)){
				throw FeError.Chrg.BNASF251();
			}
			if(CommUtil.isNull(relvfg)){
				throw FeError.Chrg.BNASF117();
			}
			
			KcpFavoPljo tblKcpfavopljo = SysUtil.getInstance(KcpFavoPljo.class);
			tblKcpfavopljo.setDiplcd(diplcd); //优惠计划代码
			tblKcpfavopljo.setSeqnum(seqnum);
			tblKcpfavopljo.setDimecg(dimecg); //维度类别
			tblKcpfavopljo.setRelvfg(relvfg); //关联标志
			tblKcpfavopljo.setFadmvl(cpinfo.getFadmvl()); //维度值
			tblKcpfavopljo.setIldmdn(cpinfo.getIldmup()); //上限
			tblKcpfavopljo.setIldmup(cpinfo.getIldmdn()); //下限
			KcpFavoPljoDao.insert(tblKcpfavopljo);
			
			//增加审计
			ApDataAudit.regLogOnInsertParameter(tblKcpfavopljo);

		}

	}
}
