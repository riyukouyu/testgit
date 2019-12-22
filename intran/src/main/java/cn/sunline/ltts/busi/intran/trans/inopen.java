package cn.sunline.ltts.busi.intran.trans;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.InModuleError;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnpBusi;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BEINTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INACST;


public class inopen {

	public static void beforeTransCheck( String busino,  String acctna, String crcycd,  String acbrch,  java.math.BigDecimal ovmony,  cn.sunline.ltts.busi.sys.type.InEnumType.E_BEINTP beintp,  java.math.BigDecimal inrate,  java.math.BigDecimal ovrate,  cn.sunline.ltts.busi.sys.type.InEnumType.E_ISPAYA ispaya,  cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___ isbein,  String reveac,  String paymac,  cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INACTP inactp,  String subsac,  String itemcd,  final cn.sunline.ltts.busi.intran.trans.intf.Inopen.Output output){

		if(CommUtil.isNull(busino)){
			throw InModuleError.InAccount.IN010001();
		}
		
		if(CommUtil.isNull(crcycd)){
			throw InModuleError.InAccount.IN010002();
		}
		
		if(!CommUtil.equals(crcycd, BusiTools.getDefineCurrency())){
			throw InModuleError.InAccount.IN010008(crcycd,BusiTools.getDefineCurrency());
		}
		
		if(CommUtil.isNull(acctna)){
			throw InModuleError.InAccount.IN010003();
		}
		if(CommUtil.isNotNull(subsac)){
			throw InModuleError.InAccount.IN010004();
		}
		if(CommUtil.isNull(inactp)){
			throw InModuleError.InAccount.IN010005();
		}
		if(CommUtil.isNull(acbrch)){
			acbrch=CommTools.getBaseRunEnvs().getTrxn_branch();
		}else if(!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), acbrch)){
			throw InModuleError.InAccount.IN010006();
		}
		//机构合法性检查
		SysUtil.getRemoteInstance(IoSrvPbBranch.class).inspectBranchLegality(acbrch, crcycd);
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		GlKnpBusi tblbusi = InQuerySqlsDao.selBusinoDetail(busino, corpno, false);
		
		/*if(busino.length() != 10){
			throw InModuleError.InAccount.IN010009("产品编码["+busino+"]位数不满足！");
		}*/
		if(CommUtil.isNull(tblbusi)){
			throw InModuleError.InAccount.IN010007(busino);
		}		
		
				
		if(CommUtil.isNull(beintp)){
			throw InModuleError.InAccount.IN010009();
		}
		
		
		if(E_BEINTP._5==beintp&&CommUtil.isNotNull(isbein)){
			throw InModuleError.InAccount.IN010011();
		}
		if(CommUtil.isNull(ispaya)){
			throw InModuleError.InAccount.IN010010();
			
		}
		

		
		/*if(E_YES___.NO==isbein&&(CommUtil.isNotNull(inrate)||CommUtil.isNotNull(ovrate))){
			
			throw InModuleError.InAccount.IN010009("结息是否入账值为否，年利率和透支年利率跳过不输入！");
		}
	
		//modify by wuzx 20161118 新增界面和查询界面结息为否利率统一显示为0 beg
		if(E_YES___.NO==isbein&&(CommUtil.compare(inrate,BigDecimal.ZERO) == 0||CommUtil.compare(ovrate,BigDecimal.ZERO) == 0)){
		
		throw InModuleError.InAccount.IN010009("结息是否入账值为否，年利率和透支年利率跳过不输入！");
		}
		//modify by wuzx 20161118 新增界面和查询界面结息为否利率统一显示为0 end
		 * */
		
		if(E_YES___.YES==isbein&&(CommUtil.isNull(inrate)||CommUtil.isNull(ovrate))){
			
			throw InModuleError.InAccount.IN010012();
		}
		
		if(E_YES___.NO==isbein&&(CommUtil.isNotNull(reveac)||CommUtil.isNotNull(paymac))){
			
			throw InModuleError.InAccount.IN010011();
		}
		
/*		if(E_YES___.YES==isbein&&(CommUtil.isNull(reveac)||CommUtil.isNull(paymac))){
			
			throw InModuleError.InAccount.IN010009("结息是否入账值为是，收息账号和付息账号必输！");
		}	*/	
		//收息账号不为空检查账户
		if(CommUtil.isNotNull(reveac)){		
			
			GlKnaAcct reacct = InQuerySqlsDao.sel_GlKnaAcct_by_acct(reveac, true);
			
			if(reacct.getAcctst()!=E_INACST.NORMAL){
				throw InModuleError.InAccount.IN020001(reveac);				
			}
			
			if(!CommUtil.equals(reacct.getBrchno(), acbrch)){
				throw InModuleError.InAccount.IN020002(reveac);			
			}
			if(CommUtil.isNotNull(tblbusi.getNobusi())&&!CommUtil.equals(reacct.getBusino(), tblbusi.getNobusi())){
				throw InModuleError.InAccount.IN020003(reveac,reacct.getBusino(),tblbusi.getNobusi());	
			}
		}else if(E_YES___.YES==isbein){
			//收息但未输入收息账号，默认账号本身
			if(CommUtil.isNotNull(tblbusi.getNobusi())&&!CommUtil.equals(busino, tblbusi.getNobusi())){
				throw InModuleError.InAccount.IN020004(tblbusi.getNobusi());	
			}
		}
		
		
		//付息账号不可为空检查账户
		if(CommUtil.isNotNull(paymac)){		
			
			GlKnaAcct payacct = InQuerySqlsDao.sel_GlKnaAcct_by_acct(paymac, true);
			
			if(payacct.getAcctst()!=E_INACST.NORMAL){
				throw InModuleError.InAccount.IN020005(paymac);
			}
			
			if(!CommUtil.equals(payacct.getBrchno(), acbrch)){
				throw InModuleError.InAccount.IN020006(paymac);
			}
			
			if(CommUtil.isNotNull(tblbusi.getOvbusi())&&!CommUtil.equals(payacct.getBusino(), tblbusi.getOvbusi())){
				throw InModuleError.InAccount.IN020007(paymac,payacct.getBusino(),tblbusi.getOvbusi());	
			}
			
		}else if(E_YES___.YES==isbein){
			//收息但未输入收息账号，默认账号本身
			if(CommUtil.isNotNull(tblbusi.getOvbusi())&&!CommUtil.equals(busino, tblbusi.getOvbusi())){
				throw InModuleError.InAccount.IN020008(tblbusi.getOvbusi());	
			}			
		}
		
		if(CommUtil.isNotNull(ovmony)&&CommUtil.compare(ovmony, BigDecimal.ZERO)<0){
			
			throw InModuleError.InAccount.IN020009();
		}		
		KnpParameter tbl_KnpParameter = KnpParameterDao.selectOne_odb1("InParm.inopen", busino, "%", "%", false);
		if(CommUtil.isNotNull(tbl_KnpParameter)){
			if("1".equals(tbl_KnpParameter.getParm_value1())){
				throw InModuleError.InAccount.IN020010();
			}
		}
		
	}



}
