package cn.sunline.ltts.busi.dptran.trans.redpck;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrDetl;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrDetlDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevGenBindCard;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInOpenClose;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.InknbRptrDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCacd;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_RPTRTP;


public class sendrp {

	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Sendrp.Input input,  final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Sendrp.Property property,  final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Sendrp.Output output){
		
		
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		
		//摘要码
		if(CommUtil.isNull(input.getSmrycd())){
			throw DpModuleError.DpstComm.BNAS0195();
		}
		
		//来源方交易流水
		if(CommUtil.isNull(input.getSoursq())){
			throw DpModuleError.DpstComm.BNAS0494();
		}
		
		KnbRptrDetl rptr = KnbRptrDetlDao.selectOne_odb1(transq, trandt, false);
		if(CommUtil.isNotNull(rptr)){
			throw DpModuleError.DpstComm.BNAS0215();
		}
		
		if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0){
			throw DpModuleError.DpstComm.BNAS0622();
		}
		E_RPTRTP rptrtp = input.getRptrtp();
		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		
		
		KnpParameter para = KnpParameterDao.selectOne_odb1("DPTRAN", "SENDRP", rptrtp.getId(), "%", false);
		if (CommUtil.isNull(para)) {
			throw DpModuleError.DpstComm.BNAS0263();
		}
		
		if(rptrtp == E_RPTRTP.SN103 || rptrtp == E_RPTRTP.SN104){ // 个人/商户发红包
			if(CommUtil.isNull(input.getOtcard())){
				throw DpModuleError.DpstComm.BNAS0541();
			}
			
			//mod 20161103
			if(CommUtil.isNotNull(input.getOtcstp())){
				if(input.getOtcstp() != E_ACSETP.SA){
					throw DpModuleError.DpstComm.BNAS0018();
				}
			}

			IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			IoCaKnaAcdc otacdc = caqry.getKnaAcdcOdb2(input.getOtcard(), false);
			if(CommUtil.isNull(otacdc)){
				throw DpModuleError.DpstComm.BNAS0750();
			}
			IoCaKnaCust otcust = caqry.getKnaCustByCustacOdb1(otacdc.getCustac(), true); //转出方电子账户信息
			
			E_ACCATP accatp = cagen.qryAccatpByCustac(otacdc.getCustac()); //转存账户类型
			if(accatp == E_ACCATP.WALLET){
				throw DpModuleError.DpstComm.BNAS0743();
			}
			CapitalTransCheck.ChkAcctstOT(otcust.getCustac());
			CapitalTransCheck.ChkAcctFrozOT(otcust.getCustac()); //检查是否允许转出
			
			
			KnaAcct otacct = CapitalTransDeal.getSettKnaAcctSub(otacdc.getCustac(), E_ACSETP.SA); //转出方子账号
		//	BigDecimal usebal = DpAcctProc.getProductBal(otacdc.getCustac(), otacct.getCrcycd(), false); //获取可用余额
			
			// 可用余额 addby xiongzhao 20161223 
			BigDecimal usebal = SysUtil.getInstance(DpAcctSvcType.class)
					.getAcctaAvaBal(otacdc.getCustac(), otacct.getAcctno(),
							otacct.getCrcycd(), E_YES___.YES, E_YES___.NO);
			
			if(CommUtil.compare(usebal, input.getTranam()) < 0){
				throw DpModuleError.DpstComm.BNAS0543();
			}
			
			property.setAcbrch(otcust.getBrchno()); //内部户机构
			property.setAcctno(otacct.getAcctno());
			property.setCustac(otacdc.getCustac());
			//property.setAuacfg(E_YES___.YES);
			property.setIsacsc(E_YES___.YES);
			property.setInbusi(para.getParm_value1());
			property.setSubsac(para.getParm_value2()); //子户号 add 20161124 slw
			property.setDscrtx(ApSmryTools.getText(input.getSmrycd()));
			
//			property.setAcbrna(otcust.getCustna());
//			property.setAcbrno(input.getOtcard());
			
			property.setReacno(input.getOtcard());
			property.setReacna(otcust.getCustna());
			/*优化，只调一次服务，并且此服务新增一个，需要外调到adm执行    by zhx *
			property.setInacno(SysUtil.getInstance(IoInOpenClose.class).
					ioQueryAndOpen(para.getParm_value1(), otcust.getBrchno(), para.getParm_value2(), input.getCrcycd()).getAcctno());
			property.setInacna(SysUtil.getInstance(IoInOpenClose.class).
					ioQueryAndOpen(para.getParm_value1(), otcust.getBrchno(), para.getParm_value2(), input.getCrcycd()).getAcctna());
			*/
		
			IoInacInfo ioqry  = CommTools.getRemoteInstance(IoInOpenClose.class)
					.IoQueryAndOpenAdm(para.getParm_value1(), otcust.getBrchno(), "", input.getCrcycd());
			property.setReacno(ioqry.getAcctno());
			property.setReacna(ioqry.getAcctna());
			
			//add 20170221 songlw 账户额度参数设值
			
			//检查对方账号是否绑定账户
			IoCaKnaCacd cacd = SysUtil.getInstance(IoCaSevGenBindCard.class).
					selBindByCard(otcust.getCustac(), input.getOtcard(), E_DPACST.NORMAL, false);
			
			property.setCustie(CommUtil.isNotNull(cacd) ? E_YES___.YES : E_YES___.NO); //是否绑定卡标识
			property.setRecpay(null); //收付方标识
			property.setServtp("NR"); //交易渠道 
			property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date()); //渠道来源日期 
			//20170221 songlw 账户额度参数设值 end
		
		}else if(rptrtp == E_RPTRTP.SN101 || rptrtp == E_RPTRTP.SN102){ //行社红包不检查转出账户的状态和类型
			if(CommUtil.isNotNull(input.getOtcard())){
				throw DpModuleError.DpstComm.E9999("行社发红包不能输入卡号");
			}
			if(CommUtil.isNull(input.getOtbrch())){
				throw DpModuleError.DpstComm.BNAS0044();
			}
			
			property.setAcbrch(input.getOtbrch()); //内部户机构
			property.setBusino(para.getParm_value1());
			property.setInbusi(para.getParm_value3());
			property.setSubsac(para.getParm_value4()); //子户号 add 20161124 slw
			property.setDscrtx(ApSmryTools.getText(input.getSmrycd()));
			property.setTrantp(E_TRANTP.TR);
			/*优化  by zhx*
			property.setReacno(SysUtil.getInstance(IoInOpenClose.class).
					ioQueryAndOpen(para.getParm_value1(), input.getOtbrch(), "", input.getCrcycd()).getAcctno());
			property.setReacna(SysUtil.getInstance(IoInOpenClose.class).
					ioQueryAndOpen(para.getParm_value1(), input.getOtbrch(), "", input.getCrcycd()).getAcctna());
			property.setInacno(SysUtil.getInstance(IoInOpenClose.class).
					ioQueryAndOpen(para.getParm_value3(), input.getOtbrch(), para.getParm_value4(), input.getCrcycd()).getAcctno());
			property.setInacna(SysUtil.getInstance(IoInOpenClose.class).
					ioQueryAndOpen(para.getParm_value3(), input.getOtbrch(), para.getParm_value4(), input.getCrcycd()).getAcctna());
			*/
			IoInacInfo ioqry = SysUtil.getInstance(IoInOpenClose.class)
					.ioQueryAndOpen(para.getParm_value1(), input.getOtbrch(), "", input.getCrcycd());
			property.setReacno(ioqry.getAcctno());
			property.setReacna(ioqry.getAcctna());
			
			IoInacInfo ioqry2 = SysUtil.getInstance(IoInOpenClose.class)
					.ioQueryAndOpen(para.getParm_value3(), input.getOtbrch(), para.getParm_value4(), input.getCrcycd());
			property.setInacno(ioqry2.getAcctno());
			property.setInacna(ioqry2.getAcctna());	
			
		}else{
			throw DpModuleError.DpstComm.BNAS0613();
		}
		
		property.setInacbr(property.getAcbrch()); //转入方机构
		property.setIntrtp(E_TRANTP.TR);
		
	}
	
	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Sendrp.Input input,  final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Sendrp.Property property,  final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Sendrp.Output output){
		
		IoSaveIoTransBill bill = SysUtil.getInstance(IoSaveIoTransBill.class);//红包明细登记明细服务
		InknbRptrDetl indptb = SysUtil.getInstance(InknbRptrDetl.class);//红包明细入参符合类型
		E_RPTRTP rptrtp = input.getRptrtp();
		
		//indptb.setSoursq(input.getSoursq()); //来源方交易流水      支付提供产品工厂流水
		//来源方交易流水    支付提供支付前置流水
		indptb.setSoursq(CommTools.getBaseRunEnvs().getInitiator_seq());//来源方交易流水    支付提供支付前置流水
		//indptb.setSourdt(CommTools.getBaseRunEnvs().getTrxn_date()); //来源方交易日期 
		indptb.setSourdt(CommTools.getBaseRunEnvs().getInitiator_date()); //来源方交易日期   支付提供的日期
		if (CommUtil.isNotNull(CommTools.getBaseRunEnvs().getInitiator_system())) {
			indptb.setSournm(CommTools.getBaseRunEnvs().getInitiator_system()); //来源方系统号 
		} else {
			indptb.setSournm("");//来源方系统号 
		}
		
		indptb.setRptrtp(input.getRptrtp()); //红包交易类型 
		
		if(input.getRptrtp() == E_RPTRTP.SN101 || input.getRptrtp() == E_RPTRTP.SN102){
			indptb.setDecard(property.getAcbrno()); //借方卡号 
		}else if (input.getRptrtp() == E_RPTRTP.SN103 || input.getRptrtp() == E_RPTRTP.SN104){
			indptb.setDecard(input.getOtcard()); //借方卡号 
		}
		
		indptb.setDebact(CommUtil.isNull(property.getCustac()) ? property.getAcbrno() : property.getCustac()); //借方账号 
		indptb.setDebnam(CommUtil.isNull(input.getOtname()) ? property.getAcbrna() : input.getOtname()); //借方户名 
		indptb.setDeacct(property.getAcctno()); //负债账号
		indptb.setDecstp(E_ACSETP.SA); //子账号类型
		indptb.setCrcard(property.getInacno()); //贷方卡号 
		indptb.setCrdact(property.getInacno()); //贷方账号 
		indptb.setCrdnam(property.getInacna()); //贷方户名 
		indptb.setTranam(input.getTranam()); //交易金额 
		indptb.setCrcycd(input.getCrcycd()); //货币代号 
		indptb.setDeborg(property.getAcbrch()); //借方机构 
		indptb.setCrdorg(property.getAcbrch()); //贷方机构 
		indptb.setRpcode(input.getRpcode()); //红包编号 
		indptb.setUserid(input.getUserid()); //用户编号 
		indptb.setTranst(E_TRANST.SUCCESS); //交易状态 
		indptb.setDescrb(input.getRptrtp().getLongName()); //交易描述 
		indptb.setChckdt(input.getChckdt()); //对账日期 
		indptb.setKeepdt(input.getKeepdt()); //清算日期 
		indptb.setStady1(CommTools.getBaseRunEnvs().getInitiator_date()); //上送系统日期 
		//上送系统流水号
		indptb.setStady2(CommTools.getBaseRunEnvs().getInitiator_seq()); //上送系统流水号
		indptb.setRemark("0"); //备注  by update cqm备注成功
//		indptb.setBusisq(CommTools.getBaseRunEnvs().getBstrsq());
		indptb.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq());
		indptb.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //主交易流水 
		indptb.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期 
		
		bill.rptrDetlBill(indptb);
		//平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
		
