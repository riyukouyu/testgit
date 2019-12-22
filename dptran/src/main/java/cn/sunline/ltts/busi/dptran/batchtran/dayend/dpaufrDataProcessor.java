package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.serviceimpl.IoDpFrozSrvTypeImpl;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStUfIn;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.edsp.base.lang.Params;
	 /**
	  * 存款冻结到期自动解冻
	  *
	  */

public class dpaufrDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Dpaufr.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Dpaufr.Property, cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz> {
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz dataItem, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Dpaufr.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Dpaufr.Property property) {
			//TODO:只处理签约交易产生的金额冻结
            if(dataItem.getFroztp() == E_FROZTP.AM && CommUtil.equals(dataItem.getLttscd(), "smsign")){//金额冻结&&签约交易
                String frozdt = dataItem.getFrozdt();//冻结日期
                String mntrsq = dataItem.getMntrsq();//冻结流水
                IoDpStUfIn ioDpStUfIn = SysUtil.getInstance(IoDpStUfIn.class);
                ioDpStUfIn.setTrandt(frozdt);
                ioDpStUfIn.setMntrsq(mntrsq);
                //单笔解冻
                SysUtil.getInstance(IoDpFrozSrvTypeImpl.class).danBiStUf(ioDpStUfIn);;
            }
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Dpaufr.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Dpaufr.Property property) {
			//TODO:	查询金额冻结到期结果集
		    String systdt = DateTools2.getDateInfo().getSystdt();//到期日期=当天
            Params params = new Params();
            params.add("freddt", systdt);
            params.add("frozst", E_FROZST.VALID);
            params.add("froztp", E_FROZTP.AM);            
            params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
            return new CursorBatchDataWalker<KnbFroz>(DpDayEndDao.namedsql_selDpKnbFrozByMadt, params);
		}

}


