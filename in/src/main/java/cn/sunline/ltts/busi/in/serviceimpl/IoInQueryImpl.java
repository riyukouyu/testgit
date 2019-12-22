package cn.sunline.ltts.busi.in.serviceimpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.util.sequ.MsSeqUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.busi.dp.errors.InModuleError;
import cn.sunline.edsp.busi.jfpal.ngp.aplt.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCorp;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpCopyProdDao;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.in.inner.InnerAcctQry;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAmbl;
import cn.sunline.ltts.busi.in.tables.In.GlKnaLsbl;
import cn.sunline.ltts.busi.in.tables.In.GlKnaLsblDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnbOcac;
import cn.sunline.ltts.busi.in.tables.In.GlKnpBusi;
import cn.sunline.ltts.busi.in.tables.In.GlKnpBusiDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnpBztp;
import cn.sunline.ltts.busi.in.tables.In.GlKnpBztpDao;
import cn.sunline.ltts.busi.in.tables.In.KnbCbtlBatch;
import cn.sunline.ltts.busi.in.tables.In.KnbCbtlBatchDao;
import cn.sunline.ltts.busi.in.tables.In.KnsPaya;
import cn.sunline.ltts.busi.in.tables.In.KnsPayaDao;
import cn.sunline.ltts.busi.in.tables.In.KnsPayd;
import cn.sunline.ltts.busi.in.tables.In.KnsPaydDao;
import cn.sunline.ltts.busi.in.type.InDayEndTypes.InacTranam;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery.IoInsBusinoInfo.Input;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery.IoInsBusinoInfo.Output;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoknblttlbatchInfo;
import cn.sunline.ltts.busi.iobus.type.IoInTable.IoInKnsPaya;
import cn.sunline.ltts.busi.iobus.type.IoInTable.IoInKnsPayd;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.InacInfoList;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoBusinoInfo;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoBusinoInfoList;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoBusinoName;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoBzprtpAllInfo;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoBzprtpAllInfoList;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoBzprtpInfo;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoBzprtpInfoList;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoClrInfo;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInAcctTmp;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacLastbl;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacTranam;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoItemcdInfo;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoItemcdInfoList;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoPayaSttInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.errors.PbError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_QURYTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRTRTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BEINTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BUSITP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ISPAYA;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYAST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_RLBLTG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_TRANWY;


 /**
  * 内部户查询服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
public class IoInQueryImpl implements cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery{
 /**
  * 内部户信息查询
  *
  */
	public cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo InacInfoQuery( String acctno){
		return InnerAcctQry.qryInacInfoByAcctno(acctno);
	}

@Override
public InacInfoList selAllInacInfo(Integer start, Integer pagect) {
	List<IoInacInfo> list = InQuerySqlsDao.QryAllInac(start, pagect, false);
	int count = InQuerySqlsDao.SelAccoutInac(false);
	InacInfoList reData = SysUtil.getInstance(InacInfoList.class);
	for(IoInacInfo info : list){
		IoInAcctTmp knaAcctTmp = InnerAcctQry.queryInnerAcctApi(info.getAcctno());
		info.setBlncdn(knaAcctTmp.getBlncdn());//余额方向
		info.setDrctbl(knaAcctTmp.getDrctbl());//联机借方余额
		info.setCrctbl(knaAcctTmp.getCrctbl());//联机贷方余额
		
		reData.getInfos().add(info);
	}
	reData.setCount(Long.valueOf(count));
	return reData;
}

    /**
     * 分页查询业务编码信息
     */
    @Override
    public IoBusinoInfoList selBusinoInfoWithPage(Integer starno, Integer pageno, String busino, String busina) {
        IoBusinoInfoList businoInfos = SysUtil.getInstance(IoBusinoInfoList.class);
        RunEnvs runEnvs = BusiTools.getBusiRunEnvs();
        if (CommUtil.isNull(starno)) {
            starno = runEnvs.getPage_start().intValue();
        }
        if (CommUtil.isNull(pageno)) {
            pageno = runEnvs.getPage_size().intValue();
        }
        // 业务编码信息
        Page<GlKnpBusi> tblGlKnpBusis = InQuerySqlsDao.selBusinoInfoWithPage(busino, busina, (starno - 1L) * pageno,
            pageno, runEnvs.getTotal_count(), false);
        for (GlKnpBusi tblGlKnpBusi : tblGlKnpBusis.getRecords()) {
            IoBusinoInfo businoInfo = SysUtil.getInstance(IoBusinoInfo.class);
            CommUtil.copyProperties(businoInfo, tblGlKnpBusi, false);
            businoInfos.getBusinoInfos().add(businoInfo);
        }

        businoInfos.setCount((int)tblGlKnpBusis.getRecordCount());
        BusiTools.getBusiRunEnvs().setTotal_count(tblGlKnpBusis.getRecordCount());

        return businoInfos;
    }

/**
 * 维护业务编码信息
 */
