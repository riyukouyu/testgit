package cn.sunline.edsp.busi.dp.reversal;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.spi.MsEventControlDefault;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsInterface;
import cn.sunline.edsp.microcore.spi.SPIMeta;
import cn.sunline.ltts.busi.dp.jfaccounting.JfDpAccountingPublic;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqRgst;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcDrawStrikeInput;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CORRTG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

@SPIMeta(id="strkeSaveKnsAcsq")
public class StrkeRegistKnsAcsqReversal  extends MsEventControlDefault {

	@Override
	public void doReversalProcess(IoMsInterface input) {

		
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
		
		cplInput = SysUtil.deserialize(input.getInformation_value(), IoApRegBook.class);
		
		IoAccounttingIntf accounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
		
		accounttingIntf.setAcctno(cplInput.getTranac());
		accounttingIntf.setProdcd(cplInput.getCustac());
		accounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());
		accounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		accounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		accounttingIntf.setAmntcd(cplInput.getAmntcd());
		accounttingIntf.setDtitcd(cplInput.getCustac());
		accounttingIntf.setCrcycd(cplInput.getCrcycd());
		accounttingIntf.setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch());
		accounttingIntf.setTranam(cplInput.getTranam().negate()); //银联成本金额
		accounttingIntf.setToacct(null);
		accounttingIntf.setToacna(null);
		accounttingIntf.setTobrch(CommTools.getBaseRunEnvs().getTrxn_branch());
		accounttingIntf.setAtowtp(E_ATOWTP.IN);
		accounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
		accounttingIntf.setBltype(E_BLTYPE.BALANCE);
		accounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		accounttingIntf.setCorpno(CommTools.getBusiOrgId());
		
		accounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//渠道       

        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(accounttingIntf);
		
	}
}
