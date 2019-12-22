package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import java.math.BigDecimal;
import java.util.List;

//import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppHold;
//import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppHoldDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.froz.DpUnfrProc;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetl;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetlDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwne;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwneDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpUnStopPayIn;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_HOLIDAYTYPE;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.edsp.base.lang.Params;

	 /**
	  * 冻结到期自动解冻
	  *
	  */

public class auunfrDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Auunfr.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Auunfr.Property, cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz> {
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz dataItem, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Auunfr.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Auunfr.Property property) {
			//获取上次交易日期
			String lastdt = DateTools2.getDateInfo().getLastdt();
//			 TODO 表中增加节假日代码
//			AppHold appHold = AppHoldDao.selectOne_odb1(holdcd, holdtp, holday, true);
			boolean bIsHoliday =  DateTools2.isHoliday(lastdt, "100", E_HOLIDAYTYPE.LEGAL);
//			String  oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//			CommTools.getBaseRunEnvs().setBusi_org_id(dataItem.getCorpno());
			
			CommTools.getBaseRunEnvs().setTrxn_date(lastdt);
			if(! bIsHoliday){
				
				IoDpUnStopPayIn cpliodpunfrozin = SysUtil.getInstance(IoDpUnStopPayIn.class);
				
				//金额解冻需要设置解冻金额
				if(dataItem.getFrlmtp() == E_FRLMTP.AMOUNT){
					
					KnbFrozDetl tblKnbFrozDetl2 = KnbFrozDetlDao.selectOne_odb2(dataItem.getFrozno(), dataItem.getFrozsq(), true);
					
					cpliodpunfrozin.setUnfram(tblKnbFrozDetl2.getFrozbl()); //解冻金额
				}
				
				//获取原冻结登记机构,用来登记跑批时解冻记录的解冻机构
				CommTools.getBaseRunEnvs().setTrxn_branch(dataItem.getTranbr());
				
				//根据电子账号ID获取电子账号
				IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
				
				//IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.kna_acdc_selectOne_odb1(dataItem.getCustac(), E_DPACST.NORMAL, false);
				
				//根据电子账号查找户名
				IoCaKnaCust tblKnaCust = caSevQryTableInfo.getKnaCustWithLockByCustacOdb1(dataItem.getCustac(), true);
				
				//客户名称
				String custna = tblKnaCust.getCustna();
				
				//电子账号
				//String cardno = caKnaAcdc.getCardno();
				
				cpliodpunfrozin.setUfexog(dataItem.getFrexog());
				cpliodpunfrozin.setCrcycd(dataItem.getCrcycd());
				cpliodpunfrozin.setFrogna(dataItem.getFrogna());
			//	cpliodpunfrozin.setCardno(cardno);
				cpliodpunfrozin.setCustna(custna);
				cpliodpunfrozin.setOdfrno(dataItem.getFrozno());
				cpliodpunfrozin.setUfcttp(null);
				cpliodpunfrozin.setUfctno(null);
				cpliodpunfrozin.setUfexog(dataItem.getFrexog());
				cpliodpunfrozin.setIdno01(null);
				cpliodpunfrozin.setIdtp01(null);
				cpliodpunfrozin.setUfna01(null);
				cpliodpunfrozin.setIdno02(null);
				cpliodpunfrozin.setIdtp02(null);
				cpliodpunfrozin.setUfna02(null);
				cpliodpunfrozin.setUfreas("冻结到期自动解除");
				cpliodpunfrozin.setRemark(null);
				
				DpUnfrProc.unFrozDo(cpliodpunfrozin, dataItem);
				
				//查询冻结明细信息
				KnbFrozDetl detlInfo = KnbFrozDetlDao.selectOne_odb2(dataItem.getFrozno(), dataItem.getFrozsq(), true);
				//更新冻结明细登记簿状态
				detlInfo.setFrozst(E_FROZST.INVALID);
				detlInfo.setFrozbl(BigDecimal.ZERO);
				KnbFrozDetlDao.updateOne_odb2(detlInfo); 
				
				//查询当前冻结登记簿信息
				KnbFroz frozInfo = KnbFrozDao.selectOne_odb8(dataItem.getFrozno(), dataItem.getFrozsq(), true);
				frozInfo.setUnfrdt(lastdt);
				frozInfo.setFrozst(E_FROZST.INVALID);
				
				KnbFrozDao.updateOne_odb8(frozInfo);
				//查询当前未生效的续冻信息
				List<KnbFroz> info = KnbFrozDao.selectAll_odb5(dataItem.getFrozno(), E_FROZST.VALID, false);
				
				if(CommUtil.isNotNull(info)){
					for(KnbFroz tblKnbFroz : info){
						//将续冻金额累加到冻结主体登记簿中
						if(E_FRLMTP.AMOUNT == tblKnbFroz.getFrlmtp()){
							//更新生效的续冻的冻结主体登记簿冻结余额
							KnbFrozDetl tblKnbFrozDetl = KnbFrozDetlDao.selectOneWithLock_odb2(dataItem.getFrozno(), tblKnbFroz.getFrozsq(), true);
							KnbFrozOwne tblKnbFrozOwne = KnbFrozOwneDao.selectOneWithLock_odb1(E_FROZOW.AUACCT, dataItem.getCustac(), true);
							tblKnbFrozOwne.setFrozbl(tblKnbFrozOwne.getFrozbl().add(tblKnbFrozDetl.getFrozam()));
							KnbFrozOwneDao.updateOne_odb1(tblKnbFrozOwne);
							//更新生效的续冻的冻结明细登记簿冻结余额
							tblKnbFrozDetl.setFrozbl(tblKnbFrozDetl.getFrozam());
							tblKnbFrozDetl.setFrozst(E_FROZST.VALID);
							KnbFrozDetlDao.updateOne_odb2(tblKnbFrozDetl);
						}
					}
				}
				
//				//根据冻结编号查询冻结信息
//				List<KnbFroz> frozInfo = KnbFrozDao.selectAll_odb5(dataItem.getFrozno(), E_FROZST.VALID, false);
//				
//				long count = 0;
//				
//				//判断是否存在续冻
//				if(CommUtil.isNotNull(frozInfo)){
//					
//					if(frozInfo.size() == 1){
//						
//						cpliodpunfrozin.setUfexog(dataItem.getFrexog());
//						cpliodpunfrozin.setCrcycd(dataItem.getCrcycd());
//						cpliodpunfrozin.setFrogna(dataItem.getFrogna());
//						cpliodpunfrozin.setCardno(cardno);
//						cpliodpunfrozin.setCustna(custna);
//						cpliodpunfrozin.setOdfrno(dataItem.getFrozno());
//						cpliodpunfrozin.setUfcttp(null);
//						cpliodpunfrozin.setUfctno(null);
//						cpliodpunfrozin.setUfexog(dataItem.getFrexog());
//						cpliodpunfrozin.setIdno01(null);
//						cpliodpunfrozin.setIdtp01(null);
//						cpliodpunfrozin.setUfna01(null);
//						cpliodpunfrozin.setIdno02(null);
//						cpliodpunfrozin.setIdtp02(null);
//						cpliodpunfrozin.setUfna02(null);
//						cpliodpunfrozin.setUfreas("到期自动解除");
//						cpliodpunfrozin.setUnmark(null);
//						
//						DpUnfrProc.unFrozDo(cpliodpunfrozin, dataItem);
//					
//					//存在续冻
//					}else if(frozInfo.size() > 1){
//						
//						for(long i = frozInfo.size(); i > 0; i--){
//							
//							KnbFroz tblKnbFroz = KnbFrozDao.selectOne_odb8(dataItem.getFrozno(), i, true);
//							
//							if(DateUtil.compareDate(tblKnbFroz.getFreddt(),DateTools2.getDateInfo().getSystdt()) <= 0){
//								count = i;
//								break;
//							}
//						}
//					    
//						//当前最大序号的冻结编号到期做解冻处理，其他只做更新登记簿状态
//						if(count == frozInfo.size()){
//							
//							for(long j = 1; j<count; j++){
//								//查询当前交易信息
//								KnbFroz tblKnbFroz = KnbFrozDao.selectOne_odb8(dataItem.getFrozno(), j, true);
//								tblKnbFroz.setFrozst(E_FROZST.INVALID);
//								KnbFrozDao.updateOne_odb8(tblKnbFroz);
//								
//								//若为部冻
//								if(E_FRLMTP.AMOUNT == tblKnbFroz.getFrlmtp()){
//									//查询冻结明细信息
//									KnbFrozDetl tblKnbFrozDetl = KnbFrozDetlDao.selectOne_odb2(dataItem.getFrozno(), j, true);
//									
//									//更新冻结主体余额
//									KnbFrozOwne tblKnbFrozOwne = KnbFrozOwneDao.selectOneWithLock_odb1(E_FROZOW.AUACCT, dataItem.getCustac(), true);
//									tblKnbFrozOwne.setFrozbl(tblKnbFrozOwne.getFrozbl().subtract(tblKnbFrozDetl.getFrozam()));
//									KnbFrozOwneDao.updateOne_odb1(tblKnbFrozOwne);
//									
//									//更新冻结明细登记簿状态
//									tblKnbFrozDetl.setFrozst(E_FROZST.INVALID);
//									tblKnbFrozDetl.setFrozbl(BigDecimal.ZERO);
//									KnbFrozDetlDao.updateOne_odb2(tblKnbFrozDetl); 
//								
//								}else{
//									//查询冻结明细信息
//									KnbFrozDetl tblKnbFrozDetl = KnbFrozDetlDao.selectOne_odb2(dataItem.getFrozno(), j, true);
//									//更新冻结明细登记簿状态
//									tblKnbFrozDetl.setFrozst(E_FROZST.INVALID);
//									KnbFrozDetlDao.updateOne_odb2(tblKnbFrozDetl); 
//								}
//								
//							}
//							
//							//查询续冻信息
//							KnbFroz tblKnbFroz = KnbFrozDao.selectOne_odb8(dataItem.getFrozno(), count, true);
//							cpliodpunfrozin.setUfexog(tblKnbFroz.getFrexog());
//							cpliodpunfrozin.setCrcycd(tblKnbFroz.getCrcycd());
//							cpliodpunfrozin.setFrogna(tblKnbFroz.getFrogna());
//							cpliodpunfrozin.setCardno(cardno);
//							cpliodpunfrozin.setCustna(custna);
//							cpliodpunfrozin.setOdfrno(tblKnbFroz.getFrozno());
//							cpliodpunfrozin.setUfcttp(null);
//							cpliodpunfrozin.setUfctno(null);
//							cpliodpunfrozin.setUfexog(tblKnbFroz.getFrexog());
//							cpliodpunfrozin.setIdno01(null);
//							cpliodpunfrozin.setIdtp01(null);
//							cpliodpunfrozin.setUfna01(null);
//							cpliodpunfrozin.setIdno02(null);
//							cpliodpunfrozin.setIdtp02(null);
//							cpliodpunfrozin.setUfna02(null);
//							cpliodpunfrozin.setUfreas("续冻到期自动解除");
//							cpliodpunfrozin.setUnmark(null);
//							
//							DpUnfrProc.unFrozDo(cpliodpunfrozin, tblKnbFroz);
//						
//						//该冻结编号下的最大冻结序号未到期，到期的数据只做更新登记簿处理
//						}else if(count < frozInfo.size()){
//							
//							for(long j = 1 ; j<=count; j++){
//								
//								KnbFroz tblKnbFroz = KnbFrozDao.selectOne_odb8(dataItem.getFrozno(), j, true);
//								tblKnbFroz.setFrozst(E_FROZST.INVALID);
//								KnbFrozDao.updateOne_odb8(tblKnbFroz);
//								
//								//若为部冻
//								if(E_FRLMTP.AMOUNT == tblKnbFroz.getFrlmtp()){
//									//查询冻结明细信息
//									KnbFrozDetl tblKnbFrozDetl = KnbFrozDetlDao.selectOne_odb2(dataItem.getFrozno(), j, true);
//									//更新冻结主体余额
//									KnbFrozOwne tblKnbFrozOwne = KnbFrozOwneDao.selectOne_odb1(E_FROZOW.AUACCT, dataItem.getCustac(), true);
//									tblKnbFrozOwne.setFrozbl(tblKnbFrozOwne.getFrozbl().subtract(tblKnbFrozDetl.getFrozam()));
//									KnbFrozOwneDao.updateOne_odb1(tblKnbFrozOwne);
//									
//									//更新冻结明细登记簿状态
//									tblKnbFrozDetl.setFrozst(E_FROZST.INVALID);
//									tblKnbFrozDetl.setFrozbl(BigDecimal.ZERO);
//									KnbFrozDetlDao.updateOne_odb2(tblKnbFrozDetl);
//									
//								}else{
//									
//								}
//							}
//							
//							long k = frozInfo.size();
//							KnbFroz tblKnbFroz = KnbFrozDao.selectOne_odb8(dataItem.getFrozno(), k, true);
//							
//							
//						}
//					}
//				}
				
			}
//			CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Auunfr.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Auunfr.Property property) {
			String lastdt = DateTools2.getDateInfo().getLastdt();
			Params params = new Params();
			params.add("freddt", lastdt);
			params.add("frozst", E_FROZST.VALID);
			params.add("froztp", E_FROZTP.JUDICIAL);
			params.add("froztp2", E_FROZTP.ADD);
			params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
			return new CursorBatchDataWalker<KnbFroz>(DpDayEndDao.namedsql_selKnbFrozByMadt, params);
		}

}


