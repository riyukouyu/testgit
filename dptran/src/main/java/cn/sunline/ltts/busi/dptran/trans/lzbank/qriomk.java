package cn.sunline.ltts.busi.dptran.trans.lzbank;


import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_VAFITY;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCkfi;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCkfiDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;


public class qriomk {

	public static void dealQriomk( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qriomk.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qriomk.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qriomk.Output output){
		String rqfisq = input.getRqfisq();//原请求流水
		String rqfidt = input.getRqfidt();//原请求日期
		E_VAFITY rqfity = input.getRqfity();//文件类型
		
		KnlCkfi tblKnlCkfi = KnlCkfiDao.selectOne_odb2(rqfisq, rqfidt, false);
		if(CommUtil.isNotNull(tblKnlCkfi)){
			if(tblKnlCkfi.getFilety() != rqfity){
				throw DpModuleError.DpTrans.TS010011();
			}
			output.setRqfisq(tblKnlCkfi.getServsq());//原请求流水   
	        output.setRqfidt(tblKnlCkfi.getServdt());//原请求日期  
	        output.setRqfity(tblKnlCkfi.getFilety());//文件类型    
	        output.setIsmkfi(tblKnlCkfi.getIsmkfi());//文件是否生成
	        output.setFimkdt(tblKnlCkfi.getFimkdt());//文件生成日期
	        output.setFipath(tblKnlCkfi.getFipath());//文件路径    
	        output.setFiname(tblKnlCkfi.getFiname());//文件名称  
		}else{
	        output.setIsmkfi(E_YES___.NO);//文件是否生成
		}

	}
}
