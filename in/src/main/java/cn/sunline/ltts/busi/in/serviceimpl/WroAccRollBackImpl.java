package cn.sunline.ltts.busi.in.serviceimpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.clwj.msap.util.sequ.MsSeqUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InTranOutDao;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.namedsql.WrAccRbDao;
import cn.sunline.ltts.busi.in.servicetype.WroAccRollBack.InsNestbk.Input;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.KnsCmbk;
import cn.sunline.ltts.busi.in.tables.In.KnsPaya;
import cn.sunline.ltts.busi.in.tables.In.KnsPayaDao;
import cn.sunline.ltts.busi.in.tables.In.KnsPayd;
import cn.sunline.ltts.busi.in.tables.In.KnsPaydDao;
import cn.sunline.ltts.busi.in.tables.In.KnsStrk;
import cn.sunline.ltts.busi.in.tables.In.KnsStrkDao;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PayaListInfo;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PayaListSecond;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PaydListInfo;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PaydListSecond;
import cn.sunline.ltts.busi.in.type.InWroAccManagerType.StrkpsInfo;
import cn.sunline.ltts.busi.in.type.InWroAccManagerType.StrkpsQryIn;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaPayaDetail;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaPaydDetail;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbAcin;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbPidl;
import cn.sunline.ltts.busi.iobus.type.IoInTable.IoGlKnaAcct;
//import cn.sunline.ltts.busi.iobus.type.IoInWriteOffComplex.KnsStrkInput;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccountClearInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaHknsAcsq;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnsTran;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
//import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchUserQt;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLERST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STTRTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ACUTTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_OPERWY;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_OPERWZ;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYAST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYATP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYDST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_QUERCO;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_REBUWA;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_STRKST;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;
 /**
  * 错账管理实现
  * 错账管理实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="WroAccRollBackImpl", longname="错账管理实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class WroAccRollBackImpl implements cn.sunline.ltts.busi.in.servicetype.WroAccRollBack{
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(WroAccRollBackImpl.class);
 
	 /**
		 * @Author chenjk
		 *         <p>
		 *         <li>功能说明：隔日错账冲正信息查询</li>
		 *         </p>
		 * @param prtrdt
		 *            原错账日期
		 * @param prtrsq
		 *            原错账流水
		 * @param wronbr
		 *            原错账机构		 
		 * @param numbsq
		 *            套号
		 * @param output
		 *            输出信息
		 * @return
		 */
	 
	@Override
	public void selNestqr(
			String prtrdt,
			String prtrsq,
			String wronbr,
			String numbsq,
			cn.sunline.ltts.busi.in.servicetype.WroAccRollBack.SelNestqr.Output output) {
		
		
		if(CommUtil.isNull(numbsq)){
			throw InError.comm.E0003("套号不能为空！");
		}		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		
		
		//modify by wuzx 根据套号查询
		//List<KnsStrk> knsStrkList = KnsStrkDao.selectAll_kns_strk_odx2(prtrdt, prtrsq, wronbr, true);
		List<KnsStrk> knsStrkList = KnsStrkDao.selectAll_kns_strk_odx4(numbsq, trandt, false);
		if(null==knsStrkList || knsStrkList.size()<=0){
			throw InError.comm.E0003("根据套号未查询到相应的记录！");
		}
		//modify by wuzx 根据套号查询表外账户套号查询
		if(knsStrkList.size() != 2){
			KnsStrk knsStrk1 = knsStrkList.get(0);
			numbsq = knsStrk1.getNumbsq();
			output.setPrtrdt(knsStrk1.getPrtrdt());
			output.setPrtrsq(knsStrk1.getPrtrsq());
			output.setWronbr(knsStrk1.getWronbr());
			output.setWronte(knsStrk1.getWronte());
			output.setNumbsq(numbsq);
			output.setReason(knsStrk1.getReason());
			output.setTrantp(knsStrk1.getTrantp());
			//账号1值获取
			output.setCustac(knsStrk1.getCustac());
			output.setAcctna(knsStrk1.getAcctna());
			output.setCrcycd(knsStrk1.getCrcycd());
			output.setCsextg(knsStrk1.getCsextg());
			output.setRebuwo(knsStrk1.getRebuwo());
			output.setAmntcd(knsStrk1.getAmntcd());
			output.setHappbl(knsStrk1.getHappbl());
			output.setCharlg(knsStrk1.getCharlg());
			output.setTranus(knsStrk1.getTranus());//操作柜员
			output.setChckus(knsStrk1.getChckus());//复核柜员
			return ;
		}
		KnsStrk knsStrk1 = knsStrkList.get(0);
		KnsStrk knsStrk2 = knsStrkList.get(1);
		numbsq = knsStrk1.getNumbsq();
		output.setPrtrdt(knsStrk1.getPrtrdt());
		output.setPrtrsq(knsStrk1.getPrtrsq());
		output.setWronbr(knsStrk1.getWronbr());
		output.setWronte(knsStrk1.getWronte());
		output.setNumbsq(numbsq);
		output.setReason(knsStrk1.getReason());
		output.setTrantp(knsStrk1.getTrantp());
		//账号1值获取
		output.setCustac(knsStrk1.getCustac());
		output.setAcctna(knsStrk1.getAcctna());
		output.setCrcycd(knsStrk1.getCrcycd());
		output.setCsextg(knsStrk1.getCsextg());
		output.setRebuwo(knsStrk1.getRebuwo());
		output.setAmntcd(knsStrk1.getAmntcd());
		output.setHappbl(knsStrk1.getHappbl());
		output.setCharlg(knsStrk1.getCharlg());
		output.setTranus(knsStrk1.getTranus());//操作柜员
		output.setChckus(knsStrk1.getChckus());//复核柜员
		//账号2值获取
		output.setCustaa(knsStrk2.getCustac());
		output.setAcctme(knsStrk2.getAcctna());
		output.setCrcyce(knsStrk2.getCrcycd());
		output.setCsexte(knsStrk2.getCsextg());
		output.setRebuwa(knsStrk2.getRebuwo());
		output.setAmntca(knsStrk2.getAmntcd());
		output.setHappba(knsStrk2.getHappbl());
		output.setCharla(knsStrk2.getCharlg());
		output.setStrkst(knsStrk2.getStrkst());
		output.setTranus(knsStrk2.getTranus());//操作柜员
		output.setChckus(knsStrk2.getChckus());//复核柜员
		//挂账1处理
		if(knsStrk1.getCharlg() ==E_PAYATP._1){
			List<KnsPaya> knsPayaList = KnsPayaDao.selectAll_kns_paya_odx3(numbsq, knsStrk1.getCustac(),trandt, true);
			Options<PayaListInfo> knsPayaOptions = new DefaultOptions<PayaListInfo>();
			for(KnsPaya knsPaya : knsPayaList){
				if(knsPaya.getPayast() ==E_PAYAST.ZF){
					continue;
				}
				PayaListInfo payaListInfo = SysUtil.getInstance(PayaListInfo.class);
				payaListInfo.setPayabr(knsPaya.getPayabr());
				payaListInfo.setPayamn(knsPaya.getPayamn());
				payaListInfo.setPayasq(knsPaya.getPayasq());
				payaListInfo.setToacna(knsPaya.getToacna());
				payaListInfo.setToacno(knsPaya.getToacct());
				knsPayaOptions.add(payaListInfo);
			}
			output.setPayaListInfoFirst(knsPayaOptions);
		}
		
		//销账1处理
		if(knsStrk1.getCharlg() ==E_PAYATP._2){
			List<KnsPayd> knsPaydList = KnsPaydDao.selectAll_kns_payd_odx3(numbsq, knsStrk1.getCustac(),trandt, true);
			Options<PaydListInfo> knsPaydOptions = new DefaultOptions<PaydListInfo>();
			for(KnsPayd knsPayd : knsPaydList){
				if(knsPayd.getPaydst() == E_PAYDST.ZF){
					continue;
				}
				PaydListInfo paydListInfo = SysUtil.getInstance(PaydListInfo.class);
				paydListInfo.setBalanc(knsPayd.getRsdlmn());
				paydListInfo.setCharam(knsPayd.getPayamn());
				paydListInfo.setCharsq(knsPayd.getPayasq());//挂账序号
				paydListInfo.setPaydsq(knsPayd.getPaydsq());//销账序号
				knsPaydOptions.add(paydListInfo);
			}
			output.setPaydListInfoFirst(knsPaydOptions);
		}
		
		//挂账2处理
		if(knsStrk2.getCharlg() ==E_PAYATP._1){
			
			List<KnsPaya> knsPayaList = KnsPayaDao.selectAll_kns_paya_odx3(numbsq, knsStrk2.getCustac(), trandt, true);
			Options<PayaListSecond> knsPayaOptions = new DefaultOptions<PayaListSecond>();
			
			for(KnsPaya knsPaya : knsPayaList){
				
				if(knsPaya.getPayast() ==E_PAYAST.ZF){
					continue;
				}
				
				PayaListSecond payaListSecond = SysUtil.getInstance(PayaListSecond.class);
				payaListSecond.setPayabh(knsPaya.getPayabr());
				payaListSecond.setPayamt(knsPaya.getPayamn());
				payaListSecond.setPayase(knsPaya.getPayasq());
				payaListSecond.setToacnm(knsPaya.getToacna());
				payaListSecond.setToacnb(knsPaya.getToacct());
				knsPayaOptions.add(payaListSecond);
			}
			
			output.setPayaListInfoSecond(knsPayaOptions);
		}
		
		//销账2处理
		if(knsStrk2.getCharlg() ==E_PAYATP._2){
			
			List<KnsPayd> knsPaydList = KnsPaydDao.selectAll_kns_payd_odx3(numbsq, knsStrk2.getCustac(), trandt, true);
			Options<PaydListSecond> knsPaydOptions = new DefaultOptions<PaydListSecond>();
			
			for(KnsPayd knsPayd : knsPaydList){
				
				if(knsPayd.getPaydst() == E_PAYDST.ZF){
					continue;
				}
				
				PaydListSecond paydListSecond = SysUtil.getInstance(PaydListSecond.class);
				paydListSecond.setBalane(knsPayd.getRsdlmn());
				paydListSecond.setCharan(knsPayd.getPayamn());
				paydListSecond.setCharse(knsPayd.getPayasq());//挂账序号
				paydListSecond.setPaydse(knsPayd.getPaydsq());//销账序号
				knsPaydOptions.add(paydListSecond);
			}
			output.setPaydListInfoSecond(knsPaydOptions);
		}
		
	}

	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-07 08：32</li>
	 *         <li>隔日错账冲正录入或修改</li>
	 *         </p>
	 * @param input
	 *           隔日错账冲正录入信息
	 * */
	@Override
	public void insNestbk(Input input,cn.sunline.ltts.busi.in.servicetype.WroAccRollBack.InsNestbk.Output output) {
		
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();						
		String mtdate = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		String timetm = DateTools2.getCurrentTimestamp(); //交易日期
		String mntrsq = "";
		//如果为转账，则账号2，红蓝字2，借贷标识2，币种必输
		if(input.getTrantp()==E_STTRTP.I){
			if(CommUtil.isNull(input.getCustaa())||CommUtil.isNull(input.getRebuwa())
					||CommUtil.isNull(input.getAmntca())||CommUtil.isNull(input.getHappba())
					||CommUtil.isNull(input.getCrcyce())){
				throw InError.comm.E0003("账号2，红蓝字2，借贷标识2，发生额2，币种为必输");
			}
		}
		//add by xionglz 20171127
		/*IoCaKnsTran knstrantbl1 =SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnsTranOdb1(input.getPrtrsq(), input.getPrtrdt(), timetm);
		if(CommUtil.isNotNull(input.getPrtrsq())){//如果流水号不为空但交易流水表中没有这条数据			
			if(CommUtil.isNull(knstrantbl)){
				throw InError.comm.E0003("原交易流水不存在！");
			}
		}*/
		if (CommUtil.isNotNull(input.getPrtrsq()) || !"".equals(input.getPrtrsq())) {
			KnsStrk tblknsStrk = WrAccRbDao.selKnsStrkByPrtrsq(input.getPrtrsq(), false);
			if (CommUtil.isNotNull(tblknsStrk)) {
				//throw InError.comm.E0003("该流水已做过错账调整");
				if(input.getOperwy() == E_OPERWY.INS&&tblknsStrk.getStrkst() != E_STRKST.ZF){
					throw InError.comm.E0003("该流水已做过错账调整");
				}
				if(input.getOperwy() == E_OPERWY.UPD&&tblknsStrk.getStrkst() != E_STRKST.WFH){
					throw InError.comm.E0003("该流水已做过错账调整");
				}
			}
		}
		if(CommUtil.isNotNull(input.getPrtrsq())){//如果流水号不为空但交易流水表中没有这条数据
			boolean isExistAcsq = false;
			//List<String> dcnList = DcnUtil.findAllDcnNosWithAdmin();
			//for(String dcn : dcnList) {//根据DCN号循环外调，查询各节点是否存在这条流水
				IoCaKnsTran knstrantbl = CommTools.getRemoteInstance(IoCaSevQryTableInfo.class).getKnsTranOdb1(input.getPrtrsq(), input.getPrtrdt(), null);
				if(CommUtil.isNotNull(knstrantbl) && !CommUtil.equals("", knstrantbl.getMntrsq())) {
					mntrsq = knstrantbl.getMntrsq();
					bizlog.debug("返回的knstrantbl[%s]",knstrantbl.toString());
					bizlog.debug("主交易流水[%s]",mntrsq);
					isExistAcsq = true;
					//break;
				}
			//}
			if(!isExistAcsq) {
				throw InError.comm.E0003("原交易流水不存在！");
			}
		}
		bizlog.debug("最终主交易流水[%s]",mntrsq);
		if(CommUtil.isNotNull(input.getPrtrsq())&&CommUtil.compare(input.getPrtrdt(), trandt) >= 0){
			throw InError.comm.E0003("原交易日期必须小于当前交易日期");
		}
		//表外记账新增或修改
		if(input.getTrantp()==E_STTRTP.O){
			//表外记账
			String numbsq=addOutTable(input);
			//设置套号
			output.setAcstno(numbsq);
			return ;
		}
		//校验借贷发生额是否相等(带红蓝字)
		
		if(CommUtil.compare(input.getHappba(), BigDecimal.ZERO)<=0||CommUtil.compare(input.getHappbl(), BigDecimal.ZERO)<=0){
			throw InError.comm.E0003("发生额不能小于等于0！");
		}
		if(input.getAmntcd()!=input.getAmntca()){
			if(input.getRebuwa()!=input.getRebuwo()){
				throw InError.comm.E0003("借贷方向不同时，红蓝字必须相同！");
			}else if(input.getHappba().equals(input.getHappbl())!=true){
				throw InError.comm.E0003("借贷方向不同时，发生额必须相同！");
			}
			
		}else if(input.getAmntcd()==input.getAmntca()){
			if(input.getRebuwa()==input.getRebuwo()){
				throw InError.comm.E0003("借贷方向相同时，红蓝字必须不同！");
			}else if(input.getHappba().equals(input.getHappbl())!=true){
				throw InError.comm.E0003("借贷方向相同时，发生额必须相同！");
			}
		}		
		//账号验证
		String custac = input.getCustac(); //账号1
		String custaa = input.getCustaa(); //账号2
		String corpno1 = "";
		String corpno2 = "";
		E_STTRTP  trantp = input.getTrantp();//交易类型
		

		
		GlKnaAcct glKnaAcct1 = InQuerySqlsDao.sel_GlKnaAcct_by_acct(custac, false);

		if(CommUtil.isNull(glKnaAcct1)){
			IoCaKnaAcdc tblKnaAcdc = CommTools.getRemoteInstance(IoCaSevQryTableInfo.class).getKnaAcdcByCardno(custac, false);
			if(CommUtil.isNull(tblKnaAcdc)){
				throw InError.comm.E0003("账号1既不是内部户也不是电子账户");
			}
			/*//判断输入账号是否是交易流水中的账号
			if((!CommUtil.equals(custac,glKnaAcct1.getAcctno()))&&(!CommUtil.equals(custac,tblKnaAcdc.getCustac()))){
				throw InError.comm.E0003("账号1不是原账号");
			}*/
		
			//改为外调服务
			CommTools.getRemoteInstance(IoCaSrvGenEAccountInfo.class).checkNestbkStatus(tblKnaAcdc.getCustac(),
					mntrsq,input.getRebuwo(),
					input.getAmntcd(),input.getHappbl());//(电子账号、主交易流水、红蓝字、借贷标识、发生额)
			//设置机构号
			corpno1 = tblKnaAcdc.getCorpno();

		}else{
			//支持表外收付
			/*if(glKnaAcct1.getIoflag() == E_IOFLAG.OUT){
				throw InError.comm.E0003("请输入正确的账号");
			}*/
			if(glKnaAcct1.getAcctst() !=E_INACST.NORMAL){
				throw InError.comm.E0003("账号"+custac+"已销户！");
			}
			corpno1 = glKnaAcct1.getCorpno();
		}
	
		//账号2是否电子账户
		//先查询是否内部户  内部户只在管理节点 不需要外调
		GlKnaAcct glKnaAcct2 =InQuerySqlsDao.sel_GlKnaAcct_by_acct(custaa, false);
		if(CommUtil.isNull(glKnaAcct2)){
			IoCaKnaAcdc ioCaKnaAcdc2 = CommTools.getRemoteInstance(IoCaSevQryTableInfo.class).getKnaAcdcByCardno(custaa, false);						
			if(CommUtil.isNull(ioCaKnaAcdc2)){
				throw InError.comm.E0003("账号2既不是内部户也不是电子账户");
			}
			
			/*//判断输入账号是否是交易流水中的账号
			if((!CommUtil.equals(corpno2,glKnaAcct2.getAcctno()))&&(!CommUtil.equals(corpno2,ioCaKnaAcdc2.getCustac()))){
				throw InError.comm.E0003("账号2不是原账号");
			}
			*/
			CommTools.getRemoteInstance(IoCaSrvGenEAccountInfo.class).checkNestbkStatus(ioCaKnaAcdc2.getCustac(),
					mntrsq,input.getRebuwa(),
					input.getAmntca(),input.getHappba());//(电子账号、主交易流水、红蓝字、借贷标识、发生额)
			corpno2 = ioCaKnaAcdc2.getCorpno();
		
		}else{
			//现支持表外收付
			/*if(glKnaAcct2.getIoflag() == E_IOFLAG.OUT){
				throw InError.comm.E0003("请输入正确的账号");
			}*/
			if(glKnaAcct2.getAcctst() !=E_INACST.NORMAL){
				throw InError.comm.E0003("账号"+custaa+"已销户！");
			}			
			corpno2 = glKnaAcct2.getCorpno();
		}
		
		
		
		if(!CommUtil.equals(corpno1, corpno2)){
			throw InError.comm.E0003("输入的账号不属于同机构");
		}
		
		if(!CommUtil.equals(input.getCrcycd(),input.getCrcyce())){
			throw InError.comm.E0003("两账号币种不同");
		}
		//add by wuzx 20161222 增加外币为日元时金额控制- beg
		/*
		if(input.getCrcycd()==E_CRCYCD.JPY){
			String str = input.getHappbl().toString();
			String str1 = str.substring(str.length()-3, str.length());
			if(!str1.equals(".00")){
				throw InError.comm.E0003("日元最小单位为元！");
			}
		}
		if(input.getCrcyce()==E_CRCYCD.JPY){
			String str = input.getHappba().toString();
			String str1 = str.substring(str.length()-3, str.length());
			if(!str1.equals(".00")){
				throw InError.comm.E0003("日元最小单位为元！");
			}
		}
		*/
		//add by wuzx 20161222 增加外币为日元时金额控制- end
		//红蓝字校验by xionglz 去除红蓝字校验
//		if(input.getRebuwo() != input.getRebuwa()){
//			throw InError.comm.E0003("同一笔记录，红蓝字应相同");
//		}
		//跨年不允许红字
		String prtrYear = input.getPrtrdt().substring(0, 4);
		String tranYear = trandt.substring(0, 4);
//		if(input.getRebuwo() == E_REBUWA.B){
//			if(CommUtil.equals(prtrYear, tranYear)){
//				throw InError.comm.E0003("原交易日期在本年，应为红字记账");
//			}
//		}
		if(input.getRebuwo() == E_REBUWA.R||input.getRebuwa() == E_REBUWA.R){
			if(CommUtil.isNotNull(input.getPrtrdt())&&!CommUtil.equals(prtrYear, tranYear)){
				throw InError.comm.E0003("原交易日期不是本年，不允许红字！");
			}
		}
		
		//录入套号input里面取
		
		KnsStrk knsStrk1 = SysUtil.getInstance(KnsStrk.class);
		String numbsq = "";
		if(input.getOperwy() == E_OPERWY.INS){
			
			numbsq = input.getNumbsq();//改为界面输入过来
			
			//关联错账冲正查询套号是否已存在  add by wuzhixiang 20161230
			List<KnsCmbk> knsCmbks = InTranOutDao.selKnsCmbkByAcst(input.getNumbsq(), CommTools.getBaseRunEnvs().getTrxn_date(), false);
        	List<KnsStrk> knsStrks = WrAccRbDao.selKnsStrkByNumbsq(input.getNumbsq(), CommTools.getBaseRunEnvs().getTrxn_date(), false);
        	
        	if(CommUtil.isNotNull(knsCmbks)||CommUtil.isNotNull(knsStrks)){
        		throw InError.comm.E0015(input.getNumbsq());    		 
        	}

			knsStrk1.setNumbsq(numbsq);
			knsStrk1.setSerlno(0);
			output.setAcstno(numbsq);//套号
		}else if(input.getOperwy() == E_OPERWY.UPD){
			
			if (CommUtil.isNull(input.getNumbsq())){
				throw InError.comm.E0003("修改时，套号不能为空");
			}
			knsStrk1 = KnsStrkDao.selectOne_kns_strk_odx1(input.getNumbsq(), 0, trandt, true);
			if(knsStrk1.getStrkst() != E_STRKST.WFH){
				throw InError.comm.E0003("当前状态不允许修改操作");
			}
		}
		
		//账号1处理
		knsStrk1.setPrtrdt(input.getPrtrdt());
		knsStrk1.setPrtrsq(input.getPrtrsq());//如果流水为空，则给none
		knsStrk1.setWronbr(input.getWronbr());
		knsStrk1.setWronte(input.getWronte());
		knsStrk1.setReason(input.getReason());
		knsStrk1.setStrkst(E_STRKST.WFH);
		
		
		knsStrk1.setCustac(input.getCustac());
		knsStrk1.setAcctna(input.getAcctna());
		knsStrk1.setCrcycd(input.getCrcycd());
		knsStrk1.setCsextg(input.getCsextg());
		knsStrk1.setRebuwo(input.getRebuwo());
		knsStrk1.setAmntcd(input.getAmntcd());
		knsStrk1.setHappbl(input.getHappbl());
		if(CommUtil.isNull(input.getCharlg())){
			knsStrk1.setCharlg(E_PAYATP._0);
		}else{
			knsStrk1.setCharlg(input.getCharlg());
		}
		
		knsStrk1.setTrandt(trandt);
		knsStrk1.setTransq(transq);
		knsStrk1.setTranus(tranus);
		knsStrk1.setTrantp(trantp);
		knsStrk1.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq());//柜员流水 //getUssqno() to getTransq() rambo
		knsStrk1.setUssrdt(CommTools.getBaseRunEnvs().getTrxn_date());//柜员日期
		
		if(input.getOperwy() == E_OPERWY.INS){
			KnsStrkDao.insert(knsStrk1);
		}else{
			KnsStrkDao.updateOne_kns_strk_odx1(knsStrk1);
		}
		
		bizlog.debug("账号1处理成功==========");
		
		KnsStrk knsStrk2 = SysUtil.getInstance(KnsStrk.class);
		if(input.getOperwy() == E_OPERWY.INS){
			knsStrk2.setNumbsq(numbsq);
			knsStrk2.setSerlno(1);
		}else if(input.getOperwy() == E_OPERWY.UPD){
			knsStrk2 = KnsStrkDao.selectOne_kns_strk_odx1(input.getNumbsq(), 1, trandt, true);
			if(knsStrk1.getStrkst() != E_STRKST.WFH){
				throw InError.comm.E0003("当前状态不允许修改操作");
			}
		}
		
		//账号2处理
		knsStrk2.setPrtrdt(input.getPrtrdt());
		knsStrk2.setPrtrsq(input.getPrtrsq());//如果流水为空，则给none
		knsStrk2.setWronbr(input.getWronbr());
		knsStrk2.setWronte(input.getWronte());
		knsStrk2.setReason(input.getReason());
		knsStrk2.setStrkst(E_STRKST.WFH);
		
		knsStrk2.setCustac(input.getCustaa());
		knsStrk2.setAcctna(input.getAcctme());
		knsStrk2.setCrcycd(input.getCrcyce());
		knsStrk2.setCsextg(input.getCsexte());
		knsStrk2.setRebuwo(input.getRebuwa());
		knsStrk2.setAmntcd(input.getAmntca());
		knsStrk2.setHappbl(input.getHappba());
		if(CommUtil.isNull(input.getCharla())){
			knsStrk2.setCharlg(E_PAYATP._0);
		}else{
			knsStrk2.setCharlg(input.getCharla());
		}
		
		knsStrk2.setTrandt(trandt);
		knsStrk2.setTransq(transq);
		knsStrk2.setTranus(tranus);
		knsStrk2.setTrantp(trantp);
		knsStrk2.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq());//柜员流水//getUssqno() to getTransq() rambo
		knsStrk2.setUssrdt(CommTools.getBaseRunEnvs().getTrxn_date());//柜员日期
		if(input.getOperwy() == E_OPERWY.INS){
			KnsStrkDao.insert(knsStrk2);
		}else{
			KnsStrkDao.updateOne_kns_strk_odx1(knsStrk2);
		}
		
		//挂账明细1处理
		if(knsStrk1.getCharlg() == E_PAYATP._1){
			
			if(input.getOperwy() == E_OPERWY.UPD){
				
				//把原挂账记录作废
				WrAccRbDao.updateKnsPayaToDelete(knsStrk1.getNumbsq(),knsStrk1.getCustac(), trandt,timetm);
								
			}
			
			if(CommUtil.isNull(input.getPayaListInfoFirst())||input.getPayaListInfoFirst().size()==0){
				
				throw InError.comm.E0003("账号["+knsStrk1.getCustac()+"]挂账，挂账明细不能为空！");
			}
			for (PayaListInfo payaListInfo : input.getPayaListInfoFirst()) {
				boolean updateFlag = false;
				KnsPaya knsPaya = SysUtil.getInstance(KnsPaya.class);
				String payasq = MsSeqUtil.genSeq("PAYASQ", CommTools.getBaseRunEnvs().getTrxn_date());
				if(input.getOperwy() == E_OPERWY.INS){
					
					
					knsPaya.setPayasq(payasq);
				}
				if(input.getOperwy() == E_OPERWY.UPD){
					if(CommUtil.isNotNull(payaListInfo.getPayasq())){
						knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(payaListInfo.getPayasq(), true);
						updateFlag =true;
					}else{
						
						knsPaya.setPayasq(payasq);
					}
				}
				
				knsPaya.setAcstno(knsStrk1.getNumbsq());
				knsPaya.setPayseq(1);
				knsPaya.setToacct(payaListInfo.getToacno());
				knsPaya.setToacna(payaListInfo.getToacna());
				knsPaya.setPayabr(payaListInfo.getPayabr());
				knsPaya.setPayamn(payaListInfo.getPayamn());
				knsPaya.setRsdlmn(payaListInfo.getPayamn());
				knsPaya.setPayaac(input.getCustac());
				knsPaya.setPayabr(payaListInfo.getPayabr());
				knsPaya.setPayast(E_PAYAST.WFH);
				knsPaya.setCrcycd(input.getCrcycd());
				knsPaya.setTrandt(trandt);
				knsPaya.setTransq(transq);
				knsPaya.setTranus(tranus);
				knsPaya.setTemp01(input.getReason());
				if(updateFlag){
					KnsPayaDao.updateOne_kns_paya_odx1(knsPaya);
				}else{
					KnsPayaDao.insert(knsPaya);
				}
			}
		}
		
		//销账明细一处理
		if(knsStrk1.getCharlg() == E_PAYATP._2){
			boolean updateFlag = false;
			if(input.getOperwy() == E_OPERWY.UPD){
				
				//把原销账记录作废
				WrAccRbDao.updateKnsPaydToDelete(knsStrk1.getNumbsq(),knsStrk1.getCustac(), trandt,timetm);
								
			}
			
			if(CommUtil.isNull(input.getPaydListInfoFirst())||input.getPaydListInfoFirst().size()==0){
				
				throw InError.comm.E0003("账号["+knsStrk1.getCustac()+"]销账，销账明细不能为空！");
			}
			
			for (PaydListInfo paydListInfo : input.getPaydListInfoFirst()) {
				KnsPayd knsPayd = SysUtil.getInstance(KnsPayd.class);
				String paydsq = MsSeqUtil.genSeq("PAYDSQ", CommTools.getBaseRunEnvs().getTrxn_date());
				if(input.getOperwy() == E_OPERWY.INS){
					knsPayd.setPaydsq(paydsq);
				}
				if(input.getOperwy() == E_OPERWY.UPD){					
					if(CommUtil.isNotNull(paydListInfo.getPaydsq())){
						knsPayd = KnsPaydDao.selectOne_kns_payd_odx1(paydListInfo.getPaydsq(), true);
						updateFlag =true;
					}else{
						knsPayd.setPaydsq(paydsq);
					}
				}
				
				KnsPaya knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(paydListInfo.getCharsq(), true);
				if(CommUtil.compare(paydListInfo.getCharam(), knsPaya.getRsdlmn())>0){
					throw InError.comm.E0003("销账金额不能大于剩余未销金额！");
				}
				if(!CommUtil.equals(knsPaya.getPayaac(), input.getCustac())){
					
					throw InError.comm.E0003("销账账号和挂账序号不匹配！");
				}
				
				knsPayd.setAcstno(input.getNumbsq());
				knsPayd.setPayseq(1);
				knsPayd.setPayamn(paydListInfo.getCharam());
				knsPayd.setPayasq(paydListInfo.getCharsq());
				knsPayd.setPaydac(input.getCustac());
				knsPayd.setPayabr(knsPaya.getPayabr()); //挂账机构
				knsPayd.setPaydbr(CommTools.getBaseRunEnvs().getTrxn_branch()); //销账机构
				knsPayd.setToacct(input.getCustaa());//账号2
				knsPayd.setToacna(input.getAcctme());//账号2名称
				knsPayd.setRsdlmn(knsPaya.getRsdlmn().subtract(paydListInfo.getCharam()));
				knsPayd.setTotlmn(knsPaya.getRsdlmn());
				knsPayd.setPaydst(E_PAYDST.WFH);
				knsPayd.setTrandt(trandt);
				knsPayd.setTransq(transq);
				knsPayd.setUntius(tranus);
				knsPayd.setTemp01(input.getReason());
				if(updateFlag){
					KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);
				}else{
					KnsPaydDao.insert(knsPayd);
				}
			}
		}
		
		//挂账明细2处理
		if(knsStrk2.getCharlg() == E_PAYATP._1){
			
			if(input.getOperwy() == E_OPERWY.UPD){
				
				//把原挂账记录作废
				WrAccRbDao.updateKnsPayaToDelete(knsStrk2.getNumbsq(),knsStrk2.getCustac(), trandt,timetm);
								
			}			
			
			if(CommUtil.isNull(input.getPayaListInfoSecond())||input.getPayaListInfoSecond().size()==0){
				
				throw InError.comm.E0003("账号["+knsStrk2.getCustac()+"]挂账，挂账明细不能为空！");
			}
					
			
			for (PayaListSecond payaListSecond : input.getPayaListInfoSecond()) {
				boolean updateFlag = false;
				KnsPaya knsPaya = SysUtil.getInstance(KnsPaya.class);
				String payasq = MsSeqUtil.genSeq("PAYASQ", CommTools.getBaseRunEnvs().getTrxn_date());
				if(input.getOperwy() == E_OPERWY.INS){
					knsPaya.setPayasq(payasq);
				}
				if(input.getOperwy() == E_OPERWY.UPD){
					if(CommUtil.isNotNull(payaListSecond.getPayase())){
						knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(payaListSecond.getPayase(), true);
						updateFlag =true;
					}else{
						knsPaya.setPayasq(payasq);
					}
				}
				
				knsPaya.setAcstno(knsStrk2.getNumbsq());
				knsPaya.setPayseq(1);
				knsPaya.setToacct(payaListSecond.getToacnb());
				knsPaya.setToacna(payaListSecond.getToacnm());
				knsPaya.setPayabr(payaListSecond.getPayabh());
				knsPaya.setPayamn(payaListSecond.getPayamt());
				knsPaya.setRsdlmn(payaListSecond.getPayamt());
				knsPaya.setPayaac(input.getCustaa());
				knsPaya.setPayast(E_PAYAST.WFH);
				knsPaya.setCrcycd(input.getCrcycd());
				knsPaya.setTrandt(trandt);
				knsPaya.setTransq(transq);
				knsPaya.setTranus(tranus);
				knsPaya.setTemp01(input.getReason());
				if(updateFlag){
					KnsPayaDao.updateOne_kns_paya_odx1(knsPaya);
				}else{
					KnsPayaDao.insert(knsPaya);
				}
			}
		}
		
		//销账明细2处理
		if(knsStrk2.getCharlg() == E_PAYATP._2){
			
			if(input.getOperwy() == E_OPERWY.UPD){
				
				//把原销账记录作废
				WrAccRbDao.updateKnsPaydToDelete(knsStrk2.getNumbsq(),knsStrk2.getCustac(), trandt,timetm);
								
			}
			
			if(CommUtil.isNull(input.getPaydListInfoSecond())||input.getPaydListInfoSecond().size()==0){
				
				throw InError.comm.E0003("账号["+knsStrk2.getCustac()+"]销账，销账明细不能为空！");
			}
							
			for (PaydListSecond paydListSecond : input.getPaydListInfoSecond()) {
				boolean updateFlag = false;
				String paydsq = MsSeqUtil.genSeq("PAYDSQ", CommTools.getBaseRunEnvs().getTrxn_date());
				KnsPayd knsPayd = SysUtil.getInstance(KnsPayd.class);
				if(input.getOperwy() == E_OPERWY.INS){
					knsPayd.setPaydsq(paydsq);
				}
				if(input.getOperwy() == E_OPERWY.UPD){
					if(CommUtil.isNotNull(paydListSecond.getPaydse())){
						knsPayd = KnsPaydDao.selectOne_kns_payd_odx1(paydListSecond.getPaydse(), true);
						updateFlag =true;
					}else{
						knsPayd.setPaydsq(paydsq);
					}
				}
				
				KnsPaya knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(paydListSecond.getCharse(), true);
				
				if(CommUtil.compare(paydListSecond.getCharan(), knsPaya.getRsdlmn())>0){
					throw InError.comm.E0003("销账金额不能大于剩余未销金额！");
				}
				if(!CommUtil.equals(knsPaya.getPayaac(), input.getCustaa())){
					
					throw InError.comm.E0003("销账账号和挂账序号不匹配！");
				}

				knsPayd.setAcstno(input.getNumbsq());
				knsPayd.setPayseq(1);
				knsPayd.setPayamn(paydListSecond.getCharan());
				knsPayd.setPayasq(paydListSecond.getCharse());
				knsPayd.setPaydac(input.getCustaa());
				knsPayd.setPayabr(knsPaya.getPayabr()); //挂账机构
				knsPayd.setPaydbr(CommTools.getBaseRunEnvs().getTrxn_branch()); //销账机构
				knsPayd.setToacct(input.getCustac());//账号1
				knsPayd.setToacna(input.getAcctna());//账号1名称
				knsPayd.setRsdlmn(knsPaya.getRsdlmn().subtract(paydListSecond.getCharan()));
				knsPayd.setTotlmn(knsPaya.getRsdlmn());
				knsPayd.setPaydst(E_PAYDST.WFH);
				knsPayd.setTrandt(trandt);
				knsPayd.setTransq(transq);
				knsPayd.setUntius(tranus);
				knsPayd.setTemp01(input.getReason());
				if(updateFlag){
					KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);
				}else{
					KnsPaydDao.insert(knsPayd);
				}
			}
		}
		output.setAcstno(numbsq);//套号
		
		dealStrkData(trandt); //调用垃圾数据清除操作
	}

	/*
	 * 隔日错账冲正复核
	 * @see cn.sunline.ltts.busi.in.servicetype.WroAccRollBack#chkNestck(cn.sunline.ltts.busi.in.servicetype.WroAccRollBack.ChkNestck.Input)
	 */
	@Override
	public void chkNestck(
			cn.sunline.ltts.busi.in.servicetype.WroAccRollBack.ChkNestck.Input input) {
		
		String chckus = CommTools.getBaseRunEnvs().getTrxn_teller();
		String chckdt = CommTools.getBaseRunEnvs().getTrxn_date();
		String chckbr = CommTools.getBaseRunEnvs().getTrxn_branch();
		List<KnsStrk> knsStrkList = WrAccRbDao.selKnsStrkByNumbsq(input.getNumbsq(), chckdt, false);//改为套号加日期查询
		//List<KnsStrk> knsStrkList = KnsStrkDao.selectAll_kns_strk_odx2(input.getPrtrdt(), input.getPrtrsq(), input.getWronbr(), false);
		//表外记账复核
		if(input.getTrantp()==E_STTRTP.O){
			//按套号日期查询错账登记簿
			KnsStrk tblknsStrk = KnsStrkDao.selectFirst_kns_strk_odx4(input.getNumbsq(), chckdt, false);
			//表外记账复核方法
			updateOutTable(input,tblknsStrk);
			
			return;
		}
		//如果为转账，则账号2，红蓝字2，借贷标识2必输
		if(input.getTrantp()==E_STTRTP.I){
			if(CommUtil.isNull(input.getCustaa())||CommUtil.isNull(input.getRebuwa())
					||CommUtil.isNull(input.getAmntca())||CommUtil.isNull(input.getHappba())
					||CommUtil.isNull(input.getCrcyce())){
				throw InError.comm.E0003("账号2，红蓝字2，借贷标识2，发生额2为必输");
			}
		}
		if(knsStrkList.size() != 2){
			throw InError.comm.E0003("该记录数据有误，请核查！");
		}
		
		KnsStrk knsStrk1 = knsStrkList.get(0);
		KnsStrk knsStrk2 = knsStrkList.get(1);

		
		if(!CommUtil.equals(chckbr, knsStrk1.getWronbr())){
			throw InError.comm.E0003("非本机构错账记录，不能复核！");
		}
		
		if(CommUtil.equals(chckus, knsStrk1.getTranus())){
			throw InError.comm.E0003("请换人复核！");
		}
		
		E_OPERWZ operwy =  input.getOperwy();
		if(operwy == null){
			throw InError.comm.E0003("操作类型不能为空！");
		}
		//红蓝字校验 delete by xionglz 去除红蓝字记账
//		if(input.getRebuwo() != input.getRebuwa()){
//			throw InError.comm.E0003("同一笔记录，红蓝字应相同");
//		}
//		String prtrYear = input.getPrtrdt().substring(0, 4);
//		String tranYear = chckdt.substring(0, 4);
//		if(input.getRebuwo() == E_REBUWA.B){
//			if(CommUtil.equals(prtrYear, tranYear)){
//				throw InError.comm.E0003("原交易日期在本年，应为红字记账");
//			}
//		}
//		if(input.getRebuwo() == E_REBUWA.R){
//			if(!CommUtil.equals(prtrYear, tranYear)){
//				throw InError.comm.E0003("原交易日期不是本年，应为蓝字记账");
//			}
//		}
		
		//操作方式为取消复核时，进行取消复核处理
		if(operwy==E_OPERWZ.QXFH){
			if(knsStrk1.getStrkst() != E_STRKST.YFH){
				throw InError.comm.E0003("此记录不处于复核状态，无法进行本操作");
			}
			if(!CommUtil.equals(knsStrk1.getChckus(), CommTools.getBaseRunEnvs().getTrxn_teller())){
				
				throw InError.comm.E0003("取消复核柜员必须和原复核柜员"+knsStrk1.getChckus()+"相同！");
			}
			
			knsStrk1.setStrkst(E_STRKST.WFH);
			KnsStrkDao.updateOne_kns_strk_odx1(knsStrk1);
			knsStrk2.setStrkst(E_STRKST.WFH);
			KnsStrkDao.updateOne_kns_strk_odx1(knsStrk2);
			return;
		}else if(knsStrk1.getStrkst() != E_STRKST.WFH){
				throw InError.comm.E0003("套号状态["+knsStrk1.getStrkst().getLongName()+"]不能复核，请核对！");
			}
		
		//默认为复核处理
		if(!CommUtil.equals(knsStrk1.getCustac(), input.getCustac()) || !CommUtil.equals(knsStrk2.getCustac(), input.getCustaa())){
			throw InError.comm.E0003("账号与录入账号不符，请核实！");
		}
		if(knsStrk1.getAmntcd() != input.getAmntcd() || knsStrk2.getAmntcd() != input.getAmntca()){
			throw InError.comm.E0003("借贷标志与录入信息不符，请核实！");
		}
		if(!CommUtil.equals(input.getHappbl(), knsStrk1.getHappbl()) ||!CommUtil.equals(input.getHappba(), knsStrk2.getHappbl()) ){
			throw InError.comm.E0003("交易金额不符请核对，请核实！");
		}
		if(input.getRebuwo()!= knsStrk1.getRebuwo() ||input.getRebuwa()!= knsStrk2.getRebuwo()){
			throw InError.comm.E0003("红蓝字不符请核对，请核实！");
		}
		if(input.getCharlg()!= knsStrk1.getCharlg() ||input.getCharla()!= knsStrk2.getCharlg()){
			throw InError.comm.E0003("挂销账标志不符请核对，请核实！");
		}
		
		knsStrk1.setReason(input.getReason());
		knsStrk1.setChckus(chckus);
		knsStrk1.setStrkst(E_STRKST.YFH);
		KnsStrkDao.updateOne_kns_strk_odx1(knsStrk1);
		
		knsStrk2.setReason(input.getReason());
		knsStrk2.setChckus(chckus);
		knsStrk2.setStrkst(E_STRKST.YFH);
		KnsStrkDao.updateOne_kns_strk_odx1(knsStrk2);
		
		//挂账明细1处理
		if(knsStrk1.getCharlg() == E_PAYATP._1){
			int count = 0;
			if(CommUtil.isNull(input.getPayaListInfoFirst()) || input.getPayaListInfoFirst().size() != 1){
				throw InError.comm.E0003("挂账必须输入挂账信息或挂账记录不止一条");
			}
			for (PayaListInfo payaListInfo : input.getPayaListInfoFirst()) {
				count = count + 1;
				KnsPaya knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(payaListInfo.getPayasq(), true);
				if(CommUtil.isNull(knsPaya)){
					throw InError.comm.E0003("挂账序号["+payaListInfo.getPayasq()+"]未找到对应的挂账记录");
				}
				if(!CommUtil.equals(knsPaya.getToacct(), payaListInfo.getToacno())){
					throw InError.comm.E0003("挂账明细1第" + count +"条记录与录入信息不符，请核实");
				}
				if(!CommUtil.equals(knsPaya.getToacna(), payaListInfo.getToacna())){
					throw InError.comm.E0003("挂账明细1第" + count +"条记录与录入信息不符，请核实");
				}
				if(!CommUtil.equals(knsPaya.getPayamn(), payaListInfo.getPayamn())){
					throw InError.comm.E0003("挂账明细1第" + count +"条记录与录入信息不符，请核实");
				}
				if(!CommUtil.equals(knsPaya.getPayabr(), payaListInfo.getPayabr())){
					throw InError.comm.E0003("挂账明细1第" + count +"条记录与录入信息不符，请核实");
				}
				
				knsPaya.setPayast(E_PAYAST.YFH);
				KnsPayaDao.updateOne_kns_paya_odx1(knsPaya);
			}
		}
		
		//销账明细一处理
		if(knsStrk1.getCharlg() == E_PAYATP._2){
			int count = 0;
			if(CommUtil.isNull(input.getPaydListInfoFirst()) || input.getPaydListInfoFirst().size() != 1){
				throw InError.comm.E0003("销账必须输入销账信息或销账记录不止一条");
			}
			for (PaydListInfo paydListInfo : input.getPaydListInfoFirst()) {
				count = count + 1;
				KnsPayd knsPayd = KnsPaydDao.selectOne_kns_payd_odx1(paydListInfo.getPaydsq(), false);
				if(CommUtil.isNull(knsPayd)){
					throw InError.comm.E0003("销账序号["+paydListInfo.getPaydsq()+"]未找到对应的销账记录");
				}
				if (CommUtil.isNull(knsPayd)) {
					throw InError.comm.E0003("销账明细1第" + count +"条记录与录入信息不符，请核实");
				}
				if(!CommUtil.equals(knsPayd.getPayasq(), paydListInfo.getCharsq())){
					throw InError.comm.E0003("销账明细1第" + count +"条记录与录入信息不符，请核实");
				}
				if(!CommUtil.equals(knsPayd.getTotlmn(), paydListInfo.getBalanc())){
					throw InError.comm.E0003("销账明细1第" + count +"条记录与录入信息不符，请核实");
				}
				if(!CommUtil.equals(knsPayd.getPayamn(), paydListInfo.getCharam())){
					throw InError.comm.E0003("销账明细1第" + count +"条记录与录入信息不符，请核实");
				}
				
				knsPayd.setPaydst(E_PAYDST.YFH);
				KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);
			}
		}
		
		//挂账明细2处理
		if(knsStrk2.getCharlg() == E_PAYATP._1){
			int count = 0;
			if(CommUtil.isNull(input.getPayaListInfoSecond()) || input.getPayaListInfoSecond().size() != 1){
				throw InError.comm.E0003("挂账必须输入挂账信息或挂账记录不止一条");
			}
			for (PayaListSecond payaListSecond : input.getPayaListInfoSecond()) {
				count = count + 1;
				KnsPaya knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(payaListSecond.getPayase(), true);
				if(CommUtil.isNull(knsPaya)){
					throw InError.comm.E0003("挂账序号["+payaListSecond.getPayase()+"]未找到对应的挂账记录");
				}
				if(!CommUtil.equals(knsPaya.getToacct(), payaListSecond.getToacnb())){
					throw InError.comm.E0003("挂账明细2第" + count +"条记录与录入信息不符，请核实");
				}
				if(!CommUtil.equals(knsPaya.getToacna(), payaListSecond.getToacnm())){
					throw InError.comm.E0003("挂账明细2第" + count +"条记录与录入信息不符，请核实");
				}
				if(!CommUtil.equals(knsPaya.getPayamn(), payaListSecond.getPayamt())){
					throw InError.comm.E0003("挂账明细2第" + count +"条记录与录入信息不符，请核实");
				}
				if(!CommUtil.equals(knsPaya.getPayabr(), payaListSecond.getPayabh())){
					throw InError.comm.E0003("挂账明细2第" + count +"条记录与录入信息不符，请核实");
				}
				
				knsPaya.setPayast(E_PAYAST.YFH);
				KnsPayaDao.updateOne_kns_paya_odx1(knsPaya);
			}
		}
		
		//销账明细2处理
		if(knsStrk2.getCharlg() == E_PAYATP._2){
			int count = 0;
			if(CommUtil.isNull(input.getPaydListInfoSecond()) || input.getPaydListInfoSecond().size() != 1){
				throw InError.comm.E0003("销账必须输入销账信息或销账记录不止一条");
			}
			for (PaydListSecond paydListSecond : input.getPaydListInfoSecond()) {
				count = count + 1;
				KnsPayd knsPayd = KnsPaydDao.selectOne_kns_payd_odx1(paydListSecond.getPaydse(), true);
				if(CommUtil.isNull(knsPayd)){
					throw InError.comm.E0003("销账序号["+paydListSecond.getPaydse()+"]未找到对应的销账记录");
				}
				if(!CommUtil.equals(knsPayd.getPayasq(), paydListSecond.getCharse())){
					throw InError.comm.E0003("销账明细2第" + count +"条记录与录入信息不符，请核实");
				}
				if(!CommUtil.equals(knsPayd.getTotlmn(), paydListSecond.getBalane())){
					throw InError.comm.E0003(knsPayd.getRsdlmn()+"-"+paydListSecond.getBalane()+"销账明细2第" + count +"条记录与录入信息不符，请核实");
				}
				if(!CommUtil.equals(knsPayd.getPayamn(), paydListSecond.getCharan())){
					throw InError.comm.E0003(knsPayd.getPayamn()+"-"+paydListSecond.getCharan()+"销账明细2第" + count +"条记录与录入信息不符，请核实");
				}
				
				knsPayd.setPaydst(E_PAYDST.YFH);
				KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);
			}
		}
	}

	/*
	 * (非 Javadoc) 
	 * <p>Title: accNestcm</p> 
	 * <p>Description: </p> 
	 * @param input 
	 * notes:隔日冲正，不需要更改原记录状态
	 */
	@Override
	public void accNestcm(
			cn.sunline.ltts.busi.in.servicetype.WroAccRollBack.AccNestcm.Input input,cn.sunline.ltts.busi.in.servicetype.WroAccRollBack.AccNestcm.Output output) {
		
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
		BigDecimal  acmlbl1 = BigDecimal.ZERO;//积数
		BigDecimal  acmlbl2 = BigDecimal.ZERO;//积数
		int ibdays=DateTools2.calDays(input.getPrtrdt(), trandt, 0, 0);//天数差
		
		bizlog.debug("===============隔日冲正调整天数："+ibdays);
		
		List<KnsStrk> knsStrkList = WrAccRbDao.selKnsStrkByNumbsq(input.getNumbsq(), trandt, false);//改为套号加日期查询
		
		//表外记账入账处理
		if(knsStrkList.get(0).getIoflag()==E_STTRTP.O){
			//表外入账处理
			checInOutTable(input,knsStrkList.get(0));
			return;
		}
		if(knsStrkList.size() != 2){
			throw InError.comm.E0003("该记录数据有误，请核查！");
		}
		
		KnsStrk knsStrk1 = knsStrkList.get(0);
		KnsStrk knsStrk2 = knsStrkList.get(1);
		
		if(knsStrk1.getStrkst() == E_STRKST.ZF){
			throw InError.comm.E0003("套号已作废，请核对！");
		}
		
		if(knsStrk1.getStrkst() != E_STRKST.YFH){
			throw InError.comm.E0003("套号不处于已复核状态，不能进行入账操作！");
		}
		
		if(!CommUtil.equals(tranbr, knsStrk1.getWronbr())){
			throw InError.comm.E0003("非本机构错账记录，不能复核！");
		}
		
		if(!CommUtil.equals(tranus, knsStrk1.getChckus())){
			throw InError.comm.E0003("入账柜员必须与复核柜员相同！");
		}
		
		//账号1冲正入账处理
		//是否内部账
		if (ApAcctRoutTools.getRouteType(knsStrk1.getCustac()) == E_ACCTROUTTYPE.INSIDE){
			
			IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
			iaAcdrInfo.setTrantp(E_TRANTP.TR);
			iaAcdrInfo.setAcctno(knsStrk1.getCustac());
			iaAcdrInfo.setTranam(knsStrk1.getHappbl());//记账金额
			iaAcdrInfo.setAcuttp(E_ACUTTP._3);//记账类型蓝字
			iaAcdrInfo.setDscrtx(knsStrk2.getReason());
			iaAcdrInfo.setSmrycd(BusinessConstants.SUMMARY_CZ);
			//红字，记负数
			if(knsStrk1.getRebuwo() ==E_REBUWA.R){
				iaAcdrInfo.setTranam(knsStrk1.getHappbl().negate());//记账金额
				iaAcdrInfo.setAcuttp(E_ACUTTP._2);//记账类型红字
			}
			iaAcdrInfo.setCrcycd(knsStrk1.getCrcycd());//币种
			iaAcdrInfo.setAcbrch(knsStrk1.getWronbr());//账户机构
			
			//挂账处理
			if(knsStrk1.getCharlg() == E_PAYATP._1){
				Options<IaPayaDetail> iaPayaDetailOption = new DefaultOptions<IaPayaDetail>();
				List<KnsPaya> knsPayaList = new ArrayList<KnsPaya>();
				knsPayaList = KnsPayaDao.selectAll_kns_paya_odx3(knsStrk1.getNumbsq(), knsStrk1.getCustac(), trandt, true);
				for(KnsPaya knsPaya : knsPayaList){
					if(knsPaya.getPayast() ==E_PAYAST.ZF){
						continue;
					}
					IaPayaDetail iaPayaDetail = SysUtil.getInstance(IaPayaDetail.class);
					iaPayaDetail.setPayaac(knsPaya.getPayaac());
					iaPayaDetail.setPayabr(knsPaya.getPayabr());
					iaPayaDetail.setPayamn(knsPaya.getPayamn());
					iaPayaDetail.setPayasq(knsPaya.getPayasq());
					iaPayaDetail.setToacna(knsPaya.getToacna());
					iaPayaDetail.setToacno(knsPaya.getToacct());
					iaPayaDetailOption.add(iaPayaDetail);
				}
				iaAcdrInfo.setPayadetail(iaPayaDetailOption);
			}
			
			//销账处理
			if(knsStrk1.getCharlg() == E_PAYATP._2){
				
				Options<IaPaydDetail> iaPaydDetailOption = new DefaultOptions<IaPaydDetail>();
				List<KnsPayd> knsPaydList = new ArrayList<KnsPayd>();
				knsPaydList = KnsPaydDao.selectAll_kns_payd_odx3(knsStrk1.getNumbsq(), knsStrk1.getCustac(), trandt, true);
				
				for(KnsPayd knsPayd : knsPaydList){
					if(knsPayd.getPaydst() ==E_PAYDST.ZF){
						continue;
					}
					IaPaydDetail iaPaydDetail = SysUtil.getInstance(IaPaydDetail.class);
					iaPaydDetail.setPaydmn(knsPayd.getPayamn());
					iaPaydDetail.setPrpysq(knsPayd.getPayasq());
					iaPaydDetail.setPaydac(knsPayd.getPaydac());
					iaPaydDetail.setRsdlmn(knsPayd.getRsdlmn());
					iaPaydDetail.setTotlmn(knsPayd.getTotlmn());
					iaPaydDetail.setPaydsq(knsPayd.getPaydsq());
					iaPaydDetailOption.add(iaPaydDetail);
				}
				iaAcdrInfo.setPayddetail(iaPaydDetailOption);
			}
			
			//记账方向
			E_AMNTCD amntcd = knsStrk1.getAmntcd();		
			//调用内部户记账服务
			IoInAccount ioInAcctount = SysUtil.getInstance(IoInAccount.class);

			switch (amntcd){
				case DR:
					ioInAcctount.ioInAcdr(iaAcdrInfo);//内部户借方服务
					break;
				case CR:
					ioInAcctount.ioInAccr(iaAcdrInfo);//内部户贷方服务
					break;
				default:
					throw InError.comm.E0003("记账方向:"+amntcd.getValue()+"["+amntcd.getLongName()+"]不支持");
			}
			
		}else{
			//客户账入账
			IoCaKnaAcdc tblKnaAcdc = CommTools.getRemoteInstance(IoCaSevQryTableInfo.class).getKnaAcdcByCardno(knsStrk1.getCustac(), true);

			
			IoDpKnaAcct ioDpKnaAcct= CommTools.getRemoteInstance(DpAcctSvcType.class).getSettKnaAcctAc(tblKnaAcdc.getCustac());
			E_AMNTCD amntcd = knsStrk1.getAmntcd();
			BigDecimal tranam = BigDecimal.ZERO;
			if(knsStrk1.getRebuwo() ==E_REBUWA.B){
				tranam = knsStrk1.getHappbl();
			}else if (knsStrk1.getRebuwo() ==E_REBUWA.R){
				tranam = knsStrk1.getHappbl().negate();
			}
			
			output.setOtacno(tblKnaAcdc.getCustac());
			output.setTranam(tranam);
			output.setAmntcd1(amntcd);
			
//			String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//			CommTools.getBaseRunEnvs().setBusi_org_id(ioDpKnaAcct.getCorpno());
			
			//add 20160206 songlw 参数控制 是否利息和积数同时调整
			if(CommUtil.equals("Y", KnpParameterDao.selectOne_odb1("nestcm_isInat", "%", "%", "%", true).getParm_value1())){
				//处理计算调整利息，并记账,返回利息调整对应天数
				int lcDays = prcAdjustInst(ioDpKnaAcct, knsStrk1, tranam, input.getPrtrdt(), amntcd);
				
				//积数调整天数重新计算，减去利息调整的天数
				ibdays = ibdays - lcDays;
			
				bizlog.debug("===============调整利息计算天数："+lcDays);
				bizlog.debug("===============调整利息后调整积数计算天数："+ibdays);
			}	
			
			if((E_AMNTCD.DR==amntcd&&knsStrk1.getRebuwo() ==E_REBUWA.B)||(E_AMNTCD.CR==amntcd&&knsStrk1.getRebuwo() ==E_REBUWA.R)){
				
				CommTools.getRemoteInstance(IoAccountSvcType.class).checkStatusBeforeAccount(tblKnaAcdc.getCustac(), E_AMNTCD.DR,E_YES___.YES, E_YES___.NO);
			}else{
				
				CommTools.getRemoteInstance(IoAccountSvcType.class).checkStatusBeforeAccount(tblKnaAcdc.getCustac(), E_AMNTCD.CR,E_YES___.YES, E_YES___.NO);
			}
			
	
			//客户账记账输入类字段赋值
			if(E_AMNTCD.CR==amntcd){
				//红字，调存入服务，记负数
				SaveDpAcctIn saveDpAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
				saveDpAcctIn.setAcctno(ioDpKnaAcct.getAcctno());
				saveDpAcctIn.setCustac(ioDpKnaAcct.getCustac());
				saveDpAcctIn.setCardno(knsStrk1.getCustac());
				saveDpAcctIn.setOpacna(knsStrk2.getAcctna());
				saveDpAcctIn.setToacct(knsStrk2.getCustac());
				saveDpAcctIn.setCrcycd(knsStrk1.getCrcycd());
				saveDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_CZ);
				saveDpAcctIn.setRemark(knsStrk1.getReason());
				saveDpAcctIn.setTranam(tranam);
				saveDpAcctIn.setNegafg(E_YES___.YES);//支持红字记账
				CommTools.getRemoteInstance(DpAcctSvcType.class).addPostAcctDp(saveDpAcctIn);
			} else {
				//蓝字，调支取服务，记正数
				DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
				drawDpAcctIn.setAcctno(ioDpKnaAcct.getAcctno());
				drawDpAcctIn.setCustac(ioDpKnaAcct.getCustac());
				drawDpAcctIn.setCardno(knsStrk1.getCustac());
				drawDpAcctIn.setOpacna(knsStrk2.getAcctna());
				drawDpAcctIn.setToacct(knsStrk2.getCustac());
				drawDpAcctIn.setCrcycd(knsStrk1.getCrcycd());
				drawDpAcctIn.setIschck(E_YES___.NO);
				drawDpAcctIn.setTranam(tranam);
				drawDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_CZ);
				drawDpAcctIn.setRemark(knsStrk1.getReason());
				CommTools.getRemoteInstance(DpAcctSvcType.class).addDrawAcctDp(drawDpAcctIn);
			}
			
			//积数调整

			acmlbl1 = knsStrk1.getHappbl().multiply(new BigDecimal(ibdays));//积数
			
			IoDpKnbAcin cplAcin1 = CommTools.getRemoteInstance(IoDpSrvQryTableInfo.class).getKnbAcinOdb1(ioDpKnaAcct.getAcctno(), true);
			if((E_AMNTCD.CR==amntcd&&CommUtil.compare(tranam, BigDecimal.ZERO)>0)||E_AMNTCD.DR==amntcd&&CommUtil.compare(tranam, BigDecimal.ZERO)<0){
				
				cplAcin1.setCutmam(cplAcin1.getCutmam().add(acmlbl1));//调增
			}else{
				cplAcin1.setCutmam(cplAcin1.getCutmam().subtract(acmlbl1));//调减
			}
			//更新积数
			CommTools.getRemoteInstance(IoDpSrvQryTableInfo.class).updateKnbAcinOdb1(cplAcin1);
			
