package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDime;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDimeDao;
import cn.sunline.ltts.busi.sys.errors.FeError;

public class mddiva {
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：修改维度值管理
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */

	public static void mddiva(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mddiva.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mddiva.Property property) {

		// 判断当前机构是否为省中心机构
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}

		String dimecg = input.getDimecg(); // 维度类别
		String dimevl = input.getDimevl(); // 维度值
		String expmsg = input.getExpmsg(); // 维度名称

		if (CommUtil.isNull(dimecg)) {
			throw FeError.Chrg.BNASF251();
		}

		if (CommUtil.isNull(dimevl)) {
			throw FeError.Chrg.BNASF259();
		}

		if (CommUtil.isNull(expmsg)) {
			throw FeError.Chrg.BNASF262();
		}

		KcpScevDime tblKcp_scev_dime = KcpScevDimeDao.selectOne_odb3(dimecg,
				input.getSeqnum(), false);
		if (CommUtil.isNotNull(tblKcp_scev_dime)) {
			if (CommUtil.compare(dimevl, tblKcp_scev_dime.getDimevl()) == 0
					&& CommUtil.compare(input.getExpmsg(),
							tblKcp_scev_dime.getExpmsg()) == 0) {
				throw FeError.Chrg.BNASF317();
			}

			KcpScevDime oldEntity = CommTools.clone(KcpScevDime.class,
					tblKcp_scev_dime);
			Long num = (long) 0;

			if (CommUtil.compare(dimevl, tblKcp_scev_dime.getDimevl()) != 0) { // 维度值
				num++;
				tblKcp_scev_dime.setDimevl(dimevl);
				if (CommUtil.isNotNull(FeDimeDao.selone_evl_dime(dimevl, dimecg, false))) {
					throw FeError.Chrg.BNASF999("维度值重复");
				}
			}
			if (CommUtil.compare(expmsg, tblKcp_scev_dime.getExpmsg()) != 0) { // 维度值
                num++;
                tblKcp_scev_dime.setExpmsg(expmsg);
                if (CommUtil.isNotNull(FeDimeDao.selone_evl_dime(expmsg, dimecg, false))) {
                    throw FeError.Chrg.BNASF999("维度值名称重复");
                }
            }

			/*if (CommUtil.compare(input.getExpmsg(),
					tblKcp_scev_dime.getExpmsg()) != 0) { // 维度值名称
				num++;
				tblKcp_scev_dime.setExpmsg(input.getExpmsg());
			}*/
			
			
			KcpScevDimeDao.updateOne_odb3(tblKcp_scev_dime);
			ApDataAudit.regLogOnUpdateParameter(oldEntity, tblKcp_scev_dime);
		} else {
			throw FeError.Chrg.BNASF152();
		}
	}
}
