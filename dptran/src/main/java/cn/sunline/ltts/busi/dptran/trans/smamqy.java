
package cn.sunline.ltts.busi.dptran.trans;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccsDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.dp.namedsql.YhtDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbdl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.type.DpYhtType.IoDpTranDetlDqTht;
//import cn.sunline.ltts.busi.dp.type.DpAcctType.IoDpTranDetlDqTht;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;



public class smamqy {
    private static final BizLog biglog = BizLogUtil.getBizLog(smamqy.class);
    public static void qrySmamqy( final cn.sunline.ltts.busi.dptran.trans.intf.Smamqy.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Smamqy.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Smamqy.Output output){
        
        if(CommUtil.isNull(input.getIdtftp())){
            throw DpModuleError.DpstComm.E9999("证件类型不能为空！");
        }
        if(CommUtil.isNull(input.getIdtfno())){
            throw DpModuleError.DpstComm.E9999("证件号码不能为空！");
        }
        if (E_IDTFTP.SFZ != input.getIdtftp()) {
            throw DpModuleError.DpstComm.E9999("暂时只支持身份证！");
        }
        
        //校验证件类型、证件号码
        BusiTools.chkCertnoInfo(input.getIdtftp(), input.getIdtfno());
        
        String custac = "";
        //根据负债账号查询电子账号
        KnaAccs tblKnaAccs = KnaAccsDao.selectOne_odb2(input.getAcctno(), false);
        if(CommUtil.isNull(tblKnaAccs)){
            throw CaError.Eacct.E0001("该存款子账户号不存在");
        }
        if(CommUtil.isNotNull(input.getProdcd())){
            if(!input.getProdcd().equals(tblKnaAccs.getProdcd())){
                throw CaError.Eacct.E0001("该存款子账户号"+tblKnaAccs.getAcctno()+"没有购买"+input.getProdcd()+"产品！");
            }
        }
        custac = tblKnaAccs.getCustac();
        
        KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(custac, false);
        if(CommUtil.isNull(tblKnaCust)){
            throw CaError.Eacct.BNAS1659();//电子账号不存在!
        }
//        CifCust cifCust = CifCustDao.selectOne_odb1(tblKnaCust.getCustno(), false);
//        if(CommUtil.isNull(cifCust)){
//            throw CaError.Eacct.E0001("客户号不存在！");
//        }
//        if(!CommUtil.isNull(cifCust)){
//            //身份证验证
//            if(!CommUtil.equals(cifCust.getIdtfno(), input.getIdtfno())){
//                throw DpModuleError.DpstComm.E9999("客户卡号和证件号码 不匹配！");
//            }
//        }
        //银户通借口文档输入字段，没有卡号，暂时注释
        /*String cardno = input.getCardno();// 银户通增加查询字段
        // 电子账号
        if (CommUtil.isNull(cardno)) {
            throw DpModuleError.DpstAcct.BNAS0311();
        }
        KnaAcdc knaAcdcDO = KnaAcdcDao.selectOne_odb2(cardno, false);
        if (CommUtil.isNotNull(knaAcdcDO)) {
            custac = knaAcdcDO.getCustac();
        }*/
        IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
        if (E_YES___.YES == cplAcStatus.getClstop()) {
            throw DpModuleError.DpstComm.BNAS0898();
        }
        // 电子账户状态检查
        E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
        if (CommUtil.isNotNull(cuacst)) {

            if (cuacst == E_CUACST.CLOSED) { // 销户
                throw DpModuleError.DpstComm.BNAS0883();

            } else if (cuacst == E_CUACST.OUTAGE) { // 停用
                throw DpModuleError.DpstComm.BNAS0886();

            } else if (cuacst == E_CUACST.PREOPEN) { // 预开户
                throw DpModuleError.DpstComm.BNAS0881();
            }
        } else {
            throw DpModuleError.DpstComm.BNAS1206();
        }
        KnaAcct dpacct = YhtDao.selKnaacctByAcctnoCorpnoAcctst(input.getAcctno(), CommTools.getBaseRunEnvs().getBusi_org_id(), E_DPACST.NORMAL, false);
                //b15(input.getAcctno(),custac, E_DPACST.NORMAL, false);
                 
        if (CommUtil.isNull(dpacct)) {
            KnaFxac dpfxac = YhtDao.selKnafxacByAcctnoCorpnoAcctst(input.getAcctno(), CommTools.getBaseRunEnvs().getBusi_org_id(), E_DPACST.NORMAL, false);
            //KnaFxac dpfxac = KnaFxacDao.selectOne_odb11(input.getAcctno(),custac, E_DPACST.NORMAL, false);
            if (CommUtil.isNull(dpfxac)) {
                throw DpModuleError.DpstComm.BNAS0701();
            }

        } else {
            if (dpacct.getDebttp() != E_DEBTTP.DP2404) {
                throw DpModuleError.DpstComm.BNAS0701();
            }
        }

        // 查询是否有正常签约信息
        if (CommUtil.isNull(input.getPageno())) {
            throw DpModuleError.DpstComm.BNAS0249();
        }
        if (CommUtil.isNull(input.getPagesz())) {
            throw DpModuleError.DpstComm.BNAS0252();
        }
    }
    public static void selAmamqy( final cn.sunline.ltts.busi.dptran.trans.intf.Smamqy.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Smamqy.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Smamqy.Output output){
        int totlCount = 0; // 记录总数
        int startno = (input.getPageno() - 1) * input.getPagesz();// 起始记录数
        String acctno = input.getAcctno();// 负债账号
        //String custac = "";
        //KnaFxac KnaFxac =SysUtil.getInstance(KnaFxac.class);
        //根据负债账号查询电子账号
        KnaAccs tblKnaAccs = KnaAccsDao.selectOne_odb2(input.getAcctno(), false);
        if(CommUtil.isNull(tblKnaAccs)){
            throw CaError.Eacct.E0001("该存款子账户号不存在");
        }
        //custac = tblKnaAccs.getCustac();
        /*String cardno = input.getCardno();// 银户通增加查询字段
        // 电子账号
        if (CommUtil.isNull(cardno)) {
            throw DpModuleError.DpstAcct.BNAS0311();
        }
        KnaAcdc knaAcdcDO = KnaAcdcDao.selectOne_odb2(cardno, false);
        if (CommUtil.isNotNull(knaAcdcDO)) {
            custac = knaAcdcDO.getCustac();
        }*/     
        // 负债账户
        if (CommUtil.isNull(acctno)) {
            throw DpModuleError.DpstAcct.BNAS0766();
        }
        // 页码
        if (CommUtil.isNull(input.getPageno())) {
            throw CaError.Eacct.BNAS0977();
        }
        // 页容量
        if (CommUtil.isNull(input.getPagesz())) {
            throw CaError.Eacct.BNAS0463();
        }

        // 根据负债子账号获取电子账号
    
        /*custac = DpAcctDao.selKnaAccsByAcctNo(acctno, false);
        if (CommUtil.isNull(custac)) {
            throw CaError.Eacct.BNAS0465(acctno);
        }*/
        //获取预计收益
        Page<KnbCbdl> pageKnbCbdl = YhtDao.selKnbCbdlByAcctno(acctno, CommTools.getBaseRunEnvs().getTrxn_date(), startno, input.getPagesz(), totlCount, false);
        List<KnbCbdl> tblKnbCbdl = pageKnbCbdl.getRecords();
        if(CommUtil.isNotNull(tblKnbCbdl)){
            
            for(int i=0;i<tblKnbCbdl.size();i++){
                IoDpTranDetlDqTht info = SysUtil.getInstance(IoDpTranDetlDqTht.class);
                if(i==0){
                    if(tblKnbCbdl.size()==1){//只有一条数据
                        info.setIntrvl(tblKnbCbdl.get(i).getCabrin());//最后一条数据，也就是第一天的应计提利息
                    }else{
                        info.setIntrvl(tblKnbCbdl.get(i).getCabrin().subtract(tblKnbCbdl.get(i+1).getCabrin()));// 系统日期前一天应计提利息
                    }
                }else if(i>0){
                    if(i==tblKnbCbdl.size()-1){//最后一条数据，也就是第一天的应计提利息
                        info.setIntrvl(tblKnbCbdl.get(i).getCabrin());//第一天的累计收益
                    }else{
                        info.setIntrvl(tblKnbCbdl.get(i).getCabrin().subtract(tblKnbCbdl.get(i+1).getCabrin()));//下一天的计提利息减去上一天的计提利息作为一天的累计收益
                    }
                }
                
                info.setTrandt(tblKnbCbdl.get(i).getCabrdt());// 计提日期
                info.setTmstmp(tblKnbCbdl.get(i).getTmstmp());// 时间戳
                info.setAcctno(acctno);// 负债账号
                info.setCrcycd(tblKnbCbdl.get(i).getCrcycd());// 币种
                
                String dateall = tblKnbCbdl.get(i).getTmstmp();
                Date ret = null;
                try {
                    ret = new SimpleDateFormat("yyyyMMddHHmmss").parse(dateall);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                	biglog.debug("[%s]", e);
//                    e.printStackTrace();
                }
                String ret12 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ret);
                biglog.info(ret12 + "--------ret12-------------");

                info.setYhtrdt(ret12);
                output.getDqtrdt().add(info);
                biglog.debug("<<<<<<<<<<<<<<<<<<定期智能存款交易明细>>>>>>>>>>>>>>>>>>>：output=" + output);
            }
            
            //累计收益最新的一天为前面所有天数收益的叠加
            /*for(KnbCbdl cbdl : tblKnbCbdl){
                IoDpTranDetlDqTht info = SysUtil.getInstance(IoDpTranDetlDqTht.class);

                BigDecimal intrvl = BigDecimal.ZERO;// 结息总额
                
                info.setTrandt(cbdl.getCabrdt());// 计提日期
                info.setTmstmp(cbdl.getTmstmp());// 时间戳
                info.setIntrvl(cbdl.getCabrin());// 应计提利息
                info.setAcctno(acctno);// 负债账号
                info.setCrcycd(cbdl.getCrcycd());// 币种
                
                String dateall = cbdl.getTmstmp();
                Date ret = null;
                try {
                    ret = new SimpleDateFormat("yyyyMMddHHmmss").parse(dateall);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String ret12 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ret);
                biglog.info(ret12 + "--------ret12-------------");

                info.setYhtrdt(ret12);
                biglog.info(intrvl + "--------intrvl-------------");
                output.getDqtrdt().add(info);
                biglog.debug("<<<<<<<<<<<<<<<<<<定期智能存款交易明细>>>>>>>>>>>>>>>>>>>：output=" + output);
            }*/
        }
        
