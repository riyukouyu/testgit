package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.util.ArrayList;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpChrgHist;
import cn.sunline.ltts.busi.fe.tables.FeHis.KcpChrgHistDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrg;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmexDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgScdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgScdfDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPlex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPlexDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmexDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class dlchdf {

	private static final BizLog bizlog = BizLogUtil.getBizLog(dlchdf.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：费种代码定义表删除
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void dlchdf(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlchdf.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlchdf.Property property,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Dlchdf.Output output) {

		bizlog.method("dlchdf begin >>>>>>");

		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}
		String chrgcd = input.getChrgcd(); // 费种代码
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); // 交易日期

		KcpChrg entity = SysUtil.getInstance(KcpChrg.class);
		// 获取删除数据
		entity = KcpChrgDao.selectOne_odb1(chrgcd, input.getCrcycd(), false);

		if (CommUtil.isNull(entity)) {
			throw FeError.Chrg.BNASF198();
		}

		if (DateUtil.compareDate(trandt, entity.getEfctdt()) >= 0
				&& DateUtil.compareDate(trandt, entity.getInefdt()) < 0) {
			throw FeError.Chrg.BNASF294();
		}

		List<KcpChrgFmex> tblKcpChrgFmex = new ArrayList<KcpChrgFmex>();
		List<KcpChrgScdf> tblKcpChrgScdf = new ArrayList<KcpChrgScdf>();
		List<KcpFavoSmex> tblKcpFavoSmex = new ArrayList<KcpFavoSmex>();
		List<KcpFavoPlex> tblKcpFavoPlex = new ArrayList<KcpFavoPlex>();

		tblKcpChrgFmex = KcpChrgFmexDao.selectAll_odb2(chrgcd, false);
		tblKcpChrgScdf = KcpChrgScdfDao.selectAll_odb4(chrgcd, false);
		tblKcpFavoSmex = KcpFavoSmexDao.selectAll_odb3(chrgcd, false);
		tblKcpFavoPlex = KcpFavoPlexDao.selectAll_odb4(chrgcd, false);

		if (CommUtil.isNotNull(tblKcpChrgFmex) || tblKcpChrgFmex.size() > 0
				|| CommUtil.isNotNull(tblKcpChrgScdf)
				|| tblKcpChrgScdf.size() > 0
				|| CommUtil.isNotNull(tblKcpFavoSmex)
				|| tblKcpFavoSmex.size() > 0
				|| CommUtil.isNotNull(tblKcpFavoPlex)
				|| tblKcpFavoPlex.size() > 0) {
			throw FeError.Chrg.BNASF175();
		}
		// 删除
		KcpChrgDao.deleteOne_odb1(chrgcd, input.getCrcycd());
		// 增加审计
		ApDataAudit.regLogOnDeleteParameter(entity);
		

		/**********************************************
		 * 维护历史表
		 **********************************************/
		// 实例化费种代码定义历史表
		KcpChrgHist tblKcpChrgHist = SysUtil.getInstance(KcpChrgHist.class);
		tblKcpChrgHist.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		tblKcpChrgHist.setChrgcd(entity.getChrgcd());
		tblKcpChrgHist.setChrgna(entity.getChrgna());
		tblKcpChrgHist.setCgpyrv(entity.getCgpyrv());
		tblKcpChrgHist.setFetype(entity.getFetype());
		tblKcpChrgHist.setMndecm(entity.getMndecm());
		tblKcpChrgHist.setCarrtp(entity.getCarrtp());
		tblKcpChrgHist.setLysptp(entity.getLysptp());
		tblKcpChrgHist.setFelytp(entity.getFelytp());
		tblKcpChrgHist.setIsfavo(entity.getIsfavo());
		tblKcpChrgHist.setMxfvrt(entity.getMxfvrt());
		tblKcpChrgHist.setMnfvrt(entity.getMnfvrt());
		tblKcpChrgHist.setFedive(entity.getFedive());
		tblKcpChrgHist.setChrgsr(entity.getChrgsr());
		tblKcpChrgHist.setCjsign(entity.getCjsign());
		tblKcpChrgHist.setChrgtp(entity.getChrgtp());
		tblKcpChrgHist.setEfctdt(entity.getEfctdt());
		tblKcpChrgHist.setInefdt(entity.getInefdt());
		// 新增历史表
		KcpChrgHistDao.insert(tblKcpChrgHist);

		// 增加审计
		ApDataAudit.regLogOnInsertParameter(tblKcpChrgHist);

	}

}
