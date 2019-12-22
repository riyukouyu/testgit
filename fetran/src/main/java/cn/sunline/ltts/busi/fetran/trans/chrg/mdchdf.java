package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrg;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_ISFAVO;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_LYSPTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class mdchdf {
	private static final BizLog bizlog = BizLogUtil.getBizLog(mdchdf.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：费种代码定义表修改
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void mdchdf(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdchdf.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Mdchdf.Property property) {
		bizlog.method("mdchdf begin >>>>>>");

		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(),
				property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}

		String chrgcd = input.getChrgcd(); // 费种代码
		String efctdt = input.getEfctdt(); // 失效日期
		String inefdt = input.getInefdt(); // 生效日期
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期

		/**
		 * 不为空判断
		 */

		if (CommUtil.isNull(input.getChrgcd())) {
			throw FeError.Chrg.BNASF076();
		}

		if (CommUtil.isNull(input.getChrgna())) {
			throw FeError.Chrg.BNASF081();
		}

		if (CommUtil.isNull(input.getCgpyrv())) {
			throw FeError.Chrg.BNASF067();
		}

		if (CommUtil.isNull(input.getFetype())) {
			throw FeError.Chrg.BNASF070();
		}

		if (CommUtil.isNull(input.getCrcycd())) {
			throw FeError.Chrg.BNASF125();
		}

		if (CommUtil.isNull(input.getMndecm())) {
			throw FeError.Chrg.BNASF346();
		}

		if (CommUtil.isNull(input.getCarrtp())) {
			throw FeError.Chrg.BNASF241();
		}

		if (CommUtil.isNull(input.getLysptp())) {
			throw FeError.Chrg.BNASF087();
		}

		if (E_LYSPTP.MANUAL == input.getLysptp()
				|| E_LYSPTP.OVAMNT == input.getLysptp()) {
			if (CommUtil.isNull(input.getFelytp())) {
				throw FeError.Chrg.BNASF227();
			}
		}

		if (CommUtil.isNull(input.getIsfavo())) {
			throw FeError.Chrg.BNASF220();
		}

		if (CommUtil.isNull(input.getFedive())) {
			throw FeError.Chrg.BNASF228();
		}

		if (CommUtil.isNull(input.getChrgsr())) {
			throw FeError.Chrg.BNASF230();
		}

		if (CommUtil.isNull(input.getCjsign())) {
			throw FeError.Chrg.BNASF271();
		}

		if (CommUtil.isNull(efctdt)) {
			throw FeError.Chrg.BNASF207();
		}

		if (CommUtil.isNull(inefdt)) {
			throw FeError.Chrg.BNASF212();
		}
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}
		
		if (DateUtil.compareDate(inefdt, trandt) <= 0) {
			throw FeError.Chrg.BNASF209();
		}

		if (input.getIsfavo() == E_ISFAVO.YES) { // 允许优惠
			if (CommUtil.isNull(input.getMnfvrt())) {
				throw FeError.Chrg.BNASF348();
			}
			if (CommUtil.isNull(input.getMxfvrt())) {
				throw FeError.Chrg.BNASF353();
			}
			if (CommUtil.compare(input.getMnfvrt(), BigDecimal.ZERO) < 0
					|| CommUtil.compare(input.getMxfvrt(),
							BigDecimal.valueOf(100)) > 0) {
				throw FeError.Chrg.BNASF278();
			}
			if (CommUtil.compare(input.getMxfvrt(), input.getMnfvrt()) <= 0) {
				throw FeError.Chrg.BNASF351();
			}
		}

		KcpChrg tblKcpchrg = KcpChrgDao.selectOne_odb1(chrgcd,
				BusiTools.getDefineCurrency(), false);

		if (CommUtil.isNotNull(tblKcpchrg)) {

			if (CommUtil.compare(tblKcpchrg.getCgpyrv(), input.getCgpyrv()) != 0) {
				throw FeError.Chrg.BNASF068();
			}

			if (CommUtil.compare(tblKcpchrg.getFetype(), input.getFetype()) != 0) {
				throw FeError.Chrg.BNASF071();
			}

			if (CommUtil.compare(tblKcpchrg.getCrcycd(), input.getCrcycd()) != 0) {
				throw FeError.Chrg.BNASF126();
			}

			if (CommUtil.compare(tblKcpchrg.getCarrtp(), input.getCarrtp()) != 0) {
				throw FeError.Chrg.BNASF242();
			}

			if (CommUtil.compare(tblKcpchrg.getLysptp(), input.getLysptp()) != 0) {
				throw FeError.Chrg.BNASF088();
			}

			if (CommUtil.compare(tblKcpchrg.getChrgsr(), input.getChrgsr()) != 0) {
				throw FeError.Chrg.BNASF148();
			}

			// 未生效的记录才能修改生效日期
			if (DateUtil.compareDate(tblKcpchrg.getEfctdt(), trandt) > 0) {
				if (DateUtil.compareDate(efctdt, trandt) <= 0) {
					throw FeError.Chrg.BNASF204();
				}
			} else {
				// 已经生效
				if (DateUtil.compareDate(tblKcpchrg.getInefdt(), trandt) < 0) {
					throw FeError.Chrg.BNASF097();
				} else {
					if (CommUtil.compare(tblKcpchrg.getChrgna(),
							input.getChrgna()) != 0
							|| CommUtil.compare(tblKcpchrg.getMndecm(),
									input.getMndecm()) != 0
							|| CommUtil.compare(tblKcpchrg.getFelytp(),
									input.getFelytp()) != 0
							|| CommUtil.compare(tblKcpchrg.getIsfavo(),
									input.getIsfavo()) != 0
							|| CommUtil.compare(tblKcpchrg.getMnfvrt(),
									input.getMnfvrt()) != 0
							|| CommUtil.compare(tblKcpchrg.getMxfvrt(),
									input.getMxfvrt()) != 0
							|| CommUtil.compare(tblKcpchrg.getFedive(),
									input.getFedive()) != 0
							|| CommUtil.compare(tblKcpchrg.getCjsign(),
									input.getCjsign()) != 0
							|| CommUtil.compare(tblKcpchrg.getEfctdt(),
									input.getEfctdt()) != 0) {
						throw FeError.Chrg.BNASF291();
					} else {
						if (CommUtil.compare(input.getInefdt(),
								tblKcpchrg.getInefdt()) == 0) {
							throw FeError.Chrg.BNASF317();
						}
					}
				}
			}

			if (CommUtil.compare(input.getChrgna(), tblKcpchrg.getChrgna()) == 0
					&& CommUtil.compare(input.getMndecm().toString(),
							tblKcpchrg.getMndecm().toString()) == 0
					&& input.getFelytp() == tblKcpchrg.getFelytp()
					&& input.getIsfavo() == tblKcpchrg.getIsfavo()
					&& CommUtil.compare(input.getMnfvrt(),
							tblKcpchrg.getMnfvrt()) == 0
					&& CommUtil.compare(input.getMxfvrt(),
							tblKcpchrg.getMxfvrt()) == 0
					&& input.getFedive() == tblKcpchrg.getFedive()
					&& CommUtil.compare(input.getEfctdt(),
							tblKcpchrg.getEfctdt()) == 0
					&& CommUtil.compare(input.getInefdt(),
							tblKcpchrg.getInefdt()) == 0
					&& input.getCjsign() == tblKcpchrg.getCjsign()) {
				throw FeError.Chrg.BNASF317();
			}
			Long num = (long) 0;
			KcpChrg oldEntity = CommTools.clone(KcpChrg.class, tblKcpchrg);

			if (CommUtil.compare(input.getChrgna(), tblKcpchrg.getChrgna()) != 0) { // 费种代码名称
				num++;
				tblKcpchrg.setChrgna(input.getChrgna());
			}

			if (CommUtil.compare(input.getMndecm().toString(), tblKcpchrg
					.getMndecm().toString()) != 0) { // 最低小数位数
				num++;
				tblKcpchrg.setMndecm(input.getMndecm());
			}

			if (input.getFelytp() != tblKcpchrg.getFelytp()) {// 收费分层取值类型
				CommUtil.equals(input.getFelytp().toString(),
						tblKcpchrg.toString());
				num++;
				tblKcpchrg.setFelytp(input.getFelytp());
			}

			if (input.getIsfavo() != tblKcpchrg.getIsfavo()) {// 是否允许优惠
				num++;
				tblKcpchrg.setIsfavo(input.getIsfavo());
			}

			if (input.getIsfavo() == E_ISFAVO.YES) {

				if (CommUtil.compare(input.getMnfvrt(), tblKcpchrg.getMnfvrt()) != 0) {// 最低优惠比例

					if (CommUtil.compare(input.getMnfvrt(), BigDecimal.ZERO) < 0
							|| CommUtil.compare(input.getMnfvrt(),
									input.getMxfvrt()) >= 0) {
						throw FeError.Chrg.BNASF347();//最低优惠比例必须满足大于等于0且小于最高优惠比例
					}

					num++;
					tblKcpchrg.setMnfvrt(input.getMnfvrt());
				}

				if (CommUtil.compare(input.getMxfvrt(), tblKcpchrg.getMxfvrt()) != 0) {// 最高优惠比例
					if (CommUtil.compare(input.getMxfvrt(),
							BigDecimal.valueOf(100)) > 0
							|| CommUtil.compare(input.getMxfvrt(),
									input.getMnfvrt()) <= 0) {
						throw FeError.Chrg.BNASF352();//"最高优惠比例不能大于等于100且需要大于最低优惠比例"
					}

					num++;
					tblKcpchrg.setMxfvrt(input.getMxfvrt());
				}
			} else {
				// 不优惠设为0
				tblKcpchrg.setMxfvrt(BigDecimal.ZERO);
				tblKcpchrg.setMnfvrt(BigDecimal.ZERO);
			}

			if (input.getFedive() != tblKcpchrg.getFedive()) {// 收费分润方式
				num++;
				tblKcpchrg.setFedive(input.getFedive());
			}

			if (CommUtil.compare(input.getEfctdt(), tblKcpchrg.getEfctdt()) != 0) {// 生效日期
				num++;
				tblKcpchrg.setEfctdt(input.getEfctdt());
			}

			if (CommUtil.compare(input.getInefdt(), tblKcpchrg.getInefdt()) != 0) { // 失效日期
				num++;
				tblKcpchrg.setInefdt(input.getInefdt());
			}

			if (input.getCjsign() != tblKcpchrg.getCjsign()) {
				num++;
				tblKcpchrg.setCjsign(input.getCjsign());
			}
			// 更新费种代码定义表
			KcpChrgDao.updateOne_odb1(tblKcpchrg);

			ApDataAudit.regLogOnUpdateParameter(oldEntity, tblKcpchrg);
		} else {
			throw FeError.Chrg.BNASF152();//"记录不存在,无法更新"
		}
	}
}
