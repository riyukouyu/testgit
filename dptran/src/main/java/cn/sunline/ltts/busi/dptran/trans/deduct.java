package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;


public class deduct {

	public static void DealEvaluate( final cn.sunline.ltts.busi.dptran.trans.intf.Deduct.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Deduct.Property property,cn.sunline.ltts.busi.dptran.trans.intf.Deduct.Output output){
		property.setAcctno(output.getOtacno());
		property.setAcctna(output.getOtacna());
		property.setOtbrch(output.getOtbrch());
		property.setBrch01(CommTools.getBaseRunEnvs().getTrxn_branch());
		property.setSmry01(BusinessConstants.SUMMARY_KH);
	}

	public static void DealEvaluateBill( final cn.sunline.ltts.busi.dptran.trans.intf.Deduct.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Deduct.Property property,cn.sunline.ltts.busi.dptran.trans.intf.Deduct.Output output){
		property.setInacno(output.getInacno());
		property.setBrch02(BusiTools.getBusiRunEnvs().getCentbr());//内部户记账机构
		property.setBusi02(CommTools.getBaseRunEnvs().getTrxn_seq());//业务跟踪号
		property.setFrdt02(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
		property.setFrsq02(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
		property.setKpdt02(BusiTools.getBusiRunEnvs().getClerdt());//清算日期
		property.setPrcs02(BusiTools.getBusiRunEnvs().getLttscd());//内部交易码
		property.setCrcy02(BusiTools.getDefineCurrency());//币种
		property.setSrtp02(CommTools.getBaseRunEnvs().getChannel_id());//交易渠道
	}

	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.intf.Deduct.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Deduct.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Deduct.Output output){
		//记账内部户业务编码
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("DPTRAN", "DEDUCT", "%","%", true);
		property.setBusino(tblKnpParameter.getParm_value1()); //业务编码
		property.setSubsac(tblKnpParameter.getParm_value2()); //子户号
		property.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());//摘要码
		property.setCapitp(E_CAPITP.DP996);//交易类型
		property.setIoflag(E_IOFLAG.OUT);//出入金标识
		property.setTranst(E_TRANST.NORMAL);
	}

	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.intf.Deduct.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Deduct.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Deduct.Output output){
		//平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._01);
		
	}

}
