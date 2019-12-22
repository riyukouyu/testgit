package cn.sunline.ltts.busi.dp.serviceimpl;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_EVENTLEVEL;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_EVNTST;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_YESORNO;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.iobus.type.DpFrozType.CustAcctInfo;
import cn.sunline.edsp.busi.dp.iobus.type.DpFrozType.DpFrozInfo;
import cn.sunline.edsp.busi.dp.iobus.type.DpFrozType.DpFrozOutInfo;
import cn.sunline.edsp.busi.dp.iobus.type.DpFrozType.frozOutInfo;
import cn.sunline.edsp.busi.dp.type.jfbase.JFBaseEnumType;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsRedu;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsReduDao;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.base.DecryptConstant;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.type.CaCustInfo;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.domain.DpFrozEntity;
import cn.sunline.ltts.busi.dp.domain.DpOpprEntity;
import cn.sunline.ltts.busi.dp.domain.DpUnfrEntity;
import cn.sunline.ltts.busi.dp.domain.QrbackEntity;
import cn.sunline.ltts.busi.dp.froz.DpAcctStatus;
import cn.sunline.ltts.busi.dp.froz.DpDeduct;
import cn.sunline.ltts.busi.dp.froz.DpFrozProc;
import cn.sunline.ltts.busi.dp.froz.DpFrozQuery;
import cn.sunline.ltts.busi.dp.froz.DpFrozTools;
import cn.sunline.ltts.busi.dp.froz.DpUnfrProc;
import cn.sunline.ltts.busi.dp.froz.DpUnfrReversal;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.DpFrozDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDedu;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDeduDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetl;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetlDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwne;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwneDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbUnfr;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbUnfrDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDelay;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDelayDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDelayDetl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDelayDetlDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.DeductOut;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoDpQryFrozInfos.Input;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoQueryFeedBack.Output;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
//import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoKnsAcsqInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpSpecInfoListForDeduIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpSpecInfoListForDeprIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpSpecInfoListForFrozIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpSpecInfoListIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoAcToAcDelayInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoCtfrozPayIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoCtfrozPayOt;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoCustacDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoCustacInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDeductOt;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpCustopInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpFrozIf;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpFrozQrIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpFrozQrOt;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpQryFrozInfoList;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpQryFrozTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpQryUnBalList;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpSpecInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStFzIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStFzOt;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStUfIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStUfOt;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStopPayOt;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStopayIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStopayOt;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpUnStopPayIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpUnStopPayOt;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpUnStopayIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpUnStopayOt;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpprovIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpprovOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoFrozInfoOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoQrBackInfoIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoQrBackInfoOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IofrozInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.danBiFrozInfo;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;
import cn.sunline.ltts.busi.sys.type.ApSmsType.ToAppSendMsg;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEPRTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPCGFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUREAS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DELAYT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRSPTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_OTSPTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_QRFRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SPECST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SPECTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_STOPTP;

/**
 * 对外冻结止付实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
public class IoDpFrozSrvTypeImpl implements
        cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType {
    private static final BizLog log = BizLogUtil
            .getBizLog(IoDpFrozSrvTypeImpl.class);

    /**
     * 司法冻结
     * 
     * @return
     * 
     */
    public IoDpStopPayOt IoDpFrozByLaw(
            final cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStopPayIn cpliodpfrozin) {

    	// 司法冻结检查
        DpFrozProc.frozByLawCheck(cpliodpfrozin);
        
        DpFrozEntity entity = new DpFrozEntity();
        IoDpKnaAcct acctInfo=SysUtil.getInstance(IoDpKnaAcct.class);
    	IoCaKnaAcdc custInfo=SysUtil.getInstance(IoCaKnaAcdc.class);
    
       	//查询子账户信息
    	custInfo=DpFrozDao.selCardCustacByAcctno(cpliodpfrozin.getCardno(),false);  	
    		
       	if(custInfo!=null) {       	
       	//主账户冻结信息验证
       		List<IoDpKnbFroz> doubleList = DpFrozDao.selFrozListByFrozwy(custInfo.getCustac(),
       				DpEnumType.E_FROZST.VALID, DpEnumType.E_FROZTP.JUDICIAL, DpEnumType.E_FRLMTP.ALL, false);
       		if(doubleList.size() == 0 || doubleList == null) {
       			List<IoDpKnbFroz> DList = DpFrozDao.selFrozListByFrozwy(custInfo.getCustac(),
       					DpEnumType.E_FROZST.VALID, DpEnumType.E_FROZTP.JUDICIAL, DpEnumType.E_FRLMTP.OUT, false);
       		    if(DList.size() != 0 && DList != null) {
       		    	if(DpEnumType.E_FROZWY.DOUBLE != cpliodpfrozin.getFrozwy()) {
       		    		throw DpModuleError.DpAcct.AT020039();
       		    	}
       		    }
       		}else {
       			throw DpModuleError.DpAcct.AT020040();
       		}      		
       	}else {
       			throw DpModuleError.DpAcct.AT020028();
       	}
       	
   		if(JFBaseEnumType.E_STACTP.STSA==cpliodpfrozin.getStactp()) {       		
   			acctInfo=DpFrozDao.selAcctInfoByAcalno(cpliodpfrozin.getAcctno(), false);
   			if(acctInfo!=null) {
   				cpliodpfrozin.setAcctno(acctInfo.getAcctno());    
   				//子账户冻结信息验证
   				List<IoDpKnbFroz>doubleListZ= DpFrozDao.selFrozListByFrozwy(acctInfo.getAcctno(),
   						DpEnumType.E_FROZST.VALID, DpEnumType.E_FROZTP.JUDICIAL, DpEnumType.E_FRLMTP.ALL,false);
   				if(doubleListZ.size()==0||doubleListZ==null) {
   					List<IoDpKnbFroz>DListZ= DpFrozDao.selFrozListByFrozwy(acctInfo.getAcctno(),
   							DpEnumType.E_FROZST.VALID, DpEnumType.E_FROZTP.JUDICIAL, DpEnumType.E_FRLMTP.OUT,false);
   					if(DListZ.size()!=0&&DListZ!=null) {
   						if(DpEnumType.E_FROZWY.DOUBLE!=cpliodpfrozin.getFrozwy()) {
   							throw DpModuleError.DpAcct.AT020041();
   						}
   					}
   				}else {
   					throw DpModuleError.DpAcct.AT020042();
   				}
   			}else {
   				throw DpModuleError.DpAcct.AT020035();
   			}
   		}
    	
       	if(JFBaseEnumType.E_STACTP.STSA==cpliodpfrozin.getStactp()) {
         	if(acctInfo!=null){
         		if (!CommUtil.equals(cpliodpfrozin.getCrcycd(), acctInfo.getCrcycd())) {
         			throw DpModuleError.DpAcct.AT020053();
         		}
         		
         		E_DPACST cuacst1=acctInfo.getAcctst();
         		if (cuacst1 == E_DPACST.CLOSE) {
         			throw DpModuleError.DpstComm.BNAS1597();
         		}  
         	}else {
         		throw DpModuleError.DpAcct.AT020035();
     		}
        }

        cpliodpfrozin.setFrozow(E_FROZOW.AUACCT);// 冻结主体类型设置为智能储蓄
        cpliodpfrozin.setFrozcd("DEFAULT");// 冻结分类码设置为DEFAULT
        cpliodpfrozin.setFroztp(E_FROZTP.JUDICIAL);// 冻结业务类型默认为司法冻结
        /*定期子账号冻结必须输入定期负债账号*/
//        if (cpliodpfrozin.getFricfg() == E_YES___.YES)
//        {
//            cpliodpfrozin.setFrozow(E_FROZOW.ACCTNO);
//        }

        // 司法冻结
        DpFrozProc.prcFroz(cpliodpfrozin, entity);

        // 获取账户余额(包括部冻、部止金额)
        BigDecimal acctbal = DpFrozProc.accountBal(cpliodpfrozin);
        if(JFBaseEnumType.E_STACTP.STSA==cpliodpfrozin.getStactp()) {
        	if(acctInfo!=null) {
        		acctbal=acctInfo.getOnlnbl();
        	}else {
        		throw  DpModuleError.DpAcct.AT020035();
    		}
        }
        
        // 获取在先冻结信息
        //List<IoFrozHistInfo> frozInfo = DpFrozProc.getFrozHistInfo(cpliodpfrozin);
        //Options<IoFrozHistInfo> OptionInfo = new DefaultOptions<>();
        //OptionInfo.addAll(frozInfo);
        
        // 将输入作为返回值
        IoDpStopPayOt cplIoDpStopPayOt = SysUtil
                .getInstance(IoDpStopPayOt.class);
        CommUtil.copyProperties(cplIoDpStopPayOt, cpliodpfrozin);
        if (JFBaseEnumType.E_STACTP.STMA == cpliodpfrozin.getStactp()) {
        	cpliodpfrozin.setAcctno(custInfo.getCustac());
		}
        cplIoDpStopPayOt.setFrozno(entity.getFrozno());
        cplIoDpStopPayOt.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
        cplIoDpStopPayOt.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
        cplIoDpStopPayOt.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());
        cplIoDpStopPayOt.setCustno(entity.getCustno());
        //cplIoDpStopPayOt.setFrozInfo(OptionInfo);
        cplIoDpStopPayOt.setOnlnbl(acctbal);
        
        String acalno=cpliodpfrozin.getAcalno();
        String custna=cpliodpfrozin.getCustna();
        String frna01=cpliodpfrozin.getFrna01();
        String frna02=cpliodpfrozin.getFrna02();
        String idno01=cpliodpfrozin.getIdno01();
        String idno02=cpliodpfrozin.getIdno02();
        
        if(CommUtil.isNotNull(acalno)) {
        	cplIoDpStopPayOt.setTmtlphno(DecryptConstant.maskMobile(acalno));
        }
        if(CommUtil.isNotNull(custna)) {
        	cplIoDpStopPayOt.setTmcustna(DecryptConstant.maskName(custna));
        }
        if(CommUtil.isNotNull(frna01)) {
        	cplIoDpStopPayOt.setTmfrna01(DecryptConstant.maskName(frna01));
        }
        if(CommUtil.isNotNull(frna02)) {
        	cplIoDpStopPayOt.setTmfrna01(DecryptConstant.maskName(frna02));
         }
        if(CommUtil.isNotNull(idno01)) {
        	cplIoDpStopPayOt.setTmidno01(DecryptConstant.maskIdCard(idno01));
         }
        if(CommUtil.isNotNull(idno02)) {
        	cplIoDpStopPayOt.setTmidno02(DecryptConstant.maskIdCard(idno02));
         }
        // 返回
        return cplIoDpStopPayOt;
    }

    /**
     * @author douwenbo
     * @return
     * @date 2016-06-14 19:22 冻结反馈有权机构信息
     */
    public void queryFeedBack(IoQrBackInfoIn ioQrBackInfoIn, Output output) {

        QrbackEntity entity = new QrbackEntity();

        // 冻结反馈有权机构
        DpFrozProc.checkQrBackInfoIn(ioQrBackInfoIn, entity, output);

        IoQrBackInfoOut qrBackInfoOut = SysUtil
                .getInstance(IoQrBackInfoOut.class);

        qrBackInfoOut.setFrctno(entity.getFrctno());
        qrBackInfoOut.setFreddt(entity.getFreddt());
        qrBackInfoOut.setFrexog(entity.getFrexog());
        qrBackInfoOut.setFrogna(entity.getFrogna());
        qrBackInfoOut.setFrozam(entity.getFrozam());

    }

    /**
     * @author douwenbo
     * @date 2016-04-21 16:02 解冻
     * @return
     */
    @Override
    public IoDpUnStopPayOt IoDpUnfrByLaw(IoDpUnStopPayIn cpliodpunfrozin) {

    	// 解冻检查
        DpUnfrProc.unfrByLawCheck(cpliodpunfrozin);

        DpUnfrEntity entity = new DpUnfrEntity();
        /*IoDpKnaAcct acctInfo = SysUtil.getInstance(IoDpKnaAcct.class);
         
        //查询子账户信息
   		if(JFBaseEnumType.E_STACTP.STSA == cpliodpunfrozin.getStactp()) {
   			acctInfo = DpFrozDao.selAcctInfoByAcalno(cpliodpunfrozin.getAcctno(), false);
   			if(acctInfo != null) {
   				cpliodpunfrozin.setAcctno(acctInfo.getAcctno());    
   			}
   		} */    		
      	
        // 解冻
        DpUnfrProc.prcUnfroz(cpliodpunfrozin, entity);

        // 设置字段到输出接口中
        IoDpUnStopPayOt cplIoDpUnStopPayOt = DpUnfrProc.setUnfiOutPut(
                cpliodpunfrozin, entity);

        cplIoDpUnStopPayOt.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());
        cplIoDpUnStopPayOt.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());

        //poc增加审计日志
