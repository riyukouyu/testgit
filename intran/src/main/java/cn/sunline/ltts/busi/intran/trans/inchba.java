package cn.sunline.ltts.busi.intran.trans;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.busi.dp.errors.InModuleError;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.in.serviceimpl.IoInQueryImpl;
import cn.sunline.ltts.busi.in.tables.In.KnsPaya;
import cn.sunline.ltts.busi.in.tables.In.KnsPayaDao;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PayaListInfo;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PayaListSecond;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PaydListInfo;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PaydListSecond;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaPayaDetail;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaPaydDetail;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INPTSR;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ISPAYA;


public class inchba {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(inchba.class);
	
	public static void BeforeTransferCheck( final cn.sunline.ltts.busi.intran.trans.intf.Inchba.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Inchba.Property property,  final cn.sunline.ltts.busi.intran.trans.intf.Inchba.Output Output){
		
		//电子账户存入支取服务中已集成冻结信息检查，且本交易为错账时调账所用，暂不考虑额度检查
		
		if(CommUtil.isNull(input.getAcctno()) || CommUtil.isNull(input.getToacct())){
			throw InModuleError.InAccount.IN010020();
		}
		
		if(CommUtil.equals(input.getTranam(),BigDecimal.ZERO) || CommUtil.isNull(input.getTranam())){
			//throw InError.comm.E0003("交易金额["+input.getTranam()+"]不能为空或为0");
			throw InModuleError.InAccount.IN010021();
		}
		
		if(CommUtil.equals(input.getAcctno(), input.getToacct())){
			throw InModuleError.InAccount.IN010022();
		}
		
		if(CommUtil.isNull(input.getDscrtx())){
			throw InModuleError.InAccount.IN010023();
		}
		
		if(CommUtil.isNull(input.getCrcycd())){
			throw InModuleError.InAccount.IN010024();
		}
		
//		//初始借方账号信息
//		IoCaTypGenEAccountInfo.QryFacctInfo info = SysUtil.getInstance(IoCaTypGenEAccountInfo.QryFacctInfo.class);
//		//初始贷方账号信息
//		IoCaTypGenEAccountInfo.QryFacctInfo info1 = SysUtil.getInstance(IoCaTypGenEAccountInfo.QryFacctInfo.class);
		
		//检查交易金额大于零标志
		if(input.getQuotfs() == E_YES___.YES && (CommUtil.compare(input.getTranam(),BigDecimal.ZERO)<0)){
			throw InError.comm.E0004();
		}
		
		//根据账户规则判断账户是内部户还是电子账户
		E_ACCTROUTTYPE routeType1 = ApAcctRoutTools.getRouteType(input.getAcctno());
		if(routeType1 == E_ACCTROUTTYPE.INSIDE){
			property.setIsinac(E_YES___.YES);
		}else{
			property.setIsinac(E_YES___.NO);
		}
		E_ACCTROUTTYPE routeType2 = ApAcctRoutTools.getRouteType(input.getToacct());
		if(routeType2 == E_ACCTROUTTYPE.INSIDE ){
			property.setToisin(E_YES___.YES);
		} else {
			property.setToisin(E_YES___.NO);
		}
        if (property.getIsinac() == property.getToisin() && property.getToisin() == E_YES___.NO) {
            throw InModuleError.InAccount.IN020011();
        }
		String brchno ="";
		String tobrch ="";
		String custno ="";
		String tocuno ="";
		E_ACCATP accatp = null; //账户1类型
		E_ACCATP toactp = null; //账户2类型
		
		
		if(property.getIsinac() == E_YES___.YES){
			bizlog.debug("账号1为内部户，处理开始------");
			
			IoInacInfo acct = SysUtil.getInstance(IoInQueryImpl.class).InacInfoQuery(input.getAcctno());
			if(CommUtil.isNull(acct)){
				throw InModuleError.InAccount.IN020012(input.getAcctno());
			}
			property.setAcctna(acct.getAcctna());
			
			if(acct.getIspaya() ==E_ISPAYA._0){
				if(CommUtil.isNotNull(input.getPayaListInfoFirst()) || CommUtil.isNotNull(input.getPaydListInfoFirst())){
					throw InModuleError.InAccount.IN030018(input.getAcctno());
				}
			}
			
			if(acct.getIspaya() ==E_ISPAYA._1){
				
				BigDecimal payaAmount = BigDecimal.ZERO; //账号1总挂账金额
				BigDecimal paydAmount = BigDecimal.ZERO; //账号1总销账金额
				
				if(CommUtil.isNull(input.getPayaListInfoFirst()) && CommUtil.isNull(input.getPaydListInfoFirst())){
					throw InModuleError.InAccount.IN030019(input.getAcctno());
				}
				
				if(CommUtil.isNotNull(input.getPayaListInfoFirst())){
					Options<IaPayaDetail> payaListFirst = new DefaultOptions<IaPayaDetail>();
					             
					for(PayaListInfo payaListInfo : input.getPayaListInfoFirst()){
						IaPayaDetail paya = SysUtil.getInstance(IaPayaDetail.class);
						
						if(CommUtil.equals(payaListInfo.getPayamn(), BigDecimal.ZERO)){
							throw InModuleError.InAccount.IN030020();
						}
						
						if(CommUtil.isNull(payaListInfo.getToacno())){
							throw InModuleError.InAccount.IN030021();
						}
						
						if(CommUtil.isNull(payaListInfo.getPayabr())){
							throw InModuleError.InAccount.IN030022();
						}
						
						if(!CommUtil.equals(payaListInfo.getToacno(), input.getToacct())){
							throw InModuleError.InAccount.IN030023();
						}
						
						if(!CommUtil.equals(payaListInfo.getPayabr(), CommTools.getBaseRunEnvs().getTrxn_branch())){
							throw InModuleError.InAccount.IN030024();
						}
						
						payaAmount = payaAmount.add(payaListInfo.getPayamn()); //挂账明细1挂账金额求和
						
						paya.setPayaac(input.getAcctno());
						paya.setPayabr(payaListInfo.getPayabr());
						paya.setPayamn(payaListInfo.getPayamn());
						paya.setToacno(input.getToacct());
						payaListFirst.add(paya);
					}
					
					property.setPayaListFirst(payaListFirst);
				}
				
				if(CommUtil.isNotNull(input.getPaydListInfoFirst())){
					
					Options<IaPaydDetail> paydListFirst = new DefaultOptions<IaPaydDetail>();
					
					for(PaydListInfo paydListInfo : input.getPaydListInfoFirst()){
						
						if(CommUtil.equals(paydListInfo.getCharam(), BigDecimal.ZERO)){
							throw InModuleError.InAccount.IN030025();
						}
						
						if(CommUtil.isNull(paydListInfo.getCharsq())){
							throw InModuleError.InAccount.IN030026();
						}
						
						KnsPaya tblKnsPaya = KnsPayaDao.selectOne_kns_paya_odx1(paydListInfo.getCharsq(), true);
						
						if(!CommUtil.equals(paydListInfo.getBalanc(), tblKnsPaya.getRsdlmn().subtract(paydListInfo.getCharam()))){
							throw InModuleError.InAccount.IN030027();
						}
						
						paydAmount = paydAmount.add(paydListInfo.getCharam()); //销账金额求和
					
						//销账明细1设值
						IaPaydDetail payd = SysUtil.getInstance(IaPaydDetail.class);
						payd.setPaydac(input.getAcctno());
						payd.setPaydmn(paydListInfo.getCharam());
						payd.setPrpysq(paydListInfo.getCharsq());
						payd.setRsdlmn(paydListInfo.getBalanc());
						payd.setTotlmn(tblKnsPaya.getRsdlmn());
						paydListFirst.add(payd);
					}
					
					property.setPaydListFirst(paydListFirst);
				}
				
				//金额检查
				if(!CommUtil.equals(input.getTranam(), payaAmount.add(paydAmount))){
					throw InModuleError.InAccount.IN030028();
				}
			}
			
			brchno=acct.getBrchno();
			property.setInptsr(E_INPTSR.GL03);//调账交易放开对记账控制的检查
			//Property.setSubsac(acct.getSubsac());
		} else {
			
			bizlog.debug("账号1为电子账户，处理开始------");
						
			IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb2(input.getAcctno(), false);
			
			if (CommUtil.isNull(tblKnaAcdc) || tblKnaAcdc.getStatus() == E_DPACST.CLOSE) {
				throw InModuleError.InAccount.IN030029(input.getAcctno());
			}
			
			accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac()); //账户1类型
			
			E_ACSETP acsetp = E_ACSETP.SA; //子账户区分类型
			if(accatp == E_ACCATP.WALLET){
				
				acsetp = E_ACSETP.MA; //账户分类为钱包账户时，子账户区分类型为钱包账户
			}
            /*
             * 20190911 xuxiaoli 通过电子账户和子账号获取子账号信息
             */
			//通过电子账号获取负债账号
            IoDpKnaAcct ioDpKnaAcct =
                SysUtil.getInstance(DpAcctSvcType.class).getKnaAcctSub(tblKnaAcdc.getCustac(), input.getDeptno()); // 获取acctno
            if (ioDpKnaAcct.getOnlnbl().compareTo(input.getTranam()) < 0) {
            	throw InModuleError.InAccount.IN030030(input.getDeptno());
            }
			property.setCustac(tblKnaAcdc.getCustac());    
			property.setCardno(input.getAcctno());
			property.setCustna(ioDpKnaAcct.getAcctna());
			property.setAcctna(ioDpKnaAcct.getAcctna());
			property.setDeptno(ioDpKnaAcct.getAcctno());
			custno = ioDpKnaAcct.getCustno();
		    brchno =ioDpKnaAcct.getBrchno();
		    
		}
		
