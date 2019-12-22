package cn.sunline.ltts.busi.catran.batchtran;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.namedsql.CaBatchTransDao;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbProm;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.dp.tables.TabDpPassword.DpbPswd;
import cn.sunline.ltts.busi.dp.tables.TabDpPassword.DpbPswdDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppb;
import cn.sunline.ltts.busi.iobus.type.IoInTable.IoGlKnpBusi;
import cn.sunline.ltts.busi.iobus.type.cf.IoCuTable.IoCifCustAccs;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ClsAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbClac;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PSDWST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STATUS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PROMST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PROMSV;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSTAT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRTRTP;
import cn.sunline.ltts.busi.sys.type.WaEnumType.E_CLOSTX;
import cn.sunline.ltts.busi.sys.type.WaEnumType.E_RELTST;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;

/**
 * 杂项处理
 * 
 */

public class miscelDataProcessor
		extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.catran.batchtran.intf.Miscel.Input, cn.sunline.ltts.busi.catran.batchtran.intf.Miscel.Property> {

	private static final BizLog bizlog = BizLogUtil
			.getBizLog(miscelDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(
			cn.sunline.ltts.busi.catran.batchtran.intf.Miscel.Input input,
			cn.sunline.ltts.busi.catran.batchtran.intf.Miscel.Property property) {

		bizlog.debug("交易开始当前交易日期：[" + CommTools.getBaseRunEnvs().getTrxn_date() + "]");

		final String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期
		
		final String timetm =DateTools2.getCurrentTimestamp();
		
		final String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		
		// 1.电子账户升级申请定期清理垃圾数据
		bizlog.info("当前正在进行电子账户升级申请定期清理垃圾数据");
		// 获取电子账户升级申请作废控制参数
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("AccUpg", "%", "%", "%", true);
		
		String beforDate = DateTools2.dateAdd (0 - ConvertUtil.toInteger(tblKnpParameter.getParm_value1()), trandt);

		//升级渠道
		E_PROMSV promsv = E_PROMSV.INCASE;

		//升级渠道
		E_PROMST promst = E_PROMST.APPLY;
		
		List<KnbProm> tblKnbPromList = CaBatchTransDao.selKnbPromCleanInfo(beforDate, promsv, promst, false);
		
		if(CommUtil.isNotNull(tblKnbPromList)){
			
			for(KnbProm knbProm : tblKnbPromList){
				
				CaBatchTransDao.updKnbPromStatus(E_PROMST.CANCEL,  knbProm.getCustac(), knbProm.getPromsv(), knbProm.getPromst());// 更新升级状态为作废
			}
			
		}
		
	
		//2.亲情钱包未确认数据清理//亲情钱包状态
		bizlog.info("当前正在进行亲情钱包未确认数据清理");
		E_RELTST reltst = E_RELTST.WAITING;
		
		KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("ClosWa.days", "%", "%", "%", true);
		int days = ConvertUtil.toInteger(tblKnaPara.getParm_value1());// 亲情钱包未确认需关闭天数
		String sDate = DateTools2.covDateToString(DateTools2.addDays(DateTools2.covStringToDate(trandt), -days));
		//关闭原因
		E_CLOSTX clostx = E_CLOSTX.UNCONFIRMED;
		
//		IoWaSrvWalletAccountType waType = SysUtil.getInstance(IoWaSrvWalletAccountType.class);
		
//		waType.ioWaCloseWa(sDate, reltst, clostx);
		
		//3.未复核利率记录批处理
		bizlog.info("当前正在进行未复核利率记录批处理");
		//IoSrvPbInterestRate pbStaPublic = SysUtil.getInstance(IoSrvPbInterestRate.class);
		IoSrvPbInterestRate pbStaPublic = SysUtil.getRemoteInstance(IoSrvPbInterestRate.class);
		pbStaPublic.deleteKupRfirInfo();
		
		//4.产品自动启用，停用
		//4.1 产品自动启用
		
		//4.1.1 装配工厂产品启用
		bizlog.info("当前正在进行装配工厂产品启用");
		List<IoDpKupDppb> dpKupDppbList = CaBatchTransDao.selKupDppbForEfctdt(CommTools.getBaseRunEnvs().getTrxn_date(), false);
		
		if(CommUtil.isNotNull(dpKupDppbList)){
			
			for(IoDpKupDppb kupDppb : dpKupDppbList){
				
				CaBatchTransDao.updKupDppbProdst(E_PRODST.NORMAL, DateTools2.getCurrentTimestamp(), kupDppb.getCorpno(), kupDppb.getProdcd());
				// 产品操作柜员登记
				SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.DEPO, kupDppb.getProdcd(), E_PRTRTP.STAR);
			}
		}
		
		//4.1.2 内部户产品启用
		bizlog.info("当前正在进行内部户产品启用");
		List<IoGlKnpBusi> glKnpBusiList = CaBatchTransDao.selGlKnpBusiForBgdate(CommTools.getBaseRunEnvs().getTrxn_date(), false);
		
		if(CommUtil.isNotNull(glKnpBusiList)){
			
			for(IoGlKnpBusi glKnpBusi : glKnpBusiList){
				
				CaBatchTransDao.updGlKnpBusiBusist(E_PRODST.NORMAL, glKnpBusi.getBusino(),glKnpBusi.getCorpno());
				// 产品操作柜员登记
				SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.INNE, glKnpBusi.getBusino(), E_PRTRTP.STAR);
				
			}
		}
		
		
		//4.2产品自动停用
		//4.2.1  装配工厂产品停用
		bizlog.info("当前正在进行装配工厂产品停用");
		List<IoDpKupDppb> dpKupDppbList2 = CaBatchTransDao.selKupDppbForInefdt(CommTools.getBaseRunEnvs().getTrxn_date(), E_PRODST.NORMAL, false);
		
		if(CommUtil.isNotNull(dpKupDppbList2)){
			
			for(IoDpKupDppb kupDppb : dpKupDppbList2){
				
				CaBatchTransDao.updKupDppbProdst(E_PRODST.DISA, DateTools2.getCurrentTimestamp(), kupDppb.getCorpno(), kupDppb.getProdcd());
				// 产品操作柜员登记
				SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.DEPO, kupDppb.getProdcd(), E_PRTRTP.STOP);
			}
		}
		
		//4.2.2 内部户产品停用
		bizlog.info("当前正在进行内部户产品停用");
		List<IoGlKnpBusi> glKnpBusiList2 = CaBatchTransDao.selGlKnpBusiForEddate(CommTools.getBaseRunEnvs().getTrxn_date(), E_PRODST.NORMAL, false);
		
		if(CommUtil.isNotNull(glKnpBusiList2)){
			
			for(IoGlKnpBusi glKnpBusi : glKnpBusiList2){
				
				CaBatchTransDao.updGlKnpBusiBusist(E_PRODST.DISA,glKnpBusi.getBusino(),glKnpBusi.getCorpno());
				// 产品操作柜员登记
				SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.INNE, glKnpBusi.getBusino(), E_PRTRTP.STOP);
			}
		}
		
		//5预销户到期后自动销户
		bizlog.info("当前正在预销户账户销户处理");
		final KnpParameter tblKnpParameter2 = KnpParameterDao.selectOne_odb1("CATRIN", "closac", "%", "%", false);
		if(CommUtil.isNull(tblKnpParameter2)){
			throw CaError.Eacct.E0001("未配置预销户转销户间隔时间参数");
		}
		
		//从客户信息表中查询状态为结清的电子账户
		Params params = new Params();
		String namedSqlId = "";// 查询数据集的命名sql
		namedSqlId = CaDao.namedsql_selknacustbyaccst;
		params.add("acctst", E_ACCTST.SETTLE);
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		
		try{
			DaoUtil.selectList(namedSqlId, params, new CursorHandler<KnaCust>() {

				@Override
				public boolean handle(int arg0, KnaCust arg1) {
					
//					CommTools.getBaseRunEnvs().setBusi_org_id(arg1.getCorpno());
					
					String custac = arg1.getCustac();//电子账号
					String custna = arg1.getCustna();//客户名称
					String custno = arg1.getCustno();//客户号
					String cardno = "";//卡号
					String idtfno = "";//证件号码
					E_IDTFTP idtftp = null;//证件类型
					
					String closdt = arg1.getClosdt();//预销户日期
					String clacdt = DateTools2.calDateByTerm(closdt, tblKnpParameter2.getParm_value1());//应销户日期
					
					bizlog.debug("**********clacdt" + clacdt + "*************");
					bizlog.debug("**********trandt" + trandt + "*************");
			
					
					//预销户账号是否到期限
					if(CommUtil.compare(trandt, clacdt) >= 0){
						
						/**
						 * 与行方 王斌，叶群讨论，考虑到 先做一笔往账，在做销户往账时，第一笔往账退回后由于销户往账资金在途不能继续做销户
						 * 现决定在杂项处理中将销户登记簿的处理提前至账户状态修改之前
						 */
						CaDao.updateknbclacstatu(E_CLSTAT.SUCC, trandt, timetm, custac, E_CLSTAT.TRSC);
						
						//检查账户余额是否为0
						BigDecimal onlbil = CaDao.selknaAcctOnbill(custac, false);
						bizlog.debug("**********onlbil" + onlbil + "*************");
						if(CommUtil.isNotNull(onlbil) && CommUtil.compare(BigDecimal.ZERO, onlbil) != 0){
							//若账户余额不为0则直接返回
							return true;
							
						}
						//获取电子账户类型
						IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
						E_ACCATP accatp = cagen.qryAccatpByCustac(custac);
						
						//新增电子账户状态字检查  2017/02/28  songkl
						//查询电子账户冻结状态 查询冻结主体登记簿
						IoDpAcStatusWord froz = SysUtil.getInstance(IoDpFrozSvcType.class)
								.getAcStatusWord(custac);
						if (froz.getBrfroz() == E_YES___.YES || froz.getDbfroz() == E_YES___.YES || 
								froz.getPtfroz() == E_YES___.YES || froz.getBkalsp() == E_YES___.YES ||
								froz.getPtstop() == BaseEnumType.E_YES___.YES || froz.getClstop() == E_YES___.YES ||
								froz.getCertdp() == E_YES___.YES || froz.getPledge() == E_YES___.YES || froz.getOtalsp() == E_YES___.YES) {
								
							return true;
						}
						
						
						//客户信息表用于获取证件类型和证件号码
//						IoSrvCfPerson.IoGetCifCust.InputSetter queryCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//						queryCust.setCustno(custno);
//						IoSrvCfPerson.IoGetCifCust.Output tblcifcust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//						SysUtil.getInstance(IoSrvCfPerson.class).getCifCust(queryCust, tblcifcust);
						
						//获取证件类型和证件号码
//						if(CommUtil.isNotNull(tblcifcust)){
//							idtfno = tblcifcust.getIdtfno();
//							idtftp = tblcifcust.getIdtftp();
//						}
						
						//获取卡号
						KnaAcdc tblknaAcdc = CaDao.selKnaAcdcByCustac(custac, false);
						if(CommUtil.isNotNull(tblknaAcdc)){
							cardno = tblknaAcdc.getCardno();
						}
						
						//实例化注销电子账户服务输入接口
						ClsAcctIn entity = SysUtil.getInstance(ClsAcctIn.class);
						entity.setCardno(cardno);
						entity.setCustac(custac);
						entity.setCustna(custna);
						entity.setIdtfno(idtfno);
						entity.setIdtftp(idtftp);
						entity.setCustno(custno);
						// 查询出用户ID
						//IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_accsByCustno(cust.getCustno(), true, E_STATUS.NORMAL);
//						IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(IoSrvCfPerson.class).getCifCustAccsByCustno(custno, E_STATUS.NORMAL, true);
						
						//调用注销电子账户服务
						SysUtil.getInstance(DpAcctSvcType.class).acctStatusUpd(entity);
						
						
						IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
						cplDimeInfo.setCustac(custac);
						
						SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);
						/**
						 * 与行方 王斌，叶群讨论，考虑到 先做一笔往账，在做销户往账时，第一笔往账退回后由于销户往账资金在途不能继续做销户
						 * 现决定在杂项处理中将销户登记簿的处理提前至账户状态修改之前
						 * CaDao.updateknbclacstatu(E_CLSTAT.SUCC, trandt, timetm, trandt, custac, E_CLSTAT.TRSC);
						 */
						//修改销户登记簿状态
						
						// 查询销户登记簿
						IoCaKnbClac cplKnbClac = SysUtil.getInstance(IoCaKnbClac.class);
						List<IoCaKnbClac> lstKnbClac = CaDao.selKnbclacByCus(custac, false);
						if (CommUtil.isNotNull(lstKnbClac) && lstKnbClac.size() != 0) {
							cplKnbClac = lstKnbClac.get(0);
						}
						
						//修改销户cmq通知  modify lull
//						MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//						mri.setMtopic("Q0101004");
//						IoCaCloseAcctSendMsg closeSendMsgInput = SysUtil.getInstance(IoCaCloseAcctSendMsg.class);
//						closeSendMsgInput.setCustid(cplCifCustAccs.getCustid()); // 用户ID
//						closeSendMsgInput.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());// 操作机构
//						closeSendMsgInput.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());// 操作柜员
//						closeSendMsgInput.setClossv(CommTools.getBaseRunEnvs().getChannel_id());// 销户渠道
//						if (cplKnbClac.getDrawwy() == E_CLSDTP.MGD) {
//							closeSendMsgInput.setClosfg(E_YES___.YES);// 是否挂失销户标志
//						} else {
//							closeSendMsgInput.setClosfg(E_YES___.NO);// 是否挂失销户标志
//						}
//
//						mri.setMsgtyp("ApSmsType.IoCaCloseAcctSendMsg");
//						mri.setMsgobj(closeSendMsgInput); 
//						AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
						/*KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("CLOSAC", "CUSTSM",
								"%", "%", true);
						
						String bdid = tblKnaPara.getParm_value1();// 服务绑定ID
						
						IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind(
								IoCaOtherService.class, bdid);
						
						// 1.销户成功发送销户结果到客户信息
						String mssdid = CommTools.getMySysId();// 消息ID
						String mesdna = tblKnaPara.getParm_value2();// 媒介名称

						IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter closeSendMsgInput = SysUtil.getInstance(IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter.class);
						
						closeSendMsgInput.setMsgid(mssdid); // 发送消息ID
						closeSendMsgInput.setMdname(mesdna); // 媒介名称
						closeSendMsgInput.setCustid(custno); // 用户ID
						closeSendMsgInput.setClosbr(cplKnbClac.getClosbr());// 操作机构
						closeSendMsgInput.setClosus(cplKnbClac.getClosus());// 操作柜员
						closeSendMsgInput.setClossv(cplKnbClac.getClossv());// 销户渠道
						if (cplKnbClac.getDrawwy() == E_CLSDTP.MGD) {
							closeSendMsgInput.setClosfg(E_YES___.YES);// 是否挂失销户标志
						} else {
							closeSendMsgInput.setClosfg(E_YES___.NO);// 是否挂失销户标志
						}
						
						caOtherService.closeAcctSendMsg(closeSendMsgInput);
						
						// 2.销户成功发送销户结果到合约库
						KnpParameter tblKnaPara1 = KnpParameterDao.selectOne_odb1("CLOSAC", "AGRTSM",
								"%", "%", true);
						
						String mssdid1 = CommTools.getMySysId();// 消息ID
						
						String mesdna1 = tblKnaPara1.getParm_value2();// 媒介名称

						IoCaOtherService.IoCaClAcSendContractMsg.InputSetter closeSendAgrtInput = SysUtil.getInstance(IoCaOtherService.IoCaClAcSendContractMsg.InputSetter.class);
						
						closeSendAgrtInput.setMsgid(mssdid1); // 发送消息ID
						closeSendAgrtInput.setMdname(mesdna1); // 媒介名称
						closeSendAgrtInput.setUserId(custno); // 用户ID
						closeSendAgrtInput.setAcctType(accatp);// 账户分类
						closeSendAgrtInput.setOrgId(arg1.getBrchno());// 归属机构
						closeSendAgrtInput.setAcctNo(cardno);// 电子账号
						closeSendAgrtInput.setAcctStat(E_CUACST.CLOSED);// 客户化状态
						closeSendAgrtInput.setAcctName(custna);// 户名
						closeSendAgrtInput.setCertNo(idtfno);// 证件号码
						closeSendAgrtInput.setCertType(idtftp);// 证件类型
						closeSendAgrtInput.setTransTime(DateTools2.getCurrentTimestamp());//操作时间

						caOtherService.clAcSendContractMsg(closeSendAgrtInput);*/
						
//						CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
						
					}
					return true;
				}
			});
		}catch(Exception e){
			bizlog.debug("转销户错误失败："+ e.getLocalizedMessage());
			throw CaError.Eacct.E0001(e.getLocalizedMessage());
		}
		
		//批量密码解锁
		List<DpbPswd> dppbpwds = DpDayEndDao.selAllDpbPwdErrors(0, E_PSDWST.LOSS, corpno, false);
		if(CommUtil.isNotNull(dppbpwds)){
			for(DpbPswd  pwd:dppbpwds){
				pwd.setPwerct(0);
				pwd.setPsdwst(E_PSDWST.NORMAL);
				DpbPswdDao.updateOne_odb1(pwd);
			}
		}
		
	}
	
}

