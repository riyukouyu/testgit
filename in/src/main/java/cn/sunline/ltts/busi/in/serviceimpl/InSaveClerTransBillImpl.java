
package cn.sunline.ltts.busi.in.serviceimpl;

import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.tables.cler.UionCler;
import cn.sunline.ltts.busi.in.tables.cler.UionClerDao;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;


 /**
  * 与统一支付清算相关服务
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="InSaveClerTransBillImpl", longname="与统一支付清算相关服务", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class InSaveClerTransBillImpl implements cn.sunline.ltts.busi.iobus.servicetype.in.InSaveClerTransBill{
	public void saveClerBill(final cn.sunline.ltts.busi.iobus.servicetype.in.InSaveClerTransBill.SaveClerBill.Input input){
		UionCler tbUionCler  = SysUtil.getInstance(UionCler.class);
		tbUionCler.setOrdrid(input.getOrdrid());
		tbUionCler.setFrondt(input.getFrondt());
		tbUionCler.setFronsq(input.getFronsq());
		tbUionCler.setTranam(input.getTranam());
		tbUionCler.setLinkno(input.getLinkno());
		tbUionCler.setFeeamt(input.getFeeamt());
		tbUionCler.setIntamt(input.getIntamt());
		tbUionCler.setCrcycd(input.getCrcycd());
		tbUionCler.setAcctno(input.getAcctno());
		tbUionCler.setAcctee(input.getAcctee());
		tbUionCler.setAcctwo(input.getAcctwo());
		tbUionCler.setToacct(input.getToacct());
		tbUionCler.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());
		tbUionCler.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		tbUionCler.setKeepdt(input.getKeepdt());
		tbUionCler.setClactp(input.getClactp());
		tbUionCler.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());
		tbUionCler.setStatus(CommUtil.nvl(input.getTranst(), E_TRANST.NORMAL));
		UionClerDao.insert(tbUionCler);
		//冲正注册
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
		cplInput.setEvent1(input.getFronsq());
		cplInput.setEvent2(input.getFrondt());
		cplInput.setTranev(ApUtil.TRANS_EVENT_INCLER);
		
		//ApStrike.regBook(cplInput);
		IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);      
	    apinput.setReversal_event_id(cplInput.getTranev());
	    apinput.setInformation_value(SysUtil.serialize(cplInput));
	    MsEvent.register(apinput, true);
		
		
	}

	public void procSaveClerBillStrike( String transq,  String trandt,  cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST status){
		String mtdate = CommTools.getBaseRunEnvs().getTrxn_date();
		String tmstmp = DateTools2.getCurrentTimestamp();
		String revrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		InQuerySqlsDao.strikeUionClerStatus(transq, trandt, status, revrsq, mtdate, tmstmp);
	}
}

