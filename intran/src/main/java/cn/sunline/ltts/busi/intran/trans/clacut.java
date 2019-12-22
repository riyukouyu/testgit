package cn.sunline.ltts.busi.intran.trans;

import java.util.List;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.util.BatchUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.KnsAcsqColl;
import cn.sunline.ltts.busi.in.tables.In.KnsAcsqCollDao;
import cn.sunline.ltts.busi.in.type.InDayEndTypes.InClerAccountList;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COCLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CLCTST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CLERST;


public class clacut {

 

	public static void clacut( String clerdt, 
			String filena, 
			cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___ issucs,
			String bathid, 
			final Options<cn.sunline.ltts.busi.in.type.InDayEndTypes.InClerAccountList> ioClerAccountList){

	if(CommUtil.isNull(clerdt)){
		
		throw InError.comm.E0005("核心记账日期不能为空！");
	}
	if(CommUtil.isNull(filena)){
		
		throw InError.comm.E0005("文件名不能为空");
	}
	if(CommUtil.isNull(issucs)){
		
		throw InError.comm.E0005("批次处理是否成功不能为空");
	}
	if(CommUtil.isNull(bathid)){
		
		throw InError.comm.E0005("批次号不能为空");
	}

	KnpParameter tbl_KnpParameter = KnpParameterDao.selectOne_odb1("InParm.clearAccount", "%", "%", "%", true);
		
	if(issucs ==E_YES___.NO){
		//前置请求核心记账失败，返回批次记账失败
		InDayEndSqlsDao.updateKnsAcsqCollByBathid(E_CLERST._3, bathid);
		List<KnsAcsqColl> tblKnsAcsqColl = KnsAcsqCollDao.selectAll_odb4(bathid, true);
	
		//更新明细记录为清算失败
		InacSqlsDao.updateKnsAcsqClerFailureByClerdt(tblKnsAcsqColl.get(0).getClerdt(), tblKnsAcsqColl.get(0).getClenum(), CommTools.getBaseRunEnvs().getTrxn_date(),DateTools2.getCurrentTimestamp());
		return;
		
	}
	 
 	//获取未清算数据
//	List<KnsAcsqColl> lstacsqcoll = InDayEndSqlsDao.selknsacsqcollByBathid(bathid, false);
	if(CommUtil.isNotNull(ioClerAccountList)){
		boolean  update =false;
		int count= InacSqlsDao.selectKnsAcsqClerByClerst(true);
		if(count>0){
			update=true;
		}
		//将当前未清算数据清算日期更新为柜面核心清算日期，清算状态更新为待清算
		for(InClerAccountList info:ioClerAccountList){
			
			String brchno = info.getBrchno();
			String crcycd = info.getCrcycd();
			E_YES___ status = info.getStatus();
			String failrs = info.getFailrs();//失败原因
			E_CLERST clerst = null;//网络核心记账状态
			E_CLCTST clctst = null;//柜面核心记账状态
			if(E_YES___.YES==status){
				clerst = E_CLERST._1;//待记账
				clctst = E_CLCTST._1;//已记账
			}else{
				clerst = E_CLERST._3;//清算失败
				clctst = E_CLCTST._2;//未记账
			}
			//柜面核心账户类型转换成网络核心的账户类型
			E_COCLTP cocltp = info.getClactp();
			E_CLACTP clactp = getClactp(cocltp);
			
			KnsAcsqColl tblColl = KnsAcsqCollDao.selectOne_odb2(bathid, brchno, crcycd, clactp, true);
			tblColl.setStatus(clerst);//清算状态
			tblColl.setTrandt(clerdt);//核心记账日期
			tblColl.setFailrs(failrs);
			tblColl.setCorest(clctst);
			KnsAcsqCollDao.update_odb1(tblColl);
			if(E_YES___.NO==status){
				//如果柜面核心记账失败，则更新明细为清算失败
				
				InacSqlsDao.updateKnsAcsqClerFailure(tblColl.getClerdt(), tblColl.getClenum(), tblColl.getBrchno(), tblColl.getProdcd(), 
						tblColl.getClactp(),CommTools.getBaseRunEnvs().getTrxn_date(),DateTools2.getCurrentTimestamp());
			}else{
				//更新之前清算失败的记录 交易日期和同步状态
				if(update)
				InacSqlsDao.updateKnsAcsqClerSuccess(tblColl.getTrandt(), tblColl.getBrchno(),  tblColl.getProdcd(), tblColl.getClactp(),DateTools2.getCurrentTimestamp());
			}
		}
		
	}
	
	//调用批量交易进行批量记账
	//   正常情况下核心记账日期和会计日期相同，但是可以比会计日期大 
	DataArea area = DataArea.buildWithEmpty();	
	area.getCommReq().setString("trandt",CommTools.getBaseRunEnvs().getTrxn_date() );
 	area.getCommReq().setString("jiaoyirq", CommTools.getBaseRunEnvs().getTrxn_date() );	
	BatchUtil.submitAndRunBatchTran(tbl_KnpParameter.getParm_value1(), tbl_KnpParameter.getParm_value2(), area);		

	
}
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
