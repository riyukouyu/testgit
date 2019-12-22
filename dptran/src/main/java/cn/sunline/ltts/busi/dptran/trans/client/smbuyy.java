package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.YhtDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbRegi;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbRegiDao;
import cn.sunline.ltts.busi.dptran.trans.client.intf.Smbuyy.InputSetter;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbSmsSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpOpenSub;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubSqrd;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BRSTUS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CORDST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRAYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;
import cn.sunline.ltts.busi.sys.type.yht.E_TRANST;



public class smbuyy {

	/**
	 * 定期智能存款购买交易前处理
	 * **/
	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smbuyy.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smbuyy.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smbuyy.Output output){
		
		// TODO 为保证计结息环境测试前端交易日期检查暂时不上，后续需要上版本
		/*String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 当前交易日期
		// 前端交易日期检查
		if (CommUtil.isNull(input.getFrondt())) {
			throw DpModuleError.DpstComm.E9999("前端交易日期输入不能为空");
		}
		if (!CommUtil.equals(trandt, input.getFrondt())) {
			throw DpModuleError.DpstComm.E9999("请稍后再试！");
		}*/
		
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();
		
		// 交易机构检查  add liaojc 20170324
		IoBrchInfo cplIoBrchInfo = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(brchno);
		if (CommUtil.isNull(cplIoBrchInfo)) {
			throw DpModuleError.DpstComm.BNAS1168( brchno );
		}
		
		if (E_BRSTUS.invalid == cplIoBrchInfo.getBrstus()) {
			throw DpModuleError.DpstComm.BNAS1167(brchno);
		}
		
		if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0){
			throw DpModuleError.DpstComm.BNAS0625();
		}
		/*if(input.getOpenInfo().getDppb().getProdmt() == E_FCFLAG.CURRENT || 
				input.getOpenInfo().getDppb().getProdlt() == E_DEBTTP.DP2404){
			throw DpModuleError.DpstComm.E9999("业务细类不是定期存款");
		}
		*/
		
		//检查账户类型
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		
		
		IoDpOpenSub open = input.getOpenInfo();
		
		if(CommUtil.isNull(open.getBase().getCrcycd())){
			throw DpModuleError.DpstComm.BNAS0689();
		}
		
		InputSetter in = SysUtil.getInstance(InputSetter.class);
		//检查钞汇标志是否输入，否则默认为现钞
		if(CommUtil.isNull(input.getCsextg())){
			in.setCsextg(E_CSEXTG.CASH);
		}
		//检查币种为人民币时，钞汇标志是否为现钞，否则设值为现钞
		else if(CommUtil.equals(open.getBase().getCrcycd(), BusiTools.getDefineCurrency()) && input.getCsextg()!=E_CSEXTG.CASH){
			in.setCsextg(E_CSEXTG.CASH);
		}
		
		IoCaKnaAcdc acdc = caqry.getKnaAcdcOdb2(open.getBase().getCardno(), false); //电子账号转换,获取电子账号ID
		if(CommUtil.isNull(acdc)){
			throw DpModuleError.DpstComm.BNAS0754();
		}
		
		//检查传入的子订单号是否重复
		KnbRegi tblKnbRegi = KnbRegiDao.selectOne_db1(input.getCordno(), false);
		
		if(CommUtil.isNotNull(tblKnbRegi)){
			
			if(E_CORDST.SUCCESS == tblKnbRegi.getCordst()){
				output.setMntrsq(tblKnbRegi.getTransq());
				output.setMntrdt(tblKnbRegi.getCocrdt());
				property.setIsflag(E_YES___.YES);
				return;
			}else{
				throw DpModuleError.DpstComm.BNAS0700();
			}
			
		}
		
		input.getOpenInfo().getBase().setCustac(acdc.getCustac());
		
		IoCaKnaCust cust = caqry.getKnaCustByCustacOdb1(acdc.getCustac(), true);
		
		input.getOpenInfo().getBase().setCustno(cust.getCustno());
		
		//检查输入的名字是否与表中一致
		String custna = input.getOpenInfo().getBase().getCustna();
		if(!CommUtil.equals(custna, cust.getCustna())){
			throw DpModuleError.DpstComm.BNAS0529();
		}
		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		
		E_ACCATP accatp = cagen.qryAccatpByCustac(acdc.getCustac());
		if(accatp == E_ACCATP.WALLET){
			throw DpModuleError.DpstComm.BNAS0402();
		}
		//检查账户状态
		CapitalTransCheck.ChkAcctstOT(cust.getCustac());
		
		
		//调用查询电子账户状态字服务
		IoDpFrozSvcType ioDpFrozSvcType = SysUtil.getInstance(IoDpFrozSvcType.class);
		IoDpAcStatusWord  cplAcStatusWord = ioDpFrozSvcType.getAcStatusWord(acdc.getCustac());

		if(E_YES___.YES == cplAcStatusWord.getDbfroz()){
			throw DpModuleError.DpstComm.BNAS0430();
		}
		
		if(E_YES___.YES == cplAcStatusWord.getBrfroz()){
			throw DpModuleError.DpstComm.BNAS0432();
		}
		
		if(E_YES___.YES == cplAcStatusWord.getBkalsp()){
			throw DpModuleError.DpstComm.BNAS0439();
		}
		
		if(E_YES___.YES == cplAcStatusWord.getClstop()){
			throw DpModuleError.DpstComm.BNAS0438();
		}
		
		if(E_YES___.YES == cplAcStatusWord.getOtalsp()){
			throw DpModuleError.DpstComm.BNAS1931();
		}
		
		//查询活期结算户信息
		KnaAcct KnaAcct = CapitalTransDeal.getSettKnaAcctSub(acdc.getCustac(), E_ACSETP.SA); 
		//查询可用余额信息
		BigDecimal usebal = BigDecimal.ZERO;
		
