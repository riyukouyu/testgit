package cn.sunline.ltts.busi.dp.acct;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.busi.pb.encrypt.DecryptConstant;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbDfir;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctAddt;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctAddtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctMatu;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctMatuDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDraw;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDrawDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDrawPlan;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacMatu;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacMatuDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacSort;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacSortDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdr;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdrDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxdrPlan;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxsv;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxsvDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxsvPlan;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSave;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSaveDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSavePlan;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.AddSubAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDfirPart;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpGentTab;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpOpenSub;
import cn.sunline.ltts.busi.iobus.type.pb.IoItpfComplexType.IoCxlilv;
import cn.sunline.ltts.busi.iobus.type.pb.IoItpfComplexType.IoInrPreferPlan;
import cn.sunline.ltts.busi.iobus.type.pb.IoItpfComplexType.IoLayInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoItpfComplexType.IoLayInfoIn;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CAINPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRFLPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MADTBY;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVPLGN;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PLANFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PLSTAT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PSAMTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_INTRTY;
/**
 * 描述:开负债子账户处理。
 * 
 * */
public class OpenSubAcctDeal {
	private static final BizLog log = BizLogUtil.getBizLog(OpenSubAcctDeal.class);
	public static AddSubAcctOut OpenSub(IoDpOpenSub openInfo){
		OpenSubAcctCheck.DealTransBefore(openInfo);
		OpenSubAcctCheck.TransCheck(openInfo);
		AddSubAcctOut openOut = SysUtil.getInstance(AddSubAcctOut.class);
		if(CommUtil.isNull(openInfo.getBase().getAcctno())){
			
			if(openInfo.getBase().getPddpfg() == E_FCFLAG.CURRENT){
				
			    OpenSubAcctDeal.OpenCurrSub(openInfo); //活期负债账户表处理
				OpenSubAcctDeal.DealCurrPost(openInfo); //存入控制
				OpenSubAcctDeal.DealCurrDraw(openInfo); //支取控制
				OpenSubAcctDeal.DealCurrPostpl(openInfo); //存入计划
				OpenSubAcctDeal.DealCurrDrawpl(openInfo); //支取计划
				OpenSubAcctDeal.DealCurrMatu(openInfo); //到期计划
				OpenSubAcctDeal.AcctProdInfo(openInfo);//活期产品附加信息
				
			}else if(openInfo.getBase().getPddpfg() == E_FCFLAG.FIX){
				OpenSubAcctDeal.OpenFxacSub(openInfo);
				OpenSubAcctDeal.DealFxacPost(openInfo);
				OpenSubAcctDeal.DealFxacDraw(openInfo);
				OpenSubAcctDeal.DealFxacPostpl(openInfo);
				OpenSubAcctDeal.DealFxacDrawpl(openInfo);
				OpenSubAcctDeal.DealFxacMatu(openInfo);
				OpenSubAcctDeal.FxacProdInfo(openInfo);//定期产品附加信息
				if(openInfo.getPost().getDetlfg() == E_YES___.YES){
					OpenSubAcctDeal.DealFxacSort(openInfo.getBase().getAcctno());
				}
			}
			OpenSubAcctDeal.DealDfir(openInfo); //违约利率信息
			/*
			 * JF Modify：即富收单存款产品不计息，暂不处理利率信息。
			if(E_INBEFG.INBE == openInfo.getIntr().getInbefg()) {
				OpenSubAcctDeal.DealIntr(openInfo); //利率利息信息
			}
			 */
			OpenSubAcctDeal.DealIntr(openInfo); //利率利息信息
			OpenSubAcctDeal.DealAcin(openInfo);//利率利息信息处理
		}
		
		openOut.setAcctno(openInfo.getBase().getAcctno());
		openOut.setPddpfg(openInfo.getBase().getPddpfg());
		openOut.setProdcd(openInfo.getBase().getProdcd());
		openOut.setDebttp(openInfo.getDppb().getProdlt());
		
		//add by zdj 20181023
        //增加到期日期
        if(openInfo.getBase().getPddpfg() == E_FCFLAG.CURRENT){//活期存款
            if(openInfo.getCust().getMadtby() == E_MADTBY.SET){ //指定到期日
                openOut.setEndate(openInfo.getCust().getMatudt()); //到期日期
            }else if(openInfo.getCust().getMadtby() == E_MADTBY.TERMCD){ //开日日期+周期
                if(openInfo.getBase().getDepttm().getValue().startsWith("9")){ //自定义存期
                    
                    openOut.setEndate(DateTools2.dateAdd (openInfo.getBase().getDeptdy().intValue(),CommTools.getBaseRunEnvs().getTrxn_date()));
                }else{ //
                    openOut.setEndate(DateTools2.calDateByTerm(CommTools.getBaseRunEnvs().getTrxn_date(), openInfo.getBase().getDepttm()));
                }
            }
        }else if(openInfo.getBase().getPddpfg() == E_FCFLAG.FIX){//定期存款
            if(openInfo.getCust().getMadtby() == E_MADTBY.SET){ //指定到期日
                openOut.setEndate(openInfo.getCust().getMatudt()); //到期日期
            }else if(openInfo.getCust().getMadtby() == E_MADTBY.TERMCD || openInfo.getCust().getMadtby() == E_MADTBY.T_OR_S){ 
                //定期型产品 首次存入日计算到期日期和开户日计算到期日是一样的
                if(openInfo.getBase().getDepttm().getValue().startsWith("9")){ //自定义存期
                    openOut.setEndate(DateTools2.dateAdd (openInfo.getBase().getDeptdy().intValue(), CommTools.getBaseRunEnvs().getTrxn_date()));
                }else{ //
                    openOut.setEndate(DateTools2.calDateByTerm(CommTools.getBaseRunEnvs().getTrxn_date(), openInfo.getBase().getDepttm()));
                }
            }
        }
        //add end
        /*
    	 * JF Modify：冲正事件登记，先注释掉。
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
		
		cplInput.setTranev(ApUtil.TRANS_EVENT_OPENSUB); //负债子账号开户冲正登记

		cplInput.setCustac(openInfo.getBase().getCustac()); //电子账户号
    	cplInput.setTranac(openInfo.getBase().getAcctno()); //负债账号
    	cplInput.setEvent1(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期
    	cplInput.setEvent2(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //交易流水
    	cplInput.setEvent3(openInfo.getDppb().getProdmt().getValue()); //定活标志

    	ApStrike.regBook(cplInput);
    	
		IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);    		
		apinput.setReversal_event_id(cplInput.getTranev());
		apinput.setInformation_value(SysUtil.serialize(cplInput));
		MsEvent.register(apinput, true);
		*/
		return openOut;
	}
	/**
	 * 活期负债账户处理
	 * **/
	public static void OpenCurrSub(IoDpOpenSub open){
		KnaAcct acct = SysUtil.getInstance(KnaAcct.class);
		String acctno = BusiTools.getAcctno();
		acct.setAcctno(acctno);
		
		acct.setAcsetp(open.getBase().getAcsetp());
		acct.setAcctna(open.getBase().getCustna());
//		acct.setTmacctna(DecryptConstant.maskName(open.getBase().getCustna()));
		acct.setAcctcd(open.getBase().getAcctcd());
		acct.setAcctst(E_DPACST.NORMAL);
		acct.setAccttp(open.getBase().getAccttp()); //是否结算户
		
		
		acct.setBgindt(CommTools.getBaseRunEnvs().getTrxn_date()); //起息日期
		acct.setMatudt(null);
		if(open.getCust().getMadtby() == E_MADTBY.SET){ //指定到期日
			acct.setMatudt(open.getCust().getMatudt()); //到期日期
		}else if(open.getCust().getMadtby() == E_MADTBY.TERMCD){ //开日日期+周期
			if(open.getBase().getDepttm().getValue().startsWith("9")){ //自定义存期
				acct.setMatudt(DateTools2.dateAdd (open.getBase().getDeptdy().intValue(), acct.getBgindt()));
			}else{ //
				acct.setMatudt(DateTools2.calDateByTerm(CommTools.getBaseRunEnvs().getTrxn_date(), open.getBase().getDepttm()));
			}
		}else if(open.getCust().getMadtby() == E_MADTBY.T_OR_S){ //首次存入日
			//TODO:首次存入日计算到期日
		}
		acct.setDeptdy(open.getBase().getDeptdy());
		acct.setBkmony(BigDecimal.ZERO); //备用金额
		acct.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch()); //机构
		acct.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id()); //法人
		acct.setCrcycd(open.getBase().getCrcycd()); //币种
		if(CommUtil.equals(open.getBase().getCrcycd(),BusiTools.getDefineCurrency())){
			acct.setCsextg(E_CSEXTG.CASH);	//钞汇标志
		}else{
			acct.setCsextg(open.getBase().getCsextg());
		}		
		acct.setCustac(open.getBase().getCustac()); //电子账户
		//acct.setTrsvtp(open.getMatu().get); //转存方式
		acct.setCustno(open.getBase().getCustno()); //客户号
		acct.setDebttp(open.getDppb().getProdlt()); //储蓄种类 业务细类
		//acct.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date()); //维护日期
		//acct.setUpbldt(CommTools.getBaseRunEnvs().getTrxn_date()); //余额更新日期 调用存入服务时写入
		acct.setHdmimy(open.getDraw().getMinibl()); //账户最小留存金额
		acct.setHdmxmy(open.getPost().getMaxibl()); //账户最大留存金额
		acct.setOnlnbl(BigDecimal.ZERO); //账户余额 调用存入服务时写入
		acct.setOpendt(CommTools.getBaseRunEnvs().getTrxn_date()); //开户日期
		acct.setOpensq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //开户流水
		acct.setOpmony(open.getBase().getOpmony()); //开户金额
		acct.setPddpfg(open.getBase().getPddpfg()); //定活标志
		acct.setProdcd(open.getBase().getProdcd()); //产品编号
		acct.setSleptg(E_YES___.NO); //形态转移标志
		acct.setSpectp(open.getBase().getSpectp()); //账户性质
		acct.setDepttm(open.getBase().getDepttm()); //存期
		acct.setIsdrft(open.getDppb().getIsdrft()); //是否允许透支
		acct.setLstrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		// modify by zhuxw 20171013 此处重复赋值，钞汇标识由180行到184行定义
