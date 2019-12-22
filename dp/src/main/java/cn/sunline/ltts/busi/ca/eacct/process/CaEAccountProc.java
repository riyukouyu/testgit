package cn.sunline.ltts.busi.ca.eacct.process;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.base.DecryptConstant;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccsDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSign;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSignDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSignDetl;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSignDetlDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpac;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpacDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnpAcctType;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnpSign;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnpSignDao;
import cn.sunline.ltts.busi.ca.type.CaCustInfo.AcctAddFlagInfo;
import cn.sunline.ltts.busi.iobus.servicetype.IoPbTableSvr;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEARelaIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEAccountIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEAccountOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaOpenAccInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubBrch;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_COACFG;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_IDCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_MPCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;

/**
 * <p>
 * 功能文件说明：电子账户的相关处理
 * </p>
 * 
 * @author renjinghua
 *
 */
@SuppressWarnings("deprecation")
public class CaEAccountProc {
	
	/**
	 * <p>
	 * 产生电子账号，获取电子账户信息，并做插入操作，返回电子账户部分信息
	 * </p>
	 * 
	 * @param cplAddEAccountIn
	 * @return
	 */
	public static IoCaAddEAccountOut addEAccountInfo(IoCaAddEAccountIn cplAddEAccountIn){
		
		//设置输出接口参数
		IoCaAddEAccountOut cplAddEAccountOut = SysUtil.getInstance(IoCaAddEAccountOut.class);
		
		String custna = cplAddEAccountIn.getCustna();
		String custno = cplAddEAccountIn.getCustno();
		//客户号
		if(CommUtil.isNull(custno)){
			throw CaError.Eacct.BNAS0567();
		}
		//客户名称
		if(CommUtil.isNull(custna)){
			throw CaError.Eacct.BNAS0566();
		}
		
		//身份核查结果
		if(E_IDCKRT.FAILD == cplAddEAccountIn.getIdckrt()){
			throw CaError.Eacct.BNAS0367();
		}
		
		//人脸识别结果
		if(E_MPCKRT.FAILD == cplAddEAccountIn.getMpckrt()){
			throw CaError.Eacct.BNAS1263();
		}
		E_CUSACT cacttp =null;
		//获取客户账号类型，为空则取默认的类型
		if(CommUtil.isNull(cplAddEAccountIn.getCacttp())){
			 cacttp = E_CUSACT.ACC;
		}else{
		     cacttp = cplAddEAccountIn.getCacttp();
		}
		//检查客户账户容器账户标志
		AcctAddFlagInfo cplAddFlagInfo = chkEAccountContainer(cacttp,cplAddEAccountIn.getCustno());
		
		if(E_YES___.NO == cplAddFlagInfo.getAdacfg() && CommUtil.isNotNull(cplAddFlagInfo.getCustac())){
			
			KnaCust tblKna_cust = KnaCustDao.selectOne_odb1(cplAddFlagInfo.getCustac(), true);
			
			String brchno = cplAddEAccountIn.getBrchno();
			tblKna_cust.setOpendt(CommTools.getBaseRunEnvs().getTrxn_date()); //开户日期
			tblKna_cust.setOpensq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //开户流水
			tblKna_cust.setUschnl(CommUtil.toEnum(BaseEnumType.E_USCHNL.class, CommTools.getBaseRunEnvs().getChannel_id()));//开户渠道
			
			//检查并设置账户归属机构
			if(CommUtil.isNotNull(brchno)){
				IoPbTableSvr tablePb = SysUtil.getInstance(IoPbTableSvr.class);
				IoPbKubBrch tblKub_brch = tablePb.kub_brch_selectOne_odb1(brchno, false);
				
				if(CommUtil.isNull(tblKub_brch.getBrchno())){
					throw CaError.Eacct.BNAS0565(brchno);
				}
				
				tblKna_cust.setBrchno(brchno);   //开户机构
			}else {
				tblKna_cust.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch()); //开户机构
			}
			
			tblKna_cust.setIdckrt(cplAddEAccountIn.getIdckrt()); //身份核查结果
			tblKna_cust.setMpckrt(cplAddEAccountIn.getMpckrt()); //人脸识别结果
			tblKna_cust.setAcctst(E_ACCTST.NORMAL);// 账户状态
			//更新电子账户状态
			/*if(E_IDCKRT.SUCCESS == cplAddEAccountIn.getIdckrt()
					&& E_MPCKRT.SUCCESS == cplAddEAccountIn.getMpckrt()
					&& E_YES___.YES == cplAddEAccountIn.getBdrtfg()){
				
				tblKna_cust.setAcctst(E_ACCTST.NORMAL); // addby xiongzhao 
				if(E_ACCATP.WALLET == cplAddEAccountIn.getAccttp()){
					
					tblKna_cust.setAcctst(E_ACCTST.INVALID);
				}else{
					
				}
			}else if(E_IDCKRT.CHECKING == cplAddEAccountIn.getIdckrt() 
					|| E_MPCKRT.CHECKING == cplAddEAccountIn.getMpckrt()
					|| E_YES___.NO == cplAddEAccountIn.getBdrtfg()){
				tblKna_cust.setAcctst(E_ACCTST.INVALID);
			}else{
				tblKna_cust.setAcctst(E_ACCTST.INVALID);
			}*/
			
			KnaCustDao.updateOne_odb1(tblKna_cust);
			
			cplAddEAccountOut.setCustac(tblKna_cust.getCustac());
			cplAddEAccountOut.setCustna(tblKna_cust.getCustna());
			cplAddEAccountOut.setCustno(tblKna_cust.getCustno());
			cplAddEAccountOut.setOpendt(tblKna_cust.getOpendt());
			cplAddEAccountOut.setOpensq(tblKna_cust.getOpensq());
			cplAddEAccountOut.setCacttp(tblKna_cust.getCacttp());
			cplAddEAccountOut.setBrchno(tblKna_cust.getBrchno());
			cplAddEAccountOut.setAdacfg(cplAddFlagInfo.getAdacfg());
			
		}else{
			
			//生成电子账号
			String custac = BusiTools.genCustac();
			
			//获取电子账户表信息  销户日期、销户流水置为null  绑定卡号置为null
			KnaCust tblKna_cust = SysUtil.getInstance(KnaCust.class);
			
			String brchno = cplAddEAccountIn.getBrchno();
			tblKna_cust.setCustac(custac); //电子账户
			tblKna_cust.setCustno(custno); //客户号
			tblKna_cust.setCustna(custna); //客户名称
			tblKna_cust.setTmcustna(DecryptConstant.maskName(custna));//脱敏字段
			tblKna_cust.setOpendt(CommTools.getBaseRunEnvs().getTrxn_date()); //开户日期
			tblKna_cust.setOpensq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //开户流水
			
			// tblKna_cust.setOpensv(CommTools.getBaseRunEnvs().getChannel_id()); 
			//tblKna_cust.setOpensv(cplAddEAccountIn.getChnlid().toString());
			// 即富开户渠道。
			tblKna_cust.setUschnl(CommUtil.toEnum(BaseEnumType.E_USCHNL.class, cplAddEAccountIn.getUschnl()));
			
			//检查并设置账户归属机构
			if(CommUtil.isNotNull(brchno)){
				/*
				 * JF Modify：外调。
				IoPbTableSvr tablePb = SysUtil.getInstance(IoPbTableSvr.class);
				 */
				IoPbTableSvr tablePb = SysUtil.getRemoteInstance(IoPbTableSvr.class);
				IoPbKubBrch tblKub_brch = tablePb.kub_brch_selectOne_odb1(brchno, false);
				if(CommUtil.isNull(tblKub_brch)){
					throw CaError.Eacct.BNAS0565(brchno);
				}
				tblKna_cust.setBrchno(brchno);   //开户机构
			}else {
				tblKna_cust.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch()); //开户机构
			}
			
