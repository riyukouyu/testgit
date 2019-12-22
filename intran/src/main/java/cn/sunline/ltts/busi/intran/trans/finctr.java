package cn.sunline.ltts.busi.intran.trans;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRINTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;


public class finctr {

	public static void dealTrans( final cn.sunline.ltts.busi.intran.trans.intf.Finctr.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Finctr.Property property,  final cn.sunline.ltts.busi.intran.trans.intf.Finctr.Output output){
	
		chkParam(input);
		
		String acbrch = BusiTools.getBusiRunEnvs().getCentbr(); //获取省中心机构号
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		
		if(input.getFinctp() == E_PRODTP.FINA){ //理财
			para = KnpParameterDao.selectOne_odb1("InParm.finctr","in", "%", "%", false);
			if(CommUtil.isNull(para)){
				throw DpModuleError.DpstComm.E9999("请配置理财过渡户系统参数");
			}
		} else {
			throw DpModuleError.DpstComm.E9999("暂不支出的产品类型");
		}
		
		String finaBusi = para.getParm_value1(); //过渡户业务编码
		String subFinaB = para.getParm_value2();
		String clarBusi = para.getParm_value3(); //往来户业务编码
		String subClarB = para.getParm_value4();
		
		//内部户借记
		IoInAccount inSrv = SysUtil.getInstance(IoInAccount.class);
		
		if(input.getTrintp() == E_TRINTP.IN101){ //往来户转入至过渡户
			IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
			IaTransOutPro acdrOt_D = SysUtil.getInstance(IaTransOutPro.class);
			IaTransOutPro acdrOt_C = SysUtil.getInstance(IaTransOutPro.class);
			//借记 往来户
			acdrIn.setAcbrch(acbrch);//省中心机构
			acdrIn.setAmntcd(E_AMNTCD.DR);
			acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
//			acdrIn.setInptsr(E_INPTSR.GL00); //交易来源类别
			acdrIn.setBusidn(E_BLNCDN.D); //业务代码方向 
			acdrIn.setCrcycd(input.getCrcycd());
			acdrIn.setSmrycd(input.getSmrycd());
			acdrIn.setToacct(finaBusi);
			acdrIn.setToacna("");
			acdrIn.setTranam(input.getTranam());
			acdrIn.setBusino(clarBusi); //业务编码
			acdrIn.setSubsac(subClarB);//子户号
			
			acdrOt_D = inSrv.ioInAcdr(acdrIn); //内部户借方记账
			//贷记过渡户
			IaAcdrInfo acdrIn2 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
			//para2 = KnpParameterDao.selectOne_odb1("InParm.cupsconfrim","in", E_CLACTP._04.getValue(), "%", true);
			acdrIn2.setAcbrch(input.getBrchno()); //挂账所属机构挂电子账户机构
			acdrIn2.setAmntcd(E_AMNTCD.CR);
			acdrIn2.setTrantp(E_TRANTP.TR); //交易类型 
//			acdrIn2.setInptsr(E_INPTSR.GL00); //交易来源类别
			acdrIn2.setBusidn(E_BLNCDN.C); //业务代码方向 
			acdrIn2.setCrcycd(input.getCrcycd());
			acdrIn2.setSmrycd(input.getSmrycd());
			acdrIn2.setToacct(clarBusi);
			acdrIn2.setToacna(acdrOt_D.getAcctna());
			acdrIn2.setTranam(input.getTranam());
			acdrIn2.setBusino(finaBusi); //业务编码
			acdrIn2.setSubsac(subFinaB);//子户号
			
			acdrOt_C = inSrv.ioInAccr(acdrIn2); //贷记服务
			
			//登记出入金登记簿
			IoSaveIoTransBill.SaveIoBill.InputSetter billInfo = SysUtil.getInstance(IoSaveIoTransBill.SaveIoBill.InputSetter.class);
			
			billInfo.setBrchno(input.getBrchno());//理财过渡户记账机构(理财产品发行机构)
			billInfo.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq());//业务跟踪号busisq//getBstrsq() to getBusisq() 19/4/17 rambo
			billInfo.setCapitp(E_CAPITP.IN998);//到期清算
			billInfo.setCardno(acdrOt_C.getAcctno());//内部户账号
			billInfo.setCrcycd(BusiTools.getDefineCurrency());//币种
			billInfo.setFrondt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
			billInfo.setFronsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
			billInfo.setIoflag(E_IOFLAG.OUT);//出入金标识
			billInfo.setKeepdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期
			billInfo.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());//内部交易码
			billInfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id().toString());//交易渠道
			billInfo.setToacct(acdrOt_D.getAcctno());//系统待清算账号
			billInfo.setToacno(acdrOt_D.getAcctno());//系统待清算账号
			billInfo.setTobrch(acbrch);//系统待清算账户开户机构(省中心机构)
			billInfo.setTranam(input.getTranam());//交易金额
			billInfo.setToscac(acdrOt_D.getAcctno());//系统待清算账号
			billInfo.setTranst(E_TRANST.NORMAL);//交易状态
			
			SysUtil.getInstance(IoSaveIoTransBill.class).saveIoBill(billInfo);
			
		} else if (input.getTrintp() == E_TRINTP.IN102){ //过渡户转入往来户
			//借记过渡户
			IaAcdrInfo acdrIn = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
			IaTransOutPro acdrOt_D = SysUtil.getInstance(IaTransOutPro.class);
			IaTransOutPro acdrOt_C = SysUtil.getInstance(IaTransOutPro.class);
			
			acdrIn.setAcbrch(input.getBrchno());//挂账所属机构挂产品发行机构
			acdrIn.setAmntcd(E_AMNTCD.DR);
			acdrIn.setTrantp(E_TRANTP.TR); //交易类型 
//			acdrIn.setInptsr(E_INPTSR.GL00); //交易来源类别
			acdrIn.setBusidn(E_BLNCDN.D); //业务代码方向 
			acdrIn.setCrcycd(input.getCrcycd());
			acdrIn.setSmrycd(input.getSmrycd());
			acdrIn.setToacct(clarBusi);
			acdrIn.setToacna("");
			acdrIn.setTranam(input.getTranam());
			acdrIn.setBusino(finaBusi); //业务编码
			acdrIn.setSubsac(subFinaB);//子户号
			
			acdrOt_D = inSrv.ioInAcdr(acdrIn); //内部户借方记账
			
			//贷记往来户
			IaAcdrInfo acdrIn2 = SysUtil.getInstance(IaAcdrInfo.class); //内部户借方记账输入
			//para2 = KnpParameterDao.selectOne_odb1("InParm.cupsconfrim","in", E_CLACTP._04.getValue(), "%", true);
			acdrIn2.setAcbrch(acbrch); //省中心机构
			acdrIn2.setAmntcd(E_AMNTCD.CR);
			acdrIn2.setTrantp(E_TRANTP.TR); //交易类型 
//			acdrIn2.setInptsr(E_INPTSR.GL00); //交易来源类别
			acdrIn2.setBusidn(E_BLNCDN.C); //业务代码方向 
			acdrIn2.setCrcycd(input.getCrcycd());
			acdrIn2.setSmrycd(input.getSmrycd());
			acdrIn2.setToacct(finaBusi);
			acdrIn2.setToacna(acdrOt_D.getAcctna());
			acdrIn2.setTranam(input.getTranam());
			acdrIn2.setBusino(clarBusi); //业务编码
			acdrIn2.setSubsac(subClarB);//子户号
			
			acdrOt_C = inSrv.ioInAccr(acdrIn2); //贷记服务
			
			//登记出入金登记簿
			IoSaveIoTransBill.SaveIoBill.InputSetter billInfo = SysUtil.getInstance(IoSaveIoTransBill.SaveIoBill.InputSetter.class);
			
			billInfo.setBrchno(acbrch);//理财往来户记账机构(省中心机构)
			billInfo.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq());//业务跟踪号busisq//getBstrsq() to getBusisq() 19/4/17 rambo
			billInfo.setCapitp(E_CAPITP.IN997);//成立清算
			billInfo.setCardno(acdrOt_C.getAcctno());//往来户
			billInfo.setCuacno(acdrOt_C.getAcctno());//往来户
			billInfo.setCrcycd(BusiTools.getDefineCurrency());//币种
			billInfo.setFrondt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
			billInfo.setFronsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
			billInfo.setIoflag(E_IOFLAG.OUT);//出入金标识
			billInfo.setKeepdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期
			billInfo.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());//内部交易码
			billInfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id().toString());//交易渠道
			billInfo.setToacct(acdrOt_D.getAcctno());//系统待清算账号
			billInfo.setToacno(acdrOt_D.getAcctno());//系统待清算账号
			billInfo.setTobrch(input.getBrchno());//理财过渡户开户机构(产品发行机构)
			billInfo.setTranam(input.getTranam());//交易金额
			billInfo.setToscac(acdrOt_D.getAcctno());//系统待清算账号
			billInfo.setTranst(E_TRANST.NORMAL);//交易状态
			
			SysUtil.getInstance(IoSaveIoTransBill.class).saveIoBill(billInfo);
			
		} else {
			throw DpModuleError.DpstComm.E9999("转账交易类型错误");
		}
		
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._01);
		
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
		
	}

	
	public static void chkParam(final cn.sunline.ltts.busi.intran.trans.intf.Finctr.Input input){
		if(CommUtil.isNull(input.getFinctp())){
			throw DpModuleError.DpstComm.E9999("产品类型不能为空");
		}
		
		if(CommUtil.isNull(input.getSmrycd())){
			throw DpModuleError.DpstComm.E9999("摘要码不能为空");
		}
		
		if(CommUtil.isNull(input.getTranam())){
			throw DpModuleError.DpstComm.E9999("交易金额不能为空");
		}
		
		if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0){
			throw DpModuleError.DpstComm.E9999("请输入正确的交易金额");
		}
		
		if(CommUtil.isNull(input.getTrintp())){
			throw DpModuleError.DpstComm.E9999("转账类型不能为空");
		}
		
		if(CommUtil.isNull(input.getBrchno())){
			throw DpModuleError.DpstComm.E9999("机构号不能为空");
		}
		
		if(CommUtil.isNull(input.getCrcycd())){
			throw DpModuleError.DpstComm.E9999("币种不能为空");
		}
	}
}