/*		// 获取转存签约明细信息
		IoCaKnaSignDetl cplkna_sign_detl = SysUtil.getInstance(IoCaSevQryTableInfo.class)
				.kna_sign_detl_selectFirst_odb2(KnaAcct.getAcctno(), E_SIGNST.QY, false);
		
		// 存在转存签约明细信息则取资金池可用余额
		if (CommUtil.isNotNull(cplkna_sign_detl)) {
			usebal = DpAcctProc.getProductBal(acdc.getCustac(), open.getBase().getCrcycd(), false);
		} else {
			// 其他取账户余额,正常的支取交易排除冻结金额
			usebal = DpAcctProc.getAcctOnlnblForFrozbl(KnaAcct.getAcctno(), false);
		}*/
		
		// 可用余额 addby xiongzhao 20161223 
		 usebal = SysUtil.getInstance(DpAcctSvcType.class)
				.getAcctaAvaBal(acdc.getCustac(), KnaAcct.getAcctno(),
						open.getBase().getCrcycd(), E_YES___.YES, E_YES___.NO);
		
		//查询冻结余额
		//BigDecimal frozbal = DpFrozTools.getFrozBala(E_FROZOW.AUACCT, acdc.getCustac());
		
		if(CommUtil.compare(usebal, input.getTranam()) < 0){
			throw DpModuleError.DpstComm.BNAS0890();
		}
		//add by zdj 20181112 
		if(open.getDppb().getProdmt() == E_FCFLAG.FIX){
		    if(CommUtil.isNull(input.getOpenInfo().getBase().getProdcd())){
		        throw CaError.Eacct.E0001("产品编号不能为空");
		    }
		    List<String> list = YhtDao.selKupdppbtermByProdcd(input.getOpenInfo().getBase().getProdcd(), false);
		    if(CommUtil.isNotNull(list)){
		        if(!list.contains(input.getOpenInfo().getBase().getDepttm().toString())){
		            throw CaError.Eacct.E0001("输入存期与该产品不符合");
		        }
		    }
		}
		//add end
		//检查是否需要检查透支标志
