package cn.sunline.ltts.busi.fe.chrg;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.tmp.LocalTmpTableCaches;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeCodeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeFormulaDao;
import cn.sunline.ltts.busi.fe.namedsql.FeSceneDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrg;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgDvid;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgDvidDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdt;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgScdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgSubj;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgCalFee_IN;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgCalFee_OUT;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgChargeFee_OUT;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgFEEINFO;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgGetJFGSInfo;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgGetKZInfo;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgJfgcxn;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgJhyhbl;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgLjyhbl;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgMaxCgprRate;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgSfgsjs;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgSfjekz;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgSfyhbl;
import cn.sunline.ltts.busi.fe.type.FeComplexType.listnm;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.CgPreDiInfo;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_TESHUZFC;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CUSTTP;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_ACCLEV;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_ACTTYP;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_CARRTP;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_CHRGSR;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_CJSIGN;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_CUFETP;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_CUSLVL;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_CVCYFG;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_FELYTP;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_ISFAVO;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_LYSPTP;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_YES___;

public class CalCharg {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(CalCharg.class);

	 /**
     * @Author wuxq
     *         <p>
     *         <li>功能说明：统一计费返回多条数据，可以多个费种代码</li>
     *         </p>
     * @param cplFeeIn
     * @return
     */
    public CgCalFee_OUT calCharge(CgCalFee_IN cplFeeIn) {

        bizlog.method("calCharge begin >>>>>>>>>>>>>>>>>>>>>>");
        bizlog.parm("cplFeeIn[%s]", cplFeeIn);
        String eBascur = cplFeeIn.getTrancy(); //默认本币
        if (CommUtil.isNull(eBascur))
        {
            eBascur = BusiTools.getDefineCurrency();   //默认本币=人民币
        }

        String sTrandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
        String sTransq = CommTools.getBaseRunEnvs().getTrxn_seq(); //柜员流水
        //String sPrcscd = CommTools.getBaseRunEnvs().getTrxn_code(); //交易码
        String sPrcscd = CommTools.getBaseRunEnvs().getTrxn_code(); //交易码
        bizlog.debug("当前交易码[%s]", sPrcscd);

        String sTranbr, sAcctbr, sChrgpd = null, sChrgdt = "";
        String sSubnum = ""; //科目号
        String sIntacc = ""; //内部账对应顺序号
        String sPronum = ""; //产品编号
        String sTrinfo = ""; //交易信息
        E_ACCLEV sAcclev = null;//对账级别
        String sPrmark = ""; //对应产品标志
        E_CJSIGN sCjsign = null; //县辖维护标志
        String eChrgcy = null; //扣费币种
        E_ACTTYP eActtyp = null; //客户账户类型
        E_ACTTYP eActtyp_SF = null; //客户账户类型
        String eCrcycd_SF = null, eCrcycd = null; //货币代号
        String eClchcy = BusiTools.getDefineCurrency();  //计费币种
        E_CUSLVL eCustlv = null; // 客户级别
        E_CUSTTP eCusttp = null; // 客户类型
        E_YES___ eAppoint = E_YES___.NO; //是否指定收费金额

        CgCalFee_OUT cplFeeOut = SysUtil.getInstance(CgCalFee_OUT.class); //计费记账输出
        CgChargeFee_OUT cplChFee_Out = SysUtil.getInstance(CgChargeFee_OUT.class); //收费输出
    	
        BigDecimal bigDvidam = BigDecimal.ZERO; // 收费分成金额  
        BigDecimal bigSpcham = BigDecimal.ZERO; // 指定收费金额
        BigDecimal bigFinlrt = BigDecimal.ZERO; // 最终优惠比例
        BigDecimal bigCgprrt = BigDecimal.ZERO; // 收费优惠比例
        BigDecimal bigChrgam = BigDecimal.ZERO; // 优惠后应收费用金额
        BigDecimal bigChexrt = BigDecimal.ZERO; //收费兑换比例
        BigDecimal bigRecvam = BigDecimal.ZERO; //应收费用金额

        CgSfyhbl tblCgprRate = SysUtil.getInstance(CgSfyhbl.class); // 优惠比例信息
		CgLjyhbl tblCgsmRate = SysUtil.getInstance(CgLjyhbl.class); // 累积优惠信息
		CgJhyhbl tblNcplRate = SysUtil.getInstance(CgJhyhbl.class); // 优惠计划信息
		CgSfjekz tblCglmDetl = SysUtil.getInstance(CgSfjekz.class); // 收费金额控制信息
		KcpChrgDvid tblCgdv = SysUtil.getInstance(KcpChrgDvid.class); // 分成信息
		CgMaxCgprRate cplCgprRate = SysUtil.getInstance(CgMaxCgprRate.class); //优惠纬度信息
		
        if (CommUtil.isNull(cplFeeIn.getTrancy())) {
            throw FeError.Chrg.BNASF156();
        }
        eCrcycd = cplFeeIn.getTrancy();

//        if (CommUtil.isNull(cplFeeIn.getChrgcy())) {
//            throw FeError.Chrg.E1014("收费币种");
//        }
        eChrgcy = cplFeeIn.getChrgcy();
        
        if(CommUtil.isNull(cplFeeIn.getCustac())){
        	throw FeError.Chrg.BNASF181();
        }
        cplCgprRate.setCustac(cplFeeIn.getCustac()); //客户账号
        
        if(CommUtil.isNull(cplFeeIn.getCustno())){
        	throw FeError.Chrg.BNASF180();
        }
        cplCgprRate.setCustno(cplFeeIn.getCustno()); //客户号
        cplFeeOut.setCustno(cplFeeIn.getCustno());
        cplFeeOut.setCustac(cplFeeIn.getCustac());

        if (CommUtil.isNull(cplFeeIn.getTranbr())) {
            sTranbr = CommTools.getBaseRunEnvs().getTrxn_branch(); // 机构号
        }
        else {
            sTranbr = cplFeeIn.getTranbr();
            SysUtil.getInstance(IoSrvPbBranch.class).getBranch(sTranbr);
        }
        //String sServtp = CommTools.getBaseRunEnvs().getChannel_id(); //交易渠道
        String sServtp = CommTools.getBaseRunEnvs().getChannel_id();
        BigDecimal bigTranam = cplFeeIn.getTranam(); // 交易金额
        if(CommUtil.compare(bigTranam,BigDecimal.ZERO)<=0){
        	throw FeError.Chrg.BNASF159();
        }
        long lTranct = 1;
        if (CommUtil.isNotNull(cplFeeIn.getAmount()) && cplFeeIn.getAmount() != 0) {
            lTranct = cplFeeIn.getAmount(); // 数量
        }
        long lShuliang = 0;
        if (lTranct >= 1) {
            lShuliang = lTranct;

        }
        else {
            lShuliang = 1;
        }

        if (cplFeeIn.getChrgsr() == E_CHRGSR.INPUT || (CommUtil.isNotNull(cplFeeIn.getSpcham())
                && CommUtil.compare(cplFeeIn.getSpcham(), BigDecimal.ZERO) > 0)) {
            eAppoint = E_YES___.YES;
            BigDecimal bigTranctBD = new BigDecimal(lShuliang); //笔数
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
        bizlog.debug("收费金额bigTranam[%s]", bigTranam);

        if (CommUtil.isNull(cplFeeIn.getScencd())) {
            cplFeeIn.setScencd("%"); //场景代码  
        }
        
    	/*
    	 * 优惠维度信息赋值  
    	 */
        Options<CgPreDiInfo> lstCgPreDiInfo = new DefaultOptions<CgPreDiInfo>(); //定义优惠维度
        Options<IoFeChrgComplexType.CgPreDiInfo> predimLst = cplFeeIn.getPredim();
        CgPreDiInfo PreDimeInfo = SysUtil.getInstance(CgPreDiInfo.class);
        
        //机构号和渠道赋默认值
		PreDimeInfo = SysUtil.getInstance(CgPreDiInfo.class);
		PreDimeInfo.setDimecg("0028"); //机构号
    	PreDimeInfo.setDimevl(CommTools.getBaseRunEnvs().getTrxn_branch());	
    	lstCgPreDiInfo.add(PreDimeInfo);
    	PreDimeInfo = SysUtil.getInstance(CgPreDiInfo.class);
		PreDimeInfo.setDimecg("0029"); //渠道
    	PreDimeInfo.setDimevl(sServtp.toString());	
    	lstCgPreDiInfo.add(PreDimeInfo);
    	PreDimeInfo = SysUtil.getInstance(CgPreDiInfo.class);
		PreDimeInfo.setDimecg("0031"); //客户id
    	PreDimeInfo.setDimevl(cplFeeIn.getCustno());	
    	lstCgPreDiInfo.add(PreDimeInfo);
    	PreDimeInfo = SysUtil.getInstance(CgPreDiInfo.class);
		PreDimeInfo.setDimecg("0032"); //客户账号
    	PreDimeInfo.setDimevl(cplFeeIn.getCustac());	
    	lstCgPreDiInfo.add(PreDimeInfo);
    	
        if(CommUtil.isNotNull(predimLst)){
            for(CgPreDiInfo info : predimLst){
            	PreDimeInfo = SysUtil.getInstance(CgPreDiInfo.class);
            	if (CommUtil.isNull(FeDimeDao.selone_evl_dime(info.getDimevl(), info.getDimecg(), false))) {
        			throw FeError.Chrg.BNASF252();
        		}
            	PreDimeInfo.setDimecg((info.getDimecg()));
            	PreDimeInfo.setDimevl((info.getDimevl()));	
            	lstCgPreDiInfo.add(PreDimeInfo);
            }
        }
    	cplCgprRate.setWdinfo(lstCgPreDiInfo);
		cplCgprRate.setAmount(lShuliang); //数量
		cplCgprRate.setTranam(cplFeeIn.getTranam()); //交易金额
		cplCgprRate.setTranbr(sTranbr); //交易机构
		
		bizlog.debug("lstCgPreDiInfo[%s]", lstCgPreDiInfo);

       /*
        * 场景代码与费种代码二选一，一个场景代码可以对应一个或者多个费种代码
        */
        List<KcpChrgScdf> lstKcp_chrg_scdf = new ArrayList<KcpChrgScdf>();
        if (CommUtil.isNotNull(cplFeeIn.getScencd()) && CommUtil.compare(cplFeeIn.getScencd(), "%") != 0) {
        	lstKcp_chrg_scdf = FeSceneDao.selall_kcp_chrg_scdf_scen(cplFeeIn.getScencd(), false);
        	
            if (CommUtil.isNull(lstKcp_chrg_scdf)) {
                bizlog.debug("场景代码[%s]在场景定义表中未配置,则不收费", cplFeeIn.getScencd());
                throw FeError.Chrg.BNASF014();
            }
        	
        }else{
        	KcpChrgScdf tblKcp_chrg_scdf = SysUtil.getInstance(KcpChrgScdf.class);
        	tblKcp_chrg_scdf.setChrgcd(cplFeeIn.getChrgcd());
        	if(CommUtil.isNull(cplFeeIn.getChrgcd())){
        		throw FeError.Chrg.BNASF076();
        	}
        	lstKcp_chrg_scdf.add(tblKcp_chrg_scdf);
        }
        
        bizlog.debug("场景计费列表lstSfsj[%s]", lstKcp_chrg_scdf);
        for (int i = 0; i < lstKcp_chrg_scdf.size(); i++) {

            /*如果是指定金额收费应该只收第一笔的收费代码*/
            if (cplFeeIn.getChrgsr() == E_CHRGSR.INPUT || (CommUtil.isNotNull(cplFeeIn.getSpcham())
                    && CommUtil.compare(cplFeeIn.getSpcham(), BigDecimal.ZERO) > 0)) {
                if (i >= 1)
                {
                    return cplFeeOut;
                }
            }

            BigDecimal bigStadam = BigDecimal.ZERO; // 标准费用金额
            BigDecimal bigReceam = BigDecimal.ZERO; // 优惠后应收金额
            String sChrgcd = lstKcp_chrg_scdf.get(i).getChrgcd();  //费种代码
    		cplCgprRate.setChrgcd(sChrgcd);// 费种代码

            bizlog.debug("开始处理费种代码[%s][%s]", sChrgcd, cplFeeIn.getScencd());

            /**********************************************
             * 2.1、获取收费代码信息 <br>
             **********************************************/
//            kcp_chrg tblCgtp = Kcp_chrgDao.selectOne_odb1(sChrgcd,U_CRCYCD.RMB, false); //根据费种代码查询收费代码定义表
            KcpChrg tblCgtp = FeCodeDao.selall_kcp_chrg_code(sChrgcd, false);
//            kcp_chrg_subj tblKcpChrgSubj = Kcp_chrg_subjDao.selectOne_odb1(sChrgcd, cplFeeIn.getScencd(), false); //根据费种代码和场景代码查询费用附加属性
            KcpChrgSubj tblKcpChrgSubj = FeCodeDao.selone_kcp_chrg_subj(sChrgcd, cplFeeIn.getScencd(), false);
            /*
             * 对于交易币种为外币，但并未配置此币种的收费规则，则优先查找本分行的第一外币（美元）的规则，再顺序查找第一本币（人民币）的收费规则应用
             */

            if (CommUtil.isNull(tblCgtp))
            {
                throw FeError.Chrg.E0057(sChrgcd, eCrcycd);
            }

            if (CommUtil.compare(tblCgtp.getEfctdt(), CommTools.getBaseRunEnvs().getTrxn_date()) > 0) {
                throw FeError.Chrg.BNASF034();
            }

            if (CommUtil.compare(tblCgtp.getInefdt(), CommTools.getBaseRunEnvs().getTrxn_date()) < 0) {
                throw FeError.Chrg.BNASF035();
            }

            if (CommUtil.compare(eCrcycd,tblCgtp.getCrcycd())!=0 && tblCgtp.getCvcyfg() != E_CVCYFG.YES) {
                //币种与费种代码中定义的不一致，且不允许币种兑换，则报错 
                throw FeError.Chrg.E0139(tblCgtp.getChrgcd(), eCrcycd, tblCgtp.getCrcycd());
            }

            if (CommUtil.isNotNull(tblCgtp.getBrchno())) {
                sAcctbr = tblCgtp.getBrchno(); //记账机构
            }

            BigDecimal bigClexrt = BigDecimal.ONE; //计费兑换比例
            BigDecimal bigJIOYJE_JF = BigDecimal.ZERO; //兑换后金额

//            if (eCrcycd != eClchcy) {
//                throw FeError.Chrg.E2015("交易币种与计费币种不一致，暂不支持");   //  TODO 交易币种与计费币种不一致，暂不支持
//            }
            
            if(CommUtil.isNull(tblKcpChrgSubj)){
            	throw FeError.Chrg.BNASF078();
            }
            
            //费种附加属性处理
            sSubnum = tblKcpChrgSubj.getSubnum(); //科目号
            sIntacc = tblKcpChrgSubj.getIntacc(); //内部账对应顺序号
            sPronum = tblKcpChrgSubj.getPronum(); //产品编号
            sTrinfo = tblKcpChrgSubj.getTrinfo(); //交易信息
            sAcclev = tblKcpChrgSubj.getAcclev(); //对账级别
            sPrmark = tblKcpChrgSubj.getPrmark();//对应产品标志
            sCjsign = tblCgtp.getCjsign(); //县辖维护标志
    		cplCgprRate.setCjsign(sCjsign);
            
            /**********************************************
             * 2.2 最终费用金额小数位处理 <br>
             **********************************************/
            int iCgtpScale = 2;
            int iCgtpRoundingMode = BigDecimal.ROUND_HALF_UP;

            if (CommUtil.isNotNull(tblCgtp.getMndecm()))
                iCgtpScale = tblCgtp.getMndecm().intValue(); //最低小数位数

            if (CommUtil.isNotNull(tblCgtp.getCarrtp())) {

                E_CARRTP eSiswrfsh = CommUtil.toEnum(E_CARRTP.class, tblCgtp.getCarrtp()); //四舍五入方式

                switch (eSiswrfsh) {
                case ROUND: // 四舍五入
                    iCgtpRoundingMode = BigDecimal.ROUND_HALF_UP;
                    break;
                case CARRY: // 进位
                    iCgtpRoundingMode = BigDecimal.ROUND_UP;
                    break;

                // case IGNORE: //不处理
                // case TRUNCATION: //舍位
                default:
                    iCgtpRoundingMode = BigDecimal.ROUND_DOWN;
                    break;
                }
            }

            /**
             * 记录计费轨迹
             */
            {
                String sCgrule = "";
                if (E_ISFAVO.YES == tblCgtp.getIsfavo()) { // 允许优惠
                    if (CommUtil.compare(tblCgtp.getMnfvrt(), BigDecimal.ZERO) > 0) {
                        if (sCgrule.length() > 0)
                            sCgrule += ",";
                        sCgrule += "控制最低优惠(" + (tblCgtp.getMnfvrt()) + "%)";
                    }
                    if (CommUtil.compare(tblCgtp.getMxfvrt(), BigDecimal.ZERO) > 0) {
                        if (sCgrule.length() > 0)
                            sCgrule += ",";
                        sCgrule += "控制最高优惠(" + (tblCgtp.getMxfvrt()) + "%)";
                    }
                }
                else {
                    sCgrule = "不允许优惠";
                }
                /**
                 * 收费代码 <br/>
                 * 收费代码名称 <br/>
                 * 优惠比例 <br/>
                 * 计费金额/档次 <br/>
                 * 规则 <br/>
                 * 标准收费 <br/>
                 * 优惠/控制后收费 <br/>
                 */
                prcChargeTrack(tblCgtp.getChrgcd(), tblCgtp.getChrgna(), null, bigTranam, sCgrule, null, null);
            }
            
            
            /**********************************************
             * 2.3 费用自动计算处理 <br>
             **********************************************/
            if (E_CHRGSR.CALCULATE == cplFeeIn.getChrgsr()) {
                bizlog.debug("自动计算开始");

                /**********************************************
                 * 2.3.1 基于收费代码的单维度费用优惠比例<br>
                 **********************************************/
                if (E_ISFAVO.YES == tblCgtp.getIsfavo()) {
                    /**********************************************
                     * 2.3.1.1基于收费代码的单维度费用优惠比例<br>
                     **********************************************/
					tblCgprRate = ChargeFavo.getMaxSfyh(cplCgprRate);
					bizlog.debug("单一优惠输出tblCgprRate[%s]", tblCgprRate);

                    /**********************************************
                     * 2.3.1.2 基于收费代码的超额优惠比例<br>
                     **********************************************/
					tblCgsmRate = ChargeFavo.prcLjyhbl(cplCgprRate);
					bizlog.debug("超额优惠输出tblCgsmRate[%s]", tblCgsmRate);
					/**********************************************
					 * 2.3.1.3 基于收费代码的多维度组合的计划优惠比例<br>
					 **********************************************/
					tblNcplRate = ChargeFavo.getMaxYhjh(cplCgprRate);
					bizlog.debug("计划优惠输出tblNcplRate[%s]", tblNcplRate);

					/**********************************************
					 * 2.3.1.4  最终优惠比例<br>
					 * 最终优惠为收费惠和计划优惠的折上折<br>
					 * 且最终优惠比例必须在收费代码定义的"最大优惠比例-最小优惠比例"区间<br>
					 **********************************************/
					/**
					 * 最终优惠 = 1.00 - (1.00 - max(单一优惠,超额优惠)) * (1.00 - 计划优惠)
					 */
					bizlog.debug("%s费优惠:%s", sChrgcd, (CommUtil.isNull(tblCgprRate) ? "null" : (tblCgprRate.getFavoir() + " " + tblCgprRate.getExpmsg())));
					bizlog.debug(
							"%s超额优惠:%s",
							sChrgcd,
							(CommUtil.isNull(tblCgsmRate) ? "null" : (tblCgsmRate.getFavoir() + " " + tblCgsmRate.getSmfacd() + "|" + tblCgsmRate.getCrcycd() + " " + tblCgsmRate
									.getExpmsg())));
					bizlog.debug(
							"%s计划优惠:%s",
							sChrgcd,
							(CommUtil.isNull(tblNcplRate) ? "null" : (tblNcplRate.getFavoir() + " " + tblNcplRate.getDiplcd() + "|" + tblNcplRate.getCrcycd() + " " + tblNcplRate
									.getDiplna())));

					String sTrackCgprrtDesc = "-", trackNcplrtDesc = "-";

					BigDecimal bigCgprrt1 = (CommUtil.isNull(tblCgprRate) || CommUtil.isNull(tblCgprRate.getFavoir())) ? BigDecimal.ZERO : tblCgprRate.getFavoir();

					BigDecimal bigCgprrt2 = (CommUtil.isNull(tblCgsmRate) || CommUtil.isNull(tblCgsmRate.getFavoir())) ? BigDecimal.ZERO : tblCgsmRate.getFavoir();

					BigDecimal bigCgprrt3 = (CommUtil.isNull(tblNcplRate) || CommUtil.isNull(tblNcplRate.getFavoir())) ? BigDecimal.ZERO : tblNcplRate.getFavoir();

					if (CommUtil.compare(bigCgprrt1, BigDecimal.ZERO) < 0)
						bigCgprrt1 = BigDecimal.ZERO;
					if (CommUtil.compare(bigCgprrt2, BigDecimal.ZERO) < 0)
						bigCgprrt2 = BigDecimal.ZERO;
					if (CommUtil.compare(bigCgprrt3, BigDecimal.ZERO) < 0)
						bigCgprrt3 = BigDecimal.ZERO;

					if (CommUtil.compare(bigCgprrt1, BigDecimal.ONE) > 0)
						bigCgprrt1 = BigDecimal.ONE;
					if (CommUtil.compare(bigCgprrt2, BigDecimal.ONE) > 0)
						bigCgprrt2 = BigDecimal.ONE;
					if (CommUtil.compare(bigCgprrt3, BigDecimal.ONE) > 0)
						bigCgprrt3 = BigDecimal.ONE;

					if (CommUtil.compare(bigCgprrt1, bigCgprrt2) >= 0) {
						bigCgprrt = bigCgprrt1;

						if (tblCgprRate != null)
							sTrackCgprrtDesc = tblCgprRate.getExpmsg();

					}
					else {
						bigCgprrt = bigCgprrt2;

						if (tblCgsmRate != null)
							sTrackCgprrtDesc = tblCgsmRate.getSmfacd() + " " + tblCgsmRate.getExpmsg();
					}
					if (tblNcplRate != null && CommUtil.isNotNull(tblNcplRate.getDiplcd())) {
						trackNcplrtDesc = tblNcplRate.getDiplcd() + " " + tblNcplRate.getDiplna();
					}

					if (CommUtil.isNotNull(tblCgprRate))
						tblCgprRate.setFavoir(bigCgprrt1);

					if (CommUtil.isNotNull(tblCgsmRate))
						tblCgsmRate.setFavoir(bigCgprrt2);

					if (CommUtil.isNotNull(tblNcplRate))
						tblNcplRate.setFavoir(bigCgprrt3);

					bizlog.debug("%s单维优惠:%s", sChrgcd, bigCgprrt);
					bizlog.debug("%s计划优惠:%s", sChrgcd, bigCgprrt3);
					if (CommUtil.compare(bigCgprrt, BigDecimal.ZERO) != 0 || CommUtil.compare(bigCgprrt3, BigDecimal.ZERO) != 0) {
						bigFinlrt = BigDecimal.ONE.subtract(BigDecimal.ONE.subtract(bigCgprrt).multiply(BigDecimal.ONE.subtract(bigCgprrt3)));

						bizlog.debug("%s折上折优惠:%s", sChrgcd, bigFinlrt);
					}
					else {

						bizlog.debug("%s无优惠:%s", sChrgcd, bigFinlrt);
					}
					if (CommUtil.compare(tblCgtp.getMnfvrt().divide(new BigDecimal("100")), BigDecimal.ZERO) > 0) {
						if (CommUtil.compare(tblCgtp.getMnfvrt().divide(new BigDecimal("100")), bigFinlrt) > 0)
							bigFinlrt = tblCgtp.getMnfvrt().divide(new BigDecimal("100"));
					}
					bizlog.debug("bigFinlrt[%f]", bigFinlrt);
					if (CommUtil.compare(tblCgtp.getMxfvrt().divide(new BigDecimal("100")), BigDecimal.ZERO) > 0) {
						if (CommUtil.compare(tblCgtp.getMxfvrt().divide(new BigDecimal("100")), bigFinlrt) < 0)
							bigFinlrt = tblCgtp.getMxfvrt().divide(new BigDecimal("100"));
					}
					bizlog.debug("bigFinlrt[%f]", bigFinlrt);
					bizlog.debug("%s最终优惠:%s", sChrgcd, bigFinlrt);
					/**
					 * 记录计费轨迹
					 */
					{
						/**
						 * 收费类别 <br/>
						 * 收费项目 <br/>
						 * 优惠比例 <br/>
						 * 计费金额/档次 <br/>
						 * 规则 <br/>
						 * 标准收费 <br/>
						 * 优惠/控制后收费 <br/>
						 */
						prcChargeTrack(null, "收费优惠比例", bigCgprrt1, null, tblCgprRate.getExpmsg(), null, null);
						prcChargeTrack(null, "累积优惠比例", bigCgprrt2, null, tblCgsmRate.getExpmsg(), null, null);
						prcChargeTrack(null, "单维优惠比例", bigCgprrt, null, sTrackCgprrtDesc, null, null);
						prcChargeTrack(null, "计划优惠比例", bigCgprrt3, null, trackNcplrtDesc, null, null);
						prcChargeTrack(null, "最终优惠比例", bigFinlrt, null, null, null, null);
					}
                }
                else {
                    bizlog.debug("%s不允许优惠", sChrgcd);
                    
                    /**
					 * 记录计费轨迹
					 */
					{
						/**
						 * 收费类别 <br/>
						 * 收费项目 <br/>
						 * 优惠比例 <br/>
						 * 计费金额/档次 <br/>
						 * 规则 <br/>
						 * 标准收费 <br/>
						 * 优惠/控制后收费 <br/>
						 */
						prcChargeTrack(null, "单维优惠比例", BigDecimal.ZERO, null, "-", null, null);
						prcChargeTrack(null, "计划优惠比例", BigDecimal.ZERO, null, "-", null, null);
						prcChargeTrack(null, "最终优惠比例", BigDecimal.ZERO, null, null, null, null);
					}
                }

                cplCgprRate.setFinpec(bigFinlrt);// 最终优惠比例

                /**********************************************
                 * 2.3.2 基于收费代码/场景值，找到对应计费公式 <br>
                 **********************************************/
                bizlog.debug("%sjxx%sss", sChrgcd, cplFeeIn.getScencd());
                bizlog.debug("[%s][%s][%s][%s]", sTrandt, sChrgcd, cplFeeIn.getScencd(), eClchcy);

                List<KcpChrgFmex> lstDetls = getChargeFormInfo(sTrandt, sChrgcd, cplFeeIn.getScencd(), eClchcy);

                bizlog.debug("lstDetls[%s]", lstDetls);
                for (KcpChrgFmex tblDetl : lstDetls) {
                    /**********************************************
                     * 2.3.2.1 根据收费代码，找到计算公式，计算出金额 <br>
                     **********************************************/
                    CgGetJFGSInfo cplCcgcdInput = SysUtil.getInstance(CgGetJFGSInfo.class);
                    cplCcgcdInput.setCgwdinfo(cplCgprRate);
                    cplCcgcdInput.setSfdmInfo(tblCgtp);// 收费代码信息
                    cplCcgcdInput.setSfjxInfo(tblDetl);// 收费公式信息

                    /*收费公式计算*/
                    CgSfgsjs cgcdOuput = calOneChrgForm(cplCcgcdInput);

                    if (CommUtil.isNull(cgcdOuput)) {
                        bizlog.debug("获取收费公式信息为空");
                        throw FeError.Chrg.BNASF055();
                    }

                    /**********************************************
                     * 2.3.2.2 累计该公式下计算金额 <br>
                     **********************************************/
                    if (CommUtil.isNotNull(cgcdOuput)) {

                        bigStadam = bigStadam.add(cgcdOuput.getCalamt()); //费用计算金额
                        bigReceam = bigReceam.add(cgcdOuput.getCgscam()); //费用应收金额
                        bizlog.debug("费用计算金额%s", cgcdOuput.getCalamt());
                        bizlog.debug("费用应收金额%s", cgcdOuput.getCgscam());

                    }

                }

                bigStadam = bigStadam.setScale(iCgtpScale, iCgtpRoundingMode); // 标准收费金额
                bigReceam = bigReceam.setScale(iCgtpScale, iCgtpRoundingMode); // 优惠后收费金额
                //cplWDInfo.setStdamt(bigStadam); // 计费标准应收金额
                if (CommUtil.compare(bigStadam, BigDecimal.ZERO) == 0 && CommUtil.compare(bigReceam, BigDecimal.ZERO) == 0) {
                    bizlog.debug("计费标准收费和优惠后收费为0，表示不收费");
//                    return cplFeeOut;
                }

                /**
                 * 记录计费轨迹
                 */
                {
                    /**
                     * 收费代码 <br/>
                     * 收费项目 <br/>
                     * 优惠比例 <br/>
                     * 计费金额/档次 <br/>
                     * 规则 <br/>
                     * 标准收费 <br/>
                     * 优惠/控制后收费 <br/>
                     */
                    prcChargeTrack(null, "计费合计", null, null, null, bigStadam, bigReceam);
                }
                
                /**********************************************
    			 * 2.3.3 基于收费代码的收费金额控制<br>
    			 **********************************************/
    			CgGetKZInfo cplCglmInput = SysUtil.getInstance(CgGetKZInfo.class);
    			cplCglmInput.setRecvam(bigReceam);// 优惠后应收金额
    			bizlog.debug("优惠后应收金额[%f]", bigReceam);
    				
				bizlog.debug("%s无收费金额控制", sChrgcd);
				bigChrgam = bigReceam;
    			/**
    			 * 记录计费轨迹
    			 */
    			{
    				/**
    				 * 收费类别 <br/>
    				 * 收费项目 <br/>
    				 * 优惠比例 <br/>
    				 * 计费金额/档次 <br/>
    				 * 规则 <br/>
    				 * 标准收费 <br/>
    				 * 优惠/控制后收费 <br/>
    				 */
    				prcChargeTrack(null, "收费金额控制", null, null, (tblCglmDetl == null ? "-" : tblCglmDetl.getConexp()), null,
    						(tblCglmDetl == null ? null : tblCglmDetl.getContam()));
    			}
                

                /**********************************************
                 * 2.3.3最终费用金额小数位处理 <br>
                 **********************************************/
                bigChrgam = bigChrgam.setScale(iCgtpScale, iCgtpRoundingMode); // 优惠后收费

                /**
                 * 记录计费轨迹
                 */
                {
                    /**
                     * 收费类别 <br/>
                     * 收费项目 <br/>
                     * 优惠比例 <br/>
                     * 计费金额/档次 <br/>
                     * 规则 <br/>
                     * 标准收费 <br/>
                     * 优惠/控制后收费 <br/>
                     */
                    prcChargeTrack(null, "实际应收金额", null, null, "自动计算", bigStadam, bigChrgam);
                }
            }
            /*费用自动计算结束*/

            /**********************************************
             * 2.4 计费金额 <br>
             **********************************************/
            BigDecimal bigStadam_out = BigDecimal.ZERO; //标准金额
            BigDecimal bigChrgam_out = BigDecimal.ZERO; //优惠后应收费用金额
            bizlog.debug("收费金额来源[%s]", cplFeeIn.getChrgsr());
            if (E_CHRGSR.INPUT == cplFeeIn.getChrgsr()) {
                bigStadam_out = bigTranam;
                bigChrgam_out = bigTranam;
            }
            else {
                bigStadam_out = bigStadam;
                bigChrgam_out = bigChrgam;
            }

            bizlog.debug("计费标准费用金额【%f】", bigStadam_out);
            bizlog.debug("计费优惠后应收金额【%f】", bigChrgam_out);
            /**********************************************
             * 2.5 收费分成<br>
             * 判断该收费代码下是否分成<br>
             **********************************************/
            /*
            if (E_FEDIVE.YES == tblCgtp.getFedive() && (CommUtil.isNotNull(cplFeeIn.getOubrno())) || (CommUtil.isNotNull(cplFeeIn.getInbrno()))) {
				if (CommUtil.isNotNull(tblCgtp.getBllwtp()) && E_BLLWTP.OWE != tblCgtp.getBllwtp() && E_BLLWTP.GRACE_PERIOD != tblCgtp.getBllwtp()
						&& E_BLLWTP.NEXT_CYCLE != tblCgtp.getBllwtp()) {
					throw FeError.Chrg.E2015("允许部分收迄的不允许分成");
				}
				tblCgdv = PbChargCalPublic.qryDvidInfo(sChrgcd, eCrcycd, sTrandt);

				bizlog.debug("收费分成比率 转入行[%f] 转出行[%f] 交易行[%f]", tblCgdv.getOudvrt().divide(new BigDecimal("100")), tblCgdv.getIndvrt().divide(new BigDecimal("100")), tblCgdv
						.getTrdvrt().divide(new BigDecimal("100")));
				if (CommUtil.compare((tblCgdv.getOudvrt().add(tblCgdv.getIndvrt()).add(tblCgdv.getTrdvrt())).divide(new BigDecimal("100")), BigDecimal.ONE) > 0) {
					throw PbError.PbComm.E0103();
				}
				bigDvidam = cplFeeIn.getDvidam();

			}
            bigDvidam = cplFeeIn.getDvidam(); //收费分成金额

	*/
            tblCgdv = KcpChrgDvidDao.selectOne_odb1(sChrgcd, eCrcycd, CommTools.getBaseRunEnvs().getTrxn_date(), false);
            //指定收费金额
            if (eAppoint == E_YES___.YES) {

                bizlog.debug("指定金额收费");

                if (CommUtil.compare(eClchcy,cplFeeIn.getChrgcy()) !=0  && tblCgtp.getCvcyfg() != E_CVCYFG.YES) {

                    throw FeError.Chrg.E0139(tblCgtp.getChrgcd(), cplFeeIn.getChrgcy(), tblCgtp.getCrcycd());

                }

                bigChexrt = BigDecimal.ZERO; //收费兑换比例
                bigRecvam = bigSpcham; //应收费用金额=指定金额
            }

//            else if (eClchcy != cplFeeIn.getChrgcy()) {
//
//                throw FeError.Chrg.E2015("计费币种与收费币种不一致，暂不支持"); //  TODO 计费币种与收费币种不一致，暂不支持
//            }

            else {
                bigChexrt = BigDecimal.ONE; //收费兑换比例
                bigRecvam = bigChrgam_out; //应收费用金额
            }
            bizlog.debug("应收费金额bigRecvam[%s]", bigRecvam);

            // 赋值费用信息结构	
            CgFEEINFO cplFeeinfo = SysUtil.getInstance(CgFEEINFO.class);
            cplFeeinfo.setTrandt(sTrandt); //交易日期
            cplFeeinfo.setTrnseq(sTransq); //交易流水
            cplFeeinfo.setSequno(Long.parseLong("0")); //顺序号
            cplFeeinfo.setCgpyrv(tblCgtp.getCgpyrv()); //费用收付标志
            cplFeeinfo.setChrgcd(sChrgcd); //收费代码
            cplFeeinfo.setChrgna(tblCgtp.getChrgna()); //费种代码名称
            cplFeeinfo.setTrancy(eCrcycd);
            if (CommUtil.isNotNull(eClchcy)) {
                cplFeeinfo.setClchcy(eClchcy); //计费币种
            }
            cplFeeinfo.setSubnum(sSubnum); //科目号
            cplFeeinfo.setIntacc(sIntacc); //内部账对应顺序号
            cplFeeinfo.setPronum(sPronum); //产品编号
            cplFeeinfo.setTrinfo(sTrinfo); //交易信息
            cplFeeinfo.setClcham(bigStadam_out); //计算费用金额
            cplFeeinfo.setCgscam(bigChrgam_out); //费用应收金额
            cplFeeinfo.setFavpec(bigFinlrt); //优惠比例
            cplFeeinfo.setChrgcy(eChrgcy); //收费币种
            cplFeeinfo.setRecvam(bigRecvam); //应收费用金额 
            cplFeeinfo.setAcclam(BigDecimal.ZERO); //实收费用金额
            cplFeeinfo.setArrgam(bigRecvam); //欠费金额
            cplFeeinfo.setDvidam(bigDvidam); //收费分成金额
            if (CommUtil.isNotNull(tblCgdv)) {
				bizlog.debug("分成参数tblCgdv[%s]", tblCgdv);
				cplFeeinfo.setOudvrt(tblCgdv.getOudvrt().divide(new BigDecimal("100"))); //转出行分成比例
				cplFeeinfo.setIndvrt(tblCgdv.getIndvrt().divide(new BigDecimal("100"))); //转入行分成比例
				cplFeeinfo.setTrdvrt(tblCgdv.getTrdvrt().divide(new BigDecimal("100"))); //交易行分成比例
				cplFeeinfo.setSpavrt(tblCgdv.getSpavrt().divide(new BigDecimal("100"))); //备用行分成比例
			}

            cplFeeinfo.setOudvam(BigDecimal.ZERO); //转出行分成金额 
            cplFeeinfo.setIndvam(BigDecimal.ZERO); //转入行分成金额 
            cplFeeinfo.setTrdvam(BigDecimal.ZERO); //交易行分成金额 
            cplFeeinfo.setSpavam(BigDecimal.ZERO); //备用行分成金额

			if (CommUtil.isNotNull(tblNcplRate)) {
				cplFeeinfo.setChgpln(tblNcplRate.getDiplcd());
			}

			listnm lstnm = SysUtil.getInstance(listnm.class);
			lstnm.setAmount(lShuliang);//数量
			lstnm.setTranam(cplFeeIn.getTranam()); //交易金额
			lstnm.setChrgcd(sChrgcd);//费种代码
			lstnm.setClcham(bigStadam_out);//计算费用金额
			lstnm.setDisrat(bigFinlrt); //优惠比例
			lstnm.setDircam(bigRecvam); //优惠后应收金额
			lstnm.setIntacc(sIntacc);//内部帐对应顺序号
			lstnm.setPrmark(sPrmark); //对应产品标志
			lstnm.setPronum(sPronum); //产品编号
			lstnm.setSubnum(sSubnum); //科目号
			lstnm.setTrinfo(sTrinfo); //交易信息
			lstnm.setAcclev(sAcclev); //对账级别
			lstnm.setDioage(cplFeeIn.getOubrno());
			lstnm.setDiwage(cplFeeIn.getInbrno());
			lstnm.setDitage(sTranbr);
			lstnm.setDifage(cplFeeIn.getDifage());
			if (CommUtil.isNotNull(tblCgdv)) {
				bizlog.debug("分成参数tblCgdv[%s]", tblCgdv);
				lstnm.setDioamo(tblCgdv.getOudvrt().divide(new BigDecimal("100").multiply(bigRecvam)));
				lstnm.setDiwamo(tblCgdv.getIndvrt().divide(new BigDecimal("100").multiply(bigRecvam)));
				lstnm.setDitamo(tblCgdv.getTrdvrt().divide(new BigDecimal("100").multiply(bigRecvam)));
				lstnm.setDifamo(tblCgdv.getSpavrt().divide(new BigDecimal("100").multiply(bigRecvam)));
			}
            cplFeeOut.getListnm().add(lstnm);
        }
        
        /*计费结束，返回输出结构信息*/
        bizlog.parm("cplFeeOut[%s]", cplFeeOut);
        bizlog.method("calCharge end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        return cplFeeOut;
    }
    
    /**
     * @Author wuxq
     *         <p>
     *         <li>功能说明：记录计费过程(计费轨迹)</li>
     *         </p>
     * @param sChrgcd
     *        收费类型 String
     * @param sChrgpj
     *        收费项目 String
     * @param bigFavpec
     *        优惠比例 BigDecimal
     * @param bigCgamou
     *        计费金额 BigDecimal
     * @param sCgrule
     *        计费规则 String
     * @param bigStadam
     *        标准收费 BigDecimal
     * @param bigFvcgam
     *        优惠/控制后收费 BigDecimal
     */
    public void prcChargeTrack(String sChrgcd, String sChrgpj, BigDecimal bigFavpec, BigDecimal bigCgamou, String sCgrule, BigDecimal bigStadam, BigDecimal bigFvcgam) {

        CgJfgcxn tblTrack = SysUtil.getInstance(CgJfgcxn.class);

        tblTrack.setChrgcd(sChrgcd);
        tblTrack.setChrgpj(sChrgpj);
        tblTrack.setFavpec(bigFavpec);
        tblTrack.setCgamou(bigCgamou);
        tblTrack.setCgrule(sCgrule);
        tblTrack.setStadam(bigStadam);
        tblTrack.setFvcgam(bigFvcgam);

        prcChargeTrack(tblTrack);
    }
    
    /**
     * @Author wuxq
     *         <p>
     *         <li>功能说明：记录计费过程(计费轨迹)</li>
     *         </p>
     * @param tblTrack
     */
    public void prcChargeTrack(CgJfgcxn tblTrack) {

        LocalTmpTableCaches.getList(CgJfgcxn.class.getSimpleName()).getValue().add(tblTrack);

    }
    
    /**
     * @Author wuxq
     *         <p>
     *         <li>功能说明：根据场景值+收费代码，找到对应计费公式或收费代码</li>
     *         </p>
     * @param sEfctdt
     *        生效日期
     * @param sChrgcd
     *        收费代码
     * @param sScencd
     *        场景代码
     * @param eCrcycd
     *        币种
     * @return
     */
    public static List<KcpChrgFmex> getChargeFormInfo(String sEfctdt, String sChrgcd, String sScencd, String eCrcycd) {

        bizlog.method("getChargeFormInfo begin >>>>>>>>>>>>>>>>>>>>>");
        bizlog.parm("sEfctdt[%s]sChrgcd[%s]sScencd[%s]eCrcycd[%s]", sEfctdt, sChrgcd, sScencd, eCrcycd);

        KcpChrgFmex tblPara = SysUtil.getInstance(KcpChrgFmex.class);
        tblPara.setChrgcd(sChrgcd); //收费代码 
        tblPara.setChrgfm(E_TESHUZFC.LIKALL.getValue()); //收费公式
        tblPara.setCrcycd(eCrcycd); //货币代号
        tblPara.setScencd(sScencd); //场景代码
        tblPara.setEfctdt(sEfctdt); //生效日期

        List<KcpChrgFmex> lstTmps = qryChrgFormExp(tblPara);

        bizlog.parm("lstTmps[%s]", lstTmps);
        bizlog.method("getChargeFormInfo end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        return lstTmps;
    }
    
    /**
     * @Author wuxq
     *         <p>
     *         <li>功能说明：获取收费代码解析表信息</li>
     *         </p>
     * @param tblPara
     * @return
     */
    public static List<KcpChrgFmex> qryChrgFormExp(KcpChrgFmex tblPara) {

        bizlog.method("qryChrgFormExp begin >>>>>>>>>>>>>>>>>>>>>>");
        bizlog.parm("tblPara[%s]", tblPara);

        if (CommUtil.isNull(tblPara.getChrgcd())) {
            throw FeError.Chrg.BNASF076();
        }

        if (CommUtil.isNull(tblPara.getScencd())) {
            throw FeError.Chrg.BNASF016();

        }
        if (CommUtil.isNull(tblPara.getCrcycd())) {
            throw FeError.Chrg.BNASF156();

        }
        if (CommUtil.isNull(tblPara.getEfctdt())) {
            throw FeError.Chrg.BNASF207();

        }
        if (CommUtil.isNull(tblPara.getChrgfm())) {
            throw FeError.Chrg.BNASF142();

        }

        // 2.1、获取相等币种信息的收费代码
        String sScencd = "%";
        if (CommUtil.isNotNull(tblPara.getScencd())) {
            sScencd = tblPara.getScencd();
        }

        String sCorpno = BusiTools.getFrdm(KcpChrgFmex.class);
        List<KcpChrgFmex> lstTmps = FeFormulaDao.selChargeFormExpInfo(sCorpno, tblPara.getChrgcd(), sScencd, tblPara.getEfctdt(), tblPara.getCrcycd(), true);

        if (CommUtil.isNull(lstTmps)) {
            throw FeError.Chrg.BNASF394();
 
        }
        bizlog.parm("lstTmps[%s]", lstTmps);
        bizlog.method("qryChrgFormExp end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        return lstTmps;
    }
    
    /**
     * @Author wuxq
     *         <p>
     *         功能说明：收费代码下的单条公式计费(含分层计费):针对向客户提供的某一种服务的定价政策，定义了具体的计价方法：按比例
     *         、按额度计费，按全量、按增量分层计费。</li>
     *         </p>
     * @param cplCcgcdInput
     * @return Cgsfgsjs
     */
    public CgSfgsjs calOneChrgForm(CgGetJFGSInfo cplCcgcdInput) {

        bizlog.method("calOneChrgForm begin >>>>>>>>>>>>>>>>>>>>>>");
        bizlog.parm("cplCcgcdInput[%s]", cplCcgcdInput);

        String sMessage = null;
        bizlog.debug("begin>>>>>>公式计算：calCgcdFee：cplCcgcdInput[%s]", cplCcgcdInput);
        CgSfgsjs tblCalResult = SysUtil.getInstance(CgSfgsjs.class); //收费公式计算结果

        String sChrgcd = cplCcgcdInput.getCgwdinfo().getChrgcd(); //收费代码
        KcpChrg tblCgtp = cplCcgcdInput.getSfdmInfo(); //收费代码定义信息

        KcpChrgFmex tblCgtpDetl = cplCcgcdInput.getSfjxInfo(); // 收费公式解析信息
        String sChrgfm = tblCgtpDetl.getChrgfm(); //收费公式代码

        String sBrchno = CommTools.getBaseRunEnvs().getTrxn_branch(); //机构号
        String sCityno = ""; //分行代号
        BigDecimal bigTranam = cplCcgcdInput.getCgwdinfo().getTranam(); //交易金额
        bizlog.debug("交易金额[%f]", bigTranam);
        BigDecimal bigTrnAmt = BigDecimal.ZERO; // 交易金额/笔数
        long lTranct = cplCcgcdInput.getCgwdinfo().getAmount(); //数量
        BigDecimal bigTranctBD = new BigDecimal(lTranct); //交易笔数
        E_LYSPTP eLayerm = tblCgtp.getLysptp(); //分层方式
        String sTrandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期

        if (E_FELYTP.AMT == tblCgtp.getFelytp()) {
            bigTrnAmt = bigTranam; //按金额
        }

        if (E_FELYTP.NUM == tblCgtp.getFelytp()) {
            bigTrnAmt = bigTranctBD; //按笔数
        }

        bizlog.debug("交易金额/笔数[%s]", bigTrnAmt);
        
        BigDecimal bigChprrt = cplCcgcdInput.getCgwdinfo().getFinpec(); //优惠比例

        if (CommUtil.isNull(bigChprrt))
            bigChprrt = BigDecimal.ZERO;

        /**
         * 定位一组收费公式，以下列字段:<br/>
         * Chrgfm公式代码<br/>
         * Sbbkcd分行号<br/>
         * jigouhao机构号<br/>
         * bizhongg币种<br/>
         * 为条件, 从支行>分行>总行的顺寻查找到一组符合计费币种的kcp_chrg_fmdt记录<br>
         * 
         */
        KcpChrgFmdf tblSfgsdy = FeFormulaDao.selone_kcp_chrg_fmdf(sChrgfm, sTrandt, false);//根据收费公式查询收费公式定义表
        
        if (CommUtil.isNull(tblSfgsdy))
        {
            bizlog.debug("没有对应的公式代码[%s]", sChrgfm);
//            throw FeError.Chrg.E9999("没有对应的公式代码" + sChrgfm);
            return null;
        }

        E_YES___ eInclfg = tblSfgsdy.getInclfg(); //是否包含下限标志
        List<KcpChrgFmdt> lstCgcds;
        
        //add 2016/12/21 songlw 根据县辖维护标志判断所取数据标准
        //String centbr = BusiTools.getBusiRunEnvs().getCentbr();
        String centbr = BusiTools.getBusiRunEnvs().getCentbr();
        if(E_CJSIGN.YES == tblCgtp.getCjsign()){
        	lstCgcds = getChrgFormDetlLst(sChrgfm, sCityno, sBrchno, tblCgtp.getCrcycd(), bigTrnAmt, eLayerm);
        	
        	if(lstCgcds.size() <= 0){
        		lstCgcds = getChrgFormDetlLst(sChrgfm, sCityno, centbr, tblCgtp.getCrcycd(), bigTrnAmt, eLayerm);
        	}
        	
        }else{
    		lstCgcds = getChrgFormDetlLst(sChrgfm, sCityno, centbr, tblCgtp.getCrcycd(), bigTrnAmt, eLayerm);
        }
        
        int iCount = lstCgcds.size();
        
        bizlog.debug("符合条件的公式个数：iCount%s", iCount);

        if (iCount <= 0) {
            sMessage = "费种代码[" + sChrgcd + "]未定义任何有效收费公式[" + sChrgfm + "]";
            bizlog.debug("费种代码[%s]未定义任何有效收费公式[%s]", sMessage, sChrgfm);
            throw FeError.Chrg.BNASF356();
        }

        if (CommUtil.isNull(eLayerm)) {
            bizlog.debug("费种代码[%s]未定义有效分层方式", sChrgcd);
            throw FeError.Chrg.E0009(sChrgcd);
        }

        int i = 0;
        BigDecimal bigCalcamtmp = BigDecimal.ZERO; // 标准收费金额
        BigDecimal bigReceamtmp = BigDecimal.ZERO; // 优惠后收费金额
        KcpChrgFmdt tblCgcdmx = SysUtil.getInstance(KcpChrgFmdt.class); // 收费公式代码表明细
        E_CUFETP eClcgtp = null; // 计费类型
        BigDecimal bigChrgrt = BigDecimal.ZERO; // 收费比例
        BigDecimal bigSngpic = BigDecimal.ZERO; // 单价
        BigDecimal bigLwxtam = BigDecimal.ZERO; // 最低金额
        BigDecimal bigHistam = BigDecimal.ZERO; // 最高金额

        /**
         * 计费轨迹
         */
        String sTrackCgitem = null; // 收费项目
        BigDecimal bigTrackCgprrt = bigChprrt; // 优惠比例
        BigDecimal bigTrackTranam = BigDecimal.ZERO; // 计费金额/档次
        String sTrackCgrule = ""; // 规则
        bizlog.debug("分层方式eLayerm[%s]", eLayerm);

        switch (eLayerm) {
        case NO: // 不分层
        case OVAMNT: // 全量
            bizlog.debug("进入全量分层");

            if (E_LYSPTP.NO == eLayerm) {
                if (iCount != 1) {
                    throw FeError.Chrg.E1020();
                }
            }
            else {
                for (i = 0; i < iCount; i++) {
                    tblCgcdmx = lstCgcds.get(i);
                    BigDecimal bigTrnAmt1 = tblCgcdmx.getLimiam(); //金额区间金额下限
                    if (eInclfg == E_YES___.YES || eInclfg == null)
                    {
                        if (CommUtil.compare(bigTrnAmt, BigDecimal.ZERO) != 0 && CommUtil.compare(bigTrnAmt1, bigTrnAmt) >= 0)
                            break;
                    } else if (eInclfg == E_YES___.NO)
                    {
                        if (CommUtil.compare(bigTrnAmt, BigDecimal.ZERO) != 0 && CommUtil.compare(bigTrnAmt1, bigTrnAmt) > 0)
                            break;
                    }
                }

                i--;
                if (i < 0)
                    i = 0;
            }

            tblCgcdmx = lstCgcds.get(i);
            bizlog.debug("第[%s]条公式[%s]", i, tblCgcdmx);

            eClcgtp = tblCgcdmx.getCufetp(); //计费类型
            bigChrgrt = tblCgcdmx.getChrgrt(); //收费比例
            bigSngpic = tblCgcdmx.getPecgam(); //计费单价
            bigLwxtam = tblCgcdmx.getCgmnam(); //最低金额
            bigHistam = tblCgcdmx.getCgmxam(); //最高金额

            sTrackCgitem = "计费公式(" + tblCgcdmx.getChrgfm() + ")";

            if (E_CUFETP.R == eClcgtp) {

                // 按比例

                bizlog.debug("按比例收费：收费比例：bigChrgrt [%f]", bigChrgrt);
                bizlog.debug("按比例收费：收费金额：bigTranam [%f]", bigTranam);
                //费用金额 = 收费比例*计费金额/100 
                bigCalcamtmp = bigChrgrt.multiply(bigTranam).divide(new BigDecimal("100"));
                tblCalResult.setCalamt(bigCalcamtmp); //标准费用金额

                if (sTrackCgrule.length() > 0)
                    sTrackCgrule += ",";
                sTrackCgrule = "按比例(" + bigChrgrt + "%)";

                /**
                 * 最低金额控制
                 */
                if (CommUtil.compare(bigLwxtam, BigDecimal.ZERO) > 0) {
                    if (CommUtil.compare(bigLwxtam, bigCalcamtmp) > 0) {
                        bigCalcamtmp = bigLwxtam;
                    }
                    if (sTrackCgrule.length() > 0)
                        sTrackCgrule += ",";
                    sTrackCgrule += "控制最低金额(" + bigLwxtam + ")";
                }

                /**
                 * 最高金额控制
                 */
                if (CommUtil.compare(bigHistam, BigDecimal.ZERO) > 0) {
                    if (CommUtil.compare(bigHistam, bigCalcamtmp) < 0) {
                        bigCalcamtmp = bigHistam;
                    }
                    if (sTrackCgrule.length() > 0)
                        sTrackCgrule += ",";
                    sTrackCgrule += "控制最高金额(" + bigHistam + ")";
                }

                // 优惠后收费金额 = 标准收费金额*(1-优惠比例) 
                bigReceamtmp = bigCalcamtmp.multiply(BigDecimal.ONE.subtract(bigChprrt));
                bizlog.debug("优惠后金额bigReceamtmp[%s]", bigReceamtmp);
                tblCalResult.setCgscam(bigReceamtmp); //费用应收金额
                bigTrackTranam = bigTrnAmt; //计费金额

            }
            else if (E_CUFETP.S == eClcgtp) {

                //按单价（与数量有关）

                if ((lTranct < 1)) {
                    throw FeError.Chrg.E1021();
                }

                //费用金额=单价*数量
                bigCalcamtmp = bigSngpic.multiply(bigTranctBD);
                tblCalResult.setCalamt(bigCalcamtmp); //标准费用金额

                if (sTrackCgrule.length() > 0)
                    sTrackCgrule += ",";
                sTrackCgrule = "按单价（与数量有关）(" + bigSngpic + ")";

                /**
                 * 最低金额控制
                 */
                if (CommUtil.compare(bigLwxtam, BigDecimal.ZERO) > 0) {
                    if (CommUtil.compare(bigLwxtam, bigCalcamtmp) > 0) {
                        bigCalcamtmp = bigLwxtam;
                    }
                    if (sTrackCgrule.length() > 0)
                        sTrackCgrule += ",";
                    sTrackCgrule += "控制最低金额(" + bigLwxtam + ")";
                }

                /**
                 * 最高金额控制
                 */
                if (CommUtil.compare(bigHistam, BigDecimal.ZERO) > 0) {
                    if (CommUtil.compare(bigHistam, bigCalcamtmp) < 0) {
                        bigCalcamtmp = bigHistam;
                    }
                    if (sTrackCgrule.length() > 0)
                        sTrackCgrule += ",";
                    sTrackCgrule += "控制最高金额(" + bigHistam + ")";
                }

                //优惠后收费金额=费用应收金额*(1-优惠比例)
                bigReceamtmp = bigCalcamtmp.multiply(BigDecimal.ONE.subtract(bigChprrt));
                bizlog.debug("按单价收：优惠后金额bigReceamtmp[%f]", bigReceamtmp);
                tblCalResult.setCgscam(bigReceamtmp); //费用应收金额

                bigTrackTranam = bigTranctBD; //计费笔数

            } else if (E_CUFETP.N == eClcgtp) {

                //按单价（与数量无关）

                //费用金额=单价 
                bigCalcamtmp = bigSngpic;
                tblCalResult.setCalamt(bigCalcamtmp); //标准收费金额

                if (sTrackCgrule.length() > 0)
                    sTrackCgrule += ",";
                sTrackCgrule = "按单价（与数量无关）(" + bigSngpic + ")";

                /**
                 * 最低金额控制
                 */
                if (CommUtil.compare(bigLwxtam, BigDecimal.ZERO) > 0) {
                    if (CommUtil.compare(bigLwxtam, bigCalcamtmp) > 0) {
                        bigCalcamtmp = bigLwxtam;
                    }
                    if (sTrackCgrule.length() > 0)
                        sTrackCgrule += ",";
                    sTrackCgrule += "控制最低金额(" + bigLwxtam + ")";
                }

                /**
                 * 最高金额控制
                 */
                if (CommUtil.compare(bigHistam, BigDecimal.ZERO) > 0) {
                    if (CommUtil.compare(bigHistam, bigCalcamtmp) < 0) {
                        bigCalcamtmp = bigHistam;
                    }
                    if (sTrackCgrule.length() > 0)
                        sTrackCgrule += ",";
                    sTrackCgrule += "控制最高金额(" + bigHistam + ")";
                }

                //优惠后应收金额=标准收费金额*(1-优惠比例)
                bigReceamtmp = bigCalcamtmp.multiply(BigDecimal.ONE.subtract(bigChprrt));
                bizlog.debug("按单价收：优惠后金额bigReceamtmp[%f]", bigReceamtmp);
                tblCalResult.setCgscam(bigReceamtmp); //费用应收金额

                bigTrackTranam = BigDecimal.ONE; //计费笔数

            }

            tblCalResult.setCalamt(bigCalcamtmp); //标准收费金额
            tblCalResult.setCgscam(bigReceamtmp); //优惠后应收费用金额

            /**
             * 记录计费轨迹
             */
            {
                /**
                 * 收费类别 <br/>
                 * 收费项目 <br/>
                 * 优惠比例 <br/>
                 * 计费金额/档次 <br/>
                 * 规则 <br/>
                 * 标准收费 <br/>
                 * 优惠/控制后收费 <br/>
                 */
                prcChargeTrack(null, sTrackCgitem, bigTrackCgprrt, bigTrackTranam, sTrackCgrule, bigCalcamtmp, bigReceamtmp);
            }
            break;

        case MANUAL: // 超额分层

            bizlog.debug("进入超额分层");
            BigDecimal bigCalcamtmp1 = BigDecimal.ZERO;
            BigDecimal bigReceamtmp1 = BigDecimal.ZERO;

            for (i = iCount; i > 0; i--) {
                tblCgcdmx = lstCgcds.get(i - 1);
                BigDecimal bigTrnAmt1 = tblCgcdmx.getLimiam(); //金额区间金额下限
                if (eInclfg == E_YES___.YES || eInclfg == null)
                {
                    if (CommUtil.compare(bigTrnAmt, BigDecimal.ZERO) != 0 && CommUtil.compare(bigTrnAmt1, bigTrnAmt) >= 0)
                        continue;
                } else if (eInclfg == E_YES___.NO)
                {
                    if (CommUtil.compare(bigTrnAmt, BigDecimal.ZERO) != 0 && CommUtil.compare(bigTrnAmt1, bigTrnAmt) > 0)
                        break;
                }

                eClcgtp = tblCgcdmx.getCufetp(); //计费类型
                bigChrgrt = tblCgcdmx.getChrgrt(); //收费比例
                bigSngpic = tblCgcdmx.getPecgam(); //计费单价
                bigLwxtam = tblCgcdmx.getCgmnam(); //最低金额
                bigHistam = tblCgcdmx.getCgmxam(); //最高金额

                sTrackCgitem = "计费公式(" + tblCgcdmx.getChrgfm() + ")"; // 收费项目
                bigTrackTranam = bigTrnAmt; // 计费金额/档次
                sTrackCgrule = ""; // 规则

                switch (eClcgtp) {

                case R: // 按比例

                    //收费金额=收费比例*（计费金额-金额区间金额下限）/100
                    bigCalcamtmp = bigChrgrt.multiply(bigTranam.subtract(tblCgcdmx.getLimiam())).divide(new BigDecimal("100"));

                    if (sTrackCgrule.length() > 0)
                        sTrackCgrule += ",";
                    sTrackCgrule = "按比例(" + bigChrgrt + "%)";

                    /**
                     * 最低金额控制
                     */
                    if (CommUtil.compare(bigLwxtam, BigDecimal.ZERO) > 0) {
                        if (CommUtil.compare(bigLwxtam, bigCalcamtmp) > 0) {
                            bigCalcamtmp = bigLwxtam;
                        }

                        if (sTrackCgrule.length() > 0)
                            sTrackCgrule += ",";
                        sTrackCgrule += "控制最低金额(" + bigLwxtam + ")";
                    }

                    /**
                     * 最高金额控制
                     */
                    if (CommUtil.compare(bigHistam, BigDecimal.ZERO) > 0) {
                        if (CommUtil.compare(bigHistam, bigCalcamtmp) < 0) {
                            bigCalcamtmp = bigHistam;
                        }

                        if (sTrackCgrule.length() > 0)
                            sTrackCgrule += ",";
                        sTrackCgrule += "控制最高金额(" + bigHistam + ")";
                    }

                    //优惠后收费金额=标准收费金额*(1-优惠比例)
                    bigReceamtmp = bigCalcamtmp.multiply(BigDecimal.ONE.subtract(bigChprrt));

                    bigTrackTranam = bigTranam; // 计费金额

                    bigTranam = tblCgcdmx.getLimiam(); //金额区间金额下限作为新的计费金额
                    break;

                case S: // 按单价（与数量有关）

                    if (lTranct < 1) {
                        throw FeError.Chrg.E1022();
                    }

                    //标准收费金额=单价*（笔数-区间下限）
                    bigCalcamtmp = bigSngpic.multiply(bigTranctBD.subtract(tblCgcdmx.getLimiam()));

                    if (sTrackCgrule.length() > 0)
                        sTrackCgrule += ",";
                    sTrackCgrule = "按单价(" + bigSngpic + ")";

                    /**
                     * 最低金额控制
                     */
                    if (CommUtil.compare(bigLwxtam, BigDecimal.ZERO) > 0) {
                        if (CommUtil.compare(bigLwxtam, bigCalcamtmp) > 0) {
                            bigCalcamtmp = bigLwxtam;
                        }

                        if (sTrackCgrule.length() > 0)
                            sTrackCgrule += ",";
                        sTrackCgrule += "控制最低金额(" + bigLwxtam + ")";
                    }

                    /**
                     * 最高金额控制
                     */
                    if (CommUtil.compare(bigHistam, BigDecimal.ZERO) > 0) {
                        if (CommUtil.compare(bigHistam, bigCalcamtmp) < 0) {
                            bigCalcamtmp = bigHistam;
                        }

                        if (sTrackCgrule.length() > 0)
                            sTrackCgrule += ",";
                        sTrackCgrule += "控制最高金额(" + bigHistam + ")";
                    }

                    //优惠后收费金额=标准收费金额*（1-优惠比例)
                    bigReceamtmp = bigCalcamtmp.multiply(BigDecimal.ONE.subtract(bigChprrt));

                    bigTrackTranam = bigTranctBD; // 计费笔数

                    bigTranctBD = tblCgcdmx.getLimiam(); //区间下限作为新的计费笔数
                    break;

                case N: //按单价（与数量无关）

                    //标准收费金额=单价
                    bigCalcamtmp = bigSngpic;

                    if (sTrackCgrule.length() > 0)
                        sTrackCgrule += ",";
                    sTrackCgrule = "按单价(与数量无关)(" + bigSngpic + ")";

                    /**
                     * 最低金额控制
                     */
                    if (CommUtil.compare(bigLwxtam, BigDecimal.ZERO) > 0) {
                        if (CommUtil.compare(bigLwxtam, bigCalcamtmp) > 0) {
                            bigCalcamtmp = bigLwxtam;
                        }

                        if (sTrackCgrule.length() > 0)
                            sTrackCgrule += ",";
                        sTrackCgrule += "控制最低金额(" + bigLwxtam + ")";
                    }

                    /**
                     * 最高金额控制
                     */
                    if (CommUtil.compare(bigHistam, BigDecimal.ZERO) > 0) {
                        if (CommUtil.compare(bigHistam, bigCalcamtmp) < 0) {
                            bigCalcamtmp = bigHistam;
                        }

                        if (sTrackCgrule.length() > 0)
                            sTrackCgrule += ",";
                        sTrackCgrule += "控制最高金额(" + bigHistam + ")";
                    }

                    //优惠后收费金额=标准收费金额*（1-优惠比例）
                    bigReceamtmp = bigCalcamtmp.multiply(BigDecimal.ONE.subtract(bigChprrt));

                    bigTrackTranam = BigDecimal.ONE; // 计费金额/档次
                    bigTranctBD = tblCgcdmx.getLimiam();
                    break;

                default:

                    bizlog.debug("收费代码[%s]公式[%s]下计费类型[%s]尚未支持", sChrgcd, sChrgfm, eClcgtp);
                    throw FeError.Chrg.E0010(sChrgcd, sChrgfm, eClcgtp);
                }

                bigCalcamtmp1 = bigCalcamtmp1.add(bigCalcamtmp); //累加标准收费金额 
                bigReceamtmp1 = bigReceamtmp1.add(bigReceamtmp); //累加优惠后收费金额
                tblCalResult.setCalamt(bigCalcamtmp1); //最终标准收费金额
                tblCalResult.setCgscam(bigReceamtmp1); //最终优惠后收费金额

                /**
                 * 记录计费轨迹
                 */
                {
                    /**
                     * 收费类别 <br/>
                     * 收费项目 <br/>
                     * 优惠比例 <br/>
                     * 计费金额/档次 <br/>
                     * 规则 <br/>
                     * 标准收费 <br/>
                     * 优惠/控制后收费 <br/>
                     */
                    prcChargeTrack(null, sTrackCgitem, bigTrackCgprrt, bigTrackTranam, sTrackCgrule, bigCalcamtmp, bigReceamtmp);
                }
            }
            break;

        default:
            bizlog.debug("进入：尚未支持此分层方式");
            bizlog.debug("收费代码[%s]下尚未支持的分层方式[%s]", sChrgcd, eLayerm);
            throw FeError.Chrg.E0011(sChrgcd, eLayerm);

        }

        tblCalResult.setFavoir(bigChprrt); //优惠比例
        bizlog.parm("tblCalResult[%s]", tblCalResult);
        bizlog.method("calOneChrgForm end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        return tblCalResult;

    }
    
    /**
     * @Author wuxq
     *         <p>
     *         <li>功能说明：获取计费公式信息</li>
     *         </p>
     * @param sChrgcd
     *        收费公式代码
     * @param sCityno
     *        分行号
     * @param sBrchno
     *        机构号
     * @param eChrgcy
     *        金额币种
     * @param bigTrnAmt
     *        金额下限
     * @param eLayerm   
     *        分层方式
     * @return
     */
    public List<KcpChrgFmdt> getChrgFormDetlLst(String sChrgfm, String sCityno, String sBrchno, String eChrgcy, BigDecimal bigTrnAmt, E_LYSPTP eLayerm) {

        bizlog.method("getChrgFormDetlLst begin >>>>>>>>>>>>>>>>>>>>>");
        bizlog.parm("sChrgfm[%s]sCityno[%s]sBrchno[%s]eChrgcy[%s]bigTrnAmt[%s]", sChrgfm, sCityno, sBrchno, eChrgcy, bigTrnAmt);

        if (CommUtil.isNull(sChrgfm)) {
            throw FeError.Chrg.BNASF142();
        }

        List<KcpChrgFmdt> lstOut = new ArrayList<KcpChrgFmdt>();
        try {
        	String sCorpno="";
        	if(sBrchno.equals(BusiTools.getBusiRunEnvs().getCentbr())){
        		//sCorpno = CommTools.getBaseRunEnvs().getCenter_org_id();
        		sCorpno = CommTools.getBaseRunEnvs().getCenter_org_id();
        	}else{
        		sCorpno = BusiTools.getFrdm(KcpChrgFmdt.class);
        	}
            
            //add 2016/12/21 songlw 收费分层为否 ，记录数多于一条，则取最低档记录
            if(E_LYSPTP.NO == eLayerm){
            	lstOut = FeFormulaDao.selOne_kcp_chrg_fmdt(sChrgfm, sBrchno, eChrgcy, bigTrnAmt, false);
            }else{
                lstOut = FeFormulaDao.selChargeFormDetlLstInf(sChrgfm, sBrchno, eChrgcy, bigTrnAmt, false);
            }
//            if(lstOut.size() <= 0){
//            	lstOut = FeFormulaDao.selChargeFormDetlLstInf("999", sChrgfm, "999000", eChrgcy, bigTrnAmt, true);
//            }
        } catch (Exception e) {

            bizlog.debug("查找[%s][%s][%s][%s]下收费公式记录失败，Exception ", sChrgfm, sBrchno, eChrgcy, bigTrnAmt);
            bizlog.debug("未找到对应的计费公式代码记录");

        }

        bizlog.parm("lstOut[%s]", lstOut);
        bizlog.method("getChrgFormDetlLst end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        return lstOut;
    }

}