		bizlog.debug("--------账号1处理结束");
		
		if(property.getToisin() == E_YES___.YES){
			
			bizlog.debug("账号2为内部户，处理开始------");
			
			IoInacInfo acct = SysUtil.getInstance(IoInQueryImpl.class).InacInfoQuery(input.getToacct());
			if(CommUtil.isNull(acct)){
				throw InModuleError.InAccount.IN030031(input.getToacct());
			}
			property.setToacctna(acct.getAcctna());
			
			if(acct.getIspaya() ==E_ISPAYA._1){
				BigDecimal payaAmount = BigDecimal.ZERO; //账号1总挂账金额
				BigDecimal paydAmount = BigDecimal.ZERO; //账号1总销账金额
				if(CommUtil.isNull(input.getPayaListInfoSecond()) && CommUtil.isNull(input.getPaydListInfoSecond())){
					throw InModuleError.InAccount.IN030032(input.getToacct());
				}
				
				if(CommUtil.isNotNull(input.getPayaListInfoSecond())){
					
					Options<IaPayaDetail> payaSecondOptions = new DefaultOptions<IaPayaDetail>();
					for(PayaListSecond payaListSecond  : input.getPayaListInfoSecond()){
						
						IaPayaDetail paya = SysUtil.getInstance(IaPayaDetail.class);
						
						if(CommUtil.equals(payaListSecond.getPayamt(), BigDecimal.ZERO)){
							throw InModuleError.InAccount.IN030033();
						}
						
						if(CommUtil.isNull(payaListSecond.getToacnb())){
							throw InModuleError.InAccount.IN030034();
						}
						
						if(CommUtil.isNull(payaListSecond.getPayabh())){
							throw InModuleError.InAccount.IN030035();
						}
						
						if(!CommUtil.equals(payaListSecond.getToacnb(), input.getAcctno())){
							throw InModuleError.InAccount.IN030036();
						}
						
						if(!CommUtil.equals(payaListSecond.getPayabh(), CommTools.getBaseRunEnvs().getTrxn_branch())){
							throw InModuleError.InAccount.IN030037();
						}
						
						payaAmount = payaAmount.add(payaListSecond.getPayamt()); //挂账明细2挂账金额求和
						paya.setPayaac(input.getToacct());
						paya.setPayabr(payaListSecond.getPayabh());
						paya.setPayamn(payaListSecond.getPayamt());
						paya.setToacno(input.getAcctno());
						payaSecondOptions.add(paya);
					}
					
					property.setPayaListSecond(payaSecondOptions);
					
				}
				
				if(CommUtil.isNotNull(input.getPaydListInfoSecond())){
					
					Options<IaPaydDetail> paydSecondOptions = new DefaultOptions<IaPaydDetail>();
					
					for(PaydListSecond paydListSecond : input.getPaydListInfoSecond()){
						if(CommUtil.equals(paydListSecond.getCharan(), BigDecimal.ZERO)){
							throw InModuleError.InAccount.IN030038();
						}
						
						if(CommUtil.isNull(paydListSecond.getCharse())){
							throw InModuleError.InAccount.IN030039();
						}
						
						KnsPaya tblKnsPaya = KnsPayaDao.selectOne_kns_paya_odx1(paydListSecond.getCharse(), true);
						
						if(!CommUtil.equals(paydListSecond.getBalane(), tblKnsPaya.getRsdlmn().subtract(paydListSecond.getCharan()))){
							throw InModuleError.InAccount.IN030040();
						}
						
						paydAmount = paydAmount.add(paydListSecond.getCharan()); //销账金额求和
					
						//销账明细1设值
						IaPaydDetail payd = SysUtil.getInstance(IaPaydDetail.class);
						payd.setPaydac(input.getToacct());
						payd.setPaydmn(paydListSecond.getCharan());
						payd.setPrpysq(paydListSecond.getCharse());
						payd.setRsdlmn(paydListSecond.getBalane());
						payd.setTotlmn(tblKnsPaya.getRsdlmn());
						paydSecondOptions.add(payd);
					}
					
					property.setPaydListSecond(paydSecondOptions);
					
				}
				
				//金额检查
				if(!CommUtil.equals(input.getTranam(), payaAmount.add(paydAmount))){
					throw InModuleError.InAccount.IN030041();
				}
			}
			
			tobrch=acct.getBrchno();
		} else {
			
			bizlog.debug("账号2为电子账号，处理开始------");
			
			IoCaKnaAcdc tblKnaAcdc2 = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb2(input.getToacct(), false);  //根据卡号获取电子账号
			
			if (CommUtil.isNull(tblKnaAcdc2) || tblKnaAcdc2.getStatus() == E_DPACST.CLOSE) {
				throw InModuleError.InAccount.IN030042(input.getToacct());
			}
			
			toactp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc2.getCustac()); //账户1类型
			E_ACSETP toacsetp = E_ACSETP.SA; //子账户区分类型
			if(toactp == E_ACCATP.WALLET){
				
				toacsetp = E_ACSETP.MA; //账户分类为钱包账户时，子账户区分类型为钱包账户
			}
            /*
             * 20190911 xuxiaoli 通过电子账户和子账号获取子账号信息
             */
			//通过电子账号获取负债账号
            IoDpKnaAcct ioDpKnaAcct2 =
                SysUtil.getInstance(DpAcctSvcType.class).getKnaAcctSub(tblKnaAcdc2.getCustac(), input.getTodeptno()); // 获取acctno
			property.setTocustac(tblKnaAcdc2.getCustac());
			property.setTocardno(input.getToacct());
			property.setTocustna(ioDpKnaAcct2.getAcctna());
			property.setTodeptno(ioDpKnaAcct2.getAcctno());
			tobrch =ioDpKnaAcct2.getBrchno();	
			tocuno = ioDpKnaAcct2.getCustno();
			property.setToacctna(ioDpKnaAcct2.getAcctna());
		}
		
		bizlog.debug("---------账号2处理结束");

		if(!CommUtil.equals(brchno, tobrch)){
			property.setCrosbr(E_YES___.YES);
		}
        property.setDetlsq(Long.parseLong(CoreUtil.nextValue("detlsq")));
	}

	public static void AfterTransferCheck( final cn.sunline.ltts.busi.intran.trans.intf.Inchba.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Inchba.Property property,  final cn.sunline.ltts.busi.intran.trans.intf.Inchba.Output output){
		E_YES___ crosbr=property.getCrosbr();
		E_CLACTP clactp=null;
		if(crosbr==E_YES___.YES){
			clactp=E_CLACTP._10;
		}
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),clactp);
	
		//电子状态更新
		if(property.getToisin() == E_YES___.NO){
			
			E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(property.getTocustac());
			
			if(cuacst == E_CUACST.DORMANT || cuacst == E_CUACST.NOACTIVE){
				
				IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
				cplDimeInfo.setCustac(property.getTocustac());
				cplDimeInfo.setDime01(cuacst.getValue()); //维度1 更新前状态
				SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);
			}
		}
		//add 20170302 songlw 增加流水输出
		output.setMtrasq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setPckgsq(CommTools.getBaseRunEnvs().getTrxn_seq());
		output.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq());//getBstrsq() to getBusisq() 19/4/17 rambo
	}
}
