package cn.sunline.edsp.busi.dp.reversal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.spi.MsEventControlDefault;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsInterface;
import cn.sunline.edsp.microcore.spi.SPIMeta;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCary;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCaryDao;

@SPIMeta(id="saveActoacPeer")
public class SaveActoacPeerReversal extends MsEventControlDefault {
	private static final BizLog bizlog = BizLogUtil.getBizLog(SaveActoacPeerReversal.class);
	@Override
	public void doReversalProcess(IoMsInterface input) {
		
		KnlCary knlCary = SysUtil.deserialize(input.getInformation_value(), KnlCary.class);
		
		KnlCaryDao.deleteOne_odb1(knlCary.getServsq(), knlCary.getServdt());
	}
}
