package cn.sunline.edsp.busi.dp.reversal;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.spi.MsEventControlDefault;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsInterface;
import cn.sunline.edsp.base.util.CT.convert;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.microcore.spi.SPIMeta;
import cn.sunline.edsp.midware.rpc.core.Convert;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcDrawStrikeInput;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

@SPIMeta(id="strkeDpDrawAcct")
public class StrkeDpDrawAcctReversal extends MsEventControlDefault {

	@Override
	public void doReversalProcess(IoMsInterface input) {
		
		ProcDrawStrikeInput strkeDpDrawAcct = SysUtil.getInstance(ProcDrawStrikeInput.class);
		
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
		
		cplInput = SysUtil.deserialize(input.getInformation_value(), IoApRegBook.class);
		
		strkeDpDrawAcct.setAcctno(cplInput.getTranac());
		strkeDpDrawAcct.setCustac(cplInput.getCustac());
		strkeDpDrawAcct.setAmntcd(cplInput.getAmntcd());
		strkeDpDrawAcct.setColrfg(E_COLOUR.RED);
		strkeDpDrawAcct.setCrcycd(cplInput.getCrcycd());
		strkeDpDrawAcct.setDetlsq(cplInput.getTranno());
		strkeDpDrawAcct.setOrtrdt(cplInput.getEvent6());
		strkeDpDrawAcct.setStacps(E_STACPS.ACCOUT);
		strkeDpDrawAcct.setTranam(cplInput.getTranam());
		strkeDpDrawAcct.setInstam(convert.toBigDecimal(CommUtil.nvl(cplInput.getEvent1(), BigDecimal.ZERO))); // 支取利息
		strkeDpDrawAcct.setIntxam(convert.toBigDecimal(CommUtil.nvl(cplInput.getEvent3(), BigDecimal.ZERO))); // 利息税
		strkeDpDrawAcct.setAcctst(CommUtil.toEnum(E_DPACST.class, cplInput.getEvent2())); // 账户状态
		strkeDpDrawAcct.setPyafam(ConvertUtil.toBigDecimal(CommUtil.nvl(cplInput.getEvent4(), BigDecimal.ZERO))); // 追缴金额
		strkeDpDrawAcct.setPydlsq(ConvertUtil.toLong(CommUtil.nvl(cplInput.getEvent5(), BigDecimal.ZERO)));
		
		SysUtil.getInstance(IoDpStrikeSvcType.class).procDrawStrike(strkeDpDrawAcct);
		
	}
}
