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
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrDetl;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrDetlDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevGenBindCard;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.InknbRptrDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCacd;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_RPTRTP;



public class recvrp {

	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Recvrp.Input input,  final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Recvrp.Property property,  final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Recvrp.Output output){
		
		// 输入项非空检查
		if (CommUtil.isNull(input.getRptrtp())) {
			throw DpModuleError.DpstComm.BNAS0675();
		}
		if (CommUtil.isNull(input.getOtbrch())) {
			throw DpModuleError.DpstComm.BNAS0673();
		}
		if (CommUtil.isNull(input.getIncard())) {
			throw DpModuleError.DpstComm.BNAS0030();
		}
		if (CommUtil.isNull(input.getCrcycd())) {
			throw DpModuleError.DpstComm.BNAS1101();
		}
		if (CommUtil.isNull(input.getCsextg())) {
			throw DpModuleError.DpstComm.BNAS1047();
		}
		if (CommUtil.isNull(input.getSmrycd())) {
			throw DpModuleError.DpstComm.BNAS0195();
		}
		if (CommUtil.isNull(input.getTranam())) {
			throw DpModuleError.DpstComm.BNAS0630();
		}
		if (CommUtil.isNull(input.getChckdt())) {
			throw DpModuleError.DpstComm.BNAS0808();
		}
		if (CommUtil.isNull(input.getKeepdt())) {
			throw DpModuleError.DpstComm.BNAS0399();
		}
		if(CommUtil.isNull(input.getSoursq())){
			throw DpModuleError.DpstComm.BNAS0494();
		}
		
		if(CommUtil.isNull(input.getRpsdtp())){
			throw DpModuleError.DpstComm.BNAS0799();
		}
		
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		if(input.getRptrtp() == E_RPTRTP.RV201 || input.getRptrtp() == E_RPTRTP.RV202){
			para = KnpParameterDao.selectOne_odb1("DPTRAN", "RECVRP", "%", "%", true);
			if(input.getRpsdtp() == E_RPTRTP.SN101 || input.getRpsdtp() == E_RPTRTP.SN102){
				property.setSubsac(para.getParm_value2()); //子户号
			}else if(input.getRpsdtp() == E_RPTRTP.SN103){
				property.setSubsac(para.getParm_value3()); //子户号
			}else if(input.getRpsdtp() == E_RPTRTP.SN104){
				property.setSubsac(para.getParm_value4()); //子户号
			}else{
				throw DpModuleError.DpstComm.BNAS0798();
			}
		}else if(input.getRptrtp() == E_RPTRTP.RV203){ //挂账提现
			para = KnpParameterDao.selectOne_odb1("DPTRAN", "RECVRP", input.getRptrtp().getId(), "%", true);
			property.setSubsac(para.getParm_value3()); //子户号
		}else{
			throw DpModuleError.DpstComm.BNAS0674();
		}
		property.setBusino(para.getParm_value1()); //业务编码IA 
		
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		KnbRptrDetl rptr = KnbRptrDetlDao.selectOne_odb1(transq, trandt, false);
		if(CommUtil.isNotNull(rptr)){
			throw DpModuleError.DpstComm.BNAS0215();
		}
		
		if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0){
			throw DpModuleError.DpstComm.BNAS0627();
		}
//		if(input.getIncstp() != E_ACSETP.SA && input.getIncstp() != E_ACSETP.MA){
//			throw DpModuleError.DpstComm.E9999("子账号类型输入有误");
//		}
		
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		IoCaKnaAcdc inacdc = caqry.getKnaAcdcOdb2(input.getIncard(), false);
		if(CommUtil.isNull(inacdc)){
			throw DpModuleError.DpstComm.BNAS0754();
		}
		IoCaKnaCust incust = caqry.getKnaCustByCustacOdb1(inacdc.getCustac(), true); //转出方电子账户信息
		E_ACCATP accatp = cagen.qryAccatpByCustac(inacdc.getCustac()); //转存账户类型
		
