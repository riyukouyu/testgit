package cn.sunline.ltts.busi.dp.acct;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.dayend.DpDayEndInt;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.InterestAndIntertax;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpIntxamInInstam;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.type.cf.IoCuTable.IoCifCustAccs;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseDetailOT;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCloseOT;
import cn.sunline.ltts.busi.sys.type.ApSmsType.IoCaCloseAcctSendMsg;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;
import cn.sunline.ltts.busi.sys.type.WaEnumType.E_CLOSFG;
import cn.sunline.ltts.busi.sys.type.WaEnumType.E_RELTST;
import cn.sunline.ltts.busi.sys.type.WaEnumType.E_UPFLAG;
import cn.sunline.ltts.busi.wa.type.WaAcctType.IoWaKnaRelt;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class DpCloseCustac {

    private static BizLog log = BizLogUtil.getBizLog(DpCloseCustac.class);

    /***
     * 电子账户销户服务
     * 
     * @param clsin
     * @return
     */
    public static IoDpCloseOT CloseCustac(IoDpCloseIN clsin) {

        log.debug("<<=======销户卡号：" + clsin.getCardno());
        log.debug("<<=======销户电子账号：" + clsin.getCustac());
        log.debug("<<=======电子账号类型：" + clsin.getAccatp().getLongName());

        //获取CA模块的服务外调
        //		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
        //		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);

        //		String inner_acct = clsin.getToacct(); //最后转入方内部户账号
        //		String inner_name = clsin.getToname(); //最后转入方内部户名称
        //		String inner_brch = clsin.getTobrch();
        //		KnaAcct in_acct = SysUtil.getInstance(KnaAcct.class); //最后转入方电子账号信息

        String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
        String tmstmp = DateTools2.getCurrentTimestamp();

        String custac = clsin.getCustac(); //转出电子账号ID
        BigDecimal interest = BigDecimal.ZERO; //利息8
        BigDecimal onbal = BigDecimal.ZERO; //账户余额
        BigDecimal settbal = BigDecimal.ZERO; //销户金额

        List<KnaAcct> lsacct = new ArrayList<>(); //活期负债账户列表
        List<KnaFxac> lsfxac = new ArrayList<>(); //定期负债账户列表

        IoDpCloseOT clsot = SysUtil.getInstance(IoDpCloseOT.class); //客户端销户输出参数

        SaveDpAcctIn savein = SysUtil.getInstance(SaveDpAcctIn.class); //存入服务输入参数
        DrawDpAcctIn drawin = SysUtil.getInstance(DrawDpAcctIn.class); //支取服务输入参数
        DpAcctSvcType dpsvc = SysUtil.getInstance(DpAcctSvcType.class); //负债账户服务
        DrawDpAcctOut drawot = SysUtil.getInstance(DrawDpAcctOut.class); //支取服务输出

        //存款子账户销户时，该类型作为转入方，结算户/三类户销户时，该类型作为转出方
        KnaAcct ot_acct = SysUtil.getInstance(KnaAcct.class); //定义结算账户或者钱包账户
        //		IaAcdrInfo acdr = SysUtil.getInstance(IaAcdrInfo.class); //内部户记账输入参数
        //		
        //		IaTransOutPro inout = SysUtil.getInstance(IaTransOutPro.class); //内部户记账输出参数

        IoDpCloseIN to_clsin = SysUtil.getInstance(IoDpCloseIN.class); //记账时用来登记余额明细

        if (clsin.getAccatp() != E_ACCATP.WALLET) { //非钱包账户,转出账户赋值为结算账户
            //lsacct = KnaAcctDao.selectAll_odb4(custac, E_DPACST.NORMAL, false);
            //lsfxac = KnaFxacDao.selectAll_odb4(custac, E_DPACST.NORMAL, false);
            lsacct = KnaAcctDao.selectAll_odb6(custac, false);
            lsfxac = KnaFxacDao.selectAll_odb5(custac, false);
        }

        ot_acct = CapitalTransDeal.getSettKnaAcctAc(custac);

        /**
         * //交易前校验转入户是否允许转入
         * if(clsin.getClsatp() == E_CLSATP.INACCT){ //转入账户是内部户
         * IoInacInfo acinfo = SysUtil.getInstance(IoInQuery.class).InacInfoQuery(clsin.getToacct());
         * inner_acct = acinfo.getAcctno();
         * inner_name = acinfo.getAcctna();
         * }else if(clsin.getClsatp() == E_CLSATP.CUSTAC){ //电子账号
         * //检查电子账户类型，状态，状态字，姓名
         * IoCaKnaAcdc acdc = caqry.kna_acdc_selectOne_odb2(clsin.getToacct(), false);
         * if(CommUtil.isNull(acdc)){
         * throw DpModuleError.DpstComm.E9999("转入电子账号不存在");
         * }
         * 
         * IoCaKnaCust cust = caqry.kna_cust_selectOne_odb1(acdc.getCustac(), true);
         * E_ACCATP accatp = cagen.qryAccatpByCustac(acdc.getCustac());
         * 
         * CapitalTransCheck.ChkAcctstIN(cust.getCuacst(), cust.getAcctst()); //检查转入电子账户的状态
         * CapitalTransCheck.ChkAcctFrozIN(cust.getCustac()); //检查状态字是否允许转入
         * 
         * if(accatp == E_ACCATP.WALLET){
         * in_acct = KnaAcctDao.selectFirst_odb7(acdc.getCustac(), E_DEBTTP.DP2402, true);
         * }else{
         * in_acct = KnaAcctDao.selectFirst_odb7(acdc.getCustac(), E_DEBTTP.DP2401, true);
         * }
         * inner_acct = clsin.getToacct();
         * inner_name = clsin.getToname();
         * }else{ //转到其他账号，非系统内
         * KnpParameter para = KnpParameterDao.selectOne_odb1("DPTRAN", "CLACBT", "%", "%", false);
         * if(CommUtil.isNull(para)){
         * throw DpModuleError.DpstComm.E9999("找不到内部户配置文件");
         * }
         * 
         * IoInacInfo acinfo = SysUtil.getInstance(IoInQuery.class).selInacInfoByBusino(para.getParm_value1(), clsin.getTobrch(), ot_acct.getCrcycd(), null);
         * inner_acct = acinfo.getAcctno();
         * inner_name = acinfo.getAcctna();
         * }
         **/
        IoDpCloseDetailOT detl_MA = SysUtil.getInstance(IoDpCloseDetailOT.class); //钱包
        IoDpCloseDetailOT detl_FW = SysUtil.getInstance(IoDpCloseDetailOT.class); //亲情钱包
        IoDpCloseDetailOT detl_DP = SysUtil.getInstance(IoDpCloseDetailOT.class); //存款
        detl_MA.setClinst(BigDecimal.ZERO);
        detl_MA.setClprcp(BigDecimal.ZERO);
        detl_MA.setDpactp(E_ACSETP.MA);

        detl_FW.setClinst(BigDecimal.ZERO);
        detl_FW.setClprcp(BigDecimal.ZERO);
        detl_FW.setDpactp(E_ACSETP.FW);

        detl_DP.setClinst(BigDecimal.ZERO);
        detl_DP.setClprcp(BigDecimal.ZERO);
        detl_DP.setDpactp(E_ACSETP.HQ);

        for (KnaAcct acct : lsacct) { //活期的销户 非钱包账户此处列表为空

            if (acct.getAcctst() == E_DPACST.CLOSE) {
                continue;
            }
            if (acct.getAcsetp() == E_ACSETP.SA) { //结算账户在后面统一处理
                continue;
            }
            to_clsin.setAcseno(clsin.getAcseno());
            to_clsin.setCardno(clsin.getCardno());
            to_clsin.setCustac(clsin.getCustac());
            to_clsin.setSmrycd(BusinessConstants.SUMMARY_ZC);
            to_clsin.setToacct(ot_acct.getAcctno());
            to_clsin.setTobrch(ot_acct.getBrchno());
            to_clsin.setToname(ot_acct.getAcctna());
            to_clsin.setRemark("子账户销户转出");

            InterestAndIntertax cplint = DpCloseAcctno.prcCurrInterest(acct, to_clsin); //利息记账处理  利息-利息税
            onbal = DpCloseAcctno.prcCurrOnbal(acct, to_clsin); //余额记账处理，并修改账户状态为关户
            interest = cplint.getDiffam();//客户应收利息=利息-利息税
            settbal = settbal.add(onbal).add(interest);
            log.debug("<<=====负债账户：[%s],销户余额：[%s],销户利息:[%s],转入账号：[%s]=======>>", acct.getAcctno(), onbal, interest, ot_acct.getAcctno());

            if (acct.getAcsetp() == E_ACSETP.MA) { //钱包账户
                detl_MA.setClinst(detl_MA.getClinst().add(interest));
                detl_MA.setClprcp(detl_MA.getClprcp().add(onbal));
                detl_MA.setClatax(detl_MA.getClatax().add(cplint.getIntxam()));//利息税
                detl_MA.setCltxin(detl_MA.getCltxin().add(cplint.getInstam()));//应税利息

            } else if (acct.getAcsetp() == E_ACSETP.FW) {
                detl_FW.setClinst(detl_FW.getClinst().add(interest));
                detl_FW.setClprcp(detl_FW.getClprcp().add(onbal));
                detl_FW.setClatax(detl_FW.getClatax().add(cplint.getIntxam()));//利息税
                detl_FW.setCltxin(detl_FW.getCltxin().add(cplint.getInstam()));//应税利息

            } else if (acct.getAcsetp() == E_ACSETP.HQ) {
                detl_DP.setClinst(detl_DP.getClinst().add(interest));
                detl_DP.setClprcp(detl_DP.getClprcp().add(onbal));
                detl_DP.setClatax(detl_DP.getClatax().add(cplint.getIntxam()));//利息税
                detl_DP.setCltxin(detl_DP.getCltxin().add(cplint.getInstam()));//应税利息

            }

            //利息入账
            if (CommUtil.compare(cplint.getInstam(), BigDecimal.ZERO) > 0) {
                //结算户存入记账处理(利息存入结算户)
                savein.setAcctno(ot_acct.getAcctno()); //结算户
                savein.setAcseno(clsin.getAcseno());
                savein.setBankcd(acct.getCorpno());
                savein.setBankna("");
                savein.setCardno(clsin.getCardno());
                savein.setCrcycd(acct.getCrcycd());
                savein.setCustac(acct.getCustac());
                savein.setLinkno("");
                savein.setOpacna(acct.getAcctna());
                savein.setOpbrch(acct.getBrchno());
                savein.setSmrycd(BusinessConstants.SUMMARY_FX);
                savein.setSmryds("付息");
                //savein.setSmrycd(BusiTools.getBusiRunEnvs().getSmrycd());
                savein.setRemark("子账户销户结息转入");
                savein.setToacct(acct.getAcctno());
                savein.setTranam(cplint.getInstam()); //利息
                savein.setIschck(E_YES___.NO);
                savein.setStrktg(E_YES___.NO); // 销户时利息存入结算户,不进行冲正

                dpsvc.addPostAcctDp(savein);
            }
            //利息税入账
            if (CommUtil.compare(cplint.getIntxam(), BigDecimal.ZERO) > 0) {
                //结算户支取记账处理(利息税从结算户支取)			
                drawin.setAcctno(ot_acct.getAcctno()); //做支取的负债账号
                drawin.setAcseno(clsin.getAcseno());
                drawin.setAuacfg(E_YES___.NO);
                drawin.setCardno(clsin.getCardno());
                drawin.setCrcycd(acct.getCrcycd());
                drawin.setCustac(acct.getCustac());
                drawin.setLinkno(null);
                drawin.setOpacna(acct.getAcctna());
                drawin.setToacct(acct.getAcctno()); //结算账号
                drawin.setTranam(cplint.getIntxam());
                drawin.setSmrycd(BusinessConstants.SUMMARY_JS);// 缴税
                drawin.setSmryds("缴税");
                drawin.setRemark("子账户销户利息税缴税");
                drawin.setStrktg(E_YES___.NO); // 销户时扣取利息税,不进行冲正
                
                drawot = dpsvc.addDrawAcctDp(drawin);

            }

            if (CommUtil.compare(onbal, BigDecimal.ZERO) > 0) {
                //结算户存入记账处理(子账户本金存入结算子账户)
                savein.setAcctno(ot_acct.getAcctno()); //结算户
                savein.setAcseno(clsin.getAcseno());
                savein.setBankcd(acct.getCorpno());
                savein.setBankna("");
                savein.setCardno(clsin.getCardno());
                savein.setCrcycd(acct.getCrcycd());
                savein.setCustac(acct.getCustac());
                savein.setLinkno("");
                savein.setOpacna(acct.getAcctna());
                savein.setOpbrch(acct.getBrchno());
                savein.setSmrycd(BusinessConstants.SUMMARY_ZR);
                savein.setSmryds("转入");
                savein.setRemark("子账户销户转入");
                savein.setToacct(acct.getAcctno());
                savein.setTranam(onbal); // 记账发生额
                savein.setIschck(E_YES___.NO);
                savein.setStrktg(E_YES___.NO); // 不允许冲正
                
                dpsvc.addPostAcctDp(savein);
            }

            if (acct.getAcsetp() == E_ACSETP.FW) {
                // 更新亲情关系信息
                DpAcctDao.updKnaRelt(acct.getAcctno(), tmstmp);

                // 获取亲情关系信息
                IoWaKnaRelt cplKnaRelt = DpAcctDao.selknaReltId(acct.getAcctno(), true);
                //取消掉亲情包相关内容
//                SysUtil.getInstance(IoWaSrvWalletAccountType.class).ioWaContract(cplKnaRelt.getReltid(), E_UPFLAG.SURR, E_CLOSFG.CUCLOS);

                /**
                 * 关联人亲情钱包为全部关闭成功后，将关闭信息发送到统一认证系统
                 */
                int cnt = DpAcctDao.selKnaReltByReltst(cplKnaRelt.getElacct(), E_RELTST.SUCCESSFUL, false);
                if (cnt == 0) {

                    // 获取关联人用户ID
                    IoCifCustAccs cplCifCustAccs = DpAcctDao.selCifCustAccsByCustno(cplKnaRelt.getElacct(), true);

                    //E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介

                    /*KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("CLOSWA", "%", "%", "%", true);

                    String bdid = tblKnaPara.getParm_value1();// 服务绑定ID

                    IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind( IoCaOtherService.class, bdid);

                    // 1.销户成功发送销户结果到客户信息
                    String mssdid = CommTools.getMySysId();// 消息ID
                    String mesdna = tblKnaPara.getParm_value2();// 媒介名称

                    IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter closeSendMsgInput = CommTools
                    		.getInstance(IoCaOtherService.IoCaCloseAcctSendMsg.InputSetter.class);

                    closeSendMsgInput.setMsgid(mssdid); // 发送消息ID
                    //closeSendMsgInput.setMedium(mssdtp); // 消息媒介
                    closeSendMsgInput.setMdname(mesdna); // 媒介名称
                    closeSendMsgInput.setCustid(cplCifCustAccs.getCustid()); // 用户ID
                    closeSendMsgInput.setOpclfg(E_YES___.NO);// 交易密码开关标志

                    caOtherService.closeAcctSendMsg(closeSendMsgInput);*/
                    //修改销户cmq通知  modify lull
                    //MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
                    //mri.setMtopic("Q0101004");
                    IoCaCloseAcctSendMsg closeSendMsgInput = SysUtil
                            .getInstance(IoCaCloseAcctSendMsg.class);
                    closeSendMsgInput.setCustid(cplCifCustAccs.getCustid()); // 用户ID
                    closeSendMsgInput.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());// 操作机构
                    closeSendMsgInput.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());// 操作柜员
                    closeSendMsgInput.setClossv(CommTools.getBaseRunEnvs().getChannel_id());// 销户渠道

                   // mri.setMsgtyp("ApSmsType.IoCaCloseAcctSendMsg");
                    //mri.setMsgobj(closeSendMsgInput);
                    //AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
                    CommTools.addMessagessToContext("Q0101004", "ApSmsType.IoCaCloseAcctSendMsg");
                }

            }

        }

        for (KnaFxac fxac : lsfxac) { //非钱包账户此处列表为空

            if (fxac.getAcctst() == E_DPACST.CLOSE) {
                continue;
            }
            interest = BigDecimal.ZERO;//利息初始化
            BigDecimal intertax = BigDecimal.ZERO;//利息税

            onbal = fxac.getOnlnbl();

            if (CommUtil.compare(fxac.getOnlnbl(), BigDecimal.ZERO) > 0) {
                drawin.setAcctno(fxac.getAcctno()); //做支取的负债账号
                drawin.setAcseno(clsin.getAcseno());
                drawin.setAuacfg(E_YES___.NO);
                drawin.setCardno(clsin.getCardno());
                drawin.setCrcycd(fxac.getCrcycd());
                drawin.setCustac(custac);
                drawin.setLinkno(null);
                drawin.setOpacna(fxac.getAcctna());
                drawin.setToacct(ot_acct.getAcctno()); //结算账号
                drawin.setTranam(fxac.getOnlnbl());
                drawin.setSmrycd(BusinessConstants.SUMMARY_ZC);
                drawin.setSmryds("转出");
                drawin.setRemark("子账户销户转出");
                drawin.setOpbrch(fxac.getBrchno());
                drawin.setStrktg(E_YES___.NO);
                drawot = dpsvc.addDrawAcctDp(drawin);
                
            	interest = drawot.getInstam();
                intertax = drawot.getIntxam();
            }
            
            interest = CommUtil.nvl(interest, BigDecimal.ZERO);
            intertax = CommUtil.nvl(intertax, BigDecimal.ZERO);

            DpIntxamInInstam cplInstamTx = DpDayEndInt.fxDrawInst(fxac.getAcctno(), fxac.getCrcycd(), trandt);
            BigDecimal drInterest = BusiTools.roundByCurrency(fxac.getCrcycd(), cplInstamTx.getInstam(), null);
            BigDecimal drIntertax = BusiTools.roundByCurrency(fxac.getCrcycd(), cplInstamTx.getIntxam(), null);

            //登记会计流水 TODO
            if (CommUtil.isNotNull(drInterest)
                    && CommUtil.compare(drInterest, BigDecimal.ZERO) != 0) {

                log.debug("登记利息支取会计流水>>>>>>>>>>>>>>>>>" + drInterest);

                IoAccounttingIntf cplIoAccounttingInrt = SysUtil
                        .getInstance(IoAccounttingIntf.class);
                cplIoAccounttingInrt.setCuacno(custac); //电子账号
                cplIoAccounttingInrt.setAcctno(fxac.getAcctno()); //账号
                cplIoAccounttingInrt.setAcseno(clsin.getAcseno()); //子户号
                cplIoAccounttingInrt.setProdcd(fxac.getProdcd()); //产品编号
                cplIoAccounttingInrt.setDtitcd(fxac.getAcctcd()); //核算口径
                cplIoAccounttingInrt.setCrcycd(fxac.getCrcycd()); //币种
                cplIoAccounttingInrt.setTranam(drInterest); //利息
                cplIoAccounttingInrt.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
                cplIoAccounttingInrt.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
                cplIoAccounttingInrt.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
                cplIoAccounttingInrt.setAcctbr(fxac.getBrchno()); //登记账户机构
                cplIoAccounttingInrt.setAmntcd(E_AMNTCD.DR); //借方
                cplIoAccounttingInrt.setAtowtp(E_ATOWTP.DP); //存款
                cplIoAccounttingInrt.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型，账务
                cplIoAccounttingInrt.setBltype(E_BLTYPE.PYIN); //余额属性利息支出
                //登记交易信息，供总账解析
                if (CommUtil.equals("1", KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%", true).getParm_value1())) {
                    KnpParameter para = SysUtil.getInstance(KnpParameter.class);
                    para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%", "%", true);
                    cplIoAccounttingInrt.setTranms(para.getParm_value1());//登记交易信息 20160701  结息           	
                }

                SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
                        cplIoAccounttingInrt);
            }
            // 登记流水登记簿 end
            
            interest = interest.add(drInterest);
            intertax = intertax.add(drIntertax);

            detl_DP.setClinst(detl_DP.getClinst().add(interest).subtract(intertax));
            detl_DP.setClprcp(detl_DP.getClprcp().add(onbal));
            detl_DP.setClatax(detl_DP.getClatax().add(intertax));
            detl_DP.setCltxin(detl_DP.getCltxin().add(interest));

            settbal = settbal.add(onbal).add(interest).subtract(intertax);
            log.debug("<<=====负债账户：[%s],销户余额：[%s],销户利息:[%s],转入账号：[%s]=======>>", fxac.getAcctno(), onbal, interest, ot_acct.getAcctno());
            //结算户记账
            if (CommUtil.compare(onbal, BigDecimal.ZERO) > 0) {
                savein.setAcctno(ot_acct.getAcctno());
                savein.setAcseno(clsin.getAcseno());
                savein.setBankcd(null);
                savein.setBankna(null);
                savein.setCardno(clsin.getCardno());
                savein.setCrcycd(fxac.getCrcycd());
                savein.setCustac(fxac.getCustac());
                savein.setLinkno(null);
                savein.setOpacna(fxac.getAcctna());
                savein.setOpbrch(fxac.getBrchno());
                savein.setSmrycd(BusinessConstants.SUMMARY_ZR);
                savein.setSmryds("转入");
                savein.setRemark("子账户销户转入");
                savein.setToacct(fxac.getAcctno());
                savein.setTranam(onbal); //发生额
                savein.setIschck(E_YES___.NO);
                savein.setStrktg(E_YES___.NO);

                dpsvc.addPostAcctDp(savein);
            }
            //利息入账
            if (CommUtil.compare(interest, BigDecimal.ZERO) > 0) {
                savein.setAcctno(ot_acct.getAcctno());
                savein.setAcseno(clsin.getAcseno());
                savein.setBankcd(null);
                savein.setBankna(null);
                savein.setCardno(clsin.getCardno());
                savein.setCrcycd(fxac.getCrcycd());
                savein.setCustac(fxac.getCustac());
                savein.setLinkno(null);
                savein.setOpacna(fxac.getAcctna());
                savein.setOpbrch(fxac.getBrchno());
                savein.setSmrycd(BusinessConstants.SUMMARY_ZR);
                savein.setSmryds("转入");
                savein.setRemark("子账户销户结息转入");
                savein.setToacct(fxac.getAcctno());
                savein.setTranam(interest); // 利息
                savein.setIschck(E_YES___.NO);
                savein.setStrktg(E_YES___.NO); // 销户时利息入账后不进行冲正

                dpsvc.addPostAcctDp(savein);
            }
            //利息税入账
            if (CommUtil.compare(intertax, BigDecimal.ZERO) > 0) {
                //结算户支取记账处理				
                drawin.setAcctno(ot_acct.getAcctno()); //做支取的负债账号
                drawin.setAcseno(clsin.getAcseno());
                drawin.setAuacfg(E_YES___.NO);
                drawin.setCardno(clsin.getCardno());
                drawin.setCrcycd(fxac.getCrcycd());
                drawin.setCustac(fxac.getCustac());
                drawin.setLinkno(null);
                drawin.setSmrycd(BusinessConstants.SUMMARY_ZC);
                drawin.setSmryds("转出");
                drawin.setOpacna(fxac.getAcctna());
                drawin.setToacct(fxac.getAcctno());
                drawin.setTranam(intertax);
                drawin.setStrktg(E_YES___.NO); // 销户时利息税入账后不进行冲正
                
                drawot = dpsvc.addDrawAcctDp(drawin);

            }

        }

        //settbal = settbal.add(ot_acct.getOnlnbl());
        /**
         * 注销原因:销户交易中根据状态来进行处理结算户的结息处理
         * //从结算户或钱包户转出到客户指定账户中
         * //钱包账户直接从钱包账户转出
         * to_clsin.setAcseno(clsin.getAcseno());
         * to_clsin.setCardno(clsin.getCardno());
         * to_clsin.setCustac(clsin.getCustac());
         * to_clsin.setSmrycd(clsin.getSmrycd());
         * to_clsin.setToacct(inner_acct);
         * to_clsin.setTobrch(inner_brch);
         * to_clsin.setToname(inner_name);
         * 
         * interest = DpCloseAcctno.prcCurrInterest(ot_acct, to_clsin); //利息记账处理
         * 
         * 
         * 
         * //结算户记账
         * savein.setAcctno(ot_acct.getAcctno());
         * savein.setAcseno(clsin.getAcseno());
         * savein.setBankcd(null);
         * savein.setBankna(null);
         * savein.setCardno(clsin.getCardno());
         * savein.setCrcycd(ot_acct.getCrcycd());
         * savein.setCustac(ot_acct.getCustac());
         * savein.setLinkno(null);
         * savein.setOpacna(ot_acct.getAcctna());
         * savein.setOpbrch(ot_acct.getBrchno());
         * savein.setRemark("电子账户销户");
         * savein.setSmrycd(clsin.getSmrycd());
         * savein.setSmryds(null);
         * savein.setToacct(ot_acct.getAcctno());
         * savein.setTranam(interest); //发生额
         * dpsvc.addPostAcctDp(savein);
         **/

        //设置返回值
        clsot.setSettbl(settbal);
        clsot.setCrcycd(ot_acct.getCrcycd());
        clsot.getDetail().add(detl_MA);
        clsot.getDetail().add(detl_FW);
        clsot.getDetail().add(detl_DP);

        return clsot;
    }

}