@Override
public void updBusinoInfo(cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery.IoUpdBudinoInfo.Input input,
		cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery.IoUpdBudinoInfo.Output output) {
	// 查询产品新增柜员
	String tranus2 = InacSqlsDao.selUserByBusino(E_BUSIBI.INNE,input.getBusino(),E_PRTRTP.ADD, true);
	String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//当前系统日期
	String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();//机构
	String tranus1 = CommTools.getBaseRunEnvs().getTrxn_teller();//维护柜员
	String Clerbr = BusiTools.getBusiRunEnvs().getCentbr();//获取省联社清算中心
	IoInQuery ioInQuery = SysUtil.getInstance(IoInQuery.class);
	
	Integer count = InacSqlsDao.SelAccoutInacByBusino(input.getBusino(), false);
	Integer glvccount = InacSqlsDao.SelGLvcByBusino(CommTools.getBaseRunEnvs().getTrxn_date(),input.getBusino(), false);

	
	IoBusinoInfo glKnaBusi = ioInQuery.selBusiIndoByBusino(input.getBusino());
	//add by wuzx 20161025 -内部账产品修改规则 beg
	if(glKnaBusi.getBusist() == E_PRODST.DISA){
		throw PbError.Branch.E0002("装配停用状态不能修改！");
	}
	if(glKnaBusi.getBusist() == E_PRODST.ASEN || 
			glKnaBusi.getBusist() == E_PRODST.ASSE){
		if(!CommUtil.equals(tranus1,tranus2)){
			throw PbError.Branch.E0002("装配录入、产品装配的修改操作柜员必须与录入柜员相一致!");
		}
	}
	if(CommUtil.compare(trandt, glKnaBusi.getBgdate())>=0 && 
			!CommUtil.equals(input.getBgdate(), glKnaBusi.getBgdate())){
		//启用日期早于系统日期，启用日期不允许修改
		throw PbError.Branch.E0002("当前系统日期大于产品启用日期，无法修改启用日期！");
	}
	if(!CommUtil.equals(input.getBusina(), glKnaBusi.getBusina())){
	
		throw PbError.Branch.E0002("业务代码中文名称不能修改！");
	}
	
	//add by wuzx 20161025 -内部账产品修改规则 end
	if(!CommUtil.equals(tranbr,Clerbr)){
		throw PbError.Branch.E0002("非省清算中心不允许操作！");
	}	
	if(CommUtil.isNull(input.getBusino())){
		throw InError.comm.E0003("业务代码不能为空！");
	}	
	if(CommUtil.isNull(input.getBusina())){
		throw InError.comm.E0003("业务代码码中文名称不能为空！");
	}	
	if(CommUtil.isNull(input.getBusitp())){
		throw InError.comm.E0003("核算性质不能为空！");
	}	
	if(CommUtil.isNull(input.getBlncdn())){
		throw InError.comm.E0003("余额方向不能为空！");
	}	
	
	if(CommUtil.isNull(input.getPmodtg())){
		throw InError.comm.E0003("余额红字标志不能为空！");
	}	
	if(CommUtil.isNull(input.getIspaya())){
		throw InError.comm.E0003("是否采用销账管理不能为空！");
	}	
	
	if(CommUtil.isNull(input.getRmsign())){
		throw InError.comm.E0003("人民币专用标志不能为空！");
	}	
	
	if(CommUtil.isNull(input.getFnsign())){
		throw InError.comm.E0003("外币专用标志不能为空！");
	}	

	if(input.getRmsign()==E_YES___.YES && input.getFnsign()==E_YES___.YES){
		
		throw InError.comm.E0003("人民币专用标志和外币专用标志不能同时为是！");
	}
	
	if(CommUtil.isNull(input.getIsbein())){
		throw InError.comm.E0003("计息标志不能为空！");
	}
	
	if(CommUtil.isNull(input.getBeintp())){
		throw InError.comm.E0003("结清标志不能为空！");
	}	
	
	if(CommUtil.isNull(input.getIsopen())){
		throw InError.comm.E0003("是否允许开内部账不能为空！");
	}	
	
	if(CommUtil.isNull(input.getProvop())){
		throw InError.comm.E0003("省中心设立标志不能为空！");
	}	
	
	if(CommUtil.isNull(input.getCounop())){
		throw InError.comm.E0003("县中心设立标志不能为空！");
	}	
	
	if(CommUtil.isNull(input.getOffiop())){
		throw InError.comm.E0003("办事处设立标不能为空！");
	}	
	
	
	if(CommUtil.isNull(input.getCredop())){
		throw InError.comm.E0003("信用社设立标志不能为空！");
	}	
	
	if(CommUtil.isNull(input.getKpacfg())){
		throw InError.comm.E0003("入账方式不能为空！");
	}	
	
	
	if(CommUtil.isNull(input.getRlbltg())){
		throw InError.comm.E0003("入分户标志不能为空！");
	}	
	
	
	if(CommUtil.isNull(input.getEtactp())){
		throw InError.comm.E0003("入明细账方式不能为空！");
	}	
	
	if(CommUtil.isNull(input.getCractp())){
		throw InError.comm.E0003("冲销标志不能为空！");
	}	
	if(CommUtil.isNull(input.getBlncdn())){
		throw InError.comm.E0003("余额方向不能为空！");
	}
	if(count >0 && input.getBlncdn()!= glKnaBusi.getBlncdn()){
		
		throw PbError.Branch.E0002("产品下存在有效账户，不允许修改余额方向！");
	}
	if(glvccount >0 && input.getRlbltg()!= glKnaBusi.getRlbltg()&&E_RLBLTG._1==input.getRlbltg()){
		
		throw PbError.Branch.E0002("产品下当日已经有账户发生交易，余额更新方式不能修改成实时更新！");
	}

	/*IoKnbParaMenuInfo menu = SysUtil.getInstance(IoDpSrvQryTableInfo.class).getKnbParaMenuOdb1(input.getBusino());
	if(menu.getAbute1().equals("D")&&input.getBlncdn()!=E_BLNCDN.D
			||menu.getAbute1().equals("C")&&input.getBlncdn()!=E_BLNCDN.C){
		
		throw InError.comm.E0003("余额方向输入错误！");
	}*/
	
	if(E_BLNCDN.C!=input.getBlncdn() && 
	   E_BLNCDN.D!=input.getBlncdn() && 
	   E_ISPAYA._0!=input.getIspaya()){
		throw InError.comm.E0003("余额方向非借方或贷方，一定不能是挂销账管理！");
	}	
	String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
	//收息
	if(input.getIsbein()==E_YES___.YES){
		
		//正常利息归属业务代码检查
		if(CommUtil.isNull(input.getNobusi())){
			
			throw InError.comm.E0003(" ");
		}else{
			
			if(!CommUtil.equals(input.getNobusi(), input.getBusino())){

				GlKnpBusi tblbusi = InQuerySqlsDao.selBusinoDetail(input.getNobusi(),corpno, false);
				if(CommUtil.isNull(tblbusi)){
					
					throw InError.comm.E0003("查询产品代码表出错，产品编码["+input.getNobusi()+"]不存在");
				}
				if(tblbusi.getBusist()!=E_PRODST.NORMAL){
					
					throw InError.comm.E0003("产品编码["+input.getNobusi()+"]非生效状态！");
				}				
						
			}			
		}
		//透支利息归属业务代码检查
		if(CommUtil.isNull(input.getOvbusi())){
			
			throw InError.comm.E0003("结息入账，正常利息归属业务代码必输！");
		}else{
			if(!CommUtil.equals(input.getOvbusi(), input.getBusino())){
				
				GlKnpBusi tblbusi = InQuerySqlsDao.selBusinoDetail(input.getOvbusi(),corpno, false);
				if(CommUtil.isNull(tblbusi)){
					
					throw InError.comm.E0003("查询产品代码表出错，产品编码["+input.getOvbusi()+"]不存在");
				}
				if(tblbusi.getBusist()!=E_PRODST.NORMAL){
					
					throw InError.comm.E0003("产品编码["+input.getOvbusi()+"]非生效状态！");
				}				
			}
						
		}		
		
	}	
	
	String busino = input.getBusino();
	GlKnpBusi tblGlKnpBusi = InQuerySqlsDao.selBusinoDetail(busino,corpno, true);
	

	if(tblGlKnpBusi.getCractp() != input.getCractp()||tblGlKnpBusi.getKpacfg() != input.getKpacfg()
			||tblGlKnpBusi.getRlbltg()!= input.getRlbltg()){		
		
		  InacSqlsDao.updateGlAcctCractpByBusino(busino, input.getCractp(), DateTools2.getCurrentTimestamp(), input.getKpacfg(), input.getRlbltg());
	}
	tblGlKnpBusi.setEtactp(input.getEtactp());
	tblGlKnpBusi.setBlncdn(input.getBlncdn());
	tblGlKnpBusi.setBusina(input.getBusina());     
	tblGlKnpBusi.setBusien(input.getBusien());     
	tblGlKnpBusi.setBusitp(input.getBusitp());     
	tblGlKnpBusi.setPmodtg(input.getPmodtg());     
	tblGlKnpBusi.setIspaya(input.getIspaya());     
	tblGlKnpBusi.setRmsign(input.getRmsign());     
	tblGlKnpBusi.setFnsign(input.getFnsign());     
	tblGlKnpBusi.setIsbein(input.getIsbein());     
	tblGlKnpBusi.setBeintp(input.getBeintp());     
	tblGlKnpBusi.setNobusi(input.getNobusi());     
	tblGlKnpBusi.setOvbusi(input.getOvbusi());     
	tblGlKnpBusi.setIsopen(input.getIsopen());     
	tblGlKnpBusi.setProvop(input.getProvop());     
	tblGlKnpBusi.setCounop(input.getCounop());     
	tblGlKnpBusi.setOffiop(input.getOffiop());     
	tblGlKnpBusi.setCredop(input.getCredop());     
	tblGlKnpBusi.setKpacfg(input.getKpacfg());     
	tblGlKnpBusi.setRlbltg(input.getRlbltg());     
	tblGlKnpBusi.setCractp(input.getCractp());  
	tblGlKnpBusi.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());//交易柜员
	if(CommUtil.compare(input.getEddate(), CommTools.getBaseRunEnvs().getTrxn_date())<=0){
		throw InError.comm.E0003("失效日期不能小于当前系统日期！");
	}
	if(CommUtil.compare(input.getBgdate(), CommTools.getBaseRunEnvs().getTrxn_date())<=0 && glKnaBusi.getBusist() != E_PRODST.NORMAL){
		throw InError.comm.E0003("生效日期不能小于当前系统日期！");
	}
	if(CommUtil.compare(input.getEddate(), input.getBgdate())<=0){
		throw InError.comm.E0003("失效日期不能小于生效日期！");
	}
	
	if(glKnaBusi.getBusist() != E_PRODST.ASSE){
		String tyinno = "";// 15位录入编号
//		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();// 机构号 
		String sequence = "";// 顺序号
		sequence = BusiTools.getSequence("prod_tyinno", 4);
		tyinno = new StringBuilder().append(corpno).append(trandt)
				.append(sequence).toString();
		tblGlKnpBusi.setTyinno(tyinno);//录入编号
	}	
	tblGlKnpBusi.setBgdate(input.getBgdate());     
	tblGlKnpBusi.setEddate(input.getEddate());     
	tblGlKnpBusi.setItemcd(input.getItemcd());     
	tblGlKnpBusi.setSubsac(input.getSubsac()); 
	
	if(input.getBusitp()==E_BUSITP._6){
		tblGlKnpBusi.setIoflag(E_IOFLAG.OUT);
	}else{
		tblGlKnpBusi.setIoflag(E_IOFLAG.IN);
	}
	if(glKnaBusi.getBusist() == E_PRODST.ASSE){//装配下才返回录入编号
		output.setTyinno(tblGlKnpBusi.getTyinno()); 
	}
	
	GlKnpBusiDao.updateOne_odb1(tblGlKnpBusi);
	
	// 产品操作柜员登记
	SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.INNE, input.getBusino(), E_PRTRTP.MDFY);
}