        /*// 查询定期存款信息
        KnaFxac = YhtDao.selKnafxacByAcctnoCorpnoAcctst(input.getAcctno(), CommTools.getBaseRunEnvs().getBusi_org_id(), E_DPACST.NORMAL, false);
        //KnaFxac KnaFxac = KnaFxacDao.selectOne_odb11(input.getAcctno(),custac, E_DPACST.NORMAL, false);

        // 判断定期存款信息是否存在
        if (CommUtil.isNull(KnaFxac)) {
            throw DpModuleError.DpstAcct.BNAS0842();
        }

        // 查询定期存款交易明细
        //Page<KnlBill> cplKnlbill = DpAcctDao.selKnlBillLiXi(acctno, startno, input.getPagesz(), totlCount, false);
        Page<KnlBill> cplKnlbill = YhtDao.selKnlbillByAcctno(acctno, CommTools.getBaseRunEnvs().getBusi_org_id(), startno, input.getPagesz(), totlCount, false);
        //Page<KnlBill> cplKnlbill = DpAcctDao.selKnlBillByAcctno(acctno, CommTools.getBaseRunEnvs().getBusi_org_id(), startno, totlCount, false);
        // 获取查询信息
        List<KnlBill> knlBill = cplKnlbill.getRecords();

        // 总利息
        BigDecimal tranall = BigDecimal.ZERO;
        for (KnlBill knlBillInfos : knlBill) {
            IoDpTranDetlDqTht info = SysUtil.getInstance(IoDpTranDetlDqTht.class);

            BigDecimal intrvl = BigDecimal.ZERO;// 结息总额
            BigDecimal rlintr = BigDecimal.ZERO;// 每条记录结息

            info.setTrandt(knlBillInfos.getTrandt());// 交易日期
            info.setTmstmp(knlBillInfos.getTmstmp());// 交易时间
            info.setTranam(knlBillInfos.getTranam());// 交易金额

            // 获取结算利息
            List<KnbPidl> tblKnbPidls = DpAcctDao.selKnbPidlByPyinsq(knlBillInfos.getAcctno(), knlBillInfos.getTransq(), false);

            if (tblKnbPidls.size() > 0) {
                for (KnbPidl tblKnbPidl : tblKnbPidls) {
                    rlintr = tblKnbPidl.getRlintr();
                    intrvl = intrvl.add(rlintr);
                }
            }
            
            if(BigDecimal.ZERO==intrvl){
                continue;
            }
            
            info.setIntrvl(intrvl);// 支取金额利息
            info.setAcctno(knlBillInfos.getAcctno());// 负债账号
            info.setCrcycd(knlBillInfos.getTrancy());// 币种
            info.setTrantp(knlBillInfos.getAmntcd());// 交易类型
            String dateall = knlBillInfos.getTrandt() + knlBillInfos.getTrantm();
            Date ret = null;
            try {
                ret = new SimpleDateFormat("yyyyMMddHHmmss").parse(dateall);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String ret12 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ret);
            biglog.info(ret12 + "--------ret12-------------");

            info.setYhtrdt(ret12);
            tranall = tranall.add(intrvl);
            biglog.info(tranall + "--------tranall-------------");
            biglog.debug("<<<<<<<<<<<<<<<<<<定期智能存款交易明细>>>>>>>>>>>>>>>>>>>：output=" + output);
            output.getDqtrdt().add(info);
        }

        biglog.info(tranall + "--------tranall-------------");
        // 设置报文头总记录条数
        CommTools.getBaseRunEnvs().setTotal_count(cplKnlbill.getRecordCount());*/

    }
}
