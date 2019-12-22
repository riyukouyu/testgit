package cn.sunline.ltts.busi.dptran.batchtran;



import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dptran.batchtran.dayend.auacinDataProcessor;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

	 /**
	  * 金融基础产品自动启用停用
	  * 
	  *
	  */

public class basproDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.intf.Baspro.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Baspro.Property> {
	private static final BizLog bizlog = BizLogUtil
			.getBizLog(auacinDataProcessor.class);

	
	/**
	 * 批次数据项处理逻辑。
	 * 金融基础产品自动启用停用
	 *  
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.intf.Baspro.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Baspro.Property property) {
	
		 //*
		 //*修改基础产品属性表   记录状态为 装配生效、装配启用、装配停用
		 //*
		 
		 bizlog.debug("交易开始当前交易日期：[" + CommTools.getBaseRunEnvs().getTrxn_date() + "]");	
		 
		 String   efctdt = CommTools.getBaseRunEnvs().getTrxn_date();//交易时间
		 String   datetm = CommTools.getBaseRunEnvs().getTrxn_date();//当前时间 
		 String   inefdt = CommTools.getBaseRunEnvs().getTrxn_date();//当前时间 		 
		 Long     timetm = System.currentTimeMillis();//时间戳

		
		 //查询修改状态为生效的的数据 且生效日期小于等于当前的  为启用状态的记录条数
		 long  count =  DpDayEndDao.selbaseprocountqi(E_PRODST.EFFE, efctdt, false);
		 
		 bizlog.debug("修改状态为生效的的数据 且生效日期小于等于当前的  为启用状态的记录条数："
		 		+ String.valueOf(count));
		 
		 
		 //修改状态为生效的的数据 且生效日期小于等于当前的  为启用状态
		 DpDayEndDao.updbaseproqi(E_PRODST.EFFE, efctdt,  timetm);
		 
		 bizlog.debug("修改状态为生效的的数据 且生效日期小于等于当前的  为启用状态结束共修改："  + "count" +"条");
			 
		 
		 
		//查询修改状态为生效的的数据 且生效日期小于等于当前的  为启用状态的记录条数
		 long  counta =  DpDayEndDao.selbaseprocountti(E_PRODST.NORMAL, inefdt, false);
		 
		 bizlog.debug("修改状态为生效的的数据 且生效日期小于等于当前的  为启用状态的记录条数："
		 		+ String.valueOf(counta));
		 
		 
		//修改状态为启用状态的数据 且生效日期小于当前的  为停用状态
		 
		 DpDayEndDao.updbaseproti(E_PRODST.NORMAL, inefdt, timetm);
		 
		 bizlog.debug("修改状态为启用状态的数据 且生效日期小于当前的  为停用状态结束共修改："  + "counta" +"条"); 
		 
		 
		 
		 
		 //*
		 //*修改内部户产品表   记录状态为 装配生效、装配启用、装配停用
		 //*
	
		 String   bgdate = CommTools.getBaseRunEnvs().getTrxn_date();//当前时间 
		 
		 //查询内部户产品表状态为生效的的数据 且生效日期小于等于当前的  为启用状态的记录条数
		 long  countgl =  DpDayEndDao.selbaseglcountqi(E_PRODST.EFFE, bgdate, false);
		 
		 bizlog.debug("内部户产品表状态为生效的的数据 且生效日期小于等于当前的  为启用状态的记录条数："
		 		+ String.valueOf(countgl));
		 
		 //修改状态为生效的的数据 且生效日期小于等于当前的  为启用状态
		 DpDayEndDao.updbaseglqi(E_PRODST.EFFE, bgdate,  timetm);
		 
		 bizlog.debug("修改状态为生效的的数据 且生效日期小于等于当前的  为启用状态结束共修改："  + "count" +"条");
			 
		 
		 
		//查询修改状态为生效的的数据 且生效日期小于等于当前的  为启用状态的记录条数
		 long  countgla =  DpDayEndDao.selbaseglcountti(E_PRODST.NORMAL, bgdate, false);
		 
		 bizlog.debug("修改状态为生效的的数据 且生效日期小于等于当前的  为启用状态的记录条数："
		 		+String.valueOf(countgla));
		 
		 
		//修改状态为启用状态的数据 且生效日期小于当前的  为停用状态
		 DpDayEndDao.updbaseglti(E_PRODST.NORMAL, bgdate, timetm);
		 
		 bizlog.debug("修改状态为启用状态的数据 且生效日期小于当前的  为停用状态结束共修改："  + "counta" +"条"); 
		
		 
		 
		 
	}

}


