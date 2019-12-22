package cn.sunline.ltts.busi.dp.serviceimpl;

import java.util.List;

import cn.sunline.adp.cedar.base.engine.sequence.SequenceManager;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.parameter.MsGlobalMultiParm;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_YESORNO;
import cn.sunline.clwj.msap.util.sequ.MsSeqUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.busi.dp.errors.DpAcError;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.ApbAcctRout;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.ApbAcctRoutDao;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aptran.namedsql.StrikeSqlsDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsq;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqCler;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqClerDao;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqClin;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqClinDao;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.iobus.servicetype.IoFeChrgSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType.IoSelChrgAccRule;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.IoDpQueryComplexType.IoKnlBillInfo;
import cn.sunline.ltts.busi.iobus.type.IoInTable.IoGlKnpBusi;
import cn.sunline.ltts.busi.iobus.type.IoInTable.IoGlKnsGlvc;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccountClearInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoKnsAcsqInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoKnsProdClerInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoKnsAcsqInfoWithBradna;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.serv.ServEacctType.EacctCustnaCardno;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DASYST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRSQTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.PbEnumType;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;

/**
 * 记账服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
public class IoAccountSvcTypeImpl implements
        cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType {
	
	static final BizLog bizlog = BizLogUtil.getBizLog(IoAccountSvcTypeImpl.class);
    //private static final BizLog bizlog = BizLogUtil.getBizLog(IoAccountSvcTypeImpl.class);

    /**
     * 登记会计流水
     * 
     */
    public void ioAccountting(
            final cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf cplIoAccounttingIntf) {
        KnsAcsq tblAccountInfo = SysUtil.getInstance(KnsAcsq.class);
        tblAccountInfo.setAcctno(cplIoAccounttingIntf.getAcctno());
        tblAccountInfo.setAcseno(cplIoAccounttingIntf.getAcseno());
        tblAccountInfo.setDtitcd(cplIoAccounttingIntf.getDtitcd());
        tblAccountInfo.setCrcycd(cplIoAccounttingIntf.getCrcycd());
        tblAccountInfo.setTrandt(cplIoAccounttingIntf.getTrandt());
        tblAccountInfo.setAmntcd(cplIoAccounttingIntf.getAmntcd());
        tblAccountInfo.setCuacno(cplIoAccounttingIntf.getCuacno());
        tblAccountInfo.setAtowtp(cplIoAccounttingIntf.getAtowtp());
        tblAccountInfo.setMntrsq(cplIoAccounttingIntf.getMntrsq());
        tblAccountInfo.setSortno(MsSeqUtil.genSeqId("KnsAcsq"));
        tblAccountInfo.setDasyst(E_DASYST.WAIT);

        /* 计提流水处理 */
        //		if (cplIoAccounttingIntf.getTrsqtp() == E_ATSQTP.CAIN) {
        //			/* 会计流水类型为计提流水时，交易流水=JT+DCN号+交易日期 */
        //			// TODO
        //			// String sDcnbima = CommTools.getBaseRunEnvs().getDqdcnhao();
        //			//
        //			// tblKjlsdj.setJiaoyils("JT" + sDcnbima + cplAcInfo.getJiaoyirq());
        //			// tblKjlsdj.setZujylius("*");
        //			// tblKjlsdj.setJizhfanx(null);
        //		} else {
        //			tblAccountInfo.setTransq(CommUtil.nvl(cplIoAccounttingIntf
        //					.getMntrsq(), CommTools.getBaseRunEnvs().getMain_trxn_seq()));
        //		}

        //	bizlog.debug("=========IoAccountSvcTypeImpl.ioAccountting========transq[%s]", CommTools.getBaseRunEnvs().getTrxn_seq());
        //	bizlog.debug("=========IoAccountSvcTypeImpl.ioAccountting========transq[%s]", cplIoAccounttingIntf.getTransq());

        tblAccountInfo.setTransq(CommUtil.nvl(cplIoAccounttingIntf
                .getTransq(), CommTools.getBaseRunEnvs().getTrxn_seq()));

        if (CommUtil.isNotNull(cplIoAccounttingIntf.getProdcd()))
            tblAccountInfo.setProdcd(cplIoAccounttingIntf.getProdcd());
        else
            tblAccountInfo.setProdcd(ApUtil.DEFAULT_PROD_CODE);

        // 默认为表内
        tblAccountInfo.setTranam(cplIoAccounttingIntf.getTranam());
        tblAccountInfo.setBltype(cplIoAccounttingIntf.getBltype());
        tblAccountInfo.setAcctdt(cplIoAccounttingIntf.getAcctdt());
        tblAccountInfo.setAcctbr(cplIoAccounttingIntf.getAcctbr());
        tblAccountInfo.setAcseno(cplIoAccounttingIntf.getAcseno());
        tblAccountInfo.setAtsqtp(cplIoAccounttingIntf.getTrsqtp());
        tblAccountInfo.setCrdcnfg(null);
        //借贷方向为收付时，记表外
        if (cplIoAccounttingIntf.getAmntcd() == E_AMNTCD.RV
                || cplIoAccounttingIntf.getAmntcd() == E_AMNTCD.PY) {
            tblAccountInfo.setIoflag(E_IOFLAG.OUT);
        } else {
            tblAccountInfo.setIoflag(E_IOFLAG.IN);
        }
        tblAccountInfo.setServtp(CommUtil.nvl(cplIoAccounttingIntf.getServtp(), CommTools.getBaseRunEnvs().getChannel_id()));//渠道
        tblAccountInfo.setTranms(cplIoAccounttingIntf.getTranms());//交易日期
        tblAccountInfo.setBookms(cplIoAccounttingIntf.getBookms());//会计信息
        tblAccountInfo.setStatms(cplIoAccounttingIntf.getStatms());//统计信息
        tblAccountInfo.setToacct(cplIoAccounttingIntf.getToacct());//对方账号
        tblAccountInfo.setToacna(cplIoAccounttingIntf.getToacna());//对方户名
        tblAccountInfo.setTobrch(cplIoAccounttingIntf.getTobrch());//对方机构
        tblAccountInfo.setClerdt(BusiTools.getClearDateInfo().getSystdt());//清算日期
        tblAccountInfo.setHashvl(CommTools.getGroupHashValue("KNS_ACSQ_HASH_VALUE", CommTools.getBaseRunEnvs().getTrxn_seq()));
        //法人号
        tblAccountInfo.setCorpno(CommUtil.nvl(cplIoAccounttingIntf.getCorpno(), CommTools.getBaseRunEnvs().getBusi_org_id()));
        tblAccountInfo.setFileid(CommTools.getBaseRunEnvs().getTrxn_code());//交易码

        KnsAcsqDao.insert(tblAccountInfo);

        /* 分布式跨DCN并且不是跨法人的情况，补跨DCN往来 */
        /*if (BusiTools.getDeptAcctdt()) {
            int iDays = CommUtil.compare(cplIoAccounttingIntf.getAcctdt(), CommTools.getBaseRunEnvs().getInitiator_date());
            if (iDays < 0) {
                throw DpModuleError.DpstAcct.BNAS1159(cplIoAccounttingIntf.getAcctdt(), CommTools.getBaseRunEnvs().getInitiator_date());
            }

            // 应入账日期 > 交易日期,并且不是跨法人 ，需要补账
            if (iDays > 0 && CommTools.getBaseRunEnvs().getCross_org_ind() == E_YESORNO.NO) {
                // 补账逻辑
                KnsAcsq tblAcct_new = SysUtil.getInstance(KnsAcsq.class);

                // 1.补一笔交易日期同方向账
                tblAcct_new.setTrandt(CommTools.getBaseRunEnvs().getInitiator_date());
                tblAcct_new.setTransq(CommUtil.nvl(cplIoAccounttingIntf
                        .getTransq(), CommTools.getBaseRunEnvs().getTrxn_seq()));
                //tblAcct_new.setSortno(Long.parseLong(SequenceManager.nextval("KnsAcsq")));
                tblAcct_new.setSortno(Long.parseLong(CoreUtil.nextValue("KnsAcsq")));
                tblAcct_new.setAcctdt(CommTools.getBaseRunEnvs().getInitiator_date());
                tblAcct_new.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
                tblAcct_new.setAtsqtp(PbEnumType.E_ATSQTP.ACCOUNT);
                tblAcct_new.setAtowtp(PbEnumType.E_ATOWTP.IN);
                tblAcct_new.setDtitcd(MsGlobalMultiParm.getGlobalParm("system.clear", "dcn").getParm_value1());
                tblAcct_new.setAcctbr(cplIoAccounttingIntf.getAcctbr());
                tblAcct_new.setCrcycd(cplIoAccounttingIntf.getCrcycd());
                tblAcct_new.setBltype(BaseEnumType.E_BLTYPE.BALANCE);
                tblAcct_new.setAmntcd(cplIoAccounttingIntf.getAmntcd());
                tblAcct_new.setTranam(cplIoAccounttingIntf.getTranam());
                tblAcct_new.setDasyst(BaseEnumType.E_DASYST.WAIT);
                tblAcct_new.setIoflag(E_IOFLAG.IN);
                tblAcct_new.setProdcd(ApUtil.DEFAULT_PROD_CODE);
                tblAcct_new.setCrdcnfg(null);
                tblAcct_new.setHashvl(CommTools.getGroupHashValue("KNS_ACSQ_HASH_VALUE", CommTools.getBaseRunEnvs().getTrxn_seq()));
                tblAcct_new.setFileid(CommTools.getBaseRunEnvs().getTrxn_code());//交易码
                KnsAcsqDao.insert(tblAcct_new);

                // 2.再补一笔应入账日期反方向账
                tblAcct_new.setTrandt(CommTools.getBaseRunEnvs().getInitiator_date());
                tblAcct_new.setTransq(CommUtil.nvl(cplIoAccounttingIntf
                        .getTransq(), CommTools.getBaseRunEnvs().getTrxn_seq()));
                //tblAcct_new.setSortno(Long.parseLong(SequenceManager.nextval("KnsAcsq")));
                tblAcct_new.setSortno(Long.parseLong(CoreUtil.nextValue("KnsAcsq")));
                tblAcct_new.setAcctdt(cplIoAccounttingIntf.getAcctdt());
                tblAcct_new.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
                tblAcct_new.setAtsqtp(PbEnumType.E_ATSQTP.ACCOUNT);
                tblAcct_new.setAtowtp(PbEnumType.E_ATOWTP.IN);
                tblAcct_new.setDtitcd(MsGlobalMultiParm.getGlobalParm("system.clear", "dcn").getParm_value1());
                tblAcct_new.setAcctbr(cplIoAccounttingIntf.getAcctbr());
                tblAcct_new.setCrcycd(cplIoAccounttingIntf.getCrcycd());
                tblAcct_new.setBltype(BaseEnumType.E_BLTYPE.BALANCE);
                tblAcct_new.setAmntcd(cplIoAccounttingIntf.getAmntcd() == BaseEnumType.E_AMNTCD.CR ? BaseEnumType.E_AMNTCD.DR : BaseEnumType.E_AMNTCD.CR);
                tblAcct_new.setTranam(cplIoAccounttingIntf.getTranam());
                tblAcct_new.setDasyst(BaseEnumType.E_DASYST.WAIT);
                tblAcct_new.setIoflag(E_IOFLAG.IN);
                tblAcct_new.setProdcd(ApUtil.DEFAULT_PROD_CODE);
                tblAcct_new.setCrdcnfg(null);
                tblAcct_new.setHashvl(CommTools.getGroupHashValue("KNS_ACSQ_HASH_VALUE", CommTools.getBaseRunEnvs().getTrxn_seq()));
                tblAcct_new.setFileid(CommTools.getBaseRunEnvs().getTrxn_code());//交易码
                KnsAcsqDao.insert(tblAcct_new);
            }
        }

        //跨DCN同时跨法人的情况下，补法人待清算
        if (CommTools.getBaseRunEnvs().getCross_org_ind() == E_YESORNO.YES) {
            KnsAcsq tblAcct_new = SysUtil.getInstance(KnsAcsq.class);
            tblAcct_new.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
            tblAcct_new.setTransq(CommUtil.nvl(cplIoAccounttingIntf
                    .getTransq(), CommTools.getBaseRunEnvs().getTrxn_seq()));
            //tblAcct_new.setSortno(Long.parseLong(SequenceManager.nextval("KnsAcsq")));
            tblAcct_new.setSortno(Long.parseLong(CoreUtil.nextValue("KnsAcsq")));
            tblAcct_new.setAcctdt(cplIoAccounttingIntf.getAcctdt());
            tblAcct_new.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
            tblAcct_new.setAtsqtp(PbEnumType.E_ATSQTP.ACCOUNT);
            tblAcct_new.setAtowtp(PbEnumType.E_ATOWTP.IN);
            tblAcct_new.setDtitcd(MsGlobalMultiParm.getGlobalParm("system.clear", "corp").getParm_value1());
            tblAcct_new.setAcctbr(cplIoAccounttingIntf.getAcctbr());
            tblAcct_new.setCrcycd(cplIoAccounttingIntf.getCrcycd());
            tblAcct_new.setBltype(BaseEnumType.E_BLTYPE.BALANCE);
            tblAcct_new.setAmntcd(cplIoAccounttingIntf.getAmntcd() == BaseEnumType.E_AMNTCD.CR ? BaseEnumType.E_AMNTCD.DR : BaseEnumType.E_AMNTCD.CR);
            tblAcct_new.setTranam(cplIoAccounttingIntf.getTranam());
            tblAcct_new.setDasyst(BaseEnumType.E_DASYST.WAIT);
            tblAcct_new.setIoflag(E_IOFLAG.IN);
            tblAcct_new.setProdcd(ApUtil.DEFAULT_PROD_CODE);
            tblAcct_new.setCrdcnfg(null);
            tblAcct_new.setHashvl(CommTools.getGroupHashValue("KNS_ACSQ_HASH_VALUE", CommTools.getBaseRunEnvs().getTrxn_seq()));
            tblAcct_new.setFileid(CommTools.getBaseRunEnvs().getTrxn_code());//交易码

            KnsAcsqDao.insert(tblAcct_new);
        }*/

    }

    /**
     * 查询会计记账流水信息
     */
	@Override
	public Options<IoKnsAcsqInfo> queryKnsAcsq(String bgindt, String endddt, String acctno, String transq,
			String corpno, Long pageno, Long pgsize, String cardno) {

        pageno = CommUtil.nvl(pageno, CommTools.getBaseRunEnvs().getPage_start());
//		pageno = (pageno == null || pageno <= 0) ? CommTools.getBaseRunEnvs().getPage_start() : pageno;
        pgsize = CommUtil.nvl(pgsize, CommTools.getBaseRunEnvs().getPage_size());
        Options<IoKnsAcsqInfo> cplKnsAcsqInfoList = new DefaultOptions<>();
        String trandt = CommTools.getBaseRunEnvs().getTrxn_date();

        if (CommUtil.isNotNull(bgindt) && CommUtil.compare(bgindt, trandt) > 0) {

            throw DpModuleError.DpstAcct.BNAS0413();
        }
        if (CommUtil.isNotNull(endddt) && CommUtil.compare(endddt, bgindt) < 0) {

            throw DpModuleError.DpstAcct.BNAS0598();
        }
        // add by jizhirong 20180109 增加cardno查询
        String acctno1=null;
        if(CommUtil.isNotNull(cardno)){
            acctno1 = CapitalTransDeal.getSettKnaAcctSub(KnaAcdcDao.selectOne_odb2(cardno, true).getCustac(), E_ACSETP.SA).getAcctno();
        }
        if(CommUtil.isNotNull(acctno)){
        	 acctno1=acctno; 
        }           
        Page<IoKnsAcsqInfo> acsqInfoList = DpAcctDao.selKnsAcsqInfosNew(trandt, transq, bgindt, endddt, corpno, acctno1, (pageno - 1) * pgsize, pgsize, 0, false);
        for (IoKnsAcsqInfo list : acsqInfoList.getRecords()) {

            if (CommUtil.equals(list.getTrsqst(), "3")) {
                //已经被冲正
                list.setStatus(E_TRSQTG.BZ);
            } else if (E_YES___.YES == list.getStrksq()) {
                //是冲正流水
                list.setStatus(E_TRSQTG.CZ);
            } else if (E_YES___.YES != list.getSacotg() && CommUtil.equals(list.getTrsqst(), "1")) {

                list.setStatus(E_TRSQTG.WF);
            } else {
                //正常
                list.setStatus(E_TRSQTG.ZC);
            }
            

            String acctna = "";
            if (list.getAtowtp() == E_ATOWTP.IN) {
            //modify by ouyt 20180130 增加业务编码名称查询方法
            	ApbAcctRout acctRoute = ApbAcctRoutDao.selectOne_odb1(list.getAcctno(), false);
            	if(CommUtil.isNotNull(acctRoute)){
            		if(E_ACCTROUTTYPE.INSIDE == acctRoute.getAcctrt()){
            			acctna = SysUtil.getInstance(IoInQuery.class).InacInfoQuery(list.getAcctno()).getAcctna();
            		}
            	}else{
            		 IoGlKnpBusi selknpbusi = StrikeSqlsDao.selknpbusi(list.getAcctno(), false);
            		  if(CommUtil.isNotNull(selknpbusi)){
            			  acctna= selknpbusi.getBusina();
            		  }
            	}
                

            } else if (list.getAtowtp() == E_ATOWTP.DP && E_BLTYPE.BALANCE == list.getBltype()) {

                EacctCustnaCardno cplCust = SysUtil.getInstance(ServEacctSvcType.class).qryCustnaAndCardno(list.getAcctno());
                if (CommUtil.isNull(cplCust.getCardno())) {
                    throw DpModuleError.DpstAcct.BNAS0944(list.getAcctno());
                }
                acctna = cplCust.getCustna();
                list.setAcctno(cplCust.getCardno());

            } else if (list.getAtowtp() == E_ATOWTP.FE) {
                /*IoSelChrgAccRule.Output output = SysUtil.getInstance(IoSelChrgAccRule.Output.class);
                IoCgChrgSvcType.IoSelChrgAccRule.InputSetter input = SysUtil.getInstance(IoSelChrgAccRule.InputSetter.class);

                //给input赋值
                input.setChrgcd(list.getCuacno());

                SysUtil.getInstance(IoCgChrgSvcType.class).SelChrgAccRule(input, output);*/

                //获取费种代码
            	IoFeChrgSvcType.IoSelKcpChrg.Output output = SysUtil.getInstance(IoFeChrgSvcType.IoSelKcpChrg.Output.class);
            	 SysUtil.getInstance(IoFeChrgSvcType.class).SelKcpChrg(list.getCuacno(), output);
                acctna = output.getOut().getChrgna();
            }
            list.setAcctna(acctna);
            list.setTransq(list.getMntrsq());
            list.setTrantp("2");
            // 内部账与客户账之间转账补客户账(当前只允许一借一贷),从传票流水表查询客户账账号和姓名
            bizlog.debug("======当前会计流水[%s]==========当前会计流水客户号[%s]======", list.toString(),list.getAcctno());
            if ( CommUtil.isNull(list.getAcctno()) || "".equals(list.getAcctno())) { 
            	IoGlKnsGlvc ioGlKnsGlvc = SysUtil.getInstance(IoInSrvQryTableInfo.class).selGlKnsGlvcByTransq(list.getTransq());
            	if (CommUtil.isNotNull(ioGlKnsGlvc)) {
            		bizlog.debug("当前会计流水账号为空[%s]",ioGlKnsGlvc.toString());
            		list.setAcctno(ioGlKnsGlvc.getToacct());
                	list.setAcctna(ioGlKnsGlvc.getToacna());
				}
			}
            /*
            			if(BusiTools.isCounterChannel()){
            				IoKapsGmjyls cplUsInfo= SysUtil.getInstance(IoApReverseImpl.class).qryKapsGmjylsInfo(trandt, null,list.getMntrsq());
            				
            				if(CommUtil.isNotNull(cplUsInfo)){	
            					
            					list.setTransq(cplUsInfo.getUssqno());//柜面返回柜面流水
            					
            				}
            			}
            			*/
            cplKnsAcsqInfoList.add(list);
        }

        CommTools.getBaseRunEnvs().setTotal_count(acsqInfoList.getRecordCount());
        return cplKnsAcsqInfoList;
    }

    /**
     * 登记会计流水
     */
    @Override
    public void registKnsAcsqCler(IoAccountClearInfo clearinfo) {

        if (CommUtil.isNull(clearinfo.getTrandt())) {

            clearinfo.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
        }
        if (CommUtil.isNull(clearinfo.getMntrsq())) {

            clearinfo.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
        }

        //clearinfo.setRecdno(Long.parseLong(SequenceManager.nextval("KnsAcsqCler")));//记录次序号
        clearinfo.setRecdno(Long.parseLong(CoreUtil.nextValue("KnsAcsqCler")));//记录次序号

        if (CommUtil.isNull(clearinfo.getClerdt())) {

            clearinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());////清算日期
        }

        if (CommUtil.isNull(clearinfo.getServtp())) {
            if (null == CommTools.getBaseRunEnvs().getChannel_id()) {
                throw DpModuleError.DpstComm.BNAS0384();
            }
            clearinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());////清算日期
        }
        clearinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次

        KnsAcsqCler tblClearInfo = SysUtil.getInstance(KnsAcsqCler.class);
        CommUtil.copyProperties(tblClearInfo, clearinfo);

        KnsAcsqClerDao.insert(tblClearInfo);
    }

    /**
     * 查询会计流水清算信息
     */
    @Override
    public Options<IoAccountClearInfo> queryKnsAcsqCler(String trandt, String mntrsq) {

        List<IoAccountClearInfo> cplKnsAcsqCler = DpAcctDao.selKnsAcsqCler(trandt, mntrsq, false);

        Options<IoAccountClearInfo> list = new DefaultOptions<>();
        for (IoAccountClearInfo cler : cplKnsAcsqCler) {
            IoAccountClearInfo info = SysUtil.getInstance(IoAccountClearInfo.class);
            CommUtil.copyProperties(info, cler);
            list.add(info);
        }

        return list;
    }

    /**
     * 
     * @Auther renjinghua
     *         <p>
     *         <li>2017年1月22日-上午10:34:26</li>
     *         <li>功能说明：查询系统内跨法人清算信息</li>
     *         <p>
     * 
     * @param trandt 交易日期
     * @param mntrsq 交易流水
     * @return 系统内跨法人清算信息
     */
    @Override
    public Options<IoAccountClearInfo> qryKnsAcsqClin(String trandt, String mntrsq) {

        List<IoAccountClearInfo> lstKnsAcsqClin = DpAcctDao.selKnsAcsqClin(trandt, mntrsq, false);

        Options<IoAccountClearInfo> lstClearInfo = new DefaultOptions<IoAccountClearInfo>();
        for (IoAccountClearInfo cplAcsqClin : lstKnsAcsqClin) {
            IoAccountClearInfo cplClearInfo = SysUtil.getInstance(IoAccountClearInfo.class);
            CommUtil.copyProperties(cplClearInfo, cplAcsqClin);
            lstClearInfo.add(cplClearInfo);
        }

        return lstClearInfo;
    }

    /**
     * 查询产品核算信息
     */
    @Override
    public Options<IoKnsProdClerInfo> selKnsProdClerInfo(String corpno, String prodcd, String eventp) {

        List<IoKnsProdClerInfo> cplKnsProdCler = DpAcctDao.selKnsProdCler(corpno, prodcd, eventp, false);

        Options<IoKnsProdClerInfo> list = new DefaultOptions<>();
        for (IoKnsProdClerInfo cler : cplKnsProdCler) {
            IoKnsProdClerInfo info = SysUtil.getInstance(IoKnsProdClerInfo.class);
            CommUtil.copyProperties(info, cler);
            list.add(info);
        }

        return list;
    }

    /**
     * 电子账户记账前状态检查
     * iscodr 借方检查
     * clactp 销户检查
     */
    @Override
    public void checkStatusBeforeAccount(String custac, E_AMNTCD amntcd, E_YES___ iscodr, E_YES___ clactp) {

        if (iscodr == null) {

            iscodr = E_YES___.YES;
        }

        IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb1(custac, E_DPACST.NORMAL, false);

        if (CommUtil.isNull(tblKnaAcdc) || CommUtil.isNull(tblKnaAcdc.getCardno())) {

            throw DpModuleError.DpstAcct.BNAS1695();
        }
        E_CUACST status = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);//查询电子账户状态信息

        IoDpAcStatusWord cplGetAcStWord = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);

        if (cplGetAcStWord.getDbfroz() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {

            throw DpModuleError.DpstAcct.BNAS1696(tblKnaAcdc.getCardno());
        }
        if (E_AMNTCD.DR == amntcd) {

            if (cplGetAcStWord.getAlstop() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {
                throw DpModuleError.DpstAcct.BNAS1697(tblKnaAcdc.getCardno());
            }
            //借方
            if (clactp == E_YES___.YES) {
                if (status != E_CUACST.NORMAL && status != E_CUACST.PRECLOS) {
                    throw DpModuleError.DpstAcct.BNAS1698(tblKnaAcdc.getCardno(), status.getLongName());
                }
            } else {
                if (status != E_CUACST.NORMAL) {
                    throw DpModuleError.DpstAcct.BNAS1699(tblKnaAcdc.getCardno(), status.getLongName());
                }
            }

            if (iscodr == E_YES___.YES) {

                if (cplGetAcStWord.getBrfroz() == cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___.YES) {

                    throw DpModuleError.DpstAcct.BNAS1700(tblKnaAcdc.getCardno());
                }
            }

        } else if (E_AMNTCD.CR == amntcd) {
            if (status == E_CUACST.PREOPEN || status == E_CUACST.PRECLOS || status == E_CUACST.CLOSED || status == E_CUACST.DELETE) {

                throw DpModuleError.DpstAcct.BNAS1701(tblKnaAcdc.getCardno(), status.getLongName());
            }
        } else {
            throw DpModuleError.DpstAcct.BNAS1702(amntcd.toString());
        }

    }

    /**
     * 
     * @Auther renjinghua
     *         <p>
     *         <li>2016年12月23日-上午11:27:40</li>
     *         <li>功能说明：系统内跨法人记账时，登记系统内会计流水清算信息</li>
     *         <p>
     * 
     * @param clearinfo 会计流水清算信息
     */
    @Override
    public void registKnsAcsqClin(IoAccountClearInfo clearinfo) {
        // TODO Auto-generated method stub
        if (CommUtil.isNull(clearinfo.getTrandt())) {

            clearinfo.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
        }
        if (CommUtil.isNull(clearinfo.getMntrsq())) {

            clearinfo.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
        }

        //clearinfo.setRecdno(Long.parseLong(SequenceManager.nextval("KnsAcsqClin")));//记录次序号
        clearinfo.setRecdno(Long.parseLong(CoreUtil.nextValue("KnsAcsqClin")));//记录次序号
        if (CommUtil.isNull(clearinfo.getClerdt())) {

            clearinfo.setClerdt(BusiTools.getBusiRunEnvs().getClerdt());////清算日期
        }

        if (CommUtil.isNull(clearinfo.getServtp())) {
            if (null == CommTools.getBaseRunEnvs().getChannel_id()) {
                throw DpModuleError.DpstComm.BNAS0384();
            }
            clearinfo.setServtp(CommTools.getBaseRunEnvs().getChannel_id());////清算日期
        }
        clearinfo.setClenum(BusiTools.getBusiRunEnvs().getClenum());//清算场次

        KnsAcsqClin tblAcsqClin = SysUtil.getInstance(KnsAcsqClin.class);
        CommUtil.copyProperties(tblAcsqClin, clearinfo);

        KnsAcsqClinDao.insert(tblAcsqClin);
    }

    /**
     * <p>Title:getKnsAcsq </p>
     * <p>Description:业务流水查询 </p>
     * 
     * @author XJW
     * @date 2018年3月5日
     * @param qrtype 查询方式
     * @param bgindt 开始日期
     * @param endddt 结束日期
     * @param acctno 账号
     * @param transq 业务流水
     * @param cardno 卡号
     * @return 会计记账流水信息
     */
    @Override
    public Options<IoKnsAcsqInfo> getKnsAcsq(String bgindt, String endddt, String acctno,
            String mntrsq, String cardno,String sortno) {
        // 页码
        long iPageno = CommTools.getBaseRunEnvs().getPage_start();
        // 页面大小
        long iPgsize = CommTools.getBaseRunEnvs().getPage_size();
        // 会记记账流水集合
        Options<IoKnsAcsqInfo> cplKnsAcsqInfoList = new DefaultOptions<>();
        //交易日期
        String trandt = CommTools.getBaseRunEnvs().getTrxn_date();

        // 卡号/负债账号、主交易流水不能同时为空
        if (CommUtil.isNull(cardno) && CommUtil.isNull(acctno) && CommUtil.isNull(mntrsq)) {
            throw DpAcError.DpDeptAcct.BNAS3009();
        }

        if (CommUtil.isNotNull(bgindt) && CommUtil.compare(bgindt, trandt) > 0) {
            throw DpAcError.DpDeptAcct.BNAS0413();
        }

        if (CommUtil.isNotNull(endddt) && CommUtil.compare(endddt, bgindt) < 0) {
            throw DpAcError.DpDeptAcct.BNAS0598();
        }

        // 根据卡号获取账号
//        if (CommUtil.isNotNull(cardno) && CommUtil.isNull(acctno)) {
//            acctno = CapitalTransDeal.getSettKnaAcctSub(KnaAcdcDao.selectOne_odb2(cardno, true).getCustac(), null).getAcctno();
//        }
        // 查询会计流水表信息
        Page<IoKnsAcsqInfo> acsqInfoList = DpAcctDao.selKnsAcsqInfosByAcctOrSq(trandt, mntrsq, bgindt, endddt, acctno,sortno, (iPageno - 1) * iPgsize, iPgsize, 0, false);
        List<IoKnsAcsqInfo> lstKnsAcsqInfo = acsqInfoList.getRecords();

        String sAcctna = "";

        if (lstKnsAcsqInfo.size() > 0) {
            //根据账户来查,如果卡号/负债账号、流水同时输入，优先用账号去查
            if (CommUtil.isNotNull(cardno) || CommUtil.isNotNull(acctno)) {
                // 账号
                String sAcctno = lstKnsAcsqInfo.get(0).getAcctno();
                // 会计主体类型
                E_ATOWTP atowtp = lstKnsAcsqInfo.get(0).getAtowtp();
                // 余额属性
                E_BLTYPE bltype = lstKnsAcsqInfo.get(0).getBltype();
                // 记账账号
                String cuacno = lstKnsAcsqInfo.get(0).getCuacno();
                /** 获取账号名称 */
                sAcctna = qryAcctna(sAcctna, sAcctno, cuacno, atowtp, bltype);

                for (IoKnsAcsqInfo list : lstKnsAcsqInfo) {
                    list.setAcctna(sAcctna);
                }
            } else if (CommUtil.isNull(cardno) && CommUtil.isNull(acctno)) {
                for (IoKnsAcsqInfo list : lstKnsAcsqInfo) {
                    /** 获取账号名称 */
                    sAcctna = qryAcctna(sAcctna, list.getAcctno(), list.getCuacno(), list.getAtowtp(), list.getBltype());

                    list.setAcctna(sAcctna);
                }
            }
        }

        cplKnsAcsqInfoList.addAll(lstKnsAcsqInfo);
        CommTools.getBaseRunEnvs().setTotal_count(acsqInfoList.getRecordCount());

        return cplKnsAcsqInfoList;
    }

    /**
     * <p>Title:qryAcctna </p>
     * <p>Description: 获取账号名称</p>
     * 
     * @author XJW
     * @param sAcctna 会计记账流水信息
     * @date 2018年3月7日
     * @param sAcctno 账号
     * @param cuacno
     * @param atowtp 会计主体类型
     * @param bltype 余额属性
     */
    private String qryAcctna(String sAcctna, String sAcctno, String cuacno, E_ATOWTP atowtp, E_BLTYPE bltype) {
        if (atowtp == E_ATOWTP.IN) {
            // 内部户名称
            sAcctna = SysUtil.getInstance(IoInQuery.class).InacInfoQuery(sAcctno).getAcctna();
        } else if (atowtp == E_ATOWTP.DP && E_BLTYPE.BALANCE == bltype) {
            String custac="";
            // 如果负债活期账户为空，就查负债定期账户
            KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(sAcctno, false);
            if(CommUtil.isNull(tblKnaAcct)){
                custac = KnaFxacDao.selectOne_odb1(sAcctno, false).getCustac();
            }else{
                custac = tblKnaAcct.getCustac();
            }
            EacctCustnaCardno cplCust = SysUtil.getInstance(ServEacctSvcType.class).qryCustnaAndCardno(custac);
            if (CommUtil.isNull(cplCust.getCardno())) {
                throw DpModuleError.DpstAcct.BNAS0944(sAcctno);
            }
            // 账户名称
            sAcctna = cplCust.getCustna();
        } else if (atowtp == E_ATOWTP.FE) {
            IoSelChrgAccRule.Output output = SysUtil.getInstance(IoSelChrgAccRule.Output.class);
            IoCgChrgSvcType.IoSelChrgAccRule.InputSetter input = SysUtil.getInstance(IoSelChrgAccRule.InputSetter.class);

            //给input赋值
            input.setChrgcd(cuacno);

            SysUtil.getInstance(IoCgChrgSvcType.class).SelChrgAccRule(input, output);

            //获取费种代码名称
            sAcctna = output.getPinfos().getValues().get(0).getChrgna();
        }else if(atowtp == E_ATOWTP.LN){
        	sAcctna = DpAcctDao.selAcctnaByAcctno(sAcctno, false);
        }
        return sAcctna;
    }

    /**
     * <p>Title:qryAcctna </p>
     * <p>Description: 查询账户交易明细信息</p>
     * 
     * @author XJW
     * @date 2018年3月9日
     * @param sAcctno 账号
     * @param bgindt 开始日期
     * @param endddt 结束日期
     * @return 账户发生明细信息
     */
    public Options<IoKnlBillInfo> QueryKnlBill(String acctno, String bgindt, String endddt) {
     // 页码
        long iPageno = CommTools.getBaseRunEnvs().getPage_start();
        // 页面大小
        long iPgsize = CommTools.getBaseRunEnvs().getPage_size();
        // 会记记账流水集合
        Options<IoKnlBillInfo> cplIoKnlBillInfoList = new DefaultOptions<>();
        //交易日期
        String trandt = CommTools.getBaseRunEnvs().getTrxn_date();

        // 负债账号不能为空
        if (CommUtil.isNull(acctno)) {
            throw DpModuleError.DpstComm.BNAS0190();
        }

        if (CommUtil.isNotNull(bgindt) && CommUtil.compare(bgindt, trandt) > 0) {
            throw DpModuleError.DpstAcct.BNAS0413();
        }

        if (CommUtil.isNotNull(endddt) && CommUtil.compare(endddt, bgindt) < 0) {
            throw DpModuleError.DpstAcct.BNAS0598();
        }
        
        Page<IoKnlBillInfo> KnlbillList = DpAcctDao.selknlbillList(trandt, bgindt, endddt, acctno, (iPageno - 1) * iPgsize, iPgsize, 0, false);
    
        cplIoKnlBillInfoList.addAll(KnlbillList.getRecords());
        
        CommTools.getBaseRunEnvs().setTotal_count(KnlbillList.getRecordCount());

        return cplIoKnlBillInfoList;
    }

	public Options<IoKnsAcsqInfoWithBradna> getKnsAcsqWithBradna( String bgindt,  String endddt,  String acctno,  String mntrsq,  String cardno,  String sortno,  String acctna,  cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD amntcd,  cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP atowtp,  java.math.BigDecimal tranam,  String bradna){
		// 页码
        long iPageno = CommTools.getBaseRunEnvs().getPage_start();
        // 页面大小
        long iPgsize = CommTools.getBaseRunEnvs().getPage_size();
        // 会记记账流水集合
        Options<IoKnsAcsqInfoWithBradna> cplKnsAcsqInfoList = new DefaultOptions<>();
        //交易日期
        String trandt = CommTools.getBaseRunEnvs().getTrxn_date();

        // 卡号/负债账号、主交易流水不能同时为空
        if (CommUtil.isNull(cardno) && CommUtil.isNull(acctno) && CommUtil.isNull(mntrsq)) {
        //    throw DpModuleError.DpstAcct.BNAS3009();
        }

        if (CommUtil.isNotNull(bgindt) && CommUtil.compare(bgindt, trandt) > 0) {
            throw DpModuleError.DpstAcct.BNAS0413();
        }

        if (CommUtil.isNotNull(endddt) && CommUtil.compare(endddt, bgindt) < 0) {
            throw DpModuleError.DpstAcct.BNAS0598();
        }

        // 根据卡号获取账号
//        if (CommUtil.isNotNull(cardno) && CommUtil.isNull(acctno)) {
//            acctno = CapitalTransDeal.getSettKnaAcctSub(KnaAcdcDao.selectOne_odb2(cardno, true).getCustac(), null).getAcctno();
//        }
        // 查询会计流水表信息
        Page<IoKnsAcsqInfoWithBradna> acsqInfoWithBradnaList = DpAcctDao.selKnsAcsqInfosByAcctOrSqWithBradna(mntrsq, bgindt, endddt, acctno,sortno, acctna, amntcd, atowtp, tranam, bradna, cardno, (iPageno - 1) * iPgsize, iPgsize, 0, false);

        List<IoKnsAcsqInfoWithBradna> lstKnsAcsqWithBradnaInfo = acsqInfoWithBradnaList.getRecords();

        String sAcctna = "";

        cplKnsAcsqInfoList.addAll(lstKnsAcsqWithBradnaInfo);
        CommTools.getBaseRunEnvs().setTotal_count(acsqInfoWithBradnaList.getRecordCount());

        return cplKnsAcsqInfoList;
	}	
}

