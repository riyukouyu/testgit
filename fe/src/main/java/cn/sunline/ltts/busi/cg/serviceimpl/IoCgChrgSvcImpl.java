package cn.sunline.ltts.busi.cg.serviceimpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.cg.charg.PbCalCharg;
import cn.sunline.ltts.busi.cg.namedsql.charg.PBChargeCodeDao;
import cn.sunline.ltts.busi.cg.namedsql.charg.PBChargeFavoDao;
import cn.sunline.ltts.busi.cg.namedsql.charg.PBChargeRegisterDao;
import cn.sunline.ltts.busi.cg.serviceimpl.charg.ChrgSvcType;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgRgst;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcpChrgAcrl;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcpChrgAcrlDao;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcpChrgAcrlHist;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcpChrgAcrlHistDao;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgCalFee_IN;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgCalFee_OUT;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgFEEINFO;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgKFZHINFO;
import cn.sunline.ltts.busi.iobus.servicetype.IoApAccount;
import cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoCgChrgAdjust.Input;
import cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoSelDistam.Output;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.type.cg.IoCgChrg.IoCgDisamtInfo;
import cn.sunline.ltts.busi.iobus.type.cg.IoCgChrg.IoCgQrcharInfo;
import cn.sunline.ltts.busi.iobus.type.cg.IoCgChrg.IoChrgAccRuleInfo;
import cn.sunline.ltts.busi.iobus.type.cg.IoCgChrg.disChargeInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalAcct_IN;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalFee_IN;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalFee_OUT;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgFEEINFO;
import cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctCustnaCardno;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_ACCTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSTRFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_GTAMFG;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_PREWAY;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ADJTTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BLLWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CGPYRV;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_CHGFLG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_PFRTIM;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_YES___;
 /**
  * 公共收费外部服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoCgChrgSvcImpl", longname="公共收费外部服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoCgChrgSvcImpl implements cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType{

    private static final BizLog bizlog = BizLogUtil.getBizLog(ChrgSvcType.class);

    /**  
     * @Author wuxq
     *         <p>
     *         <li>通用转帐收费（慎用）</li>
     *         </p>
     * @param
     * @return
     */
    public IoCgCalFee_OUT CalAccounting(IoCgCalAcct_IN cplIn) {

        IoCgCalFee_OUT cplOut = SysUtil.getInstance(IoCgCalFee_OUT.class);
        CgKFZHINFO cplKfzhInfo = SysUtil.getInstance(CgKFZHINFO.class);
        PbCalCharg CalChrg = SysUtil.getInstance(PbCalCharg.class);

        if (CommUtil.isNull(cplIn.getScencd()) && CommUtil.isNull(cplIn.getChrgcd())
                && CommUtil.isNull(cplIn.getChevno())) {
            throw FeError.Chrg.BNASF379();
        }

        //收费事件编号检查
/*        if (CommUtil.isNotNull(cplIn.getChevno())) {

            List<kcp_chrg_evnt> lstSfsj = new ArrayList<kcp_chrg_evnt>();
            lstSfsj = Kcp_chrg_evntDao.selectAll_odb2(cplIn.getChevno(), true); //查询事件收费控制表
            if (CommUtil.isNull(lstSfsj) && lstSfsj.size() == 0)
            {
                throw FeError.Chrg.E2015("收费事件未定义相关费用种类参数");
            }
        }*/

        E_CHGFLG eChgflg = cplIn.getChgflg();
        if (CommUtil.isNull(eChgflg)) {
            throw FeError.Chrg.BNASF380();
        }

        //是否汇总记帐标志
        //        E_YES___ eSHIFOUBZ = E_YES___.NO;
        //        if (CommUtil.isNotNull(cplIn.getTotflg())) {
        //            eSHIFOUBZ = cplIn.getTotflg();
        //        }

        bizlog.debug("eChgflg[%s]", eChgflg);

        //记账标志为记帐时相关检查
        if (E_CHGFLG.ONE == cplIn.getChgflg() || E_CHGFLG.ALL == cplIn.getChgflg()) {

            if (CommUtil.isNull(cplIn.getCustac()) && CommUtil.isNull(cplIn.getAccgac())) {
                throw FeError.Chrg.BNASF381();
            }

        }

        if (CommUtil.isNull(cplIn.getTrancy())) {
            throw FeError.Chrg.BNASF155();
        }

        if (CommUtil.isNull(cplIn.getChrgcy())) {
            throw FeError.Chrg.BNASF378();
        }

        //实际扣费账号不为空时,赋值扣费账号信息
        if (CommUtil.isNotNull(cplIn.getAccgac())) {

            if (CommUtil.compare(cplIn.getCustac(), cplIn.getAccgac()) != 0 || !CommUtil.equals(cplIn.getTrancy(), cplIn.getAccgcy())
                    || CommUtil.compare(cplIn.getSeqnum(), cplIn.getCgsbsq()) != 0 ) {
                cplKfzhInfo.setCustac(cplIn.getAccgac());
                cplKfzhInfo.setCrcycd(cplIn.getAccgcy());
                cplKfzhInfo.setCsexfg(null);
                cplKfzhInfo.setCgsbsq(cplIn.getCgsbsq());
            }
        }

        CgCalFee_IN cplFeeIn = SysUtil.getInstance(CgCalFee_IN.class);
        cplFeeIn.setModtyp(cplIn.getModtyp()); //所属模块
        cplFeeIn.setCustno(cplIn.getCustno()); //客户号
        cplFeeIn.setCustac(cplIn.getCustac()); //客户账号
        cplFeeIn.setTrancy(cplIn.getTrancy()); //交易币种
        cplFeeIn.setCsexfg(null); //钞汇标志
        cplFeeIn.setSeqnum(cplIn.getSeqnum()); //顺序号
        cplFeeIn.setProdcd(null); //产品号
        cplFeeIn.setTranam(cplIn.getTranam()); //交易金额 
        cplFeeIn.setAmount(cplIn.getAmount()); //数量
        cplFeeIn.setScencd(cplIn.getScencd()); //场景代码
        cplFeeIn.setChrgcd(cplIn.getChrgcd()); //收费代码
        cplFeeIn.setChevno(cplIn.getChevno()); //收费事件编号
        cplFeeIn.setChrgpd(null); //收费周期
        cplFeeIn.setLastdt(null); //上一扣费日期

        cplFeeIn.setChrgcy(cplIn.getChrgcy()); //收费币种

        if (CommUtil.isNotNull(cplIn.getAccgcy())) {
            cplFeeIn.setChrgcy(cplIn.getAccgcy()); //收费币种
        }

        cplFeeIn.setChrgsr(cplIn.getChrgsr()); //收费金额来源
        cplFeeIn.setSpcham(cplIn.getSpcham()); //指定收费金额
        cplFeeIn.setDvidam(null); //收费分成金额
        cplFeeIn.setCstrfg(E_CSTRFG.TRNSFER); //现转标志 -转帐
        cplFeeIn.setCsprcd(null); //现金项目代码

        cplFeeIn.setChgflg(cplIn.getChgflg()); //记帐标志
        cplFeeIn.setTotflg(cplIn.getTotflg()); //是否汇总记账标志
        cplFeeIn.setCplKfzhu(cplKfzhInfo); //扣费帐户信息

        if (CommUtil.isNotNull(cplIn.getTranbr())) {
            cplFeeIn.setTranbr(cplIn.getTranbr()); //交易机构
        }
        else {
            cplFeeIn.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch()); //交易机构
        }
        cplFeeIn.setOubrno(null); //转出行机构号
        cplFeeIn.setInbrno(null); //转入行机构号
        cplFeeIn.setSmrycd(cplIn.getSmrycd()); //摘要代码
        cplFeeIn.setSmryds(cplIn.getSmryds()); //摘要描述
        cplFeeIn.setRemark(cplIn.getRemark()); //备注
        
        CgCalFee_OUT cplFee_OUT = CalChrg.calCharge(cplFeeIn); //  统一计费/收费处理 

        if (CommUtil.isNotNull(cplFee_OUT)) {
            cplOut.setCgpyrv(cplFee_OUT.getCgpyrv());
            cplOut.setTotamt(cplFee_OUT.getTotamt());
            cplOut.setCustno(cplFee_OUT.getCustno());
            cplOut.setCustac(cplFee_OUT.getCustac());
            cplOut.setAccgac(cplFee_OUT.getAccgac());
            Options<IoCgFEEINFO> lstCgFEEINFO = new DefaultOptions<IoCgFEEINFO>();
            if (CommUtil.isNotNull(cplFee_OUT.getLstFeexx())) {
                for (int i = 0; i < cplFee_OUT.getLstFeexx().size(); i++) {
                    IoCgFEEINFO cplCgFEEINFO = SysUtil.getInstance(IoCgFEEINFO.class);
                    CgFEEINFO cplTmp = cplFee_OUT.getLstFeexx().get(i);
                    cplCgFEEINFO.setChgpln(cplTmp.getChgpln());
                    cplCgFEEINFO.setCgpyrv(cplTmp.getCgpyrv());
                    cplCgFEEINFO.setCgscam(cplTmp.getCgscam());
                    cplCgFEEINFO.setCghacd(cplTmp.getCghacd());
                    cplCgFEEINFO.setTrnseq(cplTmp.getTrnseq());
                    cplCgFEEINFO.setCgfacd(cplTmp.getCgfacd());
                    cplCgFEEINFO.setTrancy(cplTmp.getTrancy());
                    cplCgFEEINFO.setTrandt(cplTmp.getTrandt());
                    cplCgFEEINFO.setClchcy(cplTmp.getClchcy());
                    cplCgFEEINFO.setClcham(cplTmp.getClcham());
/*                    cplCgFEEINFO.setTrdvrt(cplTmp.getTrdvrt());
                    cplCgFEEINFO.setTrdvam(cplTmp.getTrdvam());*/
                    cplCgFEEINFO.setArrgam(cplTmp.getArrgam());
                    cplCgFEEINFO.setDedudt(cplTmp.getDedudt());
                    cplCgFEEINFO.setChrgcy(cplTmp.getChrgcy());
                    cplCgFEEINFO.setChrgcd(cplTmp.getChrgcd());
                    cplCgFEEINFO.setDvidam(cplTmp.getDvidam());
                    cplCgFEEINFO.setChrgna(cplTmp.getChrgna());
                    cplCgFEEINFO.setChrgpd(cplTmp.getChrgpd());
                    cplCgFEEINFO.setRecvfg(cplTmp.getRecvfg());
                    cplCgFEEINFO.setAcclam(cplTmp.getAcclam());
                    cplCgFEEINFO.setSequno(cplTmp.getSequno());
                    cplCgFEEINFO.setRecvam(cplTmp.getRecvam());
/*                    cplCgFEEINFO.setOudvrt(cplTmp.getOudvrt());
                    cplCgFEEINFO.setOudvam(cplTmp.getOudvam());
                    cplCgFEEINFO.setIndvrt(cplTmp.getIndvrt());
                    cplCgFEEINFO.setIndvam(cplTmp.getIndvam());*/
                    cplCgFEEINFO.setFavpec(cplTmp.getFavpec());
                    lstCgFEEINFO.add(cplCgFEEINFO);

                }
            }

            cplOut.setLstFeexx(lstCgFEEINFO);
            cplOut.setArrgam(cplFee_OUT.getArrgam());  //欠费金额
            cplOut.setApcgam(cplFee_OUT.getApcgam());  //实付金额
            cplOut.setChrgcy(cplFee_OUT.getChrgcy());  //收费币种
            cplOut.setAcclam(cplFee_OUT.getAcclam());  //实收金额
            cplOut.setAmount(cplFee_OUT.getAmount());  //数量
            cplOut.setSpcgam(cplFee_OUT.getSpcgam());  //应付金额
            cplOut.setRecvam(cplFee_OUT.getRecvam());  //应收金额
            cplOut.setAcchnm(cplFee_OUT.getAcchnm());  //账户中文名
        }
        
        return cplOut ;

    }
    
    /**  
     * @Author wuxq
     *         <p>
     *         <li>统一收费</li>
     *         </p>
     * @param
     * @return
     */    

    public IoCgCalFee_OUT CalCharge(IoCgCalFee_IN cplIn) {
        
        IoCgCalFee_OUT cplOut = SysUtil.getInstance(IoCgCalFee_OUT.class);
        PbCalCharg CalChrg = SysUtil.getInstance(PbCalCharg.class);
        CgCalFee_IN cplFeeIn = SysUtil.getInstance(CgCalFee_IN.class);
        
        cplFeeIn.setAmount(cplIn.getAmount());  //  数量
        cplFeeIn.setCstrfg(cplIn.getCstrfg());//现转标志
        cplFeeIn.setChevno(cplIn.getChevno());//收费事件编号
        cplFeeIn.setChgflg(cplIn.getChgflg());//记账标志
        cplFeeIn.setChrgcd(cplIn.getChrgcd());//收费代码
        cplFeeIn.setChrgcy(cplIn.getChrgcy());//收费币种
        cplFeeIn.setChrgpd(cplIn.getChrgpd());//收费周期
        cplFeeIn.setChrgsr(cplIn.getChrgsr());//收费金额来源 
        cplFeeIn.getCplKfzhu().setCustac(cplIn.getCplKfzhu().getCustac());//客户账号
        cplFeeIn.getCplKfzhu().setCgsbsq(cplIn.getCplKfzhu().getCgsbsq());//扣费子账户序号
        cplFeeIn.getCplKfzhu().setCrcycd(cplIn.getCplKfzhu().getCrcycd());
        cplFeeIn.getCplKfzhu().setCsexfg(cplIn.getCplKfzhu().getCsexfg());
        cplFeeIn.setCsexfg(cplIn.getCsexfg());
        cplFeeIn.setCsprcd(cplIn.getCsprcd());//现金项目代码
        cplFeeIn.setCustac(cplIn.getCustac());//客户账号
        cplFeeIn.setCustno(cplIn.getCustno());//客户号
        cplFeeIn.setDcmttp(cplIn.getDcmttp());//凭证种类
        cplFeeIn.setDvidam(cplIn.getDvidam());//收费分成金额
        cplFeeIn.setInbrno(cplIn.getInbrno());//转入行机构号
        cplFeeIn.setLastdt(cplIn.getLastdt());//上一扣费日期
        cplFeeIn.setModtyp(cplIn.getModtyp());//所属模块
        cplFeeIn.setOubrno(cplIn.getOubrno());//转出行机构号
        cplFeeIn.setProdcd(cplIn.getProdcd());//产品号
        cplFeeIn.setRemark(cplIn.getRemark());//备注信息
        cplFeeIn.setScencd(cplIn.getScencd());//场景代码
        cplFeeIn.setSeqnum(cplIn.getSeqnum());//顺序号
        cplFeeIn.setSmrycd(cplIn.getSmrycd());//摘要代码
        cplFeeIn.setSmryds(cplIn.getSmryds());//摘要描述
        cplFeeIn.setSpcham(cplIn.getSpcham());//指定收费金额
        cplFeeIn.setTotflg(cplIn.getTotflg());//是否汇总记账标志
        cplFeeIn.setTranam(cplIn.getTranam());//交易金额
        cplFeeIn.setTranbr(cplIn.getTranbr());//交易机构
        cplFeeIn.setTrancy(cplIn.getTrancy());//交易币种
        cplFeeIn.setCalcenter(cplIn.getCalcenter());//计费中心返回信息
        cplFeeIn.setPayaDetail(cplIn.getPayaDetail());//内部户挂账明细
        cplFeeIn.setPaydDetail(cplIn.getPaydDetail());//内部户销账明细
        cplFeeIn.setStrktg(cplIn.getStrktg());//是否允许冲正
        cplFeeIn.setIsclos(cplIn.getIsclos());
        cplFeeIn.setClactp(cplIn.getClactp()); //销户标识
        
        
        CgCalFee_OUT cplFee_OUT = CalChrg.calCharge(cplFeeIn); //  统一计费/收费处理 
        
        /*==========================================收费结束,返回收费信息==============================*/
        
        if (CommUtil.isNotNull(cplFee_OUT)) {
            cplOut.setCgpyrv(cplFee_OUT.getCgpyrv());
            cplOut.setTotamt(cplFee_OUT.getTotamt());
            cplOut.setCustno(cplFee_OUT.getCustno());
            cplOut.setCustac(cplFee_OUT.getCustac());
            cplOut.setAccgac(cplFee_OUT.getAccgac());
            Options<IoCgFEEINFO> lstCgFEEINFO = new DefaultOptions<IoCgFEEINFO>();
            if (CommUtil.isNotNull(cplFee_OUT.getLstFeexx())) {
                for (int i = 0; i < cplFee_OUT.getLstFeexx().size(); i++) {
                    IoCgFEEINFO cplCgFEEINFO = SysUtil.getInstance(IoCgFEEINFO.class);
                    CgFEEINFO cplTmp = cplFee_OUT.getLstFeexx().get(i);
                    cplCgFEEINFO.setChgpln(cplTmp.getChgpln());
                    cplCgFEEINFO.setCgpyrv(cplTmp.getCgpyrv());
                    cplCgFEEINFO.setCgscam(cplTmp.getCgscam());
                    cplCgFEEINFO.setCghacd(cplTmp.getCghacd());
                    cplCgFEEINFO.setTrnseq(cplTmp.getTrnseq());
                    cplCgFEEINFO.setCgfacd(cplTmp.getCgfacd());
                    cplCgFEEINFO.setTrancy(cplTmp.getTrancy());
                    cplCgFEEINFO.setTrandt(cplTmp.getTrandt());
                    cplCgFEEINFO.setClchcy(cplTmp.getClchcy());
                    cplCgFEEINFO.setClcham(cplTmp.getClcham());
/*                    cplCgFEEINFO.setTrdvrt(cplTmp.getTrdvrt());
                    cplCgFEEINFO.setTrdvam(cplTmp.getTrdvam());*/
                    cplCgFEEINFO.setArrgam(cplTmp.getArrgam());
                    cplCgFEEINFO.setDedudt(cplTmp.getDedudt());
                    cplCgFEEINFO.setChrgcy(cplTmp.getChrgcy());
                    cplCgFEEINFO.setChrgcd(cplTmp.getChrgcd());
                    cplCgFEEINFO.setDvidam(cplTmp.getDvidam());
                    cplCgFEEINFO.setChrgna(cplTmp.getChrgna());
                    cplCgFEEINFO.setChrgpd(cplTmp.getChrgpd());
                    cplCgFEEINFO.setRecvfg(cplTmp.getRecvfg());
                    cplCgFEEINFO.setAcclam(cplTmp.getAcclam());
                    cplCgFEEINFO.setSequno(cplTmp.getSequno());
                    cplCgFEEINFO.setRecvam(cplTmp.getRecvam());
/*                    cplCgFEEINFO.setOudvrt(cplTmp.getOudvrt());
                    cplCgFEEINFO.setOudvam(cplTmp.getOudvam());
                    cplCgFEEINFO.setIndvrt(cplTmp.getIndvrt());
                    cplCgFEEINFO.setIndvam(cplTmp.getIndvam());*/
                    cplCgFEEINFO.setFavpec(cplTmp.getFavpec());
                    lstCgFEEINFO.add(cplCgFEEINFO);

                }
            }

            cplOut.setLstFeexx(lstCgFEEINFO);
            cplOut.setArrgam(cplFee_OUT.getArrgam());  //欠费金额
            cplOut.setApcgam(cplFee_OUT.getApcgam());  //实付金额
            cplOut.setChrgcy(cplFee_OUT.getChrgcy());  //收费币种
            cplOut.setAcclam(cplFee_OUT.getAcclam());  //实收金额
            cplOut.setAmount(cplFee_OUT.getAmount());  //数量
            cplOut.setSpcgam(cplFee_OUT.getSpcgam());  //应付金额
            cplOut.setRecvam(cplFee_OUT.getRecvam());  //应收金额
            cplOut.setAcchnm(cplFee_OUT.getAcchnm());  //账户中文名
        }
        
        return cplOut;

    }

	@Override
	public void SelDistam(String custac, String bgdate, String endate, Long pageno, Long pagesize, Output output) {
		// 页数
		if (CommUtil.isNull(pageno)) {
			throw FeError.Chrg.BNASF281();
		}

		// 页容量
		if (CommUtil.isNull(pagesize)) {
			throw FeError.Chrg.BNASF283();
		}
		
		if(CommUtil.compare(bgdate, endate)>0){
			throw FeError.Chrg.BNASF179();
		}

		long starno = (pageno - 1) * pagesize; // 起始数
		
		IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcByCardno(custac, true);

		
		Map<String,Object> total = new HashMap<String,Object>();
		total = PBChargeFavoDao.selDistamCount(tblKnaAcdc.getCustac(),bgdate, endate, false);
		
		int count = (int) total.get("count");
		BigDecimal sum = (BigDecimal) total.get("disamt");
		if (CommUtil.compare(count, 0) > 0) {
			output.setHcount(count); //设置优惠记录数
			output.setHdisam(sum);//设置优惠总金额

			// 查询优惠明细
			List<IoCgDisamtInfo> InfoList = PBChargeFavoDao.selDistamInfo(
					tblKnaAcdc.getCustac(), bgdate, endate, starno, pagesize, false);

			if (CommUtil.isNotNull(InfoList) && InfoList.size() > 0) {
				Options<IoCgDisamtInfo> upgInfos = new DefaultOptions<IoCgDisamtInfo>();
				upgInfos.addAll(InfoList);
				output.setDisamtinfo(upgInfos);//设置优惠明细记录
			}

		} else {
			output.setHcount(0); //设置优惠记录数
			output.setHdisam(BigDecimal.ZERO);//设置优惠总金额
//			throw FeError.Chrg.E9999("无对应优惠记录");
		}

	}

    /**
     *收费调整登记簿查询
     */
	@Override
	public void IoCgChrgAdjust(
			Input input,
			cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoCgChrgAdjust.Output output) {
		
		int totlCount = 0; // 记录总数
		long startno = (input.getPageno() - 1) * input.getPagesz();// 起始记录数
		
		//获取输入参数
		E_ADJTTP adjttp = input.getAdjttp();//调整方式
		String bgdate = input.getBgdate();//起始日期
		String endate = input.getEndate();//截止日期
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date();// 系统日期
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
		//调整方式
		if(CommUtil.isNull(adjttp)){
			throw FeError.Chrg.BNASF243();
		}
		if(E_ADJTTP.FOU==adjttp){
			adjttp=null;
		}
		if(CommUtil.isNull(input.getCardno())){
			throw FeError.Chrg.BNASF500();
		}
		
		//起始日期
		if(CommUtil.isNull(bgdate)){
			throw FeError.Chrg.BNASF194();
		}
		
		//截止日期
		if(CommUtil.isNull(endate)){
			throw FeError.Chrg.BNASF170();
		}
		
		//起始日期不能大于截止日期
		if (DateUtil.compareDate(bgdate, endate) > 0) {
			throw FeError.Chrg.BNASF192();
		}
		
		//起始日期不能大于系统日期
		if (DateUtil.compareDate(bgdate, sTime) > 0) {
			throw FeError.Chrg.BNASF193();
		}
		
		//截止日期不能大于系统日期
		if (DateUtil.compareDate(endate, sTime) > 0) {
			throw FeError.Chrg.BNASF169();
		}
		
		//分页查询
		Page<cn.sunline.ltts.busi.iobus.type.cg.IoCgChrg.IoCgChrgAdjust> infosPage = PBChargeRegisterDao.selChargeAdjustRegistrInfo( bgdate, endate,adjttp,input.getCardno(),tranbr, startno, input.getPagesz(), totlCount, false);
		
		for(cn.sunline.ltts.busi.iobus.type.cg.IoCgChrg.IoCgChrgAdjust info : infosPage.getRecords()){
		
			if(CommUtil.isNotNull(info.getChrgcd())){
				
				KcpChrgAcrl tblCgtp =PBChargeCodeDao.selChargeAccRuleDetail(info.getChrgcd(), false); //根据收费代码  	
				if(CommUtil.isNotNull(tblCgtp)){
					
					info.setChrgna(tblCgtp.getChrgna());
				}
			}
			if(CommUtil.isNotNull(info.getJtchcd())){
				
				KcpChrgAcrl tblCgtp =PBChargeCodeDao.selChargeAccRuleDetail(info.getJtchcd(), false); //根据收费代码  	
				
				if(CommUtil.isNotNull(tblCgtp)){
					
					info.setJtchna(tblCgtp.getChrgna());
				}
			}
			BigDecimal chrgam = new BigDecimal(BigInteger.ZERO);
			List<KcbChrgRgst> ListKcbChrgRgst = PBChargeRegisterDao.selChargeRegistrByTrdtTrsq(CommTools.getBaseRunEnvs().getBusi_org_id(),info.getPrtrdt(),info.getPrtrsq(),false);
			if(CommUtil.isNotNull(ListKcbChrgRgst)){
				for(KcbChrgRgst kcbChrgRgst: ListKcbChrgRgst){
					chrgam = chrgam.add(kcbChrgRgst.getTranam());
				}
			}
			info.setChrgam(chrgam);
		}
		
		output.getChargeAdjustInfo().addAll(infosPage.getRecords());//把查询记录复制给output
		output.setCounts(ConvertUtil.toInteger(infosPage.getRecordCount()));//总记录数
		CommTools.getBaseRunEnvs().setTotal_count(infosPage.getRecordCount());// 设置报文头总记录条数
	}

	/**
	 * 查询收费核算规则定义
	 */
	@Override
	public void SelChrgAccRule(
			cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoSelChrgAccRule.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoSelChrgAccRule.Output output) {
		
		bizlog.method(">>>>>>>>>>>>>>>SelChrgAccRule beigin>>>>>>>>>>>>>>>");
		
		String chrgcd = input.getChrgcd(); //费种代码
		String chrgna = input.getChrgna(); //费种代码名称
		long pageno = CommTools.getBaseRunEnvs().getPage_start();//页码	
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();//页容量
		
		long totlCount = 0;
		
		Page<IoChrgAccRuleInfo> lstKcpchrgacrl =  PBChargeCodeDao.selChargeAccRule(chrgcd, chrgna, (pageno-1)*pgsize, pgsize, totlCount, false);
		Options<IoChrgAccRuleInfo> optKcpchrgacrl = new DefaultOptions<IoChrgAccRuleInfo>();// 初始化输出对象
		optKcpchrgacrl.addAll(lstKcpchrgacrl.getRecords());
		
		//输出
		output.setPinfos(optKcpchrgacrl);
		
	    CommTools.getBaseRunEnvs().setTotal_count(lstKcpchrgacrl.getRecordCount());// 记录总数
	
		bizlog.method(">>>>>>>>>>>>>>SelChrgAccRule end >>>>>>>>>>>>>>>");
	}

	/**
	 * 新增收费核算规则定义
	 */
	@Override
	public void InsChrgAccRule(
			cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoInsChrgAccRule.Input input) {
		
		bizlog.method(">>>>>>>>>>>>>InsChrgAccRule begin>>>>>>>>>>>>>>>>>");
		
		String chrgcd = input.getChrgcd(); //费种代码
		String chrgna = input.getChrgna(); //费种代码名称
		E_YES___ chrgsg = input.getChrgsg(); // 是否允许集中收费标志
		E_BLLWTP bllwtp = input.getBllwtp(); //余额不足处理方式
		String debkpd = input.getDebkpd(); //欠费扣费周期
		String chrgpd = input.getChrgpd(); //收费周期
		E_PREWAY fvrmde = input.getFvrmde(); //优惠方式
		E_PFRTIM pfrtim = input.getPfrtim(); //优惠返还时间
		String crcycd = input.getCrcycd(); //币种
		
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), input.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}
		
		if(CommUtil.isNull(crcycd)){
			throw FeError.Chrg.BNASF156();
		}
		
		if(CommUtil.isNull(chrgcd)){
			throw FeError.Chrg.BNASF076();
		}
		
		if(CommUtil.isNull(chrgna)){
			throw FeError.Chrg.BNASF081();
		}
		
		if(CommUtil.isNull(chrgsg)){
			throw FeError.Chrg.BNASF219();
		}
		
		if(CommUtil.isNull(bllwtp)){
			throw FeError.Chrg.BNASF316();
		}
		
		if(E_YES___.YES == chrgsg){
			if(CommUtil.isNull(chrgpd)){
				throw FeError.Chrg.BNASF236();
			}
			if(E_BLLWTP.SWITCH_FAILE == bllwtp ){
				throw FeError.Chrg.BNASF134();
			}
		}else {
			if(CommUtil.isNotNull(chrgpd)){
				throw FeError.Chrg.BNASF401();
			}
		}
		
		if((E_BLLWTP.ONLNBL_OWE == bllwtp) || E_BLLWTP.OWE == bllwtp){
			if(CommUtil.isNull(debkpd)){
				throw FeError.Chrg.BNASF196();
			}
		}else {
			if(CommUtil.isNotNull(debkpd)){
				throw FeError.Chrg.BNASF402();
			}
		}
		if(CommUtil.isNotNull(chrgpd)){
			if(!CommUtil.equals(chrgpd,"01")&&!CommUtil.equals(chrgpd, "02")&&!CommUtil.equals(chrgpd, "03")){
				throw FeError.Chrg.BNASF403();
			}
		}
		if(CommUtil.isNotNull(debkpd)){
			if(!CommUtil.equals(debkpd,"01")&&!CommUtil.equals(debkpd, "02")&&!CommUtil.equals(debkpd, "03")){
				throw FeError.Chrg.BNASF403();
			}
		}
