package cn.sunline.ltts.busi.dp.serviceimpl;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.acct.DpCloseAcctno;
import cn.sunline.ltts.busi.dp.acct.DpProductProc;
import cn.sunline.ltts.busi.dp.acct.DpSaveProc;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType.CmpDpProd.Input;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType.QryDpBreifInfo.Output;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcctTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActpTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAddtTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCustTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfirTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrplTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntrTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbMatuTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPoplTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTerm;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTermDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTermTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbUser;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbUserDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDprebypd;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDprebypdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.dp.type.DpProdType.KupDppbInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkEacctStatusOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ChkSaveDepositIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DppbDetailInfo;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DppbTermInfo;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.QryDpAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.ReserveProductOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCheckPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseOT;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCustPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDetailInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDfirPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDppbBrchPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDppbPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDrawPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDrawplPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpIntrPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpMatuPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpPostPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpPostplPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpTermInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpBrDtChgeQry;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpBrhcno;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpSalInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.KupDppbBriefInfo;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSATP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CYCLES;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_OPERAT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_REBYPE;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_REBYST;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

/**
 * 负债产品服务实现
 * 
 * @author Cuijia
 */
@cn.sunline.adp.core.annotation.Generated
public class DpProdSvcImpl implements
cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(DpProdSvcImpl.class);
	
	/**
	 * 带币种查询活期账户
	 * 
	 */
	@Override
	public DpProdSvc.QryDpAcctOut qryDpAcct(String custac, String crcycd) {
		
		if(CommUtil.isNull(crcycd)){
			throw DpModuleError.DpstProd.BNAS0662();
		};
		
		if(CommUtil.isNull(custac)){
			throw DpModuleError.DpstProd.BNAS0926();
		};
		
		DpProdSvc.QryDpAcctOut out=SysUtil.getInstance(DpProdSvc.QryDpAcctOut.class);
/*		
		//负债账号客户账号对照表获取活期账户信息
		//kna_accs tblKna_accs = CaTools.getAcctAccs(custac, crcycd);
		IoCaStaPublic casta = SysUtil.getInstance(IoCaStaPublic.class);
		IoCaKnaAccs tblKna_accs = casta.CaTools_getAcctAccs(custac, crcycd);
		
		//获取负债账户信息
		KnaAcct tblKnaAcct=KnaAcctDao.selectOne_odb5(tblKna_accs.getAcctno(), crcycd,false);
		
		if(CommUtil.isNull(tblKnaAcct)){
			throw DpModuleError.DpstProd.E0010("账号信息不存在");
		};
		out = initOut(tblKna_accs, tblKnaAcct);
		
		*/
		KnaAcct  tblKnaAcct = CapitalTransDeal.getSettKnaAcctAc(custac);
		CommUtil.copyProperties(out, tblKnaAcct);
		
		return out;
	}




	/**
	 * 电子账户检查
	 */
	@Override
	public ChkEacctStatusOut chkEacct(ChkSaveDepositIn saveDpIn) {
		// TODO Auto-generated method stub
		//电子账户状态检查
		ChkEacctStatusOut out = DpSaveProc.chkEacctSt(saveDpIn);
		
		//卡状态检查----现开户不登记此表，暂不检查   by  zhx  20180329
		//DpSaveProc.chkCardStatus(out.getCardno(),out.getCustac(), saveDpIn.getTocdno());
		
		return out;
	}



	/**
	 * 根据电子帐号，子户号查询负债帐号
	 */
	@Override
	public QryDpAcctOut QryDpByAcctSub(String custac, String subsac) {
		if(CommUtil.isNull(subsac)){
			throw CaError.Eacct.BNAS1694();
		};
		
		if(CommUtil.isNull(custac)){
			throw DpModuleError.DpstProd.BNAS0926();
		};
		
		DpProdSvc.QryDpAcctOut out=SysUtil.getInstance(DpProdSvc.QryDpAcctOut.class);
		
		//获取电子账户负债账户对照表
		//kna_accs tblKna_accs = Kna_accsDao.selectOne_odb1(custac, subsac, false);
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaAccs tblKna_accs = caqry.getKnaAccsOdb1(custac, subsac, false);
		
		//获取对应的负债账户
		if(DpEnumType.E_FCFLAG.CURRENT==tblKna_accs.getFcflag()){
			//获取活期负债账户信息
			KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb5(tblKna_accs.getAcctno(), tblKna_accs.getCrcycd(),false);
			out = initOut(tblKna_accs, tblKnaAcct);
			if(CommUtil.isNull(tblKnaAcct)){
				throw DpModuleError.DpstProd.BNAS1757();
			};
		}else if(DpEnumType.E_FCFLAG.FIX==tblKna_accs.getFcflag()){
			KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(tblKna_accs.getAcctno(), false);
			out = initOut(tblKna_accs, tblKnaFxac);
			if(CommUtil.isNull(tblKnaFxac)){
				throw DpModuleError.DpstProd.BNAS1757();
			};
		}else{
			throw DpModuleError.DpstProd.BNAS1758();
		}
		
		return out;
	}

	private DpProdSvc.QryDpAcctOut initOut(IoCaKnaAccs tblKna_accs,KnaFxac KnaFxac) {
		DpProdSvc.QryDpAcctOut out = SysUtil.getInstance(DpProdSvc.QryDpAcctOut.class);
		out.setAcctcd(KnaFxac.getAcctcd());		//核算代码
		out.setAcctna(KnaFxac.getAcctna());		//账户名称
		out.setAcctno(KnaFxac.getAcctno());		//负债账号
		out.setAcctst(KnaFxac.getAcctst());		//账号状态
		out.setAccttp(KnaFxac.getAccttp());		//结束账户标志
		out.setBgindt(KnaFxac.getBgindt());		//起息日期
		out.setBkmony(KnaFxac.getBkmony());		//备用金额
		out.setBrchno(KnaFxac.getBrchno());		//账户所属机构
		out.setClosdt(KnaFxac.getClosdt());		//销户日期
		out.setClossq(KnaFxac.getClossq());		//销户流水
		out.setCrcycd(KnaFxac.getCrcycd());		//货币代号
		out.setCsextg(KnaFxac.getCsextg());		//账户钞汇标志
		out.setCustac(KnaFxac.getCustac());		//客户账号
		out.setCustno(KnaFxac.getCustno());		//客户号
		out.setDebttp(KnaFxac.getDebttp());		//存款种类
		out.setDepttm(KnaFxac.getDepttm());		//存期
		out.setHdmimy(KnaFxac.getHdmimy());		//最小留存金额
		out.setHdmxmy(KnaFxac.getHdmxmy());		//最大留存金额
		out.setLastbl(KnaFxac.getLastbl());		//上日账户余额
		out.setLstrdt(KnaFxac.getLstrdt());		//上次交易日期
		out.setLstrsq(KnaFxac.getLstrsq());		//上次交易流水
		out.setMatudt(KnaFxac.getMatudt());		//到期日期
		out.setOnlnbl(KnaFxac.getOnlnbl());		//当前账户余额
		out.setOpendt(KnaFxac.getOpendt());		//开户日期
		out.setOpensq(KnaFxac.getOpensq());		//开户流水
		out.setOpmony(KnaFxac.getOpmony());		//开户金额
		out.setPddpfg(KnaFxac.getPddpfg());		//产品定活标志
		out.setProdcd(KnaFxac.getProdcd());		//产品编号
		out.setSleptg(KnaFxac.getSleptg());		//形态转移标志
		out.setSpectp(KnaFxac.getSpectp());		//负债账户性质
		out.setTrsvtp(KnaFxac.getTrsvtp());		//转存方式
		
		out.setSubsac(tblKna_accs.getSubsac());		//子户号
		return out;
	}

	
	private DpProdSvc.QryDpAcctOut initOut(IoCaKnaAccs tblKna_accs,
			KnaAcct tblKnaAcct) {
		DpProdSvc.QryDpAcctOut out = SysUtil.getInstance(DpProdSvc.QryDpAcctOut.class);
		out.setAcctcd(tblKnaAcct.getAcctcd());		//核算代码
		out.setAcctna(tblKnaAcct.getAcctna());		//账户名称
		out.setAcctno(tblKnaAcct.getAcctno());		//负债账号
		out.setAcctst(tblKnaAcct.getAcctst());		//账号状态
		out.setAccttp(tblKnaAcct.getAccttp());		//结束账户标志
		out.setBgindt(tblKnaAcct.getBgindt());		//起息日期
		out.setBkmony(tblKnaAcct.getBkmony());		//备用金额
		out.setBrchno(tblKnaAcct.getBrchno());		//账户所属机构
		out.setClosdt(tblKnaAcct.getClosdt());		//销户日期
		out.setClossq(tblKnaAcct.getClossq());		//销户流水
		out.setCrcycd(tblKnaAcct.getCrcycd());		//货币代号
		out.setCsextg(tblKnaAcct.getCsextg());		//账户钞汇标志
		out.setCustac(tblKnaAcct.getCustac());		//客户账号
		out.setCustno(tblKnaAcct.getCustno());		//客户号
		out.setDebttp(tblKnaAcct.getDebttp());		//存款种类
		out.setDepttm(tblKnaAcct.getDepttm());		//存期
		out.setHdmimy(tblKnaAcct.getHdmimy());		//最小留存金额
		out.setHdmxmy(tblKnaAcct.getHdmxmy());		//最大留存金额
		out.setLastbl(tblKnaAcct.getLastbl());		//上日账户余额
		out.setLstrdt(tblKnaAcct.getLstrdt());		//上次交易日期
		out.setLstrsq(tblKnaAcct.getLstrsq());		//上次交易流水
		out.setMatudt(tblKnaAcct.getMatudt());		//到期日期
		out.setOnlnbl(DpAcctProc.getAcctBalance(tblKnaAcct));		//当前账户余额
		out.setOpendt(tblKnaAcct.getOpendt());		//开户日期
		out.setOpensq(tblKnaAcct.getOpensq());		//开户流水
		out.setOpmony(tblKnaAcct.getOpmony());		//开户金额
		out.setPddpfg(tblKnaAcct.getPddpfg());		//产品定活标志
		out.setProdcd(tblKnaAcct.getProdcd());		//产品编号
		out.setSleptg(tblKnaAcct.getSleptg());		//形态转移标志
		out.setSpectp(tblKnaAcct.getSpectp());		//负债账户性质
		out.setTrsvtp(tblKnaAcct.getTrsvtp());		//转存方式
		
		out.setSubsac(tblKna_accs.getSubsac());		//子户号
		return out;
	}



	/**
	 * 
	 */
	@Override
	public QryDpAcctOut QryDpByAcctno(String acctno) {
		if(CommUtil.isNull(acctno)){
			throw DpModuleError.DpstProd.BNAS1759();
		};
		
		
		DpProdSvc.QryDpAcctOut out=SysUtil.getInstance(DpProdSvc.QryDpAcctOut.class);
		
		//获取电子账户负债账户对照表
		//kna_accs tblKna_accs = Kna_accsDao.selectOne_odb2(acctno, true);
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaAccs tblKna_accs = caqry.getKnaAccsOdb3(acctno, true);
		
		//获取对应的负债账户
		if(DpEnumType.E_FCFLAG.CURRENT==tblKna_accs.getFcflag()){
			//获取活期负债账户信息
			KnaAcct tblKnaAcct = ActoacDao.selKnaAcct(acctno, false);
//			KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb5(tblKna_accs.getAcctno(), tblKna_accs.getCrcycd(),false);
			if(CommUtil.isNull(tblKnaAcct)){
				throw DpModuleError.DpstProd.BNAS1757();
			}
			out = initOut(tblKna_accs, tblKnaAcct);
		}else if(DpEnumType.E_FCFLAG.FIX==tblKna_accs.getFcflag()){
			KnaFxac tblKnaFxac = ActoacDao.selKnaFxac(acctno, false);
//			KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(tblKna_accs.getAcctno(), false);
			if(CommUtil.isNull(tblKnaFxac)){
				throw DpModuleError.DpstProd.BNAS1757();
			}
			out = initOut(tblKna_accs, tblKnaFxac);
		}else{
			throw DpModuleError.DpstProd.BNAS1758();
		}
		
		return out;
	}




	/**
	 * 根据产品编号查询产品详细信息
	 */
	@Override
	public DppbDetailInfo QryDpByProdcd(String prodcd) {
		if (CommUtil.isNull(prodcd)) {
			throw DpModuleError.DpstProd.BNAS1054();
		}
		
		DppbDetailInfo pdinfo = SysUtil.getInstance(DppbDetailInfo.class);

		//获取产品发行份数，限额等信息
		pdinfo = DpProductProc.getDppbInfo(prodcd);
		
		//获取产品存期及存期天数
		List<KupDppbTerm> lstterms = KupDppbTermDao.selectAll_odb2(prodcd, true);
		if (lstterms.size() <= 0) {
			throw DpModuleError.DpstProd.BNAS1053();
		}
		//获取存期的集合信息
		Options<DppbTermInfo> lstTerms = new DefaultOptions<DppbTermInfo>();
		for (int i = 0; i < lstterms.size(); i++) {
			DppbTermInfo dpTermInfo = SysUtil.getInstance(DppbTermInfo.class);
			dpTermInfo.setDeptdy(lstterms.get(i).getDeptdy());
			dpTermInfo.setDepttm(lstterms.get(i).getDepttm());
			lstTerms.add(dpTermInfo);
		}
		
		pdinfo.setDeptif(lstTerms);
		
		return pdinfo;
	}

	/**
	 * 
	 * @Title: qryProdcd 
	 * @Description: (根据账户分类查询开立的产品编号) 
	 * @param accatp
	 * @author xiongzhao
	 * @date 2016年7月11日 下午6:50:29 
	 * @version V2.3.0
	 * @return 
	 */
	public KnpParameter qryProdcd(E_ACCATP accatp) {
		
		// 检查传入字段是否为空
		if (CommUtil.isNull(accatp)) {
			throw DpModuleError.DpstProd.BNAS0853();
		}
		
		// 调用方法查询开户升级所需产品参数
		KnpParameter tblKnpParameter = DpPublic.getProdcdByAccatp(accatp);
		
		// 返回查询结果
		return tblKnpParameter;
	}

	/**
	 * 
	 * @Title: QryDpBreifInfo 
	 * @Description: (产品统计查询) 
	 * @param input
	 * @param output
	 * @author chenjk
	 * @date 2016年7月14日 下午3:58:29 
	 * @version V2.3.0
	 * @return 
	 */	
	@Override
	public void qryDpBreifInfo(KupDppbBriefInfo input, Output output) {
		
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());//操作网点权限验证
		
		bizlog.debug("===========产品统计查询垃圾数据清除===========");

		delProdRubbishRecords(); //调用垃圾数据清除处理

		bizlog.debug("===========产品统计查询服务处理正式开始===========");
		
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		
		if (CommUtil.isNull(input.getContfg())) {
			throw DpModuleError.DpstProd.BNAS1760();
		}
		
		//分页查询
		long pageno = CommTools.getBaseRunEnvs().getPage_start(); //页码
		long pagesize = CommTools.getBaseRunEnvs().getPage_size(); //页容量
		Page<KupDppbBriefInfo> kupDppbBriefInfoPage = DpProductDao.selPageBriefKupDppb(input.getProdcd(), 
				input.getProdtx(), input.getBusibi(), input.getProdtp(), input.getPddpfg(),
				input.getDebttp(), input.getPrentp(), input.getProdtg(), 
				input.getProdst(), corpno,input.getContfg().getValue(), (pageno - 1) * pagesize, pagesize, 0, false);
		
		
		//将查询结果导出
		if(kupDppbBriefInfoPage.getRecordCount()!=0){
			Options<KupDppbBriefInfo> kupDppbBriefInfoOptions = new DefaultOptions<KupDppbBriefInfo>();
			for(KupDppbBriefInfo kupDppbBriefInfo : kupDppbBriefInfoPage.getRecords()){
				kupDppbBriefInfoOptions.add(kupDppbBriefInfo);
			}
			
			output.setQryInfos(kupDppbBriefInfoOptions);
			bizlog.debug("===========产品统计查询服务处理成功===========");
		}
		
		// 设置记录总数
		CommTools.getBaseRunEnvs().setTotal_count(kupDppbBriefInfoPage.getRecordCount());// 记录总数
	}

	/**
	 * 
	 * @Title: QryDpProdDetailInfo 
	 * @Description: (存款产品详细查询) 
	 * @param prodcd
	 * @param output
	 * @author chenjk
	 * @date 2016年7月15日 上午10:44:29 
	 * @version V2.3.0
	 * @return 
	 */	
	@Override
	public void qryDpProdDetailInfo(String prodcd, IoDpDetailInfo output) {
		
		bizlog.debug("===========存款产品详细查询服务处理开始===========");
		
		prodcdCheck(prodcd); //产品编号为空判断
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());//操作网点权限验证
		
		//基础部件信息查询
		IoDpDppbPartDetail ioDpDppbPartDetail = DpProductDao.selBaseInfoByProdcdandCorpno(prodcd,CommTools.getBaseRunEnvs().getBusi_org_id(), false);
		if(CommUtil.isNull(ioDpDppbPartDetail)){
			
			ioDpDppbPartDetail = SysUtil.getInstance(IoDpDppbPartDetail.class);
		}
		output.setBaseInfo(ioDpDppbPartDetail);
		output.setProdcd(prodcd);
		
		//开户控制部件信息查询
		IoDpCustPartDetail ioDpCustPartDetail = DpProductDao.selCustInfoByProdcd(prodcd, false);
		if(CommUtil.isNull(ioDpCustPartDetail)){
			
			ioDpCustPartDetail = SysUtil.getInstance(IoDpCustPartDetail.class);
		}
		
		//开户存期信息获取
		List<IoDpTermInfo> termInfoList = DpProductDao.selTermInfoByProdcd(prodcd, false);
		Options<IoDpTermInfo> termInfoOptions = new DefaultOptions<IoDpTermInfo>();
		for(IoDpTermInfo ioDpTermInfo : termInfoList){
			
			termInfoOptions.add(ioDpTermInfo);
		}
		ioDpCustPartDetail.setTmInfo(termInfoOptions);
		output.setCustInfo(ioDpCustPartDetail);
		
		//存入控制部件信息查询
		IoDpPostPartDetail ioDpPostPartDetail = DpProductDao.selPostInfoByProdcd(prodcd, false);
		if(CommUtil.isNull(ioDpPostPartDetail)){
			
			ioDpPostPartDetail = SysUtil.getInstance(IoDpPostPartDetail.class);
		}
		output.setPostInfo(ioDpPostPartDetail);
		
		//存入计划控制部件信息查询
		IoDpPostplPartDetail ioDpPostplPartDetail = DpProductDao.selPoplInfoByProdcd(prodcd, false);
		if(CommUtil.isNull(ioDpPostplPartDetail)){
			
			ioDpPostplPartDetail = SysUtil.getInstance(IoDpPostplPartDetail.class);
		}
		output.setPoplInfo(ioDpPostplPartDetail);
		
		//支取控制部件信息查询
		IoDpDrawPartDetail ioDpDrawPartDetail = DpProductDao.selDrawInfoByProdcd(prodcd, false);
		if(CommUtil.isNull(ioDpDrawPartDetail)){
			
			ioDpDrawPartDetail = SysUtil.getInstance(IoDpDrawPartDetail.class);
		}
		output.setDrawInfo(ioDpDrawPartDetail);
		
		//支取计划控制部件信息查询
		IoDpDrawplPartDetail ioDpDrawplPartDetail = DpProductDao.selDrplInfoByProdcd(prodcd, false);
		if(CommUtil.isNull(ioDpDrawplPartDetail)){
			
			ioDpDrawplPartDetail = SysUtil.getInstance(IoDpDrawplPartDetail.class);
		}
		output.setDrplInfo(ioDpDrawplPartDetail);
		
		//到期控制部件信息查询
		IoDpMatuPartDetail ioDpMatuPartDetail = DpProductDao.selMatuInfoByProdcd(prodcd, false);
		if(CommUtil.isNull(ioDpMatuPartDetail)){
			
			ioDpMatuPartDetail = SysUtil.getInstance(IoDpMatuPartDetail.class);
		}
		output.setMatuInfo(ioDpMatuPartDetail);
		
		//利息利率部件信息查询
		IoDpIntrPartDetail ioDpIntrPartDetail = DpProductDao.selIntrInfoByProdcd(prodcd, false);
		if(CommUtil.isNull(ioDpIntrPartDetail)){
			
			ioDpIntrPartDetail = SysUtil.getInstance(IoDpIntrPartDetail.class);
		}
		output.setIntrInfo(ioDpIntrPartDetail);
		
		//违约支取利息利率部件信息查询
		Options<IoDpDfirPartDetail> ioDpDfirPartDetailOptions = new DefaultOptions<IoDpDfirPartDetail>();
		List<IoDpDfirPartDetail> ioDpDfirPartDetailList = DpProductDao.selDfirInfoByProdcd(prodcd, false);
		
		if(CommUtil.isNotNull(ioDpDfirPartDetailList)){
			
			for(IoDpDfirPartDetail IoDpDfirPartDetail : ioDpDfirPartDetailList){
				ioDpDfirPartDetailOptions.add(IoDpDfirPartDetail);
			}
		}
		
		output.setDfirInfo(ioDpDfirPartDetailOptions);
		
		//核算部件信息查询
		IoDpCheckPartDetail ioDpCheckPartDetail = DpProductDao.selAcctInfoByProdcd(prodcd, false);
		if(CommUtil.isNull(ioDpCheckPartDetail)){
			
			ioDpCheckPartDetail = SysUtil.getInstance(IoDpCheckPartDetail.class);
		}
		output.setAcctInfo(ioDpCheckPartDetail);
		
		//产品机构控制部件信息查询
		IoDpDppbBrchPartDetail brchPartDetail =  DpProductDao.selBrchByProdcdandCorpno(prodcd,CommTools.getBaseRunEnvs().getBusi_org_id(), false);
		if(CommUtil.isNull(brchPartDetail)){
			
			brchPartDetail = SysUtil.getInstance(IoDpDppbBrchPartDetail.class);
		}
		output.setBrchInfo(brchPartDetail);
		
		bizlog.debug("===========存款产品详细查询服务处理成功===========");
	}
	
	/**
	 * 
	 * @Title: DelProdByProdcd 
	 * @Description: (产品删除) 
	 * @param prodcd
	 * @param busibi
	 * @author chenjk
	 * @date 2016年7月19日 上午17:07:29 
	 * @version V2.3.0
	 * @return 
	 */	
	@Override
	public void DelProdByProdcd(String prodcd, E_BUSIBI busibi) {
		
		bizlog.debug("===========产品删除服务处理开始===========");
		
		prodcdCheck(prodcd); //产品编号为空判断
		//busibiCheck(busibi); //业务大类为空判断   modify by chenlk 去除这个检查 20160928
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());//操作网点权限验证
		
		if(busibi == E_BUSIBI.INNE){
			
			bizlog.debug("内部户产品" + prodcd + "信息删除开始===========");
			
			//内部户产品删除
			SysUtil.getInstance(IoInQuery.class).ioDelBusinoInfo(prodcd);
			
			// 产品操作柜员登记
			SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.INNE, prodcd, E_PRTRTP.DELE);
			
		} else if(busibi == E_BUSIBI.DEPO){
		
			bizlog.debug("存款产品" + prodcd + "在临时表中信息删除开始===========");
			
			//存款产品删除，若数据位于正式表中，报错；若位于临时表中，则状态一定是未复核（产品装配和装配录入）状态，可以直接进行删除操作。
			//查询是否在正式表里
			KupDppb KupDppb = KupDppbDao.selectOne_odb1(prodcd, false);
			
			if(CommUtil.isNotNull(KupDppb)){
				
				throw DpModuleError.DpstProd.BNAS1761(); //在正式表里，报错
			}
			
			bizlog.debug("===========临时产品" + prodcd + "处理开始===========");
			
			//查询临时属性表
			KupDppbTemp KupDppbTemp = KupDppbTempDao.selectOne_odb1(prodcd, false);
			if(CommUtil.isNull(KupDppbTemp)){
				
				throw DpModuleError.DpstProd.BNAS1762(); //临时表未找到记录，报错
			}
			
			String crcycd = KupDppbTemp.getPdcrcy(); //搜索条件中币种处理
			
			KupDppbCustTempDao.deleteOne_odb1(prodcd, crcycd);//产品开户信息删除
//			KupDppbBrchTempDao.delete_odb1(prodcd, crcycd);//产品机构信息删除
			DpProductDao.delKupBrchTempByProdcd(prodcd, crcycd);//产品机构信息删除
			KupDppbTermTempDao.delete_odb2(prodcd);//产品存期信息删除
			KupDppbPostTempDao.deleteOne_odb1(prodcd, crcycd);//产品存入信息删除
			KupDppbDrawTempDao.deleteOne_odb1(prodcd, crcycd);//产品支取信息删除
			KupDppbMatuTempDao.deleteOne_odb1(prodcd, crcycd);//产品到期信息删除
			KupDppbDfirTempDao.delete_odb2(prodcd, crcycd);//产品违约支取利息信息删除
			KupDppbPoplTempDao.deleteOne_odb1(prodcd, crcycd);//产品存入计划删除
			KupDppbDrplTempDao.deleteOne_odb1(prodcd, crcycd);//产品支取计划删除
			KupDppbIntrTempDao.deleteOne_odb1(prodcd, crcycd);//产品利息利率定义信息删除
			KupDppbAcctTempDao.delete_odb2(prodcd);//产品核算信息删除
			KupDppbActpTempDao.delete_odb2(prodcd);//产品账户类型信息删除
			KupDppbAddtTempDao.deleteOne_odb1(prodcd);//产品附加属性信息删除
			KupDppbPartTempDao.delete_odb2(busibi, prodcd);//产品部件表删除
			KupDppbTempDao.deleteOne_odb1(prodcd);//产品基础属性部件表删除
			
			// 产品操作柜员登记
			SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.DEPO, prodcd, E_PRTRTP.DELE);
			
		} else {
			throw DpModuleError.DpstProd.BNAS1763(); //在正式表里，报错
		}
		
		bizlog.debug("===========临时产品" + prodcd + "删除成功===========");
		
	}


	/**
	 * 
	 * @Title: CmpDpProd 
	 * @Description: (存款产品比对) 
	 * @param input
	 * @param output
	 * @author chenjk
	 * @date 2016年7月19日 上午17:07:29 
	 * @version V2.3.0
	 * @return 
	 */	
	@Override
	public void cmpDpProd(
			Input input,
			cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType.CmpDpProd.Output output) {
		bizlog.debug("=========存款产品比对服务处理开始========");
		
		E_BUSIBI busibi = input.getBusibi(); //业务大类
		String pdcd01 = input.getPdcd01(); //产品编号1
		String pdcd02 = input.getPdcd02(); //产品编号2
		Options<IoDpDetailInfo> infoList = new DefaultOptions<IoDpDetailInfo>(); //两产品明细list
		
		prodcdCheck(pdcd01); //产品编号1为空判断
		prodcdCheck(pdcd02); //产品编号2为空判断
		busibiCheck(busibi); //业务大类为空判断
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());//操作网点权限验证
		
		//开始处理数据
		//两个账户，处理两次
		for(int i=0; i<2; i++){
			
			//两次分别取两个编号
			String prodcd = "";
			if(i == 0){
				prodcd = pdcd01;
			}else{
				prodcd = pdcd02;
			}
			
			bizlog.debug("产品" + prodcd + "信息查询处理开始========");
			IoDpDetailInfo ioDpDetailInfo = SysUtil.getInstance(IoDpDetailInfo.class);
			
			//基础部件信息查询
			IoDpDppbPartDetail ioDpDppbPartDetail = DpProductDao.selBaseInfoByProdcd(prodcd, false);
			if(CommUtil.isNull(ioDpDppbPartDetail)){
				//未找到对应记录，设为空值
				ioDpDppbPartDetail =  SysUtil.getInstance(IoDpDppbPartDetail.class); 
			}
			ioDpDetailInfo.setBaseInfo(ioDpDppbPartDetail);
			
			//开户控制部件信息查询
			IoDpCustPartDetail ioDpCustPartDetail = DpProductDao.selCustInfoByProdcd(prodcd, false);
			if(CommUtil.isNull(ioDpCustPartDetail)){
				//未找到对应记录，设为空值
				ioDpCustPartDetail = SysUtil.getInstance(IoDpCustPartDetail.class);
			}
			
			//开户存期信息获取
			List<IoDpTermInfo> termInfoList = DpProductDao.selTermInfoByProdcd(prodcd, false);
			Options<IoDpTermInfo> termInfoOptions = new DefaultOptions<IoDpTermInfo>();
			for(IoDpTermInfo ioDpTermInfo : termInfoList){
				
				termInfoOptions.add(ioDpTermInfo);
			}
			ioDpCustPartDetail.setTmInfo(termInfoOptions);
			ioDpDetailInfo.setCustInfo(ioDpCustPartDetail);
			
			//存入控制部件信息查询
			IoDpPostPartDetail ioDpPostPartDetail = DpProductDao.selPostInfoByProdcd(prodcd, false);
			if(CommUtil.isNull(ioDpPostPartDetail)){
				
				ioDpPostPartDetail = SysUtil.getInstance(IoDpPostPartDetail.class);
			}
			ioDpDetailInfo.setPostInfo(ioDpPostPartDetail);
			
			//存入计划控制部件信息查询
			IoDpPostplPartDetail ioDpPostplPartDetail = DpProductDao.selPoplInfoByProdcd(prodcd, false);
			if(CommUtil.isNull(ioDpPostplPartDetail)){
				//未找到对应记录，设为空值
				ioDpPostplPartDetail = SysUtil.getInstance(IoDpPostplPartDetail.class);
			}
			ioDpDetailInfo.setPoplInfo(ioDpPostplPartDetail);
			
			//支取控制部件信息查询
			IoDpDrawPartDetail ioDpDrawPartDetail = DpProductDao.selDrawInfoByProdcd(prodcd, false);
			if(CommUtil.isNull(ioDpDrawPartDetail)){
				//未找到对应记录，设为空值
				ioDpDrawPartDetail = SysUtil.getInstance(IoDpDrawPartDetail.class);
			}
			ioDpDetailInfo.setDrawInfo(ioDpDrawPartDetail);
			
			//支取计划控制部件信息查询
			IoDpDrawplPartDetail ioDpDrawplPartDetail = DpProductDao.selDrplInfoByProdcd(prodcd, false);
			
			if(CommUtil.isNull(ioDpDrawplPartDetail)){
				//未找到对应记录，设为空值
				ioDpDrawplPartDetail = SysUtil.getInstance(IoDpDrawplPartDetail.class);
			}
			ioDpDetailInfo.setDrplInfo(ioDpDrawplPartDetail);
			
			//到期控制部件信息查询
			IoDpMatuPartDetail ioDpMatuPartDetail = DpProductDao.selMatuInfoByProdcd(prodcd, false);
			
			if(CommUtil.isNull(ioDpMatuPartDetail)){
				//未找到对应记录，设为空值
				ioDpMatuPartDetail = SysUtil.getInstance(IoDpMatuPartDetail.class);
			}
			ioDpDetailInfo.setMatuInfo(ioDpMatuPartDetail);
			
			//利息利率部件信息查询
			IoDpIntrPartDetail ioDpIntrPartDetail = DpProductDao.selIntrInfoByProdcd(prodcd, false);
			
			if(CommUtil.isNull(ioDpIntrPartDetail)){
				//未找到对应记录，设为空值
				ioDpIntrPartDetail = SysUtil.getInstance(IoDpIntrPartDetail.class);
			}
			ioDpDetailInfo.setIntrInfo(ioDpIntrPartDetail);
			
			//违约支取利息利率部件信息查询
			Options<IoDpDfirPartDetail> ioDpDfirPartDetailOptions = new DefaultOptions<IoDpDfirPartDetail>();
			List<IoDpDfirPartDetail> ioDpDfirPartDetailList = DpProductDao.selDfirInfoByProdcd(prodcd, false);
			
			if(CommUtil.isNotNull(ioDpDfirPartDetailList)){
				
				for(IoDpDfirPartDetail IoDpDfirPartDetail : ioDpDfirPartDetailList){
					ioDpDfirPartDetailOptions.add(IoDpDfirPartDetail);
	}
			}
			
			ioDpDetailInfo.setDfirInfo(ioDpDfirPartDetailOptions);
			
			//核算部件信息查询
			IoDpCheckPartDetail ioDpCheckPartDetail = DpProductDao.selAcctInfoByProdcd(prodcd, false);
			if(CommUtil.isNull(ioDpCheckPartDetail)){
				//未找到对应记录，设为空值
				ioDpCheckPartDetail = SysUtil.getInstance(IoDpCheckPartDetail.class);
			}
			ioDpDetailInfo.setAcctInfo(ioDpCheckPartDetail);
			
			//将所查询结果塞入结果集当中
			infoList.add(ioDpDetailInfo); 
		
		}
		
		output.setInfoList(infoList);
		bizlog.debug("=========存款产品比对服务处理成功========");
	}
	
	/**
	 * 
	 * @Description: (产品垃圾数据清除) 
	 * @author chenjk
	 * @return 
	 */	
	private void delProdRubbishRecords() {
		
		//获取当前日期时间戳
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		
		bizlog.debug("===========内部户产品垃圾数据清除===========");
		int count1 = DpProductDao.selKnpBusiRubbishCount(trandt, false); //查询有无垃圾数据
		
		if(CommUtil.compare(count1, 0) > 0){
			//存在垃圾数据，则进行删除操作
			DpProductDao.delKnpBusiRubbish(trandt);
		}
		
		bizlog.debug("===========存款产品垃圾数据清除===========");
		int count2 = DpProductDao.selDppbTempRubbishCount(trandt, false);
		if(CommUtil.compare(count2, 0) > 0){
			//存在垃圾数据，则进行删除操作
			DpProductDao.delDppbTempRubbish(trandt);//删除基础属性临时表垃圾数据
			DpProductDao.delPartTempRubbish(trandt);//删除部件临时表垃圾数据
			DpProductDao.delCustTempRubbish(trandt);//删除开户临时表垃圾数据
			DpProductDao.delBrchTempRubbish(trandt);//删除机构临时表垃圾数据
			DpProductDao.delTermTempRubbish(trandt);//删除存期临时表垃圾数据
			DpProductDao.delPostTempRubbish(trandt);//删除存入临时表垃圾数据
			DpProductDao.delDrawTempRubbish(trandt);//删除支取临时表垃圾数据
			DpProductDao.delMatuTempRubbish(trandt);//删除到期临时表垃圾数据
			DpProductDao.delDfirTempRubbish(trandt);//删除违约支取利息临时表垃圾数据
			DpProductDao.delPoplTempRubbish(trandt);//删除存入计划临时表垃圾数据
			DpProductDao.delDrplTempRubbish(trandt);//删除支取计划临时表垃圾数据
			DpProductDao.delIntrTempRubbish(trandt);//删除利息利率临时表垃圾数据
			DpProductDao.delAcctTempRubbish(trandt);//删除核算临时表垃圾数据
			DpProductDao.delActpTempRubbish(trandt);//删除账户类型临时表垃圾数据
			DpProductDao.delAddtTempRubbish(trandt);//删除附加属性临时表垃圾数据
			
		}
		
		if (CommUtil.compare(count1, 0) > 0 || CommUtil.compare(count2, 0) > 0) {
			DpProductDao.delUserRubbish(trandt);//删除柜员登记薄
		}
		
	}
	
	/**
	 * 
	 * @Description: (产品号检查) 
	 * @author chenjk
	 * @return 
	 */	
	private void prodcdCheck(String prodcd){
		//产品编号为空验证
		if(CommUtil.isNull(prodcd)){
			
			throw DpModuleError.DpstComm.BNAS1480(); 
		}
	}
	
	/**
	 * 
	 * @Description: (业务大类检查) 
	 * @author chenjk
	 * @return 
	 */	
	private void busibiCheck(E_BUSIBI busibi){
		//业务大类为空验证
		if(CommUtil.isNull(busibi)){
			
			throw DpModuleError.DpstProd.BNAS1764(); 
		}
		// 业务大类必须为存款
		if (E_BUSIBI.DEPO != busibi) {
			throw DpModuleError.DpstProd.BNAS1765(); 
		}
	}

	/**
	 * 
	 * @Title: QryBrchByProd 
	 * @Description: (查询存款机构控制部件信息) 
	 * @param prodcd
	 * @param qryBrchInfos
	 * @author chenjk
	 * @date 2016年7月20日 上午18:29:29 
	 * @version V2.3.0
	 * @return 
	 */	
	@Override
	public void qryBrchByProd(String prodcd, Long start, Long count,
				cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType.QryBrchByProd.Output output) {


		//操作网点权限验证
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
		
		bizlog.debug("===========查询存款机构控制部件信息开始===========");
		long  startno = (start-1)*count;// 总记录数
		long  totlCount = 0;
		
		prodcdCheck(prodcd); //产品编号为空检查
		
		// 获取产品基础信息
		KupDppbInfo kubdpp = DpProductDao.selprodcd(prodcd, CommTools.getBaseRunEnvs().getBusi_org_id(), false);
		if (CommUtil.isNotNull(kubdpp)) {
		
			// 临时表数据
			if (E_PRODST.ASSE == kubdpp.getProdst() || E_PRODST.INPUT == kubdpp.getProdst()) {
				
				//数据位于临时表中，去临时表中查询机构信息
				Page<IoDpBrhcno> brchnoPage = DpProductDao.selTempBrchInfoByProdcd(prodcd, startno, count, totlCount, false);
				
				// 设置输出
				output.getQryBrchInfos().addAll(brchnoPage.getRecords());
				// 设置报文头总记录条数
				CommTools.getBaseRunEnvs().setTotal_count(brchnoPage.getRecordCount());
			
			// 正式表数据
			} else {
				
				//数据位于临时表中，去临时表中查询机构信息
				Page<IoDpBrhcno> brchnoPage = DpProductDao.selBrchInfoByProdcd(prodcd, startno, count, totlCount, false);
				
				// 设置输出
				output.getQryBrchInfos().addAll(brchnoPage.getRecords());
				// 设置报文头总记录条数
				CommTools.getBaseRunEnvs().setTotal_count(brchnoPage.getRecordCount());
			}
		}
		
		bizlog.debug("===========查询存款机构控制部件信息结束===========");
		
	}


	/**
	 * 
	 * @Title: QryBrchDateChangeInfo 
	 * @Description: (产品机构日期调整查询) 
	 * @param input
	 * @param output
	 * @author chenjk
	 * @date 2016年7月21日 上午15:10:29 
	 * @version V2.3.0
	 * @return 
	 */	
	@Override
	public void qryBrchDateChangeInfo(
			IoDpBrDtChgeQry input,
			cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType.QryBrchDateChangeInfo.Output output) {
		
		bizlog.debug("===========产品机构日期调整查询处理开始===========");
		
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());//操作网点权限验证
		
		//输入为空判断
		if(CommUtil.isNotNull(input.getInefdt())){
			if (input.getInefdt().length() != 8) {
				
				throw DpModuleError.DpstProd.BNAS1766();
			}
		}
		
		if(CommUtil.compare(input.getInefdt(), input.getEfctdt()) < 0){
			throw DpModuleError.DpstProd.BNAS1767();
		}
		
		if(CommUtil.isNotNull(input.getEfctdt())){
			if (input.getEfctdt().length() != 8) {
				
				throw DpModuleError.DpstProd.BNAS1768();
			}
		} 
		