//		if(CommUtil.compare(input.getTranam(), KnaAcct.getOnlnbl().subtract(frozbal)) < 0){
//			//活期结算户可用余额不足以支付购买金额,需检查透支标志
//			if(KnaAcct.getIsdrft() == E_YES___.NO){
//				throw DpModuleError.DpstComm.E9999("电子账户结算户不允许透支");
//			}
//		}
		//开户
		input.getOpenInfo().getBase().setCustac(acdc.getCustac());
		property.setCustac(acdc.getCustac());
		
		property.setAcctno(KnaAcct.getAcctno()); //结算户
		property.setAcseno(null);
		/*传值错误导致计算余额失败
		 * xiejun 20161227
		 */
		//property.setAuacfg(E_YES___.NO);
		property.setCardno(input.getOpenInfo().getBase().getCardno());
		property.setLinkno(null);
		property.setOpacna(input.getOpenInfo().getBase().getCustna());
		//property.setToacct();
		property.setCrcycd(input.getOpenInfo().getBase().getCrcycd());
		property.setFcflag(input.getOpenInfo().getDppb().getProdmt());
		property.setProdtp(E_PRODTP.DEPO);
		property.setProdcd(input.getOpenInfo().getBase().getProdcd());
		property.setTraype(E_TRAYPE.buyer);
		
		// 摘要码
		BusiTools.getBusiRunEnvs().setSmrycd(BusinessConstants.SUMMARY_TZ);// 投资
		//备注信息
		property.setRemark("购买-"+input.getOpenInfo().getBase().getSprdna());
		//poc
		ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
		apAudit.regLogOnInsertBusiPoc(input.getOpenInfo().getBase().getCardno());
		
	}
	/**
	 * 定期智能存款购买交易后处理
	 * **/
	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smbuyy.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smbuyy.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smbuyy.Output output){
		
		/**
		 * add xj 20180910 短信通知登记
		 */
		IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
//		cplKubSqrd.setAppsid();//APP推送ID 
		cplKubSqrd.setCardno(input.getOpenInfo().getBase().getCardno());//交易卡号  
		cplKubSqrd.setPmvl01(input.getOpenInfo().getBase().getSprdna());//参数01  产品名称 
//		cplKubSqrd.setPmvl02();//参数02    
//		cplKubSqrd.setPmvl03();//参数03    
//		cplKubSqrd.setPmvl04();//参数04    
//		cplKubSqrd.setPmvl05();//参数05    
		cplKubSqrd.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());//内部交易码
		cplKubSqrd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期  
		cplKubSqrd.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());//交易流水  
		cplKubSqrd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());//交易时间  
		IoPbSmsSvcType svcType = SysUtil.getInstance(IoPbSmsSvcType.class);
		svcType.pbTransqReg(cplKubSqrd);
		/**end*/
		
		//平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
		
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
		output.setAcctno(property.getToacct());
		//add by zdj 20181023
        output.setEndate(property.getEndate());//到期日期
	    output.setTranst(E_TRANST.SUCCESS);//订单状态
		//add end
	}
	
	/**
	 * 涉案账户交易信息登记
	 * @Title: prcyInacRegister 
	 * @Description: 涉案账户交易信息登记
	 * @param input
	 * @param property
	 * @param output
	 * @author liaojincai
	 * @date 2016年8月2日 下午2:19:06 
	 * @version V2.3.0
	 */
	public static void prcyInacRegister( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smbuyy.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smbuyy.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smbuyy.Output output){

		E_INSPFG invofg = property.getInvofg();// 转出账号是否涉案

		// 涉案账户交易信息登记
		if (E_INSPFG.INVO == invofg) {

			// 独立事务
			DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
				@Override
				public Void execute() {

					// 获取涉案账户交易信息登记输入信息
					IoCaKnbTrinInput cplKnbTrin = SysUtil.getInstance(IoCaKnbTrinInput.class);
					cplKnbTrin.setTrantp(cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRANTP.TRANSFER);// 交易类型
					cplKnbTrin.setOtcard(input.getOpenInfo().getBase().getCardno());// 转出账号
					cplKnbTrin.setOtacna(input.getOpenInfo().getBase().getCustna());// 转出账号名称
					cplKnbTrin.setOtbrch(CommTools.getBaseRunEnvs().getTrxn_branch());// 转出机构
					cplKnbTrin.setCrcycd(input.getOpenInfo().getBase().getCrcycd());// 币种
					cplKnbTrin.setTranam(input.getTranam());// 交易金额
					cplKnbTrin.setIssucc(E_YES___.NO);// 交易是否成功

					// 涉案账户交易信息登记
					SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(cplKnbTrin);

					return null;
				}
			});

			throw DpModuleError.DpstAcct.BNAS1910();


		}

	}
}
