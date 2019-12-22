package cn.sunline.ltts.busi.dptran.trans;

/**
 * 
 * @ClassName: upddir 
 * @Description: (更改目录) 
 * @author lei wei
 * @date 2016年7月20日 下午4:48:17 
 *
 */

public class upddir {

public static void upddiru( String cataid,  String catana){
	   
//	String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();  //交易机构
//	
//	if(CommUtil.isNull(catana)){
//		throw PbError.Intr.E9999("产品目录不能为空！");
//	}
//	
//	if(CommUtil.isNull(cataid)){
//		throw PbError.Intr.E9999("产品目录编号不能为空！");
//	}
//	
//	IoPbKubBrch cplKubBrch = SysUtil.getInstance(IoPbTableSvr.class).kub_brch_selectOne_odb1(brchno, true);
//	if(E_BRCHLV.PROV != cplKubBrch.getBrchlv()){
//		throw DpModuleError.DpstAcct.E9999("非省级机构没有操作权限！");
//	
//	}
//	
//	kup_cata tebCata = Kup_cataDao.selectOne_kup_cata_idx3(catana, false);
//	
//		if (CommUtil.isNotNull(tebCata)) {
//		PbError.Intr.E9999("目录名称已存在！");
//	}
//		//根据产品编号修改产品名称
//	 kup_cata tblCata = Kup_cataDao.selectOne_kup_cata_idx1(cataid, false);
//	    tblCata.setCatana(catana);
//	    
//     // kup_cata   entity = SysUtil.getInstance(kup_cata.class);
//     //   entity.setCataid(cataid);
//     //  entity.setCatana(catana);
//     //  entity.setCreadt(creadt);
//     //  entity.setCataid(cataid);
//     // entity.setCreatm(creatm.substring(8));
//    
//       Kup_cataDao.updateOne_kup_cata_idx1(tblCata);
	
}
}
