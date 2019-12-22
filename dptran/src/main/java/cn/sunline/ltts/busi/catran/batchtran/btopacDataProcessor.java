package cn.sunline.ltts.busi.catran.batchtran;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.namedsql.CaBatchTransDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpbt;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpbtDao;
import cn.sunline.ltts.busi.catran.batchtran.intf.Btopac.Input;
import cn.sunline.ltts.busi.catran.batchtran.intf.Btopac.Property;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.AddAcct;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbSmsSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.AddSubAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEARelaIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEAccountIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEAccountOut;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubSqrd;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CUSTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRSNPR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SEXTYP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_WORKTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_OPACWY;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_OPENRT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;

	 /**
	  * 电子账户批量开户处理
	  *
	  */

public class btopacDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.catran.batchtran.intf.Btopac.Input, cn.sunline.ltts.busi.catran.batchtran.intf.Btopac.Property, cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpbt> {
	  
	  private static BizLog bizLog = BizLogUtil.getBizLog(btopacDataProcessor.class);
	  private static kapb_wjplxxb tblkapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	  
	  /**
		 * 批次数据项处理逻辑。
		 * 
		 * @param job 批次作业ID
		 * @param index  批次作业第几笔数据(从1开始)
		 * @param dataItem 批次数据项
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 */
		@Override
		public void process(String jobId, int index, cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpbt dataItem, cn.sunline.ltts.busi.catran.batchtran.intf.Btopac.Input input, cn.sunline.ltts.busi.catran.batchtran.intf.Btopac.Property property) {
			
			bizLog.method("btopac process begin========================>>");
			
			// 每笔处理交易流水重置
			//CommTools.genTransq();
			 MsSystemSeq.getTrxnSeq();
			
			//根据机构号获取法人，放到公共区
			IoSrvPbBranch brchSvcType = SysUtil.getInstance(IoSrvPbBranch.class);
			
			IoBrchInfo brchInfo = brchSvcType.getBranch(dataItem.getBrchno());
			
			String corpno = brchInfo.getCorpno();
			
			if (CommUtil.isNotNull(dataItem.getBrchno())) {
				CommTools.getBaseRunEnvs().setTrxn_branch(dataItem.getBrchno());
			}
			if (CommUtil.isNotNull(corpno)) {
//				CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
			}
			if (CommUtil.isNotNull(dataItem.getTranus())) {
				CommTools.getBaseRunEnvs().setTrxn_teller(dataItem.getTranus());
			}

			String custna = dataItem.getCustna(); //客户名称
			String idtftp = dataItem.getIdtftp(); //证件类型
			String idtfno = dataItem.getIdtfno(); //证件号码
			String custid = dataItem.getCustid(); //用户ID
			String custcd = dataItem.getCustcd(); //客户内码
			E_YES___ agisln = dataItem.getAgisln();
			String agstdt = dataItem.getAgstdt();
			String agendt = dataItem.getAgendt();
			E_WORKTP worktp = dataItem.getWorktp();
			String cutycd = dataItem.getCutycd();
			E_SEXTYP sextyp = dataItem.getSextyp();
			String addres = dataItem.getAddres();
			E_PRSNPR prsnpr = dataItem.getPrsnpr();
			String worktx = dataItem.getWorktx();
			E_CUSTTP custtp = E_CUSTTP.PERSON;
			E_IDTFTP idtftp1 =null;
			
			//如果交易正常，更新批量开户登记簿为成功，如果交易抛出异常，则更新批量开户登记簿为失败
			try{
				// 客户姓名不能为空
				if (CommUtil.isNull(custna)) {
					throw DpModuleError.DpstComm.BNAS0527();
				}
				// 证件类型不能为空
				if (CommUtil.isNull(idtftp)) {
					throw DpModuleError.DpstAcct.BNAS1036();
				}
				// 证件号码不能为空
				if (CommUtil.isNull(idtfno)) {
					throw DpModuleError.DpstComm.BNAS0157();
				}
				// 用户ID不能为空
				if (CommUtil.isNull(custid)) {
					throw CaError.Eacct.BNAS0241();
				}
				// 客户内码不能为空
				if (CommUtil.isNull(custcd)) {
					throw DpModuleError.DpstAcct.BNAS1737();
				}
				
				//暂时不支持开立Ⅰ类户
				if(E_ACCATP.GLOBAL == dataItem.getAccttp()){
					throw DpModuleError.DpstAcct.BNAS1738();
				}
				
				idtftp1 = CommUtil.toEnum(E_IDTFTP.class, idtftp);
			
				/* 1、调用新增客户信息服务  */
//				IoCuCustSvcType ioCuCustSvcType = SysUtil.getInstance(IoCuCustSvcType.class);
//				String custno  = ioCuCustSvcType.addCust(custna, idtftp1, idtfno, null, custid, custcd, custtp, agisln, agstdt,agendt, worktp, cutycd, sextyp, addres, prsnpr, worktx);
				
				/* 2、调用开立电子账户服务 */
				IoCaSrvGenEAccountInfo ioCaGenEAccount = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
				
				//设置开立电子账号输入参数
				IoCaAddEAccountIn cplAddEAccountIn  =  SysUtil.getInstance(IoCaAddEAccountIn.class);
				cplAddEAccountIn.setAccttp(dataItem.getAccttp()); //账户分类
				cplAddEAccountIn.setCustna(dataItem.getCustna()); //客户名称
				cplAddEAccountIn.setBrchno(dataItem.getBrchno()); //所属机构
//				cplAddEAccountIn.setCustno(custno); //客户号
				cplAddEAccountIn.setIdckrt(dataItem.getIdckrt()); //身份联网核查结果
				cplAddEAccountIn.setTlphno(dataItem.getTeleno()); //联系手机号
				cplAddEAccountIn.setCrcycd(dataItem.getCrcycd()); //币种
				cplAddEAccountIn.setCsextg(E_CSEXTG.CASH); //钞汇标志
				cplAddEAccountIn.setIsopcd(E_YES___.YES); //是否开卡标志
				cplAddEAccountIn.setOpacwy(E_OPACWY.BATCH); //批量开户
				cplAddEAccountIn.setCustno(dataItem.getCustid()); //客户ID
				cplAddEAccountIn.setServtp(dataItem.getServtp());//交易渠道
				
				//调用服务
				IoCaAddEAccountOut cplAddEAccountOut = ioCaGenEAccount.prcAddEAccount(cplAddEAccountIn);
				
				
				/* 如果是开立的Ⅲ类电子账户，则只开立Ⅲ类的钱包账户，如果开立的是Ⅰ、Ⅱ电子账户，则同时开立 Ⅰ或Ⅱ结算户跟Ⅲ的钱包账户*/
				DpAcctSvcType cplDpAcct = SysUtil.getInstance(DpAcctSvcType.class);
				
				//查询开立Ⅰ、Ⅱ、Ⅲ类电子账户对应的存款产品编号
				KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("ACCATP", dataItem.getAccttp().getId(), "%", "%", true);
				
				//判断开立电子账户的账户分类
				if(E_ACCATP.GLOBAL == dataItem.getAccttp() || E_ACCATP.FINANCE == dataItem.getAccttp()){
					
					/* 3、调用开立结算负债账户服务 */
					//开立个人结算账户
					//设置输入参数
					AddAcct.InputSetter cplAcctIn = SysUtil.getInstance(AddAcct.InputSetter.class);
					cplAcctIn.setCustac(cplAddEAccountOut.getCustac()); //电子账号
					cplAcctIn.setAcctna(dataItem.getCustna()); //客户名称
					cplAcctIn.setCrcycd(dataItem.getCrcycd()); //币种
//					cplAcctIn.setCustno(custno); // 客户号
					cplAcctIn.setDepttm(E_TERMCD.T000); //存期
					cplAcctIn.setOpacfg(E_YES___.YES); //首开户标志
					cplAcctIn.setProdcd(tblKnpParameter.getParm_value1()); //产品编号
					cplAcctIn.setCusttp(E_CUSTTP.PERSON); //客户类型
					cplAcctIn.setCacttp(E_CUSACT.ACC); //客户账号类型
					
					//调用服务
					AddSubAcctOut cplAddAcctOut = cplDpAcct.addAcct(cplAcctIn);
					
					/* 4、调用电子账户与结算子账户关联服务  */
					//设置输入参数
					IoCaAddEARelaIn cplAddEARelaIn = SysUtil.getInstance(IoCaAddEARelaIn.class);
					cplAddEARelaIn.setCustac(cplAddEAccountOut.getCustac()); //电子账号
					cplAddEARelaIn.setAcctno(cplAddAcctOut.getAcctno()); //子账号，负债账号
					cplAddEARelaIn.setCrcycd(dataItem.getCrcycd()); //币种
					cplAddEARelaIn.setFcflag(cplAddAcctOut.getPddpfg()); //定活标志
					cplAddEARelaIn.setProdcd(cplAddAcctOut.getProdcd()); //产品编号
					cplAddEARelaIn.setProdtp(E_PRODTP.DEPO); //产品类型
					
					//调用服务
					ioCaGenEAccount.prcAddEARela(cplAddEARelaIn);
				}
				
				/* 5、调用开立钱包子账户服务 */
				//开立钱包账户
				//设置输入参数
				AddAcct.InputSetter cplWalletIn = SysUtil.getInstance(AddAcct.InputSetter.class);
				cplWalletIn.setCustac(cplAddEAccountOut.getCustac()); //电子账号
				cplWalletIn.setAcctna(dataItem.getCustna()); //客户名称
				cplWalletIn.setCrcycd(dataItem.getCrcycd()); //币种
//				cplWalletIn.setCustno(custno); // 客户号
				cplWalletIn.setDepttm(E_TERMCD.T000); //存期
				cplWalletIn.setOpacfg(E_YES___.YES); //首开户标志
				cplWalletIn.setProdcd(tblKnpParameter.getParm_value2()); //产品编号
				cplWalletIn.setCusttp(E_CUSTTP.PERSON); //客户类型
				cplWalletIn.setCacttp(E_CUSACT.ACC); //客户账号类型
				
				//调用服务
				AddSubAcctOut cplAddWalletOut = cplDpAcct.addAcct(cplWalletIn);
				
				/* 6、调用电子账户与结算子账户关联服务  */
				//设置输入参数
				IoCaAddEARelaIn cplWalletRelaIn = SysUtil.getInstance(IoCaAddEARelaIn.class);
				cplWalletRelaIn.setCustac(cplAddEAccountOut.getCustac()); //电子账号
				cplWalletRelaIn.setAcctno(cplAddWalletOut.getAcctno()); //子账号，负债账号
				cplWalletRelaIn.setCrcycd(dataItem.getCrcycd()); //币种
				cplWalletRelaIn.setFcflag(cplAddWalletOut.getPddpfg()); //定活标志
				cplWalletRelaIn.setProdcd(tblKnpParameter.getParm_value2()); //产品编号
				cplWalletRelaIn.setProdtp(E_PRODTP.DEPO); //产品类型
				
				//调用服务
				ioCaGenEAccount.prcAddEARela(cplWalletRelaIn);
				
				//更新批量开户登记簿，更新为成功
				dataItem.setCustac(cplAddEAccountOut.getCustac()); //电子账号
				dataItem.setOpenrt(E_OPENRT.ec); //开户结果
				
				// 短信流水登记
				IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
	    		cplKubSqrd.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());// 内部交易码
	    		cplKubSqrd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
	    		cplKubSqrd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());// 交易时间
	    		cplKubSqrd.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
	    		cplKubSqrd.setTmstmp(DateTools2.getCurrentTimestamp());// 时间戳
	    		// 调用短信流水登记服务
	    		SysUtil.getInstance(IoPbSmsSvcType.class).pbTransqReg(cplKubSqrd);
				
