package cn.sunline.ltts.busi.dptran.trans;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.namedsql.DpFrozDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwne;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwneDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCacd;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;


public class dpspuf {

	
	/**
	 * 根据电子账号查询账户是否被冻结，冻结且输入金额==余额，则解冻，并将余额出金
	 * @param custac
	 */
	public static void doUnfrozeAndTransfer( final cn.sunline.ltts.busi.dptran.trans.intf.Dpspuf.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Dpspuf.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Dpspuf.Output output){
		String crcycd =  BusiTools.getDefineCurrency(); //默认人民币
		String custac = input.getCustac();
		BigDecimal tranam = input.getTranam();
		if(CommUtil.isNull(custac)){
			throw DpModuleError.DpstComm.BNAS0955();
		}
		//1、查询活期负债帐号，账号不存在该方法会抛出异常
		DpProdSvc.QryDpAcctOut acctOut = SysUtil.getInstance(DpProdSvcType.class).qryDpAcct(custac, crcycd);
		
		//kna_cacd cacd = Kna_cacdDao.selectFirst_odb3(custac, E_DPACST.NORMAL, true);
		IoCaKnaCacd cacd = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaCacdFirstOdb3(custac, E_DPACST.NORMAL, true);
		//2、查询冻结主体
		KnbFrozOwne owne = DpFrozDao.selFrozOwneByCustac(custac, false);
		if(CommUtil.isNull(owne) || owne.getFralfg() == E_YES___.NO){
			throw DpModuleError.DpstComm.BNAS0943(custac);
		}else{
			//解冻
			owne.setFralfg(E_YES___.NO);
			KnbFrozOwneDao.updateOne_odb1(owne);
			BigDecimal account = DpAcctProc.getProductBal(custac, crcycd, false);
			if(CommUtil.compare(account, tranam)==0){
				//电子账户支取
				DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
				cplDrawAcctIn.setCustac(custac);
				cplDrawAcctIn.setTranam(tranam);
				cplDrawAcctIn.setCrcycd(crcycd);
				cplDrawAcctIn.setAcctno(acctOut.getAcctno());
				cplDrawAcctIn.setOpacna(cacd.getAcctna());
				cplDrawAcctIn.setToacct(cacd.getCardno());
				CommTools.getRemoteInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
				
				//银联全渠道入金
				IaAcdrInfo acdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
				KnpParameter KnpParameter = KnpParameterDao.selectOne_odb1("InParm.clearbusi", "in", "19",null,false);
//				acdrInfo.setBusino("CP3001");
				acdrInfo.setBusino(KnpParameter.getParm_value1());
				acdrInfo.setCrcycd(crcycd);
				acdrInfo.setToacct(custac);
				acdrInfo.setToacna(acctOut.getAcctna());
				acdrInfo.setTranam(tranam);
				acdrInfo.setAcbrch(acctOut.getBrchno());
				CommTools.getRemoteInstance(IoInAccount.class).ioInAccr(acdrInfo);
				
				// 设置属性
				property.setCrcycd(crcycd);
				property.setCardno(cacd.getCardno());
				property.setCustna(cacd.getAcctna());
				
				// 设置输出
				output.setAcctno(custac);
				output.setCrcycd(crcycd);
				output.setTranam(tranam);
				output.setRemark("强制解冻扣划");
				output.setTranno(CommTools.getBaseRunEnvs().getMain_trxn_seq());
				output.setBankcd(cacd.getBrchna());
				output.setRecvac(cacd.getCardno());
				output.setRecvna(cacd.getAcctna());
				output.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
				output.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());
			}else{
				throw DpModuleError.DpstComm.BNAS1254();
			}
		}
	}

	public static void afterCheck( final cn.sunline.ltts.busi.dptran.trans.intf.Dpspuf.Input Input,  final cn.sunline.ltts.busi.dptran.trans.intf.Dpspuf.Property Property,  final cn.sunline.ltts.busi.dptran.trans.intf.Dpspuf.Output Output){
		//平衡性检查 开关开时检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
	}
}
