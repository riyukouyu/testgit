package cn.sunline.ltts.busi.dptran.trans.lzbank;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.type.CustInfoCountType.InfoKnlCnapotError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_RQFITY;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qylmor {

    public static void dealqylmor( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qylmor.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qylmor.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qylmor.Output output){
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
        String subsys=null;
        if(rqfity==E_RQFITY.DE){
            subsys="0";//0-大额
        }else if(rqfity==E_RQFITY.SE){
            subsys="1";//1-小额
        }else{
            throw DpModuleError.DpstComm.E9999("文件类型不匹配!");
        }
        Page<InfoKnlCnapotError> list = DpAcctDao.selKnlCnapotError(trandt, subsys,status, (pageno-1)*pgsize, pgsize, totlCount, false);
        Options<InfoKnlCnapotError> Iofnprod = new DefaultOptions<InfoKnlCnapotError>();
        if(list.getRecordCount()>0){
            for(InfoKnlCnapotError info:list.getRecords()){
                Iofnprod.add(info);
            }
            output.getIoKnlCnapotError().addAll(Iofnprod);
            CommTools.getBaseRunEnvs().setTotal_count(list.getRecordCount());
        }
    
    }
}