//		if(CommUtil.isNotNull(input.getBusibi())){
//			if (input.getBusibi() != E_BUSIBI.DEPO) {
//				
//				throw DpModuleError.DpstProd.E0010("此接口仅支持业务大类为存款产品的查询！");
//			}
//		}
		
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		
		//分页查询
		long pageno = CommTools.getBaseRunEnvs().getPage_start(); //页码
		long pagesize = CommTools.getBaseRunEnvs().getPage_size(); //页容量
		Page<IoDpBrDtChgeQry> ioDpBrDtChgeQryPage = DpProductDao.selBrDtChgeInfo(input.getProdcd(), 
				input.getProdtx(),input.getBusibi(), input.getProdtp(), input.getPddpfg(),
				input.getDebttp(), input.getProdst(), input.getEfctdt(), input.getInefdt(), corpno,
				(pageno - 1) * pagesize, pagesize, 0, false);
		//将查询结果导出
		
		if (ioDpBrDtChgeQryPage.getRecordCount() != 0) {
			Options<IoDpBrDtChgeQry> brDtChgeOptions = new DefaultOptions<IoDpBrDtChgeQry>();
			for (IoDpBrDtChgeQry ioDpBrDtChgeQry : ioDpBrDtChgeQryPage.getRecords()) {
				brDtChgeOptions.add(ioDpBrDtChgeQry);
			}

			output.setQryInfos(brDtChgeOptions);
		}
		// 设置记录总数
		CommTools.getBaseRunEnvs().setTotal_count(ioDpBrDtChgeQryPage.getRecordCount());// 记录总数
		bizlog.debug("===========产品统计查询服务处理成功===========");
		
	}




	/**
	 * 新增产品操作柜员登记
	 * @Title: inskupDppbUser 
	 * @Description: 新增产品操作柜员登记 
	 * @param busibi 业务大类
	 * @param prodcd 产品编号
	 * @param prtrtp 产品交易类型
	 * @author liaojincai
	 * @date 2016年8月11日 上午11:38:55 
	 * @version V2.3.0
	  */
    public void inskupDppbUser(E_BUSIBI busibi, String prodcd, E_PRTRTP prtrtp) {

        // 输入项非空检查
        if (CommUtil.isNull(busibi)) {
            throw DpModuleError.DpstProd.BNAS1769();
        }
        if (CommUtil.isNull(prodcd)) {
            throw DpModuleError.DpstProd.BNAS1770();
        }
        if (CommUtil.isNull(prtrtp)) {
            throw DpModuleError.DpstProd.BNAS1771();
        }

        // 产品操作柜员登记薄信息
        KupDppbUser tblKupDppbUser = SysUtil.getInstance(KupDppbUser.class);
        tblKupDppbUser.setBusibi(busibi);// 业务大类
        tblKupDppbUser.setProdcd(prodcd);// 产品编号
        tblKupDppbUser.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
        tblKupDppbUser.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());// 交易柜员
        tblKupDppbUser.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq());// 柜员流水
        tblKupDppbUser.setPrtrtp(prtrtp);// 产品交易类型

        KupDppbUserDao.insert(tblKupDppbUser);
    }
	 
