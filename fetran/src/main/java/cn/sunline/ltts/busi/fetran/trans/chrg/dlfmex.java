package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpChrgFmexHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpChrgFmexHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmexDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class dlfmex {

	private static final BizLog bizlog = BizLogUtil.getBizLog(dlfmex.class);

	/**
	 * 
	 * @Title: dlfmex
	 * @Description: 删除标准费率管理
	 * @param input
	 * @param property
	 * @author songliangwei
	 * @date 2016年7月7日 下午8:47:49
	 * @version V2.3.0
	 */
	public static void dlfmex(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlfmex.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlfmex.Property property) {

		bizlog.method("dlfmex begin >>>>>>");

		// 判断当前机构是否为省中心机构
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}

		String chrgcd = input.getChrgcd(); // 费种代码
		String chrgfm = input.getChrgfm(); // 计费公式代码
		//modify by wenbo 20170705 修改获取默认币种方式
		//String crcycd = BusiTools.getBusiRunEnvs().getCrcycd();// 默认人民币
		String crcycd = BusiTools.getDefineCurrency();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); // 交易日期

		if (CommUtil.isNull(chrgcd)) {
			throw FeError.Chrg.BNASF076();
		}

		if (CommUtil.isNull(chrgfm)) {
			throw FeError.Chrg.BNASF142();
		}

		if (CommUtil.isNull(crcycd)) {
			throw FeError.Chrg.BNASF125();
		}

		KcpChrgFmex entity = SysUtil.getInstance(KcpChrgFmex.class);
		entity = KcpChrgFmexDao.selectOne_odb1(chrgcd, chrgfm, true);

		if (CommUtil.isNull(entity)) {
			throw FeError.Chrg.BNASF144();
		}

		// 已生效的标准费率不能删除
		if (DateUtil.compareDate(entity.getEfctdt(), trandt) <= 0) {

			// 标准费率未失效判断 add by chenjk
			if (CommUtil.isNull(entity.getInefdt())
					|| DateUtil.compareDate(entity.getInefdt(), trandt) >= 0) {
				throw FeError.Chrg.BNASF091();
			}
		}

		// 删除
		KcpChrgFmexDao.deleteOne_odb1(chrgcd, chrgfm);

		// 增加审计
		ApDataAudit.regLogOnDeleteParameter(entity);
		/**********************************************
		 * 维护历史表
		 **********************************************/
		// 实例化公式解析历史表
		KcpChrgFmexHist tblKcpChrgFmexHist = SysUtil.getInstance(KcpChrgFmexHist.class);
		tblKcpChrgFmexHist.setChrgcd(entity.getChrgcd());
		tblKcpChrgFmexHist.setChrgfm(entity.getChrgfm());
		tblKcpChrgFmexHist.setEfctdt(entity.getEfctdt());
		tblKcpChrgFmexHist.setInefdt(entity.getInefdt());
		// 新增历史表
		KcpChrgFmexHistDao.insert(tblKcpChrgFmexHist);
        
		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblKcpChrgFmexHist);
		

	}
}
