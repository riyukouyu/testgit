package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.tables.DpMessTable.KnpPromMess;
import cn.sunline.ltts.busi.dp.tables.DpMessTable.KnpPromMessDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;

public class edmess {

	public static void messageSave(
			final cn.sunline.ltts.busi.aptran.trans.intf.Edmess.Input input,
			final cn.sunline.ltts.busi.aptran.trans.intf.Edmess.Property property,
			final cn.sunline.ltts.busi.aptran.trans.intf.Edmess.Output output) {

		String messno = input.getMessno();
		String messna = input.getMessna();
		String groupcd = ApUtil.MESS_GROUPCD;
		String contxt = input.getContxt();
		if (CommUtil.isNull(messna)) {
			throw DpModuleError.DpstComm.E9027("信息名称");
		}
		if (CommUtil.isNull(input.getIsprom())) {
			throw DpModuleError.DpstComm.E9027("提示标志");
		}
		if (CommUtil.isNull(contxt)) {
			throw DpModuleError.DpstComm.E9027("提示信息内容");
		}
		
		if (CommUtil.isNull(messno)) {
			messno = BusiTools.getSequence("messno", 10, "0");
		}
		KnpPromMess knpPromMess = KnpPromMessDao.selectOne_odb1(messno, false);
		if (CommUtil.isNull(knpPromMess)) {
			knpPromMess = SysUtil.getInstance(KnpPromMess.class);
			knpPromMess.setMessno(messno);
			knpPromMess.setMessna(messna);
			knpPromMess.setGropcd(groupcd);
			knpPromMess.setIsprom(input.getIsprom());
			knpPromMess.setContxt(contxt);
			KnpPromMessDao.insert(knpPromMess);
		} else {
			knpPromMess.setMessno(messno);
			knpPromMess.setMessna(messna);
			knpPromMess.setGropcd(groupcd);
			knpPromMess.setIsprom(input.getIsprom());
			knpPromMess.setContxt(contxt);
			KnpPromMessDao.updateOne_odb1(knpPromMess);
		}
	}
	
}