/**
 * 新增业务编码信息
 */
@Override
public void insBusinoInfo(Input input, Output output) {
	
	/*List<IoKnsProdClerInfo> cplProdCler = SysUtil.getInstance(IoAccountSvcType.class).selKnsProdClerInfo(input.getCorpno(),input.getBusino(), null);
	
	if(CommUtil.isNull(cplProdCler)||cplProdCler.size()==0){
		
		throw InError.comm.E0003("内部户产品输入错误或未发布，不允许新增！");
	}*/
	
	/*IoKnbParaMenuInfo menu = SysUtil.getInstance(IoDpSrvQryTableInfo.class).getKnbParaMenuOdb1(input.getBusino());
	if(menu.getAbute1().equals("D")&&input.getBlncdn()!=E_BLNCDN.D
			||menu.getAbute1().equals("C")&&input.getBlncdn()!=E_BLNCDN.C){
		
		throw InError.comm.E0003("余额方向输入错误！");
	}*/

 	
	GlKnpBusi tblGlKnpBusiOld = InQuerySqlsDao.selBusinoDetail(input.getBusino(),input.getCorpno(), false);
	
	
	if(null!=tblGlKnpBusiOld&&CommUtil.isNotNull(tblGlKnpBusiOld.getBusino())){
		
		throw InError.comm.E0003("内部户产品已经存在！");
	}
	String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
	String Clerbr = BusiTools.getBusiRunEnvs().getCentbr();//获取省联社清算中心

	if(!CommUtil.equals(tranbr,Clerbr)){
		throw PbError.Branch.E0002("非省清算中心不允许操作！");
	}			
	if(CommUtil.compare(input.getBgdate(), CommTools.getBaseRunEnvs().getTrxn_date())<0){
		
		throw InError.comm.E0003("生效日期["+input.getBgdate()+"]不能小于当前系统日期["+CommTools.getBaseRunEnvs().getTrxn_date()+"]！");
	}
	if(CommUtil.compare(input.getEddate(), CommTools.getBaseRunEnvs().getTrxn_date())<0){
		
		throw InError.comm.E0003("失效日期不能小于当前系统日期！");
	}
	if(CommUtil.compare(input.getEddate(), input.getBgdate())<0){
		
		throw InError.comm.E0003("失效日期不能小于生效日期日期！");
	}
	if(CommUtil.isNull(input.getBusino())){
		
		throw InError.comm.E0003("产品编码不能为空！");
	}	
	if(CommUtil.isNull(input.getBusina())){
		
		throw InError.comm.E0003("业务代码码中文名称不能为空！");
	}	
	if(CommUtil.isNull(input.getBusitp())){
		
		throw InError.comm.E0003("核算性质不能为空！");
	}
	if(input.getBusitp() == E_BUSITP._5){
		
		throw InError.comm.E0003("暂不支持核算属性为损益！");
	}
	if(CommUtil.isNull(input.getBlncdn())){
		
		throw InError.comm.E0003("余额方向不能为空！");
	}	
	
	if(CommUtil.isNull(input.getPmodtg())){
		
		throw InError.comm.E0003("余额红字标志不能为空！");
	}	
	if(CommUtil.isNull(input.getIspaya())){
		
		throw InError.comm.E0003("是否采用销账管理不能为空！");
	}	
	
	if(CommUtil.isNull(input.getRmsign())){
		
		throw InError.comm.E0003("人民币专用标志不能为空！");
	}	
	
	if(CommUtil.isNull(input.getFnsign())){
		
		throw InError.comm.E0003("外币专用标志不能为空！");
	}	
	if(input.getRmsign()==E_YES___.YES&&input.getFnsign()==E_YES___.YES){
		
		throw InError.comm.E0003("人民币专用标志和外币专用标志不能同时为是！");
	}
	
	if(CommUtil.isNull(input.getIsbein())){
		throw InError.comm.E0003("计息标志不能为空！");
	}
	
	if(CommUtil.isNull(input.getBeintp())){
		
		throw InError.comm.E0003("结清标志不能为空！");
	}	
	
	if(CommUtil.isNull(input.getIsopen())){
		throw InError.comm.E0003("是否允许开内部账不能为空！");
	}	
	
	if(CommUtil.isNull(input.getProvop())){
		
		throw InError.comm.E0003("省中心设立标志不能为空！");
	}	
	
	if(CommUtil.isNull(input.getCounop())){
		
		throw InError.comm.E0003("县中心设立标志不能为空！");
	}	
	
	if(CommUtil.isNull(input.getOffiop())){
		throw InError.comm.E0003("办事处设立标不能为空！");
	}	
	
	
	if(CommUtil.isNull(input.getCredop())){
		
		throw InError.comm.E0003("信用社设立标志不能为空！");
	}	
	
	if(CommUtil.isNull(input.getKpacfg())){
		
		throw InError.comm.E0003("入账方式不能为空！");
	}	
	
	
	if(CommUtil.isNull(input.getRlbltg())){
		
		throw InError.comm.E0003("入分户标志不能为空！");
	}	
	
	
	if(CommUtil.isNull(input.getEtactp())){
		
		throw InError.comm.E0003("入明细账方式不能为空！");
	}	
	
	if(CommUtil.isNull(input.getCractp())){
		
		throw InError.comm.E0003("冲销标志不能为空！");
	}	
	if(CommUtil.isNull(input.getBlncdn())){
		
		throw InError.comm.E0003("余额方向不能为空！");
	}
	//收息
	if(input.getIsbein()==E_YES___.YES){
		
		//正常利息归属业务代码检查
		if(CommUtil.isNull(input.getNobusi())){
			
			throw InError.comm.E0003(" ");
		}else{
			
			if(!CommUtil.equals(input.getNobusi(), input.getBusino())){

				GlKnpBusi tblbusi = InQuerySqlsDao.selBusinoDetail(input.getNobusi(),input.getCorpno(), false);
				if(CommUtil.isNull(tblbusi)){
					
					throw InError.comm.E0003("查询产品代码表出错，产品编码["+input.getNobusi()+"]不存在");
				}
				if(tblbusi.getBusist()!=E_PRODST.NORMAL){
					
					throw InError.comm.E0003("产品编码["+input.getNobusi()+"]非生效状态！");
				}				
						
			}			
		}
		//透支利息归属业务代码检查
		if(CommUtil.isNull(input.getOvbusi())){
			
			throw InError.comm.E0003("结息入账，正常利息归属业务代码必输！");
		}else{
			if(!CommUtil.equals(input.getOvbusi(), input.getBusino())){
				
				GlKnpBusi tblbusi = InQuerySqlsDao.selBusinoDetail(input.getOvbusi(),input.getCorpno(), false);
				if(CommUtil.isNull(tblbusi)){
					
					throw InError.comm.E0003("查询产品代码表出错，产品编码["+input.getOvbusi()+"]不存在");
				}
				if(tblbusi.getBusist()!=E_PRODST.NORMAL){
					
					throw InError.comm.E0003("产品编码["+input.getOvbusi()+"]非生效状态！");
				}				
			}
						
		}		
		
	}	
	
	if(E_BLNCDN.C!=input.getBlncdn() &&E_BLNCDN.D!=input.getBlncdn()&&E_ISPAYA._0!=input.getIspaya()){
		throw InError.comm.E0003("余额方向非借方或贷方，一定不能是挂销账管理！");
	}
	GlKnpBusi tblGlKnpBusi = SysUtil.getInstance(GlKnpBusi.class);
	CommUtil.copyProperties(tblGlKnpBusi, input, false);	
	if(input.getBusitp()==E_BUSITP._6){
		tblGlKnpBusi.setIoflag(E_IOFLAG.OUT);
	}else{
		tblGlKnpBusi.setIoflag(E_IOFLAG.IN);
	}
	if(CommUtil.isNull(tblGlKnpBusi.getCorpno())){
		tblGlKnpBusi.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
	}
	tblGlKnpBusi.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());//交易柜员

	// add by liaojc 20161129 修改录入编号获取方式
	String tyinno = getBusiTyinno();
	
	tblGlKnpBusi.setTyinno(tyinno);//录入编号
	//add by wuzx 20161025 新增时候产生录入编号 end 
	tblGlKnpBusi.setBusist(E_PRODST.ASSE);
	
	GlKnpBusiDao.insert(tblGlKnpBusi);
	output.setTyinno(tyinno);//返回录入编号
	// 产品操作柜员登记
	SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.INNE, input.getBusino(), E_PRTRTP.ADD);
	 
}

