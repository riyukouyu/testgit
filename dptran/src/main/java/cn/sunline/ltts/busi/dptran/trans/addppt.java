package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPost;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostDao;
import cn.sunline.ltts.busi.dp.type.DpProdConfType.KupDppbPostInfo;
import cn.sunline.ltts.busi.dptran.trans.intf.Addppt.Input.Dpposts;


public class addppt {

	public static void addKupDppbPost( final Dpposts dppost){
		for(KupDppbPostInfo info : dppost.getDppost()){
			KupDppbPost post = SysUtil.getInstance(KupDppbPost.class);
			post.setAmntwy(info.getAmntwy());
			post.setCrcycd(info.getCrcycd());
			post.setDetlfg(info.getDetlfg());
			post.setMaxiam(info.getMaxiam());
			post.setMaxitm(info.getMaxitm());
			post.setMiniam(info.getMiniam());
			post.setMinitm(info.getMinitm());
			post.setPosttp(info.getPosttp());
			post.setPostwy(info.getPostwy());
			post.setProdcd(info.getProdcd());
			post.setTimewy(info.getTimewy());
			if (CommUtil.isNull(info.getSvrule())) {
				post.setSvrule(0L);
			} else {
				post.setSvrule(info.getSvrule());
			}
			KupDppbPostDao.insert(post);
		}
	}

	

}
