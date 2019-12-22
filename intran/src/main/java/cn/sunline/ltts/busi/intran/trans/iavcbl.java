package cn.sunline.ltts.busi.intran.trans;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InTranOutDao;
import cn.sunline.ltts.busi.in.namedsql.WrAccRbDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.KnsCmbk;
import cn.sunline.ltts.busi.in.tables.In.KnsStrk;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PayaFirst;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PayaSecond;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PaydFirst;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PaydSecond;
import cn.sunline.ltts.busi.intran.trans.intf.Iavcbl.Input;
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


public class iavcbl {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(iavcbk.class);
	/**
	 * 
	 *  @author wuzhixiang
	 *  @date Dec 9 
	 *  内部户转内部账跨机构录入检查
	 */	
	public static void befroeCheck( final cn.sunline.ltts.busi.intran.trans.intf.Iavcbl.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Iavcbl.Output output){
	bizlog.debug("==========内部户转内部户录入检查开始==========");
	
		//内部账输入检查
	inputCheck(input);
	
	bizlog.debug("==========内部户转内部户录入检查结束==========");
}
	public static void inputCheck(final cn.sunline.ltts.busi.intran.trans.intf.Iavcbl.Input input){
		
		bizlog.debug("内部户录入检测开始==========");
		
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
		//add by wuzx 20161222 增加外币为日元时金额控制- beg
		
		BusiTools.validAmount(input.getIncrcd(), input.getTranam());
		//add by wuzx 20161222 增加外币为日元时金额控制- end
	    if(!CommUtil.equals(input.getOtcrcd(),input.getIncrcd())){
	    	throw InError.comm.E0003("转入币种和转出币种类型不符，请核查！");
	    }
	    if(input.getInamcd() != E_AMNTCD.CR){
	    	throw InError.comm.E0003("转入借贷标志必须为贷方！");
	    }
	    if(input.getOtamcd() != E_AMNTCD.DR){
	    	throw InError.comm.E0003("转出借贷标志必须为借方！");
	    }
	    if(CommUtil.isNull(input.getOnlnbl())){
			throw InError.comm.E0003("余额不能为空！");
		}
	    if(CommUtil.isNull(input.getOtpatp())){
	    	throw InError.comm.E0003("转出挂销账标志不能为空！");
	    }
	    if(CommUtil.isNull(input.getInpatp())){
	    	throw InError.comm.E0003("转入挂销账标志不能为空！");
	    }
	    
	    //关联错账冲正查询套号是否已存在  add by wuzhixiang 20161230
   	 	List<KnsCmbk> knsCmbks = InTranOutDao.selKnsCmbkByAcst(input.getAcstno(), CommTools.getBaseRunEnvs().getTrxn_date(), false);
   	 	List<KnsStrk> knsStrks = WrAccRbDao.selKnsStrkByNumbsq(input.getAcstno(), CommTools.getBaseRunEnvs().getTrxn_date(), false);
		if(CommUtil.isNotNull(knsCmbks)||CommUtil.isNotNull(knsStrks)){
			 throw InError.comm.E0015(input.getAcstno());    		 
		}
	    
		//金额检查 当前余额+透支限额 - 发生额>= 0 否则拒绝
		GlKnaAcct glKnaAcct = InQuerySqlsDao.sel_GlKnaAcct_by_acct(input.getOtacno(), true);
		GlKnaAcct glKnaInAcct = InQuerySqlsDao.sel_GlKnaAcct_by_acct(input.getInacno(), true);
		if(!CommUtil.equals(glKnaAcct.getBrchno(),CommTools.getBaseRunEnvs().getTrxn_branch())){
			throw InError.comm.E0003("交易机构与转出账号机构不一致，不允许交易！");
		}
		if((input.getOtamcd() == E_AMNTCD.CR && glKnaAcct.getBlncdn() == E_BLNCDN.D) ||
			(input.getOtamcd() == E_AMNTCD.DR && glKnaAcct.getBlncdn() == E_BLNCDN.C)){
			
			if(CommUtil.compare(input.getOnlnbl().add(glKnaAcct.getOvmony()),input.getTranam())<0){
				throw InError.comm.E0003("当前可用余额必须大于发生额！");
			}	
		}
		
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
	   		if(!CommUtil.equals(input.getAcctbr(), glKnaInAcct.getBrchno())){
	   			throw InError.comm.E0003("账户机构与转入机构的开户机构不一致！");
	   		}
	   	}
	   	if(CommUtil.isNull(input.getSmrytx())){
	   		throw InError.comm.E0003("摘要不能为空！");
	   	}
		//系统内转出内部户检查
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
        	throw InError.comm.E0003("转入户名与账户注册户名不一致！");
		}
	
		if(ioToacInfo.getIoflag()==E_IOFLAG.OUT){
			throw InError.comm.E0003("该账户不能为表外账户！");
		}
		
		//转入转出挂账录入检查
	    if(input.getOtpatp() == E_PAYATP._1 || input.getInpatp() == E_PAYATP._1){
	    	
	    	inputPayaChk(input);
	    }
	  //转入转出销账录入检查
	    if(input.getOtpatp() == E_PAYATP._2 || input.getInpatp() == E_PAYATP._2){
	    	
	    	inputPaydChk(input);
	    }	
		bizlog.debug("内部户录入检测结束==========");
	}
	//转入转出销账录入检查
	private static void inputPaydChk(Input input) {
		//销账明细录入检查
		if(input.getOtpatp() == E_PAYATP._2){
			BigDecimal amount = new BigDecimal(0);
			 for(PaydFirst paydFirst : input.getPaydListInfoFirst()){
		    		
		    		/*if(CommUtil.isNull(paydFirst.getPaydsq())){
						throw InError.comm.E0003("录入时，销账序号不能为空！");
					}*/
		    		
		    		if(CommUtil.isNull(paydFirst.getCharsq())){
						throw InError.comm.E0003("销账明细中，待销账流水不能为空");
					}
			    	
		    		if(CommUtil.equals(paydFirst.getTotlmn(), BigDecimal.ZERO)){
						throw InError.comm.E0003("销账明细中，原未销金额不能为空或零");
					}
			    	
		    		if(CommUtil.compare(paydFirst.getCharam(), BigDecimal.ZERO) <= 0){
		    			throw InError.comm.E0003("销账明细中，销账金额大于零");
		    		}
		    		if(CommUtil.compare(paydFirst.getCharam(), paydFirst.getTotlmn()) > 0){
		    			throw InError.comm.E0003("销账明细，本次销账金额["+paydFirst.getCharam()+"]大于原未销金额["+paydFirst.getTotlmn()+"],销账失败！");
		    		}
		    		if(!CommUtil.equals(paydFirst.getBalanc(), paydFirst.getTotlmn().subtract(paydFirst.getCharam()))){
		    			throw InError.comm.E0003("销账明细中，剩余挂账金额应为原未销金额与本次销账金额之差");
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
	    		
	    		/*if(CommUtil.isNull(paydSecond.getPaydse())){
					throw InError.comm.E0003("录入时，销账序号不能为空");
				}*/
	    		
	    		if(CommUtil.isNull(paydSecond.getCharse())){
					throw InError.comm.E0003("销账明细中，待销账流水不能为空");
				}
		    	
	    		if(CommUtil.equals(paydSecond.getTotlmt(), BigDecimal.ZERO)){
					throw InError.comm.E0003("销账明细中，原未销金额不能为空或零");
				}
		    	
	    		if(CommUtil.compare(paydSecond.getCharan(), BigDecimal.ZERO) <= 0){
	    			throw InError.comm.E0003("销账明细中，销账金额大于零");
	    		}
	    		if(CommUtil.compare(paydSecond.getCharan(), paydSecond.getTotlmt()) > 0){
	    			throw InError.comm.E0003("销账明细，本次销账金额["+paydSecond.getCharan()+"]大于原未销金额["+paydSecond.getTotlmt()+"],销账失败！");
	    		}	    		
	    		if(!CommUtil.equals(paydSecond.getBalane(), paydSecond.getTotlmt().subtract(paydSecond.getCharan()))){
	    			throw InError.comm.E0003("销账明细中，剩余挂账金额应为原未销金额与本次销账金额之差");
	    		}
	    		amount2 = amount2.add(paydSecond.getCharan());
	    	}
	    	if(CommUtil.compare(amount2, input.getTranam()) != 0){				 
				 throw InError.comm.E0003("合计销账金额不等于交易金额！");
			 }
		}
		
	}
	//转入转出
	private static void inputPayaChk(Input input) {
		//挂账明细录入检查
	    bizlog.debug("挂账明细检测开始==========");
		//转出账户挂账检查
	    if(input.getOtpatp() == E_PAYATP._1 ){
	    	for(PayaFirst payaFirst : input.getPayaListInfoFirst()){
				
				if(CommUtil.isNull(payaFirst.getToacno())){
					throw InError.comm.E0003("转出账号挂账明细中，对方账号不能为空!");
				}
				if(!CommUtil.equals(payaFirst.getToacno(), input.getInacno())){
	    			throw InError.comm.E0003("转出账号挂账明细中，对方账号应为转入账号!");
	    		}
	    		if(CommUtil.isNull(payaFirst.getToacna())){
					throw InError.comm.E0003("转出账号挂账明细中，对方户名不能为空!");
				}
	    		if(!CommUtil.equals(payaFirst.getToacna(), input.getInacna())){
	    			throw InError.comm.E0003("转出账号挂账明细中，对方户名应为转入户名!");
	    		}
	    		if(CommUtil.isNull(payaFirst.getPayamn())){
					throw InError.comm.E0003("转出账号挂账明细中，挂账金额不能为空!");
				}	    		
	    		if(CommUtil.isNull(payaFirst.getPayabr())){
					throw InError.comm.E0003("转出账号挂账明细中，挂账机构不能为空!");
				}	    			    		
			}
	    }
		//转入账户挂账检查
	    if(input.getInpatp() == E_PAYATP._1){
	    	for(PayaSecond payaSecond : input.getPayaListInfoSecond()){
				
				if(CommUtil.isNull(payaSecond.getToacnb())){
					throw InError.comm.E0003("转入挂账明细中，对方账号不能为空！");
				}
				if(!CommUtil.equals(payaSecond.getToacnb(), input.getOtacno())){
	    			throw InError.comm.E0003("转入账号挂账明细中，对方账号应为转出账号！");
	    		}
	    		if(CommUtil.isNull(payaSecond.getToacnm())){
					throw InError.comm.E0003("转入账号挂账明细中，对方户名不能为空!");
				}
	    		if(!CommUtil.equals(payaSecond.getToacnm(), input.getOtacna())){
	    			throw InError.comm.E0003("转入账号挂账明细中，对方户名应为转出户名！");
	    		}
	    		if(CommUtil.isNull(payaSecond.getPayamt())){
					throw InError.comm.E0003("转入账号挂账明细中，挂账金额不能为空!");
				}	    		
	    		if(CommUtil.isNull(payaSecond.getPayabh())){
					throw InError.comm.E0003("转入账号挂账明细中，挂账机构不能为空!");
				}
	    		
			}
	    }		
		  bizlog.debug("挂账明细检测结束==========");		
	}
	
}
