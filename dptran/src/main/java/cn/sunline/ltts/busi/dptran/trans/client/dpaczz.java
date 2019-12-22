package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIobl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblDao;
import cn.sunline.ltts.busi.iobus.servicetype.IoApAccount;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppb;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_ACLMFG;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.WaError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_ACCTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;



public class dpaczz {


public static void dealBeforeTrans( final cn.sunline.ltts.busi.dptran.trans.client.intf.Dpaczz.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Dpaczz.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Dpaczz.Output output){
	//交易前检查原流水是否存在
	String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
	String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
	KnlIobl iobl = KnlIoblDao.selectOne_odb1(transq, trandt, false);
	
	if(CommUtil.isNotNull(iobl)){	
		output.setMntrdt(iobl.getTrandt());	//主交易日期
		output.setMntrsq(iobl.getTransq()); // 主流水流水
		output.setMntrtm(iobl.getTrantm());//主交易时间
		return;
	}
	// 检查产品基本信息
	if(CommUtil.isNull(input.getTransfe().getProdcd())){
		throw DpModuleError.DpstComm.BNAS1925();
	}
		IoDpSrvQryTableInfo tablInfo = CommTools.getRemoteInstance(IoDpSrvQryTableInfo.class);
		IoDpKupDppb dppb = SysUtil.getInstance(IoDpKupDppb.class);
		dppb = tablInfo.getKupDppbOdb1(input.getTransfe().getProdcd(), false);
		
    if (CommUtil.isNull(dppb)) {
			throw WaError.Wacct.BNAS0761();
	}	
	if(CommUtil.isNull(input.getTransfe().getAcctna())){
		throw DpModuleError.DpstComm.BNAS0041();				
	}
	
	if(CommUtil.compare(input.getTransfe().getTranam(), BigDecimal.ZERO) <= 0){ //校验交易金额
		throw DpModuleError.DpstComm.BNAS0394();
	}
	
	if(CommUtil.isNull(input.getTransfe().getAcctna())){
		throw DpModuleError.DpstComm.BNAS0173();
	}
	
	
	final cn.sunline.ltts.busi.iobus.servicetype.IoApAccount.queryAccountType.Output out =  SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.IoApAccount.queryAccountType.Output.class);
    final cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo put =  SysUtil.getInstance(cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo.class);
	//内部户验证
    SysUtil.getInstance(IoApAccount.class).queryAccountType(input.getAcctno(), out);
    
    if(out.getAccttp() == E_ACCTTP.IN){//内部账户
    	
    	SysUtil.getInstance(IoInAccount.class).ioInAcdr(put);
    
    }else if(out.getAccttp() == E_ACCTTP.DP){  //电子账户
    	//TODO...IoCaSevAccountLimit
    	
    	//通过Acctno(cardno) 找到custac
    	IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);    	
		IoCaKnaAcdc knaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(input.getAcctno(), false);
    	
		if(CommUtil.isNull(knaAcdc)){
    		throw DpModuleError.DpstComm.BNAS0696();
    	}
		
    	cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter quoInput = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.InputSetter.class);
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output quoOutput = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output.class);
		//额度扣减相关参数
		quoInput.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());//交易机构
		quoInput.setAcctrt(E_ACCTROUTTYPE.PERSON);//客户类型
		quoInput.setLimttp(E_LIMTTP.TR);//额度类型
		quoInput.setServtp("TE");//渠道
		quoInput.setAclmfg(E_ACLMFG._0); //TODO
		quoInput.setAccttp(E_ACCATP.GLOBAL);//账户分类
		quoInput.setSbactp(E_SBACTP._13);//子账户类型
		//quoInput.setCustac(KnaAcdc.getCustac()); //TODO 
		quoInput.setCustac(knaAcdc.getCustac()); //TODO 
		quoInput.setTranam(input.getTranam());//交易金额
		quoInput.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		quoInput.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());

		SysUtil.getInstance(IoCaSevAccountLimit.class).SubAcctQuota(quoInput, quoOutput);	//电子账户系统扣减账户额度
		
		
		final cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn cpl = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn.class);
		
		KnaAcct ioDpKnaAcct = CapitalTransDeal.getSettKnaAcctAc(knaAcdc.getCustac());
		
		cpl.setAcctno(ioDpKnaAcct.getAcctno());//负债账号
		cpl.setCrcycd(input.getCrcycd());//币种
		cpl.setTranam(input.getTransfe().getTranam());//交易金额
		cpl.setCardno(input.getAcctno());//卡号
		cpl.setCustac(knaAcdc.getCustac());//电子账号
		cpl.setToacct(input.getTransfe().getToacct());//对方账号

		SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cpl);//支取记账处理
    }
}
}

