package cn.sunline.ltts.busi.intran.trans;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InTranOutDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.KnsCmbk;
import cn.sunline.ltts.busi.in.tables.In.KnsPaya;
import cn.sunline.ltts.busi.in.tables.In.KnsPayaDao;
import cn.sunline.ltts.busi.in.tables.In.KnsPayd;
import cn.sunline.ltts.busi.in.tables.In.KnsPaydDao;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PayaFirst;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PayaSecond;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PaydFirst;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PaydSecond;
import cn.sunline.ltts.busi.intran.trans.intf.Iavccl.Input;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_INTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_IAVCTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_KPACFG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYATP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
/**
 * 
 * @author wuzhixiang
 * @date 13,Dec 11:00 pm
 * 内部账转内部账跨法人复核
 */

public class iavccl {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(iavccl.class);
	
	public static void befroeCheck( final cn.sunline.ltts.busi.intran.trans.intf.Iavccl.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Iavccl.Output output){
		
		bizlog.debug("==========内部账转内部账跨法人复核检查开始==========");
	
		basicInputChk(input);//基础字段输入检查
	    
		chkInputChk(input);//验证输入信息
		
		bizlog.debug("==========内部账转内部账跨法人复核检查结束==========");
}

	private static void chkInputChk(Input input) {
		//校验转出账号 
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();	
		KnsCmbk knsCmbk = InTranOutDao.selKnsCmbkByOtacnoAndAcstno(input.getAcstno(), trandt, input.getOtacno(), true);		
		if(!CommUtil.equals(input.getAcstno(), knsCmbk.getAcstno())){
			throw InError.comm.E0003("套号不一致，请核对！");
		}		
		if(!CommUtil.equals(input.getAcctbr(),knsCmbk.getAcctbr())){
			throw InError.comm.E0003("记账机构不一致，请核对！");
		}
		if(!CommUtil.equals(input.getOtacno(),knsCmbk.getOtacno())){
			throw InError.comm.E0003("转出内部户账号不一致，请核对！");
		}
		if(!CommUtil.equals(input.getOtacna(),knsCmbk.getOtacna())){
			throw InError.comm.E0003("转出户名不一致，请核对！");
		}
		if(!CommUtil.equals(input.getOtcrcd(),knsCmbk.getCrcycd())){
			throw InError.comm.E0003("转出币种不一致，请核对！");
		}
		if(input.getOtamcd()!= knsCmbk.getOtamcd()){
			throw InError.comm.E0003("转出借贷标志不一致，请核对！");
		}
		if(input.getIntype()!=knsCmbk.getIntype()){
			throw InError.comm.E0003("跨机构转入不一致，请核对！");
		}
		if(input.getIavctp() != knsCmbk.getIavctp()){
			throw InError.comm.E0003("传票类型不一致，请核对！");
		}		
		if(CommUtil.compare(input.getTranam(), knsCmbk.getTranam()) != 0){
			throw InError.comm.E0003("交易金额不一致，请核对！");
		}
		//校验转出账号
		KnsCmbk knsCmbk2 = InTranOutDao.selKnsCmbkByOtacnoAndAcstno(input.getAcstno(), trandt, input.getInacno(), true);	
		if(!CommUtil.equals(input.getAcstno(), knsCmbk2.getAcstno())){
			throw InError.comm.E0003("套号不一致，请核对！");
		}		
		if(!CommUtil.equals(input.getAcctbr(),knsCmbk2.getAcctbr())){
			throw InError.comm.E0003("记账机构不一致，请核对！");
		}
		if(!CommUtil.equals(input.getInacno(),knsCmbk2.getOtacno())){
			throw InError.comm.E0003("转出内部户账号不一致，请核对！");
		}
		if(!CommUtil.equals(input.getInacna(),knsCmbk2.getOtacna())){
			throw InError.comm.E0003("转出户名不一致，请核对！");
		}
		if(!CommUtil.equals(input.getOtcrcd(),knsCmbk.getCrcycd())){
			throw InError.comm.E0003("转出币种不一致，请核对！");
		}
		if(input.getOtamcd()!= knsCmbk.getOtamcd()){
			throw InError.comm.E0003("转出借贷标志不一致，请核对！");
		}
		if(input.getIntype()!=knsCmbk.getIntype()){
			throw InError.comm.E0003("跨机构转入不一致，请核对！");
		}
		if(input.getIavctp() != knsCmbk.getIavctp()){
			throw InError.comm.E0003("传票类型不一致，请核对！");
		}		
		if(CommUtil.compare(input.getTranam(), knsCmbk.getTranam()) != 0){
			throw InError.comm.E0003("交易金额不一致，请核对！");
		}
		 //转入转出挂账复核检查
	    if(input.getOtpatp() == E_PAYATP._1 || input.getInpatp() == E_PAYATP._1){
	    	
	    	inputPayaChk(input);
	    }

	    //转入转出复核销账检查
	    if(input.getOtpatp() == E_PAYATP._2 || input.getInpatp() == E_PAYATP._2){
	    	
	    	inputPaydChk(input);
	    }
	
	}
	 //转入转出挂账检查
	private static void inputPayaChk(Input input) {
		//挂账明细复核检查
	    bizlog.debug("挂账明细检测开始==========");
		//转出账户挂账检查
	    
	    if(input.getOtpatp() == E_PAYATP._1 ){
	    	
	    	for(PayaFirst payaFirst : input.getPayaListInfoFirst()){
				
				if(CommUtil.isNull(payaFirst.getToacno())){
					throw InError.comm.E0003("挂账明细中，对方账号不能为空");
				}
	    		
	    		if(CommUtil.isNull(payaFirst.getToacna())){
					throw InError.comm.E0003("挂账明细中，对方户名不能为空");
				}
	    		
	    		if(CommUtil.isNull(payaFirst.getPayamn())){
					throw InError.comm.E0003("挂账明细中，挂账金额不能为空");
				}
	    		
	    		if(CommUtil.isNull(payaFirst.getPayabr())){
					throw InError.comm.E0003("挂账明细中，挂账机构不能为空");
				}
	    		
	    		if(CommUtil.isNull(payaFirst.getPayasq())){
					throw InError.comm.E0003("复核时，挂账序号不能为空");
				}
	    		
	    		KnsPaya tblKnsPaya = KnsPayaDao.selectOne_kns_paya_odx1(payaFirst.getPayasq(), true);
	    		
	    		if(!CommUtil.equals(tblKnsPaya.getAcstno(), input.getAcstno())){
	    			throw InError.comm.E0003("挂账序号" + payaFirst.getPayasq() + "对应挂账记录非本套号内容，请核查！");
	    		}
	    		
	    		if(!CommUtil.equals(payaFirst.getToacno(), tblKnsPaya.getToacct())){
	    			throw InError.comm.E0003("挂账序号" + payaFirst.getPayasq() + "所在挂账记录对方账号与原录入不符！");
	    		}
	    		
	    		if(!CommUtil.equals(payaFirst.getToacna(), tblKnsPaya.getToacna())){
	    			throw InError.comm.E0003("挂账序号" + payaFirst.getPayasq() + "所在挂账记录对方户名与原录入不符！");
	    		}
	    		
	    		if(!CommUtil.equals(payaFirst.getPayamn(), tblKnsPaya.getPayamn())){
	    			throw InError.comm.E0003("挂账序号" + payaFirst.getPayasq() + "所在挂账记录挂账金额与原录入不符！");
	    		}
	    		
	    		if(!CommUtil.equals(payaFirst.getPayabr(), tblKnsPaya.getPayabr())){
	    			throw InError.comm.E0003("挂账序号" + payaFirst.getPayasq() + "所在挂账记录挂账机构与原录入不符！");
	    		}

	    		if(!CommUtil.equals(input.getOtacno(), tblKnsPaya.getPayaac())){
	    			throw InError.comm.E0003("挂账序号为" + payaFirst.getPayasq() + "的挂账明细中，对应传票账号与挂账账号不符，请核查！");
	    		}
	    		
			}
	    }
	    
	    if(input.getInpatp() == E_PAYATP._1){
	    	//转入账户挂账检查
			for(PayaSecond payaSecond : input.getPayaListInfoSecond()){
				
				if(CommUtil.isNull(payaSecond.getToacnb())){
					throw InError.comm.E0003("挂账明细中，对方账号不能为空");
				}
	    		
	    		if(CommUtil.isNull(payaSecond.getToacnm())){
					throw InError.comm.E0003("挂账明细中，对方户名不能为空");
				}
	    		
	    		if(CommUtil.isNull(payaSecond.getPayamt())){
					throw InError.comm.E0003("挂账明细中，挂账金额不能为空");
				}
	    		
	    		if(CommUtil.isNull(payaSecond.getPayabh())){
					throw InError.comm.E0003("挂账明细中，挂账机构不能为空");
				}
	    		
	    		if(CommUtil.isNull(payaSecond.getPayase())){
					throw InError.comm.E0003("复核时，挂账序号不能为空");
				}

	    		//KnsPaya tblKnsPaya = KnsPayaDao.selectOne_kns_paya_odx1(payaSecond.getPayase(), true);
	    		KnsPaya tblKnsPaya = InTranOutDao.selKnsPayaByAcstnoAndPayasq(input.getAcstno(), payaSecond.getPayase(), CommTools.getBaseRunEnvs().getTrxn_date(), true);  
	    		if(!CommUtil.equals(tblKnsPaya.getAcstno(), input.getAcstno())){
	    			throw InError.comm.E0003("挂账序号" + payaSecond.getPayase() + "对应挂账记录非本套号内容，请核查！");
	    		}
	    		
	    		if(!CommUtil.equals(payaSecond.getToacnb(), tblKnsPaya.getToacct())){
	    			throw InError.comm.E0003("挂账序号" + payaSecond.getPayase() + "所在挂账记录对方账号与原录入不符！");
	    		}
	    		
	    		if(!CommUtil.equals(payaSecond.getToacnm(), tblKnsPaya.getToacna())){
	    			throw InError.comm.E0003("挂账序号" + payaSecond.getPayase() + "所在挂账记录对方户名与原录入不符！");
	    		}
	    		
	    		if(!CommUtil.equals(payaSecond.getPayamt(), tblKnsPaya.getPayamn())){
	    			throw InError.comm.E0003("挂账序号" + payaSecond.getPayase() + "所在挂账记录挂账金额与原录入不符！");
	    		}
	    		
	    		if(!CommUtil.equals(payaSecond.getPayabh(), tblKnsPaya.getPayabr())){
	    			throw InError.comm.E0003("挂账序号" + payaSecond.getPayase() + "所在挂账记录挂账机构与原录入不符！");
	    		}

	    		if(!CommUtil.equals(input.getInacno(), payaSecond.getPayaae())){
	    			throw InError.comm.E0003("挂账序号为" + payaSecond.getPayase() + "的挂账明细中，对应传票账号与挂账账号不符，请核查！");
	    		}
			}
			 
	    }
		 bizlog.debug("挂账明细检测结束==========");
		
	}
	
