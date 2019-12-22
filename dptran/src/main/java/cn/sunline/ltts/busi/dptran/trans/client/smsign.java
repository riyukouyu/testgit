package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
//import cn.sunline.ltts.busi.ltts.zjrc.infrastructure.dap.plugin.type.ECustPlugin.E_MEDIUM;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
//import cn.sunline.ltts.busi.dp.acct.OpenSubAcctDeal;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDraw;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDrawDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbRegi;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbRegiDao;
import cn.sunline.ltts.busi.dptran.trans.client.intf.Smbuyy.InputSetter;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpOtherService;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.cf.IoCuTable.IoCifCustAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaSign;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaSignDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCustPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpPostPart;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BRSTUS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CORDST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SAVECT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRAYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_TROTTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AMNTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;
import cn.sunline.ltts.busi.sys.type.yht.E_TRANST;



public class smsign {

	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smsign.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smsign.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smsign.Output output){
		
		// 交易机构检查  add liaojc 20170324
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();
		IoBrchInfo cplIoBrchInfo = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(brchno);
		if (CommUtil.isNull(cplIoBrchInfo)) {
			throw DpModuleError.DpstComm.BNAS1168( brchno );
		}
		
		if (E_BRSTUS.invalid == cplIoBrchInfo.getBrstus()) {
			throw DpModuleError.DpstComm.BNAS1167(brchno );
		}
		
		//输入参数检查
		String frequy = input.getFrequy();
        if(CommUtil.isNull(input.getTrottp())){
          throw CaError.Eacct.BNAS0039();
        }
		
		if(CommUtil.isNull(input.getOpenInfo().getBase().getCardno())){
			throw CaError.Eacct.BNAS0916();
		}
		if(CommUtil.isNull(input.getOpenInfo().getBase().getCrcycd())){
			throw DpModuleError.DpstComm.BNAS1101();
		}
		InputSetter in = SysUtil.getInstance(InputSetter.class);
		//检查钞汇标志是否输入，否 则默认为现钞
		if(CommUtil.isNull(input.getCsextg())){
			in.setCsextg(E_CSEXTG.CASH);
		}
		//检查币种为人民币时，钞汇标志是否为现钞，否则设值为现钞
		if(CommUtil.equals(input.getOpenInfo().getBase().getCrcycd(), BusiTools.getDefineCurrency())
				
				&& input.getCsextg()!=E_CSEXTG.CASH){
			in.setCsextg(E_CSEXTG.CASH);
		}
		
		if(CommUtil.isNull(frequy)){
			frequy = "1DA";
		}
		if(!DateTools2.chkFrequence(frequy)){
			throw CaError.Eacct.BNAS0425();
		}
		
		//检查订单是重复
		KnbRegi tblKnbRegi = KnbRegiDao.selectOne_db1(input.getCordno(), false);
		
		if(CommUtil.isNotNull(tblKnbRegi)){
			
			if(E_CORDST.SUCCESS == tblKnbRegi.getCordst()){
				property.setIsflag(E_YES___.YES);
				return;
			}else{
				throw DpModuleError.DpstComm.BNAS0700();
			}
			
		}
		if(input.getTrottp() == E_TROTTP.DZJE){
		
			if(CommUtil.compare(input.getKeepam(),BigDecimal.ZERO) < 0){
				throw CaError.Eacct.BNAS0049();
			}
			if(CommUtil.compare(input.getMiniam(),BigDecimal.ZERO) < 0){
				throw CaError.Eacct.BNAS0024();
			}
			if(CommUtil.compare(input.getTrmiam(),BigDecimal.ZERO) < 0){
				throw CaError.Eacct.BNAS0038();
			}
			if(CommUtil.compare(input.getUpamnt(),BigDecimal.ZERO) < 0){
				throw CaError.Eacct.BNAS0961();
			}
		}else if(input.getTrottp() == E_TROTTP.DBJE){
			if(CommUtil.compare(input.getSignam(), BigDecimal.ZERO) < 0){
				throw CaError.Eacct.BNAS0404();
			}
		}
		if(CommUtil.isNull(input.getUnsgtp())){
			throw CaError.Eacct.BNAS0670();
		}
		
		if(CommUtil.isNotNull(input.getOtmiam()) && CommUtil.compare(input.getOtmiam(), BigDecimal.ZERO) < 0){
			throw CaError.Eacct.BNAS0038();
		}
		
		if(CommUtil.isNotNull(input.getOtupam()) && CommUtil.compare(input.getOtupam(), BigDecimal.ZERO) < 0){
			throw CaError.Eacct.BNAS0047();
		}
		
		if(input.getOpenInfo().getPost().getDetlfg() == E_YES___.NO && input.getOpenInfo().getDppb().getProdlt() != E_DEBTTP.DP2404){
			throw DpModuleError.DpstComm.BNAS0261();
		}
		//根据电子账号查询结算户信息
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		//获得对内的电子账号ID
		IoCaKnaAcdc kna_acdc = caqry.getKnaAcdcOdb2(input.getOpenInfo().getBase().getCardno(), false);  
		if(CommUtil.isNull(kna_acdc) || kna_acdc.getStatus() == E_DPACST.CLOSE){
			throw DpModuleError.DpstComm.BNAS0750();
		}
		
		input.getOpenInfo().getBase().setCustac(kna_acdc.getCustac());
		
		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		//检查是否已签约
		IoCaKnaSign sign = caqry.getKnaSignOdb1(kna_acdc.getCustac(), E_SIGNTP.ZNCXL, E_SIGNST.QY, false);
		if(CommUtil.isNotNull(sign)){
			throw DpModuleError.DpstComm.BNAS0707();
		}
		//查询结算子账户信息
		//KnaAcct KnaAcct = KnaAcctDao.selectOne_odb7(kna_acdc.getCustac(), E_YES___.YES, true);
		E_ACCATP accatp = cagen.qryAccatpByCustac(kna_acdc.getCustac()); //转出方电子账户类型
		//电子账户类型检查
		if(accatp == E_ACCATP.WALLET){
			throw DpModuleError.DpstComm.BNAS0959();		
		}
		
		KnaAcct KnaAcct = CapitalTransDeal.getSettKnaAcctSub(kna_acdc.getCustac(), E_ACSETP.SA);;
		if(CommUtil.isNull(KnaAcct)){
			throw DpModuleError.DpstComm.BNAS0711();	
		}
		//根据电子账号ID查询电子账户信息
		IoCaKnaCust kna_cust = caqry.getKnaCustByCustacOdb1(kna_acdc.getCustac(), true); 
		
		input.getOpenInfo().getBase().setCustno(kna_cust.getCustno());
		
		CapitalTransCheck.ChkAcctstOT(kna_cust.getCustac());
		//电子账户状态字检查
		//根据电子账号ID查询冻结信息
		//调用查询电子账户状态字服务
		IoDpFrozSvcType ioDpFrozSvcType = SysUtil.getInstance(IoDpFrozSvcType.class);
		IoDpAcStatusWord  cplAcStatusWord = ioDpFrozSvcType.getAcStatusWord(kna_cust.getCustac());

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
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();

		//TODO:检查是否已签约,该步检查放入签约服务中进行处理
		
		//OpenSubAcctCheck check = SysUtil.getInstance(OpenSubAcctCheck.class);
		KnaDraw KnaDraw = KnaDrawDao.selectOne_odb1(KnaAcct.getAcctno(), false);
		if(CommUtil.compare(input.getMiniam(), BigDecimal.ZERO) > 0){ //转入最小金额 
			//TODO:检查活期智能存款子账号存入控制
			IoDpPostPart post = input.getOpenInfo().getPost();
			if(post.getPosttp() == E_SAVECT.COND)
			if(post.getAmntwy() == E_AMNTWY.MNAC || post.getAmntwy() == E_AMNTWY.SCAC){ //控制最小金额或金额范围
				if(CommUtil.compare(input.getMiniam(), post.getMiniam()) < 0){
					throw DpModuleError.DpstComm.BNAS0001();
				}
			}
		}
		if(CommUtil.compare(input.getKeepam(), BigDecimal.ZERO) > 0){ //转出保留最低余额
			//TODO:检查结算户的最低保留金额
			//获取结算户的支取控制信息
			
			if(CommUtil.isNotNull(KnaDraw) && KnaDraw.getIsmamt() == E_YES___.NO){
				if(CommUtil.compare(input.getKeepam(), KnaDraw.getMinibl()) < 0){
					throw DpModuleError.DpstComm.BNAS0048();
				}
			}
		}
		if(CommUtil.compare(input.getTrmiam(), BigDecimal.ZERO) > 0){ //转出最小金额
			//TODO:检查结算户的支取控制
			//有设置支取计划，且控制金额方式为控制最小
			if(CommUtil.isNotNull(KnaDraw) && CommUtil.isNotNull(KnaDraw.getDramwy()) && KnaDraw.getDramwy() == E_AMNTWY.MNAC){
				if(CommUtil.compare(input.getTrmiam(), KnaDraw.getDrmiam()) < 0){
					throw DpModuleError.DpstComm.BNAS0037();
				}
			}
		}
		if(CommUtil.compare(input.getUpamnt(), BigDecimal.ZERO) > 0){ //递增金额
			//TODO:是否检查步长值
			IoDpCustPart cust = input.getOpenInfo().getCust();
			if(CommUtil.isNotNull(cust.getStepvl()) && CommUtil.compare(cust.getStepvl(), BigDecimal.ZERO) > 0){
				if(CommUtil.compare(cust.getStepvl(), input.getUpamnt()) != 0){
					throw DpModuleError.DpstComm.BNAS0960();
				}
			}
		}
		
		if(CommUtil.isNull(input.getSigntm())){
			throw CaError.Eacct.BNAS0403();
		}
//		CommUtil.toEnum(E_TERMCD.class, input.getSignam());
//		property.setEffedt("");
//		if(CommUtil.isNotNull(input.getSigntm())){
//			if(input.getSigntm().toString().startsWith("9")){
//				if(CommUtil.isNull(input.getSigndy()) || CommUtil.compare(input.getSigndy(), 0) <= 0){
//					throw DpModuleError.DpstComm.E9999("自定义期限天数不能为0");
//				}
//				String effedt = DateTools2.dateAdd (input.getSigndy(), trandt);
//				property.setEffedt(effedt);// 签约失效日期
//			}else{
//				String effedt = DateTools2.calDateByTerm(trandt, input.getSigntm());
//				property.setEffedt(effedt);// 签约失效日期
//			}
//		}
		
		if(CommUtil.isNotNull(input.getSigntm())){
			String effedt = DateTools2.calDateByTerm(trandt, input.getSigntm()+"Y");
			property.setEffedt(effedt);// 签约失效日期
		}
		
		input.getOpenInfo().getBase().setCustac(kna_acdc.getCustac());
		property.setCustac(kna_acdc.getCustac());
		property.setAcctno(KnaAcct.getAcctno());
		property.setProdcd(KnaAcct.getProdcd()); //普通活期产品编号
		property.setToprod(input.getOpenInfo().getBase().getProdcd()); //智能存款产品编号
		property.setFrequt(frequy);//频率
		property.setCrcycd(input.getOpenInfo().getBase().getCrcycd());
		property.setFcflag(input.getOpenInfo().getDppb().getProdmt());
		property.setProdtp(E_PRODTP.DEPO);
		property.setTraype(E_TRAYPE.contra);
		
		/**
         * add by huangwh 20181204: start
         * description: 根据参数表中的数据组装属性接口中的冻结信息！
         */
		//根据产品编号查询公共参数表，得到产品冻结信息
        KnpParameter frozPara = KnpParameterDao.selectOne_odb1(input.getOpenInfo().getBase().getProdcd(), "frozpa", "%", "%", true);
		if(!CommUtil.isNull(frozPara)){
	        property.setFrozfl(E_YES___.YES);//冻结标识为是
		    property.setSprdna(input.getOpenInfo().getBase().getSprdna());//产品名称
		    property.setCardno(input.getOpenInfo().getBase().getCardno());//冻结账号
		    property.setFrozam(new BigDecimal(frozPara.getParm_value1()));//冻结金额
		    property.setFroztm(frozPara.getParm_value2());//冻结期限
		}else{
		    property.setFrozfl(E_YES___.NO);//冻结标识为否
		}
		/**
         * add by huangwh 20181204: end
         */
	}
	


	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smsign.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smsign.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smsign.Output output){
//		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq());
	    //add by zdj 20181023
	    output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());//主交易日期
		output.setEndate(property.getEffedt());//失效日期
		output.setTranst(E_TRANST.SUCCESS);//订单状态
		//add end
