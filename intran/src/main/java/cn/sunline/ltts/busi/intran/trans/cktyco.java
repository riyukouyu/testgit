package cn.sunline.ltts.busi.intran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnpBusi;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.PbError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRTRTP;


public class cktyco {

public static void rechkTyinco( String prodcd,  String tyinno,  final cn.sunline.ltts.busi.intran.trans.intf.Cktyco.Output output){
	
	String Clerbr = BusiTools.getBusiRunEnvs().getCentbr();//获取省联社清算中心
	String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();//维护柜员
	if(!CommUtil.equals(tranbr,Clerbr)){
		throw PbError.Branch.E0002("非省清算中心不允许操作！");
	}	
	// 输入项非空检查
	if (CommUtil.isNull(prodcd)) {
		throw DpModuleError.DpstComm.E9999("产品编号输入不能为空");
	}
	if (CommUtil.isNull(tyinno)) {
		throw DpModuleError.DpstComm.E9999("录入编号输入不能为空");
	}
	String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
	// 获取产品信息
	GlKnpBusi tblGlKnpBusiOld = InQuerySqlsDao.selBusinoDetail(prodcd, corpno, false);
	if (CommUtil.isNull(tblGlKnpBusiOld)) {
		throw DpModuleError.DpstComm.E9999("产品编号不存在");
	}
	
	// 检查产品状态是否为待复核的状态
	if (E_PRODST.ASSE != tblGlKnpBusiOld.getBusist()) {
		throw DpModuleError.DpstComm.E9999("不存在待复核的产品");
	}
	
	// 判断录入编号是否正确
	if (!CommUtil.equals(tyinno, tblGlKnpBusiOld.getTyinno())) {
		throw DpModuleError.DpstComm.E9999("录入编号输入错误");
	}

	String tranus = InacSqlsDao.selUserByBusino(E_BUSIBI.INNE, prodcd, E_PRTRTP.ADD, true);
	output.setUserid(tranus);// 交易柜员
}

}

