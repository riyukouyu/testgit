package cn.sunline.ltts.busi.in.serviceimpl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcctDao;
import cn.sunline.ltts.busi.iobus.type.IoInTable.IoGlKnsGlvc;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
 /**
  * 查询内部户相关表信息服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoInSrvQryTableInfoImpl", longname="查询内部户相关表信息服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoInSrvQryTableInfoImpl implements cn.sunline.ltts.busi.iobus.servicetype.in.IoInSrvQryTableInfo{
 /**
  * 根据内部账号查询账户信息
  *
  */
	public cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo selKnaGlAcctnoByAcctno( String acctno, Boolean epflag){
		
		IoInacInfo cplInacInfo = null;
		
		GlKnaAcct tblGlKnaAcct = GlKnaAcctDao.selectOne_odb1(acctno, epflag);
		
		if(CommUtil.isNotNull(tblGlKnaAcct)){
			cplInacInfo = SysUtil.getInstance(IoInacInfo.class);
			CommUtil.copyProperties(cplInacInfo, tblGlKnaAcct);
		}
		
		return cplInacInfo;
	}

public cn.sunline.ltts.busi.iobus.type.IoInTable.IoGlKnsGlvc selGlKnsGlvcByTransq( String transq){
	IoGlKnsGlvc ioGlKnsGlvc = InQuerySqlsDao.selGlKnsGlvcByTransq(transq, false);
	return ioGlKnsGlvc;
}

}

