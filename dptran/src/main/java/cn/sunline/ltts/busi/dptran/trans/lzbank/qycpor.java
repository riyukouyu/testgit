package cn.sunline.ltts.busi.dptran.trans.lzbank;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.type.CustInfoCountType.InfoKnlIoblCupsError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DJTYPE;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUPSST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_RQFITY;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qycpor {

    public static void dealqycpor( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qycpor.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qycpor.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qycpor.Output output){
        String trandt=input.getTrandt();
        String rqfityStatus = input.getRqfity();
        E_RQFITY rqfity = CommUtil.toEnum(E_RQFITY.class, rqfityStatus.substring(0, 2));//截取前两位转换为枚举类型--文件类型
        E_CUPSST status = CommUtil.toEnum(E_CUPSST.class, rqfityStatus.substring(2));//截取最后一位转换为枚举类型--交易状态
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
        String djtype=null;
        if(rqfity==E_RQFITY.YLCP){
            djtype=E_DJTYPE.CUPSZZ.getValue();
        }else if(rqfity==E_RQFITY.YLWK){
            djtype=E_DJTYPE.YLWKZZ.getValue();
        }else if(rqfity==E_RQFITY.ALCH){
            djtype=E_DJTYPE.ALCHZZ.getValue();
        }else{
            throw DpModuleError.DpstComm.E9999("文件类型不匹配!");
        }
        /*if(rqfity!=E_RQFITY.YLCP){
            throw DpModuleError.DpstComm.E9999("文件类型不匹配!");
        }*/
        Page<InfoKnlIoblCupsError> list = DpAcctDao.selKnlIoblCupsError(trandt,status,djtype,(pageno-1)*pgsize, pgsize, totlCount, false);
        Options<InfoKnlIoblCupsError> Iofnprod = new DefaultOptions<InfoKnlIoblCupsError>();
        if(list.getRecordCount()>0){
            for(InfoKnlIoblCupsError info:list.getRecords()){
                Iofnprod.add(info);
            }
            output.getIoKnlIoblCupsError().addAll(Iofnprod);
            CommTools.getBaseRunEnvs().setTotal_count(list.getRecordCount());
        }
    }
}
