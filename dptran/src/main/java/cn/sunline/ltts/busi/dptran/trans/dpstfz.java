package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.fn.type.FnfunctionType.IoFinaProd;

public class dpstfz {


	public static void saveFnbProd( final cn.sunline.ltts.busi.dptran.trans.intf.Dpstfz.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Dpstfz.Output output){
		if (CommUtil.isNull(input.getSprdvr())) {
			throw DpModuleError.DpstComm.BNAS1113();
		}

		if (CommUtil.isNull(input.getEndday())) {
			throw DpModuleError.DpstComm.BNAS0493();
		}

		if (CommUtil.isNull(input.getFtrate())) {
			throw DpModuleError.DpstComm.BNAS0447();
		}

		if (CommUtil.isNull(input.getProdcd())) {
			throw DpModuleError.DpstComm.BNAS1052();
		}

		if (CommUtil.isNull(input.getSprdid())) {
			throw DpModuleError.DpstComm.BNAS0545();
		}

		if (CommUtil.isNull(input.getTtrate())) {
			throw DpModuleError.DpstComm.BNAS0446();
		}

		if (CommUtil.isNull(input.getSprdna())) {
			throw DpModuleError.DpstComm.BNAS0544();
		}
		
		// 登记理财产品附加信息
//		IoFnSevQryTableInfo fnQry = SysUtil.getInstance(IoFnSevQryTableInfo.class);
		IoFinaProd finaInfo = SysUtil.getInstance(IoFinaProd.class);

		CommUtil.copyProperties(finaInfo, input);
		finaInfo.setFrozdt(CommTools.getBaseRunEnvs().getTrxn_date());
		finaInfo.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());

//		fnQry.fnb_prod_insert(finaInfo);
		//return null;
	}
}