//		acct.setCsextg(E_CSEXTG.EXCHANGE);
		open.getBase().setAcctno(acctno); //负债账号放入开户信息中,在后面会用到
		open.getCust().setMatudt(acct.getMatudt()); //到期日期设置到开户控制部件中
		acct.setLastbl(BigDecimal.ZERO);//新开户上日账户余额舒为0， add by xieqq -20170624

		KnaAcctDao.insert(acct);
		
		//添加负债账户附加表信息
		if (E_FCFLAG.CURRENT == open.getBase().getPddpfg()) {
			KnaAcctAddt addt = SysUtil.getInstance(KnaAcctAddt.class);
			addt.setAccatp(open.getBase().getAccatp());
			addt.setAcctno(acctno);
			addt.setHigham(new BigDecimal(0));

			KnaAcctAddtDao.insert(addt);
		}
		
	}
	/**
	 * 定期负债账户处理
	 * **/
	public static void OpenFxacSub(IoDpOpenSub open){
		KnaFxac acct = SysUtil.getInstance(KnaFxac.class);
		String acctno = BusiTools.getAcctno();
		
		
		acct.setAcctno(acctno);
		
		acct.setAcsetp(open.getBase().getAcsetp());
		acct.setAcctna(open.getBase().getCustna());
		acct.setAcctcd(open.getBase().getAcctcd()); //核算代码
		acct.setAcctst(E_DPACST.NORMAL);
		acct.setAccttp(open.getBase().getAccttp()); //结算户标志
		
		acct.setBgindt(CommTools.getBaseRunEnvs().getTrxn_date()); //起息日期
		acct.setBkmony(new BigDecimal(0));
		acct.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
		acct.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		acct.setCrcycd(open.getBase().getCrcycd());
		acct.setCsextg(E_CSEXTG.CASH); //钞汇标志
		//acct.setTrsvtp(open.getMatu().get); //转存方式
		acct.setCustac(open.getBase().getCustac()); //电子账号
		acct.setCustno(open.getBase().getCustno()); //客户号
		acct.setDebttp(open.getDppb().getProdlt()); //储蓄种类
		//acct.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
		acct.setHdmimy(open.getDraw().getMinibl()); //账户最小留存金额
		acct.setHdmxmy(open.getPost().getMaxibl()); //账户最大留存金额
		acct.setOnlnbl(new BigDecimal(0));
		acct.setOpendt(CommTools.getBaseRunEnvs().getTrxn_date());
		acct.setOpensq(CommTools.getBaseRunEnvs().getTrxn_seq());
		acct.setOpmony(open.getBase().getOpmony());
		acct.setPddpfg(open.getBase().getPddpfg()); //定活标志
		acct.setProdcd(open.getBase().getProdcd());
		acct.setSleptg(E_YES___.NO);
		acct.setSpectp(open.getBase().getSpectp()); //负债账户性质
		acct.setDepttm(open.getBase().getDepttm()); //存期
		acct.setIsdrft(open.getDppb().getIsdrft()); //是否允许透支
		acct.setMatudt(null);
		//到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
		if(open.getCust().getMadtby() == E_MADTBY.SET){ //指定到期日
			acct.setMatudt(open.getCust().getMatudt()); //到期日期
		}else if(open.getCust().getMadtby() == E_MADTBY.TERMCD || open.getCust().getMadtby() == E_MADTBY.T_OR_S){ 
			//定期型产品 首次存入日计算到期日期和开户日计算到期日是一样的
			if(open.getBase().getDepttm().getValue().startsWith("9")){ //自定义存期
				acct.setMatudt(DateTools2.dateAdd (open.getBase().getDeptdy().intValue(), acct.getBgindt()));
			}else{ //
				acct.setMatudt(DateTools2.calDateByTerm(CommTools.getBaseRunEnvs().getTrxn_date(), open.getBase().getDepttm()));
			}
		}
		acct.setDeptdy(open.getBase().getDeptdy());
		acct.setLstrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		open.getBase().setAcctno(acctno); //负债账号放入开户信息中,在后面会用到
		open.getCust().setMatudt(acct.getMatudt()); //到期日期设置到开户控制部件中
		KnaFxacDao.insert(acct);
	}
	/**
	 * 活期负债账户存入控制处理
	 * **/
	public static void DealCurrPost(IoDpOpenSub open){
		
		KnaSave save = SysUtil.getInstance(KnaSave.class);
		save.setAcctno(open.getBase().getAcctno());
		save.setPosttp(open.getPost().getPosttp()); //存入控制方式
		save.setPostwy(open.getPost().getPostwy()); //存入控制方法
		//save.setAdjtpd(acctOut.getAdjtpd()); //存入计划调整周期
		save.setAmntwy(open.getPost().getAmntwy()); //存入金额控制方式
		save.setTimewy(open.getPost().getTimewy()); //存入次数控制方式
		save.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		//save.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
		
		save.setMaxiam(open.getPost().getMaxiam()); //单次存入最大金额
		save.setMiniam(open.getPost().getMiniam()); //单次存入最小金额
		save.setMaxitm(open.getPost().getMaxitm()); //最大存入次数
		save.setMinitm(open.getPost().getMinitm()); //最小存入次数
		
		//save.setEndtwy(acctOut.getDredwy()); //存入计划结束日期方式
		/*
		 * JF Modify：即富收单存款产品增加非空检查。
		 */
		if(CommUtil.isNotNull(open.getPostpl())) {
			save.setGentwy(open.getPostpl().getGentwy()); //存入计划生成方式
			save.setPlanpd(open.getPostpl().getPlanpd()); //存入计划生成周期
			save.setSvlewy(open.getPostpl().getSvlewy()); //存入漏补方式
			save.setMaxisp(open.getPostpl().getMaxisp()); //最大补足次数
			save.setDfltsd(open.getPostpl().getDfltsd()); //存入违约标准
			save.setDfltwy(open.getPostpl().getDfltwy()); //存入违约处理方式
			save.setSvlepd(open.getPostpl().getSvlepd()); //漏存补足宽限期
			save.setSvletm(open.getPostpl().getSvletm()); //漏存次数
			save.setPlanfg(open.getPostpl().getPlanfg()); //设置存入计划标志
			//save.setPlanwy(acctOut.getPlanwy()); //存入计划调整方式
			save.setPscrwy(open.getPostpl().getPscrwy()); //存入计划控制方式
			save.setResvam(new BigDecimal(0));	 //实际存入金额
			save.setSpbkfg(E_YES___.NO);		 //存入计划违约标志
			//save.setDetlfg(open.getPost().getDetlfg()); //是否明细汇总
			save.setPsamtp(open.getPostpl().getPsamtp()); //存入计划金额类型
		}
		KnaSaveDao.insert(save);
	}
	/**
	 * 定期负债账户存入控制处理
	 * */
	public static void DealFxacPost(IoDpOpenSub open){
		KnaFxsv fxsv = SysUtil.getInstance(KnaFxsv.class);
		fxsv.setAcctno(open.getBase().getAcctno());
		
		fxsv.setPosttp(open.getPost().getPosttp());//存入控制方式
		fxsv.setPostwy(open.getPost().getPostwy());//存入控制方法
		
		//fxsv.setAdjtpd(acctOut.getAdjtpd()); //存入计划调整周期
		fxsv.setAmntwy(open.getPost().getAmntwy()); //存入金额控制方式
		fxsv.setTimewy(open.getPost().getTimewy()); //存入次数控制方式
		fxsv.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		//fxsv.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
		
		fxsv.setMaxiam(open.getPost().getMaxiam()); //单次存入最大金额
		fxsv.setMiniam(open.getPost().getMiniam()); //单次存入最小金额
		fxsv.setMaxitm(open.getPost().getMaxitm()); //最大存入次数
		fxsv.setMinitm(open.getPost().getMinitm()); //最小存入次数
		
		//save.setEndtwy(acctOut.getDredwy()); //存入计划结束日期方式
		fxsv.setGentwy(open.getPostpl().getGentwy()); //存入计划生成方式
		fxsv.setPlanpd(open.getPostpl().getPlanpd()); //存入计划生成周期
		fxsv.setSvlewy(open.getPostpl().getSvlewy()); //存入漏补方式
		fxsv.setMaxisp(open.getPostpl().getMaxisp()); //最大补足次数
		fxsv.setDfltsd(open.getPostpl().getDfltsd()); //存入违约标准
		fxsv.setDfltwy(open.getPostpl().getDfltwy()); //存入违约处理方式
		fxsv.setSvlepd(open.getPostpl().getSvlepd()); //漏存补足宽限期
		fxsv.setSvletm(open.getPostpl().getSvletm()); //漏存次数
		fxsv.setPlanfg(open.getPostpl().getPlanfg()); //设置存入计划标志
		//fxsv.setPlanwy(acctOut.getPlanwy()); //存入计划调整方式
		fxsv.setPscrwy(open.getPostpl().getPscrwy()); //存入计划控制方式
		fxsv.setResvam(new BigDecimal(0));	 //实际存入金额
		fxsv.setSpbkfg(E_YES___.NO);		 //存入计划违约标志
		fxsv.setPsamtp(open.getPostpl().getPsamtp()); //存入计划金额类型
		
		fxsv.setDetlfg(open.getPost().getDetlfg()); //是否明细汇总
		KnaFxsvDao.insert(fxsv);
	}
	/**
	 * 活期负债账户支取控制处理
	 * **/
	public static void DealCurrDraw(IoDpOpenSub open){
		KnaDraw KnaDraw = SysUtil.getInstance(KnaDraw.class);
		KnaDraw.setAcctno(open.getBase().getAcctno());
		KnaDraw.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		KnaDraw.setDrawtp(open.getDraw().getDrawtp()); //支取控制方式
		KnaDraw.setCtrlwy(open.getDraw().getCtrlwy()); //支取控制方法
		//KnaDraw.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
		KnaDraw.setDpbkfg(E_YES___.NO);	//支取计划违约标志
		KnaDraw.setDprosq(null);		//支取保护顺序
		//KnaDraw.setDradpd(acctOut.getDradpd()); //支取计划调整周期
		KnaDraw.setDramwy(open.getDraw().getDramwy()); //支取金额控制方式
		KnaDraw.setDrmiam(open.getDraw().getDrmiam()); //单次支取最小金额
		KnaDraw.setDrmxam(open.getDraw().getDrmxam()); //单次支取最大金额
		
		KnaDraw.setDrtmwy(open.getDraw().getDrtmwy()); //支取次数控制方式
		KnaDraw.setDrmitm(open.getDraw().getDrmitm()); //最小支取次数
		KnaDraw.setDrmxtm(open.getDraw().getDrmxtm()); //最大支取次数
		/*
		 * JF Modify：即富收单存款产品增加非空检查。
		 */
		if(CommUtil.isNotNull(open.getDrawpl())) {
			KnaDraw.setDrdfsd(open.getDrawpl().getDrdfsd()); //支取违约标准
			//KnaDraw.setDredwy(acctOut.getDredwy()); //支取计划结束日期方式
			KnaDraw.setDrdfwy(open.getDrawpl().getDrdfwy()); //支取违约处理方式
			KnaDraw.setDrcrwy(open.getDrawpl().getDrcrwy()); //支取计划控制方式
			KnaDraw.setDradwy(open.getDrawpl().getDradwy()); //支取计划生成方式
			KnaDraw.setPlanpd(open.getDrawpl().getGendpd()); //支取计划生成周期
			KnaDraw.setSetpwy(open.getDrawpl().getSetpwy()); //是否设置支取计划
		}
		KnaDraw.setDwperi(null);	//支取间隔
		//KnaDraw.setOrdrwy(acctOut.getOrdrwy()); //支取预约方式
		KnaDraw.setProtmd(null); //保护性质
		KnaDraw.setRedqam(new BigDecimal(0)); //实际支取金额
		KnaDraw.setRedwnm(new Long(0)); //实际支取次数
		//KnaDraw.setTermwy(acctOut.getTermwy()); // 支取计划调整周期方式
		KnaDraw.setDrrule(open.getDraw().getDrrule()); //支取规则
		KnaDraw.setIsmamt(open.getDraw().getIsmibl()); //是否允许小于最低留存余额
		KnaDrawDao.insert(KnaDraw);
	}
	/**
	 * 定期负债账户支取控制处理
	 * **/
	public static void DealFxacDraw(IoDpOpenSub open){
		KnaFxdr KnaFxdr = SysUtil.getInstance(KnaFxdr.class);
		KnaFxdr.setAcctno(open.getBase().getAcctno());
		KnaFxdr.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		
		KnaFxdr.setDrawtp(open.getDraw().getDrawtp());//支取控制方式
		KnaFxdr.setCtrlwy(open.getDraw().getCtrlwy());//支取控制方法
		//KnaFxdr.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
		KnaFxdr.setDpbkfg(E_YES___.NO);	//支取计划违约标志
		KnaFxdr.setDprosq(null);		//支取保护顺序
		//KnaFxdr.setDradpd(acctOut.getDradpd()); //支取计划调整周期
		KnaFxdr.setDramwy(open.getDraw().getDramwy()); //支取金额控制方式
		KnaFxdr.setDrmiam(open.getDraw().getDrmiam()); //单次支取最小金额
		KnaFxdr.setDrmxam(open.getDraw().getDrmxam()); //单次支取最大金额
		
		KnaFxdr.setDrtmwy(open.getDraw().getDrtmwy()); //支取次数控制方式
		KnaFxdr.setDrmitm(open.getDraw().getDrmitm()); //最小支取次数
		KnaFxdr.setDrmxtm(open.getDraw().getDrmxtm()); //最大支取次数
		KnaFxdr.setDrdfsd(open.getDrawpl().getDrdfsd()); //支取违约标准
		
		//KnaDraw.setDredwy(acctOut.getDredwy()); //支取计划结束日期方式
		KnaFxdr.setDrdfwy(open.getDrawpl().getDrdfwy()); //支取违约处理方式
		KnaFxdr.setDrcrwy(open.getDrawpl().getDrcrwy()); //支取计划控制方式
		KnaFxdr.setDradwy(open.getDrawpl().getDradwy()); //支取计划生成方式
		KnaFxdr.setPlanpd(open.getDrawpl().getGendpd()); //支取计划生成周期
		
		KnaFxdr.setDwperi(null);	//支取间隔
		//KnaDraw.setOrdrwy(acctOut.getOrdrwy()); //支取预约方式
		KnaFxdr.setProtmd(null); //保护性质
		KnaFxdr.setRedqam(new BigDecimal(0)); //实际支取金额
		KnaFxdr.setRedwnm(new Long(0)); //实际支取次数
		KnaFxdr.setSetpwy(open.getDrawpl().getSetpwy()); //是否设置支取计划
		//KnaDraw.setTermwy(acctOut.getTermwy()); // 支取计划调整周期方式
		KnaFxdr.setDrrule(open.getDraw().getDrrule()); //支取规则
		KnaFxdr.setIsmamt(open.getDraw().getIsmibl()); //是否允许小于最低留存余额
		KnaFxdrDao.insert(KnaFxdr);
	}
	/**
	 * 负债活期账户存入计划处理
	 * **/
	public static void DealCurrPostpl(IoDpOpenSub open){
		/*
		 * JF Add：即富收单存款产品增加非空检查。
		 */
		if(CommUtil.isNull(open.getPostpl())) {
			return;
		}
		if(open.getPostpl().getPlanfg() == E_PLANFG.SET){
			
			Options<IoDpGentTab> options = null;
			List<KnaSavePlan> list = new ArrayList<>();
			if(open.getPostpl().getGentwy() == E_SVPLGN.T1){ //按计划生成
				options = GetPlanNm(open.getBase().getDepttm(), open.getPostpl().getPlanpd(), open.getBase().getDeptdy());
			}else{
				options = open.getPostpldt();
			}
			if(options.size() > 0){
				for(IoDpGentTab gent : options){
					KnaSavePlan plan = SysUtil.getInstance(KnaSavePlan.class);
					plan.setAcctno(open.getBase().getAcctno());
					plan.setCrcycd(open.getBase().getCrcycd()); //
					plan.setMaxiam(open.getPost().getMaxiam()); //单次存入最大金额
					plan.setMiniam(open.getPost().getMiniam()); //单次存入最小金额
					//plan.setPlmony(open.getBase().getOpmony().multiply(BigDecimal.valueOf(options.size()))); //计划总额
					plan.setPlmony(open.getBase().getOpmony()); //计划总额
					plan.setPloved(gent.getPloved()); //计划结束日期
					plan.setPlstad(gent.getPlstad()); //计划开始日期
					plan.setPlstat(E_PLSTAT.NODL); //计划处理状态
					plan.setPltime(1L); //计划次数
					plan.setResvam(BigDecimal.ZERO); //实际存入总额
					plan.setResvnm(0L); //实际存入次数
					plan.setSeqnum(gent.getSeqnum()); //序号
					plan.setSpbkfg(E_YES___.NO); //是否违约标志
					plan.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
					//plan.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
					list.add(plan);
				}
				DaoUtil.insertBatch(KnaSavePlan.class, list);
			}
		}
	}
	/**
	 * 负债活期账户支取计划处理
	 * **/
	public static void DealCurrDrawpl(IoDpOpenSub open){
		/*
		 * JF Add：即富收单存款产品增加非空检查。
		 */
		if(CommUtil.isNull(open.getDrawpl())) {
			return;
		}
		if(open.getDrawpl().getSetpwy() == E_YES___.YES){
			
			Options<IoDpGentTab> options = null;
			List<KnaDrawPlan> list = new ArrayList<>();
			if(open.getPostpl().getGentwy() == E_SVPLGN.T1){ //按计划生成
				options = GetPlanNm(open.getBase().getDepttm(), open.getDrawpl().getGendpd(), open.getBase().getDeptdy());
			}else{
				options = open.getDrawpldt();
			}
			if(options.size() > 0){
				BigDecimal avgAmt = BigDecimal.ZERO;
				BigDecimal lstAmt = BigDecimal.ZERO;
				int count = 0;
				if(open.getPostpl().getPsamtp() == E_PSAMTP.YES){
					avgAmt = open.getBase().getOpmony().divide(BigDecimal.valueOf(options.size()), 2, BigDecimal.ROUND_HALF_UP);
					lstAmt = open.getBase().getOpmony().subtract(avgAmt.multiply(BigDecimal.valueOf(options.size() - 1L)));
				}
				
				for(IoDpGentTab gent : options){
					KnaDrawPlan plan = SysUtil.getInstance(KnaDrawPlan.class);
					plan.setAcctno(open.getBase().getAcctno());
					plan.setCrcycd(open.getBase().getCrcycd()); //
					plan.setDrmxam(open.getPost().getMaxiam()); //单次存入最大金额
					plan.setDrmiam(open.getPost().getMiniam()); //单次存入最小金额
					
					if(count == options.size()){
						plan.setPlmony(lstAmt); //计划总额
					}else{
						plan.setPlmony(avgAmt); //计划总额
					}
					plan.setPloved(gent.getPloved()); //计划结束日期
					plan.setPlstad(gent.getPlstad()); //计划开始日期
					plan.setPlstat(E_PLSTAT.NODL); //计划处理状态
					plan.setPltime(1L); //计划次数
					plan.setRedqam(BigDecimal.ZERO); //实际存入总额
					plan.setRedwnm(0L); //实际存入次数
					plan.setSeqnum(gent.getSeqnum()); //序号
					plan.setDpbkfg(E_YES___.NO); //是否违约标志
					plan.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
					//plan.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
					list.add(plan);
				}
				DaoUtil.insertBatch(KnaDrawPlan.class, list);
			}
		}
	}
	/**
	 * 负债定期账户存入计划处理
	 * **/
	public static void DealFxacPostpl(IoDpOpenSub open){
		if(open.getPostpl().getPlanfg() == E_PLANFG.SET){
			
			Options<IoDpGentTab> options = null;
			List<KnaFxsvPlan> list = new ArrayList<>();
			if(open.getPostpl().getGentwy() == E_SVPLGN.T1){ //按计划生成
				options = GetPlanNm(open.getBase().getDepttm(), open.getPostpl().getPlanpd(), open.getBase().getDeptdy());
			}else{
				options = open.getPostpldt();
			}
			if(options.size() > 0){
				
				for(IoDpGentTab gent : options){
					KnaFxsvPlan plan = SysUtil.getInstance(KnaFxsvPlan.class);
					plan.setAcctno(open.getBase().getAcctno());
					plan.setCrcycd(open.getBase().getCrcycd()); //
					plan.setMaxiam(open.getPost().getMaxiam()); //单次存入最大金额
					plan.setMiniam(open.getPost().getMiniam()); //单次存入最小金额
					//plan.setPlmony(open.getBase().getOpmony().multiply(BigDecimal.valueOf(options.size()))); //计划总额
					plan.setPlmony(open.getBase().getOpmony()); //计划总额
					plan.setPloved(gent.getPloved()); //计划结束日期
					plan.setPlstad(gent.getPlstad()); //计划开始日期
					plan.setPlstat(E_PLSTAT.NODL); //计划处理状态
					plan.setPltime(1L); //计划次数
					plan.setResvam(BigDecimal.ZERO); //实际存入总额
					plan.setResvnm(0L); //实际存入次数
					plan.setSeqnum(gent.getSeqnum()); //序号
					plan.setSpbkfg(E_YES___.NO); //是否违约标志
					plan.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
					//plan.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
					list.add(plan);
				}
				DaoUtil.insertBatch(KnaFxsvPlan.class, list);
			}
		}
	}
	
	/**
	 * 负债定期账户支取计划处理
	 * **/
	public static void DealFxacDrawpl(IoDpOpenSub open){
		if(open.getDrawpl().getSetpwy() == E_YES___.YES){
			
			Options<IoDpGentTab> options = null;
			List<KnaFxdrPlan> list = new ArrayList<>();
			if(open.getPostpl().getGentwy() == E_SVPLGN.T1){ //按计划生成
				options = GetPlanNm(open.getBase().getDepttm(), open.getDrawpl().getGendpd(), open.getBase().getDeptdy());
			}else{
				options = open.getDrawpldt();
			}
			if(options.size() > 0){
				
				
				BigDecimal avgAmt = BigDecimal.ZERO;
				BigDecimal lstAmt = BigDecimal.ZERO;
				int count = 0;
				if(open.getPostpl().getPsamtp() == E_PSAMTP.YES){
					avgAmt = open.getBase().getOpmony().divide(BigDecimal.valueOf(options.size()), 2, BigDecimal.ROUND_HALF_UP);
					lstAmt = open.getBase().getOpmony().subtract(avgAmt.multiply(BigDecimal.valueOf(options.size() - 1L)));
				}
				
				for(IoDpGentTab gent : options){
					count = count + 1;
					KnaFxdrPlan plan = SysUtil.getInstance(KnaFxdrPlan.class);
					plan.setAcctno(open.getBase().getAcctno());
					plan.setCrcycd(open.getBase().getCrcycd()); //
					plan.setDrmxam(open.getPost().getMaxiam()); //单次存入最大金额
					plan.setDrmiam(open.getPost().getMiniam()); //单次存入最小金额
					
					if(count == options.size()){
						plan.setPlmony(lstAmt); //计划总额
					}else{
						plan.setPlmony(avgAmt); //计划总额
					}
					
					
					plan.setPloved(gent.getPloved()); //计划结束日期
					plan.setPlstad(gent.getPlstad()); //计划开始日期
					plan.setPlstat(E_PLSTAT.NODL); //计划处理状态
					plan.setPltime(1L); //计划次数
					plan.setRedqam(BigDecimal.ZERO); //实际存入总额
					plan.setRedwnm(0L); //实际存入次数
					plan.setSeqnum(gent.getSeqnum()); //序号
					plan.setDpbkfg(E_YES___.NO); //是否违约标志
					plan.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
					//plan.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
					list.add(plan);
				}
				DaoUtil.insertBatch(KnaFxdrPlan.class, list);
			}
		}
	}
	/**
	 * 活期负债账户到期控制处理
	 * **/
	public static void DealCurrMatu(IoDpOpenSub open){
		if(CommUtil.isNotNull(open.getMatu())){
			KnaAcctMatu matu = SysUtil.getInstance(KnaAcctMatu.class);
			matu.setAcctno(open.getBase().getAcctno()); //负债账号
			matu.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
			//matu.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
			matu.setCrcycd(open.getBase().getCrcycd());
			matu.setDelyfg(open.getMatu().getDelyfg()); //是否根据存款顺延到期日
			matu.setDumpnm(open.getMatu().getTrintm()); //可转存次数
			matu.setFestdl(open.getMatu().getFestdl()); //遇节假日处理方式
			matu.setMatupd(open.getMatu().getMatupd()); //到期宽限期
			matu.setTrdpfg(open.getMatu().getTrdpfg()); //允许转存标志
			matu.setTrinwy(open.getMatu().getTrinwy()); //转存利率调整方式
			matu.setTrpdfg(open.getMatu().getTrpdfg()); //是否可用更换转存产品号
			matu.setTrprod(open.getMatu().getTrprod()); //转存产品编号
			matu.setTrsvtp(open.getMatu().getTrsvtp()); //转存方式
			matu.setTrdptm(open.getMatu().getTrdptm()); //转存产品存期
			matu.setUndump(0); //已转存次数
			KnaAcctMatuDao.insert(matu);
		}
	}
	/**
	 * 定期负债账户到期控制处理
	 */
	public static void DealFxacMatu(IoDpOpenSub open){
		if(CommUtil.isNotNull(open.getMatu())){
			KnaFxacMatu matu = SysUtil.getInstance(KnaFxacMatu.class);
			matu.setAcctno(open.getBase().getAcctno()); //负债账号
			matu.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
			//matu.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
			matu.setCrcycd(open.getBase().getCrcycd());
			matu.setDelyfg(open.getMatu().getDelyfg()); //是否根据存款顺延到期日
			matu.setDumpnm(open.getMatu().getTrintm()); //可转存次数
			matu.setFestdl(open.getMatu().getFestdl()); //遇节假日处理方式
			matu.setMatupd(open.getMatu().getMatupd()); //到期宽限期
			matu.setTrdpfg(open.getMatu().getTrdpfg()); //允许转存标志
			matu.setTrinwy(open.getMatu().getTrinwy()); //转存利率调整方式
			matu.setTrpdfg(open.getMatu().getTrpdfg()); //是否可用更换转存产品号
			matu.setTrprod(open.getMatu().getTrprod()); //转存产品编号
			matu.setTrsvtp(open.getMatu().getTrsvtp()); //转存方式
			matu.setTrdptm(open.getMatu().getTrdptm()); //转存产品存期
			matu.setUndump(0); //已转存次数
			KnaFxacMatuDao.insert(matu);
		}
	}
	/**
	 * 违约支取利率利息处理
	 * **/
	public static void DealDfir(IoDpOpenSub open){
		if(CommUtil.isNotNull(open.getDfir()) && open.getDfir().size() > 0){
			
			List<KnbDfir> list = new ArrayList<>();
			
			Options<IoDpDfirPart> dfirs = open.getDfir();
			for(IoDpDfirPart dfir : dfirs){
				KnbDfir KnbDfir = SysUtil.getInstance(KnbDfir.class);
				KnbDfir.setAcctno(open.getBase().getAcctno());
				KnbDfir.setBsinam(dfir.getBsinam()); //违约基准结息金额来源
				KnbDfir.setBsindt(dfir.getBsindt()); //违约基准结息起始日来源
				KnbDfir.setBsinrl(dfir.getBsinrl()); //基准结息规则
				KnbDfir.setCrcycd(open.getBase().getCrcycd()); 
				KnbDfir.setDrdein(dfir.getDrdein()); //支取是否扣除已支付利息
				KnbDfir.setInadtp(dfir.getInadtp()); //违约利息调整类型
				KnbDfir.setIncdtp(dfir.getIncdtp()); //利率代码类型
				KnbDfir.setInclfg(dfir.getInclfg()); //利率靠档标志
				KnbDfir.setInedsc(dfir.getInedsc()); //违约结息终止日来源
				KnbDfir.setIntrcd(dfir.getIntrcd()); //利率代码
				KnbDfir.setInsrwy(dfir.getInsrwy()); //违约利率确定方式
				KnbDfir.setIntrwy(dfir.getIntrwy()); //利率靠档方式
				KnbDfir.setTeartp(dfir.getTeartp()); //违约支取利息类型
				KnbDfir.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
				KnbDfir.setIntrdt(dfir.getIntrdt()); //利率确定日方式
				KnbDfir.setLevety(dfir.getLevety()); //靠档类型
				//dfir.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
				list.add(KnbDfir);
			}
			
			DaoUtil.insertBatch(KnbDfir.class, list);
		}
	}
	/**
	 * 利率利息信息处理
	 * **/
	public static void DealAcin(IoDpOpenSub open){

		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String nextTranDt = CommTools.getBaseRunEnvs().getNext_date();
		KnbAcin acin = SysUtil.getInstance(KnbAcin.class);
		acin.setAcctno(open.getBase().getAcctno());//账号
		acin.setIntrtp(open.getIntr().getIntrtp()); //利率类型
		acin.setCrcycd(open.getBase().getCrcycd()); //币种
		acin.setInbefg(open.getIntr().getInbefg()); //计息标志
		acin.setTxbefg(open.getIntr().getTxbefg());//计税标志
		acin.setTxbebs(open.getIntr().getTxbebs());//计息基础
		acin.setHutxfg(open.getIntr().getHutxfg());//舍弃角分计息标志
		acin.setInammd(open.getIntr().getInammd()); //计息金额模式
		acin.setBldyca(open.getIntr().getBldyca());//平均余额天数计算方式
		acin.setTxbefr(open.getIntr().getTxbefr());//结息频率
		acin.setReprwy(open.getIntr().getReprwy());//重订价利息处理方式
		acin.setIntrcd(open.getIntr().getIntrcd()); //利率编号
		acin.setTaxecd(open.getIntr().getTaxecd()); //税率编号

		acin.setInprwy(open.getIntr().getInprwy()); //利率重定价方式
		acin.setInadlv(open.getIntr().getInadlv()); //利率调整频率
		acin.setFvrbfg(open.getIntr().getFvrbfg()); //优惠变化调整优惠标志
		//acin.setFvrblv(intr.getFvrblv());
		//acin.setLafvdt(trandt);
		//计算出下次优惠更新日期
		//String sYhxcriqi = DpPublic.getNextPeriod(trandt, nextTranDt, intr.getFvrblv());
		//acin.setNxfvdt(sYhxcriqi);//优惠下次更新日
		acin.setNxindt(trandt); //下次计息日
		if(CommUtil.isNotNull(open.getIntr().getTxbefr())){
			String sNextDate = DpPublic.getNextPeriod(trandt, nextTranDt, open.getIntr().getTxbefr());
			acin.setNcindt(sNextDate); //下次结息日
		}
		
		acin.setInclfg(open.getIntr().getInclfg()); //利率靠档标志
		acin.setIntrwy(open.getIntr().getIntrwy()); //利率靠档方式
		
		acin.setIntrdt(open.getIntr().getIntrdt()); //利率确定日方式
		acin.setLevety(open.getIntr().getLevety()); //靠档类型
		
		acin.setLydttp(open.getIntr().getLydttp()); //分层明细积数调整方式
		acin.setIrwptp(open.getIntr().getCycltp());  //平均余额天数计算方式周期类型
		acin.setIrwpdy(open.getIntr().getSpeday()); //指定天数
		acin.setIsrgdt(open.getIntr().getIsrgdt()); //是否登记分层明细
		
		//acin.setLuindt(); //上次利率更新日
		//acin.setNuindt(); //下次利率更新日
		//acin.setLaindt(); //上次计息日期
		//acin.setLcindt(); //下次计息日
		//acin.setEdindt(); //止息日
		
		acin.setOpendt(trandt);//开户日期
		acin.setBgindt(trandt);//起息日期
		acin.setPlanin(BigDecimal.ZERO);//计提利息
		acin.setLastdt(trandt); //最近更新日期
		acin.setPlblam(BigDecimal.ZERO); //计息累计余额(积数)
		acin.setNxdtin(BigDecimal.ZERO); //上计提日利率
		acin.setMustin(BigDecimal.ZERO);//应缴税金
		acin.setLsinop(E_INDLTP.CAIN); //上次利息操作
		acin.setIndtds(0); //计息天数
		acin.setEvrgbl(BigDecimal.ZERO); //平均余额
		acin.setCutmin(BigDecimal.ZERO); //本期利息
		acin.setCutmis(BigDecimal.ZERO); //本期利息税
		acin.setCutmam(BigDecimal.ZERO);//本期积数
		acin.setAmamfy(BigDecimal.ZERO); //本年累计积数
		acin.setLyamam(BigDecimal.ZERO); //上年累计积数
		acin.setDiffin(BigDecimal.ZERO); //应加/减利息
		acin.setDiffct(BigDecimal.ZERO); //应加/减积数
		acin.setLsinsq(String.valueOf(0));//上次利息序号
		
		acin.setProdcd(open.getBase().getProdcd()); //产品号
		acin.setDetlfg(open.getPost().getDetlfg()); //明细汇总
		acin.setPddpfg(open.getBase().getPddpfg()); //定活标志
		acin.setLaamdt(trandt); //积数更新日期
		
		acin.setIncdtp(open.getIntr().getIncdtp()); //利率代码类型
		acin.setLyinwy(open.getIntr().getLyinwy()); //分层计息方式 全额、超额
		KnbAcinDao.insert(acin);
	}
	
	/**
	 * 客户利率处理
	 */
	public static void DealIntr(IoDpOpenSub open){
		
		/*
		 * JF Modify：外调公共利率服务。
		IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
		 */
		IoSrvPbInterestRate pbpub = SysUtil.getRemoteInstance(IoSrvPbInterestRate.class);
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		//分层:分档利率,且靠档标志为不靠档
		if(open.getIntr().getIncdtp() == E_IRCDTP.LAYER && open.getIntr().getInclfg() == E_YES___.NO){
			IoLayInfoIn  layerIn = SysUtil.getInstance(IoLayInfoIn.class);
			/*
			 * JF Modify：外调公共利率服务。
			IoSrvPbInterestRate pfIntr = SysUtil.getInstance(IoSrvPbInterestRate.class);
			 */
			IoSrvPbInterestRate pfIntr = SysUtil.getRemoteInstance(IoSrvPbInterestRate.class);
			
			layerIn.setIntrcd(open.getIntr().getIntrcd());
			layerIn.setCrcycd(open.getBase().getCrcycd());
			layerIn.setIntrkd(E_INTRTY.DP); //存款利率
//			layerIn.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
			
			Options<IoLayInfo> options = pfIntr.getLayerIntr(layerIn);
			if(CommUtil.isNull(options) || options.size() <= 0){
				throw DpModuleError.DpstComm.BNAS0162();
			}
			for(IoLayInfo layer : options){
				KubInrt inrt = SysUtil.getInstance(KubInrt.class);
				
				inrt.setAcctno(open.getBase().getAcctno()); //账号
				inrt.setIntrtp(open.getIntr().getIntrtp()); //利息类型
				inrt.setIndxno(layer.getInplsq());  //顺序号
				inrt.setIntrcd(open.getIntr().getIntrcd()); //利率编码
				inrt.setIntrwy(open.getIntr().getIntrwy()); //利率靠档方式
				inrt.setIncdtp(open.getIntr().getIncdtp()); //利率代码类型
				inrt.setLyinwy(open.getIntr().getLyinwy()); //分层计息方式
				
				inrt.setLvamot(layer.getLvamlm()); //层次金额下限
				inrt.setLvindt(layer.getRfirtm()); //层次存期
				inrt.setLvaday(layer.getDayllm()); 
				inrt.setOpintr(layer.getIntrvl()); //开户利率
				inrt.setBsintr(layer.getBaseir()); //基准利率
				inrt.setIrflby(layer.getFlirwy()); //浮动方式
				if(layer.getFlirwy() == E_IRFLPF.POINT){
					inrt.setInflpo(layer.getFlirvl()); //浮动点数
				}else if(layer.getFlirwy() == E_IRFLPF.RATE){
					inrt.setInflrt(layer.getFlirrt()); //浮动百分比
				}
				
				IoCxlilv cplCxlilv = GetPrefer(open);
				
				//执行利率
				inrt.setCuusin(layer.getIntrvl().add(cplCxlilv.getFlirvl()).add(layer.getIntrvl().multiply(cplCxlilv.getFlirrt().divide(BigDecimal.valueOf(100)))));
				//inrt.setCuusin(layer.getIntrvl()); //当前执行利率
				
				//inrt.setRealin(BigDecimal.ZERO);
				inrt.setIsfavo(E_YES___.YES); //是否优惠
				//inrt.setPfirwy(E_IRFLPF.); //优惠浮动方式
				inrt.setFavovl(cplCxlilv.getFlirvl()); //优惠浮动值
				inrt.setFavort(cplCxlilv.getFlirrt()); //利率浮动百分比
				inrt.setLacain(BigDecimal.ZERO);
				inrt.setLastbl(BigDecimal.ZERO); //上日余额
				inrt.setLastdt(trandt); //上日余额更新日期
				
				inrt.setClvsmt(BigDecimal.ZERO); //当前层次积数
				inrt.setClvamt(BigDecimal.ZERO); //当前层级计息金额
				inrt.setClvudt(""); //当前层次计息金额更新日期
				
				KubInrtDao.insert(inrt);
			}
		}else{
			
			//账户利率信息
			KubInrt inrt = SysUtil.getInstance(KubInrt.class);
			inrt.setAcctno(open.getBase().getAcctno()); //账号
			inrt.setIntrtp(open.getIntr().getIntrtp()); //利息类型
			inrt.setIndxno(1L);  //顺序号
			inrt.setIntrcd(open.getIntr().getIntrcd()); //利率编码
			inrt.setIntrwy(open.getIntr().getIntrwy()); //利率靠档方式
			inrt.setIncdtp(open.getIntr().getIncdtp()); //利率代码类型
			inrt.setLyinwy(open.getIntr().getLyinwy()); //分层计息方式
			
			inrt.setLvindt(open.getBase().getDepttm()); //存期
			
			//IntrPublicEntity entity = new IntrPublicEntity();
			IoPbIntrPublicEntity entity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
			entity.setCrcycd(open.getBase().getCrcycd());
			entity.setDepttm(open.getBase().getDepttm());
			entity.setIntrcd(open.getIntr().getIntrcd());
			entity.setIncdtp(open.getIntr().getIncdtp());
			entity.setTrandt(trandt);
			entity.setIntrwy(open.getIntr().getIntrwy());
			entity.setBgindt(trandt);
			entity.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
			entity.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
			entity.setTranam(open.getBase().getOpmony());
			entity.setCainpf(E_CAINPF.T1); //分档利率使用，暂时默认存期
			
			
			//到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
			if (CommUtil.isNotNull(open.getBase().getDepttm())) {
				String termcd = open.getBase().getDepttm().getValue();
				if(CommUtil.equals(termcd.substring(0, 1),"9")){
					entity.setEdindt(DateTools2.dateAdd (open.getBase().getDeptdy().intValue(), trandt));
				}else{
					entity.setEdindt(DateTools2.calDateByTerm(trandt, open.getBase().getDepttm()));			
				}
			}
			
			if(CommUtil.isNull(entity.getEdindt())){
				entity.setEdindt("20991231"); //无到期日设置为最大
			}
			
			//计算利率
			entity = pbpub.countInteresRate(entity);
			
			//获取优惠利率
			IoCxlilv cplCxlilv = GetPrefer(open);
			
			//-------优惠后的利率也不能超过关联的基础利率的最大值  mod by leipeng 20170213  start------

			log.debug("基础利率:"+entity.getBaseir()+"--最大浮动比例："+entity.getFlmxsc()+"--最小浮动比例："+entity.getFlmxsc());
			//利率能达到的最大值
		    BigDecimal flmxsc = entity.getBaseir().multiply(BigDecimal.ONE.add(entity.getFlmxsc().divide(BigDecimal.valueOf(100))));
		  
		    BigDecimal flmnsc = entity.getBaseir().multiply(BigDecimal.ONE.add(entity.getFlmnsc().divide(BigDecimal.valueOf(100))));
		    
		    //优惠后的利率
		    BigDecimal intrprf = entity.getIntrvl().add(cplCxlilv.getFlirvl()).add(entity.getIntrvl().multiply(cplCxlilv.getFlirrt().divide(BigDecimal.valueOf(100))));
		    
		    //最后的执行利率
		    BigDecimal endvel = intrprf;
		    
		    if(CommUtil.compare(intrprf, flmxsc)>0){
		    	endvel = flmxsc;
		    }
		    
		    if(CommUtil.compare(intrprf, flmnsc)<0){
		    	endvel = flmnsc;
		    }
		    
		    log.debug("浮动利率："+entity.getIntrvl()+"--优惠后利率："+intrprf+"最终利率："+endvel);
		    
		    //最后利率
			inrt.setCuusin(endvel);
			inrt.setLacain(BigDecimal.ZERO);
			
			//-------优惠后的利率也不能超过关联的基础利率的最大返回  mod by leipeng 20170213  end------
			
			
			//inrt.setRealin(BigDecimal.ZERO);
			inrt.setIsfavo(E_YES___.YES); //是否优惠
			//inrt.setPfirwy(E_IRFLPF.); //优惠浮动方式
			inrt.setFavovl(cplCxlilv.getFlirvl()); //优惠浮动值
			inrt.setFavort(cplCxlilv.getFlirrt()); //利率浮动百分比
			
			inrt.setOpintr(entity.getIntrvl()); //开户利率
			inrt.setBsintr(entity.getBaseir()); //基准利率
			inrt.setIrflby(entity.getFlirwy()); //浮动方式
			inrt.setInflpo(entity.getFlirvl()); //浮动点数
			inrt.setInflrt(entity.getFlirrt()); //浮动百分比
			//inrt.setCuusin(entity.getIntrvl()); //当前执行利率
			
			KubInrtDao.insert(inrt);
		}
	}
	
	/**
	 * @Title: GetPrefer 
	 * @Description: 获取利率优惠
	 * @author zhangan
	 * @date 2016年7月13日 下午7:05:36 
	 * @version V2.3.0
	 */
	public static IoCxlilv GetPrefer(IoDpOpenSub open){
		IoInrPreferPlan cplPrefer = SysUtil.getInstance(IoInrPreferPlan.class);
		
		//IoCuSevQryTableInfo cusev = SysUtil.getInstance(IoCuSevQryTableInfo.class);
		//String custno = open.getBase().getCustno();
		//IoCucifCust tblCifCust = cusev.cif_cust_selectOne_odb1(custno, false);
		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		E_ACCATP accatp = cagen.qryAccatpByCustac(open.getBase().getCustac());
		
		cplPrefer.setActtyp(accatp);
		cplPrefer.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
		cplPrefer.setCdprod(null); //卡产品编号
		cplPrefer.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		cplPrefer.setCrcycd(open.getBase().getCrcycd());
		cplPrefer.setCuslvl(CommUtil.nvl(open.getBase().getCuslvl(), null)); //客户级别
		//优惠关联的电子账户不是电子账户ID mod by leipeng
		cplPrefer.setCustac(open.getBase().getCardno());//电子账号
		cplPrefer.setCustct(CommUtil.nvl(open.getBase().getCustct(), null)); //客户贡献度
		cplPrefer.setCustno(CommUtil.nvl(open.getBase().getCustid(), null));
		cplPrefer.setCusttp(CommUtil.nvl(open.getBase().getCusttp(), null));
		cplPrefer.setDepttm(open.getBase().getDepttm());
		cplPrefer.setEmplcu(CommUtil.nvl(open.getBase().getEmplcu(), null)); //是否员工客户
		cplPrefer.setGender(CommUtil.nvl(open.getBase().getGender(), null));
		cplPrefer.setIntrcd(open.getIntr().getIntrcd());
		cplPrefer.setProdcd(open.getBase().getProdcd());
		cplPrefer.setReglst(E_YES___.YES);
		cplPrefer.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		cplPrefer.setTermfm(null); //贷款期限
		cplPrefer.setTranam(open.getBase().getOpmony());
		cplPrefer.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		/*
		 * JF Modify：外调pb。
		 */
		//IoCxlilv cplCxlilv = SysUtil.getInstance(IoSrvPbInterestRate.class).getInrPreferPlan(cplPrefer);
		IoCxlilv cplCxlilv = SysUtil.getRemoteInstance(IoSrvPbInterestRate.class).getInrPreferPlan(cplPrefer);
		
		return cplCxlilv;
	}
	
	/**
	 * 
	 * @Title: AcctProdInfo 
	 * @Description: (活期产品附加信息存入) 
	 * @param open
	 * @author xiongzhao
	 * @date 2016年8月17日 上午10:11:06 
	 * @version V2.3.0
	 */
	public static void AcctProdInfo(IoDpOpenSub open){
		KnaAcctProd tblKnaAcctProd = SysUtil.getInstance(KnaAcctProd.class);
		tblKnaAcctProd.setAcctno(open.getBase().getAcctno());// 负债账号
		tblKnaAcctProd.setSprdid(open.getBase().getSprdid());// 可售产品ID
		tblKnaAcctProd.setSprdvr(open.getBase().getSprdvr());// 当前版本号
		tblKnaAcctProd.setObgaon(open.getBase().getSprdna());// 可售产品名称
		KnaAcctProdDao.insert(tblKnaAcctProd);
	}
	
	/**
	 * 
	 * @Title: AcctProdInfo 
	 * @Description: (定期产品附加信息存入) 
	 * @param open
	 * @author xiongzhao
	 * @date 2016年8月17日 上午10:11:06 
	 * @version V2.3.0
	 */
	public static void FxacProdInfo(IoDpOpenSub open){
		KnaFxacProd tblKnaFxacProd = SysUtil.getInstance(KnaFxacProd.class);
		tblKnaFxacProd.setAcctno(open.getBase().getAcctno());// 负债账号
		tblKnaFxacProd.setSprdid(open.getBase().getSprdid());// 可售产品ID
		tblKnaFxacProd.setSprdvr(open.getBase().getSprdvr());// 当前版本号
		tblKnaFxacProd.setObgaon(open.getBase().getSprdna());// 可售产品名称
		KnaFxacProdDao.insert(tblKnaFxacProd);
	}
	
	/***
	 * @Title: GetPlanNm 
	 * @Description: 计算存入支取计划的开始日期和结束日期
	 * @param depttm
	 * @param gentpd
	 * @param deptdy
	 * @return
	 * @author zhangan
	 * @date 2016年7月13日 下午7:03:00 
	 * @version V2.3.0
	 */
	public static Options<IoDpGentTab> GetPlanNm(E_TERMCD depttm, String gentpd, long deptdy){
		
		Options<IoDpGentTab> gent = new DefaultOptions<>();
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //存入日期
		String value = depttm.getValue();
		String edindt = ""; //到期日
		if(CommUtil.equals(value.substring(0, 1),"9")){
			edindt = DateTools2.dateAdd (ConvertUtil.toInteger(deptdy), trandt);
		}else{
			edindt = DateTools2.calDateByTerm(trandt, depttm);			
		}
		int count = 0;
		String tmpdt = trandt;
		
		while(true){
			if(CommUtil.compare(tmpdt, edindt) >= 0){
				break;
			}
			IoDpGentTab tab = SysUtil.getInstance(IoDpGentTab.class);
			tab.setPlstad(tmpdt);
			//tmpdt = DateTools2.calDateByTerm(tmpdt, DateTools2.calETermCdByTerm(str));
			tmpdt = DateTools2.calDateByFreq(tmpdt, gentpd);
			tab.setPloved(tmpdt);
			count = count + 1;
			tab.setSeqnum(ConvertUtil.toLong(count));
			gent.add(tab);
		}
		gent.get(count-1).setPloved(edindt); //最后一个计划的结束日期等于到期日
		
		return gent;
	}
	/**
	 * @Title: DealFxacSort 
	 * @Description: 初始化序号表 
	 * @param acctno
	 * @author zhangan
	 * @date 2016年7月13日 下午7:03:31 
	 * @version V2.3.0
	 */
	public static void DealFxacSort(String acctno){
		KnaFxacSort sort = SysUtil.getInstance(KnaFxacSort.class);
		sort.setAcctno(acctno);
		sort.setDetlsq(Long.valueOf(0));
		
		KnaFxacSortDao.insert(sort);
	}
	
	
	/**
	 * @Title: strikeOpenSubAcct 
	 * @Description: 负债子账户开户冲正 
	 * @param acctno 负债子账号
	 * @param retrdt 原交易日期
	 * @param retrsq 原交易流水
	 * @author zhangan
	 * @date 2016年9月29日 上午9:52:27 
	 * @version V2.3.0
	 */
	public static void strikeOpenSubAcct(String custac, String acctno, String retrdt, String retrsq, E_FCFLAG fcflag){
		
		String tmstmp =DateTools2.getCurrentTimestamp();
		if(fcflag == E_FCFLAG.CURRENT){
			KnaAcct tblKnaAcct = ActoacDao.selKnaAcct(acctno, false);
			if(CommUtil.isNull(tblKnaAcct)){
				throw DpModuleError.DpstAcct.BNAS1421();
			}
			
			if(!CommUtil.equals(tblKnaAcct.getOpendt(), retrdt)){
				throw DpModuleError.DpstAcct.BNAS1422();
			}
			
			if(!CommUtil.equals(tblKnaAcct.getOnlnbl(), BigDecimal.ZERO)){
				throw DpModuleError.DpstAcct.BNAS1423();
			}
			
			tblKnaAcct.setAcctst(E_DPACST.CLOSE);
			KnaAcctDao.updateOne_odb1(tblKnaAcct);
			
			ActoacDao.updKnaAccs(E_DPACST.CLOSE, acctno,tmstmp);
		}else if(fcflag == E_FCFLAG.FIX){
			KnaFxac tblKnaFxac = ActoacDao.selKnaFxac(acctno, false);
			if(CommUtil.isNull(tblKnaFxac)){
				throw DpModuleError.DpstAcct.BNAS1421();
			}
			
			if(!CommUtil.equals(tblKnaFxac.getOpendt(), retrdt)){
				throw DpModuleError.DpstAcct.BNAS1422();
			}
			
			if(!CommUtil.equals(tblKnaFxac.getOnlnbl(), BigDecimal.ZERO)){
				throw DpModuleError.DpstAcct.BNAS1423();
			}
			
			tblKnaFxac.setAcctst(E_DPACST.CLOSE);
			KnaFxacDao.updateOne_odb1(tblKnaFxac);
			
			ActoacDao.updKnaAccs(E_DPACST.CLOSE, acctno,tmstmp);
		}
		
		
	}
}