//        KnaAcdc kacdc = KnaAcdcDao.selectFirst_odb1(cpliodpunfrozin.getCardno(), E_DPACST.NORMAL, false);
//        if (CommUtil.isNotNull(kacdc)) {
//            ApDataAudit apAudit = SysUtil.getInstance(ApDataAudit.class);
//            apAudit.regLogOnInsertBusiPoc(kacdc.getCardno());
//        }
        
        String acalno=cpliodpunfrozin.getAcalno();
        String custna=cpliodpunfrozin.getCustna();
        String ufna01=cpliodpunfrozin.getUfna01();
        String ufna02=cpliodpunfrozin.getUfna02();
        String idno01=cpliodpunfrozin.getIdno01();
        String idno02=cpliodpunfrozin.getIdno02();
        
        if(CommUtil.isNotNull(acalno)) {
        	cplIoDpUnStopPayOt.setTmtlphno(DecryptConstant.maskMobile(acalno));
        }
        if(CommUtil.isNotNull(custna)) {
        	cplIoDpUnStopPayOt.setTmcustna(DecryptConstant.maskName(custna));
        }
        if(CommUtil.isNotNull(ufna01)) {
        	cplIoDpUnStopPayOt.setTmufna01(DecryptConstant.maskName(ufna01));
        }
        if(CommUtil.isNotNull(ufna02)) {
        	cplIoDpUnStopPayOt.setTmufna02(DecryptConstant.maskName(ufna02));
         }
        if(CommUtil.isNotNull(idno01)) {
        	cplIoDpUnStopPayOt.setTmidno01(DecryptConstant.maskIdCard(idno01));
         }
        if(CommUtil.isNotNull(idno02)) {
        	cplIoDpUnStopPayOt.setTmidno02(DecryptConstant.maskIdCard(idno02));
         }
        
        // 冲正登记
        IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
        cplInput.setTranam(cpliodpunfrozin.getUnfram());// 解冻金额
        if (CommUtil.isNotNull(cpliodpunfrozin.getOdfrno())) {
            cplInput.setEvent1(cpliodpunfrozin.getOdfrno()); // 原冻结编号
        }
        IoMsRegEvent ioMsReg = SysUtil.getInstance(IoMsRegEvent.class);
		ioMsReg.setEvent_status(E_EVNTST.SUCCESS);//事件状态SUCCESS（成功）STRIKED（已冲正）NEED2C（需要二次提交）
		ioMsReg.setInformation_value(SysUtil.serialize(cplInput));
    	ioMsReg.setReversal_event_id("strkeDpUnfrAcct");//冲正事件ID
    	ioMsReg.setService_id("strkeDpUnfrAcct");//服务ID
		ioMsReg.setSub_system_id(CoreUtil.getSubSystemId());//子系统ID
		ioMsReg.setTxn_event_level(E_EVENTLEVEL.LOCAL);//教义事件级别NORMAL（）INQUIRE（）LOCAL（）CRDIT（）
		ioMsReg.setIs_across_dcn(E_YESORNO.NO);
		
		MsEvent.register(ioMsReg, true);
        
        // 输出
        return cplIoDpUnStopPayOt;
    }

    /**
     * 系统冻结
     * 
     * @author douwenbo
     * @date 2016-06-01 15:41
     * @param cpliodpstfzin
     *        系统冻结输入接口
     * @return
     */
    @Override
    public IoDpStFzOt IoDpStFz(IoDpStFzIn cpliodpstfzin) {

        // 系统冻结检查
        DpFrozProc.syetemFrozCheck(cpliodpstfzin);

        // 系统冻结
        DpFrozProc.systemFrozDo(cpliodpstfzin);

        IoDpStFzOt cpliodpstfzot = SysUtil.getInstance(IoDpStFzOt.class);

        // 将输入作为返回值
        // CommUtil.copyProperties(cpliodpstfzot, cpliodpstfzin);
        cpliodpstfzot.setFrozdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 冻结日期
        cpliodpstfzot.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
        cpliodpstfzot.setRemark(cpliodpstfzin.getFroztp().getValue()); // 备注

        return cpliodpstfzot;
    }

    /**
     * 
     * 功能描述：系统解冻
     * 
     * @author douwenbo
     * @date 2016年6月22日-下午3:38:51
     * @param cpliodpstufin
     * @return
     */
    @Override
    public IoDpStUfOt IoDpStUf(IoDpStUfIn cpliodpstufin) {

        /**
         * 上送解冻可能不是本系统的原交易流水为上送流水，所以要先查找下
         */
        if (CommUtil.isNotNull(cpliodpstufin.getInpudt()) && CommUtil.isNotNull(cpliodpstufin.getInpusq())) {
            KnsRedu knsReduDO = KnsReduDao.selectFirst_odb4(cpliodpstufin.getInpusq(), cpliodpstufin.getInpudt(), false);
            if (CommUtil.isNotNull(knsReduDO)) {
                cpliodpstufin.setMntrsq(knsReduDO.getTransq());
                cpliodpstufin.setTrandt(knsReduDO.getTrandt());
            }
        }
        // 系统解冻检查
        DpFrozProc.syetemUnfrCheck(cpliodpstufin);

        // 系统解冻
        DpFrozProc.systemUnfrDo(cpliodpstufin);
        // 将输入作为返回值
        IoDpStUfOt cplIoDpStUfOt = SysUtil.getInstance(IoDpStUfOt.class);
        CommUtil.copyProperties(cplIoDpStUfOt, cpliodpstufin);

        // cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoDpStUf.Output
        // output = SysUtil
        // .getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoDpStUf.Output.class);
        // output.setCpliodpstufot(cplIoDpStUfOt);
        // 返回
        return cplIoDpStUfOt;
    }

    /**
     * @author douwenbo
     * @date 2016-04-22 14:02 续冻
     */
    @Override
    public IoCtfrozPayOt IoDpCtfrByLaw(IoCtfrozPayIn cplioctfrozin) {
        DpFrozEntity entity = new DpFrozEntity();
        IoDpKnaAcct acctInfo=SysUtil.getInstance(IoDpKnaAcct.class);

   		if(JFBaseEnumType.E_STACTP.STSA==cplioctfrozin.getStactp()) {
   		  acctInfo=DpFrozDao.selAcctInfoByAcalno(cplioctfrozin.getAcctno(), false);
   		  if(acctInfo!=null) {
   			cplioctfrozin.setAcctno(acctInfo.getAcctno());    
   		    if (!CommUtil.equals(cplioctfrozin.getCrcycd(), acctInfo.getCrcycd())) {
             throw DpModuleError.DpstComm.BNAS0313();
              }       
   		  }else {
      		throw  DpModuleError.DpAcct.AT020035();
 		  }
   		}
       
        // 续冻检查
        DpFrozProc.ctfrozByLawCheck(cplioctfrozin);

        // 续冻
        DpFrozProc.prcCtfr(cplioctfrozin, entity);

        // 将输入作为返回值
        IoCtfrozPayOt cplIoCtfrozPayOt = SysUtil
                .getInstance(IoCtfrozPayOt.class);
        CommUtil.copyProperties(cplIoCtfrozPayOt, cplioctfrozin);

        cplIoCtfrozPayOt.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());
        cplIoCtfrozPayOt.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
        cplIoCtfrozPayOt.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
        cplIoCtfrozPayOt.setFrreas(cplioctfrozin.getCtfrre());
        cplIoCtfrozPayOt.setCustno(entity.getCustno());

        String acalno=cplioctfrozin.getAcalno();
        String custna=cplioctfrozin.getCustna();
        String frna01=cplioctfrozin.getFrna01();
        String frna02=cplioctfrozin.getFrna02();
        String idno01=cplioctfrozin.getIdno01();
        String idno02=cplioctfrozin.getIdno02();
        
        if(CommUtil.isNotNull(acalno)) {
        	cplIoCtfrozPayOt.setTmtlphno(DecryptConstant.maskMobile(acalno));
        }
        if(CommUtil.isNotNull(custna)) {
        	cplIoCtfrozPayOt.setTmcustna(DecryptConstant.maskName(custna));
        }
        if(CommUtil.isNotNull(frna01)) {
        	cplIoCtfrozPayOt.setTmfrna01(DecryptConstant.maskName(frna01));
        }
        if(CommUtil.isNotNull(frna02)) {
        	cplIoCtfrozPayOt.setTmfrna01(DecryptConstant.maskName(frna02));
         }
        if(CommUtil.isNotNull(idno01)) {
        	cplIoCtfrozPayOt.setTmidno01(DecryptConstant.maskIdCard(idno01));
         }
        if(CommUtil.isNotNull(idno02)) {
        	cplIoCtfrozPayOt.setTmidno02(DecryptConstant.maskIdCard(idno02));
         }
      
        // 返回
        return cplIoCtfrozPayOt;
    }

    /**
     * @author douwenbo
     * @date 2016-04-28 14:16 特殊业务查询
     */

    
    public void queryDpSpec(
            DpSpecInfoListIn specInfoListIn,
            Long pageno,
            Long pgsize,
            cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoQueryDpSpec.Output output) {

        long totlCount = 0; // 记录总数
        long counts = 0l; // 总笔数

        long startno = (pageno - 1) * pgsize;// 起始记录数

        // 获取特殊业务信息集合输出接口
        // DpSpecInfoListOt cplDpSpecInfoList =
        // SysUtil.getInstance(DpSpecInfoListOt.class);

        // 特殊业务查询输入参数的基本检查
        DpFrozQuery.specInfoListInCheck(specInfoListIn);

        if (specInfoListIn.getSpectp() == E_SPECTP.FREEZE) {// 特殊业务类型为 1冻结/解冻
            Page<IoDpSpecInfo> specList = DpFrozQuery
                    .querySpecInfoListForFreeze(specInfoListIn, totlCount,
                            startno, pgsize);
            Options<IoDpSpecInfo> infoList = new DefaultOptions<>();

            if (CommUtil.isNotNull(specList)) {
                counts = specList.getRecordCount();
                for (IoDpSpecInfo specInfo : specList.getRecords()) {
                    IoDpSpecInfo specDetl = SysUtil
                            .getInstance(IoDpSpecInfo.class);
                    CommUtil.copyProperties(specDetl, specInfo);
                    infoList.add(specDetl);
                }
            }

            CommTools.getBaseRunEnvs().setTotal_count(counts);
            output.setSpecInfoList(infoList);

        } else if (specInfoListIn.getSpectp() == E_SPECTP.DEDUCT) {// 特殊业务类型为
                                                                   // 2扣划

            Page<IoDpSpecInfo> specList = DpFrozQuery
                    .querySpecInfoListForDeduct(specInfoListIn, totlCount,
                            startno, pgsize);
            Options<IoDpSpecInfo> infoList = new DefaultOptions<>();
            if (CommUtil.isNotNull(specList)) {
                counts = specList.getRecordCount();
                for (IoDpSpecInfo specInfo : specList.getRecords()) {
                    IoDpSpecInfo specDetl = SysUtil
                            .getInstance(IoDpSpecInfo.class);
                    CommUtil.copyProperties(specDetl, specInfo);
                    infoList.add(specDetl);
                }
            }

            CommTools.getBaseRunEnvs().setTotal_count(counts);
            output.setSpecInfoList(infoList);

        } else if (specInfoListIn.getSpectp() == E_SPECTP.STOPPY) {// 特殊业务类型为
                                                                   // 3止付/解止

            Page<IoDpSpecInfo> specList = DpFrozQuery
                    .querySpecInfoListForStoppy(specInfoListIn, totlCount,
                            startno, pgsize);
            Options<IoDpSpecInfo> infoList = new DefaultOptions<>();
            if (CommUtil.isNotNull(specList)) {
                counts = specList.getRecordCount();
                for (IoDpSpecInfo specInfo : specList.getRecords()) {
                    IoDpSpecInfo specDetl = SysUtil
                            .getInstance(IoDpSpecInfo.class);
                    CommUtil.copyProperties(specDetl, specInfo);
                    infoList.add(specDetl);
                }
            }

            CommTools.getBaseRunEnvs().setTotal_count(counts);
            output.setSpecInfoList(infoList);

        } else if (specInfoListIn.getSpectp() == E_SPECTP.DPCTCT) {// 特殊业务类型为
                                                                   // 4存款证明

            Page<IoDpSpecInfo> specList = DpFrozQuery
                    .querySpecInfoListForDpctct(specInfoListIn, totlCount,
                            startno, pgsize);
            Options<IoDpSpecInfo> infoList = new DefaultOptions<>();

            if (CommUtil.isNotNull(specList)) {
                counts = specList.getRecordCount();
                for (IoDpSpecInfo specInfo : specList.getRecords()) {

                    IoDpSpecInfo specDetl = SysUtil
                            .getInstance(IoDpSpecInfo.class);
                    CommUtil.copyProperties(specDetl, specInfo);

                    if (specInfo.getDeprtp() == E_DEPRTP.TD) {
                        specDetl.setOtsptp(E_OTSPTP.TD);// 时期证明
                    }

                    if (specInfo.getFrozst() == E_FROZST.VALID) {
                        specDetl.setSpecst(E_SPECST.NOREMO);
                    }

                    if (specInfo.getFrozst() == E_FROZST.INVALID) {
                        specDetl.setSpecst(E_SPECST.OKREMO);
                    }
                    if (specInfo.getDeprtp() == E_DEPRTP.TP) {
                        specDetl.setOtsptp(E_OTSPTP.TP);// 时点证明
                    }

                    infoList.add(specDetl);
                }
            }

            CommTools.getBaseRunEnvs().setTotal_count(counts);
            output.setSpecInfoList(infoList);

        } else if (specInfoListIn.getSpectp() == E_SPECTP.CUSTOP) {// 特殊业务类型为5-账户保护/解除保护

            Page<IoDpSpecInfo> specList = DpFrozQuery
                    .querySpecInfoListForCustop(specInfoListIn, totlCount,
                            startno, pgsize);
            Options<IoDpSpecInfo> infoList = new DefaultOptions<>();

            if (CommUtil.isNotNull(specList)) {
                counts = specList.getRecordCount();

                for (IoDpSpecInfo specInfo : specList.getRecords()) {
                    IoDpSpecInfo specDetl = SysUtil.getInstance(IoDpSpecInfo.class);
                    CommUtil.copyProperties(specDetl, specInfo);
                    infoList.add(specDetl);
                }
            }

            CommTools.getBaseRunEnvs().setTotal_count(counts);
            output.setSpecInfoList(infoList);

        } else {
            throw DpModuleError.DpstComm.BNAS1251();
        }
        // else if (specInfoListIn.getSpectp() == E_SPECTP.ALL) {// 特殊业务类型为6全部
        //
        // Page<IoDpSpecInfo> specInfoList =
        // DpFrozQuery.querySpecInfoListForAll(specInfoListIn, totlCount,
        // startno,pgsize);
        // Options<IoDpSpecInfo> infoList = new DefaultOptions<>();
        //
        // if(CommUtil.isNotNull(specInfoList)){
        // counts = specInfoList.getRecordCount();
        //
        // for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
        //
        // IoDpSpecInfo specDetl = SysUtil.getInstance(IoDpSpecInfo.class);
        // CommUtil.copyProperties(specDetl, specInfo);
        //
        // if(specInfo.getDeprtp() == E_DEPRTP.TD){
        // specDetl.setOtsptp(E_OTSPTP.TD);//时期证明
        // }
        //
        // if(specInfo.getFrozst() == E_FROZST.VALID){
        // specDetl.setSpecst(E_SPECST.NOREMO);
        // }
        //
        // if(specInfo.getFrozst() == E_FROZST.INVALID){
        // specDetl.setSpecst(E_SPECST.OKREMO);
        // }
        //
        // if(specInfo.getDeprtp() == E_DEPRTP.TP){
        // specDetl.setOtsptp(E_OTSPTP.TP);//时点证明
        // }
        //
        // if(specInfo.getFroztp() == E_FROZTP.JUDICIAL || specInfo.getFroztp()
        // == E_FROZTP.ADD){
        // if(specInfo.getFrlmtp() == E_FRLMTP.AMOUNT){
        // specDetl.setOtsptp(E_OTSPTP.PCFROZ);//部冻
        // }
        // if(specInfo.getFrlmtp() == E_FRLMTP.OUT){
        // specDetl.setOtsptp(E_OTSPTP.CMFROZ);//借冻
        // }
        // if(specInfo.getFrlmtp() == E_FRLMTP.ALL){
        // specDetl.setOtsptp(E_OTSPTP.ALLFROZ);//双冻
        // }
        // }
        //
        // if(specInfo.getFrozst() == E_FROZST.INVALID){
        // specDetl.setOtsptp(E_OTSPTP.UNFROZ); //解冻
        // }
        //
        // if(specInfo.getFrozst() == E_FROZST.VALID){
        // if(specInfo.getFrlmtp() == E_FRLMTP.AMOUNT){
        // specDetl.setOtsptp(E_OTSPTP.PORSTO);//部止
        // }
        // if(specInfo.getFrlmtp() == E_FRLMTP.OUT){
        // specDetl.setOtsptp(E_OTSPTP.ALLSTOP);//全止
        // }
        // }
        //
        // if(specInfo.getFrozst() == E_FROZST.INVALID){
        // specDetl.setOtsptp(E_OTSPTP.UNSTOP);//解止
        // }
        //
        // infoList.add(specDetl);
        // }
        // }
        //
        // CommTools.getBaseRunEnvs().setTotal_count(ConvertUtil.toInteger(counts));
        // output.setSpecInfoList(infoList);
        // }
    }

    /**
     * 冻结查询
     * 
     * @return
     * 
     */
    @Override
    public cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoDpFrozQuery.Output IoDpFrozQuery(
            IoDpFrozQrIn cpliodpfrozqrin) {

        IoDpFrozQrOt cplIoDpFrozQrOt = SysUtil.getInstance(IoDpFrozQrOt.class);

        //IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
        // 查询前检查
        DpFrozQuery.queryCheck(cpliodpfrozqrin);

        // 即富更改，将卡号变更为子账户
        /*// 如果输入电子账号为空,卡号不为空，则按卡号查询电子账号并赋值
        if (CommUtil.isNull(cpliodpfrozqrin.getCustac())
                && CommUtil.isNotNull(cpliodpfrozqrin.getCardno())) {
            // cpliodpfrozqrin.setCustac(Kna_acdcDao.selectOne_odb2(cpliodpfrozqrin.getCardno(),
            // true).getCustac());
            cpliodpfrozqrin.setCustac(caqry.getKnaAcdcOdb2(
                    cpliodpfrozqrin.getCardno(), true).getCustac());
        }*/

        // 查询
        Options<IoDpFrozIf> lstFrozInfo = DpFrozQuery
                .queryFrozInfo(cpliodpfrozqrin);

        cplIoDpFrozQrOt.setLsfroz(lstFrozInfo);
       
        // 输出
        cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoDpFrozQuery.Output output = SysUtil
                .getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoDpFrozQuery.Output.class);
        output.setCpliodpfrozqrot(cplIoDpFrozQrOt);
        return output;
    }

    /**
     * 止付
     * 
     * @return
     * 
     */
    @Override
    public IoDpStopayOt IoStopay(IoDpStopayIn stopayin) {
        DpFrozEntity entity = new DpFrozEntity();      
        	  // 止付检查
            DpFrozProc.stopayCheck(stopayin);
            // 止付
            DpFrozProc.stopPayment(stopayin, entity);
        // 将输入作为返回值
        IoDpStopayOt cplIoStopayOt = SysUtil.getInstance(IoDpStopayOt.class);
        CommUtil.copyProperties(cplIoStopayOt, stopayin);
        
        String acalno=stopayin.getAcalno();
        String custna=stopayin.getCustna();
        String frna01=stopayin.getFrna01();
        String frna02=stopayin.getFrna02();
        String idno01=stopayin.getIdno01();
        String idno02=stopayin.getIdno02();
        if(CommUtil.isNotNull(acalno)) {
        	cplIoStopayOt.setTmtlphno(DecryptConstant.maskMobile(acalno));
        }
        if(CommUtil.isNotNull(custna)) {
        	cplIoStopayOt.setTmcustna(DecryptConstant.maskName(custna));
        }
        if(CommUtil.isNotNull(frna01)) {
        	cplIoStopayOt.setTmfrna01(DecryptConstant.maskName(frna01));
        }
        if(CommUtil.isNotNull(frna02)) {
        	cplIoStopayOt.setTmfrna01(DecryptConstant.maskName(frna02));
         }
        if(CommUtil.isNotNull(idno01)) {
        	cplIoStopayOt.setTmidno01(DecryptConstant.maskIdCard(idno01));
         }
        if(CommUtil.isNotNull(idno02)) {
        	cplIoStopayOt.setTmidno02(DecryptConstant.maskIdCard(idno02));
         }
       
        cplIoStopayOt.setFrozno(entity.getFrozno());

        // cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoStopay.Output
        // output = SysUtil
        // .getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoStopay.Output.class);
        // output.setStopayot(cplIoStopayOt);
        // 发送移动前端通知
        // E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE; // 消息媒介
        //
        // KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("DPUPGD", "DPFZSM",
        // "%", "%", true);
        //
        // String bdid = tblKnaPara.getParm_value1();// 服务绑定ID
        //
        // IoDpOtherService dpOtherService = SysUtil.getInstanceProxyByBind(
        // IoDpOtherService.class, bdid);
        //
        // // 账户保护通知移动前端
        // String mssdid = CommTools.getMySysId();// 消息ID
        // String mesdna = tblKnaPara.getParm_value2();// 媒介名称
        //
        // cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpOtherService.IoDpSendFrozMsg.InputSetter
        // openSendMsgInput = CommTools// .getInstance(IoDpOtherService.IoDpSendFrozMsg.InputSetter.class);
        //
        // openSendMsgInput.setMedium(mssdtp); // 消息媒介
        // openSendMsgInput.setMsgid(mssdid); // 发送消息ID
        // openSendMsgInput.setMdname(mesdna); // 媒介名称
        //
        // IoFrozMsgInfo info = SysUtil.getInstance(IoFrozMsgInfo.class);
        // IoCaQryMesgOut mesgOut = SysUtil.getInstance(IoCaQryMesgOut.class);
        // // 查询短信公共参数服务初始化
        // IoCaSrvGenEAccountInfo eAccountInfo =
        // SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
        //
        // IoCaKnaAcdc otacdc = ActoacDao.selKnaAcdc(stopayin.getCardno(),
        // false);
        // // 查询相关推送参数
        // mesgOut = eAccountInfo.selMesgInfo(otacdc.getCustac(),
        // CommTools.getBaseRunEnvs().getMain_trxn_seq(),
        // BusiTools.getBusiRunEnvs().getTrantm());
        // info.setBrchno("浙江农信");
        // info.setTellno(mesgOut.getLastnm());
        // openSendMsgInput.setFrzinf(info);
        // dpOtherService.sendFrozMsg(openSendMsgInput);
        /**
         * 推送app消息
         * yusheng
         */
        if (stopayin.getStoptp() == E_STOPTP.CUSTSTOPAY) {
//            MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//            mri.setMtopic("Q0101005");
            ToAppSendMsg AppSendMsgInput = SysUtil.getInstance(ToAppSendMsg.class);
            String custac="";
            //根据卡号查询电子账号
            IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
            IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(
                    stopayin.getCardno(), false);
            custac= caKnaAcdc.getCustac();
      
            //查询电子账户信息
            CaCustInfo.accoutinfos accoutinfos = SysUtil.getInstance(CaCustInfo.accoutinfos.class);
            accoutinfos = EacctMainDao.selCustInfobyCustac(custac,
                    E_ACALST.NORMAL, E_ACALTP.CELLPHONE, false);

            AppSendMsgInput.setUserId(accoutinfos.getCustid()); // 用户ID
            AppSendMsgInput.setOutNoticeId("Q0101005");//外部消息ID
            AppSendMsgInput.setNoticeTitle("电子账户保护");//公告标题
            /*String date  = CommTools.getBaseRunEnvs().getTrxn_date().substring(0,4)+"年"
            +CommTools.getBaseRunEnvs().getTrxn_date().substring(4,6)+"月"+ 
            		CommTools.getBaseRunEnvs().getTrxn_date().substring(6,8)+"日"+
            		BusiTools.getBusiRunEnvs().getTrantm().substring(0,2)+":"+
            		BusiTools.getBusiRunEnvs().getTrantm().substring(2,4)+":"+
            		BusiTools.getBusiRunEnvs().getTrantm().substring(4,6);*/

//            AppSendMsgInput.setContent("您的ThreeBank电子账户已被保护。如有疑问可咨询我行客服，电话0471-96616。"); //内容
//            AppSendMsgInput.setSendtime(CommTools.getBaseRunEnvs().getTrxn_date());//消息生成时间
//            AppSendMsgInput.setClickType(cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_CLIKTP.NO);
//            AppSendMsgInput.setClickValue("");//点击动作值
//            AppSendMsgInput.setTirggerSys(CommTools.getBaseRunEnvs().getSystcd());//触发系统
//            AppSendMsgInput.setTransType(cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_APPTTP.CUACCH);
//
//            mri.setMsgtyp("ApSmsType.ToAppSendMsg");
//            mri.setMsgobj(AppSendMsgInput);
//            AsyncMessageUtil.add(mri);
            CommTools.addMessagessToContext("Q0101005", "ApSmsType.PbinusSendMsg");
        }
        // 返回
        return cplIoStopayOt;
    }

    /**
     * 解止
     * 
     * @return
     * 
     */
    @Override
    public IoDpUnStopayOt IoStunpy(IoDpUnStopayIn stunpyin) {
    	 
        // 解止检查
        DpUnfrProc.stunpyCheck(stunpyin);

        // 解止
         DpUnfrProc.stunPayment(stunpyin);
        // 将输入作为返回值
        IoDpUnStopayOt stunpyot = SysUtil.getInstance(IoDpUnStopayOt.class);
        CommUtil.copyProperties(stunpyot, stunpyin);
        String acalno=stunpyin.getAcalno();
        String custna=stunpyin.getCustna();
        String frna01=stunpyin.getFrna01();
        String frna02=stunpyin.getFrna02();
        String idno01=stunpyin.getIdno01();
        String idno02=stunpyin.getIdno02();
        if(CommUtil.isNotNull(acalno)) {
        	  stunpyot.setTmtlphno(DecryptConstant.maskMobile(acalno));
        }
        if(CommUtil.isNotNull(custna)) {
    	   stunpyot.setTmcustna(DecryptConstant.maskName(custna));
        }
        if(CommUtil.isNotNull(frna01)) {
    	   stunpyot.setTmfrna01(DecryptConstant.maskName(frna01));
        }
        if(CommUtil.isNotNull(frna02)) {
    	   stunpyot.setTmfrna01(DecryptConstant.maskName(frna02));
           }
        if(CommUtil.isNotNull(idno01)) {
    	   stunpyot.setTmidno01(DecryptConstant.maskIdCard(idno01));
           }
        if(CommUtil.isNotNull(idno02)) {
    	   stunpyot.setTmidno02(DecryptConstant.maskIdCard(idno02));
           }
        
        stunpyot.setFrozno(stunpyin.getStopno());
        // 输出
        // cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoStunpy.Output
        // output = SysUtil
        // .getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoStunpy.Output.class);
        // output.setStunpyot(stunpyot);
        /**
         * 账户解除保护推送给app消息
         * yusheng
         */
        if (stunpyin.getStoptp() == E_STOPTP.CUSTSTOPAY) {
           // MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
            //mri.setMtopic("Q0101005");
            ToAppSendMsg AppSendMsgInput = SysUtil.getInstance(ToAppSendMsg.class);           
            //根据卡号查询电子账号
            IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
            IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(
                    stunpyin.getCardno(), false);
            String custac = caKnaAcdc.getCustac();
            //查询电子账户信息
            CaCustInfo.accoutinfos accoutinfos = SysUtil.getInstance(CaCustInfo.accoutinfos.class);
            accoutinfos = EacctMainDao.selCustInfobyCustac(custac,
                    E_ACALST.NORMAL, E_ACALTP.CELLPHONE, false);

            AppSendMsgInput.setUserId(accoutinfos.getCustid()); // 用户ID
            AppSendMsgInput.setOutNoticeId("Q0101005");//外部消息ID
            AppSendMsgInput.setNoticeTitle("电子账户解除保护");//公告标题
            /*String date  = CommTools.getBaseRunEnvs().getTrxn_date().substring(0,4)+"年"
            +CommTools.getBaseRunEnvs().getTrxn_date().substring(4,6)+"月"+ 
            		CommTools.getBaseRunEnvs().getTrxn_date().substring(6,8)+"日"+
            		BusiTools.getBusiRunEnvs().getTrantm().substring(0,2)+":"+
            		BusiTools.getBusiRunEnvs().getTrantm().substring(2,4)+":"+
            		BusiTools.getBusiRunEnvs().getTrantm().substring(4,6);*/

//            AppSendMsgInput.setContent("您的ThreeBank电子账户已成功解除保护。感谢您对金谷农商银行的支持！"); //内容
//            AppSendMsgInput.setSendtime(CommTools.getBaseRunEnvs().getTrxn_date());//消息生成时间
//            AppSendMsgInput.setClickType(cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_CLIKTP.NO);
//            AppSendMsgInput.setClickValue("");//点击动作值
//            AppSendMsgInput.setTirggerSys(CommTools.getBaseRunEnvs().getSystcd());//触发系统
//            AppSendMsgInput.setTransType(cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_APPTTP.CUACCH);
//
//            mri.setMsgtyp("ApSmsType.ToAppSendMsg");
//            mri.setMsgobj(AppSendMsgInput);
//            AsyncMessageUtil.add(mri);
            CommTools.addMessagessToContext("Q0101005", "ApSmsType.PbinusSendMsg");
        }
        return stunpyot;

    }

    /**
     * 开立存款证明
     * 
     * @return
     * 
     */
    @Override
    public IoDpprovOut IoOpDpprov(IoDpprovIn opDpprIn) {
        // 存款证明检查
        DpFrozProc.dpprovCheck(opDpprIn);

        DpOpprEntity entity = new DpOpprEntity();

        IoDpprovOut dpprout = SysUtil.getInstance(IoDpprovOut.class);

        DpFrozProc.dpprovProc(opDpprIn, entity);

        // 调用收费方法
        if (E_DPCGFG.Y == opDpprIn.getIschge()) {
            DpFrozProc.freeOfDeposit(opDpprIn, entity);
        }

        CommUtil.copyProperties(dpprout, opDpprIn);

        dpprout.setCustna(entity.getCustna()); // 户名
        dpprout.setCrcycd(entity.getCrcycd()); // 币种
        dpprout.setCsextg(entity.getCsextg()); // 钞汇属性
        dpprout.setTranbl(entity.getAmount()); // 可用余额
        dpprout.setDeprnm(entity.getDeprnm()); // 存款证明书编号

        return dpprout;
    }

    /**
     * 撤销存款证明
     * 
     * @return
     * 
     */
    @Override
    public IoDpprovOut IoCaDpprov(IoDpprovIn caDpprIn) {
        // 存款证明输入检查
        DpFrozProc.dpprovCheck(caDpprIn);

        // 撤销存款证明检查
        DpFrozProc.caDpprovCheck(caDpprIn);

        // 撤销存款证明处理
        DpOpprEntity entity = new DpOpprEntity();

        IoDpprovOut dpprout = SysUtil.getInstance(IoDpprovOut.class);
        DpFrozProc.dpprovProc(caDpprIn, entity);

        CommUtil.copyProperties(dpprout, caDpprIn);
        dpprout.setCustna(entity.getCustna()); // 户名
        dpprout.setCrcycd(entity.getCrcycd()); // 币种
        dpprout.setCsextg(entity.getCsextg()); // 钞汇属性
        dpprout.setTranbl(entity.getAmount()); // 可用余额

        IoDpprovOut output = SysUtil.getInstance(IoDpprovOut.class);
        CommUtil.copyProperties(output, caDpprIn);

        return output;
    }

    /**
     * 补打存款证明
     * 
     */
    @Override
    public cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoReDpprov.Output IoReDpprov(
            IoDpprovIn reDpprIn) {
        // 存款证明输入检查
        DpFrozProc.dpprovCheck(reDpprIn);

        // 补打存款证明检查
        DpFrozProc.reDpprovCheck(reDpprIn);

        // 补打存款证明处理
        DpOpprEntity entity = new DpOpprEntity();

        IoDpprovOut dpprout = SysUtil.getInstance(IoDpprovOut.class);

        DpFrozProc.dpprovProc(reDpprIn, entity);

        // 调用收费方法
        if (E_DPCGFG.Y == reDpprIn.getIschge()) {
            DpFrozProc.freeOfDeposit(reDpprIn, entity);
        }

        // 设置输出
        CommUtil.copyProperties(dpprout, reDpprIn);

        dpprout.setCustna(entity.getCustna()); // 户名
        dpprout.setCrcycd(entity.getCrcycd()); // 币种
        dpprout.setCsextg(entity.getCsextg()); // 钞汇属性
        dpprout.setTranbl(entity.getAmount()); // 可用余额

        IoReDpprov.Output output = SysUtil.getInstance(IoReDpprov.Output.class);

        output.setReDpprOut(dpprout);

        return output;
    }

    @Override
    public void IoDpUnfrReversal(String mntrsq) {
        DpUnfrReversal.deductPrc(mntrsq);
    }

    /**
     * @author douwenbo
     * @date 2016-05-03 19:44 查询冻解冻/解止付信息
     */
    @Override
    public void queryDpSpecForFroz(
            DpSpecInfoListForFrozIn specInfoListForFrozIn,
            IoQueryDpSpecForFroz.Output output) {

        // 查询前的基本检查
        DpFrozQuery.queryDpSpecForFrozCheck(specInfoListForFrozIn);

        // 查询冻结登记簿、冻结明细登记簿、解冻登记簿信息
        DpFrozQuery.queryDpSpecForFrozInfo(specInfoListForFrozIn, output);

    }

    /**
     * @author douwenbo
     * @date 2016-05-04 16:43 查询扣划信息
     */
    @Override
    public void queryDpSpecForDedu(
            DpSpecInfoListForDeduIn specInfoListForDeduIn,
            IoQueryDpSpecForDedu.Output output) {

        // 查询前的基本检查
        DpFrozQuery.queryDpSpecForDeduCheck(specInfoListForDeduIn);

        // 查询扣划登记簿信息
        DpFrozQuery.queryDpSpecForDeduInfo(specInfoListForDeduIn, output);

    }

    /**
     * @author douwenbo
     * @date 2016-05-05 16:16 查询存款证明信息
     */
    @Override
    public void queryDpSpecForDepr(
            DpSpecInfoListForDeprIn specInfoListForDpurIn,
            IoQueryDpSpecForDepr.Output output) {

        // 查询前的基本检查
        DpFrozQuery.queryDpSpecForDeprCheck(specInfoListForDpurIn);

        // 查询存款证明主体登记簿信息、存款证明登记簿信息
        DpFrozQuery.queryDpSpecForDeprInfo(specInfoListForDpurIn, output);

    }

    /**
     * 
     * @author xiongzhao
     *         <p>
     *         <li>2016年6月27日-下午4:10:08</li>
     *         <li>功能描述：查询电子账户不可用余额</li>
     *         </p>
     * 
     * @param custac
     *        电子账号ID
     * @return
     */
    public void qryUnBalance(
            String custac,
            cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoDpQryUnBalance.Output output) {

        String sFrozno = "";// 冻结编号
        Long lFrozsq = null;// 冻结序号
        String sCardno = "";// 电子账号
        BigDecimal bigAcctbl = BigDecimal.ZERO;// 可支取余额
        BigDecimal bigUnavbl = BigDecimal.ZERO;// 不可用余额

        // 检查输入接口必输项是否为空
        if (CommUtil.isNull(custac)) {
            throw DpModuleError.DpstComm.BNAS0936();
        }

        // 调用Ca模块通过电子账号ID查询出电子账号
        IoCaSevQryTableInfo ioCaSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);

        IoCaKnaAcdc ioCaKnaAcdc = ioCaSevQryTableInfo.getKnaAcdcOdb1(custac,
                E_DPACST.NORMAL, false);

        if (CommUtil.isNull(ioCaKnaAcdc)) {
            throw DpModuleError.DpstComm.BNAS1264();
        }

        // CommTools.getBaseRunEnvs().setBusi_org_id(ioCaKnaAcdc.getCorpno());

        sCardno = ioCaKnaAcdc.getCardno();

        // 查询当前余额
        KnaAcct tblKnaAcct = CapitalTransDeal.getSettKnaAcctAc(custac);

        String eCrcycd = tblKnaAcct.getCrcycd();// 币种
        BigDecimal bigOnlnbl = tblKnaAcct.getOnlnbl();// 当前余额

        // 可用余额
        bigAcctbl = SysUtil.getInstance(DpAcctSvcType.class).getAcctaAvaBal(
                custac, tblKnaAcct.getAcctno(), eCrcycd, E_YES___.YES,
                E_YES___.NO);

        // 查询不可用余额
        KnbFrozOwne tblKnbFrozOwne = KnbFrozOwneDao.selectOne_odb1(
                E_FROZOW.AUACCT, custac, false);
        if (CommUtil.isNotNull(tblKnbFrozOwne)) {
            bigUnavbl = tblKnbFrozOwne.getFrozbl();// 不可用余额
        }

        // 定义LIST集合
        List<IoDpQryUnBalList> ioDpUnBalList1 = new DefaultOptions<>();

        // 根据电子账号ID查询冻结信息明细表
        List<IoDpKnbFroz> listKnbFroz = DpFrozDao.selFrozByCustac(custac,
                E_FROZST.VALID, false);

        for (IoDpKnbFroz Froz : listKnbFroz) {
            sFrozno = Froz.getFrozno();// 冻结编号
            lFrozsq = Froz.getFrozsq();// 冻结序号
            IoDpQryUnBalList ioDpUnBalList = SysUtil.getInstance(IoDpQryUnBalList.class);
            KnbFrozDetl tblKnbFrozDetl = KnbFrozDetlDao.selectOne_odb2(sFrozno,
                    lFrozsq, false);
            if (CommUtil.isNotNull(tblKnbFrozDetl)) {
                ioDpUnBalList.setFrozam(tblKnbFrozDetl.getFrozbl());// 冻结金额
                ioDpUnBalList.setFrozdt(Froz.getFrozdt());// 冻结日期
                ioDpUnBalList.setFroztm(Froz.getFroztm());// 冻结时间
                ioDpUnBalList.setFroztp(Froz.getFroztp());// 冻结类型
                // TODO 备注返回
                if (Froz.getFroztp() == E_FROZTP.ADD) {
                    ioDpUnBalList.setRemark(Froz.getFrogna() + "冻结"); // 备注
                } else if (Froz.getFroztp() == E_FROZTP.BANKSTOPAY) {
                    ioDpUnBalList.setRemark(Froz.getFrogna() + "冻结");
                } else if (Froz.getFroztp() == E_FROZTP.DEPRSTOPAY) {
                    ioDpUnBalList.setRemark("存款证明止付");
                } else if (Froz.getFroztp() == E_FROZTP.EXTSTOPAY) {
                    ioDpUnBalList.setRemark(Froz.getFrogna() + "止付");
                } else if (Froz.getFroztp() == E_FROZTP.FNFROZ) {
                    ioDpUnBalList.setRemark("购买理财冻结");
                } else if (Froz.getFroztp() == E_FROZTP.JUDICIAL) {
                    ioDpUnBalList.setRemark(Froz.getFrogna() + "冻结");
                }
            }
            // 将编辑过的结果放到集合中
            ioDpUnBalList1.add(ioDpUnBalList);
        }
        // 输出映射
        output.setAcctbl(bigAcctbl);// 可支取余额
        output.setCardno(sCardno);// 卡号
        output.setOnlnbl(bigOnlnbl);// 当前余额
        output.setUnavbl(bigUnavbl);// 不可用余额
        output.getUnavaiInfoList().addAll(ioDpUnBalList1);// 不可用余额信息列表
        CommTools.getBaseRunEnvs().setTotal_count(Long.valueOf(ioDpUnBalList1.size()));// 总笔数
    }

    /**
     * 
     * @author xiongzhao
     *         <p>
     *         <li>2016年6月28日-下午2:36:37</li>
     *         <li>功能描述：冻结止付信息详情查询</li>
     *         </p>
     * 
     * @param custac
     *        电子账号ID
     * @param qrfrtp
     *        查询冻结类型
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public void qryFrozInfoList(
            Input input,
            cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoDpQryFrozInfos.Output output) {

        int counts = 0;// 总笔数
        String sCustac = input.getCustac();// 电子账号
        E_QRFRTP eQrfrtp = input.getQrfrtp();// 查询冻结类型
        int pageno = input.getPageno();// 页数
        int pgsize = input.getPgsize();// 页容量

        // 检查输入必输项是否为空
        if (CommUtil.isNull(sCustac)) {
            throw DpModuleError.DpstComm.BNAS0904();
        }
        if (CommUtil.isNull(eQrfrtp)) {
            throw DpModuleError.DpstComm.BNAS1075();
        }
        if (CommUtil.isNull(pageno)) {
            throw CaError.Eacct.BNAS0249();
        }
        if (CommUtil.isNull(pgsize)) {
            throw DpModuleError.DpstComm.BNAS0252();
        }
        //modify yusheng 增加电子账号不存在的判断
        KnaAcdc knaAcdc = KnaAcdcDao.selectFirst_odb3(sCustac, false);
        if (CommUtil.isNull(knaAcdc)) {
            throw DpModuleError.DpstComm.BNAS0936();
        }

        int starno = (input.getPageno() - 1) * input.getPgsize(); // 起始数

        Options<IoDpQryFrozInfoList> ioDpQryFrozInfoList1 = SysUtil.getInstance(Options.class);

        // 查询冻结类型为司法冻结
        if (eQrfrtp == E_QRFRTP.JUDCLFROZ) {
            Page<IoDpQryFrozTableInfo> ioDpQryFrozTableInfo = DpFrozDao
                    .selFrozInfosByFrozo(sCustac, starno, pgsize, counts, false);
            for (IoDpQryFrozTableInfo ioFrozInfo : ioDpQryFrozTableInfo
                    .getRecords()) {
                IoDpQryFrozInfoList ioDpQryFrozInfoList = SysUtil
                        .getInstance(IoDpQryFrozInfoList.class);
                ioDpQryFrozInfoList.setCrcycd(ioFrozInfo.getCrcycd());
                ioDpQryFrozInfoList.setFrexog(ioFrozInfo.getFrexog());
                ioDpQryFrozInfoList.setFrogna(ioFrozInfo.getFrogna());
                ioDpQryFrozInfoList.setFrozam(ioFrozInfo.getFrozbl());
                ioDpQryFrozInfoList.setFrozdt(ioFrozInfo.getFrozdt());
                ioDpQryFrozInfoList.setFrozno(ioFrozInfo.getFrozno());
                ioDpQryFrozInfoList.setFroztm(ioFrozInfo.getFroztm());
                if (ioFrozInfo.getFrlmtp() == E_FRLMTP.ALL) {
                    ioDpQryFrozInfoList.setFrsptp(E_FRSPTP.DOUBLE);
                } else if (ioFrozInfo.getFrlmtp() == E_FRLMTP.AMOUNT) {
                    ioDpQryFrozInfoList.setFrsptp(E_FRSPTP.PARTFROZ);
                } else if (ioFrozInfo.getFrlmtp() == E_FRLMTP.OUT) {
                    ioDpQryFrozInfoList.setFrsptp(E_FRSPTP.BORROW);
                }
                ioDpQryFrozInfoList.setRemark(ioFrozInfo.getRemark());
                ioDpQryFrozInfoList1.add(ioDpQryFrozInfoList);
            }
            // 设置报文头总记录条数
            CommTools.getBaseRunEnvs().setTotal_count(
                    ioDpQryFrozTableInfo.getRecordCount());
        }
        // 查询冻结类型为止付
        else if (eQrfrtp == E_QRFRTP.STOPPAY) {
            Page<IoDpQryFrozTableInfo> ioDpQryFrozTableInfo = DpFrozDao
                    .selFrozInfosByFrozt(sCustac, E_FROZST.VALID, starno,
                            pgsize, counts, false);

            for (IoDpQryFrozTableInfo ioFrozInfo : ioDpQryFrozTableInfo
                    .getRecords()) {
                IoDpQryFrozInfoList ioDpQryFrozInfoList = SysUtil
                        .getInstance(IoDpQryFrozInfoList.class);
                ioDpQryFrozInfoList.setCrcycd(ioFrozInfo.getCrcycd());
                ioDpQryFrozInfoList.setFrexog(ioFrozInfo.getFrexog());
                ioDpQryFrozInfoList.setFrogna(ioFrozInfo.getFrogna());
                ioDpQryFrozInfoList.setFrozam(ioFrozInfo.getFrozbl());
                ioDpQryFrozInfoList.setFrozdt(ioFrozInfo.getFrozdt());
                ioDpQryFrozInfoList.setFrozno(ioFrozInfo.getFrozno());
                ioDpQryFrozInfoList.setFroztm(ioFrozInfo.getFroztm());
                if (ioFrozInfo.getFrlmtp() == E_FRLMTP.OUT) {
                    ioDpQryFrozInfoList.setFrsptp(E_FRSPTP.ALLSTPAY);
                } else if (ioFrozInfo.getFrlmtp() == E_FRLMTP.AMOUNT) {
                    ioDpQryFrozInfoList.setFrsptp(E_FRSPTP.PARTSTPAY);
                }
                ioDpQryFrozInfoList.setRemark(ioFrozInfo.getRemark());
                ioDpQryFrozInfoList1.add(ioDpQryFrozInfoList);
            }
            // 设置报文头总记录条数
            CommTools.getBaseRunEnvs().setTotal_count(
                    ioDpQryFrozTableInfo.getRecordCount());
        }
        // 查询冻结类型为系统冻结（理财基金冻结）
        else if (eQrfrtp == E_QRFRTP.SYSFROZ) {
            Page<IoDpQryFrozTableInfo> ioDpQryFrozTableInfo = DpFrozDao
                    .selFrozInfosByFrozh(sCustac, E_FROZST.VALID, starno,
                            pgsize, counts, false);

            for (IoDpQryFrozTableInfo ioFrozInfo : ioDpQryFrozTableInfo
                    .getRecords()) {
                IoDpQryFrozInfoList ioDpQryFrozInfoList = SysUtil
                        .getInstance(IoDpQryFrozInfoList.class);
                ioDpQryFrozInfoList.setCrcycd(ioFrozInfo.getCrcycd());
                ioDpQryFrozInfoList.setFrexog(ioFrozInfo.getFrexog());
                ioDpQryFrozInfoList.setFrogna(ioFrozInfo.getFrogna());
                ioDpQryFrozInfoList.setFrozam(ioFrozInfo.getFrozbl());
                ioDpQryFrozInfoList.setFrozdt(ioFrozInfo.getFrozdt());
                ioDpQryFrozInfoList.setFrozno(ioFrozInfo.getFrozno());
                ioDpQryFrozInfoList.setFroztm(ioFrozInfo.getFroztm());
                ioDpQryFrozInfoList.setRemark(ioFrozInfo.getRemark());
                ioDpQryFrozInfoList1.add(ioDpQryFrozInfoList);
            }
            // 设置报文头总记录条数
            CommTools.getBaseRunEnvs().setTotal_count(
                    ioDpQryFrozTableInfo.getRecordCount());
        }
        // TODO 查询冻结类型为预授权
        else if (eQrfrtp == E_QRFRTP.PREAUZATION) {
            throw DpModuleError.DpstComm.BNAS0202();
        }
        output.getFrozInfoList().addAll(ioDpQryFrozInfoList1);
    }

    /**
     * 
     * @author xiongzhao
     *         <p>
     *         <li>2016年6月30日-上午9:10:12</li>
     *         <li>功能描述：获取电子账户状态字</li>
     *         </p>
     * 
     * @param custac
     * @return
     */
    public IoDpAcStatusWord getAcStatusWord(String custac) {
        // 检查输入项电子账户是否为空
        if (CommUtil.isNull(custac)) {
            throw DpModuleError.DpstProd.BNAS0935();
        }
        // 调用获取电子账户状态字方法
        IoDpAcStatusWord cplGetAcStWord = DpAcctStatus.GetAcStatus(custac);

        return cplGetAcStWord;
    }

    /**
     * 
     * @author huangzhikai
     *         <p>
     *         <li>2016年8月19日-上午9:10:12</li>
     *         <li>功能描述：获取账户冻结信息</li>
     *         </p>
     * 
     * @param custac
     * @return
     */
    @Override
    public IoFrozInfoOut IoDpQryFrozInfo(String mntrsq, String frozdt,
            Boolean isable) {

        IoFrozInfoOut frozout = SysUtil.getInstance(IoFrozInfoOut.class);

        KnbFroz tblKnbFroz = KnbFrozDao.selectOne_odb13(frozdt, mntrsq, false);
        KnbFrozDetl tblKnbFrozDetl = KnbFrozDetlDao.selectOne_odb2(
                tblKnbFroz.getFrozno(), tblKnbFroz.getFrozsq(), false);

        if (CommUtil.isNotNull(tblKnbFroz)) {
            frozout.setCustac(tblKnbFroz.getCustac());
            frozout.setFrozno(tblKnbFroz.getFrozno());
            frozout.setFrozst(tblKnbFroz.getFrozst());
            frozout.setFroztp(tblKnbFroz.getFroztp());
        }

        if (CommUtil.isNotNull(tblKnbFrozDetl)) {
            frozout.setFrozam(tblKnbFrozDetl.getFrozam());
        }

        return frozout;
    }

    /**
     * 
     * @Title: qryKnbFroz
     * @Description: (根据电子账号,冻结状态查询冻结登记簿)
     * @param custac
     * @param frozst
     * @return
     * @author xiongzhao
     * @date 2016年9月27日 上午10:01:46
     * @version V2.3.0
     */
    @Override
    public Options<IoDpKnbFroz> qryKnbFroz(String custac, E_FROZST frozst) {

        @SuppressWarnings("unchecked")
        Options<IoDpKnbFroz> cplKnbFroz = SysUtil.getInstance(Options.class);
        List<IoDpKnbFroz> cplKnbFroz1 = DpFrozDao.selFrozamByCustac(custac,
                frozst, false);
        if (CommUtil.isNotNull(cplKnbFroz1) && cplKnbFroz1.size() > 0) {
            cplKnbFroz.addAll(cplKnbFroz1);
        }
        return cplKnbFroz;
    }

    /**
     * 
     * @author huangzhikai
     *         <p>
     *         <li>2016年9月27日-上午10:10:12</li>
     *         <li>功能描述：根据冻结编号查询冻结信息</li>
     *         </p>
     * 
     * @param custac
     * @return
     */
    @Override
    public void IoDpQryFroz(
            cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoDpQryFroz.Input input,
            cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoDpQryFroz.Output output) {

        if (CommUtil.isNull(input.getFrozno())) {
            throw DpModuleError.DpstComm.BNAS0834();
        }

        IofrozInfo frozInfo = SysUtil.getInstance(IofrozInfo.class);
        // 判断是止付还是冻结 addby yusheng 增加对冻结序号的长度判断，以免在后续截取字符串时报错（内管提示不友好）
        if (CommUtil.isNotNull(input.getFrozno()) && input.getFrozno().length() < 2) {
            throw DpModuleError.DpstComm.BNAS1703();
        }
        if (CommUtil.equals("21", input.getFrozno().substring(0, 2))) {
            List<KnbFroz> frozList = KnbFrozDao.selectAll_odb4(
                    input.getFrozno(), false);
            if (CommUtil.isNull(frozList)) {
                throw DpModuleError.DpstComm.BNAS1703();
            }

            List<KnbFroz> frozInfoList = KnbFrozDao.selectAll_odb5(
                    input.getFrozno(), E_FROZST.VALID, false);
            if (CommUtil.isNull(frozInfoList)) {
                throw DpModuleError.DpstComm.BNAS0733();
            }
            E_YES___ isflag = null;

            // 冻结条数大于1条，则存在续冻，设置续冻标识
            if (frozInfoList.size() > 1) {
                isflag = E_YES___.YES;
            } else {
                isflag = E_YES___.NO;
            }
            // 查询当前生效的冻结信息
            frozInfo = DpAcctQryDao
                    .selFrozInfoByFrozno(input.getFrozno(), true);
            // 查询当前生效的冻结明细信息
            KnbFrozDetl frozDetl = KnbFrozDetlDao.selectOne_odb2(
                    input.getFrozno(), frozInfo.getFrozsq(), true);
            //根据冻结主体不同，对冻结主体赋值    by zhx 20180110
            String custac;
            if (frozDetl.getFrozow() == E_FROZOW.ACCTNO) {
                KnaFxac knaFxac = KnaFxacDao.selectOne_odb1(frozDetl.getFrowid(), true);
                custac = knaFxac.getCustac();
            }
            else {
                custac = frozDetl.getFrowid();
            }

            // 查询电子账户信息
            String cardno = DpFrozDao
                    .selKnaAcdcInfo(custac, true);
            frozInfo.setCardno(cardno);
            frozInfo.setTranam(frozDetl.getFrozbl());
            frozInfo.setIsflag(isflag);

            // 止付
        } else if (CommUtil.equals("22", input.getFrozno().substring(0, 2))) {

            frozInfo = DpFrozDao.selStopInfoByFrozno(input.getFrozno(), true);
            KnbFrozDetl frozDetl = KnbFrozDetlDao.selectOne_odb2(
                    input.getFrozno(), frozInfo.getFrozsq(), true);
            // 查询电子账户信息
            String cardno = DpFrozDao
                    .selKnaAcdcInfo(frozDetl.getFrowid(), true);
            frozInfo.setCardno(cardno);
        } else {
            throw DpModuleError.DpstComm.BNAS0712();
        }

        output.setFrozInfo(frozInfo);
    }

    /**
     * 
     * @author huangzhikai
     *         <p>
     *         <li>2016年11月23日-上午10:10:12</li>
     *         <li>功能描述：获取账户可用余额</li>
     *         </p>
     * 
     * @param custac
     * @return
     */
    @Override
    public BigDecimal getAcctBal(String custac, String crcycd) {
        BigDecimal bal = BigDecimal.ZERO;
        // 查询活期子账户信息
        List<KnaAcct> acctList = KnaAcctDao.selectAll_odb6(custac, false);
        // 获取活期子账户所有余额
        if (CommUtil.isNotNull(acctList)) {
            for (KnaAcct acct : acctList) {
                bal = bal.add(DpAcctProc.getAcctBalance(acct));
            }
        }

        // 查询定期子账户信息
        List<KnaFxac> fxacList = KnaFxacDao.selectAll_odb5(custac, false);
        // 获取活期子账户所有余额
        if (CommUtil.isNotNull(fxacList)) {
            for (KnaFxac fxac : fxacList) {
                bal = bal.add(fxac.getOnlnbl());
                // 减掉子账户冻结余额     by  zhx
                bal = bal.subtract(DpFrozTools.getFrozBala(E_FROZOW.ACCTNO, fxac.getAcctno()));
            }
        }

        // 减掉智能储蓄冻结余额
        bal = bal.subtract(DpFrozTools.getFrozBala(E_FROZOW.AUACCT, custac));

        // 可用余额如果小于0，则返回0
        if (CommUtil.compare(bal, BigDecimal.ZERO) < 0)
            bal = BigDecimal.ZERO;

        log.parm("账户余额", bal);

        return bal;
    }

    /**
     * 
     * @author huangzhikai
     *         <p>
     *         <li>2016年11月30日-下午14:08:12</li>
     *         <li>功能描述：查询账号保护信息</li>
     *         </p>
     * 
     * @param custac
     * @return
     */
    @Override
    public IoDpCustopInfo qryCustInfo(String cardno) {

        if (CommUtil.isNull(cardno)) {
            throw DpModuleError.DpstAcct.BNAS0311();
        }

        IoCustacInfo tblKnaAcdc = DpFrozDao
                .selCustacInfoByCardno(cardno, false);

        if (CommUtil.isNull(tblKnaAcdc)) {
            throw CaError.Eacct.BNAS1279();
        }

        // 原交易环境变量的法人
        //		String oldCoprno = CommTools.getBaseRunEnvs().getBusi_org_id();
        // 开户行法人
        // CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaAcdc.getCorpno());

        // 根据电子账号查询电子账号信息
        IoCustacDetl custacDl = DpFrozDao.selCustacDetl(tblKnaAcdc.getCustac(),
                false);
        if (CommUtil.isNull(custacDl)) {
            throw CaError.Eacct.BNAS0873();
        }

        // 查询电子账户分类
        E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
                .qryAccatpByCustac(tblKnaAcdc.getCustac());
        KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);

        if (eAccatp == E_ACCATP.GLOBAL || eAccatp == E_ACCATP.FINANCE) {

            tblKnaAcct = KnaAcctDao.selectFirst_odb9(E_ACSETP.SA,
                    tblKnaAcdc.getCustac(), true);

        } else if (eAccatp == E_ACCATP.WALLET) {

            tblKnaAcct = KnaAcctDao.selectFirst_odb9(E_ACSETP.MA,
                    tblKnaAcdc.getCustac(), true);
        }

        String crcycd = tblKnaAcct.getCrcycd();// 币种
        E_CSEXTG csextg = tblKnaAcct.getCsextg();// 钞汇标识
        E_CUREAS cureas = null;// 账户保护原因

        if (CommUtil.isNotNull(crcycd)
                && BusiTools.getDefineCurrency().equals(crcycd)) {
            csextg = null;
        }

        // 获取电子账号状态
        E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
                .selCaStInfo(tblKnaAcdc.getCustac());

        // 查询账户保护原因
        KnbFroz tbl_KnbFroz = DpFrozDao.selStopInfoByCustac(
                tblKnaAcdc.getCustac(), false);
        if (CommUtil.isNull(tbl_KnbFroz)) {
            cureas = null;
        } else {
            cureas = CommUtil.toEnum(E_CUREAS.class, tbl_KnbFroz.getFrreas());
        }

        IoDpCustopInfo custopInfo = SysUtil.getInstance(IoDpCustopInfo.class);
        custopInfo.setCustna(custacDl.getCustna());
        custopInfo.setCrcycd(crcycd);
        custopInfo.setCsextg(csextg);
        custopInfo.setCureas(cureas);
        custopInfo.setCuacst(cuacst);

        //		CommTools.getBaseRunEnvs().setBusi_org_id(oldCoprno);

        return custopInfo;
    }

    /**
     * 
     * @author huangzhikai
     *         <p>
     *         <li>2017年1月3日-下午14:08:12</li>
     *         <li>功能描述：扣划状态字冲正</li>
     *         </p>
     * 
     * @param custac
     * @return
     */
    @Override
    public void deduStrike(String mntrsq, String trandt, String frozno) {

        // 若为有冻结扣划
        if (CommUtil.isNotNull(frozno)) {
            List<KnbUnfr> info = DpFrozDao.selUnfrInfoByMntrsq(mntrsq, false); // KnbUnfrDao.selectAll_odb1(mntrsq,
                                                                               // false);

            // 有解冻记录扣划特殊业务状态冲正
            if (CommUtil.isNotNull(info)) {
                for (KnbUnfr tblKnbUnfr : info) {

                    // 更新解冻记录为已冲正
                    tblKnbUnfr.setIsflag(E_YES___.YES);
                    KnbUnfrDao.updateOne_odb2(tblKnbUnfr);

                    // 查询冻结记录
                    KnbFroz tblKnbFroz = KnbFrozDao.selectOne_odb8(frozno,
                            tblKnbUnfr.getOdfrsq(), false);

                    if (CommUtil.isNull(tblKnbFroz)) {
                        throw DpModuleError.DpstComm.BNAS1704();
                    }

                    // 无金额冻结
                    if (CommUtil
                            .equals(tblKnbUnfr.getUnfram(), BigDecimal.ZERO)) {
                        // 更新冻结登记簿状态
                        tblKnbFroz.setFrozst(E_FROZST.VALID);
                        tblKnbFroz.setUnfrdt(null);
                        KnbFrozDao.updateOne_odb8(tblKnbFroz);

                        // 更新冻结明细登记簿
                        KnbFrozDetl tblKnbFrozDetl = KnbFrozDetlDao
                                .selectOne_odb2(frozno, tblKnbFroz.getFrozsq(),
                                        false);
                        if (CommUtil.isNull(tblKnbFrozDetl)) {
                            throw DpModuleError.DpstComm.BNAS1705();
                        }

                        // 更新冻结明细登记簿状态
                        tblKnbFrozDetl.setFrozst(E_FROZST.VALID);
                        KnbFrozDetlDao.updateOne_odb2(tblKnbFrozDetl);

                        // 更新冻结主体登记簿标识
                        KnbFrozOwne tblKnbFrozOwne = KnbFrozOwneDao
                                .selectOne_odb1(E_FROZOW.AUACCT,
                                        tblKnbFroz.getCustac(), false);

                        if (tblKnbFroz.getFrlmtp() == E_FRLMTP.ALL) {
                            tblKnbFrozOwne.setFralfg(E_YES___.YES);

                        } else if (tblKnbFroz.getFrlmtp() == E_FRLMTP.OUT) {
                            tblKnbFrozOwne.setFrotfg(E_YES___.YES);
                        }

                        KnbFrozOwneDao.updateOne_odb1(tblKnbFrozOwne);

                        // 有金额冻结
                    } else {
                        // 更新冻结登记簿状态
                        tblKnbFroz.setFrozst(E_FROZST.VALID);
                        tblKnbFroz.setUnfrdt(null);
                        KnbFrozDao.updateOne_odb8(tblKnbFroz);

                        // 更新冻结明细登记簿
                        KnbFrozDetl tblKnbFrozDetl = KnbFrozDetlDao
                                .selectOneWithLock_odb2(frozno,
                                        tblKnbFroz.getFrozsq(), false);
                        if (CommUtil.isNull(tblKnbFrozDetl)) {
                            throw DpModuleError.DpstComm.BNAS1705();
                        }

                        // 生效冻结
                        if (CommUtil.compare(tblKnbFroz.getFrozsq(), info
                                .get(0).getOdfrsq()) == 0) {
                            // 累加当前冻结余额
                            tblKnbFrozDetl.setFrozbl(tblKnbUnfr.getUnfram()
                                    .add(tblKnbFrozDetl.getFrozbl()));

                            // 未生效的续冻
                        } else if (info.size() > 1
                                && (CommUtil.compare(tblKnbFroz.getFrozsq(),
                                        info.get(1).getOdfrsq()) == 0)) {

                            if (CommUtil.compare(
                                    tblKnbUnfr.getUnfram().add(
                                            tblKnbFrozDetl.getFrozbl()),
                                    tblKnbFrozDetl.getFrozam()) <= 0) {
                                tblKnbFrozDetl.setFrozbl(tblKnbUnfr.getUnfram()
                                        .add(tblKnbFrozDetl.getFrozbl()));

                            } else {
                                tblKnbFrozDetl.setFrozbl(tblKnbFrozDetl
                                        .getFrozbl());
                            }
                        }

                        tblKnbFrozDetl.setFrozst(E_FROZST.VALID);
                        KnbFrozDetlDao.updateOne_odb2(tblKnbFrozDetl);

                    }
                }

                // 对冻结主体登记簿（部冻）
                if (!CommUtil.equals(info.get(0).getUnfram(), BigDecimal.ZERO)) {
                    KnbFrozOwne tblKnbFrozOwne = KnbFrozOwneDao
                            .selectOneWithLock_odb1(E_FROZOW.AUACCT, info
                                    .get(0).getCustac(), false);

                    if (CommUtil.isNull(tblKnbFrozOwne)) {
                        throw DpModuleError.DpstComm.BNAS1706();
                    }

                    // 查询冻结记录
                    // KnbFroz tblKnbFroz = KnbFrozDao.selectOne_odb8(frozno,
                    // info.get(0).getOdfrsq(), false);
                    // if(CommUtil.isNull(tblKnbFroz)){
                    // throw DpModuleError.DpstComm.E9999("该冻结记录不存在");
                    // }

                    tblKnbFrozOwne.setFrozbl(tblKnbFrozOwne.getFrozbl().add(
                            info.get(0).getUnfram()));
                    KnbFrozOwneDao.updateOne_odb1(tblKnbFrozOwne);
                }
            }
        }

        KnbDedu tblKnbDedu = KnbDeduDao.selectOne_odb2(trandt, mntrsq, false);

        if (CommUtil.isNull(tblKnbDedu)) {
            throw DpModuleError.DpstComm.BNAS1707();
        }
        // 更新扣划冲正标识
        tblKnbDedu.setIsflag(E_YES___.YES);
        KnbDeduDao.updateOne_odb2(tblKnbDedu);
    }

    /**
     * 扣划
     * 
     * @return
     * 
     */
    public void IoDeduct(final cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDeductIn deductin,
            final cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoDeduct.Output output) {
        // 扣划输入检查
        DpDeduct.deductCheck(deductin);

        // 扣划
        DeductOut deduno = DpDeduct.deductPrc(deductin);

        // 将输入作为返回值
        IoDeductOt deductot = SysUtil.getInstance(IoDeductOt.class);
        deductot.setDeduno(deduno.getDeduno());
        CommUtil.copyProperties(deductot, deductin);
        deductot.setAcctno(deduno.getAcctno());
        deductot.setAcctna(deduno.getAcctna());
        deductot.setBrchno(deduno.getBrchno());
        // 输出
        // cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoDeduct.Output
        // output = SysUtil
        // .getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoDeduct.Output.class);
        output.setDeductot(deductot);
    }

    /*电子账户延迟转账电子账户登记簿*/
    public void prcIoDpAcToAcDelay(final cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoDpAcToAcDelay.Input input) {
        KnaDelay tblKnaDelay = SysUtil.getInstance(KnaDelay.class);

        if (input.getIssucc() == E_YES___.YES) {
            tblKnaDelay.setTransq(input.getTransq()); // 主交易流水
            tblKnaDelay.setTrandt(input.getMntrdt()); // 交易日期
            tblKnaDelay.setTrantm(input.getMntrtm()); // 交易时间
        } else {
            tblKnaDelay.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
            tblKnaDelay.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); // 交易日期
            tblKnaDelay.setTrantm(BusiTools.getBusiRunEnvs().getTrantm()); // 交易时间
        }
        tblKnaDelay.setAcdate(input.getAcdate()); // 到账日期
        tblKnaDelay.setActime(input.getActime()); // 到账时间
        tblKnaDelay.setServtp(input.getServtp()); // 交易渠道
        tblKnaDelay.setCapitp(E_CAPITP.NT301); // 资金交易类型 --电子账户转电子账户
        tblKnaDelay.setCardno(input.getCardno()); // 卡号
        tblKnaDelay.setCuacno(input.getCuacno()); // 转入方电子账户
        tblKnaDelay.setAcctno(input.getAcctno()); // 转入方子账户
        tblKnaDelay.setInacna(input.getInacna()); // 转入方户名
        tblKnaDelay.setOtacna(input.getOtacna()); // 转出方户名
        tblKnaDelay.setBrchno(input.getBrchno()); // 转入方账户所属机构
        tblKnaDelay.setIncorp(input.getIncorp()); // 转入方账户所属法人
        tblKnaDelay.setTocorp(input.getTocorp()); // 转出方账户所属法人
        tblKnaDelay.setTobrch(input.getTobrch()); // 转出方账号所属机构
        tblKnaDelay.setToacct(input.getToacct()); // 转出方账号/卡号
        tblKnaDelay.setToscac(input.getToscac()); // 转出方电子账户
        tblKnaDelay.setToacno(input.getToacno()); // 转出方子账号
        tblKnaDelay.setCrcycd(input.getCrcycd()); // 币种
        tblKnaDelay.setTrantp(input.getTrantp()); // 转账时效标志
        tblKnaDelay.setTranam(input.getTranam()); // 交易金额
        tblKnaDelay.setDelayt(E_DELAYT.WAIT); // 转账状态 -- 等待转账
        tblKnaDelay.setTlcgam(input.getTlcgam()); // 收费总金额
        tblKnaDelay.setRemark(null); // 备注信息

        KnaDelayDao.insert(tblKnaDelay);
    }

    /*电子账户延迟转账电子账户明细登记簿*/
    public void prcIoDpAcToAcDelayDetl(final cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoDpAcToAcDelayDetl.Input input) {
        KnaDelayDetl tblKnaDelayDetl = SysUtil.getInstance(KnaDelayDetl.class);

        if (input.getIssucc() == E_YES___.YES) {
            tblKnaDelayDetl.setTransq(input.getTransq()); // 主交易流水
        } else {
            tblKnaDelayDetl.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
        }
        tblKnaDelayDetl.setServsq(input.getServsq()); // 渠道流水
        tblKnaDelayDetl.setServdt(input.getServdt()); // 渠道日期
        tblKnaDelayDetl.setBusisq(input.getBusisq()); // 业务跟踪编号
        tblKnaDelayDetl.setPrcscd(input.getPrcscd()); // 处理码
        tblKnaDelayDetl.setKeepdt(input.getKeepdt()); // 清算日期
        tblKnaDelayDetl.setChckdt(input.getChckdt()); // 对账日期
        tblKnaDelayDetl.setAccttp(input.getAccttp()); // 转入账户分类
        tblKnaDelayDetl.setAclmfg(input.getAclmfg()); // 累积限额标志
        tblKnaDelayDetl.setCustac(input.getCustac()); // 转入方电子账号
        tblKnaDelayDetl.setFacesg(input.getFacesg()); // 转入方面签标识2
        tblKnaDelayDetl.setCustie(input.getCustie()); // 转入方是否绑定卡2
        tblKnaDelayDetl.setAuthtp(input.getAuthtp()); // 认证方式
        tblKnaDelayDetl.setDcflag(input.getDcflag()); // 额度收方标志
        tblKnaDelayDetl.setCustid(input.getCustid()); // 客户ID
        tblKnaDelayDetl.setCustlv(input.getCustlv()); // 客户等级
        tblKnaDelayDetl.setAcctrt(input.getAcctrt()); // 账号路由类型
        tblKnaDelayDetl.setLimttp(input.getLimttp()); // 额度类型
        tblKnaDelayDetl.setPytltp(input.getPytltp()); // 支付工具
        tblKnaDelayDetl.setRebktp(input.getRebktp()); // 收款行范围
        tblKnaDelayDetl.setRisklv(input.getRisklv()); // 风险承受等级
        tblKnaDelayDetl.setSbactp(input.getSbactp()); // 子账户类型
        tblKnaDelayDetl.setRecpay(input.getRecpay()); // 收付方标识
        tblKnaDelayDetl.setAuacfg(input.getAuacfg()); // 存取标志
        tblKnaDelayDetl.setLinkno(input.getLinkno()); // 连笔号
        tblKnaDelayDetl.setSmryco(input.getSmryco()); // 转出方摘要码
        tblKnaDelayDetl.setSmryci(input.getSmryci()); // 转入方摘要码
        tblKnaDelayDetl.setIschrg(input.getIschrg()); // 是否收费
        tblKnaDelayDetl.setChrgcd(input.getChrgpm().get(0).getChrgcd()); // 收费代码
        tblKnaDelayDetl.setCustno(input.getChrgpm().get(0).getCustno()); // 客户号
        tblKnaDelayDetl.setCustad(input.getChrgpm().get(0).getCustac()); // 客户账号
        tblKnaDelayDetl.setAmount(input.getChrgpm().get(0).getAmount()); // 数量
        tblKnaDelayDetl.setClcham(input.getChrgpm().get(0).getClcham()); // 计算费用金额
        tblKnaDelayDetl.setDisrat(input.getChrgpm().get(0).getDisrat()); // 优惠比率
        tblKnaDelayDetl.setDircam(input.getChrgpm().get(0).getDircam()); // 优惠后应收金额
        tblKnaDelayDetl.setPaidam(input.getChrgpm().get(0).getPaidam()); // 实收金额
        tblKnaDelayDetl.setChnotp(input.getChrgpm().get(0).getChnotp()); // 费种大类
        tblKnaDelayDetl.setChnona(input.getChrgpm().get(0).getChnona()); // 费种大类名称
        tblKnaDelayDetl.setPronum(input.getChrgpm().get(0).getPronum()); // 产品编码
        tblKnaDelayDetl.setTrinfo(input.getChrgpm().get(0).getTrinfo()); // 交易信息
        tblKnaDelayDetl.setServtp(input.getChrgpm().get(0).getServtp()); // 交易渠道
        tblKnaDelayDetl.setDioage(input.getChrgpm().get(0).getDioage()); // 分润方一机构号
        tblKnaDelayDetl.setDiwage(input.getChrgpm().get(0).getDiwage()); // 分润方二机构号
        tblKnaDelayDetl.setDitage(input.getChrgpm().get(0).getDitage()); // 分润方三机构号
        tblKnaDelayDetl.setDifage(input.getChrgpm().get(0).getDifage()); // 分润方四机构号
        tblKnaDelayDetl.setDioamo(input.getChrgpm().get(0).getDioamo()); // 分润方一金额
        tblKnaDelayDetl.setDiwamo(input.getChrgpm().get(0).getDiwamo()); // 分润方二金额
        tblKnaDelayDetl.setDitamo(input.getChrgpm().get(0).getDitamo()); // 分润方三金额
        tblKnaDelayDetl.setDifamo(input.getChrgpm().get(0).getDifamo()); // 分润方四金额
        tblKnaDelayDetl.setScencd(input.getChrgpm().get(0).getScencd()); // 场景代码
        tblKnaDelayDetl.setScends(input.getChrgpm().get(0).getScends());// 场景描述
        tblKnaDelayDetl.setChgflg(input.getChgflg()); // 记账标志
        tblKnaDelayDetl.setCsextg(input.getCsextg()); // 钞汇标志
        tblKnaDelayDetl.setIncorp(input.getIncorp()); // 转入方法人
        tblKnaDelayDetl.setOtcorp(input.getOtcorp()); // 转出方法人
        tblKnaDelayDetl.setCuacst(input.getCuacst()); // 客户号状态
        tblKnaDelayDetl.setAcctno(input.getAcctno()); // 负债账号
        
        KnaDelayDetlDao.insert(tblKnaDelayDetl);
    }

    // 查询电子账户延迟转账登记簿
    public Options<IoAcToAcDelayInfo> prcIoQueryDelay(final cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoQueryDelay.Input input) {
        // 卡号、主交易流水不能同时为空
        if (CommUtil.isNull(input.getCardno()) && CommUtil.isNull(input.getTransq())) {
            throw DpModuleError.DpstComm.E9028();
        }

        // 页码
        long iPageno = CommTools.getBaseRunEnvs().getPage_start();
        // 页面大小
        long iPgsize = CommTools.getBaseRunEnvs().getPage_size();

        Page<IoAcToAcDelayInfo> acToAcDelay = EacctMainDao.selAcToAcDelay(input.getTransq(), input.getCardno(), (iPageno - 1) * iPgsize, iPgsize, 0, false);

        Options<IoAcToAcDelayInfo> cplIoAcToAcDelayInfo = new DefaultOptions<>();

        cplIoAcToAcDelayInfo.addAll(acToAcDelay.getRecords());

        CommTools.getBaseRunEnvs().setTotal_count(acToAcDelay.getRecordCount());

        return cplIoAcToAcDelayInfo;
    }

    /*撤销转账*/
    public void prcCancelAcToAc(final cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.CancelAcToAc.Input input) {
        if (CommUtil.isNull(input.getTransq())) {
            throw DpModuleError.DpstComm.BNAS0050();
        }
        KnaDelay tblKnaDelay = KnaDelayDao.selectOne_odb1(input.getTransq(), false);

        if (CommUtil.isNull(tblKnaDelay)) {
            throw DpModuleError.DpstComm.E9030();
        }
        
        if(tblKnaDelay.getDelayt()!= E_DELAYT.WAIT){
            throw DpModuleError.DpstComm.E9029();
        }

        tblKnaDelay.setDelayt(E_DELAYT.STOP);

        KnaDelayDao.updateOne_odb1(tblKnaDelay);
    }
    
    /**
     * 
     * <p>Title:danBiFroz </p>
     * <p>Description:单笔冻结，为实现存款冻结功能，单独冻结一个电子账户-金额冻结。	</p>
     * @author huangwh
     * @date   2018年11月30日 
     * @param danBiFrozInfo
     * 
     */
    @Override
    public void danBiFroz(danBiFrozInfo danBiFrozInfo){
        //输入参数校验
        InputChk(danBiFrozInfo);
        
        //根据冻结期限(频率)计算冻结终止日期
        String endday = DateTools2.calDateByNextFreq(CommTools.getBaseRunEnvs().getTrxn_date(), danBiFrozInfo.getFroztm());
        
        //组装系统冻结检查参数
        IoDpStFzIn ioDpStFzIn = SysUtil.getInstance(IoDpStFzIn.class);
        ioDpStFzIn.setCardno(danBiFrozInfo.getCardno());//卡号：客户电子账号
        ioDpStFzIn.setCrcycd(danBiFrozInfo.getCrcycd());//币种
        ioDpStFzIn.setEndday(endday);//冻结终止日期
        ioDpStFzIn.setFrozam(danBiFrozInfo.getFrozam());//冻结金额
        ioDpStFzIn.setFroztp(E_FROZTP.AM);//冻结业务类型:金额冻结
        ioDpStFzIn.setSprdid(danBiFrozInfo.getProdcd());//产品编号
        ioDpStFzIn.setSprdna(danBiFrozInfo.getSprdna());//产品名称
        
        ioDpStFzIn.setProdcd(null);//产品代码
        ioDpStFzIn.setSprdvr(null);//版本号
        ioDpStFzIn.setFtrate(null);//预期年化收益率(最低)
        ioDpStFzIn.setTtrate(null);//预期年化收益率(最低)
        
        //系统冻结检查
        DpFrozProc.syetemFrozCheck(ioDpStFzIn);

        // 系统冻结
        DpFrozProc.systemFrozDo(ioDpStFzIn);//冻结终止日期写死了，需修改：根据参数中的有无设置值！
        
    }
    
    /**
     * 
     * <p>Title:InputChk </p>
     * <p>Description:输入参数校验	</p>
     * @author huangwh
     * @date   2018年11月30日 
     * @param danBiFrozInfo 单笔冻结输入参数
     * 
     */
    public void InputChk(danBiFrozInfo danBiFrozInfo){
        //参数非空校验
        if (CommUtil.isNull(danBiFrozInfo.getProdcd())) {
            throw DpModuleError.DpstComm.E9990("产品编号不能为空");
        }
        if (CommUtil.isNull(danBiFrozInfo.getSprdna())) {
            throw DpModuleError.DpstComm.E9990("产品名称不能为空");
        }
        if (CommUtil.isNull(danBiFrozInfo.getFrozam())) {
            throw DpModuleError.DpstComm.E9990("冻结金额不能为空");
        }
        if (CommUtil.isNull(danBiFrozInfo.getFroztm())) {
            throw DpModuleError.DpstComm.E9990("冻结期限不能为空");
        }
        if (CommUtil.isNull(danBiFrozInfo.getCardno())) {
            throw DpModuleError.DpstComm.E9990("冻结账号不能为空");
        }
        if (CommUtil.isNull(danBiFrozInfo.getCrcycd())) {
            throw DpModuleError.DpstComm.E9990("币种不能为空");
        }
    }
    
    /**
     * 
     * <p>Title:danBiStUf </p>
     * <p>Description:单笔解冻，为实现存款解约解冻，单独解冻一个电子账户。</p>
     * @author huangwh
     * @date   2018年12月1日 
     * @param ioDpStUfIn 系统解冻输入接口：交易日期、主交易流水、上送原交易日期、上送原交易流水
     */
    @Override
    public void danBiStUf(IoDpStUfIn ioDpStUfIn){
         //上送解冻可能不是本系统的原交易流水为上送流水，所以要先查找下
        if (CommUtil.isNotNull(ioDpStUfIn.getInpudt()) && CommUtil.isNotNull(ioDpStUfIn.getInpusq())) {
            KnsRedu knsReduDO = KnsReduDao.selectFirst_odb4(ioDpStUfIn.getInpusq(), ioDpStUfIn.getInpudt(), false);
            if (CommUtil.isNotNull(knsReduDO)) {
                ioDpStUfIn.setMntrsq(knsReduDO.getTransq());
                ioDpStUfIn.setTrandt(knsReduDO.getTrandt());
            }
        }
        // 系统解冻检查
        DpFrozProc.syetemUnfrCheck(ioDpStUfIn);

        // 系统解冻
        DpFrozProc.systemUnfrDo(ioDpStUfIn);
    }
    
    /**
     * 系统止付--即富收单调用
     */
	@Override
	public IoDpStFzOt IoDpSyStopay(String acctno, BigDecimal tranam, String crcycd, E_FROZTP froztp) {

		// 系统止付检查
        DpFrozProc.syetemStopayCheck(acctno, tranam, crcycd, froztp);

        // 系统止付
        DpFrozProc.systemStopayDo(acctno, tranam, crcycd, froztp);

        IoDpStFzOt cpliodpstfzot = SysUtil.getInstance(IoDpStFzOt.class);

        // 将输入作为返回值
        // CommUtil.copyProperties(cpliodpstfzot, cpliodpstfzin);
        cpliodpstfzot.setFrozdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 冻结日期
        cpliodpstfzot.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 主交易流水
        cpliodpstfzot.setRemark(froztp.getValue()); // 备注

        return cpliodpstfzot;
	}

	@Override
	public void loQueryAcctList(cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.loQueryAcctList.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.loQueryAcctList.Output output) {
			String acalno=input.getAcalno();
			String corpno = CommTools.getBaseRunEnvs().getBusi_org_id(); //法人代码 
			List<CustAcctInfo> acctnoList =  DpFrozDao.selAcctnoList(acalno, corpno, false);
			if(acctnoList.size()==0) {
				throw DpModuleError.DpAcct.AT020038();
			}else {
		
			output.getAcctnoList().setValues(acctnoList);
		
			}
	}

	public void qryFrozInfo( final cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.qryFrozInfo.Input input,  final cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.qryFrozInfo.Output output){
		long pageno = CommTools.getBaseRunEnvs().getPage_start();
		long pagesize = CommTools.getBaseRunEnvs().getPage_size();
		// 电子账号
		String custac = input.getCustac();
		String acalno=input.getAcalno();
		String frozno=input.getFrozno();
		E_FROZTP froztp=input.getFroztp();
		String frozdt=input.getFrozdt();
		E_FROZST frozst=input.getFrozst();
		String corpno= BusiTools.getTranCorpno();
		Page<frozOutInfo> frozList = DpFrozDao.qryFrozInfo(froztp, frozst, frozdt, frozno, custac, acalno, (pageno-1)*pagesize, pagesize, 0, false);
		Options list=SysUtil.getInstance(Options.class);
		for(frozOutInfo ent:frozList.getRecords()) {
			if(JFBaseEnumType.E_STACTP.STMA == ent.getStactp()) {
			 if(CommUtil.isNotNull(ent.getCustna())) {
			   ent.setTmcustna(DecryptConstant.maskName(ent.getCustna()));
			 }
			}else if(JFBaseEnumType.E_STACTP.STSA == ent.getStactp()){
				ent.setTmcustna(ent.getCustna());
			}
			if(CommUtil.isNotNull(ent.getAcalno())) {
			ent.setTmtlphno(DecryptConstant.maskMobile(ent.getAcalno()));
			}
			if(CommUtil.isNotNull(ent.getFrna01())) {
				ent.setTmfrna01(DecryptConstant.maskMobile(ent.getFrna01()));
			}
			if(CommUtil.isNotNull(ent.getFrna02())) {
				ent.setTmfrna02(DecryptConstant.maskMobile(ent.getFrna02()));
			}
			if(CommUtil.isNotNull(ent.getIdno01())) {
				ent.setTmidno01(DecryptConstant.maskMobile(ent.getIdno01()));
			}
			if(CommUtil.isNotNull(ent.getIdno02())) {
				ent.setTmidno02(DecryptConstant.maskMobile(ent.getIdno02()));
			}			
			list.add(ent);
		}	
	  //  list.setValues(frozList.getRecords());
		output.setFrozInfo(list);
		CommTools.getBaseRunEnvs().setTotal_count(frozList.getRecordCount());
	}

	/**
	 * 即富冻结
	 */
	public DpFrozOutInfo IoDpFrozForJF( final DpFrozInfo dpFrozInfo){

    	// 司法冻结检查
        DpFrozProc.frozByLawCheckJF(dpFrozInfo);
        
        DpFrozOutInfo entity = SysUtil.getInstance(DpFrozOutInfo.class);
        // 司法冻结
        DpFrozProc.prcFrozJF(dpFrozInfo, entity);
        
        entity.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
        entity.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());
        
        // 冲正登记
        IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
        if (CommUtil.isNotNull(entity.getFrozno())) {
            cplInput.setEvent1(entity.getFrozno()); // 冻结编号
        }
        IoMsRegEvent ioMsReg = SysUtil.getInstance(IoMsRegEvent.class);
		ioMsReg.setEvent_status(E_EVNTST.SUCCESS);//事件状态SUCCESS（成功）STRIKED（已冲正）NEED2C（需要二次提交）
		ioMsReg.setInformation_value(SysUtil.serialize(cplInput));
    	ioMsReg.setReversal_event_id("strkeDpFrozAcct");//冲正事件ID
    	ioMsReg.setService_id("strkeDpFrozAcct");//服务ID
		ioMsReg.setSub_system_id(CoreUtil.getSubSystemId());//子系统ID
		ioMsReg.setTxn_event_level(E_EVENTLEVEL.LOCAL);//教义事件级别NORMAL（）INQUIRE（）LOCAL（）CRDIT（）
		ioMsReg.setIs_across_dcn(E_YESORNO.NO);
		
		MsEvent.register(ioMsReg, true);
        
        return entity;
    
	}
}