			tblKna_cust.setCacttp(cacttp); //客户账号类型
			tblKna_cust.setAccttp(cplAddEAccountIn.getAccttp()); //电子账户分类
			tblKna_cust.setIdckrt(cplAddEAccountIn.getIdckrt()); //身份核查结果
			tblKna_cust.setMpckrt(cplAddEAccountIn.getMpckrt()); //人脸识别结果
			tblKna_cust.setAcctst(E_ACCTST.NORMAL);// 账户状态
			/*// 更新电子账户状态。
			if(E_IDCKRT.SUCCESS == cplAddEAccountIn.getIdckrt()
					&& E_MPCKRT.SUCCESS == cplAddEAccountIn.getMpckrt()
					&& E_YES___.YES == cplAddEAccountIn.getBdrtfg()){
				
				tblKna_cust.setAcctst(E_ACCTST.NORMAL); // addby xiongzhao 
				if(E_ACCATP.WALLET == cplAddEAccountIn.getAccttp()){
					
					tblKna_cust.setAcctst(E_ACCTST.INVALID);
				}else{
					
				}
			}else if(E_IDCKRT.CHECKING == cplAddEAccountIn.getIdckrt() 
					|| E_MPCKRT.CHECKING == cplAddEAccountIn.getMpckrt()
					|| E_YES___.NO == cplAddEAccountIn.getBdrtfg()){
				tblKna_cust.setAcctst(E_ACCTST.INVALID);
			}else{
				tblKna_cust.setAcctst(E_ACCTST.INVALID);
			}*/
			
