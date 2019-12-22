package cn.sunline.edsp.busi.dp.reversal;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.spi.MsEventControlDefault;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsInterface;
import cn.sunline.edsp.microcore.spi.SPIMeta;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;

@SPIMeta(id="strkeDpUnfrAcct")
public class StrkeDpUnfrAcctReversal extends MsEventControlDefault {
	@Override
	public void doReversalProcess(IoMsInterface input) {

		IoApRegBook cplInput = SysUtil.deserialize(input.getInformation_value(), IoApRegBook.class);
		
		SysUtil.getInstance(IoDpStrikeSvcType.class).proUnfrStrike(cplInput.getEvent1(), cplInput.getTranam());
	}
}
