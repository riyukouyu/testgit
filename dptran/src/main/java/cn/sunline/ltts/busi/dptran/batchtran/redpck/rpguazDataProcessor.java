package cn.sunline.ltts.busi.dptran.batchtran.redpck;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
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
import cn.sunline.ltts.busi.dp.namedsql.redpck.RpBatchTransDao;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBachDao;
import cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Input;
import cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Property;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_RPTRTP;
	/**
	 * 
	 * @ClassName: rpguazDataProcessor 
	 * @Description: 红包批量挂账 
	 * @author huangzhikai
	 * @date 2016年7月7日 上午10:53:04 
	 *
	 */
public class rpguazDataProcessor extends
  AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Input, cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Property, String, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach> {
	  
	  private static BizLog log = BizLogUtil.getBizLog(rpbackDataProcessor.class);
	  private static kapb_wjplxxb tblkapbWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);
	  /**
	     * 
	     * @Title: beforeTranProcess 
	     * @Description: 批次数据项处理逻辑前处理 
	     * @param input 批量交易输入接口
	     * @param property 批量交易属性接口
	     * @author huangzhikai
	     * @date 2016年7月7日 上午10:54:02 
	     * @version V2.3.0
	     */
	    @Override
		public void beforeTranProcess(
				String taskId,
				cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Input input,
				cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Property property) {
	    	log.debug("<<===================批量提现交易前更新处理状态======================>>");
	    	tblkapbWjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
			
	    	property.setSourdt(tblkapbWjplxxb.getAcctdt());
	    	
			super.beforeTranProcess(taskId, input, property);
		}
	    
	    /**
	     * 
	     * @Title: process 
	     * @Description: 批次数据项处理逻辑 
	     * @param jobId 批次作业ID
	     * @param index 批次作业第几笔数据(从1开始)
	     * @param dataItem 批次数据项
	     * @param input 批量交易输入接口
	     * @param property 批量交易属性接口
	     * @author huangzhikai
	     * @date 2016年7月7日 上午10:54:02 
	     * @version V2.3.0
	     */
		@Override
		public void process(String jobId, int index, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach dataItem, cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Input input, cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Property property) {
			
			log.debug("<<===================批量挂账交易处理开始======================>>");
			log.debug("<<=========================================>>");
			log.debug("贷方卡号：" + dataItem.getCrcard());
			log.debug("贷方账号：" + dataItem.getCrdact());
			log.debug("贷方子账号：" + dataItem.getCracct());
			log.debug("借方卡号：" + dataItem.getDecard());
			log.debug("借方账号：" + dataItem.getDebact());
			log.debug("借方子账号：" + dataItem.getDeacct());
			log.debug("<<=========================================>>");
			
			//CommTools.genNewSerail(dataItem.getTransq());
			MsSystemSeq.getTrxnSeq();
			// 内部户借方记账输入
			IaAcdrInfo acdr1 = SysUtil.getInstance(IaAcdrInfo.class); 
			// 内部户贷方记账输入
			IaAcdrInfo acdr2 = SysUtil.getInstance(IaAcdrInfo.class); 
			//内部户借方记账输出
			IaTransOutPro inout1 = SysUtil.getInstance(IaTransOutPro.class);
			//内部户贷方记账输出
			IaTransOutPro inout2 = SysUtil.getInstance(IaTransOutPro.class);
			// 内部户记账服务
			IoInAccount insvc = SysUtil.getInstance(IoInAccount.class); 
			
//			DpAcctSvcType dpsvc = SysUtil.getInstance(DpAcctSvcType.class); // 客户账记账接口
//			IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
//			IoCaKnaAcdc acdc = SysUtil.getInstance(IoCaKnaAcdc.class);
//			kna_acct acct = SysUtil.getInstance(kna_acct.class);
			E_RPTRTP bathtp = dataItem.getRptrtp();
			E_RPTRTP rptype = dataItem.getRptype();
//			SaveDpAcctIn save = SysUtil.getInstance(SaveDpAcctIn.class); // 负债账户存入参数

			dataItem.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			dataItem.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
			
			
			//判断是否为红包挂账
			if(bathtp == E_RPTRTP.BT302){
				
				
				KnpParameter tbl_KnpParameter = KnpParameterDao.selectOne_odb1("DPTRAN", "RPPATH", "020100", rptype.getId(), true);
				
				acdr1.setAcbrch(dataItem.getDeborg()); // 借方机构
				acdr1.setBusino(tbl_KnpParameter.getParm_value1()); // 业务代码9922410142
				acdr1.setCrcycd(dataItem.getCrcycd()); // 币种
				acdr1.setSmrycd(dataItem.getSmrycd()); // 摘要码
				acdr1.setSubsac(tbl_KnpParameter.getParm_value4());//子户号
				acdr1.setDscrtx(tbl_KnpParameter.getParm_value2()); // 描述
				acdr1.setTranam(dataItem.getTranam());
				
				
				
				KnpParameter para = KnpParameterDao.selectOne_odb1("DPTRAN", "RPPATH", "020100", "C", true);
				acdr2.setAcbrch(dataItem.getDeborg()); // 贷方机构
				acdr2.setBusino(para.getParm_value1()); // 业务代码9922410142
				acdr2.setCrcycd(dataItem.getCrcycd()); // 币种
				acdr2.setSmrycd(dataItem.getSmrycd()); // 摘要码
				acdr2.setSubsac(para.getParm_value4());//子户号
				acdr2.setDscrtx(para.getParm_value2()); // 描述
				acdr2.setTranam(dataItem.getTranam());
				
				inout1 = insvc.ioInAcdr(acdr1); // 内部户借方记账处理
				inout2 = insvc.ioInAccr(acdr2);//内部户贷方记账处理
				
				dataItem.setDebact(inout1.getAcctno());
				dataItem.setDebnam(inout1.getAcctna());
				
				dataItem.setCrdact(inout2.getAcctno());
				dataItem.setCrdnam(inout2.getAcctna());
//				acdc = caqry.kna_acdc_selectOne_odb2(dataItem.getCrcard(), true);
//				acct = Kna_acctDao.selectFirst_odb7(acdc.getCustac(),E_DEBTTP.DP1101, true);
//
//				save.setAcctno(acct.getAcctno());
//				save.setAcseno("");
//				save.setCardno(acdc.getCardno());
//				save.setCrcycd(dataItem.getCrcycd());
//				save.setCustac(acdc.getCustac());
//				save.setOpacna(inout.getAcctna());
//				save.setToacct(inout.getAcctno());
//				save.setTranam(dataItem.getTranam());
//				dpsvc.addPostAcctDp(save); // 负债账户存入记账处理
				
			}
			
			//平衡性检查
			SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._01);
			dataItem.setTranst(E_TRANST.SUCCESS);
			dataItem.setDescrb("成功完成");
			
			KnbRptrBachDao.updateOne_odb1(dataItem);

			log.debug("<<===================批量挂账交易处理结束======================>>");
		}
		
		/**
		 * 
		 * @Title: jobExceptionProcess 
		 * @Description: 更新批处理作业失败状态 
		 * @param taskId
		 * @param input
		 * @param property
		 * @param jobId
		 * @param dataItem
		 * @param t
		 * @author huangzhikai
		 * @date 2016年7月7日 上午10:57:40 
		 * @version V2.3.0
		 */
		@Override
		public void jobExceptionProcess(
			String taskId,
			cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Property property,
			String jobId, KnbRptrBach dataItem, Throwable t) {
			
			dataItem.setTranst(E_TRANST.FAIL);
			dataItem.setDescrb(t.getMessage());
			KnbRptrBachDao.updateOne_odb1(dataItem);
					
			//super.jobExceptionProcess(taskId, input, property, jobId, dataItem, t);
		}
		
		/**
		 * 
		 * @Title: getBatchDataWalker 
		 * @Description: 获取数据遍历器 
		 * @param input
		 * @param property
		 * @return ListBatchDataWalker<String>
		 * @author huangzhikai
		 * @date 2016年7月7日 上午10:58:50 
		 * @version V2.3.0
		 */
		@Override
		public BatchDataWalker<String> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Input input, cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Property property) {
			log.debug("<<===================获取作业编号开始======================>>");
			
			Params param = new Params();
			param.put("filesq", input.getFilesq());
			param.put("sourdt", property.getSourdt());
			
			return new CursorBatchDataWalker<String>(
					RpBatchTransDao.namedsql_selDataidByFilesq,param);
		}
		
		/**
		 * 
		 * @Title: getJobBatchDataWalker 
		 * @Description: 获取作业数据遍历器 
		 * @param input
		 * @param property
		 * @param dataItem
		 * @return CursorBatchDataWalker<KnbRptrBach>
		 * @author huangzhikai
		 * @date 2016年7月7日 上午10:59:23 
		 * @version V2.3.0
		 */
		public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach> getJobBatchDataWalker(
				cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Input input, 
				cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Property property, String dataItem) {
			
			log.debug("<<===================获取作业数据集合开始======================>>");
			Params param = new Params();
			param.put("filesq", input.getFilesq());
			param.put("sourdt", property.getSourdt());
			param.put("dataid", dataItem);
			log.debug("<<===================获取作业数据集合结束======================>>");
			return new CursorBatchDataWalker<KnbRptrBach>(
					RpBatchTransDao.namedsql_selBatchDataByFilesq, param);
		}
		
		/**
		 * 
		 * @Title: afterTranProcess 
		 * @Description: 交易后处理 
		 * @param taskId
		 * @param input
		 * @param property
		 * @author huangzhikai
		 * @date 2016年7月7日 上午11:00:01 
		 * @version V2.3.0
		 */
		@Override
		public void afterTranProcess(
			String taskId,
			cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpguaz.Property property) {
/*				log.debug("<<===================批量提现交易结束后修改状态======================>>");
				tblkapbWjplxxb.setBtfest(E_BTFEST.BUSISUCC);
				Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
				log.debug("<<===================批量提现交易结束后修改状态结束======================>>");*/
				super.afterTranProcess(taskId, input, property);
		}
		
		/**
		 * 
		 * @Title: tranExceptionProcess 
		 * @Description: (失败异常处理) 
		 * @param taskId
		 * @param input
		 * @param property
		 * @param t
		 * @author xiongzhao
		 * @date 2017年1月17日 下午8:03:25 
		 * @version V2.3.0
		 */
		@Override
			public void tranExceptionProcess(String taskId, Input input,
					Property property, Throwable t) {
			log.debug("<<===================批量提现交易失败后修改状态======================>>");
			DaoUtil.executeInNewTransation(new RunnableWithReturn<kapb_wjplxxb>() {
				@Override
				public kapb_wjplxxb execute() {
					tblkapbWjplxxb.setBtfest(E_BTFEST.FAIL);
					Kapb_wjplxxbDao.updateOne_odb1(tblkapbWjplxxb);
					return null;
				}
			});
			log.debug("<<===================批量提现交易失败后修改状态结束======================>>");
			super.tranExceptionProcess(taskId, input, property, t);
			}
}