//		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
		output.setAcctno(property.getToacct());
	}



	/**
	 * 涉案账户交易信息登记
	 * @Title: prcyInacRegister 
	 * @Description: 涉案账户交易信息登记 
	 * @param input
	 * @param property
	 * @param output
	 * @author liaojincai
	 * @date 2016年8月2日 上午11:37:51 
	 * @version V2.3.0
	 */
	public static void prcyInacRegister( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smsign.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smsign.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smsign.Output output){
		
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
					cplKnbTrin.setIssucc(E_YES___.NO);// 交易是否成功

					// 涉案账户交易信息登记
					SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(cplKnbTrin);

					return null;
				}
			});

			throw DpModuleError.DpstAcct.BNAS1910();


		}

	}



	public static void signmq( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smsign.Input input,  
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Smsign.Property property,  
			final cn.sunline.ltts.busi.dptran.trans.client.intf.Smsign.Output output){
			
			String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//系统当前日期
			
			KnpParameter para = KnpParameterDao.selectOne_odb1("SIGNMQ", "%", "%", "%", true);
			
			String bdid = para.getParm_value1();// 服务绑定ID
			
			String mssdid = CommTools.getMySysId();// 随机生成消息ID
			
			String mesdna = para.getParm_value2();// 媒介名称
			
//			E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介
			
			IoDpOtherService dpOtherService = 
					SysUtil.getInstanceProxyByBind (IoDpOtherService.class, bdid);
			
			IoDpOtherService.IoDpSendSignMsg.InputSetter mqInput = 
					SysUtil.getInstance(IoDpOtherService.IoDpSendSignMsg.InputSetter.class);
			//获取客户关联信息
			IoCifCustAccs IoCifCustAccs =
					DpAcctDao.selCifCustAccsByCustno(input.getOpenInfo().getBase().getCustno(), false);
			//获取签约明细信息
			IoCaKnaSignDetl ioCaKnaSignDetl = DpAcctDao.selKnaSignDetlInfo(output.getAcctno(),false);
			
			mqInput.setMsgid(mssdid); //发送消息ID
//			mqInput.setMedium(mssdtp); //消息媒介
			mqInput.setMdname(mesdna); //媒介名称
			mqInput.setSignst(E_SIGNST.QY);
			mqInput.setCustno(IoCifCustAccs.getCustid());
			mqInput.setCustna(input.getOpenInfo().getBase().getCustna());
			mqInput.setCardno(input.getOpenInfo().getBase().getCardno());
			mqInput.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
//			mqInput.setBrchna("");
			mqInput.setProdcd(property.getToprod());
			mqInput.setProdna(input.getOpenInfo().getBase().getSprdna());
			mqInput.setKeepam(input.getKeepam());
			mqInput.setTrmiam(input.getTrmiam());
			mqInput.setTimetm(ioCaKnaSignDetl.getTmstmp());
			mqInput.setTrandt(trandt);
			mqInput.setSigndt(trandt);
			mqInput.setEffedt(property.getEffedt());
			mqInput.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());
			mqInput.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
			mqInput.setFcflag(property.getFcflag());
			dpOtherService.sendSignMsg(mqInput);
	}



	/**
	 * 
	 * <p>Title:updateFrozsq </p>
	 * <p>Description:	冻结成功后，映射负债子户与冻结流水(主交易流水)的关系，为解约时解冻做准备。</p>
	 * @author huangwh
	 * @date   2018年12月4日 
	 * @param input
	 * @param property
	 * @param output
	 */
    public static void updateFrozsq( final cn.sunline.ltts.busi.dptran.trans.client.intf.Smsign.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smsign.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Smsign.Output output){
        if(input.getOpenInfo().getBase().getPddpfg() == E_FCFLAG.CURRENT){//活期，更新活期附加产品信息表
            //活期附加产品信息表
            KnaAcctProd knaAcctProd = KnaAcctProdDao.selectOne_odb1(property.getToacct(),true);
            //更新冻结流水=主交易流水
            knaAcctProd.setObgasi(CommTools.getBaseRunEnvs().getTrxn_seq());//预留字段6
            KnaAcctProdDao.updateOne_odb1(knaAcctProd);
        }else if(input.getOpenInfo().getBase().getPddpfg() == E_FCFLAG.FIX){//定期，更新定期附加产品信息表
            //定期附加产品信息表
            KnaFxacProd knaFxacProd = KnaFxacProdDao.selectOne_odb1(property.getToacct(), true);
            //更新冻结流水=主交易流水
            knaFxacProd.setObgasi(CommTools.getBaseRunEnvs().getTrxn_seq());//预留字段6
            KnaFxacProdDao.updateOne_odb1(knaFxacProd);
        }
    }
}