/*		if("EB" == CommTools.getBaseRunEnvs().getChannel_id()){
			//机构、柜员额度验证
			IoBrchUserQt ioBrchUserQt = SysUtil.getInstance(IoBrchUserQt.class);
			ioBrchUserQt.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
			ioBrchUserQt.setBusitp(E_BUSITP.TR);
			ioBrchUserQt.setCrcycd(input.getCrcycd());
			ioBrchUserQt.setTranam(input.getTranam());
			ioBrchUserQt.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());
			SysUtil.getInstance(IoSrvPbBranch.class).selBrchUserQt(ioBrchUserQt);
		}*/
		
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
		
		if(rptrtp == E_RPTRTP.SN103 || rptrtp == E_RPTRTP.SN104){ // 个人/商户发红包
			output.setCaccno(property.getInacno());
			output.setCaccna(property.getInacna());
		}else if(rptrtp == E_RPTRTP.SN101 || rptrtp == E_RPTRTP.SN102){ //行社红包
			output.setDaccno(property.getReacno());
			output.setDaccna(property.getReacna());
			output.setCaccno(property.getInacno());
			output.setCaccna(property.getInacna());
		}
	}

	public static void registerAccountting( final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Sendrp.Input input,  final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Sendrp.Property property,  final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Sendrp.Output output){
		/*
		// 登记会计流水开始
        IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
        cplIoAccounttingIntf.setCuacno(property.getInbusi()); //记账账号-登记核算代码
        cplIoAccounttingIntf.setAcseno(property.getInbusi()); //子账户序号-登记核算代码
        cplIoAccounttingIntf.setAcctno(property.getInbusi()); //负债账号-登记核算代码
        cplIoAccounttingIntf.setProdcd(property.getInbusi()); //产品编号-登记核算代码
        cplIoAccounttingIntf.setDtitcd(property.getInbusi()); //核算口径-登记核算代码
        cplIoAccounttingIntf.setCrcycd(input.getCrcycd()); //币种                 
        cplIoAccounttingIntf.setTranam(input.getTranam()); //交易金额 
        cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
        cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //主交易流水
        cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期 
        cplIoAccounttingIntf.setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch()); //账务机构
        cplIoAccounttingIntf.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
        cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR); //借贷标志

        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.IN); //会计主体类型-内部资金户
        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型-账务流水
        cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE); //余额属性-本金科目
        cplIoAccounttingIntf.setTranms("1010000");//交易信息
        cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
        //登记会计流水
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);*/
	}
}
