package cn.sunline.ltts.busi.dptran.trans.online;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CUSTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

public class opcpac {

	public static void beforeFlow(
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Opcpac.Input Input,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Opcpac.Property Property,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Opcpac.Output Output) {

		//是银行，则设置客户类型为同业客户。
		if(Input.getIsbank() == E_YES___.YES){
			Property.setCusttp(E_CUSTTP.BANK);
		}
		
		//TODO 其他检查
		//组织机构代码只允许开户一次
		//if(CommUtil.isNotNull(Cif_corpDao.selectOne_idx2(Input.getCropcd(), false))){
//		if(CommUtil.isNotNull(SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_corp_selectOne_idx2(Input.getCropcd(), false))){
//			throw DpModuleError.DpstComm.BNAS0012(Input.getCropcd());
//		}
	}
}
