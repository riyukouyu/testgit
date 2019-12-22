
package cn.sunline.ltts.busi.dptran.trans;


import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.namedsql.YhtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapot;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIobl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ORDEST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUPSST;


public class qrinot {

public static void qrinot( final cn.sunline.ltts.busi.dptran.trans.intf.Qrinot.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrinot.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrinot.Output output){
    E_ORDEST ordest = null;
    KnlIobl knlIobl = YhtDao.selKnlioblByOrdesq(input.getOrdesq(), false);//根据订单流水查询电子账户出入金和超级网银
    KnlIoblCups knlIoblCups = YhtDao.selknlioblcupsByOrdesq(input.getOrdesq(), false);//根据订单流水查询银联CUPS/银联无卡
    KnlCnapot knlCnapot = YhtDao.selknlcnapotByOrdesq(input.getOrdesq(), false);//根据订单流水查询大小额往来账
    if(CommUtil.isNull(knlIobl) && CommUtil.isNull(knlIoblCups) && CommUtil.isNull(knlCnapot)){
        throw DpModuleError.DpstComm.E9999("该订单流水不存在！");
    }
    if(CommUtil.isNotNull(knlIobl)){//订单状态的转变
        if(knlIobl.getStatus()==E_TRANST.NORMAL || knlIobl.getStatus()==E_TRANST.SUCCESS){
            ordest = E_ORDEST.SUCCESS;
        }else if(knlIobl.getStatus()==E_TRANST.WAIT || knlIobl.getStatus()==E_TRANST.EXE){
            ordest = E_ORDEST.EXE;
        }else{
            ordest = E_ORDEST.FAIL;
        }
    }else if(CommUtil.isNotNull(knlIoblCups)){//订单状态的转变
//        if(knlIoblCups.getTranst()==E_CUPSST.SUCC || knlIoblCups.getTranst()==E_CUPSST.CLWC){
//            ordest = E_ORDEST.SUCCESS;
//        }else if(knlIoblCups.getTranst()==E_CUPSST.DCL){
//            ordest = E_ORDEST.EXE;
//        }else{
//            ordest = E_ORDEST.FAIL;
//        }
    }else if(CommUtil.isNotNull(knlCnapot)){//订单状态的转变
        if(knlCnapot.getStatus()==E_TRANST.NORMAL || knlCnapot.getStatus()==E_TRANST.SUCCESS){
            ordest = E_ORDEST.SUCCESS;
        }else if(knlCnapot.getStatus()==E_TRANST.WAIT || knlCnapot.getStatus()==E_TRANST.EXE){
            ordest = E_ORDEST.EXE;
        }else{
            ordest = E_ORDEST.FAIL;
        }
        output.setClerdt(knlCnapot.getClerdt());//清算日期
    }
    output.setOrdest(ordest);//订单状态
    output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
    output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());//主交易日期
    output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());//主交易时间
    
}
}
