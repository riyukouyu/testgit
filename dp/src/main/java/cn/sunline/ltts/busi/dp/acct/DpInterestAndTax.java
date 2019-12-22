package cn.sunline.ltts.busi.dp.acct;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.dayend.DpInterest;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.CalInterTax;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbInRaSelSvc;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbintrratsel.Pbintrratsel;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CAINPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IBAMMD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_REPRWY;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;

/**
 * 
 * @author chenlinkang
 *描述：账户利率分段或税率分段时计算账户的利息及税金
 */
public class DpInterestAndTax {

	private final static BizLog bizlog = BizLogUtil.getBizLog(BizLog.class);
	
	public static InterestAndIntertax calcInterAndTax(KnbAcin tblKnbAcin, CalInterTax calInterTax, boolean isable){
		
		IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
		
		InterestAndIntertax interestAndTax = SysUtil.getInstance(InterestAndIntertax.class);
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		BigDecimal interest = BigDecimal.ZERO;
		BigDecimal intertax = BigDecimal.ZERO;
		
		//活期总积数
		BigDecimal totalAcmltn = BigDecimal.ZERO;
		BigDecimal cuusin = BigDecimal.ZERO;
		if(tblKnbAcin.getPddpfg()==E_FCFLAG.CURRENT){

			// 个人电子账户活期存款
			KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(tblKnbAcin.getAcctno(), true);//利率调整计提结息之前，计算利息的保证账户利率是最新的
			KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(tblKnbAcin.getAcctno(), true);
			
			if(CommUtil.isNotNull(calInterTax.getCuusin())&&CommUtil.compare(calInterTax.getCuusin(), BigDecimal.ZERO)>0){
				cuusin = calInterTax.getCuusin();
			}else{
				
				 cuusin = tblKubInrt.getCuusin();
			}
			
			List<KnbIndl> tblKnbIndls = KnbIndlDao.selectAll_odb4(tblKnbAcin.getAcctno(), E_INDLST.YOUX, false);
			
			for(KnbIndl indl : tblKnbIndls){

				totalAcmltn = totalAcmltn.add(indl.getAcmltn());

					
			}			
			//实际积数
			BigDecimal realCutmam = DpPublic.calRealTotalAmt(tblKnbAcin.getCutmam(), calInterTax.getTranam(), trandt, tblKnbAcin.getLaamdt());
			
			totalAcmltn = totalAcmltn.add(realCutmam); //加实际积数
			
			if(E_IRCDTP.LAYER == tblKnbAcin.getIncdtp()){//靠档利率
			    
			    if(E_IBAMMD.AVG == tblKnbAcin.getInammd()){//平均余额
			        int days = DpInterest.calAvgDays(tblKnbAcin.getIrwptp(),tblKnbAcin.getBldyca(), tblKnbAcin.getTxbefr(), tblKnbAcin.getLcindt(), tblKnbAcin.getNcindt(),trandt);
			        BigDecimal avgtranam = BigDecimal.ZERO;
			        if(CommUtil.equals(totalAcmltn, BigDecimal.ZERO)){
			            avgtranam =calInterTax.getTranam();//平均余额
			        }else{
			            avgtranam = totalAcmltn.divide(BigDecimal.valueOf(days), 2,BigDecimal.ROUND_HALF_UP);//平均余额
			        }

			        IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
			        intrEntity.setCorpno(tblKnbAcin.getCorpno());  //法人代码
			        intrEntity.setBrchno(tblKnaAcct.getBrchno());//机构号
			        intrEntity.setTranam(avgtranam);//交易金额
			        intrEntity.setTrandt(trandt);//交易日期
			        intrEntity.setIntrcd(tblKnbAcin.getIntrcd());   //利率代码 
			        intrEntity.setIncdtp(tblKnbAcin.getIncdtp());  //利率代码类型
			        intrEntity.setCrcycd(tblKnbAcin.getCrcycd());//币种
			        intrEntity.setInbebs(tblKnbAcin.getTxbebs());   //计息基础
			        intrEntity.setIntrwy(tblKnbAcin.getIntrwy());  //靠档方式
			        intrEntity.setCainpf(E_CAINPF.T1);              //计息规则
			        intrEntity.setBgindt(tblKnbAcin.getBgindt());  //起息日期
			        intrEntity.setEdindt(trandt);  //止息日

			        intrEntity.setLevety(tblKnbAcin.getLevety());
			        if(tblKnbAcin.getIntrdt() == E_INTRDT.OPEN){
			            intrEntity.setTrandt(tblKnbAcin.getOpendt());
			            intrEntity.setTrantm("999999");
			        }
			        pbpub.countInteresRate(intrEntity);

			        BigDecimal intrvl = intrEntity.getIntrvl();

			        // 利率优惠后执行利率
			        intrvl = intrvl.add(intrvl.multiply(CommUtil.nvl(tblKubInrt.getFavort(),BigDecimal.ZERO).
			                divide(BigDecimal.valueOf(100))));

			        //mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
			        //利率的最大范围值
			        BigDecimal intrvlmax = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
			        //利率的最小范围值
			        BigDecimal intrvlmin = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

			        if(CommUtil.compare(intrvl, intrvlmin)<0){
			            intrvl = intrvlmin;
			        }else if(CommUtil.compare(intrvl, intrvlmax)>0){
			            intrvl = intrvlmax;
			        }

			        cuusin=intrvl;
			    }else if(E_IBAMMD.SUM == tblKnbAcin.getInammd()){//积数
			        /** add by huangwh 20181121 start   积数靠档 */
			        
			        IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                    intrEntity.setCorpno(tblKnbAcin.getCorpno());  //法人代码
                    intrEntity.setBrchno(tblKnaAcct.getBrchno());//机构号
                    intrEntity.setTranam(totalAcmltn);/** 交易金额   = 活期总积数 */
                    intrEntity.setTrandt(trandt);//交易日期
                    intrEntity.setIntrcd(tblKnbAcin.getIntrcd());   //利率代码 
                    intrEntity.setIncdtp(tblKnbAcin.getIncdtp());  //利率代码类型
                    intrEntity.setCrcycd(tblKnbAcin.getCrcycd());//币种
                    intrEntity.setInbebs(tblKnbAcin.getTxbebs());   //计息基础
                    intrEntity.setIntrwy(tblKnbAcin.getIntrwy());  //靠档方式
                    intrEntity.setCainpf(E_CAINPF.T1);              //计息规则
                    intrEntity.setBgindt(tblKnbAcin.getBgindt());  //起息日期
                    intrEntity.setEdindt(trandt);  //止息日

                    intrEntity.setLevety(tblKnbAcin.getLevety());
                    if(tblKnbAcin.getIntrdt() == E_INTRDT.OPEN){
                        intrEntity.setTrandt(tblKnbAcin.getOpendt());
                        intrEntity.setTrantm("999999");
                    }
                    pbpub.countInteresRate(intrEntity);

                    BigDecimal intrvl = intrEntity.getIntrvl();

                    // 利率优惠后执行利率
                    intrvl = intrvl.add(intrvl.multiply(CommUtil.nvl(tblKubInrt.getFavort(),BigDecimal.ZERO).
                            divide(BigDecimal.valueOf(100))));

                    //mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
                    //利率的最大范围值
                    BigDecimal intrvlmax = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                    //利率的最小范围值
                    BigDecimal intrvlmin = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

                    if(CommUtil.compare(intrvl, intrvlmin)<0){
                        intrvl = intrvlmin;
                    }else if(CommUtil.compare(intrvl, intrvlmax)>0){
                        intrvl = intrvlmax;
                    }

                    cuusin=intrvl;
			    }else{
			        throw DpModuleError.DpstComm.BNAS1593();
			    }
			    /** add by huangwh 20181121 end */
			}
			bizlog.parm("记账账号[%s] 执行利率[%s] ", tblKnbAcin.getAcctno(), cuusin);
			
			

			//基础利率类型、浮动利率类型、 分档利率类型 利息和利息税计算方法相同，区别在于利率的取值方式不同,
			//计息结息动作再利率调整之后，保证账户利率信息中的当前执行利率是最新的
			
			BigDecimal cutman = BigDecimal.ZERO; //定义积数				
			interest = BigDecimal.ZERO;
			intertax = BigDecimal.ZERO;
		
			BigDecimal currInterest = BigDecimal.ZERO; //定义当期段利息
			BigDecimal currIntertax = BigDecimal.ZERO;
			BigDecimal lastInterest = BigDecimal.ZERO; //前面段总利息
			BigDecimal lastIntertax = BigDecimal.ZERO;
			if(tblKnbAcin.getReprwy() == E_REPRWY.ALL || CommUtil.isNull(tblKnbAcin.getReprwy())){ //全段调整，表示利息不分段（个人活期都按照此规则）
//				利息税计算方法：
//				1、根据总积数和当前执行利率计算出总利息
//				2、非最后一段，分段计算利息并计算出利息和利息税
//				3、最后一段利息税=(总利息-各段利息)*利息税
				
			BigDecimal taxrat= BigDecimal.ZERO;
			if(tblKnbAcin.getTxbefg() == E_YES___.YES) {
				
				taxrat = SysUtil.getInstance(IoPbInRaSelSvc.class).inttxRate(tblKnbAcin.getTaxecd()).getTaxrat();
			}
				
			for(KnbIndl indl : tblKnbIndls){
				//计算当期段利息
				currInterest =  pbpub.countInteresRateByBase(cuusin, indl.getAcmltn());
				//计算当期段税金
				currIntertax = currInterest.multiply(indl.getCatxrt());
				
				lastInterest = lastInterest.add(currInterest);
				lastIntertax = lastIntertax.add(currIntertax);
				
				 bizlog.parm("记账账号[%s] 分段利息[%s]  利息税[%s]  ",tblKnbAcin.getAcctno(),lastInterest, lastIntertax);
				
				 cutman = cutman.add(indl.getAcmltn());
				
				indl.setRlintr(currInterest);
				indl.setRlintx(currIntertax);
				indl.setIndlst(E_INDLST.WUX);
				if(isable){
					KnbIndlDao.updateOne_odb1(indl);
				}
					
			}
			//当期积数
			BigDecimal currCutmam = DpPublic.calRealTotalAmt(tblKnbAcin.getCutmam(), calInterTax.getTranam(), trandt, tblKnbAcin.getLaamdt());
			//计算总积数
			cutman = cutman.add(currCutmam); 
			
			//计算全段利息
			
			interest = BusiTools.roundByCurrency(tblKnbAcin.getCrcycd(),pbpub.countInteresRateByBase(cuusin, cutman),null); 
			//计算最后段利息
			currInterest = interest.subtract(lastInterest);
			
			//最后段利息税
			currIntertax = currInterest.multiply(taxrat);
			//总税金
			intertax =  BusiTools.roundByCurrency(tblKnbAcin.getCrcycd(),lastIntertax.add(currIntertax),null);
			
			
			interestAndTax.setInstam(interest);
			interestAndTax.setIntxam(intertax);
			interestAndTax.setDiffam(interest.subtract(intertax));
//			 bizlog.parm("记账账号[%s] 利息[%s] 利息税[%s]  ", tblKnbAcin.getAcctno(),interest, intertax);
			 bizlog.parm("记账账号[%s] 利息[%s] 利息税[%s] 当期积数[%s] 总积数[%s]  ", tblKnbAcin.getAcctno(),interest, intertax,currCutmam,cutman);
		}else if(tblKnbAcin.getReprwy() == E_REPRWY.BACK){
				//后段调整处理（对公活期按照此规则处理，暂无此种业务场景-20170219）
//				利息税计算方法：
//				1、各段按登记的积数和利率、税率分别计算出利息和利息税,不做精度处理
//				2、总利息=各段利息汇总金额       总利息税= 各段利息税汇总金额  
//				3、汇总金额做精度处理
			BigDecimal taxrat = BigDecimal.ZERO;
			if (E_YES___.YES == tblKnbAcin.getTxbefg()) {
				
				taxrat = SysUtil.getInstance(IoPbInRaSelSvc.class).inttxRate(tblKnbAcin.getTaxecd()).getTaxrat();
			}
				for(KnbIndl indl : tblKnbIndls){
					//计算当期段利息
					currInterest = pbpub.countInteresRateByBase(indl.getCuusin(), indl.getAcmltn());
					//计算当期段税金
					currIntertax =  currInterest.multiply(indl.getCatxrt());
					
					lastInterest = lastInterest.add(currInterest);
					lastIntertax = lastIntertax.add(currIntertax);
										
					indl.setRlintr(currInterest);
					indl.setRlintx(currIntertax);
					indl.setIndlst(E_INDLST.WUX);
					if(isable){
						KnbIndlDao.updateOne_odb1(indl);
					}					
				}
				//当期积数
				BigDecimal currCutmam = DpPublic.calRealTotalAmt(tblKnbAcin.getCutmam(), calInterTax.getTranam(), trandt, tblKnbAcin.getLaamdt());
			
				
				//计算最后段利息
				currInterest = pbpub.countInteresRateByBase(cuusin, currCutmam);
				//计算最后段税率
				currIntertax =  currInterest.multiply(taxrat);
				
				//计算总利息
				interest = BusiTools.roundByCurrency(tblKnbAcin.getCrcycd(),currInterest.add(lastInterest),null);
				
				//总税金
				intertax = BusiTools.roundByCurrency(tblKnbAcin.getCrcycd(),lastIntertax.add(currIntertax),null);
				
				//查询当前账户的分段信息
				
				
				interestAndTax.setInstam(interest);
				interestAndTax.setIntxam(intertax);
				interestAndTax.setDiffam(interest.subtract(intertax));
				 bizlog.parm("记账账号[%s] 利息[%s] 利息税[%s]  ", tblKnbAcin.getAcctno(),interest, intertax);
			}
			
		}else{
			
			// 检查天数类型是否为空
			if (CommUtil.isNull(calInterTax.getInbebs())) {
				throw DpModuleError.DpstAcct.BNAS0645();
			}
			
			// 个人电子账户定期存款
//			计算方法：
//			1、根据起始日期获取最近的税率  生效日期
//			2、获取生效日期之后的税率列表
//			3、根据列表进行循环处理各段利息税
			
			BigDecimal segInstam=BigDecimal.ZERO;//利息
			BigDecimal segIntxam=BigDecimal.ZERO;//本段利息税
			BigDecimal sumInstam =BigDecimal.ZERO;//累计利息
			BigDecimal sumIntxam =BigDecimal.ZERO;//累计利息税
			BigDecimal instam = BusiTools.roundByCurrency(tblKnbAcin.getCrcycd(),calInterTax.getInstam(),null);//总利息
			//获取离起息日最近的税率发生日期
			String nearEfectdt  ="";
			nearEfectdt =  SysUtil.getInstance(IoPbInRaSelSvc.class).getTxrtEfctdtNearStdate(calInterTax.getBegndt()).getEfctdt();
			//获取生效日期之后的税率列表
			Options<Pbintrratsel> cplTxlist = SysUtil.getInstance(IoPbInRaSelSvc.class).getIntxrtList(nearEfectdt);
			
			for (Pbintrratsel cplTx:cplTxlist ){
				
				segInstam = BigDecimal.ZERO;// 初始化当段利息为0
				String efftdt = cplTx.getEfctdt();//本段税率生效日期
				String inefdt = cplTx.getInefdt();//本段税率失效日期
				
				String txstdt ="";//本段利息税开始日期
				String txeddt ="";//本段利息税结束日期
				boolean isLast =false;//是否最后一段
				if(efftdt.compareTo(calInterTax.getBegndt())>0){
					txstdt = efftdt;
				} else {
					txstdt = calInterTax.getBegndt();
				}
				

				
				//如果失效日期>截止日期，则以截止日期作为本段利息税结束日期，否则以失效日期作为本段利息税结束日期
				if(inefdt.compareTo(calInterTax.getEnddat())>0){
					txeddt = calInterTax.getEnddat();
					isLast = true;		
				}else{
					txeddt=inefdt;
				}
				

//				BigDecimal days =new BigDecimal(DateTools2.calDays(txstdt, txeddt, 0, 0));
//				segAcmlbl = calInterTax.getTranam().multiply(days);//积数
				

				if(isLast){//最后一段利息=传入总利息-累计利息
					if (CommUtil.compare(txeddt, txstdt) > 0){
						segInstam = instam.subtract(sumInstam);
					} 
					
				}else{
					segInstam= pbpub.countInteresRateByAmounts(calInterTax.getCuusin(), txstdt, txeddt, calInterTax.getTranam(), calInterTax.getInbebs());
				}
				bizlog.parm("last[%s]  利息[%s]  累进利息[%s]  ", isLast,segInstam,sumInstam);	
				//税率
				BigDecimal segIntxrt =  cplTx.getTaxrat().divide(new BigDecimal(cplTx.getTxunit()), 7, BigDecimal.ROUND_HALF_UP);
				//利息税
				segIntxam = segInstam.multiply(segIntxrt);
				
//				bizlog.parm("记账账号[%s]  起始日期[%s] 结束日期[%s] 天数[%s]  积数[%s]  ", tblKnbAcin.getAcctno(), txstdt,txeddt,days,segAcmlbl);		
				

				sumInstam = sumInstam.add(segInstam);
				sumIntxam = sumIntxam.add(segIntxam);
				bizlog.parm("记账账号[%s]  利息税[%s] 汇总利息税[%s] 利息[%s]  汇总利息[%s]  ", tblKnbAcin.getAcctno(), segIntxam,sumIntxam,segInstam,sumInstam);	
								
				if(isLast){
					interestAndTax.setInstam(calInterTax.getInstam());
					interestAndTax.setIntxam(BusiTools.roundByCurrency(tblKnbAcin.getCrcycd(),sumIntxam,null));
					interestAndTax.setDiffam(interest.subtract(BusiTools.roundByCurrency(tblKnbAcin.getCrcycd(),sumInstam,null)));
					bizlog.parm("记账账号[%s] 利息[%s] 利息税[%s]  ", tblKnbAcin.getAcctno(),calInterTax.getInstam(), interestAndTax.getIntxam());
					break;
				}
				
			}
		}
		

		
		
		return interestAndTax;
	}
}
