package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class caswqy {
	private static BizLog log = BizLogUtil.getBizLog(caswqy.class);
	//查询未启用电子账户信息
public static void selwqyInfo( cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp,  String idtfno,  final cn.sunline.ltts.busi.catran.trans.intf.Caswqy.Output output){
	log.debug("<<==========未启用电子账户信息查询==========>>");
	//输入参数检查
	if(CommUtil.isNull(idtftp)){
		throw DpModuleError.DpstAcct.BNAS1036();
	}
	if(CommUtil.isNull(idtfno)){
		throw DpModuleError.DpstAcct.BNAS1037();
	}
	
	String cardno = CaDao.selCardnoByIdtftpIdtfno(idtftp, idtfno, false);
	
	if(CommUtil.isNull(cardno)){
		throw CaError.Eacct.BNAS0704();
	}
	
	IoCaKnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCard(cardno, false);
	if (CommUtil.isNull(tblKnaAcdc) || tblKnaAcdc.getStatus() == E_DPACST.CLOSE) {
		throw DpModuleError.DpstComm.BNAS0754();
	}
	
	KnaCust tbl = CaDao.selKnaCustByCustac(tblKnaAcdc.getCustac(), false);
	if (CommUtil.isNull(tbl)) {
		throw DpModuleError.DpstComm.BNAS0754();
	} 
	
	// 查询电子账户状态
	E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(tblKnaAcdc.getCustac());
	if (cuacst != E_CUACST.NOENABLE) {
		throw CaError.Eacct.BNAS0704();
	}
	
	// 查询电子账户账户分类
	E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac());
	
	output.setAccttp(eAccatp);
	output.setCardno(cardno);
	output.setCustna(tbl.getCustna());
	output.setCustac(tbl.getCustac());
	output.setBrchno(tbl.getBrchno());

}
}
