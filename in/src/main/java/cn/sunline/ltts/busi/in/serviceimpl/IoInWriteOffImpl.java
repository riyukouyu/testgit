package cn.sunline.ltts.busi.in.serviceimpl;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.KnsCmbk;
import cn.sunline.ltts.busi.in.tables.In.KnsCmbkDao;
import cn.sunline.ltts.busi.in.tables.In.KnsPaya;
import cn.sunline.ltts.busi.in.tables.In.KnsPayaDao;
import cn.sunline.ltts.busi.in.tables.In.KnsPayd;
import cn.sunline.ltts.busi.in.tables.In.KnsPaydDao;
import cn.sunline.ltts.busi.in.tables.In.KnsStrk;
import cn.sunline.ltts.busi.in.tables.In.KnsStrkDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.IoDpTable;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbAcin;
import cn.sunline.ltts.busi.iobus.type.IoInWriteOffComplex.IavccmRbInput;
import cn.sunline.ltts.busi.iobus.type.IoInWriteOffComplex.NestcmRbInput;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoBusinoInfo;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.InEnumType;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CMBK_TRANST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CNTSYS;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_IAVCTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYAST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYATP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYDST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_REBUWA;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_STRKST;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
 /**
  * 内部户转出及错账管理实现
  * 内部户转出套平入账冲销
错账管理记账冲销
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoInAccTranOutImpl", longname="内部户转出及错账管理实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoInWriteOffImpl implements cn.sunline.ltts.busi.iobus.servicetype.IoInWriteOff{
	 
	private static final BizLog bizlog = BizLogUtil.getBizLog(IoInAcctTranOutImpl.class);
	
	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-11 11：12</li>
	 *         <li>套平入账冲销</li>
	 *         </p>
	 * @param transq
	 *           错账流水
	 * */
	public void iavccmRb(IavccmRbInput input){
		
		bizlog.debug("==========套平入账冲销处理开始==========");
		
		String transq = input.getTransq();
		String trandt = input.getTrandt();
		//通过流水冲正当日新增的隔日错账冲正信息
		List<KnsCmbk> KnsCmbkList = KnsCmbkDao.selectAll_kns_cmbk_odx3(transq, trandt, true);
		for(KnsCmbk knsCmbk : KnsCmbkList){
			
			if(knsCmbk.getIavcst() == E_CMBK_TRANST._2){
				continue;
			}
			//更新冲销状态
			knsCmbk.setIavcst(E_CMBK_TRANST._9);
			KnsCmbkDao.updateOne_kns_cmbk_odx1(knsCmbk);
			
			//系统外内部账需改变挂销账状态
			if(knsCmbk.getIavctp() == E_IAVCTP._1){
				if(knsCmbk.getCntsys() == E_CNTSYS._0){
					String account = knsCmbk.getOtacno();
					IoBusinoInfo ioBusinoInfo = SysUtil.getInstance(IoInQuery.class).selBusiIndoByBusino(account.substring(8, 18));
					if(account.length() != 23 || CommUtil.isNull(ioBusinoInfo)){
						
						bizlog.debug("系统外内部账冲销挂销账处理开始==========");
						//系统外转内部户，且账号非系统内账号，则改动改变刮削账状态
						String acstno = knsCmbk.getAcstno();
						
						//挂账信息处理
						if(knsCmbk.getPayatp() == E_PAYATP._1){
							List<KnsPaya> knsPayaList = KnsPayaDao.selectAll_kns_paya_odx5(acstno, knsCmbk.getPayseq(), trandt, true);
							
							for(KnsPaya knsPaya : knsPayaList){
								//作废记录过滤
								if(knsPaya.getPayast() == E_PAYAST.ZF){
									continue;
								}
								//更新冲销状态
								knsPaya.setPayast(E_PAYAST.CX);
								KnsPayaDao.updateOne_kns_paya_odx1(knsPaya);
							}
						}
						
						//销账信息处理
						if(knsCmbk.getPayatp() == E_PAYATP._2){
							List<KnsPayd> knsPaydList = KnsPaydDao.selectAll_kns_payd_odx5(acstno, knsCmbk.getPayseq(), trandt, true);
							
							for(KnsPayd knsPayd : knsPaydList){
								//作废记录过滤
								if(knsPayd.getPaydst() == E_PAYDST.ZF){
									continue;
								}
								//更新冲销状态
								knsPayd.setPaydst(E_PAYDST.CX);
								KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);
							}
						}
						
						bizlog.debug("==========系统外内部账冲销挂销账处理结束");
					}
				}
			}
		}
		
		bizlog.debug("==========套平入账冲销处理结束==========");
	}
	
	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-11 11：12</li>
	 *         <li>隔日错账冲正冲销</li>
	 *         </p>
	 * @param transq
	 *           错账流水
	 * */
	@Override
	public void nestcmRb(NestcmRbInput input){
		
		bizlog.debug("==========隔日错账冲正冲销处理开始==========");
		
		String transq = input.getTransq();
		String trandt = input.getTrandt();
		
		//通过流水冲正当日新增的隔日错账冲正信息
		List<KnsStrk> knsStrkList = KnsStrkDao.selectAll_kns_strk_odx3(trandt, transq, true);
		for(KnsStrk knsStrk : knsStrkList){
			knsStrk.setStrkst(E_STRKST.CX);
			KnsStrkDao.updateOne_kns_strk_odx1(knsStrk);
			
			//判断是否存款账户
			if (ApAcctRoutTools.getRouteType(knsStrk.getCustac()) == E_ACCTROUTTYPE.DEPOSIT){
				
				E_AMNTCD amntcd = knsStrk.getAmntcd(); //借贷标志
				E_REBUWA rebuwa = knsStrk.getRebuwo(); //红蓝字
				//调整利息
				BigDecimal bigAdinst = knsStrk.getAdinst(); //调整利息
				//调整积数
				BigDecimal bigAdamcl = knsStrk.getAdamcl(); //调整积数
				
				//冲正时，如果原记录为蓝字记账，则更新为负金额
				if(E_REBUWA.B == rebuwa){
					bigAdinst = bigAdinst.negate();
				}
				
				if(CommUtil.compare(bigAdinst, BigDecimal.ZERO) != 0 || CommUtil.compare(bigAdamcl, BigDecimal.ZERO) != 0){
					
					//查询电子账户信息
					IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcByCardno(knsStrk.getCustac(), true);
					
					//查询电子账户负债子账户，结算户
					IoDpKnaAcct cplKnaAcct= SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(tblKnaAcdc.getCustac());
					String acctno = cplKnaAcct.getAcctno();
					
//					String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//					CommTools.getBaseRunEnvs().setBusi_org_id(cplKnaAcct.getCorpno());
					
					//调整利息不为0
					if(CommUtil.compare(bigAdinst, BigDecimal.ZERO) != 0){
						
						if(E_AMNTCD.DR == amntcd ){
							
							/** 登记支取利息会计流水，入账指令 **/
							IoAccounttingIntf cplIoAccounttingInrt = SysUtil
									.getInstance(IoAccounttingIntf.class);
							cplIoAccounttingInrt.setCuacno(cplKnaAcct.getCustac()); //电子账号
							cplIoAccounttingInrt.setAcctno(acctno); //账号
							cplIoAccounttingInrt.setProdcd(cplKnaAcct.getProdcd()); //产品编号
							cplIoAccounttingInrt.setDtitcd(cplKnaAcct.getAcctcd()); //核算口径
							cplIoAccounttingInrt.setCrcycd(knsStrk.getCrcycd()); //币种
							cplIoAccounttingInrt.setTranam(bigAdinst); //利息
							cplIoAccounttingInrt.setAcctdt(trandt);// 应入账日期
							cplIoAccounttingInrt.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
							cplIoAccounttingInrt.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
							cplIoAccounttingInrt.setAcctbr(cplKnaAcct.getBrchno()); //登记账户机构
							cplIoAccounttingInrt.setAmntcd(E_AMNTCD.CR); //贷方
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
						}else{
							
							/** 登记支取利息会计流水，入账指令 **/
							IoAccounttingIntf cplIoAccounttingInrt = SysUtil
									.getInstance(IoAccounttingIntf.class);
							cplIoAccounttingInrt.setCuacno(cplKnaAcct.getCustac()); //电子账号
							cplIoAccounttingInrt.setAcctno(acctno); //账号
							cplIoAccounttingInrt.setProdcd(cplKnaAcct.getProdcd()); //产品编号
							cplIoAccounttingInrt.setDtitcd(cplKnaAcct.getAcctcd()); //核算口径
							cplIoAccounttingInrt.setCrcycd(knsStrk.getCrcycd()); //币种
							cplIoAccounttingInrt.setTranam(bigAdinst); //利息
							cplIoAccounttingInrt.setAcctdt(trandt);// 应入账日期
							cplIoAccounttingInrt.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
							cplIoAccounttingInrt.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
							cplIoAccounttingInrt.setAcctbr(cplKnaAcct.getBrchno()); //登记账户机构
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
					
					//调整积数不为0
					if(CommUtil.compare(bigAdamcl, BigDecimal.ZERO) != 0){
						
						IoDpKnbAcin cplAcin1 = SysUtil.getInstance(IoDpSrvQryTableInfo.class).getKnbAcinOdb1(acctno, true);
						
						//贷方蓝字记或借方红字记账 为调增，冲正时，做反向操作
						if((E_AMNTCD.CR == amntcd && E_REBUWA.B == rebuwa)
								||E_AMNTCD.DR == amntcd && E_REBUWA.R == rebuwa){
							
							cplAcin1.setCutmam(cplAcin1.getCutmam().subtract(bigAdamcl));//冲正调增
						}else{
							cplAcin1.setCutmam(cplAcin1.getCutmam().add(bigAdamcl));//冲正调减
						}
						
						//更新积数
						SysUtil.getInstance(IoDpSrvQryTableInfo.class).updateKnbAcinOdb1(cplAcin1);
						
					}
					
//					CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
				}
				
			}
		}
		
		bizlog.debug("==========隔日错账冲正冲销处理结束==========");
	}
	

	@Override
	public Integer prcAdjust(IoDpKnaAcct ioDpKnaAcct, BigDecimal tranam,
			String prtrdt, E_AMNTCD amntcd, E_REBUWA rebuwo) {
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
	    int lcDays = 0; //利息调整天数
	    int ibdays = DateTools2.calDays(prtrdt, trandt, 0, 0); //积数调整天数
	    
	    if(ibdays == 0){
	    	return 0;
	    }
	    

	    //利息调整
	    String acctno = ioDpKnaAcct.getAcctno();
	    bizlog.debug("========================" + CommTools.getBaseRunEnvs().getBusi_org_id(), new Object[0]);
	    IoDpTable.IoDpKnbAcin cplDpKnbAcin = ((IoDpSrvQryTableInfo)SysUtil.getInstance(IoDpSrvQryTableInfo.class)).getKnbAcinOdb1(acctno, Boolean.valueOf(true));
	    String lcindt = cplDpKnbAcin.getLcindt();

	    if ((CommUtil.isNotNull(lcindt)) && (CommUtil.compare(prtrdt, lcindt) < 0) && (CommUtil.compare(trandt, lcindt) >= 0))
	    {
	      IoDpTable.IoDpKnbPidl cplDpKnbPidl = InacSqlsDao.selknbPidlByAcctnoPyindt(acctno, lcindt, false);

	      if (CommUtil.isNotNull(cplDpKnbPidl)) {
	        lcDays = DateTools2.calDays(prtrdt, lcindt, 0, 0);

	        BigDecimal bigChangAcmlbl = tranam.abs().multiply(new BigDecimal(lcDays));
	        bizlog.debug("=================调整利息对应积数为：" + bigChangAcmlbl, new Object[0]);

	        BigDecimal bigAdjInst = ((IoSrvPbInterestRate)SysUtil.getInstance(IoSrvPbInterestRate.class)).countInteresRateByBase(cplDpKnbPidl.getCuusin(), bigChangAcmlbl);
	        bigAdjInst = BusiTools.roundByCurrency(ioDpKnaAcct.getCrcycd(), bigAdjInst, null);

	        bizlog.debug("=================调整利息为：" + bigAdjInst + ",借贷方向：" + amntcd + ",红蓝字：" + rebuwo, new Object[0]);

	        if (CommUtil.compare(bigAdjInst, BigDecimal.ZERO) != 0)
	        {
	          if (((BaseEnumType.E_AMNTCD.DR == amntcd) && (InEnumType.E_REBUWA.B == rebuwo)) || ((BaseEnumType.E_AMNTCD.CR == amntcd) && (InEnumType.E_REBUWA.R == rebuwo)))
	          {
	            BigDecimal bigAcctbl = ((DpAcctSvcType)SysUtil.getInstance(DpAcctSvcType.class)).getAcctaAvaBal(ioDpKnaAcct.getCustac(), acctno, ioDpKnaAcct.getCrcycd(), BaseEnumType.E_YES___.YES, BaseEnumType.E_YES___.NO);

	            bizlog.debug("==================电子账户可用余额为：" + bigAcctbl, new Object[0]);

	            BigDecimal bigAdjam = tranam.abs().add(bigAdjInst.abs());

	            if (CommUtil.compare(bigAdjam, bigAcctbl) > 0) {
	              BigDecimal bigDiffam = bigAdjam.subtract(bigAcctbl);
	              bizlog.debug("=================调减利息时相差金额为：" + bigDiffam, new Object[0]);
	              throw InError.comm.E0003("调减利息时账户余额补足，还需补足金额：" + bigDiffam);
	            }

	            bigAdjInst = bigAdjInst.negate();
	          }

	          DpProdSvc.SaveDpAcctIn saveDpAcctIn = (DpProdSvc.SaveDpAcctIn)SysUtil.getInstance(DpProdSvc.SaveDpAcctIn.class);
	          saveDpAcctIn.setAcctno(ioDpKnaAcct.getAcctno());
	          saveDpAcctIn.setCustac(ioDpKnaAcct.getCustac());
	          saveDpAcctIn.setCardno(ioDpKnaAcct.getCustac());
	          saveDpAcctIn.setCrcycd(ioDpKnaAcct.getCrcycd());
	          saveDpAcctIn.setTranam(bigAdjInst);
	          saveDpAcctIn.setNegafg(BaseEnumType.E_YES___.YES);
	          ((DpAcctSvcType)SysUtil.getInstance(DpAcctSvcType.class)).addPostAcctDp(saveDpAcctIn);

	          IoAccountComplexType.IoAccounttingIntf cplIoAccounttingInrt = (IoAccountComplexType.IoAccounttingIntf)SysUtil.getInstance(IoAccountComplexType.IoAccounttingIntf.class);

	          cplIoAccounttingInrt.setCuacno(ioDpKnaAcct.getCustac());
	          cplIoAccounttingInrt.setAcctno(acctno);
	          cplIoAccounttingInrt.setProdcd(ioDpKnaAcct.getProdcd());
	          cplIoAccounttingInrt.setDtitcd(ioDpKnaAcct.getAcctcd());
	          cplIoAccounttingInrt.setCrcycd(ioDpKnaAcct.getCrcycd());
	          cplIoAccounttingInrt.setTranam(bigAdjInst);
	          cplIoAccounttingInrt.setAcctdt(trandt);
	          cplIoAccounttingInrt.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
	          cplIoAccounttingInrt.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
	          cplIoAccounttingInrt.setAcctbr(ioDpKnaAcct.getBrchno());
	          cplIoAccounttingInrt.setAmntcd(BaseEnumType.E_AMNTCD.DR);
	          cplIoAccounttingInrt.setAtowtp(E_ATOWTP.DP);
	          cplIoAccounttingInrt.setTrsqtp(E_ATSQTP.ACCOUNT);
	          cplIoAccounttingInrt.setBltype(BaseEnumType.E_BLTYPE.PYIN);

	          if (CommUtil.equals("1", KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%", true).getParm_value1())) {
	            KnpParameter para = SysUtil.getInstance(KnpParameter.class);
	            para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%", "%", true);
	            cplIoAccounttingInrt.setTranms(para.getParm_value1());
	          }

	          ((IoAccountSvcType)SysUtil.getInstance(IoAccountSvcType.class)).ioAccountting(cplIoAccounttingInrt);
	        }

	      }

	    }
	    
	    //积数调整
	    BigDecimal acmlbl1 = BigDecimal.ZERO;
	   
	    ibdays = ibdays - lcDays;  //积数调整天数，应为冲账当天与原交易日相隔天数减去利息调整天数
	    acmlbl1 = tranam.multiply(new BigDecimal(ibdays));
	    
	    if(rebuwo == E_REBUWA.R){
	    	tranam = tranam.negate();
	    }

	    IoDpTable.IoDpKnbAcin cplAcin1 = ((IoDpSrvQryTableInfo)CommTools.getRemoteInstance(IoDpSrvQryTableInfo.class)).getKnbAcinOdb1(ioDpKnaAcct.getAcctno(), Boolean.valueOf(true));
	    if (((BaseEnumType.E_AMNTCD.CR == amntcd) && (CommUtil.compare(tranam, BigDecimal.ZERO) > 0)) || ((BaseEnumType.E_AMNTCD.DR == amntcd) && (CommUtil.compare(tranam, BigDecimal.ZERO) < 0)))
	    {
	      cplAcin1.setCutmam(cplAcin1.getCutmam().add(acmlbl1));
	    }
	    else cplAcin1.setCutmam(cplAcin1.getCutmam().subtract(acmlbl1));

	    ((IoDpSrvQryTableInfo)CommTools.getRemoteInstance(IoDpSrvQryTableInfo.class)).updateKnbAcinOdb1(cplAcin1);


	    return Integer.valueOf(lcDays);
		
	}

	
}

