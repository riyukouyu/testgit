package cn.sunline.ltts.busi.fe.chrg;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.jfpal.ngp.aplt.parm.TrxEnvs;
import cn.sunline.edsp.busi.jfpal.ngp.aplt.parm.TrxEnvs.CjSfsv;
import cn.sunline.edsp.busi.jfpal.ngp.aplt.parm.TrxEnvs.Feinfo;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgCalFee_IN;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgCalFee_OUT;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgFFEEHD;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgFrmFeeHD;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSTRFG;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_CGPYRV;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CONFLG;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

public class ChargPublic {

	private static final BizLog bizlog = BizLogUtil.getBizLog(ChargPublic.class);
	
	
	/**
     * 按场景收费
     * 
     * @Author wuxq
     *         <p>
     *         <li>2015年10月16日</li>
     *         <li>功能说明：按场景收费</li>
     *         </p>
     * @param cplFeeIn
     * @return 
     */
    public static CgFFEEHD calCjsf(CgCalFee_IN cplFeeIn) {

        bizlog.method("calCjsf begin >>>>>>>>>>>>>>>>>>>>>>");
        bizlog.parm("cplFeeIn[%s]", cplFeeIn);
        
        //输出收费打印信息
        CgFFEEHD cplOut = SysUtil.getInstance(CgFFEEHD.class);
        
        E_CGPYRV eCgpyrv = null ; //费用收付标志
        List<Feinfo> lstFyxx = BusiTools.getBusiRunEnvs().getChgevo().getFeelst(); //费用信息
        if(CommUtil.isNotNull(lstFyxx))
        {
        	TrxEnvs.Feinfo cplFeinfo = lstFyxx.get(0); 
            cplFeeIn.setCsexfg(cplFeinfo.getCsexfg()); //钞汇标志
            cplFeeIn.setCustac(cplFeinfo.getCustac()); //客户帐号
            cplFeeIn.setChrgcy(cplFeinfo.getChrgcy());  //收费币种
            cplFeeIn.setSeqnum( cplFeinfo.getSeqnum()); //顺序号
            cplFeeIn.setCstrfg(cplFeinfo.getCstrfg());  //现转标志
            cplFeeIn.setCsprcd(cplFeinfo.getCsprcd()); //现金项目代码
            cplFeeIn.setSpcham(cplFeinfo.getSpcham()); //指定收费金额
            bizlog.debug("cplFeinfo[%s]",cplFeinfo);
        }       

        E_CONFLG eConflg = BusiTools.getBusiRunEnvs().getChgevo().getConflg(); //费用确认标志
        bizlog.debug("费用确认标志[%s]", eConflg);        
        Options<CjSfsv> lstCjsf = BusiTools.getBusiRunEnvs().getChgevo().getCjsfls();
        BigDecimal bigYSFYZE = BigDecimal.ZERO;/* 应收费用总额 */
        BigDecimal bigSSFYZE = BigDecimal.ZERO;/* 实收费用总额 */
        BigDecimal bigYFFYZE = BigDecimal.ZERO;/* 应付费用总额 */
        BigDecimal bigSFFYZE = BigDecimal.ZERO;/* 实付费用总额 */
        BigDecimal bigHEJSXF = BigDecimal.ZERO;/* 合计总额 */
        Options<CjSfsv> lstSfsvout = new DefaultOptions<CjSfsv>();       
        
        if (CommUtil.isNull(lstCjsf) ||  lstCjsf.size() == 0) {
            bizlog.debug("收费场景列表为空标志不收费");
            return null;
        }

        E_CHGFLG eChgflg = null;
        if (eConflg == E_CONFLG.CAL)// 0-试算
        {
            eChgflg = E_CHGFLG.NONE;
            if (CommUtil.isNull(cplFeeIn.getCstrfg())) {
                //cplFeeIn.setXianzhbz(E_CSTRFG.XJ); //默认为现金？
                cplFeeIn.setCstrfg(E_CSTRFG.TRNSFER); //默认为 1-转帐
            }
        }
        else if (eConflg == E_CONFLG.ONE)// 1-收费，只记费用账
        {
            eChgflg = E_CHGFLG.ONE;
        }
        else if (eConflg == E_CONFLG.ALL)// 2-收费，记双边账
        {
            eChgflg = E_CHGFLG.ALL;
        }
        else if (eConflg == E_CONFLG.NSNS)// 9-不收费,也不试算
        {
            eChgflg = E_CHGFLG.NSNS;
            bizlog.debug("收费标志位【%s】表示既不收费也不试算", eConflg);
            return null;
        }else if(eConflg == E_CONFLG.XSHS) // 8-试算后有收费       
        {
            //防止前台授权时将返回的8再次送过来
            eChgflg = E_CHGFLG.NONE;
        }
        bizlog.debug("记账标志eChgflg[%s]", eChgflg);
        cplFeeIn.setChgflg(eChgflg);
       
        if(CommUtil.isNull(cplFeeIn.getTrancy())){
            cplFeeIn.setTrancy(BusiTools.getDefineCurrency()); //为空默认取人民币
        }
        
        bizlog.debug("cplFeeIn.getChgflg()[%s]", cplFeeIn.getChgflg()); //记账标志
        bizlog.debug("cplFeeIn.getChrgcy()[%s]", cplFeeIn.getChrgcy()); //收费币种
        if(CommUtil.isNull(cplFeeIn.getChrgcy()))
        {
            if (CommUtil.isNull(cplFeeIn.getChrgcy())&& E_CHGFLG.NONE == cplFeeIn.getChgflg()) {
                cplFeeIn.setChrgcy(BusiTools.getDefineCurrency());
            }else
            {
                throw FeError.Chrg.BNASF222();
            }
        }
        
        if (CommUtil.isNull(cplFeeIn.getCstrfg()) && E_CHGFLG.NONE != cplFeeIn.getChgflg()) {
            throw FeError.Chrg.BNASF273();
        }        
        
        if (CommUtil.isNotNull(cplFeeIn.getCstrfg())) {
            if (E_CSTRFG.TRNSFER == cplFeeIn.getCstrfg()) {
                if (CommUtil.isNull(cplFeeIn.getCustac())) {
                    //现转标志为转帐时，客户帐号不能为空
                    throw FeError.Chrg.BNASF360();
                }
            }
            else if (E_CSTRFG.CASH == cplFeeIn.getCstrfg() && 
                    (eConflg == E_CONFLG.ONE || eConflg == E_CONFLG.ALL)
                    && CommUtil.isNull(cplFeeIn.getCsprcd())) {
                //现转标志为现金，且费用记账时，现金项目代码不能为空
                throw FeError.Chrg.BNASF361();
            }

        }        

        bizlog.debug("收费场景列表个数lstCjsf[%s]", lstCjsf.size());

        for (CjSfsv cplSfsv : lstCjsf) {
            String sScencd = cplSfsv.getScencd(); //场景代码
            String sScends = cplSfsv.getScends(); //场景描述
            if (CommUtil.isNull(sScencd)) {
                bizlog.debug("场景代码[%s]不能为空", sScencd);
                return null;
            }
            bizlog.debug("场景代码sScencd[%s]", sScencd);
            cplFeeIn.setScencd(sScencd);
            CalCharg CalChargAPI = SysUtil.getInstance(CalCharg.class);
            bizlog.debug("收费输入cplFeeIn%s]", cplFeeIn);
            CgCalFee_OUT cplFee_OUT = CalChargAPI.calCharge(cplFeeIn); //统一计费/收费
            bizlog.debug("收费输出cplFee_OUT%s]", cplFee_OUT);
            if (CommUtil.isNull(cplFee_OUT)) {
                BusiTools.getBusiRunEnvs().getChgevo().setConflg(E_CONFLG.NSNS); //9-不试算 不收费
            }
            else {
                
                bizlog.debug("收费明细输出cplFee_OUT.getLstFeexx()[%s]",cplFee_OUT.getLstFeexx());
                if (CommUtil.isNotNull(cplFee_OUT.getLstFeexx())) 
                {
                    for(int i = 0; i < cplFee_OUT.getLstFeexx().size(); i++)
                    {
                        CjSfsv cplTmp = SysUtil.getInstance(CjSfsv.class);
                        cplTmp.setScencd(sScencd); //场景代码
                        cplTmp.setScends(sScends); //场景描述
//                        IoApProcCjDm cplAp = SysUtil.getInstance(IoApProcCjDm.class);
//                        IoApQryCjHzInfoOut tblTmp = cplAp.qryCjHzInfo(cplFeeIn.getScencd());
//                        
//                        if (CommUtil.isNotNull(tblTmp) && tblTmp.getJiluztai() == E_JILUZTAI.Delete) {
//                            throw CmError.Comm.E2015("场景代码在场景信息汇总表未配置,则不收费");
//                        }

//                        cplTmp.setChangjms(tblTmp.);
                        
                        cplTmp.setChrgcd(cplFee_OUT.getLstFeexx().get(i).getChrgcd());
                        cplTmp.setChrgna(cplFee_OUT.getLstFeexx().get(i).getChrgna());
                        cplTmp.setAcclam(cplFee_OUT.getLstFeexx().get(i).getAcclam());
                        cplTmp.setRecvam(cplFee_OUT.getLstFeexx().get(i).getRecvam());
                        cplTmp.setFavpec(cplFee_OUT.getLstFeexx().get(i).getFavpec().multiply(new BigDecimal("100")));
                        lstSfsvout.add(cplTmp);
                        BusiTools.getBusiRunEnvs().getChgevo().setCjsfls(lstSfsvout);  //场景收费列表信息
                    }
                    
                    bigYSFYZE = bigYSFYZE.add(cplFee_OUT.getRecvam()); //应收费用金额
                    bigSSFYZE = bigSSFYZE.add(cplFee_OUT.getAcclam()); //实收费用金额
                    bigYFFYZE = bigYFFYZE.add(cplFee_OUT.getSpcgam()); //应付费用金额
                    bigSFFYZE = bigSFFYZE.add(cplFee_OUT.getApcgam()); //实付费用金额
                
                }

            
            if (eChgflg == E_CHGFLG.NONE) {
    
                if (CommUtil.compare(bigYSFYZE, bigYFFYZE) >= 0) {
                    eCgpyrv = E_CGPYRV.RECIVE;  //收费
                    bigHEJSXF = bigYSFYZE.subtract(bigYFFYZE);
                }
                else {
                    eCgpyrv = E_CGPYRV.PAY; //付费
                    bigHEJSXF = bigYFFYZE.subtract(bigYSFYZE);
                }
            }
            else {
                if (CommUtil.compare(bigSSFYZE, bigSFFYZE) >= 0) {
                    eCgpyrv = E_CGPYRV.RECIVE;  //收费
                    bigHEJSXF = bigSSFYZE.subtract(bigSFFYZE);
                }
                else {
                    eCgpyrv = E_CGPYRV.PAY;  //付费
                    bigHEJSXF = bigSFFYZE.subtract(bigSSFYZE);
                }   
            }
            
            bizlog.debug("合计金额收费bigHEJSXF[%s]",bigHEJSXF);
    
            if (CommUtil.compare(bigHEJSXF, BigDecimal.ZERO) < 0) {
                bigHEJSXF = bigHEJSXF.multiply(new BigDecimal("-1"));
            }
            bizlog.debug("应收费用总额bigYSFYZE[%f]", bigYSFYZE);
            bizlog.debug("应付费用总额bigYFFYZE[%f]", bigYFFYZE);
            bizlog.debug("实收费用总额bigSFFYZE[%f]", bigSFFYZE);
            bizlog.debug("实付费用总额bigSSFYZE[%f]", bigSSFYZE);
            

            // 只有确认收费后才打印
//            if (eConflg == E_CONFLG.ONE|| eConflg == E_CONFLG.ALL) 
//            {
                if (CommUtil.isNotNull(cplFee_OUT) && CommUtil.isNotNull(cplFee_OUT.getLstFeexx())) {
                    for(int i = 0; i < cplFee_OUT.getLstFeexx().size(); i++)
                    {
                        CgFrmFeeHD cplFeeHD = SysUtil.getInstance(CgFrmFeeHD.class);
                        cplFeeHD.setChrgcd(cplFee_OUT.getLstFeexx().get(i).getChrgcd()); //收费代码
                        cplFeeHD.setChrgna(cplFee_OUT.getLstFeexx().get(i).getChrgna()); //收费代码名称
                        cplFeeHD.setFeeamt(cplFee_OUT.getLstFeexx().get(i).getAcclam()); //手续费金额=实收费用金额
                        cplFeeHD.setCgpyrv(cplFee_OUT.getLstFeexx().get(i).getCgpyrv()); //费用收付标志
                        cplFeeHD.setRecvfg(cplFee_OUT.getLstFeexx().get(i).getRecvfg()); //收讫标志
                        cplFeeHD.setArrgam(cplFee_OUT.getLstFeexx().get(i).getArrgam()); //欠费金额
                        cplOut.getLstFEEHD().add(cplFeeHD);
                    }
                }
//              }
            
            }
        }        
        
        BusiTools.getBusiRunEnvs().getChgevo().setSpcham(bigHEJSXF); //总金额
        // 正常都是0，在交易后，如果赋值为８，表示试算后有收费，需要回显前台
        if (eChgflg == E_CHGFLG.NONE && CommUtil.compare(bigHEJSXF, BigDecimal.ZERO) >= 0 && CommUtil.isNotNull(lstSfsvout)) {
            BusiTools.getBusiRunEnvs().getChgevo().setConflg(E_CONFLG.XSHS); //8-试算后有收费
            if(CommUtil.compare(cplFeeIn.getSpcham(), BigDecimal.ZERO) > 0)
            {
                //添加为结算那边的指定金额做特殊处理
                //BusiTools.getBusiRunEnvs().getChgevo().setSpcham(cplFeeIn.getSpcham());
                BusiTools.getBusiRunEnvs().getChgevo().setSpcham(cplFeeIn.getSpcham());
            }
        }
        else {
            BusiTools.getBusiRunEnvs().getChgevo().setConflg(E_CONFLG.NSNS); //9-不试算不收费
        }

        // 只有确认收费后才打印
//        if (eConflg == E_CONFLG.ONE|| eConflg == E_CONFLG.ALL) {
            cplOut.setTrnnam("统一收费");
            if (CommUtil.isNull(cplFeeIn.getCplKfzhu().getCustac())) {
                cplOut.setChacno(cplFeeIn.getCustac()); //收费客户帐号
                cplOut.setAcctno(""); //账号
                cplOut.setCuscnm(""); //客户中文名称
            }
            else {
                cplOut.setChacno(cplFeeIn.getCplKfzhu().getCustac()); //收费客户帐号
                cplOut.setAcctno(""); //账号
                cplOut.setCuscnm(""); //客户中文名称
            }

            cplOut.setChrgcy(cplFeeIn.getChrgcy()); //收费币种哦你
//            cplOut.setCgpyrv(eCgpyrv); //费用收付标志
            cplOut.setTotamt(bigHEJSXF); //合计金额
            cplOut.setCstrfg(cplFeeIn.getCstrfg()); //现转标志
            cplOut.setRemark(cplFeeIn.getRemark()); //备注信息
            if (CommUtil.isNotNull(cplFeeIn.getSmryds())) {  //摘要描述
                cplOut.setSmryds(cplFeeIn.getSmryds());
            }
            else if (eCgpyrv == E_CGPYRV.RECIVE) {
                cplOut.setSmryds("收费");
            }
            else if (eCgpyrv == E_CGPYRV.PAY) {
                cplOut.setSmryds("付费");
            }
//        }

        bizlog.debug("CommTools.getBaseRunEnvs().getConflg[%s]", BusiTools.getBusiRunEnvs().getChgevo().getConflg()); 
        bizlog.parm("cplOut[%s]", cplOut);
        bizlog.method("calCjsf end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");        
        
        return cplOut;
        
    }
	
}
