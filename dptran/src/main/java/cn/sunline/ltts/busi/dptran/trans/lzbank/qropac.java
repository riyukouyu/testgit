package cn.sunline.ltts.busi.dptran.trans.lzbank;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpac;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpacDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;


public class qropac {

	public static void dealQropac( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qropac.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qropac.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qropac.Output output){
		KnbOpac tblKnbOpac = KnbOpacDao.selectOne_odb2(input.getOptrsq(), false);
		if(CommUtil.isNull(tblKnbOpac)){
			output.setIsopen(E_YES___.NO);
		}else{
			output.setCardno(KnaAcdcDao.selectFirst_odb3(tblKnbOpac.getCustac(), false).getCardno());
			output.setAccttp(tblKnbOpac.getAccttp());
			output.setCustna(tblKnbOpac.getCustna());
			output.setOpendt(tblKnbOpac.getOpendt());
			output.setOpentm(tblKnbOpac.getOpentm());
			output.setCustac(tblKnbOpac.getCustac());
			output.setIsopen(E_YES___.YES);
		}
	}
}
