
package cn.sunline.ltts.busi.dptran.trans;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsPckg;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsPckgDao;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsRedu;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.dp.namedsql.YhtDao;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsq;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbDraw;
import cn.sunline.ltts.busi.dp.type.DpYhtType.DpKnbRegiInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaInfoOut;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TXNSTS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.yht.E_ISOVER;
import cn.sunline.ltts.busi.sys.type.yht.E_ORDETP;
import cn.sunline.ltts.busi.sys.type.yht.E_TRANST;



public class qrdprt {

    public static void susmqrSel( final cn.sunline.ltts.busi.dptran.trans.intf.Qrdprt.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrdprt.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrdprt.Output output){
        
        E_ORDETP ordrty = input.getOrdrty(); //订单类型
        String cardno = input.getCardno();//电子帐号---------输出用到
        String cordno = input.getCordno();//子订单号---------购买时候查询订单表
        String inpusq = input.getInpusq();//上送系统流水
        String inpudt = input.getInpudt();//上送系统时间
        String ordrsq = input.getOrdrsq();//订单流水
        //电子帐号
        if(CommUtil.isNull(cardno)){
            throw DpModuleError.DpstComm.BNAS0754();
        }
        //订单类型
        if(CommUtil.isNull(ordrty)){
            throw DpModuleError.DpstComm.BNAS0754();
        }
        //上送系统时间
        if(CommUtil.isNull(inpudt)){
            throw CaError.Eacct.E0001("上送系统交易日期不能为空");
        }
        //如果是购买上送系统流水和日期不能为空
        if(E_ORDETP.CKGM==ordrty &&CommUtil.isNull(cordno)){
            throw CaError.Eacct.E0001("订单类型为购买时，子订单号不能为空");
        }
        //如果是支取，交易订单流水不能为空
        if(E_ORDETP.CKZQ==ordrty && CommUtil.isNull(ordrsq)){
            throw CaError.Eacct.E0001("订单类型为支取时，订单流水不能为空");
        }
        //获取交易流水
        IoCaInfoOut info = SysUtil.getInstance(IoCaInfoOut.class);
        if(E_ORDETP.CKGM==ordrty){
            info = CaDao.selKnsreduTranByInpusqdt(inpusq,inpudt,false);
        }else if(E_ORDETP.CKZQ==ordrty){
//            info = CaDao.selKnsreduTranByInpusqdt(ordrsq,inpudt,false);
            KnbDraw tblKnbDraw = YhtDao.selKnbdrawByOrdridOrdedt(ordrsq, inpudt, false);//根据支取银户通送的订单流水和订单日期进行查询
            if(CommUtil.isNotNull(tblKnbDraw)){
                if(CommUtil.equals(tblKnbDraw.getCordst().toString(), "1")){//查询订单状态是否是成功的
                    info = SysUtil.getInstance(IoCaInfoOut.class);
                    info.setTxnsts(E_TXNSTS.SUCCESS);//订单成功时做转换
                    info.setTransq(tblKnbDraw.getTransq());//交易流水
                    info.setTrandt(tblKnbDraw.getTrandt());//交易日期
                }
            }
        }
        //获取错误码
        KnsPckg knpc =  KnsPckgDao.selectFirst_odb1(info.getTrandt(),info.getTransq(), false);
        if(CommUtil.isNotNull(info) && CommUtil.isNotNull(info.getInpusq())){
            if(CommUtil.isNotNull(info) && info.getTxnsts()==E_TXNSTS.SUCCESS){
                if(CommUtil.compare(E_ORDETP.CKGM, ordrty)==0){
                  //购买信息
                    DpKnbRegiInfo kr = YhtDao.selKnbregiByCordno(cordno, false);
                    if(CommUtil.isNotNull(kr)){
                        String acctno = null;//存款子帐号
                        String matudt = null;//到期日期
                        KupDppb kup = KupDppbDao.selectOne_odb1(kr.getProdcd(), false);
                        if(CommUtil.isNotNull(kup)){
                            if(E_FCFLAG.CURRENT == kup.getPddpfg()){
                                KnaAcct tblKnaAcct = YhtDao.selKnaacctByTransq(kr.getTransq(), false);
                                acctno = tblKnaAcct.getAcctno();
                                matudt = tblKnaAcct.getMatudt();
                            }else{
                                //购买活期的开户流水是系统生成的主交易流水，购买定期的开户流水是使用业务流水
                                KnsRedu tblKnsRedu = YhtDao.selKnsreduByOrdrsq(kr.getTransq(), false);
                                if(CommUtil.isNotNull(tblKnsRedu)){
                                    KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb9(tblKnsRedu.getBusisq(), false);
                                    if(CommUtil.isNotNull(tblKnaFxac)){
                                        acctno = tblKnaFxac.getAcctno();
                                        matudt = tblKnaFxac.getMatudt();
                                    }
                                }
                            }
                            //购买信息返回
                            output.setAcctno(acctno);//存款子帐号
                            output.setIntere(new BigDecimal(0));//利息
                            output.setCardno(cardno);//客户卡号
                            output.setDrdate(null);//支取日期
                            output.setIsover(E_ISOVER.CONTINUE);//是否到期标志
                            output.setEndate(matudt);//到期日期
                            output.setErrocd(knpc.getErrocd());//错误码
                            output.setErrotx("交易成功");//错误码描述
                            output.setOrdrty(E_ORDETP.CKGM);//订单类型
                            output.setOrdest(E_TRANST.SUCCESS);//订单状态
                            output.setTranam(kr.getSbtram());//交易金额
                            output.setBankdt(kr.getCocrdt());//交易日期
                            output.setBanksq(kr.getTransq());//交易流水
                        }
                    }else{
                        throw CaError.Eacct.E0001("该订单对应的购买信息不存在");
                   }
                }else if(CommUtil.compare(E_ORDETP.CKZQ, ordrty)==0){
                  //支取信息
                    List<KnsAcsq> tblKnsAcsq = YhtDao.selKnsacsqByTransqAmntcd(E_AMNTCD.DR, info.getTransq(), false);
                    
                    if(CommUtil.isNull(tblKnsAcsq)){
                        throw CaError.Eacct.E0001("该订单对应的会计流水不存在");
                    }
                    BigDecimal tranam = BigDecimal.ZERO;
                    for(int i=0;i<tblKnsAcsq.size();i++){
                        tranam = tranam.add(tblKnsAcsq.get(i).getTranam());
                    }
                    if(CommUtil.isNotNull(tblKnsAcsq)){
                        if(CommUtil.isNotNull(tblKnsAcsq.get(0).getProdcd())){
                            KupDppb kup = KupDppbDao.selectOne_odb1(tblKnsAcsq.get(0).getProdcd(), false);
                            if(CommUtil.isNotNull(kup)){
                                if(E_FCFLAG.CURRENT == kup.getPddpfg()){
                                    //KnaAcct tblKnaAcct = YhtDao.selKnaacctByTransq(info.getTransq(), false);
                                    //支取信息返回
                                    output.setAcctno(tblKnsAcsq.get(0).getAcctno());//负债子帐号
                                    KnsAcsq tblKnsAcsqInte = YhtDao.selKnsacsqByTransqBltype(info.getTransq(), E_BLTYPE.PYIN, false);
                                    if(CommUtil.isNull(tblKnsAcsqInte)){
                                        output.setIntere(BigDecimal.ZERO);//利息
                                    }else{
                                        output.setIntere(tblKnsAcsqInte.getTranam());//利息
                                    }
                                    output.setCardno(cardno);//客户卡号
                                    output.setDrdate(tblKnsAcsq.get(0).getTrandt());//支取日期
                                    output.setIsover(E_ISOVER.CONTINUE);//是否到期标志-未到期
                                    output.setEndate(null);//到期日期
                                    output.setErrocd(knpc.getErrocd());//错误码
                                    output.setErrotx("交易成功");//错误码描述
                                    output.setOrdrty(E_ORDETP.CKZQ);//订单类型
                                    output.setOrdest(E_TRANST.SUCCESS);//订单状态
                                    output.setTranam(tranam);//交易金额
                                    output.setBankdt(tblKnsAcsq.get(0).getTrandt());//交易日期
                                    output.setBanksq(tblKnsAcsq.get(0).getTransq());//交易流水
                                }else{
                                    KnaFxac tblKnafxac = KnaFxacDao.selectOne_odb1(tblKnsAcsq.get(0).getAcctno(), false);
                                    //String acctno = tblKnafxac.getAcctno();
                                    String acctno = tblKnsAcsq.get(0).getAcctno();
                                    String matudt = tblKnafxac.getMatudt();
                                    //支取信息返回
                                    output.setAcctno(acctno);//负债子帐号
                                    KnsAcsq tblKnsAcsqInte = YhtDao.selKnsacsqByTransqBltype(info.getTransq(), E_BLTYPE.PYIN, false);
                                    if(CommUtil.isNull(tblKnsAcsqInte)){
                                        output.setIntere(BigDecimal.ZERO);//利息
                                    }else{
                                        output.setIntere(tblKnsAcsqInte.getTranam());//利息
                                    }
                                    output.setCardno(cardno);//客户卡号
                                    output.setDrdate(tblKnsAcsq.get(0).getTrandt());//支取日期
                                    if(CommUtil.isNotNull(tblKnafxac.getMatudt())){
                                        if(CommUtil.compare(tblKnafxac.getMatudt(), CommTools.getBaseRunEnvs().getTrxn_date())>=0){
                                            output.setIsover(E_ISOVER.CONTINUE);//是否到期标志-未到期
                                        }else{
                                            output.setIsover(E_ISOVER.OVER);//是否到期标志-到期
                                        }
                                    }
                                    output.setEndate(matudt);//到期日期
                                    output.setErrocd(knpc.getErrocd());//错误码
                                    output.setErrotx("交易成功");//错误码描述
                                    output.setOrdrty(E_ORDETP.CKZQ);//订单类型
                                    output.setOrdest(E_TRANST.SUCCESS);//订单状态
                                    output.setTranam(tranam);//交易金额
                                    output.setBankdt(tblKnsAcsq.get(0).getTrandt());//交易日期
                                    output.setBanksq(tblKnsAcsq.get(0).getTransq());//交易流水
                                }
                            }
                        }
                    
                    }
                    
                    }else{
                        throw CaError.Eacct.BNAS1672(inpusq);
                    }
            }else{
                throw CaError.Eacct.E0001("不支持其他订单类型查询");
            }
        }else if(CommUtil.isNotNull(info) && info.getTxnsts()==E_TXNSTS.FAILURE){
            //支取信息返回
            output.setAcctno(null);//负债子帐号
            output.setIntere(new BigDecimal(0));//利息
            output.setCardno(cardno);//客户卡号
            output.setDrdate(null);
            output.setIsover(E_ISOVER.CONTINUE);
            output.setEndate(null);
            output.setErrocd(knpc.getErrocd());//错误码
            output.setErrotx(knpc.getErrotx());//错误信息描述
            output.setOrdrty(ordrty); //订单类型
            output.setOrdest(E_TRANST.FAIL);//订单状态
            output.setTranam(new BigDecimal(0));//交易金额（核心只登记成功订单信息，因此无法实际获取交易金额）
            output.setBankdt(info.getTrandt());//交易日期
            output.setBanksq(info.getTransq());//交易流水
        }else{
            throw CaError.Eacct.BNAS1672(inpusq);
        }
    }
}
