package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcalDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;


public class repswd {

public static void darRepswd( final cn.sunline.ltts.busi.catran.trans.intf.Repswd.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Repswd.Output output){
        if(CommUtil.isNull(input.getCustac())){
            throw DpModuleError.DpstComm.E9999("电子账户不能为空!");
        }
        if(CommUtil.isNull(input.getNwpswd())){
            throw DpModuleError.DpstComm.E9999("输入密码不能为空!");
        }
        if(CommUtil.isNull(input.getAcctrt())){
            throw DpModuleError.DpstComm.E9999("账号类型不能为空!");
        }
        if(CommUtil.isNull(input.getIdtftp())){
            throw DpModuleError.DpstComm.E9999("证件类型不能为空!");
        }
        if(CommUtil.isNull(input.getIdtfno())){
            throw DpModuleError.DpstComm.E9999("证件号码不能为空!");
        }
        if(CommUtil.isNull(input.getTeleno())){
            throw DpModuleError.DpstComm.E9999("电话号码不能为空!");
        }
        KnaAcdc knaAcdc = KnaAcdcDao.selectOne_odb2(input.getCustac(), false);
        if(CommUtil.isNull(knaAcdc)){
            throw DpModuleError.DpstComm.E9999("电子账户未开立或已销户!");
        }
        KnaCust knaCust = KnaCustDao.selectOne_odb1(knaAcdc.getCustac(), false);
//        CifCust cifCust = CifCustDao.selectOne_odb1(knaCust.getCustno(), false);
        KnaAcal knaAcal = KnaAcalDao.selectOne_odb1(knaAcdc.getCustac(),E_ACALTP.CELLPHONE, input.getTeleno(), false);
//        if(!CommUtil.equals(input.getIdtfno(), cifCust.getIdtfno())){
//            throw DpModuleError.DpstComm.E9999("证件号码输入有误!");
//        }
        if(CommUtil.isNull(knaAcal)){
            throw DpModuleError.DpstComm.E9999("电话号码输入有误!");
        }
    }

public static void aftRepswd( final cn.sunline.ltts.busi.catran.trans.intf.Repswd.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Repswd.Output output){
    //重置密码短信通知
//    IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
////  cplKubSqrd.setAppsid();//APP推送ID 
//    cplKubSqrd.setCardno(input.getCustac());//交易卡号  
////  cplKubSqrd.setPmvl01();//参数01    
////  cplKubSqrd.setPmvl02();//参数02    
////  cplKubSqrd.setPmvl03();//参数03    
////  cplKubSqrd.setPmvl04();//参数04    
////  cplKubSqrd.setPmvl05();//参数05    
//    cplKubSqrd.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());//内部交易码
//    cplKubSqrd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期  
//    cplKubSqrd.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());//交易流水  
//    cplKubSqrd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());//交易时间  
//    IoPbSmsSvcType svcType = SysUtil.getInstance(IoPbSmsSvcType.class);
//    svcType.pbTransqReg(cplKubSqrd);
    
        output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
        output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());//主交易日期
        output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());//主交易时间
    }
}
