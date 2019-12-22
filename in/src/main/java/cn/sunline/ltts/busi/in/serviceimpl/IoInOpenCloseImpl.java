package cn.sunline.ltts.busi.in.serviceimpl;

import java.math.BigDecimal;
import java.util.Map;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.inner.InnerAcctQry;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcctDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAmbl;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAmblDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnbOcac;
import cn.sunline.ltts.busi.in.tables.In.GlKnbOcacDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnpBusi;
import cn.sunline.ltts.busi.in.type.InQueryTypes.InacProInfo;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.in.IoInOpenCloseComplex.IoInacOpen_IN;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ISOPEN;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_OPACTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_RLBLTG;
 /**
  * 内部户开销户
  *
  */
@cn.sunline.adp.core.annotation.Generated
public class IoInOpenCloseImpl implements cn.sunline.ltts.busi.iobus.servicetype.in.IoInOpenClose{
 /**
  * 内部账户开户
  *
  */
	@Override
	public String inacOpen( IoInacOpen_IN inacopIn) {
		
		//账户机构未传值默认当前交易机构
		String acbrch = CommUtil.nvl(inacopIn.getAcbrch(), CommTools.getBaseRunEnvs().getTrxn_branch());
		inacopIn.setAcbrch(acbrch);
		//机构合法性检查
		SysUtil.getRemoteInstance(IoSrvPbBranch.class).inspectBranchLegality(acbrch, inacopIn.getCrcycd());
		String rlacct = InnerAcctQry.addInAcct(inacopIn).getAcctno();
		return rlacct;
	}

	@Override
	public void ioInacClose(String acctno) {
		
		GlKnaAcct acct = GlKnaAcctDao.selectOneWithLock_odb1(acctno, false);
		if(CommUtil.isNull(acct)){
			throw InError.comm.E0003("内部户["+acctno+"]不存在");
		}
		if(acct.getAcctst()==E_INACST.CLOSED){
			throw InError.comm.E0003("内部户["+acctno+"]已经是销户状态");
		}
		if(!(CommUtil.equals(acct.getDrctbl() , BigDecimal.ZERO) && CommUtil.equals(acct.getCrctbl() , BigDecimal.ZERO))){
			throw InError.comm.E0003("内部户["+acctno+"]余额不等于零，不能销户");
		} 
		// 销户检查非实时余额  change by cjk 20180518
		if(acct.getRlbltg() == E_RLBLTG._2){
			Map<String, Object> ResultMap = InacSqlsDao.qrySumRlbl(acctno, false);
			if (CommUtil.isNotNull(ResultMap) && !(CommUtil.equals(ConvertUtil.toBigDecimal(ResultMap.get("tranam")) , BigDecimal.ZERO))) {
				throw InError.comm.E0003("内部户["+acctno+"]余额不等于零，不能销户");
			}
		}
		
		if(E_INACTP.BASE==acct.getInactp()){
			throw InError.comm.E0003("内部户["+acctno+"]不允许手工销户");
		}
		if(!CommUtil.equals(acct.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch())){
			throw InError.comm.E0003("内部户["+acctno+"]非交易机构所属内部，不允许销户！");
		}
		GlKnaAmbl ambl=GlKnaAmblDao.selectOne_odb1(acctno, false);
		if(null!=ambl&&(CommUtil.compare(ambl.getAcmlbl(), BigDecimal.ZERO)>0||CommUtil.compare(ambl.getOvacml(), BigDecimal.ZERO)>0)){
			throw InError.comm.E0003("该内部账尚未结息处理，请先做内部账单户结息");
		}
		//修改状态
		acct.setAcctst(E_INACST.CLOSED);
		GlKnaAcctDao.updateOne_odb1(acct);
		//登记开销户登记簿
		GlKnbOcac tblocac = GlKnbOcacDao.selectOne_odb1(acctno, false);
		if(CommUtil.isNull(tblocac)){
			throw InError.comm.E0003("内部户["+acctno+"]开户记录不存在");
		}
		//add by sh 20170731 非手工开户的不允许销户
		if(E_OPACTP._1 != tblocac.getOpactp()){
		    throw InError.comm.E0003("内部户["+acctno+"]非手工开户，不允许销户！");
		}
		tblocac.setClactp(E_CLACTP._1);
		tblocac.setClbrno(CommTools.getBaseRunEnvs().getTrxn_branch());
		tblocac.setClosdt(CommTools.getBaseRunEnvs().getTrxn_date());
		tblocac.setClossq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		tblocac.setCloser(CommTools.getBaseRunEnvs().getTrxn_teller());
		GlKnbOcacDao.updateOne_odb1(tblocac);
	}

