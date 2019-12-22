
package cn.sunline.ltts.busi.dptran.trans;


import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcalDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.dp.namedsql.YhtDao;
import cn.sunline.ltts.busi.dptran.trans.intf.Qraccs.Output.BindCardList;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;


public class qraccs {

public static void qraccs( final cn.sunline.ltts.busi.dptran.trans.intf.Qraccs.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Qraccs.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Qraccs.Output output){
    if(CommUtil.isNull(input.getCardno())){
        throw DpModuleError.DpstComm.E9999("客户卡号不能为空！");
    }
    if(CommUtil.isNull(input.getTeleno())){
        throw DpModuleError.DpstComm.E9999("手机号不能为空！");
    }
    if(CommUtil.isNull(input.getIdtftp())){
        throw DpModuleError.DpstComm.E9999("证件类型不能为空！");
    }
    if(CommUtil.isNull(input.getIdtfno())){
        throw DpModuleError.DpstComm.E9999("证件号码不能为空！");
    }
    if(CommUtil.isNull(input.getCustna())){
        throw DpModuleError.DpstComm.E9999("客户名称不能为空！");
    }
    if (E_IDTFTP.SFZ != input.getIdtftp()) {
        throw DpModuleError.DpstComm.E9999("暂时只支持身份证！");
    }
    //手机号码位数验证
    if (input.getTeleno().length() != 11) {
        throw DpModuleError.DpstComm.BNAS0469();
    }
    // 校验手机号是否全为数字
    if (!BusiTools.isNum(input.getTeleno())) {
        throw CaError.Eacct.BNAS0319();
    }
    //校验证件类型、证件号码
    BusiTools.chkCertnoInfo(input.getIdtftp(), input.getIdtfno());
    
	KnaAcdc knaAcdc = KnaAcdcDao.selectOne_odb2(input.getCardno(), false);
	//判断输入的卡号是否正确
	if(CommUtil.isNull(knaAcdc) || knaAcdc.getStatus() == E_DPACST.CLOSE){
	    throw DpModuleError.DpstComm.E9999("该卡号不存在！");
	}
	//通过卡号获取对应系统内的电子账号
	KnaCust knaCust = KnaCustDao.selectOne_odb1(knaAcdc.getCustac(), false);
	if(CommUtil.isNull(knaCust)){
	    throw DpModuleError.DpstComm.E9999("该卡号对应的电子账号不存在！");
	}
//	CifCust cifCust = CifCustDao.selectOne_odb1(knaCust.getCustno(), false);
//	if(!CommUtil.isNull(cifCust)){
//	    //客户名称验证
//	    if(!CommUtil.equals(cifCust.getCustna(), input.getCustna())){
//	        throw DpModuleError.DpstComm.E9999("客户卡号和客户名称不匹配！");
//	    }
//	    //身份证验证
//	    if(!CommUtil.equals(cifCust.getIdtfno(), input.getIdtfno())){
//	        throw DpModuleError.DpstComm.E9999("客户卡号和证件号码 不匹配！");
//	    }
//	}
	KnaAcal knaAcal = KnaAcalDao.selectFirst_odb4(knaCust.getCustac(), E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);
	if(!CommUtil.isNull(knaAcal)){
	    //手机号码验证
	    if(!CommUtil.equals(knaAcal.getTlphno(), input.getTeleno())){
	        throw DpModuleError.DpstComm.E9999("客户卡号和手机号码 不匹配！");
	    }
	}
	List<KnaCacd> knaCacd = YhtDao.selKnacacdByCustac(knaAcdc.getCustac(), false);
	if(CommUtil.isNull(knaCacd)){
	    throw DpModuleError.DpstComm.E9999("电子账号对应的绑定卡不存在！");
	}
	BindCardList a = SysUtil.getInstance(BindCardList.class);
	for (KnaCacd cacd : knaCacd) {
	    a.setBrchna(cacd.getBrchna());//绑定账户开户行名称
	    a.setOpbrch(cacd.getOpbrch());//绑定账户开户行行号
	    a.setCdopac(cacd.getCdopac());//绑定账户卡号
	    a.setCdopna(cacd.getAcctna());//绑定账户名称
	    output.getBindCardList().add(a);
    }
	output.setAccatp(knaCust.getAccttp());//电子账户分类
}
}
