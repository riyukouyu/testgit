package cn.sunline.ltts.busi.ca.serviceimpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.CaPublic;
import cn.sunline.ltts.busi.ca.namedsql.AccountLimitDao;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KubTsdtAcct;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KubTsdtAcctDao;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KubTsscAcct;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KubTsscAcctDao;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupAcrtBrch;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupAcrtBrchDao;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupAcrtCust;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupAcrtCustDao;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtLimt;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtLimtDao;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtSbac;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtServ;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupCurtServDao;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupPrcsLimt;
import cn.sunline.ltts.busi.ca.tables.AccountLimit.KupPrcsLimtDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAccLimitBrchQry.Input;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAccLimitBrchQry.Output;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.servicetype.qt.IoQtInsSrv;
import cn.sunline.ltts.busi.iobus.servicetype.qt.IoQtInsSrv.IoQtRevest;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountLimitInfo.IoAccLimitIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountLimitInfo.IoDelAccLimitIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountLimitInfo.IoKupAcrtBrch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.iobus.type.qt.IoQtLogLimitSel.IoLogLimitSel;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_ACLMFG;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTKD;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTST;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_PYTLTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_QUOTTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RECPAY;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RISKLV;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SELTYP;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.errors.QtError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BRCHLV;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BRCHTP;
 /**
  * 电子账户限额服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoCaSevAccountLimitImpl", longname="电子账户限额服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoCaSevAccountLimitImpl implements cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit{
	private static final BizLog log = BizLogUtil
			.getBizLog(IoCaSevAccountLimitImpl.class);
	/**
	  * 新增账户限额
	  *
	  */
	@Override
	public void accLimitBrchAdd(IoAccLimitBrchAdd.Input inputs) {
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch(); //机构号
		Options<IoAccLimitIn> options = inputs.getInfos();
		
		IoSrvPbBranch tblkub_brch = SysUtil.getInstance(IoSrvPbBranch.class);
        
		IoBrchInfo kub_brch = tblkub_brch.getBranch(brchno);

		if (E_BRCHLV.PROV != kub_brch.getBrchlv() || E_BRCHTP.CLEA != kub_brch.getBrchtp()) {
			throw QtError.Custa.BNASE105();
		}
		 
		for (IoAccLimitIn input: options) {
			//判断输入机构号是否为当前机构
			if (!CommUtil.equals(brchno, input.getBrchno())) {
				throw QtError.Custa.BNASE040();
			}
			
			IoCaSevAccountLimtPublic.accLimitInputCheck(input); //输入数据检查
			
			log.debug("-----相同交易检查开始-----");
			IoCaSevAccountLimtPublic.checkSameRecord(input); //相同或互斥交易检查
			log.debug("-----相同交易检查结束-----");
			
			IoCaSevAccountLimtPublic.accLimitContrlCheck2(input); //业务控制检查
			
			KupAcrtBrch entity = SysUtil.getInstance(KupAcrtBrch.class);
			String acrtsq = IoCaSevAccountLimtPublic.genAcrtsq("acrtsq");
			String acrtno = IoCaSevAccountLimtPublic.genAcrtno("acrtno");
			entity.setAcrtsq(acrtsq); //账户限额序号
			entity.setAcrtno(acrtno); //账户限额编号
			
			entity.setBrchno(brchno); //机构号
			entity.setAcctrt(input.getAcctrt()); //客户类型
			entity.setLimttp(input.getLimttp()); //额度类型
			entity.setLimtkd(input.getLimtkd()); //额度种类
			entity.setServtp(input.getServtp()); //渠道
			entity.setAccttp(input.getAccttp()); //账户分类
			entity.setSbactp(input.getSbactp()); //子账户类型
			entity.setPytltp(input.getPytltp()); //支付工具
			entity.setLmtmax(input.getLmtmax()); //可设区间(最大值)
			entity.setLmtmin(input.getLmtmin()); //可设区间(最小值)
			entity.setLmtval(input.getLmtval()); //限额
			entity.setLimtst(E_LIMTST.NL); //限额状态
			entity.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller()); //交易柜员
			entity.setAuthus(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); //授权柜员
			
			KupAcrtBrchDao.insert(entity);
			
		}
	}

	/**
	  * 查询账户限额
	  *
	  */
	public void accLimitBrchQry(Input input, Output output) {
		
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();// 交易机构号
		String brchno = input.getBrchno();// 传入机构号
		E_ACCTROUTTYPE custtp = input.getAcctrt();// 客户类型
		String limttp = input.getLimttp();// 额度类型
		E_LIMTKD limtkd = input.getLimtkd();// 额度种类
		
		if (CommUtil.isNull(brchno)) {
			throw QtError.Custa.BNASE099();
		}
		
//		if (CommUtil.isNull(custtp)) custtp = CommUtil.toEnum(E_CUSTTP.class, "%");
		//if (CommUtil.isNull(limttp)) limttp = CommUtil.toEnum(E_LIMTTP.class, "%");
//		if (CommUtil.isNull(limtkd)) limtkd = CommUtil.toEnum(E_LIMTKD.class, "%");
		
		int totlCount = 0; // 记录总数
		Long pageno = CommTools.getBaseRunEnvs().getPage_start();//input.getPageno();
		Long pgsize = CommTools.getBaseRunEnvs().getPage_size();//input.getPgsize();
		Page<IoKupAcrtBrch> page = null;
		
		E_BRCHLV brchlv = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(brchno).getBrchlv();
		
		if (E_BRCHLV.PROV == brchlv) {
			page = AccountLimitDao.selKupAcrtBrch4(brchno, custtp, limttp, limtkd, (pageno-1) * pgsize, pgsize, totlCount, false);
		} else if (E_BRCHLV.COUNT == brchlv) {
		//	String upbrch = SysUtil.getInstance(IoSrvPbBranch.class).getUpprBranch(brchno, E_BRMPTP.M, BusiTools.getDefineCurrency()).getBrchno();
			E_BRCHLV tranlv = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(tranbr).getBrchlv();
			if (E_BRCHLV.COUNT == tranlv && !CommUtil.equals(tranbr, brchno)) {
				throw QtError.Custa.BNASE029();
			}
			
		//	page = AccountLimitDao.selKupAcrtBrch5(upbrch,brchno, custtp, limttp, limtkd, (pageno-1) * pgsize, pgsize, totlCount, false);
			page = AccountLimitDao.selKupAcrtBrch4(brchno, custtp, limttp, limtkd, (pageno-1) * pgsize, pgsize, totlCount, false);
		} else {
			throw QtError.Custa.BNASE127();
		}
		
		List<IoKupAcrtBrch> acrtBrchInfo = page.getRecords();
		
		output.getInfos().addAll(acrtBrchInfo);
		output.setCounts(ConvertUtil.toInteger(page.getRecordCount()));
		
		// 设置报文头总记录条数
		CommTools.getBaseRunEnvs().setTotal_count(page.getRecordCount());
		
	}

	/**
	  * 修改账户限额
	  *
	  */
	@Override
	public void accLimitBrchUpd(IoAccLimitBrchUpd.Input inputs) {
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch(); //交易机构号
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id(); //法人代码 
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller(); //交易柜员
		String authus = BusiTools.getBusiRunEnvs().getAuthvo().getAuthus(); //授权柜员
		
		String timetm = DateTools2.getCurrentTimestamp();
		
		Options<IoAccLimitIn> options = inputs.getInfos();
		if (options.size() <= 0) {
			throw QtError.Custa.BNASE123();
		}

		//获取当前机构信息
		IoBrchInfo cplKubBrch = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(tranbr);
		
		/*if(E_BRCHTP.CLEA != cplKubBrch.getBrchtp()){
			throw QtError.Custa.BNASE106();
		}*/
		
		if (E_BRCHLV.PROV == cplKubBrch.getBrchlv()) { //省级
			for (IoAccLimitIn input: options) {
				
				IoCaSevAccountLimtPublic.accLimitInputCheck(input); //输入数据检查,输入字段非空检查
				
				if (!CommUtil.equals(cplKubBrch.getBrchno().substring(0,3), input.getBrchno().substring(0,3))){
					throw QtError.Custa.BNASE042();
				}
				
				if (CommUtil.isNull(input.getAcrtsq())) {
					throw QtError.Custa.BNASE013();
				}
				
				KupAcrtBrch selAcrtBrch = AccountLimitDao.selAcrtBrchBysq(input.getAcrtsq(), input.getBrchno(), false);
				
				if (CommUtil.isNull(selAcrtBrch)) {
					throw QtError.Custa.BNASE015();
				}
				if (!CommUtil.equals(selAcrtBrch.getBrchno(), input.getBrchno())) {
					throw QtError.Custa.BNASE100();
				}
				if (selAcrtBrch.getAcctrt() != input.getAcctrt()) {
					throw QtError.Custa.BNASE069();
				}
				if (!CommUtil.equals(selAcrtBrch.getLimttp(), input.getLimttp())) {
					throw QtError.Custa.BNASE037();
				}
				if (selAcrtBrch.getLimtkd() != input.getLimtkd()) {
					throw QtError.Custa.BNASE112();
				}
				if (CommUtil.compare(input.getLmtval(), BigDecimal.ZERO) < 0) {
					throw QtError.Custa.BNASE024();
				}
				
				//至少修改一个字段
				if(CommUtil.equals(input.getBrchno(), selAcrtBrch.getBrchno()) &&
				   CommUtil.equals(input.getAccttp(), selAcrtBrch.getAccttp()) &&
				   CommUtil.equals(input.getLmtmax(), selAcrtBrch.getLmtmax()) &&
				   CommUtil.equals(input.getLmtmin(), selAcrtBrch.getLmtmin()) &&
				   CommUtil.equals(input.getLmtval(), selAcrtBrch.getLmtval()) &&
				   CommUtil.equals(input.getServtp(), selAcrtBrch.getServtp()) &&				   
				   CommUtil.equals(input.getSbactp(), selAcrtBrch.getSbactp()) &&
				   input.getPytltp() == selAcrtBrch.getPytltp()){
					throw QtError.Custa.BNASE008();
				}
				
				log.debug("-----相同交易检查开始-----");
				IoCaSevAccountLimtPublic.checkSameRecord2(input); //相同或互斥交易检查
				log.debug("-----相同交易检查结束-----");
				
				//IoCaSevAccountLimtPublic.accLimitContrlCheck(input); //业务控制检查
				
				List<KupAcrtBrch> listAcrtBrch = AccountLimitDao.selAcrtByAcrtno(selAcrtBrch.getAcrtno(), true);
				/*如省联社对渠道、收款行范围、客户价值等级、风险承受等级进行了修改，而县级行社对同序号的客户限额进行过维护
				县级行社的客户限额表中同序号的同字段进行同步修改*/
				if (!CommUtil.equals(input.getAccttp(), selAcrtBrch.getAccttp())
				  ||!CommUtil.equals(input.getSbactp(), selAcrtBrch.getSbactp())
				  ||!CommUtil.equals(input.getServtp(), selAcrtBrch.getServtp())
				  ||input.getPytltp() != selAcrtBrch.getPytltp()) {
					if (listAcrtBrch.size() > 1) {
						for (KupAcrtBrch obj: listAcrtBrch) {
							if (CommUtil.equals(obj.getAcrtsq(), selAcrtBrch.getAcrtsq())) //跳过省联社配置
								continue;

							BigDecimal lmtval = obj.getLmtval();
							if(!CommUtil.equals(input.getLmtmax(), selAcrtBrch.getLmtmax())
							         ||!CommUtil.equals(input.getLmtmin(), selAcrtBrch.getLmtmin())){						
								if (CommUtil.compare(lmtval, input.getLmtmin()) < 0) {
									lmtval = input.getLmtmin();
								} else if (CommUtil.compare(lmtval, input.getLmtmax()) > 0) {
									lmtval = input.getLmtmax();
								}
							}
//							AccountLimitDao.updAcrtByAcrtsq(obj.getAcrtsq(), obj.getCorpno(), input.getServtp(), input.getAccttp(), input.getSbactp(),
//									input.getPytltp(), input.getLmtmax(), input.getLmtmin(), lmtval, tranus, authus,mtdate,timetm);
							// 要保留联动修改的数据以便于日志查询，这里改为插入数据
							//更新原记录状态
							AccountLimitDao.updLimtst(obj.getAcrtsq(), E_LIMTST.UP, obj.getTranus(), obj.getAuthus(),timetm);
							//写入新记录
							KupAcrtBrch entity = SysUtil.getInstance(KupAcrtBrch.class);
							
							String acrtsq = IoCaSevAccountLimtPublic.genAcrtsq("acrtsq");
							entity.setAcrtsq(acrtsq); //账户限额序号
							entity.setAcrtno(obj.getAcrtno());
							entity.setCorpno(obj.getCorpno());
							entity.setBrchno(obj.getBrchno());
							
							entity.setAcctrt(obj.getAcctrt()); //客户类型
							entity.setLimttp(obj.getLimttp()); //额度类型
							entity.setLimtkd(obj.getLimtkd()); //额度种类
							entity.setServtp(input.getServtp()); //渠道
							entity.setAccttp(input.getAccttp()); //账户分类
							entity.setSbactp(input.getSbactp()); //子账户类型
							entity.setPytltp(input.getPytltp()); //支付工具
							entity.setLmtmax(input.getLmtmax()); //可设区间(最大值)
							entity.setLmtmin(input.getLmtmin()); //可设区间(最小值)
							entity.setLmtval(lmtval); //限额
							entity.setLimtst(E_LIMTST.NL); //限额状态
							entity.setTranus(tranus); //交易柜员
							entity.setAuthus(authus); //授权柜员

							if (CommUtil.isNotNull(obj.getBrchno()) 
									&& obj.getBrchno().length() > 0) {
//								CommTools.getBaseRunEnvs().setBusi_org_id(obj.getBrchno().substring(0, 3));
							}
							KupAcrtBrchDao.insert(entity);
							//法人号需要还原
//							CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
						
						}
					}
				} else if (!CommUtil.equals(input.getLmtmax(), selAcrtBrch.getLmtmax())
				         ||!CommUtil.equals(input.getLmtmin(), selAcrtBrch.getLmtmin())) {
					if (listAcrtBrch.size() > 1) {
						for (KupAcrtBrch obj: listAcrtBrch) {
							if (CommUtil.equals(obj.getAcrtsq(), selAcrtBrch.getAcrtsq())) //跳过省联社配置
								continue;
							
							BigDecimal lmtval = obj.getLmtval();
							if (CommUtil.compare(lmtval, input.getLmtmin()) < 0) {
								lmtval = input.getLmtmin();
							} else if (CommUtil.compare(lmtval, input.getLmtmax()) > 0) {
								lmtval = input.getLmtmax();
							}
							
//							AccountLimitDao.updAcrtByAcrtsq2(obj.getAcrtsq(), obj.getCorpno(), input.getLmtmax(), input.getLmtmin(), 
//									lmtval, tranus, authus,mtdate,timetm);
							// 要保留联动修改的数据以便于日志查询，这里改为插入数据
							//更新原记录状态
							AccountLimitDao.updLimtst(obj.getAcrtsq(), E_LIMTST.UP, obj.getTranus(), obj.getAuthus(),timetm);
							//写入新记录
							KupAcrtBrch entity = SysUtil.getInstance(KupAcrtBrch.class);
							
							String acrtsq = IoCaSevAccountLimtPublic.genAcrtsq("acrtsq");
							entity.setAcrtsq(acrtsq); //账户限额序号
							entity.setAcrtno(obj.getAcrtno());
							entity.setCorpno(obj.getCorpno());
							entity.setBrchno(obj.getBrchno());
							
							entity.setAcctrt(obj.getAcctrt()); //客户类型
							entity.setLimttp(obj.getLimttp()); //额度类型
							entity.setLimtkd(obj.getLimtkd()); //额度种类
							entity.setServtp(obj.getServtp()); //渠道
							entity.setAccttp(obj.getAccttp()); //账户分类
							entity.setSbactp(obj.getSbactp()); //子账户类型
							entity.setPytltp(obj.getPytltp()); //支付工具
							entity.setLmtmax(input.getLmtmax()); //可设区间(最大值)
							entity.setLmtmin(input.getLmtmin()); //可设区间(最小值)
							entity.setLmtval(lmtval); //限额
							entity.setLimtst(E_LIMTST.NL); //限额状态
							entity.setTranus(tranus); //交易柜员
							entity.setAuthus(authus); //授权柜员

							if (CommUtil.isNotNull(obj.getBrchno()) 
									&& obj.getBrchno().length() > 0) {
//								CommTools.getBaseRunEnvs().setBusi_org_id(obj.getBrchno().substring(0, 3));
							}
							KupAcrtBrchDao.insert(entity);
							//法人号需要还原
//							CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
						}
					}
				}
				
				//更新原记录状态
				AccountLimitDao.updLimtst(input.getAcrtsq(), E_LIMTST.UP, selAcrtBrch.getTranus(), selAcrtBrch.getAuthus(),timetm);
				
				//写入新记录
				KupAcrtBrch entity = SysUtil.getInstance(KupAcrtBrch.class);
				
				String acrtsq = IoCaSevAccountLimtPublic.genAcrtsq("acrtsq");
				entity.setAcrtsq(acrtsq); //账户限额序号
				entity.setAcrtno(selAcrtBrch.getAcrtno());
				entity.setCorpno(corpno);
				entity.setBrchno(tranbr);
				
				entity.setAcctrt(input.getAcctrt()); //客户类型
				entity.setLimttp(input.getLimttp()); //额度类型
				entity.setLimtkd(input.getLimtkd()); //额度种类
				entity.setServtp(input.getServtp()); //渠道
				entity.setAccttp(input.getAccttp()); //账户分类
				entity.setSbactp(input.getSbactp()); //子账户类型
				entity.setPytltp(input.getPytltp()); //支付工具
				entity.setLmtmax(input.getLmtmax()); //可设区间(最大值)
				entity.setLmtmin(input.getLmtmin()); //可设区间(最小值)
				entity.setLmtval(input.getLmtval()); //限额
				entity.setLimtst(E_LIMTST.NL); //限额状态
				entity.setTranus(tranus); //交易柜员
				entity.setAuthus(authus); //授权柜员

				KupAcrtBrchDao.insert(entity);
				
			}
			for (IoAccLimitIn input: options) {
				IoCaSevAccountLimtPublic.accLimitContrlCheck2(input); //业务控制检查
			}
		} else if (E_BRCHLV.COUNT == cplKubBrch.getBrchlv()) { //县级
			for (IoAccLimitIn input: options) {
				
				if (CommUtil.isNull(input.getAcrtsq())) {
					throw QtError.Custa.BNASE013();
				}
				if (CommUtil.isNull(input.getBrchno())) {
					throw QtError.Custa.BNASE099();
				}
				//
				E_BRCHLV brchlv = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(input.getBrchno()).getBrchlv();
			/*	if (brchlv != E_BRCHLV.PROV){
					if(!CommUtil.equals(input.getBrchno(), tranbr)) {
						throw QtError.Custa.BNASE009();
					}
				} else {
					//这里主要是交易报文体里的info.brchno传错，若传值是999000就不会报错，这里需要更改，改为报错
					throw QtError.Custa.BNASE030();
				}*/
				//
				KupAcrtBrch selAcrtBrch = AccountLimitDao.selAcrtBrchBysq2(input.getAcrtsq() ,false);
				
				if (CommUtil.isNull(selAcrtBrch)) {
					throw QtError.Custa.BNASE015();					
				}
				
				//检查入参
				IoCaSevAccountLimtPublic.checkXianJiInfo(input, selAcrtBrch);
				
				//判断限额值是否有更新
				if(CommUtil.equals(input.getLmtval(), selAcrtBrch.getLmtval())){
					throw QtError.Custa.BNASE011();
				}
				
				if (CommUtil.equals(selAcrtBrch.getBrchno(), tranbr)) { //修改本行社记录时，更新原记录状态
					AccountLimitDao.updLimtst(input.getAcrtsq(), E_LIMTST.UP, selAcrtBrch.getTranus(), selAcrtBrch.getAuthus(),timetm);
				}
				
				//写入新记录
				KupAcrtBrch entity = SysUtil.getInstance(KupAcrtBrch.class);
				String acrtsq = IoCaSevAccountLimtPublic.genAcrtsq("acrtsq");
				entity.setAcrtsq(acrtsq); //账户限额序号
				entity.setAcrtno(selAcrtBrch.getAcrtno()); //账户限额编号
				entity.setCorpno(corpno);
				entity.setBrchno(tranbr);
				
				entity.setAcctrt(input.getAcctrt()); //客户类型
				entity.setLimttp(input.getLimttp()); //额度类型
				entity.setLimtkd(input.getLimtkd()); //额度种类
				entity.setServtp(input.getServtp()); //渠道
				entity.setAccttp(input.getAccttp()); //账户分类
				entity.setSbactp(input.getSbactp()); //子账户类型
				entity.setPytltp(input.getPytltp()); //支付工具
				entity.setLmtmax(input.getLmtmax()); //可设区间(最大值)
				entity.setLmtmin(input.getLmtmin()); //可设区间(最小值)
				entity.setLmtval(input.getLmtval()); //限额
				entity.setLimtst(E_LIMTST.NL); //限额状态
				entity.setTranus(tranus); //交易柜员
				entity.setAuthus(authus); //授权柜员
				
				KupAcrtBrchDao.insert(entity);
			}
			// 获取省联社机构号
			IoBrchInfo supKubBrch =SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(tranbr);
			
			// 限额维护值检查
			for (IoAccLimitIn input: options) {
				IoCaSevAccountLimtPublic.accLimitContrlCheck3(supKubBrch.getBrchno(),tranbr,input); //业务控制检查
			}
		}
	}

	/**
	  * 删除账户限额
	  *
	  */
	@Override
	public void accLimitBrchDel(IoAccLimitBrchDel.Input inputs) {
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch(); //交易机构
		//String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller(); //交易柜员
		String authus = BusiTools.getBusiRunEnvs().getAuthvo().getAuthus(); //授权柜员
		
		String timetm =DateTools2.getCurrentTimestamp();
		
		Options<IoDelAccLimitIn> options = inputs.getInfos();
		if (options.size() <= 0) {
			throw QtError.Custa.BNASE123();
		}
		
		//查询机构信息
		IoBrchInfo tblKubBrch = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(tranbr);	
		
		if (E_BRCHLV.PROV != tblKubBrch.getBrchlv() || E_BRCHTP.CLEA != tblKubBrch.getBrchtp()) {
			throw QtError.Custa.BNASE105();
		}
		
		for (IoDelAccLimitIn input: options) {
			
			if (CommUtil.isNull(input.getAcrtsq())) {
				throw QtError.Custa.BNASE012();
			}
			
			KupAcrtBrch tbl = AccountLimitDao.selAcrtBrchBysq2(input.getAcrtsq(), true);
			if (tbl.getLimtst() == E_LIMTST.DL) {
				throw QtError.Custa.BNASE101();
			}
			
			if(!CommUtil.equals(tbl.getBrchno().substring(0,3), tblKubBrch.getBrchno().substring(0,3))){
				throw QtError.Custa.BNASE110();
			}
			
			List<String> acrtsqList = AccountLimitDao.selAcrtBrchByno(tbl.getAcrtno(), true);
			//更新记录状态
			for (String acrtsq: acrtsqList) {
				AccountLimitDao.updLimtst(acrtsq, E_LIMTST.DL, tranus, authus,timetm);
			}
			
		}
	}
	
	/**
	 * 查询账户限额日志
	 */
	/**
	 * 电子账户限额管理日志查询
	 */
	@Override
	public Options<IoLogLimitSel> acloglimitsel(String brchno, E_SELTYP seltyp, String staday, String endday,
				Long pageno, Long pgsize) {
	
		if (seltyp != E_SELTYP.AC) {
			throw QtError.Custa.BNASE126();
		}
		if(CommUtil.isNull(brchno)){
			throw QtError.Custa.BNASE099();
		}
		if (CommUtil.isNull(staday)) {
			throw QtError.Custa.BNASE077();
		}		  
	    if (CommUtil.isNull(endday)) {
			throw QtError.Custa.BNASE081();
		}
		if(CommUtil.compare(staday, endday) > 0){
		    throw QtError.Custa.BNASE078();
		}
		String currdt =  DateTools2.getSystemDate();
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();// 交易机构号
		E_BRCHLV tranlv = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(tranbr).getBrchlv();
		if(E_BRCHLV.COUNT == tranlv){
			E_BRCHLV inBrchlv = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(brchno).getBrchlv();
			if (E_BRCHLV.COUNT == inBrchlv && !CommUtil.equals(tranbr, brchno)) {
				throw QtError.Custa.BNASE155();
			}
		}
		
		long count = 0;
		Page<KupAcrtBrch> page = AccountLimitDao.selAcLogInfo(brchno,currdt,staday, endday, (pageno-1)*pgsize, pgsize, count, false);
		List<KupAcrtBrch> acrt = page.getRecords();
//		if (acrt.size() <= 0) {
////			throw QtError.Custa.E0001("没有符合条件记录");
//			return null;
//		}
		@SuppressWarnings("unchecked")
		Options<IoLogLimitSel> output = SysUtil.getInstance(Options.class);
		
		for (KupAcrtBrch entity : acrt) {
			
			IoLogLimitSel acbrch = SysUtil.getInstance(IoLogLimitSel.class);
			acbrch.setLimtst(entity.getBrchno().substring(0,3).equals("999")?entity.getLimtst().getValue():"3");
			acbrch.setAcctrt(entity.getAcctrt());
			acbrch.setLimttp(entity.getLimttp());
			acbrch.setLimtkd(entity.getLimtkd());
			acbrch.setServtp(entity.getServtp());
			acbrch.setAccttp(entity.getAccttp());
			acbrch.setSbactp(entity.getSbactp());
			acbrch.setPytltp(entity.getPytltp());			
			acbrch.setLmtmax(entity.getLmtmax());
			acbrch.setLmtmin(entity.getLmtmin());
			acbrch.setLmtval(entity.getLmtval());
			acbrch.setOpeday(entity.getTmstmp().substring(0, 8));
			acbrch.setTranus(entity.getTranus());
			acbrch.setAcrtno(new Long(entity.getAcrtno()));
			acbrch.setAuthus(entity.getAuthus());
			output.add(acbrch);
		}
		CommTools.getBaseRunEnvs().setTotal_count(page.getRecordCount());
		
		return output;
	}

	/**
	 * 电子账户额度扣减
	 */
	@Override
	public void SubAcctQuota(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota.Output output) {
//		String brchno = input.getBrchno(); //客户所属机构号
//		E_ACCATP custtp = input.getAccttp(); //客户类型
		log.debug("---------------------电子账户额度扣减------------------------");
		
		
		E_ACLMFG aclmfg;    //累计限额标识
		E_PYTLTP pytltp;    //支付工具
		E_RISKLV risklv;    //风险承受等级

        if(CommUtil.isNull(input.getAclmfg())){
            aclmfg = E_ACLMFG._2;      
        }else{
            aclmfg = input.getAclmfg();  
        }

        if(CommUtil.isNull(input.getPytltp())|| input.getPytltp() == E_PYTLTP.ALL){
            pytltp = E_PYTLTP._99; 
        }else{
            pytltp = input.getPytltp();  
        }
        
        if(CommUtil.isNull(input.getRisklv())){
            risklv = E_RISKLV._01; 
        }else{
            risklv = input.getRisklv();  
        } 
		
		String custid = input.getCustid(); //客户id
		E_LIMTTP limttp = input.getLimttp(); //额度类型
		String servtp = input.getServtp(); //渠道
		String servdt = input.getServdt(); //渠道交易日期
		String servsq = input.getServsq(); //渠道流水
		BigDecimal tranam = input.getTranam(); //交易金额
//		E_ACCATP accttp = input.getAccttp(); //账户分类
		E_SBACTP sbactp = input.getSbactp(); //子账户类型
		String custac = input.getCustac(); //电子账号
		E_RECPAY dcflag = input.getDcflag();//收付标志
		
		//获取组合账户限额类型
		String prcscd = CommTools.getBaseRunEnvs().getTrxn_code();
		KupPrcsLimt tblPrcs = SysUtil.getInstance(KupPrcsLimt.class);
		if(CommUtil.isNull(dcflag)){ //
			tblPrcs = KupPrcsLimtDao.selectFirst_odb1(prcscd, false);
		}else{
			tblPrcs = AccountLimitDao.selPrcsLimtByRemak(prcscd, dcflag.getValue().toString(), false);
		}
		
//		if (CommUtil.isNull(tblPrcs)) {
//			BizLog log = BizLogUtil.getBizLog(IoCaSevAccountLimitImpl.class);
//			log.debug("---------------------"+prcscd+"该交易未在额度类型表中配置------------------------");
//			return;
//		}
		
		if (CommUtil.isNull(tblPrcs)) {
			log.debug("---------------------"+prcscd+"该交易未在额度类型表中配置------------------------");
			return;
		}
		
        if(CommUtil.isNull(input.getLimttp())){
            limttp = CommUtil.toEnum(E_LIMTTP.class, tblPrcs.getLimttp()); 
        }
		
		if(input.getCustie()==E_YES___.YES){
			return;
		}
		
	/*	if (CommUtil.isNull(input.getAuthtp())) {
			throw QtError.Custa.BNASE046();
		}*/
			
//		if (CommUtil.isNull(brchno)) {
//			throw QtError.Custa.E0001("客户所属机构号不能为空");
//		}
//		if (CommUtil.isNull(custtp)) {
//			throw QtError.Custa.E0001("客户类型不能为空");
//		}
//		if (CommUtil.isNull(limttp)) {
//			throw QtError.Custa.BNASE117();
//		}
		if (CommUtil.isNull(servtp)) {
			throw QtError.Custa.BNASE051();
		}
		if (CommUtil.isNull(servsq)) {
			throw QtError.Custa.BNASE048();
		}
		if (CommUtil.isNull(servdt)) {
			throw QtError.Custa.BNASE049();
		}
//		if (CommUtil.isNull(accttp)) {
//			throw QtError.Custa.E0001("账户分类不能为空");
//		}
		if (CommUtil.isNull(sbactp)) {
		throw QtError.Custa.BNASE005();
		}
		
		if (CommUtil.isNull(input.getOldate()) && CommUtil.compare(input.getTranam(), BigDecimal.ZERO) < 0) {
			throw QtError.Custa.BNASE156();
		}
		if (CommUtil.isNull(custac)) {
			throw QtError.Custa.BNASE157();
		}
//		if (CommUtil.isNull(risklv)) {
//			throw QtError.Custa.BNASE104();
//		}
//		if (CommUtil.isNull(input.getAclmfg())) {
//			throw QtError.Custa.BNASE028();
//		}
		
		KnaCust knaCust = AccountLimitDao.selKnaCust(input.getCustac(), true);
		
		// 根据电子账号查询用户ID
		if(CommUtil.isNull(custid)){
			custid = CaDao.selCustidByCustac(custac, false);
			if (CommUtil.isNull(custid)) {
				throw QtError.Custa.BNASE158();
			}
		}
		
		String brchno = knaCust.getBrchno(); //客户所属机构号
//		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();//法人代码
//		String corpnotemp = knaCust.getBrchno().substring(0, 3);//账户所属法人
//		CommTools.getBaseRunEnvs().setBusi_org_id(corpnotemp);//设置账户所属法人
		
		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		E_ACCATP accttp = cagen.qryAccatpByCustac(input.getCustac()); //转出方电子账户类型
		
		if(accttp == E_ACCATP.WALLET && CommUtil.isNull(sbactp)){			
			sbactp =E_SBACTP._12;
		}else if (accttp != E_ACCATP.WALLET && CommUtil.isNull(sbactp)){		
			sbactp =E_SBACTP._11;
		}
		
		String currdt = CommTools.getBaseRunEnvs().getTrxn_date();
		
		BigDecimal sglmam = BigDecimal.ZERO; //单笔交易限额
		BigDecimal dylmam = BigDecimal.ZERO; //日累计限额

		BigDecimal mtlmam = BigDecimal.ZERO; //月累计限额
		BigDecimal yrlmam = BigDecimal.ZERO; //年累计限额
		BigDecimal dylmtm = BigDecimal.ZERO; //日累计次数
		BigDecimal mtlmtm = BigDecimal.ZERO; //月累计次数
		BigDecimal yrlmtm = BigDecimal.ZERO; //年累计次数
		
		
		
		/*if (CommUtil.isNull(pytltp) || pytltp == E_PYTLTP.ALL) {
			pytltp = CommUtil.toEnum(E_PYTLTP.class, "99");
		}*/
		
		//获取组合渠道类型
		String serv = CaPublic.QryCmsvtpsAcc(servtp);
		if (CommUtil.isNull(serv)) {
			throw QtError.Custa.BNASE053();
		}
		
	   //获取组合子账户类型
	   String sbac = CaPublic.QrySbattps(sbactp.getValue());
	   if (CommUtil.isNull(sbac)) {
		   throw QtError.Custa.BNASE006();
	   }
		
		String limtStr = "";
		if (CommUtil.isNotNull(tblPrcs) && CommUtil.isNotNull(tblPrcs.getLimttp())) {
			limtStr = tblPrcs.getLimttp();
		} else {
			throw QtError.Custa.BNASE159();
		}
		
		//取额度类型中文名
		String limttpName = CommUtil.isNotNull(tblPrcs.getRemak2()) ? tblPrcs.getRemak2() : "";
				
		//获取组合账户额度类型
		String limt = CaPublic.QryCmqttps(limtStr);
		//log.debug("-----认证方式限额有效记录数："+aurtList.size());
		
//		E_ACLMFG aclmfg = E_ACLMFG._3;
		
		E_ACCTROUTTYPE accounttype = ApAcctRoutTools.getRouteType(knaCust.getCustno());
		
		/*
		if (CommUtil.isNull(input.getLimttp())){
		    throw DpModuleError.DpstAcct.E9999("额度类型输入不能为空!");
		}*/
		
		
		// 红包回退、隔日冲正 传入负值，不金额额度控制
		if(CommUtil.compare(tranam, BigDecimal.ZERO)>0){
			log.debug("-----额度控制正交易进入-------");
			IoBrchInfo supKubBrch =SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(brchno);
			// 查询所有配置信息
			List<KupAcrtBrch> acrtup  =AccountLimitDao.selKupAcrtBrchAll(supKubBrch.getBrchno(), accounttype, limttp.getValue(), null, null, accttp.getValue(), input.getSbactp().getValue(), pytltp, false);
			List<KupAcrtBrch> acrtcou  =AccountLimitDao.selKupAcrtBrchAll(brchno, accounttype, limttp.getValue(), null, null, accttp.getValue(),  input.getSbactp().getValue(), pytltp, false);
			// 取出有效账户配置信息
			List<KupAcrtBrch> acrtList = new ArrayList<>();
			for(KupAcrtBrch acrt_tmp:acrtup){
				if(limt.indexOf("'"+acrt_tmp.getLimttp()+"'")>=0 && serv.indexOf("'"+acrt_tmp.getServtp()+"'") >=0 && sbac.indexOf("'"+acrt_tmp.getSbactp()+"'") >=0){
					boolean flag = false;
					for(KupAcrtBrch acrtC:acrtcou){
						if(acrtC.getAcrtno().equals(acrt_tmp.getAcrtno())){
							acrtList.add(acrtC);
							flag = true;
						}
					}
					if(flag == false){
						acrtList.add(acrt_tmp);
					}
				}
			}
			acrtup = null;
			acrtcou=null;
			log.debug("-----额度限额有效信息记录数："+acrtList.size());
			//  查客户累计信息
			List<KubTsscAcct>  acctList = AccountLimitDao.selKubTsscAcct2(custac, accounttype, limttp.getValue(), null , input.getSbactp() , pytltp, false);
			log.debug("-----客户限额累计信息记录数："+acctList.size());
			
			// 查询额度类型、渠道、子账户类型明细信息
			// 查询额度明细类型 
			List<KupCurtLimt> listLim =  AccountLimitDao.selCurtLimt(null,null, false);
			
			//查询渠道明细类型
			//List<KupCurtServ> listSer = AccountLimitDao.selCurtServ(null, null, false);
			
			//查询子账户明细类型
			List<KupCurtSbac> listSba = AccountLimitDao.selCurtSmb(null, null,  false);
			
			// 根据查询到的有效配置信息控制限额
			for(KupAcrtBrch acrt_tmp:acrtList){
				// 单笔
				if(CommUtil.compare(acrt_tmp.getLimtkd(), E_LIMTKD._01) == 0){
					log.debug("单笔限额序号："+acrt_tmp.getAcrtno()+"，限额值："+acrt_tmp.getLmtval());
					if (CommUtil.compare(tranam, acrt_tmp.getLmtval()) > 0 && CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {
						throw QtError.Custa.BNASE160();
					}
				}
				// 日累计金额
				if(CommUtil.compare(acrt_tmp.getLimtkd(), E_LIMTKD._02) == 0){
					// String strServ = IoCaSevAccountLimtPublic.getLowerList(listSer, acrt_tmp.getServtp(), "0");
					String strServ;
					//　modify by zhuxw　20171024
					List<KupCurtServ> listSer1 = KupCurtServDao.selectAll_odb2(acrt_tmp.getServtp(), false);
					StringBuffer sbf = new StringBuffer();
					if(CommUtil.isNotNull(listSer1)){
						for(KupCurtServ KupCurtServ_tmp:listSer1){
							sbf.append("'").append(KupCurtServ_tmp.getServtp()).append("'");
						}
					}
					strServ = sbf.toString();
					//　modify by zhuxw　20171024
					String strLim =  IoCaSevAccountLimtPublic.getLowerList(listLim, acrt_tmp.getLimttp(), "1");
					String strSba =  IoCaSevAccountLimtPublic.getLowerList(listSba, acrt_tmp.getSbactp(), "2");
					BigDecimal talVal = IoCaSevAccountLimtPublic.getTsscAccVal(strLim, strServ, strSba, E_LIMTKD._02, currdt, acctList);
					log.debug("日累计金额限额序号："+acrt_tmp.getAcrtno()+"，限额值："+acrt_tmp.getLmtval()+"，客户累计额："+talVal);
					if(aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1  //日累计
							&& CommUtil.compare(tranam.add(talVal), acrt_tmp.getLmtval()) > 0 
							&& CommUtil.compare(tranam, BigDecimal.ZERO) > 0){
								throw QtError.Custa.BNASE161();
					}
				}
				//月累计金额
				if(CommUtil.compare(acrt_tmp.getLimtkd(), E_LIMTKD._03) == 0){
					// String strServ = IoCaSevAccountLimtPublic.getLowerList(listSer, acrt_tmp.getServtp(), "0");
					String strServ;
					//　modify by zhuxw　20171024
					List<KupCurtServ> listSer1 = KupCurtServDao.selectAll_odb2(acrt_tmp.getServtp(), false);
					StringBuffer sbf = new StringBuffer();
					if(CommUtil.isNotNull(listSer1)){
						for(KupCurtServ KupCurtServ_tmp:listSer1){
							sbf.append("'").append(KupCurtServ_tmp.getServtp()).append("'");
						}
					}
					strServ = sbf.toString();
					//　modify by zhuxw　20171024
					String strLim =  IoCaSevAccountLimtPublic.getLowerList(listLim, acrt_tmp.getLimttp(), "1");
					String strSba =  IoCaSevAccountLimtPublic.getLowerList(listSba, acrt_tmp.getSbactp(), "2");
					BigDecimal talVal = IoCaSevAccountLimtPublic.getTsscAccVal(strLim, strServ, strSba, E_LIMTKD._03, currdt, acctList);
					log.debug("月累计金额限额序号："+acrt_tmp.getAcrtno()+"，限额值："+acrt_tmp.getLmtval()+"，客户累计额："+talVal);
					if(aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1
							&& CommUtil.compare(tranam.add(talVal), acrt_tmp.getLmtval()) > 0
							&& CommUtil.compare(tranam, BigDecimal.ZERO) > 0){
						throw QtError.Custa.BNASE162();
					}
				}
				
				//年累计金额
				if(CommUtil.compare(acrt_tmp.getLimtkd(), E_LIMTKD._04) == 0){
					// String strServ = IoCaSevAccountLimtPublic.getLowerList(listSer, acrt_tmp.getServtp(), "0");
					String strServ;
					//　modify by zhuxw　20171024
					List<KupCurtServ> listSer1 = KupCurtServDao.selectAll_odb2(acrt_tmp.getServtp(), false);
					StringBuffer sbf = new StringBuffer();
					if(CommUtil.isNotNull(listSer1)){
						for(KupCurtServ KupCurtServ_tmp:listSer1){
							sbf.append("'").append(KupCurtServ_tmp.getServtp()).append("'");
						}
					}
					strServ = sbf.toString();
					//　modify by zhuxw　20171024
					String strLim =  IoCaSevAccountLimtPublic.getLowerList(listLim, acrt_tmp.getLimttp(), "1");
					String strSba =  IoCaSevAccountLimtPublic.getLowerList(listSba, acrt_tmp.getSbactp(), "2");
					BigDecimal talVal = IoCaSevAccountLimtPublic.getTsscAccVal(strLim, strServ, strSba, E_LIMTKD._04, currdt, acctList);
					log.debug("年累计金额限额序号："+acrt_tmp.getAcrtno()+"，限额值："+acrt_tmp.getLmtval()+"，客户累计额："+talVal);
					if(aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1
							&& CommUtil.compare(tranam.add(talVal), acrt_tmp.getLmtval()) > 0
							&& CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {
						throw QtError.Custa.BNASE163();
					}
				}

				// 日累计次数
				if(CommUtil.compare(acrt_tmp.getLimtkd(), E_LIMTKD._11) == 0){
					// String strServ = IoCaSevAccountLimtPublic.getLowerList(listSer, acrt_tmp.getServtp(), "0");
					String strServ;
					//　modify by zhuxw　20171024
					List<KupCurtServ> listSer1 = KupCurtServDao.selectAll_odb2(acrt_tmp.getServtp(), false);
					StringBuffer sbf = new StringBuffer();
					if(CommUtil.isNotNull(listSer1)){
						for(KupCurtServ KupCurtServ_tmp:listSer1){
							sbf.append("'").append(KupCurtServ_tmp.getServtp()).append("'");
						}
					}
					strServ = sbf.toString();
					//　modify by zhuxw　20171024
					String strLim =  IoCaSevAccountLimtPublic.getLowerList(listLim, acrt_tmp.getLimttp(), "1");
					String strSba =  IoCaSevAccountLimtPublic.getLowerList(listSba, acrt_tmp.getSbactp(), "2");
					BigDecimal talVal = IoCaSevAccountLimtPublic.getTsscAccVal(strLim, strServ, strSba, E_LIMTKD._11, currdt, acctList);
					log.debug("日累计次数限额序号："+acrt_tmp.getAcrtno()+"，限额值："+acrt_tmp.getLmtval()+"，客户累计额："+talVal);
					if(aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1
							&& CommUtil.compare(talVal.add(BigDecimal.ONE), acrt_tmp.getLmtval()) > 0
							&& CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {
						throw QtError.Custa.BNASE088();
					}
				}
				
				// 月累计次数
				if(CommUtil.compare(acrt_tmp.getLimtkd(), E_LIMTKD._12) == 0){
					// String strServ = IoCaSevAccountLimtPublic.getLowerList(listSer, acrt_tmp.getServtp(), "0");
					String strServ;
					//　modify by zhuxw　20171024
					List<KupCurtServ> listSer1 = KupCurtServDao.selectAll_odb2(acrt_tmp.getServtp(), false);
					StringBuffer sbf = new StringBuffer();
					if(CommUtil.isNotNull(listSer1)){
						for(KupCurtServ KupCurtServ_tmp:listSer1){
							sbf.append("'").append(KupCurtServ_tmp.getServtp()).append("'");
						}
					}
					strServ = sbf.toString();
					//　modify by zhuxw　20171024
					String strLim =  IoCaSevAccountLimtPublic.getLowerList(listLim, acrt_tmp.getLimttp(), "1");
					String strSba =  IoCaSevAccountLimtPublic.getLowerList(listSba, acrt_tmp.getSbactp(), "2");
					BigDecimal talVal = IoCaSevAccountLimtPublic.getTsscAccVal(strLim, strServ, strSba, E_LIMTKD._12, currdt, acctList);
					log.debug("月累计次数限额序号："+acrt_tmp.getAcrtno()+"，限额值："+acrt_tmp.getLmtval()+"，客户累计额："+talVal);
					if(aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1
							&& CommUtil.compare(talVal.add(BigDecimal.ONE), acrt_tmp.getLmtval()) > 0
							&& CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {
						throw QtError.Custa.BNASE089();
					}
				}
				
				// 年累计次数
				if(CommUtil.compare(acrt_tmp.getLimtkd(), E_LIMTKD._13) == 0){
					// String strServ = IoCaSevAccountLimtPublic.getLowerList(listSer, acrt_tmp.getServtp(), "0");
					String strServ;
					//　modify by zhuxw　20171024
					List<KupCurtServ> listSer1 = KupCurtServDao.selectAll_odb2(acrt_tmp.getServtp(), false);
					StringBuffer sbf = new StringBuffer();
					if(CommUtil.isNotNull(listSer1)){
						for(KupCurtServ KupCurtServ_tmp:listSer1){
							sbf.append("'").append(KupCurtServ_tmp.getServtp()).append("'");
						}
					}
					strServ = sbf.toString();
					//　modify by zhuxw　20171024
					String strLim =  IoCaSevAccountLimtPublic.getLowerList(listLim, acrt_tmp.getLimttp(), "1");
					String strSba =  IoCaSevAccountLimtPublic.getLowerList(listSba, acrt_tmp.getSbactp(), "2");
					BigDecimal talVal = IoCaSevAccountLimtPublic.getTsscAccVal(strLim, strServ, strSba, E_LIMTKD._13, currdt, acctList);
					log.debug("年累计次数限额序号："+acrt_tmp.getAcrtno()+"，限额值："+acrt_tmp.getLmtval()+"，客户累计额："+talVal);
					if (aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1
							&& CommUtil.compare(talVal.add(BigDecimal.ONE), acrt_tmp.getLmtval()) > 0
							&& CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {
						throw QtError.Custa.BNASE090();
					}
				}
			}
			
			sglmam = CaPublic.setValueIfZero(sglmam);
			dylmam = CaPublic.setValueIfZero(dylmam);
			mtlmam = CaPublic.setValueIfZero(mtlmam);
			yrlmam = CaPublic.setValueIfZero(yrlmam);
			dylmtm = CaPublic.setValueIfZero(dylmtm);
			mtlmtm = CaPublic.setValueIfZero(mtlmtm);
			yrlmtm = CaPublic.setValueIfZero(yrlmtm);
			
			BigDecimal talam01 = BigDecimal.ZERO; // 日累计金额
			BigDecimal talam02 = BigDecimal.ZERO; // 月累计金额
			BigDecimal talam03 = BigDecimal.ZERO; // 年累计金额
			long taltm01 = 0;// 日累计次数
			long taltm02 = 0;// 月累计次数
			long taltm03 = 0;// 年累计次数
			
			// 如果行社没有设置限额走下面控制，设置限额走下面控制也不受影响
			if (CommUtil.compare(tranam, sglmam) > 0 && CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {
				throw QtError.Custa.BNASE160();
			}
			if (aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1){
				talam01 = IoCaSevAccountLimtPublic.getTsscAccVal(limtStr, servtp, sbactp.getValue(), E_LIMTKD._02, currdt, acctList);
				if(CommUtil.compare(tranam.add(talam01), dylmam) > 0){
							throw QtError.Custa.BNASE161();
				}
				
				talam02 = IoCaSevAccountLimtPublic.getTsscAccVal(limtStr, servtp, sbactp.getValue(), E_LIMTKD._02, currdt, acctList);
				if(CommUtil.compare(tranam.add(talam02), mtlmam) > 0){
					throw QtError.Custa.BNASE162();
				}
				
				talam03 = IoCaSevAccountLimtPublic.getTsscAccVal(limtStr, servtp, sbactp.getValue(), E_LIMTKD._02, currdt, acctList);
				if( CommUtil.compare(tranam.add(talam03), yrlmam) > 0) {
					throw QtError.Custa.BNASE163();
				}
				
				taltm01 = IoCaSevAccountLimtPublic.getTsscAccVal(limtStr, servtp, sbactp.getValue(), E_LIMTKD._02, currdt, acctList).longValue();
				if( CommUtil.compare(taltm01 + 1, dylmtm.longValue()) > 0) {
					throw QtError.Custa.BNASE088();
				}
				
				taltm02 = IoCaSevAccountLimtPublic.getTsscAccVal(limtStr, servtp, sbactp.getValue(), E_LIMTKD._02, currdt, acctList).longValue();
				if(CommUtil.compare(taltm02 + 1, mtlmtm.longValue()) > 0) {
					throw QtError.Custa.BNASE089();
				}
				
				taltm03 = IoCaSevAccountLimtPublic.getTsscAccVal(limtStr, servtp, sbactp.getValue(), E_LIMTKD._02, currdt, acctList).longValue();
				if (CommUtil.compare(taltm03 + 1, yrlmtm.longValue()) > 0) {
					throw QtError.Custa.BNASE090();
				}
			}
		}

		//查询客户额度、次数累计信息
		KubTsscAcct tblTsscAcct = KubTsscAcctDao.selectOneWithLock_odb1(custac, accounttype, CommUtil.toEnum(E_LIMTTP.class, limtStr), servtp, sbactp, pytltp, false);
		if (aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1) {
			//登记客户额度累计信息表kub_tssc_acct
			long times = 1;
			if(CommUtil.isNotNull(input.getOldate()) && CommUtil.compare(input.getTranam(), BigDecimal.ZERO) < 0) {   //收费调整错账冲正，存在金额为负数，用于恢复额度的情况 ——2017/02/15
				if (CommUtil.isNull(tblTsscAcct)) {
					return;
				} else {
					String tranYear = currdt.substring(0, 4); //交易年份
					String tranMonth = currdt.substring(4, 6); //交易月份
					String lastTranYear = input.getOldate().substring(0, 4); //上次交易年份
					String lastTranMonth = input.getOldate().substring(4, 6); //上次交易月份
					if (!CommUtil.equals(lastTranYear, tranYear)) { //年不同
						return;
					} else { //年相同
						tblTsscAcct.setYrtlam(tranam.add(tblTsscAcct.getYrtlam()));
						tblTsscAcct.setYrtltm(tblTsscAcct.getYrtltm() - times);
						if (!CommUtil.equals(lastTranMonth, tranMonth)) { //月不同
//							return;
						} else { //月相同
							tblTsscAcct.setMhtlam(tranam.add(tblTsscAcct.getMhtlam()));
							tblTsscAcct.setMhtltm(tblTsscAcct.getMhtltm() - times);
							if (CommUtil.compare(currdt, input.getOldate()) != 0) { //天不同
//								return;
							} else { //日期完全相同
								tblTsscAcct.setDytlam(tranam.add(tblTsscAcct.getDytlam()));
								tblTsscAcct.setDytltm(tblTsscAcct.getDytltm() - times);
							}
						}
					}
					KubTsscAcctDao.updateOne_odb1(tblTsscAcct);
				}				
			} else {
				if (CommUtil.isNull(tblTsscAcct)) {
					tblTsscAcct = SysUtil.getInstance(KubTsscAcct.class);
					tblTsscAcct.setAcctrt(accounttype);
					tblTsscAcct.setLimttp(CommUtil.toEnum(E_LIMTTP.class, limtStr));
					tblTsscAcct.setServtp(servtp);
					tblTsscAcct.setCustac(custac);
					tblTsscAcct.setAccttp(accttp);
					tblTsscAcct.setPytltp(pytltp);
					tblTsscAcct.setSbactp(sbactp);
					tblTsscAcct.setLastdt(currdt);
					tblTsscAcct.setDytlam(tranam);
					tblTsscAcct.setDytltm(times);
					tblTsscAcct.setMhtlam(tranam);
					tblTsscAcct.setMhtltm(times);
					tblTsscAcct.setYrtlam(tranam);
					tblTsscAcct.setYrtltm(times);
					
					KubTsscAcctDao.insert(tblTsscAcct);
				} else {
					String tranYear = currdt.substring(0, 4); //交易年份
					String tranMonth = currdt.substring(4, 6); //交易月份
					String lastTranYear = tblTsscAcct.getLastdt().substring(0, 4); //上次交易年份
					String lastTranMonth = tblTsscAcct.getLastdt().substring(4, 6); //上次交易月份
					if (!CommUtil.equals(lastTranYear, tranYear)) { //年不同
						tblTsscAcct.setLastdt(currdt);
						tblTsscAcct.setDytlam(tranam);
						tblTsscAcct.setMhtlam(tranam);
						tblTsscAcct.setYrtlam(tranam);
						tblTsscAcct.setDytltm(times);
						tblTsscAcct.setMhtltm(times);
						tblTsscAcct.setYrtltm(times);
					} else { //年相同
						tblTsscAcct.setYrtlam(tranam.add(tblTsscAcct.getYrtlam()));
						tblTsscAcct.setYrtltm(times + tblTsscAcct.getYrtltm());
						if (!CommUtil.equals(lastTranMonth, tranMonth)) { //月不同
							tblTsscAcct.setLastdt(currdt);
							tblTsscAcct.setDytlam(tranam);
							tblTsscAcct.setMhtlam(tranam);
							tblTsscAcct.setDytltm(times);
							tblTsscAcct.setMhtltm(times);
						} else { //月相同
							tblTsscAcct.setMhtlam(tranam.add(tblTsscAcct.getMhtlam()));
							tblTsscAcct.setMhtltm(times + tblTsscAcct.getMhtltm());
							if (CommUtil.compare(currdt, tblTsscAcct.getLastdt()) != 0) { //天不同
								tblTsscAcct.setDytlam(tranam);
								tblTsscAcct.setLastdt(currdt);
								tblTsscAcct.setDytltm(times);
							} else { //日期完全相同
								tblTsscAcct.setDytlam(tranam.add(tblTsscAcct.getDytlam()));
								tblTsscAcct.setDytltm(times + tblTsscAcct.getDytltm());
							}
						}
					}
					KubTsscAcctDao.updateOne_odb1(tblTsscAcct);
			}
			
				
			}
		}
		
//		CommTools.getBaseRunEnvs().setBusi_org_id(corpno);//还原交易报文头上送法人
		
		//登记交易明细表
		KubTsdtAcct tblTsdt = SysUtil.getInstance(KubTsdtAcct.class);
		tblTsdt.setTrandt(currdt); //交易日期
		String transq = CaPublic.genTransq();
		tblTsdt.setTransq(transq);
		tblTsdt.setServtp(servtp); //渠道
		tblTsdt.setServsq(servsq); //渠道流水
		tblTsdt.setServdt(servdt); //渠道交易日期
		tblTsdt.setAcctrt(accounttype); //客户类型
		tblTsdt.setLimttp(CommUtil.toEnum(E_LIMTTP.class, limtStr)); //额度类型
		tblTsdt.setCustac(custac);
		tblTsdt.setAccttp(accttp);
		tblTsdt.setSbactp(sbactp);
		tblTsdt.setPytltp(pytltp);
		tblTsdt.setTranam(tranam); //交易金额
		tblTsdt.setQuottp(E_QUOTTP.DED);
		tblTsdt.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch()); //交易机构
		tblTsdt.setAclmfg(aclmfg);
		
		KubTsdtAcctDao.insert(tblTsdt);
		
		
		/**
    	 * 冲正登记
    	 */
    	IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
    	cplInput.setCustac(custac); //电子账户号
    	cplInput.setTranam(tranam);
    	cplInput.setTranev(ApUtil.CA_EDUK_QT);
    	cplInput.setEvent1(String.valueOf(servtp));
    	cplInput.setEvent2(servsq);
    	cplInput.setEvent3(servdt);
    	//ApStrike.regBook(cplInput);
    	
		IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);    		
		apinput.setReversal_event_id(ApUtil.CA_EDUK_QT);
		apinput.setInformation_value(SysUtil.serialize(cplInput));
		MsEvent.register(apinput, true);

/*
 * 扣减账户限额的同时扣减客户限额    	
 */     
    	if (aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._2) {
    	    if (CommUtil.isNull(input.getAuthtp())){
    	        throw DpModuleError.DpstAcct.E9999("认证方式输入不能为空!");
    	    }
    	}
        
    	IoQtInsSrv.IoQtChkAndSub.InputSetter qtInput = SysUtil.getInstance(IoQtInsSrv.IoQtChkAndSub.InputSetter.class);
		qtInput.setAcctrt(accounttype);
		qtInput.setAccttp(input.getAccttp());
		qtInput.setAclmfg(aclmfg);
		if (CommUtil.isNotNull(input.getAuthtp())){
		    qtInput.setAuthtp(input.getAuthtp().getValue());
		}
		qtInput.setBrchno(input.getBrchno());
		qtInput.setCustac(input.getCustac());
		qtInput.setCustid(custid);
		qtInput.setLimttp(limttp.getValue());
		qtInput.setPytltp(pytltp);
		qtInput.setRebktp(input.getRebktp());
		qtInput.setRisklv(risklv);
		qtInput.setSbactp(input.getSbactp());
		qtInput.setServdt(input.getServdt());
		qtInput.setServsq(input.getServsq());
		qtInput.setServtp(input.getServtp());
		qtInput.setTranam(input.getTranam());
		qtInput.setCustlv(input.getCustlv());
		IoQtInsSrv ioQtInsSrv = SysUtil.getInstance(IoQtInsSrv.class);
		IoQtInsSrv.IoQtChkAndSub.Output qtOutput = SysUtil.getInstance(IoQtInsSrv.IoQtChkAndSub.Output.class);
		if (CommUtil.compare(qtInput.getTranam(), BigDecimal.ZERO) >= 0) {
			ioQtInsSrv.ChKAndSubQt(qtInput, qtOutput);
		}
/*
 * 恢复账户限额的同时恢复客户限额 （特殊情况下使用）  	
 */		
		else{
			String strksq = input.getStrksq();
			if(CommUtil.isNull(strksq)){
				throw DpModuleError.DpstComm.BNAS1634();
			}
			qtInput.setTranam(qtInput.getTranam().abs());
			IoQtRevest.InputSetter qrInput = SysUtil.getInstance(IoQtRevest.InputSetter.class);
			CommUtil.copyProperties(qrInput, qtInput);
			qrInput.setStrksq(strksq);
			IoQtInsSrv.IoQtRevest.Output qrOutput = SysUtil.getInstance(IoQtInsSrv.IoQtRevest.Output.class);
			ioQtInsSrv.RevestQT(qrInput, qrOutput);
		}
		
	}
	
	/**
	 * 电子账户额度恢复
	 */
	@Override
	public void RevAcctQuota(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcRevQuota.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcRevQuota.Output output) {
		String servtp = input.getServtp(); //渠道
		String servdt = input.getServdt(); //渠道交易日期
		String servsq = input.getServsq(); //渠道流水
		String custac = input.getCustac(); //电子账号
/*		String brchno = input.getBrchno(); //客户所属机构号
		E_CUSTTP custtp = input.getCusttp(); //客户类型
		E_LIMTTP limttp = input.getLimttp(); //额度类型
		BigDecimal tranam = input.getTranam(); //交易金额
		E_ACCATP accttp = input.getAccttp(); //账户分类
		E_SBACTP sbactp = input.getSbactp(); //子账户类型
		E_PYTLTP pytltp = input.getPytltp(); //支付工具
		String custac = input.getCustac(); //电子账号
*/		
//		if (CommUtil.isNull(servtp)) {
//			throw QtError.Custa.E0001("渠道不能为空");
//		}
		if (CommUtil.isNull(servsq)) {
			throw QtError.Custa.BNASE048();
		}
		if (CommUtil.isNull(servdt)) {
			throw QtError.Custa.BNASE049();
		}
		if (CommUtil.isNull(custac)) {
			throw QtError.Custa.BNASE120();
		}
/*		if (CommUtil.isNull(brchno)) {
			throw QtError.Custa.E0001("客户所属机构号不能为空");
		}
		if (CommUtil.isNull(custtp)) {
			throw QtError.Custa.E0001("客户类型不能为空");
		}
		if (CommUtil.isNull(limttp)) {
			throw QtError.Custa.E0001("额度类型不能为空");
		}
		if (CommUtil.isNull(tranam)) {
			throw QtError.Custa.E0001("交易金额不能为空");
		}*/
		
		KubTsdtAcct tblTsdt = AccountLimitDao.selKubTsdt(custac, servdt, servsq, false);
		if (CommUtil.isNull(tblTsdt)) {
//			throw QtError.Custa.E0001("找不到原交易");
			return;   //找不到原交易不报错，直接跳出  —— mod by chengen 20170329
		}
		
		if (CommUtil.isNotNull(tblTsdt.getServtp())) {
			servtp = tblTsdt.getServtp();
		}
		
		if (CommUtil.isNull(servtp)) {
			throw QtError.Custa.BNASE164();
		}
		
		E_ACCTROUTTYPE custtp = tblTsdt.getAcctrt(); //客户类型
		E_LIMTTP limttp = tblTsdt.getLimttp(); //额度类型
		BigDecimal tranam = tblTsdt.getTranam(); //交易金额
//		String custac = tblTsdt.getCustac(); //电子账户
		E_SBACTP sbactp = tblTsdt.getSbactp(); //子账户类型
		E_PYTLTP pytltp = tblTsdt.getPytltp(); //支付工具
		E_ACLMFG aclmfg = E_ACLMFG._3; //累计限额标志
		if(CommUtil.isNull(aclmfg)){
			aclmfg = E_ACLMFG._3;
		}

		String currdt = CommTools.getBaseRunEnvs().getTrxn_date();
		String currYear = currdt.substring(0, 4); //额度中心年份
		String currMonth = currdt.substring(4, 6); //额度中心月份
		
		String origYear = tblTsdt.getTrandt().substring(0, 4); //原交易年份
		String origMonth = tblTsdt.getTrandt().substring(4, 6);//原交易月份
		
		//恢复账户限额
		if (CommUtil.equals(currdt, tblTsdt.getTrandt())) { //日期相同
			if (aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1) {
				if (CommUtil.isNull(pytltp)) pytltp = CommUtil.toEnum(E_PYTLTP.class, "%");
				KubTsscAcct tblTsscCust = KubTsscAcctDao.selectOneWithLock_odb1(custac, custtp, limttp, servtp, sbactp, pytltp, false);
				if (CommUtil.isNull(tblTsscCust)) {
					throw QtError.Custa.BNASE165();
				}
				BigDecimal dytlam = tblTsscCust.getDytlam();
				BigDecimal mhtlam = tblTsscCust.getMhtlam();
				BigDecimal yrtlam = tblTsscCust.getYrtlam();
				long dytltm = tblTsscCust.getDytltm();
				long mhtltm = tblTsscCust.getMhtltm();
				long yrtltm = tblTsscCust.getYrtltm();
				long times = 1;
				tblTsscCust.setDytlam(dytlam.subtract(tranam));
				tblTsscCust.setMhtlam(mhtlam.subtract(tranam));
				tblTsscCust.setYrtlam(yrtlam.subtract(tranam));
				tblTsscCust.setDytltm(dytltm - times);
				tblTsscCust.setMhtltm(mhtltm - times);
				tblTsscCust.setYrtltm(yrtltm - times);
				KubTsscAcctDao.updateOne_odb1(tblTsscCust);
			}
			
		} else if (CommUtil.equals(currMonth, origMonth)) { //月相同、日不相同
			if (aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1) {
				if (CommUtil.isNull(pytltp)) pytltp = CommUtil.toEnum(E_PYTLTP.class, "%");
				KubTsscAcct tblTsscCust = KubTsscAcctDao.selectOneWithLock_odb1(custac, custtp, limttp, servtp, sbactp, pytltp, false);
				if (CommUtil.isNull(tblTsscCust)) {
					throw QtError.Custa.BNASE165();
				}
				
				BigDecimal mhtlam = tblTsscCust.getMhtlam();
				BigDecimal yrtlam = tblTsscCust.getYrtlam();
				long mhtltm = tblTsscCust.getMhtltm();
				long yrtltm = tblTsscCust.getYrtltm();
				long times = 1;
				tblTsscCust.setMhtlam(mhtlam.subtract(tranam));
				tblTsscCust.setYrtlam(yrtlam.subtract(tranam));
				tblTsscCust.setMhtltm(mhtltm - times);
				tblTsscCust.setYrtltm(yrtltm - times);
				
				KubTsscAcctDao.updateOne_odb1(tblTsscCust);
			}
		} else if (CommUtil.equals(currYear, origYear)) { //年分相同、月不同的情况值恢复年累计额度
			if (aclmfg != E_ACLMFG._0 && aclmfg != E_ACLMFG._1) {
				if (CommUtil.isNull(pytltp)) pytltp = CommUtil.toEnum(E_PYTLTP.class, "%");
				KubTsscAcct tblTsscCust = KubTsscAcctDao.selectOneWithLock_odb1(custac, custtp, limttp, servtp, sbactp, pytltp, false);
				if (CommUtil.isNull(tblTsscCust)) {
					throw QtError.Custa.BNASE165();
				}
				
				BigDecimal yrtlam = tblTsscCust.getYrtlam();
				long yrtltm = tblTsscCust.getYrtltm();
				long times = 1;
				tblTsscCust.setYrtlam(yrtlam.subtract(tranam));
				tblTsscCust.setYrtltm(yrtltm - times);
				
				KubTsscAcctDao.updateOne_odb1(tblTsscCust);
			} else {
				return;
			}
		}
		
		//修改交易状态
		tblTsdt.setQuottp(E_QUOTTP.REV);
		KubTsdtAcctDao.insert(tblTsdt);

/*
 * 恢复客户限额
 */
		String custid = CaDao.selCustidByCustac(custac, false);
		IoQtInsSrv ioQtInsSrv = SysUtil.getInstance(IoQtInsSrv.class);
		IoQtRevest.InputSetter qrInput = SysUtil.getInstance(IoQtRevest.InputSetter.class);
		qrInput.setServtp(servtp);
		qrInput.setServsq(servsq);
		qrInput.setStrksq(servsq);
		qrInput.setServdt(servdt);
		qrInput.setCustid(custid);
		qrInput.setTranam(tranam);
		IoQtRevest.Output qrOutput = SysUtil.getInstance(IoQtRevest.Output.class);
		ioQtInsSrv.RevestQT(qrInput, qrOutput);
	}

	/**
	 * 
	 * @Title: moaclimitsel
	 * @Description: 移动前端账户限额查询
	 * @param input
	 * @param property
	 * @param output
	 * @author xvdawei
	 * @date 2016年7月7日 上午9:53:58
	 * @version V2.3.0
	 */	
	@Override
	public void moaclimitsel(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.MoAcLimitSel.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.MoAcLimitSel.Output output) {

		if (CommUtil.isNull(input.getBrchno())) {
			throw QtError.Custa.BNASE099();
		}
		if (CommUtil.isNull(input.getCustac())) {
			throw QtError.Custa.BNASE120();
		}
		if (CommUtil.isNull(input.getAccttp())) {
			throw QtError.Custa.BNASE016();
		}
		if (CommUtil.isNull(input.getAcctrt())) {
			throw QtError.Custa.BNASE094();
		}
		if (CommUtil.isNull(input.getServtp())) {
			throw QtError.Custa.BNASE051();
		}
		if (CommUtil.isNull(input.getSbactp())) {
			throw QtError.Custa.BNASE005();
		}

		E_ACCTROUTTYPE custtp = input.getAcctrt(); //账户类型
		String servtp = input.getServtp(); //渠道
		E_ACCATP accttp = input.getAccttp(); //账户分类
		String brchno = input.getBrchno(); //客户所属机构
//		String custac = input.getCustac(); //电子账户
		E_SBACTP sbactp = input.getSbactp();//子账户类型
		E_LIMTST limtst = E_LIMTST.NL;
		
		//出账
		BigDecimal limitDyOut = BigDecimal.ZERO; //出账日累计限额
		BigDecimal limitYrOut = BigDecimal.ZERO; //出账年累计限额
		
		//入账
		BigDecimal limitDyIn = BigDecimal.ZERO; //入账日累计限额
		BigDecimal limitYrIn = BigDecimal.ZERO; // 入账年累计限额
				
		String servtps = CaPublic.QryCmsvtps(servtp);
		String sbactps = CaPublic.QrySbattps(sbactp.getValue());
		
		
		//检查支付工具是否为 99-全部
//        if (AccountLimitDao.selCntKupAcrtBrch2(brchno, custtp, accttp, servtps, false) <= 0) {
//        	
//        	//出账
//        	limitDyOut = BigDecimal.valueOf(999999999999.0);
//        	limitYrOut = BigDecimal.valueOf(999999999999.0);
//        	
//        	//入账
//        	limitDyIn = BigDecimal.valueOf(999999999999.0);
//        	limitYrIn = BigDecimal.valueOf(999999999999.0);
//        } 
        
		
        E_PYTLTP pytltp = null;//CommUtil.toEnum(E_PYTLTP.class, "99");

        String limttp_out = "2";
        KupAcrtBrch acbrDylmOut = IoCaSevAccountLimtPublic.getAcrtLimitMin(brchno, custtp, limttp_out, E_LIMTKD._02, servtps, accttp.getValue(), sbactps, pytltp, limtst);

        if (CommUtil.isNotNull(acbrDylmOut)) {
        	limitDyOut = acbrDylmOut.getLmtval();
        }

        KupAcrtBrch acbrYrlmOut = IoCaSevAccountLimtPublic.getAcrtLimitMin(brchno, custtp, limttp_out, E_LIMTKD._04, servtps, accttp.getValue(), sbactps, pytltp, limtst);

        if (CommUtil.isNotNull(acbrYrlmOut)) {
        	limitYrOut = acbrYrlmOut.getLmtval();
        }
        
        String limttp_in = "1";
        KupAcrtBrch acbrDylmIn = IoCaSevAccountLimtPublic.getAcrtLimitMin(brchno, custtp, limttp_in, E_LIMTKD._02, servtps, accttp.getValue(), sbactps, pytltp, limtst);
        
        if (CommUtil.isNotNull(acbrDylmIn)) {
        	limitDyIn = acbrDylmIn.getLmtval();
        }
        
        KupAcrtBrch acbrYrlmIn = IoCaSevAccountLimtPublic.getAcrtLimitMin(brchno, custtp, limttp_in, E_LIMTKD._04, servtps, accttp.getValue(), sbactps, pytltp, limtst);
        
        if (CommUtil.isNotNull(acbrYrlmIn)) {
        	limitYrIn = acbrYrlmIn.getLmtval();
        }
        	

        limitDyIn = IoCaSevAccountLimtPublic.setValueIfZero(limitDyIn);
        limitYrIn = IoCaSevAccountLimtPublic.setValueIfZero(limitYrIn);
        limitDyOut = IoCaSevAccountLimtPublic.setValueIfZero(limitDyOut);
        limitYrOut = IoCaSevAccountLimtPublic.setValueIfZero(limitYrOut);

        
        String limit_py = E_LIMTTP.PY.toString();
        
        BigDecimal limitSgPy = BigDecimal.valueOf(999999999999.0);
        KupAcrtBrch pyacSglm = IoCaSevAccountLimtPublic.getAcrtLimitMin(brchno, custtp, limit_py, E_LIMTKD._01, servtps, accttp.getValue(), sbactps, pytltp, limtst);
		
        if (CommUtil.isNotNull(pyacSglm)) {
        	limitSgPy = pyacSglm.getLmtval();
        }
        limitSgPy = IoCaSevAccountLimtPublic.setValueIfZero(limitSgPy);
        
        
        BigDecimal limitDyPy = BigDecimal.valueOf(999999999999.0);
        KupAcrtBrch pyacDylm = IoCaSevAccountLimtPublic.getAcrtLimitMin(brchno, custtp, limit_py, E_LIMTKD._02, servtps, accttp.getValue(), sbactps, pytltp, limtst);
        
        if (CommUtil.isNotNull(pyacDylm)) {
        	limitDyPy = pyacDylm.getLmtval();
        }
        limitDyPy = IoCaSevAccountLimtPublic.setValueIfZero(limitDyPy);
        
        BigDecimal limitMnPy = BigDecimal.valueOf(999999999999.0);
        KupAcrtBrch  pyacMnlm = IoCaSevAccountLimtPublic.getAcrtLimitMin(brchno, custtp, limit_py, E_LIMTKD._03, servtps, accttp.getValue(), sbactps, pytltp, limtst);
        
        if (CommUtil.isNotNull(pyacMnlm)) {
        	limitMnPy = pyacMnlm.getLmtval();
        }
        limitMnPy = IoCaSevAccountLimtPublic.setValueIfZero(limitMnPy);

		// 输出
		output.setLimitDyIn(limitDyIn);
		output.setLimitDyOut(limitDyOut);
		output.setLimitYrIn(limitYrIn);
		output.setLimitYrOut(limitYrOut);
		output.setLimitSgPy(limitSgPy);
		output.setLimitDyPy(limitDyPy);
		output.setLimitMnPy(limitMnPy);
		
	}

	/**
	 * 
	 * @Title: moaclimitupd
	 * @Description: 移动前端账户限额修改
	 * @param input
	 * @author xvdawei
	 * @date 2016年7月7日 上午9:53:58
	 * @version V2.3.0
	 */	
	@SuppressWarnings("unused")
	@Override
	public void moaclimitupd(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.MoAcLimitUpd.Input input) {
		
		if (true) {
			throw DpModuleError.DpstComm.BNAS0209();
		}
		
		if (CommUtil.isNull(input.getAccttp())) {
			throw CaError.Eacct.BNAS0896();
		}
		
		E_ACCTROUTTYPE acctrt = input.getAcctrt(); //客户类型
		String servtp = input.getServtp(); //渠道
		E_ACCATP accttp = input.getAccttp(); //账户分类
		String brchno = input.getBrchno();
		E_SBACTP sbactp = input.getSbactp();//子账户类型
		E_LIMTST limtst = E_LIMTST.NL;
		BigDecimal sglmam = BigDecimal.ZERO; //单笔限额
		BigDecimal dllmam = BigDecimal.ZERO; //日累计限额

		E_PYTLTP pytltp =  null;//CommUtil.toEnum(E_PYTLTP.class, "%");
		
//		if ((accttp == E_ACCATP.GLOBAL || accttp == E_ACCATP.FINANCE)
//				&& CommUtil.isNull(sbactp)) {
//			sbactp = E_SBACTP._11;
//		}
//		else if (accttp == E_ACCATP.WALLET && CommUtil.isNull(sbactp)) {
//			sbactp = E_SBACTP._12;
//		}
		
//		SelLimitOut limitOutSglmam = 
//				IoCaSevAccountLimtPublic.getAcrtLimitVal(brchno, custtp, E_LIMTTP.PY, E_LIMTKD._01, servtp, accttp, sbactp, pytltp);
//		SelLimitOut limitOutDllmam = 
//				IoCaSevAccountLimtPublic.getAcrtLimitVal(brchno, custtp, E_LIMTTP.PY, E_LIMTKD._02, servtp, accttp, sbactp, pytltp);
//		
		String servtps = CaPublic.QryCmsvtps(servtp);
		String limttp_py = CaPublic.QryCmqttps(E_LIMTTP.PY.getValue());
		String sbactps = CaPublic.QrySbattps(sbactp.getValue());
		KupAcrtBrch acbrSglm01 = IoCaSevAccountLimtPublic.getAcrtLimitMin(brchno, acctrt, limttp_py, E_LIMTKD._01, servtps, accttp.getValue(), sbactps, pytltp,limtst);
		KupAcrtBrch acbrDllm01 = IoCaSevAccountLimtPublic.getAcrtLimitMin(brchno, acctrt, limttp_py, E_LIMTKD._02, servtps, accttp.getValue(), sbactps, pytltp,limtst);
		
		if (CommUtil.isNotNull(acbrSglm01)) {
			sglmam = acbrSglm01.getLmtval();
		}else{
			throw QtError.Custa.BNASE166();
		}
		if (CommUtil.isNotNull(acbrDllm01)) {
			dllmam = acbrDllm01.getLmtval();
		}else{
			throw QtError.Custa.BNASE034();
		}
		// 修改额度类型为支付的限额
		if (CommUtil.compare(sglmam, BigDecimal.ZERO) > 0
				&& CommUtil.compare(input.getSglm02(), sglmam) > 0) {
			throw QtError.Custa.BNASE167();
		}
		if (CommUtil.compare(dllmam, BigDecimal.ZERO) > 0
				&& CommUtil.compare(input.getDllm02(), dllmam) > 0) {
			throw QtError.Custa.BNASE044();
		}

		KupAcrtCust custup = KupAcrtCustDao.selectOne_odb1(input.getCustac(), input.getAcctrt(), E_LIMTTP.PY, input.getServtp(), false);
		if (CommUtil.isNotNull(custup)) {
			custup.setSinglm(input.getSglm02());
			custup.setDytllm(input.getDllm02());
			KupAcrtCustDao.updateOne_odb1(custup);
		} else {
			// 生成账户限额序号
			String aclisq = IoCaSevAccountLimtPublic.genAcrtsq("aclisq");
			// 插入记录
			KupAcrtCust acrt = SysUtil.getInstance(KupAcrtCust.class);
			acrt.setCustac(input.getCustac());
			acrt.setBrchno(input.getBrchno());
			acrt.setAcctrt(input.getAcctrt());
			acrt.setServtp(servtp);
			acrt.setAcrtsq(aclisq);
			acrt.setDytllm(input.getDllm02());
			acrt.setLimttp(E_LIMTTP.PY);
			acrt.setSinglm(input.getSglm02());
//			acrt.setAccttp(input.getAccttp());
			
			KupAcrtCustDao.insert(acrt);
		}

		// 修改额度类型为转账的限额
		if (input.getAccttp() == E_ACCATP.GLOBAL || input.getAccttp() == E_ACCATP.FINANCE) {
//			SelLimitOut limitOutSglmamone = IoCaSevAccountLimtPublic.getAcrtLimitVal(brchno, custtp, E_LIMTTP.TR, E_LIMTKD._01, servtp, accttp, sbactp, pytltp);
//			SelLimitOut limitOutDllmamone = IoCaSevAccountLimtPublic.getAcrtLimitVal(brchno, custtp, E_LIMTTP.TR, E_LIMTKD._02, servtp, accttp, sbactp, pytltp);
//			
			String limttp_tr = CaPublic.QryCmqttps(E_LIMTTP.TR.getValue());
    		KupAcrtBrch acbrSglm03 = IoCaSevAccountLimtPublic.getAcrtLimitMin(brchno, acctrt, limttp_tr, E_LIMTKD._01, servtps, accttp.getValue(), sbactps, pytltp,limtst);
    		KupAcrtBrch acbrDllm03 = IoCaSevAccountLimtPublic.getAcrtLimitMin(brchno, acctrt, limttp_tr, E_LIMTKD._02, servtps, accttp.getValue(), sbactps, pytltp,limtst);
    		
			
			if (CommUtil.isNotNull(acbrSglm03)) {
				sglmam = acbrSglm03.getLmtval();
			}else{
				throw QtError.Custa.BNASE168();
			}
			if (CommUtil.isNotNull(acbrDllm03)){
				dllmam = acbrDllm03.getLmtval();
			}else{
				throw QtError.Custa.BNASE032();
			}
			
			if (CommUtil.isNotNull(input.getSglm04())) {

				if (CommUtil.compare(input.getSglm04(), BigDecimal.valueOf((double)999999999999.0)) < 0
						&& CommUtil.compare(sglmam, BigDecimal.ZERO) > 0
						&& CommUtil.compare(input.getSglm04(), sglmam) > 0) {
					throw QtError.Custa.BNASE122();

				}
				if (CommUtil.compare(input.getDllm04(), BigDecimal.valueOf((double)999999999999.0)) < 0
						&& CommUtil.compare(dllmam, BigDecimal.ZERO) > 0
						&& CommUtil.compare(input.getDllm04(), dllmam) > 0) {
					throw QtError.Custa.BNASE043();

				}

				KupAcrtCust custuz = KupAcrtCustDao.selectOne_odb1(input.getCustac(), input.getAcctrt(), E_LIMTTP.TR, input.getServtp(), false);
				if (CommUtil.isNotNull(custuz)) {
					custuz.setSinglm(input.getSglm04());
					custuz.setDytllm(input.getDllm04());
					KupAcrtCustDao.updateOne_odb1(custuz);
				} else {

					// 生成账户限额序号
					String aclisq = IoCaSevAccountLimtPublic.genAcrtsq("aclisq");
					// 插入记录
					KupAcrtCust acrt = SysUtil.getInstance(KupAcrtCust.class);
					acrt.setCustac(input.getCustac());
					acrt.setBrchno(input.getBrchno());
					acrt.setAcctrt(acctrt);
					acrt.setServtp(servtp);
					acrt.setAcrtsq(aclisq);
					acrt.setDytllm(input.getDllm04());
					acrt.setLimttp(E_LIMTTP.TR);
					acrt.setSinglm(input.getSglm04());
//					acrt.setAccttp(input.getAccttp());
					KupAcrtCustDao.insert(acrt);
				}
			}
		}
		
		// 修改额度类型为取现的限额
		if (input.getAccttp() == E_ACCATP.GLOBAL) {
			
//			SelLimitOut limitOutSglmamtwo = IoCaSevAccountLimtPublic.getAcrtLimitVal(brchno, custtp, E_LIMTTP.DM, E_LIMTKD._01, servtp, accttp, sbactp, pytltp);
//			SelLimitOut limitOutDllmamtwo = IoCaSevAccountLimtPublic.getAcrtLimitVal(brchno, custtp, E_LIMTTP.DM, E_LIMTKD._02, servtp, accttp, sbactp, pytltp);

			String limttp_dm = CaPublic.QryCmqttps(E_LIMTTP.DM.getValue());
    		KupAcrtBrch acbrSglm05 = IoCaSevAccountLimtPublic.getAcrtLimitMin(brchno, acctrt, limttp_dm, E_LIMTKD._01, servtps, accttp.getValue(), sbactps, pytltp,limtst);
    		KupAcrtBrch acbrDllm05 = IoCaSevAccountLimtPublic.getAcrtLimitMin(brchno, acctrt, limttp_dm, E_LIMTKD._02, servtps, accttp.getValue(), sbactps, pytltp,limtst);
    	
			if (CommUtil.isNotNull(acbrSglm05)){
				sglmam = acbrSglm05.getLmtval();
			}else{
				throw QtError.Custa.BNASE169();
			}
			if (CommUtil.isNotNull(acbrDllm05)){
				dllmam = acbrDllm05.getLmtval();
			}else{
				throw QtError.Custa.BNASE036();
			}
			
			if (CommUtil.isNotNull(input.getSglm06())) {
				if (CommUtil.compare(input.getSglm06(), BigDecimal.valueOf((double)999999999999.0)) < 0
						&& CommUtil.compare(sglmam, BigDecimal.ZERO) > 0
						&& CommUtil.compare(input.getSglm06(), sglmam) > 0) {
					throw QtError.Custa.BNASE170();
				}
				
				if (CommUtil.compare(input.getDllm06(), BigDecimal.valueOf((double)999999999999.0)) < 0
						&& CommUtil.compare(dllmam, BigDecimal.ZERO) > 0
						&& CommUtil.compare(input.getDllm06(), dllmam) > 0) {
					throw QtError.Custa.BNASE171();
				}
				
				KupAcrtCust custuq = KupAcrtCustDao.selectOne_odb1(input.getCustac(), input.getAcctrt(), E_LIMTTP.DM, input.getServtp(), false);

				if (CommUtil.isNotNull(custuq)) {
					custuq.setSinglm(input.getSglm06());
					custuq.setDytllm(input.getDllm06());
					KupAcrtCustDao.updateOne_odb1(custuq);
				} else {

					// 生成账户限额序号
					String aclisq = IoCaSevAccountLimtPublic.genAcrtsq("aclisq");
					// 插入记录
					KupAcrtCust acrt = SysUtil.getInstance(KupAcrtCust.class);
					acrt.setCustac(input.getCustac());
					acrt.setAcrtsq(aclisq);
					acrt.setBrchno(input.getBrchno());
					acrt.setAcctrt(acctrt);
					acrt.setServtp(servtp);
					acrt.setDytllm(input.getDllm06());
					acrt.setLimttp(E_LIMTTP.DM);
					acrt.setSinglm(input.getSglm06());
//					acrt.setAccttp(input.getAccttp());
					KupAcrtCustDao.insert(acrt);
				}
			}
		}
	}

	@Override
	public void qryCmqttp(
			String limttp,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoQryCmqttp.Output output) {
		if(CommUtil.isNull(limttp)){
			throw QtError.Custa.BNASE117();
		}
		List<KupCurtLimt> lst = KupCurtLimtDao.selectAll_odb2(limttp, false);
		for(KupCurtLimt limt : lst){
			output.getCmqttp().add(limt.getCmqttp());
		}
		
	}

	@Override
	public void qryCmsvtp(
			String servtp,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoQryCmsvtp.Output output) {
		if(CommUtil.isNull(servtp)){
			throw QtError.Custa.BNASE172();
		}
		List<KupCurtServ> lst = KupCurtServDao.selectAll_odb3(servtp, false);
		for(KupCurtServ serv : lst){
			output.getCmsvtp().add(serv.getCmsvtp());
		}
		
	}
}