/**
 * 
 * @Title: closeCurAcc 
 * @Description: TODO(个人活期结算户销户服务) 
 * @param clsin
 * @return
 * @author xiongzhao
 * @date 2017年3月13日 下午7:57:28 
 * @version V2.3.0
 */
	@Override
	public IoDpCloseOT closeCurAcc(IoDpCloseIN clsin) {

		String custac = clsin.getCustac();
		String smrycd = clsin.getSmrycd();
		
		// 检查传入字段是否为空
		if (CommUtil.isNull(custac)) {
			throw DpModuleError.DpstProd.BNAS0935();
		}


		// 根据电子账号ID查询出电子账号
		IoCaKnaAcdc cplKnaAcdc = SysUtil.getInstance(
				IoCaSevQryTableInfo.class).getKnaAcdcOdb1(custac,
				E_DPACST.NORMAL, true);

		// 根据电子账号ID查询出电子账户分类
		E_ACCATP eAccatp = SysUtil.getInstance(
				IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac);

		// 根据电子账号ID查询出对应的个人结算户信息
		KnaAcct tblKnaAcct = CapitalTransDeal.getSettKnaAcctAc(custac);
		
		
		// 给调用方法的输入接口赋值
		IoDpCloseIN to_clsin = SysUtil.getInstance(IoDpCloseIN.class);
		to_clsin.setAccatp(eAccatp);
		to_clsin.setAcseno(null);
		to_clsin.setCardno(cplKnaAcdc.getCardno());
		to_clsin.setClsatp(E_CLSATP.CUSTAC);
		to_clsin.setCrcycd(tblKnaAcct.getCrcycd());
		to_clsin.setToacct(clsin.getToacct());
		if (CommUtil.isNotNull(clsin.getTobrch())) {
			to_clsin.setTobrch(clsin.getTobrch());
		} else {
			to_clsin.setTobrch(tblKnaAcct.getBrchno());
		}
		if (CommUtil.isNotNull(clsin.getToname())) {
			to_clsin.setToname(clsin.getToname());
		} else {
			to_clsin.setToname(tblKnaAcct.getAcctna());
		}

		
		//摘要码默认xh，非空判断是为了别的交易会用到升级。
		if(CommUtil.isNotNull(smrycd)){
			to_clsin.setSmrycd(smrycd);
		}else{
			to_clsin.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
		}

		// 调用销子户方法销掉个人活期结算户
		
		// 处理结算户利息
		//BigDecimal interest =
		InterestAndIntertax cplint	= DpCloseAcctno.prcCurrInterest(tblKnaAcct, to_clsin);
		DpAcctSvcType dpSrv = SysUtil.getInstance(DpAcctSvcType.class); //存款记账服务
		
		if(CommUtil.compare(cplint.getInstam(), BigDecimal.ZERO) > 0){
			
			
			SaveDpAcctIn saveIn = SysUtil.getInstance(SaveDpAcctIn.class); //电子账户存入记账复合类型
			
			saveIn.setAcctno(tblKnaAcct.getAcctno()); //结算账户、钱包账户
			saveIn.setBankcd("");
			saveIn.setBankna("");
			saveIn.setCardno(cplKnaAcdc.getCardno());
			saveIn.setCrcycd(tblKnaAcct.getCrcycd());
			saveIn.setCustac(tblKnaAcct.getCustac());
			saveIn.setOpacna(tblKnaAcct.getAcctna());
			saveIn.setOpbrch(tblKnaAcct.getBrchno());
			saveIn.setSmrycd(BusinessConstants.SUMMARY_FX);
			saveIn.setRemark("子账户销户结息转入");
			saveIn.setToacct(tblKnaAcct.getAcctno());
			saveIn.setTranam(cplint.getInstam());//利息（包含利息税）
			saveIn.setIschck(E_YES___.NO);
			saveIn.setStrktg(E_YES___.NO);
			dpSrv.addPostAcctDp(saveIn);
		}

		//利息税入账
		DrawDpAcctIn drawin = SysUtil.getInstance(DrawDpAcctIn.class); //支取服务输入参数
		if(CommUtil.compare(cplint.getIntxam(), BigDecimal.ZERO) > 0){
			//结算户支取记账处理				
			drawin.setAcctno(tblKnaAcct.getAcctno()); //做支取的负债账号
			drawin.setAuacfg(E_YES___.NO);
			drawin.setCardno(cplKnaAcdc.getCardno());
			drawin.setCrcycd(tblKnaAcct.getCrcycd());
			drawin.setCustac(tblKnaAcct.getCustac());
			drawin.setLinkno(null);
			drawin.setOpacna(tblKnaAcct.getAcctna());
			drawin.setToacct(tblKnaAcct.getAcctno()); //结算账号
			drawin.setTranam(cplint.getIntxam());
			drawin.setSmrycd(BusinessConstants.SUMMARY_JS);// 摘要码-缴税
			drawin.setStrktg(E_YES___.NO);
			dpSrv.addDrawAcctDp(drawin);
			
			
		}		
		tblKnaAcct = CapitalTransDeal.getSettKnaAcctAc(custac);
		// 处理结算户余额
		BigDecimal onbal = DpCloseAcctno.prcCurrOnbal(tblKnaAcct, to_clsin);

		// 计算转出交易金额
		BigDecimal bigTranam = onbal;
		
		IoDpCloseOT cplClosot = SysUtil.getInstance(IoDpCloseOT.class);
		cplClosot.setCrcycd(tblKnaAcct.getCrcycd());
		cplClosot.setSettbl(bigTranam);
		
		return cplClosot;
    }

    /**
     * 预约购买产品管理
     */
    public Options<ReserveProductOut> magDpReserveProduct(E_OPERAT operat, String cardno, String crcycd, String prodcd, E_TERMCD depttm,
            String restdt, E_CYCLES cycles, Long recycl, E_REBYPE rebype, BigDecimal quanmy, BigDecimal holdmy) {
        // 预约购买产品集合
        Options<ReserveProductOut> lstKupDprebypdInfo = new DefaultOptions<ReserveProductOut>();
        // 管理功能码
        switch (operat) {

        // 新增
        case INSERT: {
            // 货币代号
            if (CommUtil.isNull(crcycd)) {
                crcycd = "CNY"; // 默认人民币
            }
            // 产品编号
            if (CommUtil.isNull(prodcd)) {
                throw DpModuleError.DpstProd.E0010("预约购买产品输入信息货币代号不能为空");
            }
            // 存期
            if (CommUtil.isNull(depttm)) {
                throw DpModuleError.DpstProd.E0010("预约购买产品输入信息存期不能为空");
            }
            // 首次预约日期
            if (CommUtil.isNull(restdt)) {
                restdt = CommTools.getBaseRunEnvs().getTrxn_date(); // 默认是当天
            }
            // 预约周期类型
            if (CommUtil.isNull(cycles)) {
                throw DpModuleError.DpstProd.E0010("预约购买产品输入信息预约周期类型不能为空");
            }
            // 预约周期
            if (CommUtil.isNull(recycl)) {
                throw DpModuleError.DpstProd.E0010("预约购买产品输入信息预约周期不能为空");
            }
            // 预约方式
            if (CommUtil.isNull(rebype)) {
                throw DpModuleError.DpstProd.E0010("预约购买产品输入信息预约方式不能为空");
            }

            if (E_REBYPE.DEGM == rebype) {
                // 定额购买金额
                if (CommUtil.isNull(quanmy)) {
                    throw DpModuleError.DpstProd.E0010("定额购买预约购买产品输入信息定额购买金额不能为空");
                }
            } else if (E_REBYPE.CEGM == rebype) {
                // 留存金额
                if (CommUtil.isNull(holdmy)) {
                    holdmy =  BigDecimal.ZERO; // 默认为0（全额购买）
                }
            }

            // 检查卡号的合法性
            IoCaKnaAcdc otacdc = ActoacDao.selKnaAcdc(cardno, false);
            if (CommUtil.isNull(otacdc)) {
                throw DpModuleError.DpstComm.BNAS0750();
            }

            if (otacdc.getStatus() == E_DPACST.CLOSE) {
                throw DpModuleError.DpstComm.BNAS0441();
            }

            // 预约购买产品信息记录表
            KupDprebypd tblKupDprebypd = KupDprebypdDao.selectOne_odb1(cardno, prodcd, false);
            if (CommUtil.isNotNull(tblKupDprebypd)) {
                if (E_REBYST.STOP == tblKupDprebypd.getRebyst()) {
                    KupDprebypdDao.deleteOne_odb1(cardno, prodcd);
                } else if (E_REBYST.NORMAL == tblKupDprebypd.getRebyst()) {
                    throw DpModuleError.DpstProd.E0010("该卡号已经预约购买该产品");
                }
            } else {
                tblKupDprebypd = SysUtil.getInstance(KupDprebypd.class);
            }
            tblKupDprebypd.setCardno(cardno); // 卡号
            tblKupDprebypd.setCrcycd(crcycd); // 货币代号
            tblKupDprebypd.setRebyst(E_REBYST.NORMAL); // 预约状态
            tblKupDprebypd.setRecycl(recycl); // 预约周期
            tblKupDprebypd.setCycles(cycles); // 预约周期类型
            tblKupDprebypd.setProdcd(prodcd); // 产品编号
            tblKupDprebypd.setDepttm(depttm); // 存期
            tblKupDprebypd.setRebype(rebype); // 预约方式
            tblKupDprebypd.setHoldmy(holdmy); // 留存金额
            tblKupDprebypd.setQuanmy(quanmy); // 定额购买金额
            tblKupDprebypd.setRestdt(restdt); // 首次预约日期

            tblKupDprebypd.setRenxdt(restdt); // 下次预约日期

            KupDprebypdDao.insert(tblKupDprebypd);
            break;
        }

        //查询
        case SELECT: {
            // 记录数
            long lTotalCount = 0;
            // 页码
            long iPageno = CommTools.getBaseRunEnvs().getPage_start();
            // 页面大小
            long iPgsize = CommTools.getBaseRunEnvs().getPage_size();

            // 根据卡号查询预约购买产品表 
            Page<ReserveProductOut> pageKupDprebypdInfo = DpProductDao.selPageKupDprebypdInfo(
                    cardno, (iPageno - 1) * iPgsize, iPgsize, lTotalCount, false);

            // 将所有记录放入返回集合中
            lstKupDprebypdInfo.addAll(pageKupDprebypdInfo.getRecords());

            // 设置总记录数
            CommTools.getBaseRunEnvs().setTotal_count(pageKupDprebypdInfo.getRecordCount());
            break;
        }

        // 删除
        case DELETE: {
            // 产品编号
            if (CommUtil.isNull(prodcd)) {
                throw DpModuleError.DpstProd.E0010("删除预约购买产品输入信息产品编号不能为空");
            }
            // 预约购买产品信息记录表
            KupDprebypd tblKupDprebypd = KupDprebypdDao.selectOne_odb1(cardno, prodcd, false);
            tblKupDprebypd.setRebyst(E_REBYST.STOP); // 预约状态
            // 更新预约购买产品信息记录表为暂停状态
            KupDprebypdDao.updateOne_odb1(tblKupDprebypd);

            break;
        }

        default: {
            throw DpModuleError.DpstProd.E0010("管理功能码只能选择I-新增、S-查询、D-删除");
        }
 
        }
        return lstKupDprebypdInfo;
    }




	@Override
	public Options<IoDpSalInfo> qryPorSalInfo(String prodcd, String plstad,
			String ploved) {
		bizlog.debug("===========产品销售情况查询===========");
		long pageno = CommTools.getBaseRunEnvs().getPage_start(); //页码
	    long pagesize = CommTools.getBaseRunEnvs().getPage_size(); //页容量
	    long counts = CommTools.getBaseRunEnvs().getTotal_count();
	    
	    Page<IoDpSalInfo> page = DpProductDao.selKupDppdSalInfo(plstad, ploved,
	    		CommTools.getBaseRunEnvs().getBusi_org_id(), prodcd, (pageno - 1) * pagesize, pagesize, counts, false);
	    
	    Options<IoDpSalInfo> optIoDpSalInfo = new DefaultOptions<IoDpSalInfo>();
	    
	    CommTools.getBaseRunEnvs().setTotal_count(page.getRecordCount());
	    
	    optIoDpSalInfo.setValues(page.getRecords());
	    
		return optIoDpSalInfo;
	}









}
