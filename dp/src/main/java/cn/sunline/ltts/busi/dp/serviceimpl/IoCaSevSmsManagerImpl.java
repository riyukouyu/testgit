package cn.sunline.ltts.busi.dp.serviceimpl;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.SmsManager.KubCustSms;
import cn.sunline.ltts.busi.dp.tables.SmsManager.KubCustSmsDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevSmsManager.QrySmsStatus.Output;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevSmsManager.SmsOpenClose.Input;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
 /**
  * 电子账户短信功能管理服务
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoCaSevSmsManagerImpl", longname="电子账户短信功能管理服务", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoCaSevSmsManagerImpl implements cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevSmsManager{
	/**
	  * 设置是否开通短信通知
	  *
	  */
	@Override
	public void smsOpenClose(Input input) {
		KubCustSms entity = KubCustSmsDao.selectOne_odb1(input.getCustac(), input.getTeleno(), false);
		if(CommUtil.isNull(entity)) {
			KubCustSms kubCustSms  = SysUtil.getInstance(KubCustSms.class);
			kubCustSms.setIsopen(input.getIsopen());
			kubCustSms.setCustac(input.getCustac());
			kubCustSms.setTeleno(input.getTeleno());
			kubCustSms.setIsopen(input.getIsopen());
			kubCustSms.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
			KubCustSmsDao.insert(kubCustSms);
			return;
		} 
		if(CommUtil.compare(input.getIsopen(), entity.getIsopen()) != 0) {
			entity.setIsopen(input.getIsopen());
			KubCustSmsDao.updateOne_odb1(entity);
		}
	}
	 /**
	  * 查询是否开通短信通知
	  *
	  */
	@Override
	public void qrySmsStatus(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevSmsManager.QrySmsStatus.Input input, Output output) {
		KubCustSms entity = KubCustSmsDao.selectOne_odb1(input.getCustac(), input.getTeleno(), false);
		if(CommUtil.isNull(entity)) {
			output.setIsopen(E_YES___.NO);
		} else {
			output.setIsopen(entity.getIsopen());
		}
	}
 

}

