package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.CfError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;

public class edidno {

	/**
	 * @author Xiaoyu Luo
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void changeIdtfnoOrCustna(
			final cn.sunline.ltts.busi.dptran.trans.intf.Edidno.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Edidno.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Edidno.Output output) {
		String cardno = input.getCardno();
		String custna = input.getCustna();
		E_IDTFTP idtftp = input.getIdtftp();
		String idtfno = input.getIdtfno();
		if (CommUtil.isNull(cardno)) {
			CfError.CfComm.E0002(BaseDict.Comm.cardno.getLongName());
		}
		if (CommUtil.isNull(custna)) {
			CfError.CfComm.E0002(BaseDict.Comm.custna.getLongName());
		}
		//证件类型、证件号码不能为空
		if (CommUtil.isNull(idtfno)) {
			CommTools.fieldNotNull(idtfno, BaseDict.Comm.idtfno.getId(), BaseDict.Comm.idtfno.getLongName());
		}
		if (CommUtil.isNull(idtftp)) {
			CommTools.fieldNotNull(idtftp, BaseDict.Comm.idtftp.getId(), BaseDict.Comm.idtftp.getLongName());
		}

		//校验证件类型、证件号码
        BusiTools.chkCertnoInfo(idtftp, idtfno);
        
		String custac = CaTools.getCustacByCardno(cardno);

		KnaCust knacust = KnaCustDao.selectOne_odb1(custac, false);
		String custno = knacust.getCustno();
//		IoCuCustSvcType ioCuCustSvcType = SysUtil.getInstance(IoCuCustSvcType.class);
//		ioCuCustSvcType.edIdtfnoOrCustna(custno, input.getCustna(), input.getIdtftp(), input.getIdtfno());
	}
}
