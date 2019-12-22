package cn.sunline.ltts.busi.dptran.trans;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.acct.OpenSubAcctCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.froz.DpFrozTools;
import cn.sunline.ltts.busi.dp.namedsql.DpFrozDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpOpenSub;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;



public class opnchk {

public static void openCheck( final cn.sunline.ltts.busi.dptran.trans.intf.Opnchk.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Opnchk.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Opnchk.Output output){
	
	if(CommUtil.isNull(input.getOpenInfo().getBase().getCardno())){
		throw DpModuleError.DpstComm.BNAS0541();
	}
	
	if(CommUtil.isNull(input.getOpenInfo().getBase().getCrcycd())){
		throw DpModuleError.DpstComm.BNAS0663();
	}
	
	if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0){
		throw DpModuleError.DpstAcct.BNAS0623();
	}
	/*if(input.getOpenInfo().getDppb().getProdmt() == E_FCFLAG.CURRENT || 
			input.getOpenInfo().getDppb().getProdlt() == E_DEBTTP.DP2403){
		throw DpModuleError.DpstComm.E9999("业务细类不是定期存款");
	}*/
	//检查账户类型
	IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
	
	
	IoDpOpenSub open = input.getOpenInfo();
	IoCaKnaAcdc acdc = caqry.getKnaAcdcOdb2(open.getBase().getCardno(), false); //电子账号转换,获取电子账号ID
	if(CommUtil.isNull(acdc)){
		throw DpModuleError.DpstComm.BNAS0754();
	}
	
	input.getOpenInfo().getBase().setCustac(acdc.getCustac());
	
	IoCaKnaCust cust = caqry.getKnaCustByCustacOdb1(acdc.getCustac(), true);
	IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
	
	E_ACCATP accatp = cagen.qryAccatpByCustac(acdc.getCustac());
	if(accatp == E_ACCATP.WALLET){
		throw DpModuleError.DpstComm.BNAS0402();
	}
	//检查账户状态
	if(cust.getAcctst() != E_ACCTST.NORMAL){
		throw DpModuleError.DpstComm.BNAS0899();
	}
	
	String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
	String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
	E_FROZST frozst = E_FROZST.VALID;
	
	//查询冻结信息
	List<IoDpKnbFroz> knb_froz = DpFrozDao.selFrozInfoByCustac(acdc.getCustac(), tranbr, trandt, frozst, false);
	for(IoDpKnbFroz froz : knb_froz){
		if(froz.getFroztp() == E_FROZTP.ADD || froz.getFroztp() == E_FROZTP.JUDICIAL){
			if(froz.getFrlmtp() == E_FRLMTP.OUT){
				throw DpModuleError.DpstComm.BNAS0866();
			}else if(froz.getFrlmtp() == E_FRLMTP.ALL){
				throw DpModuleError.DpstComm.BNAS0859();
			}

		}else if(froz.getFroztp() == E_FROZTP.BANKSTOPAY){
			throw DpModuleError.DpstComm.BNAS0860();

		}else if(froz.getFroztp() == E_FROZTP.CUSTSTOPAY){
			throw DpModuleError.DpstComm.BNAS0864();
		}else if(froz.getFroztp() == E_FROZTP.EXTSTOPAY){
			throw DpModuleError.DpstComm.BNAS0862();
		}
	}
	
	//查询活期结算户信息
	KnaAcct KnaAcct = CapitalTransDeal.getSettKnaAcctSub(acdc.getCustac(), E_ACSETP.SA);
	
/*	//查询可用余额信息
	BigDecimal usebal = DpAcctProc.getProductBal(acdc.getCustac(), open.getBase().getCrcycd(), false);
*/	
	// 可用余额 addby xiongzhao 20161223 
		BigDecimal usebal = SysUtil.getInstance(DpAcctSvcType.class)
				.getAcctaAvaBal(acdc.getCustac(), KnaAcct.getAcctno(),
						open.getBase().getCrcycd(), E_YES___.YES, E_YES___.NO);
	
	//查询冻结余额
	BigDecimal frozbal = DpFrozTools.getFrozBala(E_FROZOW.AUACCT, acdc.getCustac());
	
	if(CommUtil.compare(usebal, input.getTranam()) < 0){
		throw DpModuleError.DpstComm.E9999("电子账户可用余额不足");
	}
	//检查是否需要检查透支标志
	if(CommUtil.compare(input.getTranam(), KnaAcct.getOnlnbl().subtract(frozbal)) < 0){
		//活期结算户可用余额不足以支付购买金额,需检查透支标志
		if(KnaAcct.getIsdrft() == E_YES___.NO){
			throw DpModuleError.DpstComm.E9999("电子账户结算户不允许透支");
		}
	}
	//开户
	input.getOpenInfo().getBase().setCustac(acdc.getCustac());
	
	//属性间关系检查
	OpenSubAcctCheck.DealTransBefore(open);
	
	//交易检查
	OpenSubAcctCheck.TransCheck(open);
}
}
