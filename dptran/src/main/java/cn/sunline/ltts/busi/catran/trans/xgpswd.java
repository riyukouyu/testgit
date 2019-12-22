package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;


public class xgpswd {

public static void darXgpswd( final cn.sunline.ltts.busi.catran.trans.intf.Xgpswd.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Xgpswd.Output output){
        if(CommUtil.isNull(input.getCustac())){
            throw DpModuleError.DpstComm.E9999("电子账户不能为空!");
        }
        if(CommUtil.isNull(input.getNwpswd())){
            throw DpModuleError.DpstComm.E9999("新交易密码不能为空!");
        }
        if(CommUtil.isNull(input.getOlpswd())){
            throw DpModuleError.DpstComm.E9999("原交易密码不能为空!");
        }
        if(CommUtil.isNull(input.getAcctrt())){
            throw DpModuleError.DpstComm.E9999("账号类型不能为空!");
        }
        KnaAcdc knaAcdc = KnaAcdcDao.selectOne_odb2(input.getCustac(), false);
        if(CommUtil.isNull(knaAcdc)){
            throw DpModuleError.DpstComm.E9999("电子账户未开立或已销户!");
        }
    }

public static void aftXgpswd( final cn.sunline.ltts.busi.catran.trans.intf.Xgpswd.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Xgpswd.Output output){
        output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
        output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());//主交易日期
        output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());//主交易时间
    }
}
