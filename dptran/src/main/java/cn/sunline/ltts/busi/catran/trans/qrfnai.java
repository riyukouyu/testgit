package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.catran.trans.intf.Qrfnai.Output.FnacctInfoList;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.fn.IoFnTable.IoFnFnaAcct;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_QRACWY;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PDTPDL;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PRODTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;


public class qrfnai {
	/**
	 * 电子账户理财投资账户详情查询(柜面)
	 * **/
	private static BizLog log = BizLogUtil.getBizLog(qrfnai.class);
	
	public static void QryFinaceAsset( final cn.sunline.ltts.busi.catran.trans.intf.Qrfnai.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Qrfnai.Output output){
		log.debug("<<==========电子账户理财投资账户详情查询==========>>");
		
		String acctCorpno = null;
		//输入参数检查
		if(CommUtil.isNull(input.getCardno()) && CommUtil.isNull(input.getIdtfno())){

			throw CaError.Eacct.BNAS0919();
		}
		
		if(CommUtil.isNotNull(input.getIdtftp()) && CommUtil.isNull(input.getIdtfno())){

			throw CaError.Eacct.BNAS0157();
		}
		
		if(CommUtil.isNull(input.getIdtftp()) && CommUtil.isNotNull(input.getIdtfno())){
			
			throw DpModuleError.DpstComm.BNAS0150();
		}
		
//		if(CommUtil.isNull(input.ggetCrcycd())){
//			
//			throw DpModuleError.DpstComm.BNAS1101();
//		}
		
		if(CommUtil.isNull(input.getProdtp())){
			
			throw CaError.Eacct.BNAS1051();
		}
		
		if(CommUtil.isNull(input.getQracwy())){
			
			throw CaError.Eacct.BNAS1072();
		}
		if(CommUtil.isNull(input.getQractp())){

			throw CaError.Eacct.BNAS1272();
		}
		
		
		String acctac = null; //电子账号
		if (CommUtil.isNotNull(input.getIdtfno()) && CommUtil.isNotNull(input.getIdtftp())) {
			IoCaKnaCust cplKnaCust =  EacctMainDao.selByCusInfo(input.getIdtftp(), input.getIdtfno(), false);
			//检查查询记录是否为空
			if (CommUtil.isNull(cplKnaCust)) {

				throw CaError.Eacct.BNAS0389();
			}
			if (input.getQracwy() == E_QRACWY.CUSQRY) {
//				CommTools.getBaseRunEnvs().setBusi_org_id(cplKnaCust.getCorpno());
			}
			acctac = cplKnaCust.getCustac();
			acctCorpno = cplKnaCust.getCorpno();
		}
		if(CommUtil.isNotNull(input.getCardno())){
			IoCaKnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCard(input.getCardno(), false);
			//检查查询记录是否为空
			if (CommUtil.isNull(tblKnaAcdc)) {

				throw CaError.Eacct.BNAS0391();
			}
			if (input.getQracwy() == E_QRACWY.CUSQRY) {
//				CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaAcdc.getCorpno());
			}
			if(CommUtil.isNotNull(acctac)){
				if (!CommUtil.equals(acctac, tblKnaAcdc.getCustac())) {

					throw CaError.Eacct.BNAS0920();	
				}
				
			}else{
				acctac = tblKnaAcdc.getCustac();
			}
			
			acctCorpno = tblKnaAcdc.getCorpno();
			
		}
		
		//若查询方式为柜员查询，则只能查询本行社的信息
		if (input.getQracwy() == E_QRACWY.TELQRY) {
			if (!CommUtil.equals(CommTools.getBaseRunEnvs().getBusi_org_id(), "999")) {
				String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
				if (CommUtil.compare(corpno, acctCorpno) != 0) {

					throw CaError.Eacct.BNAS0112();
				}
			}
		}
		
//		IoFnSevQryTableInfo fnqry = SysUtil.getInstance(IoFnSevQryTableInfo.class);
		//CommTools.getBaseRunEnvs()
		long pageno = CommTools.getBaseRunEnvs().getPage_start();
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();
		
		//页数
		if(CommUtil.isNull(pageno)){

			throw CaError.Eacct.BNAS0249();
		}
		
		if(pageno <= 0){

			throw CaError.Eacct.BNAS0250();
		}
		
		//页容量
		if(CommUtil.isNull(pgsize)){

			throw DpModuleError.DpstComm.BNAS0252();
		}
		
		if(pgsize <= 0){

			throw CaError.Eacct.BNAS0251();
		}
		E_CUACST cuacst = SysUtil.getInstance(
				IoCaSrvGenEAccountInfo.class).selCaStInfo(acctac);
		if (input.getAcctst()!=cuacst) {
			return;
		}
		if(input.getProdtp() == E_PRODTP.MANAGE){ //理财
//			Options<IoFnFnaAcct> lstFnFnaAccts = fnqry.fna_acct_selectAll_odb4(acctac, Integer.parseInt(String.valueOf(pageno)), Integer.parseInt(String.valueOf(pgsize)));
//			if(CommUtil.isNotNull(lstFnFnaAccts) && lstFnFnaAccts.size() > 0){
//				
//				for(IoFnFnaAcct fina : lstFnFnaAccts){
//					FnacctInfoList fnacct = SysUtil.getInstance(FnacctInfoList.class);
//					fnacct.setAcctno(fina.getAcctno());//子账号
//					fnacct.setAcctna(fina.getCustna());//子账户名称
//					fnacct.setCrcycd(fina.getCrcycd());//币种
//					fnacct.setCsextg(fina.getCsextg());//钞汇属性
//					fnacct.setProdtp(input.getProdtp());//产品类型
//					fnacct.setPdtpdl(E_PDTPDL.MANAGE);//产品类型细分
//					fnacct.setProdcd(fina.getProdcd());//产品代码
//					fnacct.setProdna(fina.getProdna());//产品名称
//					fnacct.setOnlnbl(fina.getNowtam());//当前余额
//					fnacct.setIntrvl(fina.getFtrate().toString()+"~"+fina.getTtrate().toString());//年华收益率
//					fnacct.setTermtm(DateTools2.calDays(fina.getOpendt(), fina.getEndday(), 0, 0));//期限
//					fnacct.setOpendt(fina.getOpendt());//开户日期
//					fnacct.setOpensq(fina.getOpensq());//开户流水
//					fnacct.setOpenbr(fina.getOpenbr());//开户机构
//					fnacct.setMatudt(fina.getEndday());//到期日期
//					fnacct.setClosdt(fina.getClosdt());//销户日期
//					fnacct.setClossq(fina.getClossq());//销户流水
//					fnacct.setDpacst(fina.getDpacst());//子账户状态
//					//fnacct.setDpacsz("0000000000"); //子账户状态字默认10个0
//					
//					output.getFnacctInfoList().add(fnacct);
//				}
//				
//			}
			
		}else if(input.getProdtp() == E_PRODTP.FUND){ //基金
			//TODO:查询基金账户表
		}else if(input.getProdtp() == E_PRODTP.PRECIOUS){ //贵金属
			//TODO：查询贵金属账户表
		}else{

			throw CaError.Eacct.BNAS1146(input.getProdtp().getLongName());
		}
	}
}
