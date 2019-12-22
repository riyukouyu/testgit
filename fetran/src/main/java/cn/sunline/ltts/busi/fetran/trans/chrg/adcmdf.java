package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeFormulaDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdfDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class adcmdf {
	private static final BizLog bizlog = BizLogUtil.getBizLog(adcmdf.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：计费公式代码新增
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void adcmdf( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcmdf.Input input,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcmdf.Property property,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcmdf.Output output){
		bizlog.method("akcmdf begin >>>>>>");
		
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}
		
		String fmunam = input.getFmunam(); //计费公式名称
		String efctdt = input.getEfctdt(); //生效日期
		String inefdt = input.getInefdt(); //失效日期
		String filebs = input.getFilebs(); //参考文件
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//交易日期
		
		if (CommUtil.isNull(inefdt)) {
			throw FeError.Chrg.BNASF212();
		}
		if (CommUtil.isNull(efctdt)) {
			throw FeError.Chrg.BNASF207();
		}
		if (CommUtil.isNull(fmunam)) {
			throw FeError.Chrg.BNASF113();
		}
		
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}
		if (DateUtil.compareDate(efctdt, trandt) <= 0) {
			throw FeError.Chrg.BNASF204();
		}
		
		//根据公式名称查相关数据
		KcpChrgFmdf tblkcpchrgfmdf = FeFormulaDao.selfum_kcp_chrg_fmdf(fmunam, false);
		
		if (CommUtil.isNotNull(tblkcpchrgfmdf)) {
			throw FeError.Chrg.BNASF116();
		}

		
		KcpChrgFmdf tblkcp_chrg_fmdf = SysUtil.getInstance(KcpChrgFmdf.class);
		
		String chrgfm = "JFGS" + BusiTools.getSequence("chrgfm_seq", 4); //计费公式代码
		
		tblkcp_chrg_fmdf.setChrgfm(chrgfm);
		tblkcp_chrg_fmdf.setFmunam(fmunam);
		tblkcp_chrg_fmdf.setEfctdt(efctdt);
		tblkcp_chrg_fmdf.setInefdt(inefdt);
		tblkcp_chrg_fmdf.setFilebs(filebs);
		KcpChrgFmdfDao.insert(tblkcp_chrg_fmdf);
		
		ApDataAudit.regLogOnInsertParameter(tblkcp_chrg_fmdf);
		
		//输出
		output.setChrgfm(chrgfm);
	}
		
	}