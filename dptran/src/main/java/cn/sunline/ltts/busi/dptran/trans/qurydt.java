package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;


public class qurydt {

	public static void qrydte( final cn.sunline.ltts.busi.dptran.trans.intf.Qurydt.Output output){
	
		//调用获取交易系统日期方法
		ApSysDateStru sysdate = DateTools2.getDateInfo();
	
		//获取上日交易日期
		String lastdt = sysdate.getLastdt();
		
		//获取单前系统日期
		String systdt = sysdate.getSystdt();
	
		//获取下个交易日期
		String nextdt = sysdate.getNextdt();
		
		//输出以上三个对应日期
		output.setLastdt(lastdt);
		
		output.setSystdt(systdt);
		
		output.setNextdt(nextdt);
	}
}
