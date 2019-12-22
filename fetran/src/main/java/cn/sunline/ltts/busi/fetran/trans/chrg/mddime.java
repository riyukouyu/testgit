package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.util.ArrayList;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpDime;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpDimeDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_WAYTYP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mddime {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mddime.class);

	/**
	 * 
	 * @Title: mddime
	 * @Description: 修改维度类别管理
	 * @param input
	 * @param property
	 * @author songliangwei
	 * @date 2016年7月8日 下午3:26:32
	 * @version V2.3.0
	 */
	public static void mddime(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mddime.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mddime.Property property) {
		bizlog.method("mddime begin >>>>>>");
		// 判断当前机构是否为省中心机构
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}

		

		// 维度类型
		E_WAYTYP waytyp = input.getWaytyp();
		// 维度类别
		String dimecg = input.getDimecg();
		// 获取维度类别表实例对象
		KcpDime tblKcpdime = KcpDimeDao.selectOne_odb1(waytyp, dimecg, false);

		Long num = (long) 0;

		if (CommUtil.isNotNull(tblKcpdime)) {

			if (CommUtil.compare(tblKcpdime.getDimecg(), input.getDimecg()) == 0
					&& CommUtil.compare(tblKcpdime.getDimena(),
							input.getDimena()) == 0) {
				throw FeError.Chrg.BNASF317();
			}
			// 判断维度信息是否存在
	        java.util.List<KcpDime> tblKcp = new ArrayList<KcpDime>();
	        tblKcp = FeDimeDao.selone_kcp_dime(input.getDimena(),
	                input.getWaytyp(), false); // Kcp_dimeDao.selectOne_odb1(waytyp,
	                                            // dimecg, false);

	        if (CommUtil.isNotNull(tblKcp)) {
	            throw FeError.Chrg.BNASF105();
	        }

			KcpDime oldEntity = CommTools.clone(KcpDime.class, tblKcpdime);

			if (CommUtil.compare(input.getDimena(), tblKcpdime.getDimena()) != 0) {// 维度类别名称
				num++;
				tblKcpdime.setDimena(input.getDimena());

			}

			// 更新维度类别管理
			KcpDimeDao.updateOne_odb1(tblKcpdime);
			ApDataAudit.regLogOnUpdateParameter(oldEntity, tblKcpdime);
		} else {
			throw FeError.Chrg.BNASF152();
		}
		
	}
}
