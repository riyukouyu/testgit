package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.domain.DpOpprEntity;
import cn.sunline.ltts.busi.dp.froz.DpFrozProc;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.namedsql.DpFrozDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDao;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpDeprInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpprovIn;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEPRBP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEPRTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPCGFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;

	 /**
	  * 存款证明到期自动撤销
	  *
	  */

public class audeprDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Audepr.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Audepr.Property, cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpDeprInfo> {
	private static BizLog log = BizLogUtil.getBizLog(audeprDataProcessor.class);
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpDeprInfo dataItem, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Audepr.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Audepr.Property property) {
			//获取上次交易日期
			String lastdt = DateTools2.getDateInfo().getLastdt();
			CommTools.getBaseRunEnvs().setTrxn_date(lastdt);
			
			//IoDpFrozSvcType DpFroz = CommTools.getRemoteInstance(IoDpFrozSvcType.class);
			IoDpprovIn caDpprIn = SysUtil.getInstance(IoDpprovIn.class);//撤销参数
			
			log.debug("++++++调用存款证明撤销开始++++++");
			//电子账号ID
			String custac = dataItem.getCustac();
			//重新获取法人代码
//			CommTools.getBaseRunEnvs().setBusi_org_id(dataItem.getCorpno());
			
			KnbFroz tblKnbFroz  = KnbFrozDao.selectOne_odb16(custac, dataItem.getMntrsq(), E_FROZST.VALID, false);
			
			CommTools.getBaseRunEnvs().setTrxn_branch(tblKnbFroz.getTranbr());
			
			if(CommUtil.isNotNull(tblKnbFroz)){
				
				String cardno = DpFrozDao.selKnaAcdcInfo(custac,true);
				
				caDpprIn.setDeprtp(dataItem.getDeprtp());
				caDpprIn.setDeprbp(E_DEPRBP.CL);
				caDpprIn.setDeprnm(dataItem.getDeprnm());
				caDpprIn.setIschge(E_DPCGFG.N);
				caDpprIn.setCardno(cardno);
				caDpprIn.setCustna(dataItem.getCustna());
				caDpprIn.setCrcycd(dataItem.getCrcycd());
				caDpprIn.setCsextg(null);
				caDpprIn.setDepram(dataItem.getDepram());
				caDpprIn.setStcenm(dataItem.getStcenm());
				caDpprIn.setEncenm(dataItem.getEncenm());
				caDpprIn.setNumber(dataItem.getNumber());
				caDpprIn.setBegndt(dataItem.getBegndt());
				caDpprIn.setEnddat(dataItem.getEnddat());
//				caDpprIn.setOpna01(dataItem.getOpna01());
//				caDpprIn.setOptp01(dataItem.getOptp01());
//				caDpprIn.setOpno01(dataItem.getOpno01());
				caDpprIn.setFeeamt(BigDecimal.ZERO);
				caDpprIn.setCorpno(dataItem.getCorpno());
				//调用存款证明撤销
				DpOpprEntity entity = new DpOpprEntity();
				DpFrozProc.dpprovProc(caDpprIn, entity);
				//DpFroz.IoCaDpprov(caDpprIn);
				log.debug("++++++调用存款证明撤销结束++++++");
			}
			
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpDeprInfo> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Audepr.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Audepr.Property property) {
			Params params = new Params();
			String lastdt = DateTools2.getDateInfo().getLastdt();
			params.add("trandt", lastdt);
			params.add("deprtp", E_DEPRTP.TP);
			params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
			return new CursorBatchDataWalker<DpDeprInfo>(DpDayEndDao.namedsql_selAutoDepr, params);
            
		}

}