			KnaCustDao.insert(tblKna_cust);
			
			cplAddEAccountOut.setCustac(tblKna_cust.getCustac());
			cplAddEAccountOut.setCustna(tblKna_cust.getCustna());
			cplAddEAccountOut.setCustno(tblKna_cust.getCustno());
			cplAddEAccountOut.setOpendt(tblKna_cust.getOpendt());
			cplAddEAccountOut.setOpensq(tblKna_cust.getOpensq());
			//重新赋值，当为空值时会被修改
			cplAddEAccountOut.setCacttp(cacttp);
			cplAddEAccountOut.setBrchno(tblKna_cust.getBrchno());
			cplAddEAccountOut.setAdacfg(cplAddFlagInfo.getAdacfg());
			
			// 如果是分布式部署，注册drs服务
//			if (CommTools.isDistributedCallFromRemote()) {
//				IDRSUtil.registryDCNByAccount(tblKna_cust.getCustac(),  CommTools.getBaseRunEnvs()
//						.getCdcnno());
//			}
		}
		
		return cplAddEAccountOut;
		
	}

	/**
	 * <p>
	 * 获取负债子账户、电子账户信息，进行关联操作，建立电子账户与负债账户关联关系
	 * </p>
	 * 
	 * @param cplAddEARelaIn
	 */
	public static void prcAddEARelaInfo(IoCaAddEARelaIn cplAddEARelaIn) {
		//检查负债账号
		if(CommUtil.isNull(cplAddEARelaIn.getAcctno())){
			throw CaError.Eacct.BNAS0637();
		}
		
		//检查电子账号
		if(CommUtil.isNull(cplAddEARelaIn.getCustac())){
			throw CaError.Eacct.BNAS1632();
		}
		
		//检查币种
		String crcycd = cplAddEARelaIn.getCrcycd();
		if(CommUtil.isNull(crcycd)){
			throw DpModuleError.DpstComm.BNAS1101();
		}
		
		//检查产品类型
		E_PRODTP prodtp = cplAddEARelaIn.getProdtp();
		if(CommUtil.isNull(prodtp)){
			throw CaError.Eacct.BNAS1051();
		}
		
		//检查定活标志
		E_FCFLAG fcflag = cplAddEARelaIn.getFcflag();
		if(E_PRODTP.DEPO == prodtp){
			if(CommUtil.isNull(fcflag)){
				throw CaError.Eacct.BNAS1039();
			}
		}
		
		//检查产品编号
		String prodcd = cplAddEARelaIn.getProdcd();
		if(CommUtil.isNull(prodcd))
			throw DpModuleError.DpstComm.BNAS1480();
		
		//检查输入钞汇标标志
		E_CSEXTG csextg = null;		
		//如果钞汇标志输入为空，则默认为现钞
		if(CommUtil.isNull(cplAddEARelaIn.getCsextg())){
			csextg = E_CSEXTG.CASH;
		}
		//如果币种为人民币，钞汇标志不为现钞，设值为现钞
		else if(CommUtil.isNotNull(cplAddEARelaIn.getCsextg())){
			if(CommUtil.equals(crcycd, BusiTools.getDefineCurrency()) && cplAddEARelaIn.getCsextg()!=E_CSEXTG.CASH){
				csextg = E_CSEXTG.CASH;
			}else{//币种为外币时，钞汇标志不为空，则取传进来的钞汇标志
				csextg = cplAddEARelaIn.getCsextg();
			}
		}
		if(BusiTools.getDefineCurrency().equals(crcycd) && E_PRODTP.DEPO == prodtp){
			csextg = E_CSEXTG.CASH;	//钞汇标志			
		}else{
			csextg = cplAddEARelaIn.getCsextg();
		}
		
		//生成电子账户子户号
		String subsac = cplAddEARelaIn.getSubsac();
		if(E_PRODTP.DEPO == prodtp && CommUtil.isNotNull(subsac))
			throw CaError.Eacct.BNAS1038();
		if(CommUtil.isNull(subsac))
			subsac = BusiTools.genSubEAccountno();
		
		//检查是否有已有关联关系
		KnaAccs tblKna_accs = KnaAccsDao.selectOne_odb2(cplAddEARelaIn.getAcctno(), false);
		
		//kna_accs accs = Kna_accsDao.selectFirst_odb6(cplAddEARelaIn.getCustac(), cplAddEARelaIn.getCrcycd(), csextg, cplAddEARelaIn.getProdtp(), cplAddEARelaIn.getProdcd(), E_DPACST.NORMAL, false);
		
		//如果没有建立关联关系，则生成电子账户子户号并建立
		if(CommUtil.isNull(tblKna_accs)){
			
			
			//获取相关信息
			KnaAccs tblKna_accs_one = SysUtil.getInstance(KnaAccs.class);
			tblKna_accs_one.setCustac(cplAddEARelaIn.getCustac());  //电子账号
			tblKna_accs_one.setSubsac(subsac);  //子户号
			tblKna_accs_one.setAcctno(cplAddEARelaIn.getAcctno());  //负债账号
			tblKna_accs_one.setFcflag(E_FCFLAG.CURRENT);  //定活标志
			tblKna_accs_one.setProdtp(prodtp);  //产品类型
			tblKna_accs_one.setCsextg(csextg); //钞汇标志
			tblKna_accs_one.setProdcd(prodcd);//产品号
			tblKna_accs_one.setCrcycd(crcycd);//币种
			tblKna_accs_one.setAcctst(E_DPACST.NORMAL); //状态
			
			tblKna_accs_one.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id()); //法人代码
			//tblKna_accs_one.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date()); //时间戳
			
			//插入操作
			KnaAccsDao.insert(tblKna_accs_one);
			
			}
		
	}
	
	/**
	 * <p>
	 * 检查客户账户容器标志，如果是单一账户，则该客户只能开一个电子账户，如果是容器账户则可开多个电子账户
	 * </p>
	 * 
	 * @param csactp 客户账号类型
	 * @param custno 客户号
	 */
	public static AcctAddFlagInfo chkEAccountContainer(E_CUSACT csactp, String custno) {
		
		AcctAddFlagInfo cplAddFlagInfo = SysUtil.getInstance(AcctAddFlagInfo.class);
		E_YES___ addAcctFlag = E_YES___.YES; //新开电子账户标志
		
		//通过客户账户类型，查找账户容器标志
		//knp_acct_type tblKnp_acct_type = Knp_acct_typeDao.selectOne_odb1(csactp, false);
		
		KnpAcctType tblKnp_acct_type = EacctMainDao.selKnpAcctType(CommTools.getBaseRunEnvs().getBusi_org_id(), csactp, false);
		if(CommUtil.isNull(tblKnp_acct_type)){
			tblKnp_acct_type = EacctMainDao.selKnpAcctType(BusiTools.getCenterCorpno(), csactp, false);
			//没有配置该客户账户类型参数
			if(CommUtil.isNull(tblKnp_acct_type)){
				throw CaError.Eacct.E0002(csactp.getValue());
			}
		}
		
		
		
		//该客户账户类型没有配置容器账户标志
		if(CommUtil.isNull(tblKnp_acct_type.getCoacfg())){
			throw CaError.Eacct.E0003(csactp.getValue());
		}
		
		//如果容器账户标志是单一账户，则检查是否开过电子账户，开过则不允许再开
		if(E_COACFG.ONE == tblKnp_acct_type.getCoacfg()){
			
			//kna_cust tblKna_cust = Kna_custDao.selectFirst_odb2(csactp,custno, false);
			KnaCust tblKnaCust = CaDao.selKnaCustByCustno(custno, csactp, false);
			
			//开过电子账户
			if(CommUtil.isNotNull(tblKnaCust)){
				
				//如果身份核查标志及人脸识别标志都为空，则不重新生成电子账号，暂时为浙江农信专用，适用先开亲情钱包再开电子账户情况
				if(CommUtil.isNull(tblKnaCust.getIdckrt()) && CommUtil.isNull(tblKnaCust.getMpckrt())){
					addAcctFlag = E_YES___.NO;
					cplAddFlagInfo.setCustac(tblKnaCust.getCustac());
				}else{
					throw CaError.Eacct.BNAS0110();
				}
			}
		}
		
		cplAddFlagInfo.setAdacfg(addAcctFlag);
		
		return cplAddFlagInfo;
		
	}
	
	/**
	 * 通过客户账号类型获取默认的产品编号
	 * 
	 * @param csactp  客户账号类型
	 * @return 产品编号
	 */
	public static String getProdcd(E_CUSACT csactp){
		//检查客户账号类型
		if(CommUtil.isNull(csactp)){
			throw CaError.Eacct.BNAS0659();
		}
		
	    //先查询当前法人产品号，如无，则取中心法人
		KnpAcctType tblKnp_acct_type = EacctMainDao.selKnpAcctType(CommTools.getBaseRunEnvs().getBusi_org_id(), csactp, false);
		if(CommUtil.isNull(tblKnp_acct_type)){
			tblKnp_acct_type = EacctMainDao.selKnpAcctType(BusiTools.getCenterCorpno(), csactp, true);
		}
		//tblKnp_acct_type = Knp_acct_typeDao.selectOne_odb1(csactp, false);
		//tblKnp_acct_type = EacctMainDao.selKnpAcctType(corpno, csactp, false);
		//客户账户类型定义表没有配置参数
		if(CommUtil.isNull(tblKnp_acct_type)){
			throw CaError.Eacct.BNAS0516(csactp.toString());
		}
		
		//获取的产品编号为空
		String baprcd = tblKnp_acct_type.getBaprcd();
		if(CommUtil.isNull(baprcd)){
			throw CaError.Eacct.BNAS0519(csactp.toString());
		}
		
		return baprcd;
	}
	
	/**
	 * 签约方法
	 * @param entity
	 * 
	 * 签约类型为空时，返回不进行签约
	 */
	public static void custSign(SignEntity entity){
		
		if(CommUtil.isNull(entity.getSigntp()))	
			return;
		if(CommUtil.isNull(entity.getCustac()))	
			throw DpModuleError.DpstProd.BNAS0926();
		
		E_SIGNTP signtp = entity.getSigntp();
		
		if(E_SIGNTP.ZNCXL == signtp){
			//智能储蓄签约
			
			//判断是否已经签约
			KnaSign tblSign = KnaSignDao.selectFirst_odb2(entity.getCustac(), signtp, E_SIGNST.QY, false);
			if(CommUtil.isNull(tblSign)){
				//登记签约
				
				//获取转存签约产品
				KnpSign sign = KnpSignDao.selectOne_odb1(signtp, false);
				if(CommUtil.isNull(sign))
					throw CaError.Eacct.BNAS1633();
				
				String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
				
				//Long signno = Long.valueOf(SequenceManager.nextval("kna_sign_seq"));
				Long signno = Long.valueOf(CoreUtil.nextValue("kna_sign_seq"));
				//签约表赋值
				tblSign = SysUtil.getInstance(KnaSign.class);
				tblSign.setSignno(signno);
				tblSign.setSigntp(entity.getSigntp());
				tblSign.setCustac(entity.getCustac());
				if(CommUtil.isNull(entity.getAutofg())){
					tblSign.setAutofg(E_YES___.YES);
				}else{
					tblSign.setAutofg(entity.getAutofg());
				}
				tblSign.setSigndt(trandt);
				tblSign.setSignst(E_SIGNST.QY);
				
				KnaSignDao.insert(tblSign);
				
				//签约明细表
				KnaSignDetl sign_detl = SysUtil.getInstance(KnaSignDetl.class);
				sign_detl.setSigntp(signtp);
				sign_detl.setSignno(signno);
				sign_detl.setCustno(entity.getCustno());
				sign_detl.setCustac(entity.getCustac());
				sign_detl.setAcctno(entity.getAcctno());
				sign_detl.setProdcd(sign.getProdcd());
				sign_detl.setTrprod(sign.getTrprod());
				sign_detl.setPeriod(sign.getPeriod());
				sign_detl.setFrequy(sign.getFrequy());
				sign_detl.setMiniam(sign.getMiniam());
				sign_detl.setKeepam(sign.getKeepam());
				sign_detl.setTrottp(sign.getTrottp());
				sign_detl.setTrmiam(sign.getTrmiam());
				sign_detl.setUpamnt(sign.getUpamnt());
				sign_detl.setSignam(entity.getSignam());
				sign_detl.setSigndt(trandt);
				sign_detl.setSignst(E_SIGNST.QY);
				sign_detl.setCurrdt(trandt);
				sign_detl.setNextdt(DateTools2.calDateByTerm(trandt, sign.getFrequy()+ sign.getPeriod()));
				
				KnaSignDetlDao.insert(sign_detl);
			}else{
				//修改签约明细
				KnaSignDetl sign_detl = KnaSignDetlDao.selectOne_odb1(tblSign.getSignno(), true);
				
				sign_detl.setFxacct(entity.getFxacct());
				
				KnaSignDetlDao.updateOne_odb1(sign_detl);
			}
		}else{
			throw CaError.Eacct.BNAS1154(signtp.toString());
		}
		
		
		
	}
	
	/**
	 * 
	 * @author renjinghua
	 * 		<p>
	 *	    <li>2016年6月30日-下午8:34:25<li>
	 *      <li>功能描述：登记开户登记簿</li>
	 *      </p>
	 * 
	 * @param cplOpenAccInfo 开户登记簿输入
	 */
	public static void addOpenAcctBook(IoCaOpenAccInfo cplOpenAccInfo){
		KnbOpac tblKnbOpac = SysUtil.getInstance(KnbOpac.class);
		tblKnbOpac.setCustac(cplOpenAccInfo.getCustac()); //电子账号
		tblKnbOpac.setCustna(cplOpenAccInfo.getCustna()); //账户名称
		tblKnbOpac.setTmcustna(DecryptConstant.maskName(cplOpenAccInfo.getCustna())); //账户名称
		tblKnbOpac.setAccttp(cplOpenAccInfo.getAccttp()); //账户分类
		tblKnbOpac.setTlphno(cplOpenAccInfo.getTlphno()); //手机号
		tblKnbOpac.setTmtlphno(DecryptConstant.maskMobile(cplOpenAccInfo.getTlphno())); //手机号
		tblKnbOpac.setBrchno(cplOpenAccInfo.getBrchno()); //所属机构
		tblKnbOpac.setCrcycd(cplOpenAccInfo.getCrcycd()); //币种
		tblKnbOpac.setOpentm(BusiTools.getBusiRunEnvs().getTrantm()); // 开户时间
		//钞汇标志
		if(CommUtil.isNotNull(cplOpenAccInfo.getCsextg())){			
			tblKnbOpac.setCsextg(cplOpenAccInfo.getCsextg());
		}else{
			tblKnbOpac.setCsextg(E_CSEXTG.CASH);
		}
		//开户日期
		if(CommUtil.isNotNull(cplOpenAccInfo.getOpendt())){
			tblKnbOpac.setOpendt(cplOpenAccInfo.getOpendt());
		}else{
			tblKnbOpac.setOpendt(CommTools.getBaseRunEnvs().getTrxn_date());
		}
		//开户流水
		if(CommUtil.isNotNull(cplOpenAccInfo.getOpensq())){
			tblKnbOpac.setOpensq(cplOpenAccInfo.getOpensq());
		}else{
			tblKnbOpac.setOpensq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		}
		//开户柜员
		String openus = CommTools.getBaseRunEnvs().getTrxn_teller();
		if(CommUtil.isNotNull(cplOpenAccInfo.getOpenus())){
			tblKnbOpac.setOpenus(cplOpenAccInfo.getOpenus());
		}else{
			tblKnbOpac.setOpenus(openus);
		}
		//开户渠道
		if(CommUtil.isNotNull(cplOpenAccInfo.getUschnl())){
			tblKnbOpac.setUschnl(cplOpenAccInfo.getUschnl());
		}else{
			tblKnbOpac.setUschnl(CommUtil.toEnum(BaseEnumType.E_USCHNL.class, CommTools.getBaseRunEnvs().getChannel_id()));
		}
		
		/**add by xj 20180531*/
		//tblKnbOpac.setServsq(CommTools.getBaseRunEnvs().getBusi_seq());
		tblKnbOpac.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		/**end*/
		//mod by xj 20180613
		KnbOpacDao.insert(tblKnbOpac);
		
	}
	
}
