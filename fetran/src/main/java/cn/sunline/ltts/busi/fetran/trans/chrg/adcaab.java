package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeCodeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeSceneDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgSubj;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgSubjDao;
import cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcaab.Input.Cainfo;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class adcaab {
	private static final BizLog bizlog = BizLogUtil.getBizLog(adcaab.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：新增费种代码核算属性
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */

	public static void adcaab(
			final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcaab.Input input,
			final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcaab.Property property) {
		bizlog.method("adcaab begin >>>>>>");

		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}
		
		if (input.getCainfo().size() <= 0) {
			throw FeError.Chrg.BNASF079();
		}
		
		for ( Cainfo cainfo : input.getCainfo()) {
			String chrgcd = cainfo.getChrgcd();
			
			if (CommUtil.isNull(chrgcd)) {
				throw FeError.Chrg.BNASF076();
			}
			if (CommUtil.isNull(FeCodeDao.selone_kcp_chrg(chrgcd, false))) {
				throw FeError.Chrg.BNASF074();
			}
			
			//若该费种代码已配置场景代码%，则不能再关联其他场景代码
			if(CommUtil.compare(cainfo.getScencd(), "%") == 0){
				List<KcpChrgSubj> aKcpChrgSubj  = KcpChrgSubjDao.selectAll_odb2(chrgcd, false);
				if(CommUtil.isNotNull(aKcpChrgSubj)){
					throw FeError.Chrg.BNASF072();
				}
			}
			
			KcpChrgSubj wdKcpChrgSubj = KcpChrgSubjDao.selectOne_odb1(chrgcd, "%", false);
			if(CommUtil.isNotNull(wdKcpChrgSubj)){
				throw FeError.Chrg.BNASF072();
			}
			
			if (CommUtil.equals(cainfo.getPrmark(), "1")
					&& CommUtil.isNull(cainfo.getPronum())) { // 当对应产品标志选择1-归属产品时，产品编号必输，若与场景代码无关，填写%
				throw FeError.Chrg.BNASF010();
			}

			if(CommUtil.isNull(cainfo.getScencd())){
				throw FeError.Chrg.BNASF016();
			}
			if (FeSceneDao.selone_kcp_scev_detl(cainfo.getScencd(), false).size() <= 0
					&& !CommUtil.equals(cainfo.getScencd(), "%")) {
				throw FeError.Chrg.BNASF015();
			}
			if ((CommUtil.isNotNull(cainfo.getPrmark())|| 
					CommUtil.isNotNull(cainfo.getTrinfo())) && 
					(CommUtil.isNull(cainfo.getPrmark())|| 
					CommUtil.isNull(cainfo.getTrinfo()))) { //网络核算属性必须同时维护
						throw FeError.Chrg.BNASF249();
					}
			
			if ((CommUtil.isNotNull(cainfo.getAcclev()) ||
					CommUtil.isNotNull(cainfo.getSubnum())|| 
					CommUtil.isNotNull(cainfo.getIntacc())) && 
					(CommUtil.isNull(cainfo.getAcclev())||
					CommUtil.isNull(cainfo.getSubnum())|| 
					CommUtil.isNull(cainfo.getIntacc()))) { // 柜面核算属性必须同时维护 
				throw FeError.Chrg.BNASF123();
			}
			
			
			KcpChrgSubj tblKcpchrgsubj = SysUtil.getInstance(KcpChrgSubj.class);
			tblKcpchrgsubj.setChrgcd(chrgcd); // 费种代码
			tblKcpchrgsubj.setSubnum(cainfo.getSubnum()); // 科目号
			tblKcpchrgsubj.setAcclev(cainfo.getAcclev()); //对账级别
			tblKcpchrgsubj.setIntacc(cainfo.getIntacc()); // 内部帐对应顺序号
			tblKcpchrgsubj.setScencd(cainfo.getScencd()); // 场景代码
			tblKcpchrgsubj.setPrmark(cainfo.getPrmark());// 对应产品标志
			tblKcpchrgsubj.setPronum(cainfo.getPronum());// 产品编号
			tblKcpchrgsubj.setTrinfo(cainfo.getTrinfo());// 交易信息
			KcpChrgSubjDao.insert(tblKcpchrgsubj);		
			
			//审计登记簿
			ApDataAudit.regLogOnInsertParameter(tblKcpchrgsubj);
			
		}
		
	}

	public static void init( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcaab.Input input,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcaab.Property property){
		
		//property.setBrchno( CommTools.getBaseRunEnvs().getTrxn_branch());
		
		
	}

}
