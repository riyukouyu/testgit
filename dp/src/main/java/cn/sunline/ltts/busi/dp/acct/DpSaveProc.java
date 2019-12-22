package cn.sunline.ltts.busi.dp.acct;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkSaveDepositIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKcdCard;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnpAcctType;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_COACFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DCMTST;

public class DpSaveProc {
	private static final BizLog bizlog = BizLogUtil.getBizLog(DpSaveProc.class);
	/**
	 * 电子账户状态检查
	 * @param saveDpIn
	 */
	public static DpProdSvc.ChkEacctStatusOut chkEacctSt(ChkSaveDepositIn saveDpIn){
		
		if(CommUtil.isNull(saveDpIn.getCrcycd())){
			CaError.Eacct.BNAS0663();
		}
		if(CommUtil.isNull(saveDpIn.getTranac())){
			CaError.Eacct.BNAS0022();
		}
		if(CommUtil.isNull(saveDpIn.getAcctna())){
			DpModuleError.DpstComm.BNAS0534();
		}
		String cardno=saveDpIn.getCardno(); //卡号
		E_CUSACT csactp=saveDpIn.getCsactp(); //客户账号类型
		String custno=saveDpIn.getCustno(); //客户账号
		
		DpProdSvc.ChkEacctStatusOut out=SysUtil.getInstance(DpProdSvc.ChkEacctStatusOut.class);
		
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
//		IoPbTableSvr pbsvr = SysUtil.getInstance(IoPbTableSvr.class);
		//电子账号对照表信息
		//kna_acdc tblKna_acdc=null;
		IoCaKnaAcdc tblKna_acdc = SysUtil.getInstance(IoCaKnaAcdc.class);
		//电子账户信息
		//kna_cust tblKna_cust=null;	
		IoCaKnaCust tblKna_cust = SysUtil.getInstance(IoCaKnaCust.class);
		
		if(CommUtil.isNotNull(saveDpIn.getEcctno())){
			try {
				//tblKna_cust=Kna_custDao.selectOne_odb1(saveDpIn.getEcctno(),true);
				tblKna_cust = caqry.getKnaCustByCustacOdb1(saveDpIn.getEcctno(), true);
			} catch (Exception e) {
				throw CaError.Eacct.BNAS1183(saveDpIn.getEcctno());
			} 
			
		} else {
			if(CommUtil.isNotNull(cardno)){
				//tblKna_acdc=Kna_acdcDao.selectOne_odb2(cardno, false);
				tblKna_acdc = caqry.getKnaAcdcOdb2(cardno, false);
				//获取电子账户信息
				//tblKna_cust=Kna_custDao.selectOne_odb1(tblKna_acdc.getCustac(), false);
				tblKna_cust = caqry.getKnaCustByCustacOdb1(tblKna_acdc.getCustac(), false);
			}else{
				//如果为空检查默认客户账户类型
				if(CommUtil.isNull(csactp)){
					/*//String corpNo=CommTools.getBaseRunEnvs().getBusi_org_id();
					//-----mdy by zhanga---- 直接使用省中心法人查询默认参数 
					 * TODO 是否检查默认客户账户类型
					String corpNo = BusiTools.getCenterCorpno();
					//csactp = Kub_corpDao.selectOne_odb1(corpNo, false).getCsactp();
					csactp = pbsvr.kub_corp_selectOne_odb1(corpNo, false).getCsactp();
					if(CommUtil.isNull(csactp)){
						throw CaError.Eacct.E0001("默认账户类型不存在");
					}*/ 
				}
				//卡号为空，电子账户类型不为空，根据账户类型获取电子账户
				//knp_acct_type tblKnp_acct_type=Knp_acct_typeDao.selectOne_odb1(csactp, false);
				IoCaKnpAcctType tblKnp_acct_type = caqry.getKnpAcctTypeOdb1(csactp, false);
				//检查客户类型定义容器，唯一获取电子账户，不唯一报错
				if(CommUtil.equals(tblKnp_acct_type.getCoacfg().getValue(),  E_COACFG.ONE.getValue())){
					//获取电子账户信息
					//tblKna_cust=Kna_custDao.selectFirst_odb2(csactp, custno, false);
					tblKna_cust = caqry.getKnaCustFirstOdb2(csactp, custno, false);
				}else{
					throw CaError.Eacct.BNAS0512();
				}
			}
		}
		
		//检查账户是否激活，第一笔存款自动激活，如果状态为关闭状态，则拒绝交易
		if(tblKna_cust.getAcctst()==E_ACCTST.INVALID){
			
			tblKna_cust.setAcctst(E_ACCTST.NORMAL);
			
		}else if(tblKna_cust.getAcctst()==E_ACCTST.CLOSE){
			
			DpModuleError.DpstProd.BNAS1717(tblKna_cust.getCustac());
		}
		//Kna_custDao.updateOne_odb1(tblKna_cust);  	//更新账户状态
		caqry.updateKnaCustOdb1(tblKna_cust);
		out.setCustac(tblKna_cust.getCustac());		//返回电子账号
		out.setAcctst(tblKna_cust.getAcctst());		//状态
		out.setBrchno(tblKna_cust.getBrchno());		//机构
		out.setCardno(tblKna_cust.getCardno());		//卡号
		out.setClosdt(tblKna_cust.getClosdt());		//销户日期
		out.setClossq(tblKna_cust.getClossq());		//销户流水
		out.setCsactp(tblKna_cust.getCacttp());		//客户账号类型
		out.setCustna(tblKna_cust.getCustna());		//客户名称
		out.setCustno(tblKna_cust.getCustno());		//客户号
		out.setOpendt(tblKna_cust.getOpendt());		//开户日期
		out.setOpensq(tblKna_cust.getOpensq());		//开户流水
		out.setAccttp(tblKna_cust.getAccttp());  	//电子账户性质
		
		return out;
	}
	
	/**
	 * 卡状态检查
	 * @param outcard 
	 * 
	 */
	public static void chkCardStatus(String cardno,String custac, String outcard) {
		
		//kcd_card tblKcd_card=null;
		//kna_acdc tblKna_acdc=null;
		IoCaKcdCard tblKcd_card = SysUtil.getInstance(IoCaKcdCard.class);
		IoCaKnaAcdc tblKna_acdc = SysUtil.getInstance(IoCaKnaAcdc.class);
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		if(CommUtil.isNotNull(cardno)){
			//tblKcd_card = Kcd_cardDao.selectOne_odb1(cardno, false);
			tblKcd_card = caqry.getKcdCardOdb1(cardno, false);
		}else {
			// 卡为空，通过电子账号获取卡信息
			if(CommUtil.isNull(custac)){
				DpModuleError.DpstComm.BNAS0541();
			}
			//tblKna_acdc = Kna_acdcDao.selectOne_odb1(custac, E_DPACST.NORMAL, false);
			tblKna_acdc = caqry.getKnaAcdcOdb1(custac, E_DPACST.NORMAL, false);
			if(CommUtil.isNull(tblKna_acdc)){
				DpModuleError.DpstProd.BNAS0872();
			}
			//tblKcd_card = Kcd_cardDao.selectOne_odb1(tblKna_acdc.getCardno(), false);
			tblKcd_card = caqry.getKcdCardOdb1(tblKna_acdc.getCardno(), false);
		}
		E_DCMTST status=tblKcd_card.getDcmtst();
		//检查状态是否正常
		bizlog.debug("卡号状态==================="+status);
		if(status!=E_DCMTST.NORMAL){
			throw DpModuleError.DpstProd.BNAS1130(tblKna_acdc.getCardno());
		}
	}

}
