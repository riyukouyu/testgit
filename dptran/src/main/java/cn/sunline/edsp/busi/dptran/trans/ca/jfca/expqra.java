
package cn.sunline.edsp.busi.dptran.trans.ca.jfca;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.util.PropertyUtil;
import cn.sunline.edsp.busi.dp.namedsql.ca.AccountFlowDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaadDao;
import cn.sunline.ltts.busi.dp.type.DpTransfer.DpAcctinIoblCupsExp;
import cn.sunline.ltts.busi.sys.errors.ApError;

/**
 * 
 * @author zhd
 * @deprecated 支付交易明细导出查询交易
 */
public class expqra {

	// 初始化日志信息
	private static BizLog bizlog = BizLogUtil.getBizLog(expqra.class);

	public static void exportQratinList(final cn.sunline.edsp.busi.dptran.trans.ca.jfca.intf.Expqra.Input input,
			final cn.sunline.edsp.busi.dptran.trans.ca.jfca.intf.Expqra.Property property,
			final cn.sunline.edsp.busi.dptran.trans.ca.jfca.intf.Expqra.Output output) {
		bizlog.method("expqra begin >>>>>>>>>>>>>>>>>>>>>>");
		String cardno = input.getCardno();
		if (CommUtil.isNotNull(input.getIdtfno())) {
			KnaMaad knaMaad = KnaMaadDao.selectOne_odb3(input.getIdtfno(), false);
			if (CommUtil.isNotNull(knaMaad)) {
				cardno = knaMaad.getCardno();
			}
		}
		List<DpAcctinIoblCupsExp> knlIoblCupsInfoExps = AccountFlowDao.selectKnlIoblCupsInfoExp(input.getInmeid(),
				input.getPameno(), input.getBusitp(), input.getOrgaid(), input.getSbrand(), input.getTermtp(),
				input.getPosnum(), input.getFrteno(), cardno, input.getOrdeno(), input.getServtp(), input.getTratbg(),
				input.getTrated(), input.getPretbg(), input.getPreted(), input.getUnttbg(), input.getUntted(),
				input.getRefeno(), input.getSvcode(), input.getFinsty(), input.getIdtfno(), input.getMercfg(), false);
		if (CommUtil.isNull(knlIoblCupsInfoExps)) {
			return;
		}

		for (DpAcctinIoblCupsExp dpAcctinIoblCupsExp : knlIoblCupsInfoExps) {
			cn.sunline.edsp.busi.dptran.trans.ca.jfca.intf.Expqra.Output.Exportlist data = SysUtil
					.getInstance(cn.sunline.edsp.busi.dptran.trans.ca.jfca.intf.Expqra.Output.Exportlist.class);

			try {
				PropertyUtil.copyProperties(dpAcctinIoblCupsExp, data, true, false);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				bizlog.error("导出支付明细列表时copyProperties异常", e);
				throw ApError.Sys.E9006();
			}
			// 转义字段
			if (CommUtil.isNotNull(dpAcctinIoblCupsExp.getBusitp())) {
				data.setBusitp(dpAcctinIoblCupsExp.getBusitp().getLocalLongName());
			}
			if (CommUtil.isNotNull(dpAcctinIoblCupsExp.getMercfg())) {
				data.setMercfg(dpAcctinIoblCupsExp.getMercfg().getLongName());
			}
			if (CommUtil.isNotNull(dpAcctinIoblCupsExp.getFinsty())) {
				data.setFinsty(dpAcctinIoblCupsExp.getFinsty().getLongName());
			}
			if (CommUtil.isNotNull(dpAcctinIoblCupsExp.getServtp())) {
				data.setServtp(dpAcctinIoblCupsExp.getServtp().getLongName());
			}
			if (CommUtil.isNotNull(dpAcctinIoblCupsExp.getTranst())) {
				data.setTranst(dpAcctinIoblCupsExp.getTranst().getLongName());
			}
			if (CommUtil.isNotNull(dpAcctinIoblCupsExp.getCardtp())) {
				data.setCardtp(dpAcctinIoblCupsExp.getCardtp().getLongName());
			}
			if (CommUtil.isNotNull(dpAcctinIoblCupsExp.getTermtp())) {
				data.setTermtp(dpAcctinIoblCupsExp.getTermtp().getLongName());
			}
			output.getExportlist().add(data);

		}
		bizlog.method("expqra end >>>>>>>>>>>>>>>>>>>>>>");
	}

}
