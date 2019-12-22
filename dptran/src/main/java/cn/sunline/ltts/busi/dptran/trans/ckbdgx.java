package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;

/**
 * 
 * @author liuz
 * 2018/10/11
 *	账户绑定关系检查
 */

public class ckbdgx {

	public static void selBindcdInfo( final cn.sunline.ltts.busi.dptran.trans.intf.Ckbdgx.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Ckbdgx.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Ckbdgx.Output output){
		String cardno = input.getCardno();	//电子账号	
		String cdopac = input.getCdopac();	//绑定卡号
		//输入校验
		if (CommUtil.isNull(cardno)) {
			throw DpModuleError.DpstComm.BNAS0901();
		}
		if (CommUtil.isNull(cdopac)) {
			throw CaError.Eacct.BNAS1112();
		}
		//检查电子账户是否存在
		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(cardno, false);//获取卡和电子账号映射
		if(CommUtil.isNull(tblKnaAcdc)){
			throw DpModuleError.DpstComm.BNAS0750();
		}	
		String custac = tblKnaAcdc.getCustac();
		//获取账户状态
		E_CUACST checkcuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		if(checkcuacst == E_CUACST.OUTAGE){
		    throw DpModuleError.DpstComm.BNAS0850();
		}
		KnaCacd tblKnaCacd = KnaCacdDao.selectOne_odb2(custac,cdopac,E_DPACST.NORMAL, false);
		if (CommUtil.isNotNull(tblKnaCacd)){
			output.setIsbind(E_YES___.YES);
		}else{
			output.setIsbind(E_YES___.NO);
		}
		
	}
}
