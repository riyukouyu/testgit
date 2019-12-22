package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeCodeDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgScdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgScdfDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_MODULE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mdsecd {

	private static final BizLog bizlog = BizLogUtil.getBizLog(mdsecd.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：修改场景计费管理
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */

	public static void mdsecd(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdsecd.Input input) {

		bizlog.method("mdsecd begin >>>>>>");

		String scencd = input.getScencd();// 场景代码
		E_MODULE Module = E_MODULE.CG;// 默认费用
		String seqnum = input.getSeqnum(); // 顺序号

		if (CommUtil.isNull(input.getChrgcd())) {
			throw FeError.Chrg.BNASF076();
		}

		KcpChrgScdf tblKcpchrgscdf = KcpChrgScdfDao.selectOne_odb1(seqnum,
				scencd, false);

		if (CommUtil.isNotNull(tblKcpchrgscdf)) {
			if (CommUtil.compare(input.getRemark(), tblKcpchrgscdf.getRemark()) == 0
					&& CommUtil.compare(input.getChrgcd(),
							tblKcpchrgscdf.getChrgcd()) == 0) {
				throw FeError.Chrg.BNASF317();
			}

			KcpChrgScdf oldEntity = CommTools.clone(KcpChrgScdf.class,
					tblKcpchrgscdf);

			// 明细登记簿维护
			Long num = (long) 0; // 序列

			if (CommUtil.compare(input.getChrgcd(), tblKcpchrgscdf.getChrgcd()) != 0) {
				num++;
				if(CommUtil.isNull(FeCodeDao.selone_kcp_chrg(input.getChrgcd(), false))){
					throw FeError.Chrg.BNASF074();
				}
				if (CommUtil.isNotNull(KcpChrgScdfDao.selectOne_odb3(scencd,
						input.getChrgcd(), false))) {
					throw FeError.Chrg.BNASF013();
				}

				tblKcpchrgscdf.setChrgcd(input.getChrgcd());
			}
			if (CommUtil.compare(input.getRemark(), tblKcpchrgscdf.getRemark()) != 0) { // 备注信息
				num++;
				tblKcpchrgscdf.setRemark(input.getRemark());
			}

			// 模块默认 cg
			tblKcpchrgscdf.setModule(Module);
			// 更新超额优惠定义表
			KcpChrgScdfDao.updateOne_odb1(tblKcpchrgscdf);
			ApDataAudit.regLogOnUpdateParameter(oldEntity, tblKcpchrgscdf);

		} else {
			throw FeError.Chrg.BNASF152();
		}

		bizlog.method("mdsecd end >>>>>>");
	}
}
