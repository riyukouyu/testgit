package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPljo;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPljoDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mdcplj {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mdcplj.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：修改优惠计划明细
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */

	public static void mdcplj(
			final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Mdcplj.Input input) {
		bizlog.method("mdcplj begin >>>>>>");
		String diplcd = input.getDiplcd(); // 优惠计划代码
		String seqnum = input.getSeqnum();// 顺序号

		if (CommUtil.isNull(diplcd)) {
			throw FeError.Chrg.BNASF303();
		}

		if (CommUtil.isNull(seqnum)) {
			throw FeError.Chrg.BNASF240();
		}

		KcpFavoPljo tblKcpfavopljo = KcpFavoPljoDao.selectOne_odb1(diplcd,
				seqnum, false);

		if (CommUtil.isNotNull(tblKcpfavopljo)) {
			if (CommUtil.compare(input.getDimecg(), tblKcpfavopljo.getDimecg()) == 0
					&& CommUtil.compare(input.getRelvfg(),
							tblKcpfavopljo.getRelvfg()) == 0
					&& CommUtil.compare(input.getFadmvl(),
							tblKcpfavopljo.getFadmvl()) == 0
					&& CommUtil.compare(input.getIldmdn(),
							tblKcpfavopljo.getIldmdn()) == 0
					&& CommUtil.compare(input.getIldmup(),
							tblKcpfavopljo.getIldmup()) == 0) {
				throw FeError.Chrg.BNASF317();
			}
			KcpFavoPljo oldEntity = CommTools.clone(KcpFavoPljo.class,
					tblKcpfavopljo);

			// 明细登记簿维护
			Long num = (long) 0;
			if (CommUtil.compare(input.getDimecg(), tblKcpfavopljo.getDimecg()) != 0) {// 维度类别
				num++;
				tblKcpfavopljo.setDimecg(input.getDimecg());
			}
			if (CommUtil.compare(input.getRelvfg(), tblKcpfavopljo.getRelvfg()) != 0) {// 关联标志
				num++;
				tblKcpfavopljo.setRelvfg(input.getRelvfg());
			}
			if (CommUtil.compare(input.getFadmvl(), tblKcpfavopljo.getFadmvl()) != 0) {// 维度值
				num++;
				tblKcpfavopljo.setFadmvl(input.getFadmvl());
			}
			if (CommUtil.compare(input.getIldmdn(), tblKcpfavopljo.getIldmdn()) != 0) {// 下限
				num++;
				tblKcpfavopljo.setIldmdn(input.getIldmdn());
			}
			if (CommUtil.compare(input.getIldmup(), tblKcpfavopljo.getIldmup()) != 0) {// 上限
				num++;
				tblKcpfavopljo.setIldmup(input.getIldmup());
			}
			KcpFavoPljoDao.updateOne_odb1(tblKcpfavopljo);
			ApDataAudit.regLogOnUpdateParameter(oldEntity, tblKcpfavopljo);

		} else {
			throw FeError.Chrg.BNASF152();
		}
	}
}
