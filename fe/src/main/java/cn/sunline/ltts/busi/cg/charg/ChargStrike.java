package cn.sunline.ltts.busi.cg.charg;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.cg.namedsql.charg.PBChargeCodeDao;
import cn.sunline.ltts.busi.cg.namedsql.charg.PBChargeRegisterDao;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbAdjtRgst;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbAdjtRgstDao;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgDetl;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgDetlDao;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgRgst;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgRgstDao;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcpChrgAcrl;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgDanbJzFee_IN;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTypeStrikeInfo.ChargStrikeOutput;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSTRFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_GTAMFG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ADJTTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CGPYRV;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_RUISMA;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_STATUS;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_WAISMA;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class ChargStrike {
    
    public static Options<ChargStrikeOutput> strikeChargRegst(String ortrdt, String ortrsq, BaseEnumType.E_YES___ isdieb) {
        
        
        /**
         * 输入项检查
         */
        if (CommUtil.isNull(ortrdt))
            throw FeError.Chrg.BNASF321();
        if (CommUtil.isNull(ortrsq))
            throw FeError.Chrg.BNASF319();
        
/*        if (CommUtil.compare(sTrandt, ortrdt) != 0)
            throw PbError.Charg.E9999("不允许隔日冲账");*/
        Options<ChargStrikeOutput> lstOutput = new DefaultOptions<>();
        
        
        //收费登记簿处理
        List<KcbChrgRgst> lstChrgRgst =PBChargeRegisterDao.selChargeRegistrByTrsqRcfgNoCorp(ortrdt, ortrsq, false);
        		
        if (CommUtil.isNotNull(lstChrgRgst) && lstChrgRgst.size() > 0){
            
            for (KcbChrgRgst tblChrgRgst : lstChrgRgst)
            {
                //费用记帐明细处理
                List<KcbChrgDetl> lstChrgDetl = PBChargeRegisterDao.selKcbChrgDetlByTrdtTrsq(ortrdt, ortrsq, tblChrgRgst.getEvrgsq(), false);
                
                if (CommUtil.isNotNull(lstChrgDetl) && lstChrgDetl.size() > 0){
                    
                    //记帐机构
                    String sJzjigo = tblChrgRgst.getAcctbr(); 
                    
                    String tfinfo =tblChrgRgst.getTrinfo();//总账核算属性-交易信息
                    KcpChrgAcrl tblFeezl = SysUtil.getInstance(KcpChrgAcrl.class);  
                    if (CommUtil.isNull(tblChrgRgst.getCgfacd())) {

                        tblFeezl = PBChargeCodeDao.selChargeAccRuleDetail(tblChrgRgst.getChrgcd(), false);
                        if (CommUtil.isNull(tblFeezl)) {
                            throw FeError.Chrg.BNASF375();
                        }
                       
                    }
                    //记帐机构
                    if (CommUtil.isNull(sJzjigo)) {
                        sJzjigo = CommTools.getBaseRunEnvs().getTrxn_branch();  //交易机构
                    }
                    
                    
                    for (KcbChrgDetl tblChrgDetl : lstChrgDetl){
                        
                        //去掉集中判断，如果非集中收费改成集中将导致无法冲正
                        if(CommUtil.compare(tblChrgDetl.getAcclam(), BigDecimal.ZERO)!=0){
                        	
                        	// 登记会计流水开始
                        	IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
                        	cplIoAccounttingIntf.setCuacno(tblChrgDetl.getChrgcd()); //记账账号-登记收费代码
                        	cplIoAccounttingIntf.setAcseno(tblChrgDetl.getChrgcd()); //子账户序号-登记收费代码
                        	cplIoAccounttingIntf.setAcctno(tblChrgDetl.getChrgcd()); //负债账号-登记收费代码
                        	cplIoAccounttingIntf.setProdcd(tblChrgDetl.getProdcd()); //产品编号
                        	cplIoAccounttingIntf.setDtitcd(tblChrgDetl.getProdcd()); //核算口径-核算业务编号
                        	cplIoAccounttingIntf.setCrcycd(tblChrgDetl.getChrgcy()); //币种                 
                        	cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
                        	cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //主交易流水
                        	cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期 
                        	cplIoAccounttingIntf.setAcctbr(sJzjigo); //账务机构
                        	
                    		cplIoAccounttingIntf.setTranam(tblChrgDetl.getAcclam().negate()); //交易金额
                        	if (tblChrgDetl.getCgpyrv() == E_CGPYRV.PAY) { //付费，记借方
                        		cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR); //借贷标志-借方
                        	}
                        	else if (tblChrgDetl.getCgpyrv() == E_CGPYRV.RECIVE) { //收费，记贷方
                        		cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR); //借贷标志-贷方   
                        	}
                        	
                        	
                        	
                        	
                        	cplIoAccounttingIntf.setAtowtp(E_ATOWTP.FE); //会计主体类型-手续费
                        	cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型-账务流水
                        	cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE); //余额属性-本金科目
                        	cplIoAccounttingIntf.setTranms(tfinfo);//交易信息
                        	//登记会计流水
                        	SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);
                        	
                        	ChargStrikeOutput output = SysUtil.getInstance(ChargStrikeOutput.class);
                            output.setBrchno(tblChrgRgst.getAcctbr());
                            output.setChrgcd(tblChrgDetl.getChrgcd());
                            output.setTranam(tblChrgDetl.getAcclam());
                            
                            lstOutput.add(output);
                            
                        	//更新费用记帐明细冲正标志
                        	tblChrgDetl.setWaisma(E_WAISMA.TOSTRIK); //1-被冲正
                        	tblChrgDetl.setRuisma(E_RUISMA.DR); //1-当日冲正
                        	KcbChrgDetlDao.updateOne_odb1(tblChrgDetl);
                        }
                        
                    }
                    
                }
                
                //tblChrgRgst.setJiluztai(E_JILUZTAI.Delete);  //1-删除
                //KcbChrgRgstDao.update_odb1(tblChrgRgst);
                KcbChrgRgstDao.deleteOne_odb1(tblChrgRgst.getTrandt(), tblChrgRgst.getTrnseq(), tblChrgRgst.getEvrgsq());
                
            }
            
        }
        
        return lstOutput;
    }
    /**
     * 销记收费登记簿冲正
     * @param ortrdt
     * @param ortrsq
     * @param evrgsq 事件登记序号
     * @param tranam
     */
    public static void strikeChargOffRegst(String ortrdt, String ortrsq,Long evrgsq,BigDecimal tranam) {
    	
    	BigDecimal bigTranam = BigDecimal.ZERO; //交易金额
    	
    	/**
    	 * 输入项检查
    	 */
    	if (CommUtil.isNull(ortrdt))
    		throw FeError.Chrg.BNASF321();
    	if (CommUtil.isNull(ortrsq))
    		throw FeError.Chrg.BNASF319();
    	if (CommUtil.isNull(evrgsq))
    		throw FeError.Chrg.BNASF376();
    	if (CommUtil.isNull(tranam))
    		throw FeError.Chrg.BNASF377();
    	
    	List<KcbChrgRgst> lstChrgRgst = new ArrayList<KcbChrgRgst>();
    	//收费登记簿处理
    	lstChrgRgst = PBChargeRegisterDao.selChargeRegistrByTrsqRgsq( ortrdt, ortrsq,evrgsq, false);
    	if (CommUtil.isNotNull(lstChrgRgst) && lstChrgRgst.size() > 0){
    		
    		for (KcbChrgRgst tblChrgRgst : lstChrgRgst)
    		{
    			//费用记帐明细处理
    			List<KcbChrgDetl> lstChrgDetl = PBChargeRegisterDao.selKcbChrgDetlByTrdtTrsq(ortrdt, ortrsq, tblChrgRgst.getEvrgsq(), false);
    			
    			if (CommUtil.isNotNull(lstChrgDetl) && lstChrgDetl.size() > 0){
    				
    				//记帐机构
    				String sJzjigo = tblChrgRgst.getAcctbr(); 
    				

    				String tfinfo =tblChrgRgst.getTrinfo();//总账核算属性-交易信息
    				KcpChrgAcrl tblFeezl = SysUtil.getInstance(KcpChrgAcrl.class);  
    				if (CommUtil.isNull(tblChrgRgst.getCgfacd())) {
    					tblFeezl = PBChargeCodeDao.selChargeAccRuleDetail(tblChrgRgst.getChrgcd(), false);
    					if (CommUtil.isNull(tblFeezl)) {
    						throw FeError.Chrg.BNASF375();
    					}
    					
    				}
    				//记帐机构
    				if (CommUtil.isNull(sJzjigo)) {
    					sJzjigo = CommTools.getBaseRunEnvs().getTrxn_branch();  //交易机构
    				}
    				
    				
    				for (KcbChrgDetl tblChrgDetl : lstChrgDetl){
    					
    					//交易金额
    					bigTranam = tblChrgDetl.getAcclam().negate() ; //实收金额取反（红字冲）
    					
    					// 登记会计流水开始
    					IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
    					cplIoAccounttingIntf.setCuacno(tblChrgDetl.getChrgcd()); //记账账号-登记收费代码
    					cplIoAccounttingIntf.setAcseno(tblChrgDetl.getChrgcd()); //子账户序号-登记收费代码
    					cplIoAccounttingIntf.setAcctno(tblChrgDetl.getChrgcd()); //负债账号-登记收费代码
    					cplIoAccounttingIntf.setProdcd(tblChrgDetl.getProdcd()); //产品编号
    					cplIoAccounttingIntf.setDtitcd(tblChrgDetl.getProdcd()); //核算口径-核算业务编号
    					cplIoAccounttingIntf.setCrcycd(tblChrgDetl.getChrgcy()); //币种                 
    					cplIoAccounttingIntf.setTranam(bigTranam); //交易金额
    					cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
    					cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //主交易流水
    					cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期 
    					cplIoAccounttingIntf.setAcctbr(sJzjigo); //账务机构
    					
    					if (tblChrgDetl.getCgpyrv() == E_CGPYRV.PAY) { //付费，记借方
    						cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR); //借贷标志-借方
    					}
    					else if (tblChrgDetl.getCgpyrv() == E_CGPYRV.RECIVE) { //收费，记贷方
    						cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR); //借贷标志-贷方   
    					}
    					
    					cplIoAccounttingIntf.setAtowtp(E_ATOWTP.FE); //会计主体类型-手续费
    					cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型-账务流水
    					cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE); //余额属性-本金科目
    					cplIoAccounttingIntf.setTranms(tfinfo);//交易信息
    					//登记会计流水
    					SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);
    					  					    					
    				}
    				
    			}
 
    			tblChrgRgst.setAcclam(tblChrgRgst.getAcclam().subtract(tranam));/* 实收金额 */
    			tblChrgRgst.setArrgam(tranam); //欠费金额
    			tblChrgRgst.setRecvfg(E_GTAMFG.NO); /* 收讫标志 */
    			tblChrgRgst.setCgstno(tblChrgRgst.getCgstno() - 1); /*收费明细序号*/
                
    			KcbChrgRgstDao.updateOne_odb1(tblChrgRgst);
    			
    			
    		}
    		
    	}
    	
    	
    }
    /**
     * 收费调整冲正
     * @param ortrdt
     * @param ortrsq
     * @param tranam
     */
    public static void strikeChargAdjtRegst(String ortrdt, String ortrsq,BigDecimal tranam) {
    	
    	
    	/**
    	 * 输入项检查
    	 */
    	if (CommUtil.isNull(ortrdt))
    		throw FeError.Chrg.BNASF321();
    	if (CommUtil.isNull(ortrsq))
    		throw FeError.Chrg.BNASF319();
    	if (CommUtil.isNull(tranam))
    		throw FeError.Chrg.BNASF377();
    	
    	KcbAdjtRgst  tblKcbAdjtRgst = KcbAdjtRgstDao.selectOne_odb2(ortrdt, ortrsq, true);
    
    	E_ADJTTP adjttp = tblKcbAdjtRgst.getAdjttp();//调整方式
    	BigDecimal adjtam =tblKcbAdjtRgst.getAdjtam();//调整金额
	
		if(E_ADJTTP.ADD ==adjttp){
			//多收退回		    	
		// 登记会计流水开始
	        IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
	        cplIoAccounttingIntf.setCuacno(tblKcbAdjtRgst.getChrgno()); //收费代码
	        cplIoAccounttingIntf.setAcseno(tblKcbAdjtRgst.getChrgno()); //子账户序号-登记收费代码
	        cplIoAccounttingIntf.setAcctno(tblKcbAdjtRgst.getChrgno()); //负债账号-登记收费代码
	        cplIoAccounttingIntf.setProdcd(tblKcbAdjtRgst.getProdno()); //产品编号
	        cplIoAccounttingIntf.setDtitcd(tblKcbAdjtRgst.getProdno()); //核算口径-核算业务编号
	        cplIoAccounttingIntf.setCrcycd(tblKcbAdjtRgst.getCrcycd()); //币种                 
	        cplIoAccounttingIntf.setTranam(adjtam); //交易金额  红红 得蓝字
	        cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
	        cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //主交易流水
	        cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期 
	        cplIoAccounttingIntf.setAcctbr(tblKcbAdjtRgst.getTranbr()); //账务机构
	        cplIoAccounttingIntf.setTranms(tblKcbAdjtRgst.getTrinfo());//交易信息为原收费交易信息
	        cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR); //多收退回冲正 等于收费，记贷方
	        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.FE); //会计主体类型-手续费
	        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型-账务流水
	        cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE); //余额属性-本金科目
	        //登记会计流水
	        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);
    					
		}else if(E_ADJTTP.DEL ==adjttp) {
			
			//多付收回		    	
		// 登记会计流水开始
	        IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
	        cplIoAccounttingIntf.setCuacno(tblKcbAdjtRgst.getChrgno()); //收费代码
	        cplIoAccounttingIntf.setAcseno(tblKcbAdjtRgst.getChrgno()); //子账户序号-登记收费代码
	        cplIoAccounttingIntf.setAcctno(tblKcbAdjtRgst.getChrgno()); //负债账号-登记收费代码
	        cplIoAccounttingIntf.setProdcd(tblKcbAdjtRgst.getProdno()); //产品编号
	        cplIoAccounttingIntf.setDtitcd(tblKcbAdjtRgst.getProdno()); //核算口径-核算业务编号
	        cplIoAccounttingIntf.setCrcycd(tblKcbAdjtRgst.getCrcycd()); //币种                 
	        cplIoAccounttingIntf.setTranam(adjtam); //交易金额  红红 得蓝字
	        cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
	        cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //主交易流水
	        cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期 
	        cplIoAccounttingIntf.setAcctbr(tblKcbAdjtRgst.getTranbr()); //账务机构
	        cplIoAccounttingIntf.setTranms(tblKcbAdjtRgst.getTrinfo());//交易信息为原收费交易信息
	        cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR); //多收退回冲正 等于付费，记贷方
	        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.FE); //会计主体类型-手续费
	        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型-账务流水
	        cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE); //余额属性-本金科目
	        //登记会计流水
	        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);
    				
    	}else if(E_ADJTTP.ALL ==adjttp){
    		//差错调整
    		CgDanbJzFee_IN cplDbFee_In = SysUtil.getInstance(CgDanbJzFee_IN.class);
			CgDanbJzFee_IN cplDbFee_In1 = SysUtil.getInstance(CgDanbJzFee_IN.class);

    		
    		if(E_CGPYRV.PAY==tblKcbAdjtRgst.getCgpyrv()){
    			//原交易为付费
				// 贷：手续费支出红字
				cplDbFee_In.setCgpyrv(E_CGPYRV.PAY);
				cplDbFee_In1.setCgpyrv(E_CGPYRV.PAY);
 		   			
    		} if(E_CGPYRV.RECIVE==tblKcbAdjtRgst.getCgpyrv()){
				cplDbFee_In.setCgpyrv(E_CGPYRV.RECIVE);
				cplDbFee_In1.setCgpyrv(E_CGPYRV.RECIVE);
    		}

			cplDbFee_In.setTranam(adjtam);//调整记红字，冲调整记蓝字
			cplDbFee_In.setChrgcd(tblKcbAdjtRgst.getChrgno());//调整前收费代码
			cplDbFee_In.setTrancy(tblKcbAdjtRgst.getCrcycd());
			cplDbFee_In.setProdcd(tblKcbAdjtRgst.getProdno());//调整前产品
			cplDbFee_In.setTrinfo(tblKcbAdjtRgst.getTrinfo());//调整前交易信息
			cplDbFee_In.setAcctbr(tblKcbAdjtRgst.getTranbr());
			cplDbFee_In.setCstrfg(E_CSTRFG.TRNSFER);// 现转标志
			AccountStrikFee(cplDbFee_In);
			
			
			//调整后手续费
			cplDbFee_In1.setChrgcd(tblKcbAdjtRgst.getJtchno());// 调整后收费代码
			cplDbFee_In1.setProdcd(tblKcbAdjtRgst.getJtprod());// 调整后产品编号 
			cplDbFee_In1.setTrancy(tblKcbAdjtRgst.getCrcycd());// 币种
			cplDbFee_In1.setAcctbr(tblKcbAdjtRgst.getTranbr());// 机构
			cplDbFee_In1.setCstrfg(E_CSTRFG.TRNSFER);// 现转标志
			cplDbFee_In1.setTranam(adjtam.negate());// 金额 蓝字
			cplDbFee_In1.setTrinfo(tblKcbAdjtRgst.getJttrif());// 调整后交易信息
			AccountStrikFee(cplDbFee_In1);  
    		
    	}
		
		//更新原记录为冲正
		tblKcbAdjtRgst.setStatus(E_STATUS.CZ);
		tblKcbAdjtRgst.setStrksq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		
		KcbAdjtRgstDao.updateOne_odb2(tblKcbAdjtRgst);
    	
    }
    /**
     * 冲正记账
     * @param cplDbFeeIn
     * @return
     */
	public static void AccountStrikFee(CgDanbJzFee_IN cplDbFeeIn) {
 
        String sJzjigo = "";
        sJzjigo = cplDbFeeIn.getAcctbr();

        if (CommUtil.isNull(cplDbFeeIn.getCgpyrv())) {
            throw FeError.Chrg.BNASF066();
        }

        if (CommUtil.isNull(cplDbFeeIn.getChrgcd())) {
            throw FeError.Chrg.BNASF226();
        }

        if (CommUtil.isNull(cplDbFeeIn.getTrancy())) {
            throw FeError.Chrg.BNASF155();
        }

        if (CommUtil.isNull(cplDbFeeIn.getCstrfg())) {
            throw FeError.Chrg.BNASF273();
        }
        if (CommUtil.isNull(sJzjigo)) {
            sJzjigo = CommTools.getBaseRunEnvs().getTrxn_branch();
        }
 

 
        // 登记会计流水开始
        IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
        cplIoAccounttingIntf.setCuacno(cplDbFeeIn.getChrgcd()); //记账账号-登记收费代码
        cplIoAccounttingIntf.setAcseno(cplDbFeeIn.getChrgcd()); //子账户序号-登记收费代码
        cplIoAccounttingIntf.setAcctno(cplDbFeeIn.getChrgcd()); //负债账号-登记收费代码
        cplIoAccounttingIntf.setProdcd(cplDbFeeIn.getProdcd()); //产品编号
        cplIoAccounttingIntf.setDtitcd(cplDbFeeIn.getProdcd()); //核算口径-核算业务编号
        cplIoAccounttingIntf.setCrcycd(cplDbFeeIn.getTrancy()); //币种                 
        cplIoAccounttingIntf.setTranam(cplDbFeeIn.getTranam()); //交易金额 
        cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
        cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //主交易流水
        cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期 
        cplIoAccounttingIntf.setAcctbr(sJzjigo); //账务机构
        cplIoAccounttingIntf.setTranms(cplDbFeeIn.getTrinfo());//交易信息

        if (cplDbFeeIn.getCgpyrv() == E_CGPYRV.PAY) { //付费，记借方
            cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR); //借贷标志-借方
        }
        else if (cplDbFeeIn.getCgpyrv() == E_CGPYRV.RECIVE) { //收费，记贷方
            cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR); //借贷标志-贷方   
        }

        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.FE); //会计主体类型-手续费
        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型-账务流水
        cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE); //余额属性-本金科目
        //登记会计流水
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);

    }
    
}
