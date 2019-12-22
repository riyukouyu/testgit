package cn.sunline.ltts.busi.dptran.batchtran;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessorWithJobDataItem;
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
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.KqBatchDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbKqjzBach;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbKqjzBachDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctAddt;
import cn.sunline.ltts.busi.dptran.batchtran.intf.Markkq.Input;
import cn.sunline.ltts.busi.dptran.batchtran.intf.Markkq.Property;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevGenBindCard;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInOpenClose;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCacd;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RECPAY;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_KQSRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_KQTRTP;

	 /**
	  * 卡券批量记账
	  *
	  */

public class markkqDataProcessor extends
  AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.dptran.batchtran.intf.Markkq.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Markkq.Property, String, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbKqjzBach> {
	 
	private static BizLog log = BizLogUtil.getBizLog(markkqDataProcessor.class);
	private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	
	@Override
	public void beforeTranProcess(String taskId, Input input, Property property) {
		log.debug("<<===================卡券批量记账交易前更新处理状态======================>>");
		filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		property.setSourdt(filetab.getAcctdt());
		super.beforeTranProcess(taskId, input, property);
	}
	
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbKqjzBach dataItem, cn.sunline.ltts.busi.dptran.batchtran.intf.Markkq.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Markkq.Property property) {
			log.debug("<<===================卡券批量记账交易处理开始======================>>");
			
			log.debug("<<=================客户账号信息：贷方========================>>");
			log.debug("贷方卡号：" + dataItem.getCrcard());
			log.debug("贷方账号：" + dataItem.getCrdact());
			log.debug("贷方子账号：" + dataItem.getCracct());
			log.debug("<<=================内部户账号信息：借方======================>>");
			log.debug("借方卡号：" + dataItem.getDecard());
			log.debug("借方账号：" + dataItem.getDebact());
			log.debug("借方子账号：" + dataItem.getDeacct());
			log.debug("<<==========================================================>>");
            log.debug("上送系统交易日期："+CommTools.getBaseRunEnvs().getInitiator_date());
			//CommTools.genNewSerail(dataItem.getSourdt());
            MsSystemSeq.getTrxnSeq();
			CommTools.getBaseRunEnvs().setBusi_seq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //设置业务流水
			log.debug("上送系统交易日期："+CommTools.getBaseRunEnvs().getInitiator_date());
			//记账：内部户：借        客户账：贷
			IaAcdrInfo acdr = SysUtil.getInstance(IaAcdrInfo.class); // 内部户记账服务接口输入参数
			IoInAccount insvc = CommTools.getRemoteInstance(IoInAccount.class); // 内部户记账服务接口
			IaTransOutPro inout = SysUtil.getInstance(IaTransOutPro.class); // 内部户记账服务返回结果集
			
			DpAcctSvcType dpsvc = SysUtil.getInstance(DpAcctSvcType.class); // 客户账记账服务接口
			IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			IoCaKnaAcdc acdc = SysUtil.getInstance(IoCaKnaAcdc.class);
			KnaAcct acct = SysUtil.getInstance(KnaAcct.class);
			IoCaKnaCust cust = SysUtil.getInstance(IoCaKnaCust.class);
			E_KQTRTP bathtp = dataItem.getKqtrtp();//卡券交易类型
			E_KQSRTP rptype = dataItem.getKqtype();//卡券类型
			SaveDpAcctIn save = SysUtil.getInstance(SaveDpAcctIn.class); // 负债账户存入参数

			dataItem.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			dataItem.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
			
			if (bathtp == E_KQTRTP.YOUHJIES) { // 卡券优惠结算
				KnpParameter para = KnpParameterDao.selectOne_odb1("KQDEPS", "%", "%", "%", true);
				acdr.setAcbrch(dataItem.getDeborg()); // 借方机构
				acdr.setAmntcd(E_AMNTCD.DR); // 借贷标志：借方
				acdr.setBusino(para.getParm_value1()); // 业务代码
				acdr.setCrcycd(dataItem.getCrcycd()); // 币种
				acdr.setSmrycd(dataItem.getSmrycd()); // 摘要码
				acdr.setDscrtx("优惠结算"); // 描述
				acdr.setTranam(dataItem.getTranam());
				
				acdc = ActoacDao.selKnaAcdc(dataItem.getCrcard(), false);// 改为不带法人查询
				if (CommUtil.isNull(acdc)) {
					throw DpModuleError.DpstComm.E9999("贷方卡号不存在！");
				}
				cust = caqry.getKnaCustByCustacOdb1(acdc.getCustac(), false);
				if (CommUtil.isNull(cust)) {
					throw DpModuleError.DpstComm.E9999("电子账号不存在！");
				}
				
				if(rptype == E_KQSRTP.FANLJ||rptype == E_KQSRTP.JIAXJ){//返利券，加息券
					acdr.setSubsac(para.getParm_value3());				
					IoInacInfo ioInacInfo = SysUtil.getInstance(IoInOpenClose.class).ioQueryAndOpen(para.getParm_value2(), dataItem.getDeborg(), "", dataItem.getCrcycd());
	                if(CommUtil.isNotNull(ioInacInfo)){
	                	acdr.setAcctno(ioInacInfo.getAcctno());
	    				acdr.setAcctna(ioInacInfo.getAcctna());
	                }
				}

				
//				add 20170720 xieqq 卡券优惠转入金额账户额度扣减
				log.debug("<<=====================卡券批量优惠额度扣减开始=====================>>");
				
				//初始化数据
				IoCaSevAccountLimit caSevAccountLimit = SysUtil.getInstance(IoCaSevAccountLimit.class);
				IoCaSevAccountLimit.IoAcSubQuota.InputSetter iSetter = SysUtil.getInstance(IoCaSevAccountLimit.IoAcSubQuota.InputSetter.class);
				IoCaSevAccountLimit.IoAcSubQuota.Output oPut = SysUtil.getInstance(IoCaSevAccountLimit.IoAcSubQuota.Output.class);
				
				//检查对方账号是否绑定账户
				IoCaKnaCacd cacd = SysUtil.getInstance(IoCaSevGenBindCard.class).
						selBindByCard(acdc.getCustac(), dataItem.getCrcard(), E_DPACST.NORMAL, false);
				iSetter.setCustie(CommUtil.isNotNull(cacd) ? E_YES___.YES : E_YES___.NO); //是否绑定卡标识
				iSetter.setServtp("NR"); //TODO 交易渠道 待定，营销系统
				iSetter.setServsq(dataItem.getSoursq()); //渠道来源方流水
				iSetter.setServdt(CommTools.getBaseRunEnvs().getTrxn_date()); //当前交易日期
				iSetter.setDcflag(E_RECPAY.REC); //收付方标志
				iSetter.setTranam(dataItem.getTranam()); //交易金额
				iSetter.setCustac(acdc.getCustac());
				
				caSevAccountLimit.SubAcctQuota(iSetter, oPut); //调用账户限额
				
				log.debug("<<=====================卡券批量优惠额度扣减结束=====================>>");
				
				inout = insvc.IoInAcdrAdm(acdr); //调用内部户借方记账服务 
				
				dataItem.setDebact(inout.getAcctno());
				dataItem.setDebnam(inout.getAcctna());

				acdc = ActoacDao.selKnaAcdc(dataItem.getCrcard(), false);// 改为不带法人查询
				if (CommUtil.isNull(acdc)) {
					throw DpModuleError.DpstComm.E9999("贷方卡号不存在！");
					}
				cust = caqry.getKnaCustByCustacOdb1(acdc.getCustac(), false);
				if (CommUtil.isNull(cust)) {
					throw DpModuleError.DpstComm.E9999("电子账号不存在！");
				}
				IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
				E_ACCATP accatp = cagen.qryAccatpByCustac(acdc.getCustac());

				CapitalTransCheck.ChkAcctstIN(cust.getCustac());
				CapitalTransCheck.ChkAcctFrozIN(cust.getCustac());
				if (accatp == E_ACCATP.WALLET) {
					acct = CapitalTransDeal.getSettKnaAcctSubAcLock(
							acdc.getCustac(), E_ACSETP.MA);

					if (CommUtil.isNull(acct)) {
						throw DpModuleError.DpstComm.E9999("电子账号不存在或电子账号处于此状态不能做此交易！");
					}
					KnaAcctAddt tblKnaCAddt = ActoacDao.selKnaAcctAddtByAcctno(
							acct.getAcctno(), false);
					if (CommUtil.isNull(tblKnaCAddt)) {
						throw DpModuleError.DpstComm.E9999("查负债信息附加表无对应信息！");
					}
					// 最高限额大于0才去比较金额
					if (CommUtil.compare(tblKnaCAddt.getHigham(), BigDecimal.ZERO) > 0) {
						if (CommUtil.compare(
								dataItem.getTranam().add(acct.getOnlnbl()),
								tblKnaCAddt.getHigham()) > 0) {
							throw DpModuleError.DpstComm.E9999("当前存入金额大于账户可用限额，请升级电子账户！");
						}
					}
					KnpParameter para1 = KnpParameterDao.selectOne_odb1("DpParm.maxbln", "3",
							"%", "%", false);
					if (CommUtil.isNull(para1)) {
						throw DpModuleError.DpstComm.E9999("Ⅲ类户最高限额参数未配置，请检查!");
					}
					BigDecimal bg = new BigDecimal(para1.getParm_value1());
					if (CommUtil.compare(
							dataItem.getTranam().add(acct.getOnlnbl()), bg) > 0) {
						throw DpModuleError.DpstComm.E9999("当前存入金额大于账户可用限额，请升级电子账户！");
					}
				} else {
					acct = CapitalTransDeal.getSettKnaAcctSubAcLock(
							acdc.getCustac(), E_ACSETP.SA);
				}
				save.setAcctno(acct.getAcctno());
				save.setAcseno("");
				save.setCardno(acdc.getCardno());
				save.setCrcycd(dataItem.getCrcycd());
				save.setCustac(acdc.getCustac());
				save.setOpacna(inout.getAcctna());
				save.setToacct(inout.getAcctno());
				save.setTranam(dataItem.getTranam());
				save.setSmrycd(dataItem.getSmrycd());// 摘要码
				save.setSmryds(ApSmryTools.getText(dataItem.getSmrycd()));
				BusiTools.getBusiRunEnvs().setRemark("卡券优惠");
				dpsvc.addPostAcctDp(save); // 负债账户存入记账处理
				
			}

			dataItem.setTranst(E_TRANST.SUCCESS);
			dataItem.setDescrb("卡券批量记账交易处理成功");
			KnbKqjzBachDao.updateOne_odb1(dataItem);
			log.debug("<<===================卡券批量记账交易处理结束======================>>");
			
			//交易平衡检查
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._10);
			
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<String> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.intf.Markkq.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Markkq.Property property) {
			
			log.debug("<<===================获取卡券批量记账作业编号开始======================>>");
			
//			List<String> list = KqBatchDao.selDataidByFilesq(input.getFilesq(), property.getSourdt(), true);
			 Params param = new Params();
			 param.put("filesq", input.getFilesq());
			 param.put("sourdt", property.getSourdt());
//			return new ListBatchDataWalker<String>(list);
			return new CursorBatchDataWalker<String>(KqBatchDao.namedsql_selDataidByFilesq,param);
		}
		
		/**
		 * 获取作业数据遍历器
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @param dataItem 批次数据项
		 * @return
		 */
		public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbKqjzBach> getJobBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.intf.Markkq.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Markkq.Property property, String dataItem) {
			
	        log.debug("<<===================获取作业数据集合开始======================>>");
			
			Params param = new Params();
			param.put("filesq", input.getFilesq());
			param.put("sourdt", filetab.getAcctdt());
			param.put("dataid", dataItem);
            
            log.debug("<<===================获取作业数据集合结束======================>>");
			
			return new CursorBatchDataWalker<KnbKqjzBach>(KqBatchDao.namedsql_selBatchDataByFilesq,param);
		}

		@Override
		public void tranExceptionProcess(String taskId, Input input, Property property, Throwable t) {

			DaoUtil.executeInNewTransation(new RunnableWithReturn<kapb_wjplxxb>() {
				@Override
				public kapb_wjplxxb execute() {
					filetab.setBtfest(E_BTFEST.FAIL);
					Kapb_wjplxxbDao.updateOne_odb1(filetab);
					return null;
				}
			});
			
			super.tranExceptionProcess(taskId, input, property, t);
		}

		@Override
		public void jobExceptionProcess(String taskId, Input input, Property property, String jobId,
				KnbKqjzBach dataItem, Throwable t) {
			dataItem.setTranst(E_TRANST.FAIL);		
			String descri = t.getMessage();
			int index = descri.indexOf("]");		
			if(index >= 0){					
				descri = descri.substring(index + 1);
			}
			dataItem.setDescrb(descri);
			KnbKqjzBachDao.updateOne_odb1(dataItem);
			super.jobExceptionProcess(taskId, input, property, jobId, dataItem, t);
		}


	  

}