//			CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
		}
		
		//账号2处理
		//是否内部账
		if (ApAcctRoutTools.getRouteType(knsStrk2.getCustac()) == E_ACCTROUTTYPE.INSIDE){
			
			IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
			iaAcdrInfo.setTrantp(E_TRANTP.TR);
			iaAcdrInfo.setAcctno(knsStrk2.getCustac());
			iaAcdrInfo.setTranam(knsStrk2.getHappbl());//记账金额
			iaAcdrInfo.setAcuttp(E_ACUTTP._3);//记账类型蓝字
			iaAcdrInfo.setDscrtx(knsStrk2.getReason());
			iaAcdrInfo.setSmrycd(BusinessConstants.SUMMARY_CZ);
			//如果是红字，记负数
			if(knsStrk2.getRebuwo() == E_REBUWA.R){
				iaAcdrInfo.setTranam(knsStrk2.getHappbl().negate());//
				iaAcdrInfo.setAcuttp(E_ACUTTP._2);//记账类型红字
			}
			iaAcdrInfo.setCrcycd(knsStrk2.getCrcycd());//币种
			iaAcdrInfo.setAcbrch(knsStrk2.getWronbr());//账户机构
			
			//挂账处理
			if(knsStrk2.getCharlg() == E_PAYATP._1){
				Options<IaPayaDetail> iaPayaDetailOption = new DefaultOptions<IaPayaDetail>();
				List<KnsPaya> knsPayaList = new ArrayList<KnsPaya>();
				knsPayaList = KnsPayaDao.selectAll_kns_paya_odx3(knsStrk2.getNumbsq(), knsStrk2.getCustac(), trandt, true);
				for(KnsPaya knsPaya : knsPayaList){
					if(knsPaya.getPayast() ==E_PAYAST.ZF){
						continue;
					}
					IaPayaDetail iaPayaDetail = SysUtil.getInstance(IaPayaDetail.class);
					iaPayaDetail.setPayaac(knsPaya.getPayaac());
					iaPayaDetail.setPayabr(knsPaya.getPayabr());
					iaPayaDetail.setPayamn(knsPaya.getPayamn());
					iaPayaDetail.setPayasq(knsPaya.getPayasq());
					iaPayaDetail.setToacna(knsPaya.getToacna());
					iaPayaDetail.setToacno(knsPaya.getToacct());
					iaPayaDetailOption.add(iaPayaDetail);
				}
				iaAcdrInfo.setPayadetail(iaPayaDetailOption);
			}
			
			//销账处理
			if(knsStrk2.getCharlg() == E_PAYATP._2){
				Options<IaPaydDetail> iaPaydDetailOption = new DefaultOptions<IaPaydDetail>();
				List<KnsPayd> knsPaydList = new ArrayList<KnsPayd>();
				knsPaydList = KnsPaydDao.selectAll_kns_payd_odx3(knsStrk2.getNumbsq(), knsStrk2.getCustac(), trandt, true);
				for(KnsPayd knsPayd : knsPaydList){
					if(knsPayd.getPaydst() ==E_PAYDST.ZF){
						continue;
					}
					IaPaydDetail iaPaydDetail = SysUtil.getInstance(IaPaydDetail.class);
					iaPaydDetail.setPaydmn(knsPayd.getPayamn());
					iaPaydDetail.setPrpysq(knsPayd.getPayasq());
					iaPaydDetail.setPaydac(knsPayd.getPaydac());
					iaPaydDetail.setRsdlmn(knsPayd.getRsdlmn());
					iaPaydDetail.setTotlmn(knsPayd.getTotlmn());
					iaPaydDetail.setPaydsq(knsPayd.getPaydsq());//销账序号
					iaPaydDetailOption.add(iaPaydDetail);
				}
				iaAcdrInfo.setPayddetail(iaPaydDetailOption);
			}
			//记账方向
			E_AMNTCD amntcd = knsStrk2.getAmntcd();

			//调用内部户记账服务
			IoInAccount ioInAcctount = SysUtil.getInstance(IoInAccount.class);
			switch (amntcd){
				case DR:
					ioInAcctount.ioInAcdr(iaAcdrInfo);//内部户借方服务
					break;
				case CR:
					ioInAcctount.ioInAccr(iaAcdrInfo);//内部户贷方服务
					break;
				default:
					throw InError.comm.E0003("记账方向:"+amntcd.getValue()+"["+amntcd.getLongName()+"]不支持");
			}
		}else{
			//客户账入账
			IoCaKnaAcdc tblKnaAcdc = CommTools.getRemoteInstance(IoCaSevQryTableInfo.class).getKnaAcdcByCardno(knsStrk2.getCustac(), true);

			
			IoDpKnaAcct ioDpKnaAcct= CommTools.getRemoteInstance(DpAcctSvcType.class).getSettKnaAcctAc(tblKnaAcdc.getCustac());
			E_AMNTCD amntcd = knsStrk2.getAmntcd();
			BigDecimal tranam = BigDecimal.ZERO;
			
			if(knsStrk1.getRebuwo() ==E_REBUWA.B){
				tranam = knsStrk2.getHappbl();
			}else if (knsStrk1.getRebuwo() ==E_REBUWA.R){
				tranam = knsStrk2.getHappbl().negate();
			}
			
//			String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//			CommTools.getBaseRunEnvs().setBusi_org_id(ioDpKnaAcct.getCorpno());
			
			output.setInacno(tblKnaAcdc.getCustac());
			output.setTranam(tranam);
			output.setAmntcd2(amntcd);
			
			//add 20160206 songlw 参数控制 是否利息和积数同时调整
			if(CommUtil.equals("Y", KnpParameterDao.selectOne_odb1("nestcm_isInat", "%", "%", "%", true).getParm_value1())){
				//处理计算调整利息，并记账,返回利息调整对应天数
				int lcDays = prcAdjustInst(ioDpKnaAcct, knsStrk2, tranam, input.getPrtrdt(), amntcd);
				
				//调整天数重新计算，减去利息调整的天数
				ibdays = ibdays - lcDays;
				
				bizlog.debug("===============调整利息计算天数："+lcDays);
				bizlog.debug("===============调整利息后调整积数计算天数："+ibdays);
			}	
			
			if((E_AMNTCD.DR==amntcd&&knsStrk2.getRebuwo() ==E_REBUWA.B)||(E_AMNTCD.CR==amntcd&&knsStrk2.getRebuwo() ==E_REBUWA.R)){
				
				CommTools.getRemoteInstance(IoAccountSvcType.class).checkStatusBeforeAccount(tblKnaAcdc.getCustac(), E_AMNTCD.DR,E_YES___.YES, E_YES___.NO);
				
			}else{
				
				CommTools.getRemoteInstance(IoAccountSvcType.class).checkStatusBeforeAccount(tblKnaAcdc.getCustac(), E_AMNTCD.CR,E_YES___.YES, E_YES___.NO);
			}
			
			//客户账记账输入类字段赋值
			if(E_AMNTCD.CR==amntcd){
				
				SaveDpAcctIn saveDpAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
				saveDpAcctIn.setAcctno(ioDpKnaAcct.getAcctno());
				saveDpAcctIn.setCustac(ioDpKnaAcct.getCustac());
				saveDpAcctIn.setCardno(knsStrk2.getCustac());
				saveDpAcctIn.setOpacna(knsStrk1.getAcctna());
				saveDpAcctIn.setToacct(knsStrk1.getCustac());
				saveDpAcctIn.setCrcycd(knsStrk2.getCrcycd());
				saveDpAcctIn.setTranam(tranam);
				saveDpAcctIn.setNegafg(E_YES___.YES);//支持红字记账
				saveDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_CZ);
				saveDpAcctIn.setRemark(knsStrk2.getReason());
				CommTools.getRemoteInstance(DpAcctSvcType.class).addPostAcctDp(saveDpAcctIn);
			
			}else {
				
				DrawDpAcctIn drawDpAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
				drawDpAcctIn.setAcctno(ioDpKnaAcct.getAcctno());
				drawDpAcctIn.setCustac(ioDpKnaAcct.getCustac());
				drawDpAcctIn.setCardno(knsStrk2.getCustac());
				drawDpAcctIn.setOpacna(knsStrk1.getAcctna());
				drawDpAcctIn.setToacct(knsStrk1.getCustac());
				drawDpAcctIn.setCrcycd(knsStrk2.getCrcycd());
				drawDpAcctIn.setTranam(tranam);
				drawDpAcctIn.setIschck(E_YES___.NO);
				drawDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_CZ);
				drawDpAcctIn.setRemark(knsStrk2.getReason());
				CommTools.getRemoteInstance(DpAcctSvcType.class).addDrawAcctDp(drawDpAcctIn);
			}
			
			
			acmlbl2 = knsStrk1.getHappbl().multiply(new BigDecimal(ibdays));//积数			
			IoDpKnbAcin cplAcin1 = CommTools.getRemoteInstance(IoDpSrvQryTableInfo.class).getKnbAcinOdb1(ioDpKnaAcct.getAcctno(), true);
			if((E_AMNTCD.CR==amntcd&&CommUtil.compare(tranam, BigDecimal.ZERO)>0)||E_AMNTCD.DR==amntcd&&CommUtil.compare(tranam, BigDecimal.ZERO)<0){
				
				cplAcin1.setCutmam(cplAcin1.getCutmam().add(acmlbl2));//调增
			}else{
				cplAcin1.setCutmam(cplAcin1.getCutmam().subtract(acmlbl2));//调减
			}
			//更新积数
			CommTools.getRemoteInstance(IoDpSrvQryTableInfo.class).updateKnbAcinOdb1(cplAcin1);	
			
