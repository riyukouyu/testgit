package cn.sunline.ltts.busi.intran.trans;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.type.InDayEndTypes.ioClerResultList;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.errors.PbError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COCLTP;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrclct {

	public static void slclct( final cn.sunline.ltts.busi.intran.trans.intf.Qrclct.Input input,  
			final cn.sunline.ltts.busi.intran.trans.intf.Qrclct.Output output){
		
		//输入参数非空检查
		if(CommUtil.isNull(input.getTrandt())){
			throw InError.comm.E0005("记账日期不能为空！");
		}
		if(CommUtil.isNull(input.getClactp())){
			throw InError.comm.E0005("账户类型不能为空！");
		}
		
		String Clerbr = BusiTools.getBusiRunEnvs().getCentbr();//获取省联社清算中心			
		//交易机构
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
		if(!CommUtil.equals(tranbr,Clerbr)){
			if(CommUtil.isNull(input.getBrchno())){
				throw PbError.Branch.E0002("非省中心，机构号必输！");
			}
		}
		if(CommUtil.isNotNull(input.getBrchno())){
			
			if(!CommUtil.equals(tranbr,Clerbr)&&!CommUtil.equals(tranbr, input.getBrchno())){
				throw PbError.Branch.E0002("非省中心，只能查询本机构的信息！");
			}			
		}
		
		//柜面核心账户类型转换成网络核心的账户类型
		E_COCLTP cocltp = input.getClactp();
		E_CLACTP clactp = getClactp(cocltp);
		
		// 当网络核心记账状态为1-已记账，则查询汇总表已清算数据
			List<ioClerResultList> cplAcsqDelt = InacSqlsDao.
					selAcsqCollClerdInfo(input.getTrandt(), clactp,input.getClenum(), input.getBrchno(), false);
			
			//检查记录是否为空
			if(cplAcsqDelt.size()<1 || CommUtil.isNull(cplAcsqDelt)){
				return;
			}
			Options<ioClerResultList> results = new DefaultOptions<ioClerResultList>();
			//遍历汇总表记录，转换账户类型
			for(ioClerResultList collDetlInfos:cplAcsqDelt){
				ioClerResultList collDetlInfo = prcCollDelt(collDetlInfos);
				results.add(collDetlInfo);
			}
			
			output.setIoClerResultList(results);
		}
	
	//将网络核心清算账户类型转换成柜面核心清算账户类型
	private static ioClerResultList prcCollDelt(ioClerResultList collDetlInfo) {

		//返回接口的账户类型转换
		String clactp = null;
		if(CommUtil.equals(collDetlInfo.getClactp(), "01")){
			//网络柜面
			clactp = "2";
		}else if(CommUtil.equals(collDetlInfo.getClactp(), "02")){
			//中间业务
			clactp = "10";
		}else if(CommUtil.equals(collDetlInfo.getClactp(), "03")){
			//银联借记
			clactp = "9";
		}else if(CommUtil.equals(collDetlInfo.getClactp(), "04")){
			//银联贷记
			clactp = "8";
		}else if(CommUtil.equals(collDetlInfo.getClactp(), "05")){
			//大额支付系统
			clactp = "4";
		}else if(CommUtil.equals(collDetlInfo.getClactp(), "06")){
			//小额支付系统
			clactp = "5";
		}else if(CommUtil.equals(collDetlInfo.getClactp(), "07")){
			//农信银支付系统
			clactp = "6";
		}else if(CommUtil.equals(collDetlInfo.getClactp(), "08")){
			//跨行网银支付系统
			clactp = "7";
		}else if(CommUtil.equals(collDetlInfo.getClactp(), "09")){
			//信用卡
			clactp = "3";
		}else if(CommUtil.equals(collDetlInfo.getClactp(), "10")){
			//系统内
			clactp = "1";
		}else if(CommUtil.equals(collDetlInfo.getClactp(), "11")){
			//银联在线代收
			clactp = "11";
		}else{
			throw InError.comm.E0005("账户类型错误！");
		}
		
		collDetlInfo.setClactp(clactp);
		
		return collDetlInfo;
	}
	
	//将柜面核心清算账户类型转换成网络核心清算账户类型
	public static E_CLACTP  getClactp(E_COCLTP cocltp ) {
		
		if(E_COCLTP._1==cocltp){
			//系统内
			return E_CLACTP._10; 
		}else if(E_COCLTP._2==cocltp){
			//网络柜面
			return E_CLACTP._01;
		}else if(E_COCLTP._3==cocltp){
			//信用卡
			return E_CLACTP._09;
		}else if(E_COCLTP._4==cocltp){
			//大额支付系统
			
			return E_CLACTP._05;
		}else if(E_COCLTP._5==cocltp){
			//小额支付系统
			
			return E_CLACTP._06;
		}else if(E_COCLTP._6==cocltp){
			//农信银支付系统
			
			return E_CLACTP._07;
		}else if(E_COCLTP._7==cocltp){
			//跨行网银支付系统
			
			return E_CLACTP._08;
		}else if(E_COCLTP._8==cocltp){
			//银联贷记业务
			
			return E_CLACTP._04;
		}else if(E_COCLTP._9==cocltp){
			//银联借记业务
			
			return E_CLACTP._03;
		}else if(E_COCLTP._10==cocltp){
			//中间业务
			
			return E_CLACTP._02;
		}else if(E_COCLTP._11==cocltp){
			//银联在线代收
			
			return E_CLACTP._18;
		}else{
			
			throw InError.comm.E0005("账户类型错误！");
		}

	}
}
