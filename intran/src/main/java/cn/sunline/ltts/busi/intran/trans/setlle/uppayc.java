package cn.sunline.ltts.busi.intran.trans.setlle;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;


public class uppayc {

	public static void CheckTransBefore( final cn.sunline.ltts.busi.serv.trans.online.debt.intf.Uppayc.Input Input,  final cn.sunline.ltts.busi.serv.trans.online.debt.intf.Uppayc.Property Property){
		String crcycd = Input.getCrcycd();
		
		//空则默认为人民币
		if(CommUtil.isNull(crcycd)){
			crcycd = BusiTools.getDefineCurrency();
		}
		//交易金额不能为负数
		if(CommUtil.compare(Input.getTranam(), BigDecimal.ZERO)<0){
			throw InError.comm.E0003("交易金额不能为负数");
		}
		
		//交易金额不能为负数
		if(CommUtil.compare(Input.getFeeeam(), BigDecimal.ZERO)<0){
			throw InError.comm.E0003("手续费金额不能为负数");
		}
		
		BigDecimal feeam = Input.getFeeeam();
		BigDecimal tranam = Input.getTranam();
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();		//交易机构
		BigDecimal summam = BigDecimal.ZERO;
		summam = tranam.add(feeam);	//交易总额=代付金额 + 手续费
		Property.setBrchno(brchno);
		Property.setSummam(summam);
	}

	public static void CheckTransfAfter( final cn.sunline.ltts.busi.serv.trans.online.debt.intf.Uppayc.Input Input,  final cn.sunline.ltts.busi.serv.trans.online.debt.intf.Uppayc.Property Property){
		//平衡性检查 开关开时检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._03);
	}
}
