package cn.sunline.ltts.busi.intran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRINTP;


public class intran {

public static void intran( final cn.sunline.ltts.busi.intran.trans.intf.Intran.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Intran.Property property,  final cn.sunline.ltts.busi.intran.trans.intf.Intran.Output output){
	
	
	String centbr = BusiTools.getBusiRunEnvs().getCentbr();
	if((input.getCapitp()==E_TRINTP.IN101&&CommUtil.equals(centbr, input.getInbrch()))
			||(input.getCapitp()==E_TRINTP.IN102&&CommUtil.equals(centbr, input.getOtbrch()))){
		
		if(input.getCapitp()==E_TRINTP.IN102){
			
			property.setAmntcd(E_AMNTCD.DR);
		}else{
			property.setAmntcd(E_AMNTCD.CR);
		}
	}
	property.setCapitt(cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP.IT601);
	
	output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
	output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
	output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());

}
}