//		if(CommUtil.isNull(fvrmde)){
//			throw FeError.Chrg.E2118("优惠方式");
//		}
		if(CommUtil.isNotNull(input.getFvrmde())){
			if(E_PREWAY.tim == fvrmde){
				if(CommUtil.isNull(pfrtim)){
					throw FeError.Chrg.BNASF300();
				}
			}
			if(E_PREWAY.imm == fvrmde){
				if(CommUtil.isNotNull(pfrtim)){
					throw FeError.Chrg.BNASF404();
				}
			}
		}
		KcpChrgAcrl kcpChrgAcrl = KcpChrgAcrlDao.selectOne_odb1(chrgcd, false);
		if(CommUtil.isNotNull(kcpChrgAcrl)){
			throw FeError.Chrg.BNASF405();
		}
		//实例化收费核算规则定义表、登记簿公共类
		KcpChrgAcrl tblKcpChrgAcrl = SysUtil.getInstance(KcpChrgAcrl.class); 		
		tblKcpChrgAcrl.setChrgcd(chrgcd); //费种代码
		tblKcpChrgAcrl.setChrgna(chrgna); //费种代码名称
		tblKcpChrgAcrl.setChrgsg(chrgsg); //是否允许集中收费标志
		tblKcpChrgAcrl.setBllwtp(bllwtp); //余额不足处理方式
		tblKcpChrgAcrl.setChrgpd(chrgpd); //收费周期
		tblKcpChrgAcrl.setDebkpd(debkpd); //欠费扣费周期
		tblKcpChrgAcrl.setFvrmde(fvrmde); //优惠方式
		tblKcpChrgAcrl.setPfrtim(pfrtim); //优惠返还时间
		tblKcpChrgAcrl.setCrcycd(crcycd); //币种
		if(CommUtil.equals("1", chrgcd.substring(2, 3))){//第3位表示费用收付标志（0-收费，1-付费）
			tblKcpChrgAcrl.setCgpyrv(E_CGPYRV.PAY);
		}else{
			tblKcpChrgAcrl.setCgpyrv(E_CGPYRV.RECIVE);
		}
		
		//新增
		KcpChrgAcrlDao.insert(tblKcpChrgAcrl); 
		
		ApDataAudit.regLogOnInsertParameter(tblKcpChrgAcrl);
		
		bizlog.method(">>>>>>>>>>>>>InsChrgAccRule end>>>>>>>>>>>>>>>>>");
	}

	/**
	 * 修改收费核算规则定义
	 */
	@Override
	public void UpdChrgAccRule(
			cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoUpdChrgAccRule.Input input) {

		bizlog.method(">>>>>>>>>>>UpdChrgAccRule begin>>>>>>>>>>>>>>>");
		String chrgcd = input.getChrgcd(); //费种代码
		E_YES___ chrgsg = input.getChrgsg();
		String debkpd = input.getDebkpd();
		String chrgpd = input.getChrgpd();
		E_BLLWTP bllwtp = input .getBllwtp();
		
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), input.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}
		
		if(CommUtil.isNull(chrgcd)){
			throw FeError.Chrg.BNASF076();
		}
		
		if(CommUtil.isNull(input.getChrgsg())){
			throw FeError.Chrg.BNASF219();
		}
		
		
		if(CommUtil.isNull(input.getBllwtp())){
			throw FeError.Chrg.BNASF316();
		}
		
		if(E_YES___.YES == chrgsg){
			if(CommUtil.isNull(chrgpd)){
				throw FeError.Chrg.BNASF236();
			}
			if(E_BLLWTP.SWITCH_FAILE == bllwtp ){
				throw FeError.Chrg.BNASF134();
			}
		}else {
			if(CommUtil.isNotNull(chrgpd)){
				throw FeError.Chrg.BNASF401();
			}
		}
		
		if((E_BLLWTP.ONLNBL_OWE == bllwtp) || E_BLLWTP.OWE == bllwtp){
			if(CommUtil.isNull(debkpd)){
				throw FeError.Chrg.BNASF196();
			}
		}else {
			if(CommUtil.isNotNull(debkpd)){
				throw FeError.Chrg.BNASF402();
			}
		}
		if(CommUtil.isNotNull(chrgpd)){
			if(!CommUtil.equals(chrgpd,"01")&&!CommUtil.equals(chrgpd, "02")&&!CommUtil.equals(chrgpd, "03")){
				throw FeError.Chrg.BNASF403();
			}
		}
		if(CommUtil.isNotNull(debkpd)){
			if(!CommUtil.equals(debkpd,"01")&&!CommUtil.equals(debkpd, "02")&&!CommUtil.equals(debkpd, "03")){
				throw FeError.Chrg.BNASF403();
			}
		}
		
