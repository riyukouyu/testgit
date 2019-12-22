
package cn.sunline.edsp.busi.dptran.trans.lzbank;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUPSST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_PASTAT;
import cn.sunline.edsp.busi.ds.iobus.servicetype.ds.IoDsManage;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdm;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmContro;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmControDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

public class tranrs {

	public static void notificationofTransactionResulDeal(
			final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Tranrs.Input input,
			final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Tranrs.Property property,
			final cn.sunline.edsp.busi.dptran.trans.lzbank.intf.Tranrs.Output output) {

		String mntrdt = input.getEdtrdt();// 代发交易日期

		String mntrsq = input.getEdtrsq();// 代发交易流水

		// 核心交易日期不能为空
		if (CommUtil.isNull(mntrdt)) {
			throw DpModuleError.DpTrans.TS010033();
		}
		// 核心交易流水不能为空
		if (CommUtil.isNull(mntrsq)) {
			throw DpModuleError.DpTrans.TS010034();
		}
		// 查询该笔流水
		KnlIoblEdm knlIoblEdm = KnlIoblEdmDao.selectOne_odb01(mntrsq, mntrdt, false);

		// 判断该笔流水是否存在
		if (CommUtil.isNull(knlIoblEdm)) {
			output.setExitfg(E_YES___.NO);
			return;
		}
		
		if (!CommUtil.equals(knlIoblEdm.getTranst().getValue(), E_CUPSST.CLWC.getValue())) {
			output.setExitfg(E_YES___.YES);
			return;
		}

		knlIoblEdm.setRetmsg(input.getRetmsg());

		knlIoblEdm.setRtcode(input.getRtcode());

		knlIoblEdm.setServtp(input.getServtp());

		knlIoblEdm.setOrgaid(input.getOrgaid());

		knlIoblEdm.setOrcdtp(input.getOrcdtp());

		knlIoblEdm.setOrcode(input.getOrcode());

		knlIoblEdm.setPrepdt(input.getPrepdt());

		knlIoblEdm.setPrepsq(input.getPrepsq());

		knlIoblEdm.setPreptm(input.getPreptm());

		knlIoblEdm.setRtcdtp(input.getRtcdtp());
		
		knlIoblEdm.setUnnumb(input.getUnnumb());
		
		knlIoblEdm.setGtkttp(input.getGtkttp());

		if (CommUtil.equals(input.getRtcdtp(), "S")) {
			knlIoblEdm.setTranst(E_CUPSST.SUCC);
		} else if (CommUtil.equals(input.getRtcdtp(), "E")) {
			knlIoblEdm.setTranst(E_CUPSST.FAIL);
			// todo交易失败冲正待处理
			
			//冲掉分润相关信息
			// 分润计算服务调用冲正
			IoDsManage ioDsManage = SysUtil.getInstance(IoDsManage.class);
			ioDsManage.rollbkCalProfit(knlIoblEdm.getRetrdt(), knlIoblEdm.getRetrsq());
		} else if (CommUtil.equals(input.getRtcdtp(), "R")) {

		}

		// 更新操作
		KnlIoblEdmDao.updateOne_odb01(knlIoblEdm);

		// 根据返回码进行判断处理

		KnlIoblEdmContro knlIoblEdmContro = KnlIoblEdmControDao.selectOne_edmOdb01(knlIoblEdm.getRetrsq(),
				knlIoblEdm.getRetrdt(), false);

		if (CommUtil.isNotNull(knlIoblEdmContro)) {

			if (CommUtil.equals(input.getRtcdtp(), "S")) {
				knlIoblEdmContro.setEdmflg(E_PASTAT.SUCC);
				KnlIoblEdmControDao.updateOne_edmOdb01(knlIoblEdmContro);
			} else if (CommUtil.equals(input.getRtcdtp(), "E")) {
				knlIoblEdmContro.setEdmflg(E_PASTAT.FAIL);
				KnlIoblEdmControDao.updateOne_edmOdb01(knlIoblEdmContro);
				// todo交易失败冲正待处理
			}
		}

	}
}