package cn.sunline.ltts.busi.cg.charg;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.errors.PbError;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BRCHTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
//import cn.sunline.ltts.busi.aplt.tables.SysDbTable.kmip_zhyocs;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class PbChargCalPublic {
    
    private static final BizLog bizlog = BizLogUtil.getBizLog(PbChargCalPublic.class);
    

    /**
     * 收费分成
     * 
     * @Author Wuxq
     *         <p>
     *         <li>2015年10月21日</li>
     *         <li>功能说明：收费分成</li>
     *         </p>
     * @param CgDivideFee_IN
     * @return CgDivideFee_OUT
     */
    /*    public static CgDivideFee_OUT prcDivideFee(CgDivideFee_IN cplDvidFeeIn) {

        bizlog.method("prcDivideFee begin >>>>>>>>>>>>>>>>>>>>>>");
        bizlog.parm("cplDvidFeeIn[%s]", cplDvidFeeIn);

        //收费分成输出
        CgDivideFee_OUT cplDvidFeeOut = SysUtil.getInstance(CgDivideFee_OUT.class);

        //交易金额必须大于0
        if (CommUtil.compare(cplDvidFeeIn.getTranam(), BigDecimal.ZERO) <= 0) {
            bizlog.debug("DvidFeeIn.Tranam=%s为零,不记账",cplDvidFeeIn.getTranam());
            return null;
        }

        //记账金额
        BigDecimal bigJiZhJe = CommTools.roundByCrcy(cplDvidFeeIn.getCrcycd(), cplDvidFeeIn.getTranam());
        bizlog.debug("cplDvidFeeIn.getCrcycd()[%s]记账金额[%f]", cplDvidFeeIn.getCrcycd(), bigJiZhJe);

        if (CommUtil.compare(cplDvidFeeIn.getTranam(), bigJiZhJe) != 0) {
             金额[%f]不满足币种[%s]最小记账单位要求的金额[%f] 
            throw PbError.PbComm.E0213(cplDvidFeeIn.getTranam(), cplDvidFeeIn.getCrcycd(), bigJiZhJe);
        }

         根据摘要代码获取摘要描述 
        if (CommUtil.isNotNull(cplDvidFeeIn.getSmrycd()) && CommUtil.isNull(cplDvidFeeIn.getSmryds())) {
            kmip_zhyocs tblKmip_zhyocs = BusiTools.getSummaryInfo(cplDvidFeeIn.getSmrycd());
            if (CommUtil.isNotNull(tblKmip_zhyocs)) {
                String sZhaiyaoms = tblKmip_zhyocs.getZhaiyoms();
                cplDvidFeeIn.setSmryds(sZhaiyaoms);
            }
        }
        
        
         * ================================================
         * =jzjigo 不为空，则收的费入该机构，否则入交易机构 =
         * =================================================
         
        String sJzjigo; //记账机构
        if (CommUtil.isNull(cplDvidFeeIn.getBrchno())) {
            sJzjigo = CommTools.getBaseRunEnvs().getTrxn_branch(); //记账机构=交易机构
        }
        else {
            sJzjigo = cplDvidFeeIn.getBrchno();  //记账机构=账务机构
        }
        bizlog.debug(">>>sJzjigo[%s]", sJzjigo);
        String sJzjigo_Zw = null;
        sJzjigo_Zw = getJgzwjg(sJzjigo); //获取记账机构对应的账务机构
        bizlog.debug(">>>费用记账机构:sJzjigo_Zw[%s]", sJzjigo_Zw);

        String sGzjigo = null; //挂账机构
        if (CommUtil.isNull(cplDvidFeeIn.getHangbr())) {
            sGzjigo = sJzjigo; //挂账机构=记账机构
        }
        else {
            sGzjigo = cplDvidFeeIn.getHangbr(); //挂账机构=挂账机构
        }
        bizlog.debug(">>>sGzjigo[%s]", sGzjigo);
        String sGzjigo_Zw = null;
        sGzjigo_Zw = getJgzwjg(sGzjigo); //获取挂账机构对应的账务机构
        bizlog.debug(">>>挂账记账机构:sGzjigo_Zw[%s]", sGzjigo_Zw);

        String sEfctdt;  //生效日期
        if (CommUtil.isNull(cplDvidFeeIn.getEfctdt())) {
            sEfctdt = CommTools.getBaseRunEnvs().getJiaoyirq(); //生效日期=交易日期
        }
        else {
            sEfctdt = cplDvidFeeIn.getEfctdt();  //生效日期=生效日期 
        } 
        bizlog.debug(">>>sEfctdt[%s]", sEfctdt);

        if (CommUtil.isNull(cplDvidFeeIn.getCrcycd())) {
            throw PbError.PbComm.E0214("货币代号");
        }

        if (CommUtil.isNull(cplDvidFeeIn.getChrgcd())) {
            throw PbError.PbComm.E0214("收费代码");
        }

        if (CommUtil.isNull(cplDvidFeeIn.getCgpyrv())) {
            throw PbError.PbComm.E0214("费用收付标志");
        }
         根据费率种类获取记账科目 
        kcp_chrg tblChrg = Kcp_chrgDao.selectOne_odb1(cplDvidFeeIn.getChrgcd(), cplDvidFeeIn.getCrcycd(), false);
        if(CommUtil.isNull(tblChrg)){
            //收费代码+币种 未定义有效的费用信息
            throw PbError.PbComm.E0057(cplDvidFeeIn.getChrgcd(), cplDvidFeeIn.getCrcycd());
        }
        if (CommUtil.compare(tblChrg.getEfctdt(), sEfctdt) > 0) {
            //收费代码+币种 未定义有效的费用信息
            throw PbError.PbComm.E0057(cplDvidFeeIn.getChrgcd(), cplDvidFeeIn.getCrcycd());
        }
        bizlog.debug(">>>>>>>>tblChrg[%s]", tblChrg);

        if (CommUtil.isNull(tblChrg.getCgfacd())) { //收费核算业务编号
            throw PbError.PbComm.E0123(tblChrg.getCgfacd());
        }

        String sCghacd = tblChrg.getCghacd();  //挂账业务编号
        if (CommUtil.isNull(tblChrg.getCghacd())) {
            if (CommUtil.isNotNull(cplDvidFeeIn.getCghacd())) {
                sCghacd = cplDvidFeeIn.getCghacd(); 
            }
            else {
                //挂账业务编号非法
                throw PbError.PbComm.E0118(sCghacd);
            }
        }
        bizlog.debug("*记费用暂收账户>>sCghacd[%s]sgzjigo[%s]huobdh[%s]>>>>>>>\n", sCghacd, sGzjigo_Zw, cplDvidFeeIn.getCrcycd());
        //内部账记帐
        IoInAccount InAccounting = SysUtil.getInstance(IoInAccount.class);
        IaAcdrInfo InAccount_in = SysUtil.getInstance(IaAcdrInfo.class); //内部户交易输入
        IaTransOutPro InAccount_out = SysUtil.getInstance(IaTransOutPro.class); //内部户交易输出
        
        if (cplDvidFeeIn.getCgpyrv() == E_CGPYRV.RECIVE){ //收费
            //记借方
            InAccount_in.setBusino(sCghacd); //挂账业务编码
            InAccount_in.setAcbrch(sGzjigo_Zw); //账务机构=挂账机构
            //InAccount_in.setInptsr(E_INPTSR.); //交易来源类别
            InAccount_in.setCrcycd(cplDvidFeeIn.getCrcycd()); //币种
            //InAccount_in.setSmrycd(cplDvidFeeIn.getSmrycd()); //摘要码 XZ-收费
            InAccount_in.setToacct(tblChrg.getCgfacd()); //对方账号
            //InAccount_in.setToacna(cplDvidFeeIn.getSmryds()); //对方户名
            InAccount_in.setTranam(cplDvidFeeIn.getTranam()); //交易金额
            InAccount_out = InAccounting.ioInAcdr(InAccount_in);            
        }else if(cplDvidFeeIn.getCgpyrv() == E_CGPYRV.PAY){ //付费
            //记贷方
            InAccount_in.setBusino(sCghacd); //挂账业务编码
            InAccount_in.setAcbrch(sGzjigo_Zw); //账务机构=挂账机构
            //InAccount_in.setInptsr(E_INPTSR.); //交易来源类别
            InAccount_in.setCrcycd(cplDvidFeeIn.getCrcycd()); //币种
            //InAccount_in.setSmrycd(cplDvidFeeIn.getSmrycd()); //摘要码 XZ-收费
            InAccount_in.setToacct(tblChrg.getCgfacd()); //对方账号
            //InAccount_in.setToacna(cplDvidFeeIn.getSmryds()); //对方户名
            InAccount_in.setTranam(cplDvidFeeIn.getTranam()); //交易金额
            InAccount_out = InAccounting.ioInAccr(InAccount_in);            
        }
        
         转入费用总账 
        bizlog.debug("%s() begin :>>>>>>>>>>");
        CgDanbJzFee_IN cplDbFeeIn = SysUtil.getInstance(CgDanbJzFee_IN.class);
        cplDbFeeIn.setCgpyrv(cplDvidFeeIn.getCgpyrv());  //费用收付标志 
        cplDbFeeIn.setAcctbr(sJzjigo_Zw);  //记账机构
        cplDbFeeIn.setChrgcd(cplDvidFeeIn.getChrgcd()); //收费代码
        cplDbFeeIn.setCgfacd(tblChrg.getCgfacd()); //收费核算业务编号
        cplDbFeeIn.setTranam(cplDvidFeeIn.getTranam()); //交易金额
        cplDbFeeIn.setTrancy(cplDvidFeeIn.getCrcycd());  //交易币种
        cplDbFeeIn.setCdtrfg(E_CSTRFG.TRNSFER);  //转账
        cplDbFeeIn.setProdcd(cplDvidFeeIn.getProdcd()); //产品号
        cplDbFeeIn.setSmrycd(cplDvidFeeIn.getSmrycd()); //摘要代码
        cplDbFeeIn.setSmryds(cplDvidFeeIn.getSmryds()); //摘要描述
        cplDbFeeIn.setRemark(cplDvidFeeIn.getRemark());  //备注
        //单边记费用科目账
        ChargeProc.calDanbJzfee(cplDbFeeIn);  

        if(InAccount_out.getBlncdn() == E_BLNCDN.C){
            //余额方向=贷方
            cplDvidFeeOut.setAcctbl(InAccount_out.getCrctbl()); //账户余额 =贷方余额       
        }else if (InAccount_out.getBlncdn() == E_BLNCDN.D){
            //余额方向=借方
            cplDvidFeeOut.setAcctbl(InAccount_out.getDrctbl()); //账户余额 =借方余额    
        }
        cplDvidFeeOut.setHangbr(sGzjigo_Zw); //挂账机构
        cplDvidFeeOut.setCghacd(sCghacd); //收费挂账业务编号
        cplDvidFeeOut.setChrgcd(cplDvidFeeIn.getChrgcd());  //收费代码
        cplDvidFeeOut.setChrgna(tblChrg.getChrgna());   //收费代码名称
        cplDvidFeeOut.setBrchno(sJzjigo_Zw); //账务机构
        cplDvidFeeOut.setCgfacd(tblChrg.getCgfacd()); //收费核算业务编号

        bizlog.parm("cplDvidFeeOut[%s]", cplDvidFeeOut);
        bizlog.method("prcDivideFee end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        return cplDvidFeeOut;
    }*/    

    /**
     * 根据机构号获取账务机构
     * 
     * @Author Wuxq
     *         <p>
     *         <li>2015年10月21日</li>
     *         <li>功能说明：根据机构号获取账务机构</li>
     *         </p>
     * @param sYNGYJG
     * @return
     */
    public static String getJgzwjg(String sYNGYJG) {
        bizlog.method("getJgzwjg begin >>>>>>>>>>>>>>>>>>>>>>");
        bizlog.parm("sYNGYJG[%s]", sYNGYJG);

        String sZHNGJG = "";
        /* 查询机构信息*/
        IoSrvPbBranch BrchQry = SysUtil.getInstance(IoSrvPbBranch.class);
        IoBrchInfo BrchInfo = SysUtil.getInstance(IoBrchInfo.class);
        BrchInfo = BrchQry.getBranch(sYNGYJG);
        if(CommUtil.isNull(BrchInfo)){
            throw PbError.PbComm.E0003(sYNGYJG);  //机构号不存在
        }
        
        bizlog.debug("Jigolx[%s]", BrchInfo.getBrchtp()); 
        sZHNGJG = sYNGYJG;
        /*if (E_BRCHTP.ACCT == BrchInfo.getBrchtp()) { //2-账务机构
            sZHNGJG = sYNGYJG;
        }
        else if (E_BRCHTP.BUSI == BrchInfo.getBrchtp()) { //1-营业机构
            //TODO 获取业务关系机构
            sZHNGJG = sYNGYJG;  //暂时认为机构都可以作为账务机构
        }
        else {
            //TODO  sZHNGJG = "";
            sZHNGJG = sYNGYJG; //暂时认为机构都可以作为账务机构
        }*/

        if (CommUtil.isNotNull(sZHNGJG) && CommUtil.compare(sZHNGJG, sYNGYJG) != 0) {
            BrchInfo = BrchQry.getBranch(sZHNGJG);
            if(CommUtil.isNull(BrchInfo)){
                throw PbError.PbComm.E0003(sZHNGJG);  //机构号不存在
            }
        }
        
        bizlog.parm("sZHNGJG[%s]", sZHNGJG);
        bizlog.method("getJgzwjg end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        return sZHNGJG;
    }    
    
      
    
}
