package cn.sunline.edsp.busi.dp.reversal;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.spi.MsEventControlDefault;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsInterface;
import cn.sunline.edsp.microcore.spi.SPIMeta;
import cn.sunline.ltts.busi.dp.jfaccounting.JfDpAccountingPublic;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqRgst;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcDrawStrikeInput;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CORRTG;

@SPIMeta(id="strkeRegistKnsAcsqRgst")
public class StrkeRegistKnsAcsqRgstReversal  extends MsEventControlDefault {

	@Override
	public void doReversalProcess(IoMsInterface input) {

		
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
		
		cplInput = SysUtil.deserialize(input.getInformation_value(), IoApRegBook.class);
		
		KnsAcsqRgst tblKnsAcsqRgst = JfDpAccountingPublic.chckOrtrsq(cplInput.getEvent1(), cplInput.getEvent2());
		
		tblKnsAcsqRgst.setTranst(E_TRANST.STRIKED);
		tblKnsAcsqRgst.setStrksq(CommTools.getBaseRunEnvs().getTrxn_seq());
		tblKnsAcsqRgst.setStrkdt(CommTools.getBaseRunEnvs().getTrxn_date());
		tblKnsAcsqRgst.setStrktm(CommTools.getBaseRunEnvs().getComputer_time());
		
		JfDpAccountingPublic.updOrtrsq(tblKnsAcsqRgst);
		
	}
}
