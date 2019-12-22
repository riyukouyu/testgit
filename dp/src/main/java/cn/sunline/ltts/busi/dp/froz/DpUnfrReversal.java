package cn.sunline.ltts.busi.dp.froz;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetl;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetlDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwne;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwneDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbUnfr;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbUnfrDao;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;

public class DpUnfrReversal {

	/**
	 * 解冻冲正
	 * 仅金额解冻回滚，给金额冻结的扣划使用
	 * @param mntrsq
	 */
	public static void deductPrc(String mntrsq) {
		// 根据主交易流水查询解冻登记簿的解冻记录
		List<KnbUnfr> lstknbUnfr = KnbUnfrDao.selectAll_odb1(mntrsq, true);
		BigDecimal reunfram = lstknbUnfr.get(0).getUnfram();
		KnbFroz knbfroz1 = 
				KnbFrozDao.selectOne_odb8(lstknbUnfr.get(0).getOdfrno(), lstknbUnfr.get(0).getOdfrsq(), true);
		// 仅金额解冻冲正
		if (knbfroz1.getFrlmtp() == E_FRLMTP.AMOUNT) {
		    for (KnbUnfr knbunfr : lstknbUnfr) {
			    KnbFroz knbfroz = 
					KnbFrozDao.selectOne_odb8(knbunfr.getOdfrno(), knbunfr.getOdfrsq(), true);
			
				KnbFrozDetl knbfrozdetl = 
						KnbFrozDetlDao.selectOne_odb2(knbunfr.getOdfrno(), knbunfr.getOdfrsq(), true);
				knbfroz.setFrozst(E_FROZST.VALID);
				knbfrozdetl.setFrozst(E_FROZST.VALID);
				knbfrozdetl.setFrozbl(knbfrozdetl.getFrozbl().add(knbunfr.getUnfram()));
				KnbFrozDao.updateOne_odb8(knbfroz);            //更新冻结登记簿
				KnbFrozDetlDao.updateOne_odb2(knbfrozdetl);   // 更新冻结明细登记簿

				knbunfr.setUnfram(BigDecimal.ZERO);
				KnbUnfrDao.updateOne_odb2(knbunfr);   // 更新解冻登记簿的解冻金额为0
				
				if (CommUtil.compare(knbunfr.getUnfram(), reunfram)>0) {
					reunfram = knbunfr.getUnfram();//主体登记簿使用，需要获取最大的解冻金额
				}
			}
			KnbFrozOwne knbfrozowne = KnbFrozOwneDao.selectOne_odb1(E_FROZOW.AUACCT,lstknbUnfr.get(0).getCustac() , true);
			knbfrozowne.setFrozbl(knbfrozowne.getFrozbl().add(reunfram));
			KnbFrozOwneDao.updateOne_odb1(knbfrozowne);
		}

	}
}
