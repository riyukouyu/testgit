package cn.sunline.ltts.busi.in.serviceimpl;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.busi.dp.errors.InModuleError;
import cn.sunline.ltts.busi.in.inner.InnerAcctQry;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InTranOutDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcctDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnlUpbl;
import cn.sunline.ltts.busi.in.tables.In.GlKnlUpblDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnpBusi;
import cn.sunline.ltts.busi.in.type.InQueryTypes.InBillInfo;
import cn.sunline.ltts.busi.in.type.InQueryTypes.InBillInfoOut;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInAcctTmp;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.errors.LnError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BEINTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ISPAYA;

/**
 * 内部户资料维护服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
public class UpInacInfoImpl implements
		cn.sunline.ltts.busi.in.servicetype.UpInacInfo {
	/**
	 * 修改内部户帐号信息
	 * 
	 */
	public void modInacName(
			final cn.sunline.ltts.busi.in.servicetype.UpInacInfo.ModInacName.Input Input,
			final cn.sunline.ltts.busi.in.servicetype.UpInacInfo.ModInacName.Output Output) {
		// 输入检查
		if (CommUtil.isNull(Input.getAcctno())) {
			throw InError.comm.E0003("输入帐号不能为空");
		}
		if (CommUtil.isNull(Input.getAcctna())) {
			throw InError.comm.E0003("输入帐号名称不能为空");
		}	
		if (CommUtil.isNull(Input.getBeintp())) {
			throw InError.comm.E0003("结息方式不能为空");
		}	
		if(CommUtil.isNull(Input.getIsbein())&&E_BEINTP._5!=Input.getBeintp()){
			throw InError.comm.E0003("结息方式不为【不结息】结息是否入账必输！");
		}
		if(E_BEINTP._5 == Input.getBeintp() && CommUtil.isNotNull(Input.getIsbein())){
			
			throw InError.comm.E0003("结息方式为不结息，结息是否入账跳过不输入！");
		}
		if(E_YES___.YES==Input.getIsbein()){
			if(CommUtil.isNull(Input.getInrate())){
				throw InError.comm.E0003("结息入账时，利率必输！");
			}			
			if(CommUtil.isNull(Input.getOvrate())){
				throw InError.comm.E0003("结息入账时，透支利率必输！");
			}			
			if(CommUtil.isNull(Input.getReveac())){
				throw InError.comm.E0003("结息入账时，收息账号必输！");
			}			
			if(CommUtil.isNull(Input.getPaymac())){
				throw InError.comm.E0003("结息入账时，付息账号必输！");
			}			
		}
		
		GlKnaAcct acct = SysUtil.getInstance(GlKnaAcct.class);

		try {
			acct = GlKnaAcctDao.selectOne_odb1(Input.getAcctno(), true);
		} catch (Exception e) {
			throw InError.comm.E0003("查询内部户帐号失败，其他错误");
		}		
		if (acct.getAcctst() != E_INACST.NORMAL) {
			throw InError.comm.E0003("内部户[" + Input.getAcctno()
					+ "]状态不正常，无法完成交易");
		}
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		GlKnpBusi tblbusi = InQuerySqlsDao.selBusinoDetail(acct.getBusino(),corpno, true);
		//收息账号不为空检查账户
		if(CommUtil.isNotNull(Input.getReveac())){		
			
			GlKnaAcct reacct = InQuerySqlsDao.sel_GlKnaAcct_by_acct(Input.getReveac(), true);
			
			if(reacct.getAcctst()!=E_INACST.NORMAL){
				throw InError.comm.E0003("收息账号["+Input.getReveac()+"]状态不正常！");
			}
			if(!CommUtil.equals(reacct.getBrchno(), acct.getBrchno())){
				throw InError.comm.E0003("收息账号["+Input.getReveac()+"]维护账户机构非同一机构！");	
			}
			if(CommUtil.isNotNull(tblbusi.getNobusi())&&!CommUtil.equals(reacct.getBusino(), tblbusi.getNobusi())){
				
				throw InError.comm.E0003("收息账号["+Input.getReveac()+"]产品["+reacct.getBusino()+"]和维护产品对应的正常利息归属业务代码["+tblbusi.getNobusi()+"]不匹配！");	
			}
			
			
		}				
		if(CommUtil.isNotNull(Input.getPaymac())){
		
			
			GlKnaAcct payacct = InQuerySqlsDao.sel_GlKnaAcct_by_acct(Input.getPaymac(), true);
			
			if(payacct.getAcctst()!=E_INACST.NORMAL){
				
				throw InError.comm.E0003("付息账号["+Input.getPaymac()+"]状态不正常！");
			}
			
			if(!CommUtil.equals(payacct.getBrchno(),  acct.getBrchno())){
				
				throw InError.comm.E0003("付息账号["+Input.getPaymac()+"]和维护账户非同一机构！");
			}
			
			if(CommUtil.isNotNull(tblbusi.getOvbusi())&&!CommUtil.equals(payacct.getBusino(), tblbusi.getOvbusi())){
				
				throw InError.comm.E0003("付息账号["+Input.getPaymac()+"]产品["+payacct.getBusino()+"]和维护账号产品对应的透支利息归属业务代码["+tblbusi.getOvbusi()+"]不匹配！");	
			}
					
		}	
		if(CommUtil.isNotNull(Input.getOvmony())&&CommUtil.compare(Input.getOvmony(), BigDecimal.ZERO)<0){
			throw InError.comm.E0003("透支限额不能小于零！");
		}
		
		String oldnam = acct.getAcctna();
		// 修改
		acct.setAcctna(Input.getAcctna());
		acct.setOvmony(Input.getOvmony());
		if(CommUtil.isNull(Input.getIspaya())){
			throw InError.comm.E0003("是否销账管理 不能输入空值！");
		}
		//modify by wuzx 控制内部户修改 beg
		/*if(E_ISPAYA._9!=tblbusi.getIspaya()&&acct.getIspaya()!=Input.getIspaya()){
			throw InError.comm.E0003("是否采用销账管理 原值非  【任意】，不允许修改！");
		}*/
		IoInAcctTmp knaAcctTmp = InnerAcctQry.queryInnerAcctApi(Input.getAcctno());
		if(E_ISPAYA._1==acct.getIspaya()&&Input.getIspaya() == E_ISPAYA._0){
			if(CommUtil.compare(knaAcctTmp.getOnlnbl(), BigDecimal.ZERO)!=0){
				throw InError.comm.E0003("存在未销账信息，不允许修改！");
			}
		}
		if(E_ISPAYA._0==acct.getIspaya()&&Input.getIspaya() == E_ISPAYA._1){
			if(CommUtil.compare(knaAcctTmp.getOnlnbl(), BigDecimal.ZERO)!=0){
				throw InError.comm.E0003("内部户余额不为零，不允许修改！");
			}
		}
		//modify by wuzx 控制内部户修改 end
		if(E_ISPAYA._9==tblbusi.getIspaya()&&Input.getIspaya()==E_ISPAYA._0&&acct.getIspaya()==E_ISPAYA._1){
			
			int  count = InTranOutDao.selKnsPayaEffectCount(acct.getAcctno(), false);
			if(count>0){
				throw InError.comm.E0003("账号下存在未销账记录数["+count+"]，不允许修改为非挂账账管理！");
			}
		}
		if(E_BEINTP._5 == Input.getBeintp()){
			acct.setIsbein(null);
		}
		acct.setIspaya(Input.getIspaya());
		acct.setBeintp(Input.getBeintp());
		acct.setIsbein(Input.getIsbein());
		//modify by wuzx 修改的时候结息为否 beg
		if(E_YES___.NO==Input.getIsbein()){
			acct.setInrate(BigDecimal.ZERO);
			acct.setOvrate(BigDecimal.ZERO);
			acct.setReveac("");
			acct.setPaymac("");
		}
		//modify by wuzx 修改的时候结息为否 end
		acct.setInrate(Input.getInrate());
		acct.setOvrate(Input.getOvrate());
		acct.setReveac(CommUtil.nvl(Input.getReveac(),acct.getReveac()));
		acct.setPaymac(CommUtil.nvl(Input.getPaymac(),acct.getPaymac()));
		GlKnaAcctDao.updateOne_odb1(acct);
		
		GlKnlUpbl entity = SysUtil.getInstance(GlKnlUpbl.class);
		if (!CommUtil.equals(acct.getAcctna(), Input.getAcctna())) {//户名修改
			entity.setAcctno(Input.getAcctno());
			entity.setAfdesc(Input.getAcctna());
			entity.setBfdesc(oldnam);
			entity.setMnitem("acctna");
			entity.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());
			entity.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
			entity.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());
			entity.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			//entity.setTranno(Long.parseLong(SequenceManager.nextval("gl_knl_upbl")));
			entity.setTranno(Long.parseLong(CoreUtil.nextValue("gl_knl_upbl")));
			GlKnlUpblDao.insert(entity);
		}		

		Output.setAcctna(Input.getAcctna());
		Output.setAcctno(acct.getAcctno());
		Output.setOldnam(oldnam);
	}

	/**
	 * 分页查询内部户交易明细
	 * 
	 * @param acctno
	 *            总账账号
	 * @param bgdate
	 *            开始日期
	 * @param endate
	 *            结束日期
	 * @param pageno
	 *            当前页码
	 * @param pagesize
	 *            每页数量
	 */
	@Override
	public InBillInfoOut selBillInfo(String acctno, String bgdate,
			String endate, Long pageno, Long pagesize, String tranus,
			String toacct) {
		
		int totlCount = 0;
		// 传入参数检查
		if (CommUtil.isNull(pageno)) {
			throw InModuleError.InAccount.IN010013();
		}
		if (CommUtil.isNull(pagesize)) {
			throw InModuleError.InAccount.IN010014();
		}
		if (CommUtil.isNull(acctno)) {
			throw InModuleError.InAccount.IN010015();
		}
		if (CommUtil.isNull(bgdate)) {
			throw InModuleError.InAccount.IN010016();
		}
		if (CommUtil.compare(bgdate, CommTools.getBaseRunEnvs().getTrxn_date())>0) {
			throw InModuleError.InAccount.IN010017();
		}
		
		if (CommUtil.isNull(endate)) {
			throw InModuleError.InAccount.IN010018();
		}
		if (CommUtil.compare(bgdate, endate)>0) {
			throw InModuleError.InAccount.IN010019();
		}
		
		InBillInfoOut bInfos = SysUtil.getInstance(InBillInfoOut.class);

		Options<InBillInfo> lsinfos = new DefaultOptions<InBillInfo>();

		Page<InBillInfo> info = InQuerySqlsDao.selBillInfos(bgdate,endate, acctno,tranus,toacct, 
				(pageno - 1) * pagesize, pagesize,totlCount,false);	
		
		long counts = 0l;
		
		if(CommUtil.isNotNull(info)){
			 counts = info.getRecordCount();
			 
			for(InBillInfo blinfo : info.getRecords()){
				InBillInfo inbill = SysUtil.getInstance(InBillInfo.class);
				CommUtil.copyProperties(inbill, blinfo);
				lsinfos.add(inbill);
			}
		}
		
		bInfos.setBillInfos(lsinfos);
		CommTools.getBaseRunEnvs().setTotal_count(counts);
		return bInfos;
	}
    
}