//		if(accatp == E_ACCATP.WALLET){
//			if(input.getIncstp() != E_ACSETP.MA){
//				throw DpModuleError.DpstComm.E9999("子账号类型输入有误");
//			}
//		}else{
//			if(input.getIncstp() == E_ACSETP.MA){
//				throw DpModuleError.DpstComm.E9999("子账号类型输入有误");
//			}
//		}
		
		E_CUACST cuacst = ChkAcctstIN(incust.getCustac());
		CapitalTransCheck.ChkAcctFrozIN(incust.getCustac()); //检查是否允许转入
		
		
		
		KnaAcct inacct = CapitalTransDeal.getSettKnaAcctAc(inacdc.getCustac());
		//mod 校验传入子账户类型 20161103
		if (CommUtil.isNotNull(input.getIncstp())) {
			if(CommUtil.isNotNull(input.getIncstp()) && input.getIncstp() != inacct.getAcsetp()){
				throw DpModuleError.DpstComm.BNAS0018();
			}
		}
		
		property.setAcctno(inacct.getAcctno());
		property.setCustac(inacdc.getCustac());
		//property.setAuacfg(E_YES___.YES);
		property.setInbrch(incust.getBrchno());
		property.setCustna(incust.getCustna());
		property.setAcbrch(input.getOtbrch()); //内部户机构
		property.setDscrtx(ApSmryTools.getText(input.getSmrycd()));
		//property.setSmrycd(E_SMRYCD.ZZ);
		property.setTrantp(E_TRANTP.TR);
		property.setTblKnaAcct(inacct);
		property.setCuacst(cuacst);
		
		//add 20170221 songlw 账户额度参数设值
		
		//检查对方账号是否绑定账户
		IoCaKnaCacd cacd = SysUtil.getInstance(IoCaSevGenBindCard.class).
				selBindByCard(inacdc.getCustac(), input.getIncard(), E_DPACST.NORMAL, false);
		
		property.setCustie(CommUtil.isNotNull(cacd) ? E_YES___.YES : E_YES___.NO); //是否绑定卡标识
		property.setRecpay(null); //收付方标识
		property.setServtp("NR"); //交易渠道 
		property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date()); //渠道来源日期 
		//20170221 songlw 账户额度参数设值 end
	}
	
	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Recvrp.Input input,  final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Recvrp.Property property,  final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Recvrp.Output output){
		
		String sournm = CommTools.getBaseRunEnvs().getInitiator_system();
		IoSaveIoTransBill bill = SysUtil.getInstance(IoSaveIoTransBill.class);//红包明细登记明细服务
		InknbRptrDetl indptb = SysUtil.getInstance(InknbRptrDetl.class);//红包明细入参符合类型
		
		//indptb.setSoursq(input.getSoursq()); //来源方交易流水 
		//indptb.setSourdt(CommTools.getBaseRunEnvs().getTrxn_date()); //来源方交易日期 
		indptb.setSoursq(CommTools.getBaseRunEnvs().getInitiator_seq());//来源方交易流水    支付流水
		indptb.setSourdt(CommTools.getBaseRunEnvs().getInitiator_date());//来源方日期  支付日期
		if (CommUtil.isNotNull(sournm)) {
			indptb.setSournm(sournm); //来源方系统号 
		}
		indptb.setRptrtp(input.getRptrtp()); //红包交易类型 
		indptb.setDecard(property.getAcbrno()); //借方卡号 
		indptb.setDebact(property.getAcbrno()); //借方账号 
		indptb.setDebnam(property.getAcbrna()); //借方户名 
		indptb.setCracct(property.getAcctno());	 //负债账号
		indptb.setCrcstp(E_ACSETP.SA); //子账号类型
		indptb.setCrcard(input.getIncard()); //贷方卡号 
		indptb.setCrdact(property.getCustac()); //贷方账号 
		indptb.setCrdnam(property.getCustna()); //贷方户名 
		indptb.setTranam(input.getTranam()); //交易金额 
		indptb.setCrcycd(input.getCrcycd()); //货币代号 
		indptb.setDeborg(property.getAcbrch()); //借方机构 
		indptb.setCrdorg(property.getInbrch()); //贷方机构 
		indptb.setRpcode(input.getRpcode()); //红包编号 
		indptb.setUserid(input.getUserid()); //用户编号 
		indptb.setTranst(E_TRANST.SUCCESS); //交易状态 
		indptb.setDescrb(input.getRptrtp().getLongName()); //交易描述 
		indptb.setChckdt(input.getChckdt()); //对账日期 
		indptb.setKeepdt(input.getKeepdt()); //清算日期 
		indptb.setStady1(CommTools.getBaseRunEnvs().getInitiator_date()); //上送系统日期 
		indptb.setStady2(CommTools.getBaseRunEnvs().getInitiator_seq()); //上送系统流水号
		indptb.setRemark("1"); //备注  by update cqm备注成功
	
//		indptb.setBusisq(CommTools.getBaseRunEnvs().getBstrsq());
		// MsSystemSeq.getTrxnSeq();
		indptb.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //主交易流水 
		indptb.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期 
		indptb.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq());//业务流水号
		bill.rptrDetlBill(indptb);
		
		
		//平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._10);
		
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
	}
	
	public static E_CUACST ChkAcctstIN(String custac){
		
		
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		if (CommUtil.isNotNull(cuacst)) {
			
			if (cuacst == E_CUACST.PRECLOS) { // 预销户
				throw DpModuleError.DpstComm.BNAS0880();

			} else if (cuacst == E_CUACST.CLOSED) { // 销户
				throw DpModuleError.DpstComm.BNAS0883();

			} else if (cuacst == E_CUACST.PREOPEN) { // 预开户
				throw DpModuleError.DpstComm.BNAS0881();

			} else if (cuacst == E_CUACST.OUTAGE) { // 停用
				// throw DpModuleError.DpstComm.E9999("电子账户为停用");

			} else if (cuacst == E_CUACST.NOACTIVE) { // 未激活
				throw DpModuleError.DpstComm.BNAS0885();
			}

		} else {
			throw DpModuleError.DpstComm.BNAS1206();
		}
		
		return cuacst;
	}

	public static void DealAcctStatAndSett( final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Recvrp.Input input,  final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Recvrp.Property property,  final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Recvrp.Output output){
		//修改账户状态，休眠转正常结息
		//转入账户的电子账户信息，转入账户的结算户信息或钱包户信息
		CapitalTransDeal.dealAcctStatAndSett(property.getCuacst(), property.getTblKnaAcct());
	}

	public static void registerAccountting( final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Recvrp.Input input,  final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Recvrp.Property property,  final cn.sunline.ltts.busi.dptran.trans.redpck.intf.Recvrp.Output output){
		/*// 登记会计流水开始
        IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
        cplIoAccounttingIntf.setCuacno(property.getBusino()); //记账账号-登记核算代码
        cplIoAccounttingIntf.setAcseno(property.getBusino()); //子账户序号-登记核算代码
        cplIoAccounttingIntf.setAcctno(property.getBusino()); //负债账号-登记核算代码
        cplIoAccounttingIntf.setProdcd(property.getBusino()); //产品编号-登记核算代码
        cplIoAccounttingIntf.setDtitcd(property.getBusino()); //核算口径-登记核算代码
        cplIoAccounttingIntf.setCrcycd(input.getCrcycd()); //币种                 
        cplIoAccounttingIntf.setTranam(input.getTranam()); //交易金额 
        cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
        cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //主交易流水
        cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期 
        cplIoAccounttingIntf.setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch()); //账务机构
        cplIoAccounttingIntf.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
        cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR); //借贷标志

        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.IN); //会计主体类型-内部资金户
        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型-账务流水
        cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE); //余额属性-本金科目
        cplIoAccounttingIntf.setTranms("1020000");//交易信息
        cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
        cplIoAccounttingIntf.setToacct(input.getIncard());
        cplIoAccounttingIntf.setToacna(input.getInname());
        //登记会计流水
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);*/
	}
}