/**
 * 查询属性类型信息，用于字典显示
 */
@Override
public IoBzprtpInfoList selBzprtpInfoForDict() {
	IoBzprtpInfoList bzprtpInfos = SysUtil.getInstance(IoBzprtpInfoList.class);
	List<IoBzprtpInfo> cplBzprtps = InQuerySqlsDao.selBzprtpInfoForDict(false);
	for(IoBzprtpInfo cplBzprtp : cplBzprtps){
		bzprtpInfos.getBzprtpInfo().add(cplBzprtp);
	}
	return bzprtpInfos;
}

/**
 * 查询科目信息，用于字典显示
 */
@Override
public IoItemcdInfoList selItemcdInfoForDict() {
	IoItemcdInfoList cplItemcdInfoList = SysUtil.getInstance(IoItemcdInfoList.class);
	List<IoItemcdInfo> itemcdInfos = InQuerySqlsDao.selItemcdInfoForDict(false);
	for(IoItemcdInfo cplItemcdInfo : itemcdInfos){
		cplItemcdInfoList.getItemcdInfos().add(cplItemcdInfo);
	}
	return cplItemcdInfoList;
}

/**
 * 分页查询属性类型信息
 */
@Override
public IoBzprtpAllInfoList selBzprtpInfoWithPage(Integer starno, Integer pageno) {
	IoBzprtpAllInfoList cplBzprtpInfoList = SysUtil.getInstance(IoBzprtpAllInfoList.class);
	int count = InQuerySqlsDao.selBzprtpCount(false);
	List<GlKnpBztp> tblGlKnpBztps = InQuerySqlsDao.selBzprtpInfoWithPage(starno, pageno, false);
	for(GlKnpBztp tblGlKnpBztp : tblGlKnpBztps){
		IoBzprtpAllInfo cplBzprtpInfo = SysUtil.getInstance(IoBzprtpAllInfo.class);
		cplBzprtpInfo.setBzprtp(tblGlKnpBztp.getBzprtp());
		cplBzprtpInfo.setPayatg(tblGlKnpBztp.getPayatg());
		cplBzprtpInfo.setPauntg(tblGlKnpBztp.getPauntg());
		cplBzprtpInfo.setRlbltg(tblGlKnpBztp.getRlbltg());
		cplBzprtpInfo.setOvdftg(tblGlKnpBztp.getOvdftg());
		cplBzprtpInfo.setTotltg(tblGlKnpBztp.getTotltg());
		cplBzprtpInfo.setKpacfg(tblGlKnpBztp.getKpacfg());
		cplBzprtpInfo.setBilltg(tblGlKnpBztp.getBilltg());
		cplBzprtpInfo.setUsdcmt(tblGlKnpBztp.getUsdcmt());
		cplBzprtpInfo.setUsdctg(tblGlKnpBztp.getUsdctg());
		cplBzprtpInfo.setTrcrlm(tblGlKnpBztp.getTrcrlm());
		cplBzprtpInfo.setTrdrlm(tblGlKnpBztp.getTrdrlm());
		cplBzprtpInfo.setDesctx(tblGlKnpBztp.getDesctx());
		cplBzprtpInfoList.getBzprtpInfo().add(cplBzprtpInfo);
	}
	cplBzprtpInfoList.setCount(count);
	return cplBzprtpInfoList;
}

/**
 * 新增属性类型信息
 */
