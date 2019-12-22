package cn.sunline.ltts.busi.dp.serviceimpl;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.dp.password.DpPassword;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;

/**
 * 交易密码服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoSrvDpPasswdImpl", longname = "交易密码服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoSrvDpPasswdImpl implements
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoSrvDpPasswd {
	/**
	 * 通用检查交易密码
	 * 
	 */
	public void validatePasswdComm(
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoSrvDpPasswd.ValidatePasswdComm.Input input) {

		DpPassword.validatePassword(input.getAcctno(), input.getPasswd(),
				"ALL", "%");

		//poc增加审计日志
		ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
		apAudit.regLogOnInsertBusiPoc(input.getAcctno());
	}

	/**
	 * 通用重置交易密码
	 * 
	 */
	public void resetPasswdComm(
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoSrvDpPasswd.ResetPasswdComm.Input input) {
		DpPassword.savePassword(input.getAcctno(), input.getAcctrt(),
				input.getPasswd(), "ALL", "%", input.getAuthif());
	}

	/**
	 * 通用修改交易密码
	 * 
	 */
	public void updatePasswdComm(
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoSrvDpPasswd.UpdatePasswdComm.Input input) {
	    String cryptoPassword=null;
	    if(CommUtil.isNotNull(input.getAuthif())){
	        // cryptoPassword=EncryTools.encryPassword(input.getOdpswd(), input.getAuthif(),input.getAcctno());          
	    }else{
	        cryptoPassword=input.getOdpswd();
	    }
		DpPassword.validatePassword(input.getAcctno(), cryptoPassword,
				"ALL", "%");
		DpPassword.savePassword(input.getAcctno(), null, input.getPasswd(),
				"ALL", "%", input.getAuthif());
		//poc增加审计日志
        KnaAcdc kacdc=KnaAcdcDao.selectFirst_odb1(input.getAcctno(), E_DPACST.NORMAL, false);
        if(CommUtil.isNotNull(kacdc)){
           ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
           apAudit.regLogOnInsertBusiPoc(kacdc.getCardno());
        }

	}

	/**
	 * 按照交易渠道检查交易密码
	 * 
	 */
	public void validatePasswd(
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoSrvDpPasswd.ValidatePasswd.Input input) {
		DpPassword.validatePassword(input.getAcctno(), input.getPasswd(),
				CommTools.getBaseRunEnvs().getChannel_id(), CommTools.getBaseRunEnvs().getChannel_id());
	}

	/**
	 * 按照交易渠道重置交易密码
	 * 
	 */
	public void resetPasswd(
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoSrvDpPasswd.ResetPasswd.Input input) {
		DpPassword.savePassword(input.getAcctno(), input.getAcctrt(),
				input.getPasswd(), CommTools.getBaseRunEnvs().getChannel_id(),
				CommTools.getBaseRunEnvs().getChannel_id(), null);
	}

	/**
	 * 按照交易渠道修改交易密码
	 * 
	 */
	public void updatePasswd(
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoSrvDpPasswd.UpdatePasswd.Input input) {
		DpPassword.savePassword(input.getAcctno(), null, input.getPasswd(),
				CommTools.getBaseRunEnvs().getChannel_id(), CommTools.getBaseRunEnvs().getChannel_id(), null);
		DpPassword.validatePassword(input.getAcctno(), input.getPasswd(),
				CommTools.getBaseRunEnvs().getChannel_id(), CommTools.getBaseRunEnvs().getChannel_id());
	}
}
