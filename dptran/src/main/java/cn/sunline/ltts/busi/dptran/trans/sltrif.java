package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.AcctTransInfoForSump;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.base.lang.Page;

public class sltrif {

	public static void qryTransInfoSump(String bgindt, String endddt,
			String cuacno, Integer startx, long pgsize,
			final cn.sunline.ltts.busi.dptran.trans.intf.Sltrif.Output Output) {
		// 检查输入项
		if (CommUtil.isNull(startx)) {
			throw DpModuleError.DpstComm.BNAS0249();
		}

		if (CommUtil.isNull(pgsize)) {
			throw DpModuleError.DpstComm.BNAS0252();
		}

		if (CommUtil.isNull(bgindt)) {
			throw DpModuleError.DpstComm.BNAS0411();
		}

		if (CommUtil.isNull(endddt)) {
			throw DpModuleError.DpstComm.BNAS0599();
		}

		if (CommUtil.isNull(cuacno)) {
			throw DpModuleError.DpstComm.BNAS1059();
		}
		int counts = 0;
		Page<AcctTransInfoForSump> infos = SysUtil.getInstance(Page.class);
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		if (CommUtil.compare(endddt, trandt) >= 0) {
			if (CommUtil.compare(bgindt, trandt) < 0) {
				// 传入日期区间包含交易日期，需查询交易日记录和历史记录表
				infos = DpAcctDao.selAllTranInfoByAcctnoForSump(bgindt, endddt,
						cuacno, startx, pgsize, counts, false);
			} else if (CommUtil.compare(bgindt, trandt) == 0) {
				// 查询当日记录
				infos = DpAcctDao.selCurrTranInfoByAcctnoForSump(trandt,
						cuacno, startx, pgsize, counts, false);
			}

		} else {
			infos = DpAcctDao.selHistTranInfoByAcctnoForSump(bgindt, endddt,
					cuacno, startx, pgsize, counts, false);
		}

		
		if (CommUtil.isNotNull(infos)){
			Output.getTraifs().setCounts((int)infos.getRecordCount());
			for (AcctTransInfoForSump info : infos.getRecords()) {
				Output.getTraifs().getTranif().add(info);
			}
		}
	}
}