    //转入转出销账检查
	private static void inputPaydChk(Input input) {
		//销账明细检查
		bizlog.debug("销账明细检测开始==========");
		//转出账户销账明细检查
		BigDecimal amount = new BigDecimal(0);
		 if(input.getOtpatp() == E_PAYATP._2){
			 
			 for(PaydFirst paydFirst : input.getPaydListInfoFirst()){
		    		
		    		if(CommUtil.isNull(paydFirst.getPaydsq())){
						throw InError.comm.E0003("复核时，销账序号不能为空");
					}
		    		
		    		if(CommUtil.isNull(paydFirst.getCharsq())){
						throw InError.comm.E0003("销账明细中，待销账流水不能为空");
					}
			    	
		    		if(CommUtil.equals(paydFirst.getTotlmn(), BigDecimal.ZERO)){
						throw InError.comm.E0003("销账明细中，原未销金额不能为空或零");
					}
			    	
		    		if(CommUtil.compare(paydFirst.getCharam(), BigDecimal.ZERO) <= 0){
		    			throw InError.comm.E0003("销账明细中，销账金额大于零");
		    		}
		    		
		    		if(!CommUtil.equals(paydFirst.getBalanc(), paydFirst.getTotlmn().subtract(paydFirst.getCharam()))){
		    			throw InError.comm.E0003("销账明细中，剩余挂账金额应为原未销金额与本次销账金额之差");
		    		}
		    		if(CommUtil.compare(paydFirst.getCharam(), paydFirst.getTotlmn()) > 0){
		    			throw InError.comm.E0003("销账明细，本次销账金额["+paydFirst.getCharam()+"]大于原未销金额["+paydFirst.getTotlmn()+"],销账失败！");
		    		}
		    		KnsPayd tblKnsPayd = KnsPaydDao.selectOne_kns_payd_odx1(paydFirst.getPaydsq(), true);
		    		
		    		if(!CommUtil.equals(tblKnsPayd.getAcstno(), input.getAcstno())){
		    			throw InError.comm.E0003("销账序号" + paydFirst.getPaydsq() + "对应销账记录非本套号内容，请核查！");
		    		}
		    		
		    	/*	if(!CommUtil.equals(paydFirst.getPaydac(), tblKnsPayd.getPayasq())){
		    			throw InError.comm.E0003("销账序号" + paydFirst.getPaydsq() + "所在销账记录待销账流水与原录入不符！");
		    		}
		    		*/
		    		if(!CommUtil.equals(paydFirst.getTotlmn(), tblKnsPayd.getTotlmn())){
		    			throw InError.comm.E0003("销账序号" + paydFirst.getPaydsq() + "所在销账记录原未销金额与原录入不符！");
		    		}
		    		
		    		if(!CommUtil.equals(paydFirst.getCharam(), tblKnsPayd.getPayamn())){
		    			throw InError.comm.E0003("销账序号" + paydFirst.getPaydsq() + "所在销账记录销账金额与原录入不符！");
		    		}
		    		
		    		if(!CommUtil.equals(paydFirst.getBalanc(), tblKnsPayd.getRsdlmn())){
		    			throw InError.comm.E0003("销账序号" + paydFirst.getPaydsq() + "所在销账记录剩余挂账金额与原录入不符！");
		    		}
		    		if(!CommUtil.equals(input.getOtacno(), tblKnsPayd.getPaydac())){
		    				throw InError.comm.E0003("销账序号为" + paydFirst.getPaydsq() + "的销账明细中，对应传票账号与挂账账号不符，请核查！");
		    		}
		    		amount = amount.add(paydFirst.getCharam());    			
		    }

			 if(CommUtil.compare(amount, input.getTranam()) != 0){
				 
				 throw InError.comm.E0003("合计销账金额不等于交易金额！");
			 }
		}
    	
	if(input.getInpatp() == E_PAYATP._2){
		
		BigDecimal amount2 = new BigDecimal(0);
		//转入销账复核检查
    	for(PaydSecond paydSecond : input.getPaydListInfoSecond()){
    		
    		if(CommUtil.isNull(paydSecond.getPaydse())){
				throw InError.comm.E0003("复核时，销账序号不能为空");
			}
    		
    		if(CommUtil.isNull(paydSecond.getCharse())){
				throw InError.comm.E0003("销账明细中，待销账流水不能为空");
			}
	    	
    		if(CommUtil.equals(paydSecond.getTotlmt(), BigDecimal.ZERO)){
				throw InError.comm.E0003("销账明细中，原未销金额不能为空或零");
			}
	    	
    		if(CommUtil.compare(paydSecond.getCharan(), BigDecimal.ZERO) <= 0){
    			throw InError.comm.E0003("销账明细中，销账金额大于零");
    		}
    		
    		if(!CommUtil.equals(paydSecond.getBalane(), paydSecond.getTotlmt().subtract(paydSecond.getCharan()))){
    			throw InError.comm.E0003("销账明细中，剩余挂账金额应为原未销金额与本次销账金额之差");
    		}
    		if(CommUtil.compare(paydSecond.getCharan(), paydSecond.getTotlmt()) > 0){
    			throw InError.comm.E0003("销账明细，本次销账金额["+paydSecond.getCharan()+"]大于原未销金额["+paydSecond.getTotlmt()+"],销账失败！");
    		}	

    		//KnsPayd tblKnsPayd = KnsPaydDao.selectOne_kns_payd_odx1(paydSecond.getPaydse(), true);
    		KnsPayd tblKnsPayd = InTranOutDao.selKnsPaydByPasq(input.getAcstno(), paydSecond.getPaydse(), CommTools.getBaseRunEnvs().getTrxn_date(), true);

    		if(!CommUtil.equals(tblKnsPayd.getAcstno(), input.getAcstno())){
    			throw InError.comm.E0003("销账序号" + paydSecond.getPaydse() + "对应销账记录非本套号内容，请核查！");
    		}
    		
    		/*if(!CommUtil.equals(paydSecond.getPaydae(), tblKnsPayd.getPayasq())){
    			throw InError.comm.E0003("销账序号" + paydSecond.getPaydse() + "所在销账记录待销账流水与原录入不符！");
    		}
    		*/
    		if(!CommUtil.equals(paydSecond.getTotlmt(), tblKnsPayd.getTotlmn())){
    			throw InError.comm.E0003("销账序号" + paydSecond.getPaydse() + "所在销账记录原未销金额与原录入不符！");
    		}
    		
    		if(!CommUtil.equals(paydSecond.getCharan(), tblKnsPayd.getPayamn())){
    			throw InError.comm.E0003("销账序号" + paydSecond.getPaydse() + "所在销账记录销账金额与原录入不符！");
    		}
    		
    		if(!CommUtil.equals(paydSecond.getBalane(), tblKnsPayd.getRsdlmn())){
    			throw InError.comm.E0003("销账序号" + paydSecond.getPaydse() + "所在销账记录剩余挂账金额与原录入不符！");
    		}
    		if(!CommUtil.equals(input.getOtacno(), tblKnsPayd.getPaydac())){
    				throw InError.comm.E0003("销账序号为" + paydSecond.getPaydse() + "的销账明细中，对应传票账号与挂账账号不符，请核查！");
    		}
    		amount2 = amount2.add(paydSecond.getCharan());
    		
    	}
    		if(CommUtil.compare(amount2, input.getTranam()) != 0){
    			
    			throw InError.comm.E0003("合计销账金额不等于交易金额！");
		 }
    		
		}
    		bizlog.debug("销账明细检测结束==========");
	}

