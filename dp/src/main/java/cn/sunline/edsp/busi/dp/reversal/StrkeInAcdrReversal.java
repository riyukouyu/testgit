package cn.sunline.edsp.busi.dp.reversal;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.spi.MsEventControlDefault;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsInterface;
import cn.sunline.edsp.microcore.spi.SPIMeta;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcDrawStrikeInput;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CORRTG;

@SPIMeta(id="strkeInAcdr")
public class StrkeInAcdrReversal  extends MsEventControlDefault {

	@Override
	public void doReversalProcess(IoMsInterface input) {

		IaAcdrInfo strkeAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
		
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
		
		cplInput = SysUtil.deserialize(input.getInformation_value(), IoApRegBook.class);
		
		strkeAcdrInfo.setAcctno(cplInput.getTranac());
		strkeAcdrInfo.setTranam(cplInput.getTranam().negate());
		strkeAcdrInfo.setCrcycd(cplInput.getCrcycd());
		strkeAcdrInfo.setAmntcd(cplInput.getAmntcd());
		strkeAcdrInfo.setCorrtg(E_CORRTG._1);
		strkeAcdrInfo.setStrktg(E_YES___.NO);
		
		SysUtil.getInstance(IoInAccount.class).ioInAcdr(strkeAcdrInfo);
		
	}
}