@Override
public void insBzprtpInfo(IoBzprtpAllInfo cplBzprtpInfo) {
	String bzprtp = cplBzprtpInfo.getBzprtp();
	GlKnpBztp tblGlKnpBztp = SysUtil.getInstance(GlKnpBztp.class);
	
	tblGlKnpBztp.setBzprtp(bzprtp);
	tblGlKnpBztp.setPayatg(cplBzprtpInfo.getPayatg());
	tblGlKnpBztp.setPauntg(cplBzprtpInfo.getPauntg());
	tblGlKnpBztp.setRlbltg(cplBzprtpInfo.getRlbltg());
	tblGlKnpBztp.setOvdftg(cplBzprtpInfo.getOvdftg());
	tblGlKnpBztp.setTotltg(cplBzprtpInfo.getTotltg());
	tblGlKnpBztp.setKpacfg(cplBzprtpInfo.getKpacfg());
	tblGlKnpBztp.setBilltg(cplBzprtpInfo.getBilltg());
	tblGlKnpBztp.setUsdcmt(cplBzprtpInfo.getUsdcmt());
	tblGlKnpBztp.setUsdctg(cplBzprtpInfo.getUsdctg());
	tblGlKnpBztp.setTrcrlm(cplBzprtpInfo.getTrcrlm());
	tblGlKnpBztp.setTrdrlm(cplBzprtpInfo.getTrdrlm());
	tblGlKnpBztp.setDesctx(cplBzprtpInfo.getDesctx());
	
	GlKnpBztpDao.insert(tblGlKnpBztp);
	
}

/**
 * 根据业务编码查询相关信息
 */
@Override
public IoBusinoInfo selBusiIndoByBusino(String busino) {
	
	if(CommUtil.isNull(busino)){
		
		throw InError.comm.E0003("业务代码不能为空！");
	}
	IoBusinoInfo cplBusiInfo = SysUtil.getInstance(IoBusinoInfo.class);
	String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
	GlKnpBusi tblGlKnpBusi = InQuerySqlsDao.selBusinoDetail(busino,corpno, false);
	
	if(CommUtil.isNotNull(tblGlKnpBusi)){
		CommUtil.copyProperties(cplBusiInfo, tblGlKnpBusi, false);
	}
	
	return cplBusiInfo;
}

/**
 * 根据属性类型查询相关信息
 */
@Override
public IoBzprtpAllInfo selBztpInfoByBzprtp(String bzprtp) {
	IoBzprtpAllInfo cplBzprtpInfo = SysUtil.getInstance(IoBzprtpAllInfo.class);
	
	GlKnpBztp tblGlKnpBztp = GlKnpBztpDao.selectOne_odb1(bzprtp, false);
	
	if(CommUtil.isNotNull(tblGlKnpBztp)){
		cplBzprtpInfo.setBzprtp(tblGlKnpBztp.getBzprtp());
		cplBzprtpInfo.setPayatg(tblGlKnpBztp.getPayatg());
		cplBzprtpInfo.setPauntg(tblGlKnpBztp.getPauntg());
		cplBzprtpInfo.setRlbltg(tblGlKnpBztp.getRlbltg());
		cplBzprtpInfo.setOvdftg(tblGlKnpBztp.getOvdftg());
		cplBzprtpInfo.setKpacfg(tblGlKnpBztp.getKpacfg());
		cplBzprtpInfo.setTotltg(tblGlKnpBztp.getTotltg());
		cplBzprtpInfo.setBilltg(tblGlKnpBztp.getBilltg());
		cplBzprtpInfo.setUsdcmt(tblGlKnpBztp.getUsdcmt());
		cplBzprtpInfo.setUsdctg(tblGlKnpBztp.getUsdctg());
		cplBzprtpInfo.setTrcrlm(tblGlKnpBztp.getTrcrlm());
		cplBzprtpInfo.setTrdrlm(tblGlKnpBztp.getTrdrlm());
		cplBzprtpInfo.setDesctx(tblGlKnpBztp.getDesctx());
		
	}
	
	return cplBzprtpInfo;
}

@Override
public cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInAcctTmp InacBalQuery(
		String acctno) {
	IoInAcctTmp tmp = InnerAcctQry.queryInnerAcctApi(acctno);
	return tmp;
}

/**
 * 根据内部户查询内部户上日余额
 */
public IoInacLastbl selInacLastblQuery(String acctno) {
	IoInacLastbl inaclLastbl  = SysUtil.getInstance(IoInacLastbl.class);
	 GlKnaLsbl tbgl_kns_lsbl =  GlKnaLsblDao.selectOne_odb1(acctno, true);
	 CommUtil.copyProperties(inaclLastbl, tbgl_kns_lsbl, false);
	return inaclLastbl;
}
/**
 * 根据内部户和日期查询当日发生额
 */
public IoInacTranam selTranInfoSyglbl(String acctno, String trandt) {
	
	InacTranam cplTranInfo = InDayEndSqlsDao.selTranInfoSyglbl(acctno, trandt, false);
	
	IoInacTranam inacTranam = SysUtil.getInstance(IoInacTranam.class);
	
	 CommUtil.copyProperties(inacTranam, cplTranInfo, false);
	 
	return inacTranam;
}

@Override
public Integer updInacEdbl(String acctno, BigDecimal dredbl, BigDecimal credbl) {
	
//	String mtdate =DateTools2.getDateInfo().getSystdt();
	String timetm =DateTools2.getCurrentTimestamp();
	
	  int rowNum = InDayEndSqlsDao.uptGlAcctEdbl(acctno, dredbl, credbl,timetm);
	  
	return rowNum;
}

@Override
public String selInacAcctno() {
	return  InDayEndSqlsDao.namedsql_selGlKnaAcctSyglbl;
}
/**
 * 分页查询业务代码编码和名称
 */
@Override
public Options<IoBusinoName> selBusinoAndBusina(String busino, Integer pgsize, Integer pageno) {
	
	
	//总记录数
	long count = InQuerySqlsDao.selBusinoNameCount(busino, false);
	
	CommTools.getBaseRunEnvs().setTotal_count(count);
	Options<IoBusinoName> businoName = new DefaultOptions<>();

	//业务编码信息
	List<IoBusinoName> businoNames = InQuerySqlsDao.selBusinoNameList(busino, (pageno-1L)*pgsize, pgsize, false);

	for(IoBusinoName info : businoNames){

		businoName.add(info);
	}	
	return businoName;
}
/**
 * 根据条件查询内部户信息
 *
 */
