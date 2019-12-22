package cn.sunline.ltts.busi.intran.trans;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;


public class inactr {
	
	//private static final BizLog bizlog = BizLogUtil.getBizLog(BatchTransactionProcessCallBackImpl.class);
	/**
	 * 内部户交易前检查
	 * */
	public static void BeforeTransferCheck( final cn.sunline.ltts.busi.intran.trans.intf.Inactr.Input Input,  final cn.sunline.ltts.busi.intran.trans.intf.Inactr.Property Property,  final cn.sunline.ltts.busi.intran.trans.intf.Inactr.Output Output){
		//1、帐号比对检查
		if(CommUtil.isNull(Input.getAcctno()) || CommUtil.isNull(Input.getToacct())){
			throw InError.comm.E0003("转入转出账户不能为空");
		}
		
		if(CommUtil.equals(Input.getAcctno(), Input.getToacct())){
			throw InError.comm.E0003("转入转出账户不能相同");
		}
		if(CommUtil.compare(Input.getTranam(), BigDecimal.ZERO)==0){
			throw InError.comm.E0003("交易金额不能为零");
		}
		//2、交易金额大于零控制
		if(Input.getQuotfs()==E_YES___.YES && CommUtil.compare(Input.getTranam(), BigDecimal.ZERO)<0){
			throw InError.comm.E0003("交易金额不能为负数");
		}
		//帐号存在检查
		GlKnaAcct acct = InQuerySqlsDao.sel_GlKnaAcct_by_acct(Input.getAcctno(), false);
		if(CommUtil.isNull(acct)){
			throw InError.comm.E0003("查询内部户帐号["+Input.getAcctno()+"]信息不存在");
		}
		GlKnaAcct toacct = InQuerySqlsDao.sel_GlKnaAcct_by_acct(Input.getToacct(), false);
		if(CommUtil.isNull(toacct)){
			throw InError.comm.E0003("查询内部户帐号["+Input.getToacct()+"]信息不存在");
		}
		if(CommUtil.equals(toacct.getBrchno(), acct.getBrchno())){
			Property.setCrosbr(E_YES___.YES);
		}
		if(!CommUtil.equals(toacct.getBrchno(), acct.getBrchno())){
			Property.setCrosbr(E_YES___.YES);
		}
	}
	
	public static void AfterTransferCheck( final cn.sunline.ltts.busi.intran.trans.intf.Inactr.Input Input,  final cn.sunline.ltts.busi.intran.trans.intf.Inactr.Property Property,  final cn.sunline.ltts.busi.intran.trans.intf.Inactr.Output Output){
		E_CLACTP clactp =null;
		if(E_YES___.YES==Property.getCrosbr()){
			clactp=E_CLACTP._10;
		}
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),clactp);
	}
}
