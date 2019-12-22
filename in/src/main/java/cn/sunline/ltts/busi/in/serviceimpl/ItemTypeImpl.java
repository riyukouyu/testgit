package cn.sunline.ltts.busi.in.serviceimpl;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnpItem;
import cn.sunline.ltts.busi.in.tables.In.GlKnpItemDao;
import cn.sunline.ltts.busi.in.type.InItemInfo.ItemInfo;
import cn.sunline.ltts.busi.in.type.InItemInfo.ItemInfos;
import cn.sunline.ltts.busi.sys.errors.InError;
 /**
  * 科目信息查询服务实现
  * 科目信息查询服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
public class ItemTypeImpl implements cn.sunline.ltts.busi.in.servicetype.ItemType{
 /**
  * 分页查询科目信息
  *
  */
	public void qryItemInfo( final cn.sunline.ltts.busi.in.servicetype.ItemType.QryItemInfo.Input Input,  final cn.sunline.ltts.busi.in.servicetype.ItemType.QryItemInfo.Output Output){
		List<GlKnpItem> list = InQuerySqlsDao.qryItemInfoPage(Input.getStartid(), Input.getRecdct(), false);
		int  count = InQuerySqlsDao.getItemCount(false);
		ItemInfos infos = SysUtil.getInstance(ItemInfos.class);
		for(GlKnpItem item : list){
			ItemInfo info = SysUtil.getInstance(ItemInfo.class);
			info.setAslbtp(item.getAslbtp());
			info.setBlncdn(item.getBlncdn());
			info.setBusino(item.getBusino());
			info.setDtittg(item.getDtittg());
			info.setIoflag(item.getIoflag());
			info.setItemcd(item.getItemcd());
			info.setItemlv(item.getItemlv());
			info.setItemna(item.getItemna());
			info.setItemrg(item.getItemrg());
			info.setItemtp(item.getItemtp());
			info.setIttype(item.getIttype());
			info.setProftp(item.getProftp());
			info.setUpitem(item.getUpitem());
			infos.getItinfo().add(info);
		}
		infos.setCount(count);
		Output.setItemInfo(infos);
	}

	/**
	  * 新增科目
	  *
	  */	
	@Override
	public void insItemInfo(ItemInfo inputItem) {
	
		String itemcd = inputItem.getItemcd();
		String itemna = inputItem.getItemna();
		String itemlv = inputItem.getItemlv();
		String busino = inputItem.getBusino();
		String upitem = inputItem.getUpitem();
	
		if(CommUtil.isNull(itemcd)){
			InError.comm.E0003("科目代码不能为空");
		}
		if(CommUtil.isNull(itemna)){
			InError.comm.E0003("科目名称不能为空");
		}
		if(CommUtil.isNull(itemlv)){
			InError.comm.E0003("科目级别不能为空");
		}
		if(CommUtil.isNull(busino)){
			InError.comm.E0003("业务编码不能为空");
		}
	
		GlKnpItem tblgl_knp_item = GlKnpItemDao.selectOne_odb1(itemcd, false);
		if(CommUtil.isNotNull(tblgl_knp_item)){
				InError.comm.E0003("科目代码"+itemcd+"已经存在");
		}
	
		if(CommUtil.isNotNull(upitem)){
			GlKnpItem tblgl_knp_item_upit = GlKnpItemDao.selectOne_odb1(upitem, false);
			if(CommUtil.isNull(tblgl_knp_item_upit)){
					InError.comm.E0003("科目代码"+upitem+"不存在");
			}
		}
		GlKnpItem item = SysUtil.getInstance(GlKnpItem.class);
		item.setItemcd(itemcd);
		item.setBusino(busino);
		item.setItemna(itemna);
		item.setItemlv(itemlv);
		item.setUpitem(upitem);
		item.setAslbtp(inputItem.getAslbtp());
		item.setBlncdn(inputItem.getBlncdn());
		item.setDtittg(inputItem.getDtittg());
	    item.setIoflag(inputItem.getIoflag());
	    item.setItemrg(inputItem.getItemrg());
	    item.setItemtp(inputItem.getItemtp());
	    item.setIttype(inputItem.getIttype());
	    item.setProftp(inputItem.getProftp());
	    
	    GlKnpItemDao.insert(item);
    
	}
	/**
	  * 维护科目
	  *
	  */
	@Override
	public void uptItemInfo(String itemcd,String itemna) {
		
		if(CommUtil.isNull(itemcd)){
			InError.comm.E0003("科目代码不能为空");
		}
		if(CommUtil.isNull(itemna)){
			InError.comm.E0003("科目名称不能为空");
		}
	
		GlKnpItem tblgl_knp_item = GlKnpItemDao.selectOne_odb1(itemcd, false);
		if(CommUtil.isNull(tblgl_knp_item)){
				InError.comm.E0003("科目代码"+itemcd+"不存在");
		}
		else{
			if(0 == CommUtil.compare(itemna, tblgl_knp_item.getItemna())){
				InError.comm.E0003("科目名称["+itemna+"]没有修改");
			}
		}
	
		GlKnpItem item = SysUtil.getInstance(GlKnpItem.class);
		item.setItemcd(itemcd);
		item.setBusino(tblgl_knp_item.getBusino());
		item.setItemna(itemna);
		item.setItemlv(tblgl_knp_item.getItemlv());
		item.setUpitem(tblgl_knp_item.getUpitem());
		item.setAslbtp(tblgl_knp_item.getAslbtp());
		item.setBlncdn(tblgl_knp_item.getBlncdn());
		item.setDtittg(tblgl_knp_item.getDtittg());
	    item.setIoflag(tblgl_knp_item.getIoflag());
	    item.setItemrg(tblgl_knp_item.getItemrg());
	    item.setItemtp(tblgl_knp_item.getItemtp());
	    item.setIttype(tblgl_knp_item.getIttype());
	    item.setProftp(tblgl_knp_item.getProftp());
	    
	    GlKnpItemDao.updateOne_odb1(item);
	    
	}

}