@Override
public Options<IoInacInfo> selInacInfoByConditions(String acctno, String acctna,
		String crcycd, String busino,String brchno,E_QURYTP qurytp,E_INACTP inactp,String subsac,E_YES___ isrept) {
	String tranbr =CommTools.getBaseRunEnvs().getTrxn_branch();
	long pageno = CommTools.getBaseRunEnvs().getPage_start();
	long pagesize =CommTools.getBaseRunEnvs().getPage_size();
        // if(inactp==E_INACTP.SPECIAL){
        // if(CommUtil.isNull(subsac)) {
        // throw DpModuleError.DpstComm.E9999("账户类型是专用账户时子户号不能为空");
        //// throw InError.comm.E0005("账户类型是专用账户时子户号");
        // }
        // }
	
	//查询上级机构
	//Options<IoBrchInfo> ioBrchInfos = SysUtil.getInstance(IoSrvPbBranch.class).getUpperLevelBranch(tranbr, BusiTools.getDefineCurrency());
	Options<IoBrchInfo> ioBrchInfos = SysUtil.getRemoteInstance(IoSrvPbBranch.class).getUpperLevelBranch(tranbr, BusiTools.getDefineCurrency());
	//下级机构查上级机构则报错	
	if(CommUtil.isNotNull(acctno)&&CommUtil.isNotNull(ioBrchInfos)){
        
		for(int i=0;i<ioBrchInfos.size();i++){
            	
            	if(CommUtil.equals(acctno.substring(0, 6).toString(), ioBrchInfos.get(i).getBrchno())){					
					
            		throw InModuleError.InAccount.IN030017(); 	
            	}						
            }			
	}
	Page<IoInacInfo> list = null;
	if(isrept==E_YES___.YES){
		
	    list = InQuerySqlsDao.selInacLastInfoByConditions( brchno,  tranbr,crcycd,  (pageno - 1)*pagesize, pagesize, 0, false);

	}else{
		
		list = InQuerySqlsDao.selInacInfoByConditions(acctno, acctna, crcycd, busino, tranbr, brchno, inactp, subsac, qurytp, (pageno - 1)*pagesize, pagesize, 0, false);
	}
	
	//查询内部户信息

	//查询记录数
	
	Options<IoInacInfo> ioInacInfo = new DefaultOptions<>();
	for(IoInacInfo info : list.getRecords()){
		
		IoInAcctTmp knaAcctTmp = InnerAcctQry.queryInnerAcctApi(info.getAcctno());
		info.setBlncdn(knaAcctTmp.getBlncdn());//余额方向
		info.setDrctbl(knaAcctTmp.getDrctbl());//联机借方余额
		info.setCrctbl(knaAcctTmp.getCrctbl());//联机贷方余额
		info.setOnlndn(info.getBlncdn());
		info.setOnlnbl(knaAcctTmp.getOnlnbl() );//联机余额
		info.setLastbl(knaAcctTmp.getLastbl());//上日余额
		info.setLastdn(knaAcctTmp.getLastdn());//上日余额方向
		//查询开户信息
		GlKnbOcac tblOcac = InQuerySqlsDao.selGlKnbOcacByAcctno(info.getAcctno(), true);
		info.setOpener(tblOcac.getOpener());//开户柜员
		info.setCloser(tblOcac.getCloser());//销户柜员
		info.setClosdt(tblOcac.getClosdt());
		info.setCltrsq(tblOcac.getClossq());
		
		GlKnaAmbl tblAmbl = InQuerySqlsDao.selGlKnaAmblByAcctno(info.getAcctno(), false);
		if(CommUtil.isNotNull(tblAmbl)){
			info.setNoacml(tblAmbl.getAcmlbl());//正常积数
			info.setOvacml(tblAmbl.getOvacml());//透支积数
		}
		if(info.getBeintp()==E_BEINTP._5){
			info.setIsbein(null);
		}
		ioInacInfo.add(info);
	}
	CommTools.getBaseRunEnvs().setTotal_count(list.getRecordCount());
	
	return ioInacInfo;
}
/**
 * 根据业务代码查询内部户
 */
@Override
public IoInacInfo selInacInfoByBusino(String busino, String brchno,
		String crcycd, String subsac) {
	
	if(CommUtil.isNull(busino)){
		throw InError.comm.E0005("业务代码"); 
	}
	
	if(CommUtil.isNull(brchno)){
		throw InError.comm.E0005("账户所属机构"); 
	}
	
	if(CommUtil.isNull(crcycd)){
		throw InError.comm.E0005("币种"); 
	}

	//如果子户号不为空则根据子户号查询内部户
	GlKnaAcct tblGKnaAcct = SysUtil.getInstance(GlKnaAcct.class);
	if(CommUtil.isNotNull(subsac)){
		
		tblGKnaAcct = InacSqlsDao.queryInAcctBySubsac(busino, crcycd, brchno, subsac, false);
	}else{
		//如果子户号为空，则默认查询基准账号
		tblGKnaAcct =InacSqlsDao.queryInBaseAcct(busino, crcycd, brchno, false);
	}
	IoInacInfo  ioInacInfo = SysUtil.getInstance(IoInacInfo.class); 
	
	CommUtil.copyProperties(ioInacInfo, tblGKnaAcct);
	
	return ioInacInfo;
	
	
	
}

/**
 * 业务代码复核
 */
