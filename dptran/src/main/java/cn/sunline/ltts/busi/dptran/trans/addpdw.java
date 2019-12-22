package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDraw;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawDao;
import cn.sunline.ltts.busi.dp.type.DpProdConfType.KupDppbDrawInfo;
import cn.sunline.ltts.busi.dptran.trans.intf.Addpdw.Input.Dpdraws;


public class addpdw {

	public static void addKupDppbDraw( final Dpdraws dpdraw){
		for(KupDppbDrawInfo info : dpdraw.getDpdraw()){
			KupDppbDraw draw = SysUtil.getInstance(KupDppbDraw.class);
			draw.setCrcycd(info.getCrcycd());
			draw.setCtrlwy(info.getCtrlwy());
			draw.setDramwy(info.getDramwy());
			draw.setDrawtp(info.getDrawtp());
			draw.setDrmiam(info.getDrmiam());
			draw.setDrmitm(info.getDrmitm());
			draw.setDrmxam(info.getDrmxam());
			draw.setDrmxtm(info.getDrmxtm());
			draw.setDrrule(info.getDrrule());
			draw.setDrtmwy(info.getDrtmwy());
			draw.setOrdrwy(info.getOrdrwy());
			draw.setProdcd(info.getProdcd());
			draw.setSelfwy(info.getSelfwy());
			
			KupDppbDrawDao.insert(draw);
		}
	}
}
