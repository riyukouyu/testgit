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
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.cg.namedsql.charg.PBChargeCodeDao;
import cn.sunline.ltts.busi.cg.namedsql.charg.PBChargeRegisterDao;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgDetl;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgDetlDao;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgRgst;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgRgstDao;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbDvidRgst;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbDvidRgstDao;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcpChrgAcrl;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgChargeFee_IN;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgChargeFee_OUT;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgChargefeeKhz_IN;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgDanbJzFee_IN;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgDanbJzFee_OUT;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgFEEINFO;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgGetAccountInf_IN;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgGetAccountInf_OUT;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.QryDpAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInAcctTmp;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.ltts.busi.sys.errors.DpError;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.errors.PbError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_ACCTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSTRFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_GTAMFG;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ACTTYP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BLLWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CGPYRV;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_DVIDST;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_OPRFLG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_RUISMA;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_WAISMA;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

public class ChargeProc {

    private static final BizLog bizlog = BizLogUtil.getBizLog(PbCalCharg.class);

    /**
     * @Author Wuxq
     *         <p>
     *         <li>功能说明：收取手续费时入客户账</li>
     *         </p>
     * @param cplKhz
     */
    public static void calChargefeeKhz(CgChargefeeKhz_IN cplKhz) {

        bizlog.method("calChargefeeKhz begin >>>>>>>>>>>>>>>>>>>>>");
        bizlog.parm("cplKhz[%s]", cplKhz);

        String sZHYODM = cplKhz.getSmrycd();

        if (CommUtil.isNull(sZHYODM)) {
            if (cplKhz.getCgpyrv() == E_CGPYRV.RECIVE)
                sZHYODM = "PB0001";
            else if (cplKhz.getCgpyrv() == E_CGPYRV.PAY)
                sZHYODM = "PB0002";
        }

        //根据摘要码获取摘要描述
        //        if (CommUtil.isNull(sZHYOMS)) {
        //            sZHYOMS = CsZhaiYManager.getZhyoms(sZHYODM);
        //        }

        //内部账记帐
        IoInAccount InAccounting = SysUtil.getInstance(IoInAccount.class);
        IaAcdrInfo InAccount_in = SysUtil.getInstance(IaAcdrInfo.class);
        //客户帐记账
        DpAcctSvcType DPAccountingAPI = SysUtil.getInstance(DpAcctSvcType.class);

        if (cplKhz.getCstrfg() == E_CSTRFG.CASH) {
            // bizlog.debug("客户现金账"); TODO 暂不考虑
            throw FeError.Chrg.BNASF324();
        }
        //转帐
        else if (cplKhz.getCstrfg() == E_CSTRFG.TRNSFER) {

            if (cplKhz.getCgpyrv() == E_CGPYRV.RECIVE) { //收费

                if (E_ACTTYP.NBZ == cplKhz.getActtyp()) { //内部账

                    //收费，记借方
                    InAccount_in.setAcctno(cplKhz.getSysacn()); //内部户账号
                    //                    InAccount_in.setSubsac(cplKhz.getActseq()); //子户号
                    InAccount_in.setCrcycd(cplKhz.getTrancy()); //币种
                    InAccount_in.setSmrycd(BusinessConstants.SUMMARY_SF); //摘要码 XZ-收费
                    InAccount_in.setDscrtx(cplKhz.getRemark()); //描述
                    InAccount_in.setTranam(cplKhz.getAcclam()); //交易金额
                    InAccount_in.setPayadetail(cplKhz.getPayaDetail());
                    InAccount_in.setPayddetail(cplKhz.getPaydDetail());
                    InAccount_in.setStrktg(cplKhz.getStrktg());
                    InAccounting.ioInAcdr(InAccount_in);

                }
                else if (E_ACTTYP.DZZH == cplKhz.getActtyp()) {
                	
                	SysUtil.getInstance(IoAccountSvcType.class).checkStatusBeforeAccount(cplKhz.getCustac(), E_AMNTCD.DR,cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES, cplKhz.getClactp());
                	 
                    //电子账号
                    DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
                    cplDrawAcctIn.setAcctno(cplKhz.getSysacn()); //负债账号
                    cplDrawAcctIn.setCustac(cplKhz.getCustac()); //电子账号  
                    cplDrawAcctIn.setAcseno(cplKhz.getActseq()); //子账户序号
                    cplDrawAcctIn.setCardno(""); //卡号   
                    cplDrawAcctIn.setCrcycd(cplKhz.getTrancy()); //币种
                    cplDrawAcctIn.setTranam(cplKhz.getAcclam()); //交易金额 
                    cplDrawAcctIn.setLinkno(""); //逐笔标志 
                    cplDrawAcctIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_SF)); //摘要描述
                    cplDrawAcctIn.setSmrycd(BusinessConstants.SUMMARY_SF); //摘要码 XZ-收费
                    cplDrawAcctIn.setRemark(cplKhz.getRemark());//备注
//                    cplDrawAcctIn.setToacct(cplKhz.getSmrycd()); //对方账号
                    cplDrawAcctIn.setStrktg(cplKhz.getStrktg());
                    DPAccountingAPI.addDrawAcctDp(cplDrawAcctIn); //客户帐支取记帐处理
                }

            }
            else if (cplKhz.getCgpyrv() == E_CGPYRV.PAY) { //付费

                if (E_ACTTYP.NBZ == cplKhz.getActtyp()) { //内部账

                    //付费，记贷方
                    InAccount_in.setAcctno(cplKhz.getSysacn()); //内部户账号
                    //                  InAccount_in.setSubsac(cplKhz.getActseq()); //子户号
                    InAccount_in.setCrcycd(cplKhz.getTrancy()); //币种
                    InAccount_in.setSmrycd(BusinessConstants.SUMMARY_FF); //摘要码 XZ-收费
                    InAccount_in.setDscrtx(cplKhz.getRemark());
//                    InAccount_in.setToacct(cplKhz.getSmrycd()); //对方账号
//                    InAccount_in.setToacna(cplKhz.getSmryds()); //对方户名
                    InAccount_in.setTranam(cplKhz.getAcclam()); //交易金额
                    InAccount_in.setPayadetail(cplKhz.getPayaDetail());
                    InAccount_in.setPayddetail(cplKhz.getPaydDetail()); 
                    InAccount_in.setStrktg(cplKhz.getStrktg());                    
                    InAccounting.ioInAccr(InAccount_in);

                }
                else if (E_ACTTYP.DZZH == cplKhz.getActtyp()) {
                    //电子账号
                	SysUtil.getInstance(IoAccountSvcType.class).checkStatusBeforeAccount(cplKhz.getCustac(), E_AMNTCD.CR,cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES, cplKhz.getClactp());
                	   IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb1(cplKhz.getCustac(), E_DPACST.NORMAL, false);

                       if (CommUtil.isNull(tblKnaAcdc) || CommUtil.isNull(tblKnaAcdc.getCardno())) {
                           throw DpError.DeptAcct.BNAS1695();
                       }
                       E_CUACST status = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(cplKhz.getCustac());//查询电子账户状态信息
                       if (status == E_CUACST.OUTAGE) {
                           throw DpError.DeptAcct.BNAS1701(tblKnaAcdc.getCardno(),status.getLongName());
                       }
                    SaveDpAcctIn cplSaveAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
                    cplSaveAcctIn.setAcctno(cplKhz.getSysacn()); //负债账号
                    cplSaveAcctIn.setCustac(cplKhz.getCustac()); //电子账号  
                    cplSaveAcctIn.setAcseno(cplKhz.getActseq()); //子账户序号
                    cplSaveAcctIn.setCardno(""); //卡号
                    cplSaveAcctIn.setCrcycd(cplKhz.getTrancy()); //币种
                    cplSaveAcctIn.setTranam(cplKhz.getAcclam()); //交易金额 
                    cplSaveAcctIn.setLinkno(""); //逐笔标志 
                    cplSaveAcctIn.setSmrycd(BusinessConstants.SUMMARY_FF);
                    cplSaveAcctIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_FF));
                    cplSaveAcctIn.setRemark(cplKhz.getRemark());
//                    cplSaveAcctIn.setOpacna(cplKhz.getSmryds()); //对方户名
//                    cplSaveAcctIn.setToacct(cplKhz.getSmrycd()); //对方账号    
                    cplSaveAcctIn.setStrktg(cplKhz.getStrktg());
                    DPAccountingAPI.addPostAcctDp(cplSaveAcctIn); //客户帐存入记帐处理
                }

            }

        }

        bizlog.method("calChargefeeKhz end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

    }

    /**
     * @Author user
     *         <p>
     *         <li>功能说明：单边记费用科目账</li>
     *         </p>
     * @param cplDbFeeIn
     * @return
     */
    public static CgDanbJzFee_OUT calDanbJzfee(CgDanbJzFee_IN cplDbFeeIn) {
        bizlog.method("calDanbJzfee begin >>>>>>>>>>>>>>>>>>>>>>");
        bizlog.parm("cplDbFeeIn[%s]", cplDbFeeIn);

        CgDanbJzFee_OUT cplOut = SysUtil.getInstance(CgDanbJzFee_OUT.class);

        if (CommUtil.compare(cplDbFeeIn.getTranam(), BigDecimal.ZERO) <= 0) {
            bizlog.debug("交易金额Jiaoyije=[%f]为零,不记账", cplDbFeeIn.getTranam());
            return cplOut;
        }

        BigDecimal bigJiZhJe = BusiTools.roundIncinByCurrency(cplDbFeeIn.getTrancy(), cplDbFeeIn.getTranam(),null);
        bizlog.debug("bigJiZhJe [%f]", bigJiZhJe);

        if (CommUtil.compare(bigJiZhJe, cplDbFeeIn.getTranam()) != 0) {
            /* 金额[%f]不满足币种[%s]最小记账单位要求的金额[%f] */
            String sJiaoyibz = "";
            if (CommUtil.isNotNull(cplDbFeeIn.getTrancy())) {
                sJiaoyibz = cplDbFeeIn.getTrancy();
            }
            throw PbError.PbComm.E1213(cplDbFeeIn.getTranam(), sJiaoyibz, bigJiZhJe);
        }

        //根据摘要码获取摘要描述
        //		if (CommUtil.isNull(cplDbFeeIn.getSmryds()) && CommUtil.isNotNull(cplDbFeeIn.getSmrycd())) {
        //			String sZhaiyaoms = CsZhaiYManager.getZhyoms(cplDbFeeIn.getSmrycd());
        //			cplDbFeeIn.setSmryds(sZhaiyaoms);
        //			bizlog.debug("摘要代码sZhaiyodm [%s] sZhaiyaoms = [%s]", cplDbFeeIn.getSmrycd(), sZhaiyaoms);
        //		}

        /*
         * ================================================
         * 若记账机构不为空，则收的费入该机构，否则入交易机构 =
         * =================================================
         */
        String sJzjigo = "";
        sJzjigo = cplDbFeeIn.getAcctbr();

        if (CommUtil.isNull(cplDbFeeIn.getCgpyrv())) {
            throw FeError.Chrg.BNASF066();
        }

        if (CommUtil.isNull(cplDbFeeIn.getChrgcd())) {
            throw FeError.Chrg.BNASF226();
        }

        if (CommUtil.isNull(cplDbFeeIn.getTrancy())) {
            throw FeError.Chrg.BNASF156();
        }

        if (CommUtil.isNull(cplDbFeeIn.getCstrfg())) {
            throw FeError.Chrg.BNASF273();
        }
        String sHesuywbh = cplDbFeeIn.getCgfacd();
/*        kcp_chrg tblFeezl = SysUtil.getInstance(kcp_chrg.class);
        if (CommUtil.isNull(cplDbFeeIn.getCgfacd())) {

            tblFeezl = Kcp_chrgDao.selectOne_odb1(cplDbFeeIn.getChrgcd(), cplDbFeeIn.getTrancy(), false);

            bizlog.debug("费率种类tblFeezl[%s]", tblFeezl);
            if (CommUtil.isNull(tblFeezl)) {
                throw PbError.PbComm.E2015("没有对应的收费代码信息");
            }
            sHesuywbh = tblFeezl.getCgfacd();
            if (CommUtil.compare(tblFeezl.getEfctdt(), CommTools.getBaseRunEnvs().getTrxn_date()) > 0) {
                throw PbError.PbComm.E2015("该收费代码尚未生效");
            }

            if (CommUtil.isNull(sJzjigo)) {
                sJzjigo = tblFeezl.getBrchno();
            }

        }*/

        if (CommUtil.isNull(sJzjigo)) {
            sJzjigo = CommTools.getBaseRunEnvs().getTrxn_branch();
        }
        bizlog.debug("sJzjigo[%s]", sJzjigo);

        /*机构合法性检查  TODO */

        bizlog.debug("核算业务编码sHesuywbh[%s]", sHesuywbh);

        // 登记会计流水开始
        IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
        cplIoAccounttingIntf.setCuacno(cplDbFeeIn.getChrgcd()); //记账账号-登记收费代码
        cplIoAccounttingIntf.setAcseno(cplDbFeeIn.getChrgcd()); //子账户序号-登记收费代码
        cplIoAccounttingIntf.setAcctno(cplDbFeeIn.getChrgcd()); //负债账号-登记收费代码
        cplIoAccounttingIntf.setProdcd(cplDbFeeIn.getProdcd()); //产品编号
        cplIoAccounttingIntf.setDtitcd(sHesuywbh); //核算口径-核算业务编号
        cplIoAccounttingIntf.setCrcycd(cplDbFeeIn.getTrancy()); //币种                 
        cplIoAccounttingIntf.setTranam(cplDbFeeIn.getTranam()); //交易金额 
        cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
        cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //主交易流水
        cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期 
        cplIoAccounttingIntf.setAcctbr(sJzjigo); //账务机构

        if (cplDbFeeIn.getCgpyrv() == E_CGPYRV.PAY) { //付费，记借方
            cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR); //借贷标志-借方
        }
        else if (cplDbFeeIn.getCgpyrv() == E_CGPYRV.RECIVE) { //收费，记贷方
            cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR); //借贷标志-贷方   
        }

        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.FE); //会计主体类型-手续费
        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型-账务流水
        cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE); //余额属性-本金科目
        cplIoAccounttingIntf.setTranms(cplDbFeeIn.getTrinfo());//交易信息
        //登记会计流水
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);

        cplOut.setChrgcd(cplDbFeeIn.getChrgcd());

        bizlog.parm("cplOut[%s]", cplOut);
        bizlog.method("calDanbJzfee end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        return cplOut;
    }


    /**
     * 根据账号查询账户信息
     * 
     * @Author Wuxq
     *         <p>
     *         <li>功能说明：根据账号查询账户信息</li>
     *         </p>
     * @param cplIn_GetAccountInf
     * @param cplOut_GetAccountInf
     * @return
     */
    public static CgGetAccountInf_OUT getAccountInf(CgGetAccountInf_IN cplIn_GetAccountInf) {

        bizlog.method("getAccountInf begin >>>>>>>>>>>>>>>>>>>>>>");
        bizlog.parm("cplIn_GetAccountInf[%s]", cplIn_GetAccountInf);

        CgGetAccountInf_OUT cplOut_GetAccountInf = SysUtil.getInstance(CgGetAccountInf_OUT.class); //输出

        DpProdSvcType QryAcct = SysUtil.getInstance(DpProdSvcType.class); ////查询负债账户
        QryDpAcctOut cplQryAcctOut = SysUtil.getInstance(QryDpAcctOut.class); //查询负债账户输出

        BigDecimal bigAdrbal = BigDecimal.ZERO; //可用余额
        
        
        //检查账户类型
        E_ACCTTP accttp=null;
        /*if(cplIn_GetAccountInf.getAcctno().length() == 23){
			IoBusinoInfo ioBusinoInfo = SysUtil.getInstance(IoInQuery.class).selBusiIndoByBusino(cplIn_GetAccountInf.getAcctno().substring(8, 18));
			// 长度23位，且第九位至第十九位为内部户产品表中某产品代码，判断为内部账号
			if(CommUtil.isNotNull(ioBusinoInfo)&&CommUtil.isNotNull(ioBusinoInfo.getBusino())){
				accttp=E_ACCTTP.IN;
			}
        }*/
        if(CommUtil.compare(ApAcctRoutTools.getRouteType(cplIn_GetAccountInf.getAcctno()),E_ACCTROUTTYPE.INSIDE)==0){
				accttp=E_ACCTTP.IN;
			}
        //内部户
        if (accttp==E_ACCTTP.IN) {
            //内部账号查询
            IoInQuery InAcctQryAPI = SysUtil.getInstance(IoInQuery.class);
            IoInacInfo acct = InAcctQryAPI.InacInfoQuery(cplIn_GetAccountInf.getAcctno());
            if (CommUtil.isNull(acct)) {
                throw FeError.Chrg.BNASF389(cplIn_GetAccountInf.getAcctno());
            }

            cplOut_GetAccountInf.setActtyp(E_ACTTYP.NBZ); //账号类型-内部账
            cplOut_GetAccountInf.setCustac(acct.getAcctno()); //总帐帐号
            cplOut_GetAccountInf.setAcctno(acct.getAcctno()); //总帐帐号
            cplOut_GetAccountInf.setAcctna(acct.getAcctna()); //总帐帐号名称
//            cplOut_GetAccountInf.setActseq(acct.getSubsac()); //子户号
            cplOut_GetAccountInf.setCustno(cplIn_GetAccountInf.getCustno()); //客户号
            cplOut_GetAccountInf.setCrcycd(acct.getCrcycd()); //币种
            cplOut_GetAccountInf.setCsextg(cplIn_GetAccountInf.getCsextg()); //账户钞汇标志
            cplOut_GetAccountInf.setBrchno(acct.getBrchno()); //机构
            cplOut_GetAccountInf.setOpendt(acct.getOpendt()); //开户日期
            cplOut_GetAccountInf.setOpensq(acct.getOptrsq()); //开户流水
            cplOut_GetAccountInf.setClosdt(acct.getClosdt()); //销户日期
            cplOut_GetAccountInf.setClossq(acct.getCltrsq()); //销户流水
            cplOut_GetAccountInf.setIoflag(acct.getIoflag()); //表内外标志
            cplOut_GetAccountInf.setBlncdn(acct.getBlncdn()); //余额方向CDR
            cplOut_GetAccountInf.setDrctbl(acct.getDrctbl()); //联机借方余额
            cplOut_GetAccountInf.setCrctbl(acct.getCrctbl()); //联机贷方余额
//            cplOut_GetAccountInf.setItemcd(acct.getItemcd()); //科目
//            cplOut_GetAccountInf.setItmcdn(acct.getItmcdn()); //科目方向
            cplOut_GetAccountInf.setPmodtg(acct.getPmodtg()); //透支许可
            cplOut_GetAccountInf.setKpacfg(acct.getKpacfg()); //记账控制
            //cplOut_GetAccountInf.setHgdram(acct.getHgdram()); //挂账金额（借方)
            cplOut_GetAccountInf.setDredbl(acct.getDredbl()); //当日借方余额
            cplOut_GetAccountInf.setDredbl(acct.getDredbl()); //当日贷方余额
            //cplOut_GetAccountInf.setBzprtp(acct.getBzprtp()); //属性类型
            cplOut_GetAccountInf.setIspaya(acct.getIspaya()); //待销账方向
            cplOut_GetAccountInf.setRlbltg(acct.getRlbltg()); //余额更新方式
            cplOut_GetAccountInf.setTotltg(acct.getTotltg()); //汇总标志
            cplOut_GetAccountInf.setKpacbr(acct.getKpacbr()); //核算机构
            // cplOut_GetAccountInf.setAcctst(acct.getAcctst()); //账户状态
            cplOut_GetAccountInf.setProdcd(acct.getBusino());//内部户产品

            //查询内部账户余额
            IoInAcctTmp cplBal = InAcctQryAPI.InacBalQuery(cplIn_GetAccountInf.getAcctno());
            if (CommUtil.isNull(cplBal)) {
                throw FeError.Chrg.BNASF389(cplIn_GetAccountInf.getAcctno());
            }
            bigAdrbal = cplBal.getOnlnbl(); //联机余额
            cplOut_GetAccountInf.setAdrbal(bigAdrbal.add(cplBal.getOvmony())); //可用余额=联机余额+透支限额 

        }
        else {

            //电子账号或负债账号
      //      if (cplIn_GetAccountInf.getAcctno().length() == 13 || CommUtil.isNotNull(cplIn_GetAccountInf.getActseq())) {
            if (CommUtil.compare(ApAcctRoutTools.getRouteType(cplIn_GetAccountInf.getAcctno()),E_ACCTROUTTYPE.CUSTAC)==0|| 
            		CommUtil.isNotNull(cplIn_GetAccountInf.getActseq())) {
                //电子账号
                if (CommUtil.isNull(cplIn_GetAccountInf.getActseq())) {
                    //根据电子账号 +货币代号  查询
                    cplQryAcctOut = QryAcct.qryDpAcct(cplIn_GetAccountInf.getAcctno(), cplIn_GetAccountInf.getCrcycd());


                }
                else {
                    //根据电子账号 +子户号 查询
                    cplQryAcctOut = QryAcct.QryDpByAcctSub(cplIn_GetAccountInf.getAcctno(), cplIn_GetAccountInf.getActseq());


                }
            }
            else {
                //电子账户下的负债账号  
                cplQryAcctOut = QryAcct.QryDpByAcctno(cplIn_GetAccountInf.getAcctno());

            }

            bizlog.debug("账户信息[%s]", cplIn_GetAccountInf.getAcctno(), cplQryAcctOut);
            cplOut_GetAccountInf.setActtyp(E_ACTTYP.DZZH); //账号类型-电子账号
            cplOut_GetAccountInf.setCustac(cplQryAcctOut.getCustac()); //客户账号
            cplOut_GetAccountInf.setAcctno(cplQryAcctOut.getAcctno()); //负债帐号
            cplOut_GetAccountInf.setAcctna(cplQryAcctOut.getAcctna()); //帐号名称
            cplOut_GetAccountInf.setActseq(cplQryAcctOut.getSubsac()); //子户号
            cplOut_GetAccountInf.setCustno(cplQryAcctOut.getCustno()); //客户号
            cplOut_GetAccountInf.setCrcycd(cplQryAcctOut.getCrcycd()); //币种
            cplOut_GetAccountInf.setCsextg(cplQryAcctOut.getCsextg()); //账户钞汇标志
            cplOut_GetAccountInf.setDepttm(cplQryAcctOut.getDepttm()); //存期 
            cplOut_GetAccountInf.setMatudt(cplQryAcctOut.getMatudt()); //到期日期 
            cplOut_GetAccountInf.setBgindt(cplQryAcctOut.getBgindt()); //起息日期
            cplOut_GetAccountInf.setBrchno(cplQryAcctOut.getBrchno()); //账户所属机构
            cplOut_GetAccountInf.setOpendt(cplQryAcctOut.getOpendt()); //开户日期 
            cplOut_GetAccountInf.setOpensq(cplQryAcctOut.getOpensq()); //开户流水
            cplOut_GetAccountInf.setOpmony(cplQryAcctOut.getOpmony()); //开户金额
            cplOut_GetAccountInf.setClosdt(cplQryAcctOut.getClosdt()); //销户日期 
            cplOut_GetAccountInf.setClossq(cplQryAcctOut.getClossq()); //销户流水
            cplOut_GetAccountInf.setOnlnbl(cplQryAcctOut.getOnlnbl()); //当前账户余额
            cplOut_GetAccountInf.setLastbl(cplQryAcctOut.getLastbl()); //上日账户余额
            cplOut_GetAccountInf.setLstrdt(cplQryAcctOut.getLstrdt()); //上次交易日期
            cplOut_GetAccountInf.setLstrsq(cplQryAcctOut.getLstrsq()); //上次交易流水 
            cplOut_GetAccountInf.setProdcd(cplQryAcctOut.getAcctcd()); //产品编号
            cplOut_GetAccountInf.setPddpfg(cplQryAcctOut.getPddpfg()); //产品定活标志
            cplOut_GetAccountInf.setHdmxmy(cplQryAcctOut.getHdmxmy()); //最大留存余额
            cplOut_GetAccountInf.setHdmimy(cplQryAcctOut.getHdmimy()); //最小留存余额 
            cplOut_GetAccountInf.setTrsvtp(cplQryAcctOut.getTrsvtp()); //转存方式
            cplOut_GetAccountInf.setBkmony(cplQryAcctOut.getBkmony()); //备用金额
            cplOut_GetAccountInf.setDebttp(cplQryAcctOut.getDebttp()); //存款种类
            cplOut_GetAccountInf.setSleptg(cplQryAcctOut.getSleptg()); //形态转移标志
            cplOut_GetAccountInf.setSpectp(cplQryAcctOut.getSpectp()); //负债账户性质
            cplOut_GetAccountInf.setAccttp(cplQryAcctOut.getAccttp()); //结算账户标志 
            cplOut_GetAccountInf.setAcctcd(cplQryAcctOut.getAcctcd()); //核算代码
            cplOut_GetAccountInf.setAcctst(cplQryAcctOut.getAcctst()); //账户状态

            //查询账户余额
            //  DpAcctSvcType QryAccBal = SysUtil.getInstance(DpAcctSvcType.class);
            
//            bigAdrbal = QryAccBal.getProductBal(cplQryAcctOut.getCustac(), cplQryAcctOut.getCrcycd());
            
            //mod 根据账户类型查询账户余额 slw 20161009 begin
            
    		// 查询当前余额
    		IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(cplQryAcctOut.getCustac());
    		
    		bigAdrbal = cplKnaAcct.getOnlnbl(); // 当前账户余额
    		
    		//根据账户类型查询账户余额 end
    		
    		
    		BigDecimal acctbl = BigDecimal.ZERO;// 可支取余额
    		//判断是否为存款证明收费(取活期账户余额)
    		if(CommUtil.equals(BusiTools.getBusiRunEnvs().getLttscd(), "opdppr")
    				|| CommUtil.equals(BusiTools.getBusiRunEnvs().getLttscd(), "redppr")){
    			
    			acctbl = SysUtil.getInstance(DpAcctSvcType.class).getAcctaAvaBal(cplQryAcctOut.getCustac(),
    					cplKnaAcct.getAcctno(), cplQryAcctOut.getCrcycd(), 
    					cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.NO, 
    					cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES);
    			// 获取转存签约明细信息
//    			IoCaKnaSignDetl cplkna_sign_detl = SysUtil.getInstance(
//    					IoCaSevQryTableInfo.class).kna_sign_detl_selectFirst_odb2(
//    					cplKnaAcct.getAcctno(), E_SIGNST.QY, false);
//
//    			// 存在转存签约明细信息则取资金池可用余额
//    			if (CommUtil.isNotNull(cplkna_sign_detl)) {
//    				acctbl = SysUtil.getInstance(DpAcctSvcType.class)
//    						.getProductBal(cplQryAcctOut.getCustac(), cplQryAcctOut.getCrcycd(), false);
//    				acctbl = DpAcctProc.getProductBal(cplQryAcctOut.getCustac(), cplQryAcctOut.getCrcycd(), E_YES___.NO, false);
//    			} else {
//    				// 其他取账户余额,正常的支取交易排除冻结金额
//    				acctbl = SysUtil.getInstance(DpAcctSvcType.class)
//    						.getOnlnblForFrozbl(cplKnaAcct.getAcctno(), false);
//    			}
    		
    		//其他收费取可用余额
    		}else{
    			
    			// addby xiongzhao 20161226
    			acctbl = SysUtil.getInstance(DpAcctSvcType.class).getAcctaAvaBal(cplQryAcctOut.getCustac(),
					cplKnaAcct.getAcctno(), cplQryAcctOut.getCrcycd(), 
					cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES, 
					cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES);}
			
            
            bizlog.debug("账户可用金额[%f]", acctbl);
            cplOut_GetAccountInf.setAdrbal(acctbl); //可用余额

        }

        bizlog.parm("cplOut_GetAccountInf[%s]", cplOut_GetAccountInf);
        bizlog.method("getAccountInf end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        return cplOut_GetAccountInf;
    }

    /**
     * @Author user
     *         <p>
     *         <li>功能说：收费登记指令流水</li>
     *         </p>
     * @param cplDbFeeIn
     * @return
     */
    public static CgDanbJzFee_OUT calChrgfeeAcsq(CgDanbJzFee_IN cplDbFeeIn) {
        bizlog.method("calDanbJzfee begin >>>>>>>>>>>>>>>>>>>>>>");
        bizlog.parm("cplDbFeeIn[%s]", cplDbFeeIn);

        CgDanbJzFee_OUT cplOut = SysUtil.getInstance(CgDanbJzFee_OUT.class);

        if (CommUtil.compare(cplDbFeeIn.getTranam(), BigDecimal.ZERO) <= 0) {
            bizlog.debug("交易金额Jiaoyije=[%f]为零,不记账", cplDbFeeIn.getTranam());
            return cplOut;
        }

        BigDecimal bigJiZhJe = BusiTools.roundIncinByCurrency(cplDbFeeIn.getTrancy(), cplDbFeeIn.getTranam(),null);
        bizlog.debug("bigJiZhJe [%f]", bigJiZhJe);

        if (CommUtil.compare(bigJiZhJe, cplDbFeeIn.getTranam()) != 0) {
            /* 金额[%f]不满足币种[%s]最小记账单位要求的金额[%f] */
            String sJiaoyibz = "";
            if (CommUtil.isNotNull(cplDbFeeIn.getTrancy())) {
                sJiaoyibz = cplDbFeeIn.getTrancy();
            }
            throw PbError.PbComm.E1213(cplDbFeeIn.getTranam(), sJiaoyibz, bigJiZhJe);
        }

        //根据摘要码获取摘要描述
        //		if (CommUtil.isNull(cplDbFeeIn.getSmryds()) && CommUtil.isNotNull(cplDbFeeIn.getSmrycd())) {
        //			String sZhaiyaoms = CsZhaiYManager.getZhyoms(cplDbFeeIn.getSmrycd());
        //			cplDbFeeIn.setSmryds(sZhaiyaoms);
        //			bizlog.debug("摘要代码sZhaiyodm [%s] sZhaiyaoms = [%s]", cplDbFeeIn.getSmrycd(), sZhaiyaoms);
        //		}

        /*
         * ================================================
         * 若记账机构不为空，则收的费入该机构，否则入交易机构 =
         * =================================================
         */
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
       // String sHesuywbh = cplDbFeeIn.getCgfacd();
        if (CommUtil.isNull(cplDbFeeIn.getTrinfo())) {
             throw FeError.Chrg.BNASF362();
        }

        if (CommUtil.isNull(sJzjigo)) {
            sJzjigo = CommTools.getBaseRunEnvs().getTrxn_branch();
        }
        bizlog.debug("sJzjigo[%s]", sJzjigo);

        /*机构合法性检查  TODO */


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
        cplIoAccounttingIntf.setCorpno(sJzjigo.substring(0, 3));
        if (cplDbFeeIn.getCgpyrv() == E_CGPYRV.PAY) { //付费，记借方
            cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR); //借贷标志-借方
        }
        else if (cplDbFeeIn.getCgpyrv() == E_CGPYRV.RECIVE) { //收费，记贷方
            cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR); //借贷标志-贷方   
        }

        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.FE); //会计主体类型-手续费
        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型-账务流水
        cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE); //余额属性-本金科目
        cplIoAccounttingIntf.setTranms(cplDbFeeIn.getTrinfo());//交易信息
        cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
        cplIoAccounttingIntf.setTranms(cplDbFeeIn.getTrinfo());//交易信息
        //登记会计流水
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);

        cplOut.setChrgcd(cplDbFeeIn.getChrgcd());

        bizlog.parm("cplOut[%s]", cplOut);
        bizlog.method("calDanbJzfee end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        return cplOut;
    }
    /**
     * @Author chenlk
     *         <p>
     *         <li>功能说明：收费-计费中心计费后进行收费记账登记</li>
     *         </p>
     * @param cplChFeeIn
     * @return CgChargeFee_OUT
     */
    public static CgChargeFee_OUT chargeFee(CgChargeFee_IN cplChFeeIn) {

        bizlog.method("calChargeFee begin >>>>>>>>>>>>>>>>>>>>>>");
        bizlog.parm("cplChFeeIn[%s]", cplChFeeIn);

        CgChargeFee_OUT cplChFeeOut = SysUtil.getInstance(CgChargeFee_OUT.class);
        String sKEHHAO_SF = "", sKEHUZH_SF = "", sSHUNXH_SF = "", sFYGZZH = "";
        String eHUOBDH_SF = null, eHUOBDH_SY = null;
        E_CSEXTG eCHUIBZ_SF = null;
        E_ACTTYP eKHZHLX_SF = null;
        String sZHANGH_SF = "", sZHUZWM_SF = "", sQFEIYY = "";
        BigDecimal bigKYNGJE = BigDecimal.ZERO, bigSFYSJE = BigDecimal.ZERO, bigSFSSJE = BigDecimal.ZERO;
        BigDecimal bigSFQFJE = BigDecimal.ZERO;
        BigDecimal bigFFYFJE = BigDecimal.ZERO;
        BigDecimal bigFFSFJE = BigDecimal.ZERO;
        BigDecimal bigFFQFJE = BigDecimal.ZERO;


        if (CommUtil.isNull(cplChFeeIn.getTrandt())) {
            throw FeError.Chrg.BNASF363();
        }

        if (CommUtil.isNull(cplChFeeIn.getTrnseq())) {
            throw FeError.Chrg.BNASF364();
        }

        if (CommUtil.isNull(cplChFeeIn.getTrancy())) {
            throw FeError.Chrg.BNASF155();
        }

        if (cplChFeeIn.getCstrfg() != E_CSTRFG.CASH && cplChFeeIn.getCstrfg() != E_CSTRFG.TRNSFER) {
            throw FeError.Chrg.BNASF365();
        }

        if (cplChFeeIn.getCstrfg() == E_CSTRFG.CASH && cplChFeeIn.getOprflg() == E_OPRFLG.BSEQNO) {
            throw FeError.Chrg.BNASF366();
        }

        String eSFBIZH = cplChFeeIn.getTrancy();

        String sJIAOYM = CommTools.getBaseRunEnvs().getTrxn_code(); //交易码
        String sJIOYRQ = DateTools2.getDateInfo().getSystdt();// 日终批量收费  缓存日期塞成了上日，导致计算下一扣费日期不正确  modify by chenlk 20170216

        bizlog.debug("客户账号Kehuzhao()[%s]", cplChFeeIn.getDecuac());
        bizlog.debug("操作标志Sfcaozbz()[%s]", cplChFeeIn.getOprflg());

        if (CommUtil.isNotNull(cplChFeeIn.getDecuac()) && cplChFeeIn.getCstrfg() == E_CSTRFG.TRNSFER) {

            if (cplChFeeIn.getOprflg() == E_OPRFLG.BSEQNO || cplChFeeIn.getOprflg() == E_OPRFLG.TOT) {
                throw FeError.Chrg.BNASF367();
            }

            //账户信息查询
            CgGetAccountInf_OUT cplOut_SF = SysUtil.getInstance(CgGetAccountInf_OUT.class); //查询账户信息输出
            CgGetAccountInf_IN cplIn = SysUtil.getInstance(CgGetAccountInf_IN.class); //查询账户信息输入

            cplIn.setAcctno(cplChFeeIn.getDecuac()); //账号
            cplIn.setActseq(cplChFeeIn.getSeqnum()); //子户号
            cplIn.setCrcycd(cplChFeeIn.getTrancy()); //币种
            cplIn.setCustno(cplChFeeIn.getCustno()); //客户号
            cplIn.setCsextg(cplChFeeIn.getCsexfg()); //钞汇标志
            
            cplOut_SF = ChargeProc.getAccountInf(cplIn); //查询账号信息
            if (CommUtil.isNull(cplOut_SF)) {
                throw FeError.Chrg.BNASF183();
            }

            sKEHHAO_SF = cplOut_SF.getCustno(); //客户号
            eKHZHLX_SF = cplOut_SF.getActtyp(); //账户类型
            sKEHUZH_SF = cplOut_SF.getCustac(); //客户账号
            eHUOBDH_SF = cplOut_SF.getCrcycd(); //币种
            sSHUNXH_SF = cplOut_SF.getActseq(); //子户号
            eCHUIBZ_SF = cplOut_SF.getCsextg(); //钞汇标志 
            sZHANGH_SF = cplOut_SF.getAcctno(); //系统账号
            sZHUZWM_SF = cplOut_SF.getAcctna(); //账户名称
            bigKYNGJE = cplOut_SF.getAdrbal();//可用余额
            
            if (CommUtil.isNotNull(cplChFeeIn.getTrancy()) && CommUtil.compare(cplChFeeIn.getTrancy(), eHUOBDH_SF) != 0) {
                throw FeError.Chrg.BNASF327();
            }

//            if (CommUtil.isNotNull(cplChFeeIn.getCsexfg()) && cplChFeeIn.getCsexfg() != eCHUIBZ_SF) {
//                throw PbError.Charg.E0003("扣费", eCHUIBZ_SF, cplChFeeIn.getCsexfg());
//            }

            if (CommUtil.isNotNull(cplChFeeIn.getSeqnum()) && CommUtil.compare(cplChFeeIn.getSeqnum(), sSHUNXH_SF) != 0) {
                throw FeError.Chrg.BNASF335();
            }

            if (CommUtil.isNotNull(cplChFeeIn.getCustno()) && CommUtil.isNotNull(sKEHHAO_SF) && CommUtil.compare(cplChFeeIn.getCustno(), sKEHHAO_SF) != 0) {
                throw FeError.Chrg.BNASF332();
            }

            if (cplOut_SF.getActtyp() == E_ACTTYP.DZZH && cplChFeeIn.getIfflag() == E_YES___.YES) {
                if (cplOut_SF.getPddpfg() == E_FCFLAG.FIX) {
                    throw PbError.Charg.E0006(sZHANGH_SF);// 扣费账号为定期账号,不允许直接从该账号收费
                }
            }

        }
        else {
            if (cplChFeeIn.getCstrfg() != E_CSTRFG.CASH && cplChFeeIn.getOprflg() != E_OPRFLG.BSEQNO && cplChFeeIn.getOprflg() != E_OPRFLG.TOT
                    && cplChFeeIn.getIfflag() != E_YES___.NO) {
                throw FeError.Chrg.BNASF368();
            }
        }

        if (CommUtil.isNotNull(cplChFeeIn.getCustno())) {
            sKEHHAO_SF = cplChFeeIn.getCustno(); //客户号
        }

        // 根据客户号获取客户中文名
        //		if (CommUtil.isNotNull(sKEHHAO_SF) && CommUtil.isNull(sZHUZWM_SF)) {
        //			IoCiKhcxSvc CiQry = SysUtil.getInstance(IoCiKhcxSvc.class);
        //			IoCiKhxx_out CifInfo = CiQry.qryKhxxByKhh(sKEHHAO_SF);
        //			sZHUZWM_SF = CifInfo.getKehuzwmc();
        //		}

        List<KcbChrgRgst> lstJfdjb = new ArrayList<KcbChrgRgst>();
        String sCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();

        if (cplChFeeIn.getOprflg() == E_OPRFLG.SEQNO)/* 指定流水号+序号收费 */
        {

            if (CommUtil.isNull(cplChFeeIn.getEvrgsq())) { //事件登记序号
                throw PbError.PbComm.E0136();
            }

            lstJfdjb = PBChargeRegisterDao.selChargeRegistrByTrsqRgsq(cplChFeeIn.getTrandt(), cplChFeeIn.getTrnseq(), cplChFeeIn.getEvrgsq(), false);

            bizlog.debug("指定流水号+序号收费lstJfdjb[%s]", lstJfdjb);
        }
        else if (cplChFeeIn.getOprflg() == E_OPRFLG.SEQ)/* 指定流水号 */
        {
            lstJfdjb = PBChargeRegisterDao.selChargeRegistrByTrsqRcfg(sCorpno, cplChFeeIn.getTrandt(), cplChFeeIn.getTrnseq(), false);

            bizlog.debug("指定流水号lstJfdjb[%s]", lstJfdjb);
        }
        else if (cplChFeeIn.getOprflg() == E_OPRFLG.BSEQNO) /* 批量交易流水号+ 起始序号收 */
        {

            lstJfdjb = PBChargeRegisterDao.selChargeRegistrByAppointTrsq(sCorpno, cplChFeeIn.getTrandt(), cplChFeeIn.getTrnseq(), cplChFeeIn.getEvrgsq(), false);
            bizlog.debug("批量交易流水号+起始序号收lstJfdjb[%s]", lstJfdjb);
        }
        else if (cplChFeeIn.getOprflg() == E_OPRFLG.TOT)/* 手续费汇总扣收 */
        {
            if (CommUtil.isNull(cplChFeeIn.getEvrgsq())) {
                throw FeError.Chrg.BNASF369();
            }

            lstJfdjb = PBChargeRegisterDao.selChargeRegistrByTrsqRgsq( cplChFeeIn.getTrandt(), cplChFeeIn.getTrnseq(), cplChFeeIn.getEvrgsq(), false);

            bizlog.debug("手续费汇总扣收lstJfdjb[%s]", lstJfdjb);
        }
        else {
            throw FeError.Chrg.BNASF370();
        }

        long lNum = 0;
        if (CommUtil.isNotNull(lstJfdjb)) {
            lNum = lstJfdjb.size();
        }

        for (int i = 0; i < lNum; i++) {

            BigDecimal bigYSFYJE = BigDecimal.ZERO; //应收费用金额
            BigDecimal bigSHSHJE = BigDecimal.ZERO; //实收费用金额
            BigDecimal bigSHSHJE_SY = BigDecimal.ZERO; //实收费用金额-损益
            BigDecimal bigSFFCJE = BigDecimal.ZERO; //收费分成金额
            BigDecimal bigSFFCJE_SY = BigDecimal.ZERO;
            BigDecimal bigQFEIJE = BigDecimal.ZERO; //欠费金额
            BigDecimal dioamo = BigDecimal.ZERO; //分润方一金额
            BigDecimal diwamo = BigDecimal.ZERO; //分润方二金额
            BigDecimal ditamo = BigDecimal.ZERO; //分润方三金额
            BigDecimal difamo = BigDecimal.ZERO; //分润方四金额

            KcbChrgRgst tblBsfdj = SysUtil.getInstance(KcbChrgRgst.class);
            KcpChrgAcrl stPflzl = SysUtil.getInstance(KcpChrgAcrl.class);

            tblBsfdj = lstJfdjb.get(i);
            
            stPflzl=PBChargeCodeDao.selChargeAccRuleDetail(tblBsfdj.getChrgcd(), false);
            if(CommUtil.isNull(stPflzl)){
            	throw FeError.Chrg.BNASF371();
            }
            bizlog.debug("收费登记tblBsfdj[%s]", tblBsfdj);
            if (CommUtil.isNull(tblBsfdj)) {
                if (i == 0
                        && (cplChFeeIn.getOprflg() == E_OPRFLG.SEQNO || cplChFeeIn.getOprflg() == E_OPRFLG.SEQ || cplChFeeIn.getOprflg() == E_OPRFLG.TOT)) {
                    throw PbError.PbComm.E0082(cplChFeeIn.getTrandt(), cplChFeeIn.getTrnseq());
                }
                break;
            }

            String sZHYODM, sZHYOMS, sBEIZXX;
            if (CommUtil.isNotNull(cplChFeeIn.getSmrycd())) {
                sZHYODM = cplChFeeIn.getSmrycd(); //摘要代码
                sZHYOMS = cplChFeeIn.getSmryds(); //摘要描述
            }
            else {
                sZHYODM = tblBsfdj.getSmrycd();
                sZHYOMS = tblBsfdj.getSmryds();
            }

            if (CommUtil.isNotNull(cplChFeeIn.getRemark())) {
                sBEIZXX = cplChFeeIn.getRemark(); //备注信息
            }
            else {
                sBEIZXX = tblBsfdj.getRemark();
            }
            	//目前采用的收费方式
            if (cplChFeeIn.getOprflg() == E_OPRFLG.SEQNO) {
                if (CommUtil.compare(cplChFeeIn.getAcclam(), tblBsfdj.getArrgam()) > 0) {
                    String sErr = String.format("实收金额[%f]不允许大于欠费金额[%f]", cplChFeeIn.getAcclam(), tblBsfdj.getArrgam());
                    throw FeError.Chrg.BNASF999(sErr);
                }

                bigYSFYJE = tblBsfdj.getArrgam(); //应收=欠费
                bigSHSHJE = cplChFeeIn.getAcclam(); //实收
                bigSFFCJE = cplChFeeIn.getDvidam(); //收费分成

            }
            else if (cplChFeeIn.getOprflg() == E_OPRFLG.SEQ) {
                bigYSFYJE = tblBsfdj.getArrgam(); //应收=欠费
                bigSHSHJE = tblBsfdj.getArrgam(); //实收=欠费
                bigSFFCJE = cplChFeeIn.getDvidam(); //收费分成
                //sSHOFZQ = tblBsfdj.getChrgpd(); //收费周期
            }
            else if (cplChFeeIn.getOprflg() == E_OPRFLG.BSEQNO) {
                bigYSFYJE = tblBsfdj.getArrgam(); //应收=欠费
                bigSHSHJE = tblBsfdj.getArrgam(); //实收=欠费
                bigSFFCJE = tblBsfdj.getDvidam(); //收费分成
                //sSHOFZQ = tblBsfdj.getChrgpd(); //收费周期

                if (CommUtil.isNotNull(tblBsfdj.getChacno()) || !CommUtil.equals(tblBsfdj.getChrgcy(), tblBsfdj.getTrancy())) {
                    throw FeError.Chrg.BNASF372();
                }
                sKEHHAO_SF = tblBsfdj.getCustno(); //客户号
                sKEHUZH_SF = tblBsfdj.getCustac(); //客户账号 
                eHUOBDH_SF = tblBsfdj.getTrancy(); //交易币种
                eCHUIBZ_SF = tblBsfdj.getCsexfg(); //钞汇标志
                sSHUNXH_SF = tblBsfdj.getSeqnum(); //顺序号 
                sZHANGH_SF = tblBsfdj.getSysacn(); //账号
                sZHUZWM_SF = tblBsfdj.getAcchnm(); //账号中文名 
                bigKYNGJE = tblBsfdj.getTranam(); //交易金额
                if (CommUtil.compare(bigKYNGJE, BigDecimal.ZERO) <= 0) {
                    continue;
                }
            }
            else if (cplChFeeIn.getOprflg() == E_OPRFLG.TOT) {
                bigYSFYJE = tblBsfdj.getArrgam(); //应收=欠费
                bigSHSHJE = tblBsfdj.getArrgam(); //实收=欠费
                bigSFFCJE = tblBsfdj.getDvidam(); //收费分成
                // sSHOFZQ = tblBsfdj.getChrgpd(); //收费周期
                bigKYNGJE = cplChFeeIn.getAcclam(); //实收费用金额

                DpProdSvcType QryAcct = SysUtil.getInstance(DpProdSvcType.class); ////查询负债账户
                QryDpAcctOut cplQryAcctOut = SysUtil.getInstance(QryDpAcctOut.class); //查询负债账户输出
                if (CommUtil.isNull(tblBsfdj.getChacno())) {

                    sKEHHAO_SF = tblBsfdj.getCustno(); //客户号
                    sKEHUZH_SF = tblBsfdj.getCustac(); //客户账号 
                    eHUOBDH_SF = tblBsfdj.getTrancy(); //交易币种
                    eCHUIBZ_SF = tblBsfdj.getCsexfg(); //钞汇标志
                    sSHUNXH_SF = tblBsfdj.getSeqnum(); //顺序号 
                    sZHANGH_SF = tblBsfdj.getSysacn(); //账号
                    sZHUZWM_SF = tblBsfdj.getAcchnm(); //账号中文名 
                    cplQryAcctOut = QryAcct.QryDpByAcctno(sZHANGH_SF);

                }
                else {

                    cplQryAcctOut = QryAcct.QryDpByAcctno(tblBsfdj.getChacno());
                    
                    sKEHHAO_SF = cplQryAcctOut.getCustno(); //收费客户号
                    sZHUZWM_SF = cplQryAcctOut.getAcctna(); //收费客户中文名
                    sKEHUZH_SF = tblBsfdj.getChacno(); //收费客户账号
                    eHUOBDH_SF = tblBsfdj.getChaccy(); //收费币种
                    eCHUIBZ_SF = tblBsfdj.getChcefg(); //收费钞汇标志
                    sSHUNXH_SF = tblBsfdj.getChacsq(); //收费账号序号
                    sZHANGH_SF = tblBsfdj.getChsyac(); //收费账号

                }
            }
            
            if (CommUtil.compare(tblBsfdj.getChrgcy(),eSFBIZH)!=0) {
                throw PbError.Charg.E0007(tblBsfdj.getChrgcy(), eSFBIZH);
            }

            //mod 2016/12/13 slw 币种判断
            String eJifeibz = tblBsfdj.getClchcy(); //计费币种
            if (CommUtil.compare(stPflzl.getChrgcd(), tblBsfdj.getChrgcd()) == 0) {

                if (CommUtil.compare(stPflzl.getCrcycd(),eJifeibz)!=0) {

                    throw PbError.PbComm.E0057(tblBsfdj.getChrgcd(), eJifeibz);

                }
            }

            if (cplChFeeIn.getCstrfg() == E_CSTRFG.TRNSFER && cplChFeeIn.getIfflag() == E_YES___.YES) {
                if (CommUtil.isNull(sKEHUZH_SF)) {
                    throw PbError.PbComm.E0126();
                }
            }
            
            //  金额处理
            bigYSFYJE = BusiTools.roundIncinByCurrency(cplChFeeIn.getTrancy(), bigYSFYJE,null);
            bigSHSHJE = BusiTools.roundIncinByCurrency(cplChFeeIn.getTrancy(), bigSHSHJE,null);
            bigSFFCJE = BusiTools.roundIncinByCurrency(cplChFeeIn.getTrancy(), bigSFFCJE,null);

            bizlog.debug("收费币种[%s] 现转标志[%s] 应收金额[%f] 实收金额[%f] 分成金额[%f]", eSFBIZH, cplChFeeIn.getCstrfg(), bigYSFYJE, bigSHSHJE, bigSFFCJE);

            String sSHOQBZ = "1"; //1-已收讫
            if (CommUtil.compare(bigSHSHJE, BigDecimal.ZERO) > 0) {

                if (cplChFeeIn.getIfflag() == E_YES___.YES || cplChFeeIn.getOprflg() == E_OPRFLG.TOT) {

                    bizlog.debug("记现金账或客户账[%f]", bigSHSHJE);
                    if (cplChFeeIn.getCstrfg() == E_CSTRFG.CASH) {
                        bizlog.debug("记现金账[%f]", bigSHSHJE);
                        //if (CommUtil.compare(cplChFeeIn.getOprflg(), "9") != 0) {
                        // TODO 暂不考虑现金
                        //}
                        throw FeError.Chrg.BNASF324();
                    }
                    else {
                        bizlog.debug("记客户账[%s]", sKEHUZH_SF);
                        if (tblBsfdj.getCgpyrv() == E_CGPYRV.RECIVE) {
                        	//非集中收费或者销记收费明细
                            if (stPflzl.getChrgsg()==E_YES___.NO||cplChFeeIn.getIsclos()==cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES)
                            {
                                // 实时,统一收费交易
                                bizlog.debug("实收金额bigSHSHJE[%s]", bigSHSHJE);
                                bizlog.debug("可用余额bigKYNGJE[%s]", bigKYNGJE);
                                if (CommUtil.compare(bigSHSHJE, BigDecimal.ZERO) > 0 && CommUtil.compare(bigKYNGJE, BigDecimal.ZERO) < 0
                                        || CommUtil.compare(bigSHSHJE, bigKYNGJE) > 0) {

                                    if (CommUtil.compare(sJIAOYM, "chgacc") == 0) { //账户批量扣费
                                        sQFEIYY = String.format("账户余额[%f]不足扣费[%f]", bigKYNGJE, bigSHSHJE);
                                        bigQFEIJE = bigSHSHJE; //欠费 =实收 
                                        bigSHSHJE = BigDecimal.ZERO;
                                        tblBsfdj.setChfltm(tblBsfdj.getChfltm() + 1); //失败次数+1
                                        if (CommUtil.isNotNull(tblBsfdj.getRecvfg())) {
                                            sSHOQBZ = tblBsfdj.getRecvfg().getValue();
                                        }
                                    }
                                    else {
                                        throw FeError.Chrg.BNASF373();//账户余额不足
                                    }
                                }

                                if (cplChFeeIn.getOprflg() == E_OPRFLG.SEQ) {
                                    bigKYNGJE = bigKYNGJE.subtract(bigSHSHJE);
                                }
                                tblBsfdj.setChgdat(sJIOYRQ);//	
                            }
                            else {
                                // 非实时收费
                            	
                            	if(stPflzl.getChrgsg()==E_YES___.YES&&!CommUtil.equals("batchg", CommTools.getBaseRunEnvs().getTrxn_code())){
                            		//集中收费
                                    sQFEIYY = String.format("费用为集中收费类型");
                                    bigQFEIJE = bigSHSHJE;
                                    bigSHSHJE = BigDecimal.ZERO;

                                    String sXiayizq = "";
                                    sXiayizq = DateTools2.calDateByCycle(sJIOYRQ, tblBsfdj.getChrgpd());
                                    
                                    tblBsfdj.setChgdat(sXiayizq);
                                    if (CommUtil.isNotNull(tblBsfdj.getRecvfg())) {
                                        sSHOQBZ = tblBsfdj.getRecvfg().getValue(); 
                                    }
                            	}else  if (CommUtil.compare(bigSHSHJE, BigDecimal.ZERO) > 0 && CommUtil.compare(bigSHSHJE, bigKYNGJE) > 0) {
                            		
                                	bizlog.debug("收费金额[%f] 可用金额[%f] 失败处理方式[%s]...", bigSHSHJE, bigKYNGJE, stPflzl.getBllwtp());
                                	
                                    switch (stPflzl.getBllwtp()) {
                                    case OWE: /* 失败算欠费 */
                                        sQFEIYY = String.format("账户余额[%f]不足扣费[%f]", bigKYNGJE, bigSHSHJE);

                                        bigQFEIJE = bigSHSHJE;
                                        bigSHSHJE = BigDecimal.ZERO;
                                        tblBsfdj.setChfltm(tblBsfdj.getChfltm() + 1);
                                        if (CommUtil.isNotNull(tblBsfdj.getRecvfg())) {
                                            sSHOQBZ = tblBsfdj.getRecvfg().getValue();
                                        }

                                        // 通过频率周期获取下一日期
                                        String sXiayizq = "";
                                        if (CommUtil.compare(stPflzl.getDebkpd(), "0") > 0) {
                                            sXiayizq = DateTools2.calDateByCycle(sJIOYRQ, stPflzl.getDebkpd());
                                        }
                                        tblBsfdj.setChgdat(sXiayizq); //收费日期
                                        break;
//                                    case ONLNBL: /* 扣剩余部分 */
//
//                                        sQFEIYY = String.format("账户余额[%f]不足扣费[%f]", bigKYNGJE, bigSHSHJE);
//                                        if (CommUtil.compare(bigKYNGJE, BigDecimal.ZERO) > 0) {
//                                            bigQFEIJE = BigDecimal.ZERO;
//                                            bigSHSHJE = bigKYNGJE;
//                                        }
//                                        else {
//                                            bigQFEIJE = BigDecimal.ZERO;
//                                            bigSHSHJE = BigDecimal.ZERO;
//                                        }
//                                        sSHOQBZ = "1"; //1-已收讫
//                                        break;
//                                    case GRACE_PERIOD: /* 宽限期扣 */
//                                        if (CommUtil.compare(tblBsfdj.getChgdat(), "00000000") == 0 || CommUtil.isNull(stPflzl.getCractm())) {
//                                            throw PbError.PbComm.E2015("输入宽限期不完全,缺少指定日期或宽限期限");
//                                        }
//                                        else {
//
//                                            String sDaoqrq = DateTools2.calDateByFreq(tblBsfdj.getChgdat(), stPflzl.getCractm(), "", CommTools.getBaseRunEnvs().getTrxn_branch(), 3, 1);
//                                            if (CommUtil.compare(sJIOYRQ, sDaoqrq) >= 0) {
//                                                sQFEIYY = String.format("账户余额[%f]不足扣费[%f]已过宽限期", bigKYNGJE, bigSHSHJE);
//
//                                                bigQFEIJE = bigSHSHJE;
//                                                bigSHSHJE = BigDecimal.ZERO;
//                                                tblBsfdj.setChfltm(tblBsfdj.getChfltm() + 1);
//                                                sSHOQBZ = "3"; //3-扣费失败 
//                                            }
//                                            else {
//
//                                                sQFEIYY = String.format("账户余额[%f]不足扣费[%f]", bigKYNGJE, bigQFEIJE);
//
//                                                bigQFEIJE = bigSHSHJE;
//                                                bigSHSHJE = BigDecimal.ZERO;
//                                                tblBsfdj.setChfltm(tblBsfdj.getChfltm() + 1);
//                                                if (CommUtil.isNotNull(tblBsfdj.getRecvfg())) {
//                                                    sSHOQBZ = tblBsfdj.getRecvfg().getValue();
//                                                }
//
//                                            }
//                                        }
//
//                                        break;
//
                                    case NEXT_CYCLE: /* 下周期扣 */

                                        sQFEIYY = String.format("账户余额[%f]不足扣费[%f]", bigKYNGJE, bigSHSHJE);
                                        bigQFEIJE = bigSHSHJE;
                                        bigSHSHJE = BigDecimal.ZERO;

                                        sXiayizq = "";
                                        if (CommUtil.compare(stPflzl.getChrgpd(), "0") > 0) {
                                            sXiayizq = DateTools2.calDateByCycle(sJIOYRQ, stPflzl.getDebkpd());
                                        }
                                        tblBsfdj.setChgdat(sXiayizq);
                                        if (CommUtil.isNotNull(tblBsfdj.getRecvfg())) {
                                            sSHOQBZ = tblBsfdj.getRecvfg().getValue();
                                        }
                                        tblBsfdj.setChfltm(tblBsfdj.getChfltm() + 1);
                                        break;

//                                    case IGNORE: /* 不扣 */
//
//                                        bigQFEIJE = BigDecimal.ZERO;
//                                        bigSHSHJE = BigDecimal.ZERO;
//                                        sSHOQBZ = "1"; //1-已收讫
//                                        break;

                                    case ONLNBL_OWE: /* 扣剩余,其余算欠费 */

                                        if (CommUtil.compare(bigKYNGJE, BigDecimal.ZERO) <= 0) {
                                            sQFEIYY = String.format("账户余额[%f]为0,未收讫", bigKYNGJE);
                                            if (CommUtil.isNotNull(tblBsfdj.getRecvfg())) {
                                                sSHOQBZ = tblBsfdj.getRecvfg().getValue();
                                            }
                                            bigQFEIJE = bigSHSHJE;
                                            bigSHSHJE = BigDecimal.ZERO;
                                        }
                                        else {
                                            sQFEIYY = String.format("账户余额[%f]不足扣费[%f]", bigKYNGJE, bigSHSHJE);
                                            sSHOQBZ = "2"; //2-部分收讫
                                            bigQFEIJE = bigSHSHJE.subtract(bigKYNGJE);
                                            bigSHSHJE = bigKYNGJE;

                                        }
                                        tblBsfdj.setChfltm(tblBsfdj.getChfltm() + 1);
                                        sXiayizq = "";
                                        if (CommUtil.compare(stPflzl.getDebkpd(), "0") > 0) {
                                            sXiayizq = DateTools2.calDateByCycle(sJIOYRQ, stPflzl.getDebkpd());
                                           // sXiayizq = DateTools2.calDateByFreq(sJIOYRQ, stPflzl.getDebkpd(), "", sYNGYJG, 3, 1);
                                        }
                                        tblBsfdj.setChgdat(sXiayizq);
                                        break;

//                                    case SWITCH_FAILE: /* 转扣费失败 */
//
//                                        sQFEIYY = String.format("账户余额[%f]不足扣费[%f]", bigKYNGJE, bigSHSHJE);
//                                        bigQFEIJE = bigSHSHJE;
//                                        bigSHSHJE = BigDecimal.ZERO;
//                                        tblBsfdj.setChfltm(tblBsfdj.getChfltm() + 1);
//                                        sSHOQBZ = "3"; //3-扣费失败
//                                        break;

                                    default:
                                        if (CommUtil.compare(sJIAOYM, "chgacc") == 0) { //账户批量扣费
                                            sQFEIYY = String.format("账户余额[%f]不足扣费[%f]", bigKYNGJE, bigSHSHJE);
                                            bigQFEIJE = bigSHSHJE; //欠费 =实收 
                                            bigSHSHJE = BigDecimal.ZERO;
                                            tblBsfdj.setChfltm(tblBsfdj.getChfltm() + 1); //失败次数+1
                                            if (CommUtil.isNotNull(tblBsfdj.getRecvfg())) {
                                                sSHOQBZ = tblBsfdj.getRecvfg().getValue();
                                            }
                                        }
                                        else {
                                            throw PbError.PbComm.E0036("", sKEHUZH_SF);
                                        }
                                    }

                                }

                                bigSHSHJE = BusiTools.roundIncinByCurrency(cplChFeeIn.getTrancy(), bigSHSHJE,null);
                                bigQFEIJE = BusiTools.roundIncinByCurrency(cplChFeeIn.getTrancy(), bigQFEIJE,null);
                                bizlog.debug("实收金额[%f] 欠费金额[%f] 收讫标志[%s]", bigSHSHJE, bigQFEIJE, sSHOQBZ);
                            }

                            if (CommUtil.compare(bigSHSHJE, BigDecimal.ZERO) > 0) {
                                if (cplChFeeIn.getOprflg() != E_OPRFLG.TOT) {
                                    //  记账处理
                                    CgChargefeeKhz_IN cplKhz = SysUtil.getInstance(CgChargefeeKhz_IN.class);
                                    cplKhz.setCstrfg(cplChFeeIn.getCstrfg());
                                    cplKhz.setOprflg(cplChFeeIn.getOprflg());
                                    cplKhz.setCgpyrv(tblBsfdj.getCgpyrv());
                                    cplKhz.setCustno(sKEHHAO_SF);
                                    cplKhz.setActtyp(eKHZHLX_SF);
                                    cplKhz.setCustac(sKEHUZH_SF);
                                    cplKhz.setTrancy(eSFBIZH);
                                    cplKhz.setCsexfg(eCHUIBZ_SF);
                                    cplKhz.setActseq(sSHUNXH_SF);
                                    cplKhz.setSysacn(sZHANGH_SF);
                                    cplKhz.setAcclam(bigSHSHJE);
                                    cplKhz.setCsprcd(cplChFeeIn.getCsprcd());
                                    cplKhz.setSmrycd(sZHYODM);
                                    cplKhz.setSmryds(sZHYOMS);
                                    cplKhz.setRemark(sBEIZXX);
                                    cplKhz.setProdcd(tblBsfdj.getProdcd());
                                    cplKhz.setPayaDetail(cplChFeeIn.getPayaDetail());
                                    cplKhz.setPaydDetail(cplChFeeIn.getPaydDetail());    
                                    cplKhz.setStrktg(cplChFeeIn.getStrktg());
                                    cplKhz.setClactp(cplChFeeIn.getClactp());
                                    calChargefeeKhz(cplKhz);

                                }
                            }

                        }

                        else if (tblBsfdj.getCgpyrv() == E_CGPYRV.PAY) /** 付费 客户账存入 */
                        {

                            if (cplChFeeIn.getOprflg() != E_OPRFLG.TOT) {
                                //  记账处理                               
                                CgChargefeeKhz_IN cplKhz = SysUtil.getInstance(CgChargefeeKhz_IN.class);
                                cplKhz.setCstrfg(cplChFeeIn.getCstrfg());
                                cplKhz.setOprflg(cplChFeeIn.getOprflg());
                                cplKhz.setCgpyrv(tblBsfdj.getCgpyrv());
                                cplKhz.setCustno(sKEHHAO_SF);
                                cplKhz.setActtyp(eKHZHLX_SF);
                                cplKhz.setCustac(sKEHUZH_SF);
                                cplKhz.setTrancy(eSFBIZH);
                                cplKhz.setCsexfg(eCHUIBZ_SF);
                                cplKhz.setActseq(sSHUNXH_SF);
                                cplKhz.setSysacn(sZHANGH_SF);
                                cplKhz.setAcclam(bigSHSHJE);
                                cplKhz.setCsprcd(cplChFeeIn.getCsprcd());
                                cplKhz.setSmrycd(sZHYODM);
                                cplKhz.setSmryds(sZHYOMS);
                                cplKhz.setRemark(sBEIZXX);
                                cplKhz.setProdcd(tblBsfdj.getProdcd());
                                cplKhz.setPayaDetail(cplChFeeIn.getPayaDetail());
                                cplKhz.setPaydDetail(cplChFeeIn.getPaydDetail());   
                                cplKhz.setClactp(cplChFeeIn.getClactp());
                                calChargefeeKhz(cplKhz);

                            }

                        }
                    }
                }

                bizlog.debug("记损益账:实收金额bigSHSHJE[%f]", bigSHSHJE);
                /* 记损益账 */
                if (CommUtil.compare(bigSHSHJE, BigDecimal.ZERO) > 0) {

/*                    bizlog.debug("记损益账:损益入账控制Plcgct[%s]", stPflzl.getPlcgct());
                    if (CommUtil.compare(stPflzl.getPlcgct(), "1") != 0 || tblBsfdj.getChrgcy() == tblBsfdj.getClchcy()) {

                        eHUOBDH_SY = tblBsfdj.getChrgcy();
                        bigSHSHJE_SY = bigSHSHJE;
                    }
                    else { */

                        //  TODO  货币兑换记账处理    IoFxGetRealRate
                        //	/* 调用货币兑换记账构件 */
                        //	IoFxGetRealRate cplFxapi = SysUtil.getInstance(IoFxGetRealRate.class);
                        //	cplFxKp_Out = cplFxapi.keepExAccount(cplFxKp_In);
                        //	bigSHSHJE_SY = cplFxKp_Out.getMairjine();

                        bigSHSHJE_SY = bigSHSHJE; //暂不考虑货币兑换，固定赋值
                        eHUOBDH_SY = tblBsfdj.getClchcy();
//                    }

                    bizlog.debug("挂账业务编码Cghacd()[%s]", tblBsfdj.getCghacd());
                    if (CommUtil.isNull(tblBsfdj.getCghacd())) {

                        bizlog.debug("记损益账 机构[%s] 业务代号[%s] 币种[%s] 金额[%f]", tblBsfdj.getAcctbr(), tblBsfdj.getCgfacd(), eHUOBDH_SY, bigSHSHJE_SY);
                        CgDanbJzFee_IN cplDbFee_In = SysUtil.getInstance(CgDanbJzFee_IN.class);
                        cplDbFee_In.setCgpyrv(tblBsfdj.getCgpyrv());
                        cplDbFee_In.setAcctbr(tblBsfdj.getAcctbr());
                        cplDbFee_In.setChrgcd(tblBsfdj.getChrgcd());
                        cplDbFee_In.setCgfacd(tblBsfdj.getCgfacd());
                        cplDbFee_In.setCghacd(tblBsfdj.getCghacd());
                        cplDbFee_In.setTrancy(tblBsfdj.getTrancy());
                        cplDbFee_In.setTranam(bigSHSHJE_SY);
                        cplDbFee_In.setTrancy(eHUOBDH_SY);
                        cplDbFee_In.setCstrfg(tblBsfdj.getCstrfg());
                        cplDbFee_In.setCsexfg(eCHUIBZ_SF);
                        cplDbFee_In.setCustac(sKEHUZH_SF);
                        cplDbFee_In.setCustno(sKEHHAO_SF);
                        cplDbFee_In.setCuscnm(sZHUZWM_SF);
                        cplDbFee_In.setProdcd(tblBsfdj.getProdcd());
                        cplDbFee_In.setSmrycd(sZHYODM);
                        cplDbFee_In.setSmryds(sZHYOMS);
                        cplDbFee_In.setRemark(sBEIZXX);
                        cplDbFee_In.setTrinfo(tblBsfdj.getTrinfo());//交易信息
                        calChrgfeeAcsq(cplDbFee_In);
                        
                	}
                    
                    else {

                        bizlog.debug("费用挂账 机构[%s] 业务编号[%s] 币种[%s] 金额[%f]", tblBsfdj.getAcctbr(), tblBsfdj.getCghacd(), eHUOBDH_SY, bigSHSHJE_SY);
                        //内部账记帐
                        IoInAccount InAccounting = SysUtil.getInstance(IoInAccount.class);
                        IaAcdrInfo InAccount_in = SysUtil.getInstance(IaAcdrInfo.class);
                        IaTransOutPro InAccount_Out = SysUtil.getInstance(IaTransOutPro.class);
                        InAccount_in.setAcbrch(tblBsfdj.getAcctbr()); //帐务机构
                        InAccount_in.setBusino(tblBsfdj.getCghacd()); //业务编码
                        InAccount_in.setTranam(bigSHSHJE_SY); //交易金额
                        InAccount_in.setCrcycd(eHUOBDH_SY); //币种
                        InAccount_in.setSmrycd(BusinessConstants.SUMMARY_SF); //摘要码 XZ-收费
                        InAccount_in.setToacct(sKEHUZH_SF); //对方账号
                        InAccount_in.setToacna(sZHUZWM_SF); //对方户名
                        if (tblBsfdj.getCgpyrv() == E_CGPYRV.PAY) {
                            //付费，记借方
                            InAccount_Out = InAccounting.ioInAcdr(InAccount_in);
                        } else if (tblBsfdj.getCgpyrv() == E_CGPYRV.RECIVE) {
                            //收费，记贷方
                            InAccount_Out = InAccounting.ioInAccr(InAccount_in);
                        }
                        sFYGZZH = InAccount_Out.getAcctno();//挂账账号
/*
                        bizlog.debug("转出行分成比例Zchfcblv()[%s]", tblBsfdj.getOudvrt());
                        bizlog.debug("转入行分成比例Zrhfcblv()[%s]", tblBsfdj.getIndvrt());
                        bizlog.debug("交易行分成比例Zrhfcblv()[%s]", tblBsfdj.getTrdvrt());
*/
                        if (CommUtil.compare((tblBsfdj.getDioamo().add(tblBsfdj.getDiwamo()).add(tblBsfdj.getDitamo()).add(tblBsfdj.getDifamo())), BigDecimal.ZERO) > 0) {
                            bizlog.debug("计费的分成为0,则等于实收金额[%f]", bigSFFCJE_SY);
                            //TODO
                            if (CommUtil.isNotNull(stPflzl.getBllwtp()) && stPflzl.getBllwtp() != E_BLLWTP.OWE && stPflzl.getBllwtp() != E_BLLWTP.GRACE_PERIOD
                                    && stPflzl.getBllwtp() != E_BLLWTP.NEXT_CYCLE) {
                                throw FeError.Chrg.BNASF374();
                            }
                            //if (CommUtil.compare(bigSFFCJE, BigDecimal.ZERO) < 0) {
                            if (CommUtil.compare(bigSFFCJE, BigDecimal.ZERO) <= 0) {
                                bigSFFCJE_SY = bigSHSHJE_SY;
                                bizlog.debug("计费的分成为0,则等于实收金额[%f]", bigSFFCJE_SY);
                            }
                            else {
                                bizlog.debug("收费分成金额bigSFFCJE[%f]", bigSFFCJE);
                                bizlog.debug("实收金额bigSHSHJE[%f]", bigSHSHJE);
                                bigSFFCJE_SY = bigSFFCJE.multiply(bigSHSHJE_SY).divide(bigSHSHJE, 6, BigDecimal.ROUND_DOWN);
                                bizlog.debug("收费分成金额[%f]", bigSFFCJE_SY);
                            }


                            if (CommUtil.compare(bigSFFCJE_SY, bigSHSHJE_SY) > 0) {
                                throw PbError.PbComm.E2120(bigSFFCJE_SY, bigSHSHJE_SY);
                            }
                            bizlog.debug("分成金额[%f] 分润方一金额[%f] 分润方二金额[%f] 分润方三金额[%f] 分润方四金额[%f]", bigSFFCJE_SY, tblBsfdj.getDioamo(), tblBsfdj.getDiwamo(), tblBsfdj.getDitamo(),tblBsfdj.getDifamo());
                            dioamo = tblBsfdj.getDioamo();
                            diwamo = tblBsfdj.getDiwamo();
                            ditamo = tblBsfdj.getDitamo();
                            difamo = tblBsfdj.getDifamo();


                            KcbDvidRgst tblBsffc = SysUtil.getInstance(KcbDvidRgst.class);
                            tblBsffc.setCaldat(tblBsfdj.getTrandt());
                            tblBsffc.setCalseq(tblBsfdj.getTrnseq());
                            tblBsffc.setEvrgsq(tblBsfdj.getEvrgsq());
                            tblBsffc.setCgstno(tblBsfdj.getCgstno() + 1);
                            tblBsffc.setChrgcd(tblBsfdj.getChrgcd());
                            tblBsffc.setChrgna(tblBsfdj.getChrgna());
                            tblBsffc.setCgpyrv(tblBsfdj.getCgpyrv());
                            tblBsffc.setCrcycd(eHUOBDH_SY);
                            tblBsffc.setDvidam(bigSFFCJE_SY);
                            tblBsffc.setCgfacd(tblBsfdj.getCgfacd());
                            tblBsffc.setCghacd(tblBsfdj.getCghacd());
                            tblBsffc.setLdgacn(sFYGZZH);
                            tblBsffc.setLdseno(tblBsfdj.getLdseno());
                            tblBsffc.setAcctbr(tblBsfdj.getAcctbr());
                            tblBsffc.setDioamo(dioamo);
                            tblBsffc.setDiwamo(diwamo);
                            tblBsffc.setDitamo(ditamo);
                            tblBsffc.setDifamo(difamo);
                            tblBsffc.setDioage(tblBsfdj.getDioage());
                            tblBsffc.setDiwage(tblBsfdj.getDiwage());
                            tblBsffc.setDitage(tblBsfdj.getDitage());
                            tblBsffc.setDifage(tblBsfdj.getDifage());
                            tblBsffc.setDvidst(E_DVIDST.NO);
                            tblBsffc.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());
                            tblBsffc.setTrnchl(CommTools.getBaseRunEnvs().getChannel_id());
                            tblBsffc.setRemark(sBEIZXX);
                            tblBsffc.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
                            //tblBsffc.setTrnseq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
                            tblBsffc.setTrnseq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
                            tblBsffc.setTrntim(Long.valueOf(DateTools2.getCurrentTimestamp()));
                            tblBsffc.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());

                            try {
                                KcbDvidRgstDao.insert(tblBsffc);
                            } catch (Exception e) {
                                throw FeError.Chrg.BNASF999("插入收费分成登记簿失败,其他错误", e);
                            }

                        }
                  
                    }

                    bizlog.debug("费用记账结束");

                }
                else {
                    bizlog.debug("实收金额为零 不记费用或费用挂账账");
                }

            }
            else {
                bizlog.debug("实收金额为零");
            }

            if (tblBsfdj.getCgpyrv() == E_CGPYRV.RECIVE) {
                bigSFYSJE = bigSFYSJE.add(bigYSFYJE);
                bigSFSSJE = bigSFSSJE.add(bigSHSHJE);
                bigSFQFJE = bigSFQFJE.add(bigQFEIJE);

            }
            else if (tblBsfdj.getCgpyrv() == E_CGPYRV.PAY) {
                bigFFYFJE = bigFFYFJE.add(bigYSFYJE);
                bigFFSFJE = bigFFSFJE.add(bigSHSHJE);
                bigFFQFJE = bigFFQFJE.add(bigQFEIJE);
            }

            tblBsfdj.setAcclam(bigSHSHJE.add(tblBsfdj.getAcclam()));/* 实收金额 */
            tblBsfdj.setArrgam(bigQFEIJE); //欠费金额
            tblBsfdj.setReduam(tblBsfdj.getReduam().add(bigYSFYJE).subtract(bigSHSHJE).subtract(bigQFEIJE));/* 费用减免金额=费用减免金额+应收费用-实收费用 -欠费金额*/
            tblBsfdj.setRecvfg(CommUtil.toEnum(E_GTAMFG.class, sSHOQBZ)); /* 收讫标志 */
            tblBsfdj.setDvidam(bigSFFCJE);/* 收费分成金额 */
            tblBsfdj.setLdgacn(sFYGZZH);/* 费用挂账账号 */
            tblBsfdj.setArgrsn(sQFEIYY);/* 欠费原因 */
            tblBsfdj.setCgstno(tblBsfdj.getCgstno() + 1); /*收费明细序号*/

            if (CommUtil.compare(sJIAOYM, "chgacc") == 0) { //账号批量扣费
                tblBsfdj.setLschda(CommTools.getBaseRunEnvs().getTrxn_date()); //上次批扣日期=交易日期
            }
            try {
                KcbChrgRgstDao.updateOne_odb1(tblBsfdj);
            } catch (Exception e) {
                throw FeError.Chrg.BNASF999("更新收费登记簿失败", e);
            }

            bizlog.debug("费用记账明细实收金额tblBfymx:bigSHSHJE[%f]", bigSHSHJE);
            if (CommUtil.compare(bigSHSHJE, BigDecimal.ZERO) > 0) {
            	KcbChrgDetl tblBfymx = SysUtil.getInstance(KcbChrgDetl.class);
                tblBfymx.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
                tblBfymx.setTrnseq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
                tblBfymx.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());
                tblBfymx.setTrnchl(CommTools.getBaseRunEnvs().getChannel_id());
                tblBfymx.setCaldat(tblBsfdj.getTrandt());/* 计费发生日期 */
                tblBfymx.setCalseq(tblBsfdj.getTrnseq());/* 计费柜员流水 */
                tblBfymx.setEvrgsq(tblBsfdj.getEvrgsq());
                tblBfymx.setCgstno(tblBsfdj.getCgstno());
                tblBfymx.setChrgcd(tblBsfdj.getChrgcd());
                tblBfymx.setChrgna(tblBsfdj.getChrgna());
                tblBfymx.setCgpyrv(tblBsfdj.getCgpyrv());
                tblBfymx.setClchcy(tblBsfdj.getClchcy());
                tblBfymx.setClcham(tblBsfdj.getClcham());
                tblBfymx.setDisrat(tblBsfdj.getDisrat());
                tblBfymx.setDircam(tblBsfdj.getDircam());
                tblBfymx.setChrgcy(eSFBIZH);
                tblBfymx.setRecvam(bigYSFYJE);
                tblBfymx.setAcclam(bigSHSHJE);
                tblBfymx.setArrgam(bigQFEIJE);
                tblBfymx.setReduam(bigYSFYJE.subtract(bigSHSHJE).subtract(bigQFEIJE));
                tblBfymx.setDvidam(bigSFFCJE);
                tblBfymx.setCstrfg(cplChFeeIn.getCstrfg());
                tblBfymx.setAcctbr(tblBsfdj.getAcctbr());
                tblBfymx.setCgfacd(tblBsfdj.getCgfacd());
                tblBfymx.setCghacd(tblBsfdj.getCghacd());
                tblBfymx.setLdgacn(sFYGZZH);
                tblBfymx.setLdseno(tblBsfdj.getLdseno());
                tblBfymx.setCustno(sKEHHAO_SF);
                tblBfymx.setActtyp(eKHZHLX_SF);
                tblBfymx.setChacno(sKEHUZH_SF);
                tblBfymx.setCrcycd(eHUOBDH_SF);
                tblBfymx.setCsexfg(eCHUIBZ_SF);
                long lSHUNXH_SF = 0;
                if (CommUtil.isNotNull(sSHUNXH_SF)) {
                    lSHUNXH_SF = Long.parseLong(sSHUNXH_SF);
                }
                tblBfymx.setSequno(lSHUNXH_SF);
                tblBfymx.setChrgac(sZHANGH_SF);
                tblBfymx.setAcchnm(sZHUZWM_SF);
                tblBfymx.setProdcd(tblBsfdj.getProdcd());
                tblBfymx.setBlpdno(tblBsfdj.getBlpdno());
                tblBfymx.setArgrsn(sQFEIYY);
                tblBfymx.setSmrycd(sZHYODM);
                tblBfymx.setSmryds(sZHYOMS);
                tblBfymx.setPrtrdt(tblBsfdj.getOrbsda());
                tblBfymx.setPrtrsq(tblBsfdj.getOrbssn());
                tblBfymx.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());
                tblBfymx.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());
                tblBfymx.setTrntim(Long.valueOf(DateTools.getCurrentTime()));
                //tblBfymx.setAuttel(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus());
                tblBfymx.setAuttel(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus());
                tblBfymx.setRuisma(E_RUISMA.WG);
                tblBfymx.setWaisma(E_WAISMA.NORMAL);
                tblBfymx.setFepecd(tblBsfdj.getFepecd());
                tblBfymx.setFepefg(tblBsfdj.getFepefg());
                tblBfymx.setFepamt(tblBsfdj.getFepamt());
                tblBfymx.setFebaln(tblBsfdj.getFebaln());
                //tblBfymx.setJiluztai(E_JILUZTAI.Normal);

                try {
                    KcbChrgDetlDao.insert(tblBfymx);
                } catch (Exception e) {
                    throw FeError.Chrg.BNASF999("新增收费记账明细记录失败", e);
                }

            }

            Options<CgFEEINFO> lstFee = new DefaultOptions<CgFEEINFO>();
            if (cplChFeeIn.getOprflg() != E_OPRFLG.BSEQNO) {
                CgFEEINFO cplFee = SysUtil.getInstance(CgFEEINFO.class);
                cplFee.setTrandt(tblBsfdj.getTrandt());
                cplFee.setTrnseq(tblBsfdj.getTrnseq());
                cplFee.setSequno(tblBsfdj.getEvrgsq());
                cplFee.setCgpyrv(tblBsfdj.getCgpyrv());
                cplFee.setChrgcd(tblBsfdj.getChrgcd());
                cplFee.setChrgna(tblBsfdj.getChrgna());
                cplFee.setTrancy(tblBsfdj.getTrancy());
                cplFee.setClchcy(tblBsfdj.getClchcy());
                cplFee.setClcham(tblBsfdj.getClcham());
                cplFee.setRecvam(tblBsfdj.getDircam());
                cplFee.setFavpec(tblBsfdj.getDisrat());
                cplFee.setChrgcy(tblBsfdj.getChrgcy());
                cplFee.setRecvam(bigYSFYJE);
                cplFee.setAcclam(bigSHSHJE);
                cplFee.setArrgam(bigQFEIJE);
                cplFee.setDvidam(bigSFFCJE);
                cplFee.setDioamo(tblBsfdj.getDioamo());
                cplFee.setDiwamo(tblBsfdj.getDiwamo());
                cplFee.setDitamo(tblBsfdj.getDitamo());
                cplFee.setDifamo(tblBsfdj.getDifamo());
                cplFee.setDioage(tblBsfdj.getDioage());
                cplFee.setDiwage(tblBsfdj.getDiwage());
                cplFee.setDitage(tblBsfdj.getDitage());
                cplFee.setDifage(tblBsfdj.getDifage());
                
                
                cplFee.setChgpln(tblBsfdj.getDiplcd());
                cplFee.setChrgpd(tblBsfdj.getChrgpd());
                cplFee.setDedudt(tblBsfdj.getChgdat());
                cplFee.setRecvfg(tblBsfdj.getRecvfg());
                cplFee.setCgfacd(tblBsfdj.getCgfacd());
                cplFee.setCghacd(tblBsfdj.getCghacd());
                cplFee.setSysacn(sZHANGH_SF);
                lstFee.add(cplFee);
                cplChFeeOut.setLstfyinf(lstFee);

            }
        }

        if (cplChFeeIn.getOprflg() == E_OPRFLG.BSEQNO) {

        }

        bizlog.debug("收费总额[%f] 付费总额[%f]", bigSFSSJE, bigFFSFJE);
        E_CGPYRV eFYSFBZ = null;
        BigDecimal bigHJYSJE = BigDecimal.ZERO;
        BigDecimal bigHJJZJE = BigDecimal.ZERO;
        BigDecimal bigHJQFJE = BigDecimal.ZERO;
        if (CommUtil.compare(bigSFSSJE, bigFFSFJE) <= 0 && CommUtil.compare(bigSFYSJE, bigFFYFJE) <= 0) {
            bigHJYSJE = BigDecimal.ZERO;
            bigHJJZJE = BigDecimal.ZERO;
            bigHJQFJE = BigDecimal.ZERO;
        }
        else if (CommUtil.compare(bigSFSSJE, bigFFSFJE) >= 0) {
            if (CommUtil.compare(bigFFSFJE, bigSFSSJE) > 0) {
                /* 付费金额大于收费金额时,则付费 */
                eFYSFBZ = E_CGPYRV.PAY;
                bigHJYSJE = bigFFYFJE.subtract(bigSFYSJE);
                bigHJJZJE = bigFFSFJE.subtract(bigSFSSJE);
                bigHJQFJE = bigFFQFJE.subtract(bigSFQFJE);
            }
            else {
                /* 收费金额大于付费金额时,则收费 */
                eFYSFBZ = E_CGPYRV.RECIVE;
                bigHJYSJE = bigSFYSJE.subtract(bigFFYFJE);
                bigHJJZJE = bigSFSSJE.subtract(bigFFSFJE);
                bigHJQFJE = bigSFQFJE.subtract(bigFFQFJE);
            }
        }
        else {
            if (CommUtil.compare(bigFFYFJE, bigSFYSJE) > 0) {
                /* 付费金额大于收费金额时,则收费 */
                eFYSFBZ = E_CGPYRV.PAY;

                bigHJYSJE = bigFFYFJE.subtract(bigSFYSJE);
                bigHJJZJE = bigFFSFJE.subtract(bigSFSSJE);
                bigHJQFJE = bigFFQFJE.subtract(bigSFQFJE);
            }
            else {
                eFYSFBZ = E_CGPYRV.RECIVE;
                bigHJYSJE = bigSFYSJE.subtract(bigFFYFJE);
                bigHJJZJE = bigSFSSJE.subtract(bigFFSFJE);
                bigHJQFJE = bigSFQFJE.subtract(bigFFQFJE);
            }
        }

        bizlog.debug("收付标志[%s] 应收金额[%f] 记账金额[%f]欠费[%f]", eFYSFBZ, bigHJYSJE, bigHJJZJE, bigHJQFJE);

        cplChFeeOut.setRecvam(bigSFYSJE); //应收费用 
        cplChFeeOut.setSpcgam(bigFFYFJE); //应付费用
        cplChFeeOut.setAcclam(bigSFSSJE); //实收费用
        cplChFeeOut.setApcgam(bigFFSFJE); //实付费用
        cplChFeeOut.setArrgam(bigHJQFJE); //欠费金额 
        cplChFeeOut.setTotamt(bigHJJZJE); //合计金额

        if (CommUtil.isNotNull(cplChFeeIn.getDecuac())) {
            cplChFeeOut.setCustac(cplChFeeIn.getDecuac());
            cplChFeeOut.setCuscnm(sZHUZWM_SF);
        }
        cplChFeeOut.setChrgcy(cplChFeeIn.getTrancy());

        cplChFeeOut.setCgpyrv(eFYSFBZ);

        bizlog.parm("cplChFeeOut[%s]", cplChFeeOut);
        bizlog.method("calChargeFee end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        return cplChFeeOut;

    }    

    /**
     * @Author chenlk
     *         <p>
     *         <li>功能说明：销记收费登记簿</li>
     *         </p>
     * @param cplChFeeIn
     * @return CgChargeFee_OUT
     */
    public static void  chargeFeeOff(CgChargeFee_IN cplChFeeIn) {

        bizlog.method("calChargeFee begin >>>>>>>>>>>>>>>>>>>>>>");
        bizlog.parm("cplChFeeIn[%s]", cplChFeeIn);

        String sKEHHAO_SF = "", sKEHUZH_SF = "", sSHUNXH_SF = "" ;
        String eHUOBDH_SF = null, eHUOBDH_SY = null;
        E_CSEXTG eCHUIBZ_SF = null;
        E_ACTTYP eKHZHLX_SF = null;
        String sZHANGH_SF = "",  sZHUZWM_SF = "";
        BigDecimal bigKYNGJE = BigDecimal.ZERO ;



        if (CommUtil.isNull(cplChFeeIn.getTrandt())) {
            throw FeError.Chrg.BNASF363();
        }

        if (CommUtil.isNull(cplChFeeIn.getTrnseq())) {
            throw FeError.Chrg.BNASF364();
        }

        if (CommUtil.isNull(cplChFeeIn.getTrancy())) {
            throw FeError.Chrg.BNASF156();
        }

        if (cplChFeeIn.getCstrfg() != E_CSTRFG.CASH && cplChFeeIn.getCstrfg() != E_CSTRFG.TRNSFER) {
            throw FeError.Chrg.BNASF365();
        }

        if (cplChFeeIn.getCstrfg() == E_CSTRFG.CASH && cplChFeeIn.getOprflg() == E_OPRFLG.BSEQNO) {
            throw FeError.Chrg.BNASF366();
        }

        String eSFBIZH = cplChFeeIn.getTrancy();

        if (CommUtil.isNotNull(cplChFeeIn.getDecuac()) && cplChFeeIn.getCstrfg() == E_CSTRFG.TRNSFER) {

            if (cplChFeeIn.getOprflg() == E_OPRFLG.BSEQNO || cplChFeeIn.getOprflg() == E_OPRFLG.TOT) {
                throw FeError.Chrg.BNASF367();
            }

            //账户信息查询
            CgGetAccountInf_OUT cplOut_SF = SysUtil.getInstance(CgGetAccountInf_OUT.class); //查询账户信息输出
            CgGetAccountInf_IN cplIn = SysUtil.getInstance(CgGetAccountInf_IN.class); //查询账户信息输入

            cplIn.setAcctno(cplChFeeIn.getDecuac()); //账号
            cplIn.setActseq(cplChFeeIn.getSeqnum()); //子户号
            cplIn.setCrcycd(cplChFeeIn.getTrancy()); //币种
            cplIn.setCustno(cplChFeeIn.getCustno()); //客户号
            cplIn.setCsextg(cplChFeeIn.getCsexfg()); //钞汇标志
            
            cplOut_SF = ChargeProc.getAccountInf(cplIn); //查询账号信息
            if (CommUtil.isNull(cplOut_SF)) {
                throw FeError.Chrg.BNASF183();
            }

            sKEHHAO_SF = cplOut_SF.getCustno(); //客户号
            eKHZHLX_SF = cplOut_SF.getActtyp(); //账户类型
            sKEHUZH_SF = cplOut_SF.getCustac(); //客户账号
            eHUOBDH_SF = cplOut_SF.getCrcycd(); //币种
            sSHUNXH_SF = cplOut_SF.getActseq(); //子户号
            eCHUIBZ_SF = cplOut_SF.getCsextg(); //钞汇标志 
            sZHANGH_SF = cplOut_SF.getAcctno(); //系统账号
            sZHUZWM_SF = cplOut_SF.getAcctna(); //账户名称
            bigKYNGJE = cplOut_SF.getAdrbal();//可用余额

            if (CommUtil.isNotNull(cplChFeeIn.getTrancy()) && CommUtil.compare(cplChFeeIn.getTrancy(),eHUOBDH_SF) != 0) {
                throw PbError.Charg.E0002("扣费", eHUOBDH_SF, cplChFeeIn.getTrancy());
            }

            if (CommUtil.isNotNull(cplChFeeIn.getCsexfg()) && cplChFeeIn.getCsexfg() != eCHUIBZ_SF) {
                throw PbError.Charg.E0003("扣费", eCHUIBZ_SF, cplChFeeIn.getCsexfg());
            }

            if (CommUtil.isNotNull(cplChFeeIn.getSeqnum()) && CommUtil.compare(cplChFeeIn.getSeqnum(), sSHUNXH_SF) != 0) {
                throw PbError.Charg.E0004("扣费", sSHUNXH_SF, cplChFeeIn.getSeqnum());
            }

            if (CommUtil.isNotNull(cplChFeeIn.getCustno()) && CommUtil.isNotNull(sKEHHAO_SF) && CommUtil.compare(cplChFeeIn.getCustno(), sKEHHAO_SF) != 0) {
                throw PbError.Charg.E0005("扣费", sKEHHAO_SF, cplChFeeIn.getCustno());
            }

            if (cplOut_SF.getActtyp() == E_ACTTYP.DZZH && cplChFeeIn.getIfflag() == E_YES___.YES) {
                if (cplOut_SF.getPddpfg() == E_FCFLAG.FIX) {
                    throw PbError.Charg.E0006(sZHANGH_SF);// 扣费账号为定期账号,不允许直接从该账号收费
                }
            }

        }
        else {
            if (cplChFeeIn.getCstrfg() != E_CSTRFG.CASH && cplChFeeIn.getOprflg() != E_OPRFLG.BSEQNO && cplChFeeIn.getOprflg() != E_OPRFLG.TOT
                    && cplChFeeIn.getIfflag() != E_YES___.NO) {
                throw FeError.Chrg.BNASF368();
            }
        }

        if (CommUtil.isNotNull(cplChFeeIn.getCustno())) {
            sKEHHAO_SF = cplChFeeIn.getCustno(); //客户号
        }

        List<KcbChrgRgst> lstJfdjb = new ArrayList<KcbChrgRgst>();
  
        if (CommUtil.isNull(cplChFeeIn.getEvrgsq())) { //事件登记序号
            throw PbError.PbComm.E0136();
        }

        lstJfdjb = PBChargeRegisterDao.selChargeRegistrByTrsqRgsq( cplChFeeIn.getTrandt(), cplChFeeIn.getTrnseq(), cplChFeeIn.getEvrgsq(), true);
        
        bizlog.debug("指定流水号+序号收费lstJfdjb[%s]", lstJfdjb);
       


            BigDecimal bigYSFYJE = BigDecimal.ZERO; //应收费用金额
            BigDecimal bigSHSHJE = BigDecimal.ZERO; //实收费用金额
            BigDecimal bigSHSHJE_SY = BigDecimal.ZERO; //实收费用金额-损益
            BigDecimal bigSFFCJE = BigDecimal.ZERO; //收费分成金额
            BigDecimal bigQFEIJE = BigDecimal.ZERO; //欠费金额
            KcbChrgRgst tblBsfdj = SysUtil.getInstance(KcbChrgRgst.class);

            tblBsfdj = lstJfdjb.get(0);
            
            bizlog.debug("收费登记tblBsfdj[%s]", tblBsfdj);

            String sZHYODM, sZHYOMS, sBEIZXX;
            if (CommUtil.isNotNull(cplChFeeIn.getSmrycd())) {
                sZHYODM = cplChFeeIn.getSmrycd(); //摘要代码
                sZHYOMS = cplChFeeIn.getSmryds(); //摘要描述
            }
            else {
                sZHYODM = tblBsfdj.getSmrycd();
                sZHYOMS = tblBsfdj.getSmryds();
            }

            if (CommUtil.isNotNull(cplChFeeIn.getRemark())) {
                sBEIZXX = cplChFeeIn.getRemark(); //备注信息
            }
            else {
                sBEIZXX = tblBsfdj.getRemark();
            }

            if (cplChFeeIn.getOprflg() == E_OPRFLG.SEQNO) {
                if (CommUtil.compare(cplChFeeIn.getAcclam(), tblBsfdj.getArrgam()) > 0) {
                    String sErr = String.format("实收金额[%f]不允许大于欠费金额[%f]", cplChFeeIn.getAcclam(), tblBsfdj.getArrgam());
                    throw FeError.Chrg.BNASF999(sErr);
                }

                bigYSFYJE = tblBsfdj.getArrgam(); //应收=欠费
                bigSHSHJE = cplChFeeIn.getAcclam(); //实收
                bigSFFCJE = cplChFeeIn.getDvidam(); //收费分成

            }
            if (CommUtil.compare(tblBsfdj.getChrgcy(),eSFBIZH) !=0 ) {
                throw PbError.Charg.E0007(tblBsfdj.getChrgcy(), eSFBIZH);
            }



            if (cplChFeeIn.getCstrfg() == E_CSTRFG.TRNSFER && cplChFeeIn.getIfflag() == E_YES___.YES) {
                if (CommUtil.isNull(sKEHUZH_SF)) {
                    throw PbError.PbComm.E0126();
                }
            }

            //  金额处理
            bigYSFYJE = BusiTools.roundIncinByCurrency(cplChFeeIn.getTrancy(), bigYSFYJE,null);
            bigSHSHJE = BusiTools.roundIncinByCurrency(cplChFeeIn.getTrancy(), bigSHSHJE,null);
            bigSFFCJE = BusiTools.roundIncinByCurrency(cplChFeeIn.getTrancy(), bigSFFCJE,null);

            bizlog.debug("收费币种[%s] 现转标志[%s] 应收金额[%f] 实收金额[%f] 分成金额[%f]", eSFBIZH, cplChFeeIn.getCstrfg(), bigYSFYJE, bigSHSHJE, bigSFFCJE);

            if (CommUtil.compare(bigSHSHJE, BigDecimal.ZERO) > 0) {

                if (cplChFeeIn.getIfflag() == E_YES___.YES || cplChFeeIn.getOprflg() == E_OPRFLG.TOT) {

                    bizlog.debug("记现金账或客户账[%f]", bigSHSHJE);
                    if (cplChFeeIn.getCstrfg() == E_CSTRFG.CASH) {
                        bizlog.debug("记现金账[%f]", bigSHSHJE);
                        throw FeError.Chrg.BNASF324();

                    }
                    else {
                        bizlog.debug("记客户账[%s]", sKEHUZH_SF);
                        if (tblBsfdj.getCgpyrv() == E_CGPYRV.RECIVE) {
                        	//收费周期为零或者销记收费明细

                                // 实时,统一收费交易
                                bizlog.debug("实收金额bigSHSHJE[%s]", bigSHSHJE);
                                bizlog.debug("可用余额bigKYNGJE[%s]", bigKYNGJE);
                                if (CommUtil.compare(bigSHSHJE, BigDecimal.ZERO) > 0 && CommUtil.compare(bigKYNGJE, BigDecimal.ZERO) < 0
                                        || CommUtil.compare(bigSHSHJE, bigKYNGJE) > 0) {

                                       throw PbError.PbComm.E0036("", sKEHUZH_SF);                                   
                                }

                                if (cplChFeeIn.getOprflg() == E_OPRFLG.SEQ) {
                                    bigKYNGJE = bigKYNGJE.subtract(bigSHSHJE);
                                }

                            

                            if (CommUtil.compare(bigSHSHJE, BigDecimal.ZERO) > 0) {
                                    //  记账处理
                                    CgChargefeeKhz_IN cplKhz = SysUtil.getInstance(CgChargefeeKhz_IN.class);
                                    cplKhz.setCstrfg(cplChFeeIn.getCstrfg());
                                    cplKhz.setOprflg(cplChFeeIn.getOprflg());
                                    cplKhz.setCgpyrv(tblBsfdj.getCgpyrv());
                                    cplKhz.setCustno(sKEHHAO_SF);
                                    cplKhz.setActtyp(eKHZHLX_SF);
                                    cplKhz.setCustac(sKEHUZH_SF);
                                    cplKhz.setTrancy(eSFBIZH);
                                    cplKhz.setCsexfg(eCHUIBZ_SF);
                                    cplKhz.setActseq(sSHUNXH_SF);
                                    cplKhz.setSysacn(sZHANGH_SF);
                                    cplKhz.setAcclam(bigSHSHJE);
                                    cplKhz.setCsprcd(cplChFeeIn.getCsprcd());
                                    cplKhz.setSmrycd(sZHYODM);
                                    cplKhz.setSmryds(sZHYOMS);
                                    cplKhz.setRemark(sBEIZXX);
                                    cplKhz.setProdcd(tblBsfdj.getProdcd());
                                    cplKhz.setPayaDetail(cplChFeeIn.getPayaDetail());
                                    cplKhz.setPaydDetail(cplChFeeIn.getPaydDetail());    
                                    cplKhz.setStrktg(cplChFeeIn.getStrktg());
                                    calChargefeeKhz(cplKhz);

                                
                            }

                        }

                        else if (tblBsfdj.getCgpyrv() == E_CGPYRV.PAY) /** 付费 客户账存入 */
                        {

                                //  记账处理                               
                                CgChargefeeKhz_IN cplKhz = SysUtil.getInstance(CgChargefeeKhz_IN.class);
                                cplKhz.setCstrfg(cplChFeeIn.getCstrfg());
                                cplKhz.setOprflg(cplChFeeIn.getOprflg());
                                cplKhz.setCgpyrv(tblBsfdj.getCgpyrv());
                                cplKhz.setCustno(sKEHHAO_SF);
                                cplKhz.setActtyp(eKHZHLX_SF);
                                cplKhz.setCustac(sKEHUZH_SF);
                                cplKhz.setTrancy(eSFBIZH);
                                cplKhz.setCsexfg(eCHUIBZ_SF);
                                cplKhz.setActseq(sSHUNXH_SF);
                                cplKhz.setSysacn(sZHANGH_SF);
                                cplKhz.setAcclam(bigSHSHJE);
                                cplKhz.setCsprcd(cplChFeeIn.getCsprcd());
                                cplKhz.setSmrycd(sZHYODM);
                                cplKhz.setSmryds(sZHYOMS);
                                cplKhz.setRemark(sBEIZXX);
                                cplKhz.setProdcd(tblBsfdj.getProdcd());
                                cplKhz.setPayaDetail(cplChFeeIn.getPayaDetail());
                                cplKhz.setPaydDetail(cplChFeeIn.getPaydDetail());                                
                                calChargefeeKhz(cplKhz);


                        }
                    }
                }

                bizlog.debug("记损益账:实收金额bigSHSHJE[%f]", bigSHSHJE);
                /* 记损益账 */
                if (CommUtil.compare(bigSHSHJE, BigDecimal.ZERO) > 0) {


                        bigSHSHJE_SY = bigSHSHJE; //暂不考虑货币兑换，固定赋值
                        eHUOBDH_SY = tblBsfdj.getClchcy();

                        CgDanbJzFee_IN cplDbFee_In = SysUtil.getInstance(CgDanbJzFee_IN.class);
                        cplDbFee_In.setCgpyrv(tblBsfdj.getCgpyrv());
                        cplDbFee_In.setAcctbr(tblBsfdj.getAcctbr());
                        cplDbFee_In.setChrgcd(tblBsfdj.getChrgcd());
                        cplDbFee_In.setCgfacd(tblBsfdj.getCgfacd());
                        cplDbFee_In.setCghacd(tblBsfdj.getCghacd());
                        cplDbFee_In.setTrancy(tblBsfdj.getTrancy());
                        cplDbFee_In.setTranam(bigSHSHJE_SY);
                        cplDbFee_In.setTrancy(eHUOBDH_SY);
                        cplDbFee_In.setCstrfg(tblBsfdj.getCstrfg());
                        cplDbFee_In.setCsexfg(eCHUIBZ_SF);
                        cplDbFee_In.setCustac(sKEHUZH_SF);
                        cplDbFee_In.setCustno(sKEHHAO_SF);
                        cplDbFee_In.setCuscnm(sZHUZWM_SF);
                        cplDbFee_In.setProdcd(tblBsfdj.getProdcd());
                        cplDbFee_In.setSmrycd(sZHYODM);
                        cplDbFee_In.setSmryds(sZHYOMS);
                        cplDbFee_In.setRemark(sBEIZXX);
                        cplDbFee_In.setTrinfo(tblBsfdj.getTrinfo());//交易信息
                        calChrgfeeAcsq(cplDbFee_In);
                                            
                    bizlog.debug("费用记账结束");

                }
                else {
                    bizlog.debug("实收金额为零 不记费用或费用挂账账");
                }

            }
            else {
                bizlog.debug("实收金额为零");
            }

            tblBsfdj.setAcclam(tblBsfdj.getAcclam().add(bigSHSHJE));/* 实收金额 */
            tblBsfdj.setArrgam(bigQFEIJE); //欠费金额
            tblBsfdj.setRecvfg(E_GTAMFG.YES); /* 收讫标志 */
            tblBsfdj.setCgstno(tblBsfdj.getCgstno() + 1); /*收费明细序号*/
            tblBsfdj.setRemark(cplChFeeIn.getRemark());
            try {
                KcbChrgRgstDao.updateOne_odb1(tblBsfdj);
            } catch (Exception e) {
                throw FeError.Chrg.BNASF999("更新收费登记簿失败", e);
            }
            
            //add 20170223 songlw 集中收费新增收费明细
            List<KcbChrgDetl> lstChrgDetl = PBChargeRegisterDao.selKcbChrgDetlByTrdtTrsq(cplChFeeIn.getTrandt(), cplChFeeIn.getTrnseq(), cplChFeeIn.getEvrgsq(), false);

            if(CommUtil.isNull(lstChrgDetl)){
            	
            	bizlog.debug("费用记账明细实收金额tblBfymx:bigSHSHJE[%f]", bigSHSHJE);
                if (CommUtil.compare(bigSHSHJE, BigDecimal.ZERO) > 0) {
                	KcbChrgDetl tblBfymx = SysUtil.getInstance(KcbChrgDetl.class);
                    tblBfymx.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
                    tblBfymx.setTrnseq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
                    //tblBfymx.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());
                    tblBfymx.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());
                    tblBfymx.setTrnchl(CommTools.getBaseRunEnvs().getChannel_id());
                    tblBfymx.setCaldat(tblBsfdj.getTrandt());/* 计费发生日期 */
                    tblBfymx.setCalseq(tblBsfdj.getTrnseq());/* 计费柜员流水 */
                    tblBfymx.setEvrgsq(tblBsfdj.getEvrgsq());
                    tblBfymx.setCgstno(tblBsfdj.getCgstno());
                    tblBfymx.setChrgcd(tblBsfdj.getChrgcd());
                    tblBfymx.setChrgna(tblBsfdj.getChrgna());
                    tblBfymx.setCgpyrv(tblBsfdj.getCgpyrv());
                    tblBfymx.setClchcy(tblBsfdj.getClchcy());
                    tblBfymx.setClcham(tblBsfdj.getClcham());
                    tblBfymx.setDisrat(tblBsfdj.getDisrat());
                    tblBfymx.setDircam(tblBsfdj.getDircam());
                    tblBfymx.setChrgcy(eSFBIZH);
                    tblBfymx.setRecvam(bigYSFYJE);
                    tblBfymx.setAcclam(bigSHSHJE);
                    tblBfymx.setArrgam(bigQFEIJE);
                    tblBfymx.setReduam(bigYSFYJE.subtract(bigSHSHJE).subtract(bigQFEIJE));
                    tblBfymx.setDvidam(bigSFFCJE);
                    tblBfymx.setCstrfg(cplChFeeIn.getCstrfg());
                    tblBfymx.setAcctbr(tblBsfdj.getAcctbr());
                    tblBfymx.setCgfacd(tblBsfdj.getCgfacd());
                    tblBfymx.setCghacd(tblBsfdj.getCghacd());
                    tblBfymx.setLdseno(tblBsfdj.getLdseno());
                    tblBfymx.setCustno(sKEHHAO_SF);
                    tblBfymx.setActtyp(eKHZHLX_SF);
                    tblBfymx.setChacno(sKEHUZH_SF);
                    tblBfymx.setCrcycd(eHUOBDH_SF);
                    tblBfymx.setCsexfg(eCHUIBZ_SF);
                    long lSHUNXH_SF = 0;
                    if (CommUtil.isNotNull(sSHUNXH_SF)) {
                        lSHUNXH_SF = Long.parseLong(sSHUNXH_SF);
                    }
                    tblBfymx.setSequno(lSHUNXH_SF);
                    tblBfymx.setChrgac(sZHANGH_SF);
                    tblBfymx.setAcchnm(sZHUZWM_SF);
                    tblBfymx.setProdcd(tblBsfdj.getProdcd());
                    tblBfymx.setBlpdno(tblBsfdj.getBlpdno());
                    tblBfymx.setSmrycd(sZHYODM);
                    tblBfymx.setSmryds(sZHYOMS);
                    tblBfymx.setPrtrdt(tblBsfdj.getOrbsda());
                    tblBfymx.setPrtrsq(tblBsfdj.getOrbssn());
                    tblBfymx.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());
                    //tblBfymx.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());
                    tblBfymx.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());
                    tblBfymx.setTrntim(Long.valueOf(DateTools.getCurrentTime()));
                    //tblBfymx.setAuttel(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus());
                    tblBfymx.setAuttel(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus());
                    tblBfymx.setRuisma(E_RUISMA.WG);
                    tblBfymx.setWaisma(E_WAISMA.NORMAL);
                    tblBfymx.setFepecd(tblBsfdj.getFepecd());
                    tblBfymx.setFepefg(tblBsfdj.getFepefg());
                    tblBfymx.setFepamt(tblBsfdj.getFepamt());
                    tblBfymx.setFebaln(tblBsfdj.getFebaln());
                    try {
                        KcbChrgDetlDao.insert(tblBfymx);
                    } catch (Exception e) {
                        throw FeError.Chrg.BNASF999("新增收费记账明细记录失败", e);
                    }
                }
            }
            
            IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
            cplInput.setTranev(ApUtil.TRANS_EVENT_CHARGE);  //PB01-收费登记
            cplInput.setTranam(bigSHSHJE);// 交易金额
            cplInput.setCrcycd(tblBsfdj.getTrancy());// 货币代号
            cplInput.setEvent1(cplChFeeIn.getTrandt());  //事件关键字1 = 原收费交易
            cplInput.setEvent2(cplChFeeIn.getTrnseq());  //事件关键字2 = 收费流水
            cplInput.setEvent3(cplChFeeIn.getEvrgsq().toString());  //事件关键字3 = 事件登记序号


            //ApStrike.regBook(cplInput);    
    		IoMsRegEvent input = SysUtil.getInstance(IoMsRegEvent.class);    		
    		input.setReversal_event_id(ApUtil.TRANS_EVENT_CHARGE);
    		input.setInformation_value(SysUtil.serialize(cplInput));
    		MsEvent.register(input, true);
        }    
    
    
}