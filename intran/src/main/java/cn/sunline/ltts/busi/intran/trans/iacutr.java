package cn.sunline.ltts.busi.intran.trans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.in.namedsql.InTranOutDao;
import cn.sunline.ltts.busi.in.namedsql.WrAccRbDao;
import cn.sunline.ltts.busi.in.tables.In.KnsCmbk;
import cn.sunline.ltts.busi.in.tables.In.KnsCmbkDao;
import cn.sunline.ltts.busi.in.tables.In.KnsPaya;
import cn.sunline.ltts.busi.in.tables.In.KnsPayaDao;
import cn.sunline.ltts.busi.in.tables.In.KnsPayd;
import cn.sunline.ltts.busi.in.tables.In.KnsStrk;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PayaDetail;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PaydDetail;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.errors.PbError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CMBKTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CMBK_TRANST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CNTSYS;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ISPAYA;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_KPACFG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYATP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;


public class iacutr {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(iacutr.class);

	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-13 09：18</li>
	 *         <li>内部账转客户账录入</li>
	 *         </p>
	 * @param input
	 *           内部账转客户账录入信息
	 * */
public static void befroeCheck( final cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Input input, final cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Property property,  final cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Output output){
		
		bizlog.debug("==========内部账转客户账录入检查开始==========");
		
		custInputCheck(input, E_CMBKTP.INS);
		//内部账转客户账输入检查
		if(CommUtil.isNull(input.getCntsys())){
			throw InError.comm.E0003("获取跨系统转账标志失败！");
		}
		
	    bizlog.debug("==========内部账转客户账录入检查结束==========");
	}
	
	
	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-07 08：32</li>
	 *         <li>内部账转客户账输入检查</li>
	 *         </p>
	 * @param input
	 *           内部账转客户账录入信息
	 * */
	//返回跨系统标志：复核接口中无跨系统标志，需从数据库中提取返回
	public static E_CNTSYS custInputCheck(final cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Input input, E_CMBKTP cmbktp){
		
		String otacno = input.getOtacno(); //转出账号：内部户
		E_CNTSYS cntsys = input.getCntsys();
	
		//必输字段基础检查
		basicInputChk(input);
        
		//客户账一套传票只能有一条记录
		List<KnsCmbk> knsCmbkList = new ArrayList<KnsCmbk>();
		knsCmbkList = KnsCmbkDao.selectAll_kns_cmbk_odx2(input.getAcstno(), CommTools.getBaseRunEnvs().getTrxn_date(), false);
		
		if(cmbktp == E_CMBKTP.INS){
			
			//关联错账冲正查询套号是否已存在  add by wuzhixiang 20161230
			List<KnsStrk> knsStrks = WrAccRbDao.selKnsStrkByNumbsq(input.getAcstno(), CommTools.getBaseRunEnvs().getTrxn_date(), false);
			if(CommUtil.isNotNull(knsCmbkList)||CommUtil.isNotNull(knsStrks)){
				 throw InError.comm.E0015(input.getAcstno());    		 
			}
		}
		
		//内部户验证Inacinfo
        IoInacInfo ioInacInfo = SysUtil.getInstance(IoInSrvQryTableInfo.class).selKnaGlAcctnoByAcctno(otacno, true);
        
        if(E_INACST.CLOSED == ioInacInfo.getAcctst()){
        	throw InError.comm.E0003("当前转出账户已销户！");
        }

        if(!CommUtil.equals(ioInacInfo.getAcctna(), input.getOtacna())){
        	throw InError.comm.E0003("输入转出户名与账户注册户名不一致！");
        }
        
        if(E_KPACFG._1 == ioInacInfo.getKpacfg()){
        	throw InError.comm.E0003("当前转出账户不允许手工记账！");
        }
        
        if(!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), ioInacInfo.getBrchno())){
        	throw InError.comm.E0003("跨法人机构无权进行该项业务！");
        }
        if(CommUtil.isNull(input.getCrcycd())){
        	throw InError.comm.E0003("币种不能为空！");
        }
        if(!CommUtil.equals(input.getCrcycd(),ioInacInfo.getCrcycd())){
        	throw InError.comm.E0003("当前输入币种与内部户币种类型不符，请核查！");
        }
        
        if(ioInacInfo.getIspaya() == E_ISPAYA._1 && input.getPayatp() == E_PAYATP._0){
			throw InError.comm.E0003("该转出账号为挂销账管理账号，挂销账标志不能为非挂销账！");
		}
		
		if(ioInacInfo.getIspaya() == E_ISPAYA._0 && input.getPayatp() != E_PAYATP._0){
			throw InError.comm.E0003("该转出账号为非挂销账管理账号，挂销账标志应为非挂销账！");
		}
        
		//销账检查中，通过内部户余额方向与转出借贷方向，判断销账标志是否正确
		E_BLNCDN blncdn = ioInacInfo.getBlncdn(); //内部户余额方向
		
		 //挂账检查
	    if(input.getPayatp() == E_PAYATP._1){
	    	inputPayaChk(input, blncdn);
	    }
		
	    //销账检查
	    if(input.getPayatp() == E_PAYATP._2){
	    	inputPaydChk(input, blncdn);
	    }
	
	    //复核验证
	    if(cmbktp == E_CMBKTP.CHK){
	    	cntsys = custCuckCheck(input);
	    }
	    
	    //复核接口中无跨系统标志，需从数据库中提取返回
	    return cntsys; 
	}
	
	//必输字段基础检查
	private static void basicInputChk(final cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Input input){
		
		String otacno = input.getOtacno(); //转出账号：内部户
		String inacno = input.getInacno(); //转入账号：客户账号
		BigDecimal tranam = input.getTranam(); //交易金额
		
		//必输字段检查
		if(CommUtil.isNull(inacno) || CommUtil.isNull(otacno)){
			throw InError.comm.E0005("转入转出账号"); //转入转出账号不能为空
		}
		
		if(CommUtil.isNull(input.getAcstno())){
			throw InError.comm.E0005("套号"); //套号不能为空
		}
		
		if(CommUtil.isNull(input.getOtacna())){
			throw InError.comm.E0005("转出户名"); //转出户名不能为空
		}
		
		if(CommUtil.isNull(input.getOtamcd())){
			throw InError.comm.E0005("转出借贷标志"); //转出借贷标志不能为空
		}
		
		if(CommUtil.isNull(input.getCrcycd())){
			throw InError.comm.E0005("币种"); //币种不能为空
		}
		//add by wuzx 20161222 增加外币为日元时金额控制- beg
		BusiTools.validAmount(input.getCrcycd(), input.getTranam());
		
		//add by wuzx 20161222 增加外币为日元时金额控制- end
		if(CommUtil.isNull(input.getInacna())){
			throw InError.comm.E0005("转入户名"); //转入户名不能为空
		}
		
		if(CommUtil.isNull(input.getAcctbr())){
			throw InError.comm.E0005("账户机构"); //账户机构不能为空
		}
		
		if(CommUtil.isNull(input.getInamcd())){
			throw InError.comm.E0005("转入借贷标志"); //转入借贷标志不能为空
		}
		
		/*if(CommUtil.isNull(input.getCsextg())){
			throw InError.comm.E0005("钞汇标志"); //钞汇标志不能为空
		}*/
		
		if(CommUtil.isNull(input.getPayatp())){
			throw InError.comm.E0005("挂销账标志"); //挂销账标志不能为空
		}
		
		if(CommUtil.isNull(input.getSmrytx())){
			throw InError.comm.E0005("摘要"); //摘要不能为空
		}
		
		/*if(!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), input.getAcctbr())){
			throw InError.comm.E0003("跨法人机构无权进行该项业务！");
		}*/
		
		if(CommUtil.isNull(input.getCntsys())){
			throw InError.comm.E0005("是否跨系统转账标志"); //是否跨系统转账标志不能为空
		}
		
		if(input.getOtamcd() == E_AMNTCD.CR ||input.getInamcd() == E_AMNTCD.DR){
			throw InError.comm.E0005("转出借贷标志只能为借方，转入借贷标志只能为贷方，请核查"); //客户账只能为贷方
		}
		
		//输入合法性检查
		/*if(input.getAcstno().length() != 11){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}*/
		
		if(CommTools.rpxMatch("W[0-9]+", input.getAcstno()) == 1){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		//交易金额验证
		if(CommUtil.compare(tranam, BigDecimal.ZERO) <= 0){
			throw InError.comm.E0003("交易金额必须大于零");
		}

        //本系统客户账验证
        if(input.getCntsys() ==  E_CNTSYS._1){
        	
        	if(inacno.length() != 18 ){
        		throw InError.comm.E0003("客户账号[" + inacno + "]格式错误");
        	}
        }else{
        	//系统外客户账，15位：柜面核心活期存款账户； 16位：信用卡； 19位：柜面核心借记卡或活期存款账户
        	if(inacno.length() != 15 && inacno.length() != 16 && inacno.length() != 19){
        		throw InError.comm.E0003("客户账号[" + inacno + "]格式错误");
        	}
        }
        
        bizlog.debug("==========必输字段检查结束");
	}
	
	//输入挂账信息检查
	private static void inputPayaChk(final cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Input input, E_BLNCDN blncdn){
		 //挂销账处理标志判断,记账方向与余额方向相同为挂账，不同为销账
    	if(blncdn == E_BLNCDN.C && input.getOtamcd() == E_AMNTCD.DR){
         		
    		throw InError.comm.E0003("转出账户记账方向与余额方向不同为销账，请核查！");
    	}
    	if(blncdn == E_BLNCDN.D && input.getOtamcd() == E_AMNTCD.CR){
      		
      		throw InError.comm.E0003("转出账户记账方向与余额方向不同为销账，请核查！");
     	}
    	
    	if(input.getPayaListInfo().size() == 0){
    		throw InError.comm.E0003("该传票挂账明细内容不能为空！");
    	}
    	
    	BigDecimal amount = new BigDecimal(0);
    	for(PayaDetail payaDetail : input.getPayaListInfo()){
    		
    		if(CommUtil.isNull(payaDetail.getToacno())){
				throw InError.comm.E0003("挂账明细中，对方账号不能为空");
			}
    		 if(input.getCntsys() ==  E_CNTSYS._1){  //系统内才校验 
    			 if(!CommUtil.equals(payaDetail.getToacno(), input.getInacno())){
    	    			throw InError.comm.E0003("挂账明细中，对方账号应为转入账号");
    			 }
    		 }
    		if(CommUtil.isNull(payaDetail.getToacna())){
				throw InError.comm.E0003("挂账明细中，对方户名不能为空");
			}
    		if(input.getCntsys() ==  E_CNTSYS._1){ //系统内才校验 
    			if(!CommUtil.equals(payaDetail.getToacna(), input.getInacna())){
        			throw InError.comm.E0003("挂账明细中，对方户名应为转入户名");
        		}
    		}   		
    		if(CommUtil.isNull(payaDetail.getPayabr())){
				throw InError.comm.E0003("挂账明细中，挂账机构不能为空");
			}
    		
    		if(!CommUtil.equals(payaDetail.getPayabr(), CommTools.getBaseRunEnvs().getTrxn_branch())){
				throw InError.comm.E0003("挂账明细中，挂账机构非本交易机构，请核查");
			}
    		
    		amount = amount.add(payaDetail.getPayamn());
		}
    	
		if(CommUtil.compare(amount, input.getTranam()) != 0){
			throw InError.comm.E0003("挂账金额不等于交易发生额");
		}
	}
	
	//输入销账信息检查
	private static void inputPaydChk(final cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Input input, E_BLNCDN blncdn){
		//挂销账处理标志判断,记账方向与余额方向相同为挂账，不同为销账
    	if(blncdn == E_BLNCDN.C && input.getOtamcd() == E_AMNTCD.CR){
        		
        	throw InError.comm.E0003("转出账户记账方向与余额方向相同为挂账，请核查！");
       	}
       	 
       	if(blncdn == E_BLNCDN.D && input.getOtamcd() == E_AMNTCD.DR){
     		
     		throw InError.comm.E0003("转出账户记账方向与余额方向相同为挂账，请核查！");
    	}
    	
    	if(input.getPaydListInfo().size() == 0){
    		throw InError.comm.E0003("该传票销账记录不能为空！");
    	}
    	
    	BigDecimal amount = new BigDecimal(0);
    	for(PaydDetail paydDetail : input.getPaydListInfo()){
    		
    		if(CommUtil.isNull(paydDetail.getPrpysq())){
				throw InError.comm.E0003("销账明细中，挂账序号不能为空");
			}
	    	
    		if(CommUtil.compare(paydDetail.getRsdlmn(), BigDecimal.ZERO) < 0){
    			throw InError.comm.E0003("销账明细中，剩余挂账金额应不小于零");
    		}
    		
    		if(CommUtil.compare(paydDetail.getTotlmn(), BigDecimal.ZERO) <= 0){
    			throw InError.comm.E0003("销账明细中，原未销金额金额应大于零");
    		}
    		
    		if(CommUtil.compare(paydDetail.getPaydmn(), BigDecimal.ZERO) <= 0){
    			throw InError.comm.E0003("销账明细中，销账金额大于零");
    		}
    		
    		if(!CommUtil.equals(paydDetail.getRsdlmn(), paydDetail.getTotlmn().subtract(paydDetail.getPaydmn()))){
    			throw InError.comm.E0003("销账明细中，剩余挂账金额应为原未销金额与本次销账金额之差");
    		}
	    	
			KnsPaya knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(paydDetail.getPrpysq(), true);
			
			if(!CommUtil.equals(knsPaya.getPayaac(), input.getOtacno())){
				throw InError.comm.E0003("转出账号与销账明细中原挂账记录挂账账号不同，请核查");
			}
			
			if(CommUtil.compare(paydDetail.getPaydmn(), knsPaya.getRsdlmn()) > 0){
				 throw InError.comm.E0003("销账金额大于挂账记录剩余挂账金额");
			}
		/*	if(!CommUtil.equals(paydDetail.getTotlmn().subtract(paydDetail.getPaydmn()), knsPaya.getRsdlmn())){
				 throw InError.comm.E0003("销账明细中，原未销金额应为对应挂账中剩余挂账金额");
			}*/
			amount = amount.add(paydDetail.getPaydmn());
		}
    	
		if(CommUtil.compare(amount, input.getTranam()) != 0){
			throw InError.comm.E0003("合计销账金额不等于交易发生额");
		}
	}
	
	//复核验证
	//返回查表所得跨系统转账标志
	private static E_CNTSYS custCuckCheck(final cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Input input){
		
		bizlog.debug("内部账转客户账复核与输入匹配测试开始==========");
    	//
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller();
		List<KnsCmbk> knsCmbks = InTranOutDao.selKnsCmbkByAcst(input.getAcstno(), trandt, false);
		if(CommUtil.isNull(knsCmbks)){
			throw InError.comm.E0003("套号不正确，请核对！");
		}
		
		if(CommUtil.equals(knsCmbks.get(0).getTranus(), tranus)){
			throw InError.comm.E0003("复核柜员不能为初始录入柜员["+knsCmbks.get(0).getTranus()+"]，请换人复核！");
		}
		
		//传票状态为已记账和删除的拒绝
		if(knsCmbks.get(0).getIavcst().equals(E_CMBK_TRANST._1) || knsCmbks.get(0).getIavcst().equals(E_CMBK_TRANST._2)){
				throw InError.comm.E0003("套号不正确，请核对！");
		}
		
		//传票状态为已复核的拒绝
		if(knsCmbks.get(0).getIavcst().equals(E_CMBK_TRANST._3)){
			throw InError.comm.E0003("该套号已复核，请核对！");
		}
		
		//账号不匹配的拒绝
		if(!CommUtil.equals(input.getOtacno(), knsCmbks.get(0).getOtacno())){
			throw InError.comm.E0003("转出账号与录入信息不符，请检查！");
		}
		
		if(!CommUtil.equals(input.getInacno(), knsCmbks.get(0).getInacno())){
			throw InError.comm.E0003("转入账号与录入信息不符，请检查！");
		}
		
		if(!CommUtil.equals(input.getOtacna(), knsCmbks.get(0).getOtacna())){
			throw InError.comm.E0003("转出户名与录入信息不符，请检查！");
		}
		
		if(!CommUtil.equals(input.getInacna(), knsCmbks.get(0).getInacna())){
			throw InError.comm.E0003("转入户名与录入信息不符，请检查！");
		}
		
		if(!CommUtil.equals(input.getCrcycd(),knsCmbks.get(0).getCrcycd())){
			throw InError.comm.E0003("币种与录入信息不符，请检查！");
		}
		
	/*	if(input.getCsextg() != knsCmbk.getCsextg()){
			throw InError.comm.E0003("钞汇标志与录入信息不符，请检查！");
		}*/
		
		if(input.getPayatp() != knsCmbks.get(0).getPayatp()){
			throw InError.comm.E0003("挂销账标志与录入信息不符，请检查！");
		}
		
        //借贷标志不匹配的拒绝
		if(input.getOtamcd() != knsCmbks.get(0).getOtamcd()){
			throw InError.comm.E0003("借贷标志与录入信息不符，请检查！");
		}
		
		//交易金额不匹配的拒绝
		if(!CommUtil.equals(input.getTranam(), knsCmbks.get(0).getTranam())){
			throw InError.comm.E0003("交易金额与录入信息不符，请检查！");
		}
		
		if(input.getCntsys() != knsCmbks.get(0).getCntsys()){
			throw InError.comm.E0003("跨系统转账标志与录入信息不符，请检查！");
		}
		
		//销账信息核查
		if(input.getPayatp() == E_PAYATP._1){
			
			chkPayaChk(input, trandt,knsCmbks.get(0).getOtacno());
		}
		
		//销账信息核查
		if(input.getPayatp() == E_PAYATP._2){
			
			chkPaydChk(input, trandt,knsCmbks.get(0).getOtacno());
		}
		return knsCmbks.get(0).getCntsys();
	}
	//复核挂账检查
	private static void chkPayaChk(final cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Input input, String trandt,String otacno){
		
		List<KnsPaya> knsPayaDbList = InTranOutDao.selKnsPayaByAcst(input.getAcstno(), trandt,otacno, true);
		Options<PayaDetail> knsPayaCkoptions = input.getPayaListInfo();
		//数目比对
		if(knsPayaDbList.size() != knsPayaCkoptions.size()){
			throw InError.comm.E0003("套内挂账信息记录数与原挂账信息不符，请核对！");
		}
		
		//遍历每一条记录，进行比对
		Map<String, KnsPaya> knsPayaMap = new HashMap<String, KnsPaya>();
		//将数据库中的每一条销账记录存在map中
		for(KnsPaya knsPaya : knsPayaDbList){
			knsPayaMap.put(knsPaya.getPayasq(), knsPaya);
		}
		
		//遍历待核查每一条记录,进行比较
		KnsPaya knsPayaDb = null;
		for(PayaDetail payaDetail : knsPayaCkoptions){
			
			if(CommUtil.isNull(payaDetail.getPayasq())){
				throw InError.comm.E0003("挂账明细中，挂账序号不能为空！");
			}
			
			knsPayaDb = knsPayaMap.get(payaDetail.getPayasq());
			if(CommUtil.isNull(knsPayaDb)){
				throw InError.comm.E0003("未找到对应挂账记录，请核对！");
			}
			if(!CommUtil.equals(knsPayaDb.getPayabr(), payaDetail.getPayabr())){
				throw InError.comm.E0003("挂账机构与原挂账信息不符，请核对！");
			}
			if(!CommUtil.equals(knsPayaDb.getToacct(), payaDetail.getToacno())){
				throw InError.comm.E0003("对方账号与原挂账信息不符，请核对！");
			}
			if(!CommUtil.equals(knsPayaDb.getToacna(), payaDetail.getToacna())){
				throw InError.comm.E0003("对方名称与原挂账信息不符，请核对！");
			}
		}
	}
	
	//复核销账检查
	private static void chkPaydChk(final cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Input input, String trandt,String otacno){
		
		List<KnsPayd> knsPaydDbList = InTranOutDao.selKnsPaydByAcst(input.getAcstno(), trandt,otacno, true);
		Options<PaydDetail> knsPaydCkoptions = input.getPaydListInfo();
		//数目比对
		if(knsPaydDbList.size() != knsPaydCkoptions.size()){
			throw InError.comm.E0003("套内销账信息记录数与原销账信息不符，请核对！");
		}
		//遍历每一条记录，进行比对
		Map<String, KnsPayd> knsPaydMap = new HashMap<String, KnsPayd>();
		//将数据库中的每一条销账记录存在map中
		for(KnsPayd knsPayd : knsPaydDbList){
			knsPaydMap.put(knsPayd.getPaydsq(), knsPayd);
		}
		
		//遍历待核查每一条记录,进行比较
		for(PaydDetail paydDetail : knsPaydCkoptions){
			
			//复核时，销账序号不能为空，维护时为空，认为是新增一条销账记录
    			
			if(CommUtil.isNull(paydDetail.getPaydsq())){
				throw InError.comm.E0003("销账明细中，销账序号不能为空！");
			}
			
			//从map中取出对应的记录
			KnsPayd knsPayd = knsPaydMap.get(paydDetail.getPaydsq());
			
			if(CommUtil.isNull(knsPayd)){
				throw InError.comm.E0003("套内销账信息挂账序号与原销账信息不符，请核对！");
			}
			
			if(!CommUtil.equals(paydDetail.getPaydmn(), knsPayd.getPayamn())){
				throw InError.comm.E0003("套内销账信息销账金额与原销账信息不符，请核对！");
			}
			
			if(!CommUtil.equals(paydDetail.getPrpysq(), knsPayd.getPayasq())){
				throw InError.comm.E0003("套内销账信息挂账序号与原销账信息不符，请核对！");
			}
			
			if(!CommUtil.equals(paydDetail.getTotlmn(), knsPayd.getTotlmn())){
				throw InError.comm.E0003("套内销账信息原未销金额与原销账信息不符，请核对！");
			}
			
			if(!CommUtil.equals(paydDetail.getRsdlmn(), knsPayd.getRsdlmn())){
				throw InError.comm.E0003("套内销账信息剩余销账金额与原销账信息不符，请核对！");
			}
		}
	}

	//客户是否可转入状态检查
	public static void custIrregular( final cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Property property,  final cn.sunline.ltts.busi.intran.trans.intf.Iacutr.Output output){
		
		bizlog.debug("客户账号状态检查开始==========");
		
		// 调入电子账号信息
		IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb2(input.getInacno(), false);
		if(CommUtil.isNull(tblKnaAcdc)){
			throw InError.comm.E0003("该转入账号["+input.getInacno()+"]不存在！");
		}
		E_CUACST status = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(tblKnaAcdc.getCustac());//查询电子账户状态信息
		if(status==E_CUACST.PREOPEN||status==E_CUACST.CLOSED||status==E_CUACST.DELETE||status==E_CUACST.PRECLOS||status==E_CUACST.OUTAGE){
			throw PbError.PbComm.E2015("该转入账号["+input.getInacno()+"]状态为["+status.getLongName()+"]，不允许交易！");
		}
		IoDpAcStatusWord cplGetAcStWord = SysUtil.getInstance(
				IoDpFrozSvcType.class).getAcStatusWord(tblKnaAcdc.getCustac());
		if (cplGetAcStWord.getDbfroz() == E_YES___.YES){
			throw DpModuleError.DpstAcct.E9999("交易失败，已被冻结！");		
		}
		IoCaKnaCust tblKnaCust = SysUtil.getInstance(
				IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(
				tblKnaAcdc.getCustac(), true);
		bizlog.debug("注册户名为：" +tblKnaCust.getCustna()  + ",输入户名为：" + input.getInacna());
		
		if(!CommUtil.equals(tblKnaCust.getCustna(), input.getInacna())){
			throw InError.comm.E0003("输入户名["+input.getInacna()+"]与客户账号注册户名["+tblKnaCust.getCustna()+"]不匹配");
		}
		bizlog.debug("客户账号状态检查结束==========");
		
	}
}
