package cn.sunline.ltts.busi.intran.trans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.in.serviceimpl.IoInQueryImpl;
import cn.sunline.ltts.busi.in.tables.In.KnsPaya;
import cn.sunline.ltts.busi.in.tables.In.KnsPayaDao;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.BorlenListIN;
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
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class borled {

	private static final BizLog bizlog = BizLogUtil.getBizLog(borled.class);
	
	public static void BeforeTransferCheck( final cn.sunline.ltts.busi.intran.trans.intf.Borled.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Borled.Property property,  final cn.sunline.ltts.busi.intran.trans.intf.Borled.Output output){
		//电子账户存入支取服务中已集成冻结信息检查，且本交易为错账时调账所用，暂不考虑额度检查
		
		if(CommUtil.isNull(input.getDrInfoList()) || CommUtil.isNull(input.getToacct())){
			throw InError.comm.E0003("转入转出账户不能为空");
		}
		
		if(CommUtil.equals(input.getTotlam(),BigDecimal.ZERO)){
			throw InError.comm.E0003("交易总金额["+input.getTotlam()+"]不能为空或为0");
		}
		
		if(CommUtil.isNull(input.getDscrtx())){
			throw InError.comm.E0003("转入描述不能为空");
		}
		
		if(CommUtil.isNull(input.getCrcycd())){
			throw InError.comm.E0003("转入币种不能为空");
		}
		
		//检查交易金额大于零标志
		if(input.getQuotfs() == E_YES___.YES && (CommUtil.compare(input.getTotlam(),BigDecimal.ZERO)<0)){
			throw InError.comm.E0004();
		}
		//校验借贷金额是否相等
		BigDecimal drtram = BigDecimal.ZERO;
		for (BorlenListIN drInfo : input.getDrInfoList()) {
			drtram = drtram.add(drInfo.getTranam());
		}
		if (CommUtil.compare(drtram, input.getTotlam()) != 0) {
			throw InError.comm.E0003("借贷金额不相等");
		}
		
		//初始化计数器和循环次数
		int counts = 0;
		int drsize = input.getDrInfoList().size();
		property.setCounts(counts);
		property.setDrsize(drsize);
		
	}

	/**
	 * 遍历借方账户记账前处理
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void getDrInfo( final cn.sunline.ltts.busi.intran.trans.intf.Borled.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Borled.Property property,  final cn.sunline.ltts.busi.intran.trans.intf.Borled.Output output){
		int count = property.getCounts();
		List<BorlenListIN> drInfoList = new ArrayList<BorlenListIN>(input.getDrInfoList());
		BorlenListIN borrowLendIn = drInfoList.get(count);
		count = count+1;
		property.setCounts(count);
		property.setAcctno(borrowLendIn.getAcctno());
		property.setTranam(borrowLendIn.getTranam());
		
		//检查交易金额大于零标志
		if(input.getQuotfs() == E_YES___.YES && (CommUtil.compare(borrowLendIn.getTranam(),BigDecimal.ZERO)<0)){
			throw InError.comm.E0004();
		}
		//根据账户规则判断账户是内部户还是电子账户
		E_ACCTROUTTYPE routeType1 = ApAcctRoutTools.getRouteType(borrowLendIn.getAcctno());
		if(routeType1 == E_ACCTROUTTYPE.INSIDE){
			property.setIsinac(E_YES___.YES);
		}else{
			property.setIsinac(E_YES___.NO);
		}
		String brchno ="";
		String custno ="";
		E_ACCATP accatp = null; //账户1账户分类
		
		//借方为内部户
		if(property.getIsinac() == E_YES___.YES){
			bizlog.debug("账号1为内部户，处理开始------第"+count+"个账户");
			
			IoInacInfo acct = SysUtil.getInstance(IoInQueryImpl.class).InacInfoQuery(borrowLendIn.getAcctno());
			if(CommUtil.isNull(acct)){
				throw InError.comm.E0003("查询内部户["+borrowLendIn.getAcctno()+"]信息失败，无对应记录");
			}
			property.setAcctna(acct.getAcctna());
			
			if(acct.getIspaya() ==E_ISPAYA._0){
				if(CommUtil.isNotNull(borrowLendIn.getPayaListInfoFirst()) || CommUtil.isNotNull(borrowLendIn.getPaydListInfoFirst())){
					throw InError.comm.E0003("内部户["+borrowLendIn.getAcctno()+"]，非挂销账管理账户");
				}
			}
			
			if(acct.getIspaya() ==E_ISPAYA._1){
				
				BigDecimal payaAmount = BigDecimal.ZERO; //账号1总挂账金额
				BigDecimal paydAmount = BigDecimal.ZERO; //账号1总销账金额
				
				if(CommUtil.isNull(borrowLendIn.getPayaListInfoFirst()) && CommUtil.isNull(borrowLendIn.getPaydListInfoFirst())){
					throw InError.comm.E0003("内部户["+borrowLendIn.getAcctno()+"]，为挂销账管理账号，请输入相关挂销账信息");
				}
				
				if(CommUtil.isNotNull(borrowLendIn.getPayaListInfoFirst())){
					Options<IaPayaDetail> payaListFirst = new DefaultOptions<IaPayaDetail>();
					             
					for(PayaListInfo payaListInfo : borrowLendIn.getPayaListInfoFirst()){
						IaPayaDetail paya = SysUtil.getInstance(IaPayaDetail.class);
						
						if(CommUtil.equals(payaListInfo.getPayamn(), BigDecimal.ZERO)){
							throw InError.comm.E0003("挂账金额不能为空或零");
						}
						
						if(CommUtil.isNull(payaListInfo.getToacno())){
							throw InError.comm.E0003("挂账明细1中对方账号不能为空");
						}
						
						if(CommUtil.isNull(payaListInfo.getPayabr())){
							throw InError.comm.E0003("挂账明细1中挂账机构不能为空");
						}
						
						if(!CommUtil.equals(payaListInfo.getToacno(), input.getToacct())){
							throw InError.comm.E0003("挂账明细1中对方账号与输入不符，请核查");
						}
						
						if(!CommUtil.equals(payaListInfo.getPayabr(), CommTools.getBaseRunEnvs().getTrxn_branch())){
							throw InError.comm.E0003("挂账明细1中挂账机构非本机构，请核查");
						}
						
						payaAmount = payaAmount.add(payaListInfo.getPayamn()); //挂账明细1挂账金额求和
						
						paya.setPayaac(borrowLendIn.getAcctno());
						paya.setPayabr(payaListInfo.getPayabr());
						paya.setPayamn(payaListInfo.getPayamn());
						paya.setToacno(input.getToacct());
						payaListFirst.add(paya);
					}
					
					property.setPayaListFirst(payaListFirst);
				}
				//销账
				if(CommUtil.isNotNull(borrowLendIn.getPaydListInfoFirst())){
					
					Options<IaPaydDetail> paydListFirst = new DefaultOptions<IaPaydDetail>();
					
					for(PaydListInfo paydListInfo : borrowLendIn.getPaydListInfoFirst()){
						
						if(CommUtil.equals(paydListInfo.getCharam(), BigDecimal.ZERO)){
							throw InError.comm.E0003("销账明细1中本次销账金额不能为空或零");
						}
						
						if(CommUtil.isNull(paydListInfo.getCharsq())){
							throw InError.comm.E0003("销账明细1中待销账流水不能为空");
						}
						
						KnsPaya tblKnsPaya = KnsPayaDao.selectOne_kns_paya_odx1(paydListInfo.getCharsq(), true);
						
						if(!CommUtil.equals(paydListInfo.getBalanc(), tblKnsPaya.getRsdlmn().subtract(paydListInfo.getCharam()))){
							throw InError.comm.E0003("销账明细1中销账余额与本次销账金额之差，不等于对应挂账明细中剩余挂账金额，请核查");
						}
						
						paydAmount = paydAmount.add(paydListInfo.getCharam()); //销账金额求和
					
						//销账明细1设值
						IaPaydDetail payd = SysUtil.getInstance(IaPaydDetail.class);
						payd.setPaydac(borrowLendIn.getAcctno());
						payd.setPaydmn(paydListInfo.getCharam());
						payd.setPrpysq(paydListInfo.getCharsq());
						payd.setRsdlmn(paydListInfo.getBalanc());
						payd.setTotlmn(tblKnsPaya.getRsdlmn());
						paydListFirst.add(payd);
					}
					
					property.setPaydListFirst(paydListFirst);
				}
				
				//金额检查
				if(!CommUtil.equals(borrowLendIn.getTranam(), payaAmount.add(paydAmount))){
					throw InError.comm.E0003("挂销账明细1总金额与交易金额不等，请核查");
				}
			}
			
			brchno=acct.getBrchno();
			property.setInptsr(E_INPTSR.GL03);//调账交易放开对记账控制的检查
			//Property.setSubsac(acct.getSubsac());
		} else { //借方为电子账户
			bizlog.debug("账号1为电子账户，处理开始------");
			
			IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb2(borrowLendIn.getAcctno(), false);
			
			if (CommUtil.isNull(tblKnaAcdc) || tblKnaAcdc.getStatus() == E_DPACST.CLOSE) {
				throw InError.comm.E0003("电子账号["+borrowLendIn.getAcctno()+"]不存在");
			}
			
			accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc.getCustac()); //账户1类型
			
			E_ACSETP acsetp = E_ACSETP.SA; //子账户区分类型
			if(accatp == E_ACCATP.WALLET){
				
				acsetp = E_ACSETP.MA; //账户分类为钱包账户时，子账户区分类型为钱包账户
			}
			//通过电子账号获取负债账号
			IoDpKnaAcct ioDpKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctSub(tblKnaAcdc.getCustac(), acsetp); //获取acctno
			property.setCustac(tblKnaAcdc.getCustac());    
			property.setCardno(borrowLendIn.getAcctno());
			property.setCustna(ioDpKnaAcct.getAcctna());
			property.setAcctna(ioDpKnaAcct.getAcctna());
			property.setDeptno(ioDpKnaAcct.getAcctno());
			custno = ioDpKnaAcct.getCustno();
		    brchno =ioDpKnaAcct.getBrchno();
		    property.setBrchno(brchno);
		    property.setCustno(custno);
		    property.setAccatp(accatp);
		}
		bizlog.debug("--------账号1处理结束,第"+count+"个账户");
		
	}
	

	public static void CrDeal( final cn.sunline.ltts.busi.intran.trans.intf.Borled.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Borled.Property property,  final cn.sunline.ltts.busi.intran.trans.intf.Borled.Output output){
		//根据账户规则判断账户是内部户还是电子账户（贷方）
		E_ACCTROUTTYPE routeType2 = ApAcctRoutTools.getRouteType(input.getToacct());
		if(routeType2 == E_ACCTROUTTYPE.INSIDE ){
			property.setToisin(E_YES___.YES);
		} else {
			property.setToisin(E_YES___.NO);
		}
		String tobrch ="";
		String tocuno ="";
		E_ACCATP toactp = null; //账户2账户分类
		
		if(property.getToisin() == E_YES___.YES){
			
			bizlog.debug("账号2为内部户，处理开始------");
			
			IoInacInfo acct = SysUtil.getInstance(IoInQueryImpl.class).InacInfoQuery(input.getToacct());
			if(CommUtil.isNull(acct)){
				throw InError.comm.E0003("查询账户["+input.getToacct()+"]失败，无对应记录");
			}
			property.setToacctna(acct.getAcctna());
			
			if(acct.getIspaya() ==E_ISPAYA._1){
				BigDecimal payaAmount = BigDecimal.ZERO; //账号2总挂账金额
				BigDecimal paydAmount = BigDecimal.ZERO; //账号2总销账金额
				if(CommUtil.isNull(input.getPayaListInfoSecond()) && CommUtil.isNull(input.getPaydListInfoSecond())){
					throw InError.comm.E0003("内部户["+input.getToacct()+"]，为挂销账管理账号，请输入相关挂销账信息");
				}
				
				if(CommUtil.isNotNull(input.getPayaListInfoSecond())){
					
					Options<IaPayaDetail> payaSecondOptions = new DefaultOptions<IaPayaDetail>();
					for(PayaListSecond payaListSecond  : input.getPayaListInfoSecond()){
						
						IaPayaDetail paya = SysUtil.getInstance(IaPayaDetail.class);
						
						if(CommUtil.equals(payaListSecond.getPayamt(), BigDecimal.ZERO)){
							throw InError.comm.E0003("挂账明细2中挂账金额不能为空或零");
						}
						
						if(CommUtil.isNull(payaListSecond.getToacnb())){
							throw InError.comm.E0003("挂账明细2中对方账号不能为空");
						}
						
						if(CommUtil.isNull(payaListSecond.getPayabh())){
							throw InError.comm.E0003("挂账明细2中挂账机构不能为空");
						}
						
						/*if(!CommUtil.equals(payaListSecond.getToacnb(), input.getAcctno())){
							throw InError.comm.E0003("挂账明细2中对方账号与输入不符，请核查");
						}*/
						boolean exitsAcctno = false;
						for (BorlenListIN borlenListIN : input.getDrInfoList()) {
							if(CommUtil.equals(payaListSecond.getToacnb(), borlenListIN.getAcctno())){
								exitsAcctno = true;
							}
						}
						if(!exitsAcctno){
							throw InError.comm.E0003("挂账明细2中对方账号与输入不符，请核查");
						}
						
						if(!CommUtil.equals(payaListSecond.getPayabh(), CommTools.getBaseRunEnvs().getTrxn_branch())){
							throw InError.comm.E0003("挂账明细2中挂账机构非本机构，请核查");
						}
						
						payaAmount = payaAmount.add(payaListSecond.getPayamt()); //挂账明细2挂账金额求和
						paya.setPayaac(input.getToacct());
						paya.setPayabr(payaListSecond.getPayabh());
						paya.setPayamn(payaListSecond.getPayamt());
						paya.setToacno(payaListSecond.getToacnb());
						payaSecondOptions.add(paya);
					}
					
					property.setPayaListSecond(payaSecondOptions);
					
				}
				
				if(CommUtil.isNotNull(input.getPaydListInfoSecond())){
					
					Options<IaPaydDetail> paydSecondOptions = new DefaultOptions<IaPaydDetail>();
					
					for(PaydListSecond paydListSecond : input.getPaydListInfoSecond()){
						if(CommUtil.equals(paydListSecond.getCharan(), BigDecimal.ZERO)){
							throw InError.comm.E0003("销账明细2中本次销账金额不能为空或零");
						}
						
						if(CommUtil.isNull(paydListSecond.getCharse())){
							throw InError.comm.E0003("销账明细2中待销账流水不能为空");
						}
						
						KnsPaya tblKnsPaya = KnsPayaDao.selectOne_kns_paya_odx1(paydListSecond.getCharse(), true);
						
						if(!CommUtil.equals(paydListSecond.getBalane(), tblKnsPaya.getRsdlmn().subtract(paydListSecond.getCharan()))){
							throw InError.comm.E0003("销账明细2中销账余额与本次销账金额之差，不等于对应挂账明细中剩余挂账金额，请核查");
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
				if(!CommUtil.equals(input.getTotlam(), payaAmount.add(paydAmount))){
					throw InError.comm.E0003("挂销账明细2总金额与交易金额不等，请核查");
				}
			}
			
			tobrch=acct.getBrchno();
		} else {
			
			bizlog.debug("账号2为电子账号，处理开始------");
			
			IoCaKnaAcdc tblKnaAcdc2 = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb2(input.getToacct(), false);  //根据卡号获取电子账号
			
			if (CommUtil.isNull(tblKnaAcdc2) || tblKnaAcdc2.getStatus() == E_DPACST.CLOSE) {
				throw InError.comm.E0003("电子账号["+input.getToacct()+"]不存在");
			}
			
			toactp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblKnaAcdc2.getCustac()); //账户1类型
			E_ACSETP toacsetp = E_ACSETP.SA; //子账户区分类型
			if(toactp == E_ACCATP.WALLET){
				
				toacsetp = E_ACSETP.MA; //账户分类为钱包账户时，子账户区分类型为钱包账户
			}
			//通过电子账号获取负债账号
			IoDpKnaAcct ioDpKnaAcct2 =  SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctSub(tblKnaAcdc2.getCustac(), toacsetp); //获取acctno
			
			property.setTocustac(tblKnaAcdc2.getCustac());
			property.setTocardno(input.getToacct());
			property.setTocustna(ioDpKnaAcct2.getAcctna());
			property.setTodeptno(ioDpKnaAcct2.getAcctno());
			tobrch = ioDpKnaAcct2.getBrchno();	
			tocuno = ioDpKnaAcct2.getCustno();
			property.setToacctna(ioDpKnaAcct2.getAcctna());
		}
		
		bizlog.debug("---------账号2处理结束");
		
		if(!CommUtil.equals(property.getBrchno(), tobrch)){
			property.setCrosbr(E_YES___.YES);
		}
		
		//弱电子账户同名校验
		if(property.getIsinac()==property.getToisin() && property.getToisin()==E_YES___.NO){
			
			if((property.getAccatp() != E_ACCATP.GLOBAL || toactp != E_ACCATP.GLOBAL) && (!CommUtil.equals(property.getCustno(),tocuno))){
				throw InError.comm.E0003("弱电子账户下，电子账户间转账需同名");
			}
		}
	}

	public static void AfterTransferCheck( final cn.sunline.ltts.busi.intran.trans.intf.Borled.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Borled.Property property,  final cn.sunline.ltts.busi.intran.trans.intf.Borled.Output output){
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
		output.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq());
	}
}