//		if(CommUtil.isNull(input.getFvrmde())){
//			throw FeError.Chrg.E2118("优惠方式");
//		}
		
		if(CommUtil.isNotNull(input.getFvrmde())){
			if(E_PREWAY.tim == input.getFvrmde()){
				if(CommUtil.isNull(input.getPfrtim())){
					throw FeError.Chrg.BNASF300();
				}
			}
			if(E_PREWAY.imm == input.getFvrmde()){
				if(CommUtil.isNotNull(input.getPfrtim())){
					throw FeError.Chrg.BNASF404();
				}
			}
		}

		
		//根据费种代码获取收费核算规则定义
		KcpChrgAcrl tblKcpChrgAcrl = KcpChrgAcrlDao.selectOne_odb1(chrgcd, false);
		
		if(CommUtil.isNotNull(tblKcpChrgAcrl)){
			if(CommUtil.compare(input.getBllwtp(), tblKcpChrgAcrl.getBllwtp()) == 0
					&&CommUtil.compare(input.getChrgpd(), tblKcpChrgAcrl.getChrgpd()) == 0
					&&CommUtil.compare(input.getChrgsg(), tblKcpChrgAcrl.getChrgsg()) == 0
					&&CommUtil.compare(input.getDebkpd(), tblKcpChrgAcrl.getDebkpd()) == 0
					&&CommUtil.compare(input.getFvrmde(), tblKcpChrgAcrl.getFvrmde()) == 0
					&&CommUtil.compare(input.getPfrtim(), tblKcpChrgAcrl.getPfrtim()) == 0){
				throw FeError.Chrg.BNASF317();
			}
			Long num = (long) 0; //初始化序号
			KcpChrgAcrl oldKcpCharAcrl = CommTools.clone(KcpChrgAcrl.class, tblKcpChrgAcrl);
			
			if(CommUtil.compare(input.getBllwtp(), tblKcpChrgAcrl.getBllwtp()) != 0){ //余额不足处理方式
				num++;
				//明细登记簿维护
			
				tblKcpChrgAcrl.setBllwtp(input.getBllwtp());
			}
			
			if(CommUtil.compare(input.getChrgpd(), tblKcpChrgAcrl.getChrgpd()) != 0){ //收费周期
				num++;
				//明细登记簿维护
				tblKcpChrgAcrl.setChrgpd(input.getChrgpd());
			}
			
			if(CommUtil.compare(input.getChrgsg(), tblKcpChrgAcrl.getChrgsg()) != 0){ //是否允许集中收费标志
				num++;
				//明细登记簿维护
				tblKcpChrgAcrl.setChrgsg(input.getChrgsg());
			}
			
			if(CommUtil.compare(input.getDebkpd(), tblKcpChrgAcrl.getDebkpd()) != 0){ //欠费扣费周期
				num++;
				//明细登记簿维护
				tblKcpChrgAcrl.setDebkpd(input.getDebkpd());
			}
			
			if(CommUtil.compare(input.getFvrmde(), tblKcpChrgAcrl.getFvrmde()) != 0){ //优惠方式
				num++;
				//明细登记簿维护
				tblKcpChrgAcrl.setFvrmde(input.getFvrmde());
			}
			
			if(CommUtil.compare(input.getPfrtim(), tblKcpChrgAcrl.getPfrtim()) != 0){ //优惠返还时间
				num++;
				//明细登记簿维护
				tblKcpChrgAcrl.setPfrtim(input.getPfrtim());
			}
			
			//更新
			KcpChrgAcrlDao.updateOne_odb1(tblKcpChrgAcrl);
			
			ApDataAudit.regLogOnUpdateParameter(oldKcpCharAcrl, tblKcpChrgAcrl);
			
		}else{
	    	throw FeError.Chrg.BNASF152();
	    }
			
		bizlog.method(">>>>>>>>>>>UpdChrgAccRule end>>>>>>>>>>>>>>>");
		
	}

	/**
	 * 删除收费核算规则定义
	 */
	@Override
	public void DelChrgAccRule(
			cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoDelChrgAccRule.Input input) {

		bizlog.method(">>>>>>>>>>>>>>>>DelChrgAccRule begin>>>>>>>>>>>>>>");
		
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), input.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}
		
		if(CommUtil.isNull(input.getChrgcd())){
			throw FeError.Chrg.BNASF076();
		}
		
		KcpChrgAcrl entity = SysUtil.getInstance(KcpChrgAcrl.class);
		entity = KcpChrgAcrlDao.selectOne_odb1(input.getChrgcd(), false);
		if(CommUtil.isNull(entity)){
			throw FeError.Chrg.BNASF338();
		}
		
		//删除
		KcpChrgAcrlDao.deleteOne_odb1(input.getChrgcd());
		
		//新增历史表
		KcpChrgAcrlHist tblKcpChrgAcrlHist = SysUtil.getInstance(KcpChrgAcrlHist.class);
		CommUtil.copyProperties(tblKcpChrgAcrlHist, entity);
		tblKcpChrgAcrlHist.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		KcpChrgAcrlHistDao.insert(tblKcpChrgAcrlHist);
		
		ApDataAudit.regLogOnDeleteParameter(entity);
		
		bizlog.method(">>>>>>>>>>>>>>>>DelChrgAccRule end>>>>>>>>>>>>>>");
		
	}
	/**
	 * 费用清单报表查询
	 */
	
	@Override
	public void CgChageList(
			cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoCgChageList.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoCgChageList.Output output) {
		
		int totlCount = 0; // 记录总数
		long pageno = CommTools.getBaseRunEnvs().getPage_start();// 页码
		long pagesz = CommTools.getBaseRunEnvs().getPage_size();//页容量
		
		
		String custno = input.getCustno();//客户号
		String custac =input.getCustac();//账号
		
		if(CommUtil.isNull(custno)&&CommUtil.isNull(custac)){
			throw FeError.Chrg.BNASF504();
		}
		
		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class); 
		//内部户验证		
		final cn.sunline.ltts.busi.iobus.servicetype.IoApAccount.queryAccountType.Output out =  SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.IoApAccount.queryAccountType.Output.class);
	  
		//add by wuzx -20161011-区分是电子账号还是内部户账号-beg
		if(CommUtil.isNotNull(custac)){
			
	    	 SysUtil.getInstance(IoApAccount.class).queryAccountType(custac, out);
	 	    
	 	    if(out.getAccttp() != E_ACCTTP.IN){////电子账户	    	    
	 	    	IoCaKnaAcdc knaAcdc = caSevQryTableInfo.getKnaAcdcByCardno(input.getCustac(), false);
	 	    	
	 	    if(CommUtil.isNotNull(knaAcdc)){				
	 				 custac = knaAcdc.getCustac();//账号
	 			}
	 	    }
	    }
		//add by wuzx -20161011-区分是电子账号还是内部户账号-end

		String brchno = input.getBrchno();//机构号
		E_GTAMFG recvfg = input.getRecvfg();//收费状态
		String bgdate = input.getBgdate();//起始日期
		String endate = input.getEndate();//终止日期
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date();
		E_CGPYRV cgpyrv = input.getCgpyrv();
		String crcycd = input.getChrgcy();
		String servtp = input.getServtp();
		
		//20170118 add songlw 账号、机构号、收费状态不能同时为空
		if(CommUtil.isNull(custac)&&CommUtil.isNull(brchno)&&CommUtil.isNull(recvfg)){
			throw FeError.Chrg.BNASF326();
		}
	    
		//如果输入机构号，则判断权限
		if(CommUtil.isNotNull(brchno)){
	//		String Clerbr = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch();//获取省联社清算中心
			String centbr = BusiTools.getBusiRunEnvs().getCentbr();
			String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
			
			if(!CommUtil.equals(tranbr,centbr)&&!CommUtil.equals(tranbr, brchno)){
				throw FeError.Chrg.BNASF061();
			}			
		}
		
		//页容量默认为10条
		if(CommUtil.isNull(pagesz)){
			
			pagesz = 10;
		}
		
		
		//费用标志只接收已收和未收两种，其他则抛异常
		if(CommUtil.isNotNull(recvfg)){
			
			if (recvfg != E_GTAMFG.NO && recvfg != E_GTAMFG.YES) {

				throw FeError.Chrg.BNASF064();

			}
		}
		
		
		//起始日期和截止日期不能为空，起始日期和终止日期不得超过当前系统日期，且跨度不得超过一年
		// 起始日期
		if (CommUtil.isNull(bgdate)) {
			throw FeError.Chrg.BNASF194();
		}

		// 截止日期
		if (CommUtil.isNull(endate)) {
			throw FeError.Chrg.BNASF170();
		}

		// 起始日期不能大于截止日期
		if (DateUtil.compareDate(bgdate, endate) > 0) {
			throw FeError.Chrg.BNASF192();
		}

		// 起始日期不能大于系统日期
		if (DateUtil.compareDate(bgdate, sTime) > 0) {
			throw FeError.Chrg.BNASF193();
		}

		// 截止日期不能大于系统日期
		if (DateUtil.compareDate(endate, sTime) > 0) {
			throw FeError.Chrg.BNASF169();
		}
		
		//跨度是否超过一年
		String Sdate = DateTools2.calDateByTerm(bgdate, "1Y");//起始日期加1年
		
	    //截止日期是否超出
		if(CommUtil.compare(endate, Sdate) > 0){
			
			throw FeError.Chrg.BNASF195();
			
		}
		
		//费用清单报表查询 
		Page<disChargeInfo> tblKcbChrgRgst = PBChargeRegisterDao.selChargeExpenseList(custno, custac, brchno, recvfg, bgdate,endate,cgpyrv,crcycd,servtp,(pageno-1) * pagesz, pagesz, totlCount, false);
		//if(CommUtil.isNull(tblKcbChrgRgst.getRecords())){
		
		//option转换
		Options<disChargeInfo> lstOut = new DefaultOptions<>();
		
		if(tblKcbChrgRgst.getRecordCount() != 0){	
			for(disChargeInfo info : tblKcbChrgRgst.getRecords()){
				
				
				EacctCustnaCardno cplCust = SysUtil.getInstance(ServEacctSvcType.class).qryCustnaAndCardno(info.getCustac());
				
				if(CommUtil.isNotNull(cplCust.getCardno())){
					
					info.setCustac(cplCust.getCardno());
				}
				if(info.getRecvfg()==E_GTAMFG.PART){
					info.setRecvfg(E_GTAMFG.NO);//部分收讫算未收讫
				}
				info.setAuthus(info.getAuttel());//授权柜员
				info.setTransq(info.getTrnseq());//交易柜员
				info.setServtp(info.getTrnchl());//渠道
				info.setTrnseq(CommTools.getBaseRunEnvs().getTrxn_seq());//柜员流水
				info.setAcctna(info.getAcchnm());//账户名称
				info.setTranbr(info.getAcctbr());
				info.setRecvam(info.getClcham());//应收费用传 计算费用（基础费率计算结果值）
				lstOut.add(info);
			}
		}
		
		//给公共总条数赋值
		CommTools.getBaseRunEnvs().setTotal_count(tblKcbChrgRgst.getRecordCount());
		
		//输出
		output.setDisChargeInfo(lstOut);
		output.setCounts(ConvertUtil.toInteger(tblKcbChrgRgst.getRecordCount()));
		
	}

	@Override
	public void CgQrchar(
			cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoCgQrchar.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoCgQrchar.Output output) {

		int totlCount = 0; // 记录总数
		long pageno = CommTools.getBaseRunEnvs().getPage_start();// 页码
		long pagesz = CommTools.getBaseRunEnvs().getPage_size();//页容量
		String sendbr = CommTools.getBaseRunEnvs().getTrxn_branch();
		
		String prtrdt = input.getPrtrdt();//原交易日期
		String prtrsq = input.getPrtrsq();//原交易流水
		
		if (CommUtil.isNull(prtrdt)) {
			throw FeError.Chrg.BNASF321();
		}
		if (CommUtil.isNull(prtrsq)) {
			throw FeError.Chrg.BNASF319();
		}
		if (CommUtil.isNull(input.getCardno())) {
			throw FeError.Chrg.BNASF500();
		}
		
		if(pageno <= 0 ||CommUtil.isNull(pageno)){					
			throw FeError.Chrg.BNASF282();
		}
		
		Page<KcbChrgRgst> pgkcbchrgrgst =PBChargeRegisterDao.selChargeRegistByPrtrdtAndPrtrsq(prtrdt, prtrsq,  (pageno-1) * pagesz, pagesz, totlCount, false);

		if(pgkcbchrgrgst.getRecordCount()==0){
			
			throw FeError.Chrg.BNASF269();
		}
		if(!CommUtil.equals(pgkcbchrgrgst.getRecords().get(0).getAcctbr(), sendbr)){
			
			throw FeError.Chrg.BNASF322();
		}
		if(!CommUtil.equals(pgkcbchrgrgst.getRecords().get(0).getCustac(), SysUtil.getInstance(IoCaSevQryTableInfo.class).
				getKnaAcdcByCardno(input.getCardno(), true).getCustac())){
			
			throw FeError.Chrg.BNASF502();
		}
		for(KcbChrgRgst info : pgkcbchrgrgst.getRecords()){
			
			IoCgQrcharInfo cain = SysUtil.getInstance(IoCgQrcharInfo.class);
			cain.setCgpyrv(info.getCgpyrv());//费用收付标志
			cain.setChrgcd(info.getChrgcd());//费种代码
			cain.setChrgna(info.getChrgna());//费种名称
			cain.setScencd(info.getScencd());//场景代码
			cain.setScends(info.getScends());//场景名称
			cain.setTrinfo(info.getTrinfo());//交易信息
			cain.setProdcd(info.getProdcd());//产品编码
			cain.setServtp(info.getTrnchl());//交易渠道
			cain.setEvrgsq(info.getEvrgsq());//事件登记序号
			EacctCustnaCardno cplCust = SysUtil.getInstance(ServEacctSvcType.class).qryCustnaAndCardno(info.getCustac());
			if(CommUtil.isNotNull(cplCust.getCardno())){
				
				cain.setAcctno(cplCust.getCardno());//返回卡号
			}else{
				
				cain.setAcctno(info.getCustac());//对方账号
			}
			
			
			
			cain.setCrcycd(info.getChrgcy());//币种
			cain.setChrgam(info.getAcclam());//金额
			
			output.getChargeDetailInfo().add(cain);
		}
		//给公共总条数赋值
		CommTools.getBaseRunEnvs().setTotal_count(pgkcbchrgrgst.getRecordCount());
	}

	/**
	 * 检查费种附加属性信息
	 */
	@Override
	public void checkAccInfoCrt(
			cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoCheckAccInfoCrt.Input input) {
		
		IoDpSrvQryTableInfo dpSrvQryTableInfo = SysUtil.getInstance(IoDpSrvQryTableInfo.class);
		Long prod_clerCount = dpSrvQryTableInfo.getKnsProdClerOdb1(input.getProdcd(), input.getEventp());
		if(prod_clerCount <= 0 ){
			throw FeError.Chrg.BNASF265();
		}
	}

	@Override
	public void IoCgChrgByTrnseq(
			String mtrasq,
			cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoCgChrgByTrnseq.Output output) {

		if(CommUtil.isNull(mtrasq)){
			throw FeError.Chrg.BNASF319();
		}
		
		List<disChargeInfo> lstDisCharInfo = PBChargeRegisterDao.selChargeRegistrByTransq(mtrasq, false);
		Options<disChargeInfo> optDisChrgInfo = new DefaultOptions<disChargeInfo>();
		optDisChrgInfo.addAll(lstDisCharInfo);
		
		output.setCgChrgAdjust(optDisChrgInfo);
		
	}
	/**
	 * 收费付费登记簿查询
	 */
	@Override
	public void CgChageRgstList(
			cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoCgChageRgstList.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoCgChageRgstList.Output output) {
		//add by wuzx - 20170119 -收费付费登记簿查询输入增加校验  -beg
		if(CommUtil.isNull(input.getBrchno())){
			throw FeError.Chrg.BNASF131();
		}
		if(CommUtil.isNull(input.getCgpyrv())){
			throw FeError.Chrg.BNASF067();
		}
		if(CommUtil.isNull(input.getChrgcy())){
			throw FeError.Chrg.BNASF156();
		}
        if(input.getCgpyrv() == E_CGPYRV.PAY && CommUtil.isNotNull(input.getRecvfg())){
        	throw FeError.Chrg.BNASF223();
        }
        
        if(input.getCgpyrv() == E_CGPYRV.RECIVE && CommUtil.isNull(input.getRecvfg())){
        	throw FeError.Chrg.BNASF224();
        }
        
        if(input.getCgpyrv() == E_CGPYRV.RECIVE && input.getRecvfg() == E_GTAMFG.NO && CommUtil.isNull(input.getCustac())){
        	throw FeError.Chrg.BNASF162();
        }
		//add by wuzx - 20170119 -收费付费登记簿查询输入增加校验  -end
		int totlCount = 0; // 记录总数
		long pageno = CommTools.getBaseRunEnvs().getPage_start();// 页码
		long pagesz = CommTools.getBaseRunEnvs().getPage_size();//页容量
		
		
		String custno = input.getCustno();//客户号
		String custac =input.getCustac();//账号
		
		if(CommUtil.isNull(custno)&&CommUtil.isNull(custac)){
			throw FeError.Chrg.BNASF504();
		}
		
		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class); 
		//内部户验证		
		final cn.sunline.ltts.busi.iobus.servicetype.IoApAccount.queryAccountType.Output out =  SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.IoApAccount.queryAccountType.Output.class);
	   //add by wuzx -20161011-区分是电子账号还是内部户账号-beg
		if(CommUtil.isNotNull(custac)){
			
	    	 SysUtil.getInstance(IoApAccount.class).queryAccountType(custac, out);
	 	    
	 	    if(out.getAccttp() != E_ACCTTP.IN){////电子账户	    	    
	 	    	IoCaKnaAcdc knaAcdc = caSevQryTableInfo.getKnaAcdcByCardno(input.getCustac(), false);
	 	    	
	 	    if(CommUtil.isNotNull(knaAcdc)){				
	 				 custac = knaAcdc.getCustac();//账号
	 			}
	 	    }
	    }
		//add by wuzx -20161011-区分是电子账号还是内部户账号-end

		String brchno = input.getBrchno();//机构号
		E_GTAMFG recvfg = input.getRecvfg();//收费状态
		String bgdate = input.getBgdate();//起始日期
		String endate = input.getEndate();//终止日期
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date();
		E_CGPYRV cgpyrv = input.getCgpyrv();
		String crcycd = input.getChrgcy();
		String servtp = input.getServtp();
	    
		//如果输入机构号，则判断权限
		if(CommUtil.isNotNull(brchno)){
		//	String Clerbr = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch().getBrchno();//获取省联社清算中心	
			String centbr = BusiTools.getBusiRunEnvs().getCentbr();
			String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
			String sendbr = CommTools.getBaseRunEnvs().getTrxn_branch();
			if((!CommUtil.equals(tranbr,centbr))&&(!CommUtil.equals(sendbr, brchno))){
				throw FeError.Chrg.BNASF061();
			}			
		}
		
		//页容量默认为10条
		if(CommUtil.isNull(pagesz)){
			
			pagesz = 10;
		}
		
		
		//费用标志只接收已收和未收两种，其他则抛异常
		if(CommUtil.isNotNull(recvfg)){
			
			if (recvfg != E_GTAMFG.NO && recvfg != E_GTAMFG.YES) {

				throw FeError.Chrg.BNASF064();

			}
		}
		
		
		//起始日期和截止日期不能为空，起始日期和终止日期不得超过当前系统日期，且跨度不得超过一年
		// 起始日期
		if (CommUtil.isNull(bgdate)) {
			throw FeError.Chrg.BNASF194();
		}

		// 截止日期
		if (CommUtil.isNull(endate)) {
			throw FeError.Chrg.BNASF170();
		}

		// 起始日期不能大于截止日期
		if (DateUtil.compareDate(bgdate, endate) > 0) {
			throw FeError.Chrg.BNASF192();
		}

		// 起始日期不能大于系统日期
		if (DateUtil.compareDate(bgdate, sTime) > 0) {
			throw FeError.Chrg.BNASF193();
		}

		// 截止日期不能大于系统日期
		if (DateUtil.compareDate(endate, sTime) > 0) {
			throw FeError.Chrg.BNASF169();
		}

		//费用清单报表查询 
		Page<disChargeInfo> tblKcbChrgRgst = PBChargeRegisterDao.selChargeList(custno, custac, brchno, recvfg, bgdate,endate,cgpyrv,crcycd,servtp,(pageno-1) * pagesz, pagesz, totlCount, false);
		
		//option转换
		Options<disChargeInfo> lstOut = new DefaultOptions<>();
		
		if(tblKcbChrgRgst.getRecordCount()!=0){
			for(disChargeInfo info : tblKcbChrgRgst.getRecords()){
				
				
				EacctCustnaCardno cplCust = SysUtil.getInstance(ServEacctSvcType.class).qryCustnaAndCardno(info.getCustac());
				
				if(CommUtil.isNotNull(cplCust.getCardno())){
					
					info.setCustac(cplCust.getCardno());
				}
				if(info.getRecvfg()==E_GTAMFG.PART){
					info.setRecvfg(E_GTAMFG.NO);//部分收讫算未收讫
				}			
				info.setAuthus(info.getAuttel());//授权柜员
				info.setTransq(info.getTrnseq());//交易柜员
				info.setServtp(info.getTrnchl());//渠道
				info.setAcctna(info.getAcchnm());//账户名称
				info.setTranbr(info.getAcctbr());
		//		info.setTrnseq(CommTools.getBaseRunEnvs().getTrxn_seq());//柜员流水
				info.setRecvam(info.getClcham());//应收费用传 计算费用（基础费率计算结果值）
				lstOut.add(info);
			}
		}
		
				
		//给公共总条数赋值
		CommTools.getBaseRunEnvs().setTotal_count(tblKcbChrgRgst.getRecordCount());
		
		//输出
		output.setDisChargeInfo(lstOut);
		output.setCounts(ConvertUtil.toInteger(tblKcbChrgRgst.getRecordCount()));
		
	}





	/**
	 *根据电子账户查询未收讫的收费记录
	 * @return 
	 */
	@Override
	public  E_YES___ CgChageRgstNotChargeByCustac(String custac) {
			 
 
		if(CommUtil.isNull(custac)){
			
			throw FeError.Chrg.BNASF383();
		}
		
		List<IoCgQrcharInfo> list = PBChargeRegisterDao.selNotChargeByCustac(custac, false);
		
		E_YES___ yesono =null;
		
		if(CommUtil.isNull(list)||list.size()==0){
			yesono=E_YES___.NO;
			
		}else{
			yesono=E_YES___.YES;
		}
		
		
		return yesono;
	}

	public void CgChrgAdjustbytransq( String transq,  String trandt,  final cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoCgChrgAdjustbytransq.Output output){
		
		if(CommUtil.isNull(trandt)){
			throw FeError.Chrg.BNASF358();
		}
		
		if(CommUtil.isNull(transq)){
			throw FeError.Chrg.BNASF319();
		}
		
			
		cn.sunline.ltts.busi.iobus.type.cg.IoCgChrg.IoCgChrgAdjust cgadif = PBChargeRegisterDao.selChargeAdjustbytransq(trandt, transq, false);
		
		if(CommUtil.isNotNull(cgadif)){
			output.setCgadif(cgadif);
		}else{
			throw FeError.Chrg.BNASF382();
		}
	}
	

}

