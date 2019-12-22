package cn.sunline.ltts.busi.dptran.batchtran;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessorWithJobDataItem;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.dp.namedsql.KqBatchDao;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbQeryBachDao;
import cn.sunline.ltts.busi.dp.tables.RpMgr.knbQeryBach;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
	 /**
	  * 查询用户签约购买产品是否提前解约支取
	  *
	  */

public class querypDataProcessor extends
  AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Property, String, cn.sunline.ltts.busi.dp.tables.RpMgr.knbQeryBach> {
	
	  private static BizLog log = BizLogUtil.getBizLog(markkqDataProcessor.class);
	  private kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	  
      
	  @Override
	  public void beforeTranProcess(String taskId,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Property property) {
		    log.debug("<<===================查询产品状态交易前更新处理状态======================>>");
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.dp.tables.RpMgr.knbQeryBach dataItem, cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Property property) {
			//1.获取这个客户签约的产品对应的到期日期
			
//			FnbDetl fnbDetl = FnbDetlDao.selectFirst_odb2(dataItem.getDindno(), false);
//			if(CommUtil.isNotNull(fnbDetl)){ //理财
//				    knbQeryBach qeryBach = KnbQeryBachDao.selectOne_odb1(dataItem.getTransq(),dataItem.getFilesq(),true);
//				    qeryBach.setProdst(E_PRODST.zhengchan);
//	    			qeryBach.setTranst(E_TRANST.SUCCESS);
//	    			qeryBach.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
//					KnbQeryBachDao.updateOne_odb1(qeryBach);
//				
//			}else{
//				
//			String cardno = dataItem.getCardno();
//			
//			KnaAcdc acdc = KnaAcdcDao.selectOne_odb2(cardno, true);
//			
//			String dindno = dataItem.getDindno();
//			
//			
//			KnbRegi knbRegi = KnbRegiDao.selectOne_db1(dindno, true);
//            KnsTran knstran = KnsTranDao.selectOne_odb1(knbRegi.getTransq(), knbRegi.getCocrdt(), true);
//		    knbQeryBach qeryBach = KnbQeryBachDao.selectOne_odb1(dataItem.getTransq(),dataItem.getFilesq(),true);
//		    
//		    KnaFxac fxac = KnaFxacDao.selectOne_odb9(knstran.getBusisq(), true); 
//		    KnaAcct acct = KnaAcctDao.selectFirst_odb9(E_ACSETP.SA, acdc.getCustac(), true);
//                  
//		    	  KupDppbPost post = KupDppbPostDao.selectOne_odb2(knbRegi.getProdcd(), true);
//                  if(fxac.getDebttp() == E_DEBTTP.DP2509 && post.getDetlfg() == E_YES___.YES){
//
//		    		KnaSignDetl sign = KnaSignDetlDao.selectFirst_odb3(acct.getAcctno(), fxac.getAcctno(), E_SIGNST.JY, false);
//		    		
//		    		KnaSignDetl sign1 = KnaSignDetlDao.selectFirst_odb3(acct.getAcctno(), fxac.getAcctno(), E_SIGNST.QY, false);
//		    		//提前解约
//		    		if(CommUtil.isNotNull(sign) && CommUtil.compare(sign.getCncldt(), knbRegi.getCocrdt()) >= 0 && CommUtil.compare(sign.getCncldt(), sign.getEffedt()) <= 0){
//		    			qeryBach.setProdst(E_PRODST.jieyue);
//		    			qeryBach.setTranst(E_TRANST.SUCCESS);
//		    			qeryBach.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
//					    KnbQeryBachDao.updateOne_odb1(qeryBach);
//		    		}else if(CommUtil.isNotNull(sign1)){// 未解约，但是支取过
//				    	KnaFxdr fxdr = KnaFxdrDao.selectOne_odb1(fxac.getAcctno(), false);
//				    	if(CommUtil.isNotNull(fxdr) && CommUtil.compare(CommTools.getBaseRunEnvs().getTrxn_date(),sign1.getEffedt()) < 0  && fxdr.getRedwnm() !=0 ){
//							 qeryBach.setProdst(E_PRODST.tishuhui);
//				    		 qeryBach.setTranst(E_TRANST.SUCCESS);
//				    		 qeryBach.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
//							 KnbQeryBachDao.updateOne_odb1(qeryBach);
//						}else{
//							 qeryBach.setProdst(E_PRODST.zhengchan);
//				    		 qeryBach.setTranst(E_TRANST.SUCCESS);
//				    		 qeryBach.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
//							 KnbQeryBachDao.updateOne_odb1(qeryBach);
//						}
//		    		}
//		    		
//                  }else{
//		    		
//			    	  KnaFxdr fxdr = KnaFxdrDao.selectOne_odb1(fxac.getAcctno(), false);
//			    	  if(CommUtil.isNotNull(fxdr) && CommUtil.compare(CommTools.getBaseRunEnvs().getTrxn_date(),fxac.getMatudt()) < 0  && fxdr.getRedwnm() !=0 ){
//						    qeryBach.setProdst(E_PRODST.tishuhui);
//			    			qeryBach.setTranst(E_TRANST.SUCCESS);
//			    			qeryBach.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
//						    KnbQeryBachDao.updateOne_odb1(qeryBach);
//					  }else{
//						    qeryBach.setProdst(E_PRODST.zhengchan);
//			    			qeryBach.setTranst(E_TRANST.SUCCESS);
//			    			qeryBach.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
//							KnbQeryBachDao.updateOne_odb1(qeryBach);
//					  }
//                  }
//			}    
		  
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<String> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Property property) {
             log.debug("<<===================获取提前支取批量记账作业编号开始======================>>");
			 Params param = new Params();
			 param.put("filesq", input.getFilesq());
			 return new CursorBatchDataWalker<String>(KqBatchDao.namedsql_selDrawDataIdByFilesq,param);
		}
		
		/**
		 * 获取作业数据遍历器
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @param dataItem 批次数据项
		 * @return
		 */
		public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.knbQeryBach> getJobBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Property property, String dataItem) {
            log.debug("<<===================获取作业数据集合开始======================>>");
			
			Params param = new Params();
			param.put("filesq", input.getFilesq());
			param.put("dataid", dataItem);
            
            log.debug("<<===================获取作业数据集合结束======================>>");
			
			return new CursorBatchDataWalker<knbQeryBach>(KqBatchDao.namedsql_selBarchDrawDataByFileSq,param);
		}



		@Override
		public void jobExceptionProcess(
				String taskId,
				cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Input input,
				cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Property property,
				String jobId, knbQeryBach dataItem, Throwable t) {
			dataItem.setTranst(E_TRANST.FAIL);		
			String descri = t.getMessage();
			int index = descri.indexOf("]");		
			if(index >= 0){					
				descri = descri.substring(index + 1);
			}
			dataItem.setDescrb(descri);
			KnbQeryBachDao.updateOne_odb1(dataItem);
			super.jobExceptionProcess(taskId, input, property, jobId, dataItem, t);
		}

		@Override
		public void tranExceptionProcess(
				String taskId,
				cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Input input,
				cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Property property,
				Throwable t) {
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
		public void afterTranProcess(
				String taskId,
				cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Input input,
				cn.sunline.ltts.busi.dptran.batchtran.intf.Queryp.Property property) {
			
			filetab.setBtfest(E_BTFEST.SUCC);
			Kapb_wjplxxbDao.updateOne_odb1(filetab);
			
			super.afterTranProcess(taskId, input, property);
		}
		
		
		
		
	  

}


