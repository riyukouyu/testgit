
package cn.sunline.ltts.busi.dptran.trans.client;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcctDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CKTRIF;


public class qrissv {

	public static void checkAcctno( final cn.sunline.ltts.busi.dptran.trans.client.intf.Qrissv.Input input,  
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Qrissv.Property property,  
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Qrissv.Output output){
		// 校验账户
		GlKnaAcct tblGlKnaAcct = GlKnaAcctDao.selectOne_odb1(input.getCardno(), false);
		if(CommUtil.isNotNull(tblGlKnaAcct)){
			property.setIschck(E_YES___.NO);
			output.setIsable(E_CKTRIF.ENOK);
			output.setCustnm(tblGlKnaAcct.getAcctna());
		}
	}
}
