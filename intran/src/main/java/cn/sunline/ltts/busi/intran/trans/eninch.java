package cn.sunline.ltts.busi.intran.trans;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.GlDateTools;
import cn.sunline.ltts.busi.in.serviceimpl.IoInQueryImpl;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlIobl;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACCTTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class eninch {

	private static final BizLog bizlog = BizLogUtil.getBizLog(inchba.class);
	
	public static void BeforeTransferCheck( final cn.sunline.ltts.busi.intran.trans.intf.Eninch.Input Input,  final cn.sunline.ltts.busi.intran.trans.intf.Eninch.Property Property,  final cn.sunline.ltts.busi.intran.trans.intf.Eninch.Output Output){
		
		//获取总账日期(总账还未换日)
        String acctdt = GlDateTools.getGlDateInfo().getSystdt(); //总账日期 
        
    	CommTools.getBaseRunEnvs().setTrxn_date(acctdt);
    	
    	if(CommUtil.isNull(Input.getAcctno()) || CommUtil.isNull(Input.getToacct())){
    		throw InError.comm.E0003("转入转出账户不能为空");
    	}

		if(CommUtil.isNull(Input.getTranam()) || CommUtil.compare(Input.getTranam(),BigDecimal.ZERO)==0){
			throw InError.comm.E0003("交易金额["+Input.getTranam()+"]不能为空或为0");
		}
		if(CommUtil.equals(Input.getAcctno(), Input.getToacct())){
			throw InError.comm.E0003("转入转出账户不能相同");
		}
		//初始借方账号信息
		IoCaTypGenEAccountInfo.QryFacctInfo info = SysUtil.getInstance(IoCaTypGenEAccountInfo.QryFacctInfo.class);
		//初始贷方账号信息
		IoCaTypGenEAccountInfo.QryFacctInfo info1 = SysUtil.getInstance(IoCaTypGenEAccountInfo.QryFacctInfo.class);
		//检查交易金额大于零标志
		if(Input.getQuotfs() == E_YES___.YES && (CommUtil.compare(Input.getTranam(),BigDecimal.ZERO)<0)){
			throw InError.comm.E0004();
		}
		bizlog.debug("---------------------"+CommUtil.equals(Input.getToacct().substring(0, 1),"9") +"  "+ (Input.getToacct().length() == 18));
		//根据账户规则判断账户是内部户还是电子账户
		if(CommUtil.equals(Input.getAcctno().substring(0, 1),"9")){// && (Input.getAcctno().length() == 18) rambo delete
			Property.setIsinac(E_YES___.YES);
		}else{
			Property.setIsinac(E_YES___.NO);
		}
		if(CommUtil.equals(Input.getToacct().substring(0, 1),"9")){// && (Input.getToacct().length() == 18) rambo delete
			Property.setToisin(E_YES___.YES);
		} else {
			Property.setToisin(E_YES___.NO);
		}
		
		if(Property.getIsinac() == E_YES___.YES){
			IoInacInfo acct = SysUtil.getInstance(IoInQueryImpl.class).InacInfoQuery(Input.getAcctno());
			if(CommUtil.isNull(acct)){
				throw InError.comm.E0003("查询内部户["+Input.getAcctno()+"]信息失败，无对应记录");
			}
			Property.setAcctna(acct.getAcctna());
			//Property.setSubsac(acct.getSubsac());
		} else {
			info = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryFacctByCustac(Input.getAcctno());
			if(CommUtil.isNull(info)){
				throw InError.comm.E0003("电子账号["+Input.getAcctno()+"]不存在");
			}
			Property.setCardno(info.getCardno());
			Property.setAcesno(info.getSubsac());
			Property.setCustna(info.getCustna());
			Property.setDeptno(info.getAcctno());
		}
		if(Property.getToisin() == E_YES___.YES){
			IoInacInfo acct = SysUtil.getInstance(IoInQueryImpl.class).InacInfoQuery(Input.getToacct());
			if(CommUtil.isNull(acct)){
				throw InError.comm.E0003("查询账户["+Input.getToacct()+"]失败，无对应记录");
			}
			Property.setToacctna(acct.getAcctna());
			//Property.setTosubsac(acct.getSubsac());
		} else {
			info1 =info = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryFacctByCustac(Input.getToacct());
			if(CommUtil.isNull(info1)){
				throw InError.comm.E0003("电子账号["+Input.getToacct()+"]不存在");
			}
			Property.setTocardno(info1.getCardno());
			Property.setToacesno(info1.getSubsac());
			Property.setTocustna(info1.getCustna());
			Property.setTodeptno(info1.getAcctno());
		}
		//弱电子账户同名校验
		if(Property.getIsinac()==Property.getToisin() && Property.getToisin()==E_YES___.NO){
			if((info.getAccttp()==E_ACCTTP.WACCT || info1.getAccttp() == E_ACCTTP.WACCT) || (!CommUtil.equals(info.getCustno(),info1.getCustno()))){
				throw InError.comm.E0003("弱电子账户下，电子账户间转账需同名");
			}
		}
		//如果是差错处理的调账交易，会传入前置日期和前置流水，根据前置流水和前置日期查询对应ussqno，如果查询不到则赋值空
		if(CommUtil.isNotNull(Input.getFrondt())&&CommUtil.isNotNull(Input.getFronsq())){
			//1.查询出入金记录表
			String fronsq = Input.getFronsq().substring(8);
			bizlog.debug("前置流水========[%s]", fronsq);
			IoKnlIobl iobl =SysUtil.getInstance(IoDpSrvQryTableInfo.class).getKnlIoblOdb1(fronsq, Input.getFrondt());
			
			bizlog.debug("交易号=======[%s]", iobl.getRemark());
			if (CommUtil.isNotNull(iobl)) {
				//判断记录状态，如果不是处理中，则----
				if (iobl.getStatus() == E_TRANST.EXE) {
					Property.setLinkno(iobl.getRemark());
					//2-更新登记簿状态
					iobl.setStatus(E_TRANST.NORMAL);
					SysUtil.getInstance(IoDpSrvQryTableInfo.class).updateKnlIoblOdb1(iobl);
				} else {
					throw InError.comm.E0003("此交易已记账！");
				}
			} else {
				Property.setLinkno("");
			}
		}
	}

	public static void AfterTransferCheck( final cn.sunline.ltts.busi.intran.trans.intf.Eninch.Input Input,  final cn.sunline.ltts.busi.intran.trans.intf.Eninch.Property Property,  final cn.sunline.ltts.busi.intran.trans.intf.Eninch.Output Output){
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
	}
}
