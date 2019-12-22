package cn.sunline.ltts.busi.in.inner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_EVENTLEVEL;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_EVNTST;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_YESORNO;
import cn.sunline.clwj.msap.util.sequ.MsSeqUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.busi.dp.errors.InModuleError;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.GlDateTools;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcctDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaLsbl;
import cn.sunline.ltts.busi.in.tables.In.GlKnaLsblDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnsGlvc;
import cn.sunline.ltts.busi.in.tables.In.GlKnsGlvcDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnsRlbl;
import cn.sunline.ltts.busi.in.tables.In.GlKnsRlblDao;
import cn.sunline.ltts.busi.in.tables.In.KnsPaya;
import cn.sunline.ltts.busi.in.tables.In.KnsPayaDao;
import cn.sunline.ltts.busi.in.tables.In.KnsPayd;
import cn.sunline.ltts.busi.in.tables.In.KnsPaydDao;
import cn.sunline.ltts.busi.in.type.InQueryTypes.InacProInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaPayaDetail;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaPayasqList;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaPaydDetail;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.in.IoInOpenCloseComplex.IoInacOpen_IN;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ACUTTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BKFNST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BUSITP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CKVCFG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CORRTG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CRACTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CRPSMD;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_GLTRST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INPTSR;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ISPAYA;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_KPACFG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYAST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYDST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_RLBLTG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_TOTLTG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

/**
 * 内部户借方交易逻辑实现
 * */

public class InacTransDeal {
    private static final BizLog bizlog = BizLogUtil.getBizLog(InacTransDeal.class);
    
