package cn.sunline.ltts.busi.cg.charg;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.DateTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.cg.namedsql.charg.PBChargeCodeDao;
import cn.sunline.ltts.busi.cg.namedsql.charg.PBChargeRegisterDao;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgRgst;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgRgstDao;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcpChrgAcrl;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgCalFee_IN;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgCalFee_OUT;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgChargeFee_IN;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgChargeFee_OUT;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgChargefeeKhz_IN;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgFEEINFO;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgGetAccountInf_IN;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgGetAccountInf_OUT;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.ltts.busi.iobus.type.tx.TxType.CalTaxAmountIn;
import cn.sunline.ltts.busi.iobus.type.tx.TxType.CalTaxAmountOut;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.errors.PbError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSTRFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_GTAMFG;
import cn.sunline.ltts.busi.sys.type.LnEnumType.E_ROUNTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ACTTYP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CGPYRV;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHRGSR;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_FETYPE;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_MODUTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_OPRFLG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
//import cn.sunline.ltts.busi.aplt.tables.SysDbTable.kmip_zhyocs;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class PbCalCharg {

    private static final BizLog bizlog = BizLogUtil.getBizLog(PbCalCharg.class);





    /**
     * @Author wuxq
     *         <p>
     *         <li>功能说明：统一计费/收费处理 返回多比数据，可以多个收费代码</li>
     *         </p>
     * @param cplFeeIn
     * @return
     */
    public CgCalFee_OUT calCharge(CgCalFee_IN cplFeeIn) {

        bizlog.parm("cplFeeIn[%s]", cplFeeIn);

        String sTrandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
        String sTransq = CommTools.getBaseRunEnvs().getMain_trxn_seq(); //柜员流水
        String sPrcscd = CommTools.getBaseRunEnvs().getTrxn_code(); //交易码
        String corpno = CommTools.getBaseRunEnvs().getBusi_org_id(); //法人代码
        bizlog.debug("当前交易码[%s]", sPrcscd);

        String   sTranbr = null;
        String sActseq = "", sProdcd = "", sCustac = "", sCustno = "", sAcctna = "", sAcctno = "", sCustno_SF = "";
        String sActseq_SF = null, sAcctno_SF = null, sCustac_SF = null;
        String  sChrgdt = "";
        String eChrgcy = null; //扣费币种
        E_ACTTYP eActtyp = null; //客户账户类型
        E_ACTTYP eActtyp_SF = null; //客户账户类型
        E_CSEXTG eCsexfg = null; //钞汇标志
        E_CSEXTG eCsexfg_SF = null; //钞汇标志
        String eCrcycd_SF = null, eCrcycd = null; //货币代号
        String eClchcy = null; //计费币种
        E_CHGFLG eChgflg = null; //记账标志
       // E_YES___ eAppoint = E_YES___.NO; //是否指定金额
        List<IoCgCalCenterReturn>  calCenterReturn= new ArrayList<IoCgCalCenterReturn>();
        CgGetAccountInf_OUT cplOut_SF = SysUtil.getInstance(CgGetAccountInf_OUT.class); //查询账户信息输出

        CgGetAccountInf_OUT cplOut = SysUtil.getInstance(CgGetAccountInf_OUT.class);  //查询账户信息输出
        CgCalFee_OUT cplFeeOut = SysUtil.getInstance(CgCalFee_OUT.class); //计费记账输出
        CgChargeFee_OUT cplChFee_Out = SysUtil.getInstance(CgChargeFee_OUT.class); //收费输出

        BigDecimal bigDvidam = BigDecimal.ZERO; // 收费分成金额  
        BigDecimal bigSpcham = BigDecimal.ZERO; // 指定收费金额
        BigDecimal bigChexrt = BigDecimal.ZERO; //收费兑换比例
        BigDecimal bigRecvam = BigDecimal.ZERO; //应收费用金额

		calCenterReturn =cplFeeIn.getCalcenter();//计费中心返回计费信息
		if(null==calCenterReturn){
			throw FeError.Chrg.BNASF150();
		}
		
        E_YES___ eTotflg = E_YES___.NO; //是否汇总记账
        if (CommUtil.isNotNull(cplFeeIn.getTotflg())) {
            eTotflg = cplFeeIn.getTotflg();
        }

        if (CommUtil.isNull(cplFeeIn.getTrancy())) {
            throw FeError.Chrg.BNASF156();
        }
        eCrcycd = cplFeeIn.getTrancy();

        if (CommUtil.isNull(cplFeeIn.getChrgcy())) {
            throw FeError.Chrg.BNASF378();
        }
        eChrgcy = cplFeeIn.getChrgcy();

        if (CommUtil.isNull(cplFeeIn.getChgflg())) {
            throw FeError.Chrg.BNASF154();
        }
        eChgflg = cplFeeIn.getChgflg();// 记账标志

        /* 根据摘要代码获取摘要描述 */
/*        if (CommUtil.isNotNull(cplFeeIn.getSmrycd()) && CommUtil.isNull(cplFeeIn.getSmryds())) {
            kmip_zhyocs tblKmip_zhyocs = BusiTools.getSummaryInfo(cplFeeIn.getSmrycd());
            if (CommUtil.isNotNull(tblKmip_zhyocs)) {
                String sZhaiyaoms = tblKmip_zhyocs.getZhaiyoms();
                cplFeeIn.setSmryds(sZhaiyaoms);
            }
        }*/

        if (CommUtil.isNotNull(cplFeeIn.getLastdt())) {
            sChrgdt = cplFeeIn.getLastdt(); //扣费日期
        }

        if (CommUtil.isNull(cplFeeIn.getTranbr())) {
            sTranbr = CommTools.getBaseRunEnvs().getTrxn_branch(); // 机构号
        }
        else {
            sTranbr = cplFeeIn.getTranbr();
        }


        E_MODUTP eModtyp = cplFeeIn.getModtyp(); //所属模块

        bizlog.debug("输入客户账号[%s]", cplFeeIn.getCustac());
        String sCustAcct = cplFeeIn.getCustac(); // 客户账号

        if (CommUtil.isNotNull(sCustAcct) && cplFeeIn.getCstrfg() == E_CSTRFG.TRNSFER && cplFeeIn.getChgflg() != E_CHGFLG.NONE) {

            CgGetAccountInf_IN cplIn = SysUtil.getInstance(CgGetAccountInf_IN.class);
            cplIn.setAcctno(sCustAcct);  //收费电子账号
            cplIn.setActseq(cplFeeIn.getSeqnum());  //子户号
            cplIn.setCrcycd(cplFeeIn.getTrancy());  //币种
            cplIn.setCustno(cplFeeIn.getCustno());  //客户号
            cplIn.setCsextg(cplFeeIn.getCsexfg());  //钞汇标志
            cplOut = ChargeProc.getAccountInf(cplIn);  //查询收费账号信息
            if (CommUtil.isNull(cplOut)) {
                throw FeError.Chrg.BNASF182();
            }
            
            sCustno = cplOut.getCustno(); //客户号
            eActtyp = cplOut.getActtyp(); //账户类型
            sCustac = cplOut.getCustac(); //客户账号 
            eCrcycd = cplOut.getCrcycd(); //币种
            sActseq = cplOut.getActseq(); //子户号
            eCsexfg = cplOut.getCsextg(); //钞汇标志 
            sAcctno = cplOut.getAcctno(); //系统账号
            sProdcd = cplOut.getProdcd(); //产品编号
            sAcctna = cplOut.getAcctna(); //账户名称
            
            if (CommUtil.isNotNull(cplFeeIn.getTrancy()) && CommUtil.compare(cplFeeIn.getTrancy(), eCrcycd)!=0) {
                throw PbError.Charg.E0002("交易", eCrcycd, cplFeeIn.getTrancy());
            }
//            if (CommUtil.isNotNull(cplFeeIn.getCsexfg()) && cplFeeIn.getCsexfg() != eCsexfg) {
//                throw PbError.Charg.E0003("交易", eCsexfg, cplFeeIn.getCsexfg());
//            }
            if (CommUtil.isNotNull(cplFeeIn.getSeqnum()) && CommUtil.compare(cplFeeIn.getSeqnum(), sActseq) != 0) {
                throw PbError.Charg.E0004("交易", sActseq, cplFeeIn.getSeqnum());
            }
            if (CommUtil.isNotNull(cplFeeIn.getCustno()) && CommUtil.compare(cplFeeIn.getCustno(), sCustno) != 0) {
                throw PbError.Charg.E0005("交易", sCustno, cplFeeIn.getCustno());
            }

            
        }
        else {
            sCustno = cplFeeIn.getCustno();
            eActtyp = null;
            sCustac = sCustAcct;
            eCsexfg = cplFeeIn.getCsexfg();
            sActseq = cplFeeIn.getSeqnum();
            sAcctno = "";
            sAcctna = "";
            sProdcd = cplFeeIn.getProdcd();
        }

        if (CommUtil.isNotNull(cplFeeIn.getCustno())) {
            sCustno = cplFeeIn.getCustno(); //客户号
        }

/*        if (CommUtil.isNotNull(cplFeeIn.getProdcd())) {
            sProdcd = cplFeeIn.getProdcd(); //产品号
        }*/

        //   TODO	
        /* 根据客户号获取客户类型，客户级别和客户中文名称 */
        //		if (CommUtil.isNotNull(sCustno)) {
        //
        //		}

        bizlog.debug("输入扣费账户信息[%s]", cplFeeIn.getCplKfzhu());
        /*扣费账户信息 */
        if (CommUtil.isNotNull(cplFeeIn.getCplKfzhu().getCustac()) && cplFeeIn.getChgflg() != E_CHGFLG.NONE) {
            
            CgGetAccountInf_IN cplIn = SysUtil.getInstance(CgGetAccountInf_IN.class);
            cplIn.setAcctno(cplFeeIn.getCplKfzhu().getCustac());  //账号
            cplIn.setActseq(cplFeeIn.getCplKfzhu().getCgsbsq());  //子户号
            if (CommUtil.isNotNull(cplFeeIn.getCplKfzhu().getCrcycd())) {
                cplIn.setCrcycd(cplFeeIn.getCplKfzhu().getCrcycd()); //币种
            }
            else { 
                cplIn.setCrcycd(cplFeeIn.getTrancy()); //币种
            }
            cplIn.setCustno(cplFeeIn.getCustno()); //客户号
            if (CommUtil.isNotNull(cplFeeIn.getCplKfzhu().getCsexfg())) {
                cplIn.setCsextg(cplFeeIn.getCplKfzhu().getCsexfg()); //钞汇标志
            }
            else {
                cplIn.setCsextg(cplFeeIn.getCsexfg()); //钞汇标志
            }
            cplOut_SF = ChargeProc.getAccountInf(cplIn);   //查询收费记账账号信息
            if (CommUtil.isNull(cplOut_SF)) {
                throw FeError.Chrg.BNASF183();
            }
             
            sCustno_SF = cplOut_SF.getCustno(); //客户号
            eActtyp_SF = cplOut_SF.getActtyp(); //账户类型
            sCustac_SF = cplOut_SF.getCustac(); //客户账号
            eCrcycd_SF = cplOut_SF.getCrcycd(); //币种
            sActseq_SF = cplOut_SF.getActseq(); //子户号
            eCsexfg_SF = cplOut_SF.getCsextg(); //钞汇标志 
            sAcctno_SF = cplOut_SF.getAcctno(); //系统账号

            if (CommUtil.isNotNull(cplFeeIn.getCplKfzhu().getCrcycd()) && CommUtil.compare(cplFeeIn.getCplKfzhu().getCrcycd(), eCrcycd_SF) != 0) {
                throw PbError.Charg.E0002("扣费", eCrcycd_SF, cplFeeIn.getCplKfzhu().getCrcycd());
            }
            if (CommUtil.isNotNull(cplFeeIn.getCplKfzhu().getCsexfg()) && CommUtil.compare(cplFeeIn.getCplKfzhu().getCsexfg(),eCsexfg_SF) != 0) {
                throw PbError.Charg.E0003("扣费", eCsexfg_SF, cplFeeIn.getCplKfzhu().getCsexfg());
            }
            if (CommUtil.isNotNull(cplFeeIn.getCplKfzhu().getCgsbsq()) && CommUtil.compare(cplFeeIn.getCplKfzhu().getCgsbsq(), sActseq_SF) != 0) {
                throw PbError.Charg.E0004("扣费", sActseq_SF, cplFeeIn.getCplKfzhu().getCgsbsq());
            }
            if (CommUtil.compare(eCrcycd_SF,cplFeeIn.getChrgcy()) !=0 ) {
                throw PbError.Charg.E0002("扣费", eCrcycd_SF, cplFeeIn.getChrgcy());
            }          
            
        }

        String sServtp = CommTools.getBaseRunEnvs().getChannel_id(); //交易渠道

        BigDecimal bigTranam = cplFeeIn.getTranam(); // 交易金额
        long amount = 1;//默认数量1
        if (CommUtil.isNotNull(cplFeeIn.getAmount()) && cplFeeIn.getAmount() != 0) {
        	amount = cplFeeIn.getAmount(); // 数量
        	
        }
         if(amount<0){
        	throw FeError.Chrg.BNASF239();
        }
        if (cplFeeIn.getChrgsr() == E_CHRGSR.INPUT || (CommUtil.isNotNull(cplFeeIn.getSpcham())
                && CommUtil.compare(cplFeeIn.getSpcham(), BigDecimal.ZERO) > 0)) {
            //eAppoint = E_YES___.YES;
            BigDecimal bigTranctBD = new BigDecimal(amount); //笔数
            bigSpcham = cplFeeIn.getSpcham().multiply(bigTranctBD); //指定收费金额
            cplFeeIn.setChrgsr(E_CHRGSR.INPUT); //收费金额来源-输入
            bigTranam = bigSpcham;// 指定金额收费
        } else if (CommUtil.isNull(cplFeeIn.getChrgsr()))
        {
            cplFeeIn.setChrgsr(E_CHRGSR.CALCULATE); //收费金额来源-计算
        }

        if (CommUtil.compare(bigTranam, BigDecimal.ZERO) == 0 || CommUtil.isNull(bigTranam)) {
            bigTranam = BigDecimal.ZERO;
        }
        bizlog.debug("需收费交易金额bigTranam[%s]", bigTranam);

        if (CommUtil.isNull(cplFeeIn.getScencd())) {
            cplFeeIn.setScencd("%"); //场景代码  
        }



        //   TODO
        /* 获取账户性质列表 */
        //		IoIbCusSignSer cplTmp = SysUtil.getInstance(IoIbCusSignSer.class);
        //		ioIBSignCXIn cplIoIn = SysUtil.getInstance(ioIBSignCXIn.class);
        //		cplIoIn.setKehuzhao(sCustAcct);
        //		ioIBSignCXOut lstOutput = cplTmp.qryIoIbSignResult(cplIoIn);
        //		cplWDInfo.setEmplus(lstHangnygh);// 账户类别

        /*根据客户账号获取该客户下对应的卡产品编号*/
        //		IoCdPzxxQryUpd cplCdAPI = SysUtil.getInstance(IoCdPzxxQryUpd.class);
        //		IoCdPzxxQryOut cplCard = SysUtil.getInstance(IoCdPzxxQryOut.class);
        //		try {
        //			cplCard = cplCdAPI.qryCdPzxxInfo(sCustAcct);
        //		}
        //		cplWDInfo.setCdpdno(cplCard.getProdcd());// 卡产品编号



       
    	for (IoCgCalCenterReturn calcenter : calCenterReturn) {
    		
    		String sChrgcd = calcenter.getChrgcd();//收费代码
    		
    		if (CommUtil.isNull(sChrgcd)) {
    			
    			throw FeError.Chrg.BNASF076();
    		}       		
    		bigRecvam=calcenter.getPaidam();
    		BigDecimal bigChrgam_out = calcenter.getPaidam();//实收金额	
    		bigDvidam = calcenter.getDioamo().add(calcenter.getDiwamo()).add(calcenter.getDitamo()).add(calcenter.getDifamo());
    		/**********************************************
    		 * 2.1、获取收费代码信息 <br>
    		 **********************************************/
    		KcpChrgAcrl  tblCgtp =PBChargeCodeDao.selChargeAccRuleDetail(sChrgcd, true); //根据收费代码  		
    		
    		// 赋值费用信息结构	
    		CgFEEINFO cplFeeinfo = SysUtil.getInstance(CgFEEINFO.class);
    		cplFeeinfo.setTrandt(sTrandt); //交易日期
    		cplFeeinfo.setTrnseq(sTransq); //交易流水
    		cplFeeinfo.setSequno(Long.parseLong("0")); //顺序号
    		cplFeeinfo.setCgpyrv(tblCgtp.getCgpyrv()); //费用收付标志
    		cplFeeinfo.setChrgcd(sChrgcd); //收费代码
    		cplFeeinfo.setChrgna(tblCgtp.getChrgna()); //收费代码名称
    		cplFeeinfo.setTrancy(eCrcycd);
    		cplFeeinfo.setProdcd(CommUtil.nvl(calcenter.getPronum(), sProdcd));//产品编码
    		cplFeeinfo.setAmount(calcenter.getAmount());//数量
    		cplFeeinfo.setClcham(calcenter.getClcham());//计算费用金额
    		if (CommUtil.compare(calcenter.getPaidam(), BigDecimal.ZERO)<=0) {
    			throw FeError.Chrg.BNASF214();
    		}     		
    		cplFeeinfo.setAcclam(calcenter.getPaidam());//实收金额
    		cplFeeinfo.setRecvam(calcenter.getDircam());//优惠后应收金额
    		cplFeeinfo.setFavpec(calcenter.getDisrat());//优惠比例
    		if (CommUtil.isNull(calcenter.getTrinfo())) {
    			throw FeError.Chrg.BNASF166();
    		}   
    		if (calcenter.getTrinfo().length()>7) {
    			throw FeError.Chrg.BNASF036();
    		}  
		/*	List<IoKnsProdClerInfo> cplProdCler = SysUtil.getInstance(IoAccountSvcType.class).selKnsProdClerInfo(corpno,cplFeeinfo.getProdcd(), calcenter.getTrinfo());
			
			if(CommUtil.isNull(cplProdCler)||cplProdCler.size()==0){
				
				throw FeError.Chrg.BNASF225();
			}   */    
    		
    		cplFeeinfo.setTrinfo(calcenter.getTrinfo());//交易信息
    		cplFeeinfo.setChnotp(calcenter.getChnotp());//费种大类
    		cplFeeinfo.setChnona(calcenter.getChnona());//费种大类名称
    		
    		if (CommUtil.isNotNull(eClchcy)) {
    			cplFeeinfo.setClchcy(eClchcy); //计费币种
    		}
    		cplFeeinfo.setCgscam(bigChrgam_out); //费用应收金额
    		//cplFeeinfo.setFavpec(bigFinlrt); //优惠比例
    		cplFeeinfo.setChrgcy(eChrgcy); //收费币种
    		cplFeeinfo.setArrgam(bigRecvam); //欠费金额
    		cplFeeinfo.setDvidam(bigDvidam); //收费分成金额
    		  		
    		cplFeeinfo.setChrgpd(tblCgtp.getChrgpd()); //收费周期
    		cplFeeinfo.setDedudt(sChrgdt); //上次扣费日期
    		cplFeeinfo.setRecvfg(E_GTAMFG.NO); //收讫标志
    		cplFeeinfo.setCghacd(""); //挂账业务编号
    		cplFeeinfo.setCgfacd(""); //核算业务编号
    		/**********************************************
    		 * 2.7 登记收费登记簿<br>
    		 **********************************************/
    		bizlog.debug("登记收费登记簿");
    		KcbChrgRgst tblsfdj = SysUtil.getInstance(KcbChrgRgst.class);
    		BigDecimal bigYingsfje = BigDecimal.ZERO; //应收费用金额
    		BigDecimal bigShoushfy = BigDecimal.ZERO; //实收费用金额
    		BigDecimal bigYffyjine = BigDecimal.ZERO; //应付费用金额
    		BigDecimal bigShifjine = BigDecimal.ZERO; //实付费用金额
    		BigDecimal bigQianfjee = BigDecimal.ZERO; //欠费金额 
    		String  fetype =null;//费种大类
    		String fetpna = "";//费种大类名称

    		if(CommUtil.isNull(cplFeeIn.getSmrycd())){
        		if(tblCgtp.getCgpyrv()== E_CGPYRV.RECIVE){
        			
        			cplFeeIn.setSmrycd(BusinessConstants.SUMMARY_SF);
        			cplFeeIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_SF));
        		}else{
        			
        			cplFeeIn.setSmrycd(BusinessConstants.SUMMARY_FF);
        			cplFeeIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_FF));
        		}

    		}
    		
    		if(CommUtil.equals(calcenter.getChrgcd().substring(3, 5), "01")){
    			//01工本费
    			fetype = E_FETYPE.GB.getValue();
    			fetpna = E_FETYPE.GB.getLongName();

    			
    		}else if (CommUtil.equals(calcenter.getChrgcd().substring(3, 5), "02")){//手续费
    			//02 手续费 
    			fetype = E_FETYPE.SX.getValue();
    			fetpna = E_FETYPE.SX.getLongName();
			
    		}else if (CommUtil.equals(calcenter.getChrgcd().substring(3, 5), "03")){//手续费
    			//03 汇划费用 
    			fetype = E_FETYPE.HH.getValue();
    			fetpna = E_FETYPE.HH.getLongName();
   			
    		}else if (CommUtil.equals(calcenter.getChrgcd().substring(3, 5), "04")){//邮电费
    			fetype = E_FETYPE.YD.getValue();
    			fetpna = E_FETYPE.YD.getLongName();
		
    		}else if (CommUtil.equals(calcenter.getChrgcd().substring(3, 5), "05")){//手续费
    			// 05 服务费
    			fetype = E_FETYPE.FW.getValue();
    			fetpna = E_FETYPE.FW.getLongName();
 			
    		}else if (CommUtil.equals(calcenter.getChrgcd().substring(3, 5), "06")){//手续费
    			// 06管理费(年费)
    			fetype = E_FETYPE.GL.getValue();
    			fetpna = E_FETYPE.GL.getLongName();
 			
    		}else if (CommUtil.equals(calcenter.getChrgcd().substring(3, 5), "07")){//手续费
    			// 07罚没费
    			fetype = E_FETYPE.FM.getValue();
    			fetpna = E_FETYPE.FM.getLongName();
 			
    		}else if (CommUtil.equals(calcenter.getChrgcd().substring(3, 5), "08")){//手续费
    			// 08 违约金
    			fetype = E_FETYPE.WY.getValue();
    			fetpna = E_FETYPE.WY.getLongName();
			
    		}else if (CommUtil.equals(calcenter.getChrgcd().substring(3, 5), "09")){//手续费
    			// 09信函费
    			fetype = E_FETYPE.XH.getValue();
    			fetpna = E_FETYPE.XH.getLongName();
	
    		}else if (CommUtil.equals(calcenter.getChrgcd().substring(3, 5), "80")){//手续费
    			// 80 税费
    			fetype = E_FETYPE.XF.getValue();
    			fetpna = E_FETYPE.XF.getLongName();
		
    		}else{
    			throw FeError.Chrg.BNASF390(calcenter.getChrgcd());
    		}
    		
    		// 若传入的交易备注为空，将费种大类名称传入交易备注登记
    		if (CommUtil.isNull(cplFeeIn.getRemark())) {
    			cplFeeIn.setRemark(tblCgtp.getChrgna());
    		}
    		
    	    		
    		if (eChgflg == E_CHGFLG.ONE || eChgflg == E_CHGFLG.ALL) {
    			
    			if (CommUtil.compare(bigChrgam_out, BigDecimal.ZERO) != 0 || CommUtil.compare(bigRecvam, BigDecimal.ZERO) != 0) {
    				
    				
    				tblsfdj.setTrandt(sTrandt); //交易日期
    				tblsfdj.setTrnseq(sTransq); //交易流水
    				
    				long lShjndjxh = PBChargeRegisterDao.selChargeRegistrMaxSeq(CommTools.getBaseRunEnvs().getBusi_org_id(), false); //事件登记序号
    				
    				bizlog.debug("上次登记序号lShjndjxh0[%s]", lShjndjxh);
    				lShjndjxh++;
    				bizlog.debug("本次登记序号lShjndjxh[%s]", lShjndjxh);
    				tblsfdj.setEvrgsq(lShjndjxh); //事件登记序号
    				cplFeeinfo.setSequno(lShjndjxh); //序号
    				tblsfdj.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code()); //交易码
    				tblsfdj.setTrnchl(sServtp); //交易渠道
    				tblsfdj.setChrgsr(cplFeeIn.getChrgsr()); //收费金额来源
    				tblsfdj.setChrgcd(sChrgcd); //收费代吗
    				tblsfdj.setChrgna(tblCgtp.getChrgna()); //收费代码名称 
    				tblsfdj.setCgpyrv(tblCgtp.getCgpyrv()); //费用收付标志
    				tblsfdj.setTrancy(eCrcycd); // 交易币种
    				tblsfdj.setTranam(calcenter.getTranam()); //交易金额
    				tblsfdj.setAmount(amount); // 数量
    				tblsfdj.setSpcham(bigSpcham); // 指定收费金额
    				if (CommUtil.isNotNull(eClchcy)) {
    					tblsfdj.setClchcy(eClchcy); //计费币种
    				}else{
    					tblsfdj.setClchcy(eCrcycd);
    				}
    				
    				tblsfdj.setClcham(calcenter.getClcham()); //计算费用金额 
    				tblsfdj.setDisrat(calcenter.getDisrat()); //优惠比例
    				tblsfdj.setDircam(calcenter.getDircam()); //优惠后应收费用金额
    				tblsfdj.setDistam(calcenter.getClcham().subtract(calcenter.getDircam()));//费用优惠金额=费用金额-费用优惠后应收

    				tblsfdj.setChrgcy(cplFeeIn.getChrgcy()); //收费币种
    				tblsfdj.setRecvam(bigRecvam); //应收费用金额
    				tblsfdj.setAcclam(BigDecimal.ZERO); //实收费用金额 初始化为零
    				tblsfdj.setArrgam(bigRecvam); //欠费金额
    				tblsfdj.setReduam(BigDecimal.ZERO); //费用减免金额
    				tblsfdj.setDvidam(bigDvidam); //收费分成金额
    				tblsfdj.setScencd(calcenter.getScencd());
    				tblsfdj.setScends(calcenter.getScends());   				
    				//tblsfdj.setClexrt(bigClexrt); //计费兑换比
    				tblsfdj.setChexrt(bigChexrt); //收费兑换比
    				tblsfdj.setCgfacd(""); //收费核算业务编号
    				tblsfdj.setCghacd(""); //收费挂账业务编号
    				tblsfdj.setLdseno("0"); //费用挂账账号子序号
    				tblsfdj.setLdgacn(""); //费用挂账账号 
    				tblsfdj.setCstrfg(cplFeeIn.getCstrfg()); //现转标志
    				tblsfdj.setRecvfg(E_GTAMFG.NO); //收讫标志
    				tblsfdj.setModtyp(eModtyp); //所属模块
    				tblsfdj.setCustno(sCustno); //客户号
    				tblsfdj.setActtyp(eActtyp); //客户账号类型
    				tblsfdj.setCustac(sCustac); //客户账号
    				tblsfdj.setCsexfg(eCsexfg); //钞汇标志
    				tblsfdj.setSeqnum(sActseq); // 顺序号
    				tblsfdj.setSysacn(sAcctno); //系统账号
    				tblsfdj.setAcchnm(sAcctna); //账户中文名
    				tblsfdj.setAcusam(BigDecimal.ZERO); //账户可用金额
    				tblsfdj.setProdcd(cplFeeinfo.getProdcd()); //产品编号    	
    				tblsfdj.setBlpdno("PB0000"); // 归属产品号
    				tblsfdj.setOrbssn(null); //原始业务流水号
    				tblsfdj.setOrbsda(null); //原始业务日期
    				tblsfdj.setChactp(eActtyp_SF); //收费客户账户类型
    				tblsfdj.setChacno(sCustac_SF); //收费客户账号
    				tblsfdj.setChaccy(eCrcycd_SF); //收费账户币种
    				tblsfdj.setChcefg(eCsexfg_SF); //收费钞汇标志
    				tblsfdj.setChsyac(sAcctno_SF); //收费系统帐号
    				tblsfdj.setChacsq(sActseq_SF); //收费账户序号
    				
    				
    				tblsfdj.setChevno(cplFeeIn.getChevno()); //收费事件编号
    				// tblsfdj.setLschda(); //上次批扣日期
    				tblsfdj.setChrgpd(tblCgtp.getChrgpd()); //收费周期
    				tblsfdj.setChgdat(sTrandt); //收费日期
    				tblsfdj.setAcctbr(sTranbr); //记账机构
    				tblsfdj.setDioage(calcenter.getDioage());//分润方一机构号
    				tblsfdj.setDiwage(calcenter.getDiwage());//分润方二机构号
    				tblsfdj.setDitage(calcenter.getDitage());//分润方三机构号
    				tblsfdj.setDifage(calcenter.getDifage());//分润方四机构号
					tblsfdj.setDioamo(calcenter.getDioamo());//分润方一金额
					tblsfdj.setDiwamo(calcenter.getDiwamo());//分润方二金额
					tblsfdj.setDitamo(calcenter.getDitamo());//分润方三金额
					tblsfdj.setDifamo(calcenter.getDifamo());//分润方四金额      			  			    				
    				tblsfdj.setCgstno(Long.parseLong("0")); //收费明细序号
    				tblsfdj.setChfltm(Long.parseLong("0")); //扣费失败次数
    				tblsfdj.setArgrsn(""); //欠费原因    				
    				tblsfdj.setSmrycd(cplFeeIn.getSmrycd()); //摘要代码
    				tblsfdj.setSmryds(cplFeeIn.getSmryds()); //摘要描述
    				tblsfdj.setRemark(cplFeeIn.getRemark()); //备注信息
    				tblsfdj.setTrntim(Long.valueOf(DateTools.getCurrentTime())); //交易时间
    				tblsfdj.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller()); //交易柜员
    				tblsfdj.setAuttel(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus()); //授权柜员
    				tblsfdj.setTrinfo(cplFeeinfo.getTrinfo());//交易信息
    				tblsfdj.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq());//柜员流水
    				tblsfdj.setUssrdt(CommTools.getBaseRunEnvs().getTrxn_date());//柜员日期
    				tblsfdj.setChnotp(fetype);//费种大类
    				tblsfdj.setChnona(fetpna);//费种大类名称
    				
    				
    			   				
    				//   TODO  滞纳金
    				//					tblsfdj.setFepecd(sFepecd);  //滞纳金收费代码
    				//					tblsfdj.setFepefg(eFepefg);  //是否重复计算滞纳金
    				//					tblsfdj.setFepamt(sFepamt);  //滞纳金金额
    				//					tblsfdj.setFebaln(sFebaln);   //滞纳金余额
    				//					tblsfdj.setFecumu(sFecumu);   //当期滞纳金累计积数
    				//					tblsfdj.setFecuda(sFecuda);   //当期滞纳金累计积数截止日 
    				//tblsfdj.setJiluztai(E_JILUZTAI.Normal); //记录状态
    				try {
    					KcbChrgRgstDao.insert(tblsfdj);
    				} catch (Exception e) {
    					throw FeError.Chrg.BNASF999("插入收费登记簿失败", e);
    				}
    				 
    				bizlog.debug(" 开始费用记账");
    				if ((eChgflg == E_CHGFLG.ONE || eChgflg == E_CHGFLG.ALL) && (CommUtil.compare(tblsfdj.getChrgpd(), "0") == 0
    						|| CommUtil.compare(tblsfdj.getChgdat(), sTrandt) <= 0))
    				{
    					bizlog.debug("实时收费");
    					
    					CgChargeFee_IN cplChFeeIn = SysUtil.getInstance(CgChargeFee_IN.class);
    					cplChFeeIn.setOprflg(E_OPRFLG.SEQNO); /* 按计费流水+序号收 */
    					
    					if (eChgflg == E_CHGFLG.ONE) {
    						cplChFeeIn.setIfflag(E_YES___.NO); /* 只记费用账 */
    					}
    					else if (eChgflg == E_CHGFLG.ALL) {
    						if (eTotflg == E_YES___.NO) {
    							cplChFeeIn.setIfflag(E_YES___.YES); /* 同时记客户和费用账 */
    							
    						}
    						else {
    							cplChFeeIn.setIfflag(E_YES___.NO); /* 只记费用账, 客户账汇总记*/
    						}
    					}
    					cplChFeeIn.setPayaDetail(cplFeeIn.getPayaDetail());
    					cplChFeeIn.setPaydDetail(cplFeeIn.getPaydDetail());
    					cplChFeeIn.setTrandt(sTrandt); //交易日期
    					cplChFeeIn.setTrnseq(sTransq); //交易流水
    					cplChFeeIn.setEvrgsq(lShjndjxh); //事件登记序号
    					cplChFeeIn.setAcclam(calcenter.getPaidam()); //实收费用金额
    					cplChFeeIn.setDvidam(bigDvidam); //收费分成金额
    					cplChFeeIn.setCstrfg(cplFeeIn.getCstrfg()); //现转标志
    					cplChFeeIn.setCsprcd(cplFeeIn.getCsprcd()); //现金项目代码
    					cplChFeeIn.setSmrycd(cplFeeIn.getSmrycd()); //摘要代码
    					cplChFeeIn.setSmryds(cplFeeIn.getSmryds()); //摘要描述
    					cplChFeeIn.setRemark(cplFeeIn.getRemark()); // 备注
    					cplChFeeIn.setTrinfo(calcenter.getTrinfo());//交易信息
    					cplChFeeIn.setProdcd(calcenter.getPronum());//产品代码
    					cplChFeeIn.setStrktg(cplFeeIn.getStrktg());//是否允许冲正  		
    					cplChFeeIn.setIsclos(cplFeeIn.getIsclos());//是否实时收费	
    					cplChFeeIn.setClactp(cplFeeIn.getClactp()); //销户标识
    					
    					
    		            //如果还款利息大于0，计算增值税
    		            String acctnoTx = sAcctno_SF;
    		            String custnoTx = sCustno_SF;
    		            String custacTx = sCustac_SF;
    		            if (CommUtil.isNull(sAcctno_SF)) {
        		            acctnoTx = sAcctno;
        		            custnoTx = sCustno;
        		            custacTx = sCustac;
						}
    		            BigDecimal vatAmount = calcenter.getPaidam();
    		            if (CommUtil.compare(vatAmount, BigDecimal.ZERO) > 0 && CommUtil.isNotNull(acctnoTx)) {
    					
                            CalTaxAmountIn taxinp = SysUtil.getInstance(CalTaxAmountIn.class);
                            taxinp.setCustno(custnoTx);
                            taxinp.setCustac(custacTx);
                            taxinp.setAcctno(acctnoTx);
                            taxinp.setAcctcd(calcenter.getPronum());
                            taxinp.setHsheam(vatAmount);
                            taxinp.setRountp(E_ROUNTP.SWR);
                            taxinp.setCrcycd(cplFeeIn.getChrgcy());
                            taxinp.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());

                            CalTaxAmountOut taxout = SysUtil.getInstance(CalTaxAmountOut.class);
                            
//                            SysUtil.getInstance(TxSvc.class).calTaxAmount(taxinp, taxout);
    		            }
    					
    					if (CommUtil.isNotNull(cplFeeIn.getCplKfzhu().getCustac())) {
    						bizlog.debug("扣费账户");
    						cplChFeeIn.setCustno(sCustno_SF); //客户号
    						cplChFeeIn.setDecuac(sCustac_SF); //扣账客户账号
    						cplChFeeIn.setTrancy(cplFeeIn.getChrgcy()); //交易币种
    						cplChFeeIn.setCsexfg(eCsexfg_SF); //钞汇币种
    						cplChFeeIn.setSeqnum(sActseq_SF); //顺序号
    					}
    					else if (CommUtil.equals(cplFeeIn.getChrgcy(), eCrcycd)) {
    						bizlog.debug("客户账户");
    						cplChFeeIn.setCustno(sCustno);
    						cplChFeeIn.setDecuac(sCustac);
    						cplChFeeIn.setTrancy(cplFeeIn.getChrgcy());
    						cplChFeeIn.setCsexfg(cplFeeIn.getCsexfg());
    						cplChFeeIn.setSeqnum(sActseq);
    					}
    					else {
    						bizlog.debug("客户账户qq");
    						cplChFeeIn.setCustno(sCustno);
    						cplChFeeIn.setDecuac(sCustac);
    						cplChFeeIn.setTrancy(cplFeeIn.getChrgcy());
    						cplChFeeIn.setCsexfg(cplFeeIn.getCsexfg());
    					}
    					bizlog.debug("收费记账输入--cplChFeeIn[%s]", cplChFeeIn);
    					//计费中心计费之后收费记账
    					
    					
    					cplChFee_Out = ChargeProc.chargeFee(cplChFeeIn);
    					bizlog.debug("收费记账输出cplChFee_Out[%s]", cplChFee_Out);
    					
    					
    					
    					if (CommUtil.isNotNull(cplChFee_Out.getLstfyinf())) {
    						CgFEEINFO cplout = cplChFee_Out.getLstfyinf().get(0);
    						cplFeeinfo.setAcclam(cplout.getAcclam()); //实收费用金额
    						cplFeeinfo.setArrgam(cplout.getArrgam()); //欠费金额
    						cplFeeinfo.setDvidam(cplout.getDvidam()); //收费分成金额
    						cplFeeinfo.setRecvfg(cplout.getRecvfg()); //收讫标志
    						cplFeeinfo.setSysacn(cplout.getSysacn()); //账号
    					}
    					
    					if (CommUtil.isNotNull(cplFeeOut.getRecvam())) {
    						bigYingsfje = cplFeeOut.getRecvam(); //应收费用金额
    					}
    					
    					if (CommUtil.isNotNull(cplFeeOut.getAcclam())) {
    						bigShoushfy = cplFeeOut.getAcclam(); //实收费用金额
    					}
    					
    					if (CommUtil.isNotNull(cplFeeOut.getSpcgam())) {
    						bigYffyjine = cplFeeOut.getSpcgam(); //应付费用金额 
    					}
    					
    					if (CommUtil.isNotNull(cplFeeOut.getApcgam())) {
    						bigShifjine = cplFeeOut.getApcgam(); //实付费用金额
    					}
    					
    					if (CommUtil.isNotNull(cplFeeOut.getArrgam())) {
    						bigQianfjee = cplFeeOut.getArrgam(); //欠费金额
    					}
    					
    					if (tblCgtp.getCgpyrv() == E_CGPYRV.RECIVE) { //收费
    						cplFeeOut.setRecvam(bigYingsfje.add(cplChFee_Out.getRecvam())); //应收费用金额
    						cplFeeOut.setAcclam(bigShoushfy.add(cplChFee_Out.getAcclam())); //实收费用金额
    					}
    					else if (tblCgtp.getCgpyrv() == E_CGPYRV.PAY) { //付费
    						cplFeeOut.setSpcgam(bigYffyjine.add(cplChFee_Out.getSpcgam())); //应付费用金额 
    						cplFeeOut.setApcgam(bigShifjine.add(cplChFee_Out.getApcgam())); //实付费用金额
    					}
    					if (CommUtil.isNotNull(cplChFee_Out.getLstfyinf())) {
    						CgFEEINFO cplout = cplChFee_Out.getLstfyinf().get(0);
    						cplFeeOut.setArrgam(bigQianfjee.add(cplout.getArrgam())); //欠费金额
    						
    					}
    					
    				}
    				else if (cplFeeIn.getChgflg() == E_CHGFLG.ONE || cplFeeIn.getChgflg() == E_CHGFLG.ALL) {
    					
    					bizlog.debug("批量收费");
    					
    					if (tblCgtp.getCgpyrv() == E_CGPYRV.RECIVE) { //收费
    						
    						cplFeeOut.setRecvam(bigYingsfje.add(cplFeeinfo.getRecvam())); //应收费用金额
    						
    					}
    					else if (tblCgtp.getCgpyrv() == E_CGPYRV.PAY) { //付费
    						
    						cplFeeOut.setSpcgam(bigYffyjine.add(cplFeeinfo.getRecvam())); //应付费用金额 
    						
    					}
    					cplFeeOut.setArrgam(bigQianfjee.add(cplFeeinfo.getArrgam())); //欠费金额
    					
    				}
    				else {
    					cplFeeinfo.setArrgam(BigDecimal.ZERO);
    					if (tblCgtp.getCgpyrv() == E_CGPYRV.RECIVE) { //收费
    						cplFeeOut.setRecvam(bigYingsfje.add(cplFeeinfo.getRecvam())); //应收费用金额
    					}
    					else if (tblCgtp.getCgpyrv() == E_CGPYRV.PAY) { //付费
    						cplFeeOut.setSpcgam(bigYffyjine.add(cplFeeinfo.getRecvam())); //应付费用金额
    						
    					}
    				}
    				
    			}
    		}
    		else {
    			cplFeeinfo.setArrgam(BigDecimal.ZERO);
    			if (tblCgtp.getCgpyrv() == E_CGPYRV.RECIVE) { //收费
    				if (CommUtil.isNull(cplFeeOut.getRecvam()))
    				{
    					cplFeeOut.setRecvam(BigDecimal.ZERO);
    				}
    				cplFeeOut.setRecvam(cplFeeOut.getRecvam().add(cplFeeinfo.getRecvam())); //应收费用金额
    				
    			}
    			else if (tblCgtp.getCgpyrv() == E_CGPYRV.PAY) { //付费
    				if (CommUtil.isNull(cplFeeOut.getSpcgam()))
    				{
    					cplFeeOut.setSpcgam(BigDecimal.ZERO);
    				}
    				cplFeeOut.setSpcgam(cplFeeOut.getSpcgam().add(cplFeeinfo.getRecvam())); //应付费用金额
    				
    			}
    			
    		}
    		
    		cplFeeOut.getLstFeexx().add(cplFeeinfo);
    		
    	}        		
        		       	
        

        //cplFeeOut.setAmount(Long.parseLong(lstSfsj.size() + "")); //数量
        cplFeeOut.setModtyp(eModtyp); //所属模块
        cplFeeOut.setCustno(sCustno); //客户号
        cplFeeOut.setCustac(sCustac); //客户账号
        cplFeeOut.setAcchnm(sAcctna); //账户中文名
        cplFeeOut.setAccgac(sCustac_SF); //扣费账号
        cplFeeOut.setChrgcy(eChrgcy); //收费币种

        /*汇总记账处理，目前系统采用单笔记账*/
        if (cplFeeIn.getChgflg() == E_CHGFLG.ONE || cplFeeIn.getChgflg() == E_CHGFLG.ALL) {
            if (CommUtil.compare(cplFeeOut.getApcgam(), cplFeeOut.getAcclam()) > 0) {
                cplFeeOut.setCgpyrv(E_CGPYRV.PAY);
                cplFeeOut.setTotamt(cplFeeOut.getApcgam().subtract(cplFeeOut.getAcclam())); //合计=实付-实收
            }
            else {
                cplFeeOut.setCgpyrv(E_CGPYRV.RECIVE);
                cplFeeOut.setTotamt(cplFeeOut.getAcclam().subtract(cplFeeOut.getApcgam())); //合计=实收-实付

            }

            if (eTotflg == E_YES___.YES)// 汇总记客户账
            {
                /* 汇总记客户账手续费 */
                if (CommUtil.isNotNull(cplFeeIn.getCplKfzhu().getCustac())) {
                    //从实际扣费账号扣费
                    CgChargefeeKhz_IN cplKhz = SysUtil.getInstance(CgChargefeeKhz_IN.class);
                    cplKhz.setCstrfg(cplFeeIn.getCstrfg());
                    cplKhz.setOprflg(E_OPRFLG.SEQNO);
                    cplKhz.setCgpyrv(cplFeeOut.getCgpyrv());
                    cplKhz.setCustno(sCustno_SF);
                    cplKhz.setActtyp(eActtyp_SF);
                    cplKhz.setCustac(sCustac_SF);
                    cplKhz.setTrancy(cplFeeIn.getChrgcy());
                    cplKhz.setCsexfg(eCsexfg_SF);
                    cplKhz.setActseq(sActseq_SF);
                    cplKhz.setSysacn(sAcctno_SF);
                    cplKhz.setAcclam(cplFeeOut.getTotamt());
                    cplKhz.setCsprcd(cplFeeIn.getCsprcd());
                    cplKhz.setSmrycd(cplFeeIn.getSmrycd());
                    cplKhz.setSmryds(cplFeeIn.getSmryds());
                    cplKhz.setRemark(cplFeeIn.getRemark());
                    ChargeProc.calChargefeeKhz(cplKhz);
                }
                else {
                    //从交易帐号扣费
                    CgChargefeeKhz_IN cplKhz = SysUtil.getInstance(CgChargefeeKhz_IN.class);
                    cplKhz.setCstrfg(cplFeeIn.getCstrfg());
                    cplKhz.setOprflg(E_OPRFLG.SEQNO);
                    cplKhz.setCgpyrv(cplFeeOut.getCgpyrv());
                    cplKhz.setCustno(sCustno);
                    cplKhz.setActtyp(eActtyp);
                    cplKhz.setCustac(sCustac);
                    cplKhz.setTrancy(cplFeeIn.getChrgcy());
                    cplKhz.setCsexfg(eCsexfg);
                    cplKhz.setActseq(sActseq);
                    cplKhz.setSysacn(sAcctno);
                    cplKhz.setAcclam(cplFeeOut.getTotamt());
                    cplKhz.setCsprcd(cplFeeIn.getCsprcd());
                    cplKhz.setSmrycd(cplFeeIn.getSmrycd());
                    cplKhz.setSmryds(cplFeeIn.getSmryds());
                    cplKhz.setRemark(cplFeeIn.getRemark());
                    cplKhz.setProdcd(sProdcd);
                    if (CommUtil.compare(cplFeeOut.getTotamt(), BigDecimal.ZERO) > 0) {
                        ChargeProc.calChargefeeKhz(cplKhz);
                    }
                }

            }
        }
        else {
            cplFeeOut.setAcclam(BigDecimal.ZERO); //实收金额
            cplFeeOut.setApcgam(BigDecimal.ZERO); //实付金额
            cplFeeOut.setArrgam(BigDecimal.ZERO); //欠费金额

            if (CommUtil.compare(cplFeeOut.getSpcgam(), cplFeeOut.getRecvam()) > 0) {
                cplFeeOut.setCgpyrv(E_CGPYRV.PAY);
                cplFeeOut.setTotamt(cplFeeOut.getSpcgam().subtract(cplFeeOut.getRecvam())); //合计=应付-应收
            }
            else {
                cplFeeOut.setCgpyrv(E_CGPYRV.RECIVE);
                cplFeeOut.setTotamt(cplFeeOut.getRecvam().subtract(cplFeeOut.getSpcgam())); //合计=应收-应付

            }

        }
        E_YES___ eRegBook =E_YES___.YES;
        if(cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.NO==cplFeeIn.getStrktg()){
        	eRegBook=E_YES___.NO;
        }
        /*冲正登记*/
        if (eRegBook == E_YES___.YES){
            
            IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
            cplInput.setCustac(sCustac); //客户账户
            cplInput.setTranac(sCustac_SF); //扣费账号
            cplInput.setTranev(ApUtil.TRANS_EVENT_PBRGST);  //PB01-收费登记
            cplInput.setTranam(cplFeeOut.getTotamt());// 交易金额
            cplInput.setAmntcd(null);// 借贷标志
            cplInput.setCrcycd(cplFeeIn.getChrgcy());// 货币代号
            cplInput.setTranno(null);// 交易序号
            cplInput.setEvent1(sTrandt);  //事件关键字1 = 交易日期
            cplInput.setEvent2(sTransq);  //事件关键字2 = 交易流水
            
            //ApStrike.regBook(cplInput);
            
    		IoMsRegEvent input = SysUtil.getInstance(IoMsRegEvent.class);    		
    		input.setReversal_event_id(ApUtil.TRANS_EVENT_PBRGST);
    		input.setInformation_value(SysUtil.serialize(cplInput));
    		MsEvent.register(input, true);
        }        
        /*计费结束，返回输出结构信息*/
        bizlog.parm("cplFeeOut[%s]", cplFeeOut);
        bizlog.method("calCharge end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        return cplFeeOut;
    }

 
}