	private static void basicInputChk(final cn.sunline.ltts.busi.intran.trans.intf.Iavccl.Input input) {
				
		if(CommUtil.isNull(input.getAcstno())){
			throw InError.comm.E0003("输入套号不能为空");
		}
		if(input.getAcstno().length() != 11){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}		
		if(CommTools.rpxMatch("W[0-9]+", input.getAcstno()) == 1){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		if(CommUtil.isNull(input.getIavctp())){
			throw InError.comm.E0003("传票类型不能为空！");
		}
		if(input.getIavctp()!= E_IAVCTP._1){
			throw InError.comm.E0003("该交易只支持内部帐转内部帐，不支持["+input.getIavctp().getLongName()+"]!");
		}
		if(CommUtil.isNull(input.getOtacno())){
			throw InError.comm.E0003("转出账号不能为空！");
		}
		if(CommUtil.isNull(input.getOtacna())){
			throw InError.comm.E0003("转出户名不能为空！");
		}
		if(CommUtil.isNull(input.getOtamcd())){
			throw InError.comm.E0003("转出借贷标志不能为空！");
		}
		if(CommUtil.isNull(input.getOtcrcd())){
			throw InError.comm.E0003("转出币种不能为空！");
		}
		if(CommUtil.isNull(input.getTranam())){
			throw InError.comm.E0003("发生额不能为空！");
		}
		if(CommUtil.compare(input.getTranam(),BigDecimal.ZERO)<=0){
			throw InError.comm.E0003("发生额必须大于零！");
		}
	    if(!CommUtil.equals(input.getOtcrcd(),input.getIncrcd())){
	    	throw InError.comm.E0003("转入币种和转出币种类型不符，请核查！");
	    }
	    if(input.getInamcd() != E_AMNTCD.CR){
	    	throw InError.comm.E0003("转入借贷标志必须为贷方！");
	    }
	    if(input.getOtamcd() != E_AMNTCD.DR){
	    	throw InError.comm.E0003("转出借贷标志必须为借方！");
	    }
		//金额检查 当前余额+透支限额 - 发生额>= 0 否则拒绝
		if(CommUtil.isNull(input.getIntype())){
			throw InError.comm.E0003("转入类型不能为空！");
		}
		//转入类型为2结算暂收 账户机构必输  
	   	if(input.getIntype()==E_INTYPE.JSZS){
	   		if(CommUtil.isNull(input.getAcctbr())){
	   			throw InError.comm.E0003("记账机构不能为空！");
	   		}
	   	} 
	   	//转入类型为3指定账号 转入账号必输 
	   	if(input.getIntype()==E_INTYPE.ZDZH){
	   		if(CommUtil.isNull(input.getInacno())){
	   			throw InError.comm.E0003("转入账号不能为空！");
	   		}
	   	}
	   	if(CommUtil.isNull(input.getSmrytx())){
	   		throw InError.comm.E0003("摘要不能为空！");
	   	}
		//系统内转出内部户检查
		//IoInacInfo ioToacInfo = SysUtil.getInstance(IoInSrvQryTableInfo.class).selKnaGlAcctnoByAcctno(input.getOtacno(), true);
		GlKnaAcct  ioToacInfo= InQuerySqlsDao.sel_GlKnaAcct_by_acct(input.getOtacno(), true);
		if(E_INACST.CLOSED == ioToacInfo.getAcctst()){
			throw InError.comm.E0003("当前账户已销户！");
		}
		
		if(E_KPACFG._1 == ioToacInfo.getKpacfg()){
			throw InError.comm.E0003("当前账户不允许手工记账！");
		}
		
		if(!CommUtil.equals(ioToacInfo.getAcctna(), input.getOtacna())){
	        	throw InError.comm.E0003("转出户名与账户注册户名不一致！");
	    }
		
		if(!CommUtil.equals(input.getOtcrcd(),ioToacInfo.getCrcycd())){
        	throw InError.comm.E0003("当前输入币种与内部户币种类型不符，请核查！");
        }
		
		if(ioToacInfo.getIoflag()==E_IOFLAG.OUT){
			throw InError.comm.E0003("该账户不能为表外账户！");
		}
		//转入内部户账号检查
		GlKnaAcct  ioInacInfo= InQuerySqlsDao.sel_GlKnaAcct_by_acct(input.getInacno(), true);
		if(E_INACST.CLOSED == ioInacInfo.getAcctst()){
			throw InError.comm.E0003("当前账户已销户！");
		}
	
		if(E_KPACFG._1 == ioInacInfo.getKpacfg()){
			throw InError.comm.E0003("当前账户不允许手工记账！");
		}
	    if(ioInacInfo.getBlncdn()!= E_BLNCDN.C){
	    	throw InError.comm.E0003("转入账户只支持贷方余额账户！");
	    }
		if(CommUtil.equals(ioToacInfo.getBrchno(), ioInacInfo.getBrchno())){
			throw InError.comm.E0003("此交易应为跨机构交易，转出机构["+ioToacInfo.getBrchno()+"]与转入机构["+ioToacInfo.getBrchno()+"]不能相同！");
		}
	
		if(!CommUtil.equals(ioInacInfo.getAcctna(), input.getInacna())){
        	throw InError.comm.E0003("转出户名与账户注册户名不一致！");
		}
	
		if(ioToacInfo.getIoflag()==E_IOFLAG.OUT){
			throw InError.comm.E0003("该账户不能为表外账户！");
		}
	}
}
