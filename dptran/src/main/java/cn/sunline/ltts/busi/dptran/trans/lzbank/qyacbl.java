package cn.sunline.ltts.busi.dptran.trans.lzbank;


import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.in.serviceimpl.IoInOpenCloseImpl;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcctDao;
import cn.sunline.ltts.busi.in.type.InQueryTypes.InacBlInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery;
import cn.sunline.ltts.busi.iobus.type.in.IoInOpenCloseComplex.IoInacOpen_IN;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInAcctTmp;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class qyacbl {

public static void chkQyacblInfo( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qyacbl.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qyacbl.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qyacbl.Output output){
    BizLog bizLog = BizLogUtil.getBizLog(qyacbl.class);
	bizLog.info("开始内部户余额明细查询");
	String qszttp = input.getQszttp();
	if(CommUtil.isNull(qszttp)){
	    throw CaError.Eacct.E0001("清算主体类型不能为空");
	}
	KnpParameter para = KnpParameterDao.selectOne_odb1("LnParam", "LnCler", qszttp+"1", qszttp, true);
	String busino = para.getParm_value1();  //费用待划转账户的业务编码 
	InacBlInfo info = SysUtil.getInstance(InacBlInfo.class);
	IoInacOpen_IN inopac = SysUtil.getInstance(IoInacOpen_IN.class);
	inopac.setBusino(busino);//业务编码
    inopac.setSubsac(para.getParm_value2());//子户号
    inopac.setCrcycd(para.getParm_value3());//币种
    //如果出账账号不存在，则新开。存在，则返回账号
    String repacc= SysUtil.getInstance(IoInOpenCloseImpl.class).inacOpen(inopac);//获取费用待划转账号——出账账号
    GlKnaAcct acct = GlKnaAcctDao.selectOne_odb1(repacc, true);
    IoInQuery query = SysUtil.getInstance(IoInQuery.class);
    IoInAcctTmp tmp = query.InacBalQuery(acct.getAcctno());//查询内部户余额信息——出账账户
    
    KnpParameter para1 = KnpParameterDao.selectOne_odb1("LnParam", "LnCler", qszttp+"2", qszttp, true);
    info.setAcctno(para1.getParm_value1());//内部户账号——入账账号
    info.setAcctna(para1.getParm_value4());//内部户名称——入账账户名称
    info.setRepacc(acct.getAcctno());//费用待划转账户——出账账号
    info.setRepana(acct.getAcctna());//费用待划转账户名称——出账账号名称
    info.setOnlnbl(tmp.getCrctbl());//费用待划转账联机贷方余额
    output.setInacBlInfo(info);
}


}
