package cn.sunline.ltts.busi.dptran.trans.online;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;


public class opacct {
	//private static final BizLog bizlog = BizLogUtil.getBizLog(BizLog.class);
	
	public static void chkCustInfo( final cn.sunline.ltts.busi.dptran.trans.online.intf.Opacct.Input Input,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opacct.Property Property,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opacct.Output Output){
		
		String custna = Input.getCustna(); //客户名称
		String idtfno = Input.getIdtfno(); //证件号码
		E_IDTFTP idtftp = Input.getIdtftp(); //证件类型
		String teleno = Input.getTeleno(); //联系方式
		String tranpw = Input.getTranpw(); //交易密码
		String crcycd = Input.getCrcycd(); //货币代号
		
		//客户名称不能为空，并且长度不能超过100
		if (CommUtil.isNull(custna)) {
			CommTools.fieldNotNull(custna, BaseDict.Comm.custna.getId(), BaseDict.Comm.custna.getLongName());
		}
		if(custna.getBytes().length > 100){
			throw DpModuleError.DpstComm.BNAS0523();
		}
		//证件类型、证件号码不能为空
		if (CommUtil.isNull(idtfno)) {
			CommTools.fieldNotNull(idtfno, BaseDict.Comm.idtfno.getId(), BaseDict.Comm.idtfno.getLongName());
		}
		if (CommUtil.isNull(idtftp)) {
			CommTools.fieldNotNull(idtftp, BaseDict.Comm.idtftp.getId(), BaseDict.Comm.idtftp.getLongName());
		}
		//联系方式不能为空，长度为11位
		if (CommUtil.isNull(teleno)) {
			CommTools.fieldNotNull(teleno, BaseDict.Comm.teleno.getId(), BaseDict.Comm.teleno.getLongName());
		}
		if(teleno.length() != 11){
			throw DpModuleError.DpstComm.BNAS0469();
		}
		//检查交易密码
		if(CommUtil.isNull(tranpw)){
			throw CaError.Eacct.BNAS0609();
		}
		//货币代号
		if(CommUtil.isNull(crcycd)){
			CommTools.fieldNotNull(crcycd, BaseDict.Comm.crcycd.getId(), BaseDict.Comm.crcycd.getLongName());
		}
		//校验证件类型、证件号码
        BusiTools.chkCertnoInfo(idtftp, idtfno);

	}

	public static void prcOpacctOut( final cn.sunline.ltts.busi.dptran.trans.online.intf.Opacct.Input Input,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opacct.Property Property,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opacct.Output Output){
		Output.setCustac(Property.getCustac());
		//Output.setCustna(Property.getCustna());
		//Output.setOpendt(Property.getOpendt());
		//Output.setOpensq(Property.getOpensq());
	}
}