	@Override
	public IoInacInfo ioQueryAndOpen(String busino, String acbrch,
			String subsac, String crcycd) {

		String acctno ="";
        InacProInfo info = SysUtil.getInstance(InacProInfo.class);
        info = InnerAcctQry.qryAcctPro( crcycd, acbrch,busino, null,null,subsac);
        //账户不存，则新开账户在
        if (info.getIsexis() == E_YES___.NO) {
        	IoInacOpen_IN inacopIn = SysUtil.getInstance(IoInacOpen_IN.class);
        	
        	inacopIn.setAcbrch(acbrch);
        	inacopIn.setBusino(busino);
        	inacopIn.setCrcycd(crcycd);
        	inacopIn.setSubsac(subsac);
        	
        	acctno=InnerAcctQry.addInAcct(inacopIn).getAcctno();
        }else{
        	acctno=info.getAcctno();
        }
        GlKnaAcct knaAcctTmp = InQuerySqlsDao.sel_GlKnaAcct_by_acct(acctno, true);
        
        IoInacInfo  ioInacInfo = SysUtil.getInstance(IoInacInfo.class); 
        CommUtil.copyProperties(ioInacInfo, knaAcctTmp);
        
    	//内部户余额返回 add by chenjk 20161221
        ioInacInfo.setOnlnbl(InnerAcctQry.queryInnerAcctApi(acctno).getOnlnbl());
        
		return ioInacInfo;
	}

	/**
	 * 
	 * @param acctno
	 * 2016年12月8日-下午4:33:06
	 * @auther chenjk
	 */
	@Override
	public void ioInacReopen(String acctno) {
		
		if(CommUtil.isNull(acctno)){
			throw InError.comm.E0003("输入内部户不能为空");
		}
		
		//内部户账户规则验证
		GlKnaAcct acct = GlKnaAcctDao.selectOneWithLock_odb1(acctno, false);
		if(CommUtil.isNull(acct)){
			throw InError.comm.E0003("本机构下，内部户["+acctno+"]不存在");
		}
		if(acct.getAcctst()!=E_INACST.CLOSED){
			throw InError.comm.E0003("内部户["+acctno+"]非销户状态");
		}
		if(!CommUtil.equals(acct.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch())){
			throw InError.comm.E0003("内部户["+acctno+"]非交易机构所属内部，不允许重启！");
		}
		if(acct.getInactp() != E_INACTP.MANUAL){
			throw InError.comm.E0003("内部户["+acctno+"]账户类型非手工账户，不允许重启");
		}
		
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		//内部户产品规则验证
		String busino = acct.getBusino();
		GlKnpBusi tblBusi = InQuerySqlsDao.selBusinoDetail(acct.getBusino(), corpno, false);
		if(CommUtil.isNull(tblBusi)){
			
			throw InError.comm.E0003("查询业务代码表出错，业务编码["+busino+"]不存在");
		}
		if(tblBusi.getBusist() != E_PRODST.NORMAL){
			throw InError.comm.E0003("产品编码["+busino+"]非生效状态，不能开立内部户！");
		}
		//add by xionglz 3-手工单账户类型销户后不允许重启
		if(tblBusi.getIsopen() == E_ISOPEN._3){
			throw InError.comm.E0003("产品编码["+busino+"]为手工单账户，不允许销户重启！");
		}
		//修改状态
		acct.setAcctst(E_INACST.NORMAL);
		GlKnaAcctDao.updateOne_odb1(acct);
		//登记开销户登记簿
		GlKnbOcac tblocac = GlKnbOcacDao.selectOne_odb1(acctno, false);
		if(CommUtil.isNull(tblocac)){
			throw InError.comm.E0003("内部户["+acctno+"]开户记录不存在");
		}
		if (!CommUtil.equals(tblocac.getClosdt(),CommTools.getBaseRunEnvs().getTrxn_date())) {
			throw InError.comm.E0003("只允许当日销户当日重启");
		}
		tblocac.setClactp(null);
		tblocac.setClbrno("");
		tblocac.setClosdt("");
		tblocac.setClossq("");
		tblocac.setCloser("");
//		tblocac.setMtdate(CommTools.getBaseRunEnvs().getTrxn_date()); //维护日期
		tblocac.setTmstmp(DateTools2.getCurrentTimestamp());
		GlKnbOcacDao.updateOne_odb1(tblocac);
		
	}

	public cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo IoQueryAndOpenAdm( String busino,  String acbrch,  String subsac,  String crcycd){
		String acctno ="";
        InacProInfo info = SysUtil.getInstance(InacProInfo.class);
        info = InnerAcctQry.qryAcctPro( crcycd, acbrch,busino, null,null,subsac);
        //账户不存，则新开账户在
        if (info.getIsexis() == E_YES___.NO) {
        	IoInacOpen_IN inacopIn = SysUtil.getInstance(IoInacOpen_IN.class);
        	
        	inacopIn.setAcbrch(acbrch);
        	inacopIn.setBusino(busino);
        	inacopIn.setCrcycd(crcycd);
        	inacopIn.setSubsac(subsac);
        	
        	acctno=InnerAcctQry.addInAcct(inacopIn).getAcctno();
        }else{
        	acctno=info.getAcctno();
        }
        GlKnaAcct knaAcctTmp = InQuerySqlsDao.sel_GlKnaAcct_by_acct(acctno, true);
        
        IoInacInfo  ioInacInfo = SysUtil.getInstance(IoInacInfo.class); 
        CommUtil.copyProperties(ioInacInfo, knaAcctTmp);
        
    	//内部户余额返回 add by chenjk 20161221
        ioInacInfo.setOnlnbl(InnerAcctQry.queryInnerAcctApi(acctno).getOnlnbl());
        
		return ioInacInfo;
	}

}

