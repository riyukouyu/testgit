package cn.sunline.ltts.busi.dptran.trans.lzbank;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsRedu;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsReduDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STATE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TXNSTS;


public class qrcups {

	public static void qrCupsTran( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qrcups.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qrcups.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Qrcups.Output output){
		String fronsq = input.getFronsq();
		String frondt = input.getFrondt();
		if(CommUtil.isNull(fronsq)){
			throw DpModuleError.DpstComm.E9999("支付前置流水不能为空");
		}
		if(CommUtil.isNull(frondt)){
			throw DpModuleError.DpstComm.E9999("支付前置日期不能为空");
		}
		int oDate = Integer.parseInt(frondt);
		int nDate = Integer.parseInt(CommTools.getBaseRunEnvs().getTrxn_date());
		if (CommUtil.compare(nDate, oDate) > 30){
			output.setTranst(E_STATE.WZDYJY);
		}else{
			KnsRedu tblKnsRedu = KnsReduDao.selectOne_odb1(fronsq, frondt, 1, false);
			if(CommUtil.isNotNull(tblKnsRedu)){
				if(E_TXNSTS.STRIKED == tblKnsRedu.getTxnsts()){
					output.setTranst(E_STATE.STRK);
					output.setRetrcd(tblKnsRedu.getErrocd());//原交易响应码
				}else if(E_TXNSTS.SUCCESS == tblKnsRedu.getTxnsts()) {
					output.setTranst(E_STATE.SUCCESS);
					output.setRetrcd(tblKnsRedu.getErrocd());//原交易响应码
				}else if(E_TXNSTS.FAILURE == tblKnsRedu.getTxnsts()) {
					output.setTranst(E_STATE.FAILURE);
					if (!CommUtil.equals("Passwd.E9901", tblKnsRedu.getErrocd()) || !CommUtil.equals("Passwd.E0002", tblKnsRedu.getErrocd())){
						output.setRetrcd(tblKnsRedu.getErrocd().substring(tblKnsRedu.getErrocd().indexOf(".")));//原交易响应码
					}else{
						output.setRetrcd(tblKnsRedu.getErrocd());//原交易响应码
					}
				}
			}else{
				output.setTranst(E_STATE.WZDYJY);
			}
		}
		
	}
}
