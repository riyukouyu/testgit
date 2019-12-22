package cn.sunline.ltts.busi.dptran.trans;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctAddt;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctAddtDao;
import cn.sunline.ltts.busi.dptran.trans.intf.Qrwalm.Output;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;



public class qrwalm {

	public static Output qrwalm( String cardno){
		
		if(CommUtil.isNull(cardno)){
			throw DpModuleError.DpstComm.BNAS0955();
		}
		
		IoCaSevQryTableInfo caType = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoDpSrvQryTableInfo dpType = SysUtil.getInstance(IoDpSrvQryTableInfo.class);
		
		//根据电子账号获取电子账号ID
		IoCaKnaAcdc caKnaAcdc = caType.getKnaAcdcOdb2(cardno, false);
		if(CommUtil.isNull(caKnaAcdc.getCustac())){
			throw DpModuleError.DpstComm.BNAS0754();
		}
		String custac = caKnaAcdc.getCustac();
		
		//根据电子账号ID获取电子账号分类
		IoCaKnaCust caKnaCust = caType.getKnaCustByCustacOdb1(custac, false);
		if(CommUtil.isNull(caKnaCust.getAccttp())){
			throw DpModuleError.DpstComm.BNAS1877();
		}
		E_ACCATP accttp = caKnaCust.getAccttp();
		
		if(accttp != E_ACCATP.WALLET){
			throw DpModuleError.DpstComm.BNAS1088();
		}
		
		//根据电子账号ID和电子账号分类获取钱包负债账号
		KnaAcct dpKnaAcct = CapitalTransDeal.getSettKnaAcctSub(custac, E_ACSETP.MA);
		if(CommUtil.isNull(dpKnaAcct.getAcctno())){
			throw DpModuleError.DpstComm.BNAS0661();
		}
		String acctno = dpKnaAcct.getAcctno();
		
		//根据钱包负债账号查询负债账户附加信息表信息
		KnaAcctAddt tblKnaAcctAddt = KnaAcctAddtDao.selectOne_odb1(acctno, false);
		if(CommUtil.isNull(tblKnaAcctAddt)){
			throw DpModuleError.DpstComm.BNAS0691();
		}
		
		BigDecimal higham = tblKnaAcctAddt.getHigham();
		
		Output output = SysUtil.getInstance(Output.class);
		
		output.setHigham(higham);
		
		return output;
		
	}
 public void qrwalm(String cardno,cn.sunline.ltts.busi.dptran.trans.intf.Qrwalm.Output output){
	 
		if(CommUtil.isNull(cardno)){
			throw DpModuleError.DpstProd.BNAS0926();
		}
		IoCaSevQryTableInfo caType = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoDpSrvQryTableInfo dpType = SysUtil.getInstance(IoDpSrvQryTableInfo.class);
		
		//根据电子账号获取电子账号ID
		IoCaKnaAcdc caKnaAcdc = caType.getKnaAcdcOdb2(cardno, false);
		if(CommUtil.isNull(caKnaAcdc.getCustac())){
			throw DpModuleError.DpstComm.BNAS0754();
		}
		String custac = caKnaAcdc.getCustac();
		
		//根据电子账号ID获取电子账号分类
		IoCaKnaCust caKnaCust = caType.getKnaCustByCustacOdb1(custac, false);
		if(CommUtil.isNull(caKnaCust.getAccttp())){
			throw DpModuleError.DpstComm.BNAS1877();
		}
		E_ACCATP accttp = caKnaCust.getAccttp();
		
		if(accttp != E_ACCATP.WALLET){
			throw DpModuleError.DpstComm.BNAS1088();
		}
		
		//根据电子账号ID和电子账号分类获取钱包负债账号
		KnaAcct dpKnaAcct = CapitalTransDeal.getSettKnaAcctSub(custac, E_ACSETP.MA);
		if(CommUtil.isNull(dpKnaAcct.getAcctno())){
			throw DpModuleError.DpstComm.BNAS0661();
		}
		String acctno = dpKnaAcct.getAcctno();
		
		//根据钱包负债账号查询负债账户附加信息表信息
		KnaAcctAddt tblKnaAcctAddt = KnaAcctAddtDao.selectOne_odb1(acctno, false);
		if(CommUtil.isNull(tblKnaAcctAddt)){
			throw DpModuleError.DpstComm.BNAS0691();
		}
		
		BigDecimal higham = tblKnaAcctAddt.getHigham();	
		output.setHigham(higham); 
 }

}
