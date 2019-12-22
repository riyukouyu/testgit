package cn.sunline.ltts.busi.intran.trans;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.in.namedsql.InTranOutDao;
import cn.sunline.ltts.busi.in.namedsql.WrAccRbDao;
import cn.sunline.ltts.busi.in.tables.In.KnsCmbk;
import cn.sunline.ltts.busi.in.tables.In.KnsPaya;
import cn.sunline.ltts.busi.in.tables.In.KnsPayaDao;
import cn.sunline.ltts.busi.in.tables.In.KnsPayd;
import cn.sunline.ltts.busi.in.tables.In.KnsPaydDao;
import cn.sunline.ltts.busi.in.tables.In.KnsStrk;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.IavcbkDetail;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PayaDetail;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PaydDetail;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CMBKTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CMBK_TRANST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CNTSYS;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_IAVCTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ISADDS;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ISPAYA;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_KPACFG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYAST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYATP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYDST;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;


public class iavcbk {

	private static final BizLog bizlog = BizLogUtil.getBizLog(iavcbk.class);
	
	
	/**
	 * @Author chenjk
	 *         <p>
	 *         <li>功能说明：内部户录入检测</li>
	 *         </p>
	 * @param input
	 *            输入信息
	 * @param output
	 *            输出信息
	 * @return
	 */
	public static void befroeCheck( final cn.sunline.ltts.busi.intran.trans.intf.Iavcbk.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Iavcbk.Output output){
		
		bizlog.debug("==========内部户转内部户录入检查开始==========");
		
		//内部账输入检查
		inputCheck(input, E_CMBKTP.INS);
		
	    bizlog.debug("==========内部户转内部户录入检查结束==========");
	}
	
	
	public static void inputCheck(final cn.sunline.ltts.busi.intran.trans.intf.Iavcbk.Input input, E_CMBKTP cmbktp){
		bizlog.debug("内部户录入检测开始==========");
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		if(CommUtil.isNull(input.getAcstno())){
			throw InError.comm.E0003("输入套号不能为空");
		}
		
		if(CommUtil.isNull(input.getCntsys())){
			throw InError.comm.E0003("跨系统转账标志不能为空");
		}

		if(input.getAcstno().length() != 11){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		if(CommTools.rpxMatch("W[0-9]+", input.getAcstno()) == 1){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		 //add by wuzx 20161229 -套号新增时需要判断套号是否重复  beg
		if(cmbktp == E_CMBKTP.INS){
			
	        if(CommUtil.isNull(input.getIsadds())){
	        	throw InError.comm.E0003("套号，序号新增标志不能为空，请核查！");
	        }       
	        if(input.getIsadds() == E_ISADDS._0){//套号新增时
	        	 List<KnsCmbk> knsCmbks = InTranOutDao.selKnsCmbkByAcst(input.getAcstno(), CommTools.getBaseRunEnvs().getTrxn_date(), false);
	        	 List<KnsStrk> knsStrks = WrAccRbDao.selKnsStrkByNumbsq(input.getAcstno(), CommTools.getBaseRunEnvs().getTrxn_date(), false);
	        	
	        	 if(CommUtil.isNotNull(knsCmbks)||CommUtil.isNotNull(knsStrks)){
	        		 throw InError.comm.E0015(input.getAcstno());    		 
	        	 }
	        }
	       
		}
		 //add by wuzx 20161229 -套号新增时需要判断套号是否重复 end
		if(cmbktp == E_CMBKTP.CHK||cmbktp ==E_CMBKTP.UPD){//复核和维护时，序号不允许为空
			
			for(IavcbkDetail iavcbkDetail : input.getIavcbkListInfo()){
				if(CommUtil.isNull(iavcbkDetail.getPayseq())||iavcbkDetail.getPayseq()==0){
					throw InError.comm.E0003("序号不能为空！");				
				}else{
					KnsCmbk knscmbk = InTranOutDao.selKnsCmbkByAcstnoAndPayseq(input.getAcstno(), trandt, iavcbkDetail.getPayseq(), false);
					if(CommUtil.isNull(knscmbk) && cmbktp == E_CMBKTP.CHK){
						throw InError.comm.E0003("交易失败，根据套号和序号查找无对应的记录！");
					}else if(CommUtil.isNotNull(knscmbk) && E_CMBK_TRANST._0 != knscmbk.getIavcst()){
						  throw InError.comm.E0003("处于该状态下的套号不允许做此交易！");
					}else {
						//传票维护时允许新增
					}
				}
			}			
		}
		//搜出所有未作废记录
	    List<KnsCmbk> knsCmbkList = InTranOutDao.selKnsCmbkByAcst(input.getAcstno(), CommTools.getBaseRunEnvs().getTrxn_date(), false);
		if(CommUtil.isNotNull(knsCmbkList)||knsCmbkList.size()>0){
			for(int i=0;i<knsCmbkList.size();i++){
				if(knsCmbkList.get(i).getIavcst() == E_CMBK_TRANST._2){
					throw InError.comm.E0003("处于该状态下的套号不允许做此交易！");
				}
			}		
		}
		
	    Integer count = 0; //系统内账号计数，第一笔一定是转出，系统内这笔
	    Boolean flag = false;// 判断是否存在红字记账
	    BigDecimal totlam = BigDecimal.ZERO;
		for(IavcbkDetail iavcbkDetail : input.getIavcbkListInfo()){
			
			count++; //开始计数
			totlam =totlam.add(iavcbkDetail.getTranam());
			//内部账验证
			String acctno = iavcbkDetail.getAcctno();
			if(CommUtil.isNull(acctno)){
				throw InError.comm.E0003("账号不能为空！");
			}
			
			//change by cjk 20180518 跨机构改动1
			 
//			if(input.getCntsys() == E_CNTSYS._1){//系统内不允许跨机构转入
//				String tranbr =  CommTools.getBaseRunEnvs().getTrxn_branch();//交易机构
//				for(int i=0 ;i<input.getIavcbkListInfo().size();i++){			
//					String brchno = input.getIavcbkListInfo().get(i).getAcctno().substring(0, 6);
//					if(!CommUtil.equals(tranbr, brchno)){
//						throw InError.comm.E0003("系统内不允许跨机构转入！");
//					}
//				}
//			}
			if(input.getCntsys() == E_CNTSYS._0){
				if(knsCmbkList.size()>2){
		    		 throw InError.comm.E0003("跨系统时，只支持一借一贷！"); 
				}
			}
			if(CommUtil.isNotNull(iavcbkDetail.getPayseq())){
				
				if(iavcbkDetail.getPayseq() < 0){
					throw InError.comm.E0003("套内序号不合法，请核查！");
				}
			}
			
			if(CommUtil.isNull(iavcbkDetail.getAcctna())){
				throw InError.comm.E0003("户名不能为空");
			}
			if(CommUtil.isNull(iavcbkDetail.getSmrytx())){
				throw InError.comm.E0003("摘要不能为空！");
			}
			if(CommUtil.isNull(iavcbkDetail.getAmntcd())){
				throw InError.comm.E0003("借贷标志不能为空");
			}
			
			if(CommUtil.isNull(iavcbkDetail.getCrcycd())){
				throw InError.comm.E0003("币种不能为空");
			}
			//add by wuzx 控制币种保持一致 beg
			if(input.getIavctp() != E_IAVCTP._3){				
	            if(CommUtil.isNotNull(knsCmbkList)){
	            	if(!CommUtil.equals(iavcbkDetail.getCrcycd(),knsCmbkList.get(0).getCrcycd())){
	    				throw InError.comm.E0003("套内币种必须保持一致！");
	    			}
	            }			
			}
			//add by wuzx 控制币种保持一致 end
			/*if(CommUtil.compare(iavcbkDetail.getTranam(), BigDecimal.ZERO) <= 0){
				throw InError.comm.E0003("发生额必须大于零");
			}*/
			//add by wuzx 20161222 增加外币为日元时金额控制- beg
			if(CommUtil.compare(iavcbkDetail.getTranam(), BigDecimal.ZERO) <= 0){
				flag = true;//
			}
			BusiTools.validAmount(iavcbkDetail.getCrcycd(), iavcbkDetail.getTranam());
			//add by wuzx 20161222 增加外币为日元时金额控制- end
			if(CommUtil.isNull(iavcbkDetail.getPayatp())){
				throw InError.comm.E0003("挂销账标志不能为空");
			}
			
			//传票类型判断
			if(CommUtil.isNull(input.getIavctp())){
				throw InError.comm.E0003("传票类型不能为空");
			}
			
			//内部账转内部账录入检查
			if(input.getIavctp() == E_IAVCTP._1){
				
				if(iavcbkDetail.getAmntcd() != E_AMNTCD.CR && iavcbkDetail.getAmntcd() != E_AMNTCD.DR){
					throw InError.comm.E0003("借贷标志类型错误！");
				}
			}
			
			//表内外收付检查
			if(input.getIavctp() == E_IAVCTP._3){
				
				if(iavcbkDetail.getAmntcd() != E_AMNTCD.PY && iavcbkDetail.getAmntcd() != E_AMNTCD.RV){
					throw InError.comm.E0003("借贷标志类型错误！");
				}
			}
			/*if(input.getIavctp() == E_IAVCTP._3){
				if(iavcbkDetail.getAmntcd() != E_AMNTCD.CR && iavcbkDetail.getAmntcd() != E_AMNTCD.DR){
					throw InError.comm.E0003("借贷标志类型错误！");
				}
			}*/
			
			//若为转客户账类型报错
			if(input.getIavctp() == E_IAVCTP._2){
				throw InError.comm.E0003("传票类型选择错误，本接口不能处理该类型传票");
			}
					
			//系统内内部户检查
			IoInacInfo ioInacInfo = SysUtil.getInstance(IoInSrvQryTableInfo.class).selKnaGlAcctnoByAcctno(acctno, false);
			if(CommUtil.isNull(ioInacInfo)){
			    throw InError.comm.E0003("内部户不存在");
			}
			//change by cjk 20180518 跨机构改动1
			if(input.getCntsys() == E_CNTSYS._1){//系统内不允许跨机构转入
				String tranbr =  CommTools.getBaseRunEnvs().getTrxn_branch();//交易机构
				for(int i=0 ;i<input.getIavcbkListInfo().size();i++){			
					String brchno = ioInacInfo.getBrchno();
					if(!CommUtil.equals(tranbr, brchno)){
						throw InError.comm.E0003("系统内不允许跨机构转入！");
					}
				}
			}
		
			//内部户查询结果为空处理
			if(CommUtil.isNull(ioInacInfo) || CommUtil.isNull(ioInacInfo.getAcctno())){
				
				if(input.getCntsys() == E_CNTSYS._1 ){
					//系统内，内部户为空则报错
					throw InError.comm.E0003("账户不存在！");
				}else{
					
					//跨系统时，第一笔账号一定为系统内账号
					if(count == 1){
						throw InError.comm.E0003("跨系统时，第一笔账号一定为系统内账号！");
					}
					if(knsCmbkList.size()>2){
				    		 throw InError.comm.E0003("跨系统时，只支持一借一贷！");  //表外收付，若已存在一笔记录，则报错
				    }
					//跨系统，内部户为空，则此账户为系统外内部户，
					if(acctno.length() != 20){
						throw InError.comm.E0003("柜面核心内部户长度为20位！");
					}
				}
			}else{
				//add by wuzx 20161212 录入增加表外账判断 beg
				if(input.getIavctp() == E_IAVCTP._1){				
					if(ioInacInfo.getIoflag()==E_IOFLAG.OUT){
						throw InError.comm.E0003("表外账户不支持内转内交易！");
					}
				}
				//add by wuzx 20161212 录入增加表外账判断 end
				if(E_INACST.CLOSED == ioInacInfo.getAcctst()){
					throw InError.comm.E0003("当前账户已销户！");
				}
				
				if(E_KPACFG._1 == ioInacInfo.getKpacfg()){
					throw InError.comm.E0003("当前账户不允许手工记账！");
				}
				
				if(!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), ioInacInfo.getBrchno())){
					throw InError.comm.E0003("跨法人机构无权进行该项业务！");
				}
				
				if(!CommUtil.equals(ioInacInfo.getAcctna(), iavcbkDetail.getAcctna())){
			        	throw InError.comm.E0003("转出户名与账户注册户名不一致！");
			    }
				
				if(!CommUtil.equals(iavcbkDetail.getCrcycd(),ioInacInfo.getCrcycd())){
		        	throw InError.comm.E0003("当前输入币种与内部户币种类型不符，请核查！");
		        }
				
				if(input.getIavctp() == E_IAVCTP._3){
					
					if(ioInacInfo.getIoflag()!=E_IOFLAG.OUT){
						throw InError.comm.E0003("该账户非表外账户！");
					}
				}
				
				if(ioInacInfo.getIspaya() == E_ISPAYA._1 && iavcbkDetail.getPayatp() == E_PAYATP._0){
					throw InError.comm.E0003("该内部户为挂销账管理账号，挂销账标志不能为非挂销账！");
				}
				
				if(ioInacInfo.getIspaya() == E_ISPAYA._0 && iavcbkDetail.getPayatp() != E_PAYATP._0){
					throw InError.comm.E0003("该内部户为非挂销账管理账号，挂销账标志应为非挂销账！");
				}
			}
			if(cmbktp == E_CMBKTP.INS){
				//本套号内，已录入传票检查
				if(CommUtil.isNotNull(knsCmbkList)){
					
					if(input.getIavctp() == E_IAVCTP._3){
			    		 throw InError.comm.E0003("表外收付一套号只能输入一笔，请核查！");  //表外收付，若已存在一笔记录，则报错
			    	 }
				}
			}
			if(cmbktp == E_CMBKTP.CHK){
				//币种检查
				if(!CommUtil.equals(iavcbkDetail.getCrcycd(),input.getIavcbkListInfo().get(0).getCrcycd())){
					throw InError.comm.E0003("当前输入币种与本套传票中其它记录币种不同！");
				}
			}
			
			//传票录入时，新录入传票的序号与账号不能与已录入传票相同
			if(cmbktp == E_CMBKTP.INS){
				for(KnsCmbk knsCmbk : knsCmbkList){
					
					//不同传票，账号应不相同
					if(CommUtil.equals(knsCmbk.getOtacno(), acctno)){
						throw InError.comm.E0003("同一笔套号，账号不能相同！");
					}
					
					if(CommUtil.compare(iavcbkDetail.getPayseq(), knsCmbk.getPayseq()) == 0){
						throw InError.comm.E0003("套内序号重复！");
					}
				}
			}
		    
			//录入和维护时，挂销账账号不能为空，复核时，挂销账序号不能为空，故将复核挂销账与传票处理分开单独处理
			if(cmbktp != E_CMBKTP.CHK){
			    
				//挂账检查
			    if(iavcbkDetail.getPayatp() == E_PAYATP._1){
			    	//modify by wuzx 跨系统双挂情况
			    	if(input.getCntsys() == E_CNTSYS._0){
		    			if(input.getPayaListInfo().size() == 2){
		    				if(CommUtil.isNull(input.getPayaListInfo().get(0).getPayamn())||CommUtil.isNull(input.getPayaListInfo().get(1).getPayamn())){
		    					throw InError.comm.E0003("挂账金额不能为空！");
		    				}
		    				if(CommUtil.compare(input.getPayaListInfo().get(0).getPayamn(), input.getPayaListInfo().get(1).getPayamn())!=0){
		    					throw InError.comm.E0003("转入方挂账金额与转出方挂账金额不相等！");
		    				}
		    			}else{
		    				payaCheck(input.getPayaListInfo(), iavcbkDetail.getAcctno(), iavcbkDetail.getTranam(), input.getCntsys());
		    			}
		    		}else{
		    			payaCheck(input.getPayaListInfo(), iavcbkDetail.getAcctno(), iavcbkDetail.getTranam(), input.getCntsys());
		    		}
		    	}
		    
			    //销账检查
			    if(iavcbkDetail.getPayatp() == E_PAYATP._2){
		    		paydCheck(input.getPaydListInfo(), iavcbkDetail.getAcctno(), iavcbkDetail.getTranam(), input.getCntsys());
		    	}
		    }
		}
		if(flag&&CommUtil.compare(totlam, BigDecimal.ZERO)!= 0){//如果存在红字记账，则必须同方向记账，则totlamt为0
			throw InError.comm.E0003("红字记账必须同方向记账");
		}
		/*//第一笔录入时摘要码子不能为空
		if(CommUtil.isNull(input.getSmrytx())&&CommUtil.isNull(knsCmbkList)){
			throw InError.comm.E0003("摘要不能为空");
		}*/
		//第一笔录入时摘要码子不能为空
     /*	if(CommUtil.isNull(input.getIavcbkListInfo().get(0).getSmrytx())&&CommUtil.isNotNull(knsCmbkList)){
			throw InError.comm.E0003("摘要不能为空");
		}*/ 
		
		//跨系统转账验证
		if(input.getCntsys() == E_CNTSYS._0){
			
			if(input.getIavcbkListInfo().size() != 2){
				throw InError.comm.E0003("跨系统转账时，传票明细应为两条，请核查！");
			}
			
			IavcbkDetail iavcbkDetail1 = input.getIavcbkListInfo().get(0);
			IavcbkDetail iavcbkDetail2 = input.getIavcbkListInfo().get(1);
			if(cmbktp == E_CMBKTP.CHK){
				if(CommUtil.compare(iavcbkDetail1.getPayseq(), iavcbkDetail2.getPayseq()) == 0){
					throw InError.comm.E0003("跨系统转账时，两传票明细序号不能相同，请核查！");
				}
			}
			if(iavcbkDetail1.getAmntcd() == iavcbkDetail2.getAmntcd()){
				throw InError.comm.E0003("跨系统转账时，两传票明细借贷标志不能相同，请核查！");
			}
			
			if(CommUtil.equals(iavcbkDetail1.getAcctno(), iavcbkDetail2.getAcctno())){
				throw InError.comm.E0003("跨系统转账时，两传票明细账号不能相同，请核查！");
			}
			
			if(!CommUtil.equals(iavcbkDetail1.getTranam(), iavcbkDetail2.getTranam())){
				throw InError.comm.E0003("跨系统转账时，两传票借贷金额不同，请核查！");
			}
			
		}
		
		//因复核时，挂销账明细中无挂销账账号，但有挂销账序号，不与传票信息一起处理
		if(cmbktp == E_CMBKTP.CHK){
			 
			if(CommUtil.isNotNull(input.getPayaListInfo())){
				//复核挂账处理
				chkPayaCheck(input.getPayaListInfo(), input.getAcstno(), input.getIavcbkListInfo());
			}
			
			if(CommUtil.isNotNull(input.getPaydListInfo())){
				//复核销账处理
				chkPaydCheck(input.getPaydListInfo(),  input.getAcstno(), input.getIavcbkListInfo());
			}
    	
		}
		
	}
	
	
	private static void payaCheck(Options<PayaDetail> payaDetailOptions, String acctno, BigDecimal tranam, E_CNTSYS cntsys){
		
		//挂账明细检查
		bizlog.debug("挂账明细检测开始==========");
		
    	BigDecimal amount = new BigDecimal(0);
    	for(PayaDetail payaDetail : payaDetailOptions){
    		
    		if(CommUtil.isNull(payaDetail.getPayaac())){
    			throw InError.comm.E0003("挂账明细中，挂账账号不能为空！");
    		}
    		
    		if(cntsys == E_CNTSYS._0){
    			
    			
    		/*	if(!CommUtil.equals(acctno, payaDetail.getPayaac())){
    				continue;
    			}*/
    			
    		}
    	//modify by wuzx 20161215-去除校验
			/*if(CommUtil.equals(payaDetail.getToacno(), acctno)){
				throw InError.comm.E0003("挂账对方账号与当前内部户账号相同，请核查！");
			}*/
    		
    		if(CommUtil.isNull(payaDetail.getToacno())){
				throw InError.comm.E0003("挂账明细中，对方账号不能为空");
			}
    		
    		if(CommUtil.isNull(payaDetail.getToacna())){
				throw InError.comm.E0003("挂账明细中，对方户名不能为空");
			}
    		//挂账账号应该余传票明细中内部账号一致
    		if(!CommUtil.equals(acctno, payaDetail.getPayaac())){
				continue;
			}
    		
    		//对方户名验证
    		//modify by chenlk 20181018 业务要求去掉这个校验
/*    		IoInacInfo ioInacInfo2 = SysUtil.getInstance(IoInSrvQryTableInfo.class).selKnaGlAcctnoByAcctno(payaDetail.getToacno(), false);
    		if(CommUtil.isNull(ioInacInfo2)){
    			throw InError.comm.E0003("对方账号不存在，请检查！");
    		}
    		
    		if(!CommUtil.equals(ioInacInfo2.getAcctna(), payaDetail.getToacna())){
    			
	        	throw InError.comm.E0003("对方户名与该对方账号账户注册户名不一致！");
    		}*/
    		
    		if(CommUtil.isNull(payaDetail.getPayamn())){
				throw InError.comm.E0003("挂账明细中，挂账金额不能为空");
			}
    		
    		if(CommUtil.isNull(payaDetail.getPayabr())){
				throw InError.comm.E0003("挂账明细中，挂账机构不能为空");
			}
    		
    		//查询判断挂账机构是否存在
    		IoBrchInfo branch = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(payaDetail.getPayabr());
    		if(CommUtil.isNull(branch) || !CommUtil.equals(branch.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch())){
    			throw InError.comm.E0003("挂账明细中，挂账机构非本交易机构，请核查");
    		}
    		
    		amount = amount.add(payaDetail.getPayamn());
		}
    	
    	/*if(!havefg){
    		throw InError.comm.E0003("账号：" + acctno + "对应传票销账记录不能为空");
    	}*/
		if(CommUtil.compare(amount, tranam) != 0){
			throw InError.comm.E0003("挂账金额不等于交易发生额");
		}
    	//modify by wuzx 20161209 支持多挂的情况
	}
	
	
	private static void paydCheck(Options<PaydDetail> paydDetailOptions, String acctno, BigDecimal tranam, E_CNTSYS cntsys){
		
		//销账明细检查
		bizlog.debug("销账明细检测开始==========");
		
    	boolean havefg = false;  //存在销账明细标志。循环结束后，若存在销账明细，状态改为true,不存在，状态为false，报错
    	BigDecimal amount = new BigDecimal(0); //合计销账金额
    	for(PaydDetail paydDetail : paydDetailOptions){
    		
    		if(CommUtil.isNull(paydDetail.getPaydac())){
    			throw InError.comm.E0003("销账明细中，销账账号不能为空");
    		}
    		//挂账账号应该余传票明细中内部账号一致
    		if(!CommUtil.equals(acctno, paydDetail.getPaydac())){
				continue;
			}
    		
    		if(cntsys == E_CNTSYS._0){
	    		
	    		if(!CommUtil.equals(acctno, paydDetail.getPaydac())){
    				continue;
    			}
    		}
    		
    		if(CommUtil.isNull(paydDetail.getPrpysq())){
				throw InError.comm.E0003("销账明细中，待销账流水不能为空");
			}
	    	
    		if(CommUtil.equals(paydDetail.getTotlmn(), BigDecimal.ZERO)){
				throw InError.comm.E0003("销账明细中，原未销金额不能为空或零");
			}
	    	
    		if(CommUtil.compare(paydDetail.getPaydmn(), BigDecimal.ZERO) <= 0){
    			throw InError.comm.E0003("销账明细中，销账金额大于零");
    		}
    		
    		if(!CommUtil.equals(paydDetail.getRsdlmn(), paydDetail.getTotlmn().subtract(paydDetail.getPaydmn()))){
    			throw InError.comm.E0003("销账明细中，剩余挂账金额应为原未销金额与本次销账金额之差");
    		}
	    	
			KnsPaya knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(paydDetail.getPrpysq(), true);
			
			if(!CommUtil.equals(knsPaya.getPayaac(), acctno)){
				throw InError.comm.E0003("销账明细所销挂账账号非本销账账号");
			}

			if(!CommUtil.equals(paydDetail.getTotlmn(), knsPaya.getRsdlmn())){
				 throw InError.comm.E0003("销账明细中，原未销金额应为对应挂账中剩余挂账金额");
			}
			
			if(CommUtil.compare(paydDetail.getPaydmn(), knsPaya.getRsdlmn()) > 0){
				 throw InError.comm.E0003("销账金额大于挂账记录剩余挂账金额");
			}
			
			amount = amount.add(paydDetail.getPaydmn()); //合计销账金额
			havefg = true;
    	}
    	
    	if(!havefg){
    		throw InError.comm.E0003("账号：" + acctno + "对应传票挂账记录不能为空");
    	}
    	
		if(CommUtil.compare(amount, tranam) != 0){
			throw InError.comm.E0003("合计销账金额不等于交易发生额");
		}
	}
	
