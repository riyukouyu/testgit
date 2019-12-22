package cn.sunline.ltts.busi.dptran.batchtran.redpck;

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
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.RpBatchTransDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBachDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctAddt;
import cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpback.Input;
import cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpback.Property;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInOpenClose;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_RPTRTP;


/**
 * 红包批量退款
 * 
 */

public class rpbackDataProcessor
		extends
		AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpback.Input, cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpback.Property, String, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach> {

	private static BizLog log = BizLogUtil.getBizLog(rpbackDataProcessor.class);
	private static kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);

	@Override
	public void beforeTranProcess(String taskId, Input input, Property property) {
		// TODO Auto-generated method stub
		log.debug("<<===================批量提现交易前更新处理状态======================>>");
		filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
		
		
		property.setSourdt(filetab.getAcctdt());
		
		// super.getTaskId();
		super.beforeTranProcess(taskId, input, property);
	}

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param job
	 *            批次作业ID
	 * @param index
	 *            批次作业第几笔数据(从1开始)
	 * @param dataItem
	 *            批次数据项
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(
			String jobId,
			int index,
			cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach dataItem,
			cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpback.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpback.Property property) {
		log.debug("<<===================批量退款交易处理开始======================>>");
		log.debug("<<=========================================>>");
		log.debug("贷方卡号：" + dataItem.getCrcard());
		log.debug("贷方账号：" + dataItem.getCrdact());
		log.debug("贷方子账号：" + dataItem.getCracct());
		log.debug("借方卡号：" + dataItem.getDecard());
		log.debug("借方账号：" + dataItem.getDebact());
		log.debug("借方子账号：" + dataItem.getDeacct());
		log.debug("<<=========================================>>");
	
		//CommTools.genNewSerail(dataItem.getSourdt());
		MsSystemSeq.getTrxnSeq();
		
		IaAcdrInfo acdr = SysUtil.getInstance(IaAcdrInfo.class); // 内部户记账参数
		
		IoInAccount insvc = CommTools.getRemoteInstance(IoInAccount.class); // 内部户记账接口
		IaTransOutPro inout = SysUtil.getInstance(IaTransOutPro.class); // 内部户记账输出
		DpAcctSvcType dpsvc = SysUtil.getInstance(DpAcctSvcType.class); // 客户账记账接口
		
		IaAcdrInfo acdr2 = SysUtil.getInstance(IaAcdrInfo.class); // 内部户贷方记账输入
		IaTransOutPro inout2 = SysUtil.getInstance(IaTransOutPro.class);//内部户贷方记账输出
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaCust cust = SysUtil.getInstance(IoCaKnaCust.class);
		IoCaKnaAcdc acdc = SysUtil.getInstance(IoCaKnaAcdc.class);
		KnaAcct acct = SysUtil.getInstance(KnaAcct.class);
		E_RPTRTP bathtp = dataItem.getRptrtp();//红包交易类型
		E_RPTRTP rptype = dataItem.getRptype();//红包类型
		SaveDpAcctIn save = SysUtil.getInstance(SaveDpAcctIn.class); // 负债账户存入参数
		DrawDpAcctIn draw = SysUtil.getInstance(DrawDpAcctIn.class);//负债账户

		dataItem.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		dataItem.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());

		if (bathtp == E_RPTRTP.BT303) { // 批量退款
			
			KnpParameter para = KnpParameterDao.selectOne_odb1("RPBACK", "%", "%", "%",
					true);
			//by update cqm 20171107
			//修改红包退款为红冲
			//D:红包营销垫款户 N985201700000017/个人存款户 C:行社发普通红包N985201700000018 0000001/0000002/0000003
			if(rptype == E_RPTRTP.SN101||rptype == E_RPTRTP.SN102){//行社机构发红包
				acdr.setAcbrch(dataItem.getDeborg()); // 借方机构
				acdr.setAmntcd(E_AMNTCD.DR); // 借贷标志
				acdr.setBusino(para.getParm_value2()); // 业务代码
				acdr.setCrcycd(dataItem.getCrcycd()); // 币种
				acdr.setSmrycd(dataItem.getSmrycd()); // 摘要码
				acdr.setDscrtx("红包退款"); // 描述
				acdr.setTranam(dataItem.getTranam().negate());//交易金额;
					IoInacInfo ioInacInfo = SysUtil.getInstance(IoInOpenClose.class).
							ioQueryAndOpen(para.getParm_value1(), dataItem.getCrdorg(), para.getParm_value3(), dataItem.getCrcycd());
	            if(CommUtil.isNotNull(ioInacInfo)){
	                acdr.setToacct(ioInacInfo.getAcctno());
	    			acdr.setToacna(ioInacInfo.getAcctna());
	            }
				
				inout = insvc.ioInAcdr(acdr); // 内部户借方记账处理

				dataItem.setDebact(inout.getAcctno());
				dataItem.setDebnam(inout.getAcctna());
			}else{//个人发红包
				acdc = ActoacDao.selKnaAcdc(dataItem.getDecard(), false);//改为不带法人查询
            	if(CommUtil.isNull(acdc)){
    				throw DpModuleError.DpstComm.E9999("借方卡号不存在！");
    			}
            	E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(acdc.getCustac());
            	if(eAccatp == E_ACCATP.WALLET){
            		acct = CapitalTransDeal.getSettKnaAcctSubAcLock(acdc.getCustac(), E_ACSETP.MA);
            	
            		if(CommUtil.isNull(acct)){
                		throw DpModuleError.DpstComm.E9999("电子账号不存在或电子账号处于此状态不能做此交易！");
                	}
                		
            		KnaAcctAddt tblKnaCAddt = ActoacDao.selKnaAcctAddtByAcctno(acct.getAcctno(), false);
	            	
	            	if(CommUtil.isNull(tblKnaCAddt)){
	            		throw DpModuleError.DpstComm.E9999("查负债信息附加表无对应信息！");
	            	}
	            	if(CommUtil.compare(tblKnaCAddt.getHigham(),BigDecimal.ZERO)>0){
	            		if(CommUtil.compare(dataItem.getTranam().add(acct.getOnlnbl()),tblKnaCAddt.getHigham()) > 0){
	    					throw DpModuleError.DpstComm.E9999("当前存入金额大于账户可用限额，请升级电子账户！");
	    				}
	            	}

					KnpParameter para1 = KnpParameterDao.selectOne_odb1("DpParm.maxbln", "3", "%", "%", false);
					
					if (CommUtil.isNull(para1)) {						
						throw DpModuleError.DpstComm.E9999("Ⅲ类户最高限额参数未配置，请检查!");
					}
					BigDecimal bg = new BigDecimal(para1.getParm_value1());
					if(CommUtil.compare(dataItem.getTranam().add(acct.getOnlnbl()),bg) > 0){
						throw DpModuleError.DpstComm.E9999("当前存入金额大于账户可用限额，请升级电子账户！");
					}
            	}else{
            		acct = CapitalTransDeal.getSettKnaAcctSubAcLock(acdc.getCustac(), E_ACSETP.SA);
            		
            	}
            	
            	if(CommUtil.isNull(acct)){
            		throw DpModuleError.DpstComm.E9999("电子账号不存在或电子账号处于此状态不能做此交易！");
            	}
            			
          						
    			draw.setAcctno(acct.getAcctno());
    			draw.setAcseno("");
    			draw.setCardno(acdc.getCardno());
    			draw.setCrcycd(dataItem.getCrcycd());
    			draw.setCustac(acdc.getCustac());
    			draw.setOpacna(inout.getAcctna());
    			draw.setToacct(inout.getAcctno());
    			draw.setTranam(dataItem.getTranam().negate());
    			draw.setSmrycd(dataItem.getSmrycd());//摘要码
    			draw.setSmryds(ApSmryTools.getText(dataItem.getSmrycd()));//摘要描述
    			BusiTools.getBusiRunEnvs().setRemark("红包退款");
    			//dpsvc.addPostAcctDp(save); // 负债账户存入记账处理
    			dpsvc.addDrawAcctDp(draw);//负债账户支取记账处理
    			
    			//add 20170222 songlw 额度扣减-红字
    			if(rptype == E_RPTRTP.SN103 || rptype == E_RPTRTP.SN104){
    				
    				//初始化
    				IoCaSevAccountLimit caSevAccountLimit = SysUtil.getInstance(IoCaSevAccountLimit.class);	
    				IoAcSubQuota.InputSetter inputSetter = SysUtil.getInstance(IoAcSubQuota.InputSetter.class);
    				IoAcSubQuota.Output output = SysUtil.getInstance(IoAcSubQuota.Output.class);
    				
    				inputSetter.setCustac(acdc.getCustac());
    				inputSetter.setServsq(CommTools.getBaseRunEnvs().getTrxn_seq()); //渠道来源流水
    				inputSetter.setServdt(dataItem.getSourdt()); //渠道来源日期
    				inputSetter.setOldate(dataItem.getOridat());
    				inputSetter.setServtp("NR");
    				inputSetter.setCustie(E_YES___.NO); //绑定关系  默认为否
    				inputSetter.setTranam(dataItem.getTranam().negate());
    				//账户额度恢复
    				caSevAccountLimit.SubAcctQuota(inputSetter, output);
    			}
			}
			acdr2.setAcbrch(dataItem.getCrdorg()); // 贷方机构
			acdr2.setBusino(para.getParm_value1()); // 业务代码
			acdr2.setSubsac(para.getParm_value3());
			acdr2.setCrcycd(dataItem.getCrcycd()); // 币种
			acdr2.setSmrycd(dataItem.getSmrycd()); // 摘要码
			acdr2.setDscrtx("红包退款"); // 描述
			acdr2.setTranam(dataItem.getTranam().negate());
			//add by wuzx - 贷方增加对方账号 - beg
			IoInacInfo ioInacInfo = SysUtil.getInstance(IoInOpenClose.class).ioQueryAndOpen(para.getParm_value2(), dataItem.getDeborg(), "", dataItem.getCrcycd());
            if(CommUtil.isNotNull(ioInacInfo)){
            	acdr2.setToacct(ioInacInfo.getAcctno());
				acdr2.setToacna(ioInacInfo.getAcctna());
            }
            //add by wuzx - 贷方增加对方账号 - end
			inout2 = insvc.ioInAccr(acdr2);//内部户贷方记账处理
			dataItem.setCrdact(inout2.getAcctno());
			dataItem.setCrdnam(inout2.getAcctna());
			
            //因分布式改造需变更红包退款流程，由红包过渡户记账改为只登记会计流水  modify by lull
            /*// 登记会计流水开始 
            IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
            cplIoAccounttingIntf.setCuacno(para.getParm_value1()); //记账账号-登记核算代码
            cplIoAccounttingIntf.setAcseno(para.getParm_value1()); //子账户序号-登记核算代码
            cplIoAccounttingIntf.setAcctno(para.getParm_value1()); //负债账号-登记核算代码
            cplIoAccounttingIntf.setProdcd(para.getParm_value1()); //产品编号-登记核算代码
            cplIoAccounttingIntf.setDtitcd(para.getParm_value1()); //核算口径-登记核算代码
            cplIoAccounttingIntf.setCrcycd(dataItem.getCrcycd()); //币种                 
            cplIoAccounttingIntf.setTranam(dataItem.getTranam().negate()); //交易金额 
            cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
            cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //主交易流水
            cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期 
            cplIoAccounttingIntf.setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch()); //账务机构
            cplIoAccounttingIntf.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
            cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR); //借贷标志

            cplIoAccounttingIntf.setAtowtp(E_ATOWTP.IN); //会计主体类型-内部资金户
            cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型-账务流水
            cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE); //余额属性-本金科目
            cplIoAccounttingIntf.setTranms("1020000");//交易信息
            cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
            IoInacInfo ioInacInfo = SysUtil.getInstance(IoInOpenClose.class).ioQueryAndOpen(para.getParm_value2(), dataItem.getDeborg(), "", dataItem.getCrcycd());
            if(CommUtil.isNotNull(ioInacInfo)){
	            cplIoAccounttingIntf.setToacct(ioInacInfo.getAcctno());
	            cplIoAccounttingIntf.setToacna(ioInacInfo.getAcctna());
            }
            //登记会计流水
            SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);*/
            
		}
    
		dataItem.setTranst(E_TRANST.SUCCESS);
		dataItem.setDescrb("成功完成");

		KnbRptrBachDao.updateOne_odb1(dataItem);
		//交易平衡检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._10);

		log.debug("<<===================批量退款交易处理结束======================>>");
	}

	@Override
	public void jobExceptionProcess(String taskId, Input input,
			Property property, String jobId, KnbRptrBach dataItem, Throwable t) {
		// TODO Auto-generated method stub

		dataItem.setTranst(E_TRANST.FAIL);
		String descri = t.getMessage();
		int index = descri.indexOf("]");		
		if(index >= 0){					
			descri = descri.substring(index + 1);
		}
		dataItem.setDescrb(descri);
		KnbRptrBachDao.updateOne_odb1(dataItem);
		//super.jobExceptionProcess(taskId, input, property, jobId, dataItem, t);
	}

	/**
	 * 获取数据遍历器。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<String> getBatchDataWalker(
			cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpback.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpback.Property property) {
		log.debug("<<===================获取作业编号开始======================>>");
		 Params param = new Params();
		 param.put("filesq", input.getFilesq());
		 param.put("sourdt", property.getSourdt());
		
		return new CursorBatchDataWalker<String>(
				RpBatchTransDao.namedsql_selDataidByFilesq,param);
		
	}

	/**
	 * 获取作业数据遍历器
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @param dataItem
	 *            批次数据项
	 * @return
	 */
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach> getJobBatchDataWalker(
			cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpback.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpback.Property property,
			String dataItem) {
		log.debug("<<===================获取作业数据集合开始======================>>");
		Params param = new Params();
		param.put("filesq", input.getFilesq());
		param.put("sourdt", property.getSourdt());
		param.put("dataid", dataItem);
		log.debug("<<===================获取作业数据集合结束======================>>");
		return new CursorBatchDataWalker<KnbRptrBach>(
				RpBatchTransDao.namedsql_selBatchDataByFilesq, param);
	}

	@Override
	public void afterTranProcess(String taskId, Input input, Property property) {
		/*log.debug("<<===================批量提现交易结束后修改状态======================>>");
		filetab.setBtfest(E_BTFEST.SUCC);
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		log.debug("<<===================批量提现交易结束后修改状态结束======================>>");*/
		super.afterTranProcess(taskId, input, property);
	}
	
	@Override
	public void tranExceptionProcess(String taskId, Input input,
			Property property, Throwable t) {
		
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

}