@Override
public void checkBusiInfo(
		cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery.IoCheckBusiInfo.Input input) {
	if(CommUtil.isNull(input.getBusino())){
		
		throw InError.comm.E0013(input.getBusino());
	}	
	
	DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
	// 查询产品新增柜员
	String tranus = InacSqlsDao.selUserByBusino(E_BUSIBI.INNE,input.getBusino(),E_PRTRTP.ADD, true);
	if (CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_teller(), tranus)) {
		throw DpModuleError.DpstComm.E9999("录入和复核不得为同一人");
	}
	
	GlKnpBusi tblGlKnpBusi = GlKnpBusiDao.selectOne_odb1(input.getBusino(), true);
	
	if(tblGlKnpBusi.getBusist()!=E_PRODST.ASSE){
		throw InError.comm.E0014(tblGlKnpBusi.getBusist().getLongName());
	}
	
    if(CommUtil.isNull(input.getBusitp())||input.getBusitp()!=tblGlKnpBusi.getBusitp()){
    	
		throw InError.comm.E0013("核算性质不匹配！");
	}	

	
   if(CommUtil.isNull(input.getBlncdn())||input.getBlncdn()!=tblGlKnpBusi.getBlncdn()){
		
 		throw InError.comm.E0013("余额方向不匹配！");
 	}	
   
   if(CommUtil.isNull(input.getPmodtg())||input.getPmodtg()!=tblGlKnpBusi.getPmodtg()){
	   
	   throw InError.comm.E0013("余额红字标志不匹配！");
   }	
   
   if(CommUtil.isNull(input.getIspaya())||input.getIspaya()!=tblGlKnpBusi.getIspaya()){
		
		throw InError.comm.E0013("是否采用销账管理标志不匹配！");
	}
   
   if(CommUtil.isNull(input.getIsbein())||input.getIsbein()!=tblGlKnpBusi.getIsbein()){
		
		throw InError.comm.E0013("计息标志不匹配！");
	}
   
   if(CommUtil.isNull(input.getBeintp())||input.getBeintp()!=tblGlKnpBusi.getBeintp()){
		
		throw InError.comm.E0013("结清标志不匹配！");
	}
   
   if(!CommUtil.equals(input.getNobusi(), tblGlKnpBusi.getNobusi())){
	   
	   throw InError.comm.E0013("正常利息归属业务代码不匹配！");
   }
   
   if(!CommUtil.equals(input.getOvbusi(), tblGlKnpBusi.getOvbusi())){
	   
	   throw InError.comm.E0013("透支利息归属业务代码不匹配！");
   }
   
   if(CommUtil.isNull(input.getIsopen())||input.getIsopen()!=tblGlKnpBusi.getIsopen()){
		
		throw InError.comm.E0013("是否允许开内部帐标志不匹配！");
	}
   
   if(CommUtil.isNull(input.getKpacfg())||input.getKpacfg()!=tblGlKnpBusi.getKpacfg()){
		
		throw InError.comm.E0013("入账方式不匹配！");
	}
   
   if(CommUtil.isNull(input.getRlbltg())||input.getRlbltg()!=tblGlKnpBusi.getRlbltg()){
		
		throw InError.comm.E0013("入分户标志不匹配！");
	}
   
   if(CommUtil.isNull(input.getEtactp())||input.getEtactp()!=tblGlKnpBusi.getEtactp()){
		
		throw InError.comm.E0013("入账明细方式不匹配！");
	}
   
   if(CommUtil.isNull(input.getCractp())||input.getCractp()!=tblGlKnpBusi.getCractp()){
		
		throw InError.comm.E0013("冲销标志不匹配！");
	}
   
   if(CommUtil.isNull(input.getBgdate())||!CommUtil.equals(input.getBgdate(), tblGlKnpBusi.getBgdate())){
		
		throw InError.comm.E0013("生效日期不匹配！");
	}
   
   if(CommUtil.isNull(input.getEddate())||!CommUtil.equals(input.getEddate(), tblGlKnpBusi.getEddate())){
		
		throw InError.comm.E0013("生效日期不匹配！");
	}
   tblGlKnpBusi.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());//交易柜员
   tblGlKnpBusi.setBusist(E_PRODST.EFFE);//更新为装配生效
   
   GlKnpBusiDao.updateOne_odb1(tblGlKnpBusi);
   copyBusinoToSecondCorpno(tblGlKnpBusi.getBusino());
   // 产品操作柜员登记
	SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.INNE, input.getBusino(), E_PRTRTP.CHCK);
	
}

	/**
	 * 拷贝内部户产品码到二级法人
	 * @param prodcd
	 */
	private void copyBusinoToSecondCorpno(String busino) {
		String cecorp = BusiTools.getCenterCorpno();
		List<AppCorp> appCorplList = DpCopyProdDao.selSecondCorpno(false);
		for (AppCorp appCorp : appCorplList) {
			InDayEndSqlsDao.insBusinoToSecondCorpno(appCorp.getCorpno(), cecorp, busino, false);
		}
	}

	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-18 15：36</li>
	 *         <li>内部户产品删除</li>
	 *         </p>
	 * @param busino
	 *           业务编号
	 * */
	@Override
	public void ioDelBusinoInfo(String busino) {

		//业务编号为空判断
		if(CommUtil.isNull(busino)){
			throw InError.comm.E0005("业务编号"); 
		}
		
		GlKnpBusi glKnpBusi = GlKnpBusiDao.selectOne_odb1(busino, true);
		
		//产品状态判断
		if (glKnpBusi.getBusist() != E_PRODST.ASSE && glKnpBusi.getBusist() != E_PRODST.INPUT) {
			
			throw InError.comm.E0005("拒绝！产品已生成，无法删除！"); 
		}
		
		//内部户产品删除完成
		GlKnpBusiDao.deleteOne_odb1(busino);
		
		// 产品操作柜员删除
		InacSqlsDao.delKupDppbUserByProdcd(busino, E_BUSIBI.INNE);
		
	}

	@Override
	public Integer knbLttlBatchInsert(IoknblttlbatchInfo batchinfo) {
		int count;
		KnbCbtlBatch tblknb_lttl = SysUtil.getInstance(KnbCbtlBatch.class);
		CommUtil.copyProperties(tblknb_lttl, batchinfo, false);
		count = KnbCbtlBatchDao.insert(tblknb_lttl);
		return count;
	}
/**
 * 查询机构、币种下有效状态账户数量
 */

	@Override
	public Long selEffectAcctnoByCrcycd(String crcycd, String brchno) {
		
		Long count = InQuerySqlsDao.selEffectAcctnoByCrcycd(brchno,crcycd,false);

		return count;
	}
/**
 * 
 * @Title: selClrbalInfo 
 * @Description: (查询对账账户余额) 
 * @param brchno
 * @param chckdt
 * @return
 * @author chaiwenchang
 * @date 2016年8月4日 下午2:44:29 
 * @version V2.3.0
 */