    /**
     * 
     * <p>内部户记账处理</p>
     * <li>1、查询内部户信息，内部户不存在，先开户</li>
     * <li>2、记账处理</li>
     * @Author wanggl
     *         <p>
     *         <li>2015年7月28日-上午10:27:45</li>
     *         <li>功能说明</li>
     *         </p>
     * @param input
     * @return
     *
     */
    public static IaTransOutPro dealInnerAccountTran(IaAcdrInfo input) {
        // 1、内部户查询
        InacProInfo info = SysUtil.getInstance(InacProInfo.class);
        info = InnerAcctQry.qryAcctPro( input.getCrcycd(), input.getAcbrch(),
                input.getBusino(), input.getAcctno(),input.getItemcd(),input.getSubsac());
        //账户不存在，则新开账户
        if (info.getIsexis() == E_YES___.NO) {
        	IoInacOpen_IN inacopIn = SysUtil.getInstance(IoInacOpen_IN.class);
        	
        	CommUtil.copyProperties(inacopIn, input);
        	
            input.setAcctno(InnerAcctQry.addInAcct(inacopIn).getAcctno());
        }
        else {
            input.setAcctno(CommUtil.nvl(input.getAcctno(), info.getAcctno()));
        }
        // 查询账户是否存在
        GlKnaAcct knaAcctTmp = InQuerySqlsDao.sel_GlKnaAcct_by_acct(input.getAcctno(), false);
        if (CommUtil.isNull(knaAcctTmp)) {
            throw InModuleError.InAccount.IN030043(input.getAcctno());
        }
        CommUtil.copyProperties(info, knaAcctTmp, false);
        // 设置法人号为内部户所在法人
        String odcorp =BusiTools.getBusiRunEnvs().getSpcono();
        BusiTools.getBusiRunEnvs().setSpcono(knaAcctTmp.getCorpno());
        // 账户状态检查
        if (CommUtil.isNull(knaAcctTmp.getAcctst())) {
        	throw InModuleError.InAccount.IN030044();
        }
        if (knaAcctTmp.getAcctst() != E_INACST.NORMAL) {
        	throw InModuleError.InAccount.IN030045();
        }
        // 交易类别检查 
        checkTrantp(input, info);
        
        /**
         * mod by xj 20180502 注释挂销账处理
         */
        /*//处理挂销账登记簿
        dealPayDetail(input, info);*/

        //交易方向检查 
        E_IOFLAG ioflag = info.getIoflag(); //表内外标志
        E_AMNTCD amntcd = input.getAmntcd(); //交易方向

        if (ioflag == E_IOFLAG.IN && (!(amntcd == E_AMNTCD.DR || amntcd == E_AMNTCD.CR))) {
        	throw InModuleError.InAccount.IN030046();
        }
        if (ioflag == E_IOFLAG.OUT && (!(amntcd == E_AMNTCD.RV || amntcd == E_AMNTCD.PY))) {
        	throw InModuleError.InAccount.IN030047();
        }
        //交易金额检查
        if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) == 0){
        	throw InModuleError.InAccount.IN030048();
        }
        if (input.getQuotfs() == E_YES___.YES && CommUtil.compare(input.getTranam(), BigDecimal.ZERO) < 0) {
        	throw InModuleError.InAccount.IN030049();
        }
        //内部帐户收付处理input, info
         IaTransOutPro pro=actionRecvPaysDeal(input, info);
         //重置法人为原法人号
         BusiTools.getBusiRunEnvs().setSpcono(odcorp);
         return pro;
    }

    private static void checkTrantp(IaAcdrInfo input, InacProInfo info) {

        E_AMNTCD amntcd = input.getAmntcd(); //交易方向
        E_BUSITP busitp = info.getBusitp(); //核算属性
        E_BLNCDN blncdn = info.getBlncdn(); //账户余额方向\
        E_TOTLTG totltg = CommUtil.nvl(info.getTotltg(), E_TOTLTG._0); //汇总记账标志
        E_TRANTP trantp = CommUtil.nvl(input.getTrantp(), E_TRANTP.TR); //交易类别
        E_INPTSR inptsr = CommUtil.nvl(input.getInptsr(), E_INPTSR.GL01); //交易来源
        E_KPACFG kpacfg = CommUtil.nvl(info.getKpacfg(), E_KPACFG._1); //记账标志-自动记账
        

       if(input.getCorrtg() == E_CORRTG._1 && info.getCractp() ==E_CRACTP._2){
        	throw InModuleError.InAccount.IN030050(info.getAcctno());
        }
        //交易类别检查
        if (!CommUtil.isInEnum(E_TRANTP.class, trantp)) {
        	throw InModuleError.InAccount.IN030051(trantp+"");
        }
        //借贷方检查
        if (!(amntcd == E_AMNTCD.CR
                || amntcd == E_AMNTCD.DR
                || amntcd == E_AMNTCD.PY || amntcd == E_AMNTCD.RV)) {
        	throw InModuleError.InAccount.IN030052();
        }

        //损益检查
        if (inptsr != E_INPTSR.GL08 && busitp == E_BUSITP._5 && (blncdn == E_BLNCDN.C || blncdn == E_BLNCDN.D)
                && !CommUtil.equals(blncdn.getValue(), amntcd.getValue())) {
        	throw InModuleError.InAccount.IN030053(info.getAcctno());
        }

        //汇总户检查
        if (totltg == E_TOTLTG._1
                && inptsr == E_INPTSR.GL01) {
        	throw InModuleError.InAccount.IN030054(info.getAcctno());
        }
        //记账检查 modify by chenlk 20161128  去除GL03的控制
        if (kpacfg == E_KPACFG._1
                && (inptsr == E_INPTSR.GL01)) {
            //增加不是busino记账报错
            if (CommUtil.isNull(input.getBusino()) && !BusiTools.isCZTrans()) {
            	throw InModuleError.InAccount.IN030055(info.getAcctno());
            }
        }
    }
    /**
     *处理挂销账登记簿 
     * @throws java.lang.Exception 
     */
	public static void dealPayDetail(IaAcdrInfo input, InacProInfo info) throws java.lang.Exception {

		// 冲正标志,默认为正交易
		E_CORRTG corrtg = CommUtil.nvl(input.getCorrtg(), E_CORRTG._0);

		if (corrtg == E_CORRTG._0) {
			// 正交易处理挂销账登记簿
			dealPayNormal(input, info);

		} else {
			// 冲正处理挂销账登记簿
			dealPayStrike(input, info);
		}

	}
    public static IaTransOutPro actionRecvPaysDeal(IaAcdrInfo input, InacProInfo info) {
        String acctno = input.getAcctno(); // 账号
        E_YES___ fg_ovcntl = CommUtil.nvl(input.getOvcntl(), E_YES___.NO); // 强制透支标志
        E_RLBLTG rlbltg = CommUtil.nvl(info.getRlbltg(), E_RLBLTG._1); // 实时余额标志
        E_CORRTG corrtg = CommUtil.nvl(input.getCorrtg(), E_CORRTG._0); // 冲账标志
        //String sttsdt = input.getSttsdt(); // 冲账日期
        //String sttssq = input.getSttssq(); // 冲账流水
        E_AMNTCD amntcd = input.getAmntcd(); // 交易方向
        E_BLNCDN busidn = info.getBusidn(); // 科目方向

        BigDecimal zero = BigDecimal.valueOf(0.00); // 初始金额零
        BigDecimal tranam = CommUtil.nvl(input.getTranam(), zero); // 交易金额
        BigDecimal onlnbl = zero; // 联机余额
        BigDecimal edctbl = zero; // 日终余额
        InEnumType.E_BLNCDN blncdn_old = info.getBlncdn(); // 账户余额方向
        String toacct = input.getToacct(); // 对方账号
        String toacna = input.getToacna(); // 对方户名
        String tosbac = input.getTosbac(); // 对方子户
        String dscrtx = CommUtil.nvl(input.getDscrtx(), BusiTools.getBusiRunEnvs().getRemark()); // 描述
        E_INPTSR inptsr = CommUtil.nvl(input.getInptsr(), E_INPTSR.GL01); //交易来源类别

        if (CommUtil.isNull(blncdn_old)) {
            blncdn_old = CommUtil.toEnum(E_BLNCDN.class, busidn);
        }

        //E_CRPSMD crpsmd = getCrpsMode(); // 余额变更模式 1正常额变更模式;2轧账期间余额变更模式
        E_CRPSMD crpsmd = E_CRPSMD.T_DAY;//没有总账系统，余额变更模式为 正常变更模式


        // 更新实时余额需要锁住账户当前状态
        GlKnaAcct AcctTmp = SysUtil.getInstance(GlKnaAcct.class);
        if (rlbltg == E_RLBLTG._1) {
            AcctTmp = GlKnaAcctDao.selectOneWithLock_odb1(input.getAcctno(), true);
        } else {
            AcctTmp = GlKnaAcctDao.selectOne_odb1(input.getAcctno(), true);
        }
        BigDecimal crctbl_new = AcctTmp.getCrctbl(); // 新联机贷方余额
        BigDecimal drctbl_new = AcctTmp.getDrctbl(); // 新联机借方余额
        BigDecimal credbl_new = AcctTmp.getCredbl(); // 新日终贷方余额
        BigDecimal dredbl_new = AcctTmp.getDredbl(); // 新日终借方余额
        //E_BLNCDN blncdn = AcctTmp.getBlncdn(); 		 // 账户余额方向
        E_YES___ ovdftg = AcctTmp.getPmodtg(); // 账户透支标志
        GlKnaLsbl AcctLsbl = GlKnaLsblDao.selectOne_odb1(acctno, true);

        E_BLNCDN lastdn = AcctLsbl.getLastdn();

        // 初始化联机余额、日终余额、上日余额
        if (busidn == E_BLNCDN.D || busidn == E_BLNCDN.R) {
            onlnbl = drctbl_new.subtract(crctbl_new); // 联机余额
            edctbl = dredbl_new.subtract(credbl_new); // 日终余额
        } else {
            onlnbl = crctbl_new.subtract(drctbl_new); // 联机余额
            edctbl = credbl_new.subtract(dredbl_new); // 日终余额
        }

        if (CommUtil.isNull(corrtg)) {
            corrtg = E_CORRTG._0;
        }
        if (CommUtil.isNull(ovdftg)) {
            ovdftg = E_YES___.NO;
        }

        if (fg_ovcntl == E_YES___.YES) {
            ovdftg = E_YES___.YES;
        }

        BigDecimal l_drctbl = zero; // 联机借方余额
        BigDecimal l_crctbl = zero; // 联机贷方余额
        BigDecimal l_dredbl = zero; // 当日借方余额
        BigDecimal l_credbl = zero; // 当日贷方余额
        BigDecimal tranbl = zero;
        String l_lstrdt = CommTools.getBaseRunEnvs().getTrxn_date();
        String l_lstrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
        E_BLNCDN l_blncdn = lastdn;
        if (rlbltg == E_RLBLTG._1) { // 实时余额情况处理
            if (busidn == E_BLNCDN.C) { // 账户科目方向为贷方处理
                // 确定余额方向
                l_blncdn = E_BLNCDN.C;
                // 确定借贷方联机余额
                l_drctbl = zero;
                if (amntcd == E_AMNTCD.CR || amntcd == E_AMNTCD.PY) {
                    l_crctbl = onlnbl.add(tranam);
                    tranbl = l_crctbl;
                } else {
                    l_crctbl = onlnbl.subtract(tranam);
                    tranbl = l_crctbl;
                }
                // 确定借贷方日终余额
                l_dredbl = zero;
                if (amntcd == E_AMNTCD.CR || amntcd == E_AMNTCD.PY) {
                    if (crpsmd == E_CRPSMD.T_DAY) {
                        l_credbl = edctbl.add(tranam);
                    } else {
                        l_credbl = edctbl;
                    }
                } else {
                    if (crpsmd == E_CRPSMD.T_DAY) {
                        l_credbl = edctbl.subtract(tranam);
                    } else {
                        l_credbl = edctbl;
                    }
                }
            } else if (busidn == E_BLNCDN.D) { // 账户科目方向为借方处理
                // 确定余额方向
                l_blncdn = E_BLNCDN.D;
                // 确定借贷方联机余额
                l_crctbl = zero;
                if (amntcd == E_AMNTCD.DR || amntcd == E_AMNTCD.RV) {
                    l_drctbl = onlnbl.add(tranam);
                    tranbl = l_drctbl;
                } else {
                    l_drctbl = onlnbl.subtract(tranam);
                    tranbl = l_drctbl;
                }
                // 确定借贷方日终余额
                l_credbl = zero;
                if (amntcd == E_AMNTCD.DR || amntcd == E_AMNTCD.RV) {
                    if (crpsmd == E_CRPSMD.T_DAY) {
                        l_dredbl = edctbl.add(tranam);
                    } else {
                        l_dredbl = edctbl;
                    }
                } else {
                    if (crpsmd == E_CRPSMD.T_DAY) {
                        l_dredbl = edctbl.subtract(tranam);
                    } else {
                        l_dredbl = edctbl;
                    }
                }
            } else if (busidn == E_BLNCDN.Z || busidn == E_BLNCDN.B) { // 账户科目方向为轧差处理

                if (amntcd == E_AMNTCD.DR || amntcd == E_AMNTCD.RV) { // 交易方向为借方、收方相同处理
                    // 轧差确定联机余额和余额方向
                    if ((onlnbl.subtract(tranam).compareTo(zero) > 0)) {
                        l_crctbl = onlnbl.subtract(tranam);
                        l_blncdn = E_BLNCDN.C;
                        l_drctbl = zero;
                        tranbl = l_crctbl;
                    } else {
                        l_drctbl = tranam.subtract(onlnbl);
                        l_blncdn = E_BLNCDN.D;
                        l_crctbl = zero;
                        tranbl = l_drctbl;
                    }
                    // 轧差确定日终余额
                    if ((edctbl.subtract(tranam).compareTo(zero) > 0)) {
                        if (crpsmd == E_CRPSMD.T_DAY) {
                            l_credbl = edctbl.subtract(tranam);
                            l_dredbl = zero;
                        } else {
                            l_credbl = edctbl;
                            l_dredbl = zero;
                        }
                    } else {
                        if (crpsmd == E_CRPSMD.T_DAY) {
                            l_dredbl = tranam.subtract(edctbl);
                            l_credbl = zero;
                        } else {
                            l_dredbl = zero.subtract(edctbl);
                            l_credbl = zero;
                        }
                    }
                } else { // 交易方向为贷方、付方相同处理
                    // 轧差确定联机余额和余额方向
                    if ((onlnbl.add(tranam).compareTo(zero) > 0)) {
                        l_crctbl = onlnbl.add(tranam);
                        l_blncdn = E_BLNCDN.C;
                        l_drctbl = zero;
                        tranbl = l_crctbl;
                    } else {
                        l_drctbl = onlnbl.add(tranam);
                        l_drctbl = zero.subtract(l_drctbl);
                        l_blncdn = E_BLNCDN.D;
                        l_crctbl = zero;
                        tranbl = l_drctbl;
                    }
                    // 轧差确定日终余额
                    if ((edctbl.add(tranam).compareTo(zero) > 0)) {
                        if (crpsmd == E_CRPSMD.T_DAY) {
                            l_credbl = edctbl.add(tranam);
                            l_dredbl = zero;
                        } else {
                            l_credbl = edctbl;
                            l_dredbl = zero;
                        }
                    } else {
                        if (crpsmd == E_CRPSMD.T_DAY) {
                            l_dredbl = edctbl.add(tranam);
                            l_dredbl = zero.subtract(l_dredbl);
                            l_credbl = zero;
                        } else {
                            l_dredbl = zero.subtract(edctbl);
                            l_credbl = zero;
                        }
                    }
                }

            } else if (busidn == E_BLNCDN.R) { // 账户科目方向为收方处理
                // 确定余额方向
                l_blncdn = E_BLNCDN.R;
                // 确定借贷方联机余额
                l_crctbl = zero;
                if (amntcd == E_AMNTCD.DR || amntcd == E_AMNTCD.RV) {
                    l_drctbl = onlnbl.add(tranam);
                    tranbl = l_drctbl;
                } else {
                    l_drctbl = onlnbl.subtract(tranam);
                    tranbl = l_drctbl;
                }
                // 确定借贷方日终余额
                l_credbl = zero;
                if (amntcd == E_AMNTCD.DR || amntcd == E_AMNTCD.RV) {
                    if (crpsmd == E_CRPSMD.T_DAY) {
                        l_dredbl = edctbl.add(tranam);
                    } else {
                        l_dredbl = edctbl;
                    }
                } else {
                    if (crpsmd == E_CRPSMD.T_DAY) {
                        l_dredbl = edctbl.subtract(tranam);
                    } else {
                        l_dredbl = edctbl;
                    }
                }
            } else {
                throw InError.comm.E0003("账户余额方向错误");

            }
            // 检查透支
            bizlog.debug("=======余额：" + tranbl, "");
            if (ovdftg == E_YES___.NO && (busidn == E_BLNCDN.D || busidn == E_BLNCDN.C || busidn == E_BLNCDN.R)) {
                if (CommUtil.compare(tranbl, zero) < 0 && (inptsr == E_INPTSR.GL01 || inptsr == E_INPTSR.GL03)) {
                    throw InError.comm.E0003("账户[" + acctno + "]可用余额不足");
                }
            }
            //账户允许透支，透支超过透支限额，抛错,透支限额为0相当于不可透支 modify by chenlk 20161024
            if (ovdftg == E_YES___.YES&& (busidn == E_BLNCDN.D || busidn == E_BLNCDN.C || busidn == E_BLNCDN.R)) {
                if (CommUtil.compare(tranbl, zero) < 0 &&CommUtil.compare(tranbl.abs(), AcctTmp.getOvmony()) > 0
                		&& (inptsr == E_INPTSR.GL01 || inptsr == E_INPTSR.GL03)) {
                    throw InError.comm.E0003("账户[" + acctno + "]可用余额不足");
                }
            }            
            // 调用API更新账户余额
            updateGlAcctBalance(acctno, l_blncdn, l_crctbl, l_drctbl, l_credbl, l_dredbl,
                    l_lstrdt, l_lstrsq);

        } else if (rlbltg == E_RLBLTG._2) {// 非实时余额处理
            //登记实时交易发生汇总表
            BigDecimal regiam = zero;
            if (amntcd == E_AMNTCD.DR || amntcd == E_AMNTCD.RV) {
                regiam = tranam;
            } else {
                regiam = zero.subtract(tranam);
            }
            GlKnsRlbl rlbl = SysUtil.getInstance(GlKnsRlbl.class);
            String sessid = BusiTools.getHostNameThread();
            // 按交易sessid号更新交易发生额			
            rlbl = queryGlRlbl(CommTools.getBaseRunEnvs().getTrxn_date(), acctno, sessid);
            if (CommUtil.isNull(rlbl.getAcctno())) {
                rlbl.setAcctno(acctno);
                rlbl.setSessid(sessid);
                rlbl.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
                rlbl.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
                rlbl.setTranam(regiam);
                insertGlRlbl(rlbl);
            } else {
                updateGlRlbl(CommTools.getBaseRunEnvs().getTrxn_date(), acctno, sessid, CommTools.getBaseRunEnvs().getMain_trxn_seq(),
                        regiam);
            }
         
            BigDecimal totlam = zero;
            // 统计账户的所有sessid发生额，用于判断账户是否透支
            totlam = querySumRlbl(CommTools.getBaseRunEnvs().getTrxn_date(), acctno);
            if (busidn == E_BLNCDN.D) {
                tranbl = onlnbl.add(totlam);// modify by chenlk 非实时余额当前账户余额和上日余额实际是一致的，兼容修改实时余额改成非实时余额做出改造
            } else {
                tranbl = onlnbl.subtract(totlam);
            }
            // 检查透支
            if (ovdftg == E_YES___.NO && (busidn == E_BLNCDN.D || busidn == E_BLNCDN.C)) {
                if (CommUtil.compare(tranbl, zero) < 0) {
                    throw InError.comm.E0003("账户[" + acctno + "]可用余额[" + tranbl + "]不足");
                }
            }

            //账户允许透支，透支超过透支限额，抛错,透支限额为0相当于不可透支 modify by chenlk 20161024
            if (ovdftg == E_YES___.YES&& (busidn == E_BLNCDN.D || busidn == E_BLNCDN.C || busidn == E_BLNCDN.R)) {
                if (CommUtil.compare(tranbl, zero) < 0 &&CommUtil.compare(tranbl.abs(), AcctTmp.getOvmony()) > 0
                		&& (inptsr == E_INPTSR.GL01 || inptsr == E_INPTSR.GL03)) {
                    throw InError.comm.E0003("账户[" + acctno + "]可用余额不足");
                }
            }             
            
            
            //非实时余额统一登记成0,余额方向为上日余额方向
            tranbl = zero;
        } else {
            throw InError.comm.E0003("实时余额标志错误[" + rlbltg + "]");
        }
        
        //录入传票
        String glvcsq = DoInsertGlvc(amntcd, l_blncdn, tranam, tranbl, corrtg, toacct, toacna, tosbac, dscrtx, inptsr, "", AcctTmp, input.getAutotg(),input.getAcuttp());

        IaTransOutPro pro = SysUtil.getInstance(IaTransOutPro.class);
        pro.setAcctno(acctno);
        pro.setAcctna(info.getAcctna());
        pro.setBlncdn(l_blncdn);
        pro.setCrctbl(l_crctbl);
        pro.setDrctbl(l_drctbl);
        pro.setTranam(tranam);
        pro.setPayasqlist(input.getPayasqlist());

        //登记会计流水
        IoAccounttingIntf cplIoAccounttingIntf = SysUtil
                .getInstance(IoAccounttingIntf.class);
        cplIoAccounttingIntf.setCorpno(AcctTmp.getCorpno());
        cplIoAccounttingIntf.setCuacno(acctno);
        cplIoAccounttingIntf.setAcctno(acctno);
        cplIoAccounttingIntf.setAcseno(acctno);
        cplIoAccounttingIntf.setProdcd(AcctTmp.getBusino());//内部户产品
        cplIoAccounttingIntf.setDtitcd(AcctTmp.getBusino());
        cplIoAccounttingIntf.setCrcycd(AcctTmp.getCrcycd());
        cplIoAccounttingIntf.setTranam(tranam);
        cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
        cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
        cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
        cplIoAccounttingIntf.setAcctbr(AcctTmp.getBrchno());//账务机构
        cplIoAccounttingIntf.setAmntcd(amntcd);
        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.IN);
        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
        cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE);
        cplIoAccounttingIntf.setToacct(input.getToacct());//对方账号
        cplIoAccounttingIntf.setToacna(input.getToacna());//对方户名
        
        //登记交易信息，供总账解析
        if(CommUtil.equals("1", KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",true).getParm_value1())){
        	KnpParameter para1 = SysUtil.getInstance(KnpParameter.class);
        	para1 = KnpParameterDao.selectOne_odb1("GlAnalysis", "1010000", "%", "%",true);//产品增加
        	KnpParameter para2 = SysUtil.getInstance(KnpParameter.class);
        	para2 = KnpParameterDao.selectOne_odb1("GlAnalysis", "1020000", "%", "%",true);//产品减少
            if (amntcd == E_AMNTCD.DR && info.getBusidn()==E_BLNCDN.D) {
            	
            	cplIoAccounttingIntf.setTranms(para1.getParm_value1());//产品增加
            	
            }else if (amntcd == E_AMNTCD.DR && info.getBusidn()==E_BLNCDN.C){
            	
            	cplIoAccounttingIntf.setTranms(para2.getParm_value1());//产品减少
            	
            }else if(amntcd == E_AMNTCD.CR && info.getBusidn()==E_BLNCDN.C){
            	
            	cplIoAccounttingIntf.setTranms(para1.getParm_value1());//产品增加
            	
            }else if (amntcd == E_AMNTCD.CR && info.getBusidn()==E_BLNCDN.D){
            	
            	cplIoAccounttingIntf.setTranms(para2.getParm_value1());//产品减少
            	
            }else if(amntcd == E_AMNTCD.RV && info.getBusidn()==E_BLNCDN.R){
            	
            	cplIoAccounttingIntf.setTranms(para1.getParm_value1());//产品增加
            	
            }else if (amntcd == E_AMNTCD.PY && info.getBusidn()==E_BLNCDN.R){
            	
            	cplIoAccounttingIntf.setTranms(para2.getParm_value1());//产品减少
            	
            }else if(amntcd == E_AMNTCD.DR && info.getBusidn()==E_BLNCDN.Z){
            	
            	cplIoAccounttingIntf.setTranms(para1.getParm_value1());//产品增加
            	
            }else if (amntcd == E_AMNTCD.CR && info.getBusidn()==E_BLNCDN.Z){
            	
            	cplIoAccounttingIntf.setTranms(para2.getParm_value1());//产品减少
            	
            }else if(amntcd == E_AMNTCD.DR && info.getBusidn()==E_BLNCDN.B){
            	
            	cplIoAccounttingIntf.setTranms(para1.getParm_value1());//产品增加
            	
            }else if (amntcd == E_AMNTCD.CR && info.getBusidn()==E_BLNCDN.B){
            	
            	cplIoAccounttingIntf.setTranms(para2.getParm_value1());//产品减少
            	
            }else{
            	 throw InError.comm.E0003("账户余额方向错误");
            }        	
        }        
        cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//渠道       


        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                cplIoAccounttingIntf);
        if(E_YES___.NO != input.getStrktg()){
        	
        	//冲正注册		
        	IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
        	cplInput.setTranam(input.getTranam());
        	cplInput.setTranac(input.getAcctno());
        	cplInput.setEvent1(glvcsq); //传票流水
        	cplInput.setEvent2(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //主交易流水
        	cplInput.setCrcycd(input.getCrcycd());
        	cplInput.setAmntcd(amntcd);
        	cplInput.setBgindt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
        	
        	IoMsRegEvent ioMsReg = SysUtil.getInstance(IoMsRegEvent.class);    		
        	ioMsReg.setInformation_value(SysUtil.serialize(cplInput));
    		ioMsReg.setCall_out_seq(CommTools.getBaseRunEnvs().getCall_out_seq());//外调流水
//    		ioMsReg.setConfirm_event_id("");//二提交事件id
//    		ioMsReg.setEvent_status(E_EVNTST.SUCCESS);//事件状态SUCCESS（成功）STRIKED（已冲正）NEED2C（需要二次提交）
    		if (input.getAmntcd() == E_AMNTCD.CR) {
        		ioMsReg.setReversal_event_id("strkeInAccr");//冲正事件ID
        		ioMsReg.setService_id("strkeInAccr");//服务ID
        	} else if (input.getAmntcd() == E_AMNTCD.DR) {
        		ioMsReg.setReversal_event_id("strkeInAcdr");//冲正事件ID
        		ioMsReg.setService_id("strkeInAcdr");//服务ID
        	}
//        	if (input.getAmntcd() == E_AMNTCD.PY) {
//        		ioMsReg.setReversal_event_id("IoInAcpv");//冲正事件ID
//        		ioMsReg.setService_id("IoInAcpv");//服务ID
//        	}
//        	else if (input.getAmntcd() == E_AMNTCD.RV) {
//        		ioMsReg.setReversal_event_id("ioInAcrv");//冲正事件ID
//        		ioMsReg.setService_id("ioInAcrv");//服务ID
//        	}
    		ioMsReg.setSub_system_id(CoreUtil.getSubSystemId());//子系统ID
//    		ioMsReg.setTarget_dcn(CoreUtil.getCurrentShardingId());//目标DCB编号
//    		ioMsReg.setTarget_org_id("025");//目标法人
    		ioMsReg.setTxn_event_level(E_EVENTLEVEL.LOCAL);//教义事件级别NORMAL（）INQUIRE（）LOCAL（）CRDIT（）
    		ioMsReg.setIs_across_dcn(E_YESORNO.NO);
    		
    		MsEvent.register(ioMsReg, true);
        }
        return pro;
    }

    private static String DoInsertGlvc(E_AMNTCD amntcd, E_BLNCDN l_blncdn,
            BigDecimal tranam, BigDecimal tranbl, E_CORRTG corrtg,
            String toacct, String toacna, String tosbac, String dscrtx,
            E_INPTSR inptsr, String string, GlKnaAcct acctTmp, E_YES___ autotg,E_ACUTTP acuttp) {
        E_TRANTP trantp = E_TRANTP.TR;
        //默认是正常交易
        if(CommUtil.isNull(acuttp)){
        	acuttp=E_ACUTTP._1;
        }
        //冲正交易
        if(corrtg==E_CORRTG._1){
        	acuttp=E_ACUTTP._2;
        }
        GlKnsGlvc glvc = SysUtil.getInstance(GlKnsGlvc.class);
        //String glvcsq = SequenceManager.nextval("gl_kns_glvc");
        String glvcsq = CoreUtil.nextValue("gl_kns_glvc");
        glvc.setAcctno(acctTmp.getAcctno());
        glvc.setAmntcd(amntcd);
        glvc.setBlncdn(l_blncdn);
        glvc.setBrchno(acctTmp.getBrchno());
        glvc.setCkbsus(BusiTools.getBusiRunEnvs().getCkbsus());
        glvc.setCrcycd(acctTmp.getCrcycd());
        glvc.setGlvcsq(glvcsq);
        glvc.setInptsr(inptsr);
        glvc.setIoflag(acctTmp.getIoflag());
        glvc.setBusino(acctTmp.getBusino());
        glvc.setKpacbr(acctTmp.getKpacbr());
        glvc.setStacno(CommTools.getBaseRunEnvs().getTrxn_seq());
        glvc.setStacsq(0);
        glvc.setStatus(E_GLTRST._1);//
        glvc.setTranam(tranam);
        glvc.setTranbl(tranbl);
        glvc.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
        glvc.setTrannm(1);
        glvc.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());
        glvc.setTrantp(trantp);
        glvc.setAcuttp(acuttp);
        glvc.setAcctbr(acctTmp.getBrchno());
        glvc.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());
        /*if (autotg == E_YES___.YES) {
            glvc.setAcctbr(acctTmp.getBrchno());
            glvc.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());
        }
        else {
            glvc.setAcctbr(acctTmp.getBrchno());
            glvc.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());
        }*/
        glvc.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());
        glvc.setBkfnst(E_BKFNST._1);
        glvc.setCmmdsq(glvcsq);
        glvc.setRlbltg(acctTmp.getRlbltg());
        if (acctTmp.getRlbltg() == E_RLBLTG._1) {
            glvc.setCkvcfg(E_CKVCFG._1);
        }
        else {
            glvc.setCkvcfg(E_CKVCFG._0);
        }
        glvc.setToacct(toacct);
        glvc.setToacna(toacna);
        glvc.setTosbac(tosbac);
        glvc.setSmrytx(dscrtx);
        glvc.setExacct(acctTmp.getAcctno());
        //add by wuzx 20161227 -beg
        glvc.setUssqno(CommTools.getBaseRunEnvs().getTrxn_seq());//业务跟踪流水
        glvc.setUssrdt(CommTools.getBaseRunEnvs().getTrxn_date());//柜员日期
        glvc.setAuttel(BusiTools.getBusiRunEnvs().getAuthvo().getAuthus());//授权柜员
      //add by wuzx 20161227 -end
        GlKnsGlvcDao.insert(glvc);
        return glvcsq;
    }

    private static BigDecimal querySumRlbl(String trandt, String acctno) {
        BigDecimal tranam = BigDecimal.valueOf(0.00);
        Map<String, Object> ResultMap;
        
        ResultMap = InacSqlsDao.querySumRlbl(trandt, acctno, true);
        if (CommUtil.isNotNull(ResultMap)) {
            tranam = ConvertUtil.toBigDecimal(ResultMap.get("tranam"));
        }


        return tranam;
    }

    private static void updateGlRlbl(String trandt, String acctno,
            String sessid, String transq, BigDecimal regiam) {
        try {
            int count = InacSqlsDao.updateGlRlbl(trandt, acctno, sessid, transq, regiam,CommTools.getBaseRunEnvs().getBusi_org_id());
            if (count != 1) {
                throw InError.comm.E0003("更新非实时交易错误");
            }
        } catch (Exception e) {
            throw InError.comm.E0003("更新非实时交易错误[" + acctno + "]");
        }

    }

    private static void insertGlRlbl(GlKnsRlbl rlbl) {
        try {
//            InacSqlsDao.insertGlRlbl(rlbl);
            GlKnsRlblDao.insert(rlbl);
        } catch (Exception e) {
            throw InError.comm.E0003("登记非实时交易汇总信息失败,主键重复!");
        }
    }

    private static GlKnsRlbl queryGlRlbl(String trandt, String acctno,
            String sessid) {
        GlKnsRlbl rlbl = SysUtil.getInstance(GlKnsRlbl.class);

        Map<String, Object> KnsRlblMap = InacSqlsDao.queryGlRlbl(trandt, acctno, sessid,CommTools.getBaseRunEnvs().getBusi_org_id(), false);
        if (CommUtil.isNotNull(KnsRlblMap)) {
            CommUtil.copyProperties(rlbl, KnsRlblMap);
        } //else {
          //  throw InError.comm.E0003("查询账号出现异常错误");
        //}

        return rlbl;
    }

    /*
    private static void updateGlBillBala(String sttsdt, String acctno,
    		String billsq, String stblsq, BigDecimal tranam) {
    	try {
    		int count = InacSqlsDao.updateGlBillBala(sttsdt, acctno, billsq, stblsq, tranam);
    		if (count != 1) {
    			throw InError.comm.E0003("更新账单错误!");
    		}
    	} catch (Exception e) {
    		throw InError.comm.E0003("更新账单错误["+acctno+"]");
    	}	
    }
    */

    /*
    private static String doInsertGlBill(E_AMNTCD amntcd, BigDecimal tranam,
    		BigDecimal tranbl, E_BLNCDN l_blncdn, E_CORRTG corrtg,
    		String toacct, String toacna, String tosbac, String dscrtx,
    		E_SMRYCD smrycd, GlKnaAcct acctTmp) {
    	E_TRANTP trantp = E_TRANTP.TR;
    	GlKnlBill Bill = SysUtil.getInstance(GlKnlBill.class);
    	String billsq = Long.parseLong(SequenceManager.nextval("gl_knl_bill"))+"";
    	Bill.setBrchno(acctTmp.getBrchno());
    	Bill.setAcctno(acctTmp.getAcctno());
    	Bill.setAmntcd(amntcd);
    	Bill.setBillsq(billsq);
    	Bill.setBkusid(CommTools.getBaseRunEnvs().getTrxn_teller());
    	// Bill.setCheqno(cheqno);
    	// Bill.setCheqtp(cheqtp);
    	// Bill.setCqtpid(cqtpid);
    	Bill.setCkbkus(CommTools.getBaseRunEnvs().getTrxn_teller()); //复查柜员 TODO
    	Bill.setCorrtg(corrtg);
    	Bill.setCrcycd(acctTmp.getCrcycd());
    	Bill.setDscrtx(dscrtx);
    	Bill.setSmrycd(smrycd);
    	Bill.setToacct(toacct);
    	Bill.setToacna(toacna);
    	Bill.setTosbac(tosbac);
    	Bill.setTranam(tranam);
    	Bill.setTranbl(tranbl);
    	Bill.setBlncdn(l_blncdn);
    	Bill.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());
    	Bill.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
    	Bill.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
    	Bill.setTrantp(trantp);
    	try {
    		GlKnlBillDao.insert(Bill);
    	} catch (Exception e) {
    		throw InError.comm.E0003("登记非实时交易汇总信息失败,主键重复!");
    	}
    	return billsq;
    }
    */

    public static E_CRPSMD getCrpsMode() {

        E_CRPSMD crpsmd = E_CRPSMD.T_DAY; // 余额变更模式 1正常额变更模式;2轧账期间余额变更模式

        // 取交易日期	
        String trandt = CommTools.getBaseRunEnvs().getTrxn_date();

        // 取总账交易日期
        String gltrdt = GlDateTools.getGlDateInfo().getSystdt();

        // 核心交易日期大于总账日期,联机余额变，日终余额不变
        // 核心交易日期等于总账日期,联机余额变，日终余额变
        // 核心交易日期小于总账日期,不能记账
        if (CommUtil.compare(trandt, gltrdt) > 0)
            crpsmd = E_CRPSMD.T_SETTLE;
        else if (CommUtil.compare(trandt, gltrdt) == 0)
            crpsmd = E_CRPSMD.T_DAY;
        else
            throw InError.comm.E0003("交易日期[" + trandt + "]不能小于总账日期[" + gltrdt + "]!");
        
        bizlog.debug("核心日期总账日期判断crpsmd =(" + crpsmd.toString() +")-["+trandt+","+gltrdt+"]" );
        
        return crpsmd;
    }

    public static void updateGlAcctBalance(String acctno, E_BLNCDN blncdn,
            BigDecimal crctbl, BigDecimal drctbl, BigDecimal credbl,
            BigDecimal dredbl, String lstrdt, String lstrsq)
    {
        try {
        	String timetm =DateTools2.getCurrentTimestamp();
        	
            int count = InacSqlsDao.updateGlAcctBalance(acctno, blncdn, crctbl, drctbl, credbl, dredbl, lstrdt, lstrsq, timetm);
            if (count != 1) {
                throw InError.comm.E0003("更新账号余额错误[" + acctno + "]");
            }
        } catch (Exception e) {
            throw InError.comm.E0003("更新账号余额错误[" + acctno + "]");
        }
    }

    public static String updateGlBill(String sttsdt, String sttssq, String acctno, E_AMNTCD amntcd, BigDecimal tranam, E_CORRTG corrtg) throws Exception {
        String stblsq;
        try {
            stblsq = InacSqlsDao.queryGlBill(sttsdt, sttssq, acctno, amntcd, tranam, true);
        } catch (Exception e) {
            throw InError.comm.E0003("查询账单错[" + acctno + "]");
        }
        try {
            int count = InacSqlsDao.updateGlBill(sttsdt, sttssq, acctno, amntcd, tranam, corrtg);
            if (count != 1) {
                throw InError.comm.E0003("更新账单错误");
            }
        } catch (Exception e) {
            throw InError.comm.E0003("更新账单错误[" + acctno + "]");
        }

        return stblsq;
    }
    /**
     * 
     * @Title: dealPayNormal 
     * @Description: 挂销账正交易处理 
     * @param input
     * @param info
     * @return
     * @throws Exception
     * @author chenlinkang
     * @date 2016年7月7日 下午5:07:17 
     * @version V2.3.0
     */
    public static void dealPayNormal(IaAcdrInfo input, InacProInfo info) throws Exception {
    	
    	E_ISPAYA ispaya =  info.getIspaya();//是否挂销账管理
    	E_BLNCDN blncdn = info.getBlncdn();//余额方向
    	E_AMNTCD amntcd = input.getAmntcd();//记账方向
    	Options<IaPayaDetail> payaDetail = input.getPayadetail();//挂账明细
    	Options<IaPaydDetail> paydDetail = input.getPayddetail();//销账明细
    	
		if(E_ISPAYA._1==ispaya &&
				((CommUtil.equals(blncdn.getValue(), amntcd.getValue())&&CommUtil.compare(input.getTranam(), BigDecimal.ZERO)>0)
						||(!CommUtil.equals(blncdn.getValue(), amntcd.getValue())&&CommUtil.compare(input.getTranam(), BigDecimal.ZERO)<0))){
			//挂账,如果未传入挂账明细，则新生成并登记
			if(null==payaDetail||payaDetail.size()==0){
				if (CommUtil.equals("EB", CommTools.getBaseRunEnvs().getChannel_id())) {
					throw InError.comm.E0003("统一后管系统挂账，必须录入挂账信息！");
				}
				KnsPaya knspaya = SysUtil.getInstance(KnsPaya.class);
				knspaya.setPayaac(info.getAcctno());//挂账账号
				knspaya.setPayabr(info.getBrchno());//挂账机构
				knspaya.setPayamn(input.getTranam().abs());//挂账金额

				String payasq= MsSeqUtil.genSeq("PAYASQ", CommTools.getBaseRunEnvs().getTrxn_date());
				
				knspaya.setPayasq(payasq);
				knspaya.setPayast(E_PAYAST.ZC);//挂账状态
				knspaya.setRsdlmn(input.getTranam().abs());//剩余挂账金额
				knspaya.setToacct(input.getToacct());//对方账号
				knspaya.setToacna(input.getToacna());//对方户名
				knspaya.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
				knspaya.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());//交易流水
				knspaya.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());//交易柜员   	
				knspaya.setCrcycd(input.getCrcycd());//币种
				knspaya.setPaydnm(0);//销账笔数
				knspaya.setTemp01(input.getDscrtx());//备注
				KnsPayaDao.insert(knspaya);
				IaPayasqList cplPayasq = SysUtil.getInstance(IaPayasqList.class);
				cplPayasq.setPayasq(knspaya.getPayasq());
				input.getPayasqlist().add(cplPayasq);
			}else{
				BigDecimal payaam = BigDecimal.ZERO;//挂账账总金
				for(IaPayaDetail list :payaDetail){
					String payasq = list.getPayasq();
					
					//存在挂账记录则更新状态 （柜面内部户转出交易）
					KnsPaya paya = SysUtil.getInstance(KnsPaya.class);
						
					paya = KnsPayaDao.selectOne_kns_paya_odx1(payasq, false);
					
					if(CommUtil.isNotNull(paya)&&CommUtil.isNotNull(paya.getPayasq())){
						
						
						paya.setPayast(E_PAYAST.ZC);
						paya.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
						paya.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());//交易流水
						paya.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());//交易柜员   							
						KnsPayaDao.updateOne_kns_paya_odx1(paya);	
						payaam=payaam.add(list.getPayamn());
						IaPayasqList cplPayasq = SysUtil.getInstance(IaPayasqList.class);
						cplPayasq.setPayasq(paya.getPayasq());
						input.getPayasqlist().add(cplPayasq);						
						
					}else{
						//根据挂账明细进行挂账
						KnsPaya knspaya = SysUtil.getInstance(KnsPaya.class);
						knspaya.setPayaac(info.getAcctno());//挂账账号
						knspaya.setPayabr(info.getBrchno());//挂账机构
						knspaya.setPayamn(list.getPayamn());//挂账金额
						
						payasq= MsSeqUtil.genSeq("PAYASQ", CommTools.getBaseRunEnvs().getTrxn_date());
						
						knspaya.setPayasq(payasq);//生成方法待定,测试暂设
						knspaya.setPayast(E_PAYAST.ZC);//挂账状态
						knspaya.setRsdlmn(list.getPayamn());//剩余挂账金额
						knspaya.setToacct(list.getToacno());//对方账号
						knspaya.setToacna(list.getToacna());//对方户名
						knspaya.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
						knspaya.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());//交易流水
						knspaya.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());//交易柜员   	
						knspaya.setCrcycd(input.getCrcycd());//币种
						knspaya.setPaydnm(0);//销账笔数
						knspaya.setTemp01(input.getDscrtx());//备注
						KnsPayaDao.insert(knspaya);
						payaam=payaam.add(list.getPayamn());
						IaPayasqList cplPayasq = SysUtil.getInstance(IaPayasqList.class);
						cplPayasq.setPayasq(knspaya.getPayasq());
						input.getPayasqlist().add(cplPayasq);		
						
					}
					
					
				}
				if(CommUtil.compare(payaam, input.getTranam().abs())!=0){
					throw InError.comm.E0003("挂账金额必须等于交易金额！");
				}
			}
		}else if(E_ISPAYA._1==ispaya 
				&&((!CommUtil.equals(blncdn.getValue(), amntcd.getValue())&&CommUtil.compare(input.getTranam(), BigDecimal.ZERO)>0)
						||(CommUtil.equals(blncdn.getValue(), amntcd.getValue())&&CommUtil.compare(input.getTranam(), BigDecimal.ZERO)<0))){
			//销账 如果未传入销账明细，则新生成并登记
			if(null==paydDetail||paydDetail.size()==0){
				throw InError.comm.E0003("销账必须录入销账信息！");
			}else{
				BigDecimal paydam = BigDecimal.ZERO;//销账总金
				for(IaPaydDetail payd:paydDetail){
					KnsPayd knspayd = SysUtil.getInstance(KnsPayd.class);
					knspayd = KnsPaydDao.selectOne_kns_payd_odx1(payd.getPaydsq(), false);					
					if(CommUtil.isNotNull(knspayd)){//统一后管录入后 发起记账
						
						KnsPaya paya = KnsPayaDao.selectOne_kns_paya_odx1(payd.getPrpysq(), true);
						
						if(CommUtil.compare(payd.getPaydmn(), paya.getRsdlmn())>0){
							throw InError.comm.E0003("剩余挂账金额["+payd.getPaydmn()+"]小于本次销账金额["+paya.getRsdlmn()+"]！");
						}
						
						if(CommUtil.compare(payd.getPaydmn(), paya.getRsdlmn())==0){
							paya.setPayast(E_PAYAST.JQ);
						}else if (CommUtil.compare(paya.getRsdlmn(), payd.getPaydmn())<0){
							throw InError.comm.E0003("挂账序号["+paya.getPayasq()+"]下剩余挂账金额["+paya.getRsdlmn()+"]小于销账序号["+payd.getPaydsq()+"]下销账金额"
									+payd.getPaydmn()+"],请检查！");
						}
						paya.setRsdlmn(paya.getRsdlmn().subtract(payd.getPaydmn()));

						if(!CommUtil.equals(paya.getPayaac(), info.getAcctno())){
							throw InError.comm.E0003("挂账账号与交易账号不符，请检查！");
						}
						paya.setPaydnm(paya.getPaydnm()+1);
						KnsPayaDao.updateOne_kns_paya_odx1(paya);//更新挂账登记簿记录
						//销账处理

						knspayd.setPaydst(E_PAYDST.ZC);
						knspayd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
						knspayd.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());
						knspayd.setUntius(CommTools.getBaseRunEnvs().getTrxn_teller());//更新销账柜员
						KnsPaydDao.updateOne_kns_payd_odx1(knspayd);//更新销账登记簿记录
						paydam = paydam.add(payd.getPaydmn());

					} else{//后台销账
						knspayd = SysUtil.getInstance(KnsPayd.class);
						String paydsq = MsSeqUtil.genSeq("PAYDSQ", CommTools.getBaseRunEnvs().getTrxn_date());
						knspayd.setPaydsq(paydsq);
						if(CommUtil.isNull(payd.getPrpysq())){
							throw InError.comm.E0003("销账时挂账序号不能为空！");
						}else{
							knspayd.setPayasq(payd.getPrpysq());     					
						}
						KnsPaya paya = KnsPayaDao.selectOne_kns_paya_odx1(payd.getPrpysq(), true);
						if(!CommUtil.equals(paya.getPayaac(), info.getAcctno())){
							throw InError.comm.E0003("挂账账号与交易账号不符，请检查！");
						}
						knspayd.setTotlmn(paya.getRsdlmn());//原挂账金额        				
						if(CommUtil.compare(payd.getPaydmn(), BigDecimal.ZERO)<=0){
							knspayd.setPayamn(paya.getRsdlmn());//如果未传入销账金额默认销所有剩余挂账金额
							knspayd.setRsdlmn(BigDecimal.ZERO);//剩余挂账金额
							paya.setRsdlmn(BigDecimal.ZERO);//挂账登记簿剩余挂账金额
							paya.setPayast(E_PAYAST.JQ);
							
						}else{
							knspayd.setPayamn(payd.getPaydmn());
							if(CommUtil.compare(paya.getRsdlmn(), payd.getPaydmn())==0){
								paya.setPayast(E_PAYAST.JQ);
							}else if (CommUtil.compare(paya.getRsdlmn(), payd.getPaydmn())<0){
								throw InError.comm.E0003("挂账序号["+paya.getPayasq()+"]剩余挂账金额["+paya.getRsdlmn()+"]小于销账序号["+payd.getPaydsq()+"]下销账金额"
										+payd.getPaydmn()+"],请检查！");
							}
							knspayd.setRsdlmn(paya.getRsdlmn().subtract(payd.getPaydmn()));//销账登记簿剩余挂账金额
							paya.setRsdlmn(paya.getRsdlmn().subtract(payd.getPaydmn()));//挂账登记簿剩余挂账金额
							
						}    
						paya.setPaydnm(paya.getPaydnm()+1);
						
						knspayd.setPaydac(CommUtil.nvl(payd.getPaydac(), info.getAcctno()));//销账账号
						knspayd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
						knspayd.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());
						knspayd.setUntius(CommTools.getBaseRunEnvs().getTrxn_teller());
						knspayd.setPaydst(E_PAYDST.ZC);	//销账状态        			
						knspayd.setPaydbr(CommTools.getBaseRunEnvs().getTrxn_branch());//销账机构
						knspayd.setPayabr(paya.getPayabr());//挂账机构
						knspayd.setToacct(input.getToacct());//对方账号
						knspayd.setToacna(input.getToacna());//对方户名
						knspayd.setTemp01(input.getDscrtx());//备注
						KnsPayaDao.updateOne_kns_paya_odx1(paya);//更新挂账登记簿记录
						KnsPaydDao.insert(knspayd);//插入销账登记簿
						paydam = paydam.add(knspayd.getPayamn());
					}
					
				}
				if(CommUtil.compare(paydam, input.getTranam().abs())!=0){
					throw InError.comm.E0003("销账金额必须等于交易金额！");
				}
				
			}
			
		}
    }
    /**
     * 
     * @Title: dealPayNormal 
     * @Description: 挂销账正交易处理 
     * @param input
     * @param info
     * @return
     * @throws Exception
     * @author chenlinkang
     * @date 2016年7月7日 下午5:07:17 
     * @version V2.3.0
     */
    public static void dealPayStrike(IaAcdrInfo input, InacProInfo info) throws Exception {
    	
    	E_ISPAYA ispaya =  info.getIspaya();//是否挂销账管理
    	E_BLNCDN blncdn = info.getBlncdn();//余额方向
    	E_AMNTCD amntcd = input.getAmntcd();//记账方向
    	String sttsdt = input.getSttsdt();//冲正日期
    	String sttssq = input.getSttssq();//冲正流水   	
    	
    	//挂账,修改挂账登记簿信息
		if(E_ISPAYA._1==ispaya &&((CommUtil.equals(blncdn.getValue(), amntcd.getValue())&&CommUtil.compare(input.getTranam(), BigDecimal.ZERO)<0)
				||(!CommUtil.equals(blncdn.getValue(), amntcd.getValue())&&CommUtil.compare(input.getTranam(), BigDecimal.ZERO)>0))){
				
		 List<KnsPaya> tblKnpPayaList = KnsPayaDao.selectAll_kns_paya_odx6(sttsdt, sttssq, false);

			if (tblKnpPayaList.size() > 0) {
				for (KnsPaya tblKnpPaya : tblKnpPayaList) {
					// 已经做过销账抛出异常
					if (CommUtil.compare(tblKnpPaya.getPayamn(),
							tblKnpPaya.getRsdlmn()) != 0) {

						throw InError.comm.E0007(tblKnpPaya.getPayasq());
					}
					// 挂账状态不正常，抛出异常
					if (E_PAYAST.ZC != tblKnpPaya.getPayast()) {
						throw InError.comm.E0008(tblKnpPaya.getPayasq());
					}
					//更新未冲销状态
					tblKnpPaya.setPayast(E_PAYAST.CX);
					
					KnsPayaDao.updateOne_kns_paya_odx1(tblKnpPaya);

				}
			} else {
				throw InError.comm.E0006();
			}
			
		}else if(E_ISPAYA._1==ispaya &&((!CommUtil.equals(blncdn.getValue(), amntcd.getValue())&&CommUtil.compare(input.getTranam(), BigDecimal.ZERO)<0)
				||(CommUtil.equals(blncdn.getValue(), amntcd.getValue())&&CommUtil.compare(input.getTranam(), BigDecimal.ZERO)>0))){
			//销账 ，更新销账登记簿和挂账登记簿
						
			 List<KnsPayd> tblKnpPaydList = KnsPaydDao.selectAll_kns_payd_odx6(sttsdt, sttssq, false);
			 			
			 if(tblKnpPaydList.size()>0){
				 for(KnsPayd blKnpPayd : tblKnpPaydList){
					 
					 //销账记录状态不正常，抛出异常
					 if(blKnpPayd.getPaydst()!=E_PAYDST.ZC){
						 
						 throw InError.comm.E0010(blKnpPayd.getPaydst().getLongName());
					 }
					 
					 blKnpPayd.setPaydst(E_PAYDST.CX);
					 blKnpPayd.setUntius(CommTools.getBaseRunEnvs().getTrxn_teller());//更新销账柜员
					 //更新销账登记簿
					 
					 KnsPaydDao.updateOne_kns_payd_odx1(blKnpPayd);
					 
					 //挂账登记簿处理
					 KnsPaya tblKnsPaya = KnsPayaDao.selectOne_kns_paya_odx1(blKnpPayd.getPayasq(), true);
					 
					 //更新剩余挂账金额=剩余挂账金额+本次销账金额
					 tblKnsPaya.setRsdlmn(tblKnsPaya.getRsdlmn().add(blKnpPayd.getPayamn()));
					 
					 //更新挂账记录状态为正常
					 tblKnsPaya.setPayast(E_PAYAST.ZC);
					 
					 //更新销账笔数,减一次
					 tblKnsPaya.setPaydnm(tblKnsPaya.getPaydnm()-1);
					 
					 //更新挂账登记簿
					 tblKnsPaya.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());//更新挂账柜员
					 KnsPayaDao.updateOne_kns_paya_odx1(tblKnsPaya);
				 }
				
			}else{
				//销账记录不存在抛出异常
				throw InError.comm.E0009();
			}
			
		}    	
    	
    }
}
