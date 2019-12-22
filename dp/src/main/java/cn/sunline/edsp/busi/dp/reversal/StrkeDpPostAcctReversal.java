package cn.sunline.edsp.busi.dp.reversal;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.spi.MsEventControlDefault;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsInterface;
import cn.sunline.edsp.microcore.spi.SPIMeta;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcSaveStrikeInput;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

@SPIMeta(id="strkeDpPostAcct")
public class StrkeDpPostAcctReversal  extends MsEventControlDefault {

	@Override
	public void doReversalProcess(IoMsInterface input) {
//		
//		super.doReversalProcess(input);
		
		ProcSaveStrikeInput procSaveStrikeInput = SysUtil.getInstance(ProcSaveStrikeInput.class);
		
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
		
		cplInput = SysUtil.deserialize(input.getInformation_value(), IoApRegBook.class);
		
		procSaveStrikeInput.setAcctno(cplInput.getTranac());
		procSaveStrikeInput.setCustac(cplInput.getCustac());
		procSaveStrikeInput.setAmntcd(cplInput.getAmntcd());
		procSaveStrikeInput.setColrfg(E_COLOUR.RED);
		procSaveStrikeInput.setCrcycd(cplInput.getCrcycd());
		procSaveStrikeInput.setDetlsq(cplInput.getTranno());
		procSaveStrikeInput.setOrtrdt(cplInput.getEvent1());
		procSaveStrikeInput.setStacps(E_STACPS.ACCOUT);
		procSaveStrikeInput.setTranam(cplInput.getTranam());
		
		SysUtil.getInstance(IoDpStrikeSvcType.class).procSaveStrike(procSaveStrikeInput);
		
	}
}