@Override
	public Options<IoClrInfo> selClrbalInfo(String brchno, String chckdt, E_TRANWY tranwy) {

		long pageno = CommTools.getBaseRunEnvs().getPage_start();//页数
		long pagesize =CommTools.getBaseRunEnvs().getPage_size();//页容量
		
		String busino = null;
		String subsac = null;
		//检查输入参数是否为空
		if(CommUtil.isNull(brchno)){
			throw InError.comm.E0005("机构号");
		}
		
		if(CommUtil.isNull(chckdt)){
			throw InError.comm.E0005("核对日期");
		}
		//检查核对日期
		if(!CommUtil.equals(chckdt, CommTools.getBaseRunEnvs().getLast_date())){
			throw CaError.Eacct.E0001("核对日期与上日交易日期必须相等");
		}
		
		//页数
		if(CommUtil.isNull(pageno)){
			throw InError.comm.E0005("页数不能为空");
		}
		
		if(pageno <= 0){
			throw InError.comm.E0005("页数必须大于0");
		}
		
		//页容量
		if(CommUtil.isNull(pagesize)){
			throw CaError.Eacct.E0001("页容量不能为空");
		}
		
		if(pagesize <= 0){
			throw CaError.Eacct.E0001("页容量必须大于0");
		}
		
		Page<IoClrInfo> cplIoClrInfo = null;
		if(CommUtil.isNull(tranwy)){
			//查询内部户信息
			cplIoClrInfo = InQuerySqlsDao.selClrInfoByBrchno(brchno, 
							(pageno - 1)*pagesize, pagesize, 0, false);

		}else{
			//判断对账模块类型
			if(tranwy==E_TRANWY.TE){
				busino="9930410402";
				subsac="0000001";
			}else if(tranwy==E_TRANWY.CT){
				busino="9930410403";
				subsac="0000001";
			}else if(tranwy==E_TRANWY.IB){
				busino="9930410402";
				subsac="0000002";
			}else if(tranwy==E_TRANWY.UP){
				busino="9930410402";
				subsac="0000009";
			}
			cplIoClrInfo = InQuerySqlsDao.selClrInfoByTranwy(brchno, busino, subsac, 
						    	(pageno - 1)*pagesize, pagesize, 0, false);
		}

		if(cplIoClrInfo.getRecords().isEmpty()){
			throw InError.comm.E0005("该产品不存在！"); 
		}
		
		Options<IoClrInfo> clrLst = new DefaultOptions<>();
		
		for(IoClrInfo info : cplIoClrInfo.getRecords()){
			IoClrInfo clrInfo = SysUtil.getInstance(IoClrInfo.class);
			CommUtil.copyProperties(clrInfo, info);
			String acctno = info.getAcctno();		
			
			
			IoInAcctTmp knaAcctTmp = InnerAcctQry.queryInnerAcctApi(info.getAcctno());
			// 可用余额处理
			clrInfo.setAccbal(knaAcctTmp.getOnlnbl());
			
			
			Map<String, Object> cplKnsGlvc = 
					InQuerySqlsDao.selGlKnsGlvcInfoByAcctno(acctno, false);
			
			if(CommUtil.isNotNull(cplKnsGlvc)){
			
					clrInfo.setDebbal((BigDecimal) cplKnsGlvc.get("drctbl"));//借方发生额
					clrInfo.setCrdbal((BigDecimal) cplKnsGlvc.get("crctbl"));//贷方发生额
					
			}

			clrInfo.setAcctno(acctno);//内部户账号
			clrInfo.setAcctna(info.getAcctna());//内部户名称
			
			//若根据对账模块查询，则set对应的对账模块值
			if(CommUtil.isNotNull(tranwy)){
				clrInfo.setTranwy(tranwy);
			}
			clrLst.add(clrInfo);
		}
		// 设置总记录数
		CommTools.getBaseRunEnvs().setTotal_count(cplIoClrInfo.getRecordCount());
		
		return clrLst;		

	}

	/**
	 * 
	 * @Title: insKnsPaya 
	 * @Description: (新增挂账信息) 
	 * @param knsPaya
	 * @return
	 * @author huangzhikai
	 * @date 2016年8月15日 上午10:44:29 
	 * @version V2.3.0
	 */
	@Override
	public void insKnsPaya(IoInKnsPaya knsPaya) {
		KnsPaya tbl_knspaya = SysUtil.getInstance(KnsPaya.class);
		CommUtil.copyProperties(tbl_knspaya, knsPaya);
		KnsPayaDao.insert(tbl_knspaya);
	}

	/**
	 * 
	 * @Title: selPaydStatus 
	 * @Description: (查询挂账状态) 
	 * @param knsPaya
	 * @return
	 * @author huangzhikai
	 * @date 2016年8月15日 上午10:44:29 
	 * @version V2.3.0
	 */
	@Override
	public Options<IoPayaSttInfo> selPaydStatus(String payasq, E_PAYAST payast) {
		List<KnsPaya> info = KnsPayaDao.selectAll_kns_paya_odx7(payasq, E_PAYAST.JQ, false);
		Options<IoPayaSttInfo> list = new DefaultOptions<>();
		for(KnsPaya paya : info){
			IoPayaSttInfo payaInfo = SysUtil.getInstance(IoPayaSttInfo.class);
			payaInfo.setPayast(paya.getPayast());
			list.add(payaInfo);
		}
		return list;
		
	}

	/**
	 * 
	 * @Title: insKnsPayd 
	 * @Description: (新增销账信息) 
	 * @param knsPayd
	 * @return 
	 * @author huangzhikai
	 * @date 2016年8月15日 下午17:44:29 
	 * @version V2.3.0
	 */
	@Override
	public void insKnsPayd(IoInKnsPayd knsPayd) {
		KnsPayd tbl_knsPayd = SysUtil.getInstance(KnsPayd.class);
		CommUtil.copyProperties(tbl_knsPayd, knsPayd);
		KnsPaydDao.insert(tbl_knsPayd);
	}
	
	/**
	 * 
	 * @Title: getProdTyinno 
	 * @Description: (生成15位（机构号3位+年份日期8位+序号4位）) 
	 * 每日自动初始化序号
	 * @return tyinno 录入编号
	 * @author liaojincai
	 * @date 2016年11月29日 下午3:44:27 
	 * @version V2.3.0
	 */
	public static String getBusiTyinno(){
		String tyinno = "";// 15位录入编号
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();// 机构号
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期
		
		// 获取录入编号
		StringBuilder bf = new StringBuilder();
//		bf.append(trandt).append(corpno).append(trandt);//rambo delete
		bf.append(corpno).append(trandt);
		tyinno = MsSeqUtil.genSeq("PRODTY", bf.toString());
		
		return tyinno;
	}
	
	/**
	 * 
	 * @Title: selInacInfos 
	 * @Description: 查询内部户正常状态信息
	 * @author chaiwenchang
	 * @date 2017年01月11日 上午09:45:24 
	 * @version V2.3.0
	 */

	public Options<IoInacInfo> selInacInfos( 
			String acctno,  String acctna,  String crcycd,  String busino,  String brchno,  cn.sunline.ltts.busi.sys.type.BaseEnumType.E_QURYTP qurytp,  cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INACTP inactp,  String subsac){

		String tranbr =CommTools.getBaseRunEnvs().getTrxn_branch();
		long pageno = CommTools.getBaseRunEnvs().getPage_start();
		long pagesize =CommTools.getBaseRunEnvs().getPage_size();
		
		if(!CommUtil.equals(crcycd, BusiTools.getDefineCurrency())){
			throw InError.comm.E0003("不支持的币种【"+crcycd+"】暂只支持币种【"+BusiTools.getDefineCurrency()+"】"); 	
		}
		
		
		//查询上级机构
		Options<IoBrchInfo> ioBrchInfos = SysUtil.getInstance(IoSrvPbBranch.class).getUpperLevelBranch(tranbr, BusiTools.getDefineCurrency());
		//下级机构查上级机构则报错	
		if(CommUtil.isNotNull(acctno)&&CommUtil.isNotNull(ioBrchInfos)){
	        
			for(int i=0;i<ioBrchInfos.size();i++){
	            	
	            	if(CommUtil.equals(acctno.substring(0, 6).toString(), ioBrchInfos.get(i).getBrchno())){					
						
	            		throw InError.comm.E0003("当前柜员无权限查询该机构！"); 	
	            	}						
	            }			
		}
		
		Page<IoInacInfo> list = null;
	    list = InQuerySqlsDao.selInacLastInfoByConditions( brchno,  tranbr,crcycd,  (pageno - 1)*pagesize, pagesize, 0, false);

		//查询内部户信息

		//查询记录数
		
		Options<IoInacInfo> ioInacInfo = new DefaultOptions<>();
		for(IoInacInfo info : list.getRecords()){
			
			IoInAcctTmp knaAcctTmp = InnerAcctQry.queryInnerAcctApi(info.getAcctno());
			info.setBlncdn(knaAcctTmp.getBlncdn());//余额方向
			info.setDrctbl(knaAcctTmp.getDrctbl());//联机借方余额
			info.setCrctbl(knaAcctTmp.getCrctbl());//联机贷方余额
			info.setOnlndn(info.getBlncdn());
			info.setOnlnbl(knaAcctTmp.getOnlnbl() );//联机余额
			info.setLastbl(knaAcctTmp.getLastbl());//上日余额
			info.setLastdn(knaAcctTmp.getLastdn());//上日余额方向
			//查询开户信息
			GlKnbOcac tblOcac = InQuerySqlsDao.selGlKnbOcacByAcctno(info.getAcctno(), true);
			info.setOpener(tblOcac.getOpener());//开户柜员
			info.setCloser(tblOcac.getCloser());//销户柜员
			info.setClosdt(tblOcac.getClosdt());
			info.setCltrsq(tblOcac.getClossq());
			
			GlKnaAmbl tblAmbl = InQuerySqlsDao.selGlKnaAmblByAcctno(info.getAcctno(), false);
			if(CommUtil.isNotNull(tblAmbl)){
				info.setNoacml(tblAmbl.getAcmlbl());//正常积数
				info.setOvacml(tblAmbl.getOvacml());//透支积数
			}
			if(info.getBeintp()==E_BEINTP._5){
				info.setIsbein(null);
			}
			ioInacInfo.add(info);
		}
		CommTools.getBaseRunEnvs().setTotal_count(list.getRecordCount());
		
		return ioInacInfo;

	}


}

