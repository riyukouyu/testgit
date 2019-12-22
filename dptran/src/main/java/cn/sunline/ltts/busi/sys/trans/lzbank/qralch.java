package cn.sunline.ltts.busi.sys.trans.lzbank;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsPckg;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsPckgDao;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsRedu;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsReduDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TXNSTS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUPSST;


public class qralch {
	/**
	 * 银联全渠道交易结果查询
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void qrAlchTran( final cn.sunline.ltts.busi.sys.trans.lzbank.intf.Qralch.Input input,  final cn.sunline.ltts.busi.sys.trans.lzbank.intf.Qralch.Property property,  final cn.sunline.ltts.busi.sys.trans.lzbank.intf.Qralch.Output output){
		String fronsq = input.getFronsq(); //渠道请求流水
		String frondt = input.getFrondt(); //渠道请求日期
		
		if(CommUtil.isNull(fronsq)){
			throw DpModuleError.DpstComm.E9999("渠道请求流水不能为空");
		}
		if(CommUtil.isNull(frondt)){
			throw DpModuleError.DpstComm.E9999("渠道请求日期不能为空");
		}
		
		KnsRedu tblKnsRedu = KnsReduDao.selectOne_odb1(fronsq, frondt, 1, false);
		KnlIoblCups tblKnlIoblCups = null;
		
		if(CommUtil.isNotNull(tblKnsRedu)){
			KnsPckg tblKnspckg = KnsPckgDao.selectOne_odb2(tblKnsRedu.getPckgdt(), tblKnsRedu.getPckgsq(), false);
			output.setRetrcd(tblKnspckg.getErrocd());//原交易响应码
			output.setRetrmg(tblKnspckg.getErrotx());//原交易响应信息
			if (tblKnsRedu.getTxnsts().equals(E_TXNSTS.SUCCESS)) {
				 tblKnlIoblCups = KnlIoblCupsDao.selectOne_odb2(input.getFronsq(), input.getFrondt(), false);
				 CommUtil.copyProperties(output, tblKnlIoblCups);
			}
		}else{
			throw DpModuleError.DpstComm.E9999("未收到请求，请确认流水和日期是否正确");
		}
			
	}
}