				bizLog.method("btopac process end<<========================");
				
			} catch(Exception e){
				
				bizLog.debug("证件类型为"+dataItem.getIdtftp()+",证件号码为"+dataItem.getIdtfno()+"的开户失败");
				
				
				//mod by songkl 20161201
				//修改登记的错误信息，去除错误码和中括号
				String mes = e.getLocalizedMessage();
				int i = mes.indexOf("]");
				if(++i < mes.length()){
					mes = mes.substring(i);
				}
				if(mes.charAt(0) == '['){
					mes = mes.substring(1, mes.length() - 1);
				}
				
				//更新批量开户登记簿，更新为成功
				dataItem.setOpenrt(E_OPENRT.ef); //开户结果
				dataItem.setLosern(mes); //失败原因
				
				throw CaError.Eacct.E0001(e.getLocalizedMessage());
			}finally{
				KnbOpbtDao.updateOne_odb1(dataItem);
			}
			
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpbt> getBatchDataWalker(cn.sunline.ltts.busi.catran.batchtran.intf.Btopac.Input input, cn.sunline.ltts.busi.catran.batchtran.intf.Btopac.Property property) {

			bizLog.method("getBatchDataWalker begin========================>>");
			
			//设置参数
			Params parm = new Params();
			parm.put("btchno", tblkapbWjplxxb.getBtchno()); //当期批次号
			parm.put("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
			parm.put("acctdt", tblkapbWjplxxb.getAcctdt()); //批次日期
			
			bizLog.method("getBatchDataWalker end<<========================");
			
			return new CursorBatchDataWalker<KnbOpbt>(CaBatchTransDao.namedsql_selKnbOpbtNormalByBtchno, parm);
			
		}
		
		/**
		 * 
		 * @param taskId
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 */
		@Override
		public void beforeTranProcess(String taskId, Input input, Property property) {
			
			bizLog.method("beforeTranProcess begin========================>>");
			
			tblkapbWjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
			
			bizLog.method("beforeTranProcess end<<========================");
			
			super.beforeTranProcess(taskId, input, property);
		}
		
		@Override
		public void jobExceptionProcess(String taskId, Input input,
				Property property, String jobId, KnbOpbt dataItem, Throwable t) {
			
			final KnbOpbt date = dataItem;
			
			DaoUtil.executeInNewTransation(new RunnableWithReturn<KnbOpbt>() {
				
				@Override
				public KnbOpbt execute() {
				
					//更新批量开户登记簿，更新为成功
					date.setOpenrt(E_OPENRT.ef); //开户结果
					date.setLosern(date.getLosern()); //失败原因
					
					KnbOpbtDao.updateOne_odb1(date);
					
					return null;
				}
			});
				
			}
		
		/**
		 * 
		 * @param taskId
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 */
		@Override
		public void afterTranProcess(String taskId, Input input, Property property) {
			
			bizLog.method("afterTranProcess begin=========================>>");
			
			tblkapbWjplxxb.setBtfest(E_BTFEST.SUCC); //批量文件状态
			
			Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
			
			bizLog.method("afterTranProcess end<<=========================");
			
			super.afterTranProcess(taskId, input, property);
		}

		@Override
		public void tranExceptionProcess(String taskId, Input input,
				Property property, Throwable t) {
			
			
			DaoUtil.executeInNewTransation(new RunnableWithReturn<kapb_wjplxxb>() {
				@Override
				public kapb_wjplxxb execute() {
					tblkapbWjplxxb.setBtfest(E_BTFEST.FAIL);
					Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
					
					
					return null;
				}
			});
			
		}
}


