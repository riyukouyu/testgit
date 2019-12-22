
package cn.sunline.edsp.busi.dptran.trans.ca.jfca;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.busi.dp.namedsql.ca.AccountFlowDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaadDao;
import cn.sunline.ltts.busi.dp.base.DpTools;
import cn.sunline.ltts.busi.dp.type.DpTransfer.DpAcctinIoblCups;

public class qratin {

	public static void acctinFlowInfoQry(final cn.sunline.edsp.busi.dptran.trans.ca.jfca.intf.Qratin.Input input,
			final cn.sunline.edsp.busi.dptran.trans.ca.jfca.intf.Qratin.Property property,
			final cn.sunline.edsp.busi.dptran.trans.ca.jfca.intf.Qratin.Output output) {

		String cardno = input.getCardno();
		if (CommUtil.isNotNull(input.getIdtfno())) {
			KnaMaad knaMaad = KnaMaadDao.selectOne_odb3(input.getIdtfno(), false);
			if (CommUtil.isNotNull(knaMaad)) {
				cardno = knaMaad.getCardno();
			}
		}

		long pageSzie = CommTools.getBaseRunEnvs().getPage_size();
		long pageStart = CommTools.getBaseRunEnvs().getPage_start();
		long startNo = (pageStart - 1) * pageSzie;

		Page<DpAcctinIoblCups> infos = AccountFlowDao.selectKnlIoblCupsInfo(input.getInmeid(), input.getPameno(),
				input.getBusitp(), input.getOrgaid(), input.getSbrand(), input.getTermtp(), input.getPosnum(),
				input.getFrteno(), cardno, input.getOrdeno(), input.getServtp(), input.getTratbg(),
				input.getTrated(), input.getPretbg(), input.getPreted(), input.getUnttbg(), input.getUntted(),
				input.getRefeno(), input.getSvcode(), input.getFinsty(), input.getIdtfno(),input.getMercfg(),input.getEdanst(),input.getTranst(),
				startNo, pageSzie, 0, false);
		CommTools.getBaseRunEnvs().setTotal_count(infos.getRecordCount());
		CommTools.getBaseRunEnvs().setPage_size(pageSzie);
		CommTools.getBaseRunEnvs().setPage_start(pageStart);
		List<DpAcctinIoblCups> list = infos.getRecords();
		
//		BigDecimal rate = new BigDecimal("0");
//		BigDecimal rate1 = new BigDecimal("0");
//		BigDecimal rate4 = new BigDecimal("0");
		for(DpAcctinIoblCups cup :list) {
//			String merate = cup.getMerate();
//			if(CommUtil.isNotNull(merate)) {
//				String [] str = merate.split("\\|");
//				String str1 = str[0];
//				String str4 = str[4];
//				if(CommUtil.isNotNull(str1)&&!"null".equals(str1)) {
//					rate1= new BigDecimal(str1);
//				}
//				if(CommUtil.isNotNull(str4)&&!"null".equals(str4)) {
//					rate4= new BigDecimal(str4);
//				}
//				rate = rate1.add(rate4);
//				cup.setMerate(rate.toString());
//			}
			cup.setSignim(DpTools.repUrl(cup.getSignim()));
		}
		output.getFlowlist().addAll(list);
	}
}