	//复核挂账明细检查
	private static void chkPayaCheck(Options<PayaDetail> payaDetailOptions, String acstno, Options<IavcbkDetail> iavcbkDetailOptions){
		
		//挂账明细检查
		bizlog.debug("挂账明细检测开始==========");
		
    	for(PayaDetail payaDetail : payaDetailOptions){
    		
    		if(CommUtil.isNull(payaDetail.getToacno())){
				throw InError.comm.E0003("挂账明细中，对方账号不能为空");
			}
    		
    		if(CommUtil.isNull(payaDetail.getToacna())){
				throw InError.comm.E0003("挂账明细中，对方户名不能为空");
			}
    		
    		if(CommUtil.isNull(payaDetail.getPayamn())){
				throw InError.comm.E0003("挂账明细中，挂账金额不能为空");
			}
    		
    		if(CommUtil.isNull(payaDetail.getPayabr())){
				throw InError.comm.E0003("挂账明细中，挂账机构不能为空");
			}
    		
    		if(CommUtil.isNull(payaDetail.getPayasq())){
				throw InError.comm.E0003("复核时，挂账序号不能为空");
			}
    		
    		KnsPaya tblKnsPaya = KnsPayaDao.selectOne_kns_paya_odx1(payaDetail.getPayasq(), true);
    		
    		if(tblKnsPaya.getPayast() != E_PAYAST.WFH){
    			throw InError.comm.E0003("挂账序号" + payaDetail.getPayasq() + "当前不为未复核状态，请核查！");
    		}
    		
    		if(!CommUtil.equals(tblKnsPaya.getAcstno(), acstno)){
    			throw InError.comm.E0003("挂账序号" + payaDetail.getPayasq() + "对应挂账记录非本套号内容，请核查！");
    		}
    		
    		if(!CommUtil.equals(payaDetail.getToacno(), tblKnsPaya.getToacct())){
    			throw InError.comm.E0003("挂账序号" + payaDetail.getPayasq() + "所在挂账记录对方账号与原录入不符！");
    		}
    		
    		if(!CommUtil.equals(payaDetail.getToacna(), tblKnsPaya.getToacna())){
    			throw InError.comm.E0003("挂账序号" + payaDetail.getPayasq() + "所在挂账记录对方户名与原录入不符！");
    		}
    		
    		if(!CommUtil.equals(payaDetail.getPayamn(), tblKnsPaya.getPayamn())){
    			throw InError.comm.E0003("挂账序号" + payaDetail.getPayasq() + "所在挂账记录挂账金额与原录入不符！");
    		}
    		
    		if(!CommUtil.equals(payaDetail.getPayabr(), tblKnsPaya.getPayabr())){
    			throw InError.comm.E0003("挂账序号" + payaDetail.getPayasq() + "所在挂账记录挂账机构与原录入不符！");
    		}
    		//找到对应传票
    		for(IavcbkDetail iavcbkDetail : iavcbkDetailOptions){
    			if(CommUtil.compare(iavcbkDetail.getPayseq(), tblKnsPaya.getPayseq()) != 0){
    				continue;
    			}
    			
    			if(!CommUtil.equals(iavcbkDetail.getAcctno(), tblKnsPaya.getPayaac())){
    				throw InError.comm.E0003("挂账序号为" + payaDetail.getPayasq() + "的挂账明细中，对应传票账号与挂账账号不符，请核查！");
    			}
    			
    			
    		}
    		
		}
    	
	}
	
	
	private static void chkPaydCheck(Options<PaydDetail> paydDetailOptions, String acstno, Options<IavcbkDetail> iavcbkDetailOptions){
		
		//销账明细检查
		bizlog.debug("销账明细检测开始==========");
		
    	for(PaydDetail paydDetail : paydDetailOptions){
    		
    		if(CommUtil.isNull(paydDetail.getPaydsq())){
				throw InError.comm.E0003("复核时，销账序号不能为空");
			}
    		
    		if(CommUtil.isNull(paydDetail.getPrpysq())){
				throw InError.comm.E0003("销账明细中，待销账流水不能为空");
			}
	    	
    		if(CommUtil.equals(paydDetail.getTotlmn(), BigDecimal.ZERO)){
				throw InError.comm.E0003("销账明细中，原未销金额不能为空或零");
			}
	    	
    		if(CommUtil.compare(paydDetail.getPaydmn(), BigDecimal.ZERO) <= 0){
    			throw InError.comm.E0003("销账明细中，销账金额大于零");
    		}
    		
    		if(!CommUtil.equals(paydDetail.getRsdlmn(), paydDetail.getTotlmn().subtract(paydDetail.getPaydmn()))){
    			throw InError.comm.E0003("销账明细中，剩余挂账金额应为原未销金额与本次销账金额之差");
    		}
	    	
    		KnsPayd tblKnsPayd = KnsPaydDao.selectOne_kns_payd_odx1(paydDetail.getPaydsq(), true);
    		
    		if(tblKnsPayd.getPaydst() != E_PAYDST.WFH){
    			throw InError.comm.E0003("销账序号" + paydDetail.getPaydsq() + "当前不为未复核状态，请核查！");
    		}
    		
    		if(!CommUtil.equals(tblKnsPayd.getAcstno(), acstno)){
    			throw InError.comm.E0003("销账序号" + paydDetail.getPaydsq() + "对应销账记录非本套号内容，请核查！");
    		}
    		
    		if(!CommUtil.equals(paydDetail.getPrpysq(), tblKnsPayd.getPayasq())){
    			throw InError.comm.E0003("销账序号" + paydDetail.getPaydsq() + "所在销账记录待销账流水与原录入不符！");
    		}
    		
    		if(!CommUtil.equals(paydDetail.getTotlmn(), tblKnsPayd.getTotlmn())){
    			throw InError.comm.E0003("销账序号" + paydDetail.getPaydsq() + "所在销账记录原未销金额与原录入不符！");
    		}
    		
    		if(!CommUtil.equals(paydDetail.getPaydmn(), tblKnsPayd.getPayamn())){
    			throw InError.comm.E0003("销账序号" + paydDetail.getPaydsq() + "所在销账记录销账金额与原录入不符！");
    		}
    		
    		if(!CommUtil.equals(paydDetail.getRsdlmn(), tblKnsPayd.getRsdlmn())){
    			throw InError.comm.E0003("销账序号" + paydDetail.getPaydsq() + "所在销账记录剩余挂账金额与原录入不符！");
    		}
    		
    		//找到对应传票
    		for(IavcbkDetail iavcbkDetail : iavcbkDetailOptions){
    			if(CommUtil.compare(iavcbkDetail.getPayseq(), tblKnsPayd.getPayseq()) != 0){
    				continue;
    			}
    			
    			if(!CommUtil.equals(iavcbkDetail.getAcctno(), tblKnsPayd.getPaydac())){
    				throw InError.comm.E0003("销账序号为" + paydDetail.getPaydsq() + "的销账明细中，对应传票账号与挂账账号不符，请核查！");
    			}
    			
    			
    		}
			
    	}
    	
	}

}