//			CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
		}
		knsStrk1.setChckus(CommTools.getBaseRunEnvs().getTrxn_teller());
		knsStrk1.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		knsStrk1.setAdamcl(acmlbl1);
		knsStrk1.setStrkst(E_STRKST.ZC);
		KnsStrkDao.updateOne_kns_strk_odx1(knsStrk1);
		
		knsStrk2.setChckus(CommTools.getBaseRunEnvs().getTrxn_teller());		
		knsStrk2.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		knsStrk2.setStrkst(E_STRKST.ZC);
		knsStrk2.setAdamcl(acmlbl2);
		KnsStrkDao.updateOne_kns_strk_odx1(knsStrk2);		
		
		
		//冲正注册

		IoApRegBook cplInput1 = SysUtil.getInstance(IoApRegBook.class);
        cplInput1.setTranam(knsStrk1.getHappbl());
        cplInput1.setTranac(knsStrk1.getCustac()); // 系统外转账
        cplInput1.setEvent1(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //交易流水
        cplInput1.setCrcycd(knsStrk1.getCrcycd());
        cplInput1.setBgindt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
        cplInput1.setTranev(ApUtil.TRANS_EVENT_NESTBK);
       // ApStrike.regBook(cplInput1);	
        IoMsRegEvent evtinput = SysUtil.getInstance(IoMsRegEvent.class);     
        evtinput.setReversal_event_id(ApUtil.TRANS_EVENT_NESTBK);
        evtinput.setInformation_value(SysUtil.serialize(cplInput1));
        MsEvent.register(evtinput, true);
        
        SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._10);
		//机构、柜员额度验证
        //TODO 复核柜员额度验证
		/*IoBrchUserQt ioBrchUserQt = SysUtil.getInstance(IoBrchUserQt.class);
		ioBrchUserQt.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
		ioBrchUserQt.setBusitp(E_BUSITP.TR);
		ioBrchUserQt.setCrcycd(knsStrk1.getCrcycd());
		ioBrchUserQt.setTranam(knsStrk1.getHappbl());
		ioBrchUserQt.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());
		SysUtil.getInstance(IoSrvPbBranch.class).selBrchUserQt(ioBrchUserQt);*/

	}

	/**
	 * 
	 * @Auther renjinghua
	 *		<p>
	 *  	<li>2017年1月12日-下午8:07:28</li>
	 * 		<li>功能说明： 调账时处理调整利息</li>
	 * 		<p>
	 *
	 * @param ioDpKnaAcct 活期账户表信息
	 * @param knsStrk1  隔日冲正登记簿
	 * @param tranam 冲正金额
	 * @param prtrdt 原交易日期
	 * @param amntcd 借贷方向
	 * @return 利息调整天数
	 */
	private int prcAdjustInst(IoDpKnaAcct ioDpKnaAcct, KnsStrk knsStrk, BigDecimal tranam, String prtrdt, E_AMNTCD amntcd) {
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		int lcDays = 0; //调整积数天数
		
		//查询计息定义信息，获取上次结息日期
		String acctno = ioDpKnaAcct.getAcctno();
		bizlog.debug("========================"+CommTools.getBaseRunEnvs().getBusi_org_id());
		IoDpKnbAcin cplDpKnbAcin = SysUtil.getInstance(IoDpSrvQryTableInfo.class).getKnbAcinOdb1(acctno, true);
		String lcindt = cplDpKnbAcin.getLcindt(); // 上次结息日
		
		//如果冲正调账日期与原交易日期之间存在结息记录，则取上次结息时利率，调整利息，如果调减利息补足扣减，则报错返回相差金额
		if(CommUtil.isNotNull(lcindt) && CommUtil.compare(prtrdt, lcindt) < 0 && CommUtil.compare(trandt, lcindt) > 0) {
			// 查询上次结息日结息记录
			IoDpKnbPidl cplDpKnbPidl = InacSqlsDao.selknbPidlByAcctnoPyindt(acctno, lcindt, false);
			
			// 如果存在结息记录则取结息利率进行利息调整
			if(CommUtil.isNotNull(cplDpKnbPidl)){
				lcDays = DateTools2.calDays(prtrdt, lcindt, 0, 0); //计算调整利息天数差
				//计算调整利息对应积数,并根据上次结息利率计算调整利息
				BigDecimal bigChangAcmlbl = tranam.abs().multiply(new BigDecimal(lcDays)); //积数
				bizlog.debug("=================调整利息对应积数为：" + bigChangAcmlbl);
				//计算调整利息
				BigDecimal bigAdjInst = SysUtil.getInstance(IoSrvPbInterestRate.class).countInteresRateByBase(cplDpKnbPidl.getCuusin(), bigChangAcmlbl);
				bigAdjInst = BusiTools.roundByCurrency(knsStrk.getCrcycd(), bigAdjInst, null);
				
				bizlog.debug("=================调整利息为：" + bigAdjInst +",借贷方向：" + amntcd + ",红蓝字：" + knsStrk.getRebuwo());
				
				
				//调整利息不等于0 进行记账
				if(CommUtil.compare(bigAdjInst, BigDecimal.ZERO) != 0){
					
					knsStrk.setAdinst(bigAdjInst.abs()); //登记调整利息
					
					if((E_AMNTCD.DR == amntcd && E_REBUWA.B == knsStrk.getRebuwo()) 
							|| (E_AMNTCD.CR == amntcd && E_REBUWA.R == knsStrk.getRebuwo())){
						//查询电子账户可用余额
						BigDecimal bigAcctbl = SysUtil.getInstance(DpAcctSvcType.class)
								.getAcctaAvaBal(ioDpKnaAcct.getCustac(), acctno, knsStrk.getCrcycd(),
										E_YES___.YES, E_YES___.NO);
						
						bizlog.debug("==================电子账户可用余额为：" + bigAcctbl);
						
						BigDecimal bigAdjam = tranam.abs().add(bigAdjInst.abs());
						
						if(CommUtil.compare(bigAdjam, bigAcctbl) > 0){
							BigDecimal bigDiffam = bigAdjam.subtract(bigAcctbl);
							bizlog.debug("=================调减利息时相差金额为：" + bigDiffam);
							throw InError.comm.E0003("调减利息时账户余额补足，还需补足金额："+ bigDiffam);
						}
						
						//调减利息时，调整利息记账为红字记账
						bigAdjInst = bigAdjInst.negate();
					}
					
					//调整利息记账，利息会计流水方向及D-借，客户账户记账方向为C-贷
					SaveDpAcctIn saveDpAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
					saveDpAcctIn.setAcctno(ioDpKnaAcct.getAcctno());
					saveDpAcctIn.setCustac(ioDpKnaAcct.getCustac());
					saveDpAcctIn.setCardno(knsStrk.getCustac());
					saveDpAcctIn.setCrcycd(knsStrk.getCrcycd());
					saveDpAcctIn.setTranam(bigAdjInst);
					saveDpAcctIn.setNegafg(E_YES___.YES);//支持红字记账
					SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(saveDpAcctIn);
					
					/** 登记支取利息会计流水，入账指令 **/
					IoAccounttingIntf cplIoAccounttingInrt = SysUtil
		                    .getInstance(IoAccounttingIntf.class);
		            cplIoAccounttingInrt.setCuacno(ioDpKnaAcct.getCustac()); //电子账号
		            cplIoAccounttingInrt.setAcctno(acctno); //账号
		            cplIoAccounttingInrt.setProdcd(ioDpKnaAcct.getProdcd()); //产品编号
		            cplIoAccounttingInrt.setDtitcd(ioDpKnaAcct.getAcctcd()); //核算口径
		            cplIoAccounttingInrt.setCrcycd(knsStrk.getCrcycd()); //币种
		            cplIoAccounttingInrt.setTranam(bigAdjInst); //利息
		            cplIoAccounttingInrt.setAcctdt(trandt);// 应入账日期
		            cplIoAccounttingInrt.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		            cplIoAccounttingInrt.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		            cplIoAccounttingInrt.setAcctbr(ioDpKnaAcct.getBrchno()); //登记账户机构
		            cplIoAccounttingInrt.setAmntcd(E_AMNTCD.DR); //借方
		            cplIoAccounttingInrt.setAtowtp(E_ATOWTP.DP); //存款
		            cplIoAccounttingInrt.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型，账务
		            cplIoAccounttingInrt.setBltype(E_BLTYPE.PYIN); //余额属性利息支出
		            //登记交易信息，供总账解析
		            if(CommUtil.equals("1", KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",true).getParm_value1())){
		            	KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		            	para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%", "%",true);
		            	cplIoAccounttingInrt.setTranms(para.getParm_value1());//登记交易信息 20160701  结息           	
		            }              
		            
		            SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
		            		cplIoAccounttingInrt);
					
				}
				
			}
		}
				
		return lcDays;
		
	}

	/* (非 Javadoc) 
	 * <p>Title: selQrstrk</p> 
	 * <p>Description: 该交易用于查询错账冲正记录。输入查询要素进行查询，输出条件内的冲正记录，查询内容分为1-当日冲正和2-隔日冲正。</p> 
	 * @param wroAccQueryInput
	 * @param output 
	 * @see cn.sunline.ltts.busi.in.servicetype.WroAccRollBack#selQrstrk(Options, cn.sunline.ltts.busi.in.servicetype.WroAccRollBack.SelQrstrk.Output) 
	 */ 
	@Override
	public void selQrstrk(StrkpsQryIn wroAccQueryInput,
			cn.sunline.ltts.busi.in.servicetype.WroAccRollBack.SelQrstrk.Output output) {
		
		bizlog.debug("==========错账冲正查询处理开始==========");
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
		
		bizlog.debug("输入检查开始==========");
		
		//查询内容验证
		if(CommUtil.isNull(wroAccQueryInput.getQuerco())){
			throw InError.comm.E0003("输入查询内容不能为空");
		}
		
		//机构号验证
		/*if(CommUtil.isNotNull(wroAccQueryInput.getOrgnbr())){
		
			//原错账机构
			String orbrcp = wroAccQueryInput.getOrgnbr();
			//省清算中心
 			String centbr = BusiTools.getBusiRunEnvs().getCentbr();
 			
 			
			if(!CommUtil.equals(orbrcp.substring(0, 3), tranbr.substring(0, 3))
					&&!CommUtil.equals(centbr, tranbr)){
				
				throw InError.comm.E0003("非省中心不允许跨法人机构查询");

			}
			if(!CommUtil.equals(tranbr.substring(3, 6), "000")&&!CommUtil.equals(orbrcp, tranbr)){
			
				throw InError.comm.E0003("非清算中心只能查询自身机构！");
			}
		}*/
		
		//起始日期验证
		if(CommUtil.isNull(wroAccQueryInput.getEfctdt())){
			throw InError.comm.E0003("输入起始日期不能为空");
		}
		
		if(DateUtil.compareDate(wroAccQueryInput.getEfctdt(), trandt) > 0){
			throw InError.comm.E0003("输入起始日期必须小于系统当前日期");
		}
		
		//终止日期验证
		if(CommUtil.isNull(wroAccQueryInput.getInefdt())){
			throw InError.comm.E0003("输入终止日期不能为空");
		}
		
		if(DateUtil.compareDate(wroAccQueryInput.getEfctdt(), wroAccQueryInput.getInefdt()) > 0){
			throw InError.comm.E0003("输入终止日期必须大于等于起始日期");
		}
		
		if(DateUtil.compareDate(wroAccQueryInput.getInefdt(), trandt) > 0){
			throw InError.comm.E0003("输入终止日期必须小于系统当前日期");
		}
		
		//查询下级机构插入临时表
//		SysUtil.getInstance(IoSrvPbBranch.class).insLowerLevelBrchno(tranbr, E_CRCYCD.RMB);
		String account ="";	
		//账号检查
		/*if(CommUtil.isNotNull(wroAccQueryInput.getAcctno())){

			
			//判断是否客户账号
			if(ApAcctRoutTools.getRouteType(wroAccQueryInput.getAcctno()) == E_ACCTROUTTYPE.DEPOSIT){
				IoCaKnaAcdc tblKnaAcdc =CommTools.getRemoteInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb2( wroAccQueryInput.getAcctno(), true);
				account=tblKnaAcdc.getCustac();//取电子账户
				bizlog.debug("输入客户账号为：" + account);
			}else{
				if(wroAccQueryInput.getAcctno().length() != 23){
					throw InError.comm.E0003("账号输入格式错误，请核查");
				}
				account=wroAccQueryInput.getAcctno();
				bizlog.debug("输入内部账号为：" + account);
			}
		}
		bizlog.debug("==========检查结束");*/
		
		long pageno = CommTools.getBaseRunEnvs().getPage_start();  //页码
		long pagesize = CommTools.getBaseRunEnvs().getPage_size(); //页容量
		Options<StrkpsInfo> strkpsInfoOptions = new DefaultOptions<StrkpsInfo>();
		
		//根据查询内容做不同处理
		if(wroAccQueryInput.getQuerco() == E_QUERCO._1){
			if(CommUtil.isNotNull(wroAccQueryInput.getAcctno())){
				if(wroAccQueryInput.getAcctno().length()==19){
					IoCaKnaAcdc tblKnaAcdc =CommTools.getRemoteInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb2( wroAccQueryInput.getAcctno(), true);
					account=tblKnaAcdc.getCustac();//取电子账户
				}else{
					account=wroAccQueryInput.getAcctno();
				}
			}
			
			//当日错账冲正查询
			Page<StrkpsInfo> StrkpsInfoList = WrAccRbDao.selWroAcctInfo(tranbr,wroAccQueryInput.getOrgnbr(), wroAccQueryInput.getOperte(), 
					account , wroAccQueryInput.getEfctdt(), wroAccQueryInput.getInefdt(), (pageno - 1) * pagesize, pagesize, 0, true);
			//查活期负债账户表
			
			//查定期负债账户表
			//查电子卡号对照表
			for(StrkpsInfo strkpsInfo : StrkpsInfoList.getRecords()){
				KnsCmbk tblKnsCmbk = InTranOutDao.selKnsCmbkByTransq(strkpsInfo.getPrtrsq(), strkpsInfo.getPrtrdt(), false);				
				if(null!=tblKnsCmbk){
					strkpsInfo.setNumbsq(tblKnsCmbk.getAcstno());
				}else{
					KnsStrk tblKnsStrk = InTranOutDao.selKnsStrkByTransq(strkpsInfo.getPrtrsq(), strkpsInfo.getPrtrdt(), false);
					if(null!=tblKnsStrk){
						strkpsInfo.setNumbsq(tblKnsStrk.getNumbsq());
					}
				}
				//拿卡号去查电子账户
				IoCaSevQryTableInfo caSevQryTableInfo = CommTools.getRemoteInstance(IoCaSevQryTableInfo.class); 
				IoCaKnaAcdc knaAcdc=SysUtil.getInstance(IoCaKnaAcdc.class);
				try {
					knaAcdc = caSevQryTableInfo.getKnaAcdcOdb1(strkpsInfo.getAcctno(),E_DPACST.NORMAL, false);
					
				} catch (Exception e) {
					// 异常不捕获
				}
				if(CommUtil.isNull(knaAcdc)||CommUtil.isNull(knaAcdc.getCardno())){
					strkpsInfo.setAcctno(strkpsInfo.getAcctno());//交易账号是内部户账号
		    	}else{
		    		strkpsInfo.setAcctno(knaAcdc.getCardno());//交易账户为电子账户
		    	}				
				strkpsInfoOptions.add(strkpsInfo);
			}
			output.setCount(StrkpsInfoList.getRecordCount());
			CommTools.getBaseRunEnvs().setTotal_count(StrkpsInfoList.getRecordCount());
			output.setStrkpsInfoList(strkpsInfoOptions);
		}else{
			bizlog.debug("查询处理开始==========");
			//隔日错账冲正信息查询
//			queryNestStrkpsInfo(wroAccQueryInput);
			
			
			
			Page<KnsStrk> knsStrkList = WrAccRbDao.selStrkInfo(tranbr,wroAccQueryInput.getOrgnbr(), wroAccQueryInput.getOperte(),
					wroAccQueryInput.getAcctno(), wroAccQueryInput.getEfctdt(), wroAccQueryInput.getInefdt(), (pageno - 1) * pagesize, pagesize, 0, true);
			
			for(KnsStrk knsStrk : knsStrkList.getRecords()){
				StrkpsInfo strkpsInfo = SysUtil.getInstance(StrkpsInfo.class);
				CommUtil.copyProperties(strkpsInfo, knsStrk);
				strkpsInfo.setAcctno(knsStrk.getCustac());
				strkpsInfo.setTranbr(knsStrk.getWronbr());
				strkpsInfo.setTranam(knsStrk.getHappbl());//冲正金额
				strkpsInfo.setWronte(knsStrk.getWronte());//错账柜员传原错账柜员
				strkpsInfo.setTranus(knsStrk.getChckus());//复核柜员
				strkpsInfoOptions.add(strkpsInfo);
			}
			output.setCount(knsStrkList.getRecordCount());
			CommTools.getBaseRunEnvs().setTotal_count(knsStrkList.getRecordCount());

			output.setStrkpsInfoList(strkpsInfoOptions);
		}
		
		bizlog.debug("==========查询处理结束");
	}
	
	 //隔日错账登记簿垃圾数据清除
	 private void dealStrkData(String trandt){
		 bizlog.debug("隔日错账冲正垃圾数据清除-日终处理开始==========");
		 
		 //检索出当日所有状态为 3：未复核， 4：已复核的记录
		 //TODO 因本任务由日终改为在联机交易时处理，sql语句中日期条件由等于trandt改为了小于trandt，若重新使用本方法，需调整回来
		 List<KnsStrk> knsStrkList = InTranOutDao.selStrkRubbish(trandt, false);
		 if(CommUtil.isNull(knsStrkList)){
				 
			 bizlog.debug(">>>>>>>>>>隔日错账冲正登记簿无未复核记录<<<<<<<");
		 }else{
			 
			 bizlog.debug(">>>>>>>>>>隔日错账冲正登记簿有" + knsStrkList.size() + "条数据需要处理<<<<<<<");
			 
			 int count = 1; //记录处理第几条记录，日志打印用
			 for(KnsStrk knsStrk :knsStrkList){
				 
				knsStrk.setStrkst(E_STRKST.ZF);
				KnsStrkDao.updateOne_kns_strk_odx1(knsStrk);
				
				//打印当前处理记录信息，方便排查
				bizlog.debug(">>>>>>>>>>第" + count + "条隔日错账冲正记录，套号" + knsStrk.getNumbsq() + "处理成功<<<<<<<");
				count++;
			 }
		 
		 }
		 
		 bizlog.debug("==========隔日错账冲正垃圾数据清除-日终处理结束");
	 }


	public void acctclebr( String brchno,  String crcycd,  java.math.BigDecimal tranam,cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD amntcd,  final cn.sunline.ltts.busi.in.servicetype.WroAccRollBack.Acctclebr.Output output){
		IoCheckBalance checkBal = SysUtil.getInstance(IoCheckBalance.class);
		//网络核心与柜面核心系统间往来 9930410306
		KnpParameter para2 = KnpParameterDao.selectOne_odb1("InParm.clearbusi","out", "%", "%", true);
		//系统内资金清算往来-网络核心和柜面核心 9930410402
		KnpParameter para3 = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", E_CLACTP._01.getValue(), "%", true);
		//网络核心与柜面核心系统间往来
		IoGlKnaAcct glAcct2 = checkBal.getGlKnaAcct(para2.getParm_value1(), brchno, crcycd, para2.getParm_value2());
		// 网络核心与柜面核心系统内往来
		IoGlKnaAcct glAcct3 = checkBal.getGlKnaAcct(para3.getParm_value1(), brchno, crcycd, para3.getParm_value2());
		E_AMNTCD ctmncd =null ;//系统内待清算记账方向
		E_AMNTCD brmncd =null ;//系统间往来记账方向
		if(CommUtil.isNull(amntcd)){
	
			throw InError.comm.E0003("清算记账方向不能");

		}
		
		if(E_AMNTCD.DR==amntcd){
			
			ctmncd = E_AMNTCD.DR;
			brmncd = E_AMNTCD.CR;			
		}else{
			ctmncd = E_AMNTCD.CR;
			brmncd = E_AMNTCD.DR;			
		}
		
		
		//补充 归属机构 网络柜面核心间往来  系统内资金清算往来-网络核心和柜面核心
		IoAccountClearInfo clearInfo_C = SysUtil.getInstance(IoAccountClearInfo.class);
		IoAccountClearInfo clearInfo_D = SysUtil.getInstance(IoAccountClearInfo.class);
		//贷 归属机构 30410306 
		clearInfo_C.setCorpno("999");//账号归属法人
		clearInfo_C.setAcctno(glAcct2.getAcctno());//系统内往来账号
		clearInfo_C.setAcctna(glAcct2.getAcctna());//系统内往来账户名称
		clearInfo_C.setProdcd(para2.getParm_value1());//产品编码
		clearInfo_C.setAcctbr(brchno);//账务机构
		clearInfo_C.setCrcycd(crcycd);//币种
		clearInfo_C.setAmntcd(brmncd);//借贷标志
		clearInfo_C.setTranam(tranam);//交易金额
		clearInfo_C.setClerst(E_CLERST.WAIT);//清算状态
		clearInfo_C.setClactp(E_CLACTP ._01);//
		clearInfo_C.setToacbr(brchno);//对方机构
		clearInfo_C.setToacct(glAcct3.getAcctno());//
		//登记会计流水清算信息
		SysUtil.getInstance(IoAccountSvcType.class).registKnsAcsqCler(clearInfo_C);
		//借 省中心 30410402
		clearInfo_D.setCorpno("999");//账号归属法人
		clearInfo_D.setAcctno(glAcct3.getAcctno());//系统内往来账号
		clearInfo_D.setAcctna(glAcct3.getAcctna());//系统内往来账户名称
		clearInfo_D.setProdcd(para3.getParm_value1());//产品编码
		clearInfo_D.setAcctbr(brchno);//账务机构
		clearInfo_D.setToacbr(brchno);//对方机构
		clearInfo_D.setCrcycd(crcycd);//币种
		clearInfo_D.setAmntcd(ctmncd);//借贷标志
		clearInfo_D.setTranam(tranam);//交易金额
		clearInfo_D.setClerst(E_CLERST.WAIT);//清算状态
		clearInfo_D.setClactp(E_CLACTP ._01);//
		clearInfo_D.setToacct(glAcct2.getAcctno());//
		//登记会计流水清算信息
		SysUtil.getInstance(IoAccountSvcType.class).registKnsAcsqCler(clearInfo_D);
					/**
			 * 冲正登记
			 */
			IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
			cplInput.setTranev(ApUtil.TRANS_EVENT_CHCKBL);
			cplInput.setTranam(tranam);// 交易金额
			cplInput.setCrcycd(crcycd);// 货币代号
			//ApStrike.regBook(cplInput);
			IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);      
		    apinput.setReversal_event_id(cplInput.getTranev());
		    apinput.setInformation_value(SysUtil.serialize(cplInput));
		    MsEvent.register(apinput, true);
	}


	/**
	 * 内部户隔日错账冲正入账
	 * 
	 */
	@Override
	public void inAccNestcm(cn.sunline.ltts.busi.in.servicetype.WroAccRollBack.InAccNestcm.Input input) {
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
		iaAcdrInfo.setTrantp(E_TRANTP.TR);
		iaAcdrInfo.setAcctno(input.getCustac());
		iaAcdrInfo.setTranam(input.getHappbl());//记账金额
		iaAcdrInfo.setAcuttp(E_ACUTTP._3);//记账类型蓝字
		iaAcdrInfo.setDscrtx(input.getReason());
		iaAcdrInfo.setSmrycd(BusinessConstants.SUMMARY_CZ);
		iaAcdrInfo.setToacct(input.getToacct());
		//红字，记负数
		if(input.getRebuwo() ==E_REBUWA.R){
			iaAcdrInfo.setTranam(input.getHappbl().negate());//记账金额
			iaAcdrInfo.setAcuttp(E_ACUTTP._2);//记账类型红字
		}
		iaAcdrInfo.setCrcycd(input.getCrcycd());//币种
		iaAcdrInfo.setAcbrch(input.getWronbr());//账户机构
		
		//挂账处理
		if(input.getCharlg() == E_PAYATP._1){
			Options<IaPayaDetail> iaPayaDetailOption = new DefaultOptions<IaPayaDetail>();
			List<KnsPaya> knsPayaList = new ArrayList<KnsPaya>();
			knsPayaList = KnsPayaDao.selectAll_kns_paya_odx3(input.getNumbsq(), input.getCustac(), trandt, true);
			for(KnsPaya knsPaya : knsPayaList){
				if(knsPaya.getPayast() ==E_PAYAST.ZF){
					continue;
				}
				IaPayaDetail iaPayaDetail = SysUtil.getInstance(IaPayaDetail.class);
				iaPayaDetail.setPayaac(knsPaya.getPayaac());
				iaPayaDetail.setPayabr(knsPaya.getPayabr());
				iaPayaDetail.setPayamn(knsPaya.getPayamn());
				iaPayaDetail.setPayasq(knsPaya.getPayasq());
				iaPayaDetail.setToacna(knsPaya.getToacna());
				iaPayaDetail.setToacno(knsPaya.getToacct());
				iaPayaDetailOption.add(iaPayaDetail);
			}
			iaAcdrInfo.setPayadetail(iaPayaDetailOption);
		}
		
		//销账处理
		if(input.getCharlg() == E_PAYATP._2){
			
			Options<IaPaydDetail> iaPaydDetailOption = new DefaultOptions<IaPaydDetail>();
			List<KnsPayd> knsPaydList = new ArrayList<KnsPayd>();
			knsPaydList = KnsPaydDao.selectAll_kns_payd_odx3(input.getNumbsq(), input.getCustac(), trandt, true);
			
			for(KnsPayd knsPayd : knsPaydList){
				if(knsPayd.getPaydst() ==E_PAYDST.ZF){
					continue;
				}
				IaPaydDetail iaPaydDetail = SysUtil.getInstance(IaPaydDetail.class);
				iaPaydDetail.setPaydmn(knsPayd.getPayamn());
				iaPaydDetail.setPrpysq(knsPayd.getPayasq());
				iaPaydDetail.setPaydac(knsPayd.getPaydac());
				iaPaydDetail.setRsdlmn(knsPayd.getRsdlmn());
				iaPaydDetail.setTotlmn(knsPayd.getTotlmn());
				iaPaydDetail.setPaydsq(knsPayd.getPaydsq());
				iaPaydDetailOption.add(iaPaydDetail);
			}
			iaAcdrInfo.setPayddetail(iaPaydDetailOption);
		}
		
		//记账方向
		E_AMNTCD amntcd = input.getAmntcd();		
		//调用内部户记账服务
		IoInAccount ioInAcctount = SysUtil.getInstance(IoInAccount.class);

		switch (amntcd){
			case DR:
				ioInAcctount.ioInAcdr(iaAcdrInfo);//内部户借方服务
				break;
			case CR:
				ioInAcctount.ioInAccr(iaAcdrInfo);//内部户贷方服务
				break;
			default:
				throw InError.comm.E0003("记账方向:"+amntcd.getValue()+"["+amntcd.getLongName()+"]不支持");
		}
		
		//冲正注册
		IoApRegBook cplInput1 = SysUtil.getInstance(IoApRegBook.class);
        cplInput1.setTranam(input.getHappbl());
        cplInput1.setTranac(input.getCustac()); // 系统外转账
        cplInput1.setEvent1(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //交易流水
        cplInput1.setCrcycd(input.getCrcycd());
        cplInput1.setBgindt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
        cplInput1.setTranev(ApUtil.TRANS_EVENT_NESTBK);
        //ApStrike.regBook(cplInput1);	
        IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);      
        apinput.setReversal_event_id(cplInput1.getTranev());
        apinput.setInformation_value(SysUtil.serialize(cplInput1));
        MsEvent.register(apinput, true);
        
        //SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._10);
				
	}

	 /**
     * 
     * @Auther xionglz
     *         <p>
     *         <li>2017年12月09日-上午11:27:40</li>
     *         <li>功能说明：电子账户调账控制</li>
     *         <p>
     * 
     * @param 
     */
   
    public void checkTrans(String custac, E_AMNTCD amntcd,E_REBUWA rebuwa,BigDecimal happal){
//    	E_YES___ ptfroz = E_YES___.NO;// 部冻
//		E_YES___ brfroz = E_YES___.NO;// 借冻
//		E_YES___ dbfroz = E_YES___.NO;// 双冻
//		E_YES___ alstop = E_YES___.NO;// 全止
//		E_YES___ ptstop = E_YES___.NO;// 部止
//		E_YES___ bkalsp = E_YES___.NO;// 银行止付全止
//		E_YES___ otalsp = E_YES___.NO;// 外部止付全止
//		E_YES___ clstop = E_YES___.NO;// 客户止付
//		E_YES___ preaut = E_YES___.NO;// 预授权
//		E_YES___ billin = E_YES___.NO;// 开单
//		E_YES___ certdp = E_YES___.NO;// 存款证明
//		E_YES___ pledge = E_YES___.NO;// 质押
    	//查询电子账户状态信息
    	  E_CUACST status = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
    	//查询电子账户状态字
          IoDpAcStatusWord cplGetAcStWord = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
        //查询电子账户卡信息
          IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb1(custac, E_DPACST.NORMAL, false);
          if (CommUtil.isNull(tblKnaAcdc) || CommUtil.isNull(tblKnaAcdc.getCardno())) {
              throw DpModuleError.DpstAcct.BNAS1695();
          }
         //电子账户状态字为双冻
          if(cplGetAcStWord.getDbfroz()==E_YES___.YES){
        	  throw DpModuleError.DpstAcct.BNAS1696(tblKnaAcdc.getCardno());
          }else if(amntcd==E_AMNTCD.DR&&rebuwa==E_REBUWA.B||amntcd==E_AMNTCD.CR&&rebuwa==E_REBUWA.R){//方向为借方蓝字或者贷方红字
        	  		if(cplGetAcStWord.getBrfroz()==E_YES___.YES){//电子账户状态字为全止或借冻
        	  			throw DpModuleError.DpstAcct.BNAS3005();
        	  		}else {//电子账户状态字为部冻或部止
        	  			//判断电子账户可用余额是否小于调账
        	  			BigDecimal acctbl = BigDecimal.ZERO;
        	  			IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);
        	  			//获取电子账户可用余额
        	  			acctbl = SysUtil.getInstance(DpAcctSvcType.class)
        						.getAcctaAvaBal(tblKnaAcdc.getCustac(), cplKnaAcct.getAcctno(),
        								cplKnaAcct.getCrcycd(), E_YES___.YES, E_YES___.NO);
        	  			//判断调账金额是否小于可用余额
        	  			if(CommUtil.compare(acctbl, happal)<0){
        	  				throw DpModuleError.DpstAcct.BNAS3006();
        	  			}
        	  		}
          }
    }
    /**
    @Auther xionglz
    *         <p>
    *         <li>2017年12月09日-上午11:27:40</li>
    *         <li>功能说明：电子账户调账校验原交易流水</li>
    *         <p>
    * 
    * @param **/
    	public void checkAcsq(String custac,String prtrsq,E_AMNTCD amntcd,BigDecimal happal){
    		//使用电子账户调账，记账方向必须在贷方
			if(amntcd.equals(E_AMNTCD.DR)){
				throw InError.comm.E0003("电子账户不允许借方调账！");
			}
			//根据电子账号查询结算账户
			IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);
			if(CommUtil.isNull(cplKnaAcct)){
				throw InError.comm.E0003("未找到有效结算账号！");
			}
			//查询会计流水
			Options<IoCaHknsAcsq> tblhknsacsq=SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnsAcsqOdb1(prtrsq, cplKnaAcct.getAcctno());
			if(tblhknsacsq.size()==0){
				throw InError.comm.E0003("未找到交易流水或交易流水不匹配！");
			}
			//汇总会计流水发生额
			BigDecimal tranam =BigDecimal.ZERO;
			for(int i = 0;i<tblhknsacsq.size();i++){
				tranam=tranam.add(tblhknsacsq.get(i).getTranam());
				}
			//调账金额必须小于原交易发生额
			if(CommUtil.compare(happal, tranam)>0){
				throw InError.comm.E0003("调账金额不能大于原交易发生额！");
			}
    	}
    /**
       @Auther xionglz
       *         <p>
       *         <li>2017年12月09日-上午11:27:40</li>
       *         <li>功能说明：表外记账录入或修改</li>
       *         <p>
       * 
       * @param **/
    	public String addOutTable(Input input){
    		//表外记账处理
    		KnsStrk knsStrk1 = SysUtil.getInstance(KnsStrk.class);
    		String numbsq = "";
    		if(input.getOperwy() == E_OPERWY.INS){
    			
    			numbsq = input.getNumbsq();//
    			
    			//关联错账冲正查询套号是否已存在
    			List<KnsCmbk> knsCmbks = InTranOutDao.selKnsCmbkByAcst(input.getNumbsq(), CommTools.getBaseRunEnvs().getTrxn_date(), false);
            	List<KnsStrk> knsStrks = WrAccRbDao.selKnsStrkByNumbsq(input.getNumbsq(), CommTools.getBaseRunEnvs().getTrxn_date(), false);
            	
            	if(CommUtil.isNotNull(knsCmbks)||CommUtil.isNotNull(knsStrks)){
            		throw InError.comm.E0015(input.getNumbsq());    		 
            	}

    			knsStrk1.setNumbsq(numbsq);
    			knsStrk1.setSerlno(0);
    			
    		}else if(input.getOperwy() == E_OPERWY.UPD){
    			
    			if (CommUtil.isNull(input.getNumbsq())){
    				throw InError.comm.E0003("修改时，套号不能为空");
    			}
    			
    			knsStrk1 = KnsStrkDao.selectOne_kns_strk_odx1(input.getNumbsq(), 0, CommTools.getBaseRunEnvs().getTrxn_date(), true);
    			if(knsStrk1.getStrkst() != E_STRKST.WFH){
    				throw InError.comm.E0003("当前状态不允许修改操作");
    			}
    		}
    		
    		//表外账号处理
    		knsStrk1.setPrtrdt(input.getPrtrdt());
    		knsStrk1.setPrtrsq(input.getPrtrsq());//如果流水为空，则给none
    		knsStrk1.setWronbr(input.getWronbr());
    		knsStrk1.setWronte(input.getWronte());
    		knsStrk1.setReason(input.getReason());
    		knsStrk1.setStrkst(E_STRKST.WFH);
    		
    		
    		knsStrk1.setCustac(input.getCustac());
    		knsStrk1.setAcctna(input.getAcctna());
    		knsStrk1.setCrcycd(input.getCrcycd());
    		knsStrk1.setCsextg(input.getCsextg());
    		knsStrk1.setRebuwo(input.getRebuwo());
    		knsStrk1.setAmntcd(input.getAmntcd());
    		knsStrk1.setHappbl(input.getHappbl());
    		knsStrk1.setIoflag(E_STTRTP.O);//设置表内外 标识
    		if(CommUtil.isNull(input.getCharlg())){
    			knsStrk1.setCharlg(E_PAYATP._0);
    		}else{
    			knsStrk1.setCharlg(input.getCharlg());
    		}
    			
    		knsStrk1.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
    		knsStrk1.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//交易流水
    		knsStrk1.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());//交易柜员
    		knsStrk1.setTrantp(input.getTrantp());//交易类型
    		knsStrk1.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq());//柜员流水 //getUssqno() to getTransq() rambo
    		knsStrk1.setUssrdt(CommTools.getBaseRunEnvs().getTrxn_date());//柜员日期
    		
    		if(input.getOperwy() == E_OPERWY.INS){
    			KnsStrkDao.insert(knsStrk1);
    		}else{
    			KnsStrkDao.updateOne_kns_strk_odx1(knsStrk1);
    		}
    		return numbsq;//返回套号
    	}
    	 /**
        @Auther xionglz
        *         <p>
        *         <li>2017年12月09日-上午11:27:40</li>
        *         <li>功能说明：表外记账复核</li>
        *         <p>
        * 
        * @param **/ 
    	public void updateOutTable(cn.sunline.ltts.busi.in.servicetype.WroAccRollBack.ChkNestck.Input input,KnsStrk knsstrk){
    		
    	
    		String chckus = CommTools.getBaseRunEnvs().getTrxn_teller();
    		String chckbr = CommTools.getBaseRunEnvs().getTrxn_branch();
    	
    		if(!CommUtil.equals(chckbr, knsstrk.getWronbr())){
    			throw InError.comm.E0003("非本机构错账记录，不能复核！");
    		}
    		
    		if(CommUtil.equals(chckus, knsstrk.getTranus())){
    			throw InError.comm.E0003("请换人复核！");
    		}
    		
    		E_OPERWZ operwy =  input.getOperwy();
    		if(operwy == null){
    			throw InError.comm.E0003("操作类型不能为空！");
    		}
    		//复核处理
    		if(!CommUtil.equals(knsstrk.getCustac(), input.getCustac()) ){
    			throw InError.comm.E0003("账号与录入账号不符，请核实！");
    		}
    		if(knsstrk.getAmntcd() != input.getAmntcd() ){
    			throw InError.comm.E0003("借贷标志与录入信息不符，请核实！");
    		}
    		if(!CommUtil.equals(input.getHappbl(), knsstrk.getHappbl())){
    			throw InError.comm.E0003("交易金额不符请核对，请核实！");
    		}
    		if(input.getRebuwo()!= knsstrk.getRebuwo() ){
    			throw InError.comm.E0003("红蓝字不符请核对，请核实！");
    		}
    		if(input.getCharlg()!= knsstrk.getCharlg()){
    			throw InError.comm.E0003("挂销账标志不符请核对，请核实！");
    		}
    		
    		knsstrk.setReason(input.getReason());
    		knsstrk.setChckus(chckus);
    		knsstrk.setStrkst(E_STRKST.YFH);
    		KnsStrkDao.updateOne_kns_strk_odx1(knsstrk);
    	}
    	/**
        @Auther xionglz
        *         <p>
        *         <li>2017年12月09日-上午11:27:40</li>
        *         <li>功能说明：表外记账入账</li>
        *         <p>
        * 
        * @param **/ 
    	public void checInOutTable(cn.sunline.ltts.busi.in.servicetype.WroAccRollBack.AccNestcm.Input input,KnsStrk knsstrk){
    		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller();
    		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
    		if(knsstrk.getStrkst() == E_STRKST.ZF){
    			throw InError.comm.E0003("套号已作废，请核对！");
    		}
    		
    		if(knsstrk.getStrkst() != E_STRKST.YFH){
    			throw InError.comm.E0003("套号不处于已复核状态，不能进行入账操作！");
    		}
    		
    		if(!CommUtil.equals(tranbr, knsstrk.getWronbr())){
    			throw InError.comm.E0003("非本机构错账记录，不能复核！");
    		}
    		
    		if(!CommUtil.equals(tranus, knsstrk.getChckus())){
    			throw InError.comm.E0003("入账柜员必须与复核柜员相同！");
    		}
    		IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
			iaAcdrInfo.setTrantp(E_TRANTP.TR);
			iaAcdrInfo.setAcctno(knsstrk.getCustac());
			iaAcdrInfo.setTranam(knsstrk.getHappbl());//记账金额
			iaAcdrInfo.setAcuttp(E_ACUTTP._3);//记账类型蓝字
			iaAcdrInfo.setDscrtx(knsstrk.getReason());
			iaAcdrInfo.setSmrycd(BusinessConstants.SUMMARY_CZ);
			//红字，记负数
			if(knsstrk.getRebuwo() ==E_REBUWA.R){
				iaAcdrInfo.setTranam(knsstrk.getHappbl().negate());//记账金额
				iaAcdrInfo.setAcuttp(E_ACUTTP._2);//记账类型红字
			}
			iaAcdrInfo.setCrcycd(knsstrk.getCrcycd());//币种
			iaAcdrInfo.setAcbrch(knsstrk.getWronbr());//账户机构
			//记账方向
			E_AMNTCD amntcd = knsstrk.getAmntcd();		
			//调用内部户记账服务
			IoInAccount ioInAcctount = CommTools.getRemoteInstance(IoInAccount.class);

			switch (amntcd){
				case RV:
					ioInAcctount.ioInAcrv(iaAcdrInfo);//内部户收方服务
					break;
				case PY:
					ioInAcctount.ioInAcpv(iaAcdrInfo);//内部户付方服务
					break;
				default:
					throw InError.comm.E0003("记账方向:"+amntcd.getValue()+"["+amntcd.getLongName()+"]不支持");
			}
			//内部户表外记账
			
			knsstrk.setChckus(CommTools.getBaseRunEnvs().getTrxn_teller());
			knsstrk.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			knsstrk.setStrkst(E_STRKST.ZC);
			KnsStrkDao.updateOne_kns_strk_odx1(knsstrk);
				
    	}

        /* 
         * 20180116
         * xionglz
         */
        @Override
        public void InOutTableAccount(cn.sunline.ltts.busi.in.servicetype.WroAccRollBack.InOutTableAccount.Input input,
                cn.sunline.ltts.busi.in.servicetype.WroAccRollBack.InOutTableAccount.Output output) {
            
            //设置内部户记账参数
            IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
            iaAcdrInfo.setTrantp(E_TRANTP.TR);
            iaAcdrInfo.setAcctno(input.getCustac());
            iaAcdrInfo.setTranam(input.getHappbl());//记账金额
            iaAcdrInfo.setAcuttp(E_ACUTTP._3);//记账类型蓝字
            iaAcdrInfo.setDscrtx(input.getReason());
            iaAcdrInfo.setSmrycd(BusinessConstants.SUMMARY_CZ);
            //红字，记负数
            if(input.getRebuwo()==E_REBUWA.R){
                iaAcdrInfo.setTranam(input.getHappbl().negate());//记账金额
                iaAcdrInfo.setAcuttp(E_ACUTTP._2);//记账类型红字
            }
            iaAcdrInfo.setCrcycd(input.getCrcycd());//币种
            iaAcdrInfo.setAcbrch(input.getWronbr());//账户机构
            //记账方向
            E_AMNTCD amntcd = input.getAmntcd();      
            //调用内部户记账服务
            IoInAccount ioInAcctount = CommTools.getRemoteInstance(IoInAccount.class);

            switch (amntcd){
                case RV:
                    ioInAcctount.ioInAcrv(iaAcdrInfo);//内部户收方服务
                    break;
                case PY:
                    ioInAcctount.ioInAcpv(iaAcdrInfo);//内部户付方服务
                    break;
                default:
                    throw InError.comm.E0003("记账方向:"+amntcd.getValue()+"["+amntcd.getLongName()+"]不支持");
            }
            //更新错账登记簿
            List<KnsStrk> knsStrkList = WrAccRbDao.selKnsStrkByNumbsq(input.getNumbsq(), CommTools.getBaseRunEnvs().getTrxn_date(), false);// 改为套号加日期查询
            KnsStrk knsstrk=knsStrkList.get(0);
            
            knsstrk.setChckus(CommTools.getBaseRunEnvs().getTrxn_teller());
            knsstrk.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
            knsstrk.setStrkst(E_STRKST.ZC);
            KnsStrkDao.updateOne_kns_strk_odx1(knsstrk);
            
        }


		
   			
}

