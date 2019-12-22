package cn.sunline.ltts.busi.dptran.trans.lzbank;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.type.CustInfoCountType.InfoKnlIoblError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_RQFITY;


public class qysfor {

    public static void dealqysfor( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qysfor.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qysfor.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qysfor.Output output){
        String trandt=input.getTrandt();
        E_RQFITY rqfity = input.getRqfity();
        E_TRANST status = input.getStatus();
        //页码
        long pageno = CommTools.getBaseRunEnvs().getPage_start();
        //页容量
        long pgsize = CommTools.getBaseRunEnvs().getPage_size();        
        long totlCount = 0;
        if(CommUtil.isNull(trandt)){
            throw DpModuleError.DpstComm.E9999("文件日期不能为空!");
        }
        if(CommUtil.isNull(rqfity)){
            throw DpModuleError.DpstComm.E9999("文件类型不能为空!");
        }
        String servtp =null;
        if(rqfity==E_RQFITY.CTHX){
            servtp = "IM";
        }else if(rqfity==E_RQFITY.SP){
            servtp = "SI";
        }else{
            throw DpModuleError.DpstComm.E9999("文件类型不匹配!");
        }
        Page<InfoKnlIoblError> list = DpAcctDao.selKnlIoblError(trandt,status, servtp,(pageno-1)*pgsize, pgsize, totlCount, false);
        if(list.getRecordCount()>0){
            output.getIoKnlIoblError().addAll(list.getRecords());
            CommTools.getBaseRunEnvs().setTotal_count(list.getRecordCount());
        }
    }
}
